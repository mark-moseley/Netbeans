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
package org.netbeans.modules.java.debug;

import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class ElementNavigatorProviderImpl implements NavigatorPanel {
    
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    
    /**
     * Default constructor for layer instance.
     */
    public ElementNavigatorProviderImpl() {
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    List<Highlight> result = new ArrayList<Highlight>();
                    FileObject file = ElementNavigatorJavaSourceFactory.getInstance().getFile();
                    
                    if (file == null)
                        return ;
                    
                    for (Node n : manager.getSelectedNodes()) {
                        result.add((Highlight) n);
                    }
                    
                    Highlighter.getDefault().setHighlights(file, "tree", result);
                }
            }
        });
    }
    
    public String getDisplayName() {
        return "Elements";
    }
    
    public String getDisplayHint() {
        return "Elements View";
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final BeanTreeView view = new BeanTreeView();
            view.setRootVisible(true);
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }

    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        ElementNavigatorJavaSourceFactory.getInstance().setLookup(context, new TaskImpl());
    }

    public void panelDeactivated() {
        ElementNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
    }
    
    private final class TaskImpl implements CancellableTask<CompilationInfo> {
        
        public void cancel() {
        }

        public void run(CompilationInfo info) {
            manager.setRootContext(ElementNode.getTree(info, info.getTrees().getElement(new TreePath(info.getCompilationUnit()))));
        }
        
    }
    
}
