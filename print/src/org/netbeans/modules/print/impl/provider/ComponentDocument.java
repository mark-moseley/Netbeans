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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.print.impl.provider;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.netbeans.modules.print.impl.util.Option;
import static org.netbeans.modules.print.impl.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.27
 */
final class ComponentDocument extends JComponent {

  ComponentDocument(String text) {
    init();
    prepare(text);
    perform();
  }

  ComponentDocument(AttributedCharacterIterator [] iterators) {
    init();
    prepare(iterators);
    perform();
  }

  private void init() {
//out();
    myWrapLines = Option.getDefault().isWrapLines();
    myLineNumbers = Option.getDefault().isLineNumbers();
    myTextColor = Option.getDefault().getTextColor();
    myTextFont = Option.getDefault().getTextFont();
    myBackgroundColor = Option.getDefault().getBackgroundColor();
    myLineSpacing = Option.getDefault().getLineSpacing();
    myLines = new ArrayList<ComponentLine>();
  }

  private void prepare(String text) {
    LineTokenizer stk = new LineTokenizer(text);

    while (stk.hasMoreTokens()) {
      ComponentLine line = new ComponentLine(
        trimEnded(stk.nextToken()), myTextFont, myTextColor);
//out();
//out(line.getWidth() + " '" + line + "'");
//line.show();
      myLines.add(line);
    }
  }

  private void prepare(AttributedCharacterIterator [] iterators) {
    for(AttributedCharacterIterator iterator : iterators) {
      ComponentLine line =
        new ComponentLine(iterator, myTextFont, myTextColor);
//out();
//out(line.getWidth() + " '" + line + "'");
//line.show();
      myLines.add(line);
    }
  }

  private void perform() {
    removeEmptyLinesAtTheEnd();

    if (myLineNumbers) {
      prepareLineNumbering();
    }
    calculateOffset();

    if (myWrapLines) {
      prepareWrapLines();
    }
    else {
      prepareNoWrapLines();
    }
    calculateMetrics();
  }

  private void removeEmptyLinesAtTheEnd() {
    int i = myLines.size() - 1;

    while (i >= 0) {
      ComponentLine line = myLines.get(i--);

      if (line.isEmpty()) {
        myLines.remove(line);
      }
      else {
        break;
      }
    }
  }

  private String trimEnded(String value) {
    int i = value.length() - 1;

    while (i >= 0 && value.charAt(i) == ' ') {
      i--;
    }
    return value.substring(0, i+1);
  }

  private void prepareLineNumbering() {
    int length = (myLines.size() + "").length(); // NOI18N
    int number = 1;

    for (ComponentLine line : myLines) {
      line.prepend(getNumber(number++, length));
    }
  }

  private void prepareNoWrapLines() {
//out();
    int maxWidth = 0;

    for (ComponentLine line : myLines) {
      int width = line.getWidth();

      if (width > maxWidth) {
        maxWidth = width;
      }
//out("" + maxWidth + " " + width + " '" + line + "'");
    }
    myWidth = maxWidth + myMinOffset;
//out();
//out(" WIDTH: " + myWidth);
  }

  private void prepareWrapLines() {
    myWidth = Option.getDefault().getPageWidth();
//out("Width: " + myWidth);
    List<ComponentLine> lines = new ArrayList<ComponentLine>();

    for (ComponentLine line : myLines) {
//out("  see: " + line.getWidth() + " " + line);
      if (line.getWidth() + myMinOffset <= myWidth) {
        lines.add(line);
      }
      else {
        addWordWrappedLine(lines, line);
      }
    }
    myLines = lines;
  }

  private void addWordWrappedLine(List<ComponentLine> lines, ComponentLine line) {
//out();
//out("add word wrap: '" + line);
    if (line.getWidth() + myMinOffset <= myWidth) {
      lines.add(line);
      return;
    }
    int last = line.length();
    ComponentLine part;
    int k;

    while (true) {
//out("  while: '" + line + "' " + last);
      k = line.lastIndexOf(' ', last - 1);
  
      if (k == -1) {
        addCharWrappedLine(lines, line);
        break;
      }
      last = k;
      part = line.substring(0, k);
      checkOffset(part);

      if (part.getWidth() + myMinOffset <= myWidth) {
//out("   -- '" + part + "' " + k);
        if (part.isEmpty()) {
          addCharWrappedLine(lines, line);
        }
        else {
          lines.add(part);
          part = line.substring(k + 1);
          checkOffset(part);
          addWordWrappedLine(lines, part);
        }
        break;
      }
    }
  }

  private void addCharWrappedLine(List<ComponentLine> lines, ComponentLine line) {
    if (line.getWidth() + myMinOffset <= myWidth) {
      lines.add(line);
      return;
    }
    ComponentLine part;
    int k = line.length();

    while (k >= 0) {
      part = line.substring(0, k);
      checkOffset(part);
  
      if (part.getWidth() + myMinOffset <= myWidth) {
        lines.add(part);
        part = line.substring(k);
        checkOffset(part);
        addCharWrappedLine(lines, part);
        break;
      }
      k--;
    }
  }

  private void checkOffset(ComponentLine line) {
    int offset = -line.getOffset();

    if (offset > myMinOffset) {
      myMinOffset = offset;
    }
  }

  private void calculateOffset() {
    myMinOffset = 0;
        
    for (ComponentLine line : myLines) {
      checkOffset(line);
    }
//out("OFFSET: " + myMinOffset);
  }

  private void calculateMetrics() {
    myHeight = 0;
        
    int size = myLines.size();
    myAscent = new int [size];
    myDescent = new int [size];
    myLeading = new int [size];
    myCorrection = new int [size];

    int pageHeight = Option.getDefault().getPageHeight();
    int breakPosition = pageHeight;
    int prevPos;
//out();

    for (int i=0; i < size; i++) {
      ComponentLine line = myLines.get(i);

      myAscent [i] = (int) Math.round(line.getAscent() * myLineSpacing);
      myDescent [i] = line.getDescent();
      myCorrection [i] = 0;
      prevPos = myHeight;
//out(getAscent(line) + " " + getDescent(line) + " " + getLeading(line));

      myHeight += myAscent [i] + myDescent [i];

      if (myHeight > breakPosition && prevPos < breakPosition) {
        myCorrection [i] = breakPosition - prevPos;
        myHeight += myCorrection [i];
        breakPosition += pageHeight;
      }
      if (i != size - 1) {
        myLeading [i] = line.getLeading();
        myHeight += myLeading [i];
      }
    }
//out("HEIGHT: " + myHeight);
  }

  @Override
  public int getWidth() {
    return myWidth;
  }

  @Override
  public int getHeight() {
    return myHeight;
  }
 
  @Override
  protected void paintComponent(Graphics graphics) {
    Graphics2D g = Option.getDefault().getGraphics(graphics);

    g.setColor(myBackgroundColor);
    g.fillRect(0, 0, myWidth, myHeight);

    int y = 0;

    for (int i=0; i < myLines.size(); i++) {
      ComponentLine line  = myLines.get(i);
      y += myCorrection [i] + myAscent [i];
      line.draw(g, myMinOffset, y);

      y += myDescent [i] + myLeading [i];
    }
  }

  private String getNumber(int number, int length) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(number);

    for (int i=buffer.length(); i < length; i++) {
      buffer.insert(0, " "); // NOI18N
    }
    buffer.append(" "); // NOI18N

    return buffer.toString();
  }

  // ---------------------------------------
  private static final class LineTokenizer {

    public LineTokenizer(String value) {
      myValue = value;
      myLength = value.length();
      myBuffer = new StringBuffer();
    }

    public boolean hasMoreTokens() {
      return myPos < myLength;
    }

    public String nextToken() {
      myBuffer.setLength(0);
      String separator = "";
      char c;

      while (myPos < myLength) {
        c = myValue.charAt(myPos);
        myPos++;

        if (c == '\r' || c == '\n') {
          if (c == '\r' && myPos < myLength && myValue.charAt(myPos) == '\n') {
            myPos++;
          }
          break;
        }       
        myBuffer.append(c);
      }
      return myBuffer.toString();
    }

    private int myPos;
    private int myLength;
    private String myValue;
    private StringBuffer myBuffer;
    // { Unix - "\n", Windows - "\r\n", Mac - "\r" }
  }

  private int myWidth;
  private int myHeight;

  private int [] myAscent;
  private int [] myDescent;
  private int [] myLeading;
  private int [] myCorrection;
  private int myMinOffset;
  private double myLineSpacing;
  
  private boolean myWrapLines;
  private boolean myLineNumbers;

  private Font myTextFont;
  private Color myTextColor;
  private Color myBackgroundColor;

  private List<ComponentLine> myLines;
}
