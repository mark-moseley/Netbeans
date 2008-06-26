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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class RunLocalCommand extends Command implements Displayable {

    public static final String ID = "run.local"; // NOI18N

    public RunLocalCommand(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeAction(final Lookup context) throws IllegalArgumentException {
        String command = getPhpInterpreter();
        FileObject scriptFo = (context == null) ? fileForProject() : fileForContext(context);
        File scriptFile = (scriptFo != null) ? FileUtil.toFile(scriptFo) : null;
        if (command == null || scriptFile == null) {
            //TODO mising error handling
            return;
        }

        //find out encoding
        Charset encoding = FileEncodingQuery.getDefaultEncoding();
        encoding = FileEncodingQuery.getEncoding(scriptFo);

        //prepare & start external process
        List<String> commandList = new ArrayList<String>();
        commandList.addAll(Arrays.asList(new String[]{command, scriptFile.getAbsolutePath()}));
        String argProperty = getProperty(PhpProjectProperties.ARGS);
        if (argProperty != null && argProperty.length() > 0) {
            commandList.addAll(Arrays.asList(argProperty.split(" "))); // NOI18N
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(scriptFile.getParentFile());
        initProcessBuilder(processBuilder);
        try {
            PhpOptions options = PhpOptions.getInstance();            
            Process process = processBuilder.start();
            final File outputTmpFile = FileUtil.normalizeFile(tempFileForScript(scriptFile));
            BufferedReader reader = reader(process.getInputStream(), encoding);
            BufferedWriter fileWriter = writer(new FileOutputStream(outputTmpFile), encoding);
            if (options.isOpenResultInOutputWindow()) {            
                BufferedWriter outputTabWriter = outputTabWriter(scriptFile, false, true);
                rewriteAndClose(null, reader, fileWriter, outputTabWriter);                
            } else {
                rewriteAndClose(null, reader, fileWriter);
            }
            int exitValue = process.waitFor();

            if (options.isOpenResultInBrowser()) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(outputTmpFile.toURL());
            }
            if (options.isOpenResultInEditor()) {
                FileObject fo = FileUtil.toFileObject(outputTmpFile);
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                ec.open();
            }
            if (options.isOpenResultInOutputWindow() && exitValue != 0) {
                reader = reader(new FileInputStream(outputTmpFile), encoding);
                BufferedWriter writer = outputTabWriter(scriptFile, true, true);
                rewriteAndClose(null, reader, writer);
            }
        } catch (IOException ex) {
            // #137225
            // inform user in output window
            try {
                processException(scriptFile, ex);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return ((context == null) ? fileForProject() : fileForContext(context)) != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(RunCommand.class, "LBL_RunLocalCommand");

    }

    //designed to set env.variables for debugger to resuse this code
    protected  void initProcessBuilder(ProcessBuilder processBuilder) {
    }

    private void processException(File scriptFile, IOException exception) throws IOException {
        BufferedWriter outputTabWriter = outputTabWriter(scriptFile, true, true);
        try {
            exception.printStackTrace(new PrintWriter(outputTabWriter));
        } finally {
            outputTabWriter.close();
        }
    }

//    private void processError(Process process, File scriptFile, Charset encoding) throws IOException {
//        BufferedReader errorReader = reader(process.getErrorStream(), encoding);
//        BufferedWriter outputWriter = outputTabWriter(scriptFile, true);
//        rewriteAndClose(errorReader, outputWriter, new StringConvertor() {
//            public String convert(String text) {
//                return NbBundle.getMessage(RunLocalCommand.class, "LBL_ExecErrorMsg", text);
//            }
//        });
//    }

    private File processOutput(Process process, File scriptFile, Charset encoding) throws IOException {
        final File retval = tempFileForScript(scriptFile);
        BufferedReader reader = reader(process.getInputStream(), encoding);
        BufferedWriter fileWriter = writer(new FileOutputStream(retval), encoding);
        rewriteAndClose(null,reader, fileWriter);
        return retval;
    }

    private File tempFileForScript(File scriptFile) throws IOException {
        File retval = File.createTempFile(scriptFile.getName(), ".html"); //NOI18N
        retval.deleteOnExit();
        return retval;
    }
}
