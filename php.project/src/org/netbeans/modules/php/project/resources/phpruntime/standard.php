<?php

// Start of standard v.5.2.4-2ubuntu5.2

class __PHP_Incomplete_Class  {
}

class php_user_filter  {
	public $filtername;
	public $params;


	/**
	 * @param in
	 * @param out
	 * @param consumed
	 * @param closing
	 */
	public function filter ($in, $out, &$consumed, $closing) {}

	public function onCreate () {}

	public function onClose () {}

}

class Directory  {

	public function close () {}

	public function rewind () {}

	public function read () {}

}

/**
 * Returns the value of a constant
 * @link http://php.net/manual/en/function.constant.php
 * @param name string <p>
 * The constant name.
 * </p>
 * @return mixed the value of the constant, or &null; if the constant is not
 * defined.
 * </p>
 */
function constant ($name) {}

/**
 * Convert binary data into hexadecimal representation
 * @link http://php.net/manual/en/function.bin2hex.php
 * @param str string <p>
 * A character.
 * </p>
 * @return string the hexadecimal representation of the given string.
 * </p>
 */
function bin2hex ($str) {}

/**
 * Delay execution
 * @link http://php.net/manual/en/function.sleep.php
 * @param seconds int <p>
 * Halt time in seconds.
 * </p>
 * @return int zero on success, or false on errors.
 * </p>
 */
function sleep ($seconds) {}

/**
 * Delay execution in microseconds
 * @link http://php.net/manual/en/function.usleep.php
 * @param micro_seconds int <p>
 * Halt time in micro seconds. A micro second is one millionth of a
 * second.
 * </p>
 * @return void &return.void;
 * </p>
 */
function usleep ($micro_seconds) {}

/**
 * Delay for a number of seconds and nanoseconds
 * @link http://php.net/manual/en/function.time-nanosleep.php
 * @param seconds int <p>
 * Must be a positive integer.
 * </p>
 * @param nanoseconds int <p>
 * Must be a positive integer less than 1 billion.
 * </p>
 * @return mixed &return.success;
 * </p>
 * <p>
 * If the delay was interrupted by a signal, an associative array will be
 * returned with the components:
 * seconds - number of seconds remaining in
 * the delay
 * nanoseconds - number of nanoseconds
 * remaining in the delay
 * </p>
 */
function time_nanosleep ($seconds, $nanoseconds) {}

/**
 * Make the script sleep until the specified time
 * @link http://php.net/manual/en/function.time-sleep-until.php
 * @param timestamp float <p>
 * The timestamp when the script should wake.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function time_sleep_until ($timestamp) {}

/**
 * Parse a time/date generated with <function>strftime</function>
 * @link http://php.net/manual/en/function.strptime.php
 * @param date string <p>
 * The string to parse (e.g. returned from strftime)
 * </p>
 * @param format string <p>
 * The format used in date (e.g. the same as
 * used in strftime).
 * </p>
 * <p>
 * For more information about the format options, read the
 * strftime page.
 * </p>
 * @return array an array, or false on failure.
 * </p>
 * <p>
 * <table>
 * The following parameters are returned in the array
 * <tr valign="top">
 * <td>parameters</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_sec</td>
 * <td>Seconds after the minute (0-61)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_min</td>
 * <td>Minutes after the hour (0-59)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_hour</td>
 * <td>Hour since midnight (0-23)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_mday</td>
 * <td>Day of the month (1-31)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_mon</td>
 * <td>Months since January (0-11)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_year</td>
 * <td>Years since 1900</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_wday</td>
 * <td>Days since Sunday (0-6)</td>
 * </tr>
 * <tr valign="top">
 * <td>tm_yday</td>
 * <td>Days since January 1 (0-365)</td>
 * </tr>
 * <tr valign="top">
 * <td>unparsed</td>
 * <td>the date part which was not
 * recognized using the specified format</td>
 * </tr>
 * </table>
 * </p>
 */
function strptime ($date, $format) {}

/**
 * Flush the output buffer
 * @link http://php.net/manual/en/function.flush.php
 * @return void &return.void;
 * </p>
 */
function flush () {}

/**
 * Wraps a string to a given number of characters
 * @link http://php.net/manual/en/function.wordwrap.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param width int[optional] <p>
 * The column width. Defaults to 75.
 * </p>
 * @param break string[optional] <p>
 * The line is broken using the optional
 * break parameter. Defaults to 
 * '\n'.
 * </p>
 * @param cut bool[optional] <p>
 * If the cut is set to true, the string is
 * always wrapped at the specified width. So if you have a word that
 * is larger than the given width, it is broken apart. (See second
 * example).
 * </p>
 * @return string the given string wrapped at the specified column.
 * </p>
 */
function wordwrap ($str, $width = null, $break = null, $cut = null) {}

/**
 * Convert special characters to HTML entities
 * @link http://php.net/manual/en/function.htmlspecialchars.php
 * @param string string <p>
 * The string being converted.
 * </p>
 * @param quote_style int[optional] <p>
 * The optional second argument, quote_style, tells
 * the function what to do with single and double quote characters.
 * The default mode, ENT_COMPAT, is the backwards compatible mode
 * which only translates the double-quote character and leaves the
 * single-quote untranslated. If ENT_QUOTES is set, both single and
 * double quotes are translated and if ENT_NOQUOTES is set neither
 * single nor double quotes are translated.
 * </p>
 * @param charset string[optional] <p>
 * Defines character set used in conversion.
 * The default character set is ISO-8859-1.
 * </p>
 * &reference.strings.charsets;
 * @param double_encode bool[optional] <p>
 * When double_encode is turned off PHP will not
 * encode existing html entities, the default is to convert everything.
 * </p>
 * @return string The converted string.
 * </p>
 */
function htmlspecialchars ($string, $quote_style = null, $charset = null, $double_encode = null) {}

/**
 * Convert all applicable characters to HTML entities
 * @link http://php.net/manual/en/function.htmlentities.php
 * @param string string <p>
 * The input string.
 * </p>
 * @param quote_style int[optional] <p>
 * Like htmlspecialchars, the optional second
 * quote_style parameter lets you define what will
 * be done with 'single' and "double" quotes. It takes on one of three
 * constants with the default being ENT_COMPAT:
 * <table>
 * Available quote_style constants
 * <tr valign="top">
 * <td>Constant Name</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_COMPAT</td>
 * <td>Will convert double-quotes and leave single-quotes alone.</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_QUOTES</td>
 * <td>Will convert both double and single quotes.</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_NOQUOTES</td>
 * <td>Will leave both double and single quotes unconverted.</td>
 * </tr>
 * </table>
 * </p>
 * @param charset string[optional] <p>
 * Like htmlspecialchars, it takes an optional
 * third argument charset which defines character
 * set used in conversion.
 * Presently, the ISO-8859-1 character set is used as the default.
 * </p>
 * &reference.strings.charsets;
 * @param double_encode bool[optional] <p>
 * When double_encode is turned off PHP will not
 * encode existing html entities. The default is to convert everything.
 * </p>
 * @return string the encoded string.
 * </p>
 */
function htmlentities ($string, $quote_style = null, $charset = null, $double_encode = null) {}

/**
 * Convert all HTML entities to their applicable characters
 * @link http://php.net/manual/en/function.html-entity-decode.php
 * @param string string <p>
 * The input string.
 * </p>
 * @param quote_style int[optional] <p>
 * The optional second quote_style parameter lets
 * you define what will be done with 'single' and "double" quotes. It takes
 * on one of three constants with the default being
 * ENT_COMPAT:
 * <table>
 * Available quote_style constants
 * <tr valign="top">
 * <td>Constant Name</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_COMPAT</td>
 * <td>Will convert double-quotes and leave single-quotes alone.</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_QUOTES</td>
 * <td>Will convert both double and single quotes.</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_NOQUOTES</td>
 * <td>Will leave both double and single quotes unconverted.</td>
 * </tr>
 * </table>
 * </p>
 * @param charset string[optional] <p>
 * The ISO-8859-1 character set is used as default for the optional third
 * charset. This defines the character set used in
 * conversion.
 * </p>
 * &reference.strings.charsets;
 * @return string the decoded string.
 * </p>
 */
function html_entity_decode ($string, $quote_style = null, $charset = null) {}

/**
 * Convert special HTML entities back to characters
 * @link http://php.net/manual/en/function.htmlspecialchars-decode.php
 * @param string string <p>
 * The string to decode
 * </p>
 * @param quote_style int[optional] <p>
 * The quote style. One of the following constants:
 * <table>
 * quote_style constants
 * <tr valign="top">
 * <td>Constant Name</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_COMPAT</td>
 * <td>Will convert double-quotes and leave single-quotes alone
 * (default)</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_QUOTES</td>
 * <td>Will convert both double and single quotes</td>
 * </tr>
 * <tr valign="top">
 * <td>ENT_NOQUOTES</td>
 * <td>Will leave both double and single quotes unconverted</td>
 * </tr>
 * </table>
 * </p>
 * @return string the decoded string.
 * </p>
 */
function htmlspecialchars_decode ($string, $quote_style = null) {}

/**
 * Returns the translation table used by <function>htmlspecialchars</function> and <function>htmlentities</function>
 * @link http://php.net/manual/en/function.get-html-translation-table.php
 * @param table int[optional] <p>
 * There are two new constants (HTML_ENTITIES,
 * HTML_SPECIALCHARS) that allow you to specify the
 * table you want. Default value for table is
 * HTML_SPECIALCHARS.
 * </p>
 * @param quote_style int[optional] <p>
 * Like the htmlspecialchars and
 * htmlentities functions you can optionally specify
 * the quote_style you are working with. The
 * default is ENT_COMPAT mode. See the description
 * of these modes in htmlspecialchars.
 * </p>
 * @return array the translation table as an array.
 * </p>
 */
function get_html_translation_table ($table = null, $quote_style = null) {}

/**
 * Calculate the sha1 hash of a string
 * @link http://php.net/manual/en/function.sha1.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param raw_output bool[optional] <p>
 * If the optional raw_output is set to true,
 * then the sha1 digest is instead returned in raw binary format with a
 * length of 20, otherwise the returned value is a 40-character
 * hexadecimal number. Defaults to false.
 * </p>
 * @return string the sha1 hash as a string.
 * </p>
 */
function sha1 ($str, $raw_output = null) {}

/**
 * Calculate the sha1 hash of a file
 * @link http://php.net/manual/en/function.sha1-file.php
 * @param filename string <p>
 * The filename
 * </p>
 * @param raw_output bool[optional] <p>
 * When true, returns the digest in raw binary format with a length of
 * 20. Defaults to false.
 * </p>
 * @return string a string on success, false otherwise.
 * </p>
 */
function sha1_file ($filename, $raw_output = null) {}

/**
 * Calculate the md5 hash of a string
 * @link http://php.net/manual/en/function.md5.php
 * @param str string <p>
 * The string.
 * </p>
 * @param raw_output bool[optional] <p>
 * If the optional raw_output is set to true,
 * then the md5 digest is instead returned in raw binary format with a
 * length of 16. Defaults to false.
 * </p>
 * @return string the hash as a 32-character hexadecimal number. 
 * </p>
 */
function md5 ($str, $raw_output = null) {}

/**
 * Calculates the md5 hash of a given file
 * @link http://php.net/manual/en/function.md5-file.php
 * @param filename string <p>
 * The filename
 * </p>
 * @param raw_output bool[optional] <p>
 * When true, returns the digest in raw binary format with a length of
 * 16. Defaults to false.
 * </p>
 * @return string a string on success, false otherwise.
 * </p>
 */
function md5_file ($filename, $raw_output = null) {}

/**
 * Calculates the crc32 polynomial of a string
 * @link http://php.net/manual/en/function.crc32.php
 * @param str string <p>
 * The data.
 * </p>
 * @return int the crc32 checksum of str as an integer.
 * </p>
 */
function crc32 ($str) {}

/**
 * Parse a binary IPTC block into single tags.
 * @link http://php.net/manual/en/function.iptcparse.php
 * @param iptcblock string <p>
 * A binary IPTC block
 * </p>
 * @return array an array using the tagmarker as an index and the value as the
 * value. It returns false on error or if no IPTC data was found. 
 * </p>
 */
function iptcparse ($iptcblock) {}

/**
 * Embed binary IPTC data into a JPEG image
 * @link http://php.net/manual/en/function.iptcembed.php
 * @param iptcdata string <p>
 * </p>
 * @param jpeg_file_name string <p>
 * </p>
 * @param spool int[optional] <p>
 * </p>
 * @return mixed 
 */
function iptcembed ($iptcdata, $jpeg_file_name, $spool = null) {}

/**
 * Get the size of an image
 * @link http://php.net/manual/en/function.getimagesize.php
 * @param filename string <p>
 * This parameter specifies the file you wish to retrieve information
 * about. It can reference a local file or (configuration permitting) a
 * remote file using one of the supported streams. 
 * </p>
 * @param imageinfo array[optional] <p>
 * This optional parameter allows you to extract some extended
 * information from the image file. Currently, this will return the
 * different JPG APP markers as an associative array.
 * Some programs use these APP markers to embed text information in 
 * images. A very common one is to embed 
 * IPTC information in the APP13 marker.
 * You can use the iptcparse function to parse the
 * binary APP13 marker into something readable.
 * </p>
 * @return array an array with 7 elements.
 * </p>
 * <p>
 * Index 0 and 1 contains respectively the width and the height of the image.
 * </p>
 * <p>
 * Some formats may contain no image or may contain multiple images. In these
 * cases, getimagesize might not be able to properly
 * determine the image size. getimagesize will return
 * zero for width and height in these cases.
 * </p>
 * <p>
 * Index 2 is one of the IMAGETYPE_XXX constants indicating 
 * the type of the image.
 * </p>
 * <p>
 * Index 3 is a text string with the correct 
 * height="yyy" width="xxx" string that can be used
 * directly in an IMG tag.
 * </p>
 * <p>
 * mime is the correspondant MIME type of the image.
 * This information can be used to deliver images with correct the HTTP 
 * Content-type header:
 * getimagesize() and MIME types
 * ]]>
 * </p>
 * <p>
 * channels will be 3 for RGB pictures and 4 for CMYK
 * pictures.
 * </p>
 * <p>
 * bits is the number of bits for each color.
 * </p>
 * <p>
 * For some image types, the presence of channels and
 * bits values can be a bit
 * confusing. As an example, GIF always uses 3 channels
 * per pixel, but the number of bits per pixel cannot be calculated for an
 * animated GIF with a global color table.
 * </p>
 * <p>
 * On failure, false is returned.
 * </p>
 */
function getimagesize ($filename, array &$imageinfo = null) {}

/**
 * Get Mime-Type for image-type returned by getimagesize,
   exif_read_data, exif_thumbnail, exif_imagetype
 * @link http://php.net/manual/en/function.image-type-to-mime-type.php
 * @param imagetype int <p>
 * One of the IMAGETYPE_XXX constants
 * </p>
 * @return string The returned values are as follows
 * <table>
 * Returned values Constants
 * <tr valign="top">
 * <td>imagetype</td>
 * <td>Returned value</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_GIF</td>
 * <td>image/gif</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_JPEG</td>
 * <td>image/jpeg</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_PNG</td>
 * <td>image/png</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_SWF</td>
 * <td>application/x-shockwave-flash</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_PSD</td>
 * <td>image/psd</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_BMP</td>
 * <td>image/bmp</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_TIFF_II (intel byte order)</td>
 * <td>image/tiff</td>
 * </tr>
 * <tr valign="top">
 * <td>
 * IMAGETYPE_TIFF_MM (motorola byte order)
 * </td>
 * <td>image/tiff</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_JPC</td>
 * <td>application/octet-stream</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_JP2</td>
 * <td>image/jp2</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_JPX</td>
 * <td>application/octet-stream</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_JB2</td>
 * <td>application/octet-stream</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_SWC</td>
 * <td>application/x-shockwave-flash</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_IFF</td>
 * <td>image/iff</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_WBMP</td>
 * <td>image/vnd.wap.wbmp</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_XBM</td>
 * <td>image/xbm</td>
 * </tr>
 * <tr valign="top">
 * <td>IMAGETYPE_ICO</td>
 * <td>image/vnd.microsoft.icon</td>
 * </tr>
 * </table>
 * </p>
 */
function image_type_to_mime_type ($imagetype) {}

/**
 * Get file extension for image type
 * @link http://php.net/manual/en/function.image-type-to-extension.php
 * @param imagetype int <p>
 * One of the IMAGETYPE_XXX constant.
 * </p>
 * @param include_dot bool[optional] <p>
 * Whether to prepend a dot to the extension or not. Default to true.
 * </p>
 * @return string A string with the extension corresponding to the given image type.
 * </p>
 */
function image_type_to_extension ($imagetype, $include_dot = null) {}

/**
 * Outputs lots of PHP information
 * @link http://php.net/manual/en/function.phpinfo.php
 * @param what int[optional] <p>
 * The output may be customized by passing one or more of the
 * following constants bitwise values summed
 * together in the optional what parameter.
 * One can also combine the respective constants or bitwise values
 * together with the or operator.
 * </p>
 * <p>
 * <table>
 * phpinfo options
 * <tr valign="top">
 * <td>Name (constant)</td>
 * <td>Value</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_GENERAL</td>
 * <td>1</td>
 * <td>
 * The configuration line, &php.ini; location, build date, Web
 * Server, System and more.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_CREDITS</td>
 * <td>2</td>
 * <td>
 * PHP Credits. See also phpcredits.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_CONFIGURATION</td>
 * <td>4</td>
 * <td>
 * Current Local and Master values for PHP directives. See
 * also ini_get.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_MODULES</td>
 * <td>8</td>
 * <td>
 * Loaded modules and their respective settings. See also
 * get_loaded_extensions.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_ENVIRONMENT</td>
 * <td>16</td>
 * <td>
 * Environment Variable information that's also available in
 * $_ENV.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_VARIABLES</td>
 * <td>32</td>
 * <td>
 * Shows all 
 * predefined variables from EGPCS (Environment, GET,
 * POST, Cookie, Server).
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_LICENSE</td>
 * <td>64</td>
 * <td>
 * PHP License information. See also the license FAQ.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>INFO_ALL</td>
 * <td>-1</td>
 * <td>
 * Shows all of the above. This is the default value.
 * </td>
 * </tr>
 * </table>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function phpinfo ($what = null) {}

/**
 * Gets the current PHP version
 * @link http://php.net/manual/en/function.phpversion.php
 * @param extension string[optional] <p>
 * An optional extension name.
 * </p>
 * @return string If the optional extension parameter is
 * specified, phpversion returns the version of that
 * extension, or false if there is no version information associated or
 * the extension isn't enabled.
 * </p>
 */
function phpversion ($extension = null) {}

/**
 * Prints out the credits for PHP
 * @link http://php.net/manual/en/function.phpcredits.php
 * @param flag int[optional] <p>
 * To generate a custom credits page, you may want to use the
 * flag parameter. flag is
 * optional, and it defaults to CREDITS_ALL.
 * </p>
 * <p>
 * <table>
 * Pre-defined phpcredits flags
 * <tr valign="top">
 * <td>name</td>
 * <td>description</td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_ALL</td>
 * <td>
 * All the credits, equivalent to using: CREDITS_DOCS +
 * CREDITS_GENERAL + CREDITS_GROUP + CREDITS_MODULES +
 * CREDITS_FULLPAGE. It generates a complete stand-alone HTML
 * page with the appropriate tags.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_DOCS</td>
 * <td>The credits for the documentation team</td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_FULLPAGE</td>
 * <td>
 * Usually used in combination with the other flags. Indicates
 * that a complete stand-alone HTML page needs to be
 * printed including the information indicated by the other
 * flags.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_GENERAL</td>
 * <td>
 * General credits: Language design and concept, PHP 4.0
 * authors and SAPI module.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_GROUP</td>
 * <td>A list of the core developers</td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_MODULES</td>
 * <td>
 * A list of the extension modules for PHP, and their authors
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>CREDITS_SAPI</td>
 * <td>
 * A list of the server API modules for PHP, and their authors
 * </td>
 * </tr>
 * </table>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function phpcredits ($flag = null) {}

/**
 * Gets the logo guid
 * @link http://php.net/manual/en/function.php-logo-guid.php
 * @return string PHPE9568F34-D428-11d2-A769-00AA001ACF42.
 * </p>
 */
function php_logo_guid () {}

function php_real_logo_guid () {}

function php_egg_logo_guid () {}

/**
 * Gets the Zend guid
 * @link http://php.net/manual/en/function.zend-logo-guid.php
 * @return string PHPE9568F35-D428-11d2-A769-00AA001ACF42.
 * </p>
 */
function zend_logo_guid () {}

/**
 * Returns the type of interface between web server and PHP
 * @link http://php.net/manual/en/function.php-sapi-name.php
 * @return string the interface type, as a lowercase string.
 * </p>
 */
function php_sapi_name () {}

/**
 * Returns information about the operating system PHP is running on
 * @link http://php.net/manual/en/function.php-uname.php
 * @param mode string[optional] <p>
 * mode is a single character that defines what
 * information is returned:
 * 'a': This is the default. Contains all modes in
 * the sequence "s n r v m".
 * @return string the description, as a string.
 * </p>
 */
function php_uname ($mode = null) {}

/**
 * Return a list of .ini files parsed from the additional ini dir
 * @link http://php.net/manual/en/function.php-ini-scanned-files.php
 * @return string a comma-separated string of .ini files on success. Each comma is
 * followed by a newline. If the directive --with-config-file-scan-dir wasn't set,
 * false is returned. If it was set and the directory was empty, an
 * empty string is returned. If a file is unrecognizable, the file will
 * still make it into the returned string but a PHP error will also result.
 * This PHP error will be seen both at compile time and while using
 * php_ini_scanned_files.
 * </p>
 */
function php_ini_scanned_files () {}

/**
 * Retrieve a path to the loaded php.ini file
 * @link http://php.net/manual/en/function.php-ini-loaded-file.php
 * @return string The loaded &php.ini; path, or false if one is not loaded.
 * </p>
 */
function php_ini_loaded_file () {}

/**
 * String comparisons using a "natural order" algorithm
 * @link http://php.net/manual/en/function.strnatcmp.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @return int Similar to other string comparison functions, this one returns &lt; 0 if
 * str1 is less than str2; &gt;
 * 0 if str1 is greater than
 * str2, and 0 if they are equal.
 * </p>
 */
function strnatcmp ($str1, $str2) {}

/**
 * Case insensitive string comparisons using a "natural order" algorithm
 * @link http://php.net/manual/en/function.strnatcasecmp.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @return int Similar to other string comparison functions, this one returns &lt; 0 if
 * str1 is less than str2 &gt;
 * 0 if str1 is greater than
 * str2, and 0 if they are equal.
 * </p>
 */
function strnatcasecmp ($str1, $str2) {}

/**
 * Count the number of substring occurrences
 * @link http://php.net/manual/en/function.substr-count.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle string <p>
 * The substring to search for
 * </p>
 * @param offset int[optional] <p>
 * The offset where to start counting
 * </p>
 * @param length int[optional] <p>
 * The maximum length after the specified offset to search for the
 * substring. It outputs a warning if the offset plus the length is
 * greater than the haystack length.
 * </p>
 * @return int This functions returns an integer.
 * </p>
 */
function substr_count ($haystack, $needle, $offset = null, $length = null) {}

/**
 * Find length of initial segment matching mask
 * @link http://php.net/manual/en/function.strspn.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @param start int[optional] <p>
 * The start position of the string to examine.
 * Negative value counts position from the end of a string.
 * </p>
 * @param length int[optional] <p>
 * The length of the string to examine.
 * Negative value sets length from the end of a string.
 * </p>
 * @return int the length of the initial segment of str1
 * which consists entirely of characters in str2.
 * </p>
 */
function strspn ($str1, $str2, $start = null, $length = null) {}

/**
 * Find length of initial segment not matching mask
 * @link http://php.net/manual/en/function.strcspn.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @param start int[optional] <p>
 * The start position of the string to examine.
 * </p>
 * @param length int[optional] <p>
 * The length of the string to examine.
 * </p>
 * @return int the length of the segment as an integer.
 * </p>
 */
function strcspn ($str1, $str2, $start = null, $length = null) {}

/**
 * Tokenize string
 * @link http://php.net/manual/en/function.strtok.php
 * @param str string <p>
 * The string being split up into smaller strings (tokens).
 * </p>
 * @param token string <p>
 * The delimiter used when splitting up str.
 * </p>
 * @return string A string token.
 * </p>
 */
function strtok ($str, $token) {}

/**
 * Make a string uppercase
 * @link http://php.net/manual/en/function.strtoupper.php
 * @param string string <p>
 * The input string.
 * </p>
 * @return string the uppercased string.
 * </p>
 */
function strtoupper ($string) {}

/**
 * Make a string lowercase
 * @link http://php.net/manual/en/function.strtolower.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the lowercased string.
 * </p>
 */
function strtolower ($str) {}

/**
 * Find position of first occurrence of a string
 * @link http://php.net/manual/en/function.strpos.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle mixed <p>
 * If needle is not a string, it is converted
 * to an integer and applied as the ordinal value of a character.
 * </p>
 * @param offset int[optional] <p>
 * The optional offset parameter allows you
 * to specify which character in haystack to
 * start searching. The position returned is still relative to the
 * beginning of haystack.
 * </p>
 * @return int the position as an integer. If needle is
 * not found, strpos will return boolean
 * false.
 * </p>
 * &return.falseproblem;
 */
function strpos ($haystack, $needle, $offset = null) {}

/**
 * Find position of first occurrence of a case-insensitive string
 * @link http://php.net/manual/en/function.stripos.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle string <p>
 * Note that the needle may be a string of one or
 * more characters.
 * </p>
 * <p>
 * If needle is not a string, it is converted to
 * an integer and applied as the ordinal value of a character.
 * </p>
 * @param offset int[optional] <p>
 * The optional offset parameter allows you
 * to specify which character in haystack to
 * start searching. The position returned is still relative to the
 * beginning of haystack.
 * </p>
 * @return int If needle is not found,
 * stripos will return boolean false.
 * </p>
 * &return.falseproblem;
 */
function stripos ($haystack, $needle, $offset = null) {}

/**
 * Find position of last occurrence of a char in a string
 * @link http://php.net/manual/en/function.strrpos.php
 * @param haystack string <p>
 * </p>
 * @param needle string <p>
 * </p>
 * @param offset int[optional] <p>
 * </p>
 * @return int </p>
 */
function strrpos ($haystack, $needle, $offset = null) {}

/**
 * Find position of last occurrence of a case-insensitive string in a string
 * @link http://php.net/manual/en/function.strripos.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle string <p>
 * Note that the needle may be a string of one or
 * more characters.
 * </p>
 * @param offset int[optional] <p>
 * The offset parameter may be specified to begin
 * searching an arbitrary number of characters into the string.
 * </p>
 * <p>
 * Negative offset values will start the search at
 * offset characters from the
 * start of the string.
 * </p>
 * @return int the numerical position of the last occurence of
 * needle. Also note that string positions start at 0,
 * and not 1.
 * </p>
 * <p>
 * If needle is not found, false is returned.
 * </p>
 * &return.falseproblem;
 */
function strripos ($haystack, $needle, $offset = null) {}

/**
 * Reverse a string
 * @link http://php.net/manual/en/function.strrev.php
 * @param string string <p>
 * The string to be reversed.
 * </p>
 * @return string the reversed string.
 * </p>
 */
function strrev ($string) {}

/**
 * Convert logical Hebrew text to visual text
 * @link http://php.net/manual/en/function.hebrev.php
 * @param hebrew_text string <p>
 * A Hebrew input string.
 * </p>
 * @param max_chars_per_line int[optional] <p>
 * This optional parameter indicates maximum number of characters per
 * line that will be returned.
 * </p>
 * @return string the visual string.
 * </p>
 */
function hebrev ($hebrew_text, $max_chars_per_line = null) {}

/**
 * Convert logical Hebrew text to visual text with newline conversion
 * @link http://php.net/manual/en/function.hebrevc.php
 * @param hebrew_text string <p>
 * A Hebrew input string.
 * </p>
 * @param max_chars_per_line int[optional] <p>
 * This optional parameter indicates maximum number of characters per
 * line that will be returned.
 * </p>
 * @return string the visual string.
 * </p>
 */
function hebrevc ($hebrew_text, $max_chars_per_line = null) {}

/**
 * Inserts HTML line breaks before all newlines in a string
 * @link http://php.net/manual/en/function.nl2br.php
 * @param string string <p>
 * The input string.
 * </p>
 * @return string the altered string.
 * </p>
 */
function nl2br ($string) {}

/**
 * Returns filename component of path
 * @link http://php.net/manual/en/function.basename.php
 * @param path string <p>
 * A path.
 * </p>
 * <p>
 * On Windows, both slash (/) and backslash
 * (\) are used as directory separator character. In
 * other environments, it is the forward slash (/).
 * </p>
 * @param suffix string[optional] <p>
 * If the filename ends in suffix this will also
 * be cut off.
 * </p>
 * @return string the base name of the given path.
 * </p>
 */
function basename ($path, $suffix = null) {}

/**
 * Returns directory name component of path
 * @link http://php.net/manual/en/function.dirname.php
 * @param path string <p>
 * A path.
 * </p>
 * <p>
 * On Windows, both slash (/) and backslash
 * (\) are used as directory separator character. In
 * other environments, it is the forward slash (/).
 * </p>
 * @return string the name of the directory. If there are no slashes in
 * path, a dot ('.') is returned,
 * indicating the current directory. Otherwise, the returned string is
 * path with any trailing
 * /component removed.
 * </p>
 */
function dirname ($path) {}

/**
 * Returns information about a file path
 * @link http://php.net/manual/en/function.pathinfo.php
 * @param path string <p>
 * The path being checked.
 * </p>
 * @param options int[optional] <p>
 * You can specify which elements are returned with optional parameter
 * options. It composes from
 * PATHINFO_DIRNAME,
 * PATHINFO_BASENAME,
 * PATHINFO_EXTENSION and
 * PATHINFO_FILENAME. It
 * defaults to return all elements.
 * </p>
 * @return mixed The following associative array elements are returned:
 * dirname, basename,
 * extension (if any), and filename.
 * </p>
 * <p>
 * If options is used, this function will return a 
 * string if not all elements are requested.
 * </p>
 */
function pathinfo ($path, $options = null) {}

/**
 * Un-quote string quoted with <function>addslashes</function>
 * @link http://php.net/manual/en/function.stripslashes.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string a string with backslashes stripped off.
 * (\' becomes ' and so on.)
 * Double backslashes (\\) are made into a single
 * backslash (\).
 * </p>
 */
function stripslashes ($str) {}

/**
 * Un-quote string quoted with <function>addcslashes</function>
 * @link http://php.net/manual/en/function.stripcslashes.php
 * @param str string <p>
 * The string to be unescaped.
 * </p>
 * @return string the unescaped string.
 * </p>
 */
function stripcslashes ($str) {}

/**
 * Find first occurrence of a string
 * @link http://php.net/manual/en/function.strstr.php
 * @param haystack string <p>
 * The input string.
 * </p>
 * @param needle mixed <p>
 * If needle is not a string, it is converted to
 * an integer and applied as the ordinal value of a character.
 * </p>
 * @param before_needle bool[optional] <p>
 * If true (the default is false), strstr returns
 * the part of the haystack before the first
 * occurence of the needle.
 * </p>
 * @return string the portion of string, or false if needle
 * is not found.
 * </p>
 */
function strstr ($haystack, $needle, $before_needle = null) {}

/**
 * Case-insensitive <function>strstr</function>
 * @link http://php.net/manual/en/function.stristr.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle mixed <p>
 * If needle is not a string, it is converted to
 * an integer and applied as the ordinal value of a character.
 * </p>
 * @param before_needle bool[optional] <p>
 * If true (the default is false), stristr
 * returns the part of the haystack before the
 * first occurence of the needle.
 * </p>
 * @return string the matched substring. If needle is not
 * found, returns false.
 * </p>
 */
function stristr ($haystack, $needle, $before_needle = null) {}

/**
 * Find the last occurrence of a character in a string
 * @link http://php.net/manual/en/function.strrchr.php
 * @param haystack string <p>
 * The string to search in
 * </p>
 * @param needle mixed <p>
 * If needle contains more than one character,
 * only the first is used. This behavior is different from that of
 * strstr.
 * </p>
 * <p>
 * If needle is not a string, it is converted to
 * an integer and applied as the ordinal value of a character.
 * </p>
 * @return string This function returns the portion of string, or false if
 * needle is not found.
 * </p>
 */
function strrchr ($haystack, $needle) {}

/**
 * Randomly shuffles a string
 * @link http://php.net/manual/en/function.str-shuffle.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the shuffled string.
 * </p>
 */
function str_shuffle ($str) {}

/**
 * Return information about words used in a string
 * @link http://php.net/manual/en/function.str-word-count.php
 * @param string string <p>
 * The string
 * </p>
 * @param format int[optional] <p>
 * Specify the return value of this function. The current supported values
 * are:
 * 0 - returns the number of words found
 * @param charlist string[optional] <p>
 * A list of additional characters which will be considered as 'word'
 * </p>
 * @return mixed an array or an integer, depending on the
 * format chosen.
 * </p>
 */
function str_word_count ($string, $format = null, $charlist = null) {}

/**
 * Convert a string to an array
 * @link http://php.net/manual/en/function.str-split.php
 * @param string string <p>
 * The input string.
 * </p>
 * @param split_length int[optional] <p>
 * Maximum length of the chunk.
 * </p>
 * @return array If the optional split_length parameter is
 * specified, the returned array will be broken down into chunks with each
 * being split_length in length, otherwise each chunk
 * will be one character in length.
 * </p>
 * <p>
 * false is returned if split_length is less than 1.
 * If the split_length length exceeds the length of
 * string, the entire string is returned as the first
 * (and only) array element.
 * </p>
 */
function str_split ($string, $split_length = null) {}

/**
 * Search a string for any of a set of characters
 * @link http://php.net/manual/en/function.strpbrk.php
 * @param haystack string <p>
 * The string where char_list is looked for.
 * </p>
 * @param char_list string <p>
 * This parameter is case sensitive.
 * </p>
 * @return string a string starting from the character found, or false if it is
 * not found.
 * </p>
 */
function strpbrk ($haystack, $char_list) {}

/**
 * Binary safe comparison of 2 strings from an offset, up to length characters
 * @link http://php.net/manual/en/function.substr-compare.php
 * @param main_str string <p>
 * </p>
 * @param str string <p>
 * </p>
 * @param offset int <p>
 * The start position for the comparison. If negative, it starts counting
 * from the end of the string.
 * </p>
 * @param length int[optional] <p>
 * The length of the comparison.
 * </p>
 * @param case_insensitivity bool[optional] <p>
 * If case_insensitivity is true, comparison is
 * case insensitive.
 * </p>
 * @return int &lt; 0 if main_str from position
 * offset is less than str, &gt;
 * 0 if it is greater than str, and 0 if they are equal.
 * If length is equal or greater than length of
 * main_str and length is set,
 * substr_compare prints warning and returns false.
 * </p>
 */
function substr_compare ($main_str, $str, $offset, $length = null, $case_insensitivity = null) {}

/**
 * Locale based string comparison
 * @link http://php.net/manual/en/function.strcoll.php
 * @param str1 string <p>
 * The first string.
 * </p>
 * @param str2 string <p>
 * The second string.
 * </p>
 * @return int &lt; 0 if str1 is less than
 * str2; &gt; 0 if
 * str1 is greater than
 * str2, and 0 if they are equal.
 * </p>
 */
function strcoll ($str1, $str2) {}

/**
 * Formats a number as a currency string
 * @link http://php.net/manual/en/function.money-format.php
 * @param format string <p>
 * The format specification consists of the following sequence:
 * <p>a % character</p>
 * @param number float <p>
 * The number to be formatted.
 * </p>
 * @return string the formatted string. Characters before and after the formatting
 * string will be returned unchanged.
 * </p>
 */
function money_format ($format, $number) {}

/**
 * Return part of a string
 * @link http://php.net/manual/en/function.substr.php
 * @param string string <p>
 * The input string.
 * </p>
 * @param start int <p>
 * If start is non-negative, the returned string
 * will start at the start'th position in
 * string, counting from zero. For instance,
 * in the string 'abcdef', the character at
 * position 0 is 'a', the
 * character at position 2 is
 * 'c', and so forth.
 * </p>
 * <p>
 * If start is negative, the returned string
 * will start at the start'th character
 * from the end of string.
 * </p>
 * <p>
 * Using a negative start
 * ]]>
 * </p>
 * @param length int[optional] <p>
 * If length is given and is positive, the string
 * returned will contain at most length characters
 * beginning from start (depending on the length of
 * string). If string is less
 * than or equal to start characters long, false
 * will be returned.
 * </p>
 * <p>
 * If length is given and is negative, then that many
 * characters will be omitted from the end of string
 * (after the start position has been calculated when a
 * start is negative). If
 * start denotes a position beyond this truncation,
 * an empty string will be returned.
 * </p>
 * Using a negative length
 * ]]>
 * @return string the extracted part of string.
 * </p>
 */
function substr ($string, $start, $length = null) {}

/**
 * Replace text within a portion of a string
 * @link http://php.net/manual/en/function.substr-replace.php
 * @param string mixed <p>
 * The input string.
 * </p>
 * @param replacement string <p>
 * The replacement string.
 * </p>
 * @param start int <p>
 * If start is positive, the replacing will
 * begin at the start'th offset into
 * string.
 * </p>
 * <p>
 * If start is negative, the replacing will
 * begin at the start'th character from the
 * end of string.
 * </p>
 * @param length int[optional] <p>
 * If given and is positive, it represents the length of the portion of
 * string which is to be replaced. If it is
 * negative, it represents the number of characters from the end of
 * string at which to stop replacing. If it
 * is not given, then it will default to strlen(
 * string ); i.e. end the replacing at the
 * end of string. Of course, if
 * length is zero then this function will have the
 * effect of inserting replacement into
 * string at the given
 * start offset.
 * </p>
 * @return mixed The result string is returned. If string is an
 * array then array is returned.
 * </p>
 */
function substr_replace ($string, $replacement, $start, $length = null) {}

/**
 * Quote meta characters
 * @link http://php.net/manual/en/function.quotemeta.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the string with meta characters quoted.
 * </p>
 */
function quotemeta ($str) {}

/**
 * Make a string's first character uppercase
 * @link http://php.net/manual/en/function.ucfirst.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the resulting string.
 * </p>
 */
function ucfirst ($str) {}

/**
 * Uppercase the first character of each word in a string
 * @link http://php.net/manual/en/function.ucwords.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the modified string.
 * </p>
 */
function ucwords ($str) {}

/**
 * Translate certain characters
 * @link http://php.net/manual/en/function.strtr.php
 * @param str string <p>
 * The string being translated.
 * </p>
 * @param from string <p>
 * The string being translated to to.
 * </p>
 * @param to string <p>
 * The string replacing from.
 * </p>
 * @return string This function returns a copy of str,
 * translating all occurrences of each character in
 * from to the corresponding character in
 * to.
 * </p>
 */
function strtr ($str, $from, $to) {}

/**
 * Quote string with slashes
 * @link http://php.net/manual/en/function.addslashes.php
 * @param str string <p>
 * The string to be escaped.
 * </p>
 * @return string the escaped string.
 * </p>
 */
function addslashes ($str) {}

/**
 * Quote string with slashes in a C style
 * @link http://php.net/manual/en/function.addcslashes.php
 * @param str string <p>
 * The string to be escaped.
 * </p>
 * @param charlist string <p>
 * A list of characters to be escaped. If
 * charlist contains characters
 * \n, \r etc., they are
 * converted in C-like style, while other non-alphanumeric characters
 * with ASCII codes lower than 32 and higher than 126 converted to
 * octal representation.
 * </p>
 * <p>
 * When you define a sequence of characters in the charlist argument
 * make sure that you know what characters come between the
 * characters that you set as the start and end of the range.
 * ]]>
 * Also, if the first character in a range has a higher ASCII value
 * than the second character in the range, no range will be
 * constructed. Only the start, end and period characters will be
 * escaped. Use the ord function to find the
 * ASCII value for a character.
 * ]]>
 * </p>
 * <p>
 * Be careful if you choose to escape characters 0, a, b, f, n, r,
 * t and v. They will be converted to \0, \a, \b, \f, \n, \r, \t
 * and \v.
 * In PHP \0 (NULL), \r (carriage return), \n (newline), \f (form feed),
 * \v (vertical tab) and \t (tab) are predefined escape sequences, 
 * while in C all of these are predefined escape sequences.
 * </p>
 * @return string the escaped string.
 * </p>
 */
function addcslashes ($str, $charlist) {}

/**
 * Strip whitespace (or other characters) from the end of a string
 * @link http://php.net/manual/en/function.rtrim.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param charlist string[optional] <p>
 * You can also specify the characters you want to strip, by means
 * of the charlist parameter.
 * Simply list all characters that you want to be stripped. With
 * .. you can specify a range of characters.
 * </p>
 * @return string the modified string.
 * </p>
 */
function rtrim ($str, $charlist = null) {}

/**
 * Replace all occurrences of the search string with the replacement string
 * @link http://php.net/manual/en/function.str-replace.php
 * @param search mixed <p>
 * </p>
 * @param replace mixed <p>
 * </p>
 * @param subject mixed <p>
 * If subject is an array, then the search and
 * replace is performed with every entry of
 * subject, and the return value is an array as
 * well.
 * </p>
 * @param count int[optional] If passed, this will hold the number of matched and replaced needles.
 * @return mixed This function returns a string or an array with the replaced values.
 * </p>
 */
function str_replace ($search, $replace, $subject, &$count = null) {}

/**
 * Case-insensitive version of <function>str_replace</function>.
 * @link http://php.net/manual/en/function.str-ireplace.php
 * @param search mixed <p>
 * Every replacement with search array is
 * performed on the result of previous replacement.
 * </p>
 * @param replace mixed <p>
 * </p>
 * @param subject mixed <p>
 * If subject is an array, then the search and
 * replace is performed with every entry of 
 * subject, and the return value is an array as
 * well.
 * </p>
 * @param count int[optional] <p>
 * The number of matched and replaced needles will
 * be returned in count which is passed by
 * reference.
 * </p>
 * @return mixed a string or an array of replacements.
 * </p>
 */
function str_ireplace ($search, $replace, $subject, &$count = null) {}

/**
 * Repeat a string
 * @link http://php.net/manual/en/function.str-repeat.php
 * @param input string <p>
 * The string to be repeated.
 * </p>
 * @param multiplier int <p>
 * Number of time the input string should be
 * repeated.
 * </p>
 * <p>
 * multiplier has to be greater than or equal to 0.
 * If the multiplier is set to 0, the function
 * will return an empty string.
 * </p>
 * @return string the repeated string.
 * </p>
 */
function str_repeat ($input, $multiplier) {}

/**
 * Return information about characters used in a string
 * @link http://php.net/manual/en/function.count-chars.php
 * @param string string <p>
 * The examined string.
 * </p>
 * @param mode int[optional] <p>
 * The optional parameter mode defaults to 0.
 * </p>
 * @return mixed Depending on mode
 * count_chars returns one of the following:
 * 0 - an array with the byte-value as key and the frequency of
 * every byte as value.
 * 1 - same as 0 but only byte-values with a frequency greater
 * than zero are listed.
 * 2 - same as 0 but only byte-values with a frequency equal to
 * zero are listed.
 * 3 - a string containing all unique characters is returned.
 * 4 - a string containing all not used characters is returned.
 * </p>
 */
function count_chars ($string, $mode = null) {}

/**
 * Split a string into smaller chunks
 * @link http://php.net/manual/en/function.chunk-split.php
 * @param body string <p>
 * The string to be chunked.
 * </p>
 * @param chunklen int[optional] <p>
 * The chunk length. Defaults to 76.
 * </p>
 * @param end string[optional] <p>
 * The line ending sequence. Defaults to "\r\n".
 * </p>
 * @return string the chunked string.
 * </p>
 */
function chunk_split ($body, $chunklen = null, $end = null) {}

/**
 * Strip whitespace (or other characters) from the beginning and end of a string
 * @link http://php.net/manual/en/function.trim.php
 * @param str string <p>
 * The string that will be trimmed.
 * </p>
 * @param charlist string[optional] <p>
 * Optionally, the stripped characters can also be specified using
 * the charlist parameter.
 * Simply list all characters that you want to be stripped. With
 * .. you can specify a range of characters.
 * </p>
 * @return string The trimmed string.
 * </p>
 */
function trim ($str, $charlist = null) {}

/**
 * Strip whitespace (or other characters) from the beginning of a string
 * @link http://php.net/manual/en/function.ltrim.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param charlist string[optional] <p>
 * You can also specify the characters you want to strip, by means of the
 * charlist parameter.
 * Simply list all characters that you want to be stripped. With
 * .. you can specify a range of characters.
 * </p>
 * @return string This function returns a string with whitespace stripped from the
 * beginning of str.
 * Without the second parameter,
 * ltrim will strip these characters:
 * " " (ASCII 32
 * (0x20)), an ordinary space.
 * "\t" (ASCII 9
 * (0x09)), a tab.
 * "\n" (ASCII 10
 * (0x0A)), a new line (line feed).
 * "\r" (ASCII 13
 * (0x0D)), a carriage return.
 * "\0" (ASCII 0
 * (0x00)), the NUL-byte.
 * "\x0B" (ASCII 11
 * (0x0B)), a vertical tab.
 * </p>
 */
function ltrim ($str, $charlist = null) {}

/**
 * Strip HTML and PHP tags from a string
 * @link http://php.net/manual/en/function.strip-tags.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param allowable_tags string[optional] <p>
 * You can use the optional second parameter to specify tags which should
 * not be stripped.
 * </p>
 * <p>
 * HTML comments and PHP tags are also stripped. This is hardcoded and
 * can not be changed with allowable_tags.
 * </p>
 * @return string the stripped string.
 * </p>
 */
function strip_tags ($str, $allowable_tags = null) {}

/**
 * Calculate the similarity between two strings
 * @link http://php.net/manual/en/function.similar-text.php
 * @param first string <p>
 * The first string.
 * </p>
 * @param second string <p>
 * The second string.
 * </p>
 * @param percent float[optional] <p>
 * By passing a reference as third argument,
 * similar_text will calculate the similarity in
 * percent for you. 
 * </p>
 * @return int the number of matching chars in both strings.
 * </p>
 */
function similar_text ($first, $second, &$percent = null) {}

/**
 * Split a string by string
 * @link http://php.net/manual/en/function.explode.php
 * @param delimiter string <p>
 * The boundary string.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @param limit int[optional] <p>
 * If limit is set, the returned array will contain
 * a maximum of limit elements with the last
 * element containing the rest of string.
 * </p>
 * <p>
 * If the limit parameter is negative, all components
 * except the last -limit are returned.
 * </p>
 * @return array If delimiter is an empty string (""),
 * explode will return false. If
 * delimiter contains a value that is not contained
 * in string, then explode will
 * return an array containing string.
 * </p>
 */
function explode ($delimiter, $string, $limit = null) {}

/**
 * Join array elements with a string
 * @link http://php.net/manual/en/function.implode.php
 * @param glue string <p>
 * Defaults to an empty string. This is not the preferred usage of
 * implode as glue would be
 * the second parameter and thus, the bad prototype would be used.
 * </p>
 * @param pieces array <p>
 * The array of strings to implode.
 * </p>
 * @return string a string containing a string representation of all the array
 * elements in the same order, with the glue string between each element.
 * </p>
 */
function implode ($glue, array $pieces) {}

/**
 * Set locale information
 * @link http://php.net/manual/en/function.setlocale.php
 * @param category int <p>
 * category is a named constant specifying the
 * category of the functions affected by the locale setting:
 * LC_ALL for all of the below
 * @param locale string <p>
 * If locale is &null; or the empty string
 * "", the locale names will be set from the
 * values of environment variables with the same names as the above
 * categories, or from "LANG".
 * </p>
 * <p>
 * If locale is "0",
 * the locale setting is not affected, only the current setting is returned.
 * </p>
 * <p>
 * If locale is an array or followed by additional
 * parameters then each array element or parameter is tried to be set as
 * new locale until success. This is useful if a locale is known under
 * different names on different systems or for providing a fallback
 * for a possibly not available locale.
 * </p>
 * @param _ string[optional] 
 * @return string the new current locale, or false if the locale functionality is
 * not implemented on your platform, the specified locale does not exist or
 * the category name is invalid.
 * </p>
 * <p>
 * An invalid category name also causes a warning message. Category/locale
 * names can be found in RFC 1766
 * and ISO 639.
 * Different systems have different naming schemes for locales.
 * </p>
 * <p>
 * The return value of setlocale depends
 * on the system that PHP is running. It returns exactly
 * what the system setlocale function returns.
 * </p>
 */
function setlocale ($category, $locale, $_ = null) {}

/**
 * Get numeric formatting information
 * @link http://php.net/manual/en/function.localeconv.php
 * @return array localeconv returns data based upon the current locale
 * as set by setlocale. The associative array that is
 * returned contains the following fields:
 * <tr valign="top">
 * <td>Array element</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>decimal_point</td>
 * <td>Decimal point character</td>
 * </tr>
 * <tr valign="top">
 * <td>thousands_sep</td>
 * <td>Thousands separator</td>
 * </tr>
 * <tr valign="top">
 * <td>grouping</td>
 * <td>Array containing numeric groupings</td>
 * </tr>
 * <tr valign="top">
 * <td>int_curr_symbol</td>
 * <td>International currency symbol (i.e. USD)</td>
 * </tr>
 * <tr valign="top">
 * <td>currency_symbol</td>
 * <td>Local currency symbol (i.e. $)</td>
 * </tr>
 * <tr valign="top">
 * <td>mon_decimal_point</td>
 * <td>Monetary decimal point character</td>
 * </tr>
 * <tr valign="top">
 * <td>mon_thousands_sep</td>
 * <td>Monetary thousands separator</td>
 * </tr>
 * <tr valign="top">
 * <td>mon_grouping</td>
 * <td>Array containing monetary groupings</td>
 * </tr>
 * <tr valign="top">
 * <td>positive_sign</td>
 * <td>Sign for positive values</td>
 * </tr>
 * <tr valign="top">
 * <td>negative_sign</td>
 * <td>Sign for negative values</td>
 * </tr>
 * <tr valign="top">
 * <td>int_frac_digits</td>
 * <td>International fractional digits</td>
 * </tr>
 * <tr valign="top">
 * <td>frac_digits</td>
 * <td>Local fractional digits</td>
 * </tr>
 * <tr valign="top">
 * <td>p_cs_precedes</td>
 * <td>
 * true if currency_symbol precedes a positive value, false
 * if it succeeds one
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>p_sep_by_space</td>
 * <td>
 * true if a space separates currency_symbol from a positive
 * value, false otherwise
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>n_cs_precedes</td>
 * <td>
 * true if currency_symbol precedes a negative value, false
 * if it succeeds one
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>n_sep_by_space</td>
 * <td>
 * true if a space separates currency_symbol from a negative
 * value, false otherwise
 * </td>
 * </tr>
 * <td>p_sign_posn</td>
 * <td>
 * 0 - Parentheses surround the quantity and currency_symbol
 * 1 - The sign string precedes the quantity and currency_symbol
 * 2 - The sign string succeeds the quantity and currency_symbol
 * 3 - The sign string immediately precedes the currency_symbol
 * 4 - The sign string immediately succeeds the currency_symbol
 * </td>
 * </tr>
 * <td>n_sign_posn</td>
 * <td>
 * 0 - Parentheses surround the quantity and currency_symbol
 * 1 - The sign string precedes the quantity and currency_symbol
 * 2 - The sign string succeeds the quantity and currency_symbol
 * 3 - The sign string immediately precedes the currency_symbol
 * 4 - The sign string immediately succeeds the currency_symbol
 * </td>
 * </tr>
 * </p>
 * <p>
 * The p_sign_posn, and n_sign_posn contain a string
 * of formatting options. Each number representing one of the above listed conditions.
 * </p>
 * <p>
 * The grouping fields contain arrays that define the way numbers should be
 * grouped. For example, the monetary grouping field for the nl_NL locale (in
 * UTF-8 mode with the euro sign), would contain a 2 item array with the
 * values 3 and 3. The higher the index in the array, the farther left the
 * grouping is. If an array element is equal to CHAR_MAX,
 * no further grouping is done. If an array element is equal to 0, the previous
 * element should be used.
 * </p>
 */
function localeconv () {}

/**
 * Query language and locale information
 * @link http://php.net/manual/en/function.nl-langinfo.php
 * @param item int <p>
 * item may be an integer value of the element or the
 * constant name of the element. The following is a list of constant names
 * for item that may be used and their description.
 * Some of these constants may not be defined or hold no value for certain
 * locales.
 * <table>
 * nl_langinfo Constants
 * <tr valign="top">
 * <td>Constant</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * LC_TIME Category Constants</td>
 * </tr>
 * <tr valign="top">
 * <td>ABDAY_(1-7)</td>
 * <td>Abbreviated name of n-th day of the week.</td>
 * </tr>
 * <tr valign="top">
 * <td>DAY_(1-7)</td>
 * <td>Name of the n-th day of the week (DAY_1 = Sunday).</td>
 * </tr>
 * <tr valign="top">
 * <td>ABMON_(1-12)</td>
 * <td>Abbreviated name of the n-th month of the year.</td>
 * </tr>
 * <tr valign="top">
 * <td>MON_(1-12)</td>
 * <td>Name of the n-th month of the year.</td>
 * </tr>
 * <tr valign="top">
 * <td>AM_STR</td>
 * <td>String for Ante meridian.</td>
 * </tr>
 * <tr valign="top">
 * <td>PM_STR</td>
 * <td>String for Post meridian.</td>
 * </tr>
 * <tr valign="top">
 * <td>D_T_FMT</td>
 * <td>String that can be used as the format string for strftime to represent time and date.</td>
 * </tr>
 * <tr valign="top">
 * <td>D_FMT</td>
 * <td>String that can be used as the format string for strftime to represent date.</td>
 * </tr>
 * <tr valign="top">
 * <td>T_FMT</td>
 * <td>String that can be used as the format string for strftime to represent time.</td>
 * </tr>
 * <tr valign="top">
 * <td>T_FMT_AMPM</td>
 * <td>String that can be used as the format string for strftime to represent time in 12-hour format with ante/post meridian.</td>
 * </tr>
 * <tr valign="top">
 * <td>ERA</td>
 * <td>Alternate era.</td>
 * </tr>
 * <tr valign="top">
 * <td>ERA_YEAR</td>
 * <td>Year in alternate era format.</td>
 * </tr>
 * <tr valign="top">
 * <td>ERA_D_T_FMT</td>
 * <td>Date and time in alternate era format (string can be used in strftime).</td>
 * </tr>
 * <tr valign="top">
 * <td>ERA_D_FMT</td>
 * <td>Date in alternate era format (string can be used in strftime).</td>
 * </tr>
 * <tr valign="top">
 * <td>ERA_T_FMT</td>
 * <td>Time in alternate era format (string can be used in strftime).</td>
 * </tr>
 * <tr valign="top">
 * LC_MONETARY Category Constants</td>
 * </tr>
 * <tr valign="top">
 * <td>INT_CURR_SYMBOL</td>
 * <td>International currency symbol.</td>
 * </tr>
 * <tr valign="top">
 * <td>CURRENCY_SYMBOL</td>
 * <td>Local currency symbol.</td>
 * </tr>
 * <tr valign="top">
 * <td>CRNCYSTR</td>
 * <td>Same value as CURRENCY_SYMBOL.</td>
 * </tr>
 * <tr valign="top">
 * <td>MON_DECIMAL_POINT</td>
 * <td>Decimal point character.</td>
 * </tr>
 * <tr valign="top">
 * <td>MON_THOUSANDS_SEP</td>
 * <td>Thousands separator (groups of three digits).</td>
 * </tr>
 * <tr valign="top">
 * <td>MON_GROUPING</td>
 * <td>Like 'grouping' element.</td>
 * </tr>
 * <tr valign="top">
 * <td>POSITIVE_SIGN</td>
 * <td>Sign for positive values.</td>
 * </tr>
 * <tr valign="top">
 * <td>NEGATIVE_SIGN</td>
 * <td>Sign for negative values.</td>
 * </tr>
 * <tr valign="top">
 * <td>INT_FRAC_DIGITS</td>
 * <td>International fractional digits.</td>
 * </tr>
 * <tr valign="top">
 * <td>FRAC_DIGITS</td>
 * <td>Local fractional digits.</td>
 * </tr>
 * <tr valign="top">
 * <td>P_CS_PRECEDES</td>
 * <td>Returns 1 if CURRENCY_SYMBOL precedes a positive value.</td>
 * </tr>
 * <tr valign="top">
 * <td>P_SEP_BY_SPACE</td>
 * <td>Returns 1 if a space separates CURRENCY_SYMBOL from a positive value.</td>
 * </tr>
 * <tr valign="top">
 * <td>N_CS_PRECEDES</td>
 * <td>Returns 1 if CURRENCY_SYMBOL precedes a negative value.</td>
 * </tr>
 * <tr valign="top">
 * <td>N_SEP_BY_SPACE</td>
 * <td>Returns 1 if a space separates CURRENCY_SYMBOL from a negative value.</td>
 * </tr>
 * <tr valign="top">
 * <td>P_SIGN_POSN</td>
 * Returns 0 if parentheses surround the quantity and currency_symbol.
 * @return string the element as a string, or false if item
 * is not valid.
 * </p>
 */
function nl_langinfo ($item) {}

/**
 * Calculate the soundex key of a string
 * @link http://php.net/manual/en/function.soundex.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the soundex key as a string.
 * </p>
 */
function soundex ($str) {}

/**
 * Calculate Levenshtein distance between two strings
 * @link http://php.net/manual/en/function.levenshtein.php
 * @param str1 string <p>
 * One of the strings being evaluated for Levenshtein distance.
 * </p>
 * @param str2 string <p>
 * One of the strings being evaluated for Levenshtein distance.
 * </p>
 * @return int This function returns the Levenshtein-Distance between the
 * two argument strings or -1, if one of the argument strings
 * is longer than the limit of 255 characters.
 * </p>
 */
function levenshtein ($str1, $str2) {}

/**
 * Return a specific character
 * @link http://php.net/manual/en/function.chr.php
 * @param ascii int <p>
 * The ascii code.
 * </p>
 * @return string the specified character.
 * </p>
 */
function chr ($ascii) {}

/**
 * Return ASCII value of character
 * @link http://php.net/manual/en/function.ord.php
 * @param string string <p>
 * A character.
 * </p>
 * @return int the ASCII value as an integer.
 * </p>
 */
function ord ($string) {}

/**
 * Parses the string into variables
 * @link http://php.net/manual/en/function.parse-str.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param arr array[optional] <p>
 * If the second parameter arr is present,
 * variables are stored in this variable as array elements instead.
 * </p>
 * @return void &return.void;
 * </p>
 */
function parse_str ($str, array &$arr = null) {}

/**
 * Pad a string to a certain length with another string
 * @link http://php.net/manual/en/function.str-pad.php
 * @param input string <p>
 * The input string.
 * </p>
 * @param pad_length int <p>
 * If the value of pad_length is negative or
 * less than the length of the input string, no padding takes place.
 * </p>
 * @param pad_string string[optional] <p>
 * The pad_string may be truncated if the
 * required number of padding characters can't be evenly divided by the
 * pad_string's length.
 * </p>
 * @param pad_type int[optional] <p>
 * Optional argument pad_type can be
 * STR_PAD_RIGHT, STR_PAD_LEFT,
 * or STR_PAD_BOTH. If
 * pad_type is not specified it is assumed to be
 * STR_PAD_RIGHT.
 * </p>
 * @return string the padded string.
 * </p>
 */
function str_pad ($input, $pad_length, $pad_string = null, $pad_type = null) {}

/**
 * &Alias; <function>rtrim</function>
 * @link http://php.net/manual/en/function.chop.php
 * @param str
 * @param character_mask[optional]
 */
function chop ($str, $character_mask) {}

/**
 * &Alias; <function>strstr</function>
 * @link http://php.net/manual/en/function.strchr.php
 * @param haystack
 * @param needle
 */
function strchr ($haystack, $needle) {}

/**
 * Return a formatted string
 * @link http://php.net/manual/en/function.sprintf.php
 * @param format string <p>
 * The format string is composed of zero or more directives:
 * ordinary characters (excluding %) that are
 * copied directly to the result, and conversion
 * specifications, each of which results in fetching its
 * own parameter. This applies to both sprintf
 * and printf.
 * </p>
 * <p>
 * Each conversion specification consists of a percent sign
 * (%), followed by one or more of these
 * elements, in order:
 * An optional sign specifier that forces a sign
 * (- or +) to be used on a number. By default, only the - sign is used
 * on a number if it's negative. This specifier forces positive numbers
 * to have the + sign attached as well, and was added in PHP 4.3.0.
 * @param args mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return string a string produced according to the formatting string
 * format.
 * </p>
 */
function sprintf ($format, $args = null, $_ = null) {}

/**
 * Output a formatted string
 * @link http://php.net/manual/en/function.printf.php
 * @param format string <p>
 * See sprintf for a description of
 * format.
 * </p>
 * @param args mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return int the length of the outputted string.
 * </p>
 */
function printf ($format, $args = null, $_ = null) {}

/**
 * Output a formatted string
 * @link http://php.net/manual/en/function.vprintf.php
 * @param format string <p>
 * See sprintf for a description of
 * format.
 * </p>
 * @param args array <p>
 * </p>
 * @return int the length of the outputted string.
 * </p>
 */
function vprintf ($format, array $args) {}

/**
 * Return a formatted string
 * @link http://php.net/manual/en/function.vsprintf.php
 * @param format string <p>
 * See sprintf for a description of
 * format.
 * </p>
 * @param args array <p>
 * </p>
 * @return string Return array values as a formatted string according to
 * format (which is described in the documentation
 * for sprintf).
 * </p>
 */
function vsprintf ($format, array $args) {}

/**
 * Write a formatted string to a stream
 * @link http://php.net/manual/en/function.fprintf.php
 * @param handle resource &fs.file.pointer;
 * @param format string <p>
 * See sprintf for a description of 
 * format.
 * </p>
 * @param args mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return int the length of the string written.
 * </p>
 */
function fprintf ($handle, $format, $args = null, $_ = null) {}

/**
 * Write a formatted string to a stream
 * @link http://php.net/manual/en/function.vfprintf.php
 * @param handle resource <p>
 * </p>
 * @param format string <p>
 * See sprintf for a description of
 * format.
 * </p>
 * @param args array <p>
 * </p>
 * @return int the length of the outputted string.
 * </p>
 */
function vfprintf ($handle, $format, array $args) {}

/**
 * Parses input from a string according to a format
 * @link http://php.net/manual/en/function.sscanf.php
 * @param str string <p>
 * The input string being parsed.
 * </p>
 * @param format string <p>
 * The interpreted format for str, which is
 * described in the documentation for sprintf.
 * </p>
 * @param _ mixed[optional] 
 * @return mixed If only
 * two parameters were passed to this function, the values parsed
 * will be returned as an array. Otherwise, if optional parameters are passed,
 * the function will return the number of assigned values. The optional
 * parameters must be passed by reference.
 * </p>
 */
function sscanf ($str, $format, &$_ = null) {}

/**
 * Parses input from a file according to a format
 * @link http://php.net/manual/en/function.fscanf.php
 * @param handle resource &fs.file.pointer;
 * @param format string <p>
 * The specified format as described in the 
 * sprintf documentation.
 * </p>
 * @param _ mixed[optional] 
 * @return mixed If only two parameters were passed to this function, the values parsed will be
 * returned as an array. Otherwise, if optional parameters are passed, the
 * function will return the number of assigned values. The optional
 * parameters must be passed by reference. 
 * </p>
 */
function fscanf ($handle, $format, &$_ = null) {}

/**
 * Parse a URL and return its components
 * @link http://php.net/manual/en/function.parse-url.php
 * @param url string <p>
 * The URL to parse
 * </p>
 * @param component int[optional] <p>
 * Specify one of PHP_URL_SCHEME,
 * PHP_URL_HOST, PHP_URL_PORT,
 * PHP_URL_USER, PHP_URL_PASS,
 * PHP_URL_PATH, PHP_URL_QUERY
 * or PHP_URL_FRAGMENT to retrieve just a specific
 * URL component as a string.
 * </p>
 * @return mixed On seriously malformed URLs, parse_url may return
 * false and emit a E_WARNING. Otherwise an associative
 * array is returned, whose components may be (at least one):
 * scheme - e.g. http
 * host 
 * port
 * user
 * pass
 * path
 * query - after the question mark ?
 * fragment - after the hashmark #
 * </p>
 * <p>
 * If the component parameter is specified a
 * string is returned instead of an array.
 * </p>
 */
function parse_url ($url, $component = null) {}

/**
 * URL-encodes string
 * @link http://php.net/manual/en/function.urlencode.php
 * @param str string <p>
 * The string to be encoded.
 * </p>
 * @return string a string in which all non-alphanumeric characters except
 * -_. have been replaced with a percent
 * (%) sign followed by two hex digits and spaces encoded
 * as plus (+) signs. It is encoded the same way that the
 * posted data from a WWW form is encoded, that is the same way as in
 * application/x-www-form-urlencoded media type. This
 * differs from the RFC 1738 encoding (see
 * rawurlencode) in that for historical reasons, spaces
 * are encoded as plus (+) signs.
 * </p>
 */
function urlencode ($str) {}

/**
 * Decodes URL-encoded string
 * @link http://php.net/manual/en/function.urldecode.php
 * @param str string <p>
 * The string to be decoded.
 * </p>
 * @return string the decoded string.
 * </p>
 */
function urldecode ($str) {}

/**
 * URL-encode according to RFC 1738
 * @link http://php.net/manual/en/function.rawurlencode.php
 * @param str string <p>
 * The URL to be encoded.
 * </p>
 * @return string a string in which all non-alphanumeric characters except
 * -_. have been replaced with a percent
 * (%) sign followed by two hex digits. This is the
 * encoding described in RFC 1738 for
 * protecting literal characters from being interpreted as special URL
 * delimiters, and for protecting URLs from being mangled by transmission
 * media with character conversions (like some email systems). 
 * </p>
 */
function rawurlencode ($str) {}

/**
 * Decode URL-encoded strings
 * @link http://php.net/manual/en/function.rawurldecode.php
 * @param str string <p>
 * The URL to be decoded.
 * </p>
 * @return string the decoded URL, as a string.
 * </p>
 */
function rawurldecode ($str) {}

/**
 * Generate URL-encoded query string
 * @link http://php.net/manual/en/function.http-build-query.php
 * @param formdata array <p>
 * May be an array or object containing properties.
 * </p>
 * <p>
 * The array form may be a simple one-dimensional structure, or an array
 * of arrays (who in turn may contain other arrays).
 * </p>
 * @param numeric_prefix string[optional] <p>
 * If numeric indices are used in the base array and this parameter is
 * provided, it will be prepended to the numeric index for elements in
 * the base array only.
 * </p>
 * <p>
 * This is meant to allow for legal variable names when the data is
 * decoded by PHP or another CGI application later on.
 * </p>
 * @param arg_separator string[optional] <p>
 * arg_separator.output
 * is used to separate arguments, unless this parameter is specified,
 * and is then used.
 * </p>
 * @return string a URL-encoded string.
 * </p>
 */
function http_build_query (array $formdata, $numeric_prefix = null, $arg_separator = null) {}

/**
 * Returns the target of a symbolic link
 * @link http://php.net/manual/en/function.readlink.php
 * @param path string <p>
 * The symbolic link path.
 * </p>
 * @return string the contents of the symbolic link path or false on error.
 * </p>
 */
function readlink ($path) {}

/**
 * Gets information about a link
 * @link http://php.net/manual/en/function.linkinfo.php
 * @param path string <p>
 * Path to the link.
 * </p>
 * @return int linkinfo returns the st_dev field
 * of the Unix C stat structure returned by the lstat
 * system call. Returns 0 or false in case of error.
 * </p>
 */
function linkinfo ($path) {}

/**
 * Creates a symbolic link
 * @link http://php.net/manual/en/function.symlink.php
 * @param target string <p>
 * Target of the link.
 * </p>
 * @param link string <p>
 * The link name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function symlink ($target, $link) {}

/**
 * Create a hard link
 * @link http://php.net/manual/en/function.link.php
 * @param target string <p>
 * Target of the link.
 * </p>
 * @param link string <p>
 * The link name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function link ($target, $link) {}

/**
 * Deletes a file
 * @link http://php.net/manual/en/function.unlink.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @param context resource[optional] &note.context-support;
 * @return bool &return.success;
 * </p>
 */
function unlink ($filename, $context = null) {}

/**
 * Execute an external program
 * @link http://php.net/manual/en/function.exec.php
 * @param command string <p>
 * The command that will be executed.
 * </p>
 * @param output array[optional] <p>
 * If the output argument is present, then the
 * specified array will be filled with every line of output from the
 * command. Trailing whitespace, such as \n, is not
 * included in this array. Note that if the array already contains some
 * elements, exec will append to the end of the array.
 * If you do not want the function to append elements, call
 * unset on the array before passing it to
 * exec.
 * </p>
 * @param return_var int[optional] <p>
 * If the return_var argument is present
 * along with the output argument, then the
 * return status of the executed command will be written to this
 * variable.
 * </p>
 * @return string The last line from the result of the command. If you need to execute a 
 * command and have all the data from the command passed directly back without 
 * any interference, use the passthru function.
 * </p>
 * <p>
 * To get the output of the executed command, be sure to set and use the
 * output parameter.
 * </p>
 */
function exec ($command, array &$output = null, &$return_var = null) {}

/**
 * Execute an external program and display the output
 * @link http://php.net/manual/en/function.system.php
 * @param command string <p>
 * The command that will be executed.
 * </p>
 * @param return_var int[optional] <p>
 * If the return_var argument is present, then the
 * return status of the executed command will be written to this
 * variable.
 * </p>
 * @return string the last line of the command output on success, and false
 * on failure.
 * </p>
 */
function system ($command, &$return_var = null) {}

/**
 * Escape shell metacharacters
 * @link http://php.net/manual/en/function.escapeshellcmd.php
 * @param command string <p>
 * The command that will be escaped.
 * </p>
 * @return string The escaped string.
 * </p>
 */
function escapeshellcmd ($command) {}

/**
 * Escape a string to be used as a shell argument
 * @link http://php.net/manual/en/function.escapeshellarg.php
 * @param arg string <p>
 * The argument that will be escaped.
 * </p>
 * @return string The escaped string.
 * </p>
 */
function escapeshellarg ($arg) {}

/**
 * Execute an external program and display raw output
 * @link http://php.net/manual/en/function.passthru.php
 * @param command string <p>
 * The command that will be executed.
 * </p>
 * @param return_var int[optional] <p>
 * If the return_var argument is present, the 
 * return status of the Unix command will be placed here.
 * </p>
 * @return void &return.void;
 * </p>
 */
function passthru ($command, &$return_var = null) {}

/**
 * Execute command via shell and return the complete output as a string
 * @link http://php.net/manual/en/function.shell-exec.php
 * @param cmd string <p>
 * The command that will be executed.
 * </p>
 * @return string The output from the executed command.
 * </p>
 */
function shell_exec ($cmd) {}

/**
 * Execute a command and open file pointers for input/output
 * @link http://php.net/manual/en/function.proc-open.php
 * @param cmd string <p>
 * The command to execute
 * </p>
 * @param descriptorspec array <p>
 * An indexed array where the key represents the descriptor number and the
 * value represents how PHP will pass that descriptor to the child
 * process. 0 is stdin, 1 is stdout, while 2 is stderr.
 * </p>
 * <p>
 * The currently supported pipe types are file and
 * pipe .
 * </p>
 * <p>
 * The file descriptor numbers are not limited to 0, 1 and 2 - you may
 * specify any valid file descriptor number and it will be passed to the
 * child process. This allows your script to interoperate with other
 * scripts that run as "co-processes". In particular, this is useful for
 * passing passphrases to programs like PGP, GPG and openssl in a more
 * secure manner. It is also useful for reading status information
 * provided by those programs on auxiliary file descriptors.
 * </p>
 * @param pipes array <p>
 * Will be set to an indexed array of file pointers that correspond to
 * PHP's end of any pipes that are created.
 * </p>
 * @param cwd string[optional] <p>
 * The initial working dir for the command. This must be an
 * absolute directory path, or &null;
 * if you want to use the default value (the working dir of the current
 * PHP process)
 * </p>
 * @param env array[optional] <p>
 * An array with the environment variables for the command that will be
 * run, or &null; to use the same environment as the current PHP process
 * </p>
 * @param other_options array[optional] <p>
 * Allows you to specify additional options. Currently supported options
 * include:
 * suppress_errors (windows only): suppresses errors
 * generated by this function when it's set to true
 * bypass_shell (windows only): bypass
 * cmd.exe shell when set to true
 * context: stream context used when opening files
 * (created with stream_context_create)
 * binary_pipes: open pipes in binary mode, instead
 * of using the usual stream_encoding
 * </p>
 * @return resource a resource representing the process, which should be freed using
 * proc_close when you are finished with it. On failure
 * returns false.
 * </p>
 */
function proc_open ($cmd, array $descriptorspec, array &$pipes, $cwd = null, array $env = null, array $other_options = null) {}

/**
 * Close a process opened by <function>proc_open</function> and return the exit code of that process.
 * @link http://php.net/manual/en/function.proc-close.php
 * @param process resource <p>
 * The proc_open resource that will
 * be closed.
 * </p>
 * @return int the termination status of the process that was run.
 * </p>
 */
function proc_close ($process) {}

/**
 * Kills a process opened by proc_open
 * @link http://php.net/manual/en/function.proc-terminate.php
 * @param process resource <p>
 * The proc_open resource that will
 * be closed.
 * </p>
 * @param signal int[optional] <p>
 * This optional parameter is only useful on POSIX
 * operating systems; you may specify a signal to send to the process
 * using the kill(2) system call. The default is
 * SIGTERM.
 * </p>
 * @return bool the termination status of the process that was run.
 * </p>
 */
function proc_terminate ($process, $signal = null) {}

/**
 * Get information about a process opened by <function>proc_open</function>
 * @link http://php.net/manual/en/function.proc-get-status.php
 * @param process resource <p>
 * The proc_open resource that will
 * be evaluated.
 * </p>
 * @return array An array of collected information on success, and false
 * on failure. The returned array contains the following elements:
 * </p>
 * <p>
 * <tr valign="top"><td>element</td><td>type</td><td>description</td></tr>
 * <tr valign="top">
 * <td>command</td>
 * <td>string</td>
 * <td>
 * The command string that was passed to proc_open.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>pid</td>
 * <td>int</td>
 * <td>process id</td>
 * </tr>
 * <tr valign="top">
 * <td>running</td>
 * <td>bool</td>
 * <td>
 * true if the process is still running, false if it has
 * terminated.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>signaled</td>
 * <td>bool</td>
 * <td>
 * true if the child process has been terminated by
 * an uncaught signal. Always set to false on Windows.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>stopped</td>
 * <td>bool</td>
 * <td>
 * true if the child process has been stopped by a
 * signal. Always set to false on Windows.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>exitcode</td>
 * <td>int</td>
 * <td>
 * The exit code returned by the process (which is only
 * meaningful if running is false).
 * Only first call of this function return real value, next calls return
 * -1.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>termsig</td>
 * <td>int</td>
 * <td>
 * The number of the signal that caused the child process to terminate
 * its execution (only meaningful if signaled is true).
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>stopsig</td>
 * <td>int</td>
 * <td>
 * The number of the signal that caused the child process to stop its
 * execution (only meaningful if stopped is true).
 * </td>
 * </tr>
 * </p>
 */
function proc_get_status ($process) {}

/**
 * Change the priority of the current process
 * @link http://php.net/manual/en/function.proc-nice.php
 * @param increment int <p>
 * The increment value of the priority change.
 * </p>
 * @return bool &return.success;
 * If an error occurs, like the user lacks permission to change the priority, 
 * an error of level E_WARNING is also generated.
 * </p>
 */
function proc_nice ($increment) {}

/**
 * Generate a random integer
 * @link http://php.net/manual/en/function.rand.php
 * @param min[optional]
 * @param max[optional]
 * @return int A pseudo random value between min
 * (or 0) and max (or RAND_MAX, inclusive).
 * </p>
 */
function rand ($min, $max) {}

/**
 * Seed the random number generator
 * @link http://php.net/manual/en/function.srand.php
 * @param seed int[optional] <p>
 * Optional seed value
 * </p>
 * @return void 
 */
function srand ($seed = null) {}

/**
 * Show largest possible random value
 * @link http://php.net/manual/en/function.getrandmax.php
 * @return int The largest possible random value returned by rand
 * </p>
 */
function getrandmax () {}

/**
 * Generate a better random value
 * @link http://php.net/manual/en/function.mt-rand.php
 * @param min[optional]
 * @param max[optional]
 * @return int A random integer value between min (or 0)
 * and max (or RAND_MAX, inclusive)
 * </p>
 */
function mt_rand ($min, $max) {}

/**
 * Seed the better random number generator
 * @link http://php.net/manual/en/function.mt-srand.php
 * @param seed int[optional] <p>
 * An optional seed value
 * </p>
 * @return void 
 */
function mt_srand ($seed = null) {}

/**
 * Show largest possible random value
 * @link http://php.net/manual/en/function.mt-getrandmax.php
 * @return int the maximum random value returned by mt_rand
 * </p>
 */
function mt_getrandmax () {}

/**
 * Get port number associated with an Internet service and protocol
 * @link http://php.net/manual/en/function.getservbyname.php
 * @param service string <p>
 * The Internet service name, as a string.
 * </p>
 * @param protocol string <p>
 * protocol is either "tcp"
 * or "udp" (in lowercase).
 * </p>
 * @return int the port number, or false if service or
 * protocol is not found.
 * </p>
 */
function getservbyname ($service, $protocol) {}

/**
 * Get Internet service which corresponds to port and protocol
 * @link http://php.net/manual/en/function.getservbyport.php
 * @param port int <p>
 * The port number.
 * </p>
 * @param protocol string <p>
 * protocol is either "tcp"
 * or "udp" (in lowercase).
 * </p>
 * @return string the Internet service name as a string.
 * </p>
 */
function getservbyport ($port, $protocol) {}

/**
 * Get protocol number associated with protocol name
 * @link http://php.net/manual/en/function.getprotobyname.php
 * @param name string <p>
 * The protocol name.
 * </p>
 * @return int the protocol number or -1 if the protocol is not found.
 * </p>
 */
function getprotobyname ($name) {}

/**
 * Get protocol name associated with protocol number
 * @link http://php.net/manual/en/function.getprotobynumber.php
 * @param number int <p>
 * The protocol number.
 * </p>
 * @return string the protocol name as a string.
 * </p>
 */
function getprotobynumber ($number) {}

/**
 * Gets PHP script owner's UID
 * @link http://php.net/manual/en/function.getmyuid.php
 * @return int the user ID of the current script, or false on error.
 * </p>
 */
function getmyuid () {}

/**
 * Get PHP script owner's GID
 * @link http://php.net/manual/en/function.getmygid.php
 * @return int the group ID of the current script, or false on error.
 * </p>
 */
function getmygid () {}

/**
 * Gets PHP's process ID
 * @link http://php.net/manual/en/function.getmypid.php
 * @return int the current PHP process ID, or false on error.
 * </p>
 */
function getmypid () {}

/**
 * Gets the inode of the current script
 * @link http://php.net/manual/en/function.getmyinode.php
 * @return int the current script's inode as an integer, or false on error.
 * </p>
 */
function getmyinode () {}

/**
 * Gets time of last page modification
 * @link http://php.net/manual/en/function.getlastmod.php
 * @return int the time of the last modification of the current
 * page. The value returned is a Unix timestamp, suitable for
 * feeding to date. Returns false on error.
 * </p>
 */
function getlastmod () {}

/**
 * Decodes data encoded with MIME base64
 * @link http://php.net/manual/en/function.base64-decode.php
 * @param data string <p>
 * The decoded data.
 * </p>
 * @param strict bool[optional] <p>
 * Returns false if input contains space or some other separator.
 * </p>
 * @return string the original data or false on failure. The returned data may be
 * binary.
 * </p>
 */
function base64_decode ($data, $strict = null) {}

/**
 * Encodes data with MIME base64
 * @link http://php.net/manual/en/function.base64-encode.php
 * @param data string <p>
 * The data to encode.
 * </p>
 * @return string The encoded data, as a string.
 * </p>
 */
function base64_encode ($data) {}

/**
 * Uuencode a string
 * @link http://php.net/manual/en/function.convert-uuencode.php
 * @param data string <p>
 * The data to be encoded.
 * </p>
 * @return string the uuencoded data.
 * </p>
 */
function convert_uuencode ($data) {}

/**
 * Decode a uuencoded string
 * @link http://php.net/manual/en/function.convert-uudecode.php
 * @param data string <p>
 * The uuencoded data.
 * </p>
 * @return string the decoded data as a string.
 * </p>
 */
function convert_uudecode ($data) {}

/**
 * Absolute value
 * @link http://php.net/manual/en/function.abs.php
 * @param number mixed <p>
 * The numeric value to process
 * </p>
 * @return number The absoulte value of number. If the
 * argument number is
 * of type float, the return type is also float,
 * otherwise it is integer (as float usually has a
 * bigger value range than integer).
 * </p>
 */
function abs ($number) {}

/**
 * Round fractions up
 * @link http://php.net/manual/en/function.ceil.php
 * @param value float <p>
 * The value to round
 * </p>
 * @return float value rounded up to the next highest
 * integer.
 * The return value of ceil is still of type
 * float as the value range of float is 
 * usually bigger than that of integer.
 * </p>
 */
function ceil ($value) {}

/**
 * Round fractions down
 * @link http://php.net/manual/en/function.floor.php
 * @param value float 
 * @return float value rounded to the next lowest integer.
 * The return value of floor is still of type
 * float because the value range of float is 
 * usually bigger than that of integer.
 * </p>
 */
function floor ($value) {}

/**
 * Rounds a float
 * @link http://php.net/manual/en/function.round.php
 * @param val float <p>
 * The value to round
 * </p>
 * @param precision int[optional] <p>
 * The optional number of decimal digits to round to, defaults to 0
 * </p>
 * @return float The rounded value
 * </p>
 */
function round ($val, $precision = null) {}

/**
 * Sine
 * @link http://php.net/manual/en/function.sin.php
 * @param arg float <p>
 * A value in radians
 * </p>
 * @return float The sine of arg
 * </p>
 */
function sin ($arg) {}

/**
 * Cosine
 * @link http://php.net/manual/en/function.cos.php
 * @param arg float <p>
 * An angle in radians 
 * </p>
 * @return float The cosine of arg
 * </p>
 */
function cos ($arg) {}

/**
 * Tangent
 * @link http://php.net/manual/en/function.tan.php
 * @param arg float <p>
 * The argument to process in radians 
 * </p>
 * @return float The tangent of arg
 * </p>
 */
function tan ($arg) {}

/**
 * Arc sine
 * @link http://php.net/manual/en/function.asin.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The arc sine of arg in radians
 * </p>
 */
function asin ($arg) {}

/**
 * Arc cosine
 * @link http://php.net/manual/en/function.acos.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The arc consine of arg in radians.
 * </p>
 */
function acos ($arg) {}

/**
 * Arc tangent
 * @link http://php.net/manual/en/function.atan.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The arc tangent of arg in radians.
 * </p>
 */
function atan ($arg) {}

/**
 * Arc tangent of two variables
 * @link http://php.net/manual/en/function.atan2.php
 * @param y float <p>
 * Dividend parameter
 * </p>
 * @param x float <p>
 * Divisor parameter
 * </p>
 * @return float The arc tangent of y/x 
 * in radians.
 * </p>
 */
function atan2 ($y, $x) {}

/**
 * Hyperbolic sine
 * @link http://php.net/manual/en/function.sinh.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The hyperbolic sine of arg
 * </p>
 */
function sinh ($arg) {}

/**
 * Hyperbolic cosine
 * @link http://php.net/manual/en/function.cosh.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The hyperbolic cosine of arg
 * </p>
 */
function cosh ($arg) {}

/**
 * Hyperbolic tangent
 * @link http://php.net/manual/en/function.tanh.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The hyperbolic tangent of arg
 * </p>
 */
function tanh ($arg) {}

/**
 * Inverse hyperbolic sine
 * @link http://php.net/manual/en/function.asinh.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The inverse hyperbolic sine of arg 
 * </p>
 */
function asinh ($arg) {}

/**
 * Inverse hyperbolic cosine
 * @link http://php.net/manual/en/function.acosh.php
 * @param arg float <p>
 * The value to process
 * </p>
 * @return float The inverse hyperbolic cosine of arg
 * </p>
 */
function acosh ($arg) {}

/**
 * Inverse hyperbolic tangent
 * @link http://php.net/manual/en/function.atanh.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float Inverse hyperbolic tangent of arg 
 * </p>
 */
function atanh ($arg) {}

/**
 * Returns exp(number) - 1, computed in a way that is accurate even
   when the value of number is close to zero
 * @link http://php.net/manual/en/function.expm1.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float 'e' to the power of arg minus one
 * </p>
 */
function expm1 ($arg) {}

/**
 * Returns log(1 + number), computed in a way that is accurate even when
   the value of number is close to zero
 * @link http://php.net/manual/en/function.log1p.php
 * @param number float <p>
 * The argument to process
 * </p>
 * @return float log(1 + number)
 * </p>
 */
function log1p ($number) {}

/**
 * Get value of pi
 * @link http://php.net/manual/en/function.pi.php
 * @return float The value of pi as float.
 * </p>
 */
function pi () {}

/**
 * Finds whether a value is a legal finite number
 * @link http://php.net/manual/en/function.is-finite.php
 * @param val float <p>
 * The value to check
 * </p>
 * @return bool true if val is a legal finite
 * number within the allowed range for a PHP float on this platform,
 * else false.
 * </p>
 */
function is_finite ($val) {}

/**
 * Finds whether a value is not a number
 * @link http://php.net/manual/en/function.is-nan.php
 * @param val float <p>
 * The value to check
 * </p>
 * @return bool true if val is 'not a number',
 * else false.
 * </p>
 */
function is_nan ($val) {}

/**
 * Finds whether a value is infinite
 * @link http://php.net/manual/en/function.is-infinite.php
 * @param val float <p>
 * The value to check
 * </p>
 * @return bool true if val is infinite, else false.
 * </p>
 */
function is_infinite ($val) {}

/**
 * Exponential expression
 * @link http://php.net/manual/en/function.pow.php
 * @param base number <p>
 * The base to use
 * </p>
 * @param exp number <p>
 * The exponent
 * </p>
 * @return number base raised to the power of exp.
 * If the result can be represented as integer it will be returned as type
 * integer, else it will be returned as type float.
 * If the power cannot be computed false will be returned instead.
 * </p>
 */
function pow ($base, $exp) {}

/**
 * Calculates the exponent of <constant>e</constant>
 * @link http://php.net/manual/en/function.exp.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float 'e' raised to the power of arg
 * </p>
 */
function exp ($arg) {}

/**
 * Natural logarithm
 * @link http://php.net/manual/en/function.log.php
 * @param arg float <p>
 * The value to calculate the logarithm for
 * </p>
 * @param base float[optional] <p>
 * The optional logarithmic base to use 
 * (defaults to 'e' and so to the natural logarithm).
 * </p>
 * @return float The logarithm of arg to 
 * base, if given, or the
 * natural logarithm. 
 * </p>
 */
function log ($arg, $base = null) {}

/**
 * Base-10 logarithm
 * @link http://php.net/manual/en/function.log10.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The base-10 logaritm of arg
 * </p>
 */
function log10 ($arg) {}

/**
 * Square root
 * @link http://php.net/manual/en/function.sqrt.php
 * @param arg float <p>
 * The argument to process
 * </p>
 * @return float The square root of arg
 * or the special value NAN for negative numbers.
 * </p>
 */
function sqrt ($arg) {}

/**
 * Calculate the length of the hypotenuse of a right-angle triangle
 * @link http://php.net/manual/en/function.hypot.php
 * @param x float <p>
 * Length of first side
 * </p>
 * @param y float <p>
 * Length of second side
 * </p>
 * @return float Calculated length of the hypotenuse
 * </p>
 */
function hypot ($x, $y) {}

/**
 * Converts the number in degrees to the radian equivalent
 * @link http://php.net/manual/en/function.deg2rad.php
 * @param number float <p>
 * Angular value in degrees 
 * </p>
 * @return float The radian equivalent of number
 * </p>
 */
function deg2rad ($number) {}

/**
 * Converts the radian number to the equivalent number in degrees
 * @link http://php.net/manual/en/function.rad2deg.php
 * @param number float <p>
 * A radian value
 * </p>
 * @return float The equivalent of number in degrees
 * </p>
 */
function rad2deg ($number) {}

/**
 * Binary to decimal
 * @link http://php.net/manual/en/function.bindec.php
 * @param binary_string string <p>
 * The binary string to convert
 * </p>
 * @return number The decimal value of binary_string
 * </p>
 */
function bindec ($binary_string) {}

/**
 * Hexadecimal to decimal
 * @link http://php.net/manual/en/function.hexdec.php
 * @param hex_string string <p>
 * The hexadecimal string to convert
 * </p>
 * @return number The decimal representation of hex_string
 * </p>
 */
function hexdec ($hex_string) {}

/**
 * Octal to decimal
 * @link http://php.net/manual/en/function.octdec.php
 * @param octal_string string <p>
 * The octal string to convert
 * </p>
 * @return number The decimal representation of octal_string
 * </p>
 */
function octdec ($octal_string) {}

/**
 * Decimal to binary
 * @link http://php.net/manual/en/function.decbin.php
 * @param number int <p>
 * Decimal value to convert 
 * </p>
 * @return string Binary string representation of number
 * </p>
 */
function decbin ($number) {}

/**
 * Decimal to octal
 * @link http://php.net/manual/en/function.decoct.php
 * @param number int <p>
 * Decimal value to convert
 * </p>
 * @return string Octal string representation of number
 * </p>
 */
function decoct ($number) {}

/**
 * Decimal to hexadecimal
 * @link http://php.net/manual/en/function.dechex.php
 * @param number int <p>
 * Decimal value to convert
 * </p>
 * @return string Hexadecimal string representation of number
 * </p>
 */
function dechex ($number) {}

/**
 * Convert a number between arbitrary bases
 * @link http://php.net/manual/en/function.base-convert.php
 * @param number string <p>
 * The number to convert
 * </p>
 * @param frombase int <p>
 * The base number is in
 * </p>
 * @param tobase int <p>
 * The base to convert number to
 * </p>
 * @return string number converted to base tobase
 * </p>
 */
function base_convert ($number, $frombase, $tobase) {}

/**
 * Format a number with grouped thousands
 * @link http://php.net/manual/en/function.number-format.php
 * @param number float <p>
 * The number being formatted.
 * </p>
 * @param decimals int[optional] <p>
 * Sets the number of decimal points.
 * </p>
 * @return string A formatted version of number.
 * </p>
 */
function number_format ($number, $decimals = null) {}

/**
 * Returns the floating point remainder (modulo) of the division
  of the arguments
 * @link http://php.net/manual/en/function.fmod.php
 * @param x float <p>
 * The dividend
 * </p>
 * @param y float <p>
 * The divisor
 * </p>
 * @return float The floating point remainder of 
 * x/y
 * </p>
 */
function fmod ($x, $y) {}

/**
 * Converts a packed internet address to a human readable representation
 * @link http://php.net/manual/en/function.inet-ntop.php
 * @param in_addr string <p>
 * A 32bit IPv4, or 128bit IPv6 address.
 * </p>
 * @return string a string representation of the address or false on failure.
 * </p>
 */
function inet_ntop ($in_addr) {}

/**
 * Converts a human readable IP address to its packed in_addr representation
 * @link http://php.net/manual/en/function.inet-pton.php
 * @param address string <p>
 * A human readable IPv4 or IPv6 address.
 * </p>
 * @return string the in_addr representation of the given
 * address
 * </p>
 */
function inet_pton ($address) {}

/**
 * Converts a string containing an (IPv4) Internet Protocol dotted address into a proper address
 * @link http://php.net/manual/en/function.ip2long.php
 * @param ip_address string <p>
 * A standard format address.
 * </p>
 * @return int the IPv4 address or false if ip_address
 * is invalid.
 * </p>
 */
function ip2long ($ip_address) {}

/**
 * Converts an (IPv4) Internet network address into a string in Internet standard dotted format
 * @link http://php.net/manual/en/function.long2ip.php
 * @param proper_address int <p>
 * A proper address representation.
 * </p>
 * @return string the Internet IP address as a string.
 * </p>
 */
function long2ip ($proper_address) {}

/**
 * Gets the value of an environment variable
 * @link http://php.net/manual/en/function.getenv.php
 * @param varname string <p>
 * The variable name.
 * </p>
 * @return string the value of the environment variable
 * varname, or false on an error.
 * </p>
 */
function getenv ($varname) {}

/**
 * Sets the value of an environment variable
 * @link http://php.net/manual/en/function.putenv.php
 * @param setting string <p>
 * The setting, like "FOO=BAR"
 * </p>
 * @return bool &return.success;
 * </p>
 */
function putenv ($setting) {}

/**
 * Gets options from the command line argument list
 * @link http://php.net/manual/en/function.getopt.php
 * @param options string Each character in this string will be used as option characters and
 * matched against options passed to the script starting with a single
 * hyphen (-).
 * For example, an option string "x" recognizes an
 * option -x.
 * @param longopts array[optional] An array of options. Each element in this array will be used as option
 * strings and matched against options passed to the script starting with
 * two hyphens (--).
 * For example, an longopts element "opt" recognizes an
 * option --opt.
 * Prior to PHP5.3.0 this parameter was only available on few systems
 * @return array This function will return an array of option / argument pairs or false on
 * failure.
 * </p>
 */
function getopt ($options, array $longopts = null) {}

/**
 * Gets system load average
 * @link http://php.net/manual/en/function.sys-getloadavg.php
 * @return array an array with three samples (last 1, 5 and 15
 * minutes).
 * </p>
 */
function sys_getloadavg () {}

/**
 * Return current Unix timestamp with microseconds
 * @link http://php.net/manual/en/function.microtime.php
 * @param get_as_float bool[optional] <p>
 * When called without the optional argument, this function returns the string
 * "msec sec" where sec is the current time measured in the number of
 * seconds since the Unix Epoch (0:00:00 January 1, 1970 GMT), and
 * msec is the microseconds part.
 * Both portions of the string are returned in units of seconds.
 * </p>
 * <p>
 * If the optional get_as_float is set to
 * true then a float (in seconds) is returned.
 * </p>
 * @return mixed 
 */
function microtime ($get_as_float = null) {}

/**
 * Get current time
 * @link http://php.net/manual/en/function.gettimeofday.php
 * @param return_float bool[optional] <p>
 * When set to true, a float instead of an array is returned.
 * </p>
 * @return mixed By default an array is returned. If return_float
 * is set, then a float is returned.
 * </p>
 * <p>
 * Array keys:
 * "sec" - seconds since the Unix Epoch
 * "usec" - microseconds
 * "minuteswest" - minutes west of Greenwich
 * "dsttime" - type of dst correction
 * </p>
 */
function gettimeofday ($return_float = null) {}

/**
 * Gets the current resource usages
 * @link http://php.net/manual/en/function.getrusage.php
 * @param who int[optional] <p>
 * If who is 1, getrusage will be called with RUSAGE_CHILDREN.
 * </p>
 * @return array an associative array containing the data returned from the system
 * call. All entries are accessible by using their documented field names.
 * </p>
 */
function getrusage ($who = null) {}

/**
 * Generate a unique ID
 * @link http://php.net/manual/en/function.uniqid.php
 * @param prefix string[optional] <p>
 * Can be useful, for instance, if you generate identifiers
 * simultaneously on several hosts that might happen to generate the
 * identifier at the same microsecond.
 * </p>
 * <p>
 * With an empty prefix, the returned string will
 * be 13 characters long. If more_entropy is
 * true, it will be 23 characters.
 * </p>
 * @param more_entropy bool[optional] <p>
 * If set to true, uniqid will add additional
 * entropy (using the combined linear congruential generator) at the end
 * of the return value, which should make the results more unique.
 * </p>
 * @return string the unique identifier, as a string.
 * </p>
 */
function uniqid ($prefix = null, $more_entropy = null) {}

/**
 * Convert a quoted-printable string to an 8 bit string
 * @link http://php.net/manual/en/function.quoted-printable-decode.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the 8-bit binary string.
 * </p>
 */
function quoted_printable_decode ($str) {}

/**
 * Convert from one Cyrillic character set to another
 * @link http://php.net/manual/en/function.convert-cyr-string.php
 * @param str string <p>
 * The string to be converted.
 * </p>
 * @param from string <p>
 * The source Cyrillic character set, as a single character.
 * </p>
 * @param to string <p>
 * The target Cyrillic character set, as a single character.
 * </p>
 * @return string the converted string.
 * </p>
 */
function convert_cyr_string ($str, $from, $to) {}

/**
 * Gets the name of the owner of the current PHP script
 * @link http://php.net/manual/en/function.get-current-user.php
 * @return string the username as a string.
 * </p>
 */
function get_current_user () {}

/**
 * Limits the maximum execution time
 * @link http://php.net/manual/en/function.set-time-limit.php
 * @param seconds int <p>
 * The maximum execution time, in seconds. If set to zero, no time limit
 * is imposed.
 * </p>
 * @return void &return.void;
 * </p>
 */
function set_time_limit ($seconds) {}

/**
 * Gets the value of a PHP configuration option
 * @link http://php.net/manual/en/function.get-cfg-var.php
 * @param option string <p>
 * The configuration option name.
 * </p>
 * @return string the current value of the PHP configuration variable specified by
 * varname, or false if an error occurs.
 * </p>
 */
function get_cfg_var ($option) {}

function magic_quotes_runtime () {}

/**
 * Sets the current active configuration setting of magic_quotes_runtime
 * @link http://php.net/manual/en/function.set-magic-quotes-runtime.php
 * @param new_setting int <p>
 * 0 for off, 1 for on.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function set_magic_quotes_runtime ($new_setting) {}

/**
 * Gets the current configuration setting of magic quotes gpc
 * @link http://php.net/manual/en/function.get-magic-quotes-gpc.php
 * @return int 0 if magic quotes gpc are off, 1 otherwise.
 * </p>
 */
function get_magic_quotes_gpc () {}

/**
 * Gets the current active configuration setting of magic_quotes_runtime
 * @link http://php.net/manual/en/function.get-magic-quotes-runtime.php
 * @return int 0 if magic quotes runtime is off, 1 otherwise.
 * </p>
 */
function get_magic_quotes_runtime () {}

/**
 * Import GET/POST/Cookie variables into the global scope
 * @link http://php.net/manual/en/function.import-request-variables.php
 * @param types string <p>
 * Using the types parameter, you can specify
 * which request variables to import. You can use 'G', 'P' and 'C' 
 * characters respectively for GET, POST and Cookie. These characters are
 * not case sensitive, so you can also use any combination of 'g', 'p'
 * and 'c'. POST includes the POST uploaded file information.
 * </p>
 * <p>
 * Note that the order of the letters matters, as when using "gp", the
 * POST variables will overwrite GET variables with the same name. Any
 * other letters than GPC are discarded.
 * </p>
 * @param prefix string[optional] <p>
 * Variable name prefix, prepended before all variable's name imported
 * into the global scope. So if you have a GET value named "userid", and
 * provide a prefix "pref_", then you'll get a global variable named
 * $pref_userid.
 * </p>
 * <p>
 * Although the prefix parameter is optional, you
 * will get an E_NOTICE level
 * error if you specify no prefix, or specify an empty string as a
 * prefix. This is a possible security hazard. Notice level errors are
 * not displayed using the default error reporting level.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function import_request_variables ($types, $prefix = null) {}

/**
 * Send an error message somewhere
 * @link http://php.net/manual/en/function.error-log.php
 * @param message string <p>
 * The error message that should be logged.
 * </p>
 * @param message_type int[optional] <p>
 * Says where the error should go. The possible message types are as 
 * follows:
 * </p>
 * <p>
 * <table>
 * error_log log types
 * <tr valign="top">
 * <td>0</td>
 * <td>
 * message is sent to PHP's system logger, using
 * the Operating System's system logging mechanism or a file, depending
 * on what the error_log
 * configuration directive is set to. This is the default option.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>
 * message is sent by email to the address in
 * the destination parameter. This is the only
 * message type where the fourth parameter,
 * extra_headers is used.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>
 * No longer an option.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>
 * message is appended to the file
 * destination. A newline is not automatically 
 * added to the end of the message string.
 * </td>
 * </tr>
 * </table>
 * </p>
 * @param destination string[optional] <p>
 * The destination. Its meaning depends on the 
 * message_type parameter as described above.
 * </p>
 * @param extra_headers string[optional] <p>
 * The extra headers. It's used when the message_type
 * parameter is set to 1.
 * This message type uses the same internal function as 
 * mail does.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function error_log ($message, $message_type = null, $destination = null, $extra_headers = null) {}

/**
 * Get the last occurred error
 * @link http://php.net/manual/en/function.error-get-last.php
 * @return array an associative array describing the last error with keys "type",
 * "message", "file" and "line". Returns &null; if there hasn't been an error
 * yet.
 * </p>
 */
function error_get_last () {}

/**
 * Call a user function given by the first parameter
 * @link http://php.net/manual/en/function.call-user-func.php
 * @param function callback <p>
 * The function to be called. Class methods may also be invoked
 * statically using this function by passing
 * array($classname, $methodname) to this parameter.
 * </p>
 * @param parameter mixed[optional] <p>
 * Zero or more parameters to be passed to the function.
 * </p>
 * <p>
 * Note that the parameters for call_user_func are
 * not passed by reference.
 * ]]>
 * </p>
 * @param _ mixed[optional] 
 * @return mixed the function result, or false on error.
 * </p>
 */
function call_user_func ($function, $parameter = null, $_ = null) {}

/**
 * Call a user function given with an array of parameters
 * @link http://php.net/manual/en/function.call-user-func-array.php
 * @param function callback <p>
 * The function to be called.
 * </p>
 * @param param_arr array <p>
 * The parameters to be passed to the function, as an indexed array.
 * </p>
 * @return mixed the function result, or false on error.
 * </p>
 */
function call_user_func_array ($function, array $param_arr) {}

/**
 * Call a user method on an specific object [deprecated]
 * @link http://php.net/manual/en/function.call-user-method.php
 * @param method_name string 
 * @param obj object 
 * @param parameter mixed[optional] 
 * @param _ mixed[optional] 
 * @return mixed 
 */
function call_user_method ($method_name, &$obj, $parameter = null, $_ = null) {}

/**
 * Call a user method given with an array of parameters [deprecated]
 * @link http://php.net/manual/en/function.call-user-method-array.php
 * @param method_name string 
 * @param obj object 
 * @param params array 
 * @return mixed 
 */
function call_user_method_array ($method_name, &$obj, array $params) {}

/**
 * Generates a storable representation of a value
 * @link http://php.net/manual/en/function.serialize.php
 * @param value mixed <p>
 * The value to be serialized. serialize
 * handles all types, except the resource-type.
 * You can even serialize arrays that contain
 * references to itself. Circular references inside the array/object you 
 * are serializeing will also be stored. Any other 
 * reference will be lost.
 * </p>
 * <p>
 * When serializing objects, PHP will attempt to call the member function
 * __sleep prior to serialization. 
 * This is to allow the object to do any last minute clean-up, etc. prior 
 * to being serialized. Likewise, when the object is restored using 
 * unserialize the __wakeup member function is called.
 * </p>
 * @return string a string containing a byte-stream representation of 
 * value that can be stored anywhere.
 * </p>
 */
function serialize ($value) {}

/**
 * Creates a PHP value from a stored representation
 * @link http://php.net/manual/en/function.unserialize.php
 * @param str string <p>
 * The serialized string.
 * </p>
 * <p>
 * If the variable being unserialized is an object, after successfully 
 * reconstructing the object PHP will automatically attempt to call the
 * __wakeup member function (if it exists).
 * </p>
 * <p>
 * unserialize_callback_func directive
 * <p>
 * It's possible to set a callback-function which will be called,
 * if an undefined class should be instantiated during unserializing.
 * (to prevent getting an incomplete object "__PHP_Incomplete_Class".)
 * Use your &php.ini;, ini_set or &htaccess; 
 * to define 'unserialize_callback_func'. Everytime an undefined class
 * should be instantiated, it'll be called. To disable this feature just
 * empty this setting.
 * </p>
 * </p>
 * @return mixed The converted value is returned, and can be a boolean,
 * integer, float, string,
 * array or object.
 * </p>
 * <p>
 * In case the passed string is not unserializeable, false is returned and
 * E_NOTICE is issued.
 * </p>
 */
function unserialize ($str) {}

/**
 * Dumps information about a variable
 * @link http://php.net/manual/en/function.var-dump.php
 * @param expression mixed <p>
 * The variable you want to export.
 * </p>
 * @param expression mixed[optional] 
 * @return void &return.void;
 * </p>
 */
function var_dump ($expression, $expression = null) {}

/**
 * Outputs or returns a parsable string representation of a variable
 * @link http://php.net/manual/en/function.var-export.php
 * @param expression mixed <p>
 * The variable you want to export.
 * </p>
 * @param return bool[optional] <p>
 * If used and set to true, var_export will return
 * the variable representation instead of outputing it.
 * </p>
 * &note.uses-ob;
 * @return mixed the variable representation when the return 
 * parameter is used and evaluates to true. Otherwise, this function will
 * return &null;.
 * </p>
 */
function var_export ($expression, $return = null) {}

/**
 * Dumps a string representation of an internal zend value to output
 * @link http://php.net/manual/en/function.debug-zval-dump.php
 * @param variable mixed <p>
 * The variable being evaluated.
 * </p>
 * @return void &return.void;
 * </p>
 */
function debug_zval_dump ($variable) {}

/**
 * Prints human-readable information about a variable
 * @link http://php.net/manual/en/function.print-r.php
 * @param expression mixed <p>
 * The expression to be printed.
 * </p>
 * @param return bool[optional] <p>
 * If you would like to capture the output of print_r, 
 * use the return parameter. If this parameter is set
 * to true, print_r will return its output, instead of
 * printing it (which it does by default).
 * </p>
 * @return mixed If given a string, integer or float,
 * the value itself will be printed. If given an array, values
 * will be presented in a format that shows keys and elements. Similar
 * notation is used for objects.
 * </p>
 */
function print_r ($expression, $return = null) {}

/**
 * Returns the amount of memory allocated to PHP
 * @link http://php.net/manual/en/function.memory-get-usage.php
 * @param real_usage bool[optional] <p>
 * Set this to true to get the real size of memory allocated from
 * system. If not set or false only the memory used by
 * emalloc() is reported.
 * </p>
 * @return int the memory amount in bytes.
 * </p>
 */
function memory_get_usage ($real_usage = null) {}

/**
 * Returns the peak of memory allocated by PHP
 * @link http://php.net/manual/en/function.memory-get-peak-usage.php
 * @param real_usage bool[optional] <p>
 * Set this to true to get the real size of memory allocated from
 * system. If not set or false only the memory used by
 * emalloc() is reported.
 * </p>
 * @return int the memory peak in bytes.
 * </p>
 */
function memory_get_peak_usage ($real_usage = null) {}

/**
 * Register a function for execution on shutdown
 * @link http://php.net/manual/en/function.register-shutdown-function.php
 * @param function callback <p>
 * </p>
 * @param parameter mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return void &return.void;
 * </p>
 */
function register_shutdown_function ($function, $parameter = null, $_ = null) {}

/**
 * Register a function for execution on each tick
 * @link http://php.net/manual/en/function.register-tick-function.php
 * @param function callback <p>
 * The function name as a string, or an array consisting of an object and
 * a method.
 * </p>
 * @param arg mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return bool &return.success;
 * </p>
 */
function register_tick_function ($function, $arg = null, $_ = null) {}

/**
 * De-register a function for execution on each tick
 * @link http://php.net/manual/en/function.unregister-tick-function.php
 * @param function_name string <p>
 * The function name, as a string.
 * </p>
 * @return void &return.void;
 * </p>
 */
function unregister_tick_function ($function_name) {}

/**
 * Syntax highlighting of a file
 * @link http://php.net/manual/en/function.highlight-file.php
 * @param filename string <p>
 * Path to the PHP file to be highlighted.
 * </p>
 * @param return bool[optional] <p>
 * Set this parameter to true to make this function return the
 * highlighted code.
 * </p>
 * @return mixed If return is set to true, returns the highlighted
 * code as a string instead of printing it out. Otherwise, it will return
 * true on success, false on failure.
 * </p>
 */
function highlight_file ($filename, $return = null) {}

/**
 * &Alias; <function>highlight_file</function>
 * @link http://php.net/manual/en/function.show-source.php
 * @param file_name
 * @param return[optional]
 */
function show_source ($file_name, $return) {}

/**
 * Syntax highlighting of a string
 * @link http://php.net/manual/en/function.highlight-string.php
 * @param str string <p>
 * The PHP code to be highligthed. This should include the opening tag.
 * </p>
 * @param return bool[optional] <p>
 * Set this parameter to true to make this function return the
 * highlighted code.
 * </p>
 * @return mixed If return is set to true, returns the highlighted
 * code as a string instead of printing it out. Otherwise, it will return
 * true on success, false on failure.
 * </p>
 */
function highlight_string ($str, $return = null) {}

/**
 * Return source with stripped comments and whitespace
 * @link http://php.net/manual/en/function.php-strip-whitespace.php
 * @param filename string <p>
 * Path to the PHP file.
 * </p>
 * @return string The stripped source code will be returned on success, or an empty string
 * on failure.
 * </p>
 * <p>
 * This function works as described as of PHP 5.0.1. Before this it would
 * only return an empty string. For more information on this bug and its
 * prior behavior, see bug report
 * #29606.
 * </p>
 */
function php_strip_whitespace ($filename) {}

/**
 * Gets the value of a configuration option
 * @link http://php.net/manual/en/function.ini-get.php
 * @param varname string <p>
 * The configuration option name.
 * </p>
 * @return string the value of the configuration option as a string on success, or
 * an empty string on failure or for null values.
 * </p>
 */
function ini_get ($varname) {}

/**
 * Gets all configuration options
 * @link http://php.net/manual/en/function.ini-get-all.php
 * @param extension string[optional] <p>
 * An optional extension name. If set, the function return only options
 * specific for that extension.
 * </p>
 * @param details bool[optional] <p>
 * Retrieve details settings or only the current value for each setting.
 * Default is true (retrieve details).
 * </p>
 * @return array an associative array with directive name as the array key.
 * </p>
 * <p>
 * When details is true (default) the array will
 * contain global_value (set in
 * &php.ini;), local_value (perhaps set with
 * ini_set or &htaccess;), and
 * access (the access level).
 * </p>
 * <p>
 * When details is false the value will be the
 * current value of the option.
 * </p>
 * <p>
 * See the manual section
 * for information on what access levels mean.
 * </p>
 * <p>
 * It's possible for a directive to have multiple access levels, which is
 * why access shows the appropriate bitmask values.
 * </p>
 */
function ini_get_all ($extension = null, $details = null) {}

/**
 * Sets the value of a configuration option
 * @link http://php.net/manual/en/function.ini-set.php
 * @param varname string <p>
 * </p>
 * <p>
 * Not all the available options can be changed using
 * ini_set. There is a list of all available options
 * in the appendix.
 * </p>
 * @param newvalue string <p>
 * The new value for the option.
 * </p>
 * @return string the old value on success, false on failure.
 * </p>
 */
function ini_set ($varname, $newvalue) {}

/**
 * &Alias; <function>ini_set</function>
 * @link http://php.net/manual/en/function.ini-alter.php
 * @param varname
 * @param newvalue
 */
function ini_alter ($varname, $newvalue) {}

/**
 * Restores the value of a configuration option
 * @link http://php.net/manual/en/function.ini-restore.php
 * @param varname string <p>
 * The configuration option name.
 * </p>
 * @return void &return.void;
 * </p>
 */
function ini_restore ($varname) {}

/**
 * Gets the current include_path configuration option
 * @link http://php.net/manual/en/function.get-include-path.php
 * @return string the path, as a string.
 * </p>
 */
function get_include_path () {}

/**
 * Sets the include_path configuration option
 * @link http://php.net/manual/en/function.set-include-path.php
 * @param new_include_path string <p>
 * The new value for the include_path
 * </p>
 * @return string the old include_path on
 * success or false on failure.
 * </p>
 */
function set_include_path ($new_include_path) {}

/**
 * Restores the value of the include_path configuration option
 * @link http://php.net/manual/en/function.restore-include-path.php
 * @return void &return.void;
 * </p>
 */
function restore_include_path () {}

/**
 * Send a cookie
 * @link http://php.net/manual/en/function.setcookie.php
 * @param name string <p>
 * The name of the cookie.
 * </p>
 * @param value string[optional] <p>
 * The value of the cookie. This value is stored on the clients
 * computer; do not store sensitive information.
 * Assuming the name is 'cookiename', this
 * value is retrieved through $_COOKIE['cookiename']
 * </p>
 * @param expire int[optional] <p>
 * The time the cookie expires. This is a Unix timestamp so is
 * in number of seconds since the epoch. In other words, you'll
 * most likely set this with the time function
 * plus the number of seconds before you want it to expire. Or
 * you might use mktime.
 * time()+60*60*24*30 will set the cookie to
 * expire in 30 days. If set to 0, or omitted, the cookie will expire at
 * the end of the session (when the browser closes).
 * </p>
 * <p>
 * <p>
 * You may notice the expire parameter takes on a
 * Unix timestamp, as opposed to the date format Wdy, DD-Mon-YYYY
 * HH:MM:SS GMT, this is because PHP does this conversion
 * internally.
 * </p>
 * <p>
 * expire is compared to the client's time which can
 * differ from server's time.
 * </p>
 * </p>
 * @param path string[optional] <p>
 * The path on the server in which the cookie will be available on.
 * If set to '/', the cookie will be available
 * within the entire domain. If set to
 * '/foo/', the cookie will only be available
 * within the /foo/ directory and all
 * sub-directories such as /foo/bar/ of
 * domain. The default value is the
 * current directory that the cookie is being set in.
 * </p>
 * @param domain string[optional] <p>
 * The domain that the cookie is available.
 * To make the cookie available on all subdomains of example.com
 * then you'd set it to '.example.com'. The
 * . is not required but makes it compatible
 * with more browsers. Setting it to www.example.com
 * will make the cookie only available in the www
 * subdomain. Refer to tail matching in the
 * spec for details.
 * </p>
 * @param secure bool[optional] <p>
 * Indicates that the cookie should only be transmitted over a
 * secure HTTPS connection from the client. When set to true, the
 * cookie will only be set if a secure connection exists. The default
 * is false. On the server-side, it's on the programmer to send this
 * kind of cookie only on secure connection (e.g. with respect to
 * $_SERVER["HTTPS"]).
 * </p>
 * @param httponly bool[optional] <p>
 * When true the cookie will be made accessible only through the HTTP
 * protocol. This means that the cookie won't be accessible by
 * scripting languages, such as JavaScript. This setting can effectly
 * help to reduce identity theft through XSS attacks (although it is
 * not supported by all browsers). Added in PHP 5.2.0.
 * true or false
 * </p>
 * @return bool If output exists prior to calling this function,
 * setcookie will fail and return false. If
 * setcookie successfully runs, it will return true.
 * This does not indicate whether the user accepted the cookie.
 * </p>
 */
function setcookie ($name, $value = null, $expire = null, $path = null, $domain = null, $secure = null, $httponly = null) {}

/**
 * Send a cookie without urlencoding the cookie value
 * @link http://php.net/manual/en/function.setrawcookie.php
 * @param name string 
 * @param value string[optional] 
 * @param expire int[optional] 
 * @param path string[optional] 
 * @param domain string[optional] 
 * @param secure bool[optional] 
 * @param httponly bool[optional] 
 * @return bool &return.success;
 * </p>
 */
function setrawcookie ($name, $value = null, $expire = null, $path = null, $domain = null, $secure = null, $httponly = null) {}

/**
 * Send a raw HTTP header
 * @link http://php.net/manual/en/function.header.php
 * @param string string <p>
 * The header string.
 * </p>
 * <p>
 * There are two special-case header calls. The first is a header
 * that starts with the string "HTTP/" (case is not
 * significant), which will be used to figure out the HTTP status
 * code to send. For example, if you have configured Apache to
 * use a PHP script to handle requests for missing files (using
 * the ErrorDocument directive), you may want to
 * make sure that your script generates the proper status code.
 * </p>
 * <p>
 * ]]>
 * </p>
 * <p>
 * The second special case is the "Location:" header. Not only does
 * it send this header back to the browser, but it also returns a
 * REDIRECT (302) status code to the browser unless
 * some 3xx status code has already been set.
 * </p>
 * <p>
 * ]]>
 * </p>
 * @param replace bool[optional] <p>
 * The optional replace parameter indicates
 * whether the header should replace a previous similar header, or
 * add a second header of the same type. By default it will replace,
 * but if you pass in false as the second argument you can force
 * multiple headers of the same type. For example:
 * </p>
 * <p>
 * ]]>
 * </p>
 * @param http_response_code int[optional] <p>
 * Forces the HTTP response code to the specified value.
 * </p>
 * @return void &return.void;
 * </p>
 */
function header ($string, $replace = null, $http_response_code = null) {}

/**
 * Checks if or where headers have been sent
 * @link http://php.net/manual/en/function.headers-sent.php
 * @param file string[optional] <p>
 * If the optional file and
 * line parameters are set, 
 * headers_sent will put the PHP source file name
 * and line number where output started in the file
 * and line variables.
 * </p>
 * @param line int[optional] <p>
 * The line number where the output started.
 * </p>
 * @return bool headers_sent will return false if no HTTP headers
 * have already been sent or true otherwise.
 * </p>
 */
function headers_sent (&$file = null, &$line = null) {}

/**
 * Returns a list of response headers sent (or ready to send)
 * @link http://php.net/manual/en/function.headers-list.php
 * @return array a numerically indexed array of headers.
 * </p>
 */
function headers_list () {}

/**
 * Check whether client disconnected
 * @link http://php.net/manual/en/function.connection-aborted.php
 * @return int 1 if client disconnected, 0 otherwise.
 * </p>
 */
function connection_aborted () {}

/**
 * Returns connection status bitfield
 * @link http://php.net/manual/en/function.connection-status.php
 * @return int the connection status bitfield, which can be used against the
 * CONNECTION_XXX constants to determine the connection
 * status.
 * </p>
 */
function connection_status () {}

/**
 * Set whether a client disconnect should abort script execution
 * @link http://php.net/manual/en/function.ignore-user-abort.php
 * @param setting bool[optional] <p>
 * If not set, the function will only return the current setting.
 * </p>
 * @return int the previous setting, as a boolean.
 * </p>
 */
function ignore_user_abort ($setting = null) {}

/**
 * Parse a configuration file
 * @link http://php.net/manual/en/function.parse-ini-file.php
 * @param filename string <p>
 * The filename of the ini file being parsed.
 * </p>
 * @param process_sections bool[optional] <p>
 * By setting the last process_sections
 * parameter to true, you get a multidimensional array, with
 * the section names and settings included. The default
 * for process_sections is false 
 * </p>
 * @return array The settings are returned as an associative array.
 * </p>
 */
function parse_ini_file ($filename, $process_sections = null) {}

/**
 * Tells whether the file was uploaded via HTTP POST
 * @link http://php.net/manual/en/function.is-uploaded-file.php
 * @param filename string <p>
 * The filename being checked.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function is_uploaded_file ($filename) {}

/**
 * Moves an uploaded file to a new location
 * @link http://php.net/manual/en/function.move-uploaded-file.php
 * @param filename string <p>
 * The filename of the uploaded file.
 * </p>
 * @param destination string <p>
 * The destination of the moved file.
 * </p>
 * @return bool If filename is not a valid upload file,
 * then no action will occur, and
 * move_uploaded_file will return
 * false.
 * </p>
 * <p>
 * If filename is a valid upload file, but
 * cannot be moved for some reason, no action will occur, and
 * move_uploaded_file will return
 * false. Additionally, a warning will be issued.
 * </p>
 */
function move_uploaded_file ($filename, $destination) {}

/**
 * Get the Internet host name corresponding to a given IP address
 * @link http://php.net/manual/en/function.gethostbyaddr.php
 * @param ip_address string <p>
 * The host IP address.
 * </p>
 * @return string the host name or the unmodified ip_address
 * on failure.
 * </p>
 */
function gethostbyaddr ($ip_address) {}

/**
 * Get the IP address corresponding to a given Internet host name
 * @link http://php.net/manual/en/function.gethostbyname.php
 * @param hostname string <p>
 * The host name.
 * </p>
 * @return string the IP address or a string containing the unmodified
 * hostname on failure.
 * </p>
 */
function gethostbyname ($hostname) {}

/**
 * Get a list of IP addresses corresponding to a given Internet host
   name
 * @link http://php.net/manual/en/function.gethostbynamel.php
 * @param hostname string <p>
 * The host name.
 * </p>
 * @return array an array of IP addresses or false if
 * hostname could not be resolved.
 * </p>
 */
function gethostbynamel ($hostname) {}

/**
 * &Alias; <function>checkdnsrr</function>
 * @link http://php.net/manual/en/function.dns-check-record.php
 * @param host
 * @param type[optional]
 */
function dns_check_record ($host, $type) {}

/**
 * Check DNS records corresponding to a given Internet host name or IP address
 * @link http://php.net/manual/en/function.checkdnsrr.php
 * @param host string <p>
 * host may either be the IP address in
 * dotted-quad notation or the host name.
 * </p>
 * @param type string[optional] <p>
 * type may be any one of: A, MX, NS, SOA,
 * PTR, CNAME, AAAA, A6, SRV, NAPTR, TXT or ANY. The default is MX.
 * </p>
 * @return bool true if any records are found; returns false if no records
 * were found or if an error occurred.
 * </p>
 */
function checkdnsrr ($host, $type = null) {}

/**
 * &Alias; <function>getmxrr</function>
 * @link http://php.net/manual/en/function.dns-get-mx.php
 * @param hostname
 * @param mxhosts
 * @param weight[optional]
 */
function dns_get_mx ($hostname, &$mxhosts, &$weight) {}

/**
 * Get MX records corresponding to a given Internet host name
 * @link http://php.net/manual/en/function.getmxrr.php
 * @param hostname string <p>
 * The Internet host name.
 * </p>
 * @param mxhosts array <p>
 * A list of the MX records found is placed into the array
 * mxhosts. 
 * </p>
 * @param weight array[optional] <p>
 * If the weight array is given, it will be filled
 * with the weight information gathered.
 * </p>
 * @return bool true if any records are found; returns false if no records
 * were found or if an error occurred.
 * </p>
 */
function getmxrr ($hostname, array &$mxhosts, array &$weight = null) {}

/**
 * Fetch DNS Resource Records associated with a hostname
 * @link http://php.net/manual/en/function.dns-get-record.php
 * @param hostname string <p>
 * hostname should be a valid DNS hostname such
 * as "www.example.com". Reverse lookups can be generated using in-addr.arpa
 * notation, but gethostbyaddr is more suitable for
 * the majority of reverse lookups.
 * </p>
 * <p>
 * Per DNS standards, email addresses are given in user.host format (for
 * example: hostmaster.example.com as opposed to hostmaster@example.com),
 * be sure to check this value and modify if necessary before using it
 * with a functions such as mail.
 * </p>
 * @param type int[optional] <p>
 * By default, dns_get_record will search for any
 * resource records associated with hostname. 
 * To limit the query, specify the optional type
 * parameter. May be any one of the following:
 * DNS_A, DNS_CNAME,
 * DNS_HINFO, DNS_MX,
 * DNS_NS, DNS_PTR,
 * DNS_SOA, DNS_TXT,
 * DNS_AAAA, DNS_SRV,
 * DNS_NAPTR, DNS_A6,
 * DNS_ALL or DNS_ANY.
 * The default is DNS_ANY.
 * </p>
 * <p>
 * Because of eccentricities in the performance of libresolv
 * between platforms, DNS_ANY will not
 * always return every record, the slower DNS_ALL
 * will collect all records more reliably.
 * </p>
 * @return array This function returns an array of associative arrays. Each associative array contains
 * at minimum the following keys:
 * <table>
 * Basic DNS attributes
 * <tr valign="top">
 * <td>Attribute</td>
 * <td>Meaning</td>
 * </tr>
 * <tr valign="top">
 * <td>host</td>
 * <td>
 * The record in the DNS namespace to which the rest of the associated data refers.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>class</td>
 * <td>
 * dns_get_record only returns Internet class records and as
 * such this parameter will always return IN.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>type</td>
 * <td>
 * String containing the record type. Additional attributes will also be contained
 * in the resulting array dependant on the value of type. See table below.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>ttl</td>
 * <td>
 * Time To Live remaining for this record. This will not equal
 * the record's original ttl, but will rather equal the original ttl minus whatever
 * length of time has passed since the authoritative name server was queried.
 * </td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * <table>
 * Other keys in associative arrays dependant on 'type'
 * <tr valign="top">
 * <td>Type</td>
 * <td>Extra Columns</td>
 * </tr>
 * <tr valign="top">
 * <td>A</td>
 * <td>
 * ip: An IPv4 addresses in dotted decimal notation.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>MX</td>
 * <td>
 * pri: Priority of mail exchanger.
 * Lower numbers indicate greater priority.
 * target: FQDN of the mail exchanger.
 * See also dns_get_mx.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>CNAME</td>
 * <td>
 * target: FQDN of location in DNS namespace to which
 * the record is aliased.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>NS</td>
 * <td>
 * target: FQDN of the name server which is authoritative
 * for this hostname.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>PTR</td>
 * <td>
 * target: Location within the DNS namespace to which
 * this record points.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>TXT</td>
 * <td>
 * txt: Arbitrary string data associated with this record.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>HINFO</td>
 * <td>
 * cpu: IANA number designating the CPU of the machine
 * referenced by this record.
 * os: IANA number designating the Operating System on
 * the machine referenced by this record.
 * See IANA's Operating System
 * Names for the meaning of these values.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>SOA</td>
 * <td>
 * mname: FQDN of the machine from which the resource
 * records originated.
 * rname: Email address of the administrative contain
 * for this domain.
 * serial: Serial # of this revision of the requested
 * domain.
 * refresh: Refresh interval (seconds) secondary name
 * servers should use when updating remote copies of this domain.
 * retry: Length of time (seconds) to wait after a
 * failed refresh before making a second attempt.
 * expire: Maximum length of time (seconds) a secondary
 * DNS server should retain remote copies of the zone data without a
 * successful refresh before discarding.
 * minimum-ttl: Minimum length of time (seconds) a
 * client can continue to use a DNS resolution before it should request
 * a new resolution from the server. Can be overridden by individual
 * resource records.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>AAAA</td>
 * <td>
 * ipv6: IPv6 address
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>A6(PHP &gt;= 5.1.0)</td>
 * <td>
 * masklen: Length (in bits) to inherit from the target
 * specified by chain.
 * ipv6: Address for this specific record to merge with
 * chain.
 * chain: Parent record to merge with
 * ipv6 data.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>SRV</td>
 * <td>
 * pri: (Priority) lowest priorities should be used first.
 * weight: Ranking to weight which of commonly prioritized
 * targets should be chosen at random.
 * target and port: hostname and port
 * where the requested service can be found.
 * For additional information see: RFC 2782
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>NAPTR</td>
 * <td>
 * order and pref: Equivalent to
 * pri and weight above.
 * flags, services, regex,
 * and replacement: Parameters as defined by
 * RFC 2915.
 * </td>
 * </tr>
 * </table>
 * </p>
 */
function dns_get_record ($hostname, $type = null) {}

/**
 * Get the integer value of a variable
 * @link http://php.net/manual/en/function.intval.php
 * @param var mixed <p>
 * The scalar value being converted to an integer
 * </p>
 * @param base int[optional] <p>
 * The base for the conversion (default is base 10)
 * </p>
 * @return int The integer value of var on success, or 0 on
 * failure. Empty arrays and objects return 0, non-empty arrays and
 * objects return 1.
 * </p>
 * <p>
 * The maximum value depends on the system. 32 bit systems have a 
 * maximum signed integer range of -2147483648 to 2147483647. So for example 
 * on such a system, intval('1000000000000') will return 
 * 2147483647. The maximum signed integer value for 64 bit systems is 
 * 9223372036854775807.
 * </p>
 * <p>
 * Strings will most likely return 0 although this depends on the 
 * leftmost characters of the string. The common rules of 
 * integer casting 
 * apply.
 * </p>
 */
function intval ($var, $base = null) {}

/**
 * Get float value of a variable
 * @link http://php.net/manual/en/function.floatval.php
 * @param var mixed <p>
 * May be any scalar type. You cannot use floatval 
 * on arrays or objects.
 * </p>
 * @return float The float value of the given variable.
 * </p>
 */
function floatval ($var) {}

/**
 * &Alias; <function>floatval</function>
 * @link http://php.net/manual/en/function.doubleval.php
 * @param var
 */
function doubleval ($var) {}

/**
 * Get string value of a variable
 * @link http://php.net/manual/en/function.strval.php
 * @param var mixed <p>
 * The variable that is being converted to a string.
 * </p>
 * <p>
 * var may be any scalar type. You cannot use
 * strval on arrays or objects.
 * </p>
 * @return string The string value of var.
 * </p>
 */
function strval ($var) {}

/**
 * Get the type of a variable
 * @link http://php.net/manual/en/function.gettype.php
 * @param var mixed <p>
 * The variable being type checked.
 * </p>
 * @return string Possibles values for the returned string are:
 * "boolean"
 * "integer"
 * "double" (for historical reasons "double" is
 * returned in case of a float, and not simply
 * "float")
 * "string"
 * "array"
 * "object"
 * "resource"
 * "NULL"
 * "unknown type"
 * </p>
 */
function gettype ($var) {}

/**
 * Set the type of a variable
 * @link http://php.net/manual/en/function.settype.php
 * @param var mixed <p>
 * The variable being converted.
 * </p>
 * @param type string <p>
 * Possibles values of type are:
 * "boolean" (or, since PHP 4.2.0, "bool")
 * @return bool &return.success;
 * </p>
 */
function settype (&$var, $type) {}

/**
 * Finds whether a variable is &null;
 * @link http://php.net/manual/en/function.is-null.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is null, false
 * otherwise.
 * </p>
 */
function is_null ($var) {}

/**
 * Finds whether a variable is a resource
 * @link http://php.net/manual/en/function.is-resource.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is a resource,
 * false otherwise.
 * </p>
 */
function is_resource ($var) {}

/**
 * Finds out whether a variable is a boolean
 * @link http://php.net/manual/en/function.is-bool.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is a boolean,
 * false otherwise.
 * </p>
 */
function is_bool ($var) {}

/**
 * &Alias; <function>is_int</function>
 * @link http://php.net/manual/en/function.is-long.php
 * @param var
 */
function is_long ($var) {}

/**
 * Finds whether the type of a variable is float
 * @link http://php.net/manual/en/function.is-float.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is a float, 
 * false otherwise.
 * </p>
 */
function is_float ($var) {}

/**
 * Find whether the type of a variable is integer
 * @link http://php.net/manual/en/function.is-int.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is an integer, 
 * false otherwise.
 * </p>
 */
function is_int ($var) {}

/**
 * &Alias; <function>is_int</function>
 * @link http://php.net/manual/en/function.is-integer.php
 * @param var
 */
function is_integer ($var) {}

/**
 * &Alias; <function>is_float</function>
 * @link http://php.net/manual/en/function.is-double.php
 * @param var
 */
function is_double ($var) {}

/**
 * &Alias; <function>is_float</function>
 * @link http://php.net/manual/en/function.is-real.php
 * @param var
 */
function is_real ($var) {}

/**
 * Finds whether a variable is a number or a numeric string
 * @link http://php.net/manual/en/function.is-numeric.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is a number or a numeric
 * string, false otherwise.
 * </p>
 */
function is_numeric ($var) {}

/**
 * Find whether the type of a variable is string
 * @link http://php.net/manual/en/function.is-string.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is of type string,
 * false otherwise.
 * </p>
 */
function is_string ($var) {}

/**
 * Finds whether a variable is an array
 * @link http://php.net/manual/en/function.is-array.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is an array, 
 * false otherwise.
 * </p>
 */
function is_array ($var) {}

/**
 * Finds whether a variable is an object
 * @link http://php.net/manual/en/function.is-object.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is an object, 
 * false otherwise.
 * </p>
 */
function is_object ($var) {}

/**
 * Finds whether a variable is a scalar
 * @link http://php.net/manual/en/function.is-scalar.php
 * @param var mixed <p>
 * The variable being evaluated.
 * </p>
 * @return bool true if var is a scalar false
 * otherwise.
 * </p>
 */
function is_scalar ($var) {}

/**
 * Verify that the contents of a variable can be called as a function
 * @link http://php.net/manual/en/function.is-callable.php
 * @param var mixed <p>
 * Can be either the name of a function stored in a string variable, or
 * an object and the name of a method within the object, like this: 
 * array($SomeObject, 'MethodName')
 * </p>
 * @param syntax_only bool[optional] <p>
 * If set to true the function only verifies that
 * var might be a function or method. It will only
 * reject simple variables that are not strings, or an array that does
 * not have a valid structure to be used as a callback. The valid ones
 * are supposed to have only 2 entries, the first of which is an object
 * or a string, and the second a string.
 * </p>
 * @param callable_name string[optional] <p>
 * Receives the "callable name". In the example below it is
 * "someClass::someMethod". Note, however, that despite the implication
 * that someClass::SomeMethod() is a callable static method, this is not
 * the case.
 * </p>
 * @return bool true if var is callable, false 
 * otherwise.
 * </p>
 */
function is_callable ($var, $syntax_only = null, &$callable_name = null) {}

/**
 * Regular expression match
 * @link http://php.net/manual/en/function.ereg.php
 * @param pattern string <p>
 * Case sensitive regular expression.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @param regs array[optional] <p>
 * If matches are found for parenthesized substrings of
 * pattern and the function is called with the
 * third argument regs, the matches will be stored
 * in the elements of the array regs. 
 * </p>
 * <p>
 * $regs[1] will contain the substring which starts at the first left
 * parenthesis; $regs[2] will contain the substring starting at the
 * second, and so on. $regs[0] will contain a copy of the complete string
 * matched.
 * </p>
 * @return int the length of the matched string if a match for
 * pattern was found in string,
 * or false if no matches were found or an error occurred.
 * </p>
 * <p>
 * If the optional parameter regs was not passed or
 * the length of the matched string is 0, this function returns 1.
 * </p>
 */
function ereg ($pattern, $string, array &$regs = null) {}

/**
 * Replace regular expression
 * @link http://php.net/manual/en/function.ereg-replace.php
 * @param pattern string <p>
 * A POSIX extended regular expression.
 * </p>
 * @param replacement string <p>
 * If pattern contains parenthesized substrings,
 * replacement may contain substrings of the form
 * \\digit, which will be
 * replaced by the text matching the digit'th parenthesized substring; 
 * \\0 will produce the entire contents of string.
 * Up to nine substrings may be used. Parentheses may be nested, in which
 * case they are counted by the opening parenthesis.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @return string The modified string is returned. If no matches are found in 
 * string, then it will be returned unchanged.
 * </p>
 */
function ereg_replace ($pattern, $replacement, $string) {}

/**
 * Case insensitive regular expression match
 * @link http://php.net/manual/en/function.eregi.php
 * @param pattern string <p>
 * Case insensitive regular expression.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @param regs array[optional] <p>
 * If matches are found for parenthesized substrings of
 * pattern and the function is called with the
 * third argument regs, the matches will be stored
 * in the elements of the array regs. 
 * </p>
 * <p>
 * $regs[1] will contain the substring which starts at the first left
 * parenthesis; $regs[2] will contain the substring starting at the
 * second, and so on. $regs[0] will contain a copy of the complete string
 * matched.
 * </p>
 * @return int the length of the matched string if a match for
 * pattern was found in string,
 * or false if no matches were found or an error occurred.
 * </p>
 * <p>
 * If the optional parameter regs was not passed or
 * the length of the matched string is 0, this function returns 1.
 * </p>
 */
function eregi ($pattern, $string, array &$regs = null) {}

/**
 * Replace regular expression case insensitive
 * @link http://php.net/manual/en/function.eregi-replace.php
 * @param pattern string <p>
 * A POSIX extended regular expression.
 * </p>
 * @param replacement string <p>
 * If pattern contains parenthesized substrings,
 * replacement may contain substrings of the form
 * \\digit, which will be
 * replaced by the text matching the digit'th parenthesized substring; 
 * \\0 will produce the entire contents of string.
 * Up to nine substrings may be used. Parentheses may be nested, in which
 * case they are counted by the opening parenthesis.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @return string The modified string is returned. If no matches are found in 
 * string, then it will be returned unchanged.
 * </p>
 */
function eregi_replace ($pattern, $replacement, $string) {}

/**
 * Split string into array by regular expression
 * @link http://php.net/manual/en/function.split.php
 * @param pattern string <p>
 * Case sensitive regular expression.
 * </p>
 * <p>
 * If you want to split on any of the characters which are considered
 * special by regular expressions, you'll need to escape them first. If
 * you think split (or any other regex function, for
 * that matter) is doing something weird, please read the file
 * regex.7, included in the 
 * regex/ subdirectory of the PHP distribution. It's
 * in manpage format, so you'll want to do something along the lines of
 * man /usr/local/src/regex/regex.7 in order to read it.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @param limit int[optional] <p>
 * If limit is set, the returned array will
 * contain a maximum of limit elements with the
 * last element containing the whole rest of
 * string.
 * </p>
 * @return array an array of strings, each of which is a substring of
 * string formed by splitting it on boundaries formed
 * by the case-sensitive regular expression pattern.
 * </p>
 * <p>
 * If there are n occurrences of
 * pattern, the returned array will contain
 * n+1 items. For example, if
 * there is no occurrence of pattern, an array with
 * only one element will be returned. Of course, this is also true if
 * string is empty. If an error occurs,
 * split returns false.
 * </p>
 */
function split ($pattern, $string, $limit = null) {}

/**
 * Split string into array by regular expression case insensitive
 * @link http://php.net/manual/en/function.spliti.php
 * @param pattern string <p>
 * Case insensitive regular expression.
 * </p>
 * <p>
 * If you want to split on any of the characters which are considered
 * special by regular expressions, you'll need to escape them first. If
 * you think spliti (or any other regex function, for
 * that matter) is doing something weird, please read the file
 * regex.7, included in the 
 * regex/ subdirectory of the PHP distribution. It's
 * in manpage format, so you'll want to do something along the lines of
 * man /usr/local/src/regex/regex.7 in order to read it.
 * </p>
 * @param string string <p>
 * The input string.
 * </p>
 * @param limit int[optional] <p>
 * If limit is set, the returned array will
 * contain a maximum of limit elements with the
 * last element containing the whole rest of
 * string.
 * </p>
 * @return array an array of strings, each of which is a substring of
 * string formed by splitting it on boundaries formed
 * by the case insensitive regular expression pattern.
 * </p>
 * <p>
 * If there are n occurrences of
 * pattern, the returned array will contain
 * n+1 items. For example, if
 * there is no occurrence of pattern, an array with
 * only one element will be returned. Of course, this is also true if
 * string is empty. If an error occurs,
 * spliti returns false.
 * </p>
 */
function spliti ($pattern, $string, $limit = null) {}

/**
 * &Alias; <function>implode</function>
 * @link http://php.net/manual/en/function.join.php
 * @param glue
 * @param pieces
 */
function join ($glue, $pieces) {}

/**
 * Make regular expression for case insensitive match
 * @link http://php.net/manual/en/function.sql-regcase.php
 * @param string string <p>
 * The input string.
 * </p>
 * @return string a valid regular expression which will match
 * string, ignoring case. This expression is
 * string with each alphabetic character converted to
 * a bracket expression; this bracket expression contains that character's
 * uppercase and lowercase form. Other characters remain unchanged.
 * </p>
 */
function sql_regcase ($string) {}

/**
 * Loads a PHP extension at runtime
 * @link http://php.net/manual/en/function.dl.php
 * @param library string <p>
 * This parameter is only the filename of the
 * extension to load which also depends on your platform. For example,
 * the sockets extension (if compiled
 * as a shared module, not the default!) would be called 
 * sockets.so on Unix platforms whereas it is called
 * php_sockets.dll on the Windows platform.
 * </p>
 * <p>
 * The directory where the extension is loaded from depends on your
 * platform:
 * </p>
 * <p>
 * Windows - If not explicitly set in the &php.ini;, the extension is
 * loaded from c:\php4\extensions\ by default.
 * </p>
 * <p>
 * Unix - If not explicitly set in the &php.ini;, the default extension
 * directory depends on
 * whether PHP has been built with --enable-debug
 * or not
 * @return int &return.success; If the functionality of loading modules is not available
 * (see Note) or has been disabled (either by turning it off
 * enable_dl or by enabling &safemode;
 * in &php.ini;) an E_ERROR is emitted
 * and execution is stopped. If dl fails because the
 * specified library couldn't be loaded, in addition to false an
 * E_WARNING message is emitted.
 * </p>
 */
function dl ($library) {}

/**
 * Closes process file pointer
 * @link http://php.net/manual/en/function.pclose.php
 * @param handle resource <p>
 * The file pointer must be valid, and must have been returned by a
 * successful call to popen.
 * </p>
 * @return int the termination status of the process that was run.
 * </p>
 */
function pclose ($handle) {}

/**
 * Opens process file pointer
 * @link http://php.net/manual/en/function.popen.php
 * @param command string <p>
 * The command
 * </p>
 * @param mode string <p>
 * The mode
 * </p>
 * @return resource a file pointer identical to that returned by
 * fopen, except that it is unidirectional (may
 * only be used for reading or writing) and must be closed with
 * pclose. This pointer may be used with
 * fgets, fgetss, and
 * fwrite.
 * </p>
 * <p>
 * If an error occurs, returns false.
 * </p>
 */
function popen ($command, $mode) {}

/**
 * Outputs a file
 * @link http://php.net/manual/en/function.readfile.php
 * @param filename string <p>
 * The filename being read.
 * </p>
 * @param use_include_path bool[optional] <p>
 * You can use the optional second parameter and set it to true, if
 * you want to search for the file in the include_path, too.
 * </p>
 * @param context resource[optional] <p>
 * A context stream resource.
 * </p>
 * @return int the number of bytes read from the file. If an error
 * occurs, false is returned and unless the function was called as
 * @readfile, an error message is printed.
 * </p>
 */
function readfile ($filename, $use_include_path = null, $context = null) {}

/**
 * Rewind the position of a file pointer
 * @link http://php.net/manual/en/function.rewind.php
 * @param handle resource <p>
 * The file pointer must be valid, and must point to a file
 * successfully opened by fopen.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function rewind ($handle) {}

/**
 * Removes directory
 * @link http://php.net/manual/en/function.rmdir.php
 * @param dirname string <p>
 * Path to the directory.
 * </p>
 * @param context resource[optional] &note.context-support;
 * @return bool &return.success;
 * </p>
 */
function rmdir ($dirname, $context = null) {}

/**
 * Changes the current umask
 * @link http://php.net/manual/en/function.umask.php
 * @param mask int[optional] <p>
 * The new umask.
 * </p>
 * @return int umask without arguments simply returns the
 * current umask otherwise the old umask is returned.
 * </p>
 */
function umask ($mask = null) {}

/**
 * Closes an open file pointer
 * @link http://php.net/manual/en/function.fclose.php
 * @param handle resource <p>
 * The file pointer must be valid, and must point to a file successfully
 * opened by fopen or fsockopen.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function fclose ($handle) {}

/**
 * Tests for end-of-file on a file pointer
 * @link http://php.net/manual/en/function.feof.php
 * @param handle resource &fs.validfp.all;
 * @return bool true if the file pointer is at EOF or an error occurs
 * (including socket timeout); otherwise returns false.
 * </p>
 */
function feof ($handle) {}

/**
 * Gets character from file pointer
 * @link http://php.net/manual/en/function.fgetc.php
 * @param handle resource &fs.validfp.all;
 * @return string a string containing a single character read from the file pointed
 * to by handle. Returns false on EOF.
 * </p>
 * &return.falseproblem;
 */
function fgetc ($handle) {}

/**
 * Gets line from file pointer
 * @link http://php.net/manual/en/function.fgets.php
 * @param handle resource &fs.validfp.all;
 * @param length int[optional] <p>
 * Reading ends when length - 1 bytes have been
 * read, on a newline (which is included in the return value), or on EOF
 * (whichever comes first). If no length is specified, it will keep
 * reading from the stream until it reaches the end of the line.
 * </p>
 * <p>
 * Until PHP 4.3.0, omitting it would assume 1024 as the line length.
 * If the majority of the lines in the file are all larger than 8KB,
 * it is more resource efficient for your script to specify the maximum
 * line length.
 * </p>
 * @return string a string of up to length - 1 bytes read from
 * the file pointed to by handle.
 * </p>
 * <p>
 * If an error occurs, returns false.
 * </p>
 */
function fgets ($handle, $length = null) {}

/**
 * Gets line from file pointer and strip HTML tags
 * @link http://php.net/manual/en/function.fgetss.php
 * @param handle resource &fs.validfp.all;
 * @param length int[optional] <p>
 * Length of the data to be retrieved.
 * </p>
 * @param allowable_tags string[optional] <p>
 * You can use the optional third parameter to specify tags which should
 * not be stripped.
 * </p>
 * @return string a string of up to length - 1 bytes read from
 * the file pointed to by handle, with all HTML and PHP
 * code striped.
 * </p>
 * <p>
 * If an error occurs, returns false.
 * </p>
 */
function fgetss ($handle, $length = null, $allowable_tags = null) {}

/**
 * Binary-safe file read
 * @link http://php.net/manual/en/function.fread.php
 * @param handle resource &fs.file.pointer;
 * @param length int <p>
 * Up to length number of bytes read.
 * </p>
 * @return string the read string or false in case of error.
 * </p>
 */
function fread ($handle, $length) {}

/**
 * Opens file or URL
 * @link http://php.net/manual/en/function.fopen.php
 * @param filename string <p>
 * If filename is of the form "scheme://...", it
 * is assumed to be a URL and PHP will search for a protocol handler
 * (also known as a wrapper) for that scheme. If no wrappers for that
 * protocol are registered, PHP will emit a notice to help you track
 * potential problems in your script and then continue as though
 * filename specifies a regular file.
 * </p>
 * <p>
 * If PHP has decided that filename specifies
 * a local file, then it will try to open a stream on that file.
 * The file must be accessible to PHP, so you need to ensure that
 * the file access permissions allow this access.
 * If you have enabled &safemode;,
 * or open_basedir further
 * restrictions may apply.
 * </p>
 * <p>
 * If PHP has decided that filename specifies
 * a registered protocol, and that protocol is registered as a
 * network URL, PHP will check to make sure that
 * allow_url_fopen is
 * enabled. If it is switched off, PHP will emit a warning and
 * the fopen call will fail.
 * </p>
 * <p>
 * The list of supported protocols can be found in . Some protocols (also referred to as
 * wrappers) support context
 * and/or &php.ini; options. Refer to the specific page for the
 * protocol in use for a list of options which can be set. (e.g.
 * &php.ini; value user_agent used by the
 * http wrapper).
 * </p>
 * <p>
 * On the Windows platform, be careful to escape any backslashes
 * used in the path to the file, or use forward slashes.
 * ]]>
 * </p>
 * @param mode string <p>
 * The mode parameter specifies the type of access
 * you require to the stream. It may be any of the following:
 * <table>
 * A list of possible modes for fopen
 * using mode
 * <tr valign="top">
 * <td>mode</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>'r'</td>
 * <td>
 * Open for reading only; place the file pointer at the
 * beginning of the file.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'r+'</td>
 * <td>
 * Open for reading and writing; place the file pointer at
 * the beginning of the file.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'w'</td>
 * <td>
 * Open for writing only; place the file pointer at the
 * beginning of the file and truncate the file to zero length.
 * If the file does not exist, attempt to create it.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'w+'</td>
 * <td>
 * Open for reading and writing; place the file pointer at
 * the beginning of the file and truncate the file to zero
 * length. If the file does not exist, attempt to create it.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'a'</td>
 * <td>
 * Open for writing only; place the file pointer at the end of
 * the file. If the file does not exist, attempt to create it.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'a+'</td>
 * <td>
 * Open for reading and writing; place the file pointer at
 * the end of the file. If the file does not exist, attempt to
 * create it.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'x'</td>
 * <td>
 * Create and open for writing only; place the file pointer at the
 * beginning of the file. If the file already exists, the
 * fopen call will fail by returning false and
 * generating an error of level E_WARNING. If
 * the file does not exist, attempt to create it. This is equivalent
 * to specifying O_EXCL|O_CREAT flags for the
 * underlying open(2) system call. 
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>'x+'</td>
 * <td>
 * Create and open for reading and writing; place the file pointer at
 * the beginning of the file. If the file already exists, the
 * fopen call will fail by returning false and
 * generating an error of level E_WARNING. If
 * the file does not exist, attempt to create it. This is equivalent
 * to specifying O_EXCL|O_CREAT flags for the
 * underlying open(2) system call.
 * </td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * Different operating system families have different line-ending
 * conventions. When you write a text file and want to insert a line
 * break, you need to use the correct line-ending character(s) for your
 * operating system. Unix based systems use \n as the
 * line ending character, Windows based systems use \r\n
 * as the line ending characters and Macintosh based systems use
 * \r as the line ending character.
 * </p>
 * <p>
 * If you use the wrong line ending characters when writing your files, you
 * might find that other applications that open those files will "look
 * funny".
 * </p>
 * <p>
 * Windows offers a text-mode translation flag ('t')
 * which will transparently translate \n to
 * \r\n when working with the file. In contrast, you
 * can also use 'b' to force binary mode, which will not
 * translate your data. To use these flags, specify either
 * 'b' or 't' as the last character
 * of the mode parameter.
 * </p>
 * <p>
 * The default translation mode depends on the SAPI and version of PHP that
 * you are using, so you are encouraged to always specify the appropriate
 * flag for portability reasons. You should use the 't'
 * mode if you are working with plain-text files and you use
 * \n to delimit your line endings in your script, but
 * expect your files to be readable with applications such as notepad. You
 * should use the 'b' in all other cases.
 * </p>
 * <p>
 * If you do not specify the 'b' flag when working with binary files, you
 * may experience strange problems with your data, including broken image
 * files and strange problems with \r\n characters.
 * </p>
 * <p>
 * For portability, it is strongly recommended that you always
 * use the 'b' flag when opening files with fopen.
 * </p>
 * <p>
 * Again, for portability, it is also strongly recommended that
 * you re-write code that uses or relies upon the 't'
 * mode so that it uses the correct line endings and
 * 'b' mode instead.
 * </p>
 * @param use_include_path bool[optional] <p>
 * The optional third use_include_path parameter
 * can be set to '1' or true if you want to search for the file in the
 * include_path, too.
 * </p>
 * @param context resource[optional] &note.context-support;
 * @return resource a file pointer resource on success, or false on error.
 * </p>
 */
function fopen ($filename, $mode, $use_include_path = null, $context = null) {}

/**
 * Output all remaining data on a file pointer
 * @link http://php.net/manual/en/function.fpassthru.php
 * @param handle resource &fs.validfp.all;
 * @return int If an error occurs, fpassthru returns
 * false. Otherwise, fpassthru returns
 * the number of characters read from handle
 * and passed through to the output.
 * </p>
 */
function fpassthru ($handle) {}

/**
 * Truncates a file to a given length
 * @link http://php.net/manual/en/function.ftruncate.php
 * @param handle resource <p>
 * The file pointer.
 * </p>
 * <p>
 * The handle must be open for writing.
 * </p>
 * @param size int <p>
 * The size to truncate to.
 * </p>
 * <p>
 * If size is larger than the file it is extended
 * with null bytes.
 * </p>
 * <p>
 * If size is smaller than the extra data
 * will be lost.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ftruncate ($handle, $size) {}

/**
 * Gets information about a file using an open file pointer
 * @link http://php.net/manual/en/function.fstat.php
 * @param handle resource &fs.file.pointer;
 * @return array an array with the statistics of the file; the format of the array
 * is described in detail on the stat manual page.
 * </p>
 */
function fstat ($handle) {}

/**
 * Seeks on a file pointer
 * @link http://php.net/manual/en/function.fseek.php
 * @param handle resource &fs.file.pointer;
 * @param offset int <p>
 * The offset.
 * </p>
 * <p>
 * To move to a position before the end-of-file, you need to pass a negative
 * value in offset.
 * </p>
 * @param whence int[optional] <p>
 * whence values are:
 * SEEK_SET - Set position equal to offset bytes.
 * SEEK_CUR - Set position to current location plus offset.
 * SEEK_END - Set position to end-of-file plus offset.
 * </p>
 * <p>
 * If whence is not specified, it is assumed to be
 * SEEK_SET.
 * </p>
 * @return int Upon success, returns 0; otherwise, returns -1. Note that seeking
 * past EOF is not considered an error.
 * </p>
 */
function fseek ($handle, $offset, $whence = null) {}

/**
 * Tells file pointer read/write position
 * @link http://php.net/manual/en/function.ftell.php
 * @param handle resource <p>
 * The file pointer must be valid, and must point to a file successfully
 * opened by fopen or popen.
 * ftell gives undefined results for append-only streams
 * (opened with "a" flag).
 * </p>
 * @return int the position of the file pointer referenced by
 * handle; i.e., its offset into the file stream.
 * </p>
 * <p>
 * If an error occurs, returns false.
 * </p>
 */
function ftell ($handle) {}

/**
 * Flushes the output to a file
 * @link http://php.net/manual/en/function.fflush.php
 * @param handle resource &fs.validfp.all;
 * @return bool &return.success;
 * </p>
 */
function fflush ($handle) {}

/**
 * Binary-safe file write
 * @link http://php.net/manual/en/function.fwrite.php
 * @param handle resource &fs.file.pointer;
 * @param string string <p>
 * The string that is to be written.
 * </p>
 * @param length int[optional] <p>
 * If the length argument is given, writing will
 * stop after length bytes have been written or
 * the end of string is reached, whichever comes
 * first.
 * </p>
 * <p>
 * Note that if the length argument is given,
 * then the magic_quotes_runtime
 * configuration option will be ignored and no slashes will be
 * stripped from string.
 * </p>
 * @return int 
 */
function fwrite ($handle, $string, $length = null) {}

/**
 * &Alias; <function>fwrite</function>
 * @link http://php.net/manual/en/function.fputs.php
 * @param fp
 * @param str
 * @param length[optional]
 */
function fputs ($fp, $str, $length) {}

/**
 * Makes directory
 * @link http://php.net/manual/en/function.mkdir.php
 * @param pathname string <p>
 * The directory path.
 * </p>
 * @param mode int[optional] <p>
 * The mode is 0777 by default, which means the widest possible
 * access. For more information on modes, read the details
 * on the chmod page.
 * </p>
 * <p>
 * mode is ignored on Windows.
 * </p>
 * <p>
 * Note that you probably want to specify the mode as an octal number,
 * which means it should have a leading zero. The mode is also modified
 * by the current umask, which you can change using
 * umask.
 * </p>
 * @param recursive bool[optional] <p>
 * Default to false.
 * </p>
 * @param context resource[optional] &note.context-support;
 * @return bool &return.success;
 * </p>
 */
function mkdir ($pathname, $mode = null, $recursive = null, $context = null) {}

/**
 * Renames a file or directory
 * @link http://php.net/manual/en/function.rename.php
 * @param oldname string <p>
 * </p>
 * <p>
 * The old name. The wrapper used in oldname
 * must match the wrapper used in
 * newname.
 * </p>
 * @param newname string <p>
 * The new name.
 * </p>
 * @param context resource[optional] &note.context-support;
 * @return bool &return.success;
 * </p>
 */
function rename ($oldname, $newname, $context = null) {}

/**
 * Copies file
 * @link http://php.net/manual/en/function.copy.php
 * @param source string <p>
 * Path to the source file.
 * </p>
 * @param dest string <p>
 * The destination path. If dest is a URL, the
 * copy operation may fail if the wrapper does not support overwriting of
 * existing files.
 * </p>
 * <p>
 * If the destination file already exists, it will be overwritten.
 * </p>
 * @param context resource[optional] <p>
 * A valid context resource created with 
 * stream_context_create.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function copy ($source, $dest, $context = null) {}

/**
 * Create file with unique file name
 * @link http://php.net/manual/en/function.tempnam.php
 * @param dir string <p>
 * The directory where the temporary filename will be created.
 * </p>
 * @param prefix string <p>
 * The prefix of the generated temporary filename.
 * </p>
 * @return string the new temporary filename, or false on
 * failure.
 * </p>
 */
function tempnam ($dir, $prefix) {}

/**
 * Creates a temporary file
 * @link http://php.net/manual/en/function.tmpfile.php
 * @return resource a file handle, similar to the one returned by
 * fopen, for the new file, or false on failure.
 * </p>
 */
function tmpfile () {}

/**
 * Reads entire file into an array
 * @link http://php.net/manual/en/function.file.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * &tip.fopen-wrapper;
 * @param flags int[optional] <p>
 * The optional parameter flags can be one, or
 * more, of the following constants:
 * FILE_USE_INCLUDE_PATH
 * Search for the file in the include_path.
 * @param context resource[optional] <p>
 * A context resource created with the 
 * stream_context_create function.
 * </p>
 * <p>
 * &note.context-support;
 * </p>
 * @return array the file in an array. Each element of the array corresponds to a
 * line in the file, with the newline still attached. Upon failure,
 * file returns false.
 * </p>
 * <p>
 * Each line in the resulting array will include the line ending, unless
 * FILE_IGNORE_NEW_LINES is used, so you still need to
 * use rtrim if you do not want the line ending
 * present.
 * </p>
 * &note.line-endings;
 */
function file ($filename, $flags = null, $context = null) {}

/**
 * Reads entire file into a string
 * @link http://php.net/manual/en/function.file-get-contents.php
 * @param filename string <p>
 * Name of the file to read.
 * </p>
 * @param flags int[optional] <p>
 * For all versions prior to PHP 6, this parameter is called
 * use_include_path and is a bool.
 * The flags parameter is only available since
 * PHP 6. If you use an older version and want to search for 
 * filename in the 
 * include path, this 
 * parameter must be true. Since PHP 6, you have to use the 
 * FILE_USE_INCLUDE_PATH flag instead.
 * </p>
 * <p>
 * The value of flags can be any combination of 
 * the following flags (with some restrictions), joined with the binary OR 
 * (|) operator.
 * </p>
 * <p>
 * <table>
 * Available flags
 * <tr valign="top">
 * <td>Flag</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_USE_INCLUDE_PATH
 * </td>
 * <td>
 * Search for filename in the include directory.
 * See include_path for more
 * information.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_TEXT
 * </td>
 * <td>
 * If unicode semantics are enabled, the default encoding of the read
 * data is UTF-8. You can specify a different encoding by creating a 
 * custom context or by changing the default using 
 * stream_default_encoding. This flag cannot be 
 * used with FILE_BINARY. 
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_BINARY
 * </td>
 * <td>
 * With this flag, the file is read in binary mode. This is the default
 * setting and cannot be used with FILE_TEXT. 
 * </td>
 * </tr>
 * </table>
 * </p>
 * @param context resource[optional] <p>
 * A valid context resource created with 
 * stream_context_create. If you don't need to use a
 * custom context, you can skip this parameter by &null;.
 * </p>
 * @param offset int[optional] <p>
 * The offset where the reading starts.
 * </p>
 * @param maxlen int[optional] <p>
 * Maximum length of data read.
 * </p>
 * @return string The function returns the read data or false on failure.
 * </p>
 */
function file_get_contents ($filename, $flags = null, $context = null, $offset = null, $maxlen = null) {}

/**
 * Write a string to a file
 * @link http://php.net/manual/en/function.file-put-contents.php
 * @param filename string <p>
 * Path to the file where to write the data.
 * </p>
 * @param data mixed <p>
 * The data to write. Can be either a string, an
 * array or a stream resource (explained above).
 * </p>
 * <p>
 * If data is a stream resource, the
 * remaining buffer of that stream will be copied to the specified file.
 * This is similar with using stream_copy_to_stream.
 * </p>
 * <p>
 * You can also specify the data parameter as a single
 * dimension array. This is equivalent to
 * file_put_contents($filename, implode('', $array)).
 * </p>
 * @param flags int[optional] <p>
 * The value of flags can be any combination of 
 * the following flags (with some restrictions), joined with the binary OR 
 * (|) operator.
 * </p>
 * <p>
 * <table>
 * Available flags
 * <tr valign="top">
 * <td>Flag</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_USE_INCLUDE_PATH
 * </td>
 * <td>
 * Search for filename in the include directory.
 * See include_path for more
 * information.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_APPEND
 * </td>
 * <td>
 * If file filename already exists, append 
 * the data to the file instead of overwriting it.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * LOCK_EX
 * </td>
 * <td>
 * Acquire an exclusive lock on the file while proceeding to the 
 * writing.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_TEXT
 * </td>
 * <td>
 * data is written in text mode. If unicode 
 * semantics are enabled, the default encoding is UTF-8.
 * You can specify a different encoding by creating a custom context
 * or by using the stream_default_encoding to
 * change the default. This flag cannot be used with 
 * FILE_BINARY. This flag is only available since
 * PHP 6.
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>
 * FILE_BINARY
 * </td>
 * <td>
 * data will be written in binary mode. This
 * is the default setting and cannot be used with
 * FILE_TEXT. This flag is only available since
 * PHP 6.
 * </td>
 * </tr>
 * </table>
 * </p>
 * @param context resource[optional] <p>
 * A valid context resource created with 
 * stream_context_create.
 * </p>
 * @return int The function returns the number of bytes that were written to the file, or
 * false on failure.
 * </p>
 */
function file_put_contents ($filename, $data, $flags = null, $context = null) {}

/**
 * Runs the equivalent of the select() system call on the given
   arrays of streams with a timeout specified by tv_sec and tv_usec
 * @link http://php.net/manual/en/function.stream-select.php
 * @param read_streams
 * @param write_streams
 * @param except_streams
 * @param tv_sec
 * @param tv_usec[optional]
 */
function stream_select (&$read_streams, &$write_streams, &$except_streams, $tv_sec, $tv_usec) {}

/**
 * Create a streams context
 * @link http://php.net/manual/en/function.stream-context-create.php
 * @param options array[optional] <p>
 * Must be an associative array of associative arrays in the format
 * $arr['wrapper']['option'] = $value.
 * </p>
 * <p>
 * Default to an empty array.
 * </p>
 * @param params array[optional] <p>
 * Must be an associative array in the format
 * $arr['parameter'] = $value.
 * Refer to stream_context_set_params for
 * a listing of standard stream parameters.
 * </p>
 * @return resource A stream context resource.
 * </p>
 */
function stream_context_create (array $options = null, array $params = null) {}

/**
 * Set parameters for a stream/wrapper/context
 * @link http://php.net/manual/en/function.stream-context-set-params.php
 * @param stream_or_context
 * @param options
 */
function stream_context_set_params ($stream_or_context, $options) {}

/**
 * Sets an option for a stream/wrapper/context
 * @link http://php.net/manual/en/function.stream-context-set-option.php
 * @param stream_or_context
 * @param wrappername
 * @param optionname
 * @param value
 */
function stream_context_set_option ($stream_or_context, $wrappername, $optionname, $value) {}

/**
 * Retrieve options for a stream/wrapper/context
 * @link http://php.net/manual/en/function.stream-context-get-options.php
 * @param stream_or_context
 */
function stream_context_get_options ($stream_or_context) {}

/**
 * Retreive the default streams context
 * @link http://php.net/manual/en/function.stream-context-get-default.php
 * @param options[optional]
 */
function stream_context_get_default ($options) {}

/**
 * Attach a filter to a stream
 * @link http://php.net/manual/en/function.stream-filter-prepend.php
 * @param stream
 * @param filtername
 * @param read_write[optional]
 * @param filterparams[optional]
 */
function stream_filter_prepend ($stream, $filtername, $read_write, $filterparams) {}

/**
 * Attach a filter to a stream
 * @link http://php.net/manual/en/function.stream-filter-append.php
 * @param stream
 * @param filtername
 * @param read_write[optional]
 * @param filterparams[optional]
 */
function stream_filter_append ($stream, $filtername, $read_write, $filterparams) {}

/**
 * Remove a filter from a stream
 * @link http://php.net/manual/en/function.stream-filter-remove.php
 * @param stream_filter
 */
function stream_filter_remove ($stream_filter) {}

/**
 * Open Internet or Unix domain socket connection
 * @link http://php.net/manual/en/function.stream-socket-client.php
 * @param remoteaddress
 * @param errcode[optional]
 * @param errstring[optional]
 * @param timeout[optional]
 * @param flags[optional]
 * @param context[optional]
 */
function stream_socket_client ($remoteaddress, &$errcode, &$errstring, $timeout, $flags, $context) {}

/**
 * Create an Internet or Unix domain server socket
 * @link http://php.net/manual/en/function.stream-socket-server.php
 * @param localaddress
 * @param errcode[optional]
 * @param errstring[optional]
 * @param flags[optional]
 * @param context[optional]
 */
function stream_socket_server ($localaddress, &$errcode, &$errstring, $flags, $context) {}

/**
 * Accept a connection on a socket created by <function>stream_socket_server</function>
 * @link http://php.net/manual/en/function.stream-socket-accept.php
 * @param serverstream
 * @param timeout[optional]
 * @param peername[optional]
 */
function stream_socket_accept ($serverstream, $timeout, &$peername) {}

/**
 * Retrieve the name of the local or remote sockets
 * @link http://php.net/manual/en/function.stream-socket-get-name.php
 * @param stream
 * @param want_peer
 */
function stream_socket_get_name ($stream, $want_peer) {}

/**
 * Receives data from a socket, connected or not
 * @link http://php.net/manual/en/function.stream-socket-recvfrom.php
 * @param stream
 * @param amount
 * @param flags[optional]
 * @param remote_addr[optional]
 */
function stream_socket_recvfrom ($stream, $amount, $flags, &$remote_addr) {}

/**
 * Sends a message to a socket, whether it is connected or not
 * @link http://php.net/manual/en/function.stream-socket-sendto.php
 * @param stream
 * @param data
 * @param flags[optional]
 * @param target_addr[optional]
 */
function stream_socket_sendto ($stream, $data, $flags, $target_addr) {}

/**
 * Turns encryption on/off on an already connected socket
 * @link http://php.net/manual/en/function.stream-socket-enable-crypto.php
 * @param stream
 * @param enable
 * @param cryptokind[optional]
 * @param sessionstream[optional]
 */
function stream_socket_enable_crypto ($stream, $enable, $cryptokind, $sessionstream) {}

/**
 * Shutdown a full-duplex connection
 * @link http://php.net/manual/en/function.stream-socket-shutdown.php
 * @param stream resource <p>
 * An open stream (opened with stream_socket_client,
 * for example)
 * </p>
 * @param how int <p>
 * One of the following constants: STREAM_SHUT_RD
 * (disable further receptions), STREAM_SHUT_WR
 * (disable further transmissions) or
 * STREAM_SHUT_RDWR (disable further receptions and
 * transmissions).
 * </p>
 * @return bool &return.success;
 * </p>
 */
function stream_socket_shutdown ($stream, $how) {}

/**
 * Creates a pair of connected, indistinguishable socket streams
 * @link http://php.net/manual/en/function.stream-socket-pair.php
 * @param domain int <p>
 * The protocol family to be used: STREAM_PF_INET,
 * STREAM_PF_INET6 or
 * STREAM_PF_UNIX
 * </p>
 * @param type int <p>
 * The type of communication to be used:
 * STREAM_SOCK_DGRAM,
 * STREAM_SOCK_RAW,
 * STREAM_SOCK_RDM,
 * STREAM_SOCK_SEQPACKET or
 * STREAM_SOCK_STREAM
 * </p>
 * @param protocol int <p>
 * The protocol to be used: STREAM_IPPROTO_ICMP,
 * STREAM_IPPROTO_IP,
 * STREAM_IPPROTO_RAW,
 * STREAM_IPPROTO_TCP or
 * STREAM_IPPROTO_UDP 
 * </p>
 * @return array an array with the two socket resources on success, or
 * false on failure.
 * </p>
 */
function stream_socket_pair ($domain, $type, $protocol) {}

/**
 * Copies data from one stream to another
 * @link http://php.net/manual/en/function.stream-copy-to-stream.php
 * @param source resource <p>
 * The source stream
 * </p>
 * @param dest resource <p>
 * The destination stream
 * </p>
 * @param maxlength int[optional] <p>
 * Maximum bytes to copy
 * </p>
 * @param offset int[optional] <p>
 * The offset where to start to copy data
 * </p>
 * @return int the total count of bytes copied.
 * </p>
 */
function stream_copy_to_stream ($source, $dest, $maxlength = null, $offset = null) {}

/**
 * Reads remainder of a stream into a string
 * @link http://php.net/manual/en/function.stream-get-contents.php
 * @param source
 * @param maxlen[optional]
 * @param offset[optional]
 */
function stream_get_contents ($source, $maxlen, $offset) {}

/**
 * Gets line from file pointer and parse for CSV fields
 * @link http://php.net/manual/en/function.fgetcsv.php
 * @param handle resource <p>
 * A valid file pointer to a file successfully opened by
 * fopen, popen, or
 * fsockopen.
 * </p>
 * @param length int[optional] <p>
 * Must be greater than the longest line (in characters) to be found in
 * the CSV file (allowing for trailing line-end characters). It became
 * optional in PHP 5. Omitting this parameter (or setting it to 0 in PHP
 * 5.0.4 and later) the maximum line length is not limited, which is
 * slightly slower.
 * </p>
 * @param delimiter string[optional] <p>
 * Set the field delimiter (one character only). Defaults as a comma.
 * </p>
 * @param enclosure string[optional] <p>
 * Set the field enclosure character (one character only). Defaults as a
 * double quotation mark.
 * </p>
 * @param escape string[optional] <p>
 * Set the escape character (one character only). Defaults as a backslash
 * (\)
 * </p>
 * @return array an indexed array containing the fields read.
 * </p>
 * <p>
 * A blank line in a CSV file will be returned as an array
 * comprising a single null field, and will not be treated
 * as an error.
 * </p>
 * &note.line-endings;
 * <p>
 * fgetcsv returns false on error, including end of
 * file.
 * </p>
 */
function fgetcsv ($handle, $length = null, $delimiter = null, $enclosure = null, $escape = null) {}

/**
 * Format line as CSV and write to file pointer
 * @link http://php.net/manual/en/function.fputcsv.php
 * @param handle resource &fs.validfp.all;
 * @param fields array <p>
 * An array of values.
 * </p>
 * @param delimiter string[optional] <p>
 * The optional delimiter parameter sets the field
 * delimiter (one character only). Defaults as a comma: ,.
 * </p>
 * @param enclosure string[optional] <p>
 * The optional enclosure parameter sets the field
 * enclosure (one character only) and defaults to a double quotation mark:
 * ".
 * </p>
 * @return int the length of the written string, or false on failure.
 * </p>
 */
function fputcsv ($handle, array $fields, $delimiter = null, $enclosure = null) {}

/**
 * Portable advisory file locking
 * @link http://php.net/manual/en/function.flock.php
 * @param handle resource <p>
 * An open file pointer.
 * </p>
 * @param operation int <p>
 * operation is one of the following:
 * LOCK_SH to acquire a shared lock (reader).
 * @param wouldblock int[optional] <p>
 * The optional third argument is set to true if the lock would block
 * (EWOULDBLOCK errno condition).
 * </p>
 * @return bool &return.success;
 * </p>
 */
function flock ($handle, $operation, &$wouldblock = null) {}

/**
 * Extracts all meta tag content attributes from a file and returns an array
 * @link http://php.net/manual/en/function.get-meta-tags.php
 * @param filename string <p>
 * The path to the HTML file, as a string. This can be a local file or an
 * URL.
 * </p>
 * <p>
 * What get_meta_tags parses
 * ]]>
 * (pay attention to line endings - PHP uses a native function to
 * parse the input, so a Mac file won't work on Unix).
 * </p>
 * @param use_include_path bool[optional] <p>
 * Setting use_include_path to true will result
 * in PHP trying to open the file along the standard include path as per
 * the include_path directive.
 * This is used for local files, not URLs.
 * </p>
 * @return array an array with all the parsed meta tags.
 * </p>
 * <p>
 * The value of the name property becomes the key, the value of the content
 * property becomes the value of the returned array, so you can easily use
 * standard array functions to traverse it or access single values. 
 * Special characters in the value of the name property are substituted with
 * '_', the rest is converted to lower case. If two meta tags have the same
 * name, only the last one is returned.
 * </p>
 */
function get_meta_tags ($filename, $use_include_path = null) {}

/**
 * Sets file buffering on the given stream
 * @link http://php.net/manual/en/function.stream-set-write-buffer.php
 * @param fp
 * @param buffer
 */
function stream_set_write_buffer ($fp, $buffer) {}

/**
 * &Alias; <function>stream_set_write_buffer</function>
 * @link http://php.net/manual/en/function.set-file-buffer.php
 * @param fp
 * @param buffer
 */
function set_file_buffer ($fp, $buffer) {}

/**
 * @param socket
 * @param mode
 */
function set_socket_blocking ($socket, $mode) {}

/**
 * Set blocking/non-blocking mode on a stream
 * @link http://php.net/manual/en/function.stream-set-blocking.php
 * @param socket
 * @param mode
 */
function stream_set_blocking ($socket, $mode) {}

/**
 * &Alias; <function>stream_set_blocking</function>
 * @link http://php.net/manual/en/function.socket-set-blocking.php
 * @param socket
 * @param mode
 */
function socket_set_blocking ($socket, $mode) {}

/**
 * Retrieves header/meta data from streams/file pointers
 * @link http://php.net/manual/en/function.stream-get-meta-data.php
 * @param fp
 */
function stream_get_meta_data ($fp) {}

/**
 * Gets line from stream resource up to a given delimiter
 * @link http://php.net/manual/en/function.stream-get-line.php
 * @param stream
 * @param maxlen
 * @param ending[optional]
 */
function stream_get_line ($stream, $maxlen, $ending) {}

/**
 * Register a URL wrapper implemented as a PHP class
 * @link http://php.net/manual/en/function.stream-wrapper-register.php
 * @param protocol
 * @param classname
 * @param flags[optional]
 */
function stream_wrapper_register ($protocol, $classname, $flags) {}

/**
 * &Alias; <function>stream_wrapper_register</function>
 * @link http://php.net/manual/en/function.stream-register-wrapper.php
 * @param protocol
 * @param classname
 * @param flags[optional]
 */
function stream_register_wrapper ($protocol, $classname, $flags) {}

/**
 * Unregister a URL wrapper
 * @link http://php.net/manual/en/function.stream-wrapper-unregister.php
 * @param protocol string <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function stream_wrapper_unregister ($protocol) {}

/**
 * Restores a previously unregistered built-in wrapper
 * @link http://php.net/manual/en/function.stream-wrapper-restore.php
 * @param protocol string <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function stream_wrapper_restore ($protocol) {}

/**
 * Retrieve list of registered streams
 * @link http://php.net/manual/en/function.stream-get-wrappers.php
 */
function stream_get_wrappers () {}

/**
 * Retrieve list of registered socket transports
 * @link http://php.net/manual/en/function.stream-get-transports.php
 */
function stream_get_transports () {}

/**
 * @param stream
 */
function stream_is_local ($stream) {}

/**
 * Fetches all the headers sent by the server in response to a HTTP request
 * @link http://php.net/manual/en/function.get-headers.php
 * @param url string <p>
 * The target URL.
 * </p>
 * @param format int[optional] <p>
 * If the optional format parameter is set to 1,
 * get_headers parses the response and sets the 
 * array's keys.
 * </p>
 * @return array an indexed or associative array with the headers, or false on
 * failure.
 * </p>
 */
function get_headers ($url, $format = null) {}

/**
 * Set timeout period on a stream
 * @link http://php.net/manual/en/function.stream-set-timeout.php
 * @param stream
 * @param seconds
 * @param microseconds
 */
function stream_set_timeout ($stream, $seconds, $microseconds) {}

/**
 * &Alias; <function>stream_set_timeout</function>
 * @link http://php.net/manual/en/function.socket-set-timeout.php
 * @param stream
 * @param seconds
 * @param microseconds
 */
function socket_set_timeout ($stream, $seconds, $microseconds) {}

/**
 * &Alias; <function>stream_get_meta_data</function>
 * @link http://php.net/manual/en/function.socket-get-status.php
 * @param fp
 */
function socket_get_status ($fp) {}

/**
 * Returns canonicalized absolute pathname
 * @link http://php.net/manual/en/function.realpath.php
 * @param path string <p>
 * The path being checked.
 * </p>
 * @return string the canonicalized absolute pathname on success. The resulting path 
 * will have no symbolic link, '/./' or '/../' components.
 * </p>
 * <p>
 * realpath returns false on failure, e.g. if
 * the file does not exist. On BSD systems realpath
 * doesn't fail if only the last path component
 * doesn't exist, while other systems will return false.
 * </p>
 */
function realpath ($path) {}

/**
 * Match filename against a pattern
 * @link http://php.net/manual/en/function.fnmatch.php
 * @param pattern string <p>
 * The shell wildcard pattern.
 * </p>
 * @param string string <p>
 * The tested string. This function is especially useful for filenames,
 * but may also be used on regular strings.
 * </p>
 * <p>
 * The average user may be used to shell patterns or at least in their
 * simplest form to '?' and '*'
 * wildcards so using fnmatch instead of
 * ereg or preg_match for
 * frontend search expression input may be way more convenient for
 * non-programming users.
 * </p>
 * @param flags int[optional] <p>
 * See the Unix manpage on fnmatch(3) for flag names
 * (as long as they are not documented here).
 * </p>
 * @return bool true if there is a match, false otherwise.
 * </p>
 */
function fnmatch ($pattern, $string, $flags = null) {}

/**
 * Open Internet or Unix domain socket connection
 * @link http://php.net/manual/en/function.fsockopen.php
 * @param hostname string <p>
 * If you have compiled in OpenSSL support, you may prefix the
 * hostname with either ssl://
 * or tls:// to use an SSL or TLS client connection
 * over TCP/IP to connect to the remote host.
 * </p>
 * @param port int[optional] <p>
 * The port number.
 * </p>
 * @param errno int[optional] <p>
 * If provided, holds the system level error number that occurred in the
 * system-level connect() call.
 * </p>
 * <p>
 * If the value returned in errno is
 * 0 and the function returned false, it is an
 * indication that the error occurred before the 
 * connect() call. This is most likely due to a
 * problem initializing the socket.
 * </p>
 * @param errstr string[optional] <p>
 * The error message as a string.
 * </p>
 * @param timeout float[optional] <p>
 * The connection timeout, in seconds.
 * </p>
 * <p>
 * If you need to set a timeout for reading/writing data over the
 * socket, use stream_set_timeout, as the 
 * timeout parameter to
 * fsockopen only applies while connecting the
 * socket.
 * </p>
 * @return resource fsockopen returns a file pointer which may be used
 * together with the other file functions (such as
 * fgets, fgetss,
 * fwrite, fclose, and
 * feof). If the call fails, it will return false
 * </p>
 */
function fsockopen ($hostname, $port = null, &$errno = null, &$errstr = null, $timeout = null) {}

/**
 * Open persistent Internet or Unix domain socket connection
 * @link http://php.net/manual/en/function.pfsockopen.php
 * @param hostname string 
 * @param port int[optional] 
 * @param errno int[optional] 
 * @param errstr string[optional] 
 * @param timeout float[optional] 
 * @return resource 
 */
function pfsockopen ($hostname, $port = null, &$errno = null, &$errstr = null, $timeout = null) {}

/**
 * Pack data into binary string
 * @link http://php.net/manual/en/function.pack.php
 * @param format string <p>
 * The format string consists of format codes
 * followed by an optional repeater argument. The repeater argument can
 * be either an integer value or * for repeating to
 * the end of the input data. For a, A, h, H the repeat count specifies
 * how many characters of one data argument are taken, for @ it is the
 * absolute position where to put the next data, for everything else the
 * repeat count specifies how many data arguments are consumed and packed
 * into the resulting binary string.
 * </p>
 * <p>
 * Currently implemented formats are:
 * <table>
 * pack format characters
 * <tr valign="top">
 * <td>Code</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>a</td>
 * <td>NUL-padded string</td>
 * </tr>
 * <tr valign="top">
 * <td>A</td>
 * <td>SPACE-padded string</td></tr>
 * <tr valign="top">
 * <td>h</td>
 * <td>Hex string, low nibble first</td></tr>
 * <tr valign="top">
 * <td>H</td>
 * <td>Hex string, high nibble first</td></tr>
 * <tr valign="top"><td>c</td><td>signed char</td></tr>
 * <tr valign="top">
 * <td>C</td>
 * <td>unsigned char</td></tr>
 * <tr valign="top">
 * <td>s</td>
 * <td>signed short (always 16 bit, machine byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>S</td>
 * <td>unsigned short (always 16 bit, machine byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>n</td>
 * <td>unsigned short (always 16 bit, big endian byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>v</td>
 * <td>unsigned short (always 16 bit, little endian byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>i</td>
 * <td>signed integer (machine dependent size and byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>I</td>
 * <td>unsigned integer (machine dependent size and byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>l</td>
 * <td>signed long (always 32 bit, machine byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>L</td>
 * <td>unsigned long (always 32 bit, machine byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>N</td>
 * <td>unsigned long (always 32 bit, big endian byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>V</td>
 * <td>unsigned long (always 32 bit, little endian byte order)</td>
 * </tr>
 * <tr valign="top">
 * <td>f</td>
 * <td>float (machine dependent size and representation)</td>
 * </tr>
 * <tr valign="top">
 * <td>d</td>
 * <td>double (machine dependent size and representation)</td>
 * </tr>
 * <tr valign="top">
 * <td>x</td>
 * <td>NUL byte</td>
 * </tr>
 * <tr valign="top">
 * <td>X</td>
 * <td>Back up one byte</td>
 * </tr>
 * <tr valign="top">
 * <td>@</td>
 * <td>NUL-fill to absolute position</td>
 * </tr>
 * </table>
 * </p>
 * @param args mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return string a binary string containing data.
 * </p>
 */
function pack ($format, $args = null, $_ = null) {}

/**
 * Unpack data from binary string
 * @link http://php.net/manual/en/function.unpack.php
 * @param format string <p>
 * See pack for an explanation of the format codes.
 * </p>
 * @param data string <p>
 * The packed data.
 * </p>
 * @return array an associative array containing unpacked elements of binary
 * string.
 * </p>
 */
function unpack ($format, $data) {}

/**
 * Tells what the user's browser is capable of
 * @link http://php.net/manual/en/function.get-browser.php
 * @param user_agent string[optional] <p>
 * The User Agent to be analyzed. By default, the value of HTTP
 * User-Agent header is used; however, you can alter this (i.e., look up
 * another browser's info) by passing this parameter.
 * </p>
 * <p>
 * You can bypass this parameter with a &null; value.
 * </p>
 * @param return_array bool[optional] <p>
 * If set to true, this function will return an array
 * instead of an object.
 * </p>
 * @return mixed The information is returned in an object or an array which will contain
 * various data elements representing, for instance, the browser's major and
 * minor version numbers and ID string; true/false values for features
 * such as frames, JavaScript, and cookies; and so forth.
 * </p>
 * <p>
 * The cookies value simply means that the browser
 * itself is capable of accepting cookies and does not mean the user has
 * enabled the browser to accept cookies or not. The only way to test if
 * cookies are accepted is to set one with setcookie,
 * reload, and check for the value.
 * </p>
 */
function get_browser ($user_agent = null, $return_array = null) {}

/**
 * One-way string encryption (hashing)
 * @link http://php.net/manual/en/function.crypt.php
 * @param str string <p>
 * The string to be encrypted.
 * </p>
 * @param salt string[optional] <p>
 * An optional salt string to base the encryption on. If not provided,
 * one will be randomly generated by PHP each time you call this function.
 * </p>
 * <p>
 * If you are using the supplied salt, you should be aware that the salt
 * is generated once. If you are calling this function repeatedly, this
 * may impact both appearance and security.
 * </p>
 * @return string the encrypted string.
 * </p>
 */
function crypt ($str, $salt = null) {}

/**
 * Open directory handle
 * @link http://php.net/manual/en/function.opendir.php
 * @param path string <p>
 * The directory path that is to be opened
 * </p>
 * @param context resource[optional] <p>
 * For a description of the context parameter, 
 * refer to the streams section of
 * the manual.
 * </p>
 * @return resource a directory handle resource on success, or
 * false on failure.
 * </p> 
 * <p>
 * If path is not a valid directory or the
 * directory can not be opened due to permission restrictions or
 * filesystem errors, opendir returns false and
 * generates a PHP error of level 
 * E_WARNING. You can suppress the error output of
 * opendir by prepending
 * '@' to the
 * front of the function name.
 * </p>
 */
function opendir ($path, $context = null) {}

/**
 * Close directory handle
 * @link http://php.net/manual/en/function.closedir.php
 * @param dir_handle resource <p>
 * The directory handle resource previously opened
 * with opendir.
 * </p>
 * @return void 
 */
function closedir ($dir_handle) {}

/**
 * Change directory
 * @link http://php.net/manual/en/function.chdir.php
 * @param directory string <p>
 * The new current directory
 * </p>
 * @return bool &return.success;
 * </p>
 */
function chdir ($directory) {}

/**
 * Change the root directory
 * @link http://php.net/manual/en/function.chroot.php
 * @param directory string <p>
 * The new directory
 * </p>
 * @return bool &return.success;
 * </p>
 */
function chroot ($directory) {}

/**
 * Gets the current working directory
 * @link http://php.net/manual/en/function.getcwd.php
 * @return string the current working directory on success, or false on
 * failure.
 * </p> 
 * <p>
 * On some Unix variants, getcwd will return
 * false if any one of the parent directories does not have the
 * readable or search mode set, even if the current directory
 * does. See chmod for more information on
 * modes and permissions.
 * </p>
 */
function getcwd () {}

/**
 * Rewind directory handle
 * @link http://php.net/manual/en/function.rewinddir.php
 * @param dir_handle resource <p>
 * The directory handle resource previously opened
 * with opendir.
 * </p>
 * @return void 
 */
function rewinddir ($dir_handle) {}

/**
 * Read entry from directory handle
 * @link http://php.net/manual/en/function.readdir.php
 * @param dir_handle resource <p>
 * The directory handle resource previously opened
 * with opendir.
 * </p>
 * @return string the filename on success, or false on failure.
 * </p> 
 * &return.falseproblem;
 */
function readdir ($dir_handle) {}

/**
 * Return an instance of the Directory class
 * @link http://php.net/manual/en/class.dir.php
 * @param directory
 * @param context[optional]
 * @return string 
 */
function dir ($directory, $context) {}

/**
 * List files and directories inside the specified path
 * @link http://php.net/manual/en/function.scandir.php
 * @param directory string <p>
 * The directory that will be scanned.
 * </p>
 * @param sorting_order int[optional] <p>
 * By default, the sorted order is alphabetical in ascending order. If
 * the optional sorting_order is used (set to 1),
 * then the sort order is alphabetical in descending order.
 * </p>
 * @param context resource[optional] <p>
 * For a description of the context parameter, 
 * refer to the streams section of
 * the manual.
 * </p>
 * @return array an array of filenames on success, or false on 
 * failure. If directory is not a directory, then 
 * boolean false is returned, and an error of level 
 * E_WARNING is generated.
 * </p>
 */
function scandir ($directory, $sorting_order = null, $context = null) {}

/**
 * Find pathnames matching a pattern
 * @link http://php.net/manual/en/function.glob.php
 * @param pattern string <p>
 * The pattern. No tilde expansion or parameter substitution is done.
 * </p>
 * @param flags int[optional] <p>
 * Valid flags:
 * GLOB_MARK - Adds a slash to each item returned
 * @return array an array containing the matched files/directories, an empty array
 * if no file matched or false on error.
 * </p>
 * <p>
 * On some systems it is impossible to distinguish between empty match and an
 * error.
 * </p>
 */
function glob ($pattern, $flags = null) {}

/**
 * Gets last access time of file
 * @link http://php.net/manual/en/function.fileatime.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the time the file was last accessed, or false in case of
 * an error. The time is returned as a Unix timestamp.
 * </p>
 */
function fileatime ($filename) {}

/**
 * Gets inode change time of file
 * @link http://php.net/manual/en/function.filectime.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the time the file was last changed, or false in case of
 * an error. The time is returned as a Unix timestamp.
 * </p>
 */
function filectime ($filename) {}

/**
 * Gets file group
 * @link http://php.net/manual/en/function.filegroup.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the group ID of the file, or false in case
 * of an error. The group ID is returned in numerical format, use
 * posix_getgrgid to resolve it to a group name.
 * Upon failure, false is returned.
 * </p>
 */
function filegroup ($filename) {}

/**
 * Gets file inode
 * @link http://php.net/manual/en/function.fileinode.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the inode number of the file, or false in case of an error.
 * </p>
 */
function fileinode ($filename) {}

/**
 * Gets file modification time
 * @link http://php.net/manual/en/function.filemtime.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the time the file was last modified, or false in case of
 * an error. The time is returned as a Unix timestamp, which is
 * suitable for the date function.
 * </p>
 */
function filemtime ($filename) {}

/**
 * Gets file owner
 * @link http://php.net/manual/en/function.fileowner.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the user ID of the owner of the file, or false in case of
 * an error. The user ID is returned in numerical format, use
 * posix_getpwuid to resolve it to a username.
 * </p>
 */
function fileowner ($filename) {}

/**
 * Gets file permissions
 * @link http://php.net/manual/en/function.fileperms.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the permissions on the file, or false in case of an error.
 * </p>
 */
function fileperms ($filename) {}

/**
 * Gets file size
 * @link http://php.net/manual/en/function.filesize.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return int the size of the file in bytes, or false (and generates an error
 * of level E_WARNING) in case of an error.
 * </p>
 * Because PHP's integer type is signed and many platforms use 32bit integers,
 * filesize may return unexpected results for files which
 * are larger than 2GB. For files between 2GB and 4GB in size this can usually
 * be overcome by using sprintf("%u", filesize($file)).
 */
function filesize ($filename) {}

/**
 * Gets file type
 * @link http://php.net/manual/en/function.filetype.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return string the type of the file. Possible values are fifo, char,
 * dir, block, link, file, socket and unknown.
 * </p>
 * <p>
 * Returns false if an error occurs. filetype will also
 * produce an E_NOTICE message if the stat call fails
 * or if the file type is unknown.
 * </p>
 */
function filetype ($filename) {}

/**
 * Checks whether a file or directory exists
 * @link http://php.net/manual/en/function.file-exists.php
 * @param filename string <p>
 * Path to the file or directory.
 * </p>
 * <p>
 * On windows, use //computername/share/filename or
 * \\computername\share\filename to check files on
 * network shares.
 * </p>
 * @return bool true if the file or directory specified by
 * filename exists; false otherwise.
 * </p>
 * <p>
 * This function will return false for symlinks pointing to non-existing
 * files.
 * </p>
 * <p>
 * This function returns false for files inaccessible due to safe mode restrictions. However these
 * files still can be included if
 * they are located in safe_mode_include_dir.
 * </p>
 * <p>
 * The check is done using the real UID/GID instead of the effective one.
 * </p>
 */
function file_exists ($filename) {}

/**
 * Tells whether the filename is writable
 * @link http://php.net/manual/en/function.is-writable.php
 * @param filename string <p>
 * The filename being checked.
 * </p>
 * @return bool true if the filename exists and is
 * writable. 
 * </p>
 */
function is_writable ($filename) {}

/**
 * &Alias; <function>is_writable</function>
 * @link http://php.net/manual/en/function.is-writeable.php
 * @param filename
 */
function is_writeable ($filename) {}

/**
 * Tells whether the filename is readable
 * @link http://php.net/manual/en/function.is-readable.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return bool true if the file or directory specified by
 * filename exists and is readable, false otherwise.
 * </p>
 */
function is_readable ($filename) {}

/**
 * Tells whether the filename is executable
 * @link http://php.net/manual/en/function.is-executable.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return bool true if the filename exists and is executable, or false on
 * error.
 * </p>
 */
function is_executable ($filename) {}

/**
 * Tells whether the filename is a regular file
 * @link http://php.net/manual/en/function.is-file.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return bool true if the filename exists and is a regular file, false
 * otherwise.
 * </p>
 */
function is_file ($filename) {}

/**
 * Tells whether the filename is a directory
 * @link http://php.net/manual/en/function.is-dir.php
 * @param filename string <p>
 * Path to the file. If filename is a relative
 * filename, it will be checked relative to the current working
 * directory.
 * </p>
 * @return bool true if the filename exists and is a directory, false
 * otherwise.
 * </p>
 */
function is_dir ($filename) {}

/**
 * Tells whether the filename is a symbolic link
 * @link http://php.net/manual/en/function.is-link.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return bool true if the filename exists and is a symbolic link, false
 * otherwise.
 * </p>
 */
function is_link ($filename) {}

/**
 * Gives information about a file
 * @link http://php.net/manual/en/function.stat.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @return array <table>
 * stat and fstat result
 * format
 * <tr valign="top">
 * <td>Numeric</td>
 * <td>Associative (since PHP 4.0.6)</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>0</td>
 * <td>dev</td>
 * <td>device number</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>ino</td>
 * <td>inode number</td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>mode</td>
 * <td>inode protection mode</td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>nlink</td>
 * <td>number of links</td>
 * </tr>
 * <tr valign="top">
 * <td>4</td>
 * <td>uid</td>
 * <td>userid of owner</td>
 * </tr>
 * <tr valign="top">
 * <td>5</td>
 * <td>gid</td>
 * <td>groupid of owner</td>
 * </tr>
 * <tr valign="top">
 * <td>6</td>
 * <td>rdev</td>
 * <td>device type, if inode device</td>
 * </tr>
 * <tr valign="top">
 * <td>7</td>
 * <td>size</td>
 * <td>size in bytes</td>
 * </tr>
 * <tr valign="top">
 * <td>8</td>
 * <td>atime</td>
 * <td>time of last access (Unix timestamp)</td>
 * </tr>
 * <tr valign="top">
 * <td>9</td>
 * <td>mtime</td>
 * <td>time of last modification (Unix timestamp)</td>
 * </tr>
 * <tr valign="top">
 * <td>10</td>
 * <td>ctime</td>
 * <td>time of last inode change (Unix timestamp)</td>
 * </tr>
 * <tr valign="top">
 * <td>11</td>
 * <td>blksize</td>
 * <td>blocksize of filesystem IO *</td>
 * </tr>
 * <tr valign="top">
 * <td>12</td>
 * <td>blocks</td>
 * <td>number of blocks allocated *</td>
 * </tr>
 * </table>
 * * Only valid on systems supporting the st_blksize type - other
 * systems (e.g. Windows) return -1.
 * </p>
 * <p>
 * In case of error, stat returns false.
 * </p>
 */
function stat ($filename) {}

/**
 * Gives information about a file or symbolic link
 * @link http://php.net/manual/en/function.lstat.php
 * @param filename string <p>
 * Path to a file or a symbolic link.
 * </p>
 * @return array See the manual page for stat for information on
 * the structure of the array that lstat returns.
 * This function is identical to the stat function
 * except that if the filename parameter is a symbolic
 * link, the status of the symbolic link is returned, not the status of the
 * file pointed to by the symbolic link.
 * </p>
 */
function lstat ($filename) {}

/**
 * Changes file owner
 * @link http://php.net/manual/en/function.chown.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @param user mixed <p>
 * A user name or number.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function chown ($filename, $user) {}

/**
 * Changes file group
 * @link http://php.net/manual/en/function.chgrp.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @param group mixed <p>
 * A group name or number.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function chgrp ($filename, $group) {}

/**
 * Changes user ownership of symlink
 * @link http://php.net/manual/en/function.lchown.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @param user mixed <p>
 * User name or number.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function lchown ($filename, $user) {}

/**
 * Changes group ownership of symlink
 * @link http://php.net/manual/en/function.lchgrp.php
 * @param filename string <p>
 * Path to the symlink.
 * </p>
 * @param group mixed <p>
 * The group specified by name or number.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function lchgrp ($filename, $group) {}

/**
 * Changes file mode
 * @link http://php.net/manual/en/function.chmod.php
 * @param filename string <p>
 * Path to the file.
 * </p>
 * @param mode int <p>
 * Note that mode is not automatically
 * assumed to be an octal value, so strings (such as "g+w") will
 * not work properly. To ensure the expected operation,
 * you need to prefix mode with a zero (0):
 * </p>
 * <p>
 * ]]>
 * </p>
 * <p>
 * The mode parameter consists of three octal
 * number components specifying access restrictions for the owner,
 * the user group in which the owner is in, and to everybody else in
 * this order. One component can be computed by adding up the needed
 * permissions for that target user base. Number 1 means that you
 * grant execute rights, number 2 means that you make the file
 * writeable, number 4 means that you make the file readable. Add
 * up these numbers to specify needed rights. You can also read more
 * about modes on Unix systems with 'man 1 chmod' and 'man 2 chmod'.
 * </p>
 * <p>
 * @return bool &return.success;
 * </p>
 */
function chmod ($filename, $mode) {}

/**
 * Sets access and modification time of file
 * @link http://php.net/manual/en/function.touch.php
 * @param filename string <p>
 * The name of the file being touched.
 * </p>
 * @param time int[optional] <p>
 * The touch time. If time is not supplied, 
 * the current system time is used.
 * </p>
 * @param atime int[optional] <p>
 * If present, the access time of the given filename is set to 
 * the value of atime
 * </p>
 * @return bool &return.success;
 * </p>
 */
function touch ($filename, $time = null, $atime = null) {}

/**
 * Clears file status cache
 * @link http://php.net/manual/en/function.clearstatcache.php
 * @return void &return.void;
 * </p>
 */
function clearstatcache () {}

/**
 * Returns the total size of a directory
 * @link http://php.net/manual/en/function.disk-total-space.php
 * @param directory string <p>
 * A directory of the filesystem or disk partition.
 * </p>
 * @return float the total number of bytes as a float. 
 * </p>
 */
function disk_total_space ($directory) {}

/**
 * Returns available space in directory
 * @link http://php.net/manual/en/function.disk-free-space.php
 * @param directory string <p>
 * A directory of the filesystem or disk partition.
 * </p>
 * <p>
 * Given a file name instead of a directory, the behaviour of the
 * function is unspecified and may differ between operating systems and
 * PHP versions.
 * </p>
 * @return float the number of available bytes as a float. 
 * </p>
 */
function disk_free_space ($directory) {}

/**
 * &Alias; <function>disk_free_space</function>
 * @link http://php.net/manual/en/function.diskfreespace.php
 * @param path
 */
function diskfreespace ($path) {}

/**
 * Send mail
 * @link http://php.net/manual/en/function.mail.php
 * @param to string <p>
 * Receiver, or receivers of the mail.
 * </p>
 * <p>
 * The formatting of this string must comply with
 * RFC 2822. Some examples are:
 * user@example.com
 * user@example.com, anotheruser@example.com
 * User &lt;user@example.com&gt;
 * User &lt;user@example.com&gt;, Another User &lt;anotheruser@example.com&gt;
 * </p>
 * @param subject string <p>
 * Subject of the email to be sent.
 * </p>
 * <p>
 * This must not contain any newline characters, or the mail may not be
 * sent properly.
 * </p>
 * @param message string <p>
 * Message to be sent.
 * </p>
 * <p>
 * Each line should be separated with a LF (\n). Lines should not be larger
 * than 70 characters.
 * </p>
 * <p>
 * (Windows only) When PHP is talking to a SMTP server directly, if a full
 * stop is found on the start of a line, it is removed. To counter-act this,
 * replace these occurrences with a double dot.
 * ]]>
 * </p>
 * @param additional_headers string[optional] <p>
 * String to be inserted at the end of the email header.
 * </p>
 * <p>
 * This is typically used to add extra headers (From, Cc, and Bcc).
 * Multiple extra headers should be separated with a CRLF (\r\n).
 * </p>
 * <p>
 * When sending mail, the mail must contain
 * a From header. This can be set with the 
 * additional_headers parameter, or a default
 * can be set in &php.ini;.
 * </p>
 * <p>
 * Failing to do this will result in an error
 * message similar to Warning: mail(): "sendmail_from" not
 * set in php.ini or custom "From:" header missing.
 * The From header sets also
 * Return-Path under Windows.
 * </p>
 * <p>
 * If messages are not received, try using a LF (\n) only.
 * Some poor quality Unix mail transfer agents replace LF by CRLF
 * automatically (which leads to doubling CR if CRLF is used).
 * This should be a last resort, as it does not comply with
 * RFC 2822.
 * </p>
 * @param additional_parameters string[optional] <p>
 * The additional_parameters parameter
 * can be used to pass an additional parameter to the program configured
 * to use when sending mail using the sendmail_path
 * configuration setting. For example, this can be used to set the
 * envelope sender address when using sendmail with the
 * -f sendmail option.
 * </p>
 * <p>
 * The user that the webserver runs as should be added as a trusted user to the
 * sendmail configuration to prevent a 'X-Warning' header from being added
 * to the message when the envelope sender (-f) is set using this method.
 * For sendmail users, this file is /etc/mail/trusted-users.
 * </p>
 * @return bool true if the mail was successfully accepted for delivery, false otherwise.
 * </p>
 * <p>
 * It is important to note that just because the mail was accepted for delivery,
 * it does NOT mean the mail will actually reach the intended destination.
 * </p>
 */
function mail ($to, $subject, $message, $additional_headers = null, $additional_parameters = null) {}

/**
 * Calculate the hash value needed by EZMLM
 * @link http://php.net/manual/en/function.ezmlm-hash.php
 * @param addr string <p>
 * The email address that's being hashed.
 * </p>
 * @return int The hash value of addr.
 * </p>
 */
function ezmlm_hash ($addr) {}

/**
 * Open connection to system logger
 * @link http://php.net/manual/en/function.openlog.php
 * @param ident string <p>
 * The string ident is added to each message. 
 * </p>
 * @param option int <p>
 * The option argument is used to indicate
 * what logging options will be used when generating a log message.
 * <table>
 * openlog Options
 * <tr valign="top">
 * <td>Constant</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_CONS</td>
 * <td>
 * if there is an error while sending data to the system logger,
 * write directly to the system console
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_NDELAY</td>
 * <td>
 * open the connection to the logger immediately
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_ODELAY</td>
 * <td>
 * (default) delay opening the connection until the first
 * message is logged
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_PERROR</td>
 * <td>print log message also to standard error</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_PID</td>
 * <td>include PID with each message</td>
 * </tr>
 * </table>
 * You can use one or more of this options. When using multiple options
 * you need to OR them, i.e. to open the connection
 * immediately, write to the console and include the PID in each message,
 * you will use: LOG_CONS | LOG_NDELAY | LOG_PID
 * </p>
 * @param facility int <p>
 * The facility argument is used to specify what
 * type of program is logging the message. This allows you to specify
 * (in your machine's syslog configuration) how messages coming from
 * different facilities will be handled.
 * <table>
 * openlog Facilities
 * <tr valign="top">
 * <td>Constant</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_AUTH</td>
 * <td>
 * security/authorization messages (use 
 * LOG_AUTHPRIV instead
 * in systems where that constant is defined)
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_AUTHPRIV</td>
 * <td>security/authorization messages (private)</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_CRON</td>
 * <td>clock daemon (cron and at)</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_DAEMON</td>
 * <td>other system daemons</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_KERN</td>
 * <td>kernel messages</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_LOCAL0 ... LOG_LOCAL7</td>
 * <td>reserved for local use, these are not available in Windows</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_LPR</td>
 * <td>line printer subsystem</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_MAIL</td>
 * <td>mail subsystem</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_NEWS</td>
 * <td>USENET news subsystem</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_SYSLOG</td>
 * <td>messages generated internally by syslogd</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_USER</td>
 * <td>generic user-level messages</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_UUCP</td>
 * <td>UUCP subsystem</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * LOG_USER is the only valid log type under Windows
 * operating systems
 * </p>
 * @return bool &return.success;
 * </p>
 */
function openlog ($ident, $option, $facility) {}

/**
 * Generate a system log message
 * @link http://php.net/manual/en/function.syslog.php
 * @param priority int <p>
 * priority is a combination of the facility and
 * the level. Possible values are:
 * <table>
 * syslog Priorities (in descending order)
 * <tr valign="top">
 * <td>Constant</td>
 * <td>Description</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_EMERG</td>
 * <td>system is unusable</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_ALERT</td>
 * <td>action must be taken immediately</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_CRIT</td>
 * <td>critical conditions</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_ERR</td>
 * <td>error conditions</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_WARNING</td>
 * <td>warning conditions</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_NOTICE</td>
 * <td>normal, but significant, condition</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_INFO</td>
 * <td>informational message</td>
 * </tr>
 * <tr valign="top">
 * <td>LOG_DEBUG</td>
 * <td>debug-level message</td>
 * </tr>
 * </table>
 * </p>
 * @param message string <p>
 * The message to send, except that the two characters
 * %m will be replaced by the error message string
 * (strerror) corresponding to the present value of
 * errno.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function syslog ($priority, $message) {}

/**
 * Close connection to system logger
 * @link http://php.net/manual/en/function.closelog.php
 * @return bool &return.success;
 * </p>
 */
function closelog () {}

/**
 * Initializes all syslog related constants
 * @link http://php.net/manual/en/function.define-syslog-variables.php
 * @return void &return.void;
 * </p>
 */
function define_syslog_variables () {}

/**
 * Combined linear congruential generator
 * @link http://php.net/manual/en/function.lcg-value.php
 * @return float A pseudo random float value in the range of (0, 1)
 * </p>
 */
function lcg_value () {}

/**
 * Calculate the metaphone key of a string
 * @link http://php.net/manual/en/function.metaphone.php
 * @param str string <p>
 * The input string.
 * </p>
 * @param phones int[optional] <p>
 * </p>
 * @return string the metaphone key as a string.
 * </p>
 */
function metaphone ($str, $phones = null) {}

/**
 * Turn on output buffering
 * @link http://php.net/manual/en/function.ob-start.php
 * @param output_callback callback[optional] <p>
 * An optional output_callback function may be
 * specified. This function takes a string as a parameter and should
 * return a string. The function will be called when
 * ob_end_flush is called, or when the output buffer
 * is flushed to the browser at the end of the request. When
 * output_callback is called, it will receive the
 * contents of the output buffer as its parameter and is expected to
 * return a new output buffer as a result, which will be sent to the
 * browser. If the output_callback is not a
 * callable function, this function will return false.
 * </p>
 * <p>
 * If the callback function has two parameters, the second parameter is
 * filled with a bit-field consisting of
 * PHP_OUTPUT_HANDLER_START,
 * PHP_OUTPUT_HANDLER_CONT and
 * PHP_OUTPUT_HANDLER_END.
 * </p>
 * <p>
 * If output_callback returns false original
 * input is sent to the browser.
 * </p>
 * <p>
 * The output_callback parameter may be bypassed
 * by passing a &null; value.
 * </p>
 * <p>
 * ob_end_clean, ob_end_flush,
 * ob_clean, ob_flush and
 * ob_start may not be called from a callback
 * function. If you call them from callback function, the behavior is
 * undefined. If you would like to delete the contents of a buffer,
 * return "" (a null string) from callback function.
 * You can't even call functions using the output buffering functions like
 * print_r($expression, true) or
 * highlight_file($filename, true) from a callback
 * function.
 * </p>
 * <p>
 * In PHP 4.0.4, ob_gzhandler was introduced to
 * facilitate sending gz-encoded data to web browsers that support
 * compressed web pages. ob_gzhandler determines
 * what type of content encoding the browser will accept and will return
 * its output accordingly.
 * </p>
 * @param chunk_size int[optional] <p>
 * If the optional parameter chunk_size is passed, the
 * buffer will be flushed after any output call which causes the buffer's
 * length to equal or exceed chunk_size.
 * Default value 0 means that the function is called only in the end,
 * other special value 1 sets chunk_size to 4096.
 * </p>
 * @param erase bool[optional] <p>
 * If the optional parameter erase is set to false,
 * the buffer will not be deleted until the script finishes (as of PHP 4.3.0).
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ob_start ($output_callback = null, $chunk_size = null, $erase = null) {}

/**
 * Flush (send) the output buffer
 * @link http://php.net/manual/en/function.ob-flush.php
 * @return void &return.void;
 * </p>
 */
function ob_flush () {}

/**
 * Clean (erase) the output buffer
 * @link http://php.net/manual/en/function.ob-clean.php
 * @return void &return.void;
 * </p>
 */
function ob_clean () {}

/**
 * Flush (send) the output buffer and turn off output buffering
 * @link http://php.net/manual/en/function.ob-end-flush.php
 * @return bool &return.success; Reasons for failure are first that you called the
 * function without an active buffer or that for some reason a buffer could
 * not be deleted (possible for special buffer).
 * </p>
 */
function ob_end_flush () {}

/**
 * Clean (erase) the output buffer and turn off output buffering
 * @link http://php.net/manual/en/function.ob-end-clean.php
 * @return bool &return.success; Reasons for failure are first that you called the
 * function without an active buffer or that for some reason a buffer could
 * not be deleted (possible for special buffer).
 * </p>
 */
function ob_end_clean () {}

/**
 * Flush the output buffer, return it as a string and turn off output buffering
 * @link http://php.net/manual/en/function.ob-get-flush.php
 * @return string the output buffer or false if no buffering is active.
 * </p>
 */
function ob_get_flush () {}

/**
 * Get current buffer contents and delete current output buffer
 * @link http://php.net/manual/en/function.ob-get-clean.php
 * @return string the contents of the output buffer and end output buffering.
 * If output buffering isn't active then false is returned.
 * </p>
 */
function ob_get_clean () {}

/**
 * Return the length of the output buffer
 * @link http://php.net/manual/en/function.ob-get-length.php
 * @return int the length of the output buffer contents or false if no
 * buffering is active.
 * </p>
 */
function ob_get_length () {}

/**
 * Return the nesting level of the output buffering mechanism
 * @link http://php.net/manual/en/function.ob-get-level.php
 * @return int the level of nested output buffering handlers or zero if output
 * buffering is not active.
 * </p>
 */
function ob_get_level () {}

/**
 * Get status of output buffers
 * @link http://php.net/manual/en/function.ob-get-status.php
 * @param full_status bool[optional] <p>
 * true to return all active output buffer levels. If false or not
 * set, only the top level output buffer is returned.
 * </p>
 * @return array If called without the full_status parameter
 * or with full_status = false a simple array
 * with the following elements is returned:
 * 2
 * [type] => 0
 * [status] => 0
 * [name] => URL-Rewriter
 * [del] => 1
 * )
 * ]]>
 * Simple ob_get_status results
 * KeyValue
 * levelOutput nesting level
 * typePHP_OUTPUT_HANDLER_INTERNAL (0) or PHP_OUTPUT_HANDLER_USER (1)
 * statusOne of PHP_OUTPUT_HANDLER_START (0), PHP_OUTPUT_HANDLER_CONT (1) or PHP_OUTPUT_HANDLER_END (2)
 * nameName of active output handler or ' default output handler' if none is set
 * delErase-flag as set by ob_start
 * </p>
 * <p>
 * If called with full_status = true an array
 * with one element for each active output buffer level is returned.
 * The output level is used as key of the top level array and each array
 * element itself is another array holding status information
 * on one active output level.
 * Array
 * (
 * [chunk_size] => 0
 * [size] => 40960
 * [block_size] => 10240
 * [type] => 1
 * [status] => 0
 * [name] => default output handler
 * [del] => 1
 * )
 * [1] => Array
 * (
 * [chunk_size] => 0
 * [size] => 40960
 * [block_size] => 10240
 * [type] => 0
 * [buffer_size] => 0
 * [status] => 0
 * [name] => URL-Rewriter
 * [del] => 1
 * )
 * )
 * ]]>
 * </p>
 * <p>
 * The full output contains these additional elements:
 * Full ob_get_status results
 * KeyValue
 * chunk_sizeChunk size as set by ob_start
 * size...
 * blocksize...
 * </p>
 */
function ob_get_status ($full_status = null) {}

/**
 * Return the contents of the output buffer
 * @link http://php.net/manual/en/function.ob-get-contents.php
 * @return string This will return the contents of the output buffer or false, if output
 * buffering isn't active.
 * </p>
 */
function ob_get_contents () {}

/**
 * Turn implicit flush on/off
 * @link http://php.net/manual/en/function.ob-implicit-flush.php
 * @param flag int[optional] <p>
 * true to turn implicit flushing on, false otherwise.
 * Defaults to true.
 * </p>
 * @return void &return.void;
 * </p>
 */
function ob_implicit_flush ($flag = null) {}

/**
 * List all output handlers in use
 * @link http://php.net/manual/en/function.ob-list-handlers.php
 * @return array This will return an array with the output handlers in use (if any). If
 * output_buffering is enabled or
 * an anonymous function was used with ob_start,
 * ob_list_handlers will return "default output
 * handler".
 * </p>
 */
function ob_list_handlers () {}

/**
 * Sort an array by key
 * @link http://php.net/manual/en/function.ksort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * You may modify the behavior of the sort using the optional
 * parameter sort_flags, for details
 * see sort.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ksort (array &$array, $sort_flags = null) {}

/**
 * Sort an array by key in reverse order
 * @link http://php.net/manual/en/function.krsort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * You may modify the behavior of the sort using the optional parameter
 * sort_flags, for details see
 * sort.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function krsort (array &$array, $sort_flags = null) {}

/**
 * Sort an array using a "natural order" algorithm
 * @link http://php.net/manual/en/function.natsort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function natsort (array &$array) {}

/**
 * Sort an array using a case insensitive "natural order" algorithm
 * @link http://php.net/manual/en/function.natcasesort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function natcasesort (array &$array) {}

/**
 * Sort an array and maintain index association
 * @link http://php.net/manual/en/function.asort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * You may modify the behavior of the sort using the optional
 * parameter sort_flags, for details
 * see sort.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function asort (array &$array, $sort_flags = null) {}

/**
 * Sort an array in reverse order and maintain index association
 * @link http://php.net/manual/en/function.arsort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * You may modify the behavior of the sort using the optional parameter
 * sort_flags, for details see
 * sort.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function arsort (array &$array, $sort_flags = null) {}

/**
 * Sort an array
 * @link http://php.net/manual/en/function.sort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * The optional second parameter sort_flags
 * may be used to modify the sorting behavior using these values:
 * </p>
 * <p>
 * Sorting type flags:
 * SORT_REGULAR - compare items normally
 * (don't change types)
 * @return bool &return.success;
 * </p>
 */
function sort (array &$array, $sort_flags = null) {}

/**
 * Sort an array in reverse order
 * @link http://php.net/manual/en/function.rsort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param sort_flags int[optional] <p>
 * You may modify the behavior of the sort using the optional
 * parameter sort_flags, for details see
 * sort.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function rsort (array &$array, $sort_flags = null) {}

/**
 * Sort an array by values using a user-defined comparison function
 * @link http://php.net/manual/en/function.usort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param cmp_function callback <p>
 * The comparison function must return an integer less than, equal to, or
 * greater than zero if the first argument is considered to be
 * respectively less than, equal to, or greater than the second.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function usort (array &$array, $cmp_function) {}

/**
 * Sort an array with a user-defined comparison function and maintain index association
 * @link http://php.net/manual/en/function.uasort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param cmp_function callback <p>
 * See usort and uksort for
 * examples of user-defined comparison functions.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function uasort (array &$array, $cmp_function) {}

/**
 * Sort an array by keys using a user-defined comparison function
 * @link http://php.net/manual/en/function.uksort.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param cmp_function callback <p>
 * The callback comparison function.
 * </p>
 * <p>
 * Function cmp_function should accept two
 * parameters which will be filled by pairs of array keys.
 * The comparison function must return an integer less than, equal
 * to, or greater than zero if the first argument is considered to
 * be respectively less than, equal to, or greater than the
 * second.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function uksort (array &$array, $cmp_function) {}

/**
 * Shuffle an array
 * @link http://php.net/manual/en/function.shuffle.php
 * @param array array <p>
 * The array.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function shuffle (array &$array) {}

/**
 * Apply a user function to every member of an array
 * @link http://php.net/manual/en/function.array-walk.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param funcname callback <p>
 * Typically, funcname takes on two parameters.
 * The array parameter's value being the first, and
 * the key/index second.
 * </p>
 * <p>
 * If funcname needs to be working with the
 * actual values of the array, specify the first parameter of
 * funcname as a
 * reference. Then,
 * any changes made to those elements will be made in the
 * original array itself.
 * </p>
 * <p>
 * Users may not change the array itself from the
 * callback function. e.g. Add/delete elements, unset elements, etc. If
 * the array that array_walk is applied to is
 * changed, the behavior of this function is undefined, and unpredictable.
 * </p>
 * @param userdata mixed[optional] <p>
 * If the optional userdata parameter is supplied,
 * it will be passed as the third parameter to the callback
 * funcname.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function array_walk (array &$array, $funcname, $userdata = null) {}

/**
 * Apply a user function recursively to every member of an array
 * @link http://php.net/manual/en/function.array-walk-recursive.php
 * @param input array <p>
 * The input array.
 * </p>
 * @param funcname callback <p>
 * Typically, funcname takes on two parameters.
 * The input parameter's value being the first, and
 * the key/index second.
 * </p>
 * <p>
 * If funcname needs to be working with the
 * actual values of the array, specify the first parameter of
 * funcname as a
 * reference. Then,
 * any changes made to those elements will be made in the
 * original array itself.
 * </p>
 * @param userdata mixed[optional] <p>
 * If the optional userdata parameter is supplied,
 * it will be passed as the third parameter to the callback
 * funcname.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function array_walk_recursive (array &$input, $funcname, $userdata = null) {}

/**
 * Count elements in an array, or properties in an object
 * @link http://php.net/manual/en/function.count.php
 * @param var mixed <p>
 * The array.
 * </p>
 * @param mode int[optional] <p>
 * If the optional mode parameter is set to
 * COUNT_RECURSIVE (or 1), count
 * will recursively count the array. This is particularly useful for
 * counting all the elements of a multidimensional array. The default
 * value for mode is 0.
 * count does not detect infinite recursion.
 * </p>
 * @return int the number of elements in var, which is
 * typically an array, since anything else will have one
 * element.
 * </p>
 * <p>
 * If var is not an array or an object with
 * implemented Countable interface,
 * 1 will be returned.
 * There is one exception, if var is &null;,
 * 0 will be returned.
 * </p>
 * <p>
 * count may return 0 for a variable that isn't set,
 * but it may also return 0 for a variable that has been initialized with an
 * empty array. Use isset to test if a variable is set.
 * </p>
 */
function count ($var, $mode = null) {}

/**
 * Set the internal pointer of an array to its last element
 * @link http://php.net/manual/en/function.end.php
 * @param array array <p>
 * The array.
 * </p>
 * @return mixed the value of the last element.
 * </p>
 */
function end (array &$array) {}

/**
 * Rewind the internal array pointer
 * @link http://php.net/manual/en/function.prev.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return mixed the array value in the previous place that's pointed to by
 * the internal array pointer, or false if there are no more
 * elements.
 * </p>
 */
function prev (array &$array) {}

/**
 * Advance the internal array pointer of an array
 * @link http://php.net/manual/en/function.next.php
 * @param array array <p>
 * The array being affected.
 * </p>
 * @return mixed the array value in the next place that's pointed to by the
 * internal array pointer, or false if there are no more elements.
 * </p>
 * &return.falseproblem;
 */
function next (array &$array) {}

/**
 * Set the internal pointer of an array to its first element
 * @link http://php.net/manual/en/function.reset.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return mixed the value of the first array element, or false if the array is
 * empty.
 * </p>
 */
function reset (array &$array) {}

/**
 * Return the current element in an array
 * @link http://php.net/manual/en/function.current.php
 * @param array array <p>
 * The array.
 * </p>
 * @return mixed The current function simply returns the
 * value of the array element that's currently being pointed to by the
 * internal pointer. It does not move the pointer in any way. If the
 * internal pointer points beyond the end of the elements list or the array is 
 * empty, current returns false.
 * </p>
 * &return.falseproblem;
 */
function current (array &$array) {}

/**
 * Fetch a key from an array
 * @link http://php.net/manual/en/function.key.php
 * @param array array <p>
 * The array.
 * </p>
 * @return mixed the index.
 * </p>
 */
function key (array &$array) {}

/**
 * Find lowest value
 * @link http://php.net/manual/en/function.min.php
 * @param values array <p>
 * An array containing the values.
 * </p>
 * @return mixed min returns the numerically lowest of the
 * parameter values.
 * </p>
 */
function min (array $values) {}

/**
 * Find highest value
 * @link http://php.net/manual/en/function.max.php
 * @param values array <p>
 * An array containing the values.
 * </p>
 * @return mixed max returns the numerically highest of the
 * parameter values.
 * </p>
 */
function max (array $values) {}

/**
 * Checks if a value exists in an array
 * @link http://php.net/manual/en/function.in-array.php
 * @param needle mixed <p>
 * The searched value.
 * </p>
 * <p>
 * If needle is a string, the comparison is done
 * in a case-sensitive manner.
 * </p>
 * @param haystack array <p>
 * The array.
 * </p>
 * @param strict bool[optional] <p>
 * If the third parameter strict is set to true
 * then the in_array function will also check the
 * types of the
 * needle in the haystack.
 * </p>
 * @return bool true if needle is found in the array,
 * false otherwise.
 * </p>
 */
function in_array ($needle, array $haystack, $strict = null) {}

/**
 * Searches the array for a given value and returns the corresponding key if successful
 * @link http://php.net/manual/en/function.array-search.php
 * @param needle mixed <p>
 * The searched value.
 * </p>
 * <p>
 * If needle is a string, the comparison is done
 * in a case-sensitive manner.
 * </p>
 * @param haystack array <p>
 * The array.
 * </p>
 * @param strict bool[optional] <p>
 * If the third parameter strict is set to true
 * then the array_search function will also check the
 * types of the
 * needle in the haystack.
 * </p>
 * @return mixed the key for needle if it is found in the
 * array, false otherwise.
 * </p>
 * <p>
 * If needle is found in haystack
 * more than once, the first matching key is returned. To return the keys for
 * all matching values, use array_keys with the optional
 * search_value parameter instead.
 * </p>
 * &return.falseproblem;
 */
function array_search ($needle, array $haystack, $strict = null) {}

/**
 * Import variables into the current symbol table from an array
 * @link http://php.net/manual/en/function.extract.php
 * @param var_array array <p>
 * Note that prefix is only required if
 * extract_type is EXTR_PREFIX_SAME,
 * EXTR_PREFIX_ALL, EXTR_PREFIX_INVALID
 * or EXTR_PREFIX_IF_EXISTS. If
 * the prefixed result is not a valid variable name, it is not
 * imported into the symbol table. Prefixes are automatically separated from
 * the array key by an underscore character.
 * </p>
 * @param extract_type int[optional] <p>
 * The way invalid/numeric keys and collisions are treated is determined
 * by the extract_type. It can be one of the
 * following values:
 * EXTR_OVERWRITE
 * If there is a collision, overwrite the existing variable.
 * @param prefix string[optional] Only overwrite the variable if it already exists in the
 * current symbol table, otherwise do nothing. This is useful
 * for defining a list of valid variables and then extracting
 * only those variables you have defined out of $_REQUEST, for
 * example.
 * @return int the number of variables successfully imported into the symbol
 * table.
 * </p>
 */
function extract (array $var_array, $extract_type = null, $prefix = null) {}

/**
 * Create array containing variables and their values
 * @link http://php.net/manual/en/function.compact.php
 * @param varname mixed <p>
 * compact takes a variable number of parameters.
 * Each parameter can be either a string containing the name of the
 * variable, or an array of variable names. The array can contain other
 * arrays of variable names inside it; compact
 * handles it recursively.
 * </p>
 * @param _ mixed[optional] 
 * @return array the output array with all the variables added to it.
 * </p>
 */
function compact ($varname, $_ = null) {}

/**
 * Fill an array with values
 * @link http://php.net/manual/en/function.array-fill.php
 * @param start_index int <p>
 * The first index of the returned array
 * </p>
 * @param num int <p>
 * Number of elements to insert
 * </p>
 * @param value mixed <p>
 * Value to use for filling
 * </p>
 * @return array the filled array
 * </p>
 */
function array_fill ($start_index, $num, $value) {}

/**
 * Fill an array with values, specifying keys
 * @link http://php.net/manual/en/function.array-fill-keys.php
 * @param keys array <p>
 * Array of values that will be used as keys. Illegal values
 * for key will be converted to string.
 * </p>
 * @param value mixed <p>
 * Value to use for filling
 * </p>
 * @return array the filled array
 * </p>
 */
function array_fill_keys (array $keys, $value) {}

/**
 * Create an array containing a range of elements
 * @link http://php.net/manual/en/function.range.php
 * @param low mixed <p>
 * Low value.
 * </p>
 * @param high mixed <p>
 * High value.
 * </p>
 * @param step number[optional] <p>
 * If a step value is given, it will be used as the
 * increment between elements in the sequence. step
 * should be given as a positive number. If not specified,
 * step will default to 1.
 * </p>
 * @return array an array of elements from low to
 * high, inclusive. If low > high, the sequence will
 * be from high to low.
 * </p>
 */
function range ($low, $high, $step = null) {}

/**
 * Sort multiple or multi-dimensional arrays
 * @link http://php.net/manual/en/function.array-multisort.php
 * @param arr array <p>
 * An array being sorted.
 * </p>
 * @param arg mixed[optional] <p>
 * Optionally another array, or sort options for the
 * previous array argument: 
 * SORT_ASC, 
 * SORT_DESC, 
 * SORT_REGULAR,
 * SORT_NUMERIC,
 * SORT_STRING.
 * </p>
 * @param _ mixed[optional] 
 * @return bool &return.success;
 * </p>
 */
function array_multisort (array $arr, $arg = null, $_ = null) {}

/**
 * Push one or more elements onto the end of array
 * @link http://php.net/manual/en/function.array-push.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param var mixed <p>
 * The pushed value.
 * </p>
 * @param _ mixed[optional] 
 * @return int the new number of elements in the array.
 * </p>
 */
function array_push (array &$array, $var, $_ = null) {}

/**
 * Pop the element off the end of array
 * @link http://php.net/manual/en/function.array-pop.php
 * @param array array <p>
 * The array to get the value from.
 * </p>
 * @return mixed the last value of array.
 * If array is empty (or is not an array),
 * &null; will be returned.
 * </p>
 */
function array_pop (array &$array) {}

/**
 * Shift an element off the beginning of array
 * @link http://php.net/manual/en/function.array-shift.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return mixed the shifted value, or &null; if array is
 * empty or is not an array.
 * </p>
 */
function array_shift (array &$array) {}

/**
 * Prepend one or more elements to the beginning of an array
 * @link http://php.net/manual/en/function.array-unshift.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param var mixed <p>
 * The prepended variable.
 * </p>
 * @param _ mixed[optional] 
 * @return int the new number of elements in the array.
 * </p>
 */
function array_unshift (array &$array, $var, $_ = null) {}

/**
 * Remove a portion of the array and replace it with something else
 * @link http://php.net/manual/en/function.array-splice.php
 * @param input array <p>
 * The input array.
 * </p>
 * @param offset int <p>
 * If offset is positive then the start of removed
 * portion is at that offset from the beginning of the
 * input array. If offset
 * is negative then it starts that far from the end of the
 * input array.
 * </p>
 * @param length int[optional] <p>
 * If length is omitted, removes everything
 * from offset to the end of the array. If
 * length is specified and is positive, then
 * that many elements will be removed. If
 * length is specified and is negative then
 * the end of the removed portion will be that many elements from
 * the end of the array. Tip: to remove everything from
 * offset to the end of the array when
 * replacement is also specified, use
 * count($input) for
 * length.
 * </p>
 * @param replacement mixed[optional] <p>
 * If replacement array is specified, then the
 * removed elements are replaced with elements from this array.
 * </p>
 * <p>
 * If offset and length
 * are such that nothing is removed, then the elements from the
 * replacement array are inserted in the place
 * specified by the offset. Note that keys in
 * replacement array are not preserved.
 * </p>
 * <p>
 * If replacement is just one element it is
 * not necessary to put array()
 * around it, unless the element is an array itself.
 * </p>
 * @return array the array consisting of the extracted elements.
 * </p>
 */
function array_splice (array &$input, $offset, $length = null, $replacement = null) {}

/**
 * Extract a slice of the array
 * @link http://php.net/manual/en/function.array-slice.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param offset int <p>
 * If offset is non-negative, the sequence will
 * start at that offset in the array. If
 * offset is negative, the sequence will
 * start that far from the end of the array.
 * </p>
 * @param length int[optional] <p>
 * If length is given and is positive, then
 * the sequence will have that many elements in it. If
 * length is given and is negative then the
 * sequence will stop that many elements from the end of the
 * array. If it is omitted, then the sequence will have everything
 * from offset up until the end of the
 * array.
 * </p>
 * @param preserve_keys bool[optional] <p>
 * Note that array_slice will reorder and reset the
 * array indices by default. You can change this behaviour by setting
 * preserve_keys to true.
 * </p>
 * @return array the slice.
 * </p>
 */
function array_slice (array $array, $offset, $length = null, $preserve_keys = null) {}

/**
 * Merge one or more arrays
 * @link http://php.net/manual/en/function.array-merge.php
 * @param array1 array <p>
 * Initial array to merge.
 * </p>
 * @param array2 array[optional] 
 * @param _ array[optional] 
 * @return array the resulting array.
 * </p>
 */
function array_merge (array $array1, array $array2 = null, array $_ = null) {}

/**
 * Merge two or more arrays recursively
 * @link http://php.net/manual/en/function.array-merge-recursive.php
 * @param array1 array <p>
 * Initial array to merge.
 * </p>
 * @param _ array[optional] 
 * @return array An array of values resulted from merging the arguments together.
 * </p>
 */
function array_merge_recursive (array $array1, array $_ = null) {}

/**
 * Return all the keys of an array
 * @link http://php.net/manual/en/function.array-keys.php
 * @param input array <p>
 * An array containing keys to return.
 * </p>
 * @param search_value mixed[optional] <p>
 * If specified, then only keys containing these values are returned.
 * </p>
 * @param strict bool[optional] <p>
 * As of PHP 5, this parameter determines if strict comparision (===)
 * should be used during the search.
 * </p>
 * @return array an array of all the keys in input.
 * </p>
 */
function array_keys (array $input, $search_value = null, $strict = null) {}

/**
 * Return all the values of an array
 * @link http://php.net/manual/en/function.array-values.php
 * @param input array <p>
 * The array.
 * </p>
 * @return array an indexed array of values.
 * </p>
 */
function array_values (array $input) {}

/**
 * Counts all the values of an array
 * @link http://php.net/manual/en/function.array-count-values.php
 * @param input array <p>
 * The array of values to count
 * </p>
 * @return array an associative array of values from input as
 * keys and their count as value.
 * </p>
 */
function array_count_values (array $input) {}

/**
 * Return an array with elements in reverse order
 * @link http://php.net/manual/en/function.array-reverse.php
 * @param array array <p>
 * The input array.
 * </p>
 * @param preserve_keys bool[optional] <p>
 * If set to true keys are preserved.
 * </p>
 * @return array the reversed array.
 * </p>
 */
function array_reverse (array $array, $preserve_keys = null) {}

/**
 * Iteratively reduce the array to a single value using a callback function
 * @link http://php.net/manual/en/function.array-reduce.php
 * @param input array <p>
 * The input array.
 * </p>
 * @param function callback <p>
 * The callback function.
 * </p>
 * @param initial int[optional] <p>
 * If the optional initial is available, it will
 * be used at the beginning of the process, or as a final result in case
 * the array is empty.
 * </p>
 * @return mixed the resulting value.
 * </p>
 * <p>
 * If the array is empty and initial is not passed,
 * array_reduce returns &null;.
 * </p>
 */
function array_reduce (array $input, $function, $initial = null) {}

/**
 * Pad array to the specified length with a value
 * @link http://php.net/manual/en/function.array-pad.php
 * @param input array <p>
 * Initial array of values to pad.
 * </p>
 * @param pad_size int <p>
 * New size of the array.
 * </p>
 * @param pad_value mixed <p>
 * Value to pad if input is less than
 * pad_size.
 * </p>
 * @return array a copy of the input padded to size specified
 * by pad_size with value 
 * pad_value. If pad_size is 
 * positive then the array is padded on the right, if it's negative then 
 * on the left. If the absolute value of pad_size is less than or equal to 
 * the length of the input then no padding takes place.
 * </p>
 */
function array_pad (array $input, $pad_size, $pad_value) {}

/**
 * Exchanges all keys with their associated values in an array
 * @link http://php.net/manual/en/function.array-flip.php
 * @param trans array <p>
 * An array of key/value pairs to be flipped.
 * </p>
 * @return array the flipped array on success and false on failure.
 * </p>
 */
function array_flip (array $trans) {}

/**
 * Changes all keys in an array
 * @link http://php.net/manual/en/function.array-change-key-case.php
 * @param input array <p>
 * The array to work on
 * </p>
 * @param case int[optional] <p>
 * Either CASE_UPPER or
 * CASE_LOWER (default)
 * </p>
 * @return array an array with its keys lower or uppercased, or false if
 * input is not an array.
 * </p>
 */
function array_change_key_case (array $input, $case = null) {}

/**
 * Pick one or more random entries out of an array
 * @link http://php.net/manual/en/function.array-rand.php
 * @param input array <p>
 * The input array.
 * </p>
 * @param num_req int[optional] <p>
 * Specifies how many entries you want to pick - if not specified,
 * defaults to 1.
 * </p>
 * @return mixed If you are picking only one entry, array_rand
 * returns the key for a random entry. Otherwise, it returns an array
 * of keys for the random entries. This is done so that you can pick
 * random keys as well as values out of the array.
 * </p>
 */
function array_rand (array $input, $num_req = null) {}

/**
 * Removes duplicate values from an array
 * @link http://php.net/manual/en/function.array-unique.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return array the filtered array.
 * </p>
 */
function array_unique (array $array) {}

/**
 * Computes the intersection of arrays
 * @link http://php.net/manual/en/function.array-intersect.php
 * @param array1 array <p>
 * The array with master values to check.
 * </p>
 * @param array2 array <p>
 * An array to compare values against.
 * </p>
 * @param _ array[optional] 
 * @return array an array containing all of the values in 
 * array1 whose values exist in all of the parameters.
 * </p>
 */
function array_intersect (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the intersection of arrays using keys for comparison
 * @link http://php.net/manual/en/function.array-intersect-key.php
 * @param array1 array <p>
 * The array with master keys to check.
 * </p>
 * @param array2 array <p>
 * An array to compare keys against.
 * </p>
 * @param _ array[optional] 
 * @return array an associative array containing all the values of 
 * array1 which have matching keys that are present in all
 * arguments.
 * </p>
 */
function array_intersect_key (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the intersection of arrays using a callback function on the keys for comparison
 * @link http://php.net/manual/en/function.array-intersect-ukey.php
 * @param array1 array <p>
 * Initial array for comparision of the arrays.
 * </p>
 * @param array2 array <p>
 * First array to compare keys against.
 * </p>
 * @param _ array[optional] 
 * @param key_compare_func callback <p>
 * User supplied callback function to do the comparision.
 * </p>
 * @return array the values of array1 whose keys exist
 * in all the arguments.
 * </p>
 */
function array_intersect_ukey (array $array1, array $array2, array $_ = null, $key_compare_func) {}

/**
 * Computes the intersection of arrays, compares data by a callback function
 * @link http://php.net/manual/en/function.array-uintersect.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * The callback comparison function.
 * </p>
 * <p>
 * The user supplied callback function is used for comparison.
 * It must return an integer less than, equal to, or greater than zero if
 * the first argument is considered to be respectively less than, equal
 * to, or greater than the second.
 * </p>
 * @return array an array containing all the values of array1
 * that are present in all the arguments.
 * </p>
 */
function array_uintersect (array $array1, array $array2, array $_ = null, $data_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check
 * @link http://php.net/manual/en/function.array-intersect-assoc.php
 * @param array1 array <p>
 * The array with master values to check.
 * </p>
 * @param array2 array <p>
 * An array to compare values against.
 * </p>
 * @param _ array[optional] 
 * @return array an associative array containing all the values in 
 * array1 that are present in all of the arguments.
 * </p>
 */
function array_intersect_assoc (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the intersection of arrays with additional index check, compares data by a callback function
 * @link http://php.net/manual/en/function.array-uintersect-assoc.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * For comparison is used the user supplied callback function.
 * It must return an integer less than, equal
 * to, or greater than zero if the first argument is considered to
 * be respectively less than, equal to, or greater than the
 * second.
 * </p>
 * @return array an array containing all the values of
 * array1 that are present in all the arguments.
 * </p>
 */
function array_uintersect_assoc (array $array1, array $array2, array $_ = null, $data_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check, compares indexes by a callback function
 * @link http://php.net/manual/en/function.array-intersect-uassoc.php
 * @param array1 array <p>
 * Initial array for comparision of the arrays.
 * </p>
 * @param array2 array <p>
 * First array to compare keys against.
 * </p>
 * @param _ array[optional] 
 * @param key_compare_func callback <p>
 * User supplied callback function to do the comparision.
 * </p>
 * @return array the values of array1 whose values exist
 * in all of the arguments.
 * </p>
 */
function array_intersect_uassoc (array $array1, array $array2, array $_ = null, $key_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check, compares data and indexes by a callback functions
 * @link http://php.net/manual/en/function.array-uintersect-uassoc.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * For comparison is used the user supplied callback function.
 * It must return an integer less than, equal
 * to, or greater than zero if the first argument is considered to
 * be respectively less than, equal to, or greater than the
 * second.
 * </p>
 * @param key_compare_func callback <p>
 * Key comparison callback function.
 * </p>
 * @return array an array containing all the values of
 * array1 that are present in all the arguments.
 * </p>
 */
function array_uintersect_uassoc (array $array1, array $array2, array $_ = null, $data_compare_func, $key_compare_func) {}

/**
 * Computes the difference of arrays
 * @link http://php.net/manual/en/function.array-diff.php
 * @param array1 array <p>
 * The array to compare from
 * </p>
 * @param array2 array <p>
 * An array to compare against
 * </p>
 * @param _ array[optional] 
 * @return array an array containing all the entries from
 * array1 that are not present in any of the other arrays.
 * </p>
 */
function array_diff (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the difference of arrays using keys for comparison
 * @link http://php.net/manual/en/function.array-diff-key.php
 * @param array1 array <p>
 * The array to compare from
 * </p>
 * @param array2 array <p>
 * An array to compare against
 * </p>
 * @param _ array[optional] 
 * @return array an array containing all the entries from
 * array1 that are not present in any of the other arrays.
 * </p>
 */
function array_diff_key (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the difference of arrays using a callback function on the keys for comparison
 * @link http://php.net/manual/en/function.array-diff-ukey.php
 * @param array1 array <p>
 * The array to compare from
 * </p>
 * @param array2 array <p>
 * An array to compare against
 * </p>
 * @param _ array[optional] 
 * @param key_compare_func callback <p>
 * callback function to use.
 * The callback function must return an integer less than, equal
 * to, or greater than zero if the first argument is considered to
 * be respectively less than, equal to, or greater than the second.
 * </p>
 * @return array an array containing all the entries from
 * array1 that are not present in any of the other arrays.
 * </p>
 */
function array_diff_ukey (array $array1, array $array2, array $_ = null, $key_compare_func) {}

/**
 * Computes the difference of arrays by using a callback function for data comparison
 * @link http://php.net/manual/en/function.array-udiff.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * The callback comparison function.
 * </p>
 * <p>
 * The user supplied callback function is used for comparison.
 * It must return an integer less than, equal to, or greater than zero if
 * the first argument is considered to be respectively less than, equal
 * to, or greater than the second.
 * </p>
 * @return array an array containing all the values of array1
 * that are not present in any of the other arguments.
 * </p>
 */
function array_udiff (array $array1, array $array2, array $_ = null, $data_compare_func) {}

/**
 * Computes the difference of arrays with additional index check
 * @link http://php.net/manual/en/function.array-diff-assoc.php
 * @param array1 array <p>
 * The array to compare from
 * </p>
 * @param array2 array <p>
 * An array to compare against
 * </p>
 * @param _ array[optional] 
 * @return array an array containing all the values from
 * array1 that are not present in any of the other arrays.
 * </p>
 */
function array_diff_assoc (array $array1, array $array2, array $_ = null) {}

/**
 * Computes the difference of arrays with additional index check, compares data by a callback function
 * @link http://php.net/manual/en/function.array-udiff-assoc.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * The callback comparison function.
 * </p>
 * <p>
 * The user supplied callback function is used for comparison.
 * It must return an integer less than, equal to, or greater than zero if
 * the first argument is considered to be respectively less than, equal
 * to, or greater than the second.
 * </p>
 * @return array array_udiff_assoc returns an array
 * containing all the values from array1
 * that are not present in any of the other arguments.
 * Note that the keys are used in the comparison unlike
 * array_diff and array_udiff.
 * The comparison of arrays' data is performed by using an user-supplied
 * callback. In this aspect the behaviour is opposite to the behaviour of
 * array_diff_assoc which uses internal function for
 * comparison.
 * </p>
 */
function array_udiff_assoc (array $array1, array $array2, array $_ = null, $data_compare_func) {}

/**
 * Computes the difference of arrays with additional index check which is performed by a user supplied callback function
 * @link http://php.net/manual/en/function.array-diff-uassoc.php
 * @param array1 array <p>
 * The array to compare from
 * </p>
 * @param array2 array <p>
 * An array to compare against
 * </p>
 * @param _ array[optional] 
 * @param key_compare_func callback <p>
 * callback function to use.
 * The callback function must return an integer less than, equal
 * to, or greater than zero if the first argument is considered to
 * be respectively less than, equal to, or greater than the second.
 * </p>
 * @return array an array containing all the entries from
 * array1 that are not present in any of the other arrays.
 * </p>
 */
function array_diff_uassoc (array $array1, array $array2, array $_ = null, $key_compare_func) {}

/**
 * Computes the difference of arrays with additional index check, compares data and indexes by a callback function
 * @link http://php.net/manual/en/function.array-udiff-uassoc.php
 * @param array1 array <p>
 * The first array.
 * </p>
 * @param array2 array <p>
 * The second array.
 * </p>
 * @param _ array[optional] 
 * @param data_compare_func callback <p>
 * The callback comparison function.
 * </p>
 * <p>
 * The user supplied callback function is used for comparison.
 * It must return an integer less than, equal to, or greater than zero if
 * the first argument is considered to be respectively less than, equal
 * to, or greater than the second.
 * </p>
 * <p>
 * The comparison of arrays' data is performed by using an user-supplied
 * callback : data_compare_func. In this aspect
 * the behaviour is opposite to the behaviour of
 * array_diff_assoc which uses internal function for
 * comparison.
 * </p>
 * @param key_compare_func callback <p>
 * The comparison of keys (indices) is done also by the callback function
 * key_compare_func. This behaviour is unlike what
 * array_udiff_assoc does, since the latter compares
 * the indices by using an internal function.
 * </p>
 * @return array an array containing all the values from
 * array1 that are not present in any of the other
 * arguments.
 * </p>
 */
function array_udiff_uassoc (array $array1, array $array2, array $_ = null, $data_compare_func, $key_compare_func) {}

/**
 * Calculate the sum of values in an array
 * @link http://php.net/manual/en/function.array-sum.php
 * @param array array <p>
 * The input array.
 * </p>
 * @return number the sum of values as an integer or float.
 * </p>
 */
function array_sum (array $array) {}

/**
 * Calculate the product of values in an array
 * @link http://php.net/manual/en/function.array-product.php
 * @param array array <p>
 * The array.
 * </p>
 * @return number the product as an integer or float.
 * </p>
 */
function array_product (array $array) {}

/**
 * Filters elements of an array using a callback function
 * @link http://php.net/manual/en/function.array-filter.php
 * @param input array <p>
 * The array to iterate over
 * </p>
 * @param callback callback[optional] <p>
 * The callback function to use
 * </p>
 * <p>
 * If no callback is supplied, all entries of
 * input equal to false (see
 * converting to
 * boolean) will be removed.
 * </p>
 * @return array the filtered array.
 * </p>
 */
function array_filter (array $input, $callback = null) {}

/**
 * Applies the callback to the elements of the given arrays
 * @link http://php.net/manual/en/function.array-map.php
 * @param callback callback <p>
 * Callback function to run for each element in each array.
 * </p>
 * @param arr1 array <p>
 * An array to run through the callback function.
 * </p>
 * @param _ array[optional] 
 * @return array an array containing all the elements of arr1
 * after applying the callback function to each one.
 * </p>
 */
function array_map ($callback, array $arr1, array $_ = null) {}

/**
 * Split an array into chunks
 * @link http://php.net/manual/en/function.array-chunk.php
 * @param input array <p>
 * The array to work on
 * </p>
 * @param size int <p>
 * The size of each chunk
 * </p>
 * @param preserve_keys bool[optional] <p>
 * When set to true keys will be preserved.
 * Default is false which will reindex the chunk numerically
 * </p>
 * @return array a multidimensional numerically indexed array, starting with zero,
 * with each dimension containing size elements.
 * </p>
 */
function array_chunk (array $input, $size, $preserve_keys = null) {}

/**
 * Creates an array by using one array for keys and another for its values
 * @link http://php.net/manual/en/function.array-combine.php
 * @param keys array <p>
 * Array of keys to be used. Illegal values for key will be
 * converted to string.
 * </p>
 * @param values array <p>
 * Array of values to be used
 * </p>
 * @return array the combined array, false if the number of elements
 * for each array isn't equal or if the arrays are empty.
 * </p>
 */
function array_combine (array $keys, array $values) {}

/**
 * Checks if the given key or index exists in the array
 * @link http://php.net/manual/en/function.array-key-exists.php
 * @param key mixed <p>
 * Value to check.
 * </p>
 * @param search array <p>
 * An array with keys to check.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function array_key_exists ($key, array $search) {}

/**
 * &Alias; <function>current</function>
 * @link http://php.net/manual/en/function.pos.php
 * @param arg
 */
function pos (&$arg) {}

/**
 * &Alias; <function>count</function>
 * @link http://php.net/manual/en/function.sizeof.php
 * @param var
 * @param mode[optional]
 */
function sizeof ($var, $mode) {}

/**
 * @param key
 * @param search
 */
function key_exists ($key, $search) {}

/**
 * Checks if assertion is &false;
 * @link http://php.net/manual/en/function.assert.php
 * @param assertion mixed <p>
 * The assertion.
 * </p>
 * @return bool false if the assertion is false, true otherwise.
 * </p>
 */
function assert ($assertion) {}

/**
 * Set/get the various assert flags
 * @link http://php.net/manual/en/function.assert-options.php
 * @param what int <p>
 * <table>
 * Assert Options
 * <tr valign="top">
 * <td>option</td>
 * <td>ini-parameter</td>
 * <td>default</td>
 * <td>description</td>
 * </tr>
 * <tr valign="top">
 * <td>ASSERT_ACTIVE</td>
 * <td>assert.active</td>
 * <td>1</td>
 * <td>enable assert evaluation</td>
 * </tr>
 * <tr valign="top">
 * <td>ASSERT_WARNING</td>
 * <td>assert.warning</td>
 * <td>1</td>
 * <td>issue a PHP warning for each failed assertion</td>
 * </tr>
 * <tr valign="top">
 * <td>ASSERT_BAIL</td>
 * <td>assert.bail</td>
 * <td>0</td>
 * <td>terminate execution on failed assertions</td>
 * </tr>
 * <tr valign="top">
 * <td>ASSERT_QUIET_EVAL</td>
 * <td>assert.quiet_eval</td>
 * <td>0</td>
 * <td>
 * disable error_reporting during assertion expression
 * evaluation
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>ASSERT_CALLBACK</td>
 * <td>assert.callback</td>
 * <td)<&null;)</td>
 * <td>user function to call on failed assertions</td>
 * </tr>
 * </table>
 * </p>
 * @param value mixed[optional] <p>
 * An optional new value for the option.
 * </p>
 * @return mixed the original setting of any option or false on errors.
 * </p>
 */
function assert_options ($what, $value = null) {}

/**
 * Compares two "PHP-standardized" version number strings
 * @link http://php.net/manual/en/function.version-compare.php
 * @param version1 string <p>
 * First version number.
 * </p>
 * @param version2 string <p>
 * Second version number.
 * </p>
 * @param operator string[optional] <p>
 * If you specify the third optional operator
 * argument, you can test for a particular relationship. The
 * possible operators are: &lt;,
 * lt, &lt;=,
 * le, &gt;,
 * gt, &gt;=,
 * ge, ==,
 * =, eq,
 * !=, &lt;&gt;,
 * ne respectively. 
 * </p>
 * <p>
 * This parameter is case-sensitive, so values should be lowercase.
 * </p>
 * @return mixed By default, version_compare returns
 * -1 if the first version is lower than the second, 
 * 0 if they are equal, and
 * 1 if the second is lower. 
 * </p>
 * <p>
 * When using the optional operator argument, the
 * function will return true if the relationship is the one specified
 * by the operator, false otherwise. 
 * </p>
 */
function version_compare ($version1, $version2, $operator = null) {}

/**
 * Convert a pathname and a project identifier to a System V IPC key
 * @link http://php.net/manual/en/function.ftok.php
 * @param pathname string <p>
 * Path to an accessible file.
 * </p>
 * @param proj string <p>
 * Project identifier. This must be a one character string.
 * </p>
 * @return int On success the return value will be the created key value, otherwise
 * -1 is returned.
 * </p>
 */
function ftok ($pathname, $proj) {}

/**
 * Perform the rot13 transform on a string
 * @link http://php.net/manual/en/function.str-rot13.php
 * @param str string <p>
 * The input string.
 * </p>
 * @return string the ROT13 version of the given string.
 * </p>
 */
function str_rot13 ($str) {}

/**
 * Retrieve list of registered filters
 * @link http://php.net/manual/en/function.stream-get-filters.php
 */
function stream_get_filters () {}

/**
 * Register a stream filter implemented as a PHP class derived from <literal>php_user_filter</literal>
 * @link http://php.net/manual/en/function.stream-filter-register.php
 * @param filtername
 * @param classname
 */
function stream_filter_register ($filtername, $classname) {}

/**
 * Return a bucket object from the brigade for operating on
 * @link http://php.net/manual/en/function.stream-bucket-make-writeable.php
 * @param brigade resource 
 * @return object 
 */
function stream_bucket_make_writeable ($brigade) {}

/**
 * Prepend bucket to brigade
 * @link http://php.net/manual/en/function.stream-bucket-prepend.php
 * @param brigade resource 
 * @param bucket resource 
 * @return void 
 */
function stream_bucket_prepend ($brigade, $bucket) {}

/**
 * Append bucket to brigade
 * @link http://php.net/manual/en/function.stream-bucket-append.php
 * @param brigade resource 
 * @param bucket resource 
 * @return void 
 */
function stream_bucket_append ($brigade, $bucket) {}

/**
 * Create a new bucket for use on the current stream
 * @link http://php.net/manual/en/function.stream-bucket-new.php
 * @param stream resource 
 * @param buffer string 
 * @return object 
 */
function stream_bucket_new ($stream, $buffer) {}

/**
 * Add URL rewriter values
 * @link http://php.net/manual/en/function.output-add-rewrite-var.php
 * @param name string <p>
 * The variable name.
 * </p>
 * @param value string <p>
 * The variable value.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function output_add_rewrite_var ($name, $value) {}

/**
 * Reset URL rewriter values
 * @link http://php.net/manual/en/function.output-reset-rewrite-vars.php
 * @return bool &return.success;
 * </p>
 */
function output_reset_rewrite_vars () {}

/**
 * Returns directory path used for temporary files
 * @link http://php.net/manual/en/function.sys-get-temp-dir.php
 * @return string the path of the temporary directory.
 * </p>
 */
function sys_get_temp_dir () {}

define ('CONNECTION_ABORTED', 1);
define ('CONNECTION_NORMAL', 0);
define ('CONNECTION_TIMEOUT', 2);
define ('INI_USER', 1);
define ('INI_PERDIR', 2);
define ('INI_SYSTEM', 4);
define ('INI_ALL', 7);
define ('PHP_URL_SCHEME', 0);
define ('PHP_URL_HOST', 1);
define ('PHP_URL_PORT', 2);
define ('PHP_URL_USER', 3);
define ('PHP_URL_PASS', 4);
define ('PHP_URL_PATH', 5);
define ('PHP_URL_QUERY', 6);
define ('PHP_URL_FRAGMENT', 7);
define ('M_E', 2.71828182846);
define ('M_LOG2E', 1.44269504089);
define ('M_LOG10E', 0.434294481903);
define ('M_LN2', 0.69314718056);
define ('M_LN10', 2.30258509299);
define ('M_PI', 3.14159265359);
define ('M_PI_2', 1.57079632679);
define ('M_PI_4', 0.785398163397);
define ('M_1_PI', 0.318309886184);
define ('M_2_PI', 0.636619772368);
define ('M_SQRTPI', 1.77245385091);
define ('M_2_SQRTPI', 1.1283791671);
define ('M_LNPI', 1.14472988585);
define ('M_EULER', 0.577215664902);
define ('M_SQRT2', 1.41421356237);
define ('M_SQRT1_2', 0.707106781187);
define ('M_SQRT3', 1.73205080757);
define ('INF', INF);
define ('NAN', NAN);
define ('INFO_GENERAL', 1);
define ('INFO_CREDITS', 2);
define ('INFO_CONFIGURATION', 4);
define ('INFO_MODULES', 8);
define ('INFO_ENVIRONMENT', 16);
define ('INFO_VARIABLES', 32);
define ('INFO_LICENSE', 64);
define ('INFO_ALL', -1);
define ('CREDITS_GROUP', 1);
define ('CREDITS_GENERAL', 2);
define ('CREDITS_SAPI', 4);
define ('CREDITS_MODULES', 8);
define ('CREDITS_DOCS', 16);
define ('CREDITS_FULLPAGE', 32);
define ('CREDITS_QA', 64);
define ('CREDITS_ALL', -1);
define ('HTML_SPECIALCHARS', 0);
define ('HTML_ENTITIES', 1);
define ('ENT_COMPAT', 2);
define ('ENT_QUOTES', 3);
define ('ENT_NOQUOTES', 0);
define ('STR_PAD_LEFT', 0);
define ('STR_PAD_RIGHT', 1);
define ('STR_PAD_BOTH', 2);
define ('PATHINFO_DIRNAME', 1);
define ('PATHINFO_BASENAME', 2);
define ('PATHINFO_EXTENSION', 4);

/**
 * Since PHP 5.2.0.
 * @link http://php.net/manual/en/filesystem.constants.php
 */
define ('PATHINFO_FILENAME', 8);
define ('CHAR_MAX', 127);
define ('LC_CTYPE', 0);
define ('LC_NUMERIC', 1);
define ('LC_TIME', 2);
define ('LC_COLLATE', 3);
define ('LC_MONETARY', 4);
define ('LC_ALL', 6);
define ('LC_MESSAGES', 5);
define ('SEEK_SET', 0);
define ('SEEK_CUR', 1);
define ('SEEK_END', 2);
define ('LOCK_SH', 1);
define ('LOCK_EX', 2);
define ('LOCK_UN', 3);
define ('LOCK_NB', 4);

/**
 * A connection with an external resource has been established.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_CONNECT', 2);

/**
 * Additional authorization is required to access the specified resource.
 * Typical issued with severity level of
 * STREAM_NOTIFY_SEVERITY_ERR.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_AUTH_REQUIRED', 3);

/**
 * Authorization has been completed (with or without success).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_AUTH_RESULT', 10);

/**
 * The mime-type of resource has been identified,
 * refer to message for a description of the
 * discovered type.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_MIME_TYPE_IS', 4);

/**
 * The size of the resource has been discovered.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_FILE_SIZE_IS', 5);

/**
 * The external resource has redirected the stream to an alternate
 * location. Refer to message.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_REDIRECTED', 6);

/**
 * Indicates current progress of the stream transfer in
 * bytes_transferred and possibly
 * bytes_max as well.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_PROGRESS', 7);

/**
 * A generic error occurred on the stream, consult
 * message and message_code
 * for details.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_FAILURE', 9);

/**
 * There is no more data available on the stream.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_COMPLETED', 8);

/**
 * A remote address required for this stream has been resolved, or the resolution
 * failed. See severity for an indication of which happened.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_RESOLVE', 1);

/**
 * Normal, non-error related, notification.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_SEVERITY_INFO', 0);

/**
 * Non critical error condition. Processing may continue.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_SEVERITY_WARN', 1);

/**
 * A critical error occurred. Processing cannot continue.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_NOTIFY_SEVERITY_ERR', 2);

/**
 * Used with stream_filter_append and
 * stream_filter_prepend to indicate
 * that the specified filter should only be applied when
 * reading
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_FILTER_READ', 1);

/**
 * Used with stream_filter_append and
 * stream_filter_prepend to indicate
 * that the specified filter should only be applied when
 * writing
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_FILTER_WRITE', 2);

/**
 * This constant is equivalent to 
 * STREAM_FILTER_READ | STREAM_FILTER_WRITE
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_FILTER_ALL', 3);

/**
 * Client socket opened with stream_socket_client
 * should remain persistent between page loads.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_CLIENT_PERSISTENT', 1);

/**
 * Open client socket asynchronously. This option must be used
 * together with the STREAM_CLIENT_CONNECT flag.
 * Used with stream_socket_client.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_CLIENT_ASYNC_CONNECT', 2);

/**
 * Open client socket connection. Client sockets should always
 * include this flag. Used with stream_socket_client.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_CLIENT_CONNECT', 4);
define ('STREAM_CRYPTO_METHOD_SSLv2_CLIENT', 0);
define ('STREAM_CRYPTO_METHOD_SSLv3_CLIENT', 1);
define ('STREAM_CRYPTO_METHOD_SSLv23_CLIENT', 2);
define ('STREAM_CRYPTO_METHOD_TLS_CLIENT', 3);
define ('STREAM_CRYPTO_METHOD_SSLv2_SERVER', 4);
define ('STREAM_CRYPTO_METHOD_SSLv3_SERVER', 5);
define ('STREAM_CRYPTO_METHOD_SSLv23_SERVER', 6);
define ('STREAM_CRYPTO_METHOD_TLS_SERVER', 7);

/**
 * Used with stream_socket_shutdown to disable
 * further receptions. Added in PHP 5.2.1.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SHUT_RD', 0);

/**
 * Used with stream_socket_shutdown to disable
 * further transmissions. Added in PHP 5.2.1.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SHUT_WR', 1);

/**
 * Used with stream_socket_shutdown to disable
 * further receptions and transmissions. Added in PHP 5.2.1.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SHUT_RDWR', 2);

/**
 * Internet Protocol Version 4 (IPv4).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_PF_INET', 2);

/**
 * Internet Protocol Version 6 (IPv6).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_PF_INET6', 10);

/**
 * Unix system internal protocols.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_PF_UNIX', 1);

/**
 * Provides a IP socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_IPPROTO_IP', 0);

/**
 * Provides a TCP socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_IPPROTO_TCP', 6);

/**
 * Provides a UDP socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_IPPROTO_UDP', 17);

/**
 * Provides a ICMP socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_IPPROTO_ICMP', 1);

/**
 * Provides a RAW socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_IPPROTO_RAW', 255);

/**
 * Provides sequenced, two-way byte streams with a transmission mechanism
 * for out-of-band data (TCP, for example).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SOCK_STREAM', 1);

/**
 * Provides datagrams, which are connectionless messages (UDP, for
 * example).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SOCK_DGRAM', 2);

/**
 * Provides a raw socket, which provides access to internal network
 * protocols and interfaces. Usually this type of socket is just available
 * to the root user.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SOCK_RAW', 3);

/**
 * Provides a sequenced packet stream socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SOCK_SEQPACKET', 5);

/**
 * Provides a RDM (Reliably-delivered messages) socket.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SOCK_RDM', 4);
define ('STREAM_PEEK', 2);
define ('STREAM_OOB', 1);

/**
 * Tells a stream created with stream_socket_server
 * to bind to the specified target. Server sockets should always include this flag.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SERVER_BIND', 4);

/**
 * Tells a stream created with stream_socket_server
 * and bound using the STREAM_SERVER_BIND flag to start
 * listening on the socket. Connection-orientated transports (such as TCP)
 * must use this flag, otherwise the server socket will not be enabled.
 * Using this flag for connect-less transports (such as UDP) is an error.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_SERVER_LISTEN', 8);

/**
 * Search for filename in
 * include_path (since PHP 5).
 * @link http://php.net/manual/en/filesystem.constants.php
 */
define ('FILE_USE_INCLUDE_PATH', 1);

/**
 * Strip EOL characters (since PHP 5).
 * @link http://php.net/manual/en/filesystem.constants.php
 */
define ('FILE_IGNORE_NEW_LINES', 2);

/**
 * Skip empty lines (since PHP 5).
 * @link http://php.net/manual/en/filesystem.constants.php
 */
define ('FILE_SKIP_EMPTY_LINES', 4);

/**
 * Append content to existing file.
 * @link http://php.net/manual/en/filesystem.constants.php
 */
define ('FILE_APPEND', 8);
define ('FILE_NO_DEFAULT_CONTEXT', 16);
define ('FNM_NOESCAPE', 2);
define ('FNM_PATHNAME', 1);
define ('FNM_PERIOD', 4);
define ('FNM_CASEFOLD', 16);

/**
 * Return Code indicating that the
 * userspace filter returned buckets in $out.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('PSFS_PASS_ON', 2);

/**
 * Return Code indicating that the
 * userspace filter did not return buckets in $out
 * (i.e. No data available).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('PSFS_FEED_ME', 1);

/**
 * Return Code indicating that the
 * userspace filter encountered an unrecoverable error
 * (i.e. Invalid data received).
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('PSFS_ERR_FATAL', 0);
define ('PSFS_FLAG_NORMAL', 0);
define ('PSFS_FLAG_FLUSH_INC', 1);
define ('PSFS_FLAG_FLUSH_CLOSE', 2);
define ('ABDAY_1', 131072);
define ('ABDAY_2', 131073);
define ('ABDAY_3', 131074);
define ('ABDAY_4', 131075);
define ('ABDAY_5', 131076);
define ('ABDAY_6', 131077);
define ('ABDAY_7', 131078);
define ('DAY_1', 131079);
define ('DAY_2', 131080);
define ('DAY_3', 131081);
define ('DAY_4', 131082);
define ('DAY_5', 131083);
define ('DAY_6', 131084);
define ('DAY_7', 131085);
define ('ABMON_1', 131086);
define ('ABMON_2', 131087);
define ('ABMON_3', 131088);
define ('ABMON_4', 131089);
define ('ABMON_5', 131090);
define ('ABMON_6', 131091);
define ('ABMON_7', 131092);
define ('ABMON_8', 131093);
define ('ABMON_9', 131094);
define ('ABMON_10', 131095);
define ('ABMON_11', 131096);
define ('ABMON_12', 131097);
define ('MON_1', 131098);
define ('MON_2', 131099);
define ('MON_3', 131100);
define ('MON_4', 131101);
define ('MON_5', 131102);
define ('MON_6', 131103);
define ('MON_7', 131104);
define ('MON_8', 131105);
define ('MON_9', 131106);
define ('MON_10', 131107);
define ('MON_11', 131108);
define ('MON_12', 131109);
define ('AM_STR', 131110);
define ('PM_STR', 131111);
define ('D_T_FMT', 131112);
define ('D_FMT', 131113);
define ('T_FMT', 131114);
define ('T_FMT_AMPM', 131115);
define ('ERA', 131116);
define ('ERA_D_T_FMT', 131120);
define ('ERA_D_FMT', 131118);
define ('ERA_T_FMT', 131121);
define ('ALT_DIGITS', 131119);
define ('CRNCYSTR', 262159);
define ('RADIXCHAR', 65536);
define ('THOUSEP', 65537);
define ('YESEXPR', 327680);
define ('NOEXPR', 327681);
define ('CODESET', 14);
define ('CRYPT_SALT_LENGTH', 12);
define ('CRYPT_STD_DES', 1);
define ('CRYPT_EXT_DES', 0);
define ('CRYPT_MD5', 1);
define ('CRYPT_BLOWFISH', 0);
define ('DIRECTORY_SEPARATOR', "/");
define ('PATH_SEPARATOR', ":");
define ('GLOB_BRACE', 1024);
define ('GLOB_MARK', 2);
define ('GLOB_NOSORT', 4);
define ('GLOB_NOCHECK', 16);
define ('GLOB_NOESCAPE', 64);
define ('GLOB_ERR', 1);
define ('GLOB_ONLYDIR', 8192);
define ('GLOB_AVAILABLE_FLAGS', 9303);
define ('EXTR_OVERWRITE', 0);
define ('EXTR_SKIP', 1);
define ('EXTR_PREFIX_SAME', 2);
define ('EXTR_PREFIX_ALL', 3);
define ('EXTR_PREFIX_INVALID', 4);
define ('EXTR_PREFIX_IF_EXISTS', 5);
define ('EXTR_IF_EXISTS', 6);
define ('EXTR_REFS', 256);

/**
 * SORT_ASC is used with
 * array_multisort to sort in ascending order.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_ASC', 4);

/**
 * SORT_DESC is used with
 * array_multisort to sort in descending order.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_DESC', 3);

/**
 * SORT_REGULAR is used to compare items normally.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_REGULAR', 0);

/**
 * SORT_NUMERIC is used to compare items numerically.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_NUMERIC', 1);

/**
 * SORT_STRING is used to compare items as strings.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_STRING', 2);

/**
 * SORT_LOCALE_STRING is used to compare items as
 * strings, based on the current locale. Added in PHP 4.4.0 and 5.0.2.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('SORT_LOCALE_STRING', 5);

/**
 * CASE_LOWER is used with
 * array_change_key_case and is used to convert array
 * keys to lower case. This is also the default case for
 * array_change_key_case.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('CASE_LOWER', 0);

/**
 * CASE_UPPER is used with
 * array_change_key_case and is used to convert array
 * keys to upper case.
 * @link http://php.net/manual/en/array.constants.php
 */
define ('CASE_UPPER', 1);
define ('COUNT_NORMAL', 0);
define ('COUNT_RECURSIVE', 1);
define ('ASSERT_ACTIVE', 1);
define ('ASSERT_CALLBACK', 2);
define ('ASSERT_BAIL', 3);
define ('ASSERT_WARNING', 4);
define ('ASSERT_QUIET_EVAL', 5);

/**
 * Flag indicating if the stream
 * used the include path.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_USE_PATH', 1);
define ('STREAM_IGNORE_URL', 2);
define ('STREAM_ENFORCE_SAFE_MODE', 4);

/**
 * Flag indicating if the wrapper
 * is responsible for raising errors using trigger_error 
 * during opening of the stream. If this flag is not set, you
 * should not raise any errors.
 * @link http://php.net/manual/en/stream.constants.php
 */
define ('STREAM_REPORT_ERRORS', 8);

/**
 * This flag is useful when your extension really must be able to randomly
 * seek around in a stream. Some streams may not be seekable in their
 * native form, so this flag asks the streams API to check to see if the
 * stream does support seeking. If it does not, it will copy the stream
 * into temporary storage (which may be a temporary file or a memory
 * stream) which does support seeking.
 * Please note that this flag is not useful when you want to seek the
 * stream and write to it, because the stream you are accessing might
 * not be bound to the actual resource you requested.
 * If the requested resource is network based, this flag will cause the
 * opener to block until the whole contents have been downloaded.
 * @link http://php.net/manual/en/internals2.ze1.streams.constants.php
 */
define ('STREAM_MUST_SEEK', 16);
define ('STREAM_URL_STAT_LINK', 1);
define ('STREAM_URL_STAT_QUIET', 2);
define ('STREAM_MKDIR_RECURSIVE', 1);
define ('STREAM_IS_URL', 1);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_GIF', 1);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_JPEG', 2);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_PNG', 3);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_SWF', 4);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_PSD', 5);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_BMP', 6);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_TIFF_II', 7);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_TIFF_MM', 8);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_JPC', 9);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_JP2', 10);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_JPX', 11);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_JB2', 12);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_SWC', 13);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_IFF', 14);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_WBMP', 15);
define ('IMAGETYPE_JPEG2000', 9);

/**
 * Image type constant used by the
 * image_type_to_mime_type and
 * image_type_to_extension functions.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMAGETYPE_XBM', 16);
define ('DNS_A', 1);
define ('DNS_NS', 2);
define ('DNS_CNAME', 16);
define ('DNS_SOA', 32);
define ('DNS_PTR', 2048);
define ('DNS_HINFO', 4096);
define ('DNS_MX', 16384);
define ('DNS_TXT', 32768);
define ('DNS_SRV', 33554432);
define ('DNS_NAPTR', 67108864);
define ('DNS_AAAA', 134217728);
define ('DNS_A6', 16777216);
define ('DNS_ANY', 268435456);
define ('DNS_ALL', 251713587);

// End of standard v.5.2.4-2ubuntu5.2
?>
