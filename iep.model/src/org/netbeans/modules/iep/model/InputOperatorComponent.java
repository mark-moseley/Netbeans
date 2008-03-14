package org.netbeans.modules.iep.model;

public interface InputOperatorComponent extends OperatorComponent, MultiWSDLComponentReference {

	public static String PROP_WS_INPUT_KEY = "wsInput";
	
	boolean isWebServiceInput();
	
}
