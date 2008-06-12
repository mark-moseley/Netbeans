/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Stack;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.loaders.DataObject;

/**
 * 
 * @author Vladimir Kvashin
 */
public class ErrorHighlightingBaseTestCase extends ProjectBasedTestCase {

    protected static final boolean TRACE = true; // Boolean.getBoolean("cnd.error.hl.tests.trace");


    public ErrorHighlightingBaseTestCase(String testName) {
        super(testName, true);
    }

    protected final void performStaticTest(String sourceFileName) throws Exception {
        String datafileName = sourceFileName + ".dat";
        File testSourceFile = getDataFile(sourceFileName);
        File workDir = getWorkDir();
        File output = new File(workDir, datafileName);
        PrintStream out = new PrintStream(output);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        Collection<CsmErrorInfo> errorInfos = CsmErrorProvider.getDefault().getErrors(doc, csmFile);
        for (CsmErrorInfo info : errorInfos) {
            String txt = String.format("%s %s [%d-%d]: %s", info.getSeverity(), sourceFileName, info.getStartOffset(), info.getEndOffset(), info.getMessage());
            out.printf("%s\n", txt);
            if (TRACE) {
                System.out.printf("%s\n", txt);
            }
        }
        compareReferenceFiles(datafileName, datafileName);
    }

    /**
     * Performs undo for changes that are made in a document
     */
    private static class Undoer implements DocumentListener {

        BaseDocument document;
        Stack<DocumentEvent> events = new Stack<DocumentEvent>();

        public Undoer(BaseDocument document) {
            this.document = document;
            document.addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent e) {
            events.add(e);
        }

        public void insertUpdate(DocumentEvent e) {
            events.add(e);
        }

        public void removeUpdate(DocumentEvent e) {
            events.add(e);
        }

        public boolean canUndo() {
            for (DocumentEvent e : events) {
                if (!canUndo(e)) {
                    return false;
                }
            }
            return true;
        }

        public void undo() {
            document.removeDocumentListener(this);
            while (!events.empty()) {
                DocumentEvent e = events.pop();
                if (e instanceof BaseDocumentEvent) {
                    ((BaseDocumentEvent) e).undo();
                } else {
                    throw new IllegalStateException("Can not undo"); //NOI18N

                }
            }
            events.clear();
            document.addDocumentListener(this);
        }

        private boolean canUndo(DocumentEvent e) {
            if (e instanceof BaseDocumentEvent) {
                return ((BaseDocumentEvent) e).canUndo();
            }
            return false;
        }
    }

    protected final void performDynamicTest(String sourceFileName, ErrorMaker errorMaker) throws Exception {
        String datafileName = sourceFileName + ".dat";
        File testSourceFile = getDataFile(sourceFileName);
        File workDir = getWorkDir();
        File output = new File(workDir, datafileName);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        Collection<CsmErrorInfo> errorInfos;
        
        errorInfos = CsmErrorProvider.getDefault().getErrors(doc, csmFile);
        //if (TRACE) trace("INITIAL:", errorInfos, sourceFileName);
        assertTrue("The shouldn't be errors in the initial state", errorInfos.isEmpty()); //NOI18N
        
        Undoer undoer = new Undoer(doc);
        errorMaker.init(doc, csmFile);
        while (errorMaker.change()) {
            if (TRACE) trace("\n\n==========", doc);
            parseModifiedFile((DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty));
            errorInfos = CsmErrorProvider.getDefault().getErrors(doc, csmFile);
            if (TRACE) trace("----------", errorInfos, sourceFileName);
            errorMaker.analyze(errorInfos);
            if (undoer.canUndo()) {
                undoer.undo();
                errorMaker.undone();
            } else {
                throw new IllegalStateException("can not undo"); //NOI18N
            }
        }
    }

    private static void parseModifiedFile(DataObject dob) throws IOException { 
        CsmFile csmFile = CsmUtilities.getCsmFile(dob, false);
        assert csmFile != null : "Must be csmFile for data object " + dob;
        CsmProject prj = csmFile.getProject();
        assert prj != null : "Must be project for csm file " + csmFile;
        prj.waitParse();
        assert csmFile.isParsed() : " file must be parsed: " + csmFile;
        assert prj.isStable(null) : " full project must be parsed" + prj;
    }
    

    protected void trace(String title, BaseDocument doc) throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
//        StringTokenizer tokenizer = new StringTokenizer(text, System.getProperty("line.separator"));
//        int lineNo = 1;
//        while (tokenizer.hasMoreTokens()) {
//            String lineText = tokenizer.nextToken();
//            System.out.printf(" %s\n", lineNo, lineText);
//        }
        System.out.printf("%s\n%s\n", title, text);
    }
    
    protected void trace(String title, Collection<CsmErrorInfo> errorInfos, String sourceFileName) {
        System.out.printf("%s\n", title);
        trace(errorInfos, sourceFileName);
    }
    
    protected void trace(Collection<CsmErrorInfo> errorInfos, String sourceFileName) {
        for (CsmErrorInfo info : errorInfos) {
            System.out.printf("%s\n", toString(info, sourceFileName));
        }
    }

    protected String toString(CsmErrorInfo info, String sourceFileName) {
        return String.format("%s %s [%d-%d]: %s", info.getSeverity(), sourceFileName, info.getStartOffset(), info.getEndOffset(), info.getMessage());
    }
}
