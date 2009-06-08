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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.ui.actions.status.StatusAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.CheckoutAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.project.UpdateWithDependenciesAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.project.AddToRepositoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.ignore.IgnoreAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.AnnotationsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ExportDiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.*;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.ExcludeFromCommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.ChangeCVSRootAction;
import org.netbeans.modules.versioning.system.cvss.ui.history.ViewRevisionAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.api.project.Project;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import java.lang.reflect.Field;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.diff.PatchAction;
import org.openide.util.ImageUtilities;

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

    private static String badgeModified = "org/netbeans/modules/versioning/system/cvss/resources/icons/modified-badge.png";
    private static String badgeConflicts = "org/netbeans/modules/versioning/system/cvss/resources/icons/conflicts-badge.png";
    
    private static String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally");
    private static String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");
  
    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED | 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT | FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    
    private final FileStatusCache cache;
    
    private String          lastAnnotationsFormat;
    private MessageFormat   lastMessageFormat;
    private String          lastEmptyAnnotation;

    Annotator(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
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
    }

    private void initDefaultColor(String name) {
        String color = System.getProperty("cvs.color." + name);  // NOI18N
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
     * Adds rendering attributes to an arbitrary String based on a CVS status. The name is usually a file or folder
     * display name and status is usually its CVS status as reported by FileStatusCache. 
     * 
     * @param name name to annotate
     * @param info status that an object with the given name has
     * @param file file this annotation belongs to. It is used to determine sticky tags for textual annotations. Pass
     * null if you do not want textual annotations to appear in returned markup
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */ 
    public String annotateNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            textAnnotation = formatAnnotation(info, file);
            if (textAnnotation.equals(lastEmptyAnnotation)) textAnnotation = ""; // NOI18N
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); 
        }
        
        switch (status) {
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
            return name;
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
            return modifiedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
            return removedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            return deletedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
            return newInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
            return modifiedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
            return removedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
            return addedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MERGE:
            return mergeableFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return conflictFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    private String formatAnnotation(FileInformation info, File file) {
        updateMessageFormat();

        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }

        String revisionString = ""; // NOI18N
        String binaryString = ""; // NOI18N
        Entry entry = info.getEntry(file);
        if (entry != null) {
            revisionString = entry.getRevision();
            binaryString = entry.getOptions();
            if ("-kb".equals(binaryString) == false) { // NOI18N
                binaryString = ""; // NOI18N
            }
        }
        String stickyString = Utils.getSticky(file);
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            binaryString
        };
        return lastMessageFormat.format(arguments, new StringBuffer(), null).toString().trim();
    }

    private void updateMessageFormat() {
        String taf = CvsModuleConfig.getDefault().getPreferences().get(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, CvsModuleConfig.DEFAULT_ANNOTATIONS_FORMAT);
        if (lastMessageFormat == null || !taf.equals(lastAnnotationsFormat)) {
            lastAnnotationsFormat = taf;
            taf = Utils.createAnnotationFormat(taf);
            if (!Utils.isAnnotationFormatValid(taf)) {
                CvsVersioningSystem.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults");
                taf = org.openide.util.NbBundle.getMessage(Annotator.class, "Annotator.defaultFormat"); // NOI18N
            }
            lastMessageFormat = new MessageFormat(taf);
            lastEmptyAnnotation = lastMessageFormat.format(new Object[]{"", "", "", ""}); // NOI18N
        }
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {
            textAnnotation = formatAnnotation(info, file);
            if (textAnnotation.equals(lastEmptyAnnotation)) textAnnotation = ""; // NOI18N
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation);
        }
        
        switch (status) {
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
        case FileInformation.STATUS_VERSIONED_MERGE:
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return name;
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }
    
    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }
    
    public String annotateNameHtml(String name, VCSContext context, int includeStatus) {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !org.netbeans.modules.versioning.util.Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ? 
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) : 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }
    
    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return Utils.getComparableStatus(a.getStatus()) < Utils.getComparableStatus(b.getStatus());
    }

    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on the supplied context.
     *
     * @param ctx context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}   
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */ 
    public static Action [] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        ResourceBundle loc = NbBundle.getBundle(Annotator.class);
        
        List<Action> actions = new ArrayList<Action>(20);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(SystemAction.get(CheckoutAction.class));
            actions.add(SystemAction.get(AddToRepositoryAction.class));
            actions.add(new ChangeCVSRootAction(loc.getString("CTL_MenuItem_ChangeCVSRoot"), ctx));
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
            actions.add(SystemAction.get(TagAction.class));
            actions.add(new BranchesMenu());
            actions.add(null);
            actions.add(SystemAction.get(AnnotationsAction.class));            
            actions.add(new ViewRevisionAction(ctx));
            actions.add(SystemAction.get(SearchHistoryAction.class));
            actions.add(null);
            actions.add(SystemAction.get(GetCleanAction.class));
            actions.add(SystemAction.get(ResolveConflictsAction.class));
            actions.add(SystemAction.get(IgnoreAction.class));
            actions.add(new ExcludeFromCommitAction(ctx));
        } else {
            Lookup context = ctx.getElements();
            File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
            boolean noneVersioned = isNothingVersioned(files);
            if (noneVersioned) {
                actions.add(SystemActionBridge.createAction(SystemAction.get(AddToRepositoryAction.class).createContextAwareInstance(context), loc.getString("CTL_PopupMenuItem_Import"), context));
            } else {
                boolean onlyFolders = onlyFolders(files);
                Node[] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
                boolean onlyProjects = onlyProjects(nodes);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update"), context));
                if (onlyProjects) {
                    actions.add(new SystemActionBridge(SystemAction.get(UpdateWithDependenciesAction.class), loc.getString("CTL_PopupMenuItem_UpdateWithDeps")));
                }
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(TagAction.class), loc.getString("CTL_PopupMenuItem_Tag"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(BranchAction.class), loc.getString("CTL_PopupMenuItem_Branch"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(SwitchBranchAction.class), loc.getString("CTL_PopupMenuItem_SwitchBranch"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(MergeBranchAction.class), loc.getString("CTL_PopupMenuItem_MergeBranch"), context));
                actions.add(null);
                if (!onlyFolders) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(AnnotationsAction.class), 
                                                                ((AnnotationsAction)SystemAction.get(AnnotationsAction.class)).visible(nodes) ? 
                                                                        loc.getString("CTL_PopupMenuItem_HideAnnotations") : 
                                                                        loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context));            
                }
                actions.add(new ViewRevisionAction(loc.getString("CTL_PopupMenuItem_ViewRevision"), ctx)); // NOI18N
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(GetCleanAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context));
                if (!onlyProjects) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                                                                ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(nodes) == IgnoreAction.UNIGNORING ? 
                                                                        loc.getString("CTL_PopupMenuItem_Unignore") : 
                                                                        loc.getString("CTL_PopupMenuItem_Ignore"), context));
                }
                actions.add(new ExcludeFromCommitAction(ctx));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static boolean isNothingVersioned(File[] files) {
        for (File file : files) {
            if (CvsVersioningSystem.isManaged(file)) return false;
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
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            FileInformation status = cache.getCachedStatus(files[i]);
            if (status == null // be optimistic, this can be a file
                        || (!files[i].exists() && !status.isDirectory())) {
                return false;
            }
        }
        return true;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(Annotator.class, key);
        return new MessageFormat(format);
    }

    private static final int STATUS_BADGEABLE = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    
    public Image annotateIcon(Image icon, VCSContext context, int includeStatus) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !org.netbeans.modules.versioning.util.Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon, includeStatus);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon, int includeStatus) {
        FileInformation mostImportantInfo = null;
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        if(mostImportantInfo == null) return null; 
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        switch (status) {
            case FileInformation.STATUS_UNKNOWN:
                break;
            case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
                statusText = null;
                break;
            case FileInformation.STATUS_VERSIONED_UPTODATE:
                statusText = null;
                break;
            case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
                statusText = modifiedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                statusText = newLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                statusText = removedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
                statusText = deletedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
                statusText = newInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
                statusText = modifiedInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
                statusText = removedInRepositoryTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
                statusText = addedLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_MERGE:
                statusText = mergeableTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_CONFLICT:
                statusText = conflictTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
                statusText = excludedTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null;
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        boolean isVersioned = false;
        for (Iterator<File> i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = i.next();
            if ((cache.getStatus(file).getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        boolean allExcluded = true;
        boolean modified = false;
        Map<File, FileInformation> map = cache.getAllModifiedFiles();
        Map<File, FileInformation> modifiedFiles = new HashMap<File, FileInformation>();
        for (Map.Entry<File, FileInformation> entry : map.entrySet()) {
            FileInformation info = entry.getValue();
            if (!info.isDirectory() && (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                modifiedFiles.put(entry.getKey(), info);
            }
        }
        for (Iterator<File> i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = i.next();
            if (VersioningSupport.isFlat(file)) {
                for (Iterator<File> j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = modifiedFiles.get(mf);
                        if (info.isDirectory()) {
                            continue;
                        }
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict); // NOI18N
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf);
                    }
                }
            } else {
                for (Iterator<File> j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = j.next();
                    if (Utils.isParentOrEqual(file, mf)) {
                        FileInformation info = modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict); // NOI18N
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf);
                    }
                }
            }
        }
        if (modified && !allExcluded) {
            Image badge = ImageUtilities.assignToolTipToImage(
                    ImageUtilities.loadImage(badgeModified, true), toolTipModified); // NOI18N
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }

}
