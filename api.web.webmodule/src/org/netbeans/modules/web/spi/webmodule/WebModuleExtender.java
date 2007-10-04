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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.spi.webmodule;

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * Provides support for extending a web module with a web framework, that is,
 * it allows to modify the web module to make use of the framework.
 *
 * @author Andrei Badea
 *
 * @since 1.9
 */
public abstract class WebModuleExtender {

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     *
     * @param  listener a listener.
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public abstract void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     *
     * @return a component or null if this extender does not provide a configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    public abstract JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     *
     * @return a help context; can be null.
     */
    public abstract HelpCtx getHelp();

    /**
     * Called when the component returned by {@link #getComponent} needs to be filled
     * with external data.
     */
    public abstract void update();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     *
     * @return true if the configuration is valid, false otherwise.
     */
    public abstract boolean isValid();

    /**
     * Called to extend the given web module with the web framework
     * corresponding to this extender.
     *
     * @param  webModule the web module to be extender; never null.
     * @return the set of newly created files in the web module.
     */
    public abstract Set<FileObject> extend(WebModule webModule);
}
