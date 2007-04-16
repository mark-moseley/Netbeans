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
package org.netbeans.modules.java.editor.semantic;

import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class MarkOccurrencesHighlighterFactory extends CaretAwareJavaSourceTaskFactory {

    /** Creates a new instance of SemanticHighlighterFactory */
    public MarkOccurrencesHighlighterFactory() {
        super(Phase.RESOLVED, Priority.LOW);
    }

    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        Preferences node = MarkOccurencesSettings.getCurrentNode();
        
        if (node.getBoolean(MarkOccurencesSettings.ON_OFF, true))
            return new MarkOccurencesHighlighter(file);
        else
            return new CancellableTask<CompilationInfo>() {
                public void cancel() {}
                
                public void run(CompilationInfo parameter) throws Exception {}
            };
    }

}
