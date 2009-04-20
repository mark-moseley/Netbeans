package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.HostInfoFetcher;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;

/**
 * Utility class that provides information about particual host.
 */
public final class HostInfoUtils {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "127.0.0.1"; // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final List<String> myIPAdresses = new ArrayList<String>();
    private static final Map<String, Boolean> filesExistenceHash =
            Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
    private static final Map<ExecutionEnvironment, HostInfoFetcher> hostInfoProviders =
            new HashMap<ExecutionEnvironment, HostInfoFetcher>();


    static {
        NetworkInterface iface = null;
        try {
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
                    ifaces.hasMoreElements();) {
                iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration ips = iface.getInetAddresses();
                        ips.hasMoreElements();) {
                    myIPAdresses.add(
                            ((InetAddress) ips.nextElement()).getHostAddress());
                }
            }
        } catch (SocketException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * Calling this method equals to calling
     * <pre>
     * fileExists(execEnv, fname, true)
     * </pre>
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname) throws IOException {
        return fileExists(execEnv, fname, true);
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @param useCache if <tt>true</tt> then subsequent tests for same files
     * in the same environment will not be actually performed, but result from
     * hash will be returned.
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     * @throws ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname, final boolean useCache)
            throws IOException {
        String key = execEnv.toString() + fname;

        if (useCache && filesExistenceHash.containsKey(key)) {
            return filesExistenceHash.get(key);
        }

        boolean fileExists = false;

        if (execEnv.isLocal()) {
            fileExists = new File(fname).exists();
        } else {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new ConnectException();
            }

            NativeProcessBuilder npb = new NativeProcessBuilder(
                    execEnv, "test").setArguments("-e", fname); // NOI18N

            try {
                fileExists = npb.call().waitFor() == 0;
            } catch (InterruptedException ex) {
                throw new IOException(ex.getMessage());
            }
        }

        filesExistenceHash.put(key, fileExists);

        return fileExists;
    }

    public static String searchFile(ExecutionEnvironment execEnv,
            List<String> searchPaths, String file, boolean searchInUserPaths) {
        NativeProcessBuilder npb;
        BufferedReader br;
        String line;
        Process p;

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv, true);

            if (hostInfo == null) {
                return null;
            }

            String shell = hostInfo.getShell();

            if (shell == null) {
                return null;
            }

            List<String> sp = new ArrayList<String>(searchPaths);

            if (searchInUserPaths) {
                npb = new NativeProcessBuilder(execEnv, shell).setArguments("-c", "echo $PATH"); // NOI18N
                p = npb.call();
                p.waitFor();
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                line = br.readLine();

                if (line != null) {
                    sp.addAll(Arrays.asList(line.split("[;:]"))); // NOI18N
                }
            }

            StringBuilder cmd = new StringBuilder();

            for (Iterator<String> i = sp.iterator(); i.hasNext();) {
                cmd.append("/bin/ls " + i.next() + "/" + file); // NOI18N
                if (i.hasNext()) {
                    cmd.append(" || "); // NOI18N
                }
            }

            npb = new NativeProcessBuilder(execEnv, shell).setArguments("-c", cmd.toString()); // NOI18N
            p = npb.call();
            p.waitFor();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = br.readLine();

            return (line == null || "".equals(line.trim())) ? null : line.trim(); // NOI18N
        } catch (IOException ex) {
            log.finest("Exception in searchFile() " + ex.toString()); // NOI18N
        } catch (InterruptedException ex) {
            log.finest("Exception in searchFile() " + ex.toString()); // NOI18N
        }

        log.finest("File " + file + " not found"); // NOI18N

        return null;
    }

    /**
     * Returns true if and only if <tt>host</tt> identifies a localhost.
     *
     * @param host host identification string. Either hostname or IP address.
     * @return true if and only if <tt>host</tt> identifies a localhost.
     */
    public static boolean isLocalhost(String host) {
        boolean result = false;

        try {
            result = myIPAdresses.contains(
                    InetAddress.getByName(host).getHostAddress());
        } catch (UnknownHostException ex) {
        }

        return result;
    }

    // blocking - if true - initiate connection and wait for info (if required)
    //            if false - just return null in case no info available yet
    public static HostInfo getHostInfo(final ExecutionEnvironment execEnv, boolean blocking) {
        HostInfoFetcher infoFetcher;

        synchronized (hostInfoProviders) {
            infoFetcher = hostInfoProviders.get(execEnv);
            if (infoFetcher == null) {
                infoFetcher = new HostInfoFetcher(execEnv);
                hostInfoProviders.put(execEnv, infoFetcher);
            }
        }

        return infoFetcher.getInfo(blocking);
    }
}
