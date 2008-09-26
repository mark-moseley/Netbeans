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

package org.netbeans.modules.websvc.wsitconf.design;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.wsitconf.api.DesignerListenerProvider;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Grebac
*/
public class MtomConfiguration  implements WSConfiguration{
  
    private Service service;
    private DataObject implementationFile;
    private Project project;
    
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
    private Binding binding;
    
    private ComponentListener cl;
    
    private boolean clAdded = false;
    
    private PropertyChangeListener configCreationListener = null;
    
    /** Creates a new instance of WSITWsConfiguration */

    public MtomConfiguration(final Service service, final FileObject implementationFile) {
        try {
            this.service = service;
            this.implementationFile = DataObject.find(implementationFile);
            this.project = FileOwnerQuery.getOwner(implementationFile);
            this.binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
            this.cl = new ComponentListener() {

                private void update() {
                    boolean enabled = TransportModelHelper.isMtomEnabled(binding);
                    for (PropertyChangeListener pcl : listeners) {
                        PropertyChangeEvent pce = new PropertyChangeEvent(MtomConfiguration.this, WSConfiguration.PROPERTY, null, enabled);
                        pcl.propertyChange(pce);
                    }
                }

                public void valueChanged(ComponentEvent evt) {
                    update();
                }

                public void childrenAdded(ComponentEvent evt) {
                    update();
                }

                public void childrenDeleted(ComponentEvent evt) {
                    update();
                }
            };
            if (binding != null) {
                addCListener(binding);
            } else {
                configCreationListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
                        addCListener(binding);
                        cl.valueChanged(null);
                    }
                };
                DesignerListenerProvider.registerListener(configCreationListener);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private synchronized void addCListener(Binding binding) {
        if ((!clAdded) && (binding != null)) {
            binding.getModel().addComponentListener(cl);
            clAdded = true;
        }
    }                
                
    public Component getComponent() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(MtomConfiguration.class, "DesignConfigPanel.mtomCB.text");
    }

    public Image getIcon() {
        return ImageUtilities.loadImage
                ("org/netbeans/modules/websvc/wsitconf/resources/designer-mtom.gif"); // NOI18N   
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MtomConfiguration.class, "DesignConfigPanel.mtomCB.text");
    }
  
    public boolean isSet() {
        if (binding != null) {
            return TransportModelHelper.isMtomEnabled(binding);
        }
        return false;
    }
    
    public void set() {
        switchIt(true);
    }

    public void unset() {
        switchIt(false);
    }

    private void switchIt(final boolean enable) {
        final ConfigRunnable r = new ConfigRunnable();        
        SwingUtilities.invokeLater(r);
        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, true, createdFiles);
                        if (binding == null) return;
                        if (!(TransportModelHelper.isMtomEnabled(binding) == enable)) {
                            TransportModelHelper.enableMtom(binding, enable);
                            WSITModelSupport.save(binding);
                        }
                    } finally {
                        r.stop();
                    }
                }
        });
    }
    
    public void registerListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void unregisterListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    protected void finalize() {
        if (binding != null) {
            binding.getModel().removeComponentListener(cl);
        }
    }

    public boolean isEnabled() {
        return true;
    }
}
