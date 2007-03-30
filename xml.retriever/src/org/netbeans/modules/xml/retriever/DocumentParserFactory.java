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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.retriever;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.retriever.impl.*;

/**
 *
 * @author girix
 */
public class DocumentParserFactory {
    
    private static ArrayList<DocumentTypeParser> registereDocumentTypeParsers = new ArrayList<DocumentTypeParser>();
    
    static{
        registereDocumentTypeParsers.add(new DocumentTypeSchemaWsdlParser());
    }
    
    public static DocumentTypeParser getParser(DocumentTypesEnum docType){
        for(DocumentTypeParser dParser: registereDocumentTypeParsers){
            if(dParser.accept(docType.toString()))
                return dParser;
        }
        return null;
    }
    
    public static List<DocumentTypeParser>getRegisteredParsers(){
        return (List<DocumentTypeParser>) registereDocumentTypeParsers;
    }
    
    public static boolean removeRegisteredParser(DocumentTypeParser oldDocumentTypeParser){
        return registereDocumentTypeParsers.remove(oldDocumentTypeParser);
    }
    
    public static void addParser(DocumentTypeParser newDocumentTypeParser){
        registereDocumentTypeParsers.add(newDocumentTypeParser);
    }
    
}
