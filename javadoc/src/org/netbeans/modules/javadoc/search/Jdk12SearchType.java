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

package org.netbeans.modules.javadoc.search;

import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/* Base class providing search for JDK1.2/1.3 documentation
 * @author Petr Hrebejk, Petr Suchomel
 */
public class Jdk12SearchType extends JavadocSearchType {

    private boolean caseSensitive = true;

    /** generated Serialized Version UID */
    static final long serialVersionUID =-2453877778724454324L;

    /** Returns human presentable name
     * @return human presentable name
    */
    public String displayName() {
        return NbBundle.getBundle( Jdk12SearchType.class ).getString("CTL_Jdk12_search_eng");   //NOI18N
    }

    /** Returns HelpCtx
     * @return help
     */    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (Jdk12SearchType.class);
    }
    
    /** Getter for property caseSensitive.
     * @return Value of property caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /** Setter for property caseSensitive.
     * @param caseSensitive New value of property caseSensitive.
    */
    public void setCaseSensitive(boolean caseSensitive) {
        boolean oldVal = this.caseSensitive;
        this.caseSensitive = caseSensitive;
        this.firePropertyChange("caseSensitive", oldVal ? Boolean.TRUE : Boolean.FALSE, caseSensitive ? Boolean.TRUE : Boolean.FALSE);   //NOI18N
    }

    public FileObject getDocFileObject( FileObject apidocRoot ) {
    
        FileObject fo = apidocRoot.getFileObject( "index-files" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = apidocRoot.getFileObject( "index-all.html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        return null;
    }    
    
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer ){
        return new SearchThreadJdk12 ( toFind, fo, diiConsumer, isCaseSensitive() );
    }


    public boolean accepts(FileObject apidocRoot, String encoding) {
        //XXX returns always true, must be the last JavadocType
        return true;
    }
}
