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

package org.netbeans.modules.websvc.design.multiview;

import java.beans.BeanInfo;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * The source editor desc for JaxWs node.
 * @author Ajit Bhate
 */
public class SourceMultiViewDesc extends Object implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -4505309173196320880L;
    /**
     *
     */
    public static final String PREFERRED_ID = "webservice-sourceview";
    private DataObject dataObject;

    /**
     *
     */
    public SourceMultiViewDesc() {
    }

    /**
     * Creates a new instance of SchemaSourceMultiviewDesc
     * @param dataObject
     */
    public SourceMultiViewDesc(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SourceMultiViewDesc.class, "LBL_sourceView_name");
    }

    public MultiViewElement createElement() {
        DataEditorSupport support = dataObject.getLookup().lookup(DataEditorSupport.class);
        if(support==null) return MultiViewFactory.BLANK_ELEMENT;
        return new SourceMultiViewElement(support);
    }

    /**
     *
     * @param out
     * @throws java.io.IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dataObject);
    }

    /**
     *
     * @param in
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if (firstObject instanceof DataObject) {
            dataObject = (DataObject) firstObject;
        }
    }
}
