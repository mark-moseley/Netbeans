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

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/** Check source files for a license notice.
 * @author Jesse Glick
 */
public class CheckLicense extends Task {

    private final List filesets = new ArrayList (1); // List<FileSet>
    private String fragment;
    private List fragments; // List<Fragment>
    private FailType fail;

    /** Add a file set of source files to check.
     * @param fs set of files to check licenses of
     */
    public void addFileSet (FileSet fs) {
        filesets.add (fs);
    }
    
    /** Add a file set of CVS-controlled source files to check.
     * @param fs set of files to check licenses of
     */
    public void addCvsFileSet(CvsFileSet fs) {
        filesets.add(fs);
    }

    /** Set the fragment of license notice which is expected
     * to be found in each source file.
     * @param f the fragment
     */
    public void setFragment (String f) {
        fragment = f;
    }
    
    public void setFail(FailType t) {
        fail = t;
    }
    
    public Convert createConvert() {
        Convert f = new Convert();
        if (fragments == null) {
            fragments = new ArrayList();
        }
        
        fragments.add(f);
        return f;
    }

    public void execute () throws BuildException {
        if (fragment == null) {
            if (fragments == null) {
                throw new BuildException("You must supply a fragment", getLocation());
            }
            
            executeReplace();
            return;
        }
        if (filesets.isEmpty ()) throw new BuildException("You must supply at least one fileset", getLocation());
        Iterator it = filesets.iterator ();
        String failMsg = null;
        try {
            while (it.hasNext ()) {
                FileScanner scanner = ((FileSet) it.next()).getDirectoryScanner(getProject());
                File baseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                log ("Looking for " + fragment + " in " + files.length + " files in " + baseDir.getAbsolutePath ());
                for (int i = 0; i < files.length; i++) {
                    File f = new File (baseDir, files[i]);
                    //log("Scanning " + f, Project.MSG_VERBOSE);
                    BufferedReader br = new BufferedReader (new FileReader (f));
                    try {
                        String line;
                        while ((line = br.readLine ()) != null) {
                            if (line.indexOf (fragment) != -1) {
                                // Found it.
                                if (fail != null && "whenpresent".equals(fail.getValue())) {
                                    if (failMsg != null) {
                                        log(failMsg, Project.MSG_ERR);
                                    }
                                    failMsg = "License found in " + f;
                                }
                                break;
                            }
                        }
                        if (line == null) {
                            String msg = f.getAbsolutePath () + ":1: no license notice found";
                            if (fail != null && "whenmissing".equals(fail.getValue())) {
                                throw new BuildException(msg);
                            }
                            if (fail == null) {
                                // Scanned whole file without finding it.
                                log (msg, Project.MSG_ERR);
                            }
                        }
                    } finally {
                        br.close ();
                    }
                }
            }
            
            if (failMsg != null) {
                throw new BuildException(failMsg);
            }
            
        } catch (IOException ioe) {
            throw new BuildException("Could not open files to check licenses", ioe, getLocation());
        }
    }
    
    private void executeReplace() throws BuildException {
        Iterator it = filesets.iterator ();
        try {
            byte[] workingArray = new byte[1024];
            while (it.hasNext ()) {
                FileScanner scanner = ((FileSet) it.next()).getDirectoryScanner(getProject());
                File baseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                log ("Replacing code in " + files.length + " files in " + baseDir.getAbsolutePath ());
                for (int i = 0; i < files.length; i++) {
                    File file = new File (baseDir, files[i]);
                    log("Processing " + file, Project.MSG_VERBOSE);
                    FileInputStream is = new FileInputStream(file);
                    int workingLength = is.read(workingArray);
                    if (workingLength == -1) {
                        continue;
                    }
                    String workingString = new String(workingArray, 0, workingLength);
                    boolean changed = false;
                    String prefix = null;
                    
                    Iterator frags = fragments.iterator();
                    while (frags.hasNext()) {
                        Convert f = (Convert)frags.next();
                        
                        Matcher matcher = f.orig.matcher(workingString);
                        
                        while (matcher.find()) {
                            if (f.prefix) {
                                if (prefix != null) {
                                    throw new BuildException("Only one convert element can be prefix!");
                                }
                                if (matcher.groupCount() != 1) {
                                    throw new BuildException("There should be one group for the prefix element. Was: " + matcher.groupCount());
                                }
                                prefix = matcher.group(1);
                            }
                            
                            String before = workingString.substring(0, matcher.start());
                            String after = workingString.substring(matcher.end());
                            String middle = wrapWithPrefix(f.repl, prefix, before.length() == 0 || before.endsWith("\n"));

                            if (!middle.equals(matcher.group(0))) {
                                workingString = before + middle + after;
                                log("Matched " + middle, Project.MSG_VERBOSE);
                                changed = true;
                            } else {
                                log("Matched, but no change: " + middle, Project.MSG_VERBOSE);
                            }
                            
                            if (!f.all) {
                                break;
                            } else {
                                matcher = f.orig.matcher(workingString);
                            }
                        }
                    }
                    
                    byte[] rest = null;
                    if (is.available() > 0 && changed) {
                        rest = new byte[is.available()];
                        int read = is.read(rest);
                        assert read == rest.length;
                    }
                    
                    is.close();

                    
                    if (changed) {
                        log ("Rewriting " + file);
                        FileOutputStream os = new FileOutputStream(file);
                        workingString = Pattern.compile(" +$", Pattern.MULTILINE).matcher(workingString+"X").replaceAll("");
                        os.write(workingString.substring(0, workingString.length() - 1).getBytes());
                        if (rest != null) {
                            os.write(rest);
                        }
                        os.close();
                    }
                }
            }
        } catch (IOException ioe) {
            throw new BuildException("Could not open files to check licenses", ioe, getLocation());
        }
    }

    private String wrapWithPrefix(String repl, String prefix, boolean startWithPrefix) {
        if (prefix == null) {
            return repl;
        }
        
        String[] all = repl.split("\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < all.length; i++) {
            if (startWithPrefix) {
                sb.append(prefix);
            }
            sb.append(all[i]);
            if (i < all.length - 1) {
                sb.append('\n');
            }
            startWithPrefix = true;
        }
        
        return sb.toString();
    }

    
    public static final class Convert {
        Pattern orig;
        String repl;
        boolean prefix;
        boolean all;
        
        public void setToken(String orig) {
            this.orig = Pattern.compile(orig, Pattern.DOTALL | Pattern.MULTILINE);
        }
        public void setReplace(String repl) {
            this.repl = repl.replace("\\n", "\n").replace("\\t", "\t");
        }
        public void setPrefix(boolean b) {
            prefix = b;
        }
        public void setReplaceAll(boolean b) {
            all = b;
        }
        public Line createLine() {
            return new Line();
        }
        
        public final class Line {
            public void setText(String t) {
                if (repl == null) {
                    repl = t;
                } else {
                    repl = repl + "\n" + t;
                }
            }
        }
    }
    public static final class FailType extends org.apache.tools.ant.types.EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { 
                "whenmissing",
                "whenpresent",
            };
        }
    }
}
