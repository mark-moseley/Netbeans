/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.j2seproject.J2SEProjectType;
import org.netbeans.modules.java.j2seproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlRenderer;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Support class for {@link JavaPlatform} manipulation in j2seproject customizer.
 * @author tzezula
 */
public class PlatformUiSupport {    
    
    
    private PlatformUiSupport() {
    }
    
    /**
     * Creates {@link ComboBoxModel} of J2SE platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to changes
     * @param activePlatform the active project's platform 
     * @return {@link ComboBoxModel}
     */
    public static ComboBoxModel createPlatformComboBoxModel (String activePlatform) {
        return new PlatformComboBoxModel (activePlatform);
    }
    
    
    /**
     * Creates a {@link ListCellRenderer} for rendering items of the {@link ComboBoxModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel} method.
     * @return {@link ListCellRenderer}
     */
    public static ListCellRenderer createPlatformListCellRenderer () {
        return new PlatformListCellRenderer ();
    }
       
    /**
     * Stores active platform, javac.source and javac.target into the project's metadata
     * @param props project's shared properties
     * @param helper to read/update project.xml
     * @param platformKey the PatformKey got from the platform model
     * @param sourceLevel source level
     */
    public static void storePlatform (EditableProperties props, UpdateHelper helper, Object platformKey, Object sourceLevelKey) {
        assert platformKey instanceof PlatformKey;        
        PlatformKey pk = (PlatformKey) platformKey;
        JavaPlatform platform = getPlatform(pk);                
        //null means active broken (unresolved) platform, no need to do anything
        if (platform != null) {
            SpecificationVersion jdk13 = new SpecificationVersion ("1.3");  //NOI18N
            String platformAntName = (String) platform.getProperties().get("platform.ant.name");    //NOI18N        
            assert platformAntName != null;
            props.put(J2SEProjectProperties.JAVA_PLATFORM, platformAntName);
            Element root = helper.getPrimaryConfigurationData(true);
            boolean defaultPlatform = pk.isDefaultPlatform();
            boolean changed = false;
            NodeList explicitPlatformNodes = root.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"explicit-platform");   //NOI18N
            if (defaultPlatform) {                
                if (explicitPlatformNodes.getLength()==1) {
                    root.removeChild(explicitPlatformNodes.item(0));
                    changed = true;
                }                
            }
            else {
                Element explicitPlatform;
                switch (explicitPlatformNodes.getLength()) {
                    case 0:
                        explicitPlatform = root.getOwnerDocument().createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); //NOI18N                    
                        NodeList sourceRootNodes = root.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");   //NOI18N
                        assert sourceRootNodes.getLength() == 1 : "Broken project.xml file"; //NOI18N
                        root.insertBefore(explicitPlatform, sourceRootNodes.item(0));
                        changed = true;
                        break;
                    case 1:
                        explicitPlatform = (Element)explicitPlatformNodes.item(0);
                        break;
                    default:
                        throw new AssertionError("Broken project.xml file");   //NOI18N
                }                
                String explicitSourceAttrValue = explicitPlatform.getAttribute("explicit-source-supported");    //NOI18N
                if (jdk13.compareTo(platform.getSpecification().getVersion())>=0 &&
                    !"false".equals(explicitSourceAttrValue)) {   //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","false"); //NOI18N
                    changed = true;
                }
                else if (jdk13.compareTo(platform.getSpecification().getVersion())<0 &&
                    !"true".equals(explicitSourceAttrValue)) {  //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","true"); //NOI18N
                    changed = true;
                }                                
            }
            
            SpecificationVersion sourceLevel;
            if (sourceLevelKey == null) {
                sourceLevel = platform.getSpecification().getVersion();
            }
            else {
                assert sourceLevelKey instanceof SourceLevelKey;
                sourceLevel = ((SourceLevelKey)sourceLevelKey).getSourceLevel();
            }
            String javacSource = sourceLevel.toString();
            String javacTarget = jdk13.compareTo(sourceLevel)>=0 ? "1.1" : javacSource;     //NOI18N
            if (!javacSource.equals(props.getProperty(J2SEProjectProperties.JAVAC_SOURCE))) {
                props.setProperty (J2SEProjectProperties.JAVAC_SOURCE, javacSource);
            }
            if (!javacTarget.equals(props.getProperty(J2SEProjectProperties.JAVAC_TARGET))) {
                props.setProperty (J2SEProjectProperties.JAVAC_TARGET, javacTarget);
            }
                        
            if (changed) {
                helper.putPrimaryConfigurationData(root, true);
            }
        }
    }
    
    
    /** 
     * Returns a {@link JavaPlatform} for an item obtained from the ComboBoxModel created by
     * the {@link PlatformUiSupport#createComboBoxModel} method
     * @param platformKey an item obtained from ComboBoxModel created by {@link PlatformUiSupport#createComboBoxModel}
     * @return JavaPlatform or null in case when platform is broken
     * @exception {@link IllegalArgumentException} is thrown in case when parameter in not an object created by
     * platform combobox model.
     */
    public static JavaPlatform getPlatform (Object platformKey) {
       if (platformKey instanceof PlatformKey) {
           return getPlatform ((PlatformKey)platformKey);
       }
       else {
           throw new IllegalArgumentException ();
       }
    }
    
    /**
     * Creates {@link ComboBoxModel} of source levels for active platform.
     * The model listens on the platform's {@link ComboBoxModel} and update its
     * state according to changes
     * @param platformComboBoxModel the platform's model used for listenning
     * @param initialValue initial source level value
     * @return {@link ComboBoxModel} of {@link SpecificationVersion}
     */
    public static ComboBoxModel createSourceLevelComboBoxModel (ComboBoxModel platformComboBoxModel, String initialValue) {
        return new SourceLevelComboBoxModel (platformComboBoxModel, initialValue);
    }
    
    
    public static ListCellRenderer createSourceLevelListCellRenderer () {
        return new SourceLevelListCellRenderer ();
    }
    
    
    private static JavaPlatform getPlatform (PlatformKey platformKey) {
        return platformKey.platform;
    }    
    
    
    /**
     * This class represents a  JavaPlatform in the {@link ListModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel}
     * method.
     */
    private static class PlatformKey implements Comparable {
        
        private String name;
        private JavaPlatform platform;
        
        /**
         * Creates a PlatformKey for a broken platform
         * @param name the ant name of the broken platform
         */
        public PlatformKey (String name) {
            assert name != null;
            this.name = name;
        }
        
        /**
         * Creates a PlatformKey for a platform
         * @param platform the {@link JavaPlatform}
         */
        public PlatformKey (JavaPlatform platform) {
            assert platform != null;
            this.platform = platform;
        }

        public int compareTo(Object o) {
            return this.getDisplayName().compareTo(((PlatformKey)o).getDisplayName());
        }
        
        public boolean equals (Object other) {
            if (other instanceof PlatformKey) {
                PlatformKey otherKey = (PlatformKey)other;
                return (this.platform == null ? otherKey.platform == null : this.platform.equals(otherKey.platform)) &&
                       otherKey.getDisplayName().equals (this.getDisplayName());
            }
            else {
                return false;
            }
        }
        
        public int hashCode () {
            return getDisplayName ().hashCode ();
        }
        
        public String toString () {
            return getDisplayName ();
        }
        
        public synchronized String getDisplayName () {
            if (this.name == null) {                
                this.name = this.platform.getDisplayName();
            }
            return this.name;
        }
        
        public boolean isDefaultPlatform () {
            if (this.platform == null) {
                return false;
            }
            return this.platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform());
        }
        
        public boolean isBroken () {
            return this.platform == null;
        }
        
    }    
    
    private static final class SourceLevelKey implements Comparable {
        
        final SpecificationVersion sourceLevel;
        final boolean broken;
        
        public SourceLevelKey (final SpecificationVersion sourceLevel) {
            this (sourceLevel, false);
        }
        
        public SourceLevelKey (final SpecificationVersion sourceLevel, final boolean broken) {
            assert sourceLevel != null : "Source level cannot be null";     //NOI18N
            this.sourceLevel = sourceLevel;
            this.broken = broken;
        }
        
        public SpecificationVersion getSourceLevel () {
            return this.sourceLevel;
        }
        
        public boolean isBroken () {
            return this.broken;
        }
        
        public int compareTo (final Object other) {
            assert other instanceof SourceLevelKey : "Illegal argument of SourceLevelKey.compareTo()";  //NOI18N
            SourceLevelKey otherKey = (SourceLevelKey) other;
            return this.sourceLevel.compareTo(otherKey.sourceLevel);
        }
        
        public /*@Override*/ boolean equals (final Object other) {
            return (other instanceof SourceLevelKey) &&
                   ((SourceLevelKey)other).sourceLevel.equals(this.sourceLevel);
        }
        
        public /*@Override*/ int hashCode () {
            return this.sourceLevel.hashCode();
        }
        
        public /*@Override*/ String toString () {
            StringBuffer buffer = new StringBuffer ();
            if (this.broken) {
                buffer.append("Broken: ");      //NOI18N
            }
            buffer.append(this.sourceLevel.toString());
            return buffer.toString();
        }
        
    }
    
    private static class PlatformComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        
        private JavaPlatformManager pm;
        private PlatformKey[] platformNamesCache;
        private String initialPlatform;
        private PlatformKey selectedPlatform;
        
        public PlatformComboBoxModel (String initialPlatform) {
            this.pm = JavaPlatformManager.getDefault();
            this.pm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pm));
            this.initialPlatform = initialPlatform;
        }
        
        public int getSize () {
            PlatformKey[] platformNames = getPlatformNames ();
            return platformNames.length;
        }
        
        public Object getElementAt (int index) {
            PlatformKey[] platformNames = getPlatformNames ();
            assert index >=0 && index< platformNames.length;
            return platformNames[index];
        }
        
        public Object getSelectedItem () {
            this.getPlatformNames(); //Force setting of selectedPlatform if it is not alredy done
            return this.selectedPlatform;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedPlatform = (PlatformKey) obj;
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.platformNamesCache = null;
                }
                this.fireContentsChanged(this, -1, -1);
            }
        }
        
        private synchronized PlatformKey[] getPlatformNames () {
            if (this.platformNamesCache == null) {
                JavaPlatform[] platforms = pm.getPlatforms (null, new Specification("j2se",null));    //NOI18N
                JavaPlatform defaultPlatform = pm.getDefaultPlatform ();
                Set/*<PlatformKey>*/ orderedNames = new TreeSet ();
                boolean activeFound = false;
                for (int i=0; i< platforms.length; i++) {
                    if (platforms[i].getInstallFolders().size()>0) {
                        PlatformKey pk = new PlatformKey(platforms[i]);
                        orderedNames.add (pk);
                        if (!activeFound && initialPlatform != null) {
                            String antName = (String) platforms[i].getProperties().get("platform.ant.name");    //NOI18N
                            if (initialPlatform.equals(antName)) {
                                if (this.selectedPlatform == null) {
                                    this.selectedPlatform = pk;
                                    initialPlatform = null;
                                }
                                activeFound = true;
                            }
                        }
                    }                    
                }
                if (!activeFound) {
                    if (initialPlatform == null) {
                        if (this.selectedPlatform == null || !orderedNames.contains(this.selectedPlatform)) {
                            this.selectedPlatform = new PlatformKey (JavaPlatformManager.getDefault().getDefaultPlatform());
                        }
                    }
                    else {
                        PlatformKey pk = new PlatformKey (this.initialPlatform);
                        orderedNames.add (pk);
                        if (this.selectedPlatform == null) {
                            this.selectedPlatform = pk;
                        }
                    }
                }
                this.platformNamesCache = (PlatformKey[]) orderedNames.toArray(new PlatformKey[orderedNames.size()]);
            }
            return this.platformNamesCache;                    
        }
        
    }
    
    private static class PlatformListCellRenderer implements ListCellRenderer {
        
        private ListCellRenderer delegate;
        
        public PlatformListCellRenderer () {
            this.delegate = HtmlRenderer.createRenderer ();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            if (value == null) {
                name = "";  //NOI18N
            }
            else {
                assert value instanceof PlatformKey : "Wrong model";  //NOI18N
                PlatformKey key = (PlatformKey) value;           
                if (key.isBroken()) {
                    name = "<html><font color=\"#A40000\">" +    //NOI18N
                        NbBundle.getMessage (PlatformUiSupport.class,"TXT_BrokenPlatformFmt", key.getDisplayName());
                }
                else {
                    name = key.getDisplayName();
                }
            }
            return this.delegate.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }        
    }
    
    private static class SourceLevelComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {
        
        private static final String VERSION_PREFIX = "1.";      //The version prefix
        private static final int INITIAL_VERSION_MINOR = 2;     //1.2
        
        private SpecificationVersion selectedSourceLevel;
        private SpecificationVersion originalSourceLevel;
        private SourceLevelKey[] sourceLevelCache;
        private final ComboBoxModel platformComboBoxModel;
        private PlatformKey activePlatform;
        
        public SourceLevelComboBoxModel (ComboBoxModel platformComboBoxModel, String initialValue) {            
            this.platformComboBoxModel = platformComboBoxModel;
            this.activePlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
            this.platformComboBoxModel.addListDataListener (this);
            if (initialValue != null && initialValue.length()>0) {
                try {
                    this.originalSourceLevel = this.selectedSourceLevel = new SpecificationVersion (initialValue);
                } catch (NumberFormatException nfe) {
                    //If the javac.source has invalid value, do not preselect and log it
                    ErrorManager.getDefault().log("Invalid javac.source: "+initialValue);
                }
            }            
        }
                
        public int getSize () {
            SourceLevelKey[] sLevels = getSourceLevels ();
            return sLevels.length;
        }
        
        public Object getElementAt (int index) {
            SourceLevelKey[] sLevels = getSourceLevels ();
            assert index >=0 && index< sLevels.length;
            return sLevels[index];
        }
        
        public Object getSelectedItem () {
            SourceLevelKey[] keys = getSourceLevels ();
            for (int i=0; i<keys.length; i++) {
                if (keys[i].getSourceLevel().equals(this.selectedSourceLevel)) {
                    return keys[i];
                }
            }
            return null;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedSourceLevel = (obj == null ? null : ((SourceLevelKey)obj).getSourceLevel());
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void intervalAdded(ListDataEvent e) {
        }

        public void intervalRemoved(ListDataEvent e) {
        }

        public void contentsChanged(ListDataEvent e) {
            PlatformKey selectedPlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
            JavaPlatform platform = getPlatform(selectedPlatform);
            if (platform != null) {
                SpecificationVersion version = platform.getSpecification().getVersion();
                if (this.selectedSourceLevel != null && this.selectedSourceLevel.compareTo(version)>0 &&
                    !shouldChangePlatform (selectedSourceLevel, version)) {
                    //Restore original
                   this.platformComboBoxModel.setSelectedItem(this.activePlatform);                            
                   return;
                }
                else {
                    this.originalSourceLevel = null;
                }
            }
            this.activePlatform = selectedPlatform;
            resetCache ();
        }
        
        private void resetCache () {            
            synchronized (this) {
                this.sourceLevelCache = null;                
            }
            this.fireContentsChanged(this, -1, -1);
        }
        
        private SourceLevelKey[] getSourceLevels () {
            if (this.sourceLevelCache == null) {
                PlatformKey selectedPlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
                JavaPlatform platform = getPlatform(selectedPlatform);                
                List/*<SpecificationVersion>*/ sLevels = new ArrayList ();
                //If platform == null broken platform, the source level range is unknown
                //The source level combo box should be empty and disabled
                boolean selSourceLevelValid = false;
                if (platform != null) {                    
                    SpecificationVersion version = platform.getSpecification().getVersion();                                        
                    int index = INITIAL_VERSION_MINOR;
                    SpecificationVersion template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));
                    boolean origSourceLevelValid = false;
                    
                    while (template.compareTo(version)<=0) {
                        if (template.equals(this.originalSourceLevel)) {
                            origSourceLevelValid = true;
                        }
                        if (template.equals(this.selectedSourceLevel)) {
                            selSourceLevelValid = true;
                        }
                        sLevels.add (new SourceLevelKey (template));
                        template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));
                    }
                    if (this.originalSourceLevel != null && !origSourceLevelValid) {
                        if (originalSourceLevel.equals(this.selectedSourceLevel)) {                            
                            selSourceLevelValid = true;
                        }
                        sLevels.add (new SourceLevelKey(this.originalSourceLevel,true));
                    }                                        
                }
                this.sourceLevelCache = (SourceLevelKey[]) sLevels.toArray(new SourceLevelKey[sLevels.size()]);
                if (!selSourceLevelValid) {
                    this.selectedSourceLevel = this.sourceLevelCache.length == 0 ? 
                        null : this.sourceLevelCache[this.sourceLevelCache.length-1].getSourceLevel();
                }
            }
            return this.sourceLevelCache;
        }
        
        private static boolean shouldChangePlatform (SpecificationVersion selectedSourceLevel, SpecificationVersion platformSourceLevel) {
            JButton changeOption = new JButton (NbBundle.getMessage(PlatformUiSupport.class, "CTL_ChangePlatform"));
            changeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformUiSupport.class, "AD_ChangePlatform"));
            String message = MessageFormat.format (NbBundle.getMessage(PlatformUiSupport.class,"TXT_ChangePlatform"),new Object[] {
                selectedSourceLevel.toString(),
                platformSourceLevel.toString(),
            });
            return DialogDisplayer.getDefault().notify(
                new NotifyDescriptor (message,
                        NbBundle.getMessage(PlatformUiSupport.class,"TXT_ChangePlatformTitle"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE,
                        new Object[] {
                            changeOption,
                            NotifyDescriptor.CANCEL_OPTION
                        },
                        changeOption)) == changeOption;
        }
    }
    
    private static class SourceLevelListCellRenderer implements ListCellRenderer {
        
        ListCellRenderer delegate;
        
        public SourceLevelListCellRenderer () {
            this.delegate = HtmlRenderer.createRenderer();
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String message;
            if (value == null) {
                message = "";   //NOI18N
            }
            else {
                assert value instanceof SourceLevelKey;
                SourceLevelKey key = (SourceLevelKey) value;            
                if (key.isBroken()) {                
                    message = "<html><font color=\"#A40000\">" +    //NOI18N
                        NbBundle.getMessage(PlatformUiSupport.class,"TXT_InvalidSourceLevel",key.getSourceLevel().toString());
                }
                else {
                    message = key.getSourceLevel().toString();
                }
            }
            return this.delegate.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
        }
    }
    
}
