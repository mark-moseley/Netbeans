package org.netbeans.test.java.hints;

import java.util.Comparator;

public class ImplementAbstractMethods9 {
    
    private void test() {
        a(new Comparator {
            
        });
    }
    
    private void a(Comparator c) {}
    
}
