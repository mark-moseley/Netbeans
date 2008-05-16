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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.i18n.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nSupport.I18nFinder;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.regexp.ParseException;
import org.netbeans.modules.i18n.regexp.Translator;
import org.netbeans.modules.properties.UtilConvert;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/*
 * This class was originally written as a static nested class of class
 * {@link JavaI18nSupport}.
 */

/**
 * Finder which search hard coded strings in java sources.
 * 
 * @author Peter Zavadsky
 */
public class JavaI18nFinder implements I18nFinder {

    /** State when finder is in normal java code. */
    protected static final int STATE_JAVA = 0;
    /** State when finder is at backslash in normal java code. */
    protected static final int STATE_JAVA_A_SLASH = 1;
    /** State when finder is in line comment. */
    protected static final int STATE_LINECOMMENT = 2;
    /** State when finder is in block comment. */
    protected static final int STATE_BLOCKCOMMENT = 3;
    /** State when finder is at star in block commnet. */
    protected static final int STATE_BLOCKCOMMENT_A_STAR = 4;
    /** State when finder is in string found in nornal java code. */
    protected static final int STATE_STRING = 5;
    /** State when finder is at backslash in string. */
    protected static final int STATE_STRING_A_BSLASH = 6;
    /** State when finder is in char in noraml java code. */
    protected static final int STATE_CHAR = 7; // to avoid misinterpreting of '"' resp. '\"' char.

    /** Document on which the search is performed. */
    protected StyledDocument document;

    /** Keeps current state. */
    protected int state;

    /** Flag of search type, if it is searched for i18n-ized strings or non-i18n-ized ones. */
    protected boolean i18nSearch;

    /** Keeps position from last search iteration. */
    protected Position lastPosition;

    /** Helper variable for keeping the java string (means pure java code, no coments etc.). */
    protected StringBuffer lastJavaString;

    /** Helper variable. Buffer at which perform search. */
    protected char[] buffer;

    /** Helper variable. Actual position of search in buffer. */
    protected int position;

    /** Helper variable. Start of actual found hard coded string or -1. */
    protected int currentStringStart;

    /** Helper variable. End of actual found hard coded string or -1. */
    protected int currentStringEnd;


    /** Constructs finder. */
    public JavaI18nFinder(StyledDocument document) {
        super();
        this.document = document;

        init();
    }

    /** Initializes finder. */
    private void init() {
        state = STATE_JAVA;
        initJavaStringBuffer();

        lastPosition = null;
    }

    /** Resets finder. */
    protected void reset() {
        init();
    }

    /**
     * Implements <code>I18nFinder</code> interface method.
     * Finds all non-internationalized hard coded strings in source document. */
    public HardCodedString[] findAllHardCodedStrings() {
        reset();
        i18nSearch = false;

        return findAllStrings();
    }

    /**
     * Implements <code>I18nFinder</code> inetrface method. 
     * Finds hard coded non-internationalized string in buffer.
     * @return next <code>HardCodedString</code> or null if there is no more one.
     */
    public HardCodedString findNextHardCodedString() {
        i18nSearch = false;

        return findNextString();
    }

    /**
     * Implements <code>I18nFinder</code> interface method.
     * Finds all internationalized hard coded strings in source document. 
     * It's used in test tool. */
    public HardCodedString[] findAllI18nStrings() {
        reset();
        i18nSearch = true;

        return findAllStrings();
    }

    /**
     * Implements <code>I18nFinder</code> inetrface method. 
     * Finds hard coded internationalized string in buffer. It's used in test tool.
     * @return next <code>HardCodedString</code> or null if there is no more one.
     */
    public HardCodedString findNextI18nString() {
        i18nSearch = true;

        return findNextString();
    }

    /** Finds all strings according specified regular expression. */
    protected HardCodedString[] findAllStrings() {

        List<HardCodedString> list = new ArrayList<HardCodedString>();

        HardCodedString hardString;
        while ((hardString = findNextString()) != null) {
            list.add(hardString);
        }

        return !list.isEmpty()
               ? list.toArray(new HardCodedString[list.size()])
               : null;
    }

    protected HardCodedString findNextString() {
        // Reset buffer.
        try {
            buffer = document.getText(0, document.getLength()).toCharArray();
        } catch (BadLocationException ble) {
            if (Boolean.getBoolean("netbeans.debug.exception")) {       //NOI18N
                ble.printStackTrace();
            }
            return null;
        }

        // Initialize position.
        position = (lastPosition == null)
                   ? 0
                   : lastPosition.getOffset();

        // Reset hard coded string offsets.
        currentStringStart = -1;
        currentStringEnd = -1;

        // Now serious work.
        while (position < buffer.length) {

            char ch = buffer[position];

            // Other chars than '\n' (new line).
            if (ch != '\n') {
                HardCodedString foundHardString = handleCharacter(ch);
                if (foundHardString != null) {
                    return foundHardString;
                }
            } else {
                handleNewLineCharacter();
            }
            position++;
        } // End of while.

        // Indicate end was reached and nothing found.
        return null;
    }

    /** Handles state changes according next charcter. */
    protected HardCodedString handleCharacter(char character) {
        if (state == STATE_JAVA) {
            return handleStateJava(character);
        } else if (state == STATE_JAVA_A_SLASH) {
            return handleStateJavaASlash(character);
        } else if (state == STATE_CHAR) {
            return handleStateChar(character);
        } else if (state == STATE_STRING_A_BSLASH) {
            return handleStateStringABSlash(character);
        } else if (state == STATE_LINECOMMENT) {
            return handleStateLineComment(character);
        } else if (state == STATE_BLOCKCOMMENT) {
            return handleStateBlockComment(character);
        } else if (state == STATE_BLOCKCOMMENT_A_STAR) {
            return handleStateBlockCommentAStar(character);
        } else if (state == STATE_STRING) {
            return handleStateString(character);
        }

        return null;
    }

    /** Handles state when new line '\n' char occures. */
    protected void handleNewLineCharacter() {
        // New line char '\n' -> reset the state.
        if (state == STATE_JAVA
                || state == STATE_JAVA_A_SLASH
                || state == STATE_CHAR
                || state == STATE_LINECOMMENT
                || state == STATE_STRING
                || state == STATE_STRING_A_BSLASH) {
            initJavaStringBuffer();
            currentStringStart = -1;
            currentStringEnd = -1;
            state = STATE_JAVA;
        } else if (state == STATE_BLOCKCOMMENT
                   || state == STATE_BLOCKCOMMENT_A_STAR) {
            state = STATE_BLOCKCOMMENT;
        }
    }

    /** Handles state <code>STATE_JAVA</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateJava(char character) {
        lastJavaString.append(character);
        if (character == '/') {
            state = STATE_JAVA_A_SLASH;
        } else if (character == '\"') {
            state = STATE_STRING;
            if (currentStringStart == -1) {
                // Found start of hard coded string.
                currentStringStart = position;
            }
        } else if (character == '\'') {
            state = STATE_CHAR;
        }

        return null;
    }

    /** Handles state <code>STATE_JAVA_A_SLASH</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateJavaASlash(char character) {
        lastJavaString.append(character);
        if (character == '/') {
            state = STATE_LINECOMMENT;
        } else if (character == '*') {
            state = STATE_BLOCKCOMMENT;
        }

        return null;
    }

    /** Handles state <code>STATE_CHAR</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateChar(char character) {
        lastJavaString.append(character);

        if (character == '\'') {
            state = STATE_JAVA;
        }

        return null;
    }

    /** Handles state <code>STATE_STRING_A_BSLASH</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateStringABSlash(char character) {
        state = STATE_STRING;

        return null;
    }

    /** Handles state <code>STATE_LINECOMMENT</code>.
     * @param character char to proceede 
     * @return null */
    protected HardCodedString handleStateLineComment(char character) {
        return null;
    }

    /** Handles state <code>STATE_BLOCKCOMMENT</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateBlockComment(char character) {
        if (character == '*') {
            state = STATE_BLOCKCOMMENT_A_STAR;
        }

        return null;
    }

    /** Handles state <code>STATE_BLOCKCOMMENT_A_STAR</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateBlockCommentAStar(char character) {
        if (character == '/') {
            state = STATE_JAVA;
            initJavaStringBuffer();
        } else if (character != '*') {
            state = STATE_BLOCKCOMMENT;
        }

        return null;
    }

    /** Handles state <code>STATE_STRING</code>.
     * @param character char to proceede 
     * @return <code>HardCodedString</code> or null if not found yet */
    protected HardCodedString handleStateString(char character) {
        if (character == '\\') {
            state = STATE_STRING_A_BSLASH;
        } else if (character == '\"') {
            state = STATE_JAVA;
            if ((currentStringEnd == -1) && (currentStringStart != -1)) {
                // Found end of hard coded string.
                currentStringEnd = position + 1;

                int foundStringLength = currentStringEnd - currentStringStart;
                try {
                    // Get hard coded string.
                    Position hardStringStart = document.createPosition(currentStringStart);
                    Position hardStringEnd = document.createPosition(currentStringEnd);

                    String hardString = document.getText(hardStringStart.getOffset(),
                                                         foundStringLength);

                    // Retrieve offset of the end of line where was found hard coded string.
                    String restBuffer = new String(buffer,
                                                   currentStringEnd,
                                                   buffer.length - currentStringEnd);
                    int endOfLine = restBuffer.indexOf('\n');
                    if (endOfLine == -1) {
                        endOfLine = restBuffer.length();
                    }

                    lastJavaString.append(document.getText(currentStringStart + 1,
                                                           hardString.length()));
                    
                    // Get the rest of line.
                    String restOfLine = document.getText(currentStringStart + 1 + hardString.length(),
                                                         currentStringEnd + endOfLine - currentStringStart - hardString.length());

                    // Replace rest of occurences of \" to cheat out regular expression for very minor case when the same string is after our at the same line.
                    lastJavaString.append(restOfLine.replace('\"', '_'));

                    // If not matches regular expression -> is not internationalized.
                    if (isSearchedString(lastJavaString.toString(), hardString)) {
                        lastPosition = hardStringEnd;

                        // Search was successful -> return.
                        return new HardCodedString(extractString(hardString),
                                                   hardStringStart,
                                                   hardStringEnd);
                    }
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                     ble);
                } finally {
                    currentStringStart = -1;
                    currentStringEnd = -1;

                    initJavaStringBuffer();
                }
            }
        }

        return null;
    }

    /** Resets <code>lastJavaString</code> variable.
     * @see #lastJavaString*/
    private void initJavaStringBuffer() {
        lastJavaString = new StringBuffer();
    }

    /** Helper utility method. */
    private String extractString(String sourceString) {
        if (sourceString == null) {
            return "";                                                  //NOI18N
        }

        if ((sourceString.length() >= 2)
                && (sourceString.charAt(0) == '\"')
                && (sourceString.charAt(sourceString.length() - 1) == '\"')) {
            sourceString = sourceString.substring(1, sourceString.length() - 1);
        }
        return sourceString;
    }

    /** 
     * Help method for decision if found hard coded string is searched string. It means
     * if it is i18n-zed or non-internationalized (depending on <code>i18nSearch</code> flag. 
     * <p>
     * The part of line 
     * (starts after previous found hard coded string) with current found hard code string is compared
     * against regular expression which can user specify via i18n options. If the compared line matches 
     * that regular expression the hard coded string is considered as internationalized.
     *
     * @param partHardLine line of code which includes hard coded string and starts from beginning or
     * the end of previous hard coded string.
     * @param hardString found hard code string
     * @return <code>true<code> if string is internationalized and <code>i18nSearch</code> flag is <code>true</code>
     *   or if if string is non-internationalized and <code>i18nSearch</code> flag is <code>false</code> */
    protected boolean isSearchedString(String partHardLine, String hardString) {
        String lineToMatch = UtilConvert.unicodesToChars(partHardLine);

        Boolean regexpTestResult;
        Exception ex = null;
        try {
            String regexp = createRegularExpression(hardString);
            regexpTestResult = (Pattern.compile(regexp).matcher(lineToMatch).find()
                                == i18nSearch);     //auto-boxing
        } catch (ParseException ex1) {
            ex = ex1;
            regexpTestResult = null;
        } catch (PatternSyntaxException ex2) {
            ex = ex2;
            regexpTestResult = null;
        }
        if (Boolean.FALSE.equals(regexpTestResult)) {
            /*
             * the string is an identifier of a bundle or a key
             * of a bundle entry
             */
            return false;
        }

        final AnnotationDetector annotationDetector = new AnnotationDetector(currentStringStart);
        try {
            boolean firstTry = true;
            do {
                if (!firstTry) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException exInterrupted) {
                        Exceptions.printStackTrace(exInterrupted);
                        //but still continue
                    }
                }
                annotationDetector.reset();
                JavaSource.forDocument(document).runUserActionTask(
                                                        annotationDetector,
                                                        true);
                firstTry = false;
            } while (annotationDetector.wasCancelled());
        } catch (IOException ioEx) {
            Exceptions.printStackTrace(ioEx);
        }
        if (annotationDetector.wasAnnotationDetected()) {
            /* the string is within an annotation */
            return false;
        }

        if (regexpTestResult != null) {
            /* both tests passed */
            assert regexpTestResult.equals(Boolean.TRUE);
            return true;
        }

        /*
         * Handle the situation that some syntax error has been detected:
         */
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);

        // Indicate error, but allow user what to do with the found hard coded string to be able go thru
        // this problem.
        // Note: All this shouldn't happen. The reason is 1) bad set reg exp format (in options) or 
        // 2) it's error in this code.
        String msg = NbBundle.getMessage(JavaI18nSupport.class,
                                         "MSG_RegExpCompileError",      //NOI18N
                                         hardString);

        Object answer = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                        msg,
                        NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE));
        return NotifyDescriptor.YES_OPTION.equals(answer);
    }

     /**
      * Creates a regular expression matching the pattern specified in the
      * module options.
      * The pattern specified in the options contains a special token
      * <code>{hardString}</code>. This token is replaced with a regular
      * expression matching exactly the string passed as a parameter
      * and a result of this substitution is returned.
      *
      * @param  hardString  hard-coded string whose regexp-equivalent is
      *                     to be put in place of token
      *                     <code>{hardString}</code>
      * @return  regular expression matching the pattern specified
      *          in the module options
      */
    private String createRegularExpression(String hardString)
                                                        throws ParseException {
        String regexpForm;
        if (i18nSearch) {
            regexpForm = I18nUtil.getOptions().getI18nRegularExpression();
        } else {
            regexpForm = I18nUtil.getOptions().getRegularExpression();
        }

        /*
         * Translate the regexp form to the JDK's java.util.regex syntax
         * and replace tokens "{key}" and "{hardString}" with the passed
         * hard-coded string.
         */
        Map<String, String> map = new HashMap<String, String>(3);
        map.put("key", hardString);   //older form of regexp format     //NOI18N
        map.put("hardString", hardString);                              //NOI18N
        return Translator.translateRegexp(regexpForm, map);
    }

    /**
     * Task that determines whether there is a Java annotation at the given
     * cursor position. It is made for the situation that it is known that
     * there is a string literal at the given cursor position and the goal
     * is to check whether that string literal is a part of an annotation.
     * It may not work in cases that there is e.g. a numeric constant
     * at the current cursor position.
     * 
     * @author  Marian Petras
     */
    static final class AnnotationDetector implements CancellableTask<CompilationController> {

        private final int caretPosition;
        private volatile boolean cancelled;
        private boolean annotationDetected = false;

        private AnnotationDetector(int caretPosition) {
            this.caretPosition = caretPosition;
        }

        void reset() {
            cancelled = false;
            annotationDetected = false;
        }

        public void run(CompilationController controller) throws IOException {
            if (cancelled) {
                return;
            }

            controller.toPhase(Phase.RESOLVED); //cursor position needed
            if (cancelled) {
                return;
            }

            TreePath treePath = controller.getTreeUtilities()
                                          .pathFor(caretPosition);
            if (treePath == null) {
                return;
            }
            Tree.Kind kind = treePath.getLeaf().getKind();
            if (kind == Tree.Kind.STRING_LITERAL) {
                if ((treePath = treePath.getParentPath()) == null) {
                    return;
                }
                kind = treePath.getLeaf().getKind();
            }
            if (kind == Tree.Kind.NEW_ARRAY) {
                if ((treePath = treePath.getParentPath()) == null) {
                    return;
                }
                kind = treePath.getLeaf().getKind();
            }
            if (kind == Tree.Kind.ASSIGNMENT) {
                if ((treePath = treePath.getParentPath()) == null) {
                    return;
                }
                kind = treePath.getLeaf().getKind();
            }

            annotationDetected = (kind == Tree.Kind.ANNOTATION);
        }

        public void cancel() {
            cancelled = true;
        }

        boolean wasCancelled() {
            return cancelled;
        }

        boolean wasAnnotationDetected() {
            return annotationDetected;
        }
    }
}