/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xslt.core.multiview.mapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.JToggleButton;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.openide.windows.TopComponent;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JButton;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;

import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xslt.mapper.palette.XsltPaletteFactory;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * 
 */
public class MapperMultiViewElement extends TopComponent
        implements MultiViewElement, Serializable
{
    private static final long serialVersionUID = 1L;
    private transient MultiViewElementCallback myMultiViewObserver;
    private transient XsltMapper myMapperView;

    private XSLTDataObject myDataObject;
    private transient JComponent myToolBarPanel;
    private static Boolean groupVisible = null;
    
    // for deserialization
    private MapperMultiViewElement() {
        super();
    }
    
    /** Creates a new instance of MapperMultiViewElement. This is the visual
     *  canvas 'Mapper' view in the multiview
     */
    public MapperMultiViewElement(XSLTDataObject dataObject) {
        myDataObject = dataObject;
        initializeLookup();
        initializeUI();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }
    
    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    @Override
    public void readExternal( ObjectInput in ) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        myDataObject = (XSLTDataObject) in.readObject();
        initializeLookup();
        initializeUI();
    }
    
    private GridBagConstraints createGBConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.BOTH;
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        return gc;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //                         MultiViewElement
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    public CloseOperationState canCloseElement() {
        //
        // actually if there are any visual changed NOT committed to the model
        // then we may need to flush something here or something
        //
        boolean lastView = isLastView();
        
        if(!lastView) {
            return CloseOperationState.STATE_OK;
        }
        
        XSLTDataEditorSupport editorSupport = myDataObject.getEditorSupport();
        boolean modified = editorSupport.isModified();
        
        if(!modified) {
            return CloseOperationState.STATE_OK;
        } else {
            return MultiViewFactory.createUnsafeCloseState(
                    "Data Object Modified", null, null);    // NOI18N
        }
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        addUndoManager();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        myMapperView = null;
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        if (myMapperView != null) {
            myMapperView.setVisible(false);
        }
        updateXsltTcGroupVisibility(false);
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        if (myMapperView != null) {
            myMapperView.setVisible(true);
        }
        addUndoManager();
        updateXsltTcGroupVisibility(true);
    }

    public JComponent getToolbarRepresentation() {
        if ( myToolBarPanel == null ) {
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
//            toolbar.addSeparator();
            
//            toolbar.add(Box.createHorizontalStrut(1));
// TODO r | m            
//            toolbar.add(new JButton("testButton"));
//            toolbar.addSeparator();
            int maxButtonHeight = 0;
            
            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    maxButtonHeight = Math.max(c.getPreferredSize().height,
                            maxButtonHeight);
                }
            }
            
            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    Dimension size = c.getMaximumSize();
                    size.height = maxButtonHeight;
                    c.setMaximumSize(size);
                    c.setMinimumSize(c.getPreferredSize());
                } else if (c instanceof JTextComponent) {
                    c.setMaximumSize(c.getPreferredSize());
                    c.setMinimumSize(c.getPreferredSize());
                } else if (c instanceof JSlider) {
                    Dimension size;
                    size = c.getMaximumSize();
                    size.width = 160;
                    c.setMaximumSize(size);
                    
                    size = c.getPreferredSize();
                    size.width = 160;
                    c.setPreferredSize(size);
                } else {
                    c.setMinimumSize(c.getPreferredSize());
                }
            }
            myToolBarPanel = toolbar;
        }
        return myToolBarPanel;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return getDataObject().getEditorSupport().getUndoManager();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }
    
    @Override
    public void requestVisible() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
    public void requestActive() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    protected boolean closeLast() {
        return true;
    }
    
    private XsltMapper createMapperView() {
        return new XsltMapper(getLookup()); // got TC's lookup or no Palette
        
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        myMapperView = createMapperView();
        add(myMapperView, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     *  Open or close the xslt_mapper TopComponentGroup.
     */
    private static void updateXsltTcGroupVisibility(final boolean show) {
        // when active TopComponent changes, check if we should open or close
        // the XSLT editor group of windows
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("xslt_mapper"); // NOI18N
        if (group == null) {
            return; // group not found (should not happen)
        }
        //
        boolean mapperSelected = false;
        Iterator it = wm.getModes().iterator();
        while (it.hasNext()) {
            Mode mode = (Mode) it.next();
            TopComponent selected = mode.getSelectedTopComponent();
            if (selected != null) {
            MultiViewHandler mvh = MultiViews.findMultiViewHandler(selected);
                if (mvh != null) {
                    MultiViewPerspective mvp = mvh.getSelectedPerspective();
                    if (mvp != null) {
                        String id = mvp.preferredID();
                        if (MapperMultiViewElementDesc.PREFERRED_ID.equals(id)) {
                            mapperSelected = true;
                            break;
                        }
                    }
                }
            }
        }
        //
        if (mapperSelected && !Boolean.TRUE.equals(groupVisible)) {
            group.open();
        } else if (!mapperSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
        }
        //
        groupVisible = mapperSelected ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public static String getMVEditorActivePanelPrefferedId() {
        TopComponent activeTC = WindowManager.getDefault().getRegistry()
        .getActivated();
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(activeTC);
        if (mvh == null) {
            return null;
        }
        
        MultiViewPerspective mvp = mvh.getSelectedPerspective();
        if (mvp != null) {
            return mvp.preferredID();
        }
        
        return null;
    }
    
    private boolean isLastView() {
        boolean oneOrLess = true;
        Enumeration en =
                ((CloneableTopComponent)myMultiViewObserver.getTopComponent()
                ).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }
        
        return oneOrLess;
    }
    
    private XSLTDataObject getDataObject() {
        return myDataObject;
    }
    
    private void initializeLookup() {
        associateLookup(createAssociateLookup());
        setActivatedNodes(new Node[] {getDataObject().getNodeDelegate()});
    }
    
    private Lookup createAssociateLookup() {
        MapperContext mapperContext = myDataObject.getLookup().lookup(MapperContext.class);
//        System.out.println("test xslt lookup: "+mapperContext);
        if (mapperContext != null) {
//            System.out.println("sourceComponent: "+mapperContext.getSourceType());
//            System.out.println("targetComponent "+mapperContext.getTargetType());
//            System.out.println("xslModel "+mapperContext.getXSLModel());
        }
        
        
        //
        // see http://www.netbeans.org/issues/show_bug.cgi?id=67257
        //
        return new ProxyLookup(new Lookup[] {
            myDataObject.getLookup(), // this lookup contain objects that are used in OM clients
            Lookups.fixed(new Object[] {
                // required to perform search on associated TCs with this dataobject
                getDataObject().getLookup(),
                
                getDataObject().getNodeDelegate(),
                XsltPaletteFactory.getPalette()})
        });
    }

    /**
     * Adds the undo/redo manager to the bpel model as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        XSLTDataEditorSupport support = myDataObject.getEditorSupport();
        if ( support!= null ){
            QuietUndoManager undo = support.getUndoManager();
            support.addUndoManagerToModel( undo );
        }
    }

}
