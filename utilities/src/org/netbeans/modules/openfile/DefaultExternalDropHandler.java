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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
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
        if ((fileList == null) || fileList.isEmpty()) {
            return false;
        }

        //TODO - remove the code below once issue #46813 is resolved
        //#46813 - NetBeans does not support UNC paths
        Object errMsg = null;
        if (fileList.size() == 1) {
            errMsg = openFile(fileList.get(0));
        } else {
            boolean hasSomeSuccess = false;
            boolean failureNonUNC = false;
            List<String> fileErrs = null;
            for (File file : fileList) {
                String fileErr = openFile(file);
                if (fileErr == null) {
                    hasSomeSuccess = true;
                } else {
                    if (!OpenFile.isSpecifiedByUNCPath(file)) {
                        failureNonUNC = true;
                    }
                    if (fileErrs == null) {
                        fileErrs = new ArrayList<String>(fileList.size());
                    }
                    fileErrs.add(fileErr);
                }
            }
            if (fileErrs != null) {         //some file could not be opened
                String mainMsgKey;
                if (!hasSomeSuccess && !failureNonUNC) {
                    /* all of the files were specified by UNC paths */
                    mainMsgKey = "MSG_could_not_open_any_file_all_UNC"; //NOI18N
                    fileErrs = null;
                } else if (hasSomeSuccess) {
                    mainMsgKey = "MSG_could_not_open_some_files";       //NOI18N
                } else {
                    mainMsgKey = "MSG_could_not_open_any_file";         //NOI18N
                }
                String mainMsg = NbBundle.getMessage(OpenFile.class, mainMsgKey);
                if (fileErrs == null) {
                    errMsg = mainMsg;
                } else {
                    JComponent msgPanel = new JPanel();
                    msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
                    msgPanel.add(new JLabel(mainMsg));
                    msgPanel.add(Box.createVerticalStrut(12));
                    for (String fileErr : fileErrs) {
                        msgPanel.add(new JLabel(fileErr));
                    }
                    errMsg = msgPanel;
                }
            }
        }
        if (errMsg != null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            errMsg,
                            NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
        return true;
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

    /**
     * Opens the given file.
     * @param  file  file to be opened
     * @return  {@code null} if the file was successfully opened;
     *          or a localized error message in case of failure
     */
    String openFile( File file ) {
        FileObject fo = FileUtil.toFileObject( FileUtil.normalizeFile( file ) );
        if (fo == null) {
            //TODO - remove once issue #46813 is resolved
            //#46813 - NetBeans does not support UNC paths
            String msgKey;
            if (OpenFile.isSpecifiedByUNCPath(file)) {
                msgKey = "MSG_UncNotSupported";                         //NOI18N
            } else {
                msgKey = "MSG_FilePathTypeNotSupported";                //NOI18N
            }
            return NbBundle.getMessage(OpenFile.class, msgKey, file.toString());
        }
        return OpenFile.open(fo, -1);
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
