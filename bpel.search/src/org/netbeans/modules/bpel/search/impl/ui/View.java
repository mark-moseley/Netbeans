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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.search.impl.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.xam.ui.search.SearchManager;
import org.netbeans.modules.print.api.PrintManager;
import org.netbeans.modules.bpel.search.impl.util.Util;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
public final class View extends TopComponent {

  public View() {
    setIcon(icon(Util.class, "find").getImage()); // NOI18N
    setLayout(new GridBagLayout());
    setFocusable(true);
  }

  void show(Tree tree) {
    myTree = tree;
    createPanel();
    open();
    requestActive();
  }

  private void createPanel() {
    removeAll();
    JScrollPane scrollPane = new JScrollPane(myTree);
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;

    // buttons
    add(createButtonPanel(), c);

    // tree
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    add(SearchManager.Access.getDefault().
      createNavigation(myTree, scrollPane, scrollPane), c);

    revalidate();
    repaint();
  }

  @Override
  public void requestActive()
  {
    super.requestActive();

    if (myTree != null) {
      myTree.requestFocus();
    }
  }

  private JToolBar createButtonPanel() {
    JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
    toolBar.setFloatable(false);
    JButton button;

    // collapse/expand
    button = createButton(
      new ButtonAction(
        icon(Util.class, "expose"), // NOI18N
        i18n(View.class, "TLT_Expose")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.expose(myTree.getSelectedNode());
        }
      }
    );
    setImageSize(button);
    toolBar.add(button);

    // previous occurence
    button = createButton(
      new ButtonAction(
        icon(Util.class, "previous"), // NOI18N
        i18n(View.class, "TLT_Previous_Occurence")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.previousOccurence(myTree.getSelectedNode());
        }
      }
    );
    setImageSize(button);
    toolBar.add(button);

    // next occurence
    button = createButton(
      new ButtonAction(
        icon(Util.class, "next"), // NOI18N
        i18n(View.class, "TLT_Next_Occurence")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.nextOccurence(myTree.getSelectedNode());
        }
      }
    );
    setImageSize(button);
    toolBar.add(button);

    // export
    button = createButton(
      new ButtonAction(
        icon(Util.class, "export"), // NOI18N
        i18n(View.class, "TLT_Export")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.export(myTree.getSelectedNode());
        }
      }
    );
    setImageSize(button);
    toolBar.add(button);

    // vlv: print
    button = createButton(PrintManager.getDefault().getPrintPreviewAction());
    setImageSize(button);
    toolBar.add(button);

    return toolBar;
  }

  @Override
  public HelpCtx getHelpCtx()
  {
    return HelpCtx.DEFAULT_HELP;
  }

  @Override
  public int getPersistenceType()
  {
    return PERSISTENCE_ALWAYS;
  }
      
  @Override
  public String getName()
  {
    return NAME;
  }
  
  @Override
  public String getDisplayName()
  {
    return i18n(View.class, "LBL_Search_Results_Name"); // NOI18N
  }

  @Override
  public String getToolTipText()
  {
    return i18n(View.class, "LBL_Search_Results_Tooltip"); // NOI18N
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    myTree = null;
  }

  @Override
  protected String preferredID()
  {
    return NAME;
  }

  private Tree myTree;
  public static final String NAME = "search"; // NOI18N
}
