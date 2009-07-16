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

package org.netbeans.core;

import java.io.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jaroslav Tulach
 */
public class NbLoaderPoolDeserTest extends NbTestCase {
    private OldStyleLoader oldL;
    private FileObject fo;

    public NbLoaderPoolDeserTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
        oldL = DataLoader.getLoader(OldStyleLoader.class);
        NbLoaderPool.doAdd(oldL, null);
        
        fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "x.prop");
    }

    protected @Override void tearDown() throws Exception {
        NbLoaderPool.remove(oldL);
    }

    public void testOldLoaderThatChangesActionsBecomesModified () throws Exception {
        NbLoaderPool.waitFinished();
        
        DataObject first = DataObject.find(fo);
        assertTrue(first.getLoader().getClass().getName().contains("Default"));

        ExtensionList el = new ExtensionList();
        el.addExtension("prop");
        oldL.setExtensions(el);
        NbLoaderPool.waitFinished();
        
        
        DataObject snd = DataObject.find(fo);
        assertEquals("They are the same as nothing has been notified yet", first, snd);
        
        NbLoaderPool.installationFinished();
        NbLoaderPool.waitFinished();
        
        DataObject third = DataObject.find(fo);
        if (third == snd) {
            fail("They should not be the same: " + third);
        }
    }
    
    public static class OldStyleLoader extends UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super(MultiDataObject.class.getName());
        }
        
        protected MultiDataObject createMultiObject(FileObject fo) throws IOException {
            return new MultiDataObject(fo, this);
        }

        @SuppressWarnings("deprecation")
        protected @Override SystemAction[] defaultActions () {
            defaultActionsCalled = true;
            SystemAction[] retValue;
            
            retValue = super.defaultActions();
            return retValue;
        }
        
        
    }
}
