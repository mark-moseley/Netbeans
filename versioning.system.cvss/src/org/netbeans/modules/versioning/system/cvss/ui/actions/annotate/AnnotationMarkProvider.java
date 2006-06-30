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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;
import java.util.Collections;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.openide.text.NbDocument;


/**
 * ErrorStripe liason, real work is done in AnnotationBar.
 *
 * @author Petr Kuzel
 */
final class AnnotationMarkProvider extends MarkProvider {
    
    private List marks = Collections.EMPTY_LIST;
    
    public void setMarks(List marks) {
        List old = this.marks;
        this.marks = marks;
        firePropertyChange(PROP_MARKS, old, marks);
    }
        
    public synchronized List/*<Mark>*/ getMarks() {
        return marks;
    }    
}
