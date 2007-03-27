/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 * Property provider that delegates to another source.
 * Useful, for example, when conditionally loading from one or another properties file.
 * @since org.netbeans.modules.project.ant/1 1.14
 */
public abstract class FilterPropertyProvider implements PropertyProvider {

    private PropertyProvider delegate;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final ChangeListener strongListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            //System.err.println("DPP: change from current provider " + delegate);
            cs.fireChange();
        }
    };
    private ChangeListener weakListener = null; // #50572: must be weak

    /**
     * Initialize the proxy.
     * @param delegate the initial delegate to use
     */
    protected FilterPropertyProvider(PropertyProvider delegate) {
        assert delegate != null;
        setDelegate(delegate);
    }

    /**
     * Change the current delegate (firing changes as well).
     * @param delegate the initial delegate to use
     */
    protected final void setDelegate(PropertyProvider delegate) {
        if (delegate == this.delegate) {
            return;
        }
        if (this.delegate != null) {
            assert weakListener != null;
            this.delegate.removeChangeListener(weakListener);
        }
        this.delegate = delegate;
        weakListener = WeakListeners.change(strongListener, delegate);
        delegate.addChangeListener(weakListener);
        cs.fireChange();
    }

    public final Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    public final synchronized void addChangeListener(ChangeListener listener) {
        // XXX could listen to delegate only when this has listeners
        cs.addChangeListener(listener);
    }

    public final synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

}
