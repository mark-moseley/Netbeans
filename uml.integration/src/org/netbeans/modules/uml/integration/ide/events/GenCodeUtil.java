/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.integration.ide.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public final class GenCodeUtil
{
    private GenCodeUtil(){}

    
    public static boolean isValidClassType(String type)
    {
        // valid class type are anything but:
        // null, empty string, void, String, or a primitive
        return !(
            type == null ||
            type.length() == 0 ||
            type.equals("void") || // NOI18N
            type.equals("String") || // NOI18N
            type.equals("java.lang.String") || // NOI18N
            JavaClassUtils.isPrimitive(type));
    }
    
    public static String getCodeGenType(
        IClassifier classType, 
        String[] collectionTypes, 
        boolean useGenerics, 
        IMultiplicity mult,
	boolean fullyQualified)
    {

	String classTypeName = getTypeCodeGenType(classType, fullyQualified);
//        if (mult != null && mult.getRangeCount() > 0)
        if (mult != null && isMultiDim(mult))
        {
	    return assembleMultiDimDataType(classTypeName, 
					    collectionTypes, 
					    useGenerics, 
					    mult.getRangeCount(),
					    fullyQualified);
	}        
	return classTypeName;

    }
    
    
    public static String assembleMultiDimDataType(
        String coreTypeName, 
        String[] collectionTypes, 
        boolean useGenerics, 
        long dimCount,
	boolean fullyQualified)
    {
	boolean isPrimitive = true;

	isPrimitive = JavaClassUtils.isPrimitive(coreTypeName);

        if (dimCount == 0)	    
	    return coreTypeName;
	
	if (isPrimitive 
	    && collectionTypes[(int)dimCount - 1] != null
	    && ! collectionTypes[(int)dimCount - 1].equals(""))
	{
	    String type = JavaClassUtils.getPrimitiveWrapperType(coreTypeName);
	    if (fullyQualified) 
		coreTypeName = "java.lang." + type;
	    else
		coreTypeName = type;

	    isPrimitive = false;
	}	    
	
	String leftPart = "";
	String rightPart = "";

	for (int i = 0; i < dimCount; i++)
        {
	    String colType = collectionTypes[i];
	    if (((colType != null) && ( ! colType.trim().equals(""))) 
		&& ((i != dimCount - 1) || ( ! isPrimitive)))
	    {
		leftPart += colType;
		if (! useGenerics) {
		    return leftPart + rightPart;
		} else {
		    leftPart += '<';
		    rightPart = '>' + rightPart;
		}
	    } else {
		rightPart = "[]" + rightPart;
	    }
	}
	return leftPart + coreTypeName + rightPart;
    }


    public final static String ASTERIK = "*";
    
    public static boolean isMultiDim(IMultiplicity mult)
    {
        if (mult == null || mult.getRanges().size() == 0)
            return false;
     
        // if more than one dimension, even if all are upper limit of 1,
        // we still want to use Collections
        else if (mult.getRanges().size() > 1)
            return true;

        String lowerStr = mult.getRanges().get(0).getLower();
        String upperStr = mult.getRanges().get(0).getUpper();
        
	boolean result = upperStr.equals(ASTERIK) || lowerStr.equals(ASTERIK);
	if (! result) 
	{
	    try 
	    {		
		result = Long.valueOf(upperStr).intValue() > 1;
	    } 
	    catch (NumberFormatException nfe) 
	    {
		// do nothing as it is just happens to be not a number 
	    }
	}
	return result;
    }

    public static String removeGenericType(String type)
    {
        return type.indexOf("<") == -1 
            ? type : type.substring(0, type.indexOf('<'));
    }


    //
    // added for template codegen
    //

    public static String[] getFullyQualifiedCodeGenType(IClassifier classType)
    {
	if (classType == null) {
	    return null;
	}
        IPackage owningPkg = classType.getOwningPackage();
	if (owningPkg == null) {
	    return null;
	}
        String fullPkgName = owningPkg.getFullyQualifiedName(false);

        // default package elements have the project as the owning package
        if (owningPkg instanceof IProject)
            fullPkgName = "";

        // get fully qualified name - "com::foo::bar::Outer::Middle::Inner"
        String qualName = classType.getFullyQualifiedName(false);
        String fullClassName = qualName;

        if (isValidClassType(fullClassName))
        {
            // extract the full class name - "Outer::Middle::Inner"
            // and convert to dot notation = "Outer.Middle.Inner"

            if (fullPkgName.length() > 0)
            {
                fullClassName = JavaClassUtils.convertUMLtoJava(
                    qualName.substring(fullPkgName.length()+2));
                fullPkgName = JavaClassUtils.convertUMLtoJava(fullPkgName);
            }
            // it's in the default package
            else
                fullClassName = JavaClassUtils.convertUMLtoJava(qualName);
	    
        }
	return new String[] {fullPkgName, fullClassName};
    }
    
    public static String getTypeName(IClassifier clazz, boolean fullyQualified) 
    {
	String fullClassName = "";
	String[] packAndName = getFullyQualifiedCodeGenType(clazz);
	if (packAndName != null && packAndName.length == 2) {
	    if (fullyQualified) 
		fullClassName = packAndName[0] + "." + packAndName[1];
	    else 
		fullClassName = packAndName[1];	
	}              
	return fullClassName;

	/*
        String name = JavaClassUtils.getFullyQualifiedName(clazz);        
        name = JavaClassUtils.getFullInnerClassName(name);
	if (! fullyQualified) 
	{
	    name = JavaClassUtils.getShortClassName(name);
	}
	return name;
	*/
    }

    public static String getTypeCodeGenType(IClassifier classType, boolean fullyQualified) 
    {
	IClassifier clazz = null;
	String result = "";
	if (classType instanceof IDerivationClassifier) 
	{
	    IDerivation drv = classType.getDerivation();
	    if (drv != null) 
	    {
		clazz = drv.getTemplate();		
		List<IUMLBinding> bindings =  drv.getBindings();
		if (bindings != null) 
		{
		    boolean first = true;
		    for (IUMLBinding b : bindings) 
		    {
			if (b.getActual() instanceof IClassifier) 
			{
			    String name = getTypeCodeGenType((IClassifier)b.getActual(), fullyQualified);
			    if (name != null && ! name.trim().equals(""))
			    {
				if (JavaClassUtils.isPrimitive(name)) {
				    name = JavaClassUtils.getPrimitiveWrapperType(name);
				}
				if (! first)
				{
				    result = result + ", ";
				} 
				else 
				{
				    first = false;
				}
				result = result + name.trim();
			    }
			}
		    }
		    if (result != null && !result.trim().equals(""))
		    {
			result = "<" + result + ">";			
		    } 
		}	
	    }
	    else 
	    {
		clazz = classType;
	    }
	}  
	else 
	{
	    clazz = classType;
	}   
	result = getTypeName(clazz, fullyQualified) + result;	
	return result;
    }


    // see getCodeGenType()/assembleMultiDimDataType() 
    // for how the type string is formed 
    public static ArrayList<String[]> getReferredCodeGenTypes(
        IClassifier classType, 
        String[] collectionTypes, 
        boolean useGenerics, 
        IMultiplicity mult)
    {
	ArrayList<String[]> res = new ArrayList<String[]>();
	boolean isPrimitive = false;

	ArrayList<String[]> refs;
	if (classType instanceof IDerivationClassifier) {
	    refs = getReferredCodeGenTypes(classType);
	} else {
	    String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(classType);
	    if ( ! ( fqType != null && fqType.length == 2 && fqType[1] != null) ) {	
		return null;
	    }
	    
	    String fullClassName = fqType[1];
	    int ind = fullClassName.indexOf('.');
	    if (ind > 0)
	    {
		// nested class, only it outer most owner 
		// is needed to be referred/imported
		fullClassName = fullClassName.substring(0, ind); 
		fqType[1] = fullClassName;
	    }	   
	    refs = new ArrayList<String[]>();
	    refs.add(fqType);
	    isPrimitive = JavaClassUtils.isPrimitive(fullClassName);
	}

	boolean reffersTheType = true;
        if (mult != null && isMultiDim(mult))
        {
	    int dimCount = (int)mult.getRangeCount();
	    if (isPrimitive 
		&& collectionTypes[(int)dimCount - 1] != null
		&& ! collectionTypes[(int)dimCount - 1].equals(""))
	    {		
		isPrimitive = false;
	    }	    
	    for (int i = 0; i < dimCount; i++)
	    {
		String colType = collectionTypes[i];
		if (((colType != null) && ( ! colType.trim().equals(""))) 
		    && ((i != dimCount - 1) || ( ! isPrimitive)))
		{
		    res.add(new String[]{JavaClassUtils.getPackageName(colType), 
					 JavaClassUtils.getShortClassName(colType)});		    
		    if (! useGenerics) {
			reffersTheType = false;
			break;
		    } 
		}
	    }
	}
	
	if (reffersTheType) {
	    if (refs != null) {
		res.addAll(refs);	
	    }    
	}

	return res;
    }


    public static ArrayList<String[]> getReferredCodeGenTypes(IClassifier classType)
    {
	ArrayList<String[]> res = new ArrayList<String[]>();

	IClassifier clazz = null;

	if (classType instanceof IDerivationClassifier) {
	    IDerivation drv = classType.getDerivation();
	    if (drv != null) {
		clazz = drv.getTemplate();
		List<IUMLBinding> bindings =  drv.getBindings();
		if (bindings != null) {
		    for (IUMLBinding b : bindings) {
			if (b.getActual() instanceof IClassifier) {
			    ArrayList<String[]> refs 
				= getReferredCodeGenTypes((IClassifier)b.getActual());
			    if (refs != null) {
				res.addAll(refs);
			    }
			}
		    }
		}
	    } else {
		// it is something like orphaned pack.clazz<type argument>, 
		// ie. there isn't derivation connecting it to pack.clazz;
		// will try to extract "pack.clazz", though without the bindings 
		// there isn't enough info for "type argument" 
		String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(classType);
		if (( fqType != null && fqType.length == 2) ) {	
		    String name = fqType[1];
		    if (name != null) {
			int ind = name.indexOf('<');
			if (ind > 1) {
			    name = name.substring(0, ind);
			}
		    }
		    res.add(new String[] {fqType[0], name});
		}
	    }
	} else {
	    clazz = classType;
	}
	
	if (clazz != null) {
	    String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(clazz);
	    if (( fqType != null && fqType.length == 2) ) {	
		res.add(fqType);	
	    }
	}

	return res;
    }



    // utility method merges 2 ArrayLists 
    // of String[2] with package and name of a class
    public static void mergeReferredCodeGenTypes(ArrayList<String[]> res, 
					  HashSet<String> fqNames, 
					  ArrayList<String[]>refs) 
    {	
	if (refs == null) {
	    return;
	}
	Iterator iter = refs.iterator();	
	while(iter.hasNext()) {
	    String[] pn = (String[]) iter.next();
	    if (pn != null && pn.length == 2) {
		if (pn[0] != null &&  pn[1] != null) {
		    String fq = pn[1]+"."+pn[0];
		    if ( ! fqNames.contains(fq) ) {
			fqNames.add(fq);
			res.add(pn);
		    }
		}
	    }
	}	       
    }


    public static String[] getCollectionOverrideDataTypes(IMultiplicity multiplicity, boolean fullyQualified){
	
	if (multiplicity == null) {
	    return null;
	} 
	List<IMultiplicityRange> ranges = multiplicity.getRanges();
	if (ranges == null) {
	    return null;
	}

	String[] res = new String[(int)multiplicity.getRangeCount()];
	Iterator<IMultiplicityRange> iter = ranges.iterator();
	for(int i = 0 ; i < res.length; i++) {
	    String type = null;
	    if (iter.hasNext()) {	
		IMultiplicityRange range = iter.next();
		if (range != null) {
		    type = range.getCollectionType(false);
		    type = JavaClassUtils.convertUMLtoJava(type);
		}	        
	    }
	    // as there is always a value set at the attribute/parameter level
	    // whereis empty means "AsArray"
	    // then there isn't need for the global one
	    //if (type == null || type.trim().equals("") ) {
	    //	type = UMLSupport.getUMLSupport().getCollectionOverride();		
	    //}
	    if (! fullyQualified) {
		type = JavaClassUtils.getShortClassName(type);
	    }
	    res[i] = type;
	}
	return res;

    }


}
