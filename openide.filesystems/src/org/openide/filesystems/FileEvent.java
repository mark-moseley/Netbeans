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

package org.openide.filesystems;

import java.util.Date;
import java.util.EventObject;

/** Event for listening on filesystem changes.
* <P>
* By calling {@link #getFile} the original file where the action occurred
* can be obtained.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class FileEvent extends EventObject {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 1028087432345400108L;

    /** Original file object where the action took place. */
    private FileObject file;

    /** time when this event has been fired */
    private long time;

    /** is expected? */
    private boolean expected;

    /***/
    private EventControl.AtomicActionLink atomActionID;

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    */
    public FileEvent(FileObject src) {
        this(src, src);
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * <p>
    * Note that the two arguments of this method need not be identical
    * in cases where it is reasonable that a different file object from
    * the one affected would be listened to by other components. E.g.,
    * in the case of a file creation event, the event source (which
    * listeners are attached to) would be the containing folder, while
    * the action object would be the newly created file object.
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred */
    public FileEvent(FileObject src, FileObject file) {
        super(src);
        this.file = file;
        this.time = System.currentTimeMillis();
    }

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object. Important if FileEvent is created according to
    * existing FileEvent but with another source and file but with the same time.
    */
    FileEvent(FileObject src, FileObject file, long time) {
        this(src, file);
        this.time = time;
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * <p>
    * Note that the two arguments of this method need not be identical
    * in cases where it is reasonable that a different file object from
    * the one affected would be listened to by other components. E.g.,
    * in the case of a file creation event, the event source (which
    * listeners are attached to) would be the containing folder, while
    * the action object would be the newly created file object.
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred
    * @param expected sets flag whether the value was expected*/
    public FileEvent(FileObject src, FileObject file, boolean expected) {
        this(src, file);
        this.expected = expected;
    }

    /** @return the original file where action occurred
    */
    public final FileObject getFile() {
        return file;
    }

    /** The time when this event has been created.
    * @return the milliseconds
    */
    public final long getTime() {
        return time;
    }

    /** Getter to test whether the change has been expected or not.
    */
    public final boolean isExpected() {
        return expected;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName().replaceFirst(".+\\.", ""));
        b.append('[');
        FileObject src = (FileObject) getSource();
        if (src != file) {
            b.append("src=");
            b.append(FileUtil.getFileDisplayName(src));
            b.append(',');
        }
        b.append("file=");
        b.append(FileUtil.getFileDisplayName(file));
        b.append(",time=");
        b.append(new Date(time));
        b.append(",expected=");
        b.append(expected);
        insertIntoToString(b);
        b.append(']');
        return b.toString();
    }
    void insertIntoToString(StringBuilder b) {}

    /** */
    void setAtomicActionLink(EventControl.AtomicActionLink atomActionID) {
        this.atomActionID = atomActionID;
    }

    /** Tests if FileEvent was fired from atomic action.
     * @param run is tested atomic action.
     * @return true if fired from run.
     * @since 1.35
     */
    public boolean firedFrom(FileSystem.AtomicAction run) {
        EventControl.AtomicActionLink currentPropID = this.atomActionID;

        if (run == null) {
            return false;
        }

        while (currentPropID != null) {
            if (run.equals(currentPropID.getAtomicAction())) {
                return true;
            }

            currentPropID = currentPropID.getPreviousLink();
        }

        return false;
    }
}
