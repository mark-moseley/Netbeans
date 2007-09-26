/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.util.logging.LogRecord;

/** Represents an operation on the list of opened projects.
 *
 * @author Jaroslav Tulach
 * @since 1.7
 */
public final class ProjectOp {
    private final String name;
    private final String type;
    private final int number;

    private ProjectOp(String name, String type, int number) {
        this.name = fixName(name, true);
        this.type = fixName(type, false);
        this.number = number;
    }
    
    private static String fixName(String name, boolean nameOrType) {
        if (nameOrType && name.indexOf("Maven") >= 0) {
            return "Maven";
        }
        return name;
    }
    
    /** Human readable name of the project the operation happened on
     */
    public String getProjectDisplayName() {
        return name;
    }

    /** Fully qualified class name of the project.
     */
    public String getProjectType() {
        return type;
    }
    
    /** Number of projects of this type that has been added.
     * @return positive value if some projects were open, negative if some were closed
     */
    public int getDelta() {
        return number;
    }
    
    /** Finds whether the record was an operation on projects.
     * @param rec the record to test
     * @return null if the record is of unknown format or data about the project operation
     */
    public static ProjectOp valueOf(LogRecord rec) {
        if ("UI_CLOSED_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            return new ProjectOp(name, type, -cnt);
        }
        if ("UI_OPEN_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            return new ProjectOp(name, type, cnt);
        }
        return null;
    }
    
    private static String getStringParam(LogRecord rec, int index, String def) {
        if (rec == null) {
            return def;
        }
        Object[] params = rec.getParameters();
        if (params == null || params.length <= index) {
            return def;
        }
        if (params[index] instanceof String) {
            return (String)params[index];
        }
        return def;
    }
}
