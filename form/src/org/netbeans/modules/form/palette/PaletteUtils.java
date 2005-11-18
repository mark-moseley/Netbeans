/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.text.MessageFormat;
import java.io.File;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.spi.palette.PaletteFilter;

/**
 * Class providing various useful methods for palette classes.
 *
 * @author Tomas Pavek, Jan Stola
 */

public final class PaletteUtils {

    private static FileObject paletteFolder;
    private static DataFolder paletteDataFolder;
    private static PaletteController palette;

    private PaletteUtils() {
    }

    static String getItemComponentDescription(PaletteItem item) {
        ClassSource classSource = item.getComponentClassSource();

        if (classSource == null || classSource.getCPRootCount() == 0) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKComponent"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansComponent"); // NOI18N
            }
        }
        else {
            String type = classSource.getCPRootType(0);
            String name = classSource.getCPRootName(0);

            if (ClassSource.JAR_SOURCE.equals(type)) {
                return MessageFormat.format(
                    getBundleString("FMT_ComponentFromJar"), // NOI18N
                    new Object[] { name });
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                return MessageFormat.format(
                    getBundleString("FMT_ComponentFromLibrary"), // NOI18N
                    new Object[] { lib != null ? lib.getDisplayName() : name });
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                try {
                    Project project = FileOwnerQuery.getOwner(new File(name).toURI());
                    return MessageFormat.format(
                          getBundleString("FMT_ComponentFromProject"), // NOI18N
                          new Object[] { project == null ? name :
                                         FileUtil.getFileDisplayName(project.getProjectDirectory()) });
                } catch (Exception ex) {
                    // XXX must catch specific exceptions and notify them or explain why they are ignored!
                }
            }
        }

        return getBundleString("MSG_UnspecifiedComponent"); // NOI18N
    }

    public static FileObject getPaletteFolder() {
        if (paletteFolder != null)
            return paletteFolder;

        try {
            paletteFolder = Repository.getDefault().getDefaultFileSystem()
                                                     .findResource("FormDesignerPalette"); // NOI18N
            if (paletteFolder == null) // not found, create new folder
                paletteFolder = Repository.getDefault().getDefaultFileSystem()
                                  .getRoot().createFolder("FormDesignerPalette"); // NOI18N
        }
        catch (java.io.IOException ex) {
            throw new IllegalStateException("Palette folder not found and cannot be created."); // NOI18N
        }
        return paletteFolder;
    }
    
    static Node getPaletteNode() {
        return getPaletteDataFolder().getNodeDelegate();
    }
    
    public static PaletteController getPalette() {
        if( null == palette ) {
            try {
                PaletteFilter filter = new PaletteFilter(){
                    public boolean isValidCategory( Lookup lkp ){                    
                        FilterNode node = (FilterNode) lkp.lookup( Node.class ); 
			return representsValidPaletteCategory(node);
                    }
                    public boolean isValidItem( Lookup lkp ){                        
                        return true;
                    }    
                };      
                
                palette = PaletteFactory.createPalette( "FormDesignerPalette", new FormPaletteActions(), filter, null); // NOI18N
            } catch( IOException ioE ) {
                ioE.printStackTrace();
                //TODO error handling
                return null;
            }
        }
        return palette;
    }

    static DataFolder getPaletteDataFolder() {
        if (paletteDataFolder == null)
            paletteDataFolder = DataFolder.findFolder(getPaletteFolder());
        return paletteDataFolder;
    }
    
    public static void clearPaletteSelection() {
        getPalette().clearSelection();
    }
    
    public static PaletteItem getSelectedItem() {
        Lookup lkp = getPalette().getSelectedItem();
        
        return (PaletteItem)lkp.lookup( PaletteItem.class );
    }
    
    public static void selectItem( PaletteItem item ) {
        if( null == item ) {
            getPalette().clearSelection();
        } else {
            // This is not the node returned by getPaletteNode()!
            Node paletteNode = (Node)getPalette().getRoot().lookup(Node.class);
            Node[] categories = getCategoryNodes(paletteNode, false, true);
            for( int i=0; i<categories.length; i++ ) {
                Node[] items = getItemNodes( categories[i], true );
                for( int j=0; j<items.length; j++ ) {
                    PaletteItem formItem = (PaletteItem)items[j].getLookup().lookup( PaletteItem.class );
                    if( item.equals( formItem ) ) {
                        getPalette().setSelectedItem( categories[i].getLookup(), items[j].getLookup() );
                    }
                }
            }
        }
    }
    
    public static PaletteItem[] getAllItems() {
        HashSet uniqueItems = null;
        Node[] categories = getCategoryNodes( getPaletteNode(), false, false );
        for( int i=0; i<categories.length; i++ ) {
            Node[] items = getItemNodes( categories[i], true );
            for( int j=0; j<items.length; j++ ) {
                PaletteItem formItem = (PaletteItem)items[j].getLookup().lookup( PaletteItem.class );
                if( null != formItem ) {
                    if( null == uniqueItems ) {
                        uniqueItems = new HashSet();
                    }
                    uniqueItems.add( formItem );
                }
            }
        }
        PaletteItem[] res;
        if( null != uniqueItems ) {
            res = (PaletteItem[]) uniqueItems.toArray( new PaletteItem[uniqueItems.size()] );
        } else {
            res = new PaletteItem[0];
        }
        return res;
    }

    static String getBundleString(String key) {
        return NbBundle.getBundle(PaletteUtils.class).getString(key);
    }
    
    /**
     * Get an array of Node for the given category.
     *
     * @param categoryNode Category node.
     * @param mustBeValid True if all the nodes returned must be valid palette items.
     * @return An array of Nodes for the given category.
     */
    public static Node[] getItemNodes( Node categoryNode, boolean mustBeValid ) {
        Node[] nodes = categoryNode.getChildren().getNodes( true );
        //TODO add a check for palette item validity as needed
        return nodes;
    }

    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     * categories with Hidden flag.
     * @return An array of categories in the given palette.
     */
    public static Node[] getCategoryNodes(Node paletteNode, boolean mustBeVisible) {
        return getCategoryNodes(paletteNode, mustBeVisible, true);
    }
    
    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     * categories with Hidden flag.
     * @param mustBePaletteCategory True to return only categories not tagged as 'isNoPaletteCategory'
     * @return An array of categories in the given palette.
     */
    private static Node[] getCategoryNodes(Node paletteNode, boolean mustBeVisible, boolean mustBePaletteCategory) {
        Node[] nodes = paletteNode.getChildren().getNodes(true);        
	java.util.List list = null; // don't create until needed
	for( int i=0; i<nodes.length; i++ ) {
	    if(    ( mustBeVisible && isVisibleCategoryNode( nodes[i], mustBeVisible ))
	       ||  (!mustBeVisible && (!mustBePaletteCategory || representsValidPaletteCategory(nodes[i]))) ) 
	    {
		if( list != null ) {
		    list.add(nodes[i]);
		}
	    } else if( list == null ) {
		list = new ArrayList( nodes.length );
		for( int j=0; j < i; j++ ) {
		    list.add(nodes[j]);
		}
	    } 
	}
	if( list != null ) {
	    nodes = new Node[list.size()];
	    list.toArray(nodes);
	}
        return nodes;
    }

    /**
     * @return True if the given node is a DataFolder and does not have Hidden flag set.
     */
    static boolean isVisibleCategoryNode( Node node, boolean visible) {
        DataFolder df = (DataFolder) node.getCookie(DataFolder.class);
        return (df != null) && (!visible || representsVisibleCategory(node));
    }

    private static boolean representsVisibleCategory(Node node) {
        Object value = node.getValue("psa_" + PaletteController.ATTR_IS_VISIBLE); // NOI18N
        DataFolder df = (DataFolder) node.getCookie(DataFolder.class);
        if (df == null) return false;
        if (null == value || "null".equals(value)) { // NOI18N
            value = df.getPrimaryFile().getAttribute(PaletteController.ATTR_IS_VISIBLE);
        }
        if (value == null) {
            value = Boolean.TRUE;
        }
        return Boolean.valueOf(value.toString()).booleanValue() && representsValidPaletteCategory(node);
    }

    private static boolean representsValidPaletteCategory(Node node) {        
        DataFolder df = (DataFolder) node.getCookie(DataFolder.class);	
        return (df != null) && !Boolean.TRUE.equals(df.getPrimaryFile().getAttribute("isNoPaletteCategory")); // NOI18N
    }
    
}
