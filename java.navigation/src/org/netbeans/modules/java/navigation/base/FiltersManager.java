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

package org.netbeans.modules.java.navigation.base;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

/** Handles creation and manipulation with boolean state filters. 
 *
 * @author Dafe Simomek
 */
public final class FiltersManager {
    
    private FiltersComponent comp;
    
    static FiltersManager create (FiltersDescription descr) {
        return new FiltersManager(descr);
    }
    
    /** Returns true when given filter is selected, false otherwise.
     * Note that this method is thread safe, can be called from any thread
     * (and usually will, as clients will call this from loadContent naturally)
     */
    public boolean isSelected (String filterName) {
        return comp.isSelected(filterName);
    }
    
    /** Sets boolean value of filter with given name. True means filter is
     * selected (enabled), false otherwise. Note, must be called from AWT thread.
     */
    public void setSelected (String filterName, boolean value) {
        comp.setFilterSelected(filterName, value);
    }

    /** @return component instance visually representing filters */ 
    public JComponent getComponent () {
        return comp;
    }
    
    /** @return Filters description */
    public FiltersDescription getDescription () {
        return comp.getDescription();
    }

    /** Assigns listener for listening to filter changes */ 
    public void hookChangeListener (FilterChangeListener l) {
        comp.hookFilterChangeListener(l);
    }
    
    /** Interface for listening to changes of filter states contained in FIltersPanel
     */
    public interface FilterChangeListener {
        /** Called whenever some changes in state of filters contained in
         * filters panel is triggered
         */
        public void filterStateChanged(ChangeEvent e);
        
    } // end of FilterChangeListener
    

    /** Private, creation managed by factory method 'create' */
    private FiltersManager (FiltersDescription descr) {
        comp = new FiltersComponent(descr);
    }
    
    /** Swing component representing filters in panel filled with toggle buttons.
     * Provides thread safe access to the states of filters by copying states
     * into private map, properly sync'ed.
     */
    private class FiltersComponent extends Box implements ActionListener {
        
        /** list of <JToggleButton> visually representing filters */
        private List toggles;
        /** description of filters */
        private final FiltersDescription filtersDesc;
 
        /** lock for listener */
        private Object L_LOCK = new Object();
        /** listener */
        private FilterChangeListener clientL;

        /** lock for map of filter states */
        private Object STATES_LOCK = new Object();
        /** copy of filter states for accessing outside AWT */
        private Map filterStates;

        /** Returns selected state of given filter, thread safe.
         */
        public boolean isSelected (String filterName) {
            Boolean result;
            synchronized (STATES_LOCK) {
                if (filterStates == null) {
                    // Swing toggles not initialized yet
                    int index = filterIndexForName(filterName);
                    if (index < 0) {
                        return false;
                    } else {
                        return filtersDesc.isSelected(index);
                    }
                }
                result = (Boolean)filterStates.get(filterName);
            }
            
            if (result == null) {
                throw new IllegalArgumentException("Filter " + filterName + " not found.");
            }
            return result.booleanValue();
        }
        
        /** Sets filter value, AWT only */
        public void setFilterSelected (String filterName, boolean value) {
            assert SwingUtilities.isEventDispatchThread();
            
            int index = filterIndexForName(filterName);
            if (index < 0) {
                throw new IllegalArgumentException("Filter " + filterName + " not found.");
            }
            // update both swing control and states map
            ((JToggleButton)toggles.get(index)).setSelected(value);
            synchronized (STATES_LOCK) {
                filterStates.put(filterName, Boolean.valueOf(value));
            }
            // notify
            fireChange();
        }
        
        public void hookFilterChangeListener (FilterChangeListener l) {
            synchronized (L_LOCK) {
                clientL = l;
            }
        }
        
        public FiltersDescription getDescription () {
            return filtersDesc;
        }
    
        /** Not public, instances created using factory method createPanel */
        FiltersComponent(FiltersDescription descr) {
            super(BoxLayout.X_AXIS);
            this.filtersDesc = descr;
            // always create swing content in AWT thread
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        initPanel();                        
                    }
                });
            } else {
                initPanel();
            }
        }

        /** Called only from AWT */
        private void initPanel () {
            setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
            JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            // create toggle buttons
            int filterCount = filtersDesc.getFilterCount();
            toggles = new ArrayList(filterCount);
            JToggleButton toggleButton = null;
            
            Map fStates = new HashMap(filterCount * 2);

            for (int i = 0; i < filterCount; i++) {
                toggleButton = createToggle(fStates, i);
                toggles.add(toggleButton);
            }
            
            // add toggle buttons
            JToggleButton curToggle;
            Dimension space = new Dimension(3, 0);
            for (int i = 0; i < toggles.size(); i++) {
                curToggle = (JToggleButton)toggles.get(i);
                curToggle.addActionListener(this);
                toolbar.add(curToggle);
                if (i != toggles.size() - 1) {
                    toolbar.addSeparator(space);
                }
            }
            
            add(toolbar);
            
            // initialize member states map
            synchronized (STATES_LOCK) {
                filterStates = fStates;
            }
        }
        
        private JToggleButton createToggle (Map fStates, int index) {
            boolean isSelected = filtersDesc.isSelected(index);
            Icon icon = filtersDesc.getSelectedIcon(index);
            // ensure small size, just for the icon
            JToggleButton result = new JToggleButton(icon, isSelected);
            Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
            result.setPreferredSize(size);
            result.setMargin(new Insets(2,3,2,3));
            result.setToolTipText(filtersDesc.getTooltip(index));
            
            fStates.put(filtersDesc.getName(index), Boolean.valueOf(isSelected));
            
            return result;
        }

        /** Finds and returns index of filter with given name or -1 if no
         * such filter exists.
         */
        private int filterIndexForName (String filterName) {
            int filterCount = filtersDesc.getFilterCount();
            String curName;
            for (int i = 0; i < filterCount; i++) {
                curName = filtersDesc.getName(i);
                if (filterName.equals(curName)) {
                    return i;
                }
            }
            return -1;
        }

        /** Reactions to toggle button click,  */
        public void actionPerformed(ActionEvent e) {
            // copy changed state first
            JToggleButton toggle = (JToggleButton)e.getSource();
            int index = toggles.indexOf(e.getSource());
            synchronized (STATES_LOCK) {
                filterStates.put(filtersDesc.getName(index),
                                Boolean.valueOf(toggle.isSelected()));
            }
            // notify
            fireChange();
        }
        
        private void fireChange () {
            FilterChangeListener lCopy;
            synchronized (L_LOCK) {
                // no listener = no notification
                if (clientL == null) {
                    return;
                }
                lCopy = clientL;
            }
            
            // notify listener
            lCopy.filterStateChanged(new ChangeEvent(FiltersManager.this));
        }
    
    } // end of FiltersComponent
    
}
