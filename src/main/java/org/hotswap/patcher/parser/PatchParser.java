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

/*
class org.hotswap.patcher.parser.Patcher {
parse(*) {
  insertBefore {
  }
  insertAfter {
  }
}

@OnResourceFileEvent(path="/", filter="*.properties") {
  call("java.util.ResourceBundle.clearCache()");
}
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

    private final String file;
    private String content;
    private int line;
    private int pos;
    private int end;
    private Character cha;

    public PatchParser(String file) {
        this.file = file;
    }

    public List<ClassPatch> parse() {
        try {
            Path filePath = Paths.get(file);
            if (Files.exists(filePath) && !Files.isRegularFile(filePath)) {
                content = Files.readString(filePath);
                return parsePatch();
            } else {
                LOGGER.error("Patch file {} does not exists!", file);
            }
        } catch (IOException e) {
            LOGGER.error("IO exception. {}", e);
        }
        return List.of();
    }

    private List<ClassPatch> parsePatch() {
        List<ClassPatch> result = new ArrayList<>();
        pos = 0;
        end = content.length();
        line = 1;
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
            }
            return result;
        } catch (ParseException e) {
        }
        return null;
    }

    private void parseClassBody(ClassPatch classPatch) throws ParseException {
        while (true) {
            String methodName = readIdentif(true);
            if (methodName.isEmpty()) {
                return;
            }
            readExpectChar('(');
            skipWs();
            boolean allMethods = false;
            List<MethodPatch.MethodParam> params = null;
            if (nextCharMatch('*')) {
                nextChar();
                readExpectChar(')');
                allMethods = true;
            } else {
                if (!nextCharMatch(')')) {
                    params = new ArrayList<>();
                    while (true) {
                        String paramClassName = readClassName();
                        skipWs();
                        boolean isArray = false;
                        if (nextCharMatch('[')) {
                            nextChar();
                            readExpectChar(']');
                            isArray = true;
                        }
                        params.add(new MethodPatch.MethodParam(paramClassName, isArray));
                        if (nextCharMatch(')')) {
                            nextChar();
                            break;
                        }
                        readExpectChar(',');
                    }
                } else {
                   nextChar();
                }
            }
            readExpectChar('{');
            MethodPatch methodPatch = new MethodPatch(methodName, allMethods, params);
            parseMethodBody(methodPatch);
            classPatch.addMethodPatch(methodPatch);
        }
    }

    private void parseMethodBody(MethodPatch methodPatch) throws ParseException {
        while (true) {
            String strTransformType = readIdentif(true);
            if (strTransformType.isEmpty()) {
                return;
            }
            MethodPatchFragment.TransformType transformType = methodTransformTypes.get(strTransformType);
            if (transformType == null) {
                throw new ParseException("Invalid method transformer \"" + strTransformType + "\".");
            }
            readExpectChar('{');
            String patchFragmentBody = readPatchFragmentBody();
            MethodPatchFragment patchFragment = new MethodPatchFragment(transformType, patchFragmentBody);
            methodPatch.addPatchFragment(patchFragment);
            readExpectChar('}');
        }
    }

    private String readPatchFragmentBody() {
        return "";
    }

    private void readExpectKeyword(String keyword) throws ParseException {
        String ident = readIdentif(true);
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

    private String readIdentif(boolean skipWsBefore) {
        if (skipWsBefore) {
            skipWs();
        }
        int start = pos;
        while (readChar() != null && isAlphanumeric()) {}
        return content.substring(start, pos);
    }

    private void skipWs() {
        while (!isEnd() && isWhitespace(peekChar())) {
            readChar();
        }
    }

    private void readExpectWs() throws ParseException {
        if (isEnd() || !isWhitespace()) {
            throw new ParseException("Whitespace expected.");
        }
        while (readChar() != null && isWhitespace()) {}
    }

    private boolean isWhitespace() {
        return isWhitespace(cha);
    }

    private boolean isWhitespace(Character c) {
        return Character.isWhitespace(c);
    }

    private boolean isAlphanumeric() {
        return Character.isLetter(cha) || Character.isDigit(cha);
    }

    private boolean isEnd() {
        return pos >= end;
    }

    private void readExpectChar(char expectChar) throws ParseException {
        readExpectChar(expectChar, true);
    }

    private void readExpectChar(char expectChar, boolean skipWsBefore) throws ParseException {
        if (skipWsBefore) {
            skipWs();
        }
        if (isEnd() || !readChar().equals(cha)) {
            throw new ParseException("Character \"" + expectChar + "\" expected.");
        }
    }

    private void nextChar() {
        readChar();
    }

    private Character readChar() {
        if (!isEnd()) {
           cha = content.charAt(pos++);
           if (cha.equals('\n')) {
               line ++;
           }
        } else {
            cha = null;
        }
        return cha;
    }

    private boolean nextCharMatch(char c) {
        return !isEnd() && content.charAt(pos) == c;
    }

    private Character peekChar() {
        if (!isEnd()) {
            return content.charAt(pos);
        }
        return null;
    }
}
