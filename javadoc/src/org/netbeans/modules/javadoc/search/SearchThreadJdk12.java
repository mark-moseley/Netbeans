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

package org.netbeans.modules.javadoc.search;

import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.DefaultListModel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.MutableAttributeSet;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

/** This class implements the index search through documenation
 * generated by Jdk 1.2 standard doclet
 */

class SearchThreadJdk12 extends IndexSearchThread {

    private BufferedReader in;
    private URL contextURL;

    private boolean stopSearch = false;

    private boolean splitedIndex = false;
    private int currentIndexNumber;
    private FileObject folder = null;
    private boolean caseSensitive;
    
    public SearchThreadJdk12 ( String toFind,
                               FileObject fo,
                               IndexSearchThread.DocIndexItemConsumer diiConsumer, boolean caseSensitive ) {

        super( toFind, fo, diiConsumer );
        this.caseSensitive = caseSensitive;
        
        if ( fo.isFolder() ) {
            // Documentation uses splited index - resolve the right file
            
            
            // This is just a try in most cases the fileNumber should be
            // the right one but when some index files are missing we have
            // to find the right one
            folder = fo;
            currentIndexNumber = (int)(Character.toUpperCase( toFind.charAt(0) ))  - 'A' + 1;
            if ( currentIndexNumber < 1 ) {
                currentIndexNumber = 1;
            }
            else if ( currentIndexNumber > 26 ) {
                currentIndexNumber = 27;
            }
                
            /*
            if ( currentIndexNumber < 1 || currentIndexNumber > 26 ) {
                currentIndexNumber = 27;
            }
            */
            findFileObject( 0 );
            
            splitedIndex = true;
        }
        else {
            try {
                contextURL = this.fo.getURL();
                //contextURL = this.fo.getParent().getURL();
            }
            catch ( org.openide.filesystems.FileStateInvalidException e ) {
                throw new InternalError( "Can't create documentation folder URL - file state invalid" ); // NOI18N
            }
            
            splitedIndex = false;
        }
    }

    public void stopSearch() {
        stopSearch = true;
        try {
            in.close();
        }
        catch ( java.io.IOException e ) {
            TopManager.getDefault().notifyException( e );
        }
    }

    public void run () {

        ParserDelegator pd = new ParserDelegator();
        
        if ( fo == null || toFind == null ) {
            taskFinished();
            return;
        }

        
        SearchCallbackJdk12 sc = null;

        int theDirection = 0;
        
        do {
            if ( sc != null ) {
                
                if (sc.badFile != theDirection ) {
                    break;
                }
                
                findFileObject( sc.badFile );
                if ( fo == null ) {
                    // No other file to search
                    break;
                }
            }

            try {    
                in = new BufferedReader( new InputStreamReader( fo.getInputStream () ));        
                pd.parse( in, sc = new SearchCallbackJdk12( splitedIndex, caseSensitive ), true );
            }
            catch ( java.io.IOException e ) {
               // Do nothing
            }
            
            if ( sc.badFile != 0 && theDirection == 0 ) {
                theDirection = sc.badFile;
            }            
        }
        while ( sc.badFile != 0 );

        try {
            in.close();
        }
        catch ( java.io.IOException e ) {
            // Do nothing
        }
        //is.searchEnded();
        taskFinished();
    }
    
    void findFileObject( int direction ) {

        
        if ( direction < 0 ) {
            currentIndexNumber--;
        }
        else if ( direction > 0 ) {
            currentIndexNumber++;
        }
        
        do {
            
            // Assure the only one direction of looking for Files
            
            
            if ( currentIndexNumber < 0 || currentIndexNumber > 27 ) {
                fo = null;
                return;
            }

            Integer fileNumber = new Integer( currentIndexNumber );

            String fileName = new String( "index-" + fileNumber.toString() ); // NOI18N

            if ( folder == null ) {
                fo = null;
                return;
            }

            fo = folder.getFileObject( fileName, "html" ); // NOI18N

            if ( fo != null ) {
                try {
                    contextURL = this.fo.getURL();
                }
                catch ( org.openide.filesystems.FileStateInvalidException e ) {
                    throw new InternalError( "Can't create documentation folder URL - file state invalid" ); // NOI18N
                }
            }
            else {
                
                currentIndexNumber += direction > 0 ? 1 : -1;
            }
        }
        while ( fo == null );
        
    }

    // Inner classes ------------------------------------------------------------------------------------


    /* These are constants for the inner class */
    
    static private final String STR_CLASS = ResourceUtils.getBundledString( "JDK12_CLASS" );
    static private final String STR_INTERFACE = ResourceUtils.getBundledString( "JDK12_INTERFACE" );
    static private final String STR_EXCEPTION = ResourceUtils.getBundledString( "JDK12_EXCEPTION" );
    static private final String STR_CONSTRUCTOR = ResourceUtils.getBundledString( "JDK12_CONSTRUCTOR" );
    static private final String STR_METHOD = ResourceUtils.getBundledString( "JDK12_METHOD" );
    static private final String STR_ERROR = ResourceUtils.getBundledString( "JDK12_ERROR" );
    static private final String STR_VARIABLE = ResourceUtils.getBundledString( "JDK12_VARIABLE" );
    static private final String STR_STATIC = ResourceUtils.getBundledString( "JDK12_STATIC" );
    static private final String STR_DASH = ResourceUtils.getBundledString( "JDK12_DASH" );
    static private final String STR_PACKAGE = ResourceUtils.getBundledString( "JDK12_PACKAGE" );

    static private final int IN_BALAST = 0;
    static private final int IN_DT = 1;
    static private final int IN_AREF = 2;
    static private final int IN_B = 3;
    static private final int IN_DESCRIPTION = 4;
    static private final int IN_DESCRIPTION_SUFFIX = 5;
    
    /** This inner class parses the JDK 1.2 Documentation index and returns
     *  found indexItems. 
     */

    private class SearchCallbackJdk12 extends HTMLEditorKit.ParserCallback {

        private String              hrefVal;
        private DocIndexItem        currentDii = null;
        private int                 where = IN_BALAST;

        private boolean             splited;
        private boolean             stopOnNext = false;
        
        private int                 badFile = 0;         
        
        int printText = 0;
        
        SearchCallbackJdk12( boolean splited, boolean caseSensitive ) {
            super();
            this.splited = splited;
        }
        
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

            if ( t == HTML.Tag.DT ) {
                where = IN_DT;
            }
            else if ( t == HTML.Tag.A && where == IN_DT ) {
                where = IN_AREF;
                Object val = a.getAttribute( HTML.Attribute.HREF );
                if ( val != null ) {
                    hrefVal = (String) val.toString();
                }
            }
            else if ( t == HTML.Tag.A && where == IN_DESCRIPTION_SUFFIX ) {
                ; // Just ignore
            }
            else if ( t == HTML.Tag.B && where == IN_AREF ) {
                where = IN_AREF;
            }
            else {
                where = IN_BALAST;
            }
        }

        public void handleText(char[] data, int pos) {
            
            if ( where == IN_AREF ) {
                
                if ( stopOnNext ) {
                    try {
                        in.close();
                        where = IN_BALAST;
                        return;
                    }
                    catch ( java.io.IOException e ) {
                        TopManager.getDefault().notifyException( e );
                    }
                }
                
                String text = new String( data );
                
                if ( splited ) {
                    // it is possible that we search wrong file
                    char first = Character.toUpperCase( toFind.charAt( 0 ) );
                    char curr = Character.toUpperCase( data[0] );
                    if ( first != curr ) {
                        
                        badFile = first < curr ? -1 : 1;
                        try {
                           in.close();
                           where = IN_BALAST;
                           return;
                        }
                        catch ( java.io.IOException e ) {
                            TopManager.getDefault().notifyException( e );
                        }
                    }
                    
                }
                
                if ( text.startsWith( toFind ) && caseSensitive ) {
                    DocIndexItem dii = new DocIndexItem( text, null, contextURL, hrefVal );
                    //insertDocIndexItem( dii );
                    currentDii = dii;
                    where = IN_DESCRIPTION;
                }
                else if ( text.toUpperCase().startsWith( toFind.toUpperCase() ) && !caseSensitive ) {
                    DocIndexItem dii = new DocIndexItem( text, null, contextURL, hrefVal );
                    //insertDocIndexItem( dii );
                    currentDii = dii;
                    where = IN_DESCRIPTION;
                }
                /*
                else if ( text.substring( 0, Math.min(toFind.length(), text.length()) ).toUpperCase().compareTo( toFind.toUpperCase() ) > 0 ) {
                    // Stop suffering if we are behind the searched words
                    stopOnNext = true;
                }
                */
                else {
                    where = IN_BALAST;
                }
            }
            else if ( where == IN_DESCRIPTION  ) {
                String text = new String( data );
                
                /*
                // Stop suffering if we are behind the searched words
                if ( text.substring( 0, Math.min(toFind.length(), text.length()) ).compareTo( toFind ) > 0 ) {
                    try {
                        System.out.println("Stoping suffering");
                        in.close();
                    }
                    catch ( java.io.IOException e ) {
                        TopManager.getDefault().notifyException( e );
                    }
                }
                */
                
                currentDii.setRemark( text );
                text = text.toUpperCase();

                StringTokenizer st = new StringTokenizer( text );
                String token = st.nextToken();
                if ( token.equals( STR_DASH ) )
                    token = st.nextToken();

                boolean isStatic = false;

                if ( token.equals( STR_STATIC ) ) {
                    isStatic = true;
                    token = st.nextToken();
                }

                if ( token.equals( STR_CLASS ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_CLASS );
                else if ( token.equals( STR_INTERFACE ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_INTERFACE );
                else if ( token.equals( STR_EXCEPTION ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_EXCEPTION );
                else if ( token.equals( STR_ERROR ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_ERROR );
                else if ( token.equals( STR_PACKAGE ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_PACKAGE );
                else if ( token.equals( STR_CONSTRUCTOR ) )
                    currentDii.setIconIndex( DocSearchIcons.ICON_CONSTRUCTOR );
                else if ( token.equals( STR_METHOD ) )
                    currentDii.setIconIndex( isStatic ? DocSearchIcons.ICON_METHOD_ST : DocSearchIcons.ICON_METHOD );
                else if ( token.equals( STR_VARIABLE ) )
                    currentDii.setIconIndex( isStatic ? DocSearchIcons.ICON_VARIABLE_ST : DocSearchIcons.ICON_VARIABLE );

                // Add the item when all information is available
                insertDocIndexItem( currentDii );

                if ( text.endsWith( "." ) ) { // NOI18N
                    where = IN_DESCRIPTION_SUFFIX;
                    currentDii.setPackage( text.substring( text.lastIndexOf( ' ' ) ) );
                }
                else
                    where = IN_BALAST;
            }
            else if ( where == IN_DESCRIPTION_SUFFIX ) {
                currentDii.setRemark( currentDii.getRemark() + new String( data ));
            }
            else
                where = IN_BALAST;

        }

    }

}


/*
 * Log
 *  15   Gandalf   1.14        1/13/00  Petr Hrebejk    i18n mk3  
 *  14   Gandalf   1.13        1/12/00  Petr Hrebejk    i18n
 *  13   Gandalf   1.12        11/3/99  Petr Hrebejk    Missing index file and 
 *       doc/api for single index file fixed
 *  12   Gandalf   1.11        10/27/99 Petr Hrebejk    Bug fixes & back button 
 *       in Javadoc Quickview
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/16/99  Petr Hrebejk    Fix of errorneous 
 *       behavior in case of missing index-file for a character
 *  9    Gandalf   1.8         8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  8    Gandalf   1.7         6/23/99  Petr Hrebejk    HTML doc view & sort 
 *       modes added
 *  7    Gandalf   1.6         6/11/99  Petr Hrebejk    
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/16/99  Petr Hrebejk    
 *  4    Gandalf   1.3         5/16/99  Petr Hrebejk    
 *  3    Gandalf   1.2         5/14/99  Petr Hrebejk    
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $
 */
