/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views;

import java.util.Enumeration;
import org.netbeans.spi.looks.*;
import org.openide.util.Lookup;
import org.openide.util.enum.SingletonEnumeration;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import threaddemo.model.Phadhail;

/**
 * A look selector matching PhadhailLook.
 * @author Jesse Glick
 */
final class PhadhailLookProvider implements LookProvider {
    
    private static final Look PHADHAIL_LOOK = new PhadhailLook();
    private static final Look STRING_LOOK = new StringLook();
    private static final Look ELEMENT_LOOK = new ElementLook();
    
    public PhadhailLookProvider() {}
    
    public Enumeration getLooksForObject(Object representedObject) {
        if (representedObject instanceof Phadhail) {
            return new SingletonEnumeration(PHADHAIL_LOOK);
        } else if (representedObject instanceof String) {
            return new SingletonEnumeration(STRING_LOOK);
        } else {
            assert representedObject instanceof Element : representedObject;
            assert representedObject instanceof EventTarget : representedObject;
            return new SingletonEnumeration(ELEMENT_LOOK);
        }
    }
    
    /**
     * Just shows plain text nodes - markers.
     */
    private static final class StringLook extends Look {
        public StringLook() {
            super("StringLook");
        }
        public String getDisplayName() {
            return "Simple Messages";
        }
        public String getName(Object o, Lookup l) {
            return (String)o;
        }
        public String getDisplayName(Object o, Lookup l) {
            return (String)o;
        }
        public boolean isLeaf(Object o, Lookup l) {
            return true;
        }
    }
    
}
