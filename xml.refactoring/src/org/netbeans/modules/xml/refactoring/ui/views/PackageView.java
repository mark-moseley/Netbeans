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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.xml.refactoring.ui.views;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;


/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
        
    private PackageView() {}
    
    
    
    /**
     * Create a renderer suited to rendering models created using {@link #createListView}.
     * The exact nature of the display is not specified.
     * Instances of String can also be rendered.
     * @return a suitable package renderer
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    public static ListCellRenderer listRenderer() {
        return new PackageListCellRenderer();
    }
    
    /**
     * Create a list or combo box model suitable for {@link javax.swing.JList} from a source group
     * showing all Java packages in the source group.
     * To display it you will also need {@link #listRenderer}.
     * <p>No particular guarantees are made as to the nature of the model objects themselves,
     * except that {@link Object#toString} will give the fully-qualified package name
     * (or <code>""</code> for the default package), regardless of what the renderer
     * actually displays.</p>
     * @param group a Java-like source group
     * @return a model of its packages
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    
    public static ComboBoxModel createListView(SourceGroup group) {        
        TreeSet data = new TreeSet();        
        findNonExcludedPackages( data, group.getRootFolder(), group );
        return new DefaultComboBoxModel( new Vector( data) );        
    }
    
    private static void findNonExcludedPackages( Collection target, FileObject fo, SourceGroup group ) {
        
        assert fo.isFolder() : "Package view only accepts folders"; // NOI18N
               
        if ( !VisibilityQuery.getDefault().isVisible( fo ) ) {
            return; // Don't show hidden packages
        }
        
        FileObject[] kids = fo.getChildren();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (int i = 0; i < kids.length; i++) {            
            // XXX could use PackageDisplayUtils.isSignificant here
            if ( VisibilityQuery.getDefault().isVisible( kids[i] ) ) {
                if (kids[i].isFolder() ) {
                    findNonExcludedPackages( target, kids[i], group );
                    hasSubfolders = true;
                } 
                else {
                    hasFiles = true;
                }
            }
        }
        if (hasFiles || !hasSubfolders) {
            if ( group != null ) {
                target.add( new PackageItem(group, fo, !hasFiles ) );
            }
            
        }
    }
    
    /**
     * Model item representing one package.
     */
    static final class PackageItem implements Comparable {
        
        private static IdentityHashMap/*<Image,Icon>*/ image2icon = new IdentityHashMap();
        
        private final boolean empty;
        private final FileObject pkg;
        private final String pkgname;
        private Icon icon;
        
        public PackageItem(SourceGroup group, FileObject pkg, boolean empty) {
            this.pkg = pkg;
            this.empty = empty;
            String path = FileUtil.getRelativePath(group.getRootFolder(), pkg);
            assert path != null : "No " + pkg + " in " + group;
            pkgname = path.replace('/', '.');
            
        }
        
        public String toString() {
            return pkgname;
        }
        
        public String getLabel() {
            //return PackageDisplayUtils.getDisplayLabel(pkgname);
            return pkgname;
        }
        
        public Icon getIcon() {
            try {
                DataObject dobj =DataObject.find(pkg);
                Node node = dobj.getNodeDelegate();
                return new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
                 
            } catch (DataObjectNotFoundException ex) {
                
            }
            return null;
        }

        public int compareTo(Object obj) {
            return pkgname.compareTo(((PackageItem) obj).pkgname);
        }
        
    }
    
    /**
     * The renderer which just displays {@link PackageItem#getLabel} and {@link PackageItem#getIcon}.
     */
    private static final class PackageListCellRenderer extends DefaultListCellRenderer {
        
        public PackageListCellRenderer() {}

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof PackageItem) {
                PackageItem pkgitem = (PackageItem) value;
                super.getListCellRendererComponent(list, pkgitem.getLabel(), index, isSelected, cellHasFocus);
                //setIcon(pkgitem.getIcon());
            } else {
                // #49954: render a specially inserted package somehow.
                String pkgitem = (String) value;
                super.getListCellRendererComponent(list, pkgitem, index, isSelected, cellHasFocus);
            }
            return this;
        }
        
    }
    
    
}
