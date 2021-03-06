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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.core.roundtripframework.roundtripevents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public class RoundTripOperationEventsTestCase extends AbstractUMLTestCase implements IRoundTripAttributeEventsSink, IRoundTripClassEventsSink, IRoundTripEnumEventsSink, IRoundTripEnumLiteralEventsSink, IRoundTripOperationEventsSink, IRoundTripPackageEventsSink, IRoundTripRelationEventsSink 
{
	private  IClass tstClass;
	private IOperation tstOper;
	private int elemKind;
	private int elemKindFromInputFile;
	private int eventState;
	private int eventStateFromInputFile;	
	private File propFile;
	private String evntInfoFile = "EventInfo.txt";
	static String testDataPath = null;
	
	// This variable is to check if any events other than expected are thrown. If so , its value becomes greater than 0
	private int count = 0;
	

	/**
     * Here the Roundtrip Controller, dispatcher created and all listeners are attached.
     */
	public void setUp()
	{
		String infoFile = getDataDir()+ evntInfoFile;
		try
		{
			propFile = new File(infoFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRoundTripAttributeEvents(this, "Java");
        disp.registerForRoundTripOperationEvents(this, "Java");
        disp.registerForRoundTripClassEvents(this, "Java");
        disp.registerForRoundTripEnumEvents(this, "Java");
        disp.registerForRoundTripEnumLiteralEvents(this, "Java");
        disp.registerForRoundTripPackageEvents(this, "Java");
        disp.registerForRoundTripRelationEvents(this, "Java");
        
	}

	/**
     * OperationCreationTestCase
     */
	public void testOperationCreate()
	{
		tstClass = createClass("TestClass1");
		resetVals();
		tstOper = tstClass.createOperation("int","TestOper1");
		tstClass.addOperation(tstOper);
		readFromFile(propFile,"Operation","create");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}
	
	/**
     * OperationModificationTestCase
     */
	public void testOperationModify()
	{
		tstClass = createClass("TestClass2");
		tstOper = tstClass.createOperation("int","TestOper2");
		tstClass.addOperation(tstOper);
		resetVals();
		tstOper.setName("NewTestClass");
		readFromFile(propFile,"Operation","modify");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}
	
	/**
     * OperationDeletionTestCase
     */
	public void testOperationDelete()
	{
		tstClass = createClass("TestClass3");
		tstOper = tstClass.createOperation("int","TestOper3");
		tstClass.addOperation(tstOper);
		resetVals();
		tstOper.delete();
		readFromFile(propFile,"Operation","delete");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
		assertEquals(0, count);
	}

	public void testOperationParamerterCreate()
	{
		tstClass = createClass("TestClass5");
		tstOper = tstClass.createOperation("int","TestOper5");
		tstClass.addOperation(tstOper);
		IParameter params = tstOper.createParameter("int","NewParam1");
		resetVals();
		tstOper.addParameter(params);
		readFromFile(propFile,"Parameter","create");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);		
	}
	
	public void testOperationParamerterModify()
	{
		tstClass = createClass("TestClass6");
		tstOper = tstClass.createOperation("int","TestOper6");
		tstClass.addOperation(tstOper);
		IParameter params = tstOper.createParameter("int","NewParam2");
		tstOper.addParameter(params);
		resetVals();
		params.setType2("String");
		readFromFile(propFile,"Parameter","modify");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
	}
	
	public void testOperationParamerterDelete()
	{
		tstClass = createClass("TestClass7");
		tstOper = tstClass.createOperation("int","TestOper7");
		tstClass.addOperation(tstOper);
		IParameter params = tstOper.createParameter("int","NewParam3");
		tstOper.addParameter(params);
		resetVals();
		params.delete();
		readFromFile(propFile,"Parameter","delete");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
	}
	public void testOperationReturnTypeCreate()
	{
		tstClass = createClass("TestClass8");
		tstOper = tstClass.createOperation("int","TestOper8");
		tstClass.addOperation(tstOper);
		IParameter params = tstOper.createParameter("String","NewParam4");
		resetVals();
		tstOper.setReturnType(params);
		readFromFile(propFile,"Parameter","create");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
	}
	
	public void testOperationReturnTypeModify()
	{
		tstClass = createClass("TestClass9");
		tstOper = tstClass.createOperation("int","TestOper9");
		tstClass.addOperation(tstOper);
		IParameter params = tstOper.createParameter("String","NewParam5");
		tstOper.setReturnType(params);
		resetVals();
		(tstOper.getReturnType()).setType2("int");
		readFromFile(propFile,"Parameter","modify");
		assertEquals(elemKind,elemKindFromInputFile);
		assertEquals(eventState,eventStateFromInputFile);
	}



	/**
     * Fired before an operation has been created. We have nothing to check here.
     */
	public void onPreOperationChangeRequest(IChangeRequest chngReq, IResultCell cell) {
	}

	/**
     * Fired after an operation has been created. 
     */
	public void onOperationChangeRequest(IChangeRequest chngReq, IResultCell cell) 
	{
		elemKind = chngReq.getElementType();
		eventState = chngReq.getState();
	}

	/**
     * Attribute preCreation event - Not needed to be implemented in this test case
     */
	public void onPreAttributeChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Attribute creation event - Not needed to be implemented in this test case
     */
	public void onAttributeChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Class preCreation event - Not needed to be implemented in this test case
     */
	public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event will be fired when an operation is created for a class. We are not incrementing the count variable as we know that 
		// this event will be fired.
		
		// count++
		
	}

	/**
     * Class Creation event - Not needed to be implemented in this test case
     */
	public void onClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event will be fired when an operation is created for a class. We are not incrementing the count variable as we know that 
		// this event will be fired.
		
		// count++
	}
	
	/**
     * Enum preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPreEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}
	
	/**
     * Enum creation event - Not needed to be implemented in this test case. 
     */
	public void onEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Enum preLiteralCreation event - Not needed to be implemented in this test case. 
     */
	public void onPreEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}
	
	 /**
     * Enum LiteralCreation event - Not needed to be implemented in this test case. 
     */
	public void onEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}
	
	/**
     * Package preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPrePackageChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Package Creation event - Not needed to be implemented in this test case. 
     */
	public void onPackageChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}

	/**
     * Relation preCreation event - Not needed to be implemented in this test case. 
     */
	public void onPreRelationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
	}


	/**
     * Relation Creation event - Not needed to be implemented in this test case. 
     */
	public void onRelationChangeRequest(IChangeRequest newVal, IResultCell cell) {
		// This event should not be fired. If it is fired we are incrmenting the count variable
		count++;
		
	}
	

	/**
     * Get the path of  test Data Folder
     */
	private String getDataDir() {
		if (testDataPath != null)
			return testDataPath;

		testDataPath = System.getProperty("xtest.data");
		if (testDataPath != null)
			return (testDataPath += File.separator
					+ "RTEventInfoFiles" + File.separator);

		String s1 = getClass().getName();
		URL url = getClass().getResource(
				s1.substring(s1.lastIndexOf('.') + 1) + ".class");
		File file = (new File(url.getFile())).getParentFile();
		for (int i = 0; (i = s1.indexOf('.', i) + 1) > 0;)
			file = file.getParentFile();
		testDataPath = file.getParent() + File.separator + "data"
				+ File.separator + "RTEventInfoFiles" + File.separator;
		return testDataPath;
	}

	  /**
	 * @propFile - the input file
	 * @findElemType - the element being acted upon like attribute, operation
	 * @findChangeKind - the action on the element like create,delete etc
     * Reading from the input file
     */	
	protected void readFromFile(File propFile, String findElemType, String findChangeKind)
	{
		String str, retValString = null ;
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader(propFile));
			while ((str = in.readLine()) != null)
			{
				if((str.indexOf(findElemType) != -1))
				{
					if((str.indexOf(findChangeKind) != -1))
					{
						retValString = str.substring(str.lastIndexOf("-")+1);
						eventStateFromInputFile = Integer.parseInt(retValString);
						String subStr1 = str.substring((str.indexOf("-")+1),str.length());
						String tempStr = subStr1.substring(0,subStr1.indexOf("-"));
						elemKindFromInputFile = Integer.parseInt(tempStr);
						break;
					}
				}
			}
			in.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			
		}  
	}

	/**
     * Reset the values for certain variables
     */
	private void resetVals()
	{
		count= 0;
		elemKind = -1;	
		eventState = -1;

	}
}
