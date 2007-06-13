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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.todo.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
final public class Settings {
    
    public static final String PROP_PATTERN_LIST = "patternList"; //NOI18N
    public static final String PROP_SCAN_COMMENTS_ONLY = "scanCommentsOnly"; //NOI18N
    
    private static Settings theInstance;
    private static final String PATTERN_DELIMITER = "|"; //NOI18N

    private ArrayList<String> patterns = new ArrayList<String>( 10 );
    private Map<String, CommentTags> ext2comments = new HashMap<String, CommentTags>( 10 );
    private boolean scanCommentsOnly = true;
    
    private PropertyChangeSupport propertySupport;
    
    /** Creates a new instance of Settings */
    private Settings() {
        patterns.addAll( decodePatterns( getPreferences().get( "patterns",  //NOI18N
                "@todo|TODO|FIXME|XXX|PENDING|<<<<<<<" )) ); //NOI18N
        
        scanCommentsOnly = getPreferences().getBoolean( "scanCommentsOnly", true ); //NOI18N
        
        ext2comments.put( "JAVA", new CommentTags( "//", "/*", "*/") ); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2comments.put( "C", new CommentTags( "//", "/*", "*/") ); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2comments.put( "CPP", new CommentTags( "//", "/*", "*/") ); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2comments.put( "CXX", new CommentTags( "//", "/*", "*/") ); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2comments.put( "HTML", new CommentTags( "<!--", "-->") ); //NOI18N //NOI18N //NOI18N
        ext2comments.put( "HTM", new CommentTags( "<!--", "-->")  ); //NOI18N //NOI18N //NOI18N
        ext2comments.put( "XML", new CommentTags( "<!--", "-->") ); //NOI18N //NOI18N //NOI18N
        ext2comments.put( "JSP", new CommentTags( "<%--", "--%>")  ); //NOI18N //NOI18N //NOI18N
        ext2comments.put( "PROPERTIES", new CommentTags("#") ); //NOI18N //NOI18N
        ext2comments.put( "SH", new CommentTags("#") ); //NOI18N //NOI18N
    }
    
    public static final Settings getDefault() {
        if( null == theInstance )
            theInstance = new Settings();
        return theInstance;
    }
    
    public Collection<String> getPatterns() {
        return Collections.unmodifiableCollection( patterns );
    }
    
    public void setPatterns( Collection<String> newPatterns ) {
        patterns.clear();
        patterns.addAll( newPatterns );
        getPreferences().put( "patterns", encodePatterns(newPatterns) ); //NOI18N
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.firePropertyChange( PROP_PATTERN_LIST, null, getPatterns() );
    }
    
    public boolean isExtensionSupported( String fileExtension ) {
        return null != ext2comments.get( fileExtension.toUpperCase() );
    }
    
    public String getLineComment( String fileExtension ) {
        CommentTags ct = ext2comments.get( fileExtension.toUpperCase() );
        return null == ct ? null : ct.lineComment;
    }
    
    public String getBlockCommentStart( String fileExtension ) {
        CommentTags ct = ext2comments.get( fileExtension.toUpperCase() );
        return null == ct ? null : ct.blockCommentStart;
    }
    
    public String getBlockCommentEnd( String fileExtension ) {
        CommentTags ct = ext2comments.get( fileExtension.toUpperCase() );
        return null == ct ? null : ct.blockCommentEnd;
    }
    
    public boolean isScanCommentsOnly() {
        return scanCommentsOnly;
    }
    
    public void setScanCommentsOnly( boolean val ) {
        boolean oldVal = scanCommentsOnly;
        this.scanCommentsOnly = val;
        getPreferences().putBoolean( "scanCommentsOnly", val ); //NOI18N
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.firePropertyChange( PROP_SCAN_COMMENTS_ONLY, oldVal, val );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        if( null != propertySupport )
            propertySupport.removePropertyChangeListener( l );
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( Settings.class );
    }
    
    private static Collection<String> decodePatterns( String encodedPatterns ) {
        StringTokenizer st = new StringTokenizer( encodedPatterns, PATTERN_DELIMITER, false );
        
        Collection<String> patterns = new ArrayList<String>();
        
        while( st.hasMoreTokens() ) {
            String im = st.nextToken();
            patterns.add(im);
        }
        
        return patterns;
    }
    
    private static String encodePatterns( Collection<String> patterns ) {
        StringBuffer sb = new StringBuffer();
        
        for( String p : patterns ) {
            sb.append( p );
            sb.append( PATTERN_DELIMITER ); 
        }
        
        return sb.toString();
    }
    
    private static class CommentTags {
        private String lineComment;
        private String blockCommentStart;
        private String blockCommentEnd;
        
        public CommentTags( String lineComment, String blockCommentStart, String blockCommentEnd ) {
            this.lineComment = lineComment;
            this.blockCommentStart = blockCommentStart;
            this.blockCommentEnd = blockCommentEnd;
        }
        
        public CommentTags( String blockCommentStart, String blockCommentEnd ) {
            this.blockCommentStart = blockCommentStart;
            this.blockCommentEnd = blockCommentEnd;
        }
        
        public CommentTags( String lineComment ) {
            this.lineComment = lineComment;
        }
    }
}
