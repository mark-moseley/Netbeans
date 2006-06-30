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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.hints.borrowed;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import org.netbeans.editor.LocaleSupport;
import org.openide.awt.HtmlRenderer;

/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class ListCompletionView extends JList {

    public ListCompletionView() {
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new ListCellRenderer() {
            private HtmlRenderer.Renderer defaultRenderer = HtmlRenderer.createRenderer();

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                defaultRenderer.reset();
                Component result = defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
                defaultRenderer.setHtml(true);
                defaultRenderer.setParentFocused(true);
                return result;
            }
        });
        getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
        getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }
    
    public void setResult(List data) {
        if (data != null) {
            setModel(new Model(data));
            if (data.size() > 0) {
                setSelectedIndex(0);
            }
        }
    }

    /** Force the list to ignore the visible-row-count property */
    public Dimension getPreferredScrollableViewportSize() {
        Dimension scrollable = super.getPreferredScrollableViewportSize();
        Dimension preferred = getPreferredSize();
        
        return new Dimension(scrollable.width, preferred.height);
    }

    public void up() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(Math.max(getSelectedIndex() - 1, 0));
            ensureIndexIsVisible(getSelectedIndex());
            repaint();
        }
    }

    public void down() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(Math.min(getSelectedIndex() + 1, lastInd));
            ensureIndexIsVisible(getSelectedIndex());
            validate();
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    static class Model extends AbstractListModel {

        List data;

        static final long serialVersionUID = 3292276783870598274L;

        public Model(List data) {
            this.data = data;
        }

        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return (index >= 0 && index < data.size()) ? data.get(index) : null;
        }

        List getData() {
            return data;
        }

    }

}
