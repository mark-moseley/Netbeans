/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* Generated By:JJTree&JavaCC: Do not edit this line. JavaParserConstants.java */
package org.netbeans.modules.debugger.jpda.expr;

interface JavaParserConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 13;
  int FORMAL_COMMENT = 14;
  int MULTI_LINE_COMMENT = 15;
  int CATEGORY_COMMENT = 16;
  int ABSTRACT = 18;
  int ASSERT = 19;
  int BOOLEAN = 20;
  int BREAK = 21;
  int BYTE = 22;
  int CASE = 23;
  int CATCH = 24;
  int CHAR = 25;
  int CLASS = 26;
  int CONST = 27;
  int CONTINUE = 28;
  int _DEFAULT = 29;
  int DO = 30;
  int DOUBLE = 31;
  int ELSE = 32;
  int ENUM = 33;
  int EXTENDS = 34;
  int FALSE = 35;
  int FINAL = 36;
  int FINALLY = 37;
  int FLOAT = 38;
  int FOR = 39;
  int GOTO = 40;
  int IF = 41;
  int IMPLEMENTS = 42;
  int IMPORT = 43;
  int INSTANCEOF = 44;
  int INT = 45;
  int INTERFACE = 46;
  int LONG = 47;
  int NATIVE = 48;
  int NEW = 49;
  int NULL = 50;
  int PACKAGE = 51;
  int PRIVATE = 52;
  int PROTECTED = 53;
  int PUBLIC = 54;
  int RETURN = 55;
  int SHORT = 56;
  int STATIC = 57;
  int STRICTFP = 58;
  int SUPER = 59;
  int SWITCH = 60;
  int SYNCHRONIZED = 61;
  int THIS = 62;
  int THROW = 63;
  int THROWS = 64;
  int TRANSIENT = 65;
  int TRUE = 66;
  int TRY = 67;
  int VOID = 68;
  int VOLATILE = 69;
  int WHILE = 70;
  int INTEGER_LITERAL = 71;
  int DECIMAL_LITERAL = 72;
  int HEX_LITERAL = 73;
  int OCTAL_LITERAL = 74;
  int FLOATING_POINT_LITERAL = 75;
  int EXPONENT = 76;
  int CHARACTER_LITERAL = 77;
  int STRING_LITERAL = 78;
  int IDENTIFIER = 79;
  int LETTER = 80;
  int DIGIT = 81;
  int LPAREN = 82;
  int RPAREN = 83;
  int LBRACE = 84;
  int RBRACE = 85;
  int LBRACKET = 86;
  int RBRACKET = 87;
  int SEMICOLON = 88;
  int COMMA = 89;
  int DOT = 90;
  int VARARG = 91;
  int ASSIGN = 92;
  int GT = 93;
  int LT = 94;
  int BANG = 95;
  int TILDE = 96;
  int HOOK = 97;
  int COLON = 98;
  int EQ = 99;
  int LE = 100;
  int GE = 101;
  int NE = 102;
  int SC_OR = 103;
  int SC_AND = 104;
  int INCR = 105;
  int DECR = 106;
  int PLUS = 107;
  int MINUS = 108;
  int STAR = 109;
  int SLASH = 110;
  int BIT_AND = 111;
  int BIT_OR = 112;
  int XOR = 113;
  int REM = 114;
  int LSHIFT = 115;
  int RSIGNEDSHIFT = 116;
  int RUNSIGNEDSHIFT = 117;
  int PLUSASSIGN = 118;
  int MINUSASSIGN = 119;
  int STARASSIGN = 120;
  int SLASHASSIGN = 121;
  int ANDASSIGN = 122;
  int ORASSIGN = 123;
  int XORASSIGN = 124;
  int REMASSIGN = 125;
  int LSHIFTASSIGN = 126;
  int RSIGNEDSHIFTASSIGN = 127;
  int RUNSIGNEDSHIFTASSIGN = 128;
  int ATTRIBUTE = 129;

  int DEFAULT = 0;
  int IN_ONLY_EOF = 1;
  int IN_SINGLE_LINE_COMMENT = 2;
  int IN_FORMAL_COMMENT = 3;
  int IN_MULTI_LINE_COMMENT = 4;
  int IN_CATEGORY_COMMENT = 5;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\f\"",
    "\"\\u001a\"",
    "<token of kind 5>",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\r\\n\"",
    "\"//\"",
    "<token of kind 10>",
    "\"/*\"",
    "\"/*<\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 17>",
    "\"abstract\"",
    "\"assert\"",
    "\"boolean\"",
    "\"break\"",
    "\"byte\"",
    "\"case\"",
    "\"catch\"",
    "\"char\"",
    "\"class\"",
    "\"const\"",
    "\"continue\"",
    "\"default\"",
    "\"do\"",
    "\"double\"",
    "\"else\"",
    "\"enum\"",
    "\"extends\"",
    "\"false\"",
    "\"final\"",
    "\"finally\"",
    "\"float\"",
    "\"for\"",
    "\"goto\"",
    "\"if\"",
    "\"implements\"",
    "\"import\"",
    "\"instanceof\"",
    "\"int\"",
    "\"interface\"",
    "\"long\"",
    "\"native\"",
    "\"new\"",
    "\"null\"",
    "\"package\"",
    "\"private\"",
    "\"protected\"",
    "\"public\"",
    "\"return\"",
    "\"short\"",
    "\"static\"",
    "\"strictfp\"",
    "\"super\"",
    "\"switch\"",
    "\"synchronized\"",
    "\"this\"",
    "\"throw\"",
    "\"throws\"",
    "\"transient\"",
    "\"true\"",
    "\"try\"",
    "\"void\"",
    "\"volatile\"",
    "\"while\"",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"...\"",
    "\"=\"",
    "\">\"",
    "\"<\"",
    "\"!\"",
    "\"~\"",
    "\"?\"",
    "\":\"",
    "\"==\"",
    "\"<=\"",
    "\">=\"",
    "\"!=\"",
    "\"||\"",
    "\"&&\"",
    "\"++\"",
    "\"--\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"&\"",
    "\"|\"",
    "\"^\"",
    "\"%\"",
    "\"<<\"",
    "\">>\"",
    "\">>>\"",
    "\"+=\"",
    "\"-=\"",
    "\"*=\"",
    "\"/=\"",
    "\"&=\"",
    "\"|=\"",
    "\"^=\"",
    "\"%=\"",
    "\"<<=\"",
    "\">>=\"",
    "\">>>=\"",
    "\"@\"",
  };

}
