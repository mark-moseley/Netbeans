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
package org.netbeans.api.editor;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Dusan Balek
 */
public final class DialogBinding {

    /**
     * Bind given component and given file together.
     * @param fileObject to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original file
     * @param component component to bind
     */
    public static void bindComponentToFile(final FileObject fileObject, int offset, int length, final JTextComponent component) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        Parameters.notNull("component", component); //NOI18N
        if (!fileObject.isValid() || !fileObject.isData())
            return;
        if (component instanceof JEditorPane)
            ((JEditorPane)component).setEditorKit(MimeLookup.getLookup(fileObject.getMIMEType()).lookup(EditorKit.class));
        Document doc = component.getDocument();
        doc.putProperty("mimeType", "text/x-dialog-binding"); //NOI18N
        InputAttributes inputAttributes = new InputAttributes();
        Language language = MimeLookup.getLookup("text/x-dialog-binding").lookup(Language.class); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.fileObject", fileObject, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.offset", offset, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.length", length, true); //NOI18N
        doc.putProperty(InputAttributes.class, inputAttributes);
    }
}
