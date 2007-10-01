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

package org.netbeans.modules.editor.impl;

import java.awt.Container;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib.URLMapper;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
public final class NbURLMapper extends URLMapper {

    private static final Logger LOG = Logger.getLogger(NbURLMapper.class.getName());
    
    public NbURLMapper() {
    }

    protected JTextComponent getTextComponent(URL url) {
        FileObject f = org.openide.filesystems.URLMapper.findFileObject(url);
        
        if (f != null) {
            DataObject d = null;
            
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, "Can't get DataObject for " + f, e); //NOI18N
            }
            
            if (d != null) {
                EditorCookie cookie = d.getLookup().lookup(EditorCookie.class);
                if (cookie != null) {
                    JEditorPane [] allJeps = cookie.getOpenedPanes();
                    if (allJeps != null) {
                        return allJeps[0];
                    }
                }
            }
        }
        
        return null;
    }

    protected URL getUrl(JTextComponent comp) {
        FileObject f = null;
        
        if (comp instanceof Lookup.Provider) {
            f = ((Lookup.Provider) comp).getLookup().lookup(FileObject.class);
        }
        
        if (f == null) {
            Container container = comp.getParent();
            while (container != null) {
                if (container instanceof Lookup.Provider) {
                    f = ((Lookup.Provider) container).getLookup().lookup(FileObject.class);
                    if (f != null) {
                        break;
                    }
                }
                
                container = container.getParent();
            }
        }
        
        if (f != null) {
            try {
                return f.getURL();
            } catch (FileStateInvalidException e) {
                LOG.log(Level.WARNING, "Can't get URL for " + f, e); //NOI18N
            }
        }
        
        return null;
    }

}
