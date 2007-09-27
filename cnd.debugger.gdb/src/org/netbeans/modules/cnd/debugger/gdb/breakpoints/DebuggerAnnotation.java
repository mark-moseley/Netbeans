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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.EditorContext;


/**
 * Debugger Annotation class.
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class DebuggerAnnotation extends Annotation {

    private Line        line;
    private String      type;
    
    public DebuggerAnnotation(String type, Line line) {
        this.type = type;
        this.line = line;
        attach(line);
    }
    
    public String getAnnotationType() {
        return type;
    }
    
    public Line getLine() {
        return line;
    }
    
    public String getShortDescription() {
        if (type == EditorContext.BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getBundle(DebuggerAnnotation.class).getString("TOOLTIP_BREAKPOINT"); // NOI18N
        } else if (type == EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getBundle(DebuggerAnnotation.class).getString 
                    ("TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
        } else if (type == EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getBundle(DebuggerAnnotation.class).getString 
                    ("TOOLTIP_CONDITIONAL_BREAKPOINT"); // NOI18N
        } else if (type == EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
            return NbBundle.getBundle(DebuggerAnnotation.class).getString 
                    ("TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT"); // NOI18N
        } else if (type == EditorContext.CURRENT_LINE_ANNOTATION_TYPE) {
            return NbBundle.getMessage(DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        } else if (type == EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE) {
            return NbBundle.getBundle(DebuggerAnnotation.class).getString("TOOLTIP_CALLSITE"); // NOI18N
        }
        return NbBundle.getBundle(DebuggerAnnotation.class).getString("TOOLTIP_ANNOTATION"); // NOI18N
    }
}
