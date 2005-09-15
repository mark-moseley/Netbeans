/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.palette;

import java.io.FileNotFoundException;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;

/**
 *
 * @author Stanislav Aubrecht
 */
public class PaletteFactoryTest extends TestCase {
    
    private FileObject paletteRootFolder;
    private static final String PALETTE_ROOT_FOLDER_NAME = "test_palette_folder";
    
    public PaletteFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        paletteRootFolder = fs.findResource( PALETTE_ROOT_FOLDER_NAME );
        if( null != paletteRootFolder )
            paletteRootFolder.delete();
        paletteRootFolder = fs.getRoot().createFolder( PALETTE_ROOT_FOLDER_NAME );
    }

    protected void tearDown() throws Exception {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        paletteRootFolder = fs.findResource( PALETTE_ROOT_FOLDER_NAME );
        if( null != paletteRootFolder )
            paletteRootFolder.delete();
    }

    /**
     * Test of createPalette method, of class org.netbeans.modules.palette.api.PaletteFactory.
     */
    public void testCreatePaletteNulls() throws Exception {
        try {
            PaletteFactory.createPalette( (String)null, new DummyActions() );
            fail( "Palette factory accepts null as folder name." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
        
        try {
            PaletteFactory.createPalette( (Node)null, new DummyActions() );
            fail( "Palette factory accepts null as root Node." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
        
        try {
            PaletteFactory.createPalette( DummyPalette.createPaletteRoot(), null );
            fail( "Palette factory accepts null for palette actions." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
    }
    
    public void testCreatePaletteFolder() throws Exception {
        try {
            PaletteFactory.createPalette( "non_existent_folder", new DummyActions() );
            fail( "Palette factory accepts non-existent folders." );
        } catch( FileNotFoundException fnfE ) {
            //that's what we want
        } catch( Throwable e ) {
            fail( "Invalid exception thrown." );
        }
        
        PaletteActions actions = new DummyActions();
        PaletteController controller = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, actions );
        Node rootNode = (Node)controller.getRoot().lookup( Node.class );
        assertNotNull( rootNode );
        assertEquals( actions, controller.getRoot().lookup( PaletteActions.class ) );
    }
    
    public void testCreatePaletteNodes() throws Exception {
        PaletteActions actions = new DummyActions();
        Node rootNode = DummyPalette.createPaletteRoot();
        PaletteController controller = PaletteFactory.createPalette( rootNode, actions );
        assertEquals( rootNode, controller.getRoot().lookup( Node.class ) );
        assertEquals( actions, controller.getRoot().lookup( PaletteActions.class ) );
    }
    
}
