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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperView;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.IMapperAutoLayout;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;

/**
 * <p>
 *
 * Title: </p> BasicMapperView<p>
 *
 * Description: </p> BasicMapperView provides basic setter and getter for
 * IBasicMapperView interface.<p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 */

public class BasicMapperView
    implements IBasicMapperView {

    /**
     * the view manager handles this view
     */
    private IBasicViewManager mViewManager;

    /**
     * the view model to be display in this view.
     */
    private IMapperViewModel mViewModel;

    /**
     * the java component repersenting this view
     */
    private Component mComp;

    /**
     * the object handles autolayout
     */
    private IMapperAutoLayout mAutoLayout;

    /**
     * the name of this mapper view
     */
    private String mViewName;

    /**
     * the property listeners list
     */
    private List mPropertyListeners;

    /**
     * flag indicates if this mapper tree is mapping enable.
     */
    private boolean mIsMapable = true;
    
    
    /**
     * Constructor for the BasicMapperView object
     */
    public BasicMapperView() {
        mPropertyListeners = new Vector();
    }

    /**
     * Sets the viewManager attribute of the BasicMapperView object
     *
     * @param viewManager  The new viewManager value
     */
    public void setViewManager(IBasicViewManager viewManager) {
        mViewManager = viewManager;
    }

    /**
     * Return the view manager handles this view.
     *
     * @return   the view manager handles this view.
     */
    public IBasicViewManager getViewManager() {
        return mViewManager;
    }

    /**
     * Return a java visual object repersents this view.
     *
     * @return   a java visual object repersents this view.
     */
    public Component getViewComponent() {
        return mComp;
    }

    /**
     * Sets the viewComponent attribute of the BasicMapperView object
     *
     * @param comp  The new viewComponent value
     */
    public void setViewComponent(Component comp) {
        mComp = comp;
    }

    /**
     * Set the view model of this view should display.
     *
     * @param model  the link mode to display
     */
    public void setViewModel(IMapperViewModel model) {
        IMapperViewModel oldModel = mViewModel;
        mViewModel = model;
        firePropertyChange(IBasicMapperView.MODEL_CHANGE, model, oldModel);
    }

    /**
     * Return the current mapper view model of this view.
     *
     * @return   the mapper view model of this view.
     */
    public IMapperViewModel getViewModel() {
        return mViewModel;
    }

    /**
     * Return a name of this view.
     *
     * @return   a String repersentation of this view name.
     */
    public String getViewName() {
        return mViewName;
    }

    /**
     * Set a name of this view.
     *
     * @param name  a String repersentation of this view name.
     */
    public void setViewName(String name) {
        String oldName = mViewName;
        mViewName = name;
        firePropertyChange(IBasicMapperView.NAME_CHANGE,name,oldName);
    }

    /**
     * Return true if this view is mapping enable, false otherwise.
     *
     * @return   The droppable value
     */
    public boolean isMapable() {
        return mIsMapable;
    }

    /**
     * Set if this view is mapping enable.
     *
     * @param droppable  the flag indicates if this view is mapping enable.
     */
    public void setIsMapable(boolean mapable) {
        mIsMapable = mapable;
    }

    /**
     * Set the auto layout object of this view.
     *
     * @param autoLayout  the auto layout of this view.
     */
    public void setAutoLayout(IMapperAutoLayout autoLayout) {
        mAutoLayout = autoLayout;
    }

    /**
     * Return the auto layout object of this view.
     *
     * @return   the auto layout object of this view.
     */
    public IMapperAutoLayout getAutoLayout() {
        return mAutoLayout;
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.add(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.remove(listener);
    }

    /**
     * Throws UnsupportedOperationException, use IBasicMapper.addMapperListener
     * instead.
     *
     * @param listener  the IMapperListener to be added
     */
    public void addMapperListener(IMapperListener listener) {
        throw new UnsupportedOperationException("Use IBasicMapper.addMapperListener instead");
    }

    /**
     * Throws UnsupportedOperationException, use
     * IBasicMapper.removeMapperListener instead.
     *
     * @param listener  the IMapperListener to be added
     */
    public void removeMapperListener(IMapperListener listener) {
        throw new UnsupportedOperationException("Use IBasicMapper.removeMapperListener instead");
    }

    /**
     * Fire a specified property change event of this node.
     *
     * @param propertyName  the name of this property has changed
     * @param newValue      the new value of the property
     * @param oldValue      the old value of the property
     */
    protected void firePropertyChange(
        String propertyName,
        Object newValue,
        Object oldValue) {

        if (mPropertyListeners.size() > 0) {
            MapperUtilities.firePropertyChanged(
                (PropertyChangeListener[]) mPropertyListeners.toArray(
                new PropertyChangeListener[mPropertyListeners.size()]), this, propertyName, newValue, oldValue);
        }
    }
}
