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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class HtmlGSFParser extends Parser {

    private HtmlParserResult lastResult;

    // ------------------------------------------------------------------------
    // o.n.m.p.spi.Parser implementation
    // ------------------------------------------------------------------------
    public
    @Override
    void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        lastResult = parse(snapshot, event);
    }

    public
    @Override
    Result getResult(Task task) throws ParseException {
        assert lastResult != null : "getResult() called prior parse()"; //NOI18N
        return lastResult;
    }

    public
    @Override
    void cancel() {
        //todo
    }

    public
    @Override
    void addChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    public
    @Override
    void removeChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    public HtmlGSFParser() {
        NbReaderProvider.setupReaders(); //initialize DTD registry
    }
    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER.j2ee.parser"); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(HtmlGSFParser.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    private HtmlParserResult parse(Snapshot snapshot, SourceModificationEvent event) {

        List<SyntaxElement> elements = SyntaxParser.parseImmutableSource(snapshot.getText());
        HtmlParserResult result = new HtmlParserResult(this, snapshot, elements);

        if (TIMERS.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "HTML parse result"); // NOI18N
            rec.setParameters(new Object[]{result});
            TIMERS.log(rec);
        }

        return result;
    }


//xxx looks like PositionManager has been deleted in CSL but I belive we need this
//to resolve elements between parser results

//    public PositionManager getPositionManager() {
//        return this;
//    }
//
//    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle handle) {
//      ElementHandle object = resolveHandle(info, handle);
//        if (object instanceof HtmlElementHandle) {
//            ParserResult presult = info.getEmbeddedResults(HTMLKit.HTML_MIME_TYPE).iterator().next();
//            final TranslatedSource source = presult.getTranslatedSource();
//            AstNode node = ((HtmlElementHandle) object).node();
//            return new OffsetRange(AstUtils.documentPosition(node.startOffset(), source), AstUtils.documentPosition(node.endOffset(), source));
//
//        } else {
//            throw new IllegalArgumentException("Foreign element: " + object + " of type " +
//                    ((object != null) ? object.getClass().getName() : "null")); //NOI18N
//        }
//    }
//
//    public static ElementHandle resolveHandle(CompilationInfo info, ElementHandle oldElementHandle) {
//        if (oldElementHandle instanceof HtmlElementHandle) {
//           HtmlElementHandle element = (HtmlElementHandle)oldElementHandle;
//            AstNode oldNode = element.node();
//
//            AstNode oldRoot = AstNodeUtils.getRoot(oldNode);
//
//            HtmlParserResult newResult = (HtmlParserResult)info.getEmbeddedResult(HTMLKit.HTML_MIME_TYPE, 0);
//
//            AstNode newRoot = newResult.root();
//
//            if (newRoot == null) {
//                return null;
//            }
//
//            // Find newNode
//            AstNode newNode = find(oldRoot, oldNode, newRoot);
//
//            if (newNode != null) {
//                return new HtmlElementHandle(newNode, info.getFileObject());
//            }
//        }
//
//        return null;
//    }
//
//    private static AstNode find(AstNode oldRoot, AstNode oldObject, AstNode newRoot) {
//        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
//        if (oldRoot == oldObject) {
//            // Found it!
//            return newRoot;
//        }
//
//        List<AstNode> oChildren = oldRoot.children();
//        List<AstNode> nChildren = newRoot.children();
//
//        for(int i = 0; i < oChildren.size(); i++) {
//
//            AstNode oCh = oChildren.get(i);
//
//            if(i == nChildren.size()) {
//                //no more new children
//                return null;
//            }
//            AstNode nCh = nChildren.get(i);
//
//            if (oCh == oldObject) {
//                // Found it!
//                return nCh;
//            }
//
//            // Recurse
//            AstNode match = find(oCh, oldObject, nCh);
//
//            if (match != null) {
//                return match;
//            }
//
//        }
//
//        return null;
//    }
//
}
