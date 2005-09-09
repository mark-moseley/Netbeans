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

package org.apache.tools.ant.module;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.netbeans.spi.options.OptionsCategory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AntCustomizer extends JPanel implements 
ActionListener {

    private JTextField      tfAntHome = new JTextField ();
    private JButton         bAntHome = new JButton ();
    private JCheckBox       cbSaveFiles = new JCheckBox ();
    private JCheckBox       cbReuseOutput = new JCheckBox ();
    private JCheckBox       cbAlwaysShowOutput = new JCheckBox();
    private JComboBox       cbVerbosity = new JComboBox ();
    private JButton         bProperties = new JButton ();
    private JButton         bClasspath = new JButton ();
    protected JLabel        lAntVersion = new JLabel ();
    
    private NbClassPath     classpath;
    private Properties      properties;
    private boolean         changed = false;
    private File            originalAntHome;
    
    
    public AntCustomizer () {
        tfAntHome.setColumns (30);
        loc (bAntHome, "Ant_Home_Button");
        bAntHome.addActionListener (this);
        bAntHome.setBackground (Color.white);
        loc (cbSaveFiles, "Save_Files");
        cbSaveFiles.setBackground (Color.white);
        cbReuseOutput.setBackground (Color.white);
        loc (cbReuseOutput, "Reuse_Output");
        loc(cbAlwaysShowOutput, "Always_Show_Output");
        cbAlwaysShowOutput.setBackground(Color.white);
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_warn"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_info"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_verbose"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_debug"));
        loc (bProperties, "Properties_Button");
        bProperties.addActionListener (this);
        bProperties.setBackground (Color.white);
        loc (bClasspath, "Classpath_Button");
        bClasspath.addActionListener (this);
        bClasspath.setBackground (Color.white);
        
        FormLayout layout = new FormLayout (
            "p:g", // cols
            "p, 3dlu, p, 3dlu, p"
        );      // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();

            FormLayout layout1 = new FormLayout (
                "p, 5dlu, p:g, 5dlu, p", // cols
                "p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p"
            );      // rows
            PanelBuilder builder1 = new PanelBuilder (layout1);
            builder1.addLabel ( loc ("Ant_Home"),       lc.xy (1, 1), 
                                tfAntHome,              cc.xy (3, 1));
            builder1.add (      bAntHome,               cc.xy (5, 1));
            builder1.add (      lAntVersion,            lc.xyw (3, 3, 3));
            builder1.add (      cbSaveFiles,            cc.xy (3, 5));
            builder1.add (      cbReuseOutput,          cc.xy (3, 7));
            builder1.add (      cbAlwaysShowOutput,     cc.xy (3, 9));
            builder1.addLabel ( loc ("Verbosity"),      lc.xy (1, 11), 
                                cbVerbosity,            cc.xy (3, 11, "l, d"));   
            builder1.getPanel ().setBackground (Color.white);
            //setBorder (new TitledBorder (loc ("Ant_Settings")));
        builder.add (builder1.getPanel (),              cc.xy (1, 1));
        
            layout1 = new FormLayout (
                "50dlu:g, 5dlu, p", // cols
                "p"
            );      // rows
            builder1 = new PanelBuilder (layout1);
            builder1.addLabel ("<html>" + loc ("Properties_Text_Area"), 
                                                        cc.xy (1, 1));
            builder1.add (bProperties,                  cc.xy (3, 1));
            builder1.getPanel ().setBackground (Color.white);
            builder1.getPanel ().setBorder 
                (new TitledBorder (loc ("Properties_Panel")));
        builder.add (builder1.getPanel (),              cc.xy (1, 3));
        
            layout1 = new FormLayout (
                "50dlu:g, 5dlu, p", // cols
                "p"
            );      // rows
            builder1 = new PanelBuilder (layout1);
            builder1.addLabel ("<html>" + loc ("Classpath_Text_Area"), 
                                                        cc.xy (1, 1));
            builder1.add (bClasspath,                   cc.xy (3, 1));
            builder1.getPanel ().setBackground (Color.white);
            builder1.getPanel ().setBorder 
                (new TitledBorder (loc ("Classpath_Panel")));
        builder.add (builder1.getPanel (),              cc.xy (1, 5));
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (AntCustomizer.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
    
    void update () {
        AntSettings settings = AntSettings.getDefault ();
        classpath = settings.getExtraClasspath ();
        properties = settings.getProperties ();
        originalAntHome = settings.getAntHomeWithDefault ();
            
        tfAntHome.setText (originalAntHome.toString ());
        cbSaveFiles.setSelected (settings.getSaveAll ());
        cbReuseOutput.setSelected (settings.getAutoCloseTabs ());
        cbAlwaysShowOutput.setSelected (settings.getAlwaysShowOutput ());
        cbVerbosity.setSelectedIndex (settings.getVerbosity () - 1);
        lAntVersion.setText ("(" + settings.getAntVersion () + ")");
    }
    
    void applyChanges () {
        if (!changed) return;
        AntSettings settings = AntSettings.getDefault ();
        settings.setAntHome (new File (tfAntHome.getText ()));
        settings.setAutoCloseTabs (cbReuseOutput.isSelected ());
        settings.setSaveAll (cbSaveFiles.isSelected ());
        settings.setAlwaysShowOutput (cbAlwaysShowOutput.isSelected ());
        settings.setVerbosity (cbVerbosity.getSelectedIndex () + 1);
        settings.setProperties (properties);
        settings.setExtraClasspath (classpath);
    }
    
    void cancel () {
        if (!changed) return;
        AntSettings settings = AntSettings.getDefault ();
        settings.setAntHome (originalAntHome);
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return true;
    }
    
    public void actionPerformed (ActionEvent e) {
        Object o = e.getSource ();
        if (o == bAntHome) {
            JFileChooser chooser = new JFileChooser (tfAntHome.getText ());
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            int r = chooser.showDialog (
                SwingUtilities.getWindowAncestor (this),
                loc ("Select_Directory")
            );
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile ();
                if (!new File (new File (file, "lib"), "ant.jar").isFile ()) {
                    DialogDisplayer.getDefault ().notify (new Message (
                        MessageFormat.format (
                            loc ("Not_a_ant_home"),
                            new Object[] {file}
                        ),
                        Message.WARNING_MESSAGE
                    ));
                    return;
                }
                tfAntHome.setText (file.getAbsolutePath ());
                AntSettings settings = AntSettings.getDefault ();
                settings.setAntHome (file);
                lAntVersion.setText ("(" + settings.getAntVersion () + ")");
                changed = true;
            }
        } else
        if (o == bClasspath) {
            PropertyEditor editor = PropertyEditorManager.findEditor 
                (NbClassPath.class);
            editor.setValue (classpath);
            Component customEditor = editor.getCustomEditor ();
            DialogDescriptor dd = new DialogDescriptor (
                customEditor,
                loc ("Classpath_Editor_Title")
            );
            Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            if (dd.getValue () == dd.OK_OPTION) {
                classpath = (NbClassPath) editor.getValue ();
                changed = true;
            }
        } else
        if (o == bProperties) {
            PropertyEditor editor = PropertyEditorManager.findEditor 
                (Properties.class);
            editor.setValue (properties);
            Component customEditor = editor.getCustomEditor ();
            DialogDescriptor dd = new DialogDescriptor (
                customEditor,
                loc ("Properties_Editor_Title")
            );
            Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            if (dd.getValue () == dd.OK_OPTION) {
                if (customEditor instanceof EnhancedCustomPropertyEditor)
                    properties = (Properties) ((EnhancedCustomPropertyEditor) customEditor).getPropertyValue ();
                else
                    properties = (Properties) editor.getValue ();
                changed = true;
            }
        }
    }
}
