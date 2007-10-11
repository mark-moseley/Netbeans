/*
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.ZipFileSet;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Makes a <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 *
 * @author Jesse Glick
 */
public class MakeNBM extends Task {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd");
    
    /** The same syntax may be used for either <samp>&lt;license&gt;</samp> or
     * <samp>&lt;description&gt;</samp> subelements.
     * <p>By setting the property <code>makenbm.nocdata</code> to <code>true</code>,
     * you can avoid using XML <code>CDATA</code> (for compatibility with older versions
     * of Auto Update which could not handle it).
     */
    public class Blurb {
        /** You may embed a <samp>&lt;file&gt;</samp> element inside the blurb.
         * If there is text on either side of it, that will be separated
         * with a line of dashes automatically.
         * But use nested <samp>&lt;text&gt;</samp> for this purpose.
         */
	public class FileInsert {
            /** File location. */
	    public void setLocation (File file) throws BuildException {
                boolean html = file.getName().endsWith(".html") || file.getName().endsWith(".htm");
                log("Including contents of " + file + " (HTML mode: " + html + ")", Project.MSG_VERBOSE);
		long lmod = file.lastModified ();
		if (lmod > mostRecentInput) mostRecentInput = lmod;
		addSeparator ();
		try {
		    InputStream is = new FileInputStream (file);
		    try {
			BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            if (html) {
                                // Clean out any markup first. First tags:
                                line = line.replaceAll("</?[a-zA-Z0-9_.:-]+( +[a-zA-Z0-9_.:-]+( *= *([^ \"]+|\"[^\"]*\"))?)*/?>", "");
                                // DOCTYPE:
                                line = line.replaceAll("<![a-zA-Z]+[^>]*>", "");
                                // Comments (single-line only at the moment):
                                line = line.replaceAll("<!--([^-]|-[^-])*-->", "");
                                // Common named character entities:
                                line = line.replaceAll("&quot;", "\"");
                                line = line.replaceAll("&nbsp;", " ");
                                line = line.replaceAll("&copy;", "\u00A9");
                                line = line.replaceAll("&apos;", "'");
                                line = line.replaceAll("&lt;", "<");
                                line = line.replaceAll("&gt;", ">");
                                line = line.replaceAll("&amp;", "&");
                            }
                            line = line.replaceAll("[\\p{Cntrl}&&[^\t]]", ""); // #74546
                            text.append(line);
                            text.append('\n');
                        }
		    } finally {
			is.close ();
		    }
		} catch (IOException ioe) {
                    throw new BuildException ("Exception reading blurb from " + file, ioe, getLocation ());
		}
	    }
	}
	private StringBuffer text = new StringBuffer ();
	private String name = null;
        /** There may be freeform text inside the element. Prefer to use nested elements. */
	public void addText (String t) {
	    addSeparator ();
	    // Strips indentation. Needed because of common style:
	    // <description>
	    //   Some text here.
	    //   And another line.
	    // </description>
	    t = getProject().replaceProperties(t.trim());
	    int min = Integer.MAX_VALUE;
	    StringTokenizer tok = new StringTokenizer (t, "\n");
	    boolean first = true;
	    while (tok.hasMoreTokens ()) {
		String line = tok.nextToken ();
		if (first) {
		    first = false;
		} else {
		    int i;
		    for (i = 0;
			 i < line.length () &&
			     Character.isWhitespace (line.charAt (i));
			 i++)
			;
		    if (i < min) min = i;
		}
	    }
	    if (min == 0) {
		text.append (t);
	    } else {
		tok = new StringTokenizer (t, "\n");
		first = true;
		while (tok.hasMoreTokens ()) {
		    String line = tok.nextToken ();
		    if (first) {
			first = false;
		    } else {
			text.append ('\n');
			line = line.substring (min);
		    }
		    text.append (line);
		}
	    }
	}
        /** Contents of a file to include. */
	public FileInsert createFile () {
	    return new FileInsert ();
	}
        /** Text to include literally. */
        public class Text {
            public void addText(String t) {
                Blurb.this.addText(t);
            }
        }
        // At least on Ant 1.3, mixed content does not work: all the text is added
        // first, then all the file inserts. Need to use subelements to be sure.
        /** Include nested literal text. */
        public Text createText() {
            return new Text();
        }
	private void addSeparator () {
	    if (text.length () > 0) {
		// some sort of separator
		if (text.charAt (text.length () - 1) != '\n')
		    text.append ('\n');
		text.append ("-----------------------------------------------------\n");
	    }
	}
        public org.w3c.dom.Text getTextNode(Document ownerDoc) {
            // XXX Current XMLUtil.write anyway does not preserve CDATA sections, it seems.
            String nocdata = getProject().getProperty("makenbm.nocdata");
            if (nocdata != null && Project.toBoolean(nocdata)) {
                return ownerDoc.createTextNode(text.toString());
            } else {
                return ownerDoc.createCDATASection(text.toString());
            }
	}
        /** @deprecated */
        @Deprecated
        public void setName(String name) {
            getProject().log(getLocation() + ": the 'name' attribute on <license> is deprecated", Project.MSG_WARN);
        }
	public String getName () {
            if (name == null) {
                name = crcOf(text);
            }
	    return name;
	}
        private String crcOf(StringBuffer text) {
            CRC32 crc = new CRC32();
            try {
                crc.update(text.toString().replaceAll("\\s+", " ").trim().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new BuildException(ex);
            }
            return Long.toHexString(crc.getValue()).toUpperCase(Locale.ENGLISH);
        }
        /** Include a file (and set the license name according to its basename). */
	public void setFile (File file) {
	    // This actually adds the text and so on:
	    new FileInsert ().setLocation (file);
	}
    }

    public class ExternalPackage {
	String name = null;
	String targetName = null;
	String startUrl = null;
	String description = null;

	public void setName(String n) {
	    this.name = n;
	}

	public void setTargetName(String t) {
	    this.targetName = t;
	}

	public void setStartURL(String u) {
	    this.startUrl = u;
	}
	
	public void setDescription(String d) {
	    this.description = d;
	}

    }

    /** <samp>&lt;signature&gt;</samp> subelement for signing the NBM. */
    public /*static*/ class Signature {
        public File keystore;
        public String storepass, alias;
        /** Path to the keystore (private key). */
        public void setKeystore(File f) {
            keystore = f;
        }
        /** Password for the keystore.
         * If a question mark (<samp>?</samp>), the NBM will not be signed
         * and a warning will be printed.
         */
        public void setStorepass(String s) {
            storepass = s;
        }
        /** Alias for the private key. */
        public void setAlias(String s) {
            alias = s;
        }
    }
    
    private File productDir = null;
    private File file = null;
    private File manifest = null;
    /** see #13850 for explanation */
    private String moduleName = null;
    private String homepage = null;
    private String distribution = "";
    private String needsrestart = null;
    private String moduleauthor = null;
    private String releasedate = null;
    private String global = null;
    private String targetcluster = null;
    private String jarSignerMaxMemory = "96m";
    private Blurb license = null;
    private Blurb description = null;
    private Blurb notification = null;
    private Signature signature = null;
    private long mostRecentInput = 0L;
    private boolean isStandardInclude = true;
    private ArrayList<ExternalPackage> externalPackages = null;
    private ArrayList<String> locales = null;
    private ArrayList<Attributes> moduleAttributes = null;
    private Attributes englishAttr = null;
    
    /** Try to find and create localized info.xml files */
    public void setLocales(String s) {
        locales = new ArrayList<String>();
        for (String st : s.split("[, ]+")) {
            locales.add(st);
        }
    }
    /** Include netbeans directory - default is true */
    public void setIsStandardInclude(boolean isStandardInclude) {
        this.isStandardInclude = isStandardInclude;
    }
    
    /** Directory of the product's files */
    public void setProductDir( File dir ) {
        productDir = dir;
    }
    
    /** Name of resulting NBM file. */
    public void setFile(File file) {
        this.file = file;
    }
    
    /** Module manifest needed for versioning.
     * @deprecated Use {@link #setModule} instead.
     */
    @Deprecated
    public void setManifest(File manifest) {
        this.manifest = manifest;
        long lmod = manifest.lastModified();
        if (lmod > mostRecentInput) mostRecentInput = lmod;
        log(getLocation() + "The 'manifest' attr on <makenbm> is deprecated, please use 'module' instead", Project.MSG_WARN);
    }
    /** Module JAR needed for generating the info file.
     * Information may be gotten either from its manifest,
     * or if it declares OpenIDE-Module-Localizing-Bundle in its
     * manifest, from that bundle.
     * The base locale variant, if any, is also checked if necessary
     * for the named bundle.
     * Currently no other locale variants of the module are examined;
     * the information is available but there is no published specification
     * of what the resulting variant NBMs (or variant information within
     * the NBM) should look like.
     */
    public void setModule(String module) {
        this.moduleName = module;
        // mostRecentInput updated below...
    }
    /** URL to a home page describing the module. */
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
    /** Does module need IDE restart to be installed? */
    public void setNeedsrestart (String needsrestart) {
        this.needsrestart = needsrestart;
    }
    /** Sets name of module author */
    public void setModuleauthor (String author) {
        this.moduleauthor = author;
    }
    /** Install globally? */
    public void setGlobal (String isGlobal) {
        this.global = isGlobal;
    }
    /** Sets pattern for target cluster */
    public void setTargetcluster (String targetCluster) {
        this.targetcluster = targetCluster;
    }
    /** Maximum memory allowed to be used by jarsigner task. Default is 96 MB. */
    public void setJarSignerMaxMemory (String jsmm) {
        this.jarSignerMaxMemory = jsmm;
    }
    /** Release date of NBM. */
    public void setReleasedate (String date) {
        this.releasedate = date;
    }
    /** URL where this NBM file is expected to be downloadable from. */
    public void setDistribution (String distribution) throws BuildException {
        this.distribution = distribution;
        if (!(this.distribution.equals(""))) {
            // check the URL
            try {
                URI uri = java.net.URI.create(this.distribution);
            } catch (IllegalArgumentException ile) {
                throw new BuildException("Distribution URL \"" + this.distribution + "\" is not a valid URI", ile, getLocation());
            }
        }
    }
    public Blurb createLicense () {
	return (license = new Blurb ());
    }
    public Blurb createNotification () {
	return (notification = new Blurb ());
    }    
    public Blurb createDescription () {
        log(getLocation() + "The <description> subelement in <makenbm> is deprecated except for emergency patches, please ensure your module has an OpenIDE-Module-Long-Description instead", Project.MSG_WARN);
	return (description = new Blurb ());
    }
    public Signature createSignature () {
	return (signature = new Signature ());
    }

    public ExternalPackage createExternalPackage(){
	ExternalPackage externalPackage = new ExternalPackage ();
	if (externalPackages == null)
	    externalPackages = new ArrayList<ExternalPackage>();
	externalPackages.add( externalPackage );
	return externalPackage;
    }
    
    private ZipFileSet main = null;
    
    public ZipFileSet createMain () {
        return (main = new ZipFileSet());
    }
    
    private Attributes getModuleAttributesForLocale(String locale) throws BuildException {
        if (locale == null) {
            throw new BuildException("Unknown locale: null",getLocation());
        }
        log("Processing module attributes for locale '"+locale+"'", Project.MSG_VERBOSE);
        Attributes attr = null;
        if ((!locale.equals("")) && (englishAttr != null)) {
            attr = new Attributes(englishAttr);
            attr.putValue("locale", locale);
            log("Copying English module attributes to localized attributes in locale "+locale,Project.MSG_VERBOSE);
            String om = attr.getValue("OpenIDE-Module");
            String omn = attr.getValue("OpenIDE-Module-Name");
            String omdc = attr.getValue("OpenIDE-Module-Display-Category");
            String omsd = attr.getValue("OpenIDE-Module-Short-Description");
            String omld = attr.getValue("OpenIDE-Module-Long-Description");
            if (om != null) log("OpenIDE-Module"+(locale.equals("")?"":"_"+locale)+" is "+om,Project.MSG_DEBUG);
            if (omn != null) log("OpenIDE-Module-Name"+(locale.equals("")?"":"_"+locale)+" is "+omn,Project.MSG_DEBUG);
            if (omdc != null) log("OpenIDE-Module-Display-Category"+(locale.equals("")?"":"_"+locale)+" is "+omdc,Project.MSG_DEBUG);
            if (omsd != null) log("OpenIDE-Module-Short-Description"+(locale.equals("")?"":"_"+locale)+" is "+omsd,Project.MSG_DEBUG);
            if (omld != null) log("OpenIDE-Module-Long-Description"+(locale.equals("")?"":"_"+locale)+" is "+omld,Project.MSG_DEBUG);
        } else {
            attr = new Attributes();
            attr.putValue("locale", locale);
        }
        moduleName = moduleName.replace(File.separatorChar, '/');
        String jarName = moduleName;
        String filename; String fname; String fext;
        if (!locale.equals("")) {
            // update file name for current locale
            filename = moduleName.substring(moduleName.lastIndexOf('/')+1);
            fname = filename.substring(0,filename.lastIndexOf('.'));
            fext = filename.substring(filename.lastIndexOf('.'));
            jarName = moduleName.substring(0,moduleName.lastIndexOf('/')) + "/locale/" + fname + "_" + locale + fext;
        }
        log("Going to open jarfile "+jarName,Project.MSG_VERBOSE);
        File mfile = new File(productDir, jarName );
        if ((mfile == null) || (!mfile.exists())) {
            // localizing jarfile does not exist, try to return english data
            if (englishAttr != null) {
                Attributes xattr = new Attributes(englishAttr);
                xattr.putValue("locale", locale);
                return xattr;
            } else {
                throw new BuildException("Unable to find English/localized data about module (locale is '"+locale+"')", getLocation());
            }
        }
        try {
            JarFile mjar = new JarFile(mfile);
            try {
                if (attr.getValue("OpenIDE-Module") == null ) {
                    attr = mjar.getManifest().getMainAttributes();
                    attr.putValue("locale", locale);
                }
                String bundlename = mjar.getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle");
                if ((bundlename == null) && (englishAttr != null)) {
                    String bname = englishAttr.getValue("OpenIDE-Module-Localizing-Bundle");
                    String bfname; String bfext;
                    if (bname != null) {
                        bname = bname.replace(File.separatorChar, '/');
                        bfname = bname.substring(0,bname.lastIndexOf('.'));
                        bfext = bname.substring(bname.lastIndexOf('.'));
                        bundlename = bfname + "_" + locale + bfext;
                        log("Determined ("+locale+") localizing bundle name: "+bundlename,Project.MSG_VERBOSE);
                    }
                }
                if (bundlename != null) {
                    Properties p = new Properties();
                    ZipEntry bundleentry = mjar.getEntry(bundlename);
                    if (bundleentry != null) {
                        InputStream is = mjar.getInputStream(bundleentry);
                        try {
                            p.load(is);
                        } finally {
                            is.close();
                        }
                        // Now pick up attributes from the bundle.
                        Iterator it = p.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry)it.next();
                            String name = (String)entry.getKey();
                            if (! name.startsWith("OpenIDE-Module-")) continue;
                            attr.putValue(name, (String)entry.getValue());
                        }
                    }
                }
            } finally {
                mjar.close();
            }
        } catch (IOException ioe) {
            throw new BuildException("exception while reading " + mfile.getName(), ioe, getLocation());
        }
        if (locale.equals("") && (englishAttr == null)) {
            log("Populating English module attributes", Project.MSG_VERBOSE);
            englishAttr = new Attributes(attr);
        }
        String om = attr.getValue("OpenIDE-Module");
        String omn = attr.getValue("OpenIDE-Module-Name");
        String omdc = attr.getValue("OpenIDE-Module-Display-Category");
        String omsd = attr.getValue("OpenIDE-Module-Short-Description");
        String omld = attr.getValue("OpenIDE-Module-Long-Description");
        if (om != null) log("OpenIDE-Module"+(locale.equals("")?"":"_"+locale)+" is "+om,Project.MSG_VERBOSE);
        if (omn != null) log("OpenIDE-Module-Name"+(locale.equals("")?"":"_"+locale)+" is "+omn,Project.MSG_VERBOSE);
        if (omdc != null) log("OpenIDE-Module-Display-Category"+(locale.equals("")?"":"_"+locale)+" is "+omdc,Project.MSG_VERBOSE);
        if (omsd != null) log("OpenIDE-Module-Short-Description"+(locale.equals("")?"":"_"+locale)+" is "+omsd,Project.MSG_VERBOSE);
        if (omld != null) log("OpenIDE-Module-Long-Description"+(locale.equals("")?"":"_"+locale)+" is "+omld,Project.MSG_VERBOSE);
        return attr;
    }

    @Override
    public void execute () throws BuildException {
        if (productDir == null) {
            throw new BuildException("must set directory of compiled product", getLocation());
        }
	if (file == null) {
	    throw new BuildException("must set file for makenbm", getLocation());
        }
        if (manifest == null && moduleName == null) {
            throw new BuildException("must set module for makenbm", getLocation());
        }
        if (manifest != null && moduleName != null) {
            throw new BuildException("cannot set both manifest and module for makenbm", getLocation());
        }
        if (locales == null) {
            locales = new ArrayList<String>();
        }

    File file;
    String rootDir = getProject ().getProperty ("nbm.target.dir");
    if (rootDir != null && !rootDir.equals ("")) { 
        file = new File (rootDir, this.file.getName ());
    } else {
        file = this.file;
    }

	// If desired, override the license and/or URL. //
        overrideURLIfNeeded() ;
	overrideLicenseIfNeeded() ;
        
        
        moduleAttributes = new ArrayList<Attributes> ();
        moduleAttributes.add(getModuleAttributesForLocale(""));
        for (String locale : locales) {
            Attributes a = getModuleAttributesForLocale(locale);
            if (a != null) moduleAttributes.add(a);
        }

        File module = new File( productDir, moduleName );
        // Will create a file Info/info.xml to be stored in tmp
        if (module != null) {
            // The normal case; read attributes from its manifest and maybe bundle.
            long mMod = module.lastModified();
            if (mostRecentInput < mMod) mostRecentInput = mMod;
        }

        if (mostRecentInput < file.lastModified()) {
            log("Skipping NBM creation as most recent input is younger: " + mostRecentInput + " than the target file: " + file.lastModified(), Project.MSG_VERBOSE);
            return;
        } else {
            log("Most recent input: " + mostRecentInput + " file: " + file.lastModified(), Project.MSG_DEBUG);
        }
        
        ArrayList<ZipFileSet> infoXMLFileSets = new ArrayList<ZipFileSet>();
        for (Attributes modAttr : moduleAttributes) {
            Document infoXmlContents = createInfoXml(modAttr);
            File infofile;
            String loc = modAttr.getValue("locale");
            if (loc == null)
                throw new BuildException("Found attributes without assigned locale code", getLocation());
            try {
                infofile = File.createTempFile("info_"+loc,".xml");
                OutputStream infoStream = new FileOutputStream (infofile);
                try {
                    XMLUtil.write(infoXmlContents, infoStream);
                } finally {
                    infoStream.close ();
                }
            } catch (IOException e) {
                throw new BuildException("exception when creating Info/info.xml for locale '"+loc+"'", e, getLocation());
            }
            infofile.deleteOnExit();
            ZipFileSet infoXML = new ZipFileSet();
            infoXML.setFile( infofile );
            if (loc.equals("")) {
                infoXML.setFullpath("Info/info.xml");
            } else {
                infoXML.setFullpath("Info/locale/info_"+loc+".xml");
                log("Adding Info/locale/info_"+loc+".xml file", Project.MSG_VERBOSE);
            }
            infoXMLFileSets.add(infoXML);
        }
        String codename = englishAttr.getValue("OpenIDE-Module");
        if (codename == null)
 	    new BuildException( "Can't get codenamebase" );
 	
 	UpdateTracking tracking = new UpdateTracking(productDir.getAbsolutePath());
 	String files[] = tracking.getListOfNBM( codename );
 	ZipFileSet fs = new ZipFileSet();
 	fs.setDir( productDir );
 	for (int i=0; i < files.length; i++)
 	    fs.createInclude().setName( files[i] );
 	fs.setPrefix("netbeans/");

	// JAR it all up together.
	long jarModified = file.lastModified (); // may be 0
	//log ("Ensuring existence of NBM file " + file);
	Jar jar = (Jar) getProject().createTask("jar");
    
        jar.setDestFile(file);
        jar.addZipfileset(fs);
        for (ZipFileSet zfs : infoXMLFileSets) {
            jar.addFileset(zfs);
        }

        if (main != null) { // Add the main dir
            main.setPrefix("main"); // use main prefix
            jar.addZipfileset(main);
        }
            
        jar.setCompress(true);
	jar.setLocation(getLocation());
	jar.init ();
	jar.execute ();

	// Print messages if we overrode anything. //
	if( file.lastModified () != jarModified) {
	  if( overrideLicense()) {
	    log( "Overriding license with: " + getLicenseOverride()) ;
	  }
	  if( overrideURL()) {
	    log( "Overriding homepage URL with: " + getURLOverride()) ;
	  }
	}

	// Maybe sign it.
	if (signature != null && file.lastModified () != jarModified) {
	    if (signature.keystore == null)
		throw new BuildException ("must define keystore attribute on <signature/>");
	    if (signature.storepass == null)
		throw new BuildException ("must define storepass attribute on <signature/>");
	    if (signature.alias == null)
		throw new BuildException ("must define alias attribute on <signature/>");
            if (signature.storepass.equals ("?") || signature.storepass.indexOf("${") != -1 || !signature.keystore.exists()) {
                log ("Not signing NBM file " + file + "; no stored-key password provided or keystore (" 
		     + signature.keystore.toString() + ") doesn't exist", Project.MSG_WARN);
            } else {
                log ("Signing NBM file " + file);
                SignJar signjar = (SignJar) getProject().createTask("signjar");
                try { // Signatures changed in various Ant versions.
                    try {
                        SignJar.class.getMethod("setKeystore", File.class).invoke(signjar, signature.keystore);
                    } catch (NoSuchMethodException x) {
                        SignJar.class.getMethod("setKeystore", String.class).invoke(signjar, signature.keystore.getAbsolutePath());
                    }
                    try {
                        SignJar.class.getMethod("setJar", File.class).invoke(signjar, file);
                    } catch (NoSuchMethodException x) {
                        SignJar.class.getMethod("setJar", String.class).invoke(signjar, file.getAbsolutePath());
                    }
                } catch (BuildException x) {
                    throw x;
                } catch (Exception x) {
                    throw new BuildException(x);
                }
                signjar.setStorepass (signature.storepass);
                signjar.setAlias (signature.alias);
                signjar.setLocation(getLocation());
                signjar.setMaxmemory(this.jarSignerMaxMemory);
                signjar.init ();
                signjar.execute ();
            }
	}
    }

    private Document createInfoXml(final Attributes attr) throws BuildException {
        DOMImplementation domimpl;
        try {
            domimpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException x) {
            throw new BuildException(x, getLocation());
        }
        String loc = attr.getValue("locale");
        if (loc == null) {
            throw new BuildException("Got module attributes for undefined locale", getLocation());
        } else {
            log("Creating info.xml from module attributes for locale '"+loc+"'", Project.MSG_VERBOSE);
        }
        
        String pub, sys;
        if (attr.getValue("AutoUpdate-Show-In-Client") != null || attr.getValue("AutoUpdate-Essential-Module") != null) {
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.5//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd";
        } else if (targetcluster != null && !("".equals(targetcluster))) {
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.4//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_4.dtd";
        } else {
            // #74866: no need for targetcluster, so keep compat w/ 5.0 AU.
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.3//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_3.dtd";
        }
        Document doc = domimpl.createDocument(null, "module", domimpl.createDocumentType("module", pub, sys));
        String codenamebase = attr.getValue("OpenIDE-Module");
        if (codenamebase == null) {
            Iterator it = attr.keySet().iterator();
            Name key; String val;
            while (it.hasNext()) {
                key = (Name) it.next();
                val = attr.getValue(key);
                log(key+" is '"+val+"'", Project.MSG_VERBOSE);
            }
            throw new BuildException("invalid manifest, does not contain OpenIDE-Module", getLocation());
        }
        // Strip major release number if any.
        int idx = codenamebase.lastIndexOf('/');
        if (idx != -1) codenamebase = codenamebase.substring(0, idx);
        Element module = doc.getDocumentElement();
        module.setAttribute("codenamebase", codenamebase);
        if (homepage != null) {
            module.setAttribute("homepage", homepage);
        }
        if (distribution != null) {
            module.setAttribute("distribution", distribution);
        } else {
            throw new BuildException("NBM distribution URL is not set", getLocation());
        }
        // Here we only write a name for the license.
        if (license != null) {
            String name = license.getName();
            if (name == null) {
                throw new BuildException("Every license must have a name or file attribute", getLocation());
            }
            module.setAttribute("license", name);
        }
        module.setAttribute("downloadsize", "0");
        if (needsrestart != null) {
            module.setAttribute("needsrestart", needsrestart);
        }
        if (global != null && !("".equals(global))) {
            module.setAttribute("global", global);
        }
        if (targetcluster != null && !("".equals(targetcluster))) {
            module.setAttribute("targetcluster", targetcluster);
        }
        if (moduleauthor != null) {
            module.setAttribute("moduleauthor", moduleauthor);
        }
        if (releasedate == null || "".equals(releasedate)) {
            // if date is null, set today
            releasedate = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
        }
        module.setAttribute("releasedate", releasedate);
        if (description != null) {
            module.appendChild(doc.createElement("description")).appendChild(description.getTextNode(doc));
        }
        if (notification != null) {
            module.appendChild(doc.createElement("module_notification")).appendChild(notification.getTextNode(doc));
        }
        if (externalPackages != null) {
            Iterator<ExternalPackage> exp = externalPackages.iterator();
            while (exp.hasNext()) {
                ExternalPackage externalPackage = exp.next();
                if (externalPackage.name == null ||
                        externalPackage.targetName == null ||
                        externalPackage.startUrl == null)
                    throw new BuildException("Must define name, targetname, starturl for external package");
                Element el = doc.createElement("external_package");
                el.setAttribute("name", externalPackage.name);
                el.setAttribute("target_name", externalPackage.targetName);
                el.setAttribute("start_url", externalPackage.startUrl);
                if (externalPackage.description != null) {
                    el.setAttribute("description", externalPackage.description);
                }
                module.appendChild(el);
            }
        }
        // Write manifest attributes.
        Element el = doc.createElement("manifest");
        List<String> attrNames = new ArrayList<String>(attr.size());
        Iterator it = attr.keySet().iterator();
        while (it.hasNext()) {
            attrNames.add(((Attributes.Name)it.next()).toString());
        }
        Collections.sort(attrNames);
        it = attrNames.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            // Ignore irrelevant attributes (cf. www/www/dtds/autoupdate-catalog-*.dtd
            //  and www/www/dtds/autoupdate-info-*.dtd):
            // XXX better would be to enumerate the attrs it *does* recognize!
            if (!name.startsWith("OpenIDE-Module") && !name.startsWith("AutoUpdate-")) continue;
            if (name.equals("OpenIDE-Module-Localizing-Bundle")) continue;
            if (name.equals("OpenIDE-Module-Install")) continue;
            if (name.equals("OpenIDE-Module-Layer")) continue;
            if (name.equals("OpenIDE-Module-Description")) continue;
            if (name.equals("OpenIDE-Module-Package-Dependency-Message")) continue;
            if (name.equals("OpenIDE-Module-Public-Packages")) continue;
            if (name.equals("OpenIDE-Module-Friends")) continue;
            el.setAttribute(name, attr.getValue(name));
        }
        module.appendChild(el);
        // Maybe write out license text.
        if (license != null) {
            el = doc.createElement("license");
            el.setAttribute("name", license.getName());
            el.appendChild(license.getTextNode(doc));
            module.appendChild(el);
        }
        return doc;
    }
   

  /** This returns true if the license should be overridden. */
  protected boolean overrideLicense() {
    return( getLicenseOverride() != null) ;
  }

  /** Get the license to use if the license should be overridden,
   *  otherwise return null.
   */
  protected String getLicenseOverride() {
    String s = getProject().getProperty( "makenbm.override.license") ;
    if( s != null) {
      if( s.equals( "")) {
	s = null ;
      }
    }
    return( s) ;
  }

  /** This returns true if the homepage URL should be overridden. */
  protected boolean overrideURL() {
    return( getURLOverride() != null) ;
  }

  /** Get the homepage URL to use if it should be overridden,
   *  otherwise return null.
   */
  protected String getURLOverride() {
    String s = getProject().getProperty( "makenbm.override.url") ;
    if( s != null) {
      if( s.equals( "")) {
	s = null ;
      }
    }
    return( s) ;
  }

  /** If required, this will create a new license using the override
   *  license file.
   */
  protected void overrideLicenseIfNeeded() {
    if( overrideLicense()) {
      license = new Blurb() ;
      license.setFile( getProject().resolveFile( getLicenseOverride())) ;
    }
  }

  /** If required, this will set the homepage URL using the
   *  override value.
   */
  protected void overrideURLIfNeeded() {
    if( overrideURL()) {
      homepage = getURLOverride() ;
    }
  }

}
