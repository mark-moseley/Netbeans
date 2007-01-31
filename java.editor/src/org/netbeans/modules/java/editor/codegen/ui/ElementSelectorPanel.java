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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.lang.model.element.Element;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;

/**
 *
 * @author  Petr Hrebejk, Dusan Balek
 */
public class ElementSelectorPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager = new ExplorerManager();
    private BeanTreeView elementView;
    
    /** Creates new form ElementSelectorPanel */
    public ElementSelectorPanel(ElementNode.Description elementDescription) {        
        setLayout(new BorderLayout());
        elementView = new BeanTreeView();
        elementView.setRootVisible(false);
        elementView.setBorder(BorderFactory.createLineBorder(Color.gray));
        add(elementView, BorderLayout.CENTER);
        setRootElement(elementDescription);
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }
    
    public Iterable<ElementHandle<? extends Element>> getSelectedElements() {
        ArrayList<ElementHandle<? extends Element>> handles = new ArrayList<ElementHandle<? extends Element>>();
        for (Node node : manager.getSelectedNodes()) {
            if (node instanceof ElementNode)
                handles.add(((ElementNode)node).getElementHandle());
        }
        return handles;
    }
    
    public void setRootElement(ElementNode.Description elementDescription) {
        manager.setRootContext(elementDescription != null ? new ElementNode(elementDescription) : Node.EMPTY);
        elementView.expandAll();
    }
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
