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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class WSDLTreeViewMultiViewDesc extends Object
	implements MultiViewDescription, Serializable {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 2580263536201519563L;
    public static final String PREFERRED_ID = "wsdl-treeview";
    private WSDLDataObject wsdlDataObject;
    
	/**
	 *
	 *
	 */
	public WSDLTreeViewMultiViewDesc() {
		super();
	}


	/**
	 *
	 *
	 */
	public WSDLTreeViewMultiViewDesc(WSDLDataObject wsdlDataObject) {
		this.wsdlDataObject = wsdlDataObject;
	}


	/**
	 *
	 *
	 */
	public String preferredID() {
		return PREFERRED_ID;
	}


	/**
	 *
	 *
	 */
	public int getPersistenceType() {
		return TopComponent.PERSISTENCE_NEVER;
	}


	/**
	 *
	 *
	 */
	public java.awt.Image getIcon() {
		return Utilities.loadImage(WSDLDataObject.WSDL_ICON_BASE_WITH_EXT);
	}


        public HelpCtx getHelpCtx() {
            return new HelpCtx(WSDLTreeViewMultiViewDesc.class);
        }


	/**
	 *
	 *
	 */
	public String getDisplayName() {
		return NbBundle.getMessage(WSDLTreeViewMultiViewDesc.class,	
			"LBL_designView_name");
	}


	/**
	 *
	 *
	 */
	public MultiViewElement createElement() {
            return new WSDLTreeViewMultiViewElement(wsdlDataObject);
	}


	/**
	 *
	 *
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(wsdlDataObject);
	}


	/**
	 *
	 *
	 */
	public void readExternal(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		Object firstObject = in.readObject();
		if (firstObject instanceof WSDLDataObject)
			wsdlDataObject = (WSDLDataObject) firstObject;
	}
}
