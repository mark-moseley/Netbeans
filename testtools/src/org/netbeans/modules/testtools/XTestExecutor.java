/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools;

/*
 * XTestExecutor.java
 *
 * Created on April 29, 2002, 10:54 AM
 */

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openide.ServiceType;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.execution.Executor;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;

import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.modules.testtools.wizards.WizardIterator;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/** Executor for XTest Workspace Build Script Data Object
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestExecutor extends Executor {
    
    static final long serialVersionUID = -5490616206437129681L;    
    
    /** Holds value of property netbeansHome. */
    private File netbeansHome;

    /** Holds value of property xtestHome. */
    private File xtestHome;
    
    /** Holds value of property jemmyHome. */
    private File jemmyHome;
    
    /** Holds value of property jellyHome. */
    private File jellyHome;
    
    /** Holds value of property testType. */
    private String testType=""; // NOI18N
    
    /** Holds value of property attributes. */
    private String attributes=""; // NOI18N
    
    /** Holds value of property showResults. */
    private boolean showResults=true;
    
    /** creates new XTestExecutor */    
    public XTestExecutor() {
        String home=System.getProperty("netbeans.home"); // NOI18N
        if (!new File(home+File.separator+"xtest-distribution").exists())  // NOI18N
            home=System.getProperty("netbeans.user"); // NOI18N
        xtestHome=new File(home+File.separator+"xtest-distribution"); // NOI18N
        jemmyHome=new File(home+File.separator+"modules"+File.separator+"ext"); // NOI18N
        jellyHome=new File(home+File.separator+"modules"+File.separator+"ext"); // NOI18N
    }
    
    /** creates new XTestEecutor, fills and returns propper Handler
     * @return ServiceType.Handle */    
/*    public static ServiceType.Handle getHandle() {
        return new ServiceType.Handle(new XTestExecutor());
    }
*/    
    /** throws "Not yet implemented" IOException
     * @param info ExecInfo
     * @throws IOException "Not yet implemented" IOException
     * @return never returns value */    
    public ExecutorTask execute(ExecInfo info) throws IOException {
        throw new IOException(NbBundle.getMessage(XTestExecutor.class, "Err_NotImplemented.")); // NOI18N
    }
    
    /** performs execution of given DataObject
     * @param obj DataObject
     * @throws IOException when some IO problems
     * @return ExecutorTask */    
    public ExecutorTask execute(DataObject obj) throws IOException {
        AntProjectCookie cookie=(AntProjectCookie)obj.getCookie(AntProjectCookie.class);
        if (cookie==null) {
            throw new IOException(NbBundle.getMessage(XTestExecutor.class, "Err_MissingAntProjectCookie")); // NOI18N
        }
        if (netbeansHome==null || XTestCompilerType.netHome.equals(netbeansHome)) {
            File home=WizardIterator.showFileChooser(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(XTestExecutor.class, "Title_SelectNetbeansHome"), true, false); // NOI18N
            if ((home!=null)&&(!XTestCompilerType.netHome.equals(home)))
                setNetbeansHome(home);
            else
                throw new IOException(NbBundle.getMessage(XTestExecutor.class, "Err_HomeDirectoryNotSet")); // NOI18N
        }
        TargetExecutor executor = new TargetExecutor(cookie, new String[]{"all"}); // NOI18N
        executor.addProperties(getProperties());
        if (showResults)
            return showResults(executor.execute(), obj);
        else
            return executor.execute();
    }
    
    private ExecutorTask showResults(final ExecutorTask task, final DataObject obj) {
        Thread t=new Thread(new Runnable() {
            public void run() {
                if (task.result()==0) {
                    try {
                        FileObject fo=obj.getFolder().getPrimaryFile();
                        fo=fo.getFileObject("results"); // NOI18N
                        fo=fo.getFileObject("index", "html"); // NOI18N
                        org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(fo.getURL());
                    } catch (Exception e) {}
                }
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        return task;
    }
    
    /** returns Help Context
     * @return HelpCtx */    
    public HelpCtx getHelpCtx() {
        return new HelpCtx (XTestExecutor.class);
    }
    
    /** Getter for property netbeansHome.
     * @return Value of property netbeansHome.
     */
    public File getNetbeansHome() {
        return this.netbeansHome; 
    }
    
    /** Setter for property netbeansHome.
     * @param netbeansHome New value of property netbeansHome.
     */
    public void setNetbeansHome(File netbeansHome) {
        File old=this.netbeansHome;
        this.netbeansHome = netbeansHome;
        firePropertyChange("netbeansHome", old, netbeansHome); // NOI18N
    }
    
    /** Getter for property xtestHome.   
     * @return Value of property xtestHome.
     */
    public File getXtestHome() {
        return this.xtestHome;
    }
    
    /** Setter for property xtestHome.
     * @param xtestHome New value of property xtestHome.
     */
    public void setXtestHome(File xtestHome) {
        File old=this.xtestHome;
        this.xtestHome = xtestHome;
        firePropertyChange("xtestHome", old, xtestHome); // NOI18N
    }
    
    /** Getter for property jemmyHome.
     * @return Value of property jemmyHome.
     */
    public File getJemmyHome() {
        return this.jemmyHome;
    }
    
    /** Setter for property jemmyHome.
     * @param jemmyHome New value of property jemmyHome.
     */
    public void setJemmyHome(File jemmyHome) {
        File old=this.jemmyHome;
        this.jemmyHome = jemmyHome;
        firePropertyChange("jemmyHome", old, jemmyHome); // NOI18N
    }
    
    /** Getter for property jellyHome.
     * @return Value of property jellyHome.
     */
    public File getJellyHome() {
        return this.jellyHome;
    }
    
    /** Setter for property jellyHome.
     * @param jellyHome New value of property jellyHome.
     */
    public void setJellyHome(File jellyHome) {
        File old=this.jellyHome;
        this.jellyHome = jellyHome;
        firePropertyChange("jellyHome", old, jellyHome); // NOI18N
    }
    
    /** Getter for property testType.
     * @return Value of property testType.
     */
    public String getTestType() {
        return this.testType;
    }
    
    /** Setter for property testType.
     * @param testType New value of property testType.
     */
    public void setTestType(String testType) {
        String old=this.testType;
        this.testType = testType;
        firePropertyChange("testType", old, testType); // NOI18N
    }
    
    /** Getter for property attributes.
     * @return Value of property attributes.
     */
    public String getAttributes() {
        return this.attributes;
    }
    
    /** Setter for property attributes.
     * @param attributes New value of property attributes.
     */
    public void setAttributes(String attributes) {
        String old=this.attributes;
        this.attributes = attributes;
        firePropertyChange("attributes", old, attributes); // NOI18N
    }
   
    private Properties getProperties() {
        Properties props=new Properties();
        if (netbeansHome!=null)
            props.setProperty("netbeans.home", netbeansHome.getAbsolutePath()); // NOI18N
        if (xtestHome!=null)
            props.setProperty("xtest.home", xtestHome.getAbsolutePath()); // NOI18N
        if (jemmyHome!=null)
            props.setProperty("jemmy.home", jemmyHome.getAbsolutePath()); // NOI18N
        if (jellyHome!=null)
            props.setProperty("jelly.home", jellyHome.getAbsolutePath()); // NOI18N
        if (testType!=null && !testType.equals("")) // NOI18N
            props.setProperty("xtest.testtype", testType); // NOI18N
        if (attributes!=null && !attributes.equals("")) // NOI18N
            props.setProperty("xtest.attribs", attributes); // NOI18N
        return props;
    } 
    
    /** Getter for property showResults.
     * @return Value of property showResults.
     */
    public boolean isShowResults() {
        return this.showResults;
    }
    
    /** Setter for property showResults.
     * @param showResults New value of property showResults.
     */
    public void setShowResults(boolean showResults) {
        Boolean old=new Boolean(this.showResults);
        this.showResults = showResults;
        firePropertyChange("showResults", old, new Boolean(showResults)); // NOI18N
    }
    
}
