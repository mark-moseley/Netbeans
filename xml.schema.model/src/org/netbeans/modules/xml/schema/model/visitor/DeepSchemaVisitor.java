/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.*;

/**
 * This visitor will visit all nodes in a tree. A typical subclass visitor
 * would inherit from this class and override methods for elements which need
 * to be visited. This would remove the reliance on the heirarchy for visiting. 
 * @author Chris Webster
 */
public class DeepSchemaVisitor implements SchemaVisitor {
    public void visit(LocalSimpleType type) {
        visitChildren(type);
    }

    public void visit(Union u) {
        visitChildren(u);
    }

    public void visit(AllElement allElement) {
        visitChildren(allElement);
    }
    
    public void visit(AllElementReference allElement) {
        visitChildren(allElement);
    }

    public void visit(GroupAll ga) {
        visitChildren(ga);
    }

    public void visit(Enumeration e) {
        visitChildren(e);
    }

    public void visit(AttributeGroupReference agr) {
        visitChildren(agr);
    }

    public void visit(GlobalAttributeGroup gag) {
        visitChildren(gag);
    }

    public void visit(KeyRef kr) {
        visitChildren(kr);
    }

    public void visit(GlobalSimpleType gst) {
        visitChildren(gst);
    }

    public void visit(AnyElement any) {
        visitChildren(any);
    }

    public void visit(Include include) {
        visitChildren(include);
    }

    public void visit(MinInclusive mi) {
        visitChildren(mi);
    }

    public void visit(Import im) {
        visitChildren(im);
    }

    public void visit(GroupSequence gs) {
        visitChildren(gs);
    }

    public void visit(Choice choice) {
        visitChildren(choice);
    }

    public void visit(Unique u) {
        visitChildren(u);
    }

    public void visit(MaxLength ml) {
        visitChildren(ml);
    }

    public void visit(Redefine rd) {
        visitChildren(rd);
    }

    public void visit(SimpleContentRestriction scr) {
        visitChildren(scr);
    }

    public void visit(LocalElement le) {
        visitChildren(le);
    }
    
    public void visit(ElementReference le) {
        visitChildren(le);
    }

    public void visit(Selector s) {
        visitChildren(s);
    }

    public void visit(Annotation ann) {
        visitChildren(ann);
    }

    public void visit(ComplexExtension ce) {
        visitChildren(ce);
    }

    public void visit(FractionDigits fd) {
        visitChildren(fd);
    }

    public void visit(SimpleExtension se) {
        visitChildren(se);
    }

    public void visit(Whitespace ws) {
        visitChildren(ws);
    }

    public void visit(LocalComplexType type) {
        visitChildren(type);
    }

    public void visit(TotalDigits td) {
        visitChildren(td);
    }

    public void visit(MaxExclusive me) {
        visitChildren(me);
    }

    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }

    public void visit(AnyAttribute anyAttr) {
        visitChildren(anyAttr);
    }

    public void visit(GlobalAttribute ga) {
        visitChildren(ga);
    }

    public void visit(All all) {
        visitChildren(all);
    }

    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
    }

    public void visit(GroupReference gr) {
        visitChildren(gr);
    }

    public void visit(Key key) {
        visitChildren(key);
    }

    public void visit(GroupChoice gc) {
        visitChildren(gc);
    }

    public void visit(List l) {
        visitChildren(l);
    }

    public void visit(Pattern p) {
        visitChildren(p);
    }

    public void visit(Documentation d) {
        visitChildren(d);
    }

    public void visit(AppInfo d) {
        visitChildren(d);
    }

    public void visit(Notation notation) {
        visitChildren(notation);
    }

    public void visit(MinExclusive me) {
        visitChildren(me);
    }

    public void visit(GlobalGroup gd) {
        visitChildren(gd);
    }

    public void visit(MinLength ml) {
        visitChildren(ml);
    }

    public void visit(Schema s) {
        visitChildren(s);
    }

    public void visit(GlobalComplexType gct) {
        visitChildren(gct);
    }

    public void visit(Sequence s) {
        visitChildren(s);
    }

    public void visit(MaxInclusive mi) {
        visitChildren(mi);
    }

    public void visit(SimpleTypeRestriction str) {
        visitChildren(str);
    }

    public void visit(LocalAttribute la) {
        visitChildren(la);
    }
    
    public void visit(AttributeReference la) {
        visitChildren(la);
    }

    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }

    public void visit(GlobalElement ge) {
        visitChildren(ge);
    }

    public void visit(Length length) {
        visitChildren(length);
    }

    public void visit(Field f) {
        visitChildren(f);
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
        }
    }
}
