package org.netbeans.modules.openfile;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import junit.framework.*;
import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author S. Aubrecht
 */
public class DefaultExternalDropHandlerTest extends NbTestCase {
    
    public DefaultExternalDropHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return new TestSuite(DefaultExternalDropHandlerTest.class);
    }

    public void testUriListFlavor() throws ClassNotFoundException {
        DefaultExternalDropHandler handler = new DefaultExternalDropHandler();

        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        assertEquals( uriListFlavor, handler.getUriListDataFlavor() );
    }

    public void testCanDrop() throws ClassNotFoundException {
        DefaultExternalDropHandler handler = new DefaultExternalDropHandler();

        DataFlavor[] flavors = new DataFlavor[] { DataFlavor.imageFlavor };
        assertFalse( handler.canDrop( flavors ) );

        flavors = new DataFlavor[] { DataFlavor.imageFlavor, DataFlavor.stringFlavor };
        assertFalse( handler.canDrop( flavors ) );

        flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor };
        assertTrue( handler.canDrop( flavors ) );

        flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor, DataFlavor.imageFlavor };
        assertTrue( handler.canDrop( flavors ) );

        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        flavors = new DataFlavor[] { uriListFlavor };
        assertTrue( handler.canDrop( flavors ) );

        flavors = new DataFlavor[] { uriListFlavor, DataFlavor.imageFlavor };
        assertTrue( handler.canDrop( flavors ) );

        flavors = new DataFlavor[] { uriListFlavor, DataFlavor.javaFileListFlavor, DataFlavor.imageFlavor };
        assertTrue( handler.canDrop( flavors ) );
    }

    public void testGetFileList() throws ClassNotFoundException, URISyntaxException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        DefaultExternalDropHandler handler = new DefaultExternalDropHandler();
        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        Transferable t = new TransferableImpl( new ArrayList(), new DataFlavor[] { DataFlavor.imageFlavor, DataFlavor.stringFlavor } );
        assertNull( handler.getFileList( t ) );

        ArrayList list = new ArrayList( 2 );
        list.add( new File("//somedir/somefile.someext") );
        list.add( new File("c:\\somedir\\somefile.someext") );
        t = new TransferableImpl( list, new DataFlavor[] { DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor } );

        List extractedList = handler.getFileList( t );
        assertNotNull( extractedList );
        assertEquals( 2, extractedList.size() );
        for( Iterator i=list.iterator(); i.hasNext(); ) {
            assertTrue( extractedList.contains( i.next() ) );
        }

        String uriList = "file:///somedir/somefile.someext\r\nfile:/c:/somedir/somefile.someext";
        t = new TransferableImpl( uriList, new DataFlavor[] { uriListFlavor, DataFlavor.stringFlavor } );
        extractedList = handler.getFileList( t );
        assertNotNull( extractedList );
        assertEquals( 2, extractedList.size() );
        assertTrue( extractedList.contains( new File( new URI("file:///somedir/somefile.someext") ) ) );
        assertTrue( extractedList.contains( new File( new URI("file:/c:/somedir/somefile.someext") ) ) );
    }

    public void testOpenFile() throws IOException {
        DefaultExternalDropHandler handler = new DefaultExternalDropHandler();
        MockOpenFileImpl openImpl = new MockOpenFileImpl();
        OpenFile.setImpl( openImpl );


        File file = File.createTempFile( "somefile", ".someext", getWorkDir() );
        handler.openFile( file );
        assertTrue( openImpl.wasOpened( file ) );
        if( null != file ) {
            file.delete();
            file.deleteOnExit();
        }
    }

    private static class MockOpenFileImpl implements OpenFileImpl {
        private Set/*<FileObject>*/ openedFiles = new HashSet();

        public boolean open(FileObject fileObject, int line) {
            if( null != fileObject ) {
                openedFiles.add( FileUtil.toFile(fileObject) );
                return true;
            }
            return false;
        }

        public boolean wasOpened( File file ) {
            return openedFiles.contains( file );
        }
    }

    private static class TransferableImpl implements Transferable {
        private Object data;
        private DataFlavor[] flavors;

        public TransferableImpl( Object data, DataFlavor[] flavors ) {
            this.data = data;
            this.flavors = flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for( int i=0; i<flavors.length; i++ ) {
                if( flavor.equals( flavors[i] ) )
                    return true;
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if( isDataFlavorSupported( flavor ) ) {
                return data;
            }
            throw new UnsupportedFlavorException( flavor );
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
    }

    private static class MockDropTarget extends DropTarget {
        private JComponent component = new JPanel();

        public MockDropTarget() {
        }

        public Component getComponent() {
            return component;
        }

    }
}
