/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.apache.regexp.*;
import org.apache.tools.ant.types.Mapper;

// [PENDING] would be nice to have line numbers reported in output;
// not clear what the best way to do that is without introducing
// overhead; maybe wrapper InputStream that counts lines?
// [PENDING] checking external links needs to go thru HTTP proxy somehow

/** Task to check for broken links in HTML.
 * Note that this is a matching task and you must give it a list of things to match.
 * @author Jesse Glick
 */
public class CheckLinks extends MatchingTask {

    private File basedir;
    private boolean checkexternal = true;
    private List mappers = new LinkedList(); // List<Mapper>

    /** Set whether to check external links (absolute URLs).
     * Local relative links are always checked.
     * By default, external links are checked.
     */
    public void setCheckexternal (boolean ce) {
        checkexternal = ce;
    }

    /** Set the base directory from which to scan files.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }

    /**
     * Add a mapper to translate file names to the "originals".
     */
    public Mapper createMapper() {
        Mapper m = new Mapper(getProject());
        mappers.add(m);
        return m;
    }

    public void execute () throws BuildException {
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String message = "Scanning for broken links in " + basedir + " ...";
        if (! checkexternal) message += " (external URLs will be skipped)";
        log (message);
        String[] files = scanner.getIncludedFiles ();
        // Set of known-good URLs (including all anchored variants etc.).
        Set okurls = new HashSet (1000); // Set<URL>
        // Set of known-bad URLs.
        Set badurls = new HashSet (100); // Set<URL>
        // Set of parsed base HTML URLs known to have had their contents checked.
        Set cleanurls = new HashSet(100); // Set<URL>
        for (int i = 0; i < files.length; i++) {
            File file = new File (basedir, files[i]);
            URL fileurl;
            try {
                fileurl = file.toURL ();
            } catch (MalformedURLException mfue) {
                throw new BuildException (mfue, location);
            }
            log ("Scanning " + file, Project.MSG_VERBOSE);
            try {
                scan(this, file.getAbsolutePath(), fileurl, okurls, badurls, cleanurls, checkexternal, 1, mappers);
            } catch (IOException ioe) {
                throw new BuildException ("Could not scan " + file + ": " + ioe, ioe, location);
            }
        }
    }
    
    static RE hrefOrAnchor;
    static {
        try {
            hrefOrAnchor = new RE("<(a|img)(\\s+shape=\"rect\")?\\s+(href|name|src)=\"([^\"#]*)(#[^\"]+)?\"(\\s+shape=\"rect\")?>");
        } catch (RESyntaxException rese) {
            throw new Error (rese.toString());
        }
        hrefOrAnchor.setMatchFlags (RE.MATCH_CASEINDEPENDENT);
    }
    
    // recurse:
    // 0 - just check that it can be opened
    // 1 - check also that any links from it can be opened
    // 2 - recurse
    public static void scan(Task task, String referrer, URL u, Set okurls, Set badurls, Set cleanurls, boolean checkexternal, int recurse, List mappers) throws IOException {
        //task.log("scan: u=" + u + " referrer=" + referrer + " okurls=" + okurls + " badurls=" + badurls + " cleanurls=" + cleanurls + " recurse=" + recurse, Project.MSG_DEBUG);
        if (okurls.contains(u) && recurse == 0) {
            // Yes it is OK.
            return;
        }
        String b = u.toString();
        int i = b.lastIndexOf('#');
        if (i != -1) {
            b = b.substring(0, i);
        }
        URL base = new URL(b);
        String frag = u.getRef();
        //task.log("scan: base=" + base + " frag=" + frag, Project.MSG_DEBUG);
        if (badurls.contains(u) || badurls.contains(base)) {
            task.log(normalize(referrer, mappers) + ": broken link (already reported): " + u, Project.MSG_WARN);
            return;
        }
        if (! checkexternal && ! "file".equals(u.getProtocol())) {
            task.log("Skipping external link: " + base, Project.MSG_VERBOSE);
            cleanurls.add(base);
            okurls.add(base);
            okurls.add(u);
            return;
        }
        task.log("Checking " + u + " (recursion level " + recurse + ")", Project.MSG_VERBOSE);
        InputStream rd;
        String mimeType;
        try {
            URLConnection conn = base.openConnection ();
            conn.connect ();
            mimeType = conn.getContentType ();
            rd = conn.getInputStream ();
        } catch (IOException ioe) {
            task.log(normalize(referrer, mappers) + ": broken link: " + base, Project.MSG_WARN);
            badurls.add(base);
            badurls.add(u);
            return;
        }
        okurls.add(base);
        Set others = null; // Set<URL>
        if (recurse > 0 && cleanurls.add(base)) {
            others = new HashSet(100);
        }
        try {
            if (recurse == 0 && frag == null) {
                // That is all we wanted to check.
                return;
            }
            if ("text/html".equals(mimeType)) {
                task.log("Parsing " + base, Project.MSG_VERBOSE);
                CharacterIterator it = new StreamCharacterIterator (rd);
                int idx = 0;
                Set names = new HashSet(100); // Set<String>
                while (hrefOrAnchor.match (it, idx)) {
                    // Advance match position past end of expression:
                    idx = hrefOrAnchor.getParenEnd (0);
                    // Get the stuff involved:
                    String type = hrefOrAnchor.getParen(3);
                    if (type.equalsIgnoreCase("name")) {
                        // We have an anchor, therefore refs to it are valid.
                        String name = unescape(hrefOrAnchor.getParen(4));
                        if (names.add(name)) {
                            okurls.add(new URL(base, "#" + name));
                        } else if (recurse == 1) {
                            task.log(normalize(referrer, mappers) + ": duplicate anchor name: " + name, Project.MSG_WARN);
                        }
                    } else {
                        // A link to some other document: href=, src=.
                        if (others != null) {
                            String otherbase = unescape(hrefOrAnchor.getParen (4));
                            String otheranchor = unescape(hrefOrAnchor.getParen (5));
                            if (!otherbase.startsWith("mailto:")) {
                                URL o = new URL(base, (otheranchor == null) ? otherbase : otherbase + otheranchor);
                                //task.log("href: " + o);
                                others.add(o);
                            }
                        } // else we are only checking that this one has right anchors
                    }
                }
            } else {
                task.log("Not checking contents of " + base, Project.MSG_VERBOSE);
            }
        } finally {
            rd.close();
        }
        if (! okurls.contains(u)) {
            task.log(normalize(referrer, mappers) + ": broken link: " + u, Project.MSG_WARN);
        }
        if (others != null) {
            Iterator it = others.iterator();
            while (it.hasNext()) {
                URL other = (URL)it.next();
                scan(task, u.getPath(), other, okurls, badurls, cleanurls, checkexternal, recurse == 1 ? 0 : 2, mappers);
            }
        }
    }
    
    private static String normalize(String path, List mappers) throws IOException {
        try {
            Iterator it = mappers.iterator();
            while (it.hasNext()) {
                Mapper m = (Mapper)it.next();
                String[] nue = m.getImplementation().mapFileName(path);
                if (nue != null) {
                    for (int i = 0; i < nue.length; i++) {
                        if (new File(nue[i]).isFile()) {
                            return nue[i];
                        }
                    }
                }
            }
            return path;
        } catch (BuildException e) {
            throw new IOException(e.toString());
        }
    }
    
    private static String unescape(String text) {
        if (text == null) {
            return null;
        }
        int pos = 0;
        int search;
        while ((search = text.indexOf('&', pos)) != -1) {
            int semi = text.indexOf(';', search + 1);
            if (semi == -1) {
                // Unterminated &... leave rest as is??
                return text;
            }
            String entity = text.substring(search + 1, semi);
            String repl;
            if (entity.equals("amp")) {
                repl = "&";
            } else if (entity.equals("quot")) {
                repl = "\"";
            } else if (entity.equals("lt")) {
                repl = "<";
            } else if (entity.equals("gt")) {
                repl = ">";
            } else if (entity.equals("apos")) {
                repl = "'";
            } else {
                // ???
                pos = semi + 1;
                continue;
            }
            text = text.substring(0, search) + repl + text.substring(semi + 1);
            pos = search + repl.length();
        }
        return text;
    }
    
}
