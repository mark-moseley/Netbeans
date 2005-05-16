/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.core.spi.multiview.*;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/**
 * @author mkuchtiak
 */
public class PagesMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private PagesPanelFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String PAGES_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_PAGES;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_PAGES;
    
    /** Creates a new instance of DDMultiViewElement */
    public PagesMultiViewElement(final DDDataObject dObj, int index) {
        super(dObj);
        this.dObj=dObj;
        this.index=index;
        comp = new ToolBarDesignEditor();
        factory = new PagesPanelFactory(comp, dObj);
        addAction = new AddAction(dObj, NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG"));
        removeAction = new RemoveAction(NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove"));
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    
    public SectionView getSectionView() {
        return view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(index);
        if (needInit || !dObj.isDocumentParseable()) {
            repaintView();
            needInit = false;
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view =new PagesView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ((SectionView)view).openPanel("welcome_files"); //NOI18N
        }
        view.checkValidity();
        dObj.checkParseable();
    }
    
    public void componentOpened() {
        super.componentOpened();
        dObj.getWebApp().addPropertyChangeListener(this);
    }
    
    public void componentClosed() {
        super.componentClosed();
        dObj.getWebApp().removePropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("WelcomeFileList")>0 || //NOI18N
                 name.indexOf("JspConfig")>0 || //NOI18N
                 name.indexOf("ErrorPage")>0 || //NOI18N
                 name.indexOf("version")>0 ) { //NOI18N
                // repaint view if the wiew is active and something is changed with elements listed above
                if (PAGES_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    class PagesView extends SectionView {
        private SectionContainer jspPGCont;
        private Node groupsNode, welcomeFilesNode, errorPagesNode;
        
        PagesView(WebApp webApp) {
            super(factory);
            JspConfig jspConfig=null;
            JspPropertyGroup[] groups=null;
            boolean jspConfigSupported=true;
            try {
                jspConfig = webApp.getSingleJspConfig();
                
            } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                jspConfigSupported=false;
            }
            
            welcomeFilesNode = new WelcomeFilesNode();
            addSection(new SectionPanel(this,welcomeFilesNode,"welcome_files")); //NOI18N

            errorPagesNode = new ErrorPagesNode();
            addSection(new SectionPanel(this,errorPagesNode,"error_pages")); //NOI18N
            
            Children rootChildren = new Children.Array();
            
            if (jspConfigSupported) {
                if (jspConfig==null) {
                    groups = new JspPropertyGroup[0];
                } else groups = jspConfig.getJspPropertyGroup();
                Node[] nodes = new Node[groups.length];
                Children ch = new Children.Array();
                for (int i=0;i<nodes.length;i++) {
                    nodes[i] = new JspPGNode(this,groups[i]);
                }
                ch.add(nodes);
                // creatings JSP Groups node, section container for JSP groups
                groupsNode = new SectionContainerNode(ch);
                groupsNode.setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_jspPropGroups"));
                groupsNode.setName(HELP_ID_PREFIX+"jspPropertyGroupsNode"); //NOI18N
                jspPGCont = new SectionContainer(this,groupsNode,
                    NbBundle.getMessage(PagesMultiViewElement.class,"TTL_jspPropGroups"));
                jspPGCont.setHeaderActions(new javax.swing.Action[]{addAction});

                // creatings section panels for JSP groups
                SectionPanel[] pan = new SectionPanel[groups.length];
                for (int i=0;i<nodes.length;i++) {
                    pan[i] = new SectionPanel(this, nodes[i], getJspGroupTitle(groups[i]),groups[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                    jspPGCont.addSection(pan[i]);
                }
                addSection(jspPGCont);
                rootChildren.add(new Node[]{welcomeFilesNode,errorPagesNode,groupsNode}); 
            } else {
                addAction.setEnabled(false);
                rootChildren.add(new Node[]{welcomeFilesNode,errorPagesNode});
            }
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }
        
        Node getJspPGsNode() {
            return groupsNode;
        }
        
        Node getWelcomeFilesNode() {
            return welcomeFilesNode;
        }
        
        SectionContainer getJspGroupsContainer(){
            return jspPGCont;
        }
        
        String getJspGroupTitle(JspPropertyGroup jspGroup) {
            String name=jspGroup.getDefaultDisplayName();
            if (name==null) name = NbBundle.getMessage(PagesMultiViewElement.class,"NODE_JSP_GROUP");
                
            String[] patterns = jspGroup.getUrlPattern();
            StringBuffer buf = new StringBuffer();
            for (int i=0;i<patterns.length;i++) {
                if (i>0) buf.append(", ");
                buf.append(patterns[i]);
            }
            return NbBundle.getMessage(PagesMultiViewElement.class,"TTL_JSP_GROUP", name, buf.toString());
        }
    
        String getJspGroupNodeName(JspPropertyGroup jspGroup) {
            String displayName=jspGroup.getDefaultDisplayName();
            if (displayName!=null) return displayName;
            else return NbBundle.getMessage(PagesMultiViewElement.class,"NODE_JSP_GROUP");
        }
        
        public Error validateView() {
            return SectionValidator.validatePages(webApp);
        }
        
    }
    
    private class JspPGNode extends org.openide.nodes.AbstractNode {
        private JspPropertyGroup group;
        JspPGNode(PagesView view, JspPropertyGroup group) {
            super(org.openide.nodes.Children.LEAF);
            this.group=group;
            setDisplayName(view.getJspGroupNodeName(group));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/jspObject"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"jspPropertyGroupNode"); //NOI18N
        }
    }
    
    private class WelcomeFilesNode extends org.openide.nodes.AbstractNode {
        WelcomeFilesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_welcomeFiles"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/htmlObject"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"welcomeFilesNode"); //NOI18N
        }
    }
    
    private class ErrorPagesNode extends org.openide.nodes.AbstractNode {
        ErrorPagesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_errorPages"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/htmlObject"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"errorPagesNode"); //NOI18N
        }
    }

    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(final DDDataObject dObj, String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            String[] labels = new String[]{
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_displayName"),
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_description"),
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_urlPatterns")
            };
            char[] mnem = new char[] {
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_displayName_mnem").charAt(0),
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_description_mnem").charAt(0),
                NbBundle.getMessage(PagesMultiViewElement.class,"LBL_urlPatterns_mnem").charAt(0)
            };
            boolean[] buttons = new boolean[]{false,false,true};
            SimpleDialogPanel.DialogDescriptor descriptor = new SimpleDialogPanel.DialogDescriptor(labels);
            descriptor.setMnemonics(mnem);
            descriptor.setButtons(buttons);
            descriptor.setTextField(new boolean[]{true,false,true});
            
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true); //NOI18N
                            String oldValue = dialogPanel.getValues()[2];
                            if (fileName.length()>0) {
                                String newValue = DDUtils.addItem(oldValue,fileName,false);
                                if (!oldValue.equals(newValue)) {
                                    dialogPanel.getTextComponents()[2].setText(newValue);
                                }
                            }
                        }
                    } catch (java.io.IOException ex) {}
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(PagesMultiViewElement.class,"TTL_JspPropertyGroup"),true) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String urlPatterns = values[2].trim();
                    return SectionValidator.validateNewJspPropertyGroup(urlPatterns);
                }
            };
            dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[2].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getTextComponents()[2].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();;
                String[] values = dialogPanel.getValues();
                String name = values[0];
                String description = values[1];
                String urls = values[2].trim();
                try {
                    JspConfig jspConfig=null;
                    try {
                        jspConfig = webApp.getSingleJspConfig();
                        if (jspConfig==null) {
                            jspConfig = (JspConfig)webApp.createBean("JspConfig"); //NOI18N
                            webApp.setJspConfig(jspConfig);
                        }
                    } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {}
                    JspPropertyGroup group = (JspPropertyGroup)jspConfig.createBean("JspPropertyGroup"); //NOI18N
                    if (name.length()>0) group.setDisplayName((String)name);
                    if (description.length()>0) group.setDescription((String)description);
                    
                    String[] patterns = DDUtils.getStringArray(urls);
                    group.setUrlPattern(patterns);
                    jspConfig.addJspPropertyGroup(group);

                    PagesView view = (PagesView)comp.getContentView();
                    Node node = new JspPGNode(view, group);
                    view.getJspPGsNode().getChildren().add(new Node[]{node});

                    SectionPanel pan = new SectionPanel(view, node, view.getJspGroupTitle(group),group);
                    pan.setHeaderActions(new javax.swing.Action[]{removeAction});
                    view.getJspGroupsContainer().addSection(pan, true);
                } catch (ClassNotFoundException ex){}
            }
        }
    }
    
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            org.openide.DialogDescriptor desc = new ConfirmDialog(
                NbBundle.getMessage(PagesMultiViewElement.class,"TXT_removeJspGroupConfirm"));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.show();
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
                JspPropertyGroup group = (JspPropertyGroup)sectionPanel.getKey();
                try {
                    // removing jsp-property-group from data model
                    dObj.modelUpdatedFromUI();
                    webApp.getSingleJspConfig().removeJspPropertyGroup(group);
                    
                    // removing section
                    sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {}
            }
        }
    }
}
