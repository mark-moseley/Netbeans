/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.* ;
import java.net.MalformedURLException;
import java.util.* ;

import org.apache.tools.ant.* ;
import org.apache.tools.ant.taskdefs.* ;
import org.apache.tools.ant.types.* ;

/** Runs the makenbm task for each locale specified in the
 * global property locmakenbm.locales.
 * NOTE: Currently this runs makelnbm, since the new
 * functionality in that hasn't been merged into makenbm
 * yet.
 *
 * @author Jerry Huth (email: jerry@solidstep.com)
 */
public class LocMakeNBM extends Task {

  protected String locales = null ;
  protected String mainDir = null ;
  protected File topDir = null ;
  protected String fileName = null ;
  protected String baseFileName = null ;
  protected boolean deleteInfo = false ;
  protected String nbmIncludes = null ;
  protected String modInfo = null ;
  protected String findLocBundle = "." ;  // relative to the directory 
				          // corresponding to the module's 
                                          // codename
  protected File locBundle = null ;  // path to localizing bundle - overrides 
                                     // findLocBundle
  protected String locIncludes = null ; // comma-separated list of 
                                        // "<locale>:<pattern>" elements

  public void setLocales( String s) {
    locales = s ;
  }
  public void setMainDir( String s) {
    mainDir = s ;
  }
  public void setTopDir( File f) {
    topDir = f ;
  }
  public void setFile( String s) {
    fileName = s ;
    if( !fileName.substring( fileName.length() - 4).equals( ".nbm")) {
      throw new BuildException( "NBM file name must end in '.nbm'") ;
    }
    baseFileName = fileName.substring( 0, fileName.length() - 4) ;
  }
  public void setDeleteInfo( boolean b) {
    deleteInfo = b ;
  }
  public void setNbmIncludes( String s) {
    nbmIncludes = s ;
  }
  public void setModInfo( String s) {
    modInfo = s ;
  }
  public void setLocBundle( File f) {
    locBundle = f ;
  }
  public void setFindLocBundle( String s) {
    findLocBundle = s ;
  }
  public void setLocIncludes( String s) {
    locIncludes = s ;
  }

  public void execute() throws BuildException {
    try {
      really_execute() ;

    } catch( BuildException be) {
      be.printStackTrace();
      throw be ;
    }
  }

  public void really_execute() throws BuildException {
    String locs, loc ;
    StringTokenizer stok ;
    LinkedList build_locales = new LinkedList() ;
    ListIterator iterator ;

    // Set default values. //
    if( mainDir == null) {
      mainDir = new String( "netbeans") ;
    }
    if( topDir == null) {
      topDir = getProject().getBaseDir() ;
    }

    // Print a warning and stop if the topDir doesn't exist. //
    if( printMissingDirWarning()) {
      return ;
    }

    locs = getLocales() ;
    if( locs == null || locs.trim().equals( "")) {
      throw new BuildException( "Must specify 1 or more locales.") ;
    }
    if( fileName == null) {
      throw new BuildException( "Must specify the file attribute.") ;
    }

    // I couldn't get it to work unless I explicitly added the task def here. //
    project.addTaskDefinition( "makelnbm", MakeLNBM.class) ;

    // Get a list of the locales for which localized files exist. //
    stok = new StringTokenizer( locs, ",") ;
    while( stok.hasMoreTokens()) {
      loc = stok.nextToken() ;
      if( hasFilesInLocale( loc)) {
	build_locales.add( loc) ;
      }
    }

    // For each locale that we need to build an NBM for. //
    iterator = build_locales.listIterator() ;
    while( iterator.hasNext()) {

      // Build the NBM for this locale. //
      buildNbm( (String) iterator.next()) ;
    }
  }

  /** Build the NBM for this locale. */
  protected void buildNbm( String locale) throws BuildException {
    MakeLNBM makenbm ;
    LinkedList list = new LinkedList() ;
    ListIterator iterator ;
    String includes = new String() ;
    String s ;
    File licenseFile ;
    boolean first_time ;
    Delete del ;

    // Delete the Info directory if desired. //
    if( deleteInfo) {
      del = (Delete) project.createTask( "delete") ;
      del.init() ;
      del.setDir( new File( topDir.getAbsolutePath() + File.separator + "Info")) ;
      del.execute() ;
      del.setDir( new File( topDir.getAbsolutePath() + File.separator + "Info_" + 
			    locale)) ;
      del.execute() ;
    }
    else {

      // Move the Info_<locale> dir to Info. //
      switchInfo( true, locale) ;
    }

    makenbm = (MakeLNBM) project.createTask( "makelnbm") ;
    makenbm.init() ;

    makenbm.setModInfo( modInfo) ;
    makenbm.setLangCode( locale) ;
    String fname = getLocalizedFileName( locale);
    makenbm.setFile( new File( getProject().getBaseDir().getAbsolutePath() + 
			       File.separator + fname)) ;
    makenbm.setTopdir( topDir) ;
    makenbm.setIsStandardInclude( false) ;
    String distbase = getProject().getProperty("dist.base");
    if (distbase != null) {
//        try {
            int idx = fname.lastIndexOf('/');
            makenbm.setDistribution(distbase + "/" + fname.substring(idx + 1));
//        } catch (MalformedURLException e) {
//            throw new BuildException(e, getLocation());
//        }
    }
    licenseFile = getLicenseFile( locale) ;
    if( licenseFile != null) {
      MakeLNBM.Blurb blurb = makenbm.createLicense() ;
      blurb.setFile( licenseFile) ;
    }

    // Set the localizing bundle specified, or look for it. //
    if( locBundle != null) {
      setLocBundle( makenbm, getSpecificLocBundleFile( locBundle, locale)) ;
    }
    else {
      setLocBundle( makenbm, findLocBundle( makenbm, locale)) ;
    }

    // Set up the signing data if it's specified. //
    if( getKeystore() != null &&
	getStorepass() != null &&
	getAlias() != null) {
      MakeLNBM.Signature sign = makenbm.createSignature() ;
      sign.setKeystore( new File( getKeystore())) ;
      sign.setStorepass( getStorepass()) ;
      sign.setAlias( getAlias()) ;
    }

    // Get the list of include patterns for this locale. //
    addLocalePatterns( list, locale) ;

    // Create a comma-separated list of include patterns. //
    iterator = list.listIterator() ;
    first_time = true ;
    while( iterator.hasNext()) {
      s = (String) iterator.next() ;
      if( !first_time) {
	includes += "," ;
      }
      includes += s ;
      first_time = false ;
    }
    // Add any extra includes that were specified. //
    if( nbmIncludes != null && !nbmIncludes.trim().equals( "")) {
      if( !first_time) {
	includes += "," ;
      }
      includes += nbmIncludes ;
    }
    makenbm.setIncludes( includes) ;

    makenbm.execute() ;

    // Move the Info dir to Info_<locale>. //
    switchInfo( false, locale) ;
  }

  /** Return the license file associated with this locale if there is
   * one.
   */
  protected File getLicenseFile( String locale) {
    String license_prop_name = locale + ".license.file" ;
    String license_prop = project.getProperty( license_prop_name) ;
    File license = null ;
    if( license_prop != null) {
      license = new File( license_prop ) ;
    }
    return( license) ;
  }

  protected void switchInfo( boolean to_info,
			     String locale) {
    File dir ;

    if( to_info) {
      dir = new File( topDir.getAbsolutePath() + File.separator + "Info_" + locale) ;
      dir.renameTo( new File( topDir.getAbsolutePath() + File.separator + "Info")) ;
    }
    else {
      dir = new File( topDir.getAbsolutePath() + File.separator + "Info") ;
      dir.renameTo( new File( topDir.getAbsolutePath() + File.separator + "Info_" + 
			      locale)) ;
    }
  }

  /** Get the localized version of the NBM filename. */
  protected String getLocalizedFileName( String locale) {
    return( baseFileName + "_" + locale + ".nbm") ;
  }

  protected String getLocales() {
    if( locales != null) {
      return( locales) ;
    }
    return( getGlobalProp( "locmakenbm.locales")) ;
  }

  /** See if there are any files for the given locale. */
  protected boolean hasFilesInLocale( String loc) {
    FileSet fs ;
    boolean ret = true ;

    // Setup a fileset to find files in this locale. //
    fs = new FileSet() ;
    fs.setDir( topDir) ;
    addLocalePatterns( fs, loc) ;

    // See if there are any localized files for this locale. //
    String[] inc_files = fs.getDirectoryScanner( project).getIncludedFiles() ;
    if( inc_files.length == 0) {
      ret = false ;
    }

    return( ret) ;
  }

  /** Add the patterns to include the localized files for the given locale. */
  protected void addLocalePatterns( FileSet fs,
				    String loc) {
    LinkedList list = new LinkedList() ;
    ListIterator iterator ;

    // Get the list of patterns for this locale. //
    addLocalePatterns( list, loc) ;

    // For each pattern for this locale. //
    iterator = list.listIterator( 0) ;
    while( iterator.hasNext()) {

      // Add it to the includes list. //
      fs.createInclude().setName( (String) iterator.next()) ;
    }

  }

  protected void addLocalePatterns( LinkedList list,
				    String loc) {
    String dir = new String() ;
    String re = new String() ;

    dir = mainDir ;
    re = dir + "/**/*_" + loc + ".*" ; // pattern is: ${dir}/**/*_${locale}.*
    list.add( new String( re)) ;
    re = dir + "/**/" + loc + "/" ;    // pattern is: ${dir}/${locale}/
    list.add( new String( re)) ;

    addLocIncludes( list, loc) ;

    // For ja locale, include these other variants. //
    if( loc.equals( "ja")) {
      addLocalePatterns( list, "ja_JP.PCK") ;
      addLocalePatterns( list, "ja_JP.eucJP") ;
      addLocalePatterns( list, "ja_JP.SJIS") ;
      addLocalePatterns( list, "ja_JP.UTF-8") ;
      addLocalePatterns( list, "ja_JP.UTF8") ;
    }
  }

  protected void addLocIncludes( LinkedList list,
				 String loc) {
    StringTokenizer tkzr ;
    String locInc, incLocale, incPattern ;
    int idx ;

    if( locIncludes == null) {
      return ;
    }

    // For each locale-specific include. //
    tkzr = new StringTokenizer( locIncludes, ",\n\t ") ;
    while( tkzr.hasMoreTokens()) {
      locInc = tkzr.nextToken() ;
      idx = locInc.indexOf( ":") ;
      if( idx != -1) {
	incLocale = locInc.substring( 0, idx) ;
	incPattern = locInc.substring( idx+1) ;
	if( incLocale.equals( loc)) {
	  list.add( new String( incPattern)) ;
	}
      }
      else {
	list.add( new String( locInc)) ;
      }
    }
  }

  protected String getGlobalProp( String name) {
    String ret ;
    ret = project.getProperty( name) ;

    // Don't return empty strings or strings whose value contains a //
    // property that isn't set.					    //
    if( ret != null) {
      if( ret.trim().equals( "")) {
	ret = null ;
      }
      else if( ret.indexOf( "${") != -1) {
	ret = null ;
      }
    }
    return( ret) ;
  }

  protected String getKeystore() {
    return( getGlobalProp( "locmakenbm.keystore")) ;
  }

  protected String getStorepass() {
    return( getGlobalProp( "locmakenbm.storepass")) ;
  }

  protected String getAlias() {
    return( getGlobalProp( "locmakenbm.alias")) ;
  }

  /** If the topDir doesn't exist, warn the user and return true. */
  protected boolean printMissingDirWarning() {
    boolean ret = false ;
    if( !topDir.exists()) {
      log( "WARNING: Skipping this task: Directory " + topDir.getPath() + 
	   " doesn't exist.") ;
      ret = true ;
    }
    return( ret) ;
  }

  /** If the localizing bundle is there, use it. */
  protected void setLocBundle( MakeLNBM makenbm,
			       File bundle) {
    if( bundle != null && bundle.exists()) {
      makenbm.setLocBundle( bundle) ;
    }
    else {
      log( "WARNING: Localizing bundle not found: " + ((bundle==null)?(""):(bundle.getPath())) ) ;
    }
  }

  protected String getSrcDir( File file) {
    InputStreamReader isr ;
    FileInputStream fis ;
    char[] buf = new char[ 200] ;
    String s = null ;
    int idx, len ;

    try {

      // Read the srcdir from the file that locjar wrote. //
      fis = new FileInputStream( file) ;
      isr = new InputStreamReader( fis) ;
      len = isr.read( buf) ;
      if( len != -1) {
	if( buf[ len-1] == '\n') {
	  len-- ;
	}
	s = new String( buf, 0, len) ;
	idx = s.indexOf( "=") ;
	if( idx != -1) {
	  s = s.substring( idx + 1) ;
	  s.trim() ;
	}
	else {
	  s = null ;
	}
      }
    }
    catch( Exception e) {
      System.out.println( "ERROR: " + e.getMessage()) ;
      e.printStackTrace() ;
      throw new BuildException() ;
    }
    return( s) ;
  }

  protected File findLocBundle( MakeLNBM makenbm,
				String locale) {
    File srcdirfile, locdir ;
    int index ;
    String s, srcdir = null ;

    // See if the file containing the srcdir is there. //
    srcdirfile = new File( topDir.getAbsolutePath() + File.separator + 
			   "srcdir.properties") ;
    if( srcdirfile.exists()) {
      srcdir = getSrcDir( srcdirfile) ;
    }
    if( srcdir == null) {
      throw new BuildException( "ERROR: Could not get source dir from: " + srcdirfile.getPath()) ;
    }

    // Get the codename of this module. //
    index = modInfo.indexOf( "/") ;
    if( index != -1) {
      s = modInfo.substring( 0, index) ;
    }
    else {
      s = new String( modInfo) ;
    }

    // Convert to pathname and set the loc bundle. //
    s = s.replace( '.', '/') ;
    locdir = new File( getRelPath( srcdir + "/" + s, findLocBundle).
		       replace( '/', File.separatorChar)) ;
    return( getDefaultLocBundleFile( locdir, locale)) ;
  }

  protected File getDefaultLocBundleFile( File dir,
					  String locale) {
    return( new File( dir.getPath() + File.separator + "Bundle_" + locale + ".properties")) ;
  }

  protected File getSpecificLocBundleFile( File enBundle,
					   String locale) {
    String path = enBundle.getPath() ;
    int idx = path.lastIndexOf( '.') ;
    if( idx != -1) {
      return( new File( path.substring( 0, idx) + "_" + locale + path.substring( idx))) ;
    }
    else {
      return( new File( path + "_" + locale)) ;
    }
  }

  /** This supports ".." path elements at the start of path2. */
  protected String getRelPath( String path1,
			       String path2) {
    int idx1, idx2 ;

    if( path2.equals( ".")) {
      return( path1) ;
    }

    // For each ".." element in path2. //
    while( true) {
      idx2 = path2.indexOf( "..") ;
      if( idx2 == -1) {
	break ;
      }

      // Strip off the ".." //
      path2 = path2.substring( 2) ;

      // Strip off the slash if it starts with slash. //
      idx2 = path2.indexOf( "/") ;
      if( idx2 == 0) {
	path2 = path2.substring( 1) ;
      }

      // Strip off the last element of path1. //
      idx1 = path1.lastIndexOf( "/") ;
      if( idx1 != -1) {
	path1 = path1.substring( 0, idx1) ;
      }
    }

    return( path1 + "/" + path2) ;
  }

}
