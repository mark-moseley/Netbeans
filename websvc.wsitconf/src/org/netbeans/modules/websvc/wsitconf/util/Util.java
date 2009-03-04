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

package org.netbeans.modules.websvc.wsitconf.util;

import java.util.logging.Logger;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.swing.JComponent;
import java.util.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.namespace.QName;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.ui.service.BindingPanel;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWsStackProvider;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class Util {

    public static final String MEX_CLASS_NAME = "com.sun.xml.ws.mex.server.MEXEndpoint";
    public static final String MEX_NAME = "MEXEndpoint";

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        ArrayList<Component> allComponents = new ArrayList<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "No FileObject found for the following URL: " + urls[i]); //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptyList();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    /* Used to validate number inputs
     */
    public static boolean isPositiveNumber(String s, boolean zeroAllowed, boolean allowEmptyValue) {
        Integer i = null;
        if ((s == null) || ("".equals(s))) {
            return allowEmptyValue ? true : false;
        }
        try {
            i = Integer.parseInt(s);
            if (i != null) {
                if (zeroAllowed) {
                    return i.intValue() >= 0;
                }
                return i.intValue() > 0;
            }
        } catch (NumberFormatException nfe) {
            logger.log(Level.FINE, null, nfe); // just ignore
        }
        return false;
    }

    public static List<String> getAliases(String storePath, char[] password, String type) throws IOException {
        if ((storePath == null) || (type == null)) return null;
        FileInputStream iStream = null;
        try {
            File f = new File(storePath);
            if ((f == null) || (!f.exists())) {
                throw new IOException();
            }
            iStream = new FileInputStream(f);
            java.security.KeyStore keyStore;
            keyStore = java.security.KeyStore.getInstance(type);
            keyStore.load(iStream, password); 
            Enumeration<String> e = keyStore.aliases();
            ArrayList<String> arr = new ArrayList<String>(keyStore.size());
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                arr.add(key);
            }
            Collections.sort(arr);
            return arr;
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (KeyStoreException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (CertificateException ex) {
            logger.log(Level.INFO, null, ex);
        } finally {
            if (iStream != null) iStream.close();
        }
        return null;
    }

    public static final String getPassword(Project p) {
        J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            InstanceProperties ip = mp.getInstanceProperties();
            return ip.getProperty(InstanceProperties.PASSWORD_ATTR);
        }
        return "";
    }

    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                Object type = j2eeModule.getModuleType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.EJB.equals(type) && (version > 2.1)) {
                    return true;
                }
                if (J2eeModule.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.CLIENT.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isEqual(Object a, Object b) {
        if ((a == null) && (b == null)) return true;
        if ((a != null) && (b != null)) return a.equals(b);
        return false;
    }

    private static boolean isOperationInList(String operName, Collection<BindingOperation> operations) {
        Iterator<BindingOperation> i = operations.iterator();
        while (i.hasNext()) {
            BindingOperation bo = i.next();
            if ((bo != null) && (operName.equals(bo.getName()))) {
                return true;
            }
        }
        return false;
    }
    
    public static Collection<BindingOperation> refreshOperations(Binding binding, FileObject jc) {
        
        if (binding == null) {
            return null;
        }
        
        Collection<BindingOperation> operations = binding.getBindingOperations();
        if (jc == null) {
            return operations;
        }
        
        PortType pt = getPortType(binding);
        
        // create operations and add them to the binding element
        List<String> bindingOperationNames = JavaWsdlMapper.getOperationNames(jc);
        for (String name : bindingOperationNames) {
            if (!isOperationInList(name, operations)) {
                generateOperation(binding, pt, name, jc);
            }
        }
        
        return binding.getBindingOperations();
    }

    public static BindingOperation generateOperation(Binding binding, PortType portType, String operationName, FileObject implClass) {
        WSDLModel model = binding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        Definitions d = (Definitions) binding.getParent();

        BindingOperation bindingOperation;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            bindingOperation = wcf.createBindingOperation();
            bindingOperation.setName(operationName);
            binding.addBindingOperation(bindingOperation);

            // add input/output messages
            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
            inputMsg.setName(operationName);
            d.addMessage(inputMsg);

            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
            outMsg.setName(operationName + "Response");                  //NOI18N
            d.addMessage(outMsg);

            org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
            oper.setName(operationName);
            portType.addOperation(oper);

            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
            oper.setInput(input);
            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
            oper.setOutput(out);
            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.BindingOutput bindingOutput = wcf.createBindingOutput();
            bindingOperation.setBindingOutput(bindingOutput);
            org.netbeans.modules.xml.wsdl.model.BindingInput bindingInput = wcf.createBindingInput();
            bindingOperation.setBindingInput(bindingInput);

            //add faults
            List<String> operationFaults = JavaWsdlMapper.getOperationFaults(implClass, operationName);
            for (String fault : operationFaults) {
                org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                bindingOperation.addBindingFault(bindingFault);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
        return bindingOperation;
    }

    public static PortType getPortType(Binding binding) {
        Definitions d = (Definitions) binding.getParent();

        QName portTypeQName = binding.getType().getQName();
        PortType portType = null;
        
        Collection<PortType> portTypes = d.getPortTypes();
        Iterator<PortType> i = portTypes.iterator();
        while (i.hasNext()) {
            PortType pt = i.next();
            if (pt != null) {
                if (portTypeQName.getLocalPart().equals(pt.getName())) {
                    portType = pt;
                    break;
                }
            }
        }
        return portType;
    }
    
    public static FileObject getFOForModel(WSDLModel model) {
        if (model == null) return null;
        ModelSource ms = model.getModelSource();
        return Utilities.getFileObject(ms);
    }

    public static FileObject getSunDDFO(Project p) {
        J2eeModuleProvider provider = ServerUtils.getProvider(p);
        if (provider == null) return null;
        FileObject[] fobjs = provider.getConfigurationFiles();
        if (fobjs.length > 0) {
            return fobjs[0];
        }
        return null;
    }
    
    public static Servlet getServlet(WebApp wa, String className) {
        Servlet[] servlets = wa.getServlet();
        for (Servlet s : servlets) {
            if (className.equals(s.getServletClass())) {
                return s;
            }
        }
        return null;
    }

    public static void checkMetroLibrary(Project p) {
        WsitProvider wsitProvider = p.getLookup().lookup(WsitProvider.class);
        if (wsitProvider == null) return;
        if (!wsitProvider.isWsitSupported()) {
            Object button = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(Util.class, "TXT_AddLibraries"), NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.OK_OPTION.equals(button)) {
                wsitProvider.addMetroLibrary();
            }
        }
    }

    public static WSStackVersion getHighestWSStackVersion(Project project) {

        J2eePlatform platform = ServerUtils.getJ2eePlatform(project);

        WSStackVersion version = null;
        WSStack<JaxWs> wsStack = platform == null ? null : JaxWsStackProvider.getJaxWsStack(platform);

        if (wsStack != null) {
            version = wsStack.getVersion();
        }
        InputStream is = null;
        BufferedReader r = null;
        try {
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/version.properties"); //NOI18N
            if (wsimportFO != null && wsimportFO.isValid()) {
                is = wsimportFO.getInputStream();
                r = new BufferedReader(new InputStreamReader(is));
                String ln = null;
                String ver = null;
                while ((ln = r.readLine()) != null) {
                    String line = ln.trim();
                    if (line.startsWith("major-version=")) {        //NOI18N
                        ver = line.substring(14);break;
                    }
                }
                WSStackVersion projectVersion = WSStackFactory.createWSStackVersion(ver);
                if (projectVersion.compareTo(version) > 0) {
                    version = projectVersion;
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (r != null) r.close();
                if (is != null) is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return version;
    }

}
