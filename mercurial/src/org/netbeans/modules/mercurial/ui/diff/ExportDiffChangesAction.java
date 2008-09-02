/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.logging.Level;
import org.netbeans.modules.proxy.Base64Encoder;

/**
 * Exports diff to file:
 *
 * <ul>
 * <li>for components that implements {@link DiffSetupSource} interface
 * exports actually displayed diff.
 *
 * <li>for DataNodes <b>local</b> differencies between the current
 * working copy and BASE repository version.
 * </ul>
 *  
 * @author Petr Kuzel
 */
public class ExportDiffChangesAction extends ContextAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private final VCSContext context;

    public ExportDiffChangesAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled () {
        if(HgUtils.getRootFile(context) == null) {
            return false;
        }  
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            return true;
        }
        return super.isEnabled() && Lookup.getDefault().lookup(DiffProvider.class) != null;                
    }

    public void performAction(ActionEvent e) {
        boolean noop;
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            noop = ((DiffSetupSource) activated).getSetups().isEmpty();
        } else {
            File [] files = HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            noop = files.length == 0;
        }
        if (noop) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3001"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffChangesAction.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportDiffChangesAction.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }
        chooser.setCurrentDirectory(new File(HgModuleConfig.getDefault().getPreferences().get("ExportDiff.saveFolder", System.getProperty("user.home")))); // NOI18N
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(ExportDiffChangesAction.class, "BK3002");
            }
        });
        
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);        
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffChangesAction.class, "MNE_Export_ExportAction").charAt(0));
        chooser.setApproveButtonText(NbBundle.getMessage(ExportDiffChangesAction.class, "CTL_Export_ExportAction"));
        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(ExportDiffChangesAction.class, "CTL_Export_Title"));
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File destination = chooser.getSelectedFile();
                    String name = destination.getName();
                    boolean requiredExt = false;
                    requiredExt |= name.endsWith(".diff");  // NOI18N
                    requiredExt |= name.endsWith(".dif");   // NOI18N
                    requiredExt |= name.endsWith(".patch"); // NOI18N
                    if (requiredExt == false) {
                        File parent = destination.getParentFile();
                        destination = new File(parent, name + ".patch"); // NOI18N
                    }

                    if (destination.exists()) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3005", destination.getAbsolutePath()));
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                            return;
                        }
                    }
                    HgModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", destination.getParent()); // NOI18N
                    final File out = destination;
                    File root = HgUtils.getRootFile(context);
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
                    HgProgressSupport ps = new HgProgressSupport() {
                        protected void perform() {
                            OutputLogger logger = getLogger();
                            async(this, context, out, logger);
                        }
                    };
                    ps.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ExportDiffChangesAction.class, "LBL_ExportChanges_Progress"));
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);

    }

    protected boolean asynchronous() {
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private void async(HgProgressSupport progress, VCSContext context, File destination, OutputLogger logger) {
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;
        try {

            // prepare setups and common parent - root

            File root;
            List<Setup> setups;

            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated instanceof DiffSetupSource) {
                setups = new ArrayList<Setup>(((DiffSetupSource) activated).getSetups());
                List<File> setupFiles = new ArrayList<File>(setups.size());
                for (Iterator i = setups.iterator(); i.hasNext();) {
                    Setup setup = (Setup) i.next();
                    setupFiles.add(setup.getBaseFile()); 
                }
                root = getCommonParent(setupFiles.toArray(new File[setupFiles.size()]));
            } else {
                File [] files = HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
                root = getCommonParent(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
                setups = new ArrayList<Setup>(files.length);
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    Mercurial.LOG.log(Level.INFO, "setups {0}", file);
                    Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
                    Mercurial.LOG.log(Level.INFO, "setup {0}", setup.getBaseFile());
                    setups.add(setup);
                }
            }
            if (root == null) {
                NotifyDescriptor nd = new NotifyDescriptor(
                        NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_BadSelection_Prompt"), 
                        NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_BadSelection_Title"), 
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }

            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffChangesAction.class,
                    "MSG_EXPORT_CHANGES_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffChangesAction.class,
                    "MSG_EXPORT_CHANGES_TITLE_SEP")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_EXPORT_CHANGES", destination)); // NOI18N

            String sep = System.getProperty("line.separator"); // NOI18N
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
            out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Following Index: paths are relative to: " + root.getAbsolutePath() + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch can be applied using context Tools: Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# It uses platform neutral UTF-8 encoding and \\n newlines." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N


            Collections.sort(setups, new Comparator<Setup>() {
                public int compare(Setup o1, Setup o2) {
                    return o1.getBaseFile().compareTo(o2.getBaseFile());
                }
            });
            Iterator<Setup> it = setups.iterator();
            int i = 0;
            while (it.hasNext()) {
                Setup setup = it.next();
                File file = setup.getBaseFile();                
                Mercurial.LOG.log(Level.INFO, "setup {0}", file.getName());
                if (file.isDirectory()) continue;
                progress.setDisplayName(file.getName());

                String index = "Index: ";   // NOI18N
                String rootPath = root.getAbsolutePath();
                String filePath = file.getAbsolutePath();
                String relativePath = filePath;
                if (filePath.startsWith(rootPath)) {
                    relativePath = filePath.substring(rootPath.length() + 1).replace(File.separatorChar, '/');
                    index += relativePath + sep;
                    out.write(index.getBytes("utf8")); // NOI18N
                }
                exportDiff(setup, relativePath, out);
                i++;
            }

            exportedFiles = i;
            success = true;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, NbBundle.getMessage(ExportDiffChangesAction.class, "BK3003"), ex);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3004", new Integer(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                } else {
                    Utils.openFile(destination);
                }
            } else {
                destination.delete();
            }
            logger.outputInRed(NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_EXPORT_CHANGES_DONE")); // NOI18N
            logger.output(""); // NOI18N


        }
    }

    private static File getCommonParent(File [] files) {
        File root = files[0];
        if (root.isFile()) root = root.getParentFile();
        for (int i = 1; i < files.length; i++) {
            root = Utils.getCommonParent(root, files[i]);
            if (root == null) return null;
        }
        return root;
    }

    /** Writes contextual diff into given stream.*/
    private void exportDiff(Setup setup, String relativePath, OutputStream out) throws IOException {
        setup.initSources();
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;

        try {
            r1 = setup.getFirstSource().createReader();
            if (r1 == null) r1 = new StringReader("");  // NOI18N
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) r2 = new StringReader("");  // NOI18N
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }

        File file = setup.getBaseFile();
        try {
            InputStream is;
            //if (!HgUtils.getMimeType(file).startsWith("text/") && differences.length == 0) {
             //   // assume the file is binary 
             //   is = new ByteArrayInputStream(exportBinaryFile(file).getBytes("utf8"));  // NOI18N
            //} else {
                r1 = setup.getFirstSource().createReader();
                if (r1 == null) r1 = new StringReader(""); // NOI18N
                r2 = setup.getSecondSource().createReader();
                if (r2 == null) r2 = new StringReader(""); // NOI18N
                TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                    relativePath + " " + setup.getFirstSource().getTitle(), // NOI18N
                    relativePath + " " + setup.getSecondSource().getTitle(),  // NOI18N
                    null,
                    null,
                    r1,
                    r2,
                    differences
                );
                info.setContextMode(true, 3);
                String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
                is = new ByteArrayInputStream(diffText.getBytes("utf8"));  // NOI18N
            //}
            while(true) {
                int i = is.read();
                if (i == -1) break;
                out.write(i);
            }
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(VCSContext context, int includeStatus) {
        File[] all = Mercurial.getInstance().getFileStatusCache().listFiles(context, includeStatus);
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];            
            files.add(file);            
        }
        
        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] rootFiles = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        for (int i = 0; i < rootFiles.length; i++) {
            File file = rootFiles[i];
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new File[files.size()]);
    }
        
    private String exportBinaryFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder((int) file.length());
        if (file.canRead()) {
            Utils.copyStreamsCloseAll(baos, new FileInputStream(file));
        }
        sb.append("MIME: application/octet-stream; encoding: Base64; length: " + (file.canRead() ? file.length() : -1)); // NOI18N
        sb.append(System.getProperty("line.separator")); // NOI18N
        sb.append(Base64Encoder.encode(baos.toByteArray(), true));
        sb.append(System.getProperty("line.separator")); // NOI18N
        return sb.toString();
    }
}
