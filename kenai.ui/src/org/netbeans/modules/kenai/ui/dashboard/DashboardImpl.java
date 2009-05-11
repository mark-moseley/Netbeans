/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.LoginAction;
import org.netbeans.modules.kenai.ui.LoginHandleImpl;
import org.netbeans.modules.kenai.ui.treelist.TreeList;
import org.netbeans.modules.kenai.ui.treelist.TreeListModel;
import org.netbeans.modules.kenai.ui.treelist.TreeListNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public final class DashboardImpl extends Dashboard {

    private static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    private static final String PREF_COUNT = "count"; //NOI18N
    private static final String PREF_ID = "id"; //NOI18N
    private static final String PREF_IGNORED_PROJECTS = "ignoredProjects"; // NOI18N
    private LoginHandle login;
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        public int getSize() {
            return 0;
        }
        public Object getElementAt(int index) {
            return null;
        }
    };
    private RequestProcessor requestProcessor = new RequestProcessor("Kenai Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    private final ArrayList<ProjectHandle> memberProjects = new ArrayList<ProjectHandle>(50);
    private final ArrayList<ProjectHandle> allProjects = new ArrayList<ProjectHandle>(50);
    private final Set<String> ignoredProjectIds = new HashSet<String>(50);
    private final JScrollPane dashboardComponent;
    private final PropertyChangeListener userListener;
    private boolean opened = false;
    private boolean memberProjectsLoaded = false;
    private boolean otherProjectsLoaded = false;
    private boolean ignoredProjectsLoaded = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = TreeListNode.TIMEOUT_INTERVAL_MILLIS;

    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;

    private final UserNode userNode;
    private final ErrorNode memberProjectsError;
    private final ErrorNode otherProjectsError;

    private final Object LOCK = new Object();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private DashboardImpl() {
        dashboardComponent = new JScrollPane();
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        userListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects();
                }
            }
        };
        final Kenai kenai = Kenai.getDefault();
        final PasswordAuthentication pa = kenai.getPasswordAuthentication();
        this.login = pa==null ? null : new LoginHandleImpl(pa.getUserName());
        userNode = new UserNode(this);
        userNode.set(login, false);
        model.addRoot(-1, userNode);

        memberProjectsError = new ErrorNode(NbBundle.getMessage(DashboardImpl.class, "ERR_OpenMemberProjects"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearError(memberProjectsError);
                refreshMemberProjects();
            }
        });

        otherProjectsError = new ErrorNode(NbBundle.getMessage(DashboardImpl.class, "ERR_OpenProjects"), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearError(otherProjectsError);
                refreshProjects();
            }
        });

        kenai.addPropertyChangeListener(Kenai.PROP_LOGIN, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                final PasswordAuthentication newValue = (PasswordAuthentication) pce.getNewValue();
                if (newValue==null) {
                    setUser(null);
                } else {
                    setUser(new LoginHandleImpl(newValue.getUserName()));
                }
            }
        });
    }

    public static DashboardImpl getInstance() {
        return Holder.theInstance;
    }

    @Override
    public ProjectHandle[] getOpenProjects() {
        ProjectHandle[] open = new ProjectHandle[model.getSize()];
        int i=0;
        for (TreeListNode n:model.getRootNodes()) {
            if (n instanceof ProjectNode) {
                open[i++]=(((ProjectNode) n).getProject());
            }
        }
        return open;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private static class Holder {
        private static final DashboardImpl theInstance = new DashboardImpl();
    }

    public void selectAndExpand(KenaiProject project) {
        for (TreeListNode n:model.getRootNodes()) {
            if (n instanceof ProjectNode) {
                if (((ProjectNode)n).getProject().getId().equals(project.getName())) {
                    treeList.setSelectedValue(n, true);
                    n.setExpanded(true);
                }
            }
        }
    }

    /**
     * Display given Kenai user in the Dashboard window, the UI will start querying for
     * user's member projects.
     * Typically should be called after successful login.
     * @param login User login details.
     */
    public void setUser( final LoginHandle login ) {
        synchronized( LOCK ) {
            if( null != this.login ) {
                this.login.removePropertyChangeListener(userListener);
            }
            this.login = login;
            
            if (login==null) {
                //remove private project from dashboard
                //private projects are visible only for
                //authenticated user who is member of this project
                Iterator<ProjectHandle> ph = allProjects.iterator();
                while (ph.hasNext()) {
                    final ProjectHandle next = ph.next();
                    if (next.isPrivate()) {
                        removeProjectsFromModel(Collections.singletonList(next));
                        ph.remove();
                    }
                    storeAllProjects();
                }
            }
            memberProjects.clear();
            memberProjectsLoaded = false;
            userNode.set(login, !allProjects.isEmpty());
            if( isOpened() ) {
                if( null != login ) {
                    startLoadingMemberProjects(false);
                }
                switchContent();
            }
            if( null != this.login ) {
                this.login.addPropertyChangeListener(userListener);
            }
        }
    }

    /**
     * Add a Kenai project to the Dashboard.
     * @param project
     * @param isMemberProject
     */
    public void addProject( ProjectHandle project, boolean isMemberProject ) {
        synchronized( LOCK ) {
            if( allProjects.contains(project) )
                return;

            ignoredProjectIds.remove(project.getId());
            if( isMemberProject && memberProjectsLoaded && !memberProjects.contains(project) ) {
                memberProjects.add(project);
            }
            storeIgnoredProjects();
            allProjects.add(project);
            storeAllProjects();
            ArrayList<ProjectHandle> tmp = new ArrayList<ProjectHandle>(1);
            tmp.add(project);
            addProjectsToModel(-1, tmp);
            userNode.set(login, !allProjects.isEmpty());
            switchMemberProjects();
            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    public void removeProject( ProjectHandle project ) {
        synchronized( LOCK ) {
            if( !allProjects.contains(project) ) {
                return;
            }
            allProjects.remove(project);

            ignoredProjectIds.add(project.getId());
            storeIgnoredProjects();

            storeAllProjects();
            ArrayList<ProjectHandle> tmp = new ArrayList<ProjectHandle>(1);
            tmp.add(project);
            removeProjectsFromModel(tmp);
            if( isOpened() ) {
                switchContent();
            }
        }
        project.firePropertyChange(ProjectHandle.PROP_CLOSE, null, null);
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    ActionListener createLoginAction() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginAction.getDefault().actionPerformed(e);
            }
        };
    }

    private ActionListener createWhatIsKenaiAction() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URLDisplayer.getDefault().showURL(
                            new URL(NbBundle.getMessage(DashboardImpl.class, "URL_KenaiOverview"))); //NOI18N
                } catch( MalformedURLException ex ) {
                    //shouldn't happen
                    Exceptions.printStackTrace(ex);
                }
            }
        };
    }

    boolean isOpened() {
        return opened;
    }

    void refreshProjects() {
        synchronized( LOCK ) {
            memberProjects.clear();
            memberProjectsLoaded = false;
            removeProjectsFromModel(allProjects);
            allProjects.clear();
            otherProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingAllProjects(true);
                startLoadingMemberProjects(true);
            }
        }
    }

    private void refreshMemberProjects() {
        synchronized( LOCK ) {
            memberProjects.clear();
            memberProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingMemberProjects(true);
            }
        }
    }

    public void close() {
        synchronized( LOCK ) {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
            opened = false;
        }
    }

    public JComponent getComponent() {
        synchronized( LOCK ) {
            opened = true;
            if( !ignoredProjectsLoaded ) {
                loadIgnoredProjects();
            }

            requestProcessor.post(new Runnable() {
                public void run() {
                    UIUtils.waitStartupFinished();
                    if (null != login && !memberProjectsLoaded) {
                        startLoadingMemberProjects(false);
                    }
                    if (!otherProjectsLoaded) {
                        startLoadingAllProjects(false);
                    }
                }
            });
            switchContent();
        }
        return dashboardComponent;
    }

    private void fillModel() {
        synchronized( LOCK ) {
            if( !model.getRootNodes().contains(userNode) ) {
                model.addRoot(0, userNode);
            }
            if( model.getSize() > 1 )
                return;
            addProjectsToModel(-1, allProjects);
        }
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            public void run() {
                boolean isEmpty = true;

                synchronized( LOCK ) {
                    isEmpty = null == DashboardImpl.this.login && allProjects.isEmpty();
                }

                boolean isTreeListShowing = dashboardComponent.getViewport().getView() == treeList;
                if( isEmpty ) {
                    if( isTreeListShowing || dashboardComponent.getViewport().getView() == null ) {
                        dashboardComponent.setViewportView(createEmptyContent());
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                    }
                } else {
                    fillModel();
                    treeList.setModel(model);
                    switchMemberProjects();
                    if( !isTreeListShowing ) {
                        dashboardComponent.setViewportView(treeList);
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                    }
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private JComponent createEmptyContent() {
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque(false);

        JLabel lbl = new JLabel(NbBundle.getMessage(DashboardImpl.class, "LBL_No_Kenai_Project_Open")); //NOI18N
        lbl.setForeground(ColorManager.getDefault().getDisabledColor());
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(NbBundle.getMessage(DashboardImpl.class, "LBL_WhatIsKenai"), createWhatIsKenaiAction() ); //NOI18N

        model.removeRoot(userNode);
        userNode.set(null, false);
        res.add( userNode.getComponent(UIManager.getColor("List.foreground"), ColorManager.getDefault().getDefaultBackground(), false, false), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }

    private void startLoadingAllProjects(boolean forceRefresh) {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_ALL_PROJECTS); //NOI18N
        int count = prefs.getInt(PREF_COUNT, 0); //NOI18N
        if( 0 == count )
            return; //nothing to load
        ArrayList<String> ids = new ArrayList<String>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get(PREF_ID+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ids.add( id.trim() );
            }
        }
        synchronized( LOCK ) {
            if( otherProjectsLoader != null )
                otherProjectsLoader.cancel();
            if( ids.isEmpty() )
                return;
            otherProjectsLoader = new OtherProjectsLoader(ids, forceRefresh);
            requestProcessor.post(otherProjectsLoader);
        }
    }

    private void loadIgnoredProjects() {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_IGNORED_PROJECTS); //NOI18N
        ignoredProjectIds.clear();
        int count = prefs.getInt(PREF_COUNT, 0); //NOI18N

        for( int i=0; i<count; i++ ) {
            String id = prefs.get(PREF_ID+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ignoredProjectIds.add( id.trim() );
            }
        }
        ignoredProjectsLoaded = true;
    }

    private void storeIgnoredProjects() {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_IGNORED_PROJECTS); //NOI18N
        prefs.putInt(PREF_COUNT, ignoredProjectIds.size()); //NOI18N
        int index = 0;
        for( String id : ignoredProjectIds ) {
            prefs.put(PREF_ID+index++, id); //NOI18N
        }
    }

    private void storeAllProjects() {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_ALL_PROJECTS); //NOI18N
        int index = 0;
        for( ProjectHandle project : allProjects ) {
            //do not store private projects
            if (!project.isPrivate()) {
                prefs.put(PREF_ID+index++, project.getId()); //NOI18N
            }
        }
        //store size
        prefs.putInt(PREF_COUNT, index); //NOI18N
    }

    private void setOtherProjects(ArrayList<ProjectHandle> projects) {
        synchronized( LOCK ) {
            removeProjectsFromModel( allProjects );
            for( ProjectHandle p : projects ) {
                if( ignoredProjectIds.contains(p.getId()) )
                    continue;
                if( !allProjects.contains( p ) )
                    allProjects.add( p );
            }
            Collections.sort(allProjects);
            otherProjectsLoaded = true;
            addProjectsToModel( -1, allProjects );
            userNode.set(login, !allProjects.isEmpty());
            storeAllProjects();

            switchMemberProjects();
            
            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    private void switchMemberProjects() {
        for( TreeListNode n : model.getRootNodes() ) {
            if( !(n instanceof ProjectNode) )
                continue;
            ProjectNode pn = (ProjectNode) n;
            pn.setMemberProject( memberProjects.contains( pn.getProject() ) );
        }
    }

    private void loadingStarted() {
        userNode.loadingStarted();
    }

    private void loadingFinished() {
        userNode.loadingFinished();
    }

    private void startLoadingMemberProjects(boolean forceRefresh) {
        synchronized( LOCK ) {
            if( memberProjectsLoader != null )
                memberProjectsLoader.cancel();
            if( null == login )
                return;
            memberProjectsLoader = new MemberProjectsLoader(login, forceRefresh);
            requestProcessor.post(memberProjectsLoader);
        }
    }

    private void setMemberProjects(ArrayList<ProjectHandle> projects) {
        synchronized( LOCK ) {
            if( projects.isEmpty() ) {
                if( isOpened() ) {
                    switchContent();
                }
            }

            removeProjectsFromModel( allProjects );
            memberProjects.clear();
            memberProjects.addAll(projects);
            memberProjectsLoaded = true;
            for( ProjectHandle p : projects ) {
                if( ignoredProjectIds.contains(p.getId()) )
                    continue;
                if( !allProjects.contains(p) )
                    allProjects.add(p);
            }
            Collections.sort(allProjects);
            storeAllProjects();
            addProjectsToModel( -1, allProjects );
            userNode.set(login, !allProjects.isEmpty());

            switchMemberProjects();

            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    private void addProjectsToModel( int index, List<ProjectHandle> projects ) {
        for( ProjectHandle p : projects ) {
            model.addRoot( index, new ProjectNode(p) );
        }
    }

    private void removeProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>(projects.size());
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof ProjectNode ) {
                ProjectNode pn = (ProjectNode) root;

                if( projects.contains( pn.getProject() ) ) {
                    nodesToRemove.add(root);
                }
            }
        }
        for( TreeListNode node : nodesToRemove ) {
            model.removeRoot(node);
        }
    }

    private void showError( ErrorNode node ) {
        synchronized( LOCK ) {
            List<TreeListNode> roots = model.getRootNodes();
            if( !roots.contains(node) ) {
                model.addRoot(1, node);
            }
        }
    }

    private void clearError( ErrorNode node ) {
        synchronized( LOCK ) {
            model.removeRoot(node);
        }
    }

    private class OtherProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final ArrayList<String> projectIds;
        private boolean forceRefresh;

        public OtherProjectsLoader( ArrayList<String> projectIds, boolean forceRefresh ) {
            this.projectIds = projectIds;
            this.forceRefresh = forceRefresh;
        }

        public void run() {
            loadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ArrayList<ProjectHandle> projects = new ArrayList<ProjectHandle>(projectIds.size());
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    for( String id : projectIds ) {
                        ProjectHandle handle = accessor.getNonMemberProject(id, forceRefresh);
                        if (handle!=null) {
                            projects.add(handle);
                        } else {
                            projects=null;
                        }
                    }
                    res[0] = projects;
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            loadingFinished();
            if( cancelled )
                return;
            if( null == res[0] ) {
                showError( otherProjectsError );
                return;
            }

            setOtherProjects( res[0] );
            clearError( otherProjectsError );
        }

        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }

    }

    private class MemberProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final LoginHandle user;
        private boolean forceRefresh;

        public MemberProjectsLoader( LoginHandle login, boolean forceRefresh ) {
            this.user = login;
            this.forceRefresh = forceRefresh;
        }

        public void run() {
            loadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    res[0] = new ArrayList( accessor.getMemberProjects(user, forceRefresh) );
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            loadingFinished();
            if( cancelled )
                return;
            if( null == res[0] ) {
                showError( memberProjectsError );
                return;
            }

            setMemberProjects( res[0] );
            clearError( memberProjectsError );
        }

        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }
    }
}
