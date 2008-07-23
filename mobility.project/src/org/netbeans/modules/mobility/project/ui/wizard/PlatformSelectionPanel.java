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

/*
 * PlatformSelectionPanel.java
 *
 * Created on 28. duben 2004, 15:52
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Adam Sotona
 */
public class PlatformSelectionPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String REQUIRED_CONFIGURATION = "RequiredConfiguration"; // NOI18N
    public static final String REQUIRED_PROFILE ="RequiredProfile"; // NOI18N
    
    public static final String PLATFORM_DESCRIPTION = "PlatformDescription"; //NOI18N
    
    public static class PlatformDescription {
        public J2MEPlatform platform;
        public J2MEPlatform.Device device;
        public String configuration;
        public String profile;
    }
    
    private PlatformSelectionPanelGUI gui;
    private String reqCfg, reqProf;
    private boolean first = true;
        
    public boolean isFinishPanel() {
        return true;
    }
    
    public void addChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
    }
    
    public void removeChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
    }
    
    public synchronized Component getComponent() {
        if (gui == null) {
            gui = new PlatformSelectionPanelGUI();
        }
        return gui;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PlatformSelectionPanel.class);
    }
    
    public boolean isValid() {
        return true;
    }
    
    public void readSettings(final Object settings) {
        if (first) {
            first = false;
            getComponent();
            final TemplateWizard wiz = (TemplateWizard)settings;
            reqCfg = (String)wiz.getProperty(REQUIRED_CONFIGURATION);
            reqProf = (String)wiz.getProperty(REQUIRED_PROFILE);
            final ArrayList<Profile> l = new ArrayList<Profile>();
            final Profile cfg = parseProfile(reqCfg);
            if (cfg != null) l.add(cfg);
            final Profile prof = parseProfile(reqProf);
            if (prof != null) l.add(prof);
            final J2MEPlatform p = findTheBestPlatform(l.toArray(new Profile[l.size()]));
            assert p != null;
            gui.setValues(wiz, p, findDevice(p, l),  reqCfg, reqProf);
        } else {
            gui.updateErrorMessage();
        }
    }
    
    private static Profile parseProfile(final String profile) {
        if (profile != null) try {
            final int j = profile.lastIndexOf('-');
            return j > 0 ? new Profile(profile.substring(0, j).trim(), new SpecificationVersion(profile.substring(j+1).trim())) : new Profile(profile.trim(), null);
        } catch (NumberFormatException nfe) {
            //HO HO HO ERROR IN PARSING !
        }
        return null;
    }
    
    private static boolean compareProfiles(final Profile p, final Profile req) {
        if (!p.getName().equalsIgnoreCase(req.getName())) return false;
        return req.getVersion() == null || (p.getVersion() != null && p.getVersion().compareTo(req.getVersion()) >= 0);
    }
    
    private static int ratePlatform(final J2MEPlatform platform, final Profile profiles[]) {
        int rating = 0;
        if (platform.getName().startsWith("J2ME_Wireless_Toolkit")) rating++; //NOI18N
        if (platform.getName().startsWith("J2ME_Wireless_Toolkit_2")) rating++; //NOI18N
        if (profiles == null) return rating;
        final Profile pp[] = platform.getSpecification().getProfiles();
        for (int j=0; j<profiles.length; j++) {
            for (int i=0; i<pp.length; i++) {
                if (compareProfiles(pp[i], profiles[j])) {
                    rating += 3;
                    i = pp.length;
                }
            }
        }
        return rating;
    }
    
    private static J2MEPlatform findTheBestPlatform(final Profile profiles[]) {
        final JavaPlatform plat[] = JavaPlatformManager.getDefault().getPlatforms(null,  new Specification(J2MEPlatform.SPECIFICATION_NAME, null, null)); //NOI18N
        J2MEPlatform best = null;
        int bestRating = -1;
        for (int i=0; i<plat.length; i++) {
            if (plat[i] instanceof J2MEPlatform) {
                final int rating = ratePlatform((J2MEPlatform)plat[i], profiles);
                if (rating > bestRating) {
                    bestRating = rating;
                    best = (J2MEPlatform)plat[i];
                }
            }
        }
        return best;
    }
    
    
    private J2MEPlatform.Device findDevice(final J2MEPlatform p, final ArrayList<Profile> profiles) {
        final J2MEPlatform.Device d[] = p.getDevices();
        for (int i=0; i<d.length; i++) {
            if (new HashSet<Profile>(Arrays.asList(d[i].getProfiles())).containsAll(profiles)) {
                return d[i];
            }
        }
        return null;
    }
    
    public void storeSettings(final Object settings) {
        final PlatformDescription desc = new PlatformDescription();
        getComponent();
        final J2MEPlatform pl = gui.getPlatform();
        final J2MEPlatform.Device dev = gui.getDevice();
        final J2MEPlatform.J2MEProfile conf = gui.getConfiguration();
        final J2MEPlatform.J2MEProfile prof = gui.getProfile();
        desc.platform = pl;
        if (conf != null) {
            desc.configuration = conf.toString();
        }
        if (prof != null) {
            desc.profile = prof.toString();
        }
        desc.device = dev;
        final TemplateWizard wiz = (TemplateWizard)settings;
        wiz.putProperty(PLATFORM_DESCRIPTION, desc);
    }
    
}
