/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Component;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.loaders.TemplateWizard;
import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;

public class CustomizeScriptPanel extends javax.swing.JPanel implements WizardDescriptor.Panel /* .FinishPanel */ {

    /** Create the wizard panel and set up some basic properties. */
    public CustomizeScriptPanel () {
        initComponents ();
        // Provide a name in the title bar.
        setName (NbBundle.getMessage (CustomizeScriptPanel.class, "CSP_LBL_cust_gend_ant_script"));
        scriptPane.setContentType ("text/xml"); // NOI18N
        // Hack; EditorKit does not permit "fallback" kits, so we have to
        // mimic what the IDE itself does:
        EditorKit kit = scriptPane.getEditorKit ();
        String clazz = kit.getClass ().getName ();
        if (clazz.equals ("javax.swing.text.DefaultEditorKit") || // NOI18N
               clazz.equals ("javax.swing.JEditorPane$PlainEditorKit")) { // NOI18N
            scriptPane.setEditorKit (JEditorPane.createEditorKitForContentType ("text/plain")); // NOI18N
        }
    }

    // --- VISUAL DESIGN OF PANEL ---
    
    public void requestFocus () {
        super.requestFocus ();
        scriptPane.requestFocus ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        scrollPane = new javax.swing.JScrollPane ();
        scriptPane = new javax.swing.JEditorPane ();
        hintsArea = new javax.swing.JTextArea ();
        
        setLayout (new java.awt.BorderLayout ());
        
        scrollPane.setViewportView (scriptPane);
        
        add (scrollPane, java.awt.BorderLayout.CENTER);
        
        hintsArea.setWrapStyleWord (true);
        hintsArea.setLineWrap (true);
        hintsArea.setEditable (false);
        hintsArea.setForeground (new java.awt.Color (102, 102, 153));
        hintsArea.setText (NbBundle.getMessage (CustomizeScriptPanel.class, "CSP_TEXT_you_may_customize_gend2"));
        hintsArea.setBackground (new java.awt.Color (204, 204, 204));
        add (hintsArea, java.awt.BorderLayout.NORTH);
        
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JEditorPane scriptPane;
    private javax.swing.JTextArea hintsArea;
    // End of variables declaration//GEN-END:variables

    // --- WizardDescriptor.Panel METHODS ---

    // Get the visual component for the panel. In this template, the same class
    // serves as the component and the Panel interface, but you could keep
    // them separate if you wished.
    public Component getComponent () {
        return this;
    }

    public HelpCtx getHelp () {
        return HelpCtx.DEFAULT_HELP;//XXX
    }

    public boolean isValid () {
        return true;
        // XXX ideally make it valid only if script is parseable without errors;
        // could use AntProjectSupport for this, or just parse the XML and check
        // for the correct root element etc.
    }

    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    /*
    private final Set listeners = new HashSet (1); // Set<ChangeListener>
    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    }
    */

    public void readSettings (Object settings) {
        TemplateWizard wiz = (TemplateWizard) settings;
        String contents = (String) wiz.getProperty (ShortcutIterator.PROP_CONTENTS);
        if (contents == null) contents = ""; // NOI18N
        scriptPane.setText (contents);
    }
    public void storeSettings (Object settings) {
        TemplateWizard wiz = (TemplateWizard) settings;
        wiz.putProperty (ShortcutIterator.PROP_CONTENTS, scriptPane.getText ());
    }

}
