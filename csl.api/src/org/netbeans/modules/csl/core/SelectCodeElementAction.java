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

package org.netbeans.modules.csl.core;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Code selection according to syntax tree.
 *
 * TODO: javadoc selection
 *
 * @author Miloslav Metelka, Jan Pokorsky
 */
public final class SelectCodeElementAction extends BaseAction {
    public static final String selectNextElementAction = "select-element-next"; //NOI18N
    public static final String selectPreviousElementAction = "select-element-previous"; //NOI18N

    private boolean selectNext;

    /**
     * Construct new action that selects next/previous code elements
     * according to the language model.
     * <br>
     *
     * @param name name of the action (should be one of
     *  <br>
     *  <code>JavaKit.selectNextElementAction</code>
     *  <code>JavaKit.selectPreviousElementAction</code>
     * @param selectNext <code>true</code> if the next element should be selected.
     *  <code>False</code> if the previous element should be selected.
     */
    public SelectCodeElementAction(String name, boolean selectNext) {
        super(name);
        this.selectNext = selectNext;
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }
        
    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getBundle(GsfEditorKitFactory.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            int selectionStartOffset = target.getSelectionStart();
            int selectionEndOffset = target.getSelectionEnd();
            if (selectionEndOffset > selectionStartOffset || selectNext) {
                SelectionHandler handler = (SelectionHandler)target.getClientProperty(SelectionHandler.class);
                if (handler == null) {
                    handler = new SelectionHandler(target);
                    target.addCaretListener(handler);
                    // No need to remove the listener above as the handler
                    // is stored is the client-property of the component itself
                    target.putClientProperty(SelectionHandler.class, handler);
                }
                
                if (selectNext) { // select next element
                    handler.selectNext();
                } else { // select previous
                    handler.selectPrevious();
                }
            }
        }
    }

    private static final class SelectionHandler extends UserTask implements CaretListener, Runnable {
        
        private JTextComponent target;
        private SelectionInfo[] selectionInfos;
        private int selIndex = -1;
        private boolean ignoreNextCaretUpdate;

        SelectionHandler(JTextComponent target) {
            this.target = target;
        }

        public void selectNext() {
            if (selectionInfos == null) {
                Source source = Source.create (target.getDocument());
                try {
                    ParserManager.parse (Collections.<Source> singleton (source), this);
                } catch (ParseException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            
            run();
        }

        public synchronized void selectPrevious() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(false);
            }
            if (selIndex > 0) {
                select(selectionInfos[--selIndex]);
            }
        }

        private void select(SelectionInfo selectionInfo) {
            Caret caret = target.getCaret();
            markIgnoreNextCaretUpdate();
            caret.setDot(selectionInfo.getStartOffset());
            markIgnoreNextCaretUpdate();
            caret.moveDot(selectionInfo.getEndOffset());
        }
        
        private void markIgnoreNextCaretUpdate() {
            ignoreNextCaretUpdate = true;
        }
        
        public void caretUpdate(CaretEvent e) {
            if (!ignoreNextCaretUpdate) {
                synchronized (this) {
                    selectionInfos = null;
                    selIndex = -1;
                }
            }
            ignoreNextCaretUpdate = false;
        }

        public void cancel() {
        }

        public void run (ResultIterator resultIterator) throws ParseException {
            ParserResult parserResult = (ParserResult) resultIterator.getParserResult (target.getCaretPosition ());
            selectionInfos = initSelectionPath(target, parserResult);
        }
        
        private KeystrokeHandler getBracketCompletion(Document doc, int offset) {
            BaseDocument baseDoc = (BaseDocument)doc;
            List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
            for (Language l : list) {
                if (l.getBracketCompletion() != null) {
                    return l.getBracketCompletion();
                }
            }

            return null;
        }
        
        private SelectionInfo[] initSelectionPath(JTextComponent target, ParserResult parserResult) {
            KeystrokeHandler bc = getBracketCompletion(target.getDocument(), target.getCaretPosition());
            if (bc != null) {
                List<OffsetRange> ranges = bc.findLogicalRanges(parserResult, target.getCaretPosition());
                SelectionInfo[] result = new SelectionInfo[ranges.size()];
                for (int i = 0; i < ranges.size(); i++) {
                    OffsetRange range = ranges.get(i);
                    result[i] = new SelectionInfo(range.getStart(), range.getEnd());
                }
                return result;
            } else {
                return new SelectionInfo[0];
            }
        }
        
        private int computeSelIndex(boolean inner) {
            Caret caret = target.getCaret();
            if (selectionInfos != null && caret != null && caret.getDot() != caret.getMark()) {
                int dot = caret.getDot();
                int mark = caret.getMark();
                int start = Math.min(dot,mark);
                //int end = Math.max(dot,mark);
                for (int i = 0; i < selectionInfos.length; i++) {
                    if (selectionInfos[i].getStartOffset() == start) {
                        // TODO - check end offset too
                        return i;
                    }
                }
                // No exact match - look at the editor selection and find the range
                // that most closely surround the selection (if inner is true, go
                // for the inner one, otherwise the outer)
                for (int i = selectionInfos.length-2; i >= 0; i--) {
                    if (selectionInfos[i].getStartOffset() > start &&
                            selectionInfos[i+1].getStartOffset() < start) {
                        return inner ? i : i-1;
                    }
                }
            }
            
            return selIndex;
        }

        public void run() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(true);
            }
            if (selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }
    
    // This looks a lot like an OffsetRange! Just reuse my own OffsetRange class?
    private static final class SelectionInfo {
        
        private int startOffset;
        private int endOffset;
        
        SelectionInfo(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public int getStartOffset() {
            return startOffset;
        }
        
        public int getEndOffset() {
            return endOffset;
        }
        
    }
}
