/* Generated By:JJTree&JavaCC: Do not edit this line. CSSParserConstants.java */
package org.netbeans.modules.css.parser;


/** 
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CSSParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int S = 1;
  /** RegularExpression Id. */
  int MSE = 4;
  /** RegularExpression Id. */
  int LBRACE = 8;
  /** RegularExpression Id. */
  int RBRACE = 9;
  /** RegularExpression Id. */
  int COMMA = 10;
  /** RegularExpression Id. */
  int DOT = 11;
  /** RegularExpression Id. */
  int SEMICOLON = 12;
  /** RegularExpression Id. */
  int COLON = 13;
  /** RegularExpression Id. */
  int ASTERISK = 14;
  /** RegularExpression Id. */
  int SLASH = 15;
  /** RegularExpression Id. */
  int PLUS = 16;
  /** RegularExpression Id. */
  int MINUS = 17;
  /** RegularExpression Id. */
  int EQUALS = 18;
  /** RegularExpression Id. */
  int GT = 19;
  /** RegularExpression Id. */
  int LSQUARE = 20;
  /** RegularExpression Id. */
  int RSQUARE = 21;
  /** RegularExpression Id. */
  int HASH = 22;
  /** RegularExpression Id. */
  int STRING = 23;
  /** RegularExpression Id. */
  int RROUND = 24;
  /** RegularExpression Id. */
  int URL = 25;
  /** RegularExpression Id. */
  int URI = 26;
  /** RegularExpression Id. */
  int CDO = 27;
  /** RegularExpression Id. */
  int CDC = 28;
  /** RegularExpression Id. */
  int INCLUDES = 29;
  /** RegularExpression Id. */
  int DASHMATCH = 30;
  /** RegularExpression Id. */
  int IMPORT_SYM = 31;
  /** RegularExpression Id. */
  int PAGE_SYM = 32;
  /** RegularExpression Id. */
  int MEDIA_SYM = 33;
  /** RegularExpression Id. */
  int FONT_FACE_SYM = 34;
  /** RegularExpression Id. */
  int CHARSET_SYM = 35;
  /** RegularExpression Id. */
  int ATKEYWORD = 36;
  /** RegularExpression Id. */
  int IMPORTANT_SYM = 37;
  /** RegularExpression Id. */
  int INHERIT = 38;
  /** RegularExpression Id. */
  int EMS = 39;
  /** RegularExpression Id. */
  int EXS = 40;
  /** RegularExpression Id. */
  int LENGTH_PX = 41;
  /** RegularExpression Id. */
  int LENGTH_CM = 42;
  /** RegularExpression Id. */
  int LENGTH_MM = 43;
  /** RegularExpression Id. */
  int LENGTH_IN = 44;
  /** RegularExpression Id. */
  int LENGTH_PT = 45;
  /** RegularExpression Id. */
  int LENGTH_PC = 46;
  /** RegularExpression Id. */
  int ANGLE_DEG = 47;
  /** RegularExpression Id. */
  int ANGLE_RAD = 48;
  /** RegularExpression Id. */
  int ANGLE_GRAD = 49;
  /** RegularExpression Id. */
  int TIME_MS = 50;
  /** RegularExpression Id. */
  int TIME_S = 51;
  /** RegularExpression Id. */
  int FREQ_HZ = 52;
  /** RegularExpression Id. */
  int FREQ_KHZ = 53;
  /** RegularExpression Id. */
  int DIMEN = 54;
  /** RegularExpression Id. */
  int PERCENTAGE = 55;
  /** RegularExpression Id. */
  int NUMBER = 56;
  /** RegularExpression Id. */
  int RGB = 57;
  /** RegularExpression Id. */
  int FUNCTION = 58;
  /** RegularExpression Id. */
  int IDENT = 59;
  /** RegularExpression Id. */
  int NAME = 60;
  /** RegularExpression Id. */
  int NUM = 61;
  /** RegularExpression Id. */
  int UNICODERANGE = 62;
  /** RegularExpression Id. */
  int RANGE = 63;
  /** RegularExpression Id. */
  int Q16 = 64;
  /** RegularExpression Id. */
  int Q15 = 65;
  /** RegularExpression Id. */
  int Q14 = 66;
  /** RegularExpression Id. */
  int Q13 = 67;
  /** RegularExpression Id. */
  int Q12 = 68;
  /** RegularExpression Id. */
  int Q11 = 69;
  /** RegularExpression Id. */
  int NMSTART = 70;
  /** RegularExpression Id. */
  int NMCHAR = 71;
  /** RegularExpression Id. */
  int STRING1 = 72;
  /** RegularExpression Id. */
  int STRING2 = 73;
  /** RegularExpression Id. */
  int NONASCII = 74;
  /** RegularExpression Id. */
  int ESCAPE = 75;
  /** RegularExpression Id. */
  int NL = 76;
  /** RegularExpression Id. */
  int UNICODE = 77;
  /** RegularExpression Id. */
  int HNUM = 78;
  /** RegularExpression Id. */
  int H = 79;
  /** RegularExpression Id. */
  int UNKNOWN = 80;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int MS_EXPRESSION = 1;
  /** Lexical state. */
  int COMMENT = 2;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "<S>",
    "\"/*\"",
    "\"expression(\"",
    "\")\"",
    "<token of kind 5>",
    "\"*/\"",
    "<token of kind 7>",
    "\"{\"",
    "\"}\"",
    "\",\"",
    "\".\"",
    "\";\"",
    "\":\"",
    "\"*\"",
    "\"/\"",
    "\"+\"",
    "\"-\"",
    "\"=\"",
    "\">\"",
    "\"[\"",
    "\"]\"",
    "<HASH>",
    "<STRING>",
    "\")\"",
    "<URL>",
    "<URI>",
    "\"<!--\"",
    "\"-->\"",
    "\"~=\"",
    "\"|=\"",
    "\"@import\"",
    "\"@page\"",
    "\"@media\"",
    "\"@font-face\"",
    "\"@charset\"",
    "<ATKEYWORD>",
    "<IMPORTANT_SYM>",
    "\"inherit\"",
    "<EMS>",
    "<EXS>",
    "<LENGTH_PX>",
    "<LENGTH_CM>",
    "<LENGTH_MM>",
    "<LENGTH_IN>",
    "<LENGTH_PT>",
    "<LENGTH_PC>",
    "<ANGLE_DEG>",
    "<ANGLE_RAD>",
    "<ANGLE_GRAD>",
    "<TIME_MS>",
    "<TIME_S>",
    "<FREQ_HZ>",
    "<FREQ_KHZ>",
    "<DIMEN>",
    "<PERCENTAGE>",
    "<NUMBER>",
    "\"rgb(\"",
    "<FUNCTION>",
    "<IDENT>",
    "<NAME>",
    "<NUM>",
    "<UNICODERANGE>",
    "<RANGE>",
    "<Q16>",
    "<Q15>",
    "<Q14>",
    "<Q13>",
    "<Q12>",
    "\"?\"",
    "<NMSTART>",
    "<NMCHAR>",
    "<STRING1>",
    "<STRING2>",
    "<NONASCII>",
    "<ESCAPE>",
    "<NL>",
    "<UNICODE>",
    "<HNUM>",
    "<H>",
    "<UNKNOWN>",
  };

}
