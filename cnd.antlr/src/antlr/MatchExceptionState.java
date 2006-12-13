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

package antlr;

/**
 *
 * @author gorrus
 */
public abstract class MatchExceptionState {
    public boolean matchError = false;
    public RecognitionException matchException = defaultException;
    
    public static final RecognitionException defaultException = new RecognitionException("Exceptions turned off, so you are unable to see error description here");
    
    // Options
    // Throw recognitionExceptions if needed (original behaviour)
    protected static final boolean throwRecExceptions = false;
    
    public void resetMatchError() {
        matchError = false;
        matchException = defaultException;
    }
}
