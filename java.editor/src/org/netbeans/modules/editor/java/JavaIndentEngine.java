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

package org.netbeans.modules.editor.java;

import java.io.*;
import javax.swing.text.Document;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.java.JavaFormatter;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.modules.editor.EditorModule;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.openide.text.IndentEngine;

/**
* Java indentation engine that delegates to java formatter
*
* @author Miloslav Metelka
*/

public class JavaIndentEngine extends FormatterIndentEngine {

    public static final String JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP
        = "javaFormatNewlineBeforeBrace"; // NOI18N

    public static final String JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP
        = "javaFormatSpaceBeforeParenthesis"; // NOI18N

    public static final String JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP
        = "javaFormatLeadingStarInComment"; // NOI18N
    
    public static final String JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP
        = "javaFormatStatementContinuationIndent"; // NOI18N

    static final long serialVersionUID = -7936605291288152329L;

    public JavaIndentEngine() {
        setAcceptedMimeTypes(new String[] { JavaKit.JAVA_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
        return new JavaFormatter(JavaKit.class);
    }


    public boolean getJavaFormatSpaceBeforeParenthesis() {
        Boolean b = (Boolean)getValue(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS);
        if (b == null) {
            b = JavaSettingsDefaults.defaultJavaFormatSpaceBeforeParenthesis;
        }
        return b.booleanValue();
    }
    public void setJavaFormatSpaceBeforeParenthesis(boolean b) {
        setValue(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS, b ? Boolean.TRUE : Boolean.FALSE, JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP);
    }

    public boolean getJavaFormatNewlineBeforeBrace() {
        Boolean b = (Boolean)getValue(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE);
        if (b == null) {
            b = JavaSettingsDefaults.defaultJavaFormatNewlineBeforeBrace;
        }
        return b.booleanValue();
    }
    public void setJavaFormatNewlineBeforeBrace(boolean b) {
        setValue(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE, b ? Boolean.TRUE : Boolean.FALSE, JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP);
    }
    
    public boolean getJavaFormatLeadingStarInComment() {
        Boolean b = (Boolean)getValue(JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT);
        if (b == null) {
            b = JavaSettingsDefaults.defaultJavaFormatLeadingStarInComment;
        }
        return b.booleanValue();
    }        
    public void setJavaFormatLeadingStarInComment(boolean b) {
        setValue(JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT, b ? Boolean.TRUE : Boolean.FALSE, JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP);
    }
    
    public int getJavaFormatStatementContinuationIndent() {
        Integer i = (Integer)getValue(JavaSettingsNames.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT);
        if (i == null) {
            i = JavaSettingsDefaults.defaultJavaFormatStatementContinuationIndent;
        }
        return i.intValue();
    }

    public void setJavaFormatStatementContinuationIndent(int javaFormatStatementContinuationIndent) {
        setValue(JavaSettingsNames.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT,
            new Integer(javaFormatStatementContinuationIndent), JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP);
    }

    // Serialization ------------------------------------------------------------

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP, Boolean.TYPE),
        new ObjectStreamField(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, Boolean.TYPE),
        new ObjectStreamField(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP, Boolean.TYPE),
        new ObjectStreamField(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, Integer.TYPE)
    };
    
    private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        setJavaFormatNewlineBeforeBrace(fields.get(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
            getJavaFormatNewlineBeforeBrace()));
        setJavaFormatSpaceBeforeParenthesis(fields.get(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
            getJavaFormatSpaceBeforeParenthesis()));
        setJavaFormatLeadingStarInComment(fields.get(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
            getJavaFormatLeadingStarInComment()));
        setJavaFormatStatementContinuationIndent(fields.get(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP,
            getJavaFormatStatementContinuationIndent()));
    }

    private void writeObject(java.io.ObjectOutputStream oos)
    throws IOException, ClassNotFoundException {
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP, getJavaFormatNewlineBeforeBrace());
        fields.put(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, getJavaFormatSpaceBeforeParenthesis());
        fields.put(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP, getJavaFormatLeadingStarInComment());
        fields.put(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, getJavaFormatStatementContinuationIndent());
        oos.writeFields();
    }

}

