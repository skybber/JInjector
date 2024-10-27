package org.hotswap.jinjector.parser;

import org.hotswap.jinjector.javassist.compiler.*;
import org.hotswap.jinjector.javassist.compiler.ast.ASTList;
import org.hotswap.jinjector.javassist.compiler.ast.FieldDecl;
import org.hotswap.jinjector.javassist.compiler.ast.MethodDecl;
import org.hotswap.jinjector.javassist.compiler.ast.Stmnt;
import org.hotswap.jinjector.logging.AgentLogger;
import org.hotswap.jinjector.patch.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PatchParser {
    private static final AgentLogger LOGGER = AgentLogger.getLogger(PatchParser.class);
    public static final String TR_ELEM_FIELD = "$field";
    public static final String TR_ELEM_METHOD = "$method";
    public static final String TR_ELEM_CONSTRUCTOR = "$constructor";
    public static final String TR_FIELD_TYPE_RENAME = "rename";
    public static final String TR_FIELD_TYPE_REMOVE = "remove";
    private static final String TR_METHOD_INSERT_BEFORE = "insertBefore";
    private static final String TR_METHOD_INSERT_AFTER = "insertAfter";
    private static final String TR_METHOD_SET_BODY = "setBody";
    private static final Map<String, TransformMethodFragment.TransformType> methodTransformTypes = Map.of(
            TR_METHOD_INSERT_BEFORE, TransformMethodFragment.TransformType.INSERT_BEFORE,
            TR_METHOD_INSERT_AFTER, TransformMethodFragment.TransformType.INSERT_AFTER,
            TR_METHOD_SET_BODY, TransformMethodFragment.TransformType.SET_BODY
    );

    private static final Set<String> transformElements = Set.of(TR_ELEM_FIELD, TR_ELEM_METHOD, TR_ELEM_CONSTRUCTOR);
    private static final Set<String> transformFieldTypes = Set.of(TR_FIELD_TYPE_RENAME, TR_FIELD_TYPE_REMOVE);

    private String fileName;
    private Lex lex;

    public Patch parseFile(String fileName) {
        try {
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                this.fileName = fileName;
                return doParse(Files.readString(filePath));
            } else {
                LOGGER.error("Patch file {} does not exists!", fileName);
            }
        } catch (IOException e) {
            LOGGER.error("IO exception. {}", e);
        }
        return null;
    }

    public Patch parseStream(InputStream inputStream) {
        try {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parse(content);
        } catch (IOException e) {
            LOGGER.error("IO exception. {}", e);
        }
        return null;
    }
    public Patch parse(String content) {
        fileName = null;
        return doParse(content);
    }

    public Patch doParse(String content) {
        lex = new Lex(content);
        return parsePatch();
    }

    private Patch parsePatch() {
        try {
            Patch result = new Patch();
            while (lex.lookAhead() >= 0) {
                parsePatchPart(result);
            }
            return result;
        } catch (JInjectorSyntaxError e) {
            if (fileName != null) {
                LOGGER.error("Patch file `{}` parse error. Line: {} : {}", fileName, e.getLineNumber(), e.getMessage());
            } else {
                LOGGER.error("Parse error. Line: {} : {}", e.getLineNumber(), e.getMessage());
            }
        }
        return null;
    }

    private void parsePatchPart(Patch result) throws JInjectorSyntaxError {
        readExpectChar('@');
        if (lex.get() != TokenId.Identifier) {
            throw new JInjectorSyntaxError("Expected an identifier following '@',", lex);
        }
        if ("Transform".equals(lex.getString())) {
            result.addTransform(parseTransform());
        } else if ("Create".equals(lex.getString())) {
            result.addCreate(parseCreate());
        } else {
            throw new JInjectorSyntaxError("Unknown keyword '" + lex.getString() + "'.", lex);
        }
    }

    private Transform parseTransform() throws JInjectorSyntaxError {
        boolean onStart = false;
        if (lex.lookAhead() == '(') {
            lex.get();
            int tok = lex.get();
            if (tok == TokenId.Identifier && "onStart".equals(lex.getString())) {
                onStart = true;
                tok = lex.get();
            }
            if (tok != ')') {
                throwExpectChar(')', tok);
            }
        }

        readExpectedKeyword(TokenId.CLASS);
        String className = readExpectClassName();
        String simpleClassName = getSimpleClassName(className);
        readExpectChar('{');

        Transform transform = new Transform(className, onStart);

        while (true) {
            int tok = lex.lookAhead();
            if (tok < 0) {
                throwUnexpectedEndOfFile();
            }
            if (tok == '}') {
                break;
            }
            tok = lex.get();
            if (tok != TokenId.Identifier || !transformElements.contains(lex.getString())) {
                throw new JInjectorSyntaxError("Expected $field or $constructor or $method, but '" + getTokenAsString(tok) + "' found.", lex);
            }
            if (TR_ELEM_FIELD.equals(lex.getString())) {
                if (lex.lookAhead() == '.') {
                    transform.addNewField(parseNewField());
                } else {
                    transform.addTransformField(parseTransformField());
                }
            } else if (TR_ELEM_CONSTRUCTOR.equals(lex.getString())) {
                if (lex.lookAhead() == '.') {
                    transform.addNewConstructor(parseNewMethod(true));
                } else {
                    transform.addTransformConstructor(parseTransformConstructor(simpleClassName));
                }
            } else if (TR_ELEM_METHOD.equals(lex.getString())) {
                if (lex.lookAhead() == '.') {
                    transform.addNewMethod(parseNewMethod(false));
                } else {
                    transform.addTransformMethod(parseTransformMethod());
                }
            } else {
                // unreachable
                throw new JInjectorSyntaxError(lex);
            }
        }

        readExpectChar('}');
        return transform;
    }

    private TransformField parseTransformField() throws JInjectorSyntaxError {
        readExpectChar('(');
        int tok = lex.get();
        if (tok != TokenId.Identifier) {
            throw new JInjectorSyntaxError("Field name expected, but '" + getTokenAsString(tok) + "' found.", lex);
        }
        String fieldName = lex.getString();
        readExpectChar(')');
        readExpectChar('.');
        tok = lex.get();
        if (tok != TokenId.Identifier || !transformFieldTypes.contains(lex.getString())) {
            throw new JInjectorSyntaxError("Expected field transform type 'rename' or 'remove', but '" + getTokenAsString(tok) + "' found.", lex);
        }
        TransformField.FieldTransformType ftt;
        String renameTo = null;
        if (TR_FIELD_TYPE_RENAME.equals(lex.getString())) {
            ftt = TransformField.FieldTransformType.RENAME;
            readExpectChar('(');
            tok = lex.get();
            if (tok != TokenId.Identifier) {
                throw new JInjectorSyntaxError("A new field name is expected at this point, but '" + getTokenAsString(tok) + "' found.", lex);
            }
            renameTo = lex.getString();
            readExpectChar(')');
            readExpectChar(';');
        } else {
            ftt = TransformField.FieldTransformType.REMOVE;
            readExpectChar('(');
            readExpectChar(')');
            readExpectChar(';');
        }
        return new TransformField(fieldName, ftt, renameTo);
    }

    private TransformConstructor parseTransformConstructor(String expectedClassName) throws JInjectorSyntaxError {
        readExpectChar('(');
        int tok = lex.get();
        if (tok != TokenId.Identifier) {
            throw new JInjectorSyntaxError("Constructor name expected, but '" + getTokenAsString(tok) + "' found.", lex);
        }
        String className = lex.getString();
        if (!className.equals(expectedClassName)) {
            throw new JInjectorSyntaxError("Constructor name does not match class name '" + expectedClassName + "'.", lex);
        }
        readExpectChar('(');
        List<String> paramClasses = readMethodParams();
        readExpectChar(')');
        TransformConstructor result = new TransformConstructor(paramClasses != null, paramClasses);
        readTransformMethodFragments(result);
        return result;
    }

    private TransformMethod parseTransformMethod() throws JInjectorSyntaxError {
        readExpectChar('(');
        int tok = lex.get();
        if (tok != TokenId.Identifier) {
            throw new JInjectorSyntaxError("Method name expected, but '" + getTokenAsString(tok) + "' found.", lex);
        }
        String methodName = lex.getString();
        readExpectChar('(');
        List<String> paramClasses = readMethodParams();
        readExpectChar(')');
        TransformMethod result = new TransformMethod(methodName, paramClasses != null, paramClasses);
        readTransformMethodFragments(result);
        return result;
    }

    private NewField parseNewField() throws JInjectorSyntaxError {
        readExpectChar('.');
        readExpectedKeyword(TokenId.NEW);
        readExpectChar('(');
        readExpectChar(')');
        readExpectChar('{');
        String src = trimEmptyLines(readNewFieldSrc()).trim();
        readExpectChar('}');
        if (src.isEmpty()) {
            throw new JInjectorSyntaxError("Source is empty.", lex);
        }
        return new NewField(src);}

    private NewMethod parseNewMethod(boolean isConstructor) throws JInjectorSyntaxError {
        readExpectChar('.');
        readExpectedKeyword(TokenId.NEW);
        readExpectChar('(');
        readExpectChar(')');
        readExpectChar('{');
        String src = trimEmptyLines(readNewMethodSrc(isConstructor)).trim();
        readExpectChar('}');
        if (src.isEmpty()) {
            throw new JInjectorSyntaxError("Source is empty.", lex);
        }
        return new NewMethod(src);
    }

    private List<String> readMethodParams() throws JInjectorSyntaxError {
        List<String> result = null;
        if (lex.lookAhead() == '*') {
            lex.get();
            readExpectChar(')');
        } else {
            result = new ArrayList<>();
            if (lex.lookAhead() != ')') {
                while (true) {
                    String paramType;
                    if (isBuiltinType(lex.lookAhead())) {
                        paramType = getTokenAsString(lex.get());
                    } else {
                        paramType = readExpectClassName();
                    }

                    if (lex.lookAhead() == '[') {
                        lex.get();
                        readExpectChar(']');
                    }
                    result.add(paramType);
                    if (lex.lookAhead() == ')') {
                        lex.get();
                        break;
                    }
                    readExpectCharWithExpectSet(',', "')' or ','");
                }
            } else {
                lex.get();
            }
        }
        return result;
    }

    private String readNewFieldSrc() throws JInjectorSyntaxError {
        int startPos = lex.getPosition();
        int startLineNumber = lex.getLineNumber();
        while (true) {
            int tok = lex.lookAhead();
            if (tok < 0) {
                throwUnexpectedEndOfFile();
            }
            if (tok == '}') {
                String src = lex.getInput().substring(startPos, lex.getPosition()-1);
                parseNewFieldByJvst(startLineNumber, src);
                return src;
            }
            lex.get();
        }
    }

    private void parseNewFieldByJvst(int startLineNumber, String src) throws JInjectorSyntaxError {
        Lex sourceLex = new Lex(src);
        Parser p = new Parser(sourceLex);
        try {
            while (p.hasMore()) {
                ASTList mem = p.parseMember(new SymbolTable());
                if (!(mem instanceof FieldDecl)) {
                    throwFieldDeclExpected();
                }
            }
        } catch (CompileError e) {
            int lineNumber = (startLineNumber - 1) + sourceLex.getLineNumber();
            throw new JInjectorSyntaxError("Code parse error: " + e.getMessage(), lex, lineNumber);
        }
    }

    private String readNewMethodSrc(boolean isConstructor) throws JInjectorSyntaxError {
        int startPos = lex.getPosition();
        int startLineNumber = lex.getLineNumber();
        int parCount = 1;
        while (true) {
            int tok = lex.lookAhead();
            if (tok < 0) {
                throwUnexpectedEndOfFile();
            }
            if (tok == '{') {
                parCount ++;
            } else if (tok == '}') {
                parCount --;
                if (parCount <= 0) {
                    String src = lex.getInput().substring(startPos, lex.getPosition()-1);
                    parseNewMethodSrcByJvst(startLineNumber, isConstructor, src);
                    return src;
                }
            }
            lex.get();
        }
    }

    private void parseNewMethodSrcByJvst(int startLineNumber, boolean isConstructor, String src) throws JInjectorSyntaxError {
        Lex sourceLex = new Lex(src);
        Parser p = new Parser(sourceLex);
        try {
            while (p.hasMore()) {
                ASTList mem = p.parseMember(new SymbolTable());
                if (!(mem instanceof MethodDecl)) {
                    if (isConstructor) {
                        throwConstructorDefExpected();
                    }
                    throwMethodDefExpected();
                }
                MethodDecl methodDecl = (MethodDecl) mem;
                if (isConstructor && !methodDecl.isConstructor()) {
                    throwConstructorDefExpected();
                }
                if (!isConstructor && methodDecl.isConstructor()) {
                    throwMethodDefExpected();
                }
            }
        } catch (CompileError e) {
            int lineNumber = (startLineNumber-1) + sourceLex.getLineNumber();
            throw new JInjectorSyntaxError("Code parse error: " + e.getMessage(), lex, lineNumber);
        }
    }

    private void readTransformMethodFragments(TransformMethodBase transformMethod) throws JInjectorSyntaxError {
        while (lex.lookAhead() == '.') {
            lex.get();
            String stt = readExpectIdentif();
            TransformMethodFragment.TransformType transformType = methodTransformTypes.get(stt);

            if (transformType == null) {
                throw new JInjectorSyntaxError("Invalid transform type '" + stt + "'.", lex);
            }
            readExpectChar('{');
            String src = trimEmptyLines(readTransformMethodFragmentSrc()).trim();
            if (!src.isEmpty()) {
                transformMethod.addPatchFragment(new TransformMethodFragment(transformType, src));
            }
            readExpectChar('}');
        }
    }

    private String readTransformMethodFragmentSrc() throws JInjectorSyntaxError {
        int startPos = lex.getPosition();
        int startLineNumber = lex.getLineNumber();
        int parCount = 1;
        while (true) {
            int tok = lex.lookAhead();
            if (tok < 0) {
                throwUnexpectedEndOfFile();
            }
            if (tok == '{') {
                parCount ++;
            } else if (tok == '}') {
                parCount --;
                if (parCount <= 0) {
                    String src = lex.getInput().substring(startPos, lex.getPosition()-1);
                    parseTransformMethodFragmentByJvst(startLineNumber, src);
                    return src;
                }
            }
            lex.get();
        }
    }

    private void parseTransformMethodFragmentByJvst(int startLineNumber, String src) throws JInjectorSyntaxError {
        Lex fragmentLex = new Lex(src);
        Parser p = new Parser(fragmentLex);
        try {
            while (p.hasMore()) {
                Stmnt s = p.parseStatement(new SymbolTable());
            }
        } catch (CompileError e) {
            int lineNumber = (startLineNumber-1) + fragmentLex.getLineNumber();
            throw new JInjectorSyntaxError("Code fragment parse error: " + e.getMessage(), lex, lineNumber);
        }
    }

    private Create parseCreate() throws JInjectorSyntaxError {
        readExpectedKeyword(TokenId.CLASS);
        String className = readExpectClassName();
        String _extends = null;
        if (lex.lookAhead() == TokenId.EXTENDS) {
            lex.get();
           _extends = readExpectClassName();
        }
        List<String> _implements = null;
        if (lex.lookAhead() == TokenId.IMPLEMENTS) {
            lex.get();
            _implements = readExpectedClassNameList();
        }
        Create result = new Create(className, _extends, _implements);
        readExpectChar('{');
        int startPos = lex.getPosition();
        int endPos;
        int linesProcessed = 0;
        Lex classElemLex;
        do {
            expectNotEnd();
            String src = lex.getInput().substring(startPos);
            classElemLex = new Lex(src);
            Parser p = new Parser(classElemLex);
            try {
                ASTList mem = p.parseMember(new SymbolTable());
                endPos = startPos + classElemLex.getPosition();
                linesProcessed += classElemLex.getLineNumber()-1;
                String parsedSrc = classElemLex.getInput().substring(0, classElemLex.getPosition());
                if (mem instanceof FieldDecl) {
                    result.appendField(parsedSrc);
                } else if (mem instanceof MethodDecl) {
                    MethodDecl methodDecl = (MethodDecl) mem;
                    if (methodDecl.isConstructor()) {
                        result.appendConstructor(parsedSrc);
                    } else {
                        result.appendMethod(parsedSrc);
                    }
                } else {
                    int lineNumber = lex.getLineNumber() + classElemLex.getLineNumber() + linesProcessed;
                    throw new JInjectorSyntaxError("Unexpected code element.", lex, lineNumber);
                }
                startPos = endPos;
            } catch (CompileError e) {
                int lineNumber = (lex.getLineNumber()-1) + (classElemLex.getLineNumber()-1) + linesProcessed;
                throw new JInjectorSyntaxError("Code fragment parse error: " + e.getMessage(), lex, lineNumber);
            }
        } while (classElemLex.lookAhead() != '}');
        lex.setPosition(endPos);
        readExpectChar('}');
        return result;
    }

    private String readExpectClassName() throws JInjectorSyntaxError {
        StringBuilder result = new StringBuilder();
        while (true) {
            result.append(readExpectIdentif());
            if (lex.lookAhead() != '.') {
                break;
            }
            lex.get();
            result.append(".");
        }
        return result.toString();
    }

    private List<String> readExpectedClassNameList() throws JInjectorSyntaxError {
        List<String> result = new ArrayList<>();
        while (true) {
            String className = readExpectClassName();
            if (result.contains(className)) {
                throw new JInjectorSyntaxError("Duplicate interface '" + className + "' found in the implement list.", lex);
            }
            result.add(className);
            if (lex.lookAhead() != ',') {
                break;
            }
            lex.get();
        }
        return result;
    }

    private void throwUnexpectedEndOfFile() throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Unexpected end of file.", lex);
    }

    private void throwExpectChar(char expected, int found) throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Character '" + expected + "' expected, but '" + getTokenAsString(found) + "' found.", lex);
    }

    private void throwExpectChars(String expectedChars, int found) throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Characters " + expectedChars + " expected, but '" + getTokenAsString(found) + "' found.", lex);
    }

    private void throwFieldDeclExpected() throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Field definition expected.", lex);
    }

    private void throwConstructorDefExpected() throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Constructor definition expected.", lex);
    }

    private void throwMethodDefExpected() throws JInjectorSyntaxError {
        throw new JInjectorSyntaxError("Method definition expected.", lex);
    }

    private void readExpectChar(char expected) throws JInjectorSyntaxError {
        int tok = lex.get();
        if (tok != expected) {
            throwExpectChar(expected, tok);
        }
    }

    private void readExpectCharWithExpectSet(char expected, String expectedChars) throws JInjectorSyntaxError {
        int tok = lex.get();
        if (tok != expected) {
            throwExpectChars(expectedChars, tok);
        }
    }

    private String readExpectIdentif() throws JInjectorSyntaxError {
        int tok = lex.get();
        if (tok != TokenId.Identifier) {
            throw new JInjectorSyntaxError("Identifier expected, but '" + getTokenAsString(tok) + "' found.", lex);
        }
        return lex.getString();
    }

    private void readExpectedKeyword(int expectedKw) throws JInjectorSyntaxError {
        int tok = lex.get();
        if (tok != expectedKw) {
            throw new JInjectorSyntaxError("'" + getTokenAsString(expectedKw) + "' keyword expected, but '" + getTokenAsString(tok) + "' found.", lex);
        }
    }

    public static String trimEmptyLines(String text) {
        return text.replaceAll("(?m)^\\s+", "")
                .replaceAll("(?m)\\s+$", "");
    }

    private String getTokenAsString(int tok) {
        return TokenId.getTokenAsString(lex, tok);
    }

    private String getSimpleClassName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return className.substring(lastDotIndex + 1);
        }
        return className;
    }

    private void expectNotEnd() throws JInjectorSyntaxError {
        if (lex.lookAhead() <= 0) {
            throw new JInjectorSyntaxError("Reached unexpected end of source.", lex);
        }
    }

    public static boolean isBuiltinType(int t) {
        return (t == TokenId.BOOLEAN || t == TokenId.BYTE || t == TokenId.CHAR || t == TokenId.SHORT
            || t == TokenId.INT || t == TokenId.LONG || t == TokenId.FLOAT || t == TokenId.DOUBLE);
    }
}
