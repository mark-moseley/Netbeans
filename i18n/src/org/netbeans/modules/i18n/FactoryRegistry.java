/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/**
 * Registry which gets i18n support factories for specified data objects.
 * It gets the factories which are registered in SFS/Services/i18n directory
 * via lookup.
 *
 * @author  Peter Zavadsky
 * @see I18nSupport.Factory
 * @see org.netbeans.modules.i18n.form.FormI18nSupport.Factory
 * @see org.netbeans.modules.i18n.jsp.JspI18nSupport.Factory
 */
public final class FactoryRegistry extends Object {

    private FactoryRegistry() {};
    
    /** All i18n supports kept as <code>Lookup.Result</code>. */
    private static Lookup.Result result;
    private static final Set cache = Collections.synchronizedSet(new HashSet(5));    
    private static final Set ncache = Collections.synchronizedSet(new HashSet(50));    
    
    /** Gets lookup result holding script type instances. */
    private static Lookup.Result getSupports() {
        if(result == null) {
            result = Lookup.getDefault().lookup(new Lookup.Template(I18nSupport.Factory.class));
            result.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent e) {
                    cache.clear();
                    ncache.clear();
                }
            });
        }
        
        return result;
    }
    
    /** Gets <code>I18nSupportFactory</code> for specified data object class.
     * @return factory for specified data object class or <code>null</code> */
    public static I18nSupport.Factory getFactory(Class dataObjectClass) {
        
        List candidates = new ArrayList(3);
        
        for(Iterator it = getSupports().allInstances().iterator(); it.hasNext(); ) {
            I18nSupport.Factory factory = (I18nSupport.Factory)it.next();

            // XXX it has to be checked for null, for cases Jsp support and java support
            // don't have their modules available, see JspI18nSupportFactory.getDataObjectClass.
            Class clazz = factory.getDataObjectClass();
            
            if(clazz != null && clazz.isAssignableFrom(dataObjectClass)) {
                candidates.add(factory);
            }
        }
        
        if(candidates.size() == 0) {
            return null;
        } else if(candidates.size() == 1) {
            return (I18nSupport.Factory)candidates.get(0);
        } else {
            I18nSupport.Factory chosen = null;
            
            // Find factory which supported class data object 
            // is the lowest one in the class hierarchy.
            for(Iterator it = candidates.iterator(); it.hasNext(); ) {
                I18nSupport.Factory fct = (I18nSupport.Factory)it.next();
                
                if(chosen == null) {
                    chosen = fct;
                    continue;
                }

                if(chosen.getDataObjectClass().isAssignableFrom(fct.getDataObjectClass()) ) {
                    chosen = fct;
                }
            }
            
            return chosen;
        }
    }

    /** 
     * Indicates if there is a factory for that data object class. 
     * It queried very often from interactive mode.
     */
    public static boolean hasFactory(Class dataObjectClass) {
        
        if (cache.contains(dataObjectClass)) return true;
        if (ncache.contains(dataObjectClass)) return false;
        
        for(Iterator it = getSupports().allInstances().iterator(); it.hasNext(); ) {
            I18nSupport.Factory factory = (I18nSupport.Factory)it.next();

            // XXX it has to be checked for null, for cases Jsp support and java support
            // don't have their modules available, see JspI18nSupportFactory.getDataObjectClass.
            Class clazz = factory.getDataObjectClass();
            
            if(clazz != null && clazz.isAssignableFrom(dataObjectClass)) {
                cache.add(dataObjectClass);
                return true;
            }
        }

        ncache.add(dataObjectClass);
        return false;
    }
        
}
