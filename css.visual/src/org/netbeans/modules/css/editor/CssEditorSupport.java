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
package org.netbeans.modules.css.editor;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.editor.model.CssModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.netbeans.modules.css.editor.model.CssRule;
import org.netbeans.modules.css.editor.model.CssRuleItem;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.api.StyleBuilderTopComponent;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewTopComponent;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable.Listener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;

/**
 * Editor Support for document of type text/css
 *
 * @author Winston Prakash
 * @author Marek Fukala
 *
 * @version 1.0
 */
public class CssEditorSupport {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private CssRule selected = null;
    private List<CssPreviewable.Listener> previewableListeners = new ArrayList<CssPreviewable.Listener>();
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.Utilities.VISUAL_EDITOR_LOGGER);
    private static final int RULE_UPDATE_DELAY = 100; //ms
    private final PaneAwareRunnable RULE_UPDATE = new PaneAwareRunnable();
    private final Task RULE_UPDATE_TASK = RequestProcessor.getDefault().create(RULE_UPDATE);
    private static CssEditorSupport INSTANCE;
    private TopComponent current;
    private JEditorPane editorPane;
    private Document document;
    private FileObject fileObject;
    private CssModel model;
    private boolean caretListenerRegistered = false;

    private static final boolean DEBUG = Boolean.getBoolean("issue_129209_debug");
    
    
    public static synchronized CssEditorSupport getDefault() {
        if (INSTANCE == null) {
            //INSTANCE = new WeakReference<CSSTCController>(new CSSTCController());
            INSTANCE = new CssEditorSupport();
        }
        return INSTANCE;
    }
    private PropertyChangeListener CSS_STYLE_DATA_LISTENER = new PropertyChangeListener() {

        public void propertyChange(final PropertyChangeEvent evt) {
            //detach myself from the source so next UI changes are not propagated to the 
            //document until the parser finishes. Then new listener will be added
            if(selected != null) {
                d("css style data listener - detachinf from rule content.");
                selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            }
            
            //remove caret listener, new one will be added one the written test is parsed
            if (caretListenerRegistered) {
                editorPane.removeCaretListener(CARET_LISTENER);
                d("removed caret listener");
                caretListenerRegistered = false;
            }
            
            final NbEditorDocument doc = (NbEditorDocument) document;
            if (doc != null) {
                doc.runAtomic(new Runnable() {

                    public void run() {
                        CssRuleItem oldRule = (CssRuleItem) evt.getOldValue();
                        CssRuleItem newRule = (CssRuleItem) evt.getNewValue();

                        if (selected == null) {
                            throw new IllegalStateException("CssRuleContent event fired, but selected rule is null!");
                        }

                        //remember the selected rule since it synchronously
                        //turns to null after each document modification
                        CssRule myRule = selected;

                        try {
                            if (oldRule != null && newRule == null) {
                                //remove the old rule line - maybe we should just cut the exact part?!?!
                                int start = oldRule.key().offset();
                                int end = oldRule.value().offset() + oldRule.value().name().length();
                                
                                //cut off also the semicolon if there is any
                                end = oldRule.semicolonOffset() != -1 ? oldRule.semicolonOffset() + 1 : end; 
                                
                                doc.remove(start, end - start);
                                
                                //check if the line is empty and possibly remove it
                                if(Utilities.isRowWhite(doc, start)) {
                                    int lineStart = Utilities.getRowStart(doc, start);
                                    int lineOffset = Utilities.getLineOffset(doc, start);
                                    int nextLineStart = Utilities.getRowStartFromLineOffset(doc, lineOffset + 1);
                                    
                                    doc.remove(lineStart, nextLineStart - lineStart);
                                }

                            } else if (oldRule == null && newRule != null) {
                                //add the new rule at the end of the rule block:
                                List<CssRuleItem> items = myRule.ruleContent().ruleItems();
                                final int INDENT = doc.getFormatter().getShiftWidth();

                                boolean initialNewLine = false;
                                if (!items.isEmpty()) {
                                    //find latest rule and add the item behind
                                    CssRuleItem last = items.get(items.size() - 1);

                                    //check if the last item has semicolon
                                    //add it if there is no semicolon
                                    if (last.semicolonOffset() == -1) {
                                        doc.insertString(last.value().offset() + last.value().name().trim().length(), ";", null); //NOI18N
                                    }

                                    initialNewLine = Utilities.getLineOffset(doc, myRule.getRuleCloseBracketOffset()) == Utilities.getLineOffset(doc, last.key().offset());
                                } else {
                                    initialNewLine = Utilities.getLineOffset(doc, myRule.getRuleCloseBracketOffset()) == Utilities.getLineOffset(doc, myRule.getRuleOpenBracketOffset());
                                }

                                int insertOffset = myRule.getRuleCloseBracketOffset();
                                String text = (initialNewLine ? LINE_SEPARATOR : "") +
                                        makeIndentString(INDENT) +
                                        newRule.key().name() + ": " + newRule.value().name() + ";" +
                                        LINE_SEPARATOR;

                                doc.insertString(insertOffset, text, null);

                            } else if (oldRule != null && newRule != null) {
                                //update the existing rule in document
                                //replace attribute name
                                doc.remove(oldRule.key().offset(), oldRule.key().name().length());
                                doc.insertString(oldRule.key().offset(), newRule.key().name(), null);
                                //replace the attribute value
                                int diff = newRule.key().name().length() - oldRule.key().name().length();
                                doc.remove(oldRule.value().offset() + diff, oldRule.value().name().length());
                                doc.insertString(oldRule.value().offset() + diff, newRule.value().name(), null);

                            } else {
                                //new rule and old rule is null
                                throw new IllegalArgumentException("Invalid PropertyChangeEvent - both old and new values are null!");
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };
    private final PropertyChangeListener MODEL_LISTENER = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(CssModel.MODEL_UPDATED)) {
                d("model updated");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (editorPane == null) {
                            //it may happen that the TC gets deactivated after this Runnable
                            //being posted and the model listener was unregistered.
                            return;
                        }
                        d("model updated from AWT");
                        updateSelectedRule(editorPane.getCaret().getDot());
                        if (!caretListenerRegistered) {
                            editorPane.addCaretListener(CARET_LISTENER);
                            d("added caret listener");
                            caretListenerRegistered = true;
                        }
                    }
                });
            } else {
                //either MODEL_INVALID or MODEL_PARSING fired
                final boolean invalid = evt.getPropertyName().equals(CssModel.MODEL_INVALID);
                d("model invalid");
                //disable editing on the StyleBuilder
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        //remove the CssStyleData listener to disallow StyleBuilder editing
                        //until the parser finishes parsing. If I do not do that, the parsed
                        //data from the CssModel are inaccurate and hence,
                        //when user uses StyleBuilder, the source may become broken.
                        if (selected != null) {
                            selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                            d("removed css style data listener from " + selected);
                            selected = null;
                        }
                        if (caretListenerRegistered) {
                            editorPane.removeCaretListener(CARET_LISTENER);
                            d("removed caret listener");
                            caretListenerRegistered = false;
                        }
                        if (invalid) {
                            //model invalid - switch the stylebuilder UI to an error panel
                            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.MODEL_ERROR);
                            firePreviewableDeactivated();
                        } else {
                            //model is about the be updated - just disable the SB editing
                            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.MODEL_UPDATING);
                        }
                    }
                });
            }
        }
    };

    private String makeIndentString(int level) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < level; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
    private CaretListener CARET_LISTENER = new CaretListener() {

        public void caretUpdate(CaretEvent ce) {
            Object source = ce.getSource();
            if (source instanceof JEditorPane) {
                if(!caretListenerRegistered) {
                    return ;
                }
                d("caret event; dot=" + ce.getDot());
                RULE_UPDATE.setPane(((JEditorPane) source));
                RULE_UPDATE_TASK.schedule(RULE_UPDATE_DELAY);
            }
        }
    };

    //always called fro AWT, no need to explicit synch with cssTCDeactivated
    public void cssTCActivated(final TopComponent tc) {
        d("activated: " + tc.getName());
        
        if (current != null) {
            if (current == tc) {
                return;
            } else {
                //deactivate the old editor
                cssTCDeactivated();
            }
        }

        this.current = tc;
        this.editorPane = getEditorPane(tc);

        if (editorPane == null) {
            return;
        }
        this.document = editorPane.getDocument();
        if (document == null) {
            return;
        }

        this.fileObject = tc.getLookup().lookup(FileObject.class);
        this.model = CssModel.get(document);

        //select the first rule if the caret in on zero offset
        //once the model is updated/created the caret is set and this listener unregistered
        model.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(CssModel.MODEL_UPDATED)) {
                    model.removePropertyChangeListener(this);
                    if (editorPane.getCaret().getDot() == 0) {
                        if (model.rules().size() > 0) {
                            d("setting caret to first rule: " + tc.getName());
                            editorPane.getCaret().setDot(model.rules().get(0).getRuleNameOffset());
                        }
                    }
                }
            }
        });

        if (!caretListenerRegistered) {
            d("added caret listener: " + tc.getName());
            editorPane.addCaretListener(CARET_LISTENER);
        }

        //attach css model listener
        model.addPropertyChangeListener(MODEL_LISTENER);
        d("added model listener: " + tc.getName());

        //we need to refresh the StyleBuilder content when switching between more css files
        if (selected != null) {
            d("removed css styledatalistener from old " + selected + ": " + tc.getName());
            selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            selected = null;
        }
        
        updateSelectedRule(editorPane.getCaret().getDot());
        
        d("activated exit: " + tc.getName());
    }

    public void cssTCDeactivated() {
        d("deactivated: " + current);
        
        //cancel scheduled rule update task if scheduled
        RULE_UPDATE_TASK.cancel();
        d("rule update task cancelled: " + current);

        if(model != null) { //null may happen if source broken
            this.model.removePropertyChangeListener(MODEL_LISTENER);
            d("removed model listener: " + current);
            this.model = null;
        }

        if (selected != null) {
            selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            d("removed css style data listener: " + current);
            selected = null;
        }
        this.current = null;

        if (caretListenerRegistered) {
            if(editorPane != null) {
                editorPane.removeCaretListener(CARET_LISTENER);
                d("removed caret listener: " + current);
            }
            caretListenerRegistered = false;
        }

        this.editorPane = null;
        this.fileObject = null;
        d("deactivated exit: " + current);
    }

    private JEditorPane getEditorPane(TopComponent tc) {
        EditorCookie ec = tc.getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return null;
    }

    private synchronized void updateSelectedRule(int dotPos) {
        if (document == null || model == null || current == null) {
            //document unloaded, just return
            d("document = " + document + "; model = " + model + "; current TC = " + current + " => exiting");
            return;
        }
        d("update selected rule " + current.getName() + " to position " + dotPos);

        LOGGER.log(Level.FINE, "updateSelectedRule(" + dotPos + ")");
        if (model.rules() == null) {
            return;//css not parsed yet, we need to wait for a parser event
        }

        //find rule on the offset
        final CssRule selectedRule = model.ruleForOffset(dotPos);

        LOGGER.log(Level.FINE, selectedRule == null ? "NO rule" : "found a rule");

        d("selected rule:" + selectedRule);
        
        if (selectedRule == null) {
            //remove the listeners from selected
            if (selected != null) {
                selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                d("no selected rule, removing css style data listener");
                //reset saved selected rule
                selected = null;
            }
            //show no selected rule panel
            StyleBuilderTopComponent.findInstance().setPanelMode(StyleBuilderTopComponent.OUT_OF_RULE);

            //disable preview
            firePreviewableDeactivated();
        } else {
            //something was selected

            if (selectedRule.equals(selected)) {
                d("already selected rule selected, exiting");
                return; //trying to select already selected rule, ignore
            }

            //remove listener from the old rule
            if (selected != null) {
                selected.ruleContent().removePropertyChangeListener(CSS_STYLE_DATA_LISTENER);
                d("removed css style data listener from previous rule: " + selected);
            }
            selected = selectedRule;

            //listen on changes possibly made by the stylebuilder and update the document accordingly
            selectedRule.ruleContent().addPropertyChangeListener(CSS_STYLE_DATA_LISTENER);
            d("added property change listener to the new rule: " + selected);
            
            //TODO make activation of the selected rule consistent for StyleBuilder and CSSPreview,
            //now one uses direct call to TC, second property change listening on this class

            //update the css preview
            CssRuleContext content =
                    new CssRuleContext(selectedRule, 
                    model, 
                    document, 
                    FileUtil.toFile(fileObject.getParent()));

            //activate the selected rule in stylebuilder
            StyleBuilderTopComponent sbTC = StyleBuilderTopComponent.findInstance();
            sbTC.setContent(content);
            sbTC.setPanelMode(StyleBuilderTopComponent.MODEL_OK);
            d("stylebuilder UI updated");
            
            firePreviewableActivated(content);
        }
        d("updateselected rule exit");
    }

    /** CssPreviewable implementation */
    public void addListener(Listener l) {
        previewableListeners.add(l);
    }

    public void removeListener(Listener l) {
        previewableListeners.remove(l);
    }

    private void firePreviewableActivated(CssRuleContext content) {
        CssPreviewTopComponent.findInstance().activate(content);
    }

    private void firePreviewableDeactivated() {
        CssPreviewTopComponent.findInstance().deactivate();
    }

    private void d(String s) {
        if(DEBUG) { //should be if(DEBUG) { d("") } but will be commented out later
            LOGGER.log(Level.INFO, s);
        }
    }
    
    private class PaneAwareRunnable implements Runnable {

        private JEditorPane editor = null;

        public void setPane(JEditorPane component) {
            this.editor = component;
        }

        public void run() {
            if (editor != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        updateSelectedRule(editor.getCaret().getDot());
                    }
                });
            }
        }
    }
}
