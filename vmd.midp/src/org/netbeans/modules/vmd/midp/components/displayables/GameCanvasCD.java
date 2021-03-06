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
 */package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.List;

/**
 * @author David Kaspar
 */

public final class GameCanvasCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.game.GameCanvas"); // NOI18N

//    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/game_canvas_16.png"; // NOI18N

    public static final Integer VALUE_LEFT_PRESSED = (1 << CanvasCD.VALUE_LEFT);
    public static final Integer VALUE_RIGHT_PRESSED = (1 << CanvasCD.VALUE_RIGHT);
    public static final Integer VALUE_UP_PRESSED = (1 << CanvasCD.VALUE_UP);
    public static final Integer VALUE_DOWN_PRESSED = (1 << CanvasCD.VALUE_DOWN);
    public static final Integer VALUE_FIRE_PRESSED = (1 << CanvasCD.VALUE_FIRE);
    public static final Integer VALUE_GAME_A_PRESSED = (1 << CanvasCD.VALUE_GAME_A);
    public static final Integer VALUE_GAME_B_PRESSED = (1 << CanvasCD.VALUE_GAME_B);
    public static final Integer VALUE_GAME_C_PRESSED = (1 << CanvasCD.VALUE_GAME_C);
    public static final Integer VALUE_GAME_D_PRESSED = (1 << CanvasCD.VALUE_GAME_D);


//    static {
//        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
//    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (CanvasCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    protected List<? extends Presenter> createPresenters () {
        return null;
    }

}
