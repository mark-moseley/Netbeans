/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.visualizer;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;

import org.openide.util.HelpCtx;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

import org.netbeans.api.diff.Difference;

//import org.netbeans.modules.vcscore.util.Debug;
//import org.netbeans.modules.vcscore.util.TopComponentCloseListener;

/**
 * This class displays two editor panes with two files and marks the differences
 * by a different color.
 *
 * @author  Martin Entlicher
 */
public class DiffComponent extends org.openide.windows.TopComponent {

    private static final java.awt.Color colorMissing = new java.awt.Color(255, 160, 180);
    private static final java.awt.Color colorAdded = new java.awt.Color(180, 255, 180);
    private static final java.awt.Color colorChanged = new java.awt.Color(160, 200, 255);
    
    //private AbstractDiff diff = null;
    private List diffs = null;
    private DiffPanel diffPanel = null;
    
    //private ArrayList closeListeners = new ArrayList();
    private int currentDiffLine = -1;
    
    /**
     * Used for deserialization.
     */
    private boolean diffSetSuccess = true;

    static final long serialVersionUID =3683458237532937983L;
    
    /**
     * An empty constructor needed by deserialization process.
     */
    public DiffComponent() {
        putClientProperty("PersistenceType", "Never");
    }
    
    /** Creates new DiffComponent from list of Difference objects */
    public DiffComponent(final List diffs, final String mainTitle, final String mimeType,
                         final String sourceName1, final String sourceName2,
                         final String title1, final String title2,
                         final Reader r1, final Reader r2) {
        this.diffs = diffs;
        setLayout(new BorderLayout());
        diffPanel = new DiffPanel();
        diffPanel.addPrevLineButtonListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (diffs.size() == 0) return ;
                currentDiffLine--;
                if (currentDiffLine < 0) currentDiffLine = diffs.size() - 1;
                Difference diff = (Difference) diffs.get(currentDiffLine);
                int line = diff.getFile1Line1();
                if (diff.getType() == Difference.ADD) line++;
                int lf1 = diff.getFile1Line2() - diff.getFile1Line1() + 1;
                int lf2 = diff.getFile2Line2() - diff.getFile2Line1() + 1;
                int length = Math.max(lf1, lf2);
                diffPanel.setCurrentLine(line, length);
            }
        });
        diffPanel.addNextLineButtonListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (diffs.size() == 0) return ;
                currentDiffLine++;
                if (currentDiffLine >= diffs.size()) currentDiffLine = 0;
                Difference diff = (Difference) diffs.get(currentDiffLine);
                int line = diff.getFile1Line1();
                if (diff.getType() == Difference.ADD) line++;
                int lf1 = diff.getFile1Line2() - diff.getFile1Line1() + 1;
                int lf2 = diff.getFile2Line2() - diff.getFile2Line1() + 1;
                int length = Math.max(lf1, lf2);
                diffPanel.setCurrentLine(line, length);
            }
        });
        
        add(diffPanel, BorderLayout.CENTER);
//        initComponents ();
        //setTitle(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        if (mainTitle == null) {
            setName(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        } else {
            setName(mainTitle);
        }
        initContent(mimeType, sourceName1, sourceName2, title1, title2, r1, r2);
        //HelpCtx.setHelpIDString (getRootPane (), DiffComponent.class.getName ());
        putClientProperty("PersistenceType", "Never");
    }
    
    private void initContent(String mimeType, String sourceName1, String sourceName2,
                             String title1, String title2, Reader r1, Reader r2) {
        setMimeType1(mimeType);
        setMimeType2(mimeType);
        try {
            setSource1(r1);
            setSource2(r2);
        } catch (IOException ioex) {
            org.openide.TopManager.getDefault().notifyException(ioex);
        }
        setSource1Title(title1);
        setSource2Title(title2);
        insertEmptyLines(true);
        setDiffHighlight(true);
    }

    
/*    private java.awt.Container getContentPane() {
        return this;
        //return getRootPane();
    }
  */  
    private void addWindowListener(java.awt.event.WindowListener listener) {
        java.awt.Component ancestor = getTopLevelAncestor();
        if (ancestor instanceof java.awt.Window) {
            ((java.awt.Window) ancestor).addWindowListener(listener);
        }
    }
    
    /*
    void addCloseListener(TopComponentCloseListener listener) {
        closeListeners.add(listener);
    }
     */
    
    protected Mode getDockingMode(Workspace workspace) {
        Mode mode = workspace.findMode(CloneableEditorSupport.EDITOR_MODE);
        if (mode == null) {
            mode = workspace.createMode(
                CloneableEditorSupport.EDITOR_MODE, getName(),
                CloneableEditorSupport.class.getResource(
                "/org/openide/resources/editorMode.gif" // NOI18N
            ));
        }
        return mode;
    }
    
    public void open(Workspace workspace) {
        //System.out.println("workspace = "+workspace);
        if (workspace == null) {
            workspace = org.openide.TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        }
        Mode editorMode = getDockingMode(workspace);
        editorMode.dockInto(this);
        super.open(workspace);
        diffPanel.open();
        requestFocus();
    }
    
    /**
     * Transfer the focus to the diff panel.
     */
    public void requestFocus() {
        super.requestFocus();
        diffPanel.requestFocus();
    }
    
    /**
     * Override for clean up reasons.
     * Will be moved to the appropriate method when will be made.
     */
    public boolean canClose(Workspace workspace, boolean last) {
        boolean can = super.canClose(workspace, last);
        if (last && can) {
            exitForm(null);
        }
        return can;
    }
    /*
    public void removeNotify() {
        System.out.println("removeNotify() called");
        exitForm(null);
        super.removeNotify();
    }
     */
    
    public void setSource1(Reader r) throws IOException {
        diffPanel.setSource1(r);
    }
    
    public void setSource2(Reader r) throws IOException {
        diffPanel.setSource2(r);
    }
    
    public void setSource1Title(String title) {
        diffPanel.setSource1Title(title);
    }
    
    public void setSource2Title(String title) {
        diffPanel.setSource2Title(title);
    }
    
    public void setMimeType1(String mime) {
        diffPanel.setMimeType1(mime);
    }
    
    public void setMimeType2(String mime) {
        diffPanel.setMimeType2(mime);
    }
    
    public void setDocument1(Document doc) {
        diffPanel.setDocument1(doc);
    }
    
    public void setDocument2(Document doc) {
        diffPanel.setDocument2(doc);
    }
    
    private void setHighlight(StyledDocument doc, int line1, int line2, java.awt.Color color) {
    }
    
    private void unhighlight(StyledDocument doc) {
    }
    
    public void unhighlightAll() {
        diffPanel.unhighlightAll();
    }
    
    public void highlightRegion1(int line1, int line2, java.awt.Color color) {
        //D.deb("Highlight region 1"); // NOI18N
        diffPanel.highlightRegion1(line1, line2, color);
    }
    
    public void highlightRegion2(int line1, int line2, java.awt.Color color) {
        //D.deb("Highlight region 2"); // NOI18N
        diffPanel.highlightRegion2(line1, line2, color);
    }
    
    public void addEmptyLines1(int line, int numLines) {
        diffPanel.addEmptyLines1(line, numLines);
    }
    
    public void addEmptyLines2(int line, int numLines) {
        diffPanel.addEmptyLines2(line, numLines);
    }
        
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        //try {
        out.writeObject(diffs);
        /*
        } catch (IOException exc) {
            System.out.println("exc = "+exc);
            exc.printStackTrace();
            throw exc;
        }
         */
    }
    
    private void insertEmptyLines(boolean updateActionLines) {
        int n = diffs.size();
        int ins1 = 0;
        int ins2 = 0;
        //D.deb("insertEmptyLines():"); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = (Difference) diffs.get(i);
            int n1 = action.getFile1Line1() + ins1;
            int n2 = action.getFile1Line2() + ins1;
            int n3 = action.getFile2Line1() + ins2;
            int n4 = action.getFile2Line2() + ins2;
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            //D.deb("ins1 = "+ins1+", ins2 = "+ins2); // NOI18N
            if (updateActionLines) {
                action.shiftFile1Lines(ins1);
                action.shiftFile2Lines(ins2);
            }
            switch (action.getType()) {
                case Difference.DELETE:
                    addEmptyLines2(n3, n2 - n1 + 1);
                    ins2 += n2 - n1 + 1;
                    break;
                case Difference.ADD:
                    addEmptyLines1(n1, n4 - n3 + 1);
                    ins1 += n4 - n3 + 1;
                    break;
                case Difference.CHANGE:
                    int r1 = n2 - n1;
                    int r2 = n4 - n3;
                    if (r1 < r2) {
                        addEmptyLines1(n2, r2 - r1);
                        ins1 += r2 - r1;
                    } else if (r1 > r2) {
                        addEmptyLines2(n4, r1 - r2);
                        ins2 += r1 - r2;
                    }
                    break;
            }
        }
    }
    
    private void setDiffHighlight(boolean set) {
        int n = diffs.size();
        //D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = (Difference) diffs.get(i);
            int n1 = action.getFile1Line1();
            int n2 = action.getFile1Line2();
            int n3 = action.getFile2Line1();
            int n4 = action.getFile2Line2();
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getType()) {
            case Difference.DELETE:
                if (set) highlightRegion1(n1, n2, colorMissing);
                else highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case Difference.ADD:
                if (set) highlightRegion2(n3, n4, colorAdded);
                else highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case Difference.CHANGE:
                if (set) {
                    highlightRegion1(n1, n2, colorChanged);
                    highlightRegion2(n3, n4, colorChanged);
                } else {
                    highlightRegion1(n1, n2, java.awt.Color.white);
                    highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        diffs = (List) obj;
        diffPanel = new DiffPanel();
        //this.diffSetSuccess = diff.setDiffComponent(this);
    }
    
    private Object readResolve() throws ObjectStreamException {
        if (this.diffSetSuccess) return this;
        else return null;
    }
    
    /**
     * Disable serialization.
     */
    protected Object writeReplace () throws java.io.ObjectStreamException {
        exitForm(null);
        return null;
    }
    
       /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        /*
        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, lineNumbersVisible);
        } catch (Throwable exc) {
            // editor module not found
        }
         */
        //System.out.println("exitForm() called.");
        //diff.closing();
        //close();
        //dispose ();
        /*
        for(Iterator it = closeListeners.iterator(); it.hasNext(); ) {
            ((TopComponentCloseListener) it.next()).closing();
        }
         */
 
    }

    private Boolean lineNumbersVisible = Boolean.FALSE;
    

}
