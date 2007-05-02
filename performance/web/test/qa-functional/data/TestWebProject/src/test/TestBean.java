/*
 * TestBean.java
 *
 * Created on 22. leden 2004, 14:13
 */

package test;

/**
 *
 * @author  lm97939
 */
public class TestBean {
    
    /**
     * Holds value of property stringProperty.
     */
    private String stringProperty;
    
    /**
     * Holds value of property intProperty.
     */
    private int intProperty;
    
    /** Creates a new instance of TestBean */
    public TestBean() {
    }
    
    /**
     * Getter for property stringProperty.
     * @return Value of property stringProperty.
     */
    public String getStringProperty() {
        return this.stringProperty;
    }
    
    /**
     * Setter for property stringProperty.
     * @param stringProperty New value of property stringProperty.
     */
    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }
    
    /**
     * Getter for property intProperty.
     * @return Value of property intProperty.
     */
    public int getIntProperty() {
        return this.intProperty;
    }
    
    /**
     * Setter for property intProperty.
     * @param intProperty New value of property intProperty.
     */
    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }
    
    public int add(int x) {
        return getIntProperty() + x ;
    }
    
}
