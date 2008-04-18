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
package org.netbeans.test.clearcase.operators;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/** Class implementing all necessary methods for handling "Versioning" view.
 * <br>
 * Usage:<br>
 * <pre>
 *      VersioningOperator vo = VersioningOperator.invoke();
 *      vo.refresh();
 *      vo.diff();
 *      vo.update();
 *      vo.performPopup("MyFile", "Exclude from Commit");
 *      CommitOperator co = vo.commit();
 *      co.setCommitMessage("Commit message.");
 *      co.commit();
 * </pre>
 *
 * @see CommitOperator
 *
 */
public class VersioningOperator extends TopComponentOperator {
    
    /** "Versioning" */
    static final String VERSIONING_TITLE = "Versioning";
    static final String CLEARCASE_TITLE = "Clearcase";
    
    /** Waits for Versioning TopComponent within whole IDE. */
    public VersioningOperator() {
        super(waitTopComponent(null, CLEARCASE_TITLE, 0, new VersioningSubchooser()));
    }
    
    /** Invokes Window|Versioning main menu item and returns new instance of
     * VersioningOperator.
     * @return new instance of VersioningOperator */
    public static VersioningOperator invoke() {
        String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
        String versioningItem = "Clearcase";
        new Action(windowItem + "|" + VERSIONING_TITLE + "|" + versioningItem, null).perform();
        return new VersioningOperator();
    }
    private JToggleButtonOperator _tbRemote;
    private JButtonOperator _btRefresh;
    private JButtonOperator _btDiff;
    private JButtonOperator _btUpdate;
    private JButtonOperator _btCommit;
    private JTableOperator _tabFiles;
    
    /** Tries to find Refresh Status JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRefresh() {
        if (_btRefresh==null) {
            _btRefresh = new JButtonOperator(this, new TooltipChooser(
                    "Refresh Status",
                    this.getComparator()));
        }
        return _btRefresh;
    }
    
    /** Tries to find Diff All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btDiff() {
        if (_btDiff==null) {
            _btDiff = new JButtonOperator(this, new TooltipChooser(
                    "Diff All",
                    this.getComparator()));
        }
        return _btDiff;
    }
    
    /** Tries to find Update All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btUpdate() {
        if (_btUpdate==null) {
            _btUpdate = new JButtonOperator(this, new TooltipChooser(
                    "Update All",
                    this.getComparator()));
        }
        return _btUpdate;
    }
    
    /** Tries to find Commit All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, new TooltipChooser(
                    "Commit All",
                    this.getComparator()));
        }
        return _btCommit;
    }
    
    /** Tries to find files JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFiles() {
        if (_tabFiles==null) {
            _tabFiles = new JTableOperator(this);
        }
        return _tabFiles;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on Refresh Status JButton
     */
    public void refresh() {
        btRefresh().push();
    }
    
    /** clicks on Diff All JButton
     */
    public void diff() {
        btDiff().push();
    }
    
    /** clicks on Update All JButton
     */
    public void update() {
        btUpdate().push();
    }
    
    /** clicks on Commit JButton and returns CommitOperator.
     * @return CommitOperator instance
     */
    public CheckinOperator commit() {
        btCommit().pushNoBlock();
        return new CheckinOperator();
    }
    
    /** Performs popup menu on specified row.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopup(int row, String popupPath) {
        tabFiles().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(tabFiles().callPopupOnCell(row, 0));
        popup.pushMenu(popupPath);
    }
    
    /** Performs popup menu on specified file.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopup(String filename, String popupPath) {
        performPopup(tabFiles().findCellRow(filename), popupPath);
    }

    /** Performs popup menu on specified row and no block further execution.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(int row, String popupPath) {
        tabFiles().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(tabFiles().callPopupOnCell(row, 0));
        popup.pushMenuNoBlock(popupPath);
    }
    
    /** Performs popup menu on specified file and no block further execution.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(String filename, String popupPath) {
        performPopupNoBlock(tabFiles().findCellRow(filename), popupPath);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of VersioningOperator by accessing all its components.
     */
    public void verify() {
        btRefresh();
        btDiff();
        btUpdate();
        btCommit();
        tabFiles();
    }
    
    /** SubChooser to determine TopComponent is instance of
     * org.netbeans.modules.clearcase.ui.status.ClearcaseTopComponent
     * Used in constructor.
     */
    private static final class VersioningSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ClearcaseTopComponent");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.clearcase.ui.status.ClearcaseTopComponent";
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a button.
     */
    private static class TooltipChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public TooltipChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Button with tooltip \""+buttonTooltip+"\".";
        }
    }
}

