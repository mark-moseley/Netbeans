/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */
 
package org.apache.tools.ant.module.api;

import javax.swing.event.ChangeListener;
import java.io.File;
import java.io.Serializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;

/** 
 * Cookie containing the state of an Ant Project.
 * Note that there is a document, and also a parse exception.
 * At least one must be non-null; it is possible for both to be
 * non-null in case there was a valid parse before, and some changes
 * are now invalid.
 */
public interface AntProjectCookie extends Node.Cookie, Serializable {
    /** Get the disk file for the build script.
     * @return the disk file, or null if none (but must be a file object)
     */
    File getFile ();
    /** Get the file object for the build script.
     * @return the file object, or null if none (but must be a disk file)
     */
    FileObject getFileObject ();
    /** Get the DOM document for the build script.
     * @return the document, or null if it could not be parsed
     */
    Document getDocument ();
    /** Get the DOM root element (<code>&lt;project&gt;</code>) for the build script.
     * @return the root element, or null if it could not be parsed
     */
    Element getProjectElement ();
    /** Get the last parse-related exception, if there was one.
     * @return the parse exception, or null if it is valid
     */
    Throwable getParseException ();
    /** Add a listener to changes in the document.
     * @param l the listener to add
     */
    void addChangeListener (ChangeListener l);
    /** Remove a listener to changes in the document.
     * @param l the listener to remove
     */
    void removeChangeListener (ChangeListener l);
    
    /** Extended cookie permitting queries of parse status.
     * If only the basic cookie is available, you cannot
     * determine if a project is already parsed or not, and
     * methods which require it to be parsed for them to return
     * may block until a parse is complete.
     * @since 2.10
     */
    interface ParseStatus extends AntProjectCookie {
        /** Check whether the project is currently parsed.
         * Note that "parsed in error" is still considered parsed.
         * <p>If not parsed, then if and when it does later become
         * parsed, a change event should be fired. A project
         * might become unparsed after being parsed, due to e.g.
         * garbage collection; this need not fire any event.
         * <p>If the project is currently parsed, the methods
         * {@link AntProjectCookie#getDocument},
         * {@link AntProjectCookie#getProjectElement}, and
         * {@link AntProjectCookie#getParseException} should
         * not block.
         * @return true if this project is currently parsed
         */
        boolean isParsed();
    }
    
}
