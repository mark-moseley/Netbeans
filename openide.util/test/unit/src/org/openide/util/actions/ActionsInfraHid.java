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

package org.openide.util.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.ActionMap;
import junit.framework.Assert;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class ActionsInfraHid implements ContextGlobalProvider {
    
    public ActionsInfraHid() {}

    private static final ActionMap EMPTY_MAP = new ActionMap();
    private static ActionMap currentMap = EMPTY_MAP;

    private static final AMLookup amLookup = new AMLookup();
    
    public Lookup createGlobalContext() {
        return amLookup;
    }

    private static Lookup.Result amResult;
    static {
        try {
            amResult = Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
            Assert.assertEquals(Collections.singleton(EMPTY_MAP), new HashSet(amResult.allInstances()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setActionMap(ActionMap newMap) {
        if (newMap == null) {
            newMap = EMPTY_MAP;
        }
        currentMap = newMap;
        amLookup.refresh();
        Assert.assertEquals(Collections.singleton(currentMap), new HashSet(amResult.allInstances()));
    }
    
    private static final class AMLookup extends ProxyLookup {
        public AMLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("AM.refresh; currentMap = " + currentMap);
            setLookups(new Lookup[] {
                Lookups.singleton(currentMap),
            });
        }
    }
    
    // Stolen from RequestProcessorTest.
    public static void doGC() {
        doGC(10);
    }
    public static void doGC(int count) {
        ArrayList l = new ArrayList(count);
        while (count-- > 0) {
            System.gc();
            System.runFinalization();
            l.add(new byte[1000]);
        }
    }

}
