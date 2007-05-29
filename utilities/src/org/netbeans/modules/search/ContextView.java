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

package org.netbeans.modules.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreePath;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static java.lang.Thread.NORM_PRIORITY;

/**
 * Panel for displaying context of a matching string within a file.
 * When a node representing a matching string is selected in the tree
 * of results, this panel displays a part of the file surrounding the selected
 * matching string, with the matching string highlighted.
 * When a node representing the whole file is selected, the beginning
 * of the file is displayed.
 *
 * @author  Tim Boudreau
 * @author  Marian Petras
 */
final class ContextView extends JPanel implements TreeSelectionListener {
    
    /** */
    private static final String FILE_VIEW = "file view";                //NOI18N
    /** */
    private static final String MESSAGE_VIEW = "message view";          //NOI18N
    
    /** */
    private final CardLayout cardLayout;
    /** editor pane actually displaying (part of) the file */
    private final JEditorPane editorPane = new JEditorPane();
    /** scroll pane containing the editor pane */
    private final JScrollPane editorScroll;
    /** displays location of the file above the editor pane */
    private final JLabel lblPath = new JLabel();
    /** displays message if no file is displayed */
    private final JLabel lblMessage = new JLabel();
    /**
     * displays content of file after it has been asynchronously loaded
     * by the {@link #requestProcessor}
     */
    private final Displayer displayer = new Displayer();
    /** used for asynchronous loading of files' contents */
    private final RequestProcessor requestProcessor
            = new RequestProcessor("TextView", NORM_PRIORITY, true);    //NOI18N
    
    /** */
    private ResultModel resultModel;
    /** */
    private RequestProcessor.Task task = null;
    /** */
    private TextFetcher textFetcher = null;
    /** */
    private String displayedCard = null;
    /** */
    private String msgNoFileSelected = null;
    /** */
    private String msgMultipleFilesSelected = null;
    /** the current MIME-type set for the {@link #editorPane} */
    private String editorMimeType = null;
    
    /**
     * 
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    public ContextView(ResultModel resultModel) {
        Border b = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(        //outside border
                                0, 0, 1, 0, 
                                UIManager.getColor("controlShadow")),   //NOI18N
                BorderFactory.createEmptyBorder(        //inside border
                                5, 5, 1, 5));
        lblPath.setBorder(b);
        
        editorPane.setEditable(false);
        editorPane.getCaret().setBlinkRate(0);
        
        editorScroll = new JScrollPane(editorPane);
        editorScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        editorScroll.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel fileViewPanel = new JPanel();
        fileViewPanel.setLayout(new BorderLayout());
        fileViewPanel.add(lblPath, BorderLayout.NORTH);
        fileViewPanel.add(editorScroll, BorderLayout.CENTER);
        
        Box messagePanel = Box.createVerticalBox();
        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(lblMessage);
        messagePanel.add(Box.createVerticalGlue());
        lblMessage.setAlignmentX(0.5f);
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        lblMessage.setEnabled(false);
        
        setLayout(cardLayout = new CardLayout());
        add(fileViewPanel, FILE_VIEW);
        add(messagePanel, MESSAGE_VIEW);
        
        setResultModel(resultModel);
    }
    
    @Override
    public Dimension getMinimumSize() {
        /*
         * Without this, the minimum width would be equal to the width
         * of the {@linkplain #lblPath file path label}.
         */
        Dimension minSize = super.getMinimumSize();
        minSize.width = 0;
        return minSize;
    }
    
    /**
     */
    void setResultModel(ResultModel resultModel) {
        if (resultModel == this.resultModel) {
            return;
        }
        
        synchronized (this) {           //PENDING - review synchronization
            if (textFetcher != null) {
                textFetcher.cancel();
                textFetcher = null;
            }
        }
        this.resultModel = resultModel;
    }
    
    /**
     */
    void bindToTreeSelection(final JTree tree) {
        assert EventQueue.isDispatchThread();
        
        displaySelectedFiles(tree);
        tree.addTreeSelectionListener(this);
    }
    
    /**
     */
    void unbindFromTreeSelection(final JTree tree) {
        assert EventQueue.isDispatchThread();
        
        tree.removeTreeSelectionListener(this);
        
        synchronized (this) {           //PENDING - review synchronization
            if (textFetcher != null) {
                textFetcher.cancel();
                textFetcher = null;
            }
        }
    }

    /**
     * Called when selection of nodes in the result tree changes.
     */
    public void valueChanged(TreeSelectionEvent e) {
        displaySelectedFiles((JTree) e.getSource());
    }
    
    /**
     * Displays file(s) selected in the given tree.
     * 
     * @author  Marian Petras
     */
    private void displaySelectedFiles(final JTree tree) {
        final TreePath[] selectedPaths = tree.getSelectionPaths();
        if ((selectedPaths == null) || (selectedPaths.length == 0)) {
            displayNoFileSelected();
        } else if (selectedPaths.length > 1) {
            displayMultipleItemsSelected();
        } else {
            assert selectedPaths.length == 1;
            
            final TreePath path = selectedPaths[0];
            int pathCount = path.getPathCount();
            if (pathCount == 1) {                   //root node selected
                displayNoFileSelected();
            } else {
                assert pathCount == 2 || pathCount == 3;
                MatchingObject matchingObj;
                int matchIndex;
                if (pathCount == 2) {               //file node selected
                    matchingObj = (MatchingObject) path.getLastPathComponent();
                    matchIndex = -1;
                } else {                            //match node selected
                    TreePath matchingObjPath = path.getParentPath();
                    matchingObj = (MatchingObject)
                                  matchingObjPath.getLastPathComponent();
                    int matchingObjRow = tree.getRowForPath(matchingObjPath);
                    int matchRow = tree.getRowForPath(path);
                    matchIndex = matchRow - matchingObjRow - 1;
                }
                displayFile(matchingObj, matchIndex);
            }
        }
    }
    
    /**
     */
    private void displayNoFileSelected() {
        if (msgNoFileSelected == null) {
            msgNoFileSelected = NbBundle.getMessage(
                                            getClass(),
                                            "MsgNoFileSelected");       //NOI18N
        }
        displayMessage(msgNoFileSelected);
    }
    
    /**
     */
    private void displayMultipleItemsSelected() {
        if (msgMultipleFilesSelected == null) {
            msgMultipleFilesSelected = NbBundle.getMessage(
                                            getClass(),
                                            "MsgMultipleFilesSelected");//NOI18N
        }
        displayMessage(msgMultipleFilesSelected);
    }
    
    /**
     */
    private void displayMessage(String message) {
        lblMessage.setText(message);
        if (displayedCard != MESSAGE_VIEW) {
            cardLayout.show(this, displayedCard = MESSAGE_VIEW);
        }
    }
    
    /**
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    private void displayFile(final MatchingObject matchingObj,
                             final int partIndex) {
        assert EventQueue.isDispatchThread();
        
        synchronized (displayer) {          //PENDING - review synchronization
            if (task != null) {
                task.cancel();
                task = null;
            }
            
            final Item item = new Item(resultModel, matchingObj, partIndex);
            
            MatchingObject.InvalidityStatus invalidityStatus
                                            = matchingObj.checkValidity();
            if (invalidityStatus != null) {
                displayMessage(invalidityStatus.getDescription(
                                            matchingObj.getFile().getPath()));
                return;
            }
            
            requestText(item, displayer);
            String description = matchingObj.getDescription();
            lblPath.setText(description);
            lblPath.setToolTipText(description);        //in case it doesn't fit
        }
    }
    
    /**
     * Fetch the text of an {@code Item}. Since the text is retrieved
     * asynchronously, this method is passed a {@code TextDisplayer},
     * which will get its {@code setText()} method called on the event thread
     * after it has been loaded on a background thread.
     * 
     * @param  item  item to be displayed by the text displayer
     * @param  textDisplayer  displayer that should display the item
     * 
     * @author  Tim Boudreau
     */
    private void requestText(Item item, TextDisplayer textDisplayer) {
        assert EventQueue.isDispatchThread();
        
        synchronized (this) {           //PENDING - review synchronization
            if (textFetcher != null) {
                if (textFetcher.replaceLocation(item, textDisplayer)) {
                    return;
                } else {
                    textFetcher.cancel();
                    textFetcher = null;
                }
            }
            if (textFetcher == null) {
                textFetcher = new TextFetcher(item,
                                              textDisplayer,
                                              requestProcessor);
            }
        }
    }

    /**
     * Implementation of {@code TextDisplayer} which is passed to get the text
     * of an item.  The text is fetched from the file asynchronously, and then
     * passed to {@link #setText()} to set the text, select the text the item
     * represents and scroll it into view.
     * 
     * @see  TextReceiver
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    private class Displayer implements TextDisplayer, Runnable {
        
        private TextDetail location;
        
        /**
         * @author  Tim Boudreau
         */
        public void setText(final String text,
                            String mimeType,
                            final TextDetail location) {
            assert EventQueue.isDispatchThread();
            
            if ("content/unknown".equals(mimeType)) {                   //NOI18N
                mimeType = "text/plain";  //Good idea? Bad? Hmm...      //NOI18N
            }
            
            /*
             * Changing content type clears the text - so the content type
             * (in this case, MIME-type only) must be set _before_ the text
             * is set.
             */
            if ((editorMimeType == null) || !editorMimeType.equals(mimeType)) {
                editorPane.setContentType(mimeType);
                editorMimeType = mimeType;
            }
            editorPane.setText(text);
            
            if (displayedCard != FILE_VIEW) {
                cardLayout.show(ContextView.this, displayedCard = FILE_VIEW);
            }
            
            if (location != null) {
                //Let the L&F do anything it needs to do before we try to fiddle
                //with it - get out of its way.  Some Swing View classes don't
                //have accurate position data until they've painted once.
                this.location = location;
                EventQueue.invokeLater(this);
            } else {
                scrollToTop();
            }
        }

        /**
         * 
         * @author  Tim Boudreau
         * @author  Marian Petras
         */
        public void run() {
            assert EventQueue.isDispatchThread();
            
            boolean scrolled = false;
            try {
                if (!editorPane.isShowing()) {
                    return;
                }
                
                if (location != null) {
                    final Document document = editorPane.getDocument();
                    if (document instanceof StyledDocument) {
                        StyledDocument styledDocument
                                = (StyledDocument) document;
                        int cursorOffset = getCursorOffset(
                                                    (StyledDocument) document,
                                                    location.getLine() - 1);
                        int startOff = cursorOffset + location.getColumn() - 1;
                        int endOff = startOff + location.getMarkLength();
                        editorPane.setSelectionStart(startOff);
                        editorPane.setSelectionEnd(endOff);
                        Rectangle r = editorPane.modelToView(startOff);
                        if (r != null) {
                            //Editor kit not yet updated, what to do
                            editorPane.scrollRectToVisible(r);
                            scrolled = true;
                        }
                    }
                    editorPane.getCaret().setBlinkRate(0);
                    editorPane.repaint();
                }
            } catch (BadLocationException e) {
                //Maybe not even notify this - not all editors
                //will have a 1:1 correspondence to file positions -
                //it's perfectly reasonable for this to be thrown
                ErrorManager.getDefault().notify(      //PENDING - ErrorManager?
                        ErrorManager.INFORMATIONAL, e);
            }
            if (!scrolled) {
                scrollToTop();
            }
        }
        
        /**
         * Computes cursor offset of a given line of a document.
         * The line number must be non-negative.
         * If the line number is greater than number of the last line,
         * the returned offset corresponds to the last line of the document.
         *
         * @param  doc  document to computer offset for
         * @param  line  line number (first line = <code>0</code>)
         * @return  cursor offset of the beginning of the given line
         * 
         * @author  Marian Petras
         */
        private int getCursorOffset(StyledDocument doc, int line) {
            assert EventQueue.isDispatchThread();
            assert line >= 0;

            try {
                return NbDocument.findLineOffset(doc, line);
            } catch (IndexOutOfBoundsException ex) {
                /* probably line number out of bounds */

                Element lineRootElement = NbDocument.findLineRootElement(doc);
                int lineCount = lineRootElement.getElementCount();
                if (line >= lineCount) {
                    return NbDocument.findLineOffset(doc, lineCount - 1);
                } else {
                    throw ex;
                }
            }
        }
    
        /**
         */
        private void scrollToTop() {
            JScrollBar scrollBar;

            scrollBar = editorScroll.getHorizontalScrollBar();
            scrollBar.setValue(scrollBar.getMinimum());

            scrollBar = editorScroll.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMinimum());
        }
        
    }

}
