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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.core.windows.view;


import java.util.ArrayList;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Class which handles controller requests.
 *
 * @author  Peter Zavadsky
 */
public interface ControllerHandler {

    public void userActivatedMode(ModeImpl mode);

    public void userActivatedModeWindow(ModeImpl mode);

    public void userActivatedEditorWindow();
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState);
    
    public void userChangedSplit( ModelElement[] snapshots, double[] splitWeights );
    
    public void userClosedTopComponent(ModeImpl mode, TopComponent tc);
    
    public void userClosedMode(ModeImpl mode);
    
    // Helpers>>
    public void userResizedMainWindowBoundsSeparatedHelp(Rectangle bounds);
    
    public void userResizedEditorAreaBoundsHelp(Rectangle bounds);
    
    public void userResizedModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    // Helpers<<
    
    // DnD>>
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, int index);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs);
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side, int modeKind);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds, int modeKind);
    // DnD<<

    // undock/dock
    public void userUndockedTopComponent(TopComponent tc, ModeImpl mode);

    public void userDockedTopComponent(TopComponent tc, ModeImpl mode);

    // Sliding>>
    public void userEnabledAutoHide(TopComponent tc, ModeImpl source, String target);
    
    public void userDisabledAutoHide(TopComponent tc, ModeImpl source);
    
    public void userResizedSlidingMode(ModeImpl mode, Rectangle rect);
    // Sliding<<
    
}

