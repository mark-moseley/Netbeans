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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import java.awt.Point;

import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ICornerLabelCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class LabelOffsetHelper {

	private CombinedFragmentLabelManager m_pManager;
	private Point m_ptEngineCenter;
	private int m_lMinHorizontalOffset;

	// maintains a point where new labels should be created
	IETPoint m_cpOffset; // only access via CalculateLabelOffset()

	public LabelOffsetHelper(CombinedFragmentLabelManager pManager)
   {
		m_pManager = pManager;
		m_ptEngineCenter = new Point(0, 0);
		m_lMinHorizontalOffset = 0;

		// Determine the logical center of the associated draw engine
		IETRect rectLogicalEngine = TypeConversions.getLogicalBoundingRect(getEngine());
		m_ptEngineCenter = rectLogicalEngine.getCenterPoint();

		// Determine the horizontal offset of the labels from the size of the corner label
		ICornerLabelCompartment cpCornerLabel = m_pManager.getCompartmentByKind( getEngine(), ICornerLabelCompartment.class );
		if (cpCornerLabel != null)
      {
			IETRect rectCornerLabel = TypeConversions.getLogicalBoundingRect(cpCornerLabel);

			// Add a little to the horizontal offset,
			// so the first InteractionConstraint does not touch the corner label.
			m_lMinHorizontalOffset = rectCornerLabel.getIntWidth() + 5;
		}
	}

	public void relayoutLabel(IInteractionOperand pOperand, IETLabel pETLabel) {
		IETPoint cpPointOffset = calculateLabelOffset(pOperand, pETLabel);
		pETLabel.setSpecifiedXY(cpPointOffset);
		pETLabel.reposition();
	}

	public void relayoutLabel(ICompartment pCompartment, IETLabel pETLabel) {
		IETPoint cpPointOffset = calculateLabelOffset(pCompartment, pETLabel);
		pETLabel.setSpecifiedXY(cpPointOffset);
		pETLabel.reposition();
	}

	protected IDrawEngine getEngine() {
		IDrawEngine cpEngine = null;

		if (m_pManager != null) {
			// Determine the logical center of the associated draw engine
			cpEngine = m_pManager.getEngine();
		}
		return cpEngine;
	}

	/// Find the location for new interaction constraint labels
	protected IETPoint calculateLabelOffset(IInteractionOperand pOperand, IETLabel pETLabel) {
		ICompartment cpCompartment = this.getEngine().findCompartmentContainingElement(pOperand);

		return calculateLabelOffset(cpCompartment, pETLabel);
	}

	/// Find the location for new interaction constraint labels
	protected IETPoint calculateLabelOffset(ICompartment pCompartment, IETLabel pETLabel) {
		if (m_cpOffset == null) {
			m_cpOffset = PointConversions.newETPoint(new Point (0, 0));
		}

		if (pETLabel != null) {
			IDrawEngine cpEngine = pETLabel.getEngine();
			if (cpEngine != null) {
				// Determine the logical bounding rectangle for the label's compartment
				IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(pCompartment);
				rectCompartment.setLeft(rectCompartment.getLeft() + m_lMinHorizontalOffset);

				// Use the bounding rectangle of the label to determine the half width and height
				// to be used as part of the offset.
				IETRect rectLabel = TypeConversions.getLogicalBoundingRect(cpEngine);

                                // Trey Spiva - Compacting the label a little.
				IETPoint ptLabelCenter = new ETPoint(rectCompartment.getLeft() + (rectLabel.getIntWidth() / 2), rectCompartment.getTop() - 5);

				IETPoint ptOffset = new ETPoint((int)(ptLabelCenter.getX() - m_ptEngineCenter.getX()), (int)(ptLabelCenter.getY() - m_ptEngineCenter.getY()));

				m_cpOffset.setX(ptOffset.getX());
				m_cpOffset.setY(ptOffset.getY());
			}
		}

		return m_cpOffset;
	}

}
