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

package org.netbeans.modules.beans.beaninfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * PropertyEditor for Icons. Depends on existing DataObject for images.
 * Images must be represented by some DataObject which returns itselv
 * as cookie, and has image file as a primary file. File extensions
 * for images is specified in isImage method.
 *
 * @author Jan Jancura
 */
final class BiIconEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    private static final String BEAN_ICONEDITOR_HELP = "beans.icon"; // NOI18N
    
    private FileObject sourceFileObject;
    private PropertyEnv env;
    
    /** Standard variable for localization. */
    static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(
    BiIconEditor.class);
    
    public static boolean isImage(String s) {
        s = s.toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".gif") || // NOI18N
        s.endsWith(".jpeg") || s.endsWith(".jpe") || // NOI18N
        s.equals("jpg") || s.equals("gif") || // NOI18N
        s.equals("jpeg") || s.equals("jpe"); // NOI18N
    }
    
    // variables .................................................................................
    
    //private Icon icon;
    
    // init .......................................................................................
    
    public BiIconEditor( FileObject sourceFileObject ) {
        this.sourceFileObject = sourceFileObject;
    }
    
    // Special access methods......................................................................
    
    
    /** @return the name of image's source - depending on the type it can be a URL, file name or
     * resource path to the image on classpath */
    public String getSourceName() {
        if (getValue() instanceof BiImageIcon)
            return getValue().getName();
        else
            return null;
    }

    @Override
    public void setValue(Object value) {
        BiImageIcon old = getValue();
        if (old == value || old != null && old.equals(value)) {
            return;
        }
        super.setValue(value);
    }

    @Override
    public BiImageIcon getValue() {
        return (BiImageIcon) super.getValue();
    }
    
    /**
     * @return The property value as a human editable string.
     * <p>   Returns null if the value can't be expressed as an editable string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *       be prepared to parse that string back in setAsText().
     */
    @Override
    public String getAsText() {
        Object val = getValue();        
        return textFromIcon((BiImageIcon) val);
    }
    
    /**
     * Set the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     * @param text  The string to be parsed.
     */
    @Override
    public void setAsText(String string) throws IllegalArgumentException {
        try { 
            setValue(iconFromText(string));
        }
        catch ( IllegalArgumentException e ) {
            // User inserted incorrect path either report or
            // do nothing
            // For now choosing doing nothing
        }
    }
    
    String textFromIcon(BiImageIcon icon) {
        return icon == null
                ? "null" // NOI18N
                : icon.getName();
    }
    
    BiImageIcon iconFromText(String string) throws IllegalArgumentException {
        BiImageIcon ii;
        try {
            if (string.length() == 0 || string.equals("null")) { // NOI18N
                ii = null;
            }
            else {
                ClassPath cp = ClassPath.getClassPath( sourceFileObject, ClassPath.SOURCE );                
                
                URL url = cp.findResource( string.substring(1) ).getURL();
                ii = new BiImageIcon(url, string);
            }
        } catch (Throwable e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
            throw new IllegalArgumentException(e);
        }
        return ii;
    }
    
    /**
     * @return  True if the class will honor the paintValue method.
     */
    @Override
    public boolean isPaintable() {
        return false;
    }
    
    /**
     * @return  True if the propertyEditor can provide a custom editor.
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /**
     * A PropertyEditor may choose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *      not supported.
     */
    @Override
    public java.awt.Component getCustomEditor() {
        return new IconPanel(this, env);
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
    
    public static final class BiImageIcon {
        private String name;
        private URL url;
        private Icon icon;
        
        public BiImageIcon() {
        }
        
        BiImageIcon(URL url, String name) {
            this.url = url;
            this.name = name;
        }
        
        String getName() {
            return name;            
        }

        public Icon getIcon() {
            if (icon == null) {
                try {
                    Image image = ImageIO.read(url);
                    icon = new ImageIcon(image);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return icon;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BiImageIcon other = (BiImageIcon) obj;
            if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            return true;
        }
        
    }
    
    private static final class IconPanel extends JPanel implements VetoableChangeListener {
        JRadioButton rbClasspath, rbNoPicture;
        JTextField tfName;
        JButton bSelect;
        JScrollPane spImage;
        private final PropertyEnv env;
        private BiImageIcon value;
        private BiIconEditor editor;
        
        IconPanel(BiIconEditor editor, PropertyEnv env) {
            this.env = env;
            this.editor = editor;
            
            // visual components .............................................
            
            JLabel lab;
            setLayout(new BorderLayout(6, 6));
            setBorder(new EmptyBorder(6, 6, 6, 6));
            getAccessibleContext().setAccessibleName(bundle.getString("ACS_IconPanelA11yName"));  // NOI18N
            getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_IconPanelA11yDesc"));  // NOI18N
            JPanel p = new JPanel(new BorderLayout(3, 3));
            JPanel p1 = new JPanel(new BorderLayout());
            p1.setBorder(new TitledBorder(new EtchedBorder(), bundle.getString("CTL_ImageSourceType")));
            JPanel p2 = new JPanel();
            p2.setBorder(new EmptyBorder(0, 3, 0, 3));
            GridBagLayout l = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            p2.setLayout(l);
            c.anchor = GridBagConstraints.WEST;
            
            p2.add(rbClasspath = new JRadioButton(bundle.getString("CTL_Classpath")));
            rbClasspath.setToolTipText(bundle.getString("ACS_ClasspathA11yDesc"));
            rbClasspath.setMnemonic(bundle.getString("CTL_Classpath_Mnemonic").charAt(0));
            c.gridwidth = 1;
            l.setConstraints(rbClasspath, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_ClasspathExample")));
            lab.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ClasspathExampleA11yDesc"));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            p2.add(rbNoPicture = new JRadioButton(bundle.getString("CTL_NoPicture")));
            rbNoPicture.setToolTipText(bundle.getString("ACS_NoPictureA11yDesc"));
            rbNoPicture.setMnemonic(bundle.getString("CTL_NoPicture_Mnemonic").charAt(0));
            c.gridwidth = 1;
            l.setConstraints(rbNoPicture, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_Null")));
            lab.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NullA11yDesc"));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbClasspath);
            bg.add(rbNoPicture);
            rbClasspath.setSelected(true);
            p1.add(p2, "West"); // NOI18N
            p.add(p1, "North"); // NOI18N
            p1 = new JPanel(new BorderLayout(6, 6));
            JLabel nameLabel = new JLabel(bundle.getString("CTL_ImageSourceName"));
            nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ImageSourceNameA11yDesc"));
            nameLabel.setDisplayedMnemonic(bundle.getString("CTL_ImageSourceName_Mnemonic").charAt(0));
            p1.add(nameLabel, "West"); // NOI18N
            p1.add(tfName = new JTextField(), "Center"); // NOI18N
            nameLabel.setLabelFor(tfName);
            tfName.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ImageSourceNameTextFieldA11yName"));
            tfName.setToolTipText(bundle.getString("ACS_ImageSourceNameTextFieldA11yDesc"));
            p1.add(bSelect = new JButton("..."), "East"); // NOI18N
            bSelect.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ImageSourceNameBrowseButtonA11yName"));
            bSelect.setToolTipText(bundle.getString("ACS_ImageSourceNameBrowseButtonA11yDesc"));
            bSelect.setEnabled(false);
            p.add(p1, "South"); // NOI18N
            add(p, "North"); // NOI18N
            spImage = new JScrollPane() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(60, 60);
                }
            };
            add(spImage, "Center"); // NOI18N
            
            // listeners .................................................
            
            tfName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setValue();
                }
            });
            rbClasspath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bSelect.setEnabled(true);
                    tfName.setEnabled(true);
                    setValue();
                }
            });
            rbNoPicture.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bSelect.setEnabled(false);
                    tfName.setEnabled(false);
                    
                    setValue(null);
                    updateIcon();
                }
            });
            bSelect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (rbClasspath.isSelected()) {
                        String name = selectResource();
                        if (name != null) {
                            tfName.setText("/" + name); // NOI18N
                            setValue();
                        }
                    }
                }
            });
            // initialization ......................................
 
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(this);
            setValue(editor.getValue());
            updateIcon();
            
            HelpCtx.setHelpIDString(this, BEAN_ICONEDITOR_HELP); 
            
            BiImageIcon i = getValue();
            if (i == null) {
                rbNoPicture.setSelected(true);
                bSelect.setEnabled(false);
                tfName.setEnabled(false);
                return;
            }
            
            rbClasspath.setSelected(true);
            bSelect.setEnabled(true);
            tfName.setText((i).getName());
        }
        
        void updateIcon() {
            BiImageIcon bii = getValue();
            Icon i = bii == null? null: bii.getIcon();
            spImage.setViewportView((i == null) ? new JLabel() : new JLabel(i));
            //      repaint();
            validate();
        }
        
        void setValue() {
            String val = tfName.getText();
            val.trim();
            if ("".equals(val)) { // NOI18N
                setValue(null);
                return;
            }
            
            try {
                setValue(editor.iconFromText(val));
            } catch (IllegalArgumentException ee) {
                // Reporting the exception is maybe too much let's do nothing
                // instead 
                // org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ee);
            }
            updateIcon();
        }
        
        private void setValue(BiImageIcon icon) {
            this.value = icon;
        }
        
        private BiImageIcon getValue() {
            return this.value;
        }
        
        private Object getPropertyValue(PropertyChangeEvent evt) throws PropertyVetoException {
            BiImageIcon ii = null;
            String s = tfName.getText().trim();
            if (rbClasspath.isSelected() && s.length() != 0 ) {                    
                ClassPath cp = ClassPath.getClassPath( editor.sourceFileObject, ClassPath.SOURCE );
                FileObject f = cp.findResource( s.substring(1) );
                try{
                    ii = new BiImageIcon(f.getURL(), s);
                }
                catch(java.lang.Throwable t){
                    throw new PropertyVetoException(
                            NbBundle.getMessage(IconPanel.class, "CTL_Icon_not_exists", s), //NOI18N
                            evt);
                }
            }
            return ii;
        }

        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (PropertyEnv.PROP_STATE == evt.getPropertyName()) {
                BiImageIcon ii = (BiImageIcon) getPropertyValue(evt);
                editor.setValue(ii);
            }
        }
        
        private List<FileObject> getRoots(ClassPath cp) {
            List<FileObject> list = new ArrayList<FileObject>(cp.entries().size());
            for (ClassPath.Entry e : cp.entries()) {
                // try to map it to sources
                URL url = e.getURL();
                SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(url);
                FileObject [] fos = r.getRoots();
                if (fos.length > 0) {
                    for (int i = 0 ; i < fos.length; i++) list.add(fos[i]);
                } else {
                    if (e.getRoot()!=null)
                        list.add(e.getRoot()); // add the class-path location directly
                }
            }
            
            return list;
        }
        
        private String rootDisplayName(FileObject fo) {
            return FileUtil.getFileDisplayName(fo);
        }
        
        /**
         * Obtains icon resource from the user.
         *
         * @returns name of the selected resource or <code>null</code>.
         */
        private String selectResource() {
            ClassPath executionClassPath = ClassPath.getClassPath(editor.sourceFileObject, ClassPath.EXECUTE);
            List<FileObject> roots = (executionClassPath == null)
                    ? Collections.<FileObject>emptyList()
                    : getRoots(executionClassPath);
            Node nodes[] = new Node[roots.size()];
            int selRoot = -1;
            try {
                ListIterator<FileObject> iter = roots.listIterator();
                while (iter.hasNext()) {
                    FileObject root = iter.next();
                    DataObject dob = DataObject.find(root);
                    final String displayName = rootDisplayName(root);
                    nodes[iter.previousIndex()] = new RootNode(dob.getNodeDelegate(), displayName);
                }
            } catch (DataObjectNotFoundException donfex) {
                Exceptions.printStackTrace(donfex);
                return null;
            }
            Children children = new Children.Array();
            children.add(nodes);
            final AbstractNode root = new AbstractNode(children);
            root.setIconBaseWithExtension("org/netbeans/modules/beans/resources/iconResourceRoot.gif"); // NOI18N
            root.setDisplayName(bundle.getString("CTL_ClassPathName")); // NOI18N
                            
            ResourceSelector selector = new ResourceSelector(root);
            DialogDescriptor dd = new DialogDescriptor(selector, bundle.getString("CTL_OpenDialogName")); // NOI18N
            Object res = DialogDisplayer.getDefault().notify(dd);
            nodes = (res == DialogDescriptor.OK_OPTION) ? selector.getNodes() : null;
            String name = null;
            if ((nodes != null) && (nodes.length == 1)) {
                DataObject dob = nodes[0].getCookie(DataObject.class);
                if (dob != null) {
                    FileObject fob = dob.getPrimaryFile();
                    if (fob != null) {                        
                        if (executionClassPath.contains(fob)) {
                            name = executionClassPath.getResourceName(fob);
                        } else {
                            ClassPath srcClassPath = ClassPath.getClassPath(fob, ClassPath.SOURCE);
                            name = srcClassPath.getResourceName(fob);
                        }
                    }
                }
            }
            return name;
        }
        
    } // end of IconPanel
    
    private static final class RootNode extends FilterNode {
        RootNode(Node node, String displayName) {
            super(node);
            if (displayName != null) {
                disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
                setDisplayName(displayName);
            }
        }
    } // RootNode
    
    private static final class ResourceSelector extends JPanel implements ExplorerManager.Provider {
        /** Manages the tree. */
        private ExplorerManager manager = new ExplorerManager();
                
        public ResourceSelector(Node root) {
            setLayout(new BorderLayout(0, 5));
            setBorder(new EmptyBorder(12, 12, 0, 11));
            getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResourceSelector")); // NOI18N
            getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResourceSelector")); // NOI18N
            manager.setRootContext(root);
            
            BeanTreeView tree = new BeanTreeView();
            tree.setPopupAllowed(false);
            tree.setDefaultActionAllowed(false);
            // install proper border for tree
            tree.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
            tree.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResourceSelectorView")); // NOI18N
            tree.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResourceSelectorView")); // NOI18N
            add(tree, BorderLayout.CENTER);
        }
        
        /**
         * Gets preferred size. Overrides superclass method.
         * Height is adjusted to 1/2 screen.
         */
        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            dim.height = Math.max(dim.height, org.openide.util.Utilities.getUsableScreenBounds().height / 2);
            return dim;
        }
        
        /**
         * @return selected nodes
         */
        public Node[] getNodes() {
            return manager.getSelectedNodes();
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
    } // ResourceSelector
    
}
