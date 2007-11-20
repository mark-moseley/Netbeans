/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.myorg.feedreader;

import java.awt.BorderLayout;
import java.io.Serializable;
import javax.swing.ActionMap;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

final class FeedTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static FeedTopComponent instance;
    
    private final ExplorerManager manager = new ExplorerManager();
    private final BeanTreeView view = new BeanTreeView();
    
    private FeedTopComponent() {
        setName(NbBundle.getMessage(FeedTopComponent.class, "CTL_FeedTopComponent"));
        setToolTipText(NbBundle.getMessage(FeedTopComponent.class, "HINT_FeedTopComponent"));
        setIcon(Utilities.loadImage("org/myorg/feedreader/rss16.gif", true));
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        view.setRootVisible(true);
        try {
            manager.setRootContext(new RssNode.RootRssNode());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        associateLookup(ExplorerUtils.createLookup(manager, map));
    }
    
    public static synchronized FeedTopComponent getDefault() {
        if (instance == null) {
            instance = new FeedTopComponent();
        }
        return instance;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    protected String preferredID() {
        return "FeedTopComponent";
    }
    
    protected Object writeReplace() {
        return new ResolvableHelper();
    }
    
    private static final class ResolvableHelper implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        public Object readResolve() {
            return FeedTopComponent.getDefault();
        }
        
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
