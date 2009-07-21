/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.stack.FunctionCall;
import org.netbeans.modules.dlight.api.stack.OpenInEditor;
import org.netbeans.modules.dlight.api.stack.StackTrace.Stack;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState.MSAState;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.visualizers.CallStackTopComponent;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadStateColumnImpl.StateResources;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class ThreadStackVisualizer extends JPanel implements Visualizer<VisualizerConfiguration> {
    private final StackTraceDescriptor descriptor;

    ThreadStackVisualizer(StackTraceDescriptor descriptor) {
        this.descriptor = descriptor;
        init();
    }

    @Override
    public String getName(){
        if (descriptor.getStacks().size() > 0) {
            return descriptor.getStacks().get(0).getThreadInfo().getThreadName();
        }
        return NbBundle.getMessage(getDefaultContainer().getClass(), "CallStackDetailes"); //NOI18N
    }

    private void init() {
        setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane();
        add(pane, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.setBackground(new JTextPane().getBackground());
        panel.setLayout(new GridBagLayout());
        pane.setViewportView(panel);
        int y = 0;
        long time = descriptor.getTime();
        String timeString = TimeLineUtils.getMillisValue(time);
        if (descriptor.getStacks().size() > 0) {
            String message = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerStackAt", timeString); //NOI18N
            panel.add(new JLabel(message),
                    new GridBagConstraints(0, y,
                                1, 1,
                                1., 0.,
                                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                new Insets(10, 10, 0, 0), 0, 0));
            for(Stack stack : descriptor.getStacks()){
                y = addThread(panel, y, stack);
            }
        } else {
            String message = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerNoStackAt", timeString); //NOI18N
            panel.add(new JLabel(message), // NOI18N
                new GridBagConstraints(0, y,
                            1, 1,
                            1., 0.,
                            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                            new Insets(10, 10, 0, 0), 0, 0));
        }
    }

    private int addThread(JPanel panel, int y, Stack stack){
        MSAState msa = stack.getState();
        StateResources res = ThreadStateColumnImpl.getThreadStateResources(msa);
        if (res != null) {
            y++;
            panel.add(new JLabel(res.name+" "+stack.getThreadInfo().getThreadName(), new ThreadStateIcon(msa, 10, 10), JLabel.LEFT), // NOI18N
                    new GridBagConstraints(0, y,
                                1, 1,
                                0., 0.,
                                GridBagConstraints.WEST, GridBagConstraints.NONE,
                                new Insets(10, 20, 0, 0), 0, 0));
            return addStack(panel, y, stack);
        }
        return y;
    }

    private int addStack(JPanel panel, int y, Stack stack) {
        for(FunctionCall call : stack.getStack()) {
            y++;
            JButton button = new JButton(call.getDisplayedName());
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setForeground(Color.blue);
            panel.add(button,
                new GridBagConstraints(0, y,
                            1, 1,
                            0., 0.,
                            GridBagConstraints.WEST, GridBagConstraints.NONE,
                            new Insets(0, 30, 0, 0), 0, 0));
            button.setAction(new OpenAction(call));
        }
        return y;
    }

    public VisualizerConfiguration getVisualizerConfiguration() {
        return new VisualizerConfiguration(){
            public DataModelScheme getSupportedDataScheme() {
                return null;
            }
            public DataTableMetadata getMetadata() {
                return null;
            }
            public String getID() {
                return "CallStack";// NOI18N
            }
        };
    }

    public JComponent getComponent() {
        return this;
    }

    public VisualizerContainer getDefaultContainer() {
        return CallStackTopComponent.findInstance();
    }

    public void refresh() {
    }

    private static final class OpenAction extends AbstractAction {
        private final FunctionCall call;
        private OpenAction(FunctionCall call) {
            this.call = call;
        }

        public void actionPerformed(ActionEvent e) {
            OpenInEditor.open(call);
        }
    }
}
