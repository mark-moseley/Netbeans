--TEST--
Heredoc with double quotes and wrong prefix
--FILE--
<?php
$test = "foo";
$var = prefix<<<"MYLABEL"
test: $test
MYLABEL;
echo $var;
?>
--EXPECTF--
Parse error: syntax error, unexpected T_START_HEREDOC in %sheredoc_013.php on line %d
