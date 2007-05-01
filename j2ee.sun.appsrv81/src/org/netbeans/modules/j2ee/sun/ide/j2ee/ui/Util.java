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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.Toolkit;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.sun.api.Asenv;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  nityad
 */
public class Util {
    
    /** Creates a new instance of Util */
    public Util() {
    }
    
    ///Numeric Document
    public static NumericDocument getNumericDocument(){
        return new NumericDocument();
    }
    public static class NumericDocument extends PlainDocument {
        final private Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {
            char[] s = str.toCharArray();
            char[] r = new char[s.length];
            int j = 0;
            for (int i = 0; i < r.length; i++) {
                if (Character.isDigit(s[i])) {
                    r[j++] = s[i];
                } else {
                    toolkit.beep();
                }
            }
            super.insertString(offs, new String(r, 0, j), a);
        }
    } // class NumericDocument
    
    public static void showInformation(final String msg,  final String title){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    public static void showInformationWhenHolding(final String msg,  final String title){
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }

    public static void showInformation(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    //Fix bug# 5005127 - deletion of cp resource should inform user about dependent resources
    public static  Object showWarning(final String msg){
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);
        return DialogDisplayer.getDefault().notify(d);
        
        
    }
    
    public static Object   showWarning(final String msg, final String title){
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);
        return DialogDisplayer.getDefault().notify(d);
        
    }
    
    public static void showError(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    public static void showError(final String msg, final String title){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    public static void setStatusBar(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        });
        
    }
    
    
    
    static File[] getRegisterableDefaultDomains(File location) {
        File[] noneRegisterable = new File[0];
        //File[] retVal = noneRegisterable;
        String ext = (File.separatorChar == '/' ? "conf" : "bat");          // NOI18N
        File asenv = new File(location,"config/asenv."+ext);            // NOI18N
        Asenv asenvContent = new Asenv(asenv);
        String defDomainsDirName = asenvContent.get(Asenv.AS_DEF_DOMAINS_PATH);
        File domainsDir  = new File(defDomainsDirName);//NOI18N
//        File domainsDir = new File(location,"domains");
        if (!domainsDir.exists() && location.getAbsolutePath().startsWith("/opt/SUNWappserver")) {
            domainsDir = new File("/var/opt/SUNWappserver/domains");
        }
        if (!domainsDir.exists())
            return noneRegisterable;
        
        File[] possibles = domainsDir.listFiles(new java.io.FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && pathname.canWrite())
                    return true;
                return false;
            }
        });
        if (null == possibles)
            return noneRegisterable;

        // prune out unusable entries...
        int realCount = 0;
        for (int i = 0; i < possibles.length; i++) {
            String mess = rootOfUsableDomain(possibles[i]);
            if (null == mess) {
                realCount++;
            } else {
                possibles[i] = null;
            }
        }
        File[] retVal = new File[realCount];
        int nextSlot = 0;
        for (int i = 0; i < possibles.length; i++) {
            if (possibles[i] != null) {
                retVal[nextSlot] = possibles[i];
                nextSlot++;
            }
        }
        return retVal;
    }
    
    /** 
     */
    static String rootOfUsableDomain(File f) {
        if (!f.exists()) {
            return NbBundle.getMessage(Util.class, "ERROR_DOMAIN_ROOT_DOES_NOT_EXIST");
        }
        File testFile = new File(f,"config");
        File testFile2 = new File(testFile,"domain.xml");
        if (!testFile2.exists())
            return NbBundle.getMessage(Util.class, "ERROR_CANNOT_FIND_DOMAIN");
        if (!testFile2.canWrite())
            return NbBundle.getMessage(Util.class, "ERROR_CANNOT_WRITE_TO_DOMAIN");
        if (!testFile.exists() || !testFile.isDirectory() || !testFile.canWrite())
            return NbBundle.getMessage(Util.class, "ERROR_CANNOT_WRITE_TO_CONFIG");
        testFile = new File(f,"logs");
        if (!testFile.exists() || !testFile.isDirectory() || !testFile.canWrite())
            return NbBundle.getMessage(Util.class, "ERROR_CANNOT_WRITE_TO_LOG");
        return null;
    }
    
    public static String getHostPort(File domainDir, File platformDir){
        File xmlRoot;
        if(File.pathSeparatorChar == ':')
            xmlRoot = new File(domainDir.getAbsolutePath() + "/config/domain.xml");//NOI18N
        else
            xmlRoot = new File(domainDir.getAbsolutePath() + "\\config\\domain.xml");//NOI18N
        String adminHostPort = null;
        try{
            Class[] argClass = new Class[2];
            argClass[0] = File.class;
            argClass[1] = File.class;
            Object[] argObject = new Object[2];
            argObject[0] = xmlRoot;
            argObject[1] = platformDir;
            
            ClassLoader loader = ServerLocationManager.getServerOnlyClassLoader(platformDir);
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.AppServerBridge");
                java.lang.reflect.Method getHostPort = cc.getMethod("getHostPort", argClass);//NOI18N
                adminHostPort = (String)getHostPort.invoke(null, argObject);
            }
        } catch (RuntimeException re) {
            Logger.getLogger(Util.class.getName()).log(Level.WARNING,"",re);
        }catch(Exception ex){
            //Suppressing exception while trying to obtain admin host port value
        }
        return adminHostPort;
    }
    
    static String getDeploymentUri(File domainDir, File platformDir) {
        return SunURIManager.SUNSERVERSURI+getHostPort(domainDir,platformDir);
    }

    static File domainFile(File domainDir) {
        if (File.pathSeparatorChar == ';')
            return new File(domainDir+"\\config\\domain.xml");
        else
            return new File(domainDir+"/config/domain.xml");
    }
    
    static void fillDescriptorFromDomainXml(final WizardDescriptor wiz, final File domainDir) {
        String hp = Util.getHostPort(domainDir,
                (File) wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION));
        if (null != hp) {
            wiz.putProperty(AddDomainWizardIterator.DOMAIN_FILE,
                    Util.domainFile(domainDir));
            int sepDex = hp.indexOf(':');
            wiz.putProperty(AddDomainWizardIterator.HOST,hp.substring(0,sepDex));
            wiz.putProperty(AddDomainWizardIterator.PORT,hp.substring(sepDex+1));
            wiz.putProperty(AddDomainWizardIterator.DOMAIN,domainDir.getName());
            wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,
                    domainDir.getParentFile().getAbsolutePath());
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
        } else {
            wiz.putProperty(AddDomainWizardIterator.HOST,"");
            wiz.putProperty(AddDomainWizardIterator.PORT,"");
            wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
            wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,
                    "");
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
        }
    }
    
    static JFileChooser getJFileChooser(final FileFilter f){
        JFileChooser chooser = new JFileChooser();
        decorateChooser(chooser,null,NbBundle.getMessage(Util.class, 
                "LBL_Chooser_Name"));
        return chooser;
    }
    
    static void decorateChooser(JFileChooser chooser,String fname,String title) {
        chooser.setDialogTitle(title);                                           //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(Util.class, 
                "Choose_Button_Mnemonic").charAt(0));                           //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(Util.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        
        chooser.getAccessibleContext().
                setAccessibleName(NbBundle.getMessage(Util.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        chooser.getAccessibleContext().
                setAccessibleDescription(NbBundle.getMessage(Util.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        if (null != fname && fname.length() > 0) {
            File sel = new File(fname);
            if (sel.isDirectory())
                chooser.setCurrentDirectory(sel);
            else
                chooser.setSelectedFile(sel);
        }
    }
    
    

}
