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
/*
 * IntEditor.java
 *
 * Created on February 28, 2003, 2:15 PM
 */

package org.netbeans.beaninfo.editors;
import java.beans.*;
import java.util.Arrays;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.*;
import org.openide.util.NbBundle;
/** An editor for primitive integer types which allows hinting of tag
 *  values and handles whitespace in setAsText better than the default
 *  JDK one.  The following hints are supported:
 *  <UL><LI><B>stringKeys</B> - an array of Strings that should be supplied
 *  from getTags()</LI><LI><B>intValues - an array of ints corresponding to
 *  the values represented by stringKeys. <I>This hint must be present if
 *  the stringKeys hint is used, and the arrays must be of the same length.</LI>
 *  <LI><B>codeValues</B> - an array of strings that should be returned from
 *  getJavaInitializationString.  This hint is optional when using the
 *  aforementioned hints.  <P>These hints will also work for the Integer
 *  editor, which wraps an instance of this editor.
 *
 * @author  Tim Boudreau
 * @version 1.0
 */
public class IntEditor extends ExPropertyEditorSupport {
    
    public static final String KEYS = "stringKeys"; //NOI18N
    public static final String VALS = "intValues"; //NOI18N
    public static final String CODE_VALS = "codeValues"; //NOI18N
    String[] keys=null;
    String[] code=null;
    int[] values=null;
    /** Creates a new instance of IntEditor */
    public IntEditor() {
    }
    
    protected void attachEnvImpl(PropertyEnv env) {
        keys = (String[]) env.getFeatureDescriptor().getValue(KEYS);
        values = (int[]) env.getFeatureDescriptor().getValue(VALS);
        code = (String[]) env.getFeatureDescriptor().getValue(CODE_VALS);
    }
    
    /** Validate that the values supplied by the PropertyEnv are proper,
     *  so there's not an obscure ArrayIndexOutOfBoundsException some time
     *  later because bad information was supplied.  */
    protected void validateEnv(PropertyEnv env) {
        //fail fast validation of illegal values
        boolean valid = keys == null && values == null && code == null;
        if (!valid) {
            valid = keys != null && values != null;
            if (!valid) {
                throw new EnvException(
                "You must specify both an array of keys and an " + //NOI18N
                "array of values if you specify one. Keys=" +   //NOI18N
                arrToStr(keys) + " Values=" + arrToStr(values));  //NOI18N
            } else {
                valid = keys.length == values.length;
                if (valid) {
                    valid = keys.length > 0 && values.length > 0;
                }
                
                if (!valid) {
                    throw new EnvException(
                    "The arrays of keys and values must have the same " + //NOI18N
                    "length and the length must be > 0. keys.length =" +  //NOI18N
                    keys.length + " values.length=" + values.length + " Keys=" + //NOI18N
                    arrToStr(keys) + " Values=" + arrToStr(values)); //NOI18N
                } else {
                    if (code != null) {
                        valid = code.length == keys.length;
                        if (valid) {
                            valid = code.length > 0;
                        }
                        if (!valid) {
                            throw new EnvException(
                            "The arrays of keys and values and codes must all" + //NOI18N
                            " have the same length, > 0. keys.length =" +   //NOI18N
                            keys.length + " values.length=" + values.length + //NOI18N
                            " Code.length=" + code.length + " Keys=" + //NOI18N
                            arrToStr(keys) + " Values=" + arrToStr(values) +
                            " Code=" + arrToStr(code));  //NOI18N
                        }
                    }
                }
            }
        }
    }
    
    private static final String arrToStr(int[] s) {
        if (s == null) return "null"; //NOI18N
        StringBuffer out = new StringBuffer(s.length * 3);
        for (int i=0; i < s.length; i++) {
            out.append(s[i]);
            if (i != s.length-1) {
                out.append(","); //NOI18N
            }
        }
        return out.toString();
    }
    
    private String getStringRep(int i) {
        if (keys != null) {
            try {
                return keys[i];
            } catch (ArrayIndexOutOfBoundsException ae) {
                //don't internationalize this exception, users never see it
                throw new IllegalArgumentException(
                "This property editor uses a set of keyed values, " +  //NOI18N
                "and the value " //NOI18N
                + i + " is out of range."); //NOI18N
            }
        } else {
            return Integer.toString(i);
        }
    }
    
    public String getAsText() {
        return getStringRep(((Integer) getValue()).intValue());
    }
    
    private void doSetAsText(String s) {
        //fixes issue 23077, confusing error message from NFE.
        //IllegalArgumentException is a more correct exception to throw
        //anyway
        try {
            setValue(new Integer(Integer.parseInt(s)));
        } catch (NumberFormatException nfe) {
            String msg = NbBundle.getMessage(
            IntEditor.class, "EXC_ILLEGAL_VALUE_TEXT") + s;
            RuntimeException iae = new IllegalArgumentException(msg); //NOI18N
            ErrorManager.getDefault().annotate(iae, ErrorManager.USER, msg,
            msg, iae, new java.util.Date());
            throw iae;
        }
    }
    
    public void setAsText(String s) {
        s = s.trim();
        if (keys == null) {
            doSetAsText(s);
        } else {
            //use the keys, translate to an int value
            int idx = Arrays.asList(keys).indexOf(s);
            if ((idx == -1) || (idx > values.length-1)) {
                StringBuffer msg = new StringBuffer();
                msg.append(NbBundle.getMessage(IntEditor.class,
                "EXC_ILLEGAL_STRING_TEXT_FIRST")); //NOI18N
                msg.append(s);
                msg.append(NbBundle.getMessage(IntEditor.class,
                "EXC_ILLEGAL_STRING_TEXT_SECOND"));
                msg.append(arrToStr(keys));
                String message = msg.toString();
                RuntimeException iae = new IllegalArgumentException(message);
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                message, message, iae, new java.util.Date());
                throw iae;
            } else {
                setValue(new Integer(values[idx]));
            }
        }
    }
    
    public String[] getTags() {
        return keys;
    }
    
    public String getJavaInitializationString() {
        String result;
        if (code == null) {
            result = getValue().toString();
        } else {
            result = code[((Integer) getValue()).intValue()];
        }
        return result;
    }
}
