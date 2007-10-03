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

package org.netbeans.modules.j2ee.sun.ddloaders;

import java.awt.Image;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.NbBundle;

/**
 * A node to represent this ejb-jar.xml object.
 *
 * @author pfiala
 * @author Peter Williams
 */
public class SunDescriptorDataNode extends DataNode {
    
    private static final String SUN_DD = "sundd"; // NOI18N
    
    private SunDescriptorDataObject dataObject;
    
    /**
     * Name of property for spec version
     */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /**
     * Listener on dataobject
     */
    private PropertyChangeListener ddListener;
    
    public SunDescriptorDataNode(SunDescriptorDataObject obj) {
        this(obj, Children.LEAF);
    }
    
    public SunDescriptorDataNode(SunDescriptorDataObject obj, Children ch) {
        super(obj, ch);
        dataObject = obj;
        initListeners();
        setIconBase(dataObject.getSaxError() == null);
    }
    
    /**
     * Initialize listening on adding/removing server so it is
     * possible to add/remove property sheets
     */
    private void initListeners() {
        ddListener = new PropertyChangeListener() {
            
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                if (SunDescriptorDataObject.PROP_DOCUMENT_DTD.equals(propertyName)) {
                    firePropertyChange(PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                } else if (DataObject.PROP_VALID.equals(propertyName) && Boolean.TRUE.equals(newValue)) {
                    removePropertyChangeListener(SunDescriptorDataNode.this.ddListener);
                } else if (XmlMultiViewDataObject.PROP_DOCUMENT_VALID.equals(propertyName)) {
                    setIconBase(Boolean.TRUE.equals(newValue));
                } else if (Node.PROP_PROPERTY_SETS.equals(propertyName)) {
                    firePropertySetsChange(null, null);
                } else if (XmlMultiViewDataObject.PROP_SAX_ERROR.equals(propertyName)) {
                    fireShortDescriptionChange((String) oldValue, (String) newValue);
                }
            }
            
        };
        getDataObject().addPropertyChangeListener(ddListener);
    }
    
    private void setIconBase(final boolean valid) {
        if (valid) {
            setIconBaseWithExtension(dataObject.getIconBaseForValidDocument());
        } else {
            setIconBaseWithExtension(dataObject.getIconBaseForInvalidDocument());
        }
        fireIconChange();
    }
    
    protected Sheet createSheet() {
        Sheet s = new Sheet();
        Sheet.Set ss = new Sheet.Set();
        ss.setName(SUN_DD);
        ss.setDisplayName(NbBundle.getMessage(SunDescriptorDataNode.class, "PROP_deploymentSet")); // NOI18N
        ss.setShortDescription(NbBundle.getMessage(SunDescriptorDataNode.class, "HINT_deploymentSet")); // NOI18N
        ss.setValue("helpID", "TBD: sun-dd node"); // NOI18N
        
//        Property p = new PropertySupport.ReadWrite(PROPERTY_DOCUMENT_TYPE,
//                String.class,
//                NbBundle.getBundle(SunDescriptorDataNode.class).getString("PROP_documentDTD"),
//                NbBundle.getBundle(SunDescriptorDataNode.class).getString("HINT_documentDTD")) {
//            public Object getValue() {
//                java.math.BigDecimal version = dataObject.getDDRoot().getVersion();
//                return (version == null ? "" : version.toString());
//            }
//            
//            public void setValue(Object value) {
//                String val = (String) value;
//                if (EjbJar.VERSION_2_1.equals(val) && !val.equals(dataObject.getDDRoot().getVersion().toString())) {
//                    dataObject.getDDRoot().setVersion(new java.math.BigDecimal(val));
//                    dataObject.modelUpdatedFromUI();
//                }
//            }
//        };
//        ss.put(p);
        s.put(ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public java.awt.Image getIcon(int type) {
        String iconBase = dataObject.getIconBaseForValidDocument();
        Image sunDDIcon = Utilities.loadImage(iconBase + ".gif"); //NOI18N
        
        if(dataObject.getSaxError() == null) {
            return sunDDIcon;
        }
        
        Image errorBadgeIcon = Utilities.loadImage(Utils.ICON_BASE_ERROR_BADGE + ".gif"); //NOI18N
        return Utilities.mergeImages(sunDDIcon, errorBadgeIcon, 6, 6);
    }
    
// Dead code?    
    
// Dead code?    
//    void descriptionChanged(String oldDesc, String newDesc) {
//        setShortDescription(newDesc == null ? "Sun Application Server Deployment Descriptor" : newDesc); //NOI18N
//    }
    
    public String getShortDescription() {
        SAXException saxError = dataObject.getSaxError();
        if(saxError == null) {
            return NbBundle.getMessage(SunDescriptorDataNode.class,"LBL_NodeShortDescription"); // NOI18N
        } else {
            return saxError.getMessage();
        }
    }
}
