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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/** 
* A PrintWriter subclass for writing to a tab in the output window.  To 
* create hyperlinked lines, call <code>println</code>, passing an instance
* of <code>OutputListener</code> which should be called when a line is 
* clicked or the caret in the output window enters it.
*
* @author Ales Novak
* @version 0.13 Feb 24, 1997
*/
public abstract class OutputWriter extends PrintWriter {
    /** Make an output writer.
    * @param w the underlying writer
    */
    protected OutputWriter (Writer w) {
        super(w);
    }

    /** Print a line which will be displayed as a hyperlink, calling the 
    * passed <code>OutputListener</code> if it is clicked, if the caret 
    * enters it, or if the enter key is pressed over it.
    * 
    *
    * @param s a string to print to the tab
    * @param l a listener that will receive events about this line
    * @throws IOException if the string could not be printed
    */
    public abstract void println(String s, OutputListener l) throws IOException;

    /** Print a line which will be displayed as a hyperlink, calling the 
     * passed <code>OutputListener</code> if it is clicked, if the caret 
     * enters it, or if the enter key is pressed over it. 
     * 
     * Implementors of this class 
     * are encouraged to override this method, which is not abstract for backward 
     * compatibility reasons only.
     *
     * @param s a string to print to the tab
     * @param l a listener that will receive events about this line
     * @param important mark the line as important. 
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling 
     *        or highlight the hyperlink.
     * @throws IOException if the string could not be printed
     * @since 1.5
     */
    public void println(String s, OutputListener l, boolean important) throws IOException {
        println(s, l);
    }
    
    /** Clear the output pane.
    * Expect this method to be deprecated in a future release and an 
    * equivalent created in <code>InputOutput</code>.  It is ambiguous what it means
    * to reset stdout but not stderr, etc.  For the current implementation, reset
    * should be called on the stdout.
    * 
    * @throws IOException if there is a problem
    */
    public abstract void reset() throws IOException;
}
