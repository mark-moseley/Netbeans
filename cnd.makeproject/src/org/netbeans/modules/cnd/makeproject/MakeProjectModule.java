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

package org.netbeans.modules.cnd.makeproject;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.modules.ModuleInstall;

public class MakeProjectModule extends ModuleInstall {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(MakeProjectModule.class.getName());
        
    private CustomizerNode profileCustomizerNode;
    
    @Override
    public void restored() {
        // Moved to services...
//	RunProfileProvider profileProvider = new RunProfileProvider();
//	ConfigurationDescriptorProvider.addAuxObjectProvider(profileProvider);
//	profileCustomizerNode = new RunProfileNodeProvider().createProfileNode();
//	CustomizerRootNodeProvider.getInstance().addCustomizerNode(profileCustomizerNode);

	//see issue #64393
        DataLoaderPool.getDefault().addOperationListener(new L());
        
    }
    
    public void uninstall() {
    }
    
    @Override
    public void close() {
	CompilerSetManager csm = CompilerSetManager.getDefault(false);
	if (csm != null) {
	    for (CompilerSet cs : csm.getCompilerSets()) {
		for (Tool tool : cs.getTools()) {
		    if (tool instanceof CCCCompiler) { // FIXUP: should implement/use 'capability' of tool
			((CCCCompiler) tool).saveSystemIncludesAndDefines();
		    }
		}
	    }
        }
    }
            
    public static class ActionWrapper extends CallableSystemAction implements ContextAwareAction, PropertyChangeListener {
        
        private Action action;
        
        public ActionWrapper( Action action ) {
            this.action = action;            
        }
            
        public String getName() {
            return (String)action.getValue( Action.NAME );
        }

        @Override
        public String iconResource() {
            return null;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public void actionPerformed( ActionEvent ev ) {
            action.actionPerformed(ev);
        }
        
        @Override
        public boolean isEnabled() {
            return action.isEnabled();            
        }

        @Override
        protected void addNotify() {
            this.action.addPropertyChangeListener( this );
            super.addNotify();
        }
        
        @Override
        protected void removeNotify() {
            this.action.removePropertyChangeListener( this );
            super.removeNotify();
        }
        
        public void performAction() {
            actionPerformed( new ActionEvent( this, 0, "" ) ); // NOI18N
        }
        
        public Action createContextAwareInstance( Lookup actionContext ) {
            return ((ContextAwareAction)action).createContextAwareInstance( actionContext );
        }
        
        public void propertyChange( PropertyChangeEvent evt ) {
            firePropertyChange( evt.getPropertyName(), evt.getOldValue(), evt.getNewValue() );
        }
        
        
    }
    
    
    public static class CompileWrapper extends ActionWrapper {
        
        CompileWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_COMPILE_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_CompileFile_Action" ), // NOI18N
                       null ) );
        }
        
    }
    
    public static class RunWrapper extends ActionWrapper {
        RunWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_RunFile_Action" ), // NOI18N
                       null ) );
            
        }
    }
    
    public static class DebugWrapper extends ActionWrapper {
        DebugWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage( MakeProjectModule.class, "LBL_DebugFile_Action" ), // NOI18N
                       null ) );
        }
    }
    
    /**See issue #64393
     */
    private static class L implements OperationListener {
        
        public L() {
        }
        
        public void operationPostCreate(OperationEvent operationEvent) {
        }

        public void operationCopy(OperationEvent.Copy copy) {
        }

        public void operationMove(OperationEvent.Move move) {
        }

        public void operationDelete(OperationEvent operationEvent) {
        }

        public void operationRename(OperationEvent.Rename rename) {
        }

        public void operationCreateShadow(OperationEvent.Copy copy) {
        }

        private MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
            ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
            
            if (pdp == null) {
                return null;
            }
            
            return (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        }
        
        public void operationCreateFromTemplate(OperationEvent.Copy copy) {
            Folder  folder = Utilities.actionsGlobalContext().lookup(Folder.class);
            Project p      = Utilities.actionsGlobalContext().lookup(Project.class);
            
            if (folder == null || p == null) {
                //maybe a file belonging into a project is selected. Try:
                DataObject od = Utilities.actionsGlobalContext().lookup(DataObject.class);
                
                if (od == null) {
                    //no file:
                    return ;
                }
                
                FileObject file = od.getPrimaryFile();
                
                p = FileOwnerQuery.getOwner(file);
                
                if (p == null) {
                    //no project:
                    return ;
                }
                
                File f = FileUtil.toFile(file);
                
                if (f == null) {
                    //not a physical file:
                    return ;
                }
                
                //check if the project is a Makefile project:
                MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);
                
                if (makeConfigurationDescriptor == null) {
                    //no:
                    return ;
                }
                
                Item i = makeConfigurationDescriptor.findProjectItemByPath(f.getAbsolutePath());
                
                if (i == null) {
                    //no item, does not really belong into this project:
                    return ;
                }
                
                //found:
                folder = i.getFolder();
            }
            
            MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);
                
            assert makeConfigurationDescriptor != null;
                
            FileObject file = copy.getObject().getPrimaryFile();
            Project owner = FileOwnerQuery.getOwner(file);
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "processing file=" + file); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "FileUtil.toFile(file.getPrimaryFile())=" + FileUtil.toFile(file)); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "into folder = " + folder); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "in project = " + p.getProjectDirectory()); // NOI18N
            }
            
            if (owner != null && owner.getProjectDirectory() == p.getProjectDirectory()) {
                File ioFile = FileUtil.toFile(file);
                if (ioFile.isDirectory())
                    return; // don't add directories.
                String itemPath;
                if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                    itemPath = IpeUtils.toAbsoluteOrRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
                }
                else if (PathPanel.getMode() == PathPanel.REL) {
                    itemPath = IpeUtils.toRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
                }
                else {
                    itemPath = ioFile.getPath();
                }
                itemPath = FilePathAdaptor.mapToRemote(itemPath);
                itemPath = FilePathAdaptor.normalize(itemPath);
                Item item = new Item(itemPath);

                folder.addItemAction(item);
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "folder: " + folder + ", added: " + file); // NOI18N
                }
            } else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "not adding: " + file + " because it is not owned by this project"); // NOI18N
                }
            }
        }
        
    }
    
}
