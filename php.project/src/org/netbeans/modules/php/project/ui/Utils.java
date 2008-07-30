/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.AbstractList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Miscellaneous UI utils.
 * @author Tomas Mysik
 */
public final class Utils {

    // protocol://[user[:password]@]domain[:port]/rel/path?query#anchor
    public static final String URL_REGEXP = "^https?://([^/?#: ]+(:[^/?#: ]+)?@)?[^/?#: ]+(:\\d+)?/[^?# ]*(\\?[^#]*)?(#\\w*)?$"; // NOI18N
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);
    private static final char[] INVALID_FILENAME_CHARS = new char[] {'/', '\\', '|', ':', '*', '?', '"', '<', '>'}; // NOI18N

    private Utils() {
    }

    public static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * @return the selected folder or <code>null</code>.
     */
    public static File browseLocationAction(final Component parent, File currentDirectory, String title) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (currentDirectory != null
                && currentDirectory.exists()) {
            chooser.setSelectedFile(currentDirectory);
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
            return FileUtil.normalizeFile(chooser.getSelectedFile());
        }
        return null;
    }

    /**
     * @return the selected folder or <code>null</code>.
     */
    public static File browseLocalServerAction(final Component parent, final JComboBox localServerComboBox,
            final MutableComboBoxModel localServerComboBoxModel, File preselected, String newSubfolderName, String title) {
        if (preselected == null) {
            LocalServer ls = (LocalServer) localServerComboBox.getSelectedItem();
            if (ls.getDocumentRoot() != null && ls.getDocumentRoot().length() > 0) {
                preselected = new File(ls.getDocumentRoot());
            }
        }
        File newLocation = browseLocationAction(parent, preselected, title);
        if (newLocation == null) {
            return null;
        }

        LastUsedFolders.setCopyFiles(newLocation);

        File file = null;
        if (newSubfolderName == null) {
            file = newLocation;
        } else {
            file = new File(newLocation, newSubfolderName);
        }
        String projectLocation = file.getAbsolutePath();
        for (int i = 0; i < localServerComboBoxModel.getSize(); i++) {
            LocalServer element = (LocalServer) localServerComboBoxModel.getElementAt(i);
            if (projectLocation.equals(element.getSrcRoot())) {
                localServerComboBox.setSelectedIndex(i);
                break;
            }
        }
        LocalServer localServer = new LocalServer(newLocation.getAbsolutePath(), projectLocation);
        localServerComboBoxModel.addElement(localServer);
        localServerComboBox.setSelectedItem(localServer);
        return newLocation;
    }

    public static List getAllItems(final JComboBox comboBox) {
        return new AbstractList() {
            public Object get(int i) {
                return comboBox.getItemAt(i);
            }

            public int size() {
                return comboBox.getItemCount();
            }
        };
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            // ignored
        }
        return null;
    }

    /**
     * Check whether the provided String is valid file name. An empty String is considered to be invalid.
     * @param fileName file name.
     * @return <code>true</true> if the provided String is valid file name.
     */
    public static boolean isValidFileName(String fileName) {
        assert fileName != null;
        if (fileName.trim().length() == 0) {
            return false;
        }
        for (char ch : INVALID_FILENAME_CHARS) {
            if (fileName.indexOf(ch) != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the provided File has a valid file name. Only the non-existing file names in the file paths are checked.
     * It means that if you pass existing directory, no check is done.
     * <p>
     * For example for <em>C:\Documents And Settings\ExistingDir\NonExistingDir\NonExistingDir2\Newdir</em> the last free file names
     * are checked.
     * <p>
     * File is not {@link FileUtil#normalizeFile(java.io.File) normalized}, caller should do it if needed.
     * @param file File to check.
     * @return <code>true</true> if the provided File has valid file name.
     * @see #isValidFileName(java.lang.String)
     */
    public static boolean isValidFileName(File file) {
        assert file != null;
        File tmp = file;
        while (tmp != null && !tmp.exists()) {
            // #132520
            if (tmp.isAbsolute() && tmp.getParentFile() == null) {
                return true;
            } else if (!isValidFileName(tmp.getName())) {
                return false;
            }
            tmp = tmp.getParentFile();
        }
        return true;
    }

    /**
     * Validate the path and get the error message or <code>null</code> if it's all right.
     * @param projectPath the path to validate.
     * @param type the type for error messages, currently "Project", "Sources" and "Folder".
     *             Add other to Bundle.properties file if more types are needed.
     * @param allowNonEmpty <code>true</code> if the folder can exist and can be non empty.
     * @param allowInRoot  <code>true</code> if the folder can exist and can be a root directory "/"
     *                     (this parameter is taken into account only for *NIX OS).
     * @return localized error message in case of error, <code>null</code> otherwise.
     * @see #validateProjectDirectory(java.io.File, java.lang.String, boolean, boolean)
     */
    public static String validateProjectDirectory(String projectPath, String type, boolean allowNonEmpty,
            boolean allowInRoot) {
        return validateProjectDirectory(new File(projectPath), type, allowNonEmpty, allowInRoot);
    }

    /**
     * Validate the file and get the error message or <code>null</code> if it's all right.
     * @param project the file to validate.
     * @param type the type for error messages, currently "Project", "Sources" and "Folder".
     *             Add other to Bundle.properties file if more types are needed.
     * @param allowNonEmpty <code>true</code> if the folder can exist and can be non empty.
     * @param allowInRoot  <code>true</code> if the folder can exist and can be a root directory "/"
     *                     (this parameter is taken into account only for *NIX OS).
     * @return localized error message in case of error, <code>null</code> otherwise.
     */
    public static String validateProjectDirectory(File project, String type, boolean allowNonEmpty,
            boolean allowInRoot) {
        assert project != null;
        assert type != null;

        // #131753
        if (!project.isAbsolute()) {
            return NbBundle.getMessage(Utils.class, "MSG_" + type + "NotAbsolute");
        }

        // not allow to create project on unix root folder, see #82339
        if (!allowInRoot && Utilities.isUnix()) {
            File cfl = Utils.getCanonicalFile(project);
            if (cfl != null && (cfl.getParentFile() == null || cfl.getParentFile().getParent() == null)) {
                return NbBundle.getMessage(Utils.class, "MSG_" + type + "InRootNotSupported");
            }
        }

        final File destFolder = project.getAbsoluteFile();
        if (Utils.getCanonicalFile(destFolder) == null) {
            return NbBundle.getMessage(Utils.class, "MSG_Illegal" + type + "Location");
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(Utils.class, "MSG_" + type + "FolderReadOnly");
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(Utils.class, "MSG_Illegal" + type + "Location");
        }

        if (!allowNonEmpty) {
            File[] kids = destFolder.listFiles();
            if (destFolder.exists() && kids != null && kids.length > 0) {
                // Folder exists and is not empty
                return NbBundle.getMessage(Utils.class, "MSG_" + type + "FolderExists");
            }
        }
        return null;
    }

    /**
     * Validate that the project sources directory and directory for copying files are "independent". It means
     * that the sources isn't underneath the target directory and vice versa. Both paths have to be normalized.
     * @param sources project sources.
     * @param copyTarget directory for copying files.
     * @return <code>true</code> if the directories are "independent".
     * @see #subdirectories(java.lang.String, java.lang.String)
     */
    public static String validateSourcesAndCopyTarget(String sources, String copyTarget) {
        if (subdirectories(sources, copyTarget)) {
            return NbBundle.getMessage(Utils.class, "MSG_SourcesEqualCopyTarget");
        }
        return null;
    }

    /**
     * Check whether the <em>dir1</em> is underneath the <em>dir2</em> and vice versa. Both paths have to be normalized.
     * @param dir1 a directory.
     * @param dir2 a directory.
     * @return <code>true</code> if the directories are subdirectories.
     */
    public static boolean subdirectories(String dir1, String dir2) {
        assert dir1 != null;
        assert dir2 != null;
        // handle "/myDir" and "/myDirectory"
        if (!dir1.endsWith(File.separator)) {
            dir1 = dir1 + File.separator;
        }
        if (!dir2.endsWith(File.separator)) {
            dir2 = dir2 + File.separator;
        }
        return dir1.startsWith(dir2) || dir2.startsWith(dir1);
    }

    /**
     * Validate that the text contains only ASCII characters. If not, return an error message.
     * @param text the text to validate, can be <code>null</code>.
     * @param propertyName property name of the given text, e.g. "Project folder name".
     * @return an error message in case that the text contains non-ASCII characters, <code>null</null> otherwise.
     * @see #isAsciiPrintable(char)
     */
    public static String validateAsciiText(String text, String propertyName) {
        assert propertyName != null;
        if (text == null) {
            return null;
        }
        for (int i = 0; i < text.length(); ++i) {
            if (!isAsciiPrintable(text.charAt(i))) {
                return NbBundle.getMessage(Utils.class, "MSG_NonAsciiCharacterFound", propertyName);
            }
        }
        return null;
    }

    // from commons-lang
    /**
     * <p>Checks whether the character is ASCII 7 bit printable.</p>
     *
     * <pre>
     *   Utils.isAsciiPrintable('a')  = true
     *   Utils.isAsciiPrintable('A')  = true
     *   Utils.isAsciiPrintable('3')  = true
     *   Utils.isAsciiPrintable('-')  = true
     *   Utils.isAsciiPrintable('\n') = false
     *   Utils.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch the character to check.
     * @return <code>true</code> if between 32 and 126 inclusive.
     */
    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * Browse for a file from the given directory and update the content of the text field.
     * @param folder folder to browse files from.
     * @param textField textfield to update.
     */
    public static void browseFolderFile(FileObject folder, JTextField textField) {
        textField.setText(browseFolderFile(folder, textField.getText()));
    }

    /**
     * Browse for a file from the given directory and return the relative path or <code>null</code> if nothing selected.
     * @param folder folder to browse files from.
     * @param preselected the preselected value, can be null.
     * @return the relative path to folder or <code>null</code> if nothing selected.
     */
    public static String browseFolderFile(FileObject folder, String preselected) {
        FileObject selected = BrowseFolders.showDialog(new FileObject[] {folder}, DataObject.class, securePreselected(preselected, true));
        if (selected != null) {
            return PropertyUtils.relativizeFile(FileUtil.toFile(folder), FileUtil.toFile(selected));
        }
        return null;
    }

    /**
     * Browse for a file from sources of a project and update the content of the text field.
     * @param project project to get sources from.
     * @param textField textfield to update.
     */
    public static void browseSourceFile(Project project, JTextField textField) {
        textField.setText(browseSource(project, textField.getText(), false));
    }

    /**
     * Browse for a file from sources of a project and return the relative path or <code>null</code> if nothing selected.
     * @param project project to get sources from.
     * @param preselected the preselected value, can be null.
     * @return the relative path to folder or <code>null</code> if nothing selected.
     */
    public static String browseSourceFile(Project project, String preselected) {
        return browseSource(project, preselected, false);
    }

    /**
     * Browse for a directory from sources of a project and update the content of the text field.
     * @param project project to get sources from.
     * @param textField textfield to update.
     */
    public static void browseSourceFolder(Project project, JTextField textField) {
        textField.setText(browseSource(project, textField.getText(), true));
    }

    /**
     * Browse for a directory from sources of a project and return the relative path or <code>null</code> if nothing selected.
     * @param project project to get sources from.
     * @param preselected the preselected value, can be null.
     * @return the relative path to folder or <code>null</code> if nothing selected.
     */
    public static String browseSourceFolder(Project project, String preselected) {
        return browseSource(project, preselected, true);
    }

    private static String browseSource(Project project, String preselected, boolean selectDirectory) {
        SourceGroup[] sourceGroups = org.netbeans.modules.php.project.Utils.getSourceGroups(project);
        assert sourceGroups.length == 1;
        assert sourceGroups[0] != null;
        File rootFolder = FileUtil.toFile(sourceGroups[0].getRootFolder());
        FileObject selected = BrowseFolders.showDialog(sourceGroups,
                selectDirectory ? DataFolder.class : DataObject.class, securePreselected(preselected, !selectDirectory));
        if (selected != null) {
            return PropertyUtils.relativizeFile(rootFolder, FileUtil.toFile(selected));
        }
        return null;
    }

    private static String securePreselected(String preselected, boolean removeExtension) {
        if (preselected == null) {
            return null;
        }
        String secure = null;
        if (preselected.length() > 0) {
            secure = preselected.replace(File.separatorChar, '/'); // NOI18N
            if (removeExtension) {
                // e.g. searching in nodes => no file extension can be there
                secure = secure.substring(0, secure.lastIndexOf(".")); // NOI18N
            }
        }
        return secure;
    }

    public static class EncodingModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = -3139920099217726436L;

        public EncodingModel() {
            this(null);
        }

        public EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null && originalEncoding != null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset(originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(EncodingModel.class.getName()).info("IllegalCharsetName: " + originalEncoding);
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }

    public static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 3196531352192214602L;

        public EncodingRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    private static class UnknownCharset extends Charset {

        UnknownCharset(String name) {
            super(name, new String[0]);
        }

        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }
}
