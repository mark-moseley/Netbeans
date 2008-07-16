<?php

// Start of SPL v.0.2

interface RecursiveIterator extends Iterator, Traversable {

	abstract public function hasChildren () {}

	abstract public function getChildren () {}

	abstract public function current () {}

	abstract public function next () {}

	abstract public function key () {}

	abstract public function valid () {}

	abstract public function rewind () {}

}

class RecursiveIteratorIterator implements Iterator, Traversable, OuterIterator {
	const LEAVES_ONLY = 0;
	const SELF_FIRST = 1;
	const CHILD_FIRST = 2;
	const CATCH_GET_CHILD = 16;


	/**
	 * @param iterator Traversable
	 * @param mode[optional]
	 * @param flags[optional]
	 */
	public function __construct (Traversable $iterator, $mode, $flags) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getDepth () {}

	/**
	 * @param level[optional]
	 */
	public function getSubIterator ($level) {}

	public function getInnerIterator () {}

	public function beginIteration () {}

	public function endIteration () {}

	public function callHasChildren () {}

	public function callGetChildren () {}

	public function beginChildren () {}

	public function endChildren () {}

	public function nextElement () {}

	/**
	 * @param max_depth[optional]
	 */
	public function setMaxDepth ($max_depth) {}

	public function getMaxDepth () {}

}

interface OuterIterator extends Iterator, Traversable {

	abstract public function getInnerIterator () {}

	abstract public function current () {}

	abstract public function next () {}

	abstract public function key () {}

	abstract public function valid () {}

	abstract public function rewind () {}

}

class IteratorIterator implements Iterator, Traversable, OuterIterator {

	/**
	 * @param iterator Traversable
	 */
	public function __construct (Traversable $iterator) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

}

class FilterIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator Iterator
	 */
	public function __construct (Iterator $iterator) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

	abstract public function accept () {}

}

class RecursiveFilterIterator extends FilterIterator implements Iterator, Traversable, OuterIterator, RecursiveIterator {

	/**
	 * @param iterator RecursiveIterator
	 */
	public function __construct (RecursiveIterator $iterator) {}

	public function hasChildren () {}

	public function getChildren () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

	abstract public function accept () {}

}

class ParentIterator extends RecursiveFilterIterator implements RecursiveIterator, OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator RecursiveIterator
	 */
	public function __construct (RecursiveIterator $iterator) {}

	public function accept () {}

	public function hasChildren () {}

	public function getChildren () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

}

interface Countable  {

	abstract public function count () {}

}

interface SeekableIterator extends Iterator, Traversable {

	/**
	 * @param position
	 */
	abstract public function seek ($position) {}

	abstract public function current () {}

	abstract public function next () {}

	abstract public function key () {}

	abstract public function valid () {}

	abstract public function rewind () {}

}

class LimitIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator Iterator
	 * @param offset[optional]
	 * @param count[optional]
	 */
	public function __construct (Iterator $iterator, $offset, $count) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	/**
	 * @param position
	 */
	public function seek ($position) {}

	public function getPosition () {}

	public function getInnerIterator () {}

}

class CachingIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator, ArrayAccess, Countable {
	const CALL_TOSTRING = 1;
	const CATCH_GET_CHILD = 16;
	const TOSTRING_USE_KEY = 2;
	const TOSTRING_USE_CURRENT = 4;
	const TOSTRING_USE_INNER = 8;
	const FULL_CACHE = 256;


	/**
	 * @param iterator Iterator
	 * @param flags[optional]
	 */
	public function __construct (Iterator $iterator, $flags) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function hasNext () {}

	public function __toString () {}

	public function getInnerIterator () {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	/**
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * @param index
	 */
	public function offsetExists ($index) {}

	public function getCache () {}

	public function count () {}

}

class RecursiveCachingIterator extends CachingIterator implements Countable, ArrayAccess, Iterator, Traversable, OuterIterator, RecursiveIterator {
	const CALL_TOSTRING = 1;
	const CATCH_GET_CHILD = 16;
	const TOSTRING_USE_KEY = 2;
	const TOSTRING_USE_CURRENT = 4;
	const TOSTRING_USE_INNER = 8;
	const FULL_CACHE = 256;


	/**
	 * @param iterator Iterator
	 * @param flags[optional]
	 */
	public function __construct (Iterator $iterator, $flags) {}

	public function hasChildren () {}

	public function getChildren () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function hasNext () {}

	public function __toString () {}

	public function getInnerIterator () {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	/**
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * @param index
	 */
	public function offsetExists ($index) {}

	public function getCache () {}

	public function count () {}

}

class NoRewindIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator Iterator
	 */
	public function __construct (Iterator $iterator) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

}

class AppendIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	public function __construct () {}

	/**
	 * @param iterator Iterator
	 */
	public function append (Iterator $iterator) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

	public function getIteratorIndex () {}

	public function getArrayIterator () {}

}

class InfiniteIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator Iterator
	 */
	public function __construct (Iterator $iterator) {}

	public function next () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function getInnerIterator () {}

}

class RegexIterator extends FilterIterator implements Iterator, Traversable, OuterIterator {
	const USE_KEY = 1;
	const MATCH = 0;
	const GET_MATCH = 1;
	const ALL_MATCHES = 2;
	const SPLIT = 3;
	const REPLACE = 4;

	public $replacement;


	/**
	 * @param iterator Iterator
	 * @param regex
	 * @param mode[optional]
	 * @param flags[optional]
	 * @param preg_flags[optional]
	 */
	public function __construct (Iterator $iterator, $regex, $mode, $flags, $preg_flags) {}

	public function accept () {}

	public function getMode () {}

	/**
	 * @param mode
	 */
	public function setMode ($mode) {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function getPregFlags () {}

	/**
	 * @param preg_flags
	 */
	public function setPregFlags ($preg_flags) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

}

class RecursiveRegexIterator extends RegexIterator implements OuterIterator, Traversable, Iterator, RecursiveIterator {
	const USE_KEY = 1;
	const MATCH = 0;
	const GET_MATCH = 1;
	const ALL_MATCHES = 2;
	const SPLIT = 3;
	const REPLACE = 4;

	public $replacement;


	/**
	 * @param iterator RecursiveIterator
	 * @param regex
	 * @param mode[optional]
	 * @param flags[optional]
	 * @param preg_flags[optional]
	 */
	public function __construct (RecursiveIterator $iterator, $regex, $mode, $flags, $preg_flags) {}

	public function hasChildren () {}

	public function getChildren () {}

	public function accept () {}

	public function getMode () {}

	/**
	 * @param mode
	 */
	public function setMode ($mode) {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function getPregFlags () {}

	/**
	 * @param preg_flags
	 */
	public function setPregFlags ($preg_flags) {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function getInnerIterator () {}

}

class EmptyIterator implements Iterator, Traversable {

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

}

class ArrayObject implements IteratorAggregate, Traversable, ArrayAccess, Countable {
	const STD_PROP_LIST = 1;
	const ARRAY_AS_PROPS = 2;


	/**
	 * @param array
	 */
	public function __construct ($array) {}

	/**
	 * @param index
	 */
	public function offsetExists ($index) {}

	/**
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * @param value
	 */
	public function append ($value) {}

	public function getArrayCopy () {}

	public function count () {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function asort () {}

	public function ksort () {}

	/**
	 * @param cmp_function
	 */
	public function uasort ($cmp_function) {}

	/**
	 * @param cmp_function
	 */
	public function uksort ($cmp_function) {}

	public function natsort () {}

	public function natcasesort () {}

	public function getIterator () {}

	/**
	 * @param array
	 */
	public function exchangeArray ($array) {}

	/**
	 * @param iteratorClass
	 */
	public function setIteratorClass ($iteratorClass) {}

	public function getIteratorClass () {}

}

class ArrayIterator implements Iterator, Traversable, ArrayAccess, SeekableIterator, Countable {
	const STD_PROP_LIST = 1;
	const ARRAY_AS_PROPS = 2;


	/**
	 * @param array
	 */
	public function __construct ($array) {}

	/**
	 * @param index
	 */
	public function offsetExists ($index) {}

	/**
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * @param value
	 */
	public function append ($value) {}

	public function getArrayCopy () {}

	public function count () {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function asort () {}

	public function ksort () {}

	/**
	 * @param cmp_function
	 */
	public function uasort ($cmp_function) {}

	/**
	 * @param cmp_function
	 */
	public function uksort ($cmp_function) {}

	public function natsort () {}

	public function natcasesort () {}

	public function rewind () {}

	public function current () {}

	public function key () {}

	public function next () {}

	public function valid () {}

	/**
	 * @param position
	 */
	public function seek ($position) {}

}

class RecursiveArrayIterator extends ArrayIterator implements SeekableIterator, ArrayAccess, Traversable, Iterator, RecursiveIterator {

	public function hasChildren () {}

	public function getChildren () {}

	/**
	 * @param array
	 */
	public function __construct ($array) {}

	/**
	 * @param index
	 */
	public function offsetExists ($index) {}

	/**
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * @param value
	 */
	public function append ($value) {}

	public function getArrayCopy () {}

	public function count () {}

	public function getFlags () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function asort () {}

	public function ksort () {}

	/**
	 * @param cmp_function
	 */
	public function uasort ($cmp_function) {}

	/**
	 * @param cmp_function
	 */
	public function uksort ($cmp_function) {}

	public function natsort () {}

	public function natcasesort () {}

	public function rewind () {}

	public function current () {}

	public function key () {}

	public function next () {}

	public function valid () {}

	/**
	 * @param position
	 */
	public function seek ($position) {}

}

class SplFileInfo  {

	/**
	 * @param file_name
	 */
	public function __construct ($file_name) {}

	public function getPath () {}

	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	public function getPathname () {}

	public function getPerms () {}

	public function getInode () {}

	public function getSize () {}

	public function getOwner () {}

	public function getGroup () {}

	public function getATime () {}

	public function getMTime () {}

	public function getCTime () {}

	public function getType () {}

	public function isWritable () {}

	public function isReadable () {}

	public function isExecutable () {}

	public function isFile () {}

	public function isDir () {}

	public function isLink () {}

	public function getLinkTarget () {}

	public function getRealPath () {}

	/**
	 * @param class_name[optional]
	 */
	public function getFileInfo ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function getPathInfo ($class_name) {}

	/**
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function openFile ($open_mode, $use_include_path, $context) {}

	/**
	 * @param class_name[optional]
	 */
	public function setFileClass ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function setInfoClass ($class_name) {}

	public function __toString () {}

}

class DirectoryIterator extends SplFileInfo implements Iterator, Traversable {

	/**
	 * @param path
	 */
	public function __construct ($path) {}

	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	public function isDot () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	public function __toString () {}

	public function getPath () {}

	public function getPathname () {}

	public function getPerms () {}

	public function getInode () {}

	public function getSize () {}

	public function getOwner () {}

	public function getGroup () {}

	public function getATime () {}

	public function getMTime () {}

	public function getCTime () {}

	public function getType () {}

	public function isWritable () {}

	public function isReadable () {}

	public function isExecutable () {}

	public function isFile () {}

	public function isDir () {}

	public function isLink () {}

	public function getLinkTarget () {}

	public function getRealPath () {}

	/**
	 * @param class_name[optional]
	 */
	public function getFileInfo ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function getPathInfo ($class_name) {}

	/**
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function openFile ($open_mode, $use_include_path, $context) {}

	/**
	 * @param class_name[optional]
	 */
	public function setFileClass ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function setInfoClass ($class_name) {}

}

class RecursiveDirectoryIterator extends DirectoryIterator implements Traversable, Iterator, RecursiveIterator {
	const CURRENT_MODE_MASK = 240;
	const CURRENT_AS_PATHNAME = 32;
	const CURRENT_AS_FILEINFO = 16;
	const CURRENT_AS_SELF = 0;
	const KEY_MODE_MASK = 3840;
	const KEY_AS_PATHNAME = 0;
	const KEY_AS_FILENAME = 256;
	const NEW_CURRENT_AND_KEY = 272;


	/**
	 * @param path
	 * @param flags[optional]
	 */
	public function __construct ($path, $flags) {}

	public function rewind () {}

	public function next () {}

	public function key () {}

	public function current () {}

	/**
	 * @param allow_links[optional]
	 */
	public function hasChildren ($allow_links) {}

	public function getChildren () {}

	public function getSubPath () {}

	public function getSubPathname () {}

	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	public function isDot () {}

	public function valid () {}

	public function __toString () {}

	public function getPath () {}

	public function getPathname () {}

	public function getPerms () {}

	public function getInode () {}

	public function getSize () {}

	public function getOwner () {}

	public function getGroup () {}

	public function getATime () {}

	public function getMTime () {}

	public function getCTime () {}

	public function getType () {}

	public function isWritable () {}

	public function isReadable () {}

	public function isExecutable () {}

	public function isFile () {}

	public function isDir () {}

	public function isLink () {}

	public function getLinkTarget () {}

	public function getRealPath () {}

	/**
	 * @param class_name[optional]
	 */
	public function getFileInfo ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function getPathInfo ($class_name) {}

	/**
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function openFile ($open_mode, $use_include_path, $context) {}

	/**
	 * @param class_name[optional]
	 */
	public function setFileClass ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function setInfoClass ($class_name) {}

}

class SplFileObject extends SplFileInfo implements RecursiveIterator, Traversable, Iterator, SeekableIterator {
	const DROP_NEW_LINE = 1;
	const READ_AHEAD = 2;
	const SKIP_EMPTY = 6;
	const READ_CSV = 8;


	/**
	 * @param file_name
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function __construct ($file_name, $open_mode, $use_include_path, $context) {}

	public function rewind () {}

	public function eof () {}

	public function valid () {}

	public function fgets () {}

	/**
	 * @param delimiter[optional]
	 * @param enclosure[optional]
	 */
	public function fgetcsv ($delimiter, $enclosure) {}

	/**
	 * @param delimiter[optional]
	 * @param enclosure[optional]
	 */
	public function setCsvControl ($delimiter, $enclosure) {}

	public function getCsvControl () {}

	/**
	 * @param operation
	 * @param wouldblock[optional]
	 */
	public function flock ($operation, &$wouldblock) {}

	public function fflush () {}

	public function ftell () {}

	/**
	 * @param pos
	 * @param whence[optional]
	 */
	public function fseek ($pos, $whence) {}

	public function fgetc () {}

	public function fpassthru () {}

	/**
	 * @param allowable_tags[optional]
	 */
	public function fgetss ($allowable_tags) {}

	/**
	 * @param format
	 */
	public function fscanf ($format) {}

	/**
	 * @param str
	 * @param length[optional]
	 */
	public function fwrite ($str, $length) {}

	public function fstat () {}

	/**
	 * @param size
	 */
	public function ftruncate ($size) {}

	public function current () {}

	public function key () {}

	public function next () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function getFlags () {}

	/**
	 * @param max_len
	 */
	public function setMaxLineLen ($max_len) {}

	public function getMaxLineLen () {}

	public function hasChildren () {}

	public function getChildren () {}

	/**
	 * @param line_pos
	 */
	public function seek ($line_pos) {}

	public function getCurrentLine () {}

	public function __toString () {}

	public function getPath () {}

	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	public function getPathname () {}

	public function getPerms () {}

	public function getInode () {}

	public function getSize () {}

	public function getOwner () {}

	public function getGroup () {}

	public function getATime () {}

	public function getMTime () {}

	public function getCTime () {}

	public function getType () {}

	public function isWritable () {}

	public function isReadable () {}

	public function isExecutable () {}

	public function isFile () {}

	public function isDir () {}

	public function isLink () {}

	public function getLinkTarget () {}

	public function getRealPath () {}

	/**
	 * @param class_name[optional]
	 */
	public function getFileInfo ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function getPathInfo ($class_name) {}

	/**
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function openFile ($open_mode, $use_include_path, $context) {}

	/**
	 * @param class_name[optional]
	 */
	public function setFileClass ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function setInfoClass ($class_name) {}

}

class SplTempFileObject extends SplFileObject implements SeekableIterator, Iterator, Traversable, RecursiveIterator {
	const DROP_NEW_LINE = 1;
	const READ_AHEAD = 2;
	const SKIP_EMPTY = 6;
	const READ_CSV = 8;


	/**
	 * @param max_memory[optional]
	 */
	public function __construct ($max_memory) {}

	public function rewind () {}

	public function eof () {}

	public function valid () {}

	public function fgets () {}

	/**
	 * @param delimiter[optional]
	 * @param enclosure[optional]
	 */
	public function fgetcsv ($delimiter, $enclosure) {}

	/**
	 * @param delimiter[optional]
	 * @param enclosure[optional]
	 */
	public function setCsvControl ($delimiter, $enclosure) {}

	public function getCsvControl () {}

	/**
	 * @param operation
	 * @param wouldblock[optional]
	 */
	public function flock ($operation, &$wouldblock) {}

	public function fflush () {}

	public function ftell () {}

	/**
	 * @param pos
	 * @param whence[optional]
	 */
	public function fseek ($pos, $whence) {}

	public function fgetc () {}

	public function fpassthru () {}

	/**
	 * @param allowable_tags[optional]
	 */
	public function fgetss ($allowable_tags) {}

	/**
	 * @param format
	 */
	public function fscanf ($format) {}

	/**
	 * @param str
	 * @param length[optional]
	 */
	public function fwrite ($str, $length) {}

	public function fstat () {}

	/**
	 * @param size
	 */
	public function ftruncate ($size) {}

	public function current () {}

	public function key () {}

	public function next () {}

	/**
	 * @param flags
	 */
	public function setFlags ($flags) {}

	public function getFlags () {}

	/**
	 * @param max_len
	 */
	public function setMaxLineLen ($max_len) {}

	public function getMaxLineLen () {}

	public function hasChildren () {}

	public function getChildren () {}

	/**
	 * @param line_pos
	 */
	public function seek ($line_pos) {}

	public function getCurrentLine () {}

	public function __toString () {}

	public function getPath () {}

	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	public function getPathname () {}

	public function getPerms () {}

	public function getInode () {}

	public function getSize () {}

	public function getOwner () {}

	public function getGroup () {}

	public function getATime () {}

	public function getMTime () {}

	public function getCTime () {}

	public function getType () {}

	public function isWritable () {}

	public function isReadable () {}

	public function isExecutable () {}

	public function isFile () {}

	public function isDir () {}

	public function isLink () {}

	public function getLinkTarget () {}

	public function getRealPath () {}

	/**
	 * @param class_name[optional]
	 */
	public function getFileInfo ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function getPathInfo ($class_name) {}

	/**
	 * @param open_mode[optional]
	 * @param use_include_path[optional]
	 * @param context[optional]
	 */
	public function openFile ($open_mode, $use_include_path, $context) {}

	/**
	 * @param class_name[optional]
	 */
	public function setFileClass ($class_name) {}

	/**
	 * @param class_name[optional]
	 */
	public function setInfoClass ($class_name) {}

}

class SimpleXMLIterator extends SimpleXMLElement implements Traversable, RecursiveIterator, Iterator, Countable {

	public function rewind () {}

	public function valid () {}

	public function current () {}

	public function key () {}

	public function next () {}

	public function hasChildren () {}

	public function getChildren () {}

	public function count () {}

	/**
	 * Creates a new SimpleXMLElement object
	 * @link http://php.net/manual/en/function.simplexml-element-construct.php
	 */
	final public function __construct () {}

	/**
	 * Return a well-formed XML string based on SimpleXML element
	 * @link http://php.net/manual/en/function.simplexml-element-asXML.php
	 * @param filename string[optional] <p>
	 * If specified, the function writes the data to the file rather than
	 * returning it.
	 * </p>
	 * @return mixed If the filename isn't specified, this function
	 * returns a string on success and false on error. If the
	 * parameter is specified, it returns true if the file was written
	 * successfully and false otherwise.
	 * </p>
	 */
	public function asXML ($filename = null) {}

	public function saveXML () {}

	/**
	 * Runs XPath query on XML data
	 * @link http://php.net/manual/en/function.simplexml-element-xpath.php
	 * @param path string <p>
	 * An XPath path
	 * </p>
	 * @return array an array of SimpleXMLElement objects or false in
	 * case of an error.
	 * </p>
	 */
	public function xpath ($path) {}

	/**
	 * Creates a prefix/ns context for the next XPath query
	 * @link http://php.net/manual/en/function.simplexml-element-registerXPathNamespace.php
	 * @param prefix string <p>
	 * The namespace prefix to use in the XPath query for the namespace given in 
	 * ns.
	 * </p>
	 * @param ns string <p>
	 * The namespace to use for the XPath query. This must match a namespace in
	 * use by the XML document or the XPath query using 
	 * prefix will not return any results.
	 * </p>
	 * @return bool &return.success;
	 * </p>
	 */
	public function registerXPathNamespace ($prefix, $ns) {}

	/**
	 * Identifies an element's attributes
	 * @link http://php.net/manual/en/function.simplexml-element-attributes.php
	 * @param ns string[optional] <p>
	 * An optional namespace for the retrieved attributes
	 * </p>
	 * @param is_prefix bool[optional] <p>
	 * Default to false
	 * </p>
	 * @return SimpleXMLElement </p>
	 */
	public function attributes ($ns = null, $is_prefix = null) {}

	/**
	 * Finds children of given node
	 * @link http://php.net/manual/en/function.simplexml-element-children.php
	 * @param ns string[optional] <p>
	 * </p>
	 * @param is_prefix bool[optional] <p>
	 * Default to false
	 * </p>
	 * @return SimpleXMLElement </p>
	 */
	public function children ($ns = null, $is_prefix = null) {}

	/**
	 * Returns namespaces used in document
	 * @link http://php.net/manual/en/function.simplexml-element-getNamespaces.php
	 * @param recursive bool[optional] <p>
	 * If specified, returns all namespaces used in parent and child nodes. 
	 * Otherwise, returns only namespaces used in root node.
	 * </p>
	 * @return array The getNamespaces method returns an array of 
	 * namespace names with their associated URIs.
	 * </p>
	 */
	public function getNamespaces ($recursive = null) {}

	/**
	 * Returns namespaces declared in document
	 * @link http://php.net/manual/en/function.simplexml-element-getDocNamespaces.php
	 * @param recursive bool[optional] <p>
	 * If specified, returns all namespaces declared in parent and child nodes. 
	 * Otherwise, returns only namespaces declared in root node.
	 * </p>
	 * @return array The getDocNamespaces method returns an array 
	 * of namespace names with their associated URIs.
	 * </p>
	 */
	public function getDocNamespaces ($recursive = null) {}

	/**
	 * Gets the name of the XML element
	 * @link http://php.net/manual/en/function.simplexml-element-getName.php
	 * @return string The getName method returns as a string the 
	 * name of the XML tag referenced by the SimpleXMLElement object.
	 * </p>
	 */
	public function getName () {}

	/**
	 * Adds a child element to the XML node
	 * @link http://php.net/manual/en/function.simplexml-element-addChild.php
	 * @param name string <p>
	 * The name of the child element to add.
	 * </p>
	 * @param value string[optional] <p>
	 * If specified, the value of the child element.
	 * </p>
	 * @param namespace string[optional] <p>
	 * If specified, the namespace to which the child element belongs.
	 * </p>
	 * @return SimpleXMLElement The addChild method returns a SimpleXMLElement
	 * object representing the child added to the XML node.
	 * </p>
	 */
	public function addChild ($name, $value = null, $namespace = null) {}

	/**
	 * Adds an attribute to the SimpleXML element
	 * @link http://php.net/manual/en/function.simplexml-element-addAttribute.php
	 * @param name string <p>
	 * The name of the attribute to add.
	 * </p>
	 * @param value string <p>
	 * The value of the attribute.
	 * </p>
	 * @param namespace string[optional] <p>
	 * If specified, the namespace to which the attribute belongs.
	 * </p>
	 * @return void &return.void;
	 * </p>
	 */
	public function addAttribute ($name, $value, $namespace = null) {}

}

class LogicException extends Exception  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class BadFunctionCallException extends LogicException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class BadMethodCallException extends BadFunctionCallException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class DomainException extends LogicException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class InvalidArgumentException extends LogicException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class LengthException extends LogicException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class OutOfRangeException extends LogicException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class RuntimeException extends Exception  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class OutOfBoundsException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class OverflowException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class RangeException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class UnderflowException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

class UnexpectedValueException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	final private function __clone () {}

	/**
	 * @param message[optional]
	 * @param code[optional]
	 */
	public function __construct ($message, $code) {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

interface SplObserver  {

	/**
	 * @param SplSubject SplSubject
	 */
	abstract public function update (SplSubject $SplSubject) {}

}

interface SplSubject  {

	/**
	 * @param SplObserver SplObserver
	 */
	abstract public function attach (SplObserver $SplObserver) {}

	/**
	 * @param SplObserver SplObserver
	 */
	abstract public function detach (SplObserver $SplObserver) {}

	abstract public function notify () {}

}

class SplObjectStorage implements Countable, Iterator, Traversable, Serializable {

	/**
	 * @param object
	 */
	public function attach ($object) {}

	/**
	 * @param object
	 */
	public function detach ($object) {}

	/**
	 * @param object
	 */
	public function contains ($object) {}

	public function count () {}

	public function rewind () {}

	public function valid () {}

	public function key () {}

	public function current () {}

	public function next () {}

	/**
	 * @param serialized
	 */
	public function unserialize ($serialized) {}

	public function serialize () {}

}

/**
 * Return available SPL classes
 * @link http://php.net/manual/en/function.spl-classes.php
 * @return array 
 */
function spl_classes () {}

/**
 * Default implementation for __autoload()
 * @link http://php.net/manual/en/function.spl-autoload.php
 * @param class_name string <p>
 * </p>
 * @param file_extensions string[optional] <p>
 * By default it checks all include paths to
 * contain filenames built up by the lowercase class name appended by the
 * filename extensions .inc and .php. 
 * </p>
 * @return void &return.void;
 * </p>
 */
function spl_autoload ($class_name, $file_extensions = null) {}

/**
 * Register and return default file extensions for spl_autoload
 * @link http://php.net/manual/en/function.spl-autoload-extensions.php
 * @param file_extensions string[optional] <p>
 * When calling without an argument, it simply returns the current list
 * of extensions each separated by comma. To modify the list of file
 * extensions, simply invoke the functions with the new list of file
 * extensions to use in a single string with each extensions separated
 * by comma.
 * </p>
 * @return string A comma delimitated list of default file extensions for
 * spl_autoload.
 * </p>
 */
function spl_autoload_extensions ($file_extensions = null) {}

/**
 * Register given function as __autoload() implementation
 * @link http://php.net/manual/en/function.spl-autoload-register.php
 * @param autoload_function callback[optional] <p>
 * The autoload function being registered.
 * If no parameter is provided, then the default implementation of
 * spl_autoload will be registered. 
 * </p>
 * @return bool &return.success;
 * </p>
 */
function spl_autoload_register ($autoload_function = null) {}

/**
 * Unregister given function as __autoload() implementation
 * @link http://php.net/manual/en/function.spl-autoload-unregister.php
 * @param autoload_function mixed <p>
 * The autoload function being unregistered.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function spl_autoload_unregister ($autoload_function) {}

/**
 * Return all registered __autoload() functions
 * @link http://php.net/manual/en/function.spl-autoload-functions.php
 * @return array An array of all registered __autoload functions.
 * If the autoload stack is not activated then the return value is false.
 * If no function is registered the return value will be an empty array.
 * </p>
 */
function spl_autoload_functions () {}

/**
 * Try all registered __autoload() function to load the requested class
 * @link http://php.net/manual/en/function.spl-autoload-call.php
 * @param class_name string <p>
 * The class name being searched.
 * </p>
 * @return void &return.void;
 * </p>
 */
function spl_autoload_call ($class_name) {}

/**
 * Return the parent classes of the given class
 * @link http://php.net/manual/en/function.class-parents.php
 * @param class mixed <p>
 * An object (class instance) or a string (class name).
 * </p>
 * @param autoload bool[optional] <p>
 * Whether to allow this function to load the class automatically through
 * the __autoload magic
 * method. Defaults to true.
 * </p>
 * @return array An array on success, or false on error.
 * </p>
 */
function class_parents ($class, $autoload = null) {}

/**
 * Return the interfaces which are implemented by the given class
 * @link http://php.net/manual/en/function.class-implements.php
 * @param class mixed <p>
 * An object (class instance) or a string (class name).
 * </p>
 * @param autoload bool[optional] <p>
 * Whether to allow this function to load the class automatically through
 * the __autoload magic
 * method. Defaults to true.
 * </p>
 * @return array An array on success, or false on error.
 * </p>
 */
function class_implements ($class, $autoload = null) {}

/**
 * Return hash id for given object
 * @link http://php.net/manual/en/function.spl-object-hash.php
 * @param obj object 
 * @return string A string that is unique for each object and is always the same for
 * the same object.
 * </p>
 */
function spl_object_hash ($obj) {}

/**
 * Copy the iterator into an array
 * @link http://php.net/manual/en/function.iterator-to-array.php
 * @param iterator IteratorAggregate <p>
 * The iterator being counted.
 * </p>
 * @param use_keys bool[optional] <p>
 * </p>
 * @return array The number of elements in iterator.
 * </p>
 */
function iterator_to_array ($iterator, $use_keys = null) {}

/**
 * Count the elements in an iterator
 * @link http://php.net/manual/en/function.iterator-count.php
 * @param iterator IteratorAggregate <p>
 * The iterator being counted.
 * </p>
 * @return int The number of elements in iterator.
 * </p>
 */
function iterator_count ($iterator) {}

/**
 * @param iterator Traversable
 * @param function
 * @param args[optional]
 */
function iterator_apply (Traversable $iterator, $functionarray , $args = null) {}

// End of SPL v.0.2
?>
