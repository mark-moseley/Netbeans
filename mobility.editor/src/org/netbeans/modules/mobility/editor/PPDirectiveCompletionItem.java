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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.editor;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.api.editor.completion.Completion;
import org.openide.util.Utilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: bohemius
 * Date: Jul 18, 2005
 * Time: 5:49:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PPDirectiveCompletionItem implements CompletionItem {
    
    private static ImageIcon icon;
    final private String name;
    final private String description;
    
    public PPDirectiveCompletionItem(String name, String desc) {
        this.name = name;
        this.description = desc;
    }
    
    public void defaultAction(final JTextComponent component) {
        substitute(component, this.name);
        
        Completion.get().hideAll();
    }
    
    public void processKeyEvent(final KeyEvent event) {
        if (event.getKeyChar()==' ' && !event.isControlDown()) {
            Completion.get().hideCompletion();
        }
    }
    
    public int getPreferredWidth(final Graphics graphics, final Font font) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(),
                graphics, font);
    }
    
    public void render(final Graphics g, final Font defaultFont, final Color defaultColor, final Color backgroundColor, final int width, final int height, final boolean selected) {
        
        synchronized (this)
        {
            if (icon == null) {
                icon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/mobility/editor/resources/d.png"));
            }
        }
        
        if (selected) {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);
            g.setColor(defaultColor);
        }
        CompletionUtilities.renderHtml(icon, getLeftText(), getRightText(), g, defaultFont, defaultColor, width, height, selected);
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public boolean instantSubstitution(@SuppressWarnings("unused")
	final JTextComponent jTextComponent) {
        return false;
    }
    
    public int getSortPriority() {
        return 50;
    }
    
    public CharSequence getSortText() {
        return name;
    }
    
    private String leftText;
    
    private String getLeftText() {
        if (leftText == null) {
            leftText = "<b>" + toHtmlText(this.name) + "</b>  ";
        }
        return leftText;
    }
    
    private String getRightText() {
        return toHtmlText(this.description);
    }
    
    private void substitute(final JTextComponent component, final String directive) {
        final int offset = component.getCaret().getDot();
        final Document doc=component.getDocument();
        
        try {
            int wordStart=org.netbeans.editor.Utilities.getWordStart(component, offset);
            final int wordEnd=org.netbeans.editor.Utilities.getWordEnd(component, offset);
            
            if (wordStart > 0) {
                String word = doc.getText(wordStart, wordEnd - wordStart);
                if (word.startsWith("//#")) {
                    word=word.substring(3);
                    wordStart+=3;
                }
                doc.remove(wordStart, word.length());
                doc.insertString(wordStart, directive, null);
                
            } else {
                doc.insertString(offset, directive, null);
            }
        } catch (BadLocationException ble) {}
    }
    
    private static String toHtmlText(final String text) {
        StringBuffer htmlText = null;
        for (int i = 0; i < text.length(); i++) {
            String rep; // replacement string
            final char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    rep = "&lt;";
                    break;
                case '>':
                    rep = "&gt;";
                    break;
                case '\n':
                    rep = "<br>";
                    break;
                default:
                    rep = null;
                    break;
            }
            
            if (rep != null) {
                if (htmlText == null) {
                    // Expect 20% of text to be html tags text
                    htmlText = new StringBuffer(120 * text.length() / 100);
                    if (i > 0) {
                        htmlText.append(text.substring(0, i));
                    }
                }
                htmlText.append(rep);
                
            } else { // no replacement
                if (htmlText != null) {
                    htmlText.append(ch);
                }
            }
        }
        return (htmlText != null) ? htmlText.toString() : text;
    }
    
    public CharSequence getInsertPrefix() {
        return name;
    }
}


