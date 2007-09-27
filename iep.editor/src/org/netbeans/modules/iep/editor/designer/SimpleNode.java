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


package org.netbeans.modules.iep.editor.designer;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoCopyEnvironment;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoNode;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import org.openide.util.HelpCtx;

/**
 * A SimpleNode has an icon, a label, and up to two ports.
 * The label is a SimpleNodeLabel, positioned at the bottom center
 * of the icon.
 * The two ports are SimpleNodePort's, one for "input" and one for
 * "output", assuming a left-to-right "flow".
 * <p>
 * SimpleNode supports the notion of a minimum size.
 * Resizing the node now only resizes the icon,
 * not the label or the ports.  Furthermore, resizing the icon now
 * maintains its original aspect ratio.
 */
public class SimpleNode extends JGoNode implements HelpCtx.Provider  {
    /** Create an empty SimpleNode.  Call initialize() before using it. */
    public SimpleNode() {
        super();
    }
    
    // The location is the top-left corner of the node;
    // the size is the dimension of the icon;
    // the labeltext may be null if no label is desired.
    public void initialize(Point loc, Dimension size, JGoImage icon,
            String labeltext, boolean hasinport, boolean hasoutport) 
    {
        setInitializing(true);
        // the area as a whole is not directly selectable using a mouse,
        // but the area can be selected by trying to select any of its
        // children, all of whom are currently !isSelectable().
        setSelectable(true);
        setPickableBackground(true);
        // the user can move this node around
        setDraggable(true);
        // the user cannot resize this node
        setResizable(false);
        // if it does become resizable, only show four resize handles
        set4ResizeHandles(true);
        
        mIcon = icon;
        if (mIcon != null) {
            mIcon.setBoundingRect(loc,size);
            mIcon.setSelectable(false);
            addObjectAtHead(mIcon);
        }
        
        // the label is a SimpleNodeLabel, centered underneath the icon
        if (labeltext != null) {
            mLabel = new SimpleNodeLabel(labeltext, this);
        }
        
        // create an input port and an output port, each instances of SimpleNodePort
        if (hasinport) {
            mInputPort = new SimpleNodePort(true,this);
        }
        if (hasoutport) {
            mOutputPort = new SimpleNodePort(false,this);
        }
        
        setInitializing(false);
        layoutChildren(null);
        setTopLeft(loc);
    }
    
    protected void copyChildren(JGoArea newarea, JGoCopyEnvironment env) {
        SimpleNode newobj = (SimpleNode)newarea;
        
        super.copyChildren(newarea, env);
        
        newobj.mIcon = (JGoImage)env.get(mIcon);
        newobj.mLabel = (JGoText)env.get(mLabel);
        newobj.mInputPort = (JGoPort)env.get(mInputPort);
        newobj.mOutputPort = (JGoPort)env.get(mOutputPort);
    }
    
    /**
     * When an object is removed, make sure there are no more references from fields.
     */
    public JGoObject removeObjectAtPos(JGoListPosition pos) {
        JGoObject child = super.removeObjectAtPos(pos);
        if (child == mIcon) {
            mIcon = null;
        } else if (child == mLabel) {
            mLabel = null;
        } else if (child == mInputPort) {
            mInputPort = null;
        } else if (child == mOutputPort) {
            mOutputPort = null;
        }
        return child;
    }
    
    /**
     * Keep the parts of a SimpleNode positioned relative to each other
     * by setting their locations using some of the standard spots of
     * any JGoObject.
     */
    public void layoutChildren(JGoObject childchanged) {
        if (isInitializing()) {
            return;
        }
        setInitializing(true);
        
        JGoObject icon = getIcon();
        JGoObject label = getLabel();
        JGoObject inport = getInputPort();
        JGoObject outport = getOutputPort();
        
        if (label != null) {
            if (icon != null) {
                label.setSpotLocation(TopCenter, icon, BottomCenter);
            } else {
                label.setSpotLocation(BottomCenter, this, BottomCenter);
            }
        }
        if (inport != null) {
            if (icon != null) {
                inport.setSpotLocation(RightCenter, icon, LeftCenter);
            } else {
                inport.setSpotLocation(LeftCenter, this, LeftCenter);
            }
        }
        if (outport != null) {
            if (icon != null) {
                outport.setSpotLocation(LeftCenter, icon, RightCenter);
            } else {
                outport.setSpotLocation(RightCenter, this, RightCenter);
            }
        }
        
        setInitializing(false);
    }
    
    /**
     * If this object is resized, do the part positioning lay out again.
     * The ports and the text label do not get resized; only the icon
     * changes size, while keeping its old aspect ratio.
     */
    public void rescaleChildren(Rectangle prevRect) {
        // only change size of icon; need to calculate its new size while
        // keeping its old aspect ratio
        if (mIcon != null) {
            int oldw = mIcon.getWidth();
            int oldh = mIcon.getHeight();
            if (oldw <= 0) {
                oldw = 1;
            }
            double ratio = oldh/((double)oldw);
            // figure out how much space is left in the area after accounting
            // for any ports and label
            int iconw = getWidth();
            int iconh = getHeight();
            if (mInputPort != null) {
                iconw -= mInputPort.getWidth();
            }
            if (mOutputPort != null) {
                iconw -= mOutputPort.getWidth();
            }
            if (mLabel != null) {
                iconh -= mLabel.getHeight();
            }
            // now we have the maximum bounds for the icon, figure out the
            // right width and height that fit while maintaining the aspect ratio
            double maxratio = iconh/((double)iconw);
            if (ratio < maxratio) {
                iconh = (int)Math.rint(ratio*iconw);
            } else {
                iconw = (int)Math.rint(iconh/ratio);
            }
            mIcon.setSize(iconw, iconh);
        }
    }
    
    public Dimension getMinimumIconSize() {
        return new Dimension(16, 16);
    }
    
    public Dimension getMinimumSize() {
        int w = 0;
        int h = 0;
        // account for any ports and label
        if (mInputPort != null) {
            w += mInputPort.getWidth();
        }
        if (mOutputPort != null) {
            w += mOutputPort.getWidth();
        }
        // now account for any minimum desired icon size
        Dimension minIconSize = getMinimumIconSize();
        w += minIconSize.width;
        h += minIconSize.height;
        if (mLabel != null) {
            w = Math.max(w, mLabel.getWidth());
            h += mLabel.getHeight();
        }
        return new Dimension(w, h);
    }
    
    // constrain to the minimum width and height
    public void setBoundingRect(int left, int top, int width, int height) {
        Dimension minSize = getMinimumSize();
        super.setBoundingRect(left, top,
                Math.max(width, minSize.width),
                Math.max(height, minSize.height));
    }
    
    // limit the minimum width and height for resizing
    protected Rectangle handleResize(Graphics2D g, JGoView view, Rectangle prevRect,
            Point newPoint, int whichHandle, int event,
            int minWidth, int minHeight) {
        Dimension minSize = getMinimumSize();
        Rectangle newRect = super.handleResize(g, view, prevRect, newPoint, whichHandle, event,
                Math.max(minWidth, minSize.width), Math.max(minHeight, minSize.height));
        // resize continuously (default only does setBoundingRect on MouseUp)
        if (event == JGoView.EventMouseMove)
            setBoundingRect(newRect);
        return null;
    }
    
    /**
     * Let single click on a label mean start editing that label.
     * Because the label is not selectable, a mouse click will be passed
     * on up to its parent, which will be this area.
     */
    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {
        JGoText lab = getLabel();
        if (lab != null && lab.isEditable() && lab.isEditOnSingleClick()) {
            JGoObject obj = view.pickDocObject(dc, false);
            if (obj == lab && obj.getLayer() != null && obj.getLayer().isModifiable()) {
                lab.doStartEdit(view, vc);
                return true;
            }
        }
        return false;
    }
     public HelpCtx getHelpCtx() {
	return new HelpCtx("org.netbeans.modules.iep.editor.designer.SimpleNode");
    }
    
    public JGoText getLabel() { return mLabel; }
    public JGoImage getIcon() { return mIcon; }
    public JGoPort getInputPort() { return mInputPort; }
    public JGoPort getOutputPort() { return mOutputPort; }
    public boolean hasInputPort() {
        return mInputPort != null;
    }
    public boolean hasOutputPort() {
        return mOutputPort != null;
    }
    
    // State
    protected JGoText mLabel = null;
    protected JGoImage mIcon = null;
    protected JGoPort mInputPort = null;
    protected JGoPort mOutputPort = null;
    /**
     * A real application will have some other data associated with
     * the node, holding state and methods to be called according to
     * the needs of the application.
     */
}
