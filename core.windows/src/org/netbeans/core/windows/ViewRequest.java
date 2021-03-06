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


package org.netbeans.core.windows;

import org.netbeans.core.windows.view.View;

/**
 * Class which describes one type of change (in model) which is sent
 * <code>ViewRequestor</code> from <code>Central</code>.
 *
 * @author  Peter Zavadsky
 */
final class ViewRequest {

    /** To distinguish between individual mode or top components. */
    public final Object source;

    public final int type;

    public final Object oldValue;

    public final Object newValue;


    /** Creates a new instance of ChangeInfo */
    public ViewRequest(Object source, int type, Object oldValue, Object newValue) {
        this.source   = source;
        this.type     = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append ("ViewRequest@");
        result.append (System.identityHashCode(this));
        result.append (" [TYPE=");
        String tp;
        switch (type) {
            case View.CHANGE_ACTIVE_MODE_CHANGED :
                tp = "CHANGE_ACTIVE_MODE_CHANGED"; //NOI18N
                break;
            case View.CHANGE_EDITOR_AREA_BOUNDS_CHANGED :
                tp = "CHANGE_EDITOR_AREA_BOUNDS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED :
                tp = "CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_STATE_CHANGED :
                tp = "CHANGE_EDITOR_AREA_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED :
                tp = "CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAXIMIZED_MODE_CHANGED :
                tp = "CHANGE_MAXIMIZED_MODE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED :
                tp = "CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_BOUNDS_CHANGED :
                tp = "CHANGE_MODE_BOUNDS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_CONSTRAINTS_CHANGED :
                tp = "CHANGE_MODE_CONSTRAINTS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_FRAME_STATE_CHANGED :
                tp = "CHANGE_MODE_FRAME_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOOLBAR_CONFIGURATION_CHANGED :
                tp = "CHANGE_TOOLBAR_CONFIGURATION_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_ICON_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_ICON_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_ACTIVATED :
                tp = "CHANGE_TOPCOMPONENT_ACTIVATED"; //NOI18N
                break;
            case  View.CHANGE_DND_PERFORMED :
                tp = "CHANGE_DND_PERFORMED"; //NOI18N
                break;
            case  View.CHANGE_UI_UPDATE :
                tp = "CHANGE_UI_UPDATE"; //NOI18N
                break;
            case  View.TOPCOMPONENT_REQUEST_ATTENTION :
                tp = "TOPCOMPONENT_REQUEST_ATTENTION"; //NOI18N
                break;
            case  View.TOPCOMPONENT_CANCEL_REQUEST_ATTENTION :
                tp = "TOPCOMPONENT_CANCEL_REQUEST_ATTENTION"; //NOI18N
                break;
            default :
                tp = "UNKNOWN";
                break;
        }
        result.append (tp).append ("]  [oldValue:").append(oldValue)
                .append("] [newValue:").append(newValue)
                .append("] [source:").append(source).append(']');
        return result.toString();
    }
        
}

