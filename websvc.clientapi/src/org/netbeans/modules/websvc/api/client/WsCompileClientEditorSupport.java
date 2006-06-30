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

package org.netbeans.modules.websvc.api.client;

import java.util.List;
import java.util.Collections;

import javax.swing.JPanel;

import org.openide.WizardValidationException;

/**
 *
 * @author Peter Williams
 */
public interface WsCompileClientEditorSupport {

    /** Editor fires this property event when the user edits a feature list
     */
    public static final String PROP_FEATURES_CHANGED = "featuresChanged";
    public static final String PROP_DEBUG_CHANGED = "debugChanged";
    public static final String PROP_OPTIMIZE_CHANGED = "optimizeChanged";
    public static final String PROP_VERBOSE_CHANGED = "verboseChanged";
    
    public WsCompileClientEditorSupport.Panel getWsCompileSupport();
    
    public interface Panel {

        /** The panel for the host
         */
        public JPanel getComponent();
        
        /** Call to initialize the properties in the editor panel
         */
        public void initValues(List/*ServiceSettings*/ settings);
        
        /** Validation entry point.
         */
        public void validatePanel() throws WizardValidationException;
    }
    
    public final class ServiceSettings {
        private String name;
        private ClientStubDescriptor stubType;
        private List/*String*/ availableFeatures;
        private List/*String*/ importantFeatures;
        private String currentFeatures;
        private String newFeatures;
        
        public ServiceSettings(String sn, ClientStubDescriptor st, String c, List a, List i) {
            name = sn;
            stubType = st;
            currentFeatures = newFeatures = c;
            availableFeatures = Collections.unmodifiableList(a);
            importantFeatures = Collections.unmodifiableList(i);
        }
        
        public String getServiceName() {
            return name;
        }
        
        public ClientStubDescriptor getClientStubDescriptor() {
            return stubType;
        }
        
        public String getCurrentFeatures() {
            return currentFeatures;
        }
        
        public String getNewFeatures() {
            return newFeatures;
        }
        
        public List/*String*/ getAvailableFeatures() {
            return availableFeatures;
        }
        
        public List/*String*/ getImportantFeatures() {
            return importantFeatures;
        }
        
        public String toString() {
            return getServiceName();
        }
        
        public void setNewFeatures(String nf) {
            newFeatures = nf;
        }
    }
    
    public final class FeatureDescriptor {
        
        private String serviceName;
        private String features;
        
        public FeatureDescriptor(String serviceName, String features) {
            this.serviceName = serviceName;
            this.features = features;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFeatures() {
            return features;
        }
    }
}
