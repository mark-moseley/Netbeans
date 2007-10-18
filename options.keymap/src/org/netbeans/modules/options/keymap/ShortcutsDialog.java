/*
 * ShortcutsDialog1.java
 *
 * Created on February 20, 2006, 2:51 PM
 */

package org.netbeans.modules.options.keymap;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Jancura
 */
public class ShortcutsDialog extends javax.swing.JPanel {
    public static final String PROP_SHORTCUT_VALID = "ShortcutsDialog.PROP_SHORTCUT_VALID"; //NOI18N
    
    private Listener listener = null;
    private JButton bTab = new JButton ();
    private JButton bClear = new JButton ();
    private ShortcutsFinder f = null;
    private boolean shortcutValid = false;
    
    void init(ShortcutsFinder f) {
        this.f = f;
        loc (lShortcut, "Shortcut"); //NOI18N
        lConflict.setForeground (Color.red);
        loc (bTab, "CTL_Tab"); //NOI18N
        bTab.getAccessibleContext().setAccessibleName(loc("AN_Tab")); //NOI18N
        bTab.getAccessibleContext().setAccessibleDescription(loc("AD_Tab")); //NOI18N
        loc (bClear, "CTL_Clear"); //NOI18N
        bClear.getAccessibleContext().setAccessibleName(loc("AN_Clear")); //NOI18N
        bClear.getAccessibleContext().setAccessibleDescription(loc("AD_Clear")); //NOI18N
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        tfShortcut.getAccessibleContext().setAccessibleName(loc("AN_Shortcut")); //NOI18N
        tfShortcut.getAccessibleContext().setAccessibleDescription(loc("AD_Shortcut")); //NOI18N
//        tfShortcut.setFocusTraversalKeys (
//            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, 
//            Collections.EMPTY_SET
//        );
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        listener = new Listener ();
        tfShortcut.addKeyListener(listener);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ShortcutsDialog.class, key);
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
    
    /** Creates new form ShortcutsDialog1 */
    public ShortcutsDialog() {
        initComponents();
    }

    public Listener getListener() {
        return listener;
    }

    public javax.swing.JLabel getLShortcut() {
        return lShortcut;
    }

    public javax.swing.JTextField getTfShortcut() {
        return tfShortcut;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lShortcut = new javax.swing.JLabel();
        tfShortcut = new javax.swing.JTextField();
        lConflict = new javax.swing.JLabel();

        lShortcut.setLabelFor(tfShortcut);
        lShortcut.setText("Shortcut:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lConflict, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lShortcut)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tfShortcut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lShortcut)
                    .add(tfShortcut, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lConflict)
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lConflict;
    private javax.swing.JLabel lShortcut;
    private javax.swing.JTextField tfShortcut;
    // End of variables declaration//GEN-END:variables

    
        class Listener implements ActionListener, KeyListener {

            private KeyStroke backspaceKS = KeyStroke.getKeyStroke 
                (KeyEvent.VK_BACK_SPACE, 0);
            private KeyStroke tabKS = KeyStroke.getKeyStroke 
                (KeyEvent.VK_TAB, 0);
            
            private String key = ""; //NOI18N

            public void keyTyped (KeyEvent e) {
                e.consume ();
            }

            public void keyPressed (KeyEvent e) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke (
                    e.getKeyCode (),
                    e.getModifiers ()
                );
                
                boolean add = e.getKeyCode () != KeyEvent.VK_SHIFT &&
                              e.getKeyCode () != KeyEvent.VK_CONTROL &&
                              e.getKeyCode () != KeyEvent.VK_ALT &&
                              e.getKeyCode () != KeyEvent.VK_META &&
                              e.getKeyCode () != KeyEvent.VK_ALT_GRAPH;
                
                if (keyStroke.equals (backspaceKS) && !key.equals ("")) {
                    // delete last key
                    int i = key.lastIndexOf (' '); //NOI18N
                    if (i < 0) {
                        key = ""; //NOI18N
                    } else {
                        key = key.substring (0, i);
                    }
                    getTfShortcut().setText (key);
                } else {
                    // add key
                    addKeyStroke (keyStroke, add);
                }
                if (add) {
                    updateWarning();
                } else {
                    setShortcutValid(false);
                }
                e.consume ();
            }

            public void keyReleased (KeyEvent e) {
                e.consume ();
            }
            
            public void actionPerformed (ActionEvent e) {
                if (e.getSource () == getBClear()) {
                    key = ""; //NOI18N
                    getTfShortcut().setText (key);
                } else 
                if (e.getSource () == getBTab()) {
                    addKeyStroke (tabKS, true);
                }
                updateWarning();
            }
            
            private void updateWarning () {
                String text = getTfShortcut().getText();
                ShortcutAction action = f.findActionForShortcut(text);
                if (action != null) {
                    lConflict.setText (MessageFormat.format (
                        loc ("Shortcut_Conflict"), //NOI18N
                        new Object[] {action.getDisplayName ()}
                    ));
                    setShortcutValid(true);
                } else {
                    lConflict.setText (""); //NOI18N
                    setShortcutValid(text != null && text.length() > 0);
                }
            }
            
            private void addKeyStroke (KeyStroke keyStroke, boolean add) {
                String k = Utils.getKeyStrokeAsText (keyStroke);
                if (key.equals ("")) { //NOI18N
                    getTfShortcut().setText (k);
                    if (add) key = k;
                } else {
                    getTfShortcut().setText (key + " " + k); //NOI18N
                    if (add) key += " " + k; //NOI18N
                }
            }
        }

    public JButton getBTab() {
        return bTab;
    }

    public JButton getBClear() {
        return bClear;
    }
    
    public boolean isShortcutValid() {
        return shortcutValid;
    }
    
    private void setShortcutValid(boolean valid) {
        if (valid != shortcutValid) {
            shortcutValid = valid;
            firePropertyChange(PROP_SHORTCUT_VALID, !shortcutValid, shortcutValid);
        }
    }
}
