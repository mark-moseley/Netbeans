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
/*
 * XMLWriter.java
 *
 * Created on March 29, 2001, 7:44 PM
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;

/**
 *
 * @author  vs124454
 * @version 
 */
public class XMLWriter {

    
    private PrettyPrinter printer;
    private String encoding;
    
    
    /** Creates new XMLWriter */
    public XMLWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {                
        printer = new PrettyPrinter(new OutputStreamWriter(out,encoding), 3, 80);
        this.encoding = encoding;
    }
    
    
    

    public void write(Document doc) throws IOException {
        write(doc, null);
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void write(Document doc, LinkedList entities) throws IOException {
        printer.print("<?xml version=\"1.0\" encoding=\""+getEncoding()+"\"?>");
        printer.newLine();
        
        if (null != entities && 0 != entities.size()) {
            writeEntities(doc.getDocumentElement().getNodeName(), entities);
        }

        writeElement(doc.getDocumentElement());
    }
    
    /**
     * Translates <, & , " and > to corresponding entities.
     */
    private String xmlEscape(String orig) {
        if (orig == null) return "";
        StringBuffer temp = new StringBuffer();
        StringCharacterIterator sci = new StringCharacterIterator(orig);
        for (char c = sci.first(); c != CharacterIterator.DONE;
             c = sci.next()) {

            switch (c) {
            case '<':
                temp.append("&lt;");
                break;
            case '>':
                temp.append("&gt;");
                break;
            case '\"':
                temp.append("&quot;");
                break;
            case '&':
                temp.append("&amp;");
                break;
            default:
                temp.append(c);
                break;
            }
        }
        return temp.toString();
    }

    /**
     *  Writes a DOM element to a stream.
     */
    private void writeElement(Element element) throws IOException {        
        
        printer.print("<"+element.getTagName());        
        printer.indentUp();

        // Write attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            printer.print(" "+attr.getName()+"=\""+xmlEscape(attr.getValue())+"\"");
        }
                
        
        boolean hasChildren = element.hasChildNodes();
        if (hasChildren) {
            printer.print(">");
            printer.newLine();
        } else {            
            printer.indentDown();
            printer.print("/>");
            printer.newLine();
            return;
        }

        // Write child attributes and text
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                writeElement((Element)child);                
            }

            if (child.getNodeType() == Node.TEXT_NODE) {
                String textNode = child.getNodeValue();
                if (!justWhiteSpaces(textNode)) {
                    printer.print(child.getNodeValue());                
                }
            }
            
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                printer.print("<![CDATA[");
                printer.printUnformatted(child.getNodeValue());
                printer.printUnformatted("]]>");
                printer.newLine();
            }
            
            if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                printer.print("&"+child.getNodeName()+";");
            }
        }
        
        // Write element close        
        printer.indentDown();        
        printer.print("</"+element.getTagName()+">");
        printer.newLine();
    }
    
    // check whether string contains just whitespaces
    private static boolean justWhiteSpaces(String string) {
        char[] str = string.toCharArray();
        for (int i=0; i < str.length; i++) {
            if (!Character.isWhitespace(str[i])) {
                return false;
            }
        }
        return true;
    }
    
    
    private void writeEntities(String name, LinkedList ents) throws IOException {
/*
<!DOCTYPE Company [
  <!ENTITY bas_d SYSTEM "bas\%id%_d.xml">
  <!ENTITY bas_c SYSTEM "bas\%id%_c.xml">
  <!ENTITY fin SYSTEM "fin\%id%.xml">
  <!ENTITY lang SYSTEM "disc://<feclipath>/configuration/lang.xml">
  <!ENTITY dummy SYSTEM "dummy.xml">
]>
*/
        printer.newLine();
        printer.print("<!DOCTYPE "+name+" [");
        printer.indentUp();
        Iterator it = ents.iterator();
        while (it.hasNext()) {
            DOMEntityDecl e = (DOMEntityDecl)it.next();
            printer.newLine();
            printer.print("<!ENTITY "+e.getName()+" SYSTEM \""+e.getSystemId()+"\">");
            printer.newLine();
        }
        printer.indentDown();
        printer.newLine();
        printer.print("]>");
    }
    
    
    
    
    
    // pretty printer class for formatting xml output with indentation and word wrapping
    public static class PrettyPrinter {
        
        private int maxColumns = 0;
        private int indentSize = 0;
        private Writer writer;
        
        private int currentColumn = 0;
        private int currentIndent = 0;
        private String indentString;
        
        PrettyPrinter(Writer writer, int indentSize, int maxColumns) {
            this.writer = writer;
            this.indentSize = indentSize;
            this.maxColumns = maxColumns;
            indentString = getIndentString(indentSize);
        }
        
        public void indentUp() {
            currentIndent++;
            
        }
        
        public void indentDown() {
            if (currentIndent > 0) {
                currentIndent--;
            }
        }
        
        public void print(String string) throws IOException {
            int oldIndex = 0 ;
            int newIndex = 0;
            while ((newIndex = string.indexOf('\n',oldIndex)) >= 0) {
                String subString = string.substring(oldIndex,newIndex);
                print(subString);
                newLine();
                oldIndex = newIndex + 1;
            }
            
            if (oldIndex > 0) {
                // there is nothing more to be done
                // the string is already printed
                return;
            }
                        
            // check whether the string will be over our limit
            if ((currentColumn + string.length()) >= maxColumns) {
                newLine();
            }
            
            // indent before printing the string (if at the beiginning of line)
            if (currentColumn == 0) {                
                printIndent();
            }
            
            // print out the string
            writer.write(string);
            currentColumn += string.length();
            writer.flush();
        }
        
        public void printUnformatted(String string) throws IOException {
            writer.write(string);
            writer.flush();
        }
    
        
        public void newLine() throws IOException {
            writer.write('\n');
            currentColumn = 0;
            writer.flush();
        }
        
        private void printIndent() throws IOException {
            for (int i=0; i < currentIndent; i++) {
                writer.write(indentString);
                currentColumn += indentSize;
            }
        }
        
        public void reset() {
            currentColumn = 0;
            currentIndent = 0;
        }
        
        private static String getIndentString(int indentSize) {
            char[] indentChars = new char[indentSize];
            for (int i=0; i < indentSize; i++) {
                indentChars[i] = ' ';
            }
            return new String(indentChars);
        }      
    }
}
