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
package org.openide.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to save document under a different file name and/or extension.
 * The action is enabled for editor windows only.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
final class SaveAsAction extends AbstractAction implements ContextAwareAction {

    private Lookup context;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    private boolean isGlobal = false;
    private boolean isDirty = true;
    private PropertyChangeListener registryListener;
    private LookupListener lookupListener;

    private SaveAsAction() {
        this( Utilities.actionsGlobalContext(), true );
    }
    
    private SaveAsAction( Lookup context, boolean isGlobal ) {
        super( NbBundle.getMessage(DataObject.class, "CTL_SaveAsAction") ); //NOI18N
        this.context = context;
        this.isGlobal = isGlobal;
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        setEnabled( false );
    }
    
    /**
     * Method is called from XML layers to create action instance for the main menu/toolbar.
     * @return Global instance for menu/toolbar
     */
    public static ContextAwareAction create() {
        return new SaveAsAction();
    }
    
    @Override
    public boolean isEnabled() {
        if (isDirty
            || null == changeSupport || !changeSupport.hasListeners("enabled") ) { //NOI18N
            refreshEnabled();
        }
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        refreshListeners();
        Collection<? extends SaveAsCapable> inst = lkpInfo.allInstances();
        if( inst.size() > 0 ) {
            SaveAsCapable saveAs = inst.iterator().next();
            File newFile = getNewFileName();
            if( null != newFile ) {
                //create target folder if necessary    
                FileObject newFolder = null;
                try {
                    File targetFolder = newFile.getParentFile();
                    if( null == targetFolder )
                        throw new IOException(newFile.getAbsolutePath());
                    newFolder = FileUtil.createFolder( targetFolder );
                } catch( IOException ioE ) {
                    NotifyDescriptor error = new NotifyDescriptor( 
                            NbBundle.getMessage(DataObject.class, "MSG_CannotCreateTargetFolder"), //NOI18N
                            NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle"), //NOI18N
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[] {NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION );
                    DialogDisplayer.getDefault().notify( error );
                    return;
                }
                
                try {
                    saveAs.saveAs( newFolder, newFile.getName() );
                } catch( IOException ioE ) {
                    Exceptions.attachLocalizedMessage( ioE, NbBundle.getMessage( DataObject.class, "MSG_SaveAsFailed" ) );  //NOI18N
                    Logger.getLogger( getClass().getName() ).log( Level.WARNING, null, ioE );
                }
            }
        }
    }
    
    /**
     * Show file 'save as' dialog window to ask user for a new file name.
     * @return File selected by the user or null if no file was selected.
     */
    private File getNewFileName() {
        File newFile = null;
        FileObject currentFileObject = getCurrentFileObject();
        if( null != currentFileObject ) {
            newFile = FileUtil.toFile( currentFileObject );
            if( null == newFile ) {
                newFile = new File( currentFileObject.getNameExt() );
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle" ) ); //NOI18N
        chooser.setMultiSelectionEnabled( false );
        if( null != newFile ) {
            chooser.setSelectedFile( newFile );
            FileUtil.preventFileChooserSymlinkTraversal( chooser, newFile.getParentFile() );
        }
        File origFile = newFile;
        if( JFileChooser.APPROVE_OPTION != chooser.showSaveDialog( WindowManager.getDefault().getMainWindow() ) ) {
            return null;
        }
        newFile = chooser.getSelectedFile();
        if( null == newFile )
            return null;
        if( newFile.equals( origFile ) ) {
            NotifyDescriptor nd = new NotifyDescriptor( 
                    NbBundle.getMessage( DataObject.class, "MSG_SaveAs_SameFileSelected"), //NOI18N
                    NbBundle.getMessage( DataObject.class, "MSG_SaveAs_SameFileSelected_Title"), //NOI18N
                    NotifyDescriptor.DEFAULT_OPTION, 
                    NotifyDescriptor.INFORMATION_MESSAGE, 
                    new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION );
            DialogDisplayer.getDefault().notifyLater( nd );
            return null;
        }

        return newFile;
    }
    
    private FileObject getCurrentFileObject() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null != tc ) {
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob )
                return dob.getPrimaryFile();
        }
        return null;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SaveAsAction( actionContext, false );
    }
    
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        refreshListeners();
    }
    
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        refreshListeners();
    }
    
    private PropertyChangeListener createRegistryListener() {
        return WeakListeners.propertyChange( new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    isDirty = true;
                }
            }, TopComponent.getRegistry() );
    }
    
    private LookupListener createLookupListener() {
        return WeakListeners.create(LookupListener.class, new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    isDirty = true;
                }
            }, lkpInfo);
    }

    private void refreshEnabled() {
        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
            lkpInfo = context.lookup (tpl);
        }
        
        TopComponent tc = TopComponent.getRegistry().getActivated();
        boolean isEditorWindowActivated = null != tc && WindowManager.getDefault().isEditorTopComponent(tc);
        setEnabled(null != lkpInfo && lkpInfo.allItems().size() != 0 && isEditorWindowActivated);
        isDirty = false;
    }
    
    private void refreshListeners() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread";

        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
            lkpInfo = context.lookup (tpl);
        }
        
        if( null == changeSupport || !changeSupport.hasListeners("enabled") ) { //NOI18N
            if( isGlobal && null != registryListener ) {
                TopComponent.getRegistry().removePropertyChangeListener( registryListener );
                registryListener = null;
            }
            if( null != lookupListener ) {
                lkpInfo.removeLookupListener(lookupListener);
                lookupListener = null;
            }
        } else {
            if( null == registryListener ) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener( registryListener );
            }

            if( null == lookupListener ) {
                lookupListener = createLookupListener();
                lkpInfo.addLookupListener( lookupListener );
            }
            refreshEnabled();
        }
    }
    
    //for unit testing
    boolean _isEnabled() {
        return super.isEnabled();
    }
}

