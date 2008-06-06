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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor.ProjectItemChangeEvent;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Customization of Make project
 */
public class MakeCustomizerProvider implements CustomizerProvider {
    
    private final Project project; 
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = 1;
    private static final int OPTION_APPLY = 2;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    private static final String COMMAND_APPLY = "APPLY";  // NOI18N
    
    private DialogDescriptor dialogDescriptor;
    private Map customizerPerProject = new WeakHashMap (); // Is is weak needed here?
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private Set<ActionListener> actionListenerList = new HashSet<ActionListener>();
    
    public MakeCustomizerProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }
            
    public void showCustomizer() {
        showCustomizer(null, null, null);
    }

    public void showCustomizer(Item item) {
        showCustomizer(null, item, null);
    }
    
    public void showCustomizer(Folder folder) {
        showCustomizer(null, null, folder);
    }

    public void showCustomizer(String preselectedNodeName) {
        showCustomizer(preselectedNodeName, null, null);
    }
    
    public void showCustomizer(final String preselectedNodeName, final Item item, final Folder folder) {
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                showCustomizerWorker(preselectedNodeName, item, folder);
            }
        });     
    }
    
    private void showCustomizerWorker(String preselectedNodeName, Item item, Folder folder) {
        
        if (customizerPerProject.containsKey (project)) {
            Dialog dlg = (Dialog)customizerPerProject.get (project);
            
            // check if the project is being customized
            if (dlg.isShowing ()) {
                // make it showed
                dlg.setVisible(true);
                return ;
            }
        }
        
        if (folder != null) {
            // Make sure all FolderConfigurations are created (they are lazyly created)
            Configuration[] configurations = projectDescriptorProvider.getConfigurationDescriptor().getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++) {
                folder.getFolderConfiguration(configurations[i]);
            }
        }
        
        // Make sure all languages are update
        ((MakeConfigurationDescriptor)projectDescriptorProvider.getConfigurationDescriptor()).refreshRequiredLanguages();

        // Create options
        JButton options[] = new JButton[] { 
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Apply_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_OK ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        options[ OPTION_CANCEL ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Cancel_Option") ); // NOI18N
        options[ OPTION_APPLY ].setActionCommand( COMMAND_APPLY );
        options[ OPTION_APPLY ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Apply_Option") ); // NOI18N

        //A11Y
        options[ OPTION_OK].getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderOk")); // NOI18N
        options[ OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderCancel")); // NOI18N
        options[ OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderApply")); // NOI18N

	// Mnemonics
        options[ OPTION_APPLY ].setMnemonic(NbBundle.getMessage(MakeCustomizerProvider.class, "MNE_Customizer_Apply_Option").charAt(0)); // NOI18N

        // RegisterListener
	ConfigurationDescriptor clonedProjectdescriptor = projectDescriptorProvider.getConfigurationDescriptor().cloneProjectDescriptor();
	Vector controls = new Vector();
	controls.add(options[OPTION_OK]);
        MakeCustomizer innerPane = new MakeCustomizer(project, preselectedNodeName, clonedProjectdescriptor, item, folder, controls);
        ActionListener optionsListener = new OptionListener( project, projectDescriptorProvider.getConfigurationDescriptor(), clonedProjectdescriptor, innerPane, folder, item);
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        options[ OPTION_APPLY ].addActionListener( optionsListener );
        
        String dialogTitle = null;
        if (item != null) {
            dialogTitle = MessageFormat.format(
                    NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_File_Customizer_Title"),
                    new Object[] {item.getFile().getName()}); // NOI18N 
        }
        else if (folder != null) {
            dialogTitle = MessageFormat.format(
                    NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Folder_Customizer_Title"),
                    new Object[] {folder.getName()}); // NOI18N 
        }
        else {
            dialogTitle = MessageFormat.format(
                    NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Project_Customizer_Title"),
                    new Object[] {ProjectUtils.getInformation(project).getDisplayName()}); // NOI18N 
        }

        dialogDescriptor = new DialogDescriptor( 
            innerPane, // innerPane
            dialogTitle,
            true,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null );                                 // listener 
            
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );
        innerPane.setDialogDescriptor(dialogDescriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        customizerPerProject.put (project, dialog);
        dialog.setVisible(true);
        
    }    
    

    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener implements ActionListener {
    
        private Project project;
	private ConfigurationDescriptor projectDescriptor;
	private ConfigurationDescriptor clonedProjectdescriptor;
	private MakeCustomizer makeCustomizer;
        private Folder folder;
        private Item item;
        
        OptionListener( Project project, ConfigurationDescriptor projectDescriptor, ConfigurationDescriptor clonedProjectdescriptor, MakeCustomizer makeCustomizer, Folder folder, Item item) {
            this.project = project;
	    this.projectDescriptor = projectDescriptor;
	    this.clonedProjectdescriptor = clonedProjectdescriptor;
	    this.makeCustomizer = makeCustomizer;
            this.folder = folder;
            this.item = item;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if (command.equals(COMMAND_OK) || command.equals(COMMAND_APPLY)) {
                int previousVersion = projectDescriptor.getVersion();
                int currentVersion = CommonConfigurationXMLCodec.CURRENT_VERSION;
                if (previousVersion < currentVersion) {                                           
                    String txt = getString("UPGRADE_TXT");
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, getString("UPGRADE_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION); // NOI18N
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    projectDescriptor.setVersion(currentVersion);
                }
                
		//projectDescriptor.copyFromProjectDescriptor(clonedProjectdescriptor);
		projectDescriptor.assign(clonedProjectdescriptor);
		projectDescriptor.setModified();
                projectDescriptor.save(); // IZ 133606
                ((MakeConfigurationDescriptor)projectDescriptor).checkForChangedItems(project, folder, item);

		((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();// FIXUP: should be moved into ProjectDescriptorHelper...
                
                fireActionEvent(e);
                
            }
            if (command.equals(COMMAND_APPLY)) {
		makeCustomizer.refresh();
	    }
            if (command.equals(COMMAND_OK) || command.equals(COMMAND_CANCEL))
                actionListenerList.clear();
        }        
    }
    
    public void addActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.add(cl);
        }
    }
    
    public void removeActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.remove(cl);
        }
    }
    
    public void fireActionEvent(ActionEvent e) {
        Iterator it;
        
        synchronized (actionListenerList) {
            it = new HashSet(actionListenerList).iterator();
        }
        while (it.hasNext()) {
            ((ActionListener)it.next()).actionPerformed(e);
        }
    }
    
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakeCustomizerProvider.class);
        }
        return bundle.getString(s);
    }
}
