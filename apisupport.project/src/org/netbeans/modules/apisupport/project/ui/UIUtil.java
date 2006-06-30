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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
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
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolderOpen.gif"; // NOI18N
    
    private UIUtil() {}
    
    public static String keyToLogicalString(KeyStroke keyStroke) {
        String keyDesc = Utilities.keyToString(keyStroke);
        int dash = keyDesc.indexOf('-');
        return dash == -1 ? keyDesc :
            keyDesc.substring(0, dash).replace('C', 'D').replace('A', 'O') + keyDesc.substring(dash);
    }
    
    public static String keyStrokeToString(KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers();
        StringBuffer sb = new StringBuffer();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0) {
            sb.append("Ctrl+"); // NOI18N
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0) {
            sb.append("Alt+"); // NOI18N
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0) {
            sb.append("Shift+"); // NOI18N
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0) {
            sb.append("Meta+"); // NOI18N
        }
        if (keyStroke.getKeyCode() != KeyEvent.VK_SHIFT &&
                keyStroke.getKeyCode() != KeyEvent.VK_CONTROL &&
                keyStroke.getKeyCode() != KeyEvent.VK_META &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT_GRAPH) {
            sb.append(Utilities.keyToString(
                    KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0)));
        }
        return sb.toString();
    }
    
    public static KeyStroke stringToKeyStroke(String keyStroke) {
        int modifiers = 0;
        if (keyStroke.startsWith("Ctrl+")) { // NOI18N
            modifiers |= InputEvent.CTRL_DOWN_MASK;
            keyStroke = keyStroke.substring(5);
        }
        if (keyStroke.startsWith("Alt+")) { // NOI18N
            modifiers |= InputEvent.ALT_DOWN_MASK;
            keyStroke = keyStroke.substring(4);
        }
        if (keyStroke.startsWith("Shift+")) { // NOI18N
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
            keyStroke = keyStroke.substring(6);
        }
        if (keyStroke.startsWith("Meta+")) { // NOI18N
            modifiers |= InputEvent.META_DOWN_MASK;
            keyStroke = keyStroke.substring(5);
        }
        KeyStroke ks = Utilities.stringToKey(keyStroke);
        if (ks == null) {
            return null;
        }
        KeyStroke result = KeyStroke.getKeyStroke(ks.getKeyCode(), modifiers);
        return result;
    }
    
    /**
     * Returns multi keystroke for given text representation of shortcuts
     * (like Alt+A B). Returns null if text is not parsable, and empty array
     * for empty string.
     */
    public static KeyStroke[] stringToKeyStrokes(String keyStrokes) {
        String delim = " "; // NOI18N
        if (keyStrokes.length() == 0) {
            return new KeyStroke [0];
        }
        StringTokenizer st = new StringTokenizer(keyStrokes, delim);
        List result = new ArrayList();
        while (st.hasMoreTokens()) {
            String ks = st.nextToken().trim();
            KeyStroke keyStroke = stringToKeyStroke(ks);
            if (keyStroke == null) { // text is not parsable
                return null;
            }
            result.add(keyStroke);
        }
        return (KeyStroke[]) result.toArray(new KeyStroke [result.size()]);
    }
    
    public static String keyStrokesToString(final KeyStroke[] keyStrokes) {
        StringBuffer sb = new StringBuffer(UIUtil.keyStrokeToString(keyStrokes [0]));
        int i, k = keyStrokes.length;
        for (i = 1; i < k; i++)
            sb.append(' ').append(UIUtil.keyStrokeToString(keyStrokes [i]));
        
        String newShortcut = sb.toString();
        return newShortcut;
    }
    
    public static String keyStrokesToLogicalString(final KeyStroke[] keyStrokes) {
        StringBuffer sb = new StringBuffer(UIUtil.keyToLogicalString(keyStrokes [0]));
        int i, k = keyStrokes.length;
        for (i = 1; i < k; i++)
            sb.append(' ').append(UIUtil.keyToLogicalString((keyStrokes [i])));
        
        String newShortcut = sb.toString();
        return newShortcut;
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
     * @param icon file representing icon
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return warning or empty <code>String</code>
     */
    public static String getIconDimensionWarning(File icon, int expectedWidth, int expectedHeight) {
        Dimension real = new Dimension(UIUtil.getIconDimension(icon));
        if (real.height == expectedHeight && real.width == expectedWidth) {
            return "";
        }
        return NbBundle.getMessage(UIUtil.class, "MSG_WrongIconSize",new Object[]  {
            Integer.toString(real.width), 
            Integer.toString(real.height), 
            Integer.toString(expectedWidth), 
            Integer.toString(expectedHeight)
        });
    }

    /**
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return warning 
     */
    public static String getNoIconSelectedWarning(int expectedWidth, int expectedHeight) {
        return NbBundle.getMessage(UIUtil.class, "MSG_NoIconSelected",new Object[]  {
            Integer.toString(expectedWidth), 
            Integer.toString(expectedHeight)
        });        
    }
    
    /**
     * @param icon file representing icon
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return true if icon corresponds to expected dimension
     */
    public static boolean isValidIcon(final File icon, int expectedWidth, int expectedHeight) {
        Dimension iconDimension = UIUtil.getIconDimension(icon);
        return (expectedWidth == iconDimension.getWidth() &&
                expectedHeight == iconDimension.getHeight());
    }
    
    /**
     * @param icon file representing icon
     * @return width and height of icon encapsulated into {@link java.awt.Dimension}
     */
    public static Dimension getIconDimension(final File icon) {
        try {
            ImageIcon imc = new ImageIcon(icon.toURI().toURL());
            return new Dimension(imc.getIconWidth(), imc.getIconHeight());
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new Dimension(-1, -1);
    }
    
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
        final JFileChooser chooser = new IconFileChooser();        
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
        StringTokenizer tukac = new StringTokenizer(str, "."); // NOI18N
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
            int res = Collator.getInstance().compare(getDisplayName(),
                    ((LayerItemPresenter) o).getDisplayName());
            if (res != 0) {
                return res;
            } else {
                return getFullPath().compareTo(((LayerItemPresenter) o).getFullPath());
            }
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
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = null;
        Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
        if (baseIcon != null) {
            base = UIUtil.convertToImage(baseIcon);
        } else {
            base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263
            if (base == null) { // fallback to our owns
                base = Utilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
    /**
     * Converts given icon to a {@link java.awt.Image}.
     *
     * @param icon {@link javax.swing.Icon} to be converted.
     */
    public static Image convertToImage(final Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            BufferedImage bImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bImage.getGraphics();
            icon.paintIcon(new JLabel(), g, 0, 0);
            g.dispose();
            return bImage;
        }
    }
    
    public static NbModuleProject runLibraryWrapperWizard(final Project suiteProvider) {
        NewNbModuleWizardIterator iterator = NewNbModuleWizardIterator.createLibraryModuleIterator(suiteProvider);
        return UIUtil.runProjectWizard(iterator, "CTL_NewLibraryWrapperProject"); // NOI18N
    }
    
    public static NbModuleProject runProjectWizard(
            final NewNbModuleWizardIterator iterator, final String titleBundleKey) {
        WizardDescriptor wd = new WizardDescriptor(iterator);
        wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wd.setTitle(NbBundle.getMessage(UIUtil.class, titleBundleKey));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setVisible(true);
        dialog.toFront();
        NbModuleProject project = null;
        boolean cancelled = wd.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            FileObject folder = iterator.getCreateProjectFolder();
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(folder);
                OpenProjects.getDefault().open(new Project[] { project }, false);
                if (wd.getProperty("setAsMain") == Boolean.TRUE) { // NOI18N
                    OpenProjects.getDefault().setMainProject(project);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return project;
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
        File suiteDir = SuiteUtils.getSuiteDirectory(suiteComp);
        assert suiteDir != null : "Invalid suite provider for: " // NOI18N
                + suiteComp.getProjectDirectory();
        return suiteDir;
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
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("gif") || // NOI18N
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("png"); // NOI18N
        }
        public String getDescription() {
            return "*.gif, *.png"; // NOI18N
        }
    }
    
    /**
     * Show an OK/cancel-type dialog with customized button texts.
     * Only a separate method because it is otherwise cumbersome to replace
     * the OK button with a button that is set as the default.
     * @param title the dialog title
     * @param message the body of the message (usually HTML text)
     * @param acceptButton a label for the default accept button; should not use mnemonics
     * @param cancelButton a label for the cancel button (or null for default); should not use mnemonics
     * @param messageType {@link NotifyDescriptor#WARNING_MESSAGE} or similar
     * @return true if user accepted the dialog
     */
    public static boolean showAcceptCancelDialog(String title, String message, String acceptButton, String cancelButton, int messageType) {
        DialogDescriptor d = new DialogDescriptor(message, title);
        d.setModal(true);
        JButton accept = new JButton(acceptButton);
        accept.setDefaultCapable(true);
        d.setOptions(new Object[] {
            accept,
            cancelButton != null ? new JButton(cancelButton) : NotifyDescriptor.CANCEL_OPTION,
        });
        d.setMessageType(messageType);
        return DialogDisplayer.getDefault().notify(d).equals(accept);
    }
    
    private static class IconFileChooser extends JFileChooser {
        private final JTextField iconInfo = new javax.swing.JTextField();        
        private  IconFileChooser() {
            JPanel accessoryPanel = getAccesoryPanel(iconInfo);
            setDialogTitle(NbBundle.getMessage(UIUtil.class, "TITLE_IconDialog"));//NOI18N
            setAccessory(accessoryPanel);
            setAcceptAllFileFilterUsed(false);
            setFileSelectionMode(JFileChooser.FILES_ONLY);
            setMultiSelectionEnabled(false);
            addChoosableFileFilter(new IconFilter());
            setFileView(new FileView() {
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
                public String getName(File f) {
                    File f2 = getSelectedFile();
                    if (f2 != null && (f2.getName().endsWith(".gif") || f2.getName().endsWith(".png"))) { // NOI18N
                        Icon icon = new ImageIcon(f2.getAbsolutePath());
                        StringBuffer sb = new StringBuffer();
                        sb.append(f2.getName()).append(" [");//NOI18N
                        sb.append(icon.getIconWidth()).append("x").append(icon.getIconHeight());
                        sb.append("]");
                        setApproveButtonToolTipText(sb.toString());
                        iconInfo.setText(sb.toString());
                    } else {
                        iconInfo.setText("");
                    }
                    return super.getName(f);
                }
                
            });            
        }
        
        private static JPanel getAccesoryPanel(final JTextField iconInfo) {
            iconInfo.setColumns(15);
            iconInfo.setEditable(false);
            
            JPanel accessoryPanel = new javax.swing.JPanel();
            JPanel inner = new javax.swing.JPanel();
            JLabel iconInfoLabel = new javax.swing.JLabel();
            accessoryPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
            
            inner.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
            
            iconInfoLabel.setLabelFor(iconInfo);
            org.openide.awt.Mnemonics.setLocalizedText(iconInfoLabel, NbBundle.getMessage(UIUtil.class, "LBL_IconInfo"));//NOI18N
            inner.add(iconInfoLabel);
            
            inner.add(iconInfo);
            
            accessoryPanel.add(inner);
            return accessoryPanel;
        }
    }
}
