package org.netbeans.modules.mobility.end2end.profiles.alljava;


import org.netbeans.modules.mobility.javon.JavonSerializer;

import org.netbeans.modules.mobility.javon.Traversable;

import org.netbeans.modules.mobility.javon.JavonMapping;

import org.netbeans.modules.mobility.e2e.classdata.ClassData;

import org.netbeans.modules.mobility.e2e.classdata.FieldData;

import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;

import org.netbeans.modules.mobility.e2e.classdata.MethodData;


import javax.lang.model.type.TypeMirror;

import javax.lang.model.type.TypeKind;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ArrayType;

import javax.lang.model.element.TypeElement;

import javax.lang.model.element.VariableElement;

import javax.lang.model.element.Modifier;

import javax.lang.model.element.ExecutableElement;

import javax.lang.model.util.ElementFilter;

import java.util.Map;


/**
 * User: bohemius
 * Date: Apr 19, 2007
 * Time: 3:09:09 PM
 */

public class AllJavaSerializer implements JavonSerializer {

    public String getName() {
        return "All Java Serializer";//NOI18N

    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        return true;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if ( TypeKind.DECLARED == type.getKind() ) {
            return getDeclaredType( type, typeCache );

        } /*else if ( TypeKind.ARRAY == type.getKind() ) {
            return getArrayType( type, typeCache );

        }*/
        return null;
    }

    private ClassData getDeclaredType( TypeMirror type, Map<String, ClassData> typeCache ) {
        assert type.getKind()==TypeKind.DECLARED;

        TypeElement clazz = (TypeElement) ( (DeclaredType) type ).asElement();
        String clsName = clazz.getSimpleName().toString();
        String fqName = clazz.getQualifiedName().toString();
        String pkgName = "";

        if ( clsName.length() != fqName.length() )
            pkgName = fqName.substring( 0, fqName.lastIndexOf( '.' ) );

        if ( typeCache.containsKey( fqName ) ) {
            return typeCache.get( fqName );
        }
        //add the new class info to the type cache, including its methods and fields
        else {
            ClassData clsData = new ClassData( pkgName, clsName, false, false, false, this );
            typeCache.put( fqName, clsData );
            return clsData;
        }
    }


    //TODO this will likely be erased and not used when the ClassDataRegistry is fixed
    private ClassData getArrayType( TypeMirror type, Map<String, ClassData> typeCache ) {
        assert type.getKind()==TypeKind.ARRAY;

        ArrayType array = (ArrayType) type;
        TypeMirror componentType = array.getComponentType();

        if (componentType.getKind()==TypeKind.ARRAY)
            getArrayType( componentType, typeCache);
        else {
            TypeElement componentClazz= (TypeElement) ( ( DeclaredType) type ).asElement();
            String componentClsName=componentClazz.getSimpleName().toString();
            String componentFqName=componentClazz.getQualifiedName().toString();
            String pkgName = "";

            if (componentClsName.length()!= componentFqName.length())
                pkgName=componentFqName.substring( 0, componentFqName.lastIndexOf( '.' ) );

            if (typeCache.containsKey( componentFqName)) {
                ClassData component=typeCache.get( componentFqName);
                if (component.isArray())
                    return component;
            } else {
                ClassData result=new ClassData (pkgName, componentClsName, false, true, false, this);
                typeCache.put( componentFqName, result);
                return result;
            }
        }
        return null;
    }

    public String instanceOf( ClassData type ) {
        return type.getFullyQualifiedName();
    }

    public String toObject( ClassData type, String variable ) {
        return null;
    }

    public String fromObject( ClassData type, String object ) {
        return null;
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        return null;
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        return null;
    }

    private String displayClassData( ClassData clsData ) {
        StringBuffer result = new StringBuffer( clsData.getFullyQualifiedName() + "\n\n" );

        for ( FieldData fe : clsData.getFields() ) {
            result.append( fe.getModifier() + " " + fe.getType() + " " + fe.getName() + "\n" );
        }

        result.append( "\n" );
        for ( MethodData me : clsData.getMethods() ) {
            result.append( me.getReturnType() + " " + me.getName() + "(" );
            int i = 0;
            for ( MethodParameter mp : me.getParameters() ) {
                result.append( mp.getType().getFullyQualifiedName() + " " + mp.getName() );
                if ( i == me.getParameters().size() - 1 )
                    result.append( "," );
                else
                    result.append( ")\n" );
            }
        }
        return result.toString();
    }
}

