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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
        setIconBase(dataObject.getIconBaseForValidDocument());
        initListeners();
    }

    /**
     * Initialize listening on adding/removing server so it is
     * possible to add/remove property sheets
     */
    private void initListeners() {
        ddListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (EjbJarMultiViewDataObject.PROP_DOCUMENT_DTD.equals(evt.getPropertyName())) {
                    firePropertyChange(PROPERTY_DOCUMENT_TYPE, evt.getOldValue(), evt.getNewValue());
                }
                if (DataObject.PROP_VALID.equals(evt.getPropertyName())
                        && Boolean.TRUE.equals(evt.getNewValue())) {
                    removePropertyChangeListener(EjbJarMultiViewDataNode.this.ddListener);
                }
                if (EjbJarMultiViewDataObject.PROP_DOC_VALID.equals(evt.getPropertyName())) {
                    if (Boolean.TRUE.equals(evt.getNewValue())) {
                        setIconBase(dataObject.getIconBaseForValidDocument());
                    } else {
                        setIconBase(dataObject.getIconBaseForInvalidDocument());
                    }
                }
                if (Node.PROP_PROPERTY_SETS.equals(evt.getPropertyName())) {
                    firePropertySetsChange(null, null);
                }
            }

        };
        getDataObject().addPropertyChangeListener(ddListener);
    }

    protected Sheet createSheet() {
        Sheet s = new Sheet();
        Sheet.Set ss = new Sheet.Set();
        ss.setName(DEPLOYMENT);
        ss.setDisplayName(NbBundle.getMessage(EjbJarMultiViewDataNode.class, "PROP_deploymentSet"));
        ss.setShortDescription(NbBundle.getMessage(EjbJarMultiViewDataNode.class, "HINT_deploymentSet"));
        ss.setValue("helpID", "TBD---Ludo ejbjar node");   // NOI18N

        Property p = new PropertySupport.ReadOnly(PROPERTY_DOCUMENT_TYPE,
                String.class,
                NbBundle.getBundle(EjbJarMultiViewDataNode.class).getString("PROP_documentDTD"),
                NbBundle.getBundle(EjbJarMultiViewDataNode.class).getString("HINT_documentDTD")) {
            public Object getValue() {
                return dataObject.getEjbJar().getVersion();
            }
        };
        ss.put(p);
        s.put(ss);

        return s;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    void descriptionChanged(String oldDesc, String newDesc) {
        setShortDescription(newDesc == null ? "Enterprise Bean deployment descriptor" : newDesc); //NOI18N
    }

}
