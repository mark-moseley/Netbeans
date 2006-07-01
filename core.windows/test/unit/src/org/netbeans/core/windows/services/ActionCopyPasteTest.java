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

package org.netbeans.core.windows.services;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.datatransfer.PasteType;

/**
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class ActionCopyPasteTest extends NbTestCase {

    ToolbarFolderNode toolbar1;
    ToolbarFolderNode toolbar2;
    MenuFolderNode menu1;
    MenuFolderNode menu2;
    DataObject actionToPaste;
    
    public ActionCopyPasteTest(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        toolbar1 = new ToolbarFolderNode( createFolder( "Toolbars", "tb1" ) );
        toolbar2 = new ToolbarFolderNode( createFolder( "Toolbars", "tb2" ) );

        menu1 = new MenuFolderNode( createFolder( "Menu", "menu1" ) );
        menu2 = new MenuFolderNode( createFolder( "Menu", "menu2" ) );
        
        createChildren( toolbar1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );
        createChildren( menu1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );

        createChildren( toolbar2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        createChildren( menu2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        
    }

    protected boolean runInEQ () {
        return true;
    }
    
    public void testDoNotPasteDuplicateActions() throws Exception {
        //check copy & paste for toolbar folders
        DataObject[] folderChildren = ((DataFolder)toolbar1.getDataObject()).getChildren();
        DataObject child1 = folderChildren[0];
        Transferable t = child1.getNodeDelegate().clipboardCopy();
        PasteType[] types;
        
        types = toolbar1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, types.length );
        
        types = toolbar2.getPasteTypes( t );
        assertTrue( "Pasting to a different folder is ok.", types.length > 0 );
        
        types = menu1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, types.length );
        
        types = menu2.getPasteTypes( t );
        assertTrue( "Pasting to a different menu is ok.", types.length > 0 );

        //check copy & paste for menu folders
        folderChildren = ((DataFolder)menu1.getDataObject()).getChildren();
        child1 = folderChildren[0];
        t = child1.getNodeDelegate().clipboardCopy();

        types = toolbar1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, types.length );
        
        types = toolbar2.getPasteTypes( t );
        assertTrue( "Pasting to a different folder is ok.", types.length > 0 );
        
        types = menu1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, types.length );
        
        types = menu2.getPasteTypes( t );
        assertTrue( "Pasting to a different menu is ok.", types.length > 0 );
    }
    
    DataFolder createFolder( String parent, String folderName ) throws Exception {
        FileObject folderObj = Repository.getDefault().getDefaultFileSystem().findResource( parent+"/"+folderName );
        if( null != folderObj )
            folderObj.delete();

        FileObject parentFolder = Repository.getDefault().getDefaultFileSystem().findResource( parent );
        assertNotNull( parentFolder );
        parentFolder.createFolder( folderName );
        
        DataFolder res = DataFolder.findFolder( Repository.getDefault().getDefaultFileSystem().findResource( parent+"/"+folderName ) );
        assertNotNull( res );
        return res;
    }
    
    void createChildren( FileObject folder, Class[] actions ) throws Exception {
        for( int i=0; i<actions.length; i++ ) {
            folder.createData( actions[i].getName()+".instance" );
        }
    }
    
    public static class ActionA1 extends AbstractAction {
        public ActionA1() {
            super( "actiona1" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }
    
    public static class ActionA2 extends AbstractAction {
        public ActionA2() {
            super( "actiona2" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }

    public static class ActionB1 extends AbstractAction {
        public ActionB1() {
            super( "actionb1" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }
    
    public static class ActionB2 extends AbstractAction {
        public ActionB2() {
            super( "actionb2" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }
}
