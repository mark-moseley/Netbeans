/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.util.ArrayList;
import java.util.Enumeration;
import java.lang.reflect.Array;
import java.io.*;
import java.text.MessageFormat;

import org.openide.compiler.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.loaders.*;
import org.openide.text.IndentEngine;

/** Support for property editor for indentation engine.
*
* @author   Jaroslav Tulach
*/

public class IndentEngineEditor extends ServiceTypeEditor {

    public IndentEngineEditor () {
        super (IndentEngine.class, "LAB_ChooseIndentEngine", IndentEngine.getDefault()); // NOI18N
    }

}
