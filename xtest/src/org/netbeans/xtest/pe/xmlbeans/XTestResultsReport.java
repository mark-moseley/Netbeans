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
 * XTestReport.java
 *
 * Created on November 19, 2001, 4:45 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

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
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    public boolean  xmlat_fullReport;
    
    // child elements
    public SystemInfo[] xmlel_SystemInfo;
    public TestRun[] xmlel_TestRun;
    
}
