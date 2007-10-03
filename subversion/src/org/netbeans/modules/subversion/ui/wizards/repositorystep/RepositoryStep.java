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

package org.netbeans.modules.subversion.ui.wizards.repositorystep;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
        
/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, PropertyChangeListener {

    public static final String IMPORT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.import";
    public static final String CHECKOUT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.checkout";
    public static final String URL_PATTERN_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.urlPattern";
    
    private Repository repository;        
    private RepositoryStepPanel panel;    
    private RepositoryFile repositoryFile;    
    private int repositoryModeMask;
    private WizardStepProgressSupport support;

    private final String helpID;
    
    public RepositoryStep(String helpID) {
        this.repositoryModeMask = 0;
        this.helpID = helpID;
    }
    
    public RepositoryStep(int repositoryModeMask, String helpID) {
        this.repositoryModeMask = repositoryModeMask;
        this.helpID = helpID;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(helpID);
    }        

    protected JComponent createComponent() {
        if (repository == null) {         
            repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(repositoryModeMask, title); 
            repository.addPropertyChangeListener(this);
            panel = new RepositoryStepPanel();            
            panel.repositoryPanel.setLayout(new BorderLayout());
            panel.repositoryPanel.add(repository.getPanel());
            valid();
        }                        
        return panel;
    }

    protected void validateBeforeNext() {            
        try {
            support = new RepositoryStepProgressSupport(panel.progressPanel);        
            SVNUrl url = getUrl();
            support.setRepositoryRoot(url);            
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
            task.waitFinished();
        } finally {
            support = null;
        }
    }
    
    public void prepareValidation() {                
    }

    private SVNUrl getUrl() {        
        try {
            return getSelectedRepositoryConnection().getSvnUrl();                
        } catch (MalformedURLException mue) {                            
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue); // should not happen
        }                                
        return null;
    }
    
    private void storeHistory() {        
        RepositoryConnection rc = getSelectedRepositoryConnection();
        if(rc != null) {  
            SvnModuleConfig.getDefault().insertRecentUrl(rc);           
        }        
    }
    
    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }               
    
    private RepositoryConnection getSelectedRepositoryConnection() {      
        try {
            return repository.getSelectedRC();
        } catch (Exception ex) {
            invalid(ex.getLocalizedMessage()); 
            return null;
        }
    }                       

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(repository.getMessage());
            } else {
                invalid(repository.getMessage());
            }
        }
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }
    
    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {

        public RepositoryStepProgressSupport(JPanel panel) {
            super(panel);
        }

        public void perform() {
            final RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc == null) {
                return;
            }
            String invalidMsg = null;
            try {
                invalid(null);                

                SvnClient client;
                SVNUrl url = rc.getSvnUrl();
                try {
                    int handledExceptions = (SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS) ^ // the default without
                                            (SvnClientExceptionHandler.EX_NO_HOST_CONNECTION |        // host connection errors (misspeled host or proxy urls, ...)
                                             SvnClientExceptionHandler.EX_AUTHENTICATION) ;           // authentication errors 
                    client = Subversion.getInstance().getClient(url, rc.getUsername(), rc.getPassword(), handledExceptions);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                    invalidMsg = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()); 
                    return;
                }
                    
                repositoryFile = null; // reset
                ISVNInfo info = null;    
                try {
                    repository.storeConfigValues();                     
                    info = client.getInfo(url);
                } catch (SVNClientException ex) {
                    annotate(ex);
                    invalidMsg = SvnClientExceptionHandler.parseExceptionMessage(ex);
                } 
                if(isCanceled()) {
                    return;
                }

                if(info != null) {
                    // XXX convert to repositoryConnection                    

                    SVNUrl repositoryUrl = info.getRepository();
                    if(repositoryUrl==null) {
                        // XXX see issue #72810 and #72921. workaround!
                        repositoryUrl = rc.getSvnUrl();
                    }
                    SVNRevision revision = rc.getSvnRevision();
                    String[] repositorySegments = repositoryUrl.getPathSegments();
                    String[] selectedSegments = rc.getSvnUrl().getPathSegments();
                    String[] repositoryFolder = new String[selectedSegments.length - repositorySegments.length];
                    System.arraycopy(selectedSegments, repositorySegments.length,
                                     repositoryFolder, 0,
                                     repositoryFolder.length);

                    repositoryFile = new RepositoryFile(repositoryUrl, repositoryFolder, revision);
                } else {
                    invalidMsg = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()); // NOI18N
                    return;
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
            } finally {
                if(isCanceled()) {
                    valid(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Canceled")); // NOI18N
                } else if(invalidMsg == null) {
                  valid();
                  storeHistory();
                } else {
                  valid(invalidMsg);
                }                
            }
        }

        public void setEditable(boolean editable) {
            repository.setEditable(editable);        
        }        
    };

}

