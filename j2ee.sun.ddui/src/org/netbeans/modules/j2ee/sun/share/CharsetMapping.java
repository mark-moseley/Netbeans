/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * CharsetMapping.java
 *
 * Created on December 10, 2003, 3:22 PM
 */

package org.netbeans.modules.j2ee.sun.share;

import java.nio.charset.Charset;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import java.text.MessageFormat;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/** Object for nice usage of Charsets in comboboxes, sorted lists, etc.
 *  Provides same equality properties as Charset (but with CharsetMapping)
 *  but a better "display name" via toString().
 *
 *  There are also several static utility methods for finding and creating
 *  Charset and CharsetMappings.
 *
 * @author Peter Williams
 */
public class CharsetMapping implements Comparable {
	
    private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.Bundle");	// NOI18N	
	
	private Charset charset;
	private String chosenAlias;
	private boolean showAliases;
	private String displayText;
	private boolean textOutOfDate;

	/** Create a mapping for a charset
	 *
	 * @param c The charset for this mapping
	 */
	public CharsetMapping(final Charset c) {
		this(c, c.displayName(), true);
	}
	
	/** Create a mapping for a charset with the obtion to turn on or off display
	 *  of aliases next to the the charset canonical name.
	 *
	 * @param c The charset for this mapping
	 * @param sa 
	 */
	public CharsetMapping(final Charset c, boolean sa) {
		this(c, c.displayName(), sa);
	}	
	
	/** Create a mapping for a charset alias
	 *
	 * @param c The charset for this mapping
	 * @param alias The particular alias represented by this mapping
	 */
	public CharsetMapping(final Charset c, String alias) {
		this(c, alias, false);
	}
	
	private CharsetMapping(final Charset c, String alias, boolean sa) {
		charset = c;
		chosenAlias = alias;
		showAliases = sa;
		displayText = buildDisplayText();
	}
	
	/** equals() maps to Charset.equals()
	 *
	 * @return true/false based on whether the embedded charset objects compare
	 *  as equal.
	 */
	public boolean equals(Object o) {
		boolean result = false;
		
		if(o instanceof CharsetMapping) {
			CharsetMapping cm = (CharsetMapping) o;
			result = chosenAlias.equals(cm.getAlias());
		}
		
		return result;
	}
	
	/** hashCode() maps to Charset.hashCode()
	 *
	 * @return the hashcode
	 */
	public int hashCode() {
		return charset.hashCode();
	}
	
	/** A more readable display string.  If the mappings are for canonical charsets
	 *  only and "showAliases" is true, then the string will include a bracketed
	 *  display of all aliases for this charset after showing the canonical name.
	 *
	 * @return A descriptive string
	 */
	public String toString() {
		if(textOutOfDate) {
			displayText = buildDisplayText();
		}
		
		return displayText;
	}

	/** The charset
	 *
	 * @return the charset this is a mapping for
	 */
	public Charset getCharset() {
		return charset;
	}
	
	/** The alias string.  If this is a canonical map, will return canonical string.
	 *
	 * @return the charset alias this is a mapping for
	 */	
	public String getAlias() {
		return chosenAlias;
	}
	
	/** Force the display text to be recalculated.  Recalculation won't happen
	 *  until next time text is requested.
	 */
	public void updateDisplayText() {
		textOutOfDate = true;
	}

	private String buildDisplayText() {
		String result = chosenAlias;
		
		if(showAliases) {
			StringBuffer aliasList = new StringBuffer(200);

			for(Iterator iter = charset.aliases().iterator(); iter.hasNext(); ) {
				aliasList.append((String) iter.next());
				if(iter.hasNext()) {
					aliasList.append(", ");	// NOI18N
				}
			}
			
			Object [] args = new Object [] { chosenAlias, aliasList.toString() };
			
			result = MessageFormat.format(
				webappBundle.getString("LBL_CharsetComboBoxDisplayText"), args);	// NOI18N
		}
		
		if(result == null || result.length() == 0) {
			result = webappBundle.getString("LBL_UnnamedCharset");	// NOI18N
		}
		
		textOutOfDate = false;
		
		return result;
	}
	
	/** For sorted collections.  We compare the alias representations of the 
	 *  embedded charset.
	 *
	 * @param obj the Charset to compare to
	 * @return result of comparison (negative, 0, or positive depending on match)
	 */
	public int compareTo(Object obj) {
		int result = -1;
		
		if(obj instanceof CharsetMapping) {
			// !PW FIXME This is different than equals.  Why?
			// -- To properly sort charsets by alias, we need this implementation
			// like this.  But how do we reconcile this with the way equals
			// works?
			//
			CharsetMapping targetMapping = (CharsetMapping) obj;
			result = chosenAlias.compareTo(targetMapping.getAlias());
		}
		
		return result;
	}
	
	private static boolean useAliases = false;
	private static SortedMap sortedCanonicalCharsetMappings = null;
	
	private static SortedMap getSortedCanonicalCharsetMappings() {
		if(sortedCanonicalCharsetMappings == null) {
			SortedMap charsets = Charset.availableCharsets();
			sortedCanonicalCharsetMappings = new TreeMap();

			for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry entry = (Map.Entry) iter.next();
				CharsetMapping mapping = new CharsetMapping((Charset) entry.getValue());
				sortedCanonicalCharsetMappings.put(mapping.getAlias(), mapping);
			}
		}
		
		return sortedCanonicalCharsetMappings;
	}
	
        private static SortedMap sortedAliasCharsetMappings = null;
	
	private static SortedMap getSortedAliasCharsetMappings() {
		if(sortedAliasCharsetMappings == null) {
			SortedMap charsets = Charset.availableCharsets();
			sortedAliasCharsetMappings = new TreeMap();

			for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry entry = (Map.Entry) iter.next();
				Charset charset = (Charset) entry.getValue();
				CharsetMapping mapping = new CharsetMapping(charset, false);
				sortedAliasCharsetMappings.put(mapping.getAlias(), mapping);

				for(Iterator aliasIter = charset.aliases().iterator(); aliasIter.hasNext(); ) {
					String alias = (String) aliasIter.next();
					CharsetMapping aliasMapping = new CharsetMapping(charset, alias);
					sortedAliasCharsetMappings.put(alias, aliasMapping);
				}
			}
		}
		
		return sortedAliasCharsetMappings;
	}
	
	/** Return a sorted map containg mappings for all charsets supported by the 
	 *  current JVM.  Depending on the value of the global alias flag, the map
	 *  will contain Charsets indexed by canonical name only, or also indexed
	 *  by alias.
	 *
	 * @return SortedMap containing CharsetMapping objects
	 */
	public static SortedMap getSortedAvailableCharsetMappings() {
		SortedMap result;
		
		if(useAliases) {
                        if(sortedAliasCharsetMappings == null){
                            sortedAliasCharsetMappings = getSortedAliasCharsetMappings();
                        }
			result = sortedAliasCharsetMappings;
		} else {
                        if(sortedCanonicalCharsetMappings == null){
                            sortedCanonicalCharsetMappings = getSortedCanonicalCharsetMappings();
                        }
			result = sortedCanonicalCharsetMappings;
		}
		
		return result;
	}

	/** Retrieve the CharsetMapping object matching this charset.
	 *
	 * @param c Charset to search for.
	 * @return CharsetMapping matching the passed in charset.  Null if not found.
	 */
	public static CharsetMapping getCharsetMapping(Charset c) {
		return (CharsetMapping) getSortedAvailableCharsetMappings().get(c.name());
	}
	
	/** Retrieve the CharsetMapping object matching this name.
	 *
	 * @param name Charset name to search for.
	 * @return CharsetMapping matching the passed in name.  Null if not found.
	 */
	public static CharsetMapping getCharsetMapping(String name) {
		CharsetMapping result = null;
		
		if(name != null) {
			try {
				Charset charset = Charset.forName(name);

				if(charset != null) {
					result = (CharsetMapping) getSortedAvailableCharsetMappings().get(charset.name());
				}
			} catch(Exception ex) {
				// FIXME handle this one better: Unrecognized or otherwise illegal charset specified.
			}
		}
		
		return result;
	}
	
/*
	public static class CharsetComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			int result = -1;
			
			if(o1 instanceof Charset && o2 instanceof Charset) {
				Charset s1 = (Charset) o1;
				Charset s2 = (Charset) o2;
				
				result = s1.compareTo(s2);
			}
			
			return result;
		}
	}	
 */
	
	/** -----------------------------------------------------------------------
	 *  property storage and notification of the user option.
	 */
	
	public static final String CHARSET_DISPLAY_TYPE = "CharsetDisplayType";
	public static final Integer CHARSET_CANONICAL = new Integer(0);
	public static final Integer CHARSET_ALIAS_ASIDE = new Integer(1);
	public static final Integer CHARSET_ALIAS_SELECTION = new Integer(2);

	private static Integer displayOption = CHARSET_ALIAS_ASIDE;
	private static java.beans.PropertyChangeSupport propSupport = new PropertyChangeSupport(CharsetMapping.class);;
	
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}
	
	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}
			
	public static void setDisplayOption(Integer option) {
		Integer newDisplayOption = getDisplayOptionEnum(option);
		
		if(newDisplayOption != null && newDisplayOption != displayOption) {
			Integer oldDisplayOption = displayOption;
			displayOption = newDisplayOption;
			updateInternalState();
			
			propSupport.firePropertyChange(CHARSET_DISPLAY_TYPE, oldDisplayOption, displayOption);
		}
	}
	
	private static void updateInternalState() {
		if(displayOption == CHARSET_CANONICAL) {
			useAliases = false;
                        if(sortedCanonicalCharsetMappings == null){
                            sortedCanonicalCharsetMappings = getSortedCanonicalCharsetMappings();
                        }
			Collection mappings = sortedCanonicalCharsetMappings.values();
			for(Iterator iter = mappings.iterator(); iter.hasNext(); ) {
				CharsetMapping mapping = (CharsetMapping) iter.next();
				mapping.showAliases = false;
				mapping.updateDisplayText();
			}
		} else if(displayOption == CHARSET_ALIAS_ASIDE) {
			useAliases = false;
                        if(sortedCanonicalCharsetMappings == null){
                            sortedCanonicalCharsetMappings = getSortedCanonicalCharsetMappings();
                        }
			Collection mappings = sortedCanonicalCharsetMappings.values();
			for(Iterator iter = mappings.iterator(); iter.hasNext(); ) {
				CharsetMapping mapping = (CharsetMapping) iter.next();
				mapping.showAliases = true;
				mapping.updateDisplayText();
			}
		} else if(displayOption == CHARSET_ALIAS_SELECTION) {
			useAliases = true;
		}
	}
	
	public static Integer getDisplayOption() {
		return displayOption;
	}
	
	private static Integer getDisplayOptionEnum(Integer option) {
		Integer result = null;
		
		if(option != null) {
			if(CHARSET_CANONICAL.compareTo(option) == 0) {
				result = CHARSET_CANONICAL;
			} else if(CHARSET_ALIAS_ASIDE.compareTo(option) == 0) {
				result = CHARSET_ALIAS_ASIDE;
			} else if(CHARSET_ALIAS_SELECTION.compareTo(option) == 0) {
				result = CHARSET_ALIAS_SELECTION;
			}
		}
		
		return result;
	}
}
