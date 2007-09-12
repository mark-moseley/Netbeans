/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xslt.mapper.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;

public class ErrorPanel extends JEditorPane {
    
    private XsltMapper mapper;
    private boolean installed = false;
    private static final long serialVersionUID = 1;
    
    public ErrorPanel(XsltMapper mapper) {
        this.mapper = mapper;
        
        setEditorKitForContentType("text/html",new HTMLEditorKit()); // NOI18N
        setEditable(false);
        setPreferredSize(new Dimension(200, 200));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentType("text/html"); // NOI18N
        setBackground(mapper.getBackground());
    }
    
    
    
    
    public void install() {
        if (!installed){
            JComponent parent = (JComponent) mapper.getParent();
            parent.remove(mapper);
            parent.add(this);
            parent.invalidate();
            parent.repaint();
            
            installed = true;
        }
    }
    public void uninstall() {
        if (installed) {
            
            JComponent parent = (JComponent) getParent();
            parent.remove(this);
            parent.add(mapper);
            parent.invalidate();
            parent.repaint();
            
            installed = false;
        }
    }
    public void setMessage(String message){
        
        
        StringBuffer s = new StringBuffer();
        s.append("<html><body><font face=sans-serif size=3 color=#990000>"); // NOI18N
        s.append(message); // NOI18N
        s.append("</font><br><br></body></html>"); // NOI18N
        setText(s.toString());
        
        
    }
    
    
    
    
    
    
    
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2);
    }
}
