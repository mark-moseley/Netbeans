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

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.*;

import java.awt.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;


/**
 *
 * @author Ramesh Dara
 */
public class CasaNodeWidgetEngineInternal extends CasaNodeWidgetEngine {
    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public CasaNodeWidgetEngineInternal(Scene scene) {
        this(scene, true);
    }
    
    public CasaNodeWidgetEngineInternal(Scene scene, boolean confStatus) {
        super(scene);
        setConfigurationStatus(confStatus);
        setTitleFont(CasaFactory.getCasaCustomizer().getFONT_SU_HEADER());
        setTitleColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_TITLE());
    }
    
    protected Color getBackgroundColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_REGION_ENGINE();
    }
    protected Color getPinHolderBorderColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER();
    }
    protected Color getPinHolderTitleColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_TITLE();
    }
    protected Color getPinHolderBackgroundColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BACKGROUND();
    }

    public GradientRectangleColorScheme getGradientColorSceheme() {
        GradientRectangleColorScheme colorScheme = CasaFactory.getCasaCustomizer().getGradientINT_SU_BACKGROUND();
        if(getState().isSelected()) {
            colorScheme.setBorderColor(CasaFactory.getCasaCustomizer().getCOLOR_SELECTED_BORDER());
        } else {
            colorScheme.setBorderColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER());
        }
        return colorScheme;
    }

}
