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

package org.netbeans.modules.vmd.game.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 *
 * @author kherink
 */
public class AnimatedTileCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.AnimatedTile"); // NOI18N
	
	
	public static final String PROPERTY_NAME = "animatedtilecd.prop.name"; // NOI18N
	public static final String PROP_IMAGE_RESOURCE = "animatedtilecd.prop.imageresource"; // NOI18N
	public static final String PROPERTY_INDEX = "animatedtilecd.prop.index"; // NOI18N
	public static final String PROPERTY_WIDTH = "animatedtilecd.prop.width"; // NOI18N
	public static final String PROPERTY_HEIGHT = "animatedtilecd.prop.height"; // NOI18N
	
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, false);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		
		List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
		
		propertyDescriptors.addAll(SequenceContainerCDProperties.getDeclaredPropertyDescriptors());
		
		propertyDescriptors.add(new PropertyDescriptor(PROPERTY_INDEX, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, null));
		propertyDescriptors.add(new PropertyDescriptor(PROPERTY_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING,
				PropertyValue.createNull(), false, false, null));
		propertyDescriptors.add(new PropertyDescriptor(PROP_IMAGE_RESOURCE, ImageResourceCD.TYPEID,
				PropertyValue.createNull(), false, false, null));
		propertyDescriptors.add(new PropertyDescriptor(PROPERTY_WIDTH, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, Versionable.FOREVER));
		propertyDescriptors.add(new PropertyDescriptor(PROPERTY_HEIGHT, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, Versionable.FOREVER));
		
		return Collections.unmodifiableList(propertyDescriptors);
	}
	
	protected List<? extends Presenter> createPresenters() {
		return Arrays.asList(
				);
	}
	
}
