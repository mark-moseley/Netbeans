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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.text.Collator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements filtering for Add Module Dependency panel.
 * @author Jesse Glick
 */
final class AddModuleFilter {

    private final Set/*<ModuleDependency>*/ universe;
    private final String dependingModuleCNB;

    /**
     * Construct a filter given a list of possible dependencies.
     */
    public AddModuleFilter(Set/*<ModuleDependency>*/ universe, String dependingModuleCNB) {
        this.universe = universe;
        this.dependingModuleCNB = dependingModuleCNB;
        // Prime the cache:
        Iterator it = universe.iterator();
        while (it.hasNext()) {
            ModuleDependency dep = (ModuleDependency) it.next();
            dep.getFilterTokens(dependingModuleCNB);
        }
        // To test "Please wait" use:
        //try{Thread.sleep(2000);}catch(InterruptedException e){}
    }
    
    /**
     * Find matches for a search string.
     */
    public Set/*<ModuleDependency>*/ getMatches(String text) {
        String textLC = text.toLowerCase(Locale.ENGLISH);
        Set/*<ModuleDependency>*/[] matches = new Set[3];
        for (int i = 0; i < matches.length; i++) {
            // Within groups, just sort by module display name:
            matches[i] = new TreeSet(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
        }
        Iterator it = universe.iterator();
        while (it.hasNext()) {
            ModuleDependency dep = (ModuleDependency) it.next();
            int matchLevel = matches.length;
            Set/*<String>*/ tokens = dep.getFilterTokens(dependingModuleCNB);
            Iterator it2 = tokens.iterator();
            while (it2.hasNext()) {
                String token = ((String) it2.next()).toLowerCase(Locale.ENGLISH);
                // Presort by relevance (#71995):
                if (token.equals(textLC) || token.endsWith("." + textLC)) { // NOI18N
                    // Exact match (possibly after dot).
                    matchLevel = Math.min(0, matchLevel);
                } else if (token.indexOf("." + textLC) != -1) { // NOI18N
                    // Starts with match (after dot).
                    matchLevel = Math.min(1, matchLevel);
                } else if (token.indexOf(textLC) != -1) {
                    // Substring match.
                    matchLevel = Math.min(2, matchLevel);
                }
            }
            if (matchLevel < matches.length) {
                matches[matchLevel].add(dep);
            }
        }
        Set result = new LinkedHashSet();
        for (int i = 0; i < matches.length; i++) {
            result.addAll(matches[i]);
        }
        return result;
    }
    
    /**
     * Find which tokens actually matched a given dependency.
     */
    public Set/*<String>*/ getMatchesFor(String text, ModuleDependency dep) {
        String textLC = text.toLowerCase(Locale.US);
        Set/*<String>*/ tokens = new TreeSet(Collator.getInstance());
        Iterator it = dep.getFilterTokens(dependingModuleCNB).iterator();
        while (it.hasNext()) {
            String token = (String) it.next();
            if (token.toLowerCase(Locale.US).indexOf(textLC) != -1) {
                tokens.add(token);
            }
        }
        return tokens;
    }
    
}
