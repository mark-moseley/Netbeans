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
package org.netbeans.modules.cnd.highlight.error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class HighlightProvider  {
    
    /** package-local interface - for test purposes only! */
    interface Hook {
        void highlightingDone(String absoluteFileName);
    }
    
    private Hook hook;
    
    public static final boolean TRACE_ANNOTATIONS = Boolean.getBoolean("cnd.highlight.trace.annotations"); // NOI18N
    
    private static final HighlightProvider instance = new HighlightProvider();
    
    /** package-local - for test purposes only! */
    synchronized  void setHook(Hook hook) {
        this.hook = hook;
    }
    
    public static HighlightProvider getInstance(){
        return instance;
    }
    
    /** Creates a new instance of HighlightProvider */
    private HighlightProvider() {
    }
    
    /* package */ void update(CsmFile file, Document doc, DataObject dao) {
        assert doc!=null || file==null;
        if (doc instanceof BaseDocument){
            addAnnotations((BaseDocument)doc, file, dao);
            Hook theHook = this.hook;
            if( theHook != null ) {
                theHook.highlightingDone(file.getAbsolutePath().toString());
            }
        }
    }
    
    /* package */ void clear(Document doc) {
        assert doc!=null;
        if (doc instanceof BaseDocument){
            removeAnnotations(doc);
        }
    }
    
    private static org.netbeans.spi.editor.hints.Severity getSeverity(CsmErrorInfo info) {
        switch( info.getSeverity() ) {
            case ERROR:     return org.netbeans.spi.editor.hints.Severity.ERROR;
            case WARNING:   return org.netbeans.spi.editor.hints.Severity.WARNING;
            default:        throw new IllegalArgumentException("Unexpected severity: " + info.getSeverity()); //NOI18N
        }
    }
    
    private void addAnnotations(BaseDocument doc, CsmFile file, DataObject dao) {
//        removing validation is questionable but it seems it's faster to just reannotate file
//        if (!isNeededUpdateAnnotations(doc, file)) {
//            return;
//        }
        
        removeAnnotations(doc);
        
        List<ErrorDescription> descriptions = new ArrayList<ErrorDescription>();
        try {
            if (TRACE_ANNOTATIONS) System.err.printf("\nSetting annotations for %s\n", file);
            for( CsmErrorInfo info : CsmErrorProvider.getDefault().getErrors(doc, file) ) {
                PositionBounds pb = createPositionBounds(dao, info.getStartOffset(), info.getEndOffset());
                ErrorDescription desc = null;
                if( pb != null ) {
                    desc = ErrorDescriptionFactory.createErrorDescription(
                            getSeverity(info), info.getMessage(), doc, pb.getBegin().getPosition(), pb.getEnd().getPosition());
                    descriptions.add(desc);
                    if (TRACE_ANNOTATIONS) System.err.printf("\tadded %s\n", desc);
                } else {
                    if (TRACE_ANNOTATIONS) System.err.printf("\tCan't create PositionBounds for %s\n", info);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        HintsController.setErrors(doc, HighlightProvider.class.getName(), descriptions);
    }
    
    
    private static PositionBounds createPositionBounds(DataObject dao, int start, int end) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(dao);
        if (ces != null) {
            PositionRef posBeg = ces.createPositionRef(start, Position.Bias.Forward);
            PositionRef posEnd = ces.createPositionRef(end, Position.Bias.Backward);
            return new PositionBounds(posBeg, posEnd);
        }
        return null;
    }
    
    private void removeAnnotations(Document doc) {
        HintsController.setErrors(doc, HighlightProvider.class.getName(), Collections.<ErrorDescription>emptyList());
    }
    
    
}
