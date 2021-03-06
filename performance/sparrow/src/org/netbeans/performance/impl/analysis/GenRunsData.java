/*
 * GenRunsData.java
 *
 * Created on October 9, 2002, 11:24 PM
 */

package org.netbeans.performance.impl.analysis;
import java.util.*;
import org.netbeans.performance.spi.*;
import org.netbeans.performance.impl.logparsing.*;
import java.io.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
/** Ant task to gather data from the logfiles produced by a run,
 * as pointed to in the log file generated from the initial log file
 * the ant task generates (which just contains start and end times 
 * and pointers to log files generated by the IDE's activity).
 *
 * @author  Tim Boudreau
 */
public class GenRunsData extends Analyzer {

    public String analyze() throws BuildException {
        NbRunLog log = new NbRunLog (datafile);
        try {
            log.writeToFile (outfile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException ("Exception while creating run set data.", ioe);
        }
        return outfile; 
    }

}
