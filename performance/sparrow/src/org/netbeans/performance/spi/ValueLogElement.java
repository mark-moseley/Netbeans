/*
 * ValueLogElement.java
 *
 * Created on October 8, 2002, 2:38 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
/**A log element that supplies a value derived from whatever
 * text it wraps.
 *
 * @author  Tim Boudreau
 */
public abstract class ValueLogElement extends AbstractLogElement implements Valued {
    protected Object value=null;
    
    /** Do nothing constructor for subclassers. */
    protected ValueLogElement () {
    }
    
    /** Creates a new instance of ValueLogElement, using the passed
     * String as the thing to be parsed to find the value
     * on demand.
     */
    public ValueLogElement(String s) {
        super (s);
    }
    
    /**Creates a new instances of ValueLogElement, using the
     * passed arguments as the value.  Instances created
     * by this constructor will <i>not</i> ever call the parse()
     * method - it is assumed the code that created them set
     * the values correctly.  
     */
    protected ValueLogElement(Object value) {
        super();
        this.value = value;
        parsed=true;
    }
    
    public synchronized Object getValue () {
        checkParsed();
        return value;
    }

    /**Get a string representation of the object.  If the <code>line</code>
     * instance variable from AbstractLogElement is null, uses the <code>value</code>
     * field by calling toString() on it.
     */
    public String toString() {
        String result = super.toString();
        if (result == null) result = value.toString();
        return result;
    }


    public int hashCode () {
        checkParsed();
        if (value != null) {
            return value.hashCode() ^ 37;
        } else {
            return 0;
        }
    }
    
    public boolean equals (Object o) {
        checkParsed();
        return (o.getClass() == ValueLogElement.class) && ((ValueLogElement) o).value == value;
    }
    
    public HTML toHTML () {
        checkParsed();
        return new HTMLTextItem (value.toString());
    }
}
