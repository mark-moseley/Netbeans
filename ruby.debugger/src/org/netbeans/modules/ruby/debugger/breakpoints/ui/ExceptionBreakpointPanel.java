
package org.netbeans.modules.ruby.debugger.breakpoints.ui;

import javax.swing.JPanel;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyExceptionBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;

public class ExceptionBreakpointPanel extends JPanel implements Controller {

    public ExceptionBreakpointPanel() {
        initComponents();
    }

    public boolean ok() {
        String exceptionClass = exceptionClassField.getText().trim();
        if (exceptionClass.length() > 0) {
            RubyExceptionBreakpoint rbe = new RubyExceptionBreakpoint(exceptionClass);
            DebuggerManager.getDebuggerManager().addBreakpoint(rbe);
        }
        return true;
    }

    public boolean cancel() {
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exceptionClassLbl = new javax.swing.JLabel();
        exceptionClassField = new javax.swing.JTextField();

        exceptionClassLbl.setLabelFor(exceptionClassField);
        org.openide.awt.Mnemonics.setLocalizedText(exceptionClassLbl, org.openide.util.NbBundle.getMessage(ExceptionBreakpointPanel.class, "ExceptionBreakpointPanel.exceptionClassLbl.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(exceptionClassLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(exceptionClassField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exceptionClassLbl)
                    .add(exceptionClassField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField exceptionClassField;
    private javax.swing.JLabel exceptionClassLbl;
    // End of variables declaration//GEN-END:variables

}
