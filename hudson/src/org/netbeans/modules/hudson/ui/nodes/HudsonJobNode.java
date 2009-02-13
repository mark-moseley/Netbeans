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

package org.netbeans.modules.hudson.ui.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.ShowBuildConsole;
import org.netbeans.modules.hudson.ui.actions.ShowChanges;
import org.netbeans.modules.hudson.ui.actions.ShowFailures;
import org.netbeans.modules.hudson.ui.actions.ShowJobDetailAction;
import org.netbeans.modules.hudson.ui.actions.StartJobAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Describes HudsonJob in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonJobNode extends AbstractNode {
    
    private static final String ICON_BASE_RED = "org/netbeans/modules/hudson/ui/resources/red.png";
    private static final String ICON_BASE_RED_RUN = "org/netbeans/modules/hudson/ui/resources/red_run.png";
    private static final String ICON_BASE_BLUE = "org/netbeans/modules/hudson/ui/resources/blue.png";
    private static final String ICON_BASE_BLUE_RUN = "org/netbeans/modules/hudson/ui/resources/blue_run.png";
    private static final String ICON_BASE_YELLOW = "org/netbeans/modules/hudson/ui/resources/yellow.png";
    private static final String ICON_BASE_YELLOW_RUN = "org/netbeans/modules/hudson/ui/resources/yellow_run.png";
    private static final String ICON_BASE_GREY = "org/netbeans/modules/hudson/ui/resources/grey.png";
    private static final String ICON_BASE_GREY_RUN = "org/netbeans/modules/hudson/ui/resources/grey_run.png";
    
    private String htmlDisplayName;
    private Color color;
    private HudsonJobImpl job;
    
    public HudsonJobNode(HudsonJobImpl job) {
        super(makeChildren(job), Lookups.singleton(job));
        setHudsonJob(job);
    }

    private static Children makeChildren(final HudsonJobImpl job) {
        return Children.create(new ChildFactory<Object>() {
            @Override
            protected boolean createKeys(List<Object> toPopulate) {
                // XXX would be nicer to avoid adding this in case there is no remote workspace...
                toPopulate.add(new Object());
                return true;
            }
            @Override
            protected Node createNodeForKey(Object key) {
                return new HudsonWorkspaceNode(job);
            }
        }, false);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(SystemAction.get(ShowJobDetailAction.class));
        actions.add(SystemAction.get(StartJobAction.class));
        int last = job.getLastBuild();
        if (last >= 0) {
            actions.add(new ShowChanges(job, last));
            actions.add(new ShowBuildConsole(job, last));
        }
        int lastCompleted = job.getLastCompletedBuild();
        if (lastCompleted >= 0 && lastCompleted != job.getLastStableBuild()) {
            actions.add(new ShowFailures(job, lastCompleted));
        }
        actions.add(null);
        actions.add(SystemAction.get(OpenUrlAction.class));
        actions.add(null);
        actions.add(SystemAction.get(PropertiesAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(ShowJobDetailAction.class);
    }
    
    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.DND_COPY);
    }
    
    @Override
    public PasteType getDropType(Transferable arg0, int arg1, int arg2) {
        return super.getDropType(arg0, arg1, arg2);
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(job.getSheetSet());
        
        return s;
    }
    
    private void refreshState() {
        // Store old html name
        String oldHtmlDisplayName = getHtmlDisplayName();
        
        // Set new node data
        htmlDisplayName = job.getDisplayName();
        color = job.getColor();
        setShortDescription(job.getUrl());
        
        // Decorate node
        switch(color) {
        case red:
            setIconBaseWithExtension(ICON_BASE_RED);
            htmlDisplayName = "<font color=\"#A40000\">"+job.getDisplayName()+"</font>";
            break;
        case red_anime:
            setIconBaseWithExtension(ICON_BASE_RED_RUN);
            htmlDisplayName = "<b><font color=\"#A40000\">"+job.getDisplayName()+"</font></b>";
            break;
        case blue:
            setIconBaseWithExtension(ICON_BASE_BLUE);
            break;
        case blue_anime:
            setIconBaseWithExtension(ICON_BASE_BLUE_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        case yellow:
            setIconBaseWithExtension(ICON_BASE_YELLOW);
            break;
        case yellow_anime:
            setIconBaseWithExtension(ICON_BASE_YELLOW_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        case grey:
            setIconBaseWithExtension(ICON_BASE_GREY);
            break;
        case grey_anime:
            setIconBaseWithExtension(ICON_BASE_GREY_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        }
        
        // Fire changes if any
        fireDisplayNameChange(oldHtmlDisplayName, getHtmlDisplayName());
    }
    
    public void setHudsonJob(HudsonJobImpl job) {
        this.job = job;
        
        // Refresh
        refreshState();
    }
    
    public HudsonJobImpl getJob() {
        return job;
    }
}