/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.support;

/**
 * @author sumitabhk
 *
 *
 */
public interface SimpleQuestionDialogKind
{
	public static int SQDK_ABORTRETRYIGNORE	= 0;
	public static int SQDK_OK	= SQDK_ABORTRETRYIGNORE + 1;
	public static int SQDK_OKCANCEL	= SQDK_OK + 1;
	public static int SQDK_RETRYCANCEL	= SQDK_OKCANCEL + 1;
	public static int SQDK_YESNO	= SQDK_RETRYCANCEL + 1;
	public static int SQDK_YESNOCANCEL	= SQDK_YESNO + 1;
	public static int SQDK_YESNOALWAYS	= SQDK_YESNOCANCEL + 1;
	public static int SQDK_YESNONEVER	= SQDK_YESNOALWAYS + 1;
}

