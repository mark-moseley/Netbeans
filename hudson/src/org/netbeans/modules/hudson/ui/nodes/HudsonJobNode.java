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

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.StartJobAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Describes HudsonJob in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonJobNode extends AbstractNode {
    
    private String htmlDisplayName;
    private HudsonJob job;
    
    public HudsonJobNode(HudsonJob job) {
        super(makeChildren(job), Lookups.singleton(job));
        setName(job.getName());
        setHudsonJob(job);
    }

    private static Children makeChildren(final HudsonJob job) {
        return Children.create(new ChildFactory<Object>() {
            final Object WORKSPACE = new Object();
            protected boolean createKeys(List<Object> toPopulate) {
                // XXX would be nicer to avoid adding this in case there is no remote workspace...
                toPopulate.add(WORKSPACE);
                toPopulate.addAll(job.getBuilds());
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                if (key == WORKSPACE) {
                    return new HudsonWorkspaceNode(job);
                } else {
                    return new HudsonJobBuildNode((HudsonJobBuild) key);
                }
            }
        }, true);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(SystemAction.get(StartJobAction.class));
        actions.add(null);
        actions.add(SystemAction.get(OpenUrlAction.class));
        actions.add(SystemAction.get(PropertiesAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(((HudsonJobImpl) job).getSheetSet()); // XXX is cast necessary?
        
        return s;
    }
    
    private void setHudsonJob(HudsonJob job) {
        this.job = job;
        Color color = job.getColor();
        setShortDescription(job.getUrl());

        // XXX #159836: tooltips
        setIconBaseWithExtension(color.iconBase());

        String oldHtmlDisplayName = getHtmlDisplayName();
        try {
            htmlDisplayName = color.colorizeDisplayName(XMLUtil.toElementContent(job.getDisplayName()));
        } catch (CharConversionException ex) {
            assert false : ex;
            return;
        }
        if (!job.isSalient()) {
            // XXX visually mark this somehow?
        }
        switch (color) {
        case red_anime:
        case yellow_anime:
        case blue_anime:
        case grey_anime:
        case aborted_anime:
            htmlDisplayName += " <font color='!controlShadow'>(running)</font>"; // XXX I18N
        }
        if (job.isInQueue()) {
            htmlDisplayName += " <font color='!controlShadow'>(in queue)</font>"; // XXX I18N
        }
        fireDisplayNameChange(oldHtmlDisplayName, htmlDisplayName);
    }

}
