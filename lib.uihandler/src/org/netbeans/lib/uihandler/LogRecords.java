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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Can persist and read log records from streams.
 *
 * @author Jaroslav Tulach
 */
public final class LogRecords {
    private LogRecords() {
    }

    private static final Logger LOG = Logger.getLogger(LogRecords.class.getName());
    
    private static final Formatter FORMATTER = new LogFormatter();
  
    /** Inspects the log record and decorates its content.
     * @param r the log record
     * @param d callback to be called with inspected values
     */
    public static void decorate(LogRecord r, Decorable d) {
        Decorations.decorate(r, d);
    }
    
    public static void write(OutputStream os, LogRecord rec) throws IOException {           
        String formated = FORMATTER.format(rec);
        byte[] arr = formated.getBytes("utf-8");
        os.write(arr);
    }

    public static void scan(InputStream is, Handler h) throws IOException {
        PushbackInputStream wrap = new PushbackInputStream(is, 32);
        byte[] arr = new byte[5];
        int len = wrap.read(arr);
        if (len == -1) {
            return;
        }
        wrap.unread(arr, 0, len);
        if (arr[0] == 0x1f && arr[1] == -117) {
            wrap = new PushbackInputStream(new GZIPInputStream(wrap), 32);
            len = wrap.read(arr);
            if (len == -1) {
                return;
            }
            wrap.unread(arr, 0, len);
        }
        
        if (arr[0] == '<' &&
            arr[1] == '?' &&
            arr[2] == 'x' &&
            arr[3] == 'm' &&
            arr[4] == 'l'
        ) {
            is = wrap;
        } else {
            ByteArrayInputStream header = new ByteArrayInputStream(
    "<?xml version='1.0' encoding='UTF-8'?><uigestures version='1.0'>".getBytes()
            );
            ByteArrayInputStream footer = new ByteArrayInputStream(
                "</uigestures>".getBytes()
            );
            is = new SequenceInputStream(
                new SequenceInputStream(header, wrap),
                footer
            );
        }
        
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        SAXParser p;
        try {
            f.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true); // NOI18N
            p = f.newSAXParser();
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        }
        
        Parser parser = new Parser(h);
        try {
            p.parse(is, parser);
        } catch (SAXException ex) {
            LOG.log(Level.WARNING, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        } catch (InternalError error){
            LOG.log(Level.WARNING, "INPUT FILE CORRUPTION", error);
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "INPUT FILE CORRUPTION", ex);
        }
    }   

    static Level parseLevel(String lev) {
        return "USER".equals(lev) ? Level.SEVERE : Level.parse(lev);
    }
    
    private static final class Parser extends DefaultHandler {
        private Handler callback;
        private static enum Elem {
            UIGESTURES, RECORD, DATE, MILLIS, SEQUENCE, LEVEL, THREAD,
            MESSAGE, KEY, PARAM, FRAME, CLASS, METHOD, LOGGER, EXCEPTION, LINE,
            CATALOG, MORE, FILE;
            
            public String parse(Map<Elem,String> values) {
                String v = values.get(this);
                return v;
            }
        }
        private Map<Elem,String> values = new EnumMap<Elem,String>(Elem.class);
        private Elem current;
        private FakeException currentEx;
        private Queue<FakeException> exceptions;
        private List<String> params;
        private StringBuilder chars = new StringBuilder();
        private int fatalErrors;
        
        public Parser(Handler c) {
            this.callback = c;
        }
        
        
        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
            callback.flush();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "uri: {0} localName: {1} qName: {2} atts: {3}", new Object[] { uri, localName, qName, atts });
            }

            try {
                current = Elem.valueOf(qName.toUpperCase());
                if (current == Elem.EXCEPTION) {
                    currentEx = new FakeException(new EnumMap<Elem,String>(values));
                }
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, "Uknown tag " + qName, ex);
                current = null;
            }
            chars = new StringBuilder();
        }
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (current != null) {
                String v = chars.toString();
                values.put(current, v);
                if (current == Elem.PARAM) {
                    if (params == null) {
                        params = new ArrayList<String>();
                    }
                    params.add(v);
                    if (params.size() > 1500) {
                        LOG.severe("Too long params when reading a record. Deleting few. Msg: " + Elem.MESSAGE.parse(values)); // NOI18N
                        for (String p : params) {
                            LOG.fine(p);
                        }
                        params.clear();
                    }
                }
            }
            current = null;
            chars = new StringBuilder();
            
            if (currentEx != null && currentEx.values != null) {
                if ("frame".equals(qName)) { // NOI18N
                    String line = Elem.LINE.parse(values);
                    StackTraceElement elem = new StackTraceElement(
                            Elem.CLASS.parse(values),
                            Elem.METHOD.parse(values),
                            Elem.FILE.parse(values),
                            line == null ? -1 : Integer.parseInt(line)
                            );
                    currentEx.trace.add(elem);
                    values.remove(Elem.CLASS);
                    values.remove(Elem.METHOD);
                    values.remove(Elem.LINE);
                }
                if ("exception".equals(qName)) {
                    currentEx.message = values.get(Elem.MESSAGE);
                    String more = values.get(Elem.MORE);
                    if (more != null) currentEx.more = Integer.parseInt(more);
                    if (exceptions == null){
                        exceptions = new LinkedList<FakeException>();
                    }
                    exceptions.add(currentEx);
                    values = currentEx.values;
                    currentEx = null;
                }
                return;
            }
            
            if ("record".equals(qName)) { // NOI18N
                String millis = Elem.MILLIS.parse(values);
                String seq = Elem.SEQUENCE.parse(values);
                String lev = Elem.LEVEL.parse(values);
                String thread = Elem.THREAD.parse(values);
                String msg = Elem.MESSAGE.parse(values);
                String key = Elem.KEY.parse(values);
                String catalog = Elem.CATALOG.parse(values);
                
                if (lev != null) {
                    LogRecord r = new LogRecord(parseLevel(lev), key != null && catalog != null ? key : msg);
                    try {
                        r.setThreadID(Integer.parseInt(thread));
                    } catch (NumberFormatException ex) {
                        LOG.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    r.setSequenceNumber(Long.parseLong(seq));
                    r.setMillis(Long.parseLong(millis));
                    r.setResourceBundleName(key);
                    if (catalog != null && key != null) {
                        r.setResourceBundleName(catalog);
                        if (!"<null>".equals(catalog)) { // NOI18N
                            try {
                                ResourceBundle b = NbBundle.getBundle(catalog);
                                b.getObject(key);
                                // ok, the key is there
                                r.setResourceBundle(b);
                            } catch (MissingResourceException e) {
                                LOG.log(Level.CONFIG, "Cannot find resource bundle {0} for key {1}", new Object[] { catalog, key });
                                r.setResourceBundle(new FakeBundle(key, msg));
                            }
                        } else {
                            LOG.log(Level.CONFIG, "Cannot find resource bundle <null> for key {1}", key);
                        }
                    }
                    if (params != null) {
                        r.setParameters(params.toArray());
                    }
                    if (exceptions != null) {
                        r.setThrown(createThrown(null));
                        // exceptions = null;  should be empty after poll
                    }

                    callback.publish(r);
                }

                currentEx = null;
                params = null;
                values.clear();
            }
            
        }
        
        /** set first element of exceptions as a result of this calling and
         * recursively fill it's cause
         */
        private FakeException createThrown(FakeException last){
            if (exceptions.size()==0) {
                return null;
            }
            FakeException result = exceptions.poll();
            if ((result!= null) && (result.getMore()!= 0)){
                assert last != null : "IF MORE IS NOT 0, LAST MUST BE SET NOT NULL";
                StackTraceElement[] trace = last.getStackTrace();
                for (int i = trace.length - result.getMore(); i < trace.length; i++){
                    result.trace.add(trace[i]);// fill the rest of stacktrace
                }
            }
            FakeException cause = createThrown(result);
            result.initCause(cause);
            return result;
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            chars.append(ch, start, length);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }

        public void fatalError(SAXParseException e) throws SAXException {
            if (fatalErrors++ > 1000) {
                throw e;
            }
        }
        
    }
    
    private static final class FakeBundle extends ResourceBundle {
        private String key;
        private String value;
         
        public FakeBundle(String key, String value) {
            this.key = key;
            this.value = value;
        }

    
        protected Object handleGetObject(String arg0) {
            if (key.equals(arg0)) {
                return value;
            } else {
                return null;
            }
        }

        public Enumeration<String> getKeys() {
            return Collections.enumeration(Collections.singleton(key));
        }
    } // end of FakeBundle
    
    private static final class FakeException extends Exception {
        final List<StackTraceElement> trace = new ArrayList<StackTraceElement>();
        Map<Parser.Elem,String> values;
        String message;
        int more;
        
        public FakeException(Map<Parser.Elem,String> values) {
            this.values = values;
            more = 0;
        }
       
        public StackTraceElement[] getStackTrace() {
            return trace.toArray(new StackTraceElement[0]);
        }

        public String getMessage() {
            return message;
        }
        
        public int getMore(){
            return more;
        }
        
        /**
         * org.netbeans.lib.uihandler.LogRecords$FakeException: NullPointerException ...
         * is not the best message - it's better to suppress FakeException
         */
        public String toString(){
            return message;
        }
        
    } // end of FakeException
}
