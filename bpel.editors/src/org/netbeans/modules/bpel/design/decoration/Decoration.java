/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.bpel.design.decoration;


public class Decoration {
    
    private DimmDescriptor dimmed;
    
    private GlowDescriptor glow;
    
    private StrokeDescriptor stroke;
    
    private TextstyleDescriptor textstyle;
    
    private ComponentsDescriptor components;
    
    
    
    public Decoration(){
        
    }
    
    public Decoration(Descriptor[] descriptors){
        for (Descriptor d : descriptors){
            if (d instanceof DimmDescriptor) {
                dimmed = (DimmDescriptor) d;
            } else if (d instanceof GlowDescriptor){
                glow = (GlowDescriptor) d;
            } else if (d instanceof StrokeDescriptor){
                stroke = (StrokeDescriptor) d;
            } else if (d instanceof TextstyleDescriptor){
                textstyle = (TextstyleDescriptor) d;
            } else if (d instanceof ComponentsDescriptor){
                components = (ComponentsDescriptor) d;
            }
        }
        
    }
    
    
    
    public GlowDescriptor getGlow() {
        return glow;
    }
    
    
    public boolean hasGlow() {
        return (glow != null);
    }
    
    
    public boolean hasDimmed() {
        return (dimmed != null);
    }
    
    
    public DimmDescriptor getDimmed() {
        return dimmed;
    }
    
    
    
    public StrokeDescriptor getStroke() {
        return stroke;
    }
    
    
    public boolean hasStroke() {
        return (stroke != null);
    }
    
    public TextstyleDescriptor getTextstyle(){
        return textstyle;
    }
    public boolean hasTextstyle(){
        return (textstyle != null);
    }
    
    
    public boolean  hasComponents(){
        return (components != null);
    }
    
    public ComponentsDescriptor getComponents(){
        return components;
    }
    
    
    
    
    public Decoration combineWith(Decoration d) {
        if (d == null){
            return this;
        }
        
        if (d.hasGlow()){
            glow = d.getGlow();
        }
        
        if (d.hasStroke()){
            stroke = d.getStroke();
        }
        
        if (d.hasDimmed()){
            dimmed = d.getDimmed();
        }
        
        if (d.hasTextstyle()){
            textstyle = d.getTextstyle();
        }
        
        if (d.hasComponents()){
            if (components == null){
                components = new ComponentsDescriptor();
            }
            components.addAll(d.getComponents());
        }
        return this;
    }
    
    
}
