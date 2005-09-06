/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/** Editor Settings main node folder.
 *  In this folder are stored global options such as global keybindings.
 *  Mime options are lazily initialized after loading appropriate kit
 *  (NbEditorKit.java) or after request of Option window to show
 *  the properties.
 *  Initialization starts with loading user's setting from
 *  XML files and then initializer is added to Settings and reseted.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class AllOptionsFolder{
    
    /** folder for Editor Settings main node */
    public static final String FOLDER = "Editors"; // NOI18N
    public static final String OPTION_FILE_NAME = "Settings.settings"; // NOI18N
    
    /** instance of this class */
    private static AllOptionsFolder settingsFolder;
    
    private static boolean baseInitialized = false;
    
    private static DataFolder folder;
    private static MIMEOptionFolder mimeFolder;
    
    // List of already initialized options
    private static Map installedOptions = new Hashtable();
    
    /** Listens to changes on the Modules folder */
    private static FileChangeListener moduleRegListener;

    
    /** Creates new AllOptionsFolder */
    private AllOptionsFolder(DataFolder fld) {
        folder = fld;
    }
    
    /** Gets the singleton of global options MIME folder */
    public MIMEOptionFolder getMIMEFolder(){
        synchronized (Settings.class){
            if (mimeFolder!=null) return mimeFolder;

            FileObject f = Repository.getDefault().getDefaultFileSystem().
            findResource(FOLDER+"/text/"+BaseOptions.BASE); //NOI18N

            // MIME folder doesn't exist, let's create it
            if (f==null){
                FileObject fo = Repository.getDefault().getDefaultFileSystem().
                findResource(AllOptionsFolder.FOLDER);
                String fName = "text/"+BaseOptions.BASE; //NOI18N

                if (fo != null){
                    try{
                        StringTokenizer stok = new StringTokenizer(fName,"/"); //NOI18N
                        while (stok.hasMoreElements()) {
                            String newFolder = stok.nextToken();
                            if (fo.getFileObject(newFolder) == null)
                                fo = fo.createFolder(newFolder);
                            else
                                fo = fo.getFileObject(newFolder);
                        }
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }

                    f = Repository.getDefault().getDefaultFileSystem().
                    findResource(AllOptionsFolder.FOLDER+"/text/"+BaseOptions.BASE); //NOI18N
                }
            }

            if (f != null) {
                try {
                    DataObject d = DataObject.find(f);
                    DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                    if (df != null) {
                        mimeFolder = new MIMEOptionFolder(df, getBase());
                        return mimeFolder;
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            return null;
        }
    }
    
    /** Returns list of installed Options. Values = options classes */
    public List getInstalledOptions(){
        
        // first XMLized options
        
        List retList = new ArrayList();
        String[] MIMES = new String[] {"text", "application"};  //#25246 application/xml-dtd // NOI18N
        for (int in = 0; in<MIMES.length; in++) {
            FileObject mainFolderFO = Repository.getDefault().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER+"/" + MIMES[in]); //NOI18N
            if (mainFolderFO != null){
                DataFolder mainFolder = DataFolder.findFolder(mainFolderFO);
                if (mainFolder != null){
                    DataObject subFolders[] = mainFolder.getChildren();
                    for (int i=0; i<subFolders.length; i++){
                        if (!(subFolders[i] instanceof DataFolder)) continue;
                        DataFolder subFolder = (DataFolder) subFolders[i];
                        FileObject optionInstance = Repository.getDefault().getDefaultFileSystem().
                            findResource(subFolder.getPrimaryFile().getPath()+"/"+AllOptionsFolder.OPTION_FILE_NAME); // NOI18N
                        if (optionInstance == null) continue;
                        try{
                            DataObject optionDO = DataObject.find(optionInstance);
                            if (optionDO == null) continue;
                            InstanceCookie ic = (InstanceCookie)optionDO.getCookie(InstanceCookie.class);
                            if (ic == null) continue;
                            BaseOptions bo = AllOptionsFolder.getDefault().getBO(ic);
                            if (bo == null) continue;
                            retList.add(bo.getClass());
                        }catch(DataObjectNotFoundException donf){
                            donf.printStackTrace();
                        }
                    }
                }
            }
        }
        
        // Now old SystemOptions options
        AllOptions allOptions
        = (AllOptions)AllOptions.findObject(AllOptions.class, true);
        
        if (allOptions == null) return retList;
        
        SystemOption[] sos = allOptions.getOptions();
        if (sos == null) return retList;
        
        for (int i=0; i<sos.length; i++){
            
            if (!(sos[i] instanceof BaseOptions)) continue;
            
            BaseOptions bo = (BaseOptions) sos[i];
            if (retList.contains(bo.getClass())) retList.remove(bo.getClass());
            if (BaseKit.getKit(bo.getKitClass()).getContentType() != null){
                retList.add(bo.getClass());
                processInitializers(bo, false);
            }else{
                final String kitClazz = bo.getKitClass().toString();
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        NotifyDescriptor msg = new NotifyDescriptor.Message(
                        
                        NbBundle.getMessage( AllOptionsFolder.class, "ERR_NoContentTypeDefined", kitClazz),
                        NotifyDescriptor.WARNING_MESSAGE
                        );
                        
                        org.openide.DialogDisplayer.getDefault().notify(msg);
                    }
                }
                );
            }
        }
        
        return retList;
    }
    
    public static void unregisterModuleRegListener(){
        FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N

        if (moduleRegistry !=null){ //NOI18N
            if (moduleRegListener!=null)
                moduleRegistry.removeFileChangeListener(moduleRegListener);
        }
    }
    
    /** Creates the only instance of AllOptionsFolder. */
    public static AllOptionsFolder getDefault(){
        synchronized (Settings.class) {
            // try to find the itutor XML settings
            if (settingsFolder!=null) return settingsFolder;
            org.openide.filesystems.FileObject f = Repository.getDefault().getDefaultFileSystem().
            findResource(FOLDER);
            if (f==null) return null;

            DataFolder df = DataFolder.findFolder(f);
            if (df == null) {
            } else {
                if (settingsFolder == null){
                    settingsFolder = new AllOptionsFolder(df);

                    // attach listeners for module registry for listening on addition or removal of modules in IDE
                    if(moduleRegListener == null) {
                        moduleRegListener = new FileChangeAdapter() {
                            public void fileChanged(FileEvent fe){
                                updateOptions();
                            }
                        };

                        FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N

                        if (moduleRegistry !=null){ //NOI18N
                            moduleRegistry.addFileChangeListener(moduleRegListener);
                        }
                    }

                    return settingsFolder;
                }
            }
            return null;
        }
    }
    
    /** Getter for KeyBingings */
    public List getKeyBindingList() {
        return getBase().getKeyBindingList();
    }
    
    /** Setter for KeyBindings */
    public void setKeyBindingList(List list) {
        getBase().setKeyBindingList(list);
    }
    
    public boolean isToolbarVisible() {
        return getBase().isToolbarVisible();
    }
    
    public void setToolbarVisible(boolean toolbarVisible) {
        getBase().setToolbarVisible(toolbarVisible);
    }
    
    public boolean isTextAntialiasing() {
        return getBase().isTextAntialiasing();
    }
    
    public void setTextAntialiasing(boolean textAntialiasing) {
        getBase().setTextAntialiasing(textAntialiasing);
    }

    /** Loads default global keyBindings List and initializes it.
     *  It is used mainly by other options for initializing global keyBindings */
    protected void loadDefaultKeyBindings(){
        getBase().getKeyBindingList();
    }
    
    /** Returns kitClass of uninstalled option */ 
    private static Class uninstallOption(){
        List updatedInstalledOptions = AllOptionsFolder.getDefault().getInstalledOptions();
        synchronized (Settings.class){
            Iterator i = installedOptions.keySet().iterator();
            while (i.hasNext()){
                Object obj = i.next();
                if(obj instanceof Class){
                    if (!updatedInstalledOptions.contains(obj)){
                        installedOptions.remove(obj);
                        return (Class)obj;
                    }
                }
            }
            return null;            
        }
    }
    
    private static void updateOptions(){
        uninstallOption();        
        List installedOpts = new ArrayList(installedOptions.values());
        Iterator i = installedOpts.iterator();        
        while (i.hasNext()){
            Object obj = i.next();
            if (obj instanceof BaseOptions){
                BaseOptions bo = (BaseOptions)obj;
                if (bo != null){
                    bo.initPopupMenuItems();
                }
            }
        }
    }
    
    /** Returns true if BaseOptions has been initialized */
    public boolean baseInitialized(){
        return baseInitialized;
    }
    
    /** Gets the singleton of BaseOptions and register it in Settings initializer,
     * if it wasn't been done before. */
    private BaseOptions getBase(){
        
        BaseOptions ret = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);
        
        synchronized (Settings.class){
            if (baseInitialized == false){
                // Add the initializer for the base options. It will not be removed
                Settings.addInitializer(ret.getSettingsInitializer(),
                Settings.OPTION_LEVEL);
                baseInitialized = true;
                Settings.reset();
            }
        }
        
        return ret;
    }
    
    /** Gets the instance of BaseOptions from InstanceCookie */
    protected BaseOptions getBO(InstanceCookie ic){
        initInstance(ic);
        BaseOptions ret = null;
        try{
            synchronized (Settings.class){
                ret = (installedOptions.get(ic.instanceClass()) instanceof BaseOptions) ? (BaseOptions) installedOptions.get(ic.instanceClass())
                : null;
            }
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return ret;
    }
    
    /** Create the instance of appropriate BaseOption subclass */
    private void initInstance(InstanceCookie ic){
        try{
            Object optionObj;
            synchronized (Settings.class){
                if (installedOptions.containsKey(ic.instanceClass())) {
                    return;
                }
                optionObj = ic.instanceCreate();
                if (!(optionObj instanceof BaseOptions)) return;
                installedOptions.put(ic.instanceClass(), (BaseOptions)optionObj);
            }
            processInitializers((BaseOptions)optionObj, false);
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    /** Lazily inits MIME Option class */
    public void loadMIMEOption(Class kitClass){
        loadMIMEOption(kitClass, true);
    }
    
    /** Lazily inits MIME Option class. If processOldTypeOption is true initializers for this option will be processed. */
    public void loadMIMEOption(Class kitClass, boolean processOldTypeOption){
        String contentType = BaseKit.getKit(kitClass).getContentType();
        if (contentType == null) return;
        FileObject optionFO = Repository.getDefault().getDefaultFileSystem().
        findResource(FOLDER+"/"+contentType+"/"+OPTION_FILE_NAME); //NOI18N
        if (optionFO == null) {
            // old type of BaseOptions.
            // Options weren't transfered to XML form for this kitClass yet.
            // We have to find them via BaseOptions.getOptions and process initializers.
            if (processOldTypeOption){
                BaseOptions oldBO = BaseOptions.getOptions(kitClass);
                if (oldBO != null){
                    boolean process = false;
                    synchronized (Settings.class){
                        if (!installedOptions.containsKey(kitClass)){
                            installedOptions.put(kitClass, oldBO);
                            process = true;
                        }
                    }
                    if (process){
                        processInitializers(oldBO, false);
                    }
                }
            }
            return;
        }

        try{
            DataObject optionDO = DataObject.find(optionFO);
            if (optionDO == null) return;

            InstanceCookie ic = (InstanceCookie)optionDO.getCookie(InstanceCookie.class);
            if (ic == null) return;

            initInstance(ic);

        }catch(DataObjectNotFoundException donf){
            donf.printStackTrace();
        }
    }
    
    /** Updates MIME option initializer. Loads user's settings stored in XML
     *  files and updates Setting's initializers via reset method */
    private void processInitializers(BaseOptions bo, boolean remove) {
        //synchronized (BaseKit.class){
            synchronized (Settings.class){
                Settings.Initializer si = bo.getSettingsInitializer();
                // Remove the old one
                Settings.removeInitializer(si.getName());
                if (!remove) { // add the new one
                    Settings.addInitializer(si, Settings.OPTION_LEVEL);
                }

                // load all settings of this mime type from XML files
                bo.loadXMLSettings();

                //initialize popup menu
                bo.initPopupMenuItems();

                /* Reset the settings so that the new initializers take effect
                 * or the old are removed. */
                Settings.reset();
            }
        //}
    }
    
}
