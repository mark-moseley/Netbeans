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

package org.openide.windows;

/** Event fired when something happens to a line in the Output Window.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.11 Dec 01, 1997
*/
public abstract class OutputEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4809584286971828815L;
    /** Create an event.
    * @param src the tab in question
    */
    public OutputEvent (InputOutput src) {
        super (src);
    }

    /** Get the text on the line.
    * @return the text
    */
    public abstract String getLine ();

    /** Get the Output Window tab in question.
    * @return the tab
    */
    public InputOutput getInputOutput() {
        return (InputOutput) getSource();
    }
}
