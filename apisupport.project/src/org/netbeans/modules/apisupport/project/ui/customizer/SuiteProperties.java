/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.SuiteSubModulesListModel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Provides convenient access to a lot of Suite Module's properties.
 *
 * @author Martin Krauskopf
 */
public final class SuiteProperties extends ModuleProperties {
    
    public static final String DISABLED_MODULES_PROPERTY = "disabled.modules"; // NOI18N
    public static final String DISABLED_CLUSTERS_PROPERTY = "disabled.clusters"; // NOI18N
    
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    
    private NbPlatform activePlatform;
    
    /** Project the current properties represents. */
    private SuiteProject project;
    
    /** Represent original set of sub-modules. */
    private Set/*<Project>*/ origSubModules;
    
    /** Represent currently set set of sub-modules. */
    private Set/*<Project>*/ subModules;
    
    // models
    private SuiteSubModulesListModel moduleListModel;
    
    /** disabled modules */
    private String[] disabledModules;
    /** disabled clusters */
    private String[] disabledClusters;
    /** boolean variable to remember whether there were some changes */
    private boolean changedDisabledModules, changedDisabledClusters;
    
    /** keeps all information related to branding*/
    private final BasicBrandingModel brandingModel;
    
    /**
     * Creates a new instance of SuiteProperties
     */
    public SuiteProperties(SuiteProject project, AntProjectHelper helper,
            PropertyEvaluator evaluator, Set/*<Project>*/ subModules) {
        super(helper, evaluator);
        this.project = project;
        refresh(subModules);
        this.disabledModules = getArrayProperty(evaluator, DISABLED_MODULES_PROPERTY);
        this.disabledClusters = getArrayProperty(evaluator, DISABLED_CLUSTERS_PROPERTY);
        brandingModel = new BasicBrandingModel(this);
    }
    
    void refresh(Set/*<Project>*/ subModules) {
        reloadProperties();
        this.origSubModules = Collections.unmodifiableSet(subModules);
        this.subModules = subModules;
        this.moduleListModel = null;
        // XXX similar to SuiteProject.getActivePlatform:
        activePlatform = NbPlatform.getPlatformByID(
                getEvaluator().getProperty("nbplatform.active")); // NOI18N
        firePropertiesRefreshed();
    }
    
    public SuiteProject getProject() {
        return project;
    }
    
    Map/*<String, String>*/ getDefaultValues() {
        return Collections.EMPTY_MAP; // no default value (yet)
    }
    
    public NbPlatform getActivePlatform() {
        if (activePlatform == null || !activePlatform.isValid()) {
            ModuleProperties.reportLostPlatform(activePlatform);
            activePlatform = NbPlatform.getDefaultPlatform();
        }
        return activePlatform;
    }
    
    void setActivePlatform(NbPlatform newPlaf) {
        NbPlatform oldPlaf = this.activePlatform;
        this.activePlatform = newPlaf;
        firePropertyChange(NB_PLATFORM_PROPERTY, oldPlaf, newPlaf);
    }
    
    String[] getDisabledModules() {
        return disabledModules;
    }
    
    String[] getDisabledClusters() {
        return disabledClusters;
    }
    
    void setDisabledClusters(String[] value) {
        if (Arrays.asList(disabledClusters).equals(Arrays.asList(value))) {
            return;
        }
        this.disabledClusters = value;
        this.changedDisabledClusters = true;
    }
    
    void setDisabledModules(String[] value) {
        if (Arrays.asList(disabledModules).equals(Arrays.asList(value))) {
            return;
        }
        this.disabledModules = value;
        this.changedDisabledModules = true;
    }
    
    public static String[] getArrayProperty(PropertyEvaluator evaluator, String p) {
        String s = evaluator.getProperty(p);
        String[] arr = null;
        if (s != null) {
            StringTokenizer tok = new StringTokenizer(s, ","); // NOI18N
            arr = new String[tok.countTokens()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = tok.nextToken().trim();
            }
        }
        return arr == null ? new String[0] : arr;
    }
    
    /**
     * Stores cached properties. This is called when the user press <em>OK</em>
     * button in the properties customizer. If <em>Cancel</em> button is
     * pressed properties will not be saved,.
     */
    void storeProperties() throws IOException {
        ModuleProperties.storePlatform(getHelper(), activePlatform);
        getBrandingModel().store();
        
        // store submodules if they've changed
        SuiteSubModulesListModel model = getModulesListModel();
        if (model.isChanged()) {
            SuiteUtils.replaceSubModules(this);
        }
        
        if (changedDisabledModules) {
            String[] separated = (String[])disabledModules.clone();
            for (int i = 0; i < disabledModules.length - 1; i++) {
                separated[i] = disabledModules[i] + ',';
            }
            setProperty(DISABLED_MODULES_PROPERTY, separated);
        }
        
        if (changedDisabledClusters) {
            String[] separated = (String[])disabledClusters.clone();
            for (int i = 0; i < disabledClusters.length - 1; i++) {
                separated[i] = disabledClusters[i] + ',';
            }
            setProperty(DISABLED_CLUSTERS_PROPERTY, separated);
        }
        
        super.storeProperties();
    }
    
    Set/*<Project>*/ getSubModules() {
        return getModulesListModel().getSubModules();
    }
    
    Set/*<Project>*/ getOrigSubModules() {
        return origSubModules;
    }
    
    /**
     * Returns list model of module's dependencies regarding the currently
     * selected platform.
     */
    SuiteSubModulesListModel getModulesListModel() {
        if (moduleListModel == null) {
            moduleListModel = new SuiteSubModulesListModel(subModules);
        }
        return moduleListModel;
    }
    
    BasicBrandingModel getBrandingModel() {
        return brandingModel;
    }
}

