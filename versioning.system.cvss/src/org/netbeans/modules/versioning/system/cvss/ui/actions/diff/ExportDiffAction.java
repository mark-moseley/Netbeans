/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import java.io.*;
import java.util.*;

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
public class ExportDiffAction extends AbstractSystemAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    public ExportDiffAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_ExportDiff";  // NOI18N
    }

    /**
     * First look for DiffSetupSource name then for super (context name).
     */
    public String getName() {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            String setupName = ((DiffSetupSource)activated).getSetupDisplayName();
            if (setupName != null) {
                return NbBundle.getMessage(this.getClass(), getBaseName() + "_Context",  // NOI18N
                                            setupName);
            }
        }
        return super.getName();
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public boolean enable(Node[] nodes) {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            return true;
        }
        return  super.enable(nodes) && 
                Lookup.getDefault().lookup(DiffProvider.class) != null;
    }

    public void performCvsAction(final Node[] nodes) {

        // reevaluate fast enablement logic guess

        boolean noop;
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            noop = ((DiffSetupSource) activated).getSetups().isEmpty();
        } else {
            Context context = getContext(nodes);
            File [] files = DiffExecutor.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            noop = files.length == 0;
        }
        if (noop) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportDiffAction.class, "BK3001"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffAction.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportDiffAction.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }
        chooser.setCurrentDirectory(new File(CvsModuleConfig.getDefault().getDefaultValue("ExportDiff.saveFolder", System.getProperty("user.home")))); // NOI18N
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(ExportDiffAction.class, "BK3002");
            }
        });
        
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffAction.class, "MNE_Export_ExportAction").charAt(0));
        int ret = chooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(ExportDiffAction.class, "CTL_Export_ExportAction"));
        if (ret == JFileChooser.APPROVE_OPTION) {
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
            CvsModuleConfig.getDefault().setDefaultValue("ExportDiff.saveFolder", destination.getParent()); // NOI18N

            final File out = destination;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    async(nodes, out);
                }
            });
        }
    }

    protected boolean asynchronous() {
        return false;
    }
    
    private void async(Node[] nodes, File destination) {
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;
        ExecutorGroup group = new ExecutorGroup(getRunningName());
        try {

            // prepare setups and common parent - root

            File root = null;
            Collection setups;

            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated instanceof DiffSetupSource) {
                setups = ((DiffSetupSource) activated).getSetups();
                List setupFiles = new ArrayList(setups.size());
                for (Iterator i = setups.iterator(); i.hasNext();) {
                    Setup setup = (Setup) i.next();
                    setupFiles.add(setup.getBaseFile()); 
                }
                root = getCommonParent((File[]) setupFiles.toArray(new File[setupFiles.size()]));
            } else {
                Context context = getContext(nodes);
                File [] files = DiffExecutor.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
                root = getCommonParent(files);
                setups = new ArrayList(files.length);
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    Setup setup = new Setup(file, Setup.DIFFTYPE_LOCAL);
                    setups.add(setup);
                }
            }
            if (root == null) {
                NotifyDescriptor nd = new NotifyDescriptor(
                        NbBundle.getMessage(ExportDiffAction.class, "MSG_BadSelection_Prompt"), 
                        NbBundle.getMessage(ExportDiffAction.class, "MSG_BadSelection_Title"), 
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }

            String sep = System.getProperty("line.separator"); // NOI18N
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
            out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Following Index: paths are relative to: " + root.getAbsolutePath() + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch can be applied using context Tools: Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# It uses platform neutral UTF-8 endcoding and \\n newlines." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N


            Iterator it = setups.iterator();
            int i = 0;
            while (it.hasNext()) {
                Setup setup = (Setup) it.next();
                File file = setup.getBaseFile();
                group.progress(file.getName());

                String index = "Index: ";   // NOI18N
                String rootPath = root.getAbsolutePath();
                String filePath = file.getAbsolutePath();
                if (filePath.startsWith(rootPath)) {
                    index += filePath.substring(rootPath.length() + 1).replace(File.separatorChar, '/') + sep;
                    out.write(index.getBytes("utf8")); // NOI18N
                }
                exportDiff(group, setup, out);
                i++;
            }

            exportedFiles = i;
            success = true;
        } catch (IOException ex) {
            ErrorManager.getDefault().annotate(ex, NbBundle.getMessage(ExportDiffAction.class, "BK3003"));
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);   // stack trace to log
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);  // message to user
        } finally {
            group.executed();
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportDiffAction.class, "BK3004", new Integer(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                }
            } else {
                destination.delete();
            }

        }
    }

    private static File getCommonParent(File [] files) {
        File root = files[0].getParentFile();
        for (int i = 1; i < files.length; i++) {
            root = Utils.getCommonParent(root, files[i]);
            if (root == null) return null;
        }
        return root;
    }

    /** Writes contextual diff into given stream.*/
    private void exportDiff(ExecutorGroup group, Setup setup, OutputStream out) throws IOException {
        setup.initSources(group);
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        Reader r1 = setup.getFirstSource().createReader();
        if (r1 == null) r1 = new StringReader("");  // NOI18N
        Reader r2 = setup.getSecondSource().createReader();
        if (r2 == null) r2 = new StringReader("");  // NOI18N
        Difference[] differences = diff.computeDiff(r1, r2);

        File file = setup.getBaseFile();
        String name = file.getAbsolutePath();
        r1 = setup.getFirstSource().createReader();
        if (r1 == null) r1 = new StringReader(""); // NOI18N
        r2 = setup.getSecondSource().createReader();
        if (r2 == null) r2 = new StringReader(""); // NOI18N
        TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
            name + " " + setup.getFirstSource().getTitle(), // NOI18N
            name + " " + setup.getSecondSource().getTitle(),  // NOI18N
            null,
            null,
            r1,
            r2,
            differences
        );
        info.setContextMode(true, 3);
        InputStream is = TextDiffVisualizer.differenceToContextDiffText(info);
        while(true) {
            int i = is.read();
            if (i == -1) break;
            out.write(i);
        }
    }

}
