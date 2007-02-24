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

package org.netbeans.modules.uml.designpattern;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditor;
import org.netbeans.modules.uml.ui.swing.treetable.JDefaultMutableTreeNode;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;


/**
 * @author sumitabhk
 *
 */
public class RoleTreeTableModel implements TreeTableModel
{
	// Names of the columns.
	static protected String[]  cNames = {"", DefaultDesignPatternResource.getString("IDS_ROLE"), DefaultDesignPatternResource.getString("IDS_PARTICIPANT")};

	// Types of the columns.
	static protected Class[]  cTypes = { String.class, String.class, String.class};

	private Vector<Object> m_Children = null;
	private HashMap<Object, Vector<Object> > m_BuiltChildren = new HashMap<Object, Vector<Object>>();

	private WizardRoles m_RolesClass = null;

	protected JDefaultMutableTreeNode root = null;

	private static JTreeTable treeTable = null;


	/**
	 *
	 */
	public RoleTreeTableModel()
	{
		super();
	}

	public RoleTreeTableModel(JDefaultMutableTreeNode root, WizardRoles rolesClass)
	{
		super();
		this.root = root;
		m_RolesClass = rolesClass;
	}

	public RoleTreeTableModel(JDefaultMutableTreeNode root, IDesignPatternRole pRole)
	{
		super();
		this.root = root;
		JDefaultMutableTreeNode node = new JDefaultMutableTreeNode(pRole, true);
		root.add(node);

	}
	public static void main(String[] args)
	{
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return cNames.length;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnName(int)
	 */
	public String getColumnName(int column)
	{
		return cNames[column];
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int column)
	{
		if (column == 1)
		{
			return this.getClass();
		}
		if (column <= cTypes.length)
			return cTypes[column];
		return null;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getValueAt(java.lang.Object, int)
	 */
	public Object getValueAt(Object node, int column)
	{
		Object retObj = null;
		if (node instanceof JDefaultMutableTreeNode)
		{
			JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj instanceof WizardRoleObject) {
				WizardRoleObject pRole = (WizardRoleObject)n.getUserObject();
				if (column == 0)
				{
					CommonResourceManager mgr = CommonResourceManager.instance();
					retObj = mgr.getIconForDisp(pRole.getRole().getElement());
				}
				else if (column == 1)
				{
					retObj = pRole.getRole().getName();
				}
				else if (column == 2)
				{
					retObj = pRole.getChosenName();
				}
			}
		}
		return retObj;
	}


	/* (non-Javadoc)
	 * @see TreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	public boolean isCellEditable(Object node, int column)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	public void setValueAt(Object aValue, Object node, int column)
	{
		if (aValue instanceof JTreeTable.TreeTableCellRenderer)
		{
			JTreeTable.TreeTableCellRenderer rend =
					(JTreeTable.TreeTableCellRenderer)aValue;

		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index)
	{
		Object retObj = null;
		if (parent != null)
		{
			JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)parent;
			int count = node.getChildCount();
			if (count > 0 && index <= count)
			{
				retObj = node.getChildAt(index);
			}
			else
			{
				retObj = getChildren(parent)[index];
			}
		}
		return retObj;
	}

	protected Object[] getChildren(Object node) {
		Object[] retObj = null;
		JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
		int count = n.getChildCount();
		if (count == 0)
		{
			Object o = n.getUserObject();
			if (o instanceof WizardRoleObject)
			{
				WizardRoleObject obj = (WizardRoleObject)o;
				Vector<WizardRoleObject> children = obj.getChildren();
				retObj = children.toArray();
			}
		}
		return retObj;
	}

	public void setChildren(Vector<Object> newChildren)
	{
		m_Children.clear();
		m_Children = newChildren;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent)
	{
		int retCount = 0;
		if (parent != null)
		{
			JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)parent;
			int count = node.getChildCount();
			if (count > 0)
			{
				retCount = count;
			}
			else
			{
				Object[] objs = getChildren(parent);
				if (objs != null)
				{
					retCount = objs.length;
				}
			}
		}
		return retCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node)
	{
		if (node instanceof JDefaultMutableTreeNode)
		{
			JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj != null && obj instanceof WizardRoleObject)
			{
				WizardRoleObject pRole = (WizardRoleObject)obj;
				if (pRole.getChildrenCount() > 0)
				{
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		for (int i = 0; i < getChildCount(parent); i++) {
		if (getChild(parent, i).equals(child)) {
				return i;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{

	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot()
	{
		return root;
	}

	public void expand(int row, boolean val)
	{
		treeTable.getTree().expandNode(row, val);

	}

	public void setTreeTable (JTreeTable tree)
	{
		treeTable = tree;
	}

}
