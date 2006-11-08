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

package org.netbeans.installer;

import com.installshield.product.RequiredBytesTable;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.wizardbeans.InstallAction;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.RunnableWizardBeanState;
import com.installshield.wizard.service.WizardLog;
import com.installshield.wizard.service.file.FileService;
import java.io.File;

import java.util.Properties;

public class NbSummaryPanel extends TextDisplayPanel {
    private int type = ProductService.PRE_INSTALL;
    
    private String nbInstallDir = "";
    private String j2seInstallDir = "";
    private String jreInstallDir = "";
    
    public NbSummaryPanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public int getType() {
        return type;        
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        logEvent(this, Log.DBG, "queryEnter ENTER");
        boolean okay = super.queryEnter(evt);
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
            nbInstallDir = (String) service.getProductBeanProperty
            (productURL, Names.CORE_IDE_ID, "installLocation");
            j2seInstallDir = (String) System.getProperties().get("j2seInstallDir");
            jreInstallDir = (String) System.getProperties().get("jreInstallDir");
            logEvent(this, Log.DBG, "queryEnter nbInstallDir: " + nbInstallDir);
            if (type == ProductService.POST_INSTALL) {
                logEvent(this, Log.DBG, "queryEnter POST_INSTALL PANEL");
                logEvent(this, Log.DBG, "queryEnter exitCode: " + getWizard().getExitCode());
                if (getWizard().getExitCode() != -1) {
                    //Installation failed or cancelled.
                    String summaryMessage;
                    if (getWizard().getExitCode() == InstallJ2sdkAction.JDK_UNHANDLED_ERROR) {
                        summaryMessage = "$L(org.netbeans.installer.Bundle,SummaryPanel.description1)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,Product.displayName)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description3)";
                        //NB install location
                        summaryMessage += "<br><br>"
                        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
                        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
                        + nbInstallDir;
                        //Error message
                        if (Util.isWindowsOS()) {
                            summaryMessage += "<br><br>"
                            + "$L(org.netbeans.installer.Bundle, SummaryPanel.errorJDK,"
                            + "$L(org.netbeans.installer.Bundle, JDK.shortName),"
                            + j2seInstallDir + ","
                            + nbInstallDir + File.separator + "_uninst" + ")";
                        } else {
                            summaryMessage += "<br><br>"
                            + "$L(org.netbeans.installer.Bundle, SummaryPanel.errorJDK,"
                            + "$L(org.netbeans.installer.Bundle, JDK.shortName),"
                            + j2seInstallDir + ","
                            + j2seInstallDir + File.separator + "_uninst" + ")";
                        }
                    } else if (getWizard().getExitCode() == InstallJ2sdkAction.JRE_UNHANDLED_ERROR) {
                        summaryMessage = "$L(org.netbeans.installer.Bundle,SummaryPanel.description1)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,Product.displayName)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description3)";
                        //NB install location
                        summaryMessage += "<br><br>"
                        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
                        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
                        + nbInstallDir;
                        //Error message
                        summaryMessage += "<br><br>"
                        + "$L(org.netbeans.installer.Bundle, SummaryPanel.errorJRE,"
                        + "$L(org.netbeans.installer.Bundle, JRE.shortName),"
                        + jreInstallDir + ","
                        + nbInstallDir + File.separator + "_uninst" + ")";
                    } else {
                        InstallAction ia = (InstallAction) getWizardTree().getBean("install");
                        RunnableWizardBeanState state = ia.getState();
                        if (state.getState() == state.CANCELED) {
                            //User cancelled installation (install action)
                            removeAllFiles();
                            summaryMessage = resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.cancel)");
                        } else {
                            logEvent(this, Log.DBG, "queryEnter INSTALLATION FAILED");
                            Properties summary = service.getProductSummary(
                            ProductService.DEFAULT_PRODUCT_SOURCE,
                            ProductService.POST_INSTALL,
                            ProductService.HTML);
                            summaryMessage = summary.getProperty(ProductService.SUMMARY_MSG);
                            summaryMessage += resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.errorNB)");
                        }
                    }
                    setText(summaryMessage);
                } else {
                    //setText(resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.description)"));
                    logEvent(this, Log.DBG, "queryEnter INSTALLATION SUCCESSFUL");
                    logEvent(this, Log.DBG, "queryEnter summaryPostInstallMsg:'" + getPostInstallSummaryMessage() + "'");
                    setText(getPostInstallSummaryMessage());
                }
            } else if (type == ProductService.PRE_INSTALL) {
                /*Properties summary = service.getProductSummary(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                type,
                ProductService.HTML);
                setText(summary.getProperty(ProductService.SUMMARY_MSG));*/
                logEvent(this, Log.DBG, "queryEnter PRE_INSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPreInstallMsg:'" + getPreInstallSummaryMessage() + "'");
                setText(getPreInstallSummaryMessage());
            } else if (type == ProductService.POST_UNINSTALL) {
                logEvent(this, Log.DBG, "queryEnter POST_UNINSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPostUninstallMsg:'" + getPostUninstallSummaryMessage() + "'");
                setText(getPostUninstallSummaryMessage());
            } else if (type == ProductService.PRE_UNINSTALL) {
                logEvent(this, Log.DBG, "queryEnter PRE_UNINSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPreUninstallMsg:'" + getPreUninstallSummaryMessage() + "'");
                setText(getPreUninstallSummaryMessage());
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        return okay;
    }
    
    /** Remove created files/dirs when user cancels installation. */
    private void removeAllFiles () {
        WizardLog wizardLog = getWizard().getServices().getWizardLog();
        wizardLog.setLogOutputEnabled(false);
        
        try {
            FileService fileService = (FileService) getService(FileService.NAME);
            logEvent(this, Log.DBG, "removeAllFiles Deleting completely: " + nbInstallDir);
            int ret = fileService.deleteDirectory(nbInstallDir,false,true);
            logEvent(this, Log.DBG, "removeAllFiles Done. " + nbInstallDir + " deleted.");
            logEvent(this, Log.DBG, "removeAllFiles Deleting completely :" + j2seInstallDir);
            ret = fileService.deleteDirectory(j2seInstallDir,false,true);
            logEvent(this, Log.DBG, "removeAllFiles Done. " + j2seInstallDir + " deleted.");
        } catch (ServiceException ex) {
            //Nothing to do. Ignore.
            System.out.println("serviceexception ex:" + ex);
            ex.printStackTrace();
        }
    }
    
    private String getPostInstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,SummaryPanel.description1)";
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage += " " + "$L(org.netbeans.installer.Bundle,JDK.shortName)"
            + " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description2)";
        }
        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,Product.displayName)";
        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description3)";
        
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage += "<br><br>"
            + "$L(org.netbeans.installer.Bundle,JDK.shortName)" + " "
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
            + j2seInstallDir;
        }
        
        if (Util.isWindowsOS() && !Util.isJREAlreadyInstalled()) {
            summaryMessage += "<br><br>"
            + "$L(org.netbeans.installer.Bundle,JRE.shortName)" + " "
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
            + jreInstallDir;
        }
        
        summaryMessage += "<br><br>"
        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
        + nbInstallDir;
        
        if (Util.isWindowsOS()) {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description5,netbeans.exe,uninstaller.exe)";
        } else {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description5,netbeans,uninstaller)";
        }
        
        //How to uninstall JDK. We will show this message only when we installed
        //JDK.
        if (!Util.isJDKAlreadyInstalled()) {
            if (Util.isWindowsOS()) {
                summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description7,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName))";
            } else {
                summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description6,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName),"
                + j2seInstallDir + ",uninstall.sh)";
            }
        }
        
        //How to uninstall public JRE. We will show this message only when we installed
        //JRE. JRE is installer together with JDK only. If only JDK is already installed
        //we do not install JRE.
        if (Util.isWindowsOS() && !Util.isJREAlreadyInstalled() && !Util.isJDKAlreadyInstalled()) {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description7,"
            + "$L(org.netbeans.installer.Bundle,JRE.shortName))";
        }
        
        return summaryMessage;
    }
    
    private String getPreInstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,Product.displayName)"
        + " "
        + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewInstallMessage)"
        + "<br>" + nbInstallDir;
        
        // only show j2se install if no prior installation was found
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage += "<br><br>"
            + "$L(org.netbeans.installer.Bundle,JDK.shortName)"
            + " "
            + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewInstallMessage)" + "<br>"
            + j2seInstallDir + "<br>";
        }
        
        summaryMessage += "<br>" + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewSize)" + "<br>"
        + getTotalSize();
        return summaryMessage;
    }
    
    private String getPostUninstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,PreviewPanel.previewPostUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle,Product.displayName))";
        
        summaryMessage += "<br><br>"
        + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstall,"
        + "$L(org.netbeans.installer.Bundle, Product.userDir))");
        if (Util.isWindowsOS()) {
            summaryMessage += " "
            + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstallWindows)");
        } else {
            summaryMessage += " "
            + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstallUnix)");
        }
        
        return summaryMessage;
    }
    
    private String getPreUninstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,PreviewPanel.previewPreUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle,Product.displayName),"
        + nbInstallDir + ")";
        
        return summaryMessage;
    }
    
    private String getTotalSize() {
        RequiredBytesTable table;
        long size = 0;
        long j2seSize = 0;
        String mBytes = null;
        
        try {
            ProductService service = (ProductService)getService(ProductService.NAME);
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
            table = service.getRequiredBytes(productURL,Names.CORE_IDE_ID);
            size = table.getBytes(nbInstallDir) >> 20;
            logEvent(this, Log.DBG, "Size of NetBeans: " + size);
            
            j2seSize = getJ2SESize() >> 20;
            logEvent(this, Log.DBG, "Adjusted size of J2SE: " + j2seSize);
            size = size + j2seSize;
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        if (size > 0) {
            mBytes = Long.toString(size);
        } else {
            logEvent(this, Log.ERROR, "Error: Total product installation size is 0.");
        }
        return mBytes + " MB";  //NOI18N
    }
    
    /* Data installed in Documents and Settings are included in this estimate. Waiting for response
     * from JDK team if we can safely delete it. It is about 55MB.
     */
    private long getJ2SESize () {
        if (Util.isWindowsOS()) {
            if (Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                return 0L;
            } else if (!Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                //Install only JDK, JRE is already installed
                return 251000000L;
            } else if (Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //We cannot install JRE if JDK is already installed, as we do not know if
                //JRE installer is installed with JDK
                return 0L;
            } else if (!Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //Install JDK and JRE
                return 353000000L;
            }
        } else if (Util.isLinuxOS()) {
            return 150000000L;
        } else if (Util.isSolarisSparc()) {
            return 148000000L;
        } else if (Util.isSolarisX86()) {
            return 140000000L;
        }
        return 0L;
    }
}
