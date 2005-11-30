/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * UI related utility methods for the module.
 *
 * @author Martin Krauskopf
 */
public final class UIUtil {
    
    private UIUtil() {}
    
    public static String keyToLogicalString(KeyStroke keyStroke) {
        String keyDesc = Utilities.keyToString(keyStroke);
        int dash = keyDesc.indexOf('-');
        return dash == -1 ? keyDesc :
            keyDesc.substring(0, dash).replace('C', 'D').replace('A', 'O') + keyDesc.substring(dash);
    }
    
    /**
     * Calls in turn {@link ProjectChooser#setProjectsFolder} if the
     * <code>folder</code> is not <code>null</code> and is a directory.
     */
    public static void setProjectChooserDir(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        ProjectChooser.setProjectsFolder(folder);
    }
    
    /**
     * Calls {@link #setProjectChooserDir} with the <code>fileOrFolder</code>'s
     * parent if it isn't <code>null</code>. Otherwise fallbacks to
     * <code>fileOrFolder</code> itself if it is a directory.
     */
    public static void setProjectChooserDirParent(File fileOrFolder) {
        if (fileOrFolder == null) {
            return;
        }
        File parent = fileOrFolder.getParentFile();
        setProjectChooserDir(parent != null ? parent :
            (fileOrFolder.isDirectory() ? fileOrFolder : null));
    }
    
    /**
     * Set the <code>text</code> for the <code>textComp</code> and set its
     * carret position to the end of the text.
     */
    public static void setText(JTextComponent textComp, String text) {
        textComp.setText(text);
        textComp.setCaretPosition(text == null ? 0 : text.length());
    }
    
    /**
     * Convenient class for listening to document changes. Use it if you don't
     * care what exact change really happened. {@link #removeUpdate} and {@link
     * #changedUpdate} just delegates to {@link #insertUpdate}. So everything
     * what is need to be notified about document changes is to override {@link
     * #insertUpdate} method.
     */
    public abstract static class DocumentAdapter implements DocumentListener {
        public void removeUpdate(DocumentEvent e) { insertUpdate(null); }
        public void changedUpdate(DocumentEvent e) { insertUpdate(null); }
    }
    
    private static WeakReference iconChooser;
    /**
     * Returns an instance of {@link javax.swing.JFileChooser} permitting
     * selection only a regular <em>icon</em>.
     */
    public static JFileChooser getIconFileChooser() {
        if (iconChooser != null) {
            JFileChooser choose = (JFileChooser)iconChooser.get();
            if (choose != null) {
                return choose;
            }
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        FileFilter[] filters = chooser.getChoosableFileFilters();
        for (int i = 0; i < filters.length; i++) {
            chooser.removeChoosableFileFilter(filters[i]);
        }
        chooser.addChoosableFileFilter(new IconFilter());
        chooser.setFileView(new FileView() {
            public Icon getIcon(File f) {
                // Show icons right in the chooser, to make it easier to find
                // the right one.
                if (f.getName().endsWith(".gif") || f.getName().endsWith(".png")) { // NOI18N
                    Icon icon = new ImageIcon(f.getAbsolutePath());
                    if (icon.getIconWidth() == 16 && icon.getIconHeight() == 16) {
                        return icon;
                    }
                }
                return null;
            }
        });
        iconChooser = new WeakReference(chooser);
        return chooser;
    }
    
    /**
     * tries to set the selected file according to currently existing data.
     * Will se it only if the String represents a file path that exists.
     */
    public static JFileChooser getIconFileChooser(String oldValue) {
        JFileChooser chooser = getIconFileChooser();
        String iconText = oldValue.trim();
        if ( iconText.length() > 0) {
            File fil = new File(iconText);
            if (fil.exists()) {
                chooser.setSelectedFile(fil);
            }
        }
        return chooser;
    }
    
    /**
     * Create combobox containing packages from the given {@link SourceGroup}.
     */
    public static JComboBox createPackageComboBox(SourceGroup srcRoot) {
        JComboBox packagesComboBox = new JComboBox(PackageView.createListView(srcRoot));
        packagesComboBox.setRenderer(PackageView.listRenderer());
        return packagesComboBox;
    }
    
    /**
     * Returns true for valid package name.
     */
    public static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns a string suitable for text areas respresenting content of {@link
     * CreatedModifiedFiles} <em>paths</em>.
     *
     * @param relPaths should be either
     *        {@link CreatedModifiedFiles#getCreatedPaths()} or
     *        {@link CreatedModifiedFiles#getModifiedPaths()}.
     */
    public static String generateTextAreaContent(String[] relPaths) {
        StringBuffer sb = new StringBuffer();
        if (relPaths.length > 0) {
            for (int i = 0; i < relPaths.length; i++) {
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(relPaths[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Calls in turn {@link #createLayerPresenterComboModel(Project, String,
     * Map)} with {@link Collections#EMPTY_MAP} as a third parameter.
     */
    public static ComboBoxModel createLayerPresenterComboModel(
            final Project project, final String sfsRoot) {
        return createLayerPresenterComboModel(project, sfsRoot, Collections.EMPTY_MAP);
    }
    
    /**
     * Returns {@link ComboBoxModel} containing {@link #LayerItemPresenter}s
     * wrapping all folders under the given <code>sfsRoot</code>.
     *
     * @param excludeAttrs {@link Map} of pairs String - Object used to filter
     *                     out folders which have one or more attribute(key)
     *                     with a corresponding value.
     */
    public static ComboBoxModel createLayerPresenterComboModel(
            final Project project, final String sfsRoot, final Map/*<Object, String>*/ excludeAttrs) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        try {
            FileSystem sfs = LayerUtils.getEffectiveSystemFilesystem(project);
            FileObject root = sfs.getRoot().getFileObject(sfsRoot);
            if (root != null) {
                Collection/*<FileObject>*/ subFolders = getFolders(root, excludeAttrs);
                SortedSet/*<LayerItemPresenter>*/ presenters = new TreeSet();
                for (Iterator it = subFolders.iterator(); it.hasNext();) {
                    presenters.add(new LayerItemPresenter((FileObject) it.next(), root));
                }
                for (Iterator it = presenters.iterator(); it.hasNext();) {
                    model.addElement(it.next());
                }
            }
        } catch (IOException exc) {
            Util.err.notify(exc);
        }
        return model;
    }
    
    public static class LayerItemPresenter implements Comparable {
        
        private String displayName;
        private FileObject item;
        private FileObject root;
        private boolean contentType;
        
        public LayerItemPresenter(final FileObject item,
                final FileObject root,
                final boolean contentType) {
            this.item = item;
            this.root = root;
            this.contentType = contentType;
        }
        
        public LayerItemPresenter(final FileObject item, final FileObject root) {
            this(item, root, false);
        }
        
        public FileObject getFileObject() {
            return item;
        }
        
        public String getFullPath() {
            return item.getPath();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = computeDisplayName();
            }
            return displayName;
        }
        
        public String toString() {
            return getDisplayName();
        }
        
        public int compareTo(Object o) {
            return Collator.getInstance().compare(getDisplayName(),
                    ((LayerItemPresenter) o).getDisplayName());
        }
        
        private static String getFileObjectName(FileObject fo) {
            String name = null;
            try {
                name = fo.getFileSystem().getStatus().annotateName(
                        fo.getNameExt(), Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                name = fo.getName();
            }
            return name;
        }
        
        private String computeDisplayName() {
            FileObject displayItem = contentType ? item.getParent() : item;
            String displaySeparator = contentType ? "/" : " | "; // NOI18N
            Stack s = new Stack();
            s.push(getFileObjectName(displayItem));
            FileObject parent = displayItem.getParent();
            while (!root.getPath().equals(parent.getPath())) {
                s.push(getFileObjectName(parent));
                parent = parent.getParent();
            }
            StringBuffer sb = new StringBuffer();
            sb.append(s.pop());
            while (!s.empty()) {
                sb.append(displaySeparator + s.pop());
            }
            return sb.toString();
        }
        
    }
    
    /** 
     * Returns path relative to the root of the SFS. May return
     * <code>null</code> for empty String or user's custom non-string items.
     * Also see {@link Util#isValidSFSPath(String)}.
     */
    public static String getSFSPath(final JComboBox lpCombo, final String supposedRoot) {
        Object editorItem = lpCombo.getEditor().getItem();
        String path = null;
        if (editorItem instanceof LayerItemPresenter) {
            path = ((LayerItemPresenter) editorItem).getFullPath();
        } else if (editorItem instanceof String) {
            String editorItemS = ((String) editorItem).trim();
            if (editorItemS.length() > 0) {
                path = searchLIPCategoryCombo(lpCombo, editorItemS);
                if (path == null) {
                    // entered by user - absolute and relative are supported...
                    path = editorItemS.startsWith(supposedRoot) ? editorItemS :
                        supposedRoot + '/' + editorItemS;
                }
            }
        }
        return path;
    }
    
    public static NbModuleProject chooseSuiteComponent(Component parent, SuiteProject suite) {
        NbModuleProject suiteComponent = null;
        Project project = chooseProject(parent);
        if (project != null) {
            if (SuiteUtils.getSubProjects(suite).contains(project)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_SuiteAlreadyContainsProject",
                        ProjectUtils.getInformation(suite).getDisplayName(),
                        ProjectUtils.getInformation(project).getDisplayName())));
                return null;
            }
            NbModuleTypeProvider nmtp = (NbModuleTypeProvider) project.
                    getLookup().lookup(NbModuleTypeProvider.class);
            if (nmtp == null) { // not netbeans module
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNonNBModule",
                        ProjectUtils.getInformation(project).getDisplayName())));
            } else if (nmtp.getModuleType() == NbModuleTypeProvider.SUITE_COMPONENT) {
                Object[] params = new Object[] {
                    ProjectUtils.getInformation(project).getDisplayName(),
                            getSuiteProjectName(project),
                            getSuiteProjectDirectory(project),
                            ProjectUtils.getInformation(suite).getDisplayName(),
                };
                NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(UIUtil.class, "MSG_MoveFromSuiteToSuite", params),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                DialogDisplayer.getDefault().notify(confirmation);
                if (confirmation.getValue() == NotifyDescriptor.OK_OPTION) {
                    suiteComponent = (NbModuleProject) project;
                }
            } else if (nmtp.getModuleType() == NbModuleTypeProvider.STANDALONE) {
                suiteComponent = (NbModuleProject) project;
            } else if (nmtp.getModuleType() == NbModuleTypeProvider.NETBEANS_ORG) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNBORGModule",
                        ProjectUtils.getInformation(project).getDisplayName())));
            }
        }
        return suiteComponent;
    }

    /**
     * Appropriately renders {@link Project}s. For others instances delegates
     * to {@link DefaultListCellRenderer}.
     */
    public static ListCellRenderer createProjectRenderer() {
        return new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (!(value instanceof Project)) {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                ProjectInformation pi = ProjectUtils.getInformation((Project) value);
                JLabel c = (JLabel) super.getListCellRendererComponent(list, pi.getDisplayName(), index, isSelected, cellHasFocus);
                c.setIcon(pi.getIcon());
                return this;
            }
        };
    }
    
    /**
     * Searches LayerItemPresenter combobox by the item's display name.
     */
    private static String searchLIPCategoryCombo(final JComboBox lpCombo, final String displayName) {
        String path = null;
        for (int i = 0; i < lpCombo.getItemCount(); i++) {
            Object item = lpCombo.getItemAt(i);
            if (!(item instanceof LayerItemPresenter)) {
                continue;
            }
            LayerItemPresenter presenter = (LayerItemPresenter) lpCombo.getItemAt(i);
            if (displayName.equals(presenter.getDisplayName())) {
                path = presenter.getFullPath();
                break;
            }
        }
        return path;
    }
    
    private static Project chooseProject(Component parent) {
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(parent);
        Project project = null;
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            UIUtil.setProjectChooserDirParent(projectDir);
            try {
                project = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
        return project;
    }
    
    private static File getSuiteDirectory(Project suiteComp) {
        SuiteProvider sp = (SuiteProvider) suiteComp.
                getLookup().lookup(SuiteProvider.class);
        assert sp != null;
        assert sp.getSuiteDirectory() != null : "Invalid suite provider for: " // NOI18N
                + suiteComp.getProjectDirectory();
        return sp.getSuiteDirectory();
    }
    
    private static String getSuiteProjectDirectory(Project suiteComp) {
        return getSuiteDirectory(suiteComp).getAbsolutePath();
    }
    
    private static String getSuiteProjectName(Project suiteComp) {
        return Util.getDisplayName(FileUtil.toFileObject(getSuiteDirectory(suiteComp)));
    }
    
    private static Collection/*<FileObject>*/ getFolders(final FileObject root, final Map excludeAttrs) {
        Collection/*<FileObject>*/ folders = new HashSet();
        SUBFOLDERS: for (Enumeration subFolders = root.getFolders(false); subFolders.hasMoreElements(); ) {
            FileObject subFolder = (FileObject) subFolders.nextElement();
            for (Iterator it = excludeAttrs.entrySet().iterator(); it.hasNext();) {
                Map.Entry me = (Map.Entry) it.next();
                if (me.getValue().equals(subFolder.getAttribute((String) me.getKey()))) {
                    continue SUBFOLDERS;
                }
            }
            folders.add(subFolder);
            folders.addAll(getFolders(subFolder, excludeAttrs));
        }
        return folders;
    }
    
    private static final class IconFilter extends FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory() ||
                    pathname.getName().toLowerCase().endsWith("gif") || // NOI18N
                    pathname.getName().toLowerCase().endsWith("png"); // NOI18N
        }
        public String getDescription() {
            return "*.gif, *.png"; // NOI18N
        }
    }
    
}

