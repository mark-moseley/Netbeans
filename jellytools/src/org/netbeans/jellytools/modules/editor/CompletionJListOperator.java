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

package org.netbeans.jellytools.modules.editor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionJList;


/**
 * Provides access to org.netbeans.modules.editor.completion.CompletionJlist
 * component.
 * Usage:
 * <pre>
 *      CompletionJlist comp = CompletionJlist.showCompletion();
 *      List list = comp.getCompletionItems();
 *      ...
 *      com.hideAll();
 * </pre>
 * @author Martin.Schovanek@sun.com
 */
public class CompletionJListOperator extends JListOperator {
    public static final String INSTANT_SUBSTITUTION = "InstantSubstitution";
    private static final Logger LOG = Logger.getLogger(CompletionJListOperator.class.getName());
    
    /**
     * This constructor is intended to use just for your own risk.
     * It could happen, that document is changed during invocation and
     * this costructor fails.
     */
    public CompletionJListOperator() {
        this(findCompletionJList());
    }
    
    private CompletionJListOperator(JList list) {
        super(list);
    }
    
    public List getCompletionItems() throws Exception {
        return getCompletionItems((JList) getSource());
    }
    
    private static List getCompletionItems(JList compJList)
            throws Exception {
        ListModel model = (ListModel) compJList.getModel();
        // dump items to List
        List<Object> data = new ArrayList<Object>(model.getSize());
        for (int i=0; i < model.getSize(); i++) {
            data.add(model.getElementAt(i));
        }
        return data;
    }
    
    private static JList findCompletionJList() {
        final String PLEASE_WAIT = Bundle.getStringTrimmed(
                "org.netbeans.modules.editor.completion.Bundle",
                "completion-please-wait");
        final Object result = waitFor(new Waitable() {
            public Object actionProduced(Object obj) {
                if (DocumentWatcher.isActive() && DocumentWatcher.isModified()) {
                    return INSTANT_SUBSTITUTION;
                }
                try {
                    // Path to the completion model:
                    // CompletionImpl.get().layout.completionPopup.getCompletionScrollPane()
                    // .view.getModel()
                    CompletionImpl comp = CompletionImpl.get();
                    //CompletionLayout.class
                    Field layoutField = CompletionImpl.class.getDeclaredField("layout");
                    layoutField.setAccessible(true);
                    Object layout = layoutField.get(comp);
                    //CompletionLayout.CompletionPopup.class
                    Field popupField = layout.getClass().getDeclaredField("completionPopup");
                    popupField.setAccessible(true);
                    Object popup = popupField.get(layout);
                    //CompletionScrollPane.class
                    Field csPaneField = popup.getClass().getDeclaredField("completionScrollPane");
                    csPaneField.setAccessible(true);
                    
                    Object compSPane = csPaneField.get(popup);
                    if(compSPane == null) {
                        return null;
                    }
                    
                    // check if all result providers finished
                    Field crField = comp.getClass().getDeclaredField("completionResult");
                    crField.setAccessible(true);
                    Object completionResult = crField.get(comp);
                    if (completionResult != null) {
                        Method grsMethod = completionResult.getClass().getDeclaredMethod("getResultSets");
                        grsMethod.setAccessible(true);
                        Object resultSets = grsMethod.invoke(completionResult, new Object[0]);
                        Method iarfMethod = comp.getClass().getDeclaredMethod("isAllResultsFinished", List.class);
                        iarfMethod.setAccessible(true);
                        Boolean allResultsFinished = (Boolean) iarfMethod.invoke(comp, resultSets);
                        if (!allResultsFinished) {
                            System.out.println(System.currentTimeMillis()+": all CC Results not finished yet.");
                            return null;
                        }
                    }
                    
                    Field viewField = compSPane.getClass().getDeclaredField("view");
                    viewField.setAccessible(true);
                    CompletionJList compJList = (CompletionJList) viewField.get(compSPane);
                    List list = getCompletionItems(compJList);
                    // check if it is no a 'Please Wait' item
                    if (list.size() > 0 && !(list.contains(PLEASE_WAIT))) {
                        LOG.fine(list.toString());
                        return compJList;
                    } else {
                        return null;
                    }
                } catch (Exception ex) {
                    throw new JemmyException("Exception when waiting for completion items.", ex);
                }
            }
            
            public String getDescription() {
                return "Wait for completion items data";
            }
        });
        
        if (result.equals(INSTANT_SUBSTITUTION)) {
            return null;
        }
        return (CompletionJList)result;
    }
    
    private static Object waitFor(Waitable action) {
        Waiter waiter = new Waiter(action);
        Timeouts waiterTimeouts = waiter.getTimeouts();
        waiterTimeouts.setTimeout("Waiter.TimeDelta", 500);
        waiterTimeouts.setTimeout("Waiter.AfterWaitingTime", 500);
        waiter.setTimeouts(waiterTimeouts);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription()+" has been " +
                    "interrupted.", ex);
        }
    }
    
    /** Returns a CompletionJListOperator or null in case of
     * instant substitution */
    public static CompletionJListOperator showCompletion() {
        CompletionJListOperator operator = null;
        
        DocumentWatcher.start();
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showCompletion();
            }
        };
        runInAWT(run);
        // wait CC
        JList list = findCompletionJList();
        if (list != null) {
            operator = new CompletionJListOperator(list);
        }
        DocumentWatcher.stop();
        return operator;
    }
    
    public static void showDocumentation() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showDocumentation();
            }
        };
        runInAWT(run);
    }
    
    public static void showToolTipPopup() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showToolTip();
            }
        };
        runInAWT(run);
    }
    
    public static void hideAll() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideAll();
            }
        };
        runInAWT(run);
    }
    
    public static void hideCompletion() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideCompletion();
            }
        };
        runInAWT(run);
    }
    
    public static void hideDocumentation() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideDocumentation();
            }
        };
        runInAWT(run);
    }
    
    public static void hideToolTipPopup() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideToolTip();
            }
        };
        runInAWT(run);
    }
    
    private static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try{
                SwingUtilities.invokeAndWait(r);
            }catch(Exception exc){
                throw new JemmyException("INVOKATION FAILED", exc);
            }
        }
    }
    
    static class DocumentWatcher {
        private static BaseDocument doc;
        private static boolean modified = false;
        private static boolean active = false;
        
        static DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                //setModified(true);
            }
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }
            public void removeUpdate(DocumentEvent e) {
                //setModified(true);
            }
        };
        
        public static void start() {
            doc = Registry.getMostActiveDocument();
            doc.addDocumentListener(listener);
            modified = false;
            active = true;
        }
        
        public static void stop() {
            if (doc != null) {
                doc.removeDocumentListener(listener);
                doc = null;
            }
            active = false;
        }
        
        public static boolean isModified() {
            if (!active) {
                throw new IllegalStateException("start() must be called before this.");
            }
            return modified;
        }
        
        public static boolean isActive() {
            return active;
        }

        private static void setModified(boolean b) {
            modified = b;
            if(doc!=null){
                doc.removeDocumentListener(listener);
                doc = null;
            }
        }
        
    }
}
