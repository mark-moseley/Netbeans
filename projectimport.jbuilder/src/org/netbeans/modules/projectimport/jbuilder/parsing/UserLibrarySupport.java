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

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.List;
import java.util.Collections;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.LoggerFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Radek Matous
 */
public final class UserLibrarySupport {
    private static final String ROOT_ELEMENT = "library";//NOI18N
    private static final String FULLNAME_ELEMENT = "fullname";//NOI18N
    private static final String CLASS_ELEMENT = "class";//NOI18N
    private static final String PATH_ELEMENT = "path";//NOI18N
    private static final String REQUIRED_LIB = "required";//NOI18N    
    
    private static File installDirLib;// = new File();
    private static File userHomeLib;// = new File();
    
    private File library;
    private String  libraryName;
    
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(UserLibrarySupport.class);
    
    public static AbstractProject.UserLibrary getInstance(String libraryName, File projectDir)  {
        File[] folders = new File[] {projectDir, getUserHomeLib(),getInstallDirLib()};
        UserLibrarySupport uSupport = UserLibrarySupport.getInstance(libraryName, folders);
        return (uSupport != null) ? uSupport.getLibrary(folders) : null;
    }
    
    public static File getUserHomeLib() {
        if (userHomeLib == null) {
            String home = System.getProperty("user.home", "");//NOI18N
            
            if (home.length() > 0) {
                userHomeLib = new File(home, ".jbuilder2006");//NOI18N
                if (!userHomeLib.exists()) {
                    userHomeLib = new File(home, ".jbuilder2005");//NOI18N
                    if (!userHomeLib.exists()) {
                        logger.finest("Not valid user.home.lib: " + userHomeLib);//NOI18N
                        userHomeLib = null;
                    }
                }
            } else {
                logger.finest("Not valid user.home: ");//NOI18N
            }
        }
        
        return userHomeLib;
    }
    
    public static File getInstallDirLib() {
        return installDirLib;
    }
    
    
    
    public static void setUserHomeLib(final File uHomeDirLib) {
        userHomeLib = uHomeDirLib;
    }
    
    
    public static void setInstallDirLib(final File iDirLib) {
        installDirLib = iDirLib;
    }
    
    private static UserLibrarySupport getInstance(String libraryName, File[] folders)  {
        final String fileName = libraryName.trim()+".library";//NOI18N        
        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null) continue;
            File library = new File(folders[i], fileName);
            if (library.exists()) {
                return new UserLibrarySupport(libraryName, library);
            }
        }
        
        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null) continue;
            final File[] allChildren = folders[i].listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return f.isFile() && f.getName().endsWith(".library");//NOI18N
                }
            });
            if (allChildren == null) continue;
            for (int j = 0; j < allChildren.length; j++) {
                UserLibrarySupport result = resolveLibrary(libraryName, allChildren[j], folders);
                if (result != null) {
                    return result;
                }
            }
        }
        
        logger.finest("library: "+libraryName + " doesn't exists");//NOI18N
        return null;
    }

    private static UserLibrarySupport resolveLibrary(final String libraryName, final File libFile, final File[] folders) {
        File library = null;
        UserLibrarySupport instance = new UserLibrarySupport(libraryName, libFile);
        AbstractProject.UserLibrary ul = instance.getLibrary(folders);
        return ul != null ? instance : null;
    }
    
    /** Creates a new instance of JBLibraries */
    private UserLibrarySupport(String  libraryName, File library) {
        this.libraryName = libraryName;
        this.library = library;
    }
    
    AbstractProject.UserLibrary getLibrary(File[] folders)  {
        try {
            return buildLibrary(folders);
        } catch (IOException iex) {
            ErrorManager.getDefault().notify(iex);
        } catch (SAXException sax) {
            ErrorManager.getDefault().notify(sax);
        }
        
        return null;
    }
    
    
    private AbstractProject.UserLibrary buildLibrary(File[] folders) throws IOException, SAXException {
        AbstractProject.UserLibrary retval = new AbstractProject.UserLibrary(libraryName);
        InputStream jprIs = new BufferedInputStream(new FileInputStream(library));
        try {
            Document doc = XMLUtil.parse(new InputSource(jprIs), false, false, null, null);
            Element docEl = getRootElement(doc);
            
            String fullName = getFullName(docEl);
            if (!fullName.equals(libraryName)) {
                return null;
            }

            List/*<Element>*/ reqElems = Util.findSubElements(docEl);
            for (int i = 0; i < reqElems.size(); i++) {
                Element elem = (Element)reqElems.get(i);
                String classElem = getClassElement(elem);
                if (classElem != null) {
                    resolvePath(folders, retval, elem);
                } else {
                    String requiredLibrary = getRequiredLibrary(elem);
                    if (requiredLibrary != null) {
                        UserLibrarySupport uS = UserLibrarySupport.getInstance(requiredLibrary, folders);
                        if (uS != null) {
                            AbstractProject.UserLibrary uL = uS.getLibrary(folders);
                            if (uL != null) {
                                retval.addDependency(uL);
                            }
                        }
                    }
                }
            }
            
            //Element classElem = Util.findElement(docEl, CLASS_ELEMENT,null);
            
        } catch (Exception ex) {            
            System.out.println("libraryName: " + libraryName);
            return null;
        } finally {
            if (jprIs != null) {
                jprIs.close();
            }
        }
        
        return retval;
    }

    private void resolvePath(final File[] folders, final AbstractProject.UserLibrary retval, final Element classElem) throws IllegalArgumentException {
        List/*<Element>*/ pathElems = (classElem != null) ? Util.findSubElements(classElem) : Collections.EMPTY_LIST;
        for (int i = 0; i < pathElems.size(); i++) {
            String path = getPath((Element)pathElems.get(i));
            if (path != null) {
                AbstractProject.Library lEntry = createLibraryEntry(path);
                if (lEntry != null) {
                    retval.addLibrary(lEntry);
                }                    
            }
        }
    }
    
    private Element getRootElement(Document doc) throws IOException {
        Element docEl = doc.getDocumentElement();
        
        if (!docEl.getTagName().equals(ROOT_ELEMENT)) { // NOI18N
            String message = NbBundle.getMessage(UserLibrarySupport.class,"ERR_WrongRootElement",docEl.getTagName());// NOI18N
            throw new IOException(message);
        }
        
        return docEl;
    }
    
    private AbstractProject.Library createLibraryEntry(String encodedPath) {
        String decodedPath = encodedPath.replaceAll("^\\[", "");//NOI18N
        decodedPath = decodedPath.replaceAll("]", "");//NOI18N
        decodedPath = decodedPath.replaceAll("\\%\\|", ":");//NOI18N
        File f = new File(decodedPath);
        if (!f.exists()) {
            f = new File(library.getParentFile(), decodedPath);
        }
        f = FileUtil.normalizeFile(f);
        if (!f.exists()) {
            logger.finest(encodedPath+ " converted into file: " + f.getAbsolutePath() );//NOI18N
        }
        return (f.exists()) ? new AbstractProject.Library(f) : null;
    }
    
    private String getFullName(Element docEl) {
        String fullName = null;
        
        if (docEl != null) {
            Element fullNameElement = Util.findElement(docEl, FULLNAME_ELEMENT,null);
            fullName = (fullNameElement != null) ? Util.findText(fullNameElement) : null;
        }
        
        return fullName;
    }
    
    
    private String getPath(Element pathElem) {
        return getElement(pathElem, PATH_ELEMENT);
    }

    private String getRequiredLibrary(Element pathElem) {
        return getElement(pathElem, REQUIRED_LIB);
    }
    
    private String getClassElement(Element pathElem) {
        return getElement(pathElem, CLASS_ELEMENT);
    }
    
    
    private String getElement(final Element pathElem, String name) {
        String path = null;
        
        if (pathElem != null && pathElem.getNodeName().equals(name)) {
            path = Util.findText(pathElem);
            
        }
        
        return path;
    }

    
}
