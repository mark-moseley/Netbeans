/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package com.netbeans.enterprise.modules.db.explorer.dataview;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.table.AbstractTableModel;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.awt.SplittedPanel;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.*;
import org.openide.windows.TopComponent;

import com.netbeans.ddl.DBConnection;
import com.netbeans.enterprise.modules.db.explorer.infos.ColumnNodeInfo;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;

public class DataViewWindow extends TopComponent
{
	private JTextArea queryarea;
	private JTable jtable;
	private DataModel dbadaptor;
	private JComboBox rcmdscombo;
	private String schema;
	private ResourceBundle bundle;
	
	public DataViewWindow(DBConnection dbcon, String user, String query)
	throws SQLException
	{
		schema = user;
		try {
		  setName("Data View");
			setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints con = new GridBagConstraints ();
			setLayout (layout);
			bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
		
			// Data model
			
			dbadaptor = new DataModel(dbcon);
		
			// Query area and button
			
			JPanel subpane = new JPanel();
			GridBagLayout sublayout = new GridBagLayout();
			GridBagConstraints subcon = new GridBagConstraints ();
			subpane.setLayout(sublayout);

	        subcon.fill = GridBagConstraints.BOTH;
	        subcon.weightx = 1.0;
	        subcon.weighty = 1.0;
	        subcon.gridx = 0;
	        subcon.gridwidth = 2;
	        subcon.gridy = 0;
			subcon.insets = new java.awt.Insets (0, 0, 5, 0);
			queryarea = new JTextArea(query, 3, 70);
			queryarea.setLineWrap(true);
			queryarea.setWrapStyleWord(true);
			queryarea.setDropTarget(new DropTarget(queryarea, new ViewDropTarget()));
			JScrollPane scrollpane = new JScrollPane(queryarea);
	        sublayout.setConstraints(scrollpane, subcon);
	        subpane.add(scrollpane);
	
			// Combo recent commands
			
	        subcon.fill = GridBagConstraints.HORIZONTAL;
	        subcon.weightx = 1.0;
	        subcon.weighty = 0.0;
	        subcon.gridx = 0;
	        subcon.gridy = 1;
	        subcon.gridwidth = 1;
			subcon.insets = new java.awt.Insets (0, 0, 5, 5);
			subcon.anchor = GridBagConstraints.SOUTH;
			rcmdscombo = new JComboBox(new ComboModel());
	        sublayout.setConstraints(rcmdscombo, subcon);
	        subpane.add(rcmdscombo);
	        rcmdscombo.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	JComboBox source = (JComboBox)e.getSource();
		        	RecentCommand cmd = (RecentCommand)source.getSelectedItem();
		            if (cmd != null) setQuery(cmd.getCommand());
		        }
		    });
			
	
			// Button Fetch
	
	        subcon.gridx = 1;
	        subcon.gridy = 1;
	        subcon.weightx = 0.0;
	        subcon.weighty = 0.0;
			subcon.insets = new java.awt.Insets (0, 0, 5, 0);
	        subcon.fill = GridBagConstraints.HORIZONTAL;
			subcon.anchor = GridBagConstraints.SOUTH;
			JButton fetchbtn = new JButton("Fetch");
	        sublayout.setConstraints(fetchbtn, subcon);
			subpane.add(fetchbtn);
	        fetchbtn.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            fetch();
		        }
		    });
		
			// Table with results

//	        TableSorter sorter = new TableSorter();
	        jtable = new JTable(dbadaptor/*sorter*/);
	        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//			sorter.addMouseListenerToHeaderInTable(table);
	        scrollpane = new JScrollPane(jtable);
	        
	        // Add it into splitview
	        
	        con.weightx = 1.0;
	        con.weighty = 1.0;
	        con.fill = GridBagConstraints.BOTH;
	        con.gridx = 0;
	        con.gridwidth = 1;
	        con.gridy = 1;
	        
			SplittedPanel split = new SplittedPanel();
			split.setSplitType(SplittedPanel.VERTICAL);
			split.setSplitTypeChangeEnabled(false);
			split.setSplitAbsolute(false);
			split.setSplitPosition(20);
    		split.add(subpane, SplittedPanel.ADD_LEFT);
    		split.add(scrollpane, SplittedPanel.ADD_RIGHT);
	        layout.setConstraints(split, con);
	        add(split);
       
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
	}
         
	/** Returns query used by panel.
	*/
	public String getQuery()
	{
		return queryarea.getText();
	}
	
	/** Sets query used by panel.
	*/
	public void setQuery(String q)
	{
		queryarea.setText(q);
	}		        

	public void fetch()
	{
		try {
			String text = queryarea.getText();
			dbadaptor.executeQuery(text);
			RecentCommand rcmd = new RecentCommand(text);
			((ComboModel)rcmdscombo.getModel()).addElement(rcmd);
		} catch (Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("DataViewFetchErrorPrefix")+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
          
    class ColDef
    {
    	private String name;
    	private boolean writable;
    	private boolean bric;
    	int datatype;
    	
    	public ColDef(String name, boolean flag)
    	{
    		this.name = name;
    		writable = flag;
		}
		    
    	public String getName()
    	{
    		return name;
    	}
    	
    	public void setName(String name)
    	{
    		this.name = name;
    	}

    	public int getDataType()
    	{
    		return datatype;
    	}
    	
    	public void setDataType(int type)
    	{
    		datatype = type;
    	}
    	
    	public boolean isWritable()
    	{
    		return writable;
    	}
    	
    	public void setWritable(boolean flag)
    	{
    		writable = flag;
    	}
    	
    	public boolean isBestRowIdentifierColumn()
    	{
    		return bric;
    	}
    	
    	public void setBestRowIdentifierColumn(boolean flag)
    	{
    		bric = flag;
    	}
    }
    
    class RecentCommand
    {
    	private String command;

		public RecentCommand(String cmd)
		{
			command = cmd;
		}
		
		public String toString()
		{
			return command;
		}		 	

		public String getCommand()
		{
			return command;
		}
		
		public boolean equals(Object obj)
		{
			if (obj instanceof RecentCommand) return ((RecentCommand)obj).getCommand().equals(command);
			return super.equals(obj);
		}
    }

    class ComboModel extends AbstractListModel implements MutableComboBoxModel
 
    {
		Vector commands;
		Object selected;
		
		public ComboModel() 
		{
			this(new Vector(1));
		}
		
		public ComboModel(Vector elems) 
		{
			commands = elems;
		}

		 public Object getSelectedItem()
		 {
		 	return selected;	
		 }
		
		public void setSelectedItem(Object anItem)
		{
			selected = anItem;
			fireContentsChanged(this,-1,-1);
		}

		public void addElement(Object obj)
		{
			if (!commands.contains(obj)) {
				commands.add(obj);
				fireContentsChanged(this,-1,-1);
			}
		}

		public void removeElement(Object obj)
		{
			commands.removeElement(obj);
			fireContentsChanged(this,-1,-1);
		}
		
		public void insertElementAt(Object obj, int index)
		{
			if (!commands.contains(obj)) {
				commands.insertElementAt(obj, index);
				fireContentsChanged(this,-1,-1);
			}
		}
		
		public void removeElementAt(int index)
		{
			commands.removeElementAt(index);
			fireContentsChanged(this,-1,-1);
		}

		public int getSize() 
		{
			return commands.size();
		}

		public Object getElementAt(int index) 
		{
			return commands.get(index);
		}	
	}

	class ViewDropTarget implements DropTargetListener
	{
		/** User is starting to drag over us */
		public void dragEnter (DropTargetDragEvent dtde) 
		{
			dtde.acceptDrag(dtde.getDropAction());
		}
		
		/** User drags over us */
		public void dragOver (DropTargetDragEvent dtde) 
		{
		}
		
		public void dropActionChanged (DropTargetDragEvent dtde) 
		{
		}
		
		/** User exits the dragging */
		public void dragExit (DropTargetEvent dte) 
		{
		}
    
    	private ColumnNodeInfo getNodeInfo(Transferable t)
    	{
			Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
			if (n != null) return (ColumnNodeInfo)n.getCookie(ColumnNodeInfo.class);
			n = NodeTransfer.node(t, NodeTransfer.COPY);
			if (n != null) return (ColumnNodeInfo)n.getCookie(ColumnNodeInfo.class);
			return null;
		}
    
		/** Performs the drop action */
		public void drop (DropTargetDropEvent dtde) 
		{
			String query = null;
			Object obj = null;
			Transferable t = dtde.getTransferable();
			StringBuffer buff = new StringBuffer();
			
			try {
				DataFlavor multiFlavor = new DataFlavor (
					NbBundle.getBundle(ExTransferable.class).getString("MultiNodeMimeType"),
					NbBundle.getBundle (ExTransferable.class).getString ("transferFlavorsMultiFlavorName")
    			);

				if (t.isDataFlavorSupported(multiFlavor)) {
					MultiTransferObject mobj = (MultiTransferObject)t.getTransferData(ExTransferable.multiFlavor);
					int count = mobj.getCount();
					int tabidx = 0;
					HashMap tabidxmap = new HashMap();
					for (int i = 0; i < count; i++) {
						ColumnNodeInfo nfo = getNodeInfo(mobj.getTransferableAt(i));
						if (nfo != null) {
							String tablename = nfo.getTable();
							Integer tableidx = (Integer)tabidxmap.get(tablename);
							if (tableidx == null) tabidxmap.put(tablename, tableidx = new Integer(tabidx++));
							if (buff.length()>0) buff.append(", ");
							buff.append("t"+tableidx+"."+nfo.getName());
						}
					}
					
					StringBuffer frombuff = new StringBuffer();
					Iterator iter = tabidxmap.keySet().iterator();
					while (iter.hasNext()) {
						String tab = (String)iter.next();
						if (frombuff.length()>0) frombuff.append(", ");
						frombuff.append(tab + " t"+tabidxmap.get(tab));
					}

					query = "select "+buff.toString()+" from "+frombuff.toString();

				} else {
					ColumnNodeInfo nfo = getNodeInfo(t);
					if (nfo != null) query = "select "+nfo.getName()+" from "+nfo.getTable();
				}
	
				if (query != null) setQuery(query);
				
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}

	class DataModel extends AbstractTableModel 
	{
		DBConnection dbcon;
		Vector coldef;
		Vector data;
		boolean editable = false;
		
		/** Constructor */
		public DataModel(DBConnection dbcon)
		throws SQLException
		{
			this.dbcon = dbcon;
		}
		
		/** Executes query
		* @param query SQL Expression
		*/
		public void executeQuery(String query) 
		throws Exception
		{
	    	if (query.length() == 0) return;
	    	if (!query.startsWith("select")) throw new Exception("not a select");
			Connection con = dbcon.createJDBCConnection();
			Statement stat = con.createStatement();
	        ResultSet rset = stat.executeQuery(query);
	        ResultSetMetaData mdata = rset.getMetaData();
	        String gschema = null;
	        
			int cols = mdata.getColumnCount();
			coldef = new Vector(cols);
	        for(int column = 1; column <= cols; column++) {
	        	ColDef cd = new ColDef(mdata.getColumnLabel(column), mdata.isWritable(column));
	        	cd.setDataType(mdata.getColumnType(column));
	            coldef.add(cd);
	        }
			
	        // Get all rows.
	        // In future implementations should be more careful
		        
			int rcounter = 0, limit = RootNode.getOption().getFetchLimit();
			int step = RootNode.getOption().getFetchStep();
	        data = new Vector();
	        while (rset.next()) {
	            Vector row = new Vector(cols);
	            for (int column = 1; column <= cols; column++) {
	            	row.add(rset.getObject(column));
	            }
	            data.addElement(row);
	            
	            // Catch row count
	            
	            if (++rcounter >= limit) {
	            	String[] arr = new String[] {
	            		(new Integer(rcounter)).toString(), 
	            		(new Integer(step)).toString()
	            	};
	            	String cancel = bundle.getString("DataViewCancelButton");
	            	String nextset = bundle.getString("DataViewNextFetchButton");
	            	String allset = bundle.getString("DataViewAllFetchButton");
	            	String message = MessageFormat.format(bundle.getString("DataViewMessage"), arr);
	            	NotifyDescriptor ndesc = new NotifyDescriptor(message, "Fetch data", NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, new Object[] {nextset, allset, cancel}, NotifyDescriptor.CANCEL_OPTION);
	            	String retv = (String)TopManager.getDefault().notify(ndesc);	
	            	if (retv.equals(allset)) limit = Integer.MAX_VALUE;
	            	else if (retv.equals(nextset)) limit = limit + step;
	            	else break;
	            }
	        }

	        rset.close();
	        stat.close();

			// Get best row identifier for update
/*			
			DatabaseMetaData dmd = con.getMetaData();
			rset = dmd.getBestRowIdentifier(con.getCatalog(), schema, mdata.getTableName(), DatabaseMetaData.bestRowSession, false);
	        while (rset.next()) {
	        	String col = rset.getString("COLUMN_NAME");
	        	Enumeration enu = coldef.elements();
	        	while (enu.hasMoreElements()) {
	        		ColDef cd = (ColDef)enu.nextElement();
	        		if (cd.getName().equals(col)) {
	        			cd.setBestRowIdentifierColumn(true);
	        			break;
	        		}
	        	}
			}
*/		
			con.close();
	        fireTableChanged(null);
		}
		
		/** Returns column name
		* @param column Column index
		*/
	    public String getColumnName(int column) 
	    {
	    	if (column < coldef.size()) {
	    		String cname = ((ColDef)coldef.elementAt(column)).getName();
	    		return cname;
	    	}
	    	
			return "";
	    }
	
		/** Returns column renderer/editor class
		* @param column Column index
		*/
	    public Class getColumnClass(int column) 
	    {
			if (column < coldef.size()) {
				int coltype = ((ColDef)coldef.elementAt(column)).getDataType();
		    	switch (coltype) {
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR: return String.class;
					case Types.BIT: return Boolean.class;
					case Types.TINYINT:
					case Types.SMALLINT:
					case Types.INTEGER: return Integer.class;
					case Types.BIGINT: return Long.class;
					case Types.FLOAT:
					case Types.DOUBLE: return Double.class;
					case Types.DATE: return java.sql.Date.class;
		    	}
		    }
	    	
	    	return Object.class;
	    }

		/** Returns true, if cell is editable
		*/
	    public boolean isCellEditable(int row, int column) 
	    {
	    	if (!editable) return false;
	    	if (column < coldef.size()) return ((ColDef)coldef.elementAt(column)).isWritable();
			return false;
	    }
	
		/** Returns colun count
		*/
	    public int getColumnCount() 
	    {
	    	if (coldef == null) return 0;
	        return coldef.size();
	    }
	
		/** Returns row count
		*/
	    public int getRowCount() 
	    {
	    	if (data == null) return 0;
	        return data.size();
	    }
	
		/** Returns value at specified position
		*/
	    public Object getValueAt(int aRow, int aColumn) 
	    {
	    	Vector row = null;
	        if (aRow < data.size()) row = (Vector)data.elementAt(aRow);
	        if (row != null && aColumn<row.size()) return row.elementAt(aColumn);
	        return null;
	    }
	    	    
	    private String format(Object value, int type)
	    {
	        if (value == null) return "null";
			switch(type) {
				case Types.INTEGER:
				case Types.DOUBLE:
				case Types.FLOAT: return value.toString();
				case Types.BIT: return ((Boolean)value).booleanValue() ? "1" : "0";
				case Types.DATE: return value.toString();
				default: return "\""+value.toString()+"\"";
			}
		}	    		   
	    	    
	    public void setValueAt(Object value, int row, int column) 
	    {
	    	int enucol = 0;
	    	StringBuffer where = new StringBuffer();
	    	HashMap map = new HashMap();
        	Enumeration enu = coldef.elements();
        	while (enu.hasMoreElements()) {
        		ColDef cd = (ColDef)enu.nextElement();
	       		if (cd.isBestRowIdentifierColumn()) {
	       			String key = cd.getName();
	       			String val = format(getValueAt(row,enucol), cd.getDataType());
					if (where.length()>0) where.append(" and ");
					where.append(key+" = "+val);
				}
				enucol++;
			}
		}
	}
}
