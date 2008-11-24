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
package org.netbeans.modules.csl.navigation;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.util.Lookup;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
public final class ClassMemberNavigatorSourceFactory extends TaskFactory {

    private static ClassMemberNavigatorSourceFactory instance = null;
    private ClassMemberPanelUI ui;
    private static final ParserResultTask<ParserResult> EMPTY_TASK = new ParserResultTask<ParserResult>() {
        public @Override void cancel() {
            // no-op
        }

        public @Override void run(ParserResult result) {
            // no-op
        }

        public @Override int getPriority() {
            return Integer.MAX_VALUE;
        }

        public @Override Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }
    };
    
    public static synchronized ClassMemberNavigatorSourceFactory getInstance() {
        if (instance == null) {
            instance = new ClassMemberNavigatorSourceFactory();
        }
        return instance;
    }
    
    private ClassMemberNavigatorSourceFactory() {
        super(); // XXX: Phase.ELEMENTS_RESOLVED, Priority.LOW
    }

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        // System.out.println("CREATE TASK FOR " + file.getNameExt() );
        String mimeType = snapshot.getMimeType();
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        if (l != null) {
            if ( ui == null) {
                return Collections.singleton(EMPTY_TASK);
            } else {
                return Collections.singleton(ui.getTask());
            }
        } else {
            return null;
        }
    }

// XXX: parsingapi
//    public List<FileObject> getFileObjects() {
//        List<FileObject> result = new ArrayList<FileObject>();
//
//        // Filter uninteresting files from the lookup
//        LanguageRegistry registry = LanguageRegistry.getInstance();
//        for( FileObject fileObject : super.getFileObjects() ) {
//            if (!registry.isSupported(FileUtil.getMIMEType(fileObject))) {
//                continue;
//            }
//            result.add(fileObject);
//        }
//
//        if (result.size() == 1)
//            return result;
//
//        return Collections.emptyList();
//    }

    public synchronized void setLookup(Lookup l, ClassMemberPanelUI ui) {
        this.ui = ui;
    }

// XXX: parsingapi
//    @Override
//    protected void lookupContentChanged() {
//          // System.out.println("lookupContentChanged");
//          if ( ui != null ) {
//            ui.showWaitNode(); // Creating new task (file changed)
//          }
//    }

}
