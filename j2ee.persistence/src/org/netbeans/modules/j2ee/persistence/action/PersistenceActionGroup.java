package org.netbeans.modules.j2ee.persistence.action;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Action group holding Java Persistence related actions.
 * It is visible only if persistence is provided by owning project 
 * (if there is at least one persistence unit defined in persistence.xml)
 *
 * @author Martin Adamek
 */
public final class PersistenceActionGroup extends NodeAction {
    
    protected void performAction(Node[] activatedNodes) {
        // this is just action group, this should not be called
    }
    
    public String getName() {
        return NbBundle.getMessage(PersistenceActionGroup.class, "CTL_PersistenceActionGroup");
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(UseEntityManagerAction.class),
        };
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        return hasPersistenceContext(activatedNodes[0]);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        Node[] activatedNodes = getActivatedNodes();
        boolean oneNodeSelected = activatedNodes.length == 1;
        if (oneNodeSelected && hasPersistenceContext(activatedNodes[0])) {
            return new LazyMenu();
        }
        JMenuItem i = super.getPopupPresenter();
        i.setVisible(false);
        return i;
    }

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(PersistenceActionGroup.this.getName());
        }
        
        @Override
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                Action[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    Action action = grouped[i];
                    if (action == null && getItemCount() != 0) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(Utilities.actionsGlobalContext());
                        }
                        if (action instanceof Presenter.Popup) {
                            add(((Presenter.Popup)action).getPopupPresenter());
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
    
    private static boolean hasPersistenceContext(Node node) {
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        if (dataObject != null) {
            return PersistenceScope.getPersistenceScope(dataObject.getPrimaryFile()) != null;
        }
        return false;
    }
    
}

