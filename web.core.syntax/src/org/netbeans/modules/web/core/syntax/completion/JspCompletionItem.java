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
package org.netbeans.modules.web.core.syntax.completion;

import java.awt.Font;
import java.awt.Graphics;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.swing.ImageIcon;
import javax.swing.text.Caret;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.core.syntax.SyntaxElement;
import org.netbeans.modules.web.core.syntax.spi.AutoTagImporterProvider;
import org.netbeans.spi.editor.completion.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Code completion result item base class
 *
 * @author  Dusan Balek, Marek Fukala
 */
public class JspCompletionItem implements CompletionItem {

    private static final int DEFAULT_SORT_PRIORITY = 10;
    public static final String COMPLETION_SUBSTITUTE_TEXT = "completion-substitute-text"; //NOI18N

    //----------- Factory methods --------------
    public static JspCompletionItem createJspAttributeValueCompletionItem(String value, int substitutionOffset) {
        return new AttributeValue(value, substitutionOffset);
    }

    public static JspCompletionItem createFileCompletionItem(String value, int substitutionOffset, Color color, ImageIcon icon) {
        return new FileAttributeValue(value, substitutionOffset, color, icon);
    }

    public static JspCompletionItem createPrefixTag(String prefix, int substitutionOffset) {
        return new PrefixTag(prefix, substitutionOffset);
    }

    public static JspCompletionItem createPrefixTag(String prefix, int substitutionOffset, TagInfo ti) {
        return new PrefixTag(prefix, substitutionOffset, ti);
    }

    public static JspCompletionItem createPrefixTag(String prefix, int substitutionOffset, TagInfo ti, SyntaxElement.Tag tagSyntaxElement) {
        return new PrefixTag(prefix, substitutionOffset, ti, tagSyntaxElement);
    }

    public static JspCompletionItem createAttribute(String name, int substitutionOffset) {
        return new Attribute(name, substitutionOffset);
    }

    public static JspCompletionItem createAttribute(int substitutionOffset, TagAttributeInfo tai) {
        return new Attribute(substitutionOffset, tai);
    }

    public static JspCompletionItem createTag(String name, int substitutionOffset) {
        return new Tag(name, substitutionOffset);
    }

    public static JspCompletionItem createTag(String name, int substitutionOffset, TagInfo tagInfo) {
        return new Tag(name, substitutionOffset, tagInfo);
    }

    public static JspCompletionItem createDelimiter(String name, int substitutionOffset) {
        return new Delimiter(name, substitutionOffset);
    }

    public static JspCompletionItem createDirective(String name, int substitutionOffset) {
        return new Directive(name, substitutionOffset);
    }

    public static JspCompletionItem createDirective(String name, int substitutionOffset, TagInfo tagInfo) {
        return new Directive(name, substitutionOffset, tagInfo);
    }

    public static JspCompletionItem createELImplicitObject(String name, int substitutionOffset, int type) {
        return new ELImplicitObject(name, substitutionOffset, type);
    }

    public static JspCompletionItem createELBean(String name, int substitutionOffset, String type) {
        return new ELBean(name, substitutionOffset, type);
    }

    public static JspCompletionItem createELProperty(String name, int substitutionOffset, String type) {
        return new ELProperty(name, substitutionOffset, type);
    }

    public static JspCompletionItem createELFunction(String name, int substitutionOffset, String type, String prefix, String parameters) {
        return new ELFunction(name, substitutionOffset, type, prefix, parameters);
    }    //------------------------------------------
    protected int substitutionOffset;
    protected String text,  help;
    protected boolean shift;

    protected JspCompletionItem(String text, int substituteOffset) {
        this.substitutionOffset = substituteOffset;
        this.text = text;
    }

    protected JspCompletionItem(String text, int substituteOffset, String help) {
        this(text, substituteOffset);
        this.help = help;
    }

    public String getItemText() {
        return text;
    }

    public int getSortPriority() {
        return DEFAULT_SORT_PRIORITY;
    }

    public CharSequence getSortText() {
        return getItemText();
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    public void defaultAction(JTextComponent component) {
        if (component != null) {
            if (!shift) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            int caretOffset = component.getSelectionEnd();
            substituteText(component, caretOffset - substitutionOffset);
        }

    }

    protected int getMoveBackLength() {
        return 0; //default
    }

    /** 
     * Subclasses may override to customize the completed text 
     * if they do not want to override the substituteText method. 
     */
    protected String getSubstituteText() {
        return getItemText();
    }

    protected boolean substituteText(JTextComponent c, int len) {
        return substituteText(c, len, getMoveBackLength());
    }

    protected boolean substituteText(final JTextComponent c, final int len, int moveBack) {
        return substituteText(c, getSubstituteText(), len, moveBack);
    }

    protected boolean substituteText(final JTextComponent c, final String substituteText, final int len, int moveBack) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final boolean[] result = new boolean[1];
        result[0] = true;

        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(substitutionOffset, (doc.getLength() - substitutionOffset) < substituteText.length() ? (doc.getLength() - substitutionOffset) : substituteText.length());
                    if (!substituteText.substring(0, substituteText.length() - 1).equals(currentText)) {
                        //remove common part
                        doc.remove(substitutionOffset, len);
                        doc.insertString(substitutionOffset, substituteText, null);
                    } else {
                        c.setCaretPosition(c.getCaret().getDot() + substituteText.length() - len);
                    }
                } catch (BadLocationException ex) {
                    result[0] = false;
                }

            }
        });

        //format the inserted text
        reindent(c);

        if (moveBack != 0) {
            Caret caret = c.getCaret();
            int dot = caret.getDot();
            caret.setDot(dot - moveBack);
        }

        return result[0];
    }

    private void reindent(JTextComponent component) {

        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int dotPos = component.getCaretPosition();
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        int startOffset = Utilities.getRowStart(doc, dotPos);
                        int endOffset = Utilities.getRowEnd(doc, dotPos);
                        indent.reindent(startOffset, endOffset);
                    } catch (BadLocationException ex) {
                        //ignore
                        }
                }
            });
        } finally {
            indent.unlock();
        }

    }

    public boolean instantSubstitution(JTextComponent component) {
        if (component != null) {
            try {
                int caretOffset = component.getSelectionEnd();
                if (caretOffset > substitutionOffset) {
                    String currentText = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                    if (!getInsertPrefix().toString().startsWith(currentText)) {
                        return false;
                    }
                }
            } catch (BadLocationException ble) {
            }
        }
        defaultAction(component);
        return true;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
    }

    protected String getLeftHtmlText() {
        return getItemText();
    }

    protected String getRightHtmlText() {
        return null;
    }

    /** Returns a url or null, if the help is not URL or the help is not defined.
     */
    public URL getHelpURL() {
        if (help == null || help.equals("")) {
            return null;
        }
        try {
            return new URL(help);
        } catch (java.io.IOException e) {
        }
        return null;
    }

    /** Returns help for the item. It can be only url. If the item doesn't have a help
     *  than returns null. The class can overwrite this method and compounds the help realtime.
     */
    public String getHelp() {
        return help;
    }

    /** Returns whether the item has a help.
     */
    public boolean hasHelp() {
        return (help != null && help.length() > 0);
    }

    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new JspCompletionProvider.DocQuery(this));
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    //------------------------------------------------------------------------------
    /** 
     * Completion item representing a JSP tag including its prefix eg. <jsp:useBean />
     */
    public static class PrefixTag extends JspCompletionItem {

        private TagInfo tagInfo;
        private boolean isEmpty = false;
        private boolean hasAttributes = false;

        PrefixTag(String text, int substitutionOffset) {
            super(text, substitutionOffset);
        }

        PrefixTag(String prefix, int substitutionOffset, TagInfo ti, SyntaxElement.Tag set) {
            super(prefix + ":" + ti.getTagName(), substitutionOffset, ti != null ? ti.getInfoString() : null); // NOI18N
            tagInfo = ti;
            if ((tagInfo != null) &&
                    (tagInfo.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY))) {
                isEmpty = true;
            }
            //test whether this tag has some attributes
            if (set != null) {
                hasAttributes = !(set.getAttributes().size() == 0);
            }
        }

        PrefixTag(String prefix, int substitutionOffset, TagInfo ti) {
            this(prefix, substitutionOffset, ti, (SyntaxElement.Tag) null);
        }

        public TagInfo getTagInfo() {
            return tagInfo;
        }

        @Override
        public String getHelp() {
            URL url = super.getHelpURL();
            if (url != null) {
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                String helpText = constructHelp(url);
                if (first > 0) {
                    int last = surl.lastIndexOf('#') + 1;
                    String from = surl.substring(first, last - 1);
                    String to = surl.substring(last);
                    first = helpText.indexOf(from);
                    if (first > 0) {
                        first = first + from.length() + 2;
                        if (first < helpText.length()) {
                            helpText = helpText.substring(first);
                        }
                    }
                    last = helpText.indexOf(to);
                    if (last > 0) {
                        helpText = helpText.substring(0, last);
                    }
                    return helpText;
                }

                helpText = helpText.substring(helpText.indexOf("<h2>")); //NOI18N
                helpText = helpText.substring(0, helpText.lastIndexOf("<h4>"));//NOI18N
                return helpText;
            }
            return constructHelp(tagInfo);
        }

        @Override //method called from default action 
        public boolean substituteText(JTextComponent c, int len) {
            String suffix = isEmpty ? "/>" : ">"; // NOI18N

            if (hasAttributes) {
                suffix = "";
            }

            if (!getItemText().startsWith("/")) {  // NOI18N
                if (!shift) {
                    return super.substituteText(c, len, 0);
                }  // NOI18N

                boolean hasAttrs = true;
                if (tagInfo != null) {
                    TagAttributeInfo[] tAttrs = tagInfo.getAttributes();
                    hasAttrs = (tAttrs != null) ? (tAttrs.length > 0) : true;
                }
                if (hasAttrs) {
                    return substituteText(c,
                            getItemText() + (hasAttributes ? "" : " ") + suffix,
                            len,
                            suffix.length());
                } // NOI18N
                else {
                    return substituteText(c, getItemText() + suffix, len, 0);
                }
            } else // closing tag
            {
                return substituteText(c, getItemText().substring(1) + ">", len, 0);
            }  // NOI18N

        }

        @Override
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);

            if (component == null) {
                return;
            }

            //handle auto tag imports
            final BaseDocument doc = (BaseDocument) component.getDocument();
            doc.runAtomic(new Runnable() {

                public void run() {
                    String mimeType = NbEditorUtilities.getFileObject(doc).getMIMEType();
                    Lookup mimeLookup = MimeLookup.getLookup(MimePath.get(mimeType));
                    Collection<? extends AutoTagImporterProvider> providers = mimeLookup.lookup(new Lookup.Template<AutoTagImporterProvider>(AutoTagImporterProvider.class)).allInstances();
                    if (providers != null) {
                        for (AutoTagImporterProvider provider : providers) {
                            provider.importLibrary(doc, tagInfo.getTagLibrary().getPrefixString(), tagInfo.getTagLibrary().getURI());
                        }
                    }
                }
            });

        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>&lt;<font color=#0000ff><b>" + getItemText() + "</font>" +
                    (isEmpty ? "/&gt;" : "&gt;</b>");
        }
    }

    /** Item representing a JSP tag (without prefix). */
    public static class Tag extends JspCompletionItem {

        private TagInfo ti = null;

        public Tag(String text, int substitutionOffset) {
            super(text, substitutionOffset);
        }

        public Tag(String text, int substitutionOffset, TagInfo ti) {
            super(text, substitutionOffset, ti != null ? ti.getInfoString() : null);
            this.ti = ti;
        }

        public TagInfo getTagInfo() {
            return ti;
        }

        @Override
        public String getHelp() {
            URL url = super.getHelpURL();
            if (url != null) {
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                String helpText = constructHelp(url);
                if (first > 0) {
                    int last = surl.lastIndexOf('#') + 1;
                    String from = surl.substring(first, last - 1);
                    String to = surl.substring(last);
                    first = helpText.indexOf(from);
                    if (first > 0) {
                        first = first + from.length() + 2;
                        if (first < helpText.length()) {
                            helpText = helpText.substring(first);
                        }
                    }
                    last = helpText.indexOf(to);
                    if (last > 0) {
                        helpText = helpText.substring(0, last);
                    }
                    return helpText;
                }

                helpText = helpText.substring(helpText.indexOf("<h2>")); //NOI18N
                helpText = helpText.substring(0, helpText.lastIndexOf("<h4>"));//NOI18N
                return helpText;
            }
            return constructHelp(ti);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>&lt;<font color=#0000ff>" + getItemText() + "</font>&gt;</b>";
        }

        @Override
        protected String getSubstituteText() {
            return getItemText().startsWith("/") ? getItemText().substring(1) + ">" : getItemText() + " ";
        }
    }

    public static class Delimiter extends JspCompletionItem {

        private static final int DELIMITER_SORT_PRIORITY = 4; //before directives!

        Delimiter(String name, int substitutionOffset) {
            super(name, substitutionOffset);
        }

        @Override
        public int getSortPriority() {
            return DELIMITER_SORT_PRIORITY;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>" + escape(getItemText()) + "</b>";
        }
    }

    /** Item representing a JSP tag (without prefix). */
    static class Directive extends JspCompletionItem {

        private static final int DIRECTIVE_SORT_PRIORITY = 5;
        TagInfo tagInfo;

        Directive(String text, int substitutionOffset) {
            super(text, substitutionOffset);
            tagInfo = null;
        }

        Directive(String text, int substitutionOffset, TagInfo tagInfo) {
            super(text, substitutionOffset, tagInfo != null ? tagInfo.getInfoString() : null);
            this.tagInfo = tagInfo;
        }

        @Override
        public int getSortPriority() {
            return DIRECTIVE_SORT_PRIORITY;
        }

        @Override
        public String getHelp() {
            if (getHelpURL() != null) {
                String helpText = constructHelp(getHelpURL());
                if (helpText != null) {
                    helpText = helpText.substring(helpText.indexOf("<h2>")); //NOI18N
                    helpText = helpText.substring(0, helpText.lastIndexOf("<h4>"));//NOI18N
                    return helpText;
                }
            }
            return constructHelp(tagInfo);
        }

        public TagInfo getTagInfo() {
            return tagInfo;
        }

        @Override
        protected String getSubstituteText() {
            return "<%@" + getItemText() + "  %>";    // NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return 3; //jump before closing symbol %>
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>&lt;%@<font color=#0000ff>" +
                    getItemText() + "</font>%&gt;</b>";
        }
        
    }

    /** Item representing an attribute of a  JSP tag or directive. */
    public static class Attribute extends JspCompletionItem {

        private TagAttributeInfo tagAttributeInfo;
        private boolean required;

        Attribute(String text, int substitutionOffset) {
            super(text, substitutionOffset);
            tagAttributeInfo = null;
            required = false;
        }

        Attribute(int substitutionOffset, TagAttributeInfo tai) {
            super(tai.getName(), substitutionOffset, tai.getTypeName() == null && tai.isFragment() ? "fragment" : tai.getTypeName());
            required = tai.isRequired();
            tagAttributeInfo = tai;
        }

        @Override
        protected int getMoveBackLength() {
            //always do the shift => jump into the attribute value between the quotation marks
            return 1;
        }

        @Override
        protected String getSubstituteText() {
            return getItemText() + "=\"\"";
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + (required ? "ff0000" : "00aa00") + ">" + getItemText() + "</font>";
        }

        @Override
        public String getHelp() {
            URL url = super.getHelpURL();
            if (url != null) {
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                int last = surl.lastIndexOf('#') + 1;
                String from;

                if (first < last) {
                    from = surl.substring(first, last - 1);
                } else {
                    from = surl.substring(first);
                }
                String helpText = constructHelp(getHelpURL());
                first = helpText.indexOf(from);
                if (first > 0) {
                    first = first + from.length() + 2;
                    if (first < helpText.length()) {
                        helpText = helpText.substring(first);
                    }
                }

                String to = surl.substring(last);
                last = helpText.indexOf(to);
                if (last > 0) {
                    helpText = helpText.substring(0, last);
                }
                return helpText;
            }
            if (tagAttributeInfo != null) {
                StringBuffer helpText = new StringBuffer();
                helpText.append("<table border=\"0\"><tr><td><b>Name:</b></td><td>");  //NOI18N
                helpText.append(tagAttributeInfo.getName());                            //NOI18N
                helpText.append("</td></tr><tr><td><b>Required:</b></td><td>");         //NOI18N
                helpText.append(tagAttributeInfo.isRequired());                         //NOI18N
                helpText.append("</td></tr><tr><td><b>Request-time:</b></td><td>");     //NOI18N
                helpText.append(tagAttributeInfo.canBeRequestTime());                   //NOI18N
                helpText.append("</td></tr><tr><td><b>Fragment:</b></td><td>");         //NOI18N
                helpText.append(tagAttributeInfo.isFragment());                         //NOI18N
                helpText.append("</td></tr></table>");                                  //NOI18N
                return helpText.toString();
            }
            return super.getHelp();
        }

        public URL getHelpURL() {
            URL url = super.getHelpURL();
            if (url != null) {
                String surl = url.toString();
                int index = surl.lastIndexOf('#'); // NOI18N
                if (index > 0) {
                    surl = surl.substring(0, index);
                }
                try {
                    url = new URL(surl);
                } catch (MalformedURLException e) {
                }
            }
            return url;
        }
    }

    /** Item representing a JSP attribute value. */
    static class AttributeValue extends JspCompletionItem {

        public AttributeValue(String value, int anchor) {
            super(value, anchor);
        }
    }

    /** Item representing a File attribute */
    public static class FileAttributeValue extends JspCompletionItem {

        private javax.swing.ImageIcon icon;
        private Color color;

        FileAttributeValue(String text, int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super(text, substitutionOffset);
            this.color = color;
            this.icon = icon;
        }

        @Override
        protected ImageIcon getIcon() {
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color='" + hexColorCode(color) + "'>" + getItemText() + "</font>"; //NOI18N
        }
    }

    private static String constructHelp(URL url) {
        if (url == null) {
            return null;
        }
        try {
            InputStream is = url.openStream();
            byte buffer[] = new byte[1000];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count = 0;
            do {
                count = is.read(buffer);
                if (count > 0) {
                    baos.write(buffer, 0, count);
                }
            } while (count > 0);

            is.close();
            String text = baos.toString();
            baos.close();
            return text;
        } catch (java.io.IOException e) {
            return null;
        }
    }

    private static String constructHelp(TagInfo tagInfo) {
        if (tagInfo == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("<h2>").append(getString("LBL_TagName")).append(" "); //NOI18N
        sb.append(tagInfo.getTagName()).append("</h2>"); // NOI18N
        String val = tagInfo.getDisplayName();
        if (val != null) {
            sb.append("<p>").append(getString("LBL_DisplayName")); //NOI18N
            sb.append("<i>").append(val).append("</i>"); // NOI18N
        }
        val = tagInfo.getInfoString();
        if (val != null) {
            sb.append("<hr>").append(val).append("<hr>");
        }                 // NOI18N

        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"2\"><font size=\"+2\"><b>");// NOI18N
        sb.append("Tag Information</b></font></td></tr>");// NOI18N
        sb.append("<tr><td>Tag Class</td><td>");// NOI18N
        if (tagInfo.getTagClassName() != null && !tagInfo.getClass().equals("")) {
            sb.append(tagInfo.getTagClassName());
        } else {
            sb.append("<i>None</i>");
        }// NOI18N
        sb.append("</td></tr><tr><td>Body Content</td><td>");// NOI18N
        sb.append(tagInfo.getBodyContent());
        sb.append("</td></tr><tr><td>Display Name</td><td>");// NOI18N
        if (tagInfo.getDisplayName() != null && !tagInfo.getDisplayName().equals("")) {
            sb.append(tagInfo.getDisplayName());
        } else {
            sb.append("<i>None</i>");
        }// NOI18N
        sb.append("</td></tr></table><br>");// NOI18N

        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"3\"><font size=\"+2\"><b>Attributes</b></font></td></tr>");// NOI18N

        TagAttributeInfo[] attrs = tagInfo.getAttributes();
        if (attrs != null && attrs.length > 0) {
            sb.append("<tr><td><b>Name</b></td><td><b>Required</b></td><td><b>Request-time</b></td></tr>");// NOI18N
            for (int i = 0; i < attrs.length; i++) {
                sb.append("<tr><td>");         // NOI18N
                sb.append(attrs[i].getName());
                sb.append("</td><td>");                     // NOI18N
                sb.append(attrs[i].isRequired());
                sb.append("</td><td>");                     // NOI18N
                sb.append(attrs[i].canBeRequestTime());
                sb.append("</td></tr>");                    // NOI18N
            }
        } else {
            sb.append("<tr><td colspan=\"3\"><i>No Attributes Defined.</i></td></tr>");// NOI18N
        }
        sb.append("</table><br>");// NOI18N
        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"4\"><font size=\"+2\"><b>Variables</b></font></td></tr>");// NOI18N
        TagVariableInfo[] variables = tagInfo.getTagVariableInfos();
        if (variables != null && variables.length > 0) {
            sb.append("<tr><td><b>Name</b></td><td><b>Type</b></td><td><b>Declare</b></td><td><b>Scope</b></td></tr>");// NOI18N
            for (int i = 0; i < variables.length; i++) {
                sb.append("<tr><td>");         // NOI18N
                if (variables[i].getNameGiven() != null && !variables[i].getNameGiven().equals("")) {// NOI18N
                    sb.append(variables[i].getNameGiven());
                } else {
                    if (variables[i].getNameFromAttribute() != null && !variables[i].getNameFromAttribute().equals("")) // NOI18N
                    {
                        sb.append("<i>From attribute '").append(variables[i].getNameFromAttribute()).append("'</i>");
                    }// NOI18N
                    else {
                        sb.append("<i>Unknown</i>");
                    }  // NOI18N
                }
                sb.append("</td><td><code>");                     // NOI18N
                if (variables[i].getClassName() == null || variables[i].getClassName().equals("")) {
                    sb.append("java.lang.String");
                }// NOI18N
                else {
                    sb.append(variables[i].getClassName());
                }
                sb.append("</code></td></tr>");                    // NOI18N
                sb.append("</td><td>");                     // NOI18N
                sb.append(variables[i].getDeclare());
                sb.append("</td></tr>");                    // NOI18N
                sb.append("</td><td>");                     // NOI18N
                switch (variables[i].getScope()) {
                    case VariableInfo.AT_BEGIN:
                        sb.append("AT_BEGIN");
                        break;// NOI18N
                    case VariableInfo.AT_END:
                        sb.append("AT_END");
                        break;// NOI18N
                    default:
                        sb.append("NESTED");// NOI18N
                    }
                sb.append("</td></tr>");                    // NOI18N
            }
        } else {
            sb.append("<tr><td colspan=\"4\"><i>No Variables Defined.</i></td></tr>");// NOI18N
        }
        sb.append("</table><br>");// NOI18N
        return sb.toString();
    }

    private static String getString(String key) {
        return NbBundle.getMessage(JspCompletionItem.class, key);
    }
    // ------------------------ EL Items ------------------------------------------
    public interface ELItem {
        //why we need this????
        };

    public static class ELImplicitObject extends JspCompletionItem implements ELItem {

        private static final String OBJECT_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/class_16.png"; //NOI18N
        private static final String MAP_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/map_16.png";      //NOI18N
        int type;

        ELImplicitObject(String text, int substitutionOffset, int type) {
            super(text, substitutionOffset);
            this.type = type;
        }

        @Override
        public int getSortPriority() {
            return 15;
        }

        @Override
        protected ImageIcon getIcon() {
            ImageIcon icon = null;
            switch (type) {
                case ELImplicitObjects.OBJECT_TYPE:
                    icon = new ImageIcon(org.openide.util.Utilities.loadImage(OBJECT_PATH));
                    break;
                case ELImplicitObjects.MAP_TYPE:
                    icon = new ImageIcon(org.openide.util.Utilities.loadImage(MAP_PATH));
                    break;
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#0000ff>" + getItemText() + "</font>";
        }

        @Override
        public String getItemText() {
            String result = text;
            if (type == org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects.MAP_TYPE) {
                result = result + "[]";
            }
            return result;    //NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return type == org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects.MAP_TYPE ? 1 : 0;
        }
    }

    public static class ELBean extends JspCompletionItem implements ELItem {

        private static final String BEAN_NAME_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String BEAN_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/bean_16.png";    //NOI18N
        private String type;

        public ELBean(String text, int substitutionOffset, String type) {
            super(text, substitutionOffset);
            if (type.lastIndexOf('.') > -1) {
                this.type = type.substring(type.lastIndexOf('.') + 1);
            } else {
                this.type = type;
            }
        }

        @Override
        public int getSortPriority() {
            return 10;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + BEAN_NAME_COLOR + ">" + getItemText() + "</font>";
        }

        @Override
        protected String getRightHtmlText() {
            return this.type;
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(org.openide.util.Utilities.loadImage(BEAN_PATH));
        }
    }

    public static class ELProperty extends ELBean {

        private static final String PROPERTY_NAME_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String PROPERTY_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/property_16.png"; //NOI18N

        public ELProperty(String text, int substitutionOffset, String type) {
            super(text, substitutionOffset, type);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + PROPERTY_NAME_COLOR + ">" + getItemText() + "</font>";
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(org.openide.util.Utilities.loadImage(PROPERTY_PATH));
        }
    }

    public static class ELFunction extends ELBean {

        private static final String PREFIX_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String FUNCTION_NAME_COLOR = hexColorCode(Color.black);
        private static final String PARAMETER_COLOR = hexColorCode(Color.black);
        private static final String ICON_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/function_16.png";
        private String prefix;
        private String parameters;

        public ELFunction(String name, int substitutionOffset, String type, String prefix, String parameters) {
            super(name, substitutionOffset, type);
            this.prefix = prefix;
            this.parameters = parameters;
        }

        @Override
        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append("<font color=#" + PREFIX_COLOR + "><b>" + prefix + "</b></font>");
            lText.append("<font color=#" + FUNCTION_NAME_COLOR + "><b>" + ":" + getItemText() + "</b></font>");
            lText.append("<font color=#" + PARAMETER_COLOR + ">" + "(" + "</b></font>");
            if (parameters != null) {
                lText.append("<font color=#" + PARAMETER_COLOR + ">" + parameters + "</b></font>");
            }
            lText.append("<font color=#" + PARAMETER_COLOR + ">" + ")" + "</b></font>");
            return lText.toString();

        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(org.openide.util.Utilities.loadImage(ICON_PATH));
        }

        @Override
        public int getSortPriority() {
            return 12;
        }

        @Override
        public String getItemText() {
            return prefix + ":" + super.getItemText() + "()";    //NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return 1;
        }
    }

    public static final String hexColorCode(Color c) {
        return Integer.toHexString(c.getRGB()).substring(2);
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {
            }
        }
        return s;
    }
}
