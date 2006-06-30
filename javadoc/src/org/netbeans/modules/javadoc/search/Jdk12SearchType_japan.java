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




import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

/* Base class providing search for JDK1.2/1.3 documentation
 * Jdk12SearchType.java
 *
 * @author Petr Hrebejk, Petr Suchomel
 */
public class Jdk12SearchType_japan extends Jdk12SearchType {

    private String  japanEncoding;

    /** generated Serialized Version UID */
    static final long serialVersionUID =-2453877778724454324L;

    /** Returns human presentable name
     * @return human presentable name
    */
    public String displayName() {
        return NbBundle.getBundle( Jdk12SearchType_japan.class ).getString("CTL_Jdk12_search_eng_ja");   //NOI18N
    }

    /** Returns HelpCtx
     * @return help
     */    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (Jdk12SearchType_japan.class);
    }
    
    /** Getter for property encoding.
     * @return Value of property encoding.
    */
    public java.lang.String getJapanEncoding() {
        return ( japanEncoding != null ) ? japanEncoding : "JISAutoDetect";    //NOI18N
    }
    
    /** Setter for property encoding.
     * @param encoding New value of property encoding.
    */
    public void setJapanEncoding(java.lang.String japanEncoding) {
        String old = this.japanEncoding;
        this.japanEncoding = japanEncoding;
        firePropertyChange("japanEncoding", old, japanEncoding);   //NOI18N
    }    
        
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer ){
        //here you can send one more parameter .. getJapanEncoding
        return new SearchThreadJdk12_japan ( toFind, fo, diiConsumer, isCaseSensitive(), getJapanEncoding() );
    }    

    public boolean accepts(org.openide.filesystems.FileObject root, String encoding) {
        if (encoding == null) {
            return false;
        }
        encoding = encoding.toLowerCase();
        String acceptedEncoding = getJapanEncoding().toLowerCase();
        
        if ("jisautodetect".equals(acceptedEncoding)) {     //NOI18N
            return "iso-2022-jp".equals (encoding) ||       //NOI18N
                   "sjis".equals (encoding) ||              //NOI18N
                   "euc-jp".equals (encoding);              //NOI18N
                   // || "utf-".equals (encoding);  XXX Probably not, UTF-8 can be anything ????
                   
        }
        else {
            return  encoding.equals (acceptedEncoding);   //NOI18N
        }        
    }
    
    

    
    
}
