package test;
import java.io.IOException;

public class OverrideTypedException<T extends Throwable> {
    
    public void test() throws T {
        
    }
    
    public static class Test1 extends OverrideTypedException<IOException> {
        
    }
    
    public static class Test2<E extends RuntimeException> extends OverrideTypedException<E> {
        
    }
    
}
