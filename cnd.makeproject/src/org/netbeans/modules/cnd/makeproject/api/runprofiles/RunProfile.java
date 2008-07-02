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

package org.netbeans.modules.cnd.makeproject.api.runprofiles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileXMLCodec;
import org.netbeans.modules.cnd.makeproject.runprofiles.ui.EnvPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class RunProfile implements ConfigurationAuxObject {
    private static final boolean NO_EXEPTION = Boolean.getBoolean("org.netbeans.modules.cnd.makeproject.api.runprofiles");

    public static final String PROFILE_ID = "runprofile"; // NOI18N
    
    /** Property name: runargs (args, cd, etc.) have changed */
    public static final String PROP_RUNARGS_CHANGED = "runargs-ch"; // NOI18N
    public static final String PROP_RUNDIR_CHANGED = "rundir-ch"; // NOI18N
    public static final String PROP_ENVVARS_CHANGED = "envvars-ch"; // NOI18N
    
    private PropertyChangeSupport pcs = null;
    
    private boolean needSave = false;
    
    // Auxiliary info objects (debugger, ...)
    private Vector auxObjects;
    
    // Where this profile is keept
    //private Profiles parent;
    // Clone
    private RunProfile cloneOf;
    // Default Profile. One and only one profile is the default.
    private boolean defaultProfile;
    // Arguments. Quoted flat representation.
    private String argsFlat;
    private boolean argsFlatValid = false;
    // Argumants. Array form.
    private String[] argsArray;
    private boolean argsArrayValid = false;
    // Run Directory. Relative or absolute.
    private String baseDir; // Alwasy set, always absolute
    private String runDir;  // relative (to baseDir) or absolute
    // Should start a build before executing/debugging.
    private boolean buildFirst;
    // Environment
    private Env environment;
    private String dorun;
    
    public static final int CONSOLE_TYPE_DEFAULT = 0;
    public static final int CONSOLE_TYPE_EXTERNAL = 1;
    public static final int CONSOLE_TYPE_OUTPUT_WINDOW = 2;
    
    private static final String[] consoleTypeNames = {
        getString("ConsoleType_Default"), // NOI18N
        getString("ConsoleType_External"), // NOI18N
        getString("ConsoleType_Output"), // NOI18N
    };
    private IntConfiguration consoleType;
    
    private IntConfiguration terminalType;
    private HashMap termPaths;
    private HashMap termOptions;
    
    public RunProfile(String baseDir) {
        this.baseDir = baseDir;
        this.pcs = null;
        initialize();
    }
    
    public RunProfile(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;
        initialize();
    }
    
    public void initialize() {
        //parent = null;
        environment = new Env();
        defaultProfile = false;
        argsFlat = ""; // NOI18N
        argsFlatValid = true;
        argsArrayValid = false;
        runDir = ""; // NOI18N
        buildFirst = true;
        dorun = getDorunScript();
        termPaths = new HashMap();
        termOptions = new HashMap();
        consoleType = new IntConfiguration(null, CONSOLE_TYPE_DEFAULT, consoleTypeNames, null);
        terminalType = new IntConfiguration(null, 0, setTerminalTypeNames(), null);
        clearChanged();
    }
    
    private String getDorunScript() {
        File file = InstalledFileLocator.getDefault().locate("bin/dorun.sh", null, false); // NOI18N
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        } else {
            if (!NO_EXEPTION) {
                throw new IllegalStateException(getString("Err_MissingDorunScript")); // NOI18N
            }
            return null;
        }
    }
    
    private boolean isWindows() {
        //TODO: RunProfile should be fully aware of remote mode
        if (CompilerSetManager.useFakeRemoteCompilerSet) {
            return false;
        }
        return Utilities.isWindows();
    }
    
    private String[] setTerminalTypeNames() {
        List list = new ArrayList();
        String def = getString("TerminalType_Default"); // NOI18N
        String name;
        String termPath;
        
        list.add(def);
        if (isWindows()) {
            String term = getString("TerminalType_CommandWindow"); // NOI18N
            list.add(term);
            termPaths.put(term, "start"); // NOI18N
            termPaths.put(def, "start"); // NOI18N
            termOptions.put(term, "sh \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + " \" -f \"{0}\" {1} {2}"); // NOI18N
            termOptions.put(def,  "sh \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + " \" -f \"{0}\" {1} {2}"); // NOI18N
        } else {
            // Start with the user's $PATH. Append various other directories and look
            // for gnome-terminal, konsole, and xterm.
            String path = Path.getPathAsString() + 
                ":/usr/X11/bin:/usr/X/bin:/usr/X11R6/bin:/opt/gnome/bin" + // NOI18N
                ":/usr/gnome/bin:/opt/kde/bin:/opt/kde3/bin/usr/kde/bin:/usr/openwin/bin"; // NOI18N
            
            termPath = searchPath(path, "gnome-terminal", "/usr/bin"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_GNOME"); // NOI18N
                list.add(name); 
                termPaths.put(name, termPath);
                termPaths.put(def, termPath);
                String opts = "--disable-factory --hide-menubar " + "--title=\"{1} {3}\" " + // NOI18N
                        "-x \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" " + // NOI18N
                        "-f \"{0}\" {1} {2}"; // NOI18N
                termOptions.put(name, opts);
                termOptions.put(def,  opts);
            }
            termPath = searchPath(path, "konsole"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_KDE"); // NOI18N
                list.add(name); 
                termPaths.put(name, termPath);
                termOptions.put(name,    "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                if (termPaths.get(def) == null) {
                    termPaths.put(def, termPath);
                    termOptions.put(def, "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                }
            }
            termPath = searchPath(path, "xterm", Utilities.getOperatingSystem() == Utilities.OS_SOLARIS ? // NOI18N
                        "/usr/openwin/bin" : "/usr/bin"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_XTerm"); // NOI18N
                list.add(name); 
                termPaths.put(name, termPath);
                termOptions.put(name,    "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                if (termPaths.get(def) == null) {
                    termPaths.put(def, termPath);
                    termOptions.put(def, "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                }
            }
            if (termPaths.get(def) == null) {
                list.add(getString("TerminalType_None")); // NOI18N
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
    
    /**
     * Search an augmented $PATH (the user's $PATH plus various standard locations
     * for a specific terminal emulater.
     *
     * @param path The path to search for program "term"
     * @param term The terminal program we're searching for
     * @returns Either a path to the specified term or null
     */
    private String searchPath(String path, String term) {
        return searchPath(path, term, null);
    }
    
    /**
     * Search an augmented $PATH (the user's $PATH plus various standard locations
     * for a specific terminal emulater.
     *
     * @param path The path to search for program "term"
     * @param term The terminal program we're searching for
     * @defaultPath A possible default path to check before searching the entire path
     * @returns Either a path to the specified term or null
     */
    private String searchPath(final String path, final String term, String defaultPath) {
        
        if (defaultPath != null) {
            File file = new File(defaultPath, term);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
//        System.err.println("RP.searchPath: Doing PATH search for " + term);
        final String[] patharray = new String[1];
        patharray[0] = null;
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                StringTokenizer st = new StringTokenizer(path, ":"); // NOI18N

                while (st.hasMoreTokens()) {
                    String dir = st.nextToken();
                    File file = new File(dir, term);
                    if (file.exists()) {
                        patharray[0] = file.getAbsolutePath();
                        break;
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join(5000);
        } catch (InterruptedException ex) {
        }
        return patharray[0];
    }
    
    public String getTerminalPath() {
        return (String) termPaths.get(getTerminalType().getName());
    }
    
    public String getTerminalOptions() {
        return (String) termOptions.get(getTerminalType().getName());
    }
    
    public boolean shared() {
        return false;
    }
    
    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     * OLD:
     * and for storing the object in xml form and
     * parsing the xml code to restore the object.
     */
    public String getId() {
        return PROFILE_ID;
    }
    
    // Set if this profile is a clone of another profile (not set for copy)
    public void setCloneOf(RunProfile profile) {
        this.cloneOf = profile;
    }
    
    public RunProfile getCloneOf() {
        return cloneOf;
    }
    
    // Default Profile ...
    public boolean isDefault() {
        return defaultProfile;
    }
    
    public void setDefault(boolean b) {
        defaultProfile = b;
    }
    
    // Args ...
    public void setArgs(String argsFlat) {
        String oldArgsFlat = getArgsFlat();
        this.argsFlat = argsFlat;
        argsFlatValid = true;
        argsArrayValid = false;
        if (pcs != null && !IpeUtils.sameString(oldArgsFlat, argsFlat))
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsFlat, argsFlat);
        needSave = true;
    }
    
    public void setArgs(String[] argsArray) {
        String[] oldArgsArray = getArgsArray();
        this.argsArray = argsArray;
        argsFlatValid = false;
        argsArrayValid = true;
        if (pcs != null && !IpeUtils.sameStringArray(oldArgsArray, argsArray))
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsArray, argsArray);
        needSave = true;
    }
    
    public String getArgsFlat() {
        if (!argsFlatValid) {
            argsFlat = ""; // NOI18N
            for (int i = 0; i < argsArray.length; i++) {
                argsFlat += IpeUtils.quoteIfNecessary(argsArray[i]);
                if (i < (argsArray.length-1))
                    argsFlat += " "; // NOI18N
            }
            argsFlatValid = true;
        }
        return argsFlat;
    }
    
    public String[] getArgsArray() {
        if (!argsArrayValid) {
            argsArray = Utilities.parseParameters(argsFlat);
            argsArrayValid = true;
        }
        return argsArray;
    }
    
        /*
         * as array shifted one and executable as arg 0
         */
    public String[] getArgv(String ex) {
        String[] argsArrayShifted = new String[getArgsArray().length+1];
        argsArrayShifted[0] = ex;
        for (int i = 0; i < getArgsArray().length; i++)
            argsArrayShifted[i+1] = getArgsArray()[i];
        return argsArrayShifted;
    }
    
        /*
         * Gets base directory. Base directory is always set and is always absolute.
         * Base directory is what run directory is relative to, if it is relative.
         */
    public String getBaseDir() {
        return baseDir;
    }
    
        /*
         * Sets base directory. Base directory should  always be set and is always absolute.
         * Base directory is what run directory is relative to if it is relative.
         */
    public void setBaseDir(String baseDir) {
        assert baseDir != null && IpeUtils.isPathAbsolute( baseDir );
        this.baseDir = baseDir;
    }
    
        /*
         * Gets run directory.
         * Run Directory is either absolute or relative (to base directory).
         */
    public String getRunDir() {
        if (runDir == null)
            runDir = ""; // NOI18N
        return runDir;
    }
    
        /*
         * sets run directory.
         * Run Directory is either absolute or relative (to base directory).
         */
    public void setRunDir(String runDir) {
        if (runDir == null)
            runDir = ""; // NOI18N
        if (this.runDir == runDir)
            return;
        if (this.runDir != null && this.runDir.equals(runDir)) {
            return;
        }
        String oldRunDir = this.runDir;
        this.runDir = runDir;
        if (pcs != null)
            pcs.firePropertyChange(PROP_RUNDIR_CHANGED, null, this);
        needSave = true;
    }
    
    
        /*
         * Gets absolute run directory.
         */
    public String getRunDirectory() {
        String runDirectory;
        String runDirectoryCanonicalPath;
        String runDir2 = getRunDir();
        if (runDir2.length() == 0)
            runDir2 = "."; // NOI18N
        if (IpeUtils.isPathAbsolute(runDir2))
            runDirectory = runDir2;
        else
            runDirectory = getBaseDir() + "/" + runDir2; // NOI18N
        
        // convert to canonical path
        File runDirectoryFile = new File(runDirectory);
        if (!runDirectoryFile.exists() || !runDirectoryFile.isDirectory()) {
            return runDirectory; // ??? FIXUP
        }
        try {
            runDirectoryCanonicalPath = runDirectoryFile.getCanonicalPath();
        } catch (IOException ioe) {
            runDirectoryCanonicalPath = runDirectory;
        }
        return runDirectoryCanonicalPath;
    }
    
        /*
         * Sets run directory.
         * If new run directory is relative, just set it.
         * If new run directory is absolute, convert to relative if already relative,
         * othervise just set it.
         */
    public void setRunDirectory(String newRunDir) {
        if (newRunDir == null || newRunDir.length() == 0) {
            newRunDir = "."; // NOI18N
        }
        setRunDir(IpeUtils.toAbsoluteOrRelativePath(getBaseDir(), newRunDir));
    }
    
    // Should Build ...
    public void setBuildFirst(boolean buildFirst) {
        this.buildFirst = buildFirst;
    }
    
    public boolean getBuildFirst() {
        return buildFirst;
    }
    
    // Environment
    public Env getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Env environment) {
        this.environment = environment;
        if (pcs != null)
            pcs.firePropertyChange(PROP_ENVVARS_CHANGED, null, this);
    }
    
    public IntConfiguration getConsoleType() {
        return consoleType;
    }
    
    public void setConsoleType(IntConfiguration consoleType) {
        this.consoleType = consoleType;
    }
    
    public int getDefaultConsoleType() {
        return CONSOLE_TYPE_EXTERNAL;
    }
    
    public IntConfiguration getTerminalType() {
        if (terminalType.getName().equals(getString("TerminalType_None"))) { // NOI18N
            return null;
        } else {
            return terminalType;
        }
    }
    
    public void setTerminalType(IntConfiguration terminalType) {
        this.terminalType = terminalType;
    }
    
    
    // Misc...
    
    /**
     * Saves this profile *and* all other profiles of the same parent to disk
     */
    public void saveToDisk() {
            /*
            if (parent != null) {
                parent.saveToDisk();
            }
             */
    }
    
    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null)
            pcs.addPropertyChangeListener(l);
    }
    
    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null)
            pcs.removePropertyChangeListener(l);
    }
    
    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    //
    
    public XMLDecoder getXMLDecoder() {
        return new RunProfileXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new RunProfileXMLCodec(this);
    }
    
    /**
     * Responsible for saving the object in xml format.
     * It should save the object in the following format using the id
     * string from getId():
     * <id-string>
     *     <...
     *     <...
     * </id-string>
     */
    
        /* OLD
        public void writeElement(PrintWriter pw, int indent, Object object) {
            RunProfileHelper.writeProfileBlock(pw, indent, this);
        }
         */
    
    /**
     * Responsible for parsing the xml code created from above and
     * for restoring the state of the object (but not the object itself).
     * Refer to the Sax parser documentation for details.
     */
    
        /* OLD
        public void startElement(String namespaceURI, String localName, String element, Attributes atts) {
            RunProfileHelper.startElement(this, element, atts);
        }
         */
    
        /* OLD
        public void endElement(String uri, String localName, String qName, String currentText) {
            RunProfileHelper.endElement(this, qName, currentText);
        }
         */
    
    // interface ProfileAuxObject
    public boolean hasChanged() {
        return needSave;
    }
    
    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }
    
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof RunProfile)) {
            // FIXUP: exception ????
            System.err.print("Profile - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        RunProfile p = (RunProfile)profileAuxObject;
        setDefault(p.isDefault());
        setArgs(p.getArgsFlat());
        setBaseDir(p.getBaseDir());
        setRunDir(p.getRunDir());
        //setRawRunDirectory(p.getRawRunDirectory());
        setBuildFirst(p.getBuildFirst());
        setEnvironment(p.getEnvironment());
        setConsoleType(p.getConsoleType());
        setTerminalType(p.getTerminalType());
    }
    
    public RunProfile cloneProfile() {
        return (RunProfile)clone();
    }
    
    /**
     * Clones the profile.
     * All fields are cloned except for 'parent'.
     */
    @Override
    public Object clone() {
        RunProfile p = new RunProfile(getBaseDir());
        //p.setParent(getParent());
        p.setCloneOf(this);
        p.setDefault(isDefault());
        p.setArgs(getArgsFlat());
        p.setRunDir(getRunDir());
        //p.setRawRunDirectory(getRawRunDirectory());
        p.setBuildFirst(getBuildFirst());
        p.setEnvironment(getEnvironment().cloneEnv());
        p.setConsoleType(getConsoleType());
        p.setTerminalType(getTerminalType());
        return p;
    }
    
    public Sheet getSheet() {
        return createSheet();
    }
    
    private Sheet createSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralName"));
        set.setShortDescription(getString("GeneralTT"));
        set.put(new ArgumentsNodeProp());
        set.put(new RunDirectoryNodeProp());
        set.put(new EnvNodeProp());
        set.put(new BuildFirstNodeProp());
        set.put(new IntNodeProp(getConsoleType(), true, null,
                getString("ConsoleType_LBL"), getString("ConsoleType_HINT"))); // NOI18N
        set.put(new IntNodeProp(getTerminalType(), true, null,
                getString("TerminalType_LBL"), getString("TerminalType_HINT"))); // NOI18N
        sheet.put(set);
        
        return sheet;
    }
    
    private static String getString(String s) {
        return NbBundle.getMessage(RunProfile.class, s);
    }
    
    private class ArgumentsNodeProp extends PropertySupport {
        public ArgumentsNodeProp() {
            super("Arguments", String.class, getString("ArgumentsName"), getString("ArgumentsHint"), true, true); // NOI18N
        }
        
        public Object getValue() {
            return getArgsFlat();
        }
        
        public void setValue(Object v) {
            setArgs((String)v);
        }
    }
    
    private class RunDirectoryNodeProp extends PropertySupport {
        public RunDirectoryNodeProp() {
            super("Run Directory", String.class, getString("RunDirectoryName"), getString("RunDirectoryHint"), true, true); // NOI18N
        }
        
        public Object getValue() {
            return getRunDir();
        }
        
        public void setValue(Object v) {
            String path = IpeUtils.toAbsoluteOrRelativePath(getBaseDir(), (String)v);
            path = FilePathAdaptor.mapToRemote(path);
            path = FilePathAdaptor.normalize(path);
            setRunDir(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            String seed;
            String runDir2 = getRunDir();
            if (runDir2.length() == 0)
                runDir2 = "."; // NOI18N
            if (IpeUtils.isPathAbsolute(runDir2))
                seed = runDir2;
            else
                seed = getBaseDir() + File.separatorChar + runDir2;
            return new DirEditor(seed);
        }
    }
    
    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            setRunDir(text);
        }
        
        @Override
        public String getAsText() {
            return getRunDir();
        }
        
        @Override
        public Object getValue() {
            return getRunDir();
        }
        
        @Override
        public void setValue(Object v) {
            setRunDir((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new DirectoryChooserPanel(seed, this, propenv);
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
    
    private class BuildFirstNodeProp extends PropertySupport {
        public BuildFirstNodeProp() {
            super("Build First", Boolean.class, getString("BuildFirstName"), getString("BuildFirstHint"), true, true); // NOI18N
        }
        
        public Object getValue() {
            return new Boolean(getBuildFirst());
        }
        
        public void setValue(Object v) {
            setBuildFirst(((Boolean)v).booleanValue());
        }
    }
    
    private class EnvNodeProp extends PropertySupport {
        public EnvNodeProp() {
            super("Environment", Env.class, getString("EnvironmentName"), getString("EnvironmentHint"), true, true); // NOI18N
        }
        
        public Object getValue() {
            return getEnvironment();
        }
        
        public void setValue(Object v) {
            getEnvironment().assign((Env)v);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new EnvEditor(getEnvironment().cloneEnv());
        }
        
        @Override
        public Object getValue(String attributeName) {
            if (attributeName.equals("canEditAsText")) // NOI18N
                return Boolean.FALSE;
            return super.getValue(attributeName);
        }
    }
    
    private class EnvEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private Env env;
        private PropertyEnv propenv;
        
        public EnvEditor(Env env) {
            this.env = env;
        }
        
        @Override
        public void setAsText(String text) {
        }
        
        @Override
        public String getAsText() {
            return env.toString();
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new EnvPanel(env, this, propenv);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
}
