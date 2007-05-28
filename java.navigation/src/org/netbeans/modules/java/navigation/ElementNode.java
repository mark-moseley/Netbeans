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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.navigation;


import java.awt.Image;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing an Element
 *
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode {

    
    private static Node WAIT_NODE;
    
    private OpenAction openAction;
    private Description description;
           
    /** Creates a new instance of TreeNode */
    public ElementNode( Description description ) {
        super(description.subs == null ? Children.LEAF: new ElementChilren(description.subs, description.ui.getFilters()));
        this.description = description;
        setDisplayName( description.name ); 
    }
    
    
    @Override
    public Image getIcon(int type) {
         return description.kind == null ? super.getIcon(type) : Utilities.icon2Image(UiUtils.getElementIcon(description.kind, description.modifiers));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        return description.name;
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        
        if ( context || description.name == null ) {
            return description.ui.getActions();
        }
        else {
            Action panelActions[] = description.ui.getActions();
            
            Action actions[]  = new Action[ 2 + panelActions.length ];
            actions[0] = getOpenAction();
            actions[1] = null;
            for( int i = 0; i < panelActions.length; i++ ){
                actions[2 + i] = panelActions[i];
            }
            return actions;
        }
    }        
    
    @Override
    public Action getPreferredAction() {
        return getOpenAction();
    }
    
    
    private synchronized Action getOpenAction() {
        if ( openAction == null ) {
            FileObject fo = description.getFileObject();
            openAction = new OpenAction(description.elementHandle, fo);
        }
        return openAction;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }
    
    public void refreshRecursively() {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
            boolean scrollOnExpand = description.ui.getScrollOnExpand();
            description.ui.setScrollOnExpand( false );
           ((ElementChilren)ch).resetKeys(description.subs, description.ui.getFilters());
           for( Node sub : ch.getNodes() ) {
               description.ui.expandNode(sub);
               ((ElementNode)sub).refreshRecursively();
           }
           description.ui.setScrollOnExpand( scrollOnExpand );
        }        
    }
    
    public ElementNode getNodeForElement( ElementHandle<Element> eh ) {
        
        if ( getDescritption().elementHandle != null &&
             eh.signatureEquals(getDescritption().elementHandle)) {
            return this;
        }
        
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
           for( Node sub : ch.getNodes() ) {
               ElementNode result = ((ElementNode)sub).getNodeForElement(eh);
               if ( result != null ) {
                   return result;
               }
           }
        }
        
        return null;
    }
    
    public void updateRecursively( Description newDescription ) {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {           
           HashSet<Description> oldSubs = new HashSet<Description>( description.subs );

           
           // Create a hashtable which maps Description to node.
           // We will then identify the nodes by the description. The trick is 
           // that the new and old description are equal and have the same hashcode
           Node[] nodes = ch.getNodes( true );           
           HashMap<Description,ElementNode> oldD2node = new HashMap<Description,ElementNode>();           
           for (Node node : nodes) {
               oldD2node.put(((ElementNode)node).description, (ElementNode)node);
           }
           
           // Now refresh keys
           ((ElementChilren)ch).resetKeys(newDescription.subs, newDescription.ui.getFilters());

           
           // Reread nodes
           nodes = ch.getNodes( true );
           
           for( Description newSub : newDescription.subs ) {
                ElementNode node = oldD2node.get(newSub);
                if ( node != null ) { // filtered out
                    if ( !oldSubs.contains(newSub) && node.getChildren() != Children.LEAF) {                                           
                        description.ui.expandNode(node); // Make sure new nodes get expanded
                    }     
                    node.updateRecursively( newSub ); // update the node recursively
                }
           }
        }
                        
        Description oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        if ( oldDescription.htmlHeader != null && !oldDescription.htmlHeader.equals(description.htmlHeader)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldDescription.htmlHeader, description.htmlHeader);
        }
        if( oldDescription.modifiers != null &&  !oldDescription.modifiers.equals(newDescription.modifiers)) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    public Description getDescritption() {
        return description;
    }
    
    private static final class ElementChilren extends Children.Keys<Description> {
            
        public ElementChilren(List<Description> descriptions, ClassMemberFilters filters ) {
            resetKeys( descriptions, filters );            
        }
        
        protected Node[] createNodes(Description key) {
            return new Node[] {new  ElementNode(key)};
        }
        
        void resetKeys( List<Description> descriptions, ClassMemberFilters filters ) {            
            setKeys( filters.filter(descriptions) );
        }
        
        
                        
    }
                       
    /** Stores all interesting data about given element.
     */    
    static class Description {
        
        public static final Comparator<Description> ALPHA_COMPARATOR =
            new DescriptionComparator(true);
        public static final Comparator<Description> POSITION_COMPARATOR = 
            new DescriptionComparator(false);    
        
        ClassMemberPanelUI ui;
                
        FileObject fileObject; // For the root description
        
        final String name;
        final ElementHandle<? extends Element> elementHandle;
        final ElementKind kind;
        Set<Modifier> modifiers;        
        List<Description> subs; 
        String htmlHeader;
        long pos;
        boolean isInherited;
        ClasspathInfo cpInfo;
        
        Description( ClassMemberPanelUI ui ) {
            this.ui = ui;
            this.name = null;
            this.elementHandle = null;
            this.kind = null;
            this.isInherited = false;
        }
         
        Description(ClassMemberPanelUI ui,
                    String name,
                    ElementHandle<? extends Element> elementHandle,
                    ElementKind kind,
                    boolean inherited ) {
            this.ui = ui;
            this.name = name;
            this.elementHandle = elementHandle;
            this.kind = kind;
            this.isInherited = inherited;
        }

        public FileObject getFileObject() {
            if( !isInherited )
                return ui.getFileObject();
            return SourceUtils.getFile( elementHandle, cpInfo );
        }
        
        @Override
        public boolean equals(Object o) {
                        
            if ( o == null ) {
                //System.out.println("- f nul");
                return false;
            }
            
            if ( !(o instanceof Description)) {
                // System.out.println("- not a desc");
                return false;
            }
            
            Description d = (Description)o;
            
            if ( kind != d.kind ) {
                // System.out.println("- kind");
                return false;
            }
            
            if ( !name.equals(d.name) ) {
                // System.out.println("- name");
                return false;
            }
            
            if ( !this.elementHandle.signatureEquals(d.elementHandle) ) {
                return false;
            }
            
            /*
            if ( !modifiers.equals(d.modifiers)) {
                // E.println("- modifiers");
                return false;
            }
            */
            
            // System.out.println("Equals called");            
            return true;
        }
        
        
        public int hashCode() {
            int hash = 7;

            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }
        
        private static class DescriptionComparator implements Comparator<Description> {
            
            boolean alpha;
            
            DescriptionComparator( boolean alpha ) {
                this.alpha = alpha;
            }
            
            public int compare(Description d1, Description d2) {
                
                if ( alpha ) {
                    return alphaCompare( d1, d2 );
                }
                else {
                    if( d1.isInherited && !d2.isInherited )
                        return 1;
                    if( !d1.isInherited && d2.isInherited )
                        return -1;
                    if( d1.isInherited && d2.isInherited ) {
                        return alphaCompare( d1, d2 );
                    }
                    return d1.pos == d2.pos ? 0 : d1.pos < d2.pos ? -1 : 1;
                }
            }
            
            int alphaCompare( Description d1, Description d2 ) {
                if ( k2i(d1.kind) != k2i(d2.kind) ) {
                    return k2i(d1.kind) - k2i(d2.kind);
                } 

                return d1.name.compareTo(d2.name);
            }
            
            int k2i( ElementKind kind ) {
                switch( kind ) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:                        
                        return 4;
                    default:
                        return 100;
                }
            }
            
        }
        
    }
        
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = Utilities.loadImage("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return NbBundle.getMessage(ElementNode.class, "LBL_WaitNode"); // NOI18N
        }
        
    }
    
    
}
