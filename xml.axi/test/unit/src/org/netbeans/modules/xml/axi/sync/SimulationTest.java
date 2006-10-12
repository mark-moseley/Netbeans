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

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator.Pattern;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.netbeans.modules.xml.axi.impl.AttributeProxy;
import org.netbeans.modules.xml.axi.impl.CompositorProxy;
import org.netbeans.modules.xml.axi.impl.ElementImpl;
import org.netbeans.modules.xml.axi.impl.ElementImpl.AnonymousType;
import org.netbeans.modules.xml.axi.impl.ElementProxy;
import org.netbeans.modules.xml.axi.impl.ElementRef;
import org.netbeans.modules.xml.axi.util.SimulationHelper;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;


/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SimulationTest extends AbstractSyncTestCase {
    
    public static final String TEST_XSD         = "resources/empty.xsd";
    private SimulationHelper helper;
    private AXIModel model;
    private AXIDocument doc;
    private ComponentEventListener componentListener;
    private ModelPCL pcl;
    
    /**
     * SimulationTest
     */
    public SimulationTest(String testName) {
	super(testName, TEST_XSD, null);
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.model = getAXIModel();
        this.doc = model.getRoot();
        this.helper = new SimulationHelper(model);
        this.componentListener = new ComponentEventListener();
        this.pcl = new ModelPCL();
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite(SimulationTest.class);
	return suite;
    }
    
    private void init(Pattern pattern) {
        helper.clearAll();
        model.setSchemaDesignPattern(pattern);
        assert(doc.getChildren().size() == 0);        
        getAXIModel().removeComponentListener(componentListener);
        componentListener.clear();
        pcl.clear();
    }
    
    public void testVarious() throws Exception {
        simulateChangeElementsType1();
        simulateChangeElementsType2();
        simulateChangeElementsType3();
        simulateChangeElementsType4();
        simulateChangeCompositor1();
        simulateChangeCompositor2();
        simulateDropOnRussianDoll();
        simulateDropOnGardenOfEden();
    }
    
    //drop E1, add E2 to E1, drop GCT1, add A1, A2 to GCT1 then
    //change the type of E1 to GCT1. Verify the results.
    public void simulateChangeElementsType1() throws Exception {
        init(Pattern.RUSSIAN_DOLL);
        
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        Element e2 = helper.dropElement(e1, "E2");
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        assert(helper.inModel(e1) && helper.inModel(e2));
        assert(e1.getType() instanceof AnonymousType);
        
        //drop a GCT named GCT1
        ContentModel gct1 = helper.dropGlobalComplexType("GCT1");
        assert(doc.getChildren().size() == 2);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        Element a1 = helper.dropElement(gct1, "A1");
        Element a2 = helper.dropElement(gct1, "A2");
        assert(helper.inModel(gct1) && helper.inModel(a1) && helper.inModel(a2));
        assert(gct1.getChildElements().size() == 2);
        
        //set the element's type to "GCT1"
        helper.setElementType(e1, gct1);
        CompositorProxy cP = (CompositorProxy)e1.getChildren().get(0);
        ElementProxy ep1 = (ElementProxy)cP.getChildren().get(0);
        ElementProxy ep2 = (ElementProxy)cP.getChildren().get(1);
        assert(e1.getChildElements().size() == 2);
        assert(ep1.getName().equals("A1") && ep2.getName().equals("A2"));
        assert(ep1.getOriginal() == a1 && ep2.getOriginal() == a2);
        assert(e1.getType() instanceof ContentModel && e1.getType() == gct1);
    }
    
    //drop E1, set the type to some simple type. drop GCT1, change the type of E1
    //to GCT1. Add an element E2 to GCT1. Verify the results.
    public void simulateChangeElementsType2() throws Exception {        
        init(Pattern.RUSSIAN_DOLL);
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        AXIType type = DatatypeFactory.getDefault().createPrimitive(Datatype.Kind.ANYURI.getName());
        helper.setElementType(e1, type);
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        assert(helper.inModel(e1));
        assert(e1.getType() instanceof Datatype);
        
        //drop a GCT named GCT1
        ContentModel gct1 = helper.dropGlobalComplexType("GCT1");
        assert(doc.getChildren().size() == 2);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        
        //set the element's type to "GCT1"
        helper.setElementType(e1, gct1);
        assert(e1.getChildElements().size() == 0);
        assert(e1.getType() instanceof ContentModel && e1.getType() == gct1);

        //getAXIModel().addComponentListener(listener);
        Element a1 = helper.dropElement(gct1, "A1");
        assert(helper.inModel(a1));
        assert(gct1.getChildElements().size() == 1);
        //assert(listener.getAddedCount() == 3);
        
        Compositor c = (Compositor)gct1.getChildren().get(0);
        CompositorProxy cP = (CompositorProxy)e1.getChildren().get(0);        
        ElementProxy ep1 = (ElementProxy)cP.getChildren().get(0);        
        assert(e1.getChildElements().size() == 1);
        assert(ep1.getName().equals("A1"));
        assert(ep1.getOriginal() == a1);
    }
    
    //drop E1, set the type to some simple type.
    //Add an element E2 to E1. Verify the results.
    public void simulateChangeElementsType3() throws Exception {
        init(Pattern.RUSSIAN_DOLL);
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        AXIType type = DatatypeFactory.getDefault().createPrimitive(Datatype.Kind.ANYURI.getName());        
        helper.setElementType(e1, type);
        assert(helper.inModel(e1));
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        assert(e1.getType() instanceof Datatype);
        Element e2 = helper.dropElement(e1, "E2");
        assert(helper.inModel(e2));
        assert(e1.getChildElements().size() == 1);
        assert(e1.getType() instanceof AnonymousType);
        Compositor c = (Compositor)e1.getChildren().get(0);
        assert(c.getChildren().get(0) == e2);
    }
    
    //drop a GCT 'PAR', drop an element 'PAR', change the type of
    //element to 'PAR' and then drop an element on element 'PAR'
    //now delete the element.
    public void simulateChangeElementsType4() throws Exception {
        init(Pattern.RUSSIAN_DOLL);
        //drop a global element E1
        ContentModel cm = helper.dropGlobalComplexType("PAR");
        Element e1 = helper.dropGlobalElement("PAR");
        assert(helper.inModel(cm) && helper.inModel(e1));
        helper.setElementType(e1, cm);
        assert(helper.inModel(e1));
        assert(doc.getChildren().size() == 2);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        assert(e1.getType() instanceof ContentModel);
        
        Element e2 = helper.dropElement(e1, "E2");
        assert(helper.inModel(e2));
        assert(e1.getChildElements().size() == 1);
        assert(e1.getType() instanceof ContentModel);
        CompositorProxy cp = (CompositorProxy)e1.getChildren().get(0);
        assert(cp.getChildren().get(0).getOriginal() == e2);
    }
    
    //drop E1, add a sequence to E1, add two children E11, E12 to sequence
    //change the sequence to a choice, verify the result.
    public void simulateChangeCompositor1() throws Exception {
        init(Pattern.RUSSIAN_DOLL);
        int count = 0;
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        assert(helper.inModel(e1));
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        
        Compositor sequence = helper.dropCompositor(e1, CompositorType.SEQUENCE);
        assert(helper.inModel(sequence));
        assert(e1.getType() instanceof AnonymousType);
        
        Element e11 = helper.dropElementOnCompositor(sequence, "E11");
        Element e12 = helper.dropElementOnCompositor(sequence, "E12");
        assert(helper.inModel(e11) && helper.inModel(e12));
        assert(e1.getChildElements().size() == 2);        
        
        e1.addPropertyChangeListener(pcl);
        helper.setCompositorType(sequence, CompositorType.CHOICE);
        assert(pcl.getEvents().size() == 2);
        PropertyChangeEvent evt1 = pcl.getEvents().get(0);
        PropertyChangeEvent evt2 = pcl.getEvents().get(1);
        assert(evt1.getPropertyName().equals(Compositor.PROP_COMPOSITOR)
            && (evt1.getOldValue() == sequence) && (evt1.getNewValue() == null));
        assert(evt2.getPropertyName().equals(Compositor.PROP_COMPOSITOR)
            && (evt2.getOldValue() == null) && (evt2.getNewValue() != null));
        assert(!helper.inModel(sequence));
        assert(!helper.inModel(e11) && !helper.inModel(e12));
        assert(e1.getChildElements().size() == 2);
    }    
    
    //drop E1, add a sequence to E1, add a child E2 to sequence
    //add one more child choice to sequence, add a child E3 to choice.
    //change the sequence to a choice, verify the result.
    public void simulateChangeCompositor2() throws Exception {
        init(Pattern.RUSSIAN_DOLL);
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        assert(helper.inModel(e1));
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        
        Compositor sequence = helper.dropCompositor(e1, CompositorType.SEQUENCE);
        assert(helper.inModel(sequence));
        assert(e1.getType() instanceof AnonymousType);
        
        Element e2 = helper.dropElementOnCompositor(sequence, "E2");
        assert(helper.inModel(e2));
        
        //drop a choice
        Compositor choice = getAXIModel().getComponentFactory().createChoice();
        helper.dropChildAtIndex(sequence, choice, 0);
        assert(helper.inModel(choice));
        assert(sequence.getChildren().size() == 2);
        Element e3 = helper.dropElementOnCompositor(choice, "E3");
        assert(helper.inModel(e3));
        assert(e1.getChildElements().size() == 2);
        
        e1.addPropertyChangeListener(pcl);
        helper.setCompositorType(sequence, CompositorType.CHOICE);
        assert(pcl.getEvents().size() == 2);
        PropertyChangeEvent evt1 = pcl.getEvents().get(0);
        PropertyChangeEvent evt2 = pcl.getEvents().get(1);
        assert(evt1.getPropertyName().equals(Compositor.PROP_COMPOSITOR)
            && (evt1.getOldValue() == sequence) && (evt1.getNewValue() == null));
        assert(evt2.getPropertyName().equals(Compositor.PROP_COMPOSITOR)
            && (evt2.getOldValue() == null) && (evt2.getNewValue() != null));
        assert(!helper.inModel(sequence) && !helper.inModel(e2) && !helper.inModel(choice));
        assert(e1.getChildElements().size() == 2);
    }    
    
    public void simulateDropOnRussianDoll() throws IOException {
        init(Pattern.RUSSIAN_DOLL);
        
        //drop a global element E1
        Element e1 = helper.dropGlobalElement("E1");
        //model looks like: GCT1(SEQ(E1))
        assert(doc.getChildren().size() == 1);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 1);
        assert(helper.inModel(e1));
        
        //drop a GCT named GCT1
        ContentModel cm = helper.dropGlobalComplexType("GCT1");
        assert(doc.getChildren().size() == 2);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        assert(helper.inModel(cm));
        
        //set the element's type to "GCT1"
        helper.setElementType(e1, cm);
        assert(e1.getType() instanceof ContentModel && e1.getType() == cm);
        
        //drop an attribute A1 on E1. This should get added to GCT1 instead.
        Attribute a1 = helper.dropAttributeOnElement(e1, "A1");
        assert(helper.inModel(a1));
        AttributeProxy aP = (AttributeProxy)e1.getChildren().get(0);
        assert(doc.getChildren().size() == 2);
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        assert(aP.getOriginal() == a1);
        
        //delete the attribute that appears in E1
        helper.delete(aP);
        assert(cm.getChildren().size() == 0);
    }
    
    public void simulateDropOnGardenOfEden() throws IOException {
        init(Pattern.GARDEN_OF_EDEN);
        //drop a GCT named GCT1
        ContentModel cm = helper.dropGlobalComplexType("GCT1");
        //drop an element E1 on GCT1
        Element e1 = helper.dropElement(cm, "E1");
        //codegen mutated schema model which now should look like E1, GCT1(SEQ(ref(E1)))
        assert(model.getSchemaModel().getSchema().getChildren().size() == 2);
        //post sync axi model should look like E1, GCT1(SEQ(ref(E1)))
        assert(doc.getChildren().size() == 2);
        assert(doc.getContentModels().size() == 1);
        ElementRef ref1 = (ElementRef)cm.getCompositor().getChildren().get(0);
        assert(ref1.getReferent().isGlobal());
        assert(ref1.getReferent() instanceof ElementImpl);
        assert(ref1.getReferent().getName().equals("E1"));        
        Element e2 = helper.dropElement(ref1, "E2");
        //codegen mutated schema model which looks like E1, GCT1(SEQ(ref(E1)))
        assert(model.getSchemaModel().getSchema().getChildren().size() == 4);
        //model.sync();
        assert(doc.getChildren().size() == 4);
        ContentModel cm2 = doc.getContentModels().get(1);
        assert(cm2.getName().equals("E1Type"));        
        ElementRef ref2 = (ElementRef)cm2.getCompositor().getChildren().get(0);
        assert(ref2.getReferent().isGlobal());
        assert(ref2.getReferent() instanceof ElementImpl);
        assert(ref2.getReferent().getName().equals("E2"));
    }
        
    public static class ModelPCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            events.add(event);
        }
        public List<PropertyChangeEvent> getEvents() {
            return events;
        }
        
        public void clear() {
            events.clear();
        }
                        
        private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    }
    
    public static class ComponentEventListener implements ComponentListener {
        
        public void valueChanged(ComponentEvent event) {
            //deleted.add(event);
        }
        
        public void childrenDeleted(ComponentEvent event) {
            deleted.add(event);
        }
        
        public void childrenAdded(ComponentEvent event) {
            added.add(event);
        }
        
        public int getAddedCount() {
            return added.size();
        }
        
        public int getDeletedCount() {
            return deleted.size();
        }

        public void clear() {
            added.clear();
            deleted.clear();
        }
                        
        private List<ComponentEvent> added = new ArrayList<ComponentEvent>();
        private List<ComponentEvent> deleted = new ArrayList<ComponentEvent>();
    }
}
