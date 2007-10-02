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
package org.netbeans.modules.print.impl.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.modules.print.impl.util.Pattern;
import org.netbeans.modules.print.impl.util.Percent;
import org.netbeans.modules.print.impl.util.Util;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.02.14
 */
final class Attribute extends Dialog
  implements FocusListener, Pattern.Listener, Percent.Listener
{
  Attribute(Preview preview) {
    myPreview = preview;

    myBorderColorValue = Util.getOption().getBorderColor();
    myTextColorValue = Util.getOption().getTextColor();
    myTextFontValue = Util.getOption().getTextFont();
    myBackgroundColorValue = Util.getOption().getBackgroundColor();

    myHeaderColorValue = Util.getOption().getHeaderColor();
    myHeaderFontValue = Util.getOption().getHeaderFont();
    myFooterColorValue = Util.getOption().getFooterColor();
    myFooterFontValue = Util.getOption().getFooterFont();
  }

  @Override
  protected DialogDescriptor createDescriptor()
  {
    myDescriptor = new DialogDescriptor(
      getResizable(createPanel()),
      i18n("LBL_Options"), // NOI18N
      true,
      getButtons(),
      DialogDescriptor.OK_OPTION,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (DialogDescriptor.OK_OPTION == event.getSource()) {
            if (updatePreview()) {
              myDescriptor.setClosingOptions(
                new Object [] { DialogDescriptor.OK_OPTION,
                  DialogDescriptor.CANCEL_OPTION });
            }
            else {
              myDescriptor.setClosingOptions(
                new Object [] { DialogDescriptor.CANCEL_OPTION });
            }
          }
        }
      }
    );
    myDescriptor.setHelpCtx(new HelpCtx(Attribute.class));
    
    return myDescriptor;
  }

  public void invalidValue(String value) {
//out("INVALID value: " + value);
    printError(i18n("ERR_Zoom_Value_Is_Invalid")); // NOI18N
  }

  private Object [] getButtons() {
    return new Object [] {
      DialogDescriptor.OK_OPTION,
      createButton(
        new ButtonAction(i18n("LBL_Apply"), i18n("TLT_Apply")) { // NOI18N
          public void actionPerformed(ActionEvent event) {
            updatePreview();
          }
        }
      ),
      DialogDescriptor.CANCEL_OPTION
    };
  }

  private boolean updatePreview() {
    int zoomWidth = Util.getInt(myZoomWidth.getText());
    int zoomHeight = Util.getInt(myZoomHeight.getText());

    if ( !checkValue(zoomWidth, zoomHeight)) {
      return false;
    }
    Util.getOption().setBorder(myBorder.isSelected());
    Util.getOption().setBorderColor(myBorderColorValue);

    Util.getOption().setHeader(myHeader.isSelected());
    Util.getOption().setHeaderLeft(myHeaderLeft.getText());
    Util.getOption().setHeaderCenter(myHeaderCenter.getText());
    Util.getOption().setHeaderRight(myHeaderRight.getText());
    Util.getOption().setHeaderColor(myHeaderColorValue);
    Util.getOption().setHeaderFont(myHeaderFontValue);

    Util.getOption().setFooter(myFooter.isSelected());
    Util.getOption().setFooterLeft(myFooterLeft.getText());
    Util.getOption().setFooterCenter(myFooterCenter.getText());
    Util.getOption().setFooterRight(myFooterRight.getText());
    Util.getOption().setFooterColor(myFooterColorValue);
    Util.getOption().setFooterFont(myFooterFontValue);

    Util.getOption().setWrapLines(myWrapLines.isSelected());
    Util.getOption().setLineNumbers(myLineNumbers.isSelected());
    Util.getOption().setUseFont(myUseFont.isSelected());
    Util.getOption().setUseColor(myUseColor.isSelected());
    Util.getOption().setTextColor(myTextColorValue);
    Util.getOption().setTextFont(myTextFontValue);
    Util.getOption().setBackgroundColor(myBackgroundColorValue);
    Util.getOption().setLineSpacing(getDouble(myLineSpacing.getValue()));
    Util.getOption().setAsEditor(myAsEditor.isSelected());

    double zoom = 0.0;

    if (myZoomFactor.isEnabled()) {
      zoom = myZoomFactor.getValue();
    }
    else if (myZoomWidth.isEnabled()) {
      zoom = Percent.createZoomWidth(zoomWidth);
    }
    else if (myZoomHeight.isEnabled()) {
      zoom = Percent.createZoomHeight(zoomHeight);
    }
    Util.getOption().setZoom(zoom);
    myPreview.updated();
//out("SAVE.zoom: " + zoom);

    return true;
  }

  private boolean checkValue(int zoomWidth, int zoomHeight) {
    if (myHeaderFontValue.getSize() > MAX_HEADER_SIZE) {
      printError(i18n("ERR_Header_Size_Is_Too_Big")); // NOI18N
      return false;
    }
    if (myFooterFontValue.getSize() > MAX_FOOTER_SIZE) {
      printError(i18n("ERR_Footer_Size_Is_Too_Big")); // NOI18N
      return false;
    }
    if (zoomWidth <= 0 || zoomHeight <= 0) {
      printError(i18n("ERR_Page_Number_Is_Invalid")); // NOI18N
      return false;
    }
    if (zoomWidth > MAX_PAGE_NUBER || zoomHeight > MAX_PAGE_NUBER) {
      printError(i18n("ERR_Page_Number_Is_Too_Big")); // NOI18N
      return false;
    }
    return true;
  }

  private JPanel createPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    // border
    panel.add(createSeparator(i18n("LBL_Print_Options")), c); // NOI18N
    panel.add(getBorderPanel(), c);

    // header & footer
    panel.add(createSeparator(i18n("LBL_Header_Footer")), c); // NOI18N
    panel.add(getTitlePanel(), c);

    // text
    panel.add(createSeparator(i18n("LBL_Text")), c); // NOI18N
    panel.add(getTextPanel(), c);

    // zoom
    panel.add(createSeparator(i18n("LBL_Zoom")), c); // NOI18N
    panel.add(getZoomPanel(), c);

    updateControl();

    return panel;
  }

  private JPanel getBorderPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    // border
    myBorder = createCheckBox(
      new ButtonAction(i18n("LBL_Print_Border")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myBorderColor.setEnabled(myBorder.isSelected());
        }
      }
    );
    panel.add(myBorder, c);

    // border.color
    c.weightx = 1.0;
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    myBorderColor = createButton(
      new ButtonAction(
        icon(Util.class, "color"), // NOI18N
        i18n("TLT_Border_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          borderColor();
        }
      }
    );
    panel.add(myBorderColor, c);

    // page setup
    c.anchor = GridBagConstraints.EAST;
    JButton button = createButton(
      new ButtonAction(i18n("LBL_PageSetup"), i18n("TLT_Page_Setup")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          if (Util.getOption().showPageSetup()) {
            updatePreview();
          }
        }
      }
    );
    panel.add(button, c);

    return panel;
  }

  private JPanel getTitlePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    setLabelPanel(panel, c);
    setHeaderPanel(panel, c);
    setFooterPanel(panel, c);
    setPatternPanel(panel, c);
//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));

    return panel;
  }

  private void setLabelPanel(JPanel panel, GridBagConstraints c) {
    // []
    c.gridy++;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    panel.add(new JLabel(), c);

    // left
    panel.add(createLabel(i18n("LBL_Left")), c); // NOI18N

    // center
    panel.add(createLabel(i18n("LBL_Center")), c); // NOI18N

    // right
    panel.add(createLabel(i18n("LBL_Right")), c); // NOI18N
  }

  private void setHeaderPanel(JPanel panel, GridBagConstraints c) {
    // header
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.WEST;
    myHeader = createCheckBox(
      new ButtonAction(i18n("LBL_Print_Header")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          boolean enabled = myHeader.isSelected();
          myHeaderLeft.setEnabled(enabled);
          myHeaderCenter.setEnabled(enabled);
          myHeaderRight.setEnabled(enabled);
          myHeaderColor.setEnabled(enabled);
          myHeaderFont.setEnabled(enabled);
        }
      }
    );
    panel.add(myHeader, c);

    // header left
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myHeaderLeft = new JTextField();
    setWidthFocused(myHeaderLeft, FIELD_WIDTH);
    panel.add(myHeaderLeft, c);

    // header center
    myHeaderCenter = new JTextField();
    setWidthFocused(myHeaderCenter, FIELD_WIDTH);
    panel.add(myHeaderCenter, c);

    // header right
    myHeaderRight = new JTextField();
    setWidthFocused(myHeaderRight, FIELD_WIDTH);
    panel.add(myHeaderRight, c);

    // header.color
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    myHeaderColor = createButton(
      new ButtonAction(
        icon(Util.class, "color"), // NOI18N
        i18n("TLT_Header_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          headerColor();
        }
      }
    );
    panel.add(myHeaderColor, c);

    // header font
    myHeaderFont = createButton(
      new ButtonAction(icon(Util.class, "font"), i18n("TLT_Header_Font")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          headerFont();
        }
      }
    );
    panel.add(myHeaderFont, c);
  }

  private void setFooterPanel(JPanel panel, GridBagConstraints c) {
    // footer
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.WEST;
    myFooter = createCheckBox(
      new ButtonAction(i18n("LBL_Print_Footer")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          boolean enabled = myFooter.isSelected();
          myFooterLeft.setEnabled(enabled);
          myFooterCenter.setEnabled(enabled);
          myFooterRight.setEnabled(enabled);
          myFooterColor.setEnabled(enabled);
          myFooterFont.setEnabled(enabled);
        }
      }
    );
    panel.add(myFooter, c);

    c.weightx = 1.0;
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myFooterLeft = new JTextField();
    setWidthFocused(myFooterLeft, FIELD_WIDTH);
    panel.add(myFooterLeft, c);

    // footer center
    myFooterCenter = new JTextField();
    setWidthFocused(myFooterCenter, FIELD_WIDTH);
    panel.add(myFooterCenter, c);

    // footer right
    myFooterRight = new JTextField();
    setWidthFocused(myFooterRight, FIELD_WIDTH);
    panel.add(myFooterRight, c);

    // footer color
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    myFooterColor = createButton(
      new ButtonAction(
        icon(Util.class, "color"), // NOI18N
        i18n("TLT_Footer_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          footerColor();
        }
      }
    );
    panel.add(myFooterColor, c);

    // footer font
    myFooterFont = createButton(
      new ButtonAction(icon(Util.class, "font"), i18n("TLT_Footer_Font")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          footerFont();
        }
      }
    );
    panel.add(myFooterFont, c);
  }

  private void setPatternPanel(JPanel panel, GridBagConstraints c) {
    JPanel p = new JPanel(new GridBagLayout());

    // []
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.CENTER;
    panel.add(new JLabel(), c);

    // buttons
    for (Pattern pattern : Pattern.values()) {
      JButton button = pattern.getButton(this);
      button.setEnabled(false);
      p.add(button, c);
    }

    // patterns
    c.weightx = 1.0;
    c.insets = new Insets(SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    c.gridwidth = 1 + 1 + 1;
    panel.add(p, c);
  }

  public void pressed(Pattern pattern) {
    mySelectedField = getSelectedTextField();
//out(field);

    if (mySelectedField != null) {
//out("Set pattern: " + pattern);
//out("     select: " + mySelectedField.getSelectionStart() + " " + mySelectedField.getSelectionEnd());
      String text = mySelectedField.getText();
      String head = text.substring(0, mySelectedField.getSelectionStart());
      String tail = text.substring(mySelectedField.getSelectionEnd(), text.length());

      mySelectedField.setText(head + pattern.getName() + tail);
    }
  }

  private JTextField getSelectedTextField() {
    if (myHeaderLeft.hasFocus()) {
      return myHeaderLeft;
    }
    if (myHeaderCenter.hasFocus()) {
      return myHeaderCenter;
    }
    if (myHeaderRight.hasFocus()) {
      return myHeaderRight;
    }
    if (myFooterLeft.hasFocus()) {
      return myFooterLeft;
    }
    if (myFooterCenter.hasFocus()) {
      return myFooterCenter;
    }
    if (myFooterRight.hasFocus()) {
      return myFooterRight;
    }
    return null;
  }

  private JPanel getTextPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    createTopTextPanel(panel, c);
    createBottomTextPanel(panel, c);

    return panel;
  }

  private void createTopTextPanel(JPanel panel, GridBagConstraints c) {
    // line numbers
    c.gridy++;
    myLineNumbers = createCheckBox(
      new ButtonAction(i18n("LBL_Line_Numbers")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myLineNumbers, c);

    // use color
    myUseColor = createCheckBox(
      new ButtonAction(i18n("LBL_Use_Color"), i18n("TLT_Use_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myUseColor, c);

    // text label
    panel.add(createLabel(i18n("LBL_Text_Font_and_Color")), c); // NOI18N

    // text color
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    myTextColor = createButton(
      new ButtonAction(icon(Util.class, "color"), i18n("TLT_Text_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          textColor();
        }
      }
    );
    panel.add(myTextColor, c);
    
    // text font
    myTextFont = createButton(
      new ButtonAction(icon(Util.class, "font"), i18n("TLT_Text_Font")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          textFont();
        }
      }
    );
    panel.add(myTextFont, c);
  }

  private void createBottomTextPanel(JPanel panel, GridBagConstraints c) {
    // wrap lines
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    myWrapLines = createCheckBox(
      new ButtonAction(i18n("LBL_Wrap_Lines")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myWrapLines, c);

    // use font
    myUseFont = createCheckBox(
      new ButtonAction(i18n("LBL_Use_Font"), i18n("TLT_Use_Font")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myUseFont, c);

    // background label
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    panel.add(createLabel(i18n("LBL_Background")), c); // NOI18N

    // background color
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    myBackgroundColor = createButton(
      new ButtonAction(
        icon(Util.class, "color"), // NOI18N
        i18n("TLT_Background_Color")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          backgroundColor();
        }
      }
    );
    panel.add(myBackgroundColor, c);
    
    // []
    c.gridy++;
    c.weightx = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    myAsEditor = createCheckBox(
      new ButtonAction(i18n("LBL_As_Editor"), i18n("TLT_As_Editor")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myAsEditor, c);

    // []
    panel.add(new JLabel(), c);

    // line spacing
    c.weightx = 0.0;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    JLabel label = createLabel(i18n("LBL_Line_Spacing")); // NOI18N
    panel.add(label, c);

    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(0, SMALL_INSET, TINY_INSET, 0);
    double value = Util.getOption().getLineSpacing();

    if (value < 0) {
      value = 1.0;
    }
    myLineSpacing = new JSpinner(new SpinnerNumberModel(
      value,
      SPACING_MIN,
      SPACING_MAX,
      SPACING_STP
    ));
    int height = myLineSpacing.getPreferredSize().height;
    setHeight(myLineSpacing, Util.round(height * SPACING_FTR));

    label.setLabelFor(myLineSpacing);
    panel.add(myLineSpacing, c);
  }

  private JPanel getZoomPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    ButtonGroup group = new ButtonGroup();
    c.anchor = GridBagConstraints.WEST;
    double zoom = Util.getOption().getZoom();
//out("GET ZOOM: " + zoom);

    // (o) Fit width to
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    JRadioButton buttonWidth = createRadioButton(i18n("LBL_Fit_Width_to")); // NOI18N
    buttonWidth.addItemListener(createItemListener(true, false, false));
    panel.add(buttonWidth, c);
    group.add(buttonWidth);

    // [width]
    c.insets = new Insets(SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    myZoomWidth = new JTextField(getString(Percent.getZoomWidth(zoom, 1)));
    setWidth(myZoomWidth, TEXT_WIDTH);
    panel.add(myZoomWidth, c);

    // page(s)
    c.weightx = 1.0;
    panel.add(createLabel(i18n("LBL_Pages")), c); // NOI18N

    // (o) Zoom to
    c.weightx = 0.0;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    JRadioButton buttonFactor = createRadioButton(i18n("LBL_Zoom_to")); // NOI18N
    buttonFactor.addItemListener(createItemListener(false, false, true));
    panel.add(buttonFactor, c);
    group.add(buttonFactor);

    // [zoom]
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    myZoomFactor = new Percent(
      this,
      Percent.getZoomFactor(zoom, 1.0),
      PERCENTS,
      0,
      new String [] { i18n("LBL_Fit_to_Page") }, // NOI18N
      i18n("TLT_Print_Zoom") // NOI18N
    );
    panel.add(myZoomFactor, c);

    // (o) Fit height to
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    JRadioButton buttonHeight = createRadioButton(i18n("LBL_Fit_Height_to")); // NOI18N
    buttonHeight.addItemListener(createItemListener(false, true, false));
    panel.add(buttonHeight, c);
    group.add(buttonHeight);

    // [height]
    c.insets = new Insets(SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    myZoomHeight = new JTextField(getString(Percent.getZoomHeight(zoom, 1)));
    setWidth(myZoomHeight, TEXT_WIDTH);
    panel.add(myZoomHeight, c);

    // page(s)
    panel.add(createLabel(i18n("LBL_Pages")), c); // NOI18N

    buttonFactor.setSelected(Percent.isZoomFactor(zoom));
    buttonWidth.setSelected(Percent.isZoomWidth(zoom));
    buttonHeight.setSelected(Percent.isZoomHeight(zoom));
//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));

    return panel;
  }

  private ItemListener createItemListener(
    final boolean width,
    final boolean height,
    final boolean factor)
  {
    return new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
        if (myZoomWidth != null) {
          myZoomWidth.setEnabled(width);
        }
        if (myZoomHeight != null) {
          myZoomHeight.setEnabled(height);
        }
        if (myZoomFactor != null) {
          myZoomFactor.setEnabled(factor);
        }
      }
    };
  }

  private String getString(int value) {
    if (value < 0) {
      return Integer.toString(-value);
    }
    return Integer.toString(value);
  }

  private void setWidthFocused(JComponent component, int width) {
    setWidth(component, width);
    component.addFocusListener(this);
  }

  public void focusGained(FocusEvent event) {
//out("FOCUS GAINED");
    setPatternEnabled(true);
  }

  public void focusLost(FocusEvent event) {
//out("FOCUS LOST");
    setPatternEnabled(false);
  }

  private void setPatternEnabled(boolean enabled) {
    for (Pattern pattern : Pattern.values()) {
      pattern.getButton().setEnabled(enabled);
    }
  }

  private void headerFont() {
    Font font = font(myHeaderFontValue);

    if (font != null) {
      myHeaderFontValue = font;
    }
  }

  private void footerFont() {
    Font font = font(myFooterFontValue);

    if (font != null) {
      myFooterFontValue = font;
    }
  }

  private void textFont() {
    Font font = font(myTextFontValue);

    if (font != null) {
      myTextFontValue = font;
    }
  }

  private Font font(Font font) {
    return (Font) new Editor(
      Font.class,
      i18n("LBL_Choose_Font"), // NOI18N
      font).getValue();
  }

  private void borderColor() {
    Color color = color(myBorderColorValue);

    if (color != null) {
      myBorderColorValue = color;
    }
  }

  private void headerColor() {
    Color color = color(myHeaderColorValue);

    if (color != null) {
      myHeaderColorValue = color;
    }
  }

  private void footerColor() {
    Color color = color(myFooterColorValue);

    if (color != null) {
      myFooterColorValue = color;
    }
  }

  private void textColor() {
    Color color = color(myTextColorValue);

    if (color != null) {
      myTextColorValue = color;
    }
  }

  private void backgroundColor() {
    Color color = color(myBackgroundColorValue);

    if (color != null) {
      myBackgroundColorValue = color;
    }
  }

  private Color color(Color color) {
    return (Color) new Editor(
      Color.class,
      i18n("LBL_Choose_Color"), // NOI18N
      color).getValue();
  }

  private void updateControl() {
    myBorder.setSelected(Util.getOption().hasBorder());
    myBorderColor.setEnabled(Util.getOption().hasBorder());

    myHeader.setSelected(Util.getOption().hasHeader());
    myHeaderLeft.setText(Util.getOption().getHeaderLeft());
    myHeaderLeft.setEnabled(Util.getOption().hasHeader());
    myHeaderCenter.setText(Util.getOption().getHeaderCenter());
    myHeaderCenter.setEnabled(Util.getOption().hasHeader());
    myHeaderRight.setText(Util.getOption().getHeaderRight());
    myHeaderRight.setEnabled(Util.getOption().hasHeader());
    myHeaderColor.setEnabled(Util.getOption().hasHeader());
    myHeaderFont.setEnabled(Util.getOption().hasHeader());

    myFooter.setSelected(Util.getOption().hasFooter());
    myFooterLeft.setText(Util.getOption().getFooterLeft());
    myFooterLeft.setEnabled(Util.getOption().hasFooter());
    myFooterCenter.setText(Util.getOption().getFooterCenter());
    myFooterCenter.setEnabled(Util.getOption().hasFooter());
    myFooterRight.setText(Util.getOption().getFooterRight());
    myFooterRight.setEnabled(Util.getOption().hasFooter());
    myFooterColor.setEnabled(Util.getOption().hasFooter());
    myFooterFont.setEnabled(Util.getOption().hasFooter());

    myLineNumbers.setSelected(Util.getOption().isLineNumbers());
    myWrapLines.setSelected(Util.getOption().isWrapLines());
    myUseFont.setSelected(Util.getOption().isUseFont());
    myUseColor.setSelected(Util.getOption().isUseColor());
    myAsEditor.setSelected(Util.getOption().isAsEditor());
  }

  @Override
  protected void opened()
  {
    myHeaderLeft.requestFocus();
  }

  public double getCustomValue(int index) {
    return 0.0;
  }

  public void valueChanged(double value, int index) {}

  private double getDouble(Object value) {
    if ( !(value instanceof Double)) {
      return -1.0;
    }
    return ((Double) value).doubleValue();
  }

  private JCheckBox myHeader;
  private JTextField myHeaderLeft;
  private JTextField myHeaderCenter;
  private JTextField myHeaderRight;
  private JButton myHeaderFont;
  private JButton myHeaderColor;
  private Color myHeaderColorValue;
  private Font myHeaderFontValue;

  private JCheckBox myFooter;
  private JTextField myFooterLeft;
  private JTextField myFooterCenter;
  private JTextField myFooterRight;
  private JButton myFooterFont;
  private JButton myFooterColor;
  private Color myFooterColorValue;
  private Font myFooterFontValue;

  private JCheckBox myBorder;
  private JButton myBorderColor;
  private Color myBorderColorValue;

  private JCheckBox myLineNumbers;
  private JCheckBox myWrapLines;
  private JCheckBox myUseFont;
  private JCheckBox myUseColor;

  private JButton myTextFont;
  private JButton myTextColor;
  private JButton myBackgroundColor;
  private JSpinner myLineSpacing;
  private Font myTextFontValue;
  private Color myTextColorValue;
  private Color myBackgroundColorValue;
  private JCheckBox myAsEditor;

  private Percent myZoomFactor;
  private JTextField myZoomWidth;
  private JTextField myZoomHeight;
  private JTextField mySelectedField;

  private Preview myPreview;
  private DialogDescriptor myDescriptor;

  private static final int TEXT_WIDTH = 30;
  private static final int FIELD_WIDTH = 90;
  private static final int MAX_PAGE_NUBER = 32;
  private static final int MAX_HEADER_SIZE = 100;
  private static final int MAX_FOOTER_SIZE = 100;
  private static final double SPACING_MIN =  0.1;
  private static final double SPACING_MAX = 10.0;
  private static final double SPACING_STP =  0.1;
  private static final double SPACING_FTR = 1.15;
  private static final int [] PERCENTS = new int [] { 25, 50, 75, 100, 125, 150 };
}
