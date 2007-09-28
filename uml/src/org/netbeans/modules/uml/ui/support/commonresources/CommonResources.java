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

package org.netbeans.modules.uml.ui.support.commonresources;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

public class CommonResources
{
	public static void setMnemonic(Object obj, String text)
	{
		if (obj != null && text != null && text.length() > 0)
		{	
			int pos = text.indexOf('&');
			if (pos > -1)
			{
				String under = text.substring(pos + 1, pos + 2);
				if (under != null && under.length() > 0)
				{
					if (obj instanceof JLabel)
					{
						JLabel lab = (JLabel)obj;
						lab.setDisplayedMnemonic(under.charAt(0));
					}
					else if (obj instanceof JCheckBox)
					{
						JCheckBox box = (JCheckBox)obj;
						box.setMnemonic(under.charAt(0));
					}
					else if (obj instanceof JRadioButton)
					{
						JRadioButton button = (JRadioButton)obj;
						button.setMnemonic(under.charAt(0));
					}
					else if (obj instanceof JButton)
					{
						JButton button = (JButton)obj;
						button.setMnemonic(under.charAt(0));
					}
				}
			}
		}
	}
	
	public static String determineText(String text)
	{
		String retStr = text;
		if (text != null && text.length() > 0)
		{	
			retStr = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		return retStr;
	}
	
	public static void setFocusAccelerator(Object obj, String text)
	{
		if (obj != null && text != null && text.length() > 0)
		{	
			int pos = text.indexOf('&');
			if (pos > -1)
			{
				String under = text.substring(pos + 1, pos + 2);
				if (under != null && under.length() > 0)
				{
					if (obj instanceof JComboBox)
					{
						JComboBox combo = (JComboBox)obj;
						((JTextField)combo.getEditor().getEditorComponent()).setFocusAccelerator(under.charAt(0));
					}
				}
			}
		}
	}
	
}
