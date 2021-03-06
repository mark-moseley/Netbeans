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
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;

/**
 * @author David Kaspar
 */
public class DisplayableAccept {
    
    static class DisplayableCommandsAcceptPresenter extends AcceptPresenter {
        
        public DisplayableCommandsAcceptPresenter() {
            super(Kind.COMPONENT_PRODUCER);
        }
        
        @Override
        public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
            if (getComponent().getComponentDescriptor().getPropertyDescriptor(DisplayableCD.PROP_COMMANDS).isReadOnly())
                return false;
            DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
            return registry.isInHierarchy(CommandCD.TYPEID, producer.getMainComponentTypeID ());
        }
        
        @Override
        public final ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
            DesignComponent displayable = getComponent();
            DesignDocument document = displayable.getDocument();
            
            DesignComponent command = producer.createComponent(document).getMainComponent();
            MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).addComponent(command);
            
            DesignComponent source = document.createComponent(CommandEventSourceCD.TYPEID);
            MidpDocumentSupport.addEventSource(displayable, DisplayableCD.PROP_COMMANDS, source);
            
            source.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
            source.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(command));
            
            return new ComponentProducer.Result (source);
        }
        
    }
    
    //    static class DisplayableCommandsEventHandlerAcceptPresenter extends AcceptPresenter {
    //
    //        public boolean isAcceptable (ComponentProducer producer) {
    //            if (getComponent ().getComponentDescriptor ().getPropertyDescriptor (DisplayableCD.PROP_COMMANDS).isReadOnly ())
    //                return false;
    //            DescriptorRegistry registry = getComponent ().getDocument ().getDescriptorRegistry ();
    //            return registry.isInHierarchy (EventHandlerCD.TYPEID, producer.getMainComponentTypeID ());
    //        }
    //
    //        public final void accept (ComponentProducer producer) {
    //            DesignComponent displayable = getComponent ();
    //            DesignDocument document = displayable.getDocument ();
    //
    //            DesignComponent handler = producer.createComponent (document).getMainComponent ();
    //            if (handler == null)
    //                return;
    //
    //            DesignComponent command = createBackCommand (document);
    //            MidpDocumentSupport.getCategoryComponent (document, CommandsCategoryCD.TYPEID).addComponent (command);
    //
    //            DesignComponent source = document.createComponent (CommandEventSourceCD.TYPEID);
    //            MidpDocumentSupport.addEventSource (displayable, DisplayableCD.PROP_COMMANDS, source);
    //
    //            source.writeProperty (CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference (displayable));
    //            source.writeProperty (CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference (command));
    //
    //            MidpDocumentSupport.updateEventHandlerWithNew (source, handler);
    //        }
    //
    //        private DesignComponent createBackCommand (DesignDocument document) {
    //            List<ComponentProducer> producers = document.getDescriptorRegistry ().getComponentProducers ();
    //            for (ComponentProducer producer : producers) {
    //                if (CommandProducer.PRODUCER_ID_BACK_COMMAND.equals (producer.getProducerID ()))
    //                    return producer.createComponent (document).getMainComponent ();
    //            }
    //            return null;
    //        }
    //
    //    }
    
}
