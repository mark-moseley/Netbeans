/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolder implements ChangeListener, PropertyChangeListener, DocumentListener {

    private static final Logger LOG = Logger.getLogger(AnnotationHolder.class.getName());
    
    final static Map<Severity, AttributeSet> COLORINGS;

    static {
        COLORINGS = new EnumMap<Severity, AttributeSet>(Severity.class);
        COLORINGS.put(Severity.ERROR, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0x00, 0x00), EditorStyleConstants.Tooltip, new TooltipResolver()));
        COLORINGS.put(Severity.WARNING, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xC0, 0xC0, 0x00), EditorStyleConstants.Tooltip, new TooltipResolver()));
        COLORINGS.put(Severity.VERIFIER, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0xD5, 0x55), EditorStyleConstants.Tooltip, new TooltipResolver()));
        COLORINGS.put(Severity.HINT, AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, new TooltipResolver()));
    };

    private Map<ErrorDescription, List<Position>> errors2Lines;
    private Map<Position, List<ErrorDescription>> line2Errors;
    private Map<Position, ParseErrorAnnotation> line2Annotations;
    private Map<String, List<ErrorDescription>> layer2Errors;

    private Set<JEditorPane> openedComponents;
    private EditorCookie.Observable editorCookie;
    private FileObject file;
    private DataObject od;
    private BaseDocument doc;

    private static Map<DataObject, AnnotationHolder> file2Holder = new HashMap<DataObject, AnnotationHolder>();

    public static synchronized AnnotationHolder getInstance(FileObject file) {
        if (file == null)
            return null;

        try {
            DataObject od = DataObject.find(file);
            AnnotationHolder result = file2Holder.get(od);

            if (result == null) {
                EditorCookie.Observable editorCookie = od.getCookie(EditorCookie.Observable.class);

                if (editorCookie == null) {
                    LOG.log(Level.WARNING,
                            "No EditorCookie.Observable for file: " + FileUtil.getFileDisplayName(file));
                } else {
                    Document doc = editorCookie.getDocument();

                    if (doc instanceof BaseDocument) {
                        file2Holder.put(od, result = new AnnotationHolder(file, od, (BaseDocument) doc, editorCookie));
                    }
                }
            }

            return result;
        } catch (IOException e) {
            LOG.log(Level.INFO, null, e);
            return null;
        }
    }

    private AnnotationHolder(FileObject file, DataObject od, BaseDocument doc, EditorCookie.Observable editorCookie) {
        if (file == null)
            return ;

        init();

        this.file = file;
        this.od = od;
        this.doc = doc;

        getBag(doc);

        this.doc.addDocumentListener(this);
        editorCookie.addPropertyChangeListener(WeakListeners.propertyChange(this, editorCookie));
        this.editorCookie = editorCookie;

        propertyChange(null);

//        LOG.log(Level.FINE, null, new Throwable("Creating AnnotationHolder for " + file.getPath()));
        Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder",
                    new Object[] {file, this});
    }

    private synchronized void init() {
        errors2Lines = new HashMap<ErrorDescription, List<Position>>();
        line2Errors = new HashMap<Position, List<ErrorDescription>>();
        line2Annotations = new HashMap<Position, ParseErrorAnnotation>();
        layer2Errors = new HashMap<String, List<ErrorDescription>>();
        openedComponents = new HashSet<JEditorPane>();
    }

    public void stateChanged(ChangeEvent evt) {
        updateVisibleRanges();
    }

    Attacher attacher = new NbDocumentAttacher();

    void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {
        attacher.attachAnnotation(line, a);
    }

    void detachAnnotation(Annotation a) {
        attacher.detachAnnotation(a);
    }

    static interface Attacher {
        public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException;
        public void detachAnnotation(Annotation a);
    }

    final class LineAttacher implements Attacher {
        public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {
            throw new UnsupportedOperationException();
//            LineCookie lc = od.getCookie(LineCookie.class);
//            Line lineRef = lc.getLineSet().getCurrent(line);
//
//            a.attach(lineRef);
        }
        public void detachAnnotation(Annotation a) {
            a.detach();
        }
    }

    final class NbDocumentAttacher implements Attacher {
        public void attachAnnotation(Position lineStart, ParseErrorAnnotation a) throws BadLocationException {
            NbDocument.addAnnotation((StyledDocument) doc, lineStart, -1, a);
        }
        public void detachAnnotation(Annotation a) {
            if (doc != null) {
                NbDocument.removeAnnotation((StyledDocument) doc, a);
            }
        }
    }

    private synchronized void clearAll() {
        //remove all annotations:
        for (ParseErrorAnnotation a : line2Annotations.values()) {
            detachAnnotation(a);
        }

        file2Holder.remove(od);
        doc.removeDocumentListener(this);

        getBag(doc).clear();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JEditorPane[] panes = editorCookie.getOpenedPanes();

                if (panes == null) {
                    clearAll();
                    return ;
                }

                Set<JEditorPane> addedPanes = new HashSet<JEditorPane>(Arrays.asList(panes));
                Set<JEditorPane> removedPanes = new HashSet<JEditorPane>(openedComponents);

                removedPanes.removeAll(addedPanes);
                addedPanes.removeAll(openedComponents);

                for (JEditorPane pane : addedPanes) {
                    Container parent = pane.getParent();

                    if (parent instanceof JViewport) {
                        JViewport viewport = (JViewport) parent;

                        viewport.addChangeListener(WeakListeners.change(AnnotationHolder.this, viewport));
                    }
                }

                openedComponents.removeAll(removedPanes);
                openedComponents.addAll(addedPanes);

                updateVisibleRanges();
                return ;
            }
        });
    }

    public synchronized void insertUpdate(DocumentEvent e) {
        try {
            int offset = Utilities.getRowStart(doc, e.getOffset());

            Set<Position> modifiedLines = new HashSet<Position>();

            int index = findPositionGE(offset);

            if (index == knownPositions.size())
                return ;

            Position line = knownPositions.get(index).get();

            if (line == null)
                return ;

            List<ErrorDescription> eds = getErrorsForLine(line, false);

            if (eds == null)
                return ;

            eds = new LinkedList<ErrorDescription>(eds);

            for (ErrorDescription ed : eds) {
                for (Position i : errors2Lines.remove(ed)) {
                    line2Errors.get(i).remove(ed);
                    modifiedLines.add(i);
                }
                for (List<ErrorDescription> edsForLayer : layer2Errors.values()) {
                    edsForLayer.remove(ed);
                }
            }

            line2Errors.remove(line);

            //make sure the highlights are removed even for multi-line inserts:
            try {
                int rowStart = e.getOffset();
                int rowEnd = Utilities.getRowEnd(doc, e.getOffset() + e.getLength());

                getBag(doc).removeHighlights(rowStart, rowEnd, false);
            } catch (BadLocationException ex) {
                throw (IOException) new IOException().initCause(ex);
            }

            for (Position lineToken : modifiedLines) {
                updateAnnotationOnLine(lineToken);
                updateHighlightsOnLine(lineToken);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized void removeUpdate(DocumentEvent e) {
        try {
            Position current = null;
            int index = -1;
            int startOffset = Utilities.getRowStart(doc, e.getOffset());

            while (current == null) {
                index = findPositionGE(startOffset);

                if (knownPositions.size() == 0) {
                    break;
                }
                if (index == knownPositions.size()) {
                    return;
                }
                current = knownPositions.get(index).get();
            }

            if (current == null) {
                //nothing to do:
                return;
            }

            assert index != (-1);

            //find the first:
            while (index > 0) {
                Position minusOne = knownPositions.get(index - 1).get();

                if (minusOne == null) {
                    index--;
                    continue;
                }

                if (minusOne.getOffset() != current.getOffset()) {
                    break;
                }

                index--;
            }

            Set<Position> modifiedLinesTokens = new HashSet<Position>();

            while (index < knownPositions.size()) {
                Position next = knownPositions.get(index).get();

                if (next == null) {
                    index++;
                    continue;
                }

                if (next.getOffset() != current.getOffset()) {
                    break;
                }

                modifiedLinesTokens.add(next);
                index++;
            }

            for (Position line : new LinkedList<Position>(modifiedLinesTokens)) {
                List<ErrorDescription> eds = line2Errors.get(line);

                if (eds == null || eds.isEmpty()) {
                    continue;
                }
                eds = new LinkedList<ErrorDescription>(eds);

                for (ErrorDescription ed : eds) {
                    for (Position i : errors2Lines.remove(ed)) {
                        line2Errors.get(i).remove(ed);
                        modifiedLinesTokens.add(i);
                    }
                    for (List<ErrorDescription> edsForLayer : layer2Errors.values()) {
                        edsForLayer.remove(ed);
                    }
                }

                line2Errors.remove(line);
            }

            for (Position line : modifiedLinesTokens) {
                updateAnnotationOnLine(line);
                updateHighlightsOnLine(line);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void changedUpdate(DocumentEvent e) {
        //ignored
    }

    private void updateVisibleRanges() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                final List<int[]> visibleRanges = new ArrayList<int[]>();

                doc.render(new Runnable() {
                    public void run() {
                        synchronized(AnnotationHolder.this) {
                            for (JEditorPane pane : openedComponents) {
                                Container parent = pane.getParent();

                                if (parent instanceof JViewport) {
                                    JViewport viewport = (JViewport) parent;
                                    Point start = viewport.getViewPosition();
                                    Dimension size = viewport.getExtentSize();
                                    Point end = new Point(start.x + size.width, start.y + size.height);

                                    int startPosition = pane.viewToModel(start);
                                    int endPosition = pane.viewToModel(end);
                                    //TODO: check differences against last:
                                    visibleRanges.add(new int[]{startPosition, endPosition});
                                }
                            }
                        }
                    }
                });

                INSTANCE.post(new Runnable() {
                    public void run() {
                        for (int[] span : visibleRanges) {
                            updateAnnotations(span[0], span[1]);
                        }
                    }
                });

                long endTime = System.currentTimeMillis();

                LOG.log(Level.FINE, "updateVisibleRanges: time={0}", endTime - startTime);
            }
        });
    }

    private void updateAnnotations(final int startPosition, final int endPosition) {
        long startTime = System.currentTimeMillis();
        final List<ErrorDescription> errorsToUpdate = new ArrayList<ErrorDescription>();

        doc.render(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        if (doc.getLength() == 0) {
                            return ;
                        }

                        int start = startPosition < doc.getLength() ? startPosition : (doc.getLength() - 1);
                        int end   = endPosition < doc.getLength() ? endPosition : (doc.getLength() - 1);

                        if (start < 0) start = 0;
                        if (end < 0) end = 0;

                        int startLine = Utilities.getRowStart(doc, start);
                        int endLine = Utilities.getRowEnd(doc, end) + 1;

                        int index = findPositionGE(startLine);

                        while (index < knownPositions.size()) {
                            Reference<Position> r = knownPositions.get(index++);
                            if (r==null)
                                continue;
                            Position lineToken = r.get();

                            if (lineToken == null)
                                continue;

                            if (lineToken.getOffset() > endLine)
                                break;

                            List<ErrorDescription> errors = line2Errors.get(lineToken);

                            if (errors != null) {
                                errorsToUpdate.addAll(errors);
                            }
                        }
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }

            }
        });

        LOG.log(Level.FINE, "updateAnnotations: errorsToUpdate={0}", errorsToUpdate);

        for (ErrorDescription e : errorsToUpdate) {
            //TODO: #115340: e can be for an unknown reason null:
            if (e == null) {
                continue;
            }

            LazyFixList l = e.getFixes();

            if (l.probablyContainsFixes() && !l.isComputed()) {
                l.getFixes();
            }
        }

        long endTime = System.currentTimeMillis();

        LOG.log(Level.FINE, "updateAnnotations: time={0}", endTime - startTime);
    }

    private List<ErrorDescription> getErrorsForLayer(String layer) {
        List<ErrorDescription> errors = layer2Errors.get(layer);

        if (errors == null) {
            layer2Errors.put(layer, errors = new ArrayList<ErrorDescription>());
        }

        return errors;
    }

    private List<ErrorDescription> getErrorsForLine(Position line, boolean create) {
        List<ErrorDescription> errors = line2Errors.get(line);

        if (errors == null && create) {
            line2Errors.put(line, errors = new ArrayList<ErrorDescription>());
        }

        if (errors != null && errors.isEmpty() && !create) {
            //clean:
            line2Errors.remove(line);
            errors = null;
        }

        return errors;
    }

    private static List<ErrorDescription> filter(List<ErrorDescription> errors, boolean onlyErrors) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        for (ErrorDescription e : errors) {
            if (e.getSeverity() == Severity.ERROR) {
                if (onlyErrors)
                    result.add(e);
            } else {
                if (!onlyErrors)
                    result.add(e);
            }
        }

        return result;
    }

    private static void concatDescription(List<ErrorDescription> errors, StringBuffer description) {
        boolean first = true;

        for (ErrorDescription e : errors) {
            if (!first) {
                description.append("\n\n");
            }
            description.append(e.getDescription());
            first = false;
        }
    }

    private LazyFixList computeFixes(List<ErrorDescription> errors) {
        List<LazyFixList> result = new ArrayList<LazyFixList>();

        for (ErrorDescription e : errors) {
            result.add(e.getFixes());
        }

        return ErrorDescriptionFactory.lazyListForDelegates(result);
    }

    private void updateAnnotationOnLine(Position line) throws BadLocationException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);

        if (errorDescriptions == null) {
            //nothing to do, remove old:
            Annotation ann = line2Annotations.remove(line);

            detachAnnotation(ann);
            return;
        }

        errorDescriptions = getErrorsForLine(line, true);

        List<ErrorDescription> trueErrors = filter(errorDescriptions, true);
        List<ErrorDescription> others = filter(errorDescriptions, false);
        boolean hasErrors = !trueErrors.isEmpty();

        //build up the description of the annotation:
        StringBuffer description = new StringBuffer();

        concatDescription(trueErrors, description);

        if (!trueErrors.isEmpty() && !others.isEmpty()) {
            description.append("\n\n");
        }

        concatDescription(others, description);

        Severity mostImportantSeverity;

        if (hasErrors) {
            mostImportantSeverity = Severity.ERROR;
        } else {
            mostImportantSeverity = Severity.HINT;

            for (ErrorDescription e : others) {
                if (mostImportantSeverity.compareTo(e.getSeverity()) > 0) {
                    mostImportantSeverity = e.getSeverity();
                }
            }
        }

        FixData fixes = new FixData(computeFixes(trueErrors), computeFixes(others));

        ParseErrorAnnotation pea = new ParseErrorAnnotation(mostImportantSeverity, fixes, description.toString(), line, this);
        Annotation previous = line2Annotations.put(line, pea);

        if (previous != null) {
            detachAnnotation(previous);
        }

        attachAnnotation(line, pea);
    }

    void updateHighlightsOnLine(Position line) throws IOException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);

        OffsetsBag bag = getBag(doc);

        updateHighlightsOnLine(bag, doc, line, errorDescriptions);
    }

    static void updateHighlightsOnLine(OffsetsBag bag, BaseDocument doc, Position line, List<ErrorDescription> errorDescriptions) throws IOException {
        try {
            int rowStart = line.getOffset();
            int rowEnd = Utilities.getRowEnd(doc, rowStart);
            int rowHighlightStart = Utilities.getRowFirstNonWhite(doc, rowStart);
            int rowHighlightEnd = Utilities.getRowLastNonWhite(doc, rowStart) + 1;

            bag.removeHighlights(rowStart, rowEnd, false);

            if (errorDescriptions != null) {
                bag.addAllHighlights(computeHighlights(doc, errorDescriptions).getHighlights(rowHighlightStart, rowHighlightEnd));
            }
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }

    static OffsetsBag computeHighlights(Document doc, List<ErrorDescription> errorDescriptions) throws IOException, BadLocationException {
        OffsetsBag bag = new OffsetsBag(doc);
        for (Severity s : Arrays.asList(Severity.VERIFIER, Severity.WARNING, Severity.ERROR)) {
            List<ErrorDescription> filteredDescriptions = new ArrayList<ErrorDescription>();

            for (ErrorDescription e : errorDescriptions) {
                if (e.getSeverity() == s) {
                    filteredDescriptions.add(e);
                }
            }

            List<int[]> currentHighlights = new ArrayList<int[]>();

            for (ErrorDescription e : filteredDescriptions) {
                int beginOffset = e.getRange().getBegin().getPosition().getOffset();
                int endOffset   = e.getRange().getEnd().getPosition().getOffset();

                if (endOffset < beginOffset) {
                    //see issue #112566
                    int swap = endOffset;

                    endOffset = beginOffset;
                    beginOffset = swap;

                    LOG.warning("Incorrect highlight in ErrorDescription, attach your messages.log to issue #112566: " + e.toString());
                }

                int[] h = new int[] {beginOffset, endOffset};

                OUT: for (Iterator<int[]> it = currentHighlights.iterator(); it.hasNext() && h != null; ) {
                    int[] hl = it.next();

                    switch (detectCollisions(hl, h)) {
                        case 0:
                            break;
                        case 1:
                            it.remove();
                            break;
                        case 2:
                            h = null; //nothing to add, hl is bigger:
                            break OUT;
                        case 4:
                        case 3:
                            int start = Math.min(hl[0], h[0]);
                            int end = Math.max(hl[1], h[1]);

                                h = new int[] {start, end};
                                it.remove();
                            break;
                    }
                }

                if (h != null) {
                    currentHighlights.add(h);
                }
            }

            for (int[] h : currentHighlights) {
                if (h[0] <= h[1]) {
                    bag.addHighlight(h[0], h[1], COLORINGS.get(s));
                } else {
                    //see issue #112566
                    StringBuilder sb = new StringBuilder();

                    for (ErrorDescription e : filteredDescriptions) {
                        sb.append("[");
                        sb.append(e.getRange().getBegin().getOffset());
                        sb.append("-");
                        sb.append(e.getRange().getEnd().getOffset());
                        sb.append("]");
                    }

                    sb.append("=>");

                    for (int[] h2 : currentHighlights) {
                        sb.append("[");
                        sb.append(h2[0]);
                        sb.append("-");
                        sb.append(h2[1]);
                        sb.append("]");
                    }

                    LOG.warning("Incorrect highlight computed, please reopen issue #112566 and attach the following output: " + sb.toString());
                }
            }
        }

        return bag;
    }

    private static int detectCollisions(int[] h1, int[] h2) {
        if (h2[1] < h1[0])
            return 0;//no collision
        if (h1[1] < h2[0])
            return 0;//no collision
        if (h2[0] < h1[0] && h2[1] > h1[1])
            return 1;//h2 encapsulates h1
        if (h1[0] < h2[0] && h1[1] > h2[1])
            return 2;//h1 encapsulates h2

        if (h1[0] < h2[0])
            return 3;//collides
        else
            return 4;
    }

    public void setErrorDescriptions(final String layer, final Collection<? extends ErrorDescription> errors) {
        doc.render(new Runnable() {
            public void run() {
                try {
                    setErrorDescriptionsImpl(file, layer, errors);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
    }

    private synchronized void setErrorDescriptionsImpl(FileObject file, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        long start = System.currentTimeMillis();

        try {
            if (file == null)
                return ;

            List<ErrorDescription> layersErrors = getErrorsForLayer(layer);

            Set<Position> primaryLines = new HashSet<Position>();
            Set<Position> allLines = new HashSet<Position>();

            for (ErrorDescription ed : layersErrors) {
                List<Position> lines = errors2Lines.remove(ed);
                if (lines == null) { //#134282
                    LOG.log(Level.WARNING, "Inconsistent error2Lines for layer {0}, file {1}.", new Object[] {layer, file.getPath()}); // NOI18N
                    continue;
                }

                boolean first = true;

                for (Position line : lines) {
                    List<ErrorDescription> errorsForLine = getErrorsForLine(line, false);

                    if (errorsForLine != null) {
                        errorsForLine.remove(ed);
                    }

                    if (first) {
                        primaryLines.add(line);
                    }

                    allLines.add(line);
                    first = false;
                }
            }

            List<ErrorDescription> validatedErrors = new ArrayList<ErrorDescription>();

            for (ErrorDescription ed : errors) {
                if (ed == null) {
                    LOG.log(Level.WARNING, "'null' ErrorDescription in layer {0}.", layer); //NOI18N
                    continue;
                }

                if (ed.getRange() == null)
                    continue;

                validatedErrors.add(ed);

                List<Position> lines = new ArrayList<Position>();
                int startLine = ed.getRange().getBegin().getLine();
                int endLine = ed.getRange().getEnd().getLine();

                for (int cntr = startLine; cntr <= endLine; cntr++) {
                    Position p = getPosition(cntr, true);
                    lines.add(p);
                }

                errors2Lines.put(ed, lines);

                boolean first = true;

                for (Position line : lines) {
                    getErrorsForLine(line, true).add(ed);

                    if (first) {
                        primaryLines.add(line);
                    }

                    allLines.add(line);
                    first = false;
                }
            }

            layersErrors.clear();
            layersErrors.addAll(validatedErrors);

            for (Position line : primaryLines) {
                updateAnnotationOnLine(line);
            }

            for (Position line : allLines) {
                updateHighlightsOnLine(line);
            }

            updateVisibleRanges();
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        } finally {
            long end = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Errors update for " + layer,
                    new Object[] {file, end - start});
        }
    }

    private List<Reference<Position>> knownPositions = new ArrayList<Reference<Position>>();

    private static class Abort extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static RuntimeException ABORT = new Abort();

    private synchronized int findPositionGE(int offset) {
        while (true) {
            try {
                int index = Collections.binarySearch(knownPositions, offset, new PositionComparator());

                if (index >= 0) {
                    return index;
                } else {
                    return - (index + 1);
                }
            } catch (Abort a) {
                LOG.log(Level.FINE, "a null Position detected - clearing");
                int removedCount = 0;
                for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                    if (it.next().get() == null) {
                        removedCount++;
                        it.remove();
                    }
                }
                LOG.log(Level.FINE, "clearing finished, {0} positions cleared", removedCount);
            }
        }
    }

    private synchronized Position getPosition(int lineNumber, boolean create) throws BadLocationException {
        try {
            while (true) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, lineNumber);
                try {
                    int index = Collections.binarySearch(knownPositions, lineStart, new PositionComparator());

                    if (index >= 0) {
                        Reference<Position> r = knownPositions.get(index);
                        Position p = r.get();

                        if (p != null) {
                            return p;
                        }
                    }

                    if (!create)
                        return null;

                    Position p = NbDocument.createPosition(doc, lineStart, Position.Bias.Forward);

                    knownPositions.add(- (index + 1), new WeakReference<Position>(p));

                    Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder - Line Token",
                            new Object[] {file, p});

                    return p;
                } catch (Abort a) {
                    LOG.log(Level.FINE, "a null Position detected - clearing");
                    int removedCount = 0;
                    for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                        if (it.next().get() == null) {
                            removedCount++;
                            it.remove();
                        }
                    }
                    LOG.log(Level.FINE, "clearing finished, {0} positions cleared", removedCount);
                }
            }
        } finally {
            LOG.log(Level.FINE, "knownPositions.size={0}", knownPositions.size());
        }
    }

    public synchronized boolean hasErrors() {
        for (ErrorDescription e : errors2Lines.keySet()) {
            if (e.getSeverity() == Severity.ERROR)
                return true;
        }

        return false;
    }

    public synchronized List<ErrorDescription> getErrors() {
        return new ArrayList<ErrorDescription>(errors2Lines.keySet());
    }

    public synchronized List<Annotation> getAnnotations() {
        return new ArrayList<Annotation>(line2Annotations.values());
    }

    public synchronized List<ErrorDescription> getErrorsGE(int offset) {
        try {
            Position current = null;
            int index = -1;
            int startOffset = Utilities.getRowStart(doc, offset);

            while (current == null) {
                index = findPositionGE(startOffset);

                if (knownPositions.size() == 0) {
                    break;
                }
                if (index == knownPositions.size()) {
                    return Collections.emptyList();
                }
                current = knownPositions.get(index).get();
            }

            if (current == null) {
                //nothing to do:
                return Collections.emptyList();
            }

            assert index != (-1);

            List<ErrorDescription> errors = line2Errors.get(current);

            if (errors != null) {
                SortedMap<Integer, List<ErrorDescription>> sortedErrors = new TreeMap<Integer, List<ErrorDescription>>();

                for (ErrorDescription ed : errors) {
                    List<ErrorDescription> errs = sortedErrors.get(ed.getRange().getBegin().getOffset());

                    if (errs == null) {
                        sortedErrors.put(ed.getRange().getBegin().getOffset(), errs = new LinkedList<ErrorDescription>());
                    }

                    errs.add(ed);
                }

                SortedMap<Integer, List<ErrorDescription>> tail = sortedErrors.tailMap(offset);

                if (!tail.isEmpty()) {
                    Integer k = tail.firstKey();

                    return new LinkedList<ErrorDescription>(sortedErrors.get(k));
                }
            }

            //try next line:
            int endOffset = Utilities.getRowEnd(doc, offset);

            return getErrorsGE(endOffset + 1);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    private static final RequestProcessor INSTANCE = new RequestProcessor("AnnotationHolder");

    public static OffsetsBag getBag(Document doc) {
        OffsetsBag ob = (OffsetsBag) doc.getProperty(AnnotationHolder.class);

        if (ob == null) {
            doc.putProperty(AnnotationHolder.class, ob = new OffsetsBag(doc));
        }

        return ob;
    }

    public int lineNumber(final Position offset) {
        final int[] result = new int[] {-1};

        doc.render(new Runnable() {
            public void run() {
                try {
                    result[0] = Utilities.getLineOffset(doc, offset.getOffset());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        return result[0];
    }
    private static class PositionComparator implements Comparator<Object> {

        private PositionComparator() {
        }

        public int compare(Object o1, Object o2) {
            int left = -1;

            if (o1 instanceof Reference) {
                Position value = (Position) ((Reference) o1).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                left = value.getOffset();
            }

            if (o1 instanceof Integer) {
                left = ((Integer) o1);
            }

            assert left != -1;

            int right = -1;

            if (o2 instanceof Reference) {
                Position value = (Position) ((Reference) o2).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                right = value.getOffset();
            }

            if (o2 instanceof Integer) {
                right = ((Integer) o2);
            }

            assert right != -1;

            return left - right;
        }
    }

    private static final class TooltipResolver implements HighlightAttributeValue<String> {

        public String getValue(final JTextComponent component, final Document document, Object attributeKey, final int startOffset, final int endOffset) {
            final Object source = document.getProperty(Document.StreamDescriptionProperty);

            if (!(source instanceof DataObject) || !(document instanceof BaseDocument)) {
                return null;
            }

            final String[] result = new String[1];

            document.render(new Runnable() {
                public void run() {
                    try {
                        int lineNumber = Utilities.getLineOffset((BaseDocument) document, startOffset);

                        if (lineNumber < 0) {
                            return;
                        }

                        AnnotationHolder h = AnnotationHolder.getInstance(((DataObject) source).getPrimaryFile());

                        if (h == null) {
                            LOG.log(Level.INFO,
                                    "File: " + ((DataObject) source).getPrimaryFile().getPath() + // NOI18N
                                    "\nStartOffset: " + startOffset); // NOI18N
                            return;
                        }

                        synchronized (h) {
                            Position p = h.getPosition(lineNumber, false);

                            if (p == null) {
                                return ;
                            }

                            List<ErrorDescription> errors = h.line2Errors.get(p);

                            if (errors == null || errors.isEmpty()) {
                                return;
                            }

                            List<ErrorDescription> trueErrors = new LinkedList<ErrorDescription>();
                            List<ErrorDescription> others = new LinkedList<ErrorDescription>();

                            for (ErrorDescription ed : errors) {
                                if (ed == null) continue;

                                PositionBounds pb = ed.getRange();

                                if (startOffset > pb.getEnd().getOffset() || pb.getBegin().getOffset() > endOffset) {
                                    continue;
                                }

                                if (ed.getSeverity() == Severity.ERROR) {
                                    trueErrors.add(ed);
                                } else {
                                    others.add(ed);
                                }
                            }

                            //build up the description of the annotation:
                            StringBuffer description = new StringBuffer();

                            concatDescription(trueErrors, description);

                            if (!trueErrors.isEmpty() && !others.isEmpty()) {
                                description.append("\n\n");
                            }

                            concatDescription(others, description);

                            result[0] = description.toString();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            return result[0];
        }

    }

}
