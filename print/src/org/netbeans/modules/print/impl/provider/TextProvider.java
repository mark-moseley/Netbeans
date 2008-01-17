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
package org.netbeans.modules.print.impl.provider;

import java.awt.Color;
import java.awt.Font;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.openide.cookies.EditorCookie;
import org.openide.text.AttributedCharacters;
import org.netbeans.editor.BaseDocument;

import org.netbeans.modules.print.impl.util.Option;
import static org.netbeans.modules.print.impl.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.04
 */
public final class TextProvider extends ComponentProvider {

  public TextProvider(EditorCookie editor, Date modified) {
    super(null, getName(editor), modified);
    myEditor = editor;
  }

  @Override
  protected JComponent getComponent()
  {
    if (Option.getDefault().isAsEditor()) {
      JEditorPane[] panes = myEditor.getOpenedPanes();

      if (panes != null && panes.length != 0) {
        return panes[0];
      }
      return null;
    }
    Document document = myEditor.getDocument();
      
    if (document == null) {
      return null;
    }
    if (document instanceof BaseDocument && document.getLength() < MAX_ATTR_TEXT_SIZE) {
//out();
//out("GET ITERATOR");
//out();
    PrintContainer container = new PrintContainer();
      ((BaseDocument) document).print(container, false, true, 0, document.getLength());
      return new ComponentDocument(container.getIterators());
    }
    return new ComponentDocument(getText(document));
  }

  private String getText(Document document) {
    try {
      return document.getText(0, document.getLength());
    }
    catch (BadLocationException e) {
      return ""; // NOI18N
    }
  }

  private static String getName(EditorCookie editor) {
    Document document = editor.getDocument();
      
    if (document == null) {
      return null;
    }
    return ((String) document.getProperty(
      Document.TitleProperty)).replace('\\', '/'); // NOI18N
  }

  // --------------------------------------
  private static final class PrintContainer
    implements org.netbeans.editor.PrintContainer
  {
    PrintContainer() {
      myCharacters = new AttributedCharacters();
      myCharactersList = new ArrayList<AttributedCharacters>();
    }

    public void add(char [] chars, Font font, Color foreColor, Color backColor) {
//out(getString(foreColor) + " " + getString(backColor) + " " + getString(font) + " " + new String(chars));
      myCharacters.append(chars, font, foreColor);
    }

    public void eol() {
//out();
      myCharactersList.add(myCharacters);
      myCharacters = new AttributedCharacters();
    }

    public boolean initEmptyLines() {
      return false;
    }
  
    AttributedCharacterIterator [] getIterators() {
      AttributedCharacterIterator [] iterators =
        new AttributedCharacterIterator [myCharactersList.size()];

      for (int i=0; i < myCharactersList.size(); i++) {
        iterators [i] = myCharactersList.get(i).iterator();
      }
      return iterators;
    }

    private AttributedCharacters myCharacters;
    private List<AttributedCharacters> myCharactersList;
  }

  private EditorCookie myEditor;
  private static final int MAX_ATTR_TEXT_SIZE = 64000;
}
