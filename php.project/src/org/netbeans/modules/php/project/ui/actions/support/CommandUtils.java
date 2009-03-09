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
package org.netbeans.modules.php.project.ui.actions.support;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous, Tomas Mysik
 */
public final class CommandUtils {
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private CommandUtils() {
    }

    public static boolean isPhpFile(FileObject file) {
        assert file != null;
        return PhpSourcePath.MIME_TYPE.equals(FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE));
    }

    public static boolean isPhpOrHtmlFile(FileObject file) {
        assert file != null;
        String mimeType = FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE, HTML_MIME_TYPE);
        return PhpSourcePath.MIME_TYPE.equals(mimeType) || HTML_MIME_TYPE.equals(mimeType);
    }

    /** Return <code>true</code> if user wants to restart the current debug session. */
    public static boolean warnNoMoreDebugSession() {
        String message = NbBundle.getMessage(CommandUtils.class, "MSG_NoMoreDebugSession");
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    public static void processExecutionException(ExecutionException exc) {
        final Throwable cause = exc.getCause();
        assert cause != null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(
                        cause, NbBundle.getMessage(CommandUtils.class, "MSG_ExceptionDuringRunScript", cause.getLocalizedMessage())));
                OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
            }
        });
    }

    /**
     * Get a {@link PhpUnit} instance (path from IDE options used).
     * @param showCustomizer if <code>true</code>, IDE options dialog is shown if the path of PHP Unit is not valid.
     * @return a {@link PhpUnit} instance or <code>null</code> if the path of PHP Unit is not valid.
     */
    public static PhpUnit getPhpUnit(boolean showCustomizer) {
        final String phpUnitPath = PhpOptions.getInstance().getPhpUnit();
        if (Utils.validatePhpUnit(phpUnitPath) != null) {
            if (showCustomizer) {
                OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
            }
            return null;
        }
        return new PhpUnit(phpUnitPath);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for given nodes.
     * @param nodes nodes to get {@link FileObject}s from.
     * @return list of <b>valid</b> {@link FileObject}s, never <code>null</code>.
     */
    public static List<FileObject> getFileObjects(final Node[] nodes) {
        if (nodes.length == 0) {
            return Collections.<FileObject>emptyList();
        }

        final List<FileObject> files = new ArrayList<FileObject>(nodes.length);
        for (Node node : nodes) {
            FileObject fo = getFileObject(node);
            // #156939
            if (fo != null) {
                files.add(fo);
            }
        }
        return files;
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for given node.
     * @param node node to get {@link FileObject}s from.
     * @return a <b>valid</b> {@link FileObject}, <code>null</code> otherwise.
     */
    public static FileObject getFileObject(Node node) {
        assert node != null;

        FileObject fileObj = node.getLookup().lookup(FileObject.class);
        if (fileObj != null && fileObj.isValid()) {
            return fileObj;
        }
        DataObject dataObj = node.getCookie(DataObject.class);
        if (dataObj == null) {
            return null;
        }
        fileObj = dataObj.getPrimaryFile();
        if (fileObj != null && fileObj.isValid()) {
            return fileObj;
        }
        return null;
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project sources directory
     * or sources directory itself.
     * @param project project to get sources directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project sources directory
     *         or sources directory itself.
     */
    public static boolean isUnderSources(PhpProject project, FileObject fileObj) {
        assert project != null;
        assert fileObj != null;
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        return sources.equals(fileObj) || FileUtil.isParentOf(sources, fileObj);
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project tests directory
     * or tests directory itself.
     * @param project project to get tests directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project tests directory
     *         or tests directory itself.
     */
    public static boolean isUnderTests(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        assert project != null;
        assert fileObj != null;
        FileObject tests = ProjectPropertiesSupport.getTestDirectory(project, showFileChooser);
        return tests != null && (tests.equals(fileObj) || FileUtil.isParentOf(tests, fileObj));
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project Selenium tests directory
     * or Selenium tests directory itself.
     * @param project project to get tests directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project Selenium tests directory
     *         or Selenium tests directory itself.
     */
    public static boolean isUnderSelenium(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        assert project != null;
        assert fileObj != null;
        FileObject selenium = ProjectPropertiesSupport.getSeleniumDirectory(project, showFileChooser);
        return selenium != null && (selenium.equals(fileObj) || FileUtil.isParentOf(selenium, fileObj));
    }

    /**
     * Get {@link FileObject}s for context.
     * @param context context to search in.
     * @return {@link FileObject}s for context.
     */
    public static FileObject[] filesForContext(Lookup context) {
        assert context != null;
        Collection<? extends FileObject> files = context.lookupAll(FileObject.class);
        return files.toArray(new FileObject[files.size()]);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for context and base directory.
     * Return <code>null</code> if any {@link FileObject} is invalid or if
     * the base directory is not parent folder of all found {@link FileObject}s.
     * @param context context to search in.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return <b>valid</b> {@link FileObject}s for context and base directory or <code>null</code>.
     */
    public static FileObject[] filesForContext(Lookup context, FileObject baseDirectory) {
        return filterValidFiles(filesForContext(context), baseDirectory);
    }

    /**
     * Get an array of {@link FileObject}s for currently selected nodes.
     * @return an array of {@link FileObject}s for currently selected nodes, never <code>null</code>.
     * @see #filesForSelectedNodes(FileObject)
     */
    public static FileObject[] filesForSelectedNodes() {
        Node[] nodes = getSelectedNodes();
        if (nodes == null) {
            return new FileObject[0];
        }
        List<FileObject> fileObjects = getFileObjects(nodes);
        return fileObjects.toArray(new FileObject[fileObjects.size()]);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for selected nodes and base directory.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return <b>valid</b> {@link FileObject}s for selected nodes and base directory or <code>null</code>.
     * @see #filesForSelectedNodes()
     */
    public static FileObject[] filesForSelectedNodes(FileObject baseDirectory) {
        return filterValidFiles(Arrays.asList(filesForSelectedNodes()), baseDirectory);
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for context or selected nodes and base directory.
     * Return <code>null</code> if any {@link FileObject} is invalid or if
     * the base directory is not parent folder of all found {@link FileObject}s.
     * @param context context to search in.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return a <b>valid</b> {@link FileObject} for context or selected nodes and base directory or <code>null</code>.
     * @see #fileForContextOrSelectedNodes(Lookup)
     */
    public static FileObject fileForContextOrSelectedNodes(Lookup context, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        FileObject[] files = filesForContext(context, baseDirectory);
        if (files == null || files.length == 0) {
            files = filesForSelectedNodes(baseDirectory);
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for context or selected nodes.
     * Return <code>null</code> if any {@link FileObject} is invalid.
     * @param context context to search in.
     * @return a <b>valid</b> {@link FileObject} for context or selected nodes or <code>null</code>.
     * @see #fileForContextOrSelectedNodes(Lookup, FileObject)
     */
    public static FileObject fileForContextOrSelectedNodes(Lookup context) {

        FileObject[] files = filesForContext(context);
        if (files.length == 0) {
            files = filesForSelectedNodes();
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    /**
     * Get {@link URL} for running a project.
     * @param project a project to get {@link URL} for.
     * @return {@link URL} for running a project.
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForProject(PhpProject project) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject indexFile = fileForProject(project, webRoot);
        return urlForFile(project, webRoot, indexFile);
    }

    /**
     * Get {@link URL} for debugging a project.
     * @param project a project to get {@link URL} for.
     * @return {@link URL} for debugging a project.
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugProject(PhpProject project) throws MalformedURLException {
        DebugInfo debugInfo = getDebugInfo(project);
        URL debugUrl = urlForProject(project);
        if (debugInfo.debugServer) {
            debugUrl = appendQuery(debugUrl, getDebugArguments());
        }
        return debugUrl;
    }

    /**
     * Create {@link URL} for debugging from the given {@link URL}.
     * @param url original URL
     * @return {@link URL} for debugging
     * @throws MalformedURLException if any error occurs
     */
    public static URL createDebugUrl(URL url) throws MalformedURLException {
        return appendQuery(url, getDebugArguments());
    }

    /**
     * Get {@link URL} for running a project context (specific file).
     * @param project a project to get {@link URL} for.
     * @param context a context to get {@link URL} for.
     * @return {@link URL} for running a project context (specific file).
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForContext(PhpProject project, Lookup context) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject selectedFile = fileForContextOrSelectedNodes(context, webRoot);
        return urlForFile(project, webRoot, selectedFile);
    }

    /**
     * Get {@link URL} for debugging a project context (specific file).
     * @param project a project to get {@link URL} for.
     * @param context a context to get {@link URL} for.
     * @return {@link URL} for debugging a project context (specific file).
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugContext(PhpProject project, Lookup context) throws MalformedURLException {
        DebugInfo debugInfo = getDebugInfo(project);
        URL debugUrl = urlForContext(project, context);
        if (debugInfo.debugServer) {
            debugUrl = appendQuery(debugUrl, getDebugArguments());
        }
        return debugUrl;
    }

    /**
     * Get the index file (start file) for a project.
     * @param project a project to get index file for.
     * @param baseDirectory base directory to which is index file resolved (sources, tests, web root).
     * @return the index file (start file) for a project, can be <code>null</code> if file is invalid or not found.
     */
    public static FileObject fileForProject(PhpProject project, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        String indexFile = ProjectPropertiesSupport.getIndexFile(project);
        if (indexFile != null) {
            return baseDirectory.getFileObject(indexFile);
        }
        return baseDirectory;
    }

    /**
     * Get {@link DebugInfo debug information} for a project (server side debugging,
     * client side debugging).
     * @param project a project to get information for.
     * @return {@link DebugInfo debug information} for a project.
     */
    public static DebugInfo getDebugInfo(PhpProject project) {
        boolean debugServer = WebClientToolsProjectUtils.getServerDebugProperty(project);
        boolean debugClient = WebClientToolsProjectUtils.getClientDebugProperty(project);

        if (!WebClientToolsSessionStarterService.isAvailable()) {
            debugServer = true;
            debugClient = false;
        }
        assert debugServer || debugClient;
        return new DebugInfo(debugClient, debugServer);
    }

    /**
     *
     * @param project
     * @return
     * @throws MalformedURLException if any error occurs.
     */
    public static URL getBaseURL(PhpProject project) throws MalformedURLException {
        String baseURLPath = ProjectPropertiesSupport.getUrl(project);
        if (baseURLPath == null) {
            throw new MalformedURLException();
        }
        return new URL(baseURLPath);
    }

    /**
     * Get {@link Command} for given project and command name (identifier).
     * @param project project to get a command for.
     * @param commandName command name (identifier).
     * @return {@link Command} for given project and command name (identifier), never <code>null</code>.
     */
    public static Command getCommand(PhpProject project, String commandName) {
        PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
        assert provider != null;
        return provider.getCommand(commandName);
    }

    private static Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }

    private static URL urlForFile(PhpProject project, FileObject webRoot, FileObject file) throws MalformedURLException {
        String relativePath = null;
        if (file == null) {
            // index file not set (or not valid but it's ok if we run project [not for debug project])
            relativePath = ""; // NOI18N
        } else {
            relativePath = FileUtil.getRelativePath(webRoot, file);
            assert relativePath != null : String.format("WebRoot %s must be parent of file %s", webRoot, file);
        }
        URL retval = new URL(getBaseURL(project), relativePath);
        String arguments = ProjectPropertiesSupport.getArguments(project);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    private static URL appendQuery(URL originalURL, String queryWithoutQMark) throws MalformedURLException {
        assert PhpProjectUtils.hasText(queryWithoutQMark);
        assert !queryWithoutQMark.startsWith("&");
        assert !queryWithoutQMark.startsWith("?");

        String query = originalURL.getQuery();
        if (PhpProjectUtils.hasText(query)) {
            queryWithoutQMark = query + "&" + queryWithoutQMark; // NOI18N
        }
        URI retval;
        try {
            //TODO: check the conversion becaus eof #159928
            retval = new URI(originalURL.getProtocol(), originalURL.getUserInfo(),
                    originalURL.getHost(), originalURL.getPort(), originalURL.getPath(),
                    queryWithoutQMark, originalURL.getRef());
        } catch (URISyntaxException ex) {
            MalformedURLException mex = new MalformedURLException(ex.getLocalizedMessage());
            mex.initCause(ex);
            throw mex;
        }
        return retval.toURL();
    }

    private static String getDebugArguments() {
        return "XDEBUG_SESSION_START=" + PhpOptions.getInstance().getDebuggerSessionId(); // NOI18N
    }

    private static FileObject[] filterValidFiles(FileObject[] files, FileObject dir) {
        return filterValidFiles(Arrays.asList(files), dir);
    }

    private static FileObject[] filterValidFiles(Collection<? extends FileObject> files, FileObject dir) {
        Collection<FileObject> retval = new LinkedHashSet<FileObject>();
        for (FileObject file : files) {
            if (!FileUtil.isParentOf(dir, file) || FileUtil.toFile(file) == null) {
                return null;
            }
            retval.add(file);
        }
        return (!retval.isEmpty()) ? retval.toArray(new FileObject[retval.size()]) : null;
    }

    /**
     * Holder class for debug information for a project (server side debugging,
     * client side debugging).
     */
    public static final class DebugInfo {
        public final boolean debugClient;
        public final boolean debugServer;

        public DebugInfo(boolean debugClient, boolean debugServer) {
            this.debugClient = debugClient;
            this.debugServer = debugServer;
        }
    }
}
