/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.test.installer;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Mikhail Vaysman
 */
public class Utils {

    public static final long MAX_EXECUTION_TIME = 60000000;
    public static final long MAX_INSTALATION_WAIT = 60000000;
    public static final int DELAY = 50;

    public static final String OK = "OK";

    public static String getInstaller(TestData data) {
        //File sourceBandle = new File(data.getInstallerFileName());
        File destBundle = new File(data.getTestWorkDir() + File.separator + "installer" + "." + data.getPlatformExt());

        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL sourceBandeleURL = new URL(data.getInstallerURL(data.getInstallerType()));
            Proxy proxy = null;

            proxy = data.getProxy();

            in = sourceBandeleURL.openConnection(proxy).getInputStream();
            out = new FileOutputStream(destBundle);

            byte[] buffer = new byte[10240];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
            return toString(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return toString(ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return toString(ex);
                }
            }
        }
        data.setInstallerFile(destBundle);
        return OK;
    }

    public static String extractBundle(TestData data) {
        int errorLevel = 0;

        try {
            java.lang.String command = null;
            command = data.getInstallerFile().getCanonicalPath();

            java.lang.ProcessBuilder builder;
            java.lang.Process process;

            if (0 != data.getPaltformName().compareTo("windows")) {
                if (data.getPaltformName().contains("linux") || data.getPaltformName().contains("solaris")) {
                    builder = new java.lang.ProcessBuilder("chmod", "+x", command);
                    process = builder.start();

                    long runningTime;
                    for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                        try {
                            errorLevel = process.exitValue();
                            break;
                        } catch (IllegalThreadStateException e) {
                            ; // do nothing - the process is still running
                        }
                    }

                    if (runningTime >= MAX_EXECUTION_TIME) {
                        process.destroy();
                        data.getLogger().log(Level.SEVERE, "Timeout. Chmod process destroyed");
                        return "Timeout. Chmod process destroyed";
                    } else if (errorLevel != 0) {
                        return "ErrorLevel=>" + errorLevel;
                    }
                }
            }

            String pathToExtract = data.getTestWorkDir() + java.io.File.separator + "bundle";

            builder = new java.lang.ProcessBuilder(command, "--silent", "--userdir", data.getTestWorkDir().getCanonicalPath(), "--extract", pathToExtract, "--output", "inst.log");
            process = builder.start();

            long runningTime;
            for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                try {
                    errorLevel = process.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    ; // do nothing - the process is still running
                }
            }

            if (runningTime >= MAX_EXECUTION_TIME) {
                process.destroy();
                data.getLogger().log(Level.SEVERE, "Timeout. Process destroyed");
                return "Timeout. Extract process destroyed";
            } else if (errorLevel == 0) {
                data.setBundleFile(new File(pathToExtract + java.io.File.separator + "bundle.jar"));
                data.getInstallerFile().deleteOnExit();
                return OK;
            } else {
                return "ErrorLevel=>" + errorLevel;
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
    }

    public static String loadClasses(TestData data) {
        try {

            data.setClassLoader(new URLClassLoader(new URL[]{data.getBundleFile().toURI().toURL()}, data.getClass().getClassLoader()));
            data.setInstallerMainClass(Class.forName(data.getInstallerMainClassName(), true, data.getClassLoader()));
        } catch (Exception ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
//        } catch (IllegalArgumentException ex) {
//            data.getLogger().log(Level.SEVERE, null, ex);
//        } catch (SecurityException ex) {
//            data.getLogger().log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            data.getLogger().log(Level.SEVERE, null, ex);
//        } catch (MalformedURLException ex) {
//            data.getLogger().log(Level.SEVERE, null, ex);
//        }
        return OK;
    }

    public static String runInstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    data.getInstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[] {}));
                } catch (Exception ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
//                } catch (NoSuchMethodException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (SecurityException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (IllegalAccessException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (IllegalArgumentException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (InvocationTargetException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                }
            }
        }).run();
        return OK;
    }

    public static String runUninstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    //dirty hack --ignore-lock
                    data.getUninstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[] {"--force-uninstall", "--ignore-lock"}));
                } catch (Exception ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
//                } catch (NoSuchMethodException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (SecurityException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (IllegalAccessException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (IllegalArgumentException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                } catch (InvocationTargetException ex) {
//                    data.getLogger().log(Level.SEVERE, null, ex);
//                }
            }
        }).run();
        return OK;
    }

    public static String loadEngineClasses(TestData data) {
        try {
            String jarFileName = data.getWorkDirCanonicalPath() + File.separator + ".nbi" + File.separator + "nbi-engine.jar";

            data.setEngineClassLoader(new URLClassLoader(new URL[]{new File(jarFileName).toURI().toURL()}, data.getClass().getClassLoader()));
            data.setUninstallerMainClass(Class.forName(data.getUninstallerMainClassName(), true, data.getEngineClassLoader()));
        } catch (Exception ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
//        } catch (IllegalArgumentException ex) {
//            data.getLogger().log(Level.SEVERE, "-1", ex);
//            return -1;
//        } catch (SecurityException ex) {
//            data.getLogger().log(Level.SEVERE, "-2", ex);
//            return -2;
//        } catch (ClassNotFoundException ex) {
//            data.getLogger().log(Level.SEVERE, "-3", ex);
//            return -3;
//        } catch (MalformedURLException ex) {
//            data.getLogger().log(Level.SEVERE, "-4", ex);
//            return -4;
//        }
        return OK;
    }

    public static void stepWelcome() {
        new JButtonOperator(new JFrameOperator("Netbeans IDE"), "Next >").push();
    }

    public static void stepLicense() {
        JFrameOperator installerMain = new JFrameOperator("Netbeans IDE");

        new JCheckBoxOperator(installerMain, "I accept").push();
        new JButtonOperator(installerMain, "Next >").push();
    }

    public static void stepSetDir(TestData data, String label, String dir) {
        JFrameOperator installerMain = new JFrameOperator("Netbeans IDE");

        new JTextFieldOperator((JTextField) (new JLabelOperator(installerMain, label)
                    .getLabelFor()
                    )).setText(data.getWorkDirCanonicalPath() + File.separator + dir);

        new JButtonOperator(installerMain, "Next >").push();
    }

    public static void stepChooseComponet(String name) {
        JFrameOperator installerMain = new JFrameOperator("Netbeans IDE");

        new JButtonOperator(installerMain, "Customize...").push();
        JDialogOperator customizeInstallation = new JDialogOperator("Customize Installation");
        JListOperator featureList = new JListOperator(customizeInstallation);
        featureList.selectItem(name);
        featureList.pressKey(KeyEvent.VK_SPACE);
        new JButtonOperator(customizeInstallation, "OK").push();
    }

    public static void stepInstall(TestData data) {
        JFrameOperator installerMain = new JFrameOperator("Netbeans IDE");

        new JButtonOperator(installerMain, "Install").push();
        new JLabelOperator(installerMain, "Installing"); //dirty hack
        JProgressBarOperator installingProgress = new JProgressBarOperator(installerMain);

        long waitingTime;
        for (waitingTime = 0; waitingTime < Utils.MAX_INSTALATION_WAIT; waitingTime += Utils.DELAY) {
            int val = ((JProgressBar) installingProgress.getSource()).getValue();
            if (val >= 100) {
                break;
            }
            Utils.wait(data, 5);
        }

        if (waitingTime >= Utils.MAX_INSTALATION_WAIT) {
            NbTestCase.fail("Installation timeout");
        }
    }

    public static void stepUninstall() {
        new JButtonOperator(new JFrameOperator("Netbeans IDE"), "Uninstall").push();
    }

    public static void stepFinish() {
        new JButtonOperator(new JFrameOperator("Netbeans IDE"), "Finish").push();
    }

    public static void phaseOne(NbTestCase thiz, TestData data, String installerType) {
        try {
            data.setWorkDir(thiz.getWorkDir());
        } catch (IOException ex) {
            NbTestCase.fail("Can not get WorkDir");
        }

        data.setInstallerType(installerType);

        System.setProperty("nbi.dont.use.system.exit", "true");
        System.setProperty("nbi.utils.log.to.console", "false");
        System.setProperty("user.home", data.getWorkDirCanonicalPath());

        NbTestCase.assertEquals("Get installer", Utils.OK, Utils.getInstaller(data));
        NbTestCase.assertEquals("Extract bundle", Utils.OK, Utils.extractBundle(data));
        NbTestCase.assertEquals("Load class", Utils.OK, Utils.loadClasses(data));
        NbTestCase.assertEquals("Run main method", Utils.OK, Utils.runInstaller(data));
    }

    public static void phaseFour(TestData data) {
        //Installation
        Utils.stepInstall(data);

        //finish
        Utils.stepFinish();

        Utils.wait(data, 5);

        NbTestCase.assertEquals("Installer Finshed", 0, ((Integer) System.getProperties().get("nbi.exit.code")).intValue());

        NbTestCase.assertEquals("Load engine classes", OK, Utils.loadEngineClasses(data));
        NbTestCase.assertEquals("Run uninstaller main class", OK, Utils.runUninstaller(data));

        Utils.stepUninstall();

        Utils.stepFinish();

        Utils.wait(data, 5);

        NbTestCase.assertEquals("Uninstaller Finshed", 0, ((Integer) System.getProperties().get("nbi.exit.code")).intValue());
    }

    public static void phaseTwo(TestData data) {
        //welcome
        stepWelcome();

        //license
        stepLicense();

        //Choose dir
        stepSetDir(data, "Install NetBeans IDE", "NetBeans");
    }

    public static void phaseThree(TestData data) {
        //Choose GF dir
        Utils.stepSetDir(data, "Install GlassFish", "GlassFish");

        //Choose Tomcat dir
        Utils.stepSetDir(data, "Installation location", "Tomcat");
    }

    public static void wait(TestData data, int sec) {
        try {
            java.lang.Thread.sleep(1000 * sec);
        } catch (InterruptedException ex) {
            data.getLogger().log(Level.SEVERE, "Interrupted");
        }
    }

    private static String toString(Exception ex) {
        return ex.getClass().getName() + "=>" + ex.getMessage();
    }

}
