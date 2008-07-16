<?php

/**
 * Gets the version of the current Zend engine
 * @link http://php.net/manual/en/function.zend-version.php
 * @return string the Zend Engine version number, as a string.
 * </p>
 */
function zend_version () {}

/**
 * Returns the number of arguments passed to the function
 * @link http://php.net/manual/en/function.func-num-args.php
 * @return int the number of arguments passed into the current user-defined
 * function.
 * </p>
 */
function func_num_args () {}

/**
 * Return an item from the argument list
 * @link http://php.net/manual/en/function.func-get-arg.php
 * @param arg_num int <p>
 * The argument offset. Function arguments are counted starting from
 * zero.
 * </p>
 * @return mixed the specified argument, or false on error.
 * </p>
 */
function func_get_arg ($arg_num) {}

/**
 * Returns an array comprising a function's argument list
 * @link http://php.net/manual/en/function.func-get-args.php
 * @return array an array in which each element is a copy of the corresponding
 * member of the current user-defined function's argument list. 
 * </p>
 */
function func_get_args () {}

/**
 * Get string length
 * @link http://php.net/manual/en/function.strlen.php
 * @param string string <p>
 * The string being measured for length.
 * </p>
 * @return int The length of the string on success, 
 * and 0 if the string is empty.
 * </p>
 */
function strlen ($string) {}

/**
 * Binary safe string comparison
 * @link http://php.net/manual/en/function.strcmp.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @return int &lt; 0 if str1 is less than
 * str2; &gt; 0 if str1
 * is greater than str2, and 0 if they are
 * equal.
 * </p>
 */
function strcmp ($str1, $str2) {}

/**
 * Binary safe string comparison of the first n characters
 * @link http://php.net/manual/en/function.strncmp.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @param len int <p>
 * Number of characters to use in the comparison.
 * </p>
 * @return int &lt; 0 if str1 is less than
 * str2; &gt; 0 if str1
 * is greater than str2, and 0 if they are
 * equal.
 * </p>
 */
function strncmp ($str1, $str2, $len) {}

/**
 * Binary safe case-insensitive string comparison
 * @link http://php.net/manual/en/function.strcasecmp.php
 * @param str1 string <p>
 * The first string
 * </p>
 * @param str2 string <p>
 * The second string
 * </p>
 * @return int &lt; 0 if str1 is less than
 * str2; &gt; 0 if str1
 * is greater than str2, and 0 if they are
 * equal.
 * </p>
 */
function strcasecmp ($str1, $str2) {}

/**
 * Binary safe case-insensitive string comparison of the first n characters
 * @link http://php.net/manual/en/function.strncasecmp.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @param len int <p>
 * The length of strings to be used in the comparison.
 * </p>
 * @return int &lt; 0 if str1 is less than
 * str2; &gt; 0 if str1 is
 * greater than str2, and 0 if they are equal.
 * </p>
 */
function strncasecmp ($str1, $str2, $len) {}

/**
 * Return the current key and value pair from an array and advance the array cursor
 * @link http://php.net/manual/en/function.each.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return array the current key and value pair from the array
 * array. This pair is returned in a four-element
 * array, with the keys 0, 1,
 * key, and value. Elements
 * 0 and key contain the key name of
 * the array element, and 1 and value
 * contain the data.
 * </p>
 * <p>
 * If the internal pointer for the array points past the end of the
 * array contents, each returns
 * false.
 * </p>
 */
function each (array &$array) {}

/**
 * Sets which PHP errors are reported
 * @link http://php.net/manual/en/function.error-reporting.php
 * @param level int[optional] <p>
 * The new error_reporting
 * level. It takes on either a bitmask, or named constants. Using named 
 * constants is strongly encouraged to ensure compatibility for future 
 * versions. As error levels are added, the range of integers increases, 
 * so older integer-based error levels will not always behave as expected.
 * </p>
 * <p>
 * The available error level constants are listed below. The actual
 * meanings of these error levels are described in the
 * predefined constants.
 * <table>
 * error_reporting level constants and bit values
 * <tr valign="top">
 * <td>value</td>
 * <td>constant</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>
 * E_ERROR
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>
 * E_WARNING
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>4</td>
 * <td>
 * E_PARSE
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>8</td>
 * <td>
 * E_NOTICE
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>16</td>
 * <td>
 * E_CORE_ERROR
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>32</td>
 * <td>
 * E_CORE_WARNING
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>64</td>
 * <td>
 * E_COMPILE_ERROR
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>128</td>
 * <td>
 * E_COMPILE_WARNING
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>256</td>
 * <td>
 * E_USER_ERROR
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>512</td>
 * <td>
 * E_USER_WARNING
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>1024</td>
 * <td>
 * E_USER_NOTICE
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>6143</td>
 * <td>
 * E_ALL
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>2048</td>
 * <td>
 * E_STRICT
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>4096</td>
 * <td>
 * E_RECOVERABLE_ERROR
 * </td>
 * </tr>
 * </table>
 * </p>
 * @return int the old error_reporting
 * level.
 * </p>
 */
function error_reporting ($level = null) {}

/**
 * Defines a named constant
 * @link http://php.net/manual/en/function.define.php
 * @param name string <p>
 * The name of the constant.
 * </p>
 * @param value mixed <p>
 * The value of the constant; only scalar and null values are allowed. 
 * Scalar values are integer, 
 * float, string or boolean values.
 * </p>
 * @param case_insensitive bool[optional] <p>
 * If set to true, the constant will be defined case-insensitive. 
 * The default behaviour is case-sensitive; i.e. 
 * CONSTANT and Constant represent
 * different values.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function define ($name, $value, $case_insensitive = null) {}

/**
 * Checks whether a given named constant exists
 * @link http://php.net/manual/en/function.defined.php
 * @param name string <p>
 * The constant name.
 * </p>
 * @return bool true if the named constant given by name
 * has been defined, false otherwise.
 * </p>
 */
function defined ($name) {}

/**
 * Returns the name of the class of an object
 * @link http://php.net/manual/en/function.get-class.php
 * @param object object[optional] <p>
 * The tested object
 * </p>
 * @return string the name of the class of which object is an
 * instance. Returns false if object is not an 
 * object.
 * </p>
 */
function get_class ($object = null) {}

/**
 * Retrieves the parent class name for object or class
 * @link http://php.net/manual/en/function.get-parent-class.php
 * @param object mixed[optional] <p>
 * The tested object or class name
 * </p>
 * @return string the name of the parent class of the class of which
 * object is an instance or the name.
 * </p>
 * <p>
 * If the object does not have a parent false will be returned.
 * </p>
 * <p>
 * If called without parameter outside object, this function returns false.
 * </p>
 */
function get_parent_class ($object = null) {}

/**
 * Checks if the class method exists
 * @link http://php.net/manual/en/function.method-exists.php
 * @param object object <p>
 * An object instance
 * </p>
 * @param method_name string <p>
 * The method name
 * </p>
 * @return bool true if the method given by method_name
 * has been defined for the given object, false 
 * otherwise.
 * </p>
 */
function method_exists ($object, $method_name) {}

/**
 * Checks if the object or class has a property
 * @link http://php.net/manual/en/function.property-exists.php
 * @param class mixed <p>
 * The class name or an object of the class to test for
 * </p>
 * @param property string <p>
 * The name of the property
 * </p>
 * @return bool true if the property exists, false if it doesn't exist or
 * &null; in case of an error.
 * </p>
 */
function property_exists ($class, $property) {}

/**
 * Checks if the class has been defined
 * @link http://php.net/manual/en/function.class-exists.php
 * @param class_name string <p>
 * The class name. The name is matched in a case-insensitive manner.
 * </p>
 * @param autoload bool[optional] <p>
 * Whether or not to call &link.autoload; by default. Defaults to true.
 * </p>
 * @return bool true if class_name is a defined class,
 * false otherwise.
 * </p>
 */
function class_exists ($class_name, $autoload = null) {}

/**
 * Checks if the interface has been defined
 * @link http://php.net/manual/en/function.interface-exists.php
 * @param interface_name string <p>
 * The interface name
 * </p>
 * @param autoload bool[optional] <p>
 * Whether to call &link.autoload; or not by default
 * </p>
 * @return bool true if the interface given by 
 * interface_name has been defined, false otherwise.
 * </p>
 */
function interface_exists ($interface_name, $autoload = null) {}

/**
 * Return &true; if the given function has been defined
 * @link http://php.net/manual/en/function.function-exists.php
 * @param function_name string <p>
 * The function name, as a string.
 * </p>
 * @return bool true if function_name exists and is a
 * function, false otherwise.
 * </p>
 * <p>
 * This function will return false for constructs, such as 
 * include_once and echo.
 * </p>
 */
function function_exists ($function_name) {}

/**
 * Returns an array with the names of included or required files
 * @link http://php.net/manual/en/function.get-included-files.php
 * @return array an array of the names of all files.
 * </p>
 * <p>
 * The script originally called is considered an "included file," so it will
 * be listed together with the files referenced by 
 * include and family.
 * </p>
 * <p>
 * Files that are included or required multiple times only show up once in
 * the returned array.
 * </p>
 */
function get_included_files () {}

/**
 * &Alias; <function>get_included_files</function>
 * @link http://php.net/manual/en/function.get-required-files.php
 */
function get_required_files () {}

/**
 * Checks if the object has this class as one of its parents
 * @link http://php.net/manual/en/function.is-subclass-of.php
 * @param object mixed <p>
 * A class name or an object instance
 * </p>
 * @param class_name string <p>
 * The class name
 * </p>
 * @return bool This function returns true if the object object,
 * belongs to a class which is a subclass of 
 * class_name, false otherwise.
 * </p>
 */
function is_subclass_of ($object, $class_name) {}

/**
 * Checks if the object is of this class or has this class as one of its parents
 * @link http://php.net/manual/en/function.is-a.php
 * @param object object <p>
 * The tested object
 * </p>
 * @param class_name string <p>
 * The class name
 * </p>
 * @return bool true if the object is of this class or has this class as one of
 * its parents, false otherwise.
 * </p>
 */
function is_a ($object, $class_name) {}

/**
 * Get the default properties of the class
 * @link http://php.net/manual/en/function.get-class-vars.php
 * @param class_name string <p>
 * The class name
 * </p>
 * @return array an associative array of default public properties of the class.
 * The resulting array elements are in the form of 
 * varname => value.
 * </p>
 */
function get_class_vars ($class_name) {}

/**
 * Gets the public properties of the given object
 * @link http://php.net/manual/en/function.get-object-vars.php
 * @param object object <p>
 * An object instance.
 * </p>
 * @return array an associative array of defined object accessible non-static properties 
 * for the specified object in scope. If a property have 
 * not been assigned a value, it will be returned with a &null; value.
 * </p>
 */
function get_object_vars ($object) {}

/**
 * Gets the class methods' names
 * @link http://php.net/manual/en/function.get-class-methods.php
 * @param class_name mixed <p>
 * The class name or an object instance
 * </p>
 * @return array an array of method names defined for the class specified by
 * class_name. In case of an error, it returns &null;.
 * </p>
 */
function get_class_methods ($class_name) {}

/**
 * Generates a user-level error/warning/notice message
 * @link http://php.net/manual/en/function.trigger-error.php
 * @param error_msg string <p>
 * The designated error message for this error. It's limited to 1024 
 * characters in length. Any additional characters beyond 1024 will be 
 * truncated.
 * </p>
 * @param error_type int[optional] <p>
 * The designated error type for this error. It only works with the E_USER
 * family of constants, and will default to E_USER_NOTICE.
 * </p>
 * @return bool This function returns false if wrong error_type is
 * specified, true otherwise.
 * </p>
 */
function trigger_error ($error_msg, $error_type = null) {}

/**
 * Alias of <function>trigger_error</function>
 * @link http://php.net/manual/en/function.user-error.php
 */
function user_error () {}

/**
 * Sets a user-defined error handler function
 * @link http://php.net/manual/en/function.set-error-handler.php
 * @param error_handler callback <p>
 * The user function needs to accept two parameters: the error code, and a
 * string describing the error. Then there are three optional parameters 
 * that may be supplied: the filename in which the error occurred, the
 * line number in which the error occurred, and the context in which the
 * error occurred (an array that points to the active symbol table at the
 * point the error occurred). The function can be shown as:
 * </p>
 * <p>
 * handler
 * interrno
 * stringerrstr
 * stringerrfile
 * interrline
 * arrayerrcontext
 * errno
 * The first parameter, errno, contains the
 * level of the error raised, as an integer.
 * @param error_types int[optional] <p>
 * Can be used to mask the triggering of the
 * error_handler function just like the error_reporting ini setting 
 * controls which errors are shown. Without this mask set the
 * error_handler will be called for every error
 * regardless to the setting of the error_reporting setting.
 * </p>
 * @return mixed a string containing the previously defined
 * error handler (if any), or &null; on error. If the previous handler
 * was a class method, this function will return an indexed array with
 * the class and the method name.
 * </p>
 */
function set_error_handler ($error_handler, $error_types = null) {}

/**
 * Restores the previous error handler function
 * @link http://php.net/manual/en/function.restore-error-handler.php
 * @return bool This function always returns true.
 * </p>
 */
function restore_error_handler () {}

/**
 * Sets a user-defined exception handler function
 * @link http://php.net/manual/en/function.set-exception-handler.php
 * @param exception_handler callback <p>
 * Name of the function to be called when an uncaught exception occurs.
 * This function must be defined before calling
 * set_exception_handler. This handler function
 * needs to accept one parameter, which will be the exception object that
 * was thrown.
 * </p>
 * @return string the name of the previously defined exception handler, or &null; on error. If
 * no previous handler was defined, &null; is also returned.
 * </p>
 */
function set_exception_handler ($exception_handler) {}

/**
 * Restores the previously defined exception handler function
 * @link http://php.net/manual/en/function.restore-exception-handler.php
 * @return bool This function always returns true.
 * </p>
 */
function restore_exception_handler () {}

/**
 * Returns an array with the name of the defined classes
 * @link http://php.net/manual/en/function.get-declared-classes.php
 * @return array an array of the names of the declared classes in the current
 * script.
 * </p>
 * <p>
 * In PHP 4.0.1, three extra classes are returned at the beginning of
 * the array: stdClass (defined in
 * Zend/zend.c),
 * OverloadedTestClass (defined in
 * ext/standard/basic_functions.c)
 * and Directory
 * (defined in ext/standard/dir.c).
 * </p>
 * <p>
 * Also note that depending on what extensions you have compiled or
 * loaded into PHP, additional classes could be present. This means that
 * you will not be able to define your own classes using these
 * names. There is a list of predefined classes in the Predefined Classes section of
 * the appendices.
 * </p>
 */
function get_declared_classes () {}

/**
 * Returns an array of all declared interfaces
 * @link http://php.net/manual/en/function.get-declared-interfaces.php
 * @return array an array of the names of the declared interfaces in the current
 * script.
 * </p>
 */
function get_declared_interfaces () {}

/**
 * Returns an array of all defined functions
 * @link http://php.net/manual/en/function.get-defined-functions.php
 * @return array an multidimensional array containing a list of all defined
 * functions, both built-in (internal) and user-defined. The internal
 * functions will be accessible via $arr["internal"], and
 * the user defined ones using $arr["user"] (see example
 * below).
 * </p>
 */
function get_defined_functions () {}

/**
 * Returns an array of all defined variables
 * @link http://php.net/manual/en/function.get-defined-vars.php
 * @return array A multidimensional array with all the variables.
 * </p>
 */
function get_defined_vars () {}

/**
 * Create an anonymous (lambda-style) function
 * @link http://php.net/manual/en/function.create-function.php
 * @param args string <p>
 * The function arguments.
 * </p>
 * @param code string <p>
 * The function code.
 * </p>
 * @return string a unique function name as a string, or false on error.
 * </p>
 */
function create_function ($args, $code) {}

/**
 * Returns the resource type
 * @link http://php.net/manual/en/function.get-resource-type.php
 * @param handle resource <p>
 * The evaluated resource handle.
 * </p>
 * @return string If the given handle is a resource, this function
 * will return a string representing its type. If the type is not identified
 * by this function, the return value will be the string 
 * Unknown.
 * </p>
 * <p>
 * This function will return false and generate an error if 
 * handle is not a resource.
 * </p>
 */
function get_resource_type ($handle) {}

/**
 * Returns an array with the names of all modules compiled and loaded
 * @link http://php.net/manual/en/function.get-loaded-extensions.php
 * @param zend_extensions bool[optional] <p>
 * Return zend_extensions or not, defaults to false (do not list
 * zend_extensions).
 * </p>
 * @return array an indexed array of all the modules names.
 * </p>
 */
function get_loaded_extensions ($zend_extensions = null) {}

/**
 * Find out whether an extension is loaded
 * @link http://php.net/manual/en/function.extension-loaded.php
 * @param name string <p>
 * The extension name.
 * </p>
 * <p>
 * You can see the names of various extensions by using
 * phpinfo or if you're using the
 * CGI or CLI version of
 * PHP you can use the -m switch to
 * list all available extensions:
 * </p>
 * @return bool true if the extension identified by name
 * is loaded, false otherwise.
 * </p>
 */
function extension_loaded ($name) {}

/**
 * Returns an array with the names of the functions of a module
 * @link http://php.net/manual/en/function.get-extension-funcs.php
 * @param module_name string <p>
 * The module name.
 * </p>
 * <p>
 * This parameter must be in lowercase.
 * </p>
 * @return array an array with all the functions, or false if 
 * module_name is not a valid extension.
 * </p>
 */
function get_extension_funcs ($module_name) {}

/**
 * Returns an associative array with the names of all the constants and their values
 * @link http://php.net/manual/en/function.get-defined-constants.php
 * @param categorize mixed[optional] <p>
 * May be passed, causing this function to return a multi-dimensional
 * array with categories in the keys of the first dimension and constants
 * and their values in the second dimension.
 * ]]>
 * &example.outputs.similar;
 * Array
 * (
 * [E_ERROR] => 1
 * [E_WARNING] => 2
 * [E_PARSE] => 4
 * [E_NOTICE] => 8
 * [E_CORE_ERROR] => 16
 * [E_CORE_WARNING] => 32
 * [E_COMPILE_ERROR] => 64
 * [E_COMPILE_WARNING] => 128
 * [E_USER_ERROR] => 256
 * [E_USER_WARNING] => 512
 * [E_USER_NOTICE] => 1024
 * [E_ALL] => 2047
 * [TRUE] => 1
 * )
 * [pcre] => Array
 * (
 * [PREG_PATTERN_ORDER] => 1
 * [PREG_SET_ORDER] => 2
 * [PREG_OFFSET_CAPTURE] => 256
 * [PREG_SPLIT_NO_EMPTY] => 1
 * [PREG_SPLIT_DELIM_CAPTURE] => 2
 * [PREG_SPLIT_OFFSET_CAPTURE] => 4
 * [PREG_GREP_INVERT] => 1
 * )
 * [user] => Array
 * (
 * [MY_CONSTANT] => 1
 * )
 * )
 * ]]>
 * </p>
 * <p>
 * The value of the categorize parameter is irrelevant,
 * only its presence is considered.
 * </p>
 * @return array </p>
 */
function get_defined_constants ($categorize = null) {}

/**
 * Generates a backtrace
 * @link http://php.net/manual/en/function.debug-backtrace.php
 * @param provide_object bool[optional] 
 * @return array an associative array. The possible returned elements
 * are as follows:
 * </p>
 * <p>
 * <table>
 * Possible returned elements from debug_backtrace
 * <tr valign="top">
 * <td>&Name;</td>
 * <td>&Type;</td>
 * <td>&Description;</td>
 * </tr>
 * <tr valign="top">
 * <td>function</td>
 * <td>string</td>
 * <td>
 * The current function name. See also
 * __FUNCTION__.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>line</td>
 * <td>integer</td>
 * <td>
 * The current line number. See also
 * __LINE__.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>file</td>
 * <td>string</td>
 * <td>
 * The current file name. See also
 * __FILE__.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>class</td>
 * <td>string</td>
 * <td>
 * The current class name. See also
 * __CLASS__
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>object</td>
 * <td>object</td>
 * <td>
 * The current object.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>type</td>
 * <td>string</td>
 * <td>
 * The current call type. If a method call, "->" is returned. If a static
 * method call, "::" is returned. If a function call, nothing is returned.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>args</td>
 * <td>array</td>
 * <td>
 * If inside a function, this lists the functions arguments. If
 * inside an included file, this lists the included file name(s).
 * </td>
 * </tr>
 * </table>
 * </p>
 */
function debug_backtrace ($provide_object = null) {}

/**
 * Prints a backtrace
 * @link http://php.net/manual/en/function.debug-print-backtrace.php
 * @return void &return.void;
 * </p>
 */
function debug_print_backtrace () {}

class stdClass  {
}

class Exception  {
	protected $message;
	private $string;
	protected $code;
	protected $file;
	protected $line;
	private $trace;


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

class ErrorException extends Exception  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;
	protected $severity;


	/**
	 * @param message[optional]
	 * @param code[optional]
	 * @param severity[optional]
	 * @param filename[optional]
	 * @param lineno[optional]
	 */
	public function __construct ($message, $code, $severity, $filename, $lineno) {}

	final public function getSeverity () {}

	final private function __clone () {}

	final public function getMessage () {}

	final public function getCode () {}

	final public function getFile () {}

	final public function getLine () {}

	final public function getTrace () {}

	final public function getTraceAsString () {}

	public function __toString () {}

}

interface Traversable  {
}

interface IteratorAggregate extends Traversable {

	abstract public function getIterator () {}

}

interface Iterator extends Traversable {

	abstract public function current () {}

	abstract public function next () {}

	abstract public function key () {}

	abstract public function valid () {}

	abstract public function rewind () {}

}

interface ArrayAccess  {

	/**
	 * @param offset
	 */
	abstract public function offsetExists ($offset) {}

	/**
	 * @param offset
	 */
	abstract public function offsetGet ($offset) {}

	/**
	 * @param offset
	 * @param value
	 */
	abstract public function offsetSet ($offset, $value) {}

	/**
	 * @param offset
	 */
	abstract public function offsetUnset ($offset) {}

}

interface Serializable  {

	abstract public function serialize () {}

	/**
	 * @param serialized
	 */
	abstract public function unserialize ($serialized) {}

}


/**
 * Fatal run-time errors. These indicate errors that can not be
 * recovered from, such as a memory allocation problem.
 * Execution of the script is halted.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_ERROR', 1);

/**
 * Catchable fatal error. It indicates that a probably dangerous error
 * occured, but did not leave the Engine in an unstable state. If the error
 * is not caught by a user defined handle (see also
 * set_error_handler), the application aborts as it
 * was an E_ERROR.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_RECOVERABLE_ERROR', 4096);

/**
 * Run-time warnings (non-fatal errors). Execution of the script is not
 * halted.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_WARNING', 2);

/**
 * Compile-time parse errors. Parse errors should only be generated by
 * the parser.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_PARSE', 4);

/**
 * Run-time notices. Indicate that the script encountered something that
 * could indicate an error, but could also happen in the normal course of
 * running a script.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_NOTICE', 8);

/**
 * Run-time notices. Enable to have PHP suggest changes
 * to your code which will ensure the best interoperability
 * and forward compatibility of your code.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_STRICT', 2048);

/**
 * Fatal errors that occur during PHP's initial startup. This is like an
 * E_ERROR, except it is generated by the core of PHP.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_CORE_ERROR', 16);

/**
 * Warnings (non-fatal errors) that occur during PHP's initial startup.
 * This is like an E_WARNING, except it is generated
 * by the core of PHP.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_CORE_WARNING', 32);

/**
 * Fatal compile-time errors. This is like an E_ERROR,
 * except it is generated by the Zend Scripting Engine.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_COMPILE_ERROR', 64);

/**
 * Compile-time warnings (non-fatal errors). This is like an
 * E_WARNING, except it is generated by the Zend
 * Scripting Engine.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_COMPILE_WARNING', 128);

/**
 * User-generated error message. This is like an
 * E_ERROR, except it is generated in PHP code by
 * using the PHP function trigger_error.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_USER_ERROR', 256);

/**
 * User-generated warning message. This is like an
 * E_WARNING, except it is generated in PHP code by
 * using the PHP function trigger_error.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_USER_WARNING', 512);

/**
 * User-generated notice message. This is like an
 * E_NOTICE, except it is generated in PHP code by
 * using the PHP function trigger_error.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_USER_NOTICE', 1024);

/**
 * All errors and warnings, as supported, except of level
 * E_STRICT in PHP &lt; 6.
 * @link http://php.net/manual/en/errorfunc.constants.php
 */
define ('E_ALL', 6143);
define ('S_MEMORY', 1);
define ('S_VARS', 4);
define ('S_FILES', 8);
define ('S_INCLUDE', 16);
define ('S_SQL', 32);
define ('S_EXECUTOR', 64);
define ('S_MAIL', 128);
define ('S_SESSION', 256);
define ('S_MISC', 2);
define ('S_INTERNAL', 536870912);
define ('S_ALL', 511);
define ('LOG_EMERG', 0);
define ('LOG_ALERT', 1);
define ('LOG_CRIT', 2);
define ('LOG_ERR', 3);
define ('LOG_WARNING', 4);
define ('LOG_NOTICE', 5);
define ('LOG_INFO', 6);
define ('LOG_DEBUG', 7);
define ('LOG_KERN', 0);
define ('LOG_USER', 8);
define ('LOG_MAIL', 16);
define ('LOG_DAEMON', 24);
define ('LOG_AUTH', 32);
define ('LOG_SYSLOG', 40);
define ('LOG_LPR', 48);
define ('LOG_NEWS', 56);
define ('LOG_UUCP', 64);
define ('LOG_CRON', 72);
define ('LOG_AUTHPRIV', 80);
define ('LOG_LOCAL0', 128);
define ('LOG_LOCAL1', 136);
define ('LOG_LOCAL2', 144);
define ('LOG_LOCAL3', 152);
define ('LOG_LOCAL4', 160);
define ('LOG_LOCAL5', 168);
define ('LOG_LOCAL6', 176);
define ('LOG_LOCAL7', 184);
define ('LOG_PID', 1);
define ('LOG_CONS', 2);
define ('LOG_ODELAY', 4);
define ('LOG_NDELAY', 8);
define ('LOG_NOWAIT', 16);
define ('LOG_PERROR', 32);
define ('TRUE', true);
define ('FALSE', false);
define ('NULL', null);
define ('ZEND_THREAD_SAFE', false);
define ('PHP_VERSION', "5.2.4-2ubuntu5.2");
define ('PHP_OS', "Linux");
define ('PHP_SAPI', "cli");
define ('DEFAULT_INCLUDE_PATH', ".:/usr/share/php:/usr/share/pear");
define ('PEAR_INSTALL_DIR', "/usr/share/php");
define ('PEAR_EXTENSION_DIR', "/usr/lib/php5/20060613+lfs");
define ('PHP_EXTENSION_DIR', "/usr/lib/php5/20060613+lfs");
define ('PHP_PREFIX', "/usr");
define ('PHP_BINDIR', "/usr/bin");
define ('PHP_LIBDIR', "/usr/lib/php5");
define ('PHP_DATADIR', "${prefix}/share");
define ('PHP_SYSCONFDIR', "/usr/etc");
define ('PHP_LOCALSTATEDIR', "/usr/var");
define ('PHP_CONFIG_FILE_PATH', "/etc/php5/cli");
define ('PHP_CONFIG_FILE_SCAN_DIR', "/etc/php5/cli/conf.d");
define ('PHP_SHLIB_SUFFIX', "so");
define ('SUHOSIN_PATCH', 1);
define ('SUHOSIN_PATCH_VERSION', "0.9.6.2");
define ('PHP_EOL', "\n");
define ('PHP_INT_MAX', 2147483647);
define ('PHP_INT_SIZE', 4);
define ('PHP_OUTPUT_HANDLER_START', 1);
define ('PHP_OUTPUT_HANDLER_CONT', 2);
define ('PHP_OUTPUT_HANDLER_END', 4);
define ('UPLOAD_ERR_OK', 0);
define ('UPLOAD_ERR_INI_SIZE', 1);
define ('UPLOAD_ERR_FORM_SIZE', 2);
define ('UPLOAD_ERR_PARTIAL', 3);
define ('UPLOAD_ERR_NO_FILE', 4);
define ('UPLOAD_ERR_NO_TMP_DIR', 6);
define ('UPLOAD_ERR_CANT_WRITE', 7);
define ('UPLOAD_ERR_EXTENSION', 8);
define ('STDIN', "Resource id #1");
define ('STDOUT', "Resource id #2");
define ('STDERR', "Resource id #3");

/**
 * The full path and filename of the file. If used inside an include,
 * the name of the included file is returned.
 * Since PHP 4.0.2, __FILE__ always contains an
 * absolute path with symlinks resolved whereas in older versions it contained relative path
 * under some circumstances.
 * @link http://php.net/manual/en/language.constants.php
 */
define ('__FILE__', null);

/**
 * The current line number of the file.
 * @link http://php.net/manual/en/language.constants.php
 */
define ('__LINE__', null);

/**
 * The class name. (Added in PHP 4.3.0) As of PHP 5 this constant 
 * returns the class name as it was declared (case-sensitive). In PHP
 * 4 its value is always lowercased.
 * @link http://php.net/manual/en/language.constants.php
 */
define ('__CLASS__', null);

/**
 * The function name. (Added in PHP 4.3.0) As of PHP 5 this constant 
 * returns the function name as it was declared (case-sensitive). In
 * PHP 4 its value is always lowercased.
 * @link http://php.net/manual/en/language.constants.php
 */
define ('__FUNCTION__', null);

/**
 * The class method name. (Added in PHP 5.0.0) The method name is
 * returned as it was declared (case-sensitive).
 * @link http://php.net/manual/en/language.constants.php
 */
define ('__METHOD__', null);
?>
