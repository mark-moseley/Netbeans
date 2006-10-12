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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.awt.Image;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
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
 */
public class EjbJarMultiViewDataNode extends DataNode {
    
    private static final String DEPLOYMENT = "deployment"; // NOI18N
    
    private EjbJarMultiViewDataObject dataObject;
    
    /**
     * Name of property for spec version
     */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /**
     * Listener on dataobject
     */
    private PropertyChangeListener ddListener;
    
    public EjbJarMultiViewDataNode(EjbJarMultiViewDataObject obj) {
        this(obj, Children.LEAF);
    }
    
    public EjbJarMultiViewDataNode(EjbJarMultiViewDataObject obj, Children ch) {
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
                if (EjbJarMultiViewDataObject.PROP_DOCUMENT_DTD.equals(propertyName)) {
                    firePropertyChange(PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                } else if (DataObject.PROP_VALID.equals(propertyName) && Boolean.TRUE.equals(newValue)) {
                    removePropertyChangeListener(EjbJarMultiViewDataNode.this.ddListener);
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
        ss.setName(DEPLOYMENT);
        ss.setDisplayName(NbBundle.getMessage(EjbJarMultiViewDataNode.class, "PROP_deploymentSet"));
        ss.setShortDescription(NbBundle.getMessage(EjbJarMultiViewDataNode.class, "HINT_deploymentSet"));
        ss.setValue("helpID", "TBD---Ludo ejbjar node");   // NOI18N
        
        Property p = new PropertySupport.ReadWrite(PROPERTY_DOCUMENT_TYPE,
                String.class,
                NbBundle.getBundle(EjbJarMultiViewDataNode.class).getString("PROP_documentDTD"),
                NbBundle.getBundle(EjbJarMultiViewDataNode.class).getString("HINT_documentDTD")) {
            public Object getValue() {
                java.math.BigDecimal version = dataObject.getEjbJar().getVersion();
                return (version == null ? "" : version.toString());
            }
            
            public void setValue(Object value) {
                String val = (String) value;
                if (EjbJar.VERSION_2_1.equals(val) && !val.equals(dataObject.getEjbJar().getVersion().toString())) {
                    dataObject.getEjbJar().setVersion(new java.math.BigDecimal(val));
                    dataObject.modelUpdatedFromUI();
                }
            }
        };
        ss.put(p);
        s.put(ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public java.awt.Image getIcon(int type) {
        Image ejbJarXmlIcon = Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); //NOI18N
        
        if (dataObject.getSaxError() == null){
            return ejbJarXmlIcon;
        }

        Image errorBadgeIcon = Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/error-badge.gif"); //NOI18N
        return Utilities.mergeImages(ejbJarXmlIcon, errorBadgeIcon, 6, 6);
    }
    
    void descriptionChanged(String oldDesc, String newDesc) {
        setShortDescription(newDesc == null ? "Enterprise Bean deployment descriptor" : newDesc); //NOI18N
    }
    
    public String getShortDescription() {
        SAXException saxError = dataObject.getSaxError();
        if (saxError==null) {
            return Utils.getBundleMessage("HINT_ejb_dd");
        } else {
            return saxError.getMessage();
        }
    }
}
