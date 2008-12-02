/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.openide.filesystems;

import org.openide.util.*;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/**
 * Utility class for working with JFileChoosers.  In particular, remembering
 * the last-used directory for a given file is made transparent.  You pass an
 * ad-hoc string key to the constructor (the fully qualified name of the
 * calling class is good for uniqueness, and there is a constructor that takes
 * a <code>Class</code> object as an argument for this purpose).  That key is
 * used to look up the most recently-used directory from any previous invocations
 * with the same key.  This makes it easy to have your user interface
 * &ldquo;remember&rdquo; where the user keeps particular types of files, and
 * saves the user from having to navigate through the same set of directories
 * every time they need to locate a file from a particular place.
 * <p/>
 * <code>FileChooserBuilder</code>'s methods each return <code>this</code>, so
 * it is possible to chain invocations to simplify setting up a file chooser.
 * Example usage:
 * <pre>
 *      <font color="gray">//The default dir to use if no value is stored</font>
 *      File home = new File (System.getProperty("user.home") + File.separator + "lib");
 *      <font color="gray">//Now build a file chooser and invoke the dialog in one line of code</font>
 *      <font color="gray">//&quot;libraries-dir&quot; is our unique key</font>
 *      File toAdd = new FileChooserBuilder ("libraries-dir").setTitle("Add Library").
 *              setDefaultWorkingDirectory(home).setApproveText("Add").showOpenDialog();
 *      <font color="gray">//Result will be null if the user clicked cancel or closed the dialog w/o OK</font>
 *      if (toAdd != null) {
 *          //do something
 *      }
 *</pre>
 * <p/>
 * Instances of this class are intended to be thrown away after use.  Typically
 * you create a builder, set it to create file choosers as you wish, then
 * use it to show a dialog or create a file chooser you then do something
 * with.
 * <p/>
 * Supports the most common subset of JFileChooser functionality;  if you
 * need to do something exotic with a file chooser, you are probably better
 * off creating your own.
 * <p/>
 * <b>Note:</b> If you use the constructor that takes a <code>Class</code> object,
 * please use <code>new FileChooserBuilder(MyClass.class)</code>, not
 * <code>new FileChooserBuilder(getClass())</code>.  This avoids unexpected
 * behavior in the case of subclassing.
 *
 * @author Tim Boudreau
 */
public class FileChooserBuilder {
    private boolean dirsOnly;
    private BadgeProvider badger;
    private String title;
    private String approveText;
    //Just in case...
    private static boolean PREVENT_SYMLINK_TRAVERSAL =
            !Boolean.getBoolean("allow.filechooser.symlink.traversal"); //NOI18N
    private final String dirKey;
    private File failoverDir;
    private FileFilter filter;
    private boolean fileHiding;
    private boolean controlButtonsShown = true;
    private String aDescription;
    private boolean filesOnly;
    private static final boolean DONT_STORE_DIRECTORIES =
            Boolean.getBoolean("forget.recent.dirs");
    /**
     * Create a new FileChooserBuilder using the name of the passed class
     * as the metadata for looking up a starting directory from previous
     * application sessions or invocations.
     * @param type A non-null class object, typically the calling class
     */
    public FileChooserBuilder(Class type) {
        this(type.getName());
    }

    /**
     * Create a new FileChooserBuilder.  The passed key is used as a key
     * into NbPreferences to look up the directory the file chooser should
     * initially be rooted on.
     *
     * @param dirKey A non-null ad-hoc string.  If a FileChooser was previously
     * used with the same string as is passed, then the initial directory
     */
    public FileChooserBuilder(String dirKey) {
        Parameters.notNull("dirKey", dirKey);
        this.dirKey = dirKey;
    }

    /**
     * Set whether or not any file choosers created by this builder will show
     * only directories.
     * @param val true if files should not be shown
     * @return this
     */
    public FileChooserBuilder setDirectoriesOnly(boolean val) {
        dirsOnly = val;
        assert !filesOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    public FileChooserBuilder setFilesOnly(boolean val) {
        filesOnly = val;
        assert !dirsOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    /**
     * Provide an implementation of BadgeProvider which will "badge" the
     * icons of some files.
     *
     * @param provider A badge provider which will alter the icon of files
     * or folders that may be of particular interest to the user
     * @return this
     */
    public FileChooserBuilder setBadgeProvider(BadgeProvider provider) {
        this.badger = provider;
        return this;
    }

    /**
     * Set the dialog title for any JFileChoosers created by this builder.
     * @param val A localized, human-readable title
     * @return this
     */
    public FileChooserBuilder setTitle(String val) {
        title = val;
        return this;
    }

    /**
     * Set the text on the OK button for any file chooser dialogs produced
     * by this builder.
     * @param val A short, localized, human-readable string
     * @return this
     */
    public FileChooserBuilder setApproveText(String val) {
        approveText = val;
        return this;
    }

    /**
     * Set a file filter which filters the list of selectable files.
     * @param filter
     * @return this
     */
    public FileChooserBuilder setFileFilter (FileFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set the current directory which should be used <b>only if</b>
     * a last-used directory cannot be found for the key string passed
     * into this builder's constructor.
     * @param dir A directory to root any created file choosers on if
     * there is no stored path for this builder's key
     * @return this
     */
    public FileChooserBuilder setDefaultWorkingDirectory (File dir) {
        failoverDir = dir;
        return this;
    }

    /**
     * Enable file hiding in any created file choosers
     * @param fileHiding Whether or not to hide files.  Default is no.
     * @return this
     */
    public FileChooserBuilder setFileHiding(boolean fileHiding) {
        this.fileHiding = fileHiding;
        return this;
    }

    /**
     * Show/hide control buttons
     * @param val Whether or not to hide files.  Default is no.
     * @return this
     */
    public FileChooserBuilder setControlButtonsAreShown(boolean val) {
        this.controlButtonsShown = val;
        return this;
    }

    /**
     * Set the accessible description for any file choosers created by this
     * builder
     * @param aDescription The description
     * @return this
     */
    public FileChooserBuilder setAccessibleDescription(String aDescription) {
        this.aDescription = aDescription;
        return this;
    }

    /**
     * Create a JFileChooser that conforms to the parameters set in this
     * builder.
     * @return A file chooser
     */
    public JFileChooser createFileChooser() {
        JFileChooser result = new SavedDirFileChooser(dirKey, failoverDir, force);
        prepareFileChooser(result);
        return result;
    }

    private boolean force = false;
    /**
     * Force use of the failover directory - i.e. ignore the directory key
     * passed in.
     * @param val
     * @return this
     */
    public FileChooserBuilder forceUseOfDefaultWorkingDirectory(boolean val) {
        this.force = val;
        return this;
    }

    /**
     * Tries to find an appropriate component to parent the file chooser to
     * when showing a dialog.
     * @return this
     */
    private Component findDialogParent() {
        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (parent == null) {
            parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        if (parent == null) {
            Frame[] f = Frame.getFrames();
            parent = f.length == 0 ? null : f[f.length - 1];
        }
        return parent;
    }

    /**
     * Show an open dialog that allows multiple selection.
     * @return An array of files, or null if the user cancelled the dialog
     */
    public File[] showMultiOpenDialog() {
        JFileChooser chooser = createFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == result) {
            File[] files = chooser.getSelectedFiles();
            return files == null ? new File[0] : files;
        } else {
            return null;
        }
    }

    /**
     * Show an open dialog with a file chooser set up according to the
     * parameters of this builder.
     * @return A file if the user clicks the accept button and a file or
     * folder was selected at the time the user clicked cancel.
     */
    public File showOpenDialog() {
        JFileChooser chooser = createFileChooser();
        chooser.setMultiSelectionEnabled(false);
        int dlgResult = chooser.showOpenDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == dlgResult) {
            File result = chooser.getSelectedFile();
            if (result != null && !result.exists()) {
                result = null;
            }
            return result;
        } else {
            return null;
        }

    }

    /**
     * Show a save dialog with the file chooser set up according to the
     * parameters of this builder.
     * @return A file if the user clicks the accept button and a file or
     * folder was selected at the time the user clicked cancel.
     */
    public File showSaveDialog() {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showSaveDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == result) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private void prepareFileChooser(JFileChooser chooser) {
        chooser.setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY
                : filesOnly ? JFileChooser.FILES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileHidingEnabled(fileHiding);
        chooser.setControlButtonsAreShown(controlButtonsShown);
        if (title != null) {
            chooser.setDialogTitle(title);
        }
        if (approveText != null) {
            chooser.setApproveButtonText(approveText);
        }
        if (badger != null) {
            chooser.setFileView(new CustomFileView(new BadgeIconProvider(badger),
                    chooser.getFileSystemView()));
        }
        if (PREVENT_SYMLINK_TRAVERSAL) {
            FileUtil.preventFileChooserSymlinkTraversal(chooser,
                    chooser.getCurrentDirectory());
        }
        if (filter != null) {
            chooser.setFileFilter(filter);
        }
        if (aDescription != null) {
            chooser.getAccessibleContext().setAccessibleDescription(aDescription);
        }
    }

    private static final class SavedDirFileChooser extends JFileChooser {
        private final String dirKey;
        SavedDirFileChooser(String dirKey, File failoverDir, boolean force) {
            this.dirKey = dirKey;
            if (force && failoverDir != null && failoverDir.exists() && failoverDir.isDirectory()) {
                setCurrentDirectory(failoverDir);
            } else {
                String path = DONT_STORE_DIRECTORIES ? null :
                    NbPreferences.forModule(FileChooserBuilder.class).get(dirKey, null);
                if (path != null) {
                    File f = new File(path);
                    if (f.exists() && f.isDirectory()) {
                        setCurrentDirectory(f);
                    } else if (failoverDir != null) {
                        setCurrentDirectory(failoverDir);
                    }
                } else if (failoverDir != null) {
                    setCurrentDirectory(failoverDir);
                }
            }
        }

        @Override
        public int showOpenDialog(Component parent) throws HeadlessException {
            int result = super.showOpenDialog(parent);
            if (result == APPROVE_OPTION) {
                saveCurrentDir();
            }
            return result;
        }

        @Override
        public int showSaveDialog(Component parent) throws HeadlessException {
            int result = super.showSaveDialog(parent);
            if (result == APPROVE_OPTION) {
                saveCurrentDir();
            }
            return result;
        }

        private void saveCurrentDir() {
            File dir = super.getCurrentDirectory();
            if (!DONT_STORE_DIRECTORIES && dir != null && dir.exists() && dir.isDirectory()) {
                NbPreferences.forModule(FileChooserBuilder.class).put(dirKey, dir.getPath());
            }
        }
    }

    //Can open this API later if there is a use-case
    interface IconProvider {
        public Icon getIcon(File file, Icon orig);
    }

    /**
     * Provides "badges" for icons that indicate files or folders of particular
     * interest to the user.
     * @see FileChooserBuilder#setBadgeProvider
     */
    public interface BadgeProvider {
        /**
         *  Get the badge the passed file should use.  <b>Note:</b> this method
         * is called for every visible file.  The negative test (deciding
         * <i>not</i> to badge a file) should be very, very fast and immediately
         * return null.
         * @param file The file in question
         * @return an icon or null if no change to the appearance of the file
         * is needed
         */
        public Icon getBadge(File file);

        /**
         * Get the x offset for badges produced by this provider.  This is
         * the location of the badge icon relative to the real icon for the
         * file.
         * @return  a rightward pixel offset
         */
        public int getXOffset();

        /**
         * Get the y offset for badges produced by this provider.  This is
         * the location of the badge icon relative to the real icon for the
         * file.
         * @return  a rightward pixel offset
         */
        public int getYOffset();
    }

    private static final class BadgeIconProvider implements IconProvider {

        private final BadgeProvider badger;

        public BadgeIconProvider(BadgeProvider badger) {
            this.badger = badger;
        }

        public Icon getIcon(File file, Icon orig) {
            Icon badge = badger.getBadge(file);
            if (badge != null && orig != null) {
                return new MergedIcon(orig, badge, badger.getXOffset(),
                        badger.getYOffset());
            }
            return orig;
        }
    }

    private static final class CustomFileView extends FileView {

        private final IconProvider provider;
        private final FileSystemView view;

        CustomFileView(IconProvider provider, FileSystemView view) {
            this.provider = provider;
            this.view = view;
        }

        @Override
        public Icon getIcon(File f) {
            Icon result = view.getSystemIcon(f);
            result = provider.getIcon(f, result);
            return result;
        }
    }

    private static class MergedIcon implements Icon {

        private Icon icon1;
        private Icon icon2;
        private int xMerge;
        private int yMerge;

        MergedIcon(Icon icon1, Icon icon2, int xMerge, int yMerge) {
            assert icon1 != null;
            assert icon2 != null;
            this.icon1 = icon1;
            this.icon2 = icon2;

            if (xMerge == -1) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }

            if (yMerge == -1) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }

            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }

        public int getIconHeight() {
            return Math.max(icon1.getIconHeight(), yMerge + icon2.getIconHeight());
        }

        public int getIconWidth() {
            return Math.max(icon1.getIconWidth(), yMerge + icon2.getIconWidth());
        }

        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon(c, g, x, y);
            icon2.paintIcon(c, g, x + xMerge, y + yMerge);
        }
    }
}
