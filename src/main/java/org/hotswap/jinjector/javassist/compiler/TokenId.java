/*
 * Javassist, a Java-bytecode translator toolkit.
 * Copyright (C) 1999- Shigeru Chiba. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License.  Alternatively, the contents of this file may be used under
 * the terms of the GNU Lesser General Public License Version 2.1 or later,
 * or the Apache License Version 2.0.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

package org.hotswap.jinjector.javassist.compiler;

public interface TokenId {
    int ABSTRACT = 300;
    int BOOLEAN = 301;
    int BREAK = 302;
    int BYTE = 303;
    int CASE = 304;
    int CATCH = 305;
    int CHAR = 306;
    int CLASS = 307;
    int CONST = 308;    // reserved keyword
    int CONTINUE = 309;
    int DEFAULT = 310;
    int DO = 311;
    int DOUBLE = 312;
    int ELSE = 313;
    int EXTENDS = 314;
    int FINAL = 315;
    int FINALLY = 316;
    int FLOAT = 317;
    int FOR = 318;
    int GOTO = 319;     // reserved keyword
    int IF = 320;
    int IMPLEMENTS = 321;
    int IMPORT = 322;
    int INSTANCEOF = 323;
    int INT = 324;
    int INTERFACE = 325;
    int LONG = 326;
    int NATIVE = 327;
    int NEW = 328;
    int PACKAGE = 329;
    int PRIVATE = 330;
    int PROTECTED = 331;
    int PUBLIC = 332;
    int RETURN = 333;
    int SHORT = 334;
    int STATIC = 335;
    int SUPER = 336;
    int SWITCH = 337;
    int SYNCHRONIZED = 338;
    int THIS = 339;
    int THROW = 340;
    int THROWS = 341;
    int TRANSIENT = 342;
    int TRY = 343;
    int VOID = 344;
    int VOLATILE = 345;
    int WHILE = 346;
    int STRICT = 347;

    int NEQ = 350;      // !=
    int MOD_E = 351;    // %=
    int AND_E = 352;    // &=
    int MUL_E = 353;    // *=
    int PLUS_E = 354;   // +=
    int MINUS_E = 355;  // -=
    int DIV_E = 356;    // /=
    int LE = 357;               // <=
    int EQ = 358;               // ==
    int GE = 359;               // >=
    int EXOR_E = 360;   // ^=
    int OR_E = 361;     // |=
    int PLUSPLUS = 362; // ++
    int MINUSMINUS = 363;       // --
    int LSHIFT = 364;   // <<
    int LSHIFT_E = 365; // <<=
    int RSHIFT = 366;   // >>
    int RSHIFT_E = 367; // >>=
    int OROR = 368;     // ||
    int ANDAND = 369;   // &&
    int ARSHIFT = 370;  // >>>
    int ARSHIFT_E = 371;        // >>>=

    // operators from NEQ to ARSHIFT_E
    String opNames[] = { "!=", "%=", "&=", "*=", "+=", "-=", "/=",
                       "<=", "==", ">=", "^=", "|=", "++", "--",
                       "<<", "<<=", ">>", ">>=", "||", "&&", ">>>",
                       ">>>=" };

    // operators from MOD_E to ARSHIFT_E
    int assignOps[] = { '%', '&', '*', '+', '-', '/', 0, 0, 0,
                        '^', '|', 0, 0, 0, LSHIFT, 0, RSHIFT, 0, 0, 0,
                        ARSHIFT };

    int Identifier = 400;
    int CharConstant = 401;
    int IntConstant = 402;
    int LongConstant = 403;
    int FloatConstant = 404;
    int DoubleConstant = 405;
    int StringL = 406;

    int TRUE = 410;
    int FALSE = 411;
    int NULL = 412;

    int CALL = 'C';     // method call
    int ARRAY = 'A';    // array access
    int MEMBER = '#';   // static member access

    int EXPR = 'E';     // expression statement
    int LABEL = 'L';    // label statement
    int BLOCK = 'B';    // block statement
    int DECL = 'D';     // declaration statement

    int BadToken = 500;

    static String getTokenAsString(Lex lex, int token) {
        if (token < 0) {
            return "<END>";
        }
        switch(token) {
            case ABSTRACT: return "abstract";
            case BOOLEAN: return "boolean";
            case BREAK: return "break";
            case BYTE: return "byte";
            case CASE: return "case";
            case CATCH: return "catch";
            case CHAR: return "char";
            case CLASS: return "class";
            case CONST: return "const";
            case CONTINUE: return "continue";
            case DEFAULT: return "default";
            case DO: return "do";
            case DOUBLE: return "double";
            case ELSE: return "else";
            case EXTENDS: return "extends";
            case FINAL: return "final";
            case FINALLY: return "finally";
            case FLOAT: return "float";
            case FOR: return "for";
            case GOTO: return "goto";
            case IF: return "if";
            case IMPLEMENTS: return "implements";
            case IMPORT: return "import";
            case INSTANCEOF: return "instanceof";
            case INT: return "int";
            case INTERFACE: return "interface";
            case LONG: return "long";
            case NATIVE: return "native";
            case NEW: return "new";
            case PACKAGE: return "package";
            case PRIVATE: return "private";
            case PROTECTED: return "protected";
            case PUBLIC: return "public";
            case RETURN: return "return";
            case SHORT: return "short";
            case STATIC: return "static";
            case SUPER: return "super";
            case SWITCH: return "switch";
            case SYNCHRONIZED: return "synchronized";
            case THIS: return "this";
            case THROW: return "throw";
            case THROWS: return "throws";
            case TRANSIENT: return "transient";
            case TRY: return "try";
            case VOID: return "void";
            case VOLATILE: return "volatile";
            case WHILE: return "while";
            case STRICT: return "strict";
            case NEQ: return "!=";
            case MOD_E: return "%=";
            case AND_E: return "&=";
            case MUL_E: return "*=";
            case PLUS_E: return "+=";
            case MINUS_E: return "-=";
            case DIV_E: return "/=";
            case LE: return "<=";
            case EQ: return "==";
            case GE: return ">=";
            case EXOR_E: return "^=";
            case OR_E: return "|=";
            case PLUSPLUS: return "++";
            case MINUSMINUS: return "--";
            case LSHIFT: return "<<";
            case LSHIFT_E: return "<<=";
            case RSHIFT: return ">>";
            case RSHIFT_E: return ">>=";
            case OROR: return "||";
            case ANDAND: return "&&";
            case ARSHIFT: return ">>>";
            case ARSHIFT_E: return ">>>=";
            case Identifier: return lex.getString();
            case CharConstant: return String.valueOf((char )lex.getLong());
            case IntConstant: return String.valueOf(lex.getLong());
            case LongConstant: return String.valueOf(lex.getLong());
            case FloatConstant: return String.valueOf(lex.getDouble());
            case DoubleConstant: return String.valueOf(lex.getDouble());
            case StringL: return "\"" + lex.getString() + "\"";
            case TRUE: return "true";
            case FALSE: return "false";
            case NULL: return "null";
            case CALL: return "method call";
            case ARRAY: return "array";
            case MEMBER: return "#";
            case EXPR: return "expression";
            case LABEL: return "label";
            case BLOCK: return "block";
            case DECL: return "decl";
            case BadToken: return "Bad token";
            default:
                return String.valueOf((char) token);
        }
    }

}
