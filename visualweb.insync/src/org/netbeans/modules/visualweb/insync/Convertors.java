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
package org.netbeans.modules.visualweb.insync;

import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;
import java.util.Iterator;

import javax.swing.text.Document;
import org.openide.util.NbBundle;

import org.xml.sax.InputSource;

import org.openide.ErrorManager;
import org.openide.util.Lookup;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Set of static methods converting misc data representations.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public final class Convertors {


    /**
     * @return current state of Document as string
     */
    public static String documentToString(final Document doc) {

        if (doc == null) throw new NullPointerException();

        final String[] str = new String[1];

        // safely take the text from the document
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };

        doc.render(run);
        return str[0];

    }

    /**
     * @return InputSource, a callie SHOULD set systemId if available
     */
    /*public static InputSource documentToInputSource(Document doc) {

        if (doc == null) throw new NullPointerException();

        String text = documentToString(doc);
        Reader reader = new StringReader(text);

        // our specifics property
        String system = (String) doc.getProperty(TextEditorSupport.PROP_DOCUMENT_URL);

        // try Swing general property
        if (system == null) {
            Object obj = doc.getProperty(Document.StreamDescriptionProperty);
            if (obj instanceof DataObject) {
                try {
                        DataObject dobj = (DataObject) obj;
                        FileObject fo = dobj.getPrimaryFile();
                        URL url = fo.getURL();
                        system = url.toExternalForm();
                } catch (IOException io) {
                    ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                    emgr.notify(io);
                }
            } else {
                ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                emgr.log("XML:Convertors:Unknown stream description:" + obj);
            }
        }

        // set something, some parsers are nervous if no system id
        if (system == null) {
            system = "XML/Core/Convertors/documentToInputSource()";  //NOI18N
        }

        InputSource in = new InputSource(system); // NOI18N
        in.setCharacterStream(reader);
        return in;
    }*/


    /**
     * Wrap reader into buffered one and start reading returning
     * String as a EOF is reached.
     */
    public static String readerToString(Reader reader) throws IOException {

        BufferedReader fastReader = new BufferedReader(reader);
        StringBuffer buf = new StringBuffer(1024);
        try {
            for (int i = fastReader.read(); i >= 0; i = fastReader.read()) {
                buf.append((char)i);
            }
        } catch (EOFException eof) {
            //expected
        }

        return buf.toString();
    }

    /**
     */
    public static final String iana2java (String iana) {
        String java = (String) Convertors.EncodingUtil.getIANA2JavaMap ().get (iana.toUpperCase ());
        return java == null ? iana : java;
    }
    
    public static final String java2iana (String java) {
        String iana = (String) Convertors.EncodingUtil.getJava2IANAMap ().get (java);
        return iana == null ? java : iana;
    }


    //!!! this code is copy pasted from TAX library TreeUtilities

    /**
     *
     */
    static class EncodingUtil {

        /** IANA to Java encoding mappings */
        protected final static Map encodingIANA2JavaMap = new TreeMap ();

        /** */
        protected final static Map encodingIANADescriptionMap = new TreeMap ();

        /** */
        protected final static Map encodingIANAAliasesMap = new TreeMap ();
        
        protected final static Map encodingJava2IANAMap = new TreeMap ();

        //
        // Static initialization
        //

        static {
            encodingIANA2JavaMap.put       ("BIG5", "Big5"); // NOI18N
            encodingIANADescriptionMap.put ("BIG5", getString ("NAME_BIG5")); // NOI18N
            encodingIANAAliasesMap.put     ("BIG5", "BIG5"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM037",       "CP037");  // NOI18N
            encodingIANADescriptionMap.put ("IBM037",       getString ("NAME_IBM037")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM037",       "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-US", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CA", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NL", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-WT", "IBM037"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM277",       "CP277");  // NOI18N
            encodingIANADescriptionMap.put ("IBM277",       getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM277",       "IBM277"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-DK", "IBM277"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NO", "IBM277"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM278",       "CP278");  // NOI18N
            encodingIANADescriptionMap.put ("IBM278",       getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM278",       "IBM278"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FI", "IBM278"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-SE", "IBM278"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM280",       "CP280");  // NOI18N
            encodingIANADescriptionMap.put ("IBM280",       getString ("NAME_IBM280")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM280",       "IBM280"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IT", "IBM280"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM284",       "CP284");  // NOI18N
            encodingIANADescriptionMap.put ("IBM284",       getString ("NAME_IBM284")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM284",       "IBM284"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ES", "IBM284"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM285",       "CP285");  // NOI18N
            encodingIANADescriptionMap.put ("IBM285",       getString ("NAME_IBM285")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM285",       "IBM285"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-GB", "IBM285"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM297",       "CP297");  // NOI18N
            encodingIANADescriptionMap.put ("IBM297",       getString ("NAME_IBM297")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM297",       "IBM297"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FR", "IBM297"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM424",       "CP424");  // NOI18N
            encodingIANADescriptionMap.put ("IBM424",       getString ("NAME_IBM424")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM424",       "IBM424"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-HE", "IBM424"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM500",       "CP500");  // NOI18N
            encodingIANADescriptionMap.put ("IBM500",       getString ("NAME_IBM500")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM500",       "IBM500"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CH", "IBM500"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-BE", "IBM500"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM870",   "CP870");  // NOI18N
            encodingIANADescriptionMap.put ("IBM870",   getString ("NAME_IBM870")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM870",   "IBM870"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ROECE", "IBM870"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-YU",    "IBM870"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM871",       "CP871");  // NOI18N
            encodingIANADescriptionMap.put ("IBM871",       getString ("NAME_IBM871")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM871",       "IBM871"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IS", "IBM871"); // NOI18N

            encodingIANA2JavaMap.put       ("IBM918", "CP918");  // NOI18N
            encodingIANADescriptionMap.put ("IBM918", getString ("NAME_IBM918")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM918", "IBM918"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-AR2", "IBM918"); // NOI18N

            encodingIANA2JavaMap.put       ("EUC-JP", "EUCJIS"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-JP", getString ("NAME_EUC-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-JP", "EUC-JP"); // NOI18N

            encodingIANA2JavaMap.put       ("EUC-KR", "KSC5601"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-KR", getString ("NAME_EUC-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-KR", "EUC-KR");  // NOI18N

            encodingIANA2JavaMap.put       ("GB2312", "GB2312"); // NOI18N
            encodingIANADescriptionMap.put ("GB2312", getString ("NAME_GB2312")); // NOI18N
            encodingIANAAliasesMap.put     ("GB2312", "GB2312"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-2022-JP", "JIS");  // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-JP", getString ("NAME_ISO-2022-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-JP", "ISO-2022-JP"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-2022-KR", "ISO2022KR");   // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-KR", getString ("NAME_ISO-2022-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-KR", "ISO-2022-KR"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-1", "8859_1");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-1", getString ("NAME_ISO-8859-1")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-1", "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN1",     "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("L1",  "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("IBM819",     "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("CP819",      "ISO-8859-1"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-2", "8859_2");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-2", getString ("NAME_ISO-8859-2")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-2", "ISO-8859-2"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN2",     "ISO-8859-2"); // NOI18N
            encodingIANAAliasesMap.put     ("L2",  "ISO-8859-2"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-3", "8859_3");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-3", getString ("NAME_ISO-8859-3")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-3", "ISO-8859-3"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN3",     "ISO-8859-3"); // NOI18N
            encodingIANAAliasesMap.put     ("L3",  "ISO-8859-3"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-4", "8859_4");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-4", getString ("NAME_ISO-8859-4")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-4", "ISO-8859-4"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN4",     "ISO-8859-4"); // NOI18N
            encodingIANAAliasesMap.put     ("L4",  "ISO-8859-4"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-5", "8859_5");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-5", getString ("NAME_ISO-8859-5")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-5", "ISO-8859-5"); // NOI18N
            encodingIANAAliasesMap.put     ("CYRILLIC",   "ISO-8859-5"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-6", "8859_6");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-6", getString ("NAME_ISO-8859-6")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-6", "ISO-8859-6"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-7", "8859_7");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-7", getString ("NAME_ISO-8859-7")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-7", "ISO-8859-7"); // NOI18N
            encodingIANAAliasesMap.put     ("GREEK",      "ISO-8859-7"); // NOI18N
            encodingIANAAliasesMap.put     ("GREEK8",     "ISO-8859-7"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-8", "8859_8");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-8", getString ("NAME_ISO-8859-8")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-8", "ISO-8859-8"); // NOI18N
            encodingIANAAliasesMap.put     ("HEBREW",     "ISO-8859-8"); // NOI18N

            encodingIANA2JavaMap.put       ("ISO-8859-9", "8859_9");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-9", getString ("NAME_ISO-8859-9")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-9", "ISO-8859-9"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN5",     "ISO-8859-9"); // NOI18N
            encodingIANAAliasesMap.put     ("L5",  "ISO-8859-9"); // NOI18N

            encodingIANA2JavaMap.put       ("KOI8-R", "KOI8_R"); // NOI18N
            encodingIANADescriptionMap.put ("KOI8-R", getString ("NAME_KOI8-R")); // NOI18N
            encodingIANAAliasesMap.put     ("KOI8-R", "KOI8-R"); // NOI18N

            encodingIANADescriptionMap.put ("US-ASCII",     getString ("NAME_ASCII")); // NOI18N
            encodingIANAAliasesMap.put     ("ASCII",     "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("US-ASCII",  "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("ISO646-US", "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("IBM367",    "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("CP367",     "US-ASCII");  // NOI18N

            encodingIANA2JavaMap.put       ("UTF-8", "UTF8");  // NOI18N
            encodingIANADescriptionMap.put ("UTF-8", getString ("NAME_UTF-8")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-8", "UTF-8"); // NOI18N

            encodingIANA2JavaMap.put       ("UTF-16", "Unicode"); // NOI18N
            encodingIANADescriptionMap.put ("UTF-16", getString ("NAME_UTF-16")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-16", "UTF-16");  // NOI18N
            
            
            Iterator iter = encodingIANA2JavaMap.keySet().iterator();
            Object key;
            while (iter.hasNext()){
                key = iter.next();
                encodingJava2IANAMap.put(encodingIANA2JavaMap.get(key), key);
            }
            
            encodingIANA2JavaMap.put       ("US-ASCII",     "8859_1"); // NOI18N    
        }

        /**
         * Get localized string from package bundle.
         * @param key Key identifing localized value.
         * @return localized value.
         */
        private static String getString(String key) {
            if (key == null) throw new NullPointerException();
            return NbBundle.getMessage(Convertors.class, key);
        }
    

        /**
         */
        public static Map getIANA2JavaMap () {
            return encodingIANA2JavaMap;
        }
        
        public static Map getJava2IANAMap () {
            return encodingJava2IANAMap;
        }

    } // end: class EncodingUtil

}
