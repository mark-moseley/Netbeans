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

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Abbreviation detection detects typing of an abbreviation
 * in the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class AbbrevDetection implements DocumentListener, PropertyChangeListener, KeyListener, CaretListener, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(AbbrevDetection.class.getName());
    
    /**
     * Document property which determines whether an ongoing document modification
     * should be completely ignored by the abbreviation framework.
     * <br/>
     * This is useful e.g. for code templates parameter replication.
     */
    private static final String ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY
            = "abbrev-ignore-modification"; // NOI18N

    private static final String COMPLETION_VISIBLE = "completion-visible"; // NOI18N
    private static final String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; //NOI18N

    private static final String SURROUND_WITH = NbBundle.getMessage(SurroundWithFix.class, "TXT_SurroundWithHint_Label"); //NOI18N
    private static final int SURROUND_WITH_DELAY = 250;
    
    public static AbbrevDetection get(JTextComponent component) {
        AbbrevDetection ad = (AbbrevDetection)component.getClientProperty(AbbrevDetection.class);
        if (ad == null) {
            ad = new AbbrevDetection(component);
            component.putClientProperty(AbbrevDetection.class, ad);
        }
        return ad;
    }
    
    public static synchronized void remove(JTextComponent component) {
        AbbrevDetection ad = (AbbrevDetection)component.getClientProperty(AbbrevDetection.class);
        if (ad != null) {
            assert ad.component == component : "Wrong component: AbbrevDetection.component=" + ad.component + ", component=" + component;
            ad.uninstall();
            component.putClientProperty(AbbrevDetection.class, null);
        }
    }
    
    private JTextComponent component;
    
    /** Document for which this abbreviation detection was constructed. */
    private Document doc;
    
    /**
     * Offset after the last typed character of the collected abbreviation.
     */
    private Position abbrevEndPosition;

    /**
     * Abbreviation characters captured from typing.
     */
    private final StringBuffer abbrevChars = new StringBuffer();

//    /** Chars on which to expand acceptor */
//    private Acceptor expandAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;
    
    private MimePath mimePath = null;
    private Preferences prefs = null;
    private PreferenceChangeListener weakPrefsListener = null;
    
    private ErrorDescription errorDescription = null;
    private List<Fix> surrounsWithFixes = null;
    private Timer surroundsWithTimer;
    
    private AbbrevDetection(JTextComponent component) {
        this.component = component;
        component.addCaretListener(this);
        doc = component.getDocument();
        if (doc != null) {
            doc.addDocumentListener(this);
        }

        String mimeType = DocumentUtilities.getMimeType(component);
        if (mimeType != null) {
            mimePath = MimePath.parse(mimeType);
            prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, this, prefs);
            prefs.addPreferenceChangeListener(weakPrefsListener);
        }
        
        // Load the settings
        preferenceChange(null);
        
        component.addKeyListener(this);
        component.addPropertyChangeListener(this);
        
        surroundsWithTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // #124515, give up when the document is locked otherwise we are likely
                // to cause a deadlock.
                if (!DocumentUtilities.isReadLocked(doc)) {
                    showSurroundWithHint();
                }
            }
        });
        surroundsWithTimer.setRepeats(false);
    }

    private void uninstall() {
        assert component != null : "Can't call uninstall before the construction finished";
        component.removeCaretListener(this);
        if (doc != null) {
            doc.addDocumentListener(this);
        }

        component.removeKeyListener(this);
        component.removePropertyChangeListener(this);
        surroundsWithTimer.stop();
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        String settingName = evt == null ? null : evt.getKey();
        if (settingName == null || "abbrev-reset-acceptor".equals(settingName)) {
            if (prefs != null) {
                resetAcceptor = (Acceptor) callFactory(prefs, mimePath, "abbrev-reset-acceptor", null); //NOI18N
            }
            
            if (resetAcceptor == null) {
                resetAcceptor = AcceptorFactory.WHITESPACE;
            }
        }
    }
    
    public void insertUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt.getDocument()) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                appendTypedText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt.getDocument()) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                removeAbbrevText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
        }
    }

    public void changedUpdate(DocumentEvent evt) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("document".equals(evt.getPropertyName())) { //NOI18N
            if (doc != null) {
                doc.removeDocumentListener(this);
            }
            
            doc = component.getDocument();
            if (doc != null) {
                doc.addDocumentListener(this);
            }

            // unregister and destroy the old preferences (if we have any)
            if (prefs != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
                prefs = null;
                weakPrefsListener = null;
                mimePath = null;
            }
            
            // load and hook up to preferences for the new mime type
            String mimeType = DocumentUtilities.getMimeType(component);
            if (mimeType != null) {
                mimePath = MimePath.parse(mimeType);
                prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, this, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            // reload the settings
            preferenceChange(null);
        }
    }
    
    public void keyPressed(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyReleased(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyTyped(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void caretUpdate(CaretEvent evt) {
        if (evt.getDot() != evt.getMark()) {
            surroundsWithTimer.setInitialDelay(SURROUND_WITH_DELAY);
            surroundsWithTimer.restart();
        } else {
            surroundsWithTimer.stop();
            hideSurroundWithHint();
        }
    }

    private boolean isIgnoreModification() {
        return Boolean.TRUE.equals(doc.getProperty(ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY));
    }
    
    private boolean isAbbrevDisabled() {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component) || Boolean.TRUE.equals(component.getClientProperty(COMPLETION_VISIBLE));
    }
    
    private void checkExpansionKeystroke(KeyEvent evt) {
        if (abbrevEndPosition != null && component != null
            && component.getCaretPosition() == abbrevEndPosition.getOffset()
            && !isAbbrevDisabled()
            && !Boolean.TRUE.equals(doc.getProperty(EDITING_TEMPLATE_DOC_PROPERTY))
        ) {
            CodeTemplateManagerOperation operation = CodeTemplateManagerOperation.get(component.getDocument(), abbrevEndPosition.getOffset());
            if (operation != null) {
                KeyStroke expandKeyStroke = operation.getExpansionKey();

                if (expandKeyStroke.equals(KeyStroke.getKeyStrokeForEvent(evt))) {
                    if (expand(operation)) {
                        evt.consume();
                    }
                }
            }
        }
    }

    /**
     * Get current abbreviation string.
     */
    private CharSequence getAbbrevText() {
        return abbrevChars;
    }

    /**
     * Reset abbreviation string collecting.
     */
    private void resetAbbrevChars() {
        synchronized(abbrevChars) {
            abbrevChars.setLength(0);
            abbrevEndPosition = null;
        }
    }
    
    private void appendTypedText(int offset, int insertLength) {
        if (abbrevEndPosition == null
            || offset + insertLength != abbrevEndPosition.getOffset()
        ) {
            // Does not follow previous insert
            resetAbbrevChars();
        }

        if (abbrevEndPosition == null) { // starting the new string
            try {
                // Start new accounting if previous char would reset abbrev
                // i.e. check that not start typing 'u' after existing 'p' which would
                // errorneously expand to 'public'
                if (offset == 0
                        || resetAcceptor.accept(DocumentUtilities.getText(doc, offset - 1, 1).charAt(0))
                ) {
                    abbrevEndPosition = doc.createPosition(offset + insertLength);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        if (abbrevEndPosition != null) {
            try {
                String typedText = doc.getText(offset, insertLength); // typically just one char
                boolean textAccepted = true;
                for (int i = typedText.length() - 1; i >= 0; i--) {
                    if (resetAcceptor.accept(typedText.charAt(i))) {
                        // In theory there could be more than one character in the typed text
                        // and the resetting could occur on the very first char
                        // the next chars would not be accumulated as the insert
                        // is treated as a batch.
                        textAccepted = false;
                        break;
                    }
                }
                
                if (textAccepted) {
                    abbrevChars.append(typedText);
                    // abbrevEndPosition should move appropriately
                } else {
                    resetAbbrevChars();
                }

            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
                resetAbbrevChars();
            }
        }
    }
    
    private void removeAbbrevText(int offset, int removeLength) {
        synchronized(abbrevChars) {
            if (abbrevEndPosition != null) {
                // Abbrev position should already move appropriately
                if (offset == abbrevEndPosition.getOffset()
                    && abbrevChars.length() >= removeLength
                ) { // removed at end
                    abbrevChars.setLength(abbrevChars.length() - removeLength);

                } else {
                    resetAbbrevChars();
                }
            }
        }
    }

    public boolean expand(CodeTemplateManagerOperation op) {
        CharSequence abbrevText = getAbbrevText();
        int abbrevEndOffset = abbrevEndPosition.getOffset();
        if (expand(op, component, abbrevEndOffset - abbrevText.length(), abbrevText)) {
            resetAbbrevChars();
            return true;
        } else {
            return false;
        }
    }
    
    private void showSurroundWithHint() {
        surrounsWithFixes = SurroundWithFix.getFixes(component);
        if (!surrounsWithFixes.isEmpty()) {
            try {                
                Position pos = doc.createPosition(component.getCaretPosition());
                errorDescription = ErrorDescriptionFactory.createErrorDescription(
                        Severity.HINT, SURROUND_WITH, surrounsWithFixes, doc, pos, pos);
                
                HintsController.setErrors(doc, SURROUND_WITH, Collections.singleton(errorDescription));
            } catch (BadLocationException ble) {
                Logger.getLogger("global").log(Level.WARNING, ble.getMessage(), ble);
            }
        } else {
            hideSurroundWithHint();
        }
    }

    private void hideSurroundWithHint() {
        if (surrounsWithFixes != null)
            surrounsWithFixes = null;
        if (errorDescription != null) {
            errorDescription = null;
            HintsController.setErrors(doc, SURROUND_WITH, Collections.<ErrorDescription>emptySet());
        }
    }

    // copied from org.netbeans.modules.editor.lib.SettingsConversions
    private static Object callFactory(Preferences prefs, MimePath mimePath, String settingName, Object defaultValue) {
        String factoryRef = prefs.get(settingName, null);
        
        if (factoryRef != null) {
            int lastDot = factoryRef.lastIndexOf('.'); //NOI18N
            assert lastDot != -1 : "Need fully qualified name of class with the static setting factory method."; //NOI18N

            String classFqn = factoryRef.substring(0, lastDot);
            String methodName = factoryRef.substring(lastDot + 1);

            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            try {
                Class factoryClass = loader.loadClass(classFqn);
                Method factoryMethod;
                
                try {
                    // normally the method should accept mime path and the a setting name
                    factoryMethod = factoryClass.getDeclaredMethod(methodName, MimePath.class, String.class);
                } catch (NoSuchMethodException nsme) {
                    // but there might be methods that don't need those params
                    try {
                        factoryMethod = factoryClass.getDeclaredMethod(methodName);
                    } catch (NoSuchMethodException nsme2) {
                        // throw the first exception complaining about the full signature
                        throw nsme;
                    }
                }
                
                Object value;
                if (factoryMethod.getParameterTypes().length == 2) {
                    value = factoryMethod.invoke(null, mimePath, settingName);
                } else {
                    value = factoryMethod.invoke(null);
                }
                
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }

        return defaultValue;
    }

    private static boolean expand(CodeTemplateManagerOperation op, JTextComponent component, int abbrevStartOffset, CharSequence abbrev) {
        op.waitLoaded();
        CodeTemplate ct = op.findByAbbreviation(abbrev.toString());
        if (ct != null) {
            try {
                // Remove the abbrev text
                Document doc = component.getDocument();
                doc.remove(abbrevStartOffset, abbrev.length());
            } catch (BadLocationException ble) {
            }
            ct.insert(component);
            return true;
        } else {
            return false;
        }
    }
}
