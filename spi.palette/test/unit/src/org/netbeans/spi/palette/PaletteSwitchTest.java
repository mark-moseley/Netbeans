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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.palette;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
/**
 *
 * @author S. Aubrecht
 */
public class PaletteSwitchTest extends AbstractPaletteTestHid {
    
    private String lookupPaletteRootName;
    private FileObject lookupPaletteRootFolder;
    
    static String mimePaletteRootName;
    private static FileObject mimePaletteRootFolder;
    private static final String MIME_TYPE_NAME = "text/x-paletteswitchtest";
    
    private FileObject dummyDocumentFile;
    private final static String DUMMY_DOCUMENT_FILE_EXTENSION = "junitPaletteSwitchTest";
    
    static {
        String[] layers = new String[] {"org/netbeans/spi/palette/mf-layer.xml"};//NOI18N
        Object[] instances = new Object[] { new MyMimeResolver() };
        IDEInitializer.setup(layers,instances);
    }
    
    public PaletteSwitchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        
        lookupPaletteRootName = "lookupPalette" + System.currentTimeMillis();
        lookupPaletteRootFolder = fs.getRoot().createFolder( lookupPaletteRootName );
        createDefaultPaletteContentInFolder( lookupPaletteRootFolder );
        
        if( null == mimePaletteRootName ) {
            mimePaletteRootName = "mimePalette" + System.currentTimeMillis();
            mimePaletteRootFolder = fs.getRoot().createFolder( mimePaletteRootName );
            createDefaultPaletteContentInFolder( mimePaletteRootFolder );
        }
        
        dummyDocumentFile = fs.getRoot().createData( "dummyDocumentFile" + System.currentTimeMillis(), DUMMY_DOCUMENT_FILE_EXTENSION );
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testNoLookupPalette() throws IOException {
        TopComponent tc = createTopComponentWithPalette( null );
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false );
        
        assertNull( foundPalette );
    }
    
    public void testNoMimePalette() throws IOException {
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( "text/_unknown" );
        
        assertNull( foundPalette );
    }
    
    public void testLookupPalette() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        
        TopComponent tc = createTopComponentWithPalette( pc );
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false );
        
        assertNotNull( foundPalette );
        assertEquals( pc.getModel().getName(), foundPalette.getModel().getName() );
    }
    
    public void testMimePalette() throws IOException {
        TopComponent tc = createTopComponentWithPalette( null );
        tc.setActivatedNodes( new Node[] { DataObject.find( dummyDocumentFile ).getNodeDelegate() } );
        tc.open();
        
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( MIME_TYPE_NAME );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
        
        foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
    }
    
    
    public void testLookupPaletteTakePrecendsOverMimePalette() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        
        TopComponent tc = createTopComponentWithPalette( pc );
        tc.setActivatedNodes( new Node[] { DataObject.find( dummyDocumentFile ).getNodeDelegate() } );
        
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( MIME_TYPE_NAME );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
        
        foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false );
        assertNotNull( foundPalette );
        assertEquals( pc.getModel().getName(), foundPalette.getModel().getName() );
    }
    
    public void testVisibilityPerDocumentType() throws Exception {
        final TopComponent palette = PaletteTopComponent.getDefault();
        assertNotNull( palette );
        
        final PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        final MyTopComponent paletteTc1 = createTopComponentWithPalette( pc );
        paletteTc1.open();

        final MyTopComponent paletteTc2 = createTopComponentWithPalette( null );
        paletteTc2.setActivatedNodes( new Node[] { DataObject.find( dummyDocumentFile ).getNodeDelegate() } );
        paletteTc2.open();
        
        final MyTopComponent noPaletteTc = createTopComponentWithPalette( null );
        noPaletteTc.open();

        noPaletteTc.requestActive();
        final PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        paletteSwitch.startListening();
        
        assertNull( "No PaletteController for TC which doesn't provide one", 
                paletteSwitch.getCurrentPalette() );
        assertFalse( "Palette window cannot be opened when a document without PaletteController is active", 
                palette.isOpened() );
        
        paletteTc1.requestActive();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                assertEquals( TopComponent.getRegistry().getActivated(), paletteTc1 );
                assertNotNull( "TC with PaletteController is active", 
                        paletteSwitch.getCurrentPalette() );
                assertTrue( "Palette window opens by default when a document with PaletteController is active", 
                        palette.isOpened() );
                
                noPaletteTc.requestActive();

                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        assertFalse( "Palette window closes when document without PaletteController is active", 
                                palette.isOpened() );
                        
                        paletteTc1.requestActive();
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                assertTrue( "Palette window opens by default when a document with PaletteController is active", 
                                        palette.isOpened() );
                                
                                //simulate closing palette window by user
                                palette.close();
                                
                                noPaletteTc.requestActive();
                                paletteTc1.requestActive();

                                SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                        assertFalse( "Palette window doesn't show up even when document with PaletteController is active because the palette window was closed by the user before.", 
                                                palette.isOpened() );
                                        
                                        paletteTc2.requestActive();
                                
                                        SwingUtilities.invokeLater( new Runnable() {
                                            public void run() {
                                                assertTrue( "Palette window opens by default when a different document with PaletteController is active", 
                                                        palette.isOpened() );
                                                paletteSwitch.stopListening();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    
    private MyTopComponent createTopComponentWithPalette( PaletteController pc ) throws IOException {
        MyTopComponent tc = new MyTopComponent( pc );
        
        Mode editorMode = WindowManagerImpl.getInstance().findMode( "unitTestEditorMode" );
        if( null == editorMode ) {
            editorMode = WindowManagerImpl.getInstance().createMode( "unitTestEditorMode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        }
        editorMode.dockInto(tc);
        return tc;
    }
    
    private static class MyTopComponent extends TopComponent {
        private boolean hackIsShowing = false;
        
        public MyTopComponent( PaletteController palette ) throws DataObjectNotFoundException {
            this( new InstanceContent(), palette );
        }
        
        private MyTopComponent( InstanceContent ic, PaletteController palette ) throws DataObjectNotFoundException {
            super( new AbstractLookup( ic ) );
            if( null != palette )
                ic.add( palette );
        }
        
        @Override
        public boolean isShowing() {
            return hackIsShowing;
        }

        @Override
        protected void componentActivated() {
            super.componentActivated();
            hackIsShowing = true;
        }

        @Override
        protected void componentDeactivated() {
            super.componentDeactivated();
            hackIsShowing = false;
        }
    }
    
    public static class MyMimeResolver extends MIMEResolver {
        
        public String findMIMEType(FileObject fo) {
            if( DUMMY_DOCUMENT_FILE_EXTENSION.equals( fo.getExt() ) )
                return MIME_TYPE_NAME;
            return null;
        }
    }
    
    
}
