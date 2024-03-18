package org.hotswap.patcher.parser;

import org.hotswap.patcher.HotswapPatcher;
import org.hotswap.patcher.logging.AgentLogger;
import org.hotswap.patcher.patch.ClassPatch;
import org.hotswap.patcher.patch.MethodPatch;
import org.hotswap.patcher.patch.MethodPatchFragment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The type Patch parser.
 */
public class PatchParser {
    private static AgentLogger LOGGER = AgentLogger.getLogger(HotswapPatcher.class);

    private static final String INSERT_BEFORE = "insertBefore";
    private static final String INSERT_AFTER = "insertAfter";

    private static final Map<String, MethodPatchFragment.TransformType> methodTransformTypes;

    static {
        methodTransformTypes = new HashMap<>();
        methodTransformTypes.put(INSERT_BEFORE, MethodPatchFragment.TransformType.INSERT_BEFORE);
        methodTransformTypes.put(INSERT_AFTER, MethodPatchFragment.TransformType.INSERT_AFTER);
    }
    
    private class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    private String fileName;
    private String content;
    private int line;
    private int pos;
    private int end;

    public PatchParser() {
    }

    public List<ClassPatch> parseFile(String fileName) {
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

    public List<ClassPatch> parse(String content) {
        fileName = null;
       return doParse(content);
    }
    public List<ClassPatch> doParse(String content) {
        this.content = content;
        return parsePatch();
    }

    private void init() {
        pos = 0;
        end = content.length();
        line = 1;
    }

    private List<ClassPatch> parsePatch() {
        List<ClassPatch> result = new ArrayList<>();
        init();
        try {
            while (pos < end) {
                readExpectKeyword("class");
                readExpectWs();
                String className = readClassName();
                readExpectChar('{');
                ClassPatch classPatch = new ClassPatch(className);
                parseClassBody(classPatch);
                readExpectChar('}');
                result.add(classPatch);
                skipWs();
            }
            return result;
        } catch (ParseException e) {
            if (fileName != null) {
                LOGGER.error("Patch file `{}` parse error. Line: {} : {}", fileName, line, e.getMessage());
            } else {
                LOGGER.error("Parse error. Line: {} : {}", line, e.getMessage());
            }
        }
        return null;
    }

    private void parseClassBody(ClassPatch classPatch) throws ParseException {
        while (true) {
            String methodName = readIdentif();
            if (methodName.isEmpty()) {
                return;
            }
            readExpectChar('(');
            skipWs();
            boolean allMethods = false;
            List<String> paramClasses = null;
            if (nextCharMatch('*')) {
                readChar();
                readExpectChar(')');
                allMethods = true;
            } else {
                paramClasses = new ArrayList<>();
                if (!nextCharMatch(')')) {
                    while (true) {
                        String paramClassName = readClassName();
                        if (nextCharMatch('[')) {
                            readChar();
                            readExpectChar(']');
                        }
                        paramClasses.add(paramClassName);
                        if (nextCharMatch(')')) {
                            readChar();
                            break;
                        }
                        readExpectChar(',');
                    }
                } else {
                   readChar();
                }
            }
            readExpectChar('{');
            MethodPatch methodPatch = new MethodPatch(methodName, allMethods, paramClasses);
            parseMethodBody(methodPatch);
            classPatch.addMethodPatch(methodPatch);
            readExpectChar('}');
        }
    }

    private void parseMethodBody(MethodPatch methodPatch) throws ParseException {
        while (true) {
            skipWs();
            if (nextCharMatch('}')) {
                return;
            }
            String strTransformType = readIdentif();
            if (strTransformType.isEmpty()) {
                throw new ParseException("Transform type expected.");
            }
            MethodPatchFragment.TransformType transformType = methodTransformTypes.get(strTransformType);
            if (transformType == null) {
                throw new ParseException("Invalid transform type\"" + strTransformType + "\".");
            }
            readExpectChar('{');
            String patchFragmentBody = trimEmptyLines(readPatchFragmentBody()).trim();
            if (!patchFragmentBody.isEmpty()) {
                methodPatch.addPatchFragment(new MethodPatchFragment(transformType, patchFragmentBody));
            }
            readExpectChar('}');
        }
    }

    private String readPatchFragmentBody() {
        int parCount = 1;
        boolean inComment = false;
        char prevChar = '{';
        StringBuilder result = new StringBuilder();
        while (true) {
            Character c = readChar();
            if (c == null) {
                break;
            }
            if (c == '/' && nextCharMatch('*')) {
                inComment = true;
            } else if (inComment && c == '/' && prevChar == '*') {
                inComment = false;
            } else {
                if (!inComment) {
                    if (c == '{') {
                        parCount++;
                    }
                    if (c == '}') {
                        parCount--;
                        if (parCount == 0) {
                            undoRead();
                            break;
                        }
                    }
                    result.append(c);
                }
            }
            prevChar = c;
        }

        return result.toString();
    }

    private void readExpectKeyword(String keyword) throws ParseException {
        String ident = readIdentif();
        if (!keyword.equals(ident)) {
            throw new ParseException("Keyword \"" + keyword + "\" expected.");
        }
    }

    private String readClassName() throws ParseException {
        StringBuilder className = new StringBuilder();
        boolean skipWs = true;
        while (true) {
            String identif = readIdentif(skipWs);
            if (identif.length() == 0) {
                break;
            }
            className.append(identif);
            Character c = readChar();
            if (c == null || !c.equals('.')) {
                undoRead();
                break;
            }
            className.append(c);
            skipWs = false;
        }
        String result = className.toString();
        if (result.isEmpty()) {
            throw new ParseException("Class name expected");
        }
        if (result.endsWith(".")) {
            throw new ParseException("Invalid class name");
        }
        return result;
    }

    private String readIdentif() {
        return readIdentif(true);
    }

    private String readIdentif(boolean skipWsBefore) {
        if (skipWsBefore) {
            skipWs();
        }
        int start = pos;
        while (isAlphanumeric(peekNextChar())) { readChar(); }
        return content.substring(start, pos);
    }

    private void skipWs() {
        while (!isAtEnd() && isWhitespace(peekNextChar())) {
            readChar();
        }
    }

    private void readExpectWs() throws ParseException {
        if (!isWhitespace(readChar())) {
            throw new ParseException("Whitespace expected.");
        }
        while (isWhitespace(peekNextChar())) { readChar(); }
    }

    private boolean isWhitespace(Character c) {
        return Character.isWhitespace(c);
    }

    private boolean isAlphanumeric(Character c) {
        return Character.isLetter(c) || Character.isDigit(c);
    }

    private void readExpectChar(char expectChar) throws ParseException {
        readExpectChar(expectChar, true);
    }

    private void readExpectChar(char expectChar, boolean skipWsBefore) throws ParseException {
        if (skipWsBefore) {
            skipWs();
        }
        if (isAtEnd() || !readChar().equals(expectChar)) {
            throw new ParseException("Character \"" + expectChar + "\" expected.");
        }
    }

    private boolean isAtEnd() {
        return pos >= end;
    }

    private Character readChar() {
        Character result = null;
        if (!isAtEnd()) {
            result = content.charAt(pos++);
            if (result.equals('\n')) {
                line ++;
            }
        }
        return result;
    }

    private boolean nextCharMatch(char c) {
        return !isAtEnd() && content.charAt(pos) == c;
    }

    private Character peekNextChar() {
        return !isAtEnd() ? content.charAt(pos) : null;
    }

    private void undoRead() {
        if (pos > 0) {
            pos--;
        }
    }

    public static String trimEmptyLines(String text) {
        return text.replaceAll("(?m)^\\s+", "")
            .replaceAll("(?m)\\s+$", "");
    }
}
