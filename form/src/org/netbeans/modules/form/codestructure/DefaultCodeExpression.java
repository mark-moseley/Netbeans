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

package org.netbeans.modules.form.codestructure;

import java.util.*;

/**
 * Default implementation class of CodeExpression interface. No other class
 * should implement CodeExpression or extend this class. All extensibility
 * is done through the CodeExpressionOrigin implementations. CodeExpression
 * is kept as an interface for compatibility and usability reasons.
 *
 * @author Tomas Pavek
 */

final class DefaultCodeExpression implements CodeExpression {

    private CodeStructure codeStructure;

    private CodeExpressionOrigin expressionOrigin;

    private CodeObjectUsage expressionUsage;


    public DefaultCodeExpression(CodeStructure codeStructure,
                                 CodeExpressionOrigin origin)
    {
        this.codeStructure = codeStructure;
        setOrigin(origin);
    }

    DefaultCodeExpression(CodeStructure codeStructure) {
        this.codeStructure = codeStructure;
    }

    // -------

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    public CodeVariable getVariable() {
        return codeStructure.getVariable(this);
    }

    public CodeExpressionOrigin getOrigin() {
        return expressionOrigin;
    }

    public void setOrigin(CodeExpressionOrigin newOrigin) {
        CodeExpressionOrigin oldOrigin = expressionOrigin;
        if (oldOrigin == newOrigin)
            return;

        CodeExpression registerParent = null;
        List registerParams = null;

        if (oldOrigin != null) {
            if (newOrigin != null) { // changing one origin to another
                CodeExpression oldParent = oldOrigin.getParentExpression();
                CodeExpression newParent = newOrigin.getParentExpression();
                if (oldParent != null && oldParent != newParent)
                    CodeStructure.unregisterObjectUsage(this, oldParent);
                if (newParent != null && newParent != oldParent)
                    registerParent = newParent;

                CodeExpression[] oldParams = oldOrigin.getCreationParameters();
                CodeExpression[] newParams = newOrigin.getCreationParameters();

                for (int i=0; i < oldParams.length; i++) {
                    CodeExpression oldPar = oldParams[i];
                    if (i < newParams.length && oldPar == newParams[i])
                        continue;
                    int j = 0;
                    while (j < newParams.length) {
                        if (oldPar == newParams[j])
                            break;
                        j++;
                    }
                    if (j == newParams.length)
                        CodeStructure.unregisterObjectUsage(this, oldPar);
                }

                for (int i=0; i < newParams.length; i++) {
                    CodeExpression newPar = newParams[i];
                    if (i < oldParams.length && newPar == oldParams[i])
                        continue;
                    int j = 0;
                    while (j < oldParams.length) {
                        if (newPar == oldParams[j])
                            break;
                        j++;
                    }
                    if (j == oldParams.length) {
                        if (registerParams == null)
                            registerParams = new ArrayList();
                        registerParams.add(newPar);
                    }
                }
            }
            else CodeStructure.unregisterUsingCodeObject(this);
        }

        expressionOrigin = newOrigin;

        if (codeStructure.isUndoRedoRecording())
            codeStructure.logUndoableChange(
                new OriginChange(oldOrigin, newOrigin));

        if (newOrigin != null) {
            if (oldOrigin != null) {
                if (registerParent != null)
                    registerParent.addUsingObject(
                        this, UsedCodeObject.DEFINED, CodeExpression.class);

                if (registerParams != null)
                    for (int i=0, n=registerParams.size(); i < n; i++) {
                        CodeExpression param = (CodeExpression)
                                               registerParams.get(i);
                        param.addUsingObject(this,
                                             UsedCodeObject.USING,
                                             CodeExpression.class);
                    }
            }
            else CodeStructure.registerUsingCodeObject(this);
        }
    }

    // --------
    // UsedCodeObject implementation - registering objects that use
    // this expression

    public void addUsingObject(UsingCodeObject usingObject,
                               int useType,
                               Object useCategory)
    {
        CodeStructureChange undoableChange =
                getExpressionUsage().addUsingObject(
                                       usingObject,
                                       useType,
                                       useCategory,
                                       codeStructure.isUndoRedoRecording());
        if (undoableChange != null)
            codeStructure.logUndoableChange(undoableChange);
    }

    public boolean removeUsingObject(UsingCodeObject usingObject) {
        CodeStructureChange undoableChange =
                getExpressionUsage().removeUsingObject(
                                       usingObject,
                                       codeStructure.isUndoRedoRecording());
        if (undoableChange != null)
            codeStructure.logUndoableChange(undoableChange);

        boolean stillUsed = !getExpressionUsage().isEmpty();
        if (!stillUsed) // the elment is no longer used in the structure
            codeStructure.removeExpressionFromVariable(this);
        return stillUsed;
    }

    public Iterator getUsingObjectsIterator(int useType, Object useCategory) {
        return getExpressionUsage().getUsingObjectsIterator(useType, useCategory);
    }

    private CodeObjectUsage getExpressionUsage() {
        if (expressionUsage == null)
            expressionUsage = new CodeObjectUsage(this);
        return expressionUsage;
    }

    // ---------
    // UsingCodeObject implementation - handling objects used by this expression

    // notifying about registering this object in used object
    public void usageRegistered(UsedCodeObject usedObject) {
    }

    // notifying about removing the used object from structure
    public boolean usedObjectRemoved(UsedCodeObject usedObject) {
//        if (!(usedObject instanceof CodeExpression))
//            return true;
        // an used expression was removed - this expression will be too ...
        codeStructure.removeExpressionFromVariable(this);
        return false;
    }

    public UsedCodeObject getDefiningObject() {
        return getOrigin().getParentExpression();
    }

    public Iterator getUsedObjectsIterator() {
        return new UsedObjectsIterator();
    }

    // --------

    private class OriginChange implements CodeStructureChange {
        private CodeExpressionOrigin oldOrigin;
        private CodeExpressionOrigin newOrigin;

        OriginChange(CodeExpressionOrigin oldOrigin,
                     CodeExpressionOrigin newOrigin)
        {
            this.oldOrigin = oldOrigin;
            this.newOrigin = newOrigin;
        }

        public void undo() {
            expressionOrigin = oldOrigin;
        }

        public void redo() {
            expressionOrigin = newOrigin;
        }
    }

    // --------

    private class UsedObjectsIterator implements Iterator {
        int index;
        CodeExpression[] parameters;

        UsedObjectsIterator() {
            index = getOrigin().getParentExpression() != null ? -1 : 0;
            parameters = getOrigin().getCreationParameters();
            if (parameters == null)
                parameters = CodeStructure.EMPTY_PARAMS;
        }

        public boolean hasNext() {
            return index < parameters.length;
        }

        public Object next() {
            if (!hasNext())
                throw new java.util.NoSuchElementException();

            Object obj = index > -1 ? parameters[index] :
                                      getOrigin().getParentExpression();
            index++;
            return obj;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
