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

import org.netbeans.core.spi.multiview.*;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionView;

import java.beans.PropertyVetoException;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public abstract class ToolBarMultiViewElement implements MultiViewElement {
    MultiViewElementCallback observer;
    private ToolBarDesignEditor editor;
    private XmlMultiViewDataObject dObj;

    public ToolBarMultiViewElement(XmlMultiViewDataObject dObj, ToolBarDesignEditor editor) {
        this(dObj);
        this.editor = editor;
    }
    
    public ToolBarMultiViewElement(final XmlMultiViewDataObject dObj) {
        this.dObj=dObj;
    }

    protected void setVisualEditor(ToolBarDesignEditor editor) {
        this.editor=editor;
    }
    
    public CloseOperationState canCloseElement() {
        try {
            editor.fireVetoableChange(ToolBarDesignEditor.PROPERTY_FLUSH_DATA, this, null);
        } catch (PropertyVetoException e) {
            return MultiViewFactory.createUnsafeCloseState(ToolBarDesignEditor.PROPERTY_FLUSH_DATA, null, null);
        }
        if (!dObj.canClose()) {
            return MultiViewFactory.createUnsafeCloseState(NbBundle.getMessage(ToolBarMultiViewElement.class,
                    "LBL_DataObjectModified"), null, null);
        } else {
            return CloseOperationState.STATE_OK;
        }
    }
    
    public void componentActivated() {
       editor.componentActivated();
    }
    
    public void componentClosed() {
        editor.componentClosed();
    }
    
    public void componentDeactivated() {
        editor.componentDeactivated();
    }
    
    public void componentHidden() {
        editor.componentHidden();
    }
    
    public void componentOpened() {
        editor.componentOpened();
    }
    
    public void componentShowing() {
        editor.componentShowing();
    }
    
    public javax.swing.Action[] getActions() {
        return dObj.getEditorSupport().getXmlTopComponent().getActions();
    }
    
    public org.openide.util.Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[] {
            dObj.getNodeDelegate().getLookup()
        });
    }
    
    public javax.swing.JComponent getToolbarRepresentation() {
        return editor.getStructureView();
    }
    
    public org.openide.awt.UndoRedo getUndoRedo() {
        return null;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        observer=callback;
        if (dObj!=null) {
            dObj.setActiveMultiViewElement(this);
            XmlMultiViewEditorSupport support = dObj.getEditorSupport();
            if (support!=null) {
                if (support.getMVTC()==null) {
                    support.setMVTC(callback.getTopComponent());
                }
                support.updateDisplayName();
            }
        }
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return editor;
    }
    /** Enable to get the SectionView included in this MultiView Element
     */ 
    public abstract SectionView getSectionView();

}
