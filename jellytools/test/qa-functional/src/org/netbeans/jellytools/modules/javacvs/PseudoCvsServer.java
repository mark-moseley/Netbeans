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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.javacvs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Unit testing CVS server implementation that provides
 * constant replies coming from prepared files and
 * simulates network and server overload failures.
 *
 * <p>Typical server usage in unit test sequence:
 * <pre>
 *   InputStream in = getClass().getResourceAsStream("...");
 *   PseudoCvsServer cvss = new PseudoCvsServer(in);
 *   new Thread(cvss).start();
 *   String cvsRoot = cvss.getCvsRoot();
 *   &lt;client operations>
 *   cvss.stop();  // check test failure
 *   &lt;tested client asserts>
 * </pre>
 *
 * <p>Fake input and output streams can be on Unix systems
 * catched using <tt>nc</tt> program. To catch command line
 * <tt>cvs</tt>:
 * <ul>
 *   <li>outgoing requests stream use <pre>nc -l -p $3000 | tee $requests.log</pre> and
 *   <pre>cvs -d :pserver:$ano@127.0.0.1:$3000/$cvs -z0 $whateEverCommand</pre>
 *   <li>incoming responses stream use <pre>nc $cvs.netbeans.org $2401 | tee $reponses.log</pre>
 * </ul>
 *
 * @author Petr Kuzel
 */
public final class PseudoCvsServer implements Runnable {

    private final int SIMULATE_SLOWNESS = 1;
    private final int SIMULATE_OVERLOAD = 2;
    private final int SIMULATE_DROP = 4;

    private final InputStream fakeDataStream;
    private OutputStream requestsStream;
    private final ServerSocket serverSocket;

    private Socket clientSocket;
    private OutputStream socketOut;
    private InputStream socketIn;

    private int outputCounter = -1;
    private int inputCounter = -1;
    private int simulationMode;

    private Exception throwable;
    private boolean stopped;
    private boolean running;
    private boolean ignoreProbe;

    /**
     * Creates new server that replies with given data.
     * @param in input stream that is consumend and <b>closed</b>
     * once server runnable terminates.
     *
     * @throws IOException if cannot create server socket
     */
    public PseudoCvsServer(InputStream in) throws IOException {
        try {
            this.fakeDataStream = in;
            serverSocket = new ServerSocket();
            serverSocket.bind(null, 2);
        } catch (IOException ex) {
            in.close();
            throw ex;
        }
    }

    /**
     * returns port that accepts client requests.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Utility method returning typical CVSRoot sutable for
     * local CVSClient testing.
     *
     * @return <code>":pserver:anoncvs@127.0.0.1:" + getPort() + "/cvs"</code>
     */
    public synchronized String getCvsRoot() {
        try {
            while (running == false) {
                this.wait();
            }
        } catch (InterruptedException e) {
        }
        return ":pserver:anoncvs@127.0.0.1:" + getPort() + "/cvs";
    }

    /**
     * Enters hard network failure simulation mode, silentry
     * dropping down connection after specified number of in/outgoing bytes.
     *
     * @param write specifies number of bytes send before
     * closing socket output stream. -1 for unlimited.
     * @param read specifies number of bytes received before
     * closing socket input stream. -1 for unlimited.
     */
    public void simulateNetworkFailure(int write, int read) {
        simulationMode |= SIMULATE_DROP;
        outputCounter = write;
        inputCounter = read;
    }

    /**
     * Enters server overload simulation mode.
     * Server properly closes streams sending TCP signals to client.
     *
     * @param write specifies number of bytes send before
     * shuting down socket output stream. -1 for unlimited.
     * @param read specifies number of bytes received before
     * shuting down socket input stream. -1 for unlimited.
     */
    public void simulateServerOverload(int write, int read) {
        simulationMode |= SIMULATE_OVERLOAD;
        outputCounter = write;
        inputCounter = read;
    }


    public void simulateSlowNetwork(int write, int read) {
        simulationMode |= SIMULATE_SLOWNESS;
        outputCounter = write;
        inputCounter = read;
    }

    /**
     * Enters ignore very first connect mode (connection probe).
     * It means that actual data are send out to second requestor.
     */
    public void ignoreProbe() {
        ignoreProbe = true;
    }

    /**
     * Logs server input intu specified stream.
     *
     * @param out log stream. The stream is closed on server termination.
     */
    public void logRequests(OutputStream out) {
        requestsStream = out;
    }

    /**
     * Entry point, starts listening at port and sends out
     * predefined replies. HAndles only first request.
     */
    public void run() {

        try {
            
            synchronized (this) {
                running = true;
                notifyAll();
            }    
            
            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    if (ignoreProbe == false) {
                        break;
                    }
                    ignoreProbe = false;
                } catch (IOException e) {
                    throwable = e;
                    return;
                }
            }
            
            
            
            try {
                socketOut = clientSocket.getOutputStream();
                socketIn = clientSocket.getInputStream();
                if (consumeInput()) {
                    return;
                }
                int nextByte = fakeDataStream.read();
                while (nextByte != -1) {
                    if (outputCounter-- == 0) {
                        if ((simulationMode & SIMULATE_DROP) != 0) {
                            socketOut.flush();
                            socketOut.close();
                        }
                        if ((simulationMode & SIMULATE_OVERLOAD) != 0) {
                            clientSocket.shutdownOutput();
                        }
                        if ((simulationMode & SIMULATE_SLOWNESS) != 0) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throwable = e;
                            }
                        }
                        if ((simulationMode & (SIMULATE_OVERLOAD | SIMULATE_DROP)) != 0) {
                            consumeInputUntilStopped();
                            return;
                        }
                    }
                    socketOut.write(nextByte);
                    if (consumeInput()) {
                        return;
                    }
                    nextByte = fakeDataStream.read();
                }
                socketOut.flush();
//                socketOut.close();  // need to propagate to client ASAP, otherwise all reads and available wait forever
                                      // on the other hand it causes premature BrokenPipe signal because it
                                      // immediately clears receiver's input buffers

                // do not close input streams prematurely
                consumeInputUntilStopped();
            } catch (IOException e) {
                throwable = e;
                return;
            }
        } finally {
            try {
                fakeDataStream.close();
            } catch (IOException alreadyClosed) {
            }
            try {
                if (socketIn != null) socketIn.close();
            } catch (IOException alreadyClosed) {
            }
            try {
                if (socketOut != null) socketOut.close();
            } catch (IOException alreadyClosed) {
            }
            try {
                if (requestsStream != null) {
                    requestsStream.flush();
                    requestsStream.close();
                }
            } catch (IOException alreadyClosed) {
            }
        }
    }

    /**
     * Stops server and optionaly rethrows internal server exception if any.
     */
    public synchronized void stop() throws Exception {
        stopped = true;
        notifyAll();
        if (throwable != null) {
            throw throwable;
        }
    }

    /** For diagnostics purpoes only. */
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter ps = new PrintWriter(sw);
        ps.write("PseudoCvsServer on " + serverSocket + "\n");
        if (throwable != null) {
            throwable.fillInStackTrace();
            throwable.printStackTrace(ps);
        }
        ps.flush();
        ps.close();
        return sw.getBuffer().toString();
    }

    /**
     * Reads client input stream possibly simulating errors.
     */
    private boolean consumeInput() throws IOException {
        int available = socketIn.available();
        for (int i = 0; i<available; i++) {
            if (inputCounter-- == 0) {
                if ((simulationMode & SIMULATE_DROP) != 0)  {
                    socketIn.close();
                    if (requestsStream != null) {
                        requestsStream.write("[PseudoCvsServer abort]".getBytes("utf8"));
                    }
                }
                if ((simulationMode & SIMULATE_OVERLOAD) != 0)  {
                    clientSocket.shutdownInput();
                    if (requestsStream != null) {
                        requestsStream.write("[PseudoCvsServer abort]".getBytes("utf8"));
                    }
                }
                return true;
            }
            int octet = socketIn.read();
            if (requestsStream != null) {
                requestsStream.write(octet);
                requestsStream.flush();
            }
        }
        return false;
    }

    private synchronized void consumeInputUntilStopped() throws IOException {
        while (stopped == false) {
            try {
                wait(100);
                consumeInput();
            } catch (InterruptedException e) {
                throwable = e;
            }
        }
    }
}
