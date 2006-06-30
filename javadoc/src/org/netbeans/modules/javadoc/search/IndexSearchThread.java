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

import java.util.StringTokenizer;

import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;

/** Abstract class for thread which searches for documentation
 *
 *  @author Petr Hrebejk, Petr Suchomel
 */

public abstract class IndexSearchThread implements Runnable  {

    // PENDING: Add some abstract methods

    //protected String                toFind;

    // documentation index file (or foldee for splitted index)
    protected FileObject            indexRoot;
    private   DocIndexItemConsumer  ddiConsumer;
    RequestProcessor.Task           rpTask = null;
    protected boolean caseSensitive;
    
    protected String lastField="";     //NOI18N
    protected String middleField="";   //NOI18N    
    protected String reminder="";   //NOI18N
    private int tokens=0;

    private String lastAdd ="";   //NOI18N
    private String lastDeclaring="";   //NOI18N
    /** This method must terminate the process of searching */
    abstract void stopSearch();

    public IndexSearchThread( String toFind, FileObject fo, DocIndexItemConsumer ddiConsumer, boolean caseSensitive ) {
        this.ddiConsumer = ddiConsumer;
        this.indexRoot = fo;
        this.caseSensitive = caseSensitive;
        
        //this.toFind = toFind;
        //rpTask = RequestProcessor.createRequest( this );

        StringTokenizer st = new StringTokenizer(toFind, ".");     //NOI18N
        tokens = st.countTokens();
        //System.out.println(tokens);
        
        if( tokens > 1 ){
            if( tokens == 2 ){
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }
            else{
                for( int i = 0; i < tokens-2; i++){
                    reminder += st.nextToken();
                    if( i+1 < tokens-2 )
                        reminder += '.';
                }            
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }            
        }
        else{
            lastField = toFind;            
        }
        if( !caseSensitive ){
            reminder    = reminder.toUpperCase();
            middleField = middleField.toUpperCase();
            lastField   = lastField.toUpperCase();
        }
        //System.out.println("lastField" + lastField);
    }

    protected synchronized void insertDocIndexItem( DocIndexItem dii ) {
        //no '.', can add directly
        //System.out.println("Inserting");
        /*
        try{
            PrintWriter pw = new PrintWriter( new FileWriter( "c:/javadoc.dump", true ));
            pw.println("\"" + dii.getField() +"\""+ " " + "\""+dii.getDeclaringClass()+ "\"" + " " + "\""+ dii.getPackage()+ "\"");
            pw.println("\"" + lastField + "\"" + " " + "\"" + middleField + "\"" + " " + "\"" + reminder + "\"");
            pw.flush();
            pw.close();
        }
        catch(IOException ioEx){ioEx.printStackTrace();}
        */
        String diiField = dii.getField();
        String diiDeclaringClass = dii.getDeclaringClass();
        String diiPackage = dii.getPackage();
        if( !caseSensitive ){
            diiField = diiField.toUpperCase();
            diiDeclaringClass = diiDeclaringClass.toUpperCase();
            diiPackage = diiPackage.toUpperCase();
        }
        
        if( tokens < 2 ){
            if( diiField.startsWith( lastField ) ){
                //System.out.println("------");
                //System.out.println("Field: " + diiField + " last field: " + lastAdd + " declaring " + diiDeclaringClass + " package " + diiPackage);
                if( !lastAdd.equals( diiField ) || !lastDeclaring.equals( diiDeclaringClass )){
                    //System.out.println("ADDED");
                    ddiConsumer.addDocIndexItem ( dii );
                    lastAdd = diiField;
                    lastDeclaring = diiDeclaringClass;
                }
                //System.out.println("------");                
            }
            else if( diiDeclaringClass.startsWith( lastField ) && dii.getIconIndex() == DocSearchIcons.ICON_CLASS ) {
                if( !lastAdd.equals( diiDeclaringClass ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Declaring class " + diiDeclaringClass + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiDeclaringClass;
                }
            }
            else if( diiPackage.startsWith( lastField + '.' ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ) {
                if( !lastAdd.equals( diiPackage ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Package " + diiPackage + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiPackage;
                }
            }
        }
        else{            
            if( tokens == 2 ){
                //class and field (method etc. are equals)
                //System.out.println(dii.getField() + "   " + lastField + "   " + dii.getDeclaringClass() + "   " + middleField);
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( middleField ) && diiDeclaringClass.equals( lastField ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (middleField + '.' + lastField) ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
            else{            
                //class and field (method etc. are equals)
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) && diiPackage.startsWith( reminder ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                //else if( diiDeclaringClass.equals(lastField) && diiPackage.startsWith( (reminder + '.' + middleField).toUpperCase()) ){
                else if( diiDeclaringClass.startsWith(lastField) && diiPackage.equals( (reminder + '.' + middleField + '.')) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (reminder + '.' + middleField + '.' + lastField) ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
        }
    }

    public void go() {
        rpTask = RequestProcessor.getDefault().post( this, 0, Thread.NORM_PRIORITY );
    }

    public void finish() {
        if ( !rpTask.isFinished() && !rpTask.cancel() )
            stopSearch();
        taskFinished();
    }

    public void taskFinished() {
        ddiConsumer.indexSearchThreadFinished( this );
    }

    /** Class for callback. Used to feed some container with found
     * index items;
     */

    public static interface DocIndexItemConsumer {

        /** Called when an item is found */
        public void addDocIndexItem ( DocIndexItem dii );

        /** Called when a task finished. May be called more than once */
        public void indexSearchThreadFinished( IndexSearchThread ist );


    }

}
