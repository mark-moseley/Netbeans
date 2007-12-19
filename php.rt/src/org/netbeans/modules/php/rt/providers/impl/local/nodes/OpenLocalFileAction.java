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
package org.netbeans.modules.php.rt.providers.impl.local.nodes;

import java.io.File;
import org.netbeans.modules.php.rt.actions.*;
import java.util.logging.Logger;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;


/**
 * This is actually is implementation action.
 * Each provider provides its own nodes and as result provider
 * has full freedom for assigning any action to node.
 * This action is quite common action for Web server host Node and
 * I put it here.
 *
 * This action is designed for working only with AbstractServerNode
 *
 * @author ads
 *
 */
public class OpenLocalFileAction extends OpenAction {

    private static final long serialVersionUID = 897647820461070358L;

    private static final String LBL_OPEN_FILE = "LBL_OpenFile"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(OpenLocalFileAction.class.getName());
    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#enable(org.openide.nodes.Node[])
     */

    public static OpenLocalFileAction findInstance() {
        return SharedClassObject.findObject(OpenLocalFileAction.class, true);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length == 0){
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#performAction(org.openide.nodes.Node[])
     */
    @Override
    protected void performAction(Node[] nodes) {
        for (Node node : nodes) {
            File file = getFile(node);
            if (!checkFile(file)){
                continue;
            }
            FileObject fo = FileUtil.toFileObject(file);
            openFile(fo);
        }
    }

    
    public static void openFile(FileObject fileObject){
        try {
            DataObject dataObject = DataObject.find(fileObject);
            if (dataObject != null) {
                OpenCookie openCookie = dataObject.getCookie(OpenCookie.class);
                if (openCookie != null) {
                    openCookie.open();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openide.util.actions.SystemAction#getHelpCtx()
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private boolean checkFile(File file){
        if (file == null){
            
            return false;
        }
        if (!file.exists()){
            return false;
        }
        if (!file.canRead()){
            return false;
        }
        return true;
    }
    
    private File getFile(Node node) {
        if (node instanceof Lookup.Provider) {
            return (File) ((Lookup.Provider) node).getLookup().lookup( File.class );
        } else {
            return null;
        }
    }

}
