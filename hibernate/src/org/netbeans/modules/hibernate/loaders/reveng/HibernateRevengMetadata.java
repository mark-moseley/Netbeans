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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.reveng;

import org.netbeans.modules.hibernate.loaders.mapping.*;
import org.netbeans.modules.hibernate.loaders.cfg.*;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.hibernate.reveng.model.HibernateReverseEngineering;
import org.openide.filesystems.FileObject;

/**
 *
 * @author gowri
 */
public class HibernateRevengMetadata {
    private static final HibernateRevengMetadata DEFAULT = new HibernateRevengMetadata();
    private Map<FileObject,HibernateReverseEngineering> ddReveng;
    
    private HibernateRevengMetadata() {
        ddReveng = new WeakHashMap<FileObject,HibernateReverseEngineering>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static HibernateRevengMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in hibernate-reverse-engineering-3.0.dtd
     * 
     * @param fo FileObject represnting Hibernate Reverse Engineering file
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public HibernateReverseEngineering getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        HibernateReverseEngineering revEngineering = null;
        synchronized (ddReveng) {
            revEngineering = (HibernateReverseEngineering) ddReveng.get(fo);
            if (revEngineering == null) {
                revEngineering = HibernateReverseEngineering.createGraph(fo.getInputStream());
                ddReveng.put(fo, revEngineering);
            }
        }
        return revEngineering;
    }

}
