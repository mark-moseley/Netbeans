/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.Exceptions;

/** Don't use this class outside fo core, copy its impl, if you really think
 * you need it. Prefered way is to use DialogDisplayer.notifyLater(...);
 *
 * @author Jaroslav Tulach
 */
public final class UIException {
    
    /** Creates a new instance of UIException */
    private UIException() {
    }
    
    public static void annotateUser(
        Throwable t,
        String msg,
        String locMsg,
        Throwable stackTrace,
        Date date
    ) {
        AnnException ex = AnnException.findOrCreate(t, true);
        LogRecord rec = new LogRecord(OwnLevel.USER, msg);
        if (stackTrace != null) {
            rec.setThrown(stackTrace);
        }
        ex.addRecord(rec);
        
        if (locMsg != null) {
            Exceptions.attachLocalizedMessage(t, locMsg);
        }
    }
    private static final class OwnLevel extends Level {
        public static final Level USER = new OwnLevel("USER", 1973); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of UserLevel
    private static final class AnnException extends Exception implements Callable/*<LogRecord[]>*/ {
        private List/*<LogRecord>*/ records;

        static AnnException findOrCreate(Throwable t, boolean create) {
            if (t instanceof AnnException) {
                return (AnnException)t;
            }
            if (t.getCause() == null) {
                if (create) {
                    t.initCause(new AnnException());
                }
                return (AnnException)t.getCause();
            }
            return findOrCreate(t.getCause(), create);
        }

        private AnnException() {
        }

        public synchronized void addRecord(LogRecord rec) {
            if (records == null) {
                records = new ArrayList/*<LogRecord>*/();
            }
            records.add(rec);
        }

        public Object/*LogRecord[]*/ call() {
            List/*<LogRecord>*/ r = records;
            LogRecord[] empty = new LogRecord[0];
            return r == null ? empty : (LogRecord[])r.toArray(empty);
        }
    } // end AnnException
}

