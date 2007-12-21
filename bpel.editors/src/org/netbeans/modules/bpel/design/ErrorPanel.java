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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.NbBundle;

public class ErrorPanel extends JEditorPane {

    private DesignView designView;
    private boolean installed = false;
    private static final long serialVersionUID = 1;

//    private Timer timer;
    
    public ErrorPanel(DesignView designView) {
        this.designView = designView;
//        this.timer = new Timer(3000, this);
        
//        setEditorKitForContentType("text/html",new HTMLEditorKit()); // NOI18N
        setEditable(false);
        setPreferredSize(new Dimension(200, 200));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentType("text/html"); // NOI18N
        setBackground(designView.getBackground());
            setText(NbBundle.getMessage(ErrorPanel.class, "LBL_ErrorPanel_Content"));
//        try {
//            setPage(ERROR_MESSAGE);
//            
//        } catch (IOException e) {};
    }


    public boolean isInstalled() {
        return installed;
    }
    
    
    public void install() {
        if (!installed) {
            Component c = getDesignView().getParent();
            
            if (c != null) {
                JScrollPane scrollPane = (JScrollPane) c.getParent();
                scrollPane.setViewportView(this);
            }
            
            installed = true;
//            timer.start();
        }
            
//        updateErrorMessage();
    }
    
    
//    public void updateErrorMessage() {
//        if (!isInstalled()) return;
//        
//        boolean verifyNamespace = false;
//        
//        if (designView.getBPELModel().getState() != BpelModel.State.VALID) {
//            AnotherVersionBpelProcess avp = designView.getBPELModel()
//                    .getAnotherVersionProcess();
//            
//            if (avp != null) {
//                String currentNS = avp.getNamespaceUri();
//                if (currentNS == null) {
//                    verifyNamespace = true;
//                } else {
//                    verifyNamespace = !currentNS.equals(BpelEntity
//                            .BUSINESS_PROCESS_NS_URI);
//                }
//            }
//        }
//
//        try {
//            setPage((verifyNamespace) ? ERROR_MESSAGE_2 : ERROR_MESSAGE_1);
//        } catch (IOException e) {};
//    }
    
    
    public void uninstall() {
        if (!installed) return;
        
        JScrollPane scrollPane = (JScrollPane) getParent().getParent();
        scrollPane.setViewportView(getDesignView());
        
//        timer.stop();
        installed = false;
    }
    
    
    public DesignView getDesignView() {
        return designView;
    }

    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2);
    }
    

//    public void actionPerformed(ActionEvent e) {
//        updateErrorMessage();
//    }
    

//    private static URL ERROR_MESSAGE; // NOI18N
    
    
    static {
//        try {
//            ERROR_MESSAGE = new URL("nbresloc:/org/netbeans/modules/bpel/design/resources/errormessage.html"); // NOI18N
//            ERROR_MESSAGE = NbBundle.getMessage(ErrorPanel.class, "LBL_ErrorPanel_Content");// NOI18N
//        } catch (MalformedURLException e) {}
    }
    
    
//    private static URL ERROR_MESSAGE_1 = ErrorPanel.class
//            .getResource("resources/errormessage1.html"); // NOI18N
//
//    private static URL ERROR_MESSAGE_2 = ErrorPanel.class
//            .getResource("resources/errormessage2.html"); // NOI18N
}
