package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class InterfaceContainsEnumTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(InterfaceContainsEnumTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testInterfaceContainsEnum() {		
		execute(getClass().getSimpleName());
	}
}
