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

package org.netbeans.modules.j2ee.ejbcore.naming;

import java.util.prefs.Preferences;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * EJB naming preferences.
 * {@link http://java.sun.com/blueprints/code/namingconventions.html}
 * 
 * @author Martin Adamek
 */
public final class EJBNameOptions extends AdvancedOption {
    
    private static final String SESSION_EJBCLASS_PREFIX = "sessionEjbClassPrefix"; // NOI18N
    private static final String SESSION_EJBCLASS_SUFFIX = "sessionEjbClassSuffix"; // NOI18N
    private static final String SESSION_LOCAL_PREFIX = "sessionLocalPrefix"; // NOI18N
    private static final String SESSION_LOCAL_SUFFIX = "sessionLocalSuffix"; // NOI18N
    private static final String SESSION_REMOTE_PREFIX = "sessionRemotePrefix"; // NOI18N
    private static final String SESSION_REMOTE_SUFFIX = "sessionRemoteSuffix"; // NOI18N
    private static final String SESSION_LOCALHOME_PREFIX = "sessionLocalHomePrefix"; // NOI18N
    private static final String SESSION_LOCALHOME_SUFFIX = "sessionLocalHomeSuffix"; // NOI18N
    private static final String SESSION_REMOTEHOME_PREFIX = "sessionRemoteHomePrefix"; // NOI18N
    private static final String SESSION_REMOTEHOME_SUFFIX = "sessionRemoteHomeSuffix"; // NOI18N
    private static final String SESSION_EJBNAME_PREFIX = "sessionEjbNamePrefix"; // NOI18N
    private static final String SESSION_EJBNAME_SUFFIX = "sessionEjbNameSuffix"; // NOI18N
    private static final String SESSION_DISPLAYNAME_PREFIX = "sessionDisplayNamePrefix"; // NOI18N
    private static final String SESSION_DISPLAYNAME_SUFFIX = "sessionDisplayNameSuffix"; // NOI18N
    
    private static final String ENTITY_EJBCLASS_PREFIX = "entityEjbClassPrefix"; // NOI18N
    private static final String ENTITY_EJBCLASS_SUFFIX = "entityEjbClassSuffix"; // NOI18N
    private static final String ENTITY_LOCAL_PREFIX = "entityLocalPrefix"; // NOI18N
    private static final String ENTITY_LOCAL_SUFFIX = "entityLocalSuffix"; // NOI18N
    private static final String ENTITY_REMOTE_PREFIX = "entityRemotePrefix"; // NOI18N
    private static final String ENTITY_REMOTE_SUFFIX = "entityRemoteSuffix"; // NOI18N
    private static final String ENTITY_LOCALHOME_PREFIX = "entityLocalHomePrefix"; // NOI18N
    private static final String ENTITY_LOCALHOME_SUFFIX = "entityLocalHomeSuffix"; // NOI18N
    private static final String ENTITY_REMOTEHOME_PREFIX = "entityRemoteHomePrefix"; // NOI18N
    private static final String ENTITY_REMOTEHOME_SUFFIX = "entityRemoteHomeSuffix"; // NOI18N
    private static final String ENTITY_EJBNAME_PREFIX = "entityEjbNamePrefix"; // NOI18N
    private static final String ENTITY_EJBNAME_SUFFIX = "entityEjbNameSuffix"; // NOI18N
    private static final String ENTITY_DISPLAYNAME_PREFIX = "entityDisplayNamePrefix"; // NOI18N
    private static final String ENTITY_DISPLAYNAME_SUFFIX = "entityDisplayNameSuffix"; // NOI18N
    
    private static final String MESSAGEDRIVEN_EJBCLASS_PREFIX = "messageDrivenEjbClassPrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBCLASS_SUFFIX = "messageDrivenEjbClassSuffix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBNAME_PREFIX = "messageDrivenEjbNamePrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBNAME_SUFFIX = "messageDrivenEjbNameSuffix"; // NOI18N
    private static final String MESSAGEDRIVEN_DISPLAYNAME_PREFIX = "messageDrivenDisplayNamePrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_DISPLAYNAME_SUFFIX = "messageDrivenDisplayNameSuffix"; // NOI18N
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EJBNameOptions.class, "AdvancedOption_DisplayName");
    }
    
    @Override
    public String getTooltip() {
        return NbBundle.getMessage(EJBNameOptions.class, "AdvancedOption_Tooltip");
    }

    @Override
    public OptionsPanelController create() {
        return new EJBNameOptionsPanelController();
    }
    
    // Session
    
    public String getSessionEjbClassPrefix() { return prefs().get(SESSION_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setSessionEjbClassPrefix(String prefix) { prefs().put(SESSION_EJBCLASS_PREFIX, prefix); }
    
    public String getSessionEjbClassSuffix() { return prefs().get(SESSION_EJBCLASS_SUFFIX, "Bean"); } // NOI18N
    public void setSessionEjbclassSuffix(String suffix) { prefs().put(SESSION_EJBCLASS_SUFFIX, suffix); }
    
    public String getSessionLocalPrefix() { return prefs().get(SESSION_LOCAL_PREFIX, ""); } // NOI18N
    public void setSessionLocalPrefix(String prefix) { prefs().put(SESSION_LOCAL_PREFIX, prefix); }
    
    public String getSessionLocalSuffix() { return prefs().get(SESSION_LOCAL_SUFFIX, "Local"); } // NOI18N
    public void setSessionLocalSuffix(String suffix) { prefs().put(SESSION_LOCAL_SUFFIX, suffix); }
    
    public String getSessionRemotePrefix() { return prefs().get(SESSION_REMOTE_PREFIX, ""); } // NOI18N
    public void setSessionRemotePrefix(String prefix) { prefs().put(SESSION_REMOTE_PREFIX, prefix); }
    
    public String getSessionRemoteSuffix() { return prefs().get(SESSION_REMOTE_SUFFIX, "Remote"); } // NOI18N
    public void setSessionRemoteSuffix(String suffix) { prefs().put(SESSION_REMOTE_SUFFIX, suffix); }
    
    public String getSessionLocalHomePrefix() { return prefs().get(SESSION_LOCALHOME_PREFIX, ""); } // NOI18N
    public void setSessionLocalHomePrefix(String prefix) { prefs().put(SESSION_LOCALHOME_PREFIX, prefix); }
    
    public String getSessionLocalHomeSuffix() { return prefs().get(SESSION_LOCALHOME_SUFFIX, "LocalHome"); } // NOI18N
    public void setSessionLocalHomeSuffix(String suffix) { prefs().put(SESSION_LOCALHOME_SUFFIX, suffix); }
    
    public String getSessionRemoteHomePrefix() { return prefs().get(SESSION_REMOTEHOME_PREFIX, ""); } // NOI18N
    public void setSessionRemoteHomePrefix(String prefix) { prefs().put(SESSION_REMOTEHOME_PREFIX, prefix); }
    
    public String getSessionRemoteHomeSuffix() { return prefs().get(SESSION_REMOTEHOME_SUFFIX, "RemoteHome"); } // NOI18N
    public void setSessionRemoteHomeSuffix(String suffix) { prefs().put(SESSION_REMOTEHOME_SUFFIX, suffix); }
    
    public String getSessionEjbNamePrefix() { return prefs().get(SESSION_EJBNAME_PREFIX, ""); } // NOI18N
    public void setSessionEjbNamePrefix(String prefix) { prefs().put(SESSION_EJBNAME_PREFIX, prefix); }
    
    public String getSessionEjbNameSuffix() { return prefs().get(SESSION_EJBNAME_SUFFIX, "Bean"); } // NOI18N
    public void setSessionEjbNameSuffix(String suffix) { prefs().put(SESSION_EJBNAME_SUFFIX, suffix); }
    
    public String getSessionDisplayNamePrefix() { return prefs().get(SESSION_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setSessionDisplayNamePrefix(String prefix) { prefs().put(SESSION_DISPLAYNAME_PREFIX, prefix); }
    
    public String getSessionDisplayNameSuffix() { return prefs().get(SESSION_DISPLAYNAME_SUFFIX, "SB"); } // NOI18N
    public void setSessionDisplayNameSuffix(String suffix) { prefs().put(SESSION_DISPLAYNAME_SUFFIX, suffix); }
    
    // Entity
    
    public String getEntityEjbClassPrefix() { return prefs().get(ENTITY_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setEntityEjbClassPrefix(String prefix) { prefs().put(ENTITY_EJBCLASS_PREFIX, prefix); }
    
    public String getEntityEjbClassSuffix() { return prefs().get(ENTITY_EJBCLASS_SUFFIX, "Bean"); } // NOI18N
    public void setEntityEjbClassSuffix(String suffix) { prefs().put(ENTITY_EJBCLASS_SUFFIX, suffix); }
    
    public String getEntityLocalPrefix() { return prefs().get(ENTITY_LOCAL_PREFIX, ""); } // NOI18N
    public void setEntityLocalPrefix(String prefix) { prefs().put(ENTITY_LOCAL_PREFIX, prefix); }
    
    public String getEntityLocalSuffix() { return prefs().get(ENTITY_LOCAL_SUFFIX, "Local"); } // NOI18N
    public void setEntityLocalSuffix(String suffix) { prefs().put(ENTITY_LOCAL_SUFFIX, suffix); }
    
    public String getEntityRemotePrefix() { return prefs().get(ENTITY_REMOTE_PREFIX, ""); } // NOI18N
    public void setEntityRemotePrefix(String prefix) { prefs().put(ENTITY_REMOTE_PREFIX, prefix); }
    
    public String getEntityRemoteSuffix() { return prefs().get(ENTITY_REMOTE_SUFFIX, "Remote"); } // NOI18N
    public void setEntityRemoteSuffix(String suffix) { prefs().put(ENTITY_REMOTE_SUFFIX, suffix); }
    
    public String getEntityLocalHomePrefix() { return prefs().get(ENTITY_LOCALHOME_PREFIX, ""); } // NOI18N
    public void setEntityLocalHomePrefix(String prefix) { prefs().put(ENTITY_LOCALHOME_PREFIX, prefix); }
    
    public String getEntityLocalHomeSuffix() { return prefs().get(ENTITY_LOCALHOME_SUFFIX, "LocalHome"); } // NOI18N
    public void setEntityLocalHomeSuffix(String suffix) { prefs().put(ENTITY_LOCALHOME_SUFFIX, suffix); }
    
    public String getEntityRemoteHomePrefix() { return prefs().get(ENTITY_REMOTEHOME_PREFIX, ""); } // NOI18N
    public void setEntityRemoteHomePrefix(String prefix) { prefs().put(ENTITY_REMOTEHOME_PREFIX, prefix); }
    
    public String getEntityRemoteHomeSuffix() { return prefs().get(ENTITY_REMOTEHOME_SUFFIX, "RemoteHome"); } // NOI18N
    public void setEntityHomeRemoteSuffix(String suffix) { prefs().put(ENTITY_REMOTEHOME_SUFFIX, suffix); }
    
    public String getEntityEjbNamePrefix() { return prefs().get(ENTITY_EJBNAME_PREFIX, ""); } // NOI18N
    public void setEntityEjbNamePrefix(String prefix) { prefs().put(ENTITY_EJBNAME_PREFIX, prefix); }
    
    public String getEntityEjbNameSuffix() { return prefs().get(ENTITY_EJBNAME_SUFFIX, "Bean"); } // NOI18N
    public void setEntityEjbNameSuffix(String suffix) { prefs().put(ENTITY_EJBNAME_SUFFIX, suffix); }
    
    public String getEntityDisplayNamePrefix() { return prefs().get(ENTITY_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setEntityDisplayNamePrefix(String prefix) { prefs().put(ENTITY_DISPLAYNAME_PREFIX, prefix); }
    
    public String getEntityDisplayNameSuffix() { return prefs().get(ENTITY_DISPLAYNAME_SUFFIX, "EB"); } // NOI18N
    public void setEntityDisplayNameSuffix(String suffix) { prefs().put(ENTITY_DISPLAYNAME_SUFFIX, suffix); }
    
    // MessageDriven
    
    public String getMessageDrivenEjbClassPrefix() { return prefs().get(MESSAGEDRIVEN_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setMessageDrivenEjbClassPrefix(String prefix) { prefs().put(MESSAGEDRIVEN_EJBCLASS_PREFIX, prefix); }
    
    public String getMessageDrivenEjbClassSuffix() { return prefs().get(MESSAGEDRIVEN_EJBCLASS_SUFFIX, "Bean"); } // NOI18N
    public void setMessageDrivenEjbClassSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_EJBCLASS_SUFFIX, suffix); }
    
    public String getMessageDrivenEjbNamePrefix() { return prefs().get(MESSAGEDRIVEN_EJBNAME_PREFIX, ""); } // NOI18N
    public void setMessageDrivenEjbNamePrefix(String prefix) { prefs().put(MESSAGEDRIVEN_EJBNAME_PREFIX, prefix); }
    
    public String getMessageDrivenEjbNameSuffix() { return prefs().get(MESSAGEDRIVEN_EJBNAME_SUFFIX, "Bean"); } // NOI18N
    public void setMessageDrivenEjbNameSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_EJBNAME_SUFFIX, suffix); }

    public String getMessageDrivenDisplayNamePrefix() { return prefs().get(MESSAGEDRIVEN_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setMessageDrivenDisplayNamePrefix(String prefix) { prefs().put(MESSAGEDRIVEN_DISPLAYNAME_PREFIX, prefix); }
    
    public String getMessageDrivenDisplayNameSuffix() { return prefs().get(MESSAGEDRIVEN_DISPLAYNAME_SUFFIX, "MDB"); } // NOI18N
    public void setMessageDrivenDisplayNameSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_DISPLAYNAME_SUFFIX, suffix); }
    
    // helpers
    
    private Preferences prefs() {
        return NbPreferences.forModule(EJBNameOptions.class);
    }
    
}
