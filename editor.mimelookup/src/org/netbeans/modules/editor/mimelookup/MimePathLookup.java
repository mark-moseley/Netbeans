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

package org.netbeans.modules.editor.mimelookup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author vita
 */
@SuppressWarnings("deprecation")
public final class MimePathLookup extends ProxyLookup implements LookupListener {
    
    private static final Logger LOG = Logger.getLogger(MimePathLookup.class.getName());
    
    private MimePath mimePath;
    private Lookup.Result<MimeDataProvider> dataProviders;
    private Lookup.Result<MimeLookupInitializer> mimeInitializers; // This is supported for backwards compatibility only.
    
    /** Creates a new instance of MimePathLookup */
    public MimePathLookup(MimePath mimePath) {
        super();
        
        if (mimePath == null) {
            throw new NullPointerException("Mime path can't be null."); //NOI18N
        }
        
        this.mimePath = mimePath;

        dataProviders = Lookup.getDefault().lookup(new Lookup.Template<MimeDataProvider>(MimeDataProvider.class));
        dataProviders.addLookupListener(WeakListeners.create(LookupListener.class, this, dataProviders));

        mimeInitializers = Lookup.getDefault().lookup(new Lookup.Template<MimeLookupInitializer>(MimeLookupInitializer.class));
        mimeInitializers.addLookupListener(WeakListeners.create(LookupListener.class, this, mimeInitializers));
        
        rebuild();
    }

    public MimePath getMimePath() {
        return mimePath;
    }
    
    private void rebuild() {
        ArrayList<Lookup> lookups = new ArrayList<Lookup>();

        // Add lookups from MimeDataProviders
        for (MimeDataProvider provider : dataProviders.allInstances()) {
            Lookup mimePathLookup = provider.getLookup(mimePath);
            if (mimePathLookup != null) {
                lookups.add(mimePathLookup);
            }
        }

        // XXX: This hack here is to make GSF and Schliemann frameworks work.
        // Basically we should somehow enforce the composition of lookups 
        // for MimeDataProviders too. But some providers such as the one from
        // editor/mimelookup/impl do the composition in their own way. So we
        // will probably have to extend the SPI somehow to accomodate both simple
        // providers and the composing ones.
        // See also http://www.netbeans.org/issues/show_bug.cgi?id=118099

        // Add lookups from deprecated MimeLookupInitializers
        List<String> paths;
        try {
            Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> ret = (List<String>) m.invoke(mimePath, null, null);
            paths = ret;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
            paths = Collections.singletonList(mimePath.getPath());
        }

        for(String path : paths) {
            MimePath mp = MimePath.parse(path);
            Collection<? extends MimeLookupInitializer> initializers = mimeInitializers.allInstances();

            for(int i = 0; i < mp.size(); i++) {
                Collection<MimeLookupInitializer> children = new ArrayList<MimeLookupInitializer>(initializers.size());

                for(MimeLookupInitializer mli : initializers) {
                    children.addAll(mli.child(mp.getMimeType(i)).allInstances());
                }

                initializers = children;
            }

            for(MimeLookupInitializer mli : initializers) {
                Lookup mimePathLookup = mli.lookup();
                if (mimePathLookup != null) {
                    lookups.add(mimePathLookup);
                }
            }
        }
        
        setLookups(lookups.toArray(new Lookup[lookups.size()]));
    }
    
    //-------------------------------------------------------------
    // LookupListener implementation
    //-------------------------------------------------------------

    public void resultChanged(LookupEvent ev) {
        rebuild();
    }
    
}
