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
package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;


import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.openide.util.Utilities;

/**
 * 
 * @author jqian
 */
public class CasaConnectionWidget extends ConnectionWidget {

    private static final Stroke STROKE_DEFAULT = new BasicStroke(1.0f);
    private static final Stroke STROKE_HOVERED = new BasicStroke(1.5f);
    private static final Stroke STROKE_SELECTED = new BasicStroke(2.0f);
    private static final Image IMAGE_QOS_BADGE_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/QoS.png");    // NOI18N
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    private Widget mQoSWidget;

    public CasaConnectionWidget(Scene scene) {
        super(scene);
        setSourceAnchorShape(AnchorShape.NONE);
        setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        setPaintControlPoints(true);
        setState(ObjectState.createNormal());

        mQoSWidget = new ImageWidget(getScene(), IMAGE_QOS_BADGE_ICON);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ObjectScene objectScene = (ObjectScene) getScene();
                final CasaConnection myCasaConnection =
                        (CasaConnection) objectScene.findObject(CasaConnectionWidget.this);
                //System.out.println("obj for connection is " + myCasaConnection);
                if (myCasaConnection == null) {
                    return; // FIXME
                }

                updateQoSWidget(myCasaConnection);

                myCasaConnection.getModel().addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        CasaConnection casaConnection = null;

                        Object source = evt.getSource();
                        if (source instanceof CasaExtensibilityElement) {
                            CasaComponent parent = (CasaExtensibilityElement) source;
                            while (parent != null &&
                                    parent instanceof CasaExtensibilityElement) {
                                parent = parent.getParent();
                            }

                            if (parent == null || !(parent instanceof CasaConnection)) {
                                return;
                            }

                            casaConnection = (CasaConnection) parent;
                        } else if (source instanceof CasaConnection) {
                            casaConnection = (CasaConnection) source;
                        }

                        if (casaConnection == myCasaConnection) {
                            updateQoSWidget(myCasaConnection);
                        }
                    }
                });
            }
        });
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state) {
        Stroke stroke;
        Color fgColor;

        CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
        if (state.isSelected() || state.isFocused()) {
            bringToFront();
            stroke = STROKE_SELECTED;
            fgColor = customizer.getCOLOR_SELECTION();
        } else if (state.isHovered() || state.isHighlighted()) {
            bringToFront();
            stroke = STROKE_HOVERED;
            fgColor = customizer.getCOLOR_HOVERED_EDGE();
        } else {
            stroke = STROKE_DEFAULT;
            fgColor = customizer.getCOLOR_CONNECTION_NORMAL();
        }

        setStroke(stroke);
        setForeground(fgColor);
    }

    public void setForegroundColor(Color color) {
        setForeground(color);
    }

    @Override
    protected void notifyAdded() {
        super.notifyAdded();

        // Update the error badge location if the widget moves.
        Widget.Dependency errorDependency = new Widget.Dependency() {

            public void revalidateDependency() {
                if (getBounds() == null) {
                    return;
                }

                Anchor sourceAnchor = getSourceAnchor();
                if (sourceAnchor == null) {
                    return;
                }

                Widget sourceWidget = sourceAnchor.getRelatedWidget();
                if (sourceWidget == null) {
                    return;
                }

                Point p = sourceAnchor.getRelatedSceneLocation();
                int x = p.x + sourceWidget.getBounds().width / 2 + 10;
                int y = p.y - 6;

                mQoSWidget.setPreferredLocation(new Point(x, y));
            }
        };

        mDependenciesRegistry.registerDependency(errorDependency);
    }

    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();

        mDependenciesRegistry.removeAllDependencies();

        if (mQoSWidget != null) {
            mQoSWidget.removeFromParent();
        }
    }

    private void updateQoSWidget(CasaConnection casaConnection) {
        if (isConnectionConfiguredWithQoS(casaConnection)) {
            if (!getChildren().contains(mQoSWidget)) {
                addChild(mQoSWidget);
                getScene().validate();
            }
        } else {
            if (getChildren().contains(mQoSWidget)) {
                removeChild(mQoSWidget);
                getScene().validate();
            }
        }
    }

    private boolean isConnectionConfiguredWithQoS(CasaConnection casaConnection) {
        return casaConnection.getChildren().size() != 0;
    }
}
