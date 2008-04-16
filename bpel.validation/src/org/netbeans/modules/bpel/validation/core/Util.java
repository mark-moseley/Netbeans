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
package org.netbeans.modules.bpel.validation.core;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.model.api.NamedElement;
import static org.netbeans.modules.soa.core.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.12.07
 */
public final class Util {

  public static List<NamedElement> getAppropriate(NamedElement [] elements, String typed) {
//out();
    List<NamedElement> appropriate = new ArrayList<NamedElement>();

    if (elements.length == 0) {
      return appropriate;
    }
    Chars [] elementsChars = new Chars [elements.length];

    for (int i=0; i < elements.length; i++) {
      elementsChars [i] = new Chars(elements [i]);
    }
    Chars typedChars = new Chars(typed);

    for (Chars elementChars : elementsChars) {
      typedChars.calculateDistance(elementChars);
//out(elementChars);
    }
    int minDistance = elementsChars [0].getDistance();

    for (int i=1; i < elementsChars.length; i++) {
      int distance = elementsChars [i].getDistance();

      if (distance < minDistance) {
        minDistance = distance;
      }
    }
    for (int i=0; i < elementsChars.length; i++) {
      if (elementsChars [i].getDistance() == minDistance) {
        appropriate.add(elementsChars [i].getElement());
      }
    }
    return appropriate;
  }

  // -------------------------
  private static class Chars {

    public Chars(NamedElement element) {
      this(element, null);
    }

    public Chars(String value) {
      this(null, value);
    }

    private Chars(NamedElement element, String value) {
      myElement = element;

      if (element == null) {
        myValue = value;
      }
      else {
        myValue = element.getName();
      }
      myValue = myValue.toLowerCase();
      myChars = new byte [0xFFFF];

      for (int i=0; i < myValue.length(); i++) {
        myChars [myValue.charAt(i)]++;
      }
    }

    public NamedElement getElement() {
      return myElement;
    }

    private void setDistance(int distance) {
      myDistance = distance;
    }

    public void calculateDistance(Chars chars) {
      int distance = 0;

      for (int i=0; i < myChars.length; i++) {
        distance += Math.abs(myChars [i] - chars.getChars() [i]);
      }
      chars.setDistance(distance);
    }

    public int getDistance() {
      return myDistance;
    }

    private byte [] getChars() {
      return myChars;
    }

    @Override
    public String toString() {
      if (myString == null) {
        StringBuffer buffer = new StringBuffer();

        for (int i=0; i < myChars.length; i++) {
          if (myChars [i] == 0) {
            continue;
          }
          buffer.append(((char) i) + "(" + myChars [i] + ")");
        }
        myString = myValue + ": " +  buffer.toString();
      }
      return myDistance + ": " + myString;
    }

    private int myDistance;
    private String myValue;
    private String myString;
    private byte [] myChars;
    private NamedElement myElement;
  }
}
