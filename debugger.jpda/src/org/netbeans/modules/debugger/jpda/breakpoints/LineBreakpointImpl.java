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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;



/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N
    
    private LineBreakpoint      breakpoint;
    private int                 lineNumber;
    private BreakpointsReader   reader;
    
    
    public LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session,
        SourcePath sourcePath
    ) {
        super (breakpoint, reader, debugger, session);
        this.reader = reader;
        this.breakpoint = breakpoint;
        lineNumber = breakpoint.getLineNumber ();
        setSourceRoot(sourcePath.getSourceRoot(breakpoint.getURL()));
        set ();
    }
    
    void fixed () {
        logger.fine("LineBreakpoint fixed: "+this);
        lineNumber = breakpoint.getLineNumber ();
        super.fixed ();
    }
    
    protected void setRequests () {
        lineNumber = breakpoint.getLineNumber ();
        String className = reader.findCachedClassName(breakpoint);
        if (className == null) {
            className = EditorContextBridge.getClassName (
                breakpoint.getURL (), 
                lineNumber
            );
            if (className != null) {
                reader.storeCachedClassName(breakpoint, className);
            }
        }
        if (className == null) {
            //HACK: for JSPs.
            //PENDING
            className = breakpoint.getURL ();
            setClassRequests (
                new String[] {
                    className
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, true);
        } else {
            // HACK
            // EditorContext does not provide the real class name, it does not
            // provide anonymous inner classes. Therefore we need to add a request
            // for className$ as well.
            
            logger.fine("LineBreakpoint "+breakpoint+" - setting request for "+className);
            setClassRequests (
                new String[] {
                    className,
                    className + "$*", // innerclasses
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, false);
        }
    }

    protected void classLoaded (ReferenceType referenceType) {
        logger.fine("Class "+referenceType+" loaded for breakpoint "+breakpoint);
        
        String[] reason = new String[] { null };
        List locations = getLocations (
            referenceType,
            breakpoint.getStratum (),
            breakpoint.getSourceName (),
            breakpoint.getSourcePath(),
            lineNumber,
            reason
        );
        if (locations.isEmpty()) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unable to submit line breakpoint to "+referenceType.name()+
                    " at line "+lineNumber+", reason: "+reason[0]);
            return;
        } 
        for (Iterator it = locations.iterator(); it.hasNext();) {
            Location location = (Location)it.next();
            try {           
                BreakpointRequest br = getEventRequestManager ().
                    createBreakpointRequest (location);
                addEventRequest (br);
                //System.out.println("Breakpoint " + br + location + "created");
            } catch (VMDisconnectedException e) {
            }
        }
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent) {
            return perform (
                breakpoint.getCondition (),
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        }
        return super.exec (event);
    }
    
    private static List getLocations (
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason
    ) {
        try {
            List locations = locationsOfLineInClass(referenceType, stratum,
                                                    sourceName, bpSourcePath,
                                                    lineNumber, reason);
            if (locations.isEmpty()) {
                // add lines from innerclasses
                Iterator i = referenceType.nestedTypes ().iterator ();
                while (i.hasNext ()) {
                    ReferenceType rt = (ReferenceType) i.next ();
                    locations = locationsOfLineInClass(rt, stratum, sourceName,
                                                       bpSourcePath, lineNumber,
                                                       reason);
                    if (!locations.isEmpty()) {
                        break;
                    }
                }
            }
            if (locations.isEmpty()) {
                reason[0] = "No executable location available at line "+lineNumber;
            }
            return locations;
        } catch (AbsentInformationException ex) {
            // we are not able to create breakpoint in this situation. 
            // should we write some message?!?
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = "Line number information is missing in the class file.";
        } catch (ObjectCollectedException ex) {
            // no problem, breakpoint will be created next time the class 
            // is loaded
            // should not occurre. see [51034]
            reason[0] = ex.getLocalizedMessage();
        } catch (ClassNotPreparedException ex) {
            // should not occurre. VirtualMachine.allClasses () returns prepared
            // classes only. But...
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        } catch (InternalException iex) {
            // Something wrong in JDI
            ErrorManager.getDefault().annotate(iex, 
                    NbBundle.getMessage(LineBreakpointImpl.class,
                    "MSG_jdi_internal_error") );
            ErrorManager.getDefault().notify(iex);
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = iex.getLocalizedMessage();
        }
        return Collections.EMPTY_LIST;
    }
    
    private static List<Location> locationsOfLineInClass(
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason) throws AbsentInformationException, ObjectCollectedException,
                                ClassNotPreparedException, InternalException {
        List<Location> list = referenceType.locationsOfLine (
            stratum,
            sourceName,
            lineNumber
        );

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("LineBreakpoint: locations for ReferenceType=" +
                    referenceType + ", stratum=" + stratum + 
                    ", source name=" + sourceName + ", bpSourcePath=" +
                    bpSourcePath+", lineNumber=" + lineNumber + 
                    " are: {" + list + "}");
        }
        if (!list.isEmpty ()) {
            if (bpSourcePath == null)
                return list;
            bpSourcePath = bpSourcePath.replace(java.io.File.separatorChar, '/');
            ArrayList<Location> locations = new ArrayList<Location>(list.size());
            for (Iterator<Location> it = list.iterator(); it.hasNext();) {
                Location l = it.next();
                String lSourcePath = l.sourcePath().replace(java.io.File.separatorChar, '/');
                lSourcePath = normalize(lSourcePath);
                if (lSourcePath.equals(bpSourcePath)) {
                    locations.add(l);
                } else {
                    reason[0] = "Breakpoint source path '"+bpSourcePath+"' is different from the location source path '"+lSourcePath+"'.";
                }
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("LineBreakpoint: relevant location(s) for path '" + bpSourcePath + "': " + locations);
            }
            if (!locations.isEmpty())
                return locations;
        }
        return Collections.emptyList();
    }
    
    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    private static String normalize(String path) {
      Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
      Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");
      
      for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); )
      {
        path = m.replaceAll("$1");
        m = thisDirectoryPattern.matcher(path);
      }
      for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); )
      {
        if (!m.group(2).equals("..")) {
          path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
          m = parentDirectoryPattern.matcher(path);        
        }
      }
      return path;
    }
}

