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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Petr Hrebejk
 */
public class CategoryView extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {

    private ExplorerManager manager;
    private BeanTreeView btv;
    private CategoryModel categoryModel;

    public CategoryView( CategoryModel categoryModel ) {

        this.categoryModel = categoryModel;

        // See #36315
        manager = new ExplorerManager();

        setLayout( new BorderLayout() );

        Dimension size = new Dimension( 220, 4 );
        btv = new BeanTreeView();    // Add the BeanTreeView
        btv.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setPopupAllowed( false );
        btv.setRootVisible( false );
        btv.setDefaultActionAllowed( false );
        btv.setMinimumSize( size );
        btv.setPreferredSize( size );
        btv.setMaximumSize( size );
        btv.setDragSource (false);
        this.add( btv, BorderLayout.CENTER );
        manager.setRootContext( createRootNode( categoryModel ) );
        manager.addPropertyChangeListener( this );
        categoryModel.addPropertyChangeListener( this );
        btv.expandAll();
        selectNode( categoryModel.getCurrentCategory() );

        btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CategoryView.class,"AN_CatgoryView"));
        btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CategoryView.class,"AD_CategoryView"));

    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void addNotify() {
        super.addNotify();
        btv.expandAll();
        btv.requestFocusInWindow();
    }

    // Private methods -----------------------------------------------------

    private void selectNode( ProjectCustomizer.Category category ) {

        Node node = findNode( category, manager.getRootContext() );

        if ( node != null ) {
            try {
                manager.setSelectedNodes( new Node[] { node } );
            }
            catch ( PropertyVetoException e ) {
                // No node will be selected
            }
        }

    }

    private Node findNode( ProjectCustomizer.Category category, Node node ) {

        Children ch = node.getChildren();

        if ( ch != null && ch != Children.LEAF ) {
            Node nodes[] = ch.getNodes( true );

            if ( nodes != null ) {
                for (Node child : nodes) {
                    ProjectCustomizer.Category cc = child.getLookup().lookup(ProjectCustomizer.Category.class);

                    if ( cc == category ) {
                        return child;
                    }
                    else {
                        Node n = findNode(category, child);
                        if ( n != null ) {
                            return n;
                        }
                    }
                }
            }
        }

        return null;
    }


    private Node createRootNode( CategoryModel categoryModel ) {
        ProjectCustomizer.Category rootCategory = ProjectCustomizer.Category.create( "root", "root", null, categoryModel.getCategories() ); // NOI18N
        return new CategoryNode( rootCategory );
    }

    // Implementation of property change listener --------------------------

    public void propertyChange(PropertyChangeEvent evt) {

        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();

        if ( source== manager && ExplorerManager.PROP_SELECTED_NODES.equals( propertyName ) ) {
            Node nodes[] = manager.getSelectedNodes();
            if ( nodes == null || nodes.length <= 0 ) {
                return;
            }
            Node node = nodes[0];

            ProjectCustomizer.Category category = node.getLookup().lookup(ProjectCustomizer.Category.class);
            if ( category != categoryModel.getCurrentCategory() ) {
                categoryModel.setCurrentCategory( category );
            }
        }

        if ( source == categoryModel && CategoryModel.PROP_CURRENT_CATEGORY.equals( propertyName ) ) {
            selectNode( (ProjectCustomizer.Category)evt.getNewValue() );
        }

    }


    // Private Inner classes -----------------------------------------------

    /** Node to be used for configuration
     */
    private static class CategoryNode extends AbstractNode implements PropertyChangeListener {

        private Image icon = org.openide.util.Utilities.loadImage( "org/netbeans/modules/project/uiapi/defaultCategory.gif" ); // NOI18N

        private ProjectCustomizer.Category category;

        public CategoryNode( ProjectCustomizer.Category category ) {
            super( ( category.getSubcategories() == null || category.getSubcategories().length == 0 ) ?
                        Children.LEAF : new CategoryChildren( category.getSubcategories() ),
                   Lookups.fixed(category));
            setName( category.getName() );
            this.category = category;
            setDisplayName( category.getDisplayName() );

            if ( category.getIcon() != null ) {
                this.icon = category.getIcon();
            }
            Utilities.getCategoryChangeSupport(category).addPropertyChangeListener(this);
        }

        public String getHtmlDisplayName() {
            return category.isValid() ? null :
                "<html><font color=\"!nb.errorForeground\">" + // NOI18N
                    category.getDisplayName() + "</font></html>"; // NOI18N
        }

        public Image getIcon( int type ) {
            return this.icon;
        }

        public Image getOpenedIcon( int type ) {
            return getIcon( type );
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == CategoryChangeSupport.VALID_PROPERTY) {
                fireDisplayNameChange(null, null);
            }
        }

    }

    /** Children used for configuration
     */
    private static class CategoryChildren extends Children.Keys<ProjectCustomizer.Category> {

        private ProjectCustomizer.Category[] descriptions;

        public CategoryChildren( ProjectCustomizer.Category[] descriptions ) {
            this.descriptions = descriptions;
        }

        // Children.Keys impl --------------------------------------------------

        public void addNotify() {
            setKeys( descriptions );
        }

        public void removeNotify() {
            setKeys(new ProjectCustomizer.Category[0]);
        }

        protected Node[] createNodes(ProjectCustomizer.Category c) {
            return new Node[] {new CategoryNode(c)};
        }
    }

}


