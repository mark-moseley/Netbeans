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

package org.netbeans.modules.web.debug;

import java.io.*;
import javax.swing.JEditorPane;
import javax.swing.text.*;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.debugger.*;
import org.openide.nodes.Node;
import org.openide.text.*;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

import org.netbeans.modules.debugger.*;
import org.netbeans.modules.debugger.support.java.*;
import org.netbeans.modules.debugger.delegator.DelegatingWatch;
import org.netbeans.modules.web.debug.util.Utils;

public class JspToolTipAnnotation extends Annotation implements Runnable {
    
    private String toolTipText = null;

    private StyledDocument doc;

    public String getShortDescription() {
        Utils.getEM().log("JspTooltip: getShortDescription");
        
        toolTipText = null;
        if (!(Register.getCurrentDebugger () instanceof JavaDebugger)
        )   return null;

        org.openide.text.Line.Part lp = (org.openide.text.Line.Part) getAttachedAnnotatable();
        org.openide.text.Line line = lp.getLine ();
        org.openide.loaders.DataObject dob = org.openide.text.DataEditorSupport.findDataObject(line);
        org.openide.cookies.EditorCookie ec = 
            (org.openide.cookies.EditorCookie) dob.getCookie(org.openide.cookies.EditorCookie.class);

        if (ec != null) { // Only for editable dataobjects
            try {
                doc = ec.openDocument();                    
                RequestProcessor.getDefault().post(this);                    
                doc.render(this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return toolTipText;
    }

    public void run () {

        Utils.getEM().log("JspTooltip: run");

        //1) get tooltip text
        Line.Part lp = (Line.Part)getAttachedAnnotatable();
        JEditorPane ep = getCurrentEditor();
        String textForTooltip = "";
        
        if ((lp == null) || (ep == null)) return;
        
        //first try EL
        String text = Utils.getELIdentifier(doc, ep,NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn());
        Utils.getEM().log("JspTooltip: ELIdentifier = " + text);

        Boolean isScriptlet = Utils.isScriptlet(
            doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
        );
        Utils.getEM().log("isScriptlet: " + isScriptlet.booleanValue());
        
        //if not, try Java
        if ((text == null) && (isScriptlet.booleanValue())) {
            text = Utils.getJavaIdentifier(
                doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
            );
            textForTooltip = text;
            Utils.getEM().log("JspTooltip: javaIdentifier = " + text);
            if (text == null) return;
        } else {
            if (text == null) return;
            textForTooltip = text;
            String textEscaped = org.openide.util.Utilities.replaceString(text, "\"", "\\\"");
            text = "pageContext.getExpressionEvaluator().evaluate(\"" + textEscaped +
                                "\", java.lang.String.class, (javax.servlet.jsp.PageContext)pageContext, null)";
        }
        
        Utils.getEM().log("JspTooltip: fullWatch = " + text);
        
        //2) create watch for given text
        final AbstractDebugger debugger = Register.getCoreDebugger ();
        AbstractWatch watch = (AbstractWatch) debugger.createWatch (text, true);
        AbstractWatch w = watch;
        if (watch instanceof DelegatingWatch) {
            AbstractWatch w1 = (AbstractWatch) ((DelegatingWatch) watch).getInnerWatch ();
            if (w1 != null) w = w1;
        }
        
        //3) obtain text representation of value of watch
        String old = toolTipText;
        toolTipText = null;
        if (watch != null) {
            Utils.getEM().log("Error: " + watch.getErrorMessage());
            if (watch instanceof JavaWatch) {
                toolTipText = ((JavaWatch) watch).toStringValue ();
            }
            if (toolTipText == null) {
                toolTipText = watch.getAsText();
            }
            if (toolTipText != null) {
                toolTipText = textForTooltip + " = " + toolTipText; // NOI18N
            }
        }
        Utils.getEM().log("JspTooltip: " + toolTipText);
        firePropertyChange (PROP_SHORT_DESCRIPTION, old, toolTipText);       
    }

    public String getAnnotationType () {
        return null;
    }

    /** 
     * Returns current editor component instance.
     *
     * Used in: ToolTipAnnotation
     */
    static JEditorPane getCurrentEditor () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if ( (nodes == null) ||
             (nodes.length != 1) ) return null;
        Node n = nodes [0];
        return (EditorCookie) n.getCookie (
            EditorCookie.class
        );
    }
    
}
