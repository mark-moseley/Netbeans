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

package org.netbeans.modules.form.codestructure;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class representing code structure of one form. Also manages a pool
 * of variables for code expressions, and a undo/redo queue.
 *
 * @author Tomas Pavek
 */

public class CodeStructure {

    public static final CodeExpression[] EMPTY_PARAMS = new CodeExpression[0];

    private static final int VARIABLE_CREATE = 1;
    private static final int VARIABLE_RENAME = 2;
    private static final int VARIABLE_RELEASE = 3;
    private static final int VARIABLE_ATTACH = 4;
    private static final int VARIABLE_DETACH = 5;

    private static UsingCodeObject globalUsingObject;

    private Map namesToVariables = new HashMap(50);
    private Map expressionsToVariables = new HashMap(50);

    private static int globalDefaultVariableType = CodeVariable.FIELD
                                                   | CodeVariable.PRIVATE;
    private int defaultVariableType = -1;

    private boolean undoRedoRecording = false;
    private int undoRedoMark = 0;
    private int oldestMark = 0;
    private int undoRedoHardLimit = 5000;
    private int undoRedoChangeLimit = 100;
    private Map undoMap;
    private Map redoMap;
    private Set providedMarksUndo;
    private Set providedMarksRedo;

    // --------
    // constructor

    public CodeStructure(boolean startUndoRedoRecording) {
        if (startUndoRedoRecording)
            setUndoRedoRecording(true);
    }

    // -------
    // expressions

    /** Creates a new expression based on a constructor. */
    public CodeExpression createExpression(Constructor ctor,
                                           CodeExpression[] params)
    {
        CodeExpressionOrigin origin =
                            new CodeSupport.ConstructorOrigin(ctor, params);
        return new DefaultCodeExpression(this, origin);
    }

    /** Creates a new expression based on a method. */
    public CodeExpression createExpression(CodeExpression parent,
                                           Method method,
                                           CodeExpression[] params)
    {
        CodeExpressionOrigin origin = new CodeSupport.MethodOrigin(
                                                      parent, method, params);
        return new DefaultCodeExpression(this, origin);
    }

    /** Creates a new expression based on a field. */
    public CodeExpression createExpression(CodeExpression parent, Field field) {
        CodeExpressionOrigin origin = new CodeSupport.FieldOrigin(parent, field);
        return new DefaultCodeExpression(this, origin);
    }

    /** Creates a new expression from based on a value. */
    public CodeExpression createExpression(Class type,
                                           Object value,
                                           String javaInitStr)
    {
        return new DefaultCodeExpression(this, new CodeSupport.ValueOrigin(
                                                    type, value, javaInitStr));
    }

    /** Creates a new expression of an arbitrary origin. /*/
    public CodeExpression createExpression(CodeExpressionOrigin origin) {
        return new DefaultCodeExpression(this, origin);
    }

    /** Creates an expression representing null value. */
    public CodeExpression createNullExpression(Class type) {
        return new DefaultCodeExpression(this, new CodeSupport.ValueOrigin(
                                                    type, null, "null")); // NOI18N
    }

    /** Creates an expression with no origin. The origin must be set
     * explicitly before the expression is used. */
    public CodeExpression createDefaultExpression() {
        return new DefaultCodeExpression(this);
    }

    /** Prevents an expression from being removed automatically from structure
     * when no more used (by any UsingCodeObject). */
    public void registerExpression(CodeExpression expression) {
        if (globalUsingObject == null)
            globalUsingObject = new GlobalUsingObject();

        expression.addUsingObject(globalUsingObject,
                                  UsedCodeObject.USING,
                                  CodeStructure.class);
    }

    /** Removes an expression from the structure completely. */
    public static void removeExpression(CodeExpression expression) {
        unregisterUsedCodeObject(expression);
        unregisterUsingCodeObject(expression);

        expression.getCodeStructure().removeExpressionFromVariable(expression);
    }

    /** Filters out expressions whose origin uses given or equal meta object.
     * Passed expressions are returned in an array. */
    public static CodeExpression[] filterExpressions(Iterator it,
                                                     Object originMetaObject)
    {
        List list = new ArrayList();
        while (it.hasNext()) {
            CodeExpression exp = (CodeExpression) it.next();
            if (originMetaObject.equals(exp.getOrigin().getMetaObject()))
                list.add(exp);
        }
        return (CodeExpression[]) list.toArray(new CodeExpression[list.size()]);
    }

    // --------
    // statements

    /** Creates a new method statement. */
    public static CodeStatement createStatement(CodeExpression expression,
                                                Method m,
                                                CodeExpression[] params)
    {
        CodeStatement statement = new CodeSupport.MethodStatement(
                                                      expression, m, params);
        registerUsingCodeObject(statement);
        return statement;
    }

    /** Creates a new field statement. */
    public static CodeStatement createStatement(CodeExpression expression,
                                                Field f,
                                                CodeExpression assignExp)
    {
        CodeStatement statement = new CodeSupport.FieldStatement(
                                                    expression, f, assignExp);
        registerUsingCodeObject(statement);
        return statement;
    }

    /** Removes a statement from the structure completely. */
    public static void removeStatement(CodeStatement statement) {
        unregisterUsingCodeObject(statement);
    }

    /** Removes all statements provided by an Iterator. */
    public static void removeStatements(Iterator it) {
        List list = new ArrayList();
        while (it.hasNext())
            list.add(it.next());

        for (int i=0, n=list.size(); i < n; i++)
            unregisterUsingCodeObject((CodeStatement) list.get(i));
    }

    /** Filters out statements using given or equal meta object. Passed
     * statements are returned in an array. */
    public static CodeStatement[] filterStatements(Iterator it,
                                                   Object metaObject)
    {
        List list = new ArrayList();
        while (it.hasNext()) {
            CodeStatement statement = (CodeStatement) it.next();
            if (metaObject.equals(statement.getMetaObject()))
                list.add(statement);
        }
        return (CodeStatement[]) list.toArray(new CodeStatement[list.size()]);
    }

    // --------
    // statements code group

    /** Creates a default group of statements. */
    public CodeGroup createCodeGroup() {
        return new CodeSupport.DefaultCodeGroup();
    }

    // --------
    // origins

    /** Creates an expression origin from a constructor. */
    public static CodeExpressionOrigin createOrigin(Constructor ctor,
                                                    CodeExpression[] params)
    {
        return new CodeSupport.ConstructorOrigin(ctor, params);
    }

    /** Creates an expression origin from a method. */
    public static CodeExpressionOrigin createOrigin(CodeExpression parent,
                                                    Method m,
                                                    CodeExpression[] params)
    {
        return new CodeSupport.MethodOrigin(parent, m, params);
    }

    /** Creates an expression origin from a field. */
    public static CodeExpressionOrigin createOrigin(CodeExpression parent,
                                                    Field f)
    {
        return new CodeSupport.FieldOrigin(parent, f);
    }

    /** Creates an expression origin from a value (and provided java string). */
    public static CodeExpressionOrigin createOrigin(Class type,
                                                    Object value,
                                                    String javaStr)
    {
        return new CodeSupport.ValueOrigin(type, value, javaStr);
    }

    // -------
    // getting to expressions and statements dependent on given expression
    // (used as their parent or parameter)

    /** Returns an iterator of expressions that are defined by given
     * expression. These expressions use the given expression as the parent
     * of origin). */
    public static Iterator getDefinedExpressionsIterator(CodeExpression exp) {
        return exp.getUsingObjectsIterator(UsedCodeObject.DEFINED,
                                           CodeExpression.class);
    }

    /** Returns an iterator of exppressions that use given expression as
     * a parameter in their origin. */
    public static Iterator getUsingExpressionsIterator(CodeExpression exp) {
        return exp.getUsingObjectsIterator(UsedCodeObject.USING,
                                           CodeExpression.class);
    }

    /** Returns an iterator of statements that are defined by given
     * expression. These statements use the given expression as the parent. */
    public static Iterator getDefinedStatementsIterator(CodeExpression exp) {
        return exp.getUsingObjectsIterator(UsedCodeObject.DEFINED,
                                           CodeStatement.class);
    }

    /** Returns an iterator of statements that use given expression as
     * a parameter. */
    public static Iterator getUsingStatementsIterator(CodeExpression exp) {
        return exp.getUsingObjectsIterator(UsedCodeObject.USING,
                                           CodeStatement.class);
    }

    // -------
    // managing references between code objects

    // Registers usage of expressions used by a statement.
    static void registerUsingCodeObject(CodeStatement statement) {
        CodeExpression parent = statement.getParentExpression();
        if (parent != null)
            parent.addUsingObject(
                statement, UsedCodeObject.DEFINED, CodeStatement.class);

        CodeExpression[] params = statement.getStatementParameters();
        if (params != null)
            for (int i=0; i < params.length; i++)
                params[i].addUsingObject(
                    statement, UsedCodeObject.USING, CodeStatement.class);
    }

    // Registers usage of expressions used by the origin of an expression.
    static void registerUsingCodeObject(CodeExpression expression) {
        CodeExpressionOrigin origin = expression.getOrigin();
        CodeExpression parent = origin.getParentExpression();

        if (parent != null)
            parent.addUsingObject(expression,
                                  UsedCodeObject.DEFINED,
                                  CodeExpression.class);

        CodeExpression[] params = origin.getCreationParameters();
        if (params != null)
            for (int i=0; i < params.length; i++)
                params[i].addUsingObject(expression,
                                         UsedCodeObject.USING,
                                         CodeExpression.class);
    }

    // Unregisters usage of all objects used by a using object.
    static void unregisterUsingCodeObject(UsingCodeObject usingObject) {
        Iterator it = usingObject.getUsedObjectsIterator();
        while (it.hasNext()) {
            UsedCodeObject usedObject = (UsedCodeObject) it.next();
            if (!usedObject.removeUsingObject(usingObject)) {
                // usedObject is no more used, so it should be removed
                if (usedObject instanceof UsingCodeObject)
                    unregisterUsingCodeObject((UsingCodeObject)usedObject);
            }
        }
    }

    // Unregisters usage of just one object used by a using object.
    static void unregisterObjectUsage(UsingCodeObject usingObject,
                                      UsedCodeObject usedObject)
    {
        if (!usedObject.removeUsingObject(usingObject)) {
            // usedObject is no more used, so it should be removed
            if (usedObject instanceof UsingCodeObject)
                unregisterUsingCodeObject((UsingCodeObject)usedObject);
        }
    }

    // This method just notifies all objects using given used object that
    // the used object is removed from the structure.
    static void unregisterUsedCodeObject(UsedCodeObject usedObject) {
        List usingObjects = new ArrayList();
        Iterator it = usedObject.getUsingObjectsIterator(0, null);
        while (it.hasNext())
            usingObjects.add(it.next());

        it = usingObjects.iterator();
        while (it.hasNext()) {
            UsingCodeObject usingObject = (UsingCodeObject) it.next();
            if (!usingObject.usedObjectRemoved(usedObject)) {
                // usingObject cannot exist without removed usedObject
                if (usingObject instanceof UsedCodeObject)
                    unregisterUsedCodeObject((UsedCodeObject)usingObject);
                unregisterUsingCodeObject(usingObject);
            }
        }
    }

    private static class GlobalUsingObject implements UsingCodeObject {
        public void usageRegistered(UsedCodeObject usedObject) {
        }
        public boolean usedObjectRemoved(UsedCodeObject usedObject) {
            return true;
        }
        public UsedCodeObject getDefiningObject() {
            return null;
        }
        public Iterator getUsedObjectsIterator() {
            return null;
        }
    }

    // -------
    // variables

    /** Creates a new variable. It is empty - with no expression attached. */
    public CodeVariable createVariable(int type,
                                       Class declaredType,
                                       String name)
    {
        if (getVariable(name) != null)
            return null; // variable already exists, cannot create new one

        if (type < 0 || name == null)
            throw new IllegalArgumentException();

        Variable var = new Variable(type, declaredType, name);
        namesToVariables.put(name, var);

        if (undoRedoRecording)
            logUndoableChange(new VariableChange(VARIABLE_CREATE, var));

        return var;
    }

    /** Renames variable of name oldName to newName. */
    public boolean renameVariable(String oldName, String newName) {
        Variable var = (Variable) namesToVariables.get(oldName);
        if (var == null || newName == null
                || newName.equals(var.getName())
                || namesToVariables.get(newName) != null)
            return false;

        namesToVariables.remove(oldName);
        var.name = newName;
        namesToVariables.put(newName, var);

        if (undoRedoRecording) {
            VariableChange change = new VariableChange(VARIABLE_RENAME, var);
            change.oldName = oldName;
            change.newName = newName;
            logUndoableChange(change);
        }

        return true;
    }

    /** Releases variable of given name. */
    public CodeVariable releaseVariable(String name) {
        Variable var = (Variable) namesToVariables.remove(name);
        if (var == null)
            return null; // there is no such variable

        Map expressionsMap = var.expressionsMap;
        if (expressionsMap == null)
            return var;

        Iterator it = expressionsMap.values().iterator();
        while (it.hasNext())
            expressionsToVariables.remove(it.next());

        if (undoRedoRecording)
            logUndoableChange(new VariableChange(VARIABLE_RELEASE, var));

        return var;
    }

    /** Checks whether given name is already used by some variable. */
    public boolean isVariableNameReserved(String name) {
        return namesToVariables.get(name) != null;
    }

    /** Creates a new variable and attaches given expression to it. If the
     * requested name is already in use, then a free name is found. If null
     * is provided as the name, then expression's short class name is used. */
    public CodeVariable createVariableForExpression(CodeExpression expression,
                                                    int type,
                                                    String name)
    {
        if (expression == null)
            throw new IllegalArgumentException();

        if (getVariable(expression) != null)
            return null; // variable already exists, cannot create new one

        if (type < 0)
            throw new IllegalArgumentException();

        if (expressionsToVariables.get(expression) != null)
            removeExpressionFromVariable(expression);

        int n = 0;
        String baseName;
        if (name != null) { // a valid name provided
            baseName = name; // try it without a suffix first
        }
        else { // derive default name from class type, add "1" as suffix
            String typeName = expression.getOrigin().getType().getName();
            int i = typeName.lastIndexOf('$');
            if (i < 0)
                i = typeName.lastIndexOf('.');
            baseName = Character.toLowerCase(typeName.charAt(i+1))
                       + typeName.substring(i+2);
            name = baseName + (++n);
        }

        // find a free name
        while (namesToVariables.get(name) != null)
            name = baseName + (++n);

        Variable var = new Variable(type,
                                    expression.getOrigin().getType(),
                                    name);
        CodeStatement statement = createVariableAssignment(var, expression);
        var.addCodeExpression(expression, statement);

        namesToVariables.put(name, var);
        expressionsToVariables.put(expression, var);

        if (undoRedoRecording) {
            logUndoableChange(new VariableChange(VARIABLE_CREATE, var));
            VariableChange change = new VariableChange(VARIABLE_ATTACH, var);
            change.expression = expression;
            change.statement = statement;
            logUndoableChange(change);
        }

        return var;
    }

    /** Attaches an expression to a variable. The variable will be used in the
     * code instead of the expression. */
    public void attachExpressionToVariable(CodeExpression expression,
                                           CodeVariable variable)
    {
        if (expression == null)
            return;
        // [should we check also expression type ??]

        if (variable.getAssignment(expression) != null)
            return; // expression already attached

        // check if this variable can have multiple expressions attached
        int mask = CodeVariable.LOCAL
                   | CodeVariable.EXPLICIT_DECLARATION;
        if ((variable.getType() & mask) == CodeVariable.LOCAL
             && variable.getAttachedExpressions().size() > 0)
        {   // local variable without a standalone declaration cannot be used
            // for multiple expressions
            throw new IllegalStateException(
                      "Standalone local variable declaration required for: " // NOI18N
                      + variable.getName());
        }

        Variable prevVar = (Variable) expressionsToVariables.get(expression);
        if (prevVar != null && prevVar != variable)
            removeExpressionFromVariable(expression);

        Variable var = (Variable) variable;
        CodeStatement statement = createVariableAssignment(var, expression);

        var.addCodeExpression(expression, statement);
        expressionsToVariables.put(expression, var);

        if (undoRedoRecording) {
            VariableChange change = new VariableChange(VARIABLE_ATTACH, var);
            change.expression = expression;
            change.statement = statement;
            logUndoableChange(change);
        }
    }

    /** Releases an expression from using a variable. */
    public void removeExpressionFromVariable(CodeExpression expression) {
        if (expression == null)
            return;

        Variable var = (Variable) expressionsToVariables.remove(expression);
        if (var == null)
            return;

        CodeStatement statement = var.removeCodeExpression(expression);

        if (undoRedoRecording) {
            VariableChange change = new VariableChange(VARIABLE_DETACH, var);
            change.expression = expression;
            change.statement = statement;
            logUndoableChange(change);
        }

        if (var.expressionsMap.isEmpty() 
                && (var.getType() & CodeVariable.EXPLICIT_RELEASE) == 0)
            // release unused variable
            releaseVariable(var.getName());
    }

    /** Returns variable of given name. */
    public CodeVariable getVariable(String name) {
        return (Variable) namesToVariables.get(name);
    }

    /** Returns variable of an expression. */
    public CodeVariable getVariable(CodeExpression expression) {
        return (Variable) expressionsToVariables.get(expression);
    }

    /** Returns an iterator of variables of given criterions. */
    public Iterator getVariablesIterator(int type, int typeMask,
                                         Class declaredType)
    {
        return new VariablesIterator(type, typeMask, declaredType);
    }

    /** Returns all variables in this CodeStructure. */
    public Collection getAllVariables() {
        return Collections.unmodifiableCollection(namesToVariables.values());
    }

    // ---------

    /** WARNING: This method will be removed in full two-way editing
     *           implementation. DO NOT USE! */
    public static void setGlobalDefaultVariableType(int type) {
        if (type < 0) {
            globalDefaultVariableType = CodeVariable.FIELD
                                        | CodeVariable.PRIVATE;
        }
        else {
            type &= CodeVariable.ALL_MASK;
            if ((type & CodeVariable.SCOPE_MASK) == CodeVariable.NO_VARIABLE)
                type |= CodeVariable.FIELD;
            int fdMask = CodeVariable.EXPLICIT_DECLARATION | CodeVariable.FINAL;
            if ((type & fdMask) == fdMask)
                type &= ~CodeVariable.EXPLICIT_DECLARATION;

            globalDefaultVariableType = type;
        }
    }

    /** WARNING: This method will be removed in full two-way editing
     *           implementation. DO NOT USE! */
    public void setDefaultVariableType(int type) {
        if (type < 0) {
            defaultVariableType = -1; // global default will be used
        }
        else {
            type &= CodeVariable.ALL_MASK;
            if ((type & CodeVariable.SCOPE_MASK) == CodeVariable.NO_VARIABLE)
                type |= CodeVariable.FIELD;
            int fdMask = CodeVariable.EXPLICIT_DECLARATION | CodeVariable.FINAL;
            if ((type & fdMask) == fdMask)
                type &= ~CodeVariable.EXPLICIT_DECLARATION;

            defaultVariableType = type;
        }
    }

    static int getGlobalDefaultVariableType() {
        return globalDefaultVariableType;
    }

    int getDefaultVariableType() {
        return defaultVariableType > -1 ?
               defaultVariableType : globalDefaultVariableType;
    }

    // ---------

    protected Map getNamesToVariablesMap() {
        return namesToVariables;
    }

    protected Map getExpressionsToVariables() {
        return expressionsToVariables;
    }

    private CodeStatement createVariableAssignment(CodeVariable var,
                                                   CodeExpression expression)
    {
        CodeStatement statement =
            new CodeSupport.AssignVariableStatement(var, expression);

        // important: assignment statement does not register usage of code
        // expressions (assigned expression, parameters) - so it does not hold
        // the expressions in the structure

        return statement;
    }

    // --------
    // undo/redo processing

    public void setUndoRedoRecording(boolean record) {
        undoRedoRecording = record;
        if (record && undoMap == null) {
            undoMap = new HashMap(undoRedoChangeLimit*5);
            redoMap = new HashMap(undoRedoChangeLimit);
            providedMarksUndo = new HashSet(undoRedoChangeLimit);
            providedMarksRedo = new HashSet(undoRedoChangeLimit);
        }
    }

    public boolean isUndoRedoRecording() {
        return undoRedoRecording;
    }

    void logUndoableChange(CodeStructureChange change) {
        undoRedoMark += redoMap.size(); // to ensure unique marks
        redoMap.clear();
        providedMarksRedo.clear();

        if (undoMap.size() == 0)
            oldestMark = undoRedoMark;

        t("adding undoable change "+undoRedoMark); // NOI18N

        undoMap.put(new Integer(undoRedoMark++), change);

        while (undoMap.size() > undoRedoHardLimit) {
            Object mark = new Integer(oldestMark++);
            undoMap.remove(mark);
            providedMarksUndo.remove(mark);
        }
    }

    public Object markForUndo() {
        undoRedoMark += redoMap.size(); // to ensure unique marks
        redoMap.clear();
        providedMarksRedo.clear();

        Object newMark = new Integer(undoRedoMark);
        if (!providedMarksUndo.contains(newMark)) {
            providedMarksUndo.add(newMark);
            if (providedMarksUndo.size() > undoRedoChangeLimit) {
                while (true) {
                    Object mark = new Integer(oldestMark);
                    if (providedMarksUndo.size() > undoRedoChangeLimit) {
                        undoMap.remove(mark);
                        providedMarksUndo.remove(mark);
                    }
                    else if (!providedMarksUndo.contains(mark))
                        undoMap.remove(mark);
                    else break;

                    oldestMark++;
                }
            }
        }

        t("marked for undo: "+newMark); // NOI18N
        return newMark;
    }

    public boolean undoToMark(Object mark) {
        int lastMark = ((Integer)mark).intValue();
        if (undoRedoMark <= lastMark)
            return false;

        t("undo to mark "+mark); // NOI18N

        boolean undoRedoOn = undoRedoRecording;
        undoRedoRecording = false;

        while (undoRedoMark > lastMark) {
            Object key = new Integer(--undoRedoMark);
            CodeStructureChange change = (CodeStructureChange)
                                         undoMap.remove(key);
            if (change != null) {
                change.undo();
                redoMap.put(key, change);

                t("undone: "+key);
            }

            if (providedMarksUndo.remove(key))
                providedMarksRedo.add(key);
        }

        if (undoRedoOn)
            undoRedoRecording = true;

        return true;
    }

    public boolean redoToMark(Object mark) {
        int toMark = ((Integer)mark).intValue();
        if (undoRedoMark >= toMark)
            return false;

        t("redo to mark "+mark); // NOI18N

        boolean undoRedoOn = undoRedoRecording;
        undoRedoRecording = false;

        while (undoRedoMark < toMark) {
            Object key = new Integer(undoRedoMark++);
            CodeStructureChange change = (CodeStructureChange)
                                         redoMap.remove(key);
            if (change != null) {
                change.redo();
                undoMap.put(key, change);

                t("redone: "+key);
            }

            if (providedMarksRedo.remove(key))
                providedMarksUndo.add(key);
        }

        if (undoRedoOn)
            undoRedoRecording = true;

        return true;
    }

    // --------
    // inner classes

    final class Variable implements CodeVariable {
        private int type;
        private Class declaredType;
        private String name;
        private Map expressionsMap;
        private CodeStatement declarationStatement;

        Variable(int type, Class declaredType, String name) {
            if ((type & FINAL) != 0)
                type &= ~EXPLICIT_DECLARATION;
            this.type = type;
            this.declaredType = declaredType;
            this.name = name;
        }

        public int getType() {
            return (type & DEFAULT_TYPE) != DEFAULT_TYPE ?
                   type : getDefaultVariableType();
        }

        public String getName() {
            return name;
        }

        public Class getDeclaredType() {
            return declaredType;
        }

        public Collection getAttachedExpressions() {
            return expressionsMap != null ?
                     Collections.unmodifiableCollection(expressionsMap.keySet()) :
                     Collections.EMPTY_LIST;
        }

        public CodeStatement getDeclaration() {
            if (declarationStatement == null)
                declarationStatement =
                    new CodeSupport.DeclareVariableStatement(this);
            return declarationStatement;
        }

        public CodeStatement getAssignment(CodeExpression expression) {
            return expressionsMap != null ?
                   (CodeStatement) expressionsMap.get(expression) : null;
        }

        // -------

        void addCodeExpression(CodeExpression expression,
                               CodeStatement statement)
        {
            if (expressionsMap == null)
                expressionsMap = new HashMap();
            expressionsMap.put(expression, statement);
        }

        CodeStatement removeCodeExpression(CodeExpression expression) {
            if (expressionsMap != null)
                return (CodeStatement) expressionsMap.remove(expression);
            return null;
        }
    }

    private final class VariablesIterator implements Iterator {
        private int type;
        private int typeMask;
        private Class declaredType;

        private Iterator subIterator;

        private CodeVariable currentVar;

        public VariablesIterator(int type, int typeMask, Class declaredType) {
            this.type = type;
            this.typeMask = typeMask;
            this.declaredType = declaredType;

            subIterator = namesToVariables.values().iterator();
        }

        public boolean hasNext() {
            if (currentVar != null)
                return true;

            while (subIterator.hasNext()) {
                CodeVariable var = (CodeVariable) subIterator.next();
                if ((type < 0
                        || (type & typeMask) == (var.getType() & typeMask))
                    &&
                    (declaredType == null
                        || declaredType.equals(var.getDeclaredType())))
                {
                    currentVar = var;
                    return true;
                }
            }

            return false;
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();

            CodeVariable var = currentVar;
            currentVar = null;
            return var;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // --------

    private class VariableChange implements CodeStructureChange {
        private int changeType;
        private Variable variable;
        private CodeExpression expression;
        private CodeStatement statement;
        private String oldName;
        private String newName;

        VariableChange(int type, Variable var) {
            changeType = type;
            variable = var;
        }

        public void undo() {
            switch (changeType) {
                case VARIABLE_CREATE:
                    namesToVariables.remove(variable.name);
                    break;
                case VARIABLE_RENAME:
                    namesToVariables.remove(newName);
                    variable.name = oldName;
                    namesToVariables.put(oldName, variable);
                    break;
                case VARIABLE_RELEASE:
                    Iterator it = variable.expressionsMap.values().iterator();
                    while (it.hasNext())
                        expressionsToVariables.put(it.next(), variable);
                    namesToVariables.put(variable.name, variable);
                    break;
                case VARIABLE_ATTACH:
                    expressionsToVariables.remove(expression);
                    variable.expressionsMap.remove(expression);
                    break;
                case VARIABLE_DETACH:
                    variable.expressionsMap.put(expression, statement);
                    expressionsToVariables.put(expression, variable);
                    break;
            }
        }

        public void redo() {
            switch (changeType) {
                case VARIABLE_CREATE:
                    namesToVariables.put(variable.name, variable);
                    break;
                case VARIABLE_RENAME:
                    namesToVariables.remove(oldName);
                    variable.name = newName;
                    namesToVariables.put(newName, variable);
                    break;
                case VARIABLE_RELEASE:
                    namesToVariables.remove(variable.name);
                    Iterator it = variable.expressionsMap.values().iterator();
                    while (it.hasNext())
                        expressionsToVariables.remove(it.next());
                    break;
                case VARIABLE_ATTACH:
                    variable.expressionsMap.put(expression, statement);
                    expressionsToVariables.put(expression, variable);
                    break;
                case VARIABLE_DETACH:
                    expressionsToVariables.remove(expression);
                    variable.expressionsMap.remove(expression);
                    break;
            }
        }
    }

    // ---------------

    /** For debugging purposes only. */
    static private int traceCount = 0;
    /** For debugging purposes only. */
    static private final boolean TRACE = false;
    /** For debugging purposes only. */
    static void t(String str) {
        if (TRACE)
            if (str != null)
                System.out.println("CodeStructure "+(++traceCount)+": "+str); // NOI18N
            else
                System.out.println(""); // NOI18N
    }
}
