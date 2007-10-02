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
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.netbeans.api.retouche.source.SourceTaskFactoryManager;
import org.netbeans.editor.Settings;
import org.netbeans.modules.retouche.source.ActivatedDocumentListener;
import org.netbeans.modules.retouche.source.usages.ClassIndexManager;
import org.netbeans.modules.retouche.source.usages.NBLockFactory;
import org.netbeans.modules.retouche.source.usages.RepositoryUpdater;
import org.netbeans.modules.retouche.source.util.LowMemoryNotifierMBean;
import org.netbeans.modules.retouche.source.util.LowMemoryNotifierMBeanImpl;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;


public class GsfModuleInstaller extends ModuleInstall {
    private static final boolean ENABLE_MBEANS =
        Boolean.getBoolean("org.netbeans.modules.gsf.enableMBeans"); //NOI18N

    @Override
    public void restored() {
        // add editor support for our registered editor types
        Settings.addInitializer(new GsfEditorSettings());
        Settings.reset();

        NBLockFactory.clearLocks();
        SourceTaskFactoryManager.register();

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() {
                    RepositoryUpdater.getDefault();
                    ActivatedDocumentListener.register();
                }
            });

        if (ENABLE_MBEANS) {
            registerMBeans();
        }
    }
    
    public @Override boolean closing () {
        final boolean ret = super.closing();
        RepositoryUpdater.getDefault().close();
        try {
            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                 public Void run() throws IOException {
                     ClassIndexManager.getDefault().close();
                     return null;
                 }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        };            
        if (ENABLE_MBEANS) {
            unregisterMBeans();
        }
        return ret;
    }
    
    private static void registerMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.registerMBean (new LowMemoryNotifierMBeanImpl(), new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            //mgs.registerMBean( LuceneIndexMBeanImpl.getDefault(), new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (NotCompliantMBeanException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MalformedObjectNameException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (InstanceAlreadyExistsException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MBeanRegistrationException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }
    
    private static void unregisterMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.unregisterMBean (new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            //mgs.unregisterMBean (new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (MalformedObjectNameException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (InstanceNotFoundException e) {
            ErrorManager.getDefault ().notify (e);
        }
        catch (MBeanRegistrationException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }
}
