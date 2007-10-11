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

package org.netbeans.modules.uml.project.ui.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.project.ui.cookies.ImportedElementCookie;
import org.netbeans.modules.uml.project.ui.nodes.actions.RemoveFromImport;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author Trey Spiva
 */
public class ImportedElementNode extends FilterNode implements Comparable, ImportedElementCookie
{

    private IProject referencingProject;
    private IElement elementImport;
    private static RemoveFromImport remove = new RemoveFromImport();

    public ImportedElementNode(IProject project, Node orig,
            IElement elementImport)
    {
        super(orig);
        this.referencingProject = project;
        this.elementImport = elementImport;
        disableDelegation(DELEGATE_DESTROY);
    }

    public <T extends Node.Cookie> T getCookie(Class<T> type)
    {
        if (type.isInstance(this))
        {
            return type.cast(this);
        }
        return super.getCookie(type);
    }

    public javax.swing.Action[] getActions(boolean context)
    {
        ArrayList<Action> ac = new ArrayList<Action>();
        ac.add(remove);
        Action[] actions = super.getActions(context);

        List<Action> list = Arrays.asList(actions);
        ac.addAll(1, list);
        Action[] a = new Action[list.size()];
        return ac.toArray(a);
    }

    public boolean canDestroy()
    {
        return true;
    }

    public void destroy() throws IOException
    {
        destroy(true);
    }

    public void destroy(boolean fromOriginal) throws IOException
    {
        if (fromOriginal == true)
        {
            this.getOriginal().destroy();
        }
        super.destroy(); // calls Node.destroy(), not orig.destroy()
    }

    public void removeImportedElement()
    {
        IProjectTreeItem item = getOriginal().getCookie(IProjectTreeItem.class);
        if (item != null)
        {
            UMLXMLManip.removeChild(referencingProject.getNode(), elementImport);
            referencingProject.setDirty(true);
        }
        try
        {
            destroy(false);
        } catch (IOException e)
        {
        }
    }

    public int compareTo(Object o)
    {
        if (!(o instanceof ImportedElementNode))
        {
            return -1;
        }
        IProjectTreeItem item1 = getOriginal().getCookie(IProjectTreeItem.class);
        IProjectTreeItem item2 = ((ImportedElementNode) o).getOriginal().getCookie(IProjectTreeItem.class);
        if (item1 != null && item2 != null)
        {
            return ProjectTreeComparable.compareTo(item1, item2);
        }
        return -1;
    }

    public IProject getReferencingProject()
    {
        return referencingProject;
    }

    public String getElementXMIID()
    {
        IProjectTreeItem item = getOriginal().getCookie(IProjectTreeItem.class);
        return (item != null) ? item.getModelElementXMIID() : "";
    }

}