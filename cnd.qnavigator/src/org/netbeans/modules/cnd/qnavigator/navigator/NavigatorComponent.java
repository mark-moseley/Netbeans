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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.CndDataObject;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class NavigatorComponent implements NavigatorPanel, LookupListener {
    
    /** Lookup template to search for java data objects. shared with InheritanceTreePanel */
    private Lookup.Template CTemplate = new Lookup.Template(CDataObject.class);
    private Lookup.Result CContext;
    private Lookup.Template CCTemplate = new Lookup.Template(CCDataObject.class);
    private Lookup.Result CCContext;
    private Lookup.Template HTemplate = new Lookup.Template(HDataObject.class);
    private Lookup.Result HContext;
    /** UI of this navigator panel */
    private NavigatorPanelUI panelUI;
    /** model actually containing content of this panel */
    private NavigatorModel curModel;
    /** current context to work on */
    /** actual data */
    private DataObject curData;
    
    public String getDisplayName() {
        return NbBundle.getBundle(NavigatorComponent.class).getString("LBL_members"); //NOI18N
    }
    
    public String getDisplayHint() {
        return NbBundle.getBundle(NavigatorComponent.class).getString("HINT_NavigatorTopComponen"); //NOI18N
    }
    
    public JComponent getComponent() {
        return getPanelUI();
    }
    
    /** Called when this panel's component is about to being displayed.
     * Right place to attach listeners to current navigation data context.
     *
     * @param context Lookup instance representing current context
     */
    public synchronized void panelActivated(Lookup context) {
        CContext = context.lookup(CTemplate);
        CCContext = context.lookup(CCTemplate);
        HContext = context.lookup(HTemplate);
        
        Collection col = CContext.allInstances();
        if (CContext.allInstances().size()>0 ){
            curData = (DataObject)(((List)col).get(0));
        }
        
        col = CCContext.allInstances();
        if (CCContext.allInstances().size()>0 && curData == null){
            curData = (DataObject)(((List)col).get(0));
        }
        
        col = HContext.allInstances();
        if (HContext.allInstances().size()>0 && curData == null){
            curData = (DataObject)(((List)col).get(0));
        }
        
        CContext.addLookupListener(this);
        CCContext.addLookupListener(this);
        HContext.addLookupListener(this);
        
        setNewContent(curData);
    }
    
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context.
     */
    public synchronized void panelDeactivated() {
        CContext.removeLookupListener(this);
        CCContext.removeLookupListener(this);
        HContext.removeLookupListener(this);
        CContext = null;
        CCContext = null;
        HContext = null;
        detachFromModel(curModel);
        curModel = null;
        curData = null;
    }
    
    /** Impl of LookupListener, reacts to changes of context */
    public synchronized void resultChanged(LookupEvent ev) {
        Collection data = ((Lookup.Result)ev.getSource()).allInstances();
        if (!data.isEmpty()) {
            CndDataObject cdo = (CndDataObject)data.iterator().next();
            if (!cdo.equals(curData)) {
		detachFromModel(curModel);
                curData = cdo;
                setNewContent(cdo);
            }
        }
    }
    
    /** Default activated Node strategy is enough for now */
    public Lookup getLookup() {
        return null;
    }
    
    // ModelBusyListener impl - sets wait cursor on content during computing
    
    public void busyStart() {
        if (SwingUtilities.isEventDispatchThread()) {
            getPanelUI().setBusyState(true);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getPanelUI().setBusyState(true);
                }
            });
        }
    }
    
    public void busyEnd() {
        if (SwingUtilities.isEventDispatchThread()) {
            getPanelUI().setBusyState(false);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getPanelUI().setBusyState(false);
                }
            });
        }
    }
    
    public void newContentReady() {
        getPanelUI().newContentReady();
    }
    
    // end of ModelBusyListener implementation
    
    // RelatedItemListener impl, for selecting method currently edited in editor
    
    public void itemsChanged(ItemEvent evt) {
    }
    
    public void itemsCleared(ItemEvent evt) {
        getPanelUI().getContent().repaint();
    }
    
    
    /********** non public stuff **********/
    
    private void setNewContent(final DataObject cdo) {
        final NavigatorPanelUI ui = getPanelUI();
	CsmModelAccessor.getModel().enqueue(new Runnable() {
	    public void run() {
		setNewContentImpl(cdo, ui);
	    }
	}, "Updating QuickNavigator Content"); //NOI18N
    }
    
    private void setNewContentImpl(DataObject cdo, NavigatorPanelUI ui) {
        curModel = new NavigatorModel(cdo, ui, this);
        CsmListeners.getDefault().addProgressListener(curModel);
        CsmListeners.getDefault().addModelListener(curModel);
        ui.getContent().setModel(curModel);
        try {
            curModel.addBusyListener(this);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        curModel.addNotify();
    }
    
    private void detachFromModel(NavigatorModel model) {
	if( model != null ) {
	    CsmListeners.getDefault().removeProgressListener(model);
	    CsmListeners.getDefault().removeModelListener(model);
	    model.removeBusyListener(this);
	    model.removeNotify();
	}
    }
    
    private NavigatorPanelUI getPanelUI() {
        if (panelUI == null) {
            panelUI = new NavigatorPanelUI();
        }
        return panelUI;
    }
}
