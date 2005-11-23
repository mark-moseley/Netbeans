/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** Implementation of standard customizer dialog.
 *
 * @author Petr Hrebejk
 */
public class CustomizerDialog {
    
    /** Factory class only 
     */
    private CustomizerDialog() {}
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
                
    public static Dialog createDialog( ActionListener okOptionListener, JPanel innerPane,
            HelpCtx helpCtx, final ProjectCustomizer.Category[] categories ) {
        
        ListeningButton okButton = new ListeningButton(
                NbBundle.getMessage(CustomizerDialog.class, "LBL_Customizer_Ok_Option"), // NOI18N
                categories);
        okButton.setEnabled(CustomizerDialog.checkValidity(categories));
        
        // Create options
        JButton options[] = new JButton[] { 
            okButton,
            new JButton( NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };
        
        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        
        //A11Y
        options[ OPTION_OK ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Cancel_Option") ); // NOI18N
        

        // RegisterListener
        ActionListener optionsListener = new OptionListener( okOptionListener );        
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        
        innerPane.getAccessibleContext().setAccessibleName( NbBundle.getMessage( CustomizerDialog.class, "AN_ProjectCustomizer") ); //NOI18N
        innerPane.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( CustomizerDialog.class, "AD_ProjectCustomizer") ); //NOI18N      
                        
        if ( helpCtx == null ) {
            helpCtx = HelpCtx.DEFAULT_HELP;
        }
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            innerPane,                             // innerPane
            NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Title" ), // NOI18N // displayName
            false,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            helpCtx,                                // helpCtx
            null );                                 // listener 

        innerPane.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
        if ( innerPane instanceof HelpCtx.Provider ) {
            dialogDescriptor.setHelpCtx( ((HelpCtx.Provider)innerPane).getHelpCtx() );
        }
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                for (int cntr = 0; cntr < categories.length; cntr++) {
                    Utilities.removeCategoryChangeSupport(categories[cntr]);
                }
            }
        });
        
        return dialog;
        
    }
    
    /** Returns whether all given categories are valid or not. */
    private static boolean checkValidity(ProjectCustomizer.Category[] categories) {
        for (int i = 0; i < categories.length; i++) {
            if (!categories[i].isValid()) {
                return false;
            }
            ProjectCustomizer.Category[] subCategories = categories[i].getSubcategories();
            if (subCategories != null) {
                if (!checkValidity(subCategories)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
    
        private ActionListener okOptionListener;
        
        OptionListener( ActionListener okOptionListener ) {
            this.okOptionListener = okOptionListener;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if ( COMMAND_OK.equals( command ) ) {
                // Call the OK option listener
                okOptionListener.actionPerformed( e ); // XXX maybe create new event
            }
            
        }        
        
    }
    
    private static class HelpCtxChangeListener implements PropertyChangeListener {
                
        DialogDescriptor dialogDescriptor;
        HelpCtx defaultHelpCtx;
        
        HelpCtxChangeListener( DialogDescriptor dialogDescriptor, HelpCtx defaultHelpCtx ) {
            this.dialogDescriptor = dialogDescriptor;
            this.defaultHelpCtx = defaultHelpCtx;
        }
        
        public void propertyChange( PropertyChangeEvent evt ) {
            
            if ( CustomizerPane.HELP_CTX_PROPERTY.equals( evt.getPropertyName() ) ) {
                HelpCtx newHelp = (HelpCtx)evt.getNewValue();
                dialogDescriptor.setHelpCtx( newHelp == null  || newHelp == HelpCtx.DEFAULT_HELP  ? defaultHelpCtx : newHelp );
            }
                        
        }
        
    }
    
    private static class ListeningButton extends JButton implements PropertyChangeListener {
        
        private ProjectCustomizer.Category[] categories;
        
        public ListeningButton(String label, ProjectCustomizer.Category[] categories) {
            super(label);
            this.categories = categories;
            for (int i = 0; i < categories.length; i++) {
                Utilities.getCategoryChangeSupport(categories[i]).addPropertyChangeListener(this);
            }
            
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == CategoryChangeSupport.VALID_PROPERTY) {
                boolean valid = ((Boolean) evt.getNewValue()).booleanValue();
                // enable only if all categories are valid
                setEnabled(valid && CustomizerDialog.checkValidity(categories));
            }
        }
        
    }
    
}
