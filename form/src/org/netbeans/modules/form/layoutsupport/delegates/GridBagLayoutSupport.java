/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.lang.ref.*;
import java.lang.reflect.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Support class for GridBagLayout. This is an example of support for layout
 * managers with complex layout constraints for which rather special code
 * structure must be managed - GridBagConstraints require to be set up
 * field by field.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridBagLayoutSupport extends AbstractLayoutSupport
{
    private static final int X_AXIS = 1;
    private static final int Y_AXIS = 2;

    private static Constructor constrConstructor;

    private static Reference customizerRef;

    /** Gets the supported layout manager class - GridBagLayout.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return GridBagLayout.class;
    }

    /** Returns a class of customizer for GridBagLayout.
     * @return layout customizer class
     */
    public Class getCustomizerClass() {
        return GridBagCustomizer.Window.class;
    }

    /** Creates an instance of customizer for GridBagLayout.
     * @return layout customizer class
     */
    public Component getSupportCustomizer() {
        GridBagCustomizer.Window customizer = null;
        if (customizerRef != null)
            customizer = (GridBagCustomizer.Window) customizerRef.get();
        if (customizer == null) {
            customizer = new GridBagCustomizer.Window();
            customizerRef = new WeakReference(customizer);
        }
        customizer.setObject(this);
        return customizer;
    }

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). Conversion from AbsoluteConstraints to
     * GridBagConstraints is implemented here.
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        if (currentConstraints == null || components == null
                || components.length > currentConstraints.length
                || components.length == 0
                || !(previousConstraints[0]
                     instanceof AbsoluteLayoutSupport.AbsoluteLayoutConstraints))
            return;
        
        List xlines = new ArrayList();
        List ylines = new ArrayList();
        
        Rectangle parentbound;
        Container con = components[0].getParent();
        if (con == null) {
            parentbound = components[0].getBounds();
        } else {
            parentbound = con.getBounds();
        }
        
        // Determine the size of the grid
        insertLines(0, xlines);
        insertLines(0, ylines);
        
        for (int i=0; i < components.length; i++) {
            Rectangle ibounds = components[i].getBounds();
            
            if (ibounds.width > 0) {
                insertLines(ibounds.x + ibounds.width, xlines);
            } else {
                insertLines(ibounds.x + 1, xlines);
            }
            
            if (ibounds.height > 0) {
                insertLines(ibounds.y + ibounds.height, ylines);
            } else {
                insertLines(ibounds.y + 1, ylines);
            }
        }
        
        // Determine grid width of components.
        LayoutInfo[] layouts = new LayoutInfo[components.length];
        for (int i=0; i < layouts.length; i++)
            layouts[i] = new LayoutInfo();
        
        for (int i=0; i < xlines.size() - 1; i++) {
            int x1 = ((Integer)xlines.get(i)).intValue();
            int x2 = ((Integer)xlines.get(i+1)).intValue();
            
            for (int j=0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                if (jbounds.width <= 0) {
                    jbounds.width = 1;
                }
                if (isOverlapped(x1, x2, jbounds.x, jbounds.x + jbounds.width - 1))
                    layouts[j].incGridWidth(i);
            }
        }
        
        // Determine grid height of components.
        for (int i=0; i < ylines.size() - 1; i++) {
            int y1 = ((Integer)ylines.get(i)).intValue();
            int y2 = ((Integer)ylines.get(i+1)).intValue();
            
            for (int j=0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                if (jbounds.height <= 0) {
                    jbounds.height = 1;
                }
                if (isOverlapped(y1, y2, jbounds.y, jbounds.y + jbounds.height - 1))
                    layouts[j].incGridHeight(i);
            }
        }
        
        // Calculate insets of the components.
        for (int i=0; i < components.length; i++) {
            Rectangle curbounds = components[i].getBounds();
            int lastleft = 0;
            int lasttop = 0;
            
            for (int j = 0; j < components.length; j++) {
                Rectangle jbounds = components[j].getBounds();
                int width = jbounds.width;
                if(width < 0) width = 0;
                if(jbounds.x + width - 1 < curbounds.x){
                    if(jbounds.x + width > lastleft){
                        lastleft = jbounds.x + width;
                    }
                }
                int height = jbounds.height;
                if (height < 0) height = 0;
                if (jbounds.y + height - 1 < curbounds.y) {
                    if(jbounds.y + height > lasttop){
                        lasttop = jbounds.y + height;
                    }
                }
            }
            
            layouts[i].setLeft(curbounds.x - lastleft);
            layouts[i].setTop(curbounds.y - lasttop);
            
            int width = (curbounds.width < 0) ? 0 : curbounds.width;
            int height = (curbounds.height < 0) ? 0 : curbounds.height;

            if (layouts[i].getLastGridX() == xlines.size() - 2) {
                layouts[i].setRight(parentbound.width - curbounds.x - width);
            }
            if (layouts[i].getLastGridY() == ylines.size() - 2) {
                layouts[i].setBottom(parentbound.height - curbounds.y - height);
            }
        }
        
        // GridBagLayout puts the remaining width of the component into the last
        // grid column/row (if the component has weight 0). This would not be
        // a problem for us if it would take components sorted according to
        // their increasing x/y grid coordinate. Unfortunately it takes
        // components sorted according to their increasing grid width/height.
        // This can result in a layout that is much wider/higher then the previous
        // absolute layout. The following code forces the right order by
        // introduction of new (otherwise redundant) grid lines.
        
        LayoutInfoComparator comp = new LayoutInfoComparator(LayoutInfoComparator.XAXIS);
        LayoutInfo [] layoutsX = (LayoutInfo []) layouts.clone();
        LayoutInfo [] layoutsY = (LayoutInfo []) layouts.clone();
        Arrays.sort(layoutsX, comp);
        comp.cord = LayoutInfoComparator.YAXIS;
        Arrays.sort(layoutsY, comp);
        
        for (int i = 0; i < components.length; i++) {
            int expand = 0;
            int lastgrid = layoutsX[i].getLastGridX();
            for (int j = i + 1; j < components.length; j++) {
                if (layoutsX[j].containsGridX(lastgrid) && (layoutsX[j].getLastGridX() > lastgrid) 
                    && (layoutsX[i].gridwidth >= layoutsX[j].gridwidth)
                    && (expand < layoutsX[i].gridwidth - layoutsX[j].gridwidth + 1)) {
                    expand = layoutsX[i].gridwidth - layoutsX[j].gridwidth + 1;
                }
            }
            if (expand > 0) {
                for (int j = i + 1; j < components.length; j++) {
                    if (layoutsX[j].containsGridX(lastgrid) && layoutsX[j].getLastGridX() > lastgrid) {
                        layoutsX[j].expandGridWidth(expand);
                    } else if (layoutsX[j].gridx > lastgrid) {
                        layoutsX[j].moveGridX(expand);
                    }
                }
            }
            
            expand = 0;
            lastgrid = layoutsY[i].getLastGridY();
            for (int j = i + 1; j < components.length; j++) {
                if (layoutsY[j].containsGridY(lastgrid) && (layoutsY[j].getLastGridY() > lastgrid)
                    && (layoutsY[i].gridheight >= layoutsY[j].gridheight)
                    && (expand < layoutsY[i].gridheight - layoutsY[j].gridheight + 1)) {
                    expand = layoutsY[i].gridheight - layoutsY[j].gridheight + 1;
                }
            }
            if (expand > 0) {
                for (int j = i + 1; j < components.length; j++) {
                    if (layoutsY[j].containsGridY(lastgrid) && layoutsY[j].getLastGridY() > lastgrid) {
                        layoutsY[j].expandGridHeight(expand);
                    } else if(layoutsY[j].gridy > lastgrid) {
                        layoutsY[j].moveGridY(expand);
                    }
                }
            }
        }
	
        // Generate constraints
        for (int i=0; i < components.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = layouts[i].gridx;
            gbc.gridy = layouts[i].gridy;
            gbc.gridwidth = layouts[i].gridwidth;
            gbc.gridheight = layouts[i].gridheight;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            
            gbc.insets = new java.awt.Insets(layouts[i].top , layouts[i].left,
            layouts[i].bottom, layouts[i].right);
            
            if (components[i].getClass().getName().equals("javax.swing.JScrollPane")) { // NOI18N
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.fill = java.awt.GridBagConstraints.BOTH;
            }
            
            Rectangle bounds = components[i].getBounds();
            Dimension minsize = components[i].getMinimumSize();
            Dimension prefsize = components[i].getPreferredSize();
            
            // issue 4617682
            int factor = (System.getProperty("java.version").startsWith("1.4")) ? 1 : 2; // NOI18N
            
            if (bounds.width > minsize.width)
                gbc.ipadx = (bounds.width - minsize.width)/factor;
            else if (bounds.width < prefsize.width)
                gbc.ipadx = (bounds.width - prefsize.width)/factor;
            if (bounds.height > minsize.height)
                gbc.ipady = (bounds.height - minsize.height)/factor;
            else if (bounds.height < prefsize.height)
                gbc.ipady = (bounds.height - prefsize.height)/factor;
            
            currentConstraints[i] = new GridBagLayoutConstraints(gbc);
        }
    }
    
    private static boolean isOverlapped(int border1, int border2,
                                        int compPos1, int compPos2)
    {
        return compPos2 >= border1 && compPos1 < border2;
    }
    
    private static void insertLines(int line, java.util.List lines) {
        if (line < 0)
            line = 0;
        for (int i=0; i < lines.size(); i++) {
            int ival = ((Integer)lines.get(i)).intValue();
            if (line < ival) {
                lines.add(i, new Integer(line));
                return;
            }
            else if (line == ival)
                return;
        }
        lines.add(new Integer(line));
    }
    
    /**
     * Comparator of <code>LayoutInfo</code> objects.
     */
    private static class LayoutInfoComparator implements java.util.Comparator{
        final static int XAXIS = 0;
        final static int YAXIS = 1;
        int cord;
        
        public LayoutInfoComparator(int cord){
            this.cord = cord;
        }
        
        public int compare(Object left, Object right) {
            LayoutInfo layoutleft = (LayoutInfo) left;
            LayoutInfo layoutright = (LayoutInfo) right;
            
            if ((left == null) || (right == null)) return 0;
            if (cord == XAXIS) {
                return layoutleft.getLastGridX() - layoutright.getLastGridX();
            } else {
                return layoutleft.getLastGridY() - layoutright.getLastGridY();
            }
        }
        
    }
 
    /**
     * Layout information for one component.
     */
    private static class LayoutInfo {
        /** Grid coordinates. */
        int gridx, gridy;
        /** Grid width. */
        int gridwidth;
        /** Grid height. */
        int gridheight;
        /** Insets. */
        int top = 0, left = 0, bottom = 0, right = 0;
        
        void setLeft(int left){
            if(left < 0) left = 0;
            this.left = left;
        }
        
        void setTop(int top){
            if(top < 0) top = 0;
            this.top = top;
        }
        
        void setBottom(int bottom){
            if(bottom < 0) bottom = 0;
            this.bottom = bottom;
        }
        
        void setRight(int right){
            if(right < 0) right = 0;
            this.right = right;
        }
        
        void moveGridX(int diff) {
            gridx += diff;
        }
        
        void moveGridY(int diff) {
            gridy += diff;
        }
        
        void expandGridWidth(int diff) {
            gridwidth += diff;
        }
        
        void expandGridHeight(int diff) {
            gridheight += diff;
        }
        
        void incGridWidth(int gridx) {
            if (gridwidth == 0)
                this.gridx = gridx;
            gridwidth++;
        }
        
        void incGridHeight(int gridy) {
            if (gridheight == 0)
                this.gridy = gridy;
            gridheight++;
        }
        
        boolean containsGridX(int grid){
            return ((grid >= gridx) && (grid < gridx + gridwidth));
        }
        
        boolean containsGridY(int grid){
            return ((grid >= gridy) && (grid < gridy + gridheight));
        }
	
        int getLastGridX(){
            return gridx + gridwidth - 1;
        }
        
        int getLastGridY(){
            return gridy + gridheight - 1;
        }
        
    }

    // --------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code (GridBagConstraints in this case).
     * @param constrExp CodeExpression object of the constraints (taken from
     *        add method in the code)
     * @param constrCode CodeGroup to be filled with the relevant constraints
     *        initialization code
     * @param compExp CodeExpression of the component for which the constraints
     *        are read (not needed here)
     * @return LayoutConstraints based on information read form code
     */
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        GridBagLayoutConstraints constr = new GridBagLayoutConstraints();
        // reading is done in GridBagLayoutConstraints
        constr.readCodeExpression(constrExp, constrCode);
        return constr;
    }

    /** Called from createComponentCode method, creates code for a component
     * layout constraints (opposite to readConstraintsCode).
     * @param constrCode CodeGroup to be filled with constraints code
     * @param constr layout constraints metaobject representing the constraints
     * @param compExp CodeExpression object representing the component; not
     *        needed here
     * @return created CodeExpression representing the layout constraints
     */
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof GridBagLayoutConstraints))
            return null;

        // the code creation is done in GridBagLayoutConstraints
        return ((GridBagLayoutConstraints)constr).createCodeExpression(
                                            getCodeStructure(), constrCode);
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new GridBagLayoutConstraints();
    }

    // -----------------

    /** LayoutConstraints implementation class for GridBagConstraints.
     * GridBagConstraints class is special in that it requires more code
     * statements for initialization (setting up the individual fields).
     *
     * There are two possible code variants: simple and complex.
     * In the simple situation, no parameter of GridBagConstraints is set, so
     * the code looks like:
     *   container.add(component, new GridBagConstraints());
     *
     * In the complex situation, there are some parameters set - this requires
     * additional code statement for each parameter, and also a variable to
     * be used for the constraints object. Then the code looks like:
     *   GridBagConstraints gridBagConstraints;
     *   ...
     *   gridBagConstraints = new GridBagConstraints();
     *   gridBagConstraints.gridx = 1;
     *   gridBagConstraints.gridy = 2;
     *   container.add(component, gridBagConstraints);
     */
    public static class GridBagLayoutConstraints implements LayoutConstraints {
        private GridBagConstraints constraints;

        private GridBagConstraints defaultConstraints = new GridBagConstraints();

        private Property[] properties;

        private CodeExpression constraintsExpression;
        private CodeGroup constraintsCode; // set of all relevant statements
        private CodeStatement[] propertyStatements; // statements for properties

        private static Constructor constrConstructor;

        private static final int variableType = CodeVariable.LOCAL
                                         | CodeVariable.EXPLICIT_DECLARATION;
        private static final int variableMask = CodeVariable.SCOPE_MASK
                                         | CodeVariable.DECLARATION_MASK;
        private static final String defaultVariableName = "gridBagConstraints"; // NOI18N

        public GridBagLayoutConstraints() {
            constraints = new GridBagConstraints();
        }

        public GridBagLayoutConstraints(GridBagConstraints constraints) {
            this.constraints = constraints;
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                createProperties();
                reinstateProperties();
            }
            return properties;
        }

        public Object getConstraintsObject() {
            return constraints;
        }

        public LayoutConstraints cloneConstraints() {
            return new GridBagLayoutConstraints((GridBagConstraints)
                                                constraints.clone());
        }

        // -------

        /** This method creates code expression for the constraints. It's
         * called from the delegate's createConstraintsCode method.
         * @param codeStructure CodeStructure in which the expression will be
         *        created
         * @param constrCode CodeGroup to be filled with all the initialization
         *        statements
         * @return CodeExpression representing the constraints
         */
        private CodeExpression createCodeExpression(CodeStructure codeStructure,
                                                    CodeGroup constrCode)
        {
            this.constraintsCode = constrCode;
            propertyStatements = null;

            // GridBagConstraints is created by a simple constructor...
            constraintsExpression = codeStructure.createExpression(
                                        getConstraintsConstructor(),
                                        CodeStructure.EMPTY_PARAMS);
            // ...but the additionlly it requires to create the initialization
            // code statements
            updateCodeExpression();

            return constraintsExpression;
        }

        /** This method reads CodeExpression object representing the
         * constraints and also all its initialization statements which are
         * mapped to the constraints properties. It's called from the
         * delegate's readConstraintsCode method.
         * @param constrExp CodeExpression of the constraints
         * @param constrCode CodeGroup to be filled with recognize
         *        initialization statements
         */
        private void readCodeExpression(CodeExpression constrExp,
                                        CodeGroup constrCode)
        {
            constraintsExpression = constrExp;
            constraintsCode = constrCode;
            propertyStatements = null;

//            constrExp.setOrigin(CodeStructure.createOrigin(
//                                        getConstraintsConstructor(),
//                                        CodeStructure.EMPTY_PARAMS));

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;

            Iterator it = CodeStructure.getDefinedStatementsIterator(constrExp);
            while (it.hasNext()) {
                // go through all the statements of constraints code expression
                CodeStatement statement = (CodeStatement) it.next();
                for (int j=0; j < properties.length; j++) {
                    Property prop = properties[j];
                    if (prop.field.equals(statement.getMetaObject())) {
                        // this statement represents a GridBagConstraints field
                        // assignment, we map the corresponding property to it
                        FormCodeSupport.readPropertyStatement(
                                            statement, prop, false);
                        setPropertyStatement(j, statement);
                        if (prop.isChanged()) { // this is a non-default value
                            constrCode.addStatement(statement);
                            isAnyChanged = true;
                        }
                        break;
                    }
                }
            }

            setupVariable(isAnyChanged);
        }

        /** This method updates the constraints code according to the
         * properties. This is called at the beginning - when the constraints
         * code expression is created - and then after each change of the
         * constraints properties. This keeps the code consistent with the
         * properties.
         */
        private void updateCodeExpression() {
            if (constraintsCode == null || constraintsExpression == null)
                return;

            constraintsCode.removeAll();

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;
            for (int i=0; i < properties.length; i++)
                // for each changed property, add the corresponding statement
                // to the code (constraintsCode - instance of CodeGroup)
                if (properties[i].isChanged()) {
                    constraintsCode.addStatement(getPropertyStatement(i));
                    isAnyChanged = true;
                }

            setupVariable(isAnyChanged);
        }

        /** This method returns the code statement corresponding to property
         * of given index. The statement is created if it does not exist yet.
         * @param index index of required statement
         */
        private CodeStatement getPropertyStatement(int index) {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];

            CodeStatement propStatement = propertyStatements[index];
            if (propStatement == null) {
                CodeExpression propExp =
                    constraintsExpression.getCodeStructure().createExpression(
                        FormCodeSupport.createOrigin(properties[index]));

                // statement is field assignment; the property code expression
                // represents the assigned value
                propStatement = CodeStructure.createStatement(
                                    constraintsExpression,
                                    properties[index].field,
                                    propExp);

                propertyStatements[index] = propStatement;
            }
            return propStatement;
        }

        /** Sets the code statement read form code for given property index.
         * @param index index of the corresponding property
         * @param propStatement CodeStatement to be set
         */
        private void setPropertyStatement(int index,
                                          CodeStatement propStatement)
        {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];
            propertyStatements[index] = propStatement;
        }

        /** This method sets up the variable for constraints code expression.
         * The variable is needed only there's some property change (i.e.
         * there's some statement in which the variable would be used). Once
         * the variable is created, it's used for all the GridBagConstraints
         * in the form.
         */
        private void setupVariable(boolean anyChangedProperty) {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();
            CodeVariable var = constraintsExpression.getVariable();

            if (anyChangedProperty) { // there should be a variable
                if (var == null) { // no variable currently used
                    var = findVariable(); // find and reuse variable
                    if (var == null) { // create a new variable
                        var = codeStructure.createVariableForExpression(
                                                constraintsExpression,
                                                variableType,
                                                defaultVariableName);
                    }
                    else { // attach the constraints expression to the variable
                        codeStructure.attachExpressionToVariable(
                                          constraintsExpression, var);
                    }
                }
                // add variable assignment code
                constraintsCode.addStatement(
                                  0, var.getAssignment(constraintsExpression));
            }
            else { // no variable needed
                codeStructure.removeExpressionFromVariable(
                                  constraintsExpression);
            }
        }

        private CodeVariable findVariable() {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();

            // first try "gridBagConstraints" name - this succeeds in most
            // cases (unless the name is used elsewhere or not created yet)
            CodeVariable var = codeStructure.getVariable(defaultVariableName);
            if (var != null
                    && (var.getType() & variableMask) == variableType
                    && GridBagConstraints.class.equals(var.getDeclaredType()))
                return var;

            // try to find variable of corresponding type (time expensive)
            Iterator it = codeStructure.getVariablesIterator(
                                            variableType,
                                            variableMask,
                                            GridBagConstraints.class);
            while (it.hasNext()) {
                var = (CodeVariable) it.next();
                if (var.getName().startsWith(defaultVariableName))
                    return var;
            }

            return null;
        }

        private void createProperties() {
            properties = new Property[] {
                new Property("gridx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridx"), // NOI18N
                             getBundle().getString("HINT_gridx"), // NOI18N
                             GridPosEditor.class),

                new Property("gridy", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridy"), // NOI18N
                             getBundle().getString("HINT_gridy"), // NOI18N
                             GridPosEditor.class),

                new Property("gridwidth", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridwidth"), // NOI18N
                             getBundle().getString("HINT_gridwidth"), // NOI18N
                             GridSizeEditor.class),

                new Property("gridheight", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridheight"), // NOI18N
                             getBundle().getString("HINT_gridheight"), // NOI18N
                             GridSizeEditor.class),

                new Property("fill", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_fill"), // NOI18N
                             getBundle().getString("HINT_fill"), // NOI18N
                             FillEditor.class),

                new Property("ipadx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_ipadx"), // NOI18N
                             getBundle().getString("HINT_ipadx"), // NOI18N
                             null),

                new Property("ipady", // NOI18N
                              Integer.TYPE,
                              getBundle().getString("PROP_ipady"), // NOI18N
                              getBundle().getString("HINT_ipady"), // NOI18N
                              null),

                new Property("anchor", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_anchor"), // NOI18N
                             getBundle().getString("HINT_anchor"), // NOI18N
                             AnchorEditor.class),

                new Property("weightx", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weightx"), // NOI18N
                             getBundle().getString("HINT_weightx"), // NOI18N
                             null),

                new Property("weighty", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weighty"), // NOI18N
                             getBundle().getString("HINT_weighty"), // NOI18N
                             null),

                new Property("insets", // NOI18N
                             Insets.class,
                             getBundle().getString("PROP_insets"), // NOI18N
                             getBundle().getString("HINT_insets"), // NOI18N
                             null)
            };

            // properties with editable combo box
            properties[0].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[1].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[2].setValue("canEditAsText", Boolean.TRUE); // NOI18N
            properties[3].setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }

        private void reinstateProperties() {
            try {
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = (FormProperty) properties[i];
                    prop.reinstateProperty();
                }
            }
            catch(IllegalAccessException e1) {} // should not happen
            catch(InvocationTargetException e2) {} // should not happen
        }

        private static Constructor getConstraintsConstructor() {
            if (constrConstructor == null) {
                try {
                    constrConstructor =
                        GridBagConstraints.class.getConstructor(new Class[0]);
                }
                catch (NoSuchMethodException ex) { // should not happen
                    ex.printStackTrace();
                }
            }
            return constrConstructor;
        }

        // ---------

        /** Property implementation for GridBagLayoutConstraints. Each property
         * is tied to one field of GridBagConstraints. After a change in
         * property, updateCodeExpression is called to reflect the change in
         * the code.
         */
        private final class Property extends FormProperty {
            private Field field;
            private Class propertyEditorClass;

            Property(String name, Class type,
                     String displayName, String shortDescription,
                     Class propertyEditorClass)
            {
                super("GridBagLayoutConstraints "+name, type, // NOI18N
                      displayName, shortDescription);
                this.propertyEditorClass = propertyEditorClass;
                try {
                    field = GridBagConstraints.class.getField(name);
                }
                catch (NoSuchFieldException ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            public Object getTargetValue() {
                try {
                    return field.get(constraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public void setTargetValue(Object value) {
                try {
                    field.set(constraints, value);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            public boolean supportsDefaultValue () {
                return true;
            }

            public Object getDefaultValue() {
                try {
                    return field.get(defaultConstraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public PropertyEditor getExpliciteEditor() {
                if (propertyEditorClass == null)
                    return null;
                try {
                    return (PropertyEditor) propertyEditorClass.newInstance();
                }
                catch (Exception ex) { //should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            protected void propertyValueChanged(Object old, Object current) {
                // #36932 - GridBagLayout allows max. 512 grid size
                if (current instanceof Integer) {
                    int n = ((Integer)current).intValue();
                    String name = getName();
                    if (((name.endsWith("gridx") || name.endsWith("gridwidth")) // NOI18N
                         && constraints.gridx + constraints.gridwidth > 512)
                     || ((name.endsWith("gridy") || name.endsWith("gridheight")) // NOI18N
                         && constraints.gridy + constraints.gridheight > 512))
                    {
                        boolean fire = isChangeFiring();
                        setChangeFiring(false);
                        try {
                            setValue(old);
                        }
                        catch (Exception ex) {} // should not happen
                        setChangeFiring(fire);
                        return;
                    }
                }

                if (isChangeFiring())
                    updateCodeExpression();
                super.propertyValueChanged(old, current);
            }

            public void setPropertyContext(
                org.netbeans.modules.form.FormPropertyContext ctx)
            { // disabling this method due to limited persistence
            } // capabilities (compatibility with previous versions)
        }
    }

    // ------------
    // property editors for properties of GridBagLayoutConstraints

    private abstract static class GridBagConstrEditor extends PropertyEditorSupport {
        String[] tags;
        Integer[] values;
        String[] javaInitStrings;
        boolean otherValuesAllowed;

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return otherValuesAllowed && value != null ?
                       value.toString() : null;
        }

        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

            if (otherValuesAllowed)
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } 
                catch (NumberFormatException e) {} // ignore
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];

            if (!otherValuesAllowed)
                return javaInitStrings[0];
            return value != null ? value.toString() : null;
        }
    }

    public static final class GridPosEditor extends GridBagConstrEditor {

        public GridPosEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE" // NOI18N
            };
            otherValuesAllowed = true;
        }
    }

    public static final class GridSizeEditor extends GridBagConstrEditor {

        public GridSizeEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative"), // NOI18N
                getBundle().getString("VALUE_remainder") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE),
                new Integer(GridBagConstraints.REMAINDER)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE", // NOI18N
                "java.awt.GridBagConstraints.REMAINDER" // NOI18N
            };
            otherValuesAllowed = true;
        }
    }

    public static final class FillEditor extends GridBagConstrEditor {
        public FillEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_fill_none"), // NOI18N
                getBundle().getString("VALUE_fill_horizontal"), // NOI18N
                getBundle().getString("VALUE_fill_vertical"), // NOI18N
                getBundle().getString("VALUE_fill_both") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.NONE),
                new Integer(GridBagConstraints.HORIZONTAL),
                new Integer(GridBagConstraints.VERTICAL),
                new Integer(GridBagConstraints.BOTH)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.NONE", // NOI18N
                "java.awt.GridBagConstraints.HORIZONTAL", // NOI18N
                "java.awt.GridBagConstraints.VERTICAL", // NOI18N
                "java.awt.GridBagConstraints.BOTH" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    public static final class AnchorEditor extends GridBagConstrEditor {
        public AnchorEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_anchor_center"), // NOI18N
                getBundle().getString("VALUE_anchor_north"), // NOI18N
                getBundle().getString("VALUE_anchor_northeast"), // NOI18N
                getBundle().getString("VALUE_anchor_east"), // NOI18N
                getBundle().getString("VALUE_anchor_southeast"), // NOI18N
                getBundle().getString("VALUE_anchor_south"), // NOI18N
                getBundle().getString("VALUE_anchor_southwest"), // NOI18N
                getBundle().getString("VALUE_anchor_west"), // NOI18N
                getBundle().getString("VALUE_anchor_northwest"), // NOI18N
                getBundle().getString("VALUE_anchor_pagestart"), // NOI18N
                getBundle().getString("VALUE_anchor_pageend"), // NOI18N
                getBundle().getString("VALUE_anchor_linestart"), // NOI18N
                getBundle().getString("VALUE_anchor_lineend"), // NOI18N
                getBundle().getString("VALUE_anchor_firstlinestart"), // NOI18N
                getBundle().getString("VALUE_anchor_firstlineend"), // NOI18N
                getBundle().getString("VALUE_anchor_lastlinestart"), // NOI18N
                getBundle().getString("VALUE_anchor_lastlineend") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.CENTER),
                new Integer(GridBagConstraints.NORTH),
                new Integer(GridBagConstraints.NORTHEAST),
                new Integer(GridBagConstraints.EAST),
                new Integer(GridBagConstraints.SOUTHEAST),
                new Integer(GridBagConstraints.SOUTH),
                new Integer(GridBagConstraints.SOUTHWEST),
                new Integer(GridBagConstraints.WEST),
                new Integer(GridBagConstraints.NORTHWEST),
                new Integer(GridBagConstraints.PAGE_START),
                new Integer(GridBagConstraints.PAGE_END),
                new Integer(GridBagConstraints.LINE_START),
                new Integer(GridBagConstraints.LINE_END),
                new Integer(GridBagConstraints.FIRST_LINE_START),
                new Integer(GridBagConstraints.FIRST_LINE_END),
                new Integer(GridBagConstraints.LAST_LINE_START),
                new Integer(GridBagConstraints.LAST_LINE_END)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.CENTER", // NOI18N
                "java.awt.GridBagConstraints.NORTH", // NOI18N
                "java.awt.GridBagConstraints.NORTHEAST", // NOI18N
                "java.awt.GridBagConstraints.EAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTHEAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTH", // NOI18N
                "java.awt.GridBagConstraints.SOUTHWEST", // NOI18N
                "java.awt.GridBagConstraints.WEST", // NOI18N
                "java.awt.GridBagConstraints.NORTHWEST", // NOI18N
                "java.awt.GridBagConstraints.PAGE_START", // NOI18N
                "java.awt.GridBagConstraints.PAGE_END", // NOI18N
                "java.awt.GridBagConstraints.LINE_START", // NOI18N
                "java.awt.GridBagConstraints.LINE_END", // NOI18N
                "java.awt.GridBagConstraints.FIRST_LINE_START", // NOI18N
                "java.awt.GridBagConstraints.FIRST_LINE_END", // NOI18N
                "java.awt.GridBagConstraints.LAST_LINE_START", // NOI18N
                "java.awt.GridBagConstraints.LAST_LINE_END" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    // ------
    // temporary hacks for GridBagCustomizer and GridBagControlCenter

    static ResourceBundle getBundleHack() {
        return getBundle(); // from AbstractLayoutSupport
    }

    LayoutSupportContext getLayoutSupportHack() {
        return super.getLayoutContext();
    }
}
