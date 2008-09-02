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

package org.netbeans.modules.glassfish.common;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Peter Williams
 */
public class SimpleIO {

    /**
     * Time in milliseconds to wait between checks of the input stream.
     */
    private static final int DELAY = 1000;
    
    private final String name;
    private final InputOutput io;
    private final CancelAction cancelAction;
    private final AtomicReference<Process> process;
    
    public SimpleIO(String displayName, Process task) {
        name = displayName;
        process = new AtomicReference<Process>(task);
        cancelAction = new CancelAction();
        io = IOProvider.getDefault().getIO(displayName, new Action [] {
            cancelAction
        });
        io.select();
    }
    
    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream... inputStreams) {
        RequestProcessor rp = RequestProcessor.getDefault();
        for(InputStream inputStream : inputStreams){
            rp.post(new IOReader(inputStream));
        }
    }

    /**
     * Writes a string to the output window
     * 
     * @param s string to be written
     */
    public synchronized void write(String s) {
        OutputWriter writer = io.getOut();
        writer.print(s);
        writer.flush();
    }

    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        io.select();
    }
    
    /**
     * Closes the output panel
     */
    public synchronized void closeIO() {
        // Don't close the window when finished -- in case of install or launching
        // failures, it makes problems easiesr for the user to diagnose.
        process.set(null);
        cancelAction.updateEnabled();
    }
    
    /**
     * Thread to read an I/O stream and write it to the output window managed
     */
    private class IOReader implements Runnable {
        
        private InputStream inputStream;
        
        public IOReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            final String originalName = Thread.currentThread().getName();
            
            try {
                Thread.currentThread().setName(this.getClass().getName() + " - " + inputStream);
                
                // create a reader from the input stream
                Reader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                int len = 0;
                while(len != -1) {
                    while((len = reader.read(chars)) != -1) {
                        write(new String(chars, 0, len));
                        selectIO();
                        
                        if(!reader.ready()) {
                            break;
                        }
                    }
                    
                    // sleep for a while when the stream is empty
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
                
                Thread.currentThread().setName(originalName);
            }
        }
    }

    /** This action will be displayed in the server output window */
    public class CancelAction extends AbstractAction {
        
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private static final String ICON = 
                "org/netbeans/modules/glassfish/common/resources/stop.png"; // NOI18N
        
        public CancelAction() {
            super(NbBundle.getMessage(SimpleIO.class, "CTL_Cancel"), 
                    new ImageIcon(Utilities.loadImage(ICON)));
            putValue(SHORT_DESCRIPTION, 
                    NbBundle.getMessage(SimpleIO.class, "LBL_CancelDesc"));
        }

        public void actionPerformed(ActionEvent e) {
            if(process.get() != null) {
                String message = NbBundle.getMessage(SimpleIO.class, "MSG_QueryCancel", name);
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                    Process p = process.getAndSet(null);
                    if(p != null) {
                        p.destroy();
                    } else {
                        Logger.getLogger("glassfish").log(Level.FINEST, "Process handle unexpectedly null, cancel aborted.");
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return process.get() != null;
        }
        
        public void updateEnabled() {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    firePropertyChange(PROP_ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
    
}
