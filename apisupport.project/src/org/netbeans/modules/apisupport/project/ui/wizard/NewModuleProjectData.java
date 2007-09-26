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

package org.netbeans.modules.apisupport.project.ui.wizard;

import org.openide.WizardDescriptor;

/**
 * Model for storing data gained from <em>NetBeans Plug-in Module</em> wizard
 * panels.
 *
 * @author Martin Krauskopf
 */
final class NewModuleProjectData {

    private WizardDescriptor settings;
    private final int wizardType;

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
     * @param wizardType one of NewNbModuleWizardIterator.TYPE_*
     */
    NewModuleProjectData(int wizardType) {
        this.wizardType = wizardType;
    }
    
    void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    WizardDescriptor getSettings() {
        assert settings != null;
        return settings;
    }
    
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
    
    int getWizardType() {
        return wizardType;
    }
    
}
