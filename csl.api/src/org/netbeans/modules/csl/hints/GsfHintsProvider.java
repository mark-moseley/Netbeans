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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.csl.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.text.NbDocument;



/**
 * This class is based on JavaHintsFactory in Retouche's org.netbeans.modules.java.hints
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * 
 * @author Jan Lahoda
 * @author leon chiver
 * @author Tor Norbye
 */
public final class GsfHintsProvider extends ParserResultTask<ParserResult> {
    
    public static Logger LOG = Logger.getLogger(GsfHintsProvider.class.getName()); // NOI18N
    
    private FileObject file;
    
    /**
     * Creates a new instance of GsfHintsProvider
     */
    GsfHintsProvider(FileObject file) {
        this.file = file;
    }
    
    private static final Map<org.netbeans.modules.csl.api.Severity, Severity> errorKind2Severity;
    
    static {
        errorKind2Severity = new EnumMap<org.netbeans.modules.csl.api.Severity, Severity>(org.netbeans.modules.csl.api.Severity.class);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.ERROR, Severity.ERROR);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.WARNING, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.WARNING, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.NOTE, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.OTHER, Severity.WARNING);
    }
    
    List<ErrorDescription> computeErrors(Document doc, ParserResult result, List<? extends Error> errors, List<ErrorDescription> descs) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "errors = " + errors);
        }
        
        for (Error/*Diagnostic*/ d : errors) {
            if (isCanceled()) {
                return null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "d = " + d);

                //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            }
            
            //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            
            //List<ErrorRule> rules = code2Rules.get(d.getKey());
            
            //if (LOG.isLoggable(Level.FINE)) {
                //LOG.log(Level.FINE, "code= " + d.getKey());
                //LOG.log(Level.FINE, "rules = " + rules);
            //}
            
            //int position = (int)d.getPosition();
            int astOffset = d.getStartPosition();
            int astEndOffset = d.getEndPosition();
            
            int position, endPosition;
            position = result.getSnapshot().getOriginalOffset(astOffset);
            if (position == -1) {
                continue;
            }
            endPosition = position+(astEndOffset-astOffset);
            
            LazyFixList ehm;
            
            //if (rules != null) {
            //    ehm = new CreatorBasedLazyFixList(info.getFileObject(), d.getKey(), (int)getPrefferedPosition(info, d), rules, data);
            //} else {
                ehm = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());
            //}
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "ehm=" + ehm);
            }
            
            final String desc = d.getDisplayName();
            final Position[] range = getLine(result, d, doc, position, endPosition);
            
            if (isCanceled()) {
                return null;
            }
            
            if (range[0] == null || range[1] == null) {
                continue;
            }
            
            descs.add(ErrorDescriptionFactory.createErrorDescription(errorKind2Severity.get(d.getSeverity()), desc, ehm, doc, range[0], range[1]));
        }
        
        if (isCanceled()) {
            return null;
        }
        
        return descs;
    }
    
    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    
    private Position[] getLine(ParserResult info, Error d, final Document doc, int startOffset, int endOffset) {
        StyledDocument sdoc = (StyledDocument) doc;
        int lineNumber = NbDocument.findLineNumber(sdoc, startOffset);
        int lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);
        String text = DataLoadersBridge.getDefault().getLine(doc, lineNumber);
        if (text == null) {
            return new Position[2];
        }
        
        boolean rangePrepared = false;
        
        if (!rangePrepared) {
            int column = 0;
            int length = text.length();
            
            while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
                column++;
            }
            
            while (length > 0 && Character.isWhitespace(text.charAt(length - 1))) {
                length--;
            }
            
            startOffset = lineOffset + column;
            endOffset = lineOffset + length;
            if (startOffset > endOffset) {
                // Space only on the line
                startOffset = lineOffset;
            }
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "startOffset = " + startOffset );
            LOG.log(Level.FINE, "endOffset = " + endOffset );
        }
        
        final int startOffsetFinal = startOffset;
        final int endOffsetFinal = endOffset;
        final Position[] result = new Position[2];
        
        doc.render(new Runnable() {
            public void run() {
                if (isCanceled()) {
                    return;
                }
                
                int len = doc.getLength();
                
                if (startOffsetFinal > len || endOffsetFinal > len) {
                    if (!isCanceled() && LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING, "document changed, but not canceled?" );
                        LOG.log(Level.WARNING, "len = " + len );
                        LOG.log(Level.WARNING, "startOffset = " + startOffsetFinal );
                        LOG.log(Level.WARNING, "endOffset = " + endOffsetFinal );
                    }
                    cancel();
                    
                    return;
                }
                
                try {
                    result[0] = NbDocument.createPosition(doc, startOffsetFinal, Bias.Forward);
                    result[1] = NbDocument.createPosition(doc, endOffsetFinal, Bias.Backward);
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
        });
        
        return result;
    }
    
    private boolean cancel;
    
    synchronized boolean isCanceled() {
        return cancel;
    }
    
    public synchronized void cancel() {
        cancel = true;
    }
    
    synchronized void resume() {
        cancel = false;
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {
        resume();
        
        final Document doc = getDocument();
        
        if (doc == null) {
            LOG.log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }
        
//        long start = System.currentTimeMillis();
        final List<ErrorDescription> descriptions = new ArrayList<ErrorDescription>();
        
        try {
            ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws ParseException {
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                    if (language != null) {
                        if(!(resultIterator.getParserResult() instanceof ParserResult)) {
                            return ;
                        }
                        ParserResult r = (ParserResult) resultIterator.getParserResult();
                        List<? extends Error> errors = r.getDiagnostics();
                        List<ErrorDescription> desc = new ArrayList<ErrorDescription>();

                        HintsProvider provider = language.getHintsProvider();
                        GsfHintsManager manager = null;
                        RuleContext ruleContext = null;
                        if (provider != null) {
                            manager = language.getHintsManager();
                            if (manager != null) {
                                ruleContext = manager.createRuleContext(r, language, -1, -1, -1);
                                if (ruleContext != null) {
                                    List<Error> unhandled = new ArrayList<Error>();
                                    List<Hint> hints = new ArrayList<Hint>();
                                    provider.computeErrors(manager, ruleContext, hints, unhandled);
                                    errors = unhandled;
                                    boolean allowDisableEmpty = true;
                                    for (Hint hint : hints) {
                                        ErrorDescription errorDesc = manager.createDescription(hint, ruleContext, allowDisableEmpty);
                                        descriptions.add(errorDesc);
                                    }
                                }
                            }
                        }

                        // Process errors without codes
                        desc = computeErrors(doc, r, errors, desc);
                        if (desc == null) {
                            //meaning: cancelled
                            return;
                        }

                        descriptions.addAll(desc);
                    }

                    for(Embedding e : resultIterator.getEmbeddings()) {
                        if (isCanceled()) {
                            return;
                        }
                        
                        run(resultIterator.getResultIterator(e));
                    }
                }
            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }

        HintsController.setErrors(doc, "csl-hints", descriptions);

//        long end = System.currentTimeMillis();
//        TimesCollector.getDefault().reportTime(info.getFileObject(), "com-hints", "Hints", end - start);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
}

