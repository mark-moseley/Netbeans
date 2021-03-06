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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author Chris Webster
 */
public class ABENodeChildren extends Children.Keys
        implements ComponentListener {
    private AXIComponent component;
    
    /** Creates a new instance of ABENodeChildren */
    public ABENodeChildren(AXIComponent component) {
        this.component = component;
    }
    
    protected Node[] createNodes(Object key) {
        if (key instanceof Compositor) {
            return new Node[] {new CompositorNode((Compositor) key)};
        }
        
        if(key instanceof AnyElement)
            return new Node[] {new AnyElementNode((AnyElement)key)};
        else if (key instanceof AbstractElement) {
            return new Node[] {new ElementNode((AbstractElement)key)};
        }
        
        if (key instanceof ContentModel) {
            return new Node[] {new ContentModelNode((ContentModel)key)};
        }
        
        return new Node[0];
    }
        
    @Override
    protected void addNotify() {
        super.addNotify();
        refreshChildren();
        ComponentListener cl = WeakListeners.create(ComponentListener.class, this,
                component.getModel());
        component.getModel().addComponentListener(cl);
    }
    
    protected void removeNotify() {
        super.removeNotify();
        setKeys(Collections.emptyList());
    }
    
    public void valueChanged(ComponentEvent evt) {
    }
    
    public void childrenDeleted(ComponentEvent evt) {
        if (evt.getSource() == component) {
            refreshChildren();
        }
    }
    
    public void childrenAdded(ComponentEvent evt) {
        if (evt.getSource() == component) {
            refreshChildren();
        }
    }
    
    private void refreshChildren() {
        setKeys(divideComponents(component.getChildren()));
    }
    
    /**
     * Divide components and keep them in different buckets.
     */
    private List divideComponents(List<AXIComponent> list){
        //applicable only for AXIDocument's children.
        if(! (this.component instanceof AXIDocument) )
            return list;
        //separate out elements and complex types
        List<AXIContainer> el = new ArrayList<AXIContainer>();
        List<AXIContainer> cml = new ArrayList<AXIContainer>();
        for(AXIComponent comp: list) {
            if(comp instanceof AbstractElement) {
                el.add((AXIContainer) comp);
            }else if(comp instanceof ContentModel) {
                if( ((ContentModel)comp).getType() == ContentModel.ContentModelType.COMPLEX_TYPE)
                    cml.add((AXIContainer)comp);
            }
        }
        List<AXIContainer> result = new ArrayList<AXIContainer>();
        //club both arrays
        result.addAll(el);
        result.addAll(cml);
        return result;
    }
}
