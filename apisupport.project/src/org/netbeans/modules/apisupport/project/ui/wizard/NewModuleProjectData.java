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

package org.netbeans.modules.apisupport.project.ui.wizard;

import org.openide.WizardDescriptor;

/**
 * Model for storing data gained from <em>NetBeans Plug-in Module</em> wizard
 * panels.
 *
 * @author mkrauskopf
 */
final class NewModuleProjectData {
    
    private static final String DATA_PROPERTY_NAME = "moduleProjectData"; // NOI18N
    
    private boolean netBeansOrg;
    private boolean standalone = true; // standalone is default
    private String projectName;
    private String projectLocation;
    private String projectFolder;
    private String suiteRoot;
    private boolean mainProject;
    private String codeNameBase;
    private String platformID;
    private String bundle;
    private String layer;
    private String projectDisplayName;
    private int moduleCounter;
    private int suiteCounter;
    
    /**
     * Tries to find an instance of {@link NewModuleProjectData} in the given
     * setting and returns it. If none is found, the new one is created, stored
     * in the setting and then returned.
     */
    static NewModuleProjectData getData(final WizardDescriptor setting) {
        NewModuleProjectData data = (NewModuleProjectData) setting.getProperty(DATA_PROPERTY_NAME);
        if (data == null) {
            data = new NewModuleProjectData();
            setting.putProperty(NewModuleProjectData.DATA_PROPERTY_NAME, data);
        }
        return data;
    }
    
    /** Creates a new instance of NewModuleProjectData */
    NewModuleProjectData() {/* empty constructor */}
    
    void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }
    
    void setNetBeansOrg(boolean netBeansOrg) {
        this.netBeansOrg = netBeansOrg;
    }
    
    boolean isNetBeansOrg() {
        return netBeansOrg;
    }
    
    boolean isStandalone() {
        return standalone;
    }
    
    boolean isSuiteComponent() {
        return !isNetBeansOrg() && !isStandalone();
    }
    
    String getProjectName() {
        return projectName;
    }
    
    void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    String getProjectLocation() {
        return projectLocation;
    }
    
    void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }
    
    String getProjectFolder() {
        return projectFolder;
    }
    
    void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }
    
    String getSuiteRoot() {
        return suiteRoot;
    }
    
    void setSuiteRoot(String suiteRoot) {
        this.suiteRoot = suiteRoot;
    }
    
    protected boolean isMainProject() {
        return mainProject;
    }
    
    protected void setMainProject(boolean mainProject) {
        this.mainProject = mainProject;
    }
    
    String getCodeNameBase() {
        return codeNameBase;
    }
    
    void setCodeNameBase(String codeNameBase) {
        this.codeNameBase = codeNameBase;
    }
    
    String getPlatformID() {
        return platformID;
    }
    
    void setPlatformID(String platformID) {
        this.platformID = platformID;
    }
    
    String getBundle() {
        return bundle;
    }
    
    void setBundle(String bundle) {
        this.bundle = bundle;
    }
    
    String getLayer() {
        return layer;
    }
    
    void setLayer(String layer) {
        this.layer = layer;
    }
    
    String getProjectDisplayName() {
        return projectDisplayName;
    }
    
    void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }
    
    int getModuleCounter() {
        return moduleCounter;
    }
    
    void setModuleCounter(int counter) {
        this.moduleCounter = counter;
    }
    
    int getSuiteCounter() {
        return suiteCounter;
    }
    
    void setSuiteCounter(int counter) {
        this.suiteCounter = counter;
    }
    
}
