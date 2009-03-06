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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jirka Rechtacek
 */
public class NetworkAccess {

    private NetworkAccess () {}
    
    public static Task createNetworkAcessTask (URL url, int timeout, NetworkListener networkAcesssListener) {
        return new Task (url, timeout, networkAcesssListener);
    }
    
    public static class Task implements Cancellable {
        private URL url;
        private int timeout;
        private NetworkListener listener;
        private ExecutorService es = Executors.newSingleThreadExecutor ();
        private Future<InputStream> connect = null;
        private RequestProcessor.Task rpTask = null;
        
        private Task (URL url, int timeout, NetworkListener listener) {
            if (url == null) {
                throw new IllegalArgumentException ("URL cannot be null.");
            }
            if (listener == null) {
                throw new IllegalArgumentException ("NetworkListener cannot be null.");
            }
            this.url = url;
            this.timeout = timeout;
            this.listener = listener;
            postTask ();
        }
        
        private void postTask () {
            final SizedConnection connectTask = createCallableNetwork (url, timeout);
            rpTask = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    connect = es.submit (connectTask);
                    InputStream is = null;
                    try {
                        is = connect.get ();
                        if (connect.isDone ()) {
                            listener.streamOpened (is, connectTask.getContentLength() );
                        } else if (connect.isCancelled ()) {
                            listener.accessCanceled ();
                        } else {
                            listener.accessTimeOut ();
                        }
                    } catch(InterruptedException ix) {
                        listener.notifyException (ix);
                    } catch (ExecutionException ex) {
                        listener.notifyException (ex);
                    }
                }
            });
        }
        
        public void waitFinished () {
            assert rpTask != null : "RequestProcessor.Task must be initialized.";
            rpTask.waitFinished ();
        }
        
        private SizedConnection createCallableNetwork (final URL url, final int timeout) {
            return new SizedConnection () {
                private int contentLength = -1;

                public int getContentLength() {
                    return contentLength;
                }

                public InputStream call () throws Exception {
                    URLConnection conn = url.openConnection ();
                    conn.setConnectTimeout (timeout);
                    InputStream is = conn.getInputStream ();
                    contentLength = conn.getContentLength();
                    return new BufferedInputStream (is);
                }
            };
        }
        
        public boolean cancel () {
            return connect.cancel (true);
        }
        
    }
    private interface SizedConnection extends Callable<InputStream> {
        public int getContentLength();
    }
    public interface NetworkListener {
        public void streamOpened (InputStream stream, int contentLength);
        public void accessCanceled ();
        public void accessTimeOut ();
        public void notifyException (Exception x);
    }
    
}
