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


/*
 * XTestReport.java
 *
 * Created on November 19, 2001, 4:45 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

import java.io.*;

/**
 *
 * @author  mb115822
 * @version 
 */
public class XTestResultsReport extends XMLBean {
    
    /** Creates new XTestReport */
    public XTestResultsReport() {
    }

    // XML attributes
    public java.sql.Timestamp     xmlat_timeStamp;
    public long     xmlat_time;
    //public String   xmlat_attributes;
    public String   xmlat_project;
    public String   xmlat_build;
    public String   xmlat_testingGroup;
    public String   xmlat_testedType;
    public String   xmlat_host;
    public String   xmlat_comment;    
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    public boolean  xmlat_fullReport;
    // only for compatibility reasons -> should to remove it
    public String   xmlat_platform;
    // project_id - project_id of the report in the database 
    public String   xmlat_project_id;
    // link - link to local pes -> this is not full link, but only the part from         
    // root of team web server
    public String   xmlat_webLink;
    // team - id of team which submitted the results
    public String   xmlat_team;
    
    // child elements
    public SystemInfo[] xmlel_SystemInfo;
    public TestRun[] xmlel_TestRun;
    
    /** Holds value of property systemInfo_id. */
    private long systemInfo_id;    
    
    // business methods
    public boolean isValid() {
        if (xmlat_project == null) return false;
        if (xmlat_build == null) return false;
        if (xmlat_testingGroup == null) return false;
        if (xmlat_testedType == null) return false;
        if (xmlat_host == null) return false;        
        if (xmlat_testsTotal < 1) return false;
        if (xmlel_SystemInfo == null || xmlel_SystemInfo.length < 1) return false;
        return true;
    }
    
    public boolean equals(Object obj) {
        return equalByAttributes(obj);
    }
    
    /** Getter for property timeStamp.
     * @return Value of property timeStamp.
     */
    public java.sql.Timestamp getTimeStamp() {
        return xmlat_timeStamp;
    }
    
    /** Setter for property timeStamp.
     * @param timeStamp New value of property timeStamp.
     */
    public void setTimeStamp(java.sql.Timestamp timeStamp) {
        xmlat_timeStamp = timeStamp;
    }
    
    /** Getter for property time.
     * @return Value of property time.
     */
    public long getTime() {
        return xmlat_time;
    }
    
    /** Setter for property time.
     * @param time New value of property time.
     */
    public void setTime(long time) {
        xmlat_time = time;
    }
    
    /** Getter for property build.
     * @return Value of property build.
     */
    public String getBuild() {
        return xmlat_build;
    }
    
    /** Setter for property build.
     * @param build New value of property build.
     */
    public void setBuild(String build) {
        xmlat_build = build;
    }
    
    /** Getter for property comment.
     * @return Value of property comment.
     */
    public String getComment() {
        return xmlat_comment;
    }
    
    /** Setter for property comment.
     * @param comment New value of property comment.
     */
    public void setComment(String comment) {
        xmlat_comment = comment;
    }
    
    /** Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return xmlat_host;
    }
    
    /** Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        xmlat_host = host;
    }
    
    /** Getter for property platform.
     * @return Value of property platform.
     */
    public String getPlatform() {
        return xmlat_platform;
    }
    
    /** Setter for property platform.
     * @param platform New value of property platform.
     */
    public void setPlatform(String platform) {
        xmlat_platform = platform;
    }
    
    /** Getter for property project.
     * @return Value of property project.
     */
    public String getProject() {
        return xmlat_project;
    }
    
    /** Setter for property project.
     * @param project New value of property project.
     */
    public void setProject(String project) {
        xmlat_project = project;
    }
    
    /** Getter for property testedType.
     * @return Value of property testedType.
     */
    public String getTestedType() {
        return xmlat_testedType;
    }
    
    /** Setter for property testedType.
     * @param testedType New value of property testedType.
     */
    public void setTestedType(String testedType) {
        xmlat_testedType = testedType;
    }
    
    /** Getter for property testingGroup.
     * @return Value of property testingGroup.
     */
    public String getTestingGroup() {
        return xmlat_testingGroup;
    }
    
    /** Setter for property testingGroup.
     * @param testingGroup New value of property testingGroup.
     */
    public void setTestingGroup(String testingGroup) {
        xmlat_testingGroup = testingGroup;
    }
    
    /** Getter for property testsError.
     * @return Value of property testsError.
     */
    public long getTestsError() {
        return xmlat_testsError;
    }
    
    /** Setter for property testsError.
     * @param testsError New value of property testsError.
     */
    public void setTestsError(long testsError) {
        xmlat_testsError = testsError;
    }
    
    /** Getter for property testsFail.
     * @return Value of property testsFail.
     */
    public long getTestsFail() {
        return xmlat_testsFail;
    }
    
    /** Setter for property testsFail.
     * @param testsFail New value of property testsFail.
     */
    public void setTestsFail(long testsFail) {
        xmlat_testsFail = testsFail;
    }
    
    /** Getter for property testsPass.
     * @return Value of property testsPass.
     */
    public long getTestsPass() {
        return xmlat_testsPass;
    }
    
    /** Setter for property testsPass.
     * @param testsPass New value of property testsPass.
     */
    public void setTestsPass(long testsPass) {
        xmlat_testsPass = testsPass;
    }
    
    /** Getter for property testsTotal.
     * @return Value of property testsTotal.
     */
    public long getTestsTotal() {
        return xmlat_testsTotal;
    }
    
    /** Setter for property testsTotal.
     * @param testsTotal New value of property testsTotal.
     */
    public void setTestsTotal(long testsTotal) {
        xmlat_testsTotal = testsTotal;
    }
    
    /** Getter for property systemInfo_id.
     * @return Value of property systemInfo_id.
     */
    public long getSystemInfo_id() {
        return this.systemInfo_id;
    }
    
    /** Setter for property systemInfo_id.
     * @param systemInfo_id New value of property systemInfo_id.
     */
    public void setSystemInfo_id(long systemInfo_id) {
        this.systemInfo_id = systemInfo_id;
    }
    
    /** getter for weblink 
     */
    public void setWebLink(String webLink) {
        xmlat_webLink = webLink;
    }

    /** setter for weblink 
     */
    public String getWebLink() {
        return xmlat_webLink;
    }
    
    public void setProject_id(String project_id) {
        this.xmlat_project_id = project_id;
    }
    
    public String getProject_id() {
        return this.xmlat_project_id;
    }
    
    /** getter for team
    */
    public void setTeam(String team) {
        xmlat_team = team;
    }

    /** setter for weblink 
     */
    public String getTeam() {
        return xmlat_team;
    }
    
    
    
    // load XTestResultsReport from a file
    /** this one should be deprecated as well :-(
     * @param reportFile
     * @throws IOException
     * @throws ClassNotFoundException
     * @return
     */    
    public static XTestResultsReport loadFromFile(File reportFile) throws IOException, ClassNotFoundException {
        XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
        if (!(xmlBean instanceof XTestResultsReport)) {
            throw new ClassNotFoundException("Loaded file "+reportFile+" does not contain XTestRestultsReport");
        }
        return (XTestResultsReport)xmlBean;
    }
    
    public static XTestResultsReport loadXTestResultsReportFromFile(File reportFile) throws IOException {
        try {
            XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
            if (!(xmlBean instanceof XTestResultsReport)) {
                throw new ClassNotFoundException("Loaded file "+reportFile+" does not contain XTestRestultsReport");
            }
            return (XTestResultsReport)xmlBean;
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Loaded file "+reportFile+" does not contain XTestRestultsReport, caused by ClassNotFoundException :"+cnfe.getMessage());
        }
    }    
    
    // old method name - should be deprecated
    /**
     * @param reportFile
     * @throws IOException
     * @throws ClassNotFoundException
     * @return
     * @deprecated
     */    
    public static XTestResultsReport loadReportFromFile(File reportFile) throws IOException, ClassNotFoundException {
        return loadFromFile(reportFile);
    }
    
}
