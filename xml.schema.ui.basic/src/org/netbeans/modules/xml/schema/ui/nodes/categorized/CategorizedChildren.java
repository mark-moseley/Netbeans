/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.FilteredSchemaComponentNodeChildren;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.StructuralSchemaNodeFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class CategorizedChildren<C extends SchemaComponent> 
	extends RefreshableChildren
{
    /**
     *
     *
     */
    public CategorizedChildren(SchemaUIContext context, 
		SchemaComponentReference<C> reference)
    {
		this(context,reference,null);
	}


    /**
     *
     *
     */
    public CategorizedChildren(SchemaUIContext context, 
		SchemaComponentReference<C> reference, 
		List<Class<? extends SchemaComponent>> childFilters)
    {
        super();
		this.context=context;
		this.reference=reference;
		this.childFilters=childFilters;

		extension = new CategorizedChildrenExtension();

		// Create a context to be used for the structural node view
		SchemaModel model = reference.get().getModel();
		structuralContext=new SchemaUIContext(
			model,
			new StructuralSchemaNodeFactory(model,
				context.getLookup()),
			context.getLookup());

		// TODO: Need to receive event and then dispatch refresh to category
		// child directly
//		context.getModel().addPropertyChangeListener(
//			new PropertyChangeListener()
//			{
//				public void propertyChange(PropertyChangeEvent event)
//				{
//					if (event.getSource()==getReference().get())
//						refreshChildren();
//				}
//			});
	}


	/**
	 *
	 *
	 */
	public SchemaUIContext getContext()
	{
		return context;
	}


	/**
	 *
	 *
	 */
	protected SchemaUIContext getStructuralContext()
	{
		return structuralContext;
	}

	/**
	 *
	 *
	 */
	public SchemaComponentReference<C> getReference()
	{
		return reference;
	}


	/**
	 *
	 *
	 */
	public List<Class<? extends SchemaComponent>> getChildFilters()
	{
		return childFilters;
	}


	/**
	 *
	 *
	 */
	@Override
	protected void addNotify()
	{
		super.addNotify();
		refreshChildren();
	}


	/**
	 *
	 *
	 */
	@Override
	protected void removeNotify()
	{
		super.removeNotify();
		super.nodes.clear();
		refresh();
	}


	/**
	 *
	 *
	 */
	public void refreshChildren()
	{
		setKeys(createKeys());
	}


	/**
	 *
	 *
	 */
	private boolean isChildAllowed(
		Class<? extends SchemaComponent> componentClass)
	{
		// If no filters are specified, allow the child
		if (getChildFilters()==null)
			return true;

		for (Class<? extends SchemaComponent> clazz: getChildFilters())
		{
			if (clazz.isAssignableFrom(componentClass))
				return true;
		}

		return false;
	}


	/**
	 *
	 *
	 */
	protected List<Node> createKeys()
	{
		C parentComponent=getReference().get();

		List<Node> keys=new ArrayList<Node>();

		ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
				ReadOnlyCookie.class);
		boolean readOnly = roc!=null && roc.isReadOnly();
//		CustomizerProvider provider = (CustomizerProvider) getNode().
//				getLookup().lookup(CustomizerProvider.class);
//                if (provider != null && (roc == null || !roc.isReadOnly()))
//		{
//			keys.add(new DetailsNode(getContext(),provider));
//		}
//
                // add extension node
                if (!readOnly && getChildFilters() != null &&
                        isChildAllowed(PrimitiveSimpleType.class)) {
                    keys.addAll(extension.getExtension(parentComponent, getContext()));
                }

		// categorize only for schema node
		if(parentComponent instanceof Schema)
		{
			// add children which are not categorized first
			for (SchemaComponent child: parentComponent.getChildren(UNCATEGORIZED_TYPES))
			{
				if(isChildAllowed(child.getComponentType()))
				{
					Node node=getContext().getFactory().createNode(child);
					keys.add(node);
				}
			}
			// add category nodes even if empty
			for(Class<? extends SchemaComponent> componentType:CATEGORIZED_TYPES.keySet())
			{
				// dont create category if filtered
				if(!isChildAllowed(componentType)) continue;
				// In read-only mode, hide empty categories as there is no
				// point in showing them (user cannot create new components).
				if(readOnly && parentComponent.getChildren(componentType).isEmpty()) 
					continue;
				CategoryNode node=new CategoryNode(
						getContext(),
						getReference(),
						componentType,
						new FilteredSchemaComponentNodeChildren<C>(
						getContext(),getReference(),componentType,
						new SchemaComponentComparator()));

				String badge = CATEGORIZED_TYPES.get(componentType);
				node.setBadge(badge);
				keys.add(node);
			}
		}
		else
		{
			// add nodes in lexical order
			for (SchemaComponent child: parentComponent.getChildren())
			{
				if(isChildAllowed(child.getComponentType()))
				{
					Node node=getContext().getFactory().createNode(child);
					keys.add(node);
				}
			}
		}

		return keys;
	}


    /**
     *
     *
     */
	@Override
	protected Node[] createNodes(Object key)
	{
		Node[] result=null;

		if (key instanceof Node)
			result=new Node[] { (Node)key };

		return result;
	}




	////////////////////////////////////////////////////////////////////////////
	// Inner class
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 */
	private static class SchemaComponentComparator
		implements Comparator<SchemaComponent>, Serializable {
            private static final long serialVersionUID = 1L;

		/**
		 *
		 *
		 */
		public int compare(SchemaComponent comp1, 
			SchemaComponent comp2)
		{
			boolean ref1Named=comp1 instanceof Named;
			boolean ref2Named=comp2 instanceof Named;

			if (ref1Named && ref2Named)
			{
				String ref1Name=((Named)comp1).getName();
				String ref2Name=((Named)comp2).getName();

				if (ref1Name!=null && ref2Name!=null)
					return ref1Name.compareTo(ref2Name);
				else
				{
					// Non-null names always sort before null names
					if (ref1Name!=null)
						return -1;
					else
						return 1;
				}
			}
			else
			{
				// Named components come before unnamed components
				if (ref1Named)
					return -1;
				else
					return 1;
			}
		}
	}



        private static Node getFolderNode() {
	    FileObject fo =
		Repository.getDefault().getDefaultFileSystem().getRoot();
	    Node n = null;
	    try {
		DataObject dobj = DataObject.find(fo);
		n = dobj.getNodeDelegate();
	    } catch (DataObjectNotFoundException ex) {
		// cannot get the node for this, this shouldn't happen
		// so just ignore
	    }
	    return n;
	}
        
        public static Image getBadgedFolderIcon(int type, Class<? extends SchemaComponent> _class) {
	    Node n = getFolderNode();
	    Image i = null;
	    if (n != null) {
		i = n.getIcon(type);
	    }
            String badge = CATEGORIZED_TYPES.get(_class);
            if(badge != null)
                return badgeImage(i, badge);
            return null;
	}
        
	public static Image getOpenedBadgedFolderIcon(int type, Class<? extends SchemaComponent> _class) {
	    Node n = getFolderNode();
	    Image i = null;
	    if (n != null) {
		i = n.getOpenedIcon(type);
	    }
            String badge = CATEGORIZED_TYPES.get(_class);
            if(badge != null)
                return badgeImage(i, badge);
	    return null;
	}
        
        
        private static Image badgeImage(Image main, String badge) {
	    Image rv = main;
	    if (badge != null) {
		Image badgeImage = Utilities.loadImage(badge);
		rv = Utilities.mergeImages(main, badgeImage, 8, 8);
	    }
	    return rv;
	}
        

	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private SchemaUIContext context;
	private SchemaUIContext structuralContext;
	private SchemaComponentReference<C> reference;
	private List<Class<? extends SchemaComponent>> childFilters;
	private CategorizedChildrenExtension extension;

	// categories for schema node
	// if we need add for more nodes, might be a good idea to create visitor
	private static final java.util.Map<Class<? extends SchemaComponent>, String> CATEGORIZED_TYPES;
	static {
		CATEGORIZED_TYPES = new LinkedHashMap<Class<? extends SchemaComponent>, String>();
		CATEGORIZED_TYPES.put(GlobalAttribute.class, "org/netbeans/modules/xml/schema/ui/nodes/resources/attribute_badge.png"); // NOI18N
		CATEGORIZED_TYPES.put(GlobalAttributeGroup.class,"org/netbeans/modules/xml/schema/ui/nodes/resources/attribute_badge.png"); // NOI18N
		CATEGORIZED_TYPES.put(GlobalComplexType.class,"org/netbeans/modules/xml/schema/ui/nodes/resources/complexType_badge.png"); // NOI18N
		CATEGORIZED_TYPES.put(GlobalElement.class,"org/netbeans/modules/xml/schema/ui/nodes/resources/element_badge.png"); // NOI18N
		CATEGORIZED_TYPES.put(GlobalGroup.class,"org/netbeans/modules/xml/schema/ui/nodes/resources/group_badge.png"); // NOI18N
		CATEGORIZED_TYPES.put(SchemaModelReference.class,"org/netbeans/modules/xml/schema/ui/nodes/resources/referencedSchemas_badge.png"); //NOI18N
		CATEGORIZED_TYPES.put(GlobalSimpleType.class, "org/netbeans/modules/xml/schema/ui/nodes/resources/simpleType_badge.png"); //NOI18N
	};
	private static final List<Class<? extends SchemaComponent>> UNCATEGORIZED_TYPES;
	static {
		UNCATEGORIZED_TYPES = new ArrayList<Class<? extends SchemaComponent>>(2);
		UNCATEGORIZED_TYPES.add(Annotation.class);
		UNCATEGORIZED_TYPES.add(Notation.class);
	};
}
