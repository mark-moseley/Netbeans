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

package org.netbeans.modules.java.navigation;

import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;

/**
 * This factory creates tasks sensitive to the caret position in open Java editor.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class CaretListeningFactory extends CaretAwareJavaSourceTaskFactory {
    
    private static CaretListeningFactory INSATNCE;
    
    public CaretListeningFactory() {
        super(Phase.RESOLVED, Priority.LOW);
        INSATNCE = this;
    }

    public CancellableTask<CompilationInfo> createTask(FileObject fileObject) {
        return new CaretListeningTask(this, fileObject);
    }
    
    static void runAgain() {
        List<FileObject> fileObjects = INSATNCE.getFileObjects();
        CaretListeningTask.resetLastEH();
        if ( !fileObjects.isEmpty() ) {
            // System.out.println("Rescheduling for " + fileObjects.get(0));
            INSATNCE.reschedule(fileObjects.iterator().next());
        }
    }
    
    
}
