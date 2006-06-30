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

package org.netbeans.editor.ext.html.javadoc;

/**
 *
 * @author Petr Pisl
 */
public class TagHelpItem implements HelpItem {

    private String identical;

    /**
     * Holds value of property startText.
     */
    private String startText;

    /**
     * Holds value of property startTextOffset.
     */
    private int startTextOffset;

    /**
     * Holds value of property endText.
     */
    private String endText;

    /**
     * Holds value of property endTextOffset.
     */
    private int endTextOffset;

    /**
     * Holds value of property textBefore.
     */
    private String textBefore;

    /**
     * Holds value of property textAfter.
     */
    private String textAfter;

    /**
     * Holds value of property file.
     */
    private String file;

    /**
     * Holds value of property name.
     */
    private String name;
    
    /** Creates a new instance of HelpItem */
    
    public TagHelpItem(String name, String identical) {
        this.identical = identical;
        this.name = name;
        file = null;
        startTextOffset = 0;
        startText = null;
        endTextOffset = 0;
        endText = null;
        textBefore = null;
        textAfter = null;
    }
    
    public TagHelpItem (String name){
        this.name = name;
        file = null;
        startTextOffset = 0;
        startText = null;
        endTextOffset = 0;
        endText = null;
        textBefore = null;
        textAfter = null;
        identical = null;
        
    }
    public TagHelpItem(String name, String file, 
                       String startText, int startOffset, 
                       String endText, int endOffset,
                       String textBefore, String textAfter){
        this.name = name;
        this.file = file;
        this.startText = startText;
        this.startTextOffset = startTextOffset;
        this.endText = endText;
        this.endTextOffset = endTextOffset;
        this.textBefore = textBefore;
        this.textAfter = textAfter;
        this.identical = null;
    }
    
    public TagHelpItem(String name, String file, String startText, int startOffset, 
                       String endText, int endOffset){
        this(name, file, startText, startOffset, endText, endOffset, null, null);
    }
    
    

    /**
     * Getter for property identical.
     * @return Value of property identical.
     */
    public String getIdentical() {
        return this.identical;
    }

    
    /**
     * Getter for property startText.
     * @return Value of property startText.
     */
    public String getStartText() {

        return this.startText;
    }

    /**
     * Getter for property startTextOffset.
     * @return Value of property startTextOffset.
     */
    public int getStartTextOffset() {

        return this.startTextOffset;
    }

    /**
     * Getter for property endText.
     * @return Value of property endText.
     */
    public String getEndText() {

        return this.endText;
    }

    /**
     * Getter for property endTextOffset.
     * @return Value of property endTextOffset.
     */
    public int getEndTextOffset() {

        return this.endTextOffset;
    }

    /**
     * Getter for property textBefore.
     * @return Value of property textBefore.
     */
    public String getTextBefore() {

        return this.textBefore;
    }

    /**
     * Getter for property textAfter.
     * @return Value of property textAfter.
     */
    public String getTextAfter() {

        return this.textAfter;
    }

    public String getHelp(){
        return "help for key "; // NOI18N
    }

    /**
     * Getter for property file.
     * @return Value of property file.
     */
    public String getFile() {

        return this.file;
    }

    /**
     * Setter for property endText.
     * @param endText New value of property endText.
     */
    public void setEndText(String endText) {
        this.endText = endText;
    }

    /**
     * Setter for property endTextOffset.
     * @param endTextOffset New value of property endTextOffset.
     */
    public void setEndTextOffset(int endTextOffset) {
        this.endTextOffset = endTextOffset;
    }

    /**
     * Setter for property file.
     * @param file New value of property file.
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Setter for property identical.
     * @param identical New value of property identical.
     */
    public void setIdentical(String identical) {
        this.identical = identical;
    }

    /**
     * Setter for property startText.
     * @param startText New value of property startText.
     */
    public void setStartText(String startText) {
        this.startText = startText;
    }

    /**
     * Setter for property startTextOffset.
     * @param startTextOffset New value of property startTextOffset.
     */
    public void setStartTextOffset(int startTextOffset) {
        this.startTextOffset = startTextOffset;
    }

    /**
     * Setter for property textAfter.
     * @param textAfter New value of property textAfter.
     */
    public void setTextAfter(String textAfter) {
        this.textAfter = textAfter;
    }

    /**
     * Setter for property textBefore.
     * @param textBefore New value of property textBefore.
     */
    public void setTextBefore(String textBefore) {
        this.textBefore = textBefore;
    }

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {

        this.name = name;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        if (identical == null){
            sb.append("\n    file: "); // NOI18N
            sb.append(file);
            sb.append("\n    start text: "); // NOI18N
            sb.append(startText);
            sb.append("\n    start text offset: "); // NOI18N
            sb.append(startTextOffset);
            sb.append("\n    end text: "); // NOI18N
            sb.append(endText);
            sb.append("\n    end text offset: "); // NOI18N
            sb.append(endTextOffset);
            sb.append("\n    text before: "); // NOI18N
            sb.append(textBefore);
            sb.append("\n    text after: "); // NOI18N
            sb.append(textAfter);
        }
        else{
            sb.append("\n    indentical to: "); // NOI18N
            sb.append(identical);
        }
        return sb.toString();
    }
}
