/*
 * NbRunLog.java
 *
 * Created on October 8, 2002, 4:43 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
import java.io.*;
/**Wrapper class for master log output by the testing suite.
 * This log contains start and end time of the run, and pointers
 * to files generated by the run (such as ide.cfg) which are
 * copied to the location listed in the run log for later parsing.<P>
 * The format of this file is very simple - name value pairs
 * delimited by "=" and separated by "%%%".  Such a format is
 * desirable particularly since it may be useful to merely
 * concatenate a series of such files to build data about additional
 * runs.
 *
 * @author  Tim Boudreau
 */
public class NbRunLog extends AbstractLogFile {
    private static final String RUN_LOG="run";
    
    /** Creates a new instance of GcLog using the
     * specified file.  */
    public NbRunLog(String filename) {
        super(filename);
        name = RUN_LOG;
    } 
    
    /**Parse out all of the garbage collection entries from the
     * log file, and build some name-value statistics about them.
     */
    protected void parse() throws ParseException {
        String s;
        try {
            s=getFullText();
        } catch (IOException ioe) {
            throw new ParseException ("Exception reading logfile to parse: " + getFileName(), ioe);
        }
        StringTokenizer tk = new StringTokenizer(s, "%%%");
        int index;
        String curr, name, value;
        
        TestSet currSet=null;
        FolderAggregation currPlatform=null;
        FolderAggregation currPlatformConfig=null;
        FolderAggregation currRun=null;
        System.out.println(s);
        while (tk.hasMoreElements()) {
            curr = tk.nextToken();
            index = curr.indexOf(":");
            if (index != -1) {
                name = curr.substring(0, index);
                value = curr.substring(index+1);
                if (name.equals("TEST_SET")) {
                    currSet = TestSet.createRegisteredTestSet(value);
                    addElement(currSet);
                } else {
                    if (name.equals("PLATFORM")) {
                        currPlatform = new FolderAggregation(value);
                        currSet.addElement(currPlatform);
                    } else {
                        if (name.equals("PLATFORM_CONFIG")) {
                            currPlatformConfig = new FolderAggregation(value);
                            currPlatform.addElement(currPlatformConfig);
                        } else {
                            try {
                                if (name.equals("TEST_RUN")) {
                                    int idx = value.indexOf (TestSet.NB_TEST_DATA_SEPARATOR);
                                    String runName = value.substring (0, idx);
                                    String rundata = value.substring (idx+TestSet.NB_TEST_DATA_SEPARATOR.length());
                                    System.out.println("RUN: " + runName);
                                    currRun = new FolderAggregation (runName);
                                    currSet.createRun(currRun, rundata);
                                    currPlatformConfig.addElement(currRun);
                                } else {
                                    currRun.addElement(new NameValueLogElement(name, value));
                                }
                            } catch (DataNotFoundException dnfe) {
                                System.out.println("FAILURE: " + dnfe.getMessage() + "\nRemoving this test set from the generated output.");
//                                currPlatformConfig.elements.remove (currSet);
                                //if it's a real parsing error, there's garbage in the
                                //logs and something's broken, so abort
                                if (dnfe instanceof ParseException) throw dnfe;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**Test execution for debugging */
    public static void main(String[] args) throws Exception {
        NbRunLog lg = new NbRunLog("/space/nbsrc/performance/gc2/report/outdata.dat");
        lg.checkParsed();
        lg.writeToFile ("/tmp/SerThing.ser");
        Iterator i = lg.iterator();
        LogElement curr;
        HashSet hs = new HashSet();
        
        while (i.hasNext()) {
            curr = (LogElement) i.next();
//            if (hs.contains (curr)) System.out.println("DUPLICATE:");
            hs.add(curr);
            System.out.println(curr.getPath());// + " in " + curr.getParent() + "=" + curr.getClass());
        }
        
        lg.toHTML().writeToFile("/tmp/test.html");
        /*
        LogElement el = lg.findElement("/run/GC Tuning/Solaris 5.8/Unknown/permTweaksSurvivorRatio1/sysinfo/Product Version");
        System.out.println("FOUND: " + el);  
        el = lg.findElement("/run/GC Tuning/Solaris 5.8/Unknown/permTweaksSurvivorRatio1/gcinfo/Heap growth events");
        System.out.println("FOUND: " + el);  
        el = lg.findElement("/run/GC Tuning/Solaris 5.8/Unknown/permTweaksSurvivorRatio1");
        System.out.println("FOUND: " + el);  
        */
    }
    
}
