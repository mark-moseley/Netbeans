/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;

/** 
 * This interface allows a plugin to receive information about what in a module
 * or application has changed since the last deployment.  The change description
 * is cumulative of change description of each child module.
 *
 * @author  George Finklang
 */
public interface AppChangeDescriptor extends EjbChangeDescriptor, ModuleChangeDescriptor {
    
}
