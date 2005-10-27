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

package org.netbeans.modules.options.indentation;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;

import org.openide.text.IndentEngine;
import org.openide.util.Lookup;


class IndentationModel {

    private boolean         originalExpandedTabs;
    private boolean         originalAddStar;
    private boolean         originalNewLine;
    private boolean         originalSpace;
    private int             originalStatementIndent = 0;
    private int             originalIndent = 0;
    
    private boolean         changed = false;

    
    IndentationModel () {
        // save original values
        originalExpandedTabs = isExpandTabs ();
        originalAddStar = getJavaFormatLeadingStarInComment ();
        originalNewLine = getJavaFormatNewlineBeforeBrace ();
        originalSpace = getJavaFormatSpaceBeforeParenthesis ();
        originalStatementIndent = getJavaFormatStatementContinuationIndent ().
                intValue ();
        originalIndent = getSpacesPerTab ().intValue ();
    }
    
    boolean isExpandTabs () {
        return ((Boolean) getParameter ("isExpandTabs", Boolean.FALSE)).
            booleanValue ();
    }
    
    void setExpandTabs (boolean expand) {
        setParameter ("setExpandTabs", Boolean.valueOf (expand), Boolean.TYPE);
        updateChanged ();
    }
    
    boolean getJavaFormatLeadingStarInComment () {
        return ((Boolean) getParameter (
            "getJavaFormatLeadingStarInComment", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatLeadingStarInComment (boolean star) {
        setParameter (
            "setJavaFormatLeadingStarInComment", 
            Boolean.valueOf (star), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    boolean getJavaFormatSpaceBeforeParenthesis () {
        return ((Boolean) getParameter (
            "getJavaFormatSpaceBeforeParenthesis", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatSpaceBeforeParenthesis (boolean space) {
        setParameter (
            "setJavaFormatSpaceBeforeParenthesis", 
            Boolean.valueOf (space), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    boolean getJavaFormatNewlineBeforeBrace () {
        return ((Boolean) getParameter (
            "getJavaFormatNewlineBeforeBrace", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatNewlineBeforeBrace (boolean newLine) {
        setParameter (
            "setJavaFormatNewlineBeforeBrace", 
            Boolean.valueOf (newLine), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    Integer getJavaFormatStatementContinuationIndent () {
        return (Integer) getParameter (
            "getJavaFormatStatementContinuationIndent", new Integer (4)
        );
    }
    
    void setJavaFormatStatementContinuationIndent (Integer continuation) {
	if (continuation.intValue () > 0)
            setParameter (
                "setJavaFormatStatementContinuationIndent", 
                continuation, 
                Integer.TYPE
            );
        updateChanged ();
    }
    
    Integer getSpacesPerTab () {
        return (Integer) getParameter (
            "getSpacesPerTab", new Integer (4)
        );
    }
    
    void setSpacesPerTab (Integer spaces) {
	if (spaces.intValue () > 0)
            setParameter (
                "setSpacesPerTab", 
                spaces, 
                Integer.TYPE
            );
        updateChanged ();
    }

    boolean isChanged () {
        return changed;
    }

    void revertChanges () {
        if (!changed) return; // no changes
        if (getJavaFormatLeadingStarInComment () != originalAddStar)
            setJavaFormatLeadingStarInComment (originalAddStar);
        if (getJavaFormatNewlineBeforeBrace () != originalNewLine)
            setJavaFormatNewlineBeforeBrace (originalNewLine);
        if (getJavaFormatSpaceBeforeParenthesis () != originalSpace)
            setJavaFormatSpaceBeforeParenthesis (originalSpace);
        if (isExpandTabs () != originalExpandedTabs)
            setExpandTabs (originalExpandedTabs);
        if (getJavaFormatStatementContinuationIndent ().intValue () != 
                originalStatementIndent &&
            originalStatementIndent > 0
        )
            setJavaFormatStatementContinuationIndent 
                (new Integer (originalStatementIndent));
        if (getSpacesPerTab ().intValue () != 
                originalIndent &&
            originalIndent > 0
        )
            setSpacesPerTab 
                (new Integer (originalIndent));
    }
    
    // private helper methods ..................................................

    private void updateChanged () {
        changed = 
                isExpandTabs () != originalExpandedTabs ||
                getJavaFormatLeadingStarInComment () != originalAddStar ||
                getJavaFormatNewlineBeforeBrace () != originalNewLine ||
                getJavaFormatSpaceBeforeParenthesis () != originalSpace ||
                getJavaFormatStatementContinuationIndent ().intValue () != 
                    originalStatementIndent ||
                getSpacesPerTab ().intValue () != originalIndent;
    }
    
    private IndentEngine javaIndentEngine;
    private Object getParameter (String parameterName, Object defaultValue) {
        if (javaIndentEngine == null) {
            BaseOptions options = getOptions ("text/x-java");
            if (options == null)
                options = getOptions ("text/plain");
            if (options == null) return defaultValue;
            javaIndentEngine = options.getIndentEngine ();
        }
        try {
            Method method = javaIndentEngine.getClass ().getMethod (
                parameterName,
                new Class [0]
            );
            return method.invoke (javaIndentEngine, new Object [0]);
        } catch (Exception ex) {
        }
        return defaultValue;
    }
    
    private void setParameter (
        String parameterName, 
        Object parameterValue,
        Class parameterType
    ) {
        Iterator it = AllOptionsFolder.getDefault ().getInstalledOptions ().
            iterator ();
        while (it.hasNext ()) {
            Class optionsClass = (Class) it.next ();
            BaseOptions baseOptions = (BaseOptions) BaseOptions.findObject 
                (optionsClass, true);
            IndentEngine indentEngine = baseOptions.getIndentEngine ();
            try {
                // HACK
                if (baseOptions.getClass ().getName ().equals ("org.netbeans.modules.java.editor.options.JavaOptions") &&
                    !indentEngine.getClass ().getName ().equals ("org.netbeans.modules.editor.java.JavaIndentEngine")
                ) {
                    Class javaIndentEngineClass = getClassLoader ().loadClass 
                        ("org.netbeans.modules.editor.java.JavaIndentEngine");
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup 
                        (javaIndentEngineClass);
                    baseOptions.setIndentEngine (indentEngine);
                }
                if (baseOptions.getClass ().getName ().equals ("org.netbeans.modules.web.core.syntax.JSPOptions") &&
                    !indentEngine.getClass ().getName ().equals ("org.netbeans.modules.web.core.syntax.JspIndentEngine")
                ) {
                    Class jspIndentEngineClass = getClassLoader ().loadClass 
                        ("org.netbeans.modules.web.core.syntax.JspIndentEngine");
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup 
                        (jspIndentEngineClass);
                    baseOptions.setIndentEngine (indentEngine);
                }
                Method method = indentEngine.getClass ().getMethod (
                    parameterName,
                    new Class [] {parameterType}
                );
                method.invoke (indentEngine, new Object [] {parameterValue});
            } catch (Exception ex) {
            }
        }
    }
    
    private ClassLoader classLoader;
    private ClassLoader getClassLoader () {
        if (classLoader == null)
            classLoader = (ClassLoader) Lookup.getDefault ().lookup 
                (ClassLoader.class);
        return classLoader;
    }
    
    private static BaseOptions getOptions (String mimeType) {
        Iterator it = AllOptionsFolder.getDefault ().getInstalledOptions ().
            iterator ();
        while (it.hasNext ()) {
            Class optionsClass = (Class) it.next ();
            BaseOptions baseOptions = (BaseOptions) BaseOptions.findObject 
                (optionsClass, true);
            BaseKit kit = BaseKit.getKit (baseOptions.getKitClass ());
            if (kit.getContentType ().equals (mimeType))
                return baseOptions;
        }
        return null;
    }
}


