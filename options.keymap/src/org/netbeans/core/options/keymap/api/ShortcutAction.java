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


package org.netbeans.core.options.keymap.api;


/**
 * A special object for representing the action to which the shortcut
 * is bound. As we are able to represent different kinds of actions (
 * (e.g. represented by javax.swing.Action or javax.swing.text.TextAction)
 * the instances of this interface wrap the original action and provide
 * methods contained in this interface.
 * 
 * @author David Strupl
 */
public interface ShortcutAction {

    /**
     * The display name is what the user can see when the shortcut
     * is displayed in the configuration dialogs.
     * @return the display name
     */
    public String getDisplayName ();
    
    /**
     * The ID of the shortcut action. It is the action class name or some
     * other unique identification of the action ("cut-to-clipboard" or
     * "org.openide.actions.CutAction").
     * @return 
     */
    public String getId ();
    
    /**
     * If the same action is supplied by more KeymapManagers they can "know"
     * about each other. If the action "knows" what the ID of the action
     * is coming from the other provider it can supply it by returning a non-null
     * value from this method. An example: actions coming from the editor
     * can supply the class name of the corresponding openide action, e.g.
     * org.openide.actions.CutAction.
     * @return 
     */
    public String getDelegatingActionId ();
    
    /**
     * If the action is "compound" action (delegating to different actions
     * for different keymapManagers) this method returns the instance registered
     * in the given manager. If the action is not composed of more actions
     * this method should simply return <code>this</code>.
     * 
     * @param keymapManagerName 
     * @return 
     */
    public ShortcutAction getKeymapManagerInstance(String keymapManagerName);
}

