/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.*;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.cookies.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.DataEditorSupport;
import org.openide.windows.*;

import org.openide.NotifyDescriptor;

import javax.swing.text.StyledDocument;
import java.io.IOException;

/**
 * XmlMultiviewEditorSupport.java
 *
 * Created on October 5, 2004, 10:46 AM
 * @author  mkuchtiak
 */
public class XmlMultiViewEditorSupport extends DataEditorSupport 
    implements EditCookie, OpenCookie, EditorCookie.Observable, PrintCookie {
        
    private XmlMultiViewDataObject dObj;
    private XmlDocumentListener xmlDocListener;
    private int xmlMultiViewIndex;
    private TopComponent mvtc, xmlTC;
    private int lastOpenView=0;
    private StyledDocument document;

    /** Creates a new instance of XmlMultiviewEditorSupport */
    public XmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
        super (dObj, new XmlEnv (dObj));
        this.dObj=dObj;

        // Set a MIME type as needed, e.g.:
        setMIMEType ("text/xml");   // NOI18N
    }

    /** providing an UndoRedo object for XMLMultiViewElement
     */
    org.openide.awt.UndoRedo getUndoRedo0() {
        return super.getUndoRedo();
    }
    
    protected CloneableTopComponent createCloneableTopComponent() {
        MultiViewDescription[] customDesc = dObj.getMultiViewDesc();
        xmlTC = super.createCloneableTopComponent();
        MultiViewDescription xmlDesc = new XmlMultiViewEditorSupport.XmlViewDesc (xmlTC, dObj);
        MultiViewDescription[] descs = new MultiViewDescription[customDesc.length+1];
        for (int i=0;i<customDesc.length;i++) descs[i]=customDesc[i];
        descs[customDesc.length]=xmlDesc;
        xmlMultiViewIndex=customDesc.length;

        CloneableTopComponent mvtc = MultiViewFactory.createCloneableMultiView(descs, descs[0],new MyCloseHandler(dObj));

        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(org.openide.text.CloneableEditorSupport.EDITOR_MODE);

        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        this.mvtc=mvtc;
        return mvtc;
    }

    /** Focuses existing component to view, or if none exists creates new.
    * The default implementation simply calls {@link #open}.
    * @see org.openide.cookies.EditCookie#edit
    */
    public void edit () {
        if (java.awt.EventQueue.isDispatchThread()) {
            openInAWT(-1);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT(-1);
                }
            });
        }
    }

    /** Opens the specific View
     */
    public void openView(final int index) {
        if (java.awt.EventQueue.isDispatchThread()) {
            openInAWT(index);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT(index);
                }
            });
        }
    }
    
    /** Overrides superclass method
     */
    public void open() {
        if (java.awt.EventQueue.isDispatchThread()) {
            openInAWT(lastOpenView);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT(lastOpenView);
                }
            });
        }
    }
    
    private void openInAWT(int index) {
        dObj.documentUpdated();
        CloneableTopComponent mvtc = openCloneableTopComponent();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
        handler.requestVisible(handler.getPerspectives()[index<0?xmlMultiViewIndex:index]);
        mvtc.requestActive();
    }
    
    void goToXmlPerspective() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
                handler.requestVisible(handler.getPerspectives()[xmlMultiViewIndex]);
            }
        });
    }
    
    org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        if (mvtc!=null) return MultiViews.findMultiViewHandler(mvtc).getSelectedPerspective();
        return null;
    }
    
    /** A description of the binding between the editor support and the object.
     * Note this may be serialized as part of the window system and so
     * should be static, and use the transient modifier where needed.
     */
    private static class XmlEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1882981960507292985L;

        /** Create a new environment based on the data object.
         * @param obj the data object to edit
         */
        public XmlEnv (XmlMultiViewDataObject obj) {
            super (obj);
        }

        /** Get the file to edit.
         * @return the primary file normally
         */
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        /** Lock the file to edit.
         * Should be taken from the file entry if possible, helpful during
         * e.g. deletion of the file.
         * @return a lock on the primary file normally
         * @throws IOException if the lock could not be taken
         */
        protected FileLock takeLock () throws IOException {
            return ((XmlMultiViewDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        /** Find the editor support this environment represents.
         * Note that we have to look it up, as keeping a direct
         * reference would not permit this environment to be serialized.
         * @return the editor support
         */
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (XmlMultiViewEditorSupport) getDataObject ().getCookie (XmlMultiViewEditorSupport.class);
        }
    }
    
    private static class XmlViewDesc implements MultiViewDescription, java.io.Serializable  {
        
        private static final long serialVersionUID = 8085725367398466167L;
        TopComponent tc;
        XmlMultiViewDataObject dObj;
        
        XmlViewDesc() {
        }
        
        XmlViewDesc(TopComponent tc, XmlMultiViewDataObject dObj) {
            this.tc=tc;
            this.dObj=dObj;
        }

        public MultiViewElement createElement() {
            return new XmlMultiViewElement(tc, dObj);
        }
        
        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(XmlMultiViewEditorSupport.class,"LBL_XML_TAB");
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return null;
        }
        
        public java.awt.Image getIcon() {
            return dObj.getXmlViewIcon();
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
        
        public String preferredID() {
            return "multiview_xml"; //NOI18N
        }
    }
    
    TopComponent getMVTC() {
        return mvtc;
    }
    
    void setMVTC(TopComponent mvtc) {
        this.mvtc=mvtc;
    }
    
    TopComponent getXmlTopComponent() {
        if (xmlTC==null) xmlTC=super.createCloneableTopComponent();
        return xmlTC;
    }

    void setLastOpenView(int index) {
        lastOpenView=index;
    }
    
    void addListener() {
        if (xmlDocListener==null) {
            xmlDocListener = new XmlDocumentListener();
            try {
                document = openDocument();
                document.addDocumentListener(xmlDocListener);
            } catch (java.io.IOException ex){}
        }
    }
    
    void removeListener() {
        if (xmlDocListener != null) {
            document.removeDocumentListener(xmlDocListener);
            document = null;
            xmlDocListener = null;
        }
    }

    private class XmlDocumentListener implements javax.swing.event.DocumentListener {
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            doUpdate();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            doUpdate();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            doUpdate();
        }

        private void doUpdate() {
            dObj.documentUpdated();
        }
    }
    
    static class MyCloseHandler implements CloseOperationHandler, java.io.Serializable {
        static final long serialVersionUID = -6512103928294991474L;
        private XmlMultiViewDataObject dObj;
        MyCloseHandler() {     
        }
        
        MyCloseHandler(XmlMultiViewDataObject dObj) {
            this.dObj=dObj;
        }
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            for (int i = 0; i < elements.length; i++) {
                CloseOperationState element = elements[i];
                if (ToolBarDesignEditor.PROPERTY_FLUSH_DATA.equals(element.getCloseWarningID())) {
                    return false;
                }
            }
            if (dObj.isModified () || dObj.isChangedFromUI()) {
                XmlMultiViewEditorSupport support = dObj.getEditorSupport();
                String msg = support.messageSave();

                java.util.ResourceBundle bundle =
                        org.openide.util.NbBundle.getBundle(org.openide.text.CloneableEditorSupport.class);

                javax.swing.JButton saveOption = new javax.swing.JButton(bundle.getString("CTL_Save")); // NOI18N
                saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
                saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N
                javax.swing.JButton discardOption = new javax.swing.JButton(bundle.getString("CTL_Discard")); // NOI18N
                discardOption.getAccessibleContext()
                        .setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
                discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
                discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt(0)); // NOI18N

                NotifyDescriptor nd = new NotifyDescriptor(
                        msg,
                        bundle.getString("LBL_SaveFile_Title"),
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        new Object[]{saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION},
                        saveOption
                );

                Object ret = org.openide.DialogDisplayer.getDefault().notify(nd);

                if (NotifyDescriptor.CANCEL_OPTION.equals(ret)
                        || NotifyDescriptor.CLOSED_OPTION.equals(ret)
                ) {
                    return false;
                }

                if (saveOption.equals(ret)) {
                    try {
                        support.saveDocument();
                    } catch (java.io.IOException e) {
                        org.openide.ErrorManager.getDefault().notify(e);
                        return false;
                    }
                } else if (discardOption.equals(ret)) {
                    try {
                        dObj.reloadModelFromFileObject();
                        support.notifyClosed();
                    } catch (java.io.IOException e) {
                        org.openide.ErrorManager.getDefault().notify(e);
                        return false;
                    }
                }
            }
            return true;
        }
    }

    // Accessibility for ToolBarMultiViewElement:  
    protected String messageName() {
        return super.messageName();
    }
}