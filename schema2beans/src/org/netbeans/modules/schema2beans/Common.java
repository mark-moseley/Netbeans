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

package org.netbeans.modules.schema2beans;

import java.text.*;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;


/**
 *  This class contains the schema2beans constants and helper methods.
 */
public class Common {
    
    //	Constants
    static public final int NONE    		= 0x00000;
    
    static public final int MASK_USER		= 0xFFFF;
    static public int USE_DEFAULT_VALUES	= 0x0001;
    static public int NO_DEFAULT_VALUES		= 0x0002;
    
    static public final int MASK_SEQUENCE	= 0x0000F;
    static public final int SEQUENCE_AND	= 0x00001;
    static public final int SEQUENCE_OR		= 0x00002;
    
    static public final int MASK_INSTANCE	= 0x000F0;
    static public final int TYPE_0_1 		= 0x00010;
    static public final int TYPE_1			= 0x00020;
    static public final int TYPE_0_N		= 0x00030;
    static public final int TYPE_1_N		= 0x00040;
    
    static public final int MASK_TYPE		= 0x0FF00;
    static public final int TYPE_STRING		= 0x00100;
    static public final int TYPE_BEAN		= 0x00200;
    static public final int TYPE_BOOLEAN	= 0x00300;
    static public final int TYPE_BYTE		= 0x00400;
    static public final int TYPE_CHAR		= 0x00500;
    static public final int TYPE_SHORT		= 0x00600;
    static public final int TYPE_INT		= 0x00700;
    static public final int TYPE_LONG		= 0x00800;
    static public final int TYPE_FLOAT		= 0x00900;
    static public final int TYPE_DOUBLE		= 0x00a00;
    static public final int TYPE_COMMENT	= 0x00f00;

    static public final int MASK_PROP		= 0xF0000;
    static public final int TYPE_KEY		= 0x10000;
    static public final int TYPE_SHOULD_NOT_BE_EMPTY		= 0x20000;
    
    static public final int TYPE_VETOABLE	= 0x100000;
    
    static public final int COMMENT		= 0x01;
    static public final int ELEMENT		= 0x02;
    static public final int ATTLIST		= 0x03;
    
    static public final String DTD_STRING	= "#PCDATA";	// NOI18N
    static public final String DTD_EMPTY	= "EMPTY";	// NOI18N
    
    static public final String CLASS_STRING		= "String";	// NOI18N
    static public final String CLASS_BOOLEAN		= "Boolean";	// NOI18N

    static public final String GENERATED_TAG = "Generated";
    
    
    public static boolean isSequenceOr(int type) {
        return ((type & MASK_SEQUENCE) == SEQUENCE_OR);
    }
    
    public static boolean isArray(int type) {
        int t = type & MASK_INSTANCE;
        return (t == TYPE_0_N || t == TYPE_1_N);
    }
    
    public static boolean isBean(int type) {
        return ((type & MASK_TYPE) == TYPE_BEAN);
    }
    
    public static boolean isString(int type) {
        return ((type & MASK_TYPE) == TYPE_STRING);
    }
    
    public static boolean isBoolean(int type) {
        return ((type & MASK_TYPE) == TYPE_BOOLEAN);
    }

    /*
    public static boolean isInt(int type) {
        return ((type & MASK_TYPE) == TYPE_INT);
    }
    */
    
    public static boolean isKey(int type) {
        return ((type & TYPE_KEY) == TYPE_KEY);
    }
    
    public static boolean shouldNotBeEmpty(int type) {
        return ((type & TYPE_SHOULD_NOT_BE_EMPTY) == TYPE_SHOULD_NOT_BE_EMPTY);
    }
    
    public static boolean isVetoable(int type) {
        return ((type & TYPE_VETOABLE) == TYPE_VETOABLE);
    }

    /**
     * Is it a Java primitive or not?
     */
    public static boolean isScalar(int type) {
        switch(type & MASK_TYPE) {
	    case TYPE_STRING:
	    case TYPE_BEAN:
        case TYPE_COMMENT:
            return false;
	    case TYPE_BOOLEAN:
	    case TYPE_BYTE:
	    case TYPE_CHAR:
	    case TYPE_SHORT:
	    case TYPE_INT:
	    case TYPE_LONG:
	    case TYPE_FLOAT:
	    case TYPE_DOUBLE:
            return true;
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    public static String wrapperGetMethod(int type) {
        switch(type & MASK_TYPE) {
	    case TYPE_BOOLEAN:
            return "booleanValue";	// NOI18N
	    case TYPE_BYTE:
            return "byteValue";	// NOI18N
	    case TYPE_CHAR:
            return "charValue";	// NOI18N
	    case TYPE_SHORT:
            return "shortValue";	// NOI18N
	    case TYPE_INT:
            return "intValue";	// NOI18N
	    case TYPE_LONG:
            return "longValue";	// NOI18N
	    case TYPE_FLOAT:
            return "floatValue";	// NOI18N
	    case TYPE_DOUBLE:
            return "doubleValue";	// NOI18N
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    public static String wrapperClass(int type) {
        switch(type & MASK_TYPE) {
	    case TYPE_BOOLEAN:
            return "Boolean";	// NOI18N
	    case TYPE_BYTE:
            return "Byte";		// NOI18N
	    case TYPE_CHAR:
            return "Character";	// NOI18N
	    case TYPE_SHORT:
            return "Short";		// NOI18N
	    case TYPE_INT:
            return "Integer";	// NOI18N
	    case TYPE_LONG:
            return "Long";		// NOI18N
	    case TYPE_FLOAT:
            return "Float";		// NOI18N
	    case TYPE_DOUBLE:
            return "Double";	// NOI18N
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    public static int wrapperToType(String wrapper) {
        if (wrapper == null)
            return NONE;
        String s = wrapper.trim();
        if (s.endsWith("boolean"))	// NOI18N
            return TYPE_BOOLEAN;
        if (s.endsWith("byte"))		// NOI18N
            return TYPE_BYTE;
        if (s.endsWith("char"))		// NOI18N
            return TYPE_CHAR;
        if (s.endsWith("short"))	// NOI18N
            return TYPE_SHORT;
        if (s.endsWith("int"))		// NOI18N
            return TYPE_INT;
        if (s.endsWith("long"))		// NOI18N
            return TYPE_LONG;
        if (s.endsWith("float"))	// NOI18N
            return TYPE_FLOAT;
        if (s.endsWith("double"))	// NOI18N
            return TYPE_DOUBLE;
        if (s.equals("String") || s.equals("java.lang.String"))
            return TYPE_STRING;
        //System.out.println("schema2beans Common.wrapperToType: couldn't find type for "+wrapper);
        return NONE;
    }
    
    public static String scalarType(int type) {
        switch(type & MASK_TYPE) {
	    case TYPE_BOOLEAN:
            return "boolean";	// NOI18N
	    case TYPE_BYTE:
            return "byte";		// NOI18N
	    case TYPE_CHAR:
            return "char";		// NOI18N
	    case TYPE_SHORT:
            return "short";		// NOI18N
	    case TYPE_INT:
            return "int";		// NOI18N
	    case TYPE_LONG:
            return "long";		// NOI18N
	    case TYPE_FLOAT:
            return "float";		// NOI18N
	    case TYPE_DOUBLE:
            return "double";	// NOI18N
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    public static String typeToString(int type) {
        switch(type & MASK_TYPE) {
	    case TYPE_STRING:
            return "TYPE_STRING";	// NOI18N
        case TYPE_COMMENT:
            return "TYPE_COMMENT";
	    case TYPE_BEAN:
            return "TYPE_BEAN";	// NOI18N
	    case TYPE_BOOLEAN:
            return "TYPE_BOOLEAN";	// NOI18N
	    case TYPE_BYTE:
            return "TYPE_BYTE";	// NOI18N
	    case TYPE_CHAR:
            return "TYPE_CHAR";	// NOI18N
	    case TYPE_SHORT:
            return "TYPE_SHORT";	// NOI18N
	    case TYPE_INT:
            return "TYPE_INT";	// NOI18N
	    case TYPE_LONG:
            return "TYPE_LONG";	// NOI18N
	    case TYPE_FLOAT:
            return "TYPE_FLOAT";	// NOI18N
	    case TYPE_DOUBLE:
            return "TYPE_DOUBLE";	// NOI18N
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    public static String dumpHex(String v) {
        String s;
	
        if (v != null) {
            s = "hex[ ";	// NOI18N
            byte[] b = v.getBytes();
            for (int i=0; i<b.length; i++)
                s += Integer.toHexString((int)b[i]) + " ";	// NOI18N
            s += "]";	// NOI18N
        }
        else
            s = "<null>";	// NOI18N
	
        return s;
    }
    
    public static String constName(String name) {
        return name.replace('-', '_').toUpperCase();
    }
    
    /**
     *	Convert a DTD name into a bean name:
     *
     *	Any - or _ character is removed. The letter following - and _
     *	is changed to be upper-case.
     *	If the user mixes upper-case and lower-case, the case is not
     *	changed.
     *	If the Word is entirely in upper-case, the word is changed to
     *	lower-case (except the characters following - and _)
     *	The first letter is always upper-case.
     */
    public static String convertName(String name) {
        return convertName(name, true);
    }

    /**
     * Same as convertName, except the name that comes out will
     * be suitable as an instance variable (first letter is lowercase).
     */
    public static String convertNameInstance(String name) {
        return convertName(name, false);
    }
    
    private static String convertName(String name, boolean up) {
        CharacterIterator 	ci;
        StringBuffer	  	n = new StringBuffer();
        boolean			keepCase = false;
        char			c;
	
        ci = new StringCharacterIterator(name);
        c = ci.first();
	
        //	If everything is uppercase, we'll lowercase the name.
        while (c != CharacterIterator.DONE) {
            if (Character.isLowerCase(c)) {
                keepCase = true;
                break;
            }
            c = ci.next();
        }
	
        c = ci.first();
        while (c != CharacterIterator.DONE) {
            if (c == '-' || c == '_' || !Character.isJavaIdentifierPart(c))
                up = true;
            else {
                if (up)
                    c = Character.toUpperCase(c);
                else
                    if (!keepCase)
                        c = Character.toLowerCase(c);
                n.append(c);
                up = false;
            }
            c = ci.next();
        }
        return n.toString();
    }
    
    /**
     *	Often, an object from the DOM graph will contain spaces or
     *	LF characters, making String comparison impossible with values
     *	specified by the user. The goal of this method is to cleanup
     *	an object from such characters before being compared with
     *	a value specified by the user.
     */
    public static Object getComparableObject(Object obj) {
        Object ret = obj;
        if (obj instanceof java.lang.String) {
            String s = (String)obj;
            ret = s.trim();
        }
	
        return ret;
    }
    
    public static Object defaultScalarValue(int type) {
        switch(type & Common.MASK_TYPE) {
	    case Common.TYPE_STRING:
            return "";	// NOI18N
        case Common.TYPE_COMMENT:
            return "";	// NOI18N
	    case Common.TYPE_BOOLEAN:
            return Boolean.FALSE;
	    case Common.TYPE_BYTE:
            return new Byte((byte)0);
	    case Common.TYPE_CHAR:
            return new Character('\0');
	    case Common.TYPE_SHORT:
            return new Short((short)0);
	    case Common.TYPE_INT:
            return new Integer(0);
	    case Common.TYPE_LONG:
            return new Long(0);
	    case Common.TYPE_FLOAT:
            return new Float(0.0);
	    case Common.TYPE_DOUBLE:
            return new Double(0.0);
	    default:
            throw new IllegalArgumentException(Common.getMessage(
                                                                 "UnknownType_msg", new Integer(type)));
        }
    }
    
    
    /*
     *	Bundle utility methods. The following methods return a formated message
     *	using the message bundle key and the optional parameters.
     *
     *	The different flavors of getMessage(String, String, ...) call the
     *  getMessage(String, Object[]) method.
     */
    
    //static private ResourceBundle rb = null;
    static private String rbName = "org.netbeans.modules.schema2beans.Bundle"; // NOI18N
    
    public static String getMessage(String key) {
        return Common.getMessage(key, null);
    }
    
    public static String getMessage(String key, Object p1) {
        return Common.getMessage(key, new Object[] {p1});
    }
    
    public static String getMessage(String key, int p1) {
        return Common.getMessage(key, new Object[] {new Integer(p1)});
    }
    
    public static String getMessage(String key, Object p1, Object p2) {
        return Common.getMessage(key, new Object[] {p1, p2});
    }
    
    public static String getMessage(String key, Object p1, Object p2, Object p3) {
        return Common.getMessage(key, new Object[] {p1, p2, p3});
    }
    
    public static String getMessage(String key, Object[] args) {
        ResourceBundle rb = null;
        
        //  Find the resource bundle if it is not loaded yet
        if (rb == null) {
            try {
                rb = ResourceBundle.getBundle(rbName, Locale.getDefault(),
                                                     (Common.class).getClassLoader());
            } catch(MissingResourceException e) {
                //  Do without bundle
                System.err.println("Couldn't find the bundle " + rbName + // NOI18N
                                   " for the locale " + Locale.getDefault()); // NOI18N
            }
        }
	
        //  Get and format the message...
        if (rb != null) {
            //  ...using the resource bundle
            if (args != null) {
                return MessageFormat.format(rb.getString(key), args);
            } else {
                return rb.getString(key);
            }
        } else {
            //	...without the resource bundle
            String p = " "; // NOI18N
            if (args != null) {
                for (int i=0; i<args.length; i++) {
                    if (args[i] != null) {
                        p += (args[i].toString() + " "); // NOI18N
                    } else {
                        p += "null "; // NOI18N
                    }
                }
            }
            return key + p;
        }
    }
    
    static public String instanceToString(int instance) {
        switch (instance) {
	    case Common.TYPE_0_1:
            return "optional";
	    case Common.TYPE_0_N:
            return "an array, possibly empty";
	    case Common.TYPE_1_N:
            return "an array containing at least one element";
	    default:
            return "mandatory";
        }
    }

    /**
     * Return the widest instance set.  Widest as in has the most elements.
     * For instance, TYPE_0_N is widder than TYPE_1.
     */
    static public int widestInstance(int instance1, int instance2) {
        if (instance1 == TYPE_0_N || instance2 == TYPE_0_N)
            return TYPE_0_N;
        if (instance1 == TYPE_1_N || instance2 == TYPE_1_N)
            return TYPE_1_N;
        if (instance1 == TYPE_0_1 || instance2 == TYPE_0_1)
            return TYPE_0_1;
        return instance1;
    }
}
