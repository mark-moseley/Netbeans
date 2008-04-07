/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.openide.*;
import org.openide.filesystems.*;

/** Loader for any kind of <code>MultiDataObject</code>. It provides
* support for recognition of a composite data object and registering
* entries into it.
*
* @author Jaroslav Tulach
*/
public abstract class MultiFileLoader extends DataLoader {
    private static final long serialVersionUID=1521919955690157343L;


    /** Creates new multi file loader.
     * @param representationClass the representation class
     * @deprecated Use MultiFileLoader#MultiFileLoader(String) instead.
    */
    @Deprecated
    protected MultiFileLoader(Class<? extends DataObject> representationClass) {
        super(representationClass);
    }

    /** Creates new multi file loader.
    * @param representationClassName the fully qualified name of the
    * representation class.
    *
    * @since 1.10
    */
    protected MultiFileLoader (String representationClassName) {
        super (representationClassName);
    }

    /*  Provides standard implementation for recognizing files in the
    * loader. First of all the findEntry method is called to allow the
    * subclass to find right entry for the
    *
    * @param fo file object to recognize
    * @param recognized recognized files buffer.
    * @exception DataObjectExistsException if the data object for specific
    *    primary file already exists (thrown by constructor of DataObject)
    * @exception IOException if the object is recognized but cannot be created
    *
    * @return suitable data object or <CODE>null</CODE> if the handler cannot
    *   recognize this object (or its group)
    */
    protected final DataObject handleFindDataObject (
        FileObject fo, RecognizedFiles recognized ) throws IOException {
        // finds primary file for given file
        FileObject primary = findPrimaryFileImpl (fo);

        // if this loader does not recognizes this file => return
        if (primary == null) return null;


        boolean willLog = ERR.isLoggable(Level.FINE);
        
        if (willLog) {
            ERR.fine(getClass().getName() + " is accepting: " + fo); // NOI18N
        }

        if (primary != fo) {
            if (willLog) {
                ERR.fine("checking correctness: primary is different than provided file: " + primary + " fo: " + fo); // NOI18N
            }
            Enumeration en = DataLoaderPool.getDefault().allLoaders(primary);
            for (;;) {
                DataLoader l = (DataLoader)en.nextElement();
                if (l == this) {
                    ERR.fine("ok, consistent"); // NOI18N
                    break;
                }
                if (l instanceof MultiFileLoader) {
                    MultiFileLoader ml = (MultiFileLoader)l;
                    if (ml.findPrimaryFile(primary) == primary) {
                        if (willLog) {
                            ERR.fine("loader seems to also take care of the file: " + ml);
                        }
                        DataObject snd;
                        try {
                            snd = ml.findDataObject(primary, recognized);
                        } catch (DataObjectExistsException ex) {
                            snd = ex.getDataObject();
                        }
                        if (snd != null) {
                            return null;
                        }
                    }
                }
            }
        }

        MultiDataObject obj;
        try {
            // create the multi object
            obj = createMultiObject (primary);
            if (willLog) {
                ERR.fine(getClass().getName() + " created object for: " + fo + " obj: " + obj); // NOI18N
            }
        } catch (DataObjectExistsException ex) {
            // object already exists
            DataObject dataObject = ex.getDataObject ();
            if (willLog) {
                ERR.fine(getClass().getName() + " object already exists for: " + fo + " obj: " + dataObject); // NOI18N
            }
            
            if (dataObject.getLoader () != this) {
                if (willLog) {
                    ERR.fine(getClass().getName() + " loader is wrong: " + dataObject.getLoader().getClass().getName()); // NOI18N
                }

                if (dataObject.getLoader() instanceof MultiFileLoader) {
                    MultiFileLoader mfl = (MultiFileLoader)dataObject.getLoader();
                    FileObject loaderPrimary = mfl.findPrimaryFileImpl(fo);
                    ERR.log(Level.FINE, "Its primary file is {0}", loaderPrimary); // NOI18N
                    if (loaderPrimary != null && dataObject.getPrimaryFile() != loaderPrimary) {
                        ERR.log(Level.FINE, "Which is different than primary of found: {0}", dataObject); // NOI18N

                        Enumeration before = DataLoaderPool.getDefault().allLoaders(fo);
                        while (before.hasMoreElements()) {
                            Object o = before.nextElement();
                            if (o == mfl) {
                                ERR.log(Level.FINE, "Returning null"); // NOI18N
                                return null;
                            }
                            if (o == this) {
                                ERR.log(Level.FINE, "The loader" + mfl + " is after " + this + ". So do break."); // NOI18N
                                break;
                            }
                        }
                    }
                }

                // try to update the data object by allowing other 
                // loaders to take care of the object
                dataObject = checkCollision (dataObject, fo);
            }
            
            if (!(dataObject instanceof MultiDataObject)) {
                // but if it is not MultiDataObject, propadate the exception
                if (willLog) {
                    ERR.fine(getClass().getName() + " object is not MultiDataObject: " + dataObject); // NOI18N
                }
                throw ex;
            }
            obj = (MultiDataObject)dataObject;
        } catch (IOException ex) {
            ERR.log(Level.FINE, null, ex);
            throw ex;
        }

        if (obj.getLoader () != this) {
            if (willLog) {
                ERR.fine(getClass().getName() + " wrong loader: " + obj.getLoader().getClass().getName()); // NOI18N
            }
            // this primary file is recognized by a different
            // loader. We should not add entries to it
            return obj;
        }

        // mark all secondary entries used
        if (willLog) {
            ERR.fine(getClass().getName() + " marking secondary entries"); // NOI18N
        }
        obj.markSecondaryEntriesRecognized (recognized);

        // if the file is not between
        if (willLog) {
            ERR.fine(getClass().getName() + " register entry: " + fo); // NOI18N
        }
        org.openide.loaders.MultiDataObject.Entry e = obj.registerEntry (fo);
        if (willLog) {
            ERR.fine(getClass().getName() + " success: " + e); // NOI18N
        }

        return obj;
    }


    /** For a given file finds the primary file.
    * @param fo the (secondary) file
    *
    * @return the primary file for the file or <code>null</code> if the file is not
    *   recognized by this loader
    */
    protected abstract FileObject findPrimaryFile (FileObject fo);

    /** Creates the right data object for a given primary file.
    * It is guaranteed that the provided file will actually be the primary file
    * returned by {@link #findPrimaryFile}.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected abstract MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException;

    /** Creates the right primary entry for a given primary file.
    *
    * @param obj requesting object
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected abstract MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile);

    /** Creates a new secondary entry for a given file.
    * Note that separate entries must be created for every secondary
    * file within a given multi-file data object.
    *
    * @param obj requesting object
    * @param secondaryFile a secondary file
    * @return the entry
    */
    protected abstract MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile);

    /** Called when there is a collision between a data object that 
    * this loader tries to create and already existing one.
    * 
    * @param obj existing data object
    * @param file the original file that has been recognized by this loader
    *    as bellonging to obj data object
    * @return the data object created for this or null
    */
    DataObject checkCollision (DataObject obj, FileObject file) {
        /* JST: Make protected when necessary. Do not forget to
        * change UniFileDataLoader too.
        */
        FileObject primary = obj.getPrimaryFile ();
        
        /*Set refusing = */DataObjectPool.getPOOL().revalidate (
            new HashSet<FileObject> (Collections.singleton(primary))
        );
            // ok, the obj is discarded
        DataObject result = DataObjectPool.getPOOL().find (primary);
        return result;
    }
    
    /** Called when an entry of the data object is deleted.
    * 
    * @param obj the object to check
    */
    void checkConsistency (MultiDataObject obj) {
        /* JST: Make protected when necessary. Do not forget to
        * change UniFileDataLoader too.
        */
        FileObject primary = obj.getPrimaryFile ();
        if (primary.equals (findPrimaryFileImpl (primary))) {
            // ok recognized
            return;
        }
        
        // something is wrong the loader does not recognize the data 
        // object anymore
        try {
            obj.setValid (false);
        } catch (java.beans.PropertyVetoException ex) {
            // ignore
        }
    }
    
    
    
    /** Called before list of files belonging to a data object
    * is returned from MultiDataObject.files () method. This allows 
    * each loader to perform additional tests and update the set of
    * entries for given data object.
    * <P>
    * Current implementation scans all files in directory.
    * 
    * @param obj the object to test
    */
    void checkFiles (MultiDataObject obj) {
        /* JST: Make protected (and rename) when necessary. Do not forget to
        * change UniFileDataLoader too.
        */


        FileObject primary = obj.getPrimaryFile ();
        assert primary != null : "Object " + obj + " cannot have null primary file"; // NOI18N
        FileObject parent = primary.getParent ();
        assert parent != null : "Object " + obj + " cannot have null parent file"; // NOI18N

        FileObject[] arr = parent.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            FileObject pf = findPrimaryFileImpl (arr[i]);

            if (pf == primary) {
                // this object could belong to this loader
                try {
                    // this will go thru regular process of looking for
                    // data object and register this file with the right (but not
                    // necessary this one) data object
                    DataObject.find (arr[i]);
                } catch (DataObjectNotFoundException ex) {
                    // ignore
                }
            }
        }
    }

    MultiDataObject.Entry createSecondaryEntryImpl (MultiDataObject obj, FileObject secondaryFile) {
        return createSecondaryEntry (obj, secondaryFile);
    }

    FileObject findPrimaryFileImpl (FileObject fo) {
        return findPrimaryFile (fo);
    }
}
