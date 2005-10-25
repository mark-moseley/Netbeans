/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import java.io.IOException;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;



/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemNode extends FilterNode {
    
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    
    private String name;
    private String displayName;
    private String description;
    private Image icon16;
    private Image icon32;
    private Lookup localLookup;
    
    PaletteItemNode(DataNode original, String name, String displayName, String description, Image icon16, Image icon32, InstanceContent content ) {
        super(original, Children.LEAF, new AbstractLookup(content));
        
        content.add( this );
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon16 = icon16;
        this.icon32 = icon32;
    }
 
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return description;
    }

    public Image getIcon(int type) {

        Image icon = icon16;
        
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32)
            icon = icon32;
        
        return icon;
    }
    
    public boolean canRename() {
        return false;
    }

    // TODO properties
    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        ActiveEditorDrop drop = (ActiveEditorDrop) lookup.lookup(ActiveEditorDrop.class);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);

        return t;
    }

    public Transferable drag() throws IOException {
        Transferable t = clipboardCopy();
        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
    
}
