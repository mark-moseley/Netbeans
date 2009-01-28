package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.support.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import org.openide.util.Exceptions;

/**
 * Utility class that provides information about particual host.
 */
public final class HostInfo {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "127.0.0.1"; // NOI18N
    private static List<String> myIPAdresses = new ArrayList<String>();
    private static Map<String, Boolean> filesExistenceHash =
            Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
    private static Map<String, String> platformPathsHash =
            Collections.synchronizedMap(new WeakHashMap<String, String>());
    private static final String cmd_uname = "/bin/uname"; // NOI18N
    private static final String cmd_sh = "/bin/sh"; // NOI18N


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
     * <tt>HostNotConnectedException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname) throws HostNotConnectedException {
        return fileExists(execEnv, fname, true);
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>HostNotConnectedException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @param useCache if <tt>true</tt> then subsequent tests for same files
     * in the same environment will not be actually performed, but result from
     * hash will be returned.
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname, final boolean useCache)
            throws HostNotConnectedException {
        String key = execEnv.toString() + fname;

        if (useCache && filesExistenceHash.containsKey(key)) {
            return filesExistenceHash.get(key);
        }

        boolean fileExists = false;

        if (execEnv.isLocal()) {
            fileExists = new File(fname).exists();
        } else {
            if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new HostNotConnectedException();
            }

            NativeTask task = new NativeTask(execEnv, "test", // NOI18N
                    new String[]{"-f", fname}); // NOI18N
            task.submit();

            try {
                fileExists = task.get() == 0;
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }

        filesExistenceHash.put(key, fileExists);

        return fileExists;
    }

    /**
     * Returns string that identifies OS installed on the host specified by the
     * <tt>execEnv</tt>.
     * For localhost it just returns <tt>System.getProperty("os.name")</tt>,
     * for remote one - the result of <tt>/bin/uname -s</tt> command execution.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt>
     * @return string that identifies OS installed on the host specified by the
     * <tt>execEnv</tt>
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static String getOS(final ExecutionEnvironment execEnv)
            throws HostNotConnectedException {
        if (execEnv.isLocal()) {
            return System.getProperty("os.name"); // NOI18N
        }

        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw new HostNotConnectedException();
        }

        StringBuffer taskOutput = new StringBuffer();
        NativeTask task = new NativeTask(execEnv, cmd_uname,
                new String[]{"-s"}, taskOutput); // NOI18N
        task.submit();
        int result = -1;

        String error = null;

        try {
            result = task.get();
        } catch (InterruptedException ex) {
            error = ex.getMessage();
        } catch (ExecutionException ex) {
            error = ex.getMessage();
        }

        return result == 0 ? taskOutput.toString().trim()
                : "Error: " + error; // NOI18N
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

    /**
     * Returns a platform path (i.e. intel-S2, sparc-S2, intel-Linux).
     * This is to be used in paths construction to system-dependent executables
     *
     * @param execEnv - execution environment to get information from
     * @return string, that represents a platform path <br>
     *         <tt>UNKNOWN</tt> if platform is unknown.
     */
    public static String getPlatformPath(
            final ExecutionEnvironment execEnv) {
        String key = execEnv.toString();

        if (platformPathsHash.containsKey(key)) {
            return platformPathsHash.get(key);
        }

        StringBuffer taskOutput = new StringBuffer();
        NativeTask task = new NativeTask(execEnv, cmd_sh, new String[]{
                    "-c", "\"" + // NOI18N
                    cmd_uname + " -s; " + // NOI18N
                    cmd_uname + " -p; " + // NOI18N
                    cmd_uname + " -m\""}, taskOutput); // NOI18N
        task.submit();

        int result = -1;

        try {
            result = task.get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }

        if (result != 0) {
            return "UNKNOWN"; // NOI18N
        }

        String[] out = taskOutput.toString().split("\n"); // NOI18N

        if (out.length != 3) {
            return "UNKNOWN"; // NOI18N
        }

        String osType = out[0];
        String platform = out[1];

        if (platform.equalsIgnoreCase("unknown")) { // NOI18N
            platform = out[2];
        }

        String os = osType.equals("SunOS") ? "S2" : "Linux"; // NOI18N

        if (platform.startsWith("i")) { // NOI18N
            platform = "intel"; // NOI18N
        }

        String platformPath = platform + "-" + os; // NOI18N
        platformPathsHash.put(key, platformPath);

        return platformPath;
    }

    /**
    /**
     * Tests whether the OS, that is ran in this execution environment, is Unix
     * or not.
     * @param execEnv <tt>ExecutionEnvironment</tt> to test
     * @return true if execEnv refers to a host that runs Solaris or Linux
     * @throws HostNotConnectedException if host is not connected yet.
     */
    public static boolean isUnix(ExecutionEnvironment execEnv)
            throws HostNotConnectedException {
        String os = getOS(execEnv);
        return "SunOS".equals(os) || "Linux".equals(os); // NOI18N
    }
}
