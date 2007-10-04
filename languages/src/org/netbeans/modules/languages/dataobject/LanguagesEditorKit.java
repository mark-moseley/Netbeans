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

package org.netbeans.modules.languages.dataobject;

import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.Action;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import javax.swing.JLabel;
import javax.swing.text.Document;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.TextAction;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;

import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.editor.PopupManager;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUI;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.features.AnnotationManager;
import org.netbeans.modules.languages.features.BraceCompletionDeleteAction;
import org.netbeans.modules.languages.features.BraceCompletionInsertAction;
import org.netbeans.modules.languages.features.InstantRenameAction;
import org.netbeans.modules.languages.features.MarkOccurrencesSupport;
import org.netbeans.modules.languages.features.CollapseFoldTypeAction;
import org.netbeans.modules.languages.features.ExpandFoldTypeAction;
import org.netbeans.modules.languages.features.HyperlinkListener;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.languages.features.DatabaseManager;
import org.netbeans.modules.languages.features.LanguagesGenerateFoldPopupAction;
import org.netbeans.modules.languages.parser.Pattern;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesEditorKit extends NbEditorKit {

    private final String mimeType;
    
    // XXX: Never use this to initialize settings that are mime type specific.
    // This is a know deficiency in the editor settings API; not all settings
    // can be initialized in the mime type friendly way.
    // See http://www.netbeans.org/nonav/issues/show_bug.cgi?id=114747,
    //     http://www.netbeans.org/nonav/issues/show_bug.cgi?id=114234
    //
    // Also, the INITIALIZER is added from the Install class to make this class
    // and the whole module unloadable.
    /* package */ static final Settings.Initializer INITIALIZER = new Settings.AbstractInitializer("LanguagesEditorKit.Settings.Initializer") { //NOI18N
        public void updateSettingsMap (Class kitClass, Map settingsMap) {
            if (kitClass != null && kitClass.equals (LanguagesEditorKit.class)) {
                settingsMap.put (SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
            }
        }
    };
        
    /** 
     * Creates a new instance of LanguagesEditorKit 
     */
    public LanguagesEditorKit (String mimeType) { 
        this.mimeType = mimeType;
        if (mimeType == null) {
            throw new NullPointerException ();
        }
    }
    
    private JLabel label;
    
    private JLabel createToolTipComponent () {
        if (label == null) {
            label = new JLabel () {
                public @Override void setSize(int width, int height) {
                    if (getText () == null) {
                        super.setSize (width, height);
                        return;
                    }
                    int docLen = getText ().length ();
                    if (docLen > 0) { // nonzero length
                        Dimension prefSize = getPreferredSize();
                        if (width > prefSize.width) { // given width unnecessarily big
                            width = prefSize.width; // shrink the width to preferred
                            if (height >= prefSize.height) {
                                height = prefSize.height;
                            } else { // height not big enough
                                height = -1;
                            }

                        } else { // available width not enough - wrap lines
                            super.setSize(width, 100000);
//                            try {
                                //Rectangle r = modelToView(docLen - 1);
                                int prefHeight = getPreferredSize ().height;//r.y + r.height;
                                if (prefHeight < height) {
                                    height = prefHeight;

                                } else { // the given height is too small
                                    height = -1;
                                }
//                            } catch (BadLocationException e) {
//                            }
                        }
                    }

                    if (height >= 0) { // only for valid height
                        super.setSize(width, height);
                    } else { // signal that the height is too small to display tooltip
                        putClientProperty(PopupManager.Placement.class, null);
                    }
                }
            };

            // bugfix of #43174
            label.setActionMap (new ActionMap ());
            label.setInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);

            Font font = UIManager.getFont ("ToolTip.font"); // NOI18N
            Color backColor = UIManager.getColor("ToolTip.background"); // NOI18N
            Color foreColor = UIManager.getColor("ToolTip.foreground"); // NOI18N

            if (font != null) {
                label.setFont(font);
            }
            if (foreColor != null) {
                label.setForeground(foreColor);
            }
            if (backColor != null) {
                label.setBackground(backColor);
            }

            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(label.getForeground()),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));
        }
        return label;
    }

    protected @Override Action[] createActions() {
        Action[] myActions = new Action[] {
            new BraceCompletionInsertAction (),
            new BraceCompletionDeleteAction (),
            //new IndentAction (),
            new InstantRenameAction(),
            new LanguagesGenerateFoldPopupAction (),
            new org.netbeans.modules.languages.features.ToggleCommentAction()
        };
        return TextAction.augmentList (
            super.createActions (), 
            myActions
        );
    }
    
    public @Override Action getActionByName(String name) {
        if (name == null)
            return super.getActionByName (name);
        if (name.startsWith(LanguagesGenerateFoldPopupAction.EXPAND_PREFIX)) {
            name = name.substring(LanguagesGenerateFoldPopupAction.EXPAND_PREFIX.length(), name.length());
            return new ExpandFoldTypeAction (name);
        }
        if (name.startsWith(LanguagesGenerateFoldPopupAction.COLLAPSE_PREFIX)) {
            name = name.substring(LanguagesGenerateFoldPopupAction.COLLAPSE_PREFIX.length(), name.length());
            return new CollapseFoldTypeAction (name);
        }
        return super.getActionByName (name);
    }
    
    protected @Override EditorUI createEditorUI () {
        return new NbEditorUI () {
            private ToolTipSupport toolTipSupport;
            public @Override ToolTipSupport getToolTipSupport() {
                if (toolTipSupport == null) {
                    toolTipSupport = new ToolTipSupport (this) {
                        public @Override void setToolTipText (String text) {
                            if (text == null) return;
                            JLabel l = createToolTipComponent ();
                            l.setText (text);
                            setToolTip (l);
                        }
                    };
                }
                return toolTipSupport;
            }
        };
    }
    
    public @Override Document createDefaultDocument() {
        Document doc = new LanguagesDocument();
        initDocument (doc);
        return doc;
    }
    
    protected void initDocument (Document doc) {
        doc.putProperty("mimeType", mimeType); //NOI18N
        new AnnotationManager (doc);
        new DatabaseManager (doc);
    }
    
//    public Syntax createSyntax (Document doc) {
//        LanguagesSyntax syntax = (LanguagesSyntax) documentToSyntax.get (doc);
//        if (syntax == null) {
//            syntax = new LanguagesSyntax (doc);
//            documentToSyntax.put (doc, syntax);
//            syntax.init ();
//        }
//        return syntax;
//    }

// Not neccessary, PlainSyntax is delivered by default, braces matching is done
// through the new SPI
//    public Syntax createSyntax(Document doc) {
//        return new PlainSyntax();
//    }
//
//    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
//        return new BraceHighlighting (doc);
//    }
//    
    public @Override void install (JEditorPane c) {
        super.install (c);
        HyperlinkListener hl = new HyperlinkListener ();
        c.addMouseMotionListener (hl);
        c.addMouseListener (hl);
        c.addCaretListener (new MarkOccurrencesSupport (c));
        
        //HACK:
        c.getInputMap ().put (KeyStroke.getKeyStroke (KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "in-place-refactoring");
        c.getInputMap ().put (KeyStroke.getKeyStroke (KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK), "comment");
    }
    
    public @Override String getContentType() {
        return mimeType;
    }
    
    public @Override Object clone () {
        return new LanguagesEditorKit (mimeType);
    }

    private static final class LanguagesDocument extends NbEditorDocument {
        
        public LanguagesDocument() {
            super(LanguagesEditorKit.class);
        }

        public @Override boolean isIdentifierPart(char ch) {
            try {
                String mimeType = (String) getProperty("mimeType"); //NOI18N
                Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                Feature f = language.getFeature("SELECTION"); //NOI18N
                if (f != null) {
                    Pattern pat = f.getPattern();
                    if (pat != null) {
                        StringBuffer buf = new StringBuffer();
                        buf.append(ch);
                        return pat.matches(buf.toString());
                    }
                }
            } catch (LanguageDefinitionNotFoundException e) {
            }
            return super.isIdentifierPart(ch);
        }
    } // End of LanguagesDocument class
}

