/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestBag.java
 *
 * Created on November 1, 2001, 6:20 PM
 */

package org.netbeans.xtest.pe.xmlbeans;


import java.io.*;

/**
 *
 * @author  mb115822
 */
public class TestBag extends XMLBean {

    /** Creates new TestBag */
    public TestBag() {
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
    
    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return xmlat_name;
    }    
    
    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        xmlat_name = name;
    }
    
    /** Getter for property module.
     * @return Value of property module.
     */
    public String getModule() {
        return xmlat_module;
    }
    
    /** Setter for property module.
     * @param module New value of property module.
     */
    public void setModule(String module) {
        xmlat_module = module;
    }
    
    /** Getter for property testType.
     * @return Value of property testType.
     */
    public String getTestType() {
        return xmlat_testType;
    }
    
    /** Setter for property testType.
     * @param testType New value of property testType.
     */
    public void setTestType(String testType) {
        xmlat_testType = testType;
    }
    
    /** Getter for property executor.
     * @return Value of property executor.
     */
    public String getExecutor() {
        return xmlat_executor;
    }
    
    /** Setter for property executor.
     * @param executor New value of property executor.
     */
    public void setExecutor(String executor) {
        xmlat_executor = executor;
    }
    
    /** Getter for property testAttribs.
     * @return Value of property testAttribs.
     */
    public String getTestAttribs() {
        return xmlat_testAttribs;
    }
    
    /** Setter for property testAttribs.
     * @param testAttribs New value of property testAttribs.
     */
    public void setTestAttribs(String testAttribs) {
        xmlat_testAttribs = testAttribs;
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
    
    /** Getter for property bagID.
     * @return Value of property bagID.
     */
    public String getBagID() {
        return xmlat_bagID;
    }
    
    /** Setter for property bagID.
     * @param bagID New value of property bagID.
     */
    public void setBagID(String bagID) {
        xmlat_bagID = bagID;
    }
    
    /** Getter for property unexpectedFailure.
     * @return Value of property unexpectedFailure.
     */
    public String getUnexpectedFailure() {
        return xmlat_unexpectedFailure;    
    }
    
    /** Setter for property unexpectedFailure.
     * @param unexpectedFailure New value of property unexpectedFailure.
     */
    public void setUnexpectedFailure(String unexpectedFailure) {
        xmlat_unexpectedFailure = unexpectedFailure;
    }
    
    /** Getter for property testRun_id.
     * @return Value of property testRun_id.
     */
    public long getTestRun_id() {
        return this.testRun_id;
    }
    
    /** Setter for property testRun_id.
     * @param testRun_id New value of property testRun_id.
     */
    public void setTestRun_id(long testRun_id) {
        this.testRun_id = testRun_id;
    }
    
    // attributes
    public String   xmlat_name;
    public String   xmlat_module;
    public String   xmlat_testType;
    public String   xmlat_executor;
    public String   xmlat_testAttribs;    
    public java.sql.Timestamp     xmlat_timeStamp;
    public long     xmlat_time;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;

    public boolean  xmlat_ideUserDir = false;
    
    public String   xmlat_bagID;
    
    public String   xmlat_unexpectedFailure;
    
    // child elements
    public UnitTestSuite[] xmlel_UnitTestSuite;
    
    /** Holds value of property testRun_id. */
    private long testRun_id;
    
    
    // load TestBag from a file
    public static TestBag loadFromFile(File aFile) throws IOException, ClassNotFoundException {
        XMLBean xmlBean = XMLBean.loadXMLBean(aFile);
        if (!(xmlBean instanceof TestBag)) {
            throw new ClassNotFoundException("Loaded file "+aFile+" does not contain TestBag");
        }
        return (TestBag)xmlBean;
    }
    
    
}
