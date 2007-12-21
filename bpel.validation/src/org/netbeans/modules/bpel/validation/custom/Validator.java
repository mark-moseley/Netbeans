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
package org.netbeans.modules.bpel.validation.custom;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.impl.FindHelperImpl;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.bpel.validation.util.ResultItem;
import static org.netbeans.modules.print.api.PrintUtil.*;

public final class Validator extends org.netbeans.modules.bpel.validation.util.Validator {

    // vlv # 93078
    @Override
    public void visit(Branches branches) {
      String content = branches.getContent();

      if (content == null) {
        return;
      }
      content = content.toLowerCase();

      if (
        content.contains("true") || content.contains("false")) {
        addError("FIX_Branches", branches); // NOI18N
      }
    }

    // vlv # 81404
    @Override
    public void visit(Process process) {
      List<Reply> replies = new ArrayList<Reply>();
      List<CorrelationsHolder> holders = new ArrayList<CorrelationsHolder>();
      visitEntities(process.getChildren(), replies, holders);
      checkReplies(replies);
      // # 109412
      checkHolders(holders);

      // # 109677
      // checkUniqueImaOperationUsage(process);
    }

    private void visitEntities(List<BpelEntity> entities, List<Reply> replies, List<CorrelationsHolder> holders) {
      for (BpelEntity entity : entities) {
        if (entity instanceof Reply) {
          replies.add((Reply) entity);
        }
        else if (entity instanceof CorrelationsHolder) {
          holders.add((CorrelationsHolder) entity);
        }
        visitEntities(entity.getChildren(), replies, holders);
      }
    }

    private void checkReplies(List<Reply> replies) {
//out();
//out();
      for (int i=0; i < replies.size(); i++) {
        Reply reply1 = replies.get(i);

        for (int j=i+1; j < replies.size(); j++) {
          checkReplies(reply1, replies.get(j));
        }
      }
//out();
//out();
    }

    private void checkHolders(List<CorrelationsHolder> holders) {
//out();
//out();
      for (int i=0; i < holders.size(); i++) {
        CorrelationsHolder holder1 = holders.get(i);

        for (int j=i+1; j < holders.size(); j++) {
          checkHolders(holder1, holders.get(j));
        }
      }
//out();
//out();
    }

    private void checkReplies(Reply reply1, Reply reply2) {
//out();
//out("reply1: " + reply1.getName());
//out("reply2: " + reply2.getName());
      if ( !isInGate(reply1) && !isInGate(reply2)) {
        if (haveTheSamePartnerLink(reply1, reply2)) {
          addError("FIX_Replies_PartnerLink", reply1); // NOI18N
          addError("FIX_Replies_PartnerLink", reply2); // NOI18N
          return;
        }
      }
      if (getParent(reply1) == getParent(reply2)) {
        if (haveTheSamePartnerLink(reply1, reply2)) {
          addError("FIX_Replies_PartnerLink", reply1); // NOI18N
          addError("FIX_Replies_PartnerLink", reply2); // NOI18N
          return;
        }
      }
    }

    private void checkHolders(CorrelationsHolder holder1, CorrelationsHolder holder2) {
//out();
//out("holder1: " + holder1);
//out("holder2: " + holder2);
      if ( !isInGate(holder1) && !isInGate(holder2)) {
        if (haveTheSameCorrelationWithInitiateYes(holder1, holder2)) {
          addError("FIX_Holder_Correlation", holder1); // NOI18N
          addError("FIX_Holder_Correlation", holder2); // NOI18N
          return;
        }
      }
      if (getParent(holder1) == getParent(holder2)) {
        if (haveTheSameCorrelationWithInitiateYes(holder1, holder2)) {
          addError("FIX_Holder_Correlation", holder1); // NOI18N
          addError("FIX_Holder_Correlation", holder2); // NOI18N
          return;
        }
      }
    }

    private boolean haveTheSameCorrelationWithInitiateYes(CorrelationsHolder holder1, CorrelationsHolder holder2) {
      CorrelationContainer container1 = holder1.getCorrelationContainer();
//out("  1");

      if (container1 == null) {
        return false;
      }
//out("  2");
      Correlation[] correlations1 = container1.getCorrelations();

      if (correlations1 == null) {
        return false;
      }
//out("  3");
      CorrelationContainer container2 = holder2.getCorrelationContainer();

      if (container2 == null) {
        return false;
      }
//out("  4");
      Correlation[] correlations2 = container2.getCorrelations();

      if (correlations2 == null) {
        return false;
      }
//out("  5");
      return checkCorrelations(correlations1, correlations2);
    }

    private boolean checkCorrelations(Correlation [] correlations1, Correlation [] correlations2) {
//out(" ");
      for (Correlation correlation : correlations1) {
//out("SEE: " + correlation + " " + correlation.getInitiate());
        if (correlation.getInitiate() != Initiate.YES) {
          continue;
        }
        if (checkCorrelation(correlations2, correlation)) {
          return true;
        }
      }
      return false;
    }

    private boolean checkCorrelation(Correlation [] correlations, Correlation correlation) {
//out("for: " + correlation);
      for (Correlation next : correlations) {
//out("  see: " + next + " " + next.getInitiate());
        if (theSame(next, correlation)) {
          return next.getInitiate() == Initiate.YES;
        }
      }
      return false;
    }

    private boolean theSame(Correlation correlation1, Correlation correlation2) {
      BpelReference<CorrelationSet> ref1 = correlation1.getSet();

      if (ref1 == null) {
        return false;
      }
      BpelReference<CorrelationSet> ref2 = correlation2.getSet();

      if (ref2 == null) {
        return false;
      }
      return ref1.get() == ref2.get();
    }

    private boolean haveTheSamePartnerLink(Reply reply1, Reply reply2) {
      if (reply1.getPartnerLink() == null) {
//out("  reply1 has PL ref null");
        return false;
      }
      PartnerLink partnerLink1 = reply1.getPartnerLink().get();

      if (partnerLink1 == null) {
//out("  reply1 has PL null");
        return false;
      }
      if (reply2.getPartnerLink() == null) {
//out("  reply2 has PL ref null");
        return false;
      }
      PartnerLink partnerLink2 = reply2.getPartnerLink().get();

      if (partnerLink2 == null) {
//out("  reply2 has PL null");
        return false;
      }
      return partnerLink1 == partnerLink2;
    }

    private BpelEntity getParent(BpelEntity entity) {
      BpelEntity parent = entity.getParent();
      
      while (true) {
        if (parent instanceof Sequence) {
          parent = parent.getParent();
          continue;
        }
        break;
      }
      return parent;
    }

    private boolean isInGate(BpelEntity entity) {
//out("  isInIfElse...");
      BpelEntity parent = entity.getParent();

      while (true) {
//out("  parent: " + parent);
        if (parent == null) {
          break;
        }
        if (parent instanceof If) {
          return true;
        }
        if (parent instanceof Else) {
          return true;
        }
        if (parent instanceof ElseIf) {
          return true;
        }
        if (parent instanceof FaultHandlers) {
          return true;
        }
        if (parent instanceof Flow) {
          return true;
        }
        if (parent instanceof OnMessage) {
          return true;
        }
        parent = parent.getParent();
      }
      return false;
    }
    
    @Override
    public void visit(Reply reply)
    {
        super.visit(reply);
        WSDLReference<Operation> opRef = reply.getOperation();
        
        if ( opRef == null ) {
            return;
        }
        Operation operation = opRef.get();

        if ( operation == null ) {
            return;
        }
        if ( !(operation instanceof RequestResponseOperation) ) {
            addError( "FIX_ReplyOperation", reply, opRef.getQName().toString() ); // NOI18N
        }
    }

    private void addError( String bundleKey , Collection<Component> collection, Object... values) {
        String str = i18n(getClass(), bundleKey);

        if ( values!= null && values.length >0 ) {
            str = MessageFormat.format(str, values );
        }
        for(Component component: collection) {
            ResultItem resultItem = new ResultItem(this, ResultType.ERROR, component, str);
            getResultItems().add(resultItem);
        }
    }

    @Override
    public void visit(Receive receive) {
      // # 109677
      collectImaOperationUsage(receive);
    }

    @Override
    public void visit(OnEvent onEvent) {
      // # 109677
      collectImaOperationUsage(onEvent);
    }
    
    @Override
    public void visit(OnMessage onMessage) {
      // # 109677
      collectImaOperationUsage(onMessage);
    }
    
    /**
     * Collects usages of WSDL operations by inbound message activities (IMA).
     * It is intended to fix the issue #109677
     * 
     * It is implied that only operation makes sense because of the 
     * reference to single operation is used instead of an operation name.
     */ 
    private void collectImaOperationUsage(OperationReference ima) {
        BpelEntity imaEntity = (BpelEntity)ima;
        WSDLReference<Operation> operRef = ima.getOperation();
        if (operRef != null) {
            Operation operation = operRef.get();
            if (operation != null) {
                // Register the operation in the map
                List<BpelEntity> imaList = operationToImaListMap.get(operation);
                if (imaList == null) {
                    imaList = new ArrayList<BpelEntity>();
                    operationToImaListMap.put(operation, imaList);
                }
                imaList.add((BpelEntity)ima);
            }
        }
    }

    /**
     * Checks that a WSDL operation is used only once per BPEL process.
     * It has to be applied to inbound message activities (IMA) only.
     * It is intended to fix the issue #109677
     * 
     * It is implied that only operation makes sense because of the 
     * reference to single operation is used instead of an operation name.
     */ 
    private void checkUniqueImaOperationUsage(Process process) {
        ResultItem resultItem;
        Set<Operation> operSet = operationToImaListMap.keySet();
        for (Operation operation : operSet) {
            List<BpelEntity> imaList = operationToImaListMap.get(operation);
            if (imaList == null || imaList.size() < 2) {
                // Skip operation if it is used less then once
                continue;
            }
            //
            // Here is an unsupported case - the operation is used more then once!
            // Now it's necessary to decide if it an error or warning has to be shown.
            // Check is there a couple of IMAs, which can be executed concurrently.
            //
            // Prepare location path for each IMA
            class ImaPath {
                BpelEntity mIma;
                List<BpelContainer> mLocationPath;
                
                ImaPath(BpelEntity ima) {
                    mIma = ima;
                    mLocationPath = FindHelperImpl.getObjectPathTo(ima);
                }
            }
            ArrayList<ImaPath> imaPathList = new ArrayList<ImaPath>();
            for (BpelEntity ima : imaList) {
                ImaPath newImaPath = new ImaPath(ima);
                imaPathList.add(newImaPath);
            }
            //
            for (int index1 = 0; index1 < imaPathList.size(); index1++) {
                for (int index2 = index1 + 1; index2 < imaPathList.size(); index2++) {
                    //
                    ImaPath imaPath1 = imaPathList.get(index1);
                    ImaPath imaPath2 = imaPathList.get(index2);
                    //
                    // Compare location paths
                    int commonParentDepthIndex = -1; // the index of the common parent in both location paths
                    BpelContainer commonParent = null; // The closest common parent
                    Iterator<BpelContainer> itr1 = imaPath1.mLocationPath.iterator();
                    Iterator<BpelContainer> itr2 = imaPath2.mLocationPath.iterator();
                    while (itr1.hasNext() && itr2.hasNext()) {
                        //
                        BpelContainer bpelCont1 = itr1.next();
                        BpelContainer bpelCont2 = itr2.next();
                        if (bpelCont1.equals(bpelCont2)) {
                            commonParent = bpelCont1;
                            commonParentDepthIndex++;
                        } else {
                            // The previous parent was the last common parent.
                            break;
                        }
                    }
                    //
                    int nextIndex = commonParentDepthIndex + 1;
                    BpelEntity nextToCommon1 = null;
                    if (imaPath1.mLocationPath.size() <= nextIndex) {
                        nextToCommon1 = imaPath1.mIma;
                    } else {
                        nextToCommon1 = imaPath1.mLocationPath.get(nextIndex);
                    }
                    //
                    BpelEntity nextToCommon2 = null;
                    if (imaPath2.mLocationPath.size() <= nextIndex) {
                        nextToCommon2 = imaPath2.mIma;
                    } else {
                        nextToCommon2 = imaPath2.mLocationPath.get(nextIndex);
                    }
                    //
                    // Check if the common parent is the FLow activity
                    if (commonParent instanceof Flow) {
                        String message = i18n(getClass(), "FIX_ConcurrentFlowImaToOperationConnection"); // NOI18N
                        addDoubleResultItem(ResultType.ERROR, imaPath1.mIma, imaPath2.mIma, message);
                    }
                    //
                    // Check if the common parent is the Pick activity
                    // and both entity is the OnMessage and they are 
                    // the direct children of the Pick.
                    if (commonParent instanceof Pick && 
                            imaPath1.mIma instanceof OnMessage && 
                            imaPath2.mIma instanceof OnMessage && 
                            imaPath1.mIma == nextToCommon1 && 
                            imaPath2.mIma == nextToCommon2) {
                        String message = i18n(getClass(), "FIX_AmbiguousOnMessageToOperationConnection"); // NOI18N
                        addDoubleResultItem(ResultType.ERROR, imaPath1.mIma, imaPath2.mIma, message);
                    }
                    //
                    // Check if the common parent is the EventHandlers activity
                    if (commonParent instanceof EventHandlers) { 
                        // If both entity is the OnEvent and they are 
                        // the direct children of the EventHandler
                        if (imaPath1.mIma instanceof OnEvent && 
                            imaPath2.mIma instanceof OnEvent && 
                            imaPath1.mIma == nextToCommon1 && 
                            imaPath2.mIma == nextToCommon2) {
                            //
                            String message = i18n(getClass(), "FIX_AmbiguousOnEventToOperationConnection"); // NOI18N
                            addDoubleResultItem(ResultType.ERROR, imaPath1.mIma, imaPath2.mIma, message);
                        }
                        else { 
                            // 2 IMA in different OnEvent or OnAlarm branches 
                            String message = i18n(getClass(), "FIX_ConcurrentEventImaToOperationConnection"); // NOI18N
                            addDoubleResultItem(ResultType.ERROR, imaPath1.mIma, imaPath2.mIma, message);
                        }
                    } 
                    //
                    // Check if the common parent is the Process or Scope and one of the 
                    // location path go through an EventHandler
                    if (commonParent instanceof BaseScope && (
                            nextToCommon1 instanceof EventHandlers ||
                            nextToCommon2 instanceof EventHandlers)) {
                        String message = i18n(getClass(), "FIX_ConcurrentOutEventImaToOperationConnection"); // NOI18N
                        addDoubleResultItem(ResultType.ERROR, imaPath1.mIma, imaPath2.mIma, message);
                    }
                }
            }
            //
            // Show the BPEL SE warning
            String message = i18n(getClass(), "FIX_AmbiguousImaToOperationConnection", operation.getName(), "" + imaList.size()); // NOI18N
            List<PartnerLink> relatedPLinks = lookForPLinks(process, operation);

            for (PartnerLink pl : relatedPLinks) {
                resultItem = new ResultItem(this, ResultType.WARNING, pl, message);
                getResultItems().add(resultItem);
            }
        }
    }
    
    private List<PartnerLink> lookForPLinks(Process process, Operation operation) {
        List<PartnerLink> result = new ArrayList<PartnerLink>();
        PartnerLinkContainer plc = process.getPartnerLinkContainer();

        if (plc != null) {
            for (PartnerLink pl : plc.getPartnerLinks()) {
                WSDLReference<Role> myRoleRef = pl.getMyRole();
                if (myRoleRef != null) {
                    Role myRole = myRoleRef.get();
                    if (myRole != null) {
                        NamedComponentReference<PortType> portTypeRef = 
                                myRole.getPortType();
                        if (portTypeRef != null) {
                            PortType portType = portTypeRef.get();
                            if (portType != null) {
                                Collection<Operation> operations = 
                                        portType.getOperations();
                                if (operations != null && operations.contains(operation)) {
                                    result.add(pl);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void addDoubleResultItem(ResultType resultType, Component comp1, Component comp2, String message) {
        ResultItem resultItem;
        resultItem = new ResultItem(this, resultType, comp1, message);
        getResultItems().add(resultItem);
        resultItem = new ResultItem(this, resultType, comp2, message);
        getResultItems().add(resultItem);
    }

    @Override
    public void visit(Import imp) {
        Model model = getModel(imp);

        if (model == null) {
            addInvalidImportModelError(imp);
            return;
        }
        validate(model);
    }

    private Model getModel(Import imp) {
        Model model = ImportHelper.getWsdlModel(imp, false);

        if (model != null) {
            return model;
        }
        return ImportHelper.getSchemaModel(imp, false);
    }

    private void addInvalidImportModelError(BpelEntity bpelEntity) {
        ResultItem resultItem = new ResultItem(this, ResultType.WARNING, bpelEntity, i18n(getClass(), "FIX_NotWellFormedImport")); // NOI18N
        getResultItems().add(resultItem);
    }

    private HashMap<Operation, List<BpelEntity>> operationToImaListMap = new HashMap<Operation, List<BpelEntity>>();
}
