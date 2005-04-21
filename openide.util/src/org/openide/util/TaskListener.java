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
package org.openide.util;

import java.util.EventListener;


/** Listener which can be notifies when a task finishes.
* @see Task
*
* @author Jaroslav Tulach
*/
public interface TaskListener extends EventListener {
    /** Called when a task finishes running.
    * @param task the finished task
    */
    public void taskFinished(Task task);
}
