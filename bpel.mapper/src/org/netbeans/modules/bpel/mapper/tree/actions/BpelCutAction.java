/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.CutAction;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author AlexanderPermyakov
 */
public class BpelCutAction extends NodeAction implements MapperSelectionListener {

    private Mapper mapper;

    public BpelCutAction() {
        super();
    }

    public void initialize(Mapper mapper) {
        if (this.mapper != mapper) {
            if (this.mapper != null) {
                this.mapper.getSelectionModel().removeSelectionListener(this);
            }
            this.mapper = mapper;
            mapper.getSelectionModel().addSelectionListener(this);
            mapperSelectionChanged(new MapperSelectionEvent(mapper));
        }
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (mapper == null) { return; }
        mapper.getActionMap().get(DefaultEditorKit.cutAction).
                actionPerformed(new ActionEvent(mapper, 0, "Bpel-Cut-Action"));
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (mapper == null) { return false; }

        if (mapper.getSelectionModel().getSelectedVerteces().size() > 0) {
            return true;
        }

        return false;
    }

    public String getName() {
        return NbBundle.getMessage(CutAction.class, "Cut");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CutAction.class);
    }

    public void mapperSelectionChanged(MapperSelectionEvent event) {
        if (mapper == null) { return; }

        if (!mapper.getSelectionModel().getSelectedVerteces().isEmpty()) {
            setEnabled(true);
            return;
        }
        if (mapper.getSelectionModel().getSelectedVertexItem() != null) {
            setEnabled(true);
            return;
        }
        setEnabled(false);
    }
}

