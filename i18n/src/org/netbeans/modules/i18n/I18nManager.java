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


package org.netbeans.modules.i18n;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.openide.cookies.EditorCookie;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;


/**
 * Manages performing of i18n action -&gt; i18n-zation of one source.
 *
 * @author   Peter Zavadsky
 */
public class I18nManager {
    
    /** Singleton instance of I18nManager. */
    private static I18nManager manager;

    /** Support for this internatioanlize session. */
    private I18nSupport support;

    /** Weak reference to i18n panel. */
    private WeakReference<I18nPanel> i18nPanelWRef = new WeakReference<I18nPanel>(null);
    
    /** Weak reference to top component in which internationalizing will be provided. */
    private WeakReference<Dialog> dialogWRef = new WeakReference<Dialog>(null);
    
    /** Weak reference to caret in editor pane. */
    private WeakReference<Caret> caretWRef;

    /** Found hard coded string. */
    private HardCodedString hcString;
    
    
    /** Private constructor. To ge instance use <code>getI18nMananger</code> method instead. */
    private I18nManager() {
    }

    
    /** Gets the only instance of I18nSupport. */
    public static synchronized I18nManager getDefault() {
        if (manager == null) {
            manager = new I18nManager();
        }
        return manager;
    }
    
    /** Get i18n support. */
    private void initSupport(DataObject sourceDataObject) throws IOException {
        I18nSupport.Factory factory = FactoryRegistry.getFactory(sourceDataObject.getClass());
        
        support = factory.create(sourceDataObject);
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize(final DataObject sourceDataObject) {

        // If there is i18n action working -> cancel it.
        closeDialog();

        // Initilialize support.
        try {
            initSupport(sourceDataObject);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return;
        }

        // initialize the component
        final EditorCookie ec = sourceDataObject.getCookie(EditorCookie.class);
        if (ec == null) {
            return;
        }

        // Add i18n panel to top component.
        getDialog(sourceDataObject);
        final I18nPanel i18nPanel = i18nPanelWRef.get();
        i18nPanel.showBundleMessage("TXT_SearchingForStrings");         //NOI18N
        i18nPanel.getCancelButton().requestFocusInWindow();
        
        final class SearchResultDisplayer implements Runnable {
            private final boolean success;
            SearchResultDisplayer(boolean success) {
                this.success = success;
            }
            public void run() {
                if (success) {
                    initCaret(ec);
                    highlightHCString();
                    fillDialogValues();                        
                    i18nPanel.getReplaceButton().requestFocusInWindow();
                } else {
                    i18nPanel.showBundleMessage("TXT_NoHardcodedString");//NOI18N
                    i18nPanel.getCancelButton().requestFocusInWindow();

                }
            }
        }

        // do the search on background
        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    boolean found = find();
                    EventQueue.invokeLater(new SearchResultDisplayer(found));
                }
            });


        
    }

    /** Initializes caret. */
    private void initCaret(EditorCookie ec) {
        JEditorPane[] panes = ec.getOpenedPanes();
        if (panes == null) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("MSG_CouldNotOpen"), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
            return;
        }

        // Keep only weak ref to caret, the strong one maintains editor pane itself.
        caretWRef = new WeakReference<Caret>(panes[0].getCaret());
    }
    
    /** Highlights found hasrdcoded string. */
    private void highlightHCString() {
        HardCodedString hStr = hcString;
        
        if (hStr == null) {
            return;
        }
        
        // Highlight found hard coded string.
        Caret caret = caretWRef.get();
        
        if (caret != null) {
            caret.setDot(hStr.getStartPosition().getOffset());
            caret.moveDot(hStr.getEndPosition().getOffset());
        }
    }

    /** Finds hard coded string. */
    private boolean find() {
        // Actual find on finder.
        hcString = support.getFinder().findNextHardCodedString();

        if (hcString != null) {
            return true;
        } 
        
        // not found in entire source document
        return false;
    }

    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        // It has to work this way, at this time the strong reference in top component have to exist.
        I18nPanel i18nPanel = i18nPanelWRef.get();

        i18nPanel.setI18nString(support.getDefaultI18nString(hcString));
        
        showDialog();
    }
    
    /** Replaces current found hard coded string and continue the search for next one. */
    private void replace() {
        I18nString i18nString = null;
        
        try {
            // To call weak without check have to be save here cause strong reference in the top component have to exist.
            i18nString = i18nPanelWRef.get().getI18nString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("EXC_BadKey"),       //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        // Try to add key to bundle.
        support.getResourceHolder().addProperty(i18nString.getKey(), i18nString.getValue(), i18nString.getComment());

        // Provide additional changes if they are available.
        if (support.hasAdditionalCustomizer()) {
            support.performAdditionalChanges();
        }
        
        // Replace hardcoded string.
        support.getReplacer().replace(hcString, i18nString);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                skip();
            }
        });
  }
    
    /** Skips foudn hard coded string and conitnue to search for next one. */
    private void skip() {
        if (find()) {
            highlightHCString();
            fillDialogValues();
        } else {
            i18nPanelWRef.get().showBundleMessage("TXT_NoMoreStrings"); //NOI18N
            i18nPanelWRef.get().getCancelButton().requestFocusInWindow();
        }
    }
    
    /** Shows info about found hard coded string. */
    private void showInfo() {
        JPanel infoPanel = support.getInfo(hcString);

        DialogDescriptor dd = new DialogDescriptor(
                infoPanel,
                I18nUtil.getBundle().getString("CTL_InfoPanelTitle"));  //NOI18N
        
        dd.setModal(true);
        dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
        dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION});
        dd.setAdditionalOptions(new Object[0]);

        
        Dialog infoDialog = DialogDisplayer.getDefault().createDialog(dd);
        infoDialog.setVisible(true);
    }
    
    /** Cancels current internationalizing session and re-layout top component to original layout. */
    public void cancel() {
        // No memory leaks.
        support = null;
        
        closeDialog();
    }
    
    /** Gets dialog. In our case it is a top component. 
     * @param name name of top component */
    private void getDialog(DataObject sourceDataObject) {
        Project project = Util.getProjectFor(sourceDataObject);

        Dialog dialog = dialogWRef.get();
        I18nPanel i18nPanel = i18nPanelWRef.get();

        // Dialog was not created yet or garbaged already.
        if (i18nPanel == null) {
            
            // Create i18n panel.
            i18nPanel = new I18nPanel(support.getPropertyPanel(),
                                      project,
                                      sourceDataObject.getPrimaryFile());

            // Helper final.
            final I18nPanel panel = i18nPanel;
            
            // Set button listeners.
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    final Object source = evt.getSource();
                    if (source == panel.getReplaceButton()) {
                        replace();
                    } else if (source == panel.getSkipButton()) {
                        skip();
                    } else if (source == panel.getInfoButton()) {
                        showInfo();
                    } else if (source == panel.getCancelButton()) {
                        cancel();
                    }
                }
            };
            
            i18nPanel.getReplaceButton().addActionListener(listener);
            i18nPanel.getSkipButton().addActionListener(listener);
            i18nPanel.getInfoButton().addActionListener(listener);
            i18nPanel.getCancelButton().addActionListener(listener);
            
            // Reset weak reference.
            i18nPanelWRef = new WeakReference<I18nPanel>(i18nPanel);

        } else {
//            i18nPanel.setProject(project);
            i18nPanel.setFile(sourceDataObject.getPrimaryFile());
        }

        // Set default i18n string.
        i18nPanel.setI18nString(support.getDefaultI18nString());
        i18nPanel.setDefaultResource(sourceDataObject);


        if (dialog == null) {
            String title = Util.getString("CTL_I18nDialogTitle"); // NOI18N
            DialogDescriptor dd = new DialogDescriptor(
                    i18nPanel,
                    title,
                    false,
                    new Object[] {},
                    null,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setLocation(80, 80);
            dialogWRef = new WeakReference<Dialog>(dialog);
        }

        dialog.setVisible(true); 
    }
    
    /** Shows dialog. In our case opens top component if it is necessary and
     * sets caret visible in editor part. */
    private void showDialog() {
        // Open dialog if available
        Dialog dialog = dialogWRef.get();
        if (dialog != null) {
            dialog.setVisible(true);
        }

        // Set caret visible.
        Caret caret = caretWRef.get();
        if (caret != null) { 
            if (!caret.isVisible()) {
                caret.setVisible(true);
            }
        }
    }
    
    /** Closes dialog. In our case removes <code>I18nPanel</code> from top component
     * and 'reconstruct it' to it's original layout. */
    private void closeDialog() {
        Dialog dialog = dialogWRef.get();
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

}
