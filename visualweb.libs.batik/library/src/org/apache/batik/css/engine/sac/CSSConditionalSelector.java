/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine.sac;

import java.util.Set;

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.ConditionalSelector} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class CSSConditionalSelector
    implements ConditionalSelector,
	       ExtendedSelector {

    /**
     * The simple selector.
     */
    protected SimpleSelector simpleSelector;

    /**
     * The condition.
     */
    protected Condition condition;

    /**
     * Creates a new ConditionalSelector object.
     */
    public CSSConditionalSelector(SimpleSelector s, Condition c) {
	simpleSelector = s;
	condition      = c;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare.
     */
    public boolean equals(Object obj) {
	if (obj == null || !(obj.getClass() != getClass())) {
	    return false;
	}
	CSSConditionalSelector s = (CSSConditionalSelector)obj;
	return s.simpleSelector.equals(simpleSelector) &&
	       s.condition.equals(condition);
    }

    /**
     * <b>SAC</b>: Implements {@link
     * org.w3c.css.sac.Selector#getSelectorType()}.
     */
    public short getSelectorType() {
	return SAC_CONDITIONAL_SELECTOR;
    }

    /**
     * Tests whether this selector matches the given element.
     */
    public boolean match(Element e, String pseudoE) {
	return ((ExtendedSelector)getSimpleSelector()).match(e, pseudoE) &&
	       ((ExtendedCondition)getCondition()).match(e, pseudoE);
    }

    /**
     * Fills the given set with the attribute names found in this selector.
     */
    public void fillAttributeSet(Set attrSet) {
	((ExtendedSelector)getSimpleSelector()).fillAttributeSet(attrSet);
        ((ExtendedCondition)getCondition()).fillAttributeSet(attrSet);
    }

    /**
     * Returns the specificity of this selector.
     */
    public int getSpecificity() {
	return ((ExtendedSelector)getSimpleSelector()).getSpecificity() +
	       ((ExtendedCondition)getCondition()).getSpecificity();
    }

    /**
     * <b>SAC</b>: Implements {@link
     * org.w3c.css.sac.ConditionalSelector#getSimpleSelector()}.
     */    
    public SimpleSelector getSimpleSelector() {
	return simpleSelector;
    }

    /**
     * <b>SAC</b>: Implements {@link
     * org.w3c.css.sac.ConditionalSelector#getCondition()}.
     */    
    public Condition getCondition() {
	return condition;
    }

    /**
     * Returns a representation of the selector.
     */
    public String toString() {
	return "" + simpleSelector + condition;
    }
    
    // BEGIN RAVE MODIFICATIONS
    public void setRule(org.apache.batik.css.engine.Rule rule) {
        this.rule = rule;
    }
    
    public org.apache.batik.css.engine.Rule getRule() {
        return rule;
    }
    
    private org.apache.batik.css.engine.Rule rule;
    // END RAVE MODIFICATIONS    
}
