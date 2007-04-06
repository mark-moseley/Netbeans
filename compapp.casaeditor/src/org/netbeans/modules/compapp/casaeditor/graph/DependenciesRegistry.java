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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author jsandusky
 */
public class DependenciesRegistry {
    
    private List<Widget.Dependency> mDependencies;
    private Widget mWidget;
    
    
    public DependenciesRegistry(Widget widget) {
        mWidget = widget;
    }
    
    
    public void removeAllDependencies() {
        if (mDependencies != null) {
            for (Widget.Dependency dependency : mDependencies) {
                mWidget.removeDependency(dependency);
            }
        }
    }
    
    public void registerDependency(Widget.Dependency dependency) {
        if (mDependencies == null) {
            mDependencies = new ArrayList();
        }
        mDependencies.add(dependency);
        mWidget.addDependency(dependency);
    }
}
