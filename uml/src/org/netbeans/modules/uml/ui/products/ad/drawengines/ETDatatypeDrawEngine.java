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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.common.ETException;

public class ETDatatypeDrawEngine extends ETNodeDrawEngine
{
    protected final int NODE_HEIGHT = 45;
    protected final int NODE_WIDTH  = 105;
    protected final String STATIC_TEXT_FONT = "Arial-12";
    
    public String getElementType()
    {
        String type = super.getElementType();
        if (type == null)
        {
            type = new String("DataType"); // or "AliasedType"
        }
        return type;
    }
    
    public void initResources()
    {
        setFillColor("datatypefill", 223, 233, 243);
        setLightGradientFillColor("datatypelightgradient", 255, 255, 255);
        setBorderColor("datatypeborder", Color.BLACK);
        
        super.initResources();
    }
    
    public void initCompartments()
    {
        ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
        newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
        newClassNameList.setName("<<datatype>>");
        
        this.addCompartment(newClassNameList);
    }
    
    public void createCompartments() throws ETException
    {
        IETGraphObjectUI parentUI =  this.getParent();
        
        if (parentUI.getOwner() != null)
        {
            if (parentUI.getModelElement() != null)
            {
                IElement element = parentUI.getModelElement();
                createAndAddCompartment("ADClassNameListCompartment");
                
                INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
                if (pNameCompartment != null)
                {
                    pNameCompartment.attach(element);
                    setDefaultCompartment(pNameCompartment);
                    
                    String elementType = this.getElementType();
                    if (elementType.equals("DataType"))
                    {
                        pNameCompartment.setName("<<datatype>>");
                    }
                    else if (elementType.equals("AliasedType"))
                    {
                        pNameCompartment.setName("<<aliasedtype>>");
                    }
                }
            }
            else
            {
                this.initCompartments();
            }
        }
    }
    
    public void drawContents(IDrawInfo pDrawInfo)
    {
        TSEGraphics graphics = pDrawInfo.getTSEGraphics();
        IETNodeUI parentUI = (IETNodeUI)this.getParent();
        
        Color borderColor = getBorderBoundsColor();
        Color fillColor = getBkColor();
        
        if (parentUI.getOwner() != null)
        {
            IETRect deviceRect = pDrawInfo.getDeviceBounds();
            int x = deviceRect.getLeft();
            int y = deviceRect.getTop();
            int h = deviceRect.getIntHeight();
            int w = deviceRect.getIntWidth();
            float centerX = (float)deviceRect.getCenterX();
            GradientPaint paint = new GradientPaint(centerX,
                    deviceRect.getBottom(),
                    fillColor,
                    centerX,
                    deviceRect.getTop(),
                    getLightGradientFillColor());
            
            GDISupport.drawRectangle(graphics,deviceRect.getRectangle(), borderColor, paint);
            
            // Draw static text
            TSEFont originalFont = parentUI.getFont();
            parentUI.setFont(new TSEFont(STATIC_TEXT_FONT));
            Font staticTextFont = parentUI.getFont().getScaledFont(pDrawInfo.getFontScaleFactor());
            graphics.setFont(staticTextFont);
            graphics.setColor(borderColor);
            String elementType = this.getElementType(), staticText = "<<datatype>>";
            if (elementType.equals("AliasedType"))
            {
                staticText = "<<aliasedtype>>";
            }
            int staticTextX = graphics.getFontMetrics().stringWidth(staticText);
            int staticTextY = graphics.getFontMetrics().getHeight();
            graphics.drawString(staticText,
                    (int)(deviceRect.getLeft() + deviceRect.getWidth() / 2 - (graphics.getFontMetrics().stringWidth(staticText) / 2)),
                    y + graphics.getFontMetrics().getHeight());
            graphics.setFont(originalFont);
            
            //	Draw the compartments
            Iterator iterator = this.getCompartments().iterator();
            if (iterator.hasNext())
            {
                IListCompartment compartment = (IListCompartment) iterator.next();
                if (compartment instanceof ETClassNameListCompartment)
                {
                    compartment.draw(pDrawInfo, new ETRect(x, y + staticTextY, w, h - staticTextY));
                }
            }
        }
    }
    
        /*
         *  (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
         */
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        TSEGraphics graphics = pDrawInfo.getTSEGraphics();
        TSTransform transform = graphics.getTSTransform();
        IETNodeUI parentUI = (IETNodeUI)this.getParent();
        
        // Get static text size
        TSEFont originalFont = parentUI.getFont();
        parentUI.setFont(new TSEFont(STATIC_TEXT_FONT));
        
        // Use at 100 %, the scale with happen below.
        Font staticTextFont = originalFont.getFont();
        
        graphics.setFont(staticTextFont);
        String elementType = this.getElementType(), staticText = "<<datatype>>";
        if (elementType != null && elementType.equals("AliasedType"))
        {
            staticText = "<<aliasedtype>>";
        }
        
        int staticTextX = graphics.getFontMetrics().stringWidth(staticText);
        int staticTextY = graphics.getFontMetrics().getHeight();
        graphics.setFont(originalFont);
        
        // Get compartment size
        IETSize retVal = null;
        Iterator<ICompartment> iterator = this.getCompartments().iterator();
        while (iterator.hasNext())
        {
            ICompartment compartment = iterator.next();
            if (compartment instanceof ETClassNameListCompartment)
            {
                retVal = compartment.calculateOptimumSize(pDrawInfo, true);
                break;
            }
        }
        
        // Calculate size and return
        if (retVal != null)
        {
            retVal.setSize(Math.max(retVal.getWidth(), staticTextX), retVal.getHeight() + 2 * staticTextY);
            retVal.setSize(Math.max(retVal.getWidth(), NODE_WIDTH), Math.max(retVal.getHeight(), NODE_HEIGHT));
        }
        
        return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, transform);
    }
    
        /*
         *  (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
         */
    public String getDrawEngineID()
    {
        return "DataTypeDrawEngine";
    }
    
        /*
         *  (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
         */
    public long modelElementHasChanged(INotificationTargets pTargets)
    {
        try
        {
            this.clearCompartments();
            this.createCompartments();
        }
        catch (Exception e)
        {
        }
        return super.modelElementHasChanged(pTargets);
    }
    
    /**
     * Initializes our compartments by attaching modelelements to each. Previously existing compartments remain,
     * so if a compartment already exists it is reattached, if not one is created.
     *
     * @param pElement [in] The presentation element we are representing
     */
    public void initCompartments(IPresentationElement presEle)
    {
        // We may get here with no compartments.  This happens if we've been created
        // by the user.  If we read from a file then the compartments have been pre-created and
        // we just need to initialize them.
        int numCompartments = getNumCompartments();
        if (numCompartments == 0)
        {
            try
            {
                createCompartments();
            }
            catch(Exception e)
            {
            }
        }
        
        IElement pModelElement = presEle.getFirstSubject();
        
        String currentMetaType = getMetaTypeOfElement();
        if (pModelElement != null)
        {
            // Tell the name compartment about the model element it should display
            INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
            if (pNameCompartment != null)
            {
                pNameCompartment.attach(pModelElement);
                
                if (currentMetaType.equals("DataType"))
                {
                    //pNameCompartment.addStaticText("<<datatype>>");
                    pNameCompartment.setName("<<datatype>>");
                }
                else if (currentMetaType.equals("AliasedType"))
                {
                    //pNameCompartment.addStaticText("<<aliasedtype>>");
                    pNameCompartment.setName("<<aliasedtype>>");
                }
            }
        }
    }
}
