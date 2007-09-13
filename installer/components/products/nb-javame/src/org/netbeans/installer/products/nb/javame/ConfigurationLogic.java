/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.products.nb.javame;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MOBILITY_CLUSTER =
            "{mobility-cluster}"; // NOI18N
    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    private static final String ID =
            "MOB"; // NOI18N
    private static final String DISTRIBUTION_README_RESOURCE =
            "org/netbeans/installer/products/nb/javame/DISTRIBUTION.txt";
    private static final String THIRDPARTYLICENSE_RESOURCE =
            "org/netbeans/installer/products/nb/javame/THIRDPARTYLICENSE.txt";
    
    private static final String[] MOBILITY_END_2_END =  {
        "config/Modules/org-netbeans-modules-mobility-end2end.xml",
        "config/Modules/org-netbeans-modules-mobility-jsr172.xml",
	"config/Modules/org-netbeans-modules-mobility-end2end-kit.xml",
        "modules/org-netbeans-modules-mobility-end2end.jar",
        "modules/org-netbeans-modules-mobility-jsr172.jar",
	"modules/org-netbeans-modules-mobility-end2end-kit.jar",
        "update_tracking/org-netbeans-modules-mobility-end2end.xml",
        "update_tracking/org-netbeans-modules-mobility-jsr172.xml",
	"update_tracking/org-netbeans-modules-mobility-end2end-kit.xml"
    };
    
    private static final String END2END_CANT_REMOVE_TEXT = ResourceUtils.getString(
            ConfigurationLogic.class, "error.cannot.remove.end2end");
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            MOBILITY_CLUSTER}, ID);
    }
    
    public void install(final Progress progress) throws InstallationException {
        super.install(progress);
        
        // HACK : remove mobility end-2-end if installed by mobility pack installer
        // and there is no enterpise4 cluster in the netbeans distribution
        File installationLocation = getProduct().getInstallationLocation();
        
        boolean removeEnd2End = false;
        
        if(NbWelcomePanel.BundleType.JAVAME.toString().equals(
                System.getProperty(NbWelcomePanel.WELCOME_PAGE_TYPE_PROPERTY))) {
            // Mobility Pack Installer
            removeEnd2End = true;
        }
        
        List <Product> toInstall = Registry.getInstance().getProductsToInstall();
        
        if(installationLocation!=null && removeEnd2End) {
            // check if pack is install in NetBeans with already installed enterprise4 cluster
            File entCluster = new File(installationLocation, ENTERPRISE_CLUSTER);
            if(!entCluster.exists() || FileUtils.isEmpty(entCluster))  {
                for(String file : MOBILITY_END_2_END) {
                    File del = new File(installationLocation,
                            MOBILITY_CLUSTER + File.separator + file);
                    try {
                        FileUtils.deleteFile(del);
                    } catch (IOException e) {
                        throw new InstallationException(
                                StringUtils.format(END2END_CANT_REMOVE_TEXT,del), e);
                    }
                }
            }
            
        }
    }
    
    public Text getDistributionReadme() {
        final String text = parseString("$R{" + DISTRIBUTION_README_RESOURCE + "}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
    
    public Text getThirdPartyLicense() {
        final String text = parseString("$R{" + THIRDPARTYLICENSE_RESOURCE + "}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
}
