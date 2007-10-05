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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;

import org.netbeans.modules.print.spi.PrintPage;
import org.netbeans.modules.print.impl.util.Macro;
import org.netbeans.modules.print.impl.util.Util;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
final class Paper extends JComponent {

  Paper(
    PrintPage page,
    int number,
    int count,
    int row,
    int column)
  {
    myPage = page;
    myNumber = number;
    myCount = String.valueOf(count);
    myRow = row;
    myColumn = column;
    myRowNumber = String.valueOf(row + 1);
    myColumnNumber = String.valueOf(column + 1);
  }

  void setInfo(
    String name,
    Date modified,
    double scale)
  {
    myName = name;
    myLastModifiedDate = modified;

    myPaperWidth = Util.getOption().getPaperWidth();
    myPaperHeight = Util.getOption().getPaperHeight();
    myPageX = Util.getOption().getPageX();
    myPageY = Util.getOption().getPageY();
    myPageWidth = Util.getOption().getPageWidth();
    myPageHeight = Util.getOption().getPageHeight();

    myHeaderY = Util.getOption().getHeaderY();
    myHasHeader = Util.getOption().hasHeader();
    myHeaderLeft = expandTitle(Util.getOption().getHeaderLeft());
    myHeaderCenter = expandTitle(Util.getOption().getHeaderCenter());
    myHeaderRight = expandTitle(Util.getOption().getHeaderRight());
    myHeaderColor = Util.getOption().getHeaderColor();
    myHeaderFont = Util.getOption().getHeaderFont();

    myFooterY = Util.getOption().getFooterY();
    myHasFooter = Util.getOption().hasFooter();
    myFooterLeft = expandTitle(Util.getOption().getFooterLeft());
    myFooterCenter = expandTitle(Util.getOption().getFooterCenter());
    myFooterRight = expandTitle(Util.getOption().getFooterRight());
    myFooterColor = Util.getOption().getFooterColor();
    myFooterFont = Util.getOption().getFooterFont();

    myHasBorder = Util.getOption().hasBorder();
    myBorderColor = Util.getOption().getBorderColor();
    myIsPainting = true;

    setScale(scale);
  }

  int getRow() {
    return myRow;
  }

  int getColumn() {
    return myColumn;
  }

  void setScale(double scale) {
    myScale = scale;

    if (myIsPainting) {
      setPreferredSize(new Dimension (
        (int) Math.floor((myPaperWidth + SHADOW_WIDTH) * myScale),
        (int) Math.floor((myPaperHeight + SHADOW_WIDTH) * myScale)
      ));
    }
    else {
      setPreferredSize(new Dimension(myPaperWidth, myPaperHeight));
    }
  }

  int getPaperWidth() {
    return myPaperWidth + SHADOW_WIDTH;
  }

  int getPaperHeight() {
    return myPaperHeight + SHADOW_WIDTH;
  }

  @Override
  public void print(Graphics g)
  {
    myIsPainting = false;
    setScale(1.0);
    super.print(g);
    myIsPainting = true;
  }

  @Override
  public void paint(Graphics graphics)
  {
    Graphics2D g = Util.getGraphics(graphics);

    // scaling
    if (myIsPainting) {
      g.scale(myScale, myScale);
    }

    // background
    g.setColor(Color.white);
    g.fillRect(myPageX, myPageY, myPageWidth, myPageHeight);

    // page
    g.translate(myPageX, myPageY);
    myPage.print(g);
    g.translate(-myPageX, -myPageY);

    // horizontal margin
    g.setColor(Color.white);

    g.fillRect(
      0, 0,
      myPaperWidth, myPageY
    );
    
    g.fillRect(
      0, myPageY + myPageHeight,
      myPaperWidth, myPaperHeight
    );

    // header
    if (myHasHeader) {
      drawTitle(g,
        myHeaderLeft,
        myHeaderCenter,
        myHeaderRight,
        myHeaderY,
        myHeaderColor,
        myHeaderFont
      );
    }

    // footer
    if (myHasFooter) {
      drawTitle(g,
        myFooterLeft,
        myFooterCenter,
        myFooterRight,
        myFooterY,
        myFooterColor,
        myFooterFont
      );
    }

    // vertical margin
    g.setColor(Color.white);

    g.fillRect(
      0, 0,
      myPageX, myPaperHeight
    );
    
    g.fillRect(
      myPageX + myPageWidth, 0,
      myPaperWidth, myPaperHeight
    );
    
    // shadow
    if (myIsPainting) {
      g.setColor(Color.gray.darker());
      g.fillRect(
        myPaperWidth,
        SHADOW_WIDTH,
        SHADOW_WIDTH + 1,
        myPaperHeight
      );
      g.fillRect(
        SHADOW_WIDTH,
        myPaperHeight,
        myPaperWidth,
        SHADOW_WIDTH + 1
      );
      g.setColor(Color.lightGray);
      g.fillRect(myPaperWidth, 0, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
      g.fillRect(0, myPaperHeight, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
    }
    
    // box
    if (myIsPainting) {
      g.setColor(Color.black);
      g.drawRect(0, 0, myPaperWidth, myPaperHeight);
    }

    // border
    if (myHasBorder) {
      g.setColor(myBorderColor);
      g.drawRect(myPageX, myPageY, myPageWidth, myPageHeight);
    }

    // number
    if (myIsPainting) {
      g.setColor(NUMBER_FONT_COLOR);
      g.setFont(NUMBER_FONT_NAME);
      g.drawString(Integer.toString(myNumber), NUMBER_X, NUMBER_Y);
    }
  }

  private void drawTitle(
    Graphics2D g,
    String left,
    String center,
    String right,
    int y,
    Color color,
    Font f)
  {
    g.setColor(color);
    drawTitle(g, left,  myPageX, y, f);
    drawTitle(g, center,myPageX + (myPageWidth - getWidth(center, f))/2, y, f);
    drawTitle(g, right, myPageX + myPageWidth - getWidth(right, f), y, f);
  }

  private void drawTitle(
    Graphics2D g,
    String text,
    int x,
    int y,
    Font font)
  {
    g.setFont(font);
    g.drawString(text, x, y);
  }

  private String expandTitle(String t) {
    Date printed = new Date(System.currentTimeMillis());

    t = Util.replace(t, Macro.NAME.getName(), myName);
    t = Util.replace(t, Macro.ROW.getName(), myRowNumber);
    t = Util.replace(t, Macro.COLUMN.getName(), myColumnNumber);
    t = Util.replace(t, Macro.USER.getName(), USER_NAME);
    t = Util.replace(t, Macro.COUNT.getName(), myCount);
    t = Util.replace(t, Macro.MODIFIED_DATE.getName(),getDate(myLastModifiedDate));
    t = Util.replace(t, Macro.MODIFIED_TIME.getName(),getTime(myLastModifiedDate));
    t = Util.replace(t, Macro.PRINTED_DATE.getName(), getDate(printed));
    t = Util.replace(t, Macro.PRINTED_TIME.getName(), getTime(printed));

    return t;
  }

  private int getWidth(String text, Font font) {
    return (int) Math.ceil(font.getStringBounds(
      text, Util.FONT_RENDER_CONTEXT).getWidth());
  }

  private String getDate(Date timestamp) {
    return getTimestamp(timestamp, "yyyy.MM.dd"); // NOI18N
  }

  private String getTime(Date timestamp) {
    return getTimestamp(timestamp, "HH:mm:ss"); // NOI18N
  }

  private String getTimestamp(Date timestamp, String format) {
    return new SimpleDateFormat(format).format(timestamp);
  }

  private int myNumber;
  private double myScale;
  private PrintPage myPage;
  private boolean myIsPainting;

  private int myPaperWidth;
  private int myPaperHeight;
  private int myPageX;
  private int myPageY;
  private int myPageWidth;
  private int myPageHeight;

  private int myHeaderY;
  private boolean myHasHeader;
  private String myHeaderLeft;
  private String myHeaderCenter;
  private String myHeaderRight;
  private Color myHeaderColor;
  private Font myHeaderFont;

  private int myFooterY;
  private boolean myHasFooter;
  private String myFooterLeft;
  private String myFooterCenter;
  private String myFooterRight;
  private Color myFooterColor;
  private Font myFooterFont;

  private boolean myHasBorder;
  private Color myBorderColor;
  private String myName;
  private String myCount;
  private String myRowNumber;
  private String myColumnNumber;
  private Date myLastModifiedDate;

  private int myRow;
  private int myColumn;

  private static final int NUMBER_FONT_SIZE = 35;
  private static final int SHADOW_WIDTH = 10; // .pt
  private static final int NUMBER_X = (int) Math.round(NUMBER_FONT_SIZE * 1.0);
  private static final int NUMBER_Y = (int) Math.round(NUMBER_FONT_SIZE * 1.5);
  private static final Color NUMBER_FONT_COLOR = new Color(125, 125, 255);
  private static final String USER_NAME = System.getProperty("user.name"); // NOI18N
  private static final Font NUMBER_FONT_NAME =
    new Font("Serif", Font.BOLD, NUMBER_FONT_SIZE); // NOI18N
}
