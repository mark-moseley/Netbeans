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
 *
 * $Id$
 */

package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.installer.download.DownloadManager;
import org.netbeans.installer.download.DownloadOptions;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.SystemUtils.ExecutionResults;
import org.netbeans.installer.utils.SystemUtils.ShortcutLocationType;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public class MacOsNativeUtils extends NativeUtils {
    public static final String APP_SUFFIX = ".app";
    
    private static final String DOCK_PROPERIES = "com.apple.dock.plist";
    
    private static final String PLUTILS = "plutil";
    private static final String PLUTILS_CONVERT = "-convert";
    private static final String PLUTILS_CONVERT_XML = "xml1";
    private static final String PLUTILS_CONVERT_BINARY = "binary1";
    private static final String[] UPDATE_DOCK_COMMAND = new String [] {
        "killall", "-HUP", "Dock"};
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) {
        String fileName = shortcut.getFileName();
        
        if (fileName == null) {
            fileName = shortcut.getExecutable().getName();
            if (fileName!=null && fileName.endsWith(APP_SUFFIX)) {
                fileName = fileName.substring(0,fileName.lastIndexOf(APP_SUFFIX));
            }
        }
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                return new File(getUserHomeDirectory(), "Desktop/" + fileName);
            case ALL_USERS_DESKTOP:
                return new File(getUserHomeDirectory(), "Desktop/" + fileName);
            case CURRENT_USER_START_MENU:
                return getDockPropertiesFile();
            case ALL_USERS_START_MENU:
                return getDockPropertiesFile();
        }
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException {
        final File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        if (locationType == ShortcutLocationType.CURRENT_USER_DESKTOP ||
                locationType == ShortcutLocationType.ALL_USERS_DESKTOP ) {
            // create a symlink on desktop
            if(!shortcutFile.exists()) {
                SystemUtils.getInstance().executeCommand(null,new String [] {
                    "ln", "-s", shortcut.getExecutablePath(),  //NOI18N
                    shortcutFile.getPath()});
            }
        } else {
            //create link in the Dock
            if(convertDockProperties(true)==0) {
                if(modifyDockLink(shortcut,shortcutFile,true)) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Updating Dock");
                    convertDockProperties(false);
                    SystemUtils.getInstance().executeCommand(null,UPDATE_DOCK_COMMAND);
                }
            }
        }
        return shortcutFile;
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws IOException {
        final File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        if (locationType == ShortcutLocationType.CURRENT_USER_DESKTOP ||
                locationType == ShortcutLocationType.ALL_USERS_DESKTOP ) {
            // create a symlink on desktop
            if(shortcutFile.exists()) {
                FileUtils.deleteFile(shortcutFile,false);
            }
        } else {
            //create link in the Dock
            if(convertDockProperties(true)==0) {
                if(modifyDockLink(shortcut,shortcutFile,false)) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Updating Dock");
                    if(convertDockProperties(false)==0) {
                        SystemUtils.getInstance().executeCommand(null,UPDATE_DOCK_COMMAND);
                    }
                }
            }
        }
        return;
    }
    
    private boolean modifyDockLink(Shortcut shortcut,File dockFile,boolean adding) {
        OutputStream outputStream = null;
        try {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    parsing xml file...");
            Document document = documentBuilder.parse(dockFile);
            LogManager.log(ErrorLevel.DEBUG,
                    "    ...complete");
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root element");
            Node root = document.getDocumentElement();
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root/dict element");
            Node dict = XMLUtils.getChildNode(root,"./dict");
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root/dict/[key=persistent-apps] element. dict = " +
                    dict.getNodeName());
            LogManager.log(ErrorLevel.DEBUG,"Get Keys");
            
            
            List <Node> keys = XMLUtils.getChildList(dict, "./key");
            LogManager.log(ErrorLevel.DEBUG,"Length = " + keys.size());
            Node persistentAppsKeyNode = null;
            int index = 0;
            while(keys.get(index)!=null) {
                if(keys.get(index).getTextContent().equals("persistent-apps")) {
                    persistentAppsKeyNode = keys.get(index);
                    break;
                }
                index++;
            }
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    done. KeyNode = " + persistentAppsKeyNode.getTextContent());
            
            if(persistentAppsKeyNode==null) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Not found.. strange.. Create new one");
                persistentAppsKeyNode = XMLUtils.addChildNode(dict,"key","persistent-apps");
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    Getting next element.. expecting it to be array element");
            Node array = keys.get(index);
            index = 0 ;
            while(!array.getNodeName().equals("array") && index < 10) {
                array = array.getNextSibling();
                index++;
            }
            if(index==10) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    is not an array element... very strange");
                return false;
            }
            
            if(array==null) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    null... very strange");
                return false;
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    OK. Content = " + array.getNodeName());
            if(!array.getNodeName().equals("array")) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Not an array element");
                return false;
            }
            if(adding) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Adding shortcut");
                dict = XMLUtils.addChildNode(array,"dict",null);
                XMLUtils.addChildNode(dict,"key","tile-data");
                Node dictChild = XMLUtils.addChildNode(dict,"dict",null);
                XMLUtils.addChildNode(dictChild,"key","file-data");
                Node dictCC = XMLUtils.addChildNode(dictChild,"dict",null);
                XMLUtils.addChildNode(dictCC,"key","_CFURLString");
                XMLUtils.addChildNode(dictCC,"string",shortcut.getExecutablePath());
                XMLUtils.addChildNode(dictCC,"key","_CFURLStringType");
                XMLUtils.addChildNode(dictCC,"integer","0");
                XMLUtils.addChildNode(dictChild,"key","file-label");
                XMLUtils.addChildNode(dictChild,"string",shortcut.getName());
                XMLUtils.addChildNode(dictChild,"key","file-type");
                XMLUtils.addChildNode(dictChild,"integer","41");
                XMLUtils.addChildNode(dict,"key","tile-type");
                XMLUtils.addChildNode(dict, "string","file-tile");
            } else {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Removing shortcut");
                String location = shortcut.getExecutablePath();
                List <Node> dcts = XMLUtils.getChildList(array,
                        "./dict/dict/dict/string");
                index = 0;
                Node dct = null;
                LogManager.log(ErrorLevel.DEBUG,
                        "    Total dict/dict/dict/string items = " + dcts.size());
                LogManager.log(ErrorLevel.DEBUG,
                        "        location = " + location);
                while(dcts.get(index)!=null) {
                    Node item = dcts.get(index);
                    LogManager.log(ErrorLevel.DEBUG,
                            "        content = " + item.getTextContent());
                    
                    if(item.getTextContent().equals(location)) {
                        dct = item;
                        break;
                    }
                    index++;
                };
                
                if(dct!=null) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Shortcut exists in the dock.plist");
                    array.removeChild(dct.getParentNode().getParentNode().getParentNode());
                }
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    Saving XML");
            XMLUtils.saveXMLDocument(document,dockFile,
                    DownloadManager.getInstance().download(
                    ProductRegistry.DEFAULT_PRODUCT_REGISTRY_STYLESHEET_URI,
                    DownloadOptions.getDefaults()));
            LogManager.log(ErrorLevel.DEBUG,
                    "    Done (saving xml)");
            
        } catch (ParserConfigurationException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        }  catch (ParseException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (DownloadException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (XMLException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (SAXException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (IOException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } finally {
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LogManager.log(ErrorLevel.WARNING,
                            "Can`t close stream for Dock properties file");
                }
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "    Return true from modifyDockLink");
        return true;
        
    }
    
    private File getDockPropertiesFile() {
        return new File(getUserHomeDirectory(),
                "Library/Preferences/" + DOCK_PROPERIES);//NOI18N
    }
    
    private int convertDockProperties(boolean decode) {
        File dockFile = getDockPropertiesFile();
        int returnResult = 0;
        try {
            if(!isCheetah() && !isPuma()) {
                if((!decode && (isTiger() || isLeopard())) || decode) {
                    // decode for all except Cheetah and Puma
                    // code only for Tiger and Leopars
                    ExecutionResults result = SystemUtils.getInstance().executeCommand(null,
                            new String [] { PLUTILS,PLUTILS_CONVERT,(decode)? PLUTILS_CONVERT_XML :
                                PLUTILS_CONVERT_BINARY,dockFile.getPath()});
                    returnResult = result.getErrorCode();
                }
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING);
            returnResult = -1;
        }
        return returnResult;
    }
    
    private String getOSVersion() {
        return System.getProperty("os.version");
    }
    
    public boolean isCheetah() {
        return (getOSVersion().startsWith("10.0"));
    }
    
    public boolean isPuma() {
        return (getOSVersion().startsWith("10.1"));
    }
    
    public boolean isJaguar() {
        return (getOSVersion().startsWith("10.2"));
    }
    
    public boolean isPanther() {
        return (getOSVersion().startsWith("10.3"));
    }
    
    public boolean isTiger() {
        return (getOSVersion().startsWith("10.4"));
    }
    
    public boolean isLeopard() {
        return (getOSVersion().startsWith("10.5"));
    }
}
