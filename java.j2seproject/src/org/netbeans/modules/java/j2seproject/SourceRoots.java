/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import org.openide.util.Mutex;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;

/**
 * This class represents a project source roots. It is used to obtain roots as Ant properties, FileObject's
 * or URLs.
 * @author Tomas Zezula
 */
public final class SourceRoots {

    public static final String PROP_ROOT_PROPERTIES = "rootProperties";    //NOI18N
    public static final String PROP_ROOTS = "roots";   //NOI18N

    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final String elementName;
    private final String newRootNameTemplate;
    private List sourceRootProperties;
    private List sourceRootNames;
    private List sourceRoots;
    private List sourceRootURLs;
    private final PropertyChangeSupport support;
    private final ProjectMetadataListener listener;
    private int rootIndex = 2;

    /**
     * Creates new SourceRoots
     * @param helper
     * @param evaluator
     * @param elementName the name of XML element under which are declared the roots
     */
    SourceRoots (UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, String elementName, String newRootNameTemplate) {
        assert helper != null && evaluator != null && refHelper != null && elementName != null && newRootNameTemplate != null;
        this.helper = helper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.elementName = elementName;
        this.newRootNameTemplate = newRootNameTemplate;
        this.support = new PropertyChangeSupport(this);
        this.listener = new ProjectMetadataListener();
        this.evaluator.addPropertyChangeListener (WeakListeners.propertyChange(this.listener,this.evaluator));
        this.helper.getAntProjectHelper().addAntProjectListener ((AntProjectListener)WeakListeners.create(AntProjectListener.class, this.listener,this.helper));
    }


    /**
     * Returns the display names of soruce roots
     * The returned array has the same length as an array returned by the getRootProperties.
     * It may contain empty strings but not null.
     * @return an array of String
     */
    public   String[] getRootNames () {
        return (String[]) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootNames == null) {
                        readProjectMetadata();
                    }
                }
                return sourceRootNames.toArray (new String[sourceRootNames.size()]);
            }
        });
    }

    /**
     * Returns names of Ant properties in the project.properties file holding the source roots.
     * @return an array of String
     */
    public String[] getRootProperties () {
        return (String[]) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootProperties == null) {
                        readProjectMetadata();
                    }
                }
                return sourceRootProperties.toArray (new String[sourceRootProperties.size()]);
            }
        });
    }

    /**
     * Returns the source roots
     * @return an array of FileObject
     */
    public FileObject[] getRoots () {
        return (FileObject[]) ProjectManager.mutex().readAccess(new Mutex.Action () {
                public Object run () {
                    synchronized (this) {
                        //Local caching
                        if (sourceRoots == null) {
                            String[] srcProps = getRootProperties();
                            List result = new ArrayList();
                            for (int i = 0; i<srcProps.length; i++) {
                                String prop = evaluator.getProperty(srcProps[i]);
                                if (prop != null) {
                                    FileObject f = helper.getAntProjectHelper().resolveFileObject(prop);
                                    if (f == null) {
                                        continue;
                                    }
                                    if (FileUtil.isArchiveFile(f)) {
                                        f = FileUtil.getArchiveRoot(f);
                                    }
                                    result.add(f);
                                }
                            }
                            sourceRoots = Collections.unmodifiableList(result);
                        }
                    }
                    return sourceRoots.toArray(new FileObject[sourceRoots.size()]);
                }
        });                
    }

    /**
     * Returns the source roots as URLs.
     * @return an array of URL
     */
    public URL[] getRootURLs() {
        return (URL[]) ProjectManager.mutex().readAccess(new Mutex.Action () {
            public Object run () {
                synchronized (this) {
                    //Local caching
                    if (sourceRootURLs == null) {
                        String[] srcProps = getRootProperties();
                        List result = new ArrayList();
                        for (int i = 0; i<srcProps.length; i++) {
                            String prop = evaluator.getProperty(srcProps[i]);
                            if (prop != null) {
                                File f = helper.getAntProjectHelper().resolveFile(prop);
                                try {
                                    URL url = f.toURI().toURL();
                                    if (FileUtil.isArchiveFile(url)) {
                                        url = FileUtil.getArchiveRoot(url);
                                    } else if (!f.exists()) {
                                        url = new URL(url.toExternalForm() + "/"); // NOI18N
                                    }
                                    result.add(url);
                                } catch (MalformedURLException e) {
                                    ErrorManager.getDefault().notify(e);
                                }
                            }
                        }
                        sourceRootURLs = Collections.unmodifiableList(result);
                    }
                }
                return sourceRootURLs.toArray(new URL[sourceRootURLs.size()]);
            }
        });                
    }

    /**
     * Adds PropertyChangeListener
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }


    /**
     * Replaces the current roots by the new ones
     * @param roots the URLs of new roots
     * @param labels the names of roots
     */
    public void putRoots (final URL[] roots, final String[] labels) {
        ProjectManager.mutex().writeAccess(
                new Mutex.Action () {
                    public Object run() {
                        String[] originalProps = getRootProperties();
                        URL[] originalRoots = getRootURLs();
                        Map oldRoots2props = new HashMap ();
                        for (int i=0; i<originalProps.length;i++) {
                            oldRoots2props.put (originalRoots[i],originalProps[i]);
                        }
                        Map newRoots2lab = new HashMap();
                        for (int i=0; i<roots.length;i++) {
                            newRoots2lab.put (roots[i],labels[i]);
                        }
                        Element cfgEl = helper.getPrimaryConfigurationData(true);
                        NodeList nl = cfgEl.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
                        assert nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
                        Element ownerElement = (Element) nl.item(0);
                        //Remove all old roots
                        NodeList rootsNodes = ownerElement.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
                        while (rootsNodes.getLength()>0) {
                            Element root = (Element) rootsNodes.item(0);
                            ownerElement.removeChild(root);
                        }
                        //Remove all unused root properties
                        List newRoots = Arrays.asList(roots);
                        Map propsToRemove = new HashMap (oldRoots2props);
                        propsToRemove.keySet().removeAll(newRoots);
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        for (Iterator it = propsToRemove.values().iterator(); it.hasNext();) {
                            String propName = (String) it.next ();
                            props.remove(propName);
                        }
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                        //Add the new roots
                        Document doc = ownerElement.getOwnerDocument();
                        oldRoots2props.keySet().retainAll(newRoots);
                        for (Iterator it = newRoots.iterator(); it.hasNext();) {
                            URL newRoot = (URL) it.next ();
                            String rootName = (String) oldRoots2props.get (newRoot);
                            if (rootName == null) {
                                //Root is new generate property for it
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                rootName = MessageFormat.format(newRootNameTemplate, new Object[]{new Integer(rootIndex)});
                                while (props.containsKey(rootName)) {
                                    rootIndex++;
                                    rootName = MessageFormat.format(newRootNameTemplate, new Object[]{new Integer(rootIndex)});
                                }
                                File f = FileUtil.normalizeFile(new File(URI.create(newRoot.toExternalForm())));
                                File projDir = FileUtil.toFile(helper.getAntProjectHelper().getProjectDirectory());
                                String path = f.getAbsolutePath();
                                String prjPath = projDir.getAbsolutePath()+File.separatorChar;
                                if (path.startsWith(prjPath)) {
                                    path = path.substring(prjPath.length());
                                }
                                else {
                                    path = refHelper.createForeignFileReference(f, JavaProjectConstants.SOURCES_TYPE_JAVA);
                                }
                                props.put(rootName,path);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                            }
                            Element newRootNode = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); //NOI18N
                            newRootNode.setAttribute("id",rootName);    //NOI18N
                            String label = (String) newRoots2lab.get (newRoot);
                            if (label != null && label.length()>0) {
                                newRootNode.setAttribute("name",label); //NOI18N
                            }
                            ownerElement.appendChild (newRootNode);
                        }
                        helper.putPrimaryConfigurationData(cfgEl,true);
                        return null;
                    }
                }
        );
    }

    private void resetCache (boolean isXMLChange, String propName) {
        boolean fire = false;
        synchronized (this) {
            //In case of change reset local cache
            if (isXMLChange) {
                this.sourceRootProperties = null;
                this.sourceRootNames = null;
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            } else if (propName == null || (sourceRootProperties != null && sourceRootProperties.contains(propName))) {
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            }
        }
        if (fire) {
            if (isXMLChange) {
                this.support.firePropertyChange (PROP_ROOT_PROPERTIES,null,null);
            }
            this.support.firePropertyChange (PROP_ROOTS,null,null);
        }
    }

    private void readProjectMetadata () {
        Element cfgEl = helper.getPrimaryConfigurationData(true);
        NodeList nl = cfgEl.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
        assert nl.getLength() == 0 || nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
        List rootProps = new ArrayList ();
        List rootNames = new ArrayList ();
        // It can be 0 in the case when the project is created by J2SEProjectGenerator and not yet customized
        if (nl.getLength()==1) {
            NodeList roots = ((Element)nl.item(0)).getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
            for (int i=0; i<roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String value = root.getAttribute("id");  //NOI18N
                assert value.length() > 0 : "Illegal project.xml";
                rootProps.add(value);
                value = root.getAttribute("name");  //NOI18N
                rootNames.add (value);
            }
        }
        this.sourceRootProperties = Collections.unmodifiableList(rootProps);
        this.sourceRootNames = Collections.unmodifiableList(rootNames);
    }

    private class ProjectMetadataListener implements PropertyChangeListener,AntProjectListener {

        public void propertyChange(PropertyChangeEvent evt) {
            resetCache (false,evt.getPropertyName());
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            resetCache (true,null);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            //Handled by propertyChange
        }
    }

}
