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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget that supports multi-line text.
 * The text is automatically centered horizontally.
 * To center the text vertically, ensure this widget stretches to the full
 * height of whatever widget contains this widget.
 * 
 * @author Josh Sandusky
 */
public class LineBreakingLabelWidget extends Widget {
    
    private static final int MINIMUM_WIDTH = 100;
    private static final String[] ELLIPSES = { "..." };
    
    private String mLabel;
    private Color mTextColor;
    private Font mFont;
    private LabelWidget[] mLabelWidgets;
    private Dimension mLastPreferredSize = new Dimension();
    private LineBreakMeasurer mLineMeasurer;
    private int mParagraphStart;
    private int mParagraphEnd;
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    
    
    public LineBreakingLabelWidget(final Scene scene) {
        super(scene);
        
        // Set up a vertical layout of label widgets, justified to the same width.
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 4));
    }
    
    
    protected void notifyAdded() {
        mDependenciesRegistry.registerDependency(new Widget.Dependency() {
            private boolean mIsIgnore;
            public void revalidateDependency() {
                if (mIsIgnore || getBounds() == null) {
                    return;
                }
                if (reBreak()) {
                    try {
                        mIsIgnore = true;
                        getScene().validate();
                    } finally {
                        mIsIgnore = false;
                    }
                }
            }
        });
    }
    
    protected void notifyRemoved() {
        mDependenciesRegistry.removeAllDependencies();
    }
    
    public void setText(String label, Color textColor, Font font) {
        mLabel = label;
        mTextColor = textColor;
        mFont = font;
        
        AttributedString attributedString = new AttributedString(label, mFont.getAttributes());
        AttributedCharacterIterator paragraph = attributedString.getIterator();
        mParagraphStart = paragraph.getBeginIndex();
        mParagraphEnd = paragraph.getEndIndex();
        
        // Create a new LineBreakMeasurer from the paragraph.
        mLineMeasurer = new LineBreakMeasurer(
                paragraph, 
                new FontRenderContext(null, false, false));

        reBreak();
    }
    
    private boolean reBreak() {
        if (getPreferredBounds() == null || getPreferredBounds().getSize().equals(mLastPreferredSize)) {
            // As long as our size remains the same, line-breaking will not change.
            return false;
        }
        mLastPreferredSize = getPreferredBounds().getSize();
        
        boolean isValidateRequired = false;
        String[] stringWithLineBreaks = calculateLineBreaks(mLabel);
        if (stringWithLineBreaks == null) {
            return false;
        }
        
        if (
                mLabelWidgets != null && 
                stringWithLineBreaks.length == mLabelWidgets.length) {
            // Just update the text, no need to remove/add widgets.
            for (int i=0; i < stringWithLineBreaks.length; i++) {
                mLabelWidgets[i].setLabel(stringWithLineBreaks[i]);
            }
        } else {
            // Each line is represented with its own single-line label widget.
            isValidateRequired = true;
            removeChildren();
            mLabelWidgets = new LabelWidget[stringWithLineBreaks.length];
            for (int i=0; i < stringWithLineBreaks.length; i++) {
                mLabelWidgets[i] = new LabelWidget(getScene(), stringWithLineBreaks[i]);
                mLabelWidgets[i].setAlignment(LabelWidget.Alignment.CENTER);
                mLabelWidgets[i].setForeground(mTextColor);
                mLabelWidgets[i].setFont(mFont);
                addChild(mLabelWidgets[i]);
            }
        }
        
        return isValidateRequired;
    }
    
    public void animateVisible(Color color) {
        mTextColor = color;
        if (mLabelWidgets != null) {
            for (LabelWidget labelWidget : mLabelWidgets) {
                labelWidget.setForeground(color);
                // forego the animation, this seems to not work reliably
//                getScene().getSceneAnimator().animateForegroundColor(labelWidget, color);
            }
        }
    }
    
    private String[] calculateLineBreaks(String str) {
        // Only use 3/4 of the width for our text.
        float formatWidth = (float) getPreferredBounds().width * 0.75f;
        if (formatWidth <= 0) {
            return null;
        } else if (formatWidth < MINIMUM_WIDTH) {
            return ELLIPSES;
        }
        
        mLineMeasurer.setPosition(mParagraphStart);
        int oldPos = mParagraphStart;
        List<String> strings = new ArrayList<String>();
        
        // Get lines from lineMeasurer until the entire
        // paragraph has been displayed.
        while (mLineMeasurer.getPosition() < mParagraphEnd) {
            int pos = mLineMeasurer.nextOffset(formatWidth);
            mLineMeasurer.setPosition(pos);
            strings.add(str.substring(oldPos, pos));
            oldPos = pos;
        }

        return (String[]) strings.toArray(new String[strings.size()]);
    }
}
