/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for notifying user about the background scan.
 * @since 1.2
 * @author Tomas Zezula
 */
public class ScanDialog {
    
    private ScanDialog () {}

    /**
     * This is a helper method to provide support for delaying invocations of actions
     * depending on java model. 
     * <br>Behavior of this method is following:<br>
     * If classpath scanning is not in progress, runnable's run() is called. <br>
     * If classpath scanning is in progress, modal cancellable notification dialog with specified
     * tile is opened.
     * </ul>
     * As soon as classpath scanning finishes, this dialog is closed and runnable's run() is called.
     * This method must be called in AWT EventQueue. Runnable is performed in AWT thread.
     *
     * @param runnable Runnable instance which will be called.
     * @param actionName Title of wait dialog.
     * @return true action was cancelled <br>
     *         false action was performed
     * 
     * @see org.netbeans.api.java.source.JavaSource#runWhenScanFinished which provides delayed invocation
     * of action without UI notification.
     */
    public static boolean runWhenScanFinished (final Runnable runnable, final String actionName) {
        assert runnable != null;
        assert actionName != null;
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            
            class AL implements ActionListener {
                
                private Dialog dialog;
                private Future<Void> monitor;
                
                public synchronized void start (final Future<Void> monitor) {
                    assert monitor != null; 
                    this.monitor = monitor;
                    if (dialog != null) {
                        dialog.setVisible(true);                                        
                    }
                }
                
                public void actionPerformed(ActionEvent e) {                    
                    monitor.cancel(false);
                    close ();
                }
                
                synchronized  void close () {
                    if (dialog != null) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        dialog = null;
                    }
                }
            };
            final AL listener = new AL ();            
            JLabel label = new JLabel(NbBundle.getMessage(ScanDialog.class,"MSG_WaitScan"),
                    javax.swing.UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(12,12,11,11));
            DialogDescriptor dd = new DialogDescriptor(label, actionName, true, new Object[]{NbBundle.getMessage(ScanDialog.class,"LBL_CancelAction",actionName)}, null, 0, null, listener);
            listener.dialog = DialogDisplayer.getDefault().createDialog(dd);
            listener.dialog.pack();
            final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
                ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]));
            final JavaSource js = JavaSource.create(info);
            try {
                Future<Void> monitor = js.runWhenScanFinished(new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws Exception {
                        
                        final Runnable r = new Runnable () {
                            public void run () {
                                listener.close();
                                runnable.run();
                            }
                        };
                        if (SwingUtilities.isEventDispatchThread()) {
                            r.run();
                        }
                        else {
                            SwingUtilities.invokeLater(r);         
                        }
                    }
                }, true);
                if (!monitor.isDone()) {
                    listener.start(monitor);
                }                
                return monitor.isCancelled();
            }catch (IOException e) {
                Exceptions.printStackTrace(e);
                return true;
            }
        } else {
            runnable.run();
            return false;
        }
    }
    
}
