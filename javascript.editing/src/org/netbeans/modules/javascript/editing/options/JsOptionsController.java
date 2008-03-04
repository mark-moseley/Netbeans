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

package org.netbeans.modules.javascript.editing.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import javax.swing.JComponent;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class JsOptionsController extends OptionsPanelController {

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    /** <i>GuardedBy("this")</i> */
    private BrowserPanel panel;

    private boolean changed;

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getComponent();
    }

    @Override
    public void update() {
        getComponent().load();
    }

    @Override
    public void applyChanges() {
        getComponent().store();
        changed = false;
    }

    @Override
    public void cancel() {
        // nothing to do
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public boolean isValid() {
        return true; // always valid
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }

    void changed() {
        if (!changed) {
            changed = true;
            propertySupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertySupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private synchronized BrowserPanel getComponent() {
        if (panel == null) {
            panel = new BrowserPanel(this);
        }
        return panel;
    }

    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of ReaderManager.class
            // that will assign value to the DEFAULT field above
            Class c = SupportedBrowsers.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }

        public abstract void setSupported(SupportedBrowsers supported, EnumSet<BrowserVersion> versions);

        public abstract void setLanguageVersion(SupportedBrowsers supported, int version);
    }
}
