package org.netbeans.modules.apisupport.feedreader;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public class FeedReaderWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private FeedReaderPanelVisual component;
    
    /** Creates a new instance of templateWizardPanel */
    public FeedReaderWizardPanel() {
    }

    
    public Component getComponent() {
        if (component == null) {
            component = new FeedReaderPanelVisual(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( FeedReaderWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid( wizardDescriptor );
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
//        Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
//        if (substitute != null) {
//           wizardDescriptor.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
//        }
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
//        ((WizardDescriptor)d).putProperty ("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public void validate () throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

}
