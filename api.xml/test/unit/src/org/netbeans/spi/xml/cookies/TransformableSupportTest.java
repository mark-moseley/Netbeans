/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;

import junit.framework.*;
import org.netbeans.junit.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

/**
 *
 * @author Libor Kramolis
 */
public class TransformableSupportTest extends NbTestCase {
    
    public TransformableSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformableSupportTest.class);
        return suite;
    }
    
    
    public void testTransform () {
        assertTrue ("Correct XML and correct XSLT must pass!",               transform ("data/doc.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Incorrect XML and correct XSLT must not pass!",  false==transform ("data/InvalidDocument.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Correct XML and incorrect XSLT must not pass!",  false==transform ("data/doc.xml", "data/InvalidDocument.xml"));
        assertTrue ("Incrrect XML and incorrect XSLT must not pass!", false==transform ("data/InvalidDocument.xml", "data/InvalidDocument.xml"));
    }
    
    private boolean transform (String xml, String xslt) {
        URL xmlURL = getClass().getResource(xml);
        URL xsltURL = getClass().getResource(xslt);
        Source xmlSource = new SAXSource (new InputSource (xmlURL.toExternalForm()));
        Source xsltSource = new SAXSource (new InputSource (xsltURL.toExternalForm()));
        Result outputResult = new StreamResult (new StringWriter());
        
        TransformableSupport support = new TransformableSupport (xmlSource);
        Observer observer = new Observer(); // not yet used
        boolean exceptionThrown = false;
        try {
            support.transform (xsltSource, outputResult, null);
        } catch (TransformerException exc) {
            System.err.println("!!! " + exc);
            exceptionThrown = true;
        }
        
        System.out.println(xml + " & " + xslt + " => " + ( exceptionThrown ? "WRONG" : "OK" ));
        return exceptionThrown==false;
    }
    

    //
    // class Observer
    //
    
    private static class Observer implements CookieObserver {
        private int receives;
        private int warnings;
        
        public void receive(CookieMessage msg) {
            receives++;
            if (msg.getLevel() >= msg.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }
        
    };
        
}
