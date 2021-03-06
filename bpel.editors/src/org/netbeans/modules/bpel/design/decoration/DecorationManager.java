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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.design.decoration;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.components.DecorationComponent;
import org.netbeans.modules.bpel.design.decoration.components.ZoomableDecorationComponent;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xam.Model;

public class DecorationManager {

    private final Object cacheKey = new Object();
    public static final Decoration emptyDecoration = new Decoration();
    private ArrayList<DecorationProvider> providersList = new ArrayList<DecorationProvider>();
    private DesignView designView;
    
    private DecorationUpdater decorationUpdater = new DecorationUpdater();
    
    public DecorationManager(DesignView designView) {
        this.designView = designView;
    }

    /**
     * Calculates the superset of states from all providers
     **/
    public Decoration getDecoration(Pattern p) {
        BpelEntity entity = p.getOMReference();

        Decoration result = null;

        if (entity != null) {
            result = (Decoration) entity.getCookie(cacheKey);
        }

        return (result != null) ? result : emptyDecoration;
    }

    public List<DecorationProvider> getProviders() {
        return providersList;
    }

    public void attachProvider(DecorationProvider provider) {
        providersList.add(provider);
    /*
     * trigger re-calculation of all decorations to add
     * of decorations reported by  new provider
     */
    }

    /**
     * Notify all providers, that view is about to close.
     * Providers can override this method to release resouces, unsubscribe from listeners, etc
     **/
    public void release() {
        for (DecorationProvider p : providersList) {
            p.release();
        }
    }

    /**
     * listenenr method to be called by providers when they need to inform us about
     * changes in element decorations.
     * @entity - entity to update decorations or null, to update decorations for all entitites
     **/
    public void decorationChanged() {
        decorationUpdater.start();
    }

    
    
    

    public void repositionComponentsRecursive() {
        repositionComponentsRecursive(designView.getBPELModel().getProcess());
    }

    private void updateResult(BpelEntity entity) {
        Decoration newDecoration = new Decoration();

        for (DecorationProvider provider : providersList) {
            Decoration d = provider.getDecoration(entity);
            if (d != null && d != emptyDecoration) {
                newDecoration.combineWith(d);
            }
        }

        Decoration oldDecoration = (Decoration) entity.getCookie(cacheKey);

        entity.setCookie(cacheKey, newDecoration);



    }

    private void updateResultRecursive(BpelEntity entity) {
        updateResult(entity);
        for (BpelEntity e : entity.getChildren()) {
            updateResultRecursive(e);
        }
    }

    private void repositionComponentsRecursive(BpelEntity entity) {
        repositionComponents(entity);
        for (BpelEntity e : entity.getChildren()) {
            repositionComponentsRecursive(e);
        }
    }

    private void repositionComponents(BpelEntity entity) {
        Decoration decoration = (Decoration) entity.getCookie(cacheKey);

        if ((decoration == null) || !decoration.hasComponents()) {
            return;
        }

        Pattern pattern = designView.getModel().getPattern(entity);

        if (pattern == null) {
            return;
        }

        double zoom = designView.getCorrectedZoom();

        //group components by positioneer
        HashMap<Positioner, List<Component>> positionerComponents =
                new HashMap<Positioner, List<Component>>();

        ComponentsDescriptor components = decoration.getComponents();
        int componentsCount = components.getComponentCount();

        for (int i = 0; i < componentsCount; i++) {
            Component c = components.getComponent(i);
            Positioner p = components.getPositioner(i);

            List<Component> list = positionerComponents.get(p);

            if (list == null) {
                list = new ArrayList<Component>(componentsCount);
                positionerComponents.put(p, list);
            }

            list.add(c);

            if (c instanceof ZoomableDecorationComponent) {
                //apply zoom
                ((ZoomableDecorationComponent) c).setZoom(zoom);
            }
        }

        //call positioners for each group
        for (Positioner p : positionerComponents.keySet()) {
            p.position(pattern, positionerComponents.get(p), zoom);
        }

    }
    private class DecorationUpdater implements Runnable {
        private boolean activated = false;
        public void start(){
            activated = true;
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            if (!activated) {
                return;
            }
            
            if (designView.getModel() == null) {
                return;
            }
            if (designView.getBPELModel().getState() != Model.State.VALID) {
                return;
            }

            //re-query all providers to get updated decorations
            updateResultRecursive(designView.getBPELModel().getProcess());

            //add/remove decoration components to awt container
            new ComponentDecorationsUpdater(designView).update();

            //reposition all components
            repositionComponentsRecursive();

            //repaint view
            designView.revalidate();
            designView.repaint();
            designView.getRightStripe().repaint();
            activated = false;
        }
    };
}
