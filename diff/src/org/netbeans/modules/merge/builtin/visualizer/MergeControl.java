/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

/**
 * This class controls the merge process.
 *
 * @author  Martin Entlicher
 */
public class MergeControl extends Object implements ActionListener, VetoableChangeListener {
    
    private Color colorUnresolvedConflict;
    private Color colorResolvedConflict;
    private Color colorOtherConflict;
    
    private MergePanel panel;
    private Difference[] diffs;
    /** The shift of differences */
    private int[][] diffShifts;
    /** The current diff */
    private int currentDiffLine = 0;
    private int[] resultDiffLocations;
    private Set resolvedConflicts = new HashSet();
    private StreamSource resultSource;
    
    /** Creates a new instance of MergeControl */
    public MergeControl(MergePanel panel) {
        this.panel = panel;
    }
    
    public void initialize(Difference[] diffs, StreamSource source1,
                           StreamSource source2, StreamSource result,
                           Color colorUnresolvedConflict, Color colorResolvedConflict,
                           Color colorOtherConflict) {
        this.diffs = diffs;
        this.diffShifts = new int[diffs.length][2];
        this.resultDiffLocations = new int[diffs.length];
        panel.setMimeType1(source1.getMIMEType());
        panel.setMimeType2(source2.getMIMEType());
        panel.setMimeType3(result.getMIMEType());
        panel.setSource1Title(source1.getTitle());
        panel.setSource2Title(source2.getTitle());
        panel.setResultSourceTitle(result.getTitle());
        panel.setName(source1.getName());
        try {
            panel.setSource1(source1.createReader());
            panel.setSource2(source2.createReader());
            panel.setResultSource(new java.io.StringReader(""));
        } catch (IOException ioex) {
            org.openide.TopManager.getDefault().notifyException(ioex);
        }
        this.colorUnresolvedConflict = colorUnresolvedConflict;
        this.colorResolvedConflict = colorResolvedConflict;
        this.colorOtherConflict = colorOtherConflict;
        insertEmptyLines(true);
        setDiffHighlight(true);
        copyToResult();
        panel.setNumConflicts(diffs.length);
        panel.addControlActionListener(this);
        showCurrentLine();
        this.resultSource = result;
    }
    
    private void insertEmptyLines(boolean updateActionLines) {
        int n = diffs.length;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            if (updateActionLines && i < n - 1) {
                diffShifts[i + 1][0] = diffShifts[i][0];
                diffShifts[i + 1][1] = diffShifts[i][1];
            }
            switch (action.getType()) {
                case Difference.DELETE:
                    panel.addEmptyLines2(n3, n2 - n1 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][1] += n2 - n1 + 1;
                    }
                    break;
                case Difference.ADD:
                    panel.addEmptyLines1(n1, n4 - n3 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][0] += n4 - n3 + 1;
                    }
                    break;
                case Difference.CHANGE:
                    int r1 = n2 - n1;
                    int r2 = n4 - n3;
                    if (r1 < r2) {
                        panel.addEmptyLines1(n2, r2 - r1);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][0] += r2 - r1;
                        }
                    } else if (r1 > r2) {
                        panel.addEmptyLines2(n4, r1 - r2);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][1] += r1 - r2;
                        }
                    }
                    break;
            }
        }
    }
    
    private void setDiffHighlight(boolean set) {
        int n = diffs.length;
        //D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getType()) {
            case Difference.DELETE:
                if (set) panel.highlightRegion1(n1, n2, colorUnresolvedConflict);
                else panel.highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case Difference.ADD:
                if (set) panel.highlightRegion2(n3, n4, colorUnresolvedConflict);
                else panel.highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case Difference.CHANGE:
                if (set) {
                    panel.highlightRegion1(n1, n2, colorUnresolvedConflict);
                    panel.highlightRegion2(n3, n4, colorUnresolvedConflict);
                } else {
                    panel.highlightRegion1(n1, n2, java.awt.Color.white);
                    panel.highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }
    
    private void copyToResult() {
        int n = diffs.length;
        int line1 = 1;
        int line3 = 1;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            int endcopy = (action.getType() != Difference.ADD) ? (n1 - 1) : n1;
            //System.out.println("diff = "+n1+", "+n2+", "+n3+", "+n4+", endcopy = "+endcopy+((endcopy >= line1) ? "; copy("+line1+", "+endcopy+", "+line3+")" : ""));
            if (endcopy >= line1) {
                panel.copySource1ToResult(line1, endcopy, line3);
                line3 += endcopy + 1 - line1;
            }
            int length = Math.max(n2 - n1, n4 - n3);
            //System.out.println("  length = "+length+", addEmptyLines3("+line3+", "+(length + 1)+")");
            panel.addEmptyLines3(line3, length + 1);
            panel.highlightRegion3(line3, line3 + length, colorUnresolvedConflict);
            resultDiffLocations[i] = line3;
            line3 += length + 1;
            line1 = Math.max(n2, n4) + 1;
        }
        //System.out.println("copy("+line1+", -1, "+line3+")");
        panel.copySource1ToResult(line1, -1, line3);
    }

    private void showCurrentLine() {
        Difference diff = diffs[currentDiffLine];
        int line = diff.getFirstStart() + diffShifts[currentDiffLine][0];
        if (diff.getType() == Difference.ADD) line++;
        int lf1 = diff.getFirstEnd() - diff.getFirstStart() + 1;
        int lf2 = diff.getSecondEnd() - diff.getSecondStart() + 1;
        int length = Math.max(lf1, lf2);
        panel.setCurrentLine(line, length, currentDiffLine,
                             resultDiffLocations[currentDiffLine]);
    }
    
    /**
     * Resolve the merge conflict with left or right part.
     * This will reduce the number of conflicts by one.
     * @param right If true, use the right part, left otherwise
     * @param conflNum The number of conflict.
     */
    private void doResolveConflict(boolean right, int conflNum) {
        Difference diff = diffs[conflNum];
        int[] shifts = diffShifts[conflNum];
        int line1, line2, line3, line4;
        if (diff.getType() == Difference.ADD) {
            line1 = diff.getFirstStart() + shifts[0] + 1;
            line2 = line1 - 1;
        } else {
            line1 = diff.getFirstStart() + shifts[0];
            line2 = diff.getFirstEnd() + shifts[0];
        }
        if (diff.getType() == Difference.DELETE) {
            line3 = diff.getSecondStart() + shifts[1] + 1;
            line4 = line3 - 1;
        } else {
            line3 = diff.getSecondStart() + shifts[1];
            line4 = diff.getSecondEnd() + shifts[1];
        }
        //System.out.println("  diff lines = "+line1+", "+line2+", "+line3+", "+line4);
        int rlength; // The length of the area before the conflict is resolved
        if (resolvedConflicts.contains(diff)) {
            rlength = (right) ? (line2 - line1) : (line4 - line3);
        } else {
            rlength = Math.max(line2 - line1, line4 - line3);
        }
        int shift;
        if (right) {
            panel.replaceSource2InResult(line3, Math.max(line4, 0), // Correction for possibly negative value
                                         resultDiffLocations[conflNum],
                                         resultDiffLocations[conflNum] + rlength);
            shift = rlength - (line4 - line3);
            panel.highlightRegion1(line1, Math.max(line2, 0), colorOtherConflict);
            panel.highlightRegion2(line3, Math.max(line4, 0), colorResolvedConflict);
        } else {
            panel.replaceSource1InResult(line1, Math.max(line2, 0), // Correction for possibly negative value
                                         resultDiffLocations[conflNum],
                                         resultDiffLocations[conflNum] + rlength);
            shift = rlength - (line2 - line1);
            panel.highlightRegion1(line1, Math.max(line2, 0), colorResolvedConflict);
            panel.highlightRegion2(line3, Math.max(line4, 0), colorOtherConflict);
        }
        if (right && (line4 >= line3) || !right && (line2 >= line1)) {
            panel.highlightRegion3(resultDiffLocations[conflNum],
                                   resultDiffLocations[conflNum] + rlength - shift,
                                   colorResolvedConflict);
        } else {
            panel.unhighlightRegion3(resultDiffLocations[conflNum],
                                     resultDiffLocations[conflNum]);
        }
        for (int i = conflNum + 1; i < diffs.length; i++) {
            resultDiffLocations[i] -= shift;
        }
        resolvedConflicts.add(diff);
        panel.setNeedsSaveState(true);
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        final String actionCommand = actionEvent.getActionCommand();
        org.openide.util.RequestProcessor.postRequest(new Runnable() {
            public void run() {
                if (MergePanel.ACTION_FIRST_CONFLICT.equals(actionCommand)) {
                    currentDiffLine = 0;
                    showCurrentLine();
                } else if (MergePanel.ACTION_LAST_CONFLICT.equals(actionCommand)) {
                    currentDiffLine = diffs.length - 1;
                    showCurrentLine();
                } else if (MergePanel.ACTION_PREVIOUS_CONFLICT.equals(actionCommand)) {
                    currentDiffLine--;
                    if (currentDiffLine < 0) currentDiffLine = diffs.length - 1;
                    showCurrentLine();
                } else if (MergePanel.ACTION_NEXT_CONFLICT.equals(actionCommand)) {
                    currentDiffLine++;
                    if (currentDiffLine >= diffs.length) currentDiffLine = 0;
                    showCurrentLine();
                } else if (MergePanel.ACTION_ACCEPT_RIGHT.equals(actionCommand)) {
                    doResolveConflict(true, currentDiffLine);
                } else if (MergePanel.ACTION_ACCEPT_LEFT.equals(actionCommand)) {
                    doResolveConflict(false, currentDiffLine);
                }
            }
        });
    }
    
    public void vetoableChange(PropertyChangeEvent propertyChangeEvent) throws PropertyVetoException {
        if (MergeDialogComponent.PROP_PANEL_SAVE.equals(propertyChangeEvent.getPropertyName())) {
            MergePanel panel = (MergePanel) propertyChangeEvent.getNewValue();
            if (this.panel == panel) {
                ArrayList unresolvedConflicts = new ArrayList();//java.util.Arrays.asList(diffs));
                int diffLocationShift = 0;
                for (int i = 0; i < diffs.length; i++) {
                    if (!resolvedConflicts.contains(diffs[i])) {
                        int diffLocation = resultDiffLocations[i] - diffLocationShift;
                        Difference conflict = new Difference(diffs[i].getType(),
                                                             diffLocation,
                                                             diffLocation + diffs[i].getFirstEnd() - diffs[i].getFirstStart(),
                                                             diffLocation,
                                                             diffLocation + diffs[i].getSecondEnd() - diffs[i].getSecondStart(),
                                                             diffs[i].getFirstText(),
                                                             diffs[i].getSecondText());
                        unresolvedConflicts.add(conflict);
                        diffLocationShift += Math.max(diffs[i].getFirstEnd() - diffs[i].getFirstStart() + 1,
                                                      diffs[i].getSecondEnd() - diffs[i].getSecondStart() + 1);
                    }
                }
                try {
                    panel.writeResult(resultSource.createWriter((Difference[]) unresolvedConflicts.toArray(
                        new Difference[unresolvedConflicts.size()])));
                    panel.setNeedsSaveState(false);
                } catch (IOException ioex) {
                    throw new PropertyVetoException(NbBundle.getMessage(MergeControl.class,
                                                        "MergeControl.failedToSave",
                                                        ioex.getLocalizedMessage()),
                                                    propertyChangeEvent);
                }
            }
        }
        if (MergeDialogComponent.PROP_PANEL_CLOSING.equals(propertyChangeEvent.getPropertyName())) {
            MergePanel panel = (MergePanel) propertyChangeEvent.getNewValue();
            if (this.panel == panel) {
                resultSource.close();
            }
        }
        if (MergeDialogComponent.PROP_ALL_CLOSED.equals(propertyChangeEvent.getPropertyName()) ||
            MergeDialogComponent.PROP_ALL_CANCELLED.equals(propertyChangeEvent.getPropertyName())) {
                resultSource.close();
        }
    }
    
}
