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
package org.netbeans.modules.refactoring.spi.impl;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;

/**
 * This listener controls click and double click on the CheckNodes. In addition
 * to it provides support for keyboard node checking/unchecking and opening
 * document.
 *
 * todo (#pf): Improve behaviour and comments.
 *
 * @author  Pavel Flaska
 */
class CheckNodeListener implements MouseListener, KeyListener {
    private final boolean isQuery;
    
    public CheckNodeListener(boolean isQuery) {
        this.isQuery = isQuery;
    }

    public void mouseClicked(MouseEvent e) {
        // todo (#pf): we need to solve problem between click and double
        // click - click should be possible only on the check box area
        // and double click should be bordered by title text.
        // we need a test how to detect where the mouse pointer is
        JTree tree = (JTree) e.getSource();
        Point p = e.getPoint();
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);

        // if path exists and mouse is clicked exactly once
        if (path != null) {
            CheckNode node = (CheckNode) path.getLastPathComponent();
            if (isQuery) {
                if (e.getClickCount() == 2) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            findInSource(node);
                        }
                    } else {
                        if (tree.isCollapsed(row))
                            tree.expandRow(row);
                        else
                            tree.collapseRow(row);
                    }
                } else if (e.getClickCount() == 1) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            openDiff(node);
                        }
                    }
                }
            } else {
                Rectangle chRect = CheckRenderer.getCheckBoxRectangle();
                Rectangle rowRect = tree.getPathBounds(path);
                chRect.setLocation(chRect.x + rowRect.x, chRect.y + rowRect.y);
                if (e.getClickCount() == 1 && chRect.contains(p) && !node.isDisabled()) {
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
                        if (isSelected)
                            tree.expandPath(path);
                        else
                            tree.collapsePath(path);
                    } 
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            openDiff(node);
                        }
                    }
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        tree.revalidate();
                        tree.repaint();
                    }
                }
                // double click, open the document
                else if (e.getClickCount() == 2 && chRect.contains(p) == false) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            findInSource(node);
                        }
                    }
                    else {
                        if (tree.isCollapsed(row))
                            tree.expandRow(row);
                        else
                            tree.collapseRow(row);
                    }
                } else if (e.getClickCount() == 1 && chRect.contains(p) == false) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            openDiff(node);
                        }
                    }
                }
            }
        }
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
    public void keyReleased(KeyEvent e) {
        // Enter key was pressed, find the reference in document
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                CheckNode node = (CheckNode) path.getLastPathComponent();
                findInSource(node);
            }
        }
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                CheckNode node = (CheckNode) path.getLastPathComponent();
                node.setSelected(!node.isSelected());
                e.consume();
            }
        }
    }
    
    static void findInSource(CheckNode node) {
        Object o = node.getUserObject();
        if (o instanceof TreeElement) {
            o = ((TreeElement)o).getUserObject();
            if (o instanceof RefactoringElement) {
                APIAccessor.DEFAULT.getRefactoringElementImplementation((RefactoringElement) o).openInEditor();
            }
        }
    }
    
    static void openDiff(CheckNode node) {
        Object o = node.getUserObject();
        if (o instanceof TreeElement) {
            o = ((TreeElement)o).getUserObject();
            if (o instanceof RefactoringElement) {
                APIAccessor.DEFAULT.getRefactoringElementImplementation((RefactoringElement) o).showPreview();
            }
        }
    }
    

} // end CheckNodeListener
