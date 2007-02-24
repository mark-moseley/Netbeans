package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;

/**
 * Test cases for ExpansionVariable.
 * 
 */
public class ExpansionVariableTestCase extends AbstractUMLTestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ExpansionVariableTestCase.class);
	}

	private ExpansionVariable var;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		var = new ExpansionVariable();
		var.setNode(DocumentFactory.getInstance().createElement("basenode"));
	}

	public void testSetExpansionName() {
		var.setExpansionName("xyzzy");
		assertEquals("xyzzy", var.getExpansionName());
	}

	public void testGetExpansionName() {
		// Tested by testSetExpansionName.
	}

	public void testSetIDLookup() {
		var.setIDLookup("xmi");
		assertEquals("xmi", var.getIDLookup());
	}

	public void testGetIDLookup() {
		// Tested by testSetIDLookup.
	}

	public void testGetIsAttributeResult() {
		var.setQuery("@x");
		assertTrue(var.getIsAttributeResult());
		var.setQuery("x");
		assertFalse(var.getIsAttributeResult());
	}

	public void testSetKind() {
		var.setKind(VariableKind.VK_BOOLEAN);
		assertEquals(VariableKind.VK_BOOLEAN, var.getKind());

		var.setKind(VariableKind.VK_NODES);
		assertEquals(VariableKind.VK_NODES, var.getKind());
	}

	public void testGetKind() {
		// Tested by testSetKind.
	}

	public void testSetMethodGet() {
		var.setMethodGet("foo");
		assertEquals("foo", var.getMethodGet());
	}

	public void testGetMethodGet() {
		// Tested by testSetMethodGet.
	}

	public void testSetName() {
		var.setName("Neiman");
		assertEquals("Neiman", var.getName());
	}

	public void testGetName() {
		// Tested by testSetName.
	}

	public void testSetNode() {
		Node n = DocumentFactory.getInstance().createElement("xyz");
		var.setNode(n);
		assertEquals(n, var.getNode());
	}

	public void testGetNode() {
		// Tested by testSetNode.
	}

	public void testSetOperator() {
		var.setOperator(ExpansionVariable.EOK_AND);
		assertEquals(ExpansionVariable.EOK_AND, var.getOperator());

		var.setOperator(ExpansionVariable.EOK_OR);
		assertEquals(ExpansionVariable.EOK_OR, var.getOperator());
	}

	public void testGetOperator() {
		// Tested by testSetOperator.
	}

	public void testSetOverrideName() {
		var.setOverrideName("zyx");
		assertEquals("zyx", var.getOverrideName());
	}

	public void testGetOverrideName() {
		// Tested by testSetOverrideName.
	}

	public void testSetQuery() {
		var.setQuery("//x");
		assertEquals("//x", var.getQuery());
	}

	public void testGetQuery() {
		// Tested by testSetQuery.
	}

	public void testSetReplaceFilter() {
		var.setReplaceFilter("filter");
		assertEquals("filter", var.getReplaceFilter());
	}

	public void testGetReplaceFilter() {
		// Tested by testSetReplaceFilter.
	}

	public void testSetTrueValue() {
		var.setTrueValue("hello");
		assertEquals("hello", var.getTrueValue());
	}

	public void testGetTrueValue() {
		// Tested by testSetTrueValue.
	}

	public void testSetTypeFilter() {
		var.setTypeFilter("ii");
		assertEquals("ii", var.getTypeFilter());
	}

	public void testGetTypeFilter() {
		// Tested by testSetTypeFilter.
	}

	public void testSetValueFilter() {
		var.setValueFilter("ii");
		assertEquals("ii", var.getValueFilter());
	}

	public void testGetValueFilter() {
		// Tested by testSetValueFilter.
	}

	public void testSetXSLFilter() {
		var.setXSLFilter("ii");
		assertEquals("ii", var.getXSLFilter());
	}

	public void testGetXSLFilter() {
		// Tested by testSetXSLFilter.
	}

	public void testExpand() {
		Element el = DocumentFactory.getInstance().createElement("Iowa");
		var.setKind(VariableKind.VK_NODE_NAME);
		var.setReplaceFilter("owa=exo");
		var.setValueFilter("Iexo=Vogon");
		assertEquals("Vogon", var.expand(el));
		
		IClass newClass = createClass("Clazz");		
		assertEquals("Class",var.expand(newClass.getNode()));
		
	}

	public void testSetExecutionContext() {
		assertNull(var.getExecutionContext());
		IVariableExpander iVE = new VariableExpander();
		var.setExecutionContext(iVE);
		assertNotNull(iVE);
		assertTrue(iVE instanceof VariableExpander);
	}

	public void testGetExecutionContext() {
		// Tested by testSetExecutionContext.
	}

	public void testSetExpansionVariable() {
		assertNull(var.getExpansionVariable());
		IExpansionVariable iVE = new ExpansionVariable();
		var.setExpansionVariable(iVE);
		assertNotNull(iVE);
		assertTrue(iVE instanceof IExpansionVariable);
	}

	public void testGetExpansionVariable() {
		// Tested by testGetExpansionVariable.
	}

	public void testSetResults() {
		assertNull(var.getResults());
		var.setResults("ifVariable");
		assertEquals("ifVariable", var.getResults());
	}

	public void testGetResults() {
		// Tested by testSetResults.
	}

	public void testSetIsTrue() {
		assertFalse(var.getIsTrue());
		var.setIsTrue(true);
		assertTrue(var.getIsTrue());
		var.setIsTrue(false);// Revert Back to Actual position
	}

	public void testGetIsTrue() {
		// Tested by testSetIsTrue.
	}

}