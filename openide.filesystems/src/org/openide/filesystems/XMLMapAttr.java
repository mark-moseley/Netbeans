/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.filesystems;

import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.io.NbObjectInputStream;

import java.io.*;

import java.lang.reflect.*;

import java.net.URL;

import java.util.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *Holds in Map attributes: Map(String attrName,XMLMapAttr.Attr attribute). This map holds all atributes for one FileObject.
 *<BR><BR>
 *<H3>Detailed description</H3>
 * Each file object (file or folder element) can have 0..* attributes.<BR> <BR>
 * Each file object <I>atrribute</I> (attribute is here name of element) must have two attributes (here XML attribute).<BR>
 * <OL>
 * <LI>First attribute name is <I>id</I> , which is mandatory and value of this
 * attribute serve as identifier to distinguish many attributes for one file object.
 * Name of attribute can contain prefix <code>transient:</code>. Transient means that such
 * marked attribute won`t be copied together with FileObject. Be aware that for:
 * fo.setAttribute("transient:foo", "bar") is true that fo.getAttribute("foo").equals("bar")
 * <LI> Second attribute is also mandatory, but you can choose such attribute name and
 * attribute value, which correspond to desirable data type
 * (e.g. <I>stringValue</I>, <I>boolValue</I> etc.).
 * </OL>
 * Desirable data type can be one of primitive data types or object data types.
 * <BR>
 * <BR>
 * Moreover value of attribute can be passed:
 * <OL>
 * <LI><I>statically</I> - means that you would be able to use literal of primitive data types (e.g. <I>stringvalue</I>="This is a literal",<I>boolvalue</I>="true" etc.).
 * If you want statically create instance of object data type you can use <I>serialValue</I>, which value is serialized byte stream (e.g.: <I>serialvalue</I>="092A54....").
 * This should ensure back compatibility.
 * <LI><I>dynamically</I> -means that instead of constant value (literal), you pass name of class including name of method, which will be used for dynamic creation of desirable object
 * (e.g.: <I>methodvalue</I>="org.openide.mypackage.MyClass.myMethod"). For dynamic creation of primitive data types could be used methods that return wrapper objects.
 * Implemetation of interface Attr will pass to method <I>myMethod</I> two parameters FileObject (file object which maintain this atrribute) and String (name of this attribute).
 * So here is sugestion of declaration of such method: <I>public static Object myMethod(FileObject myFo,String myName)</I>.
 *
 * <A NAME="primitive"><H4>Primitive data types</H4>
 * </A>
 * Here is sugested list of attribute names for primitive data types
 * (I expect that from the name is obvious which type of value is expected):
 * <OL>
 * <I>
 * <LI> bytevalue
 * <LI> shortvalue
 * <LI> intvalue
 * <LI> longvalue
 * <LI> floatvalue
 * <LI> doublevalue
 * <LI> boolvalue
 * <LI> charvalue
 * </I>
 * </OL>
 * <BR><BR>
 *<A NAME="object"><H4>Object data types</H4></A>
 * <OL>
 * <LI> <I>methodvalue</I> - dynamic creation (for primitive data could be returned wrapper objects)
  <LI> <I>newvalue</I> - newInstance is called
 * <LI> <I>serialValue</I> - static creation
 *
 * </OL>
  <BR><BR>
 * Attributes are stored in xml file, then there must be used encoding for not permitted
 * chars.  There are used Java-style <code>&#92;uXXXX</code> Unicode escapes for ISO control characters and
 * minimal set of character entities <code>&lt;</code>, <code>&amp;</code>, <code>'</code>
 * and <code>"</code>.
 *
 * @author rmatous
 */
@SuppressWarnings("unchecked")
final class XMLMapAttr implements Map {
    Map/*<String,Attr>*/ map;

    /** Creates new XMLMapAttr and delegetaor is instanced */
    public XMLMapAttr() {
        this.map = new HashMap(5);
    }

    static Attr createAttributeAndDecode(String key, String value) {
        if (Attr.isValid(key) == Attr.isValid("stringvalue")) { // NOI18N
            value = Attr.decode(value);
        }

        return new Attr(key, value);
    }

    static Attr createAttribute(int index, String value) {
        return new Attr(index, value);
    }

    /** According to name of attribute returns attribute as object
    * @param p1 is name of attribute
    * @return attribute, which is hold in XMLMapAttr.Attr or null if such attribute doesn`t exist or isn`t able to construct form String representation
    */
    public Object get(final Object p1) {
        Object obj;

        try {
            obj = getAttribute(p1);
        } catch (Exception e) {
            obj = null;
            ExternalUtil.exception(e);
        }

        return obj;
    }

    /** According to name of attribute returns attribute as object
     * @param params has sense only for methodvalue invocation; and only 2 parametres will be used
     * @return attribute, which is hold in XMLMapAttr.Attr or null if such attribute doesn`t exist or isn`t able to construct form String representation
     */
    public Object get(final Object p1, Object[] params) {
        Object obj;

        try {
            obj = getAttribute(p1, params);
        } catch (Exception e) {
            obj = null;
            ExternalUtil.exception(e);
        }

        return obj;
    }

    /** implementation of Map.get. But fires Exception to have chance in
     * DefaultAttributes to catch and annotate*/
    Object getAttribute(Object attrName) throws Exception {
        return getAttribute(attrName, null);
    }

    private Object getAttribute(Object attrName, Object[] params)
    throws Exception {
        Attr attr;
        String origAttrName = (String) attrName;
        Object[] keyValuePair = ModifiedAttribute.translateInto((String) attrName, null);
        attrName = (String) keyValuePair[0];

        synchronized (this) {
            attr = (Attr) map.get(attrName);
        }

        Object retVal = null;

        try {
            retVal = (attr == null) ? attr : attr.get(params);
        } catch (Exception e) {
            ExternalUtil.annotate(e, "attrName = " + attrName); //NOI18N                                                 
            throw e;
        }

        if (retVal instanceof ModifiedAttribute) {
            Object res = ((ModifiedAttribute) retVal).getValue(origAttrName);

            if (res instanceof Attr) {
                return ((Attr) res).get(params);
            } else {
                return res;
            }
        }

        return retVal;
    }

    /**
     * @param p1 is name of attribute
     * @param p2 is attribute as object
     * @return previous value associated with specified key, or null if there was no mapping for key.
     * A null return can also indicate that the HashMap previously associated null with the specified key.
     */
    public synchronized Object put(final Object p1, final Object p2) {
        return put(p1, p2, true);
    }

    synchronized Object put(final Object p1, final Object p2, boolean decode) {
        if ((p1 == null) || !(p1 instanceof String)) {
            return null;
        }

        Object[] keyValuePair = ModifiedAttribute.translateInto((String) p1, p2);
        String key = (String) keyValuePair[0];
        Object value = keyValuePair[1];
        Object toStore = ((value == null) || value instanceof Attr) ? value : new Attr(value);

        if (decode) {
            key = Attr.decode(key).intern();
        }

        return map.put(key, toStore);
    }

    /**
     * Writes heading to XML file
     * @param pw where to write
     */
    public static void writeHeading(PrintWriter pw) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
        pw.println(
            "<!DOCTYPE attributes PUBLIC \"-//NetBeans//DTD DefaultAttributes 1.0//EN\" \"http://www.netbeans.org/dtds/attributes-1_0.dtd\">"
        ); //NOI18N
        pw.println("<attributes version=\"1.0\">"); // NOI18N
    }

    /**
     * Writes ending to XML file
     * @param pw where to write
     */
    public static void writeEnding(PrintWriter pw) {
        pw.println("</attributes>"); // NOI18N        
    }

    /**
     * Writes all attributes for one FileObject with fileName
     * @param pw where to write
     * @param fileName
     * @param blockPrefix is prefix which is used before each line
     */
    public synchronized void write(PrintWriter pw, final String fileName, String blockPrefix) {
        boolean isHeadingWr = false;

        if (isEmpty()) {
            return;
        }

        //pw.println(blockPrefix+"<fileobject name=\""+fileName+"\">");// NOI18N
        SortedSet<String> attrNames = new TreeSet<String>();
        Iterator entryIter = map.entrySet().iterator();

        while (entryIter.hasNext()) {
            Map.Entry entry = (Map.Entry) entryIter.next();

            String attrName = (String) entry.getKey();
            Attr attr = (Attr) entry.getValue();

            if ((attrName == null) || (attr == null) || (attrName.length() == 0) || (attr.isValid() == -1)) {
                if ((attrName != null) && (attrName.length() != 0) && ((attr == null) || (attr.isValid() == -1))) {
                    entryIter.remove();
                }

                continue;
            }

            attrNames.add(attrName);
        }

        entryIter = attrNames.iterator();

        while (entryIter.hasNext()) {
            String attrName = (String) entryIter.next();
            Attr attr = (Attr) map.get(attrName);

            if (attr != null) {
                attr.transformMe();
            }

            if (!isHeadingWr) {
                isHeadingWr = true;

                String quotedFileName = fileName;

                try {
                    quotedFileName = org.openide.xml.XMLUtil.toAttributeValue(fileName);
                } catch (IOException ignore) {
                }

                pw.println(blockPrefix + "<fileobject name=\"" + quotedFileName + "\">"); // NOI18N                
            }

            pw.println(
                blockPrefix + blockPrefix + "<attr name=\"" + attr.getAttrNameForPrint(attrName) + "\" " +
                attr.getKeyForPrint() + "=\"" + attr.getValueForPrint() + "\"/>"
            ); // NOI18N
            attr.maybeAddSerValueComment(pw, blockPrefix + blockPrefix);
        }

        if (isHeadingWr) {
            pw.println(blockPrefix + "</fileobject>"); // NOI18N        
        }
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized Object remove(Object p1) {
        return map.remove(p1);
    }

    public synchronized boolean containsValue(Object p1) {
        return map.containsValue(p1);
    }

    @Override
    public synchronized int hashCode() {
        return map.hashCode();
    }

    public synchronized java.util.Set<String> keySet() {
        return map.keySet();
    }

    public synchronized java.util.Collection values() {
        return map.values();
    }

    // XXX this is wrong - values not translated
    public synchronized java.util.Set entrySet() {
        return map.entrySet();
    }

    public synchronized void putAll(java.util.Map p1) {
        map.putAll(p1);
    }

    public synchronized boolean containsKey(Object p1) {
        return map.containsKey(p1);
    }

    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public synchronized boolean equals(Object p1) {
        if (p1 instanceof Map) {
            return map.equals(p1);
        } else {
            return false;
        }
    }

    public synchronized int size() {
        return map.size();
    }

    /**
     * Holds textual representation of one attribute. And on request construct new instance of
     * attribute a returns it as Object. Each Attr contains pair key and value. Key is type. Value is real value (in textual form) of this type.
     * Detailed describtion is in <A HREF="XMLMapAttr.html">XMLMapAttr<A>
     */
    final static class Attr extends java.lang.Object {
        // static final long serialVersionUID = -62733358015297232L;
        private static final String[] ALLOWED_ATTR_KEYS = {
            "bytevalue", "shortvalue", "intvalue", "longvalue", "floatvalue", "doublevalue", "boolvalue", "charvalue",
            "stringvalue", "methodvalue", "serialvalue", "urlvalue", "newvalue", "bundlevalue"
        }; // NOI18N
        private String value;
        private int keyIndex;
        private Object obj; //back compatibility

        private Attr(Object obj) {
            this.obj = obj;
        }

        private Attr(int index, String value) {
            keyIndex = index;
            this.value = (value != null) ? value.intern() : null;
        }

        /**
         * @param key One of the possible keys:
         * "bytevalue","shortvalue","intvalue","longvalue","floatvalue","doublevalue","boolvalue","charvalue","stringvalue","methodvalue","serialvalue","urlvalue"
         * @param value Corresponding value to key in textual form.
         */
        private Attr(String key, String value) {
            keyIndex = isValid(key);
            this.value = value.intern();
        }

        /**
         * @return array of Strings. Each String is textual form of allowed type of attribute - textual form of key
         */
        static String[] getAttrTypes() {
            return ALLOWED_ATTR_KEYS;
        }

        /**
         * Checks if key is valid and sets key and value
         * @param key Key of attribute. Defines type of attribute in textual form.
         * @param value Value of attribute. Defines value of attribute as literal or  HEX expression of serialization.
         */
        private final void putEntry(String key, String value) {
            this.keyIndex = isValid(key);
            this.value = value.intern();
        }

        /**
         *  added for future use - convert NbMarshalledObject to primitive data types and other supported types (if possible)
         */
        static Object unMarshallObjectRecursively(Object mo) {
            Object o = mo;

            while (o instanceof NbMarshalledObject) {
                try {
                    o = ((NbMarshalledObject) o).get();
                } catch (IOException e) {
                    ExternalUtil.exception(e);

                    return mo;
                } catch (ClassNotFoundException e) {
                    ExternalUtil.exception(e);

                    return mo;
                }
            }

            return (o == null) ? mo : o;
        }

        /**Method for back compatibility; called in write*/
        private void transformMe() {
            int objType;

            if (obj == null) {
                return;
            }

            Object unObj = unMarshallObjectRecursively(obj);

            if (unObj != null) {
                if ((objType = XMLMapAttr.Attr.distinguishObject(unObj)) != XMLMapAttr.Attr.isValid("SERIALVALUE")) { // NOI18N
                    obj = null;
                    putEntry(ALLOWED_ATTR_KEYS[objType], unObj.toString());
                } else {
                    String newValue;

                    try {
                        newValue = encodeValue(unObj);
                    } catch (IOException iox) {
                        return;
                    }

                    obj = null;
                    putEntry(ALLOWED_ATTR_KEYS[objType], newValue);
                }
            }
        }

        /**
         *  added for future use - convert NbMarshalledObject to primitive data types and other supported types (if possible)
         */
        static int distinguishObject(Object o) {
            if (o instanceof Byte) {
                return isValid("BYTEVALUE"); // NOI18N
            }

            if (o instanceof Short) {
                return isValid("SHORTVALUE"); // NOI18N        
            }

            if (o instanceof Integer) {
                return isValid("INTVALUE"); // NOI18N 
            }

            if (o instanceof Long) {
                return isValid("LONGVALUE"); // NOI18N        
            }

            if (o instanceof Float) {
                return isValid("FLOATVALUE"); // NOI18N
            }

            if (o instanceof Double) {
                return isValid("DOUBLEVALUE"); // NOI18N
            }

            if (o instanceof Boolean) {
                return isValid("BOOLVALUE"); // NOI18N
            }

            if (o instanceof Character) {
                return isValid("CHARVALUE"); // NOI18N
            }

            if (o instanceof String) {
                return isValid("STRINGVALUE"); // NOI18N         
            }

            if (o instanceof URL) {
                return isValid("URLVALUE"); // NOI18N            
            }

            return isValid("SERIALVALUE"); // NOI18N
        }

        static String encode(String inStr) {
            try {
                inStr = org.openide.xml.XMLUtil.toAttributeValue(inStr);
            } catch (Exception ignore) {
            }

            StringBuffer outStr = new StringBuffer(6 * inStr.length());

            for (int i = 0; i < inStr.length(); i++) {
                if (Character.isISOControl(inStr.charAt(i)) || isEncodedChar(i, inStr)) {
                    outStr.append(encodeChar(inStr.charAt(i)));

                    continue;
                }

                outStr.append(inStr.charAt(i));
            }

            return outStr.toString();
        }

        static String encodeChar(char ch) {
            String encChar = Integer.toString((int) ch, 16);

            return "\\u" + "0000".substring(0, "0000".length() - encChar.length()).concat(encChar); // NOI18N
        }

        static String decode(final String inStr) {
            StringBuffer outStr = new StringBuffer(inStr.length());

            try {
                for (int i = 0; i < inStr.length(); i++) {
                    if (isEncodedChar(i, inStr)) {
                        String decChar = inStr.substring(i + 2, i + 6);
                        outStr.append((char) Integer.parseInt(decChar, 16));
                        i += 5;
                    } else {
                        outStr.append(inStr.charAt(i));
                    }
                }
            } catch (NumberFormatException e) {
                Exceptions.printStackTrace(e);

                return inStr;
            }

            return outStr.toString();
        }

        private static boolean isEncodedChar(final int currentPosition, final String inStr) {
            boolean isEncodedChar = (currentPosition + 5) < inStr.length();

            if (isEncodedChar) {
                isEncodedChar &= ((inStr.charAt(currentPosition) == '\\') &&
                (inStr.charAt(currentPosition + 1) == 'u'));

                for (int i = currentPosition + 2; isEncodedChar && (i < (currentPosition + 6)); i++) {
                    char c = inStr.charAt(i);
                    isEncodedChar &= (Character.digit(c, 16) != -1);
                }
            }

            return isEncodedChar;
        }

        /**
        * Constructs new attribute as Object. Used for static creation from literal or serialValue.
        * @return new attribute as Object
        */
        private Object get() throws Exception {
            return getObject(null); //getObject is ready to aobtain null
        }

        /**
         * Constructs new attribute as Object. Used for dynamic creation: methodvalue .
         * @param objs has sense only for methodvalue invocation; and only 2 parametres will be used
         *@return new attribute as Object
         */
        private Object get(Object[] objs) throws Exception {
            return getObject(objs);
        }

        /**
         * @return key. Key expresses type of this attribute (in textual form) or "" if internal error.
         */
        final String getKey() {
            String[] keyArray = getAttrTypes();

            if (obj != null) {
                return "serialvalue"; //back compatibility // NOI18N
            }

            if (isValid() == -1) {
                return ""; // NOI18N
            }

            return keyArray[keyIndex];
        }

        /**
         * @return value in textual format or "" if internal error.
         */
        final String getValue() {
            if (obj != null) {
                getValue(obj);
            }

            return (value != null) ? value : ""; // NOI18N
        }

        static final String getValue(Object obj) {
            try {
                return encodeValue(obj); //back compatibility
            } catch (IOException ioe) {
                return ""; // NOI18N
            }
        }

        final String getValueForPrint() {
            if (obj != null) {
                Attr modifAttr = null;

                if (obj instanceof ModifiedAttribute) {
                    modifAttr = (Attr) ((ModifiedAttribute) obj).getValue();
                }

                return (modifAttr != null) ? encode(modifAttr.getValue()) : encode(getValue());
            }

            return (value != null) ? encode(value) : ""; // NOI18N
        }

        final String getKeyForPrint() {
            if ((obj != null) && obj instanceof ModifiedAttribute) {
                Attr modifAttr = (Attr) ((ModifiedAttribute) obj).getValue();
                int keyIdx = Attr.isValid("SERIALVALUE"); //NOI18N

                if (modifAttr != null) {
                    keyIdx = distinguishObject(modifAttr.getValue());
                }

                String[] keyArray = getAttrTypes();

                return keyArray[keyIdx];
            }

            return getKey();
        }

        final String getAttrNameForPrint(String attrName) {
            if ((obj != null) && obj instanceof ModifiedAttribute) {
                Object[] retVal = ModifiedAttribute.revert(attrName, obj);

                return encode((String) retVal[0]);
            }

            return encode(attrName);
        }

        final void maybeAddSerValueComment(PrintWriter pw, String indent) {
            if (obj != null) {
                Object modifObj = null;

                if (obj instanceof ModifiedAttribute) {
                    modifObj = ((Attr) ((ModifiedAttribute) obj).getValue()).getValue();

                    if (distinguishObject(modifObj) != Attr.isValid("SERIALVALUE")) { //NOI18N

                        return;
                    }
                }

                // Important for debugging to know what this stuff really is.
                // Note this comment is only written to disk when the attr is
                // first saved; after that successive saves will just know the
                // ser value and will not print the comment. So look at .nbattrs
                // immediately after setting something serialized. --jglick
                pw.print(indent);
                pw.print("<!-- "); // NOI18N

                String s = (modifObj != null) ? modifObj.toString() : obj.toString();

                if (s.indexOf("--") != -1) { // NOI18N

                    // XML comment no-no.
                    s = s.replace('-', '_'); // NOI18N
                }

                pw.print(s);
                pw.println(" -->"); // NOI18N
            }
        }

        /**
         * Creates serialized object, which was encoded in HEX format
         * @param value Encoded serialized object in HEX format
         * @return Created object from encoded HEX format
         * @throws IOException
         */
        static Object decodeValue(String value) throws IOException {
            if ((value == null) || (value.length() == 0)) {
                return null;
            }

            byte[] bytes = new byte[value.length() / 2];
            int tempI;
            int count = 0;

            for (int i = 0; i < value.length(); i += 2) {
                try {
                    tempI = Integer.parseInt(value.substring(i, i + 2), 16);

                    if (tempI > 127) {
                        tempI -= 256;
                    }

                    bytes[count++] = (byte) tempI;
                } catch (NumberFormatException e) {
                    throw (IOException) ExternalUtil.copyAnnotation(new IOException(), e);
                }
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);

            try {
                ObjectInputStream ois = new NbObjectInputStream(bis);
                Object ret = ois.readObject();

                return ret;
            } catch (Exception e) {
                throw (IOException) ExternalUtil.copyAnnotation(new IOException(), e);
            }

            /*unreachable code*/

            //throw new InternalError ();
        }

        /**
         * Encodes Object into String encoded in HEX format
         * @param value Object, which will be encoded
         * @return  serialized Object in String encoded in HEX format
         * @throws IOException
         */
        static String encodeValue(Object value) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(value);
                oos.close();
            } catch (Exception e) {
                throw (IOException) ExternalUtil.copyAnnotation(new IOException(), e);
            }

            byte[] bArray = bos.toByteArray();
            StringBuffer strBuff = new StringBuffer(bArray.length * 2);

            for (int i = 0; i < bArray.length; i++) {
                if ((bArray[i] < 16) && (bArray[i] >= 0)) {
                    strBuff.append("0"); // NOI18N
                }

                strBuff.append(Integer.toHexString((bArray[i] < 0) ? (bArray[i] + 256) : bArray[i]));
            }

            return strBuff.toString();
        }

        /**
         * Encodes Object into String encoded in HEX format
         * @param params Array (2 length) of objects ( Object[] o = {fo,name}).  Attribute is assigned to some fo-FileObject and has its name-String.
         * params can be null.
         * @return   Object or null
         */
        private Object getObject(Object[] params) throws Exception {
            int index;

            if (obj != null) {
                return obj; //back compatibility
            }

            if ((index = isValid()) != -1) {
                try {
                    switch (index) {
                    case 0:
                        return new Byte(value);

                    case 1:
                        return new Short(value);

                    case 2:
                        return new Integer(value); //(objI);

                    case 3:
                        return new Long(value);

                    case 4:
                        return new Float(value);

                    case 5:
                        return new Double(value);

                    case 6:
                        return Boolean.valueOf(value);

                    case 7:

                        if (value.trim().length() != 1) {
                            break;
                        }

                        return new Character(value.charAt(0));

                    case 8:
                        return value;

                    case 9:
                        return methodValue(value, params);

                    case 10:
                        return decodeValue(value);

                    case 11:
                        return new URL(value);

                    case 12:

                        // special support for singletons
                        Class cls = ExternalUtil.findClass(Utilities.translate(value));

                        if (SharedClassObject.class.isAssignableFrom(cls)) {
                            return SharedClassObject.findObject(cls, true);
                        } else {
                            return cls.newInstance();
                        }
                    case 13:
                        String[] arr = value.split("#", 2); // NOI18N
                        return NbBundle.getBundle(arr[0]).getObject(arr[1]);
                    }
                } catch (Exception exc) {
                    ExternalUtil.annotate(exc, "value = " + value); //NOI18N
                    throw exc;
                } catch (LinkageError e) {
                    throw (ClassNotFoundException) ExternalUtil.annotate(new ClassNotFoundException(value), e);
                }
            }

            throw new InstantiationException(value);
        }

        /** Constructs new attribute as Object. Used for dynamic creation: methodvalue .
         * @param params only 2 parametres will be used
         * @return   Object or null
         */
        private final Object methodValue(String value, Object[] params)
        throws Exception {
            int sepIdx = value.lastIndexOf('.');

            if (sepIdx != -1) {
                String methodName = value.substring(sepIdx + 1);
                Class cls = ExternalUtil.findClass(value.substring(0, sepIdx));
                FileObject fo = null;
                String attrName = null;

                for (int i = 0; i < params.length; i++) {
                    if ((fo == null) && params[i] instanceof FileObject) {
                        fo = (FileObject) params[i];
                    }

                    if ((attrName == null) && params[i] instanceof String) {
                        attrName = (String) params[i];
                    }
                }

                Class[][] paramArray = {
                        { FileObject.class, String.class }, { String.class, FileObject.class },
                        { FileObject.class }, { String.class }, {  },
                        { Map.class, String.class }, { Map.class },
                    };

                boolean both = ((fo != null) && (attrName != null));
                Object[] objectsList = new Object[7];
                objectsList[0] = (both) ? new Object[] { fo, attrName } : null;
                objectsList[1] = (both) ? new Object[] { attrName, fo } : null;
                objectsList[2] = (fo != null) ? new Object[] { fo } : null;
                objectsList[3] = (attrName != null) ? new Object[] { attrName } : null;
                objectsList[4] = new Object[] {  };

                Map fileMap = wrapToMap(fo);
                objectsList[5] = attrName != null ? new Object[] { fileMap, attrName } : null;
                objectsList[6] = new Object[] { fileMap };

                for (int i = 0; i < paramArray.length; i++) {
                    Object[] objArray = (Object[]) objectsList[i];

                    if (objArray == null) {
                        continue;
                    }

                    try {
                        Method method = cls.getDeclaredMethod(methodName, paramArray[i]);

                        if (method != null) {
                            method.setAccessible(true);

                            return method.invoke(null, objArray);
                        }
                    } catch (NoSuchMethodException nsmExc) {
                        continue;
                    }
                }
            }

            throw new InstantiationException(value);
        }

        static final Map wrapToMap(FileObject fo) {
            return fo == null ? Collections.EMPTY_MAP : new FileMap(fo);
        }

        /**
         * Checks if key is valid
         * @return Index to array of allowed keys or -1 which means error.
         */
        final int isValid() {
            String[] keyArray = getAttrTypes();

            if (obj != null) {
                return isValid("SERIALVALUE"); //back compatibility // NOI18N
            }

            if ((keyIndex >= keyArray.length) || (keyIndex < 0)) {
                return -1;
            }

            return keyIndex;
        }

        /**
         * Checks if key is valid
         * @return Index to array of allowed keys or -1 which means error.
         */
        final static int isValid(String key) {
            int index = -1;
            int i;
            String[] strArray = getAttrTypes();
            String trimmedKey = key.trim();

            for (i = 0; i < strArray.length; i++) {
                if (trimmedKey.equalsIgnoreCase(strArray[i]) == true) {
                    index = i;

                    break;
                }
            }

            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Attr) {
                Attr other = (Attr)obj;
                
                if (other.keyIndex != keyIndex) {
                    return false;
                }
                
                return other.value.equals(value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 743 + keyIndex << 8 + value.hashCode();
        }
    }

    /**
     * Helper class for decorating attributes with modifiers.
     * Object that is made persistent using setAttribute can contain also modifiers.
     * This class is wrapper class that holds original object and its modifiers.
     * Intended as replacer of original class in attributes.
     * Currently exists only one modifier: tranisent.
     * Transient modifier means that such attribute won`t be copied with FileObject.
     */
    static class ModifiedAttribute implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 84214031923497718L;
        private final static String[] fragments = new String[] { "transient:" }; //NOI18N
        private int modifier = 0;
        private Object origAttrValue = null;

        /** Creates a new instance of AttributeFactory */
        private ModifiedAttribute(Object origAttrValue) {
            this.origAttrValue = origAttrValue;
        }

        /** This method looks for modifiers in attribute name (currently transient:).
         *
         * @param attrName original name of attribute
         * @param value original value - can be null
         * @return Object array with size 2.
         * If there are no modifiers in attribute name, then is returned Object array with
         * , where first is placed unchanged attribute name, and then uchanged value.
         * If there are modifiers in attribute name, then as attribute name is returned
         * stripped original attribute name (without modifiers) and ModifiedAttribute object,
         * that wraps original object and also contain modifiers.
         */
        static Object[] translateInto(String attrName, Object value) {
            String newAttrName = attrName;
            Object newValue = value;
            ModifiedAttribute attr = null;

            for (int i = 0; i < fragments.length; i++) {
                String fragment = fragments[i];
                int idx = newAttrName.indexOf(fragment);

                if (idx != -1) {
                    /** fragment is cleared away */
                    newAttrName = newAttrName.substring(0, idx) + newAttrName.substring(idx + fragment.length());

                    if (attr == null) {
                        newValue = attr = new ModifiedAttribute(value);
                    }

                    attr.modifier |= (1 << i); //set modifier
                }
            }

            return new Object[] { newAttrName, newValue };
        }

        /**
         * This method is opposite to method translateInto
         */
        static Object[] revert(String attrName, Object value) {
            if (!(value instanceof ModifiedAttribute) || (value == null)) {
                return new Object[] { attrName, value };
            }

            ModifiedAttribute attr = (ModifiedAttribute) value;
            String newAttrName = attrName;
            Object newValue = attr;

            for (int i = 0; i < fragments.length; i++) {
                String fragment = fragments[i];

                if (((attr.modifier & (1 << i)) != 0) && (fragment != null)) {
                    /** fragment is cleared away */
                    newAttrName = fragment + newAttrName;

                    if (newValue instanceof ModifiedAttribute) {
                        newValue = attr.origAttrValue;
                    }
                }
            }

            return new Object[] { newAttrName, newValue };
        }

        /** ModifiedAttribute holds original value + modifiers. This method returns original value.
         * @return If there are no modifiers in attribute name, then returns original value
         * If there are  modifiers in attribute name, then returns current instance of
         * ModifiedAttribute.
         */
        Object getValue(String attrName) {
            for (int i = 0; i < fragments.length; i++) {
                String fragment = fragments[i];
                int idx = attrName.indexOf(fragment);

                if (idx != -1) {
                    return this;
                }
            }

            return origAttrValue;
        }

        /** ModifiedAttribute holds original value + modifiers. This method returns original value.
         * @return then returns original value
         */
        Object getValue() {
            return getValue(""); //NOI18N
        }

        /**
         * Decides if value stored in attributes is transient
         * @param fo fileobject where attribute is looked for
         * @param attrName  name of attribute
         * @return true if transient
         */
        static boolean isTransient(FileObject fo, String attrName) {
            Object value = fo.getAttribute(fragments[0] + attrName);

            if (value instanceof ModifiedAttribute) {
                return ((((ModifiedAttribute) value).modifier & (1 << 0)) == 0) ? false : true;
            }

            return false;
        }
    }
    
    private static final class FileMap extends AbstractMap<String,Object> {
        private FileObject fo;
        
        private FileMap (FileObject fo) {
            this.fo = fo;
}
        
        public Set<Map.Entry<String,Object>> entrySet() {
            return new AttrFileSet(fo);
        }

        public Object get(String key) {
            return fo.getAttribute(key);
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }
        
    }
    private static final class AttrFileSet extends AbstractSet<Map.Entry<String,Object>> {
        private FileObject fo;
        
        private AttrFileSet(FileObject fo) {
            this.fo = fo;
        }
        
        public Iterator<Map.Entry<String, Object>> iterator() {
            class Iter implements Iterator<Map.Entry<String, Object>> {
                Enumeration<String> attrs = fo.getAttributes();
                
                public boolean hasNext() {
                    return attrs.hasMoreElements();
                }
                
                public Map.Entry<String, Object> next() {
                    String s = attrs.nextElement();
                    return new FOEntry(fo, s);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            }
            return new Iter();
        }

        public int size() {
            Enumeration<String> all = fo.getAttributes();
            int cnt = 0;
            while (all.hasMoreElements()) {
                cnt++;
                all.nextElement();
            }
            return cnt;
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    } // end of AttrFileSet
    
    private static final class FOEntry implements Map.Entry<String, Object> {
        private FileObject fo;
        private String attr;
        
        private FOEntry(FileObject fo, String attr) {
            this.fo = fo;
            this.attr = attr;
        }

        public String getKey() {
            return attr;
        }

        public Object getValue() {
            return fo.getAttribute(attr);
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    } // end of FOEntry
}
