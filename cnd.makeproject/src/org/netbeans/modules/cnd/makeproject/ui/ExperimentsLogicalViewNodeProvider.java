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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProvider;

public class ExperimentsLogicalViewNodeProvider implements LogicalViewNodeProvider {

    public AbstractNode getLogicalViewNode(Project project) {
        return new ExperimentsRootNode(project);
    }
}

class ExperimentsRootNode extends AbstractNode {

    public ExperimentsRootNode(Project project) {
        super(new ExperimentsRootNodeChildren(project));
        setName("Experiments"); // NOI18N
        setDisplayName("Experiments"); // NOI18N
    }

    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolder.gif"); // NOI18N
    }

    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolderOpen.gif"); // NOI18N
    }

    public boolean canRename() {
        return false;
    }
}

class ExperimentsRootNodeChildren extends Children.Keys implements ChangeListener {

    Project project;

    public ExperimentsRootNodeChildren(Project project) {
        this.project = project;
    }

    protected void addNotify() {
        super.addNotify();
        setKeys(getKeys());
    }

    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof ExperimentsGroupNode) {
            node = (Node) key;
        } else {
            // FIXUP: error
        }
        return new Node[]{node};
    }

    public void stateChanged(ChangeEvent e) {
        setKeys(getKeys());
    }

    private Collection getKeys() {
        Vector<ExperimentsGroupNode> v = new Vector<ExperimentsGroupNode>();
        v.add(new ExperimentsGroupNode(project, "Heap Tracing")); // NOI18N
        v.add(new ExperimentsGroupNode(project, "Data Race Detection")); // NOI18N
        v.add(new ExperimentsGroupNode(project, "Runtime Checking")); // NOI18N
        return v;
    }
}

class ExperimentsGroupNode extends AbstractNode {

    public ExperimentsGroupNode(Project project, String name) {
        super(new ExperimentsGroupNodeChildren(project));
        setName(name);
        setDisplayName(name);
    }

    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolder.gif"); // NOI18N
    }

    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolderOpen.gif"); // NOI18N
    }

    public boolean canRename() {
        return false;
    }

    public Action[] getActions(boolean context) {
        return new Action[]{
                    new ImportExperimentAction(),};
    }
}

class ExperimentsGroupNodeChildren extends Children.Keys implements ChangeListener {

    Project project;

    public ExperimentsGroupNodeChildren(Project project) {
        this.project = project;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(getKeys());
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof Experiment) {
            node = (Node) key;
        } else {
            ; // FIXUP: error
        }
        return new Node[]{node};
    }

    public void stateChanged(ChangeEvent e) {
        setKeys(getKeys());
    }

    private Collection<Experiment> getKeys() {
        // FIXUP: add per project...
        Vector<Experiment> v = new Vector<Experiment>();
        v.add(new Experiment("Experiment-01212007-1422")); // NOI18N
        v.add(new Experiment("Experiment-01212007-1427")); // NOI18N
        v.add(new Experiment("Experiment-01212007-1532")); // NOI18N
        return v;
    }
}

class Experiment extends AbstractNode {

    public Experiment(String name) {
        super(Children.LEAF);
        setName(name);
        setDisplayName(name);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolder.gif"); // NOI18N
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    new OpenExperimentAction(),
                    SystemAction.get(DeleteAction.class),
                    SystemAction.get(RenameAction.class),
                    null,
                    SystemAction.get(PropertiesAction.class),};
    }
}

class ImportExperimentAction extends DummyAction {

    public ImportExperimentAction() {
        super("Import Experiment..."); // NOI18N
    }
}

class OpenExperimentAction extends DummyAction {

    public OpenExperimentAction() {
        super("Open Experiment..."); // NOI18N
    }
}

class DummyAction extends NodeAction {

    private String name;

    public DummyAction(String name) {
        this.name = name;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public String getName() {
        return name;
    }

    public void performAction(Node[] activatedNodes) {
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
