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
package org.netbeans.modules.timers;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.timers.TimesCollectorPeer.Description;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach
 */
public class HandlerTest extends NbTestCase {

    public HandlerTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        Install.findObject(Install.class, true).restored();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void tearDown() throws Exception {
    for (FileSystem fs : Repository.getDefault().toArray()) {
            Repository.getDefault().removeFileSystem(fs);
        }

        
        List<Reference> gc = new ArrayList<Reference>();
        
        for (Object key : TimesCollectorPeer.getDefault().getFiles()) {
            gc.add(new WeakReference<Object>(key));
        }
        
        for (Reference<?> reference : gc) {
            assertGC("GC it", reference);
        }

    }
    
    
    
    public void testLoggingMessageWithBundle() throws Exception {
        FileObject dir  = TimesCollectorPeerTest.makeScratchDir(this);
        
        Logger LOG = Logger.getLogger("TIMER.instance.of.my.object");
        LogRecord rec = new LogRecord(Level.FINE, "LOG_Project"); // NOI18N
        rec.setParameters(new Object[] { dir, dir });
        rec.setResourceBundle(ResourceBundle.getBundle(HandlerTest.class.getName()));
        LOG.log(rec);

        Collection<Object> files = TimesCollectorPeer.getDefault().getFiles();
        assertEquals("One object " + files, 1, files.size());
        
        Description descr = TimesCollectorPeer.getDefault().getDescription(files.iterator().next(), "LOG_Project");
        assertNotNull(descr);
        
        if (descr.getMessage().indexOf("My Project") == -1) {
            fail("Localized msg should contain 'My Project': " + descr.getMessage());
        }
    }
    
    public void testLoggingMessageWithBundleAndArg() throws Exception {
        FileObject dir  = TimesCollectorPeerTest.makeScratchDir(this);
        
        Logger LOG = Logger.getLogger("TIMER.instance.of.my.object");
        LogRecord rec = new LogRecord(Level.FINE, "LOG_ProjectWithArg"); // NOI18N
        rec.setParameters(new Object[] { dir, dir, "Lovely" });
        rec.setResourceBundle(ResourceBundle.getBundle(HandlerTest.class.getName()));
        LOG.log(rec);

        Collection<Object> files = TimesCollectorPeer.getDefault().getFiles();
        assertEquals("One object " + files, 1, files.size());
        
        Description descr = TimesCollectorPeer.getDefault().getDescription(files.iterator().next(), "LOG_ProjectWithArg");
        assertNotNull(descr);
        
        if (descr.getMessage().indexOf("My Lovely Project") == -1) {
            fail("Localized msg should contain 'My Lovely Project': " + descr.getMessage());
        }
    }
    
}
