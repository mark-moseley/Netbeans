// $ANTLR 2.7.2: "java15.g" -> "JavaLexer.java"$

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class JavaLexer extends antlr.CharScanner implements JavaTokenTypes, TokenStream
 {

   public void setEventController(ParserEventController newVal)
   {
      mController = newVal;
   }

   /** 
    * Parser error-reporting function can be overridden in subclass.
    * @param ex The exception that occured.
    */
   public void reportError(RecognitionException ex) 
   {
      mController.errorFound(ex.getMessage(), 
                             ex.getLine(), 
                             ex.getColumn(), 
                             ex.getFilename()); 
   }

   private ParserEventController mController;

   // The following was provided by the Antlr website.
   /** flag for enabling the "assert" keyword */
   private boolean assertEnabled = true;
   /** flag for enabling the "enum" keyword */
   private boolean enumEnabled = true;

   /** Enable the "assert" keyword */
   public void enableAssert(boolean shouldEnable) { assertEnabled = shouldEnable; }
   /** Query the "assert" keyword state */
   public boolean isAssertEnabled() { return assertEnabled; }
   /** Enable the "enum" keyword */
   public void enableEnum(boolean shouldEnable) { enumEnabled = shouldEnable; }
   /** Query the "enum" keyword state */
   public boolean isEnumEnabled() { return enumEnabled; }
public JavaLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public JavaLexer(Reader in) {
	this(new CharBuffer(in));
}
public JavaLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public JavaLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("byte", this), new Integer(81));
	literals.put(new ANTLRHashString("public", this), new Integer(92));
	literals.put(new ANTLRHashString("case", this), new Integer(126));
	literals.put(new ANTLRHashString("short", this), new Integer(83));
	literals.put(new ANTLRHashString("break", this), new Integer(119));
	literals.put(new ANTLRHashString("while", this), new Integer(117));
	literals.put(new ANTLRHashString("new", this), new Integer(162));
	literals.put(new ANTLRHashString("instanceof", this), new Integer(149));
	literals.put(new ANTLRHashString("implements", this), new Integer(110));
	literals.put(new ANTLRHashString("synchronized", this), new Integer(97));
	literals.put(new ANTLRHashString("float", this), new Integer(85));
	literals.put(new ANTLRHashString("package", this), new Integer(65));
	literals.put(new ANTLRHashString("return", this), new Integer(121));
	literals.put(new ANTLRHashString("throw", this), new Integer(123));
	literals.put(new ANTLRHashString("null", this), new Integer(161));
	literals.put(new ANTLRHashString("threadsafe", this), new Integer(96));
	literals.put(new ANTLRHashString("protected", this), new Integer(93));
	literals.put(new ANTLRHashString("class", this), new Integer(105));
	literals.put(new ANTLRHashString("throws", this), new Integer(112));
	literals.put(new ANTLRHashString("do", this), new Integer(118));
	literals.put(new ANTLRHashString("strictfp", this), new Integer(41));
	literals.put(new ANTLRHashString("super", this), new Integer(73));
	literals.put(new ANTLRHashString("transient", this), new Integer(94));
	literals.put(new ANTLRHashString("native", this), new Integer(95));
	literals.put(new ANTLRHashString("interface", this), new Integer(106));
	literals.put(new ANTLRHashString("final", this), new Integer(39));
	literals.put(new ANTLRHashString("if", this), new Integer(115));
	literals.put(new ANTLRHashString("double", this), new Integer(87));
	literals.put(new ANTLRHashString("volatile", this), new Integer(98));
	literals.put(new ANTLRHashString("assert", this), new Integer(124));
	literals.put(new ANTLRHashString("catch", this), new Integer(129));
	literals.put(new ANTLRHashString("try", this), new Integer(127));
	literals.put(new ANTLRHashString("enum", this), new Integer(107));
	literals.put(new ANTLRHashString("int", this), new Integer(84));
	literals.put(new ANTLRHashString("for", this), new Integer(125));
	literals.put(new ANTLRHashString("extends", this), new Integer(72));
	literals.put(new ANTLRHashString("boolean", this), new Integer(80));
	literals.put(new ANTLRHashString("char", this), new Integer(82));
	literals.put(new ANTLRHashString("private", this), new Integer(91));
	literals.put(new ANTLRHashString("default", this), new Integer(109));
	literals.put(new ANTLRHashString("false", this), new Integer(160));
	literals.put(new ANTLRHashString("this", this), new Integer(111));
	literals.put(new ANTLRHashString("static", this), new Integer(68));
	literals.put(new ANTLRHashString("abstract", this), new Integer(40));
	literals.put(new ANTLRHashString("continue", this), new Integer(120));
	literals.put(new ANTLRHashString("finally", this), new Integer(128));
	literals.put(new ANTLRHashString("else", this), new Integer(116));
	literals.put(new ANTLRHashString("import", this), new Integer(67));
	literals.put(new ANTLRHashString("void", this), new Integer(79));
	literals.put(new ANTLRHashString("switch", this), new Integer(122));
	literals.put(new ANTLRHashString("true", this), new Integer(159));
	literals.put(new ANTLRHashString("long", this), new Integer(86));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '?':
				{
					mQUESTION(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case '{':
				{
					mLCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mRCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case ':':
				{
					mCOLON(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '~':
				{
					mBNOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\u000c':  case '\r':
				case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\'':
				{
					mCHAR_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':
				{
					mSTRING_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
				{
					mNUM_INT(true);
					theRetToken=_returnToken;
					break;
				}
				case '@':
				{
					mAT(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (LA(4)=='=')) {
						mBSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='=')) {
						mSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (true)) {
						mBSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (LA(3)=='=')) {
						mSL_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=')) {
						mEQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (LA(2)=='=')) {
						mNOT_EQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='=')) {
						mDIV_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='=')) {
						mPLUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='+')) {
						mINC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='=')) {
						mMINUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='-')) {
						mDEC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='=')) {
						mSTAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (LA(2)=='=')) {
						mMOD_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (true)) {
						mSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='=')) {
						mGE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (true)) {
						mSL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=')) {
						mLE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='=')) {
						mBXOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='=')) {
						mBOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='|')) {
						mLOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='=')) {
						mBAND_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='&')) {
						mLAND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='/')) {
						mSL_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mML_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (true)) {
						mASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (true)) {
						mLNOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (true)) {
						mDIV(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (true)) {
						mPLUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (true)) {
						mMINUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (true)) {
						mSTAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (true)) {
						mMOD(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mGT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mLT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (true)) {
						mBXOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (true)) {
						mBOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (true)) {
						mBAND(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1)))) {
						mIDENT(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LBRACK;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RBRACK;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASSIGN;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUAL;
		int _saveIndex;
		
		match("==");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LNOT;
		int _saveIndex;
		
		match('!');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BNOT;
		int _saveIndex;
		
		match('~');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQUAL;
		int _saveIndex;
		
		match("!=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV_ASSIGN;
		int _saveIndex;
		
		match("/=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS_ASSIGN;
		int _saveIndex;
		
		match("+=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INC;
		int _saveIndex;
		
		match("++");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS_ASSIGN;
		int _saveIndex;
		
		match("-=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDEC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DEC;
		int _saveIndex;
		
		match("--");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_ASSIGN;
		int _saveIndex;
		
		match("*=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD_ASSIGN;
		int _saveIndex;
		
		match("%=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR;
		int _saveIndex;
		
		match(">>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR_ASSIGN;
		int _saveIndex;
		
		match(">>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR;
		int _saveIndex;
		
		match(">>>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR_ASSIGN;
		int _saveIndex;
		
		match(">>>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GE;
		int _saveIndex;
		
		match(">=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match(">");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL;
		int _saveIndex;
		
		match("<<");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_ASSIGN;
		int _saveIndex;
		
		match("<<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LE;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LT;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR_ASSIGN;
		int _saveIndex;
		
		match("^=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR_ASSIGN;
		int _saveIndex;
		
		match("|=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LOR;
		int _saveIndex;
		
		match("||");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND;
		int _saveIndex;
		
		match('&');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND_ASSIGN;
		int _saveIndex;
		
		match("&=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LAND;
		int _saveIndex;
		
		match("&&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		int _cnt369=0;
		_loop369:
		do {
			switch ( LA(1)) {
			case ' ':
			{
				match(' ');
				break;
			}
			case '\t':
			{
				match('\t');
				break;
			}
			case '\u000c':
			{
				match('\f');
				break;
			}
			case '\n':  case '\r':
			{
				{
				if ((LA(1)=='\r') && (LA(2)=='\n') && (true) && (true)) {
					match("\r\n");
				}
				else if ((LA(1)=='\r') && (true) && (true) && (true)) {
					match('\r');
				}
				else if ((LA(1)=='\n')) {
					match('\n');
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					newline();
				}
				break;
			}
			default:
			{
				if ( _cnt369>=1 ) { break _loop369; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt369++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_COMMENT;
		int _saveIndex;
		
		match("//");
		{
		_loop373:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				{
				match(_tokenSet_1);
				}
			}
			else {
				break _loop373;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case '\n':
		{
			match('\n');
			if ( inputState.guessing==0 ) {
				/* $setType(Token.SKIP); */ newline();
			}
			break;
		}
		case '\r':
		{
			match('\r');
			{
			if ((LA(1)=='\n')) {
				match('\n');
			}
			else {
			}
			
			}
			if ( inputState.guessing==0 ) {
				/* $setType(Token.SKIP); */ newline();
			}
			break;
		}
		default:
			{
			}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mML_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ML_COMMENT;
		int _saveIndex;
		
		match("/*");
		{
		_loop379:
		do {
			if ((LA(1)=='\r') && (LA(2)=='\n') && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && ((LA(4) >= '\u0003' && LA(4) <= '\uffff'))) {
				match('\r');
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if (((LA(1)=='*') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')))&&( LA(2)!='/' )) {
				match('*');
			}
			else if ((LA(1)=='\r') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && (true)) {
				match('\r');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((LA(1)=='\n')) {
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((_tokenSet_2.member(LA(1)))) {
				{
				match(_tokenSet_2);
				}
			}
			else {
				break _loop379;
			}
			
		} while (true);
		}
		match("*/");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCHAR_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CHAR_LITERAL;
		int _saveIndex;
		
		match('\'');
		{
		if ((LA(1)=='\\')) {
			mESC(false);
		}
		else if ((_tokenSet_3.member(LA(1)))) {
			{
			match(_tokenSet_3);
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		match('\'');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		
		match('\\');
		{
		switch ( LA(1)) {
		case 'n':
		{
			match('n');
			break;
		}
		case 'r':
		{
			match('r');
			break;
		}
		case 't':
		{
			match('t');
			break;
		}
		case 'b':
		{
			match('b');
			break;
		}
		case 'f':
		{
			match('f');
			break;
		}
		case '"':
		{
			match('"');
			break;
		}
		case '\'':
		{
			match('\'');
			break;
		}
		case '\\':
		{
			match('\\');
			break;
		}
		case 'u':
		{
			{
			int _cnt390=0;
			_loop390:
			do {
				if ((LA(1)=='u')) {
					match('u');
				}
				else {
					if ( _cnt390>=1 ) { break _loop390; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt390++;
			} while (true);
			}
			mHEX_DIGIT(false);
			mHEX_DIGIT(false);
			mHEX_DIGIT(false);
			mHEX_DIGIT(false);
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		{
			matchRange('0','3');
			{
			if (((LA(1) >= '0' && LA(1) <= '7')) && (_tokenSet_4.member(LA(2))) && (true) && (true)) {
				matchRange('0','7');
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && (_tokenSet_4.member(LA(2))) && (true) && (true)) {
					matchRange('0','7');
				}
				else if ((_tokenSet_4.member(LA(1))) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
			}
			else if ((_tokenSet_4.member(LA(1))) && (true) && (true) && (true)) {
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			break;
		}
		case '4':  case '5':  case '6':  case '7':
		{
			matchRange('4','7');
			{
			if (((LA(1) >= '0' && LA(1) <= '7')) && (_tokenSet_4.member(LA(2))) && (true) && (true)) {
				matchRange('0','7');
			}
			else if ((_tokenSet_4.member(LA(1))) && (true) && (true) && (true)) {
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_LITERAL;
		int _saveIndex;
		
		match('"');
		{
		_loop386:
		do {
			if ((LA(1)=='\\')) {
				mESC(false);
			}
			else if ((_tokenSet_5.member(LA(1)))) {
				{
				match(_tokenSet_5);
				}
			}
			else {
				break _loop386;
			}
			
		} while (true);
		}
		match('"');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_DIGIT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			matchRange('A','F');
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			matchRange('a','f');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mVOCAB(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VOCAB;
		int _saveIndex;
		
		matchRange('\3','\377');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mIDENT_LETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENT_LETTER;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('\u0041','\u005a');
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('\u0061','\u007a');
			break;
		}
		case '\u00c0':  case '\u00c1':  case '\u00c2':  case '\u00c3':
		case '\u00c4':  case '\u00c5':  case '\u00c6':  case '\u00c7':
		case '\u00c8':  case '\u00c9':  case '\u00ca':  case '\u00cb':
		case '\u00cc':  case '\u00cd':  case '\u00ce':  case '\u00cf':
		case '\u00d0':  case '\u00d1':  case '\u00d2':  case '\u00d3':
		case '\u00d4':  case '\u00d5':  case '\u00d6':
		{
			matchRange('\u00c0','\u00d6');
			break;
		}
		case '\u00d8':  case '\u00d9':  case '\u00da':  case '\u00db':
		case '\u00dc':  case '\u00dd':  case '\u00de':  case '\u00df':
		case '\u00e0':  case '\u00e1':  case '\u00e2':  case '\u00e3':
		case '\u00e4':  case '\u00e5':  case '\u00e6':  case '\u00e7':
		case '\u00e8':  case '\u00e9':  case '\u00ea':  case '\u00eb':
		case '\u00ec':  case '\u00ed':  case '\u00ee':  case '\u00ef':
		case '\u00f0':  case '\u00f1':  case '\u00f2':  case '\u00f3':
		case '\u00f4':  case '\u00f5':  case '\u00f6':
		{
			matchRange('\u00d8','\u00f6');
			break;
		}
		case '\u00f8':  case '\u00f9':  case '\u00fa':  case '\u00fb':
		case '\u00fc':  case '\u00fd':  case '\u00fe':  case '\u00ff':
		{
			matchRange('\u00f8','\u00ff');
			break;
		}
		default:
			if (((LA(1) >= '\u0100' && LA(1) <= '\u1fff'))) {
				matchRange('\u0100','\u1fff');
			}
			else if (((LA(1) >= '\u3040' && LA(1) <= '\u318f'))) {
				matchRange('\u3040','\u318f');
			}
			else if (((LA(1) >= '\u3300' && LA(1) <= '\u337f'))) {
				matchRange('\u3300','\u337f');
			}
			else if (((LA(1) >= '\u3400' && LA(1) <= '\u3d2d'))) {
				matchRange('\u3400','\u3d2d');
			}
			else if (((LA(1) >= '\u4e00' && LA(1) <= '\u9fff'))) {
				matchRange('\u4e00','\u9fff');
			}
			else if (((LA(1) >= '\uf900' && LA(1) <= '\ufaff'))) {
				matchRange('\uf900','\ufaff');
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '_':
		{
			match('_');
			break;
		}
		case '$':
		{
			match('$');
			break;
		}
		default:
			if ((_tokenSet_6.member(LA(1)))) {
				mIDENT_LETTER(false);
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop401:
		do {
			switch ( LA(1)) {
			case '_':
			{
				match('_');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			default:
				if ((_tokenSet_6.member(LA(1)))) {
					mIDENT_LETTER(false);
				}
			else {
				break _loop401;
			}
			}
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
						// check if "assert" keyword is enabled
						if (assertEnabled && "assert".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
							_ttype = LITERAL_assert; // set token type for the rule in the parser
						}
						// check if "enum" keyword is enabled
						if (enumEnabled && "enum".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
							_ttype = LITERAL_enum; // set token type for the rule in the parser
						}
					
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNUM_INT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUM_INT;
		int _saveIndex;
		Token f1=null;
		Token f2=null;
		Token f3=null;
		Token f4=null;
		Token fh1=null;
		Token fh2=null;
		boolean isDecimal=false; Token t=null;
		
		if ((LA(1)=='0') && (LA(2)=='X'||LA(2)=='x')) {
			{
			match('0');
			{
			switch ( LA(1)) {
			case 'x':
			{
				match('x');
				break;
			}
			case 'X':
			{
				match('X');
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':
			{
				{
				{
				int _cnt437=0;
				_loop437:
				do {
					if ((_tokenSet_7.member(LA(1))) && (true) && (true) && (true)) {
						mHEX_DIGIT(false);
					}
					else {
						if ( _cnt437>=1 ) { break _loop437; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt437++;
				} while (true);
				}
				{
				if ((_tokenSet_8.member(LA(1)))) {
					{
					{
					switch ( LA(1)) {
					case '.':
					{
						match('.');
						break;
					}
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case 'A':  case 'B':
					case 'C':  case 'D':  case 'E':  case 'F':
					case 'P':  case 'a':  case 'b':  case 'c':
					case 'd':  case 'e':  case 'f':  case 'p':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					{
					_loop444:
					do {
						if ((_tokenSet_7.member(LA(1)))) {
							mHEX_DIGIT(false);
						}
						else {
							break _loop444;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case 'p':
					{
						match('p');
						break;
					}
					case 'P':
					{
						match('P');
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					{
					switch ( LA(1)) {
					case '+':
					{
						match('+');
						break;
					}
					case '-':
					{
						match('-');
						break;
					}
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					{
					int _cnt448=0;
					_loop448:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							if ( _cnt448>=1 ) { break _loop448; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt448++;
					} while (true);
					}
					{
					if ((LA(1)=='D'||LA(1)=='F'||LA(1)=='d'||LA(1)=='f')) {
						mFLOAT_SUFFIX(true);
						fh1=_returnToken;
						if ( inputState.guessing==0 ) {
							t=fh1;
						}
					}
					else {
					}
					
					}
					}
				}
				else {
					{
					if ((LA(1)=='L'||LA(1)=='l')) {
						{
						switch ( LA(1)) {
						case 'l':
						{
							match('l');
							break;
						}
						case 'L':
						{
							match('L');
							break;
						}
						default:
						{
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						}
						}
						if ( inputState.guessing==0 ) {
							_ttype = NUM_LONG;
						}
					}
					else {
					}
					
					}
				}
				
				}
				}
				break;
			}
			case '.':
			{
				{
				match('.');
				{
				int _cnt452=0;
				_loop452:
				do {
					if ((_tokenSet_7.member(LA(1)))) {
						mHEX_DIGIT(false);
					}
					else {
						if ( _cnt452>=1 ) { break _loop452; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt452++;
				} while (true);
				}
				{
				switch ( LA(1)) {
				case 'p':
				{
					match('p');
					break;
				}
				case 'P':
				{
					match('P');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				switch ( LA(1)) {
				case '+':
				{
					match('+');
					break;
				}
				case '-':
				{
					match('-');
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt456=0;
				_loop456:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt456>=1 ) { break _loop456; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt456++;
				} while (true);
				}
				{
				if ((LA(1)=='D'||LA(1)=='F'||LA(1)=='d'||LA(1)=='f')) {
					mFLOAT_SUFFIX(true);
					fh2=_returnToken;
					if ( inputState.guessing==0 ) {
						t=fh2;
					}
				}
				else {
				}
				
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					    if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
				_ttype = NUM_FLOAT;
				}
					    else {
							_ttype = NUM_DOUBLE; // assume double
						}
						
			}
		}
		else if ((LA(1)=='.')) {
			match('.');
			if ( inputState.guessing==0 ) {
				_ttype = DOT;
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				{
				{
				int _cnt406=0;
				_loop406:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt406>=1 ) { break _loop406; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt406++;
				} while (true);
				}
				{
				if ((LA(1)=='E'||LA(1)=='e')) {
					mEXPONENT(false);
				}
				else {
				}
				
				}
				{
				if ((LA(1)=='D'||LA(1)=='F'||LA(1)=='d'||LA(1)=='f')) {
					mFLOAT_SUFFIX(true);
					f1=_returnToken;
					if ( inputState.guessing==0 ) {
						t=f1;
					}
				}
				else {
				}
				
				}
				if ( inputState.guessing==0 ) {
					
									if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
										_ttype = NUM_FLOAT;
									}
									else {
										_ttype = NUM_DOUBLE; // assume double
									}
									
				}
				}
				break;
			}
			case '.':
			{
				{
				match("..");
				if ( inputState.guessing==0 ) {
					_ttype = TRIPLE_DOT;
				}
				}
				break;
			}
			default:
				{
				}
			}
			}
		}
		else if (((LA(1) >= '0' && LA(1) <= '9')) && (true)) {
			{
			switch ( LA(1)) {
			case '0':
			{
				match('0');
				if ( inputState.guessing==0 ) {
					isDecimal = true;
				}
				{
				boolean synPredMatched416 = false;
				if ((((LA(1) >= '0' && LA(1) <= '9')) && (true) && (true) && (true))) {
					int _m416 = mark();
					synPredMatched416 = true;
					inputState.guessing++;
					try {
						{
						{
						int _cnt414=0;
						_loop414:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9'))) {
								matchRange('0','9');
							}
							else {
								if ( _cnt414>=1 ) { break _loop414; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
							}
							
							_cnt414++;
						} while (true);
						}
						{
						switch ( LA(1)) {
						case '.':
						{
							match('.');
							break;
						}
						case 'E':  case 'e':
						{
							mEXPONENT(false);
							break;
						}
						case 'D':  case 'F':  case 'd':  case 'f':
						{
							mFLOAT_SUFFIX(false);
							break;
						}
						default:
						{
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						}
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched416 = false;
					}
					rewind(_m416);
					inputState.guessing--;
				}
				if ( synPredMatched416 ) {
					{
					int _cnt418=0;
					_loop418:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							if ( _cnt418>=1 ) { break _loop418; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt418++;
					} while (true);
					}
				}
				else if (((LA(1) >= '0' && LA(1) <= '7')) && (true) && (true) && (true)) {
					{
					int _cnt420=0;
					_loop420:
					do {
						if (((LA(1) >= '0' && LA(1) <= '7'))) {
							matchRange('0','7');
						}
						else {
							if ( _cnt420>=1 ) { break _loop420; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt420++;
					} while (true);
					}
				}
				else {
				}
				
				}
				break;
			}
			case '1':  case '2':  case '3':  case '4':
			case '5':  case '6':  case '7':  case '8':
			case '9':
			{
				{
				matchRange('1','9');
				}
				{
				_loop423:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						break _loop423;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					isDecimal=true;
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			if ((LA(1)=='L'||LA(1)=='l')) {
				{
				switch ( LA(1)) {
				case 'l':
				{
					match('l');
					break;
				}
				case 'L':
				{
					match('L');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					_ttype = NUM_LONG;
				}
			}
			else if (((LA(1)=='.'||LA(1)=='D'||LA(1)=='E'||LA(1)=='F'||LA(1)=='d'||LA(1)=='e'||LA(1)=='f'))&&(isDecimal)) {
				{
				switch ( LA(1)) {
				case '.':
				{
					match('.');
					{
					_loop428:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							break _loop428;
						}
						
					} while (true);
					}
					{
					if ((LA(1)=='E'||LA(1)=='e')) {
						mEXPONENT(false);
					}
					else {
					}
					
					}
					{
					if ((LA(1)=='D'||LA(1)=='F'||LA(1)=='d'||LA(1)=='f')) {
						mFLOAT_SUFFIX(true);
						f2=_returnToken;
						if ( inputState.guessing==0 ) {
							t=f2;
						}
					}
					else {
					}
					
					}
					break;
				}
				case 'E':  case 'e':
				{
					mEXPONENT(false);
					{
					if ((LA(1)=='D'||LA(1)=='F'||LA(1)=='d'||LA(1)=='f')) {
						mFLOAT_SUFFIX(true);
						f3=_returnToken;
						if ( inputState.guessing==0 ) {
							t=f3;
						}
					}
					else {
					}
					
					}
					break;
				}
				case 'D':  case 'F':  case 'd':  case 'f':
				{
					mFLOAT_SUFFIX(true);
					f4=_returnToken;
					if ( inputState.guessing==0 ) {
						t=f4;
					}
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
								if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
									_ttype = NUM_FLOAT;
								}
								else {
									_ttype = NUM_DOUBLE; // assume double
								}
								
				}
			}
			else {
			}
			
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EXPONENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'e':
		{
			match('e');
			break;
		}
		case 'E':
		{
			match('E');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt463=0;
		_loop463:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				matchRange('0','9');
			}
			else {
				if ( _cnt463>=1 ) { break _loop463; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt463++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mFLOAT_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'f':
		{
			match('f');
			break;
		}
		case 'F':
		{
			match('F');
			break;
		}
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[3988];
		data[0]=68719476736L;
		data[1]=576460745995190270L;
		data[3]=-36028797027352577L;
		for (int i = 4; i<=127; i++) { data[i]=-1L; }
		for (int i = 193; i<=197; i++) { data[i]=-1L; }
		data[198]=65535L;
		for (int i = 204; i<=205; i++) { data[i]=-1L; }
		for (int i = 208; i<=243; i++) { data[i]=-1L; }
		data[244]=70368744177663L;
		for (int i = 312; i<=639; i++) { data[i]=-1L; }
		for (int i = 996; i<=1003; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[2048];
		data[0]=-9224L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[2048];
		data[0]=-4398046520328L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[2048];
		data[0]=-549755823112L;
		data[1]=-268435457L;
		for (int i = 2; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[2048];
		data[0]=-9224L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[2048];
		data[0]=-17179878408L;
		data[1]=-268435457L;
		for (int i = 2; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[3988];
		data[1]=576460743847706622L;
		data[3]=-36028797027352577L;
		for (int i = 4; i<=127; i++) { data[i]=-1L; }
		for (int i = 193; i<=197; i++) { data[i]=-1L; }
		data[198]=65535L;
		for (int i = 204; i<=205; i++) { data[i]=-1L; }
		for (int i = 208; i<=243; i++) { data[i]=-1L; }
		data[244]=70368744177663L;
		for (int i = 312; i<=639; i++) { data[i]=-1L; }
		for (int i = 996; i<=1003; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[1025];
		data[0]=288019269919178752L;
		data[1]=282016142655614L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	
	}
