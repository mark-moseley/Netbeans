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

package org.netbeans.modules.cnd.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.TargetEditor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 * Implements Make target Action
 */
public class MakeTargetAction extends MakeBaseAction implements Presenter.Popup {

    /* target mnemonics for the first 10 targets */
    private static String mnemonics = "1234567890"; // NOI18N

    public String getName () {
	return getString("BTN_Target");	// NOI18N
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeTargetAction.class); // FIXUP ???
    }

    public JMenuItem getPopupPresenter() {
	Node[] activeNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        return new TargetPopupMenu(this, enable(activeNodes), activeNodes);
    }

    private class TargetPopupMenu extends JMenu {
	private boolean initialized = false;
	private SystemAction action = null;
	private Node[] activeNodes;

        public TargetPopupMenu(SystemAction action, boolean en, Node[] activeNodes) {
            super();
	    this.action = action;
	    this.activeNodes = activeNodes;
	    setEnabled(en);
	    setText(action.getName());
        }
        
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
	    if (!initialized) {
		if (activeNodes == null || activeNodes.length != 1) {
		    return null; 
		}

		Node activedNode = activeNodes[0];
		MakeExecSupport mes = (MakeExecSupport) activedNode.getCookie(MakeExecSupport.class);
		String[] targets = mes.getMakeTargetsArray();

		//popup.add(new PopupItemDefaultTarget(activedNode, getString("DEFAULT_TARGET"))); // NOI18N
		//if (targets.length > 0)
		    //popup.add(new JSeparator());
		for (int i = 0; i < targets.length; i++) {
		    popup.add(new PopupItemTarget(activedNode, targets[i], -1));
		}
		if (targets.length > 0)
		    popup.add(new JSeparator());
		popup.add(new PopupItemAddTarget(activedNode)); // NOI18N
		initialized = true;
	    }
            return popup;
        }
        
    }

    /**
     * Compose new name with a mnemonic: <targetname>  (<mnemonic>)
     * Compose new name with a mnemonic: <mnemonic> <targetname>
     */
    private String nameWithMnemonic(String name, int mne) {
	if (mne >= 0 && mne < mnemonics.length()) {
	    return "" + mnemonics.charAt(mne)  + "  " + name; // NOI18N
	    //return name + "  (" + mnemonics.charAt(mne) + ")"; // NOI18N
	}
	else {
	    return name; // no mnemonic
	}
    }

    protected class PopupItemTarget extends JMenuItem implements ActionListener {
	Node node = null;
	String target = null;


	public PopupItemTarget(Node activeNode, String name, int mne) {
	    //super(nameWithMnemonic(name, mne), new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/resources/blank.gif", true)));
	    super(nameWithMnemonic(name, mne));
	    node = activeNode;
	    target = name;
	    addActionListener(this);
	    if (mne >= 0 && mne < mnemonics.length()) {
		setMnemonic(mnemonics.charAt(mne));
	    }
	}
        
       /** Invoked when an action occurs. */
       public void actionPerformed(ActionEvent e) {
	    performAction(node, target);
	}
    }

    protected class PopupItemAddTarget extends JMenuItem implements ActionListener {
	Node node = null;
	public PopupItemAddTarget(Node activeNode) {
	    //super(getString("ADD_NEW_TARGET"), new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/resources/AddMakeTargetAction.gif", true))); // NOI18N
	    super(getString("ADD_NEW_TARGET")); // NOI18N
	    node = activeNode;
	    addActionListener(this);
	    setMnemonic(getString("ADD_NEW_TARGET_MNEMONIC").charAt(0));
	}
        
       /** Invoked when an action occurs. */
       public void actionPerformed(ActionEvent e) {
	    MakeExecSupport mes = (MakeExecSupport) node.getCookie(MakeExecSupport.class);
	    TargetEditor targetEditor = new TargetEditor(mes.getMakeTargetsArray(), null, null);
	    int ret = targetEditor.showOpenDialog((JFrame)WindowManager.getDefault().getMainWindow());
	    if (ret == TargetEditor.OK_OPTION) {
		mes.setMakeTargets(targetEditor.getTargets());
	    }
	}
    }
}
