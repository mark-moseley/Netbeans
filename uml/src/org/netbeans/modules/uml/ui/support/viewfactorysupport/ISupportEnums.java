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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

/**
 * @author sumitabhk
 *
 */
public interface ISupportEnums
{
	//enum BoxKind
	public static int BK_SIMPLE_BOX	= 0;
	public static int BK_3DBOX	= BK_SIMPLE_BOX + 1;
	public static int BK_ELONGATED_3DBOX	= BK_3DBOX + 1;
	public static int BK_DIAMOND	= BK_ELONGATED_3DBOX + 1;
	public static int BK_SIMPLE_FILLED_BOX	= BK_DIAMOND + 1;
	public static int BK_NO_BORDER	= BK_SIMPLE_FILLED_BOX + 1;

// NL Use TSLabelKind instead
	//enum TSLabelKind
//	public static int TSLK_UNKNOWN	= -1;
//	public static int TSLK_INTERFACE	= TSLK_UNKNOWN + 1;
//	public static int TSLK_ASSOCIATION_NAME	= TSLK_INTERFACE + 1;
//	public static int TSLK_ASSOCIATION_END0_ROLE_NAME	= TSLK_ASSOCIATION_NAME + 1;
//	public static int TSLK_ASSOCIATION_END0_MULTIPLICITY	= TSLK_ASSOCIATION_END0_ROLE_NAME + 1;
//	public static int TSLK_ASSOCIATION_END1_ROLE_NAME	= TSLK_ASSOCIATION_END0_MULTIPLICITY + 1;
//	public static int TSLK_ASSOCIATION_END1_MULTIPLICITY	= TSLK_ASSOCIATION_END1_ROLE_NAME + 1;
//	public static int TSLK_STEREOTYPE	= TSLK_ASSOCIATION_END1_MULTIPLICITY + 1;
//	public static int TSLK_MESSAGE_OPERATION_NAME	= TSLK_STEREOTYPE + 1;
//	public static int TSLK_INTERACTION_CONSTRAINT	= TSLK_MESSAGE_OPERATION_NAME + 1;
//	public static int TSLK_GUARD_CONDITION	= TSLK_INTERACTION_CONSTRAINT + 1;
//	public static int TSLK_ICON_LABEL	= TSLK_GUARD_CONDITION + 1;
//	public static int TSLK_NAME	= TSLK_ICON_LABEL + 1;
//	public static int TSLK_DERIVATION_BINDING	= TSLK_NAME + 1;
//	public static int TSLK_MESSAGECONNECTOR_OPERATION_NAME	= TSLK_DERIVATION_BINDING + 1;
//	public static int TSLK_MESSAGE_NUMBER	= TSLK_MESSAGECONNECTOR_OPERATION_NAME + 1;
//	public static int TSLK_PRE_CONDITION	= TSLK_MESSAGE_NUMBER + 1;
//	public static int TSLK_POST_CONDITION	= TSLK_PRE_CONDITION + 1;

	//enum RobustnessKind
	public static int RK_BOUNDARY	= 0;
	public static int RK_CONTROL	= RK_BOUNDARY + 1;
	public static int RK_ENTITY	= RK_CONTROL + 1;
	public static int RK_NONE	= RK_ENTITY + 1;

	//enum EllipseKind
	public static int EK_UNKNOWN	= -1;
	public static int EK_CIRCLE_INSIDE_CIRCLE_CENTER_FILLED	= EK_UNKNOWN + 1;
	public static int EK_CIRCLE_WITH_X	= EK_CIRCLE_INSIDE_CIRCLE_CENTER_FILLED + 1;
	public static int EK_CIRCLE_INSIDE_FILLED	= EK_CIRCLE_WITH_X + 1;
	public static int EK_NONE	= EK_CIRCLE_INSIDE_FILLED + 1;

	//enum QuadrantKind, see QuadrantKindEnum

	//enum ValidQuadrantsKind
	public static int VQK_TOPBOTTOM	= 0;
	public static int VQK_LEFTRIGHT	= VQK_TOPBOTTOM + 1;
	public static int VQK_TOPBOTTOM_AND_LEFTRIGHT	= VQK_LEFTRIGHT + 1;

	//enum DisplayFormatKind
	public static int DFK_UML	= 0;
	public static int DFK_CPP	= DFK_UML + 1;

	//enum TextFieldKind
	public static int TFK_UNDEFINED	= -1;
	public static int TFK_VISIBILITY	= 1;
	public static int TFK_NAME	= TFK_VISIBILITY + 1;
	public static int TFK_TYPE	= TFK_NAME + 1;
	public static int TFK_MULTIPLICITY	= TFK_TYPE + 1;
	public static int TFK_ORDERING	= TFK_MULTIPLICITY + 1;
	public static int TFK_INITIALVALUE	= TFK_ORDERING + 1;
	public static int TFK_PROPERTY	= TFK_INITIALVALUE + 1;
	public static int TFK_PARAMLIST	= TFK_PROPERTY + 1;
	public static int TFK_RETURNTYPE	= TFK_PARAMLIST + 1;
	public static int TFK_SEPARATOR	= TFK_RETURNTYPE + 1;
	public static int TFK_DIRECTION	= TFK_SEPARATOR + 1;
	public static int TFK_DEFAULTVALUE	= TFK_DIRECTION + 1;
	public static int TFK_INTERACTION_OPERATOR	= TFK_DEFAULTVALUE + 1;
	public static int TFK_INTERACTION_CONSTRAINT	= TFK_INTERACTION_OPERATOR + 1;
	public static int TFK_EXPRESSION	= TFK_INTERACTION_CONSTRAINT + 1;

}


