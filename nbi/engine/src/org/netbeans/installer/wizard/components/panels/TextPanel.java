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
 *  
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JTextPane;

/**
 *
 * @author ks152834
 */
public class TextPanel extends DefaultWizardPanel {
    private JTextPane textPane;
    
    public TextPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(CONTENT_TYPE_PROPERTY, DEFAULT_CONTENT_TYPE);
    }
    
    public void initialize() {
        textPane.setContentType(getProperty(CONTENT_TYPE_PROPERTY));
        textPane.setText(getProperty(TEXT_PROPERTY));
    }
    
    public void initComponents() {
        setLayout(new GridBagLayout());
        
        textPane = new JTextPane();
        textPane.setOpaque(false);
        
        add(textPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
    
    public static final String TEXT_PROPERTY = "text";
    public static final String CONTENT_TYPE_PROPERTY = "content.type";
    
    public static final String DEFAULT_TEXT = "";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
}
