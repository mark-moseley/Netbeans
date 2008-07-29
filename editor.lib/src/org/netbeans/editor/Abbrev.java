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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.editor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/** 
 * Abbreviation support allowing to expand defined character sequences
 * into the expanded strings or call the arbitrary action.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated Use Editor Code Templates API instead. This class is no longer
 *   functional.
 */
public class Abbrev implements /* SettingsChangeListener,*/ PropertyChangeListener {
    
    /**
     * Test whether the abbreviation expansion is disabled
     * at the caret position in the given component's document.
     * 
     * @param component non-null text component.
     * @return true if the abbreviation can be expanded or false if not.
     */
    public static boolean isAbbrevDisabled(JTextComponent component) {
        Document doc = component.getDocument();
        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = (BaseDocument)doc;
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            if (sup != null) {
                return sup.isAbbrevDisabled(component.getCaretPosition());
            }
        }
        return false; // abbrev not disabled
    }

    /** Abbreviation accounting string. Here the characters forming
    * abbreviation are stored.
    */
    private StringBuffer abbrevSB = new StringBuffer();

    /** Check whether the document text matches the abbreviation accounting
    * string.
    */
    private boolean checkDocText;

    /** Additional check whether the character right before the abbreviation
    * string in the text is not accepted by the <tt>addTypedAcceptor</tt>.
    * This test is only performed if <tt>checkDocText</tt> is true.
    */
    private boolean checkTextDelimiter;

    /** Extended UI to which this abbreviation is associated to */
    protected EditorUI editorUI;

    /** Chars on which to expand acceptor */
    private Acceptor doExpandAcceptor;

    /** Whether add the typed char */
    private Acceptor addTypedAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;

    /** Abbreviation map */
    private HashMap abbrevMap;

    public Abbrev(EditorUI editorUI, boolean checkDocText, boolean checkTextDelimiter) {
        this.editorUI = editorUI;
        this.checkDocText = checkDocText;
        this.checkTextDelimiter = checkTextDelimiter;

//        Settings.addSettingsChangeListener(this);

        synchronized (editorUI.getComponentLock()) {
            // if component already installed in EditorUI simulate installation
            JTextComponent component = editorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(editorUI,
                                                       EditorUI.COMPONENT_PROPERTY, null, component));
            }

            editorUI.addPropertyChangeListener(this);
        }
    }

//    /** Called when settings were changed. The method is called
//    * by editorUI when settings were changed and from constructor.
//    */
//    public void settingsChange(SettingsChangeEvent evt) {
//        Class kitClass = Utilities.getKitClass(editorUI.getComponent());
//
//        if (kitClass != null) {
//            String settingName = (evt != null) ? evt.getSettingName() : null;
//
//            if (settingName == null || SettingsNames.ABBREV_ACTION_MAP.equals(settingName)
//                || SettingsNames.ABBREV_MAP.equals(settingName) 
//            ) {
//                abbrevMap = new HashMap();
//                // Inspect action abbrevs
//                Map m = (Map)Settings.getValue(kitClass, SettingsNames.ABBREV_ACTION_MAP);
//                if (m != null) {
//                    BaseKit kit = Utilities.getKit(editorUI.getComponent());
//                    Iterator iter = m.entrySet().iterator();
//                    while (iter.hasNext()) {
//                        Map.Entry me = (Map.Entry)iter.next();
//                        Object value = me.getValue();
//                        Action a = null;
//                        if (value instanceof String) {
//                            a = kit.getActionByName((String)value);
//                        } else if (value instanceof Action) {
//                            a = (Action)value;
//                        }
//
//                        if (a != null) {
//                            abbrevMap.put(me.getKey(), a);
//                        }
//                    }
//                }
//                
//                m = (Map)Settings.getValue(kitClass, SettingsNames.ABBREV_MAP);
//                if (m != null) {
//                    Iterator iter = m.entrySet().iterator();
//                    while (iter.hasNext()) {
//                        Map.Entry me = (Map.Entry)iter.next();
//                        Object value = me.getValue();
//                        if (value != null) {
//                            abbrevMap.put(me.getKey(), value);
//                        }
//                    }
//                }
//            }
//
//            if (settingName == null || SettingsNames.ABBREV_EXPAND_ACCEPTOR.equals(settingName)) {
//                doExpandAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_EXPAND_ACCEPTOR, AcceptorFactory.FALSE);
//            }
//            if (settingName == null || SettingsNames.ABBREV_ADD_TYPED_CHAR_ACCEPTOR.equals(settingName)) {
//                addTypedAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_ADD_TYPED_CHAR_ACCEPTOR, AcceptorFactory.FALSE);
//            }
//            if (settingName == null || SettingsNames.ABBREV_RESET_ACCEPTOR.equals(settingName)) {
//                resetAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_RESET_ACCEPTOR, AcceptorFactory.TRUE);
//            }
//        }
//    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
            JTextComponent component = (JTextComponent)evt.getNewValue();
            if (component != null) { // just installed

//                settingsChange(null);

            } else { // just deinstalled
                //        component = (JTextComponent)evt.getOldValue();

            }

        }
    }

    /** Reset abbreviation accounting. */
    public void reset() {
        abbrevSB.setLength(0);
    }

    /** Add typed character to abbreviation accounting string. */
    public void addChar(char ch) {
        abbrevSB.append(ch);
    }

    /** Get current abbreviation string */
    public String getAbbrevString() {
        return abbrevSB.toString();
    }

    /** Get mapping table [abbrev, expanded-abbrev] */
    public Map getAbbrevMap() {
        return abbrevMap;
    }

    /** Translate string using abbreviation table
    * @param abbrev string to translate. Pass null to translate current abbreviation
    *    string
    * @return expanded abbreviation
    */
    public Object translateAbbrev(String abbrev) {
        String abbStr = (abbrev != null) ? abbrev : abbrevSB.toString();
        return getAbbrevMap().get(abbStr);
    }

    /** Checks whether there's valid string to expand and if so it returns it.
    */
    public String getExpandString(char typedChar) {
        return (doExpandAcceptor.accept(typedChar)) ? getExpandString() : null;
    }

    public String getExpandString() {
        BaseDocument doc = (BaseDocument)editorUI.getDocument();
        String abbrevStr = getAbbrevString();
        int abbrevStrLen = abbrevStr.length();
        Object expansion = translateAbbrev(abbrevStr);
        Caret caret = editorUI.getComponent().getCaret();
        int dotPos = caret.getDot();
        if (abbrevStr != null && expansion != null
                && dotPos >= abbrevStrLen
           ) {
            if (checkDocText) {
                try {
                    CharSequence prevChars = DocumentUtilities.getText(doc, dotPos - abbrevStrLen, abbrevStrLen);
                    if (CharSequenceUtilities.textEquals(prevChars, abbrevStr)) { // abbrev chars really match text
                        if (!checkTextDelimiter || dotPos == abbrevStrLen
                                || resetAcceptor.accept(
                                    doc.getChars(dotPos - abbrevStrLen - 1, 1)[0])
                           ) {
                            return abbrevStr;
                        }
                    }
                } catch (BadLocationException e) {
                }
            }
        }
        return null;
    }

    protected boolean doExpansion(int dotPos, String expandStr, ActionEvent evt)
    throws BadLocationException {
        Object expansion = translateAbbrev(expandStr);
        boolean expanded = false;
        if (expansion instanceof String) { // expand to string
            BaseDocument doc = editorUI.getDocument();
            String ins = (String)expansion;
            int offset = ins.indexOf('|');
            if (offset >= 0) {
                if (offset > 0) doc.insertString(dotPos, ins.substring(0, offset), null);
                if (offset+1 < ins.length()) doc.insertString(dotPos + offset,
                        ins.substring(offset + 1), null);
                Caret caret = editorUI.getComponent().getCaret();
                caret.setDot(dotPos + offset);
            } else {
                doc.insertString(dotPos, ins, null);
            }

            if(ins.indexOf("\n") != -1) { // NOI18N
                Formatter formatter = doc.getFormatter();
                formatter.reformatLock();
                try {
                    formatter.reformat(doc, dotPos, dotPos + ins.length());
                } finally {
                    formatter.reformatUnlock();
                }
            }
            
            expanded = true;
        } else if (expansion instanceof Action) {
            ((Action)expansion).actionPerformed(evt);
            expanded = true;
        }
        return expanded;
    }

    public boolean expandString(char typedChar, String expandStr, ActionEvent evt)
    throws BadLocationException {
        if (expandString(expandStr, evt)) {
            if (addTypedAcceptor.accept(typedChar)) {
                int dotPos = editorUI.getComponent().getCaret().getDot();
                editorUI.getDocument().insertString(dotPos, String.valueOf(typedChar), null);
            }
            return true;
        }
        return false;
    }

    /** Expand abbreviation on current caret position.
    * Remove characters back to the word start and insert expanded abbreviation.
    * @return whether the typed character should be added to the abbreviation or not
    */
    public boolean expandString(final String expandStr, final ActionEvent evt)
    throws BadLocationException {
        // Disabled due to code templates
        if (true) {
            reset();
            return true;
        }

        final BaseDocument doc = editorUI.getDocument();
        final Object[] result = new Object [1];
        doc.runAtomic (new Runnable () {
            public void run () {
                try {
                    Caret caret = editorUI.getComponent().getCaret();
                    int pos = caret.getDot() - expandStr.length();
                    try {
                        doc.remove(pos, expandStr.length());
                        result [0] = doExpansion(pos, expandStr, evt);
                    } catch (BadLocationException ex) {
                        result [0] = ex;
                    }
                } finally {
                    if (result [0] instanceof Boolean && (Boolean) result [0]) {
                        reset();
                    } else {
                        doc.breakAtomicLock();
                    }
                }
            }
        });
        if (result [0] instanceof BadLocationException)
            throw (BadLocationException) result [0];
        return (Boolean) result [0];
    }

    public boolean checkReset(char typedChar) {
        if (resetAcceptor.accept(typedChar)) {
            reset();
            return true;
        }
        return false;
    }

    public boolean checkAndExpand(char typedChar, ActionEvent evt)
    throws BadLocationException {
        boolean doInsert = true;
        boolean disableAbbrev = false;

        // Check whether the expansion should be prevented
        // in certain tokens
        JTextComponent component = editorUI.getComponent();
        Document doc = component.getDocument();
        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = (BaseDocument)doc;
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            disableAbbrev = sup.isAbbrevDisabled(component.getCaretPosition());
        }
        
        if (disableAbbrev) {
            reset();
        } else {
            String expandStr = getExpandString(typedChar);
            if (expandStr != null) { // should expand
                doInsert = false;
                expandString(typedChar, expandStr, evt);
            } else {
                addChar(typedChar);
            }
            checkReset(typedChar);
        }
        return doInsert;
    }

    public void checkAndExpand(ActionEvent evt)
    throws BadLocationException {
        String expandStr = getExpandString();
        if (expandStr != null) {
            expandString(expandStr, evt);
        }
    }

}
