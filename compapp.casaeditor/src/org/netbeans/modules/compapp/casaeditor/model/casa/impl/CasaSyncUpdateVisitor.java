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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connection;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connections;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumer;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provider;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceAssembly;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Services;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 * @author jqian
 */
public class CasaSyncUpdateVisitor extends CasaComponentVisitor.Default 
        implements ComponentUpdater<CasaComponent> {
    
    private CasaComponent target;
    private Operation operation;
    private int index;
    
    public CasaSyncUpdateVisitor() {
    }

    public void update(CasaComponent target, CasaComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }

    public void update(CasaComponent target, CasaComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }
    /*

    private void insert(String propertyName, CasaComponent component) {
        ((CasaComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, CasaComponent component) {
        ((CasaComponentImpl)target).removeChild(propertyName, component);
    }
    
    public void visit(CasaServiceUnits component) {
        if (target instanceof Casa) {
            if (operation == Operation.ADD) {
                insert(Casa.SERVICE_UNITS_PROPERTY, component);
            } else {
                remove(Casa.SERVICE_UNITS_PROPERTY, component);
            }
        }
    }

    public void visit(CasaEngineServiceUnit component) {
        if (target instanceof CasaServiceUnits) {
            if (operation == Operation.ADD) {
                insert(CasaServiceUnits.ENGINE_SERVICE_UNIT_PROPERTY, component);
            } else {
                remove(CasaServiceUnits.ENGINE_SERVICE_UNIT_PROPERTY, component);
            }
        }
    }

     public void visit(CasaBindingServiceUnit component) {
        if (target instanceof CasaServiceUnits) {
            if (operation == Operation.ADD) {
                insert(CasaServiceUnits.BINDING_SERVICE_UNIT_PROPERTY, component);
            } else {
                remove(CasaServiceUnits.BINDING_SERVICE_UNIT_PROPERTY, component);
            }
        }
    }
     
    public void visit(ServiceAssembly component) {
        if (target instanceof JBI) {
            if (operation == Operation.ADD) {
                insert(JBI.SERVICE_ASSEMBLY_PROPERTY, component);
            } else {
                remove(JBI.SERVICE_ASSEMBLY_PROPERTY, component);
            }
        }
    }
    
    public void visit(ServiceUnit component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.SERVICE_UNIT_PROPERTY, component);
            } else {
                remove(ServiceAssembly.SERVICE_UNIT_PROPERTY, component);
            }
        }
    }
    
    public void visit(Connections component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.CONNECTIONS_PROPERTY, component);
            } else {
                remove(ServiceAssembly.CONNECTIONS_PROPERTY, component);
            }
        }
    }
    
    public void visit(Identification component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.IDENTIFICATION_PROPERTY, component);
            } else {
                remove(ServiceAssembly.IDENTIFICATION_PROPERTY, component);
            }
        } else if (target instanceof ServiceUnit) {
            if (operation == Operation.ADD) {
                insert(ServiceUnit.IDENTIFICATION_PROPERTY, component);
            } else {
                remove(ServiceUnit.IDENTIFICATION_PROPERTY, component);
            }
        }
    }
    
    public void visit(Target component) {
        if (target instanceof ServiceUnit) {
            if (operation == Operation.ADD) {
                insert(ServiceUnit.TARGET_PROPERTY, component);
            } else {
                remove(ServiceUnit.TARGET_PROPERTY, component);
            }
        } 
    }
    
    public void visit(Connection component) {
        if (target instanceof Connections) {
            if (operation == Operation.ADD) {
                insert(Connections.CONNECTION_PROPERTY, component);
            } else {
                remove(Connections.CONNECTION_PROPERTY, component);
            }
        }
    }

    public void visit(Consumer component) {
        if (target instanceof Connection) {
            if (operation == Operation.ADD) {
                insert(Connection.CONSUMER_PROPERTY, component);
            } else {
                remove(Connection.CONSUMER_PROPERTY, component);
            }
        }
    }

     public void visit(Provider component) {
        if (target instanceof Connection) {
            if (operation == Operation.ADD) {
                insert(Connection.PROVIDER_PROPERTY, component);
            } else {
                remove(Connection.PROVIDER_PROPERTY, component);
            }
        }
    }
     **/
}
