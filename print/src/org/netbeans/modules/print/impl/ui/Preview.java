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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.netbeans.modules.print.spi.PrintPage;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.impl.util.Option;
import org.netbeans.modules.print.impl.util.Percent;
import static org.netbeans.modules.print.impl.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
public final class Preview extends Dialog implements Percent.Listener {

  public static Preview getDefault() {
    return DEFAULT;
  }

  private Preview() {
    myPrinter = new Printer();
    myKeyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent event) {
        char ch = event.getKeyChar();

        if (ch == '+' || ch == '=') {
          myScale.increaseValue();
        }
        else if (ch == '-' || ch == '_') {
          myScale.decreaseValue();
        }
        else if (ch == '/') {
          myScale.normalValue();
        }
        else if (ch == '*') {
          showCustom(true);
        }
      }
    };
  }

  public void print(List<PrintProvider> providers, boolean withPreview) {
    assert providers != null : "Print providers can't be null"; // NOI18N
    assert providers.size() > 0 : "Must be at least one provider"; // NOI18N
//out();
//out("Do action");
    myPrintProviders = providers;

    if (withPreview) {
      show();
    }
    else {
      print(true);
    }
  }

  private JPanel createPanel() {
//out("Create Main panel");
    JPanel p = new JPanel(new GridBagLayout());
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // navigate
    c.anchor = GridBagConstraints.WEST;
    p.add(createNavigatePanel(), c);

    // scale
    c.weightx = 1.0;
    c.weighty = 0.0;
    p.add(createScalePanel(), c);

    // toggle
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, TINY_INSET, MEDIUM_INSET);
    myToggle = createToggleButton(
      new ButtonAction(icon(Option.class, "toggle"), i18n("TLT_Toggle")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          toggle();
        }
      }
    );
    myToggle.addKeyListener(myKeyListener);
    p.add(myToggle, c);

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(MEDIUM_INSET, 0, MEDIUM_INSET, 0);
    panel.add(p, c);

    // scroll
    c.gridy++;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createScrollPanel(), c);

    toggle();

    return panel;
  }

  @Override
  protected void updated()
  {
//out("Update content");
    createPapers();
    toggle();
  }

  private void toggle() {
    updatePaperNumber();
    addPapers();
    updateButtons();
  }

  private JComponent createNavigatePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // first
    panel = new JPanel(new GridBagLayout());
    c.insets = new Insets(TINY_INSET, TINY_INSET, TINY_INSET, TINY_INSET);
    myFirst = createButton(
      new ButtonAction(icon(Option.class, "first"), i18n("TLT_First")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          first();
        }
      }
    );
    myFirst.addKeyListener(myKeyListener);
    panel.add(myFirst, c);

    // previous
    myPrevious = createButton(
      new ButtonAction(
        icon(Option.class, "previous"), // NOI18N
        i18n("TLT_Previous")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          previous();
        }
      }
    );
    myPrevious.addKeyListener(myKeyListener);
    panel.add(myPrevious, c);

    // text field
    myGoto = new JTextField();
    int width = (int)Math.round(myPrevious.getPreferredSize().width/PREVIEW_FACTOR);
    int height = myPrevious.getPreferredSize().height;
    myGoto.setPreferredSize(new Dimension(width, height));
    myGoto.setMinimumSize(new Dimension(width, height));

    InputMap inputMap = myGoto.getInputMap();
    ActionMap actionMap = myGoto.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke('+'), INCREASE);
    inputMap.put(KeyStroke.getKeyStroke('='), INCREASE);
    inputMap.put(KeyStroke.getKeyStroke('-'), DECREASE);
    inputMap.put(KeyStroke.getKeyStroke('_'), DECREASE);
    
    actionMap.put(INCREASE, new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        next();
      }
    });
    actionMap.put(DECREASE, new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        previous();
      }
    });
    myGoto.setHorizontalAlignment(JTextField.CENTER);
    myGoto.setToolTipText(i18n("TLT_Goto")); // NOI18N
    myGoto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        goTo();
      }
    });
    panel.add(myGoto, c);
    
    // next
    myNext = createButton(
      new ButtonAction(icon(Option.class, "next"), i18n("TLT_Next")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          next();
        }
      }
    );
    myNext.addKeyListener(myKeyListener);
    panel.add(myNext, c);

    // last
    myLast = createButton(
      new ButtonAction(icon(Option.class, "last"), i18n("TLT_Last")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          last();
        }
      }
    );
    myLast.addKeyListener(myKeyListener);
    panel.add(myLast, c);

    return panel;
  }

  private JComponent createScalePanel() {
//out("Create scale panel");
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // fit to window
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, TINY_INSET, TINY_INSET);
    myFit = createButton(
      new ButtonAction(icon(Option.class, "fit"), i18n("TLT_Fit")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          showCustom(true);
        }
      }
    );
    myFit.addKeyListener(myKeyListener);
    panel.add(myFit, c);

    // scale
    c.insets = new Insets(TINY_INSET, TINY_INSET, TINY_INSET, TINY_INSET);
    myScale = new Percent(
      this,
      Option.getDefault().getScale(),
      PERCENTS,
      CUSTOMS.length - 1,
      CUSTOMS,
      i18n("TLT_Preview_Scale") // NOI18N
    );
    int width = myScale.getPreferredSize().width;
    int height = myPrevious.getPreferredSize().height;
    myScale.setPreferredSize(new Dimension(width, height));
    myScale.setMinimumSize(new Dimension(width, height));
    panel.add(myScale, c);
    
    // decrease
    myDecrease = createButton(
      new ButtonAction(icon(Option.class, "minus"), i18n("TLT_Zoom_Out")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myScale.decreaseValue();
        }
      }
    );
    myDecrease.addKeyListener(myKeyListener);
    panel.add(myDecrease, c);

    // increase
    myIncrease = createButton(
      new ButtonAction(icon(Option.class, "plus"), i18n("TLT_Zoom_In")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myScale.increaseValue();
        }
      }
    );
    myIncrease.addKeyListener(myKeyListener);
    panel.add(myIncrease, c);

    return panel;
  }

  private JComponent createScrollPanel() {
//out("Create scroll panel");
    GridBagConstraints c = new GridBagConstraints();

    // papers
    myPaperPanel = new JPanel(new GridBagLayout());
    myPaperPanel.setBackground(Color.lightGray);
    JPanel panel = new JPanel(new GridBagLayout());

    c.gridy = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    panel.setBackground(Color.lightGray);
    panel.add(myPaperPanel, c);

//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.yellow));
//  optionPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));
//  myPaperPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));

    // scroll
    c.fill = GridBagConstraints.BOTH;
    myScrollPane = new MyScrollPane(panel);
    myScrollPane.setFocusable(true);

    myScrollPane.addWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent event) {
        if (SwingUtilities.isRightMouseButton(event) || event.isControlDown()) {
          myScrollPane.setWheelScrollingEnabled(false);

          if (event.getWheelRotation() > 0) {
            myScale.increaseValue();
          }
          else {
            myScale.decreaseValue();
          }
        }
        else {
          myScrollPane.setWheelScrollingEnabled(true);
        }
      }
    });
    myScrollPane.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          if (SwingUtilities.isRightMouseButton(event)) {
            myScale.customValue(CUSTOMS.length - 1);
            myCustomIndex = 0;
          }
          else {
            showCustom(true);
          }
        }
      }
    });
    myScrollPane.addKeyListener(myKeyListener);

    return myScrollPane;
  }

  private void showCustom(boolean doNext) {
    if (doNext) {
      myCustomIndex++;

      if (myCustomIndex == CUSTOMS.length) {
        myCustomIndex = 0;
      }
    }
    myScale.customValue(myCustomIndex);
  }

  private void updateButtons() {
    myGoto.setText(getPaper(myPaperNumber));
    myFirst.setEnabled(myPaperNumber > 1);
    myPrevious.setEnabled(myPaperNumber > 1);
    myNext.setEnabled(myPaperNumber < getPaperCount());
    myLast.setEnabled(myPaperNumber < getPaperCount());
    boolean enabled = getPaperCount() > 0;
    myGoto.setEnabled(enabled);
    myScale.setEnabled(enabled);
    myToggle.setEnabled(enabled);
    myFit.setEnabled(enabled);
    myIncrease.setEnabled(enabled);
    myDecrease.setEnabled(enabled);
    myPrintButton.setEnabled(enabled);
  }

  private void scrollTo() {
//out("Scroll to: " + myPaperNumber);
    Paper paper = myPapers [myPaperNumber - 1];
    int gap  = getGap();
    int x = paper.getX() - gap;
    int y = paper.getY() - gap;
    int w = paper.getWidth();
    int h = paper.getHeight();
    JViewport view = myScrollPane.getViewport();

    if ( !view.getViewRect().contains(x, y, w, h)) {
      view.setViewPosition(new Point(x,y));
      updatePaperPanel();
    }
  }

  public double getCustomValue(int index) {
    if (getPaperCount() == 0) {
      return 0.0;
    }
    int width = myPapers [0].getPaperWidth() + GAP_SIZE;
    int height = myPapers [0].getPaperHeight() + GAP_SIZE;

    if (index == 0) {
      return getWidthScale(width);
    }
    if (index == 1) {
      return getHeightScale(height);
    }
    if (index == 2) {
      return getAllScale(width, height);
    }
    return 1.0;
  }

  private double getWidthScale(int width) {
    final int JAVA_INSET = 5;
    
    double scrollWidth = (double) (myScrollPane.getWidth() -
      myScrollPane.getVerticalScrollBar().getWidth() - JAVA_INSET);

    return scrollWidth / width;
  }

  private double getHeightScale(int height) {
    final int JAVA_INSET = 5;
    
    double scrollHeight = (double) (myScrollPane.getHeight() -
      myScrollPane.getHorizontalScrollBar().getHeight() - JAVA_INSET);

    return scrollHeight / height;
  }

  private double getAllScale(int width, int height) {
    int w = width;
    int h = height;

    if ( !isSingleMode()) {
      int maxRow = 0;
      int maxColumn = 0;

      for (Paper paper : myPapers) {
        maxRow = Math.max(maxRow, paper.getRow());
        maxColumn = Math.max(maxColumn, paper.getColumn());
      }
      w *= maxColumn + 1;
      h *= maxRow + 1;
    }
    return Math.min(getWidthScale(w), getHeightScale(h));
  }

  public void valueChanged(double value, int index) {
//out();
//out("Set scale: " + value + " " + index);
    if (index != -1) {
      myCustomIndex = index;
    }
    if (getPaperCount() == 0) {
      return;
    }
    for (Paper paper : myPapers) {
      paper.setScale(value);
    }
    addPapers();
  }

  private void addPapers() {
//out("Add papers");
    myPaperPanel.removeAll();

    if (getPaperCount() == 0) {
      updatePaperPanel();
      return;
    }
    int gap = getGap();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(gap, gap, 0, 0);

    if (isSingleMode()) {
      myPaperPanel.add(myPapers [myPaperNumber - 1], c);
    }
    else {
      for (Paper paper : myPapers) {
        c.gridx = paper.getColumn();
        c.gridy = paper.getRow();
        myPaperPanel.add(paper, c);
      }
    }
    updatePaperPanel();
  }

  private void updatePaperPanel() {
    myPaperPanel.revalidate();
    myPaperPanel.repaint();
  }

  private void createPapers() { // todo start here
    PrintProvider myPrintProvider = myPrintProviders.get(0);
    PrintPage [][] pages = myPrintProvider.getPages(
      Option.getDefault().getPageWidth(),
      Option.getDefault().getPageHeight(),
      Option.getDefault().getZoom());
//out("Create papers: " + pages.length);
    myPapers = null;

    if (pages == null) {
      return;
    }
    String name = myPrintProvider.getName();
    
    if (name == null) {
      name = ""; // NOI18N
    }
    Date modified = myPrintProvider.getLastModifiedDate();

    if (modified == null) {
      modified = new Date(System.currentTimeMillis());
    }
    double scale = 1.0;

    if (myScale != null) {
      scale = myScale.getValue();
    }
    int number = 0;
    int count = getCount(pages);
    myPapers = new Paper [count];

    for (int i=0; i < pages.length; i++) {
      for (int j=0; j < pages [i].length; j++) {
        PrintPage page = pages [i][j];

        if (page == null) {
          continue;
        }
        myPapers [number] = new Paper(
          page,
          name,
          modified,
          scale
        );
        myPapers [number].setInfo(
          number + 1,
          i, j,
          count
        );
        number++;
      }
    }
  }

  private int getCount(PrintPage [][] pages) {
    int count = 0;

    for (int i=0; i < pages.length; i++) {
      for (int j=0; j < pages [i].length; j++) {
        PrintPage page = pages [i][j];

        if (page != null) {
          count++;
        }
      }
    }
    return count;
  }

  public void invalidValue(String value) {}

  private int getPaperCount() {
    if (myPapers == null) {
      return 0;
    }
    return myPapers.length;
  }

  private void first() {
    updatePaperNumber();
    changePaper();
  }

  private void previous() {
    if (myPaperNumber == 1) {
      return;
    }
    myPaperNumber--;
    changePaper();
  }

  private void next() {
    if (myPaperNumber == getPaperCount()) {
      return;
    }
    myPaperNumber++;
    changePaper();
  }

  private void last() {
    myPaperNumber = getPaperCount();
    changePaper();
  }

  private void goTo() {
    String value = myGoto.getText();
    int number = getPaperNumber(value);
    int count = getPaperCount();

    if (number < 1 || number > count) {
      myGoto.setText(getPaper(myPaperNumber));
    }
    else {
      myPaperNumber = number;
      changePaper();
    }
    myGoto.selectAll();
  }

  private void changePaper() {
    if (isSingleMode()) {
      addPapers();
    }
    else {
      scrollTo();
    }
    updateButtons();
  }

  private void updatePaperNumber() {
    myPaperNumber = getPaperCount() == 0 ? 0 : 1;
  }

  private int getGap() {
    return (int) Math.round(GAP_SIZE * myScale.getValue());
  }

  private String getPaper(int value) {
    return Option.getPageOfCount(String.valueOf(value), String.valueOf(getPaperCount()));
  }
      
  @Override
  protected DialogDescriptor createDescriptor()
  {
    Object [] rightButtons = getRightButtons();
    Object [] leftButtons = getLeftButtons();

    DialogDescriptor descriptor = new DialogDescriptor(
      getResizable(createPanel()),
      i18n("LBL_Print_Preview"), // NOI18N
      true,
      rightButtons,
      myPrintButton,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      null
    );
    descriptor.setClosingOptions(new Object [] { myPrintButton, myCloseButton });
    descriptor.setAdditionalOptions(leftButtons);

    return descriptor;
  }

  @Override
  protected void opened()
  {
//out("Opened");
    myScrollPane.requestFocus();
  }

  @Override
  protected void resized()
  {
    if (myScale.isCustomValue()) {
      showCustom(false);
    }
  }

  private Object [] getLeftButtons() {
    JButton pageSetup = createButton(
      new ButtonAction(
        i18n("LBL_Page_Setup_Button"), // NOI18N
        i18n("TLT_Page_Setup_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          if (Option.getDefault().showPageSetup()) {
            updated();
          }
        }
      }
    );
    JButton printOption = createButton(
      new ButtonAction(
        i18n("LBL_Print_Option_Button"), // NOI18N
        i18n("TLT_Print_Option_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          option();
        }
      }
    );
    printOption.addKeyListener(myKeyListener);

    return new Object [] {
      pageSetup,
      printOption,
    };
  }

  private Object [] getRightButtons() {
    myPrintButton = createButton(
      new ButtonAction(
        i18n("LBL_Print_Button"), // NOI18N
        i18n("TLT_Print_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          print(false);
        }
      }
    );
    myPrintButton.addKeyListener(myKeyListener);

    myCloseButton = createButton(
      new ButtonAction(
        i18n("LBL_Close_Button"), // NOI18N
        i18n("TLT_Close_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          close();
        }
      }
    );
    myCloseButton.addKeyListener(myKeyListener);

    return new Object [] {
      myPrintButton,
      myCloseButton,
    };
  }

  private void close() {
//out("Closed");
    myPapers = null;
    myPrintProviders = null;
    Option.getDefault().setScale(myScale.getValue());
  }

  private boolean isSingleMode() {
    return myToggle.isSelected();
  }

  private void print(boolean doUpdate) {
    if (doUpdate) {
      createPapers();
    }
    myPrinter.print(myPapers);
  }

  private void option() {
    new Attribute(this).show();
  }

  private int getPaperNumber(String text) {
    String value = text.trim();
    StringBuffer buffer = new StringBuffer();

    for (int i=0; i < value.length(); i++) {
      char c = value.charAt(i);

      if ( !isAplha(c)) {
        break;
      }
      buffer.append(c);
    }
    return getInt(buffer.toString());
  }

  private boolean isAplha(char c) {
    return "0123456789".indexOf(c) != -1; // NOI18N
  }

  // ----------------------------------------------------------
  private static final class MyScrollPane extends JScrollPane {
    MyScrollPane(JPanel panel) {
      super(
        panel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
      );
      getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

      int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
      int height = (int) Math.round(screenHeight * PREVIEW_FACTOR);
      int width = (int) Math.round(height * PREVIEW_FACTOR);

      Dimension dimension = new Dimension(width, height);
      setMinimumSize(dimension);
      setPreferredSize(dimension);
    }

    public void addMouseWheelListener(MouseWheelListener listener) {
      if (myMouseWheelListeners == null) {
        myMouseWheelListeners = new ArrayList<MouseWheelListener>();
      }
//out("Listener: " + listener.getClass().getName());
      myMouseWheelListeners.add(listener);
    }

    public void addWheelListener(MouseWheelListener wheelListener) {
      super.addMouseWheelListener(wheelListener);

      for (int i=0; i < myMouseWheelListeners.size(); i++) {
        super.addMouseWheelListener(myMouseWheelListeners.get(i));
      }
    }

    private List<MouseWheelListener> myMouseWheelListeners;
  }

  private Paper [] myPapers;
  private JPanel myPaperPanel;
  
  private JButton myFirst;
  private JButton myPrevious;
  private JButton myNext;
  private JButton myLast;

  private JButton myFit;
  private JButton myIncrease;
  private JButton myDecrease;

  private JButton myPrintButton;
  private JButton myCloseButton;

  private Percent myScale;
  private JTextField myGoto;
  private int myPaperNumber;
  private int myCustomIndex;
  private JToggleButton myToggle;
  private MyScrollPane myScrollPane;
  private KeyListener myKeyListener;

  private Printer myPrinter;
  private List<PrintProvider> myPrintProviders;

  private static final int GAP_SIZE = 20;
  private static final int SCROLL_INCREMENT = 40;
  private static final double PREVIEW_FACTOR = 0.75;
  private static final int [] PERCENTS = new int [] { 25, 50, 75, 100, 200, 400 };

  private static final String INCREASE = "increase"; // NOI18N
  private static final String DECREASE = "decrease"; // NOI18N
  
  private final String [] CUSTOMS = new String [] {
    i18n("LBL_Fit_to_Width"), // NOI18N
    i18n("LBL_Fit_to_Height"), // NOI18N
    i18n("LBL_Fit_to_All"), // NOI18N
  };
  private static final Preview DEFAULT = new Preview();
}
