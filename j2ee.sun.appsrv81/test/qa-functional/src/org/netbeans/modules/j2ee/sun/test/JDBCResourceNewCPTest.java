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
/*
 * JDBCResourceNewCPTest.java
 *
 * Created on March 21, 2006, 4:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Vector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Amanpreet Kaur
 */
public class JDBCResourceNewCPTest extends NbTestCase implements WizardConstants{
    
    private static String CONNECTION_POOL_NAME = "poolTest";
    private static String DATA_RESOURCE_NAME="resourceTest";
    
    
    
    /** Creates a new instance of JDBCResourceNewCPTest */
    public JDBCResourceNewCPTest(String testName) {
        
        super(testName);
    }
    
    public void registerDataResource() {
        try {
            ServerInstance    inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //Project    project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData dsdata = new ResourceConfigData();
            ResourceConfigData cpdata = new ResourceConfigData();
            //connection pool settings
            cpdata.setProperties(new Vector());
            cpdata.addProperty(__DatabaseVendor, "derby_net");
            cpdata.addProperty(__User, "app");
            cpdata.addProperty(__Password,"app");
            cpdata.addProperty(__ServerName,"localhost");
            cpdata.addProperty(__DerbyPortNumber,"1527");
            cpdata.addProperty(__DatabaseName,"sample");
            cpdata.setString(__Name, CONNECTION_POOL_NAME);
            cpdata.setString(__ResType, "javax.sql.DataSource");
            cpdata.setString(__DatasourceClassname, "org.apache.derby.jdbc.ClientDataSource");
            cpdata.setString(__SteadyPoolSize, "8");
            cpdata.setString(__MaxPoolSize, "32");
            cpdata.setString(__MaxWaitTimeInMillis, "60000");
            cpdata.setString(__PoolResizeQuantity, "2");
            cpdata.setString(__IdleTimeoutInSeconds, "300");
            //datasouce settings
            dsdata.setString(__JndiName,DATA_RESOURCE_NAME);
            dsdata.setString(__Enabled, "true");
            dsdata.setString(__JdbcObjectType, "user");
            dsdata.setString(__PoolName,CONNECTION_POOL_NAME);
//            dsdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            dsdata.setTargetFileObject(falseProject);
            dsdata.setTargetFile("resourceTest");
            cpdata.setTargetFileObject(falseProject);
            cpdata.setTargetFile("poolTest");
            ResourceUtils.saveJDBCResourceDatatoXml(dsdata,cpdata);
            SunResourceDataObject resourceObjds = (SunResourceDataObject)SunResourceDataObject.find(falseProject.getFileObject("setup/resourceTest.sun-resource"));
            Resources resds = Util.getResourcesObject(resourceObjds);
            SunResourceDataObject resourceObjcp = (SunResourceDataObject)SunResourceDataObject.find(falseProject.getFileObject("setup/poolTest.sun-resource"));
            Resources rescp = Util.getResourcesObject(resourceObjcp);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(rescp.getJdbcConnectionPool(0), mejb, false);
            ResourceUtils.register(resds.getJdbcResource(0), mejb, false);
            resourceObjds.delete();
            resourceObjcp.delete();
            Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] dataResources =Util.getResourcesNames("getJdbcResource","jndi-name",mejb);
            for(int i=0;i<dataResources.length;i++) {
                if(dataResources[i].equals(DATA_RESOURCE_NAME))
                    return;
            }
            
            throw new Exception("Data Resource hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterDataResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] commandds = new String[] {"delete-jdbc-resource", "--user", "admin",  DATA_RESOURCE_NAME};
            String[] commandcp = new String[] {"delete-jdbc-connection-pool", "--user", "admin", CONNECTION_POOL_NAME};
            Process p=Util.runAsadmin(commandds);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
           Process pc=Util.runAsadmin(commandcp);
            Util.sleep(Util.SLEEP);
            BufferedReader errorcp = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
            errorMess = error.readLine();
            BufferedReader inputcp = new BufferedReader(new InputStreamReader(pc.getInputStream()));
            String outputcp=inputcp.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+outputcp);
            System.out.println(outputcp);
            Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] dataRes = Util.getResourcesNames("getJdbcResource", "jndi-name", mejb);
            for(int i=0;i<dataRes.length;i++) {
                if(dataRes[i].equals(DATA_RESOURCE_NAME))
                    throw new Exception("Data Resource hasn't been removed !");}
            String[] connPools = Util.getResourcesNames("getJdbcConnectionPool", "name", mejb);
            for(int i=0;i<connPools.length;i++) {
                if(connPools[i].equals(CONNECTION_POOL_NAME))
                    throw new Exception("Connection Pool hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("JDBCResourceNewCPTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new StartStopServerTest("startServer"));
        suite.addTest(new JDBCResourceNewCPTest("registerDataResource"));
        suite.addTest(new JDBCResourceNewCPTest("unregisterDataResource"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}