/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.editor.BpelEditorContext;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 */
public class BpelLineBreakpointView extends BpelBreakpointView {
    
    private BpelBreakpointListener myBreakpointListener;
    
    protected String getName(BpelBreakpoint breakpoint) throws UnknownTypeException {
        if (!(breakpoint instanceof LineBreakpoint)) {
            throw new UnknownTypeException(breakpoint);
        }
        
        if (myBreakpointListener == null) {
            myBreakpointListener = (BpelBreakpointListener) 
                    DebuggerManager.getDebuggerManager().lookupFirst 
                    (null, BpelBreakpointListener.class);
        }
        
        final LineBreakpoint lbp = (LineBreakpoint) breakpoint;
        final Object annotation = myBreakpointListener.findAnnotation(lbp);
        
        String result = EditorUtil.getFileName(lbp.getURL()) +
                ": " + lbp.getLineNumber();
        
        if (annotation != null) {
            final AnnotationType type = 
                    EditorContextBridge.getAnnotationType(annotation);
            
            if (type != null) {
                if (AnnotationType.BROKEN_BREAKPOINT.equals(type)) {
                    result += " (broken)";
                }
            }
        }
        
        return result;
    }
    
    public String getIconBase(Object object) throws UnknownTypeException {
        if (!(object instanceof LineBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        
        final LineBreakpoint lbp = (LineBreakpoint) object;
        final Object annotation = myBreakpointListener.findAnnotation(lbp);
        
        if (annotation != null) {
            final AnnotationType type = 
                    EditorContextBridge.getAnnotationType(annotation);
            
            if (type != null) {
                if (AnnotationType.BROKEN_BREAKPOINT.equals(type)) {
                    return BROKEN_BREAKPOINT;
                }
            }
        }
        
        return LINE_BREAKPOINT;
    }
}
