package org.hotswap.jinjector.parser;

import org.hotswap.jinjector.javassist.compiler.CompileError;
import org.hotswap.jinjector.javassist.compiler.Lex;

public class JInjectorSyntaxError extends CompileError {
    private final int lineNumber;

    public JInjectorSyntaxError(Lex lex) {
        super("Syntax error near \"" + lex.getTextAround() + "\"", lex);
        this.lineNumber = lex.getLineNumber();
    }

    public JInjectorSyntaxError(String s, Lex lex) {
        super(s, lex);
        this.lineNumber = lex.getLineNumber();
    }

    public JInjectorSyntaxError(String s, Lex lex, int lineNumber) {
        super(s, lex);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
