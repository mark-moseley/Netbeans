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

package org.netbeans.modules.subversion.ui.actions;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.LifecycleManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import java.text.MessageFormat;
import java.text.DateFormat;
import java.io.File;
import java.util.MissingResourceException;
import java.util.Date;
import java.awt.event.ActionEvent;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Base for all context-sensitive SVN actions.
 * 
 * @author Maros Sandor
 */
public abstract class ContextAction extends NodeAction {

    // it's singleton
    // do not declare any instance data

    protected ContextAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName(Node[] activatedNodes);

    protected boolean enable(Node[] nodes) {
        return getContext(nodes).getRootFiles().length > 0;
    }
    
    /**
     * Synchronizes memory modificatios with disk and calls
     * {@link  #performContextAction}.
     */
    protected void performAction(final Node[] nodes) {
        // TODO try to save files in invocation context only
        // list somehow modified file in the context and save
        // just them.
        // The same (global save) logic is in CVS, no complaint
        LifecycleManager.getDefault().saveAll();        
        performContextAction(nodes);           
    }

    protected SVNUrl getSvnUrl(Node[] nodes) throws SVNClientException {
        return getSvnUrl(getContext(nodes)); 
    }

    public static SVNUrl getSvnUrl(Context ctx) throws SVNClientException {
        File[] roots = ctx.getRootFiles();
        return SvnUtils.getRepositoryRootUrl(roots[0]);        
    }

    protected abstract void performContextAction(Node[] nodes);

    /** Be sure nobody overwrites */
    public final boolean isEnabled() {
        return super.isEnabled();
    }

    /** Be sure nobody overwrites */
    public final void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /** Be sure nobody overwrites */
    public final void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
    }

    /** Be sure nobody overwrites */
    public final void performAction() {
        super.performAction();
    }    

    /**
     * Running action display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName() + "Running"</code> key
     *   <li><code>getBaseName() + "Running_Context"</code> key for one selected file
     *   <li><code>getBaseName() + "Running_Context_Multiple"</code> key for multiple selected files
     *   <li><code>getBaseName() + "Running_Project"</code> key for one selected project
     *   <li><code>getBaseName() + "Running_Projects"</code> key for multiple selected projects
     * </ul>
     */    
    public String getRunningName(Node [] activatedNodes) {
        return getName("Running", activatedNodes); // NOI18N
    }

    public String getName() {
        return getName("", TopComponent.getRegistry().getActivatedNodes()); // NOI18N
    }
    
    /**
     * Display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName()</code> key
     *   <li><code>getBaseName() + "_Context"</code> key for one selected file
     *   <li><code>getBaseName() + "_Context_Multiple"</code> key for multiple selected files
     *   <li><code>getBaseName() + "_Project"</code> key for one selected project
     *   <li><code>getBaseName() + "_Projects"</code> key for multiple selected projects
     * </ul>
     */
    public String getName(String role, Node[] activatedNodes) {
        String baseName = getBaseName(activatedNodes) + role;
        if (!isEnabled()) {
            return NbBundle.getBundle(this.getClass()).getString(baseName);
        }

        File [] nodes = SvnUtils.getCurrentContext(activatedNodes, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return NbBundle.getBundle(this.getClass()).getString(baseName);
        } else if (objectCount == 1) {
            if (projectsOnly) {
                String dispName = ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
                return NbBundle.getMessage(this.getClass(), baseName + "_Context",  // NOI18N
                                                dispName);
            }
            String name;
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                name = fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    name = dao.getPrimaryFile().getNameExt();
                } else {
                    name = activatedNodes[0].getDisplayName();
                }
            }
            return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Context"),  // NOI18N
                                            new Object [] { name });
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Projects"),  // NOI18N
                                                new Object [] { new Integer(objectCount) });
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Context_Multiple"),  // NOI18N
                                        new Object [] { new Integer(objectCount) });
        }
    }
    
    /**
     * Computes display name of the context this action will operate.
     * 
     * @return String name of this action's context, e.g. "3 files", "MyProject", "2 projects", "Foo.java". Returns
     * null if the context is empty
     */ 
    public String getContextDisplayName(Node [] activatedNodes) {
        // TODO: reuse this code in getName() 
        File [] nodes = SvnUtils.getCurrentContext(activatedNodes, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return null;
        } else if (objectCount == 1) {
            if (projectsOnly) {
                return ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
            }
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                return fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    return dao.getPrimaryFile().getNameExt();
                } else {
                    return activatedNodes[0].getDisplayName();
                }
            }
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(ContextAction.class).getString("MSG_ActionContext_MultipleProjects"),  // NOI18N
                                                new Object [] { new Integer(objectCount) });
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(ContextAction.class).getString("MSG_ActionContext_MultipleFiles"),  // NOI18N
                                        new Object [] { new Integer(objectCount) });
        }
    }    
        
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    protected Context getContext(Node[] nodes) {
        return SvnUtils.getCurrentContext(nodes, getFileEnabledStatus(), getDirectoryEnabledStatus());
    }
    
    protected int getFileEnabledStatus() {
        return ~0;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected RequestProcessor createRequestProcessor(Node[] nodes) {
        SVNUrl repository = null;
        try {
            repository = getSvnUrl(nodes);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
        }        
        return Subversion.getInstance().getRequestProcessor(repository);
    }

    protected abstract static class ProgressSupport  extends SvnProgressSupport {

        private final ContextAction action;
        private final Node[] nodes;
        private long progressStamp;
        private String runningName;
        public ProgressSupport(ContextAction action, Node[] nodes) {
            this.action = action;
            this.nodes = nodes;
        }

        public RequestProcessor.Task start(RequestProcessor  rp) {
            runningName = ActionUtils.cutAmpersand(action.getRunningName(nodes));
            SVNUrl url = null;
            try {
                url = getSvnUrl(action.getContext(nodes));
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }                    
            return start(rp, url, runningName);
        }

        public abstract void perform();

        protected void startProgress() {
            getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + runningName); // NOI18N
            ProgressHandle progress = getProgressHandle();
            progress.setInitialDelay(500);
            progressStamp = System.currentTimeMillis() + 500;
            progress.start();            
        }

        protected void finnishProgress() {
            // TODO add failed and restart texts                

            final ProgressHandle progress = getProgressHandle();
            progress.switchToDeterminate(100);
            progress.progress(NbBundle.getMessage(ContextAction.class, "MSG_Progress_Done"), 100); // NOI18N
            if (System.currentTimeMillis() > progressStamp) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        progress.finish();
                    }
                }, 15 * 1000);
            } else {
                progress.finish();
            }

            if (isCanceled() == false) {
                getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + runningName + " " + NbBundle.getMessage(ContextAction.class, "MSG_Progress_Finished")); // NOI18N
            } else {
                getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + runningName + " " + NbBundle.getMessage(ContextAction.class, "MSG_Progress_Canceled")); // NOI18N
            }
        }
    }
}
