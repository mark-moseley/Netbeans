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


package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteFilter;

import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * The root node representing the Component Palette content.
 *
 * @author S. Aubrecht
 */
public final class RootNode extends FilterNode {
    
    static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private Action[] actions;
    

    // --------

    public RootNode( Node originalRoot, Lookup lkp ) {
        this( originalRoot, new InstanceContent(), lkp );
    }

    private RootNode( Node originalRoot, InstanceContent content, Lookup lkp ) {
        super( originalRoot, 
                new Children( originalRoot, lkp ),
                new ProxyLookup( new Lookup[] { lkp, new AbstractLookup( content ), originalRoot.getLookup() } ) );
        DataFolder df = (DataFolder)getOriginal().getCookie( DataFolder.class );
        if( null != df ) {
            content.add( new DataFolder.Index( df, this ) );
        }
        content.add( this );
        setDisplayName(Utils.getBundleString("CTL_Component_palette")); // NOI18N
    }
    
    // --------

    public NewType[] getNewTypes() {
        NewType[] res = super.getNewTypes();
        if( null == res || res.length == 0 )
            res = new NewType[] { new NewCategory() };
        return res;
    }

    public Action[] getActions(boolean context) {
        if (actions == null) {
            actions = new Action[] {
                new Utils.NewCategoryAction( this ),
                null,
                new Utils.SortCategoriesAction( this ),
                null,
                new Utils.RefreshPaletteAction()
            };
        }
        PaletteActions customActions = (PaletteActions)getLookup().lookup( PaletteActions.class );
        if( null != customActions ) {
            return Utils.mergeActions( actions, customActions.getCustomPaletteActions() );
        }
        return actions;
    }

    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

//    public HelpCtx getHelpCtx() {
//        //TODO revisit this
//        return new HelpCtx("gui.options.component-palette"); // NOI18N
//    }

    public PasteType getDropType(Transferable t, int action, int index) {
        //no drop is accepted in palette's root node
        return null;
    }


    public void refreshChildren() {
        ((Children)getChildren()).refreshNodes();
    }

    // ------------

    void createNewCategory() throws java.io.IOException {
        java.util.ResourceBundle bundle = Utils.getBundle();
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
            bundle.getString("CTL_NewCategoryName"), // NOI18N
            bundle.getString("CTL_NewCategoryTitle")); // NOI18N
        input.setInputText(bundle.getString("CTL_NewCategoryValue")); // NOI18N

        while (DialogDisplayer.getDefault().notify(input)
                                              == NotifyDescriptor.OK_OPTION)
        {
            String categoryName = input.getInputText();
            if( CategoryNode.checkCategoryName( this, categoryName, null ) ) {
                DataFolder paletteFolder = (DataFolder)getCookie( DataFolder.class );
                FileObject parentFolder = paletteFolder.getPrimaryFile();
                String folderName = CategoryNode.convertCategoryToFolderName( parentFolder, categoryName, null );
                FileObject folder = parentFolder.createFolder(folderName);
                if (!folderName.equals(categoryName))
                    folder.setAttribute( CategoryNode.CAT_NAME, categoryName );
                break;
            }
        }
    }

    public boolean canCut() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    // --------------

    /** Children for the PaletteNode. Creates PaletteCategoryNode instances
     * as filter subnodes. */
    private static class Children extends FilterNode.Children {

        private PaletteFilter filter;
        private Lookup lkp;
        
        public Children(Node original, Lookup lkp) {
            super(original);
            this.lkp = lkp;
            filter = (PaletteFilter)lkp.lookup( PaletteFilter.class );
        }

        protected Node copyNode(Node node) {
            return new CategoryNode( node, lkp );
        }
        
        protected Node[] createNodes(Object key) {
            Node n = (Node) key;
            
            if( null == filter || filter.isValidCategory( n.getLookup() ) ) {
                return new Node[] { copyNode(n) };
            }

            return null;
        }
        
        public void refreshNodes() {
            Node[] nodes = original.getChildren().getNodes();
            setKeys( Collections.EMPTY_LIST );
            setKeys( nodes );
        }
    }

    // -------


    // -------
    /**
     * New type for creation of new palette category.
     */
    final class NewCategory extends NewType {

        public String getName() {
            return Utils.getBundleString("CTL_NewCategory"); // NOI18N
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(NewCategory.class);
        }

        public void create() throws java.io.IOException {
            RootNode.this.createNewCategory();
        }
    }
}
