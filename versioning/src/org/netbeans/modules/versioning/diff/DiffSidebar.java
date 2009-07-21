/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.versioning.diff;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.diff.*;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.modules.versioning.Utils;
import org.openide.ErrorManager;
import org.openide.nodes.CookieSet;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.*;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.text.ChoiceFormat;
import java.text.MessageFormat;

/**
 * Left editor sidebar showing changes in the file against the base version.
 * 
 * @author Maros Sandor
 */
class DiffSidebar extends JPanel implements DocumentListener, ComponentListener, FoldHierarchyListener, FileChangeListener {
    
    private static final int BAR_WIDTH = 9;
    
    private final JTextComponent  textComponent;
    /**
     * We must keep FileObject here because a File may change if the FileObject is renamed.
     * The fileObejct can be DELETED TOO!
     */
    private FileObject            fileObject;

    private final EditorUI        editorUI;
    private final FoldHierarchy   foldHierarchy;
    private final BaseDocument    document;
    
    private boolean                 sidebarVisible;
    private boolean                 sidebarInComponentHierarchy;
    private Difference []           currentDiff;
    private DiffMarkProvider        markProvider;

    private Color colorAdded =      new Color(150, 255, 150);
    private Color colorChanged =    new Color(160, 200, 255);
    private Color colorRemoved =    new Color(255, 160, 180);
    private Color colorBorder =     new Color(102, 102, 102);
    
    private int     originalContentSerial;
    private int     originalContentBufferSerial = -1;
    private String  originalContentBuffer;

    private RequestProcessor.Task   refreshDiffTask;
    private VersioningSystem ownerVersioningSystem;

    public DiffSidebar(JTextComponent target, File file) {
        this.textComponent = target;
        this.fileObject = FileUtil.toFileObject(file);
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.document = editorUI.getDocument();
        this.markProvider = new DiffMarkProvider();
        setToolTipText(""); // NOI18N
        refreshDiffTask = DiffSidebarManager.getInstance().createDiffSidebarTask(new RefreshDiffTask());
        setMaximumSize(new Dimension(BAR_WIDTH, Integer.MAX_VALUE));
    }

    FileObject getFileObject() {
        return fileObject;
    }

    private void refreshOriginalContent() {
        File file = fileObject != null ? FileUtil.toFile(fileObject) : null;
        ownerVersioningSystem = file != null ? VersioningManager.getInstance().getOwner(file) : null;
        originalContentSerial++;
        refreshDiff();
    }
    
    JTextComponent getTextComponent() {
        return textComponent;
    }

    Difference[] getCurrentDiff() {
        return currentDiff;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Difference diff = getDifferenceAt(event);
        return getShortDescription(diff);
    }

    static String getShortDescription(Difference diff) {
        if (diff == null) {
            return null;
        }
        int n;
        switch (diff.getType()) {
            case Difference.ADD:
                n = diff.getSecondEnd() - diff.getSecondStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesAdded")).format(n), n); // NOI18N      
            case Difference.CHANGE:
                n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesChanged")).format(n), n); // NOI18N      
            case Difference.DELETE:
                n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesDeleted")).format(n), n); // NOI18N      
            default:
                throw new IllegalStateException("Unknown difference type: " + diff.getType()); // NOI18N
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_CLICKED || event.isPopupTrigger()) {
            Difference diff = getDifferenceAt(event);
            if (diff == null) {
                return;
            }
            onClick(event, diff);
        } else {
            super.processMouseEvent(event);
        }
    }
    
    private void onClick(MouseEvent event, Difference diff) {
        Point p = new Point(event.getPoint());
        SwingUtilities.convertPointToScreen(p, this);
        Point p2 = new Point(p);
        SwingUtilities.convertPointFromScreen(p2, textComponent);
        showTooltipWindow(new Point(p.x - p2.x, p.y), diff);
    }

    private void showTooltipWindow(Point p, Difference diff) {
        DiffActionTooltipWindow ttw = new DiffActionTooltipWindow(this, diff);
        ttw.show(new Point(p.x, p.y));
    }
    
    private Difference getDifferenceAt(MouseEvent event) {
        if (currentDiff == null) {
            return null;
        }
        int line = getLineFromMouseEvent(event);
        if (line == -1) {
            return null;
        }
        Difference diff = getDifference(line + 1);
        if (diff == null) {
            // delete annotations (arrows) are rendered between lines
            diff = getDifference(line);
            if ((diff != null) && (diff.getType() != Difference.DELETE)) {
                diff = null;
            }
        } else if (diff.getType() == Difference.DELETE) {
            Difference diffPrev = getDifference(line);
            if (diffPrev != null && diffPrev.getType() == Difference.DELETE) {
                // two delete arrows next to each other cause some selection problems, select the closer one
                diff = getCloserDifference(event, diffPrev, diff);
            }
        }
        return diff;
    }

    /**
     * In case of two neighboring DELETE differences which meet on the same line, this method returns a difference
     * which' annotation is closer (in y-axis) to the click-point
     * @param event event with the click-point
     * @param previous a difference reaching out from the previous line
     * @param next a difference reaching out from the next line
     * @return
     */
    private Difference getCloserDifference(MouseEvent event, Difference previous, Difference next) {
        Difference returnedDiff = next;
        JTextComponent component = editorUI.getComponent();
        if (component != null) {
            BaseTextUI textUI = (BaseTextUI) component.getUI();
            try {
                Rectangle rec = textUI.modelToView(component, textUI.viewToModel(component, new Point(0, event.getY())));
                if (rec != null && event.getY() < rec.getY() + rec.getHeight() / 2) {
                    // previous difference is closer to the click
                    returnedDiff = previous;
                }
            } catch (BadLocationException ex) {
                // not interested, default next is returned
            }
        }
        return returnedDiff;
    }

    void onDiff(Difference diff) {
        try {
            DiffController view = DiffController.create(new SidebarStreamSource(true), new SidebarStreamSource(false));
            DiffTopComponent tc = new DiffTopComponent(view);
            tc.setName(NbBundle.getMessage(DiffSidebar.class, "CTL_DiffPanel_Title", new Object[] {fileObject.getNameExt()})); // NOI18N
            tc.open();
            tc.requestActive();
            view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, getDiffIndex(diff));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private int getDiffIndex(Difference diff) {
        for (int i = 0; i < currentDiff.length; i++) {
            if (diff == currentDiff[i]) {
                return i;
            }
        }
        return -1;
    }

    private int computeDocumentOffset(int lineOffset) {
        int end = Utilities.getRowStartFromLineOffset(document, lineOffset);
        if (end == -1) {
            Element lineRoot = document.getParagraphElement(0).getParentElement();
            for (end = lineRoot.getElement(lineOffset - 1).getEndOffset(); end > document.getLength(); end--) {
            }
        }
        return end;
    }

    void onRollback(Difference diff) {
        try {
            if (diff.getType() == Difference.ADD) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = computeDocumentOffset(diff.getSecondEnd());
                document.remove(start, end - start);
            } else if (diff.getType() == Difference.CHANGE) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = computeDocumentOffset(diff.getSecondEnd());
                document.replace(start, end - start, diff.getFirstText(), null);
            } else {
                int start = computeDocumentOffset(diff.getSecondStart());
                String newline = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart()) == -1 ? "\n" : "";
                document.insertString(start, newline + diff.getFirstText(), null);
            }
            refreshDiff();
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    boolean canRollback(Difference diff) {
        if (!(document instanceof GuardedDocument)) {
            return true;
        }
        int start, end;
        if (diff.getType() == Difference.DELETE) {
            start = end = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart());
        } else {
            start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
            end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
        }
        MarkBlockChain mbc = ((GuardedDocument) document).getGuardedBlockChain();
        return (mbc.compareBlock(start, end) & MarkBlock.OVERLAP) == 0;
    }
    
    void onPrevious(Difference diff) {
        diff = currentDiff[getDiffIndex(diff) - 1];
        Point location = scrollToDifference(diff);
        showTooltipWindow(location, diff);
        textComponent.repaint();
    }

    void onNext(Difference diff) {
        diff = currentDiff[getDiffIndex(diff) + 1];
        Point location = scrollToDifference(diff);
        showTooltipWindow(location, diff);
        textComponent.repaint();
    }

    private Point scrollToDifference(Difference diff) {
        int lineStart = diff.getSecondStart() - 1;
        int lineEnd = diff.getSecondEnd() - 1;
        if (lineStart == -1) {
            // the change was delete starting on the first line, show the diff on the next line
            // since in this case the file cannot be empty, 0 index does not throw BLE
            lineStart = 0;
        }
        if (diff.getType() == Difference.DELETE) {
            lineEnd = lineStart;
        }
        try {
            int visibleBorder = editorUI.getLineHeight() * 5;
            int startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) textComponent.getDocument(), lineStart);
            int endOffset = Utilities.getRowStartFromLineOffset((BaseDocument) textComponent.getDocument(), lineEnd);
            Rectangle startRect = textComponent.getUI().modelToView(textComponent, startOffset);
            Rectangle endRect = textComponent.getUI().modelToView(textComponent, endOffset);
            Rectangle visibleRect = new Rectangle(startRect.x - visibleBorder, startRect.y - visibleBorder, 
                                                  startRect.x, endRect.y - startRect.y + endRect.height + visibleBorder * 2);
            textComponent.scrollRectToVisible(visibleRect);
            
            Point p = new Point(endRect.x, endRect.y + endRect.height + 1);
            SwingUtilities.convertPointToScreen(p, textComponent);
            return p;
        } catch (BadLocationException e) {
            Logger.getLogger(DiffSidebar.class.getName()).log(Level.WARNING, "scrollToDifference", e); // NOI18N
        }
        return null;
    }
    
    String getMimeType() {
        if (textComponent instanceof JEditorPane) {
            return ((JEditorPane) textComponent).getContentType();
        }
        return "text/plain"; // NOI18N
    }

    public void fileFolderCreated(FileEvent fe) {
        // should not happen
    }

    public void fileDataCreated(FileEvent fe) {
        // should not happen
    }

    public void fileChanged(FileEvent fe) {
        // not interested
    }

    public void fileDeleted(FileEvent fe) {
        if (fileObject != null) {
            // needed since we are changing the fileObject instance
            fileObject.removeFileChangeListener(this);
            fileObject = null;
        }
        DataObject dobj = (DataObject) document.getProperty(Document.StreamDescriptionProperty);
        if (dobj != null) {
            fileObject = dobj.getPrimaryFile();
        }
        fileRenamed(null);
    }

    public void fileRenamed(FileRenameEvent fe) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }

    private static class DiffTopComponent extends TopComponent {
        
        private JComponent diffView;

        public DiffTopComponent() {
        }

        public DiffTopComponent(DiffController c) {
            this.diffView = c.getJComponent();
            setLayout(new BorderLayout());
            diffView.putClientProperty(TopComponent.class, this);
            
            DiffSidebarDiffPanel dsdp = new  DiffSidebarDiffPanel(c);
            add(dsdp);
            
//            getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
//            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
        }

        @Override
        public UndoRedo getUndoRedo() {
            UndoRedo undoredo = (UndoRedo) diffView.getClientProperty(UndoRedo.class);
            return undoredo == null ? UndoRedo.NONE : undoredo;
        }
        
        @Override
        public int getPersistenceType(){
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        protected String preferredID(){
            return "DiffSidebarTopComponent";    // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(getClass());
        }
    }

    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                if (component != null) {
                    BaseTextUI textUI = (BaseTextUI)component.getUI();
                    int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                    line = Utilities.getLineOffset(document, clickOffset);
                }
            }catch (BadLocationException ble){
                Logger.getLogger(DiffSidebar.class.getName()).log(Level.WARNING, "getLineFromMouseEvent", ble); // NOI18N
            }
        }
        return line;
    }

    void refresh() {
        if (!sidebarInComponentHierarchy) {
            return;
        }
        shutdown();
        initialize();
        refreshDiff();
        revalidate();  // resize the component
    }
        
    public void setSidebarVisible(boolean visible) {
        if (sidebarVisible == visible) {
            return;
        }
        sidebarVisible = visible;
        refreshDiff();
        revalidate();  // resize the component
    }

    @Override
    public void addNotify() {
        super.addNotify();
        sidebarInComponentHierarchy = true;
        initialize();
    }

    @Override
    public void removeNotify() {
        shutdown();
        sidebarInComponentHierarchy = false;
        super.removeNotify();
    }
    
    private void initialize() {
        assert SwingUtilities.isEventDispatchThread();

        document.addDocumentListener(this);
        textComponent.addComponentListener(this);
        foldHierarchy.addFoldHierarchyListener(this);
        refreshOriginalContent();
        if (fileObject != null) {
            fileObject.addFileChangeListener(this);
        }
    }

    private void shutdown() {
        assert SwingUtilities.isEventDispatchThread();

        if (fileObject != null) {
            fileObject.removeFileChangeListener(this);
        }
        foldHierarchy.removeFoldHierarchyListener(this);
        textComponent.removeComponentListener(this);
        document.removeDocumentListener(this);
    }

    private Reader getDocumentReader() {
        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return null;
        }

        return Utils.getDocumentReader(component.getDocument());
    }
    
    private void refreshDiff() {
        refreshDiffTask.schedule(50);
    }
        
    MarkProvider getMarkProvider() {
        return markProvider;
    }

    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[2048];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    static void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[2048];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        dim.width = sidebarVisible ? BAR_WIDTH : 0;
        return dim;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();
        if (clip.y >= 16) {
            // compensate for scrolling: marks on bottom/top edges are not drawn completely while scrolling
            clip.y -= 16;
            clip.height += 16;
        }

        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return;
        }

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        Difference [] paintDiff = currentDiff;        
        if (paintDiff == null || paintDiff.length == 0) {
            return;
        }
        
        try{
            int startPos = textUI.getPosFromY(clip.y);
            int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
            int rootViewCount = rootView.getViewCount();

            if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                // find the nearest visible line with an annotation
                Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                int y = (rec == null) ? 0 : rec.y;
                int [] yCoords = new int[3];

                int clipEndY = clip.y + clip.height;
                Element rootElem = textUI.getRootView(component).getElement();

                View view = rootView.getView(startViewIndex);
                int line = rootElem.getElementIndex(view.getStartOffset());
                line++; // make it 1-based
                if (line == 1 && paintDiff[0].getSecondStart() == 0 && paintDiff[0].getType() == Difference.DELETE) {
                    g.setColor(getColor(paintDiff[0]));
                    yCoords[0] = y - editorUI.getLineAscent() / 2;
                    yCoords[1] = y;
                    yCoords[2] = y + editorUI.getLineAscent() / 2;
                    g.fillPolygon(new int [] { 0, BAR_WIDTH, 0 }, yCoords, 3);
                }
                
                for (int i = startViewIndex; i < rootViewCount; i++){
                    view = rootView.getView(i);
                    line = rootElem.getElementIndex(view.getStartOffset());
                    line++; // make it 1-based
                    Difference ad = getDifference(line);
                    if (ad != null) {
                        g.setColor(getColor(ad));
                        if (ad.getType() == Difference.DELETE) {
                            yCoords[0] = y + editorUI.getLineAscent();
                            yCoords[1] = y + editorUI.getLineAscent() * 3 / 2;
                            yCoords[2] = y + editorUI.getLineAscent() * 2;
                            g.fillPolygon(new int [] { 2, BAR_WIDTH, 2 }, yCoords, 3);
                            g.setColor(colorBorder);
                            g.drawLine(2, yCoords[0], 2, yCoords[2] - 1);
                        } else {
                            g.fillRect(3, y, BAR_WIDTH - 3, editorUI.getLineHeight());
                            g.setColor(colorBorder);
                            int y1 = y + editorUI.getLineHeight();
                            g.drawLine(2, y, 2, y1);
                            if (ad.getSecondStart() == line) {
                                g.drawLine(2, y, BAR_WIDTH - 1, y);
                            }
                            g.drawLine(2, y1, BAR_WIDTH - 1, y1);
                        }
                    }
                    y += editorUI.getLineHeight();
                    if (y >= clipEndY) {
                        break;
                    }
                }
            }
        } catch (BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        }
    }

    private Color getColor(Difference ad) {
        if (ad.getType() == Difference.ADD) {
            return colorAdded;
        }
        if (ad.getType() == Difference.CHANGE) {
            return colorChanged;
        }
        return colorRemoved;
    }

    private Difference getDifference(int line) {
        if (line < 0) {
            return null;
        }
        for (int i = 0; i < currentDiff.length; i++) {
            Difference difference = currentDiff[i];
            if (line < difference.getSecondStart()) {
                return null;
            }
            if ((difference.getType() == Difference.DELETE) && (line == difference.getSecondStart())) {
                return difference;
            }
            if (line <= difference.getSecondEnd()) {
                return difference;
            }
        }
        return null;
    }

    private Color backgroundColor() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }

        return Color.WHITE;
    }

    public void insertUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void removeUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void changedUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        repaint();
    }

    /**
     * Integration provider for the error stripe.
     */
    private class DiffMarkProvider extends MarkProvider {

        private List<DiffMark> marks;

        public DiffMarkProvider() {
            marks = getMarksForDifferences();
        }

        public List getMarks() {
            return marks;
        }

        void refresh() {
            List<DiffMark> oldMarks = marks;
            marks = getMarksForDifferences();
            firePropertyChange(PROP_MARKS, oldMarks, marks);
        }

        private List<DiffMark> getMarksForDifferences() {
            if ((currentDiff == null) || !isVisible() || (getWidth() <= 0)) {
                return Collections.emptyList();
            }
            List<DiffMark> marksList = new ArrayList<DiffMark>(currentDiff.length);
            for (int i = 0; i < currentDiff.length; i++) {
                Difference difference = currentDiff[i];
                marksList.add(new DiffMark(difference, getColor(difference)));
            }
            return marksList;
        }
    }

    /**
     * RP task to compute new diff after a change in the document or a change in the base text.
     */
    public class RefreshDiffTask implements Runnable {

        public void run() {
            computeDiff();
            repaint();
            markProvider.refresh();
        }

        private void computeDiff() {
            if (!sidebarVisible) {
                currentDiff = null;
                return;
            }
            fetchOriginalContent();
            if (originalContentBuffer == null) {
                currentDiff = null;
                return;
            }
            Reader working = getDocumentReader();
            if (working == null) {
                // TODO: what to do in this case? let's keep the old dirrerence set for now
                return;
            }
            DiffProvider diff = Lookup.getDefault().lookup(DiffProvider.class);
            if (diff == null) {
                currentDiff = null;
                return;
            }
            try {
                currentDiff = diff.computeDiff(new StringReader(originalContentBuffer), working);
            } catch (IOException e) {
                currentDiff = null;
            }
        }

        private void fetchOriginalContent() {
            int serial = originalContentSerial;
            if ((originalContentBuffer != null) && (originalContentBufferSerial == serial)) {
                return;
            }
            originalContentBufferSerial = serial;

            Reader r = getText(ownerVersioningSystem);
            if (r == null) {
                originalContentBuffer = null;
                return;
        }

            StringWriter w = new StringWriter(2048);
            try {
                copyStreamsCloseAll(w, r);
                originalContentBuffer = w.toString();
            } catch (IOException e) {
                // ignore, we will show no diff
    }
        }
    }

    /**
     * Gets the original content of the working copy. This method is typically only called after the OriginalContent
     * object is created and once for every property change event. 
     * 
     * @param oc current OriginalContent
     * @return Reader original content of the working copy or null if the original content is not available
     */ 
    private Reader getText(VersioningSystem vs) {
        if (vs == null) {
            return null;
        }

        Collection<File> filesToCheckout = getFiles(fileObject);
        if (filesToCheckout.isEmpty()) {
            return null;
        }

        File tempFolder = Utils.getTempFolder();
        tempFolder.deleteOnExit();

        Collection<File> originalFiles = null;
        DiffFileEncodingQueryImpl encodinqQuery = null;
        try {            
            originalFiles = checkoutOriginalFiles(filesToCheckout, tempFolder, vs);

            encodinqQuery = Lookup.getDefault().lookup(DiffFileEncodingQueryImpl.class);
            if(encodinqQuery != null) {
                encodinqQuery.associateEncoding(fileObject, originalFiles);
            }
            return createReader(new File(tempFolder, fileObject.getNameExt()));
        } catch (Exception e) {
            // let providers raise errors when they feel appropriate
            return null;
        } finally {
            if ((originalFiles != null) && (encodinqQuery != null)) {
                encodinqQuery.resetEncodingForFiles(originalFiles);
            }
        }
    }

    /**
     * Returns files that belong to the same {@code DataObject} as the given
     * file.
     * @param  fileObj  file whose siblings are to be found
     * @return  list of files that belong to the {@code DataObject} determined
     *              by the given {@code FileObject},
     *          or a singleton collection containing just the {@code File}
     *              corresponding to the given {@code FileObject},
     *          or an empty collection if the given {@code FileObject} could
     *              not be translated to a {@code File}
     */
    private static Collection<File> getFiles(FileObject fileObj) {
        File file = FileUtil.toFile(fileObj);
        if (file == null) {
            return Collections.emptyList();
        }

        Collection<File> files;
        try {
            Set<FileObject> fileObjects = DataObject.find(fileObj).files();
            assert fileObjects.contains(fileObj);
            if (fileObjects.size() == 1) {
                files = Collections.singleton(file);
            } else {
                files = new ArrayList<File>(fileObjects.size());
                for (FileObject fo : fileObjects) {
                    files.add(FileUtil.toFile(fo));
                }
            }
        } catch (DataObjectNotFoundException e) {
            // no dataobject, never mind
            files = Collections.singleton(file);
        }
        assert files.contains(file);
        return files;
    }

    /**
     * Checks out original (unmodified) content of the given files to the given
     * target folder.
     * @param  filesToCheckout  files whose original content is to be checked out
     * @param  targetFolder  target folder where unmodified files should be stored
     * @param  vs  versioning system to be used for the checkout
     * @return  collection of files containing unmodified content of the given
     *          files; these files are named after the given files
     */
    private Collection<File> checkoutOriginalFiles(Collection<File> filesToCheckout,
                                                   File targetFolder,
                                                   VersioningSystem vs) {
        Collection<File> originalFiles = new ArrayList<File>(filesToCheckout.size());

        for (File file : filesToCheckout) {
            File originalFile = new File(targetFolder, file.getName());
            originalFile.deleteOnExit();
            vs.getOriginalFile(file, originalFile);
            originalFiles.add(originalFile);
        }

        return originalFiles;
    }

    private Reader createReader(File file) {
        try {
            /*
             * Text returned by EditorCookie.openDocument.getText(...)
             * may differ from the raw text contained in the file.
             * For example, this is the case of FormDataObject, which trims
             * some special comments (tags) while the Java source file is
             * being loaded to the editor. When the file is being saved,
             * the special comments are written to the raw file.
             * This is the reason why text for the diff is read using
             * the EditorCookie, instead of reading it directly from the
             * File or FileObject.
             */
            FileObject fo = FileUtil.toFileObject(file);
            DataObject dao = DataObject.find(fo);
            Document doc = null;
            if (dao instanceof MultiDataObject) {
                MultiDataObject mdao = (MultiDataObject) dao;
                for (MultiDataObject.Entry entry : mdao.secondaryEntries()) {
                    if (fo.equals(entry.getFile()) && entry instanceof CookieSet.Factory) {
                        CookieSet.Factory factory = (CookieSet.Factory) entry;
                        EditorCookie ec = factory.createCookie(EditorCookie.class);
                        doc = ec.openDocument();
                    }
                }
            }
            if (doc == null) {
                EditorCookie ec = dao.getCookie(EditorCookie.class);
                doc = ec.openDocument();
            }
            return new StringReader(doc.getText(0, doc.getLength()));
        } catch (Exception e) {
            // something's wrong, read the file from disk
        }
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private class SidebarStreamSource extends StreamSource {

        private final boolean isFirst;

        public SidebarStreamSource(boolean isFirst) {
            this.isFirst = isFirst;
        }

        @Override
        public boolean isEditable() {
            return !isFirst;
        }

        @Override
        public Lookup getLookup() {
            if (isFirst) {
                return super.getLookup();
            }
            return Lookups.fixed(document);
        }

        public String getName() {
            return fileObject.getNameExt();
        }

        public String getTitle() {
            if (isFirst) {
                return NbBundle.getMessage(DiffSidebar.class, "LBL_DiffPane_Original"); // NOI18N
            } else {
                return NbBundle.getMessage(DiffSidebar.class, "LBL_DiffPane_WorkingCopy"); // NOI18N
            }
        }

        public String getMIMEType() {
            return getMimeType();
        }

        public Reader createReader() throws IOException {
            return isFirst ? new StringReader(originalContentBuffer) : getDocumentReader();
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
