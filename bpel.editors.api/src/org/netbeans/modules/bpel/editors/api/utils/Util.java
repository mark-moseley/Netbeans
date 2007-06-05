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
package org.netbeans.modules.bpel.editors.api.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightGroup;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants;
import org.netbeans.modules.bpel.editors.api.nodes.FactoryAccess;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * 
 * @author vb160295
 * @version 1.0
 */
public class Util {
    
    private static Map<Class<? extends Component>, NodeType> ENTITY_NODETYPE_MAP;
    
    static {
        ENTITY_NODETYPE_MAP = new HashMap<Class<? extends Component>, NodeType>();
        
        ENTITY_NODETYPE_MAP.put(Assign.class, NodeType.ASSIGN);

        ENTITY_NODETYPE_MAP.put(BooleanExpr.class, NodeType.BOOLEAN_EXPR);

        ENTITY_NODETYPE_MAP.put(Catch.class, NodeType.CATCH);
        ENTITY_NODETYPE_MAP.put(CompensatableActivityHolder.class, NodeType.CATCH_ALL);
        ENTITY_NODETYPE_MAP.put(CompensationHandler.class, NodeType.COMPENSATION_HANDLER);
        ENTITY_NODETYPE_MAP.put(Compensate.class, NodeType.COMPENSATE);
        ENTITY_NODETYPE_MAP.put(CompensateScope.class, NodeType.COMPENSATE_SCOPE);
        ENTITY_NODETYPE_MAP.put(CompletionCondition.class
                , NodeType.COMPLETION_CONDITION);
        ENTITY_NODETYPE_MAP.put(Copy.class, NodeType.COPY);
        ENTITY_NODETYPE_MAP.put(Correlation.class, NodeType.CORRELATION);
        ENTITY_NODETYPE_MAP.put(CorrelationSet.class, NodeType.CORRELATION_SET);
        ENTITY_NODETYPE_MAP.put(CorrelationSetContainer.class
                , NodeType.CORRELATION_SET_CONTAINER);

        ENTITY_NODETYPE_MAP.put(Else.class, NodeType.ELSE);
        ENTITY_NODETYPE_MAP.put(ElseIf.class, NodeType.ELSE_IF);
        ENTITY_NODETYPE_MAP.put(Empty.class, NodeType.EMPTY);
        ENTITY_NODETYPE_MAP.put(EventHandlers.class, NodeType.EVENT_HANDLERS);
        ENTITY_NODETYPE_MAP.put(Exit.class, NodeType.EXIT);

        ENTITY_NODETYPE_MAP.put(FaultHandlers.class, NodeType.FAULT_HANDLERS);
        ENTITY_NODETYPE_MAP.put(Flow.class, NodeType.FLOW);
        ENTITY_NODETYPE_MAP.put(ForEach.class, NodeType.FOR_EACH);
        ENTITY_NODETYPE_MAP.put(From.class, NodeType.FROM);
        ENTITY_NODETYPE_MAP.put(FromPart.class, NodeType.FROM_PART);

        ENTITY_NODETYPE_MAP.put(If.class, NodeType.IF);
        ENTITY_NODETYPE_MAP.put(Import.class, NodeType.IMPORT);
        ENTITY_NODETYPE_MAP.put(Invoke.class, NodeType.INVOKE);

        ENTITY_NODETYPE_MAP.put(MessageExchange.class
                , NodeType.MESSAGE_EXCHANGE);
        ENTITY_NODETYPE_MAP.put(MessageExchangeContainer.class
                , NodeType.MESSAGE_EXCHANGE_CONTAINER);

        ENTITY_NODETYPE_MAP.put(OnAlarmEvent.class, NodeType.ALARM_EVENT_HANDLER);
        ENTITY_NODETYPE_MAP.put(OnAlarmPick.class, NodeType.ALARM_HANDLER);
        ENTITY_NODETYPE_MAP.put(OnEvent.class, NodeType.ON_EVENT);
        ENTITY_NODETYPE_MAP.put(OnMessage.class, NodeType.MESSAGE_HANDLER);

        ENTITY_NODETYPE_MAP.put(PartnerLink.class, NodeType.PARTNER_LINK);
        ENTITY_NODETYPE_MAP.put(PatternedCorrelation.class, NodeType.CORRELATION_P);
        ENTITY_NODETYPE_MAP.put(Pick.class, NodeType.PICK);
        ENTITY_NODETYPE_MAP.put(Process.class, NodeType.PROCESS);

        ENTITY_NODETYPE_MAP.put(Receive.class, NodeType.RECEIVE);
        ENTITY_NODETYPE_MAP.put(RepeatUntil.class, NodeType.REPEAT_UNTIL);
        ENTITY_NODETYPE_MAP.put(Reply.class, NodeType.REPLY);

        ENTITY_NODETYPE_MAP.put(Scope.class, NodeType.SCOPE);
        ENTITY_NODETYPE_MAP.put(Sequence.class, NodeType.SEQUENCE);
        
        ENTITY_NODETYPE_MAP.put(TerminationHandler.class, NodeType.TERMINATION_HANDLER);
        ENTITY_NODETYPE_MAP.put(Throw.class, NodeType.THROW);
        ENTITY_NODETYPE_MAP.put(To.class, NodeType.TO);
        ENTITY_NODETYPE_MAP.put(ToPart.class, NodeType.TO_PART);

        ENTITY_NODETYPE_MAP.put(Wait.class, NodeType.WAIT);
        ENTITY_NODETYPE_MAP.put(While.class, NodeType.WHILE);

        ENTITY_NODETYPE_MAP.put(Variable.class, NodeType.VARIABLE);
        ENTITY_NODETYPE_MAP.put(VariableContainer.class, NodeType.VARIABLE_CONTAINER);
    }
    
    
    private Util() {
    }
    
    /**
     * 
     * @param entityClass class which represents primary entity interface
     * @return generally used NodeType
     */
    public static NodeType getBasicNodeType(Class<? extends Component> entityClass) {
        NodeType entityType = ENTITY_NODETYPE_MAP.get(entityClass);
        return entityType == null ? NodeType.UNKNOWN_TYPE : entityType;
    }

    public static boolean isNavigatorShowableNodeType(NodeType nodeType) {
        if (nodeType == null) {
            return false;
        }
        boolean isShowable = true;
        switch (nodeType) {
        case UNKNOWN_TYPE:
        case DEFAULT_BPEL_ENTITY_NODE:
        case BOOLEAN_EXPR:
            isShowable = false;
            break;
        default:
            isShowable = true;
        }
        
        return isShowable;
    }
    
    /**
     * 
     * @param entity 
     * @param lookup 
     * @return the closest node
     */
    public static Node getClosestNavigatorNode(
            BpelEntity entity, 
            Lookup lookup) 
    {
        if (entity == null || entity.getElementType() == null) {
            return null;
        }
        Node basicNode = null;
        BpelEntity curEntity = entity;
        NodeType basicNodeType = getBasicNodeType(entity);

        while (!isNavigatorShowableNodeType(basicNodeType)
                
                && curEntity != null )
        {
            curEntity = curEntity.getParent();
            basicNodeType = getBasicNodeType(curEntity);
        }
        
        if (curEntity != null 
                && !(NodeType.UNKNOWN_TYPE.equals(basicNodeType))) 
        {
            basicNode = FactoryAccess.getPropertyNodeFactory().
                    createNode(basicNodeType, curEntity, lookup);
        }
        return basicNode;
    }

    // vlv
    public static Component getRoot(Model model) {
      if (model instanceof BpelModel) {
        return ((BpelModel) model).getProcess();
      }
      if (model instanceof SchemaModel) {
        return ((SchemaModel) model).getSchema();
      }
      if (model instanceof WSDLModel) {
        return ((WSDLModel) model).getDefinitions();
      }
      return null;
    }

    /**
     * This method don't aware about bpelModel lock
     * @param entity BpelEntity 
     * @return generally used NodeType, if entity or 
     *  enity#getElementType is null then return null
     */
    public static NodeType getBasicNodeType(Component component) {
        if ( !(component instanceof BpelEntity)) {
            // todo m
            return null;
        }
        BpelEntity bpelEntity = (BpelEntity) component;

        if (bpelEntity == null || bpelEntity.getElementType() == null) {
            return null;
        }
        return getBasicNodeType(bpelEntity.getElementType());
    }
    
    public static void goToSource(Component component) {
        if ( !(component instanceof DocumentComponent)) {
            return;
        }
        DocumentComponent document = (DocumentComponent) component;
        FileObject fo = getFileObjectByModel(component.getModel());

        if (fo == null) {
            return;
        }
        try {
            DataObject d = DataObject.find(fo);
            LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            int lineNum = getLineNum(document);
            if (lineNum < 0) {
                return;
            }
            
            final Line l = lc.getLineSet().getCurrent(lineNum);
            final int column = getColumnNum(document);
            if (column < 0) {
                return;
            }
            
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(Line.SHOW_GOTO, column);
                    openActiveSourceEditor();
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
    }

    public static void goToDesign(final Component component) {
        // vlv
        if ( !(component instanceof BpelEntity)) {
          HighlightManager manager = HighlightManager.getDefault();
          List<HighlightGroup> groups = manager.getHighlightGroups(HighlightGroup.SEARCH);

          if (groups != null) {
            for (HighlightGroup group : groups) {
              manager.removeHighlightGroup(group);
            }
          }
          HighlightGroup group = new HighlightGroup(HighlightGroup.SEARCH);
          Highlight highlight = new Highlight(component, Highlight.SEARCH_RESULT);
          group.addHighlight(highlight);
          manager.addHighlightGroup(group);
          return;
        }
        final BpelEntity bpelEntity = (BpelEntity) component;
        FileObject fo = getFileObjectByModel(bpelEntity.getBpelModel());

        if (fo == null) {
            return;                                                        }
        try {
            DataObject d = DataObject.find(fo);
            final Lookup lookup = d instanceof Lookup.Provider
                    ? ((Lookup.Provider)d).getLookup()
                    : null;
            
            final EditCookie ec = (EditCookie) d.getCookie(EditCookie.class);
            if (ec == null) {
                return;
            }
            
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ec.edit();
                    openActiveDesignEditor();
                    if (lookup != null || bpelEntity != null) {
                        NodeType nodeType = getBasicNodeType(bpelEntity);
                        if (nodeType == null || NodeType.UNKNOWN_TYPE.equals(nodeType)) {
                            return;
                        }
                        Node bpelNode = FactoryAccess.getPropertyNodeFactory()
                        .createNode(nodeType,bpelEntity, lookup);
                        //TODO m
                        TopComponent designTc = WindowManager.getDefault().getRegistry().getActivated();
                        if (designTc != null) {
                            designTc.setActivatedNodes(new Node[0]);
                            designTc.setActivatedNodes(new Node[] {bpelNode});
                        }
                    }
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
    }

    public static FileObject getFileObjectByModel(Model model) {
        if (model != null){
            
            ModelSource src = model.getModelSource();
            if (src != null){
                
                Lookup lookup = src.getLookup();
                if (lookup != null){
                    return (FileObject) lookup.lookup(FileObject.class);
                }
            }
        }
        return null;
    }

    // TODO r|m
    public static String getTextForBpelEntity(final Component comp){
        BpelEntity entity = null;
        if (comp instanceof BpelEntity){
            entity = BpelEntity.class.cast(comp);
        }
        if (entity == null) {
            return ""; // NOI18N
        }
        
        FileObject fo = getFileObjectByModel(entity.getBpelModel());
        if (fo == null) {
            return ""; // NOI18N
        }

        // TODO - if the line doesn't contain the target (query component name) string, keep searcing subsequent lines
        
        
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        
        int line = getLineNum(entity);
        int col = getColumnNum(entity);
        ModelSource modelSource = entity.getBpelModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument)lookup.lookup(StyledDocument.class);
        if (document == null) {
            return ""; // NOI18N
        }
            
        CloneableEditorSupport editor = (CloneableEditorSupport)dobj.getCookie(org.openide.cookies.EditorCookie.class);
        Line.Set s =editor.getLineSet();
            
        Line xmlLine = s.getCurrent(line);
        String nodeLabel =   xmlLine.getText().trim();
        // substitute xml angle brackets <> for &lt; and &gt;
        Pattern lt = Pattern.compile("<"); //NOI18N
        Matcher mlt = lt.matcher(nodeLabel);
        nodeLabel = mlt.replaceAll("&lt;");  //NOI18N
        Pattern gt = Pattern.compile(">"); //NOI18N
        Matcher mgt = gt.matcher(nodeLabel);
        nodeLabel = mgt.replaceAll("&gt;");  //NOI18N
        return boldenRefOrType(nodeLabel);
    }

    // private methods
    private static int getLineNum(DocumentComponent entity) {
        int position = entity.findPosition();
        ModelSource modelSource = entity.getModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument)lookup.lookup(StyledDocument.class);
        if (document == null) {
            return -1;
        }
        return NbDocument.findLineNumber(document,position);
    }
    
    private static int getColumnNum(DocumentComponent entity) {
        int position = entity.findPosition();
        ModelSource modelSource = entity.getModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument)lookup.lookup(StyledDocument.class);
        if (document == null) {
            return -1;
        }
        return NbDocument.findLineColumn(document,position);
    }

    private static void openActiveDesignEditor() {
        openActiveMVEditor(BpelEditorConstants.BPEL_DESIGNMV_PREFFERED_ID);
    }

    private static void openActiveSourceEditor() {
        openActiveMVEditor(BpelEditorConstants.BPEL_SOURCEMV_PREFFERED_ID);
    }

    private static void openActiveMVEditor(String mvPreferedID) {
        if (mvPreferedID == null) {
            return;
        }
        
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        if (mvh == null) {
            return;
        }

        MultiViewPerspective[] mvps = mvh.getPerspectives();
        if (mvps != null && mvps.length >0) {
            for (MultiViewPerspective mvp : mvps) {
                if (mvp.preferredID().equals(mvPreferedID)) {  // NOI18N
                    mvh.requestVisible(mvp);
                    mvh.requestActive(mvp);
                }
            }
        }
    }
    
    // TODO get xml snippet for line that contains the
    //  query component name
    /**
     * If the label contains ref= or type=
     * the substring containing the named portion of the attribute
     * will be surrounded with html bold tags
     * e.g.,
     * input param <xsd:element ref="comment" minOccurs="0"/>
     * return <xsd:element ref="<b>comment</b>" minOccurs="0"/>
     *
     *
     */
    private static String boldenRefOrType(String label){
        // find index of type or ref
        // find 1st occurence of " from index
        // find 1st occurence of : after ", if any
        // insert <b>
        // find closing "
        // insert </b>
        int it = label.indexOf(" type"); //NOI18N
        if (it < 0){
            it = label.indexOf(" ref"); //NOI18N
        }
        if (it < 0){
            // no type or ref found
            return label;
        }
        int iq1 = label.indexOf('"',it);
        if (iq1 < it){
            // no begin quote
            return label;
        }
        int ic = label.indexOf(':',iq1);
        if (ic < iq1){
            // no colon
        }
        int iq2 = label.indexOf('"', iq1+1);
        if (iq2 < iq1 || ic > iq2){
            // couldn't find closing quote for tag
            return label;
        }
        int ib1 = -1;
        if (ic > -1){
            ib1 = ic+1;
        } else {
            ib1 = iq1+1;
        }
        StringBuffer l = new StringBuffer(label);
        l.insert(ib1,"<b>");
        // the close quote has now been pushed right 3 spaces
        l.insert(iq2+3,"</b>");
        return l.toString();
        
    }
    
    
    public static void goToReferenceSource(Reference<Referenceable> reference) {
        Referenceable referenceable = reference.get();
        if (referenceable == null) return;
        if (!(referenceable instanceof DocumentComponent)) return;
        goToDocumentComponentSource((DocumentComponent<DocumentComponent>) referenceable);
    }
    
    
    public static boolean canGoToDocumentComponentSource(
            DocumentComponent<DocumentComponent> component)
    {
        if (component == null) return false;
        
        Model model = component.getModel();
        if (model == null) return false;

        ModelSource modelSource = model.getModelSource();
        if (modelSource == null) return false;

        Lookup lookup = modelSource.getLookup();
        if (lookup == null) return false;
        
        FileObject fileObject = (FileObject) lookup.lookup(FileObject.class);
        if (fileObject == null) return false;
            
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {}
        if (dataObject == null) return false;
        
        LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie
            .class);
        if (lineCookie == null) return false;
        
        Line.Set lineSet = lineCookie.getLineSet();
        if (lineSet == null) return false;
        
        StyledDocument document = (StyledDocument) lookup.lookup(StyledDocument
                .class);
        if (document == null) return false;
        
        Line line = null;
        int column = 0;
        
        try {
            int pos = component.findPosition();
            line = lineSet.getCurrent(NbDocument.findLineNumber(document, pos));
            column = NbDocument.findLineColumn(document, pos);
        } catch (IndexOutOfBoundsException e) {}
            
        if (line == null) {
            try {
                line = lineCookie.getLineSet().getCurrent(0);
            } catch (IndexOutOfBoundsException e) {}
        }
        if (line == null) return false;
        
        return true;
    }
            
    
    
    public static void goToDocumentComponentSource(
            DocumentComponent<DocumentComponent> component) 
    {
        if (component == null) return;
        
        Model model = component.getModel();
        if (model == null) return;

        ModelSource modelSource = model.getModelSource();
        if (modelSource == null) return;

        Lookup lookup = modelSource.getLookup();
        if (lookup == null) return;
        
        FileObject fileObject = (FileObject) lookup.lookup(FileObject.class);
        if (fileObject == null) return;
            
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {}
        if (dataObject == null) return;
        
        LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie
            .class);
        if (lineCookie == null) return;
        
        Line.Set lineSet = lineCookie.getLineSet();
        if (lineSet == null) return;
        
        StyledDocument document = (StyledDocument) lookup.lookup(StyledDocument
                .class);
        if (document == null) return;
        
        Line line = null;
        int column = 0;
        
        try {
            int pos = component.findPosition();
            line = lineSet.getCurrent(NbDocument.findLineNumber(document, pos));
            column = NbDocument.findLineColumn(document, pos);
        } catch (IndexOutOfBoundsException e) {}
            
        if (line == null) {
            try {
                line = lineCookie.getLineSet().getCurrent(0);
            } catch (IndexOutOfBoundsException e) {}
        }
        if (line == null) return;
        
        final Line fLine = line;
        final int fColumn = column;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fLine.show(Line.SHOW_GOTO, fColumn);
            }
        });
    }

    public static int getChildIndex(BpelEntity child, CompositeActivity parent) {
        assert child != null && parent != null;
        int childIndex = -1;
        for (int i = 0; i < parent.sizeOfActivities(); i++) {
            if (child.equals(parent.getActivity(i))) {
                childIndex = i;
                break;
            }
        }
        return childIndex;
    }

    public static final String getCorrectedHtmlRenderedString(String htmlString) {
        if (htmlString == null) {
            return null;
        }
        htmlString = htmlString.replaceAll("&amp;","&"); // NOI18n
        htmlString = htmlString.replaceAll("&gt;",">;"); // NOI18n
        htmlString = htmlString.replaceAll("&lt;","<"); // NOI18n
        
        htmlString = htmlString.replaceAll("&","&amp;"); // NOI18n
        htmlString = htmlString.replaceAll(">","&gt;"); // NOI18n
        htmlString = htmlString.replaceAll("<","&lt;"); // NOI18n
        return htmlString;
    }

    public static String getTagName(DocumentComponent component ) {
        if (component == null) {
            return null;
        }
        
        Element enEl = component.getPeer();
        return enEl == null ? null : enEl.getTagName();
    }
    
}
