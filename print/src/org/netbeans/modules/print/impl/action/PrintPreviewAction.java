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
package org.netbeans.modules.print.impl.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.print.Printable;
import java.util.Date;

import javax.swing.Action;
import javax.swing.JComponent;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.impl.provider.ComponentProvider;
import org.netbeans.modules.print.impl.provider.TextProvider;
import org.netbeans.modules.print.impl.ui.Preview;
import org.netbeans.modules.print.impl.util.Option;
import static org.netbeans.modules.print.impl.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.24
 */
public final class PrintPreviewAction extends IconAction {

  public PrintPreviewAction() {
    this("LBL_Print_Preview_Action", "TLT_Print_Preview_Action", "print"); // NOI18N
  }

  private PrintPreviewAction(String name, String toolTip, String icon) {
    super(
      i18n(PrintPreviewAction.class, name),
      i18n(PrintPreviewAction.class, toolTip),
      icon(Option.class, icon)
    );
    setEnabled(false);
  }

  public void actionPerformed(ActionEvent event) {
    PrintProvider provider = getPrintProvider();

    if (provider != null) {
      Preview.getDefault().print(provider, true);
    }
    else {
      PrintCookie cookie = getPrintCookie();

      if (cookie != null) {
        cookie.print();
      }
    }
  }

  private PrintProvider getPrintProvider() {
//out();
    PrintProvider provider;
    Node node = getActiveNode();

    if (node != null) {
      provider = (PrintProvider) node.getLookup().lookup(PrintProvider.class);

      if (provider != null) {
//out("NODE PROVIDER: " + provider);
        return provider;
      }
    }
    TopComponent top = getActivateTopComponent();

    if (top == null) {
      return null;
    }
//out(" TOP: " + top.getDisplayName() + " " + top.getName() + " " + top.getClass().getName());
    provider = (PrintProvider) top.getLookup().lookup(PrintProvider.class);

    if (provider != null) {
//out("TOP PROVIDER: " + provider);
      return provider;
    }
    DataObject data = (DataObject) top.getLookup().lookup(DataObject.class);
//out("DATA: " + data);

    if (data != null) {
      provider = (PrintProvider) data.getLookup().lookup(PrintProvider.class);

      if (provider != null) {
//out("DATA PROVIDER: " + provider);
        return provider;
      }
    }
    provider = getComponentProvider(top, data);

    if (provider != null) {
//out("COMPONENT PROVIDER: " + provider);
      return provider;
    }
    provider = getEditorProvider(top, data);

    if (provider != null) {
//out("EDITOR PROVIDER: " + provider);
      return provider;
    }
    return null;
  }

  private PrintProvider getComponentProvider(TopComponent top, DataObject data) {
    JComponent component = getComponent(top, ""); // NOI18N

    if (component == null) {
      return null;
    }
    Object object = component.getClientProperty(Printable.class);
    String name = null;

    if (object instanceof String && !object.equals("")) { // NOI18N
      name = (String) object;
    }
    else {
      if (data != null) {
        name = data.getName();
      }
      if (name == null) {
        name = top.getDisplayName();
      }
    }
    object = component.getClientProperty(Date.class);
    Date date;

    if (object instanceof Date) {
      date = (Date) object;
    }
    else {
      if (data == null) {
        date = new Date(System.currentTimeMillis());
      }
      else {
        date = getDate(data);
      }
    }
    return new ComponentProvider(component, name, date);
  }

  private Date getDate(DataObject data) {
    return data.getPrimaryFile().lastModified();
  }

  private PrintProvider getEditorProvider(TopComponent top, DataObject data) {
    if (data == null) {
      return null;
    }
    EditorCookie editor = (EditorCookie) data.getCookie(EditorCookie.class);

    if (editor == null) {
      return null;
    }
    if (editor.getDocument() == null) {
      return null;
    }
    return new TextProvider(editor, getDate(data));
  }

  private JComponent getComponent(Container container, String indent) {
    if (
      container.isShowing() &&
      container instanceof JComponent &&
      ((JComponent) container).getClientProperty(Printable.class) != null)
    {
      return (JComponent) container;
    }
    Component[] components = container.getComponents();

    for (Component component : components) {
      if (component instanceof Container) {
        JComponent jcomponent =
          getComponent((Container) component, "    " + indent); // NOI18N

        if (jcomponent != null) {
          return jcomponent;
        }
      }
    }
    return null;
  }

  private PrintCookie getPrintCookie() {
    Node node = getActiveNode();

    if (node == null) {
      return null;
    }
    return (PrintCookie) node.getCookie(PrintCookie.class);
  }

  @Override
  public boolean isEnabled()
  {
    if (super.isEnabled()) {
      return true;
    }
//out("IS ENABLED: " + (getPrintProvider() != null || getPrintCookie() != null));
    return getPrintProvider() != null || getPrintCookie() != null;
  }

  public static final Action DEFAULT;

  static {
    DEFAULT = new PrintPreviewAction(null,"TLT_Print_Preview_Action","print"); // NOI18N
    DEFAULT.setEnabled(true);
  }
}
