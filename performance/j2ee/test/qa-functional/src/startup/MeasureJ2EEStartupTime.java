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

package startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbPerformanceTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog.
 * Number of starts with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 * Run measurement defined number times, but forget first measured value,
 * it's a attempt to have still the same testing conditions with
 * loaded and cached files.
 *
 * @author Martin.Brehovsky@sun.com, Antonin.Nebuzelsky@sun.com, Marian.Mirilovic@sun.com
 */
public class MeasureJ2EEStartupTime extends NbPerformanceTestCase {
    
    /** String used for unix platform. */
    protected static final String UNIX = "unix";
    
    /** String used for windows platform. */
    protected static final String WINDOWS = "windows";
    
    /** String used for unknown platform. */
    protected static final String UNKNOWN = "unknown";
    
    /** Number of repeated runs on the same userdir (for first run is used new userdir). */
    protected static int repeat = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue();
    
    /** Number of repeated runs on new userdir. */
    protected static int repeatNewUserdir = Integer.getInteger("org.netbeans.performance.repeat.with.new.userdir", 1).intValue();
    
    /** Number used for unknow time. */
    protected final static long UNKNOWN_TIME = -1;
    
    /** Table of the supported platforms. */
    protected static final String [][] SUPPORTED_PLATFORMS = {
        // XXX need to add MacOS
        {"Linux,i386",UNIX},
        {"SunOS,sparc",UNIX},
        {"SunOS,x86",UNIX},
        {"Windows_NT,x86",WINDOWS},
        {"Windows_2000,x86",WINDOWS},
        {"Windows_XP,x86",WINDOWS},
        {"Windows_95,x86",WINDOWS},
        {"Windows_98,x86",WINDOWS},
        {"Windows_Me,x86",WINDOWS}
    };
    
    protected static final String[][] STARTUP_DATA = {
        {"ModuleSystem.readList finished", "ModuleSystem.readList finished, took ","ms"},
        {"Preparation finished", "Preparation finished, took ","ms"},
        {"Window system loaded", "Window system loaded dT=", ""},
        {"Window system shown" , "Window system shown dT=",""},
        {"Start", "IDE starts t=", ""},
        {"End", "IDE is running t=", ""}
    };
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public MeasureJ2EEStartupTime(java.lang.String testName) {
        super(testName);
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDE() throws IOException {
        // don't report first run, try to have still the same testing conditions
        runIDE(getIdeHome(),new File(getWorkDir(),"ideuserdir_prepare"),getMeasureFile(0,0),0);
        
        for (int n=1;n <= repeatNewUserdir; n++){
            for (int i=1; i <= repeat; i++) {
                long measuredTime = runIDEandMeasureStartup(getMeasureFile(i,n), getUserdirFile(n),5000);
                reportPerformance("Startup Time", measuredTime, "ms", i>1?2:1);
            }
        }
    }
    
     /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithOpenedFiles() throws IOException {
        for (int i=1; i <= repeat; i++) {
            long measuredTime = runIDEandMeasureStartup(getMeasureFile(i), getUserdirFile(), 30000);
            reportPerformance("Startup Time with opened J2EE projects", measuredTime, "ms", 2);
        }
        
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithWeb() throws IOException {
        for (int i=1; i <= repeat; i++) {
            long measuredTime = runIDEandMeasureStartup(getMeasureFile(i), getUserdirFile(), 30000);
            reportPerformance("Startup Time with opened Web projects", measuredTime, "ms", 2);
        }
        
    }
    
    /** Run IDE and read measured time from file
     *
     * @param measureFile file where the time of window system painting is stored
     * @throws java.io.IOException
     * @return startup time
     */
    private long runIDEandMeasureStartup(File measureFile, File userdir, long timeout) throws IOException {
        long startTime = runIDE(getIdeHome(),userdir,measureFile,timeout);
        Hashtable measuredValues = parseMeasuredValues(measureFile);
        
        if(measuredValues==null)
            fail("It isn't possible measure startup time");
        
        long runTime=((Long)measuredValues.get("IDE starts t=")).longValue(); // from STARTUP_DATA
        measuredValues.remove("IDE starts t="); // remove from measured values, the rest we will log as performance data
        long endTime=((Long)measuredValues.get("IDE is running t=")).longValue(); // from STARTUP_DATA
        measuredValues.remove("IDE is running t="); // remove from measured values, the rest we will log as performance data
        
        long startupTime = endTime - startTime;
        
        System.out.println("Measured Startup Time=" + startupTime + " (run-start="+(runTime-startTime)+" , end-run="+(endTime-runTime)+")");
        
        if (startupTime <= 0)
            fail("Measured value ["+startupTime+"] is not > 0 !");
        
        reportPerformance("IDE run",(runTime-startTime),"ms",1); 
        
        
        for(int i=0; i<STARTUP_DATA.length; i++){
            if(measuredValues.containsKey(STARTUP_DATA[i][1])){
                long value = ((Long)measuredValues.get(STARTUP_DATA[i][1])).longValue();
                System.out.println(STARTUP_DATA[i][0]+"="+value);
                reportPerformance(STARTUP_DATA[i][0],value,"ms",1); 
            }else{
                System.out.println("Value for "+STARTUP_DATA[i][1]+" isn't present");
            }
        }
        
        return startupTime;
    }
    
    
    /** Get platform on which the code is executed
     * @return current platform name
     */
    protected static String getPlatform() {
        String platformString=(System.getProperty("os.name","")+","+ System.getProperty("os.arch","")).replace(' ','_');
        for (int i=0; i<SUPPORTED_PLATFORMS.length; i++) {
            if (platformString.equalsIgnoreCase(SUPPORTED_PLATFORMS[i][0])) {
                return SUPPORTED_PLATFORMS[i][1];
            }
        }
        return UNKNOWN;
    }
    
    
    
    /** Creates and executes the command line for running IDE.
     * @param ideHome IDE home directory
     * @param userdir User directory
     * @param measureFile file where measured time is stored
     * @throws IOException
     */
    protected static long runIDE(File ideHome, File userdir, File measureFile, long timeout) throws IOException {
        
        //check <userdir>/lock file
        if(new File(userdir, "lock").exists())
            fail("Original Userdir is locked!");
        
        //add guitracker on classpath
        String classpath = System.getProperty("performance.testutilities.dist.jar");
        
        // create jdkhome switch
        String jdkhome = System.getProperty("java.home");
        if(jdkhome.endsWith("jre"))
            jdkhome = jdkhome.substring(0, jdkhome.length()-4);
        
        String platform = getPlatform();
        File ideBinDir = new File(ideHome,"bin");
        String cmd;
        if (platform.equals(WINDOWS)) {
            cmd = (new File(ideBinDir,"netbeans.exe")).getAbsolutePath();
        } else {
            cmd = (new File(ideBinDir,"netbeans")).getAbsolutePath();
        }
        // add other argumens
        // guiltracker lib
        cmd += " --cp:a "+classpath;
        // userdir
        cmd += " --userdir "+userdir.getAbsolutePath();
        // get jdkhome path
        cmd += " --jdkhome "+jdkhome;
        // netbeans full hack
        cmd += " -J-Dnetbeans.full.hack=true";
        // measure argument
        cmd += " -J-Dorg.netbeans.log.startup.logfile="+measureFile.getAbsolutePath();
        // measure argument - we have to set this one to ommit repaint of memory toolbar (see openide/actions/GarbageCollectAction)
        cmd += " -J-Dorg.netbeans.log.startup=tests";
        // close the IDE after startup
        cmd += " -J-Dnetbeans.close=true";
        // wait after startup, need to set longer time for complex startup because rescan rises
        cmd += " -J-Dorg.netbeans.performance.waitafterstartup="+timeout;
        // disable rescaning after startup
        //        cmd += " -J-Dnetbeans.javacore.noscan=true";
        
        System.out.println("Running: "+cmd);
        
        Runtime runtime = Runtime.getRuntime();
        
        long startTime=System.currentTimeMillis();
        
        // need to create out and err handlers
        Process ideProcess = runtime.exec(cmd,null,ideBinDir);
        
        // track out and errs from ide - the last parameter is PrintStream where the
        // streams are copied - currently set to null, so it does not hit performance much
        ThreadReader sout = new ThreadReader(ideProcess.getInputStream(), null);
        ThreadReader serr = new ThreadReader(ideProcess.getErrorStream(), null);
        try {
            int exitStatus = ideProcess.waitFor();
            System.out.println("IDE exited with status = "+exitStatus);
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
            IOException ioe = new IOException("Caught InterruptedException :"+ie.getMessage());
            ioe.initCause(ie);
            throw ioe;
        }
        
        return startTime;
    }
    
    /** Get IDE home directory.
     * @throws IOException
     * @return IDE home directory
     */
    protected File getIdeHome() throws IOException {
        String nbHome = System.getProperty("netbeans.dest.dir");
        File ideHome = new File(nbHome);
        if (!ideHome.isDirectory()) {
            throw new IOException("Cannot found netbeans.dest.dir - supplied value is "+nbHome);
        }
        return ideHome;
    }
    
    /** Get User directory
     * @throws IOException
     * @return User directory
     */
    protected File getUserdirFile(int n) throws IOException {
        return new File(getWorkDir(),"ideuserdir_"+n);
    }
    
    /** Get User directory.
     * User directory is prepared and defined by property
     * <br> <code> userdir.prepared </code> + "/sys/ide"
     * @throws IOException
     * @return User directory
     */
    protected File getUserdirFile() throws IOException {
        return new File(new File(System.getProperty("userdir.prepared"),"sys"),"ide");
    }
    
    /** Get Sketchpad directory.
     * @throws IOException
     * @return Sketchpad directory
     */
    private File getSketchpad() throws IOException {
        String xtestSketchpad = System.getProperty("xtest.sketchpad");
        if (xtestSketchpad == null) {
            throw new IOException("Cannot find xtest.sketchpad");
        } else {
            return new File(xtestSketchpad);
        }
    }
    
    
    /** Get file where measured time will be stored.
     * @param i number of the new userdir used
     * @param j number of the run with the same userdir
     * @throws IOException
     * @return file where the measured time will be stored
     */
    protected File getMeasureFile(int i, int j) throws IOException {
        return new File(getWorkDir(),"measured_startup_"+i+"_"+j+".txt");
    }
    
    
    /** Get file where measured time will be stored.
     * @param j number of the run with the same userdir
     * @throws IOException
     * @return file where the measured time will be stored
     */
    protected File getMeasureFile(int i) throws IOException {
        return new File(getWorkDir(),"measured_startup_"+i+".txt");
    }
    
    /** Parse logged startup time from the file.
     * @param measuredFile file where the startup time is stored
     * @return measured startup time
     */
    protected static Hashtable parseMeasuredValues(File measuredFile) {
        Hashtable measuredValues = new Hashtable();
        
        Hashtable startup_data = new Hashtable();
        for(int i=0; i<STARTUP_DATA.length; i++)
            startup_data.put(STARTUP_DATA[i][1], STARTUP_DATA[i][2]);
                
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(measuredFile));
            String readLine, str, value;
            int begin, end;
            while((readLine = br.readLine())!=null && !startup_data.isEmpty()){
                try {
                    java.util.Iterator iter = startup_data.keySet().iterator();
                    while(iter.hasNext()){
                        str = (String)iter.next();
                        begin = readLine.indexOf(str);
                        if(begin!=-1){
                            end = readLine.indexOf((String)startup_data.get(str));
                            
                            if(end<=begin) end=readLine.length();
                            
                            value = readLine.substring(begin+str.length(), end);
                            measuredValues.put(str, new Long(value));
                            startup_data.remove(str);
                            break;
                        }
                            
                    }
                    
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace(System.err);
                    return null;
                }
            }
            return measuredValues;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return null;
                }
            }
        }
    }
    
    
    protected static class ThreadReader implements Runnable {
        
        private Thread thread;
        private BufferedReader br;
        private PrintStream out;
        private String s;
        
        ThreadReader(InputStream in, PrintStream out) {
            br = new BufferedReader(new InputStreamReader(in));
            this.out = out;
            thread = new Thread(this);
            thread.start();
        }
        
        public void run() {
            Thread myThread = Thread.currentThread();
            while (thread == myThread) {
                try {
                    s = br.readLine();
                    if (s == null) {
                        stop();
                        break;
                    } else {
                        if (out != null) {
                            out.println(s);
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    if (out != null) {
                        out.println("Caught IOE when reading IDE's out or err streams:"+ioe);
                    }
                    stop();
                }
            }
        }
        
        public void stop() {
            thread = null;
        }
        
    }
    
}
