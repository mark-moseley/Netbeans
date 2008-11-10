/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.maven.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.embedder.writer.WriterUtils;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * controller for maven2 settings in the options dialog.
 * @author Milos Kleint
 */
class MavenOptionController extends OptionsPanelController {
    public static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<settings xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">" + "</settings>";
    private SettingsPanel panel;
    private SettingsModel setts;
    private final List<PropertyChangeListener> listeners;
    /**
     * Creates a new instance of MavenOptionController
     */
    MavenOptionController() {
        listeners = new ArrayList<PropertyChangeListener>();
    }

    private SettingsModel createModel() {
        ModelSource source = null;
        File mavenFolder = MavenSettingsSingleton.getInstance().getM2UserDir();
        FileObject mavenFO = FileUtil.toFileObject(mavenFolder);
        if (mavenFO != null) {
            FileObject settingsFO = mavenFO.getFileObject("settings.xml");
            if (settingsFO != null) {
                source = Utilities.createModelSource(settingsFO, true);
            }
        }
        if (source == null) {
            File file = new File(mavenFolder, "settings.xml"); //NOI18N
            source = Utilities.createModelSourceForMissingFile(file, true, TEMPLATE, "text/xml"); //NOI18N
        }
        return SettingsModelFactory.getDefault().getModel(source);
    }
    
    public void update() {
        if (setts == null) {
            setts = createModel();
        }
        getPanel().setValues(setts.getSettings());
    }
    
    public void applyChanges() {
        if (setts == null) {
            setts = createModel();
        }
        setts.startTransaction();
        try {
            getPanel().applyValues(setts.getSettings());
        } finally {
            setts.endTransaction();
        }
        try {
            Utilities.saveChanges(setts);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void cancel() {
        setts = null;
    }
    
    public boolean isValid() {
        return getPanel().hasValidValues();
    }
    
    public boolean isChanged() {
        return getPanel().hasChangedValues();
    }
    
    public JComponent getComponent(Lookup lookup) {
        return getPanel();
    }

    void firePropChange(String property, Object oldVal, Object newVal) {
        ArrayList<PropertyChangeListener> lst;
        synchronized (listeners) {
            lst = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent evnt = new PropertyChangeEvent(this, property, oldVal, newVal);
        for (PropertyChangeListener prop : lst) {
            prop.propertyChange(evnt);
        }
    }
    
    private SettingsPanel getPanel() {
        if (panel == null) {
            panel = new SettingsPanel(this);
        }
        return panel;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.add(propertyChangeListener);
        }
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.remove(propertyChangeListener);
        }
    }
    
}
