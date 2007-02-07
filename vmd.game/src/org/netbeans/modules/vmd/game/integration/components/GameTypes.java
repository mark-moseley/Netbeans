/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.vmd.game.integration.components;

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;

import java.awt.*;

/**
 * @author David Kaspar, Karel Herink
 */
public class GameTypes {

	public static final TypeID TYPEID_DIMENSION = new TypeID(TypeID.Kind.PRIMITIVE, "#Dimension"); // NOI18N
	public static final TypeID TYPEID_POINT = new TypeID(TypeID.Kind.PRIMITIVE, "#Point"); // NOI18N
	public static final TypeID TYPEID_RECTANGLE = new TypeID(TypeID.Kind.PRIMITIVE, "#Rectangle"); // NOI18N

	public static final TypeID TYPEID_TILED_LAYER_TILES = new TypeID(TypeID.Kind.PRIMITIVE, GamePrimitiveDescriptor.TYPEID_STRING_TILES);
	public static final TypeID TYPEID_SEQUENCE_FRAMES = new TypeID(TypeID.Kind.PRIMITIVE, GamePrimitiveDescriptor.TYPEID_STRING_FRAMES);

	public static PropertyValue createTilesProperty(int[][] tiles) {
		return PropertyValue.createValue(GamePrimitiveDescriptor.PRIMITIVE_DESCRIPTOR_TILES, TYPEID_TILED_LAYER_TILES, tiles);
	}

	public static int[][] getTiles(PropertyValue value) {
		return (int[][]) value.getPrimitiveValue();
	}

	public static PropertyValue createFramesProperty(int[] frames) {
		return PropertyValue.createValue(GamePrimitiveDescriptor.PRIMITIVE_DESCRIPTOR_FRAMES, TYPEID_SEQUENCE_FRAMES, frames);
	}
	
	public static PropertyValue createPointProperty(Point point) {
		return PropertyValue.createValue(GamePrimitiveDescriptor.PRIMITIVE_DESCRIPTOR_POINT, TYPEID_POINT, point);
	}

	public static int[] getFrames(PropertyValue value) {
		return (int[]) value.getPrimitiveValue();
	}

    public static Point getPoint (PropertyValue value) {
        return new Point ((Point) value.getPrimitiveValue ());
    }

    public static PropertyValue createPointValue (Point point) {
        return PropertyValue.createValue (GamePrimitiveDescriptor.PRIMITIVE_DESCRIPTOR_POINT, TYPEID_POINT, new Point (point));
    }

}
