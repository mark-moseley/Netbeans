/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.utils;

import java.awt.Component;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.netbeans.modules.cnd.loaders.CSrcObject;
import org.netbeans.modules.cnd.loaders.CoreElfObject;
import org.netbeans.modules.cnd.loaders.MakefileDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Miscellaneous utility classes useful for the Ipe module
 */
public class IpeUtils {
    
    /** Store the real environment here */
    static private WeakReference wrEnv;
    
    /**
     * Global flag which when set, generated additional diagnostic messages
     * on standard output. Used for development purposes only.
     */
    static public final boolean IfdefDiagnostics = Boolean.getBoolean( "ifdef.debug.diagnostics"); //NOI18N
    
    /** Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static final String getDirName(String path) {
        int sep = path.lastIndexOf('/');
        if (sep == -1)
            sep = path.lastIndexOf('\\');
        if (sep != -1) {
            return path.substring(0, sep);
        }
        return null;
    }
    
    /** Same as the C library basename function: given a path, return
     * its filename.
     */
    public static final String getBaseName(String path) {
        int sep = path.lastIndexOf('/');
        if (sep == -1)
            sep = path.lastIndexOf('\\');
        if (sep != -1) {
            return path.substring(sep+1);
        }
        return path;
    }
    
    /**
     *  Given a path and a base directory, return a relative path equivalent to
     *  the the original path and relative to the base directory.
     *
     *  @param base The directory we want the returned path relative to
     *  @param path The initial path. This <B>should</B> be an absolute path
     *
     *  @return Either a relative pathname of <code>path</code> in relationship
     *  to <code>base</code> or a copy of <code>path</code>. In all cases the
     *  returned path is a NEW string.
     */
    static public final String getRelativePath(String base, String path) {
        // Convert both to canonical paths first
        File baseFile = new File(base);
        if (baseFile.exists()) {
            try {
                base = baseFile.getCanonicalPath();
            } catch (Exception e) {}
        }
        
        if (path.equals(base)) {
            return new String(path);
        } else if (path.startsWith(base + '/')) {
            // This should be the normal case...
            return new String(path.substring(base.length() + 1));
        } else if (path.startsWith(base + '\\')) {
            // This should be the normal case...
            return new String(path.substring(base.length() + 1));
        } else if (!isPathAbsolute(path)) {
            // already a relative path, return path as-is
            return new String(path);
        } else {
            // some other absolute path
            Object[] bnames = getPathNameArray(base);
            Object[] pnames = getPathNameArray(path);
            int match = 0;
            for (int i = 0; i < bnames.length && i < pnames.length; i++) {
                String bstring = new String(bnames[i].toString());
                String pstring = new String(pnames[i].toString());
                if (bstring.equals(pstring)) {
                    match++;
                } else {
                    break;
                }
            }
            
            if (match > 1 && match == pnames.length && bnames.length > pnames.length) {
                // path is a substring of
                StringBuffer s = new StringBuffer();
                for (int cnt = 0; cnt < (bnames.length - match-1); cnt++) {
                    s.append(".." + File.separator);					//NOI18N
                }
                s.append("..");					//NOI18N
                return s.toString();
            } else if (match > 1) {
                StringBuffer s = new StringBuffer();
                
                for (int cnt = bnames.length - match; cnt > 0; cnt--) {
                    s.append(".." + File.separator);					//NOI18N
                }
                for (int i = match; i < pnames.length; i++) {
                    if (s.charAt(s.length()-1) != File.separatorChar)
                        s.append(File.separator);
                    s.append(pnames[i].toString());
                }
                return s.toString();
            } else {
                return new String(path);
            }
        }
    }
    
    /*
     * From PicklistUtils
     */
    public static String toAbsolutePath(String base, String path) {
        String newPath = path;
        if (newPath == null || newPath.length() == 0)
            newPath = "."; // NOI18N
        if (!isPathAbsolute(newPath)) {
            // it is a relative path
            //RemoteUtils ru = RemoteUtils.getInstance();
            //String localBase = ru.getRemotetoLocalMapping(base);
            //String localPath = ru.getRemotetoLocalMapping(newPath);
            // newPath = localBase + File.separator + localPath;
            newPath = base + File.separator + path; // NOI18N
            File newPathFile = new File(newPath);
            if (newPathFile.exists()) {
                try {
                    newPath = newPathFile.getCanonicalPath();
                } catch (Exception e) {
                }
            }
            // newPath = ru.getLocalToRemoteMapping(newPath);
        }
        return newPath;
    }
    
    /*
     * From PicklistUtils
     */
    public static String toRelativePath(String base, String path) {
        String relPath = path;
        if (relPath == null || relPath.length() == 0)
            relPath = "."; // NOI18N
        if (isPathAbsolute(relPath)) {
            if (relPath.equals(base))
                relPath = "."; // NOI18N
            else if (isPathAbsolute(base))
                relPath = IpeUtils.getRelativePath(base, relPath);
            else
                relPath = path;
        }
        return relPath;
    }
    
    public static String toAbsoluteOrRelativePath(String base, String path) {
        String resRel = toRelativePath(base, path);
        String res;
        if (isPathAbsolute(resRel) || resRel.startsWith("..")) { // NOI18N
            res = path;
        } else {
            String dotSlash = "." + File.separatorChar; // NOI18N
            if (resRel.startsWith(dotSlash))
                res = resRel.substring(2);
            else
                res = resRel;
        }
        return res;
    }
    
    /*
     * From PicklistUtils
     */
    public static String subPath(String path, int levels) {
        if (path == null || path.length() == 0)
            return path;
        String newPath = null;
        
        Vector separators = new Vector();
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/')
                separators.add(0, new Integer(i));
            else if (path.charAt(i) == '\\')
                separators.add(0, new Integer(i));
        }
        if (separators.size() > levels) {
            newPath =  "..." + // NOI18N
		path.substring(((Integer)separators.get(levels)).intValue(), path.length());
        } else {
            newPath = path;
        }
        return newPath;
    }
    
    
    /**
     *  Compute an array of the individual path elements of a pathname.
     */
    static private final Object[] getPathNameArray(String path) {
        ArrayList l;
        int pos = 1;			    // start of a path name component
        int next;			    // position of next '/' in path
        
        l = new ArrayList();
        if (isPathAbsolute(path)) {
            while (pos > 0) {
                next = path.indexOf('/', pos);
                if (next < 0)
                    next = path.indexOf('\\', pos);
                if (next > 0) {		    // another '/' found
                    l.add(path.substring(pos, next));
                    pos = next + 1;
                } else {		    // doint the last name
                    l.add(path.substring(pos));
                    pos = -1;		    // found end-of-string
                }
            }
        }
        
        return l.toArray();
    }
    
    /**
     * Expand '~' and env variables in path.
     * Also strips off leading and trailing white space.
     *
     *	@param filename input string to be expanded
     *	@returns the expanded string
     *
     * <P>Handles:
     * <ul>
     *   <li> If '~' is the first non-white space char, then:
     *     <ul>
     *	     <li> ~	    =>	home dir
     *	      <li> ~user    =>	user's home dir
     *	      <li> \~	    =>	~/
     *     </ul>
     *
     *   <li> If the environment variable a = "foo" and b = "bar" then:
     *     <ul>
     *	     <li> $a	    =>	foo
     *	     <li> $a$b	    =>	foobar
     *	     <li> $a.c	    =>	foo.c
     *	     <li> xxx$a	    =>	xxxfoo
     *	     <li> ${a}!	    =>	foo!
     *	     <li> \$a	    =>	$a
     *     </ul>
     * </ul>
     */
    public static String expandPath(String filename) {
        int si = 0; // Index into 'source' (filename)
        int max = filename.length(); // Length of filename
        int beginIndex;
        int endIndex;
        StringBuffer dp = new StringBuffer(256); // Result buffer
        
        // Skip leading whitespace
        while (si < max && Character.isSpaceChar(filename.charAt(si))) {
            si++;
        }
        
        // Expand ~ and ~user
        if (si < max && filename.charAt(si) == '~') {
            if (si++ < max && (si == max || filename.charAt(si) == '/')) {
                // ~/filename
                dp.append(System.getProperty("user.home"));    // NOI18N
            } else { // ~user/filename
                /*
                // Cannot do this in cnd context
                PasswordEntry pent = new PasswordEntry();
                beginIndex = si;
                while (si < max && filename.charAt(si) != '/') {
                    si++;
                }
                 
                if (pent.fillFor(filename.substring(beginIndex, si))) {
                    dp.append(pent.getHomeDirectory());
                } else {
                    // lookup failed - use raw string
                    dp.append(filename.substring(beginIndex, si));
                }
                 */
            }
        }
        
        /* Expand inline environment variables */
        while (si < max) {
            char c = filename.charAt(si++);
            if (c == '\\' && si < max) {
                if (filename.charAt(si) == '$') {
                    // Don't try and expand it as an environment
                    // variable. It is being escaped
                    dp.append('\\');
                    dp.append('$');
                    si++;			// skip over the '$'
                } else {
                    // Don't loose the escaped character
                    dp.append(c);
                }
            } else if (c == '$' && si < max && filename.charAt(si) == '(') {
                // A Make variable
                endIndex = filename.indexOf(')', si);
                dp.append('$');
                if (endIndex > -1) {
                    dp.append(filename.substring(si, endIndex));
                    si = endIndex;
                } else {
                    // this is probably an error but we just pass it through
                    dp.append(filename.substring(si));
                    si = max;
                }
            } else if (c == '$' && si < max) {
                // An environment variable!
                boolean braces = (filename.charAt(si) == '{');
                
                if (braces) { // skip over left brace
                    si++;
                }
                
                // Find end of environment variable
                beginIndex = si;
                while (si < max) {
                    char c2 = filename.charAt(si);
                    if (braces && c2 == '}') {
                        break;
                    }
                    if (!(Character.isLetterOrDigit(c2) || (c2 == '_'))) {
                        break;
                    }
                    si++;
                }
                
                endIndex = si;
                if ((si < max) && braces) {
                    si++; // skip over right brace
                }
                
                if (endIndex > beginIndex) {
                    String value = IpeUtils.getenv(
                            filename.substring(beginIndex, endIndex));
                    
                    if (value != null) {
                        dp.append(value);
                    } else {
                        // Bad/unknown env variable: Put it back in
                        // the string (it might be a filename)
                        dp.append('$');
                        if (braces) {
                            dp.append('{');
                        }
                        dp.append(filename.substring(beginIndex, endIndex));
                        if (braces) {
                            dp.append('}');
                        }
                    }
                } else {
                    // Empty string
                    dp.append('$');
                    if (braces) {
                        dp.append("{}");				//NOI18N
                    }
                }
            } else {
                // Just add the character
                dp.append(c);
            }
        }
        
        return dp.toString();
    }
    
    
    /** Get the value of an environment variable */
    public static String getenv(String name) {
        return getUnixEnv().getenv(name);
    }
    
    
    /** Same as getenv() */
    public static String valueOf(String name) {
        return IpeUtils.getenv(name);
    }
    
    
    /** Returns the whole name=value string */
    public static String entryFor(String name) {
        return getUnixEnv().entryFor(name);
    }
    
    
    /** Put the environment variable into the environment */
    public static void putenv(String entry) {
        getUnixEnv().putenv(entry);
    }
    
    
    /** Similar to putenv but takes separate arguments for name and value */
    public static void setValueOf(String name, String value) {
        getUnixEnv().setValueOf(name, value);
    }
    
    
    /** Return the whole environment in an array of Strings */
    public static String[] environ() {
        return getUnixEnv().environ();
    }
    
    
    /** The UnixEnv.dump() method */
    public static void envDump() {
        getUnixEnv().dump();
    }
    
    private static UnixEnv getUnixEnv() {
        UnixEnv env;
        
        if (wrEnv == null) {
            env = null;
        } else {
            env = (UnixEnv) wrEnv.get();
        }
        
        if (env == null) {
            env = new UnixEnv();
            wrEnv = new WeakReference(env);
        }
        return env;
    }
    
    /** Trim trailing slashes */
    public static String trimSlashes(String dir) {
        int trim = 0;
        
        int i = dir.length();
        while (i > 0 && (dir.charAt(i - 1) == '/' || dir.charAt(i - 1) == '\\')) {
            trim++;
            i--;
        }
        if (trim > 0) {
            return dir.substring(0, dir.length() - trim);
        } else {
            return dir;
        }
    }
    
    /** Trim surrounding white space and trailing slashes */
    public static String trimpath(String dir) {
        return trimSlashes(dir.trim());
    }
    
    // Utility to request focus for a component by using the
    // swing utilities to invoke it at a later
    public static void requestFocus(final Component c) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (c != null) {
                    if (c.getParent() != null) {
                        try {
                            c.requestFocus();
                        } catch (NullPointerException npe) {
                            // Throw away the npe. This is probably due to
                            // the parent of this component not existing
                            // before we're through processing the
                            // requestFocus() call. This can happen when
                            // quickly clicking through a wizard.
                        }
                    }
                }
            }
        });
    }
    
    // Utility to request focus for a component by using the
    // swing utilities to invoke it at a later
    public static void setDefaultButton(final JRootPane rootPane, final JButton button) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (button != null) {
                    if (button.getParent() != null && button.isVisible()) {
                        try {
                            rootPane.setDefaultButton(button);
                        } catch (NullPointerException npe) {
                            // Throw away the npe. This is probably due to
                            // the parent of this component not existing
                            // before we're through processing the
                            // requestFocus() call. This can happen when
                            // quickly clicking through a wizard.
                        }
                    }
                }
            }
        });
    }
    
    /** Add quotes around the string if necessary.
     * This is the case when the string contains space or meta characters.
     * For now, we only worry about space, tab, *, [, ], ., ( and )
     */
    public static final String quoteIfNecessary(String s) {
        int n = s.length();
        if (n == 0) {
            // Don't quote empty strings ("")
            return s;
        }
        // A quoted string in the first place?
        if ((s.charAt(0) == '"') ||
                (s.charAt(n-1) == '"')) {
            return s;
        }
        
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') || (c == '*') ||
                    (c == '[') || (c == ']') ||
                    (c == '(') || (c == ')')) {
                // Contains some kind of meta character == so quote the
                // darn thing
                return '"' + s + '"'; // NOI18N
            }
        }
        return s;
    }
    
    public static boolean isPathAbsolute(String path) {
        if (path == null || path.length() == 0)
            return false;
        else if (path.charAt(0) == '/')
            return true;
        else if (path.charAt(0) == '\\')
            return true;
        else if (path.indexOf(':') > 0)
            return true;
        else
            return false;
    }
    
    public static Node findNode(String filePath) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath)));
        if (fo == null)
            return null; // FIXUP
        
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (Exception e) {
            // FIXUP
        }
        if (dataObject == null)
            return null; // FIXUP
        
        Node node = dataObject.getNodeDelegate();
        if (node == null)
            return null; // FIXUP
        
        return node;
    }
    
    public static DataNode findCorefileNode(String filePath) {
        if (filePath == null)
            return null;
        Node node = findNode(filePath);
        if (node == null)
            return null;
        if (!(node instanceof DataNode))
            return null;
        DataObject dataObject = (DataObject)node.getCookie(DataObject.class);
        if (!(dataObject instanceof CoreElfObject))
            return null;
        
        return (DataNode)node;
    }
    
    // From PicklistUtils. FIXUP: probably not really needed anymore
    public static ExecutionSupport findExecutionSupport(DataNode executionNode) {
        if (executionNode == null)
            return null;
        if (executionNode.getDataObject() instanceof CSrcObject)
            return null;
        if (executionNode.getDataObject() instanceof CoreElfObject)
            return null;
        if (executionNode.getDataObject() instanceof MakefileDataObject)
            return null;
        ExecutionSupport bes = (ExecutionSupport) executionNode.getCookie(ExecutionSupport.class);
        return bes;
    }
    
    public static DataNode findDebuggableNode(String filePath) {
        if (filePath == null)
            return null;
        Node node = findNode(filePath);
        if (node == null)
            return null;
        if (!(node instanceof DataNode))
            return null;
        /*
        if (node.getCookie(DebuggerCookie.class) == null)
            return null;
         */
        
        return (DataNode)node;
    }
    
    /**
     * Same as String.equals, but allows arguments to be null
     */
    public static boolean sameString(String a, String b) {
        if (a == null) {
            return (b == null);
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }
    
    /**
     * Apply 'equals' to two arrays of Strings
     */
    public static boolean sameStringArray(String[] a, String[] b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.length != b.length)
            return false;
        for (int x = 0; x < a.length; x++) {
            if (!IpeUtils.sameString(a[x], b[x]))
                return false;
        }
        return true;
    }
    
    public static String escapeSpaces(String s) {
        if (s.indexOf(' ') < 0)
            return s;
        else {
            //return s.replace(" ", "\\ "); // NOI8N JDK1.5
            return s.replaceAll(" ", "\\\\ "); // NOI18N
        }
    }
    
    /**
     * Trims .. from a file path.
     * NOTE: This is not safe to use on Unix if any of the directories are softlinks in
     * which case abc/def/.. is not necessary the same directory as abc.
     */
    public static String trimDotDot(String path) {
        Stack stack = new Stack();
        String absPrefix = null;
        
        if (isPathAbsolute(path)) {
            if (path.charAt(0) == '/') {
                absPrefix = "/"; // NOI18N
                path = path.substring(1);
}
            else if (path.charAt(1) == ':') {
                absPrefix = path.substring(0,3);
                path = path.substring(3);
            }
        }
        int down = 0;
        // resolve ..
        StringTokenizer st = new StringTokenizer(path, "/"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("..")) { // NOI18N
                if (down > 0) {
                    stack.pop();
                    down--;
}
                else {
                    stack.push(token);
                }
            }
            else {
                stack.push(token);
                down++;
            }
        }
        String retpath = ""; // NOI18N
        if (absPrefix != null)
            retpath = absPrefix; // NOI18N
        for (int i = 0; i < stack.size(); i++) {
            retpath = retpath + stack.elementAt(i);
            if (i < (stack.size() - 1))
            retpath = retpath + "/"; // NOI18N
        }

        return retpath;
    }
    
    public static String createUniqueFileName(String folder, String name, String ext) {
        if (folder == null || folder.length() == 0 || !isPathAbsolute(folder) || name == null || name.length() == 0) {
            assert false;
            return null;
        }
        
        String newPath;
        String newName = name;
        for (int i = 0;;i++) {
            if (i > 0)
                newName = name + "_" + i; // NOI18N
            newPath = folder + "/" + newName; // NOI18N
            if (ext.length() > 0)
                newPath = newPath + "." + ext; // NOI18N
            if (!new File(newPath).exists()) {
                break;
            }
        }
        return newName;
    }
}

