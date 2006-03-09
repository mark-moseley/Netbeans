package org.netbeans.modules.j2ee.websphere6.dd.loaders.appext;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

public class WSAppExtDataNode extends DataNode {
    
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/j2ee/websphere6/dd/resources/ws2.gif";
    
    public WSAppExtDataNode(WSAppExtDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
    
//    /** Creates a property sheet. */
//    protected Sheet createSheet() {
//        Sheet s = super.createSheet();
//        Sheet.Set ss = s.get(Sheet.PROPERTIES);
//        if (ss == null) {
//            ss = Sheet.createPropertiesSet();
//            s.put(ss);
//        }
//        // TODO add some relevant properties: ss.put(...)
//        return s;
//    }
    
}
