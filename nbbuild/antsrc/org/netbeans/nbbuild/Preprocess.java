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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;

// In Javadoc below: &#42; == *
/** Preprocesses content of file replacing all conditional blocks.
* <PRE>
* /&#42;nbif someswitch
* // PART A
* nbelse&#42;/
* // PART B
* /&#42;nbend&#42;/
* </PRE>
* If <code>someswitch</code> is off, nothing will be changed, so part A will be
* commented-out and part B will be active. If <code>someswitch</code> is on,
* the code will be changed to:
* <PRE>
* /&#42;nbif someswitch&#42;/
* // PART A
* /&#42;nbelse
* // PART B
* /&#42;nbend&#42;/
* </PRE>
* So that part A is active and part B is commented-out.
* <p>You can also use a block without an else:
* <PRE>
* /&#42;nbif someswitch
* // PART A
* /&#42;nbend&#42;/
* </PRE>
* With the switch off, it will again be left as is, i.e. commented-out. With the switch
* on, you will get:
* <PRE>
* /&#42;nbif someswitch&#42;/
* // PART A
* /&#42;nbend&#42;/
* </PRE>
* where the interior section is now active.
* <p>Intent of this preprocessor is to permit incompatible API changes to made in source
* code, while creating a variant binary compatibility kit without the changes or with
* more conservative changes. It should <em>not</em> be used as a general-purpose Java
* preprocessor, we are not C++ programmers here!
* @author Jaroslav Tulach, Jesse Glick
* @deprecated No longer used.
*/
@Deprecated
public class Preprocess extends MatchingTask {
    /** the format of begining of substitution */
    private static final String F_BEGIN = "/*nbif"; // NOI18N
    /** format of else statement */
    private static final String F_ELSE = "nbelse*/"; // NOI18N
    /** how to replace the else */
    private static final String R_ELSE = "/*nbelse"; // NOI18N
    /** format of end statement */
    private static final String F_END = "/*nbend*/"; // NOI18N
    
    
    
    /** source directory to scan files from */
    private File src;
    /** target directory */
    private File dest;
    /** copy all/copy modified */
    private boolean copyAll = false;
    /** switches to check in conditionals */
    private List<Switch> switches = new LinkedList<Switch> ();
    
    /** Setter for the source directory to scan. */
    public void setSrcDir (File f) {
        src = f;
    }
    
    /** Setter for the target directory to create processed files in.
    */
    public void setDestDir (File f) {
        dest = f;
    }
    
    /** Can be set to copy all files, if necessary.
     * Default is not to copy a file unless it was actually modified.
    * @param copyAll true if all files (even unmodified) should be copied
    */
    public void setCopyAll (boolean copyAll) {
        this.copyAll = copyAll;
    }

    /** A switch to use as the test in preprocessor conditionals.
     * Only switches explicitly listed here will be recognized.
     */
    public class Switch {
        String name;
        boolean on;
        /** Set the name of the switch, which will be used in <code>nbif</code> tests. */
        public void setName (String name) {
            this.name = name;
        }
        /** Set whether the switch should be on or not. */
        public void setOn (boolean on) {
            this.on = on;
        }
    }

    /** Add a conditional switch to control preprocessing. */
    public Switch createSwitch () {
        Switch s = new Switch ();
        switches.add (s);
        return s;
    }
    
    public void execute () throws BuildException {
        if (src == null || dest == null) {
            throw new BuildException ("src and dest must be specified");
        }
        if (switches.isEmpty ()) {
            throw new BuildException ("Useless to preprocess sources with no switches specified!");
        }
        
        DirectoryScanner scanner = getDirectoryScanner (src);
        scanner.scan ();
        String[] files = scanner.getIncludedFiles ();
        String message1 = "Processing " + files.length + 
            " file(s) from directory " + src + " to " + dest;
        
        StringBuffer message2 = new StringBuffer ("Switches:");
        Set<String> ss = new HashSet<String> ();
        for (Switch s: switches) {
            if (s.on) {
                ss.add (s.name);
                message2.append (' ');
                message2.append (s.name);
            } else {
                message2.append (" !");
                message2.append (s.name);
            }
        }
        
        try {
            boolean shownMessages = false;
            for (int i = 0; i < files.length; i++) {
                File src = new File (this.src, files[i]);
                File dest = new File (this.dest, files[i]);
                // Up-to-date check (note that this ignores changes in
                // switches; for that you must clean first!):
                if (dest.exists () && dest.lastModified () >= src.lastModified ()) {
                    continue;
                }

                int size = (int)src.length () + 500;
                BufferedReader r = new BufferedReader (
                    new FileReader (src), size
                );
                StringWriter w = new StringWriter (size);

                boolean modified = replace (r, w, ss);
                w.close ();
                r.close ();

                if ((modified || copyAll) && ! shownMessages) {
                    shownMessages = true;
                    log (message1);
                    log (message2.toString ());
                }
                
                if (modified) {
                    log ("Modified: " + files[i]);
                }

                if (modified || copyAll) {
                    // the file has been modified
                    
                    // ensure the directories exists
                    File dir = dest.getParentFile ();
                    dir.mkdirs ();
                    
                    Writer file = new FileWriter (dest);
                    file.write (w.getBuffer().toString());
                    file.close ();
                }
            }
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    
    
    
/*nbif test
    public static void main (String[] args) throws IOException {
        BufferedReader r = new BufferedReader (
            new FileReader (args[0])
        );
        
        BufferedWriter w = new BufferedWriter (
            new OutputStreamWriter (System.out), System.getProperties ()
        );
        
        replace (r, w);
        
        w.close ();
    }
/*nbend*/
    
    
    
    
    /** Reads a content of a file and produces replaces given lines
     *
     * @param r reader to read from
     * @param w writer to write to
     * @param props properties to be considered on
     * @return true if the content of r has been modified
     */
    @SuppressWarnings("fallthrough")
    private static boolean replace (
        BufferedReader r, Writer w, Set props // Set<String>
    ) throws IOException {
        boolean modified = false;
        
        int state = 0;
        
        for (;;) {
            String line = r.readLine ();
            if (line == null) {
                return modified;
            }
            
            switch (state) {
                case 0: // regular text
                    if (line.trim ().startsWith (F_BEGIN)) {
                        String rest = line.trim ().substring(F_BEGIN.length ()).trim ();
                        if (props.contains (rest)) {
                            // successful test, the content of line should
                            // be included
                            line += "*/"; // NOI18N
                            modified = true;
                            state = 1;
                        }
                    }
                    break;
                case 1: // waiting for the nbelse*/ statement
                    if (line.trim ().equals (F_ELSE)) {
                        line = R_ELSE;
                        modified = true; // redundant, for clarity
                        state = 2;
                    }
                    // FALLTHROUGH: OK to have a if-end block with no else!
                case 2: // inside the else, waiting for end
                    if (line.trim ().equals (F_END)) {
                        state = 0;
                    }
                    break;
            }
            
            w.write (line);
            w.write ('\n');
        }
    }
}
