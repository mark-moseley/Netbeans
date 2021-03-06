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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author vv159170
 */
public class CsmIncludeCompletionItem implements CompletionItem {
       
    protected final static String QUOTE = "\""; // NOI18N
    protected final static String SYS_OPEN = "<"; // NOI18N
    protected final static String SYS_CLOSE = ">"; // NOI18N
    protected final static String SLASH = "/"; // NOI18N
    protected final static String PARENT_COLOR_TAG = "<font color=\"#557755\">"; // NOI18N
    
    private final static int MAX_DISPLAYED_DIR_LENGTH = 35;
    private final static int NR_DISPLAYED_FRONT_DIRS = 2;
    private final static int NR_DISPLAYED_TRAILING_DIRS = 2;

    private final int substitutionOffset;
    private final int priority;
    private final String item;
    private final String parentFolder;
    private final String childSubdir;
    private final boolean isSysInclude;
    private final boolean isFolder;
    private final boolean supportInstantSubst;
    
    private static final int FOLDER_PRIORITY = 30;
    private static final int FILE_PRIORITY = 10;
    private static final int SYS_VS_USR = 5;
    
    protected CsmIncludeCompletionItem(int substitutionOffset, int priority, 
            String parentFolder, String childSubdir, String item,
            boolean sysInclude, boolean isFolder,
            boolean supportInstantSubst) {
        this.substitutionOffset = substitutionOffset;
        this.priority = priority;
        this.parentFolder = parentFolder == null ? "" : parentFolder;
        this.childSubdir = childSubdir == null ? "" : childSubdir;
        this.isSysInclude = sysInclude;
        this.isFolder = isFolder;
        assert item != null;
        this.item = item;
        this.supportInstantSubst = supportInstantSubst;
    }
    
    public static CsmIncludeCompletionItem createItem(int substitutionOffset, 
                                                    String relFileName, 
                                                    String dirPrefix, String childSubdir,
                                                    boolean sysInclude,
                                                    boolean highPriority,
                                                    boolean isFolder,
                                                    boolean supportInstantSubst) {
        int priority;
        if (isFolder) {
            if (highPriority) {
                priority = FOLDER_PRIORITY - SYS_VS_USR;
            } else {
                priority = FOLDER_PRIORITY + SYS_VS_USR;
            }
        } else {
            if (highPriority) {
                priority = FILE_PRIORITY - SYS_VS_USR;
            } else {
                priority = FILE_PRIORITY + SYS_VS_USR;
            }
        }
        String item = relFileName;
        return new CsmIncludeCompletionItem(substitutionOffset, priority, 
                dirPrefix, childSubdir, item, sysInclude, isFolder, supportInstantSubst);
    }
    
    public String getItemText() {
        return item;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, isFolder() ? SLASH : null);
            if (this.isFolder()) {
                Completion.get().showCompletion();
            }
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            JTextComponent component = (JTextComponent)evt.getSource();
            BaseDocument doc = (BaseDocument)component.getDocument();
            int caretOffset = component.getSelectionEnd();
            int len = caretOffset - substitutionOffset;
            if (len < 0) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            switch (evt.getKeyChar()) {
                case '>':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                    break;
                case '"':
                    doc.atomicLock();
                    try {
                        if (len > 0) {
                            String toReplace = doc.getText(substitutionOffset, len);
                            if (toReplace.startsWith("\"") && len > 1) { // NOI18N
                                Completion.get().hideDocumentation();
                                Completion.get().hideCompletion();
                                break;
                            }
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        doc.atomicUnlock();
                    }
                    break;
                case '/':
                    if (len > 1 && isFolder()) {
                        Completion.get().hideDocumentation();
                        Completion.get().hideCompletion();
                        substituteText(component, substitutionOffset, len, SLASH);
                        evt.consume();                        
                        Completion.get().showCompletion();
                    }
                    break;
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
        if (supportInstantSubst) {
            defaultAction(component);
            return true;
        } else {
            return false;
        }
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(true), getRightText(false, File.separator), g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {        
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(true), PARENT_COLOR_TAG + getRightText(true, File.separator), g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.isFolder() ? "[D] ": "[F] "); // NOI18N
        out.append(this.isSysInclude() ? "<" : "\""); // NOI18N
        out.append(this.getLeftHtmlText(false));
        out.append(this.isSysInclude() ? ">" : "\""); // NOI18N
        out.append(" : "); // NOI18N
        out.append(this.getRightText(false, "/")); // NOI18N
        return out.toString();
    }
    
    public int getSortPriority() {
        return this.priority;
    }

    public CharSequence getSortText() {
        return item;
    }

    public CharSequence getInsertPrefix() {
        return item;
    }        
    
    protected ImageIcon getIcon() {
        return CsmImageLoader.getIncludeImageIcon(isSysInclude(),isFolder());
    }
    
    protected String getLeftHtmlText(boolean html) {
        return (html ? (isFolder() ? "<i>" : "") : "") + this.getItemText(); // NOI18N
    }
    
    protected String getRightText(boolean shrink, String separator) {
        StringBuilder builder = new StringBuilder(this.getParentFolder());
        builder.append(separator).append(getChildSubdir());
        int len = builder.length();
        if (shrink && len > MAX_DISPLAYED_DIR_LENGTH) {
            
            StringBuilder reverse = new StringBuilder(builder).reverse();
            int st = builder.indexOf(separator);
            if (st < 0) {
                st = 0;
            } else {
                st++;
            }
            int end = 0;
            while (reverse.charAt(end) == separator.charAt(0)) {
                end++;
            }
            int firstSlash = NR_DISPLAYED_FRONT_DIRS > 0 ? Integer.MAX_VALUE : -1;
            for (int i = NR_DISPLAYED_FRONT_DIRS; i > 0 && firstSlash > 0; i--) {
                firstSlash = builder.indexOf(separator, st);
                st = firstSlash + 1;
            }
            int lastSlash = NR_DISPLAYED_TRAILING_DIRS > 0 ? Integer.MAX_VALUE : -1;
            for (int i = NR_DISPLAYED_TRAILING_DIRS; i > 0 && lastSlash > 0; i--) {
                lastSlash = reverse.indexOf(separator, end);
                end = lastSlash + 1;
            }
            if (lastSlash > 0 && firstSlash > 0) {
                lastSlash = len - lastSlash;
                if (firstSlash < lastSlash - 1) {
                    builder.replace(firstSlash, lastSlash - 1, "..."); // NOI18N
                }
            }
        }
        return builder.toString(); // NOI18N
    }
    
    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getItemText();
        if (text != null) {
            TokenItem token = null;
            NbCsmSyntaxSupport sup = (NbCsmSyntaxSupport) Utilities.getSyntaxSupport(c).get(NbCsmSyntaxSupport.class);
            if (sup != null) {
                token = sup.getTokenItem(offset);
            }
            if (toAdd != null) {
                text += toAdd;
            }
            String pref = QUOTE;
            String post = QUOTE;
            if (token != null) {
                switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.WHITESPACE_ID:
                case CCTokenContext.IDENTIFIER_ID:
                    pref = this.isSysInclude ? SYS_OPEN : QUOTE;
                    post = this.isSysInclude ? SYS_CLOSE : QUOTE;
                    break;
                case CCTokenContext.USR_INCLUDE_ID:
                case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                    pref = QUOTE;
                    post = QUOTE;
                    len = (token.getOffset() + token.getImage().length()) - offset;
                    break;
                case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                case CCTokenContext.SYS_INCLUDE_ID:
                    pref = SYS_OPEN;
                    post = SYS_CLOSE;
                    len = (token.getOffset() + token.getImage().length()) - offset;
                    break;
                }
            }
            // Update the text
            doc.atomicLock();
            try {
                String parent = getChildSubdir();
                if (parent.length() > 0 && !parent.endsWith(SLASH)) {
                    parent += SLASH;
                }
                text = pref + parent + text + post;               
                Position position = doc.createPosition(offset);
                Position lastPosition = doc.createPosition(offset + len);
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
                if (c != null && this.isFolder()) {
                    c.setCaretPosition(lastPosition.getOffset() - 1);
                }                
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
    }

    protected boolean isFolder() {
        return isFolder;
    }

    protected String getParentFolder() {
        return parentFolder;
    }

    protected String getChildSubdir() {
        return childSubdir;
    }    
    
    protected boolean isSysInclude() {
        return isSysInclude;
    }
}
