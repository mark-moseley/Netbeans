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
 *
 */

package org.netbeans.modules.vmd.midpnb.codegen;

import com.sun.source.tree.CompilationUnitTree;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.vmd.api.codegen.CodeGlobalLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCode;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.commands.*;
import org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGSplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.openide.util.Exceptions;

import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class MidpCustomCodePresenterSupport {

    public static final String PARAM_DISPLAY = "display"; // NOI18N
    public static final String PARAM_TIMEOUT = "timeout"; // NOI18N
    public static final String PARAM_SVG_TIMEOUT = "timeout"; // NOI18N
    public static final String PARAM_SVG_MENU_ELEMENT = "menuElement"; // NOI18N

    private static final Parameter PARAMETER_DISPLAY = new DisplayParameter ();
    private static final Parameter PARAMETER_TIMEOUT = new TimeoutParameter ();
    private static final Parameter PARAMETER_SVG_TIMEOUT = new SVGTimeoutParameter ();
    private static final Parameter PARAMETER_WAITSCREEN_COMMAND = new WaitScreenCommandParameter ();
    private static final Parameter PARAMETER_SPLASHSCREEN_COMMAND = new SplashScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_WAITSCREEN_COMMAND = new SVGWaitScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_SPLASHSCREEN_COMMAND = new SVGSplashScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_MENU_ELEMENT = new SVGMenuElementParameter ();

    private MidpCustomCodePresenterSupport () {
    }

    public static Parameter createDisplayParameter () {
        return PARAMETER_DISPLAY;
    }

    public static Parameter createTimeoutParameter () {
        return PARAMETER_TIMEOUT;
    }

    public static Parameter createSVGTimeoutParameter () {
        return PARAMETER_SVG_TIMEOUT;
    }

    public static Parameter createWaitScreenCommandParameter () {
        return PARAMETER_WAITSCREEN_COMMAND;
    }

    public static Parameter createSplashScreenCommandParameter () {
        return PARAMETER_SPLASHSCREEN_COMMAND;
    }

    public static Parameter createSVGWaitScreenCommandParameter () {
        return PARAMETER_SVG_WAITSCREEN_COMMAND;
    }

    public static Parameter createSVGSplashScreenCommandParameter () {
        return PARAMETER_SVG_SPLASHSCREEN_COMMAND;
    }

    public static Parameter createSVGMenuElementParameter () {
        return PARAMETER_SVG_MENU_ELEMENT;
    }

    public static Presenter createAddImportPresenter () {
        return new CodeGlobalLevelPresenter() {
            protected void performGlobalGeneration (StyledDocument styledDocument) {
                try {
                    JavaSource.forDocument (styledDocument).runModificationTask (new CancellableTask<WorkingCopy>() {
                        public void cancel () {
                        }

                        public void run (WorkingCopy parameter) throws Exception {
                            String fqn = getComponent ().getType ().getString ();
                            parameter.toPhase (JavaSource.Phase.PARSED);
                            CompilationUnitTree oldTree = parameter.getCompilationUnit ();
                            CompilationUnitTree newTree = SourceUtils.addImports (oldTree, Arrays.asList (fqn), parameter.getTreeMaker ());
                            parameter.rewrite (oldTree, newTree);
                        }
                    }).commit ();
                } catch (IOException e) {
                    Exceptions.printStackTrace (e);
                }
            }
        };

    }

    private static class DisplayParameter implements Parameter {

        public String getParameterName () {
            return PARAM_DISPLAY;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            section.getWriter ().write ("getDisplay ()"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return false;
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return false;
        }

    }

    private static class TimeoutParameter extends MidpParameter {

        protected TimeoutParameter () {
            super (PARAM_TIMEOUT);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue value = component.readProperty (SplashScreenCD.PROP_TIMEOUT);
            if (value.getKind () == PropertyValue.Kind.VALUE)
                if (MidpTypes.getInteger (value) == 0) {
                    section.getWriter ().write ("SplashScreen.TIMEOUT"); // NOI18N
                    return;
                }
            super.generateParameterCode (component, section, index);
        }
    }

    private static class SVGTimeoutParameter extends MidpParameter {

        protected SVGTimeoutParameter () {
            super (PARAM_SVG_TIMEOUT);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue value = component.readProperty (SVGSplashScreenCD.PROP_TIMEOUT);
            if (value.getKind () == PropertyValue.Kind.VALUE)
                if (MidpTypes.getInteger (value) == 0) {
                    section.getWriter ().write ("SVGSplashScreen.TIMEOUT"); // NOI18N
                    return;
                }
            super.generateParameterCode (component, section, index);
        }
    }

    private static final class WaitScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (WaitScreenSuccessCommandCD.TYPEID, command.getType ()))
                return false;
            if (command != null && descriptorRegistry.isInHierarchy (WaitScreenFailureCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SplashScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SplashScreenDismissCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGWaitScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SVGWaitScreenSuccessCommandCD.TYPEID, command.getType ()))
                return false;
            if (command != null && descriptorRegistry.isInHierarchy (SVGWaitScreenFailureCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGSplashScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SVGSplashScreenDismissCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGMenuElementParameter implements Parameter {

        public String getParameterName () {
            return PARAM_SVG_MENU_ELEMENT;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            List<PropertyValue> elements = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            for (PropertyValue value : elements) {
                DesignComponent element = value.getComponent ();
                PropertyValue string = element.readProperty (SVGMenuElementEventSourceCD.PROP_STRING);
                MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
            }
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            List<PropertyValue> array = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            return array != null  &&  array.size () != 0;
        }

        public int getCount (DesignComponent component) {
            List<PropertyValue> array = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            return array != null ? array.size () : 0;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

}
