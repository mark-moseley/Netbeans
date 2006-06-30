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

package org.netbeans.modules.web.core.jsploader;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.actions.*;

import org.netbeans.modules.java.*;

/** The DataLoader for servlets (JavaDataObjects), which have been generated by JaSPer
* from JSP pages. Recognizes by string <code>_jsp_</code> in the name of the file.
* Does not recognize associated .class files, which are recognized as a separate ClassDataObject
*
* @author Petr Jiricka
*/
public class JspServletDataLoader extends JavaDataLoader {

    /** serialVersionUID */
    private static final long serialVersionUID = -6033464827752236719L;

    public static final String JSP_MARK = "_jsp";   // NOI18N

    /** Creates a new JspServletDataLoader
     * 
     */ 
    public JspServletDataLoader() {
        this ("org.netbeans.modules.web.core.jsploader.JspServletDataObject"); // NOI18N
    }

    /** Creates a new JspServletDataLoader
     * 
     */ 
    public JspServletDataLoader(String repClass) {
        super (repClass);
    }
    
    /** Creates a new JspServletDataLoader
     * 
     */ 
    public JspServletDataLoader(Class recognizedObject) {
        super (recognizedObject);
    }

    
    /** Gets default display name. Overrides superclass mthod. */
    protected String defaultDisplayName() {
        return NbBundle.getBundle(JspServletDataLoader.class).getString("PROP_JspServletLoader_Name");
    }
    
    /** Gets default actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        return new SystemAction [] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }

    /** For a given file finds a primary file.
    * @param fo the file to find primary file for
    *
    * @return the primary file for the file or null if the file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {

        // detects  *_jsp*.java
        FileObject javaPrim = super.findPrimaryFile(fo);
        if (javaPrim == null)
            return null;
        
        // if there is a source JSP set then this is generated from a JSP
        if (javaPrim.getAttribute(JspServletDataObject.EA_ORIGIN_JSP_PAGE) != null) {
            return javaPrim;
        }

        // PENDING: old way of recognition was by name, need to remove this later
        //if (javaPrim.getName().lastIndexOf(JSP_MARK) != -1)
        //    return javaPrim;
            
        return null;
    }

    /** Create the <code>JspServletDataObject</code>.
    * Subclasses should rather create their own data object type.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new JspServletDataObject(primaryFile, this);
    }

}
