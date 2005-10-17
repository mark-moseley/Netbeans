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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JSplitPane;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Dedicated layout support class for JSplitPane.
 * @author Tomas Pavek
 */

public class JSplitPaneSupport extends AbstractLayoutSupport {

    private static Method setLeftComponentMethod;
    private static Method setRightComponentMethod;
    private static Method setTopComponentMethod;
    private static Method setBottomComponentMethod;

    private static String LEFT_TOP_BUTTON = "cp_left_top_button";
    private static String RIGHT_BOTTOM_BUTTON = "cp_right_bottom_button";
	
    /** Gets the supported layout manager class - JSplitPane.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return JSplitPane.class;
    }
    
    /** This method calculates layout constraints for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged, not needed here
     * @param index position (index) of the component in its container;
     *        not needed here
     * @param posInCont position of mouse in the container
     * @param posInComp position of mouse in the dragged component; not needed
     * @return new LayoutConstraints object corresponding to the position of
     *         the component in the container
     */
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        if (!(container instanceof JSplitPane))
            return null;

        JSplitPane splitPane = (JSplitPane) container;
        Dimension sz = splitPane.getSize();
        int orientation = splitPane.getOrientation();

	JButton left  = (JButton) splitPane.getClientProperty(LEFT_TOP_BUTTON);
	JButton right = (JButton) splitPane.getClientProperty(RIGHT_BOTTOM_BUTTON);
	
        if ( (left == null && right == null) || 
	     (left != null && right != null) ) 
	{	    	    
	    String freePosition;        	    
            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                if (posInCont.x <= sz.width / 2) 
                    freePosition = JSplitPane.LEFT;
		else 
                    freePosition = JSplitPane.RIGHT;
            }
            else {				
                if (posInCont.y <= sz.height / 2) 
                    freePosition = JSplitPane.TOP;		
		else 
                    freePosition = JSplitPane.BOTTOM;
            }
	    return new SplitConstraints(freePosition);
	}

	return new SplitConstraints(findFreePosition());	    		
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param newConstraints component layout constraints to be presented
     * @param newIndex component's index position to be presented; not needed
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JSplitPane))
            return false;
	
        String position = (String) newConstraints.getConstraintsObject();
        if (position == null)
            return false;
        
        JSplitPane splitPane = (JSplitPane) container;
        int orientation = splitPane.getOrientation();

        Dimension sz = splitPane.getSize();
        Insets insets = container.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;

        Rectangle rect = new Rectangle(insets.left, insets.top, sz.width, sz.height);

        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            Component left = splitPane.getLeftComponent();
            Component right = splitPane.getRightComponent();
            
            if (position == JSplitPane.LEFT) {
                if ((right == null) || (right == component)) {
                    rect.width = sz.width / 2;
                }
                else {
                    rect.width = right.getBounds().x - rect.x;
                }
            }
            else {
                if ((left == null) || (left == component)) {
                    rect.x = insets.left + sz.width / 2;
                    rect.width = sz.width - rect.x;
                }
                else {
                    rect.x = left.getBounds().x + left.getBounds().width;
                    rect.width = sz.width - rect.x;
                }
            }
        }
        else {
            Component top = splitPane.getTopComponent();
            Component bottom = splitPane.getBottomComponent();
            
            if (position == JSplitPane.TOP) {
                if ((bottom == null) || (bottom == component)) {
                    rect.height /= 2;
                }
                else {
                    rect.height = bottom.getBounds().y - rect.y;
                }
            }
            else {
                if ((top == null) || (top == component)) {
                    rect.y = insets.top + sz.height / 2;
                    rect.height = sz.height - rect.y;
                }
                else {
                    rect.y = top.getBounds().y + top.getBounds().height;
                    rect.height = sz.height - rect.y;
                }
            }
        }
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        return true;
    }

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (!(container instanceof JSplitPane))
            return;	
	
        for (int i=0; i < components.length; i++) {	    	    
	    JSplitPane splitPane = (JSplitPane) container;
	    
            int descPos = convertPosition(getConstraints(i + index));
            if (descPos == 0) {
		if(splitPane.getClientProperty(LEFT_TOP_BUTTON)==null) {	    
		    // store the defaul swing button, so we can fall back to it 
		    // if component[i] will be removed later...
		    splitPane.putClientProperty(LEFT_TOP_BUTTON, splitPane.getLeftComponent());  
		} 
		splitPane.setLeftComponent(components[i]);
	    } 
	    else if (descPos == 1) {
		if(splitPane.getClientProperty(RIGHT_BOTTOM_BUTTON)==null) {
		    // store the defaul swing button, so we can fall back to it 
		    // if component[i] will be removed later...
		    splitPane.putClientProperty(RIGHT_BOTTOM_BUTTON, splitPane.getRightComponent());	    	    	    
		} 							    				
		splitPane.setRightComponent(components[i]);	    
	    }
                
        }	
    }

    /** Removes a real component from a real container.
     * @param container instance of a real container
     * @param containerDelegate effective container delegate of the container
     * @param component component to be removed
     * @return whether it was possible to remove the component (some containers
     *         may not support removing individual components reasonably)
     */
    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component)
    {
	if( !(containerDelegate instanceof JSplitPane) ) {
	    return false; // should not happen
	}	
	
	JSplitPane splitPane = (JSplitPane) containerDelegate;
	
	if( component == splitPane.getLeftComponent() ) { 
	    if( super.removeComponentFromContainer(container, containerDelegate, component) ) {
		JButton left = (JButton) splitPane.getClientProperty(LEFT_TOP_BUTTON);
		if( left != null ) {
		    // fall back to the default swing setting
		    splitPane.setLeftComponent(left);
		    splitPane.putClientProperty(LEFT_TOP_BUTTON, null);
		}	
		return true;
	    }
	} else if ( component == splitPane.getRightComponent() ) {    
	    if( super.removeComponentFromContainer(container, containerDelegate, component) ) {
		JButton right = (JButton) splitPane.getClientProperty(RIGHT_BOTTOM_BUTTON);
		if( right != null ) {
		    // fall back to the default swing setting		    
		    splitPane.setRightComponent(right);		    
		    splitPane.putClientProperty(RIGHT_BOTTOM_BUTTON, null);
		}	
		return true;
	    }
	}
	
        return false;
    }

    /** Removes all components from given real container.
     * @param container instance of a real container to be cleared
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @return whether it was possible to clear the container (some containers
     *         may not support this)
     */
    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {

	// don't remove components which are a default part of JSplitPane
	
	JSplitPane splitPane = (JSplitPane) container;
	JButton left  = (JButton) splitPane.getClientProperty(LEFT_TOP_BUTTON);
	JButton right = (JButton) splitPane.getClientProperty(RIGHT_BOTTOM_BUTTON);
	
	if(left != null) {
	    // left/top component has already been set -> remove it
	    removeComponentFromContainer(container, containerDelegate, splitPane.getLeftComponent());
	}
	if(right != null) {
	    // right/bottom component has already been set -> remove it
	    removeComponentFromContainer(container, containerDelegate, splitPane.getRightComponent());
	}
	
        return true;
    }    
    
    
    // ------

    /** This method is used for scanning code structures and recognizing
     * components added to containers and their constraints. It's called from
     * initialize method. When a relevant code statement is found, then the
     * CodeExpression of component is get and added to component, and also the
     * layout constraints information is read.
     * @param statement CodeStatement to be tested if it contains relevant code
     * @param componentCode CodeGroup to be filled with all component code
     * @return CodeExpression representing found component; null if the
     *         statement is not relevant
     */
    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        CodeExpression[] params = statement.getStatementParameters();
        if (params.length != 1)
            return null;

        String position;

        Object connectingObject = statement.getMetaObject();
        if (getSetLeftComponentMethod().equals(connectingObject))
            position = JSplitPane.LEFT;
        else if (getSetRightComponentMethod().equals(connectingObject))
            position = JSplitPane.RIGHT;
        else if (getSetTopComponentMethod().equals(connectingObject))
            position = JSplitPane.TOP;
        else if (getSetBottomComponentMethod().equals(connectingObject))
            position = JSplitPane.BOTTOM;
        else return null;

        SplitConstraints constr = new SplitConstraints(position);
        getConstraintsList().add(constr);

        componentCode.addStatement(statement);

        return params[0];
    }

    /** Creates code for a component added to the layout (opposite to
     * readComponentCode method).
     * @param componentCode CodeGroup to be filled with complete component code
     *        (code for initializing the layout constraints and adding the
     *        component to the layout)
     * @param componentExpression CodeExpression object representing component
     * @param index position of the component in the layout
     */
    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression componentExpression,
                                       int index)
    {
        LayoutConstraints constr = getConstraints(index);
        if (!(constr instanceof SplitConstraints))
            return; // should not happen

        ((SplitConstraints)constr).createComponentCode(
                               componentCode,
                               getLayoutContext().getContainerCodeExpression(),
                               componentExpression);
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new SplitConstraints(findFreePosition());
    }

    // ------------

    private int convertPosition(LayoutConstraints desc) {
        Object position = desc.getConstraintsObject();
        if (JSplitPane.LEFT.equals(position) || JSplitPane.TOP.equals(position))
            return 0;
        if (JSplitPane.RIGHT.equals(position) || JSplitPane.BOTTOM.equals(position))
            return 1;
        return -1;
    }

    private String findFreePosition() {
        int leftTop = 0, rightBottom = 0;
        int orientation = JSplitPane.HORIZONTAL_SPLIT;
	
        for (int i=0, n=getComponentCount(); i < n; i++) {
            LayoutConstraints constraints = getConstraints(i);
            if (!(constraints instanceof SplitConstraints))
                continue;

            int constrPos = convertPosition(constraints);
            if (constrPos == 0)
                leftTop++;
            else if (constrPos == 1)
                rightBottom++;
        }

        if (leftTop == 0 || leftTop < rightBottom)
            return orientation == JSplitPane.HORIZONTAL_SPLIT ?
                JSplitPane.LEFT : JSplitPane.TOP;
        else 
            return orientation == JSplitPane.HORIZONTAL_SPLIT ?
                JSplitPane.RIGHT : JSplitPane.BOTTOM;	
    }

    // --------

    private static Method getSetLeftComponentMethod() {
        if (setLeftComponentMethod == null)
            setLeftComponentMethod = getAddMethod("setLeftComponent"); // NOI18N
        return setLeftComponentMethod;
    }

    private static Method getSetRightComponentMethod() {
        if (setRightComponentMethod == null)
            setRightComponentMethod = getAddMethod("setRightComponent"); // NOI18N
        return setRightComponentMethod;
    }

    private static Method getSetTopComponentMethod() {
        if (setTopComponentMethod == null)
            setTopComponentMethod = getAddMethod("setTopComponent"); // NOI18N
        return setTopComponentMethod;
    }

    private static Method getSetBottomComponentMethod() {
        if (setBottomComponentMethod == null)
            setBottomComponentMethod = getAddMethod("setBottomComponent"); // NOI18N
        return setBottomComponentMethod;
    }

    private static Method getAddMethod(String name) {
        try {
            return JSplitPane.class.getMethod(name,
                                              new Class[] { Component.class });
        }
        catch (NoSuchMethodException ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    // -----------

    /** LayoutConstraints implementation holding component position in
     * JSplitPane.
     */
    public static class SplitConstraints implements LayoutConstraints {
        private String position;

        private Node.Property[] properties;

        private CodeExpression containerExpression;
        private CodeExpression componentExpression;
        private CodeGroup componentCode;

        public SplitConstraints(String position) {
            this.position = position;
        }

        public Node.Property[] getProperties() {
            if (properties == null)
                properties = new Node.Property[] {
                    new FormProperty(
                            "SplitConstraints splitPosition", // NOI18N
                            String.class,
                            getBundle().getString("PROP_splitPos"), // NOI18N
                            getBundle().getString("HINT_splitPos")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return position;
                        }
                        public void setTargetValue(Object value) {
                            position = (String)value;
                        }
                        public PropertyEditor getExpliciteEditor() {
                            return new SplitPositionEditor();
                        }
                        protected void propertyValueChanged(Object old,
                                                            Object current) {
                            if (isChangeFiring())
                                updateCode();
                            super.propertyValueChanged(old, current);
                        }
                        public void setPropertyContext(
                            org.netbeans.modules.form.FormPropertyContext ctx)
                        { // disabling this method due to limited persistence
                        } // capabilities (compatibility with previous versions)
                    }
                };

            return properties;
        }

        public Object getConstraintsObject() {
            return position;
        }

        public LayoutConstraints cloneConstraints() {
            return new SplitConstraints(position);
        }

        private void createComponentCode(CodeGroup compCode,
                                         CodeExpression contExp,
                                         CodeExpression compExp)
        {
            componentCode = compCode;
            containerExpression = contExp;
            componentExpression = compExp;
            updateCode();
        }

        private void updateCode() {
            if (componentCode == null)
                return;

            CodeStructure.removeStatements(
                componentCode.getStatementsIterator());
            componentCode.removeAll();

            Method addMethod;
            if (JSplitPane.LEFT.equals(position))
                addMethod = getSetLeftComponentMethod();
            else if (JSplitPane.RIGHT.equals(position))
                addMethod = getSetRightComponentMethod();
            else if (JSplitPane.TOP.equals(position))
                addMethod = getSetTopComponentMethod();
            else if (JSplitPane.BOTTOM.equals(position))
                addMethod = getSetBottomComponentMethod();
            else return;

            componentCode.addStatement(
                    CodeStructure.createStatement(
                           containerExpression,
                           addMethod,
                           new CodeExpression[] { componentExpression }));
        }
    }

    static class SplitPositionEditor extends PropertyEditorSupport {
        private final String[] values = {
            JSplitPane.LEFT,
            JSplitPane.RIGHT,
            JSplitPane.TOP,
            JSplitPane.BOTTOM
        };

        public String[] getTags() {
            return values;
        }

        public String getAsText() {
            return (String)getValue();
        }

        public void setAsText(String str) {
            for (int i = 0; i < values.length; i++)
                if (str.equals(values[i])) {
                    setValue(str);
                    break;
                }
        }
    }
    
}
