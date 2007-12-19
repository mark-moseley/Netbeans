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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.local;

import javax.swing.JPanel;
import org.netbeans.modules.php.rt.providers.impl.AbstractProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectCustomizerComponent;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.ServersUtils;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author avk
 */
public class LocalServerProjectCustomizerPanel extends LocalServerProjectVisual 
        implements ProjectCustomizerComponent
{

    public LocalServerProjectCustomizerPanel(EditableProperties properties) {
        myProperties = properties;
    }
        
    public void read(EditableProperties properties) {
        myProperties = properties;
        String context = getProperties().getProperty(AbstractProjectConfigProvider.CONTEXT);
        getContextPath().setText(context);
        updateHost();
        contextUpdated();
    }

    public JPanel getPanel() {
        return this;
    }

    @Override
    protected void setDefaults() {
        // do nothing
    }

    protected void contextUpdated() {

        Host hostCandidate = getHost();
        if (hostCandidate != null && hostCandidate instanceof LocalHostImpl) {
            LocalHostImpl host = (LocalHostImpl) hostCandidate;

            updateDocumentPath(host);
            updateHttpPath(host);
        }
        if (isContentValid()) {
            store();
        }
    }

    private void store() {
        String context = getContextPath().getText();
        getProperties().setProperty(AbstractProjectConfigProvider.CONTEXT, context);
    }

    private boolean isContentValid() {
        return true;
    }

    /**
     * reloads host from WebServerRegistry if host id in properties was changed
     */
    private Host updateHost() {
        String hostId = getProperties().getProperty(WebServerProvider.HOST_ID);
        if (myHost != null && myHost.getId().equals(hostId)) {
            return myHost;
        }
        Host tmpHost = ServersUtils.findHostById(hostId);
        if (tmpHost != null){
            myHost = tmpHost;
        }
        return myHost;
    }

    private Host getHost() {
        return myHost;
    }

    private EditableProperties getProperties() {
        return myProperties;
    }

    private EditableProperties myProperties;

    private Host myHost;

}
