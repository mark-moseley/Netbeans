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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Service;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTestCase;
import java.util.*;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public class WSDLModelTest extends NbTestCase {
    
    private /* String*/ List serviceNames, portNames, opNames, opTypes, paramNames, paramTypes;
    
    private Object expectedValue, realValue;
    private int numberOfEvents;
    
    private static final String[][] SERVICE_NAMES={{"Test2WS"},{"AddNumbers"},{"AddNumbers1"}};
    private static final String[][] PORT_NAMES={{"FooPortTypePort"},{"AddNumbersSEIPort"},{"AddNumbersSEIPort"}};
    private static final String[][] OP_NAMES={{"FooOperation"},{"add"},{"add"}};
    private static final String[][] OP_TYPES={{"org.netbeans.xml.examples.targetns.ItemType"},{"int"},{"int"}};
    private static final String[][] PARAM_NAMES={{"id"},{"x","y"},{"x","y"}};
    private static final String[][] PARAM_TYPES={{"java.lang.String"},{"int","int"},{"int","int"}};
    
    public WSDLModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    public void testAsynchronousModelCreation() throws java.net.MalformedURLException {
        System.out.println("Test 1 : Asynchronous Model Creation");
        initResults();
        final WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getUrl("T2.wsdl"));
        assertNotNull("WsdlModelerFactory failed to create object", wsdlModeler);
        for (int i=0;i<1000;i++) {
            final int j=i+1; 
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                    public void modelCreated(WsdlModel model) {
                        System.out.println("T2("+String.valueOf(j)+") model Created "+"THREAD:"+Thread.currentThread().getName());
                        compareWsdl(model,0);
                    }
            });
        }

        wsdlModeler.task.waitFinished();
        System.out.println("Test 1 : FINISHED");
        if (expectedValue!=null) assertEquals (expectedValue,realValue);
        assertEquals(1000,numberOfEvents);
    }
    public void testSynchronousModelCreation() throws java.net.MalformedURLException {
        System.out.println("Test 2 : Synchronous Model Creation");
        initResults();
        final WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getUrl("AddNumbers.wsdl"));
        assertNotNull("WsdlModelerFactory failed to create object", wsdlModeler);
        WsdlModel wsdlModel = wsdlModeler.getAndWaitForWsdlModel();
        assertNotNull("WsdlModeler failed to create WSDL Model", wsdlModel);
        compareWsdl(wsdlModel,1);
        if (expectedValue!=null) assertEquals (expectedValue,realValue);
    }
    
    public void testInvalidWsdl() throws java.net.MalformedURLException {
        System.out.println("Test 3 : Invalid WSDL");
        final WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getUrl("T2_invalid.wsdl"));
        assertNotNull("WsdlModelerFactory failed to create object", wsdlModeler);
        WsdlModel wsdlModel = wsdlModeler.getAndWaitForWsdlModel();
        assertNull("WsdlModeler should fail for this WSDL",wsdlModel);
        System.out.println("TEST 3 Exception : "+wsdlModeler.getCreationException());
        assertNotNull("Exception shouldn't be null", wsdlModeler.getCreationException());
    }
    
    public void testReloadWsdl() throws java.net.MalformedURLException {
        System.out.println("Test 4 : Reload Test WSDL");
        initResults();
        final WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getUrl("AddNumbers.wsdl"));
        assertNotNull("WsdlModelerFactory failed to create object", wsdlModeler);
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    System.out.println("AddNumbers(1) model Created, THREAD:"+Thread.currentThread().getName());
                    compareWsdl(model,1);
                }
        });
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    System.out.println("AddNumbers(2) model Created, THREAD:"+Thread.currentThread().getName());
                    compareWsdl(model,1);
                }
        });
        wsdlModeler.task.waitFinished();
        Thread.currentThread().yield();
        wsdlModeler.setWsdlUrl(getUrl("AddNumbers1.wsdl"));
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    System.out.println("AddNumbers(3) model Created, THREAD:"+Thread.currentThread().getName());
                    compareWsdl(model,2);
                }
        },true);
        wsdlModeler.task.waitFinished();
        System.out.println("Test 4 : FINISHED");
        if (expectedValue!=null) assertEquals (expectedValue,realValue);
        assertEquals(3,numberOfEvents);
    }

    public void testGarbageCollection() {
        System.out.println("Test 4 : Garbage Collection Test");
        try {Thread.sleep(100);} catch (InterruptedException ex) {}
        System.gc();System.gc();System.gc();
        WeakHashMap modelers = WsdlModelerFactory.getDefault().modelers;
        assertNotNull(modelers);
        System.out.println("TEST 4 - modelers.size = "+modelers.size());
        
        URL[] urls = new URL[modelers.keySet().size()];
        
        modelers.keySet().toArray(urls);
        for (int i=0;i<urls.length;i++) {
            if (urls[i]!=null) System.out.println("value = "+((WeakReference)modelers.get(urls[i])).get());
            assertNull(urls[i]);
        }
    }
    
    private void compareWsdl(WsdlModel model, int testNumber) {
        if (expectedValue!=null || realValue!=null) return;
        initLists();
        List services = model.getServices();
        for (Iterator it = services.iterator();it.hasNext();) {
            WsdlService s = (WsdlService)it.next();
            serviceNames.add(s.getName());
            List ports = s.getPorts();
            for (Iterator it1 = ports.iterator();it1.hasNext();) {
                WsdlPort port = (WsdlPort)it1.next();
                portNames.add(port.getName());
                List operations = port.getOperations();
                for (Iterator it2 = operations.iterator();it2.hasNext();) {
                    WsdlOperation op = (WsdlOperation) it2.next();
                    opNames.add(op.getName());
                    opTypes.add(op.getReturnTypeName());
                    List parameters = op.getParameters();
                    for (Iterator it3 = parameters.iterator();it3.hasNext();) {
                        WsdlParameter param = (WsdlParameter)it3.next();
                        paramNames.add(param.getName());
                        paramTypes.add(param.getTypeName());
                    }
                }
            }
        }
        compareResults(testNumber);
        numberOfEvents++;
    }
    
    private URL getUrl(String file) throws java.net.MalformedURLException {
        return new File(getDataDir(),file).toURL();
    }
    
    private void compareResults(int testNumber) {
        
        if (!comp(SERVICE_NAMES[testNumber].length,serviceNames.size())) return;
        if (!comp(PORT_NAMES[testNumber].length,portNames.size())) return;
        if (!comp(OP_NAMES[testNumber].length,opNames.size())) return;
        if (!comp(OP_TYPES[testNumber].length,opTypes.size())) return;
        if (!comp(PARAM_NAMES[testNumber].length,paramNames.size())) return;
        if (!comp(PARAM_TYPES[testNumber].length,paramTypes.size())) return;
        for (int i=0;i<SERVICE_NAMES[testNumber].length;i++) {
            if (!comp(SERVICE_NAMES[testNumber][i],serviceNames.get(i))) return;
        }
        for (int i=0;i<PORT_NAMES[testNumber].length;i++) {
            if (!comp(PORT_NAMES[testNumber][i],portNames.get(i))) return;
        }
        for (int i=0;i<OP_NAMES[testNumber].length;i++) {
            if (!comp(OP_NAMES[testNumber][i],opNames.get(i))) return;
        }
        for (int i=0;i<OP_TYPES[testNumber].length;i++) {
            if (!comp(OP_TYPES[testNumber][i],opTypes.get(i))) return;
        }
        for (int i=0;i<PARAM_NAMES[testNumber].length;i++) {
            if (!comp(PARAM_NAMES[testNumber][i],paramNames.get(i))) return;
        }
        for (int i=0;i<PARAM_TYPES[testNumber].length;i++) {
            if (!comp(PARAM_TYPES[testNumber][i],paramTypes.get(i))) return;
        }
    }
    
    private void initLists() {
        serviceNames = new ArrayList();
        portNames = new ArrayList();
        opNames = new ArrayList();
        opTypes = new ArrayList();
        paramNames = new ArrayList();
        paramTypes = new ArrayList();
    }
    
    private void initResults() {
        expectedValue=null;realValue=null;
        numberOfEvents=0;
    }
    
    private boolean comp(int x, int y) {
        if (x!=y) {
            expectedValue = new Integer(x);
            realValue = new Integer(y);
            return false;
        } else {
            return true;
        }
    }

    private boolean comp(Object x, Object y) {
        if (!x.equals(y)) {
            expectedValue = x;
            realValue = y;
            return false;
        } else {
            return true;
        }
    }
}
