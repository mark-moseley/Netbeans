/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * AbstractSVGAction.java
 * Created on May 30, 2007, 4:43 PM
 */

package org.netbeans.modules.mobility.svgcore.view.svg;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractSVGAction extends AbstractAction implements Presenter.Popup {
    public static final String ICON_PATH_PREFIX = "org/netbeans/modules/mobility/svgcore/resources/"; //NOI18N
    public static final String LBL_ID_PREFIX    = "LBL_"; //NOI18N
    public static final String ICON_ID_PREFIX   = "ICON_"; //NOI18N
    public static final String HINT_ID_PREFIX   = "HINT_"; //NOI18N
    public static final String KEY_ID_PREFIX    = "KEY_"; //NOI18N

    protected final String    m_name;
    protected final String    m_label;
    protected final String    m_hint;
    protected final ImageIcon m_icon;
    protected final int       m_toolbarPos;
        
    public AbstractSVGAction(String name) {
        this(name, true);
    }

    public AbstractSVGAction(String name, boolean isEnabled) {
        this(name, isEnabled, 100);
    }
    
    public AbstractSVGAction(String name, boolean isEnabled, int toolBarPos) {
        m_name  = name;
        m_label = getMessage(LBL_ID_PREFIX + name);
        
        m_icon = getIcon(ICON_ID_PREFIX + name);
        setIcon( m_icon);
        
        String hint;
        try {
            hint = getMessage(HINT_ID_PREFIX + name);
        } catch( MissingResourceException e) {
            hint = m_label;
        }
        m_hint = hint;
        
        try {
            String    key    = getCleanMessage( KEY_ID_PREFIX + name);
            KeyStroke stroke = KeyStroke.getKeyStroke(key);
            assert stroke != null : "Invalid key stroke: " + name; //NOI18N
            putValue(Action.ACCELERATOR_KEY, stroke);
        } catch( MissingResourceException e) {}
        
        setDescription(hint);
        setEnabled(isEnabled);
        m_toolbarPos = toolBarPos;
    }

    protected final void setIcon(ImageIcon icon) {
        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        }
    }
    
    protected final void setDescription(String hint) {
        KeyStroke stroke = (KeyStroke) getValue(Action.ACCELERATOR_KEY);
        if (stroke != null) {
            hint += " ("; //NOI18N
            String str = KeyEvent.getKeyModifiersText(stroke.getModifiers());
            if (str.length() > 0) {
                hint += str + "+"; //NOI18N
            }
            hint += KeyEvent.getKeyText(stroke.getKeyCode()) + ")"; //NOI18N
        }
        putValue(Action.SHORT_DESCRIPTION, hint);
    }
    
    public String getActionID() {
        return m_name;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setText(getLabel());
        menu.setToolTipText(null);
        menu.setIcon(null);
        return menu;
    }    
    
    public int getPositionInToolbar() {
        return m_toolbarPos;
    }    
    
    public void registerAction( TopComponent tc) {
        KeyStroke ks = (KeyStroke) getValue(Action.ACCELERATOR_KEY);
        if (ks != null) {
            InputMap inputMap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put( ks, m_name);        
            tc.getActionMap().put(m_name, this);
        }
    }

    protected String getLabel() {
        return m_label;
    }
    
    public static String getMessage(String msgId) {
        return NbBundle.getMessage(SVGViewTopComponent.class, msgId);       
    }
    
    public static String getCleanMessage(String msgId) {
        String msg = getMessage(msgId);
        
        if ( msg != null && msg.length() > 0) {
            // remove the (m,n) suffix that gets attached when NbBundle.DEBUG 
            // property is set to true
            int p;
            if ( (p=msg.lastIndexOf('(')) != -1) {
                int q;
                if ( (q=msg.indexOf(')', p)) != -1) {
                    StringBuilder sb = new StringBuilder(msg);
                    sb.delete(p, q+1);
                    msg = sb.toString().trim();
                }
            }
        }
        return msg;
    }
    
    public static ImageIcon getIcon( String bundleKey) {
        try {
            String iconFileName = getCleanMessage( bundleKey).trim();
            String iconPath = ICON_PATH_PREFIX + iconFileName;
            Image img = Utilities.loadImage(iconPath);
            assert img != null : "Icon not found: " + iconPath; //NOI18N
            return new ImageIcon(img);
        } catch( MissingResourceException e) {
            // no icon is provided
            return null;
        }
    }
}
