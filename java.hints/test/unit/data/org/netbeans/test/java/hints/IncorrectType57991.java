/*
 * AbstractClass.java
 *
 * Created on March 12, 2005, 7:22 PM
 */

package org.netbeans.test.java.hints;

import java.io.IOException;
import javax.swing.text.BadLocationException;

/**
 *
 * @author lahvac
 */
public abstract class IncorrectType57991 {
    
    /** Creates a new instance of AbstractClass */
    public IncorrectType57991() {
    }
    
    public void test() {
        this.foo = "bar";
    }
    
}
