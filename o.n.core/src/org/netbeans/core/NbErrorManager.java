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

package org.netbeans.core;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Wraps errormanager with logger.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class NbErrorManager extends Handler {
    /** our root logger */
    static NbErrorManager ROOT = new NbErrorManager();
    
    Exc createExc(Throwable t, Level severity, LogRecord add) {
        LogRecord[] ann = findAnnotations(t, add);
        return new Exc(t, severity, ann, findAnnotations0(t, add, true, new HashSet<Throwable>()));
    }

    public void publish(LogRecord record) {
        if (record.getThrown() != null) {
            Level level = record.getLevel();
            if (level.intValue() == Level.WARNING.intValue() + 1) {
                // unknown level
                level = null;
            }
            if (level != null && level.intValue() == Level.SEVERE.intValue() + 1) {
                // unknown level
                level = null;
            }
            Exc ex = createExc(record.getThrown(), level, record.getLevel().intValue() == 1973 ? record : null);
            NotifyExcPanel.notify(ex);
        }
    }
    
    public void flush() {
        //logWriter.flush();
    }
    
    public void close() throws SecurityException {
        // nothing needed
    }
    
    /** Extracts localized message from a LogRecord */
    static final String getLocalizedMessage(LogRecord rec) {
        ResourceBundle rb = rec.getResourceBundle();
        if (rb == null) {
            return null;
        }
        
        String msg = rec.getMessage();
        if (msg == null) {
            return null;
        }
        
        String format = rb.getString(msg);

        Object[] arr = rec.getParameters();
        if (arr == null) {
            return format;
        }

        return MessageFormat.format(format, arr);
    }

    /** Finds annotations associated with given exception.
     * @param t the exception
     * @return array of annotations or null
     */
    public synchronized LogRecord[] findAnnotations(Throwable t, LogRecord add) {
        return findAnnotations0(t, add, false, new HashSet<Throwable>());
    }
    
    /** If recursively is true it is not adviced to print all annotations
     * because a lot of warnings will be printed. But while searching for
     * localized message we should scan all the annotations (even recursively).
     */
    private synchronized LogRecord[] findAnnotations0(Throwable t, LogRecord add, boolean recursively, Set<Throwable> alreadyVisited) {
        List<LogRecord> l = new ArrayList<LogRecord>();
        Throwable collect = t;
        while (collect != null) {
            if (collect instanceof Callable) {
                Object res = null;
                try {
                    res = ((Callable) collect).call();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (res instanceof LogRecord[]) {
                    LogRecord[] arr = (LogRecord[])res;
                    l.addAll(Arrays.asList(arr));
                }
            }
            collect = collect.getCause();
        }

        if (add != null) {
            l.add(add);
        }

        
        if (recursively) {
            ArrayList<LogRecord> al = new ArrayList<LogRecord>();
            for (Iterator<LogRecord> i = l.iterator(); i.hasNext(); ) {
                LogRecord ano = i.next();
                Throwable t1 = ano.getThrown();
                if ((t1 != null) && (! alreadyVisited.contains(t1))) {
                    alreadyVisited.add(t1);
                    LogRecord[] tmpAnnoArray = findAnnotations0(t1, null, true, alreadyVisited);
                    if ((tmpAnnoArray != null) && (tmpAnnoArray.length > 0)) {
                        al.addAll(Arrays.asList(tmpAnnoArray));
                    }
                }
            }
            l.addAll(al);
        }

        Throwable cause = t.getCause();
        if (cause != null) {
            LogRecord[] extras = findAnnotations0(cause, null, true, alreadyVisited);
            if (extras != null && extras.length > 0) {
                l.addAll(Arrays.asList(extras));
            }
        }
        
        LogRecord[] arr;
        arr = new LogRecord[l.size()];
        l.toArray(arr);
        
        return arr;
    }
    
    /**
     * Another final class that is used to communicate with
     * NotifyExcPanel and provides enough information to the dialog.
     */
    final class Exc {
        /** the original throwable */
        private Throwable t;
        private Date d;
        private LogRecord[] arr;
        private LogRecord[] arrAll; // all - recursively
        private Level severity;
        
        /** @param severity if -1 then we will compute the
         * severity from annotations
         */
        Exc(Throwable t, Level severity, LogRecord[] arr, LogRecord[] arrAll) {
            this.t = t;
            this.severity = severity;
            this.arr = arr == null ? new LogRecord[0] : arr;
            this.arrAll = arrAll == null ? new LogRecord[0] : arrAll;
        }
        
        /** @return message */
        String getMessage() {
            String m = t.getMessage();
            if (m != null) {
                return m;
            }
            return (String)find(1);
        }
        
        /** @return localized message */
        String getLocalizedMessage() {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return m;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2);
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = NbErrorManager.getLocalizedMessage(arrAll[i]);
                if (s != null) {
                    return s;
                }
            }
            return m;
        }
        
        boolean isLocalized() {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return true;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2) != null;
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = NbErrorManager.getLocalizedMessage(arrAll[i]);
                if (s != null) {
                    return true;
                }
            }
            return false;
        }
        
        /** @return class name of the exception */
        String getClassName() {
            return (String)find(3);
        }
        
        /** @return the severity of the exception */
        Level getSeverity() {
            if (severity != null) {
                return severity;
            }
            
            LogRecord[] anns = (arrAll != null) ? arrAll : arr;
            for (int i = 0; i < anns.length; i++) {
                Level s = anns[i].getLevel();
                if (severity == null || s.intValue() > severity.intValue()) {
                    severity = s;
                }
            }
            
            if (severity == null || severity == Level.ALL) {
                // no severity specified, assume this is an error
                severity = t instanceof Error ? Level.SEVERE : Level.WARNING;
            }
            
            return severity;
        }
        
        /** @return date assigned to the exception */
        Date getDate() {
            if (d == null) {
                d = (Date)find(4);
            }
            return d;
        }
        
        void printStackTrace(PrintStream ps) {
            printStackTrace(new PrintWriter(new OutputStreamWriter(ps)));
        }
        /** Prints stack trace of all annotations and if
         * there is no annotation trace then of the exception
         */
        void printStackTrace(PrintWriter pw) {
            // #19487: don't go into an endless loop here
            printStackTrace(pw, new HashSet<Throwable>(10));
        }
        
        private void printStackTrace(PrintWriter pw, Set<Throwable> nestingCheck) {
            if (t != null && !nestingCheck.add(t)) {
                // Unlocalized log message - this is for developers of NB, not users
                Logger l = Logger.getAnonymousLogger();
                l.warning("WARNING - ErrorManager detected cyclic exception nesting:"); // NOI18N
                Iterator it = nestingCheck.iterator();
                while (it.hasNext()) {
                    Throwable t = (Throwable)it.next();
                    l.warning("\t" + t); // NOI18N
                    LogRecord[] anns = findAnnotations(t, null);
                    if (anns != null) {
                        for (int i = 0; i < anns.length; i++) {
                            Throwable t2 = anns[i].getThrown();
                            if (t2 != null) {
                                l.warning("\t=> " + t2); // NOI18N
                            }
                        }
                    }
                }
                l.warning("Be sure not to annotate an exception with itself, directly or indirectly."); // NOI18N
                return;
            }
            /*Heaeder
            pw.print (getDate ());
            pw.print (": "); // NOI18N
            pw.print (getClassName ());
            pw.print (": "); // NOI18N
            String theMessage = getMessage();
            if (theMessage != null) {
                pw.print(theMessage);
            } else {
                pw.print("<no message>"); // NOI18N
            }
            pw.println ();
             */
            /*Annotations */
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getThrown();
                String annotation = NbErrorManager.getLocalizedMessage(arr[i]);
                
                if (annotation == null) annotation = arr[i].getMessage();
                /*
                if (annotation == null && thr != null) annotation = thr.getLocalizedMessage();
                if (annotation == null && thr != null) annotation = thr.getMessage();
                 */
                
                if (annotation != null) {
                    if (thr == null) {
                        pw.println("Annotation: "+annotation);// NOI18N
                    }
                    //else pw.println ("Nested annotation: "+annotation);// NOI18N
                }
            }
            
            // ok, print trace of the original exception too
            // Attempt to show an annotation indicating where the exception
            // was caught. Not 100% reliable but often helpful.
            if (t instanceof VirtualMachineError) {
                // Decomposition may not work here, e.g. for StackOverflowError.
                // Play it safe.
                t.printStackTrace(pw);
            } else {
                // All other kinds of throwables we check for a stack trace.
                // First try to find where the throwable was caught.
                StackTraceElement[] tStack = t.getStackTrace();
                StackTraceElement[] hereStack = new Throwable().getStackTrace();
                int idx = -1;
                for (int i = 1; i <= Math.min(tStack.length, hereStack.length); i++) {
                    if (!tStack[tStack.length - i].equals(hereStack[hereStack.length - i])) {
                        idx = tStack.length - i + 1;
                        break;
                    }
                }
                String[] tLines = decompose(t);
                for (int i = 0; i < tLines.length; i++) {
                    if (i == idx) {
                        pw.print("[catch]"); // NOI18N
                        // Also translate following tab -> space since formatting is bad in
                        // Output Window (#8104) and some mail agents screw it up etc.
                        if (tLines[i].charAt(0) == '\t') {
                            pw.print(' ');
                            tLines[i] = tLines[i].substring(1);
                        }
                    }
                    pw.println(tLines[i]);
                }
            }
            /*Nested annotations */
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getThrown();
                if (thr != null) {
                    LogRecord[] ans = findAnnotations(thr, null);
                    Exc ex = new Exc(thr, null, ans, null);
                    pw.println("==>"); // NOI18N
                    ex.printStackTrace(pw, nestingCheck);
                }
            }
        }
        
        /** Get a throwable's stack trace, decomposed into individual lines. */
        private  String[] decompose(Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            StringTokenizer tok = new StringTokenizer(sw.toString(), "\n\r"); // NOI18N
            int c = tok.countTokens();
            String[] lines = new String[c];
            for (int i = 0; i < c; i++)
                lines[i] = tok.nextToken();
            return lines;
        }
        
        /**
         * Method that iterates over annotations to find out
         * the first annotation that brings the requested value.
         *
         * @param kind what to look for (1, 2, 3, 4, ...);
         * @return the found object
         */
        private Object find(int kind) {
            return find(kind, true);
        }
        
        /**
         * Method that iterates over annotations to find out
         * the first annotation that brings the requested value.
         *
         * @param kind what to look for (1, 2, 3, 4, ...);
         * @return the found object
         */
        private Object find(int kind, boolean def) {
            for (int i = 0; i < arr.length; i++) {
                LogRecord a = arr[i];
                
                Object o = null;
                switch (kind) {
                    case 1: // message
                        o = a.getMessage(); break;
                    case 2: // localized
                        o = NbErrorManager.getLocalizedMessage(a); break;
                    case 3: // class name
                    {
                        Throwable t = a.getThrown();
                        o = t == null ? null : t.getClass().getName();
                        break;
                    }
                    case 4: // date
                        o = new Date(a.getMillis()); break;
                }
                
                if (o != null) {
                    return o;
                }
            }
            
            if (!def)
                return null;
            switch (kind) {
                case 1: // message
                    return t.getMessage();
                case 2: // loc.msg.
                    return t.getLocalizedMessage();
                case 3: // class name
                    return t.getClass().getName();
                case 4: // date
                    return new Date();
                default:
                    throw new IllegalArgumentException(
                        "Unknown " + Integer.valueOf(kind) // NOI18N
                        );
            }
        }
    }
    
}
