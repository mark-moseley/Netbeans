/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */
 
/* $Id$ */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.TextAreaPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeTextAreaPeer extends FakeTextComponentPeer implements TextAreaPeer
{
    FakeTextAreaPeer(TextArea target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void insert(String text, int pos) {
    }

    public void replaceRange(String text, int start, int end) {
    }

    public Dimension getPreferredSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public Dimension getMinimumSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public void insertText(String txt, int pos) {
        insert(txt, pos);
    }

    public void replaceText(String txt, int start, int end) {
        replaceRange(txt, start, end);
    }

    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }

    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        public void paint(Graphics g) {
            super.paint(g);

            TextArea target =(TextArea) _target;
            String text = target.getText();

            if (text != null) { // draw the text
                Color c = getForeground();
                if (c == null)
                    c = SystemColor.controlText;
                g.setColor(c);
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int th = fm.getHeight(),
                    ty = th,
                    h = target.getSize().height,
                    i = target.getCaretPosition(),
                    len = text.length();
                StringBuffer buf = new StringBuffer(len);

                for ( ; i < len; i++) {
                    char ch = text.charAt(i);
                    if (ch != '\n' && ch != '\r') buf.append(ch);
                    else if (ch == '\n') {
                        g.drawString(buf.toString(),4,ty);
                        if (ty > h) break;
                        ty += th;
                        buf.delete(0,buf.length());
                    }
                }
                g.drawString(buf.toString(),4,ty);
            }
        }

        public Dimension getMinimumSize() {
            TextArea target = (TextArea)_target;
            return FakeTextAreaPeer.this.getMinimumSize(target.getColumns(),target.getRows());
        }
    }
}
