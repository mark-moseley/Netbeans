/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.api.webmodule;

/** Constants ueful for web-based projects;
 *
 * @author  Milan Kuchtiak
 */
public interface WebProjectConstants {

    /**
     * Document root root sources type (source folders for JSPs, HTML ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    
    /**
     * WEB-INF sources type (source folders for TLD files ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    /**
     * WEB-INF sources type (source folders for Tag Files ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_TAGS="tags"; //NOI18N
    
    /**
     * Standard command for redeploying a web project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
}
