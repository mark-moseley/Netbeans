/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import java.awt.Dialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.explorer.dlg.GrabTableProgressPanel;
import org.openide.DialogDescriptor;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.ColumnNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;

public class GrabTableAction extends DatabaseAction {
    static final long serialVersionUID =-7685449970256732671L;
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1)
            return true;
        else
            return false;
    }

    public void performAction (Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;
        
        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            DatabaseNodeInfo nfo = info.getParent(nodename);
            Specification spec = (Specification)nfo.getSpecification();
            String tablename = (String)nfo.get(DatabaseNode.TABLE);

            // Get command

            CreateTable cmd = (CreateTable)spec.createCommandCreateTable(tablename);
            
            GrabTableWorker run = new GrabTableWorker(nfo);
            Enumeration enu = run.execute();
            
            while (enu.hasMoreElements()) {
                Object element = enu.nextElement();
                if (element instanceof ColumnNodeInfo) {
                    cmd.getColumns().add(((ColumnNodeInfo)element).getColumnSpecification());
                }
            }

            //			System.out.println(cmd.getCommand());

            // Get filename

            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle(bundle().getString("GrabTableFileSaveDialogTitle")); //NOI18N
            chooser.setSelectedFile(new File(tablename+".grab")); //NOI18N
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                      public boolean accept(File f) {
                                          return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                                      }
                                      public String getDescription() {
                                          return bundle().getString("GrabTableFileTypeDescription"); //NOI18N
                                      }
                                  });

            java.awt.Component par = WindowManager.getDefault().getMainWindow();
            boolean noResult = true;
            File file = null;
            while(noResult) {
                if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    if (file != null) {
                        if(file.exists()) {
                            Object yesOption = new JButton(bundle().getString("Yes"));
                            Object noOption = new JButton (bundle().getString("No"));
                            Object result = DialogDisplayer.getDefault ().notify (new NotifyDescriptor
                                            (MessageFormat.format(bundle().getString("MSG_ReplaceFileOrNot"), // NOI18N
                                                new String[] {file.getName()}), //question
                                             bundle().getString("GrabTableFileSaveDialogTitle"), // title
                                             NotifyDescriptor.YES_NO_OPTION, // optionType
                                             NotifyDescriptor.QUESTION_MESSAGE, // messageType

                                             new Object[] { yesOption, noOption }, // options
                                             yesOption // initialValue
                                            ));
                            if (result.equals(yesOption)) {
                                // the file can be replaced
                                noResult = false;
                            }
                        } else noResult = false;
                    }
                } else return;
            }
            FileOutputStream fstream = new FileOutputStream(file);
            ObjectOutputStream ostream = new ObjectOutputStream(fstream);
            cmd.setSpecification(null);
            ostream.writeObject(cmd);
            ostream.flush();
            ostream.close();

        } catch(Exception exc) {
            String message = MessageFormat.format(bundle().getString("ERR_UnableToGrabTable"), new String[] {exc.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    private static final class GrabTableWorker {

        private DatabaseNodeInfo nfo;
        private Task task;
        private Dialog dialog;
        private ProgressHandle progressHandle;
        //private boolean finished;
        
        private Enumeration enumeration;
        private DatabaseException exception;
        
        public GrabTableWorker(DatabaseNodeInfo nfo) {
            this.nfo = nfo;
        }
        
        public Enumeration execute() throws DatabaseException {
            progressHandle = ProgressHandleFactory.createHandle(null);
            GrabTableProgressPanel progressPanel = new GrabTableProgressPanel();
            progressPanel.setProgressComponent(ProgressHandleFactory.createProgressComponent(progressHandle));
            String dialogTitle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("GrabTableProgressDialogTitle"); // NOI18N
            DialogDescriptor desc = new DialogDescriptor(progressPanel, dialogTitle, true, new Object[0], DialogDescriptor.NO_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setResizable(false);
            if (dialog instanceof JDialog) {
                ((JDialog)dialog).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            }
            progressHandle.start();
            
            task = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        enumeration = nfo.getChildren().elements();
                    } catch (DatabaseException e) {
                        exception = e;
                    }
                }
            });
            
            task.addTaskListener(new TaskListener() {
                public void taskFinished(Task t) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dialog.setVisible(false);
                        }
                    });
                }
            });
            
            if (!task.isFinished()) {
                dialog.setVisible(true);
            }
            dialog.dispose();
            progressHandle.finish();
            if (exception != null) {
                throw exception;
            }
            return enumeration;
        }
    }
}
