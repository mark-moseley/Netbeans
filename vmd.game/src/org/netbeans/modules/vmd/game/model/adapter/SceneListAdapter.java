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
package org.netbeans.modules.vmd.game.model.adapter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;

public class SceneListAdapter implements ListModel, SceneListener {

	private Scene scene;
	private ArrayList listeners = new ArrayList();
	
	public SceneListAdapter(Scene layerModel) {
		this.scene = layerModel;
		this.scene.addSceneListener(this);
	}
	
	public int getSize() {
		return this.scene.getLayerCount();
	}

	public Object getElementAt(int index) {
		return this.scene.getLayerAt(index);
	}

	public void addListDataListener(ListDataListener l) {
		this.listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		this.listeners.remove(l);
	}


	public void layerAdded(Scene sourceScene, Layer layer, int index) {
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.INTERVAL_ADDED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.intervalAdded(lde);
		}
	}

	public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info, int index) {
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.INTERVAL_REMOVED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.intervalRemoved(lde);
		}
	}

	public void layerModified(Scene sourceScene, Layer layer) {
		int index = sourceScene.indexOf(layer);
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.CONTENTS_CHANGED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.contentsChanged(lde);
		}
	}

	public void layerMoved(Scene sourceScene, Layer layer, int indexOld, int indexNew) {
		this.layerModified(sourceScene, layer);
	}

	public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
		this.layerModified(sourceScene, layer);
	}

	public void layerPositionChanged(Scene sourceScene, Layer layer, Point oldPosition, Point newPosition, boolean inTransition) {
		this.layerModified(sourceScene, layer);
	}

	public void layerVisibilityChanged(Scene sourceScene, Layer layer, boolean visible) {
		this.layerModified(sourceScene, layer);
	}

}
