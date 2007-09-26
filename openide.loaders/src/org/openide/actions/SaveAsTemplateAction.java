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

package org.openide.actions;


import java.io.IOException;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/** Saves a data object to a folder under in the
* system's templates area.
*
* @author  Ales Novak, Dafe Simonek
*/
public final class SaveAsTemplateAction extends NodeAction {

    public HelpCtx getHelpCtx () {
        return new HelpCtx (SaveAsTemplateAction.class);
    }

    public String getName () {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "SaveAsTemplate");
    }

    /** @deprecated Should never be called publically. */
    @Deprecated
    public String iconResource () {
        return super.iconResource ();
    }

    protected boolean surviveFocusChange () {
        return false;
    }

    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0)
            return false;
        // test if all nodes support saving as template
        DataObject curCookie;
        for (int i = 0; i < activatedNodes.length; i++) {
            curCookie = (DataObject)activatedNodes[i].getCookie(DataObject.class);
            if ((curCookie == null) || (!curCookie.isCopyAllowed()))
                // not supported
                return false;
        }
        return true;
    }

    /* Performs the action - launches new file dialog,
    * saves as a template ...
    * Overrides abstract enable(..) from superclass.
    *
    * @param activatedNodes Array of activated nodes
    */
    protected void performAction (Node[] activatedNodes) {
        // prepare variables
        NodeAcceptor acceptor = FolderNodeAcceptor.getInstance();
        String title = NbBundle.getMessage(org.openide.loaders.DataObject.class, "Title_SaveAsTemplate");
        String rootTitle = NbBundle.getMessage(org.openide.loaders.DataObject.class, "CTL_SaveAsTemplate");
        Node templatesNode = NewTemplateAction.getTemplateRoot ();
        templatesNode.setDisplayName(NbBundle.getMessage(org.openide.loaders.DataObject.class, "CTL_SaveAsTemplate_TemplatesRoot"));
        Node[] selected;
        // ask user: where to save the templates?
        try {
            selected = NodeOperation.getDefault().
                       select(title, rootTitle, templatesNode, acceptor, null);
        } catch (UserCancelException ex) {
            // user cancelled the operation
            return;
        }
        // create & save them all
        // we know DataFolder and DataObject cookies must be supported
        // so we needn't check for null values
        DataFolder targetFolder =
            (DataFolder)selected[0].getCookie(DataFolder.class);
        for (int i = 0; i < activatedNodes.length; i++ ) {
            createNewTemplate(
                (DataObject)activatedNodes[i].getCookie(DataObject.class),
                targetFolder);
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }

    /** Performs the work of creating a new template */
    private void createNewTemplate(DataObject source,
                                   DataFolder targetFolder) {
        try {
            SaveCookie cookie = (SaveCookie)source.getCookie (SaveCookie.class);
            if (cookie != null) {
                cookie.save ();
            }
            DataObject newTemplate = source.copy(targetFolder);
            newTemplate.setTemplate(true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** Inner class functioning like node acceptor for
    * user dialogs when selecting where to save as template.
    * Accepts folders only. Singleton.
    */
    static final class FolderNodeAcceptor implements NodeAcceptor {

        /** an instance */
        private static FolderNodeAcceptor instance;

        /** singleton */
        private FolderNodeAcceptor() {
        }

        /** accepts a selected folder */
        public final boolean acceptNodes(Node[] nodes) {
            if (nodes == null || nodes.length != 1) return false;
            return nodes[0].getCookie(DataFolder.class) != null;
        }

        /** getter for an instance */
        static FolderNodeAcceptor getInstance() {
            if (instance == null) instance = new FolderNodeAcceptor();
            return instance;
        }
    } // end of FolderNodeAcceptor inner class

}
