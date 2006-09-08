/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * NodeFactory.java
 *
 * Created on 27 April 2006, 16:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.configurations.ProjectConfiguration;
import org.openide.util.Utilities;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.CloneConfigurationPanel;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.mobility.project.ui.customizer.VisualConfigSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Lukas Waldmann
 */

final class NodeFactory
{
    private static class NodeKeys extends Children.Keys
    {
        final HashMap<String,Node> nodeMap=new HashMap<String,Node>();
        
        NodeKeys(final Node[] ns)
        {
            add(ns);
        }
        
        public boolean add(Node[] ns)
        {
            for ( Node n : ns)
            {
                nodeMap.put(n.getName(),n);
            }
            this.setKeys(nodeMap.keySet());
            return true;
        }
        
        public boolean remove(Node[] ns)
        {
            for ( Node n : ns )
            {
                nodeMap.remove(n.getName());
            }
            this.setKeys(nodeMap.keySet());
            return true;
        }

        
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_SET);
        }
        
        protected Node[] createNodes(Object key)
        { 
            return new Node[] {nodeMap.get(key)};
        }
    }

    
    static public Node createProjCfgsNode(final Node nodes[], final Lookup lookup, final String name, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(nodes);
        return new ProjCfgNode(child,lookup,name,icon,act);
    }
    
    static public Node resourcesNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon)
    {
        final Children child=new NodeKeys(nodes);
        return new ResourcesNode(child,lookup,name,dName, icon, null);
    }
    
    static public Node resourcesNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(nodes);
        return new ResourcesNode(child,lookup,name,dName, icon, act);
    }
}

class ActionNode extends AbstractNode
{
    Action[] actions;    
    
    public ActionNode(Children ch,final Lookup lookup,String name,String dName,String icon, Action act[])
    {
        super(ch,lookup);
        setName(name);
        if (dName != null) setDisplayName(dName);
        if (icon  != null) setIconBaseWithExtension(icon);
        actions=act;
    }
    
    public ActionNode(Children ch,final Lookup lookup, String name,String icon, Action act[])
    {
        this(ch,lookup,name,null,icon,act);
    }
    

    public void setActions( final Action[] act)
    {
        actions=act;
    }
    
    
    public Action[] getActions(final boolean context)
    {
        return actions==null?super.getActions(context):actions.clone();
    }
    

    final public void setName(final String name)
    {
        if (name==this.getName())
            fireDisplayNameChange(null, null);
        else
            super.setName(name);
    }

    public String getHtmlDisplayName () {
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
        final Boolean bold=(Boolean)this.getValue("bold");
        if (bold==Boolean.TRUE)
            return "<B>" + displayName + "</B>"; //NOI18N
        
        final Boolean error=(Boolean)this.getValue("error");
        if (error==Boolean.TRUE)
            return "<font color=\"#A40000\">"+displayName+"</font>";
        
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray==Boolean.TRUE)
            return "<font color=\"#A0A0A0\">"+displayName+"</font>";
            
        return displayName ; //NOI18N
            
    }
}

class ProjCfgNode extends ActionNode
{
    protected ProjCfgNode(Children ch,Lookup lookup,String name,String icon, Action act[])
    {
        super(ch,lookup,name,null,icon,act);
    }
    
    private PasteType getPasteType (final Transferable tr, DataFlavor[] flavors ) 
    {
        final String PRIMARY_TYPE = "application";   //NOI18N     
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N
        final HashMap<J2MEProject,HashSet<Node>> map=new HashMap<J2MEProject,HashSet<Node>>();
        
        class CfgPasteType extends PasteType
        {
            public Transferable paste() throws IOException
            {
                final J2MEProject projectDrop=ProjCfgNode.this.getLookup().lookup(J2MEProject.class);
                final J2MEProjectProperties dropProperties = new J2MEProjectProperties( projectDrop, 
                                                    projectDrop.getLookup().lookup(AntProjectHelper.class),
                                                    projectDrop.getLookup().lookup(ReferenceHelper.class), 
                                                    projectDrop.getConfigurationHelper() );
                final ArrayList<ProjectConfiguration> allNames=new ArrayList<ProjectConfiguration>(Arrays.asList(dropProperties.getConfigurations()));                
                final int size=allNames.size();
                ProjectConfiguration cfg=null;
                
                for (J2MEProject project : map.keySet())
                {
                    HashSet<Node> set=map.get(project);
                    final ArrayList<String> allStrNames=new ArrayList<String>(allNames.size()+set.size());
                    final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, 
                            project.getLookup().lookup(AntProjectHelper.class),
                            project.getLookup().lookup(ReferenceHelper.class), 
                            project.getConfigurationHelper() );

                    for (Node node : set)
                    {
                        cfg=node.getLookup().lookup(ProjectConfiguration.class);
                        //Check if configuration with the same name already exist
                        ProjectConfiguration exst=projectDrop.getConfigurationHelper().getConfigurationByName(cfg.getName());
                        if (exst != null)
                        {
                            for (ProjectConfiguration name : allNames)
                                allStrNames.add(name.getName());
                            
                            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allStrNames);
                            final DialogDescriptor dd = new DialogDescriptor(ccp, cfg.getName() + " : " + NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_DuplConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
                            ccp.setDialogDescriptor(dd);
                            final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ccp.getName() : null;
                            if (newName != null) {
                                cfg = new ProjectConfiguration() {
                                    public String getName() {
                                        return newName;
                                    }
                                };
                            }
                            else
                                continue;
                        }
                        final String keys[] = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
                        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + cfg.getName();
                        for (int i=0; i<keys.length; i++) {
                            if (keys[i].startsWith(prefix))
                                dropProperties.put(J2MEProjectProperties.CONFIG_PREFIX + cfg.getName() + keys[i].substring(prefix.length()), j2meProperties.get(keys[i]));
                        }

                        
                        allNames.add(cfg);
                    }
                }
                
                //No configuration was added
                if (allNames.size() == size)
                    return null;
                
                dropProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));
                // Store the properties
                final ProjectConfiguration lcfg=cfg;
                
                Children.MUTEX.writeAccess( new Runnable() 
                {
                    public void run()
                    {
                        try {
                            ProjectManager.mutex().writeAccess( new Runnable() {
                                public void run()
                                {
                                    dropProperties.store();                                        
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                    
                SwingUtilities.invokeLater( new Runnable() 
                {
                    public void run() {  
                        assert lcfg != null;
                        projectDrop.getConfigurationHelper().setActiveConfiguration(lcfg);
                    }
                });
                return tr;
            }
        }
        
        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType ()))
            {
                if (MULTI_TYPE.equals(flavor.getSubType ())) {
                    Node nodes[]=NodeTransfer.nodes(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    for (Node node : nodes)
                    {
                        J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                        if (project != null)
                        {
                            HashSet<Node> set=map.get(project);
                            if (set == null)
                            {
                                set = new HashSet<Node>();
                                map.put(project,set);
                            }
                            set.add(node);
                        }
                    }
                    if (map.size() != 0)
                        return new CfgPasteType();
                }
                if (DND_TYPE.equals(flavor.getSubType ())) {
                    Node node=NodeTransfer.node(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                    if (project != null)
                    {
                        HashSet<Node> set=map.get(project);
                        if (set == null)
                        {
                            set = new HashSet<Node>();
                            map.put(project,set);
                        }
                        set.add(node);
                    }
                    if (map.size() != 0)
                        return new CfgPasteType();
                }
            }
        }
        return null;
    }
    
    public PasteType getDropType(Transferable tr, int action, int index)
    {
        DataFlavor fr[]=tr.getTransferDataFlavors();
        PasteType type=getPasteType(tr,fr);
        return type;
    }
    
    protected void createPasteTypes(Transferable t, List<PasteType> s) 
    {
        PasteType pt=getDropType(t,0,0);
        if (pt != null) s.add(pt);
    }
}

class ResourcesNode extends ActionNode
{
    private static final Image ICON_BADGE = Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/libraries-badge.png");    //NOI18N

    protected ResourcesNode(Children ch,Lookup lookup,String name,String dName,String icon, Action act[])
    {
        super(ch,lookup,name,dName,icon,act);
    }
    
    private PasteType getPasteType (final Transferable tr, DataFlavor[] flavors ) 
    {
        final String PRIMARY_TYPE = "application";   //NOI18N
        final String LIST_TYPE = "x-java-file-list"; //NOI18N
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N
        final HashSet<VisualClassPathItem> set=new HashSet<VisualClassPathItem>();
            
        class NDPasteType extends PasteType
        {
            public Transferable paste() throws IOException
            {
                if (set.size() != 0)                        
                {
                    NodeAction.pasteAction(set,ResourcesNode.this);
                    set.clear();
                }
                    
                return tr;
            }
        }
        
        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType ()))
            {                
                if (LIST_TYPE.equals(flavor.getSubType ())) {
                    List<File> files;
                    try
                    {
                        files = (List<File>) tr.getTransferData(flavor);
                        for (File file : files)
                        {
                            final String s = file.getName().toLowerCase();
                            if (file.isDirectory())
                            {
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem( file,
                                    VisualClassPathItem.TYPE_FOLDER,
                                    null,
                                    file.getPath()));
                            }
                            else if (s.endsWith(".zip") || s.endsWith(".jar"))
                            {
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem( file,
                                    VisualClassPathItem.TYPE_JAR,
                                    null,
                                    file.getPath()));
                            }
                            else
                            {
                                set.clear();
                                return null;
                            }
                        }
                        return new NDPasteType();
                            
                    } catch (Exception ex)
                    {
                        return null;
                    }
                    
                }
                
                 if (MULTI_TYPE.equals(flavor.getSubType ())) {
                    Node nodes[]=NodeTransfer.nodes(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    for (Node node : nodes)
                    {
                        if (node != null && node.getValue("resource") != null )
                        {
                            VisualClassPathItem item=(VisualClassPathItem)node.getValue("VCPI");
                            if (item != null)
                                set.add(item);
                        }
                        //Node is not of correct type
                        else 
                        {
                            set.clear();
                            return null;
                        }
                    }
                    return  new NDPasteType();
                }
                
                if (DND_TYPE.equals(flavor.getSubType ())) {
                    Node node=NodeTransfer.node(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    if (node != null && node.getValue("resource") != null )
                    {
                        VisualClassPathItem item=(VisualClassPathItem)node.getValue("VCPI");
                        if (item != null)
                            set.add(item);
                    }
                    //Node is not of correct type
                    else 
                    {
                        set.clear();
                        return null;
                    }
                    return  new NDPasteType();
                }
            }
        }
        return null;
    }
    
    public Image getIcon( int type ) {        
        Image image = super.getIcon(type);
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }
    
    public Image getOpenedIcon( int type ) {        
        Image image = super.getOpenedIcon(type);
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    
    public PasteType getDropType(Transferable tr, int action, int index)
    {
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray == Boolean.FALSE)
        {
            DataFlavor fr[]=tr.getTransferDataFlavors();
            PasteType type=getPasteType(tr,fr);
            return type;
        }
        return null;
    }
    
    protected void createPasteTypes(Transferable t, List<PasteType> s) 
    {
        PasteType pt=getDropType(t,0,0);
        if (pt != null) s.add(pt);
    }    
}