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

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.ui.copy.*;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.update.*;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.ui.diff.ExportDiffAction;
import org.netbeans.modules.subversion.ui.blame.BlameAction;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.ui.project.ImportAction;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.api.project.Project;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.properties.SvnPropertiesAction;
import org.netbeans.modules.subversion.ui.relocate.RelocateAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.diff.PatchAction;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 *
 * @author Maros Sandor
 */
public class Annotator {

    private static MessageFormat uptodateFormat = getFormat("uptodateFormat");  // NOI18N
    private static MessageFormat newLocallyFormat = getFormat("newLocallyFormat");  // NOI18N
    private static MessageFormat addedLocallyFormat = getFormat("addedLocallyFormat"); // NOI18N
    private static MessageFormat modifiedLocallyFormat = getFormat("modifiedLocallyFormat"); // NOI18N
    private static MessageFormat removedLocallyFormat = getFormat("removedLocallyFormat"); // NOI18N
    private static MessageFormat deletedLocallyFormat = getFormat("deletedLocallyFormat"); // NOI18N
    private static MessageFormat newInRepositoryFormat = getFormat("newInRepositoryFormat"); // NOI18N
    private static MessageFormat modifiedInRepositoryFormat = getFormat("modifiedInRepositoryFormat"); // NOI18N
    private static MessageFormat removedInRepositoryFormat = getFormat("removedInRepositoryFormat"); // NOI18N
    private static MessageFormat conflictFormat = getFormat("conflictFormat"); // NOI18N
    private static MessageFormat mergeableFormat = getFormat("mergeableFormat"); // NOI18N
    private static MessageFormat excludedFormat = getFormat("excludedFormat"); // NOI18N

    private static MessageFormat newLocallyTooltipFormat = getFormat("newLocallyTooltipFormat");  // NOI18N
    private static MessageFormat addedLocallyTooltipFormat = getFormat("addedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat modifiedLocallyTooltipFormat = getFormat("modifiedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat removedLocallyTooltipFormat = getFormat("removedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat deletedLocallyTooltipFormat = getFormat("deletedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat newInRepositoryTooltipFormat = getFormat("newInRepositoryTooltipFormat"); // NOI18N
    private static MessageFormat modifiedInRepositoryTooltipFormat = getFormat("modifiedInRepositoryTooltipFormat"); // NOI18N
    private static MessageFormat removedInRepositoryTooltipFormat = getFormat("removedInRepositoryTooltipFormat"); // NOI18N
    private static MessageFormat conflictTooltipFormat = getFormat("conflictTooltipFormat"); // NOI18N
    private static MessageFormat mergeableTooltipFormat = getFormat("mergeableTooltipFormat"); // NOI18N
    private static MessageFormat excludedTooltipFormat = getFormat("excludedTooltipFormat"); // NOI18N

    private static String badgeModified = "org/netbeans/modules/subversion/resources/icons/modified-badge.png";
    private static String badgeConflicts = "org/netbeans/modules/subversion/resources/icons/conflicts-badge.png";

    private static String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally");
    private static String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");

    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N

    public static String ANNOTATION_REVISION    = "revision";
    public static String ANNOTATION_STATUS      = "status";
    public static String ANNOTATION_FOLDER      = "folder";
    public static String ANNOTATION_MIME_TYPE   = "mime_type";

    public static String[] LABELS = new String[] {ANNOTATION_REVISION, ANNOTATION_STATUS, ANNOTATION_FOLDER, ANNOTATION_MIME_TYPE};
    private final RequestProcessor.Task modifiedFilesRPScanTask;
    private final ModifiedFilesScanTask modifiedFilesScanTask;
    private static final RequestProcessor rp = new RequestProcessor("MercurialAnnotateScan", 1, true); // NOI18N
    private final WeakSet<Map<File, FileInformation>> allModifiedFiles = new WeakSet<Map<File, FileInformation>>(1);
    private Map<File, FileInformation> modifiedFiles = null;

    private final FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;

    private boolean mimeTypeFlag;

    Annotator(Subversion svn) {
        this.cache = svn.getStatusCache();
        modifiedFilesRPScanTask = rp.create(modifiedFilesScanTask = new ModifiedFilesScanTask());
        initDefaults();
    }

    private void initDefaults() {
        Field [] fields = Annotator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (name.endsWith("Format")) {  // NOI18N
                initDefaultColor(name.substring(0, name.length() - 6));
            }
        }
        refresh();
    }

    public void refresh() {
        String string = SvnModuleConfig.getDefault().getAnnotationFormat(); //System.getProperty("netbeans.experimental.svn.ui.statusLabelFormat");  // NOI18N
        if (string != null && !string.trim().equals("")) { // NOI18N
            mimeTypeFlag = string.indexOf("{mime_type}") > -1;
            string = SvnUtils.createAnnotationFormat(string);
            if (!SvnUtils.isAnnotationFormatValid(string)) {
                Subversion.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults");
                string = org.openide.util.NbBundle.getMessage(Annotator.class, "Annotator.defaultFormat"); // NOI18N
                mimeTypeFlag = string.contains("{3}");
            }
            format = new MessageFormat(string);
            emptyFormat = format.format(new String[] {"", "", "", ""} , new StringBuffer(), null).toString().trim();
        }
    }

    private void initDefaultColor(String name) {
        String color = System.getProperty("svn.color." + name);  // NOI18N
        if (color == null) return;
        setAnnotationColor(name, color);
    }

    /**
     * Changes annotation color of files.
     *
     * @param name name of the color to change. Can be one of:
     * newLocally, addedLocally, modifiedLocally, removedLocally, deletedLocally, newInRepository, modifiedInRepository,
     * removedInRepository, conflict, mergeable, excluded.
     * @param colorString new color in the format: 4455AA (RGB hexadecimal)
     */
    private void setAnnotationColor(String name, String colorString) {
        try {
            Field field = Annotator.class.getDeclaredField(name + "Format");  // NOI18N
            MessageFormat format = new MessageFormat("<font color=\"" + colorString + "\">{0}</font><font color=\"#999999\">{1}</font>");  // NOI18N
            field.set(null, format);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color name");  // NOI18N
        }
    }

    /**
     * Adds rendering attributes to an arbitrary String based on a SVN status. The name is usually a file or folder
     * display name and status is usually its SVN status as reported by FileStatusCache.
     *
     * @param name name to annotate
     * @param info status that an object with the given name has
     * @param file file this annotation belongs to. It is used to determine sticky tags for textual annotations. Pass
     * null if you do not want textual annotations to appear in returned markup
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */
    public String annotateNameHtml(String name, FileInformation info, File file) {
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping annotateNameHtml due to missing client");
            return name;
        }
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky = SvnUtils.getCopy(file);
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                    textAnnotation = "";  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if (sticky == null) {
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) {
                        textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = "";
                    }
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation);
        }

        // aligned with SvnUtils.getComparableStatus

        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return conflictFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return mergeableFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return deletedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return removedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return addedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return modifiedLocallyFormat.format(new Object [] { name, textAnnotation });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return removedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return newInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return modifiedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    /**
     * Applies custom format.
     */
    private String formatAnnotation(FileInformation info, File file) {
        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }

        String revisionString = "";     // NOI18N
        String binaryString = "";       // NOI18N


        ISVNStatus snvStatus = info.getEntry(file);
        if (snvStatus != null) {
            SVNRevision rev = snvStatus.getRevision();
            revisionString = rev != null ? rev.toString() : "";
            if(mimeTypeFlag) {
                binaryString = getMimeType(file);
            }
        }

        String stickyString = SvnUtils.getCopy(file);
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            binaryString
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return "";
        } else {
            return " " + annotation;
        }
    }

    private String getMimeType(File file) {
        try {
            SvnClient client = Subversion.getInstance().getClient(false);
            ISVNProperty prop = client.propertyGet(file, ISVNProperty.MIME_TYPE);
            if(prop != null) {
                String mime = prop.getValue();
                return mime != null ? mime : "";
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
            return "";
        }
        return "";
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {

            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky;
                ISVNStatus lstatus = info.getEntry(file);
                if (lstatus != null && lstatus.getUrl() != null) {
                    sticky = SvnUtils.getCopy(lstatus.getUrl());
                } else {
                    // slower
                    sticky = SvnUtils.getCopy(file);
                }

                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                    textAnnotation = ""; // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else  if (sticky == null) {
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) { // NOI18N
                        textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = ""; // NOI18N
                    }
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); // NOI18N
        }

        if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MERGE)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }

    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }

    public String annotateNameHtml(String name, VCSContext context, int includeStatus) {
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping annotateNameHtml due to missing client");
            return name;
        }
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;

        for (File file : context.getRootFiles()) {
            if (SvnUtils.isPartOfSubversionMetadata(file)) {
                // no need to handle .svn files, eliminates some warnings as 'no repository url found for managed file .svn'
                // happens e.g. when annotating a Project folder
                continue;
            }
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                // status not in cache, plan refresh
                File parentFile = file.getParentFile();
                Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                cache.refreshAsync(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ?
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) :
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return SvnUtils.getComparableStatus(a.getStatus()) < SvnUtils.getComparableStatus(b.getStatus());
    }

    String annotateName(String name, Set files) {
        return null;
    }

    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on the supplied context.
     *
     * @param ctx context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}
     * @param destination
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */
    public static Action [] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        List<Action> actions = new ArrayList<Action>(20);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(SystemAction.get(CheckoutAction.class));
            actions.add(SystemAction.get(ImportAction.class));
            actions.add(SystemAction.get(RelocateAction.class));
            actions.add(null);
            actions.add(SystemAction.get(UpdateWithDependenciesAction.class));
            actions.add(null);
            actions.add(SystemAction.get(StatusAction.class));
            actions.add(SystemAction.get(DiffAction.class));
            actions.add(SystemAction.get(UpdateAction.class));
            actions.add(SystemAction.get(CommitAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ExportDiffAction.class));
            actions.add(SystemAction.get(PatchAction.class));
            actions.add(null);
            actions.add(SystemAction.get(CreateCopyAction.class));
            actions.add(SystemAction.get(SwitchToAction.class));
            actions.add(SystemAction.get(MergeAction.class));
            actions.add(null);
            actions.add(SystemAction.get(BlameAction.class));
            actions.add(SystemAction.get(SearchHistoryAction.class));
            actions.add(null);
            actions.add(SystemAction.get(RevertModificationsAction.class));
            actions.add(SystemAction.get(ResolveConflictsAction.class));
            actions.add(SystemAction.get(IgnoreAction.class));
            actions.add(null);
            actions.add(SystemAction.get(SvnPropertiesAction.class));
        } else {
            ResourceBundle loc = NbBundle.getBundle(Annotator.class);
            File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
            Lookup context = ctx.getElements();
            boolean noneVersioned = isNothingVersioned(files);
            if (noneVersioned) {
                actions.add(SystemActionBridge.createAction(SystemAction.get(ImportAction.class).createContextAwareInstance(context), loc.getString("CTL_PopupMenuItem_Import"), context));
            } else {
                Node[] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
                boolean onlyFolders = onlyFolders(files);
                boolean onlyProjects = onlyProjects(nodes);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update"), context));
                if (onlyProjects) {
                    actions.add(new SystemActionBridge(SystemAction.get(UpdateWithDependenciesAction.class), loc.getString("CTL_PopupMenuItem_UpdateWithDeps")));
                }
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(CreateCopyAction.class), loc.getString("CTL_PopupMenuItem_Copy"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(SwitchToAction.class), loc.getString("CTL_PopupMenuItem_Switch"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(MergeAction.class), loc.getString("CTL_PopupMenuItem_Merge"), context));
                actions.add(null);
                if (!onlyFolders) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(BlameAction.class),
                                                                ((BlameAction)SystemAction.get(BlameAction.class)).visible(nodes) ?
                                                                        loc.getString("CTL_PopupMenuItem_HideAnnotations") :
                                                                        loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context));
                }
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(RevertModificationsAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context));
                if (!onlyProjects) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                                                                ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(nodes) == IgnoreAction.UNIGNORING ?
                                                                        loc.getString("CTL_PopupMenuItem_Unignore") :
                                                                        loc.getString("CTL_PopupMenuItem_Ignore"), context));
                }
                actions.add(null);
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(SvnPropertiesAction.class),
                                loc.getString("CTL_PopupMenuItem_Properties"), context));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static boolean isNothingVersioned(File[] files) {
        for (File file : files) {
            if (SvnUtils.isManaged(file)) return false;
        }
        return true;
    }

    private static boolean onlyProjects(Node[] nodes) {
        if (nodes == null || nodes.length == 0) return false;
        for (Node node : nodes) {
            if (node.getLookup().lookup(Project.class) == null) return false;
        }
        return true;
    }

    private static boolean onlyFolders(File[] files) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        boolean onlyFolders = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            FileInformation status = cache.getCachedStatus(files[i]);
            if (status == null) {
                onlyFolders = false; // be optimistic, this can be a file
            } else if (!files[i].exists() && !status.isDirectory()) {
                onlyFolders = false;
                break;
            }
        }
        return onlyFolders;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(Annotator.class, key);
        return new MessageFormat(format);
    }

    private static final int STATUS_BADGEABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public Image annotateIcon(Image icon, VCSContext context, int includeStatus) {
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping annotateIcon due to missing client");
            return null;
        }
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon, includeStatus);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon, int includeStatus) {
        FileInformation mostImportantInfo = null;

        List<File> filesToRefresh = new LinkedList<File>();
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                File parentFile = file.getParentFile();
                Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                filesToRefresh.add(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        cache.refreshAsync(filesToRefresh);

        if(mostImportantInfo == null) return null;
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            statusText = conflictTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            statusText = mergeableTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            statusText = deletedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            statusText = removedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            statusText = newLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            statusText = addedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            statusText = modifiedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            statusText = removedInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            statusText = newInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            statusText = modifiedInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            statusText = null;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            statusText = excludedTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            statusText = null;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            statusText = null;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null; // NOI18
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        List<File> filesToRefresh = new LinkedList<File>();
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                filesToRefresh.add(file);
            } else if ((info.getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        cache.refreshAsync(filesToRefresh);
        
        if (!isVersioned) {
            return null;
        }
        
        IconSelector sc = new IconSelector(context.getRootFiles(), icon);
        // return the icon as soon as possible and schedule a complete scan if needed
        sc.scanFilesLazy();
        return sc.getBadge();
    }

    /**
     * Returns modified files from tha cache.
     * @param changed if not null, returns cached modified files and changed[0] denotes if the returned values are outdated.
     * If null, performs the complete scan which may access I/O
     * @return
     */
    private Map<File, FileInformation> getLocallyChangedFiles (final boolean changed[]) {
        Map<File, FileInformation> map;
        if (changed != null) {
            // return cached values
            map = cache.getAllModifiedFilesCached(changed);
        } else {
            // perform complete scan if needed
            map = cache.getAllModifiedFiles();
        }
        Map<File, FileInformation> m = null;
        synchronized (allModifiedFiles) {
            for (Map<File, FileInformation> sm : allModifiedFiles) {
                m = sm;
                break;
            }
            if (modifiedFiles == null || map != m) {
                allModifiedFiles.clear();
                allModifiedFiles.add(map);
                modifiedFiles = new HashMap<File, FileInformation>();
                for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                    File file = (File) i.next();
                    FileInformation info = map.get(file);
                    if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                        modifiedFiles.put(file, info);
                    }
                }
            }
            return modifiedFiles;
        }
    }

    /**
     * A task which performs a complete modified files scan, reevaluates all registered icon selectors
     * and fires events if a wrong folder badge should be repainted
     */
    private class ModifiedFilesScanTask implements Runnable {

        private final LinkedList<IconSelector> scanners;

        public ModifiedFilesScanTask() {
            scanners = new LinkedList<IconSelector>();
        }

        public void run() {
            LinkedList<IconSelector> toScan;
            synchronized (scanners) {
                toScan = new LinkedList<IconSelector>(scanners);
                scanners.clear();
            }
            // complete modified files scan
            Map<File, FileInformation> modifiedFiles = getLocallyChangedFiles(null);
            Set<File> filesToRefresh = new HashSet<File>();
            for (IconSelector scanner : toScan) {
                // all registered iconn selectors are re-evaluated
                scanner.scanFiles(modifiedFiles);
                filesToRefresh.addAll(scanner.getFilesToRefresh());
            }
            // fire an event if needed
            if (filesToRefresh.size() > 0) {
                Subversion.getInstance().refreshAnnotations(filesToRefresh.toArray(new File[filesToRefresh.size()]));
            }
        }

        /**
         * Registers a given badge selector and reschedules this task
         *@param scanner
         */
        public void schedule(IconSelector scanner) {
            synchronized (scanners) {
                scanners.add(scanner);
                modifiedFilesRPScanTask.schedule(1000);
            }
        }
    }

    /**
     * Evaluates root files' status and return their common badge. It tries to evaluate as fast as possible so it can return a fake badge.
     *
     * If cached all modified files, which it uses in the evaluation, are outdated, it schedules a complete scan of those files (which may access I/O)
     * With these freshly scanned files it recalculates the icon and if that differs from the one returned after the first scan,
     * the instance method getFilesToRefresh returns a non-empty set of responsible files which should be refreshed.
     */
    private final class IconSelector {

        private Set<File> rootFiles;
        private final Image initialIcon;
        private Image badge = null;
        private String badgePath;
        private String originalBadgePath;
        private final Set<File> responsibleFiles;
        boolean allExcluded;
        boolean modified;

        public IconSelector(Set<File> rootFiles, Image initialIcon) {
            this.rootFiles = rootFiles;
            this.initialIcon = initialIcon;
            this.responsibleFiles = new HashSet<File>();
        }

        void scanFilesLazy() {
            boolean changed[] = new boolean[1];
            Map<File, FileInformation> locallyChangedFiles = getLocallyChangedFiles(changed);
            scanFiles(locallyChangedFiles);
            if (changed[0]) {
                // schedule a scan
                scheduleDeepScan();
            }
        }

        /**
         * Computes the badge for given root files.
         * Iterates through all root files and check if any of its children is by any chance included in a map of modified files.
         * If it finds any such child, it sets the badge to modified (or conflicted if any of rootfile's children is in conflict).
         * @param locallyChangedFiles
         */
        private void scanFiles(Map<File, FileInformation> locallyChangedFiles) {
            allExcluded = true;
            modified = false;
            SvnModuleConfig config = SvnModuleConfig.getDefault();
            responsibleFiles.clear();
            for (File file : rootFiles) {
                for (Map.Entry<File, FileInformation> entry : locallyChangedFiles.entrySet()) {
                    File mf = entry.getKey();
                    if (mf == null) {
                        Subversion.LOG.log(Level.WARNING, "null File entry returned from getAllModifiedFiles");
                        continue;
                    }
                    FileInformation info = entry.getValue();
                    int status = info.getStatus();
                    if (VersioningSupport.isFlat(file)) {
                        if (mf.getParentFile().equals(file)) {
                            if (info.isDirectory()) {
                                continue;
                            }
                            if (checkConflictAndUpdateFlags(mf, config, status)) {
                                return;
                            }
                        }
                    } else {
                        if (Utils.isAncestorOrEqual(file, mf)) {
                            if ((status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) && file.equals(mf)) {
                                continue;
                            }
                            if (checkConflictAndUpdateFlags(mf, config, status)) {
                                return;
                            }
                        }
                    }
                }
            }

            if (modified && !allExcluded) {
                badge = ImageUtilities.assignToolTipToImage(
                        ImageUtilities.loadImage(badgePath = badgeModified, true), toolTipModified);
                badge = ImageUtilities.mergeImages(initialIcon, badge, 16, 9);
            } else {
                badge = null;
                badgePath = "";
                responsibleFiles.addAll(rootFiles);
            }
        }

        /**
         *
         * @param modifiedFile
         * @param config
         * @param status
         * @return true if the badge should be 'CONFLICT', false otherwise
         */
        private boolean checkConflictAndUpdateFlags(File modifiedFile, SvnModuleConfig config, int status) {
            responsibleFiles.add(modifiedFile);
            // conflict - this status has the highest weight
            if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                badge = ImageUtilities.assignToolTipToImage(
                        ImageUtilities.loadImage(badgePath = badgeConflicts, true), toolTipConflict);
                badge = ImageUtilities.mergeImages(initialIcon, badge, 16, 9);
                return true;
            }
            modified = true;
            allExcluded &= config.isExcludedFromCommit(modifiedFile.getAbsolutePath());
            return false;
        }

        Image getBadge() {
            return badge;
        }

        private void scheduleDeepScan() {
            originalBadgePath = badgePath; // save the badge path for later comparison
            modifiedFilesScanTask.schedule(this);
        }

        /**
         * Returns files which are responsible for a badge being changed and whose refresh should result in badge repainting.
         * @return
         */
        public Set<File> getFilesToRefresh() {
            assert originalBadgePath != null; // scan has been already run
            if (!badgePath.equals(originalBadgePath)) {
                // badge has changed
                return responsibleFiles;
            } else {
                return Collections.EMPTY_SET;
            }
        }
    }
}
