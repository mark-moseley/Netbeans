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

/*
 * ErrorAnnotation.java
 *
 * Created on November 9, 2004, 3:09 PM
 */

package org.netbeans.modules.web.core.syntax.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.core.syntax.JspParserErrorAnnotation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;


/**
 *
 * @author Petr Pisl
 */
public class ErrorAnnotation {
    
    public static final int JSP_ERROR = 1;
    
    /** Jsp file, for which is the ErrorAnnotation */
    private FileObject jspFo;
    
    private ArrayList annotations;
    
    /** Creates a new instance of ErrorAnnotation */
    public ErrorAnnotation(FileObject jspFo) {
        this.jspFo = jspFo;
        annotations = new ArrayList();
    }
    
    /** Adds annotation for the errors. If the error is already annotated, does nothing. If there are 
     *  annotated erros, which are not in the input array, then these annotations are deleted.
     *
     *  
     */
    public void annotate(ErrorInfo[] errors){
        ArrayList added, removed, unchanged;
        Collection newAnnotations;
        
        // obtain data object
        DataObject doJsp;
        try {
            doJsp = DataObject.find(jspFo);
        }
        catch (DataObjectNotFoundException e){
            return;
        }
        
        EditorCookie editor = (EditorCookie)doJsp.getCookie(EditorCookie.class);
        if (editor == null)
            return;
        StyledDocument document = editor.getDocument();
        if (document == null)
            return;
        
        // The approriate JText component
        JTextComponent component = editor.getOpenedPanes()[0];
        if (component != null){
            if (errors != null && errors.length > 0){
                // Place the first error in the status bar
                org.netbeans.editor.Utilities.setStatusBoldText(component , " " + errors[0].getDescription()); //NOI18N
            }
            else{
                // clear status bar
                org.netbeans.editor.Utilities.clearStatusText(component);
            }
        }
        
        // create annotations from errors
        newAnnotations = getAnnotations(errors, document);
        // which annotations are really new
        added=new ArrayList(newAnnotations);
        added.removeAll(annotations);
        // which annotations were here before
        unchanged=new ArrayList(annotations);
        unchanged.retainAll(newAnnotations);
        // which annotations are obsolete
        removed = annotations;
        removed.removeAll(newAnnotations);
        detachAnnotations(removed);

        // are there new annotations?
        if (!added.isEmpty()) {
            final ArrayList finalAdded = added;
            final DataObject doJsp2 = doJsp;
            Runnable docRenderer = new Runnable() {
                public void run() {
                    LineCookie cookie = (LineCookie)doJsp2.getCookie(LineCookie.class);
                    Line.Set lines = cookie.getLineSet();

                    for (Iterator i=finalAdded.iterator();i.hasNext();) {
                        LineSetAnnotation ann=(LineSetAnnotation)i.next();
                        ann.attachToLineSet(lines);
                    }
                }
            };

            if (document != null) {
                document.render(docRenderer);
            } else {
                docRenderer.run();
            }
        }
        
        // remember current annotations
        annotations=unchanged;
        annotations.addAll(added);
       
    }
    
    /** Transforms ErrosInfo to Annotation
     */
    private Collection getAnnotations(ErrorInfo[] errors, StyledDocument document) {
        HashMap map = new HashMap(errors.length);
        for (int i = 0; i < errors.length; i ++) {
            ErrorInfo err = errors[i];
            int line = err.getLine();

            if (line<0) 
                continue; // When error is outside the file, don't annotate it
            int column = err.getColumn();
            String message = err.getDescription();
            LineSetAnnotation ann;
            switch (err.getType()){
                case JSP_ERROR:
                    ann = new JspParserErrorAnnotation(line, column, message, (NbEditorDocument)document);
                    break;
                default:
                    ann = new JspParserErrorAnnotation(line, column, message, (NbEditorDocument)document);
                    break;
            }
           

            // This is trying to ensure that annotations on the same
            // line are "chained" (so we get a single annotation for
            // multiple errors on a line).
            // If we knew the errors were sorted by file & line number,
            // this would be easy (and we wouldn't need to do the hashmap
            // "sort"
            Integer lineInt = new Integer(line);
            /*LineSetAnnotation prev = (LineSetAnnotation)map.get(lineInt);
            if (prev != null) {
                prev.chain(ann);
            } else if (map.size() < maxErrors) {*/
            map.put(lineInt, ann);
            //}
        }
        return map.values();
    }
    
    /** Removes obsolete annotations
     */
    
    private static void detachAnnotations(Collection anns) {
        Iterator i;

        for (i=anns.iterator();i.hasNext();) {
            Annotation ann=(Annotation)i.next();
            if (ann.getAttachedAnnotatable() != null) {
                ann.detach();
            }
        }
    }
    
    public abstract static class LineSetAnnotation extends Annotation {

        public abstract void attachToLineSet(Line.Set lines);
    }
    
    
    public static class ErrorInfo {
        /**
         * Holds value of property description.
         */
        private String description;

        /**
         * Holds value of property line.
         */
        private int line;

        /**
         * Holds value of property column.
         */
        private int column;

        /**
         * Holds value of property type.
         */
        private int type;

        
        public ErrorInfo(String description, int line, int column, int type){
            this.description = description;
            this.line = line;
            this.column = column;
            this.type = type;
        }
        /**
         * Getter for property description.
         * @return Value of property description.
         */
        public String getDescription() {

            return this.description;
        }

        /**
         * Getter for property line.
         * @return Value of property line.
         */
        public int getLine() {

            return this.line;
        }

        /**
         * Getter for property column.
         * @return Value of property column.
         */
        public int getColumn() {

            return this.column;
        }

        /**
         * Getter for property type.
         * @return Value of property type.
         */
        public int getType() {

            return this.type;
        }
        
        
    }
}
