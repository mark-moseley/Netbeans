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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.*;

import org.netbeans.modules.j2ee.dd.api.application.*;

import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;


import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.Project;
/**
 * List of children of a containing node.
 * Each child node is represented by one key from some data model.
 * Remember to document what your permitted keys are!
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 */
public class LogicalViewChildren extends Children.Keys  implements PropertyChangeListener {
    
    private final Application model;
    private final EarProjectProperties epp;
    
    public LogicalViewChildren(Application model, EarProjectProperties epp) {
        if (null == model)
            throw new IllegalArgumentException("model");
        this.model = model;
        this.epp = epp;
        //model.addPropertyChangeListener(this);
        epp.addPropertyChangeListener(this);
    }
    
    protected void addNotify() {
        super.addNotify();
        // set the children to use:
        updateKeys();
        // and listen to changes in the model too:
        model.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        List keys = Collections.EMPTY_LIST;
//        Module mods[] = model.getModule();
//        if (null != mods) {
//            keys = new ArrayList();
////            List webs = new ArrayList();
//            for (int i = 0; i < mods.length; i++) {
//                String modPath = mods[i].getEjb();
//                if (null != modPath) {
//                    keys.add(modPath);
//                    continue;
//                }
//                Web web = mods[i].getWeb();
//                if (null != web) {
//                    keys.add(web.getWebUri());
//                    continue;
//                }
//                modPath = mods[i].getConnector();
//                if (null != modPath) {
//                    keys.add(modPath);
//                    continue;
//                }
//                modPath = mods[i].getJava();
//                if (null != modPath) {
//                    keys.add(modPath);
//                    continue;
//                }
//            }
//        }
        Object t = epp.get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        Iterator iter = vcpis.iterator();
        keys = new ArrayList();
        while (iter.hasNext()) {
            t = iter.next();
            if (! (t instanceof VisualClassPathItem)) {
                assert false : "jar content element isn't a VCPI?????";
                continue;
            }
            VisualClassPathItem vcpi = (VisualClassPathItem) t;
            Object obj = vcpi.getObject();
            AntArtifact aa;
            Project p;
            if (obj instanceof AntArtifact) {
                aa = (AntArtifact) obj;
                p = aa.getProject();            
            }
            else continue;
            //try {
                J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
                //AppDDSegmentProvider seg = (AppDDSegmentProvider) p.getLookup().lookup(AppDDSegmentProvider.class);
                if (null != jmp) {
                    //J2eeModule jm = jmp.getJ2eeModule();
                    keys.add(vcpi);
                }
            //}
            
        }
        
       
            
        
        // get your keys somehow from the data model:
        //MyDataElement[] keys = model.getChildren();
        // you can also use Collection rather than an array
        setKeys(keys);
    }
    
    protected void removeNotify() {
        epp.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        // interpret your key here...usually one node generated, but could be zero or more
        VisualClassPathItem vcpi = (VisualClassPathItem) key;
        return new Node[] {new ModuleNode(vcpi,epp) };
    }
    
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        updateKeys();
    }
 
/*    private void addKeyValues(List keyContainer, List beans) {
        Iterator it = beans.iterator();
        while (it.hasNext()) {
            keyContainer.add(new Schema2BeansKey(it.next()));
        }
    }
    
    private static class Schema2BeansKey {
        private Object delegate;
        public Schema2BeansKey(Object delegate) {
            this.delegate = delegate;
        }
        
        public Object getBean() {
            return delegate;
        }
        
        public boolean equals(Object other) {
            return other != null  &&
                   other.getClass().equals(getClass()) &&
                   getBean() == ((Schema2BeansKey)other).getBean();
        }
        
        public int hashCode() {
            return System.identityHashCode(getBean());
        }
    }*/
}
