<?php

// Start of xmlwriter v.0.1

/**
 * @link http://php.net/manual/en/ref.xmlwriter.php
 */
class XMLWriter  {

	/**
	 * Create new xmlwriter using source uri for output
	 * @link http://php.net/manual/en/function.xmlwriter-open-uri.php
	 * @param uri string <p>
	 * The URI of the resource for the output.
	 * </p>
	 * @return bool Object oriented style: &return.success;.
	 * </p>
	 * <p>
	 * Procedural style: Returns a new xmlwriter resource for later use with the
	 * xmlwriter functions on success, false on error.
	 * </p>
	 */
	public function openUri ($uri) {}

	/**
	 * Create new xmlwriter using memory for string output
	 * @link http://php.net/manual/en/function.xmlwriter-open-memory.php
	 * @return bool Object oriented style: &return.success;.
	 * </p>
	 * <p>
	 * Procedural style: Returns a new xmlwriter resource for later use with the
	 * xmlwriter functions on success, false on error.
	 * </p>
	 */
	public function openMemory () {}

	/**
	 * Toggle indentation on/off
	 * @link http://php.net/manual/en/function.xmlwriter-set-indent.php
	 * @param indent bool <p>
	 * Whether indentation is enabled. Defaults to false.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function setIndent ($indent) {}

	/**
	 * Set string used for indenting
	 * @link http://php.net/manual/en/function.xmlwriter-set-indent-string.php
	 * @param indentString string <p>
	 * The indentation string.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function setIndentString ($indentString) {}

	/**
	 * Create start comment
	 * @link http://php.net/manual/en/function.xmlwriter-start-comment.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function startComment () {}

	/**
	 * Create end comment
	 * @link http://php.net/manual/en/function.xmlwriter-end-comment.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endComment () {}

	/**
	 * Create start attribute
	 * @link http://php.net/manual/en/function.xmlwriter-start-attribute.php
	 * @param name string <p>
	 * The attribute name.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startAttribute ($name) {}

	/**
	 * End attribute
	 * @link http://php.net/manual/en/function.xmlwriter-end-attribute.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endAttribute () {}

	/**
	 * Write full attribute
	 * @link http://php.net/manual/en/function.xmlwriter-write-attribute.php
	 * @param name string <p>
	 * The name of the attribute.
	 * </p>
	 * @param value string <p>
	 * The value of the attribute.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeAttribute ($name, $value) {}

	/**
	 * Create start namespaced attribute
	 * @link http://php.net/manual/en/function.xmlwriter-start-attribute-ns.php
	 * @param prefix string <p>
	 * The namespace prefix.
	 * </p>
	 * @param name string <p>
	 * The attribute name.
	 * </p>
	 * @param uri string <p>
	 * The namespace URI.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startAttributeNs ($prefix, $name, $uri) {}

	/**
	 * Write full namespaced attribute
	 * @link http://php.net/manual/en/function.xmlwriter-write-attribute-ns.php
	 * @param prefix string <p>
	 * The namespace prefix.
	 * </p>
	 * @param name string <p>
	 * The attribute name.
	 * </p>
	 * @param uri string <p>
	 * The namespace URI.
	 * </p>
	 * @param content string <p>
	 * The attribute value.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeAttributeNs ($prefix, $name, $uri, $content) {}

	/**
	 * Create start element tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-element.php
	 * @param name string <p>
	 * The element name.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startElement ($name) {}

	/**
	 * End current element
	 * @link http://php.net/manual/en/function.xmlwriter-end-element.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endElement () {}

	/**
	 * End current element
	 * @link http://php.net/manual/en/function.xmlwriter-full-end-element.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function fullEndElement () {}

	/**
	 * Create start namespaced element tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-element-ns.php
	 * @param prefix string <p>
	 * The namespace prefix.
	 * </p>
	 * @param name string <p>
	 * The element name.
	 * </p>
	 * @param uri string <p>
	 * The namespace URI.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startElementNs ($prefix, $name, $uri) {}

	/**
	 * Write full element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-element.php
	 * @param name string <p>
	 * The element name.
	 * </p>
	 * @param content string[optional] <p>
	 * The element contents.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeElement ($name, $content = null) {}

	/**
	 * Write full namesapced element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-element-ns.php
	 * @param prefix string <p>
	 * The namespace prefix.
	 * </p>
	 * @param name string <p>
	 * The element name.
	 * </p>
	 * @param uri string <p>
	 * The namespace URI.
	 * </p>
	 * @param content string[optional] <p>
	 * The element contents.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeElementNs ($prefix, $name, $uri, $content = null) {}

	/**
	 * Create start PI tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-pi.php
	 * @param target string <p>
	 * The target of the processing instruction.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startPi ($target) {}

	/**
	 * End current PI
	 * @link http://php.net/manual/en/function.xmlwriter-end-pi.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endPi () {}

	/**
	 * Writes a PI
	 * @link http://php.net/manual/en/function.xmlwriter-write-pi.php
	 * @param target string <p>
	 * The target of the processing instruction.
	 * </p>
	 * @param content string <p>
	 * The content of the processing instruction.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writePi ($target, $content) {}

	/**
	 * Create start CDATA tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-cdata.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function startCdata () {}

	/**
	 * End current CDATA
	 * @link http://php.net/manual/en/function.xmlwriter-end-cdata.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endCdata () {}

	/**
	 * Write full CDATA tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-cdata.php
	 * @param content string <p>
	 * The contents of the CDATA.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeCdata ($content) {}

	/**
	 * Write text
	 * @link http://php.net/manual/en/function.xmlwriter-text.php
	 * @param content string <p>
	 * The contents of the text.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function text ($content) {}

	/**
	 * Write a raw XML text
	 * @link http://php.net/manual/en/function.xmlwriter-write-raw.php
	 * @param content string <p>
	 * The text string to write.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeRaw ($content) {}

	/**
	 * Create document tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-document.php
	 * @param version string[optional] <p>
	 * The version number of the document as part of the XML declaration. 
	 * Defaults to 1.0.
	 * </p>
	 * @param encoding string[optional] <p>
	 * The encoding of the document as part of the XML declaration.
	 * &null; by default.
	 * </p>
	 * @param standalone string[optional] <p>
	 * yes or no.
	 * &null; by default.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startDocument ($version = null, $encoding = null, $standalone = null) {}

	/**
	 * End current document
	 * @link http://php.net/manual/en/function.xmlwriter-end-document.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endDocument () {}

	/**
	 * Write full comment tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-comment.php
	 * @param content string <p>
	 * The contents of the comment.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeComment ($content) {}

	/**
	 * Create start DTD tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd.php
	 * @param qualifiedName string <p>
	 * The qualified name of the document type to create.
	 * </p>
	 * @param publicId string[optional] <p>
	 * The external subset public identifier.
	 * </p>
	 * @param systemId string[optional] <p>
	 * The external subset system identifier.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startDtd ($qualifiedName, $publicId = null, $systemId = null) {}

	/**
	 * End current DTD
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endDtd () {}

	/**
	 * Write full DTD tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd.php
	 * @param name string <p>
	 * The DTD name.
	 * </p>
	 * @param publicId string[optional] <p>
	 * The external subset public identifier.
	 * </p>
	 * @param systemId string[optional] <p>
	 * The external subset system identifier.
	 * </p>
	 * @param subset string[optional] <p>
	 * The content of the DTD.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeDtd ($name, $publicId = null, $systemId = null, $subset = null) {}

	/**
	 * Create start DTD element
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-element.php
	 * @param qualifiedName string <p>
	 * The qualified name of the document type to create.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startDtdElement ($qualifiedName) {}

	/**
	 * End current DTD element
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-element.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endDtdElement () {}

	/**
	 * Write full DTD element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-element.php
	 * @param name string <p>
	 * The name of the DTD element.
	 * </p>
	 * @param content string <p>
	 * The content of the element.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeDtdElement ($name, $content) {}

	/**
	 * Create start DTD AttList
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-attlist.php
	 * @param name string <p>
	 * The attribute list name.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startDtdAttlist ($name) {}

	/**
	 * End current DTD AttList
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-attlist.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endDtdAttlist () {}

	/**
	 * Write full DTD AttList tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-attlist.php
	 * @param name string <p>
	 * The name of the DTD attribute list.
	 * </p>
	 * @param content string <p>
	 * The content of the DTD attribute list.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeDtdAttlist ($name, $content) {}

	/**
	 * Create start DTD Entity
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-entity.php
	 * @param name string <p>
	 * The name of the entity.
	 * </p>
	 * @param isparam bool <p>
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function startDtdEntity ($name, $isparam) {}

	/**
	 * End current DTD Entity
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-entity.php
	 * @return bool &return.success;
	 * </p>
	 */
	public function endDtdEntity () {}

	/**
	 * Write full DTD Entity tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-entity.php
	 * @param name string <p>
	 * The name of the entity.
	 * </p>
	 * @param content string <p>
	 * The content of the entity.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function writeDtdEntity ($name, $content) {}

	/**
	 * Returns current buffer
	 * @link http://php.net/manual/en/function.xmlwriter-output-memory.php
	 * @param flush bool[optional] <p>
	 * Whether to flush the output buffer or no. Default is true.
	 * </p>
	 * @return string the current buffer as a string.
	 * </p>
	 */
	public function outputMemory ($flush = null) {}

	/**
	 * Flush current buffer
	 * @link http://php.net/manual/en/function.xmlwriter-flush.php
	 * @param empty bool[optional] <p>
	 * Whether to empty the buffer or no. Default is true.
	 * </p>
	 * @return mixed If you opened the writer in memory, this function returns the generated XML buffer,
	 * Else, if using URI, this function will write the buffer and return the number of 
	 * written bytes.
	 * </p>
	 */
	public function flush ($empty = null) {}

}

/**
 * Create new xmlwriter using source uri for output
 * @link http://php.net/manual/en/function.xmlwriter-open-uri.php
 * @param uri string <p>
 * The URI of the resource for the output.
 * </p>
 * @return bool Object oriented style: &return.success;.
 * </p>
 * <p>
 * Procedural style: Returns a new xmlwriter resource for later use with the
 * xmlwriter functions on success, false on error.
 * </p>
 */
function xmlwriter_open_uri ($uri) {}

/**
 * Create new xmlwriter using memory for string output
 * @link http://php.net/manual/en/function.xmlwriter-open-memory.php
 * @return bool Object oriented style: &return.success;.
 * </p>
 * <p>
 * Procedural style: Returns a new xmlwriter resource for later use with the
 * xmlwriter functions on success, false on error.
 * </p>
 */
function xmlwriter_open_memory () {}

/**
 * Toggle indentation on/off
 * @link http://php.net/manual/en/function.xmlwriter-set-indent.php
 * @param indent bool <p>
 * Whether indentation is enabled. Defaults to false.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_set_indent ($indent) {}

/**
 * Set string used for indenting
 * @link http://php.net/manual/en/function.xmlwriter-set-indent-string.php
 * @param indentString string <p>
 * The indentation string.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_set_indent_string ($indentString) {}

/**
 * Create start comment
 * @link http://php.net/manual/en/function.xmlwriter-start-comment.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_comment () {}

/**
 * Create end comment
 * @link http://php.net/manual/en/function.xmlwriter-end-comment.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_comment () {}

/**
 * Create start attribute
 * @link http://php.net/manual/en/function.xmlwriter-start-attribute.php
 * @param name string <p>
 * The attribute name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_attribute ($name) {}

/**
 * End attribute
 * @link http://php.net/manual/en/function.xmlwriter-end-attribute.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_attribute () {}

/**
 * Write full attribute
 * @link http://php.net/manual/en/function.xmlwriter-write-attribute.php
 * @param name string <p>
 * The name of the attribute.
 * </p>
 * @param value string <p>
 * The value of the attribute.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_attribute ($name, $value) {}

/**
 * Create start namespaced attribute
 * @link http://php.net/manual/en/function.xmlwriter-start-attribute-ns.php
 * @param prefix string <p>
 * The namespace prefix.
 * </p>
 * @param name string <p>
 * The attribute name.
 * </p>
 * @param uri string <p>
 * The namespace URI.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_attribute_ns ($prefix, $name, $uri) {}

/**
 * Write full namespaced attribute
 * @link http://php.net/manual/en/function.xmlwriter-write-attribute-ns.php
 * @param prefix string <p>
 * The namespace prefix.
 * </p>
 * @param name string <p>
 * The attribute name.
 * </p>
 * @param uri string <p>
 * The namespace URI.
 * </p>
 * @param content string <p>
 * The attribute value.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_attribute_ns ($prefix, $name, $uri, $content) {}

/**
 * Create start element tag
 * @link http://php.net/manual/en/function.xmlwriter-start-element.php
 * @param name string <p>
 * The element name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_element ($name) {}

/**
 * End current element
 * @link http://php.net/manual/en/function.xmlwriter-end-element.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_element () {}

/**
 * End current element
 * @link http://php.net/manual/en/function.xmlwriter-full-end-element.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_full_end_element () {}

/**
 * Create start namespaced element tag
 * @link http://php.net/manual/en/function.xmlwriter-start-element-ns.php
 * @param prefix string <p>
 * The namespace prefix.
 * </p>
 * @param name string <p>
 * The element name.
 * </p>
 * @param uri string <p>
 * The namespace URI.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_element_ns ($prefix, $name, $uri) {}

/**
 * Write full element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-element.php
 * @param name string <p>
 * The element name.
 * </p>
 * @param content string[optional] <p>
 * The element contents.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_element ($name, $content = null) {}

/**
 * Write full namesapced element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-element-ns.php
 * @param prefix string <p>
 * The namespace prefix.
 * </p>
 * @param name string <p>
 * The element name.
 * </p>
 * @param uri string <p>
 * The namespace URI.
 * </p>
 * @param content string[optional] <p>
 * The element contents.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_element_ns ($prefix, $name, $uri, $content = null) {}

/**
 * Create start PI tag
 * @link http://php.net/manual/en/function.xmlwriter-start-pi.php
 * @param target string <p>
 * The target of the processing instruction.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_pi ($target) {}

/**
 * End current PI
 * @link http://php.net/manual/en/function.xmlwriter-end-pi.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_pi () {}

/**
 * Writes a PI
 * @link http://php.net/manual/en/function.xmlwriter-write-pi.php
 * @param target string <p>
 * The target of the processing instruction.
 * </p>
 * @param content string <p>
 * The content of the processing instruction.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_pi ($target, $content) {}

/**
 * Create start CDATA tag
 * @link http://php.net/manual/en/function.xmlwriter-start-cdata.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_cdata () {}

/**
 * End current CDATA
 * @link http://php.net/manual/en/function.xmlwriter-end-cdata.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_cdata () {}

/**
 * Write full CDATA tag
 * @link http://php.net/manual/en/function.xmlwriter-write-cdata.php
 * @param content string <p>
 * The contents of the CDATA.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_cdata ($content) {}

/**
 * Write text
 * @link http://php.net/manual/en/function.xmlwriter-text.php
 * @param content string <p>
 * The contents of the text.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_text ($content) {}

/**
 * Write a raw XML text
 * @link http://php.net/manual/en/function.xmlwriter-write-raw.php
 * @param content string <p>
 * The text string to write.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_raw ($content) {}

/**
 * Create document tag
 * @link http://php.net/manual/en/function.xmlwriter-start-document.php
 * @param version string[optional] <p>
 * The version number of the document as part of the XML declaration. 
 * Defaults to 1.0.
 * </p>
 * @param encoding string[optional] <p>
 * The encoding of the document as part of the XML declaration.
 * &null; by default.
 * </p>
 * @param standalone string[optional] <p>
 * yes or no.
 * &null; by default.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_document ($version = null, $encoding = null, $standalone = null) {}

/**
 * End current document
 * @link http://php.net/manual/en/function.xmlwriter-end-document.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_document () {}

/**
 * Write full comment tag
 * @link http://php.net/manual/en/function.xmlwriter-write-comment.php
 * @param content string <p>
 * The contents of the comment.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_comment ($content) {}

/**
 * Create start DTD tag
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd.php
 * @param qualifiedName string <p>
 * The qualified name of the document type to create.
 * </p>
 * @param publicId string[optional] <p>
 * The external subset public identifier.
 * </p>
 * @param systemId string[optional] <p>
 * The external subset system identifier.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_dtd ($qualifiedName, $publicId = null, $systemId = null) {}

/**
 * End current DTD
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_dtd () {}

/**
 * Write full DTD tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd.php
 * @param name string <p>
 * The DTD name.
 * </p>
 * @param publicId string[optional] <p>
 * The external subset public identifier.
 * </p>
 * @param systemId string[optional] <p>
 * The external subset system identifier.
 * </p>
 * @param subset string[optional] <p>
 * The content of the DTD.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_dtd ($name, $publicId = null, $systemId = null, $subset = null) {}

/**
 * Create start DTD element
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-element.php
 * @param qualifiedName string <p>
 * The qualified name of the document type to create.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_dtd_element ($qualifiedName) {}

/**
 * End current DTD element
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-element.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_dtd_element () {}

/**
 * Write full DTD element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-element.php
 * @param name string <p>
 * The name of the DTD element.
 * </p>
 * @param content string <p>
 * The content of the element.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_dtd_element ($name, $content) {}

/**
 * Create start DTD AttList
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-attlist.php
 * @param name string <p>
 * The attribute list name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_dtd_attlist ($name) {}

/**
 * End current DTD AttList
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-attlist.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_dtd_attlist () {}

/**
 * Write full DTD AttList tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-attlist.php
 * @param name string <p>
 * The name of the DTD attribute list.
 * </p>
 * @param content string <p>
 * The content of the DTD attribute list.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_dtd_attlist ($name, $content) {}

/**
 * Create start DTD Entity
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-entity.php
 * @param name string <p>
 * The name of the entity.
 * </p>
 * @param isparam bool <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_start_dtd_entity ($name, $isparam) {}

/**
 * End current DTD Entity
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-entity.php
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_end_dtd_entity () {}

/**
 * Write full DTD Entity tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-entity.php
 * @param name string <p>
 * The name of the entity.
 * </p>
 * @param content string <p>
 * The content of the entity.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function xmlwriter_write_dtd_entity ($name, $content) {}

/**
 * Returns current buffer
 * @link http://php.net/manual/en/function.xmlwriter-output-memory.php
 * @param flush bool[optional] <p>
 * Whether to flush the output buffer or no. Default is true.
 * </p>
 * @return string the current buffer as a string.
 * </p>
 */
function xmlwriter_output_memory ($flush = null) {}

/**
 * Flush current buffer
 * @link http://php.net/manual/en/function.xmlwriter-flush.php
 * @param empty bool[optional] <p>
 * Whether to empty the buffer or no. Default is true.
 * </p>
 * @return mixed If you opened the writer in memory, this function returns the generated XML buffer,
 * Else, if using URI, this function will write the buffer and return the number of 
 * written bytes.
 * </p>
 */
function xmlwriter_flush ($empty = null) {}

// End of xmlwriter v.0.1
?>
