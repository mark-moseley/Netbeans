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
package org.netbeans.modules.uml.drawingarea.palette.context;

import java.awt.Image;
import java.util.ArrayList;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.openide.loaders.DataObject;

/**
 * The ContextPaletteButtonModel interface specifies the data used to build
 * the buttons in the context palette.  A palette button can be a group of
 * buttons.  It is up to the palette implementation of how to display a 
 * group of actions.
 * 
 * @author treyspiva
 */
public interface ContextPaletteButtonModel 
{
    /**
     * Gives the ContextPaletteButtonModel the chance to initialize the buttons
     * details from a NetBeans DataObject
     * 
     * @param fo the data object.
     */
    public void initialize(DataObject fo);
    
    /**
     * Retrieves the actions that should be executed when the users presses 
     * the button.
     * 
     * @param scene The scene that contains the palettes associates widget.
     * @return The actions that need to be executed.
     */
    public WidgetAction[] createActions(Scene scene);
    
    /**
     * Checks if the palette button is a group of associated buttons.
     * @return true if the button is a true, false otherwise.
     */
    public boolean isGroup();
    
    /**
     * If the button is a group button, then getChildren will return the 
     * button models that are part of the group.
     * @return
     */
    public ArrayList<ContextPaletteButtonModel> getChildren();
    
    /**
     * The image that represents the button.
     * 
     * @return the image associated with the button.
     */
    public Image getImage();
    
    /**
     * The buttons name.
     * 
     * @return the name.
     */
    public String getName();
    
    /**
     * The buttons tooltip.
     * 
     * @return tooltip.
     */
    public String getTooltip();
    
    /**
     * Sets the image for the button.
     * @param image the image
     */
    public void setImage(Image image);
    
    /**
     * Sets the buttons tooltip.
     * @param tooltip the tooltip.
     */
    public void setTooltip(String tooltip);
    
    /**
     * Sets the ContextPaletteModel that is owns the button model.
     * @param model The owner.
     * @see ContextPaletteModel
     */
    public void setPaletteModel(ContextPaletteModel model);
    
    /**
     * Retrieves the ContextPaletteModel that owns the button model.
     * 
     * @return the owner.
     * @param ContextPaletteModel
     */
    public ContextPaletteModel getPaletteModel();
    
}
