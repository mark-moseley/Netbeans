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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.hints;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.hints.AnnotationHolder.Attacher;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescriptionTestSupport;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import static org.netbeans.modules.editor.hints.AnnotationHolder.*;
import static org.netbeans.spi.editor.hints.Severity.*;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolderTest extends NbTestCase {
    
    private FileObject file;
    private Document doc;
    private EditorCookie ec;
    
    /** Creates a new instance of AnnotationHolderTest */
    public AnnotationHolderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MimeDataProviderImpl.class);
        FileSystem fs = FileUtil.createMemoryFileSystem();
        
        file = fs.getRoot().createData("test.txt");
        
        writeIntoFile(file, "01234567890123456789\n  abcdefg  \n  hijklmnop");
        
        DataObject od = DataObject.find(file);
        
        ec = od.getCookie(EditorCookie.class);
        doc = ec.openDocument();
    }
    
    public void testComputeHighlightsOneLayer1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 3, 5, 6}, new AttributeSet[] {COLORINGS.get(ERROR), COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsOneLayer2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsOneLayer3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 3, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);

        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsOneLayer4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsTwoLayers1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 5, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 3, 5, 7}, new AttributeSet[] {COLORINGS.get(ERROR), COLORINGS.get(WARNING)});
    }
    
    public void testComputeHighlightsTwoLayers2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 3, 5);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, 7}, new AttributeSet[] {COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsTwoLayers3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 4, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {3, 5, /*6*/5, 7}, new AttributeSet[] {COLORINGS.get(ERROR), COLORINGS.get(WARNING)});
    }
    
    public void testComputeHighlightsTwoLayers4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 4);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, /*2*/3, 3, 5}, new AttributeSet[] {COLORINGS.get(WARNING), COLORINGS.get(ERROR)});
    }
    
    public void testComputeHighlightsTwoLayers5() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        final OffsetsBag bag = AnnotationHolder.computeHighlights(doc, errors);
        
        assertHighlights("",bag, new int[] {1, /*2*/3, 3, 5, /*6*/5, 7}, new AttributeSet[] {COLORINGS.get(WARNING), COLORINGS.get(ERROR), COLORINGS.get(WARNING)});
    }
    
    public void testNullSpan() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionTestSupport.createErrorDescription(file, "", Severity.DISABLED, ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), null);
        ErrorDescription ed3 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        
        ec.open();
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1, ed2, ed3));
        
        ec.close();
    }
    
    public void testMultilineHighlights() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 47 - 30, 72 - 30);
        OffsetsBag bag = new OffsetsBag(doc);
        
        List<ErrorDescription> errors = Arrays.asList(ed1);
        BaseDocument bdoc = (BaseDocument) doc;
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(Utilities.getRowStartFromLineOffset(bdoc, 0)), errors);
        
        assertHighlights("", bag, new int[] {47 - 30, 50 - 30}, new AttributeSet[] {COLORINGS.get(ERROR)});
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(Utilities.getRowStartFromLineOffset(bdoc, 1)), errors);
        
        assertHighlights("", bag, new int[] {53 - 30, 60 - 30}, new AttributeSet[] {COLORINGS.get(ERROR)});
        
        bag = new OffsetsBag(doc);
        AnnotationHolder.updateHighlightsOnLine(bag, bdoc, bdoc.createPosition(Utilities.getRowStartFromLineOffset(bdoc, 2)), errors);
        
        assertHighlights("", bag, new int[] {65 - 30, 72 - 30}, new AttributeSet[] {COLORINGS.get(ERROR)});
    }
    
    public void testComputeSeverity() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, "2", file, 1, 7);
        ErrorDescription ed3 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        ErrorDescription ed4 = ErrorDescriptionFactory.createErrorDescription(Severity.VERIFIER, "2", file, 1, 7);
        
        ec.open();
        
        class AttacherImpl implements Attacher {
            private ParseErrorAnnotation annotation;
            public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {
                if (line.getOffset() == 0) {
                    this.annotation = a;
                }
            }
            public void detachAnnotation(Annotation a) {}
        }
        
        AttacherImpl impl = new AttacherImpl();
        
        AnnotationHolder.getInstance(file).attacher = impl;
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1, ed2, ed3));
        
        assertEquals(Severity.ERROR, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2, ed3));
        
        assertEquals(Severity.WARNING, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2));
        
        assertEquals(Severity.HINT, impl.annotation.getSeverity());
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed2, ed4));
        
        assertEquals(Severity.VERIFIER, impl.annotation.getSeverity());
        
        ec.close();
    }
    
    public void testTypeIntoLine() throws Exception {
        performTypingTest(25, "a", new int[0], new AttributeSet[0]);
    }
    
    public void testTypeOnLineStart() throws Exception {
        performTypingTest(21, "a", new int[0], new AttributeSet[0]);
    }
    
    public void testTypeOnLineStartWithNewline() throws Exception {
        performTypingTest(21, "a\n", new int[0], new AttributeSet[0]);
    }
    
    public void testTypeOnLineStartWithNewlines() throws Exception {
        performTypingTest(21, "a\na\na\na\n", new int[0], new AttributeSet[0]);
    }
    
    private void performTypingTest(int index, String insertWhat, int[] highlightSpans, AttributeSet[] highlightValues) throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 21, 32);
        
        ec.open();
        
        //these tests currently ignore annotations:
        class AttacherImpl implements Attacher {
            public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {}
            public void detachAnnotation(Annotation a) {}
        }
        
        AnnotationHolder.getInstance(file).attacher = new AttacherImpl();
        
        AnnotationHolder.getInstance(file).setErrorDescriptions("foo", Arrays.asList(ed1));
        
        doc.insertString(index, insertWhat, null);

        assertHighlights("highlights correct", AnnotationHolder.getBag(doc), highlightSpans, highlightValues);
        
        LifecycleManager.getDefault().saveAll();
        
        ec.close();
    }
    
    private void assertHighlights(String message, OffsetsBag bag, int[] spans, AttributeSet[] values) {
        HighlightsSequence hs = bag.getHighlights(0, Integer.MAX_VALUE);
        int index = 0;
        
        while (hs.moveNext()) {
            assertEquals(message, spans[2 * index], hs.getStartOffset());
            assertEquals(message, spans[2 * index + 1], hs.getEndOffset());
            assertEquals(message, values[index], hs.getAttributes());
            index++;
        }
    }
    
    @Override 
    protected boolean runInEQ() {
        return true;
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    public static final class MimeDataProviderImpl implements MimeDataProvider {

        @SuppressWarnings("deprecation")
        public Lookup getLookup(MimePath mimePath) {
            return Lookups.singleton(new DefaultEditorKit() {
                @Override
                public Document createDefaultDocument() {
                    return new GuardedDocument(this.getClass());
                }
            });
        }
        
    }
}
