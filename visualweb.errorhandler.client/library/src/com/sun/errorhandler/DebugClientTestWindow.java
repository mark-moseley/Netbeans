/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */ 
package com.sun.errorhandler;
/*
 * DebugClientWindow.java
 * Created on January 6, 2004, 1:21 PM
 */

/**
 * @author  Winston Prakash
 */
public class DebugClientTestWindow extends javax.swing.JFrame {
    DebugClientTestThread clientThread = null;
    /** Creates new form ServerWindow */
    public DebugClientTestWindow() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        messagePanel = new javax.swing.JPanel();
        messageField = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        exitButton = new javax.swing.JButton();
        sendButton = new javax.swing.JButton();

        setTitle("Client Window");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        mainPanel.setLayout(new java.awt.BorderLayout());

        textArea.setColumns(35);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(15);
        textArea.setWrapStyleWord(true);
        scrollPane.setViewportView(textArea);

        mainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        messagePanel.setLayout(new java.awt.BorderLayout());

        messagePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 1, 1, 1)));
        messagePanel.add(messageField, java.awt.BorderLayout.NORTH);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 1, 1, 1)));
        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(exitButton, gridBagConstraints);

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(sendButton, gridBagConstraints);

        messagePanel.add(buttonPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(messagePanel, java.awt.BorderLayout.SOUTH);

        setBounds(500, 100, 400, 300);
    }//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        if(messageField.getText() != null || !messageField.getText().equals("")){
            if( clientThread.isConnected()) clientThread.sendMessage(messageField.getText());
            messageField.setText("");
        }
    }//GEN-LAST:event_sendButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed
    public void connectSocket(){
        clientThread = new DebugClientTestThread(textArea);
        clientThread.start();
    }
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        clientThread.disconnect();
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DebugClientTestWindow clientWindow = new DebugClientTestWindow();
        clientWindow.connectSocket();
        clientWindow.show();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton exitButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField messageField;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
    
}
