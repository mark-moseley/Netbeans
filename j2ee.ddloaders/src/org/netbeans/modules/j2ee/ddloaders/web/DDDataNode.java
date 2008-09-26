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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ddloaders.web;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import java.beans.*;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/** A node to represent this object.
 *
 * @author  mkuchtiak
 * @version 1.0
 */
public class DDDataNode extends DataNode {

    private DDDataObject dataObject;

    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N

    /** Listener on dataobject */
    private PropertyChangeListener ddListener;
    
    public DDDataNode (DDDataObject obj) {
        this (obj, Children.LEAF);
    }

    public DDDataNode (DDDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        initListeners();
    }
    
    private static final java.awt.Image ERROR_BADGE = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/error-badge.gif" ); //NOI18N
    private static final java.awt.Image WEB_XML = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        if (dataObject.getSaxError()==null)
            return WEB_XML;
        else 
            return ImageUtilities.mergeImages(WEB_XML, ERROR_BADGE, 6, 6);
    }
    
    public String getShortDescription() {
        org.xml.sax.SAXException saxError = dataObject.getSaxError();
        if (saxError==null) {
            return NbBundle.getBundle(DDDataNode.class).getString("HINT_web_dd");
        } else {
            return saxError.getMessage();
        }
    }

    void iconChanged() {
        fireIconChange();
    }
  
    /** Initialize listening on adding/removing server so it is 
     * possible to add/remove property sheets
     */
    private void initListeners(){
        ddListener = new PropertyChangeListener () {
            
            public void propertyChange (PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName ();
                Object newValue = evt.getNewValue ();
                Object oldValue = evt.getOldValue ();
                if (DDDataObject.PROP_DOCUMENT_DTD.equals (propertyName)) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                }
                if (DataObject.PROP_VALID.equals (propertyName)
                &&  Boolean.TRUE.equals (newValue)) {
                    removePropertyChangeListener (DDDataNode.this.ddListener);
                }
                if (Node.PROP_PROPERTY_SETS.equals (propertyName)) {
                    firePropertySetsChange(null,null);
                }
                if (XmlMultiViewDataObject.PROP_SAX_ERROR.equals(propertyName)) {
                    fireShortDescriptionChange((String) oldValue, (String) newValue);
                }
            }
            
        };
        getDataObject ().addPropertyChangeListener (ddListener);
    }

    protected Sheet createSheet () {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(DDDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(DDDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getWebApp().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
}
