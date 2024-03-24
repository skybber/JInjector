package org.hotswap.patcher.parser;

import org.hotswap.patcher.javassist.compiler.CompileError;
import org.hotswap.patcher.javassist.compiler.Lex;

public class PatcherSyntaxError extends CompileError {
    private final int lineNumber;

    public PatcherSyntaxError(Lex lex) {
        super("Syntax error near \"" + lex.getTextAround() + "\"", lex);
        this.lineNumber = lex.getLineNumber();
    }

    public PatcherSyntaxError(String s, Lex lex) {
        super(s, lex);
        this.lineNumber = lex.getLineNumber();
    }

    public PatcherSyntaxError(String s, Lex lex, int lineNumber) {
        super(s, lex);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
