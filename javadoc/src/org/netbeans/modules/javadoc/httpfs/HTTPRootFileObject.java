/**************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Software is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2002.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *	<p>Represents the root file found on the file sysetm.</p>
 *
 *	@since 3.4
 */
class HTTPRootFileObject
    extends HTTPFileObject implements Runnable {

    private static final String IDE_SETTINGS_NAME = "Services/org-netbeans-core-IDESettings.settings"; // NOI18N
    private Thread  refreshThread;
    private Date    lastRefreshDate;
    private boolean threadIsRunning;
	private boolean refreshPending;
    
    private static boolean proxyInit;

    /**
     *	Constructs a <code>HTTPRootFileObject</code> with the file system passed.
     *
     *	@param parentFileSystem The file system that contains this file.
     *
     *	@since 3.4
     */
    HTTPRootFileObject(
        HTTPFileSystem parentFileSystem
    ) {

        super( "/", parentFileSystem );    // NOI18N

        // Start reading the items in the root directory in the background
        refreshThread = new Thread( this );
        threadIsRunning = true;
        refreshPending = false;
        refreshThread.start( );

    }


    /**
     *	Constructs an empty <code>HTTPRootFileObject</code>.  This constructor is only
     *	expected to be used during deserialization.
     *
     *	@since 3.4
     */
    protected HTTPRootFileObject(
    ) {

        super( );

    }


    /**
     *  Forces the filesystem to be refreshed.
     *
     *  @since 3.4
     */
    void triggerRefresh( ) {

        refreshPending = true;
        refreshThread.interrupt( );

    }


    /**
     *  Called to initialize the root file object in the background, and to
     *  run the background refresh thread.
     *
     *  @since 3.4
     */
    public void run( ) {

        // Interfal in minutes to check the web site for a new version
        int         refreshInterval;
        // The next time the web site is scheduled to be checked
        Calendar    nextRefreshTime;
        // Flags whether the docs have changed
        boolean     docsHaveChanged;


        // Start by reading the web site's contents for the first time
        refreshRootContents( );
        refreshThread.setPriority( Thread.MIN_PRIORITY );

        while( threadIsRunning ) {

            docsHaveChanged = false;

            // If this file system is configured to be refreshed,
            refreshInterval = parentFileSystem.getRefreshRate( );
            if( refreshInterval > 0 ) {

                // Calculate the next time this file object should try to refresh
                nextRefreshTime = new GregorianCalendar( );
                nextRefreshTime.setTime( lastRefreshDate );
                nextRefreshTime.add( Calendar.MINUTE, refreshInterval );

                // If the next refresh time has passed,
                if( nextRefreshTime.before( new GregorianCalendar( ) ) ) {

                    // Check if the documentation has changed
                    docsHaveChanged = hasDocumentationChanged( );

                }

            }

            // If the JavaDocs need to be refreshed,
            if( docsHaveChanged || refreshPending ) {

                // Refresh the contents of this root file object
                refreshPending = false;
                refreshRootContents( );

            }

            try {

                // Test if it's time to check once every minute
                Thread.sleep( 60 * 1000 );

            } catch( InterruptedException e ) {

                // Ignore

            }

        }

    }


    /**
     *  Checks if the "package-list" on the web site is newer than the version
     *  in memory.
     *
     *  @return True if the web site has changed, false if not.
     *
     *  @since 3.4
     */
    private boolean hasDocumentationChanged(
    ) {

        // File object for /package-list
        HTTPFileObject      packageFile;
        // Connection to the web server for the package-list file
        HttpURLConnection   fileConnection;
        // Current date/time of the current file in memory
        Date                currentPackageFileDate;
        // Flag to return if the we site file has changed
        boolean             hasChanged;


        // Get the local "package-list" file
        packageFile = child( "package-list", false );   //NOI18N

        // If there is a local "package-list" file,
        if( packageFile != null ) {

            try {

                // Compare the date of the local "package-list" file to the one on the web server
                currentPackageFileDate = packageFile.lastModified( );
                fileConnection = (HttpURLConnection)packageFile.fileURL.openConnection( );
                fileConnection.setRequestMethod( "HEAD" );  // NOI18N

                hasChanged = currentPackageFileDate.before( new Date( fileConnection.getLastModified( ) ) );

                fileConnection.disconnect( );

            } catch( IOException e ) {

                // There's a problem at the moment - force the refresh thread to not read this site
                hasChanged = false;

            }

        // If there is no local "package-list" file,
        } else {

            // Try again
            hasChanged = true;

        }
        return hasChanged;

    }


    /**
     *	Reads the base files available at the URL to build the directory tree.
     *
     *	@since 3.4
     */
    private void refreshRootContents( ) {
        initHTTPProxyHack();
        // File object for /package-list
        HTTPFileObject	packageFile;
        // File object for /index-files/ directory
        HTTPFileObject	indexDirectory;
        // Reader of package names in /package-list
        BufferedReader packageReader;
        // Package name read from /package-list
        String packageName;
        // File number for the next split index file
        int indexFileNumber;


        // Tell the file system that it is being refreshed
        parentFileSystem.setState( HTTPFileSystem.STATE_READING );
        
        // Remove all existing children from the root file object
        removeAllChildren( );

        // Add the standard files for a Javadoc directory structre
        if( addOptionalChild( "/package-list" ) ) { //NOI18N

            packageFile = child( "package-list", false );       //NOI18N
            addChild( "/allclasses-frame.html" );        //NOI18N
            addOptionalChild( "/deprecated-list.html" ); //NOI18N
            addOptionalChild( "/help-doc.html" );        //NOI18N
            addOptionalChild( "/index.html" );           //NOI18N
            addChild( "/overview-frame.html" );          //NOI18N
            addChild( "/overview-summary.html" );        //NOI18N
            addOptionalChild( "/overview-tree.html" );   //NOI18N
            addChild( "/packages.html" );                //NOI18N
            addChild( "/serialized-form.html" );         //NOI18N
            addChild( "/stylesheet.css" );               //NOI18N

            // Add the full index file
            if( !addOptionalChild( "/index-all.html" ) ) {   //NOI18N

                // If there was no full index, search for split index files
                indexFileNumber = 1;
                while( addOptionalChild( "/index-" + indexFileNumber + ".html" ) ) { //NOI18N

                    indexFileNumber++;                    

                }

                // If no index were found in the root,
                if( indexFileNumber == 1 ) {

                    // Look in /index-files/
                    indexDirectory = new HTTPFileObject( "/index-files/", parentFileSystem );   //NOI18N
                    // Add the full index file
                    if( !indexDirectory.addOptionalChild( "/index-files/index-all.html" ) ) {   //NOI18N

                        // If there was no full index, search for split index files
                        indexFileNumber = 1;
                        while( indexDirectory.addOptionalChild( "/index-files/index-" + indexFileNumber + ".html" ) ) { //NOI18N

                            indexFileNumber++;

                        }
                        // If index file were found in this directory,
                        if( indexFileNumber != 1 ) {

                            addChild( indexDirectory );

                        }

                    // If there was an index file found in this directory,

                    } else {

                        addChild( indexDirectory );

                    }

                }

            }
            try {

                // Read all of the package names from the /package-list file
                packageReader = new BufferedReader( new InputStreamReader( packageFile.getInputStream( ) ) );
                packageName = packageReader.readLine( );
                while( packageName != null ) {

                    // Add each package to this file system
                    addPackage( packageName );
                    packageName = packageReader.readLine( );

                }
                packageReader.close( );

            } catch( IOException e ) {

                // Ignore packages

            }

        }
        lastRefreshDate = new Date( );

        // Tell the file system that the refresh is done
        parentFileSystem.setState( HTTPFileSystem.STATE_COMPLETE );

    }


    /**
     *	Adds the named package to this file sytem.
     *
     *	@param packageName Package name to add to this file system.
     *
     *	@since 3.4
     */
    private void addPackage( String packageName ) {
        
        // Parser to break up the package heirarchy
        StringTokenizer packageParser;
        // One level of this package heirarchy
        String          packagePart;
        // The diretory that belongs to the selected package
        HTTPFileObject  packageDirectory;
        
        
        // Pull apart the package heirarchy
        packageParser = new StringTokenizer( packageName, "." );    //NOI18N
        packageDirectory = this;
        
        // With each level of the package,
        while( packageParser.hasMoreElements( ) ) {
            
            packagePart = (String)packageParser.nextElement( );
            
            // Find its directory object
            if( packageDirectory.child( packagePart, false ) == null ) {
                
                packageDirectory.addChild( packageDirectory.uriStem + packagePart + "/" );  //NOI18N

            }
            packageDirectory = packageDirectory.child( packagePart, false );            

        }
        // flag this directory as containing class files
        packageDirectory.makePackage( );

    }


    /**
     *  Cleans up this object when it is finalized.
     *
     *  @throws Throwable
     *
     *  @since 3.4
     */
    protected void finalize(
    ) throws Throwable {

        super.finalize( );

        // Close the refresh thread for this file
        threadIsRunning = false;
        refreshThread.interrupt( );

    }
    
    /**
     * Note that this is a *hack*, since it depends on some strange name given to the
     * IDESettings instance by the Core. But otherwise I don't know about a way
     * how to force the setting to be read.
     * //!!!
     */
    static void initHTTPProxyHack() {
        if (proxyInit)
            return;
        FileObject f = FileUtil.getConfigFile(IDE_SETTINGS_NAME);
        try {
            DataObject d = DataObject.find(f);
            InstanceCookie ic = (InstanceCookie)d.getCookie(InstanceCookie.class);
            if (ic != null) {
                Object o = ic.instanceCreate(); // NOPMD
                // OK, we've initialized.
                proxyInit = true;
            }
        } catch (DataObjectNotFoundException ex) {
        } catch (IOException ex) {
            proxyInit = true;
        } catch (ClassNotFoundException ex) {
            proxyInit = true;
        } 
    }
}
