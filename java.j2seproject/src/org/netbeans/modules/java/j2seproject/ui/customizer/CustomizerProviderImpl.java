/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** Customization of J2SE project
 *
 * @author Petr Hrebejk
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final Project project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private static Map /*<Project,Dialog>*/project2Dialog = new HashMap(); 
    
    public CustomizerProviderImpl(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
    }
            
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    public void showCustomizer( String preselectedCategory ) {
        
        Dialog dialog = (Dialog)project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.show ();
            return;
        }
        else {
            J2SEProjectProperties uiProperties = new J2SEProjectProperties( (J2SEProject)project, updateHelper, evaluator, refHelper );        
            init( uiProperties );

            OptionListener listener = new OptionListener( project, uiProperties );
            HelpCtx helpCtx = new HelpCtx( "org.netbeans.modules.java.j2seproject.ui.customizer.J2SECustomizer" );
            dialog = ProjectCustomizer.createCustomizerDialog( categories, panelProvider, preselectedCategory, listener, helpCtx );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.show();
        }
    }    
    
    // Names of categories
    private static final String BUILD_CATEGORY = "BuildCategory";
    private static final String RUN_CATEGORY = "RunCategory";
    
    private static final String GENERAL = "General";
    private static final String SOURCES = "Sources";
    private static final String LIBRARIES = "Libraries";
    
    private static final String BUILD = "Build";
    private static final String BUILD_TESTS = "BuildTests";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    private static final String RUN = "Run";    
    private static final String RUN_TESTS = "RunTests";
    
    private void init( J2SEProjectProperties uiProperties ) {
        
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
    
        /*
        ProjectCustomizer.Category general = ProjectCustomizer.Category.create( 
                BUILD, 
                bundle.getString( "LBL_Config_Build" ), 
                null, 
                null );        
        */
        
        
        ProjectCustomizer.Category sources = ProjectCustomizer.Category.create(
                SOURCES,
                bundle.getString ("LBL_Config_Sources"),
                null,
                null);
        
        ProjectCustomizer.Category libraries = ProjectCustomizer.Category.create (
                LIBRARIES,
                bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                null,
                null );
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                BUILD, 
                bundle.getString( "LBL_Config_Build" ), // NOI18N
                null,
                null);
        ProjectCustomizer.Category jar = ProjectCustomizer.Category.create(
                JAR,
                bundle.getString( "LBL_Config_Jar" ), // NOI18N
                null,
                null );
        ProjectCustomizer.Category javadoc = ProjectCustomizer.Category.create(
                JAVADOC,
                bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                null,
                null );
        
        ProjectCustomizer.Category run = ProjectCustomizer.Category.create(
                RUN,
                bundle.getString( "LBL_Config_Run" ), // NOI18N
                null,
                null );    
        ProjectCustomizer.Category runTests = ProjectCustomizer.Category.create(
                RUN_TESTS,
                bundle.getString( "LBL_Config_Test" ), // NOI18N
                null,
                null);

        ProjectCustomizer.Category buildCategory = ProjectCustomizer.Category.create(
                BUILD_CATEGORY,
                bundle.getString( "LBL_Config_BuildCategory" ), // NOI18N
                null,
                new ProjectCustomizer.Category[] { build, jar, javadoc }  );
        categories = new ProjectCustomizer.Category[] { 
                sources,
                libraries,        
                buildCategory,
                run,  
        };
        
        Map panels = new HashMap();
        panels.put( sources, new CustomizerSources( uiProperties ) );
        panels.put( libraries, new CustomizerLibraries( uiProperties ) );
        panels.put( build, new CustomizerCompile( uiProperties ) );
        panels.put( jar, new CustomizerJar( uiProperties ) );
        panels.put( javadoc, new CustomizerJavadoc( uiProperties ) );
        panels.put( run, new CustomizerRun( uiProperties ) ); 
        
        panelProvider = new PanelProvider( panels );
        
    }
    
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        private Map /*<Category,JPanel>*/ panels;
        
        PanelProvider( Map panels ) {
            this.panels = panels;            
        }
        
        public JComponent create( ProjectCustomizer.Category category ) {
            JComponent panel = (JComponent)panels.get( category );
            return panel == null ? EMPTY_PANEL : panel;
        }
                        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        private J2SEProjectProperties uiProperties;
        
        OptionListener( Project project, J2SEProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;            
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            // Store the properties into project 
            uiProperties.save();
            
            // Close & dispose the the dialog
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.hide();
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
    }    
                            
}
