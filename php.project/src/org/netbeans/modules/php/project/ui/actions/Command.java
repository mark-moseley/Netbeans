/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Radek Matous
 */
public abstract class Command {

    private final PhpProject project;

    public Command(PhpProject project) {
        this.project = project;
        assert project != null;
    }

    public abstract String getCommandId();

    public abstract void invokeAction(Lookup context) throws IllegalArgumentException;

    public abstract boolean isActionEnabled(Lookup context) throws IllegalArgumentException;

    public boolean asyncCallRequired() {
        return true;
    }

    public boolean saveRequired() {
        return true;
    }

    public final PhpProject getProject() {
        return project;
    }

    //Helper|Utility methods for subclasses
    protected final void showURLForContext(Lookup context) throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(urlForContext(context));
    }

    protected final void showURLForProjectFile() throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(urlForProjectFile());
    }

    protected final void showURLForDebugProjectFile() throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(urlForDebugProjectFile());
    }

    protected final void showURLForDebugContext(Lookup context) throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(urlForDebugContext(context));
    }

    protected final String getProperty(String propertyName) {
        return getPropertyEvaluator().getProperty(propertyName);
    }

    protected final URL getBaseURL() throws MalformedURLException {
        String baseURLPath = getProperty(PhpProjectProperties.URL);
        if (baseURLPath == null) {
            throw new MalformedURLException();
        }
        return new URL(baseURLPath);
    }

    protected final URL appendQuery(URL originalURL, String queryWithoutQMark) throws MalformedURLException {
        URI retval;
        try {
            retval = new URI(originalURL.getProtocol(), originalURL.getUserInfo(),
                    originalURL.getHost(), originalURL.getPort(), originalURL.getPath(),
                    queryWithoutQMark, originalURL.getRef());
            return retval.toURL();
        } catch (URISyntaxException ex) {
            MalformedURLException mex = new MalformedURLException(ex.getLocalizedMessage());
            mex.initCause(ex);
            throw mex;
        }
    }

    protected final URL urlForDebugProjectFile() throws MalformedURLException {
        return appendQuery(urlForProjectFile(), "XDEBUG_SESSION_START=" + PhpSourcePath.DEBUG_SESSION); //NOI18N
    }

    protected final URL urlForDebugContext(Lookup context) throws MalformedURLException {
        return appendQuery(urlForContext(context), "XDEBUG_SESSION_START=" + PhpSourcePath.DEBUG_SESSION); //NOI18N
    }

    protected final URL urlForProjectFile() throws MalformedURLException {
        String relativePath = relativePathForProject();
        if (relativePath == null) {
            //TODO makes sense just in case if listing is enabled | maybe user message
            relativePath = ""; //NOI18N
        }
        URL retval = new URL(getBaseURL(), relativePath);
        String arguments = getProperty(PhpProjectProperties.ARGS);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    protected final URL urlForContext(Lookup context) throws MalformedURLException {
        String relativePath = relativePathForConext(context);
        if (relativePath == null) {
            throw new MalformedURLException();
        }
        URL retval = new URL(getBaseURL(), relativePath);
        String arguments = getProperty(PhpProjectProperties.ARGS);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    //or null
    protected final String relativePathForConext(Lookup context) {
        return getCommandUtils().getRelativeSrcPath(fileForContext(context));
    }

    //or null
    protected final String relativePathForProject() {
        return getCommandUtils().getRelativeSrcPath(fileForProject());
    }

    //or null
    protected final FileObject fileForProject() {
        FileObject retval = null;
        String nameOfIndexFile = getProperty(PhpProjectProperties.INDEX_FILE);
        FileObject[] srcRoots = Utils.getSourceObjects(getProject());
        for (FileObject fileObject : srcRoots) {
            retval = fileObject.getFileObject(nameOfIndexFile);
            if (retval != null) {
                break;
            }
        }
        return retval;
    }

    protected boolean useInterpreter() {
        String runAs = getPropertyEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        return PhpProjectProperties.RunAsType.SCRIPT.name().equals(runAs);
    }

    protected String getPhpInterpreter() {
        String retval = PhpOptions.getInstance().getPhpInterpreter();
        return (retval != null && retval.length() >  0) ? retval.trim() : null;
    }

    protected boolean isRemoteConfigSelected() {
        String runAs = getPropertyEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        return PhpProjectProperties.RunAsType.REMOTE.name().equals(runAs);
    }

    //or null
    protected final FileObject fileForContext(Lookup context) {
        CommandUtils utils = getCommandUtils();
        FileObject[] files = utils.phpFilesForContext(context);
        if (files == null || files.length == 0) {
            files = utils.phpFilesForSelectedNodes();
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    protected final Command getOtherCommand(String commandName) {
        PhpActionProvider provider = getProject().getLookup().lookup(PhpActionProvider.class);
        assert provider != null;
        return provider.getCommand(commandName);
    }


    private CommandUtils getCommandUtils() {
        CommandUtils utils = new CommandUtils(getProject());
        return utils;
    }

    private static OutputWriter getOutputWriter(String outTabTitle) {
        InputOutput io = IOProvider.getDefault().getIO(outTabTitle, false);
        io.select();
        OutputWriter writer = io.getOut();
        return writer;
    }

    protected final BufferedReader reader(InputStream is, Charset encoding) {
        return new BufferedReader(new InputStreamReader(is, encoding));
    }

    protected final BufferedWriter outputTabWriter(File scriptFile) {
        String outputTitle = getOutputTabTitle(scriptFile);
        BufferedWriter outputWriter = new BufferedWriter(getOutputWriter(outputTitle));
        return outputWriter;
    }

    protected final String getOutputTabTitle(File scriptFile) {
        assert this instanceof Displayable;
        return MessageFormat.format("{0} - {1}", ((Displayable) this).getDisplayName(), scriptFile.getName());
    }

    protected final BufferedWriter writer(OutputStream os, Charset encoding) {
        return new BufferedWriter(new OutputStreamWriter(os, encoding));
    }

    protected final void rewriteAndClose(BufferedReader reader, BufferedWriter writer,
            Command.StringConvertor convertor) throws IOException {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                line = (convertor != null) ? convertor.convert(line) : line;
                writer.write(line);
                writer.newLine();
            }
        } finally {
            writer.flush();
            reader.close();
            writer.close();
        }
    }

    public interface StringConvertor {
        String convert(String text);
    }

    private PropertyEvaluator getPropertyEvaluator() {
        return getProject().getEvaluator();
    }
}