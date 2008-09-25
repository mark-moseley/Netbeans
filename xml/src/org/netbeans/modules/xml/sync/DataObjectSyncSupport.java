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
package org.netbeans.modules.xml.sync;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.text.Document;

import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.cookies.CookieManagerCookie;
import org.netbeans.modules.xml.util.Util;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;


/**
 * Simple representations manager. It handles mutual exclusivity of File and
 * Text representations because of possible problems with modified status and
 * save cookie management.
 * <p>
 * It also always adds Text representation if Tree representation was required.
 * This is workaround adding undo feature to tree operations via text undo.
 * Also only Text representation needs to take care about modifications and save().
 *
 * @author  Petr Kuzel
 * @version 
 */
public class DataObjectSyncSupport extends SyncSupport implements Synchronizator {
    
    private final Vector reps;
        
    private final CookieManagerCookie cookieMgr;

    
    /** Creates new DataObjectSyncSupport */
    public DataObjectSyncSupport(XMLDataObjectLook dobj) {
        super((DataObject)dobj);
        reps = new Vector(3);
        cookieMgr = dobj.getCookieManager();

        Representation basic = new FileRepresentation (getDO(), this);
        reps.add(basic);
    }


    public void representationChanged(Class type) {
        super.representationChanged(type);
    }
    
    /*
     * Return conditional set of representations.
     *
     */
    protected Representation[] getRepresentations() {
        return (Representation[]) reps.toArray(new Representation[0]);
    }

    /**
     * Select from loaded representation proper one that can be used as primary.
     */
    public Representation getPrimaryRepresentation() {
        
        final Class priority[] = new Class[] {  //??? it should be provided by protected method
            Document.class, 
//              TreeDocumentRoot.class,
            FileObject.class
        };
        
        Representation all[] = getRepresentations();
        
        for (int i = 0; i<priority.length; i++) {
            for (int r = 0; r<all.length; r++) {
                Representation rep = all[r];
                if (rep.isValid() == false) 
                    continue;
                if (rep.represents(priority[i])) {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Primary rep = " + rep); // NOI18N

                    return rep;
                }
            }
        }
        
        throw new IllegalStateException("No primary representation found: " + reps); // NOI18N
    }
    
    /*
     * Manipulate appropriare cookies at data object.
     * Keep text and file rpresentation as mutually exclusive.
     */
    public void addRepresentation(Representation rep) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Sync addRepresentation " + rep); // NOI18N

        if (rep.represents(Document.class)) {
            for (Iterator it = reps.iterator(); it.hasNext();) {
                Representation next = (Representation) it.next();
                if (next.represents(FileObject.class)) {
                    it.remove();
                }                               
            }
        } else if (rep.level() > 1) {
            
            // load also text representation, tree cannot live without it
            
            loadTextRepresentation();
        }
        reps.add(rep);
    }

    

    /*
     * Manipulate appropriare cookies at data object.
     * Keep text and file rpresentation as mutually exclusive.
     */    
    public void removeRepresentation(Representation rep) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Sync removeRepresentation " + rep); // NOI18N
        
        boolean modelLoaded = false;

        if (rep.represents(Document.class)) {
            
            // check whether tree representation is loaded
            

            for (Iterator it = reps.iterator(); it.hasNext();) {
                Representation next = (Representation) it.next();
                if (next.level() > 1) {
                    modelLoaded = true;
                }                               
            }

            if (modelLoaded == false) {
            
                Representation basic = new FileRepresentation (getDO(), this);
                reps.add(basic);
            
            } else {     
                
                // reload text representation, tree cannot live without it

                loadTextRepresentation();
            }
        }                        
        reps.remove(rep);

        if ( modelLoaded ) {//&& ( getDO().isValid() ) ) {
            representationChanged (Document.class);
        }
    }

    
    private void loadTextRepresentation() {
        if ( getDO().isValid() ) { // because of remove modified document
            try {
                EditorCookie editor = getDO().getCookie(EditorCookie.class);
                editor.openDocument();
            } catch (IOException ex) {
                //??? ignore it just now
            }
        }
    }
}
