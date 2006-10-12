/*
 * GlobalElementsNode.java
 *
 * Created on September 18, 2006, 3:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.Image;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class GlobalElementsNode extends AbstractNode {
    
    
    /** Creates a new instance of ABEDocumentNode */
    public GlobalElementsNode(ABEUIContext context, AXIDocument document){
        super(new GlobalElements(context, document));
        setName(NbBundle.getMessage(GlobalContentModelsNode.class,
                "LBL_CategoryNode_GlobalElementsNode"));
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public Image getOpenedIcon(int i) {
        return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                CategorizedChildren.getBadgedFolderIcon(i, GlobalElement.class);
    }
    
    public Image getIcon(int i) {
        return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                CategorizedChildren.getOpenedBadgedFolderIcon(i, GlobalElement.class);
    }
        
    private static class GlobalElements extends Children.Keys {
        GlobalElements(ABEUIContext context, AXIDocument document) {
            super();
            this.context = context;
            this.document = document;
        }
        protected Node[] createNodes(Object key) {
            if(key instanceof Element) {
                Node node = context.getFactory().createNode(getNode(), (Element)key);
                return new Node[] {node};
            }
            assert false;
            return new Node[]{};
        }
        
        protected void addNotify() {
            setKeys(document.getElements());
        }
        
        private AXIDocument document;
        private ABEUIContext context;
    }
    
}
