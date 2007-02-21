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
package org.netbeans.modules.refactoring.api;

import java.util.Hashtable;
import org.netbeans.modules.refactoring.api.*;
import org.openide.util.Lookup;

/**
 * Where used query does not do any "real" refactoring.
 * It just encapsulates parameters for Find Usages, which is implemented by
 * plugins.
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class WhereUsedQuery extends AbstractRefactoring {
    /**
     * key for {@link getBooleanValue()}
     * is search in comments requested?
     */
    public static final String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS";
    /**
     * key for {@link getBooleanValue()}
     * is find references requested?
     */
    public static final String FIND_REFERENCES = "FIND_REFERENCES";
    
    /**
     * Creates a new instance of WhereUsedQuery.
     * WhereUsedQuery implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject} with content type text/x-java (class references)
     *                                    <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method references)
     *                                    </ul>
     *                              <td>Finds references</td></tr>
     * </table>
     * @param lookup put object for which you request references into Lookup instance.
     */
    public WhereUsedQuery(Lookup lookup) {
        super(lookup);
        putValue(FIND_REFERENCES, true);
    }

   /**
     * Setter for searched item
     * @param lookup put object for which you request references into Lookup instance.
     */
    public final void setRefactoringSource(Lookup lookup) {
        this.refactoringSource = lookup;
    }
    
    private Hashtable hash = new Hashtable();
    
    /**
     * getter for various properties
     * @param key 
     * @return value for given key
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final boolean getBooleanValue(Object key) {
        Object o = hash.get(key);
        if (o instanceof Boolean) 
            return (Boolean)o;
        return false;
    }
    
    /**
     * setter for various properties
     * @param key 
     * @param value set value for given key
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final void putValue(Object key, Object value) {
        hash.put(key, (Boolean) value);
    }
}

