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


package org.netbeans.modules.i18n.jsp;


import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nModule;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.JavaI18nSupport;

import org.openide.loaders.DataObject;


/** 
 * Support for internationalizing strings in jsp sources.
 * It support i18n-izing strings occured only in jsp scriptlets, declaractions and expressions.
 *
 * @author Peter Zavadsky
 * @see org.netbeans.modules.i18n.JavaI18nSupport
 */
public class JspI18nSupport extends JavaI18nSupport {


    /** Constructor. */
    public JspI18nSupport(DataObject sourceDataObject) {
        super(sourceDataObject);
    }
    
    
    /** Creates <code>I18nFinder</code>. Implements superclass abstract method. */
    protected I18nFinder createFinder() {
        return new JspI18nFinder(document);
    }
 
    /** Overrides superclass method. 
     * @return false */
    public boolean hasAdditionalCustomizer() {
        return false;
    }
    
    /** Overrides superclass method. 
     * @return null */
    public JPanel getAdditionalCustomizer(I18nString i18nString) {
        return null;
    }
    
    
    /** Finder which search hard coded strings in java sources. */
    public static class JspI18nFinder extends JavaI18nFinder {

        /** State when finder is in jsp code excluding parts where java code can occure. */
        protected static final int STATE_JSP = 8;
        /** State when finder is at the start of scripting tag where java code could occure. */
        protected static final int STATE_JSP_START_SCRIPTING = 9;
        /** State when finder is at beginnig of tag where java code occure. */
        protected static final int STATE_JSP_SCRIPTING = 10;
        /** State when finder is at the end of scripting tag where java code occures. */
        protected static final int STATE_JSP_END_SCRIPTING = 11;

        /** Helper array holding jsp scripting element tags for jsp using xml tags. */
        private static final String[] jspStrings = new String[] {
            "jsp:declaration", // NOI18N
            "jsp:expression", // NOI18N
            "jsp:scriptlet" // NOI18N
        }; // PENDING<< Don't know if to use it.

        
        /** Helper variable. Stores old state of java code when possible end of srcipting element occured
         * and state chaned to STATE_JSP_END_SCRIPTING. */
        private int oldJavaState;
        
        /** Constructor. */
        public JspI18nFinder(StyledDocument document) {
            super(document);
        }

        
        /** Initializes finder. Overrides superclass method. */
        protected void initialize() {
            super.initialize();
            
            state = STATE_JSP;
        }
        
        /** Handles state changes according next character. Overrides superclass method. */
        protected HardCodedString handleCharacter(char character) {
            if(state == STATE_JSP)
                return handleStateJsp(character);
            else if(state == STATE_JSP_START_SCRIPTING)
                return handleStateJspStartScripting(character);
            else if(state == STATE_JSP_SCRIPTING)
                return handleStateJspScripting(character);
            else if(state == STATE_JSP_END_SCRIPTING)
                return handleStateJspEndScripting(character);
            else {
                // Java code states.
                if(character == '%') {
                    // Could be end of scripting element.
                    state = STATE_JSP_END_SCRIPTING;
                    oldJavaState = state;
                    
                    return null;
                } else if(character == '<') { // PENDING see above.
                    // Could be end jsp:expression, jsp:scriptlet or jsp:declaration tag.
                    for(int i=0; i<jspStrings.length; i++) {
                        if(isNextString("</"+jspStrings[i]+">")) { // NOI18N

                            position += jspStrings[i].length() + 2;
                            state = STATE_JSP;
                            
                            return null;
                        }
                    }
                }
                
                return super.handleCharacter(character);
            }
        }

        /** Handles state <code>STATE_JSP</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJsp(char character) {
            if(character == '<')
                state = STATE_JSP_START_SCRIPTING;
                
            return null;
        }

        /** Handles state <code>STATE_JSP_START_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspStartScripting(char character) {
            if(character == '%')
                state = STATE_JSP_SCRIPTING;
            else if(character == 'j') { // PENDING see above.
                // Could be jsp:expression, jsp:scriptlet or jsp:declaration tag.
                for(int i=0; i<jspStrings.length; i++) {
                    if(isNextString(jspStrings[i]+">")) { // NOI18N
                        
                        position += jspStrings[i].length();
                        state = STATE_JAVA;
                    }
                }
            } else
                state = STATE_JSP;
                
            return null;
        }

        /** Utility method.
         * @return true if follows string in searched docuement */
        private boolean isNextString(String nextString) {
            // PENDING better would be operate on buffer tah document.
            
            if(buffer.length < position + nextString.length())
                return false;
            
            try {
                if(nextString.equals(document.getText(position, nextString.length())))
                    return true;
            } catch(BadLocationException ble) {
                // It's OK just to catch it.
            }
            
            return false;
        }
        
        /** Handles state <code>STATE_JSP_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspScripting(char character) {
            if(character == '!' // JSP declaraction.
                || character == '=' // JSP expression.
                || Character.isWhitespace(character)) // JSP scriptlet.
                    
                state = STATE_JAVA;
            else
                state = STATE_JSP;
                
            return null;
        }
        
        /** Handles state <code>STATE_JSP_END_SCRIPTING</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateJspEndScripting(char character) {
            if(character == '>')
                state = STATE_JSP;
            else
                state = oldJavaState;
                
            return null;
        }
       
    } // End of JavaI18nFinder nested class.
    
    
    /** Factory for <code>JspI18nSupport</code>. */
    public static class Factory implements I18nSupport.Factory {
        
        /** Implements interface. */
        public I18nSupport create(DataObject dataObject) {
            return new JspI18nSupport(dataObject);
        }
    }
}
