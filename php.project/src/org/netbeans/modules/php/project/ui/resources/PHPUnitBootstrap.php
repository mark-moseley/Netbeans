<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "../Licenses/license-${project.license}.txt">

/**
 * To regenerate this file, simply delete it and run any PHPUnit test.
 * @author ${user}
 */

// DO NOT REMOVE "%INCLUDE_PATH%" FROM TEMPLATE!
// TODO: check include path
ini_set('include_path', ini_get('include_path')%INCLUDE_PATH%);

// put your code here
?>
