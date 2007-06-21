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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.beans.BeanInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Ayub Khan
 */
public class RestPaletteFactory {
    
    public static final String REST_PALETTE_FOLDER = "RestPalette";
    
    public static final String REST_COMPONENTS_FOLDER = "RestComponents";
    
    private static final String REST_COMPONENT_DATA = "RestComponentData";
    
    private static PaletteController pc = null;
    
    private static boolean paletteUpdateInProgress = false;
    
    public RestPaletteFactory() {
    }
    
    public static PaletteController createPalette() {
        try {
            getPaletteRoot();
            PaletteController pc = PaletteFactory.createPalette(REST_PALETTE_FOLDER, new RestPaletteActions());
            setPaletteController(pc);
            updateAllPaletteItems();
            return pc;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public synchronized static PaletteController getPaletteController() {
        return pc;
    }
    
    public synchronized static void setPaletteController(PaletteController pController) {
        pc = pController;
    }
    
    public synchronized static boolean isPaletteUpdateInProgress() {
        return paletteUpdateInProgress;
    }
    
    public synchronized static void setPaletteUpdateInProgress(boolean inProgress) {
        paletteUpdateInProgress = inProgress;
    }
    
    //----------------------------------   helpers  ------------------------------------------------------------------
        
    public static void updateAllPaletteItems() {
        try {
            setPaletteUpdateInProgress(true);
            for (org.openide.filesystems.FileObject fo : getAllRestComponentFiles()) {
                try {
                    createPaletteItemFromComponent(fo, false);
                } catch (javax.xml.parsers.ParserConfigurationException ex) {
                } catch (org.xml.sax.SAXException ex) {
                } catch (java.io.IOException ex) {
                }
            }
            
        } catch (IOException ex) {
        }
        setPaletteUpdateInProgress(false);
    }
    
    public static boolean createPaletteItemFromComponent(FileObject fo) 
            throws IOException, ParserConfigurationException, SAXException {
        return createPaletteItemFromComponent(fo, true);
    }
    
    private static boolean createPaletteItemFromComponent(FileObject fo, boolean skipInProgress)
            throws IOException, ParserConfigurationException, SAXException {
        if(skipInProgress && isPaletteUpdateInProgress())
            return false;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fo.getInputStream());
        RestComponentData data = new RestComponentData(doc);

        String itemsFolderPath = data.getCategoryPath();
        FileObject root = getPaletteRoot();
        FileObject itemsFolder1 = createItemsFolder(root, itemsFolderPath);
        String itemsFolderDisplay = data.getCategoryName();
        if(itemsFolderDisplay != null) {
            DataObject d = DataObject.find(itemsFolder1);
            if(d != null && d.getNodeDelegate() != null)
                d.getNodeDelegate().setDisplayName(itemsFolderDisplay);
        }
        String displayName = data.getDisplayName();
        String description = data.getDescription();
        Node itemNode1 = createItemNode(itemsFolder1, getComponentFileName(data),
                data.getClassName(), data.getIcon16(), data.getIcon32(), displayName, description);
        setRestComponentData(itemNode1, data);
        return true;
    }
    
    public static RestComponentData getRestComponentData(Node itemNode) {
        if(itemNode != null)
            return (RestComponentData) itemNode.getValue(REST_COMPONENT_DATA);
        return null;
    }
    
    public static void setRestComponentData(Node itemNode, RestComponentData data) {
        itemNode.setValue(REST_COMPONENT_DATA, data);
    }
    
    public static List<FileObject> getAllRestComponentFiles() throws IOException {
        List<FileObject> files = new ArrayList<FileObject>();
        FileObject rcFolder = getRestComponentsFolder();
        getAllRestComponentFiles(rcFolder, files);
        return files;
    }
    
    public static void getAllRestComponentFiles(FileObject rcFolder, List<FileObject> files) throws IOException {
        for(FileObject fo: rcFolder.getChildren()) {
            if(fo.isFolder())
                getAllRestComponentFiles(fo, files);
            else
                files.add(fo);
        }
    }
    
    public static String getComponentFileName(RestComponentData data) {
        return data.getName() + ".xml";
    }
    
    public static FileObject createItemsFolder(FileObject root, String itemsFolder) throws IOException {
        if(root == null)
            throw new IOException("Rest Palette Folder null");
        if(itemsFolder == null)
            throw new IOException("Folder name null");
        FileObject fooCategory = root.getFileObject(itemsFolder);
        if(fooCategory == null)
            fooCategory = FileUtil.createFolder(root,itemsFolder);
        return fooCategory;
    }
    
    public static Node createItemNode(FileObject itemsFolder, String name,
            String className, String icon16, String icon32, String displayName,
            String description)
            throws IOException, ParserConfigurationException, SAXException {
        FileObject itemFile = createItemFileWithActiveEditorDrop(itemsFolder, name,
                className, icon16, icon32, displayName, description);
        Node itemNode = DataObject.find(itemFile).getNodeDelegate();
        itemNode.setHidden(false);
        
        getPaletteRoot().refresh();
        
        Object o = itemNode.getLookup().lookup(ActiveEditorDrop.class);
        return itemNode;
    }
    
    public static Node createItemNode(FileObject itemsFolder, String name,
            String className, String icon16, String icon32, String bundleName,
            String nameKey, String toolTipKey)
            throws IOException, ParserConfigurationException, SAXException {
        FileObject itemFile = createItemFileWithActiveEditorDrop(itemsFolder, name,
                className, icon16, icon32, bundleName, nameKey, toolTipKey);
        Node itemNode = DataObject.find(itemFile).getNodeDelegate();
        itemNode.setHidden(false);
        
        getPaletteRoot().refresh();
        
        Object o = itemNode.getLookup().lookup(ActiveEditorDrop.class);
        return itemNode;
    }
    
    public static FileObject createItemFileWithActiveEditorDrop(FileObject itemsFolder,
            String itemFile, String className, String icon16, String icon32,
            String displayName, String description) throws IOException {
        FileObject fo = itemsFolder.getFileObject(itemFile);
        if(fo != null)
            fo.delete();
        fo = itemsFolder.createData(itemFile);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>");
                writer.write("<editor_palette_item version='1.1'>");
                writer.write("<class name='" + className + "' />");
                writer.write("<icon16 urlvalue='" + icon16 + "' />");
                writer.write("<icon32 urlvalue='" + icon32 + "' />");
                writer.write("<inline-description>");
                writer.write("<display-name>"+displayName+"</display-name>");
                writer.write("<tooltip> <![CDATA[ "+description+" ]]> </tooltip>");
                writer.write("</inline-description>");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    public static FileObject createItemFileWithActiveEditorDrop(FileObject itemsFolder,
            String itemFile, String className, String icon16, String icon32,
            String bundleName, String nameKey, String toolTipKey) throws IOException {
        FileObject fo = itemsFolder.getFileObject(itemFile);
        if(fo != null)
            fo.delete();
        fo = itemsFolder.createData(itemFile);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>");
                writer.write("<editor_palette_item version='1.1'>");
                writer.write("<class name='" + className + "' />");
                writer.write("<icon16 urlvalue='" + icon16 + "' />");
                writer.write("<icon32 urlvalue='" + icon32 + "' />");
                writer.write("<description localizing-bundle='" + bundleName + "' display-name-key='" + nameKey + "' tooltip-key='" + toolTipKey + "' />");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    public static FileObject getPaletteRoot() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject paletteRoot = fs.findResource(REST_PALETTE_FOLDER);
        if(paletteRoot == null)
            paletteRoot = createItemsFolder(fs.getRoot(), REST_PALETTE_FOLDER);
        return paletteRoot;
    }
    
    public static FileObject getRestComponentsFolder() throws IOException {
        String compFolderName = REST_COMPONENTS_FOLDER;
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        return fs.findResource(compFolderName);
    }
    
    public static Lookup getCurrentPaletteItem() {
        return getPaletteController().getSelectedItem();
    }
}
