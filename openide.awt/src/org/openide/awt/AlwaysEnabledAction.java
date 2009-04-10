package org.openide.awt;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/** Lazily initialized always enabled action
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class AlwaysEnabledAction extends AbstractAction
implements PropertyChangeListener, ContextAwareAction {

    // -J-Dorg.openide.awt.AlwaysEnabledAction.level=FINE
    private static final Logger LOG = Logger.getLogger(AlwaysEnabledAction.class.getName());

    private final Map map;
    private ActionListener delegate;
    private final Lookup context;
    private final Object equals;

    public AlwaysEnabledAction(Map m) {
        super();
        this.map = m;
        this.context = null;
        this.equals = this;
    }

    private AlwaysEnabledAction(Map m, ActionListener delegate, Lookup context, Object equals) {
        super();
        this.map = m;
        this.delegate = bindToContext(delegate, context);
        this.context = context;
        this.equals = equals;
    }

    private static ActionListener bindToContext(ActionListener a, Lookup context) {
        if (context != null) {
            if (a instanceof ContextAwareAction) {
                return ((ContextAwareAction)a).createContextAwareInstance(context);
            }
        }
        return a;
    }

    private ActionListener getDelegate() {
        if (delegate == null) {
            Object listener = map.get("delegate"); // NOI18N
            if (!(listener instanceof ActionListener)) {
                throw new NullPointerException();
            }
            delegate = bindToContext((ActionListener)listener, context);
            if (delegate instanceof Action) {
                Action actionDelegate = (Action) delegate;
                actionDelegate.addPropertyChangeListener(this);
                // Ensure display names and other properties are in sync or propagate them
                syncActionDelegateProperty(Action.NAME, actionDelegate);
            }
        }
        return delegate;
    }

    private void syncActionDelegateProperty(String propertyName, Action actionDelegate) {
        Object value = extractCommonAttribute(map, this, propertyName);
        Object delegateValue = actionDelegate.getValue(propertyName);
        if (value != null) {
            if (delegateValue == null) {
                actionDelegate.putValue(propertyName, value);
            } else {
                if (!delegateValue.equals(value)) { // Values differ
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.warning("Value of property \"" + propertyName +
                                "\" of AlwaysEnabledAction is \"" +
                                value + "\" but delegate has \"" + delegateValue +
                                "\"\ndelegate:" + delegate + '\n');
                    }
                }
            }
        } // else either both values are null or
        // this has null and delegate has non-null which is probably fine (declarer does not care)
    }

    @Override
    public boolean isEnabled() {
//        assert EventQueue.isDispatchThread();
        if (delegate instanceof Action) {
            return ((Action)delegate).isEnabled();
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        assert EventQueue.isDispatchThread();
        if (getDelegate() instanceof Action) {
            if (!((Action)getDelegate()).isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                firePropertyChange(null, null, null);
                return;
            }
        }

        getDelegate().actionPerformed(e);
    }

    @Override
    public Object getValue(String name) {
        if (delegate instanceof Action) {
            Object ret = ((Action)delegate).getValue(name);
            if (ret != null) {
                return ret;
            }
        }

        return extractCommonAttribute(map, this, name);
    }

    static final Object extractCommonAttribute(Map fo, Action action, String name) {
        if (Action.NAME.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            //return Actions.cutAmpersand(actionName);
            return actionName;
        }
        if (Action.MNEMONIC_KEY.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            int position = Mnemonics.findMnemonicAmpersand(actionName);

            return position == -1 ? null : Character.valueOf(actionName.charAt(position + 1));
        }
        if (Action.SMALL_ICON.equals(name)) {
            Object icon = fo == null ? null : fo.get("iconBase"); // NOI18N
            if (icon instanceof Icon) {
                return (Icon) icon;
            }
            if (icon instanceof URL) {
                icon = Toolkit.getDefaultToolkit().getImage((URL)icon);
            }
            if (icon instanceof Image) {
                return ImageUtilities.image2Icon((Image)icon);
            }
            if (icon instanceof String) {
                return ImageUtilities.loadImageIcon((String)icon, false);
            }
        }
        if ("iconBase".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("iconBase"); // NOI18N
        }
        if ("noIconInMenu".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("noIconInMenu"); // NOI18N
        }
        if (Action.ACCELERATOR_KEY.equals(name)) {
            Keymap map = Lookup.getDefault().lookup(Keymap.class);
            if (map != null) {
                KeyStroke[] arr = map.getKeyStrokesForAction(action);
                return arr.length > 0 ? arr[0] : null;
            }
        }
        // Delegate query to other properties to "fo" ignoring special properties
        if (!"delegate".equals(name) && !"instanceCreate".equals(name)) {
            return fo.get(name);
        }

        return null;
    }


    @Override
    public int hashCode() {
        if (equals == this) {
            return super.hashCode();
        }
        return equals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AlwaysEnabledAction) {
            final AlwaysEnabledAction other = (AlwaysEnabledAction) obj;
            if (this == this.equals && other == other.equals) {
                return (this == other);
            }

            if (this.equals.equals(other.equals)) {
                return true;
            }
        }
        return false;
    }



    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == delegate) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new AlwaysEnabledAction(map, delegate, actionContext, equals);
    }
}
