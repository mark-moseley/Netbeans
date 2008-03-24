/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;

/**
 *
 * @author Alexey
 */
public abstract class DesignModeAction extends DesignViewAction {

    private static final long serialVersionUID = 1L;

    public DesignModeAction(DesignView view) {
        super(view);
    }

    @Override
        public boolean isEnabled() {
        DesignView view = getDesignView();
        DiagramModel model = view == null ? null : view.getModel();
        return super.isEnabled() && view != null && model != null &&
                !model.isReadOnly() && 
                view.isDesignMode();
    }
}