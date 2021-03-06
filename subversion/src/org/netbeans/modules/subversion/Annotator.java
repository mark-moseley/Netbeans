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
import org.openide.util.Utilities;
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
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.properties.SvnPropertiesAction;
import org.netbeans.modules.subversion.ui.relocate.RelocateAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.diff.PatchAction;
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
    
    private final FileStatusCache cache;
    private MessageFormat format;        
    private String emptyFormat;
            
    private boolean mimeTypeFlag;

    Annotator(Subversion svn) {
        this.cache = svn.getStatusCache();
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
        if (string != null && !string.trim().equals("")) {
            mimeTypeFlag = string.indexOf("{mime_type}") > -1;
            string = string.replaceAll("\\{revision\\}",  "\\{0\\}");           // NOI18N    
            string = string.replaceAll("\\{status\\}",    "\\{1\\}");           // NOI18N
            string = string.replaceAll("\\{folder\\}",    "\\{2\\}");           // NOI18N
            string = string.replaceAll("\\{mime_type\\}", "\\{3\\}");           // NOI18N
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
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
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
            revisionString = snvStatus.getRevision().toString();
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
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
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
        ResourceBundle loc = NbBundle.getBundle(Annotator.class);
        Node [] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        Lookup context = ctx.getElements();
        boolean noneVersioned = isNothingVersioned(files);
        boolean onlyFolders = onlyFolders(files);
        boolean onlyProjects = onlyProjects(nodes);
        
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
            if (noneVersioned) {
                actions.add(SystemActionBridge.createAction(SystemAction.get(ImportAction.class).createContextAwareInstance(context), loc.getString("CTL_PopupMenuItem_Import"), context));
            } else {
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
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (File file : files) {
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) return false;
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
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            if (!files[i].exists() && !cache.getStatus(files[i]).isDirectory()) return false;
        }
        return true;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(Annotator.class, key);
        return new MessageFormat(format);  
    }

    private static final int STATUS_BADGEABLE = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;
    
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
            return null;
        }

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if ((cache.getStatus(file).getStatus() & STATUS_BADGEABLE) != 0) {  
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) return null;
        
        SvnModuleConfig config = SvnModuleConfig.getDefault();
        boolean allExcluded = true;
        boolean modified = false;

        Map map = cache.getAllModifiedFiles();
        Map<File, FileInformation> modifiedFiles = new HashMap<File, FileInformation>();
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) map.get(file);
            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) modifiedFiles.put(file, info);
        }

        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if (VersioningSupport.isFlat(file)) {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        if (info.isDirectory()) continue;
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/conflicts-badge.png", true);  // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            } else {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (Utils.isAncestorOrEqual(file, mf)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if ((status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) && file.equals(mf)) {
                            continue;
                        }
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/conflicts-badge.png", true); // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }

        if (modified && !allExcluded) {
            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/modified-badge.png", true); // NOI18N
            return Utilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }

}
