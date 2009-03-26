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
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 *
 * @author Sergey Grinev
 */
class HostsListTableModel extends AbstractTableModel {

    private static final Logger LOG = Logger.getLogger("cnd.remote.logger"); // NOI18N

    private final ProgressHandle phandle;

    public HostsListTableModel() {
        this.phandle = ProgressHandleFactory.createHandle("Gathering hosts information"); //NOI18N
    }

    public ProgressHandle getProgressHandle() {
        return phandle;
    }

    private Runnable runOnFinish;

    public void start(Runnable runOnFinish) {
        this.runOnFinish = runOnFinish;
        phandle.start(255);
        new Thread(new HostsLoader()).start();
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return 2; //3; no platform yet
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        HostRecord record = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return record.name;
            case 1:
                return CreateHostWizardIterator.getString(record.ssh ? "HostAvailable" : "HostUnavailable"); //NOI18N
            case 2:
                return null;
            default:
                return null;
        }
    }

    public String getHostName(int row) {
        return rows.get(row).name;
    }

    private final static Comparator<HostRecord> hrc = new Comparator<HostRecord>() {
        public int compare(HostRecord o1, HostRecord o2) {
            if (o1.ssh && !o2.ssh) {
                return -1;
            } else if (!o1.ssh && o2.ssh) {
                return 1;
            } else {
                return o1.name.compareTo(o2.name);
            }
        }
    };

    private void addHost(String ip, String name, boolean ssh) {
        synchronized (rows) {
            HostRecord record = new HostRecord(ip, name, ssh);
            rows.add(record);
            Collections.sort(rows, hrc);
            fireTableDataChanged();
            //fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }
     //   queueForCheck.add(record);
    }

    private final List<HostRecord> rows = new ArrayList<HostRecord>();

    //private final List<HostRecord> queueForCheck = Collections.synchronizedList(new ArrayList<HostRecord>());

    private static class HostRecord {

        public final String name;
        public final String ip;
        public final boolean ssh;

        public HostRecord(String ip, String name, boolean ssh) {
            this.name = name;
            this.ip = ip;
            this.ssh = ssh;
        }

        @Override
        public String toString() {
            return name + " [" + ip + "] " + (ssh ? "ssh" : "nossh"); //NOI18N
        }
        //platform
    }

    private class HostsLoader implements Runnable {

        public void run() {
            try {
                byte[] ip = InetAddress.getLocalHost().getAddress();
                if (ip.length == 0) {
                    // let's be paranoiac
                    return;
                }
                int idxLast = ip.length - 1; // FF.FF.FF.0
                byte localLastOne = ip[idxLast];
                long n = System.currentTimeMillis();
                int count = 0;
                for (short i = 0; i <= 255; i++) {
                    phandle.progress(i);
                    if (i == localLastOne) {
                        // localhost will never be offline again
                        continue;
                    }
                    ip[idxLast] = (byte) i;
                    InetAddress host = InetAddress.getByAddress(ip);
                    try {
                        if (host.isReachable(1000)) {
                            count++;
                            HostsListTableModel.this.addHost(host.getHostAddress(), host.getHostName(), doPing(host, 22));
                        }
                    } catch (IOException ex) {
                        // it's quite normal if host denies to respond (firewall, etc)
                        // so it doesn't make sense even to log this :)
                        // LOG.log(Level.INFO, null, ex);
                    }
                }
                LOG.info("Finding " + count + " host(s) took " + ((System.currentTimeMillis() - n) / 1000) + "s");

            } catch (UnknownHostException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                phandle.finish();
                if (runOnFinish != null) {
                    SwingUtilities.invokeLater(runOnFinish); //SwingUtilities is a bit cheat here, but otherwise one have to introduce ugly double Runnable in caller
                }
            }

        }
    }

//    private class SshValidator implements Runnable {
//
//        public void run() {
//            while(true) {
//                if (queueForCheck.isEmpty()) {
//                    try {
//                        Thread.sleep(999);
//                    } catch (InterruptedException ex) {
//                    }
//                } else {
//                    queueForCheck.get(0);
//                }
//            }
//        }
//
//    }

    private static boolean doPing(InetAddress addr, int port) {
        try {
            Socket socket = new Socket(addr, port);
            socket.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
