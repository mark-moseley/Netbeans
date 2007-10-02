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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.jboss4.config.mdb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Libor Kotouc
 */
public final class JBossMessageDestinationDeployment implements MessageDestinationDeployment {
    
//    private static final String DSdotXML = "-ds.xml"; // NOI18N
//    private static final String JBossDSdotXML = "jboss-ds.xml"; // NOI18N
    
    // server's deploy dir
    private FileObject deployDir;
    
    public JBossMessageDestinationDeployment(String serverUrl) {
        String serverDirPath = InstanceProperties.getInstanceProperties(serverUrl).
                                        getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        deployDir = FileUtil.toFileObject(new File(serverDirPath));
    }
    
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        HashSet<MessageDestination> destinations = new HashSet<MessageDestination>();
        destinations.add(new JBossMessageDestination("SampleServerQueue", MessageDestination.Type.QUEUE));
        destinations.add(new JBossMessageDestination("SampleServerTopic", MessageDestination.Type.TOPIC));
        return destinations;
    }

    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
    }
    
    
/*  
    public Set<Datasource> getDatasources() throws ConfigurationException {
        
        Set<Datasource> datasources = new HashSet<Datasource>();
        
        if (deployDir == null || !deployDir.isValid() || !deployDir.isFolder() || !deployDir.canRead()) {
            ErrorManager.getDefault().log(ErrorManager.USER, 
                    NbBundle.getMessage(JBossMessageDestinationDeployment.class, "ERR_WRONG_DEPLOY_DIR"));
            return datasources;
        }
        
        Enumeration files = deployDir.getChildren(true);
        List<FileObject> confs = new LinkedList<FileObject>();
        while (files.hasMoreElements()) { // searching for config files with DS
            FileObject file = (FileObject) files.nextElement();
            if (!file.isFolder() && file.getNameExt().endsWith(DSdotXML) && file.canRead())
                confs.add(file);
        }
        
        if (confs.size() == 0) // nowhere to search
            return datasources;

        for (Iterator it = confs.iterator(); it.hasNext();) {
            FileObject dsFO = (FileObject)it.next();
            File dsFile = FileUtil.toFile(dsFO);
            try {
                Datasources ds = null;
                try {
                    ds = Datasources.createGraph(dsFile);
                } catch (RuntimeException re) {
                    // most likely not a data source (e.g. jms-ds.xml in JBoss 5.x)
                    String msg = NbBundle.getMessage(JBossMessageDestinationDeployment.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, msg);
                    continue;
                }
                LocalTxDatasource ltxds[] = ds.getLocalTxDatasource();
                for (int i = 0; i < ltxds.length; i++) {
                    if (ltxds[i].getJndiName().length() > 0) {
                        datasources.add(new JBossDatasource(
                                    ltxds[i].getJndiName(),
                                    ltxds[i].getConnectionUrl(),
                                    ltxds[i].getUserName(),
                                    ltxds[i].getPassword(),
                                    ltxds[i].getDriverClass()));
                    }
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(JBossMessageDestinationDeployment.class, "MSG_CannotReadDatasources", dsFile.getAbsolutePath());
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(JBossMessageDestinationDeployment.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                throw new ConfigurationException(msg ,re);
            }
        }
        
        return datasources;
    }

    public void deployDatasources(Set<Datasource> datasources) 
    throws ConfigurationException, DatasourceAlreadyExistsException 
    {
        Set<Datasource> deployedDS = getDatasources();
        Map<String, Datasource> ddsMap = transform(deployedDS); // for faster searching
        
        HashMap<String, Datasource> newDS = new HashMap<String, Datasource>(); // will contain all ds which do not conflict with existing ones
        
        //resolve all conflicts
        LinkedList<Datasource> conflictDS = new LinkedList<Datasource>();
        for (Iterator<Datasource> it = datasources.iterator(); it.hasNext();) {
            Object o = it.next();
            if (!(o instanceof JBossDatasource))
                continue;
            JBossDatasource ds = (JBossDatasource)o;
            String jndiName = ds.getJndiName();
            if (ddsMap.keySet().contains(jndiName)) { // conflicting ds found
                if (!ddsMap.get(jndiName).equals(ds)) { // found ds is not equal
                    conflictDS.add(ddsMap.get(jndiName)); // NOI18N
                }
            }
            else if (jndiName != null) {
                newDS.put(jndiName, ds);
            }
        }
        
        if (conflictDS.size() > 0) { // conflict found -> exception
            throw new DatasourceAlreadyExistsException(conflictDS);
        }
        
        //write jboss-ds.xml
        FileObject dsXmlFo = serverDir.getFileObject(JBossDSdotXML);
        File dsXMLFile = (dsXmlFo != null ? FileUtil.toFile(dsXmlFo) : null);

        Datasources deployedDSGraph = null;
        try {
            deployedDSGraph = (dsXMLFile != null ? Datasources.createGraph(dsXMLFile) : new Datasources());
        }
        catch (IOException ioe) {
            ErrorManager.getDefault().annotate(ioe, NbBundle.getMessage(getClass(), "ERR_CannotReadDSdotXml"));
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return;
        }

        //merge ds graph with newDS - remove conflicting ds from graph
        LocalTxDatasource ltxds[] = deployedDSGraph.getLocalTxDatasource();
        for (int i = 0; i < ltxds.length; i++) {
            String jndiName = ltxds[i].getJndiName();
            if (newDS.keySet().contains(jndiName)) //conflict, we must remove it from graph
                deployedDSGraph.removeLocalTxDatasource(ltxds[i]);
        }
        
        //add all ds from newDS
        for (Iterator it = newDS.values().iterator(); it.hasNext();) {
            JBossDatasource ds = (JBossDatasource) it.next();
            
            LocalTxDatasource lds = new LocalTxDatasource();
            lds.setJndiName(ds.getJndiName());
            lds.setConnectionUrl(ds.getUrl());
            lds.setDriverClass(ds.getDriverClassName());
            lds.setUserName(ds.getUsername());
            lds.setPassword(ds.getPassword());
            lds.setMinPoolSize(ds.getMinPoolSize());
            lds.setMaxPoolSize(ds.getMaxPoolSize());
            lds.setIdleTimeoutMinutes(ds.getIdleTimeoutMinutes());

            deployedDSGraph.addLocalTxDatasource(lds);
        }
        
        //write modified graph into jboss-ds.xml
        if (newDS.size() > 0) {
            if (dsXMLFile == null) {
                try {
                    dsXmlFo = serverDir.createData(JBossDSdotXML);
                }
                catch (IOException ioe) {
                    ErrorManager.getDefault().annotate(ioe, NbBundle.getMessage(getClass(), "ERR_CannotCreateDSdotXml"));
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    return;
                }
                
                dsXMLFile = FileUtil.toFile(dsXmlFo);
            }
            
            writeFile(dsXMLFile, deployedDSGraph);            
        }        
        
    }
    
    private Map<String, Datasource> transform(Set<Datasource> datasources) {
        HashMap<String, Datasource> map = new HashMap<String, Datasource>();
        for (Iterator it = datasources.iterator(); it.hasNext();) {
            JBossDatasource ds = (JBossDatasource) it.next();
            if (ds.getJndiName() != null)
                map.put(ds.getJndiName(), ds);
        }
        return map;
    }
*/
    private void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        try {

            FileSystem fs = deployDir.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = deployDir.getFileObject(name);
                        if (configFO == null) {
                            configFO = deployDir.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4096);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try { os.close(); } catch(IOException ioe) {}
                        }
                        if (lock != null) 
                            lock.releaseLock();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }

}
