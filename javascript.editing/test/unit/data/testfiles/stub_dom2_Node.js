/**
* <p>(Placeholder)
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Node = {
  // This is just a stub for a builtin native JavaScript object.
/**
 * The node is an Attr.
 * @type Number
 */
ATTRIBUTE_NODE: undefined,
/**
 * The node is a CDATASection.
 * @type Number
 */
CDATA_SECTION_NODE: undefined,
/**
 * The node is a Comment.
 * @type Number
 */
COMMENT_NODE: undefined,
/**
 * The node is a DocumentFragment.
 * @type Number
 */
DOCUMENT_FRAGMENT_NODE: undefined,
/**
 * The node is a Document.
 * @type Number
 */
DOCUMENT_NODE: undefined,
/**
 * The node is a DocumentType.
 * @type Number
 */
DOCUMENT_TYPE_NODE: undefined,
/**
 * The node is an Element.
 * @type Number
 */
ELEMENT_NODE: undefined,
/**
 * The node is an Entity.
 * @type Number
 */
ENTITY_NODE: undefined,
/**
 * The node is an EntityReference.
 * @type Number
 */
ENTITY_REFERENCE_NODE: undefined,
/**
 * The node is a Notation.
 * @type Number
 */
NOTATION_NODE: undefined,
/**
 * The node is a ProcessingInstruction.
 * @type Number
 */
PROCESSING_INSTRUCTION_NODE: undefined,
/**
 * The node is a Text node.
 * @type Number
 */
TEXT_NODE: undefined,
/**
 * Adds the node newChild to the end
 * of the list of children of this node. If the newChild is already in the tree, it is first removed.
 * @param {Node} newChild of type Node The node to add. If it is a DocumentFragment object,
 * the entire contents of the document fragment are moved into the
 * child list of this node
 * @return Node The node added.
 * @type Node
 */
appendChild: function(newChild) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * A NamedNodeMap containing
 * the attributes of this node (if it is an Element) or
 * null otherwise.
 * @type NamedNodeMap
 */
attributes: undefined,
/**
 * The absolute base URI of this node or null if the
 * implementation wasn't able to obtain an absolute URI. This value is
 * computed as described in Base URIs. However, when
 * the Document
 * supports the feature "HTML" [DOM Level 2 HTML], the
 * base URI is computed using first the value of the href attribute of
 * the HTML BASE element if any, and the value of the
 * documentURI attribute from the Document interface
 * otherwise.
 * @type String
 */
baseURI: undefined, // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
/**
 * A NodeList
 * that contains all children of this node. If there are no children,
 * this is a NodeList containing no
 * nodes.
 * @type NodeList
 */
childNodes: undefined,
/**
 * Returns a duplicate of this node, i.e., serves
 * as a generic copy constructor for nodes. The duplicate node has no
 * parent ( parentNode is null ) and no user
 * data. User data associated to the imported node is not carried
 * over. However, if any UserDataHandlers has
 * been specified along with the associated data these handlers will
 * be called with the appropriate parameters before this method
 * returns. Cloning an Element copies all
 * attributes and their values, including those generated by the XML
 * processor to represent defaulted attributes, but this method does
 * not copy any children it contains unless it is a deep clone. This
 * includes text contained in an the Element since the text is
 * contained in a child Text node. Cloning an Attr directly, as
 * opposed to be cloned as part of an Element cloning
 * operation, returns a specified attribute ( specified is true ). Cloning an Attr always clones its
 * children, since they represent its value, no matter whether this is
 * a deep clone or not. Cloning an EntityReference automatically constructs its subtree if a corresponding Entity is available, no
 * matter whether this is a deep clone or not. Cloning any other type
 * of node simply returns a copy of this node. Note that cloning an immutable subtree results in a mutable copy,
 * but the children of an EntityReference clone are readonly . In addition,
 * clones of unspecified Attr nodes are specified.
 * And, cloning Document , DocumentType , Entity , and Notation nodes is
 * implementation dependent.
 * @param {Boolean} deep of type boolean If true , recursively clone the subtree under the
 * specified node; if false , clone only the node itself
 * (and its attributes, if it is an Element ).
 * @return Node The duplicate node.
 * @type Node
 */
cloneNode: function(deep) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Compares the reference node, i.e. the node on
 * which this method is being called, with a node, i.e. the one passed
 * as a parameter, with regard to their position in the document and
 * according to the document
 * order .
 * @param {Node} other of type Node The node to compare against the reference node.
 * @return unsigned short Returns how the node is positioned relatively to the reference
 * node.
 * @type Number
 */
compareDocumentPosition: function(other) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * The first child of this node. If there is no such node, this
 * returns null.
 * @type Node
 */
firstChild: undefined,
/**
 * This method returns a specialized object which
 * implements the specialized APIs of the specified feature and
 * version, as specified in DOM
 * Features . The specialized object may also be obtained by using
 * binding-specific casting methods but is not necessarily expected
 * to, as discussed in Mixed DOM
 * Implementations . This method also allow the implementation to
 * provide specialized objects which do not support the Node interface.
 * @param {String} feature of type DOMString The name of the feature requested. Note that any plus sign "+"
 * prepended to the name of the feature will be ignored since it is
 * not significant in the context of this method.
 * @param {String} version of type DOMString This is the version number of the feature to test.
 * @return DOMObject Returns an object which implements the specialized APIs of the
 * specified feature and version, if any, or null if
 * there is no object which implements interfaces associated with that
 * feature. If the DOMObject returned by this
 * method implements the Node interface, it must delegate
 * to the primary core Node and not return results
 * inconsistent with the primary core Node such as
 * attributes, childNodes, etc.
 * @type Object
 */
getFeature: function(feature, version) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Retrieves the object associated to a key on a
 * this node. The object must first have been set to this node by
 * calling setUserData with the same key.
 * @param {String} key of type DOMString The key the object is associated to.
 * @return DOMUserData Returns the DOMUserData associated to
 * the given key on this node, or null if there was
 * none.
 * @type any
 */
getUserData: function(key) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Returns whether this node (if it is an element)
 * has any attributes.
 * @return boolean Returns true if this node has any attributes, false otherwise.
 * @type Boolean
 */
hasAttributes: function() { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Returns whether this node has any children.
 * @return boolean Returns true if this node has any children, false otherwise.
 * @type Boolean
 */
hasChildNodes: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Inserts the node newChild before
 * the existing child node refChild . If refChild is null , insert newChild at the end of the list of children. If newChild is a DocumentFragment object,
 * all of its children are inserted, in the same order, before refChild . If the newChild is already in
 * the tree, it is first removed. Note: Inserting a node before itself is implementation
 * dependent.
 * @param {Node} newChild of type Node The node to insert.
 * @param {Node} refChild of type Node The reference node, i.e., the node before which the new node
 * must be inserted.
 * @return Node The node being inserted.
 * @type Node
 */
insertBefore: function(newChild, refChild) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * This method checks if the specified namespaceURI is the default namespace or not.
 * @param {String} namespaceURI of type DOMString The namespace URI to look for.
 * @return boolean Returns true if the specified namespaceURI is the default namespace, false otherwise.
 * @type Boolean
 */
isDefaultNamespace: function(namespaceURI) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Tests whether two nodes are equal. This method tests for equality of nodes, not sameness (i.e.,
 * whether the two nodes are references to the same object) which can
 * be tested with Node.isSameNode() .
 * All nodes that are the same will also be equal, though the reverse
 * may not be true. Two nodes are equal if and only if the following conditions are
 * satisfied: The two nodes are of the same type. The following string attributes are equal: nodeName , localName , namespaceURI , prefix , nodeValue . This is: they are both null ,
 * or they have the same length and are character for character
 * identical. The attributes NamedNodeMaps are equal.
 * This is: they are both null , or they have the same
 * length and for each node that exists in one map there is a node
 * that exists in the other map and is equal, although not necessarily
 * at the same index. The childNodes NodeLists are equal. This
 * is: they are both null , or they have the same length
 * and contain equal nodes at the same index. Note that normalization
 * can affect equality; to avoid this, nodes should be normalized
 * before being compared. For two DocumentType nodes to be
 * equal, the following conditions must also be satisfied: The following string attributes are equal: publicId , systemId , internalSubset . The entities NamedNodeMaps are
 * equal. The notations NamedNodeMaps are
 * equal. On the other hand, the following do not affect equality: the ownerDocument , baseURI , and parentNode attributes, the specified attribute for Attr nodes, the schemaTypeInfo attribute for Attr and Element nodes, the Text.isElementContentWhitespace attribute for Text nodes, as well as
 * any user data or event listeners registered on the nodes. Note: As a general rule, anything not mentioned in the
 * description above is not significant in consideration of equality
 * checking. Note that future versions of this specification may take
 * into account more attributes and implementations conform to this
 * specification are expected to be updated accordingly.
 * @param {Node} arg of type Node The node to compare equality with.
 * @return boolean Returns true if the nodes are equal, false otherwise.
 * @type Boolean
 */
isEqualNode: function(arg) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Returns whether this node is the same node as
 * the given one. This method provides a way to determine whether two Node references returned by the implementation
 * reference the same object. When two Node references
 * are references to the same object, even if through a proxy, the
 * references may be used completely interchangeably, such that all
 * attributes have the same values and calling the same DOM method on
 * either reference always has exactly the same effect.
 * @param {Node} other of type Node The node to test against.
 * @return boolean Returns true if the nodes are the same, false otherwise.
 * @type Boolean
 */
isSameNode: function(other) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Tests whether the DOM implementation implements
 * a specific feature and that feature is supported by this node, as
 * specified in DOM Features .
 * @param {String} feature of type DOMString The name of the feature to test.
 * @param {String} version of type DOMString This is the version number of the feature to test.
 * @return boolean Returns true if the specified feature is supported
 * on this node, false otherwise.
 * @type Boolean
 */
isSupported: function(feature, version) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * The last child of this node. If there is no such node, this
 * returns null.
 * @type Node
 */
lastChild: undefined,
/**
 * Returns the local part of the qualified name of this
 * node.
 * For nodes of any type other than ELEMENT_NODE and
 * ATTRIBUTE_NODE and nodes created with a DOM Level 1
 * method, such as Document.createElement(),
 * this is always null.
 * @type String
 */
localName: undefined, // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
/**
 * Look up the namespace URI associated to the
 * given prefix, starting from this node. See Namespace URI
 * Lookup for details on the algorithm used by this method.
 * @param {String} prefix of type DOMString The prefix to look for. If this parameter is null ,
 * the method will return the default namespace URI if any.
 * @return DOMString Returns the associated namespace URI or null if
 * none is found.
 * @type String
 */
lookupNamespaceURI: function(prefix) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Look up the prefix associated to the given
 * namespace URI, starting from this node. The default namespace
 * declarations are ignored by this method. See Namespace
 * Prefix Lookup for details on the algorithm used by this method.
 * @param {String} namespaceURI of type DOMString The namespace URI to look for.
 * @return DOMString Returns an associated namespace prefix if found or null if none is found. If more than one prefix are
 * associated to the namespace prefix, the returned namespace prefix
 * is implementation dependent.
 * @type String
 */
lookupPrefix: function(namespaceURI) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * The namespace URI
 * of this node, or null if it is unspecified (see
 * XML
 * Namespaces).
 * This is not a computed value that is the result of a namespace
 * lookup based on an examination of the namespace declarations in
 * scope. It is merely the namespace URI given at creation time.
 * For nodes of any type other than ELEMENT_NODE and
 * ATTRIBUTE_NODE and nodes created with a DOM Level 1
 * method, such as Document.createElement(),
 * this is always null.
 * Note: Per the Namespaces in XML Specification
 * [XML Namespaces] an
 * attribute does not inherit its namespace from the element it is
 * attached to. If an attribute is not explicitly given a namespace,
 * it simply has no namespace.
 * @type String
 */
namespaceURI: undefined, // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
/**
 * The node immediately following this node. If there is no such
 * node, this returns null.
 * @type Node
 */
nextSibling: undefined,
/**
 * The name of this node, depending on its type; see the table
 * above.
 * @type String
 */
nodeName: undefined,
/**
 * A code representing the type of the underlying object, as
 * defined above.
 * @type Number
 */
nodeType: undefined,
/**
 * The value of this node, depending on its type; see the table
 * above. When it is defined to be null, setting it has
 * no effect, including if the node is read-only.
 * Exceptions on setting
 * @type String
 */
nodeValue: undefined,
/**
 * Puts all Text nodes in the full
 * depth of the sub-tree underneath this Node , including
 * attribute nodes, into a "normal" form where only structure (e.g.,
 * elements, comments, processing instructions, CDATA sections, and
 * entity references) separates Text nodes, i.e., there
 * are neither adjacent Text nodes nor empty Text nodes. This can be
 * used to ensure that the DOM view of a document is the same as if it
 * were saved and re-loaded, and is useful when operations (such as
 * XPointer [ XPointer ] lookups) that
 * depend on a particular document tree structure are to be used. If
 * the parameter " normalize-characters "
 * of the DOMConfiguration object attached to the Node.ownerDocument is true , this method will also fully normalize the
 * characters of the Text nodes. Note: In cases where the document contains CDATASections , the
 * normalize operation alone may not be sufficient, since XPointers do
 * not differentiate between Text nodes and CDATASection nodes.
 */
normalize: function() { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * The Document
 * object associated with this node. This is also the Document
 * object used to create new nodes. When this node is a Document or a
 * DocumentType
 * which is not used with any Document yet, this is
 * null.
 * @type Document
 */
ownerDocument: undefined,
/**
 * The parent of this node.
 * All nodes, except Attr, Document, DocumentFragment, Entity, and Notation may have a
 * parent. However, if a node has just been created and not yet added
 * to the tree, or if it has been removed from the tree, this is
 * null.
 * @type Node
 */
parentNode: undefined,
/**
 * The namespace
 * prefix of this node, or null if it is unspecified.
 * When it is defined to be null, setting it has no
 * effect, including if the node is read-only.
 * Note that setting this attribute, when permitted, changes the
 * nodeName attribute, which holds the qualified name, as well as the
 * tagName and name attributes of the
 * Element and
 * Attr interfaces,
 * when applicable.
 * Setting the prefix to null makes it unspecified,
 * setting it to an empty string is implementation dependent.
 * Note also that changing the prefix of an attribute that is known to
 * have a default value, does not make a new attribute with the
 * default value and the original prefix appear, since the
 * namespaceURI and localName do not
 * change.
 * For nodes of any type other than ELEMENT_NODE and
 * ATTRIBUTE_NODE and nodes created with a DOM Level 1
 * method, such as createElement from the Document interface, this is
 * always null.
 * Exceptions on setting
 * @type String
 */
prefix: undefined, // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
/**
 * The node immediately preceding this node. If there is no such
 * node, this returns null.
 * @type Node
 */
previousSibling: undefined,
/**
 * Removes the child node indicated by oldChild from the list of children, and returns it.
 * @param {Node} oldChild of type Node The node being removed.
 * @return Node The node removed.
 * @type Node
 */
removeChild: function(oldChild) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Replaces the child node oldChild with newChild in the list of children, and returns the oldChild node. If newChild is a DocumentFragment object, oldChild is replaced by all of the DocumentFragment children, which are inserted in the same order. If the newChild is already in the tree, it is first removed. Note: Replacing a node with itself is implementation
 * dependent.
 * @param {Node} newChild of type Node The new node to put in the child list.
 * @param {Node} oldChild of type Node The node being replaced in the list.
 * @return Node The node replaced.
 * @type Node
 */
replaceChild: function(newChild, oldChild) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * Associate an object to a key on this node. The
 * object can later be retrieved from this node by calling getUserData with the same key.
 * @param {String} key of type DOMString The key to associate the object to.
 * @param {any} data of type DOMUserData The object to associate to the given key, or null to remove any existing association to that key.
 * @param {UserDataHandler} handler of type UserDataHandler The handler to associate to that key, or null .
 * @return DOMUserData Returns the DOMUserData previously
 * associated to the given key on this node, or null if
 * there was none.
 * @type any
 */
setUserData: function(key, data, handler) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
 * This attribute returns the text content of this node and its
 * descendants. When it is defined to be null, setting it
 * has no effect. On setting, any possible children this node may have
 * are removed and, if it the new string is not empty or
 * null, replaced by a single Text node containing the
 * string this attribute is set to.
 * On getting, no serialization is performed, the returned string does
 * not contain any markup. No whitespace normalization is performed
 * and the returned string does not contain the white spaces in
 * element content (see the attribute Text.isElementContentWhitespace).
 * Similarly, on setting, no parsing is performed either, the input
 * string is taken as pure textual content.
 * The string returned is made of the text content of this node
 * depending on its type, as defined below:
 * @type String
 */
textContent: undefined, // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
};

