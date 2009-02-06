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
package org.netbeans.modules.cnd.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.netbeans.modules.cnd.settings.ShellSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Support for execution of a class file. Looks for the class with
 * the same base name as the primary file, locates a main method
 * in it, and starts it.
 *
 */
//public class ShellExecSupport extends ExecSupport {
public class ShellExecSupport extends ExecutionSupport {

    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N
    private static final String PROP_SHELL_COMMAND = "shellcommand"; // NOI18N

    /** new ShellExecSupport */
    public ShellExecSupport(Entry entry) {
        super(entry);
    }

    @Override
    public void addProperties(Sheet.Set set) {
        set.put(createParamsProperty());
        set.put(createRunDirectoryProperty());
        set.put(createShellCommandProperty());
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createRunDirectoryProperty() {
        return new PropertySupport.ReadWrite<String>(PROP_RUN_DIRECTORY, String.class,
                getString("PROP_RUN_DIRECTORY"), // NOI18N
                getString("HINT_RUN_DIRECTORY")) { // NOI18N

            public String getValue() {
                return getRunDirectory();
            }

            public void setValue(String val) {
                setRunDirectory(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createShellCommandProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_SHELL_COMMAND, String.class,
                getString("PROP_SHELL_COMMAND"), // NOI18N
                getString("HINT_SHELL_COMMAND")) { // NOI18N

            public String getValue() {
                return getShellCommand();
            }

            public void setValue(String val) {
                setShellCommand(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get the the run directory, the directory to invoke make from.
     *
     *  @return the run directory
     */
    public String getRunDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);

        if (dir == null) {
            dir = "."; // NOI18N
            setRunDirectory(dir);
        }

        return dir;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setRunDirectory(String dir) {
        FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_RUN_DIRECTORY, dir);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set run directory", // NOI18N
            //        new Object[]{FileUtil.toFile(fo).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /**
     *  Get the the shell command
     *
     *  @return the shell command
     */
    public String getShellCommand() {
        String shellCommand = (String) getEntry().getFile().getAttribute(PROP_SHELL_COMMAND);
        if (shellCommand == null || shellCommand.length() == 0) {
            shellCommand = ""; // NOI18N
        }

        return shellCommand;
    }

    /*
     * Return...
     */
    public String[] getShellCommandAndArgs(FileObject fo) {
        String shellCommand = getShellCommand(); // From property

        // If no shell command set, read first line in script and use if set here
        if (shellCommand == null || shellCommand.length() == 0) {
            String fullFileName = FileUtil.toFile(fo).getPath();
            try {
                BufferedReader in = new BufferedReader(new FileReader(fullFileName));
                if (in != null) {
                    String firstLine = in.readLine();
                    if (firstLine != null) {
                        if (firstLine.startsWith("#!")) { // NOI18N
                            if (firstLine.length() > 2) {
                                int i = 2;
                                while (Character.isWhitespace(firstLine.charAt(i))) {
                                    i++;
                                }
                                shellCommand = firstLine.substring(i);
                            }
                        }
                    }
                    in.close();
                }
            } catch (Exception e) {
            }
        }

        // If still no shell command, base it on suffix
        String[] argvParsed;
        if (shellCommand == null || shellCommand.length() == 0) {
            String ext = fo.getExt();
            if (ext != null && ext.length() > 0) {
                if ((ext.equals("bat") || ext.equals("cmd")) && Utilities.isWindows()) {// NOI18N
                    argvParsed = new String[1];
                    argvParsed[0] = ""; // NOI18N
                    return argvParsed;
                } else {
                    shellCommand = "/bin/" + ext; // NOI18N
                    if (!new File(shellCommand).exists()) {
                        shellCommand = null;
                    }
                }
            }
        }

        // If still no shell command, use default from ShellSettings
        if (shellCommand == null || shellCommand.length() == 0) {
            shellCommand = ShellSettings.getDefault().getDefaultShellCommand();
        }
        argvParsed = Utilities.parseParameters(shellCommand);

        return argvParsed;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setShellCommand(String command) {
        FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_SHELL_COMMAND, command);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set shell command", // NOI18N
            //        new Object[]{FileUtil.toFile(fo).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /** Creates the fileparams property for entry.
     * @return the property
     */
    private PropertySupport<String> createParamsProperty() {
        PropertySupport<String> result = new PropertySupport.ReadWrite<String>(
                PROP_FILE_PARAMS,
                String.class,
                getString("PROP_fileParams"),
                getString("HINT_fileParams")) {

            public String getValue() {
                String[] args = getArguments();
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    b.append(args[i]).append(' ');
                }
                return b.toString();
            }

            public void setValue(String val) throws InvocationTargetException {
                if (val != null) {
                    try {
                        // Keep user arguments as is in args[0]
                        setArguments(new String[]{val});
                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() throws InvocationTargetException {
                try {
                    setArguments(null);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
            }

            @Override
            public boolean canWrite() {
                Boolean isReadOnly = (Boolean) getEntry().getFile().getAttribute(READONLY_ATTRIBUTES);
                return (isReadOnly == null) ? false : (!isReadOnly.booleanValue());
            }
        };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.  Arguments can't be multiline anyway.
        result.setValue("oneline", Boolean.TRUE);// NOI18N
        return result;
    }
    private ResourceBundle bundle = null;

    private String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ShellExecSupport.class);
        }
        return bundle.getString(s);
    }
}
