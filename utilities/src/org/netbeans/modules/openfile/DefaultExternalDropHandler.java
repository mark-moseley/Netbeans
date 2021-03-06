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

package org.netbeans.modules.openfile;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.ExternalDropHandler;

/**
 *
 * @author S. Aubrecht
 */
public class DefaultExternalDropHandler extends ExternalDropHandler {
    
    public boolean canDrop(DropTargetDragEvent e) {
        return canDrop( e.getCurrentDataFlavors() );
    }

    public boolean canDrop(DropTargetDropEvent e) {
        return canDrop( e.getCurrentDataFlavors() );
    }

    boolean canDrop( DataFlavor[] flavors ) {
        for( int i=0; null != flavors && i<flavors.length; i++ ) {
            if( DataFlavor.javaFileListFlavor.equals( flavors[i] )
                || getUriListDataFlavor().equals( flavors[i] ) ) {

                return true;
            }
        }
        return false;
    }

    public boolean handleDrop(DropTargetDropEvent e) {
        Transferable t = e.getTransferable();
        if( null == t )
            return false;
        List<File> fileList = getFileList( t );

        if( null != fileList && !fileList.isEmpty() ) {
            for (File file : fileList) {
                openFile(file);
            }
            return true;
        }
        return false;
    }

    List<File> getFileList( Transferable t ) {
        try {
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
                //windows & mac
                return (List<File>) t.getTransferData( DataFlavor.javaFileListFlavor );
            } else if( t.isDataFlavorSupported( getUriListDataFlavor() ) ) {
                //linux
                String uriList = (String)t.getTransferData( getUriListDataFlavor() );
                return textURIListToFileList( uriList );
            }
        } catch( UnsupportedFlavorException ex ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ex );
        } catch( IOException ex ) {
            // Ignore. Can be just "Owner timed out" from sun.awt.X11.XSelection.getData.
            Logger.getLogger(DefaultExternalDropHandler.class.getName()).log(Level.FINE, null, ex);
        }
        return null;
    }

    void openFile( File file ) {
        FileObject fo = FileUtil.toFileObject( FileUtil.normalizeFile( file ) );
        OpenFile.open(fo, -1);
    }

    private static DataFlavor uriListDataFlavor;

    DataFlavor getUriListDataFlavor() {
        if( null == uriListDataFlavor ) {
            try {
                uriListDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            } catch( ClassNotFoundException cnfE ) {
                //cannot happen
                throw new AssertionError(cnfE);
            }
        }
        return uriListDataFlavor;
    }

    List<File> textURIListToFileList( String data ) {
        List<File> list = new ArrayList<File>(1);
        for( StringTokenizer st = new StringTokenizer(data, "\r\n");
            st.hasMoreTokens();) {
            String s = st.nextToken();
            if( s.startsWith("#") ) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = new File(uri);
                list.add( file );
            } catch( java.net.URISyntaxException e ) {
                // malformed URI
            } catch( IllegalArgumentException e ) {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }
}
