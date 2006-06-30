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

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 * Lets user pick an icon for a given layer file.
 * @author Jesse Glick
 */
public class PickIconAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        FileObject f = ((DataObject) activatedNodes[0].getCookie(DataObject.class)).getPrimaryFile();
        URL location = (URL) f.getAttribute("WritableXMLFileSystem.location"); // NOI18N
        assert location != null : f;
        NbModuleProject p = (NbModuleProject) FileOwnerQuery.getOwner(URI.create(location.toExternalForm()));
        assert p != null : location;
        FileObject src = p.getSourceDirectory();
        JFileChooser chooser = UIUtil.getIconFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, FileUtil.toFile(src));
        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        FileObject icon = FileUtil.toFileObject(chooser.getSelectedFile());
        // XXX might instead get WritableXMLFileSystem.cp and search for it in there:
        String iconPath = FileUtil.getRelativePath(src, icon);
        try {
            if (iconPath == null) {
                String folderPath;
                String layerPath = ManifestManager.getInstance(p.getManifest(), false).getLayer();
                if (layerPath != null) {
                    folderPath = layerPath.substring(0, layerPath.lastIndexOf('/'));
                } else {
                    folderPath = p.getCodeNameBase().replace('.', '/') + "/resources"; // NOI18N
                }
                FileObject folder = FileUtil.createFolder(src, folderPath);
                FileUtil.copyFile(icon, folder, icon.getName(), icon.getExt());
                iconPath = folderPath + '/' + icon.getNameExt();
            }
            f.setAttribute("SystemFileSystem.icon", new URL("nbresloc:/" + iconPath)); // NOI18N
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        FileObject f = ((DataObject) activatedNodes[0].getCookie(DataObject.class)).getPrimaryFile();
        URL location = (URL) f.getAttribute("WritableXMLFileSystem.location"); // NOI18N
        return location != null; // #63458
    }

    public String getName() {
        return NbBundle.getMessage(PickIconAction.class, "LBL_pick_icon");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {DataObject.class};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
