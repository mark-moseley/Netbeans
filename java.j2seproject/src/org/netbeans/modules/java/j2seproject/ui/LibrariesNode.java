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

package org.netbeans.modules.java.j2seproject.ui;


import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.java.j2seproject.UpdateHelper;
import org.netbeans.modules.java.j2seproject.classpath.J2SEProjectClassPathExtender;
import org.netbeans.modules.java.j2seproject.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.ui.customizer.LibrariesChooser;




/**
 * LibrariesNode displays the content of classpath and optionaly Java platform.
 * @author Tomas Zezula
 */
final class LibrariesNode extends AbstractNode {

    private static final Image ICON_BADGE = Utilities.loadImage("org/netbeans/modules/java/j2seproject/ui/resources/libraries-badge.png");    //NOI18N
    static final RequestProcessor rp = new RequestProcessor ();
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;

    private final String displayName;
    private final Action[] librariesNodeActions;


    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param displayName the display name of the node
     * @param project the project owning this node, the proejct is placed into LibrariesNode's lookup
     * @param eval {@link PropertyEvaluator} used for listening
     * @param helper {@link UpdateHelper} used for reading and updating project's metadata
     * @param refHelper {@link ReferenceHelper} used for destroying unused references
     * @param classPathProperty the ant property name of classpath which should be visualized
     * @param classPathIgnoreRef the array of ant property names which should not be displayed, may be
     * an empty array but not null
     * @param platformProperty the ant name property holding the J2SE platform system name or null
     * if the platform should not be displayed
     * @param librariesNodeActions actions which should be available on the created node.
     */
    LibrariesNode (String displayName, Project project, PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                   String classPathProperty, String[] classPathIgnoreRef, String platformProperty,
                   Action[] librariesNodeActions) {
        super (new LibrariesChildren (eval, helper, refHelper, classPathProperty, classPathIgnoreRef, platformProperty), Lookups.singleton(project));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    public String getDisplayName () {
        return this.displayName; 
    }

    public String getName () {
        return this.getDisplayName();
    }    
    
    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }
        
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    public Action[] getActions(boolean context) {        
        return this.librariesNodeActions;
    }

    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createAddProjectAction (Project p, String classPathId) {
        return new AddProjectAction (p, classPathId);
    }

    public static Action createAddLibraryAction (Project p, String classPathId) {
        return new AddLibraryAction (p, classPathId);
    }

    public static Action createAddFolderAction (Project p, String classPathId) {
        return new AddFolderAction (p, classPathId);
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }
    
    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    //Static inner classes
    private static class LibrariesChildren extends Children.Keys implements PropertyChangeListener {

        
        /**
         * Constant represneting a prefix of library reference generated by {@link org.netbeans.modules.java.j2seplatform.libraries.J2SELibraryTypeProvider}
         */
        private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
        
        /**
         * Constant representing a prefix of artifact reference generated by {@link ReferenceHelper}
         */
        private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
        /**
         * Constant representing a prefix of file reference generated by {@link ReferenceHelper}
         */
        private static final String FILE_REF_PREFIX = "${file.reference."; //NOI18N
        /**
         * Constant representing a prefix of ant property reference
         */
        private static final String REF_PREFIX = "${"; //NOI18N
        
        private static final String LIBRARIES_ICON = "org/netbeans/modules/java/j2seproject/ui/resources/libraries.gif"; //NOI18N
        private static final String ARCHIVE_ICON = "org/netbeans/modules/java/j2seproject/ui/resources/jar.gif";//NOI18N        

        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final String classPathProperty;
        private final String platformProperty;
        private final Set classPathIgnoreRef;
        
        //XXX: Workaround: classpath is used only to listen on non existent files.
        // This should be removed when there will be API for it
        // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
        private ClassPath fsListener;


        LibrariesChildren (PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                           String classPathProperty, String[] classPathIgnoreRef, String platformProperty) {
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.classPathProperty = classPathProperty;
            this.classPathIgnoreRef = new HashSet(Arrays.asList(classPathIgnoreRef));
            this.platformProperty = platformProperty;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (classPathProperty.equals(propName) || ClassPath.PROP_ROOTS.equals(propName) || LibraryManager.PROP_LIBRARIES.equals(propName)) {
                synchronized (this) {
                    if (fsListener!=null) {
                        fsListener.removePropertyChangeListener (this);
                    }
                }
                rp.post (new Runnable () {
                    public void run () {
                        setKeys(getKeys());
                    }
                });
            }
        }

        protected void addNotify() {
            this.eval.addPropertyChangeListener (this);
            LibraryManager.getDefault().addPropertyChangeListener(this);
            this.setKeys(getKeys ());
        }

        protected void removeNotify() {
            this.eval.removePropertyChangeListener(this);
            LibraryManager.getDefault().removePropertyChangeListener(this);
            synchronized (this) {
                if (fsListener!=null) {
                    fsListener.removePropertyChangeListener (this);
                    fsListener = null;
                }
            }
            this.setKeys(Collections.EMPTY_SET);
        }

        protected Node[] createNodes(Object obj) {
            Node[] result = null;
            if (obj instanceof Key) {
                Key key = (Key) obj;
                switch (key.getType()) {
                    case Key.TYPE_PLATFORM:
                        result = new Node[] {PlatformNode.create(eval, platformProperty)};
                        break;
                    case Key.TYPE_PROJECT:
                        result = new Node[] {new ProjectNode(key.getProject(), key.getArtifactLocation(), helper, refHelper, key.getClassPathId(),
                            key.getEntryId())};
                        break;
                    case Key.TYPE_LIBRARY:
                        result = new Node[] {ActionFilterNode.create(PackageView.createPackageView(key.getSourceGroup()),
                            helper, key.getClassPathId(), key.getEntryId())};
                        break;
                }
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        private List getKeys () {
            EditableProperties projectSharedProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties projectPrivateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            EditableProperties privateProps = PropertyUtils.getGlobalProperties();
            List/*<URL>*/ rootsList = new ArrayList ();
            List result = getKeys (projectSharedProps, projectPrivateProps, privateProps, classPathProperty, rootsList);
            //Add PlatformNode if needed and project exists
            FileObject projectDir = helper.getAntProjectHelper().getProjectDirectory();
            if (platformProperty!=null && projectDir !=null && projectDir.isValid() && !projectDir.isVirtual()) {
                result.add (new Key());
            }
            //XXX: Workaround: Remove this when there will be API for listening on nonexistent files
            // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
            ClassPath cp = ClassPathSupport.createClassPath ((URL[])rootsList.toArray(new URL[rootsList.size()]));
            cp.addPropertyChangeListener (this);
            cp.getRoots();
            synchronized (this) {
                fsListener = cp;
            }
            return result;
        }

        private List getKeys (EditableProperties projectSharedProps, EditableProperties projectPrivateProps,
                              EditableProperties privateProps, String currentClassPath, List/*<URL>*/ rootsList) {
            List result = new ArrayList ();
            String raw = projectSharedProps.getProperty (currentClassPath);
            if (raw == null) {
                raw = projectPrivateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                raw = privateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                return result;
            }
            List pe = new ArrayList(Arrays.asList(PropertyUtils.tokenizePath( raw )));
            while (pe.size()>0){
                String prop = (String) pe.remove(0);
                String propName = org.netbeans.modules.java.j2seproject.classpath.ClassPathSupport.getAntPropertyName (prop);
                if (classPathIgnoreRef.contains(propName)) {
                    continue;
                }
                else if (prop.startsWith( LIBRARY_PREFIX )) {
                    //Library reference
                    String eval = prop.substring( LIBRARY_PREFIX.length(), prop.lastIndexOf('.') ); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary (eval);
                    if (lib != null) {
                        List/*<URL>*/ roots = lib.getContent("classpath");  //NOI18N
                        Icon libIcon = new ImageIcon (Utilities.loadImage(LIBRARIES_ICON));
                        for (Iterator it = roots.iterator(); it.hasNext();) {
                            URL rootUrl = (URL) it.next();
                            rootsList.add (rootUrl);
                            FileObject root = URLMapper.findFileObject (rootUrl);
                            if (root != null) {
                                String displayName;
                                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                    FileObject file = FileUtil.getArchiveFile (root);
                                    displayName = file.getNameExt();
                                }
                                else {
                                    File file = FileUtil.toFile (root);
                                    if (file != null) {
                                        displayName = file.getAbsolutePath();
                                    }
                                    else {
                                        displayName = root.getNameExt();
                                    }
                                }
                                displayName = MessageFormat.format (
                                    NbBundle.getMessage (LibrariesNode.class,"TXT_LibraryPartFormat"),
                                    new Object[] {lib.getDisplayName(), displayName});
                                SourceGroup sg = new LibrariesSourceGroup (root, displayName, libIcon, libIcon);
                                result.add (new Key(sg,currentClassPath, propName));
                            }
                        }
                    }
                    //Todo: May try to resolve even broken library
                }
                else if (prop.startsWith(ANT_ARTIFACT_PREFIX)) {
                    //Project reference
                    Object ret[] = refHelper.findArtifactAndLocation(prop);
                    if ( ret[0] != null && ret[1] != null) {
                        AntArtifact artifact = (AntArtifact)ret[0];
                        URI uri = (URI) ret[1];
                        result.add (new Key(artifact, uri, currentClassPath, propName));
                    }
                }
                else if (prop.startsWith(FILE_REF_PREFIX)) {
                    //File reference
                    String evaluatedRef = eval.getProperty(propName);
                    if (evaluatedRef != null) {
                        File file = helper.getAntProjectHelper().resolveFile(evaluatedRef);
                        SourceGroup sg = createFileSourceGroup(file,rootsList);
                        if (sg !=null) {
                            result.add (new Key(sg,currentClassPath, propName));
                        }
                    }
                }
                else if (prop.startsWith(REF_PREFIX)) {
                    //Path reference
                    result.addAll(getKeys(projectSharedProps, projectPrivateProps, privateProps,propName, rootsList));
                }
                else {
                    //file
                    File file = helper.getAntProjectHelper().resolveFile(prop);
                    SourceGroup sg = createFileSourceGroup(file,rootsList);
                    if (sg !=null) {
                        result.add ( new Key(sg,currentClassPath, propName));
                    }
                }
            }
            return result;
        }

        private static SourceGroup createFileSourceGroup (File file, List/*<URL>*/ rootsList) {
            Icon icon;
            Icon openedIcon;
            String displayName;
            try {
                URL url = file.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                    icon = openedIcon = new ImageIcon (Utilities.loadImage(ARCHIVE_ICON));
                    displayName = file.getName();
                }
                else {
                    String sURL = url.toExternalForm();
                    if (!sURL.endsWith("/")) {  //NOI18N
                        url = new URL (sURL+"/");   //NOI18N
                    }
                    icon = getFolderIcon (false);
                    openedIcon = getFolderIcon (true);
                    displayName = file.getAbsolutePath();
                }
                rootsList.add (url);
                FileObject root = URLMapper.findFileObject (url);
                if (root != null) {
                    return new LibrariesSourceGroup (root,displayName,icon,openedIcon);
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
            return null;
        }        
    }

    private static class Key {
        static final int TYPE_PLATFORM = 0;
        static final int TYPE_LIBRARY = 1;
        static final int TYPE_PROJECT = 2;

        private int type;
        private String classPathId;
        private String entryId;
        private SourceGroup sg;
        private AntArtifact antArtifact;
        private URI uri;

        Key () {
            this.type = TYPE_PLATFORM;
        }

        Key (SourceGroup sg, String classPathId, String entryId) {
            this.type = TYPE_LIBRARY;
            this.sg = sg;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        Key (AntArtifact a, URI uri, String classPathId, String entryId) {
            this.type = TYPE_PROJECT;
            this.antArtifact = a;
            this.uri = uri;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        public int getType () {
            return this.type;
        }

        public String getClassPathId () {
            return this.classPathId;
        }

        public String getEntryId () {
            return this.entryId;
        }

        public SourceGroup getSourceGroup () {
            return this.sg;
        }

        public AntArtifact getProject () {
            return this.antArtifact;
        }
        
        public URI getArtifactLocation () {
            return this.uri;
        }

        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.antArtifact == null ? 0 : this.antArtifact.hashCode();
                    break;
            }
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (other.type != type) {
                return false;
            }
            switch (type) {
                case TYPE_LIBRARY:
                    return (this.sg == null ? other.sg == null : this.sg.equals(other.sg)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PROJECT:
                    return (this.antArtifact == null ? other.antArtifact == null : this.antArtifact.equals(other.antArtifact)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PLATFORM:
                    return true;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class AddProjectAction extends AbstractAction {

        private final Project project;
        private final String classPathId;

        public AddProjectAction (Project project, String classPathId) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
        }

        public void actionPerformed(ActionEvent e) {
            AntArtifactChooser.ArtifactItem ai[] = AntArtifactChooser.showDialog(
                    new String[] {JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
                    project, null);
                if ( ai != null ) {
                    addArtifacts( ai );
                }
        }

        private void addArtifacts (AntArtifactChooser.ArtifactItem[] artifactItems) {
            J2SEProjectClassPathExtender cpExtender = (J2SEProjectClassPathExtender) project.getLookup().lookup(J2SEProjectClassPathExtender.class);
            if (cpExtender != null) {
                for (int i=0; i<artifactItems.length;i++) {
                    try {
                        cpExtender.addAntArtifact(classPathId, artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI());
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            }
            else {
                ErrorManager.getDefault().log ("J2SEProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }
    }

    private static class AddLibraryAction extends AbstractAction {

        private final Project project;
        private final String classPathId;

        public AddLibraryAction (Project project, String classPathId) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
        }

        public void actionPerformed(ActionEvent e) {
            Object[] options = new Object[] {
                new JButton (NbBundle.getMessage (LibrariesNode.class,"LBL_AddLibrary")),
                DialogDescriptor.CANCEL_OPTION
            };
            ((JButton)options[0]).setEnabled(false);
            ((JButton)options[0]).getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (LibrariesNode.class,"AD_AddLibrary"));
            LibrariesChooser panel = new LibrariesChooser ((JButton)options[0]);
            DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( LibrariesNode.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            if (desc.getValue() == options[0]) {
                addLibraries (panel.getSelectedLibraries());
            }
            dlg.dispose();
        }

        private void addLibraries (Library[] libraries) {
            J2SEProjectClassPathExtender cpExtender = (J2SEProjectClassPathExtender) project.getLookup().lookup(J2SEProjectClassPathExtender.class);
            if (cpExtender != null) {
                for (int i=0; i<libraries.length;i++) {
                    try {
                        cpExtender.addLibrary(classPathId, libraries[i]);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            }
            else {
                ErrorManager.getDefault().log ("J2SEProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }

    }

    private static class AddFolderAction extends AbstractAction {

        private final Project project;
        private final String classPathId;

        public AddFolderAction (Project project, String classPathId) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddFolder_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setMultiSelectionEnabled( true );
            chooser.setDialogTitle( NbBundle.getMessage( LibrariesNode.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
            FileFilter fileFilter = new SimpleFileFilter (
                NbBundle.getMessage( LibrariesNode.class, "LBL_ZipJarFolderFilter" ),   // NOI18N
                new String[] {"ZIP","JAR"} );   // NOI18N
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            chooser.setFileFilter(fileFilter);                                                                 
            File curDir = FoldersListSettings.getDefault().getLastUsedClassPathFolder();
            chooser.setCurrentDirectory (curDir);
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() );
            if ( option == JFileChooser.APPROVE_OPTION ) {
                File files[] = chooser.getSelectedFiles();
                addJarFiles( files, fileFilter );
                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                FoldersListSettings.getDefault().setLastUsedClassPathFolder(curDir);
            }
        }

        private void addJarFiles (File[] files, FileFilter fileFilter) {
            J2SEProjectClassPathExtender cpExtender = (J2SEProjectClassPathExtender) project.getLookup().lookup(J2SEProjectClassPathExtender.class);
            if (cpExtender != null) {
                for (int i=0; i<files.length;i++) {
                    try {
                        //Check if the file is acceted by the FileFilter,
                        //user may enter the name of non displayed file into JFileChooser
                        if (fileFilter.accept(files[i])) {
                            FileObject fo = FileUtil.toFileObject (files[i]);
                            assert fo != null : files[i];
                            cpExtender.addArchiveFile(classPathId, fo);
                        }
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            }
            else {
                ErrorManager.getDefault().log ("J2SEProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }

    }

    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;            
            try {
                return FileUtil.isArchiveFile(f.toURI().toURL());
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
                return false;
            }
        }

        public String getDescription() {
            return this.description;
        }
    }
}
