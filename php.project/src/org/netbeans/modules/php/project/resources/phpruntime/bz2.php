<?php

// Start of bz2 v.

/**
 * Opens a bzip2 compressed file
 * @link http://php.net/manual/en/function.bzopen.php
 * @param filename string <p>
 * The name of the file to open.
 * </p>
 * @param mode string <p>
 * Similar to the fopen function ('r' for read, 
 * 'w' for write, etc.).
 * </p>
 * @return resource If the open fails, bzopen returns false, otherwise 
 * it returns a pointer to the newly opened file.
 * </p>
 */
function bzopen ($filename, $mode) {}

/**
 * Binary safe bzip2 file read
 * @link http://php.net/manual/en/function.bzread.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @param length int[optional] <p>
 * If not specified, bzread will read 1024 
 * (uncompressed) bytes at a time.
 * </p>
 * @return string the uncompressed data, or false on error.
 * </p>
 */
function bzread ($bz, $length = null) {}

/**
 * Binary safe bzip2 file write
 * @link http://php.net/manual/en/function.bzwrite.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @param data string <p>
 * The written data.
 * </p>
 * @param length int[optional] <p>
 * If supplied, writing will stop after length 
 * (uncompressed) bytes have been written or the end of 
 * data is reached, whichever comes first.
 * </p>
 * @return int the number of bytes written, or false on error.
 * </p>
 */
function bzwrite ($bz, $data, $length = null) {}

/**
 * Force a write of all buffered data
 * @link http://php.net/manual/en/function.bzflush.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @return int &return.success;
 * </p>
 */
function bzflush ($bz) {}

/**
 * Close a bzip2 file
 * @link http://php.net/manual/en/function.bzclose.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @return int &return.success;
 * </p>
 */
function bzclose ($bz) {}

/**
 * Returns a bzip2 error number
 * @link http://php.net/manual/en/function.bzerrno.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @return int the error number as an integer.
 * </p>
 */
function bzerrno ($bz) {}

/**
 * Returns a bzip2 error string
 * @link http://php.net/manual/en/function.bzerrstr.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @return string a string containing the error message.
 * </p>
 */
function bzerrstr ($bz) {}

/**
 * Returns the bzip2 error number and error string in an array
 * @link http://php.net/manual/en/function.bzerror.php
 * @param bz resource <p>
 * The file pointer. It must be valid and must point to a file 
 * successfully opened by bzopen.
 * </p>
 * @return array an associative array, with the error code in the 
 * errno entry, and the error message in the
 * errstr entry.
 * </p>
 */
function bzerror ($bz) {}

/**
 * Compress a string into bzip2 encoded data
 * @link http://php.net/manual/en/function.bzcompress.php
 * @param source string <p>
 * The string to compress.
 * </p>
 * @param blocksize int[optional] <p>
 * Specifies the blocksize used during compression and should be a number 
 * from 1 to 9 with 9 giving the best compression, but using more 
 * resources to do so. blocksize defaults to 4.
 * </p>
 * @param workfactor int[optional] <p>
 * Controls how the compression phase behaves when presented with worst
 * case, highly repetitive, input data. The value can be between 0 and
 * 250 with 0 being a special case and 30 being the default value. 
 * </p>
 * <p>
 * Regardless of the workfactor, the generated 
 * output is the same.
 * </p>
 * @return mixed The compressed string or number of error in case of error.
 * </p>
 */
function bzcompress ($source, $blocksize = null, $workfactor = null) {}

/**
 * Decompresses bzip2 encoded data
 * @link http://php.net/manual/en/function.bzdecompress.php
 * @param source string <p>
 * The string to decompress.
 * </p>
 * @param small int[optional] <p>
 * If true, an alternative decompression algorithm will be used which
 * uses less memory (the maximum memory requirement drops to around 2300K) 
 * but works at roughly half the speed.
 * </p>
 * <p>
 * See the bzip2 documentation for more 
 * information about this feature.
 * </p>
 * @return mixed The decompressed string or number of error in case of error.
 * </p>
 */
function bzdecompress ($source, $small = null) {}

// End of bz2 v.
?>
