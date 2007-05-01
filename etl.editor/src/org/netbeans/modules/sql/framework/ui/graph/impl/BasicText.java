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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.nwoods.jgo.JGoGlobal;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicText extends JGoText {

    /**
     * ORIGINAL TEXT Property
     */
    public static final String ORIGINAL_TEXT = "original_text";

    private String originalText;
    private boolean showDot = true;
    private ArrayList listeners = new ArrayList();
    private boolean isEditing = false;

    /**
     * create a new instance of text area
     * 
     * @param text text
     */
    public BasicText(String text) {
        super(text);
        setAutoResize(false);
        setResizable(true);
        setSelectable(false);
        setOriginalText(text);
        setDefaultFontSize(11);
        setDefaultFontFaceName("SansSerif");
        setClipping(true);
    }

    /**
     * set the original text. Original text is something that will be shown if this text
     * area has enough space to show all of it
     * 
     * @param text text
     */
    public void setOriginalText(String text) {
        String oldVal = this.originalText;
        this.originalText = text;
        //also set text on the jgo text
        this.setText(text);
        firePropertyChangeEvent(ORIGINAL_TEXT, oldVal, originalText);
    }

    /**
     * get the original text. This text area modifies the text by adding ... so if we
     * expand the text area we should see the full length text
     * 
     * @return original text
     */
    public String getOriginalText() {
        return this.originalText;
    }

    /**
     * set the flag so that this text are will calculate for ...
     * 
     * @param show whether a calculation for ... is to be done
     */
    public void setShowDot(boolean show) {
        showDot = show;
    }

    /**
     * get the minimum width
     * 
     * @return minimum width
     */
    public int getMinimumWidth() {
        //we at least want to show ...
        return computeWidthFor("...");
    }

    public int getMaximumWidth() {
        return computeWidthFor(this.getOriginalText());
    }

    /**
     * @return
     */
    private int computeWidthFor(String text) {
        int w = 10; // minimum width if we can't obtain required components for the
                    // computation.

        Component somecomp = JGoGlobal.getComponent();
        if (somecomp == null) {
            return w;
        }

        Graphics2D g2 = (Graphics2D) somecomp.getGraphics();
        if (g2 == null) {
            return w;
        }

        try {
            FontRenderContext frc = g2.getFontRenderContext();
            if (text.equals("")) {
                return 0;
            }

            double dwidth = getFont().getStringBounds(text, frc).getWidth();
            w = (int) Math.ceil(dwidth);
        } finally {
            g2.dispose();
        }

        return w;
    }

    public void doEndEdit() {
        isEditing = false;
        this.setOriginalText(this.getText());
        super.doEndEdit();
    }

    /**
     * paint this area
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(java.awt.Graphics2D g, JGoView view) {
        if (showDot) {
            GraphUtility.adjustText(g, this);
        }
        super.paint(g, view);
    }

    /**
     * add a property change listener
     * 
     * @param l listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }

    /**
     * remove a property change listener
     * 
     * @param l listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }

    protected synchronized void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

        for (int i = 0; i < listeners.size(); i++) {
            PropertyChangeListener l = (PropertyChangeListener) listeners.get(i);
            l.propertyChange(evt);
        }
    }

    /**
     * Change the cursor at the port
     * 
     * @param flags
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if (this.isEditable() && getLayer() != null && getLayer().isModifiable()) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            return true;
        }
        return false;
    }

    public void setBackgroundColor(Color c) {
        this.setBkColor(c);
    }

    public String getText() {
        if (isEditing) {
            return this.getOriginalText();
        }
        return super.getText();
    }

    /**
     * Programmatically start the editing of the text for the user. This will cause a
     * JTextComponent to appear.
     * <p>
     * This is responsible for starting a transaction.
     * 
     * @param view the view in which the user will be editing
     * @param vc the view coordinates where the user may have clicked
     */
    public void doStartEdit(JGoView view, Point vc) {
        isEditing = true;
        //we need to set text again to fix array index out of bound exception
        this.setText(this.getOriginalText());
        super.doStartEdit(view, vc);
    }

}

