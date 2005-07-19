/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.spi.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.palette.PaletteSwitch;
import org.netbeans.modules.palette.Utils;
import org.netbeans.modules.palette.ui.PalettePanel;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;




/**
 * <p>Top component which displays component palette.</p>
 *
 *
 * @author S. Aubrecht
 */

final class PaletteTopComponent extends TopComponent implements PropertyChangeListener {

    static final long serialVersionUID = 4248268998485315735L;

    private static PaletteTopComponent instance;
    /** holds currently scheduled/running task for set of activated node */
    private RequestProcessor.Task nodeSetterTask;
    private final Object NODE_SETTER_LOCK = new Object();
    
    private TopComponent paletteSource;
    
    /** Creates new PaletteTopComponent */
    private PaletteTopComponent() {
        setName(Utils.getBundleString("CTL_Component_palette"));  // NOI18N
        setToolTipText(Utils.getBundleString("HINT_PaletteComponent"));
        setIcon(Utilities.loadImage("org/netbeans/modules/form/resources/palette.png")); // NOI18N
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(505, 88));
        add( PalettePanel.getDefault().getScrollPane(), BorderLayout.CENTER );
    }
    
    public void requestActive() {
        super.requestActive();
        PalettePanel.getDefault().requestFocusInWindow();
    }

    /** Gets default instance. Don't use directly, it reserved for '.settings' file only,
     * i.e. deserialization routines, otherwise you can get non-deserialized instance. */
    public static synchronized PaletteTopComponent getDefault() {
        if(instance == null) {
            instance = new PaletteTopComponent();
        }
        return instance;
    }
    
    /** Overriden to explicitely set persistence type of PaletteTopComponent
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void componentOpened() {
        PaletteSwitch switcher = PaletteSwitch.getDefault();
        
        switcher.addPropertyChangeListener( this );
        setPaletteController( switcher.getCurrentPalette() );
    }
    
    public void componentClosed() {
        // palette is closed so reset its contents
        setPaletteController( null );
        
        PaletteSwitch.getDefault().removePropertyChangeListener( this );
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return getClass().getName();
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if( PaletteSwitch.PROP_PALETTE_CONTENTS.equals( e.getPropertyName() ) ) {
            PaletteController pc = (PaletteController)e.getNewValue();
            
            setPaletteController( pc );
        }
    }
    
    private void setPaletteController( PaletteController pc ) {
        if( null != pc ) {
            PalettePanel.getDefault().setContent( pc, pc.getModel(), pc.getSettings() );
        } else {
            PalettePanel.getDefault().setContent( null, null, null );
        }
    }
    
    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457788L;
        public Object readResolve() {
            return PaletteTopComponent.getDefault();
        }
    }
}
