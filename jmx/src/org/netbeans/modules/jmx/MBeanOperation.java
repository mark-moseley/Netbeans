/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;

/**
 *
 * @author tl156378
 */
public class MBeanOperation implements Comparable {
    
    private String name;
    private Method method;
    private boolean methodExists = false;
    private boolean wrapped = false;
    private String description;
    private String returnTypeName = "";// NOI18N
    private List<MBeanOperationParameter> parameters = null;
    private List<MBeanOperationException> exceptions = null;
    
    /** Creates a new instance of MBeanOperation */
    public MBeanOperation(Method method, String description) {
        this.method = method;
        this.description = description;
        this.name = method.getName();
        this.methodExists = true;
        this.returnTypeName = method.getType().getName();
        //inits exceptions
        List<JavaClass> exceptions = method.getExceptions();
        ArrayList exceptArray = new ArrayList();
        for (Iterator<JavaClass> it = exceptions.iterator(); it.hasNext();) {
            JavaClass excep = it.next();
            exceptArray.add(
                    new MBeanOperationException(excep.getName(), ""));// NOI18N
        }
        this.exceptions = exceptArray;
        //inits parameters
        List<Parameter> params = method.getParameters();
        ArrayList paramArray = new ArrayList();
        int i = 0;
        for (Iterator<Parameter> it = params.iterator(); it.hasNext();) {
            Parameter param = it.next();
            paramArray.add(
                    new MBeanOperationParameter("param"+ i, // NOI18N
                        param.getType().getName(), ""));// NOI18N
            i++;
        }
        this.parameters = paramArray;
        
        forceParamName(this.parameters);
    }
    
    /**
     * Constructor
     * @param operationName the name of the operation
     * @param operationReturnType the operation return type
     * @param operationParameters the parameter list of that operation
     * @param operationExceptions the exception list of that operation
     * @param operationDescription the description of that operation
     */
    public MBeanOperation(String operationName, String operationReturnType,
            List<MBeanOperationParameter> operationParameters,
            List<MBeanOperationException> operationExceptions,
            String operationDescription) {
        
        this.name = operationName;
        this.returnTypeName = operationReturnType;
        if (operationParameters == null)
            this.parameters = new ArrayList();
        else
            this.parameters = operationParameters;
        if (operationExceptions == null)
            this.exceptions = new ArrayList();
        else
            this.exceptions = operationExceptions;
        this.description = operationDescription;
        this.method = null;
        
        forceParamName(this.parameters);
    }
    
    /**
     * Constructor
     * @param operationName the name of the operation
     * @param operationReturnType the operation return type
     * @param operationParameters the parameter list of that operation
     * @param operationExceptions the exception list of that operation
     * @param operationDescription the description of that operation
     * @param isIntrospected true only if attribute have been discovered
     */
    public MBeanOperation(String operationName, String operationReturnType,
            List<MBeanOperationParameter> operationParameters,
            List<MBeanOperationException> operationExceptions,
            String operationDescription, boolean isIntrospected) {
        
        this.name = operationName;
        this.returnTypeName = operationReturnType;
        if (operationParameters == null)
            this.parameters = new ArrayList();
        else
            this.parameters = operationParameters;
        if (operationExceptions == null)
            this.exceptions = new ArrayList();
        else
            this.exceptions = operationExceptions;
        this.description = operationDescription;
        this.method = null;
        
        this.wrapped = isIntrospected;
        forceParamName(this.parameters);
    }

    private void forceParamName(List<MBeanOperationParameter> parameters) {
        
        MBeanOperationParameter current;
        
        for (int i = 0; i < parameters.size(); i++) {
             current = getParameter(i);
             if (current.getParamName().equals(""))// NOI18N
                 current.setParamName("p"+i);// NOI18N
        }
    }
    
    public Method getMethod() {
        return method;
    }
    
    /**
     * Returns an operation description.
     * @return String the operation description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the name of the operation.
     * @return String the name of the operation
     *
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the operation name.
     * @param name the operation name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the operation return type name.
     * @param retType the operation return type to set
     */
    public void setReturnTypeName(String retType) {
        this.returnTypeName = retType;
    }
    
    /**
     * Returns the return type name of the operation.
     * @return String the return type of the operation
     */
    public String getReturnTypeName() {
        return returnTypeName;
    }
    
    /**
     * Adds an operation parameter to the list of parameters
     * @param param the parameter to add
     */
    public void addParameter(MBeanOperationParameter param) {
        if (parameters == null)
            throw new IllegalStateException("This operation has no params list.");// NOI18N
        this.parameters.add(param);
    }
    
    /**
     * Removes an operation parameter to the list of parameters
     * @param param the parameter to remove
     */
    public void removeParameter(MBeanOperationParameter param) {
        if (parameters == null)
            throw new IllegalStateException("This operation has no params list.");// NOI18N
        this.parameters.remove(param);
    }
    
    /**
     * Removes an operation parameter to the list of parameters
     * @param index the index of the parameter to remove
     */
    public void removeParameter(int index) {
        if (parameters == null)
            throw new IllegalStateException("This operation has no params list.");// NOI18N
        this.parameters.remove(index);
    }
    
    /**
     * Method which returns an operation parameter
     * @param index the index of the operation parameter
     * @return MBeanOperationParameter the parameter at index
     *
     */
    public MBeanOperationParameter getParameter(int index) {
        if (parameters == null)
            throw new IllegalStateException("This operation has no params list.");// NOI18N
        return parameters.get(index);
    }
    
    /**
     * Sets the class variable to the current list of parameters
     * @param array the list of parameters to set for the current operation
     */
    public void setParametersList(List<MBeanOperationParameter>
            array) {
        parameters = array;
    }
    
    /**
     * Returns the whole parameter list for the current operation
     * @return ArrayList<MBeanOperationParameter> the list of parameters
     */
    public List<MBeanOperationParameter> getParametersList() {
        return parameters;
    }
    
    /**
     * Returns a string concat of the couples (parameter type parameter name);
     * The couples are seperated from another by ","
     * @return String the string containing the parameter couples
     */
    public String getSignature() {
        String paramName = "";// NOI18N
        String paramType = "";// NOI18N
        String paramString = "";// NOI18N
        for(int i = 0; i < parameters.size(); i++) {
            paramName = parameters.get(i).getParamName();
            paramType = parameters.get(i).getParamType();
            paramString += paramType + " " + paramName;// NOI18N
            
            if (i < parameters.size() -1){
                paramString += ",";// NOI18N
            }
        }
        return paramString;
    }
    
    /**
     * Returns a string concat of all parameter types for the 
     * current operation
     * Each type is seperated form the next one by ","
     * @return String the concat string of parameter types
     */
    public String getSimpleSignature() {
        String paramType = "";// NOI18N
        String paramString = "";// NOI18N
        for(int i = 0; i < parameters.size(); i++) {
            paramType = parameters.get(i).getParamType();
            paramString += paramType;
            
            if (i < parameters.size() -1){
                paramString += ",";// NOI18N
            }
        }
        return paramString;
    }
    
    /**
     * Adds an exception to the list of exceptions for the current operation
     * @param excep The MBeanOperationException to add
     */
    public void addException(MBeanOperationException excep) {
        if (exceptions == null)
            throw new IllegalStateException("This operation has no exceptions list.");// NOI18N
        this.exceptions.add(excep);
    }
    
    /**
     * Removes an operation exception from the exception list of the current
     * operation
     * @param excep the operation exception to remove from the list
     */
    public void removeException(MBeanOperationException excep) {
        if (exceptions == null)
            throw new IllegalStateException("This operation has no exceptions list.");// NOI18N
        this.exceptions.remove(excep);
    }
    
    /**
     * Removes an operation exception from the list by it's index
     * @param index the index of the exception to remove from the list
     */
    public void removeException(int index) {
        if (exceptions == null)
            throw new IllegalStateException("This operation has no exceptions list.");// NOI18N
        this.exceptions.remove(index);
    }
    
    /**
     * Method which returns an operation exception
     * @param index the index of the operation exception
     * @return MBeanOperationException the exception at index
     *
     */
    public MBeanOperationException getException(int index) {
        if (exceptions == null)
            throw new IllegalStateException("This operation has no exceptions list.");// NOI18N
        return exceptions.get(index);
    }
    
    /**
     * Sets the exception list for the current operation
     * @param array the exception list to set
     */
    public void setExceptionsList(ArrayList<MBeanOperationException> 
            array) {
        exceptions = array;
    }
    
    /**
     * Returns the whole exception list for the current operation
     * @return ArrayList<MBeanOperationException> the exception list
     */
    public List<MBeanOperationException> getExceptionsList() {
        return exceptions;
    }
    
    /**
     * Returns a string concat of all exception classes for the current 
     * operation
     * Each one is seperated from the next one by ","
     * @return String the concat of exception classes
     */
    
    public String getExceptionClasses() {
        String excepClassAndDescription = "";// NOI18N
        for (int i = 0; i < exceptions.size(); i++) {
            excepClassAndDescription += 
                    exceptions.get(i).getExceptionClass();
            
            if (i < exceptions.size() -1)
                excepClassAndDescription += ",";// NOI18N
        }
        return excepClassAndDescription;
    }
    
    /**
     * Converts a concat string of all parameter types into an
     * arraylist of operation parameters for the current operation
     * @param concat the conctened String
     */
    public void setExceptionClasses(String concat) {
        String copyString = concat;
        
        String[] classes = copyString.split(WizardConstants.EXCEPTIONS_SEPARATOR);
        
        for (int i = 0; i < classes.length; i++) {
            addException(new MBeanOperationException(classes[i],""));// NOI18N
        }
    }
    
    /**
     * Sets the operation description.
     * @param descr the operation description to set
     */
    public void setDescription(String descr) {
        this.description = descr;
    }
    
    /**
     * Method which returns the number of parameters
     * If null, it returns -1
     * @return int the number of parameters
     *
     */
    public int getParametersSize() {
        if (parameters != null)
            return parameters.size();
        else
            return -1;
    }
    
    /**
     * Method which returns the number of exceptions
     * If null, it returns -1
     * @return int the number of exceptions
     *
     */
    public int getExceptionsSize() {
        if (exceptions != null)
            return exceptions.size();
        else
            return -1;
    }

    public boolean isMethodExists() {
        return methodExists;
    }

    public void setMethodExists(boolean methodExists) {
        this.methodExists = methodExists;
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }
    
    public int compareTo(Object o) {
        MBeanOperation op = (MBeanOperation) o;
        
        int comp = name.compareTo(op.getName());
        if (comp != 0)
            return comp;
        
        return getSimpleSignature().compareTo(op.getSimpleSignature());
    }
    
}
