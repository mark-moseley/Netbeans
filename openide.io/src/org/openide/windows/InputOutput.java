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

package org.openide.windows;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.openide.util.io.NullOutputStream;
import org.openide.util.io.NullInputStream;

/** An I/O connection to one tab on the Output Window.  To acquire an instance
 * to write to, call, e.g., 
 * <code>IOProvider.getDefault().getInputOutput("someName", false)</code>.
 * To get actual streams to write to, call <code>getOut()</code> or <code>
 * getErr()</code> on the returned instance.
 * <p>
 * Generally it is preferable not to hold a reference to an instance of 
 * <code>InputOutput</code>, but rather to fetch it by name from <code>IOProvider</code> as
 * needed.<p>
 * <b>Note:</b> For historical reasons, the mechanism to clear an output tab
 * is via the method <code>OutputWriter.reset()</code>, though it would have
 * made more sense implemented here.
 * 
 * @see OutputWriter
 * @author   Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan Jancura
 */
public interface InputOutput {


    /** Null InputOutput */
    /*public static final*/ InputOutput NULL = new InputOutput$Null();

    /** Acquire an output writer to write to the tab.
    * This is the usual use of a tab--it writes to the main output pane.
    * @return the writer
    */
    public OutputWriter getOut();

    /** Get a reader to read from the tab.
    * If a reader is ever requested, an input line is added to the
    * tab and used to read one line at a time.
    * @return the reader
    */
    public Reader getIn();

    /** Get an output writer to write to the tab in error mode.
    * This might show up in a different color than the regular output, e.g., or
    * appear in a separate pane.
    * @return the writer
    */
    public OutputWriter getErr();

    /** Closes this tab.  The effect of calling any method on an instance
     * of InputOutput after calling closeInputOutput() on it is undefined.
     */
    public void closeInputOutput();

    /** Test whether this tab has been closed, either by a call to closeInputOutput()
    * or by the user closing the tab in the UI.
    *
    * @see #closeInputOutput
    * @return <code>true</code> if it is closed
    */
    public boolean isClosed();

    /** Show or hide the standard output pane, if separated. Does nothing in either
    * of the available implementations of this API.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setOutputVisible(boolean value);

    /** Show or hide the error pane, if separated.  Does nothing in either
    * of the available implementations of this API.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setErrVisible(boolean value);

    /** Show or hide the input line.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setInputVisible(boolean value);

    /**
    * Ensure this pane is visible.
    */
    public void select ();

    /** Test whether the error output is mixed into the regular output or not.
    * Always true for both available implementations of this API.
    * @return <code>true</code> if separate, <code>false</code> if mixed in
    */
    public boolean isErrSeparated();

    /** Set whether the error output should be mixed into the regular output or not.
    * Note that this method is optional and is not supported by either of the
    * current implementations of InputOutput (core/output and core/output2).
    * @return <code>true</code> to separate, <code>false</code> to mix in
    */
    public void setErrSeparated(boolean value);

    /** Test whether the output window takes focus when anything is written to it.
    * @return <code>true</code> if any write to the tab should cause it to gain
    * keyboard focus <strong>(not recommended)</strong>
    */
    public boolean isFocusTaken();

    /** Set whether the output window should take focus when anything is written to it.
    * <strong>Note that this really means the output window will steal keyboard
    * focus whenever a line of output is written.  This is generally an extremely
    * bad idea and strongly recommended against by most UI guidelines.</strong> 
    * @return <code>true</code> to take focus
    */
    public void setFocusTaken(boolean value);

    /** Flush pending data in the input-line's reader.
    * Called when the reader is about to be reused.
    * @return the flushed reader
    * @deprecated meaningless, does nothing
    */
    public Reader flushReader();

    /** @deprecated Use {@link #NULL} instead. */
    /*public static final*/ Reader nullReader = new InputStreamReader(new NullInputStream());

    /** @deprecated Use {@link #NULL} instead. */
    /*public static final*/ OutputWriter nullWriter = new InputOutput$NullOutputWriter();

}

final class InputOutput$Null extends Object implements InputOutput {
    public InputOutput$Null () {
    }
    
    public OutputWriter getOut() {
        return nullWriter;
    }
    public Reader getIn() {
        return nullReader;
    }
    public OutputWriter getErr() {
        return nullWriter;
    }
    public void closeInputOutput() {
    }
    public boolean isClosed() {
        return true;
    }
    public void setOutputVisible(boolean value) {
    }
    public void setErrVisible(boolean value) {
    }
    public void setInputVisible(boolean value) {
    }
    public void select () {
    }
    public boolean isErrSeparated() {
        return false;
    }
    public void setErrSeparated(boolean value) {
    }
    public boolean isFocusTaken() {
        return false;
    }
    public void setFocusTaken(boolean value) {
    }
    public Reader flushReader() {
        return nullReader;
    }
}

final class InputOutput$NullOutputWriter extends OutputWriter {
    InputOutput$NullOutputWriter() {
        super(new OutputStreamWriter(new NullOutputStream()));
    }
    public void reset() {
    }
    public void println(String s, OutputListener l) {
    }
}

