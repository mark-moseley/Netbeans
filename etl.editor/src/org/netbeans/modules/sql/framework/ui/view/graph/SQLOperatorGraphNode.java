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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphFieldNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicComboBoxArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorGraphFieldNode;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorGraphNode;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.sun.sql.framework.exception.BaseException;

/**
 * Graphical representation of a SQL operator.
 *
 * @author Ritesh Adval
 * @author Jonathan Giron
 */
public class SQLOperatorGraphNode extends OperatorGraphNode implements ItemListener {
    
    protected static final JGoPen PEN_DEFAULT = JGoPen.makeStockPen(Color.WHITE);
    
    protected static final Color COLOR_BG_OUTPUT = new Color(236, 217, 217); // pink 
    
    protected static final Color COLOR_BG_INPUT = new Color(240, 240, 240); // light gray
    
    protected static final Color COLOR_BG_INPUT_HOVER = new Color(254, 254, 244); // light beige
    
    protected static final Color TEXT_COLOR_RESULT = Color.BLACK;
    
    protected static final Color TEXT_COLOR_INPUT = new Color(100, 100, 90); // gray
    
    protected static final Color TEXT_COLOR_LITERAL = new Color(30, 70, 230); // navy    
    
    protected static final JGoBrush BRUSH_OUTPUT_REGULAR = JGoBrush.makeStockBrush(COLOR_BG_OUTPUT);
    
    protected static final JGoBrush BRUSH_INPUT_REGULAR = JGoBrush.makeStockBrush(COLOR_BG_INPUT);
    
    protected static final JGoBrush BRUSH_INPUT_HIGHLIGHTED = JGoBrush.makeStockBrush(COLOR_BG_INPUT_HOVER); 

    private static URL showSqlUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");

    private static URL removeUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/remove.png");
    protected static URL editUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/edit_join.png");
    
    private JMenuItem showSqlItem;
    private JMenuItem removeItem;

    private Map fieldNameToControlMap = new HashMap();

    private boolean showParen = false;
    private ParenthesisCheckBoxArea cbWrap;

    /** Creates a new instance of OperatorGraphNode */
    public SQLOperatorGraphNode(IOperatorXmlInfo info) {
        this(info, false);
    }

    /** Creates a new instance of OperatorGraphNode */
    public SQLOperatorGraphNode(IOperatorXmlInfo info, boolean show) {
        super(info);

        initialize(info);
        initializePopUpMenu();
        
        this.showParen = show;
        if (this.showParen) {
            cbWrap = new ParenthesisCheckBoxArea();
            cbWrap.setBackgroundColor(BRUSH_INPUT_REGULAR.getColor());
            cbWrap.setTextColor(TEXT_COLOR_INPUT);
            this.addObjectAtTail(cbWrap);
            this.addItemListener(new ParenthesisItemListener());
        }

        setSize(this.getMaximumWidth(), this.getMaximumHeight());
    }

    /** Creates a new instance of OperatorGraphNode */
    public SQLOperatorGraphNode(IOperatorXmlInfo info, boolean show, String nameOverride) {
        super(nameOverride, info.getToolTip(), info.getIcon());

        initialize(info);
        initializePopUpMenu();
        
        this.showParen = show;
        if (this.showParen) {
            cbWrap = new ParenthesisCheckBoxArea();
            cbWrap.setBackgroundColor(BRUSH_INPUT_REGULAR.getColor());
            cbWrap.setTextColor(TEXT_COLOR_INPUT);
            this.addObjectAtTail(cbWrap);
            this.addItemListener(new ParenthesisItemListener());
        }

        setSize(this.getMaximumWidth(), this.getMaximumHeight());
    }
    
    public SQLOperatorGraphNode(String displayName, String toolTip, Icon icon) {
        super(displayName, toolTip, icon);
        
        initializePopUpMenu();
        setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * Gets output graph port, given its field name.
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        if (resultField != null) {
            return resultField.getRightGraphPort();
        }
        return null;
    }

    /**
     * get input graph port, given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getInputGraphPort(String fieldName) {
        //special handling for variable argument operator we ignore the fieldName and
        //always return port for first field in the field list
        Object dataObj = this.getDataObject();
        if (dataObj instanceof SQLGenericOperator) {

            SQLGenericOperator operator = (SQLGenericOperator) dataObj;
            if (operator.hasVariableArgs()) {
                IGraphFieldNode fieldNode = (IGraphFieldNode) fieldList.get(0);
                return fieldNode.getLeftGraphPort();
            }
            return super.getInputGraphPort(fieldName);
        }

        return super.getInputGraphPort(fieldName);
    }

    protected void initializePopUpMenu() {
        OperatorActionListener aListener = new OperatorActionListener();
        if( popUpMenu == null ) {
            popUpMenu = new JPopupMenu();
        }
        showSqlItem = new JMenuItem("Show SQL...", new ImageIcon(showSqlUrl));
        showSqlItem.addActionListener(aListener);
        popUpMenu.add(showSqlItem);

        //remove menu
        popUpMenu.addSeparator();
        removeItem = new JMenuItem("Remove", new ImageIcon(removeUrl));
        removeItem.addActionListener(aListener);
        popUpMenu.add(removeItem);
    }

    protected class OperatorActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == showSqlItem) {
                ShowSql_actionPerformed(e);
            } else if (source == removeItem) {
                Remove_ActionPerformed(e);
            }
        }
    }

    /**
     * Invoked when an action occurs.
     */
    private void ShowSql_actionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLOperatorGraphNode.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, new Object[] { sqlObject});
    }

    private void Remove_ActionPerformed(ActionEvent e) {
        this.getGraphView().deleteNode(this);
    }

    protected void initialize(IOperatorXmlInfo info) {
        fieldNameToControlMap = new HashMap();

        // Collect lists of input graph and parameter fields.
        for (int i = 0; i < info.getInputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getInputFields().get(i);
            if (field.isStatic()) {
                BasicComboBoxArea control = new BasicComboBoxArea(field.getName(), null, new Vector(field.getAcceptableValues()), new Vector(
                    field.getAcceptableDisplayValues()), field.getToolTip(), field.isEditable());
                control.addItemListener(this);
                fieldNameToControlMap.put(field.getName(), control);
                fieldList.add(control);
            } else {
                OperatorGraphFieldNode fieldNode = new OperatorGraphFieldNode(BasicCellArea.LEFT_PORT_AREA, field);
                fieldNode.setBrush(BRUSH_INPUT_REGULAR);
                fieldNode.setTextColor(TEXT_COLOR_INPUT);
                fieldNode.setLinePen(PEN_DEFAULT);
                fieldNode.setHighlightEnabled(true);
                fieldNode.getHighlightConfigurator().setHoverBrush(BRUSH_INPUT_HIGHLIGHTED);
                
                fieldList.add(fieldNode);
            }
        }

        Iterator iter = fieldList.iterator();
        while (iter.hasNext()) {
            CanvasArea fieldNode = (CanvasArea) iter.next();
            this.addObjectAtTail(fieldNode);
        }

        // Add output graph field.
        for (int i = 0; i < info.getOutputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getOutputFields().get(i);

            OperatorGraphFieldNode fieldNode = new OperatorGraphFieldNode(BasicCellArea.RIGHT_PORT_AREA, field, JGoText.ALIGN_CENTER);
            fieldNode.setBrush(BRUSH_OUTPUT_REGULAR);
            fieldNode.setTextColor(TEXT_COLOR_RESULT);
            fieldNode.setLinePen(PEN_DEFAULT);
            fieldNode.setHighlightEnabled(false);

            //there is only one result field for operator
            resultField = fieldNode;
            this.addObjectAtTail(fieldNode);
            fieldList.add(fieldNode);
        }

        this.setResizable(true);
    }

    /**
     * Gets the maximum height of the node.
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        int maxHt = super.getMaximumHeight();

        if (this.showParen) {
            maxHt += cbWrap.getMaximumHeight();
        }
        return maxHt;
    }

    /**
     * Gets the maximum width of the node.
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int maxWidth = super.getMaximumWidth();

        if (this.showParen && cbWrap.getMaximumWidth() > maxWidth) {
            maxWidth = cbWrap.getMaximumWidth();
        }

        return maxWidth;
    }

    public int getMinimumHeight() {
        int minHeight = super.getMinimumHeight();

        if (this.showParen) {
            minHeight += cbWrap.getMaximumHeight();
        }

        return minHeight;
    }

    public int getMinimumWidth() {
        int minWidth = super.getMinimumWidth();

        if (this.showParen) {
            minWidth = Math.max(minWidth, cbWrap.getMinimumWidth());
        }

        return minWidth;
    }

    public void setExpanded(boolean sExpanded) {
        super.setExpanded(sExpanded);
        if (sExpanded) {
            setInitialComboBoxState();
        }
    }

    private void setInitialComboBoxState() {
        try {
            if (dataObject instanceof SQLOperator) { // also does implicit null check
                SQLOperator operator = (SQLOperator) dataObject;
                Iterator iter = fieldNameToControlMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String field = (String) entry.getKey();
                    BasicComboBoxArea control = (BasicComboBoxArea) entry.getValue();

                    Object val = operator.getArgumentValue(field);
                    if (val != null && val instanceof SQLLiteral) {
                        SQLLiteral literal = (SQLLiteral) val;
                        String item = literal.getValue();
                        int idx = control.getAcceptableValues().indexOf(item);
                        if (idx != -1) {
                            control.setSelectedItem(control.getAcceptableValues().get(idx));
                        } else if (control.isEditable()) {
                            control.setSelectedItem(item);
                        }
                    } else {
                        //by default first item is selected, so set
                        //that in SQLOperator
                        setArgument(control, (String) control.getAcceptableValues().get(0));
                    }
                }
            }
        } catch (BaseException ex) {
            //TODO log me
            throw new IllegalArgumentException("Cannot initialize values in function drop down list.");
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IGraphNode#setDataObject(java.lang.Object)
     */
    public void setDataObject(Object obj) {
        if (obj instanceof SQLConnectableObject && obj instanceof SQLOperator) {
            super.setDataObject(obj);
            this.setShowParenthesis(((SQLOperator) obj).isShowParenthesis());
            setInitialComboBoxState();
        } else {
            String msg = "SQLOperatorGraphNode only accepts as its data object " + "instances of SQLOperator.";
            try {
                msg = NbBundle.getMessage(SQLOperatorGraphNode.class, "ERROR_paramopgraphnode_invalid_data_object");
            } catch (MissingResourceException ignore) {
                // Do nothing - use default message above.
            }
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Lays out child components.
     */
    public void layoutChildren() {
        Rectangle boundingRect = new Rectangle(this.getBoundingRect());

        int minLayoutHeight = isExpandedState() ? getMaximumHeight() : getMinimumHeight();
        int maxLayoutHeight = isExpandedState() ? getMaximumHeight() : getMinimumHeight();
        if (boundingRect.height < minLayoutHeight) {
            boundingRect.height = minLayoutHeight;
        } else if (boundingRect.height > maxLayoutHeight) {
            boundingRect.height = maxLayoutHeight;
        }

        int minLayoutWidth = getMinimumWidth();
        if (boundingRect.width < minLayoutWidth || !isExpandedState()) {
            boundingRect.width = minLayoutWidth;
        }

        this.setBoundingRect(boundingRect);
        
        columnRect.setBoundingRect(this.getBoundingRect());
        
        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();
        int rectheight = getHeight();

        int left = rectleft + insets.left;
        int top = recttop + insets.top;
        int width = rectwidth - insets.left - insets.right;
        int height = rectheight - insets.top - insets.bottom;

        titleArea.setBoundingRect(left, top, width, titleArea.getMinimumHeight());

        // for generic parameterized operators, fields should be in order (top to bottow):
        // HEADER, [all parameter controls], verticalgap, [all linkable inputs],
        // verticalgap,
        // [all linkable results]

        if (this.showParen) {
            cbWrap.setBoundingRect(left, top + titleArea.getMinimumHeight(), width, cbWrap.getMaximumHeight());
        }

        int aggrHeight = top + titleArea.getMinimumHeight() + (this.showParen ? cbWrap.getMaximumHeight() : 0);

        // Linkable fields

        // loop from the next field onwards
        for (int i = 0; i < fieldList.size(); i++) {
            CanvasArea fieldNode = (CanvasArea) fieldList.get(i);

            if (aggrHeight < top + height) {
                fieldNode.setVisible(true);
                fieldNode.setBoundingRect(left, aggrHeight, width, fieldNode.getHeight() + verticalGap);
            } else {
                fieldNode.setVisible(false);
                fieldNode.setBoundingRect(left, top, width, fieldNode.getHeight() + verticalGap);
            }
            aggrHeight += fieldNode.getHeight() + verticalGap;
        }
    }

    public void itemStateChanged(ItemEvent e) {
        setArgument(e.getSource(), (String) e.getItem());
        setSize(getMaximumWidth(), getMaximumHeight());
        layoutChildren();
    }

    private void setArgument(Object control, String val) {
        String fieldName = getFieldNameFor(control);
        if (fieldName != null && getDataObject() instanceof SQLOperator) {
            SQLOperator operator = (SQLOperator) getDataObject();
            try {
                operator.setArgument(fieldName, val);
            } catch (BaseException ex) {
                // Ignore for now.
            }
        }
    }

    protected BasicComboBoxArea getComboBoxFor(String fieldName) {
        return (BasicComboBoxArea) fieldNameToControlMap.get(fieldName);
    }

    protected String getFieldNameFor(Object control) {
        Iterator iter = fieldNameToControlMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getValue().equals(control)) {
                return (String) entry.getKey();
            }
        }

        return null;
    }

    public void addItemListener(ItemListener l) {
        if (this.cbWrap != null) {
            this.cbWrap.addItemListener(l);
        }
    }

    public void removeItemListener(ItemListener l) {
        if (this.cbWrap != null) {
            this.cbWrap.removeItemListener(l);
        }
    }

    /**
     * whether to select or deselect check box ui
     */
    public void setShowParenthesis(boolean select) {
        if (this.cbWrap != null) {
            this.cbWrap.setShowParenthesis(select);
        }
    }

    class ParenthesisItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e) {
            SQLOperator operator = (SQLOperator) getDataObject();
            if (operator == null) {
                return;
            }

            boolean showParens = false;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                showParens = true;
            }

            operator.setShowParenthesis(showParens);
        }
    }
}

