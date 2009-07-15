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

package org.netbeans.modules.html.editor.gsf.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlExtension {

    private static final Collection<HtmlExtension> EXTENSIONS = new ArrayList<HtmlExtension>();
    /** register a new extension to the html support
     * TODO use mimelookup
     */
    public static void register(HtmlExtension extension) {
        synchronized (EXTENSIONS) {
            EXTENSIONS.add(extension);
        }
    }

    public static void unregister(HtmlExtension extension) {
        synchronized (EXTENSIONS) {
            EXTENSIONS.remove(extension);
        }
    }

    public static Collection<HtmlExtension> getRegisteredExtensions() {
        return EXTENSIONS;
    }

    //highlighting
    public abstract Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event);

    //completion
    public abstract List<CompletionItem> completeOpenTags(CompletionContext context);

    public abstract List<CompletionItem> completeCloseTags(CompletionContext context);

    public abstract List<CompletionItem> completeAttributes(CompletionContext context);

    public abstract List<CompletionItem> completeAttributeValue(CompletionContext context);


    public static class CompletionContext {

        private HtmlParserResult result;
        private int originalOffset;
        private int ccItemStartOffset;

        private int astoffset;
        private String preText;
        private AstNode currentNode;

        public CompletionContext(HtmlParserResult result, int originalOffset, int astoffset, int ccItemStartOffset, String preText) {
            this(result, originalOffset, astoffset, ccItemStartOffset, preText, null);
        }


        public CompletionContext(HtmlParserResult result, int originalOffset, int astoffset, int ccItemStartOffset, String preText, AstNode currentNode) {
            this.result = result;
            this.originalOffset = originalOffset;
            this.astoffset = astoffset;
            this.preText = preText;
            this.ccItemStartOffset = ccItemStartOffset;
            this.currentNode = currentNode;
        }

        public String getPrefix() {
            return preText;
        }

        public int getAstoffset() {
            return astoffset;
        }

        public int getOriginalOffset() {
            return originalOffset;
        }

        public int getCCItemStartOffset() {
            return ccItemStartOffset;
        }
        
        public HtmlParserResult getResult() {
            return result;
        }

        public AstNode getCurrentNode() {
            return currentNode;
        }

    }


}
