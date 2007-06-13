/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.editor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.dialogs.AddDialog;
import org.netbeans.modules.web.jsf.dialogs.AddManagedBeanDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationCaseDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationRuleDialog;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Petr Pisl
 */
public final class JSFPopupAction extends SystemAction implements Presenter.Popup {
    
    private ArrayList actions = null;
    static private String END_LINE = System.getProperty("line.separator");  //NOI18N
    protected final static int MANAGED_BEAN_TYPE = 1;
    protected final static int NAVIGATION_RULE_TYPE = 2;
    
    public String getName() {
        return NbBundle.getMessage(JSFPopupAction.class, "org-netbeans-modules-web-jsf-editor-JSFPopupAction.instance"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing - should never be called
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public javax.swing.JMenuItem getPopupPresenter() {
        return new SubMenu(getName());
    }
    
    public class SubMenu extends JMenu {
        
        public SubMenu(String s){
            super(s);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            pm.add(new AddNavigationRuleAction());
            pm.add(new AddNavigationCaseAction());
            pm.add(new JSeparator());
            pm.add(new AddManagedBeanAction());
            pm.pack();
            return pm;
        }
    }
    
    public static class AddManagedBeanAction extends BaseAction{
        public AddManagedBeanAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-managed-bean-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddManagedBeanDialog dialogPanel = new AddManagedBeanDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JSFPopupAction.class,"TTL_AddManagedBean"), //NOI18N
                    new HelpCtx(AddManagedBeanDialog.class));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try             {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true).getRootComponent();
                    ManagedBean bean = facesConfig.getModel().getFactory().createManagedBean();
                    
                    bean.setManagedBeanName(dialogPanel.getManagedBeanName());
                    bean.setManagedBeanClass(dialogPanel.getBeanClass());
                    bean.setManagedBeanScope(dialogPanel.getScope());
                    if (dialogPanel.getManagedBeanDescription() != null &&
                            dialogPanel.getManagedBeanDescription().trim().length() > 0){
                        Description description = facesConfig.getModel().getFactory().createDescription();
                        description.setValue(END_LINE + dialogPanel.getManagedBeanDescription() + END_LINE);
                        bean.addDescription(description);
                    }
                    facesConfig.getModel().startTransaction();
                    facesConfig.addManagedBean(bean);
                    facesConfig.getModel().endTransaction();
                    facesConfig.getModel().sync();
                    target.setCaretPosition(bean.findPosition());
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                            ex.getMessage(),
                            ex);
                }
            }
        }
    }
    
    public static class AddNavigationRuleAction extends BaseAction{
        public AddNavigationRuleAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-navigation-rule-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationRuleDialog dialogPanel = new AddNavigationRuleDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JSFPopupAction.class,"TTL_AddNavigationRule"), //NOI18N
                    new HelpCtx(AddNavigationRuleDialog.class));
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true);
                    FacesConfig facesConfig = model.getRootComponent();
                    NavigationRule rule = facesConfig.getModel().getFactory().createNavigationRule();
                    String descriptionText = dialogPanel.getDescription();
                    if (descriptionText != null && descriptionText.trim().length() > 0){
                        Description description = facesConfig.getModel().getFactory().createDescription();
                        description.setValue(END_LINE + descriptionText + END_LINE);
                        rule.addDescription(description);
                    }
                    if (dialogPanel.getFromView() != null && dialogPanel.getFromView().trim().length() > 0){
                        rule.setFromViewId(dialogPanel.getFromView());
                    }
                    facesConfig.getModel().startTransaction();
                    facesConfig.addNavigationRule(rule);
                    facesConfig.getModel().endTransaction();
                    facesConfig.getModel().sync();
                    target.setCaretPosition(rule.findPosition());
                } catch (java.io.IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public static class AddNavigationCaseAction extends BaseAction{
        public AddNavigationCaseAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-navigation-case-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationCaseDialog dialogPanel = new AddNavigationCaseDialog(data,
                    JSFEditorUtilities.getNavigationRule((BaseDocument)doc, target.getCaretPosition()));
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JSFPopupAction.class,"TTL_AddNavigationCase"),    //NOI18N
                    new HelpCtx(AddNavigationCaseDialog.class));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true).getRootComponent();
                    boolean newRule = false;
                    
                    String fromView = dialogPanel.getRule();
                    if (fromView.length() == 0){
                        fromView = null;
                    }
                    NavigationRule rule = JSFConfigUtilities.findNavigationRule(data, fromView);
                    if (rule == null){
                        rule = facesConfig.getModel().getFactory().createNavigationRule();
                        rule.setFromViewId(fromView);
                        facesConfig.getModel().startTransaction();
                        facesConfig.addNavigationRule(rule);
                        facesConfig.getModel().endTransaction();
                        newRule = true;
                    }
                    NavigationCase nCase = facesConfig.getModel().getFactory().createNavigationCase();
                    if(dialogPanel.getFromAction() != null && !dialogPanel.getFromAction().equals(""))      //NOI18N
                        nCase.setFromAction(dialogPanel.getFromAction());
                    if(dialogPanel.getFromOutcome() != null && !dialogPanel.getFromOutcome().equals(""))    //NOI18N
                        nCase.setFromOutcome(dialogPanel.getFromOutcome());
                    nCase.setRedirected(dialogPanel.isRedirect());
                    nCase.setToViewId(dialogPanel.getToView());
                    if(dialogPanel.getDescription() != null && !dialogPanel.getDescription().equals("")) {   //NOI18N
                        Description description = nCase.getModel().getFactory().createDescription();
                        description.setValue(END_LINE + dialogPanel.getDescription() + END_LINE);
                        nCase.addDescription(description);
                    }
                    facesConfig.getModel().startTransaction();
                    rule.addNavigationCase(nCase);
                    facesConfig.getModel().endTransaction();
                    facesConfig.getModel().sync();
                    
                    if (newRule)
                        target.setCaretPosition(rule.findPosition());    //NOI18N
                    else
                        target.setCaretPosition(nCase.findPosition());
                } catch (java.io.IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
