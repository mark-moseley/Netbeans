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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.openide.modules.SpecificationVersion;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * The first panel of the custom wizard used to register new server instance.
 * User is required to enter the local server's installation directory at this
 * phase.
 *
 * @author Kirill Sorokin
 */
public class ServerLocationVisual extends JPanel {

    private transient WLInstantiatingIterator instantiatingIterator;
    public ServerLocationVisual(WLInstantiatingIterator instantiatingIterator) {

        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;

        // register the supplied listener
        //addChangeListener(listener);

        // set the panel's name
         setName(NbBundle.getMessage(ServerPropertiesPanel.class,
                "SERVER_LOCATION_STEP"));        // NOI18N

        // init the GUI
        init();
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);

        // test if IDE is run on correct JDK version
        if (!runningOnCorrectJdk()) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "WARN_INVALID_JDK");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
        }

        // check for the validity of the entered installation directory
        // if it's invalid, return false
        String location = this.getInstallLocation();

        if (location.trim().length() < 1) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_EMPTY_SERVER_ROOT");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }
        
        File serverRoot = new File(location);

        if (!WLPluginProperties.isSupportedVersion(serverRoot)) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_SERVER_VERSION");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }

        if (!WLPluginProperties.isGoodServerLocation(serverRoot)) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_SERVER_ROOT");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }

        if (!WLPluginProperties.domainListExists(serverRoot)) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_SERVER_ROOT") +   // NOI18N
                         " " +    // NOI18N
                         NbBundle.getMessage(ServerLocationVisual.class, "DOMAIN_LIST_NOT_FOUND",    // NOI18N
                            serverRoot.getPath() + File.separator + WLPluginProperties.DOMAIN_LIST
                         );
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }


        WLPluginProperties.getInstance().setInstallLocation(location);
        WLPluginProperties.getInstance().saveProperties();
        // set the server root in the parent instantiating iterator
        instantiatingIterator.setServerRoot(location);

        // everything seems ok
        return true;
    }

    private static final String J2SE_PLATFORM_VERSION_15 = "1.5"; // NOI18N

    private boolean runningOnCorrectJdk() {
        SpecificationVersion defPlatVersion = JavaPlatformManager.getDefault()
                .getDefaultPlatform().getSpecification().getVersion();
        // test just JDK 1.5 for now, because WL 9.x and 10 throws marshalling
        // exception when running on JDK 6.
        return J2SE_PLATFORM_VERSION_15.equals(defPlatVersion.toString());
    }

    ////////////////////////////////////////////////////////////////////////////
    // JPanel section
    ////////////////////////////////////////////////////////////////////////////
    private JButton locationBrowseButton;
    private JLabel locationLabel;
    private JTextField locationField;
    private JPanel formattingPanel;

    /**
     * Inits the GUI components
     */
    private void init() {
        // we use the GridBagLayout so we need the GridBagConstraints to
        // properly place the components
        GridBagConstraints gridBagConstraints;

        // initialize the components
        locationLabel = new JLabel();
        locationField = new JTextField();
        locationBrowseButton = new JButton();
        formattingPanel = new JPanel();

        // set the desired layout
        setLayout(new GridBagLayout());

        // add server installation directory field label
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, NbBundle.getMessage(ServerLocationVisual.class, "LBL_SERVER_LOCATION")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        locationLabel.setLabelFor(locationField);
        add(locationLabel, gridBagConstraints);

        // add server installation directory field
        locationField.setColumns(10);
        locationField.addKeyListener(new LocationKeyListener());
        String loc = WLPluginProperties.getInstance().getInstallLocation();
        if (loc != null) { // NOI18N
            locationField.setText(loc);
        }
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        locationField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerLocationVisual.class, "ACSD_ServerLocationPanel_locationField")); // NOI18N
        add(locationField, gridBagConstraints);

        // add server installation directory field browse button
        org.openide.awt.Mnemonics.setLocalizedText(locationBrowseButton, NbBundle.getMessage(ServerLocationVisual.class, "LBL_BROWSE_BUTTON"));  // NOI18N
        locationBrowseButton.addActionListener(new BrowseActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        locationBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerLocationVisual.class, "ACSD_ServerLocationPanel_locationBrowseButton")); // NOI18N
        add(locationBrowseButton, gridBagConstraints);

        // add the empty panel, that will take up all the remaining space
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(formattingPanel, gridBagConstraints);
    }

     public String getInstallLocation() {
        return locationField.getText();
    }

    /**
     * An instance of the fileschooser that is used for locating the server
     * installation directory
     */
    private JFileChooser fileChooser;

    /**
     * Shows the filechooser set to currently selected directory or to the
     * default system root if the directory is invalid
     */
    private void showFileChooser() {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new DirectoryFileFilter());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // set the current directory
        File currentLocation = new File(locationField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }

        // wait for the user to choose the directory and if he clicked the OK
        // button store the selected directory in the server location field
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            locationField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registrered listeners vector
     */
    private Vector listeners = new Vector();

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Fires a change event originating from this panel
     */
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        fireChangeEvent(event);
    }

    /**
     * Fires a custom change event
     *
     * @param event the event
     */
    private void fireChangeEvent(ChangeEvent event) {
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }

        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Simple key listener that delegates the event to its parent's listeners
     *
     * @author Kirill Sorokin
     */
    private class LocationKeyListener extends KeyAdapter {
        /**
         * This method is called when a user presses a key on the keyboard
         */
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }

        /**
         * This method is called when a user releases a key on the keyboard
         */
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }

    /**
     * Simple listener that reacts on the user's clicking the Browse button
     *
     * @author Kirill Sorokin
     */
    private class BrowseActionListener implements ActionListener {
        /**
         * this methos is called when a user clicks Browse and show the file
         * chooser dialog in response
         */
        public void actionPerformed(ActionEvent event) {
            showFileChooser();
        }
    }

    /**
     * An extension of the FileFilter class that is setup to accept only
     * directories.
     *
     * @author Kirill Sorokin
     */
    private static class DirectoryFileFilter extends FileFilter {
        /**
         * This method is called when it is needed to decide whether a chosen
         * file meets the filter's requirements
         *
         * @return true if the file meets the requirements, false otherwise
         */
        public boolean accept(File file) {
            // if the file exists and it's a directory - accept it
            if (file.exists() && file.isDirectory()) {
                return true;
            }

            // in all other cases - refuse
            return false;
        }

        /**
         * Returns the description of file group described by this filter
         *
         * @return group name
         */
        public String getDescription() {
            return NbBundle.getMessage(ServerLocationVisual.class, "DIRECTORIES_FILTER_NAME"); // NOI18N
        }
    }
}
