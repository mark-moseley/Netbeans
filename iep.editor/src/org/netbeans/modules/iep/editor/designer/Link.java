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

package org.netbeans.modules.iep.editor.designer;

import com.nwoods.jgo.*;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import java.util.logging.Level;

// Flows are implemented as labeled links
//
// For this example app, the only property, Text, is
// actually just the label's Text.
public class Link extends JGoLink implements GuiConstants, ComponentHolder {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(Link.class.getName());

    private static final JGoPen mPen = JGoPen.black;

    // State
    private transient TcgComponent mComponent;
    
    // Used for carring content of mComponent during cut, copy, and paste
    private String mComponentXml;

    public Link() {
        super();
    }
    
    public Link(TcgComponent component, JGoPort from, JGoPort to) {
        super(from, to);
        try {
            mComponent = component;
            EntityNode fromNode = getFromNode();
            if (fromNode != null) {
                String fromId = (String)fromNode.getComponent().getProperty(ID_KEY).getValue();
                component.getProperty(FROM_KEY).setValue(fromId);
            }
            EntityNode toNode = getToNode();
            if (toNode != null) {
                String toId = (String)toNode.getComponent().getProperty(ID_KEY).getValue();
                component.getProperty(TO_KEY).setValue(toId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   /**
    * Create a new instance of this object.
    * JGoDocument.copyFromCollection does not copy links until after both ports have been copied.
    *
    * Called when copy from PdCanvas to Clipboard
    *
    * @param env the environment for the copy operation - keeps track of copied objects.
    */
    public JGoObject copyObject(JGoCopyEnvironment env) {
        Link newObj = (Link)super.copyObject(env);
        newObj.mComponentXml = mComponent.toXml();
        return newObj;
    }

    
    public void initialize() {
        setJumpsOver(true);
        setAvoidsNodes(true);
        setAdjustingStyle(JGoLink.AdjustingStyleStretch);
        setPen(mPen);
    }
    
    public PdModel getDoc() { 
        return (PdModel)getDocument(); 
    }
    
    public EntityNode getFromNode() {
        if (getFromPort() != null) {
            return (EntityNode)(getFromPort().getParent());
        }
        return null;
    }
    
    public EntityNode getToNode() {
        if (getToPort() != null) {
            return (EntityNode)(getToPort().getParent());
        }
        return null;
    }
    
    // Normally you wouldn't need this--but this override of setOrthogonal
    // is here just because the user interface allows the link style
    // to be switched dynamically from Orthogonal to non-Orthogonal.
    // The overridden calculateStroke doesn't call removeAllPoints,
    // it just shifts the existing stroke points.  When the links
    // are orthogonal, extra stroke points are added, which causes
    // amusing behavior when the user switches back to non-orthogonal
    // style.
    public void setOrthogonal(boolean bOrtho) {
        if (bOrtho != isOrthogonal()) {
            removeAllPoints();
        }
        super.setOrthogonal(bOrtho);
    }

    //========================================
    public TcgComponent getComponent() {
        return mComponent;
    }

    public void releaseComponent() {
        mComponent = null;
    }
    
    //========================================
    /**
     * Used to paste from Clipboard to PdCanvas
     */
    public JGoObject copyObjectAndResetContextProperties(JGoCopyEnvironment env, Plan plan) {
        Link newObj = (Link)super.copyObject(env);
        TcgComponent component = null;
        try {
            component = TcgModelManager.getComponent("Clipboard", mComponentXml);
        } catch (Exception e) {
            mLog.log(Level.SEVERE, java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("Link.copyObjectAndResetContextProperties_failed"), e);
            return newObj;
        }
        // new id and name for newObj.mComponent
        newObj.mComponent = plan.copyAndAddLink(component);
        return newObj;
    }
    
    
}