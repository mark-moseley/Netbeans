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
package org.netbeans.modules.mercurial;

import java.util.regex.Matcher;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.clone.CloneExternalAction;
import org.netbeans.modules.mercurial.ui.create.CreateAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.api.project.Project;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.Utils;
import javax.swing.*;
import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.reflect.Field;
import org.netbeans.modules.mercurial.ui.annotate.AnnotateAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffChangesAction;
import org.netbeans.modules.mercurial.ui.diff.ImportDiffAction;
import org.netbeans.modules.mercurial.ui.ignore.IgnoreAction;
import org.netbeans.modules.mercurial.ui.log.LogAction;
import org.netbeans.modules.mercurial.ui.properties.PropertiesAction;
import org.netbeans.modules.mercurial.ui.pull.FetchAction;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.modules.mercurial.ui.update.ConflictResolvedAction;
import org.netbeans.modules.mercurial.ui.update.ResolveConflictsAction;
import org.netbeans.modules.mercurial.ui.update.UpdateAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakSet;

/**
 * Responsible for coloring file labels and file icons in the IDE and providing IDE with menu items.
 *
 * @author Maros Sandor
 */
public class MercurialAnnotator extends VCSAnnotator {
    
    private static final int INITIAL_ACTION_ARRAY_LENGTH = 25;
    private static MessageFormat uptodateFormat = getFormat("uptodateFormat");  // NOI18N
    private static MessageFormat newLocallyFormat = getFormat("newLocallyFormat");  // NOI18N
    private static MessageFormat addedLocallyFormat = getFormat("addedLocallyFormat"); // NOI18N
    private static MessageFormat modifiedLocallyFormat = getFormat("modifiedLocallyFormat"); // NOI18N
    private static MessageFormat removedLocallyFormat = getFormat("removedLocallyFormat"); // NOI18N
    private static MessageFormat deletedLocallyFormat = getFormat("deletedLocallyFormat"); // NOI18N
    private static MessageFormat excludedFormat = getFormat("excludedFormat"); // NOI18N
    private static MessageFormat conflictFormat = getFormat("conflictFormat"); // NOI18N

    private static MessageFormat newLocallyTooltipFormat = getFormat("newLocallyTooltipFormat");  // NOI18N
    private static MessageFormat addedLocallyTooltipFormat = getFormat("addedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat modifiedLocallyTooltipFormat = getFormat("modifiedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat removedLocallyTooltipFormat = getFormat("removedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat deletedLocallyTooltipFormat = getFormat("deletedLocallyTooltipFormat"); // NOI18N
    private static MessageFormat excludedTooltipFormat = getFormat("excludedTooltipFormat"); // NOI18N
    private static MessageFormat conflictTooltipFormat = getFormat("conflictTooltipFormat"); // NOI18N
    
    private static final int STATUS_TEXT_ANNOTABLE = 
            FileInformation.STATUS_NOTVERSIONED_EXCLUDED |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    
    private static final int STATUS_BADGEABLE = 
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    private static int STATUS_IS_IMPORTANT =
                        FileInformation.STATUS_VERSIONED_UPTODATE |
                        FileInformation.STATUS_LOCAL_CHANGE |
                        FileInformation.STATUS_NOTVERSIONED_EXCLUDED;

    public static String ANNOTATION_STATUS      = "status"; // NOI18N
    public static String ANNOTATION_FOLDER      = "folder"; // NOI18N

    public static String[] LABELS = new String[] {ANNOTATION_STATUS, ANNOTATION_FOLDER};

    private FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;
    private File folderToScan;
    private ConcurrentLinkedQueue<File> dirsToScan = new ConcurrentLinkedQueue<File>();
    private Map<File, FileInformation> modifiedFiles = null;
    private RequestProcessor.Task scanTask;
    private static final RequestProcessor rp = new RequestProcessor("MercurialAnnotateScan", 1, true); // NOI18N

    private static String badgeModified = "org/netbeans/modules/mercurial/resources/icons/modified-badge.png";
    private static String badgeConflicts = "org/netbeans/modules/mercurial/resources/icons/conflicts-badge.png";
    private static String toolTipModified = "<img src=\"" + MercurialAnnotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(MercurialAnnotator.class, "MSG_Contains_Modified_Locally");
    private static String toolTipConflict = "<img src=\"" + MercurialAnnotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(MercurialAnnotator.class, "MSG_Contains_Conflicts");

    private WeakSet<Map<File, FileInformation>> allModifiedFiles = new WeakSet<Map<File, FileInformation>>(1);

    public MercurialAnnotator() {
        cache = Mercurial.getInstance().getFileStatusCache();
        scanTask = rp.create(new ScanTask());
        initDefaults();
    }
    
    private void initDefaults() {
        Field [] fields = MercurialAnnotator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (name.endsWith("Format")) {  // NOI18N
                initDefaultColor(name.substring(0, name.length() - 6));
            }
        }
        refresh();
    }

    public void refresh() {
        String string = HgModuleConfig.getDefault().getAnnotationFormat();
        if (string != null && !string.trim().equals("")) { // NOI18N
            string = HgUtils.createAnnotationFormat(string);
            if (!HgUtils.isAnnotationFormatValid(string))   {
                // see #136440
                Mercurial.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults");
                string = org.openide.util.NbBundle.getMessage(MercurialAnnotator.class, "MercurialAnnotator.defaultFormat"); // NOI18N
            }
            format = new MessageFormat(string);
            emptyFormat = format.format(new String[] {"", "", ""} , new StringBuffer(), null).toString().trim(); // NOI18N
        }
    }
    

    private void initDefaultColor(String name) {
        String color = System.getProperty("hg.color." + name);  // NOI18N
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
            Field field = MercurialAnnotator.class.getDeclaredField(name + "Format");  // NOI18N
            MessageFormat format = new MessageFormat("<font color=\"" + colorString + "\">{0}</font><font color=\"#999999\">{1}</font>");  // NOI18N
            field.set(null, format);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color name");  // NOI18N
        }
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(MercurialAnnotator.class, key);
        return new MessageFormat(format);
    }
    
    public String annotateName(String name, VCSContext context) {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
                
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                File parentFile = file.getParentFile();
                Mercurial.LOG.log(Level.FINE, "null cached status for: {0} {1} {2}", new Object[] {file, folderToScan, parentFile});
                if (!Mercurial.getInstance().isRefreshScheduled(parentFile)) {
                    folderToScan = parentFile;
                    reScheduleScan(1000);
                }
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & STATUS_IS_IMPORTANT) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }
        
        if (mostImportantInfo == null) return null;
        return folderAnnotation ?
            annotateFolderNameHtml(name, context, mostImportantInfo, mostImportantFile) :
            annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }
                
    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }
        
        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon) throws IllegalArgumentException {
        FileInformation mostImportantInfo = null;
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                File parentFile = file.getParentFile();
                Mercurial.LOG.log(Level.FINE, "null cached status for: {0} {1} {2}", new Object[]{file, folderToScan, parentFile});
                if (!Mercurial.getInstance().isRefreshScheduled(parentFile)) {
                    folderToScan = parentFile;
                    reScheduleScan(1000);
                }
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & STATUS_IS_IMPORTANT) == 0) {
                continue;
            }
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        if(mostImportantInfo == null) return null; 
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            statusText = excludedTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            statusText = deletedLocallyTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            statusText = removedLocallyTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            statusText = newLocallyTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            statusText = addedLocallyTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            statusText = modifiedLocallyTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            statusText = null;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            statusText = conflictTooltipFormat.format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            statusText = null;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            statusText = null;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null;
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            // There is an assumption here that annotateName was already
            // called and FileStatusCache.getStatus was scheduled if
            // FileStatusCache.getCachedStatus returned null.
            FileInformation info = cache.getCachedStatus(file);
            if (info != null && (info.getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        HgModuleConfig config = HgModuleConfig.getDefault();
        boolean allExcluded = true;
        boolean modified = false;
        Map<File, FileInformation> locallyChangedFiles = getLocallyChangedFiles();
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if (VersioningSupport.isFlat(file)) {
                for (Iterator j = locallyChangedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = locallyChangedFiles.get(mf);
                        if (info.isDirectory()) {
                            continue;
                        }
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict);
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            } else {
                for (Iterator j = locallyChangedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (Utils.isAncestorOrEqual(file, mf)) {
                        FileInformation info = locallyChangedFiles.get(mf);
                        int status = info.getStatus();
                        if ((status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) && file.equals(mf)) {
                            continue;
                        }
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict);
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }
        if (modified && !allExcluded) {
            Image badge = ImageUtilities.assignToolTipToImage(
                    ImageUtilities.loadImage(badgeModified, true), toolTipModified);
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }

    private synchronized Map<File, FileInformation> getLocallyChangedFiles() {
        Map<File, FileInformation> map = cache.getAllModifiedFiles();
        Map<File, FileInformation> m = null;
        for (Map<File, FileInformation> sm : allModifiedFiles) {
            m = sm;
            break;
        }
        if(modifiedFiles == null || map != m) {
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

    public Action[] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        // TODO: get resource strings for all actions:
        ResourceBundle loc = NbBundle.getBundle(MercurialAnnotator.class);
        Node [] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        File root = HgUtils.getRootFile(ctx);
        boolean noneVersioned = root == null;
        boolean onlyFolders = onlyFolders(files);
        boolean onlyProjects = onlyProjects(nodes);

        List<Action> actions = new ArrayList<Action>(INITIAL_ACTION_ARRAY_LENGTH);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(new CreateAction(loc.getString("CTL_MenuItem_Create"), ctx)); // NOI18N
            actions.add(null);
            actions.add(new StatusAction(loc.getString("CTL_PopupMenuItem_Status"), ctx)); // NOI18N
            actions.add(new DiffAction(loc.getString("CTL_PopupMenuItem_Diff"), ctx)); // NOI18N
            actions.add(new UpdateAction(loc.getString("CTL_PopupMenuItem_Update"), ctx)); // NOI18N
            actions.add(new CommitAction(loc.getString("CTL_PopupMenuItem_Commit"), ctx)); // NOI18N
            actions.add(null);
            actions.add(new ExportDiffAction(loc.getString("CTL_PopupMenuItem_ExportDiff"), ctx)); // NOI18N
            actions.add(new ExportDiffChangesAction(loc.getString("CTL_PopupMenuItem_ExportDiffChanges"), ctx)); // NOI18N
            actions.add(new ImportDiffAction(loc.getString("CTL_PopupMenuItem_ImportDiff"), ctx)); // NOI18N

            actions.add(null);
            if (root != null) {
                actions.add(new CloneAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_CloneLocal",  // NOI18N
                        root.getName()), ctx));
            }
            actions.add(new CloneExternalAction(loc.getString("CTL_PopupMenuItem_CloneOther"), ctx));     // NOI18N        
            actions.add(new FetchAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_FetchLocal"), ctx)); // NOI18N
            actions.add(new ShareMenu(ctx)); 
            actions.add(new MergeMenu(ctx, false));                 
            actions.add(null);
            actions.add(new LogAction(loc.getString("CTL_PopupMenuItem_Log"), ctx)); // NOI18N
            if (!onlyProjects  && !onlyFolders) {
                AnnotateAction tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_ShowAnnotations"), ctx); // NOI18N

                if (tempA.visible(nodes)) {
                    actions.add(new ShowMenu(ctx, true, true));
                } else {
                    actions.add(new ShowMenu(ctx, true, false));
                }
            }else{
                actions.add(new ShowMenu(ctx, false, false));
            }
            actions.add(null);
            actions.add(new RevertModificationsAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Revert"), ctx)); // NOI18N
            actions.add(new RecoverMenu(ctx)); 
            if (!onlyProjects && !onlyFolders) {
                IgnoreAction tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Ignore"), ctx); // NOI18N
                actions.add(tempIA);
            }
            actions.add(null);
            actions.add(new PropertiesAction(loc.getString("CTL_PopupMenuItem_Properties"), ctx)); // NOI18N
        } else {
            if (noneVersioned){
                actions.add(new CreateAction(loc.getString("CTL_PopupMenuItem_Create"), ctx)); // NOI18N
            }else{
                actions.add(new StatusAction(loc.getString("CTL_PopupMenuItem_Status"), ctx)); // NOI18N
                actions.add(new DiffAction(loc.getString("CTL_PopupMenuItem_Diff"), ctx)); // NOI18N
                actions.add(new CommitAction(loc.getString("CTL_PopupMenuItem_Commit"), ctx)); // NOI18N
                actions.add(null);
                actions.add(new ResolveConflictsAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_Resolve"), ctx)); // NOI18N
                if (!onlyProjects  && !onlyFolders) {
                    actions.add(new ConflictResolvedAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_MarkResolved"), ctx)); // NOI18N
                }
                actions.add(null);                

                if (!onlyProjects  && !onlyFolders) {
                    AnnotateAction tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_ShowAnnotations"), ctx);  // NOI18N
                    if (tempA.visible(nodes)) {
                        tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_HideAnnotations"), ctx);  // NOI18N
                    }
                    actions.add(tempA);
                }
                actions.add(new LogAction(loc.getString("CTL_PopupMenuItem_Log"), ctx)); // NOI18N
                actions.add(null);
                actions.add(new RevertModificationsAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_Revert"), ctx)); // NOI18N
                if (!onlyProjects  && !onlyFolders) {
                    IgnoreAction tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Ignore"), ctx);  // NOI18N
                    actions.add(tempIA);
                }
                actions.add(null);
                actions.add(new PropertiesAction(loc.getString("CTL_PopupMenuItem_Properties"), ctx)); // NOI18N
            }
        }
        return actions.toArray(new Action[actions.size()]);
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

        //String stickyString = SvnUtils.getCopy(file);
        String stickyString = null;
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            statusString,
            stickyString,
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return ""; // NOI18N
        } else {
            return " " + annotation; // NOI18N
        }
    }

    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }

    public String annotateNameHtml(String name, FileInformation mostImportantInfo, File mostImportantFile) {
        // Hg: The codes used to show the status of files are:
        // M = modified
        // A = added
        // R = removed
        // C = clean
        // ! = deleted, but still tracked
        // ? = not tracked
        // I = ignored (not shown by default)
        
        name = htmlEncode(name);
        
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        int status = mostImportantInfo.getStatus();
        
        if (annotationsVisible && mostImportantFile != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(mostImportantInfo, mostImportantFile);
            } else {
                //String sticky = SvnUtils.getCopy(mostImportantFile);
                String sticky = null;
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                    textAnnotation = "";  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if (sticky == null) {
                    String statusText = mostImportantInfo.getShortStatusText();
                    if(!statusText.equals("")) { // NOI18N
                        textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = ""; // NOI18N
                    }
                } else {
                    textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }

        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(MercurialAnnotator.class, "textAnnotation", textAnnotation); // NOI18N
        }

        if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
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
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return conflictFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }
    
    private String annotateFolderNameHtml(String name, VCSContext context, FileInformation mostImportantInfo, File mostImportantFile) {
        String nameHtml = htmlEncode(name);
        if (mostImportantInfo.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED){
            return excludedFormat.format(new Object [] { nameHtml, ""}); // NOI18N
        }
        String fileName = mostImportantFile.getName();
        if (fileName.equals(name)){
            return uptodateFormat.format(new Object [] { nameHtml, "" }); // NOI18N
        }

        final Set<File> rootFiles = context.getRootFiles();
        File repo = null;        
        String folderAnotation = null;
        if(rootFiles.size() == 1) {
            File root = null; 
            for (File file : rootFiles) {
                repo = Mercurial.getInstance().getRepositoryRoot(file);
                if(repo == null) {
                    Mercurial.LOG.warning("Couldn't find repository root for file " + file);
                } else {
                    root = file;
                    break;
                }
            }
            // repo = null iff the file' status is actually unversioned, but cache has not yet have the right value
            if (repo == null || !repo.getAbsolutePath().equals(root.getAbsolutePath())) {
                // not from repo root => do not annnotate with folder name 
                return uptodateFormat.format(new Object [] { nameHtml, ""});
            }             
        } else {
        
            // Label top level repository nodes with a repository name label when:
            // Display Name (name) is different from its repo name (repo.getName())        
            File parentFile = null;
            for (File file : rootFiles) {            
                if(parentFile == null) {
                    parentFile = file.getParentFile();
                } else {
                    File p = file.getParentFile();
                    if(p == null || !parentFile.getAbsolutePath().equals(p.getAbsolutePath())) {
                        // not comming from the same parent => do not annnotate with folder name
                        return uptodateFormat.format(new Object [] { nameHtml, ""});
                    }
                }
            }
            for (File file : rootFiles) {            
                repo = Mercurial.getInstance().getRepositoryRoot(file);
                if(repo == null) {
                    Mercurial.LOG.warning("Couldn't find repository root for file " + file);
                } else if (!repo.getAbsolutePath().equals(parentFile.getAbsolutePath())) {
                    // not from repo root => do not annnotate with folder name 
                    return uptodateFormat.format(new Object [] { nameHtml, ""});
                } else {
                    break;
                }
            }
        }

        // file is versioned
        if (repo != null && !repo.getName().equals(name)){
            folderAnotation = repo.getName();
        }

        return uptodateFormat.format(new Object [] { nameHtml, folderAnotation != null ? " [" + folderAnotation + "]" : ""}); // NOI18N
    }
    
    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return getComparableStatus(a.getStatus()) < getComparableStatus(b.getStatus());
    }
    
    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    private static boolean onlyProjects(Node[] nodes) {
        if (nodes == null) return false;
        for (Node node : nodes) {
            if (node.getLookup().lookup(Project.class) == null) return false;
        }
        return true;
    }
    
    private boolean onlyFolders(File[] files) {
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

    private void reScheduleScan(int delayMillis) {
        if (!dirsToScan.contains(folderToScan)) {
            if (!dirsToScan.offer(folderToScan)) {
                Mercurial.LOG.log(Level.FINE, "reScheduleScan failed to add to dirsToScan queue: {0} ", folderToScan);
            }
        }
        scanTask.schedule(delayMillis);
    }

    private class ScanTask implements Runnable {
        public void run() {
            Thread.interrupted();
            File dirToScan = dirsToScan.poll();
            if (dirToScan != null) {
                cache.getScannedFiles(dirToScan, null);
                dirToScan = dirsToScan.peek();
                if (dirToScan != null) {
                    scanTask.schedule(1000);
                }
            }
        }
    }
}
