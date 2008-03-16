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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.util;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URL;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.ParamStyle;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author nam
 */
public class SaasUtil {
    public static final String APPLICATION_WADL = "resources/application.wadl";
    public static final String DEFAULT_SERVICE_NAME = "Service";
    public static final String CATALOG = "catalog";
    
    

    public static <T> T loadJaxbObject(FileObject input, Class<T> type, boolean includeAware) throws IOException {
        if (input == null) {
            return null;
        }
        InputStream in = null;
        try {
            Exception jbex = null;
            try {
                in = input.getInputStream();
                T t = loadJaxbObject(in, type, includeAware);
                if (t != null) {
                    return t;
                }
            } catch (JAXBException ex) {
                jbex = ex;
            } catch (IOException ioe) {
                jbex = ioe;
            }
            String msg = NbBundle.getMessage(SaasUtil.class, "MSG_ErrorLoadingJaxb", type.getName(), input.getPath());
            IOException ioe = new IOException(msg);
            if (jbex != null) {
                ioe.initCause(jbex);
            }
            throw ioe;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static <T> T loadJaxbObject(InputStream in, Class<T> type) throws JAXBException {
        return loadJaxbObject(in, type, false);
    }
    
    public static <T> T loadJaxbObject(InputStream in, Class<T> type, boolean includeAware) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(type.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object o;
        //TODO fix claspath: http://www.jroller.com/navanee/entry/unsupportedoperationexception_this_parser_does_not
        includeAware = false;
        if (includeAware) {
            SAXSource ss = getSAXSourceWithXIncludeEnabled(in);
            o = unmarshaller.unmarshal(ss);
        } else {
            o = unmarshaller.unmarshal(in);
        }
        
        if (type.equals(o.getClass())) {
            return type.cast(o);
        } else if (o instanceof JAXBElement) {
            JAXBElement e = (JAXBElement) o;
            return type.cast(e.getValue());
        }

        throw new IllegalArgumentException("Expect: " + type.getName() + " get: " + o.getClass().getName());
    }

    public static SAXSource getSAXSourceWithXIncludeEnabled(InputStream in) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
//TODO: fix classpath http://www.jroller.com/navanee/entry/unsupportedoperationexception_this_parser_does_not            
            spf.setXIncludeAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            return new SAXSource(xmlReader, new InputSource(in));
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static SaasGroup loadSaasGroup(FileObject input) throws IOException {
        if (input == null) {
            return null;
        }
        Group g = loadJaxbObject(input, Group.class, false);
        return new SaasGroup(null, g);
    }

    public static SaasGroup loadSaasGroup(InputStream input) throws JAXBException {
        Group g = loadJaxbObject(input, Group.class);
        if (g != null) {
            return new SaasGroup((SaasGroup)null, g);
        }
        return null;
    }

    public static void saveSaasGroup(SaasGroup saasGroup, File outFile) throws IOException, JAXBException {
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            saveSaasGroup(saasGroup, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public static final QName QNAME_GROUP = new QName(Saas.NS_SAAS, "group");
    public static final QName QNAME_SAAS_SERVICES = new QName(Saas.NS_SAAS, "saas-services");
    
    public static void saveSaasGroup(SaasGroup saasGroup, OutputStream output) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Group.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<Group> jbe = new JAXBElement<Group>(QNAME_GROUP, Group.class, saasGroup.getDelegate());
        marshaller.marshal(jbe, output);
    }

    public static void saveSaas(Saas saas, FileObject file) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance(SaasServices.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<SaasServices> jbe = new JAXBElement<SaasServices>(QNAME_SAAS_SERVICES, SaasServices.class, saas.getDelegate());
        OutputStream out = null;
        FileLock lock = null;
        try {
            lock = file.lock();
            out = file.getOutputStream(lock);
            marshaller.marshal(jbe, out);
        } finally {
            if (out != null) {
                out.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    public static Application loadWadl(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, Application.class, true);
    }

    public static Application loadWadl(InputStream in) throws JAXBException {
        return loadJaxbObject(in, Application.class, true);
    }

    public static SaasServices loadSaasServices(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, SaasServices.class, true);
    }

    public static SaasServices loadSaasServices(InputStream in) throws JAXBException {
        return loadJaxbObject(in, SaasServices.class, true);
    }

    private static Lookup.Result<SaasNodeActionsProvider> extensionsResult = null;
    public static Collection<? extends SaasNodeActionsProvider> getSaasNodeActionsProviders() {
        if (extensionsResult == null) {
            extensionsResult = Lookup.getDefault().lookupResult(SaasNodeActionsProvider.class);
        }
        return extensionsResult.allInstances();
    }
    
    /*public static <T> T fromXPath(Object root, String xpath, Class<T> type) {
        JXPathContext context = JXPathContext.newContext(root);
        context.registerNamespace("", Saas.NS_WADL);
        return type.cast(context.getValue(xpath));
    }*/

    public static Method wadlMethodFromIdRef(Application app, String methodIdRef) {
        String methodId = methodIdRef;
        if (methodId.charAt(0) == '#') {
            methodId = methodId.substring(1);
        }
        Method result = null;
        for (Object o : app.getResourceTypeOrMethodOrRepresentation()) {
            if (o instanceof Method) {
                Method m = (Method) o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
            }
        }
        for (Resource base : app.getResources().getResource()) {
            result = findMethodById(base, methodId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    static Method findMethodById(Resource base, String methodId) {
        for (Object o : base.getMethodOrResource()) {
            if (o instanceof Method) {
                Method m = (Method)o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
                continue;
            } else {
                Method m = findMethodById((Resource)o, methodId);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }
    
    public static Method wadlMethodFromXPath(Application app, String xpath) {
        String paths[] = xpath.split("/");
        Resource current = null;
        for (int pathIndex = 0; pathIndex < paths.length; pathIndex++) {
            String path = paths[pathIndex];
            if ("application".equals(path) || path.length() == 0 || "resources".equals(path)) {
                continue;
            } else if (path.startsWith("resource[")) {
                int i = getIndex(path);
                if (i > -1) {
                    List<Resource> resources = getCurrentResources(app, current);
                    if (i < resources.size()) {
                        current = resources.get(i);
                        continue;
                    }    
                }
                return null;
            } else if (path.startsWith("method[")) {
                int iTarget = getIndex(path);
                if (iTarget > -1) {
                    int i = 0;
                    for (Object o : current.getMethodOrResource()) {
                        if (o instanceof Method) {
                            if (i == iTarget) {
                                if (pathIndex == (paths.length -1)) {
                                    return (Method) o;
                                } else {
                                    return null;
                                }
                            }
                            if (i < iTarget) {
                                i++;
                            } else {
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }
    
    static List<Resource> getCurrentResources(Application app, Resource current) {
        if (current == null) {
            return app.getResources().getResource();
        }
        List<Resource> result = new ArrayList<Resource>();
        for (Object o : current.getMethodOrResource()) {
            if (o instanceof Resource) {
                result.add((Resource)o);
            }
        }
        return result;
    }
    
    static int getIndex(String path) {
        int iOpen = path.indexOf('[');
        int iClose = path.indexOf(']');
        if (iOpen < 0 || iClose < 0 || iClose <= iOpen) {
            return -1;
        }
        try {
            return Integer.valueOf(path.substring(iOpen+1, iClose)) - 1; //xpath index is 1-based
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Set<String> getMediaTypesFromJAXBElement(List<JAXBElement<RepresentationType>> repElements) {
        Set<String> result = new HashSet<String>();
        for (JAXBElement<RepresentationType> repElement : repElements) {
            result.add(repElement.getValue().getMediaType());
        }
        return result;
    }
    
    public static Set<String> getMediaTypes(List<RepresentationType> repTypes) {
        Set<String> result = new HashSet<String>();
        for (RepresentationType repType : repTypes) {
            result.add(repType.getMediaType());
        }
        return result;
    }
    
    public static String getSignature(WadlSaasMethod method) {
        WadlSaas saas = method.getSaas();
        Resource[] paths = method.getResourcePath();
        Method m = method.getWadlMethod();
        
        StringBuffer sb = new StringBuffer();
        sb.append(m.getName());
        sb.append(" : ");
        try {
            sb.append(saas.getWadlModel().getResources().getBase());
        } catch(IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        for (Resource r : paths) {
            sb.append(r.getPath());
            sb.append('/');
        }
        Param[] params = null;
        if (m.getRequest() != null && m.getRequest().getParam() != null) {
            params = m.getRequest().getParam().toArray(new Param[m.getRequest().getParam().size()]);
            if (params.length > 0) {
                sb.append(" (");
            }
            for (int i=0 ; i < params.length; i++) {
                Param p = params[i];
                if (i > 0) {
                    sb.append(",");
                }
                if (p.getStyle() == ParamStyle.TEMPLATE) {
                    sb.append('{');
                    sb.append(p.getName());
                    sb.append('}');
                } else if (p.getStyle() == ParamStyle.QUERY) {
                    sb.append('?');
                    sb.append(p.getName());
                } else if (p.getStyle() == ParamStyle.MATRIX) {
                    sb.append('[');
                    sb.append(p.getName());
                    sb.append(']');
                } else if (p.getStyle() == ParamStyle.HEADER) {
                    sb.append('<');
                    sb.append(p.getName());
                    sb.append('>');
                } else {
                    sb.append(p.getName());
                }
            }
            if (params.length > 0) {
                sb.append(" )");
            }
        }
        return sb.toString();
    }
    
    public static Image loadIcon(SaasGroup saasGroup, int type) {
        String path = saasGroup.getIcon16Path();
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            path =  saasGroup.getIcon32Path();
        }
        if (path != null) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            return Utilities.loadImage(path);
        }
        return null;
    }
    
    public static String deriveFileName(String path) {
        String name = null;
        try {
            URL url = new URL(path);
            name = url.getPath();
            
        } catch(MalformedURLException e) {
        }
        if (name == null) {
            name = path;
        }
        name = name.substring(name.lastIndexOf('/')+1);   
        return name;
    }
    
    public static FileObject extractWadlFile(WadlSaas saas) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(saas.getUrl());
        if (in == null) {
            return null;
        }
        OutputStream out = null;
        FileObject wadlFile;
        try {
            FileObject dir = saas.getSaasFolder();
            FileObject catalogDir = dir.getFileObject("catalog");
            if (catalogDir == null) {
                catalogDir = dir.createFolder(CATALOG);
            }
            String wadlFileName = deriveFileName(saas.getUrl());
            wadlFile = catalogDir.getFileObject(wadlFileName);
            if (wadlFile == null) {
                wadlFile = catalogDir.createData(wadlFileName);
            }
            out = wadlFile.getOutputStream();
            FileUtil.copy(in, out);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return wadlFile;
    }

    public static Saas getServiceByUrl(SaasGroup group, String url) {
        for (Saas s : group.getServices()) {
            if (s.getUrl().equals(url)) {
                return s;
            }
        }
        return null;
    }

    public static String getWadlServiceDirName(String wadlUrl) {
            String urlPath = wadlUrl.replace('\\', '/');
            if (urlPath.endsWith(APPLICATION_WADL)) {
                urlPath = urlPath.substring(0, urlPath.length() - APPLICATION_WADL.length() - 1);
            }
            int start = urlPath.lastIndexOf("/") + 1; //NOI18N
            String name = urlPath.substring(start);
            if (name.endsWith(".wadl") || name.endsWith(".WADL")) {
                name = name.substring(0, name.length()- 5);
            }
            name = name.replace('.', '-'); // NOI18N
            return ensureUniqueServiceDirName(name);
    }
    
    public static String ensureUniqueServiceDirName(String name) {
        String result = name;
        for (int i=0 ; i<1000 ; i++) {
            FileObject websvcHome = SaasServicesModel.getWebServiceHome();
            if (i > 0) {
                result = name + i;
            }
            if (websvcHome.getFileObject(result) == null) {
                try {
                    websvcHome.createFolder(result);
                } catch(IOException e) {
                    Exceptions.printStackTrace(e);
                }
                break;
            }
        }
        return result;
    }

    public static FileObject retrieveWadlFile(WadlSaas saas) {
        try {
            FileObject saasFolder = saas.getSaasFolder();
            File catalogFile = new File(FileUtil.toFile(saasFolder), CATALOG);
            URI catalog  = catalogFile.toURI();
            URI wadlUrl = new URI(saas.getUrl());
            
            return getRetriever().retrieveResource(saasFolder, catalog, wadlUrl);
            
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    private static Retriever getRetriever() {
        Retriever r = Lookup.getDefault().lookup(Retriever.class);
        if (r != null) {
            return r;
        }
        return Retriever.getDefault();
    }
}