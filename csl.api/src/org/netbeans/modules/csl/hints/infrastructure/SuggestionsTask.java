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
package org.netbeans.modules.csl.hints.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

/**
 * Task which delegates to the language plugins for actual suggestions-computation
 * 
 * @author Tor Norbye
 */
public class SuggestionsTask extends ParserResultTask<ParserResult> {
    
    private static final Logger LOG = Logger.getLogger(SuggestionsTask.class.getName());
    private boolean cancelled = false;

    public SuggestionsTask() {
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {
        resume();
        
        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        if (fileObject == null || isCancelled()) {
            return;
        }

        if (!(event instanceof CursorMovedSchedulerEvent) || isCancelled()) {
            return;
        }

        // Do we have a selection? If so, don't do suggestions
        CursorMovedSchedulerEvent evt = (CursorMovedSchedulerEvent) event;
        int[] range = new int [] {
            Math.min(evt.getMarkOffset(), evt.getCaretOffset()),
            Math.max(evt.getMarkOffset(), evt.getCaretOffset())
        };
        if (range != null && range.length == 2 && range[0] != -1 && range[1] != -1 && range[0] != range[1]) {
            HintsController.setErrors(fileObject, SuggestionsTask.class.getName(), Collections.<ErrorDescription>emptyList());
            return;
        }

        final int pos = evt.getCaretOffset();
        if (pos == -1 || isCancelled()) {
            return;
        }

        try {
            ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult(pos);
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(r.getSnapshot().getMimeType());
                    if (language == null || isCancelled()) {
                        return;
                    }

                    HintsProvider provider = language.getHintsProvider();
                    if (provider == null || isCancelled()) {
                        return;
                    }
                    GsfHintsManager manager = language.getHintsManager();
                    if (manager == null || isCancelled()) {
                        return;
                    }
                    RuleContext ruleContext = manager.createRuleContext((ParserResult) r, language, pos, -1, -1);
                    if (ruleContext == null || isCancelled()) {
                        return;
                    }
                    List<ErrorDescription> descriptions = new ArrayList<ErrorDescription>();
                    List<Hint> hints = new ArrayList<Hint>();

                    provider.computeSuggestions(manager, ruleContext, hints, pos);

                    for (Hint hint : hints) {
                        if (isCancelled()) {
                            return;
                        }

                        ErrorDescription desc = manager.createDescription(hint, ruleContext, false);
                        descriptions.add(desc);
                    }

                    if (isCancelled()) {
                        return;
                    }

                    HintsController.setErrors(r.getSnapshot().getSource().getFileObject(), SuggestionsTask.class.getName(), descriptions);
                }
            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }

    public @Override int getPriority() {
        return Integer.MAX_VALUE;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    public @Override synchronized void cancel() {
        cancelled = true;
    }

    private synchronized void resume() {
        cancelled = false;
    }

    private synchronized boolean isCancelled() {
        return cancelled;
    }
}
