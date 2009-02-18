/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.eclipse.core.runtime;

/**
 * @author Maros Sandor
 */
public class Status implements IStatus {
    
    public static final IStatus CANCEL_STATUS = null;
    
    public static final IStatus OK_STATUS = null;
    private int severity;
    private String message;
    private Throwable exception;
    private int code;
    private String pluginId;
    
    public Status(int severity, String pluginId, int code, String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
        this.code = code;
        this.pluginId = pluginId;
        this.severity = severity;
    }

    public Status(int severity, String pluginId, String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
        this.pluginId = pluginId;
        this.severity = severity;
    }
    
    public Status(int severity, String pluginId, String message){
        this.message = message;
        this.pluginId = pluginId;
        this.severity = severity;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isOK() {
        return false;
    }

    public int getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
    
    protected void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }
}
