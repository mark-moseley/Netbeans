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

package org.netbeans.modules.javadoc;

import java.io.File;
import java.io.IOException;
import java.beans.*;
import java.awt.Image;

import java.util.Enumeration;
import java.lang.reflect.Method;
import javax.swing.event.*;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.src.nodes.FilterFactory;
import org.openide.actions.CutAction;
import org.openide.util.actions.SystemAction;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.javadoc.settings.StdDocletSettings;
import org.netbeans.modules.javadoc.comments.JavaDocPropertySupportFactory;
import org.netbeans.modules.javadoc.comments.AutoCommentAction;
import org.netbeans.modules.javadoc.search.SearchDocAction;
import org.netbeans.modules.javadoc.search.DocFileSystem;


/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public class JavadocModule extends ModuleInstall {

    /** serialVersionUID */
    private static final long serialVersionUID = 984124010415492146L;

    private static final String PROP_INSTALL_COUNT = "installCount"; // NOI18N
    
    public static final ErrorManager err = TopManager.getDefault ().getErrorManager ().getInstance ("org.apache.tools.ant.module"); // NOI18N

    /** By first install of module in the IDE, check whether standard documentation folder
    * exists. If not creates it.
    */
    public void installed() {
        // Install Search Action 
        restored();
    }

    /** By uninstalling module from the IDE do nothing.
    */
    public void uninstalled () {
        // Unmount docs (AutoUpdate should handle actually removing the file):
        Repository repo = TopManager.getDefault ().getRepository ();
        Enumeration e = repo.fileSystems ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof GlobalLocalFileSystem) {
                repo.removeFileSystem ((FileSystem) o);
            }
        }
    }

    /** Called on IDE startup. Registers actions for generating documentation
    * on DataFolder and JavaDataObject.
    */
    public void restored() {
        // Mount docs, or remount if project was discarded:
        Integer count = (Integer) getProperty (PROP_INSTALL_COUNT);
        int icount = count == null ? 1 : count.intValue () + 1;
        putProperty (PROP_INSTALL_COUNT, new Integer (icount));
        // 1: first install (project is discarded anyway)
        // 2: first restore as actual user
        // 3: next restore (project settings incl. Repository loaded)
        notify ("JavadocModule: numberOfStarts=" + icount); // NOI18N
        if (icount <= 2) {
            installJavadocDirectories();
        }

        // Install the factory for adding JavaDoc property to nodes
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addExplorerFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addBrowserFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );

        // Assign the Ctrl+F1 to JavaDoc Index Search Action
        // [PENDING] should be in installed() whenever global keymap editor is finished
        /*
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();
        try {
          assign ("C-F1", "org.netbeans.modules.javadoc.search.SearchDocAction", map);
    } catch (ClassNotFoundException e) {
          // print and go on
          e.printStackTrace();
    }
        */
    }

    // UTILITY METHODS ----------------------------------------------------------------------

    /** Assigns a key to an action
    * @param key key name
    * @param action name of the action
    */
    /*
    private static void assign (String key, String action, Keymap map) throws ClassNotFoundException {
      KeyStroke str = Utilities.stringToKey (key);
      if (str == null) {
        System.err.println ("Not a valid key: " + key);
        // go on
        return;
      }

      Class actionClass = Class.forName (action);

      // create instance of the action
      SystemAction a = SystemAction.get (actionClass);

      map.addActionForKeyStroke (str, a);
      a.setEnabled( true );
}
    */
    /** Dynamicaly invokes a method
     */
    private void invokeDynamic( String className, String methodName, FilterFactory factory ) {

        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )
                return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] { FilterFactory.class }  );
            if ( method == null )
                return;

            method.invoke( null, new Object[] { factory } );
        }
        catch ( java.lang.ClassNotFoundException e ) {
            notify (e);
        }
        catch ( java.lang.NoSuchMethodException e ) {
            notify (e);
        }
        catch ( java.lang.IllegalAccessException e ) {
            notify (e);
        }
        catch ( java.lang.reflect.InvocationTargetException e ) {
            notify (e);
        }
    }

    /** Tries to find standard Javadoc directory, open-api javadoc directrory
     * and directroy for javadoc output and mounts it into javadoc repository
     */

    public static void installJavadocDirectories() {

        // Try to find Java documantation

        File jdkDocsDir = new File ( System.getProperty ("java.home")  + java.io.File.separator + ".." // NOI18N
                                     + java.io.File.separator + "docs" ); // NOI18N
        mount( jdkDocsDir, true );

        // Try to find NetBeans open-api documentation
        // This is now done by Apisupport module
        /*
        File apiDocsDir = null;

        apiDocsDir = new File ( System.getProperty ("netbeans.user")  + java.io.File.separator + "docs" // NOI18N
                                     + java.io.File.separator + "openide-api" ); // NOI18N
        if ( apiDocsDir == null || !apiDocsDir.isDirectory() )
          apiDocsDir = new File ( System.getProperty ("netbeans.home")  + java.io.File.separator + "docs" // NOI18N
                                     + java.io.File.separator + "openide-api" ); // NOI18N
        mount( apiDocsDir, true );
        */

        // Create default directory for JavaDoc
        //StdDocletSettings sdsTemp = (StdDocletSettings) SharedClassObject.findObject (StdDocletSettings.class, true);
        StdDocletSettings sdsTemp = (StdDocletSettings)TopManager.getDefault ().getServices ().find (StdDocletSettings.class);
        if( sdsTemp == null ) 
            sdsTemp = new StdDocletSettings(); 
        // Reseting javadoc output directory is necessary for
        // multiuser installation
        String fileSep = System.getProperty ("file.separator");

        File directory = null;

        try {
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile();
        }
        catch ( java.io.IOException e ) {
            err.notify (ErrorManager.INFORMATIONAL, e);
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile();
        }

        //if ( sdsTemp.getDirectory() != null && !sdsTemp.getDirectory().equals( directory ) ) {
        if ( System.getProperty ("netbeans.user") != null &&
                !System.getProperty ("netbeans.user").equals(System.getProperty ("netbeans.home") ) ) {

            // Multiuser we need to unmount the old file system

            LocalFileSystem localFS = new GlobalLocalFileSystem();
            try {
                File oldDirectory = null;
                try {
                    oldDirectory = new File (System.getProperty ("netbeans.home") + fileSep + "javadoc").getCanonicalFile();
                }
                catch ( java.io.IOException e ) {
                    notify (e);
                    oldDirectory = new File (System.getProperty ("netbeans.home") + fileSep + "javadoc").getAbsoluteFile();
                }

                localFS.setRootDirectory ( oldDirectory );
                Repository r = TopManager.getDefault ().getRepository ();

                FileSystem fs = r.findFileSystem( localFS.getSystemName() );

                if (fs != null) {
                    r.removeFileSystem (fs);
                }
            }
            catch (java.io.IOException ex) {
                notify (ex);
            }
            catch (java.beans.PropertyVetoException ex) {
                notify (ex);
            }
        }

        sdsTemp.setDirectory( directory );
        File jdOutputDir = sdsTemp.getDirectory();

        if ( !jdOutputDir.isDirectory() )
            jdOutputDir.mkdirs();
        mount( jdOutputDir, false );

    }

    /** Method finds out wether directory exists and whether it is
     *  a searchable javadoc directory if so mounts the directory
     *  into Javadoc repository
     */
    static void mount( File root, boolean testSearchability ) {
        notify ("JavadocModule.mount: root=" + root); // NOI18N
  
        if ((root != null) && (root.isDirectory())) {
            String dirName = root.getAbsolutePath();

            FileSystemCapability.Bean cap = new FileSystemCapability.Bean();
            cap.setCompile( false );
            cap.setExecute( false );
            cap.setDebug( false );
            cap.setDoc( true );

            LocalFileSystem localFS = new GlobalLocalFileSystem( cap );
            localFS.setHidden( true );

            try {
                localFS.setRootDirectory (new File (dirName));
                Repository r = TopManager.getDefault ().getRepository ();

                FileSystem fs = r.findFileSystem(localFS.getSystemName());

                if (fs == null) {
                    if( !testSearchability || DocFileSystem.getDocFileObject( localFS ) != null ) {                        
                        r.addFileSystem (localFS);
                        notify ("JavadocModule mounted: " + localFS.getSystemName ()); // NOI18N
                    }
                }
            }
            catch (java.io.IOException ex) {
                notify (ex);
            }
            catch (java.beans.PropertyVetoException ex) {
                notify (ex);
            }
        }
    }

    // Implementation of java.io.Externalizable ------------------

    public void readExternal(final java.io.ObjectInput objectInput )
    throws java.io.IOException, java.lang.ClassNotFoundException {
        super.readExternal( objectInput );
        putProperty (PROP_INSTALL_COUNT, new Integer(objectInput.readInt()));
        //numberOfStarts = objectInput.readInt();
    }

    public void writeExternal(final java.io.ObjectOutput objectOutput )
    throws java.io.IOException {
        super.writeExternal( objectOutput );
        //objectOutput.writeObject (getProperty (PROP_INSTALL_COUNT));
        objectOutput.writeInt( ((Integer)(getProperty (PROP_INSTALL_COUNT))).intValue() );
    }

    private static void notify (Exception e) {
        TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
    }

    private static void notify (String s) {
        TopManager.getDefault ().getErrorManager ().log (ErrorManager.INFORMATIONAL, s);
    }
  
    /** Exists only for the sake of its bean info. */
    public static final class GlobalLocalFileSystem extends LocalFileSystem {        
        private static final long serialVersionUID = 3563912690225075761L;
        
        public GlobalLocalFileSystem(){
            super();
        }
        public GlobalLocalFileSystem(FileSystemCapability cap){
            super(cap);
        }
    }
    /** Marks it as global (not project-specific). */
    public static final class GlobalLocalFileSystemBeanInfo extends SimpleBeanInfo {
        public BeanDescriptor getBeanDescriptor () {
            BeanDescriptor bd = new BeanDescriptor (GlobalLocalFileSystem.class);
            bd.setValue ("global", Boolean.TRUE); // NOI18N
            return bd;
        }
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (LocalFileSystem.class) };
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
        public Image getIcon (int kind) {
            try {
                return Introspector.getBeanInfo (LocalFileSystem.class).getIcon (kind);
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
    }
}
