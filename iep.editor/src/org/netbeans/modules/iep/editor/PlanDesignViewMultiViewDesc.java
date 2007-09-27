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

package org.netbeans.modules.iep.editor;

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
public class PlanDesignViewMultiViewDesc extends Object
	implements MultiViewDescription, Serializable {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 2580263536201519563L;
    public static final String PREFERRED_ID = "wsdl-treeview";
    private PlanDataObject wsdlDataObject;
    
	/**
	 *
	 *
	 */
	public PlanDesignViewMultiViewDesc() {
		super();
	}


	/**
	 *
	 *
	 */
	public PlanDesignViewMultiViewDesc(PlanDataObject wsdlDataObject) {
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
		return Utilities.loadImage(PlanDataObject.IEP_ICON_BASE_WITH_EXT);
	}


        public HelpCtx getHelpCtx() {
            return new HelpCtx(PlanDesignViewMultiViewDesc.class);
        }


	/**
	 *
	 *
	 */
	public String getDisplayName() {
		return NbBundle.getMessage(PlanDesignViewMultiViewDesc.class,	
			"LBL_designView_name");
	}


	/**
	 *
	 *
	 */
	public MultiViewElement createElement() {
            return new PlanDesignViewMultiViewElement(wsdlDataObject);
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
		if (firstObject instanceof PlanDataObject)
			wsdlDataObject = (PlanDataObject) firstObject;
	}
}
