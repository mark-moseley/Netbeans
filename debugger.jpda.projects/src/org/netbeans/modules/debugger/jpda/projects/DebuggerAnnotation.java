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

package org.netbeans.modules.debugger.jpda.projects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerAnnotation extends Annotation implements Lookup.Provider {

    private Line        line;
    private String      type;
    private JPDAThread  thread;


    DebuggerAnnotation (String type, Line line, JPDAThread thread) {
        this.type = type;
        this.line = line;
        this.thread = thread;
        attach (line);
    }
    
    DebuggerAnnotation (String type, Line.Part linePart) {
        this.type = type;
        this.line = linePart.getLine();
        attach (linePart);
    }
    
    DebuggerAnnotation (String type, Highlight highlight, FileObject fo) {
        this.type = type;
        attach (new HighlightAnnotatable(highlight, fo));
    }
    
    public String getAnnotationType () {
        return type;
    }
    
    Line getLine () {
        return line;
    }
    
    public String getShortDescription () {
        if (type == EditorContext.CURRENT_LINE_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        if (type == EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_EXP_LINE"); // NOI18N
        else
        if (type == EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CALLSITE"); // NOI18N
        if (type == EditorContext.OTHER_THREAD_ANNOTATION_TYPE) {
            return NbBundle.getMessage(DebuggerAnnotation.class, "TOOLTIP_OTHER_THREAD", thread.getName());
        }
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown annotation type '"+type+"'."));
        return null;
    }
    
    private static final class HighlightAnnotatable extends Annotatable {
        
        private static Map highlightsByFiles = new HashMap();
        
        private Highlight highlight;
        private FileObject fo;
        
        public HighlightAnnotatable(Highlight highlight, FileObject fo) {
            this.highlight = highlight;
            this.fo = fo;
        }
        
        public String getText() {
            return null;
        }

        protected void addAnnotation(Annotation anno) {
            Collection highlights;
            synchronized (highlightsByFiles) {
                highlights = (Collection) highlightsByFiles.get(fo);
                if (highlights == null) {
                    highlights = new HashSet();
                    highlightsByFiles.put(fo, highlights);
                }
                highlights.add(highlight);
            }
            Highlighter.getDefault().setHighlights(fo, getClass().getName(), highlights);
        }

        protected void removeAnnotation(Annotation anno) {
            Collection highlights;
            synchronized (highlightsByFiles) {
                highlights = (Collection) highlightsByFiles.get(fo);
                if (highlights == null) {
                    highlights = Collections.EMPTY_SET;
                } else {
                    highlights.remove(highlight);
                    if (highlights.isEmpty()) {
                        highlightsByFiles.remove(fo);
                    }
                }
            }
            Highlighter.getDefault().setHighlights(fo, getClass().getName(), highlights);
        }
        

    }

    public Lookup getLookup() {
        if (thread == null) {
            return Lookup.EMPTY;
        } else {
            return Lookups.singleton(thread);
        }
    }
    
}
