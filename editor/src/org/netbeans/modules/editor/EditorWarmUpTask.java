/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.View;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.modules.editor.java.JCStorage;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.plain.PlainKit;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * "Warm-up" task for editor. Executed after IDE startup, it should
 * pre-initialize some suitable parts of the module to improve first time usage
 * experience - which might suffer from long response time due to class loading
 * and various initialization.
 * See {@link org.netbeans.core.AfterStartWarmUp} for details about how the task is run.
 *
 * @author  Tomas Pavek, Martin Roskanin
 */

public class EditorWarmUpTask implements Runnable{
    
    /**
     * Number of lines that an artificial document
     * for view hierarchy code optimization will have.
     * <br>
     * The default threshold for hotspot method compilation
     * is 1500 invocations.
     */
    private static final int ARTIFICIAL_DOCUMENT_LINE_COUNT = 1700;

    /**
     * Number of times a long document is assigned to the editor pane
     * which causes the view hierarchy for it to be (re)built.
     */
    private static final int VIEW_HIERARCHY_CREATION_COUNT = 1;
    
    /**
     * Width of buffered image area.
     */
    private static final int IMAGE_WIDTH = 600;
    
    /**
     * Height of buffered image area.
     */
    private static final int IMAGE_HEIGHT = 400;
    
    /**
     * Number of paints to be simulated.
     */
    private static final int PAINT_COUNT = 30;
    

    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.warmup"); // NOI18N
    
    
    private void sampleDirParsing(){
        File userdir = new File(System.getProperty("netbeans.user", ""),"sampledir"); //NOI18N
        String fsName = userdir.getAbsolutePath().replace('\\','/');
        JCStorage.getStorage().parseFSOnBackground(Repository.getDefault().findFileSystem(fsName));
    }
    
    public void run() {
        long startTime = System.currentTimeMillis();
        
        // initializing code completion database. Reading *.jcs files and creating memory map of available 
        // completin classes
        JCStorage.getStorage();
        if (debug){
            System.out.println("Storage initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Parsing of sampledir, that is mounted by default.
        // The autoupdate of that filesystem cannot start as it starts only after 
        // mount action.
        //sampleDirParsing();
        if (debug){
            System.out.println("Sample dir parsed:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Initialization of editor settings initializers and PrintOptions.
        EditorModule.init();        

        // Init of JavaKit and JavaOptions
        BaseKit javaKit = BaseKit.getKit(JavaKit.class);
        BaseKit plainKit = BaseKit.getKit(PlainKit.class);
        
        //creating actions instances
        javaKit.getActions();
        

        // Start of a code block that tries to force hotspot to compile
        // the view hierarchy and related classes for faster performance
        if (debug) {
            startTime = System.currentTimeMillis();
        }

        // Work with artificial frame that will host an editor pane
        final JEditorPane pane = new JEditorPane();
        pane.setEditorKit(javaKit);

        // Obtain extended component (with editor's toolbar and scrollpane)
        EditorUI editorUI = Utilities.getEditorUI(pane);
        if (editorUI != null) {
            // Make sure extended component necessary classes get loaded
            editorUI.getExtComponent();
        }

        // Have two documents - one empty and another one filled with many lines
        Document emptyDoc = javaKit.createDefaultDocument();
        Document longDoc = pane.getDocument();
        
        try {
            
            // Fill the document with data.
            // Number of lines is more important here than number of columns in a line
            // Do one big insert instead of many small inserts
            StringBuffer sb = new StringBuffer();
            for (int i = ARTIFICIAL_DOCUMENT_LINE_COUNT; i > 0; i--) {
                sb.append("int ident = 1; // comment\n"); // NOI18N
            }
            longDoc.insertString(0, sb.toString(), null);

            // Switch between empty doc and long several times
            // to force view hierarchy creation
            for (int i = 0; i < VIEW_HIERARCHY_CREATION_COUNT; i++) {
                pane.setDocument(emptyDoc);
                
                // Set long doc - causes view hierarchy to be rebuilt
                pane.setDocument(longDoc);
            }

            // Create buffered image for painting simulation
            BufferedImage bImage = new BufferedImage(
                IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            final Graphics bGraphics = bImage.getGraphics();
            bGraphics.setClip(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                    
            // Do view-related operations
            AbstractDocument doc = (AbstractDocument)pane.getDocument();
            doc.readLock();
            try {
                final View rootView = Utilities.getDocumentView(pane);
                LockView lockView = LockView.get(rootView);
                lockView.lock();
                try {
                    int viewCount = rootView.getViewCount();

                    // Force switch the line views from estimated spans to exact measurements
                    Runnable resetChildrenEstimatedSpans = new Runnable() {
                        public void run() {
                            int cnt = rootView.getViewCount();                            
                            for (int j = 0; j < cnt; j++) {
                                View v = rootView.getView(j);
                                if (v instanceof EstimatedSpanView) {
                                    ((EstimatedSpanView)v).setEstimatedSpan(false);
                                }
                            }
                        }
                    };
                    if (rootView instanceof org.netbeans.lib.editor.view.GapDocumentView) {
                        ((org.netbeans.lib.editor.view.GapDocumentView)rootView).
                            renderWithUpdateLayout(resetChildrenEstimatedSpans);
                    } else { // not specialized instance => run normally
                        resetChildrenEstimatedSpans.run();
                    }

                    // Get child allocation for each line
                    for (int j = 0; j < viewCount; j++) {
                        Rectangle alloc = new Rectangle(0, 0,
                            (int)rootView.getPreferredSpan(View.X_AXIS),
                            (int)rootView.getPreferredSpan(View.Y_AXIS)
                        );
                        rootView.getChildAllocation(j, alloc);
                    }

                    // Test modelToView and viewToModel
                    float rootViewYSpan = rootView.getPreferredSpan(View.Y_AXIS);
                    float maybeLineSpan = rootViewYSpan / viewCount;
                    Point point = new Point();
                    point.x = 5; // likely somewhere inside the first char on the line
                    for (int j = 0; j < viewCount; j++) {
                        pane.modelToView(rootView.getView(j).getStartOffset());

                        point.y = (int)(j * maybeLineSpan);
                        int pos = pane.viewToModel(point);
                    }

                    int rootViewWidth = (int)rootView.getPreferredSpan(View.X_AXIS);
                    int rootViewHeight = (int)rootView.getPreferredSpan(View.Y_AXIS);
                    Rectangle alloc = new Rectangle(0, 0, rootViewWidth, rootViewHeight);
                    
                    // Paint into buffered image
                    for (int i = PAINT_COUNT - 1; i >= 0; i--) {
                        rootView.paint(bGraphics, alloc);
                    }

                } finally {
                    lockView.unlock();
                }
            } finally {
                doc.readUnlock();
            }
            
            final javax.swing.JFrame frame = new javax.swing.JFrame();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EditorUI ui = Utilities.getEditorUI(pane);
                    JComponent mainComp = null;
                    if (ui != null) {
                        mainComp = ui.getExtComponent();
                    }
                    if (mainComp == null) {
                        mainComp = new javax.swing.JScrollPane(pane);
                    }
                    frame.getContentPane().add(mainComp);
                    frame.pack();
                    frame.paint(bGraphics);
                }
            });

        } catch (BadLocationException e) {
        }

        // Candidates Annotations.getLineAnnotations()
            
        if (debug) {
            System.out.println("View hierarchy initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
    }
}
