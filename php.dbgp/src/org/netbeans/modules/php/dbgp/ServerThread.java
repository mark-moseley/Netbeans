package org.netbeans.modules.php.dbgp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

class ServerThread extends SingleThread {
    private static final int TIMEOUT = 10000;
    private static final String PORT_OCCUPIED = "MSG_PortOccupied"; // NOI18N

    private int myPort;
    private ServerSocket myServer;
    private AtomicBoolean isStopped;
    private SessionManager sessionManager;

    ServerThread(SessionManager sessionManager) {
        super();
        this.sessionManager = sessionManager;
        isStopped = new AtomicBoolean(false);
    }


    public void run() {
        ProxyClient proxy = null;
        boolean proxyUsed = false;
        isStopped = new AtomicBoolean(false);
        DebugSession debugSession = getDebugSession();
        if (debugSession != null && createServerSocket(debugSession)) {
            proxy = ProxyClient.getInstance(debugSession.getOptions());
            proxyUsed = (proxy != null) ? proxy.init() : false;
            debugSession.startBackend();
            while (!isStopped() && getDebugSession() != null) {
                try {
                    Socket sessionSocket = myServer.accept();
                    if (!isStopped.get() && sessionSocket != null) {
                        debugSession.start(sessionSocket);
                        sessionManager.add(debugSession);
                    }
                } catch (SocketTimeoutException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
            closeSocket();
        }
        if (proxyUsed) {
            assert proxy != null;
            proxy.close();
        }
    }

    private DebugSession getDebugSession() {
        DebugSession retval = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, DebugSession.class);
        if (retval == null) {
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (Session session : sessions) {
                retval = session.lookupFirst(null, DebugSession.class);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;
    }

    private void log(Throwable exception) {
        Logger.getLogger(ServerThread.class.getName()).log(Level.FINE, null, exception);
    }

    private boolean createServerSocket(DebugSession debugSession) {
        synchronized (ServerThread.class) {
            try {
                myServer = new ServerSocket(myPort = debugSession.getOptions().getPort());
                myServer.setSoTimeout(TIMEOUT);
                myServer.setReuseAddress(true);
            } catch (IOException e) {
                String mesg = NbBundle.getMessage(ServerThread.class, PORT_OCCUPIED);
                mesg = MessageFormat.format(mesg, myPort);
                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(mesg, JOptionPane.YES_NO_OPTION);
                Object choice = DialogDisplayer.getDefault().notify(descriptor);
                if (choice.equals(JOptionPane.YES_OPTION)) {
                    Utils.openPhpOptionsDialog();
                }
                log(e);
                return false;
            }
            return true;
        }
    }

    private void closeSocket() {
        synchronized (ServerThread.class) {
            if (myServer == null) {
                return;
            }
            try {
                if (!myServer.isClosed()) {
                    myServer.close();
                }
            } catch (IOException e) {
                log(e);
            }
        }
    }

    public void cancel() {
        isStopped.set(true);
        closeSocket();
    }

    private boolean isStopped() {
        return isStopped.get();
    }
}
