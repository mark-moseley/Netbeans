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

package org.netbeans.modules.cnd.modelui.switcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
/**
 *
 * @author Vladimir Kvashin
 */
public class SwitchProjectAction extends NodeAction {
    
    private JCheckBoxMenuItem presenter;
    private ModelImpl model;
    private static boolean running = false;
    
    private enum State {
        Enabled, Disabled, Indeterminate
    }
    
    public SwitchProjectAction() {
        presenter = new JCheckBoxMenuItem(getName());
        presenter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onActionPerformed();
            }
        });
        CsmModel model = CsmModelAccessor.getModel();
        if( model instanceof ModelImpl ) {
            this.model = (ModelImpl) model;
        }
    }
    
    public String getName() {
	return NbBundle.getMessage(getClass(), ("CTL_SwitchProjectAction")); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }    
    
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }
    
    private JMenuItem getPresenter() {
        final Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null ) {
            presenter.setEnabled(!running);
            presenter.setSelected(false);
        }
        else {
	    try {
		State state = getState(projects);
		if( state == State.Indeterminate ) {
		    presenter.setEnabled(!running);
		    presenter.setSelected(false);
		}
		else {
		    presenter.setEnabled(!running);
		    presenter.setSelected(state == State.Enabled);
		}
	    }
	    catch( Throwable thr ) { 
		// we are in awt thread;
		// if exception occurs here, it doesn't allow even to close the project!
		thr.printStackTrace();
		presenter.setEnabled(false);
		presenter.setSelected(true);
	    }
        }
        return presenter;
    }

    /** 
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    private Collection<NativeProject> getNativeProjects(Node[] nodes) {
        Collection<NativeProject> projects = new ArrayList<NativeProject>();
        for (int i = 0; i < nodes.length; i++) {
            Object o = nodes[i].getValue("Project"); // NOI18N 
            if( ! (o instanceof  Project) ) {
                return null;
            }
            NativeProject nativeProject = ((Project) o).getLookup().lookup(NativeProject.class);
            if( nativeProject == null ) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }
    
    private State getState(Collection<NativeProject> projects) {
        if( model == null ) {
            return State.Indeterminate;
        }
        State state = State.Indeterminate;
        for( NativeProject p : projects ) {
            State curr = getState(p);
            if( state == State.Indeterminate ) {
                state = curr;
            }
            else {
                if( state != curr ) {
                    return State.Indeterminate;
                }
            }
        }
        return state;
    }
    
    private State getState(NativeProject p) {
        return model.isProjectEnabled(p) ? State.Enabled : State.Disabled;
    }
    
    protected boolean enable(Node[] activatedNodes)  {
        if( model == null ) {
            return false;
        }
	if( running ) {
	    return false;
	}
        Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null) {
            return false;
        }
        return getState(projects) != State.Indeterminate;
    }

    private void onActionPerformed() {
        performAction(getActivatedNodes());
    }
    
    /** Actually nobody but us call this since we have a presenter. */
    public void performAction(final Node[] activatedNodes) {
	running = true;
	model.enqueue(new Runnable() {
	    public void run() {
                try {
                    performAction(getNativeProjects(getActivatedNodes()));
                } finally {
                    running = false;
                }
	    }
	}, "Switching code model ON/OFF"); //NOI18N
    }
    
    private void performAction(Collection<NativeProject> projects) {
        if( projects != null ) {
            State state = getState(projects);
            switch( state ) {
                case Enabled:
                    for( NativeProject p : projects ) {
                        model.disableProject(p);
                    }
                    break;
                case Disabled:
                    for( NativeProject p : projects ) {
                        model.enableProject(p);
                    }
                    break;
            }
        }
    }
    
    protected boolean asynchronous () {
        return false;
    }
}
