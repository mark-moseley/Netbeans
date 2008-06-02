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
package org.netbeans.modules.java.hints.analyzer;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.java.hints.infrastructure.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

public final class AnalyzeFolder extends AbstractAction implements ContextAwareAction {

    private final boolean def;
    private final Lookup context;

    public AnalyzeFolder() {
        context = Utilities.actionsGlobalContext();
        def = true;
        putValue(NAME, NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder"));
    }

    @Override
    public boolean isEnabled() {
        if (!def) {
            return super.isEnabled();
        }
        
        return Analyzer.normalizeLookup(context) != null;
    }

    public AnalyzeFolder(Lookup context) {
        this.context = context;
        def = false;
        setEnabled(Analyzer.normalizeLookup(context) != null);
        putValue(NAME, NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder"));
    }
    
    private static final Set<String> SUPPORTED_IDS = new HashSet<String>(Arrays.asList("create-javadoc", "error-in-javadoc"));
    
    public void actionPerformed(ActionEvent e) {
        Map<String, Preferences> preferencesOverlay = new HashMap<String, Preferences>();
        for (List<TreeRule> rules : RulesManager.getInstance().getHints().values()) {
            for (TreeRule r : rules) {
                String id = r.getId();
                
                if (r instanceof AbstractHint && !preferencesOverlay.containsKey(id)) {
                    OverridePreferences prefs = new OverridePreferences(((AbstractHint) r).getPreferences(null));
                    
                    preferencesOverlay.put(r.getId(),prefs);
                    HintsSettings.setEnabled(prefs, SUPPORTED_IDS.contains(id));
                    HintsSettings.setSeverity(prefs, HintSeverity.WARNING);
                }
            }
        }
        
        Analyzer.process(Analyzer.normalizeLookup(context), preferencesOverlay);
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new AnalyzeFolder(actionContext);
    }
    
    public final static class ToolsAction extends SystemAction implements ContextAwareAction {

        private Action delegate;
        
        public ToolsAction() {
            delegate = new AnalyzeFolder();
        }

        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new AnalyzeFolder(actionContext);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
        
    }

}
