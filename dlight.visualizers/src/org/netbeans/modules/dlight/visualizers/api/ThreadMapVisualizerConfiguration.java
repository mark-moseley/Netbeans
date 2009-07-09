/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.visualizers.api;

import java.util.List;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.ThreadMapMetadata;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadTableMetrics;
import org.netbeans.modules.dlight.visualizers.api.impl.ThreadMapVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;

/**
 *
 * @author Alexander Simon
 */
public class ThreadMapVisualizerConfiguration  implements VisualizerConfiguration {
    private ThreadMapMetadata threadMapMetadata;
    private DataTableMetadata threadTable;

    static{
        ThreadMapVisualizerConfigurationAccessor.setDefault(new ThreadMapVisualizerConfigurationAccessorImpl());
    }

    public ThreadMapVisualizerConfiguration(ThreadMapMetadata threadMapMetadata){
        this.threadMapMetadata = threadMapMetadata;
        List<Column> list = ThreadTableMetrics.getThredMapColumn();
        threadTable = new DataTableMetadata("threadmap", list, null); //NOI18N
    }

    public DataModelScheme getSupportedDataScheme() {
        return DataModelSchemeProvider.getInstance().getScheme("model:threadmap"); //NOI18N
    }

    public DataTableMetadata getMetadata() {
        return threadTable;
    }

    public String getID() {
        return VisualizerConfigurationIDsProvider.THREAD_MAP_VISUALIZER;
    }

    public ThreadMapMetadata getThreadMapMetadata() {
        return threadMapMetadata;
    }

    private static final class ThreadMapVisualizerConfigurationAccessorImpl extends ThreadMapVisualizerConfigurationAccessor {

        @Override
        public List<Column> getTableColumns(ThreadMapVisualizerConfiguration configuration) {
            return configuration.getMetadata().getColumns();
        }

        @Override
        public ThreadMapMetadata getThreadMapMetadata(ThreadMapVisualizerConfiguration configuration) {
            return configuration.getThreadMapMetadata();
        }
    }
}
