/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.indent.CppIndentTask;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class FormattingSupport {

    private FormattingSupport() {
    }

    public static String getLineIndentation(Document doc, int caretOffset) {
        int indent = new CppIndentTask(doc).getLineIndentation(caretOffset);
        StringBuilder sb = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            sb.append(' '); // NOI18N
        }
        return sb.toString();
    }

    public static CharSequence getFormattedText(Document doc, int caretOffset, CharSequence textToFormat) {
        if (doc == null) {
            System.err.println("original document is not specified for getFormattedText" + doc);
            return textToFormat;
        }
        String mimeType = (String) doc.getProperty(BaseDocument.MIME_TYPE_PROP);
        if (!MIMENames.isHeaderOrCppOrC(mimeType)) {
            System.err.println("Unsupported MIME type of document " + doc);
            return textToFormat;
        }
        String ext = MIMEExtensions.get(mimeType).getDefaultExtension();
        try {
            FileSystem fs = FileUtil.createMemoryFileSystem();
            FileObject root = fs.getRoot();
            String fileName = FileUtil.findFreeFileName(root, "cnd-format", ext);// NOI18N
            FileObject data = FileUtil.createData(root, fileName + "." + ext);// NOI18N
            Writer writer = new OutputStreamWriter(data.getOutputStream());
            try {
                writer.append(textToFormat);
                writer.flush();
            } finally {
                writer.close();
            }
            DataObject dob = DataObject.find(data);
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            if (ec != null) {
                StyledDocument fmtDoc = ec.openDocument();
                Reformat fmt = Reformat.get(fmtDoc);
                fmt.lock();
                try {
                    try {
                        fmt.reformat(0, fmtDoc.getLength());
                    } catch (BadLocationException ex) {
                    }
                } finally {
                    fmt.unlock();
                }
                SaveCookie save = dob.getCookie(SaveCookie.class);
                if (save != null) {
                    save.save();
                }
                final String text = fmtDoc.getText(0, fmtDoc.getLength());
                StringBuilder declText = new StringBuilder();
                final int len = text.length();
                int start = 0;
                int end = len - 1;
                // skip all whitespaces in the beginning and end of formatted text
                for (; start < len && Character.isWhitespace(text.charAt(start)); start++) {
                }
                for (; end > start && Character.isWhitespace(text.charAt(end)); end--) {
                }
                String indent = getLineIndentation(doc, caretOffset);
                // start with indented line
                declText.append(indent);// NOI18N
                for (int i = start; i <= end; i++) {
                    final char charAt = text.charAt(i);
                    if (charAt == '\n') { // NOI18N
                        // indented new line if not the last
                        if (i <= end) {
                            declText.append(charAt);
                            declText.append(indent);
                        }
                    } else {
                        declText.append(charAt);
                    }
                }
                return declText.toString();
            }
            data.delete();
        } catch (BadLocationException ex) {
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return textToFormat;
    }
}
