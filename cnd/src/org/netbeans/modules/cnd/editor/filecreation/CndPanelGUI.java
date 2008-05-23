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

package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public abstract class CndPanelGUI  extends javax.swing.JPanel implements DocumentListener {

    protected Project project;
    protected SourceGroup[] folders;

    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (500, 340);
    protected static final String NEW_FILE_PREFIX = getMessage("LBL_NewCndFileChooserPanelGUI_NewFilePrefix"); // NOI18N
    protected final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();
        
    public CndPanelGUI(Project project, SourceGroup[] folders) {
        this.project = project;
        this.folders = folders;
    }

    protected final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    public abstract SourceGroup getTargetGroup();
    public abstract String getTargetFolder();
    public abstract String getTargetName();
    protected abstract void updateCreatedFile();
    public abstract void initValues( FileObject template, FileObject preselectedFolder, String documentName );

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }    
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    protected static SourceGroup getPreselectedGroup( SourceGroup[] groups, FileObject folder ) {        
        for( int i = 0; folder != null && i < groups.length; i++ ) {
            if( FileUtil.isParentOf( groups[i].getRootFolder(), folder )
                || groups[i].getRootFolder().equals(folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    protected static String getRelativeNativeName( FileObject root, FileObject folder ) {
        if (root == null) {
            throw new NullPointerException("null root passed to getRelativeNativeName"); // NOI18N
        }
        
        String path;
        
        if (folder == null) {
            path = ""; // NOI18N
        }
        else {
            path = FileUtil.getRelativePath( root, folder );            
        }
        
        return path == null ? "" : path.replace( '/', File.separatorChar ); // NOI18N
    }
    
    protected static String getMessage(String name) {
        return NbBundle.getMessage( CndPanelGUI.class, name);
    }

    protected class GroupCellRenderer extends JLabel implements ListCellRenderer {
    
        public GroupCellRenderer() {
            setOpaque( true );
        }
        
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup)value;
                String projectDisplayName = ProjectUtils.getInformation( project ).getDisplayName();
                String groupDisplayName = group.getDisplayName();
                if ( projectDisplayName.equals( groupDisplayName ) ) {
                    setText( groupDisplayName );
                }
                else {
                    setText( MessageFormat.format( PhysicalView.GroupNode.GROUP_NAME_PATTERN,
                        new Object[] { groupDisplayName, projectDisplayName, group.getRootFolder().getName() } ) );
                }
                
                setIcon( group.getIcon( false ) );
            } 
            else {
                setText( value.toString () );
                setIcon( null );
            }
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
             
            }
            return this;        
        }
                
    }
}
