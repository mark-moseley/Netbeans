/*
 * PUBLIC DOMAIN PCCTS-BASED C++ GRAMMAR (cplusplus.g, stat.g, expr.g)
 *
 * Authors: Sumana Srinivasan, NeXT Inc.;            sumana_srinivasan@next.com
 *          Terence Parr, Parr Research Corporation; parrt@parr-research.com
 *          Russell Quong, Purdue University;        quong@ecn.purdue.edu
 *
 * VERSION 1.1
 *
 * SOFTWARE RIGHTS
 *
 * This file is a part of the ANTLR-based C++ grammar and is free
 * software.  We do not reserve any LEGAL rights to its use or
 * distribution, but you may NOT claim ownership or authorship of this
 * grammar or support code.  An individual or company may otherwise do
 * whatever they wish with the grammar distributed herewith including the
 * incorporation of the grammar or the output generated by ANTLR into
 * commerical software.  You may redistribute in source or binary form
 * without payment of royalties to us as long as this header remains
 * in all source distributions.
 *
 * We encourage users to develop parsers/tools using this grammar.
 * In return, we ask that credit is given to us for developing this
 * grammar.  By "credit", we mean that if you incorporate our grammar or
 * the generated code into one of your programs (commercial product,
 * research project, or otherwise) that you acknowledge this fact in the
 * documentation, research report, etc....  In addition, you should say nice
 * things about us at every opportunity.
 *
 * As long as these guidelines are kept, we expect to continue enhancing
 * this grammar.  Feel free to send us enhancements, fixes, bug reports,
 * suggestions, or general words of encouragement at parrt@parr-research.com.
 * 
 * NeXT Computer Inc.
 * 900 Chesapeake Dr.
 * Redwood City, CA 94555
 * 12/02/1994
 * 
 * Restructured for public consumption by Terence Parr late February, 1995.
 *
 * DISCLAIMER: we make no guarantees that this grammar works, makes sense,
 *             or can be used to do anything useful.
 */
/*
 * 2001-2003 Version 2.0 September 2003
 *
 * Some modifications were made to this file to support a project by
 * Jianguo Zuo and David Wigg at
 * The Centre for Systems and Software Engineering
 * South Bank University
 * London, UK.
 * wiggjd@bcs.ac.uk
 * blackse@lsbu.ac.uk
*/
/* 2003-2004 Version 3.0 July 2004
 * Modified by David Wigg at London South Bank University for CPP_parser.g
 *
 * See MyReadMe.txt for further information
 *
 * This file is best viewed in courier font with tabs set to 4 spaces
 */

/* 2005
 * Some modifications were made by Gordon Prieur (Gordon.Prieur@sun.com);
 * after that the grammar was ported to Java by Vladimir Kvashin (Vladimir.Kvashin@sun.com)
 *
 * NOCDDL
 */



package org.netbeans.modules.cnd.modelimpl.parser;

import antlr.*;
import antlr.collections.AST;
import java.util.Hashtable;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;

/**
 * Extends CPPParser to implement logic, ported from Support.cpp.
 *
 * In the original some methods implementations reside in a separate 
 * file - Support.cpp.
 *
 * Since in Java I can't split class definition between several files,
 * I decided to use CPPParser for this purpose.
 */
public class CPPParserEx extends CPPParser {
    
    private boolean lazyCompound = TraceFlags.EXCLUDE_COMPOUND;

    private static class AstFactoryEx extends antlr.ASTFactory {
	
	public AstFactoryEx(Hashtable tokenTypeToClassMap) {
	    super(tokenTypeToClassMap);
	}

        @Override
        public final AST create(Token tok) {
            AST t = createTokenASTByType(tok.getType());
            if (t == null) {
                t = new CsmAST();
            }
           t.initialize(tok);
            return t;
        }
    }
    
    //Change statementTrace from cppparser.g directly 
    //private final boolean trace = Boolean.getBoolean("cnd.parser.trace");
    //private TokenStreamSelector selector = new TokenStreamSelector();
    
    protected CPPParserEx(TokenStream stream) {
        super(stream);
    }    
    
    @Override
    protected final boolean isLazyCompound() {
        return lazyCompound;
    }
    
    public final void setLazyCompound(boolean lazy) {
        this.lazyCompound = lazy;
    }
    
    public static CPPParserEx getInstance(String file, TokenStream ts, int flags) {
        assert (ts != null);
        assert (file != null);   
        CPPParserEx parser = new CPPParserEx(ts);
        parser.init(file, flags);
        return parser;            

    }    

    
    @Override
    protected final void init(String filename, int flags) {
        /*if( trace ) {
            flags |= CPPParser.CPP_STATEMENT_TRACE;
        }*/
	setASTFactory(new AstFactoryEx(getTokenTypeToASTClassMap()));
	getASTFactory().setASTNodeClass(CsmAST.class);
        super.init(filename, flags);
    }
    
    private static int strcmp(String s1, String s2) {
        return (s1 == null) ? ( s2 == null ? 0 : -1) : s1.compareTo(s2);
    }
    
    // Shorthand for a string of (qualifiedItemIs()==xxx||...)
    @Override
    protected final boolean qualifiedItemIsOneOf(/*QualifiedItem*/int qiFlags, int lookahead_offset) { 
	/*QualifiedItem*/int qi = qualifiedItemIs(lookahead_offset); 
	return (qi & qiFlags) != 0; 
    }
    
    
    // This is an important function, but will be replaced with
    // an enhanced predicate in the future, once predicates
    // and/or predicate guards can contain loops.
    //
    // Scan past the ::T::B:: to see what lies beyond.
    // Return qiType if the qualified item can pose as type name.
    // Note that T::T is NOT a type; it is a constructor.  Also,
    // class T { ... T...} yields the enclosed T as a ctor.  This
    // is probably a type as I separate out the constructor defs/decls,
    // I want it consistent with T::T.
    //
    // In the below examples, I use A,B,T and example types, and
    // a,b as example ids.
    // In the below examples, any A or B may be a
    // qualified template, i.e.,  A<...>
    //
    // T::T outside of class T yields qiCtor.
    // T<...>::T outside of class T yields qiCtor.
    // T inside of class T {...} yields qiCtor.
    // T, ::T, A::T outside of class T yields qiType.
    // a, ::a,  A::B::a yields qiId
    // a::b yields qiInvalid
    // ::operator, operator, A::B::operator yield qiOPerator
    // A::*, A::B::* yield qiPtrMember
    // ::*, * yields qiInvalid
    // ::~T, ~T, A::~T yield qiDtor
    // ~a, ~A::a, A::~T::, ~T:: yield qiInvalid
    @Override
    protected final /*QualifiedItem*/int qualifiedItemIs(int lookahead_offset) { 
        try {
            return _qualifiedItemIs(lookahead_offset);
        }
        catch( TokenStreamException e ) {
            reportError(e.getMessage());
            return qiInvalid;
        }
    }
        
    protected final /*QualifiedItem*/int _qualifiedItemIs(int lookahead_offset) throws TokenStreamException { 
        
	int tmp_k = lookahead_offset + 1;
        
	int final_type_idx = 0;
	boolean scope_found = false;
	// Skip leading "::"
	if (LT(tmp_k).getType() == SCOPE) {
	    tmp_k++;
	    scope_found = true;
	}

	// Skip sequences of T:: or T<...>::
	//printf("qualifiedItemIs{%d]: tmp_k %d, getType: %d isTypeName: %d, "
	//	"isClassName: %d, guessing %d\n", LT(tmp_k).getLine(),
	//	tmp_k,LT(tmp_k).getType(),isTypeName((LT(tmp_k).getText()).data()),
	//	isClassName((LT(tmp_k).getText()).data()),inputState.guessing);
	while (LT(tmp_k).getType() == ID && isTypeName((LT(tmp_k).getText()))) {
	    // If this type is the same as the last type, then ctor
	    if (final_type_idx != 0 && strcmp((LT(final_type_idx).getText()),
			(LT(tmp_k).getText())) == 0) {// Like T::T
		// As an extra check, do not allow T::T::
		if (LT(tmp_k+1).getType() == SCOPE) {
		    //printf("support.cpp qualifiedItemIs qiInvalid returned\n");
		    return qiInvalid;
		} else {
		    //printf("support.cpp qualifiedItemIs qiCtor returned\n");
		    return qiCtor;
		}
	    }

	    // Record this as the most recent type seen in the series
	    final_type_idx = tmp_k;
	    
	    //printf("support.cpp qualifiedItemIs if step reached final_type_idx %d\n",
	    //		final_type_idx);
	    
	    // Skip this token
	    tmp_k++;

	    // Skip over any template qualifiers <...>
	    // I believe that "T<..." cannot be anything valid but a template
	    if (LT(tmp_k).getType() == LESSTHAN) {
		if ( (tmp_k = skipTemplateQualifiers(tmp_k)) < 0 ) {
		    //printf("support.cpp qualifiedItemIs qiInvalid(2) returned\n");
		    return qiInvalid;
		}
		//printf("support.cpp qualifiedItemIs template skipped, tmp_k %d\n",tmp_k);
		// tmp_k has been updated to token following <...>
	    }
	    if (LT(tmp_k).getType() == SCOPE) {
		// Skip the "::" and keep going
		tmp_k++; scope_found = true;
	    } else {
		// Series terminated -- last ID in the sequence was a type
		// Return ctor if last type is in containing class
		// We already checked for T::T inside loop
                
                String tmp_str = enclosingClass;                
                { 
                     int tmp_int = enclosingClass.lastIndexOf("::"); // NOI18N
                     if(tmp_int != -1 && tmp_int + 2 < tmp_str.length()) {
                          tmp_str = enclosingClass.substring(tmp_int + 2);
                     }
                }

		if (strcmp(tmp_str, (LT(final_type_idx).getText()))==0) {
		    // Like class T  T()
		    //printf("support.cpp qualifiedItemIs qiCtor(2) returned\n");
		    return qiCtor;
		} else {
		    //printf("support.cpp qualifiedItemIs qiType returned\n");
		    return qiType;
		}
	    }
	}

	// LT(tmp_k) is not an ID, or it is an ID but not a typename.
	//printf("support.cpp qualifiedItemIs second switch reached\n");
	switch (LT(tmp_k).getType()) {
	case ID:
	    // ID but not a typename
	    // Do not allow id::
	    if (LT(tmp_k+1).getType() == SCOPE) {
		//printf("support.cpp qualifiedItemIs qiInvalid(3) returned\n");
		return qiInvalid;
	    }
	    if (strcmp(enclosingClass,(LT(tmp_k).getText()))==0) {
		// Like class T  T()
		//printf("support.cpp qualifiedItemIs qiCtor(3) returned\n");
		return qiCtor;
	    } else {
		if (scope_found) {
		    // DW 25/10/03 Assume type if at least one SCOPE present (test12.i)
		    return qiType;
		} else {
		    //printf("support.cpp qualifiedItemIs qiVar returned\n");
		    return qiVar;	// DW 19/03/04 was qiID Could be function?
		}
	    }
	    //break;

	case TILDE:
	    // check for dtor
	    if (LT(tmp_k+1).getType() == ID && 
		    isTypeName((LT(tmp_k+1).getText())) &&
		    LT(tmp_k+2).getType() != SCOPE) {
		// Like ~B or A::B::~B
		// Also (incorrectly?) matches ::~A.
		//printf("support.cpp qualifiedItemIs qiDtor returned\n");
		return qiDtor;
	    } else {
		// ~a or ~A::a is qiInvalid
		//printf("support.cpp qualifiedItemIs qiInvalid(4) returned\n");
		return qiInvalid;
	    }
	    //break;

	case STAR:
	    // Like A::*
	    // Do not allow * or ::*
	    if (final_type_idx == 0) {
		// Haven't seen a type yet
		//printf("support.cpp qualifiedItemIs qiInvalid(5) returned\n");
		return qiInvalid;
	    } else {
		//printf("support.cpp qualifiedItemIs qiPtrMember returned\n");
		return qiPtrMember;
	    }
	    //break;

	case LITERAL_OPERATOR:
	    // Like A::operator, ::operator, or operator
	    //printf("support.cpp qualifiedItemIs qiOperator returned\n");
	    return qiOperator;

	default:
	    // Something that neither starts with :: or ID, or
	    // a :: not followed by ID, operator, ~, or *
	    //printf("support.cpp qualifiedItemIs qiInvalid(6) returned\n");
	    return qiInvalid;
	}
    }

    // Skip over <...>.  This correctly handles nested <> and (), e.g:
    //    <T>
    //    < (i>3) >
    //    < T2<...> >
    // but not
    //    < i>3 >
    //
    // On input, kInOut is the index of the "<"
    // On output, if the return is true, then 
    //                kInOut is the index of the token after ">"
    //            else
    //                kInOut is unchanged

    // VK: original skipTemplateQualifiers(int& kInOut) passes kInOut BY REF
    // Since kInOut is never negative, I changed it to the following convention:
    // it returns -1 in case of false
    // otherwise new kInOut value
    private int skipTemplateQualifiers(int kInOut) { // Start after "<"
        try {
            return _skipTemplateQualifiers(kInOut);
        }
        catch( TokenStreamException e ) {
            reportError(e.getMessage());
            return -1;
        }
    }
    
    private int _skipTemplateQualifiers(int kInOut) throws TokenStreamException { // Start after "<"
	int tmp_k = kInOut + 1;

	while (LT(tmp_k).getType() != GREATERTHAN) { // scan to end of <...>
	    switch (LT(tmp_k).getType()) {
		case EOF:
			return -1;
		case LESSTHAN:
			if ( (tmp_k = skipTemplateQualifiers(tmp_k)) < 0 ) {
			    return -1;
			}
			break;
		case LPAREN:
			if ( (tmp_k = skipNestedParens(tmp_k)) < 0 ) {
			    return -1;
			}
			break;

		default:
			tmp_k++;     // skip everything else
			break;
	    }
	    if (tmp_k > MaxTemplateTokenScan) {
		return -1;
	    }
	}

	// Update output argument to point past ">"
	kInOut = tmp_k + 1;
	return kInOut;
    }
    
    // Skip over (...).  This correctly handles nested (), e.g:
    //    (i>3, (i>5))
    //
    // On input, kInOut is the index of the "("
    // On output, if the return is true, then 
    //                kInOut is the index of the token after ")"
    //            else
    //                kInOut is unchanged
    
    // VK: original skipNestedParens(int& kInOut) passes kInOut BY REF!
    // Since kInOut is never negative, I changed it to the following convention:
    // it returns -1 in case of false
    // otherwise new kInOut value
    private int skipNestedParens(int kInOut) throws TokenStreamException  {
	int tmp_k = kInOut + 1;

	while (LT(tmp_k).getType() != RPAREN) {   // scan to end of (...)
	    switch (LT(tmp_k).getType()) {
	    case EOF:
		    return -1;

	    case LPAREN:
		    if ( (tmp_k = skipNestedParens(tmp_k)) < 0 ) 
			    {return -1;}
		    break;

	    default:
		    tmp_k++;     // skip everything else
		    break;
	    }
	    if (tmp_k > MaxTemplateTokenScan) {
		return -1;
	    }
	}

	// Update output argument to point past ")"
	kInOut = tmp_k + 1;
	return kInOut;
    }

    // Return true if "::blah" or "fu::bar<args>::..." found.
    @Override
    protected final boolean scopedItem(int tmp_k)  { 
        try {
	//printf("support.cpp scopedItem tmp_k %d\n",tmp_k);
	return (LT(tmp_k).getType()==SCOPE ||
			(LT(tmp_k).getType()==ID && !finalQualifier(tmp_k)));
        } catch( TokenStreamException e ) {
            reportError(e.getMessage());
            return false;
        }
    }
    
    // Return true if ID<...> or ID is last item in qualified item list.
    // Return false if LT(tmp_k) is not an ID.
    // ID must be a type to check for ID<...>,
    // or else we would get confused by "i<3"
    private boolean finalQualifier(int tmp_k) throws TokenStreamException {
	if (LT(tmp_k).getType() == ID) {
	    if (isTypeName((LT(tmp_k).getText())) &&
			LT(tmp_k + 1).getType() == LESSTHAN) {
		// Starts with "T<".  Skip <...>
		tmp_k++;
		tmp_k = skipTemplateQualifiers(tmp_k);
                if (tmp_k == -1) {
                    return true;
                }
	    } else {// skip ID;
		tmp_k++;
	    }
	    //return (LT(tmp_k).getType() != SCOPE);
	    return safeGetType(LT(tmp_k)) != SCOPE;
	} else {// not an ID
	    return false;
	}
    }
    
    private int safeGetType(Token tok) {
	return (tok == null) ? 0 : tok.getType();
    }
  
    @Override
    protected final boolean isTypeName(String s) { 
        return isValidIdentifier(s);
        /* TODO: revive the original code:
	CPPSymbol *cs = (CPPSymbol *) symbols->lookup(s);

	if (cs != NULL && (cs->getType() == CPPSymbol::otTypedef ||
		cs->getType() == CPPSymbol::otEnum ||
		cs->getType() == CPPSymbol::otClass ||
		cs->getType() == CPPSymbol::otStruct ||
		cs->getType() == CPPSymbol::otUnion)) {
	    return 1;
	} else if (cs == NULL && codeFoldingParse && isValidIdentifier(s)) {
	    return 1;
	} else {
	    return 0;
	}
        */
    }

    private final boolean isValidIdentifier(String id) { 
        if( id != null && id.length() > 0 ) {
            if( Character.isJavaIdentifierStart(id.charAt(0)) ) {
                for( int i = 1; i < id.length(); i++ ) {
                    if( ! Character.isJavaIdentifierPart(id.charAt(i)) ) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isClassName(String  s) { 
        return isValidIdentifier(s);
        /* TODO: revive the original code:
	CPPSymbol *cs = (CPPSymbol *) symbols->lookup(s);
	
	if (cs != NULL && (cs->getType() == CPPSymbol::otClass ||
		cs->getType() == CPPSymbol::otStruct ||
		cs->getType() == CPPSymbol::otUnion)) {
	    return 1;
	} else if (cs == NULL && codeFoldingParse && isValidIdentifier(s)) {
	    return 1;
	} else {
	    return 0;
	}
        */
    }
    
    //protected void beginFunctionDefinition() {
        //functionDefinition = 1;
        //super.beginFunctionDefinition();
    //}
    
    //protected void endFunctionDefinition() {
        /* TODO: revive the original code:
	// Remove parameter scope
	symbols->dumpScope(stdout);
	//fprintf(stdout, "endFunctionDefinition remove parameter scope(%d):\n",
	//		    symbols->getCurrentScopeIndex());
	symbols->removeScope();
	symbols->restoreScope();
	//printf("endFunctionDefinition restoreScope() now %d\n",
	//		    symbols->getCurrentScopeIndex());
        */
	//functionDefinition = 0;
        //super.endFunctionDefinition(fi);
    //}
    
    //protected void beginConstructorDefinition() {
        //functionDefinition = 1;
        //super.beginConstructorDefinition();
    //}
    
    //protected void endConstructorDefinition() {
        /* TODO: revive the original code:
	symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
	//printf("endConstructorDefinition restoreScope() now %d\n",
	//		    symbols->getCurrentScopeIndex());
        */
	//functionDefinition = 0;
    //}
    
   // protected void beginDestructorDefinition() {
        //functionDefinition = 1;
        //super.beginConstructorDefinition();
    //}
    
    //protected void endDestructorDefinition() {
        /* TODO: revive the original code:
	symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
	//printf("endDestructorDefinition restoreScope() now %d\n",
	//		    symbols->getCurrentScopeIndex());
         */
	//functionDefinition = 0;
   // }

    @Override
    protected final void declarationSpecifier(boolean td, boolean fd, StorageClass sc, TypeQualifier tq,
                         /*TypeSpecifier*/int ts, DeclSpecifier ds) {
	//printf("support.cpp declarationSpecifier td %d, fd %d, sc %d, tq, "
	//	"%d, ts %d, ds %d, qi %d\n", td,fd,sc,tq,ts,ds,qi);
	_td = td;	// For typedef
	_fd = fd;	// For friend
	_sc = sc;
	_tq = tq;
	_ts = ts;
	_ds = ds;
	//_qi = qi;
    }
    
    /*
     * Symbols from declarators are added to the symbol table here. 
     * The symbol is added to whatever the current scope is in the symbol table. 
     * See list of object types below.
     */
    @Override
    protected final void declaratorID(String id, /*QualifiedItem*/int qi) {	// stores new symbol with type
        
	CPPSymbol c;

	//printf("support.cpp declaratorID line %d %s found, _td = %d, qi = %d\n",
	//		    getLine(1),id,_td,qi);
	if (qi == qiType || _td) {	// Check for type declaration
	    // DW 21/05/04 Check consistency of local qi with global _td with view to
	    // removing test for global _td
	    if (reportOddWarnings && !(qi == qiType && _td)) {
		printf("declaratorID[%d]: Warning qi %d, _td %d inconsistent for %s\n", // NOI18N
			    getLine(1), qi, _td, id);
	    }
	    
	    c = new CPPSymbol(id, CPPSymbol.otTypedef);
            /* TODO: revive the original code:
	    symbols.defineInScope(id, c, externalScope);
             */
	    if(statementTrace >= 2) {
		    printf("declaratorID[%d]: Declare %s in external scope 1, " + // NOI18N
			    "ObjectType %d\n", getLine(1), id, c.getType()); // NOI18N
	    }
	    // DW 04/08/03 Scoping not fully implemented
	    // Typedefs all recorded in 'external' scope and therefor never removed
	} else if (qi == qiFun) {	// For function declaration
	    c = new CPPSymbol(id, CPPSymbol.otFunction);
            /* TODO: revive the original code:
	    symbols.define(id, c);	// Add to current scope
             */
	    if(statementTrace >= 2) {
		printf("declaratorID[%d]: Declare %s in current scope %d, " + // NOI18N
			    "ObjectType %d\n", getLine(1), id, // NOI18N
			    /*symbols.getCurrentScopeIndex()*/0, c.getType());
	    }
	} else {
	    if (reportOddWarnings && qi != qiVar) {
		printf("declaratorID[%d]: Warning qi (%d) not qiVar (%d) for %s\n", // NOI18N
			    getLine(1), qi, qiVar, id); 
	    }

	    // Used to leave otInvalid DW 02/07/03 Think this should be otVariable
	    c = new CPPSymbol(id, CPPSymbol.otVariable);
            /* TODO: revive the original code:
	    symbols.define(id, c);	// Add to current scope
             */
	    if(statementTrace >= 2) {
		    printf("declaratorID[%d]: Declare %s in current scope %d, " + // NOI18N
			    "ObjectType %d\n", getLine(1), // NOI18N
			    id, /*symbols.getCurrentScopeIndex()*/0, c.getType());
	    }
	}
    }
    
    //protected void declaratorParameterList(boolean def) {
        /* TODO: revive the original code:
	symbols->saveScope();
         */
	//printf("declaratorParameterList saveScope() now %d\n",
	//			symbols->getCurrentScopeIndex());
    //}
    
    //protected void declaratorEndParameterList(boolean def) {
        /* TODO: revive the original code:
	if (!def) {
	    symbols->dumpScope(stdout);
	    symbols->removeScope();
	    symbols->restoreScope();
	    //printf("declaratorEndParameterList restoreScope() now %d\n",
	    //			symbols->getCurrentScopeIndex());
	}
         */
    //}
    
    //protected void functionParameterList() {
        /* TODO: revive the original code:
	symbols->saveScope();
         */
	//printf("functionParameterList saveScope() now %d\n",symbols->getCurrentScopeIndex());
	// DW 25/3/97 change flag from function to parameter list
	//functionDefinition = 2;
    //}
    
    /*protected void functionEndParameterList(boolean def) {
	// If this parameter list is not in a definition then this
	if (!def) {
            /* TODO: revive the original code:
	    symbols->dumpScope(stdout);
	    symbols->removeScope();
	    symbols->restoreScope();
             */
	    //printf("functionEndParameterList restoreScope() now %d\n",
	    //		symbols->getCurrentScopeIndex());
	/*} else {
	    // Change flag from parameter list to body of definition
	    functionDefinition = 3;   
	}
	/* Otherwise endFunctionDefinition removes the parameters from scope */
    //}
    
    /*protected void enterNewLocalScope() {
    	//blockDepth++;
        /* TODO: revive the original code:
	symbols->saveScope();
         */
	//printf("enterNewLocalScope saveScope() now %d\n",symbols->getCurrentScopeIndex());
//}
    
    //protected void exitLocalScope() {
	//blockDepth--;
        /* TODO: revive the original code:
	symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
         */
	//printf("exitLocalScope restoreScope() now %d\n",symbols->getCurrentScopeIndex());
    //}
    
    //protected void enterExternalScope() {
	// Scope has been initialised to 1 in CPPParser.init() in CPPParser.h
	// DW 25/3/97 initialise
	//functionDefinition = 0;
	//classDefinition = 0;	DW 19/03/04 not used anywhere
    //}
    
    //protected void exitExternalScope() {
        /* TODO: revive the original code:
	symbols->dumpScope(stdout);
	symbols->removeScope();	    // This just removes the symbols stored in the current scope
	symbols->restoreScope();    // This just reduces the current scope by 1
         */
    //}
    
    @Override
    protected final void classForwardDeclaration(/*TypeSpecifier*/int ts, DeclSpecifier ds, String tag) {

        CPPSymbol c = null;

        /* TODO: revive the original code:
	// if already in symbol table as a class, don't add
	// of course, this is incorrect as you can rename
	// classes by entering a new scope, but our limited
	// example basically keeps all types globally visible.
	if (symbols->lookup(tag)!=NULL) {
	    CPPSymbol *cs = (CPPSymbol *) symbols->lookup(tag);

	    if (statementTrace >= 2) {
		printf("classForwardDeclaration[%d]: %s already stored in dictionary,"
			" ObjectType %d\n", LT(1)->getLine(),tag,cs->getType());
	    }
	    return;
	}
         */

	switch (ts) {
	case tsSTRUCT :
	    c = new CPPSymbol(tag, CPPSymbol.otStruct);
	    break;

	case tsUNION :
	    c = new CPPSymbol(tag, CPPSymbol.otUnion);
	    break;

	case tsCLASS :
	    c = new CPPSymbol(tag, CPPSymbol.otClass);
	    break;
        case tsInvalid:
            reportError("classForwardDeclaration: invalid TypeSpecifier"); // NOI18N
        default:
           throw new IllegalArgumentException("Illegal argument: " + ts); // NOI18N
	}

        /* TODO: revive the original code:
	symbols->defineInScope(tag, c, externalScope);
         */
	if (statementTrace >= 2) {
	    printf("classForwardDeclaration[%d]: Declare %s in external scope, " + // NOI18N
			"ObjectType %d\n", getLine(1), tag, c.getType()); // NOI18N
	}

	// If it's a friend class forward decl, put in global scope also.
	// DW 04/07/03 No need if already in external scope. See above.
	//if ( ds==dsFRIEND )
	//	{
	//	CPPSymbol *ext_c = new CPPSymbol(tag, CPPSymbol::otClass);
	//	if ( ext_c==NULL ) panic("can't alloc CPPSymbol");
	//	if ( symbols->getCurrentScopeIndex()!=externalScope )	// DW 04/07/03 Not sure this test is really necessary
	//		{
	//		printf("classForwardDeclaration defineInScope(externalScope)\n");
	//		symbols->defineInScope(tag, ext_c, externalScope);
	//		}
	//	}
    }
    
    
    @Override
    protected final void beginClassDefinition(/*TypeSpecifier*/int ts, String tag) {
	CPPSymbol c;

        /* TODO: revive the original code:
	// if already in symbol table as a class, don't add
	// of course, this is incorrect as you can rename
	// classes by entering a new scope, but our limited
	// example basically keeps all types globally visible.
	if (symbols->lookup(tag) != NULL) {
	    symbols->saveScope();   // still have to use scope to collect members
	    //printf("support.cpp beginClassDefinition_1 saveScope() now %d\n",
	    //			symbols->getCurrentScopeIndex());
	    if (statementTrace >= 2) {
		printf("beginClassDefinition[%d]: Classname %s already "
			    "in dictionary\n", LT(1)->getLine(),tag);
	    }
	    return;
	}
         */

	switch (ts) {
	case tsSTRUCT:
	    c = new CPPSymbol(tag, CPPSymbol.otStruct);
	    break;

	case tsUNION:
	    c = new CPPSymbol(tag, CPPSymbol.otUnion);
	    break;

	case tsCLASS:
	    c = new CPPSymbol(tag, CPPSymbol.otClass);
	    break;
        case tsInvalid:
            reportError("classForwardDeclaration: invalid TypeSpecifier"); // NOI18N
        default:
           throw new IllegalArgumentException("Illegal argument: " + ts); // NOI18N
	}
        
        /* TODO: revive the original code:
	symbols->defineInScope(tag, c, externalScope);
         */
	if (statementTrace >= 2) {
	    printf("beginClassDefinition[%d]: Define %s in external scope (1), " + // NOI18N
			"ObjectType %d\n", getLine(1),tag,c.getType()); // NOI18N
	}
	qualifierPrefix.append(tag);
	qualifierPrefix.append("::"); // NOI18N

	// add all member type symbols into the global scope (not correct, but
	// will work for most code).
	// This symbol lives until the end of the file

        /* TODO: revive the original code:
	symbols->saveScope();   // use the scope to collect list of fields
         */
    }
    
    @Override
    protected final void endClassDefinition() {
        /* TODO: revive the original code:
	symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
         */

	// remove final T:: from A::B::C::T::
	// upon backing out of last class, qualifierPrefix is set to ""
        int pos = qualifierPrefix.lastIndexOf("::"); // NOI18N
        if( pos >= 0 ) {
            qualifierPrefix.setLength(pos);
        }
        /* original code was:
	char *p = &(qualifierPrefix[strlen(qualifierPrefix) - 3]);
	while (p > &(qualifierPrefix[0]) && *p!=':') {
	    p--;
	}
	if (p > &(qualifierPrefix[0])) {
	    p++;
	}
	*p = '\0';
         */
        //super.endClassDefinition();
    }

    @Override
    protected final void beginEnumDefinition(String e) {
	// DW 26/3/97 Set flag for new class
	    
	// Add all enum tags into the global scope (not correct, but
	// will work for most code).
	// This symbol lives until the end of the file
	CPPSymbol c = new CPPSymbol(e, CPPSymbol.otEnum);
        /* TODO: revive the original code:
	symbols->defineInScope(e, c, externalScope);
         */
	if (statementTrace >= 2) { 
	    printf("beginEnumDefinition[%d]: %s define in external scope, " + // NOI18N
			"ObjectType %d\n", getLine(1),e,c.getType()); // NOI18N
	}
    }
    
    @Override
    protected final void endEnumDefinition() {
    }
    
    @Override
    protected final void templateTypeParameter(String t) {
	//DW 11/06/03 Symbol saved in templateParameterScope (0)
	//  as a temporary measure until scope is implemented fully
	// This symbol lives until the end of the file
	CPPSymbol e = new CPPSymbol(t, CPPSymbol.otTypedef);
        /* TODO: revive the original code:
	symbols->defineInScope(t, e, templateParameterScope);
         */
	if (statementTrace >= 2) {
	    printf("templateTypeParameter[%d]: Declare %s in " + // NOI18N
			"template parameter scope (0), ObjectType %d\n", // NOI18N
			getLine(1),t,e.getType());
	}
    }
    
    //protected void beginTemplateDeclaration() {
    //}
    
    //protected void endTemplateDeclaration() {
        /* TODO: revive the original code:
	// DW 30/05/03 Remove any template typenames from lower scope
	symbols->saveScope();
	//printf("endTemplateDeclaration saveScope() now %d\n",symbols->getCurrentScopeIndex());
	//symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
	//printf("endTemplateDeclaration restoreScope() now %d\n",
	//		    symbols->getCurrentScopeIndex());
         */
    //}
    
    //protected void beginTemplateParameterList() {
        /* TODO: revive the original code:
	// DW 26/05/03 To scope template parameters
	symbols->saveScope();	// All this does is increase currentScope (lower level)
         */
    //}
    
    //protected void endTemplateParameterList() {
        /* TODO: revive the original code:
	// DW 26/05/03 To end scope template parameters
	symbols->restoreScope();	// All this does is reduce currentScope (higher level)
         */
    //}

    /*protected void exceptionBeginHandler() {
    }
    
    protected void exceptionEndHandler() {
        /* TODO: revive the original code:
	// remove parm elements from the handler scope
	symbols->dumpScope(stdout);
	symbols->removeScope();
	symbols->restoreScope();
         */
    //}
    
//    protected final boolean isCtor() { 
//        return _isCtor();
//    }
    
//    protected final boolean _isCtor() { 
//	if (codeFoldingParse) {
//            String lastID, refID;
//	    /*
//	     * The hard case. We really don't have enough information. What do we look for?
//	     * A list of SCOPE separated IDs, with the last 2 IDs being identical.
//	     */
//	    int i = 1;
//	    Token ref, last = null;
//	    if (LT(i).getType() == SCOPE) {
//		i++;
//	    }
//	    while ((ref = LT(i)).getType() == ID && LT(i + 1).getType() == SCOPE) {
//		i += 2;
//		last = ref;
//	    }
//            lastID = last != null ? last.getText() : null;
//            refID = ref != null ? ref.getText() : null;
//	    return lastID != null && refID != null && strcmp(lastID, refID) == 0;
//	} else {
//	    // The easy case...
//	return qualifiedItemIsOneOf(qiCtor);
//    }
    
    
    /*protected int getOffset() { 
        //try {
            // TODO: correct either code or function name!!
            return LT(0).getColumn();
        /*}
        catch( TokenStreamException e ) {
            reportError(e.getMessage());
            return 0;
        }
    }*/
    
    /*protected int getLine() { 
        return getLine(0);
    }*/
    
    protected final int getLine(int k) { 
        //try {
            return LT(k).getLine();
        /*}
        catch( TokenStreamException e ) {
            reportError(e.getMessage());
            return 0;
        }*/
    }
    
    
    @Override
    protected final void printf(String pattern, int i) { 
        Printf.printf(pattern, new Object[] { Integer.valueOf(i) });
    }
    
    @Override
    protected final void printf(String pattern, Object o) { 
        Printf.printf(pattern, new Object[] { o });
    }
    
    @Override
    protected final void printf(String pattern, int i, Object o) { 
        Printf.printf(pattern, new Object[] { Integer.valueOf(i), o });
    }
    
    @Override
    protected final void printf(String pattern, int i, Object o1, Object o2) { 
        Printf.printf(pattern, new Object[] { Integer.valueOf(i), o1, o2 });
    }
    
    @Override
    protected final void printf(String pattern, int i1, int i2, boolean b1, Object o) { 
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), Integer.valueOf(i2), Boolean.valueOf(b1), o });
    }
    
    @Override
    protected final void printf (String pattern, int i1, Object o1, int i2, Object o2) {
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), o1, Integer.valueOf(i2), o2 });
    }

    protected final void printf (String pattern, int i1, int i2, int i3, Object o) {
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), Integer.valueOf(i2), Integer.valueOf(i3), o });
    }
    
    @Override
    protected final void printf (String pattern, int i1, int i2, int i3, String s) {
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), Integer.valueOf(i2), Integer.valueOf(i3), s });
    }
    
    @Override
    protected final void printf(String pattern, int i1, int i2) {
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), Integer.valueOf(i2) });
    }


    private void printf(String pattern, int i1, Object o1, Object o2, Object o3) { 
        Printf.printf(pattern, new Object[] { Integer.valueOf(i1), o1, o2, o3 });
    }
    
    
    /** overrides base implementation to make indentation 2 instead of 1 */
    @Override
    public final void traceIndent() {
        for (int i = 0; i < traceDepth; i++)
            System.out.print("  "); // NOI18N
    }    
    
    /** 
     * Skips all tokens up to token of type right,
     * matching nested left,...,right pairs
     *
     * TODO: make it (optionally?) build AST. For now AST isn't built.
     */
    @Override
    protected final void balanceBraces(int left, int right) throws RecognitionException, TokenStreamException { 
	int depth = 0;
	while( LA(1) != Token.EOF_TYPE ) { // && LA(1) != CPPTokenTypes.RPAREN ) {
	    if( LA(1) == left ) {
		depth++;
		consume();
	    }
	    else if( LA(1) == right ) {
		consume();
		if( depth == 0 ) {
		    return;
		}
		depth--;
	    }
	    else {
		consume();
	    }
	}
    }    
    
    /**
     * Methods fireSyntacticPredicateStarted(), fireSyntacticPredicateSucceeded() 
     * and fireSyntacticPredicateFailed() are generated by custom antlr,
     * which codegenerator was slightly changed by Vladimir Kvashin.
     * This is made for analyzing time spent in guessing.
     * If we use an ordinary ANTLR (as in official CND), this code never works
     */
    @Override
    protected final void syntacticPredicateStarted(int idx, int nestingLevel, int line) {
        if( guessingCount[idx] == 0 ) {
            // first time - let's find a name
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StackTraceElement element = stack[3];
            guessingNames[idx] = element.getMethodName() + ':' /*+ element.getLineNumber() + ':'*/ + line;
        }
        if( nestingLevel ==0 ) {
	    guessingTimes[idx] -= System.currentTimeMillis();
	}

        guessingCount[idx]++;
        MAX_GUESS_IDX = Math.max(MAX_GUESS_IDX, idx);
    }

    /** see syntacticPredicateFailed description */
    @Override
    protected final void syntacticPredicateFailed(int idx, int nestingLevel) {
	if( nestingLevel ==0 ) {
	    guessingTimes[idx] += System.currentTimeMillis();
	}
        guessingFailures[idx]++;
    }

    /** see syntacticPredicateStarted description */
    @Override
    protected final void syntacticPredicateSucceeded(int idx, int nestingLevel) {
	if( nestingLevel ==0 ) {
	    guessingTimes[idx] += System.currentTimeMillis();
	}
    }
    
    private static final int MAX_GUESS = 256;
    public static int MAX_GUESS_IDX = 0;
    
    public static long[] guessingTimes = new long[MAX_GUESS];
    public static long[] guessingCount = new long[MAX_GUESS];
    public static long[] guessingFailures = new long[MAX_GUESS];
    public static String[] guessingNames = new String[MAX_GUESS];

    static {
	for (int i = 0; i<MAX_GUESS;i++) {
	    guessingTimes[i]=0;
	    guessingCount[i]=0;
	    guessingFailures[i]=0;
	}
    }

    public interface ErrorDelegate {
        void onError(RecognitionException e);
    }
    
    private ErrorDelegate errorDelegate;
    
    public void setErrorDelegate(ErrorDelegate errorDelegate) {
        this.errorDelegate = errorDelegate;
    }
    
    protected void onError(RecognitionException e) {
        ErrorDelegate delegate = errorDelegate;
        if( delegate != null ) {
            delegate.onError(e);
        }
    }
}

