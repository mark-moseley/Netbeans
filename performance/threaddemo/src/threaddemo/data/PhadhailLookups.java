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

package threaddemo.data;

import java.util.*;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.*;
import threaddemo.model.Phadhail;

/**
 * Serves "cookies" for phadhails.
 * @author Jesse Glick
 */
public class PhadhailLookups {
    
    /** no instances */
    private PhadhailLookups() {}
    
    private static final Map lookups = new WeakHashMap(); // Map<Phadhail,PhadhailLookup>
    
    // XXX rather than being synch, should be readAccess, and modified/saved should be writeAccess
    public static synchronized Lookup getLookup(Phadhail ph) {
        Lookup l = (Lookup)lookups.get(ph);
        if (l == null) {
            l = new PhadhailLookup(ph);
            lookups.put(ph, l);
        }
        return l;
    }
    
    // Access from PhadhailEditorSupport
    static void modified(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).modified(s);
    }
    
    static void saved(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).saved(s);
    }

    // XXX #32203 would be really helpful here!
    private static final class PhadhailLookup extends AbstractLookup implements InstanceContent.Convertor {
        
        private static final Object KEY_EDITOR = "editor";
        private static final Object KEY_DOM_PROVIDER = "domProvider";
        
        private final Phadhail ph;
        // XXX Have to keep the InstanceContent separately; it is a field in AbstractLookup
        // but we cannot access it!
        private final InstanceContent c;
        
        public PhadhailLookup(Phadhail ph) {
            this(ph, new InstanceContent());
        }
        
        private PhadhailLookup(Phadhail ph, InstanceContent c) {
            super(c);
            this.ph = ph;
            this.c = c;
            if (!ph.hasChildren()) {
                c.add(KEY_EDITOR, this);
                /* XXX readd when DomSupport functional:
                if (ph.getName().endsWith(".xml")) {
                    c.add(KEY_DOM_PROVIDER, this);
                }
                 */
            }
        }
        
        public void modified(SaveCookie s) {
            c.add(s);
        }
        
        public void saved(SaveCookie s) {
            c.remove(s);
        }
        
        public Object convert(Object obj) {
            if (obj == KEY_EDITOR) {
                return new PhadhailEditorSupport(ph);
            } else {
                assert obj == KEY_DOM_PROVIDER;
                // XXX is it permitted to do a lookup inside another?
                PhadhailEditorSupport edit = (PhadhailEditorSupport)lookup(PhadhailEditorSupport.class);
                assert edit != null;
                Mutex m = ph.mutex(); // XXX may need a different mutex...
                return new DomSupport(ph, edit, m);
            }
        }
        
        public Class type(Object obj) {
            if (obj == KEY_EDITOR) {
                return PhadhailEditorSupport.class; // a bunch of interfaces
            } else {
                assert obj == KEY_DOM_PROVIDER;
                return DomProvider.class;
            }
        }
        
        public String displayName(Object obj) {
            throw new UnsupportedOperationException();
        }
        
        public String id(Object obj) {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
