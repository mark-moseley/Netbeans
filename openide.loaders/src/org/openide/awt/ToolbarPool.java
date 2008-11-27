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

package org.openide.awt;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * This class keeps track of the current toolbars and their names.
 * @author David Peroutka, Libor Kramolis
 */
public final class ToolbarPool extends JComponent implements Accessible {
    /** Default ToolbarPool */
    private static ToolbarPool defaultPool;

    /** objects responsible for creation of the window */
    private Folder instance;

    /** DataFolder from which the pool was created */
    private DataFolder folder;

    /** Maps name to <code>Toolbar</code>s */
    private Map<String, Toolbar> toolbars;
    private ArrayList<String> toolbarNames;
    /** Maps name to <code>ToolbarPool.Configuration</code>s */
    private Map<String, ToolbarPool.Configuration> toolbarConfigs;

    /** Current name of selected configuration */
    private String name = ""; // NOI18N

    /** Center component */
    private Component center;

    /** Popup menu listener */
    private PopupListener listener;

    /** Accessible context */
    private AccessibleContext toolbarAccessibleContext;

    /** Name of default toolbar configuration. */
    public static final String DEFAULT_CONFIGURATION = "Standard"; // NOI18N
    
    private TPTaskListener taskListener;
    
    /** Preferred icon size. 2 sizes are supported now: 16 and 24. */
    private int preferredIconSize = 24;
    
    /**
     * Returns default toolbar pool.
     * @return default system pool
     */
    public static synchronized ToolbarPool getDefault () {
        if (defaultPool == null) {
            FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
            FileObject fo = null;
            try {
                fo = FileUtil.createFolder(root, "Toolbars"); // NOI18N
            } catch (IOException ex) {
                Logger.getLogger(ToolbarPool.class.getName()).log(Level.CONFIG, "Cannot create Toolbars folder.", ex);
            }
            if (fo == null)
                throw new IllegalStateException("No Toolbars/"); // NOI18N
            DataFolder folder = DataFolder.findFolder(fo);
            defaultPool = new ToolbarPool(folder);
            // we mustn't do this in constructor to prevent from
            // nevereding recursive calls to this method.
            defaultPool.instance.recreate();
        }
        return defaultPool;
    }

    static final long serialVersionUID =3420915387298484008L;


    /**
     * Creates a new <code>ToolbarPool</code>. Useful for modules that need they
     * own toolbars.
     *
     * @param df the data folder to read toolbar definitions and configurations from
     * @since 1.5
     */
    public ToolbarPool (DataFolder df) {
        folder = df;

        setLayout (new BorderLayout ());
        listener = new PopupListener();
        toolbars = new TreeMap<String, Toolbar>();
        toolbarNames = new ArrayList<String>(20);
        toolbarConfigs = new TreeMap<String, ToolbarPool.Configuration>();

        instance = new Folder (df);

        getAccessibleContext().setAccessibleName(instance.instanceName());
        getAccessibleContext().setAccessibleDescription(instance.instanceName());
    }
    
    /**
     * Gets preferred size of icons used by toolbar buttons. Default icon size
     * is 24x24. Icon size 16x16 is also supported.
     * @return preferred size of toolbar icons in pixels
     * @since 4.15
     */
    public int getPreferredIconSize () {
        return preferredIconSize;
    }
    
    /**
     * Sets preferred size of icons used by toolbar buttons.
     * @param preferredIconSize size of toolbar icons in pixels; currently one of 16 or 24
     * @throws IllegalArgumentException if an unsupported size is given
     * @since 4.15
     */
    public void setPreferredIconSize (int preferredIconSize) throws IllegalArgumentException {
        if ((preferredIconSize != 16) && (preferredIconSize != 24)) {
            throw new IllegalArgumentException("Unsupported argument value:" + preferredIconSize);  //NOI18N
        }
        this.preferredIconSize = preferredIconSize;
    }

    /** Allows to wait till the content of the pool is initialized. */
    public final void waitFinished () {
        instance.instanceFinished ();
    }

    /** Initialization of new values.
     * @param toolbars map (String, Toolbar) of toolbars
     * @param conf map (String, Configuration) of configs
     */
    void update (Map<String, Toolbar> toolbars, Map<String, ToolbarPool.Configuration> conf,
            ArrayList<String> toolbarNames) {
        this.toolbars = toolbars;
        this.toolbarNames = new ArrayList<String>( toolbarNames );
        this.toolbarConfigs = conf;

        if (!"".equals(name)) {
            setConfiguration (name);
        }
    }

    /** Updates the default configuration. */
    private synchronized void updateDefault () {
        Toolbar[] bars = getToolbars ();
        name = ""; // NOI18N
        
        if (bars.length == 1) {
            revalidate(bars[0]);
        } else {
            JPanel tp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            for (int i = 0; i < bars.length; i++) {
                tp.add(bars[i]);
            }
            revalidate(tp); 
        }
    }

    /** Activates a configuration.
     * @param c configuration
     */
    private synchronized void activate (Configuration c) {
        Component comp = c.activate ();
        name = c.getName();
        revalidate (comp);
    }

    /** Sets DnDListener to all Toolbars. 
     * @deprecated
     */
    @Deprecated
    public void setToolbarsListener (Toolbar.DnDListener l) {
        for (Toolbar t: toolbars.values()) {
            t.setDnDListener (l);
        }
    }

    /** Uses new component as a cental one. */
    private void revalidate (Component c) {
        if (c != center) {
            // exchange
            if (center != null) {
                remove (center);
                center.removeMouseListener (listener);
            }
            add (center = c, BorderLayout.CENTER);
            center.addMouseListener (listener);
        }
    }

    /**
     * Returns a <code>Toolbar</code> to which this pool maps the given name.
     * @param name a <code>String</code> that is to be a toolbar's name
     * @return a <code>Toolbar</code> to which this pool maps the name
     */
    public final Toolbar findToolbar (String name) {
        return toolbars.get (name);
    }

    /**
     * Getter for the name of current configuration.
     * @return the name of current configuration
     */
    public final String getConfiguration () {
        return name;
    }

    /**
     * Switch to toolbar configuration by specific config name
     * @param n toolbar configuration name
     */
    public final void setConfiguration (String n) {
        String old = name;
        
        // should be 'instance.waitFinished();' but some bug in isFinished ...
        if (!instance.isFinished()) {
            if (taskListener == null) {
                taskListener = new TPTaskListener();
                instance.addTaskListener(taskListener);
            }
            taskListener.setConfiguration(n);
            return;
        }
        if (taskListener != null) {
            instance.removeTaskListener(taskListener);
            taskListener = null;
        }

        Configuration config = null;
        if (n != null) {
            config = toolbarConfigs.get (n);
        }
        if (config != null) { // if configuration found
            activate (config);
        } else if (toolbarConfigs.isEmpty()) { // if no toolbar configuration
            updateDefault ();
        } else {
            // line below commented - bugfix, we need default configuration always when unknown config name is used:
            // if (center == null) { // bad config name (n) and no configuration activated yet
            config = toolbarConfigs.get (DEFAULT_CONFIGURATION);
            if (config == null) {
                config = toolbarConfigs.values().iterator().next();
            }
            activate (config);
        }
        
        firePropertyChange("configuration", old, name); //NOI18N
    }

    /**
     * @return the <code>DataFolder</code> from which the pool was created.
     */
    public final DataFolder getFolder() {
        return folder;
    }

    /**
     * Returns the toolbars contained in this pool.
     * @return the toolbars contained in this pool
     */
    public final synchronized Toolbar[] getToolbars() {
        Toolbar[] arr = new Toolbar[toolbarNames.size ()];
        int index = 0;
        for( String tn : toolbarNames ) {
            arr[index++] = findToolbar(tn);
        }
        return arr;
    }

    /**
     * @return the names of toolbar configurations contained in this pool
     */
    public final synchronized String[] getConfigurations () {
        ArrayList<String> list = new ArrayList<String>( toolbarConfigs.keySet() );
        Collections.sort( list );
        String[] arr = new String[ list.size() ];
        return list.toArray( arr );
    }

        /** Read accessible context
     * @return - accessible context
     */
    @Override
    public AccessibleContext getAccessibleContext () {
        if(toolbarAccessibleContext == null) {
            toolbarAccessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.TOOL_BAR;
                }
            };
        }
        return toolbarAccessibleContext;
    }
    
    /**
     * This class is used for delayed setting of configuration after instance
     * creation is finished. It may happen during IDE start that 
     * ToolbarPool.setConfiguration is called before instance is created.
     */
    private class TPTaskListener implements TaskListener {
        private String conf;
        
        TPTaskListener() {}
        
        public void taskFinished(Task task) {
            ToolbarPool.this.setConfiguration(conf);
            conf = null;
        }
        
        void setConfiguration(String conf) {
            // #23619: Don't reset already pending configuration to be set.
            if(this.conf == null) {
                this.conf = conf;
            }
        }
    }

    /**
     * This class can be used to produce a <code>ToolbarPool</code> instance
     * from the given <code>DataFolder</code>.
     */
    private class Folder extends FolderInstance {
        private WeakHashMap<DataFolder, InstanceCookie> foldersCache = 
                new WeakHashMap<DataFolder, InstanceCookie> (15);

        public Folder (DataFolder f) {
            super (f);
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        @Override
        public String instanceName () {
            return instanceClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        @Override
        public Class instanceClass () {
            return ToolbarPool.class;
        }

        /**
         * Accepts only cookies that can provide <code>Configuration</code>.
         * @param cookie the instance cookie to test
         * @return true if the cookie can provide <code>Configuration</code>
         */
        @Override
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
            throws java.io.IOException, ClassNotFoundException {

            Class cls = cookie.instanceClass();
            if (ToolbarPool.Configuration.class.isAssignableFrom (cls)) {
                return cookie;
            }
            if (Component.class.isAssignableFrom (cls)) {
                return cookie;
            }
            return null;
        }

        /**
         * Returns a <code>Toolbar.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Toolbar.Folder</code> for the specified folder
         */
        @Override
        protected InstanceCookie acceptFolder (DataFolder df) {
            InstanceCookie ic = foldersCache.get (df);
            if (ic == null) {
                ic = (FolderInstance) new Toolbar(df).waitFinished ();
                foldersCache.put (df, ic);
            }
            return ic;
        }

        /**
         * Updates the <code>ToolbarPool</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance (InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            final int length = cookies.length;

            final Map<String, Toolbar> toolbars = new TreeMap<String, Toolbar> ();
            final ArrayList<String> toolbarNames = new ArrayList<String>();
            final Map<String, Configuration> conf = new TreeMap<String, Configuration> ();

            for (int i = 0; i < length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();

                    if (obj instanceof Toolbar) {
                        Toolbar toolbar = (Toolbar) obj;

                        // should be done by ToolbarPanel in add method
                        toolbar.removeMouseListener(listener);
                        toolbar.addMouseListener(listener);
                        toolbars.put(toolbar.getName(), toolbar);
                        toolbarNames.add(toolbar.getName());
                        continue;
                    }
                    if (obj instanceof ToolbarPool.Configuration) {
                        ToolbarPool.Configuration config = (ToolbarPool.Configuration) obj;
                        java.lang.String name = config.getName();

                        if (name == null) {
                            name = cookies[i].instanceName();
                        }
                        conf.put(name, config);
                        continue;
                    }
                    if (obj instanceof Component) {
                        Component comp = (Component) obj;
                        String name = comp.getName();

                        if (name == null) {
                            name = cookies[i].instanceName();
                        }
                        conf.put(name, new ToolbarPool.ComponentConfiguration(comp));
                        continue;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ToolbarPool.class.getName()).log(Level.INFO, "Error while creating toolbars.", ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ToolbarPool.class.getName()).log(Level.INFO, "Error while creating toolbars.", ex);
                }
            }
            update (toolbars, conf, toolbarNames);

            return ToolbarPool.this;
        }

        /** Recreate the instance in AWT thread.
        */
        @Override
        protected Task postCreationTask (Runnable run) {
            return new AWTTask (run);
        }

    } // end of Folder


    /**
     * Class to showing popup menu
     */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {
	PopupListener() {}
        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        protected void showPopup (MouseEvent e) {
            Configuration conf = toolbarConfigs.get (name);
            if (conf != null) {
                JPopupMenu pop = conf.getContextMenu();
                pop.show (e.getComponent (), e.getX (), e.getY ());
            }
        }
    } // end of PopupListener


    /**
     * Abstract class for toolbar configuration
     */
    public static interface Configuration {
        /** Activates the configuration and returns right
        * component that can display the configuration.
        * @return representation component
        */
        public abstract Component activate ();

        /** Name of the configuration.
        * @return the name
        */
        public abstract String getName ();

        /** Popup menu that should be displayed when the users presses
        * right mouse button on the panel. This menu can contain
        * contains list of possible configurations, additional actions, etc.
        *
        * @return popup menu to be displayed
        */
        public abstract JPopupMenu getContextMenu ();
    }


    /** Implementation of configuration that reacts to one
    * component */
    private static final class ComponentConfiguration extends JPopupMenu
        implements Configuration, ActionListener {

        private Component comp;

        ComponentConfiguration() {
        }

        static final long serialVersionUID =-409474484612485719L;
        /** @param comp component that represents this configuration */
        public ComponentConfiguration (Component comp) {
            this.comp = comp;
        }

        /** Simply returns the representation component */
        public Component activate () {
            return comp;
        }

        /** @return name of the component
        */
        @Override
        public String getName () {
            return comp.getName ();
        }

        /** Updates items in popup menu and returns itself.
        */
        public JPopupMenu getContextMenu () {
            removeAll ();

            // generate list of available toolbar panels
            ButtonGroup bg = new ButtonGroup ();
            String current = ToolbarPool.getDefault ().getConfiguration ();
            for( String name : ToolbarPool.getDefault().getConfigurations() ) {
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem (name, (name.compareTo (current) == 0));
                mi.addActionListener (this);
                bg.add (mi);
                this.add (mi);
            }

            return this;
        }

        /** Reacts to action in popup menu. Switches the configuration.
        */
        public void actionPerformed (ActionEvent evt) {
            ToolbarPool.getDefault().setConfiguration (evt.getActionCommand ());
        }
    }
} // end of ToolbarPool

