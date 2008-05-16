package org.netbeans.modules.iep.model;

public interface OutputOperatorComponent extends OperatorComponent, MultiWSDLComponentReference {

    public static String PROP_WS_OUTPUT_KEY = "wsOutput";
    
    boolean isWebServiceOutput();
}
