/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.properties.PropertiesDataObject; // PENDING
import org.netbeans.spi.project.ui.support.LogicalViews;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.FileOwnerQuery;





/**
 * A static utility class grouping functions for dialogs for selection 
 * sources from project and other places.
 */
public class SelectorUtils {

  /**
   * The filter used to filter out folders and resource bundles.
   */
  private static FilteredNode.NodeFilter BUNDLES_FILTER = 		    
    new FilteredNode.NodeFilter() {
      public boolean acceptNode(Node n) {
	// Has to be data object.
	DataObject dataObject = (DataObject)n.getCookie(DataObject.class);
	if(dataObject == null)
	  return false;
		   
	// Has to be a folder or a resource class.
	return 
	  (dataObject instanceof DataFolder) ||
	  (dataObject instanceof PropertiesDataObject); // PENDING same like above.
      }
    };

				       

  /**
   * Brings up a modal windows for selection of a resource bundle from
   * the give project.
   * @param prj the project to select from
   * @return DataObject representing the selected bundle file or null
   */
  static public DataObject selectBundle(Project prj) {
    try {
      Node root = sourcesNode(prj, BUNDLES_FILTER);

      Node[] selectedNodes= 
	NodeOperation.getDefault().
	select(
	       Util.getString("CTL_SelectPropDO_Dialog_Title"),
	       Util.getString("CTL_SelectPropDO_Dialog_RootTitle"),
	       root,
	       new NodeAcceptor() {
		 public boolean acceptNodes(Node[] nodes) {
		   if(nodes == null || nodes.length != 1) {
		     return false;
		   }

		   // Has to be data object.
		   DataObject dataObject = (DataObject)nodes[0].getCookie(DataObject.class);
		   if(dataObject == null)
		     return false;
                      
		   // Has to be of resource class.
		   return dataObject.getClass().equals(PropertiesDataObject.class); // PENDING same like above.
		 }
	       }
	       );
      return (DataObject)selectedNodes[0].getCookie(DataObject.class);
    } catch (UserCancelException uce) {
      if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
	System.err.println("I18N: User cancelled selection"); // NOI18N
      return null;
    }

  }


  /** 
   * Prepare node structure showing sources 
   * @param prj the project to select from
   * @param filter NodeFilter used to filter only relevant information
   * @return root Node of source files from <code>prj</code> filtered
   * by <code>filter</code>
   **/
  static public Node sourcesNode(Project prj, FilteredNode.NodeFilter filter) {
      Sources src = ProjectUtils.getSources(prj);
      SourceGroup[] srcgrps = src.getSourceGroups("java");
      java.util.List nodes = new ArrayList();      
      for (int i = 0 ; i < srcgrps.length; i++) {
	try {
	  FileObject rfo = srcgrps[i].getRootFolder();
	  FilteredNode node = new FilteredNode(DataObject.find(rfo).getNodeDelegate(),
					       filter);
	  //	  node.setName(srcgrps[i].getName());
	  node.setDisplayName(srcgrps[i].getDisplayName());
	  //	node.setIcon(srcgrps[i].getIcon());
					     
	  nodes.add(node);
	} catch (org.openide.loaders.DataObjectNotFoundException ex) {}
      }

      Children ch = new Children.Array();
      Node[] nodesArray = new Node[ nodes.size() ];
      nodes.toArray( nodesArray );
      ch.add( nodesArray );

      Node repositoryNode = new AbstractNode( ch );
      repositoryNode.setName( NbBundle.getMessage( SelectorUtils.class, "LBL_Sources" ) );
      // XXX Needs some icon.

      return repositoryNode;
      //    }
  }

  /** Instantiate a template object.
   * Asks user for the target file's folder and creates the file.
   * @param obj the template to use
   * @return the generated DataObject
   * @exception UserCancelException if the user cancels the action
   * @exception IOException on I/O error
   * @see DataObject#createFromTemplate
   */
  public static DataObject instantiateTemplate(Project project, DataObject template) throws IOException {
    // Create component for for file name input.
    ObjectNameInputPanel panel = new ObjectNameInputPanel();
        
    Node repositoryNode =  SelectorUtils.sourcesNode(project, BUNDLES_FILTER);
        
    // Selects one folder from data systems.
    DataFolder dataFolder = 
      (DataFolder)NodeOperation.getDefault().select
      (
       I18nUtil.getBundle().getString ("CTL_Template_Dialog_Title"),
       I18nUtil.getBundle().getString ("CTL_Template_Dialog_RootTitle"),
       repositoryNode,
       new NodeAcceptor() {
	 public boolean acceptNodes(Node[] nodes) {
	   if(nodes == null || nodes.length != 1) {
	     return false;
	   }
                    
	   DataFolder cookie = (DataFolder)nodes[0].getCookie(DataFolder.class);
	   return (cookie != null && !cookie.getPrimaryFile().isReadOnly());
	 }
       },
       panel
       )[0].getCookie(DataFolder.class);
        
       String name = panel.getText();
        
       DataObject newObject;
        
       if(name.equals ("")) { // NOI18N
	 newObject = template.createFromTemplate(dataFolder);
       } else {
	 newObject = template.createFromTemplate(dataFolder, name);
       }
        
       try {
	 return newObject;
       } catch(ClassCastException cce) {
	 throw new UserCancelException();
       }
  }

  /** Panel used by <code>instantiateTemplate</code> method. */
  private static class ObjectNameInputPanel extends JPanel {
        
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 1980214734060402958L;

    /** Text field. */
    JTextField text;

        
    /** Constructs panel. */
    public ObjectNameInputPanel () {
      BorderLayout layout = new BorderLayout();
      layout.setVgap(5);
      layout.setHgap(5);
      setLayout(layout);
            
      // label and text field with mnemonic
      String labelText = I18nUtil.getBundle().getString ("LBL_TemplateName");
      JLabel label = new JLabel(labelText.replace('&', ' '));
      text = new JTextField();
      text.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString ("ACS_TEXT_ObjectNameInputPanel"));
            
      label.setDisplayedMnemonic(labelText.charAt(labelText.indexOf('&') + 1));
      label.setLabelFor(text);
            
      add(BorderLayout.WEST, label);
      add(BorderLayout.CENTER, text);
    }

        
    /** Requets focus for text field. */
    public void requestFocus () {
      text.requestFocus ();
    }
        
    /** Getter for <code>text</code>. */
    public String getText () {
      return text.getText ();
    }

    /** Setter for <code>text</code>. */
    public void setText (String s) {
      setText(s);
    }
  } // End of nested class ObjectNameInputPanel
    



}
