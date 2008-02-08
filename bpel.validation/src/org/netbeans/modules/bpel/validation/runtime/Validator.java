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
package org.netbeans.modules.bpel.validation.runtime;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.ExtensibleAssign;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.LinkContainer;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.SourceContainer;
import org.netbeans.modules.bpel.model.api.TargetContainer;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.bpel.validation.core.Outcome;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends org.netbeans.modules.bpel.validation.core.Validator {
    
    // vlv
    private void processCorrelationsHolder(CorrelationsHolder holder) {
//out();
//out();
//out("processCorrelationsHolder: " + holder);
      if (holder instanceof Reply) {
//out("[skip]");
        return;
      }
      CreateInstanceActivity creator = getCreateInstanceActivity(holder);
      CorrelationContainer container = holder.getCorrelationContainer();
//out("creator: " + creator);

      // # 105786
      if (container == null && !isCreateInstanceYes(creator)) {
        // # 99711
        addWarning("FIX_Empty_Correlations", holder); // NOI18N
        return;
      }
      if (container == null) {
        return;
      }
      Correlation [] correlations = container.getCorrelations();

      // # 105786
      if ((correlations == null || correlations.length == 0) && !isCreateInstanceYes(creator)) {
        // # 99711
        addWarning("FIX_Empty_Correlations", container); // NOI18N
        return;
      }
      // # 81537
      List<CorrelationSet> sets = new ArrayList<CorrelationSet>();

      for (Correlation correlation : correlations) {
        if (correlation.getInitiate() != Initiate.YES) {
          continue;
        }
        CorrelationSet set = correlation.getSet().get();

        if (sets.contains(set)) {
          addError("FIX_Repeated_Corelation_Sets", container); // NOI18N
          return;
        }
        sets.add(set);
      }
      if (creator != null && creator.getCreateInstance() == TBoolean.YES) {
        return;
      }
      // # 96091
      for (Correlation correlation : correlations) {
        Initiate initiate = correlation.getInitiate();
//out("  see: " + initiate);

        if (initiate == null || initiate == Initiate.NO) {
//out("    ok");
          return;
        }
      }
      addWarning("FIX_Correlating_Activity", container);
    }

    private boolean isCreateInstanceYes(CreateInstanceActivity activity) {
      return activity != null && activity.getCreateInstance() == TBoolean.YES;
    }

    private CreateInstanceActivity getCreateInstanceActivity(Component component) {
      if (component instanceof CreateInstanceActivity) {
        return (CreateInstanceActivity) component;
      }
      if (component.getParent() instanceof CreateInstanceActivity) {
        return (CreateInstanceActivity) component.getParent();
      }
      return null;
    }

    @Override
    public void visit( Process process ) {

        String queryLang = process.getQueryLanguage();
        if ( queryLang != null ) {
            addAttributeWarning( Process.QUERY_LANGUAGE, process );
        }
        String expression = process.getExpressionLanguage();
        if ( expression != null ) {
            addAttributeWarning( Process.EXPRESSION_LANGUAGE, process );
        }
        TBoolean value = process.getSuppressJoinFailure();
        if ( value != null ) {
            addAttributeWarning( Process.SUPPRESS_JOIN_FAILURE, process );
        }
        value = process.getExitOnStandardFault();
        if ( value != null ) {
            addAttributeWarning( Process.EXIT_ON_STANDART_FAULT, process );
        }
        
        // check whether the URI is valid.
        checkValidURI(process, Process.QUERY_LANGUAGE, process.getQueryLanguage());
        checkValidURI(process, Process.EXPRESSION_LANGUAGE, process.getExpressionLanguage());
    }
    
    @Override
    public void visit( Validate validate ) {
        addElementError( validate );
    }
    
    @Override
    public void visit( ReThrow reThrow ) {
       // addElementError( reThrow ); //Rethrow is supported now
    }
    
    @Override
    public void visit( Compensate compensate ) {
//      addElementError( compensate );
    }
    
    @Override
    public void visit( PartnerLink partnerLink ) {
        if ( partnerLink.getInitializePartnerRole() != null ) {
            addAttributeWarning( PartnerLink.INITIALIZE_PARTNER_ROLE, partnerLink);
        }
    }
    
    @Override
    public void visit( CompensationHandler handler ) {
//      addElementError( handler );
    }
    
    @Override
    public void visit( TerminationHandler handler ) {
//      addElementError( handler );
    }
    
    @Override
    public void visit( Variable variable ) {
        From from = variable.getFrom();
        if ( from != null ) {
            addElementsInParentError( variable , from );
        }
    }
    
    @Override
    public void visit( TargetContainer container ) {
        addElementError( container );
    }
    
    @Override
    public void visit( SourceContainer container ) {
        addElementError( container );
    }
    
    @Override
    public void visit( Invoke invoke ) {
        super.visit(invoke);
        Catch[] catches = invoke.getCatches();
        if ( catches!= null && catches.length >0 ) {
            addElementsInParentError(invoke, (BpelEntity[])catches);
        }
        CatchAll catchAll = invoke.getCatchAll();
        if ( catchAll != null ) {
            addElementsInParentError( invoke, catchAll );
        }
        
        // Rule: <fromPart>, <toPart> is not supported.
        if(invoke.getFromPartContaner() != null ) {
            addElementsInParentError(invoke, FROM_PARTS);
        }
        if(invoke.getToPartContaner() != null ) {
            addElementsInParentError(invoke, TO_PARTS);
        }
    }
    
    @Override
    public void visit( ExtensibleAssign extensibleAssign ) {
        addElementError(extensibleAssign);
    }
    
    @Override
    public void visit( Assign assign ) {
        super.visit(assign);

        if (assign.getValidate() != null) {
            addAttributeWarning( Assign.VALIDATE, assign );
        }
    }
    
    @Override
    public void visit( From from ) {
        Documentation[] docs = from.getDocumentations();
        if ( docs!= null && docs.length>0 ) {
            addElementsInParentError(from, (BpelEntity[])docs);
        }
        if ( from.getExpressionLanguage()!= null ) {
            addAttributeWarning( From.EXPRESSION_LANGUAGE, from );
        }
        if ( from.getProperty()!= null ) {
            addAttributeWarning( From.PROPERTY, from );
        }
// # 123382
//        if ( from.getPartnerLink()!= null ) {
//            addAttributeWarning( From.PARTNER_LINK, from );
//        }
        if ( from.getEndpointReference()!= null ) {
            addAttributeWarning( From.ENDPOINT_REFERENCE, from );
        }
        
        checkAbsenceExtensions( from );
    }
    
    public void visit( To to ) {
    Documentation[] docs = to.getDocumentations();
        if ( docs!= null && docs.length>0 ) {
            addElementsInParentError(to, (BpelEntity[])docs);
        }
        if ( to.getProperty()!= null ) {
            addAttributeWarning( To.PROPERTY, to );
        }
// # 123382
//        if ( to .getPartnerLink()!= null ) {
//            addAttributeWarning(To.PARTNER_LINK, to );
//        }
        checkAbsenceExtensions( to );
    }
    
    @Override
    public void visit( Flow flow ) {
        super.visit(flow);
        LinkContainer container = flow.getLinkContainer();
        if ( container!= null ) {
            addElementError( container );
        }
    }
    
    @Override
    public void visit( Scope scope ) {
        super.visit(scope);
        PartnerLinkContainer container = scope.getPartnerLinkContainer();
        if ( container!= null ) {
            addElementsInParentError( scope, container );
        }
        CorrelationSetContainer setContainer = scope.getCorrelationSetContainer();
        if ( setContainer!= null ) {
            addElementsInParentError( scope, setContainer );
        }
        
        if ( scope.getIsolated() != null ) {
            addAttributeWarning( Scope.ISOLATED, scope );
        }
        if ( scope.getExitOnStandardFault()!= null ) {
            addAttributeWarning( Scope.EXIT_ON_STANDART_FAULT, scope );
        }
    }
    
    @Override
    public void visit( ForEach forEach ) {
        super.visit(forEach);
        if ( TBoolean.YES.equals( forEach.getParallel())) {
            addAttributeWarning( ForEach.PARALLEL, forEach );
        }
    }
    
    @Override
    protected void visit( Activity activity ) {
        if ( activity.getSuppressJoinFailure() !=null ) {
            addAttributeWarning(Activity.SUPPRESS_JOIN_FAILURE, activity);
        }
    }
    
    
    @Override
    public void visit(Import bpelImport) {
        
        if( ! isAttributeValueSpecified(bpelImport.getLocation())) {
            addAttributeNeededForRuntime(bpelImport.LOCATION, bpelImport);
        }
        
        if( ! isAttributeValueSpecified(bpelImport.getNamespace())) {
            addAttributeNeededForRuntime(bpelImport.NAMESPACE, bpelImport);
        }
        
    }
    
    @Override
    public void visit(Receive receive) {
        super.visit(receive);
        // Rule: <fromPart>, <toPart> is not supported.
        if(receive.getFromPartContaner()!= null ) {
            addElementsInParentError(receive, FROM_PARTS);
        }
        
        // Rule: MessageExchange not supported.
        if (receive.getMessageExchange() != null) {
            addAttributeWarning(Receive.MESSAGE_EXCHANGE, receive);
        }
        processCorrelationsHolder(receive);
    }
    
    @Override
    public void visit(Reply reply) {
        
        super.visit(reply);
        // Rule: <fromPart>, <toPart> is not supported.
        if(reply.getToPartContaner() != null ) {
            addElementsInParentError(reply, TO_PARTS);
        }
        
        // Rule: MessageExchange not supported.
        if(reply.getMessageExchange() != null) {
            addAttributeWarning(Reply.MESSAGE_EXCHANGE, reply);
        }
        processCorrelationsHolder(reply);
    }
    
    @Override
    public void visit(OnEvent onEvent) {
        // Rule: <fromPart>, <toPart> is not supported.
        if(onEvent.getFromPartContaner() != null ) {  
            addElementsInParentError(onEvent, FROM_PARTS);
        }
        
        // Rule: MessageExchange not supported.
        if(onEvent.getMessageExchange() != null) {
            addAttributeWarning(OnEvent.MESSAGE_EXCHANGE, onEvent);
        }
        processCorrelationsHolder(onEvent);
    }
    
    @Override
    public void visit(OnMessage onMessage) {
        // Rule: <fromPart>, <toPart> is not supported.
        if(onMessage.getFromPartContaner() != null ) {
            addElementsInParentError(onMessage, FROM_PARTS);
        }
        // Rule: MessageExchange not supported.
        if(onMessage.getMessageExchange() != null) {
            addAttributeWarning(OnMessage.MESSAGE_EXCHANGE, onMessage);
        }
        processCorrelationsHolder(onMessage);
    }
    
    @Override
    public void  visit(MessageExchangeContainer messageExchangeContainer) {
            addElementError(messageExchangeContainer);
    }

    @Override
    public void visit(ExtensionEntity entity) {
        // TODO a to suuport Logging Alerting extensions
    }

    private void checkAbsenceExtensions( ExtensibleElements element ) {
        if ( element instanceof AbstractDocumentComponent ){
            AbstractDocumentComponent component =
                    (AbstractDocumentComponent)element;
            
            Map map = component.getAttributeMap();
            for( Object obj : map.keySet() ){
                QName qName = (QName)obj;
                if ( qName.getNamespaceURI()!= null &&
                        qName.getNamespaceURI().length()>0 ){
                    addAttributeWarning( qName.toString() , element );
                }
            }
            
            NodeList list = component.getPeer().getChildNodes();
            for ( int i=0 ; i<list.getLength() ; i++ ){
                Node node = list.item(i);
                if ( node instanceof Element ){
                    Element childElement = (Element) node;
                    if ( !BpelEntity.BUSINESS_PROCESS_NS_URI.equals(
                            childElement.getNamespaceURI() ) ) {
                        addElementsInParentError( element ,
                                childElement.getLocalName() );
                    }
                }
            }
        }
    }
    
    private void addAttributeWarning(String attributeName, Component entities) {
        String str = i18n(getClass(), FIX_ATTRIBUTE);
        str = MessageFormat.format( str, attributeName);
        getResultItems().add(new Outcome(this, ResultType.WARNING, entities, str));
    }
    
    private void addElementError(BpelEntity entity) {
        String str = i18n(getClass(), FIX_ELEMENT);
        str = MessageFormat.format( str,  entity.getPeer().getLocalName());
        getResultItems().add(new Outcome(this, ResultType.ERROR, (Component) entity, str));
    }
    
    private void addElementsInParentError(BpelContainer parent, BpelEntity... entities) {
        assert entities.length >0;
        String str = i18n( getClass(), FIX_ELEMENT_IN_PARENT);
        str = MessageFormat.format( str,  entities[0].getPeer().getLocalName(), parent.getPeer().getLocalName());
        getResultItems().add(new Outcome(this, ResultType.ERROR, (Component)entities[0], str));
    }
    
    private void addElementsInParentError( BpelContainer parent, String tagName ) {
        String str = i18n( getClass(), FIX_ELEMENT_IN_PARENT);
        str = MessageFormat.format(str, tagName,parent.getPeer().getLocalName());
        getResultItems().add(new Outcome(this, ResultType.ERROR, (Component) parent, str));
    }
    
    private boolean isAttributeValueSpecified(String value) {
        return value != null && !value.trim().equals("");
    }
    
    private void addAttributeNeededForRuntime(String attributeName, Component component) {
        String str = i18n(getClass(), FIX_ATTRIBUTE_REQUIRED_SUN_BPELSE);
        str = MessageFormat.format(str, attributeName);
        getResultItems().add(new Outcome(this, ResultType.WARNING, component, str));
    }
    
    private void checkValidURI(BpelEntity bpelEntity, String attribute, String attributeValue) {
        if(attributeValue != null) {
            try {
                new URI(attributeValue);
            }
            catch (URISyntaxException ex) {
                String message = i18n(getClass(), FIX_INVALID_URI, attribute);
                getResultItems().add(new Outcome(this, ResultType.ERROR, bpelEntity, message));
            }
        }
    }
    
    private BpelContainer hasParent( BpelEntity entity, Class<? extends BpelContainer>... types) {
        BpelContainer parent = entity.getParent();

        while( parent != null ) {
            for( Class<? extends BpelContainer> clazz :types ) {
                if (clazz.isInstance(parent)) {
                    return parent;
                }
            }
            parent = parent.getParent();
        }
        return null;
    }
    
    private static final String FIX_ATTRIBUTE = "FIX_Attribute";    // NOI18N
    private static final String FIX_ELEMENT = "FIX_Element";        // NOI18N
    private static final String FIX_ELEMENT_IN_PARENT = "FIX_ElementInParent"; // NOI18N
    private static final String FIX_ATTRIBUTE_REQUIRED_SUN_BPELSE = "FIX_Attribute_Required_For_Sun_BpelSE"; // NOI18N
    private static final String FROM_PARTS = "<fromParts>";  // NOI18N
    private static final String TO_PARTS = "<toParts>";
    private static final String FIX_INVALID_URI = "FIX_INVALID_URI"; // NOI18N
}
