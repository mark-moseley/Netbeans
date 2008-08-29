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
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
public final class LegacyEssMatcher implements BracesMatcher, BracesMatcherFactory {

    private final MatcherContext context;
    private final ExtSyntaxSupport ess;
    
    private int [] block;
    
    public LegacyEssMatcher() {
        this(null, null);
    }

    private LegacyEssMatcher(MatcherContext context, ExtSyntaxSupport ess) {
        this.context = context;
        this.ess = ess;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int offset;

            if (context.isSearchingBackward()) {
                offset = context.getSearchOffset() - 1;
            } else {
                offset = context.getSearchOffset();
            }

            block = ess.findMatchingBlock(offset, false);
            return block == null ? null : new int [] { offset, offset };
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException {
        return block;
    }

    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    public BracesMatcher createMatcher(MatcherContext context) {
        Document d = context.getDocument();
        
        if (d instanceof BaseDocument) {
            SyntaxSupport ss = ((BaseDocument) d).getSyntaxSupport();
            if (ss instanceof ExtSyntaxSupport && ss.getClass() != ExtSyntaxSupport.class) {
                return new LegacyEssMatcher(context, (ExtSyntaxSupport) ss);
            }
        }
        
        return null;
    }

}
