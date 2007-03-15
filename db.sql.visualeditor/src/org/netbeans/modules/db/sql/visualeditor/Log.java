/* * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.db.sql.visualeditor;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Log utility for VisualSQLEditor
 *
 */
public class Log {

    private static final String   DEFAULT_NAME = "SEVERE";     //NOI18N
    private static final Level    DEFAULT      = Level.SEVERE;
    private static final String   OFF_NAME     = "OFF";        //NOI18N
    private static final Level    OFF_LEVEL    = Level.OFF;
    private static final String[] LEVEL_NAMES  = {"OFF", "SEVERE", "WARNING", "INFO",    //NOI18N
                                                  "CONFIG", "FINE", "FINER", "FINEST" }; //NOI18N
    private static final Level[]  LEVELS       = { Level.OFF, Level.SEVERE, Level.WARNING,
                                                   Level.INFO, Level.CONFIG, Level.FINE,
                                                   Level.FINER, Level.FINEST };

    private static Log log = null;

    private Logger logger = null;
    private Level  level  = DEFAULT;
    private String packageName;

    private static ResourceBundle rb = ResourceBundle.getBundle(
        "org.netbeans.modules.db.sql.visualeditor.Bundle", // NOI18N
        Locale.getDefault());

    Log(String packageName) {
        this.packageName = packageName;
    }

    private Logger getPackageLogger() {
        if (logger == null) {
            String prop = System.getProperty(packageName, DEFAULT_NAME);
            for (int i = 1; i < LEVELS.length; i++) {
                if (prop.toLowerCase().equals(LEVEL_NAMES[i].toLowerCase())) {
                    level = LEVELS[i];
                    break;
                }
            }
            LogManager.getLogManager().addLogger(new Logger(packageName, null)
		{});
            logger = LogManager.getLogManager().getLogger(packageName);
            if (logger == null) {
                System.out.println(packageName + ": "
				   + rb.getString("CANT_GET_LOGGER"));
                return Logger.getLogger("global");
            }
            try {
                logger.setLevel(level);
                Handler handler = new ConsoleHandler();
                handler.setLevel(level);
		// Default handlers don't provide class name or method name, so roll our own
		logger.setUseParentHandlers(false);
		Formatter formatter = new Formatter() {
			public String format(LogRecord record) {
			    StringBuffer s = new StringBuffer();
			    s.append(record.getLevel().getLocalizedName());
			    s.append(' ');
			    if (record.getLoggerName() != null) {
				s.append('[');
				s.append(record.getLoggerName());
				s.append("]: ");
			    }
			    if (record.getSourceClassName() != null) {
				s.append("| ");
				if (record.getLevel().equals(Level.FINEST)) {
				    String className = record.getSourceClassName();
				    s.append(className.substring(Math.max(className.lastIndexOf('.')+1,0)));
				} else {
				    s.append(record.getSourceClassName());
				}
				s.append(' ');
			    }
			    if (record.getSourceMethodName() != null) {
				s.append("|  ");
				s.append(record.getSourceMethodName());
				s.append("( ");
				Object[] parms = record.getParameters();
				if (parms != null) {
				    for (int i = 0; i < parms.length; i++) {
					if (i != 0) {
					    s.append(", ");
					}
					s.append(parms[i]);
				    }
				}
				s.append(" ) ");
			    }
			    if (record.getThrown() != null) {
				s.append("| ");
				s.append(record.getThrown());
			    }
			    if (record.getMessage() != null) {
				s.append('|');
				s.append(record.getMessage());
			    }
			    s.append('\n');
			    return s.toString();
			}
                    };
                handler.setFormatter(formatter);
                logger.addHandler(handler);
            }
            catch (SecurityException e) {
            }
        }

        return logger;
    }

    public static Logger getLogger() {
        if (log == null) {
            log = new Log(Log.class.getPackage().getName());
        }
        return log.getPackageLogger();
    }
}
