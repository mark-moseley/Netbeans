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
package org.netbeans.modules.localhistory.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Tomas Stupka
 */
public class ShowLocalHistoryAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public ShowLocalHistoryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected void performAction(final Node[] activatedNodes) {                        
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        final Set<File> rootSet = ctx.getRootFiles();                    

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                File[] files = rootSet.toArray(new File[rootSet.size()]);                

                final LocalHistoryTopComponent tc = new LocalHistoryTopComponent();
                tc.setName(NbBundle.getMessage(this.getClass(), "CTL_LocalHistoryTopComponent", files[0].getName()));
                tc.open();
                tc.requestActive();                                
                
                if(files[0].isFile()) {
                    LocalHistoryFileView fileView = new LocalHistoryFileView();                
                    LocalHistoryDiffView diffView = new LocalHistoryDiffView(tc); 
                    fileView.getExplorerManager().addPropertyChangeListener(diffView); 
                    fileView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                                tc.setActivatedNodes((Node[]) evt.getNewValue());  
                            }
                        } 
                    });
                    tc.init(diffView.getPanel(), fileView);
                    fileView.refresh(files);
                } 
            }
        });

    }
    
    protected boolean enable(Node[] activatedNodes) {     
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<File> rootSet = ctx.getRootFiles();                
        if(rootSet == null || rootSet.size() == 0) { 
            return false;
        }                        
        for (File file : rootSet) {            
            if(file != null && !file.isFile()) {
                return false;
            }
        }        
        return true;           
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowLocalHistory");        
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowLocalHistoryAction.class);
    }
    
}
