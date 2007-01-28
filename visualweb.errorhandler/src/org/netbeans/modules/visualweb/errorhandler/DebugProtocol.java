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
package org.netbeans.modules.visualweb.errorhandler;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
/*
 * DebugProtocol.java
 * Created on January 6, 2004, 1:21 PM
 */

/**
 * @author  Winston Prakash
 */

public class DebugProtocol {
    public static final int STATE_WAITING = 0;
    public static final int STATE_DEBUG_REQUEST = 1;
    public static final int STATE_CLIENT_RCOGNIZED = 2;
    public static final int STATE_CLIENT_UNRCOGNIZED = 3;
    public static final int STATE_DONE = 4;

    public static String DEBUG_CLIENT_ID = "DEBUG_CLIENT_ID";  // NOI18N
    public static String DEBUG_CLIENT_NAME = "Creator Debug Client";  // NOI18N
    public static String DEBUG_REQUEST_START = "DEBUG_REQUEST_START";  // NOI18N
    public static String DEBUG_REQUEST_END = "DEBUG_REQUEST_END";  // NOI18N
    public static String DEBUG_CLASS_NAME = "ClassName";  // NOI18N
    public static String DEBUG_FILE_NAME = "FileName";  // NOI18N
    public static String DEBUG_METHOD_NAME = "MethodName";  // NOI18N
    public static String DEBUG_LINE_NUMBER = "LineNumber";  // NOI18N
    public static String DEBUG_DELIMITER = ":";  // NOI18N

    private int state = STATE_WAITING;

    ErrorInfo errorInfo = null;

    StringTokenizer tokenizer;

    public String processInput(String inputString) {
        String outputString = null;
        if(state == STATE_WAITING){
            tokenizer = new StringTokenizer(inputString, DEBUG_DELIMITER);
            String param = tokenizer.nextToken();
            if (param.startsWith(DEBUG_CLIENT_ID)) {
                String clientName = tokenizer.nextToken();
                if(clientName.equals(DEBUG_CLIENT_NAME)){
                    outputString = "Client recognized";   // NOI18N
                    state = STATE_CLIENT_RCOGNIZED;
                    return outputString;
                }else{
                    outputString = "Client unrecognized continuing ..";   // NOI18N
                    state = STATE_CLIENT_UNRCOGNIZED;
                }
                return outputString;
            }else{
                outputString = "Client unrecognized disconnecting ..";   // NOI18N
                state = STATE_CLIENT_UNRCOGNIZED;
            }
        }else if(state == STATE_CLIENT_RCOGNIZED){
            if (inputString.startsWith(DEBUG_REQUEST_START)) {
                outputString = "Debug Request Received";   // NOI18N
                state = STATE_DEBUG_REQUEST;
                errorInfo = new ErrorInfo();
                return outputString;
            }
        }else if(state == STATE_DEBUG_REQUEST){
            if (inputString.startsWith(DEBUG_REQUEST_END)) {
                outputString = "Debug Request Completed";   // NOI18N
                processRequest();
                state = STATE_DONE;
                return outputString;
            }
            tokenizer = new StringTokenizer(inputString, DEBUG_DELIMITER);
            String param = tokenizer.nextToken();
            if (param.startsWith(DEBUG_CLASS_NAME)) {
                String className = tokenizer.nextToken();
                outputString = "Class Name - " + className;   // NOI18N
                errorInfo.setClassName(className);
                return outputString;
            }
            if (param.startsWith(DEBUG_FILE_NAME)) {
                String fileName = tokenizer.nextToken();
                outputString = "File Name - " + fileName;   // NOI18N
                errorInfo.setFileName(fileName);
                return outputString;
            }
            if (param.startsWith(DEBUG_METHOD_NAME)) {
                String methodName = tokenizer.nextToken();
                outputString = "Method Name - " + methodName;   // NOI18N
                errorInfo.setMethodName(methodName);
                return outputString;
            }
            if (param.startsWith(DEBUG_LINE_NUMBER)) {
                String lineNumber = tokenizer.nextToken();
                outputString = "Line Number - " + lineNumber;   // NOI18N
                try{
                    errorInfo.setLineNumber(Integer.parseInt(lineNumber));
                }catch(Exception exc){
                    exc.printStackTrace();
                }
                return outputString;
            }
        }
        return outputString;
    }
    
    private void processRequest(){
        //Process the request
    }
    
    public int getState(){
        return state;
    }
    
    public void setState(int state){
        this.state = state;
        if(state == STATE_WAITING) errorInfo = null;
    }
    
    public ErrorInfo getErrorInfo(){
        return errorInfo;
    }
    
    public class ErrorInfo{
        String className = null;
        String fileName = null;
        String methodName = null;
        String filePath = null;
        int lineNumber;
        
        public void setClassName(String cName){
            className = cName;
        }
        
        public void setMethodName(String mName){
            methodName = mName;
        }
        
        public void setFileName(String fName){
            fileName = fName;
        }
        
        public void setLineNumber(int lNumber){
            lineNumber = lNumber;
        }
        
        public int getLineNumber(){
            return lineNumber;
        }
        
        public String getFilePath(){
            return className.replace('.', '/') + ".java";   // NOI18N
        }
        
    }
}
