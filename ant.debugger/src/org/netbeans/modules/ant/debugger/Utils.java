/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.debugger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
        
        
/*
 * AntTest.java
 *
 * Created on 19. leden 2004, 20:03
 */

/**
 *
 * @author  Honza
 */
public class Utils {
            
    private static Object currentLine;
    
    static void markCurrent (final Object line) {
        unmarkCurrent ();
        
        Annotatable[] annotatables = (Annotatable[]) line;
        int i = 0, k = annotatables.length;
        
        // first line with icon in gutter
        DebuggerAnnotation[] annotations = new DebuggerAnnotation [k];
        if (annotatables [i] instanceof Line.Part)
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE,
                annotatables [i]
            );
        else
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE,
                annotatables [i]
            );
        
        // other lines
        for (i = 1; i < k; i++)
            if (annotatables [i] instanceof Line.Part)
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE2,
                    annotatables [i]
                );
            else
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE2,
                    annotatables [i]
                );
        currentLine = annotations;
        
        showLine (line);
    }
    
    static void unmarkCurrent () {
        if (currentLine != null) {
            
//            ((DebuggerAnnotation) currentLine).detach ();
            int i, k = ((DebuggerAnnotation[]) currentLine).length;
            for (i = 0; i < k; i++)
                ((DebuggerAnnotation[]) currentLine) [i].detach ();
            
            currentLine = null;
        }
    }
    
    static void showLine (final Object line) {
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
//                ((Line) line).show (Line.SHOW_GOTO);
//            }
//        });
        
        final Annotatable[] a = (Annotatable[]) line;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (a [0] instanceof Line)
                    ((Line) a [0]).show (Line.SHOW_GOTO);
                else
                if (a [0] instanceof Line.Part)
                    ((Line.Part) a [0]).getLine ().show (Line.SHOW_GOTO);
                else
                    throw new InternalError ();
            }
        });
    }
    
    static int getLineNumber (Object line) {
//        return ((Line) line).getLineNumber ();
        
        final Annotatable[] a = (Annotatable[]) line;
        if (a [0] instanceof Line)
            return ((Line) a [0]).getLineNumber ();
        else
        if (a [0] instanceof Line.Part)
            return ((Line.Part) a [0]).getLine ().getLineNumber ();
        else
            throw new InternalError ();
    }
    
    public static boolean contains (Object currentLine, Line line) {
        if (currentLine == null) return false;
        final Annotatable[] a = (Annotatable[]) currentLine;
        int i, k = a.length;
        for (i = 0; i < k; i++) {
            if (a [i].equals (line)) return true;
            if ( a [i] instanceof Line.Part &&
                 ((Line.Part) a [i]).getLine ().equals (line)
            ) return true;
        }
        return false;
    }
    
    
    static Object getLine (
        final AntEvent event
    ) {
        File file = event.getScriptLocation ();
        final int lineNumber = event.getLine ();
        if (file == null) return null;
        if (lineNumber < 0) return null;

        FileObject fileObject = FileUtil.toFileObject (file);
        EditorCookie editor;
        LineCookie lineCookie;
        try {
            DataObject d = DataObject.find (fileObject);
            editor = (EditorCookie) d.getCookie (EditorCookie.class);
            lineCookie = (LineCookie) d.getCookie (LineCookie.class);
            assert editor != null;
            assert lineCookie != null;
        
            StyledDocument doc = editor.openDocument ();
            InputSource in = createInputSource 
                (fileObject, editor, doc);
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            SAXParser parser = factory.newSAXParser ();
            final int[] line = new int [4];
            class Handler extends DefaultHandler {
                private Locator locator;
                public void setDocumentLocator (Locator l) {
                    locator = l;
                }
                public void startElement (
                    String uri, 
                    String localname, 
                    String qname, 
                    Attributes attr
                ) throws SAXException {
                    if (line [0] == 0) {
                        if ( qname.equals (event.getTaskName ()) &&
                             locator.getLineNumber () == lineNumber
                        ) {
                            line[0] = locator.getLineNumber ();
                            line[1] = locator.getColumnNumber () - 1;
                        }
                    }
                }
                public void endElement (
                    String uri, 
                    String localname, 
                    String qname
                ) throws SAXException {
                    if ( line [0] != 0 &&
                         line [2] == 0 &&
                         qname.equals (event.getTaskName ())
                    ) {
                        line[2] = locator.getLineNumber ();
                        line[3] = locator.getColumnNumber () - 1;
                    }
                }
            }
            parser.parse (in, new Handler ());
            if (line [0] == 0) return null;
            Annotatable[] annotatables = new Annotatable [
                line [2] - line [0] + 1
            ];
            int i = 0;
            for (int ln = line [0]; ln <= line [2]; ln ++) {
                Line l = lineCookie.getLineSet ().getCurrent (ln - 1);
                annotatables [i++] = l;
            }
            return annotatables;
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return null;
    }
    
    static Object getLine (
        final TargetLister.Target target, 
        String nextTargetName
    ) {
        FileObject fileObject = target.getScript ().getFileObject ();
        assert fileObject != null : "No build script for " + target.getName ();
        EditorCookie editor;
        LineCookie lineCookie;
        try {
            DataObject d = DataObject.find (fileObject);
            editor = (EditorCookie) d.getCookie (EditorCookie.class);
            lineCookie = (LineCookie) d.getCookie (LineCookie.class);
            assert editor != null;
            assert lineCookie != null;
        } catch (DataObjectNotFoundException e) {
            throw new AssertionError (e);
        }
        try {
            StyledDocument doc = editor.openDocument ();
            InputSource in = createInputSource 
                (fileObject, editor, doc);
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            SAXParser parser = factory.newSAXParser ();
            final int[] line = new int [4];
            class Handler extends DefaultHandler {
                private Locator locator;
                public void setDocumentLocator (Locator l) {
                    locator = l;
                }
                public void startElement (
                    String uri, 
                    String localname, 
                    String qname, 
                    Attributes attr
                ) throws SAXException {
                    if (line [0] == 0) {
                        if (qname.equals ("target") &&  // NOI18N
                            target.getName ().equals (attr.getValue ("name")) // NOI18N
                        ) {
                            line[0] = locator.getLineNumber ();
                            line[1] = locator.getColumnNumber ();
                        }
                    }
                }
                public void endElement (
                    String uri, 
                    String localname, 
                    String qname
                ) throws SAXException {
                    if ( line [0] != 0 &&
                         line [2] == 0 &&
                         qname.equals ("target")
                    ) {
                        line[2] = locator.getLineNumber ();
                        line[3] = locator.getColumnNumber ();
                    }
                }
            }
            parser.parse (in, new Handler ());
            if (line [0] == 0) return null;
            
            int ln = line [0] - 1;
            List annotatables = new ArrayList ();
            if (nextTargetName != null) {
                Line fLine = lineCookie.getLineSet ().getCurrent (ln);
                int inx = fLine.getText ().indexOf (nextTargetName);
                if (inx >= 0) {
                    annotatables.add (fLine.createPart (
                        inx, nextTargetName.length ()
                    ));
                    ln ++;
                }
            }
            if (annotatables.size () < 1)
                for (; ln < line [2]; ln ++) {
                    Line l = lineCookie.getLineSet ().getCurrent (ln);
                    annotatables.add (l);
                }
            return annotatables.toArray (new Annotatable [annotatables.size ()]);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return null;
    }
    
    /**
     * Utility method to get a properly configured XML input source for a script.
     */
    private static InputSource createInputSource (
        FileObject fo, 
        EditorCookie editor, 
        final StyledDocument document
    ) throws IOException, BadLocationException {
        final StringWriter w = new StringWriter (document.getLength ());
        final EditorKit kit = findKit (editor.getOpenedPanes ());
        final IOException[] ioe = new IOException [1];
        final BadLocationException[] ble = new BadLocationException [1];
        document.render(new Runnable () {
            public void run() {
                try {
                    kit.write (w, document, 0, document.getLength ());
                } catch (IOException e) {
                    ioe [0] = e;
                } catch (BadLocationException e) {
                    ble [0] = e;
                }
            }
        });
        if (ioe[0] != null) {
            throw ioe [0];
        } else if (ble [0] != null) {
            throw ble [0];
        }
        InputSource in = new InputSource (new StringReader (w.toString ()));
        if (fo != null) { // #10348
            try {
                in.setSystemId (fo.getURL ().toExternalForm ());
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
            // [PENDING] Ant's ProjectHelper has an elaborate set of work-
            // arounds for inconsistent parser behavior, e.g. file:foo.xml
            // works in Ant but not with Xerces parser. You must use just foo.xml
            // as the system ID. If necessary, Ant's algorithm could be copied
            // here to make the behavior match perfectly, but it ought not be necessary.
        }
        return in;
    }
    
    private static EditorKit findKit (JEditorPane[] panes) {
        EditorKit kit;
        if (panes != null) {
            kit = panes[0].getEditorKit ();
        } else {
            kit = JEditorPane.createEditorKitForContentType ("text/xml"); // NOI18N
            if (kit == null) {
                // #39301: fallback; can happen if xml/text-edit is disabled
                kit = new DefaultEditorKit ();
            }
        }
        assert kit != null;
        return kit;
    }
}
