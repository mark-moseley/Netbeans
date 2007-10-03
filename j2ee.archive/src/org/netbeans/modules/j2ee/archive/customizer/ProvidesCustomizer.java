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

package org.netbeans.modules.j2ee.archive.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import org.netbeans.modules.j2ee.archive.project.ArchiveProject;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;


/** Customization of J2EE/Java EE archive project
 *
 * @author vince kraemer
 */
public class ProvidesCustomizer implements CustomizerProvider {
    
    private final Project project;
    private final AntProjectHelper antProjectHelper;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    public ProvidesCustomizer(Project project, AntProjectHelper antProjectHelper) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }
    
    public void showCustomizer() {
        Dialog dialog = createDialog();
        dialog.setVisible(true);
    }
    
    Dialog createDialog() {
        // Create options
        JButton options[] = new JButton[] {
            new JButton( NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };
        
        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        
        // RegisterListener
        ArchiveProjectProperties apProperties = new ArchiveProjectProperties( (ArchiveProject)project, antProjectHelper );
        ActionListener optionsListener = new OptionListener( project, apProperties );
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        
        
        ArchiveProjectCustomizer innerPane = new ArchiveProjectCustomizer(apProperties); // , pwm); // , preselectedNodeName);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                innerPane, // new EarCustomizer(apProperties, pwm), // innerPane
                NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Title" , // displayName
                ProjectUtils.getInformation(project).getDisplayName() ),
                false,                                  // modal
                options,                                // options
                options[OPTION_OK],                     // initial value
                DialogDescriptor.BOTTOM_ALIGN,          // options align
                null,                                   // helpCtx
                null );                                 // listener
        
        innerPane.setDialogDescriptor(dialogDescriptor);
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        return dialog; // dialog.show();
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
        
        private Project project;
        private ArchiveProjectProperties apProperties;
        
        OptionListener( Project project, ArchiveProjectProperties apProperties ) {
            this.project = project;
            this.apProperties = apProperties;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if (COMMAND_OK.equals(command)) {
                // Store the properties
                apProperties.save();
                
                // XXX Maybe move into WebProjectProperties
                // And save the project
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch ( IOException ex ) {
                    ErrorManager.getDefault().notify( ex );
                }
            }
            
        }
        
    }
    
}
