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

package org.netbeans.modules.tasklist.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JToolBar;
import org.netbeans.modules.tasklist.impl.ScanningScopeList;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.tasklist.filter.FilterRepository;
import org.netbeans.modules.tasklist.impl.ScannerList;
import org.netbeans.modules.tasklist.impl.ScanningScopeList;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class TaskListTopComponent extends TopComponent {
    
    private static TaskListTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/tasklist/ui/resources/taskList.png"; //NOI18N
    
    private static final String PREFERRED_ID = "TaskListTopComponent"; //NOI18N
    
    private TaskManagerImpl taskManager;
    private PropertyChangeListener scopeListListener;
    private PropertyChangeListener scannerListListener;
    private TaskListModel model;
    private FilterRepository filters;
    private TaskListTable table;
    private PropertyChangeListener changeListener;
    
    private TaskListTopComponent() {
        taskManager = TaskManagerImpl.getInstance();
        
        initComponents();
        setName(NbBundle.getMessage(TaskListTopComponent.class, "CTL_TaskListTopComponent")); //NOI18N
        setToolTipText(NbBundle.getMessage(TaskListTopComponent.class, "HINT_TaskListTopComponent")); //NOI18N
        setIcon(Utilities.loadImage(ICON_PATH, true));
        
        tableScroll.addMouseListener( new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                maybePopup( e );
            }
            
            @Override
            public void mouseReleased( MouseEvent e ) {
                maybePopup( e );
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolbar = new javax.swing.JToolBar();
        tableHolderPanel = new javax.swing.JPanel();
        tableScroll = new javax.swing.JScrollPane();
        toolbarSeparator = new javax.swing.JSeparator();
        statusSeparator = new javax.swing.JSeparator();
        statusBarPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        toolbar.setFloatable(false);
        toolbar.setOrientation(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(toolbar, gridBagConstraints);

        tableHolderPanel.setOpaque(false);

        tableScroll.setBorder(null);

        org.jdesktop.layout.GroupLayout tableHolderPanelLayout = new org.jdesktop.layout.GroupLayout(tableHolderPanel);
        tableHolderPanel.setLayout(tableHolderPanelLayout);
        tableHolderPanelLayout.setHorizontalGroup(
            tableHolderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
        );
        tableHolderPanelLayout.setVerticalGroup(
            tableHolderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableHolderPanel, gridBagConstraints);

        toolbarSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(toolbarSeparator, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(statusSeparator, gridBagConstraints);

        statusBarPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        add(statusBarPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JSeparator statusSeparator;
    private javax.swing.JPanel tableHolderPanel;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.JToolBar toolbar;
    private javax.swing.JSeparator toolbarSeparator;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TaskListTopComponent getDefault() {
        if (instance == null) {
            instance = new TaskListTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the TaskListTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TaskListTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            getLogger().log( Level.INFO, "Cannot find TaskList component. It will not be located properly in the window system." ); //NOI18N
            return getDefault();
        }
        if (win instanceof TaskListTopComponent) {
            return (TaskListTopComponent)win;
        }
        getLogger().log( Level.INFO,
                "There seem to be multiple components with the '" + PREFERRED_ID + //NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); //NOI18N
        return getDefault();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    public void componentOpened() {
        TaskScanningScope activeScope = Settings.getDefault().getActiveScanningScope();
        
        if( null == activeScope )
            activeScope = ScanningScopeList.getDefault().getDefaultScope();
        if( null == filters ) {
            try {
                filters = FilterRepository.getDefault();
                filters.load();
            } catch( IOException ioE ) {
                getLogger().log( Level.INFO, ioE.getMessage(), ioE );
            }
        }
        
        taskManager.addPropertyChangeListener( TaskManagerImpl.PROP_WORKING_STATUS, getChangeListener() );
        
        if( null == model ) {
            table = new TaskListTable();
            //later on the button in the toolbar will switch to the previously used table model
            model = new TaskListModel( taskManager.getTasks() );
            table.setModel( model );
            tableScroll.setViewportView( table );
            tableScroll.setBorder( BorderFactory.createEmptyBorder() );
            statusBarPanel.add( new StatusBar(taskManager.getTasks()), BorderLayout.CENTER );
        }
        ScanningScopeList.getDefault().addPropertyChangeListener( getScopeListListener() );
        ScannerList.getFileScannerList().addPropertyChangeListener( getScannerListListener() );
        ScannerList.getPushScannerList().addPropertyChangeListener( getScannerListListener() );
        
        rebuildToolbar();

        final TaskScanningScope scopeToObserve = activeScope;
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                taskManager.observe( scopeToObserve, filters.getActive() );
            }
        });
    }
    
    @Override
    public void componentClosed() {
        ScanningScopeList.getDefault().removePropertyChangeListener( getScopeListListener() );
        ScannerList.getFileScannerList().removePropertyChangeListener( getScannerListListener() );
        ScannerList.getPushScannerList().removePropertyChangeListener( getScannerListListener() );
        taskManager.observe( null, null );
        taskManager.removePropertyChangeListener( TaskManagerImpl.PROP_WORKING_STATUS, getChangeListener() );
        if( null != progress )
            progress.finish();
        progress = null;
        try {
            FilterRepository.getDefault().save();
        } catch( IOException ioE ) {
            getLogger().log( Level.INFO, null, ioE );
        }
    }
    
    @Override
    public void requestActive() {
        super.requestActive();
        table.requestFocusInWindow();
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    private void maybePopup( MouseEvent e ) {
        if( e.isPopupTrigger() ) {
            e.consume();
            JPopupMenu popup = Util.createPopup( table );
            popup.show( this, e.getX(), e.getY() );
        }
    }
    
    private PropertyChangeListener getScopeListListener() {
        if( null == scopeListListener ) {
            scopeListListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent arg0) {
                    ScanningScopeList scopeList = ScanningScopeList.getDefault();
                    List<TaskScanningScope> newScopes = scopeList.getTaskScanningScopes();
                    if( newScopes.isEmpty() ) {
                        getLogger().log( Level.INFO, "No task scanning scope found" ); //NOI18N
                    }
                    rebuildToolbar();
                }
            };
        }
        return scopeListListener;
    }
    
    private PropertyChangeListener getScannerListListener() {
        if( null == scannerListListener ) {
            scannerListListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            final TaskScanningScope scopeToObserve = taskManager.getScope();
                            taskManager.observe( null, null );
                            SwingUtilities.invokeLater( new Runnable() {
                                public void run() {
                                    taskManager.observe( scopeToObserve, filters.getActive() );
                                }
                            });
                        }
                    });
                }
            };
        }
        return scannerListListener;
    }
    
    private void rebuildToolbar() {
        toolbar.removeAll();
        toolbar.setFocusable( false );
        //scope buttons
        List<TaskScanningScope> scopes = ScanningScopeList.getDefault().getTaskScanningScopes();
        for( TaskScanningScope scope : scopes ) {
            toolbar.add( new ScopeButton( taskManager, scope ) );
        }
        toolbar.add( new JToolBar.Separator() );
        //filter
        JToggleButton toggleFilter = new FiltersMenuButton( filters.getActive() );
        toolbar.add( toggleFilter );
        //grouping & other butons
        toolbar.addSeparator();
        final JToggleButton toggleGroups = new JToggleButton( new ImageIcon(Utilities.loadImage( "org/netbeans/modules/tasklist/ui/resources/groups.png" )) ); //NOI18N
        toggleGroups.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                switchTableModel( e.getStateChange() == ItemEvent.SELECTED );
                Settings.getDefault().setGroupTasksByCategory( toggleGroups.isSelected() );
                toggleGroups.setToolTipText( toggleGroups.isSelected() 
                        ? NbBundle.getMessage( TaskListTopComponent.class, "HINT_TasksAsList" )  //NOI18N
                        : NbBundle.getMessage( TaskListTopComponent.class, "HINT_GrouppedTasks" ) ); //NOI18N
            }
        });
        toggleGroups.setSelected( Settings.getDefault().getGroupTasksByCategory() );
        toggleGroups.setToolTipText( toggleGroups.isSelected() 
                        ? NbBundle.getMessage( TaskListTopComponent.class, "HINT_TasksAsList" )  //NOI18N
                        : NbBundle.getMessage( TaskListTopComponent.class, "HINT_GrouppedTasks" ) ); //NOI18N
        toggleGroups.setFocusable( false );
        toolbar.add( toggleGroups );
    }
    
    private void switchTableModel( boolean useFoldingModel ) {
        if( useFoldingModel ) {
            model = new FoldingTaskListModel( taskManager.getTasks() );
            table.setModel( model );
            statusBarPanel.removeAll();
        } else {
            model = new TaskListModel( taskManager.getTasks() );
            table.setModel( model );
            statusBarPanel.add( new StatusBar(taskManager.getTasks()), BorderLayout.CENTER );
        }
        statusBarPanel.setVisible( !useFoldingModel );
        statusSeparator.setVisible( !useFoldingModel );
    }
    
    static private Logger getLogger() {
        return Logger.getLogger( TaskListTopComponent.class.getName() );
    }
    
    private ProgressHandle progress;
    private PropertyChangeListener getChangeListener() {
        if( null == changeListener ) {
            changeListener = new PropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent e ) {
                    synchronized( this ) {
                        if( ((Boolean)e.getNewValue()).booleanValue() ) {
                            if( null == progress ) {
                                progress = ProgressHandleFactory.createHandle( 
                                        NbBundle.getMessage( TaskListTopComponent.class, "LBL_ScanProgress" ), //NOI18N
                                        new Cancellable() { //NOI18N
                                            public boolean cancel() {
                                                taskManager.abort();
                                                return true;
                                            }
                                        });                            
                            }
                            progress.start();
                            progress.switchToIndeterminate();
                        } else {
                            if( null != progress )
                                progress.finish();
                            progress = null;
                        }
                    }
                }
            };
        }
        return changeListener;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return TaskListTopComponent.getDefault();
        }
    }
}
