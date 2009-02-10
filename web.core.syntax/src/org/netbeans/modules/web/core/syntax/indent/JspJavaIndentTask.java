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
package org.netbeans.modules.web.core.syntax.indent;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.formatting.JspJavaFormatter;
import org.openide.util.Exceptions;

public class JspJavaIndentTask implements IndentTask {

    private Context context;
    
    JspJavaIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        ExtFormatter formatter = new JspJavaFormatter(JavaKit.class);
        try {
            formatter.reformat((BaseDocument)context.document(), context.startOffset(), context.endOffset(), true);
        }catch(IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public ExtraLock indentLock() {
        return null;
    }  
}
