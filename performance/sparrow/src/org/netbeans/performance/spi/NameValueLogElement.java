/*
 * NameValueLogElement.java
 *
 * Created on October 8, 2002, 6:22 PM
 */

package org.netbeans.performance.spi;

/**Log element which is intended to be instantiated directly
 * with its name and value, with no parsing.  This class is
 * pretty much a special case for NetBeans log entries that
 * gang several settings on a single line.
 *
 * @author  Tim Boudreau
 */
public class NameValueLogElement extends NameValuePairLogElement implements Named {
    private float jitter = Float.NaN;
    
    /**Subclass constructor for classes which will set their value
     * in their constructor by some other means than it being passed
     * to them.  Note that unless the subclass in question needs to
     * parse data, the subclass should set the <code>parsed</code>
     * field to <code>true</code>.
     */
    protected NameValueLogElement (String name) {
        this.name=name;
    }
    
    /** Creates a new instance of NameValueLogElement.
     * Note that this constructor does not do at all what
     * the two argument constructor for NamevaluePairLogElement!
     */
    public NameValueLogElement(String name, Object value) {
        this (name);
        this.value = value;
        parsed = true;
    }
    
    /**Parse for this class throws an UnsupportedOperationException by design.  
     * Do not call it - this class is for things that need to know name and
     * value when the constructor is called.
     */
    protected void parse() throws ParseException {
        throw new UnsupportedOperationException ("NameValueLogElement does not support parsing - create the values directly using the constructor");//NOI18N
    }
    
    public String toString() {
        return name + "=" + value;
    }

}
