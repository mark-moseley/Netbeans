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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Queries the user for the platform directory associated with the
 * instance they are registering.
 */
class AddDomainPlatformPanel implements WizardDescriptor.FinishablePanel,
        ChangeListener {
    
    private static Profile[] SHORT_PROFILES_LIST = { Profile.DEFAULT };
    private static Profile[] LONG_PROFILES_LIST = { Profile.DEFAULT, Profile.DEVELOPER,
        Profile.CLUSTER, Profile.ENTERPRISE };
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualPlatformPanel component;
    private WizardDescriptor wiz;
    
   /*  Get the visual component for the panel. In this template, the component
     is kept separate. This can be more efficient: if the wizard is created
     but never displayed, or not all panels are displayed, it is better to
     create only those which really need to be visible.
    */
    public Component getComponent() {
        return getAIVPP();
    }
    
    private AddInstanceVisualPlatformPanel getAIVPP() {
        if (component == null) {
            File f = ServerLocationManager.getLatestPlatformLocation();
            File defaultLoc = null;
            if (null == f || !f.exists()) {
                String prop = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME);
                if (null != prop && prop.length() > 0) {
                    // there is a possible root directory for the AS
                    File installRoot = new File(prop);
                    if (ServerLocationManager.isGoodAppServerLocation(installRoot)) {
                        defaultLoc = installRoot;
                    }
                }
                if (null == defaultLoc) {
                    defaultLoc = new File(System.getProperty("user.home"));//NOI18N
                }
            } else {
                defaultLoc = f;
            }
            component = new AddInstanceVisualPlatformPanel(defaultLoc);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_RegServ_EnterPlatformDir"); //NOI18N
    }
    
    /** Determine if the input is valid.
     *
     * Is the user entered directory an app server install directory?
     *
     * If the install directory is a GlassFish install, is the IDE running in
     * a J2SE 5.0 VM?
     *
     * If the user attempts to register a default instance, is there a usable one?
     *   See Util.getRegisterableDefaultDomains(File)
     *
     * Is the user asking for an unsupported instance type?
     */
    public boolean isValid() {
        boolean retVal = true;
        if (null == wiz) {
            return false;
        }
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, null);
        String instLoc = getAIVPP().getInstallLocation();
        if (instLoc.startsWith("\\\\")) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainPlatformPanel.class,
                    "Msg_NoAuthorityComponent"));                               // NOI18N
            retVal = false;
        }
        File location = new File(getAIVPP().getInstallLocation());
        if (retVal && !ServerLocationManager.isGoodAppServerLocation(location)) {
            Object selectedType = getAIVPP().getSelectedType();
            if (selectedType == AddDomainWizardIterator.REMOTE){
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_NeedValidInstallEvenForRemote"));
            }else{
                // not valid install directory
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_InValidInstall")); // NOI18N
            }
            getAIVPP().setDomainsList(new Object[0],false);
            getAIVPP().setProfilesList(new Profile[0],false);
            retVal = false;
        } else if (retVal) {
            Object oldPlatformLoc = wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
            if (!location.equals(oldPlatformLoc) || getAIVPP().getDomainsListModel().getSize() < 1) {
                Object[] domainsList = getDomainList(Util.getRegisterableDefaultDomains(location),location);
                getAIVPP().setDomainsList(domainsList,true);
            }
            if (!location.equals(oldPlatformLoc) || getAIVPP().getProfilesListModel().getSize() < 1) {
                if (ServerLocationManager.getAppServerPlatformVersion(location) !=
                        ServerLocationManager.GF_V2) {
                    getAIVPP().setProfilesList(SHORT_PROFILES_LIST,true);
                } else {
                    getAIVPP().setProfilesList(LONG_PROFILES_LIST,true);
                }
            }
            //component.setDomainsList();
            if (ServerLocationManager.isGlassFish(location)) {
                String javaClassVersion =
                        System.getProperty("java.class.version");               // NOI18N
                double jcv = Double.parseDouble(javaClassVersion);
                if (jcv < 49.0) {
                    // prevent ClassVersionUnsupportedError....
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(AddDomainPlatformPanel.class,
                            "Msg_RequireJ2SE5"));                               // NOI18N
                    retVal = false;
                }
            }
        }
        if (retVal) {
            wiz.putProperty(AddDomainWizardIterator.PLATFORM_LOCATION,location);
            wiz.putProperty(AddDomainWizardIterator.USER_NAME,
                    AddDomainWizardIterator.BLANK);
            wiz.putProperty(AddDomainWizardIterator.PASSWORD,
                    AddDomainWizardIterator.BLANK);
            Object selectedType = getAIVPP().getSelectedType();
            if (selectedType == AddDomainWizardIterator.DEFAULT) {
                File[] usableDomains = Util.getRegisterableDefaultDomains(location);
                if (usableDomains.length == 0) {
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(AddDomainPlatformPanel.class,
                            "Msg_NoDefaultDomainsAvailable"));                      //NOI18N
                    retVal = false;
                }
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                String dirCandidate = getAIVPP().getDomainDir();
                if (null != dirCandidate) {
                    File domainDir = new File(dirCandidate);
                    // this should not happen. The previous page of the wizard should
                    // prevent this panel from appearing.
                    String mess = Util.rootOfUsableDomain(domainDir);
                    if (null != mess) {
                        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                                mess);
                        retVal = false;
                    } else {
                        //File platformDir = (File) wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
                        Util.fillDescriptorFromDomainXml(wiz, domainDir);
                        // fill in the admin name and password from the asadminprefs file
                        String username = "admin";
                        String password = null;
                        File f = new File(System.getProperty("user.home")+"/.asadminprefs"); //NOI18N
                        if (f.exists()){
                            FileInputStream fis = null;
                            try{
                                
                                fis = new FileInputStream(f);
                                Properties p = new Properties();
                                p.load(fis);
//                                fis.close();
                                
                                Enumeration e = p.propertyNames() ;
                                while(e.hasMoreElements()) {
                                    String v = (String)e.nextElement();
                                    if ("AS_ADMIN_USER".equals(v))//admin user//NOI18N
                                        username = p.getProperty(v );
                                    else if ("AS_ADMIN_PASSWORD".equals(v)){ // admin password//NOI18N
                                        password = p.getProperty(v );
                                    }
                                }
                                
                            } catch (IOException e){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        e);
                            } finally {
                                if (null != fis) {
                                    try {
                                        fis.close();
                                    } catch (IOException ex) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                ex);
                                    }
                                }
                            }
                        }
                        wiz.putProperty(AddDomainWizardIterator.PASSWORD, password);
                        wiz.putProperty(AddDomainWizardIterator.USER_NAME,username);
                    }
                } else {
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(AddDomainPlatformPanel.class,
                            "Msg_NoDefaultDomainsAvailable"));                      //NOI18N
                    retVal = false;
                }
            } else if (selectedType == AddDomainWizardIterator.REMOTE) {
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
                wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
            } else if (selectedType == AddDomainWizardIterator.LOCAL) {
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
                wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
            } else if (selectedType == AddDomainWizardIterator.PERSONAL) {
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
                wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
                wiz.putProperty(AddDomainWizardIterator.PROFILE, getAIVPP().getProfile());
            } else {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_UnsupportedType"));                                    //NOI18N
                retVal = false;
            }
        }
        return retVal;
    }
    
    private Object[] getDomainList(File[] dirs, File location){
        return getServerList(dirs,location);
    }
    
    private Object[] getServerList(File[] dirs,File location){
        java.util.List xmlList = new java.util.ArrayList();
        Object retVal[];
        for(int i=0; location != null && i<dirs.length; i++){
            String hostPort = Util.getHostPort(dirs[i],location);
            if(hostPort != null) {
                xmlList.add(
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "LBL_domainListEntry", new Object[] {hostPort,dirs[i].toString()}));
            }
        }//for
        retVal = xmlList.toArray();
        return retVal;
    }
    
    // Event Handling
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
//        System.out.println("PP fireChangeEvent on");
        while (it.hasNext()) {
            ChangeListener l = (ChangeListener) it.next();
//            System.out.println("    "+l);
            l.stateChanged(ev);
        }
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    public void storeSettings(Object settings) {
        // TODO implement?
    }
    
    public void stateChanged(ChangeEvent e) {
//        System.out.println("PP stateChanged");
        if (null != wiz) {
            wiz.putProperty(AddDomainWizardIterator.TYPE, getAIVPP().getSelectedType());
            fireChangeEvent(); //e);
        }
    }
    
    public boolean isFinishPanel() {
        Object selectedType = getAIVPP().getSelectedType();
        return selectedType == AddDomainWizardIterator.DEFAULT;
    }
    
}

