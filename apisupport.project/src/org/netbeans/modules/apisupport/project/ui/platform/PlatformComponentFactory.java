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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 * Factory for creating miscellaneous UI components, their models and renderers
 * as they are needed through the code of this module.
 *
 * @author Martin Krauskopf
 */
public final class PlatformComponentFactory {
    
    private static final Color INVALID_PLAF_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N
    
    /** Set of suites added by the user in <em>this</em> IDE session. */
    private static Set<String> userSuites = new TreeSet<String>(Collator.getInstance());
    
    private PlatformComponentFactory() {
        // don't allow instances
    }
    
    /**
     * Returns <code>JComboBox</code> initialized with {@link
     * NbPlatformListModel} which contains all NetBeans platform.
     */
    public static JComboBox getNbPlatformsComboxBox() {
        JComboBox plafComboBox = new JComboBox(new NbPlatformListModel());
        plafComboBox.setRenderer(new NbPlatformListRenderer());
        return plafComboBox;
    }
    
    /**
     * Returns <code>JList</code> initialized with {@link NbPlatformListModel}
     * which contains all NetBeans platform.
     */
    public static JList getNbPlatformsList() {
        JList plafList = new JList(new NbPlatformListModel());
        plafList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plafList.setCellRenderer(new NbPlatformListRenderer());
        return plafList;
    }
    
    /**
     * Returns <code>JComboBox</code> containing all suites. Also see
     * {@link #addUserSuite}.
     */
    public static JComboBox getSuitesComboBox() {
        MutableComboBoxModel model = new SuiteListModel(userSuites);
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            String suiteDir = SuiteUtils.getSuiteDirectoryPath(projects[i]);
            if (suiteDir != null) {
                model.addElement(suiteDir);
            }
        }
        JComboBox suiteCombo = new JComboBox(model);
        if (model.getSize() > 0) {
            suiteCombo.setSelectedIndex(0);
        }
        return suiteCombo;
    }
    
    /**
     * Adds <code>suiteDir</code> to the list of suites returned by the
     * {@link #getSuitesComboBox} method. Such a suites are remembered
     * <b>only</b> for the current IDE session.
     */
    public static void addUserSuite(String suiteDir) {
        userSuites.add(suiteDir);
    }
    
    public static ListCellRenderer getURLListRenderer() {
        return new URLListRenderer();
    }
    
    /**
     * Render {@link NbPlatform} using its computed display name. If computation
     * fails platform ID is used as a fallback. For <code>null</code> values
     * renders an empty string.
     * <p>Use in conjuction with {@link NbPlatformListModel}</p>
     */
    private static class NbPlatformListRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public NbPlatformListRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            NbPlatform plaf = ((NbPlatform) value);
            // NetBeans.org modules doesn't have platform at all --> null
            String text = plaf == null ? "" : plaf.getLabel(); // NOI18N
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            if (plaf != null && !plaf.isValid()) {
                setForeground(INVALID_PLAF_COLOR);
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
    /**
     * Returns model containing all <em>currently</em> registered NbPlatforms.
     * See also {@link NbPlatform#getPlatforms}.
     * <p>Use in conjuction with {@link NbPlatformListRenderer}</p>
     */
    public static class NbPlatformListModel extends AbstractListModel
            implements ComboBoxModel {
        
        private static NbPlatform[] getSortedPlatforms() {
            Set<NbPlatform> _platforms = NbPlatform.getPlatforms();
            NbPlatform[] platforms = _platforms.toArray(new NbPlatform[_platforms.size()]);
            Arrays.sort(platforms, new Comparator<NbPlatform>() {
                public int compare(NbPlatform p1, NbPlatform p2) {
                    int res = Collator.getInstance().compare(p1.getLabel(), p2.getLabel());
                    if (res != 0) {
                        return res;
                    } else {
                        return System.identityHashCode(p1) - System.identityHashCode(p2);
                    }
                }
            });
            return platforms;
        }
        
        private NbPlatform[] nbPlafs;
        private Object selectedPlaf;
        
        public NbPlatformListModel() {
            nbPlafs = getSortedPlatforms();
            if (nbPlafs.length > 0) {
                selectedPlaf = nbPlafs[0];
            }
        }
        
        public int getSize() {
            return nbPlafs.length;
        }
        
        public Object getElementAt(int index) {
            return index < nbPlafs.length ? nbPlafs[index] : null;
        }

        public void setSelectedItem(Object plaf) {
            assert plaf == null || plaf instanceof NbPlatform;
            if (selectedPlaf != plaf) {
                selectedPlaf = plaf;
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            return selectedPlaf;
        }
        
        void removePlatform(NbPlatform plaf) {
            try {
                NbPlatform.removePlatform(plaf);
                nbPlafs = getSortedPlatforms(); // refresh
                fireContentsChanged(this, 0, nbPlafs.length - 1);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        NbPlatform addPlatform(String id, String destdir, String label) {
            try {
                NbPlatform def = NbPlatform.getDefaultPlatform();
                NbPlatform plaf = def != null ?
                    NbPlatform.addPlatform(id, new File(destdir), /* #71629 */ def.getHarnessLocation(), label) :
                    // Installation somehow corrupted, but try to behave gracefully:
                    NbPlatform.addPlatform(id, new File(destdir), label);
                nbPlafs = getSortedPlatforms(); // refresh
                fireContentsChanged(this, 0, nbPlafs.length - 1);
                return plaf;
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
            return null;
        }
    }
    
    static class ModuleEntryListModel extends AbstractListModel {
        
        private ModuleEntry[] mes;
        
        ModuleEntryListModel(ModuleEntry[] mes) {
            this.mes = mes;
        }
        
        public int getSize() {
            return mes.length;
        }
        
        public Object getElementAt(int index) {
            return mes[index].getLocalizedName();
        }
    }
    
    private static class SuiteListModel extends AbstractListModel
            implements MutableComboBoxModel {
        
        private Set<String> suites = new TreeSet<String>(Collator.getInstance());
        private String selectedSuite;
        
        SuiteListModel(Set<String> suites) {
            this.suites.addAll(suites);
        }
        
        public void setSelectedItem(Object suite) {
            if (suite == null) {
                return;
            }
            if (selectedSuite != suite) {
                selectedSuite = (String) suite;
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            return selectedSuite;
        }
        
        public int getSize() {
            return suites.size();
        }
        
        public Object getElementAt(int index) {
            return suites.toArray()[index];
        }
        
        public void addElement(Object obj) {
            suites.add((String) obj);
            fireIntervalAdded(this, 0, suites.size());
        }
        
        /** Shouldn't be needed in the meantime. */
        public void insertElementAt(Object obj, int index) {
            assert false : "Who needs to insertElementAt?"; // NOI18N
        }
        
        /** Shouldn't be needed in the meantime. */
        public void removeElement(Object obj) {
            assert false : "Who needs to removeElement?"; // NOI18N
        }
        
        /** Shouldn't be needed in the meantime. */
        public void removeElementAt(int index) {
            assert false : "Who needs to call removeElementAt?"; // NOI18N
        }
    }
    
    /**
     * <code>ListModel</code> capable to manage NetBeans platform source roots.
     * <p>Can be used in conjuction with {@link URLListRenderer}</p>
     */
    static final class NbPlatformSourceRootsModel extends AbstractListModel {
        
        private NbPlatform plaf;
        private URL[] srcRoots;
        
        NbPlatformSourceRootsModel(NbPlatform plaf) {
            this.plaf = plaf;
            this.srcRoots = plaf.getSourceRoots();
        }
        
        public Object getElementAt(int index) {
            return srcRoots[index];
        }
        
        public int getSize() {
            return srcRoots.length;
        }
        
        void removeSourceRoot(URL[] srcRootToRemove) {
            try {
                plaf.removeSourceRoots(srcRootToRemove);
                this.srcRoots = plaf.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRootToRemove.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void addSourceRoot(URL srcRootToAdd) {
            try {
                plaf.addSourceRoot(srcRootToAdd);
                this.srcRoots = plaf.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveSourceRootsDown(int[] toMoveDown) {
            try {
                for (int i = 0; i < toMoveDown.length; i++) {
                    plaf.moveSourceRootDown(toMoveDown[i]);
                }
                this.srcRoots = plaf.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveSourceRootsUp(int[] toMoveUp) {
            try {
                for (int i = 0; i < toMoveUp.length; i++) {
                    plaf.moveSourceRootUp(toMoveUp[i]);
                }
                this.srcRoots = plaf.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
    }
    
    /**
     * <code>ListModel</code> capable to manage NetBeans platform javadoc roots.
     * <p>Can be used in conjuction with {@link URLListRenderer}</p>
     */
    static final class NbPlatformJavadocRootsModel extends AbstractListModel {
        
        private NbPlatform plaf;
        private URL[] javadocRoots;
        
        NbPlatformJavadocRootsModel(NbPlatform plaf) {
            this.plaf = plaf;
            this.javadocRoots = plaf.getJavadocRoots();
        }
        
        public Object getElementAt(int index) {
            return javadocRoots[index];
        }
        
        public int getSize() {
            return javadocRoots.length;
        }
        
        void removeJavadocRoots(URL[] jdRootToRemove) {
            try {
                plaf.removeJavadocRoots(jdRootToRemove);
                this.javadocRoots = plaf.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void addJavadocRoot(URL jdRootToAdd) {
            try {
                plaf.addJavadocRoot(jdRootToAdd);
                this.javadocRoots = plaf.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveJavadocRootsDown(int[] toMoveDown) {
            try {
                for (int i = 0; i < toMoveDown.length; i++) {
                    plaf.moveJavadocRootDown(toMoveDown[i]);
                }
                this.javadocRoots = plaf.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveJavadocRootsUp(int[] toMoveUp) {
            try {
                for (int i = 0; i < toMoveUp.length; i++) {
                    plaf.moveJavadocRootUp(toMoveUp[i]);
                }
                this.javadocRoots = plaf.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
    }
    
    /**
     * Render {@link java.net.URL} using {@link java.net.URL#getFile}.
     * <p>Use in conjuction with {@link NbPlatformSourceRootsModel} and
     * {@link NbPlatformJavadocRootsModel}</p>
     */
    static final class URLListRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            URL u = (URL) value;
            String text = u.toExternalForm();
            if (u.getProtocol().equals("file")) { // NOI18N
                text = new File(URI.create(u.toExternalForm())).getAbsolutePath();
            } else if (u.getProtocol().equals("jar")) { // NOI18N
                URL baseU = FileUtil.getArchiveFile(u);
                if (u.equals(FileUtil.getArchiveRoot(baseU)) && baseU.getProtocol().equals("file")) { // NOI18N
                    text = new File(URI.create(baseU.toExternalForm())).getAbsolutePath();
                }
            }
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
    
}
