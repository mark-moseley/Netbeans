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

package org.netbeans.modules.javahelp;

import com.sun.java.help.impl.ViewAwareComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import java.net.MalformedURLException;
import java.net.URL;

import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 * This class is a lightweight component to be included in HTML content within
 * JHContentViewer. It invokes default IDE html browser to show external URL.
 * (Default browser should be external browser to show external URL properly.
 * Component is displayed as a mouse enabled Label. Only text is supported.
 * <p>
 * To use this class within HTML content use the &ltobject&gt tag. Below is an
 * example usage:
 * <p><pre>
 * &ltobject CLASSID="java:org.netbeans.module.javahelp.BrowserDisplayer"&gt
 * &ltparam name="content" value="http://www.netbeans.org"&gt
 * &ltparam name="text" value="Click here"&gt
 * &ltparam name="textFontFamily" value="SansSerif"&gt
 * &ltparam name="textFontSize" value="x-large"&gt
 * &ltparam name="textFontWeight" value="plain"&gt
 * &ltparam name="textFontStyle" value="italic"&gt
 * &ltparam name="textColor" value="red"&gt
 * &lt/object&gt
 * </pre><p>
 * Valid parameters are:
 * <ul>
 * <li>content - a valid external url like http://java.sun.com
 * @see setContent
 * <li>text - the text of the activator
 * @see setText
 * <li>textFontFamily - the font family of the activator text
 * @see setTextFontFamily
 * <li>textFontSize - the size of the activator text font. Size is specified
 * in a css terminology. See the setTextFontSize for acceptable syntax.
 * @see setTextFontSize
 * <li>textFontWeight - the activator text font weight
 * @see setTextFontWeight
 * <li>textFontStyle - the activator text font style
 * @see setTextFontStyle
 * <li>textColor - the activator text color
 * @see setTextColor
 * <ul>
 *
 * @author Marek Slama
 */
public class BrowserDisplayer extends JButton implements ActionListener, ViewAwareComponent {
    private View myView;
    private SimpleAttributeSet textAttribs;
    private HTMLDocument doc;
    private URL base;

    private final static Cursor handCursor =
	Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private Cursor origCursor;

    /**
     * Create a secondaryviewer. By default the viewer creates a button with
     * the text of ">"
     */
    public BrowserDisplayer() {
	super();
	setMargin(new Insets(0,0,0,0));
	createLinkLabel();
	addActionListener(this);
	origCursor = getCursor();
        getAccessibleContext().setAccessibleDescription
        (NbBundle.getMessage(BrowserDisplayer.class,"ACSD_Label"));
	addMouseListener(new MouseListener() {
	    public void mouseClicked(MouseEvent e) {
	    }

	    public void mouseEntered(MouseEvent e) {
		setCursor(handCursor);
	    }

	    public void mouseExited(MouseEvent e) {
		setCursor(origCursor);
	    }

	    public void mousePressed(MouseEvent e) {
	    }

	    public void mouseReleased(MouseEvent e) {
	    }
	});
    }
    
    /**
     * Sets data optained from the View
     */
    public void setViewData(View v) {
	myView = v;
	doc = (HTMLDocument) myView.getDocument();
	base = doc.getBase();

	// Set the current font information in the local text attributes
	Font font = getFont();
	textAttribs = new SimpleAttributeSet();
	textAttribs.removeAttribute(StyleConstants.FontSize);
	textAttribs.removeAttribute(StyleConstants.Bold);
	textAttribs.removeAttribute(StyleConstants.Italic);
	textAttribs.addAttribute(StyleConstants.FontFamily,
				 font.getName());
	textAttribs.addAttribute(StyleConstants.FontSize,
				 new Integer(font.getSize()));
	textAttribs.addAttribute(StyleConstants.Bold,
				 Boolean.valueOf(font.isBold()));
	textAttribs.addAttribute(StyleConstants.Italic,
				 Boolean.valueOf(font.isItalic()));
    }
    
    /**
     *  properties
     */
    private String content = "";

    /**
     * Set the content for the secondary viewer
     * @param content a valid URL
     */
    public void setContent(String content) {
	this.content = content;
    }

    /**
     * Returns the content of the secondary viewer
     */
    public String getContent() {
	return content;
    }

    /**
     * Creates a link label. A link label is a form of a JButton but without a
     * button like appearance.
     */
    private void createLinkLabel() {
	setBorder(new EmptyBorder(1,1,1,1));
	setBorderPainted(false);
	setFocusPainted(false);
	setAlignmentY(getPreferredLabelAlignment());
	setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
	setBackground(UIManager.getColor("EditorPane.background"));
	if (textAttribs != null &&
	    textAttribs.isDefined(StyleConstants.Foreground)) {
	    setForeground((Color)textAttribs.getAttribute(StyleConstants.Foreground));
	} else {
	    setForeground(Color.blue);
	}
	invalidate();
    }

    /**
     * Determine the alignment offset so the text is aligned with other views
     * correctly.
     */
    private float getPreferredLabelAlignment() {
        Icon icon = (Icon)getIcon();
        String text = getText();

        Font font = getFont();
        FontMetrics fm = getToolkit().getFontMetrics(font);
          
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

        SwingUtilities.layoutCompoundLabel(
            this, fm, text, icon,
            getVerticalAlignment(), getHorizontalAlignment(),
            getVerticalTextPosition(), getHorizontalTextPosition(),
            viewR, iconR, textR,
	    (text == null ? 0 : ((BasicButtonUI)ui).getDefaultTextIconGap(this))
        );

        // The preferred size of the button is the size of 
        // the text and icon rectangles plus the buttons insets.
        Rectangle r = iconR.union(textR);

        Insets insets = getInsets();
        r.height += insets.top + insets.bottom;

        // Ensure that the height of the button is odd,
        // to allow for the focus line.
        if(r.height % 2 == 0) { 
	    r.height += 1;
	}

	float offAmt = fm.getMaxAscent() + insets.top;
	return offAmt/(float)r.height;
    }
    
    /**
     * Sets the text Font family for the activator text.
     * For JDK 1.1 this must a family name of Dialog, DialogInput, Monospaced, 
     * Serif, SansSerif, or Symbol.
     */
    public void setTextFontFamily(String family) {
	textAttribs.removeAttribute(StyleConstants.FontFamily);
	textAttribs.addAttribute(StyleConstants.FontFamily, family);
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font family name of the activator text
     */
    public String getTextFontFamily() {
	return StyleConstants.getFontFamily(textAttribs);
    }

    /**
     * Sets the text size for the activator text.
     * The String size is a valid Cascading Style Sheet value for
     * text size. Acceptable values are as follows:
     * <ul>
     * <li>xx-small
     * <li>x-small
     * <li>small
     * <li>medium
     * <li>large
     * <li>x-large
     * <li>xx-large
     * <li>bigger - increase the current base font size by 1
     * <li>smaller - decrease the current base font size by 1
     * <li>xxpt - set the font size to a specific pt value of "xx"
     * <li>+x - increase the current base font size by a value of "x"
     * <li>-x - decrease the current base font size by a value of "x"
     * <li>x - set the font size to the point size associated with 
     * the index "x"
     * </ul>
     */
    public void setTextFontSize(String size) {
	int newsize;
	StyleSheet css = doc.getStyleSheet();
	try {
	    if (size.equals("xx-small")) {
		newsize = (int)css.getPointSize(0);
	    } else if (size.equals("x-small")) {
		newsize = (int)css.getPointSize(1);
	    } else if (size.equals("small")) {
		newsize = (int)css.getPointSize(2);
	    } else if (size.equals("medium")) {
		newsize = (int)css.getPointSize(3);
	    } else if (size.equals("large")) {
		newsize = (int)css.getPointSize(4);
	    } else if (size.equals("x-large")) {
		newsize = (int)css.getPointSize(5);
	    } else if (size.equals("xx-large")) {
		newsize = (int)css.getPointSize(6);
	    } else if (size.equals("bigger")) {
		newsize = (int)css.getPointSize("+1");
	    } else if (size.equals("smaller")) {
		newsize = (int)css.getPointSize("-1");
	    } else if (size.endsWith("pt")) {
		String sz = size.substring(0, size.length() - 2);
		newsize = Integer.parseInt(sz);
	    } else {
		newsize = (int) css.getPointSize(size);
	    }
	} catch (NumberFormatException nfe) {
	    return;
	}
	if (newsize == 0) {
	    return;
	}
	textAttribs.removeAttribute(StyleConstants.FontSize);
	textAttribs.addAttribute(StyleConstants.FontSize,
				 new Integer(newsize));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }
    
    /**
     * Returns the text Font family name of the activator text
     */
    public String getTextFontSize() {
	return Integer.toString(StyleConstants.getFontSize(textAttribs));
    }

    /**
     * Sets the text Font Weigth for the activator text.
     * Valid weights are
     * <ul>
     * <li>plain
     * <li>bold
     * </ul>
     */
    public void setTextFontWeight(String weight) {
	boolean isBold=false;
	if ("bold".equals(weight)) {
	    isBold = true;
	} else {
	    isBold = false;
	}
	textAttribs.removeAttribute(StyleConstants.Bold);
	textAttribs.addAttribute(StyleConstants.Bold, Boolean.valueOf(isBold));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font weight of the activator text
     */
    public String getTextFontWeight() {
	if (StyleConstants.isBold(textAttribs)) {
	    return "bold";
	}
	return "plain";
    }

    /**
     * Sets the text Font Style for the activator text.
     * Valid font styles are
     * <ul>
     * <li>plain
     * <li>italic
     * </ul>
     */
    public void setTextFontStyle(String style) {
	boolean isItalic=false;
	if ("italic".equals(style)) {
	    isItalic = true;
	} else {
	    isItalic = false;
	}
	textAttribs.removeAttribute(StyleConstants.Italic);
	textAttribs.addAttribute(StyleConstants.Italic, Boolean.valueOf(isItalic));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font style of the activator text
     */
    public String getTextFontStyle() {
	if (StyleConstants.isItalic(textAttribs)) {
	    return "italic";
	}
	return "plain";
    }

    /**
     * Sets the text Color for the activator text.
     * The following is a list of supported Color names
     * <ul>
     * <li>black
     * <li>blue
     * <li>cyan
     * <li>darkGray
     * <li>gray
     * <li>green
     * <li>lightGray
     * <li>magenta
     * <li>orange
     * <li>pink
     * <li>red
     * <li>white
     * <li>yellow
     * </ul>
     */
    public void setTextColor(String name) {
	Color color=null;
	if ("black".equals(name)) {
	    color = Color.black;
	} else if ("blue".equals(name)) {
	    color = Color.blue;
	} else if ("cyan".equals(name)) {
	    color = Color.cyan;
	} else if ("darkGray".equals(name)) {
	    color = Color.darkGray;
	} else if ("gray".equals(name)) {
	    color = Color.gray;
	} else if ("green".equals(name)) {
	    color = Color.green;
	} else if ("lightGray".equals(name)) {
	    color = Color.lightGray;
	} else if ("magenta".equals(name)) {
	    color = Color.magenta;
	} else if ("orange".equals(name)) {
	    color = Color.orange;
	} else if ("pink".equals(name)) {
	    color = Color.pink;
	} else if ("red".equals(name)) {
	    color = Color.red;
	} else if ("white".equals(name)) {
	    color = Color.white;
	} else if ("yellow".equals(name)) {
	    color = Color.yellow;
	}

	if (color == null) {
	    return;
	}
	textAttribs.removeAttribute(StyleConstants.Foreground);
	textAttribs.addAttribute(StyleConstants.Foreground, color);
	setForeground(color);
    }

    /**
     * Returns the text Color of the activator text
     */
    public String getTextColor() {
	Color color = getForeground();
	return color.toString();
    }

    /**
     * Gets the font from an attribute set.  This is
     * implemented to try and fetch a cached font
     * for the given AttributeSet, and if that fails 
     * the font features are resolved and the
     * font is fetched from the low-level font cache.
     * Font's are cached in the StyleSheet of a document
     *
     * @param attr the attribute set
     * @return the font
     */
    private Font getAttributeSetFont(AttributeSet attr) {
        // PENDING(prinz) add cache behavior
        int style = Font.PLAIN;
        if (StyleConstants.isBold(attr)) {
            style |= Font.BOLD;
        }
        if (StyleConstants.isItalic(attr)) {
            style |= Font.ITALIC;
        }
        String family = StyleConstants.getFontFamily(attr);
        int size = StyleConstants.getFontSize(attr);

	/**
	 * if either superscript or subscript is
	 * is set, we need to reduce the font size
	 * by 2.
	 */
	if (StyleConstants.isSuperscript(attr) ||
	    StyleConstants.isSubscript(attr)) {
	    size -= 2;
	}

	// fonts are cached in the StyleSheet so use that
        return doc.getStyleSheet().getFont(family, style, size);
    }

    /**
     * Displays the viewer according to the viewerType
     */
    public void actionPerformed(ActionEvent e) {
        URL link;
        try {
            link = new URL(content);
        } catch (MalformedURLException exc) {
            //XXX log something to ide.log??
            return;
        }
        HtmlBrowser.URLDisplayer.getDefault().showURL(link);
    }

}
