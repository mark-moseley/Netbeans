/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Simple token implementation for demo purposes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DemoSampleToken extends StringToken {
    
    private int lookahead;
    
    private int lookback;
    
    private Object state;

    DemoSampleToken(TokenId id, String text) {
        super(id, text);
    }

    public int getLookahead() {
        return lookahead;
    }

    void setLookahead(int lookahead) {
        this.lookahead = lookahead;
    }

    public int getLookback() {
        return lookback;
    }
    
    void setLookback(int lookback) {
        this.lookback = lookback;
    }
    
    public Object getState() {
        return state;
    }
    
    void setState(Object state) {
        this.state = state;
    }
    
}

