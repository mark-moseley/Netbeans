/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;

import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileUtil;

/**
 * Class which is able to serve index files of Javadoc for all
 * currently used Javadoc documentation sets.
 * @author Petr Hrebejk
 */
public class JavadocRegistry {
        
    private static JavadocRegistry INSTANCE;
    
    private Set /*<FileObject*/ roots;
    
    /** Creates a new instance of JavadocRegistry */
    private JavadocRegistry() {
        roots = new HashSet();
        readRoots();
    }
    
    public static synchronized JavadocRegistry getDefault() {
        if ( INSTANCE == null ) {
            INSTANCE = new JavadocRegistry();
        }
        return INSTANCE;
    }

    /** Returns Array of the Javadoc Index roots
     */
    public synchronized FileObject[] getDocRoots() {
        // XXX shouldn't this be conditional on whether roots != null?
        readRoots();
        FileObject[] result = new FileObject[ roots.size() ];
        roots.toArray( result );
        return result;
    }
    
    
    public JavadocSearchType findSearchType( FileObject apidocRoot ) {
        // XXX Should try to find correct engine
        JavadocSearchType type = (JavadocSearchType)DocumentationSettings.getDefault().getSearchEngine();
        assert type != null;
        return type;
    }    
        
    // Private methods ---------------------------------------------------------
    
    private void readRoots() {
        List paths = new LinkedList();
        paths.addAll( GlobalPathRegistry.getDefault().getPaths( ClassPath.COMPILE ) );        
        paths.addAll( GlobalPathRegistry.getDefault().getPaths( ClassPath.BOOT ) );
        
        roots.clear();
        for( Iterator it = paths.iterator(); it.hasNext(); ) {
            ClassPath ccp = (ClassPath)it.next();
            //System.out.println("CCP " + ccp );
            FileObject ccpRoots[] = ccp.getRoots();
            
            for( int i = 0; i < ccpRoots.length; i++ ) {
                //System.out.println(" CCPR " + ccpRoots[i]);
                URL[] jdRoots = JavadocForBinaryQuery.findJavadoc( URLMapper.findURL(ccpRoots[i], URLMapper.EXTERNAL ) ).getRoots();
                    
                for ( int j = 0; j < jdRoots.length; j++ ) {
                    //System.out.println( "  JDR " + jdRoots[j] );
                    //System.out.println("Looking for root of: "+jdRoots[j]);
                    FileObject fo = URLMapper.findFileObject(jdRoots[j]);
                    //System.out.println("Found: "+fo);
                    if (fo != null) {                        
                        roots.add(fo);
                    }
                }
                                    
            }
        }
        //System.out.println("roots=" + roots);
    }            
        
}
