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
 * PES_Config.java
 *
 * Created on May 14, 2002, 9:34 AM
 */

package org.netbeans.xtest.pes.xmlbeans;

import java.io.*;
import org.w3c.dom.*;
import org.netbeans.xtest.pe.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.util.*;

// mailer stuff
import org.netbeans.xtest.pes.PESMailer;

// logging stuff
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;

/**
 *
 * @author  breh
 */
public class PESConfig extends XMLBean {

    /** Creates a new instance of PES_Config */
    public PESConfig() {
    }

    // attributes
    // incoming directory for PES
    public String xmlat_incomingDir;
    // working directory for PES
    public String xmlat_workDir;
    // name of the team who owns this PES
    public String xmlat_team;
    
    // default logging level 
    private static final String DEFAULT_LOGGING_LEVEL = "WARNING";
    // logging level for PES - default is warning
    public String xmlat_loggingLevel = DEFAULT_LOGGING_LEVEL;
    
    // path where to upload results for storing in database
    public String xmlat_databaseUploadPath;
    

    // project group
    public PESWeb xmlel_PESWeb[];
    // mail information
    public Mail xmlel_Mail[];
    
    // getters/setters
    public PESWeb[] getPESWebs() {
        return xmlel_PESWeb;
    }
    
    // return all webs of given type, if inverse return all but the specified ones
    public PESWeb[] getPESWebs(String webtype, boolean inverse) {
        if (xmlel_PESWeb == null) return new PESWeb[0];
        Collection webs = new ArrayList();
        for (int i=0 ; i<xmlel_PESWeb.length ; i++) {
            if (xmlel_PESWeb[i].isType(webtype) != inverse) {
                webs.add(xmlel_PESWeb[i]);
            }
        }
        return (PESWeb[])(webs.toArray(new PESWeb[0]));
    }
    
    // return the team name
    public String getTeam() {
        return xmlat_team;
    }
    
    // constants
    
    // incoming dir
    private static final String INCOMING_REPLACE = "replace";
    private static final String INCOMING_INVALID = "invalid";
    
    // work dir
    private static final String WORK_PROCESSING = "processing";
    private static final String WORK_DB_UPLOADS = "uploads";
    
    
    // bussiness methods
    
    public static PESConfig loadConfig(String configFilename) throws IOException, ClassNotFoundException, PESConfigurationException {
        File configFile = new File(configFilename);
        return loadConfig(configFile);
    }
    
    public static PESConfig loadConfig(File configFile) throws IOException, ClassNotFoundException, PESConfigurationException {
        XMLBean xmlBean = XMLBean.loadXMLBean(configFile);        
        PESLogger.logger.fine("Config file:"+configFile+" loaded");
        if (!(xmlBean instanceof PESConfig)) {
            throw new ClassNotFoundException("File "+configFile+" is not of required type - PESConfig required");
        } else {
            PESConfig config = (PESConfig)xmlBean;
            config.finishInitialization();
            config.checkValidity();
            PESLogger.logger.fine("Config file seems to be valid");
            return config;
        }
        
    }
    
    // return true only if valid
    public void checkValidity() throws PESConfigurationException {
        if (xmlat_incomingDir == null) throw new PESConfigurationException("PESConfig: incomingDir is not set");
        if (xmlat_workDir == null) throw new PESConfigurationException("PESConfig: workDir is not set");
        if ((xmlel_PESWeb == null) | (xmlel_PESWeb.length == 0)) throw new PESConfigurationException("PESConfig: no PESWeb elements are defined");
        // just for the time being - only one PESWeb is supported
        /*
        if (xmlel_PESWeb.length != 1) {
            throw new PESConfigurationException("PESConfig: PESConfig can contain only one instance of PESWeb");
        }
         */
        int mainCount = 0;        
        for (int i=0; i < xmlel_PESWeb.length; i++) {
            if (xmlel_PESWeb[i].isType(PESWeb.MAIN)) {
                mainCount++;
            }
            xmlel_PESWeb[i].checkValidity();
        }
        
        if (mainCount == 0)  throw new PESConfigurationException("PESConfig: no main PESWeb is defined");
        if (mainCount > 1)  throw new PESConfigurationException("PESConfig: only one main PESWeb can be defined");
        
        // mail
        if (xmlel_Mail != null) {
            if (xmlel_Mail.length==1) {
                if (xmlel_Mail[0] != null) {
                    xmlel_Mail[0].checkValidity();
                }
            }
        }
    }
    
    public static PESConfig getDefaultConfig() {
           
        // create config
        PESConfig config = new PESConfig();        
        // create projects element        
        PESWeb webs[] = { new PESWeb() };
        
        config.xmlel_PESWeb = webs;
        
        // create empty groups
        PESProjectGroup groups[] = new PESProjectGroup[0];;
        webs[0].xmlel_PESProjectGroup = groups; 
        
        config.finishInitialization();        
        return config;
    }    
    
    
    
    // this is not good !!!!
    /**
     * @return
     * @deprecated needs to be done
     *
     */
    public PESProjectGroup[] getProjectGroups() {
        return xmlel_PESWeb[0].xmlel_PESProjectGroup;
    }
    
    
    public String[] getSecondaryProjects() {
        PESProjectGroup groups[] = getProjectGroups();
        Collection list = new ArrayList();
        for (int i=0; i < groups.length ; i++) {
            PESProject projects[] = groups[i].xmlel_PESProject;
            if ( (!groups[i].isMain()) & (projects != null) ) {                
                for (int j=0; j < projects.length; j++) {
                    String aProject = projects[j].xmlat_project;
                    if (aProject != null) {
                        list.add(aProject);
                    }
                }
            }            
        }
        return (String[])(list.toArray(new String[0]));
    }
    
    
        public File getIncomingDir() throws IOException {
        String dirName = xmlat_incomingDir;
        if (dirName == null) {
            throw new IOException("Incoming dir is not specified in config");
        }
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            boolean result = dir.mkdirs();
            if (result == false) {
                throw new IOException("Incoming dir "+dirName+" does not exist and cannot be created");
            }
        }
        return dir;
    }
    
    public File getIncomingReplaceDir() throws IOException {
        File incoming = getIncomingDir();
        File replaceDir = new File(incoming,PESConfig.INCOMING_REPLACE);
        if (!replaceDir.isDirectory()) {
            boolean result = replaceDir.mkdirs();
            if (result == false) {
                throw new IOException("Replace dir "+replaceDir+" does not exist and cannot be created");
            }
        }
        return replaceDir;
    }
    
    public File getIncomingInvalidDir() throws IOException {
        File incoming = getIncomingDir();
        File invalidDir = new File(incoming,PESConfig.INCOMING_INVALID);
        if (!invalidDir.isDirectory()) {
            boolean result = invalidDir.mkdirs();
            if (result == false) {
                throw new IOException("Invalid dir "+invalidDir+" does not exist and cannot be created");
            }
        }
        return invalidDir;
    }
    
    public PESMailer getPESMailer() {
        if (xmlel_Mail != null) {
            if ((xmlel_Mail.length == 1) & (xmlel_Mail[0] != null)) {
                return xmlel_Mail[0].getPESMailer();                
            }
        }
        return null;
    }
    
    // can this configutation send emails ?
    public boolean canUseEmails() {
        if (getPESMailer() != null) {
            return true;
        } else {
            return false;
        }
    }
    
    // get email logging level - if no mail support - return null;
    public String getEmailLoggingLevel() {
        if (getPESMailer() != null) {
            return xmlel_Mail[0].getLoggingLevel();
        } else {
            return null;
        }
    }
    
    // get configured logging level
    public String getLoggingLevel() {
        if (xmlat_loggingLevel != null) {
            return xmlat_loggingLevel;
        }
        return DEFAULT_LOGGING_LEVEL;
    }
    
    
    public File getWorkdir() throws IOException {
        String dirName = xmlat_workDir;
        if (dirName == null) {
            throw new IOException("Work dir is not specified in config");
        }
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            boolean result = dir.mkdirs();
            if (result == false) {
                throw new IOException("Work dir "+dirName+" does not exist and cannot be created");
            }
        }
        return dir;
    }
    
    public File getDBUploadsWorkdir() throws IOException {
        return checkOrCreateWorkdir(new File(getWorkdir(),WORK_DB_UPLOADS));
    }
    
    public File getProcessingWorkdir() throws IOException {        
        return checkOrCreateWorkdir(new File(getWorkdir(),WORK_PROCESSING));
    }
    
    /** checks whether a supplied directory exists. If not, then it tries 
     * to create it. 
     * @param workDir - working directory to be checked/created
     */
    private File checkOrCreateWorkdir(File workDir) throws IOException {
        if (!workDir.isDirectory()) {
            boolean result = workDir.mkdirs();
            if (result == false) {
                throw new IOException("work dir "+workDir.getPath()+" does not exist and cannot be created");
            }
        }
        return workDir;             
    }
    
    private void finishInitialization() {
        if (xmlel_PESWeb != null) {
            for (int i=0; i<xmlel_PESWeb.length; i++) {
                if (xmlel_PESWeb[i] != null) {
                    xmlel_PESWeb[i].finishInitialization(this);
                }
            }
        }
        if (xmlel_Mail != null) {
            xmlel_Mail[0].finishInitialization();
        }
    }
    
    /** Getter for property xmlat_databaseUploadPath.
     * @return Value of property xmlat_databaseUploadPath.
     *
     */
    public java.lang.String getDatabaseUploadPath() {
        
        return xmlat_databaseUploadPath;
    }   
    
    public File getDatabaseUploadDir() throws IOException {
        // finally set uploadToDatabase = false, if there is not databaseUploadPath specified in the config object
        if (getDatabaseUploadPath() == null) {
                throw new IOException("Database upload directory is not specified");
        } else {
            // check whether the directory exists and is r/w for this machine
            File uploadDir = new File(getDatabaseUploadPath());
            if ((!uploadDir.isDirectory())|(!uploadDir.canWrite())) {
                PESLogger.logger.warning("databaseUploadPath points to non-existent (or non writtable) directory:"+uploadDir.getAbsolutePath()+", disabling upload functionality");
                throw new IOException("Database upload directory "+uploadDir.getPath()+" is not a directory, or is has not write access");
            }
            return uploadDir;
        }
    }
    

    

}
