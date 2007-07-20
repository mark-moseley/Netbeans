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
 */
package org.netbeans.modules.vmd.api.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a factory for components. The DescriptorRegistry contains a set of component producers. These producers are
 * usually visualized in the palette and represents a component, a group of components or component with special post-initialization.
 * ComponentProducer is automatically created for all ComponentDescriptors which return non-null value from getPaletteDescriptor method.
 *
 * Usually you have to implement postInitialize method to initialize main component and/or create secondary components.
 * Then you have to implement checkValidity method.
 *
 * @author David Kaspar
 */
public abstract class ComponentProducer {

    private String producerID;
    private TypeID typeID;
    private PaletteDescriptor paletteDescriptor;

    /**
     * Creates a component producer.
     * @param producerID the unique producer id
     * @param typeID the type id of the main component created by the producer.
     * @param paletteDescriptor the palette descriptor used for visualization of the producer.
     */
    protected ComponentProducer (String producerID, TypeID typeID, PaletteDescriptor paletteDescriptor) {
        assert producerID != null  &&  typeID != null  &&  paletteDescriptor != null;
        this.producerID = producerID;
        this.typeID = typeID;
        this.paletteDescriptor = paletteDescriptor;
    }

    /**
     * Returns producer id.
     * @return the producer id
     */
    public final String getProducerID () {
        return producerID;
    }

    /**
     * Returns a type id of the main component created by the producer.
     * @return the type id of the main component
     */
    public final TypeID getMainComponentTypeID () {
        return typeID;
    }

    /**
     * Returns palette descriptor of the producer.
     * @return the palette descriptor
     */
    public final PaletteDescriptor getPaletteDescriptor () {
        return paletteDescriptor;
    }

    /**
     * Creates a component.
     * @param document the document
     * @return the result of creation
     */
    public final Result createComponent (DesignDocument document) {
        DesignComponent mainComponent = createMainComponent(document);
        assert mainComponent != null;
        Result result = postInitialize (document, mainComponent);
        assert result != null;
        assert result.getMainComponent () == mainComponent;
        return result;
    }
    
    /**
     * Creates or finds a main component of the producer for a document.
     * @param document the docuemnt
     * @return created or found non-null main component
     */
    protected DesignComponent createMainComponent (DesignDocument document) {
        return document.createComponent (getMainComponentTypeID ());
    }

    /**
     * Post-initialize main component. You can also create secondary components and initialize them too.
     * Default implementation returns a result with unchanged main component only.
     * @param document the document
     * @param mainComponent the main component usually created from getMainComponentTypeID method
     * @return the result of creation
     */
    public Result postInitialize (DesignDocument document, DesignComponent mainComponent) {
        return new Result (mainComponent);
    }
    
    /**
     * Called for checking validity or availability of the producer for a specified document.
     * Usually it check whether the main component is in registry and the class in target language is
     * available on the class of a project where the document belongs.
     * 
     * @param document the document where the producer could be used (and therefore checked against)
     * @return the result checking; true if the producer is valid
     */
    public abstract boolean checkValidity(DesignDocument document);

    /**
     * Represents the result of creation by the producer. Should be created by implementation of ComponentProducer.createComponent method.
     */
    public static final class Result {

        private DesignComponent mainComponent;
        private List<DesignComponent> components;

        /**
         * Creates a result with an array of components that are created by a producer.
         * @param components the array of components; the first component is taken as the main component
         */
        public Result (DesignComponent... components) {
            this.mainComponent = components.length > 0 ? components[0] : null;
            this.components = Arrays.asList (components);
            assert ! this.components.contains (null);
        }

        /**
         * Creates a result with a list of components that are created by a producer.
         * @param mainComponent the main component
         * @param components the list of non-main components
         */
        public Result (DesignComponent mainComponent, List<DesignComponent> components) {
            this.mainComponent = mainComponent;
            this.components = components;
        }

        /**
         * Returns a main component.
         * @return the main component
         */
        public DesignComponent getMainComponent () {
            return mainComponent;
        }

        /**
         * Returns all components created by a producer.
         * @return the list of all components
         */
        public List<DesignComponent> getComponents () {
            return components;
        }

    }

    static ComponentProducer createDefault (ComponentDescriptor descriptor) {
        PaletteDescriptor paletteDescriptor = descriptor.getPaletteDescriptor ();
        TypeID typeid = descriptor.getTypeDescriptor ().getThisType ();
        if (paletteDescriptor == null)
            return null;

        return new ComponentProducer (typeid.toString (), typeid, paletteDescriptor) {
            public boolean checkValidity(DesignDocument document) {
                return true;
            }
        };
    }

}
