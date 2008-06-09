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
package org.netbeans.modules.bpel.project.ui.catalog;

import javax.swing.Action;
import java.awt.Image;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.06.09
 */
final class ResourceNode extends AbstractNode{
    
  ResourceNode(CatalogEntry system) {
    super(Children.LEAF);
    mySystem = system;
  }
  
  @Override
  public Image getIcon(int type) {
    return IMAGE;
  }
  
  @Override
  public boolean canRename() {
    return false;
  }
  
  @Override
  public String getDisplayName() {
    return getFileName(mySystem.getSource());
  }
  
  private String getFileName(String file) {
    file = file.replaceAll("%20", " ");

    if (file.startsWith("file:")) { // NOI18N
      file = file.substring(5);
    }
    if (file.startsWith("/") && Utilities.isWindows()) {
      file = file.substring(1);
    }
    return file.replace("\\", "/"); // NOI18N
  }

  @Override
  public String getName() {
    return getDisplayName();
  }

  @Override
  public Action[] getActions(boolean context) {
    return new Action [] {
      null, // todo a
//          SystemAction.get(DeleteAction.class),
//          SystemAction.get(RenameAction.class),
    };
  }

  private CatalogEntry mySystem;
  private static final Image IMAGE = Utilities.loadImage("org/netbeans/modules/bpel/project/ui/resources/resource.gif"); // NOI18N
}
