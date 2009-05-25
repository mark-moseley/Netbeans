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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.web.core.syntax.formatting.JspIndenter;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of IndentTask for text/x-jsp mimetype.
 *
 * @author Marek Fukala
 */
                                        

public class JspIndentTask implements IndentTask, Lookup.Provider {

    private JspIndenter indenter;
    private Lookup lookup;
    
    JspIndentTask(Context context) {
        indenter = new JspIndenter(context);
        lookup = Lookups.singleton(indenter.createFormattingContext());
    }

    public void reindent() throws BadLocationException {
        indenter.reindent();
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    public Lookup getLookup() {
        return lookup;
    }
}
