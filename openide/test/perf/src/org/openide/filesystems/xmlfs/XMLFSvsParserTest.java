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

package org.openide.filesystems.xmlfs;

import java.io.*;
import java.net.URL;

import org.xml.sax.*;

import org.openide.filesystems.XMLFileSystem;



/** A test that should measure and compare the speed of parsing an
 * XML document to creation of new XMLFileSystem
 *
 * @author  Jaroslav Tulach
 */
public class XMLFSvsParserTest extends org.netbeans.performance.Benchmark 
implements EntityResolver {
    private static final String PUBLIC_ID = "-//NetBeans//DTD Filesystem 1.0//EN";// NOI18N
    private static final String DTD_PATH = "/org/openide/filesystems/filesystem.dtd";// NOI18N
    /** URL of a document to parse */
    private URL url;
    
    /** Constructor
     */
    public XMLFSvsParserTest (String name) {
        super (name, new Object[] {
            new Integer (100),
            new Integer (1000),
            new Integer (10000)
        });
    }
    
    /** A main method.
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new junit.framework.TestSuite (XMLFSvsParserTest.class));
    }
    
    /** Setups the test.
     */
    protected void setUp () throws Exception {
        File file = File.createTempFile ("afile", null);
        file.deleteOnExit ();
        
        Integer count = (Integer)getArgument ();
        
        prepareXmlFilesystem (file, count.intValue (), "anyfilewithanyvalues");
        
        url = file.toURL ();
    }
        
    
    /** Tests how quickly a parser can parse the file.
     */
    public void testXMLParser () throws Exception {
        int cnt = getIterationCount ();
        
        while (cnt-- > 0) {
            org.xml.sax.HandlerBase base = new org.xml.sax.HandlerBase ();
            javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();
            factory.setValidating (false);
            org.xml.sax.Parser parser = factory.newSAXParser ().getParser ();
            parser.setEntityResolver(this);            
            parser.setDocumentHandler (base);
            
            parser.parse (new InputSource (url.openStream ()));
        }
    }
        

    /** Tests how quickly an XML fs can be created.
     */
    public void testXMLFS () throws Exception {
        int cnt = getIterationCount ();
        while (cnt-- > 0) {
            XMLFileSystem fs = new XMLFileSystem (url);
        }
    }

    /** Implements entity resolver.
     */
    public InputSource resolveEntity(java.lang.String pid,java.lang.String sid) throws SAXException   {
        if (pid != null && pid.equals(PUBLIC_ID)) 
            return new InputSource (getClass ().getResourceAsStream (DTD_PATH));

        return new InputSource (sid);            
    }
    
        
    /** Writes the content of a file
     */
    private static void prepareXmlFilesystem( File jar, int count, String prefix ) throws IOException {
        FileOutputStream stream = new FileOutputStream( jar );
        OutputStreamWriter writer = new OutputStreamWriter( stream, "UTF8" );
        PrintWriter print = new PrintWriter( writer );
        print.println(
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE filesystem PUBLIC " +
            "\"-//NetBeans//DTD Filesystem 1.0//EN\" " + 
            "\"http://www.netbeans.org/dtds/filesystem-1_0.dtd\">\n" +
            "<filesystem>"
        );

        for( int i=0; i<count; i++ ) {
            print.println( "<file name=\"" + prefix + i + "\"/>" );
        }
        print.println( "</filesystem>" );
        print.flush();
        print.close();
    }
        
}
