/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import javax.swing.text.html.HTMLEditorKit;

import java.io.StringWriter;

import javax.swing.JButton;

import java.awt.Dialog;

/**
 * This Dialog will show exceptions encountered while a user is testing a web service client method.
 * @author  David Botterill
 */
public class MethodExceptionDialog extends javax.swing.JPanel {
    
    private String currentMessage = "";
    private JButton okButton = new JButton(NbBundle.getMessage(this.getClass(), "OPTION_OK"));
    private DialogDescriptor dlg;
    private Dialog dialog;
    
    /** Creates new form MethodExceptionDialog */
    public MethodExceptionDialog(Exception inException) {
        initComponents();
        setMessage(inException,false);
        
    }
    public void show(){
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(this.getClass(), "CLIENT_EXCEPTION"),
        false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
        DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), null);
        dlg.setOptions(new Object[] { okButton });
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setSize(500,300);
        dialog.show();
    }
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TestWebServiceMethodDlg.class);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        scrollPane = new javax.swing.JScrollPane();
        messagePane = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        messagePane.setEditorKit(new HTMLEditorKit()
        );
        scrollPane.setViewportView(messagePane);

        add(scrollPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    public void setMessage(Exception inException,boolean keepOld){
        
        
        String htmlStart = "<HTML><HEAD>" +
        "<style type=\"text/css\">" +
        "body { font-family: Verdana, sans-serif; font-size: 12; }" +
        "</style>" +
        "</HEAD>" +
        "<BODY>";
        String htmlEnd = "</BODY></HTML>";
        String exceptionString = "";
        /**
         * unwrap the exceptions.
         */
        Throwable cause = inException;
        while(null != cause) {
            exceptionString += "<BR><FONT COLOR=\"RED\">" + cause.getLocalizedMessage() + "</FONT>";
            StackTraceElement [] traceElements = cause.getStackTrace();
            String stackTrace = "<BR>Stack Trace<BR><BR>";
            for(int ii=0;ii < traceElements.length;ii++) {
                exceptionString += "<BR>" + traceElements[ii].toString();
            }
            cause = cause.getCause();
			if(null != cause) {
				exceptionString += "<BR>Next Exception Layer";
			}
        }
        
        
        if(keepOld) {
            currentMessage += exceptionString;
        } else {
            currentMessage = exceptionString;
        }
        
        
        
        
        messagePane.setText(htmlStart + currentMessage + htmlEnd);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane messagePane;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
}
