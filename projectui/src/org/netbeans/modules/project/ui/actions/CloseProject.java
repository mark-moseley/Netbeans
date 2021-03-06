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

package org.netbeans.modules.project.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/** Action for removing project from the open projects tab
 */
public class CloseProject extends ProjectAction implements PropertyChangeListener, Presenter.Popup {
    
    private static final String namePattern = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Popup_Name" ); // NOI18N
    
    private String popupName;
    
    private PropertyChangeListener wpcl;
   
    /** Creates a new instance of BrowserAction */
    public CloseProject() {
        this( null );        
    }
    
    public CloseProject( Lookup context ) {
        super( (String)null, namePattern, null, context );        
        wpcl = WeakListeners.propertyChange( this, OpenProjectList.getDefault() );
        OpenProjectList.getDefault().addPropertyChangeListener( wpcl );
        refresh( getLookup() );
    }
        
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        // show all modified documents, if an user cancel it then no project is closed        
        OpenProjectList.getDefault().close( projects, true );
    }
    
    public void refresh( Lookup context ) {
        
        super.refresh( context );
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        // XXX make it work better for mutliple open projects
        if ( projects.length == 0 || !OpenProjectList.getDefault().isOpen( projects[0] ) ) {
            setEnabled( false );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, new Project[0] ) );
            popupName = ActionsUtil.formatProjectSensitiveName( namePatternPopup, new Project[0] );
        }
        else {
            setEnabled( true );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, projects ) );
            popupName = ActionsUtil.formatProjectSensitiveName( namePatternPopup, projects );
        }        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CloseProject( actionContext );
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        refresh( getLookup() );
    }
    
    // Implementation of Presenter.Popup ---------------------------------------
    
    public JMenuItem getPopupPresenter() {
        JMenuItem popupPresenter = new JMenuItem( this );

        popupPresenter.setIcon( null );
        popupPresenter.setText( popupName );
        
        return popupPresenter;
    }
    
}
