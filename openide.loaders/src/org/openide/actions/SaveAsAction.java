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
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to save document under a different file name and/or extension.
 * The action is enabled for editor windows only.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
final class SaveAsAction extends AbstractAction implements ContextAwareAction, LookupListener, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    private boolean isEditorWindowActivated;
    
    private SaveAsAction() {
        this( Utilities.actionsGlobalContext() );
        TopComponent.getRegistry().addPropertyChangeListener( this );
    }
    
    private SaveAsAction( Lookup context ) {
        super( NbBundle.getMessage(DataObject.class, "CTL_SaveAsAction") ); //NOI18N
        this.context = context;
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    /**
     * Method is called from XML layers to create action instance for the main menu/toolbar.
     * @return Global instance for menu/toolbar
     */
    public static ContextAwareAction create() {
        return new SaveAsAction();
    }

    void init() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }

        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
        lkpInfo = context.lookup (tpl);
        lkpInfo.addLookupListener(this);
        propertyChange(null);
    }

    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        init();
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
    
    public void resultChanged(LookupEvent ev) {
        setEnabled (null != lkpInfo && lkpInfo.allItems().size() != 0 && isEditorWindowActivated );
    }
    
    /**
     * Show file 'save as' dialog window to ask user for a new file name.
     * @return File selected by the user or null if no file was selected.
     */
    private File getNewFileName() {
        File newFile = null;
        FileObject currentFileObject = getCurrentFileObject();
        if( null != currentFileObject )
            newFile = FileUtil.toFile( currentFileObject );

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
        if( null == newFile || newFile.equals( origFile ) )
            return null;

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
        return new SaveAsAction( actionContext );
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        
        isEditorWindowActivated = null != tc && WindowManager.getDefault().isEditorTopComponent( tc );
        
        resultChanged( null );
    }
}

