/*
 * MacWarningPanel.java
 *
 * Created on September 18, 2007, 5:25 PM
 */

package org.netbeans.modules.uml;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author  krichard
 */
public class MacWarningPanel extends javax.swing.JPanel {
    
    /** Creates new form MacWarningPanel */
    public MacWarningPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jEditorPane1 = new javax.swing.JEditorPane();

        jEditorPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jEditorPane1.setContentType(org.openide.util.NbBundle.getMessage(MacWarningPanel.class, "MacWarningPanel.jEditorPane1.contentType")); // NOI18N
        jEditorPane1.setEditable(false);
        jEditorPane1.setText(org.openide.util.NbBundle.getMessage(MacWarningPanel.class, "HTML_MAC_WARNING")); // NOI18N
        jEditorPane1.setOpaque(false);
        jEditorPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                jEditorPane1HyperlinkUpdate(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 551, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jEditorPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 526, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 93, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jEditorPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jEditorPane1HyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPane1HyperlinkUpdate
        // TODO add your handling code here:
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                 JEditorPane pane = (JEditorPane) evt.getSource();
                 if (evt instanceof HTMLFrameHyperlinkEvent) {
                     HTMLFrameHyperlinkEvent  e = (HTMLFrameHyperlinkEvent)evt;
                     HTMLDocument doc = (HTMLDocument)pane.getDocument();
                     doc.processHTMLFrameHyperlinkEvent(e);
                 } else {
                     try {
                         HtmlBrowser.URLDisplayer.getDefault().showURL(evt.getURL());
                         
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
             }
    }//GEN-LAST:event_jEditorPane1HyperlinkUpdate
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    // End of variables declaration//GEN-END:variables
    

}
