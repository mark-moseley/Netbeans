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

package org.netbeans.installer.products.nb.portalpack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    private static final String THIRDPARTYLICENSE_RESOURCE =
            "org/netbeans/installer/products/nb/portalpack/THIRDPARTYLICENSE.txt";
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/nb/portalpack/wizard.xml"; // NOI18N
    private static final String NB_BASE_UID= "nb-base";
    private List<WizardComponent> wizardComponents;
    
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    public void install(Progress progress) throws InstallationException {
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(NB_BASE_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        try {
            NetBeansUtils.runUpdater(nbLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("cl.error.running.updater"),//NOI18N
                    e);
        }
    }
    public void uninstall(Progress progress) throws UninstallationException {
        //remove data created by updater
    }
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    public boolean registerInSystem() {
        return false;
    }
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
    @Override
    public Text getThirdPartyLicense() {
        final String text = parseString("$R{" + THIRDPARTYLICENSE_RESOURCE + "}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
}
