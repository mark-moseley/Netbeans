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

package org.netbeans.modules.j2ee.common.project.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea, Radko Najman
 */
public class J2eePlatformUiSupport {
    
    private J2eePlatformUiSupport() {
    }

    @Deprecated
    public static ComboBoxModel createPlatformComboBoxModel(String serverInstanceId, String j2eeLevel, Object moduleType) {
        return new J2eePlatformComboBoxModel(serverInstanceId, Profile.fromPropertiesString(j2eeLevel), J2eeModule.Type.fromJsrType(moduleType));
    }

    public static ComboBoxModel createPlatformComboBoxModel(String serverInstanceId, Profile j2eeProfile, J2eeModule.Type moduleType) {
        return new J2eePlatformComboBoxModel(serverInstanceId, j2eeProfile, moduleType);
    }

    @Deprecated
    public static ComboBoxModel createSpecVersionComboBoxModel(String j2eeSpecVersion) {
        return new J2eeSpecVersionComboBoxModel(Profile.fromPropertiesString(j2eeSpecVersion));
    }

    public static ComboBoxModel createSpecVersionComboBoxModel(Profile profile) {
        return new J2eeSpecVersionComboBoxModel(profile);
    }

    @Deprecated
    public static boolean getJ2eePlatformAndSpecVersionMatch(Object j2eePlatformModelObject,
            Object j2eeSpecVersionModelObject, Object moduleType) {

        return getJ2eePlatformAndSpecVersionMatch(j2eePlatformModelObject, j2eeSpecVersionModelObject, J2eeModule.Type.fromJsrType(moduleType));
    }

    public static boolean getJ2eePlatformAndSpecVersionMatch(Object j2eePlatformModelObject,
            Object j2eeSpecVersionModelObject, J2eeModule.Type moduleType) {
        if (!(j2eePlatformModelObject instanceof J2eePlatformAdapter
                && (j2eeSpecVersionModelObject instanceof String || j2eeSpecVersionModelObject instanceof Profile))) {
            return false;
        }

        J2eePlatform j2eePlatform = ((J2eePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
        Profile profile = null;
        if (j2eeSpecVersionModelObject instanceof Profile) {
            profile = (Profile) j2eeSpecVersionModelObject;
        } else {
            profile = Profile.fromPropertiesString((String) j2eeSpecVersionModelObject);
        }
        return j2eePlatform.getSupportedProfiles(moduleType).contains(profile);
    }

    @Deprecated
    public static String getSpecVersion(Object j2eeSpecVersionModelObject) {
        return ((J2eePlatformComboBoxItem)j2eeSpecVersionModelObject).getProfile().toPropertiesString();
    }

    public static Profile getJavaEEProfile(Object j2eeSpecVersionModelObject) {
        return ((J2eePlatformComboBoxItem) j2eeSpecVersionModelObject).getProfile();
    }
    
    public static String getServerInstanceID(Object j2eePlatformModelObject) {
        if (j2eePlatformModelObject == null)
            return null;

        J2eePlatform j2eePlatform = ((J2eePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null && platform.getDisplayName().equals(j2eePlatform.getDisplayName())) {
                return serverInstanceIDs[i];
            }
        }
        
        return null;
    }
    
    /**
     * TODO: AB: Temporary fix of #54544.
     */
    public static void setSelectedPlatform(ComboBoxModel model, String serverInstanceID) {
        if (!(model instanceof J2eePlatformComboBoxModel))
            return;
        
        ((J2eePlatformComboBoxModel)model).setSelectedItem(serverInstanceID);
    }
    
    private static final class J2eePlatformComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private J2eePlatformAdapter[] j2eePlatforms;
        private final String initialJ2eePlatform;
        private J2eePlatformAdapter selectedJ2eePlatform;
        private final Profile j2eeProfile;
        private final J2eeModule.Type moduleType;
        
        public J2eePlatformComboBoxModel(String serverInstanceID, Profile j2eeProfile, J2eeModule.Type moduleType) {
            initialJ2eePlatform = serverInstanceID;
            this.j2eeProfile = j2eeProfile;
            this.moduleType = moduleType;

            getJ2eePlatforms(moduleType);
        }
        
        public Object getElementAt(int index) {
            return getJ2eePlatforms(moduleType)[index];
        }

        public int getSize() {
            return getJ2eePlatforms(moduleType).length;
        }
        
        public Object getSelectedItem() {
            return selectedJ2eePlatform;
        }
        
        public void setSelectedItem(Object obj) {
            selectedJ2eePlatform = (J2eePlatformAdapter)obj;
        }
        
        public void setSelectedItem(String serverInstanceID) {
            for (int i = 0; i < j2eePlatforms.length; i++) {
                if (j2eePlatforms[i].getServerInstanceID().equals(serverInstanceID)) {
                    selectedJ2eePlatform = j2eePlatforms[i];
                    return;
                }
            }
        }
                
        private synchronized J2eePlatformAdapter[] getJ2eePlatforms(J2eeModule.Type moduleType) {
            if (j2eePlatforms == null) {
                String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
                Set<J2eePlatformAdapter> orderedNames = new TreeSet<J2eePlatformAdapter>();
                boolean activeFound = false;

                for (int i = 0; i < serverInstanceIDs.length; i++) {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
                    if (j2eePlatform != null) {
                        if (j2eePlatform.getSupportedTypes().contains(moduleType)
                                && j2eePlatform.getSupportedProfiles(moduleType).contains(j2eeProfile)) {
                            J2eePlatformAdapter adapter = new J2eePlatformAdapter(j2eePlatform, serverInstanceIDs[i]);
                            orderedNames.add(adapter);
                        
                            if (selectedJ2eePlatform == null && !activeFound && initialJ2eePlatform != null) {
                                if (serverInstanceIDs[i].equals(initialJ2eePlatform)) {
                                    selectedJ2eePlatform = adapter;
                                    activeFound = true;
                                }
                            }
                        }
                    }
                }
                j2eePlatforms = orderedNames.toArray(new J2eePlatformAdapter[orderedNames.size()]);
            }
            return j2eePlatforms;
        }
    }
        
    private static final class J2eePlatformAdapter implements Comparable {
        private J2eePlatform platform;
        private String serverInstanceID;
        
        public J2eePlatformAdapter(J2eePlatform platform, String serverInstanceID) {
            this.platform = platform;
            this.serverInstanceID = serverInstanceID;
        }
        
        public J2eePlatform getJ2eePlatform() {
            return platform;
        }
        
        public String getServerInstanceID() {
            return serverInstanceID;
        }
        
        public String toString() {
            String s = platform.getDisplayName();
            if (s == null) {
                s = ""; // NOI18N
            }
            return s;
        }

        public int compareTo(Object o) {
            J2eePlatformAdapter oa = (J2eePlatformAdapter)o;
            return toString().compareTo(oa.toString());
        }
    }
    
    private static final class J2eeSpecVersionComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private static final long serialVersionUID = 20366133932230984L;
        
        private J2eePlatformComboBoxItem[] j2eeSpecVersions;
        
        private J2eePlatformComboBoxItem initialJ2eeSpecVersion;
        private J2eePlatformComboBoxItem selectedJ2eeSpecVersion;
    
        public J2eeSpecVersionComboBoxModel(Profile j2eeProfile) {
            initialJ2eeSpecVersion = new J2eePlatformComboBoxItem(j2eeProfile);
            
            List<J2eePlatformComboBoxItem> orderedListItems = new ArrayList<J2eePlatformComboBoxItem>();
            orderedListItems.add(new J2eePlatformComboBoxItem(Profile.JAVA_EE_5));
            orderedListItems.add(new J2eePlatformComboBoxItem(Profile.J2EE_14));
            if (!(Profile.JAVA_EE_5 == initialJ2eeSpecVersion.getProfile()) &&
                    !(Profile.J2EE_14 == initialJ2eeSpecVersion.getProfile())) {
                orderedListItems.add(0, new J2eePlatformComboBoxItem(Profile.J2EE_13));
            }
            
            j2eeSpecVersions = orderedListItems.toArray(new J2eePlatformComboBoxItem[orderedListItems.size()]);
            selectedJ2eeSpecVersion = initialJ2eeSpecVersion;
        }
        
        public Object getElementAt(int index) {
            return j2eeSpecVersions[index];
        }
        
        public int getSize() {
            return j2eeSpecVersions.length;
        }
        
        public Object getSelectedItem() {
            return selectedJ2eeSpecVersion;
        }
        
        public void setSelectedItem(Object obj) {
            selectedJ2eeSpecVersion = (J2eePlatformComboBoxItem)obj;
        }
    }
    
    private static final class J2eePlatformComboBoxItem{
        private final Profile profile;

        public J2eePlatformComboBoxItem (Profile profile){
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }

        @Override
        public String toString(){
            return profile.getDisplayName();
        }
    }

    
}
