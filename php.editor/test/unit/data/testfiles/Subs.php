<?php
/**********************************************************************************
* Subs.php                                                                        *
***********************************************************************************
* SMF: Simple Machines Forum                                                      *
* Open-Source Project Inspired by Zef Hemel (zef@zefhemel.com)                    *
* =============================================================================== *
* Software Version:           SMF 1.1                                             *
* Software by:                Simple Machines (http://www.simplemachines.org)     *
* Copyright 2006 by:          Simple Machines LLC (http://www.simplemachines.org) *
*           2001-2006 by:     Lewis Media (http://www.lewismedia.com)             *
* Support, News, Updates at:  http://www.simplemachines.org                       *
***********************************************************************************
* This program is free software; you may redistribute it and/or modify it under   *
* the terms of the provided license as published by Simple Machines LLC.          *
*                                                                                 *
* This program is distributed in the hope that it is and will be useful, but      *
* WITHOUT ANY WARRANTIES; without even any implied warranty of MERCHANTABILITY    *
* or FITNESS FOR A PARTICULAR PURPOSE.                                            *
*                                                                                 *
* See the "license.txt" file for details of the Simple Machines license.          *
* The latest version can always be found at http://www.simplemachines.org.        *
**********************************************************************************/
if (!defined('SMF'))
	die('Hacking attempt...');

/*	This file has all the main functions in it that relate to, well,
	everything.  It provides all of the following functions:

	resource db_query(string database_query, string __FILE__, int __LINE__)
		- should always be used in place of mysql_query.
		- executes a query string, and implements needed error checking.
		- always use the magic constants __FILE__ and __LINE__.
		- returns a MySQL result resource, to be freed with mysql_free_result.

	int db_affected_rows()
		- should always be used in place of db_insert_id.
		- returns the number of affected rows by the most recently executed
		  query.
		- handles the current connection so the forum with other connections
		  active at the same time.

	int db_insert_id()
		- should always be used in place of mysql_insert_id.
		- returns the most recently generated auto_increment column.
		- handles the current connection so the forum with other connections
		  active at the same time.

	void updateStats(string statistic, string condition = '1')
		- statistic can be 'member', 'message', 'topic', 'calendar', or
		  'postgroups'.
		- parameter1 and parameter2 are optional, and are used to update only
		  those stats that need updating.
		- the 'member' statistic updates the latest member, the total member
		  count, and the number of unapproved members.
		- 'member' also only counts approved members when approval is on, but
		  is much more efficient with it off.
		- updating 'message' changes the total number of messages, and the
		  highest message id by ID_MSG - which can be parameters 1 and 2,
		  respectively.
		- 'topic' updates the total number of topics, or if parameter1 is true
		  simply increments them.
		- the 'calendar' statistic updates the cache of the calendar
		  information for a day before and after today.
		- the 'postgroups' case updates those members who match condition's
		  post-based membergroups in the database (restricted by parameter1).

	void updateMemberData(int ID_MEMBER, array data)
		- updates the columns in the members table.
		- ID_MEMBER is either an int or an array of ints to be updated.
		- data is an associative array of the columns to be updated and their
		  respective values.
		- any string values updated should be quoted and slashed.
		- the value of any column can be '+' or '-', which mean 'increment'
		  and decrement, respectively.
		- if the member's post number is updated, updates their post groups.
		- this function should be used whenever member data needs to be
		  updated in place of an UPDATE query.

	void updateSettings(array changeArray, use_update = false)
		- updates both the settings table and $modSettings array.
		- all of changeArray's indexes and values are assumed to have escaped
		  apostrophes (')!
		- if a variable is already set to what you want to change it to, that
		  variable will be skipped over; it would be unnecessary to reset.
		- if use_update is true, UPDATEs will be used instead of REPALCE.
		- when use_update is true, the value can be true or false to increment
		  or decrement it, respectively.

	string constructPageIndex(string base_url, int &start, int max_value,
			int num_per_page, bool compact_start = false)
		- builds the page list, e.g. 1 ... 6 7 [8] 9 10 ... 15.
		- compact_start caused it to use "url.page" instead of
		  "url;start=page".
		- handles any wireless settings (adding special things to URLs.)
		- very importantly, cleans up the start value passed, and forces it to
		  be a multiple of num_per_page.
		- also checks that start is not more than max_value.
		- base_url should be the URL without any start parameter on it.
		- uses the compactTopicPagesEnable and compactTopicPagesContiguous
		  settings to decide how to display the menu.
		- an example is available near the function definition.

	string comma_format(float number, int override_decimal_count = false)
		- formats a number to display in the style of the admins' choosing.
		- uses the format of number_format to decide how to format the number.
		- for example, it might display "1 234,50".
		- caches the formatting data from the setting for optimization.

	string timeformat(int time, bool show_today = true)
		- returns a pretty formated version of time based on the user's format
		  in $user_info['time_format'].
		- applies any necessary time offsets to the timestamp.
		- if todayMod is set and show_today was not not specified or true, an
		  alternate format string is used to show the date with something to
		  show it is "today" or "yesterday".
		- performs localization (more than just strftime would do alone.)

	string un_htmlspecialchars(string text)
		- removes the base entities (&lt;, &quot;, etc.) from text.
		- should be used instead of html_entity_decode for PHP version
		  compatibility reasons.
		- additionally converts &nbsp; and &#039;.
		- returns the string without entities.

	string shorten_subject(string regular_subject, int length)
		- shortens a subject so that it is either shorter than length, or that
		  length plus an ellipsis.
		- respects internationalization characters and entities as one character.
		- avoids trailing entities.
		- returns the shortened string.

	int forum_time(bool use_user_offset = true)
		- returns the current time with offsets.
		- always applies the offset in the time_offset setting.
		- if use_user_offset is true, applies the user's offset as well.
		- returns seconds since the unix epoch.

	array permute(array input)
		- calculates all the possible permutations (orders) of array.
		- should not be called on huge arrays (bugger than like 10 elements.)
		- returns an array containing each permutation.

	string doUBBC(string message, bool enableSmileys = true)
		- passes through parse_bbc(message, enableSmileys).
		- available for older implementations.

	string parse_bbc(string message, bool smileys = true, string cache_id = '')
		- this very hefty function parses bbc in message.
		- only parses bbc tags which are not disabled in disabledBBC.
		- also handles basic HTML, if enablePostHTML is on.
		- caches the from/to replace regular expressions so as not to reload
		  them every time a string is parsed.
		- only parses smileys if smileys is true.
		- does nothing if the enableBBC setting is off.
		- applies the fixLongWords magic if the setting is set to on.
		- uses the cache_id as a unique identifier to facilitate any caching
		  it may do.
		- returns the modified message.

	void parsesmileys(string &message)
		- the smiley parsing function which makes pretty faces appear :).
		- if custom smiley sets are turned off by smiley_enable, the default
		  set of smileys will be used.
		- these are specifically not parsed in code tags [url=mailto:Dad@blah.com]
		- caches the smileys from the database or array in memory.
		- doesn't return anything, but rather modifies message directly.

	string highlight_php_code(string code)
		- Uses PHP's highlight_code() to highlight PHP syntax
		- does special handling to keep the tabs in the code available.
		- used to parse PHP code from inside [code] and [php] tags.
		- returns the code with highlighted HTML.

	void writeLog(bool force = false)
		// !!!

	void redirectexit(string setLocation = '', bool use_refresh = false)
		// !!!

	void obExit(bool do_header = true, bool do_footer = do_header)
		// !!!

	void adminIndex($area)
		// !!!

	int logAction($action, $extra = array())
		// !!!

	void trackStats($stats = array())
		- caches statistics changes, and flushes them if you pass nothing.
		- if '+' is used as a value, it will be incremented.
		- does not actually commit the changes until the end of the page view.
		- depends on the trackStats setting.

	void spamProtection(string error_type)
		- attempts to protect from spammed messages and the like.
		- takes a $txt index. (not an actual string.)
		- depends on the spamWaitTime setting.

	array url_image_size(string url)
		- uses getimagesize() to determine the size of a file.
		- attempts to connect to the server first so it won't time out.
		- returns false on failure, otherwise the output of getimagesize().

	void determineTopicClass(array &topic_context)
		// !!!

	void setupThemeContext()
		// !!!

	void template_rawdata()
		// !!!

	void template_header()
		// !!!

	void theme_copyright(bool get_it = false)
		// !!!

	void template_footer()
		// !!!

	void db_debug_junk()
		// !!!

	void getAttachmentFilename(string filename, int ID_ATTACH, bool new = true)
		// !!!

	string host_from_ip(string ip_address)
		// !!!

	string create_button(string filename, string alt, string label, bool custom = '')
		// !!!
*/

// Do a query.  Takes care of errors too.
function db_query($db_string, $file, $line)
{
	global $db_cache, $db_count, $db_connection, $db_show_debug, $modSettings;

	// One more query....
	$db_count = !isset($db_count) ? 1 : $db_count + 1;

	// Debugging.
	if (isset($db_show_debug) && $db_show_debug === true)
	{
		// Initialize $db_cache if not already initialized.
		if (!isset($db_cache))
			$db_cache = array();

		if (!empty($_SESSION['debug_redirect']))
		{
			$db_cache = array_merge($_SESSION['debug_redirect'], $db_cache);
			$db_count = count($db_cache) + 1;
			$_SESSION['debug_redirect'] = array();
		}

		$db_cache[$db_count]['q'] = $db_string;
		$db_cache[$db_count]['f'] = $file;
		$db_cache[$db_count]['l'] = $line;
		$st = microtime();
	}

	// First, we clean strings out of the query, reduce whitespace, lowercase, and trim - so we can check it over.
	if (empty($modSettings['disableQueryCheck']))
	{
		$clean = '';
		$old_pos = 0;
		$pos = -1;
		while (true)
		{
			$pos = strpos($db_string, '\'', $pos + 1);
			if ($pos === false)
				break;
			$clean .= substr($db_string, $old_pos, $pos - $old_pos);

			while (true)
			{
				$pos1 = strpos($db_string, '\'', $pos + 1);
				$pos2 = strpos($db_string, '\\', $pos + 1);
				if ($pos1 === false)
					break;
				elseif ($pos2 == false || $pos2 > $pos1)
				{
					$pos = $pos1;
					break;
				}

				$pos = $pos2 + 1;
			}
			$clean .= '%s';

			$old_pos = $pos + 1;
		}
		$clean .= substr($db_string, $old_pos);
		$clean = trim(strtolower(preg_replace(array('~\s+~s', '~/\*!40001 SQL_NO_CACHE \*/~', '~/\*!40000 USE INDEX \([A-Za-z\_]+?\) \*/~'), array(' ', '', ''), $clean)));

		// We don't use UNION in SMF, at least so far.  But it's useful for injections.
		if (strpos($clean, 'union') !== false && preg_match('~(^|[^a-z])union($|[^[a-z])~s', $clean) != 0)
			$fail = true;
		// Comments?  We don't use comments in our queries, we leave 'em outside!
		elseif (strpos($clean, '/*') > 2 || strpos($clean, '--') !== false || strpos($clean, ';') !== false)
			$fail = true;
		// Trying to change passwords, slow us down, or something?
		elseif (strpos($clean, 'set password') !== false && preg_match('~(^|[^a-z])set password($|[^[a-z])~s', $clean) != 0)
			$fail = true;
		elseif (strpos($clean, 'benchmark') !== false && preg_match('~(^|[^a-z])benchmark($|[^[a-z])~s', $clean) != 0)
			$fail = true;
		// Sub selects?  We don't use those either.
		elseif (preg_match('~\([^)]*?select~s', $clean) != 0)
			$fail = true;

		if (!empty($fail))
		{
			log_error('Hacking attempt...' . "\n" . $db_string, $file, $line);
			fatal_error('Hacking attempt...', false);
		}
	}

	$ret = mysql_query($db_string, $db_connection);
	if ($ret === false && $file !== false)
		$ret = db_error($db_string, $file, $line);

	// Debugging.
	if (isset($db_show_debug) && $db_show_debug === true)
		$db_cache[$db_count]['t'] = array_sum(explode(' ', microtime())) - array_sum(explode(' ', $st));

	return $ret;
}

function db_affected_rows()
{
	global $db_connection;

	return mysql_affected_rows($db_connection);
}

function db_affected_rows2()
{
	global $db_connection;

	return mysql_affected_rows($db_connection);
}

function db_insert_id()
{
	global $db_connection;

	return mysql_insert_id($db_connection);
}

// Update some basic statistics...
function updateStats3($type, $parameter1 = null, $parameter2 = null)
{
	global $db_prefix, $sourcedir, $modSettings;

	switch ($type)
	{
	case 'member':
		$changes = array(
			'memberlist_updated' => time(),
		);

		// Are we using registration approval?
		if (!empty($modSettings['registration_method']) && $modSettings['registration_method'] == 2)
		{
			// Update the latest activated member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members
				WHERE is_activated = 1", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest activated member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Update the amount of members awaiting approval - ignoring COPPA accounts, as you can't approve them until you get permission.
			$result = db_query("
				SELECT COUNT(*)
				FROM {$db_prefix}members
				WHERE is_activated IN (3, 4)", __FILE__, __LINE__);
			list ($changes['unapprovedMembers']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}
		// If $parameter1 is a number, it's the new ID_MEMBER and #2 is the real name for a new registration.
		elseif ($parameter1 !== null && $parameter1 !== false)
		{
			$changes['latestMember'] = $parameter1;
			$changes['latestRealName'] = $parameter2;

			updateSettings(array('totalMembers' => true), true);
		}
		// If $parameter1 is false, and approval is off, we need change nothing.
		elseif ($parameter1 !== false)
		{
			// Update the latest member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}

		updateSettings($changes);
		break;

	case 'message':
		if ($parameter1 === true && $parameter2 !== null)
			updateSettings(array('totalMessages' => true, 'maxMsgID' => $parameter2), true);
		else
		{
			// SUM and MAX on a smaller table is better for InnoDB tables.
			$result = db_query("
				SELECT SUM(numPosts) AS totalMessages, MAX(ID_LAST_MSG) AS maxMsgID
				FROM {$db_prefix}boards", __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array(
				'totalMessages' => $row['totalMessages'],
				'maxMsgID' => $row['maxMsgID'] === null ? 0 : $row['maxMsgID']
			));
		}
		break;

	case 'subject':
		// Remove the previous subject (if any).
		db_query("
			DELETE FROM {$db_prefix}log_search_subjects
			WHERE ID_TOPIC = " . (int) $parameter1, __FILE__, __LINE__);

		// Insert the new subject.
		if ($parameter2 !== null)
		{
			$parameter1 = (int) $parameter1;
			$parameter2 = text2words($parameter2);

			$inserts = array();
			foreach ($parameter2 as $word)
				$inserts[] = "'$word', $parameter1";

			if (!empty($inserts))
				db_query("
					INSERT INTO {$db_prefix}log_search_subjects
						(word, ID_TOPIC)
					VALUES (" . implode('),
						(', array_unique($inserts)) . ")", __FILE__, __LINE__);
		}
		break;

	case 'topic':
		if ($parameter1 === true)
			updateSettings(array('totalTopics' => true), true);
		else
		{
			// Get the number of topics - a SUM is better for InnoDB tables.
			// We also ignore the recycle bin here because there will probably be a bunch of one-post topics there.
			$result = db_query("
				SELECT SUM(numTopics) AS totalTopics
				FROM {$db_prefix}boards" . (!empty($modSettings['recycle_enable']) && $modSettings['recycle_board'] > 0 ? "
				WHERE ID_BOARD != $modSettings[recycle_board]" : ''), __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array('totalTopics' => $row['totalTopics']));
		}
		break;

	case 'calendar':
		require_once($sourcedir . '/Calendar.php');

		// Calculate the YYYY-MM-DD of the lowest and highest days.
		$low_date = strftime('%Y-%m-%d', forum_time(false) - 24 * 3600);
		$high_date = strftime('%Y-%m-%d', forum_time(false) + $modSettings['cal_days_for_index'] * 24 * 3600);

		$holidays = calendarHolidayArray($low_date, $high_date);
		$bday = calendarBirthdayArray($low_date, $high_date);
		$events = calendarEventArray($low_date, $high_date, false);

		// Cache the results in the settings.
		updateSettings(array(
			'cal_today_updated' => strftime('%Y%m%d', forum_time(false)),
			'cal_today_holiday' => addslashes(serialize($holidays)),
			'cal_today_birthday' => addslashes(serialize($bday)),
			'cal_today_event' => addslashes(serialize($events))
		));
		break;

	case 'postgroups':
		// Parameter two is the updated columns: we should check to see if we base groups off any of these.
		if ($parameter2 !== null && !in_array('posts', $parameter2))
			return;

		if (($postgroups = cache_get_data('updateStats:postgroups', 360)) == null)
		{
			// Fetch the postgroups!
			$request = db_query("
				SELECT ID_GROUP, minPosts
				FROM {$db_prefix}membergroups
				WHERE minPosts != -1", __FILE__, __LINE__);
			$postgroups = array();
			while ($row = mysql_fetch_assoc($request))
				$postgroups[$row['ID_GROUP']] = $row['minPosts'];
			mysql_free_result($request);

			// Sort them this way because if it's done with MySQL it causes a filesort :(.
			arsort($postgroups);

			cache_put_data('updateStats:postgroups', $postgroups, 360);
		}

		// Oh great, they've screwed their post groups.
		if (empty($postgroups))
			return;

		// Set all membergroups from most posts to least posts.
		$conditions = '';
		foreach ($postgroups as $id => $minPosts)
		{
			$conditions .= '
					WHEN posts >= ' . $minPosts . (!empty($lastMin) ? ' AND posts <= ' . $lastMin : '') . ' THEN ' . $id;
			$lastMin = $minPosts;
		}

		// A big fat CASE WHEN... END is faster than a zillion UPDATE's ;).
		db_query("
			UPDATE {$db_prefix}members
			SET ID_POST_GROUP = CASE$conditions
					ELSE 0
				END" . ($parameter1 != null ? "
			WHERE $parameter1" : ''), __FILE__, __LINE__);
		break;

		default:
			trigger_error('updateStats(): Invalid statistic type \'' . $type . '\'', E_USER_NOTICE);
	}
}

// Update some basic statistics...
function updateStats($type, $parameter1 = null, $parameter2 = null)
{
	global $db_prefix, $sourcedir, $modSettings;

	switch ($type)
	{
	case 'member':
		$changes = array(
			'memberlist_updated' => time(),
		);

		// Are we using registration approval?
		if (!empty($modSettings['registration_method']) && $modSettings['registration_method'] == 2)
		{
			// Update the latest activated member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members
				WHERE is_activated = 1", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest activated member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Update the amount of members awaiting approval - ignoring COPPA accounts, as you can't approve them until you get permission.
			$result = db_query("
				SELECT COUNT(*)
				FROM {$db_prefix}members
				WHERE is_activated IN (3, 4)", __FILE__, __LINE__);
			list ($changes['unapprovedMembers']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}
		// If $parameter1 is a number, it's the new ID_MEMBER and #2 is the real name for a new registration.
		elseif ($parameter1 !== null && $parameter1 !== false)
		{
			$changes['latestMember'] = $parameter1;
			$changes['latestRealName'] = $parameter2;

			updateSettings(array('totalMembers' => true), true);
		}
		// If $parameter1 is false, and approval is off, we need change nothing.
		elseif ($parameter1 !== false)
		{
			// Update the latest member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}

		updateSettings($changes);
		break;

	case 'message':
		if ($parameter1 === true && $parameter2 !== null)
			updateSettings(array('totalMessages' => true, 'maxMsgID' => $parameter2), true);
		else
		{
			// SUM and MAX on a smaller table is better for InnoDB tables.
			$result = db_query("
				SELECT SUM(numPosts) AS totalMessages, MAX(ID_LAST_MSG) AS maxMsgID
				FROM {$db_prefix}boards", __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array(
				'totalMessages' => $row['totalMessages'],
				'maxMsgID' => $row['maxMsgID'] === null ? 0 : $row['maxMsgID']
			));
		}
		break;

	case 'subject':
		// Remove the previous subject (if any).
		db_query("
			DELETE FROM {$db_prefix}log_search_subjects
			WHERE ID_TOPIC = " . (int) $parameter1, __FILE__, __LINE__);

		// Insert the new subject.
		if ($parameter2 !== null)
		{
			$parameter1 = (int) $parameter1;
			$parameter2 = text2words($parameter2);

			$inserts = array();
			foreach ($parameter2 as $word)
				$inserts[] = "'$word', $parameter1";

			if (!empty($inserts))
				db_query("
					INSERT INTO {$db_prefix}log_search_subjects
						(word, ID_TOPIC)
					VALUES (" . implode('),
						(', array_unique($inserts)) . ")", __FILE__, __LINE__);
		}
		break;

	case 'topic':
		if ($parameter1 === true)
			updateSettings(array('totalTopics' => true), true);
		else
		{
			// Get the number of topics - a SUM is better for InnoDB tables.
			// We also ignore the recycle bin here because there will probably be a bunch of one-post topics there.
			$result = db_query("
				SELECT SUM(numTopics) AS totalTopics
				FROM {$db_prefix}boards" . (!empty($modSettings['recycle_enable']) && $modSettings['recycle_board'] > 0 ? "
				WHERE ID_BOARD != $modSettings[recycle_board]" : ''), __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array('totalTopics' => $row['totalTopics']));
		}
		break;

	case 'calendar':
		require_once($sourcedir . '/Calendar.php');

		// Calculate the YYYY-MM-DD of the lowest and highest days.
		$low_date = strftime('%Y-%m-%d', forum_time(false) - 24 * 3600);
		$high_date = strftime('%Y-%m-%d', forum_time(false) + $modSettings['cal_days_for_index'] * 24 * 3600);

		$holidays = calendarHolidayArray($low_date, $high_date);
		$bday = calendarBirthdayArray($low_date, $high_date);
		$events = calendarEventArray($low_date, $high_date, false);

		// Cache the results in the settings.
		updateSettings(array(
			'cal_today_updated' => strftime('%Y%m%d', forum_time(false)),
			'cal_today_holiday' => addslashes(serialize($holidays)),
			'cal_today_birthday' => addslashes(serialize($bday)),
			'cal_today_event' => addslashes(serialize($events))
		));
		break;

	case 'postgroups':
		// Parameter two is the updated columns: we should check to see if we base groups off any of these.
		if ($parameter2 !== null && !in_array('posts', $parameter2))
			return;

		if (($postgroups = cache_get_data('updateStats:postgroups', 360)) == null)
		{
			// Fetch the postgroups!
			$request = db_query("
				SELECT ID_GROUP, minPosts
				FROM {$db_prefix}membergroups
				WHERE minPosts != -1", __FILE__, __LINE__);
			$postgroups = array();
			while ($row = mysql_fetch_assoc($request))
				$postgroups[$row['ID_GROUP']] = $row['minPosts'];
			mysql_free_result($request);

			// Sort them this way because if it's done with MySQL it causes a filesort :(.
			arsort($postgroups);

			cache_put_data('updateStats:postgroups', $postgroups, 360);
		}

		// Oh great, they've screwed their post groups.
		if (empty($postgroups))
			return;

		// Set all membergroups from most posts to least posts.
		$conditions = '';
		foreach ($postgroups as $id => $minPosts)
		{
			$conditions .= '
					WHEN posts >= ' . $minPosts . (!empty($lastMin) ? ' AND posts <= ' . $lastMin : '') . ' THEN ' . $id;
			$lastMin = $minPosts;
		}

		// A big fat CASE WHEN... END is faster than a zillion UPDATE's ;).
		db_query("
			UPDATE {$db_prefix}members
			SET ID_POST_GROUP = CASE$conditions
					ELSE 0
				END" . ($parameter1 != null ? "
			WHERE $parameter1" : ''), __FILE__, __LINE__);
		break;

		default:
			trigger_error('updateStats(): Invalid statistic type \'' . $type . '\'', E_USER_NOTICE);
	}
}


// Update some basic statistics...
function updateStats2($type, $parameter1 = null, $parameter2 = null)
{
	global $db_prefix, $sourcedir, $modSettings;

	switch ($type)
	{
	case 'member':
		$changes = array(
			'memberlist_updated' => time(),
		);

		// Are we using registration approval?
		if (!empty($modSettings['registration_method']) && $modSettings['registration_method'] == 2)
		{
			// Update the latest activated member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members
				WHERE is_activated = 1", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest activated member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Update the amount of members awaiting approval - ignoring COPPA accounts, as you can't approve them until you get permission.
			$result = db_query("
				SELECT COUNT(*)
				FROM {$db_prefix}members
				WHERE is_activated IN (3, 4)", __FILE__, __LINE__);
			list ($changes['unapprovedMembers']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}
		// If $parameter1 is a number, it's the new ID_MEMBER and #2 is the real name for a new registration.
		elseif ($parameter1 !== null && $parameter1 !== false)
		{
			$changes['latestMember'] = $parameter1;
			$changes['latestRealName'] = $parameter2;

			updateSettings(array('totalMembers' => true), true);
		}
		// If $parameter1 is false, and approval is off, we need change nothing.
		elseif ($parameter1 !== false)
		{
			// Update the latest member (highest ID_MEMBER) and count.
			$result = db_query("
				SELECT COUNT(*), MAX(ID_MEMBER)
				FROM {$db_prefix}members", __FILE__, __LINE__);
			list ($changes['totalMembers'], $changes['latestMember']) = mysql_fetch_row($result);
			mysql_free_result($result);

			// Get the latest member's display name.
			$result = db_query("
				SELECT realName
				FROM {$db_prefix}members
				WHERE ID_MEMBER = " . (int) $changes['latestMember'] . "
				LIMIT 1", __FILE__, __LINE__);
			list ($changes['latestRealName']) = mysql_fetch_row($result);
			mysql_free_result($result);
		}

		updateSettings($changes);
		break;

	case 'message':
		if ($parameter1 === true && $parameter2 !== null)
			updateSettings(array('totalMessages' => true, 'maxMsgID' => $parameter2), true);
		else
		{
			// SUM and MAX on a smaller table is better for InnoDB tables.
			$result = db_query("
				SELECT SUM(numPosts) AS totalMessages, MAX(ID_LAST_MSG) AS maxMsgID
				FROM {$db_prefix}boards", __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array(
				'totalMessages' => $row['totalMessages'],
				'maxMsgID' => $row['maxMsgID'] === null ? 0 : $row['maxMsgID']
			));
		}
		break;

	case 'subject':
		// Remove the previous subject (if any).
		db_query("
			DELETE FROM {$db_prefix}log_search_subjects
			WHERE ID_TOPIC = " . (int) $parameter1, __FILE__, __LINE__);

		// Insert the new subject.
		if ($parameter2 !== null)
		{
			$parameter1 = (int) $parameter1;
			$parameter2 = text2words($parameter2);

			$inserts = array();
			foreach ($parameter2 as $word)
				$inserts[] = "'$word', $parameter1";

			if (!empty($inserts))
				db_query("
					INSERT INTO {$db_prefix}log_search_subjects
						(word, ID_TOPIC)
					VALUES (" . implode('),
						(', array_unique($inserts)) . ")", __FILE__, __LINE__);
		}
		break;

	case 'topic':
		if ($parameter1 === true)
			updateSettings(array('totalTopics' => true), true);
		else
		{
			// Get the number of topics - a SUM is better for InnoDB tables.
			// We also ignore the recycle bin here because there will probably be a bunch of one-post topics there.
			$result = db_query("
				SELECT SUM(numTopics) AS totalTopics
				FROM {$db_prefix}boards" . (!empty($modSettings['recycle_enable']) && $modSettings['recycle_board'] > 0 ? "
				WHERE ID_BOARD != $modSettings[recycle_board]" : ''), __FILE__, __LINE__);
			$row = mysql_fetch_assoc($result);
			mysql_free_result($result);

			updateSettings(array('totalTopics' => $row['totalTopics']));
		}
		break;

	case 'calendar':
		require_once($sourcedir . '/Calendar.php');

		// Calculate the YYYY-MM-DD of the lowest and highest days.
		$low_date = strftime('%Y-%m-%d', forum_time(false) - 24 * 3600);
		$high_date = strftime('%Y-%m-%d', forum_time(false) + $modSettings['cal_days_for_index'] * 24 * 3600);

		$holidays = calendarHolidayArray($low_date, $high_date);
		$bday = calendarBirthdayArray($low_date, $high_date);
		$events = calendarEventArray($low_date, $high_date, false);

		// Cache the results in the settings.
		updateSettings(array(
			'cal_today_updated' => strftime('%Y%m%d', forum_time(false)),
			'cal_today_holiday' => addslashes(serialize($holidays)),
			'cal_today_birthday' => addslashes(serialize($bday)),
			'cal_today_event' => addslashes(serialize($events))
		));
		break;

	case 'postgroups':
		// Parameter two is the updated columns: we should check to see if we base groups off any of these.
		if ($parameter2 !== null && !in_array('posts', $parameter2))
			return;

		if (($postgroups = cache_get_data('updateStats:postgroups', 360)) == null)
		{
			// Fetch the postgroups!
			$request = db_query("
				SELECT ID_GROUP, minPosts
				FROM {$db_prefix}membergroups
				WHERE minPosts != -1", __FILE__, __LINE__);
			$postgroups = array();
			while ($row = mysql_fetch_assoc($request))
				$postgroups[$row['ID_GROUP']] = $row['minPosts'];
			mysql_free_result($request);

			// Sort them this way because if it's done with MySQL it causes a filesort :(.
			arsort($postgroups);

			cache_put_data('updateStats:postgroups', $postgroups, 360);
		}

		// Oh great, they've screwed their post groups.
		if (empty($postgroups))
			return;

		// Set all membergroups from most posts to least posts.
		$conditions = '';
		foreach ($postgroups as $id => $minPosts)
		{
			$conditions .= '
					WHEN posts >= ' . $minPosts . (!empty($lastMin) ? ' AND posts <= ' . $lastMin : '') . ' THEN ' . $id;
			$lastMin = $minPosts;
		}

		// A big fat CASE WHEN... END is faster than a zillion UPDATE's ;).
		db_query("
			UPDATE {$db_prefix}members
			SET ID_POST_GROUP = CASE$conditions
					ELSE 0
				END" . ($parameter1 != null ? "
			WHERE $parameter1" : ''), __FILE__, __LINE__);
		break;

		default:
			trigger_error('updateStats(): Invalid statistic type \'' . $type . '\'', E_USER_NOTICE);
	}
}

// Assumes the data has been slashed.
function updateMemberData($members, $data)
{
	global $db_prefix, $modSettings, $ID_MEMBER, $user_info;

	if (is_array($members))
		$condition = 'ID_MEMBER IN (' . implode(', ', $members) . ')
		LIMIT ' . count($members);
	elseif ($members === null)
		$condition = '1';
	else
		$condition = 'ID_MEMBER = ' . $members . '
		LIMIT 1';

	if (isset($modSettings['integrate_change_member_data']) && function_exists($modSettings['integrate_change_member_data']))
	{
		// Only a few member variables are really interesting for integration.
		$integration_vars = array(
			'memberName',
			'realName',
			'emailAddress',
			'ID_GROUP',
			'gender',
			'birthdate',
			'websiteTitle',
			'websiteUrl',
			'location',
			'hideEmail',
			'timeFormat',
			'timeOffset',
			'avatar',
			'lngfile',
		);
		$vars_to_integrate = array_intersect($integration_vars, array_keys($data));

		// Only proceed if there are any variables left to call the integration function.
		if (count($vars_to_integrate) != 0)
		{
			// Fetch a list of memberNames if necessary
			if ((!is_array($members) && $members === $ID_MEMBER) || (is_array($members) && count($members) == 1 && in_array($ID_MEMBER, $members)))
				$memberNames = array($user_info['username']);
			else
			{
				$memberNames = array();
				$request = db_query("
					SELECT memberName
					FROM {$db_prefix}members
					WHERE $condition", __FILE__, __LINE__);
				while ($row = mysql_fetch_assoc($request))
					$memberNames[] = $row['memberName'];
				mysql_free_result($request);
			}

			if (!empty($memberNames))
				foreach ($vars_to_integrate as $var)
					call_user_func($modSettings['integrate_change_member_data'], $memberNames, $var, stripslashes($data[$var]));
		}
	}

	foreach ($data as $var => $val)
	{
		if ($val === '+')
			$data[$var] = $var . ' + 1';
		elseif ($val === '-')
			$data[$var] = $var . ' - 1';
	}

	// Ensure posts, instantMessages, and unreadMessages never go below 0.
	if (isset($data['posts']))
		$data['posts'] = 'IF(' . $data['posts'] . ' < 0, 0, ' . $data['posts'] . ')';
	if (isset($data['instantMessages']))
		$data['instantMessages'] = 'IF(' . $data['instantMessages'] . ' < 0, 0, ' . $data['instantMessages'] . ')';
	if (isset($data['unreadMessages']))
		$data['unreadMessages'] = 'IF(' . $data['unreadMessages'] . ' < 0, 0, ' . $data['unreadMessages'] . ')';

	$setString = '';
	foreach ($data as $var => $val)
	{
		$setString .= "
			$var = $val,";
	}

	db_query("
		UPDATE {$db_prefix}members
		SET" . substr($setString, 0, -1) . '
		WHERE ' . $condition, __FILE__, __LINE__);

	updateStats('postgroups', $condition, array_keys($data));

	// Clear any caching?
	if (!empty($modSettings['cache_enable']) && $modSettings['cache_enable'] >= 2 && !empty($members))
	{
		if (!is_array($members))
			$members = array($members);

		foreach ($members as $member)
		{
			if ($modSettings['cache_enable'] == 3)
			{
				cache_put_data('member_data-profile-' . $member, null, 120);
				cache_put_data('member_data-normal-' . $member, null, 120);
				cache_put_data('member_data-minimal-' . $member, null, 120);
			}
			cache_put_data('user_settings-' . $member, null, 60);
		}
	}
}

// Updates the settings table as well as $modSettings... only does one at a time if $update is true.
// All input variables and values are assumed to have escaped apostrophes(')!
function updateSettings($changeArray, $update = false)
{
	global $db_prefix, $modSettings;

	if (empty($changeArray) || !is_array($changeArray))
		return;

	// In some cases, this may be better and faster, but for large sets we don't want so many UPDATEs.
	if ($update)
	{
		foreach ($changeArray as $variable => $value)
		{
			db_query("
				UPDATE {$db_prefix}settings
				SET value = " . ($value === true ? 'value + 1' : ($value === false ? 'value - 1' : "'$value'")) . "
				WHERE variable = '$variable'
				LIMIT 1", __FILE__, __LINE__);
			$modSettings[$variable] = $value === true ? $modSettings[$variable] + 1 : ($value === false ? $modSettings[$variable] - 1 : stripslashes($value));
		}

		// Clean out the cache and make sure the cobwebs are gone too.
		cache_put_data('modSettings', null, 90);

		return;
	}

	$replaceArray = array();
	foreach ($changeArray as $variable => $value)
	{
		// Don't bother if it's already like that ;).
		if (isset($modSettings[$variable]) && $modSettings[$variable] == stripslashes($value))
			continue;
		// If the variable isn't set, but would only be set to nothing'ness, then don't bother setting it.
		elseif (!isset($modSettings[$variable]) && empty($value))
			continue;

		$replaceArray[] = "(SUBSTRING('$variable', 1, 255), SUBSTRING('$value', 1, 65534))";
		$modSettings[$variable] = stripslashes($value);
	}

	if (empty($replaceArray))
		return;

	db_query("
		REPLACE INTO {$db_prefix}settings
			(variable, value)
		VALUES " . implode(',
			', $replaceArray), __FILE__, __LINE__);

	// Kill the cache - it needs redoing now, but we won't bother ourselves with that here.
	cache_put_data('modSettings', null, 90);
}

// Constructs a page list.
// $pageindex = constructPageIndex($scripturl . '?board=' . $board, $_REQUEST['start'], $num_messages, $maxindex, true);
function constructPageIndex($base_url, &$start, $max_value, $num_per_page, $flexible_start = false)
{
	global $modSettings;

	// Save whether $start was less than 0 or not.
	$start_invalid = $start < 0;

	// Make sure $start is a proper variable - not less than 0.
	if ($start_invalid)
		$start = 0;
	// Not greater than the upper bound.
	elseif ($start >= $max_value)
		$start = max(0, (int) $max_value - (((int) $max_value % (int) $num_per_page) == 0 ? $num_per_page : ((int) $max_value % (int) $num_per_page)));
	// And it has to be a multiple of $num_per_page!
	else
		$start = max(0, (int) $start - ((int) $start % (int) $num_per_page));

	// Wireless will need the protocol on the URL somewhere.
	if (WIRELESS)
		$base_url .= ';' . WIRELESS_PROTOCOL;

	$base_link = '<a class="navPages" href="' . ($flexible_start ? $base_url : strtr($base_url, array('%' => '%%')) . ';start=%d') . '">%s</a> ';

	// Compact pages is off or on?
	if (empty($modSettings['compactTopicPagesEnable']))
	{
		// Show the left arrow.
		$pageindex = $start == 0 ? ' ' : sprintf($base_link, $start - $num_per_page, '&#171;');

		// Show all the pages.
		$display_page = 1;
		for ($counter = 0; $counter < $max_value; $counter += $num_per_page)
			$pageindex .= $start == $counter && !$start_invalid ? '<b>' . $display_page++ . '</b> ' : sprintf($base_link, $counter, $display_page++);

		// Show the right arrow.
		$display_page = ($start + $num_per_page) > $max_value ? $max_value : ($start + $num_per_page);
		if ($start != $counter - $max_value && !$start_invalid)
			$pageindex .= $display_page > $counter - $num_per_page ? ' ' : sprintf($base_link, $display_page, '&#187;');
	}
	else
	{
		// If they didn't enter an odd value, pretend they did.
		$PageContiguous = (int) ($modSettings['compactTopicPagesContiguous'] - ($modSettings['compactTopicPagesContiguous'] % 2)) / 2;

		// Show the first page. (>1< ... 6 7 [8] 9 10 ... 15)
		if ($start > $num_per_page * $PageContiguous)
			$pageindex = sprintf($base_link, 0, '1');
		else
			$pageindex = '';

		// Show the ... after the first page.  (1 >...< 6 7 [8] 9 10 ... 15)
		if ($start > $num_per_page * ($PageContiguous + 1))
			$pageindex .= '<b> ... </b>';

		// Show the pages before the current one. (1 ... >6 7< [8] 9 10 ... 15)
		for ($nCont = $PageContiguous; $nCont >= 1; $nCont--)
			if ($start >= $num_per_page * $nCont)
			{
				$tmpStart = $start - $num_per_page * $nCont;
				$pageindex.= sprintf($base_link, $tmpStart, $tmpStart / $num_per_page + 1);
			}

		// Show the current page. (1 ... 6 7 >[8]< 9 10 ... 15)
		if (!$start_invalid)
			$pageindex .= '[<b>' . ($start / $num_per_page + 1) . '</b>] ';
		else
			$pageindex .= sprintf($base_link, $start, $start / $num_per_page + 1);

		// Show the pages after the current one... (1 ... 6 7 [8] >9 10< ... 15)
		$tmpMaxPages = (int) (($max_value - 1) / $num_per_page) * $num_per_page;
		for ($nCont = 1; $nCont <= $PageContiguous; $nCont++)
			if ($start + $num_per_page * $nCont <= $tmpMaxPages)
			{
				$tmpStart = $start + $num_per_page * $nCont;
				$pageindex .= sprintf($base_link, $tmpStart, $tmpStart / $num_per_page + 1);
			}

		// Show the '...' part near the end. (1 ... 6 7 [8] 9 10 >...< 15)
		if ($start + $num_per_page * ($PageContiguous + 1) < $tmpMaxPages)
			$pageindex .= '<b> ... </b>';

		// Show the last number in the list. (1 ... 6 7 [8] 9 10 ... >15<)
		if ($start + $num_per_page * $PageContiguous < $tmpMaxPages)
			$pageindex .= sprintf($base_link, $tmpMaxPages, $tmpMaxPages / $num_per_page + 1);
	}

	return $pageindex;
}

// Formats a number to display in the style of the admin's choosing.
function comma_format($number, $override_decimal_count = false)
{
	global $modSettings;
	static $thousands_separator = null, $decimal_separator = null, $decimal_count = null;

	// !!! Should, perhaps, this just be handled in the language files, and not a mod setting?
	// (French uses 1 234,00 for example... what about a multilingual forum?)

	// Cache these values...
	if ($decimal_separator === null)
	{
		// Not set for whatever reason?
		if (empty($modSettings['number_format']) || preg_match('~^1([^\d]*)?234([^\d]*)(0*?)$~', $modSettings['number_format'], $matches) != 1)
			return $number;

		// Cache these each load...
		$thousands_separator = $matches[1];
		$decimal_separator = $matches[2];
		$decimal_count = strlen($matches[3]);
	}

	// Format the string with our friend, number_format.
	return number_format($number, is_float($number) ? ($override_decimal_count === false ? $decimal_count : $override_decimal_count) : 0, $decimal_separator, $thousands_separator);
}

// Format a time to make it look purdy.
function timeformat($logTime, $show_today = true)
{
	global $user_info, $txt, $db_prefix, $modSettings, $func;

	// Offset the time.
	$time = $logTime + ($user_info['time_offset'] + $modSettings['time_offset']) * 3600;

	// We can't have a negative date (on Windows, at least.)
	if ($time < 0)
		$time = 0;

	// Today and Yesterday?
	if ($modSettings['todayMod'] >= 1 && $show_today === true)
	{
		// Get the current time.
		$nowtime = forum_time();

		$then = @getdate($time);
		$now = @getdate($nowtime);

		// Try to make something of a time format string...
		$s = strpos($user_info['time_format'], '%S') === false ? '' : ':%S';
		if (strpos($user_info['time_format'], '%H') === false && strpos($user_info['time_format'], '%T') === false)
			$today_fmt = '%I:%M' . $s . ' %p';
		else
			$today_fmt = '%H:%M' . $s;

		// Same day of the year, same year.... Today!
		if ($then['yday'] == $now['yday'] && $then['year'] == $now['year'])
			return $txt['smf10'] . timeformat($logTime, $today_fmt);

		// Day-of-year is one less and same year, or it's the first of the year and that's the last of the year...
		if ($modSettings['todayMod'] == '2' && (($then['yday'] == $now['yday'] - 1 && $then['year'] == $now['year']) || ($now['yday'] == 0 && $then['year'] == $now['year'] - 1) && $then['mon'] == 12 && $then['mday'] == 31))
			return $txt['smf10b'] . timeformat($logTime, $today_fmt);
	}

	$str = !is_bool($show_today) ? $show_today : $user_info['time_format'];

	if (setlocale(LC_TIME, $txt['lang_locale']))
	{
		foreach (array('%a', '%A', '%b', '%B') as $token)
			if (strpos($str, $token) !== false)
				$str = str_replace($token, $func['ucwords'](strftime($token, $time)), $str);
	}
	else
	{
		// Do-it-yourself time localization.  Fun.
		foreach (array('%a' => 'days_short', '%A' => 'days', '%b' => 'months_short', '%B' => 'months') as $token => $text_label)
			if (strpos($str, $token) !== false)
				$str = str_replace($token, $txt[$text_label][(int) strftime($token === '%a' || $token === '%A' ? '%w' : '%m', $time)], $str);
		if (strpos($str, '%p'))
			$str = str_replace('%p', (strftime('%H', $time) < 12 ? 'am' : 'pm'), $str);
	}

	// Format any other characters..
	return strftime($str, $time);
}

// Removes special entities from strings.  Compatibility...
function un_htmlspecialchars($string)
{
	return strtr($string, array_flip(get_html_translation_table(HTML_SPECIALCHARS, ENT_QUOTES)) + array('&#039;' => '\'', '&nbsp;' => ' '));
}

if (!function_exists('stripos'))
{
	function stripos($haystack, $needle, $offset = 0)
	{
		return strpos(strtolower($haystack), strtolower($needle), $offset);
	}
}

// Shorten a subject + internationalization concerns.
function shorten_subject($subject, $len)
{
	global $func;

	// It was already short enough!
	if ($func['strlen']($subject) <= $len)
		return $subject;

	// Shorten it by the length it was too long, and strip off junk from the end.
	return $func['substr']($subject, 0, $len) . '...';
}

// The current time with offset.
function forum_time($use_user_offset = true, $timestamp = null)
{
	global $user_info, $modSettings;

	if ($timestamp === null)
		$timestamp = time();
	elseif ($timestamp == 0)
		return 0;

	return $timestamp + ($modSettings['time_offset'] + ($use_user_offset ? $user_info['time_offset'] : 0)) * 3600;
}

// This gets all possible permutations of an array.
function permute($array)
{
	$orders = array($array);

	$n = count($array);
	$p = range(0, $n);
	for ($i = 1; $i < $n; null)
	{
		$p[$i]--;
		$j = $i % 2 != 0 ? $p[$i] : 0;

		$temp = $array[$i];
		$array[$i] = $array[$j];
		$array[$j] = $temp;

		for ($i = 1; $p[$i] == 0; $i++)
			$p[$i] = 1;

		$orders[] = $array;
	}

	return $orders;
}

// For old stuff still using doUBBC()...
function doUBBC($message, $enableSmileys = true)
{
	return parse_bbc($message, $enableSmileys);
}

// Parse bulletin board code in a string, as well as smileys optionally.
function parse_bbc($message, $smileys = true, $cache_id = '')
{
	global $txt, $scripturl, $context, $modSettings, $user_info;
	static $bbc_codes = array(), $itemcodes = array(), $no_autolink_tags = array();
	static $disabled;

	// Never show smileys for wireless clients.  More bytes, can't see it anyway :P.
	if (WIRELESS)
		$smileys = false;
	elseif ($smileys !== null && ($smileys == '1' || $smileys == '0'))
		$smileys = (bool) $smileys;

	if (empty($modSettings['enableBBC']) && $message !== false)
	{
		if ($smileys === true)
			parsesmileys($message);

		return $message;
	}

	// Just in case it wasn't determined yet whether UTF-8 is enabled.
	if (!isset($context['utf8']))
		$context['utf8'] = (empty($modSettings['global_character_set']) ? $txt['lang_character_set'] : $modSettings['global_character_set']) === 'UTF-8';

	// Sift out the bbc for a performance improvement.
	if (empty($bbc_codes) || $message === false)
	{
		if (!empty($modSettings['disabledBBC']))
		{
			$temp = explode(',', strtolower($modSettings['disabledBBC']));

			foreach ($temp as $tag)
				$disabled[trim($tag)] = true;
		}

		if (empty($modSettings['enableEmbeddedFlash']))
			$disabled['flash'] = true;

		/* The following bbc are formatted as an array, with keys as follows:

			tag: the tag's name - should be lowercase!

			type: one of...
				- (missing): [tag]parsed content[/tag]
				- unparsed_equals: [tag=xyz]parsed content[/tag]
				- parsed_equals: [tag=parsed data]parsed content[/tag]
				- unparsed_content: [tag]unparsed content[/tag]
				- closed: [tag], [tag/], [tag /]
				- unparsed_commas: [tag=1,2,3]parsed content[/tag]
				- unparsed_commas_content: [tag=1,2,3]unparsed content[/tag]
				- unparsed_equals_content: [tag=...]unparsed content[/tag]

			parameters: an optional array of parameters, for the form
			  [tag abc=123]content[/tag].  The array is an associative array
			  where the keys are the parameter names, and the values are an
			  array which may contain the following:
				- match: a regular expression to validate and match the value.
				- quoted: true if the value should be quoted.
				- validate: callback to evaluate on the data, which is $data.
				- value: a string in which to replace $1 with the data.
				  either it or validate may be used, not both.
				- optional: true if the parameter is optional.

			test: a regular expression to test immediately after the tag's
			  '=', ' ' or ']'.  Typically, should have a \] at the end.
			  Optional.

			content: only available for unparsed_content, closed,
			  unparsed_commas_content, and unparsed_equals_content.
			  $1 is replaced with the content of  the tag.  Parameters
			  are repalced in the form {param}.  For unparsed_commas_content,
			  $2, $3, ..., $n are replaced.

			before: only when content is not used, to go before any
			  content.  For unparsed_equals, $1 is replaced with the value.
			  For unparsed_commas, $1, $2, ..., $n are replaced.

			after: similar to before in every way, except that it is used
			  when the tag is closed.

			disabled_content: used in place of content when the tag is
			  disabled.  For closed, default is '', otherwise it is '$1' if
			  block_level is false, '<div>$1</div>' elsewise.

			disabled_before: used in place of before when disabled.  Defaults
			  to '<div>' if block_level, '' if not.

			disabled_after: used in place of after when disabled.  Defaults
			  to '</div>' if block_level, '' if not.

			block_level: set to true the tag is a "block level" tag, similar
			  to HTML.  Block level tags cannot be nested inside tags that are
			  not block level, and will not be implicitly closed as easily.
			  One break following a block level tag may also be removed.

			trim: if set, and 'inside' whitespace after the begin tag will be
			  removed.  If set to 'outside', whitespace after the end tag will
			  meet the same fate.

			validate: except when type is missing or 'closed', a callback to
			  validate the data as $data.  Depending on the tag's type, $data
			  may be a string or an array of strings (corresponding to the
			  replacement.)

			quoted: when type is 'unparsed_equals' or 'parsed_equals' only,
			  may be not set, 'optional', or 'required' corresponding to if
			  the content may be quoted.  This allows the parser to read
			  [tag="abc]def[esdf]"] properly.

			require_parents: an array of tag names, or not set.  If set, the
			  enclosing tag *must* be one of the listed tags, or parsing won't
			  occur.

			require_children: similar to require_parents, if set children
			  won't be parsed if they are not in the list.

			disallow_children: similar to, but very different from,
			  require_children, if it is set the listed tags will not be
			  parsed inside the tag.
		*/

		$codes = array(
			array(
				'tag' => 'abbr',
				'type' => 'unparsed_equals',
				'before' => '<abbr title="$1">',
				'after' => '</abbr>',
				'quoted' => 'optional',
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'acronym',
				'type' => 'unparsed_equals',
				'before' => '<acronym title="$1">',
				'after' => '</acronym>',
				'quoted' => 'optional',
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'anchor',
				'type' => 'unparsed_equals',
				'test' => '[#]?([A-Za-z][A-Za-z0-9_\-]*)\]',
				'before' => '<span id="post_$1" />',
				'after' => '',
			),
			array(
				'tag' => 'b',
				'before' => '<b>',
				'after' => '</b>',
			),
			array(
				'tag' => 'black',
				'before' => '<span style="color: black;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'blue',
				'before' => '<span style="color: blue;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'br',
				'type' => 'closed',
				'content' => '<br />',
			),
			array(
				'tag' => 'code',
				'type' => 'unparsed_content',
				'content' => '<div class="codeheader">' . $txt['smf238'] . ':</div><div class="code">' . ($context['browser']['is_gecko'] ? '<pre style="margin-top: 0; display: inline;">$1</pre>' : '$1') . '</div>',
				// !!! Maybe this can be simplified?
				'validate' => isset($disabled['code']) ? null : create_function('&$tag, &$data, $disabled', '
					global $context;

					if (!isset($disabled[\'code\']))
					{
						$php_parts = preg_split(\'~(&lt;\?php|\?&gt;)~\', $data, -1, PREG_SPLIT_DELIM_CAPTURE);

						for ($php_i = 0, $php_n = count($php_parts); $php_i < $php_n; $php_i++)
						{
							// Do PHP code coloring?
							if ($php_parts[$php_i] != \'&lt;?php\')
								continue;

							$php_string = \'\';
							while ($php_i + 1 < count($php_parts) && $php_parts[$php_i] != \'?&gt;\')
							{
								$php_string .= $php_parts[$php_i];
								$php_parts[$php_i++] = \'\';
							}
							$php_parts[$php_i] = highlight_php_code($php_string . $php_parts[$php_i]);
						}

						// Fix the PHP code stuff...
						$data = str_replace("<pre style=\"display: inline;\">\t</pre>", "\t", implode(\'\', $php_parts));

						// Older browsers are annoying, aren\'t they?
						if ($context[\'browser\'][\'is_ie4\'] || $context[\'browser\'][\'is_ie5\'] || $context[\'browser\'][\'is_ie5.5\'])
							$data = str_replace("\t", "<pre style=\"display: inline;\">\t</pre>", $data);
						elseif (!$context[\'browser\'][\'is_gecko\'])
							$data = str_replace("\t", "<span style=\"white-space: pre;\">\t</span>", $data);
					}'),
				'block_level' => true,
			),
			array(
				'tag' => 'code',
				'type' => 'unparsed_equals_content',
				'content' => '<div class="codeheader">' . $txt['smf238'] . ': ($2)</div><div class="code">' . ($context['browser']['is_gecko'] ? '<pre style="margin-top: 0; display: inline;">$1</pre>' : '$1') . '</div>',
				// !!! Maybe this can be simplified?
				'validate' => isset($disabled['code']) ? null : create_function('&$tag, &$data, $disabled', '
					global $context;

					if (!isset($disabled[\'code\']))
					{
						$php_parts = preg_split(\'~(&lt;\?php|\?&gt;)~\', $data[0], -1, PREG_SPLIT_DELIM_CAPTURE);

						for ($php_i = 0, $php_n = count($php_parts); $php_i < $php_n; $php_i++)
						{
							// Do PHP code coloring?
							if ($php_parts[$php_i] != \'&lt;?php\')
								continue;

							$php_string = \'\';
							while ($php_i + 1 < count($php_parts) && $php_parts[$php_i] != \'?&gt;\')
							{
								$php_string .= $php_parts[$php_i];
								$php_parts[$php_i++] = \'\';
							}
							$php_parts[$php_i] = highlight_php_code($php_string . $php_parts[$php_i]);
						}

						// Fix the PHP code stuff...
						$data[0] = str_replace("<pre style=\"display: inline;\">\t</pre>", "\t", implode(\'\', $php_parts));

						// Older browsers are annoying, aren\'t they?
						if ($context[\'browser\'][\'is_ie4\'] || $context[\'browser\'][\'is_ie5\'] || $context[\'browser\'][\'is_ie5.5\'])
							$data = str_replace("\t", "<pre style=\"display: inline;\">\t</pre>", $data);
						elseif (!$context[\'browser\'][\'is_gecko\'])
							$data = str_replace("\t", "<span style=\"white-space: pre;\">\t</span>", $data);
					}'),
				'block_level' => true,
			),
			array(
				'tag' => 'center',
				'before' => '<div align="center">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'color',
				'type' => 'unparsed_equals',
				'test' => '(#[\da-fA-F]{3}|#[\da-fA-F]{6}|[A-Za-z]{1,12})\]',
				'before' => '<span style="color: $1;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'email',
				'type' => 'unparsed_content',
				'content' => '<a href="mailto:$1">$1</a>',
				// !!! Should this respect guest_hideContacts?
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
			),
			array(
				'tag' => 'email',
				'type' => 'unparsed_equals',
				'before' => '<a href="mailto:$1">',
				'after' => '</a>',
				// !!! Should this respect guest_hideContacts?
				'disallow_children' => array('email', 'ftp', 'url', 'iurl'),
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'ftp',
				'type' => 'unparsed_content',
				'content' => '<a href="$1" target="_blank">$1</a>',
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
			),
			array(
				'tag' => 'ftp',
				'type' => 'unparsed_equals',
				'before' => '<a href="$1" target="_blank">',
				'after' => '</a>',
				'disallow_children' => array('email', 'ftp', 'url', 'iurl'),
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'font',
				'type' => 'unparsed_equals',
				'test' => '[A-Za-z0-9_,\-\s]+?\]',
				'before' => '<span style="font-family: $1;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'flash',
				'type' => 'unparsed_commas_content',
				'test' => '\d+,\d+\]',
				'content' => ($context['browser']['is_ie'] && !$context['browser']['is_mac_ie'] ? '<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="$2" height="$3"><param name="movie" value="$1" /><param name="play" value="true" /><param name="loop" value="true" /><param name="quality" value="high" /><param name="AllowScriptAccess" value="never" /><embed src="$1" width="$2" height="$3" play="true" loop="true" quality="high" AllowScriptAccess="never" /><noembed><a href="$1" target="_blank">$1</a></noembed></object>' : '<embed type="application/x-shockwave-flash" src="$1" width="$2" height="$3" play="true" loop="true" quality="high" AllowScriptAccess="never" /><noembed><a href="$1" target="_blank">$1</a></noembed>'),
				'validate' => create_function('&$tag, &$data, $disabled', '
					if (isset($disabled[\'url\']))
						$tag[\'content\'] = \'$1\';'),
				'disabled_content' => '<a href="$1" target="_blank">$1</a>',
			),
			array(
				'tag' => 'green',
				'before' => '<span style="color: green;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'glow',
				'type' => 'unparsed_commas',
				'test' => '[#0-9a-zA-Z\-]{3,12},([012]\d{1,2}|\d{1,2})(,[^]]+)?\]',
				'before' => $context['browser']['is_ie'] ? '<table border="0" cellpadding="0" cellspacing="0" style="display: inline; vertical-align: middle; font: inherit;"><tr><td style="filter: Glow(color=$1, strength=$2); font: inherit;">' : '<span style="background-color: $1;">',
				'after' => $context['browser']['is_ie'] ? '</td></tr></table> ' : '</span>',
			),
			array(
				'tag' => 'hr',
				'type' => 'closed',
				'content' => '<hr />',
				'block_level' => true,
			),
			array(
				'tag' => 'html',
				'type' => 'unparsed_content',
				'content' => '$1',
				'block_level' => true,
				'disabled_content' => '$1',
			),
			array(
				'tag' => 'img',
				'type' => 'unparsed_content',
				'parameters' => array(
					'alt' => array('optional' => true),
					'width' => array('optional' => true, 'value' => ' width="$1"', 'match' => '(\d+)'),
					'height' => array('optional' => true, 'value' => ' height="$1"', 'match' => '(\d+)'),
				),
				'content' => '<img src="$1" alt="{alt}"{width}{height} border="0" />',
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
				'disabled_content' => '($1)',
			),
			array(
				'tag' => 'img',
				'type' => 'unparsed_content',
				'content' => '<img src="$1" alt="" border="0" />',
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
				'disabled_content' => '($1)',
			),
			array(
				'tag' => 'i',
				'before' => '<i>',
				'after' => '</i>',
			),
			array(
				'tag' => 'iurl',
				'type' => 'unparsed_content',
				'content' => '<a href="$1">$1</a>',
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
			),
			array(
				'tag' => 'iurl',
				'type' => 'unparsed_equals',
				'before' => '<a href="$1">',
				'after' => '</a>',
				'validate' => create_function('&$tag, &$data, $disabled', '
					if (substr($data, 0, 1) == \'#\')
						$data = \'#post_\' . substr($data, 1);'),
				'disallow_children' => array('email', 'ftp', 'url', 'iurl'),
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'li',
				'before' => '<li>',
				'after' => '</li>',
				'trim' => 'outside',
				'require_parents' => array('list'),
				'block_level' => true,
				'disabled_before' => '',
				'disabled_after' => '<br />',
			),
			array(
				'tag' => 'list',
				'before' => '<ul style="margin-top: 0; margin-bottom: 0;">',
				'after' => '</ul>',
				'trim' => 'inside',
				'require_children' => array('li'),
				'block_level' => true,
			),
			array(
				'tag' => 'list',
				'parameters' => array(
					'type' => array('match' => '(none|disc|circle|square|decimal|decimal-leading-zero|lower-roman|upper-roman|lower-alpha|upper-alpha|lower-greek|lower-latin|upper-latin|hebrew|armenian|georgian|cjk-ideographic|hiragana|katakana|hiragana-iroha|katakana-iroha)'),
				),
				'before' => '<ul style="margin-top: 0; margin-bottom: 0; list-style-type: {type};">',
				'after' => '</ul>',
				'trim' => 'inside',
				'require_children' => array('li'),
				'block_level' => true,
			),
			array(
				'tag' => 'left',
				'before' => '<div style="text-align: left;">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'ltr',
				'before' => '<div dir="ltr">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'me',
				'type' => 'unparsed_equals',
				'before' => '<div class="meaction">* $1 ',
				'after' => '</div>',
				'quoted' => 'optional',
				'block_level' => true,
				'disabled_before' => '/me ',
				'disabled_after' => '<br />',
			),
			array(
				'tag' => 'move',
				'before' => '<marquee>',
				'after' => '</marquee>',
				'block_level' => true,
			),
			array(
				'tag' => 'nobbc',
				'type' => 'unparsed_content',
				'content' => '$1',
			),
			array(
				'tag' => 'pre',
				'before' => '<pre>',
				'after' => '</pre>',
			),
			array(
				'tag' => 'php',
				'type' => 'unparsed_content',
				'content' => '<div class="phpcode">$1</div>',
				'validate' => isset($disabled['php']) ? null : create_function('&$tag, &$data, $disabled', '
					if (!isset($disabled[\'php\']))
					{
						$add_begin = substr(trim($data), 0, 5) != \'&lt;?\';
						$data = highlight_php_code($add_begin ? \'&lt;?php \' . $data . \'?&gt;\' : $data);
						if ($add_begin)
							$data = preg_replace(array(\'~^(.+?)&lt;\?.{0,40}?php(&nbsp;|\s)~\', \'~\?&gt;((?:</(font|span)>)*)$~\'), \'$1\', $data, 2);
					}'),
				'block_level' => true,
				'disabled_content' => '$1',
			),
			array(
				'tag' => 'quote',
				'before' => '<div class="quoteheader">' . $txt['smf240'] . '</div><div class="quote">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'quote',
				'parameters' => array(
					'author' => array('match' => '(.{1,192}?)', 'quoted' => true, 'validate' => 'parse_bbc'),
				),
				'before' => '<div class="quoteheader">' . $txt['smf239'] . ': {author}</div><div class="quote">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'quote',
				'type' => 'parsed_equals',
				'before' => '<div class="quoteheader">' . $txt['smf239'] . ': $1</div><div class="quote">',
				'after' => '</div>',
				'quoted' => 'optional',
				'block_level' => true,
			),
			array(
				'tag' => 'quote',
				'parameters' => array(
					'author' => array('match' => '([^<>]{1,192}?)'),
					'link' => array('match' => '(?:board=\d+;)?((?:topic|threadid)=[\dmsg#\./]{1,40}(?:;start=[\dmsg#\./]{1,40})?|action=profile;u=\d+)'),
					'date' => array('match' => '(\d+)', 'validate' => 'timeformat'),
				),
				'before' => '<div class="quoteheader"><a href="' . $scripturl . '?{link}">' . $txt['smf239'] . ': {author} ' . $txt[176] . ' {date}</a></div><div class="quote">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'quote',
				'parameters' => array(
					'author' => array('match' => '(.{1,192}?)', 'validate' => 'parse_bbc'),
				),
				'before' => '<div class="quoteheader">' . $txt['smf239'] . ': {author}</div><div class="quote">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'right',
				'before' => '<div style="text-align: right;">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 'red',
				'before' => '<span style="color: red;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'rtl',
				'before' => '<div dir="rtl">',
				'after' => '</div>',
				'block_level' => true,
			),
			array(
				'tag' => 's',
				'before' => '<del>',
				'after' => '</del>',
			),
			array(
				'tag' => 'size',
				'type' => 'unparsed_equals',
				'test' => '([\d]{1,2}p[xt]|(?:x-)?small(?:er)?|(?:x-)?large[r]?)\]',
				// !!! line-height
				'before' => '<span style="font-size: $1; line-height: 1.3em;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'size',
				'type' => 'unparsed_equals',
				'test' => '[\d]\]',
				// !!! line-height
				'before' => '<font size="$1" style="line-height: 1.3em;">',
				'after' => '</font>',
			),
			array(
				'tag' => 'sub',
				'before' => '<sub>',
				'after' => '</sub>',
			),
			array(
				'tag' => 'sup',
				'before' => '<sup>',
				'after' => '</sup>',
			),
			array(
				'tag' => 'shadow',
				'type' => 'unparsed_commas',
				'test' => '[#0-9a-zA-Z\-]{3,12},(left|right|top|bottom|[0123]\d{0,2})\]',
				'before' => $context['browser']['is_ie'] ? '<span style="filter: Shadow(color=$1, direction=$2); height: 1.2em;\">' : '<span style="text-shadow: $1 $2">',
				'after' => '</span>',
				'validate' => $context['browser']['is_ie'] ? create_function('&$tag, &$data, $disabled', '
					if ($data[1] == \'left\')
						$data[1] = 270;
					elseif ($data[1] == \'right\')
						$data[1] = 90;
					elseif ($data[1] == \'top\')
						$data[1] = 0;
					elseif ($data[1] == \'bottom\')
						$data[1] = 180;
					else
						$data[1] = (int) $data[1];') : create_function('&$tag, &$data, $disabled', '
					if ($data[1] == \'top\' || (is_numeric($data[1]) && $data[1] < 50))
						return \'0 -2px\';
					elseif ($data[1] == \'right\' || (is_numeric($data[1]) && $data[1] < 100))
						return \'2px 0\';
					elseif ($data[1] == \'bottom\' || (is_numeric($data[1]) && $data[1] < 190))
						return \'0 2px\';
					elseif ($data[1] == \'left\' || (is_numeric($data[1]) && $data[1] < 280))
						return \'-2px 0\';
					else
						return \'0 0\';'),
			),
			array(
				'tag' => 'time',
				'type' => 'unparsed_content',
				'content' => '$1',
				'validate' => create_function('&$tag, &$data, $disabled', '
					if (is_numeric($data))
						$data = timeformat($data);
					else
						$tag[\'content\'] = \'[time]$1[/time]\';'),
			),
			array(
				'tag' => 'tt',
				'before' => '<tt>',
				'after' => '</tt>',
			),
			array(
				'tag' => 'table',
				'before' => '<table style="font: inherit; color: inherit;">',
				'after' => '</table>',
				'trim' => 'inside',
				'require_children' => array('tr'),
				'block_level' => true,
			),
			array(
				'tag' => 'tr',
				'before' => '<tr>',
				'after' => '</tr>',
				'require_parents' => array('table'),
				'require_children' => array('td'),
				'trim' => 'both',
				'block_level' => true,
				'disabled_before' => '',
				'disabled_after' => '',
			),
			array(
				'tag' => 'td',
				'before' => '<td valign="top" style="font: inherit; color: inherit;">',
				'after' => '</td>',
				'require_parents' => array('tr'),
				'trim' => 'outside',
				'block_level' => true,
				'disabled_before' => '',
				'disabled_after' => '',
			),
			array(
				'tag' => 'url',
				'type' => 'unparsed_content',
				'content' => '<a href="$1" target="_blank">$1</a>',
				'validate' => create_function('&$tag, &$data, $disabled', '$data = strtr($data, array(\'<br />\' => \'\'));'),
			),
			array(
				'tag' => 'url',
				'type' => 'unparsed_equals',
				'before' => '<a href="$1" target="_blank">',
				'after' => '</a>',
				'disallow_children' => array('email', 'ftp', 'url', 'iurl'),
				'disabled_after' => ' ($1)',
			),
			array(
				'tag' => 'u',
				'before' => '<span style="text-decoration: underline;">',
				'after' => '</span>',
			),
			array(
				'tag' => 'white',
				'before' => '<span style="color: white;">',
				'after' => '</span>',
			),
		);

		// This is mainly for the bbc manager, so it's easy to add tags above.  Custom BBC should be added above this line.
		if ($message === false)
			return $codes;

		// So the parser won't skip them.
		$itemcodes = array(
			'*' => '',
			'@' => 'disc',
			'+' => 'square',
			'x' => 'square',
			'#' => 'square',
			'o' => 'circle',
			'O' => 'circle',
			'0' => 'circle',
		);
		if (!isset($disabled['li']) && !isset($disabled['list']))
		{
			foreach ($itemcodes as $c => $dummy)
				$bbc_codes[$c] = array();
		}

		// Inside these tags autolink is not recommendable.
		$no_autolink_tags = array(
			'url', 
			'iurl', 
			'ftp', 
			'email',
		);

		// Shhhh!
		if (!isset($disabled['color']))
		{
			$codes[] = array(
				'tag' => 'chrissy',
				'before' => '<span style="color: #CC0099;">',
				'after' => ' :-*</span>',
			);
			$codes[] = array(
				'tag' => 'kissy',
				'before' => '<span style="color: #CC0099;">',
				'after' => ' :-*</span>',
			);
		}

		foreach ($codes as $c)
			$bbc_codes[substr($c['tag'], 0, 1)][] = $c;
		$codes = null;
	}

	// Shall we take the time to cache this?
	if ($cache_id != '' && !empty($modSettings['cache_enable']) && (($modSettings['cache_enable'] >= 2 && strlen($message) > 1000) || strlen($message) > 2400))
	{
		// It's likely this will change if the message is modified.
		$cache_key = 'parse:' . $cache_id . '-' . md5(md5($message) . '-' . $smileys . (empty($disabled) ? '' : implode(',', array_keys($disabled))) . serialize($context['browser']) . $txt['lang_locale'] . $user_info['time_offset'] . $user_info['time_format']);

		if (($temp = cache_get_data($cache_key, 240)) != null)
			return $temp;

		$cache_t = microtime();
	}

	if ($smileys === 'print')
	{
		// [glow], [shadow], and [move] can't really be printed.
		$disabled['glow'] = true;
		$disabled['shadow'] = true;
		$disabled['move'] = true;

		// Colors can't well be displayed... supposed to be black and white.
		$disabled['color'] = true;
		$disabled['black'] = true;
		$disabled['blue'] = true;
		$disabled['white'] = true;
		$disabled['red'] = true;
		$disabled['green'] = true;
		$disabled['me'] = true;

		// Color coding doesn't make sense.
		$disabled['php'] = true;

		// Links are useless on paper... just show the link.
		$disabled['ftp'] = true;
		$disabled['url'] = true;
		$disabled['iurl'] = true;
		$disabled['email'] = true;
		$disabled['flash'] = true;

		// !!! Change maybe?
		if (!isset($_GET['images']))
			$disabled['img'] = true;

		// !!! Interface/setting to add more?
	}

	$open_tags = array();
	$message = strtr($message, array("\n" => '<br />'));

	// The non-breaking-space looks a bit different each time.
	$non_breaking_space = $context['utf8'] ? ($context['server']['complex_preg_chars'] ? '\x{C2A0}' : chr(0xC2) . chr(0xA0)) : '\xA0';

	$pos = -1;
	while ($pos !== false)
	{
		$last_pos = isset($last_pos) ? max($pos, $last_pos) : $pos;
		$pos = strpos($message, '[', $pos + 1);

		// Failsafe.
		if ($pos === false || $last_pos > $pos)
			$pos = strlen($message) + 1;

		// Can't have a one letter smiley, URL, or email! (sorry.)
		if ($last_pos < $pos - 1)
		{
			// We want to eat one less, and one more, character (for smileys.)
			$last_pos = max($last_pos - 1, 0);
			$data = substr($message, $last_pos, $pos - $last_pos + 1);

			// Take care of some HTML!
			if (!empty($modSettings['enablePostHTML']) && strpos($data, '&lt;') !== false)
			{
				$data = preg_replace('~&lt;a\s+href=(?:&quot;)?((?:http://|ftp://|https://|ftps://|mailto:).+?)(?:&quot;)?&gt;~i', '[url=$1]', $data);
				$data = preg_replace('~&lt;/a&gt;~i', '[/url]', $data);

				// <br /> should be empty.
				$empty_tags = array('br', 'hr');
				foreach ($empty_tags as $tag)
					$data = str_replace(array('&lt;' . $tag . '&gt;', '&lt;' . $tag . '/&gt;', '&lt;' . $tag . ' /&gt;'), '[' . $tag . ' /]', $data);

				// b, u, i, s, pre... basic tags.
				$closable_tags = array('b', 'u', 'i', 's', 'em', 'ins', 'del', 'pre', 'blockquote');
				foreach ($closable_tags as $tag)
				{
					$diff = substr_count($data, '&lt;' . $tag . '&gt;') - substr_count($data, '&lt;/' . $tag . '&gt;');
					$data = strtr($data, array('&lt;' . $tag . '&gt;' => '<' . $tag . '>', '&lt;/' . $tag . '&gt;' => '</' . $tag . '>'));

					if ($diff > 0)
						$data .= str_repeat('</' . $tag . '>', $diff);
				}

				// Do <img ... /> - with security... action= -> action-.
				preg_match_all('~&lt;img\s+src=(?:&quot;)?((?:http://|ftp://|https://|ftps://).+?)(?:&quot;)?(?:\s+alt=(?:&quot;)?(.*?)(?:&quot;)?)?(?:\s?/)?&gt;~i', $data, $matches, PREG_PATTERN_ORDER);
				if (!empty($matches[0]))
				{
					$replaces = array();
					foreach ($matches[1] as $match => $imgtag)
					{
						// No alt?
						if (!isset($matches[2][$match]))
							$matches[2][$match] = '';

						// Remove action= from the URL - no funny business, now.
						if (preg_match('~action(=|%3d)(?!dlattach)~i', $imgtag) != 0)
							$imgtag = preg_replace('~action(=|%3d)(?!dlattach)~i', 'action-', $imgtag);

						// Check if the image is larger than allowed.
						if (!empty($modSettings['max_image_width']) && !empty($modSettings['max_image_height']))
						{
							list ($width, $height) = url_image_size($imgtag);

							if (!empty($modSettings['max_image_width']) && $width > $modSettings['max_image_width'])
							{
								$height = (int) (($modSettings['max_image_width'] * $height) / $width);
								$width = $modSettings['max_image_width'];
							}

							if (!empty($modSettings['max_image_height']) && $height > $modSettings['max_image_height'])
							{
								$width = (int) (($modSettings['max_image_height'] * $width) / $height);
								$height = $modSettings['max_image_height'];
							}

							// Set the new image tag.
							$replaces[$matches[0][$match]] = '<img src="' . $imgtag . '" width="' . $width . '" height="' . $height . '" alt="' . $matches[2][$match] . '" border="0" />';
						}
						else
							$replaces[$matches[0][$match]] = '<img src="' . $imgtag . '" alt="' . $matches[2][$match] . '" border="0" />';
					}

					$data = strtr($data, $replaces);
				}
			}

			if (!empty($modSettings['autoLinkUrls']))
			{
				// Are we inside tags that should be auto linked?
				$no_autolink_area = false;
				if (!empty($open_tags))
				{
					foreach ($open_tags as $open_tag)
						if (in_array($open_tag['tag'], $no_autolink_tags))
							$no_autolink_area = true;
				}

				if (!$no_autolink_area)
				{
					// Parse any URLs.... have to get rid of the @ problems some things cause... stupid email addresses.
					if (!isset($disabled['url']) && (strpos($data, '://') !== false || strpos($data, 'www.') !== false))
					{
						// Switch out quotes really quick because they can cause problems.
						$data = strtr($data, array('&#039;' => '\'', '&nbsp;' => $context['utf8'] ? "\xC2\xA0" : "\xA0", '&quot;' => '>">', '"' => '<"<', '&lt;' => '<lt<'));
						$data = preg_replace(array('~(?<=[\s>\.(;\'"]|^)((?:http|https|ftp|ftps)://[\w\-_%@:|]+(?:\.[\w\-_%]+)*(?::\d+)?(?:/[\w\-_\~%\.@,\?&;=#+:\'\\\\]*|[\(\{][\w\-_\~%\.@,\?&;=#(){}+:\'\\\\]*)*[/\w\-_\~%@\?;=#}\\\\])~i', '~(?<=[\s>(\'<]|^)(www(?:\.[\w\-_]+)+(?::\d+)?(?:/[\w\-_\~%\.@,\?&;=#+:\'\\\\]*|[\(\{][\w\-_\~%\.@,\?&;=#(){}+:\'\\\\]*)*[/\w\-_\~%@\?;=#}\\\\])~i'), array('[url]$1[/url]', '[url=http://$1]$1[/url]'), $data);
						$data = strtr($data, array('\'' => '&#039;', $context['utf8'] ? "\xC2\xA0" : "\xA0" => '&nbsp;', '>">' => '&quot;', '<"<' => '"', '<lt<' => '&lt;'));
					}

					// Next, emails...
					if (!isset($disabled['email']) && strpos($data, '@') !== false)
					{
						$data = preg_replace('~(?<=[\?\s' . $non_breaking_space . '\[\]()*\\\;>]|^)([\w\-\.]{1,80}@[\w\-]+\.[\w\-\.]+[\w\-])(?=[?,\s' . $non_breaking_space . '\[\]()*\\\]|$|<br />|&nbsp;|&gt;|&lt;|&quot;|&#039;|\.(?:\.|;|&nbsp;|\s|$|<br />))~i' . ($context['utf8'] ? 'u' : ''), '[email]$1[/email]', $data);
						$data = preg_replace('~(?<=<br />)([\w\-\.]{1,80}@[\w\-]+\.[\w\-\.]+[\w\-])(?=[?\.,;\s' . $non_breaking_space . '\[\]()*\\\]|$|<br />|&nbsp;|&gt;|&lt;|&quot;|&#039;)~i' . ($context['utf8'] ? 'u' : ''), '[email]$1[/email]', $data);
					}
				}
			}

			$data = strtr($data, array("\t" => '&nbsp;&nbsp;&nbsp;'));

			if (!empty($modSettings['fixLongWords']) && $modSettings['fixLongWords'] > 5)
			{
				// This is SADLY and INCREDIBLY browser dependent.
				if ($context['browser']['is_gecko'] || $context['browser']['is_konqueror'])
					$breaker = '<span style="margin: 0 -0.5ex 0 0;"> </span>';
				// Opera...
				elseif ($context['browser']['is_opera'])
					$breaker = '<span style="margin: 0 -0.65ex 0 -1px;"> </span>';
				// Internet Explorer...
				else
					$breaker = '<span style="width: 0; margin: 0 -0.6ex 0 -1px;"> </span>';

				// PCRE will not be happy if we don't give it a short.
				$modSettings['fixLongWords'] = (int) min(65535, $modSettings['fixLongWords']);

				// The idea is, find words xx long, and then replace them with xx + space + more.
				if (strlen($data) > $modSettings['fixLongWords'])
				{
					// This is done in a roundabout way because $breaker has "long words" :P.
					$data = strtr($data, array($breaker => '< >', '&nbsp;' => $context['utf8'] ? "\xC2\xA0" : "\xA0"));
					$data = preg_replace(
						'~(?<=[>;:!? ' . $non_breaking_space . '\]()]|^)([\w\.]{' . $modSettings['fixLongWords'] . ',})~e' . ($context['utf8'] ? 'u' : ''),
						"preg_replace('/(.{" . ($modSettings['fixLongWords'] - 1) . '})/' . ($context['utf8'] ? 'u' : '') . "', '\\\$1< >', '\$1')",
						$data);
					$data = strtr($data, array('< >' => $breaker, $context['utf8'] ? "\xC2\xA0" : "\xA0" => '&nbsp;'));
				}
			}

			// Do any smileys!
			if ($smileys === true)
				parsesmileys($data);

			// If it wasn't changed, no copying or other boring stuff has to happen!
			if ($data != substr($message, $last_pos, $pos - $last_pos + 1))
			{
				$message = substr($message, 0, $last_pos) . $data . substr($message, $pos + 1);

				// Since we changed it, look again incase we added or removed a tag.  But we don't want to skip any.
				$old_pos = strlen($data) + $last_pos - 1;
				$pos = strpos($message, '[', $last_pos);
				$pos = $pos === false ? $old_pos : min($pos, $old_pos);
			}
		}

		// Are we there yet?  Are we there yet?
		if ($pos >= strlen($message) - 1)
			break;

		$tags = strtolower(substr($message, $pos + 1, 1));

		if ($tags == '/' && !empty($open_tags))
		{
			$pos2 = strpos($message, ']', $pos + 1);
			if ($pos2 == $pos + 2)
				continue;
			$look_for = strtolower(substr($message, $pos + 2, $pos2 - $pos - 2));

			$to_close = array();
			$block_level = null;
			do
			{
				$tag = array_pop($open_tags);
				if (!$tag)
					break;

				if (!empty($tag['block_level']))
				{
					// Only find out if we need to.
					if ($block_level === false)
					{
						array_push($open_tags, $tag);
						break;
					}

					// The idea is, if we are LOOKING for a block level tag, we can close them on the way.
					if (strlen($look_for) > 0 && isset($bbc_codes[$look_for{0}]))
					{
						foreach ($bbc_codes[$look_for{0}] as $temp)
							if ($temp['tag'] == $look_for)
							{
								$block_level = !empty($temp['block_level']);
								break;
							}
					}

					if ($block_level !== true)
					{
						$block_level = false;
						array_push($open_tags, $tag);
						break;
					}
				}

				$to_close[] = $tag;
			}
			while ($tag['tag'] != $look_for);

			// Did we just eat through everything and not find it?
			if ((empty($open_tags) && (empty($tag) || $tag['tag'] != $look_for)))
			{
				$open_tags = $to_close;
				continue;
			}
			elseif (!empty($to_close) && $tag['tag'] != $look_for)
			{
				if ($block_level === null && isset($look_for{0}, $bbc_codes[$look_for{0}]))
				{
					foreach ($bbc_codes[$look_for{0}] as $temp)
						if ($temp['tag'] == $look_for)
						{
							$block_level = !empty($temp['block_level']);
							break;
						}
				}

				// We're not looking for a block level tag (or maybe even a tag that exists...)
				if (!$block_level)
				{
					foreach ($to_close as $tag)
						array_push($open_tags, $tag);
					continue;
				}
			}

			foreach ($to_close as $tag)
			{
				$message = substr($message, 0, $pos) . $tag['after'] . substr($message, $pos2 + 1);
				$pos += strlen($tag['after']);
				$pos2 = $pos - 1;

				// See the comment at the end of the big loop - just eating whitespace ;).
				if (!empty($tag['block_level']) && substr($message, $pos, 6) == '<br />')
					$message = substr($message, 0, $pos) . substr($message, $pos + 6);
				if (!empty($tag['trim']) && $tag['trim'] != 'inside' && preg_match('~(<br />|&nbsp;|\s)*~', substr($message, $pos), $matches) != 0)
					$message = substr($message, 0, $pos) . substr($message, $pos + strlen($matches[0]));
			}

			if (!empty($to_close))
			{
				$to_close = array();
				$pos--;
			}

			continue;
		}

		// No tags for this character, so just keep going (fastest possible course.)
		if (!isset($bbc_codes[$tags]))
			continue;

		$inside = empty($open_tags) ? null : $open_tags[count($open_tags) - 1];
		$tag = null;
		foreach ($bbc_codes[$tags] as $possible)
		{
			// Not a match?
			if (strtolower(substr($message, $pos + 1, strlen($possible['tag']))) != $possible['tag'])
				continue;

			$next_c = substr($message, $pos + 1 + strlen($possible['tag']), 1);

			// A test validation?
			if (isset($possible['test']) && preg_match('~^' . $possible['test'] . '~', substr($message, $pos + 1 + strlen($possible['tag']) + 1)) == 0)
				continue;
			// Do we want parameters?
			elseif (!empty($possible['parameters']))
			{
				if ($next_c != ' ')
					continue;
			}
			elseif (isset($possible['type']))
			{
				// Do we need an equal sign?
				if (in_array($possible['type'], array('unparsed_equals', 'unparsed_commas', 'unparsed_commas_content', 'unparsed_equals_content', 'parsed_equals')) && $next_c != '=')
					continue;
				// Maybe we just want a /...
				if ($possible['type'] == 'closed' && $next_c != ']' && substr($message, $pos + 1 + strlen($possible['tag']), 2) != '/]' && substr($message, $pos + 1 + strlen($possible['tag']), 3) != ' /]')
					continue;
				// An immediate ]?
				if ($possible['type'] == 'unparsed_content' && $next_c != ']')
					continue;
			}
			// No type means 'parsed_content', which demands an immediate ] without parameters!
			elseif ($next_c != ']')
				continue;

			// Check allowed tree?
			if (isset($possible['require_parents']) && ($inside === null || !in_array($inside['tag'], $possible['require_parents'])))
				continue;
			elseif (isset($inside['require_children']) && !in_array($possible['tag'], $inside['require_children']))
				continue;
			// If this is in the list of disallowed child tags, don't parse it.
			elseif (isset($inside['disallow_children']) && in_array($possible['tag'], $inside['disallow_children']))
				continue;

			$pos1 = $pos + 1 + strlen($possible['tag']) + 1;

			// This is long, but it makes things much easier and cleaner.
			if (!empty($possible['parameters']))
			{
				$preg = array();
				foreach ($possible['parameters'] as $p => $info)
					$preg[] = '(\s+' . $p . '=' . (empty($info['quoted']) ? '' : '&quot;') . (isset($info['match']) ? $info['match'] : '(.+?)') . (empty($info['quoted']) ? '' : '&quot;') . ')' . (empty($info['optional']) ? '' : '?');

				// Okay, this may look ugly and it is, but it's not going to happen much and it is the best way of allowing any order of parameters but still parsing them right.
				$match = false;
				$orders = permute($preg);
				foreach ($orders as $p)
					if (preg_match('~^' . implode('', $p) . '\]~i', substr($message, $pos1 - 1), $matches) != 0)
					{
						$match = true;
						break;
					}

				// Didn't match our parameter list, try the next possible.
				if (!$match)
					continue;

				$params = array();
				for ($i = 1, $n = count($matches); $i < $n; $i += 2)
				{
					$key = strtok(ltrim($matches[$i]), '=');
					if (isset($possible['parameters'][$key]['value']))
						$params['{' . $key . '}'] = strtr($possible['parameters'][$key]['value'], array('$1' => $matches[$i + 1]));
					elseif (isset($possible['parameters'][$key]['validate']))
						$params['{' . $key . '}'] = $possible['parameters'][$key]['validate']($matches[$i + 1]);
					else
						$params['{' . $key . '}'] = $matches[$i + 1];

					// Just to make sure: replace any $ or { so they can't interpolate wrongly.
					$params['{' . $key . '}'] = strtr($params['{' . $key . '}'], array('$' => '&#036;', '{' => '&#123;'));
				}

				foreach ($possible['parameters'] as $p => $info)
				{
					if (!isset($params['{' . $p . '}']))
						$params['{' . $p . '}'] = '';
				}

				$tag = $possible;

				// Put the parameters into the string.
				if (isset($tag['before']))
					$tag['before'] = strtr($tag['before'], $params);
				if (isset($tag['after']))
					$tag['after'] = strtr($tag['after'], $params);
				if (isset($tag['content']))
					$tag['content'] = strtr($tag['content'], $params);

				$pos1 += strlen($matches[0]) - 1;
			}
			else
				$tag = $possible;
			break;
		}

		// Item codes are complicated buggers... they are implicit [li]s and can make [list]s!
		if ($smileys !== false && $tag === null && isset($itemcodes[substr($message, $pos + 1, 1)]) && substr($message, $pos + 2, 1) == ']' && !isset($disabled['list']) && !isset($disabled['li']))
		{
			if (substr($message, $pos + 1, 1) == '0' && !in_array(substr($message, $pos - 1, 1), array(';', ' ', "\t", '>')))
				continue;
			$tag = $itemcodes[substr($message, $pos + 1, 1)];

			// First let's set up the tree: it needs to be in a list, or after an li.
			if ($inside === null || ($inside['tag'] != 'list' && $inside['tag'] != 'li'))
			{
				$open_tags[] = array(
					'tag' => 'list',
					'after' => '</ul>',
					'block_level' => true,
					'require_children' => array('li'),
					'disallow_children' => isset($inside['disallow_children']) ? $inside['disallow_children'] : null,
				);
				$code = '<ul style="margin-top: 0; margin-bottom: 0;">';
			}
			// We're in a list item already: another itemcode?  Close it first.
			elseif ($inside['tag'] == 'li')
			{
				array_pop($open_tags);
				$code = '</li>';
			}
			else
				$code = '';

			// Now we open a new tag.
			$open_tags[] = array(
				'tag' => 'li',
				'after' => '</li>',
				'trim' => 'outside',
				'block_level' => true,
				'disallow_children' => isset($inside['disallow_children']) ? $inside['disallow_children'] : null,
			);

			// First, open the tag...
			$code .= '<li' . ($tag == '' ? '' : ' type="' . $tag . '"') . '>';
			$message = substr($message, 0, $pos) . $code . substr($message, $pos + 3);
			$pos += strlen($code) - 1;

			// Next, find the next break (if any.)  If there's more itemcode after it, keep it going - otherwise close!
			$pos2 = strpos($message, '<br />', $pos);
			$pos3 = strpos($message, '[/', $pos);
			if ($pos2 !== false && ($pos2 <= $pos3 || $pos3 === false))
			{
				preg_match('~^(<br />|&nbsp;|\s|\[)+~', substr($message, $pos2 + 6), $matches);
				$message = substr($message, 0, $pos2) . (!empty($matches[0]) && substr($matches[0], -1) == '[' ? '[/li]' : '[/li][/list]') . substr($message, $pos2);

				$open_tags[count($open_tags) - 2]['after'] = '</ul>';
			}
			// Tell the [list] that it needs to close specially.
			else
			{
				// Move the li over, because we're not sure what we'll hit.
				$open_tags[count($open_tags) - 1]['after'] = '';
				$open_tags[count($open_tags) - 2]['after'] = '</li></ul>';
			}

			continue;
		}

		// Implicitly close lists and tables if something other than what's required is in them.  This is needed for itemcode.
		if ($tag === null && $inside !== null && !empty($inside['require_children']))
		{
			array_pop($open_tags);

			$message = substr($message, 0, $pos) . $inside['after'] . substr($message, $pos);
			$pos += strlen($inside['after']) - 1;
		}

		// No tag?  Keep looking, then.  Silly people using brackets without actual tags.
		if ($tag === null)
			continue;

		// Propagate the list to the child (so wrapping the disallowed tag won't work either.)
		if (isset($inside['disallow_children']))
			$tag['disallow_children'] = isset($tag['disallow_children']) ? array_unique(array_merge($tag['disallow_children'], $inside['disallow_children'])) : $inside['disallow_children'];

		// Is this tag disabled?
		if (isset($disabled[$tag['tag']]))
		{
			if (!isset($tag['disabled_before']) && !isset($tag['disabled_after']) && !isset($tag['disabled_content']))
			{
				$tag['before'] = !empty($tag['block_level']) ? '<div>' : '';
				$tag['after'] = !empty($tag['block_level']) ? '</div>' : '';
				$tag['content'] = isset($tag['type']) && $tag['type'] == 'closed' ? '' : (!empty($tag['block_level']) ? '<div>$1</div>' : '$1');
			}
			elseif (isset($tag['disabled_before']) || isset($tag['disabled_after']))
			{
				$tag['before'] = isset($tag['disabled_before']) ? $tag['disabled_before'] : (!empty($tag['block_level']) ? '<div>' : '');
				$tag['after'] = isset($tag['disabled_after']) ? $tag['disabled_after'] : (!empty($tag['block_level']) ? '</div>' : '');
			}
			else
				$tag['content'] = $tag['disabled_content'];
		}

		// The only special case is 'html', which doesn't need to close things.
		if (!empty($tag['block_level']) && $tag['tag'] != 'html' && empty($inside['block_level']))
		{
			$n = count($open_tags) - 1;
			while (empty($open_tags[$n]['block_level']) && $n >= 0)
				$n--;

			// Close all the non block level tags so this tag isn't surrounded by them.
			for ($i = count($open_tags) - 1; $i > $n; $i--)
			{
				$message = substr($message, 0, $pos) . $open_tags[$i]['after'] . substr($message, $pos);
				$pos += strlen($open_tags[$i]['after']);
				$pos1 += strlen($open_tags[$i]['after']);

				// Trim or eat trailing stuff... see comment at the end of the big loop.
				if (!empty($open_tags[$i]['block_level']) && substr($message, $pos, 6) == '<br />')
					$message = substr($message, 0, $pos) . substr($message, $pos + 6);
				if (!empty($open_tags[$i]['trim']) && $tag['trim'] != 'inside' && preg_match('~(<br />|&nbsp;|\s)*~', substr($message, $pos), $matches) != 0)
					$message = substr($message, 0, $pos) . substr($message, $pos + strlen($matches[0]));

				array_pop($open_tags);
			}
		}

		// No type means 'parsed_content'.
		if (!isset($tag['type']))
		{
			// !!! Check for end tag first, so people can say "I like that [i] tag"?
			$open_tags[] = $tag;
			$message = substr($message, 0, $pos) . $tag['before'] . substr($message, $pos1);
			$pos += strlen($tag['before']) - 1;
		}
		// Don't parse the content, just skip it.
		elseif ($tag['type'] == 'unparsed_content')
		{
			$pos2 = stripos($message, '[/' . substr($message, $pos + 1, strlen($tag['tag'])) . ']', $pos1);
			if ($pos2 === false)
				continue;

			$data = substr($message, $pos1, $pos2 - $pos1);

			if (!empty($tag['block_level']) && substr($data, 0, 6) == '<br />')
				$data = substr($data, 6);

			if (isset($tag['validate']))
				$tag['validate']($tag, $data, $disabled);

			$code = strtr($tag['content'], array('$1' => $data));
			$message = substr($message, 0, $pos) . $code . substr($message, $pos2 + 3 + strlen($tag['tag']));
			$pos += strlen($code) - 1;
		}
		// Don't parse the content, just skip it.
		elseif ($tag['type'] == 'unparsed_equals_content')
		{
			// The value may be quoted for some tags - check.
			if (isset($tag['quoted']))
			{
				$quoted = substr($message, $pos1, 6) == '&quot;';
				if ($tag['quoted'] != 'optional' && !$quoted)
					continue;

				if ($quoted)
					$pos1 += 6;
			}
			else
				$quoted = false;

			$pos2 = strpos($message, $quoted == false ? ']' : '&quot;]', $pos1);
			if ($pos2 === false)
				continue;
			$pos3 = stripos($message, '[/' . substr($message, $pos + 1, strlen($tag['tag'])) . ']', $pos2);
			if ($pos3 === false)
				continue;

			$data = array(
				substr($message, $pos2 + ($quoted == false ? 1 : 7), $pos3 - ($pos2 + ($quoted == false ? 1 : 7))),
				substr($message, $pos1, $pos2 - $pos1)
			);

			if (!empty($tag['block_level']) && substr($data[0], 0, 6) == '<br />')
				$data[0] = substr($data[0], 6);

			// Validation for my parking, please!
			if (isset($tag['validate']))
				$tag['validate']($tag, $data, $disabled);

			$code = strtr($tag['content'], array('$1' => $data[0], '$2' => $data[1]));
			$message = substr($message, 0, $pos) . $code . substr($message, $pos3 + 3 + strlen($tag['tag']));
			$pos += strlen($code) - 1;
		}
		// A closed tag, with no content or value.
		elseif ($tag['type'] == 'closed')
		{
			$pos2 = strpos($message, ']', $pos);
			$message = substr($message, 0, $pos) . $tag['content'] . substr($message, $pos2 + 1);
			$pos += strlen($tag['content']) - 1;
		}
		// This one is sorta ugly... :/.  Unforunately, it's needed for flash.
		elseif ($tag['type'] == 'unparsed_commas_content')
		{
			$pos2 = strpos($message, ']', $pos1);
			if ($pos2 === false)
				continue;
			$pos3 = stripos($message, '[/' . substr($message, $pos + 1, strlen($tag['tag'])) . ']', $pos2);
			if ($pos3 === false)
				continue;

			// We want $1 to be the content, and the rest to be csv.
			$data = explode(',', ',' . substr($message, $pos1, $pos2 - $pos1));
			$data[0] = substr($message, $pos2 + 1, $pos3 - $pos2 - 1);

			if (isset($tag['validate']))
				$tag['validate']($tag, $data, $disabled);

			$code = $tag['content'];
			foreach ($data as $k => $d)
				$code = strtr($code, array('$' . ($k + 1) => trim($d)));
			$message = substr($message, 0, $pos) . $code . substr($message, $pos3 + 3 + strlen($tag['tag']));
			$pos += strlen($code) - 1;
		}
		// This has parsed content, and a csv value which is unparsed.
		elseif ($tag['type'] == 'unparsed_commas')
		{
			$pos2 = strpos($message, ']', $pos1);
			if ($pos2 === false)
				continue;

			$data = explode(',', substr($message, $pos1, $pos2 - $pos1));

			if (isset($tag['validate']))
				$tag['validate']($tag, $data, $disabled);

			// Fix after, for disabled code mainly.
			foreach ($data as $k => $d)
				$tag['after'] = strtr($tag['after'], array('$' . ($k + 1) => trim($d)));

			$open_tags[] = $tag;

			// Replace them out, $1, $2, $3, $4, etc.
			$code = $tag['before'];
			foreach ($data as $k => $d)
				$code = strtr($code, array('$' . ($k + 1) => trim($d)));
			$message = substr($message, 0, $pos) . $code . substr($message, $pos2 + 1);
			$pos += strlen($code) - 1;
		}
		// A tag set to a value, parsed or not.
		elseif ($tag['type'] == 'unparsed_equals' || $tag['type'] == 'parsed_equals')
		{
			// The value may be quoted for some tags - check.
			if (isset($tag['quoted']))
			{
				$quoted = substr($message, $pos1, 6) == '&quot;';
				if ($tag['quoted'] != 'optional' && !$quoted)
					continue;

				if ($quoted)
					$pos1 += 6;
			}
			else
				$quoted = false;

			$pos2 = strpos($message, $quoted == false ? ']' : '&quot;]', $pos1);
			if ($pos2 === false)
				continue;

			$data = substr($message, $pos1, $pos2 - $pos1);

			// Validation for my parking, please!
			if (isset($tag['validate']))
				$tag['validate']($tag, $data, $disabled);

			// For parsed content, we must recurse to avoid security problems.
			if ($tag['type'] != 'unparsed_equals')
				$data = parse_bbc($data);

			$tag['after'] = strtr($tag['after'], array('$1' => $data));

			$open_tags[] = $tag;

			$code = strtr($tag['before'], array('$1' => $data));
			$message = substr($message, 0, $pos) . $code . substr($message, $pos2 + ($quoted == false ? 1 : 7));
			$pos += strlen($code) - 1;
		}

		// If this is block level, eat any breaks after it.
		if (!empty($tag['block_level']) && substr($message, $pos + 1, 6) == '<br />')
			$message = substr($message, 0, $pos + 1) . substr($message, $pos + 7);

		// Are we trimming outside this tag?
		if (!empty($tag['trim']) && $tag['trim'] != 'outside' && preg_match('~(<br />|&nbsp;|\s)*~', substr($message, $pos + 1), $matches) != 0)
			$message = substr($message, 0, $pos + 1) . substr($message, $pos + 1 + strlen($matches[0]));
	}

	// Close any remaining tags.
	while ($tag = array_pop($open_tags))
		$message .= $tag['after'];

	if (substr($message, 0, 1) == ' ')
		$message = '&nbsp;' . substr($message, 1);

	// Cleanup whitespace.
	$message = strtr($message, array('  ' => ' &nbsp;', "\r" => '', "\n" => '<br />', '<br /> ' => '<br />&nbsp;'));

	// Cache the output if it took some time...
	if (isset($cache_key, $cache_t) && array_sum(explode(' ', microtime())) - array_sum(explode(' ', $cache_t)) > 0.05)
		cache_put_data($cache_key, $message, 240);

	return $message;
}

// Parse smileys in the passed message.
function parsesmileys(&$message)
{
	global $modSettings, $db_prefix, $txt, $user_info, $context;
	static $smileyfromcache = array(), $smileytocache = array();

	// No smiley set at all?!
	if ($user_info['smiley_set'] == 'none')
		return;

	// If the smiley array hasn't been set, do it now.
	if (empty($smileyfromcache))
	{
		// Use the default smileys if it is disabled. (better for "portability" of smileys.)
		if (empty($modSettings['smiley_enable']))
		{
			$smileysfrom = array('>:D', ':D', '::)', '>:(', ':)', ';)', ';D', ':(', ':o', '8)', ':P', '???', ':-[', ':-X', ':-*', ':\'(', ':-\\', '^-^', 'O0', 'C:-)', '0:)');
			$smileysto = array('evil.gif', 'cheesy.gif', 'rolleyes.gif', 'angry.gif', 'smiley.gif', 'wink.gif', 'grin.gif', 'sad.gif', 'shocked.gif', 'cool.gif', 'tongue.gif', 'huh.gif', 'embarrassed.gif', 'lipsrsealed.gif', 'kiss.gif', 'cry.gif', 'undecided.gif', 'azn.gif', 'afro.gif', 'police.gif', 'angel.gif');
			$smileysdescs = array('', $txt[289], $txt[450], $txt[288], $txt[287], $txt[292], $txt[293], $txt[291], $txt[294], $txt[295], $txt[451], $txt[296], $txt[526], $txt[527], $txt[529], $txt[530], $txt[528], '', '', '', '');
		}
		else
		{
			// Load the smileys in reverse order by length so they don't get parsed wrong.
			if (($temp = cache_get_data('parsing_smileys', 480)) == null)
			{
				$result = db_query("
					SELECT code, filename, description
					FROM {$db_prefix}smileys", __FILE__, __LINE__);
				$smileysfrom = array();
				$smileysto = array();
				$smileysdescs = array();
				while ($row = mysql_fetch_assoc($result))
				{
					$smileysfrom[] = $row['code'];
					$smileysto[] = $row['filename'];
					$smileysdescs[] = $row['description'];
				}
				mysql_free_result($result);

				cache_put_data('parsing_smileys', array($smileysfrom, $smileysto, $smileysdescs), 480);
			}
			else
				list ($smileysfrom, $smileysto, $smileysdescs) = $temp;
		}

		// The non-breaking-space is a complex thing...
		$non_breaking_space = $context['utf8'] ? ($context['server']['complex_preg_chars'] ? '\x{C2A0}' : chr(0xC2) . chr(0xA0)) : '\xA0';

		// This smiley regex makes sure it doesn't parse smileys within code tags (so [url=mailto:David@bla.com] doesn't parse the :D smiley)
		for ($i = 0, $n = count($smileysfrom); $i < $n; $i++)
		{
			$smileyfromcache[] = '/(?<=[>:\?\.\s' . $non_breaking_space . '[\]()*\\\;]|^)(' . preg_quote($smileysfrom[$i], '/') . '|' . preg_quote(htmlspecialchars($smileysfrom[$i], ENT_QUOTES), '/') . ')(?=[^[:alpha:]0-9]|$)/' . ($context['utf8'] ? 'u' : '');
			// Escape a bunch of smiley-related characters in the description so it doesn't get a double dose :P.
			$smileytocache[] = '<img src="' . $modSettings['smileys_url'] . '/' . $user_info['smiley_set'] . '/' . $smileysto[$i] . '" alt="' . strtr(htmlspecialchars($smileysdescs[$i]), array(':' => '&#58;', '(' => '&#40;', ')' => '&#41;', '$' => '&#36;', '[' => '&#091;')) . '" border="0" />';
		}
	}

	// Replace away!
	// !!! There must be a way to speed this up.
	$message = preg_replace($smileyfromcache, $smileytocache, $message);
}

// Highlight any code...
function highlight_php_code($code)
{
	global $context;

	// Remove special characters.
	$code = un_htmlspecialchars(strtr($code, array('<br />' => "\n", "\t" => 'SMF_TAB();', '&#91;' => '[')));

	$oldlevel = error_reporting(0);

	// It's easier in 4.2.x+.
	if (@version_compare(PHP_VERSION, '4.2.0') == -1)
	{
		ob_start();
		@highlight_string($code);
		$buffer = str_replace(array("\n", "\r"), '', ob_get_contents());
		ob_end_clean();
	}
	else
		$buffer = str_replace(array("\n", "\r"), '', @highlight_string($code, true));

	error_reporting($oldlevel);

	// Yes, I know this is kludging it, but this is the best way to preserve tabs from PHP :P.
	$buffer = preg_replace('~SMF_TAB(</(font|span)><(font color|span style)="[^"]*?">)?\(\);~', "<pre style=\"display: inline;\">\t</pre>", $buffer);

	return strtr($buffer, array('\'' => '&#039;', '<code>' => '', '</code>' => ''));
}

// Put this user in the online log.
function writeLog($force = false)
{
	global $db_prefix, $ID_MEMBER, $user_info, $user_settings, $sc, $modSettings, $settings, $topic, $board;

	// If we are showing who is viewing a topic, let's see if we are, and force an update if so - to make it accurate.
	if (!empty($settings['display_who_viewing']) && ($topic || $board))
	{
		// Take the opposite approach!
		$force = true;
		// Don't update for every page - this isn't wholly accurate but who cares.
		if ($topic)
		{
			if (isset($_SESSION['last_topic_id']) && $_SESSION['last_topic_id'] == $topic)
				$force = false;
			$_SESSION['last_topic_id'] = $topic;
		}
	}

	// Don't mark them as online more than every so often.
	if (!empty($_SESSION['log_time']) && $_SESSION['log_time'] >= (time() - 8) && !$force)
		return;

	if (!empty($modSettings['who_enabled']))
	{
		$serialized = $_GET + array('USER_AGENT' => $_SERVER['HTTP_USER_AGENT']);
		unset($serialized['sesc']);
		$serialized = addslashes(serialize($serialized));
	}
	else
		$serialized = '';

	// Guests use 0, members use their session ID.
	$session_id = $user_info['is_guest'] ? 'ip' . $user_info['ip'] : session_id();

	// Grab the last all-of-SMF-specific log_online deletion time.
	$do_delete = cache_get_data('log_online-update', 10) < time() - 10;

	// If the last click wasn't a long time ago, and there was a last click...
	if (!empty($_SESSION['log_time']) && $_SESSION['log_time'] >= time() - $modSettings['lastActive'] * 20)
	{
		if ($do_delete)
		{
			db_query("
				DELETE FROM {$db_prefix}log_online
				WHERE logTime < NOW() - INTERVAL " . ($modSettings['lastActive'] * 60) . " SECOND
					AND session != '$session_id'", __FILE__, __LINE__);
			cache_put_data('log_online-update', time(), 10);
		}

		db_query("
			UPDATE {$db_prefix}log_online
			SET logTime = NOW(), ip = IFNULL(INET_ATON('$user_info[ip]'), 0), url = '$serialized'
			WHERE session = '$session_id'
			LIMIT 1", __FILE__, __LINE__);

		// Guess it got deleted.
		if (db_affected_rows() == 0)
			$_SESSION['log_time'] = 0;
	}
	else
		$_SESSION['log_time'] = 0;

	// Otherwise, we have to delete and insert.
	if (empty($_SESSION['log_time']))
	{
		if ($do_delete || !empty($ID_MEMBER))
			db_query("
				DELETE FROM {$db_prefix}log_online
				WHERE " . ($do_delete ? "logTime < NOW() - INTERVAL " . ($modSettings['lastActive'] * 60) . ' SECOND' : '') . ($do_delete && !empty($ID_MEMBER) ? ' OR ' : '') . (empty($ID_MEMBER) ? '' : "ID_MEMBER = $ID_MEMBER"), __FILE__, __LINE__);

		db_query("
			" . ($do_delete ? 'INSERT IGNORE' : 'REPLACE') . " INTO {$db_prefix}log_online
				(session, ID_MEMBER, logTime, ip, url)
			VALUES ('$session_id', $ID_MEMBER, NOW(), IFNULL(INET_ATON('$user_info[ip]'), 0), '$serialized')", __FILE__, __LINE__);
	}

	// Mark your session as being logged.
	$_SESSION['log_time'] = time();

	// Well, they are online now.
	if (empty($_SESSION['timeOnlineUpdated']))
		$_SESSION['timeOnlineUpdated'] = time();

	// Set their login time, if not already done within the last minute.
	if (SMF != 'SSI' && !empty($user_info['last_login']) && $user_info['last_login'] < time() - 60)
	{
		// Don't count longer than 15 minutes.
		if (time() - $_SESSION['timeOnlineUpdated'] > 60 * 15)
			$_SESSION['timeOnlineUpdated'] = time();

		$user_settings['totalTimeLoggedIn'] += time() - $_SESSION['timeOnlineUpdated'];
		updateMemberData($ID_MEMBER, array('lastLogin' => time(), 'memberIP' => '\'' . $user_info['ip'] . '\'', 'memberIP2' => '\'' . $_SERVER['BAN_CHECK_IP'] . '\'', 'totalTimeLoggedIn' => $user_settings['totalTimeLoggedIn']));

		if (!empty($modSettings['cache_enable']) && $modSettings['cache_enable'] >= 2)
			cache_put_data('user_settings-' . $ID_MEMBER, $user_settings, 60);

		$user_info['total_time_logged_in'] += time() - $_SESSION['timeOnlineUpdated'];
		$_SESSION['timeOnlineUpdated'] = time();
	}
}

// Make sure the browser doesn't come back and repost the form data.  Should be used whenever anything is posted.
function redirectexit($setLocation = '', $refresh = false)
{
	global $scripturl, $context, $modSettings, $db_show_debug;

	$add = preg_match('~^(ftp|http)[s]?://~', $setLocation) == 0 && substr($setLocation, 0, 6) != 'about:';

	if (WIRELESS)
	{
		// Add the scripturl on if needed.
		if ($add)
			$setLocation = $scripturl . '?' . $setLocation;

		$char = strpos($setLocation, '?') === false ? '?' : ';';

		if (strpos($setLocation, '#') ==! false)
			$setLocation = strtr($setLocation, array('#' => $char . WIRELESS_PROTOCOL . '#'));
		else
			$setLocation .= $char . WIRELESS_PROTOCOL;
	}
	elseif ($add)
		$setLocation = $scripturl . ($setLocation != '' ? '?' . $setLocation : '');

	// Put the session ID in.
	if (defined('SID') && SID != '')
		$setLocation = preg_replace('/^' . preg_quote($scripturl, '/') . '(?!\?' . preg_quote(SID, '/') . ')(\?)?/', $scripturl . '?' . SID . ';', $setLocation);
	// Keep that debug in their for template debugging!
	elseif (isset($_GET['debug']))
		$setLocation = preg_replace('/^' . preg_quote($scripturl, '/') . '(\?)?/', $scripturl . '?debug;', $setLocation);

	if (!empty($modSettings['queryless_urls']) && (empty($context['server']['is_cgi']) || @ini_get('cgi.fix_pathinfo') == 1) && !empty($context['server']['is_apache']))
	{
		if (defined('SID') && SID != '')
			$setLocation = preg_replace('/^' . preg_quote($scripturl, '/') . '\?(?:' . SID . ';)((?:board|topic)=[^#]+?)(#[^"]*?)?$/e', "\$scripturl . '/' . strtr('\$1', '&;=', '//,') . '.html\$2?' . SID", $setLocation);
		else
			$setLocation = preg_replace('/^' . preg_quote($scripturl, '/') . '\?((?:board|topic)=[^#"]+?)(#[^"]*?)?$/e', "\$scripturl . '/' . strtr('\$1', '&;=', '//,') . '.html\$2'", $setLocation);
	}

	if (isset($modSettings['integrate_redirect']) && function_exists($modSettings['integrate_redirect']))
		$modSettings['integrate_redirect']($setLocation, $refresh);

	// We send a Refresh header only in special cases because Location looks better. (and is quicker...)
	if ($refresh)
		header('Refresh: 0; URL=' . strtr($setLocation, array(' ' => '%20', ';' => '%3b')));
	else
		header('Location: ' . str_replace(' ', '%20', $setLocation));

	// Debugging.
	if (isset($db_show_debug) && $db_show_debug === true)
		$_SESSION['debug_redirect'] = &$GLOBALS['db_cache'];

	obExit(false);
}

// Ends execution.  Takes care of template loading and remembering the previous URL.
function obExit($header = null, $do_footer = null, $from_index = false)
{
	global $context, $settings, $modSettings, $txt;
	static $header_done = false, $footer_done = false;

	// Clear out the stat cache.
	trackStats();

	$do_header = $header === null ? !$header_done : $header;
	if ($do_footer === null)
		$do_footer = $do_header;

	// Has the template/header been done yet?
	if ($do_header)
	{
		// Start up the session URL fixer.
		ob_start('ob_sessrewrite');

		// Just in case we have anything bad already in there...
		if ((isset($_REQUEST['debug']) || isset($_REQUEST['xml']) || (WIRELESS && WIRELESS_PROTOCOL == 'wap')) && in_array($txt['lang_locale'], array('UTF-8', 'ISO-8859-1')))
			ob_start('validate_unicode__recursive');

		if (!empty($settings['output_buffers']) && is_string($settings['output_buffers']))
			$buffers = explode(',', $settings['output_buffers']);
		elseif (!empty($settings['output_buffers']))
			$buffers = $settings['output_buffers'];
		else
			$buffers = array();

		if (isset($modSettings['integrate_buffer']))
			$buffers = array_merge(explode(',', $modSettings['integrate_buffer']), $buffers);

		if (!empty($buffers))
			foreach ($buffers as $buffer_function)
			{
				if (function_exists(trim($buffer_function)))
					ob_start(trim($buffer_function));
			}

		// Display the screen in the logical order.
		template_header();
		$header_done = true;
	}
	if ($do_footer)
	{
		if (WIRELESS && !isset($context['sub_template']))
			fatal_lang_error('wireless_error_notyet', false);

		// Just show the footer, then.
		loadSubTemplate(isset($context['sub_template']) ? $context['sub_template'] : 'main');

		// Just so we don't get caught in an endless loop of errors from the footer...
		if (!$footer_done)
		{
			$footer_done = true;
			template_footer();

			// (since this is just debugging... it's okay that it's after </html>.)
			if (!isset($_REQUEST['xml']))
				db_debug_junk();
		}
	}

	// Remember this URL in case someone doesn't like sending HTTP_REFERER.
	if (strpos($_SERVER['REQUEST_URL'], 'action=dlattach') === false)
		$_SESSION['old_url'] = $_SERVER['REQUEST_URL'];

	// For session check verfication.... don't switch browsers...
	$_SESSION['USER_AGENT'] = $_SERVER['HTTP_USER_AGENT'];

	// Hand off the output to the portal, etc. we're integrated with.
	if (isset($modSettings['integrate_exit'], $context['template_layers']) && in_array('main', $context['template_layers']) && function_exists($modSettings['integrate_exit']))
		call_user_func($modSettings['integrate_exit'], $do_footer && !WIRELESS);

	// Don't exit if we're coming from index.php; that will pass through normally.
	if (!$from_index || WIRELESS)
		exit;
}

// Set up the administration sections.
function adminIndex($area)
{
	global $txt, $context, $scripturl, $sc, $modSettings, $user_info, $settings;

	// Load the language and templates....
	loadLanguage('Admin');
	loadTemplate('Admin');

	// Admin area 'Main'.
	$context['admin_areas']['forum'] = array(
		'title' => $txt[427],
		'areas' => array(
			'index' => '<a href="' . $scripturl . '?action=admin">' . $txt[208] . '</a>',
			'credits' => '<a href="' . $scripturl . '?action=admin;credits">' . $txt['support_credits_title'] . '</a>',
		)
	);

	if (allowedTo(array('edit_news', 'send_mail', 'admin_forum')))
		$context['admin_areas']['forum']['areas']['news'] = '<a href="' . $scripturl . '?action=news">' . $txt['news_title'] . '</a>';

	if (allowedTo('admin_forum'))
		$context['admin_areas']['forum']['areas']['manage_packages'] =  '<a href="' . $scripturl . '?action=packages">' . $txt['package1'] . '</a>';

	// Admin area 'Configuration'.
	if (allowedTo('admin_forum'))
	{
		$context['admin_areas']['config'] = array(
			'title' => $txt[428],
			'areas' => array(
				'edit_mods_settings' => '<a href="' . $scripturl . '?action=featuresettings">' . $txt['modSettings_title'] . '</a>',
				'edit_settings' => '<a href="' . $scripturl . '?action=serversettings;sesc=' . $sc . '">' . $txt[222] . '</a>',
				'edit_theme_settings' => '<a href="' . $scripturl . '?action=theme;sa=settings;th=' . $settings['theme_id'] . ';sesc=' . $sc . '">' . $txt['theme_current_settings'] . '</a>',
				'manage_themes' => '<a href="' . $scripturl . '?action=theme;sa=admin;sesc=' . $sc . '">' . $txt['theme_admin'] . '</a>',
			)
		);
	}

	// Admin area 'Forum'.
	if (allowedTo(array('manage_boards', 'admin_forum', 'manage_smileys', 'manage_attachments', 'moderate_forum')))
	{
		$context['admin_areas']['layout'] = array(
			'title' => $txt['layout_controls'],
			'areas' => array()
		);

		if (allowedTo('manage_boards'))
			$context['admin_areas']['layout']['areas']['manage_boards'] =  '<a href="' . $scripturl . '?action=manageboards">' . $txt[4] . '</a>';

		if (allowedTo(array('admin_forum', 'moderate_forum')))
			$context['admin_areas']['layout']['areas']['posts_and_topics'] = '<a href="' . $scripturl . '?action=postsettings">' . $txt['manageposts'] . '</a>';
		if (allowedTo('admin_forum'))
		{
			$context['admin_areas']['layout']['areas']['manage_calendar'] = '<a href="' . $scripturl . '?action=managecalendar">' . $txt['manage_calendar'] . '</a>';
			$context['admin_areas']['layout']['areas']['manage_search'] = '<a href="' . $scripturl . '?action=managesearch">' . $txt['manage_search'] . '</a>';
		}
		if (allowedTo('manage_smileys'))
			$context['admin_areas']['layout']['areas']['manage_smileys'] = '<a href="' . $scripturl . '?action=smileys">' . $txt['smileys_manage'] . '</a>';

		if (allowedTo('manage_attachments'))
			$context['admin_areas']['layout']['areas']['manage_attachments'] = '<a href="' . $scripturl . '?action=manageattachments">' . $txt['smf201'] . '</a>';
	}

	// Admin area 'Members'.
	if (allowedTo(array('moderate_forum', 'manage_membergroups', 'manage_bans', 'manage_permissions', 'admin_forum')))
	{
		$context['admin_areas']['members'] = array(
			'title' => $txt[426],
			'areas' => array()
		);

		if (allowedTo('moderate_forum'))
			$context['admin_areas']['members']['areas']['view_members'] = '<a href="' . $scripturl . '?action=viewmembers">' . $txt[5] . '</a>';

		if (allowedTo('manage_membergroups'))
			$context['admin_areas']['members']['areas']['edit_groups'] = '<a href="' . $scripturl . '?action=membergroups;">' . $txt[8] . '</a>';

		if (allowedTo('manage_permissions'))
			$context['admin_areas']['members']['areas']['edit_permissions'] = '<a href="' . $scripturl . '?action=permissions">' . $txt['edit_permissions'] . '</a>';

		if (allowedTo(array('admin_forum', 'moderate_forum')))
			$context['admin_areas']['members']['areas']['registration_center'] = '<a href="' . $scripturl . '?action=regcenter">' . $txt['registration_center'] . '</a>';

		if (allowedTo('manage_bans'))
			$context['admin_areas']['members']['areas']['ban_members'] = '<a href="' . $scripturl . '?action=ban">' . $txt['ban_title'] . '</a>';
	}

	// Admin area 'Maintenance Controls'.
	if (allowedTo('admin_forum'))
	{
		$context['admin_areas']['maintenance'] = array(
			'title' => $txt[501],
			'areas' => array(
				'maintain_forum' => '<a href="' . $scripturl . '?action=maintain">' . $txt['maintain_title'] . '</a>',
				'generate_reports' => '<a href="' . $scripturl . '?action=reports">' . $txt['generate_reports'] . '</a>',
				// !!! Why?  Don't you want to take care of errors in the order they happened, like normal people and programmers?  Seeing them backwards helps no one, and only increases confusion.
				// !!! You know I've argued this before.
				'view_errors' => '<a href="' . $scripturl . '?action=viewErrorLog;desc">' . $txt['errlog1'] . '</a>'
			)
		);

		if (!empty($modSettings['modlog_enabled']))
			$context['admin_areas']['maintenance']['areas']['view_moderation_log'] = '<a href="' . $scripturl . '?action=modlog">' . $txt['modlog_view'] . '</a>';
	}

	// Make sure the administrator has a valid session...
	validateSession();

	// Figure out which one we're in now...
	foreach ($context['admin_areas'] as $id => $section)
		if (isset($section[$area]))
			$context['admin_section'] = $id;
	$context['admin_area'] = $area;

	// obExit will know what to do!
	$context['template_layers'][] = 'admin';
}

// Usage: logAction('remove', array('starter' => $ID_MEMBER_STARTED));
function logAction($action, $extra = array())
{
	global $db_prefix, $ID_MEMBER, $modSettings, $user_info;

	if (!is_array($extra))
		trigger_error('logAction(): data is not an array with action \'' . $action . '\'', E_USER_NOTICE);

	if (isset($extra['topic']) && !is_numeric($extra['topic']))
		trigger_error('logAction(): data\'s topic is not an number', E_USER_NOTICE);
	if (isset($extra['member']) && !is_numeric($extra['member']))
		trigger_error('logAction(): data\'s member is not an number', E_USER_NOTICE);

	if (!empty($modSettings['modlog_enabled']))
	{
		db_query("
			INSERT INTO {$db_prefix}log_actions
				(logTime, ID_MEMBER, ip, action, extra)
			VALUES (" . time() . ", $ID_MEMBER, SUBSTRING('$user_info[ip]', 1, 16), SUBSTRING('$action', 1, 30),
				SUBSTRING('" . addslashes(serialize($extra)) . "', 1, 65534))", __FILE__, __LINE__);

		return db_insert_id();
	}

	return false;
}

// Track Statistics.
function trackStats($stats = array())
{
	global $db_prefix, $modSettings;
	static $cache_stats = array();

	if (empty($modSettings['trackStats']))
		return false;
	if (!empty($stats))
		return $cache_stats = array_merge($cache_stats, $stats);
	elseif (empty($cache_stats))
		return false;

	$setStringUpdate = '';
	foreach ($cache_stats as $field => $change)
	{
		$setStringUpdate .= '
			' . $field . ' = ' . ($change === '+' ? $field . ' + 1' : $change) . ',';

		if ($change === '+')
			$cache_stats[$field] = 1;
	}

	$date = strftime('%Y-%m-%d', forum_time(false));
	db_query("
		UPDATE {$db_prefix}log_activity
		SET" . substr($setStringUpdate, 0, -1) . "
		WHERE date = '$date'
		LIMIT 1", __FILE__, __LINE__);
	if (db_affected_rows() == 0)
	{
		db_query("
			INSERT IGNORE INTO {$db_prefix}log_activity
				(date, " . implode(', ', array_keys($cache_stats)) . ")
			VALUES ('$date', " . implode(', ', $cache_stats) . ')', __FILE__, __LINE__);
	}

	// Don't do this again.
	$cache_stats = array();

	return true;
}

// Make sure the user isn't posting over and over again.
function spamProtection($error_type)
{
	global $modSettings, $txt, $db_prefix, $user_info;

	// Delete old entries... if you can moderate this board or this is login, override spamWaitTime with 2.
	if ($error_type == 'spam' && !allowedTo('moderate_board'))
		db_query("
			DELETE FROM {$db_prefix}log_floodcontrol
			WHERE logTime < " . (time() - $modSettings['spamWaitTime']), __FILE__, __LINE__);
	else
		db_query("
			DELETE FROM {$db_prefix}log_floodcontrol
			WHERE (logTime < " . (time() - 2) . " AND ip = '$user_info[ip]')
				OR logTime < " . (time() - $modSettings['spamWaitTime']), __FILE__, __LINE__);

	// Add a new entry, deleting the old if necessary.
	db_query("
		REPLACE INTO {$db_prefix}log_floodcontrol
			(ip, logTime)
		VALUES (SUBSTRING('$user_info[ip]', 1, 16), " . time() . ")", __FILE__, __LINE__);
	// If affected is 0 or 2, it was there already.
	if (db_affected_rows() != 1)
	{
		// Spammer!  You only have to wait a *few* seconds!
		fatal_lang_error($error_type . 'WaitTime_broken', false, array($modSettings['spamWaitTime']));
		return true;
	}

	// They haven't posted within the limit.
	return false;
}

// Get the size of a specified image with better error handling.
function url_image_size($url)
{
	global $sourcedir;

	// Can we pull this from the cache... please please?
	if (($temp = cache_get_data('url_image_size-' . md5($url), 240)) !== null)
		return $temp;
	$t = microtime();

	// Get the host to pester...
	preg_match('~^\w+://(.+?)/(.*)$~', $url, $match);

	// Can't figure it out, just try the image size.
	if ($url == '' || $url == 'http://' || $url == 'https://')
		return false;
	elseif (!isset($match[1]))
		$size = @getimagesize($url);
	else
	{
		// Try to connect to the server... give it half a second.
		$temp = 0;
		$fp = @fsockopen($match[1], 80, $temp, $temp, 0.5);

		// Successful?  Continue...
		if ($fp != false)
		{
			// Send the HEAD request (since we don't have to worry about chunked, HTTP/1.1 is fine here.)
			fwrite($fp, 'HEAD /' . $match[2] . ' HTTP/1.1' . "\r\n" . 'Host: ' . $match[1] . "\r\n" . 'User-Agent: PHP/SMF' . "\r\n" . 'Connection: close' . "\r\n\r\n");

			// Read in the HTTP/1.1 or whatever.
			$test = substr(fgets($fp, 11), -1);
			fclose($fp);

			// See if it returned a 404/403 or something.
			if ($test < 4)
			{
				$size = @getimagesize($url);

				// This probably means allow_url_fopen is off, let's try GD.
				if ($size === false && function_exists('imagecreatefromstring'))
				{
					include_once($sourcedir . '/Subs-Package.php');

					// It's going to hate us for doing this, but another request...
					$image = @imagecreatefromstring(fetch_web_data($url));
					if ($image !== false)
					{
						$size = array(imagesx($image), imagesy($image));
						imagedestroy($image);
					}
				}
			}
		}
	}

	// If we didn't get it, we failed.
	if (!isset($size))
		$size = false;

	// If this took a long time, we may never have to do it again, but then again we might...
	if (array_sum(explode(' ', microtime())) - array_sum(explode(' ', $t)) > 0.8)
		cache_put_data('url_image_size-' . md5($url), $size, 240);

	// Didn't work.
	return $size;
}

function determineTopicClass(&$topic_context)
{
	// Set topic class depending on locked status and number of replies.
	if ($topic_context['is_very_hot'])
		$topic_context['class'] = 'veryhot';
	elseif ($topic_context['is_hot'])
		$topic_context['class'] = 'hot';
	else
		$topic_context['class'] = 'normal';

	$topic_context['class'] .= $topic_context['is_poll'] ? '_poll' : '_post';

	if ($topic_context['is_locked'])
		$topic_context['class'] .= '_locked';

	if ($topic_context['is_sticky'])
		$topic_context['class'] .= '_sticky';

	// This is so old themes will still work.
	$topic_context['extended_class'] = &$topic_context['class'];
}

// Sets up the basic theme context stuff.
function setupThemeContext()
{
	global $modSettings, $user_info, $scripturl, $context, $settings, $options, $txt, $maintenance;

	// Get some news...
	$context['news_lines'] = explode("\n", str_replace("\r", '', trim(addslashes($modSettings['news']))));
	$context['fader_news_lines'] = array();
	for ($i = 0, $n = count($context['news_lines']); $i < $n; $i++)
	{
		if (trim($context['news_lines'][$i]) == '')
			continue;

		// Clean it up for presentation ;).
		$context['news_lines'][$i] = parse_bbc(stripslashes(trim($context['news_lines'][$i])), true, 'news' . $i);

		// Gotta be special for the javascript.
		$context['fader_news_lines'][$i] = strtr(addslashes($context['news_lines'][$i]), array('/' => '\/', '<a href=' => '<a hre" + "f='));
	}
	$context['random_news_line'] = $context['news_lines'][rand(0, count($context['news_lines']) - 1)];

	if (!$user_info['is_guest'])
	{
		$context['user']['messages'] = &$user_info['messages'];
		$context['user']['unread_messages'] = &$user_info['unread_messages'];

		// Personal message popup...
		if ($user_info['unread_messages'] > (isset($_SESSION['unread_messages']) ? $_SESSION['unread_messages'] : 0))
			$context['user']['popup_messages'] = true;
		else
			$context['user']['popup_messages'] = false;
		$_SESSION['unread_messages'] = $user_info['unread_messages'];

		if (allowedTo('moderate_forum'))
			$context['unapproved_members'] = !empty($modSettings['registration_method']) && $modSettings['registration_method'] == 2 ? $modSettings['unapprovedMembers'] : 0;

		$context['user']['avatar'] = array();

		// Figure out the avatar... uploaded?
		if ($user_info['avatar']['url'] == '' && !empty($user_info['avatar']['ID_ATTACH']))
			$context['user']['avatar']['href'] = $user_info['avatar']['custom_dir'] ? $modSettings['custom_avatar_url'] . '/' . $user_info['avatar']['filename'] : $scripturl . '?action=dlattach;attach=' . $user_info['avatar']['ID_ATTACH'] . ';type=avatar';
		// Full URL?
		elseif (substr($user_info['avatar']['url'], 0, 7) == 'http://')
		{
			$context['user']['avatar']['href'] = $user_info['avatar']['url'];

			if ($modSettings['avatar_action_too_large'] == 'option_html_resize' || $modSettings['avatar_action_too_large'] == 'option_js_resize')
			{
				if (!empty($modSettings['avatar_max_width_external']))
					$context['user']['avatar']['width'] = $modSettings['avatar_max_width_external'];
				if (!empty($modSettings['avatar_max_height_external']))
					$context['user']['avatar']['height'] = $modSettings['avatar_max_height_external'];
			}
		}
		// Otherwise we assume it's server stored?
		elseif ($user_info['avatar']['url'] != '')
			$context['user']['avatar']['href'] = $modSettings['avatar_url'] . '/' . htmlspecialchars($user_info['avatar']['url']);

		if (!empty($context['user']['avatar']))
			$context['user']['avatar']['image'] = '<img src="' . $context['user']['avatar']['href'] . '"' . (isset($context['user']['avatar']['width']) ? ' width="' . $context['user']['avatar']['width'] . '"' : '') . (isset($context['user']['avatar']['height']) ? ' height="' . $context['user']['avatar']['height'] . '"' : '') . ' alt="" class="avatar" border="0" />';

		// Figure out how long they've been logged in.
		$context['user']['total_time_logged_in'] = array(
			'days' => floor($user_info['total_time_logged_in'] / 86400),
			'hours' => floor(($user_info['total_time_logged_in'] % 86400) / 3600),
			'minutes' => floor(($user_info['total_time_logged_in'] % 3600) / 60)
		);
	}
	else
	{
		$context['user']['messages'] = 0;
		$context['user']['unread_messages'] = 0;
		$context['user']['avatar'] = array();
		$context['user']['total_time_logged_in'] = array('days' => 0, 'hours' => 0, 'minutes' => 0);
		$context['user']['popup_messages'] = false;

		if (!empty($modSettings['registration_method']) && $modSettings['registration_method'] == 1)
			$txt['welcome_guest'] .= $txt['welcome_guest_activate'];

		// If we've upgraded recently, go easy on the passwords.
		if (!empty($modSettings['disableHashTime']) && ($modSettings['disableHashTime'] == 1 || time() < $modSettings['disableHashTime']))
			$context['disable_login_hashing'] = true;
		elseif ($context['browser']['is_ie5'] || $context['browser']['is_ie5.5'])
			$context['disable_login_hashing'] = true;
	}

	// Set up the menu privileges.
	$context['allow_search'] = allowedTo('search_posts');
	$context['allow_admin'] = allowedTo(array('admin_forum', 'manage_boards', 'manage_permissions', 'moderate_forum', 'manage_membergroups', 'manage_bans', 'send_mail', 'edit_news', 'manage_attachments', 'manage_smileys'));
	$context['allow_edit_profile'] = !$user_info['is_guest'] && allowedTo(array('profile_view_own', 'profile_view_any', 'profile_identity_own', 'profile_identity_any', 'profile_extra_own', 'profile_extra_any', 'profile_remove_own', 'profile_remove_any', 'moderate_forum', 'manage_membergroups'));
	$context['allow_memberlist'] = allowedTo('view_mlist');
	$context['allow_calendar'] = allowedTo('calendar_view') && !empty($modSettings['cal_enabled']);

	$context['allow_pm'] = allowedTo('pm_read');

	$context['in_maintenance'] = !empty($maintenance);
	$context['current_time'] = timeformat(time(), false);
	$context['current_action'] = isset($_GET['action']) ? $_GET['action'] : '';
	$context['show_quick_login'] = !empty($modSettings['enableVBStyleLogin']) && $user_info['is_guest'];

	if (empty($settings['theme_version']))
		$context['show_vBlogin'] = $context['show_quick_login'];

	// This is here because old index templates might still use it.
	$context['show_news'] = !empty($settings['enable_news']);

	// This is done to make it easier to add to all themes...
	if ($context['user']['popup_messages'] && !empty($options['popup_messages']) && (!isset($_REQUEST['action']) || $_REQUEST['action'] != 'pm'))
	{
		$context['html_headers'] .= '
	<script language="JavaScript" type="text/javascript"><!-- // --><![CDATA[
		if (confirm("' . $txt['show_personal_messages'] . '"))
			window.open("' . $scripturl . '?action=pm");
	// ]]></script>';
	}

	// Resize avatars the fancy, but non-GD requiring way.
	if ($modSettings['avatar_action_too_large'] == 'option_js_resize' && (!empty($modSettings['avatar_max_width_external']) || !empty($modSettings['avatar_max_height_external'])))
	{
		$context['html_headers'] .= '
	<script language="JavaScript" type="text/javascript"><!-- // --><![CDATA[
		var smf_avatarMaxWidth = ' . (int) $modSettings['avatar_max_width_external'] . ';
		var smf_avatarMaxHeight = ' . (int) $modSettings['avatar_max_height_external'] . ';';

		if (!$context['browser']['is_ie'] && !$context['browser']['is_mac_ie'])
			$context['html_headers'] .= '
	window.addEventListener("load", smf_avatarResize, false);';
		else
			$context['html_headers'] .= '
	var window_oldAvatarOnload = window.onload;
	window.onload = smf_avatarResize;';

		// !!! Move this over to script.js?
		$context['html_headers'] .= '
	// ]]></script>';
	}

	// This looks weird, but it's because BoardIndex.php references the variable.
	$context['common_stats']['latest_member'] = array(
		'id' => $modSettings['latestMember'],
		'name' => $modSettings['latestRealName'],
		'href' => $scripturl . '?action=profile;u=' . $modSettings['latestMember'],
		'link' => '<a href="' . $scripturl . '?action=profile;u=' . $modSettings['latestMember'] . '">' . $modSettings['latestRealName'] . '</a>',
	);
	$context['common_stats'] = array(
		'total_posts' => comma_format($modSettings['totalMessages']),
		'total_topics' => comma_format($modSettings['totalTopics']),
		'total_members' => comma_format($modSettings['totalMembers']),
		'latest_member' => $context['common_stats']['latest_member'],
	);

	if (empty($settings['theme_version']))
		$context['html_headers'] .= '
	<script language="JavaScript" type="text/javascript"><!-- // --><![CDATA[
		var smf_scripturl = "' . $scripturl . '";
	// ]]></script>';

	if (!isset($context['page_title']))
		$context['page_title'] = '';
}

// This is the only template included in the sources...
function template_rawdata()
{
	global $context;

	echo $context['raw_data'];
}

function template_header()
{
	global $txt, $modSettings, $context, $settings, $user_info, $boarddir;

	setupThemeContext();

	// Print stuff to prevent caching of pages (except on attachment errors, etc.)
	if (empty($context['no_last_modified']))
	{
		header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
		header('Last-Modified: ' . gmdate('D, d M Y H:i:s') . ' GMT');

		// Are we debugging the template/html content?
		if (!isset($_REQUEST['xml']) && isset($_GET['debug']) && !$context['browser']['is_ie'] && !WIRELESS)
			header('Content-Type: application/xhtml+xml');
		elseif (!isset($_REQUEST['xml']) && !WIRELESS)
			header('Content-Type: text/html; charset=' . (empty($context['character_set']) ? 'ISO-8859-1' : $context['character_set']));
	}

	header('Content-Type: text/' . (isset($_REQUEST['xml']) ? 'xml' : 'html') . '; charset=' . (empty($context['character_set']) ? 'ISO-8859-1' : $context['character_set']));

	foreach ($context['template_layers'] as $layer)
	{
		loadSubTemplate($layer . '_above', true);

		// May seem contrived, but this is done in case the main layer isn't there...
		if ($layer == 'main' && allowedTo('admin_forum') && !$user_info['is_guest'])
		{
			$securityFiles = array('install.php', 'webinstall.php', 'upgrade.php', 'convert.php', 'repair_paths.php', 'repair_settings.php');
			foreach ($securityFiles as $i => $securityFile)
			{
				if (!file_exists($boarddir . '/' . $securityFile))
					unset($securityFiles[$i]);
			}

			if (!empty($securityFiles))
			{
				echo '
		<div style="margin: 2ex; padding: 2ex; border: 2px dashed #cc3344; color: black; background-color: #ffe4e9;">
			<div style="float: left; width: 2ex; font-size: 2em; color: red;">!!</div>
			<b style="text-decoration: underline;">', $txt['smf299'], '</b><br />
			<div style="padding-left: 6ex;">';

				foreach ($securityFiles as $securityFile)
					echo '
			', $txt['smf300'], '<b>', $securityFile, '</b>!<br />';

				echo '
			</div>
		</div>';
			}
		}
		// If the user is banned from posting inform them of it.
		elseif ($layer == 'main' && isset($_SESSION['ban']['cannot_post']))
		{
			echo '
				<div class="windowbg" style="margin: 2ex; padding: 2ex; border: 2px dashed red; color: red;">
					', sprintf($txt['you_are_post_banned'], $user_info['is_guest'] ? $txt[28] : $user_info['name']);

			if (!empty($_SESSION['ban']['cannot_post']['reason']))
				echo '
					<div style="padding-left: 4ex; padding-top: 1ex;">', $_SESSION['ban']['cannot_post']['reason'], '</div>';

			echo '
				</div>';
		}
	}

	if (isset($settings['use_default_images']) && $settings['use_default_images'] == 'defaults' && isset($settings['default_template']))
	{
		$settings['theme_url'] = $settings['default_theme_url'];
		$settings['images_url'] = $settings['default_images_url'];
		$settings['theme_dir'] = $settings['default_theme_dir'];
	}
}

// Show the copyright...
function theme_copyright($get_it = false)
{
	global $forum_copyright, $context, $boardurl, $forum_version, $txt, $modSettings;
	static $found = false;

	// DO NOT MODIFY THIS FUNCTION.  DO NOT REMOVE YOUR COPYRIGHT.
	// DOING SO VOIDS YOUR LICENSE AND IS ILLEGAL.

	// Meaning, this is the footer checking in..
	if ($get_it === true)
		return $found;

	// Naughty, naughty.
	if (rand(0, 2) == 1)
	{
		$temporary = preg_replace('~<!--.+?-->~s', '', ob_get_contents());
		if (strpos($temporary, '<!--') !== false)
			echo '-->';
	}

	// For SSI and other things, detect the version.
	if (!isset($forum_version) || strpos($forum_version, 'SMF') === false || isset($_GET['checkcopyright']))
	{
		$data = substr(file_get_contents(__FILE__), 0, 4096);
		if (preg_match('~\*\s*Software\s+Version:\s+(SMF\s+.+?)[\s]{2}~i', $data, $match) == 0)
			$match = array('', 'SMF');
		$forum_copyright = preg_replace('~(<a href="http://www.simplemachines.org/"[^>]+>)</a>~', '$1' . $match[1] . '</a>', $forum_copyright);
	}

	// Lewis Media no longer holds the copyright.
	$forum_copyright = str_replace(array('Lewis Media', 'href="http://www.lewismedia.com/"', '2001-'), array('Simple Machines LLC', 'href="http://www.simplemachines.org/about/copyright.php" title="Free Forum Software"', ''), $forum_copyright);

	echo '
		<span class="smalltext" style="display: inline; visibility: visible; font-family: Verdana, Arial, sans-serif;">';

	if ($get_it == 'none')
	{
		$found = true;
		echo '
			<div style="white-space: normal;">The administrator doesn\'t want a copyright notice saying this is copyright 2006 by <a href="http://www.simplemachines.org/about/copyright.php" target="_blank">Simple Machines LLC</a>, and named <a href="http://www.simplemachines.org/">SMF</a>, so the forum will honor this request and be quiet.</div>';
	}
	// If it's in the copyright, and we are outputting it... it's been found.
	elseif (isset($modSettings['copyright_key']) && sha1($modSettings['copyright_key'] . 'banjo') == '1d01885ece7a9355bdeb22ed107f0ffa8c323026'){$found = true; return;}elseif ((strpos($forum_copyright, '<a href="http://www.simplemachines.org/" title="Simple Machines Forum" target="_blank">Powered by SMF') !== false || strpos($forum_copyright, '<a href="http://www.simplemachines.org/" onclick="this.href += \'referer.php?forum=' . urlencode($context['forum_name'] . '|' . $boardurl . '|' . $forum_version) . '\';" target="_blank">SMF') !== false || strpos($forum_copyright, '<a href="http://www.simplemachines.org/" target="_blank">SMF') !== false || strpos($forum_copyright, '<a href="http://www.simplemachines.org/" title="Simple Machines Forum" target="_blank">SMF') !== false)&&((strpos($forum_copyright, '<a href="http://www.simplemachines.org/about/copyright.php" title="Free Forum Software" target="_blank">SMF &copy;') !== false && (strpos($forum_copyright, 'Lewis Media</a>') !== false || strpos($forum_copyright, 'Simple Machines LLC</a>') !== false)) || strpos($forum_copyright, '<a href="http://www.lewismedia.com/">Lewis Media</a>') !== false || strpos($forum_copyright, '<a href="http://www.lewismedia.com/" target="_blank">Lewis Media</a>') !== false || (strpos($forum_copyright, '<a href="http://www.simplemachines.org/about/copyright.php"') !== false &&	strpos($forum_copyright, 'Simple Machines LLC') !== false))){$found = true; echo $forum_copyright;}

	echo '
		</span>';
}

function template_footer()
{
	global $context, $settings, $modSettings, $time_start, $db_count;

	// Show the load time?  (only makes sense for the footer.)
	$context['show_load_time'] = !empty($modSettings['timeLoadPageEnable']);
	$context['load_time'] = round(array_sum(explode(' ', microtime())) - array_sum(explode(' ', $time_start)), 3);
	$context['load_queries'] = $db_count;

	if (isset($settings['use_default_images']) && $settings['use_default_images'] == 'defaults' && isset($settings['default_template']))
	{
		$settings['theme_url'] = $settings['actual_theme_url'];
		$settings['images_url'] = $settings['actual_images_url'];
		$settings['theme_dir'] = $settings['actual_theme_dir'];
	}

	foreach (array_reverse($context['template_layers']) as $layer)
		loadSubTemplate($layer . '_below', true);

	// Do not remove hard-coded text - it's in here so users cannot change the text easily. (as if it were in language file)
	if (!theme_copyright(true) && !empty($context['template_layers']) && SMF !== 'SSI' && !WIRELESS)
	{
		// DO NOT MODIFY THIS SECTION.  DO NOT REMOVE YOUR COPYRIGHT.
		// DOING SO VOIDS YOUR LICENSE AND IS ILLEGAL.

		echo '
			<div style="text-align: center !important; display: block !important; visibility: visible !important; font-size: large !important; font-weight: bold; color: black !important; background-color: white !important;">
				Sorry, the copyright must be in the template.<br />
				Please notify this forum\'s administrator that this site is missing the copyright message for <a href="http://www.simplemachines.org/" style="color: black !important; font-size: large !important;">SMF</a> so they can rectify the situation. Display of copyright is a <a href="http://www.simplemachines.org/about/license.php" style="color: red;">legal requirement</a>. For more information on this please visit the <a href="http://www.simplemachines.org">Simple Machines</a> website.', empty($context['user']['is_admin']) ? '' : '<br />
				Not sure why this message is appearing?  <a href="http://www.simplemachines.org/redirect/index.php?copyright_error">Take a look at some common causes.</a>', '
			</div>';

		log_error('Copyright removed!!');
	}
}

// Debugging.
function db_debug_junk()
{
	global $context, $scripturl, $boarddir, $modSettings;
	global $db_cache, $db_count, $db_show_debug, $cache_count, $cache_hits;

	// Add to Settings.php if you want to show the debugging information.
	if (!isset($db_show_debug) || $db_show_debug !== true || (isset($_GET['action']) && $_GET['action'] == 'viewquery') || WIRELESS)
		return;

	if (empty($_SESSION['view_queries']))
		$_SESSION['view_queries'] = 0;
	if (empty($context['debug']['language_files']))
		$context['debug']['language_files'] = array();

	$files = get_included_files();
	$total_size = 0;
	for ($i = 0, $n = count($files); $i < $n; $i++)
	{
		$total_size += filesize($files[$i]);
		$files[$i] = strtr($files[$i], array($boarddir => '.'));
	}

	$warnings = 0;
	foreach ($db_cache as $q => $qq)
	{
		if (!empty($qq['w']))
			$warnings += count($qq['w']);
	}

	$_SESSION['debug'] = &$db_cache;

	// Gotta have valid HTML ;).
	$temp = ob_get_contents();
	if (function_exists('ob_clean'))
		ob_clean();
	else
	{
		ob_end_clean();
		ob_start('ob_sessrewrite');
	}

	echo preg_replace('~</body>\s*</html>~', '', $temp), '
<div class="smalltext" style="text-align: left; margin: 1ex;">
	Templates: ', count($context['debug']['templates']), ': <i>', implode('</i>, <i>', $context['debug']['templates']), '</i>.<br />
	Sub templates: ', count($context['debug']['sub_templates']), ': <i>', implode('</i>, <i>', $context['debug']['sub_templates']), '</i>.<br />
	Language files: ', count($context['debug']['language_files']), ': <i>', implode('</i>, <i>', $context['debug']['language_files']), '</i>.<br />
	Files included: ', count($files), ' - ', round($total_size / 1024), 'KB. (<a href="javascript:void(0);" onclick="document.getElementById(\'debug_include_info\').style.display = \'inline\'; this.style.display = \'none\'; return false;">show</a><span id="debug_include_info" style="display: none;"><i>', implode('</i>, <i>', $files), '</i></span>)<br />';

	if (!empty($modSettings['cache_enable']) && !empty($cache_hits))
	{
		$entries = array();
		$total_t = 0;
		$total_s = 0;
		foreach ($cache_hits as $h)
		{
			$entries[] = $h['d'] . ' ' . $h['k'] . ': ' . comma_format($h['t'], 5) . ' - ' . $h['s'] . ' bytes';
			$total_t += $h['t'];
			$total_s += $h['s'];
		}

		echo '
	Cache hits: ', $cache_count, ': ', comma_format($total_t, 5), 's for ', comma_format($total_s), ' bytes (<a href="javascript:void(0);" onclick="document.getElementById(\'debug_cache_info\').style.display = \'inline\'; this.style.display = \'none\'; return false;">show</a><span id="debug_cache_info" style="display: none;"><i>', implode('</i>, <i>', $entries), '</i></span>)<br />';
	}

	echo '
	<a href="', $scripturl, '?action=viewquery" target="_blank">Queries used: ', $db_count, $warnings == 0 ? '' : ', ' . $warnings . ' warning(s)', '</a>.<br />
	<br />';

	if ($_SESSION['view_queries'] == 1)
		foreach ($db_cache as $q => $qq)
		{
			$is_select = substr(trim($qq['q']), 0, 6) == 'SELECT' || preg_match('~^INSERT(?: IGNORE)? INTO \w+(?:\s+\([^)]+\))?\s+SELECT .+$~s', trim($qq['q'])) != 0;

			echo '
	<b>', $is_select ? '<a href="' . $scripturl . '?action=viewquery;qq=' . ($q + 1) . '#qq' . $q . '" target="_blank" style="text-decoration: none;">' : '', nl2br(str_replace("\t", '&nbsp;&nbsp;&nbsp;', htmlspecialchars(ltrim($qq['q'], "\n\r")))) . ($is_select ? '</a></b>' : '</b>') . '<br />
	&nbsp;&nbsp;&nbsp;';
			if (!empty($qq['f']) && !empty($qq['l']))
				echo 'in <i>' . $qq['f'] . '</i> line <i>' . $qq['l'] . '</i>, ';
			echo 'which took ' . round($qq['t'], 8) . ' seconds.<br />
	<br />';
		}

	echo '
	<a href="' . $scripturl . '?action=viewquery;sa=hide">[' . (empty($_SESSION['view_queries']) ? 'show' : 'hide') . ' queries]</a>
</div></body></html>';
}

// Get an attachment's encrypted filename.  If $new is true, won't check for file existence.
function getAttachmentFilename($filename, $attachment_id, $new = false)
{
	global $modSettings;

	// Remove special accented characters - ie. s�.
	$clean_name = strtr($filename, '������������������������������������������������������������', 'SZszYAAAAAACEEEEIIIINOOOOOOUUUUYaaaaaaceeeeiiiinoooooouuuuyy');
	$clean_name = strtr($clean_name, array('�' => 'TH', '�' => 'th', '�' => 'DH', '�' => 'dh', '�' => 'ss', '�' => 'OE', '�' => 'oe', '�' => 'AE', '�' => 'ae', '�' => 'u'));

	// Sorry, no spaces, dots, or anything else but letters allowed.
	$clean_name = preg_replace(array('/\s/', '/[^\w_\.\-]/'), array('_', ''), $clean_name);

	$enc_name = $attachment_id . '_' . strtr($clean_name, '.', '_') . md5($clean_name);
	$clean_name = preg_replace('~\.[\.]+~', '.', $clean_name);

	if ($attachment_id == false || ($new && empty($modSettings['attachmentEncryptFilenames'])))
		return $clean_name;
	elseif ($new)
		return $enc_name;

	if (file_exists($modSettings['attachmentUploadDir'] . '/' . $enc_name))
		$filename = $modSettings['attachmentUploadDir'] . '/' . $enc_name;
	else
		$filename = $modSettings['attachmentUploadDir'] . '/' . $clean_name;

	return $filename;
}

// Lookup an IP; try shell_exec first because we can do a timeout on it.
function host_from_ip($ip)
{
	global $modSettings;

	if (($host = cache_get_data('hostlookup-' . $ip, 600)) !== null)
		return $host;
	$t = microtime();

	// If we can't access nslookup/host, PHP 4.1.x might just crash.
	if (@version_compare(PHP_VERSION, '4.2.0') == -1)
		$host = false;

	// Try the Linux host command, perhaps?
	if (!isset($host) && strpos(strtolower(PHP_OS), 'win') === false && rand(0, 1) == 1)
	{
		if (!isset($modSettings['host_to_dis']))
			$test = @shell_exec('host -W 1 ' . @escapeshellarg($ip));
		else
			$test = @shell_exec('host ' . @escapeshellarg($ip));

		// Did host say it didn't find anything?
		if (strpos($test, 'not found') !== false)
			$host = '';
		// Invalid server option?
		elseif (strpos($test, 'invalid option') && !isset($modSettings['host_to_dis']))
			updateSettings(array('host_to_dis' => 1));
		// Maybe it found something, after all?
		elseif (preg_match('~\s([^\s]+?)\.\s~', $test, $match) == 1)
			$host = $match[1];
	}

	// This is nslookup; usually only Windows, but possibly some Unix?
	if (!isset($host) && strpos(strtolower(PHP_OS), 'win') !== false && rand(0, 1) == 1)
	{
		$test = @shell_exec('nslookup -timeout=1 ' . @escapeshellarg($ip));
		if (strpos($test, 'Non-existent domain') !== false)
			$host = '';
		elseif (preg_match('~Name:\s+([^\s]+)~', $test, $match) == 1)
			$host = $match[1];
	}

	// This is the last try :/.
	if (!isset($host) || $host === false)
		$host = @gethostbyaddr($ip);

	// It took a long time, so let's cache it!
	if (array_sum(explode(' ', microtime())) - array_sum(explode(' ', $t)) > 0.5)
		cache_put_data('hostlookup-' . $ip, $host, 600);

	return $host;
}

// Chops a string into words and prepares them to be inserted into (or searched from) the database.
function text2words($text, $max_chars = 20, $encrypt = false)
{
	global $func, $context;

	// Step 1: Remove entities/things we don't consider words:
	$words = preg_replace('~([\x0B\0' . ($context['utf8'] ? ($context['server']['complex_preg_chars'] ? '\x{C2A0}' : chr(0xC2) . chr(0xA0)) : '\xA0') . '\t\r\s\n(){}\\[\\]<>!@$%^*.,:+=`\~\?/\\\\]|&(amp|lt|gt|quot);)+~' . ($context['utf8'] ? 'u' : ''), ' ', strtr($text, array('<br />' => ' ')));

	// Step 2: Entities we left to letters, where applicable, lowercase.
	$words = un_htmlspecialchars($func['strtolower']($words));

	// Step 3: Ready to split apart and index!
	$words = explode(' ', $words);

	if ($encrypt)
	{
		$possible_chars = array_flip(array_merge(range(46, 57), range(65, 90), range(97, 122)));
		$returned_ints = array();
		foreach ($words as $word)
		{
			if (($word = trim($word, '-_\'')) !== '')
			{
				$encrypted = substr(crypt($word, 'uk'), 2, $max_chars);
				$total = 0;
				for ($i = 0; $i < $max_chars; $i++)
					$total += $possible_chars[ord($encrypted{$i})] * pow(63, $i);
				$returned_ints[] = $max_chars == 4 ? min($total, 16777215) : $total;
			}
		}
		return array_unique($returned_ints);
	}
	else
	{
		// Trim characters before and after and add slashes for database insertion.
		$returned_words = array();
		foreach ($words as $word)
			if (($word = trim($word, '-_\'')) !== '')
				$returned_words[] = addslashes($max_chars === null ? $word : substr($word, 0, $max_chars));

		// Filter out all words that occur more than once.
		return array_unique($returned_words);
	}
}

// Creates an image/text button
function create_button($name, $alt, $label = '', $custom = '')
{
	global $settings, $txt, $context;

	if (!$settings['use_image_buttons'])
		return $txt[$alt];
	elseif (!empty($settings['use_buttons']))
		return '<img src="' . $settings['images_url'] . '/buttons/' . $name . '" alt="' . $txt[$alt] . '" ' . $custom . ' />' . ($label != '' ? '<b>' . $txt[$label] . '</b>' : '');
	else
		return '<img src="' . $settings['images_url'] . '/' . $context['user']['language'] . '/' . $name . '" alt="' . $txt[$alt] . '" ' . $custom . ' />';
}

// Creates an image/text button
function create_button2($name, $alt, $label = '', $custom = '')
{
	global $settings, $txt, $context;

	if (!$settings['use_image_buttons'])
		return $txt[$alt];
	elseif (!empty($settings['use_buttons']))
		return '<img src="' . $settings['images_url'] . '/buttons/' . $name . '" alt="' . $txt[$alt] . '" ' . $custom . ' />' . ($label != '' ? '<b>' . $txt[$label] . '</b>' : '');
	else
		return '<img src="' . $settings['images_url'] . '/' . $context['user']['language'] . '/' . $name . '" alt="' . $txt[$alt] . '" ' . $custom . ' />';
}

/**
 * @link http://php.net/manual/en/ref.libxml.php
 */
class LibXMLError  {
}

/**
 * Set the streams context for the next libxml document load or write
 * @link http://php.net/manual/en/function.libxml-set-streams-context.php
 * @param streams_context resource
 * @return void 
 */
function libxml_set_streams_context ($streams_context) {}

/**
 * Disable libxml errors and allow user to fetch error information as needed
 * @link http://php.net/manual/en/function.libxml-use-internal-errors.php
 * @param use_errors bool[optional]
 * @return bool 
 */
function libxml_use_internal_errors ($use_errors = null) {}

/**
 * Retrieve last error from libxml
 * @link http://php.net/manual/en/function.libxml-get-last-error.php
 * @return LibXMLError a LibXMLError object if there is any error in the
 */
function libxml_get_last_error () {}

/**
 * Clear libxml error buffer
 * @link http://php.net/manual/en/function.libxml-clear-errors.php
 * @return void 
 */
function libxml_clear_errors () {}

/**
 * Retrieve array of errors
 * @link http://php.net/manual/en/function.libxml-get-errors.php
 * @return array an array with LibXMLError objects if there are any
 */
function libxml_get_errors () {}


/**
 * libxml version like 20605 or 20617
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_VERSION', 20627);

/**
 * libxml version like 2.6.5 or 2.6.17
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_DOTTED_VERSION', "2.6.27");

/**
 * Substitute entities
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOENT', 2);

/**
 * Load the external subset
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_DTDLOAD', 4);

/**
 * Default DTD attributes
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_DTDATTR', 8);

/**
 * Validate with the DTD
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_DTDVALID', 16);

/**
 * Suppress error reports
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOERROR', 32);

/**
 * Suppress warning reports
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOWARNING', 64);

/**
 * Remove blank nodes
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOBLANKS', 256);

/**
 * Implement XInclude substitution
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_XINCLUDE', 1024);

/**
 * Remove redundant namespaces declarations
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NSCLEAN', 8192);

/**
 * Merge CDATA as text nodes
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOCDATA', 16384);

/**
 * Disable network access when loading documents
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NONET', 2048);

/**
 * Activate small nodes allocation optimization. This may speed up your
 * application without needing to change the code.
 * Only available in Libxml &gt;= 2.6.21
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_COMPACT', 65536);

/**
 * Drop the XML declaration when saving a document
 * Only available in Libxml &gt;= 2.6.21
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOXMLDECL', 2);

/**
 * Expand empty tags (e.g. &lt;br/&gt; to
 * &lt;br&gt;&lt;/br&gt;)
 * This option is currently just available in the
 * and
 * functions.
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_NOEMPTYTAG', 4);

/**
 * No errors
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_ERR_NONE', 0);

/**
 * A simple warning
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_ERR_WARNING', 1);

/**
 * A recoverable error
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_ERR_ERROR', 2);

/**
 * A fatal error
 * @link http://php.net/manual/en/libxml.constants.php
 */
define ('LIBXML_ERR_FATAL', 3);

// End of libxml v.

// Start of xml v.

/**
 * Create an XML parser
 * @link http://php.net/manual/en/function.xml-parser-create.php
 * @param encoding string[optional]
 * @return resource a resource handle for the new XML parser.
 */
function xml_parser_create ($encoding = null) {}

/**
 * Create an XML parser with namespace support
 * @link http://php.net/manual/en/function.xml-parser-create-ns.php
 * @param encoding string[optional]
 * @param separator string[optional]
 * @return resource a resource handle for the new XML parser.
 */
function xml_parser_create_ns ($encoding = null, $separator = null) {}

/**
 * Use XML Parser within an object
 * @link http://php.net/manual/en/function.xml-set-object.php
 * @param parser resource
 * @param object object
 * @return bool 
 */
function xml_set_object ($parser, &$object) {}

/**
 * Set up start and end element handlers
 * @link http://php.net/manual/en/function.xml-set-element-handler.php
 * @param parser resource
 * @param start_element_handler callback
 * @param end_element_handler callback
 * @return bool 
 */
function xml_set_element_handler ($parser, $start_element_handler, $end_element_handler) {}

/**
 * Set up character data handler
 * @link http://php.net/manual/en/function.xml-set-character-data-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_character_data_handler ($parser, $handler) {}

/**
 * Set up processing instruction (PI) handler
 * @link http://php.net/manual/en/function.xml-set-processing-instruction-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_processing_instruction_handler ($parser, $handler) {}

/**
 * Set up default handler
 * @link http://php.net/manual/en/function.xml-set-default-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_default_handler ($parser, $handler) {}

/**
 * Set up unparsed entity declaration handler
 * @link http://php.net/manual/en/function.xml-set-unparsed-entity-decl-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_unparsed_entity_decl_handler ($parser, $handler) {}

/**
 * Set up notation declaration handler
 * @link http://php.net/manual/en/function.xml-set-notation-decl-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_notation_decl_handler ($parser, $handler) {}

/**
 * Set up external entity reference handler
 * @link http://php.net/manual/en/function.xml-set-external-entity-ref-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_external_entity_ref_handler ($parser, $handler) {}

/**
 * Set up start namespace declaration handler
 * @link http://php.net/manual/en/function.xml-set-start-namespace-decl-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_start_namespace_decl_handler ($parser, $handler) {}

/**
 * Set up end namespace declaration handler
 * @link http://php.net/manual/en/function.xml-set-end-namespace-decl-handler.php
 * @param parser resource
 * @param handler callback
 * @return bool 
 */
function xml_set_end_namespace_decl_handler ($parser, $handler) {}

/**
 * Start parsing an XML document
 * @link http://php.net/manual/en/function.xml-parse.php
 * @param parser resource
 * @param data string
 * @param is_final bool[optional]
 * @return int 1 on success or 0 on failure.
 */
function xml_parse ($parser, $data, $is_final = null) {}

/**
 * Parse XML data into an array structure
 * @link http://php.net/manual/en/function.xml-parse-into-struct.php
 * @param parser resource
 * @param data string
 * @param values array
 * @param index array[optional]
 * @return int 
 */
function xml_parse_into_struct ($parser, $data, array &$values, array &$index = null) {}

/**
 * Get XML parser error code
 * @link http://php.net/manual/en/function.xml-get-error-code.php
 * @param parser resource
 * @return int 
 */
function xml_get_error_code ($parser) {}

/**
 * Get XML parser error string
 * @link http://php.net/manual/en/function.xml-error-string.php
 * @param code int
 * @return string a string with a textual description of the error
 */
function xml_error_string ($code) {}

/**
 * Get current line number for an XML parser
 * @link http://php.net/manual/en/function.xml-get-current-line-number.php
 * @param parser resource
 * @return int 
 */
function xml_get_current_line_number ($parser) {}

/**
 * Get current column number for an XML parser
 * @link http://php.net/manual/en/function.xml-get-current-column-number.php
 * @param parser resource
 * @return int 
 */
function xml_get_current_column_number ($parser) {}

/**
 * Get current byte index for an XML parser
 * @link http://php.net/manual/en/function.xml-get-current-byte-index.php
 * @param parser resource
 * @return int 
 */
function xml_get_current_byte_index ($parser) {}

/**
 * Free an XML parser
 * @link http://php.net/manual/en/function.xml-parser-free.php
 * @param parser resource
 * @return bool 
 */
function xml_parser_free ($parser) {}

/**
 * Set options in an XML parser
 * @link http://php.net/manual/en/function.xml-parser-set-option.php
 * @param parser resource
 * @param option int
 * @param value mixed
 * @return bool 
 */
function xml_parser_set_option ($parser, $option, $value) {}

/**
 * Get options from an XML parser
 * @link http://php.net/manual/en/function.xml-parser-get-option.php
 * @param parser resource
 * @param option int
 * @return mixed 
 */
function xml_parser_get_option ($parser, $option) {}

/**
 * Encodes an ISO-8859-1 string to UTF-8
 * @link http://php.net/manual/en/function.utf8-encode.php
 * @param data string
 * @return string the UTF-8 translation of data.
 */
function utf8_encode ($data) {}

/**
 * Converts a string with ISO-8859-1 characters encoded with UTF-8
   to single-byte ISO-8859-1
 * @link http://php.net/manual/en/function.utf8-decode.php
 * @param data string
 * @return string the ISO-8859-1 translation of data.
 */
function utf8_decode ($data) {}

define ('XML_ERROR_NONE', 0);
define ('XML_ERROR_NO_MEMORY', 1);
define ('XML_ERROR_SYNTAX', 2);
define ('XML_ERROR_NO_ELEMENTS', 3);
define ('XML_ERROR_INVALID_TOKEN', 4);
define ('XML_ERROR_UNCLOSED_TOKEN', 5);
define ('XML_ERROR_PARTIAL_CHAR', 6);
define ('XML_ERROR_TAG_MISMATCH', 7);
define ('XML_ERROR_DUPLICATE_ATTRIBUTE', 8);
define ('XML_ERROR_JUNK_AFTER_DOC_ELEMENT', 9);
define ('XML_ERROR_PARAM_ENTITY_REF', 10);
define ('XML_ERROR_UNDEFINED_ENTITY', 11);
define ('XML_ERROR_RECURSIVE_ENTITY_REF', 12);
define ('XML_ERROR_ASYNC_ENTITY', 13);
define ('XML_ERROR_BAD_CHAR_REF', 14);
define ('XML_ERROR_BINARY_ENTITY_REF', 15);
define ('XML_ERROR_ATTRIBUTE_EXTERNAL_ENTITY_REF', 16);
define ('XML_ERROR_MISPLACED_XML_PI', 17);
define ('XML_ERROR_UNKNOWN_ENCODING', 18);
define ('XML_ERROR_INCORRECT_ENCODING', 19);
define ('XML_ERROR_UNCLOSED_CDATA_SECTION', 20);
define ('XML_ERROR_EXTERNAL_ENTITY_HANDLING', 21);
define ('XML_OPTION_CASE_FOLDING', 1);
define ('XML_OPTION_TARGET_ENCODING', 2);
define ('XML_OPTION_SKIP_TAGSTART', 3);
define ('XML_OPTION_SKIP_WHITE', 4);
define ('XML_SAX_IMPL', "libxml");

// End of xml v.

// Start of wddx v.

/**
 * Serialize a single value into a WDDX packet
 * @link http://php.net/manual/en/function.wddx-serialize-value.php
 * @param var mixed
 * @param comment string[optional]
 * @return string the WDDX packet, or false on error.
 */
function wddx_serialize_value ($var, $comment = null) {}

/**
 * Serialize variables into a WDDX packet
 * @link http://php.net/manual/en/function.wddx-serialize-vars.php
 * @param var_name mixed
 * @param ... mixed[optional]
 * @return string the WDDX packet, or false on error.
 */
function wddx_serialize_vars ($var_name) {}

/**
 * Starts a new WDDX packet with structure inside it
 * @link http://php.net/manual/en/function.wddx-packet-start.php
 * @param comment string[optional]
 * @return resource a packet ID for use in later functions, or false on error.
 */
function wddx_packet_start ($comment = null) {}

/**
 * Ends a WDDX packet with the specified ID
 * @link http://php.net/manual/en/function.wddx-packet-end.php
 * @param packet_id resource
 * @return string the string containing the WDDX packet.
 */
function wddx_packet_end ($packet_id) {}

/**
 * Add variables to a WDDX packet with the specified ID
 * @link http://php.net/manual/en/function.wddx-add-vars.php
 * @param packet_id resource
 * @param var_name mixed
 * @param ... mixed[optional]
 * @return bool 
 */
function wddx_add_vars ($packet_id, $var_name) {}

/**
 * &Alias; <function>wddx_unserialize</function>
 * @link http://php.net/manual/en/function.wddx-deserialize.php
 */
function wddx_deserialize () {}

// End of wddx v.

// Start of session v.

/**
 * Get and/or set the current session name
 * @link http://php.net/manual/en/function.session-name.php
 * @param name string[optional]
 * @return string the name of the current session.
 */
function session_name ($name = null) {}

/**
 * Get and/or set the current session module
 * @link http://php.net/manual/en/function.session-module-name.php
 * @param module string[optional]
 * @return string the name of the current session module.
 */
function session_module_name ($module = null) {}

/**
 * Get and/or set the current session save path
 * @link http://php.net/manual/en/function.session-save-path.php
 * @param path string[optional]
 * @return string the path of the current directory used for data storage.
 */
function session_save_path ($path = null) {}

/**
 * Get and/or set the current session id
 * @link http://php.net/manual/en/function.session-id.php
 * @param id string[optional]
 * @return string 
 */
function session_id ($id = null) {}

/**
 * Update the current session id with a newly generated one
 * @link http://php.net/manual/en/function.session-regenerate-id.php
 * @param delete_old_session bool[optional]
 * @return bool 
 */
function session_regenerate_id ($delete_old_session = null) {}

/**
 * Decodes session data from a string
 * @link http://php.net/manual/en/function.session-decode.php
 * @param data string
 * @return bool 
 */
function session_decode ($data) {}

/**
 * Register one or more global variables with the current session
 * @link http://php.net/manual/en/function.session-register.php
 * @param name mixed
 * @param ... mixed[optional]
 * @return bool 
 */
function session_register ($name) {}

/**
 * Unregister a global variable from the current session
 * @link http://php.net/manual/en/function.session-unregister.php
 * @param name string
 * @return bool 
 */
function session_unregister ($name) {}

/**
 * Find out whether a global variable is registered in a session
 * @link http://php.net/manual/en/function.session-is-registered.php
 * @param name string
 * @return bool 
 */
function session_is_registered ($name) {}

/**
 * Encodes the current session data as a string
 * @link http://php.net/manual/en/function.session-encode.php
 * @return string the contents of the current session encoded.
 */
function session_encode () {}

/**
 * Initialize session data
 * @link http://php.net/manual/en/function.session-start.php
 * @return bool 
 */
function session_start () {}

/**
 * Destroys all data registered to a session
 * @link http://php.net/manual/en/function.session-destroy.php
 * @return bool 
 */
function session_destroy () {}

/**
 * Free all session variables
 * @link http://php.net/manual/en/function.session-unset.php
 * @return void 
 */
function session_unset () {}

/**
 * Sets user-level session storage functions
 * @link http://php.net/manual/en/function.session-set-save-handler.php
 * @param open callback
 * @param close callback
 * @param read callback
 * @param write callback
 * @param destroy callback
 * @param gc callback
 * @return bool 
 */
function session_set_save_handler ($open, $close, $read, $write, $destroy, $gc) {}

/**
 * Get and/or set the current cache limiter
 * @link http://php.net/manual/en/function.session-cache-limiter.php
 * @param cache_limiter string[optional]
 * @return string the name of the current cache limiter.
 */
function session_cache_limiter ($cache_limiter = null) {}

/**
 * Return current cache expire
 * @link http://php.net/manual/en/function.session-cache-expire.php
 * @param new_cache_expire int[optional]
 * @return int the current setting of session.cache_expire.
 */
function session_cache_expire ($new_cache_expire = null) {}

/**
 * Set the session cookie parameters
 * @link http://php.net/manual/en/function.session-set-cookie-params.php
 * @param lifetime int
 * @param path string[optional]
 * @param domain string[optional]
 * @param secure bool[optional]
 * @param httponly bool[optional]
 * @return void 
 */
function session_set_cookie_params ($lifetime, $path = null, $domain = null, $secure = null, $httponly = null) {}

/**
 * Get the session cookie parameters
 * @link http://php.net/manual/en/function.session-get-cookie-params.php
 * @return array an array with the current session cookie information, the array
 */
function session_get_cookie_params () {}

/**
 * Write session data and end session
 * @link http://php.net/manual/en/function.session-write-close.php
 * @return void 
 */
function session_write_close () {}

/**
 * &Alias; <function>session_write_close</function>
 * @link http://php.net/manual/en/function.session-commit.php
 */
function session_commit () {}

// End of session v.

// Start of pcre v.

/**
 * Perform a regular expression match
 * @link http://php.net/manual/en/function.preg-match.php
 * @param pattern string
 * @param subject string
 * @param matches array[optional]
 * @param flags int[optional]
 * @param offset int[optional]
 * @return int 
 */
function preg_match ($pattern, $subject, array &$matches = null, $flags = null, $offset = null) {}

/**
 * Perform a global regular expression match
 * @link http://php.net/manual/en/function.preg-match-all.php
 * @param pattern string
 * @param subject string
 * @param matches array
 * @param flags int[optional]
 * @param offset int[optional]
 * @return int the number of full pattern matches (which might be zero),
 */
function preg_match_all ($pattern, $subject, array &$matches, $flags = null, $offset = null) {}

/**
 * Perform a regular expression search and replace
 * @link http://php.net/manual/en/function.preg-replace.php
 * @param pattern mixed
 * @param replacement mixed
 * @param subject mixed
 * @param limit int[optional]
 * @param count int[optional]
 * @return mixed 
 */
function preg_replace ($pattern, $replacement, $subject, $limit = null, &$count = null) {}

/**
 * Perform a regular expression search and replace using a callback
 * @link http://php.net/manual/en/function.preg-replace-callback.php
 * @param pattern mixed
 * @param callback callback
 * @param subject mixed
 * @param limit int[optional]
 * @param count int[optional]
 * @return mixed 
 */
function preg_replace_callback ($pattern, $callback, $subject, $limit = null, &$count = null) {}

/**
 * Split string by a regular expression
 * @link http://php.net/manual/en/function.preg-split.php
 * @param pattern string
 * @param subject string
 * @param limit int[optional]
 * @param flags int[optional]
 * @return array an array containing substrings of subject
 */
function preg_split ($pattern, $subject, $limit = null, $flags = null) {}

/**
 * Quote regular expression characters
 * @link http://php.net/manual/en/function.preg-quote.php
 * @param str string
 * @param delimiter string[optional]
 * @return string the quoted string.
 */
function preg_quote ($str, $delimiter = null) {}

/**
 * Return array entries that match the pattern
 * @link http://php.net/manual/en/function.preg-grep.php
 * @param pattern string
 * @param input array
 * @param flags int[optional]
 * @return array an array indexed using the keys from the
 */
function preg_grep ($pattern, array $input, $flags = null) {}

/**
 * Returns the error code of the last PCRE regex execution
 * @link http://php.net/manual/en/function.preg-last-error.php
 * @return int one of the following constants (
 */
function preg_last_error () {}


/**
 * Orders results so that $matches[0] is an array of full pattern
 * matches, $matches[1] is an array of strings matched by the first
 * parenthesized subpattern, and so on. This flag is only used with
 * preg_match_all.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_PATTERN_ORDER', 1);

/**
 * Orders results so that $matches[0] is an array of first set of
 * matches, $matches[1] is an array of second set of matches, and so
 * on. This flag is only used with preg_match_all.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_SET_ORDER', 2);

/**
 * See the description of
 * PREG_SPLIT_OFFSET_CAPTURE. This flag is
 * available since PHP 4.3.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_OFFSET_CAPTURE', 256);

/**
 * This flag tells preg_split to return only non-empty
 * pieces.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_SPLIT_NO_EMPTY', 1);

/**
 * This flag tells preg_split to capture
 * parenthesized expression in the delimiter pattern as well. This flag
 * is available since PHP 4.0.5.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_SPLIT_DELIM_CAPTURE', 2);

/**
 * If this flag is set, for every occurring match the appendant string
 * offset will also be returned. Note that this changes the return
 * values in an array where every element is an array consisting of the
 * matched string at offset 0 and its string offset within subject at
 * offset 1. This flag is available since PHP 4.3.0
 * and is only used for preg_split.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_SPLIT_OFFSET_CAPTURE', 4);
define ('PREG_GREP_INVERT', 1);

/**
 * Returned by preg_last_error if there were no
 * errors. Available since PHP 5.2.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_NO_ERROR', 0);

/**
 * Returned by preg_last_error if there was an
 * internal PCRE error. Available since PHP 5.2.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_INTERNAL_ERROR', 1);

/**
 * Returned by preg_last_error if backtrack limit was exhausted.
 * Available since PHP 5.2.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_BACKTRACK_LIMIT_ERROR', 2);

/**
 * Returned by preg_last_error if recursion limit was exhausted.
 * Available since PHP 5.2.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_RECURSION_LIMIT_ERROR', 3);

/**
 * Returned by preg_last_error if the last error was
 * caused by malformed UTF-8 data (only when running a regex in UTF-8 mode). Available
 * since PHP 5.2.0.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PREG_BAD_UTF8_ERROR', 4);

/**
 * PCRE version and release date (e.g. "7.0 18-Dec-2006"). Available since
 * PHP 5.2.4.
 * @link http://php.net/manual/en/pcre.constants.php
 */
define ('PCRE_VERSION', "7.2 2007-06-19");

// End of pcre v.

// Start of SimpleXML v.0.1

class SimpleXMLElement implements Traversable {

	/**
	 * Creates a new SimpleXMLElement object
	 * @link http://php.net/manual/en/function.simplexml-element-construct.php
	 */
	final public function __construct () {}

	/**
	 * Return a well-formed XML string based on SimpleXML element
	 * @link http://php.net/manual/en/function.simplexml-element-asXML.php
	 * @param filename string[optional]
	 * @return mixed 
	 */
	public function asXML ($filename = null) {}

	public function saveXML () {}

	/**
	 * Runs XPath query on XML data
	 * @link http://php.net/manual/en/function.simplexml-element-xpath.php
	 * @param path string
	 * @return array an array of SimpleXMLElement objects or false in
	 */
	public function xpath ($path) {}

	/**
	 * Creates a prefix/ns context for the next XPath query
	 * @link http://php.net/manual/en/function.simplexml-element-registerXPathNamespace.php
	 * @param prefix string
	 * @param ns string
	 * @return bool 
	 */
	public function registerXPathNamespace ($prefix, $ns) {}

	/**
	 * Identifies an element's attributes
	 * @link http://php.net/manual/en/function.simplexml-element-attributes.php
	 * @param ns string[optional]
	 * @param is_prefix bool[optional]
	 * @return SimpleXMLElement 
	 */
	public function attributes ($ns = null, $is_prefix = null) {}

	/**
	 * Finds children of given node
	 * @link http://php.net/manual/en/function.simplexml-element-children.php
	 * @param ns string[optional]
	 * @param is_prefix bool[optional]
	 * @return SimpleXMLElement 
	 */
	public function children ($ns = null, $is_prefix = null) {}

	/**
	 * Returns namespaces used in document
	 * @link http://php.net/manual/en/function.simplexml-element-getNamespaces.php
	 * @param recursive bool[optional]
	 * @return array 
	 */
	public function getNamespaces ($recursive = null) {}

	/**
	 * Returns namespaces declared in document
	 * @link http://php.net/manual/en/function.simplexml-element-getDocNamespaces.php
	 * @param recursive bool[optional]
	 * @return array 
	 */
	public function getDocNamespaces ($recursive = null) {}

	/**
	 * Gets the name of the XML element
	 * @link http://php.net/manual/en/function.simplexml-element-getName.php
	 * @return string 
	 */
	public function getName () {}

	/**
	 * Adds a child element to the XML node
	 * @link http://php.net/manual/en/function.simplexml-element-addChild.php
	 * @param name string
	 * @param value string[optional]
	 * @param namespace string[optional]
	 * @return SimpleXMLElement 
	 */
	public function addChild ($name, $value = null, $namespace = null) {}

	/**
	 * Adds an attribute to the SimpleXML element
	 * @link http://php.net/manual/en/function.simplexml-element-addAttribute.php
	 * @param name string
	 * @param value string
	 * @param namespace string[optional]
	 * @return void 
	 */
	public function addAttribute ($name, $value, $namespace = null) {}

}

/**
 * Interprets an XML file into an object
 * @link http://php.net/manual/en/function.simplexml-load-file.php
 * @param filename string
 * @param class_name string[optional]
 * @param options int[optional]
 * @param ns string[optional]
 * @param is_prefix bool[optional]
 * @return object an object of class SimpleXMLElement with
 */
function simplexml_load_file ($filename, $class_name = null, $options = null, $ns = null, $is_prefix = null) {}

/**
 * Interprets a string of XML into an object
 * @link http://php.net/manual/en/function.simplexml-load-string.php
 * @param data string
 * @param class_name string[optional]
 * @param options int[optional]
 * @param ns string[optional]
 * @param is_prefix bool[optional]
 * @return object an object of class SimpleXMLElement with
 */
function simplexml_load_string ($data, $class_name = null, $options = null, $ns = null, $is_prefix = null) {}

/**
 * Get a <literal>SimpleXMLElement</literal> object from a DOM node.
 * @link http://php.net/manual/en/function.simplexml-import-dom.php
 * @param node DOMNode
 * @param class_name string[optional]
 * @return SimpleXMLElement a SimpleXMLElement or false on failure.
 */
function simplexml_import_dom (DOMNode $node, $class_name = null) {}

// End of SimpleXML v.0.1

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

	/**
	 * Rewind the iterator to the first element of the top level inner iterator
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current position is valid
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Access the current key
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-key.php
	 */
	public function key () {}

	/**
	 * Access the current element value
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-current.php
	 */
	public function current () {}

	/**
	 * Move forward to the next element
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the current depth of the recursive iteration
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-getDepth.php
	 */
	public function getDepth () {}

	/**
	 * The current active sub iterator
	 * @link http://php.net/manual/en/function.RecursiveIteratorIterator-getSubIterator.php
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

abstract class FilterIterator extends IteratorIterator implements OuterIterator, Traversable, Iterator {

	/**
	 * @param iterator Iterator
	 */
	public function __construct (Iterator $iterator) {}

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.FilterIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.FilterIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Get the current key
	 * @link http://php.net/manual/en/function.FilterIterator-key.php
	 */
	public function key () {}

	/**
	 * Get the current element value
	 * @link http://php.net/manual/en/function.FilterIterator-current.php
	 */
	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.FilterIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the inner iterator
	 * @link http://php.net/manual/en/function.FilterIterator-getInnerIterator.php
	 */
	public function getInnerIterator () {}

	abstract public function accept () {}

}

abstract class RecursiveFilterIterator extends FilterIterator implements Iterator, Traversable, OuterIterator, RecursiveIterator {

	/**
	 * @param iterator RecursiveIterator
	 */
	public function __construct (RecursiveIterator $iterator) {}

	public function hasChildren () {}

	public function getChildren () {}

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.FilterIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.FilterIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Get the current key
	 * @link http://php.net/manual/en/function.FilterIterator-key.php
	 */
	public function key () {}

	/**
	 * Get the current element value
	 * @link http://php.net/manual/en/function.FilterIterator-current.php
	 */
	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.FilterIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the inner iterator
	 * @link http://php.net/manual/en/function.FilterIterator-getInnerIterator.php
	 */
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

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.FilterIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.FilterIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Get the current key
	 * @link http://php.net/manual/en/function.FilterIterator-key.php
	 */
	public function key () {}

	/**
	 * Get the current element value
	 * @link http://php.net/manual/en/function.FilterIterator-current.php
	 */
	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.FilterIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the inner iterator
	 * @link http://php.net/manual/en/function.FilterIterator-getInnerIterator.php
	 */
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

	/**
	 * Rewind the iterator to the specified starting offset
	 * @link http://php.net/manual/en/function.LimitIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.LimitIterator-valid.php
	 */
	public function valid () {}

	public function key () {}

	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.LimitIterator-next.php
	 */
	public function next () {}

	/**
	 * Seek to the given position
	 * @link http://php.net/manual/en/function.LimitIterator-seek.php
	 * @param position
	 */
	public function seek ($position) {}

	/**
	 * Return the current position
	 * @link http://php.net/manual/en/function.LimitIterator-getPosition.php
	 */
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

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.CachingIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.CachingIterator-valid.php
	 */
	public function valid () {}

	public function key () {}

	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.CachingIterator-next.php
	 */
	public function next () {}

	/**
	 * Check whether the inner iterator has a valid next element
	 * @link http://php.net/manual/en/function.CachingIterator-hasNext.php
	 */
	public function hasNext () {}

	/**
	 * Return the string representation of the current element
	 * @link http://php.net/manual/en/function.CachingIterator-toString.php
	 */
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

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.CachingIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.CachingIterator-valid.php
	 */
	public function valid () {}

	public function key () {}

	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.CachingIterator-next.php
	 */
	public function next () {}

	/**
	 * Check whether the inner iterator has a valid next element
	 * @link http://php.net/manual/en/function.CachingIterator-hasNext.php
	 */
	public function hasNext () {}

	/**
	 * Return the string representation of the current element
	 * @link http://php.net/manual/en/function.CachingIterator-toString.php
	 */
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

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.FilterIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.FilterIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Get the current key
	 * @link http://php.net/manual/en/function.FilterIterator-key.php
	 */
	public function key () {}

	/**
	 * Get the current element value
	 * @link http://php.net/manual/en/function.FilterIterator-current.php
	 */
	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.FilterIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the inner iterator
	 * @link http://php.net/manual/en/function.FilterIterator-getInnerIterator.php
	 */
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

	/**
	 * Rewind the iterator
	 * @link http://php.net/manual/en/function.FilterIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether the current element is valid
	 * @link http://php.net/manual/en/function.FilterIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Get the current key
	 * @link http://php.net/manual/en/function.FilterIterator-key.php
	 */
	public function key () {}

	/**
	 * Get the current element value
	 * @link http://php.net/manual/en/function.FilterIterator-current.php
	 */
	public function current () {}

	/**
	 * Move the iterator forward
	 * @link http://php.net/manual/en/function.FilterIterator-next.php
	 */
	public function next () {}

	/**
	 * Get the inner iterator
	 * @link http://php.net/manual/en/function.FilterIterator-getInnerIterator.php
	 */
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
	 * Construct a new array object
	 * @link http://php.net/manual/en/function.ArrayObject-construct.php
	 * @param array
	 */
	public function __construct ($array) {}

	/**
	 * Returns whether the requested $index exists
	 * @link http://php.net/manual/en/function.ArrayObject-offsetExists.php
	 * @param index
	 */
	public function offsetExists ($index) {}

	/**
	 * Returns the value at the specified $index
	 * @link http://php.net/manual/en/function.ArrayObject-offsetGet.php
	 * @param index
	 */
	public function offsetGet ($index) {}

	/**
	 * Sets the value at the specified $index to $newval
	 * @link http://php.net/manual/en/function.ArrayObject-offsetSet.php
	 * @param index
	 * @param newval
	 */
	public function offsetSet ($index, $newval) {}

	/**
	 * Unsets the value at the specified $index
	 * @link http://php.net/manual/en/function.ArrayObject-offsetUnset.php
	 * @param index
	 */
	public function offsetUnset ($index) {}

	/**
	 * Appends the value
	 * @link http://php.net/manual/en/function.ArrayObject-append.php
	 * @param value
	 */
	public function append ($value) {}

	public function getArrayCopy () {}

	/**
	 * Return the number of elements in the Iterator
	 * @link http://php.net/manual/en/function.ArrayObject-count.php
	 */
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

	/**
	 * Create a new iterator from an ArrayObject instance
	 * @link http://php.net/manual/en/function.ArrayObject-getIterator.php
	 */
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

	/**
	 * Rewind array back to the start
	 * @link http://php.net/manual/en/function.ArrayIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Return current array entry
	 * @link http://php.net/manual/en/function.ArrayIterator-current.php
	 */
	public function current () {}

	/**
	 * Return current array key
	 * @link http://php.net/manual/en/function.ArrayIterator-key.php
	 */
	public function key () {}

	/**
	 * Move to next entry
	 * @link http://php.net/manual/en/function.ArrayIterator-next.php
	 */
	public function next () {}

	/**
	 * Check whether array contains more entries
	 * @link http://php.net/manual/en/function.ArrayIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Seek to position
	 * @link http://php.net/manual/en/function.ArrayIterator-seek.php
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

	/**
	 * Rewind array back to the start
	 * @link http://php.net/manual/en/function.ArrayIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Return current array entry
	 * @link http://php.net/manual/en/function.ArrayIterator-current.php
	 */
	public function current () {}

	/**
	 * Return current array key
	 * @link http://php.net/manual/en/function.ArrayIterator-key.php
	 */
	public function key () {}

	/**
	 * Move to next entry
	 * @link http://php.net/manual/en/function.ArrayIterator-next.php
	 */
	public function next () {}

	/**
	 * Check whether array contains more entries
	 * @link http://php.net/manual/en/function.ArrayIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Seek to position
	 * @link http://php.net/manual/en/function.ArrayIterator-seek.php
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
	 * Constructs a new dir iterator from a path
	 * @link http://php.net/manual/en/function.DirectoryIterator-construct.php
	 * @param path
	 */
	public function __construct ($path) {}

	/**
	 * Return filename of current dir entry
	 * @link http://php.net/manual/en/function.DirectoryIterator-getFilename.php
	 */
	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	/**
	 * Returns true if current entry is '.' or '..'
	 * @link http://php.net/manual/en/function.DirectoryIterator-isDot.php
	 */
	public function isDot () {}

	/**
	 * Rewind dir back to the start
	 * @link http://php.net/manual/en/function.DirectoryIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether dir contains more entries
	 * @link http://php.net/manual/en/function.DirectoryIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Return current dir entry
	 * @link http://php.net/manual/en/function.DirectoryIterator-key.php
	 */
	public function key () {}

	/**
	 * Return this (needed for Iterator interface)
	 * @link http://php.net/manual/en/function.DirectoryIterator-current.php
	 */
	public function current () {}

	/**
	 * Move to next entry
	 * @link http://php.net/manual/en/function.DirectoryIterator-next.php
	 */
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

	/**
	 * Rewind dir back to the start
	 * @link http://php.net/manual/en/function.RecursiveDirectoryIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Move to next entry
	 * @link http://php.net/manual/en/function.RecursiveDirectoryIterator-next.php
	 */
	public function next () {}

	/**
	 * Return path and filename of current dir entry
	 * @link http://php.net/manual/en/function.RecursiveDirectoryIterator-key.php
	 */
	public function key () {}

	public function current () {}

	/**
	 * Returns whether current entry is a directory and not '.' or '..'
	 * @link http://php.net/manual/en/function.RecursiveDirectoryIterator-hasChildren.php
	 * @param allow_links[optional]
	 */
	public function hasChildren ($allow_links) {}

	/**
	 * Returns an iterator for the current entry if it is a directory
	 * @link http://php.net/manual/en/function.RecursiveDirectoryIterator-getChildren.php
	 */
	public function getChildren () {}

	public function getSubPath () {}

	public function getSubPathname () {}

	/**
	 * Return filename of current dir entry
	 * @link http://php.net/manual/en/function.DirectoryIterator-getFilename.php
	 */
	public function getFilename () {}

	/**
	 * @param suffix[optional]
	 */
	public function getBasename ($suffix) {}

	/**
	 * Returns true if current entry is '.' or '..'
	 * @link http://php.net/manual/en/function.DirectoryIterator-isDot.php
	 */
	public function isDot () {}

	/**
	 * Check whether dir contains more entries
	 * @link http://php.net/manual/en/function.DirectoryIterator-valid.php
	 */
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

	/**
	 * Rewind SimpleXML back to the start
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-rewind.php
	 */
	public function rewind () {}

	/**
	 * Check whether SimpleXML contains more entries
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-valid.php
	 */
	public function valid () {}

	/**
	 * Return current SimpleXML entry
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-current.php
	 */
	public function current () {}

	/**
	 * Return current SimpleXML key
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-key.php
	 */
	public function key () {}

	/**
	 * Move to next entry
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-next.php
	 */
	public function next () {}

	/**
	 * Returns whether current entry is a SimpleXML object
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-hasChildren.php
	 */
	public function hasChildren () {}

	/**
	 * Returns an iterator for the current entry if it is a SimpleXML object
	 * @link http://php.net/manual/en/function.SimpleXMLIterator-getChildren.php
	 */
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
	 * @param filename string[optional]
	 * @return mixed 
	 */
	public function asXML ($filename = null) {}

	public function saveXML () {}

	/**
	 * Runs XPath query on XML data
	 * @link http://php.net/manual/en/function.simplexml-element-xpath.php
	 * @param path string
	 * @return array an array of SimpleXMLElement objects or false in
	 */
	public function xpath ($path) {}

	/**
	 * Creates a prefix/ns context for the next XPath query
	 * @link http://php.net/manual/en/function.simplexml-element-registerXPathNamespace.php
	 * @param prefix string
	 * @param ns string
	 * @return bool 
	 */
	public function registerXPathNamespace ($prefix, $ns) {}

	/**
	 * Identifies an element's attributes
	 * @link http://php.net/manual/en/function.simplexml-element-attributes.php
	 * @param ns string[optional]
	 * @param is_prefix bool[optional]
	 * @return SimpleXMLElement 
	 */
	public function attributes ($ns = null, $is_prefix = null) {}

	/**
	 * Finds children of given node
	 * @link http://php.net/manual/en/function.simplexml-element-children.php
	 * @param ns string[optional]
	 * @param is_prefix bool[optional]
	 * @return SimpleXMLElement 
	 */
	public function children ($ns = null, $is_prefix = null) {}

	/**
	 * Returns namespaces used in document
	 * @link http://php.net/manual/en/function.simplexml-element-getNamespaces.php
	 * @param recursive bool[optional]
	 * @return array 
	 */
	public function getNamespaces ($recursive = null) {}

	/**
	 * Returns namespaces declared in document
	 * @link http://php.net/manual/en/function.simplexml-element-getDocNamespaces.php
	 * @param recursive bool[optional]
	 * @return array 
	 */
	public function getDocNamespaces ($recursive = null) {}

	/**
	 * Gets the name of the XML element
	 * @link http://php.net/manual/en/function.simplexml-element-getName.php
	 * @return string 
	 */
	public function getName () {}

	/**
	 * Adds a child element to the XML node
	 * @link http://php.net/manual/en/function.simplexml-element-addChild.php
	 * @param name string
	 * @param value string[optional]
	 * @param namespace string[optional]
	 * @return SimpleXMLElement 
	 */
	public function addChild ($name, $value = null, $namespace = null) {}

	/**
	 * Adds an attribute to the SimpleXML element
	 * @link http://php.net/manual/en/function.simplexml-element-addAttribute.php
	 * @param name string
	 * @param value string
	 * @param namespace string[optional]
	 * @return void 
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
 */
function spl_classes () {}

/**
 * Default implementation for __autoload()
 * @link http://php.net/manual/en/function.spl-autoload.php
 */
function spl_autoload () {}

/**
 * Register and return default file extensions for spl_autoload
 * @link http://php.net/manual/en/function.spl-autoload-extensions.php
 */
function spl_autoload_extensions () {}

/**
 * Register given function as __autoload() implementation
 * @link http://php.net/manual/en/function.spl-autoload-register.php
 */
function spl_autoload_register () {}

/**
 * Unregister given function as __autoload() implementation
 * @link http://php.net/manual/en/function.spl-autoload-unregister.php
 */
function spl_autoload_unregister () {}

/**
 * Return all registered __autoload() functions
 * @link http://php.net/manual/en/function.spl-autoload-functions.php
 */
function spl_autoload_functions () {}

/**
 * Try all registered __autoload() function to load the requested class
 * @link http://php.net/manual/en/function.spl-autoload-call.php
 */
function spl_autoload_call () {}

/**
 * Return the parent classes of the given class
 * @link http://php.net/manual/en/function.class-parents.php
 * @param class mixed
 * @param autoload bool[optional]
 * @return array an array or false on error.
 */
function class_parents ($class, $autoload = null) {}

/**
 * Return the interfaces which are implemented by the given class
 * @link http://php.net/manual/en/function.class-implements.php
 * @param class mixed
 * @param autoload bool[optional]
 * @return array an array or false on error.
 */
function class_implements ($class, $autoload = null) {}

/**
 * Return hash id for given object
 * @link http://php.net/manual/en/function.spl-object-hash.php
 */
function spl_object_hash () {}

/**
 * Copy the iterator into an array
 * @link http://php.net/manual/en/function.iterator-to-array.php
 * @param iterator Traversable
 * @param use_keys[optional]
 */
function iterator_to_array (Traversable $iterator, $use_keys) {}

/**
 * Count the elements in an iterator
 * @link http://php.net/manual/en/function.iterator-count.php
 * @param iterator Traversable
 */
function iterator_count (Traversable $iterator) {}

/**
 * @param iterator Traversable
 * @param function
 * @param args[optional]
 */
function iterator_apply (Traversable $iterator, $functionarray , $args = null) {}

// End of SPL v.0.2

// Start of standard v.5.2.4

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
 * @param name string
 * @return mixed the value of the constant, or &null; if the constant is not
 */
function constant ($name) {}

/**
 * Convert binary data into hexadecimal representation
 * @link http://php.net/manual/en/function.bin2hex.php
 * @param str string
 * @return string the hexadecimal representation of the given string.
 */
function bin2hex ($str) {}

/**
 * Delay execution
 * @link http://php.net/manual/en/function.sleep.php
 * @param seconds int
 * @return int zero on success, or false on errors.
 */
function sleep ($seconds) {}

/**
 * Delay execution in microseconds
 * @link http://php.net/manual/en/function.usleep.php
 * @param micro_seconds int
 * @return void 
 */
function usleep ($micro_seconds) {}

/**
 * Delay for a number of seconds and nanoseconds
 * @link http://php.net/manual/en/function.time-nanosleep.php
 * @param seconds int
 * @param nanoseconds int
 * @return mixed 
 */
function time_nanosleep ($seconds, $nanoseconds) {}

/**
 * Make the script sleep until the specified time
 * @link http://php.net/manual/en/function.time-sleep-until.php
 * @param timestamp float
 * @return bool 
 */
function time_sleep_until ($timestamp) {}

/**
 * Parse a time/date generated with <function>strftime</function>
 * @link http://php.net/manual/en/function.strptime.php
 * @param date string
 * @param format string
 * @return array an array, or false on failure.
 */
function strptime ($date, $format) {}

/**
 * Flush the output buffer
 * @link http://php.net/manual/en/function.flush.php
 * @return void 
 */
function flush () {}

/**
 * Wraps a string to a given number of characters
 * @link http://php.net/manual/en/function.wordwrap.php
 * @param str string
 * @param width int[optional]
 * @param break string[optional]
 * @param cut bool[optional]
 * @return string the given string wrapped at the specified column.
 */
function wordwrap ($str, $width = null, $break = null, $cut = null) {}

/**
 * Convert special characters to HTML entities
 * @link http://php.net/manual/en/function.htmlspecialchars.php
 * @param string
 * @param quote_style[optional]
 * @param charset[optional]
 * @param double_encode[optional]
 */
function htmlspecialchars ($string, $quote_style, $charset, $double_encode) {}

/**
 * Convert all applicable characters to HTML entities
 * @link http://php.net/manual/en/function.htmlentities.php
 * @param string string
 * @param quote_style int[optional]
 * @param charset string[optional]
 * @param double_encode bool[optional]
 * @return string the encoded string.
 */
function htmlentities ($string, $quote_style = null, $charset = null, $double_encode = null) {}

/**
 * Convert all HTML entities to their applicable characters
 * @link http://php.net/manual/en/function.html-entity-decode.php
 * @param string string
 * @param quote_style int[optional]
 * @param charset string[optional]
 * @return string the decoded string.
 */
function html_entity_decode ($string, $quote_style = null, $charset = null) {}

/**
 * Convert special HTML entities back to characters
 * @link http://php.net/manual/en/function.htmlspecialchars-decode.php
 * @param string string
 * @param quote_style int[optional]
 * @return string the decoded string.
 */
function htmlspecialchars_decode ($string, $quote_style = null) {}

/**
 * Returns the translation table used by <function>htmlspecialchars</function> and <function>htmlentities</function>
 * @link http://php.net/manual/en/function.get-html-translation-table.php
 * @param table int[optional]
 * @param quote_style int[optional]
 * @return array the translation table as an array.
 */
function get_html_translation_table ($table = null, $quote_style = null) {}

/**
 * Calculate the sha1 hash of a string
 * @link http://php.net/manual/en/function.sha1.php
 * @param str string
 * @param raw_output bool[optional]
 * @return string the sha1 hash as a string.
 */
function sha1 ($str, $raw_output = null) {}

/**
 * Calculate the sha1 hash of a file
 * @link http://php.net/manual/en/function.sha1-file.php
 * @param filename string
 * @param raw_output bool[optional]
 * @return string a string on success, false otherwise.
 */
function sha1_file ($filename, $raw_output = null) {}

/**
 * Calculate the md5 hash of a string
 * @link http://php.net/manual/en/function.md5.php
 * @param str string
 * @param raw_output bool[optional]
 * @return string the hash as a 32-character hexadecimal number.
 */
function md5 ($str, $raw_output = null) {}

/**
 * Calculates the md5 hash of a given file
 * @link http://php.net/manual/en/function.md5-file.php
 * @param filename string
 * @param raw_output bool[optional]
 * @return string a string on success, false otherwise.
 */
function md5_file ($filename, $raw_output = null) {}

/**
 * Calculates the crc32 polynomial of a string
 * @link http://php.net/manual/en/function.crc32.php
 * @param str string
 * @return int 
 */
function crc32 ($str) {}

/**
 * Parse a binary IPTC block into single tags.
 * @link http://php.net/manual/en/function.iptcparse.php
 * @param iptcblock string
 * @return array an array using the tagmarker as an index and the value as the
 */
function iptcparse ($iptcblock) {}

/**
 * Embed binary IPTC data into a JPEG image
 * @link http://php.net/manual/en/function.iptcembed.php
 * @param iptcdata string
 * @param jpeg_file_name string
 * @param spool int[optional]
 * @return mixed 
 */
function iptcembed ($iptcdata, $jpeg_file_name, $spool = null) {}

/**
 * Get the size of an image
 * @link http://php.net/manual/en/function.getimagesize.php
 * @param filename string
 * @param imageinfo array[optional]
 * @return array an array with 5 elements.
 */
function getimagesize ($filename, array &$imageinfo = null) {}

/**
 * Get Mime-Type for image-type returned by getimagesize,
   exif_read_data, exif_thumbnail, exif_imagetype
 * @link http://php.net/manual/en/function.image-type-to-mime-type.php
 * @param imagetype int
 * @return string 
 */
function image_type_to_mime_type ($imagetype) {}

/**
 * Get file extension for image type
 * @link http://php.net/manual/en/function.image-type-to-extension.php
 * @param imagetype int
 * @param include_dot bool[optional]
 * @return string 
 */
function image_type_to_extension ($imagetype, $include_dot = null) {}

/**
 * Outputs lots of PHP information
 * @link http://php.net/manual/en/function.phpinfo.php
 * @param what int[optional]
 * @return bool 
 */
function phpinfo ($what = null) {}

/**
 * Gets the current PHP version
 * @link http://php.net/manual/en/function.phpversion.php
 * @param extension string[optional]
 * @return string 
 */
function phpversion ($extension = null) {}

/**
 * Prints out the credits for PHP
 * @link http://php.net/manual/en/function.phpcredits.php
 * @param flag int[optional]
 * @return bool 
 */
function phpcredits ($flag = null) {}

/**
 * Gets the logo guid
 * @link http://php.net/manual/en/function.php-logo-guid.php
 * @return string PHPE9568F34-D428-11d2-A769-00AA001ACF42.
 */
function php_logo_guid () {}

function php_real_logo_guid () {}

function php_egg_logo_guid () {}

/**
 * Gets the Zend guid
 * @link http://php.net/manual/en/function.zend-logo-guid.php
 * @return string PHPE9568F35-D428-11d2-A769-00AA001ACF42.
 */
function zend_logo_guid () {}

/**
 * Returns the type of interface between web server and PHP
 * @link http://php.net/manual/en/function.php-sapi-name.php
 * @return string the interface type, as a lowercase string.
 */
function php_sapi_name () {}

/**
 * Returns information about the operating system PHP is running on
 * @link http://php.net/manual/en/function.php-uname.php
 * @param mode string[optional]
 * @return string the description, as a string.
 */
function php_uname ($mode = null) {}

/**
 * Return a list of .ini files parsed from the additional ini dir
 * @link http://php.net/manual/en/function.php-ini-scanned-files.php
 * @return string a comma-separated string of .ini files on success. Each comma is
 */
function php_ini_scanned_files () {}

function php_ini_loaded_file () {}

/**
 * String comparisons using a "natural order" algorithm
 * @link http://php.net/manual/en/function.strnatcmp.php
 * @param str1 string
 * @param str2 string
 * @return int 
 */
function strnatcmp ($str1, $str2) {}

/**
 * Case insensitive string comparisons using a "natural order" algorithm
 * @link http://php.net/manual/en/function.strnatcasecmp.php
 * @param str1 string
 * @param str2 string
 * @return int 
 */
function strnatcasecmp ($str1, $str2) {}

/**
 * Count the number of substring occurrences
 * @link http://php.net/manual/en/function.substr-count.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @param length int[optional]
 * @return int 
 */
function substr_count ($haystack, $needle, $offset = null, $length = null) {}

/**
 * Find length of initial segment matching mask
 * @link http://php.net/manual/en/function.strspn.php
 * @param str1 string
 * @param str2 string
 * @param start int[optional]
 * @param length int[optional]
 * @return int the length of the initial segment of str1
 */
function strspn ($str1, $str2, $start = null, $length = null) {}

/**
 * Find length of initial segment not matching mask
 * @link http://php.net/manual/en/function.strcspn.php
 * @param str1 string
 * @param str2 string
 * @param start int[optional]
 * @param length int[optional]
 * @return int the length of the segment as an integer.
 */
function strcspn ($str1, $str2, $start = null, $length = null) {}

/**
 * Tokenize string
 * @link http://php.net/manual/en/function.strtok.php
 * @param str
 * @param token
 */
function strtok ($str, $token) {}

/**
 * Make a string uppercase
 * @link http://php.net/manual/en/function.strtoupper.php
 * @param string string
 * @return string the uppercased string.
 */
function strtoupper ($string) {}

/**
 * Make a string lowercase
 * @link http://php.net/manual/en/function.strtolower.php
 * @param str string
 * @return string the lowercased string.
 */
function strtolower ($str) {}

/**
 * Find position of first occurrence of a string
 * @link http://php.net/manual/en/function.strpos.php
 * @param haystack string
 * @param needle mixed
 * @param offset int[optional]
 * @return int the position as an integer. If needle is
 */
function strpos ($haystack, $needle, $offset = null) {}

/**
 * Find position of first occurrence of a case-insensitive string
 * @link http://php.net/manual/en/function.stripos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @return int 
 */
function stripos ($haystack, $needle, $offset = null) {}

/**
 * Find position of last occurrence of a char in a string
 * @link http://php.net/manual/en/function.strrpos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @return int 
 */
function strrpos ($haystack, $needle, $offset = null) {}

/**
 * Find position of last occurrence of a case-insensitive string in a string
 * @link http://php.net/manual/en/function.strripos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @return int the numerical position of the last occurence of
 */
function strripos ($haystack, $needle, $offset = null) {}

/**
 * Reverse a string
 * @link http://php.net/manual/en/function.strrev.php
 * @param string string
 * @return string the reversed string.
 */
function strrev ($string) {}

/**
 * Convert logical Hebrew text to visual text
 * @link http://php.net/manual/en/function.hebrev.php
 * @param hebrew_text string
 * @param max_chars_per_line int[optional]
 * @return string the visual string.
 */
function hebrev ($hebrew_text, $max_chars_per_line = null) {}

/**
 * Convert logical Hebrew text to visual text with newline conversion
 * @link http://php.net/manual/en/function.hebrevc.php
 * @param hebrew_text string
 * @param max_chars_per_line int[optional]
 * @return string the visual string.
 */
function hebrevc ($hebrew_text, $max_chars_per_line = null) {}

/**
 * Inserts HTML line breaks before all newlines in a string
 * @link http://php.net/manual/en/function.nl2br.php
 * @param string string
 * @return string the altered string.
 */
function nl2br ($string) {}

/**
 * Returns filename component of path
 * @link http://php.net/manual/en/function.basename.php
 * @param path string
 * @param suffix string[optional]
 * @return string the base name of the given path.
 */
function basename ($path, $suffix = null) {}

/**
 * Returns directory name component of path
 * @link http://php.net/manual/en/function.dirname.php
 * @param path string
 * @return string the name of the directory. If there are no slashes in
 */
function dirname ($path) {}

/**
 * Returns information about a file path
 * @link http://php.net/manual/en/function.pathinfo.php
 * @param path string
 * @param options int[optional]
 * @return mixed 
 */
function pathinfo ($path, $options = null) {}

/**
 * Un-quote string quoted with <function>addslashes</function>
 * @link http://php.net/manual/en/function.stripslashes.php
 * @param str string
 * @return string a string with backslashes stripped off.
 */
function stripslashes ($str) {}

/**
 * Un-quote string quoted with <function>addcslashes</function>
 * @link http://php.net/manual/en/function.stripcslashes.php
 * @param str string
 * @return string the unescaped string.
 */
function stripcslashes ($str) {}

/**
 * Find first occurrence of a string
 * @link http://php.net/manual/en/function.strstr.php
 * @param haystack string
 * @param needle string
 * @param before_needle bool
 * @return string the portion of string, or false if needle
 */
function strstr ($haystack, $needle, $before_needle) {}

/**
 * Case-insensitive <function>strstr</function>
 * @link http://php.net/manual/en/function.stristr.php
 * @param haystack string
 * @param needle string
 * @param before_needle bool
 * @return string the matched substring. If needle is not
 */
function stristr ($haystack, $needle, $before_needle) {}

/**
 * Find the last occurrence of a character in a string
 * @link http://php.net/manual/en/function.strrchr.php
 * @param haystack string
 * @param needle string
 * @return string 
 */
function strrchr ($haystack, $needle) {}

/**
 * Randomly shuffles a string
 * @link http://php.net/manual/en/function.str-shuffle.php
 * @param str string
 * @return string the shuffled string.
 */
function str_shuffle ($str) {}

/**
 * Return information about words used in a string
 * @link http://php.net/manual/en/function.str-word-count.php
 * @param string string
 * @param format int[optional]
 * @param charlist string[optional]
 * @return mixed an array or an integer, depending on the
 */
function str_word_count ($string, $format = null, $charlist = null) {}

/**
 * Convert a string to an array
 * @link http://php.net/manual/en/function.str-split.php
 * @param string string
 * @param split_length int[optional]
 * @return array 
 */
function str_split ($string, $split_length = null) {}

/**
 * Search a string for any of a set of characters
 * @link http://php.net/manual/en/function.strpbrk.php
 * @param haystack string
 * @param char_list string
 * @return string a string starting from the character found, or false if it is
 */
function strpbrk ($haystack, $char_list) {}

/**
 * Binary safe comparison of 2 strings from an offset, up to length characters
 * @link http://php.net/manual/en/function.substr-compare.php
 * @param main_str string
 * @param str string
 * @param offset int
 * @param length int[optional]
 * @param case_insensitivity bool[optional]
 * @return int &lt; 0 if main_str from position
 */
function substr_compare ($main_str, $str, $offset, $length = null, $case_insensitivity = null) {}

/**
 * Locale based string comparison
 * @link http://php.net/manual/en/function.strcoll.php
 * @param str1 string
 * @param str2 string
 * @return int &lt; 0 if str1 is less than
 */
function strcoll ($str1, $str2) {}

/**
 * Formats a number as a currency string
 * @link http://php.net/manual/en/function.money-format.php
 * @param format string
 * @param number float
 * @return string the formatted string. Characters before and after the formatting
 */
function money_format ($format, $number) {}

/**
 * Return part of a string
 * @link http://php.net/manual/en/function.substr.php
 * @param string string
 * @param start int
 * @param length int[optional]
 * @return string the extracted part of string.
 */
function substr ($string, $start, $length = null) {}

/**
 * Replace text within a portion of a string
 * @link http://php.net/manual/en/function.substr-replace.php
 * @param string mixed
 * @param replacement string
 * @param start int
 * @param length int[optional]
 * @return mixed 
 */
function substr_replace ($string, $replacement, $start, $length = null) {}

/**
 * Quote meta characters
 * @link http://php.net/manual/en/function.quotemeta.php
 * @param str string
 * @return string the string with meta characters quoted.
 */
function quotemeta ($str) {}

/**
 * Make a string's first character uppercase
 * @link http://php.net/manual/en/function.ucfirst.php
 * @param str string
 * @return string the resulting string.
 */
function ucfirst ($str) {}

/**
 * Uppercase the first character of each word in a string
 * @link http://php.net/manual/en/function.ucwords.php
 * @param str string
 * @return string the modified string.
 */
function ucwords ($str) {}

/**
 * Translate certain characters
 * @link http://php.net/manual/en/function.strtr.php
 * @param str
 * @param from
 * @param to[optional]
 */
function strtr ($str, $from, $to) {}

/**
 * Quote string with slashes
 * @link http://php.net/manual/en/function.addslashes.php
 * @param str string
 * @return string the escaped string.
 */
function addslashes ($str) {}

/**
 * Quote string with slashes in a C style
 * @link http://php.net/manual/en/function.addcslashes.php
 * @param str string
 * @param charlist string
 * @return string the escaped string.
 */
function addcslashes ($str, $charlist) {}

/**
 * Strip whitespace (or other characters) from the end of a string
 * @link http://php.net/manual/en/function.rtrim.php
 * @param str string
 * @param charlist string[optional]
 * @return string the modified string.
 */
function rtrim ($str, $charlist = null) {}

/**
 * Replace all occurrences of the search string with the replacement string
 * @link http://php.net/manual/en/function.str-replace.php
 * @param search mixed
 * @param replace mixed
 * @param subject mixed
 * @param count int[optional]
 * @return mixed 
 */
function str_replace ($search, $replace, $subject, &$count = null) {}

/**
 * Case-insensitive version of <function>str_replace</function>.
 * @link http://php.net/manual/en/function.str-ireplace.php
 * @param search mixed
 * @param replace mixed
 * @param subject mixed
 * @param count int[optional]
 * @return mixed a string or an array of replacements.
 */
function str_ireplace ($search, $replace, $subject, &$count = null) {}

/**
 * Repeat a string
 * @link http://php.net/manual/en/function.str-repeat.php
 * @param input string
 * @param multiplier int
 * @return string the repeated string.
 */
function str_repeat ($input, $multiplier) {}

/**
 * Return information about characters used in a string
 * @link http://php.net/manual/en/function.count-chars.php
 * @param string string
 * @param mode int[optional]
 * @return mixed 
 */
function count_chars ($string, $mode = null) {}

/**
 * Split a string into smaller chunks
 * @link http://php.net/manual/en/function.chunk-split.php
 * @param body string
 * @param chunklen int[optional]
 * @param end string[optional]
 * @return string the chunked string.
 */
function chunk_split ($body, $chunklen = null, $end = null) {}

/**
 * Strip whitespace (or other characters) from the beginning and end of a string
 * @link http://php.net/manual/en/function.trim.php
 * @param str string
 * @param charlist string[optional]
 * @return string 
 */
function trim ($str, $charlist = null) {}

/**
 * Strip whitespace (or other characters) from the beginning of a string
 * @link http://php.net/manual/en/function.ltrim.php
 * @param str string
 * @param charlist string[optional]
 * @return string 
 */
function ltrim ($str, $charlist = null) {}

/**
 * Strip HTML and PHP tags from a string
 * @link http://php.net/manual/en/function.strip-tags.php
 * @param str string
 * @param allowable_tags string[optional]
 * @return string the stripped string.
 */
function strip_tags ($str, $allowable_tags = null) {}

/**
 * Calculate the similarity between two strings
 * @link http://php.net/manual/en/function.similar-text.php
 * @param first string
 * @param second string
 * @param percent float[optional]
 * @return int the number of matching chars in both strings.
 */
function similar_text ($first, $second, &$percent = null) {}

/**
 * Split a string by string
 * @link http://php.net/manual/en/function.explode.php
 * @param delimiter string
 * @param string string
 * @param limit int[optional]
 * @return array 
 */
function explode ($delimiter, $string, $limit = null) {}

/**
 * Join array elements with a string
 * @link http://php.net/manual/en/function.implode.php
 * @param glue string
 * @param pieces array
 * @return string a string containing a string representation of all the array
 */
function implode ($glue, array $pieces) {}

/**
 * Set locale information
 * @link http://php.net/manual/en/function.setlocale.php
 * @param category int
 * @param locale string
 * @param ... string[optional]
 * @return string the new current locale, or false if the locale functionality is
 */
function setlocale ($category, $locale) {}

/**
 * Get numeric formatting information
 * @link http://php.net/manual/en/function.localeconv.php
 * @return array 
 */
function localeconv () {}

/**
 * Query language and locale information
 * @link http://php.net/manual/en/function.nl-langinfo.php
 * @param item int
 * @return string the element as a string, or false if item
 */
function nl_langinfo ($item) {}

/**
 * Calculate the soundex key of a string
 * @link http://php.net/manual/en/function.soundex.php
 * @param str string
 * @return string the soundex key as a string.
 */
function soundex ($str) {}

/**
 * Calculate Levenshtein distance between two strings
 * @link http://php.net/manual/en/function.levenshtein.php
 * @param str1
 * @param str2
 * @param cost_ins
 * @param cost_rep
 * @param cost_del
 */
function levenshtein ($str1, $str2, $cost_ins, $cost_rep, $cost_del) {}

/**
 * Return a specific character
 * @link http://php.net/manual/en/function.chr.php
 * @param ascii int
 * @return string the specified character.
 */
function chr ($ascii) {}

/**
 * Return ASCII value of character
 * @link http://php.net/manual/en/function.ord.php
 * @param string string
 * @return int the ASCII value as an integer.
 */
function ord ($string) {}

/**
 * Parses the string into variables
 * @link http://php.net/manual/en/function.parse-str.php
 * @param str string
 * @param arr array[optional]
 * @return void 
 */
function parse_str ($str, array &$arr = null) {}

/**
 * Pad a string to a certain length with another string
 * @link http://php.net/manual/en/function.str-pad.php
 * @param input string
 * @param pad_length int
 * @param pad_string string[optional]
 * @param pad_type int[optional]
 * @return string the padded string.
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
 * @param format string
 * @param args mixed[optional]
 * @param ... mixed[optional]
 * @return string a string produced according to the formatting string
 */
function sprintf ($format, $args = null) {}

/**
 * Output a formatted string
 * @link http://php.net/manual/en/function.printf.php
 * @param format string
 * @param args mixed[optional]
 * @param ... mixed[optional]
 * @return int the length of the outputted string.
 */
function printf ($format, $args = null) {}

/**
 * Output a formatted string
 * @link http://php.net/manual/en/function.vprintf.php
 * @param format string
 * @param args array
 * @return int the length of the outputted string.
 */
function vprintf ($format, array $args) {}

/**
 * Return a formatted string
 * @link http://php.net/manual/en/function.vsprintf.php
 * @param format string
 * @param args array
 * @return string 
 */
function vsprintf ($format, array $args) {}

/**
 * Write a formatted string to a stream
 * @link http://php.net/manual/en/function.fprintf.php
 * @param handle resource
 * @param format string
 * @param args mixed[optional]
 * @param ... mixed[optional]
 * @return int the length of the outputted string.
 */
function fprintf ($handle, $format, $args = null) {}

/**
 * Write a formatted string to a stream
 * @link http://php.net/manual/en/function.vfprintf.php
 * @param handle resource
 * @param format string
 * @param args array
 * @return int the length of the outputted string.
 */
function vfprintf ($handle, $format, array $args) {}

/**
 * Parses input from a string according to a format
 * @link http://php.net/manual/en/function.sscanf.php
 * @param str
 * @param format
 * @param ...[optional]
 */
function sscanf ($str, $format) {}

/**
 * Parses input from a file according to a format
 * @link http://php.net/manual/en/function.fscanf.php
 * @param handle resource
 * @param format string
 * @param ... mixed[optional]
 * @return mixed 
 */
function fscanf ($handle, $format) {}

/**
 * Parse a URL and return its components
 * @link http://php.net/manual/en/function.parse-url.php
 * @param url string
 * @param component int[optional]
 * @return mixed 
 */
function parse_url ($url, $component = null) {}

/**
 * URL-encodes string
 * @link http://php.net/manual/en/function.urlencode.php
 * @param str string
 * @return string a string in which all non-alphanumeric characters except
 */
function urlencode ($str) {}

/**
 * Decodes URL-encoded string
 * @link http://php.net/manual/en/function.urldecode.php
 * @param str string
 * @return string the decoded string.
 */
function urldecode ($str) {}

/**
 * URL-encode according to RFC 1738
 * @link http://php.net/manual/en/function.rawurlencode.php
 * @param str string
 * @return string a string in which all non-alphanumeric characters except
 */
function rawurlencode ($str) {}

/**
 * Decode URL-encoded strings
 * @link http://php.net/manual/en/function.rawurldecode.php
 * @param str string
 * @return string the decoded URL, as a string.
 */
function rawurldecode ($str) {}

/**
 * Generate URL-encoded query string
 * @link http://php.net/manual/en/function.http-build-query.php
 * @param formdata array
 * @param numeric_prefix string[optional]
 * @param arg_separator string[optional]
 * @return string a URL-encoded string.
 */
function http_build_query (array $formdata, $numeric_prefix = null, $arg_separator = null) {}

/**
 * Returns the target of a symbolic link
 * @link http://php.net/manual/en/function.readlink.php
 * @param path string
 * @return string the contents of the symbolic link path or false on error.
 */
function readlink ($path) {}

/**
 * Gets information about a link
 * @link http://php.net/manual/en/function.linkinfo.php
 * @param path string
 * @return int 
 */
function linkinfo ($path) {}

/**
 * Creates a symbolic link
 * @link http://php.net/manual/en/function.symlink.php
 * @param target string
 * @param link string
 * @return bool 
 */
function symlink ($target, $link) {}

/**
 * Create a hard link
 * @link http://php.net/manual/en/function.link.php
 * @param target string
 * @param link string
 * @return bool 
 */
function link ($target, $link) {}

/**
 * Deletes a file
 * @link http://php.net/manual/en/function.unlink.php
 * @param filename string
 * @param context resource[optional]
 * @return bool 
 */
function unlink ($filename, $context = null) {}

/**
 * Execute an external program
 * @link http://php.net/manual/en/function.exec.php
 * @param command string
 * @param output array[optional]
 * @param return_var int[optional]
 * @return string 
 */
function exec ($command, array &$output = null, &$return_var = null) {}

/**
 * Execute an external program and display the output
 * @link http://php.net/manual/en/function.system.php
 * @param command string
 * @param return_var int[optional]
 * @return string the last line of the command output on success, and false
 */
function system ($command, &$return_var = null) {}

/**
 * Escape shell metacharacters
 * @link http://php.net/manual/en/function.escapeshellcmd.php
 * @param command string
 * @return string 
 */
function escapeshellcmd ($command) {}

/**
 * Escape a string to be used as a shell argument
 * @link http://php.net/manual/en/function.escapeshellarg.php
 * @param arg string
 * @return string 
 */
function escapeshellarg ($arg) {}

/**
 * Execute an external program and display raw output
 * @link http://php.net/manual/en/function.passthru.php
 * @param command string
 * @param return_var int[optional]
 * @return void 
 */
function passthru ($command, &$return_var = null) {}

/**
 * Execute command via shell and return the complete output as a string
 * @link http://php.net/manual/en/function.shell-exec.php
 * @param cmd string
 * @return string 
 */
function shell_exec ($cmd) {}

/**
 * Execute a command and open file pointers for input/output
 * @link http://php.net/manual/en/function.proc-open.php
 * @param cmd string
 * @param descriptorspec array
 * @param pipes array
 * @param cwd string[optional]
 * @param env array[optional]
 * @param other_options array[optional]
 * @return resource a resource representing the process, which should be freed using
 */
function proc_open ($cmd, array $descriptorspec, array &$pipes, $cwd = null, array $env = null, array $other_options = null) {}

/**
 * Close a process opened by <function>proc_open</function> and return the exit code of that process.
 * @link http://php.net/manual/en/function.proc-close.php
 * @param process resource
 * @return int the termination status of the process that was run.
 */
function proc_close ($process) {}

/**
 * Kills a process opened by proc_open
 * @link http://php.net/manual/en/function.proc-terminate.php
 * @param process resource
 * @param signal int[optional]
 * @return bool the termination status of the process that was run.
 */
function proc_terminate ($process, $signal = null) {}

/**
 * Get information about a process opened by <function>proc_open</function>
 * @link http://php.net/manual/en/function.proc-get-status.php
 * @param process resource
 * @return array 
 */
function proc_get_status ($process) {}

/**
 * Change the priority of the current process
 * @link http://php.net/manual/en/function.proc-nice.php
 * @param increment int
 * @return bool 
 */
function proc_nice ($increment) {}

/**
 * Generate a random integer
 * @link http://php.net/manual/en/function.rand.php
 * @param min int[optional]
 * @param max int
 * @return int 
 */
function rand ($min = null, $max) {}

/**
 * Seed the random number generator
 * @link http://php.net/manual/en/function.srand.php
 * @param seed int[optional]
 * @return void 
 */
function srand ($seed = null) {}

/**
 * Show largest possible random value
 * @link http://php.net/manual/en/function.getrandmax.php
 * @return int 
 */
function getrandmax () {}

/**
 * Generate a better random value
 * @link http://php.net/manual/en/function.mt-rand.php
 * @param min int[optional]
 * @param max int
 * @return int 
 */
function mt_rand ($min = null, $max) {}

/**
 * Seed the better random number generator
 * @link http://php.net/manual/en/function.mt-srand.php
 * @param seed int[optional]
 * @return void 
 */
function mt_srand ($seed = null) {}

/**
 * Show largest possible random value
 * @link http://php.net/manual/en/function.mt-getrandmax.php
 * @return int the maximum random value returned by mt_rand
 */
function mt_getrandmax () {}

/**
 * Get port number associated with an Internet service and protocol
 * @link http://php.net/manual/en/function.getservbyname.php
 * @param service string
 * @param protocol string
 * @return int the port number, or false if service or
 */
function getservbyname ($service, $protocol) {}

/**
 * Get Internet service which corresponds to port and protocol
 * @link http://php.net/manual/en/function.getservbyport.php
 * @param port int
 * @param protocol string
 * @return string the Internet service name as a string.
 */
function getservbyport ($port, $protocol) {}

/**
 * Get protocol number associated with protocol name
 * @link http://php.net/manual/en/function.getprotobyname.php
 * @param name string
 * @return int the protocol number or -1 if the protocol is not found.
 */
function getprotobyname ($name) {}

/**
 * Get protocol name associated with protocol number
 * @link http://php.net/manual/en/function.getprotobynumber.php
 * @param number int
 * @return string the protocol name as a string.
 */
function getprotobynumber ($number) {}

/**
 * Gets PHP script owner's UID
 * @link http://php.net/manual/en/function.getmyuid.php
 * @return int the user ID of the current script, or false on error.
 */
function getmyuid () {}

/**
 * Get PHP script owner's GID
 * @link http://php.net/manual/en/function.getmygid.php
 * @return int the group ID of the current script, or false on error.
 */
function getmygid () {}

/**
 * Gets PHP's process ID
 * @link http://php.net/manual/en/function.getmypid.php
 * @return int the current PHP process ID, or false on error.
 */
function getmypid () {}

/**
 * Gets the inode of the current script
 * @link http://php.net/manual/en/function.getmyinode.php
 * @return int the current script's inode as a string, or false on error.
 */
function getmyinode () {}

/**
 * Gets time of last page modification
 * @link http://php.net/manual/en/function.getlastmod.php
 * @return int the time of the last modification of the current
 */
function getlastmod () {}

/**
 * Decodes data encoded with MIME base64
 * @link http://php.net/manual/en/function.base64-decode.php
 * @param data string
 * @param strict bool[optional]
 * @return string the original data or false on failure. The returned data may be
 */
function base64_decode ($data, $strict = null) {}

/**
 * Encodes data with MIME base64
 * @link http://php.net/manual/en/function.base64-encode.php
 * @param data string
 * @return string 
 */
function base64_encode ($data) {}

/**
 * Uuencode a string
 * @link http://php.net/manual/en/function.convert-uuencode.php
 * @param data string
 * @return string the uuencoded data.
 */
function convert_uuencode ($data) {}

/**
 * Decode a uuencoded string
 * @link http://php.net/manual/en/function.convert-uudecode.php
 * @param data string
 * @return string the decoded data as a string.
 */
function convert_uudecode ($data) {}

/**
 * Absolute value
 * @link http://php.net/manual/en/function.abs.php
 * @param number mixed
 * @return number 
 */
function abs ($number) {}

/**
 * Round fractions up
 * @link http://php.net/manual/en/function.ceil.php
 * @param value float
 * @return float 
 */
function ceil ($value) {}

/**
 * Round fractions down
 * @link http://php.net/manual/en/function.floor.php
 * @param value float
 * @return float 
 */
function floor ($value) {}

/**
 * Rounds a float
 * @link http://php.net/manual/en/function.round.php
 * @param val float
 * @param precision int[optional]
 * @return float 
 */
function round ($val, $precision = null) {}

/**
 * Sine
 * @link http://php.net/manual/en/function.sin.php
 * @param arg float
 * @return float 
 */
function sin ($arg) {}

/**
 * Cosine
 * @link http://php.net/manual/en/function.cos.php
 * @param arg float
 * @return float 
 */
function cos ($arg) {}

/**
 * Tangent
 * @link http://php.net/manual/en/function.tan.php
 * @param arg float
 * @return float 
 */
function tan ($arg) {}

/**
 * Arc sine
 * @link http://php.net/manual/en/function.asin.php
 * @param arg float
 * @return float 
 */
function asin ($arg) {}

/**
 * Arc cosine
 * @link http://php.net/manual/en/function.acos.php
 * @param arg float
 * @return float 
 */
function acos ($arg) {}

/**
 * Arc tangent
 * @link http://php.net/manual/en/function.atan.php
 * @param arg float
 * @return float 
 */
function atan ($arg) {}

/**
 * Arc tangent of two variables
 * @link http://php.net/manual/en/function.atan2.php
 * @param y float
 * @param x float
 * @return float 
 */
function atan2 ($y, $x) {}

/**
 * Hyperbolic sine
 * @link http://php.net/manual/en/function.sinh.php
 * @param arg float
 * @return float 
 */
function sinh ($arg) {}

/**
 * Hyperbolic cosine
 * @link http://php.net/manual/en/function.cosh.php
 * @param arg float
 * @return float 
 */
function cosh ($arg) {}

/**
 * Hyperbolic tangent
 * @link http://php.net/manual/en/function.tanh.php
 * @param arg float
 * @return float 
 */
function tanh ($arg) {}

/**
 * Inverse hyperbolic sine
 * @link http://php.net/manual/en/function.asinh.php
 * @param arg float
 * @return float 
 */
function asinh ($arg) {}

/**
 * Inverse hyperbolic cosine
 * @link http://php.net/manual/en/function.acosh.php
 * @param arg float
 * @return float 
 */
function acosh ($arg) {}

/**
 * Inverse hyperbolic tangent
 * @link http://php.net/manual/en/function.atanh.php
 * @param arg float
 * @return float 
 */
function atanh ($arg) {}

/**
 * Returns exp(number) - 1, computed in a way that is accurate even
   when the value of number is close to zero
 * @link http://php.net/manual/en/function.expm1.php
 * @param arg float
 * @return float 
 */
function expm1 ($arg) {}

/**
 * Returns log(1 + number), computed in a way that is accurate even when
   the value of number is close to zero
 * @link http://php.net/manual/en/function.log1p.php
 * @param number float
 * @return float 
 */
function log1p ($number) {}

/**
 * Get value of pi
 * @link http://php.net/manual/en/function.pi.php
 * @return float 
 */
function pi () {}

/**
 * Finds whether a value is a legal finite number
 * @link http://php.net/manual/en/function.is-finite.php
 * @param val float
 * @return bool 
 */
function is_finite ($val) {}

/**
 * Finds whether a value is not a number
 * @link http://php.net/manual/en/function.is-nan.php
 * @param val float
 * @return bool true if val is 'not a number',
 */
function is_nan ($val) {}

/**
 * Finds whether a value is infinite
 * @link http://php.net/manual/en/function.is-infinite.php
 * @param val float
 * @return bool 
 */
function is_infinite ($val) {}

/**
 * Exponential expression
 * @link http://php.net/manual/en/function.pow.php
 * @param base number
 * @param exp number
 * @return number 
 */
function pow ($base, $exp) {}

/**
 * Calculates the exponent of <constant>e</constant>
 * @link http://php.net/manual/en/function.exp.php
 * @param arg float
 * @return float 
 */
function exp ($arg) {}

/**
 * Natural logarithm
 * @link http://php.net/manual/en/function.log.php
 * @param arg float
 * @param base float[optional]
 * @return float 
 */
function log ($arg, $base = null) {}

/**
 * Base-10 logarithm
 * @link http://php.net/manual/en/function.log10.php
 * @param arg float
 * @return float 
 */
function log10 ($arg) {}

/**
 * Square root
 * @link http://php.net/manual/en/function.sqrt.php
 * @param arg float
 * @return float 
 */
function sqrt ($arg) {}

/**
 * Calculate the length of the hypotenuse of a right-angle triangle
 * @link http://php.net/manual/en/function.hypot.php
 * @param x float
 * @param y float
 * @return float 
 */
function hypot ($x, $y) {}

/**
 * Converts the number in degrees to the radian equivalent
 * @link http://php.net/manual/en/function.deg2rad.php
 * @param number float
 * @return float 
 */
function deg2rad ($number) {}

/**
 * Converts the radian number to the equivalent number in degrees
 * @link http://php.net/manual/en/function.rad2deg.php
 * @param number float
 * @return float 
 */
function rad2deg ($number) {}

/**
 * Binary to decimal
 * @link http://php.net/manual/en/function.bindec.php
 * @param binary_string string
 * @return number 
 */
function bindec ($binary_string) {}

/**
 * Hexadecimal to decimal
 * @link http://php.net/manual/en/function.hexdec.php
 * @param hex_string string
 * @return number 
 */
function hexdec ($hex_string) {}

/**
 * Octal to decimal
 * @link http://php.net/manual/en/function.octdec.php
 * @param octal_string string
 * @return number 
 */
function octdec ($octal_string) {}

/**
 * Decimal to binary
 * @link http://php.net/manual/en/function.decbin.php
 * @param number int
 * @return string 
 */
function decbin ($number) {}

/**
 * Decimal to octal
 * @link http://php.net/manual/en/function.decoct.php
 * @param number int
 * @return string 
 */
function decoct ($number) {}

/**
 * Decimal to hexadecimal
 * @link http://php.net/manual/en/function.dechex.php
 * @param number int
 * @return string 
 */
function dechex ($number) {}

/**
 * Convert a number between arbitrary bases
 * @link http://php.net/manual/en/function.base-convert.php
 * @param number string
 * @param frombase int
 * @param tobase int
 * @return string 
 */
function base_convert ($number, $frombase, $tobase) {}

/**
 * Format a number with grouped thousands
 * @link http://php.net/manual/en/function.number-format.php
 * @param number
 * @param num_decimal_places[optional]
 * @param dec_seperator[optional]
 * @param thousands_seperator[optional]
 */
function number_format ($number, $num_decimal_places, $dec_seperator, $thousands_seperator) {}

/**
 * Returns the floating point remainder (modulo) of the division
  of the arguments
 * @link http://php.net/manual/en/function.fmod.php
 * @param x float
 * @param y float
 * @return float 
 */
function fmod ($x, $y) {}

/**
 * Converts a packed internet address to a human readable representation
 * @link http://php.net/manual/en/function.inet-ntop.php
 * @param in_addr string
 * @return string a string representation of the address or false on failure.
 */
function inet_ntop ($in_addr) {}

/**
 * Converts a human readable IP address to its packed in_addr representation
 * @link http://php.net/manual/en/function.inet-pton.php
 * @param address string
 * @return string the in_addr representation of the given
 */
function inet_pton ($address) {}

/**
 * Converts a string containing an (IPv4) Internet Protocol dotted address into a proper address
 * @link http://php.net/manual/en/function.ip2long.php
 * @param ip_address string
 * @return int the IPv4 address or false if ip_address
 */
function ip2long ($ip_address) {}

/**
 * Converts an (IPv4) Internet network address into a string in Internet standard dotted format
 * @link http://php.net/manual/en/function.long2ip.php
 * @param proper_address int
 * @return string the Internet IP address as a string.
 */
function long2ip ($proper_address) {}

/**
 * Gets the value of an environment variable
 * @link http://php.net/manual/en/function.getenv.php
 * @param varname string
 * @return string the value of the environment variable
 */
function getenv ($varname) {}

/**
 * Sets the value of an environment variable
 * @link http://php.net/manual/en/function.putenv.php
 * @param setting string
 * @return bool 
 */
function putenv ($setting) {}

/**
 * Gets options from the command line argument list
 * @link http://php.net/manual/en/function.getopt.php
 * @param options string
 * @param longopts array[optional]
 * @return array 
 */
function getopt ($options, array $longopts = null) {}

/**
 * Gets system load average
 * @link http://php.net/manual/en/function.sys-getloadavg.php
 * @return array an array with three samples (last 1, 5 and 15
 */
function sys_getloadavg () {}

/**
 * Return current Unix timestamp with microseconds
 * @link http://php.net/manual/en/function.microtime.php
 * @param get_as_float bool[optional]
 * @return mixed 
 */
function microtime ($get_as_float = null) {}

/**
 * Get current time
 * @link http://php.net/manual/en/function.gettimeofday.php
 * @param return_float bool[optional]
 * @return mixed 
 */
function gettimeofday ($return_float = null) {}

/**
 * Gets the current resource usages
 * @link http://php.net/manual/en/function.getrusage.php
 * @param who int[optional]
 * @return array an associative array containing the data returned from the system
 */
function getrusage ($who = null) {}

/**
 * Generate a unique ID
 * @link http://php.net/manual/en/function.uniqid.php
 * @param prefix string[optional]
 * @param more_entropy bool[optional]
 * @return string the unique identifier, as a string.
 */
function uniqid ($prefix = null, $more_entropy = null) {}

/**
 * Convert a quoted-printable string to an 8 bit string
 * @link http://php.net/manual/en/function.quoted-printable-decode.php
 * @param str string
 * @return string the 8-bit binary string.
 */
function quoted_printable_decode ($str) {}

/**
 * Convert from one Cyrillic character set to another
 * @link http://php.net/manual/en/function.convert-cyr-string.php
 * @param str string
 * @param from string
 * @param to string
 * @return string the converted string.
 */
function convert_cyr_string ($str, $from, $to) {}

/**
 * Gets the name of the owner of the current PHP script
 * @link http://php.net/manual/en/function.get-current-user.php
 * @return string the username as a string.
 */
function get_current_user () {}

/**
 * Limits the maximum execution time
 * @link http://php.net/manual/en/function.set-time-limit.php
 * @param seconds int
 * @return void 
 */
function set_time_limit ($seconds) {}

/**
 * Gets the value of a PHP configuration option
 * @link http://php.net/manual/en/function.get-cfg-var.php
 * @param option string
 * @return string the current value of the PHP configuration variable specified by
 */
function get_cfg_var ($option) {}

function magic_quotes_runtime () {}

/**
 * Sets the current active configuration setting of magic_quotes_runtime
 * @link http://php.net/manual/en/function.set-magic-quotes-runtime.php
 * @param new_setting int
 * @return bool 
 */
function set_magic_quotes_runtime ($new_setting) {}

/**
 * Gets the current configuration setting of magic quotes gpc
 * @link http://php.net/manual/en/function.get-magic-quotes-gpc.php
 * @return int 0 if magic quotes gpc are off, 1 otherwise.
 */
function get_magic_quotes_gpc () {}

/**
 * Gets the current active configuration setting of magic_quotes_runtime
 * @link http://php.net/manual/en/function.get-magic-quotes-runtime.php
 * @return int 0 if magic quotes runtime is off, 1 otherwise.
 */
function get_magic_quotes_runtime () {}

/**
 * Import GET/POST/Cookie variables into the global scope
 * @link http://php.net/manual/en/function.import-request-variables.php
 * @param types string
 * @param prefix string[optional]
 * @return bool 
 */
function import_request_variables ($types, $prefix = null) {}

/**
 * Send an error message somewhere
 * @link http://php.net/manual/en/function.error-log.php
 * @param message string
 * @param message_type int[optional]
 * @param destination string[optional]
 * @param extra_headers string[optional]
 * @return bool 
 */
function error_log ($message, $message_type = null, $destination = null, $extra_headers = null) {}

/**
 * Get the last occurred error
 * @link http://php.net/manual/en/function.error-get-last.php
 * @return array an associative array describing the last error with keys "type",
 */
function error_get_last () {}

/**
 * Call a user function given by the first parameter
 * @link http://php.net/manual/en/function.call-user-func.php
 * @param function callback
 * @param parameter mixed[optional]
 * @param ... mixed[optional]
 * @return mixed the function result, or false on error.
 */
function call_user_func ($function, $parameter = null) {}

/**
 * Call a user function given with an array of parameters
 * @link http://php.net/manual/en/function.call-user-func-array.php
 * @param function callback
 * @param param_arr array
 * @return mixed the function result, or false on error.
 */
function call_user_func_array ($function, array $param_arr) {}

/**
 * Call a user method on an specific object [deprecated]
 * @link http://php.net/manual/en/function.call-user-method.php
 * @param method_name string
 * @param obj object
 * @param parameter mixed[optional]
 * @param ... mixed[optional]
 * @return mixed 
 */
function call_user_method ($method_name, &$obj, $parameter = null) {}

/**
 * Call a user method given with an array of parameters [deprecated]
 * @link http://php.net/manual/en/function.call-user-method-array.php
 * @param method_name string
 * @param obj object
 * @param paramarr array
 * @return mixed 
 */
function call_user_method_array ($method_name, &$obj, array $paramarr) {}

/**
 * Generates a storable representation of a value
 * @link http://php.net/manual/en/function.serialize.php
 * @param value mixed
 * @return string a string containing a byte-stream representation of
 */
function serialize ($value) {}

/**
 * Creates a PHP value from a stored representation
 * @link http://php.net/manual/en/function.unserialize.php
 * @param str string
 * @return mixed 
 */
function unserialize ($str) {}

/**
 * Dumps information about a variable
 * @link http://php.net/manual/en/function.var-dump.php
 * @param expression mixed
 * @param expression mixed[optional]
 * @return void 
 */
function var_dump ($expression, $expression = null) {}

/**
 * Outputs or returns a parsable string representation of a variable
 * @link http://php.net/manual/en/function.var-export.php
 * @param expression mixed
 * @param return bool[optional]
 * @return mixed the variable representation when the return
 */
function var_export ($expression, $return = null) {}

/**
 * Dumps a string representation of an internal zend value to output
 * @link http://php.net/manual/en/function.debug-zval-dump.php
 * @param variable mixed
 * @return void 
 */
function debug_zval_dump ($variable) {}

/**
 * Prints human-readable information about a variable
 * @link http://php.net/manual/en/function.print-r.php
 * @param expression mixed
 * @param return bool[optional]
 * @return mixed 
 */
function print_r ($expression, $return = null) {}

/**
 * Returns the amount of memory allocated to PHP
 * @link http://php.net/manual/en/function.memory-get-usage.php
 * @param real_usage bool[optional]
 * @return int the memory amount in bytes.
 */
function memory_get_usage ($real_usage = null) {}

/**
 * Returns the peak of memory allocated by PHP
 * @link http://php.net/manual/en/function.memory-get-peak-usage.php
 * @param real_usage bool[optional]
 * @return int the memory peak in bytes.
 */
function memory_get_peak_usage ($real_usage = null) {}

/**
 * Register a function for execution on shutdown
 * @link http://php.net/manual/en/function.register-shutdown-function.php
 * @param function callback
 * @param parameter mixed[optional]
 * @param ... mixed[optional]
 * @return void 
 */
function register_shutdown_function ($function, $parameter = null) {}

/**
 * Register a function for execution on each tick
 * @link http://php.net/manual/en/function.register-tick-function.php
 * @param function callback
 * @param arg mixed[optional]
 * @param ... mixed[optional]
 * @return bool 
 */
function register_tick_function ($function, $arg = null) {}

/**
 * De-register a function for execution on each tick
 * @link http://php.net/manual/en/function.unregister-tick-function.php
 * @param function_name string
 * @return void 
 */
function unregister_tick_function ($function_name) {}

/**
 * Syntax highlighting of a file
 * @link http://php.net/manual/en/function.highlight-file.php
 * @param filename string
 * @param return bool[optional]
 * @return mixed 
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
 * @param str string
 * @param return bool[optional]
 * @return mixed 
 */
function highlight_string ($str, $return = null) {}

/**
 * Return source with stripped comments and whitespace
 * @link http://php.net/manual/en/function.php-strip-whitespace.php
 * @param filename string
 * @return string 
 */
function php_strip_whitespace ($filename) {}

/**
 * Gets the value of a configuration option
 * @link http://php.net/manual/en/function.ini-get.php
 * @param varname string
 * @return string the value of the configuration option as a string on success, or
 */
function ini_get ($varname) {}

/**
 * Gets all configuration options
 * @link http://php.net/manual/en/function.ini-get-all.php
 * @param extension string[optional]
 * @return array an associative array uses the directive name as the array key,
 */
function ini_get_all ($extension = null) {}

/**
 * Sets the value of a configuration option
 * @link http://php.net/manual/en/function.ini-set.php
 * @param varname string
 * @param newvalue string
 * @return string the old value on success, false on failure.
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
 * @param varname string
 * @return void 
 */
function ini_restore ($varname) {}

/**
 * Gets the current include_path configuration option
 * @link http://php.net/manual/en/function.get-include-path.php
 * @return string the path, as a string.
 */
function get_include_path () {}

/**
 * Sets the include_path configuration option
 * @link http://php.net/manual/en/function.set-include-path.php
 * @param new_include_path string
 * @return string the old include_path on
 */
function set_include_path ($new_include_path) {}

/**
 * Restores the value of the include_path configuration option
 * @link http://php.net/manual/en/function.restore-include-path.php
 * @return void 
 */
function restore_include_path () {}

/**
 * Send a cookie
 * @link http://php.net/manual/en/function.setcookie.php
 * @param name string
 * @param value string[optional]
 * @param expire int[optional]
 * @param path string[optional]
 * @param domain string[optional]
 * @param secure bool[optional]
 * @param httponly bool[optional]
 * @return bool 
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
 * @return bool 
 */
function setrawcookie ($name, $value = null, $expire = null, $path = null, $domain = null, $secure = null, $httponly = null) {}

/**
 * Send a raw HTTP header
 * @link http://php.net/manual/en/function.header.php
 * @param string string
 * @param replace bool[optional]
 * @param http_response_code int[optional]
 * @return void 
 */
function header ($string, $replace = null, $http_response_code = null) {}

/**
 * Checks if or where headers have been sent
 * @link http://php.net/manual/en/function.headers-sent.php
 * @param file string[optional]
 * @param line int[optional]
 * @return bool 
 */
function headers_sent (&$file = null, &$line = null) {}

/**
 * Returns a list of response headers sent (or ready to send)
 * @link http://php.net/manual/en/function.headers-list.php
 * @return array a numerically indexed array of headers.
 */
function headers_list () {}

/**
 * Check whether client disconnected
 * @link http://php.net/manual/en/function.connection-aborted.php
 * @return int 1 if client disconnected, 0 otherwise.
 */
function connection_aborted () {}

/**
 * Returns connection status bitfield
 * @link http://php.net/manual/en/function.connection-status.php
 * @return int the connection status bitfield, which can be used against the
 */
function connection_status () {}

/**
 * Set whether a client disconnect should abort script execution
 * @link http://php.net/manual/en/function.ignore-user-abort.php
 * @param setting bool[optional]
 * @return int the previous setting, as a boolean.
 */
function ignore_user_abort ($setting = null) {}

/**
 * Parse a configuration file
 * @link http://php.net/manual/en/function.parse-ini-file.php
 * @param filename string
 * @param process_sections bool[optional]
 * @return array 
 */
function parse_ini_file ($filename, $process_sections = null) {}

/**
 * Tells whether the file was uploaded via HTTP POST
 * @link http://php.net/manual/en/function.is-uploaded-file.php
 * @param filename string
 * @return bool 
 */
function is_uploaded_file ($filename) {}

/**
 * Moves an uploaded file to a new location
 * @link http://php.net/manual/en/function.move-uploaded-file.php
 * @param filename string
 * @param destination string
 * @return bool 
 */
function move_uploaded_file ($filename, $destination) {}

/**
 * Get the Internet host name corresponding to a given IP address
 * @link http://php.net/manual/en/function.gethostbyaddr.php
 * @param ip_address string
 * @return string the host name or the unmodified ip_address
 */
function gethostbyaddr ($ip_address) {}

/**
 * Get the IP address corresponding to a given Internet host name
 * @link http://php.net/manual/en/function.gethostbyname.php
 * @param hostname string
 * @return string the IP address or a string containing the unmodified
 */
function gethostbyname ($hostname) {}

/**
 * Get a list of IP addresses corresponding to a given Internet host
   name
 * @link http://php.net/manual/en/function.gethostbynamel.php
 * @param hostname string
 * @return array an array of IP addresses or false if
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
 * @param host string
 * @param type string[optional]
 * @return int true if any records are found; returns false if no records
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
 * @param hostname string
 * @param mxhosts array
 * @param weight array[optional]
 * @return bool true if any records are found; returns false if no records
 */
function getmxrr ($hostname, array &$mxhosts, array &$weight = null) {}

/**
 * Fetch DNS Resource Records associated with a hostname
 * @link http://php.net/manual/en/function.dns-get-record.php
 * @param hostname string
 * @param type int[optional]
 * @param authns array[optional]
 * @param addtl array
 * @return array 
 */
function dns_get_record ($hostname, $type = null, array &$authns = null, array &$addtl) {}

/**
 * Get the integer value of a variable
 * @link http://php.net/manual/en/function.intval.php
 * @param var mixed
 * @param base int[optional]
 * @return int 
 */
function intval ($var, $base = null) {}

/**
 * Get float value of a variable
 * @link http://php.net/manual/en/function.floatval.php
 * @param var mixed
 * @return float 
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
 * @param var mixed
 * @return string 
 */
function strval ($var) {}

/**
 * Get the type of a variable
 * @link http://php.net/manual/en/function.gettype.php
 * @param var mixed
 * @return string 
 */
function gettype ($var) {}

/**
 * Set the type of a variable
 * @link http://php.net/manual/en/function.settype.php
 * @param var mixed
 * @param type string
 * @return bool 
 */
function settype (&$var, $type) {}

/**
 * Finds whether a variable is &null;
 * @link http://php.net/manual/en/function.is-null.php
 * @param var mixed
 * @return bool true if var is null, false
 */
function is_null ($var) {}

/**
 * Finds whether a variable is a resource
 * @link http://php.net/manual/en/function.is-resource.php
 * @param var mixed
 * @return bool true if var is a resource,
 */
function is_resource ($var) {}

/**
 * Finds out whether a variable is a boolean
 * @link http://php.net/manual/en/function.is-bool.php
 * @param var mixed
 * @return bool true if var is a boolean,
 */
function is_bool ($var) {}

/**
 * &Alias; <function>is_int</function>
 * @link http://php.net/manual/en/function.is-long.php
 * @param var
 */
function is_long ($var) {}

/**
 * Finds whether a variable is a float
 * @link http://php.net/manual/en/function.is-float.php
 * @param var mixed
 * @return bool true if var is a float,
 */
function is_float ($var) {}

/**
 * Find whether the type of a variable is integer
 * @link http://php.net/manual/en/function.is-int.php
 * @param var mixed
 * @return bool true if var is an integer,
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
 * @param var mixed
 * @return bool true if var is a number or a numeric
 */
function is_numeric ($var) {}

/**
 * Finds whether a variable is a string
 * @link http://php.net/manual/en/function.is-string.php
 * @param var mixed
 * @return bool true if var is a string,
 */
function is_string ($var) {}

/**
 * Finds whether a variable is an array
 * @link http://php.net/manual/en/function.is-array.php
 * @param var mixed
 * @return bool true if var is an array,
 */
function is_array ($var) {}

/**
 * Finds whether a variable is an object
 * @link http://php.net/manual/en/function.is-object.php
 * @param var mixed
 * @return bool true if var is an object,
 */
function is_object ($var) {}

/**
 * Finds whether a variable is a scalar
 * @link http://php.net/manual/en/function.is-scalar.php
 * @param var mixed
 * @return bool true if var is a scalar false
 */
function is_scalar ($var) {}

/**
 * Verify that the contents of a variable can be called as a function
 * @link http://php.net/manual/en/function.is-callable.php
 * @param var mixed
 * @param syntax_only bool[optional]
 * @param callable_name string[optional]
 * @return bool true if var is callable, false
 */
function is_callable ($var, $syntax_only = null, &$callable_name = null) {}

/**
 * Regular expression match
 * @link http://php.net/manual/en/function.ereg.php
 * @param pattern string
 * @param string string
 * @param regs array[optional]
 * @return int the length of the matched string if a match for
 */
function ereg ($pattern, $string, array &$regs = null) {}

/**
 * Replace regular expression
 * @link http://php.net/manual/en/function.ereg-replace.php
 * @param pattern string
 * @param replacement string
 * @param string string
 * @return string 
 */
function ereg_replace ($pattern, $replacement, $string) {}

/**
 * Case insensitive regular expression match
 * @link http://php.net/manual/en/function.eregi.php
 * @param pattern string
 * @param string string
 * @param regs array[optional]
 * @return int the length of the matched string if a match for
 */
function eregi ($pattern, $string, array &$regs = null) {}

/**
 * Replace regular expression case insensitive
 * @link http://php.net/manual/en/function.eregi-replace.php
 * @param pattern string
 * @param replacement string
 * @param string string
 * @return string 
 */
function eregi_replace ($pattern, $replacement, $string) {}

/**
 * Split string into array by regular expression
 * @link http://php.net/manual/en/function.split.php
 * @param pattern string
 * @param string string
 * @param limit int[optional]
 * @return array an array of strings, each of which is a substring of
 */
function split ($pattern, $string, $limit = null) {}

/**
 * Split string into array by regular expression case insensitive
 * @link http://php.net/manual/en/function.spliti.php
 * @param pattern string
 * @param string string
 * @param limit int[optional]
 * @return array an array of strings, each of which is a substring of
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
 * @param string string
 * @return string a valid regular expression which will match
 */
function sql_regcase ($string) {}

/**
 * Loads a PHP extension at runtime
 * @link http://php.net/manual/en/function.dl.php
 * @param library string
 * @return int 
 */
function dl ($library) {}

/**
 * Closes process file pointer
 * @link http://php.net/manual/en/function.pclose.php
 * @param handle resource
 * @return int the termination status of the process that was run.
 */
function pclose ($handle) {}

/**
 * Opens process file pointer
 * @link http://php.net/manual/en/function.popen.php
 * @param command string
 * @param mode string
 * @return resource a file pointer identical to that returned by
 */
function popen ($command, $mode) {}

/**
 * Outputs a file
 * @link http://php.net/manual/en/function.readfile.php
 * @param filename string
 * @param use_include_path bool[optional]
 * @param context resource[optional]
 * @return int the number of bytes read from the file. If an error
 */
function readfile ($filename, $use_include_path = null, $context = null) {}

/**
 * Rewind the position of a file pointer
 * @link http://php.net/manual/en/function.rewind.php
 * @param handle resource
 * @return bool 
 */
function rewind ($handle) {}

/**
 * Removes directory
 * @link http://php.net/manual/en/function.rmdir.php
 * @param dirname string
 * @param context resource[optional]
 * @return bool 
 */
function rmdir ($dirname, $context = null) {}

/**
 * Changes the current umask
 * @link http://php.net/manual/en/function.umask.php
 * @param mask int[optional]
 * @return int 
 */
function umask ($mask = null) {}

/**
 * Closes an open file pointer
 * @link http://php.net/manual/en/function.fclose.php
 * @param handle resource
 * @return bool 
 */
function fclose ($handle) {}

/**
 * Tests for end-of-file on a file pointer
 * @link http://php.net/manual/en/function.feof.php
 * @param handle resource
 * @return bool true if the file pointer is at EOF or an error occurs
 */
function feof ($handle) {}

/**
 * Gets character from file pointer
 * @link http://php.net/manual/en/function.fgetc.php
 * @param handle resource
 * @return string a string containing a single character read from the file pointed
 */
function fgetc ($handle) {}

/**
 * Gets line from file pointer
 * @link http://php.net/manual/en/function.fgets.php
 * @param handle resource
 * @param length int[optional]
 * @return string a string of up to length - 1 bytes read from
 */
function fgets ($handle, $length = null) {}

/**
 * Gets line from file pointer and strip HTML tags
 * @link http://php.net/manual/en/function.fgetss.php
 * @param handle resource
 * @param length int[optional]
 * @param allowable_tags string[optional]
 * @return string a string of up to length - 1 bytes read from
 */
function fgetss ($handle, $length = null, $allowable_tags = null) {}

/**
 * Binary-safe file read
 * @link http://php.net/manual/en/function.fread.php
 * @param handle resource
 * @param length int
 * @return string the read string or false in case of error.
 */
function fread ($handle, $length) {}

/**
 * Opens file or URL
 * @link http://php.net/manual/en/function.fopen.php
 * @param filename string
 * @param mode string
 * @param use_include_path bool[optional]
 * @param context resource[optional]
 * @return resource a file pointer resource on success, or false on error.
 */
function fopen ($filename, $mode, $use_include_path = null, $context = null) {}

/**
 * Output all remaining data on a file pointer
 * @link http://php.net/manual/en/function.fpassthru.php
 * @param handle resource
 * @return int 
 */
function fpassthru ($handle) {}

/**
 * Truncates a file to a given length
 * @link http://php.net/manual/en/function.ftruncate.php
 * @param handle resource
 * @param size int
 * @return bool 
 */
function ftruncate ($handle, $size) {}

/**
 * Gets information about a file using an open file pointer
 * @link http://php.net/manual/en/function.fstat.php
 * @param handle resource
 * @return array an array with the statistics of the file; the format of the array
 */
function fstat ($handle) {}

/**
 * Seeks on a file pointer
 * @link http://php.net/manual/en/function.fseek.php
 * @param handle resource
 * @param offset int
 * @param whence int[optional]
 * @return int 
 */
function fseek ($handle, $offset, $whence = null) {}

/**
 * Tells file pointer read/write position
 * @link http://php.net/manual/en/function.ftell.php
 * @param handle resource
 * @return int the position of the file pointer referenced by
 */
function ftell ($handle) {}

/**
 * Flushes the output to a file
 * @link http://php.net/manual/en/function.fflush.php
 * @param handle resource
 * @return bool 
 */
function fflush ($handle) {}

/**
 * Binary-safe file write
 * @link http://php.net/manual/en/function.fwrite.php
 * @param handle resource
 * @param string string
 * @param length int[optional]
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
 * @param pathname string
 * @param mode int[optional]
 * @param recursive bool[optional]
 * @param context resource[optional]
 * @return bool 
 */
function mkdir ($pathname, $mode = null, $recursive = null, $context = null) {}

/**
 * Renames a file or directory
 * @link http://php.net/manual/en/function.rename.php
 * @param oldname string
 * @param newname string
 * @param context resource[optional]
 * @return bool 
 */
function rename ($oldname, $newname, $context = null) {}

/**
 * Copies file
 * @link http://php.net/manual/en/function.copy.php
 * @param source string
 * @param dest string
 * @return bool 
 */
function copy ($source, $dest) {}

/**
 * Create file with unique file name
 * @link http://php.net/manual/en/function.tempnam.php
 * @param dir string
 * @param prefix string
 * @return string the new temporary filename, or false on
 */
function tempnam ($dir, $prefix) {}

/**
 * Creates a temporary file
 * @link http://php.net/manual/en/function.tmpfile.php
 * @return resource a file handle, similar to the one returned by
 */
function tmpfile () {}

/**
 * Reads entire file into an array
 * @link http://php.net/manual/en/function.file.php
 * @param filename string
 * @param flags int[optional]
 * @param context resource[optional]
 * @return array the file in an array. Each element of the array corresponds to a
 */
function file ($filename, $flags = null, $context = null) {}

/**
 * Reads entire file into a string
 * @link http://php.net/manual/en/function.file-get-contents.php
 * @param filename string
 * @param flags int[optional]
 * @param context resource[optional]
 * @param offset int[optional]
 * @param maxlen int[optional]
 * @return string 
 */
function file_get_contents ($filename, $flags = null, $context = null, $offset = null, $maxlen = null) {}

/**
 * Write a string to a file
 * @link http://php.net/manual/en/function.file-put-contents.php
 * @param filename string
 * @param data mixed
 * @param flags int[optional]
 * @param context resource[optional]
 * @return int 
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
 * @param options[optional]
 */
function stream_context_create ($options) {}

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
 * @param stream resource
 * @param how int
 * @return bool 
 */
function stream_socket_shutdown ($stream, $how) {}

/**
 * Creates a pair of connected, indistinguishable socket streams
 * @link http://php.net/manual/en/function.stream-socket-pair.php
 * @param domain int
 * @param type int
 * @param protocol int
 * @return array an array with the two socket resources on success, or
 */
function stream_socket_pair ($domain, $type, $protocol) {}

/**
 * Copies data from one stream to another
 * @link http://php.net/manual/en/function.stream-copy-to-stream.php
 * @param source resource
 * @param dest resource
 * @param maxlength int[optional]
 * @param offset int[optional]
 * @return int the total count of bytes copied.
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
 * @param handle resource
 * @param length int[optional]
 * @param delimiter string[optional]
 * @param enclosure string[optional]
 * @return array an indexed array containing the fields read.
 */
function fgetcsv ($handle, $length = null, $delimiter = null, $enclosure = null) {}

/**
 * Format line as CSV and write to file pointer
 * @link http://php.net/manual/en/function.fputcsv.php
 * @param handle resource
 * @param fields array
 * @param delimiter string[optional]
 * @param enclosure string[optional]
 * @return int the length of the written string, or false on failure.
 */
function fputcsv ($handle, array $fields, $delimiter = null, $enclosure = null) {}

/**
 * Portable advisory file locking
 * @link http://php.net/manual/en/function.flock.php
 * @param handle resource
 * @param operation int
 * @param wouldblock int[optional]
 * @return bool 
 */
function flock ($handle, $operation, &$wouldblock = null) {}

/**
 * Extracts all meta tag content attributes from a file and returns an array
 * @link http://php.net/manual/en/function.get-meta-tags.php
 * @param filename string
 * @param use_include_path bool[optional]
 * @return array an array with all the parsed meta tags.
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
 * Alias of <function>stream_wrapper_register</function>
 * @link http://php.net/manual/en/function.stream-register-wrapper.php
 * @param protocol
 * @param classname
 * @param flags[optional]
 */
function stream_register_wrapper ($protocol, $classname, $flags) {}

/**
 * Unregister a URL wrapper
 * @link http://php.net/manual/en/function.stream-wrapper-unregister.php
 * @param protocol
 */
function stream_wrapper_unregister ($protocol) {}

/**
 * Restores a previously unregistered built-in wrapper
 * @link http://php.net/manual/en/function.stream-wrapper-restore.php
 * @param protocol
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
 * @param url string
 * @param format int[optional]
 * @return array an indexed or associative array with the headers, or false on
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
 * @param path string
 * @return string the canonicalized absolute pathname on success. The resulting path
 */
function realpath ($path) {}

/**
 * Match filename against a pattern
 * @link http://php.net/manual/en/function.fnmatch.php
 * @param pattern string
 * @param string string
 * @param flags int[optional]
 * @return bool true if there is a match, false otherwise.
 */
function fnmatch ($pattern, $string, $flags = null) {}

/**
 * Open Internet or Unix domain socket connection
 * @link http://php.net/manual/en/function.fsockopen.php
 * @param hostname string
 * @param port int[optional]
 * @param errno int[optional]
 * @param errstr string[optional]
 * @param timeout float[optional]
 * @return resource 
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
 * @param format string
 * @param args mixed[optional]
 * @param ... mixed[optional]
 * @return string a binary string containing data.
 */
function pack ($format, $args = null) {}

/**
 * Unpack data from binary string
 * @link http://php.net/manual/en/function.unpack.php
 * @param format string
 * @param data string
 * @return array an associative array containing unpacked elements of binary
 */
function unpack ($format, $data) {}

/**
 * Tells what the user's browser is capable of
 * @link http://php.net/manual/en/function.get-browser.php
 * @param user_agent string[optional]
 * @param return_array bool[optional]
 * @return mixed 
 */
function get_browser ($user_agent = null, $return_array = null) {}

/**
 * One-way string encryption (hashing)
 * @link http://php.net/manual/en/function.crypt.php
 * @param str string
 * @param salt string[optional]
 * @return string the encrypted string.
 */
function crypt ($str, $salt = null) {}

/**
 * Open directory handle
 * @link http://php.net/manual/en/function.opendir.php
 * @param path string
 * @param context resource[optional]
 * @return resource a directory handle resource on success, or
 */
function opendir ($path, $context = null) {}

/**
 * Close directory handle
 * @link http://php.net/manual/en/function.closedir.php
 * @param dir_handle resource
 * @return void 
 */
function closedir ($dir_handle) {}

/**
 * Change directory
 * @link http://php.net/manual/en/function.chdir.php
 * @param directory string
 * @return bool 
 */
function chdir ($directory) {}

/**
 * Change the root directory
 * @link http://php.net/manual/en/function.chroot.php
 * @param directory string
 * @return bool 
 */
function chroot ($directory) {}

/**
 * Gets the current working directory
 * @link http://php.net/manual/en/function.getcwd.php
 * @return string the current working directory on success, or false on
 */
function getcwd () {}

/**
 * Rewind directory handle
 * @link http://php.net/manual/en/function.rewinddir.php
 * @param dir_handle resource
 * @return void 
 */
function rewinddir ($dir_handle) {}

/**
 * Read entry from directory handle
 * @link http://php.net/manual/en/function.readdir.php
 * @param dir_handle resource
 * @return string the filename on success, or false on failure.
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
 * @param directory string
 * @param sorting_order int[optional]
 * @param context resource[optional]
 * @return array an array of filenames on success, or false on
 */
function scandir ($directory, $sorting_order = null, $context = null) {}

/**
 * Find pathnames matching a pattern
 * @link http://php.net/manual/en/function.glob.php
 * @param pattern string
 * @param flags int[optional]
 * @return array an array containing the matched files/directories, an empty array
 */
function glob ($pattern, $flags = null) {}

/**
 * Gets last access time of file
 * @link http://php.net/manual/en/function.fileatime.php
 * @param filename string
 * @return int the time the file was last accessed, or false in case of
 */
function fileatime ($filename) {}

/**
 * Gets inode change time of file
 * @link http://php.net/manual/en/function.filectime.php
 * @param filename string
 * @return int the time the file was last changed, or false in case of
 */
function filectime ($filename) {}

/**
 * Gets file group
 * @link http://php.net/manual/en/function.filegroup.php
 * @param filename string
 * @return int the group ID of the file, or false in case
 */
function filegroup ($filename) {}

/**
 * Gets file inode
 * @link http://php.net/manual/en/function.fileinode.php
 * @param filename string
 * @return int the inode number of the file, or false in case of an error.
 */
function fileinode ($filename) {}

/**
 * Gets file modification time
 * @link http://php.net/manual/en/function.filemtime.php
 * @param filename string
 * @return int the time the file was last modified, or false in case of
 */
function filemtime ($filename) {}

/**
 * Gets file owner
 * @link http://php.net/manual/en/function.fileowner.php
 * @param filename string
 * @return int the user ID of the owner of the file, or false in case of
 */
function fileowner ($filename) {}

/**
 * Gets file permissions
 * @link http://php.net/manual/en/function.fileperms.php
 * @param filename string
 * @return int the permissions on the file, or false in case of an error.
 */
function fileperms ($filename) {}

/**
 * Gets file size
 * @link http://php.net/manual/en/function.filesize.php
 * @param filename string
 * @return int the size of the file in bytes, or false (and generates an error
 */
function filesize ($filename) {}

/**
 * Gets file type
 * @link http://php.net/manual/en/function.filetype.php
 * @param filename string
 * @return string the type of the file. Possible values are fifo, char,
 */
function filetype ($filename) {}

/**
 * Checks whether a file or directory exists
 * @link http://php.net/manual/en/function.file-exists.php
 * @param filename string
 * @return bool true if the file or directory specified by
 */
function file_exists ($filename) {}

/**
 * Tells whether the filename is writable
 * @link http://php.net/manual/en/function.is-writable.php
 * @param filename string
 * @return bool true if the filename exists and is
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
 * @param filename string
 * @return bool true if the file or directory specified by
 */
function is_readable ($filename) {}

/**
 * Tells whether the filename is executable
 * @link http://php.net/manual/en/function.is-executable.php
 * @param filename string
 * @return bool true if the filename exists and is executable, or false on
 */
function is_executable ($filename) {}

/**
 * Tells whether the filename is a regular file
 * @link http://php.net/manual/en/function.is-file.php
 * @param filename string
 * @return bool true if the filename exists and is a regular file, false
 */
function is_file ($filename) {}

/**
 * Tells whether the filename is a directory
 * @link http://php.net/manual/en/function.is-dir.php
 * @param filename string
 * @return bool true if the filename exists and is a directory, false
 */
function is_dir ($filename) {}

/**
 * Tells whether the filename is a symbolic link
 * @link http://php.net/manual/en/function.is-link.php
 * @param filename string
 * @return bool true if the filename exists and is a symbolic link, false
 */
function is_link ($filename) {}

/**
 * Gives information about a file
 * @link http://php.net/manual/en/function.stat.php
 * @param filename string
 * @return array 
 */
function stat ($filename) {}

/**
 * Gives information about a file or symbolic link
 * @link http://php.net/manual/en/function.lstat.php
 * @param filename string
 * @return array 
 */
function lstat ($filename) {}

/**
 * Changes file owner
 * @link http://php.net/manual/en/function.chown.php
 * @param filename string
 * @param user mixed
 * @return bool 
 */
function chown ($filename, $user) {}

/**
 * Changes file group
 * @link http://php.net/manual/en/function.chgrp.php
 * @param filename string
 * @param group mixed
 * @return bool 
 */
function chgrp ($filename, $group) {}

/**
 * Changes user ownership of symlink
 * @link http://php.net/manual/en/function.lchown.php
 * @param filename string
 * @param user mixed
 * @return bool 
 */
function lchown ($filename, $user) {}

/**
 * Changes group ownership of symlink
 * @link http://php.net/manual/en/function.lchgrp.php
 * @param filename string
 * @param group mixed
 * @return bool 
 */
function lchgrp ($filename, $group) {}

/**
 * Changes file mode
 * @link http://php.net/manual/en/function.chmod.php
 * @param filename string
 * @param mode int
 * @return bool 
 */
function chmod ($filename, $mode) {}

/**
 * Sets access and modification time of file
 * @link http://php.net/manual/en/function.touch.php
 * @param filename string
 * @param time int[optional]
 * @param atime int[optional]
 * @return bool 
 */
function touch ($filename, $time = null, $atime = null) {}

/**
 * Clears file status cache
 * @link http://php.net/manual/en/function.clearstatcache.php
 * @return void 
 */
function clearstatcache () {}

/**
 * Returns the total size of a directory
 * @link http://php.net/manual/en/function.disk-total-space.php
 * @param directory string
 * @return float the total number of bytes as a float.
 */
function disk_total_space ($directory) {}

/**
 * Returns available space in directory
 * @link http://php.net/manual/en/function.disk-free-space.php
 * @param directory string
 * @return float the number of available bytes as a float.
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
 * @param to string
 * @param subject string
 * @param message string
 * @param additional_headers string[optional]
 * @param additional_parameters string[optional]
 * @return bool true if the mail was successfully accepted for delivery, false otherwise.
 */
function mail ($to, $subject, $message, $additional_headers = null, $additional_parameters = null) {}

/**
 * Calculate the hash value needed by EZMLM
 * @link http://php.net/manual/en/function.ezmlm-hash.php
 * @param addr string
 * @return int 
 */
function ezmlm_hash ($addr) {}

/**
 * Open connection to system logger
 * @link http://php.net/manual/en/function.openlog.php
 * @param ident string
 * @param option int
 * @param facility int
 * @return bool 
 */
function openlog ($ident, $option, $facility) {}

/**
 * Generate a system log message
 * @link http://php.net/manual/en/function.syslog.php
 * @param priority int
 * @param message string
 * @return bool 
 */
function syslog ($priority, $message) {}

/**
 * Close connection to system logger
 * @link http://php.net/manual/en/function.closelog.php
 * @return bool 
 */
function closelog () {}

/**
 * Initializes all syslog related constants
 * @link http://php.net/manual/en/function.define-syslog-variables.php
 * @return void 
 */
function define_syslog_variables () {}

/**
 * Combined linear congruential generator
 * @link http://php.net/manual/en/function.lcg-value.php
 * @return float 
 */
function lcg_value () {}

/**
 * Calculate the metaphone key of a string
 * @link http://php.net/manual/en/function.metaphone.php
 * @param str string
 * @param phones int[optional]
 * @return string the metaphone key as a string.
 */
function metaphone ($str, $phones = null) {}

/**
 * Turn on output buffering
 * @link http://php.net/manual/en/function.ob-start.php
 * @param output_callback callback[optional]
 * @param chunk_size int[optional]
 * @param erase bool[optional]
 * @return bool 
 */
function ob_start ($output_callback = null, $chunk_size = null, $erase = null) {}

/**
 * Flush (send) the output buffer
 * @link http://php.net/manual/en/function.ob-flush.php
 * @return void 
 */
function ob_flush () {}

/**
 * Clean (erase) the output buffer
 * @link http://php.net/manual/en/function.ob-clean.php
 * @return void 
 */
function ob_clean () {}

/**
 * Flush (send) the output buffer and turn off output buffering
 * @link http://php.net/manual/en/function.ob-end-flush.php
 * @return bool 
 */
function ob_end_flush () {}

/**
 * Clean (erase) the output buffer and turn off output buffering
 * @link http://php.net/manual/en/function.ob-end-clean.php
 * @return bool 
 */
function ob_end_clean () {}

/**
 * Flush the output buffer, return it as a string and turn off output buffering
 * @link http://php.net/manual/en/function.ob-get-flush.php
 * @return string the output buffer or false if no buffering is active.
 */
function ob_get_flush () {}

/**
 * Get current buffer contents and delete current output buffer
 * @link http://php.net/manual/en/function.ob-get-clean.php
 * @return string the contents of the output buffer and end output buffering.
 */
function ob_get_clean () {}

/**
 * Return the length of the output buffer
 * @link http://php.net/manual/en/function.ob-get-length.php
 * @return int the length of the output buffer contents or false if no
 */
function ob_get_length () {}

/**
 * Return the nesting level of the output buffering mechanism
 * @link http://php.net/manual/en/function.ob-get-level.php
 * @return int the level of nested output buffering handlers or zero if output
 */
function ob_get_level () {}

/**
 * Get status of output buffers
 * @link http://php.net/manual/en/function.ob-get-status.php
 * @param full_status bool[optional]
 * @return array 
 */
function ob_get_status ($full_status = null) {}

/**
 * Return the contents of the output buffer
 * @link http://php.net/manual/en/function.ob-get-contents.php
 * @return string 
 */
function ob_get_contents () {}

/**
 * Turn implicit flush on/off
 * @link http://php.net/manual/en/function.ob-implicit-flush.php
 * @param flag int[optional]
 * @return void 
 */
function ob_implicit_flush ($flag = null) {}

/**
 * List all output handlers in use
 * @link http://php.net/manual/en/function.ob-list-handlers.php
 * @return array 
 */
function ob_list_handlers () {}

/**
 * Sort an array by key
 * @link http://php.net/manual/en/function.ksort.php
 * @param arg
 * @param sort_flags[optional]
 */
function ksort (&$arg, $sort_flags) {}

/**
 * Sort an array by key in reverse order
 * @link http://php.net/manual/en/function.krsort.php
 * @param arg
 * @param sort_flags[optional]
 */
function krsort (&$arg, $sort_flags) {}

/**
 * Sort an array using a "natural order" algorithm
 * @link http://php.net/manual/en/function.natsort.php
 * @param arg
 */
function natsort (&$arg) {}

/**
 * Sort an array using a case insensitive "natural order" algorithm
 * @link http://php.net/manual/en/function.natcasesort.php
 * @param arg
 */
function natcasesort (&$arg) {}

/**
 * Sort an array and maintain index association
 * @link http://php.net/manual/en/function.asort.php
 * @param arg
 * @param sort_flags[optional]
 */
function asort (&$arg, $sort_flags) {}

/**
 * Sort an array in reverse order and maintain index association
 * @link http://php.net/manual/en/function.arsort.php
 * @param arg
 * @param sort_flags[optional]
 */
function arsort (&$arg, $sort_flags) {}

/**
 * Sort an array
 * @link http://php.net/manual/en/function.sort.php
 * @param arg
 * @param sort_flags[optional]
 */
function sort (&$arg, $sort_flags) {}

/**
 * Sort an array in reverse order
 * @link http://php.net/manual/en/function.rsort.php
 * @param arg
 * @param sort_flags[optional]
 */
function rsort (&$arg, $sort_flags) {}

/**
 * Sort an array by values using a user-defined comparison function
 * @link http://php.net/manual/en/function.usort.php
 * @param arg
 * @param cmp_function
 */
function usort (&$arg, $cmp_function) {}

/**
 * Sort an array with a user-defined comparison function and maintain index association
 * @link http://php.net/manual/en/function.uasort.php
 * @param arg
 * @param cmp_function
 */
function uasort (&$arg, $cmp_function) {}

/**
 * Sort an array by keys using a user-defined comparison function
 * @link http://php.net/manual/en/function.uksort.php
 * @param arg
 * @param cmp_function
 */
function uksort (&$arg, $cmp_function) {}

/**
 * Shuffle an array
 * @link http://php.net/manual/en/function.shuffle.php
 * @param arg
 */
function shuffle (&$arg) {}

/**
 * Apply a user function to every member of an array
 * @link http://php.net/manual/en/function.array-walk.php
 * @param input
 * @param funcname
 * @param userdata[optional]
 */
function array_walk (&$input, $funcname, $userdata) {}

/**
 * Apply a user function recursively to every member of an array
 * @link http://php.net/manual/en/function.array-walk-recursive.php
 * @param input
 * @param funcname
 * @param userdata[optional]
 */
function array_walk_recursive (&$input, $funcname, $userdata) {}

/**
 * Count elements in an array, or properties in an object
 * @link http://php.net/manual/en/function.count.php
 * @param var
 * @param mode[optional]
 */
function count ($var, $mode) {}

/**
 * Set the internal pointer of an array to its last element
 * @link http://php.net/manual/en/function.end.php
 * @param arg
 */
function end (&$arg) {}

/**
 * Rewind the internal array pointer
 * @link http://php.net/manual/en/function.prev.php
 * @param arg
 */
function prev (&$arg) {}

/**
 * Advance the internal array pointer of an array
 * @link http://php.net/manual/en/function.next.php
 * @param arg
 */
function next (&$arg) {}

/**
 * Set the internal pointer of an array to its first element
 * @link http://php.net/manual/en/function.reset.php
 * @param arg
 */
function reset (&$arg) {}

/**
 * Return the current element in an array
 * @link http://php.net/manual/en/function.current.php
 * @param arg
 */
function current (&$arg) {}

/**
 * Fetch a key from an associative array
 * @link http://php.net/manual/en/function.key.php
 * @param arg
 */
function key (&$arg) {}

/**
 * Find lowest value
 * @link http://php.net/manual/en/function.min.php
 * @param values array
 * @return mixed 
 */
function min (array $values) {}

/**
 * Find highest value
 * @link http://php.net/manual/en/function.max.php
 * @param values array
 * @return mixed 
 */
function max (array $values) {}

/**
 * Checks if a value exists in an array
 * @link http://php.net/manual/en/function.in-array.php
 * @param needle
 * @param haystack
 * @param strict[optional]
 */
function in_array ($needle, $haystack, $strict) {}

/**
 * Searches the array for a given value and returns the corresponding key if successful
 * @link http://php.net/manual/en/function.array-search.php
 * @param needle
 * @param haystack
 * @param strict[optional]
 */
function array_search ($needle, $haystack, $strict) {}

/**
 * Import variables into the current symbol table from an array
 * @link http://php.net/manual/en/function.extract.php
 * @param arg
 * @param extract_type[optional]
 * @param prefix[optional]
 */
function extract ($arg, $extract_type, $prefix) {}

/**
 * Create array containing variables and their values
 * @link http://php.net/manual/en/function.compact.php
 * @param var_names
 * @param ...[optional]
 */
function compact ($var_names) {}

/**
 * Fill an array with values
 * @link http://php.net/manual/en/function.array-fill.php
 * @param start_index int
 * @param num int
 * @param value mixed
 * @return array the filled array
 */
function array_fill ($start_index, $num, $value) {}

/**
 * Fill an array with values, specifying keys
 * @link http://php.net/manual/en/function.array-fill-keys.php
 * @param keys array
 * @param value mixed
 * @return array the filled array
 */
function array_fill_keys (array $keys, $value) {}

/**
 * Create an array containing a range of elements
 * @link http://php.net/manual/en/function.range.php
 * @param low
 * @param high
 * @param step[optional]
 */
function range ($low, $high, $step) {}

/**
 * Sort multiple or multi-dimensional arrays
 * @link http://php.net/manual/en/function.array-multisort.php
 * @param arr1
 * @param SORT_ASC_or_SORT_DESC[optional]
 * @param SORT_REGULAR_or_SORT_NUMERIC_or_SORT_STRING[optional]
 * @param arr2[optional]
 * @param SORT_ASC_or_SORT_DESC[optional]
 * @param SORT_REGULAR_or_SORT_NUMERIC_or_SORT_STRING[optional]
 */
function array_multisort (&$arr1, &$SORT_ASC_or_SORT_DESC, &$SORT_REGULAR_or_SORT_NUMERIC_or_SORT_STRING, &$arr2, &$SORT_ASC_or_SORT_DESC, &$SORT_REGULAR_or_SORT_NUMERIC_or_SORT_STRING) {}

/**
 * Push one or more elements onto the end of array
 * @link http://php.net/manual/en/function.array-push.php
 * @param stack
 * @param var
 * @param ...[optional]
 */
function array_push (&$stack, $var) {}

/**
 * Pop the element off the end of array
 * @link http://php.net/manual/en/function.array-pop.php
 * @param array array
 * @return mixed the last value of array.
 */
function array_pop (array &$array) {}

/**
 * Shift an element off the beginning of array
 * @link http://php.net/manual/en/function.array-shift.php
 * @param stack
 */
function array_shift (&$stack) {}

/**
 * Prepend one or more elements to the beginning of an array
 * @link http://php.net/manual/en/function.array-unshift.php
 * @param stack
 * @param var
 * @param ...[optional]
 */
function array_unshift (&$stack, $var) {}

/**
 * Remove a portion of the array and replace it with something else
 * @link http://php.net/manual/en/function.array-splice.php
 * @param arg
 * @param offset
 * @param length[optional]
 * @param replacement[optional]
 */
function array_splice (&$arg, $offset, $length, $replacement) {}

/**
 * Extract a slice of the array
 * @link http://php.net/manual/en/function.array-slice.php
 * @param arg
 * @param offset
 * @param length[optional]
 * @param preserve_keys[optional]
 */
function array_slice ($arg, $offset, $length, $preserve_keys) {}

/**
 * Merge one or more arrays
 * @link http://php.net/manual/en/function.array-merge.php
 * @param arr1
 * @param arr2
 * @param ...[optional]
 */
function array_merge ($arr1, $arr2) {}

/**
 * Merge two or more arrays recursively
 * @link http://php.net/manual/en/function.array-merge-recursive.php
 * @param array1 array
 * @param ... array[optional]
 * @return array 
 */
function array_merge_recursive (array $array1) {}

/**
 * Return all the keys of an array
 * @link http://php.net/manual/en/function.array-keys.php
 * @param input array
 * @param search_value mixed[optional]
 * @param strict bool[optional]
 * @return array an array of all the keys in input.
 */
function array_keys (array $input, $search_value = null, $strict = null) {}

/**
 * Return all the values of an array
 * @link http://php.net/manual/en/function.array-values.php
 * @param arg
 */
function array_values ($arg) {}

/**
 * Counts all the values of an array
 * @link http://php.net/manual/en/function.array-count-values.php
 * @param input array
 * @return array an assosiative array of values from input as
 */
function array_count_values (array $input) {}

/**
 * Return an array with elements in reverse order
 * @link http://php.net/manual/en/function.array-reverse.php
 * @param input
 * @param preserve_keys[optional]
 */
function array_reverse ($input, $preserve_keys) {}

/**
 * Iteratively reduce the array to a single value using a callback function
 * @link http://php.net/manual/en/function.array-reduce.php
 * @param arg
 * @param callback
 * @param initial[optional]
 */
function array_reduce ($arg, $callback, $initial) {}

/**
 * Pad array to the specified length with a value
 * @link http://php.net/manual/en/function.array-pad.php
 * @param input array
 * @param pad_size int
 * @param pad_value mixed
 * @return array a copy of the input padded to size specified
 */
function array_pad (array $input, $pad_size, $pad_value) {}

/**
 * Exchanges all keys with their associated values in an array
 * @link http://php.net/manual/en/function.array-flip.php
 * @param trans array
 * @return array the flipped array on success and false on failure.
 */
function array_flip (array $trans) {}

/**
 * Changes all keys in an array
 * @link http://php.net/manual/en/function.array-change-key-case.php
 * @param input array
 * @param case int[optional]
 * @return array an array with its keys lower or uppercased, or false if
 */
function array_change_key_case (array $input, $case = null) {}

/**
 * Pick one or more random entries out of an array
 * @link http://php.net/manual/en/function.array-rand.php
 * @param arg
 * @param num_req[optional]
 */
function array_rand ($arg, $num_req) {}

/**
 * Removes duplicate values from an array
 * @link http://php.net/manual/en/function.array-unique.php
 * @param arg
 */
function array_unique ($arg) {}

/**
 * Computes the intersection of arrays
 * @link http://php.net/manual/en/function.array-intersect.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array an array containing all of the values in
 */
function array_intersect (array $array1, array $array2) {}

/**
 * Computes the intersection of arrays using keys for comparison
 * @link http://php.net/manual/en/function.array-intersect-key.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array an associative array containing all the values of
 */
function array_intersect_key (array $array1, array $array2) {}

/**
 * Computes the intersection of arrays using a callback function on the keys for comparison
 * @link http://php.net/manual/en/function.array-intersect-ukey.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @param key_compare_func callback
 * @return array the values of array1 whose keys exist
 */
function array_intersect_ukey (array $array1, array $array2, $key_compare_func) {}

/**
 * Computes the intersection of arrays, compares data by a callback function
 * @link http://php.net/manual/en/function.array-uintersect.php
 * @param arr1
 * @param arr2
 * @param callback_data_compare_func
 */
function array_uintersect ($arr1, $arr2, $callback_data_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check
 * @link http://php.net/manual/en/function.array-intersect-assoc.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array an associative array containing all the values in
 */
function array_intersect_assoc (array $array1, array $array2) {}

/**
 * Computes the intersection of arrays with additional index check, compares data by a callback function
 * @link http://php.net/manual/en/function.array-uintersect-assoc.php
 * @param arr1
 * @param arr2
 * @param callback_data_compare_func
 */
function array_uintersect_assoc ($arr1, $arr2, $callback_data_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check, compares indexes by a callback function
 * @link http://php.net/manual/en/function.array-intersect-uassoc.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @param key_compare_func callback
 * @return array the values of array1 whose values exist
 */
function array_intersect_uassoc (array $array1, array $array2, $key_compare_func) {}

/**
 * Computes the intersection of arrays with additional index check, compares data and indexes by a callback functions
 * @link http://php.net/manual/en/function.array-uintersect-uassoc.php
 * @param arr1
 * @param arr2
 * @param callback_data_compare_func
 * @param callback_key_compare_func
 */
function array_uintersect_uassoc ($arr1, $arr2, $callback_data_compare_func, $callback_key_compare_func) {}

/**
 * Computes the difference of arrays
 * @link http://php.net/manual/en/function.array-diff.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array 
 */
function array_diff (array $array1, array $array2) {}

/**
 * Computes the difference of arrays using keys for comparison
 * @link http://php.net/manual/en/function.array-diff-key.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array an array containing all the entries from
 */
function array_diff_key (array $array1, array $array2) {}

/**
 * Computes the difference of arrays using a callback function on the keys for comparison
 * @link http://php.net/manual/en/function.array-diff-ukey.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @param key_compare_func callback
 * @return array an array containing all the entries from
 */
function array_diff_ukey (array $array1, array $array2, $key_compare_func) {}

/**
 * Computes the difference of arrays by using a callback function for data comparison
 * @link http://php.net/manual/en/function.array-udiff.php
 * @param arr1
 * @param arr2
 * @param callback_data_comp_func
 */
function array_udiff ($arr1, $arr2, $callback_data_comp_func) {}

/**
 * Computes the difference of arrays with additional index check
 * @link http://php.net/manual/en/function.array-diff-assoc.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @return array an array containing all the values from
 */
function array_diff_assoc (array $array1, array $array2) {}

/**
 * Computes the difference of arrays with additional index check, compares data by a callback function
 * @link http://php.net/manual/en/function.array-udiff-assoc.php
 * @param arr1
 * @param arr2
 * @param callback_key_comp_func
 */
function array_udiff_assoc ($arr1, $arr2, $callback_key_comp_func) {}

/**
 * Computes the difference of arrays with additional index check which is performed by a user supplied callback function
 * @link http://php.net/manual/en/function.array-diff-uassoc.php
 * @param array1 array
 * @param array2 array
 * @param ... array[optional]
 * @param key_compare_func callback
 * @return array an array containing all the entries from
 */
function array_diff_uassoc (array $array1, array $array2, $key_compare_func) {}

/**
 * Computes the difference of arrays with additional index check, compares data and indexes by a callback function
 * @link http://php.net/manual/en/function.array-udiff-uassoc.php
 * @param arr1
 * @param arr2
 * @param callback_data_comp_func
 * @param callback_key_comp_func
 */
function array_udiff_uassoc ($arr1, $arr2, $callback_data_comp_func, $callback_key_comp_func) {}

/**
 * Calculate the sum of values in an array
 * @link http://php.net/manual/en/function.array-sum.php
 * @param arg
 */
function array_sum ($arg) {}

/**
 * Calculate the product of values in an array
 * @link http://php.net/manual/en/function.array-product.php
 * @param arg
 */
function array_product ($arg) {}

/**
 * Filters elements of an array using a callback function
 * @link http://php.net/manual/en/function.array-filter.php
 * @param input array
 * @param callback callback[optional]
 * @return array the filtered array.
 */
function array_filter (array $input, $callback = null) {}

/**
 * Applies the callback to the elements of the given arrays
 * @link http://php.net/manual/en/function.array-map.php
 * @param callback callback
 * @param arr1 array
 * @param ... array[optional]
 * @return array an array containing all the elements of arr1
 */
function array_map ($callback, array $arr1) {}

/**
 * Split an array into chunks
 * @link http://php.net/manual/en/function.array-chunk.php
 * @param input array
 * @param size int
 * @param preserve_keys bool[optional]
 * @return array a multidimensional numerically indexed array, starting with zero,
 */
function array_chunk (array $input, $size, $preserve_keys = null) {}

/**
 * Creates an array by using one array for keys and another for its values
 * @link http://php.net/manual/en/function.array-combine.php
 * @param keys array
 * @param values array
 * @return array the combined array, false if the number of elements
 */
function array_combine (array $keys, array $values) {}

/**
 * Checks if the given key or index exists in the array
 * @link http://php.net/manual/en/function.array-key-exists.php
 * @param key mixed
 * @param search array
 * @return bool 
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
 * @param assertion mixed
 * @return bool 
 */
function assert ($assertion) {}

/**
 * Set/get the various assert flags
 * @link http://php.net/manual/en/function.assert-options.php
 * @param what int
 * @param value mixed[optional]
 * @return mixed the original setting of any option or false on errors.
 */
function assert_options ($what, $value = null) {}

/**
 * Compares two "PHP-standardized" version number strings
 * @link http://php.net/manual/en/function.version-compare.php
 * @param version1 string
 * @param version2 string
 * @param operator string[optional]
 * @return mixed 
 */
function version_compare ($version1, $version2, $operator = null) {}

/**
 * Convert a pathname and a project identifier to a System V IPC key
 * @link http://php.net/manual/en/function.ftok.php
 * @param pathname string
 * @param proj string
 * @return int 
 */
function ftok ($pathname, $proj) {}

/**
 * Perform the rot13 transform on a string
 * @link http://php.net/manual/en/function.str-rot13.php
 * @param str string
 * @return string the ROT13 version of the given string.
 */
function str_rot13 ($str) {}

/**
 * Retrieve list of registered filters
 * @link http://php.net/manual/en/function.stream-get-filters.php
 */
function stream_get_filters () {}

/**
 * Register a stream filter implemented as a PHP class 
     derived from <literal>php_user_filter</literal>
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
 * @param name string
 * @param value string
 * @return bool 
 */
function output_add_rewrite_var ($name, $value) {}

/**
 * Reset URL rewriter values
 * @link http://php.net/manual/en/function.output-reset-rewrite-vars.php
 * @return bool 
 */
function output_reset_rewrite_vars () {}

/**
 * Returns directory path used for temporary files
 * @link http://php.net/manual/en/function.sys-get-temp-dir.php
 * @return string the path of the temporary directory.
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
define ('M_E', 2.718281828459);
define ('M_LOG2E', 1.442695040889);
define ('M_LOG10E', 0.43429448190325);
define ('M_LN2', 0.69314718055995);
define ('M_LN10', 2.302585092994);
define ('M_PI', 3.1415926535898);
define ('M_PI_2', 1.5707963267949);
define ('M_PI_4', 0.78539816339745);
define ('M_1_PI', 0.31830988618379);
define ('M_2_PI', 0.63661977236758);
define ('M_SQRTPI', 1.7724538509055);
define ('M_2_SQRTPI', 1.1283791670955);
define ('M_LNPI', 1.1447298858494);
define ('M_EULER', 0.57721566490153);
define ('M_SQRT2', 1.4142135623731);
define ('M_SQRT1_2', 0.70710678118655);
define ('M_SQRT3', 1.7320508075689);
define ('INF', INF);
define ('NAN', NAN);
define ('INFO_GENERAL', 1);
define ('INFO_CREDITS', 2);
define ('INFO_CONFIGURATION', 4);
define ('INFO_MODULES', 8);
define ('INFO_ENVIRONMENT', 16);
define ('INFO_VARIABLES', 32);
define ('INFO_LICENSE', 64);
define ('INFO_ALL', 4294967295);
define ('CREDITS_GROUP', 1);
define ('CREDITS_GENERAL', 2);
define ('CREDITS_SAPI', 4);
define ('CREDITS_MODULES', 8);
define ('CREDITS_DOCS', 16);
define ('CREDITS_FULLPAGE', 32);
define ('CREDITS_QA', 64);
define ('CREDITS_ALL', 4294967295);
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
define ('CRYPT_SALT_LENGTH', 60);
define ('CRYPT_STD_DES', 1);
define ('CRYPT_EXT_DES', 0);
define ('CRYPT_MD5', 1);
define ('CRYPT_BLOWFISH', 1);
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
define ('IMAGETYPE_GIF', 1);
define ('IMAGETYPE_JPEG', 2);
define ('IMAGETYPE_PNG', 3);
define ('IMAGETYPE_SWF', 4);
define ('IMAGETYPE_PSD', 5);
define ('IMAGETYPE_BMP', 6);
define ('IMAGETYPE_TIFF_II', 7);
define ('IMAGETYPE_TIFF_MM', 8);
define ('IMAGETYPE_JPC', 9);
define ('IMAGETYPE_JP2', 10);
define ('IMAGETYPE_JPX', 11);
define ('IMAGETYPE_JB2', 12);
define ('IMAGETYPE_SWC', 13);
define ('IMAGETYPE_IFF', 14);
define ('IMAGETYPE_WBMP', 15);
define ('IMAGETYPE_JPEG2000', 9);
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

// End of standard v.5.2.4

// Start of Reflection v.0.1

/**
 * ReflectionException extends the standard Exception and is thrown by Reflection
 * API. No specific methods or properties are introduced.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionException extends Exception  {
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

class Reflection  {

	/**
	 * @param modifiers
	 */
	public static function getModifierNames ($modifiers) {}

	/**
	 * @param reflector Reflector
	 * @param return[optional]
	 */
	public static function export (Reflector $reflector, $return) {}

}

/**
 * Reflector is an interface implemented by all
 * exportable Reflection classes.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
interface Reflector  {

	abstract public static function export () {}

	abstract public function __toString () {}

}

abstract class ReflectionFunctionAbstract implements Reflector {
	abstract public $name;


	final private function __clone () {}

	abstract public function __toString () {}

	public function isInternal () {}

	public function isUserDefined () {}

	public function getName () {}

	public function getFileName () {}

	public function getStartLine () {}

	public function getEndLine () {}

	public function getDocComment () {}

	public function getStaticVariables () {}

	public function returnsReference () {}

	public function getParameters () {}

	public function getNumberOfParameters () {}

	public function getNumberOfRequiredParameters () {}

	public function getExtension () {}

	public function getExtensionName () {}

	public function isDeprecated () {}

}

/**
 * The ReflectionFunction class lets you
 * reverse-engineer functions.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionFunction extends ReflectionFunctionAbstract implements Reflector {
	const IS_DEPRECATED = 262144;

	public $name;


	/**
	 * @param name
	 */
	public function __construct ($name) {}

	public function __toString () {}

	/**
	 * @param name
	 * @param return[optional]
	 */
	public static function export ($name, $return) {}

	public function isDisabled () {}

	/**
	 * @param args
	 */
	public function invoke ($args) {}

	/**
	 * @param args
	 */
	public function invokeArgs (array $args) {}

	final private function __clone () {}

	public function isInternal () {}

	public function isUserDefined () {}

	public function getName () {}

	public function getFileName () {}

	public function getStartLine () {}

	public function getEndLine () {}

	public function getDocComment () {}

	public function getStaticVariables () {}

	public function returnsReference () {}

	public function getParameters () {}

	public function getNumberOfParameters () {}

	public function getNumberOfRequiredParameters () {}

	public function getExtension () {}

	public function getExtensionName () {}

	public function isDeprecated () {}

}

/**
 * The ReflectionParameter class retrieves
 * information about a function's or method's parameters.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionParameter implements Reflector {
	public $name;


	final private function __clone () {}

	/**
	 * @param function
	 * @param parameter
	 * @param return[optional]
	 */
	public static function export ($function, $parameter, $return) {}

	/**
	 * @param function
	 * @param parameter
	 */
	public function __construct ($function, $parameter) {}

	public function __toString () {}

	public function getName () {}

	public function isPassedByReference () {}

	public function getDeclaringFunction () {}

	public function getDeclaringClass () {}

	public function getClass () {}

	public function isArray () {}

	public function allowsNull () {}

	public function getPosition () {}

	public function isOptional () {}

	public function isDefaultValueAvailable () {}

	public function getDefaultValue () {}

}

/**
 * The ReflectionMethod class lets you
 * reverse-engineer class methods.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionMethod extends ReflectionFunctionAbstract implements Reflector {
	const IS_STATIC = 1;
	const IS_PUBLIC = 256;
	const IS_PROTECTED = 512;
	const IS_PRIVATE = 1024;
	const IS_ABSTRACT = 2;
	const IS_FINAL = 4;

	public $name;
	public $class;


	/**
	 * @param class
	 * @param name
	 * @param return[optional]
	 */
	public static function export ($class, $name, $return) {}

	/**
	 * @param class_or_method
	 * @param name[optional]
	 */
	public function __construct ($class_or_method, $name) {}

	public function __toString () {}

	public function isPublic () {}

	public function isPrivate () {}

	public function isProtected () {}

	public function isAbstract () {}

	public function isFinal () {}

	public function isStatic () {}

	public function isConstructor () {}

	public function isDestructor () {}

	public function getModifiers () {}

	/**
	 * @param object
	 * @param args
	 */
	public function invoke ($object, $args) {}

	/**
	 * @param object
	 * @param args
	 */
	public function invokeArgs ($objectarray , $args) {}

	public function getDeclaringClass () {}

	public function getPrototype () {}

	final private function __clone () {}

	public function isInternal () {}

	public function isUserDefined () {}

	public function getName () {}

	public function getFileName () {}

	public function getStartLine () {}

	public function getEndLine () {}

	public function getDocComment () {}

	public function getStaticVariables () {}

	public function returnsReference () {}

	public function getParameters () {}

	public function getNumberOfParameters () {}

	public function getNumberOfRequiredParameters () {}

	public function getExtension () {}

	public function getExtensionName () {}

	public function isDeprecated () {}

}

/**
 * The ReflectionClass class lets
 * you reverse-engineer classes.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionClass implements Reflector {
	const IS_IMPLICIT_ABSTRACT = 16;
	const IS_EXPLICIT_ABSTRACT = 32;
	const IS_FINAL = 64;

	public $name;


	final private function __clone () {}

	/**
	 * @param argument
	 * @param return[optional]
	 */
	public static function export ($argument, $return) {}

	/**
	 * @param argument
	 */
	public function __construct ($argument) {}

	public function __toString () {}

	public function getName () {}

	public function isInternal () {}

	public function isUserDefined () {}

	public function isInstantiable () {}

	public function getFileName () {}

	public function getStartLine () {}

	public function getEndLine () {}

	public function getDocComment () {}

	public function getConstructor () {}

	/**
	 * @param name
	 */
	public function hasMethod ($name) {}

	/**
	 * @param name
	 */
	public function getMethod ($name) {}

	/**
	 * @param filter[optional]
	 */
	public function getMethods ($filter) {}

	/**
	 * @param name
	 */
	public function hasProperty ($name) {}

	/**
	 * @param name
	 */
	public function getProperty ($name) {}

	/**
	 * @param filter[optional]
	 */
	public function getProperties ($filter) {}

	/**
	 * @param name
	 */
	public function hasConstant ($name) {}

	public function getConstants () {}

	/**
	 * @param name
	 */
	public function getConstant ($name) {}

	public function getInterfaces () {}

	public function getInterfaceNames () {}

	public function isInterface () {}

	public function isAbstract () {}

	public function isFinal () {}

	public function getModifiers () {}

	/**
	 * @param object
	 */
	public function isInstance ($object) {}

	/**
	 * @param args
	 */
	public function newInstance ($args) {}

	/**
	 * @param args[optional]
	 */
	public function newInstanceArgs (array $args) {}

	public function getParentClass () {}

	/**
	 * @param class
	 */
	public function isSubclassOf ($class) {}

	public function getStaticProperties () {}

	/**
	 * @param name
	 * @param default[optional]
	 */
	public function getStaticPropertyValue ($name, $default) {}

	/**
	 * @param name
	 * @param value
	 */
	public function setStaticPropertyValue ($name, $value) {}

	public function getDefaultProperties () {}

	public function isIterateable () {}

	/**
	 * @param interface
	 */
	public function implementsInterface ($interface) {}

	public function getExtension () {}

	public function getExtensionName () {}

}

/**
 * The ReflectionObject class lets
 * you reverse-engineer objects.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionObject extends ReflectionClass implements Reflector {
	const IS_IMPLICIT_ABSTRACT = 16;
	const IS_EXPLICIT_ABSTRACT = 32;
	const IS_FINAL = 64;

	public $name;


	/**
	 * @param argument
	 * @param return[optional]
	 */
	public static function export ($argument, $return) {}

	/**
	 * @param argument
	 */
	public function __construct ($argument) {}

	final private function __clone () {}

	public function __toString () {}

	public function getName () {}

	public function isInternal () {}

	public function isUserDefined () {}

	public function isInstantiable () {}

	public function getFileName () {}

	public function getStartLine () {}

	public function getEndLine () {}

	public function getDocComment () {}

	public function getConstructor () {}

	/**
	 * @param name
	 */
	public function hasMethod ($name) {}

	/**
	 * @param name
	 */
	public function getMethod ($name) {}

	/**
	 * @param filter[optional]
	 */
	public function getMethods ($filter) {}

	/**
	 * @param name
	 */
	public function hasProperty ($name) {}

	/**
	 * @param name
	 */
	public function getProperty ($name) {}

	/**
	 * @param filter[optional]
	 */
	public function getProperties ($filter) {}

	/**
	 * @param name
	 */
	public function hasConstant ($name) {}

	public function getConstants () {}

	/**
	 * @param name
	 */
	public function getConstant ($name) {}

	public function getInterfaces () {}

	public function getInterfaceNames () {}

	public function isInterface () {}

	public function isAbstract () {}

	public function isFinal () {}

	public function getModifiers () {}

	/**
	 * @param object
	 */
	public function isInstance ($object) {}

	/**
	 * @param args
	 */
	public function newInstance ($args) {}

	/**
	 * @param args[optional]
	 */
	public function newInstanceArgs (array $args) {}

	public function getParentClass () {}

	/**
	 * @param class
	 */
	public function isSubclassOf ($class) {}

	public function getStaticProperties () {}

	/**
	 * @param name
	 * @param default[optional]
	 */
	public function getStaticPropertyValue ($name, $default) {}

	/**
	 * @param name
	 * @param value
	 */
	public function setStaticPropertyValue ($name, $value) {}

	public function getDefaultProperties () {}

	public function isIterateable () {}

	/**
	 * @param interface
	 */
	public function implementsInterface ($interface) {}

	public function getExtension () {}

	public function getExtensionName () {}

}

/**
 * The ReflectionProperty class lets you
 * reverse-engineer class properties.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionProperty implements Reflector {
	const IS_STATIC = 1;
	const IS_PUBLIC = 256;
	const IS_PROTECTED = 512;
	const IS_PRIVATE = 1024;

	public $name;
	public $class;


	final private function __clone () {}

	/**
	 * @param argument
	 * @param return[optional]
	 */
	public static function export ($argument, $return) {}

	/**
	 * @param argument
	 */
	public function __construct ($argument) {}

	public function __toString () {}

	public function getName () {}

	/**
	 * @param object[optional]
	 */
	public function getValue ($object) {}

	/**
	 * @param object
	 * @param value
	 */
	public function setValue ($object, $value) {}

	public function isPublic () {}

	public function isPrivate () {}

	public function isProtected () {}

	public function isStatic () {}

	public function isDefault () {}

	public function getModifiers () {}

	public function getDeclaringClass () {}

	public function getDocComment () {}

}

/**
 * The ReflectionExtension class lets you
 * reverse-engineer extensions. You can retrieve all loaded extensions
 * at runtime using the get_loaded_extensions.
 * @link http://php.net/manual/en/language.oop5.reflection.php
 */
class ReflectionExtension implements Reflector {
	public $name;


	final private function __clone () {}

	/**
	 * @param name
	 * @param return[optional]
	 */
	public static function export ($name, $return) {}

	/**
	 * @param name
	 */
	public function __construct ($name) {}

	public function __toString () {}

	public function getName () {}

	public function getVersion () {}

	public function getFunctions () {}

	public function getConstants () {}

	public function getINIEntries () {}

	public function getClasses () {}

	public function getClassNames () {}

	public function getDependencies () {}

	public function info () {}

}
// End of Reflection v.0.1

// Start of json v.1.2.1

/**
 * Returns the JSON representation of a value
 * @link http://php.net/manual/en/function.json-encode.php
 * @param value mixed
 * @return string a JSON encoded string on success.
 */
function json_encode ($value) {}

/**
 * Decodes a JSON string
 * @link http://php.net/manual/en/function.json-decode.php
 * @param json string
 * @param assoc bool[optional]
 * @return mixed an object or if the optional
 */
function json_decode ($json, $assoc = null) {}

// End of json v.1.2.1

// Start of hash v.1.0

/**
 * Generate a hash value (message digest)
 * @link http://php.net/manual/en/function.hash.php
 * @param algo string
 * @param data string
 * @param raw_output bool[optional]
 * @return string a string containing the calculated message digest as lowercase hexits
 */
function hash ($algo, $data, $raw_output = null) {}

/**
 * Generate a hash value using the contents of a given file
 * @link http://php.net/manual/en/function.hash-file.php
 * @param algo string
 * @param filename string
 * @param raw_output bool[optional]
 * @return string a string containing the calculated message digest as lowercase hexits
 */
function hash_file ($algo, $filename, $raw_output = null) {}

/**
 * Generate a keyed hash value using the HMAC method
 * @link http://php.net/manual/en/function.hash-hmac.php
 * @param algo string
 * @param data string
 * @param key string
 * @param raw_output bool[optional]
 * @return string a string containing the calculated message digest as lowercase hexits
 */
function hash_hmac ($algo, $data, $key, $raw_output = null) {}

/**
 * Generate a keyed hash value using the HMAC method and the contents of a given file
 * @link http://php.net/manual/en/function.hash-hmac-file.php
 * @param algo string
 * @param filename string
 * @param key string
 * @param raw_output bool[optional]
 * @return string a string containing the calculated message digest as lowercase hexits
 */
function hash_hmac_file ($algo, $filename, $key, $raw_output = null) {}

/**
 * Initialize an incremental hashing context
 * @link http://php.net/manual/en/function.hash-init.php
 * @param algo string
 * @param options int[optional]
 * @param key string
 * @return resource a Hashing Context resource for use with hash_update,
 */
function hash_init ($algo, $options = null, $key) {}

/**
 * Pump data into an active hashing context
 * @link http://php.net/manual/en/function.hash-update.php
 * @param context resource
 * @param data string
 * @return bool true.
 */
function hash_update ($context, $data) {}

/**
 * Pump data into an active hashing context from an open stream
 * @link http://php.net/manual/en/function.hash-update-stream.php
 * @param context resource
 * @param handle resource
 * @param length int[optional]
 * @return int 
 */
function hash_update_stream ($context, $handle, $length = null) {}

/**
 * Pump data into an active hashing context from a file
 * @link http://php.net/manual/en/function.hash-update-file.php
 * @param context resource
 * @param filename string
 * @param context resource[optional]
 * @return bool 
 */
function hash_update_file ($context, $filename, $context = null) {}

/**
 * Finalize an incremental hash and return resulting digest
 * @link http://php.net/manual/en/function.hash-final.php
 * @param context resource
 * @param raw_output bool[optional]
 * @return string a string containing the calculated message digest as lowercase hexits
 */
function hash_final ($context, $raw_output = null) {}

/**
 * Return a list of registered hashing algorithms
 * @link http://php.net/manual/en/function.hash-algos.php
 * @return array a numerically indexed array containing the list of supported
 */
function hash_algos () {}


/**
 * Optional flag for hash_init.
 * Indicates that the HMAC digest-keying algorithm should be
 * applied to the current hashing context.
 * @link http://php.net/manual/en/hash.constants.php
 */
define ('HASH_HMAC', 1);

// End of hash v.1.0

// Start of filter v.0.11.0

/**
 * Gets variable from outside PHP and optionally filters it
 * @link http://php.net/manual/en/function.filter-input.php
 * @param type int
 * @param variable_name string
 * @param filter int[optional]
 * @param options mixed[optional]
 * @return mixed 
 */
function filter_input ($type, $variable_name, $filter = null, $options = null) {}

/**
 * Filters a variable with a specified filter
 * @link http://php.net/manual/en/function.filter-var.php
 * @param variable mixed
 * @param filter int[optional]
 * @param options mixed[optional]
 * @return mixed the filtered data, or false if the filter fails.
 */
function filter_var ($variable, $filter = null, $options = null) {}

/**
 * Gets multiple variables from outside PHP and optionally filters them
 * @link http://php.net/manual/en/function.filter-input-array.php
 * @param type int
 * @param definition mixed[optional]
 * @return mixed 
 */
function filter_input_array ($type, $definition = null) {}

/**
 * Gets multiple variables and optionally filters them
 * @link http://php.net/manual/en/function.filter-var-array.php
 * @param data array
 * @param definition mixed[optional]
 * @return mixed 
 */
function filter_var_array (array $data, $definition = null) {}

/**
 * Returns a list of all supported filters
 * @link http://php.net/manual/en/function.filter-list.php
 * @return array an array of names of all supported filters, empty array if there
 */
function filter_list () {}

/**
 * Checks if variable of specified type exists
 * @link http://php.net/manual/en/function.filter-has-var.php
 * @param type int
 * @param variable_name string
 * @return bool 
 */
function filter_has_var ($type, $variable_name) {}

/**
 * Returns the filter ID belonging to a named filter
 * @link http://php.net/manual/en/function.filter-id.php
 * @param filtername string
 * @return int 
 */
function filter_id ($filtername) {}


/**
 * POST variables.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_POST', 0);

/**
 * GET variables.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_GET', 1);

/**
 * COOKIE variables.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_COOKIE', 2);

/**
 * ENV variables.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_ENV', 4);

/**
 * SERVER variables.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_SERVER', 5);

/**
 * SESSION variables.
 * (not implemented yet)
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_SESSION', 6);

/**
 * REQUEST variables.
 * (not implemented yet)
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('INPUT_REQUEST', 99);

/**
 * No flags.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_NONE', 0);

/**
 * Flag used to require scalar as input
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_REQUIRE_SCALAR', 33554432);

/**
 * Require an array as input.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_REQUIRE_ARRAY', 16777216);

/**
 * Always returns an array.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FORCE_ARRAY', 67108864);

/**
 * Use NULL instead of FALSE on failure.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_NULL_ON_FAILURE', 134217728);

/**
 * ID of "int" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_INT', 257);

/**
 * ID of "boolean" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_BOOLEAN', 258);

/**
 * ID of "float" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_FLOAT', 259);

/**
 * ID of "validate_regexp" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_REGEXP', 272);

/**
 * ID of "validate_url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_URL', 273);

/**
 * ID of "validate_email" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_EMAIL', 274);

/**
 * ID of "validate_ip" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_VALIDATE_IP', 275);

/**
 * ID of default ("string") filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_DEFAULT', 516);

/**
 * ID of "unsafe_raw" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_UNSAFE_RAW', 516);

/**
 * ID of "string" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_STRING', 513);

/**
 * ID of "stripped" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_STRIPPED', 513);

/**
 * ID of "encoded" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_ENCODED', 514);

/**
 * ID of "special_chars" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_SPECIAL_CHARS', 515);

/**
 * ID of "email" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_EMAIL', 517);

/**
 * ID of "url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_URL', 518);

/**
 * ID of "number_int" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_NUMBER_INT', 519);

/**
 * ID of "number_float" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_NUMBER_FLOAT', 520);

/**
 * ID of "magic_quotes" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_SANITIZE_MAGIC_QUOTES', 521);

/**
 * ID of "callback" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_CALLBACK', 1024);

/**
 * Allow octal notation (0[0-7]+) in "int" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ALLOW_OCTAL', 1);

/**
 * Allow hex notation (0x[0-9a-fA-F]+) in "int" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ALLOW_HEX', 2);

/**
 * Strip characters with ASCII value less than 32.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_STRIP_LOW', 4);

/**
 * Strip characters with ASCII value greater than 127.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_STRIP_HIGH', 8);

/**
 * Encode characters with ASCII value less than 32.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ENCODE_LOW', 16);

/**
 * Encode characters with ASCII value greater than 127.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ENCODE_HIGH', 32);

/**
 * Encode &amp;.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ENCODE_AMP', 64);

/**
 * Don't encode ' and ".
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_NO_ENCODE_QUOTES', 128);

/**
 * (No use for now.)
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_EMPTY_STRING_NULL', 256);

/**
 * Allow fractional part in "number_float" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ALLOW_FRACTION', 4096);

/**
 * Allow thousand separator (,) in "number_float" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ALLOW_THOUSAND', 8192);

/**
 * Allow scientific notation (e, E) in
 * "number_float" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_ALLOW_SCIENTIFIC', 16384);

/**
 * Require scheme in "validate_url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_SCHEME_REQUIRED', 65536);

/**
 * Require host in "validate_url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_HOST_REQUIRED', 131072);

/**
 * Require path in "validate_url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_PATH_REQUIRED', 262144);

/**
 * Require query in "validate_url" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_QUERY_REQUIRED', 524288);

/**
 * Allow only IPv4 address in "validate_ip" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_IPV4', 1048576);

/**
 * Allow only IPv6 address in "validate_ip" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_IPV6', 2097152);

/**
 * Deny reserved addresses in "validate_ip" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_NO_RES_RANGE', 4194304);

/**
 * Deny private addresses in "validate_ip" filter.
 * @link http://php.net/manual/en/filter.constants.php
 */
define ('FILTER_FLAG_NO_PRIV_RANGE', 8388608);

// End of filter v.0.11.0

// Start of dom v.20031129

/**
 * DOM operations raise exceptions under particular circumstances, i.e.,
 * when an operation is impossible to perform for logical reasons.
 * @link http://php.net/manual/en/ref.dom.php
 */
final class DOMException extends Exception  {
	protected $message;
	public $code;
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

class DOMStringList  {

	public function item () {}

}

/**
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMNameList  {

	public function getName () {}

	public function getNamespaceURI () {}

}

class DOMImplementationList  {

	public function item () {}

}

class DOMImplementationSource  {

	public function getDomimplementation () {}

	public function getDomimplementations () {}

}

/**
 * The DOMImplementation interface provides a number
 * of methods for performing operations that are independent of any 
 * particular instance of the document object model.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMImplementation  {

	public function getFeature () {}

	/**
	 * Test if the DOM implementation implements a specific feature
	 * @link http://php.net/manual/en/function.dom-domimplementation-hasfeature.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function hasFeature ($feature, $version) {}

	/**
	 * Creates an empty DOMDocumentType object
	 * @link http://php.net/manual/en/function.dom-domimplementation-createdocumenttype.php
	 * @param qualifiedName string[optional]
	 * @param publicId string[optional]
	 * @param systemId string[optional]
	 * @return DOMDocumentType 
	 */
	public function createDocumentType ($qualifiedName = null, $publicId = null, $systemId = null) {}

	/**
	 * Creates a DOMDocument object of the specified type with its document element
	 * @link http://php.net/manual/en/function.dom-domimplementation-createdocument.php
	 * @param namespaceURI string[optional]
	 * @param qualifiedName string[optional]
	 * @param doctype DOMDocumentType[optional]
	 * @return DOMDocument 
	 */
	public function createDocument ($namespaceURI = null, $qualifiedName = null, DOMDocumentType $doctype = null) {}

}

/**
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMNode  {

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

class DOMNameSpaceNode  {
}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMDocumentFragment extends DOMNode  {

	public function __construct () {}

	/**
	 * Append raw XML data
	 * @link http://php.net/manual/en/function.dom-domdocumentfragment-appendxml.php
	 * @param data string
	 * @return bool 
	 */
	public function appendXML ($data) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMDocument extends DOMNode  {

	/**
	 * Create new element node
	 * @link http://php.net/manual/en/function.dom-domdocument-createelement.php
	 * @param name string
	 * @param value string[optional]
	 * @return DOMElement a new instance of class DOMElement or false
	 */
	public function createElement ($name, $value = null) {}

	/**
	 * Create new document fragment
	 * @link http://php.net/manual/en/function.dom-domdocument-createdocumentfragment.php
	 * @return DOMDocumentFragment 
	 */
	public function createDocumentFragment () {}

	/**
	 * Create new text node
	 * @link http://php.net/manual/en/function.dom-domdocument-createtextnode.php
	 * @param content string
	 * @return DOMText 
	 */
	public function createTextNode ($content) {}

	/**
	 * Create new comment node
	 * @link http://php.net/manual/en/function.dom-domdocument-createcomment.php
	 * @param data string
	 * @return DOMComment 
	 */
	public function createComment ($data) {}

	/**
	 * Create new cdata node
	 * @link http://php.net/manual/en/function.dom-domdocument-createcdatasection.php
	 * @param data string
	 * @return DOMCDATASection 
	 */
	public function createCDATASection ($data) {}

	/**
	 * Creates new PI node
	 * @link http://php.net/manual/en/function.dom-domdocument-createprocessinginstruction.php
	 * @param target string
	 * @param data string[optional]
	 * @return DOMProcessingInstruction 
	 */
	public function createProcessingInstruction ($target, $data = null) {}

	/**
	 * Create new attribute
	 * @link http://php.net/manual/en/function.dom-domdocument-createattribute.php
	 * @param name string
	 * @return DOMAttr 
	 */
	public function createAttribute ($name) {}

	/**
	 * Create new entity reference node
	 * @link http://php.net/manual/en/function.dom-domdocument-createentityreference.php
	 * @param name string
	 * @return DOMEntityReference 
	 */
	public function createEntityReference ($name) {}

	/**
	 * Searches for all elements with given tag name
	 * @link http://php.net/manual/en/function.dom-domdocument-getelementsbytagname.php
	 * @param name string
	 * @return DOMNodeList 
	 */
	public function getElementsByTagName ($name) {}

	/**
	 * Import node into current document
	 * @link http://php.net/manual/en/function.dom-domdocument-importnode.php
	 * @param importedNode DOMNode
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function importNode (DOMNode $importedNode, $deep = null) {}

	/**
	 * Create new element node with an associated namespace
	 * @link http://php.net/manual/en/function.dom-domdocument-createelementns.php
	 * @param namespaceURI string
	 * @param qualifiedName string
	 * @param value string[optional]
	 * @return DOMElement 
	 */
	public function createElementNS ($namespaceURI, $qualifiedName, $value = null) {}

	/**
	 * Create new attribute node with an associated namespace
	 * @link http://php.net/manual/en/function.dom-domdocument-createattributens.php
	 * @param namespaceURI string
	 * @param qualifiedName string
	 * @return DOMAttr 
	 */
	public function createAttributeNS ($namespaceURI, $qualifiedName) {}

	/**
	 * Searches for all elements with given tag name in specified namespace
	 * @link http://php.net/manual/en/function.dom-domdocument-getelementsbytagnamens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return DOMNodeList 
	 */
	public function getElementsByTagNameNS ($namespaceURI, $localName) {}

	/**
	 * Searches for an element with a certain id
	 * @link http://php.net/manual/en/function.dom-domdocument-getelementbyid.php
	 * @param elementId string
	 * @return DOMElement the DOMElement or &null; if the element is
	 */
	public function getElementById ($elementId) {}

	public function adoptNode () {}

	/**
	 * Normalizes the document
	 * @link http://php.net/manual/en/function.dom-domdocument-normalizedocument.php
	 * @return void 
	 */
	public function normalizeDocument () {}

	public function renameNode () {}

	/**
	 * Load XML from a file
	 * @link http://php.net/manual/en/function.dom-domdocument-load.php
	 * @param filename string
	 * @param options int[optional]
	 * @return mixed 
	 */
	public function load ($filename, $options = null) {}

	/**
	 * Dumps the internal XML tree back into a file
	 * @link http://php.net/manual/en/function.dom-domdocument-save.php
	 * @param filename string
	 * @param options int[optional]
	 * @return mixed the number of bytes written or false if an error occurred.
	 */
	public function save ($filename, $options = null) {}

	/**
	 * Load XML from a string
	 * @link http://php.net/manual/en/function.dom-domdocument-loadxml.php
	 * @param source string
	 * @param options int[optional]
	 * @return mixed 
	 */
	public function loadXML ($source, $options = null) {}

	/**
	 * Dumps the internal XML tree back into a string
	 * @link http://php.net/manual/en/function.dom-domdocument-savexml.php
	 * @param node DOMNode[optional]
	 * @param options int[optional]
	 * @return string the XML, or false if an error occurred.
	 */
	public function saveXML (DOMNode $node = null, $options = null) {}

	/**
	 * Creates a new DOMDocument object
	 * @link http://php.net/manual/en/function.dom-domdocument-construct.php
	 */
	public function __construct () {}

	/**
	 * Validates the document based on its DTD
	 * @link http://php.net/manual/en/function.dom-domdocument-validate.php
	 * @return bool 
	 */
	public function validate () {}

	/**
	 * Substitutes XIncludes in a DomDocument Object
	 * @link http://php.net/manual/en/function.domdocument-xinclude.php
	 * @param options int[optional]
	 * @return int the number of XIncludes in the document.
	 */
	public function xinclude ($options = null) {}

	/**
	 * Load HTML from a string
	 * @link http://php.net/manual/en/function.dom-domdocument-loadhtml.php
	 * @param source string
	 * @return bool 
	 */
	public function loadHTML ($source) {}

	/**
	 * Load HTML from a file
	 * @link http://php.net/manual/en/function.dom-domdocument-loadhtmlfile.php
	 * @param filename string
	 * @return bool 
	 */
	public function loadHTMLFile ($filename) {}

	/**
	 * Dumps the internal document into a string using HTML formatting
	 * @link http://php.net/manual/en/function.dom-domdocument-savehtml.php
	 * @return string the HTML, or false if an error occurred.
	 */
	public function saveHTML () {}

	/**
	 * Dumps the internal document into a file using HTML formatting
	 * @link http://php.net/manual/en/function.dom-domdocument-savehtmlfile.php
	 * @param filename string
	 * @return int the number of bytes written or false if an error occurred.
	 */
	public function saveHTMLFile ($filename) {}

	/**
	 * Validates a document based on a schema
	 * @link http://php.net/manual/en/function.dom-domdocument-schemavalidate.php
	 * @param filename string
	 * @return bool 
	 */
	public function schemaValidate ($filename) {}

	/**
	 * Validates a document based on a schema
	 * @link http://php.net/manual/en/function.dom-domdocument-schemavalidatesource.php
	 * @param source string
	 * @return bool 
	 */
	public function schemaValidateSource ($source) {}

	/**
	 * Performs relaxNG validation on the document
	 * @link http://php.net/manual/en/function.dom-domdocument-relaxngvalidate.php
	 * @param filename string
	 * @return bool 
	 */
	public function relaxNGValidate ($filename) {}

	/**
	 * Performs relaxNG validation on the document
	 * @link http://php.net/manual/en/function.dom-domdocument-relaxngvalidatesource.php
	 * @param source string
	 * @return bool 
	 */
	public function relaxNGValidateSource ($source) {}

	/**
	 * Register extended class used to create base node type
	 * @link http://php.net/manual/en/function.dom-domdocument-registernodeclass.php
	 * @param baseclass string
	 * @param extendedclass string
	 * @return bool 
	 */
	public function registerNodeClass ($baseclass, $extendedclass) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMNodeList  {

	/**
	 * Retrieves a node specified by index
	 * @link http://php.net/manual/en/function.dom-domnodelist-item.php
	 * @param index int
	 * @return DOMNode 
	 */
	public function item ($index) {}

}

/**
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMNamedNodeMap  {

	/**
	 * Retrieves a node specified by name
	 * @link http://php.net/manual/en/function.dom-domnamednodemap-getnameditem.php
	 * @param name string
	 * @return DOMNode 
	 */
	public function getNamedItem ($name) {}

	public function setNamedItem () {}

	public function removeNamedItem () {}

	/**
	 * Retrieves a node specified by index
	 * @link http://php.net/manual/en/function.dom-domnamednodemap-item.php
	 * @param index int
	 * @return DOMNode 
	 */
	public function item ($index) {}

	/**
	 * Retrieves a node specified by local name and namespace URI
	 * @link http://php.net/manual/en/function.dom-domnamednodemap-getnameditemns.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return DOMNode 
	 */
	public function getNamedItemNS ($namespaceURI, $localName) {}

	public function setNamedItemNS () {}

	public function removeNamedItemNS () {}

}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMCharacterData extends DOMNode  {

	/**
	 * Extracts a range of data from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-substringdata.php
	 * @param offset int
	 * @param count int
	 * @return string 
	 */
	public function substringData ($offset, $count) {}

	/**
	 * Append the string to the end of the character data of the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-appenddata.php
	 * @param data string
	 * @return void 
	 */
	public function appendData ($data) {}

	/**
	 * Insert a string at the specified 16-bit unit offset
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-insertdata.php
	 * @param offset int
	 * @param data string
	 * @return void 
	 */
	public function insertData ($offset, $data) {}

	/**
	 * Remove a range of characters from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-deletedata.php
	 * @param offset int
	 * @param count int
	 * @return void 
	 */
	public function deleteData ($offset, $count) {}

	/**
	 * Replace a substring within the DOMCharacterData node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-replacedata.php
	 * @param offset int
	 * @param count int
	 * @param data string
	 * @return void 
	 */
	public function replaceData ($offset, $count, $data) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode. The DOMAttr
 * interface represents an attribute in an DOMElement object.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMAttr extends DOMNode  {

	/**
	 * Checks if attribute is a defined ID
	 * @link http://php.net/manual/en/function.dom-domattr-isid.php
	 * @return bool 
	 */
	public function isId () {}

	/**
	 * Creates a new DOMAttr object
	 * @link http://php.net/manual/en/function.dom-domattr-construct.php
	 */
	public function __construct () {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMElement extends DOMNode  {

	/**
	 * Returns value of attribute
	 * @link http://php.net/manual/en/function.dom-domelement-getattribute.php
	 * @param name string
	 * @return string 
	 */
	public function getAttribute ($name) {}

	/**
	 * Adds new attribute
	 * @link http://php.net/manual/en/function.dom-domelement-setattribute.php
	 * @param name string
	 * @param value string
	 * @return bool 
	 */
	public function setAttribute ($name, $value) {}

	/**
	 * Removes attribute
	 * @link http://php.net/manual/en/function.dom-domelement-removeattribute.php
	 * @param name string
	 * @return bool 
	 */
	public function removeAttribute ($name) {}

	/**
	 * Returns attribute node
	 * @link http://php.net/manual/en/function.dom-domelement-getattributenode.php
	 * @param name string
	 * @return DOMAttr 
	 */
	public function getAttributeNode ($name) {}

	/**
	 * Adds new attribute node to element
	 * @link http://php.net/manual/en/function.dom-domelement-setattributenode.php
	 * @param attr DOMAttr
	 * @return DOMAttr old node if the attribute has been replaced or &null;.
	 */
	public function setAttributeNode (DOMAttr $attr) {}

	/**
	 * Removes attribute
	 * @link http://php.net/manual/en/function.dom-domelement-removeattributenode.php
	 * @param oldnode DOMAttr
	 * @return bool 
	 */
	public function removeAttributeNode (DOMAttr $oldnode) {}

	/**
	 * Gets elements by tagname
	 * @link http://php.net/manual/en/function.dom-domelement-getelementsbytagname.php
	 * @param name string
	 * @return DOMNodeList 
	 */
	public function getElementsByTagName ($name) {}

	/**
	 * Returns value of attribute
	 * @link http://php.net/manual/en/function.dom-domelement-getattributens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return string 
	 */
	public function getAttributeNS ($namespaceURI, $localName) {}

	/**
	 * Adds new attribute
	 * @link http://php.net/manual/en/function.dom-domelement-setattributens.php
	 * @param namespaceURI string
	 * @param qualifiedName string
	 * @param value string
	 * @return void 
	 */
	public function setAttributeNS ($namespaceURI, $qualifiedName, $value) {}

	/**
	 * Removes attribute
	 * @link http://php.net/manual/en/function.dom-domelement-removeattributens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return bool 
	 */
	public function removeAttributeNS ($namespaceURI, $localName) {}

	/**
	 * Returns attribute node
	 * @link http://php.net/manual/en/function.dom-domelement-getattributenodens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return DOMAttr 
	 */
	public function getAttributeNodeNS ($namespaceURI, $localName) {}

	/**
	 * Adds new attribute node to element
	 * @link http://php.net/manual/en/function.dom-domelement-setattributenodens.php
	 * @param attr DOMAttr
	 * @return DOMAttr the old node if the attribute has been replaced.
	 */
	public function setAttributeNodeNS (DOMAttr $attr) {}

	/**
	 * Get elements by namespaceURI and localName
	 * @link http://php.net/manual/en/function.dom-domelement-getelementsbytagnamens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return DOMNodeList 
	 */
	public function getElementsByTagNameNS ($namespaceURI, $localName) {}

	/**
	 * Checks to see if attribute exists
	 * @link http://php.net/manual/en/function.dom-domelement-hasattribute.php
	 * @param name string
	 * @return bool 
	 */
	public function hasAttribute ($name) {}

	/**
	 * Checks to see if attribute exists
	 * @link http://php.net/manual/en/function.dom-domelement-hasattributens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return bool 
	 */
	public function hasAttributeNS ($namespaceURI, $localName) {}

	/**
	 * Declares the attribute specified by name to be of type ID
	 * @link http://php.net/manual/en/function.dom-domelement-setidattribute.php
	 * @param name string
	 * @param isId bool
	 * @return void 
	 */
	public function setIdAttribute ($name, $isId) {}

	/**
	 * Declares the attribute specified by local name and namespace URI to be of type ID
	 * @link http://php.net/manual/en/function.dom-domelement-setidattributens.php
	 * @param namespaceURI string
	 * @param localName string
	 * @param isId bool
	 * @return void 
	 */
	public function setIdAttributeNS ($namespaceURI, $localName, $isId) {}

	/**
	 * Declares the attribute specified by node to be of type ID
	 * @link http://php.net/manual/en/function.dom-domelement-setidattributenode.php
	 * @param attr DOMAttr
	 * @param isId bool
	 * @return void 
	 */
	public function setIdAttributeNode (DOMAttr $attr, $isId) {}

	/**
	 * Creates a new DOMElement object
	 * @link http://php.net/manual/en/function.dom-domelement-construct.php
	 */
	public function __construct () {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMCharacterData.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMText extends DOMCharacterData  {

	/**
	 * Breaks this node into two nodes at the specified offset
	 * @link http://php.net/manual/en/function.dom-domtext-splittext.php
	 * @param offset int
	 * @return DOMText 
	 */
	public function splitText ($offset) {}

	/**
	 * Indicates whether this text node contains whitespace
	 * @link http://php.net/manual/en/function.dom-domtext-iswhitespaceinelementcontent.php
	 * @return bool 
	 */
	public function isWhitespaceInElementContent () {}

	public function isElementContentWhitespace () {}

	public function replaceWholeText () {}

	/**
	 * Creates a new DOMText object
	 * @link http://php.net/manual/en/function.dom-domtext-construct.php
	 */
	public function __construct () {}

	/**
	 * Extracts a range of data from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-substringdata.php
	 * @param offset int
	 * @param count int
	 * @return string 
	 */
	public function substringData ($offset, $count) {}

	/**
	 * Append the string to the end of the character data of the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-appenddata.php
	 * @param data string
	 * @return void 
	 */
	public function appendData ($data) {}

	/**
	 * Insert a string at the specified 16-bit unit offset
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-insertdata.php
	 * @param offset int
	 * @param data string
	 * @return void 
	 */
	public function insertData ($offset, $data) {}

	/**
	 * Remove a range of characters from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-deletedata.php
	 * @param offset int
	 * @param count int
	 * @return void 
	 */
	public function deleteData ($offset, $count) {}

	/**
	 * Replace a substring within the DOMCharacterData node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-replacedata.php
	 * @param offset int
	 * @param count int
	 * @param data string
	 * @return void 
	 */
	public function replaceData ($offset, $count, $data) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMCharacterData.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMComment extends DOMCharacterData  {

	/**
	 * Creates a new DOMComment object
	 * @link http://php.net/manual/en/function.dom-domcomment-construct.php
	 */
	public function __construct () {}

	/**
	 * Extracts a range of data from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-substringdata.php
	 * @param offset int
	 * @param count int
	 * @return string 
	 */
	public function substringData ($offset, $count) {}

	/**
	 * Append the string to the end of the character data of the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-appenddata.php
	 * @param data string
	 * @return void 
	 */
	public function appendData ($data) {}

	/**
	 * Insert a string at the specified 16-bit unit offset
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-insertdata.php
	 * @param offset int
	 * @param data string
	 * @return void 
	 */
	public function insertData ($offset, $data) {}

	/**
	 * Remove a range of characters from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-deletedata.php
	 * @param offset int
	 * @param count int
	 * @return void 
	 */
	public function deleteData ($offset, $count) {}

	/**
	 * Replace a substring within the DOMCharacterData node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-replacedata.php
	 * @param offset int
	 * @param count int
	 * @param data string
	 * @return void 
	 */
	public function replaceData ($offset, $count, $data) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

class DOMTypeinfo  {
}

class DOMUserDataHandler  {

	public function handle () {}

}

class DOMDomError  {
}

class DOMErrorHandler  {

	public function handleError () {}

}

class DOMLocator  {
}

class DOMConfiguration  {

	public function setParameter () {}

	public function getParameter () {}

	public function canSetParameter () {}

}

class DOMCdataSection extends DOMText  {

	public function __construct () {}

	/**
	 * Breaks this node into two nodes at the specified offset
	 * @link http://php.net/manual/en/function.dom-domtext-splittext.php
	 * @param offset int
	 * @return DOMText 
	 */
	public function splitText ($offset) {}

	/**
	 * Indicates whether this text node contains whitespace
	 * @link http://php.net/manual/en/function.dom-domtext-iswhitespaceinelementcontent.php
	 * @return bool 
	 */
	public function isWhitespaceInElementContent () {}

	public function isElementContentWhitespace () {}

	public function replaceWholeText () {}

	/**
	 * Extracts a range of data from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-substringdata.php
	 * @param offset int
	 * @param count int
	 * @return string 
	 */
	public function substringData ($offset, $count) {}

	/**
	 * Append the string to the end of the character data of the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-appenddata.php
	 * @param data string
	 * @return void 
	 */
	public function appendData ($data) {}

	/**
	 * Insert a string at the specified 16-bit unit offset
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-insertdata.php
	 * @param offset int
	 * @param data string
	 * @return void 
	 */
	public function insertData ($offset, $data) {}

	/**
	 * Remove a range of characters from the node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-deletedata.php
	 * @param offset int
	 * @param count int
	 * @return void 
	 */
	public function deleteData ($offset, $count) {}

	/**
	 * Replace a substring within the DOMCharacterData node
	 * @link http://php.net/manual/en/function.dom-domcharacterdata-replacedata.php
	 * @param offset int
	 * @param count int
	 * @param data string
	 * @return void 
	 */
	public function replaceData ($offset, $count, $data) {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMDocumentType extends DOMNode  {

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMNotation  {
}

/**
 * Extends DOMNode
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMEntity extends DOMNode  {

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMEntityReference extends DOMNode  {

	/**
	 * Creates a new DOMEntityReference object
	 * @link http://php.net/manual/en/function.dom-domentityreference-construct.php
	 */
	public function __construct () {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

/**
 * Extends DOMNode.
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMProcessingInstruction extends DOMNode  {

	/**
	 * Creates a new DOMProcessingInstruction object
	 * @link http://php.net/manual/en/function.dom-domprocessinginstruction-construct.php
	 */
	public function __construct () {}

	/**
	 * Adds a new child before a reference node
	 * @link http://php.net/manual/en/function.dom-domnode-insertbefore.php
	 * @param newnode DOMNode
	 * @param refnode DOMNode[optional]
	 * @return DOMNode 
	 */
	public function insertBefore (DOMNode $newnode, DOMNode $refnode = null) {}

	/**
	 * Replaces a child
	 * @link http://php.net/manual/en/function.dom-domnode-replacechild.php
	 * @param newnode DOMNode
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function replaceChild (DOMNode $newnode, DOMNode $oldnode) {}

	/**
	 * Removes child from list of children
	 * @link http://php.net/manual/en/function.dom-domnode-removechild.php
	 * @param oldnode DOMNode
	 * @return DOMNode 
	 */
	public function removeChild (DOMNode $oldnode) {}

	/**
	 * Adds new child at the end of the children
	 * @link http://php.net/manual/en/function.dom-domnode-appendchild.php
	 * @param newnode DOMNode
	 * @return DOMNode 
	 */
	public function appendChild (DOMNode $newnode) {}

	/**
	 * Checks if node has children
	 * @link http://php.net/manual/en/function.dom-domnode-haschildnodes.php
	 * @return bool 
	 */
	public function hasChildNodes () {}

	/**
	 * Clones a node
	 * @link http://php.net/manual/en/function.dom-domnode-clonenode.php
	 * @param deep bool[optional]
	 * @return DOMNode 
	 */
	public function cloneNode ($deep = null) {}

	/**
	 * Normalizes the node
	 * @link http://php.net/manual/en/function.dom-domnode-normalize.php
	 * @return void 
	 */
	public function normalize () {}

	/**
	 * Checks if feature is supported for specified version
	 * @link http://php.net/manual/en/function.dom-domnode-issupported.php
	 * @param feature string
	 * @param version string
	 * @return bool 
	 */
	public function isSupported ($feature, $version) {}

	/**
	 * Checks if node has attributes
	 * @link http://php.net/manual/en/function.dom-domnode-hasattributes.php
	 * @return bool 
	 */
	public function hasAttributes () {}

	public function compareDocumentPosition () {}

	/**
	 * Indicates if two nodes are the same node
	 * @link http://php.net/manual/en/function.dom-domnode-issamenode.php
	 * @param node DOMNode
	 * @return bool 
	 */
	public function isSameNode (DOMNode $node) {}

	/**
	 * Gets the namespace prefix of the node based on the namespace URI
	 * @link http://php.net/manual/en/function.dom-domnode-lookupprefix.php
	 * @param namespaceURI string
	 * @return string 
	 */
	public function lookupPrefix ($namespaceURI) {}

	/**
	 * Checks if the specified namespaceURI is the default namespace or not
	 * @link http://php.net/manual/en/function.dom-domnode-isdefaultnamespace.php
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function isDefaultNamespace ($namespaceURI) {}

	/**
	 * Gets the namespace URI of the node based on the prefix
	 * @link http://php.net/manual/en/function.dom-domnode-lookupnamespaceuri.php
	 * @param prefix string
	 * @return string 
	 */
	public function lookupNamespaceUri ($prefix) {}

	public function isEqualNode () {}

	public function getFeature () {}

	public function setUserData () {}

	public function getUserData () {}

	public function getNodePath () {}

	public function C14N () {}

	public function C14NFile () {}

}

class DOMStringExtend  {

	public function findOffset16 () {}

	public function findOffset32 () {}

}

/**
 * @link http://php.net/manual/en/ref.dom.php
 */
class DOMXPath  {

	/**
	 * Creates a new DOMXPath object
	 * @link http://php.net/manual/en/function.dom-domxpath-construct.php
	 */
	public function __construct () {}

	/**
	 * Registers the namespace with the DOMXPath object
	 * @link http://php.net/manual/en/function.dom-domxpath-registernamespace.php
	 * @param prefix string
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function registerNamespace ($prefix, $namespaceURI) {}

	/**
	 * Evaluates the given XPath expression
	 * @link http://php.net/manual/en/function.dom-domxpath-query.php
	 * @param expression string
	 * @param contextnode DOMNode[optional]
	 * @return DOMNodeList a DOMNodeList containing all nodes matching
	 */
	public function query ($expression, DOMNode $contextnode = null) {}

	/**
	 * Evaluates the given XPath expression and returns a typed result if possible.
	 * @link http://php.net/manual/en/function.dom-domxpath-evaluate.php
	 * @param expression string
	 * @param contextnode DOMNode[optional]
	 * @return mixed a typed result if possible or a DOMNodeList
	 */
	public function evaluate ($expression, DOMNode $contextnode = null) {}

}

/**
 * Gets a DOMElement object from a SimpleXMLElement object
 * @link http://php.net/manual/en/function.dom-import-simplexml.php
 * @param node SimpleXMLElement
 * @return DOMElement 
 */
function dom_import_simplexml (SimpleXMLElement $node) {}


/**
 * 1
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ELEMENT_NODE', 1);

/**
 * 2
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_NODE', 2);

/**
 * 3
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_TEXT_NODE', 3);

/**
 * 4
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_CDATA_SECTION_NODE', 4);

/**
 * 5
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ENTITY_REF_NODE', 5);

/**
 * 6
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ENTITY_NODE', 6);

/**
 * 7
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_PI_NODE', 7);

/**
 * 8
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_COMMENT_NODE', 8);

/**
 * 9
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_DOCUMENT_NODE', 9);

/**
 * 10
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_DOCUMENT_TYPE_NODE', 10);

/**
 * 11
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_DOCUMENT_FRAG_NODE', 11);

/**
 * 12
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_NOTATION_NODE', 12);

/**
 * 13
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_HTML_DOCUMENT_NODE', 13);

/**
 * 14
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_DTD_NODE', 14);

/**
 * 15
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ELEMENT_DECL_NODE', 15);

/**
 * 16
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_DECL_NODE', 16);

/**
 * 17
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ENTITY_DECL_NODE', 17);

/**
 * 18
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_NAMESPACE_DECL_NODE', 18);

/**
 * 2
 * @link http://php.net/manual/en/domxml.constants.php
 */
define ('XML_LOCAL_NAMESPACE', 18);

/**
 * 1
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_CDATA', 1);

/**
 * 2
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_ID', 2);

/**
 * 3
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_IDREF', 3);

/**
 * 4
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_IDREFS', 4);

/**
 * 5
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_ENTITY', 6);

/**
 * 7
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_NMTOKEN', 7);

/**
 * 8
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_NMTOKENS', 8);

/**
 * 9
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_ENUMERATION', 9);

/**
 * 10
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('XML_ATTRIBUTE_NOTATION', 10);
define ('DOM_PHP_ERR', 0);

/**
 * 1
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INDEX_SIZE_ERR', 1);

/**
 * 2
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOMSTRING_SIZE_ERR', 2);

/**
 * 3
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_HIERARCHY_REQUEST_ERR', 3);

/**
 * 4
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_WRONG_DOCUMENT_ERR', 4);

/**
 * 5
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INVALID_CHARACTER_ERR', 5);

/**
 * 6
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_NO_DATA_ALLOWED_ERR', 6);

/**
 * 7
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_NO_MODIFICATION_ALLOWED_ERR', 7);

/**
 * 8
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_NOT_FOUND_ERR', 8);

/**
 * 9
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_NOT_SUPPORTED_ERR', 9);

/**
 * 10
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INUSE_ATTRIBUTE_ERR', 10);

/**
 * 11
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INVALID_STATE_ERR', 11);

/**
 * 12
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_SYNTAX_ERR', 12);

/**
 * 13
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INVALID_MODIFICATION_ERR', 13);

/**
 * 14
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_NAMESPACE_ERR', 14);

/**
 * 15
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_INVALID_ACCESS_ERR', 15);

/**
 * 16
 * @link http://php.net/manual/en/dom.constants.php
 */
define ('DOM_VALIDATION_ERR', 16);

// End of dom v.20031129

// Start of date v.5.2.4

class DateTime  {
	const ATOM = "Y-m-d\TH:i:sP";
	const COOKIE = "l, d-M-y H:i:s T";
	const ISO8601 = "Y-m-d\TH:i:sO";
	const RFC822 = "D, d M y H:i:s O";
	const RFC850 = "l, d-M-y H:i:s T";
	const RFC1036 = "D, d M y H:i:s O";
	const RFC1123 = "D, d M Y H:i:s O";
	const RFC2822 = "D, d M Y H:i:s O";
	const RFC3339 = "Y-m-d\TH:i:sP";
	const RSS = "D, d M Y H:i:s O";
	const W3C = "Y-m-d\TH:i:sP";


	public function __construct () {}

	public function format () {}

	public function modify () {}

	public function getTimezone () {}

	public function setTimezone () {}

	public function getOffset () {}

	public function setTime () {}

	public function setDate () {}

	public function setISODate () {}

}

class DateTimeZone  {

	public function __construct () {}

	public function getName () {}

	public function getOffset () {}

	public function getTransitions () {}

	public static function listAbbreviations () {}

	public static function listIdentifiers () {}

}

/**
 * Parse about any English textual datetime description into a Unix timestamp
 * @link http://php.net/manual/en/function.strtotime.php
 * @param time string
 * @param now int[optional]
 * @return int a timestamp on success, false otherwise. Previous to PHP 5.1.0,
 */
function strtotime ($time, $now = null) {}

/**
 * Format a local time/date
 * @link http://php.net/manual/en/function.date.php
 * @param format string
 * @param timestamp int[optional]
 * @return string a formatted date string. If a non-numeric value is used for
 */
function date ($format, $timestamp = null) {}

/**
 * Format a local time/date as integer
 * @link http://php.net/manual/en/function.idate.php
 * @param format string
 * @param timestamp int[optional]
 * @return int an integer.
 */
function idate ($format, $timestamp = null) {}

/**
 * Format a GMT/UTC date/time
 * @link http://php.net/manual/en/function.gmdate.php
 * @param format string
 * @param timestamp int[optional]
 * @return string a formatted date string. If a non-numeric value is used for
 */
function gmdate ($format, $timestamp = null) {}

/**
 * Get Unix timestamp for a date
 * @link http://php.net/manual/en/function.mktime.php
 * @param hour int[optional]
 * @param minute int[optional]
 * @param second int[optional]
 * @param month int[optional]
 * @param day int[optional]
 * @param year int[optional]
 * @param is_dst int[optional]
 * @return int 
 */
function mktime ($hour = null, $minute = null, $second = null, $month = null, $day = null, $year = null, $is_dst = null) {}

/**
 * Get Unix timestamp for a GMT date
 * @link http://php.net/manual/en/function.gmmktime.php
 * @param hour int[optional]
 * @param minute int[optional]
 * @param second int[optional]
 * @param month int[optional]
 * @param day int[optional]
 * @param year int[optional]
 * @param is_dst int[optional]
 * @return int a integer Unix timestamp.
 */
function gmmktime ($hour = null, $minute = null, $second = null, $month = null, $day = null, $year = null, $is_dst = null) {}

/**
 * Validate a Gregorian date
 * @link http://php.net/manual/en/function.checkdate.php
 * @param month int
 * @param day int
 * @param year int
 * @return bool true if the date given is valid; otherwise returns false.
 */
function checkdate ($month, $day, $year) {}

/**
 * Format a local time/date according to locale settings
 * @link http://php.net/manual/en/function.strftime.php
 * @param format string
 * @param timestamp int[optional]
 * @return string 
 */
function strftime ($format, $timestamp = null) {}

/**
 * Format a GMT/UTC time/date according to locale settings
 * @link http://php.net/manual/en/function.gmstrftime.php
 * @param format string
 * @param timestamp int[optional]
 * @return string 
 */
function gmstrftime ($format, $timestamp = null) {}

/**
 * Return current Unix timestamp
 * @link http://php.net/manual/en/function.time.php
 * @return int 
 */
function time () {}

/**
 * Get the local time
 * @link http://php.net/manual/en/function.localtime.php
 * @param timestamp int[optional]
 * @param is_associative bool[optional]
 * @return array 
 */
function localtime ($timestamp = null, $is_associative = null) {}

/**
 * Get date/time information
 * @link http://php.net/manual/en/function.getdate.php
 * @param timestamp int[optional]
 * @return array an associative array of information related to
 */
function getdate ($timestamp = null) {}

/**
 * Returns new DateTime object
 * @link http://php.net/manual/en/function.date-create.php
 * @param time string[optional]
 * @param timezone DateTimeZone[optional]
 * @return DateTime DateTime object on success or false on failure.
 */
function date_create ($time = null, DateTimeZone $timezone = null) {}

/**
 * Returns associative array with detailed info about given date
 * @link http://php.net/manual/en/function.date-parse.php
 * @param date string
 * @return array array on success or false on failure.
 */
function date_parse ($date) {}

/**
 * Returns date formatted according to given format
 * @link http://php.net/manual/en/function.date-format.php
 * @param object DateTime
 * @param format string
 * @return string formatted date on success or false on failure.
 */
function date_format (DateTime $object, $format) {}

/**
 * Alters the timestamp
 * @link http://php.net/manual/en/function.date-modify.php
 * @param object DateTime
 * @param modify string
 * @return void &null; on success or false on failure.
 */
function date_modify (DateTime $object, $modify) {}

/**
 * Return time zone relative to given DateTime
 * @link http://php.net/manual/en/function.date-timezone-get.php
 * @param object DateTime
 * @return DateTimeZone DateTimeZone object on success or false on failure.
 */
function date_timezone_get (DateTime $object) {}

/**
 * Sets the time zone for the DateTime object
 * @link http://php.net/manual/en/function.date-timezone-set.php
 * @param object DateTime
 * @param timezone DateTimeZone
 * @return void &null; on success or false on failure.
 */
function date_timezone_set (DateTime $object, DateTimeZone $timezone) {}

/**
 * Returns the daylight saving time offset
 * @link http://php.net/manual/en/function.date-offset-get.php
 * @param object DateTime
 * @return int DST offset in seconds on success or false on failure.
 */
function date_offset_get (DateTime $object) {}

/**
 * Sets the time
 * @link http://php.net/manual/en/function.date-time-set.php
 * @param object DateTime
 * @param hour int
 * @param minute int
 * @param second int[optional]
 * @return void &null; on success or false on failure.
 */
function date_time_set (DateTime $object, $hour, $minute, $second = null) {}

/**
 * Sets the date
 * @link http://php.net/manual/en/function.date-date-set.php
 * @param object DateTime
 * @param year int
 * @param month int
 * @param day int
 * @return void &null; on success or false on failure.
 */
function date_date_set (DateTime $object, $year, $month, $day) {}

/**
 * Sets the ISO date
 * @link http://php.net/manual/en/function.date-isodate-set.php
 * @param object DateTime
 * @param year int
 * @param week int
 * @param day int[optional]
 * @return void &null; on success or false on failure.
 */
function date_isodate_set (DateTime $object, $year, $week, $day = null) {}

/**
 * Returns new DateTimeZone object
 * @link http://php.net/manual/en/function.timezone-open.php
 * @param timezone string
 * @return DateTimeZone DateTimeZone object on success or false on failure.
 */
function timezone_open ($timezone) {}

/**
 * Returns the name of the timezone
 * @link http://php.net/manual/en/function.timezone-name-get.php
 * @param object DateTimeZone
 * @return string time zone name on success or false on failure.
 */
function timezone_name_get (DateTimeZone $object) {}

/**
 * Returns the timezone name from abbrevation
 * @link http://php.net/manual/en/function.timezone-name-from-abbr.php
 * @param abbr string
 * @param gmtOffset int[optional]
 * @param isdst int[optional]
 * @return string time zone name on success or false on failure.
 */
function timezone_name_from_abbr ($abbr, $gmtOffset = null, $isdst = null) {}

/**
 * Returns the timezone offset from GMT
 * @link http://php.net/manual/en/function.timezone-offset-get.php
 * @param object DateTimeZone
 * @param datetime DateTime
 * @return int time zone offset in seconds on success or false on failure.
 */
function timezone_offset_get (DateTimeZone $object, DateTime $datetime) {}

/**
 * Returns all transitions for the timezone
 * @link http://php.net/manual/en/function.timezone-transitions-get.php
 * @param object DateTimeZone
 * @return array numerically indexed array containing associative array with all
 */
function timezone_transitions_get (DateTimeZone $object) {}

/**
 * Returns numerically index array with all timezone identifiers
 * @link http://php.net/manual/en/function.timezone-identifiers-list.php
 * @return array array on success or false on failure.
 */
function timezone_identifiers_list () {}

/**
 * Returns associative array containing dst, offset and the timezone name
 * @link http://php.net/manual/en/function.timezone-abbreviations-list.php
 * @return array array on success or false on failure.
 */
function timezone_abbreviations_list () {}

/**
 * Sets the default timezone used by all date/time functions in a script
 * @link http://php.net/manual/en/function.date-default-timezone-set.php
 * @param timezone_identifier string
 * @return bool 
 */
function date_default_timezone_set ($timezone_identifier) {}

/**
 * Gets the default timezone used by all date/time functions in a script
 * @link http://php.net/manual/en/function.date-default-timezone-get.php
 * @return string a string.
 */
function date_default_timezone_get () {}

/**
 * Returns time of sunrise for a given day and location
 * @link http://php.net/manual/en/function.date-sunrise.php
 * @param timestamp int
 * @param format int[optional]
 * @param latitude float[optional]
 * @param longitude float[optional]
 * @param zenith float[optional]
 * @param gmt_offset float[optional]
 * @return mixed the sunrise time in a specified format on
 */
function date_sunrise ($timestamp, $format = null, $latitude = null, $longitude = null, $zenith = null, $gmt_offset = null) {}

/**
 * Returns time of sunset for a given day and location
 * @link http://php.net/manual/en/function.date-sunset.php
 * @param timestamp int
 * @param format int[optional]
 * @param latitude float[optional]
 * @param longitude float[optional]
 * @param zenith float[optional]
 * @param gmt_offset float[optional]
 * @return mixed the sunset time in a specified format on
 */
function date_sunset ($timestamp, $format = null, $latitude = null, $longitude = null, $zenith = null, $gmt_offset = null) {}

/**
 * Returns an array with information about sunset/sunrise and twilight begin/end
 * @link http://php.net/manual/en/function.date-sun-info.php
 * @param time int
 * @param latitude float
 * @param longitude float
 * @return array array on success or false on failure.
 */
function date_sun_info ($time, $latitude, $longitude) {}


/**
 * Atom (example: 2005-08-15T15:52:01+00:00)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_ATOM', "Y-m-d\TH:i:sP");

/**
 * HTTP Cookies (example: Monday, 15-Aug-05 15:52:01 UTC)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_COOKIE', "l, d-M-y H:i:s T");

/**
 * ISO-8601 (example: 2005-08-15T15:52:01+0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_ISO8601', "Y-m-d\TH:i:sO");

/**
 * RFC 822 (example: Mon, 15 Aug 05 15:52:01 +0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC822', "D, d M y H:i:s O");

/**
 * RFC 850 (example: Monday, 15-Aug-05 15:52:01 UTC)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC850', "l, d-M-y H:i:s T");

/**
 * RFC 1036 (example: Mon, 15 Aug 05 15:52:01 +0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC1036', "D, d M y H:i:s O");

/**
 * RFC 1123 (example: Mon, 15 Aug 2005 15:52:01 +0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC1123', "D, d M Y H:i:s O");

/**
 * RFC 2822 (Mon, 15 Aug 2005 15:52:01 +0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC2822', "D, d M Y H:i:s O");

/**
 * Same as DATE_ATOM (since PHP 5.1.3)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RFC3339', "Y-m-d\TH:i:sP");

/**
 * RSS (Mon, 15 Aug 2005 15:52:01 +0000)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_RSS', "D, d M Y H:i:s O");

/**
 * World Wide Web Consortium (example: 2005-08-15T15:52:01+00:00)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('DATE_W3C', "Y-m-d\TH:i:sP");

/**
 * Timestamp
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('SUNFUNCS_RET_TIMESTAMP', 0);

/**
 * Hours:minutes (example: 08:02)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('SUNFUNCS_RET_STRING', 1);

/**
 * Hours as floating point number (example 8.75)
 * @link http://php.net/manual/en/datetime.constants.php
 */
define ('SUNFUNCS_RET_DOUBLE', 2);

// End of date v.5.2.4

// Start of ctype v.

/**
 * Check for alphanumeric character(s)
 * @link http://php.net/manual/en/function.ctype-alnum.php
 * @param text string
 * @return bool true if every character in text is either
 */
function ctype_alnum ($text) {}

/**
 * Check for alphabetic character(s)
 * @link http://php.net/manual/en/function.ctype-alpha.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_alpha ($text) {}

/**
 * Check for control character(s)
 * @link http://php.net/manual/en/function.ctype-cntrl.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_cntrl ($text) {}

/**
 * Check for numeric character(s)
 * @link http://php.net/manual/en/function.ctype-digit.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_digit ($text) {}

/**
 * Check for lowercase character(s)
 * @link http://php.net/manual/en/function.ctype-lower.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_lower ($text) {}

/**
 * Check for any printable character(s) except space
 * @link http://php.net/manual/en/function.ctype-graph.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_graph ($text) {}

/**
 * Check for printable character(s)
 * @link http://php.net/manual/en/function.ctype-print.php
 * @param text string
 * @return bool true if every character in text
 */
function ctype_print ($text) {}

/**
 * Check for any printable character which is not whitespace or an
   alphanumeric character
 * @link http://php.net/manual/en/function.ctype-punct.php
 * @param text string
 * @return bool true if every character in text
 */
function ctype_punct ($text) {}

/**
 * Check for whitespace character(s)
 * @link http://php.net/manual/en/function.ctype-space.php
 * @param text string
 * @return bool true if every character in text
 */
function ctype_space ($text) {}

/**
 * Check for uppercase character(s)
 * @link http://php.net/manual/en/function.ctype-upper.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_upper ($text) {}

/**
 * Check for character(s) representing a hexadecimal digit
 * @link http://php.net/manual/en/function.ctype-xdigit.php
 * @param text string
 * @return bool true if every character in text is
 */
function ctype_xdigit ($text) {}

// End of ctype v.

// Start of Zend Core v.2.5.0

function zend_core_version () {}

function zend_core_restart () {}

// End of Zend Core v.2.5.0

// Start of zlib v.1.1

/**
 * Output a gz-file
 * @link http://php.net/manual/en/function.readgzfile.php
 * @param filename string
 * @param use_include_path int[optional]
 * @return int the number of (uncompressed) bytes read from the file. If
 */
function readgzfile ($filename, $use_include_path = null) {}

/**
 * Rewind the position of a gz-file pointer
 * @link http://php.net/manual/en/function.gzrewind.php
 * @param zp resource
 * @return bool 
 */
function gzrewind ($zp) {}

/**
 * Close an open gz-file pointer
 * @link http://php.net/manual/en/function.gzclose.php
 * @param zp resource
 * @return bool 
 */
function gzclose ($zp) {}

/**
 * Test for end-of-file on a gz-file pointer
 * @link http://php.net/manual/en/function.gzeof.php
 * @param zp resource
 * @return int true if the gz-file pointer is at EOF or an error occurs;
 */
function gzeof ($zp) {}

/**
 * Get character from gz-file pointer
 * @link http://php.net/manual/en/function.gzgetc.php
 * @param zp resource
 * @return string 
 */
function gzgetc ($zp) {}

/**
 * Get line from file pointer
 * @link http://php.net/manual/en/function.gzgets.php
 * @param zp resource
 * @param length int
 * @return string 
 */
function gzgets ($zp, $length) {}

/**
 * Get line from gz-file pointer and strip HTML tags
 * @link http://php.net/manual/en/function.gzgetss.php
 * @param zp resource
 * @param length int
 * @param allowable_tags string[optional]
 * @return string 
 */
function gzgetss ($zp, $length, $allowable_tags = null) {}

/**
 * Binary-safe gz-file read
 * @link http://php.net/manual/en/function.gzread.php
 * @param zp resource
 * @param length int
 * @return string 
 */
function gzread ($zp, $length) {}

/**
 * Open gz-file
 * @link http://php.net/manual/en/function.gzopen.php
 * @param filename string
 * @param mode string
 * @param use_include_path int[optional]
 * @return resource a file pointer to the file opened, after that, everything you read
 */
function gzopen ($filename, $mode, $use_include_path = null) {}

/**
 * Output all remaining data on a gz-file pointer
 * @link http://php.net/manual/en/function.gzpassthru.php
 * @param zp resource
 * @return int 
 */
function gzpassthru ($zp) {}

/**
 * Seek on a gz-file pointer
 * @link http://php.net/manual/en/function.gzseek.php
 * @param zp resource
 * @param offset int
 * @return int 
 */
function gzseek ($zp, $offset) {}

/**
 * Tell gz-file pointer read/write position
 * @link http://php.net/manual/en/function.gztell.php
 * @param zp resource
 * @return int 
 */
function gztell ($zp) {}

/**
 * Binary-safe gz-file write
 * @link http://php.net/manual/en/function.gzwrite.php
 * @param zp resource
 * @param string string
 * @param length int[optional]
 * @return int the number of (uncompressed) bytes written to the given gz-file
 */
function gzwrite ($zp, $string, $length = null) {}

/**
 * &Alias; <function>gzwrite</function>
 * @link http://php.net/manual/en/function.gzputs.php
 */
function gzputs () {}

/**
 * Read entire gz-file into an array
 * @link http://php.net/manual/en/function.gzfile.php
 * @param filename string
 * @param use_include_path int[optional]
 * @return array 
 */
function gzfile ($filename, $use_include_path = null) {}

/**
 * Compress a string
 * @link http://php.net/manual/en/function.gzcompress.php
 * @param data string
 * @param level int[optional]
 * @return string 
 */
function gzcompress ($data, $level = null) {}

/**
 * Uncompress a compressed string
 * @link http://php.net/manual/en/function.gzuncompress.php
 * @param data string
 * @param length int[optional]
 * @return string 
 */
function gzuncompress ($data, $length = null) {}

/**
 * Deflate a string
 * @link http://php.net/manual/en/function.gzdeflate.php
 * @param data string
 * @param level int[optional]
 * @return string 
 */
function gzdeflate ($data, $level = null) {}

/**
 * Inflate a deflated string
 * @link http://php.net/manual/en/function.gzinflate.php
 * @param data string
 * @param length int[optional]
 * @return string 
 */
function gzinflate ($data, $length = null) {}

/**
 * Create a gzip compressed string
 * @link http://php.net/manual/en/function.gzencode.php
 * @param data string
 * @param level int[optional]
 * @param encoding_mode int[optional]
 * @return string 
 */
function gzencode ($data, $level = null, $encoding_mode = null) {}

/**
 * ob_start callback function to gzip output buffer
 * @link http://php.net/manual/en/function.ob-gzhandler.php
 * @param buffer string
 * @param mode int
 * @return string 
 */
function ob_gzhandler ($buffer, $mode) {}

/**
 * Returns the coding type used for output compression
 * @link http://php.net/manual/en/function.zlib-get-coding-type.php
 * @return string 
 */
function zlib_get_coding_type () {}

define ('FORCE_GZIP', 1);
define ('FORCE_DEFLATE', 2);

// End of zlib v.1.1

// Start of openssl v.

/**
 * Frees a private key
 * @link http://php.net/manual/en/function.openssl-pkey-free.php
 * @param key resource
 * @return void 
 */
function openssl_pkey_free ($key) {}

/**
 * Generates a new private key
 * @link http://php.net/manual/en/function.openssl-pkey-new.php
 * @param configargs array[optional]
 * @return resource a resource identifier for the pkey on success, or false on
 */
function openssl_pkey_new (array $configargs = null) {}

/**
 * Gets an exportable representation of a key into a string
 * @link http://php.net/manual/en/function.openssl-pkey-export.php
 * @param key mixed
 * @param out string
 * @param passphrase string[optional]
 * @param configargs array[optional]
 * @return bool 
 */
function openssl_pkey_export ($key, &$out, $passphrase = null, array $configargs = null) {}

/**
 * Gets an exportable representation of a key into a file
 * @link http://php.net/manual/en/function.openssl-pkey-export-to-file.php
 * @param key mixed
 * @param outfilename string
 * @param passphrase string[optional]
 * @param configargs array[optional]
 * @return bool 
 */
function openssl_pkey_export_to_file ($key, $outfilename, $passphrase = null, array $configargs = null) {}

/**
 * Get a private key
 * @link http://php.net/manual/en/function.openssl-pkey-get-private.php
 * @param key mixed
 * @param passphrase string[optional]
 * @return resource a positive key resource identifier on success, or false on error.
 */
function openssl_pkey_get_private ($key, $passphrase = null) {}

/**
 * Extract public key from certificate and prepare it for use
 * @link http://php.net/manual/en/function.openssl-pkey-get-public.php
 * @param certificate mixed
 * @return resource a positive key resource identifier on success, or false on error.
 */
function openssl_pkey_get_public ($certificate) {}

/**
 * Returns an array with the key details (bits, pkey, type)
 * @link http://php.net/manual/en/function.openssl-pkey-get-details.php
 * @param key resource
 * @return array 
 */
function openssl_pkey_get_details ($key) {}

/**
 * Free key resource
 * @link http://php.net/manual/en/function.openssl-free-key.php
 * @param key_identifier resource
 * @return void 
 */
function openssl_free_key ($key_identifier) {}

/**
 * &Alias; <function>openssl_pkey_get_private</function>
 * @link http://php.net/manual/en/function.openssl-get-privatekey.php
 */
function openssl_get_privatekey () {}

/**
 * &Alias; <function>openssl_pkey_get_public</function>
 * @link http://php.net/manual/en/function.openssl-get-publickey.php
 */
function openssl_get_publickey () {}

/**
 * Parse an X.509 certificate and return a resource identifier for
  it
 * @link http://php.net/manual/en/function.openssl-x509-read.php
 * @param x509certdata mixed
 * @return resource a resource identifier on success, or false on failure.
 */
function openssl_x509_read ($x509certdata) {}

/**
 * Free certificate resource
 * @link http://php.net/manual/en/function.openssl-x509-free.php
 * @param x509cert resource
 * @return void 
 */
function openssl_x509_free ($x509cert) {}

/**
 * Parse an X509 certificate and return the information as an array
 * @link http://php.net/manual/en/function.openssl-x509-parse.php
 * @param x509cert mixed
 * @param shortnames bool[optional]
 * @return array 
 */
function openssl_x509_parse ($x509cert, $shortnames = null) {}

/**
 * Verifies if a certificate can be used for a particular purpose
 * @link http://php.net/manual/en/function.openssl-x509-checkpurpose.php
 * @param x509cert mixed
 * @param purpose int
 * @param cainfo array[optional]
 * @param untrustedfile string[optional]
 * @return int true if the certificate can be used for the intended purpose,
 */
function openssl_x509_checkpurpose ($x509cert, $purpose, array $cainfo = null, $untrustedfile = null) {}

/**
 * Checks if a private key corresponds to a certificate
 * @link http://php.net/manual/en/function.openssl-x509-check-private-key.php
 * @param cert mixed
 * @param key mixed
 * @return bool true if key is the private key that
 */
function openssl_x509_check_private_key ($cert, $key) {}

/**
 * Exports a certificate as a string
 * @link http://php.net/manual/en/function.openssl-x509-export.php
 * @param x509 mixed
 * @param output string
 * @param notext bool[optional]
 * @return bool 
 */
function openssl_x509_export ($x509, &$output, $notext = null) {}

/**
 * Exports a certificate to file
 * @link http://php.net/manual/en/function.openssl-x509-export-to-file.php
 * @param x509 mixed
 * @param outfilename string
 * @param notext bool[optional]
 * @return bool 
 */
function openssl_x509_export_to_file ($x509, $outfilename, $notext = null) {}

/**
 * @param var1
 * @param var2
 */
function openssl_pkcs12_export ($var1, &$var2) {}

function openssl_pkcs12_export_to_file () {}

/**
 * @param var1
 * @param var2
 */
function openssl_pkcs12_read ($var1, &$var2) {}

/**
 * Generates a CSR
 * @link http://php.net/manual/en/function.openssl-csr-new.php
 * @param dn array
 * @param privkey resource
 * @param configargs array[optional]
 * @param extraattribs array[optional]
 * @return mixed the CSR.
 */
function openssl_csr_new (array $dn, &$privkey, array $configargs = null, array $extraattribs = null) {}

/**
 * Exports a CSR as a string
 * @link http://php.net/manual/en/function.openssl-csr-export.php
 * @param csr resource
 * @param out string
 * @param notext bool[optional]
 * @return bool 
 */
function openssl_csr_export ($csr, &$out, $notext = null) {}

/**
 * Exports a CSR to a file
 * @link http://php.net/manual/en/function.openssl-csr-export-to-file.php
 * @param csr resource
 * @param outfilename string
 * @param notext bool[optional]
 * @return bool 
 */
function openssl_csr_export_to_file ($csr, $outfilename, $notext = null) {}

/**
 * Sign a CSR with another certificate (or itself) and generate a certificate
 * @link http://php.net/manual/en/function.openssl-csr-sign.php
 * @param csr mixed
 * @param cacert mixed
 * @param priv_key mixed
 * @param days int
 * @param configargs array[optional]
 * @param serial int[optional]
 * @return resource an x509 certificate resource on success, false on failure.
 */
function openssl_csr_sign ($csr, $cacert, $priv_key, $days, array $configargs = null, $serial = null) {}

/**
 * Returns the subject of a CERT
 * @link http://php.net/manual/en/function.openssl-csr-get-subject.php
 * @param csr mixed
 * @param use_shortnames bool[optional]
 * @return array 
 */
function openssl_csr_get_subject ($csr, $use_shortnames = null) {}

/**
 * Returns the public key of a CERT
 * @link http://php.net/manual/en/function.openssl-csr-get-public-key.php
 * @param csr mixed
 * @param use_shortnames bool[optional]
 * @return resource 
 */
function openssl_csr_get_public_key ($csr, $use_shortnames = null) {}

/**
 * Generate signature
 * @link http://php.net/manual/en/function.openssl-sign.php
 * @param data string
 * @param signature string
 * @param priv_key_id mixed
 * @param signature_alg int[optional]
 * @return bool 
 */
function openssl_sign ($data, &$signature, $priv_key_id, $signature_alg = null) {}

/**
 * Verify signature
 * @link http://php.net/manual/en/function.openssl-verify.php
 * @param data string
 * @param signature string
 * @param pub_key_id mixed
 * @param signature_alg int[optional]
 * @return int 1 if the signature is correct, 0 if it is incorrect, and
 */
function openssl_verify ($data, $signature, $pub_key_id, $signature_alg = null) {}

/**
 * Seal (encrypt) data
 * @link http://php.net/manual/en/function.openssl-seal.php
 * @param data string
 * @param sealed_data string
 * @param env_keys array
 * @param pub_key_ids array
 * @return int the length of the sealed data on success, or false on error.
 */
function openssl_seal ($data, &$sealed_data, array &$env_keys, array $pub_key_ids) {}

/**
 * Open sealed data
 * @link http://php.net/manual/en/function.openssl-open.php
 * @param sealed_data string
 * @param open_data string
 * @param env_key string
 * @param priv_key_id mixed
 * @return bool 
 */
function openssl_open ($sealed_data, &$open_data, $env_key, $priv_key_id) {}

/**
 * Verifies the signature of an S/MIME signed message
 * @link http://php.net/manual/en/function.openssl-pkcs7-verify.php
 * @param filename string
 * @param flags int
 * @param outfilename string[optional]
 * @param cainfo array[optional]
 * @param extracerts string[optional]
 * @param content string[optional]
 * @return mixed true if the signature is verified, false if it is not correct
 */
function openssl_pkcs7_verify ($filename, $flags, $outfilename = null, array $cainfo = null, $extracerts = null, $content = null) {}

/**
 * Decrypts an S/MIME encrypted message
 * @link http://php.net/manual/en/function.openssl-pkcs7-decrypt.php
 * @param infilename string
 * @param outfilename string
 * @param recipcert mixed
 * @param recipkey mixed[optional]
 * @return bool 
 */
function openssl_pkcs7_decrypt ($infilename, $outfilename, $recipcert, $recipkey = null) {}

/**
 * Sign an S/MIME message
 * @link http://php.net/manual/en/function.openssl-pkcs7-sign.php
 * @param infilename string
 * @param outfilename string
 * @param signcert mixed
 * @param privkey mixed
 * @param headers array
 * @param flags int[optional]
 * @param extracerts string[optional]
 * @return bool 
 */
function openssl_pkcs7_sign ($infilename, $outfilename, $signcert, $privkey, array $headers, $flags = null, $extracerts = null) {}

/**
 * Encrypt an S/MIME message
 * @link http://php.net/manual/en/function.openssl-pkcs7-encrypt.php
 * @param infile string
 * @param outfile string
 * @param recipcerts mixed
 * @param headers array
 * @param flags int[optional]
 * @param cipherid int[optional]
 * @return bool 
 */
function openssl_pkcs7_encrypt ($infile, $outfile, $recipcerts, array $headers, $flags = null, $cipherid = null) {}

/**
 * Encrypts data with private key
 * @link http://php.net/manual/en/function.openssl-private-encrypt.php
 * @param data string
 * @param crypted string
 * @param key mixed
 * @param padding int[optional]
 * @return bool 
 */
function openssl_private_encrypt ($data, &$crypted, $key, $padding = null) {}

/**
 * Decrypts data with private key
 * @link http://php.net/manual/en/function.openssl-private-decrypt.php
 * @param data string
 * @param decrypted string
 * @param key mixed
 * @param padding int[optional]
 * @return bool 
 */
function openssl_private_decrypt ($data, &$decrypted, $key, $padding = null) {}

/**
 * Encrypts data with public key
 * @link http://php.net/manual/en/function.openssl-public-encrypt.php
 * @param data string
 * @param crypted string
 * @param key mixed
 * @param padding int[optional]
 * @return bool 
 */
function openssl_public_encrypt ($data, &$crypted, $key, $padding = null) {}

/**
 * Decrypts data with public key
 * @link http://php.net/manual/en/function.openssl-public-decrypt.php
 * @param data string
 * @param decrypted string
 * @param key mixed
 * @param padding int[optional]
 * @return bool 
 */
function openssl_public_decrypt ($data, &$decrypted, $key, $padding = null) {}

/**
 * Return openSSL error message
 * @link http://php.net/manual/en/function.openssl-error-string.php
 * @return string an error message string, or false if there are no more error
 */
function openssl_error_string () {}

define ('OPENSSL_VERSION_TEXT', "OpenSSL 0.9.8d 28 Sep 2006");
define ('OPENSSL_VERSION_NUMBER', 9470031);
define ('X509_PURPOSE_SSL_CLIENT', 1);
define ('X509_PURPOSE_SSL_SERVER', 2);
define ('X509_PURPOSE_NS_SSL_SERVER', 3);
define ('X509_PURPOSE_SMIME_SIGN', 4);
define ('X509_PURPOSE_SMIME_ENCRYPT', 5);
define ('X509_PURPOSE_CRL_SIGN', 6);
define ('X509_PURPOSE_ANY', 7);

/**
 * Used as default algorithm by openssl_sign and
 * openssl_verify.
 * @link http://php.net/manual/en/openssl.constants.php
 */
define ('OPENSSL_ALGO_SHA1', 1);
define ('OPENSSL_ALGO_MD5', 2);
define ('OPENSSL_ALGO_MD4', 3);
define ('OPENSSL_ALGO_MD2', 4);
define ('PKCS7_DETACHED', 64);
define ('PKCS7_TEXT', 1);
define ('PKCS7_NOINTERN', 16);
define ('PKCS7_NOVERIFY', 32);
define ('PKCS7_NOCHAIN', 8);
define ('PKCS7_NOCERTS', 2);
define ('PKCS7_NOATTR', 256);
define ('PKCS7_BINARY', 128);
define ('PKCS7_NOSIGS', 4);
define ('OPENSSL_PKCS1_PADDING', 1);
define ('OPENSSL_SSLV23_PADDING', 2);
define ('OPENSSL_NO_PADDING', 3);
define ('OPENSSL_PKCS1_OAEP_PADDING', 4);
define ('OPENSSL_CIPHER_RC2_40', 0);
define ('OPENSSL_CIPHER_RC2_128', 1);
define ('OPENSSL_CIPHER_RC2_64', 2);
define ('OPENSSL_CIPHER_DES', 3);
define ('OPENSSL_CIPHER_3DES', 4);
define ('OPENSSL_KEYTYPE_RSA', 0);
define ('OPENSSL_KEYTYPE_DSA', 1);
define ('OPENSSL_KEYTYPE_DH', 2);
define ('OPENSSL_KEYTYPE_EC', 3);

// End of openssl v.

// Start of bcmath v.

/**
 * Add two arbitrary precision numbers
 * @link http://php.net/manual/en/function.bcadd.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return string 
 */
function bcadd ($left_operand, $right_operand, $scale = null) {}

/**
 * Subtract one arbitrary precision number from another
 * @link http://php.net/manual/en/function.bcsub.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return string 
 */
function bcsub ($left_operand, $right_operand, $scale = null) {}

/**
 * Multiply two arbitrary precision number
 * @link http://php.net/manual/en/function.bcmul.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return string the result as a string.
 */
function bcmul ($left_operand, $right_operand, $scale = null) {}

/**
 * Divide two arbitrary precision numbers
 * @link http://php.net/manual/en/function.bcdiv.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return string the result of the division as a string, or &null; if
 */
function bcdiv ($left_operand, $right_operand, $scale = null) {}

/**
 * Get modulus of an arbitrary precision number
 * @link http://php.net/manual/en/function.bcmod.php
 * @param left_operand string
 * @param modulus string
 * @return string the modulus as a string, or &null; if
 */
function bcmod ($left_operand, $modulus) {}

/**
 * Raise an arbitrary precision number to another
 * @link http://php.net/manual/en/function.bcpow.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return string the result as a string.
 */
function bcpow ($left_operand, $right_operand, $scale = null) {}

/**
 * Get the square root of an arbitrary precision number
 * @link http://php.net/manual/en/function.bcsqrt.php
 * @param operand string
 * @param scale int[optional]
 * @return string the square root as a string, or &null; if
 */
function bcsqrt ($operand, $scale = null) {}

/**
 * Set default scale parameter for all bc math functions
 * @link http://php.net/manual/en/function.bcscale.php
 * @param scale int
 * @return bool 
 */
function bcscale ($scale) {}

/**
 * Compare two arbitrary precision numbers
 * @link http://php.net/manual/en/function.bccomp.php
 * @param left_operand string
 * @param right_operand string
 * @param scale int[optional]
 * @return int 0 if the two operands are equal, 1 if the
 */
function bccomp ($left_operand, $right_operand, $scale = null) {}

/**
 * Raise an arbitrary precision number to another, reduced by a specified modulus
 * @link http://php.net/manual/en/function.bcpowmod.php
 * @param left_operand string
 * @param right_operand string
 * @param modulus string
 * @param scale int[optional]
 * @return string the result as a string, or &null; if modulus
 */
function bcpowmod ($left_operand, $right_operand, $modulus, $scale = null) {}

// End of bcmath v.

// Start of curl v.

/**
 * Initialize a cURL session
 * @link http://php.net/manual/en/function.curl-init.php
 * @param url string[optional]
 * @return resource a cURL handle on success, false on errors.
 */
function curl_init ($url = null) {}

/**
 * Copy a cURL handle along with all of its preferences
 * @link http://php.net/manual/en/function.curl-copy-handle.php
 * @param ch resource
 * @return resource a new cURL handle.
 */
function curl_copy_handle ($ch) {}

/**
 * Gets cURL version information
 * @link http://php.net/manual/en/function.curl-version.php
 * @param age int[optional]
 * @return array an associative array with the following elements:
 */
function curl_version ($age = null) {}

/**
 * Set an option for a cURL transfer
 * @link http://php.net/manual/en/function.curl-setopt.php
 * @param ch resource
 * @param option int
 * @param value mixed
 * @return bool 
 */
function curl_setopt ($ch, $option, $value) {}

/**
 * Set multiple options for a cURL transfer
 * @link http://php.net/manual/en/function.curl-setopt-array.php
 * @param ch resource
 * @param options array
 * @return bool true if all options were successfully set. If an option could
 */
function curl_setopt_array ($ch, array $options) {}

/**
 * Perform a cURL session
 * @link http://php.net/manual/en/function.curl-exec.php
 * @param ch resource
 * @return mixed 
 */
function curl_exec ($ch) {}

/**
 * Get information regarding a specific transfer
 * @link http://php.net/manual/en/function.curl-getinfo.php
 * @param ch resource
 * @param opt int[optional]
 * @return mixed 
 */
function curl_getinfo ($ch, $opt = null) {}

/**
 * Return a string containing the last error for the current session
 * @link http://php.net/manual/en/function.curl-error.php
 * @param ch resource
 * @return string the error number or '' (the empty string) if no
 */
function curl_error ($ch) {}

/**
 * Return the last error number
 * @link http://php.net/manual/en/function.curl-errno.php
 * @param ch resource
 * @return int the error number or 0 (zero) if no error
 */
function curl_errno ($ch) {}

/**
 * Close a cURL session
 * @link http://php.net/manual/en/function.curl-close.php
 * @param ch resource
 * @return void 
 */
function curl_close ($ch) {}

/**
 * Returns a new cURL multi handle
 * @link http://php.net/manual/en/function.curl-multi-init.php
 * @return resource a cURL on handle on success, false on failure.
 */
function curl_multi_init () {}

/**
 * Add a normal cURL handle to a cURL multi handle
 * @link http://php.net/manual/en/function.curl-multi-add-handle.php
 * @param mh resource
 * @param ch resource
 * @return int 0 on success, or one of the CURLM_XXX errors
 */
function curl_multi_add_handle ($mh, $ch) {}

/**
 * Remove a multi handle from a set of cURL handles
 * @link http://php.net/manual/en/function.curl-multi-remove-handle.php
 * @param mh resource
 * @param ch resource
 * @return int 
 */
function curl_multi_remove_handle ($mh, $ch) {}

/**
 * Get all the sockets associated with the cURL extension, which can then be "selected"
 * @link http://php.net/manual/en/function.curl-multi-select.php
 * @param mh resource
 * @param timeout float[optional]
 * @return int 
 */
function curl_multi_select ($mh, $timeout = null) {}

/**
 * Run the sub-connections of the current cURL handle
 * @link http://php.net/manual/en/function.curl-multi-exec.php
 * @param mh resource
 * @param still_running int
 * @return int 
 */
function curl_multi_exec ($mh, &$still_running) {}

/**
 * Return the content of a cURL handle if <constant>CURLOPT_RETURNTRANSFER</constant> is set
 * @link http://php.net/manual/en/function.curl-multi-getcontent.php
 * @param ch resource
 * @return string 
 */
function curl_multi_getcontent ($ch) {}

/**
 * Get information about the current transfers
 * @link http://php.net/manual/en/function.curl-multi-info-read.php
 * @param mh resource
 * @param msgs_in_queue int[optional]
 * @return array 
 */
function curl_multi_info_read ($mh, $msgs_in_queue = null) {}

/**
 * Close a set of cURL handles
 * @link http://php.net/manual/en/function.curl-multi-close.php
 * @param mh resource
 * @return void 
 */
function curl_multi_close ($mh) {}

define ('CURLOPT_DNS_USE_GLOBAL_CACHE', 91);
define ('CURLOPT_DNS_CACHE_TIMEOUT', 92);
define ('CURLOPT_PORT', 3);
define ('CURLOPT_FILE', 10001);
define ('CURLOPT_READDATA', 10009);
define ('CURLOPT_INFILE', 10009);
define ('CURLOPT_INFILESIZE', 14);
define ('CURLOPT_URL', 10002);
define ('CURLOPT_PROXY', 10004);
define ('CURLOPT_VERBOSE', 41);
define ('CURLOPT_HEADER', 42);
define ('CURLOPT_HTTPHEADER', 10023);
define ('CURLOPT_NOPROGRESS', 43);
define ('CURLOPT_NOBODY', 44);
define ('CURLOPT_FAILONERROR', 45);
define ('CURLOPT_UPLOAD', 46);
define ('CURLOPT_POST', 47);
define ('CURLOPT_FTPLISTONLY', 48);
define ('CURLOPT_FTPAPPEND', 50);
define ('CURLOPT_NETRC', 51);

/**
 * This constant is not available when opendbase_dir or safe_mode are
 * enabled.
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_FOLLOWLOCATION', 52);
define ('CURLOPT_PUT', 54);
define ('CURLOPT_USERPWD', 10005);
define ('CURLOPT_PROXYUSERPWD', 10006);
define ('CURLOPT_RANGE', 10007);
define ('CURLOPT_TIMEOUT', 13);
define ('CURLOPT_POSTFIELDS', 10015);
define ('CURLOPT_REFERER', 10016);
define ('CURLOPT_USERAGENT', 10018);
define ('CURLOPT_FTPPORT', 10017);
define ('CURLOPT_FTP_USE_EPSV', 85);
define ('CURLOPT_LOW_SPEED_LIMIT', 19);
define ('CURLOPT_LOW_SPEED_TIME', 20);
define ('CURLOPT_RESUME_FROM', 21);
define ('CURLOPT_COOKIE', 10022);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_COOKIESESSION', 96);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_AUTOREFERER', 58);
define ('CURLOPT_SSLCERT', 10025);
define ('CURLOPT_SSLCERTPASSWD', 10026);
define ('CURLOPT_WRITEHEADER', 10029);
define ('CURLOPT_SSL_VERIFYHOST', 81);
define ('CURLOPT_COOKIEFILE', 10031);
define ('CURLOPT_SSLVERSION', 32);
define ('CURLOPT_TIMECONDITION', 33);
define ('CURLOPT_TIMEVALUE', 34);
define ('CURLOPT_CUSTOMREQUEST', 10036);
define ('CURLOPT_STDERR', 10037);
define ('CURLOPT_TRANSFERTEXT', 53);
define ('CURLOPT_RETURNTRANSFER', 19913);
define ('CURLOPT_QUOTE', 10028);
define ('CURLOPT_POSTQUOTE', 10039);
define ('CURLOPT_INTERFACE', 10062);
define ('CURLOPT_KRB4LEVEL', 10063);
define ('CURLOPT_HTTPPROXYTUNNEL', 61);
define ('CURLOPT_FILETIME', 69);
define ('CURLOPT_WRITEFUNCTION', 20011);
define ('CURLOPT_READFUNCTION', 20012);
define ('CURLOPT_HEADERFUNCTION', 20079);
define ('CURLOPT_MAXREDIRS', 68);
define ('CURLOPT_MAXCONNECTS', 71);
define ('CURLOPT_CLOSEPOLICY', 72);
define ('CURLOPT_FRESH_CONNECT', 74);
define ('CURLOPT_FORBID_REUSE', 75);
define ('CURLOPT_RANDOM_FILE', 10076);
define ('CURLOPT_EGDSOCKET', 10077);
define ('CURLOPT_CONNECTTIMEOUT', 78);
define ('CURLOPT_SSL_VERIFYPEER', 64);
define ('CURLOPT_CAINFO', 10065);
define ('CURLOPT_CAPATH', 10097);
define ('CURLOPT_COOKIEJAR', 10082);
define ('CURLOPT_SSL_CIPHER_LIST', 10083);
define ('CURLOPT_BINARYTRANSFER', 19914);
define ('CURLOPT_NOSIGNAL', 99);
define ('CURLOPT_PROXYTYPE', 101);
define ('CURLOPT_BUFFERSIZE', 98);
define ('CURLOPT_HTTPGET', 80);
define ('CURLOPT_HTTP_VERSION', 84);
define ('CURLOPT_SSLKEY', 10087);
define ('CURLOPT_SSLKEYTYPE', 10088);
define ('CURLOPT_SSLKEYPASSWD', 10026);
define ('CURLOPT_SSLENGINE', 10089);
define ('CURLOPT_SSLENGINE_DEFAULT', 90);
define ('CURLOPT_SSLCERTTYPE', 10086);
define ('CURLOPT_CRLF', 27);
define ('CURLOPT_ENCODING', 10102);
define ('CURLOPT_PROXYPORT', 59);
define ('CURLOPT_UNRESTRICTED_AUTH', 105);
define ('CURLOPT_FTP_USE_EPRT', 106);

/**
 * Available since PHP 5.2.1
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_TCP_NODELAY', 121);
define ('CURLOPT_HTTP200ALIASES', 10104);
define ('CURL_TIMECOND_IFMODSINCE', 1);
define ('CURL_TIMECOND_IFUNMODSINCE', 2);
define ('CURL_TIMECOND_LASTMOD', 3);
define ('CURLOPT_HTTPAUTH', 107);
define ('CURLAUTH_BASIC', 1);
define ('CURLAUTH_DIGEST', 2);
define ('CURLAUTH_GSSNEGOTIATE', 4);
define ('CURLAUTH_NTLM', 8);
define ('CURLAUTH_ANY', -1);
define ('CURLAUTH_ANYSAFE', -2);
define ('CURLOPT_PROXYAUTH', 111);
define ('CURLOPT_FTP_CREATE_MISSING_DIRS', 110);

/**
 * Available since PHP 5.2.4
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_PRIVATE', 10103);
define ('CURLCLOSEPOLICY_LEAST_RECENTLY_USED', 2);
define ('CURLCLOSEPOLICY_LEAST_TRAFFIC', 3);
define ('CURLCLOSEPOLICY_SLOWEST', 4);
define ('CURLCLOSEPOLICY_CALLBACK', 5);
define ('CURLCLOSEPOLICY_OLDEST', 1);
define ('CURLINFO_EFFECTIVE_URL', 1048577);
define ('CURLINFO_HTTP_CODE', 2097154);
define ('CURLINFO_HEADER_SIZE', 2097163);
define ('CURLINFO_REQUEST_SIZE', 2097164);
define ('CURLINFO_TOTAL_TIME', 3145731);
define ('CURLINFO_NAMELOOKUP_TIME', 3145732);
define ('CURLINFO_CONNECT_TIME', 3145733);
define ('CURLINFO_PRETRANSFER_TIME', 3145734);
define ('CURLINFO_SIZE_UPLOAD', 3145735);
define ('CURLINFO_SIZE_DOWNLOAD', 3145736);
define ('CURLINFO_SPEED_DOWNLOAD', 3145737);
define ('CURLINFO_SPEED_UPLOAD', 3145738);
define ('CURLINFO_FILETIME', 2097166);
define ('CURLINFO_SSL_VERIFYRESULT', 2097165);
define ('CURLINFO_CONTENT_LENGTH_DOWNLOAD', 3145743);
define ('CURLINFO_CONTENT_LENGTH_UPLOAD', 3145744);
define ('CURLINFO_STARTTRANSFER_TIME', 3145745);
define ('CURLINFO_CONTENT_TYPE', 1048594);
define ('CURLINFO_REDIRECT_TIME', 3145747);
define ('CURLINFO_REDIRECT_COUNT', 2097172);

/**
 * Available since PHP 5.1.3
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLINFO_HEADER_OUT', 2);

/**
 * Available since PHP 5.2.4
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLINFO_PRIVATE', 1048597);
define ('CURL_VERSION_IPV6', 1);
define ('CURL_VERSION_KERBEROS4', 2);
define ('CURL_VERSION_SSL', 4);
define ('CURL_VERSION_LIBZ', 8);
define ('CURLVERSION_NOW', 2);
define ('CURLE_OK', 0);
define ('CURLE_UNSUPPORTED_PROTOCOL', 1);
define ('CURLE_FAILED_INIT', 2);
define ('CURLE_URL_MALFORMAT', 3);
define ('CURLE_URL_MALFORMAT_USER', 4);
define ('CURLE_COULDNT_RESOLVE_PROXY', 5);
define ('CURLE_COULDNT_RESOLVE_HOST', 6);
define ('CURLE_COULDNT_CONNECT', 7);
define ('CURLE_FTP_WEIRD_SERVER_REPLY', 8);
define ('CURLE_FTP_ACCESS_DENIED', 9);
define ('CURLE_FTP_USER_PASSWORD_INCORRECT', 10);
define ('CURLE_FTP_WEIRD_PASS_REPLY', 11);
define ('CURLE_FTP_WEIRD_USER_REPLY', 12);
define ('CURLE_FTP_WEIRD_PASV_REPLY', 13);
define ('CURLE_FTP_WEIRD_227_FORMAT', 14);
define ('CURLE_FTP_CANT_GET_HOST', 15);
define ('CURLE_FTP_CANT_RECONNECT', 16);
define ('CURLE_FTP_COULDNT_SET_BINARY', 17);
define ('CURLE_PARTIAL_FILE', 18);
define ('CURLE_FTP_COULDNT_RETR_FILE', 19);
define ('CURLE_FTP_WRITE_ERROR', 20);
define ('CURLE_FTP_QUOTE_ERROR', 21);
define ('CURLE_HTTP_NOT_FOUND', 22);
define ('CURLE_WRITE_ERROR', 23);
define ('CURLE_MALFORMAT_USER', 24);
define ('CURLE_FTP_COULDNT_STOR_FILE', 25);
define ('CURLE_READ_ERROR', 26);
define ('CURLE_OUT_OF_MEMORY', 27);
define ('CURLE_OPERATION_TIMEOUTED', 28);
define ('CURLE_FTP_COULDNT_SET_ASCII', 29);
define ('CURLE_FTP_PORT_FAILED', 30);
define ('CURLE_FTP_COULDNT_USE_REST', 31);
define ('CURLE_FTP_COULDNT_GET_SIZE', 32);
define ('CURLE_HTTP_RANGE_ERROR', 33);
define ('CURLE_HTTP_POST_ERROR', 34);
define ('CURLE_SSL_CONNECT_ERROR', 35);
define ('CURLE_FTP_BAD_DOWNLOAD_RESUME', 36);
define ('CURLE_FILE_COULDNT_READ_FILE', 37);
define ('CURLE_LDAP_CANNOT_BIND', 38);
define ('CURLE_LDAP_SEARCH_FAILED', 39);
define ('CURLE_LIBRARY_NOT_FOUND', 40);
define ('CURLE_FUNCTION_NOT_FOUND', 41);
define ('CURLE_ABORTED_BY_CALLBACK', 42);
define ('CURLE_BAD_FUNCTION_ARGUMENT', 43);
define ('CURLE_BAD_CALLING_ORDER', 44);
define ('CURLE_HTTP_PORT_FAILED', 45);
define ('CURLE_BAD_PASSWORD_ENTERED', 46);
define ('CURLE_TOO_MANY_REDIRECTS', 47);
define ('CURLE_UNKNOWN_TELNET_OPTION', 48);
define ('CURLE_TELNET_OPTION_SYNTAX', 49);
define ('CURLE_OBSOLETE', 50);
define ('CURLE_SSL_PEER_CERTIFICATE', 51);
define ('CURLE_GOT_NOTHING', 52);
define ('CURLE_SSL_ENGINE_NOTFOUND', 53);
define ('CURLE_SSL_ENGINE_SETFAILED', 54);
define ('CURLE_SEND_ERROR', 55);
define ('CURLE_RECV_ERROR', 56);
define ('CURLE_SHARE_IN_USE', 57);
define ('CURLE_SSL_CERTPROBLEM', 58);
define ('CURLE_SSL_CIPHER', 59);
define ('CURLE_SSL_CACERT', 60);
define ('CURLE_BAD_CONTENT_ENCODING', 61);
define ('CURLE_LDAP_INVALID_URL', 62);
define ('CURLE_FILESIZE_EXCEEDED', 63);
define ('CURLE_FTP_SSL_FAILED', 64);
define ('CURLPROXY_HTTP', 0);
define ('CURLPROXY_SOCKS5', 5);
define ('CURL_NETRC_OPTIONAL', 1);
define ('CURL_NETRC_IGNORED', 0);
define ('CURL_NETRC_REQUIRED', 2);
define ('CURL_HTTP_VERSION_NONE', 0);
define ('CURL_HTTP_VERSION_1_0', 1);
define ('CURL_HTTP_VERSION_1_1', 2);
define ('CURLM_CALL_MULTI_PERFORM', -1);
define ('CURLM_OK', 0);
define ('CURLM_BAD_HANDLE', 1);
define ('CURLM_BAD_EASY_HANDLE', 2);
define ('CURLM_OUT_OF_MEMORY', 3);
define ('CURLM_INTERNAL_ERROR', 4);
define ('CURLMSG_DONE', 1);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_FTPSSLAUTH', 129);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPAUTH_DEFAULT', 0);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPAUTH_SSL', 1);

/**
 * Available since PHP 5.1.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPAUTH_TLS', 2);

/**
 * Available since PHP 5.2.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLOPT_FTP_SSL', 119);

/**
 * Available since PHP 5.2.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPSSL_NONE', 0);

/**
 * Available since PHP 5.2.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPSSL_TRY', 1);

/**
 * Available since PHP 5.2.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPSSL_CONTROL', 2);

/**
 * Available since PHP 5.2.0
 * @link http://php.net/manual/en/curl.constants.php
 */
define ('CURLFTPSSL_ALL', 3);

// End of curl v.

// Start of ftp v.

/**
 * Opens an FTP connection
 * @link http://php.net/manual/en/function.ftp-connect.php
 * @param host string
 * @param port int[optional]
 * @param timeout int[optional]
 * @return resource a FTP stream on success or false on error.
 */
function ftp_connect ($host, $port = null, $timeout = null) {}

/**
 * Opens an Secure SSL-FTP connection
 * @link http://php.net/manual/en/function.ftp-ssl-connect.php
 * @param host string
 * @param port int[optional]
 * @param timeout int[optional]
 * @return resource a SSL-FTP stream on success or false on error.
 */
function ftp_ssl_connect ($host, $port = null, $timeout = null) {}

/**
 * Logs in to an FTP connection
 * @link http://php.net/manual/en/function.ftp-login.php
 * @param ftp_stream resource
 * @param username string
 * @param password string
 * @return bool 
 */
function ftp_login ($ftp_stream, $username, $password) {}

/**
 * Returns the current directory name
 * @link http://php.net/manual/en/function.ftp-pwd.php
 * @param ftp_stream resource
 * @return string the current directory name or false on error.
 */
function ftp_pwd ($ftp_stream) {}

/**
 * Changes to the parent directory
 * @link http://php.net/manual/en/function.ftp-cdup.php
 * @param ftp_stream resource
 * @return bool 
 */
function ftp_cdup ($ftp_stream) {}

/**
 * Changes the current directory on a FTP server
 * @link http://php.net/manual/en/function.ftp-chdir.php
 * @param ftp_stream resource
 * @param directory string
 * @return bool 
 */
function ftp_chdir ($ftp_stream, $directory) {}

/**
 * Requests execution of a command on the FTP server
 * @link http://php.net/manual/en/function.ftp-exec.php
 * @param ftp_stream resource
 * @param command string
 * @return bool true if the command was successful (server sent response code:
 */
function ftp_exec ($ftp_stream, $command) {}

/**
 * Sends an arbitrary command to an FTP server
 * @link http://php.net/manual/en/function.ftp-raw.php
 * @param ftp_stream resource
 * @param command string
 * @return array the server's response as an array of strings.
 */
function ftp_raw ($ftp_stream, $command) {}

/**
 * Creates a directory
 * @link http://php.net/manual/en/function.ftp-mkdir.php
 * @param ftp_stream resource
 * @param directory string
 * @return string the newly created directory name on success or false on error.
 */
function ftp_mkdir ($ftp_stream, $directory) {}

/**
 * Removes a directory
 * @link http://php.net/manual/en/function.ftp-rmdir.php
 * @param ftp_stream resource
 * @param directory string
 * @return bool 
 */
function ftp_rmdir ($ftp_stream, $directory) {}

/**
 * Set permissions on a file via FTP
 * @link http://php.net/manual/en/function.ftp-chmod.php
 * @param ftp_stream resource
 * @param mode int
 * @param filename string
 * @return int the new file permissions on success or false on error.
 */
function ftp_chmod ($ftp_stream, $mode, $filename) {}

/**
 * Allocates space for a file to be uploaded
 * @link http://php.net/manual/en/function.ftp-alloc.php
 * @param ftp_stream resource
 * @param filesize int
 * @param result string[optional]
 * @return bool 
 */
function ftp_alloc ($ftp_stream, $filesize, &$result = null) {}

/**
 * Returns a list of files in the given directory
 * @link http://php.net/manual/en/function.ftp-nlist.php
 * @param ftp_stream resource
 * @param directory string
 * @return array an array of filenames from the specified directory on success or
 */
function ftp_nlist ($ftp_stream, $directory) {}

/**
 * Returns a detailed list of files in the given directory
 * @link http://php.net/manual/en/function.ftp-rawlist.php
 * @param ftp_stream resource
 * @param directory string
 * @param recursive bool[optional]
 * @return array an array where each element corresponds to one line of text.
 */
function ftp_rawlist ($ftp_stream, $directory, $recursive = null) {}

/**
 * Returns the system type identifier of the remote FTP server
 * @link http://php.net/manual/en/function.ftp-systype.php
 * @param ftp_stream resource
 * @return string the remote system type, or false on error.
 */
function ftp_systype ($ftp_stream) {}

/**
 * Turns passive mode on or off
 * @link http://php.net/manual/en/function.ftp-pasv.php
 * @param ftp_stream resource
 * @param pasv bool
 * @return bool 
 */
function ftp_pasv ($ftp_stream, $pasv) {}

/**
 * Downloads a file from the FTP server
 * @link http://php.net/manual/en/function.ftp-get.php
 * @param ftp_stream resource
 * @param local_file string
 * @param remote_file string
 * @param mode int
 * @param resumepos int[optional]
 * @return bool 
 */
function ftp_get ($ftp_stream, $local_file, $remote_file, $mode, $resumepos = null) {}

/**
 * Downloads a file from the FTP server and saves to an open file
 * @link http://php.net/manual/en/function.ftp-fget.php
 * @param ftp_stream resource
 * @param handle resource
 * @param remote_file string
 * @param mode int
 * @param resumepos int[optional]
 * @return bool 
 */
function ftp_fget ($ftp_stream, $handle, $remote_file, $mode, $resumepos = null) {}

/**
 * Uploads a file to the FTP server
 * @link http://php.net/manual/en/function.ftp-put.php
 * @param ftp_stream resource
 * @param remote_file string
 * @param local_file string
 * @param mode int
 * @param startpos int[optional]
 * @return bool 
 */
function ftp_put ($ftp_stream, $remote_file, $local_file, $mode, $startpos = null) {}

/**
 * Uploads from an open file to the FTP server
 * @link http://php.net/manual/en/function.ftp-fput.php
 * @param ftp_stream resource
 * @param remote_file string
 * @param handle resource
 * @param mode int
 * @param startpos int[optional]
 * @return bool 
 */
function ftp_fput ($ftp_stream, $remote_file, $handle, $mode, $startpos = null) {}

/**
 * Returns the size of the given file
 * @link http://php.net/manual/en/function.ftp-size.php
 * @param ftp_stream resource
 * @param remote_file string
 * @return int the file size on success, or -1 on error.
 */
function ftp_size ($ftp_stream, $remote_file) {}

/**
 * Returns the last modified time of the given file
 * @link http://php.net/manual/en/function.ftp-mdtm.php
 * @param ftp_stream resource
 * @param remote_file string
 * @return int the last modified time as a Unix timestamp on success, or -1 on
 */
function ftp_mdtm ($ftp_stream, $remote_file) {}

/**
 * Renames a file or a directory on the FTP server
 * @link http://php.net/manual/en/function.ftp-rename.php
 * @param ftp_stream resource
 * @param oldname string
 * @param newname string
 * @return bool 
 */
function ftp_rename ($ftp_stream, $oldname, $newname) {}

/**
 * Deletes a file on the FTP server
 * @link http://php.net/manual/en/function.ftp-delete.php
 * @param ftp_stream resource
 * @param path string
 * @return bool 
 */
function ftp_delete ($ftp_stream, $path) {}

/**
 * Sends a SITE command to the server
 * @link http://php.net/manual/en/function.ftp-site.php
 * @param ftp_stream resource
 * @param command string
 * @return bool 
 */
function ftp_site ($ftp_stream, $command) {}

/**
 * Closes an FTP connection
 * @link http://php.net/manual/en/function.ftp-close.php
 * @param ftp_stream resource
 * @return bool 
 */
function ftp_close ($ftp_stream) {}

/**
 * Set miscellaneous runtime FTP options
 * @link http://php.net/manual/en/function.ftp-set-option.php
 * @param ftp_stream resource
 * @param option int
 * @param value mixed
 * @return bool true if the option could be set; false if not. A warning
 */
function ftp_set_option ($ftp_stream, $option, $value) {}

/**
 * Retrieves various runtime behaviours of the current FTP stream
 * @link http://php.net/manual/en/function.ftp-get-option.php
 * @param ftp_stream resource
 * @param option int
 * @return mixed the value on success or false if the given
 */
function ftp_get_option ($ftp_stream, $option) {}

/**
 * Retrieves a file from the FTP server and writes it to an open file (non-blocking)
 * @link http://php.net/manual/en/function.ftp-nb-fget.php
 * @param ftp_stream resource
 * @param handle resource
 * @param remote_file string
 * @param mode int
 * @param resumepos int[optional]
 * @return int FTP_FAILED or FTP_FINISHED
 */
function ftp_nb_fget ($ftp_stream, $handle, $remote_file, $mode, $resumepos = null) {}

/**
 * Retrieves a file from the FTP server and writes it to a local file (non-blocking)
 * @link http://php.net/manual/en/function.ftp-nb-get.php
 * @param ftp_stream resource
 * @param local_file string
 * @param remote_file string
 * @param mode int
 * @param resumepos int[optional]
 * @return int FTP_FAILED or FTP_FINISHED
 */
function ftp_nb_get ($ftp_stream, $local_file, $remote_file, $mode, $resumepos = null) {}

/**
 * Continues retrieving/sending a file (non-blocking)
 * @link http://php.net/manual/en/function.ftp-nb-continue.php
 * @param ftp_stream resource
 * @return int FTP_FAILED or FTP_FINISHED
 */
function ftp_nb_continue ($ftp_stream) {}

/**
 * Stores a file on the FTP server (non-blocking)
 * @link http://php.net/manual/en/function.ftp-nb-put.php
 * @param ftp_stream resource
 * @param remote_file string
 * @param local_file string
 * @param mode int
 * @param startpos int[optional]
 * @return int FTP_FAILED or FTP_FINISHED
 */
function ftp_nb_put ($ftp_stream, $remote_file, $local_file, $mode, $startpos = null) {}

/**
 * Stores a file from an open file to the FTP server (non-blocking)
 * @link http://php.net/manual/en/function.ftp-nb-fput.php
 * @param ftp_stream resource
 * @param remote_file string
 * @param handle resource
 * @param mode int
 * @param startpos int[optional]
 * @return int FTP_FAILED or FTP_FINISHED
 */
function ftp_nb_fput ($ftp_stream, $remote_file, $handle, $mode, $startpos = null) {}

/**
 * &Alias; <function>ftp_close</function>
 * @link http://php.net/manual/en/function.ftp-quit.php
 * @param ftp
 */
function ftp_quit ($ftp) {}

define ('FTP_ASCII', 1);
define ('FTP_TEXT', 1);
define ('FTP_BINARY', 2);
define ('FTP_IMAGE', 2);

/**
 * Automatically determine resume position and start position for GET and PUT requests
 * (only works if FTP_AUTOSEEK is enabled)
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_AUTORESUME', -1);

/**
 * See ftp_set_option for information.
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_TIMEOUT_SEC', 0);

/**
 * See ftp_set_option for information.
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_AUTOSEEK', 1);

/**
 * Asynchronous transfer has failed
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_FAILED', 0);

/**
 * Asynchronous transfer has finished
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_FINISHED', 1);

/**
 * Asynchronous transfer is still active
 * @link http://php.net/manual/en/ftp.constants.php
 */
define ('FTP_MOREDATA', 2);

// End of ftp v.

// Start of ldap v.

/**
 * Connect to an LDAP server
 * @link http://php.net/manual/en/function.ldap-connect.php
 * @param hostname string[optional]
 * @param port int[optional]
 * @return resource a positive LDAP link identifier on success, or false on error.
 */
function ldap_connect ($hostname = null, $port = null) {}

/**
 * &Alias; <function>ldap_unbind</function>
 * @link http://php.net/manual/en/function.ldap-close.php
 */
function ldap_close () {}

/**
 * Bind to LDAP directory
 * @link http://php.net/manual/en/function.ldap-bind.php
 * @param link_identifier resource
 * @param bind_rdn string[optional]
 * @param bind_password string[optional]
 * @return bool 
 */
function ldap_bind ($link_identifier, $bind_rdn = null, $bind_password = null) {}

/**
 * Unbind from LDAP directory
 * @link http://php.net/manual/en/function.ldap-unbind.php
 * @param link_identifier resource
 * @return bool 
 */
function ldap_unbind ($link_identifier) {}

/**
 * Read an entry
 * @link http://php.net/manual/en/function.ldap-read.php
 * @param link_identifier resource
 * @param base_dn string
 * @param filter string
 * @param attributes array[optional]
 * @param attrsonly int[optional]
 * @param sizelimit int[optional]
 * @param timelimit int[optional]
 * @param deref int[optional]
 * @return resource a search result identifier or false on error.
 */
function ldap_read ($link_identifier, $base_dn, $filter, array $attributes = null, $attrsonly = null, $sizelimit = null, $timelimit = null, $deref = null) {}

/**
 * Single-level search
 * @link http://php.net/manual/en/function.ldap-list.php
 * @param link_identifier resource
 * @param base_dn string
 * @param filter string
 * @param attributes array[optional]
 * @param attrsonly int[optional]
 * @param sizelimit int[optional]
 * @param timelimit int[optional]
 * @param deref int[optional]
 * @return resource a search result identifier or false on error.
 */
function ldap_list ($link_identifier, $base_dn, $filter, array $attributes = null, $attrsonly = null, $sizelimit = null, $timelimit = null, $deref = null) {}

/**
 * Search LDAP tree
 * @link http://php.net/manual/en/function.ldap-search.php
 * @param link_identifier resource
 * @param base_dn string
 * @param filter string
 * @param attributes array[optional]
 * @param attrsonly int[optional]
 * @param sizelimit int[optional]
 * @param timelimit int[optional]
 * @param deref int[optional]
 * @return resource a search result identifier or false on error.
 */
function ldap_search ($link_identifier, $base_dn, $filter, array $attributes = null, $attrsonly = null, $sizelimit = null, $timelimit = null, $deref = null) {}

/**
 * Free result memory
 * @link http://php.net/manual/en/function.ldap-free-result.php
 * @param result_identifier resource
 * @return bool 
 */
function ldap_free_result ($result_identifier) {}

/**
 * Count the number of entries in a search
 * @link http://php.net/manual/en/function.ldap-count-entries.php
 * @param link_identifier resource
 * @param result_identifier resource
 * @return int number of entries in the result or false on error.
 */
function ldap_count_entries ($link_identifier, $result_identifier) {}

/**
 * Return first result id
 * @link http://php.net/manual/en/function.ldap-first-entry.php
 * @param link_identifier resource
 * @param result_identifier resource
 * @return resource the result entry identifier for the first entry on success and
 */
function ldap_first_entry ($link_identifier, $result_identifier) {}

/**
 * Get next result entry
 * @link http://php.net/manual/en/function.ldap-next-entry.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @return resource entry identifier for the next entry in the result whose entries
 */
function ldap_next_entry ($link_identifier, $result_entry_identifier) {}

/**
 * Get all result entries
 * @link http://php.net/manual/en/function.ldap-get-entries.php
 * @param link_identifier resource
 * @param result_identifier resource
 * @return array a complete result information in a multi-dimensional array on
 */
function ldap_get_entries ($link_identifier, $result_identifier) {}

/**
 * Return first attribute
 * @link http://php.net/manual/en/function.ldap-first-attribute.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @param ber_identifier int
 * @return string the first attribute in the entry on success and false on
 */
function ldap_first_attribute ($link_identifier, $result_entry_identifier, &$ber_identifier) {}

/**
 * Get the next attribute in result
 * @link http://php.net/manual/en/function.ldap-next-attribute.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @param ber_identifier resource
 * @return string the next attribute in an entry on success and false on
 */
function ldap_next_attribute ($link_identifier, $result_entry_identifier, &$ber_identifier) {}

/**
 * Get attributes from a search result entry
 * @link http://php.net/manual/en/function.ldap-get-attributes.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @return array a complete entry information in a multi-dimensional array
 */
function ldap_get_attributes ($link_identifier, $result_entry_identifier) {}

/**
 * Get all values from a result entry
 * @link http://php.net/manual/en/function.ldap-get-values.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @param attribute string
 * @return array an array of values for the attribute on success and false on
 */
function ldap_get_values ($link_identifier, $result_entry_identifier, $attribute) {}

/**
 * Get all binary values from a result entry
 * @link http://php.net/manual/en/function.ldap-get-values-len.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @param attribute string
 * @return array an array of values for the attribute on success and false on
 */
function ldap_get_values_len ($link_identifier, $result_entry_identifier, $attribute) {}

/**
 * Get the DN of a result entry
 * @link http://php.net/manual/en/function.ldap-get-dn.php
 * @param link_identifier resource
 * @param result_entry_identifier resource
 * @return string the DN of the result entry and false on error.
 */
function ldap_get_dn ($link_identifier, $result_entry_identifier) {}

/**
 * Splits DN into its component parts
 * @link http://php.net/manual/en/function.ldap-explode-dn.php
 * @param dn string
 * @param with_attrib int
 * @return array an array of all DN components.
 */
function ldap_explode_dn ($dn, $with_attrib) {}

/**
 * Convert DN to User Friendly Naming format
 * @link http://php.net/manual/en/function.ldap-dn2ufn.php
 * @param dn string
 * @return string the user friendly name.
 */
function ldap_dn2ufn ($dn) {}

/**
 * Add entries to LDAP directory
 * @link http://php.net/manual/en/function.ldap-add.php
 * @param link_identifier resource
 * @param dn string
 * @param entry array
 * @return bool 
 */
function ldap_add ($link_identifier, $dn, array $entry) {}

/**
 * Delete an entry from a directory
 * @link http://php.net/manual/en/function.ldap-delete.php
 * @param link_identifier resource
 * @param dn string
 * @return bool 
 */
function ldap_delete ($link_identifier, $dn) {}

/**
 * Modify an LDAP entry
 * @link http://php.net/manual/en/function.ldap-modify.php
 * @param link_identifier resource
 * @param dn string
 * @param entry array
 * @return bool 
 */
function ldap_modify ($link_identifier, $dn, array $entry) {}

/**
 * Add attribute values to current attributes
 * @link http://php.net/manual/en/function.ldap-mod-add.php
 * @param link_identifier resource
 * @param dn string
 * @param entry array
 * @return bool 
 */
function ldap_mod_add ($link_identifier, $dn, array $entry) {}

/**
 * Replace attribute values with new ones
 * @link http://php.net/manual/en/function.ldap-mod-replace.php
 * @param link_identifier resource
 * @param dn string
 * @param entry array
 * @return bool 
 */
function ldap_mod_replace ($link_identifier, $dn, array $entry) {}

/**
 * Delete attribute values from current attributes
 * @link http://php.net/manual/en/function.ldap-mod-del.php
 * @param link_identifier resource
 * @param dn string
 * @param entry array
 * @return bool 
 */
function ldap_mod_del ($link_identifier, $dn, array $entry) {}

/**
 * Return the LDAP error number of the last LDAP command
 * @link http://php.net/manual/en/function.ldap-errno.php
 * @param link_identifier resource
 * @return int 
 */
function ldap_errno ($link_identifier) {}

/**
 * Convert LDAP error number into string error message
 * @link http://php.net/manual/en/function.ldap-err2str.php
 * @param errno int
 * @return string the error message, as a string.
 */
function ldap_err2str ($errno) {}

/**
 * Return the LDAP error message of the last LDAP command
 * @link http://php.net/manual/en/function.ldap-error.php
 * @param link_identifier resource
 * @return string string error message.
 */
function ldap_error ($link_identifier) {}

/**
 * Compare value of attribute found in entry specified with DN
 * @link http://php.net/manual/en/function.ldap-compare.php
 * @param link_identifier resource
 * @param dn string
 * @param attribute string
 * @param value string
 * @return mixed true if value matches otherwise returns
 */
function ldap_compare ($link_identifier, $dn, $attribute, $value) {}

/**
 * Sort LDAP result entries
 * @link http://php.net/manual/en/function.ldap-sort.php
 * @param link resource
 * @param result resource
 * @param sortfilter string
 * @return bool 
 */
function ldap_sort ($link, $result, $sortfilter) {}

/**
 * Modify the name of an entry
 * @link http://php.net/manual/en/function.ldap-rename.php
 * @param link_identifier resource
 * @param dn string
 * @param newrdn string
 * @param newparent string
 * @param deleteoldrdn bool
 * @return bool 
 */
function ldap_rename ($link_identifier, $dn, $newrdn, $newparent, $deleteoldrdn) {}

/**
 * Get the current value for given option
 * @link http://php.net/manual/en/function.ldap-get-option.php
 * @param link_identifier resource
 * @param option int
 * @param retval mixed
 * @return bool 
 */
function ldap_get_option ($link_identifier, $option, &$retval) {}

/**
 * Set the value of the given option
 * @link http://php.net/manual/en/function.ldap-set-option.php
 * @param link_identifier resource
 * @param option int
 * @param newval mixed
 * @return bool 
 */
function ldap_set_option ($link_identifier, $option, $newval) {}

/**
 * Return first reference
 * @link http://php.net/manual/en/function.ldap-first-reference.php
 * @param link resource
 * @param result resource
 * @return resource 
 */
function ldap_first_reference ($link, $result) {}

/**
 * Get next reference
 * @link http://php.net/manual/en/function.ldap-next-reference.php
 * @param link resource
 * @param entry resource
 * @return resource 
 */
function ldap_next_reference ($link, $entry) {}

/**
 * Extract information from reference entry
 * @link http://php.net/manual/en/function.ldap-parse-reference.php
 * @param link resource
 * @param entry resource
 * @param referrals array
 * @return bool 
 */
function ldap_parse_reference ($link, $entry, array &$referrals) {}

/**
 * Extract information from result
 * @link http://php.net/manual/en/function.ldap-parse-result.php
 * @param link resource
 * @param result resource
 * @param errcode int
 * @param matcheddn string[optional]
 * @param errmsg string[optional]
 * @param referrals array[optional]
 * @return bool 
 */
function ldap_parse_result ($link, $result, &$errcode, &$matcheddn = null, &$errmsg = null, array &$referrals = null) {}

/**
 * Start TLS
 * @link http://php.net/manual/en/function.ldap-start-tls.php
 * @param link resource
 * @return bool 
 */
function ldap_start_tls ($link) {}

/**
 * Set a callback function to do re-binds on referral chasing
 * @link http://php.net/manual/en/function.ldap-set-rebind-proc.php
 * @param link resource
 * @param callback callback
 * @return bool 
 */
function ldap_set_rebind_proc ($link, $callback) {}

define ('LDAP_DEREF_NEVER', 0);
define ('LDAP_DEREF_SEARCHING', 1);
define ('LDAP_DEREF_FINDING', 2);
define ('LDAP_DEREF_ALWAYS', 3);
define ('LDAP_OPT_DEREF', 2);
define ('LDAP_OPT_SIZELIMIT', 3);
define ('LDAP_OPT_TIMELIMIT', 4);
define ('LDAP_OPT_PROTOCOL_VERSION', 17);
define ('LDAP_OPT_ERROR_NUMBER', 49);
define ('LDAP_OPT_REFERRALS', 8);
define ('LDAP_OPT_RESTART', 9);
define ('LDAP_OPT_HOST_NAME', 48);
define ('LDAP_OPT_ERROR_STRING', 50);
define ('LDAP_OPT_MATCHED_DN', 51);
define ('LDAP_OPT_SERVER_CONTROLS', 18);
define ('LDAP_OPT_CLIENT_CONTROLS', 19);
define ('LDAP_OPT_DEBUG_LEVEL', 20481);

// End of ldap v.

// Start of mhash v.

/**
 * Get the block size of the specified hash
 * @link http://php.net/manual/en/function.mhash-get-block-size.php
 * @param hash int
 * @return int the size in bytes or false, if the hash
 */
function mhash_get_block_size ($hash) {}

/**
 * Get the name of the specified hash
 * @link http://php.net/manual/en/function.mhash-get-hash-name.php
 * @param hash int
 * @return string the name of the hash or false, if the hash does not exist.
 */
function mhash_get_hash_name ($hash) {}

/**
 * Generates a key
 * @link http://php.net/manual/en/function.mhash-keygen-s2k.php
 * @param hash int
 * @param password string
 * @param salt string
 * @param bytes int
 * @return string the generated key as a string, or false on error.
 */
function mhash_keygen_s2k ($hash, $password, $salt, $bytes) {}

/**
 * Get the highest available hash id
 * @link http://php.net/manual/en/function.mhash-count.php
 * @return int the highest available hash id. Hashes are numbered from 0 to this
 */
function mhash_count () {}

/**
 * Compute hash
 * @link http://php.net/manual/en/function.mhash.php
 * @param hash int
 * @param data string
 * @param key string[optional]
 * @return string the resulting hash (also called digest) or HMAC as a string, or
 */
function mhash ($hash, $data, $key = null) {}

define ('MHASH_CRC32', 0);
define ('MHASH_MD5', 1);
define ('MHASH_SHA1', 2);
define ('MHASH_HAVAL256', 3);
define ('MHASH_RIPEMD160', 5);
define ('MHASH_TIGER', 7);
define ('MHASH_GOST', 8);
define ('MHASH_CRC32B', 9);
define ('MHASH_HAVAL224', 10);
define ('MHASH_HAVAL192', 11);
define ('MHASH_HAVAL160', 12);
define ('MHASH_HAVAL128', 13);
define ('MHASH_TIGER128', 14);
define ('MHASH_TIGER160', 15);
define ('MHASH_MD4', 16);
define ('MHASH_SHA256', 17);
define ('MHASH_ADLER32', 18);
define ('MHASH_SHA224', 19);
define ('MHASH_SHA512', 20);
define ('MHASH_SHA384', 21);
define ('MHASH_WHIRLPOOL', 22);
define ('MHASH_RIPEMD128', 23);
define ('MHASH_RIPEMD256', 24);
define ('MHASH_RIPEMD320', 25);
define ('MHASH_SNEFRU128', 26);
define ('MHASH_SNEFRU256', 27);
define ('MHASH_MD2', 28);

// End of mhash v.

// Start of mysql v.1.0

/**
 * Open a connection to a MySQL Server
 * @link http://php.net/manual/en/function.mysql-connect.php
 * @param server string[optional]
 * @param username string[optional]
 * @param password string[optional]
 * @param new_link bool[optional]
 * @param client_flags int[optional]
 * @return resource a MySQL link identifier on success, or false on failure.
 */
function mysql_connect ($server = null, $username = null, $password = null, $new_link = null, $client_flags = null) {}

/**
 * Open a persistent connection to a MySQL server
 * @link http://php.net/manual/en/function.mysql-pconnect.php
 * @param server string[optional]
 * @param username string[optional]
 * @param password string[optional]
 * @param client_flags int[optional]
 * @return resource a MySQL persistent link identifier on success, or false on
 */
function mysql_pconnect ($server = null, $username = null, $password = null, $client_flags = null) {}

/**
 * Close MySQL connection
 * @link http://php.net/manual/en/function.mysql-close.php
 * @param link_identifier resource[optional]
 * @return bool 
 */
function mysql_close ($link_identifier = null) {}

/**
 * Select a MySQL database
 * @link http://php.net/manual/en/function.mysql-select-db.php
 * @param database_name string
 * @param link_identifier resource[optional]
 * @return bool 
 */
function mysql_select_db ($database_name, $link_identifier = null) {}

/**
 * Send a MySQL query
 * @link http://php.net/manual/en/function.mysql-query.php
 * @param query string
 * @param link_identifier resource[optional]
 * @return resource 
 */
function mysql_query ($query, $link_identifier = null) {}

/**
 * Send an SQL query to MySQL, without fetching and buffering the result rows
 * @link http://php.net/manual/en/function.mysql-unbuffered-query.php
 * @param query string
 * @param link_identifier resource[optional]
 * @return resource 
 */
function mysql_unbuffered_query ($query, $link_identifier = null) {}

/**
 * Send a MySQL query
 * @link http://php.net/manual/en/function.mysql-db-query.php
 * @param database string
 * @param query string
 * @param link_identifier resource[optional]
 * @return resource a positive MySQL result resource to the query result,
 */
function mysql_db_query ($database, $query, $link_identifier = null) {}

/**
 * List databases available on a MySQL server
 * @link http://php.net/manual/en/function.mysql-list-dbs.php
 * @param link_identifier resource[optional]
 * @return resource a result pointer resource on success, or false on
 */
function mysql_list_dbs ($link_identifier = null) {}

/**
 * List tables in a MySQL database
 * @link http://php.net/manual/en/function.mysql-list-tables.php
 * @param database string
 * @param link_identifier resource[optional]
 * @return resource 
 */
function mysql_list_tables ($database, $link_identifier = null) {}

/**
 * List MySQL table fields
 * @link http://php.net/manual/en/function.mysql-list-fields.php
 * @param database_name string
 * @param table_name string
 * @param link_identifier resource[optional]
 * @return resource 
 */
function mysql_list_fields ($database_name, $table_name, $link_identifier = null) {}

/**
 * List MySQL processes
 * @link http://php.net/manual/en/function.mysql-list-processes.php
 * @param link_identifier resource[optional]
 * @return resource 
 */
function mysql_list_processes ($link_identifier = null) {}

/**
 * Returns the text of the error message from previous MySQL operation
 * @link http://php.net/manual/en/function.mysql-error.php
 * @param link_identifier resource[optional]
 * @return string the error text from the last MySQL function, or
 */
function mysql_error ($link_identifier = null) {}

/**
 * Returns the numerical value of the error message from previous MySQL operation
 * @link http://php.net/manual/en/function.mysql-errno.php
 * @param link_identifier resource[optional]
 * @return int the error number from the last MySQL function, or
 */
function mysql_errno ($link_identifier = null) {}

/**
 * Get number of affected rows in previous MySQL operation
 * @link http://php.net/manual/en/function.mysql-affected-rows.php
 * @param link_identifier resource[optional]
 * @return int the number of affected rows on success, and -1 if the last query
 */
function mysql_affected_rows ($link_identifier = null) {}

/**
 * Get the ID generated from the previous INSERT operation
 * @link http://php.net/manual/en/function.mysql-insert-id.php
 * @param link_identifier resource[optional]
 * @return int 
 */
function mysql_insert_id ($link_identifier = null) {}

/**
 * Get result data
 * @link http://php.net/manual/en/function.mysql-result.php
 * @param result resource
 * @param row int
 * @param field mixed[optional]
 * @return string 
 */
function mysql_result ($result, $row, $field = null) {}

/**
 * Get number of rows in result
 * @link http://php.net/manual/en/function.mysql-num-rows.php
 * @param result resource
 * @return int 
 */
function mysql_num_rows ($result) {}

/**
 * Get number of fields in result
 * @link http://php.net/manual/en/function.mysql-num-fields.php
 * @param result resource
 * @return int the number of fields in the result set resource on
 */
function mysql_num_fields ($result) {}

/**
 * Get a result row as an enumerated array
 * @link http://php.net/manual/en/function.mysql-fetch-row.php
 * @param result resource
 * @return array an numerical array of strings that corresponds to the fetched row, or
 */
function mysql_fetch_row ($result) {}

/**
 * Fetch a result row as an associative array, a numeric array, or both
 * @link http://php.net/manual/en/function.mysql-fetch-array.php
 * @param result resource
 * @param result_type int[optional]
 * @return array an array of strings that corresponds to the fetched row, or false
 */
function mysql_fetch_array ($result, $result_type = null) {}

/**
 * Fetch a result row as an associative array
 * @link http://php.net/manual/en/function.mysql-fetch-assoc.php
 * @param result resource
 * @return array an associative array of strings that corresponds to the fetched row, or
 */
function mysql_fetch_assoc ($result) {}

/**
 * Fetch a result row as an object
 * @link http://php.net/manual/en/function.mysql-fetch-object.php
 * @param result resource
 * @param class_name string[optional]
 * @param params array[optional]
 * @return object an object with string properties that correspond to the
 */
function mysql_fetch_object ($result, $class_name = null, array $params = null) {}

/**
 * Move internal result pointer
 * @link http://php.net/manual/en/function.mysql-data-seek.php
 * @param result resource
 * @param row_number int
 * @return bool 
 */
function mysql_data_seek ($result, $row_number) {}

/**
 * Get the length of each output in a result
 * @link http://php.net/manual/en/function.mysql-fetch-lengths.php
 * @param result resource
 * @return array 
 */
function mysql_fetch_lengths ($result) {}

/**
 * Get column information from a result and return as an object
 * @link http://php.net/manual/en/function.mysql-fetch-field.php
 * @param result resource
 * @param field_offset int[optional]
 * @return object an object containing field information. The properties
 */
function mysql_fetch_field ($result, $field_offset = null) {}

/**
 * Set result pointer to a specified field offset
 * @link http://php.net/manual/en/function.mysql-field-seek.php
 * @param result resource
 * @param field_offset int
 * @return bool 
 */
function mysql_field_seek ($result, $field_offset) {}

/**
 * Free result memory
 * @link http://php.net/manual/en/function.mysql-free-result.php
 * @param result resource
 * @return bool 
 */
function mysql_free_result ($result) {}

/**
 * Get the name of the specified field in a result
 * @link http://php.net/manual/en/function.mysql-field-name.php
 * @param result resource
 * @param field_offset int
 * @return string 
 */
function mysql_field_name ($result, $field_offset) {}

/**
 * Get name of the table the specified field is in
 * @link http://php.net/manual/en/function.mysql-field-table.php
 * @param result resource
 * @param field_offset int
 * @return string 
 */
function mysql_field_table ($result, $field_offset) {}

/**
 * Returns the length of the specified field
 * @link http://php.net/manual/en/function.mysql-field-len.php
 * @param result resource
 * @param field_offset int
 * @return int 
 */
function mysql_field_len ($result, $field_offset) {}

/**
 * Get the type of the specified field in a result
 * @link http://php.net/manual/en/function.mysql-field-type.php
 * @param result resource
 * @param field_offset int
 * @return string 
 */
function mysql_field_type ($result, $field_offset) {}

/**
 * Get the flags associated with the specified field in a result
 * @link http://php.net/manual/en/function.mysql-field-flags.php
 * @param result resource
 * @param field_offset int
 * @return string a string of flags associated with the result, or false on failure.
 */
function mysql_field_flags ($result, $field_offset) {}

/**
 * Escapes a string for use in a mysql_query
 * @link http://php.net/manual/en/function.mysql-escape-string.php
 * @param unescaped_string string
 * @return string the escaped string.
 */
function mysql_escape_string ($unescaped_string) {}

/**
 * Escapes special characters in a string for use in a SQL statement
 * @link http://php.net/manual/en/function.mysql-real-escape-string.php
 * @param unescaped_string string
 * @param link_identifier resource[optional]
 * @return string the escaped string, or false on error.
 */
function mysql_real_escape_string ($unescaped_string, $link_identifier = null) {}

/**
 * Get current system status
 * @link http://php.net/manual/en/function.mysql-stat.php
 * @param link_identifier resource[optional]
 * @return string a string with the status for uptime, threads, queries, open tables,
 */
function mysql_stat ($link_identifier = null) {}

/**
 * Return the current thread ID
 * @link http://php.net/manual/en/function.mysql-thread-id.php
 * @param link_identifier resource[optional]
 * @return int 
 */
function mysql_thread_id ($link_identifier = null) {}

/**
 * Returns the name of the character set
 * @link http://php.net/manual/en/function.mysql-client-encoding.php
 * @param link_identifier resource[optional]
 * @return string the default character set name for the current connection.
 */
function mysql_client_encoding ($link_identifier = null) {}

/**
 * Ping a server connection or reconnect if there is no connection
 * @link http://php.net/manual/en/function.mysql-ping.php
 * @param link_identifier resource[optional]
 * @return bool true if the connection to the server MySQL server is working,
 */
function mysql_ping ($link_identifier = null) {}

/**
 * Get MySQL client info
 * @link http://php.net/manual/en/function.mysql-get-client-info.php
 * @return string 
 */
function mysql_get_client_info () {}

/**
 * Get MySQL host info
 * @link http://php.net/manual/en/function.mysql-get-host-info.php
 * @param link_identifier resource[optional]
 * @return string a string describing the type of MySQL connection in use for the
 */
function mysql_get_host_info ($link_identifier = null) {}

/**
 * Get MySQL protocol info
 * @link http://php.net/manual/en/function.mysql-get-proto-info.php
 * @param link_identifier resource[optional]
 * @return int the MySQL protocol on success, or false on failure.
 */
function mysql_get_proto_info ($link_identifier = null) {}

/**
 * Get MySQL server info
 * @link http://php.net/manual/en/function.mysql-get-server-info.php
 * @param link_identifier resource[optional]
 * @return string the MySQL server version on success, or false on failure.
 */
function mysql_get_server_info ($link_identifier = null) {}

/**
 * Get information about the most recent query
 * @link http://php.net/manual/en/function.mysql-info.php
 * @param link_identifier resource[optional]
 * @return string information about the statement on success, or false on
 */
function mysql_info ($link_identifier = null) {}

/**
 * Sets the client character set
 * @link http://php.net/manual/en/function.mysql-set-charset.php
 * @param charset string
 * @param link_identifier resource[optional]
 * @return bool 
 */
function mysql_set_charset ($charset, $link_identifier = null) {}

function mysql () {}

function mysql_fieldname () {}

function mysql_fieldtable () {}

function mysql_fieldlen () {}

function mysql_fieldtype () {}

function mysql_fieldflags () {}

function mysql_selectdb () {}

function mysql_freeresult () {}

function mysql_numfields () {}

function mysql_numrows () {}

function mysql_listdbs () {}

function mysql_listtables () {}

function mysql_listfields () {}

/**
 * Get result data
 * @link http://php.net/manual/en/function.mysql-db-name.php
 * @param result resource
 * @param row int
 * @param field mixed[optional]
 * @return string the database name on success, and false on failure. If false
 */
function mysql_db_name ($result, $row, $field = null) {}

function mysql_dbname () {}

/**
 * Get table name of field
 * @link http://php.net/manual/en/function.mysql-tablename.php
 * @param result resource
 * @param i int
 * @return string 
 */
function mysql_tablename ($result, $i) {}

function mysql_table_name () {}

define ('MYSQL_ASSOC', 1);
define ('MYSQL_NUM', 2);
define ('MYSQL_BOTH', 3);
define ('MYSQL_CLIENT_COMPRESS', 32);
define ('MYSQL_CLIENT_SSL', 2048);
define ('MYSQL_CLIENT_INTERACTIVE', 1024);
define ('MYSQL_CLIENT_IGNORE_SPACE', 256);

// End of mysql v.1.0

// Start of PDO v.1.0.4dev

/**
 * Represents an error raised by PDO. You should not throw a
 * PDOException from your own code.
 * See Exceptions for more
 * information about Exceptions in PHP.
 * @link http://php.net/manual/en/ref.pdo.php
 */
class PDOException extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;
	public $errorInfo;


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

/**
 * Represents a connection between PHP and a database server.
 * @link http://php.net/manual/en/ref.pdo.php
 */
class PDO  {
	const PARAM_BOOL = 5;
	const PARAM_NULL = 0;
	const PARAM_INT = 1;
	const PARAM_STR = 2;
	const PARAM_LOB = 3;
	const PARAM_STMT = 4;
	const PARAM_INPUT_OUTPUT = 2147483648;
	const PARAM_EVT_ALLOC = 0;
	const PARAM_EVT_FREE = 1;
	const PARAM_EVT_EXEC_PRE = 2;
	const PARAM_EVT_EXEC_POST = 3;
	const PARAM_EVT_FETCH_PRE = 4;
	const PARAM_EVT_FETCH_POST = 5;
	const PARAM_EVT_NORMALIZE = 6;
	const FETCH_LAZY = 1;
	const FETCH_ASSOC = 2;
	const FETCH_NUM = 3;
	const FETCH_BOTH = 4;
	const FETCH_OBJ = 5;
	const FETCH_BOUND = 6;
	const FETCH_COLUMN = 7;
	const FETCH_CLASS = 8;
	const FETCH_INTO = 9;
	const FETCH_FUNC = 10;
	const FETCH_GROUP = 65536;
	const FETCH_UNIQUE = 196608;
	const FETCH_KEY_PAIR = 12;
	const FETCH_CLASSTYPE = 262144;
	const FETCH_SERIALIZE = 524288;
	const FETCH_PROPS_LATE = 1048576;
	const FETCH_NAMED = 11;
	const ATTR_AUTOCOMMIT = 0;
	const ATTR_PREFETCH = 1;
	const ATTR_TIMEOUT = 2;
	const ATTR_ERRMODE = 3;
	const ATTR_SERVER_VERSION = 4;
	const ATTR_CLIENT_VERSION = 5;
	const ATTR_SERVER_INFO = 6;
	const ATTR_CONNECTION_STATUS = 7;
	const ATTR_CASE = 8;
	const ATTR_CURSOR_NAME = 9;
	const ATTR_CURSOR = 10;
	const ATTR_ORACLE_NULLS = 11;
	const ATTR_PERSISTENT = 12;
	const ATTR_STATEMENT_CLASS = 13;
	const ATTR_FETCH_TABLE_NAMES = 14;
	const ATTR_FETCH_CATALOG_NAMES = 15;
	const ATTR_DRIVER_NAME = 16;
	const ATTR_STRINGIFY_FETCHES = 17;
	const ATTR_MAX_COLUMN_LEN = 18;
	const ATTR_EMULATE_PREPARES = 20;
	const ATTR_DEFAULT_FETCH_MODE = 19;
	const ERRMODE_SILENT = 0;
	const ERRMODE_WARNING = 1;
	const ERRMODE_EXCEPTION = 2;
	const CASE_NATURAL = 0;
	const CASE_LOWER = 2;
	const CASE_UPPER = 1;
	const NULL_NATURAL = 0;
	const NULL_EMPTY_STRING = 1;
	const NULL_TO_STRING = 2;
	const ERR_NONE = 00000;
	const FETCH_ORI_NEXT = 0;
	const FETCH_ORI_PRIOR = 1;
	const FETCH_ORI_FIRST = 2;
	const FETCH_ORI_LAST = 3;
	const FETCH_ORI_ABS = 4;
	const FETCH_ORI_REL = 5;
	const CURSOR_FWDONLY = 0;
	const CURSOR_SCROLL = 1;
	const MYSQL_ATTR_USE_BUFFERED_QUERY = 1000;
	const MYSQL_ATTR_LOCAL_INFILE = 1001;
	const MYSQL_ATTR_INIT_COMMAND = 1002;
	const MYSQL_ATTR_READ_DEFAULT_FILE = 1003;
	const MYSQL_ATTR_READ_DEFAULT_GROUP = 1004;
	const MYSQL_ATTR_MAX_BUFFER_SIZE = 1005;
	const MYSQL_ATTR_DIRECT_QUERY = 1006;
	const PGSQL_ATTR_DISABLE_NATIVE_PREPARED_STATEMENT = 1000;


	/**
	 * Creates a PDO instance representing a connection to a database
	 * @link http://php.net/manual/en/function.PDO-construct.php
	 * @param dsn string
	 * @param username string[optional]
	 * @param password string[optional]
	 * @param driver_options array[optional]
	 * @return PDO a PDO object on success.
	 */
	public function __construct ($dsn, $username = null, $password = null, array $driver_options = null) {}

	/**
	 * Prepares a statement for execution and returns a statement object
	 * @link http://php.net/manual/en/function.PDO-prepare.php
	 * @param statement string
	 * @param driver_options array[optional]
	 * @return PDOStatement 
	 */
	public function prepare ($statement, array $driver_options = null) {}

	/**
	 * Initiates a transaction
	 * @link http://php.net/manual/en/function.PDO-beginTransaction.php
	 * @return bool 
	 */
	public function beginTransaction () {}

	/**
	 * Commits a transaction
	 * @link http://php.net/manual/en/function.PDO-commit.php
	 * @return bool 
	 */
	public function commit () {}

	/**
	 * Rolls back a transaction
	 * @link http://php.net/manual/en/function.PDO-rollBack.php
	 * @return bool 
	 */
	public function rollBack () {}

	/**
	 * Set an attribute
	 * @link http://php.net/manual/en/function.PDO-setAttribute.php
	 * @param attribute int
	 * @param value mixed
	 * @return bool 
	 */
	public function setAttribute ($attribute, $value) {}

	/**
	 * Execute an SQL statement and return the number of affected rows
	 * @link http://php.net/manual/en/function.PDO-exec.php
	 * @param statement string
	 * @return int 
	 */
	public function exec ($statement) {}

	/**
	 * Executes an SQL statement, returning a result set as a PDOStatement object
	 * @link http://php.net/manual/en/function.PDO-query.php
	 * @param statement string
	 * @param PDO::FETCH_INTO int
	 * @param object object
	 * @return PDOStatement 
	 */
	public function query ($statement, $PDO_FETCH_INTO, $object) {}

	/**
	 * Returns the ID of the last inserted row or sequence value
	 * @link http://php.net/manual/en/function.PDO-lastInsertId.php
	 * @param name string[optional]
	 * @return string 
	 */
	public function lastInsertId ($name = null) {}

	/**
	 * Fetch the SQLSTATE associated with the last operation on the database handle
	 * @link http://php.net/manual/en/function.PDO-errorCode.php
	 * @return string a SQLSTATE, a five-character alphanumeric identifier defined in
	 */
	public function errorCode () {}

	/**
	 * Fetch extended error information associated with the last operation on the database handle
	 * @link http://php.net/manual/en/function.PDO-errorInfo.php
	 * @return array 
	 */
	public function errorInfo () {}

	/**
	 * Retrieve a database connection attribute
	 * @link http://php.net/manual/en/function.PDO-getAttribute.php
	 * @param attribute int
	 * @return mixed 
	 */
	public function getAttribute ($attribute) {}

	/**
	 * Quotes a string for use in a query.
	 * @link http://php.net/manual/en/function.PDO-quote.php
	 * @param string string
	 * @param parameter_type int[optional]
	 * @return string a quoted string that is theoretically safe to pass into an
	 */
	public function quote ($string, $parameter_type = null) {}

	final public function __wakeup () {}

	final public function __sleep () {}

	/**
	 * Return an array of available PDO drivers
	 * @link http://php.net/manual/en/function.PDO-getAvailableDrivers.php
	 * @return array 
	 */
	public static function getAvailableDrivers () {}

}

/**
 * Represents a prepared statement and, after the statement is executed, an 
 * associated result set.
 * @link http://php.net/manual/en/ref.pdo.php
 */
class PDOStatement implements Traversable {
	public $queryString;


	/**
	 * Executes a prepared statement
	 * @link http://php.net/manual/en/function.PDOStatement-execute.php
	 * @param input_parameters array[optional]
	 * @return bool 
	 */
	public function execute (array $input_parameters = null) {}

	/**
	 * Fetches the next row from a result set
	 * @link http://php.net/manual/en/function.PDOStatement-fetch.php
	 * @param fetch_style int[optional]
	 * @param cursor_orientation int[optional]
	 * @param cursor_offset int[optional]
	 * @return mixed 
	 */
	public function fetch ($fetch_style = null, $cursor_orientation = null, $cursor_offset = null) {}

	/**
	 * Binds a parameter to the specified variable name
	 * @link http://php.net/manual/en/function.PDOStatement-bindParam.php
	 * @param parameter mixed
	 * @param variable mixed
	 * @param data_type int[optional]
	 * @param length int[optional]
	 * @param driver_options mixed[optional]
	 * @return bool 
	 */
	public function bindParam ($parameter, &$variable, $data_type = null, $length = null, $driver_options = null) {}

	/**
	 * Bind a column to a PHP variable
	 * @link http://php.net/manual/en/function.PDOStatement-bindColumn.php
	 * @param column mixed
	 * @param param mixed
	 * @param type int[optional]
	 * @return bool 
	 */
	public function bindColumn ($column, &$param, $type = null) {}

	/**
	 * Binds a value to a parameter
	 * @link http://php.net/manual/en/function.PDOStatement-bindValue.php
	 * @param parameter mixed
	 * @param value mixed
	 * @param data_type int[optional]
	 * @return bool 
	 */
	public function bindValue ($parameter, $value, $data_type = null) {}

	/**
	 * Returns the number of rows affected by the last SQL statement
	 * @link http://php.net/manual/en/function.PDOStatement-rowCount.php
	 * @return int the number of rows.
	 */
	public function rowCount () {}

	/**
	 * Returns a single column from the next row of a result set
	 * @link http://php.net/manual/en/function.PDOStatement-fetchColumn.php
	 * @param column_number int[optional]
	 * @return string 
	 */
	public function fetchColumn ($column_number = null) {}

	/**
	 * Returns an array containing all of the result set rows
	 * @link http://php.net/manual/en/function.PDOStatement-fetchAll.php
	 * @param fetch_style int[optional]
	 * @param column_index int[optional]
	 * @param ctor_args array[optional]
	 * @return array 
	 */
	public function fetchAll ($fetch_style = null, $column_index = null, array $ctor_args = null) {}

	/**
	 * Fetches the next row and returns it as an object.
	 * @link http://php.net/manual/en/function.PDOStatement-fetchObject.php
	 * @param class_name string[optional]
	 * @param ctor_args array[optional]
	 * @return mixed an instance of the required class with property names that
	 */
	public function fetchObject ($class_name = null, array $ctor_args = null) {}

	/**
	 * Fetch the SQLSTATE associated with the last operation on the statement handle
	 * @link http://php.net/manual/en/function.PDOStatement-errorCode.php
	 * @return string 
	 */
	public function errorCode () {}

	/**
	 * Fetch extended error information associated with the last operation on the statement handle
	 * @link http://php.net/manual/en/function.PDOStatement-errorInfo.php
	 * @return array 
	 */
	public function errorInfo () {}

	/**
	 * Set a statement attribute
	 * @link http://php.net/manual/en/function.PDOStatement-setAttribute.php
	 * @param attribute int
	 * @param value mixed
	 * @return bool 
	 */
	public function setAttribute ($attribute, $value) {}

	/**
	 * Retrieve a statement attribute
	 * @link http://php.net/manual/en/function.PDOStatement-getAttribute.php
	 * @param attribute int
	 * @return mixed the attribute value.
	 */
	public function getAttribute ($attribute) {}

	/**
	 * Returns the number of columns in the result set
	 * @link http://php.net/manual/en/function.PDOStatement-columnCount.php
	 * @return int the number of columns in the result set represented by the
	 */
	public function columnCount () {}

	/**
	 * Returns metadata for a column in a result set
	 * @link http://php.net/manual/en/function.PDOStatement-getColumnMeta.php
	 * @param column int
	 * @return array an associative array containing the following values representing
	 */
	public function getColumnMeta ($column) {}

	/**
	 * Set the default fetch mode for this statement
	 * @link http://php.net/manual/en/function.PDOStatement-setFetchMode.php
	 * @param PDO::FETCH_INTO int
	 * @param object object
	 * @return bool 1 on success or false on failure.
	 */
	public function setFetchMode ($PDO_FETCH_INTO, $object) {}

	/**
	 * Advances to the next rowset in a multi-rowset statement handle
	 * @link http://php.net/manual/en/function.PDOStatement-nextRowset.php
	 * @return bool 
	 */
	public function nextRowset () {}

	/**
	 * Closes the cursor, enabling the statement to be executed again.
	 * @link http://php.net/manual/en/function.PDOStatement-closeCursor.php
	 * @return bool 
	 */
	public function closeCursor () {}

	public function debugDumpParams () {}

	final public function __wakeup () {}

	final public function __sleep () {}

}

final class PDORow  {
}

function pdo_drivers () {}

// End of PDO v.1.0.4dev

// Start of posix v.

/**
 * Send a signal to a process
 * @link http://php.net/manual/en/function.posix-kill.php
 * @param pid int
 * @param sig int
 * @return bool 
 */
function posix_kill ($pid, $sig) {}

/**
 * Return the current process identifier
 * @link http://php.net/manual/en/function.posix-getpid.php
 * @return int the identifier, as an integer.
 */
function posix_getpid () {}

/**
 * Return the parent process identifier
 * @link http://php.net/manual/en/function.posix-getppid.php
 * @return int the identifier, as an integer.
 */
function posix_getppid () {}

/**
 * Return the real user ID of the current process
 * @link http://php.net/manual/en/function.posix-getuid.php
 * @return int the user id, as an integer
 */
function posix_getuid () {}

/**
 * Set the UID of the current process
 * @link http://php.net/manual/en/function.posix-setuid.php
 * @param uid int
 * @return bool 
 */
function posix_setuid ($uid) {}

/**
 * Return the effective user ID of the current process
 * @link http://php.net/manual/en/function.posix-geteuid.php
 * @return int the user id, as an integer
 */
function posix_geteuid () {}

/**
 * Set the effective UID of the current process
 * @link http://php.net/manual/en/function.posix-seteuid.php
 * @param uid int
 * @return bool 
 */
function posix_seteuid ($uid) {}

/**
 * Return the real group ID of the current process
 * @link http://php.net/manual/en/function.posix-getgid.php
 * @return int the real group id, as an integer.
 */
function posix_getgid () {}

/**
 * Set the GID of the current process
 * @link http://php.net/manual/en/function.posix-setgid.php
 * @param gid int
 * @return bool 
 */
function posix_setgid ($gid) {}

/**
 * Return the effective group ID of the current process
 * @link http://php.net/manual/en/function.posix-getegid.php
 * @return int an integer of the effective group ID.
 */
function posix_getegid () {}

/**
 * Set the effective GID of the current process
 * @link http://php.net/manual/en/function.posix-setegid.php
 * @param gid int
 * @return bool 
 */
function posix_setegid ($gid) {}

/**
 * Return the group set of the current process
 * @link http://php.net/manual/en/function.posix-getgroups.php
 * @return array an array of integers containing the numeric group ids of the group
 */
function posix_getgroups () {}

/**
 * Return login name
 * @link http://php.net/manual/en/function.posix-getlogin.php
 * @return string the login name of the user, as a string.
 */
function posix_getlogin () {}

/**
 * Return the current process group identifier
 * @link http://php.net/manual/en/function.posix-getpgrp.php
 * @return int the identifier, as an integer.
 */
function posix_getpgrp () {}

/**
 * Make the current process a session leader
 * @link http://php.net/manual/en/function.posix-setsid.php
 * @return int the session id, or -1 on errors.
 */
function posix_setsid () {}

/**
 * Set process group id for job control
 * @link http://php.net/manual/en/function.posix-setpgid.php
 * @param pid int
 * @param pgid int
 * @return bool 
 */
function posix_setpgid ($pid, $pgid) {}

/**
 * Get process group id for job control
 * @link http://php.net/manual/en/function.posix-getpgid.php
 * @param pid int
 * @return int the identifier, as an integer.
 */
function posix_getpgid ($pid) {}

/**
 * Get the current sid of the process
 * @link http://php.net/manual/en/function.posix-getsid.php
 * @param pid int
 * @return int the identifier, as an integer.
 */
function posix_getsid ($pid) {}

/**
 * Get system name
 * @link http://php.net/manual/en/function.posix-uname.php
 * @return array a hash of strings with information about the
 */
function posix_uname () {}

/**
 * Get process times
 * @link http://php.net/manual/en/function.posix-times.php
 * @return array a hash of strings with information about the current
 */
function posix_times () {}

/**
 * Get path name of controlling terminal
 * @link http://php.net/manual/en/function.posix-ctermid.php
 * @return string 
 */
function posix_ctermid () {}

/**
 * Determine terminal device name
 * @link http://php.net/manual/en/function.posix-ttyname.php
 * @param fd int
 * @return string 
 */
function posix_ttyname ($fd) {}

/**
 * Determine if a file descriptor is an interactive terminal
 * @link http://php.net/manual/en/function.posix-isatty.php
 * @param fd int
 * @return bool true if fd is an open descriptor connected
 */
function posix_isatty ($fd) {}

/**
 * Pathname of current directory
 * @link http://php.net/manual/en/function.posix-getcwd.php
 * @return string a string of the absolute pathname on success.
 */
function posix_getcwd () {}

/**
 * Create a fifo special file (a named pipe)
 * @link http://php.net/manual/en/function.posix-mkfifo.php
 * @param pathname string
 * @param mode int
 * @return bool 
 */
function posix_mkfifo ($pathname, $mode) {}

/**
 * Create a special or ordinary file (POSIX.1)
 * @link http://php.net/manual/en/function.posix-mknod.php
 * @param pathname string
 * @param mode int
 * @param major int[optional]
 * @param minor int[optional]
 * @return bool 
 */
function posix_mknod ($pathname, $mode, $major = null, $minor = null) {}

/**
 * Determine accessibility of a file
 * @link http://php.net/manual/en/function.posix-access.php
 * @param file string
 * @param mode int[optional]
 * @return bool 
 */
function posix_access ($file, $mode = null) {}

/**
 * Return info about a group by name
 * @link http://php.net/manual/en/function.posix-getgrnam.php
 * @param name string
 * @return array 
 */
function posix_getgrnam ($name) {}

/**
 * Return info about a group by group id
 * @link http://php.net/manual/en/function.posix-getgrgid.php
 * @param gid int
 * @return array 
 */
function posix_getgrgid ($gid) {}

/**
 * Return info about a user by username
 * @link http://php.net/manual/en/function.posix-getpwnam.php
 * @param username string
 * @return array 
 */
function posix_getpwnam ($username) {}

/**
 * Return info about a user by user id
 * @link http://php.net/manual/en/function.posix-getpwuid.php
 * @param uid int
 * @return array an associative array with the following elements:
 */
function posix_getpwuid ($uid) {}

/**
 * Return info about system resource limits
 * @link http://php.net/manual/en/function.posix-getrlimit.php
 * @return array an associative array of elements for each
 */
function posix_getrlimit () {}

/**
 * Retrieve the error number set by the last posix function that failed
 * @link http://php.net/manual/en/function.posix-get-last-error.php
 * @return int the errno (error number) set by the last posix function that
 */
function posix_get_last_error () {}

function posix_errno () {}

/**
 * Retrieve the system error message associated with the given errno
 * @link http://php.net/manual/en/function.posix-strerror.php
 * @param errno int
 * @return string the error message, as a string.
 */
function posix_strerror ($errno) {}

/**
 * Calculate the group access list
 * @link http://php.net/manual/en/function.posix-initgroups.php
 * @param name string
 * @param base_group_id int
 * @return bool 
 */
function posix_initgroups ($name, $base_group_id) {}


/**
 * Check whether the file exists.
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_F_OK', 0);

/**
 * Check whether the file exists and has execute permissions.
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_X_OK', 1);

/**
 * Check whether the file exists and has write permissions.
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_W_OK', 2);

/**
 * Check whether the file exists and has read permissions.
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_R_OK', 4);

/**
 * Normal file
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_S_IFREG', 32768);

/**
 * Character special file
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_S_IFCHR', 8192);

/**
 * Block special file
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_S_IFBLK', 24576);

/**
 * FIFO (named pipe) special file
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_S_IFIFO', 4096);

/**
 * Socket
 * @link http://php.net/manual/en/posix.constants.php
 */
define ('POSIX_S_IFSOCK', 49152);

// End of posix v.

// Start of sockets v.

/**
 * Runs the select() system call on the given arrays of sockets with a specified timeout
 * @link http://php.net/manual/en/function.socket-select.php
 * @param read array
 * @param write array
 * @param except array
 * @param tv_sec int
 * @param tv_usec int[optional]
 * @return int 
 */
function socket_select (array &$read, array &$write, array &$except, $tv_sec, $tv_usec = null) {}

/**
 * Create a socket (endpoint for communication)
 * @link http://php.net/manual/en/function.socket-create.php
 * @param domain int
 * @param type int
 * @param protocol int
 * @return resource 
 */
function socket_create ($domain, $type, $protocol) {}

/**
 * Opens a socket on port to accept connections
 * @link http://php.net/manual/en/function.socket-create-listen.php
 * @param port int
 * @param backlog int[optional]
 * @return resource 
 */
function socket_create_listen ($port, $backlog = null) {}

/**
 * Creates a pair of indistinguishable sockets and stores them in an array
 * @link http://php.net/manual/en/function.socket-create-pair.php
 * @param domain int
 * @param type int
 * @param protocol int
 * @param fd array
 * @return bool 
 */
function socket_create_pair ($domain, $type, $protocol, array &$fd) {}

/**
 * Accepts a connection on a socket
 * @link http://php.net/manual/en/function.socket-accept.php
 * @param socket resource
 * @return resource a new socket resource on success, or false on error. The actual
 */
function socket_accept ($socket) {}

/**
 * Sets nonblocking mode for file descriptor fd
 * @link http://php.net/manual/en/function.socket-set-nonblock.php
 * @param socket resource
 * @return bool 
 */
function socket_set_nonblock ($socket) {}

/**
 * Sets blocking mode on a socket resource
 * @link http://php.net/manual/en/function.socket-set-block.php
 * @param socket resource
 * @return bool 
 */
function socket_set_block ($socket) {}

/**
 * Listens for a connection on a socket
 * @link http://php.net/manual/en/function.socket-listen.php
 * @param socket resource
 * @param backlog int[optional]
 * @return bool 
 */
function socket_listen ($socket, $backlog = null) {}

/**
 * Closes a socket resource
 * @link http://php.net/manual/en/function.socket-close.php
 * @param socket resource
 * @return void 
 */
function socket_close ($socket) {}

/**
 * Write to a socket
 * @link http://php.net/manual/en/function.socket-write.php
 * @param socket resource
 * @param buffer string
 * @param length int[optional]
 * @return int the number of bytes successfully written to the socket or false
 */
function socket_write ($socket, $buffer, $length = null) {}

/**
 * Reads a maximum of length bytes from a socket
 * @link http://php.net/manual/en/function.socket-read.php
 * @param socket resource
 * @param length int
 * @param type int[optional]
 * @return string 
 */
function socket_read ($socket, $length, $type = null) {}

/**
 * Queries the local side of the given socket which may either result in host/port or in a Unix filesystem path, dependent on its type
 * @link http://php.net/manual/en/function.socket-getsockname.php
 * @param socket resource
 * @param addr string
 * @param port int[optional]
 * @return bool 
 */
function socket_getsockname ($socket, &$addr, &$port = null) {}

/**
 * Queries the remote side of the given socket which may either result in host/port or in a Unix filesystem path, dependent on its type
 * @link http://php.net/manual/en/function.socket-getpeername.php
 * @param socket resource
 * @param address string
 * @param port int[optional]
 * @return bool 
 */
function socket_getpeername ($socket, &$address, &$port = null) {}

/**
 * Initiates a connection on a socket
 * @link http://php.net/manual/en/function.socket-connect.php
 * @param socket resource
 * @param address string
 * @param port int[optional]
 * @return bool 
 */
function socket_connect ($socket, $address, $port = null) {}

/**
 * Return a string describing a socket error
 * @link http://php.net/manual/en/function.socket-strerror.php
 * @param errno int
 * @return string the error message associated with the errno
 */
function socket_strerror ($errno) {}

/**
 * Binds a name to a socket
 * @link http://php.net/manual/en/function.socket-bind.php
 * @param socket resource
 * @param address string
 * @param port int[optional]
 * @return bool 
 */
function socket_bind ($socket, $address, $port = null) {}

/**
 * Receives data from a connected socket
 * @link http://php.net/manual/en/function.socket-recv.php
 * @param socket resource
 * @param buf string
 * @param len int
 * @param flags int
 * @return int 
 */
function socket_recv ($socket, &$buf, $len, $flags) {}

/**
 * Sends data to a connected socket
 * @link http://php.net/manual/en/function.socket-send.php
 * @param socket resource
 * @param buf string
 * @param len int
 * @param flags int
 * @return int 
 */
function socket_send ($socket, $buf, $len, $flags) {}

/**
 * Receives data from a socket whether or not it is connection-oriented
 * @link http://php.net/manual/en/function.socket-recvfrom.php
 * @param socket resource
 * @param buf string
 * @param len int
 * @param flags int
 * @param name string
 * @param port int[optional]
 * @return int 
 */
function socket_recvfrom ($socket, &$buf, $len, $flags, &$name, &$port = null) {}

/**
 * Sends a message to a socket, whether it is connected or not
 * @link http://php.net/manual/en/function.socket-sendto.php
 * @param socket resource
 * @param buf string
 * @param len int
 * @param flags int
 * @param addr string
 * @param port int[optional]
 * @return int 
 */
function socket_sendto ($socket, $buf, $len, $flags, $addr, $port = null) {}

/**
 * Gets socket options for the socket
 * @link http://php.net/manual/en/function.socket-get-option.php
 * @param socket resource
 * @param level int
 * @param optname int
 * @return mixed the value of the given option, or false on errors.
 */
function socket_get_option ($socket, $level, $optname) {}

/**
 * Sets socket options for the socket
 * @link http://php.net/manual/en/function.socket-set-option.php
 * @param socket resource
 * @param level int
 * @param optname int
 * @param optval mixed
 * @return bool 
 */
function socket_set_option ($socket, $level, $optname, $optval) {}

/**
 * Shuts down a socket for receiving, sending, or both
 * @link http://php.net/manual/en/function.socket-shutdown.php
 * @param socket resource
 * @param how int[optional]
 * @return bool 
 */
function socket_shutdown ($socket, $how = null) {}

/**
 * Returns the last error on the socket
 * @link http://php.net/manual/en/function.socket-last-error.php
 * @param socket resource[optional]
 * @return int 
 */
function socket_last_error ($socket = null) {}

/**
 * Clears the error on the socket or the last error code
 * @link http://php.net/manual/en/function.socket-clear-error.php
 * @param socket resource[optional]
 * @return void 
 */
function socket_clear_error ($socket = null) {}

function socket_getopt () {}

function socket_setopt () {}

define ('AF_UNIX', 1);
define ('AF_INET', 2);
define ('AF_INET6', 10);
define ('SOCK_STREAM', 1);
define ('SOCK_DGRAM', 2);
define ('SOCK_RAW', 3);
define ('SOCK_SEQPACKET', 5);
define ('SOCK_RDM', 4);
define ('MSG_OOB', 1);
define ('MSG_WAITALL', 256);
define ('MSG_PEEK', 2);
define ('MSG_DONTROUTE', 4);
define ('MSG_EOR', 128);
define ('MSG_EOF', 512);
define ('SO_DEBUG', 1);
define ('SO_REUSEADDR', 2);
define ('SO_KEEPALIVE', 9);
define ('SO_DONTROUTE', 5);
define ('SO_LINGER', 13);
define ('SO_BROADCAST', 6);
define ('SO_OOBINLINE', 10);
define ('SO_SNDBUF', 7);
define ('SO_RCVBUF', 8);
define ('SO_SNDLOWAT', 19);
define ('SO_RCVLOWAT', 18);
define ('SO_SNDTIMEO', 21);
define ('SO_RCVTIMEO', 20);
define ('SO_TYPE', 3);
define ('SO_ERROR', 4);
define ('SOL_SOCKET', 1);
define ('SOMAXCONN', 128);
define ('PHP_NORMAL_READ', 1);
define ('PHP_BINARY_READ', 2);
define ('SOCKET_EPERM', 1);
define ('SOCKET_ENOENT', 2);
define ('SOCKET_EINTR', 4);
define ('SOCKET_EIO', 5);
define ('SOCKET_ENXIO', 6);
define ('SOCKET_E2BIG', 7);
define ('SOCKET_EBADF', 9);
define ('SOCKET_EAGAIN', 11);
define ('SOCKET_ENOMEM', 12);
define ('SOCKET_EACCES', 13);
define ('SOCKET_EFAULT', 14);
define ('SOCKET_ENOTBLK', 15);
define ('SOCKET_EBUSY', 16);
define ('SOCKET_EEXIST', 17);
define ('SOCKET_EXDEV', 18);
define ('SOCKET_ENODEV', 19);
define ('SOCKET_ENOTDIR', 20);
define ('SOCKET_EISDIR', 21);
define ('SOCKET_EINVAL', 22);
define ('SOCKET_ENFILE', 23);
define ('SOCKET_EMFILE', 24);
define ('SOCKET_ENOTTY', 25);
define ('SOCKET_ENOSPC', 28);
define ('SOCKET_ESPIPE', 29);
define ('SOCKET_EROFS', 30);
define ('SOCKET_EMLINK', 31);
define ('SOCKET_EPIPE', 32);
define ('SOCKET_ENAMETOOLONG', 36);
define ('SOCKET_ENOLCK', 37);
define ('SOCKET_ENOSYS', 38);
define ('SOCKET_ENOTEMPTY', 39);
define ('SOCKET_ELOOP', 40);
define ('SOCKET_EWOULDBLOCK', 11);
define ('SOCKET_ENOMSG', 42);
define ('SOCKET_EIDRM', 43);
define ('SOCKET_ECHRNG', 44);
define ('SOCKET_EL2NSYNC', 45);
define ('SOCKET_EL3HLT', 46);
define ('SOCKET_EL3RST', 47);
define ('SOCKET_ELNRNG', 48);
define ('SOCKET_EUNATCH', 49);
define ('SOCKET_ENOCSI', 50);
define ('SOCKET_EL2HLT', 51);
define ('SOCKET_EBADE', 52);
define ('SOCKET_EBADR', 53);
define ('SOCKET_EXFULL', 54);
define ('SOCKET_ENOANO', 55);
define ('SOCKET_EBADRQC', 56);
define ('SOCKET_EBADSLT', 57);
define ('SOCKET_ENOSTR', 60);
define ('SOCKET_ENODATA', 61);
define ('SOCKET_ETIME', 62);
define ('SOCKET_ENOSR', 63);
define ('SOCKET_ENONET', 64);
define ('SOCKET_EREMOTE', 66);
define ('SOCKET_ENOLINK', 67);
define ('SOCKET_EADV', 68);
define ('SOCKET_ESRMNT', 69);
define ('SOCKET_ECOMM', 70);
define ('SOCKET_EPROTO', 71);
define ('SOCKET_EMULTIHOP', 72);
define ('SOCKET_EBADMSG', 74);
define ('SOCKET_ENOTUNIQ', 76);
define ('SOCKET_EBADFD', 77);
define ('SOCKET_EREMCHG', 78);
define ('SOCKET_ERESTART', 85);
define ('SOCKET_ESTRPIPE', 86);
define ('SOCKET_EUSERS', 87);
define ('SOCKET_ENOTSOCK', 88);
define ('SOCKET_EDESTADDRREQ', 89);
define ('SOCKET_EMSGSIZE', 90);
define ('SOCKET_EPROTOTYPE', 91);
define ('SOCKET_ENOPROTOOPT', 92);
define ('SOCKET_EPROTONOSUPPORT', 93);
define ('SOCKET_ESOCKTNOSUPPORT', 94);
define ('SOCKET_EOPNOTSUPP', 95);
define ('SOCKET_EPFNOSUPPORT', 96);
define ('SOCKET_EAFNOSUPPORT', 97);
define ('SOCKET_EADDRINUSE', 98);
define ('SOCKET_EADDRNOTAVAIL', 99);
define ('SOCKET_ENETDOWN', 100);
define ('SOCKET_ENETUNREACH', 101);
define ('SOCKET_ENETRESET', 102);
define ('SOCKET_ECONNABORTED', 103);
define ('SOCKET_ECONNRESET', 104);
define ('SOCKET_ENOBUFS', 105);
define ('SOCKET_EISCONN', 106);
define ('SOCKET_ENOTCONN', 107);
define ('SOCKET_ESHUTDOWN', 108);
define ('SOCKET_ETOOMANYREFS', 109);
define ('SOCKET_ETIMEDOUT', 110);
define ('SOCKET_ECONNREFUSED', 111);
define ('SOCKET_EHOSTDOWN', 112);
define ('SOCKET_EHOSTUNREACH', 113);
define ('SOCKET_EALREADY', 114);
define ('SOCKET_EINPROGRESS', 115);
define ('SOCKET_EISNAM', 120);
define ('SOCKET_EREMOTEIO', 121);
define ('SOCKET_EDQUOT', 122);
define ('SOCKET_ENOMEDIUM', 123);
define ('SOCKET_EMEDIUMTYPE', 124);
define ('SOL_TCP', 6);
define ('SOL_UDP', 17);

// End of sockets v.

// Start of sysvsem v.

/**
 * Get a semaphore id
 * @link http://php.net/manual/en/function.sem-get.php
 * @param key int
 * @param max_acquire int[optional]
 * @param perm int[optional]
 * @param auto_release int[optional]
 * @return resource a positive semaphore identifier on success, or false on
 */
function sem_get ($key, $max_acquire = null, $perm = null, $auto_release = null) {}

/**
 * Acquire a semaphore
 * @link http://php.net/manual/en/function.sem-acquire.php
 * @param sem_identifier resource
 * @return bool 
 */
function sem_acquire ($sem_identifier) {}

/**
 * Release a semaphore
 * @link http://php.net/manual/en/function.sem-release.php
 * @param sem_identifier resource
 * @return bool 
 */
function sem_release ($sem_identifier) {}

/**
 * Remove a semaphore
 * @link http://php.net/manual/en/function.sem-remove.php
 * @param sem_identifier resource
 * @return bool 
 */
function sem_remove ($sem_identifier) {}

// End of sysvsem v.

// Start of tokenizer v.0.1

/**
 * Split given source into PHP tokens
 * @link http://php.net/manual/en/function.token-get-all.php
 * @param source string
 * @return array 
 */
function token_get_all ($source) {}

/**
 * Get the symbolic name of a given PHP token
 * @link http://php.net/manual/en/function.token-name.php
 * @param token int
 * @return string 
 */
function token_name ($token) {}

define ('T_REQUIRE_ONCE', 258);
define ('T_REQUIRE', 259);
define ('T_EVAL', 260);
define ('T_INCLUDE_ONCE', 261);
define ('T_INCLUDE', 262);
define ('T_LOGICAL_OR', 263);
define ('T_LOGICAL_XOR', 264);
define ('T_LOGICAL_AND', 265);
define ('T_PRINT', 266);
define ('T_SR_EQUAL', 267);
define ('T_SL_EQUAL', 268);
define ('T_XOR_EQUAL', 269);
define ('T_OR_EQUAL', 270);
define ('T_AND_EQUAL', 271);
define ('T_MOD_EQUAL', 272);
define ('T_CONCAT_EQUAL', 273);
define ('T_DIV_EQUAL', 274);
define ('T_MUL_EQUAL', 275);
define ('T_MINUS_EQUAL', 276);
define ('T_PLUS_EQUAL', 277);
define ('T_BOOLEAN_OR', 278);
define ('T_BOOLEAN_AND', 279);
define ('T_IS_NOT_IDENTICAL', 280);
define ('T_IS_IDENTICAL', 281);
define ('T_IS_NOT_EQUAL', 282);
define ('T_IS_EQUAL', 283);
define ('T_IS_GREATER_OR_EQUAL', 284);
define ('T_IS_SMALLER_OR_EQUAL', 285);
define ('T_SR', 286);
define ('T_SL', 287);
define ('T_INSTANCEOF', 288);
define ('T_UNSET_CAST', 289);
define ('T_BOOL_CAST', 290);
define ('T_OBJECT_CAST', 291);
define ('T_ARRAY_CAST', 292);
define ('T_STRING_CAST', 293);
define ('T_DOUBLE_CAST', 294);
define ('T_INT_CAST', 295);
define ('T_DEC', 296);
define ('T_INC', 297);
define ('T_CLONE', 298);
define ('T_NEW', 299);
define ('T_EXIT', 300);
define ('T_IF', 301);
define ('T_ELSEIF', 302);
define ('T_ELSE', 303);
define ('T_ENDIF', 304);
define ('T_LNUMBER', 305);
define ('T_DNUMBER', 306);
define ('T_STRING', 307);
define ('T_STRING_VARNAME', 308);
define ('T_VARIABLE', 309);
define ('T_NUM_STRING', 310);
define ('T_INLINE_HTML', 311);
define ('T_CHARACTER', 312);
define ('T_BAD_CHARACTER', 313);
define ('T_ENCAPSED_AND_WHITESPACE', 314);
define ('T_CONSTANT_ENCAPSED_STRING', 315);
define ('T_ECHO', 316);
define ('T_DO', 317);
define ('T_WHILE', 318);
define ('T_ENDWHILE', 319);
define ('T_FOR', 320);
define ('T_ENDFOR', 321);
define ('T_FOREACH', 322);
define ('T_ENDFOREACH', 323);
define ('T_DECLARE', 324);
define ('T_ENDDECLARE', 325);
define ('T_AS', 326);
define ('T_SWITCH', 327);
define ('T_ENDSWITCH', 328);
define ('T_CASE', 329);
define ('T_DEFAULT', 330);
define ('T_BREAK', 331);
define ('T_CONTINUE', 332);
define ('T_FUNCTION', 333);
define ('T_CONST', 334);
define ('T_RETURN', 335);
define ('T_TRY', 336);
define ('T_CATCH', 337);
define ('T_THROW', 338);
define ('T_USE', 339);
define ('T_GLOBAL', 340);
define ('T_PUBLIC', 341);
define ('T_PROTECTED', 342);
define ('T_PRIVATE', 343);
define ('T_FINAL', 344);
define ('T_ABSTRACT', 345);
define ('T_STATIC', 346);
define ('T_VAR', 347);
define ('T_UNSET', 348);
define ('T_ISSET', 349);
define ('T_EMPTY', 350);
define ('T_HALT_COMPILER', 351);
define ('T_CLASS', 352);
define ('T_INTERFACE', 353);
define ('T_EXTENDS', 354);
define ('T_IMPLEMENTS', 355);
define ('T_OBJECT_OPERATOR', 356);
define ('T_DOUBLE_ARROW', 357);
define ('T_LIST', 358);
define ('T_ARRAY', 359);
define ('T_CLASS_C', 360);
define ('T_METHOD_C', 361);
define ('T_FUNC_C', 362);
define ('T_LINE', 363);
define ('T_FILE', 364);
define ('T_COMMENT', 365);
define ('T_DOC_COMMENT', 366);
define ('T_OPEN_TAG', 367);
define ('T_OPEN_TAG_WITH_ECHO', 368);
define ('T_CLOSE_TAG', 369);
define ('T_WHITESPACE', 370);
define ('T_START_HEREDOC', 371);
define ('T_END_HEREDOC', 372);
define ('T_DOLLAR_OPEN_CURLY_BRACES', 373);
define ('T_CURLY_OPEN', 374);
define ('T_PAAMAYIM_NEKUDOTAYIM', 375);
define ('T_DOUBLE_COLON', 375);

// End of tokenizer v.0.1

// Start of xsl v.0.1

/**
 * @link http://php.net/manual/en/ref.xsl.php
 */
class XSLTProcessor  {

	/**
	 * Import stylesheet
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-import-stylesheet.php
	 * @param stylesheet DOMDocument
	 * @return void 
	 */
	public function importStylesheet (DOMDocument $stylesheet) {}

	/**
	 * Transform to a DOMDocument
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-transform-to-doc.php
	 * @param doc DOMNode
	 * @return DOMDocument 
	 */
	public function transformToDoc (DOMNode $doc) {}

	/**
	 * Transform to URI
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-transform-to-uri.php
	 * @param doc DOMDocument
	 * @param uri string
	 * @return int the number of bytes written or false if an error occurred.
	 */
	public function transformToUri (DOMDocument $doc, $uri) {}

	/**
	 * Transform to XML
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-transform-to-xml.php
	 * @param doc DOMDocument
	 * @return string 
	 */
	public function transformToXml (DOMDocument $doc) {}

	/**
	 * Set value for a parameter
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-set-parameter.php
	 * @param namespace string
	 * @param options array
	 * @return bool 
	 */
	public function setParameter ($namespace, array $options) {}

	/**
	 * Get value of a parameter
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-get-parameter.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return string 
	 */
	public function getParameter ($namespaceURI, $localName) {}

	/**
	 * Remove parameter
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-remove-parameter.php
	 * @param namespaceURI string
	 * @param localName string
	 * @return bool 
	 */
	public function removeParameter ($namespaceURI, $localName) {}

	/**
	 * Determine if PHP has EXSLT support
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-has-exslt-support.php
	 * @return bool 
	 */
	public function hasExsltSupport () {}

	/**
	 * Enables the ability to use PHP functions as XSLT functions
	 * @link http://php.net/manual/en/function.xsl-xsltprocessor-register-php-functions.php
	 * @param restrict mixed[optional]
	 * @return void 
	 */
	public function registerPHPFunctions ($restrict = null) {}

}
define ('XSL_CLONE_AUTO', 0);
define ('XSL_CLONE_NEVER', -1);
define ('XSL_CLONE_ALWAYS', 1);

/**
 * libxslt version like 10117. Available as of PHP 5.1.2.
 * @link http://php.net/manual/en/xsl.constants.php
 */
define ('LIBXSLT_VERSION', 10117);

/**
 * libxslt version like 1.1.17. Available as of PHP 5.1.2.
 * @link http://php.net/manual/en/xsl.constants.php
 */
define ('LIBXSLT_DOTTED_VERSION', "1.1.17");

/**
 * libexslt version like 813. Available as of PHP 5.1.2.
 * @link http://php.net/manual/en/xsl.constants.php
 */
define ('LIBEXSLT_VERSION', 813);

/**
 * libexslt version like 1.1.17. Available as of PHP 5.1.2.
 * @link http://php.net/manual/en/xsl.constants.php
 */
define ('LIBEXSLT_DOTTED_VERSION', "1.1.17");

// End of xsl v.0.1

// Start of bz2 v.

/**
 * Opens a bzip2 compressed file
 * @link http://php.net/manual/en/function.bzopen.php
 * @param filename string
 * @param mode string
 * @return resource 
 */
function bzopen ($filename, $mode) {}

/**
 * Binary safe bzip2 file read
 * @link http://php.net/manual/en/function.bzread.php
 * @param bz resource
 * @param length int[optional]
 * @return string the uncompressed data, or false on error.
 */
function bzread ($bz, $length = null) {}

/**
 * Binary safe bzip2 file write
 * @link http://php.net/manual/en/function.bzwrite.php
 * @param bz resource
 * @param data string
 * @param length int[optional]
 * @return int the number of bytes written, or false on error.
 */
function bzwrite ($bz, $data, $length = null) {}

/**
 * Force a write of all buffered data
 * @link http://php.net/manual/en/function.bzflush.php
 * @param bz resource
 * @return int 
 */
function bzflush ($bz) {}

/**
 * Close a bzip2 file
 * @link http://php.net/manual/en/function.bzclose.php
 * @param bz resource
 * @return int 
 */
function bzclose ($bz) {}

/**
 * Returns a bzip2 error number
 * @link http://php.net/manual/en/function.bzerrno.php
 * @param bz resource
 * @return int the error number as an integer.
 */
function bzerrno ($bz) {}

/**
 * Returns a bzip2 error string
 * @link http://php.net/manual/en/function.bzerrstr.php
 * @param bz resource
 * @return string a string containing the error message.
 */
function bzerrstr ($bz) {}

/**
 * Returns the bzip2 error number and error string in an array
 * @link http://php.net/manual/en/function.bzerror.php
 * @param bz resource
 * @return array an associative array, with the error code in the
 */
function bzerror ($bz) {}

/**
 * Compress a string into bzip2 encoded data
 * @link http://php.net/manual/en/function.bzcompress.php
 * @param source string
 * @param blocksize int[optional]
 * @param workfactor int[optional]
 * @return mixed 
 */
function bzcompress ($source, $blocksize = null, $workfactor = null) {}

/**
 * Decompresses bzip2 encoded data
 * @link http://php.net/manual/en/function.bzdecompress.php
 * @param source string
 * @param small int[optional]
 * @return mixed 
 */
function bzdecompress ($source, $small = null) {}

// End of bz2 v.

// Start of gd v.

/**
 * Retrieve information about the currently installed GD library
 * @link http://php.net/manual/en/function.gd-info.php
 * @return array an associative array.
 */
function gd_info () {}

/**
 * Draws an arc
 * @link http://php.net/manual/en/function.imagearc.php
 * @param image resource
 * @param cx int
 * @param cy int
 * @param width int
 * @param height int
 * @param start int
 * @param end int
 * @param color int
 * @return bool 
 */
function imagearc ($image, $cx, $cy, $width, $height, $start, $end, $color) {}

/**
 * Draw an ellipse
 * @link http://php.net/manual/en/function.imageellipse.php
 * @param image resource
 * @param cx int
 * @param cy int
 * @param width int
 * @param height int
 * @param color int
 * @return bool 
 */
function imageellipse ($image, $cx, $cy, $width, $height, $color) {}

/**
 * Draw a character horizontally
 * @link http://php.net/manual/en/function.imagechar.php
 * @param image resource
 * @param font int
 * @param x int
 * @param y int
 * @param c string
 * @param color int
 * @return bool 
 */
function imagechar ($image, $font, $x, $y, $c, $color) {}

/**
 * Draw a character vertically
 * @link http://php.net/manual/en/function.imagecharup.php
 * @param image resource
 * @param font int
 * @param x int
 * @param y int
 * @param c string
 * @param color int
 * @return bool 
 */
function imagecharup ($image, $font, $x, $y, $c, $color) {}

/**
 * Get the index of the color of a pixel
 * @link http://php.net/manual/en/function.imagecolorat.php
 * @param image resource
 * @param x int
 * @param y int
 * @return int the index of the color.
 */
function imagecolorat ($image, $x, $y) {}

/**
 * Allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolorallocate.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @return int 
 */
function imagecolorallocate ($image, $red, $green, $blue) {}

/**
 * Copy the palette from one image to another
 * @link http://php.net/manual/en/function.imagepalettecopy.php
 * @param destination resource
 * @param source resource
 * @return void 
 */
function imagepalettecopy ($destination, $source) {}

/**
 * Create a new image from the image stream in the string
 * @link http://php.net/manual/en/function.imagecreatefromstring.php
 * @param data string
 * @return resource 
 */
function imagecreatefromstring ($data) {}

/**
 * Get the index of the closest color to the specified color
 * @link http://php.net/manual/en/function.imagecolorclosest.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @return int the index of the closest color, in the palette of the image, to
 */
function imagecolorclosest ($image, $red, $green, $blue) {}

/**
 * Get the index of the color which has the hue, white and blackness nearest to the given color
 * @link http://php.net/manual/en/function.imagecolorclosesthwb.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @return int 
 */
function imagecolorclosesthwb ($image, $red, $green, $blue) {}

/**
 * De-allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolordeallocate.php
 * @param image resource
 * @param color int
 * @return bool 
 */
function imagecolordeallocate ($image, $color) {}

/**
 * Get the index of the specified color or its closest possible alternative
 * @link http://php.net/manual/en/function.imagecolorresolve.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @return int a color index.
 */
function imagecolorresolve ($image, $red, $green, $blue) {}

/**
 * Get the index of the specified color
 * @link http://php.net/manual/en/function.imagecolorexact.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @return int the index of the specified color in the palette, or -1 if the
 */
function imagecolorexact ($image, $red, $green, $blue) {}

/**
 * Set the color for the specified palette index
 * @link http://php.net/manual/en/function.imagecolorset.php
 * @param image resource
 * @param index int
 * @param red int
 * @param green int
 * @param blue int
 * @return void 
 */
function imagecolorset ($image, $index, $red, $green, $blue) {}

/**
 * Define a color as transparent
 * @link http://php.net/manual/en/function.imagecolortransparent.php
 * @param image resource
 * @param color int[optional]
 * @return int 
 */
function imagecolortransparent ($image, $color = null) {}

/**
 * Find out the number of colors in an image's palette
 * @link http://php.net/manual/en/function.imagecolorstotal.php
 * @param image resource
 * @return int the number of colors in the specified image's palette or 0 for
 */
function imagecolorstotal ($image) {}

/**
 * Get the colors for an index
 * @link http://php.net/manual/en/function.imagecolorsforindex.php
 * @param image resource
 * @param index int
 * @return array an associative array with red, green, blue and alpha keys that
 */
function imagecolorsforindex ($image, $index) {}

/**
 * Copy part of an image
 * @link http://php.net/manual/en/function.imagecopy.php
 * @param dst_im resource
 * @param src_im resource
 * @param dst_x int
 * @param dst_y int
 * @param src_x int
 * @param src_y int
 * @param src_w int
 * @param src_h int
 * @return bool 
 */
function imagecopy ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h) {}

/**
 * Copy and merge part of an image
 * @link http://php.net/manual/en/function.imagecopymerge.php
 * @param dst_im resource
 * @param src_im resource
 * @param dst_x int
 * @param dst_y int
 * @param src_x int
 * @param src_y int
 * @param src_w int
 * @param src_h int
 * @param pct int
 * @return bool 
 */
function imagecopymerge ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h, $pct) {}

/**
 * Copy and merge part of an image with gray scale
 * @link http://php.net/manual/en/function.imagecopymergegray.php
 * @param dst_im resource
 * @param src_im resource
 * @param dst_x int
 * @param dst_y int
 * @param src_x int
 * @param src_y int
 * @param src_w int
 * @param src_h int
 * @param pct int
 * @return bool 
 */
function imagecopymergegray ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h, $pct) {}

/**
 * Copy and resize part of an image
 * @link http://php.net/manual/en/function.imagecopyresized.php
 * @param dst_image resource
 * @param src_image resource
 * @param dst_x int
 * @param dst_y int
 * @param src_x int
 * @param src_y int
 * @param dst_w int
 * @param dst_h int
 * @param src_w int
 * @param src_h int
 * @return bool 
 */
function imagecopyresized ($dst_image, $src_image, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h) {}

/**
 * Create a new palette based image
 * @link http://php.net/manual/en/function.imagecreate.php
 * @param width int
 * @param height int
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreate ($width, $height) {}

/**
 * Create a new true color image
 * @link http://php.net/manual/en/function.imagecreatetruecolor.php
 * @param width int
 * @param height int
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatetruecolor ($width, $height) {}

/**
 * Finds whether an image is a truecolor image
 * @link http://php.net/manual/en/function.imageistruecolor.php
 * @param image resource
 * @return bool true if the image is truecolor, false
 */
function imageistruecolor ($image) {}

/**
 * Convert a true color image to a palette image
 * @link http://php.net/manual/en/function.imagetruecolortopalette.php
 * @param image resource
 * @param dither bool
 * @param ncolors int
 * @return bool 
 */
function imagetruecolortopalette ($image, $dither, $ncolors) {}

/**
 * Set the thickness for line drawing
 * @link http://php.net/manual/en/function.imagesetthickness.php
 * @param image resource
 * @param thickness int
 * @return bool 
 */
function imagesetthickness ($image, $thickness) {}

/**
 * Draw a partial ellipse and fill it
 * @link http://php.net/manual/en/function.imagefilledarc.php
 * @param image resource
 * @param cx int
 * @param cy int
 * @param width int
 * @param height int
 * @param start int
 * @param end int
 * @param color int
 * @param style int
 * @return bool 
 */
function imagefilledarc ($image, $cx, $cy, $width, $height, $start, $end, $color, $style) {}

/**
 * Draw a filled ellipse
 * @link http://php.net/manual/en/function.imagefilledellipse.php
 * @param image resource
 * @param cx int
 * @param cy int
 * @param width int
 * @param height int
 * @param color int
 * @return bool 
 */
function imagefilledellipse ($image, $cx, $cy, $width, $height, $color) {}

/**
 * Set the blending mode for an image
 * @link http://php.net/manual/en/function.imagealphablending.php
 * @param image resource
 * @param blendmode bool
 * @return bool 
 */
function imagealphablending ($image, $blendmode) {}

/**
 * Set the flag to save full alpha channel information (as opposed to single-color transparency) when saving PNG images
 * @link http://php.net/manual/en/function.imagesavealpha.php
 * @param image resource
 * @param saveflag bool
 * @return bool 
 */
function imagesavealpha ($image, $saveflag) {}

/**
 * Allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolorallocatealpha.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @param alpha int
 * @return int 
 */
function imagecolorallocatealpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the specified color + alpha or its closest possible alternative
 * @link http://php.net/manual/en/function.imagecolorresolvealpha.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @param alpha int
 * @return int a color index.
 */
function imagecolorresolvealpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the closest color to the specified color + alpha
 * @link http://php.net/manual/en/function.imagecolorclosestalpha.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @param alpha int
 * @return int the index of the closest color in the palette.
 */
function imagecolorclosestalpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the specified color + alpha
 * @link http://php.net/manual/en/function.imagecolorexactalpha.php
 * @param image resource
 * @param red int
 * @param green int
 * @param blue int
 * @param alpha int
 * @return int the index of the specified color+alpha in the palette of the
 */
function imagecolorexactalpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Copy and resize part of an image with resampling
 * @link http://php.net/manual/en/function.imagecopyresampled.php
 * @param dst_image resource
 * @param src_image resource
 * @param dst_x int
 * @param dst_y int
 * @param src_x int
 * @param src_y int
 * @param dst_w int
 * @param dst_h int
 * @param src_w int
 * @param src_h int
 * @return bool 
 */
function imagecopyresampled ($dst_image, $src_image, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h) {}

/**
 * Rotate an image with a given angle
 * @link http://php.net/manual/en/function.imagerotate.php
 * @param source_image resource
 * @param angle float
 * @param bgd_color int
 * @param ignore_transparent int[optional]
 * @return resource 
 */
function imagerotate ($source_image, $angle, $bgd_color, $ignore_transparent = null) {}

/**
 * Should antialias functions be used or not
 * @link http://php.net/manual/en/function.imageantialias.php
 * @param image resource
 * @param on bool
 * @return bool 
 */
function imageantialias ($image, $on) {}

/**
 * Set the tile image for filling
 * @link http://php.net/manual/en/function.imagesettile.php
 * @param image resource
 * @param tile resource
 * @return bool 
 */
function imagesettile ($image, $tile) {}

/**
 * Set the brush image for line drawing
 * @link http://php.net/manual/en/function.imagesetbrush.php
 * @param image resource
 * @param brush resource
 * @return bool 
 */
function imagesetbrush ($image, $brush) {}

/**
 * Set the style for line drawing
 * @link http://php.net/manual/en/function.imagesetstyle.php
 * @param image resource
 * @param style array
 * @return bool 
 */
function imagesetstyle ($image, array $style) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefrompng.php
 * @param filename string
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatefrompng ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgif.php
 * @param filename string
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatefromgif ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromjpeg.php
 * @param filename string
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatefromjpeg ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromwbmp.php
 * @param filename string
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatefromwbmp ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromxbm.php
 * @param filename string
 * @return resource an image resource identifier on success, false on errors.
 */
function imagecreatefromxbm ($filename) {}

/**
 * Create a new image from GD file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd.php
 * @param filename string
 * @return resource 
 */
function imagecreatefromgd ($filename) {}

/**
 * Create a new image from GD2 file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd2.php
 * @param filename string
 * @return resource 
 */
function imagecreatefromgd2 ($filename) {}

/**
 * Create a new image from a given part of GD2 file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd2part.php
 * @param filename string
 * @param srcX int
 * @param srcY int
 * @param width int
 * @param height int
 * @return resource 
 */
function imagecreatefromgd2part ($filename, $srcX, $srcY, $width, $height) {}

/**
 * Output a PNG image to either the browser or a file
 * @link http://php.net/manual/en/function.imagepng.php
 * @param image resource
 * @param filename string[optional]
 * @param quality int[optional]
 * @param filters int[optional]
 * @return bool 
 */
function imagepng ($image, $filename = null, $quality = null, $filters = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagegif.php
 * @param image resource
 * @param filename string[optional]
 * @return bool 
 */
function imagegif ($image, $filename = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagejpeg.php
 * @param image resource
 * @param filename string[optional]
 * @param quality int[optional]
 * @return bool 
 */
function imagejpeg ($image, $filename = null, $quality = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagewbmp.php
 * @param image resource
 * @param filename string[optional]
 * @param foreground int[optional]
 * @return bool 
 */
function imagewbmp ($image, $filename = null, $foreground = null) {}

/**
 * Output GD image to browser or file
 * @link http://php.net/manual/en/function.imagegd.php
 * @param image resource
 * @param filename string[optional]
 * @return bool 
 */
function imagegd ($image, $filename = null) {}

/**
 * Output GD2 image to browser or file
 * @link http://php.net/manual/en/function.imagegd2.php
 * @param image resource
 * @param filename string[optional]
 * @param chunk_size int[optional]
 * @param type int[optional]
 * @return bool 
 */
function imagegd2 ($image, $filename = null, $chunk_size = null, $type = null) {}

/**
 * Destroy an image
 * @link http://php.net/manual/en/function.imagedestroy.php
 * @param image resource
 * @return bool 
 */
function imagedestroy ($image) {}

/**
 * Apply a gamma correction to a GD image
 * @link http://php.net/manual/en/function.imagegammacorrect.php
 * @param image resource
 * @param inputgamma float
 * @param outputgamma float
 * @return bool 
 */
function imagegammacorrect ($image, $inputgamma, $outputgamma) {}

/**
 * Flood fill
 * @link http://php.net/manual/en/function.imagefill.php
 * @param image resource
 * @param x int
 * @param y int
 * @param color int
 * @return bool 
 */
function imagefill ($image, $x, $y, $color) {}

/**
 * Draw a filled polygon
 * @link http://php.net/manual/en/function.imagefilledpolygon.php
 * @param image resource
 * @param points array
 * @param num_points int
 * @param color int
 * @return bool 
 */
function imagefilledpolygon ($image, array $points, $num_points, $color) {}

/**
 * Draw a filled rectangle
 * @link http://php.net/manual/en/function.imagefilledrectangle.php
 * @param image resource
 * @param x1 int
 * @param y1 int
 * @param x2 int
 * @param y2 int
 * @param color int
 * @return bool 
 */
function imagefilledrectangle ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Flood fill to specific color
 * @link http://php.net/manual/en/function.imagefilltoborder.php
 * @param image resource
 * @param x int
 * @param y int
 * @param border int
 * @param color int
 * @return bool 
 */
function imagefilltoborder ($image, $x, $y, $border, $color) {}

/**
 * Get font width
 * @link http://php.net/manual/en/function.imagefontwidth.php
 * @param font int
 * @return int the width of the pixel
 */
function imagefontwidth ($font) {}

/**
 * Get font height
 * @link http://php.net/manual/en/function.imagefontheight.php
 * @param font int
 * @return int the height of the pixel.
 */
function imagefontheight ($font) {}

/**
 * Enable or disable interlace
 * @link http://php.net/manual/en/function.imageinterlace.php
 * @param image resource
 * @param interlace int[optional]
 * @return int 1 if the interlace bit is set for the image, 0 otherwise.
 */
function imageinterlace ($image, $interlace = null) {}

/**
 * Draw a line
 * @link http://php.net/manual/en/function.imageline.php
 * @param image resource
 * @param x1 int
 * @param y1 int
 * @param x2 int
 * @param y2 int
 * @param color int
 * @return bool 
 */
function imageline ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Load a new font
 * @link http://php.net/manual/en/function.imageloadfont.php
 * @param file string
 * @return int 
 */
function imageloadfont ($file) {}

/**
 * Draws a polygon
 * @link http://php.net/manual/en/function.imagepolygon.php
 * @param image resource
 * @param points array
 * @param num_points int
 * @param color int
 * @return bool 
 */
function imagepolygon ($image, array $points, $num_points, $color) {}

/**
 * Draw a rectangle
 * @link http://php.net/manual/en/function.imagerectangle.php
 * @param image resource
 * @param x1 int
 * @param y1 int
 * @param x2 int
 * @param y2 int
 * @param color int
 * @return bool 
 */
function imagerectangle ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Set a single pixel
 * @link http://php.net/manual/en/function.imagesetpixel.php
 * @param image resource
 * @param x int
 * @param y int
 * @param color int
 * @return bool 
 */
function imagesetpixel ($image, $x, $y, $color) {}

/**
 * Draw a string horizontally
 * @link http://php.net/manual/en/function.imagestring.php
 * @param image resource
 * @param font int
 * @param x int
 * @param y int
 * @param string string
 * @param color int
 * @return bool 
 */
function imagestring ($image, $font, $x, $y, $string, $color) {}

/**
 * Draw a string vertically
 * @link http://php.net/manual/en/function.imagestringup.php
 * @param image resource
 * @param font int
 * @param x int
 * @param y int
 * @param string string
 * @param color int
 * @return bool 
 */
function imagestringup ($image, $font, $x, $y, $string, $color) {}

/**
 * Get image width
 * @link http://php.net/manual/en/function.imagesx.php
 * @param image resource
 * @return int 
 */
function imagesx ($image) {}

/**
 * Get image height
 * @link http://php.net/manual/en/function.imagesy.php
 * @param image resource
 * @return int 
 */
function imagesy ($image) {}

/**
 * Draw a dashed line
 * @link http://php.net/manual/en/function.imagedashedline.php
 * @param image resource
 * @param x1 int
 * @param y1 int
 * @param x2 int
 * @param y2 int
 * @param color int
 * @return bool 
 */
function imagedashedline ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Give the bounding box of a text using TrueType fonts
 * @link http://php.net/manual/en/function.imagettfbbox.php
 * @param size float
 * @param angle float
 * @param fontfile string
 * @param text string
 * @return array 
 */
function imagettfbbox ($size, $angle, $fontfile, $text) {}

/**
 * Write text to the image using TrueType fonts
 * @link http://php.net/manual/en/function.imagettftext.php
 * @param image resource
 * @param size float
 * @param angle float
 * @param x int
 * @param y int
 * @param color int
 * @param fontfile string
 * @param text string
 * @return array an array with 8 elements representing four points making the
 */
function imagettftext ($image, $size, $angle, $x, $y, $color, $fontfile, $text) {}

/**
 * Give the bounding box of a text using fonts via freetype2
 * @link http://php.net/manual/en/function.imageftbbox.php
 * @param size float
 * @param angle float
 * @param font_file string
 * @param text string
 * @param extrainfo array[optional]
 * @return array 
 */
function imageftbbox ($size, $angle, $font_file, $text, array $extrainfo = null) {}

/**
 * Write text to the image using fonts using FreeType 2
 * @link http://php.net/manual/en/function.imagefttext.php
 * @param image resource
 * @param size float
 * @param angle float
 * @param x int
 * @param y int
 * @param col int
 * @param font_file string
 * @param text string
 * @param extrainfo array[optional]
 * @return array 
 */
function imagefttext ($image, $size, $angle, $x, $y, $col, $font_file, $text, array $extrainfo = null) {}

/**
 * Load a PostScript Type 1 font from file
 * @link http://php.net/manual/en/function.imagepsloadfont.php
 * @param filename string
 * @return resource 
 */
function imagepsloadfont ($filename) {}

/**
 * Free memory used by a PostScript Type 1 font
 * @link http://php.net/manual/en/function.imagepsfreefont.php
 * @param fontindex resource
 * @return bool 
 */
function imagepsfreefont ($fontindex) {}

/**
 * Change the character encoding vector of a font
 * @link http://php.net/manual/en/function.imagepsencodefont.php
 * @param font_index resource
 * @param encodingfile string
 * @return bool 
 */
function imagepsencodefont ($font_index, $encodingfile) {}

/**
 * Extend or condense a font
 * @link http://php.net/manual/en/function.imagepsextendfont.php
 * @param font_index int
 * @param extend float
 * @return bool 
 */
function imagepsextendfont ($font_index, $extend) {}

/**
 * Slant a font
 * @link http://php.net/manual/en/function.imagepsslantfont.php
 * @param font_index resource
 * @param slant float
 * @return bool 
 */
function imagepsslantfont ($font_index, $slant) {}

/**
 * Draws a text over an image using PostScript Type1 fonts
 * @link http://php.net/manual/en/function.imagepstext.php
 * @param image resource
 * @param text string
 * @param font resource
 * @param size int
 * @param foreground int
 * @param background int
 * @param x int
 * @param y int
 * @param space int[optional]
 * @param tightness int[optional]
 * @param angle float[optional]
 * @param antialias_steps int[optional]
 * @return array 
 */
function imagepstext ($image, $text, $font, $size, $foreground, $background, $x, $y, $space = null, $tightness = null, $angle = null, $antialias_steps = null) {}

/**
 * Give the bounding box of a text rectangle using PostScript Type1 fonts
 * @link http://php.net/manual/en/function.imagepsbbox.php
 * @param text string
 * @param font int
 * @param size int
 * @param space int[optional]
 * @param tightness int
 * @param angle float
 * @return array an array containing the following elements:
 */
function imagepsbbox ($text, $font, $size, $space = null, $tightness, $angle) {}

/**
 * Return the image types supported by this PHP build
 * @link http://php.net/manual/en/function.imagetypes.php
 * @return int a bit-field corresponding to the image formats supported by the
 */
function imagetypes () {}

/**
 * Convert JPEG image file to WBMP image file
 * @link http://php.net/manual/en/function.jpeg2wbmp.php
 * @param jpegname string
 * @param wbmpname string
 * @param dest_height int
 * @param dest_width int
 * @param threshold int
 * @return bool 
 */
function jpeg2wbmp ($jpegname, $wbmpname, $dest_height, $dest_width, $threshold) {}

/**
 * Convert PNG image file to WBMP image file
 * @link http://php.net/manual/en/function.png2wbmp.php
 * @param pngname string
 * @param wbmpname string
 * @param dest_height int
 * @param dest_width int
 * @param threshold int
 * @return bool 
 */
function png2wbmp ($pngname, $wbmpname, $dest_height, $dest_width, $threshold) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.image2wbmp.php
 * @param image resource
 * @param filename string[optional]
 * @param threshold int[optional]
 * @return bool 
 */
function image2wbmp ($image, $filename = null, $threshold = null) {}

/**
 * Set the alpha blending flag to use the bundled libgd layering effects
 * @link http://php.net/manual/en/function.imagelayereffect.php
 * @param image resource
 * @param effect int
 * @return bool 
 */
function imagelayereffect ($image, $effect) {}

/**
 * Makes the colors of the palette version of an image more closely match the true color version
 * @link http://php.net/manual/en/function.imagecolormatch.php
 * @param image1 resource
 * @param image2 resource
 * @return bool 
 */
function imagecolormatch ($image1, $image2) {}

/**
 * Output XBM image to browser or file
 * @link http://php.net/manual/en/function.imagexbm.php
 * @param image resource
 * @param filename string
 * @param foreground int[optional]
 * @return bool 
 */
function imagexbm ($image, $filename, $foreground = null) {}

/**
 * Applies a filter to an image
 * @link http://php.net/manual/en/function.imagefilter.php
 * @param image resource
 * @param filtertype int
 * @param arg1 int[optional]
 * @param arg2 int[optional]
 * @param arg3 int[optional]
 * @return bool 
 */
function imagefilter ($image, $filtertype, $arg1 = null, $arg2 = null, $arg3 = null) {}

/**
 * Apply a 3x3 convolution matrix, using coefficient and offset
 * @link http://php.net/manual/en/function.imageconvolution.php
 * @param image resource
 * @param matrix array
 * @param div float
 * @param offset float
 * @return bool 
 */
function imageconvolution ($image, array $matrix, $div, $offset) {}

define ('IMG_GIF', 1);
define ('IMG_JPG', 2);
define ('IMG_JPEG', 2);
define ('IMG_PNG', 4);
define ('IMG_WBMP', 8);
define ('IMG_XPM', 16);
define ('IMG_COLOR_TILED', -5);
define ('IMG_COLOR_STYLED', -2);
define ('IMG_COLOR_BRUSHED', -3);
define ('IMG_COLOR_STYLEDBRUSHED', -4);
define ('IMG_COLOR_TRANSPARENT', -6);
define ('IMG_ARC_ROUNDED', 0);
define ('IMG_ARC_PIE', 0);
define ('IMG_ARC_CHORD', 1);
define ('IMG_ARC_NOFILL', 2);
define ('IMG_ARC_EDGED', 4);
define ('IMG_GD2_RAW', 1);
define ('IMG_GD2_COMPRESSED', 2);
define ('IMG_EFFECT_REPLACE', 0);
define ('IMG_EFFECT_ALPHABLEND', 1);
define ('IMG_EFFECT_NORMAL', 2);
define ('IMG_EFFECT_OVERLAY', 3);
define ('GD_BUNDLED', 1);
define ('IMG_FILTER_NEGATE', 0);
define ('IMG_FILTER_GRAYSCALE', 1);
define ('IMG_FILTER_BRIGHTNESS', 2);
define ('IMG_FILTER_CONTRAST', 3);
define ('IMG_FILTER_COLORIZE', 4);
define ('IMG_FILTER_EDGEDETECT', 5);
define ('IMG_FILTER_GAUSSIAN_BLUR', 7);
define ('IMG_FILTER_SELECTIVE_BLUR', 8);
define ('IMG_FILTER_EMBOSS', 6);
define ('IMG_FILTER_MEAN_REMOVAL', 9);
define ('IMG_FILTER_SMOOTH', 10);
define ('GD_VERSION', "2.0.35");
define ('GD_MAJOR_VERSION', 2);
define ('GD_MINOR_VERSION', 0);
define ('GD_RELEASE_VERSION', 35);
define ('GD_EXTRA_VERSION', "");
define ('PNG_NO_FILTER', 0);
define ('PNG_FILTER_NONE', 8);
define ('PNG_FILTER_SUB', 16);
define ('PNG_FILTER_UP', 32);
define ('PNG_FILTER_AVG', 64);
define ('PNG_FILTER_PAETH', 128);
define ('PNG_ALL_FILTERS', 248);

// End of gd v.

// Start of iconv v.

/**
 * Convert string to requested character encoding
 * @link http://php.net/manual/en/function.iconv.php
 * @param in_charset string
 * @param out_charset string
 * @param str string
 * @return string the converted string or false on failure.
 */
function iconv ($in_charset, $out_charset, $str) {}

/**
 * Convert character encoding as output buffer handler
 * @link http://php.net/manual/en/function.ob-iconv-handler.php
 * @param contents string
 * @param status int
 * @return string 
 */
function ob_iconv_handler ($contents, $status) {}

/**
 * Retrieve internal configuration variables of iconv extension
 * @link http://php.net/manual/en/function.iconv-get-encoding.php
 * @param type string[optional]
 * @return mixed the current value of the internal configuration variable if
 */
function iconv_get_encoding ($type = null) {}

/**
 * Set current setting for character encoding conversion
 * @link http://php.net/manual/en/function.iconv-set-encoding.php
 * @param type string
 * @param charset string
 * @return bool 
 */
function iconv_set_encoding ($type, $charset) {}

/**
 * Returns the character count of string
 * @link http://php.net/manual/en/function.iconv-strlen.php
 * @param str string
 * @param charset string[optional]
 * @return int the character count of str, as an integer.
 */
function iconv_strlen ($str, $charset = null) {}

/**
 * Cut out part of a string
 * @link http://php.net/manual/en/function.iconv-substr.php
 * @param str string
 * @param offset int
 * @param length int[optional]
 * @param charset string[optional]
 * @return string the portion of str specified by the
 */
function iconv_substr ($str, $offset, $length = null, $charset = null) {}

/**
 * Finds position of first occurrence of a needle within a haystack
 * @link http://php.net/manual/en/function.iconv-strpos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @param charset string[optional]
 * @return int the numeric position of the first occurrence of
 */
function iconv_strpos ($haystack, $needle, $offset = null, $charset = null) {}

/**
 * Finds the last occurrence of a needle within a haystack
 * @link http://php.net/manual/en/function.iconv-strrpos.php
 * @param haystack string
 * @param needle string
 * @param charset string[optional]
 * @return int the numeric position of the last occurrence of
 */
function iconv_strrpos ($haystack, $needle, $charset = null) {}

/**
 * Composes a <literal>MIME</literal> header field
 * @link http://php.net/manual/en/function.iconv-mime-encode.php
 * @param field_name string
 * @param field_value string
 * @param preferences array[optional]
 * @return string an encoded MIME field on success,
 */
function iconv_mime_encode ($field_name, $field_value, array $preferences = null) {}

/**
 * Decodes a <literal>MIME</literal> header field
 * @link http://php.net/manual/en/function.iconv-mime-decode.php
 * @param encoded_header string
 * @param mode int[optional]
 * @param charset string[optional]
 * @return string a decoded MIME field on success,
 */
function iconv_mime_decode ($encoded_header, $mode = null, $charset = null) {}

/**
 * Decodes multiple <literal>MIME</literal> header fields at once
 * @link http://php.net/manual/en/function.iconv-mime-decode-headers.php
 * @param encoded_headers string
 * @param mode int[optional]
 * @param charset string[optional]
 * @return array 
 */
function iconv_mime_decode_headers ($encoded_headers, $mode = null, $charset = null) {}

define ('ICONV_IMPL', "glibc");
define ('ICONV_VERSION', 1.9);
define ('ICONV_MIME_DECODE_STRICT', 1);
define ('ICONV_MIME_DECODE_CONTINUE_ON_ERROR', 2);

// End of iconv v.

// Start of mbstring v.

/**
 * Perform case folding on a string
 * @link http://php.net/manual/en/function.mb-convert-case.php
 */
function mb_convert_case () {}

/**
 * Make a string uppercase
 * @link http://php.net/manual/en/function.mb-strtoupper.php
 */
function mb_strtoupper () {}

/**
 * Make a string lowercase
 * @link http://php.net/manual/en/function.mb-strtolower.php
 */
function mb_strtolower () {}

/**
 * Set/Get current language
 * @link http://php.net/manual/en/function.mb-language.php
 */
function mb_language () {}

/**
 * Set/Get internal character encoding
 * @link http://php.net/manual/en/function.mb-internal-encoding.php
 */
function mb_internal_encoding () {}

/**
 * Detect HTTP input character encoding
 * @link http://php.net/manual/en/function.mb-http-input.php
 */
function mb_http_input () {}

/**
 * Set/Get HTTP output character encoding
 * @link http://php.net/manual/en/function.mb-http-output.php
 */
function mb_http_output () {}

/**
 * Set/Get character encoding detection order
 * @link http://php.net/manual/en/function.mb-detect-order.php
 */
function mb_detect_order () {}

/**
 * Set/Get substitution character
 * @link http://php.net/manual/en/function.mb-substitute-character.php
 */
function mb_substitute_character () {}

/**
 * Parse GET/POST/COOKIE data and set global variable
 * @link http://php.net/manual/en/function.mb-parse-str.php
 * @param var1
 * @param var2
 */
function mb_parse_str ($var1, &$var2) {}

/**
 * Callback function converts character encoding in output buffer
 * @link http://php.net/manual/en/function.mb-output-handler.php
 */
function mb_output_handler () {}

/**
 * Get MIME charset string
 * @link http://php.net/manual/en/function.mb-preferred-mime-name.php
 */
function mb_preferred_mime_name () {}

/**
 * Get string length
 * @link http://php.net/manual/en/function.mb-strlen.php
 */
function mb_strlen () {}

/**
 * Find position of first occurrence of string in a string
 * @link http://php.net/manual/en/function.mb-strpos.php
 */
function mb_strpos () {}

/**
 * Find position of last occurrence of a string in a string
 * @link http://php.net/manual/en/function.mb-strrpos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @param encoding string[optional]
 * @return int 
 */
function mb_strrpos ($haystack, $needle, $offset = null, $encoding = null) {}

/**
 * Finds position of first occurrence of a string within another, case insensitive
 * @link http://php.net/manual/en/function.mb-stripos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @param encoding string[optional]
 * @return int 
 */
function mb_stripos ($haystack, $needle, $offset = null, $encoding = null) {}

/**
 * Finds position of last occurrence of a string within another, case insensitive
 * @link http://php.net/manual/en/function.mb-strripos.php
 * @param haystack string
 * @param needle string
 * @param offset int[optional]
 * @param encoding string[optional]
 * @return int 
 */
function mb_strripos ($haystack, $needle, $offset = null, $encoding = null) {}

/**
 * Finds first occurrence of a string within another
 * @link http://php.net/manual/en/function.mb-strstr.php
 * @param haystack string
 * @param needle string
 * @param part bool[optional]
 * @param encoding string[optional]
 * @return string the portion of haystack,
 */
function mb_strstr ($haystack, $needle, $part = null, $encoding = null) {}

/**
 * Finds the last occurrence of a character in a string within another
 * @link http://php.net/manual/en/function.mb-strrchr.php
 * @param haystack string
 * @param needle string
 * @param part bool[optional]
 * @param encoding string[optional]
 * @return string the portion of haystack.
 */
function mb_strrchr ($haystack, $needle, $part = null, $encoding = null) {}

/**
 * Finds first occurrence of a string within another, case insensitive
 * @link http://php.net/manual/en/function.mb-stristr.php
 * @param haystack string
 * @param needle string
 * @param part bool[optional]
 * @param encoding string[optional]
 * @return string the portion of haystack,
 */
function mb_stristr ($haystack, $needle, $part = null, $encoding = null) {}

/**
 * Finds the last occurrence of a character in a string within another, case insensitive
 * @link http://php.net/manual/en/function.mb-strrichr.php
 * @param haystack string
 * @param needle string
 * @param part bool[optional]
 * @param encoding string[optional]
 * @return string the portion of haystack.
 */
function mb_strrichr ($haystack, $needle, $part = null, $encoding = null) {}

/**
 * Count the number of substring occurrences
 * @link http://php.net/manual/en/function.mb-substr-count.php
 */
function mb_substr_count () {}

/**
 * Get part of string
 * @link http://php.net/manual/en/function.mb-substr.php
 */
function mb_substr () {}

/**
 * Get part of string
 * @link http://php.net/manual/en/function.mb-strcut.php
 */
function mb_strcut () {}

/**
 * Return width of string
 * @link http://php.net/manual/en/function.mb-strwidth.php
 */
function mb_strwidth () {}

/**
 * Get truncated string with specified width
 * @link http://php.net/manual/en/function.mb-strimwidth.php
 */
function mb_strimwidth () {}

/**
 * Convert character encoding
 * @link http://php.net/manual/en/function.mb-convert-encoding.php
 */
function mb_convert_encoding () {}

/**
 * Detect character encoding
 * @link http://php.net/manual/en/function.mb-detect-encoding.php
 */
function mb_detect_encoding () {}

function mb_list_encodings () {}

/**
 * Convert "kana" one from another ("zen-kaku", "han-kaku" and more)
 * @link http://php.net/manual/en/function.mb-convert-kana.php
 */
function mb_convert_kana () {}

/**
 * Encode string for MIME header
 * @link http://php.net/manual/en/function.mb-encode-mimeheader.php
 */
function mb_encode_mimeheader () {}

/**
 * Decode string in MIME header field
 * @link http://php.net/manual/en/function.mb-decode-mimeheader.php
 */
function mb_decode_mimeheader () {}

/**
 * Convert character code in variable(s)
 * @link http://php.net/manual/en/function.mb-convert-variables.php
 * @param var1
 * @param var2
 */
function mb_convert_variables ($var1, $var2) {}

/**
 * Encode character to HTML numeric string reference
 * @link http://php.net/manual/en/function.mb-encode-numericentity.php
 */
function mb_encode_numericentity () {}

/**
 * Decode HTML numeric string reference to character
 * @link http://php.net/manual/en/function.mb-decode-numericentity.php
 */
function mb_decode_numericentity () {}

/**
 * Send encoded mail
 * @link http://php.net/manual/en/function.mb-send-mail.php
 */
function mb_send_mail () {}

/**
 * Get internal settings of mbstring
 * @link http://php.net/manual/en/function.mb-get-info.php
 */
function mb_get_info () {}

/**
 * Check if the string is valid for the specified encoding
 * @link http://php.net/manual/en/function.mb-check-encoding.php
 * @param var string[optional]
 * @param encoding string[optional]
 * @return bool 
 */
function mb_check_encoding ($var = null, $encoding = null) {}

/**
 * Returns current encoding for multibyte regex as string
 * @link http://php.net/manual/en/function.mb-regex-encoding.php
 */
function mb_regex_encoding () {}

/**
 * Set/Get the default options for mbregex functions
 * @link http://php.net/manual/en/function.mb-regex-set-options.php
 */
function mb_regex_set_options () {}

/**
 * Regular expression match with multibyte support
 * @link http://php.net/manual/en/function.mb-ereg.php
 * @param var1
 * @param var2
 * @param var3
 */
function mb_ereg ($var1, $var2, &$var3) {}

/**
 * Regular expression match ignoring case with multibyte support
 * @link http://php.net/manual/en/function.mb-eregi.php
 * @param var1
 * @param var2
 * @param var3
 */
function mb_eregi ($var1, $var2, &$var3) {}

/**
 * Replace regular expression with multibyte support
 * @link http://php.net/manual/en/function.mb-ereg-replace.php
 */
function mb_ereg_replace () {}

/**
 * Replace regular expression with multibyte support
     ignoring case
 * @link http://php.net/manual/en/function.mb-eregi-replace.php
 */
function mb_eregi_replace () {}

/**
 * Split multibyte string using regular expression
 * @link http://php.net/manual/en/function.mb-split.php
 */
function mb_split () {}

/**
 * Regular expression match for multibyte string
 * @link http://php.net/manual/en/function.mb-ereg-match.php
 */
function mb_ereg_match () {}

/**
 * Multibyte regular expression match for predefined multibyte string
 * @link http://php.net/manual/en/function.mb-ereg-search.php
 */
function mb_ereg_search () {}

/**
 * Return position and length of matched part of multibyte regular
     expression for predefined multibyte string
 * @link http://php.net/manual/en/function.mb-ereg-search-pos.php
 */
function mb_ereg_search_pos () {}

/**
 * Returns the matched part of multibyte regular expression
 * @link http://php.net/manual/en/function.mb-ereg-search-regs.php
 */
function mb_ereg_search_regs () {}

/**
 * Setup string and regular expression for multibyte regular
     expression match
 * @link http://php.net/manual/en/function.mb-ereg-search-init.php
 */
function mb_ereg_search_init () {}

/**
 * Retrieve the result from the last multibyte regular expression
     match
 * @link http://php.net/manual/en/function.mb-ereg-search-getregs.php
 */
function mb_ereg_search_getregs () {}

/**
 * Returns start point for next regular expression match
 * @link http://php.net/manual/en/function.mb-ereg-search-getpos.php
 */
function mb_ereg_search_getpos () {}

/**
 * Set start point of next regular expression match
 * @link http://php.net/manual/en/function.mb-ereg-search-setpos.php
 */
function mb_ereg_search_setpos () {}

function mbregex_encoding () {}

function mbereg () {}

function mberegi () {}

function mbereg_replace () {}

function mberegi_replace () {}

function mbsplit () {}

function mbereg_match () {}

function mbereg_search () {}

function mbereg_search_pos () {}

function mbereg_search_regs () {}

function mbereg_search_init () {}

function mbereg_search_getregs () {}

function mbereg_search_getpos () {}

function mbereg_search_setpos () {}

define ('MB_OVERLOAD_MAIL', 1);
define ('MB_OVERLOAD_STRING', 2);
define ('MB_OVERLOAD_REGEX', 4);
define ('MB_CASE_UPPER', 0);
define ('MB_CASE_LOWER', 1);
define ('MB_CASE_TITLE', 2);

// End of mbstring v.

// Start of ming v.

class SWFShape  {

	public function __construct () {}

	public function setLine () {}

	public function addFill () {}

	public function setLeftFill () {}

	public function setRightFill () {}

	public function movePenTo () {}

	public function movePen () {}

	public function drawLineTo () {}

	public function drawLine () {}

	public function drawCurveTo () {}

	public function drawCurve () {}

	public function drawGlyph () {}

	public function drawCircle () {}

	public function drawArc () {}

	public function drawCubic () {}

	public function drawCubicTo () {}

}

class SWFFill  {

	public function __construct () {}

	public function moveTo () {}

	public function scaleTo () {}

	public function rotateTo () {}

	public function skewXTo () {}

	public function skewYTo () {}

}

class SWFGradient  {

	public function __construct () {}

	public function addEntry () {}

}

class SWFBitmap  {

	public function __construct () {}

	public function getWidth () {}

	public function getHeight () {}

}

class SWFText  {

	public function __construct () {}

	public function setFont () {}

	public function setHeight () {}

	public function setSpacing () {}

	public function setColor () {}

	public function moveTo () {}

	public function addString () {}

	public function addUTF8String () {}

	public function getWidth () {}

	public function getUTF8Width () {}

	public function getAscent () {}

	public function getDescent () {}

	public function getLeading () {}

}

class SWFTextField  {

	public function __construct () {}

	public function setFont () {}

	public function setBounds () {}

	public function align () {}

	public function setHeight () {}

	public function setLeftMargin () {}

	public function setRightMargin () {}

	public function setMargins () {}

	public function setIndentation () {}

	public function setLineSpacing () {}

	public function setColor () {}

	public function setName () {}

	public function addString () {}

	public function setPadding () {}

	public function addChars () {}

}

class SWFFont  {

	public function __construct () {}

	public function getWidth () {}

	public function getUTF8Width () {}

	public function getAscent () {}

	public function getDescent () {}

	public function getLeading () {}

	public function getShape () {}

}

class SWFDisplayItem  {

	public function moveTo () {}

	public function move () {}

	public function scaleTo () {}

	public function scale () {}

	public function rotateTo () {}

	public function rotate () {}

	public function skewXTo () {}

	public function skewX () {}

	public function skewYTo () {}

	public function skewY () {}

	public function setMatrix () {}

	public function setDepth () {}

	public function setRatio () {}

	public function addColor () {}

	public function multColor () {}

	public function setName () {}

	public function addAction () {}

	public function remove () {}

	public function setMaskLevel () {}

	public function endMask () {}

	public function getX () {}

	public function getY () {}

	public function getXScale () {}

	public function getYScale () {}

	public function getXSkew () {}

	public function getYSkew () {}

	public function getRot () {}

}

class SWFMovie  {

	public function __construct () {}

	public function nextFrame () {}

	public function labelFrame () {}

	public function add () {}

	public function remove () {}

	public function output () {}

	public function save () {}

	public function saveToFile () {}

	public function setBackground () {}

	public function setRate () {}

	public function setDimension () {}

	public function setFrames () {}

	public function streamMP3 () {}

	public function addExport () {}

	public function writeExports () {}

	public function startSound () {}

	public function stopSound () {}

	public function importChar () {}

	public function importFont () {}

	public function addFont () {}

	public function protect () {}

	public function namedAnchor () {}

}

class SWFButton  {

	public function __construct () {}

	public function setHit () {}

	public function setOver () {}

	public function setUp () {}

	public function setDown () {}

	public function setAction () {}

	public function addShape () {}

	public function setMenu () {}

	public function addAction () {}

	public function addSound () {}

}

class SWFAction  {

	public function __construct () {}

}

class SWFMorph  {

	public function __construct () {}

	public function getShape1 () {}

	public function getShape2 () {}

}

class SWFSprite  {

	public function __construct () {}

	public function add () {}

	public function remove () {}

	public function nextFrame () {}

	public function labelFrame () {}

	public function setFrames () {}

	public function startSound () {}

	public function stopSound () {}

}

class SWFSound  {

	public function __construct () {}

}

class SWFFontChar  {

	public function addChars () {}

	public function addUTF8Chars () {}

}

class SWFSoundInstance  {

	public function noMultiple () {}

	public function loopInPoint () {}

	public function loopOutPoint () {}

	public function loopCount () {}

}

class SWFVideoStream  {

	public function __construct () {}

	public function setdimension () {}

	public function getnumframes () {}

}

/**
 * Set cubic threshold
 * @link http://php.net/manual/en/function.ming-setcubicthreshold.php
 * @param threshold int
 * @return void 
 */
function ming_setcubicthreshold ($threshold) {}

/**
 * Set scale
 * @link http://php.net/manual/en/function.ming-setscale.php
 * @param scale int
 * @return void 
 */
function ming_setscale ($scale) {}

/**
 * Sets the SWF version
 * @link http://php.net/manual/en/function.ming-useswfversion.php
 * @param version int
 * @return void 
 */
function ming_useswfversion ($version) {}

/**
 * Returns the action flag for keyPress(char)
 * @link http://php.net/manual/en/function.ming-keypress.php
 * @param char string
 * @return int 
 */
function ming_keypress ($char) {}

/**
 * Use constant pool
 * @link http://php.net/manual/en/function.ming-useconstants.php
 * @param use int
 * @return void 
 */
function ming_useconstants ($use) {}

/**
 * Sets the SWF output compression
 * @link http://php.net/manual/en/function.ming-setswfcompression.php
 * @param level int
 * @return void 
 */
function ming_setswfcompression ($level) {}

define ('MING_NEW', 1);
define ('MING_ZLIB', 1);
define ('SWFBUTTON_HIT', 8);
define ('SWFBUTTON_DOWN', 4);
define ('SWFBUTTON_OVER', 2);
define ('SWFBUTTON_UP', 1);
define ('SWFBUTTON_MOUSEUPOUTSIDE', 64);
define ('SWFBUTTON_DRAGOVER', 160);
define ('SWFBUTTON_DRAGOUT', 272);
define ('SWFBUTTON_MOUSEUP', 8);
define ('SWFBUTTON_MOUSEDOWN', 4);
define ('SWFBUTTON_MOUSEOUT', 2);
define ('SWFBUTTON_MOUSEOVER', 1);
define ('SWFFILL_RADIAL_GRADIENT', 18);
define ('SWFFILL_LINEAR_GRADIENT', 16);
define ('SWFFILL_TILED_BITMAP', 64);
define ('SWFFILL_CLIPPED_BITMAP', 65);
define ('SWFTEXTFIELD_HASLENGTH', 2);
define ('SWFTEXTFIELD_NOEDIT', 8);
define ('SWFTEXTFIELD_PASSWORD', 16);
define ('SWFTEXTFIELD_MULTILINE', 32);
define ('SWFTEXTFIELD_WORDWRAP', 64);
define ('SWFTEXTFIELD_DRAWBOX', 2048);
define ('SWFTEXTFIELD_NOSELECT', 4096);
define ('SWFTEXTFIELD_HTML', 512);
define ('SWFTEXTFIELD_USEFONT', 256);
define ('SWFTEXTFIELD_AUTOSIZE', 16384);
define ('SWFTEXTFIELD_ALIGN_LEFT', 0);
define ('SWFTEXTFIELD_ALIGN_RIGHT', 1);
define ('SWFTEXTFIELD_ALIGN_CENTER', 2);
define ('SWFTEXTFIELD_ALIGN_JUSTIFY', 3);
define ('SWFACTION_ONLOAD', 1);
define ('SWFACTION_ENTERFRAME', 2);
define ('SWFACTION_UNLOAD', 4);
define ('SWFACTION_MOUSEMOVE', 8);
define ('SWFACTION_MOUSEDOWN', 16);
define ('SWFACTION_MOUSEUP', 32);
define ('SWFACTION_KEYDOWN', 64);
define ('SWFACTION_KEYUP', 128);
define ('SWFACTION_DATA', 256);
define ('SWF_SOUND_NOT_COMPRESSED', 0);
define ('SWF_SOUND_ADPCM_COMPRESSED', 16);
define ('SWF_SOUND_MP3_COMPRESSED', 32);
define ('SWF_SOUND_NOT_COMPRESSED_LE', 48);
define ('SWF_SOUND_NELLY_COMPRESSED', 96);
define ('SWF_SOUND_5KHZ', 0);
define ('SWF_SOUND_11KHZ', 4);
define ('SWF_SOUND_22KHZ', 8);
define ('SWF_SOUND_44KHZ', 12);
define ('SWF_SOUND_8BITS', 0);
define ('SWF_SOUND_16BITS', 2);
define ('SWF_SOUND_MONO', 0);
define ('SWF_SOUND_STEREO', 1);

// End of ming v.

// Start of pdo_mysql v.1.0.2
// End of pdo_mysql v.1.0.2

// Start of pdo_pgsql v.1.0.2
// End of pdo_pgsql v.1.0.2

// Start of shmop v.

/**
 * Create or open shared memory block
 * @link http://php.net/manual/en/function.shmop-open.php
 * @param key int
 * @param flags string
 * @param mode int
 * @param size int
 * @return int 
 */
function shmop_open ($key, $flags, $mode, $size) {}

/**
 * Read data from shared memory block
 * @link http://php.net/manual/en/function.shmop-read.php
 * @param shmid int
 * @param start int
 * @param count int
 * @return string the data or false on failure.
 */
function shmop_read ($shmid, $start, $count) {}

/**
 * Close shared memory block
 * @link http://php.net/manual/en/function.shmop-close.php
 * @param shmid int
 * @return void 
 */
function shmop_close ($shmid) {}

/**
 * Get size of shared memory block
 * @link http://php.net/manual/en/function.shmop-size.php
 * @param shmid int
 * @return int an int, which represents the number of bytes the shared memory
 */
function shmop_size ($shmid) {}

/**
 * Write data into shared memory block
 * @link http://php.net/manual/en/function.shmop-write.php
 * @param shmid int
 * @param data string
 * @param offset int
 * @return int 
 */
function shmop_write ($shmid, $data, $offset) {}

/**
 * Delete shared memory block
 * @link http://php.net/manual/en/function.shmop-delete.php
 * @param shmid int
 * @return bool 
 */
function shmop_delete ($shmid) {}

// End of shmop v.

// Start of SQLite v.2.0-dev

/**
 * Represents an opened SQLite database.
 * @link http://php.net/manual/en/ref.sqlite.php
 */
class SQLiteDatabase  {

	/**
	 * @param var1
	 * @param var2
	 * @param var3
	 */
	final public function __construct ($var1, $var2, &$var3) {}

	/**
	 * @param var1
	 * @param var2
	 * @param var3
	 */
	public function query ($var1, $var2, &$var3) {}

	/**
	 * @param var1
	 * @param var2
	 */
	public function queryExec ($var1, &$var2) {}

	public function arrayQuery () {}

	public function singleQuery () {}

	/**
	 * @param var1
	 * @param var2
	 * @param var3
	 */
	public function unbufferedQuery ($var1, $var2, &$var3) {}

	public function lastInsertRowid () {}

	public function changes () {}

	public function createAggregate () {}

	public function createFunction () {}

	public function busyTimeout () {}

	public function lastError () {}

	public function fetchColumnTypes () {}

}

/**
 * Represents a buffered SQLite result set.
 * @link http://php.net/manual/en/ref.sqlite.php
 */
final class SQLiteResult implements Iterator, Traversable, Countable {

	public function fetch () {}

	public function fetchObject () {}

	public function fetchSingle () {}

	public function fetchAll () {}

	public function column () {}

	public function numFields () {}

	public function fieldName () {}

	public function current () {}

	public function key () {}

	public function next () {}

	public function valid () {}

	public function rewind () {}

	public function count () {}

	public function prev () {}

	public function hasPrev () {}

	public function numRows () {}

	public function seek () {}

}

/**
 * Represents an unbuffered SQLite result set. Unbuffered results sets are sequential, forward-seeking only.
 * @link http://php.net/manual/en/ref.sqlite.php
 */
final class SQLiteUnbuffered  {

	public function fetch () {}

	public function fetchObject () {}

	public function fetchSingle () {}

	public function fetchAll () {}

	public function column () {}

	public function numFields () {}

	public function fieldName () {}

	public function current () {}

	public function next () {}

	public function valid () {}

}

final class SQLiteException extends RuntimeException  {
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

/**
 * Opens a SQLite database and create the database if it does not exist
 * @link http://php.net/manual/en/function.sqlite-open.php
 * @param filename string
 * @param mode int[optional]
 * @param error_message string[optional]
 * @return resource a resource (database handle) on success, false on error.
 */
function sqlite_open ($filename, $mode = null, &$error_message = null) {}

/**
 * Opens a persistent handle to an SQLite database and create the database if it does not exist
 * @link http://php.net/manual/en/function.sqlite-popen.php
 * @param filename string
 * @param mode int[optional]
 * @param error_message string[optional]
 * @return resource a resource (database handle) on success, false on error.
 */
function sqlite_popen ($filename, $mode = null, &$error_message = null) {}

/**
 * Closes an open SQLite database
 * @link http://php.net/manual/en/function.sqlite-close.php
 * @param dbhandle resource
 * @return void 
 */
function sqlite_close ($dbhandle) {}

/**
 * Executes a query against a given database and returns a result handle
 * @link http://php.net/manual/en/function.sqlite-query.php
 * @param query string
 * @param result_type int[optional]
 * @param error_msg string[optional]
 * @return SQLiteResult 
 */
function sqlite_query ($query, $result_type = null, &$error_msg = null) {}

/**
 * Executes a result-less query against a given database
 * @link http://php.net/manual/en/function.sqlite-exec.php
 * @param query string
 * @param error_msg string[optional]
 * @return bool 
 */
function sqlite_exec ($query, &$error_msg = null) {}

/**
 * Execute a query against a given database and returns an array
 * @link http://php.net/manual/en/function.sqlite-array-query.php
 * @param query string
 * @param result_type int[optional]
 * @param decode_binary bool[optional]
 * @return array an array of the entire result set; false otherwise.
 */
function sqlite_array_query ($query, $result_type = null, $decode_binary = null) {}

/**
 * Executes a query and returns either an array for one single column or the value of the first row
 * @link http://php.net/manual/en/function.sqlite-single-query.php
 * @param query string
 * @param first_row_only bool[optional]
 * @param decode_binary bool[optional]
 * @return array 
 */
function sqlite_single_query ($query, $first_row_only = null, $decode_binary = null) {}

/**
 * Fetches the next row from a result set as an array
 * @link http://php.net/manual/en/function.sqlite-fetch-array.php
 * @param result_type int[optional]
 * @param decode_binary bool[optional]
 * @return array an array of the next row from a result set; false if the
 */
function sqlite_fetch_array ($result_type = null, $decode_binary = null) {}

/**
 * Fetches the next row from a result set as an object
 * @link http://php.net/manual/en/function.sqlite-fetch-object.php
 * @param class_name string[optional]
 * @param ctor_params array[optional]
 * @param decode_binary bool[optional]
 * @return object 
 */
function sqlite_fetch_object ($class_name = null, array $ctor_params = null, $decode_binary = null) {}

/**
 * Fetches the first column of a result set as a string
 * @link http://php.net/manual/en/function.sqlite-fetch-single.php
 * @param decode_binary bool[optional]
 * @return string 
 */
function sqlite_fetch_single ($decode_binary = null) {}

/**
 * &Alias; <function>sqlite_fetch_single</function>
 * @link http://php.net/manual/en/function.sqlite-fetch-string.php
 */
function sqlite_fetch_string () {}

/**
 * Fetches all rows from a result set as an array of arrays
 * @link http://php.net/manual/en/function.sqlite-fetch-all.php
 * @param result_type int[optional]
 * @param decode_binary bool[optional]
 * @return array an array of the remaining rows in a result set. If called right
 */
function sqlite_fetch_all ($result_type = null, $decode_binary = null) {}

/**
 * Fetches the current row from a result set as an array
 * @link http://php.net/manual/en/function.sqlite-current.php
 * @param result_type int[optional]
 * @param decode_binary bool[optional]
 * @return array an array of the current row from a result set; false if the
 */
function sqlite_current ($result_type = null, $decode_binary = null) {}

/**
 * Fetches a column from the current row of a result set
 * @link http://php.net/manual/en/function.sqlite-column.php
 * @param index_or_name mixed
 * @param decode_binary bool[optional]
 * @return mixed 
 */
function sqlite_column ($index_or_name, $decode_binary = null) {}

/**
 * Returns the version of the linked SQLite library
 * @link http://php.net/manual/en/function.sqlite-libversion.php
 * @return string 
 */
function sqlite_libversion () {}

/**
 * Returns the encoding of the linked SQLite library
 * @link http://php.net/manual/en/function.sqlite-libencoding.php
 * @return string 
 */
function sqlite_libencoding () {}

/**
 * Returns the number of rows that were changed by the most
   recent SQL statement
 * @link http://php.net/manual/en/function.sqlite-changes.php
 * @return int 
 */
function sqlite_changes () {}

/**
 * Returns the rowid of the most recently inserted row
 * @link http://php.net/manual/en/function.sqlite-last-insert-rowid.php
 * @return int 
 */
function sqlite_last_insert_rowid () {}

/**
 * Returns the number of rows in a buffered result set
 * @link http://php.net/manual/en/function.sqlite-num-rows.php
 * @return int 
 */
function sqlite_num_rows () {}

/**
 * Returns the number of fields in a result set
 * @link http://php.net/manual/en/function.sqlite-num-fields.php
 * @return int 
 */
function sqlite_num_fields () {}

/**
 * Returns the name of a particular field
 * @link http://php.net/manual/en/function.sqlite-field-name.php
 * @param field_index int
 * @return string the name of a field in an SQLite result set, given the ordinal
 */
function sqlite_field_name ($field_index) {}

/**
 * Seek to a particular row number of a buffered result set
 * @link http://php.net/manual/en/function.sqlite-seek.php
 * @param rownum int
 * @return bool false if the row does not exist, true otherwise.
 */
function sqlite_seek ($rownum) {}

/**
 * Seek to the first row number
 * @link http://php.net/manual/en/function.sqlite-rewind.php
 * @return bool false if there are no rows in the result set, true otherwise.
 */
function sqlite_rewind () {}

/**
 * Seek to the next row number
 * @link http://php.net/manual/en/function.sqlite-next.php
 * @return bool true on success, or false if there are no more rows.
 */
function sqlite_next () {}

/**
 * Seek to the previous row number of a result set
 * @link http://php.net/manual/en/function.sqlite-prev.php
 * @return bool true on success, or false if there are no more previous rows.
 */
function sqlite_prev () {}

/**
 * Returns whether more rows are available
 * @link http://php.net/manual/en/function.sqlite-valid.php
 * @return bool true if there are more rows available from the
 */
function sqlite_valid () {}

/**
 * Finds whether or not more rows are available
 * @link http://php.net/manual/en/function.sqlite-has-more.php
 * @param result resource
 * @return bool true if there are more rows available from the
 */
function sqlite_has_more ($result) {}

/**
 * Returns whether or not a previous row is available
 * @link http://php.net/manual/en/function.sqlite-has-prev.php
 * @return bool true if there are more previous rows available from the
 */
function sqlite_has_prev () {}

/**
 * Escapes a string for use as a query parameter
 * @link http://php.net/manual/en/function.sqlite-escape-string.php
 * @param item string
 * @return string 
 */
function sqlite_escape_string ($item) {}

/**
 * Set busy timeout duration, or disable busy handlers
 * @link http://php.net/manual/en/function.sqlite-busy-timeout.php
 * @param milliseconds int
 * @return void 
 */
function sqlite_busy_timeout ($milliseconds) {}

/**
 * Returns the error code of the last error for a database
 * @link http://php.net/manual/en/function.sqlite-last-error.php
 * @return int 
 */
function sqlite_last_error () {}

/**
 * Returns the textual description of an error code
 * @link http://php.net/manual/en/function.sqlite-error-string.php
 * @param error_code int
 * @return string 
 */
function sqlite_error_string ($error_code) {}

/**
 * Execute a query that does not prefetch and buffer all data
 * @link http://php.net/manual/en/function.sqlite-unbuffered-query.php
 * @param query string
 * @param result_type int[optional]
 * @param error_msg string[optional]
 * @return SQLiteUnbuffered a result handle or false on failure.
 */
function sqlite_unbuffered_query ($query, $result_type = null, &$error_msg = null) {}

/**
 * Register an aggregating UDF for use in SQL statements
 * @link http://php.net/manual/en/function.sqlite-create-aggregate.php
 * @param function_name string
 * @param step_func callback
 * @param finalize_func callback
 * @param num_args int[optional]
 * @return void 
 */
function sqlite_create_aggregate ($function_name, $step_func, $finalize_func, $num_args = null) {}

/**
 * Registers a "regular" User Defined Function for use in SQL statements
 * @link http://php.net/manual/en/function.sqlite-create-function.php
 * @param function_name string
 * @param callback callback
 * @param num_args int[optional]
 * @return void 
 */
function sqlite_create_function ($function_name, $callback, $num_args = null) {}

/**
 * Opens a SQLite database and returns a SQLiteDatabase object
 * @link http://php.net/manual/en/function.sqlite-factory.php
 * @param filename string
 * @param mode int[optional]
 * @param error_message string[optional]
 * @return SQLiteDatabase a SQLiteDatabase object on success, &null; on error.
 */
function sqlite_factory ($filename, $mode = null, &$error_message = null) {}

/**
 * Encode binary data before returning it from an UDF
 * @link http://php.net/manual/en/function.sqlite-udf-encode-binary.php
 * @param data string
 * @return string 
 */
function sqlite_udf_encode_binary ($data) {}

/**
 * Decode binary data passed as parameters to an UDF
 * @link http://php.net/manual/en/function.sqlite-udf-decode-binary.php
 * @param data string
 * @return string 
 */
function sqlite_udf_decode_binary ($data) {}

/**
 * Return an array of column types from a particular table
 * @link http://php.net/manual/en/function.sqlite-fetch-column-types.php
 * @param table_name string
 * @param result_type int[optional]
 * @return array an array of column data types; false on error.
 */
function sqlite_fetch_column_types ($table_name, $result_type = null) {}


/**
 * Columns are returned into the array having both a numerical index
 * and the field name as the array index.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_BOTH', 3);

/**
 * Columns are returned into the array having a numerical index to the
 * fields. This index starts with 0, the first field in the result.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_NUM', 2);

/**
 * Columns are returned into the array having the field name as the array
 * index.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_ASSOC', 1);

/**
 * Successful result.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_OK', 0);

/**
 * SQL error or missing database.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_ERROR', 1);

/**
 * An internal logic error in SQLite.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_INTERNAL', 2);

/**
 * Access permission denied.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_PERM', 3);

/**
 * Callback routine requested an abort.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_ABORT', 4);

/**
 * The database file is locked.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_BUSY', 5);

/**
 * A table in the database is locked.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_LOCKED', 6);

/**
 * Memory allocation failed.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_NOMEM', 7);

/**
 * Attempt to write a readonly database.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_READONLY', 8);

/**
 * Operation terminated internally.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_INTERRUPT', 9);

/**
 * Disk I/O error occurred.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_IOERR', 10);

/**
 * The database disk image is malformed.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_CORRUPT', 11);

/**
 * (Internal) Table or record not found.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_NOTFOUND', 12);

/**
 * Insertion failed because database is full.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_FULL', 13);

/**
 * Unable to open the database file.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_CANTOPEN', 14);

/**
 * Database lock protocol error.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_PROTOCOL', 15);

/**
 * (Internal) Database table is empty.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_EMPTY', 16);

/**
 * The database schema changed.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_SCHEMA', 17);

/**
 * Too much data for one row of a table.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_TOOBIG', 18);

/**
 * Abort due to constraint violation.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_CONSTRAINT', 19);

/**
 * Data type mismatch.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_MISMATCH', 20);

/**
 * Library used incorrectly.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_MISUSE', 21);

/**
 * Uses of OS features not supported on host.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_NOLFS', 22);

/**
 * Authorized failed.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_AUTH', 23);
define ('SQLITE_NOTADB', 26);
define ('SQLITE_FORMAT', 24);

/**
 * Internal process has another row ready.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_ROW', 100);

/**
 * Internal process has finished executing.
 * @link http://php.net/manual/en/sqlite.constants.php
 */
define ('SQLITE_DONE', 101);

// End of SQLite v.2.0-dev

// Start of sysvshm v.

/**
 * Creates or open a shared memory segment
 * @link http://php.net/manual/en/function.shm-attach.php
 * @param key int
 * @param memsize int[optional]
 * @param perm int[optional]
 * @return int a shared memory segment identifier.
 */
function shm_attach ($key, $memsize = null, $perm = null) {}

/**
 * Removes shared memory from Unix systems
 * @link http://php.net/manual/en/function.shm-remove.php
 * @param shm_identifier int
 * @return bool 
 */
function shm_remove ($shm_identifier) {}

/**
 * Disconnects from shared memory segment
 * @link http://php.net/manual/en/function.shm-detach.php
 * @param shm_identifier int
 * @return bool 
 */
function shm_detach ($shm_identifier) {}

/**
 * Inserts or updates a variable in shared memory
 * @link http://php.net/manual/en/function.shm-put-var.php
 * @param shm_identifier int
 * @param variable_key int
 * @param variable mixed
 * @return bool 
 */
function shm_put_var ($shm_identifier, $variable_key, $variable) {}

/**
 * Returns a variable from shared memory
 * @link http://php.net/manual/en/function.shm-get-var.php
 * @param shm_identifier int
 * @param variable_key int
 * @return mixed the variable with the given key.
 */
function shm_get_var ($shm_identifier, $variable_key) {}

/**
 * Removes a variable from shared memory
 * @link http://php.net/manual/en/function.shm-remove-var.php
 * @param shm_identifier int
 * @param variable_key int
 * @return bool 
 */
function shm_remove_var ($shm_identifier, $variable_key) {}

// End of sysvshm v.

// Start of xmlreader v.0.1

/**
 * @link http://php.net/manual/en/ref.xmlreader.php
 */
class XMLReader  {
	const NONE = 0;
	const ELEMENT = 1;
	const ATTRIBUTE = 2;
	const TEXT = 3;
	const CDATA = 4;
	const ENTITY_REF = 5;
	const ENTITY = 6;
	const PI = 7;
	const COMMENT = 8;
	const DOC = 9;
	const DOC_TYPE = 10;
	const DOC_FRAGMENT = 11;
	const NOTATION = 12;
	const WHITESPACE = 13;
	const SIGNIFICANT_WHITESPACE = 14;
	const END_ELEMENT = 15;
	const END_ENTITY = 16;
	const XML_DECLARATION = 17;
	const LOADDTD = 1;
	const DEFAULTATTRS = 2;
	const VALIDATE = 3;
	const SUBST_ENTITIES = 4;


	/**
	 * Close the XMLReader input
	 * @link http://php.net/manual/en/function.xmlreader-close.php
	 * @return bool 
	 */
	public function close () {}

	/**
	 * Get the value of a named attribute
	 * @link http://php.net/manual/en/function.xmlreader-getattribute.php
	 * @param name string
	 * @return string 
	 */
	public function getAttribute ($name) {}

	/**
	 * Get the value of an attribute by index
	 * @link http://php.net/manual/en/function.xmlreader-getattributeno.php
	 * @param index int
	 * @return string 
	 */
	public function getAttributeNo ($index) {}

	/**
	 * Get the value of an attribute by localname and URI
	 * @link http://php.net/manual/en/function.xmlreader-getattributens.php
	 * @param localName string
	 * @param namespaceURI string
	 * @return string 
	 */
	public function getAttributeNs ($localName, $namespaceURI) {}

	/**
	 * Indicates if specified property has been set
	 * @link http://php.net/manual/en/function.xmlreader-getparserproperty.php
	 * @param property int
	 * @return bool 
	 */
	public function getParserProperty ($property) {}

	/**
	 * Indicates if the parsed document is valid
	 * @link http://php.net/manual/en/function.xmlreader-isvalid.php
	 * @return bool 
	 */
	public function isValid () {}

	/**
	 * Lookup namespace for a prefix
	 * @link http://php.net/manual/en/function.xmlreader-lookupnamespace.php
	 * @param prefix string
	 * @return bool 
	 */
	public function lookupNamespace ($prefix) {}

	/**
	 * Move cursor to an attribute by index
	 * @link http://php.net/manual/en/function.xmlreader-movetoattributeno.php
	 * @param index int
	 * @return bool 
	 */
	public function moveToAttributeNo ($index) {}

	/**
	 * Move cursor to a named attribute
	 * @link http://php.net/manual/en/function.xmlreader-movetoattribute.php
	 * @param name string
	 * @return bool 
	 */
	public function moveToAttribute ($name) {}

	/**
	 * Move cursor to a named attribute
	 * @link http://php.net/manual/en/function.xmlreader-movetoattributens.php
	 * @param localName string
	 * @param namespaceURI string
	 * @return bool 
	 */
	public function moveToAttributeNs ($localName, $namespaceURI) {}

	/**
	 * Position cursor on the parent Element of current Attribute
	 * @link http://php.net/manual/en/function.xmlreader-movetoelement.php
	 * @return bool 
	 */
	public function moveToElement () {}

	/**
	 * Position cursor on the first Attribute
	 * @link http://php.net/manual/en/function.xmlreader-movetofirstattribute.php
	 * @return bool 
	 */
	public function moveToFirstAttribute () {}

	/**
	 * Position cursor on the next Attribute
	 * @link http://php.net/manual/en/function.xmlreader-movetonextattribute.php
	 * @return bool 
	 */
	public function moveToNextAttribute () {}

	/**
	 * Set the URI containing the XML to parse
	 * @link http://php.net/manual/en/function.xmlreader-open.php
	 * @param URI string
	 * @param encoding string[optional]
	 * @param options int[optional]
	 * @return bool 
	 */
	public function open ($URI, $encoding = null, $options = null) {}

	/**
	 * Move to next node in document
	 * @link http://php.net/manual/en/function.xmlreader-read.php
	 * @return bool 
	 */
	public function read () {}

	/**
	 * Move cursor to next node skipping all subtrees
	 * @link http://php.net/manual/en/function.xmlreader-next.php
	 * @param localname string[optional]
	 * @return bool 
	 */
	public function next ($localname = null) {}

	public function readInnerXml () {}

	public function readOuterXml () {}

	public function readString () {}

	/**
	 * @param filename
	 */
	public function setSchema ($filename) {}

	/**
	 * Set or Unset parser options
	 * @link http://php.net/manual/en/function.xmlreader-setparserproperty.php
	 * @param property int
	 * @param value bool
	 * @return bool 
	 */
	public function setParserProperty ($property, $value) {}

	/**
	 * Set the filename or URI for a RelaxNG Schema
	 * @link http://php.net/manual/en/function.xmlreader-setrelaxngschema.php
	 * @param filename string
	 * @return bool 
	 */
	public function setRelaxNGSchema ($filename) {}

	/**
	 * Set the data containing a RelaxNG Schema
	 * @link http://php.net/manual/en/function.xmlreader-setrelaxngschemasource.php
	 * @param source string
	 * @return bool 
	 */
	public function setRelaxNGSchemaSource ($source) {}

	/**
	 * Set the data containing the XML to parse
	 * @link http://php.net/manual/en/function.xmlreader-xml.php
	 * @param source string
	 * @param encoding string[optional]
	 * @param options int[optional]
	 * @return bool 
	 */
	public function XML ($source, $encoding = null, $options = null) {}

	/**
	 * Returns a copy of the current node as a DOM object
	 * @link http://php.net/manual/en/function.xmlreader-expand.php
	 * @return DOMNode 
	 */
	public function expand () {}

}
// End of xmlreader v.0.1

// Start of zip v.1.4.0

class ZipArchive  {
	const CREATE = 1;
	const EXCL = 2;
	const CHECKCONS = 4;
	const OVERWRITE = 8;
	const FL_NOCASE = 1;
	const FL_NODIR = 2;
	const FL_COMPRESSED = 4;
	const FL_UNCHANGED = 8;
	const CM_DEFAULT = -1;
	const CM_STORE = 0;
	const CM_SHRINK = 1;
	const CM_REDUCE_1 = 2;
	const CM_REDUCE_2 = 3;
	const CM_REDUCE_3 = 4;
	const CM_REDUCE_4 = 5;
	const CM_IMPLODE = 6;
	const CM_DEFLATE = 8;
	const CM_DEFLATE64 = 9;
	const CM_PKWARE_IMPLODE = 10;
	const ER_OK = 0;
	const ER_MULTIDISK = 1;
	const ER_RENAME = 2;
	const ER_CLOSE = 3;
	const ER_SEEK = 4;
	const ER_READ = 5;
	const ER_WRITE = 6;
	const ER_CRC = 7;
	const ER_ZIPCLOSED = 8;
	const ER_NOENT = 9;
	const ER_EXISTS = 10;
	const ER_OPEN = 11;
	const ER_TMPOPEN = 12;
	const ER_ZLIB = 13;
	const ER_MEMORY = 14;
	const ER_CHANGED = 15;
	const ER_COMPNOTSUPP = 16;
	const ER_EOF = 17;
	const ER_INVAL = 18;
	const ER_NOZIP = 19;
	const ER_INTERNAL = 20;
	const ER_INCONS = 21;
	const ER_REMOVE = 22;
	const ER_DELETED = 23;


	/**
	 * Open a ZIP file archive
	 * @link http://php.net/manual/en/function.ziparchive-open.php
	 * @param filename string
	 * @param flags int[optional]
	 * @return mixed true on success or the error code.
	 */
	public function open ($filename, $flags = null) {}

	/**
	 * Close the active archive (opened or newly created)
	 * @link http://php.net/manual/en/function.ziparchive-close.php
	 * @return bool 
	 */
	public function close () {}

	/**
	 * Add a new directory
	 * @link http://php.net/manual/en/function.ziparchive-addemptydir.php
	 * @param dirname string
	 * @return bool 
	 */
	public function addEmptyDir ($dirname) {}

	/**
	 * Add a file to a ZIP archive using its contents
	 * @link http://php.net/manual/en/function.ziparchive-addfromstring.php
	 * @param localname string
	 * @param contents string
	 * @return bool 
	 */
	public function addFromString ($localname, $contents) {}

	/**
	 * Adds a file to a ZIP archive from the given path
	 * @link http://php.net/manual/en/function.ziparchive-addfile.php
	 * @param filename string
	 * @param localname string[optional]
	 * @return bool 
	 */
	public function addFile ($filename, $localname = null) {}

	/**
	 * Renames an entry defined by its index
	 * @link http://php.net/manual/en/function.ziparchive-renameindex.php
	 * @param index int
	 * @param newname string
	 * @return bool 
	 */
	public function renameIndex ($index, $newname) {}

	/**
	 * Renames an entry defined by its name
	 * @link http://php.net/manual/en/function.ziparchive-renamename.php
	 * @param name string
	 * @param newname string
	 * @return bool 
	 */
	public function renameName ($name, $newname) {}

	/**
	 * Set the comment of a ZIP archive
	 * @link http://php.net/manual/en/function.ziparchive-setarchivecomment.php
	 * @param comment string
	 * @return mixed 
	 */
	public function setArchiveComment ($comment) {}

	/**
	 * Returns the Zip archive comment
	 * @link http://php.net/manual/en/function.ziparchive-getarchivecomment.php
	 * @return string the Zip archive comment or false on failure.
	 */
	public function getArchiveComment () {}

	/**
	 * Set the comment of an entry defined by its index
	 * @link http://php.net/manual/en/function.ziparchive-setcommentindex.php
	 * @param index int
	 * @param comment string
	 * @return mixed 
	 */
	public function setCommentIndex ($index, $comment) {}

	/**
	 * Set the comment of an entry defined by its name
	 * @link http://php.net/manual/en/function.ziparchive-setCommentName.php
	 * @param name string
	 * @param comment string
	 * @return mixed 
	 */
	public function setCommentName ($name, $comment) {}

	/**
	 * Returns the comment of an entry using the entry index
	 * @link http://php.net/manual/en/function.ziparchive-getcommentindex.php
	 * @param index int
	 * @param flags int[optional]
	 * @return string the comment on success or false on failure.
	 */
	public function getCommentIndex ($index, $flags = null) {}

	/**
	 * Returns the comment of an entry using the entry name
	 * @link http://php.net/manual/en/function.ziparchive-getcommentname.php
	 * @param name string
	 * @param flags int[optional]
	 * @return string the comment on success or false on failure.
	 */
	public function getCommentName ($name, $flags = null) {}

	/**
	 * delete an entry in the archive using its index
	 * @link http://php.net/manual/en/function.ziparchive-deleteindex.php
	 * @param index int
	 * @return bool 
	 */
	public function deleteIndex ($index) {}

	/**
	 * delete an entry in the archive using its name
	 * @link http://php.net/manual/en/function.ziparchive-deletename.php
	 * @param name string
	 * @return bool 
	 */
	public function deleteName ($name) {}

	/**
	 * Get the details of an entry defined by its name.
	 * @link http://php.net/manual/en/function.ziparchive-statname.php
	 * @param name name
	 * @param flags int[optional]
	 * @return mixed an array containing the entry details or false on failure.
	 */
	public function statName ($name, $flags = null) {}

	/**
	 * Get the details of an entry defined by its index.
	 * @link http://php.net/manual/en/function.ziparchive-statindex.php
	 * @param index int
	 * @param flags int[optional]
	 * @return mixed an array containing the entry details or false on failure.
	 */
	public function statIndex ($index, $flags = null) {}

	/**
	 * Returns the index of the entry in the archive
	 * @link http://php.net/manual/en/function.ziparchive-locatename.php
	 * @param name string
	 * @param flags int[optional]
	 * @return mixed the index of the entry on success or false on failure.
	 */
	public function locateName ($name, $flags = null) {}

	/**
	 * Returns the name of an entry using its index
	 * @link http://php.net/manual/en/function.ziparchive-getnameindex.php
	 * @param index int
	 * @return string the name on success or false on failure.
	 */
	public function getNameIndex ($index) {}

	/**
	 * Revert all global changes done in the archive.
	 * @link http://php.net/manual/en/function.ziparchive-unchangearchive.php
	 * @return mixed 
	 */
	public function unchangeArchive () {}

	/**
	 * Undo all changes done in the archive.
	 * @link http://php.net/manual/en/function.ziparchive-unchangeall.php
	 * @return mixed 
	 */
	public function unchangeAll () {}

	/**
	 * Revert all changes done to an entry at the given index.
	 * @link http://php.net/manual/en/function.ziparchive-unchangeindex.php
	 * @param index int
	 * @return mixed 
	 */
	public function unchangeIndex ($index) {}

	/**
	 * Revert all changes done to an entry with the given name.
	 * @link http://php.net/manual/en/function.ziparchive-unchangename.php
	 * @param name string
	 * @return mixed 
	 */
	public function unchangeName ($name) {}

	/**
	 * Extract the archive contents
	 * @link http://php.net/manual/en/function.ziparchive-extractto.php
	 * @param destination string
	 * @param entries mixed[optional]
	 * @return mixed 
	 */
	public function extractTo ($destination, $entries = null) {}

	/**
	 * Returns the entry contents using its name.
	 * @link http://php.net/manual/en/function.ziparchive-getfromname.php
	 * @param name string
	 * @param flags int[optional]
	 * @return mixed the contents of the entry on success or false on failure.
	 */
	public function getFromName ($name, $flags = null) {}

	/**
	 * Returns the entry contents using its index.
	 * @link http://php.net/manual/en/function.ziparchive-getfromindex.php
	 * @param index int
	 * @param flags int[optional]
	 * @return mixed the contents of the entry on success or false on failure.
	 */
	public function getFromIndex ($index, $flags = null) {}

	/**
	 * Get a file handler to the entry defined by its name (read only).
	 * @link http://php.net/manual/en/function.ziparchive-getstream.php
	 * @param name string
	 * @return resource a file pointer (resource) on success or false on failure.
	 */
	public function getStream ($name) {}

}

/**
 * Open a ZIP file archive
 * @link http://php.net/manual/en/function.zip-open.php
 * @param filename string
 * @return mixed a resource handle for later use with
 */
function zip_open ($filename) {}

/**
 * Close a ZIP file archive
 * @link http://php.net/manual/en/function.zip-close.php
 * @param zip resource
 * @return void 
 */
function zip_close ($zip) {}

/**
 * Read next entry in a ZIP file archive
 * @link http://php.net/manual/en/function.zip-read.php
 * @param zip resource
 * @return mixed a directory entry resource for later use with the
 */
function zip_read ($zip) {}

/**
 * Open a directory entry for reading
 * @link http://php.net/manual/en/function.zip-entry-open.php
 * @param zip resource
 * @param zip_entry resource
 * @param mode string[optional]
 * @return bool 
 */
function zip_entry_open ($zip, $zip_entry, $mode = null) {}

/**
 * Close a directory entry
 * @link http://php.net/manual/en/function.zip-entry-close.php
 * @param zip_entry resource
 * @return bool 
 */
function zip_entry_close ($zip_entry) {}

/**
 * Read from an open directory entry
 * @link http://php.net/manual/en/function.zip-entry-read.php
 * @param zip_entry resource
 * @param length int[optional]
 * @return string the data read, or false if the end of the file is
 */
function zip_entry_read ($zip_entry, $length = null) {}

/**
 * Retrieve the actual file size of a directory entry
 * @link http://php.net/manual/en/function.zip-entry-filesize.php
 * @param zip_entry resource
 * @return int 
 */
function zip_entry_filesize ($zip_entry) {}

/**
 * Retrieve the name of a directory entry
 * @link http://php.net/manual/en/function.zip-entry-name.php
 * @param zip_entry resource
 * @return string 
 */
function zip_entry_name ($zip_entry) {}

/**
 * Retrieve the compressed size of a directory entry
 * @link http://php.net/manual/en/function.zip-entry-compressedsize.php
 * @param zip_entry resource
 * @return int 
 */
function zip_entry_compressedsize ($zip_entry) {}

/**
 * Retrieve the compression method of a directory entry
 * @link http://php.net/manual/en/function.zip-entry-compressionmethod.php
 * @param zip_entry resource
 * @return string 
 */
function zip_entry_compressionmethod ($zip_entry) {}

// End of zip v.1.4.0

// Start of calendar v.

/**
 * Converts Julian Day Count to Gregorian date
 * @link http://php.net/manual/en/function.jdtogregorian.php
 * @param julianday int
 * @return string 
 */
function jdtogregorian ($julianday) {}

/**
 * Converts a Gregorian date to Julian Day Count
 * @link http://php.net/manual/en/function.gregoriantojd.php
 * @param month int
 * @param day int
 * @param year int
 * @return int 
 */
function gregoriantojd ($month, $day, $year) {}

/**
 * Converts a Julian Day Count to a Julian Calendar Date
 * @link http://php.net/manual/en/function.jdtojulian.php
 * @param julianday int
 * @return string 
 */
function jdtojulian ($julianday) {}

/**
 * Converts a Julian Calendar date to Julian Day Count
 * @link http://php.net/manual/en/function.juliantojd.php
 * @param month int
 * @param day int
 * @param year int
 * @return int 
 */
function juliantojd ($month, $day, $year) {}

/**
 * Converts a Julian day count to a Jewish calendar date
 * @link http://php.net/manual/en/function.jdtojewish.php
 * @param juliandaycount int
 * @param hebrew bool[optional]
 * @param fl int[optional]
 * @return string 
 */
function jdtojewish ($juliandaycount, $hebrew = null, $fl = null) {}

/**
 * Converts a date in the Jewish Calendar to Julian Day Count
 * @link http://php.net/manual/en/function.jewishtojd.php
 * @param month int
 * @param day int
 * @param year int
 * @return int 
 */
function jewishtojd ($month, $day, $year) {}

/**
 * Converts a Julian Day Count to the French Republican Calendar
 * @link http://php.net/manual/en/function.jdtofrench.php
 * @param juliandaycount int
 * @return string 
 */
function jdtofrench ($juliandaycount) {}

/**
 * Converts a date from the French Republican Calendar to a Julian Day Count
 * @link http://php.net/manual/en/function.frenchtojd.php
 * @param month int
 * @param day int
 * @param year int
 * @return int 
 */
function frenchtojd ($month, $day, $year) {}

/**
 * Returns the day of the week
 * @link http://php.net/manual/en/function.jddayofweek.php
 * @param julianday int
 * @param mode int[optional]
 * @return mixed 
 */
function jddayofweek ($julianday, $mode = null) {}

/**
 * Returns a month name
 * @link http://php.net/manual/en/function.jdmonthname.php
 * @param julianday int
 * @param mode int
 * @return string 
 */
function jdmonthname ($julianday, $mode) {}

/**
 * Get Unix timestamp for midnight on Easter of a given year
 * @link http://php.net/manual/en/function.easter-date.php
 * @param year int[optional]
 * @return int 
 */
function easter_date ($year = null) {}

/**
 * Get number of days after March 21 on which Easter falls for a given year
 * @link http://php.net/manual/en/function.easter-days.php
 * @param year int[optional]
 * @param method int[optional]
 * @return int 
 */
function easter_days ($year = null, $method = null) {}

/**
 * Convert Unix timestamp to Julian Day
 * @link http://php.net/manual/en/function.unixtojd.php
 * @param timestamp int[optional]
 * @return int 
 */
function unixtojd ($timestamp = null) {}

/**
 * Convert Julian Day to Unix timestamp
 * @link http://php.net/manual/en/function.jdtounix.php
 * @param jday int
 * @return int 
 */
function jdtounix ($jday) {}

/**
 * Converts from a supported calendar to Julian Day Count
 * @link http://php.net/manual/en/function.cal-to-jd.php
 * @param calendar int
 * @param month int
 * @param day int
 * @param year int
 * @return int 
 */
function cal_to_jd ($calendar, $month, $day, $year) {}

/**
 * Converts from Julian Day Count to a supported calendar
 * @link http://php.net/manual/en/function.cal-from-jd.php
 * @param jd int
 * @param calendar int
 * @return array an array containing calendar information like month, day, year,
 */
function cal_from_jd ($jd, $calendar) {}

/**
 * Return the number of days in a month for a given year and calendar
 * @link http://php.net/manual/en/function.cal-days-in-month.php
 * @param calendar int
 * @param month int
 * @param year int
 * @return int 
 */
function cal_days_in_month ($calendar, $month, $year) {}

/**
 * Returns information about a particular calendar
 * @link http://php.net/manual/en/function.cal-info.php
 * @param calendar int[optional]
 * @return array 
 */
function cal_info ($calendar = null) {}

define ('CAL_GREGORIAN', 0);
define ('CAL_JULIAN', 1);
define ('CAL_JEWISH', 2);
define ('CAL_FRENCH', 3);
define ('CAL_NUM_CALS', 4);
define ('CAL_DOW_DAYNO', 0);
define ('CAL_DOW_SHORT', 1);
define ('CAL_DOW_LONG', 2);
define ('CAL_MONTH_GREGORIAN_SHORT', 0);
define ('CAL_MONTH_GREGORIAN_LONG', 1);
define ('CAL_MONTH_JULIAN_SHORT', 2);
define ('CAL_MONTH_JULIAN_LONG', 3);
define ('CAL_MONTH_JEWISH', 4);
define ('CAL_MONTH_FRENCH', 5);
define ('CAL_EASTER_DEFAULT', 0);
define ('CAL_EASTER_ROMAN', 1);
define ('CAL_EASTER_ALWAYS_GREGORIAN', 2);
define ('CAL_EASTER_ALWAYS_JULIAN', 3);
define ('CAL_JEWISH_ADD_ALAFIM_GERESH', 2);
define ('CAL_JEWISH_ADD_ALAFIM', 4);
define ('CAL_JEWISH_ADD_GERESHAYIM', 8);

// End of calendar v.

// Start of exif v.1.4

/**
 * Reads the <acronym>EXIF</acronym> headers from <acronym>JPEG</acronym> or <acronym>TIFF</acronym>
 * @link http://php.net/manual/en/function.exif-read-data.php
 * @param filename string
 * @param sections string[optional]
 * @param arrays bool[optional]
 * @param thumbnail bool[optional]
 * @return array 
 */
function exif_read_data ($filename, $sections = null, $arrays = null, $thumbnail = null) {}

/**
 * &Alias; <function>exif_read_data</function>
 * @link http://php.net/manual/en/function.read-exif-data.php
 * @param filename
 * @param sections_needed[optional]
 * @param sub_arrays[optional]
 * @param read_thumbnail[optional]
 */
function read_exif_data ($filename, $sections_needed, $sub_arrays, $read_thumbnail) {}

/**
 * Get the header name for an index
 * @link http://php.net/manual/en/function.exif-tagname.php
 * @param index string
 * @return string the header name, or false if index is
 */
function exif_tagname ($index) {}

/**
 * Retrieve the embedded thumbnail of a TIFF or JPEG image
 * @link http://php.net/manual/en/function.exif-thumbnail.php
 * @param filename string
 * @param width int[optional]
 * @param height int[optional]
 * @param imagetype int[optional]
 * @return string the embedded thumbnail, or false if the image contains no
 */
function exif_thumbnail ($filename, &$width = null, &$height = null, &$imagetype = null) {}

/**
 * Determine the type of an image
 * @link http://php.net/manual/en/function.exif-imagetype.php
 * @param filename string
 * @return int 
 */
function exif_imagetype ($filename) {}

define ('EXIF_USE_MBSTRING', 0);

// End of exif v.1.4

// Start of gmp v.

/**
 * Create GMP number
 * @link http://php.net/manual/en/function.gmp-init.php
 * @param number mixed
 * @param base int[optional]
 * @return resource 
 */
function gmp_init ($number, $base = null) {}

/**
 * Convert GMP number to integer
 * @link http://php.net/manual/en/function.gmp-intval.php
 * @param gmpnumber resource
 * @return int 
 */
function gmp_intval ($gmpnumber) {}

/**
 * Convert GMP number to string
 * @link http://php.net/manual/en/function.gmp-strval.php
 * @param gmpnumber resource
 * @param base int[optional]
 * @return string 
 */
function gmp_strval ($gmpnumber, $base = null) {}

/**
 * Add numbers
 * @link http://php.net/manual/en/function.gmp-add.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_add ($a, $b) {}

/**
 * Subtract numbers
 * @link http://php.net/manual/en/function.gmp-sub.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_sub ($a, $b) {}

/**
 * Multiply numbers
 * @link http://php.net/manual/en/function.gmp-mul.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_mul ($a, $b) {}

/**
 * Divide numbers and get quotient and remainder
 * @link http://php.net/manual/en/function.gmp-div-qr.php
 * @param n resource
 * @param d resource
 * @param round int[optional]
 * @return array an array, with the first
 */
function gmp_div_qr ($n, $d, $round = null) {}

/**
 * Divide numbers
 * @link http://php.net/manual/en/function.gmp-div-q.php
 * @param a resource
 * @param b resource
 * @param round int[optional]
 * @return resource 
 */
function gmp_div_q ($a, $b, $round = null) {}

/**
 * Remainder of the division of numbers
 * @link http://php.net/manual/en/function.gmp-div-r.php
 * @param n resource
 * @param d resource
 * @param round int[optional]
 * @return resource 
 */
function gmp_div_r ($n, $d, $round = null) {}

/**
 * &Alias; <function>gmp_div_q</function>
 * @link http://php.net/manual/en/function.gmp-div.php
 * @param a
 * @param b
 * @param round[optional]
 */
function gmp_div ($a, $b, $round) {}

/**
 * Modulo operation
 * @link http://php.net/manual/en/function.gmp-mod.php
 * @param n resource
 * @param d resource
 * @return resource 
 */
function gmp_mod ($n, $d) {}

/**
 * Exact division of numbers
 * @link http://php.net/manual/en/function.gmp-divexact.php
 * @param n resource
 * @param d resource
 * @return resource 
 */
function gmp_divexact ($n, $d) {}

/**
 * Negate number
 * @link http://php.net/manual/en/function.gmp-neg.php
 * @param a resource
 * @return resource -a, as a GMP number.
 */
function gmp_neg ($a) {}

/**
 * Absolute value
 * @link http://php.net/manual/en/function.gmp-abs.php
 * @param a resource
 * @return resource the absolute value of a, as a GMP number.
 */
function gmp_abs ($a) {}

/**
 * Factorial
 * @link http://php.net/manual/en/function.gmp-fact.php
 * @param a int
 * @return resource 
 */
function gmp_fact ($a) {}

/**
 * Calculate square root
 * @link http://php.net/manual/en/function.gmp-sqrt.php
 * @param a resource
 * @return resource 
 */
function gmp_sqrt ($a) {}

/**
 * Square root with remainder
 * @link http://php.net/manual/en/function.gmp-sqrtrem.php
 * @param a resource
 * @return array array where first element is the integer square root of
 */
function gmp_sqrtrem ($a) {}

/**
 * Raise number into power
 * @link http://php.net/manual/en/function.gmp-pow.php
 * @param base resource
 * @param exp int
 * @return resource 
 */
function gmp_pow ($base, $exp) {}

/**
 * Raise number into power with modulo
 * @link http://php.net/manual/en/function.gmp-powm.php
 * @param base resource
 * @param exp resource
 * @param mod resource
 * @return resource 
 */
function gmp_powm ($base, $exp, $mod) {}

/**
 * Perfect square check
 * @link http://php.net/manual/en/function.gmp-perfect-square.php
 * @param a resource
 * @return bool true if a is a perfect square,
 */
function gmp_perfect_square ($a) {}

/**
 * Check if number is "probably prime"
 * @link http://php.net/manual/en/function.gmp-prob-prime.php
 * @param a resource
 * @param reps int[optional]
 * @return int 
 */
function gmp_prob_prime ($a, $reps = null) {}

/**
 * Calculate GCD
 * @link http://php.net/manual/en/function.gmp-gcd.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_gcd ($a, $b) {}

/**
 * Calculate GCD and multipliers
 * @link http://php.net/manual/en/function.gmp-gcdext.php
 * @param a resource
 * @param b resource
 * @return array 
 */
function gmp_gcdext ($a, $b) {}

/**
 * Inverse by modulo
 * @link http://php.net/manual/en/function.gmp-invert.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_invert ($a, $b) {}

/**
 * Jacobi symbol
 * @link http://php.net/manual/en/function.gmp-jacobi.php
 * @param a resource
 * @param p resource
 * @return int 
 */
function gmp_jacobi ($a, $p) {}

/**
 * Legendre symbol
 * @link http://php.net/manual/en/function.gmp-legendre.php
 * @param a resource
 * @param p resource
 * @return int 
 */
function gmp_legendre ($a, $p) {}

/**
 * Compare numbers
 * @link http://php.net/manual/en/function.gmp-cmp.php
 * @param a resource
 * @param b resource
 * @return int a positive value if a &gt; b, zero if
 */
function gmp_cmp ($a, $b) {}

/**
 * Sign of number
 * @link http://php.net/manual/en/function.gmp-sign.php
 * @param a resource
 * @return int 1 if a is positive,
 */
function gmp_sign ($a) {}

/**
 * Random number
 * @link http://php.net/manual/en/function.gmp-random.php
 * @param limiter int
 * @return resource 
 */
function gmp_random ($limiter) {}

/**
 * Logical AND
 * @link http://php.net/manual/en/function.gmp-and.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_and ($a, $b) {}

/**
 * Logical OR
 * @link http://php.net/manual/en/function.gmp-or.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_or ($a, $b) {}

/**
 * Calculates one's complement
 * @link http://php.net/manual/en/function.gmp-com.php
 * @param a resource
 * @return resource the one's complement of a, as a GMP number.
 */
function gmp_com ($a) {}

/**
 * Logical XOR
 * @link http://php.net/manual/en/function.gmp-xor.php
 * @param a resource
 * @param b resource
 * @return resource 
 */
function gmp_xor ($a, $b) {}

/**
 * Set bit
 * @link http://php.net/manual/en/function.gmp-setbit.php
 * @param a resource
 * @param index int
 * @param set_clear bool[optional]
 * @return void 
 */
function gmp_setbit (&$a, $index, $set_clear = null) {}

/**
 * Clear bit
 * @link http://php.net/manual/en/function.gmp-clrbit.php
 * @param a resource
 * @param index int
 * @return void 
 */
function gmp_clrbit (&$a, $index) {}

/**
 * Scan for 0
 * @link http://php.net/manual/en/function.gmp-scan0.php
 * @param a resource
 * @param start int
 * @return int the index of the found bit, as an integer. The
 */
function gmp_scan0 ($a, $start) {}

/**
 * Scan for 1
 * @link http://php.net/manual/en/function.gmp-scan1.php
 * @param a resource
 * @param start int
 * @return int the index of the found bit, as an integer.
 */
function gmp_scan1 ($a, $start) {}

/**
 * Population count
 * @link http://php.net/manual/en/function.gmp-popcount.php
 * @param a resource
 * @return int 
 */
function gmp_popcount ($a) {}

/**
 * Hamming distance
 * @link http://php.net/manual/en/function.gmp-hamdist.php
 * @param a resource
 * @param b resource
 * @return int 
 */
function gmp_hamdist ($a, $b) {}

/**
 * Find next prime number
 * @link http://php.net/manual/en/function.gmp-nextprime.php
 * @param a int
 * @return resource 
 */
function gmp_nextprime ($a) {}

define ('GMP_ROUND_ZERO', 0);
define ('GMP_ROUND_PLUSINF', 1);
define ('GMP_ROUND_MINUSINF', 2);

/**
 * The GMP library version
 * @link http://php.net/manual/en/gmp.constants.php
 */
define ('GMP_VERSION', "4.2.1");

// End of gmp v.

// Start of imap v.

/**
 * Open an IMAP stream to a mailbox
 * @link http://php.net/manual/en/function.imap-open.php
 * @param mailbox string
 * @param username string
 * @param password string
 * @param options int[optional]
 * @param n_retries int[optional]
 * @return resource an IMAP stream on success or false on error.
 */
function imap_open ($mailbox, $username, $password, $options = null, $n_retries = null) {}

/**
 * Reopen IMAP stream to new mailbox
 * @link http://php.net/manual/en/function.imap-reopen.php
 * @param imap_stream resource
 * @param mailbox string
 * @param options int[optional]
 * @param n_retries int[optional]
 * @return bool 
 */
function imap_reopen ($imap_stream, $mailbox, $options = null, $n_retries = null) {}

/**
 * Close an IMAP stream
 * @link http://php.net/manual/en/function.imap-close.php
 * @param imap_stream resource
 * @param flag int[optional]
 * @return bool 
 */
function imap_close ($imap_stream, $flag = null) {}

/**
 * Gets the number of messages in the current mailbox
 * @link http://php.net/manual/en/function.imap-num-msg.php
 * @param imap_stream resource
 * @return int 
 */
function imap_num_msg ($imap_stream) {}

/**
 * Gets the number of recent messages in current mailbox
 * @link http://php.net/manual/en/function.imap-num-recent.php
 * @param imap_stream resource
 * @return int the number of recent messages in the current mailbox, as an
 */
function imap_num_recent ($imap_stream) {}

/**
 * Returns headers for all messages in a mailbox
 * @link http://php.net/manual/en/function.imap-headers.php
 * @param imap_stream resource
 * @return array an array of string formatted with header info. One
 */
function imap_headers ($imap_stream) {}

/**
 * Read the header of the message
 * @link http://php.net/manual/en/function.imap-headerinfo.php
 * @param imap_stream resource
 * @param msg_number int
 * @param fromlength int[optional]
 * @param subjectlength int[optional]
 * @param defaulthost string[optional]
 * @return object the information in an object with following properties:
 */
function imap_headerinfo ($imap_stream, $msg_number, $fromlength = null, $subjectlength = null, $defaulthost = null) {}

/**
 * Parse mail headers from a string
 * @link http://php.net/manual/en/function.imap-rfc822-parse-headers.php
 * @param headers string
 * @param defaulthost string[optional]
 * @return object an object similar to the one returned by
 */
function imap_rfc822_parse_headers ($headers, $defaulthost = null) {}

/**
 * Returns a properly formatted email address given the mailbox, host, and personal info
 * @link http://php.net/manual/en/function.imap-rfc822-write-address.php
 * @param mailbox string
 * @param host string
 * @param personal string
 * @return string a string properly formatted email address as defined in
 */
function imap_rfc822_write_address ($mailbox, $host, $personal) {}

/**
 * Parses an address string
 * @link http://php.net/manual/en/function.imap-rfc822-parse-adrlist.php
 * @param address string
 * @param default_host string
 * @return array an array of objects. The objects properties are:
 */
function imap_rfc822_parse_adrlist ($address, $default_host) {}

/**
 * Read the message body
 * @link http://php.net/manual/en/function.imap-body.php
 * @param imap_stream resource
 * @param msg_number int
 * @param options int[optional]
 * @return string the body of the specified message, as a string.
 */
function imap_body ($imap_stream, $msg_number, $options = null) {}

/**
 * Read the structure of a specified body section of a specific message
 * @link http://php.net/manual/en/function.imap-bodystruct.php
 * @param imap_stream resource
 * @param msg_number int
 * @param section string
 * @return object the information in an object, for a detailed description
 */
function imap_bodystruct ($imap_stream, $msg_number, $section) {}

/**
 * Fetch a particular section of the body of the message
 * @link http://php.net/manual/en/function.imap-fetchbody.php
 * @param imap_stream resource
 * @param msg_number int
 * @param part_number string
 * @param options int[optional]
 * @return string a particular section of the body of the specified messages as a
 */
function imap_fetchbody ($imap_stream, $msg_number, $part_number, $options = null) {}

/**
 * Save a specific body section to a file
 * @link http://php.net/manual/en/function.imap-savebody.php
 * @param imap_stream resource
 * @param file mixed
 * @param msg_number int
 * @param part_number string[optional]
 * @param options int[optional]
 * @return bool 
 */
function imap_savebody ($imap_stream, $file, $msg_number, $part_number = null, $options = null) {}

/**
 * Returns header for a message
 * @link http://php.net/manual/en/function.imap-fetchheader.php
 * @param imap_stream resource
 * @param msg_number int
 * @param options int[optional]
 * @return string the header of the specified message as a text string.
 */
function imap_fetchheader ($imap_stream, $msg_number, $options = null) {}

/**
 * Read the structure of a particular message
 * @link http://php.net/manual/en/function.imap-fetchstructure.php
 * @param imap_stream resource
 * @param msg_number int
 * @param options int[optional]
 * @return object an object includes the envelope, internal date, size, flags and
 */
function imap_fetchstructure ($imap_stream, $msg_number, $options = null) {}

/**
 * Delete all messages marked for deletion
 * @link http://php.net/manual/en/function.imap-expunge.php
 * @param imap_stream resource
 * @return bool true.
 */
function imap_expunge ($imap_stream) {}

/**
 * Mark a message for deletion from current mailbox
 * @link http://php.net/manual/en/function.imap-delete.php
 * @param imap_stream int
 * @param msg_number int
 * @param options int[optional]
 * @return bool true.
 */
function imap_delete ($imap_stream, $msg_number, $options = null) {}

/**
 * Unmark the message which is marked deleted
 * @link http://php.net/manual/en/function.imap-undelete.php
 * @param imap_stream resource
 * @param msg_number int
 * @param flags int[optional]
 * @return bool 
 */
function imap_undelete ($imap_stream, $msg_number, $flags = null) {}

/**
 * Check current mailbox
 * @link http://php.net/manual/en/function.imap-check.php
 * @param imap_stream resource
 * @return object the information in an object with following properties:
 */
function imap_check ($imap_stream) {}

/**
 * Copy specified messages to a mailbox
 * @link http://php.net/manual/en/function.imap-mail-copy.php
 * @param imap_stream resource
 * @param msglist string
 * @param mailbox string
 * @param options int[optional]
 * @return bool 
 */
function imap_mail_copy ($imap_stream, $msglist, $mailbox, $options = null) {}

/**
 * Move specified messages to a mailbox
 * @link http://php.net/manual/en/function.imap-mail-move.php
 * @param imap_stream resource
 * @param msglist string
 * @param mailbox string
 * @param options int[optional]
 * @return bool 
 */
function imap_mail_move ($imap_stream, $msglist, $mailbox, $options = null) {}

/**
 * Create a MIME message based on given envelope and body sections
 * @link http://php.net/manual/en/function.imap-mail-compose.php
 * @param envelope array
 * @param body array
 * @return string the MIME message.
 */
function imap_mail_compose (array $envelope, array $body) {}

/**
 * Create a new mailbox
 * @link http://php.net/manual/en/function.imap-createmailbox.php
 * @param imap_stream resource
 * @param mailbox string
 * @return bool 
 */
function imap_createmailbox ($imap_stream, $mailbox) {}

/**
 * Rename an old mailbox to new mailbox
 * @link http://php.net/manual/en/function.imap-renamemailbox.php
 * @param imap_stream resource
 * @param old_mbox string
 * @param new_mbox string
 * @return bool 
 */
function imap_renamemailbox ($imap_stream, $old_mbox, $new_mbox) {}

/**
 * Delete a mailbox
 * @link http://php.net/manual/en/function.imap-deletemailbox.php
 * @param imap_stream resource
 * @param mailbox string
 * @return bool 
 */
function imap_deletemailbox ($imap_stream, $mailbox) {}

/**
 * Subscribe to a mailbox
 * @link http://php.net/manual/en/function.imap-subscribe.php
 * @param imap_stream resource
 * @param mailbox string
 * @return bool 
 */
function imap_subscribe ($imap_stream, $mailbox) {}

/**
 * Unsubscribe from a mailbox
 * @link http://php.net/manual/en/function.imap-unsubscribe.php
 * @param imap_stream string
 * @param mailbox string
 * @return bool 
 */
function imap_unsubscribe ($imap_stream, $mailbox) {}

/**
 * Append a string message to a specified mailbox
 * @link http://php.net/manual/en/function.imap-append.php
 * @param imap_stream resource
 * @param mailbox string
 * @param message string
 * @param options string[optional]
 * @return bool 
 */
function imap_append ($imap_stream, $mailbox, $message, $options = null) {}

/**
 * Check if the IMAP stream is still active
 * @link http://php.net/manual/en/function.imap-ping.php
 * @param imap_stream resource
 * @return bool true if the stream is still alive, false otherwise.
 */
function imap_ping ($imap_stream) {}

/**
 * Decode BASE64 encoded text
 * @link http://php.net/manual/en/function.imap-base64.php
 * @param text string
 * @return string the decoded message as a string.
 */
function imap_base64 ($text) {}

/**
 * Convert a quoted-printable string to an 8 bit string
 * @link http://php.net/manual/en/function.imap-qprint.php
 * @param string string
 * @return string an 8 bits string.
 */
function imap_qprint ($string) {}

/**
 * Convert an 8bit string to a quoted-printable string
 * @link http://php.net/manual/en/function.imap-8bit.php
 * @param string string
 * @return string a quoted-printable string.
 */
function imap_8bit ($string) {}

/**
 * Convert an 8bit string to a base64 string
 * @link http://php.net/manual/en/function.imap-binary.php
 * @param string string
 * @return string a base64 encoded string.
 */
function imap_binary ($string) {}

/**
 * Converts MIME-encoded text to UTF-8
 * @link http://php.net/manual/en/function.imap-utf8.php
 * @param mime_encoded_text string
 * @return string an UTF-8 encoded string.
 */
function imap_utf8 ($mime_encoded_text) {}

/**
 * Returns status information on a mailbox
 * @link http://php.net/manual/en/function.imap-status.php
 * @param imap_stream resource
 * @param mailbox string
 * @param options int
 * @return object 
 */
function imap_status ($imap_stream, $mailbox, $options) {}

/**
 * Get information about the current mailbox
 * @link http://php.net/manual/en/function.imap-mailboxmsginfo.php
 * @param imap_stream resource
 * @return object the information in an object with following properties:
 */
function imap_mailboxmsginfo ($imap_stream) {}

/**
 * Sets flags on messages
 * @link http://php.net/manual/en/function.imap-setflag-full.php
 * @param imap_stream resource
 * @param sequence string
 * @param flag string
 * @param options int[optional]
 * @return bool 
 */
function imap_setflag_full ($imap_stream, $sequence, $flag, $options = null) {}

/**
 * Clears flags on messages
 * @link http://php.net/manual/en/function.imap-clearflag-full.php
 * @param imap_stream resource
 * @param sequence string
 * @param flag string
 * @param options string[optional]
 * @return bool 
 */
function imap_clearflag_full ($imap_stream, $sequence, $flag, $options = null) {}

/**
 * Gets and sort messages
 * @link http://php.net/manual/en/function.imap-sort.php
 * @param imap_stream resource
 * @param criteria int
 * @param reverse int
 * @param options int[optional]
 * @param search_criteria string[optional]
 * @param charset string[optional]
 * @return array an array of message numbers sorted by the given
 */
function imap_sort ($imap_stream, $criteria, $reverse, $options = null, $search_criteria = null, $charset = null) {}

/**
 * This function returns the UID for the given message sequence number
 * @link http://php.net/manual/en/function.imap-uid.php
 * @param imap_stream resource
 * @param msg_number int
 * @return int 
 */
function imap_uid ($imap_stream, $msg_number) {}

/**
 * Gets the message sequence number for the given UID
 * @link http://php.net/manual/en/function.imap-msgno.php
 * @param imap_stream resource
 * @param uid int
 * @return int the message sequence number for the given
 */
function imap_msgno ($imap_stream, $uid) {}

/**
 * Read the list of mailboxes
 * @link http://php.net/manual/en/function.imap-list.php
 * @param imap_stream resource
 * @param ref string
 * @param pattern string
 * @return array an array containing the names of the mailboxes.
 */
function imap_list ($imap_stream, $ref, $pattern) {}

/**
 * List all the subscribed mailboxes
 * @link http://php.net/manual/en/function.imap-lsub.php
 * @param imap_stream resource
 * @param ref string
 * @param pattern string
 * @return array an array of all the subscribed mailboxes.
 */
function imap_lsub ($imap_stream, $ref, $pattern) {}

/**
 * Read an overview of the information in the headers of the given message
 * @link http://php.net/manual/en/function.imap-fetch-overview.php
 * @param imap_stream resource
 * @param sequence string
 * @param options int[optional]
 * @return array an array of objects describing one message header each.
 */
function imap_fetch_overview ($imap_stream, $sequence, $options = null) {}

/**
 * Returns all IMAP alert messages that have occurred
 * @link http://php.net/manual/en/function.imap-alerts.php
 * @return array an array of all of the IMAP alert messages generated or false if
 */
function imap_alerts () {}

/**
 * Returns all of the IMAP errors that have occured
 * @link http://php.net/manual/en/function.imap-errors.php
 * @return array 
 */
function imap_errors () {}

/**
 * Gets the last IMAP error that occurred during this page request
 * @link http://php.net/manual/en/function.imap-last-error.php
 * @return string the full text of the last IMAP error message that occurred on the
 */
function imap_last_error () {}

/**
 * This function returns an array of messages matching the given search criteria
 * @link http://php.net/manual/en/function.imap-search.php
 * @param imap_stream resource
 * @param criteria string
 * @param options int[optional]
 * @param charset string[optional]
 * @return array an array of message numbers or UIDs.
 */
function imap_search ($imap_stream, $criteria, $options = null, $charset = null) {}

/**
 * Decodes a modified UTF-7 encoded string
 * @link http://php.net/manual/en/function.imap-utf7-decode.php
 * @param text string
 * @return string a string that is encoded in ISO-8859-1 and consists of the same
 */
function imap_utf7_decode ($text) {}

/**
 * Converts ISO-8859-1 string to modified UTF-7 text
 * @link http://php.net/manual/en/function.imap-utf7-encode.php
 * @param data string
 * @return string data encoded with the modified UTF-7
 */
function imap_utf7_encode ($data) {}

/**
 * Decode MIME header elements
 * @link http://php.net/manual/en/function.imap-mime-header-decode.php
 * @param text string
 * @return array 
 */
function imap_mime_header_decode ($text) {}

/**
 * Returns a tree of threaded message
 * @link http://php.net/manual/en/function.imap-thread.php
 * @param imap_stream resource
 * @param options int[optional]
 * @return array 
 */
function imap_thread ($imap_stream, $options = null) {}

/**
 * Set or fetch imap timeout
 * @link http://php.net/manual/en/function.imap-timeout.php
 * @param timeout_type int
 * @param timeout int[optional]
 * @return mixed 
 */
function imap_timeout ($timeout_type, $timeout = null) {}

/**
 * Retrieve the quota level settings, and usage statics per mailbox
 * @link http://php.net/manual/en/function.imap-get-quota.php
 * @param imap_stream resource
 * @param quota_root string
 * @return array an array with integer values limit and usage for the given
 */
function imap_get_quota ($imap_stream, $quota_root) {}

/**
 * Retrieve the quota settings per user
 * @link http://php.net/manual/en/function.imap-get-quotaroot.php
 * @param imap_stream resource
 * @param quota_root string
 * @return array an array of integer values pertaining to the specified user
 */
function imap_get_quotaroot ($imap_stream, $quota_root) {}

/**
 * Sets a quota for a given mailbox
 * @link http://php.net/manual/en/function.imap-set-quota.php
 * @param imap_stream resource
 * @param quota_root string
 * @param quota_limit int
 * @return bool 
 */
function imap_set_quota ($imap_stream, $quota_root, $quota_limit) {}

/**
 * Sets the ACL for a giving mailbox
 * @link http://php.net/manual/en/function.imap-setacl.php
 * @param imap_stream resource
 * @param mailbox string
 * @param id string
 * @param rights string
 * @return bool 
 */
function imap_setacl ($imap_stream, $mailbox, $id, $rights) {}

/**
 * Gets the ACL for a given mailbox
 * @link http://php.net/manual/en/function.imap-getacl.php
 * @param imap_stream resource
 * @param mailbox string
 * @return array 
 */
function imap_getacl ($imap_stream, $mailbox) {}

/**
 * Send an email message
 * @link http://php.net/manual/en/function.imap-mail.php
 * @param to string
 * @param subject string
 * @param message string
 * @param additional_headers string[optional]
 * @param cc string[optional]
 * @param bcc string[optional]
 * @param rpath string[optional]
 * @return bool 
 */
function imap_mail ($to, $subject, $message, $additional_headers = null, $cc = null, $bcc = null, $rpath = null) {}

/**
 * &Alias; <function>imap_headerinfo</function>
 * @link http://php.net/manual/en/function.imap-header.php
 */
function imap_header () {}

/**
 * &Alias; <function>imap_list</function>
 * @link http://php.net/manual/en/function.imap-listmailbox.php
 */
function imap_listmailbox () {}

/**
 * Read the list of mailboxes, returning detailed information on each one
 * @link http://php.net/manual/en/function.imap-getmailboxes.php
 * @param imap_stream resource
 * @param ref string
 * @param pattern string
 * @return array an array of objects containing mailbox information. Each
 */
function imap_getmailboxes ($imap_stream, $ref, $pattern) {}

/**
 * &Alias; <function>imap_listscan</function>
 * @link http://php.net/manual/en/function.imap-scanmailbox.php
 */
function imap_scanmailbox () {}

/**
 * &Alias; <function>imap_lsub</function>
 * @link http://php.net/manual/en/function.imap-listsubscribed.php
 */
function imap_listsubscribed () {}

/**
 * List all the subscribed mailboxes
 * @link http://php.net/manual/en/function.imap-getsubscribed.php
 * @param imap_stream resource
 * @param ref string
 * @param pattern string
 * @return array an array of objects containing mailbox information. Each
 */
function imap_getsubscribed ($imap_stream, $ref, $pattern) {}

function imap_fetchtext () {}

function imap_scan () {}

function imap_create () {}

function imap_rename () {}

define ('NIL', 0);
define ('IMAP_OPENTIMEOUT', 1);
define ('IMAP_READTIMEOUT', 2);
define ('IMAP_WRITETIMEOUT', 3);
define ('IMAP_CLOSETIMEOUT', 4);
define ('OP_DEBUG', 1);

/**
 * Open mailbox read-only
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('OP_READONLY', 2);

/**
 * Don't use or update a .newsrc for news 
 * (NNTP only)
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('OP_ANONYMOUS', 4);
define ('OP_SHORTCACHE', 8);
define ('OP_SILENT', 16);
define ('OP_PROTOTYPE', 32);

/**
 * For IMAP and NNTP names, open a connection but don't open a mailbox.
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('OP_HALFOPEN', 64);
define ('OP_EXPUNGE', 128);
define ('OP_SECURE', 256);

/**
 * silently expunge the mailbox before closing when
 * calling imap_close
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('CL_EXPUNGE', 32768);

/**
 * The parameter is a UID
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('FT_UID', 1);

/**
 * Do not set the \Seen flag if not already set
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('FT_PEEK', 2);
define ('FT_NOT', 4);

/**
 * The return string is in internal format, will not canonicalize to CRLF.
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('FT_INTERNAL', 8);
define ('FT_PREFETCHTEXT', 32);

/**
 * The sequence argument contains UIDs instead of sequence numbers
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('ST_UID', 1);
define ('ST_SILENT', 2);
define ('ST_SET', 4);

/**
 * the sequence numbers contain UIDS
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('CP_UID', 1);

/**
 * Delete the messages from the current mailbox after copying
 * with imap_mail_copy
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('CP_MOVE', 2);

/**
 * Return UIDs instead of sequence numbers
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SE_UID', 1);
define ('SE_FREE', 2);

/**
 * Don't prefetch searched messages
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SE_NOPREFETCH', 4);
define ('SO_FREE', 8);
define ('SO_NOSERVER', 16);
define ('SA_MESSAGES', 1);
define ('SA_RECENT', 2);
define ('SA_UNSEEN', 4);
define ('SA_UIDNEXT', 8);
define ('SA_UIDVALIDITY', 16);
define ('SA_ALL', 31);

/**
 * This mailbox has no "children" (there are no
 * mailboxes below this one).
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('LATT_NOINFERIORS', 1);

/**
 * This is only a container, not a mailbox - you
 * cannot open it.
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('LATT_NOSELECT', 2);

/**
 * This mailbox is marked. Only used by UW-IMAPD.
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('LATT_MARKED', 4);

/**
 * This mailbox is not marked. Only used by
 * UW-IMAPD.
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('LATT_UNMARKED', 8);
define ('LATT_REFERRAL', 16);
define ('LATT_HASCHILDREN', 32);
define ('LATT_HASNOCHILDREN', 64);

/**
 * Sort criteria for imap_sort:
 * message Date
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTDATE', 0);

/**
 * Sort criteria for imap_sort:
 * arrival date
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTARRIVAL', 1);

/**
 * Sort criteria for imap_sort:
 * mailbox in first From address
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTFROM', 2);

/**
 * Sort criteria for imap_sort:
 * message subject
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTSUBJECT', 3);

/**
 * Sort criteria for imap_sort:
 * mailbox in first To address
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTTO', 4);

/**
 * Sort criteria for imap_sort:
 * mailbox in first cc address
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTCC', 5);

/**
 * Sort criteria for imap_sort:
 * size of message in octets
 * @link http://php.net/manual/en/imap.constants.php
 */
define ('SORTSIZE', 6);
define ('TYPETEXT', 0);
define ('TYPEMULTIPART', 1);
define ('TYPEMESSAGE', 2);
define ('TYPEAPPLICATION', 3);
define ('TYPEAUDIO', 4);
define ('TYPEIMAGE', 5);
define ('TYPEVIDEO', 6);
define ('TYPEMODEL', 7);
define ('TYPEOTHER', 8);
define ('ENC7BIT', 0);
define ('ENC8BIT', 1);
define ('ENCBINARY', 2);
define ('ENCBASE64', 3);
define ('ENCQUOTEDPRINTABLE', 4);
define ('ENCOTHER', 5);

// End of imap v.

// Start of mcrypt v.

/**
 * Deprecated: Encrypt/decrypt data in ECB mode
 * @link http://php.net/manual/en/function.mcrypt-ecb.php
 */
function mcrypt_ecb () {}

/**
 * Encrypt/decrypt data in CBC mode
 * @link http://php.net/manual/en/function.mcrypt-cbc.php
 */
function mcrypt_cbc () {}

/**
 * Encrypt/decrypt data in CFB mode
 * @link http://php.net/manual/en/function.mcrypt-cfb.php
 */
function mcrypt_cfb () {}

/**
 * Encrypt/decrypt data in OFB mode
 * @link http://php.net/manual/en/function.mcrypt-ofb.php
 */
function mcrypt_ofb () {}

/**
 * Get the key size of the specified cipher
 * @link http://php.net/manual/en/function.mcrypt-get-key-size.php
 */
function mcrypt_get_key_size () {}

/**
 * Get the block size of the specified cipher
 * @link http://php.net/manual/en/function.mcrypt-get-block-size.php
 */
function mcrypt_get_block_size () {}

/**
 * Get the name of the specified cipher
 * @link http://php.net/manual/en/function.mcrypt-get-cipher-name.php
 */
function mcrypt_get_cipher_name () {}

/**
 * Create an initialization vector (IV) from a random source
 * @link http://php.net/manual/en/function.mcrypt-create-iv.php
 */
function mcrypt_create_iv () {}

/**
 * Get an array of all supported ciphers
 * @link http://php.net/manual/en/function.mcrypt-list-algorithms.php
 */
function mcrypt_list_algorithms () {}

/**
 * Get an array of all supported modes
 * @link http://php.net/manual/en/function.mcrypt-list-modes.php
 */
function mcrypt_list_modes () {}

/**
 * Returns the size of the IV belonging to a specific cipher/mode combination
 * @link http://php.net/manual/en/function.mcrypt-get-iv-size.php
 */
function mcrypt_get_iv_size () {}

/**
 * Encrypts plaintext with given parameters
 * @link http://php.net/manual/en/function.mcrypt-encrypt.php
 */
function mcrypt_encrypt () {}

/**
 * Decrypts crypttext with given parameters
 * @link http://php.net/manual/en/function.mcrypt-decrypt.php
 */
function mcrypt_decrypt () {}

/**
 * Opens the module of the algorithm and the mode to be used
 * @link http://php.net/manual/en/function.mcrypt-module-open.php
 */
function mcrypt_module_open () {}

/**
 * This function initializes all buffers needed for encryption
 * @link http://php.net/manual/en/function.mcrypt-generic-init.php
 */
function mcrypt_generic_init () {}

/**
 * This function encrypts data
 * @link http://php.net/manual/en/function.mcrypt-generic.php
 */
function mcrypt_generic () {}

/**
 * Decrypt data
 * @link http://php.net/manual/en/function.mdecrypt-generic.php
 */
function mdecrypt_generic () {}

/**
 * This function terminates encryption
 * @link http://php.net/manual/en/function.mcrypt-generic-end.php
 */
function mcrypt_generic_end () {}

/**
 * This function deinitializes an encryption module
 * @link http://php.net/manual/en/function.mcrypt-generic-deinit.php
 */
function mcrypt_generic_deinit () {}

/**
 * This function runs a self test on the opened module
 * @link http://php.net/manual/en/function.mcrypt-enc-self-test.php
 */
function mcrypt_enc_self_test () {}

/**
 * Checks whether the encryption of the opened mode works on blocks
 * @link http://php.net/manual/en/function.mcrypt-enc-is-block-algorithm-mode.php
 */
function mcrypt_enc_is_block_algorithm_mode () {}

/**
 * Checks whether the algorithm of the opened mode is a block algorithm
 * @link http://php.net/manual/en/function.mcrypt-enc-is-block-algorithm.php
 */
function mcrypt_enc_is_block_algorithm () {}

/**
 * Checks whether the opened mode outputs blocks
 * @link http://php.net/manual/en/function.mcrypt-enc-is-block-mode.php
 */
function mcrypt_enc_is_block_mode () {}

/**
 * Returns the blocksize of the opened algorithm
 * @link http://php.net/manual/en/function.mcrypt-enc-get-block-size.php
 */
function mcrypt_enc_get_block_size () {}

/**
 * Returns the maximum supported keysize of the opened mode
 * @link http://php.net/manual/en/function.mcrypt-enc-get-key-size.php
 */
function mcrypt_enc_get_key_size () {}

/**
 * Returns an array with the supported keysizes of the opened algorithm
 * @link http://php.net/manual/en/function.mcrypt-enc-get-supported-key-sizes.php
 */
function mcrypt_enc_get_supported_key_sizes () {}

/**
 * Returns the size of the IV of the opened algorithm
 * @link http://php.net/manual/en/function.mcrypt-enc-get-iv-size.php
 */
function mcrypt_enc_get_iv_size () {}

/**
 * Returns the name of the opened algorithm
 * @link http://php.net/manual/en/function.mcrypt-enc-get-algorithms-name.php
 */
function mcrypt_enc_get_algorithms_name () {}

/**
 * Returns the name of the opened mode
 * @link http://php.net/manual/en/function.mcrypt-enc-get-modes-name.php
 */
function mcrypt_enc_get_modes_name () {}

/**
 * This function runs a self test on the specified module
 * @link http://php.net/manual/en/function.mcrypt-module-self-test.php
 */
function mcrypt_module_self_test () {}

/**
 * Returns if the specified module is a block algorithm or not
 * @link http://php.net/manual/en/function.mcrypt-module-is-block-algorithm-mode.php
 */
function mcrypt_module_is_block_algorithm_mode () {}

/**
 * This function checks whether the specified algorithm is a block algorithm
 * @link http://php.net/manual/en/function.mcrypt-module-is-block-algorithm.php
 */
function mcrypt_module_is_block_algorithm () {}

/**
 * Returns if the specified mode outputs blocks or not
 * @link http://php.net/manual/en/function.mcrypt-module-is-block-mode.php
 */
function mcrypt_module_is_block_mode () {}

/**
 * Returns the blocksize of the specified algorithm
 * @link http://php.net/manual/en/function.mcrypt-module-get-algo-block-size.php
 */
function mcrypt_module_get_algo_block_size () {}

/**
 * Returns the maximum supported keysize of the opened mode
 * @link http://php.net/manual/en/function.mcrypt-module-get-algo-key-size.php
 */
function mcrypt_module_get_algo_key_size () {}

/**
 * Returns an array with the supported keysizes of the opened algorithm
 * @link http://php.net/manual/en/function.mcrypt-module-get-supported-key-sizes.php
 */
function mcrypt_module_get_supported_key_sizes () {}

/**
 * Close the mcrypt module
 * @link http://php.net/manual/en/function.mcrypt-module-close.php
 */
function mcrypt_module_close () {}

define ('MCRYPT_ENCRYPT', 0);
define ('MCRYPT_DECRYPT', 1);
define ('MCRYPT_DEV_RANDOM', 0);
define ('MCRYPT_DEV_URANDOM', 1);
define ('MCRYPT_RAND', 2);
define ('MCRYPT_3DES', "tripledes");
define ('MCRYPT_ARCFOUR_IV', "arcfour-iv");
define ('MCRYPT_ARCFOUR', "arcfour");
define ('MCRYPT_BLOWFISH', "blowfish");
define ('MCRYPT_BLOWFISH_COMPAT', "blowfish-compat");
define ('MCRYPT_CAST_128', "cast-128");
define ('MCRYPT_CAST_256', "cast-256");
define ('MCRYPT_CRYPT', "crypt");
define ('MCRYPT_DES', "des");
define ('MCRYPT_ENIGNA', "crypt");
define ('MCRYPT_GOST', "gost");
define ('MCRYPT_LOKI97', "loki97");
define ('MCRYPT_PANAMA', "panama");
define ('MCRYPT_RC2', "rc2");
define ('MCRYPT_RIJNDAEL_128', "rijndael-128");
define ('MCRYPT_RIJNDAEL_192', "rijndael-192");
define ('MCRYPT_RIJNDAEL_256', "rijndael-256");
define ('MCRYPT_SAFER64', "safer-sk64");
define ('MCRYPT_SAFER128', "safer-sk128");
define ('MCRYPT_SAFERPLUS', "saferplus");
define ('MCRYPT_SERPENT', "serpent");
define ('MCRYPT_THREEWAY', "threeway");
define ('MCRYPT_TRIPLEDES', "tripledes");
define ('MCRYPT_TWOFISH', "twofish");
define ('MCRYPT_WAKE', "wake");
define ('MCRYPT_XTEA', "xtea");
define ('MCRYPT_IDEA', "idea");
define ('MCRYPT_MARS', "mars");
define ('MCRYPT_RC6', "rc6");
define ('MCRYPT_SKIPJACK', "skipjack");
define ('MCRYPT_MODE_CBC', "cbc");
define ('MCRYPT_MODE_CFB', "cfb");
define ('MCRYPT_MODE_ECB', "ecb");
define ('MCRYPT_MODE_NOFB', "nofb");
define ('MCRYPT_MODE_OFB', "ofb");
define ('MCRYPT_MODE_STREAM', "stream");

// End of mcrypt v.

// Start of mysqli v.0.1

final class mysqli_sql_exception extends RuntimeException  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;
	protected $sqlstate;


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

final class mysqli_driver  {

	public function embedded_server_start () {}

	public function embedded_server_end () {}

}

/**
 * Represents a connection between PHP and a MySQL database.
 * @link http://php.net/manual/en/ref.mysqli.php
 */
class mysqli  {

	public function autocommit () {}

	public function change_user () {}

	public function character_set_name () {}

	public function client_encoding () {}

	public function close () {}

	public function commit () {}

	public function connect () {}

	public function debug () {}

	public function disable_reads_from_master () {}

	public function disable_rpl_parse () {}

	public function dump_debug_info () {}

	public function enable_reads_from_master () {}

	public function enable_rpl_parse () {}

	public function get_charset () {}

	public function get_client_info () {}

	public function get_server_info () {}

	public function get_warnings () {}

	public function init () {}

	public function kill () {}

	public function set_local_infile_default () {}

	public function set_local_infile_handler () {}

	public function master_query () {}

	public function multi_query () {}

	public function mysqli () {}

	public function more_results () {}

	public function next_result () {}

	public function options () {}

	public function ping () {}

	public function prepare () {}

	public function query () {}

	public function real_connect () {}

	public function real_escape_string () {}

	public function escape_string () {}

	public function real_query () {}

	public function rollback () {}

	public function rpl_parse_enabled () {}

	public function rpl_probe () {}

	public function rpl_query_type () {}

	public function select_db () {}

	public function set_charset () {}

	public function set_opt () {}

	public function slave_query () {}

	public function ssl_set () {}

	public function stat () {}

	public function stmt_init () {}

	public function store_result () {}

	public function thread_safe () {}

	public function use_result () {}

}

final class mysqli_warning  {

	protected function __construct () {}

	public function next () {}

}

/**
 * Represents the result set obtained from a query against the database.
 * @link http://php.net/manual/en/ref.mysqli.php
 */
class mysqli_result  {

	public function mysqli_result () {}

	public function close () {}

	public function free () {}

	public function data_seek () {}

	public function fetch_field () {}

	public function fetch_fields () {}

	public function fetch_field_direct () {}

	public function fetch_array () {}

	public function fetch_assoc () {}

	public function fetch_object () {}

	public function fetch_row () {}

	public function field_count () {}

	public function field_seek () {}

	public function free_result () {}

}

/**
 * Represents a prepared statement.
 * @link http://php.net/manual/en/ref.mysqli.php
 */
class mysqli_stmt  {

	public function mysqli_stmt () {}

	public function attr_get () {}

	public function attr_set () {}

	/**
	 * @param var1
	 */
	public function bind_param ($var1) {}

	public function bind_result () {}

	public function close () {}

	public function data_seek () {}

	public function execute () {}

	public function fetch () {}

	public function get_warnings () {}

	public function result_metadata () {}

	public function num_rows () {}

	public function send_long_data () {}

	public function stmt () {}

	public function free_result () {}

	public function reset () {}

	public function prepare () {}

	public function store_result () {}

}

/**
 * Gets the number of affected rows in a previous MySQL operation
 * @link http://php.net/manual/en/function.mysqli-affected-rows.php
 * @param link mysqli
 * @return int 
 */
function mysqli_affected_rows (mysqli $link) {}

/**
 * Turns on or off auto-commiting database modifications
 * @link http://php.net/manual/en/function.mysqli-autocommit.php
 * @param mode bool
 * @return bool 
 */
function mysqli_autocommit ($mode) {}

/**
 * Changes the user of the specified database connection
 * @link http://php.net/manual/en/function.mysqli-change-user.php
 * @param user string
 * @param password string
 * @param database string
 * @return bool 
 */
function mysqli_change_user ($user, $password, $database) {}

/**
 * Returns the default character set for the database connection
 * @link http://php.net/manual/en/function.mysqli-character-set-name.php
 * @return string 
 */
function mysqli_character_set_name () {}

/**
 * Closes a previously opened database connection
 * @link http://php.net/manual/en/function.mysqli-close.php
 * @return bool 
 */
function mysqli_close () {}

/**
 * Commits the current transaction
 * @link http://php.net/manual/en/function.mysqli-commit.php
 * @return bool 
 */
function mysqli_commit () {}

/**
 * Open a new connection to the MySQL server
 * @link http://php.net/manual/en/function.mysqli-connect.php
 * @param host string[optional]
 * @param username string[optional]
 * @param passwd string[optional]
 * @param dbname string[optional]
 * @param port int[optional]
 * @param socket string[optional]
 * @return mysqli a object which represents the connection to a MySQL Server or
 */
function mysqli_connect ($host = null, $username = null, $passwd = null, $dbname = null, $port = null, $socket = null) {}

/**
 * Returns the error code from last connect call
 * @link http://php.net/manual/en/function.mysqli-connect-errno.php
 * @return int 
 */
function mysqli_connect_errno () {}

/**
 * Returns a string description of the last connect error
 * @link http://php.net/manual/en/function.mysqli-connect-error.php
 * @return string 
 */
function mysqli_connect_error () {}

/**
 * Adjusts the result pointer to an arbitary row in the result
 * @link http://php.net/manual/en/function.mysqli-data-seek.php
 * @param offset int
 * @return bool 
 */
function mysqli_data_seek ($offset) {}

/**
 * Performs debugging operations
 * @link http://php.net/manual/en/function.mysqli-debug.php
 * @param message string
 * @return bool true.
 */
function mysqli_debug ($message) {}

/**
 * Disable reads from master
 * @link http://php.net/manual/en/function.mysqli-disable-reads-from-master.php
 * @return void 
 */
function mysqli_disable_reads_from_master () {}

/**
 * Disable RPL parse
 * @link http://php.net/manual/en/function.mysqli-disable-rpl-parse.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_disable_rpl_parse (mysqli $link) {}

/**
 * Dump debugging information into the log
 * @link http://php.net/manual/en/function.mysqli-dump-debug-info.php
 * @return bool 
 */
function mysqli_dump_debug_info () {}

/**
 * Enable reads from master
 * @link http://php.net/manual/en/function.mysqli-enable-reads-from-master.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_enable_reads_from_master (mysqli $link) {}

/**
 * Enable RPL parse
 * @link http://php.net/manual/en/function.mysqli-enable-rpl-parse.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_enable_rpl_parse (mysqli $link) {}

/**
 * @link http://php.net/manual/en/function.mysqli-embedded-server-end.php
 * @return void 
 */
function mysqli_embedded_server_end () {}

/**
 * @link http://php.net/manual/en/function.mysqli-embedded-server-start.php
 * @param start bool
 * @param arguments array
 * @param groups array
 * @return bool 
 */
function mysqli_embedded_server_start ($start, array $arguments, array $groups) {}

/**
 * Returns the error code for the most recent function call
 * @link http://php.net/manual/en/function.mysqli-errno.php
 * @param link mysqli
 * @return int 
 */
function mysqli_errno (mysqli $link) {}

/**
 * Returns a string description of the last error
 * @link http://php.net/manual/en/function.mysqli-error.php
 * @param link mysqli
 * @return string 
 */
function mysqli_error (mysqli $link) {}

/**
 * Executes a prepared Query
 * @link http://php.net/manual/en/function.mysqli-stmt-execute.php
 * @return bool 
 */
function mysqli_stmt_execute () {}

/**
 * Alias for <function>mysqli_stmt_execute</function>
 * @link http://php.net/manual/en/function.mysqli-execute.php
 */
function mysqli_execute () {}

/**
 * Returns the next field in the result set
 * @link http://php.net/manual/en/function.mysqli-fetch-field.php
 * @return object an object which contains field definition information or false
 */
function mysqli_fetch_field () {}

/**
 * Returns an array of objects representing the fields in a result set
 * @link http://php.net/manual/en/function.mysqli-fetch-fields.php
 * @return array an array of objects which contains field definition information or
 */
function mysqli_fetch_fields () {}

/**
 * Fetch meta-data for a single field
 * @link http://php.net/manual/en/function.mysqli-fetch-field-direct.php
 * @param fieldnr int
 * @return object an object which contains field definition information or false
 */
function mysqli_fetch_field_direct ($fieldnr) {}

/**
 * Returns the lengths of the columns of the current row in the result set
 * @link http://php.net/manual/en/function.mysqli-fetch-lengths.php
 * @param result mysqli_result
 * @return array 
 */
function mysqli_fetch_lengths (mysqli_result $result) {}

/**
 * Fetch a result row as an associative, a numeric array, or both
 * @link http://php.net/manual/en/function.mysqli-fetch-array.php
 * @param resulttype int[optional]
 * @return mixed an array of strings that corresponds to the fetched row or &null; if there
 */
function mysqli_fetch_array ($resulttype = null) {}

/**
 * Fetch a result row as an associative array
 * @link http://php.net/manual/en/function.mysqli-fetch-assoc.php
 * @return array an associative array of strings representing the fetched row in the result
 */
function mysqli_fetch_assoc () {}

/**
 * Returns the current row of a result set as an object
 * @link http://php.net/manual/en/function.mysqli-fetch-object.php
 * @param class_name string[optional]
 * @param params array[optional]
 * @return object an object with string properties that corresponds to the fetched
 */
function mysqli_fetch_object ($class_name = null, array $params = null) {}

/**
 * Get a result row as an enumerated array
 * @link http://php.net/manual/en/function.mysqli-fetch-row.php
 * @return mixed 
 */
function mysqli_fetch_row () {}

/**
 * Returns the number of columns for the most recent query
 * @link http://php.net/manual/en/function.mysqli-field-count.php
 * @return int 
 */
function mysqli_field_count () {}

/**
 * Set result pointer to a specified field offset
 * @link http://php.net/manual/en/function.mysqli-field-seek.php
 * @param fieldnr int
 * @return bool the previous value of field cursor.
 */
function mysqli_field_seek ($fieldnr) {}

/**
 * Get current field offset of a result pointer
 * @link http://php.net/manual/en/function.mysqli-field-tell.php
 * @param result mysqli_result
 * @return int current offset of field cursor.
 */
function mysqli_field_tell (mysqli_result $result) {}

/**
 * Frees the memory associated with a result
 * @link http://php.net/manual/en/function.mysqli-free-result.php
 * @return void 
 */
function mysqli_free_result () {}

/**
 * Returns a character set object
 * @link http://php.net/manual/en/function.mysqli-get-charset.php
 * @param link mysqli
 * @return object 
 */
function mysqli_get_charset (mysqli $link) {}

/**
 * Returns the MySQL client version as a string
 * @link http://php.net/manual/en/function.mysqli-get-client-info.php
 * @return string 
 */
function mysqli_get_client_info () {}

/**
 * Get MySQL client info
 * @link http://php.net/manual/en/function.mysqli-get-client-version.php
 * @return int 
 */
function mysqli_get_client_version () {}

/**
 * Returns a string representing the type of connection used
 * @link http://php.net/manual/en/function.mysqli-get-host-info.php
 * @param link mysqli
 * @return string 
 */
function mysqli_get_host_info (mysqli $link) {}

/**
 * Returns the version of the MySQL protocol used
 * @link http://php.net/manual/en/function.mysqli-get-proto-info.php
 * @param link mysqli
 * @return int an integer representing the protocol version.
 */
function mysqli_get_proto_info (mysqli $link) {}

/**
 * Returns the version of the MySQL server
 * @link http://php.net/manual/en/function.mysqli-get-server-info.php
 * @param link mysqli
 * @return string 
 */
function mysqli_get_server_info (mysqli $link) {}

/**
 * Returns the version of the MySQL server as an integer
 * @link http://php.net/manual/en/function.mysqli-get-server-version.php
 * @param link mysqli
 * @return int 
 */
function mysqli_get_server_version (mysqli $link) {}

/**
 * @link http://php.net/manual/en/function.mysqli-get-warnings.php
 * @param link mysqli
 * @return object 
 */
function mysqli_get_warnings (mysqli $link) {}

/**
 * Initializes MySQLi and returns a resource for use with mysqli_real_connect()
 * @link http://php.net/manual/en/function.mysqli-init.php
 * @return mysqli an object.
 */
function mysqli_init () {}

/**
 * Retrieves information about the most recently executed query
 * @link http://php.net/manual/en/function.mysqli-info.php
 * @param link mysqli
 * @return string 
 */
function mysqli_info (mysqli $link) {}

/**
 * Returns the auto generated id used in the last query
 * @link http://php.net/manual/en/function.mysqli-insert-id.php
 * @param link mysqli
 * @return int 
 */
function mysqli_insert_id (mysqli $link) {}

/**
 * Asks the server to kill a MySQL thread
 * @link http://php.net/manual/en/function.mysqli-kill.php
 * @param processid int
 * @return bool 
 */
function mysqli_kill ($processid) {}

/**
 * Unsets user defined handler for load local infile command
 * @link http://php.net/manual/en/function.mysqli-set-local-infile-default.php
 * @param link mysqli
 * @return void 
 */
function mysqli_set_local_infile_default (mysqli $link) {}

/**
 * Set callback functions for LOAD DATA LOCAL INFILE command
 * @link http://php.net/manual/en/function.mysqli-set-local-infile-handler.php
 * @param link mysqli
 * @param read_func callback
 * @return bool 
 */
function mysqli_set_local_infile_handler (mysqli $link, $read_func) {}

/**
 * Enforce execution of a query on the master in a master/slave setup
 * @link http://php.net/manual/en/function.mysqli-master-query.php
 * @param link mysqli
 * @param query string
 * @return bool 
 */
function mysqli_master_query (mysqli $link, $query) {}

/**
 * Check if there are any more query results from a multi query
 * @link http://php.net/manual/en/function.mysqli-more-results.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_more_results (mysqli $link) {}

/**
 * Performs a query on the database
 * @link http://php.net/manual/en/function.mysqli-multi-query.php
 * @param query string
 * @return bool false if the first statement failed.
 */
function mysqli_multi_query ($query) {}

/**
 * Prepare next result from multi_query
 * @link http://php.net/manual/en/function.mysqli-next-result.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_next_result (mysqli $link) {}

/**
 * Get the number of fields in a result
 * @link http://php.net/manual/en/function.mysqli-num-fields.php
 * @param result mysqli_result
 * @return int 
 */
function mysqli_num_fields (mysqli_result $result) {}

/**
 * Gets the number of rows in a result
 * @link http://php.net/manual/en/function.mysqli-num-rows.php
 * @param result mysqli_result
 * @return int number of rows in the result set.
 */
function mysqli_num_rows (mysqli_result $result) {}

/**
 * Set options
 * @link http://php.net/manual/en/function.mysqli-options.php
 * @param option int
 * @param value mixed
 * @return bool 
 */
function mysqli_options ($option, $value) {}

/**
 * Pings a server connection, or tries to reconnect if the connection has gone down
 * @link http://php.net/manual/en/function.mysqli-ping.php
 * @return bool 
 */
function mysqli_ping () {}

/**
 * Prepare a SQL statement for execution
 * @link http://php.net/manual/en/function.mysqli-prepare.php
 * @param query string
 * @return mysqli_stmt 
 */
function mysqli_prepare ($query) {}

/**
 * Enables or disables internal report functions
 * @link http://php.net/manual/en/function.mysqli-report.php
 * @param flags int
 * @return bool 
 */
function mysqli_report ($flags) {}

/**
 * Performs a query on the database
 * @link http://php.net/manual/en/function.mysqli-query.php
 * @param query string
 * @param resultmode int[optional]
 * @return mixed 
 */
function mysqli_query ($query, $resultmode = null) {}

/**
 * Opens a connection to a mysql server
 * @link http://php.net/manual/en/function.mysqli-real-connect.php
 * @param host string[optional]
 * @param username string[optional]
 * @param passwd string[optional]
 * @param dbname string[optional]
 * @param port int[optional]
 * @param socket string[optional]
 * @param flags int[optional]
 * @return bool 
 */
function mysqli_real_connect ($host = null, $username = null, $passwd = null, $dbname = null, $port = null, $socket = null, $flags = null) {}

/**
 * Escapes special characters in a string for use in a SQL statement, taking into account the current charset of the connection
 * @link http://php.net/manual/en/function.mysqli-real-escape-string.php
 * @param escapestr string
 * @return string an escaped string.
 */
function mysqli_real_escape_string ($escapestr) {}

/**
 * Execute an SQL query
 * @link http://php.net/manual/en/function.mysqli-real-query.php
 * @param query string
 * @return bool 
 */
function mysqli_real_query ($query) {}

/**
 * Rolls back current transaction
 * @link http://php.net/manual/en/function.mysqli-rollback.php
 * @return bool 
 */
function mysqli_rollback () {}

/**
 * Check if RPL parse is enabled
 * @link http://php.net/manual/en/function.mysqli-rpl-parse-enabled.php
 * @param link mysqli
 * @return int 
 */
function mysqli_rpl_parse_enabled (mysqli $link) {}

/**
 * RPL probe
 * @link http://php.net/manual/en/function.mysqli-rpl-probe.php
 * @param link mysqli
 * @return bool 
 */
function mysqli_rpl_probe (mysqli $link) {}

/**
 * Returns RPL query type
 * @link http://php.net/manual/en/function.mysqli-rpl-query-type.php
 * @param query string
 * @return int 
 */
function mysqli_rpl_query_type ($query) {}

/**
 * Selects the default database for database queries
 * @link http://php.net/manual/en/function.mysqli-select-db.php
 * @param dbname string
 * @return bool 
 */
function mysqli_select_db ($dbname) {}

/**
 * Sets the default client character set
 * @link http://php.net/manual/en/function.mysqli-set-charset.php
 * @param charset string
 * @return bool 
 */
function mysqli_set_charset ($charset) {}

/**
 * @link http://php.net/manual/en/function.mysqli-stmt-attr-get.php
 * @param stmt mysqli_stmt
 * @param attr int
 * @return int 
 */
function mysqli_stmt_attr_get (mysqli_stmt $stmt, $attr) {}

/**
 * @link http://php.net/manual/en/function.mysqli-stmt-attr-set.php
 * @param stmt mysqli_stmt
 * @param attr int
 * @param mode int
 * @return bool 
 */
function mysqli_stmt_attr_set (mysqli_stmt $stmt, $attr, $mode) {}

/**
 * Returns the number of field in the given statement
 * @link http://php.net/manual/en/function.mysqli-stmt-field-count.php
 * @param stmt mysqli_stmt
 * @return int 
 */
function mysqli_stmt_field_count (mysqli_stmt $stmt) {}

/**
 * Initializes a statement and returns an object for use with mysqli_stmt_prepare
 * @link http://php.net/manual/en/function.mysqli-stmt-init.php
 * @return mysqli_stmt an object.
 */
function mysqli_stmt_init () {}

/**
 * Prepare a SQL statement for execution
 * @link http://php.net/manual/en/function.mysqli-stmt-prepare.php
 * @param query string
 * @return mixed 
 */
function mysqli_stmt_prepare ($query) {}

/**
 * Returns result set metadata from a prepared statement
 * @link http://php.net/manual/en/function.mysqli-stmt-result-metadata.php
 * @return mysqli_result a result object or false if an error occured.
 */
function mysqli_stmt_result_metadata () {}

/**
 * Send data in blocks
 * @link http://php.net/manual/en/function.mysqli-stmt-send-long-data.php
 * @param param_nr int
 * @param data string
 * @return bool 
 */
function mysqli_stmt_send_long_data ($param_nr, $data) {}

/**
 * Binds variables to a prepared statement as parameters
 * @link http://php.net/manual/en/function.mysqli-stmt-bind-param.php
 * @param types string
 * @param var1 mixed
 * @param ... mixed[optional]
 * @return bool 
 */
function mysqli_stmt_bind_param ($types, &$var1) {}

/**
 * Binds variables to a prepared statement for result storage
 * @link http://php.net/manual/en/function.mysqli-stmt-bind-result.php
 * @param var1 mixed
 * @param ... mixed[optional]
 * @return bool 
 */
function mysqli_stmt_bind_result (&$var1) {}

/**
 * Fetch results from a prepared statement into the bound variables
 * @link http://php.net/manual/en/function.mysqli-stmt-fetch.php
 * @return bool 
 */
function mysqli_stmt_fetch () {}

/**
 * Frees stored result memory for the given statement handle
 * @link http://php.net/manual/en/function.mysqli-stmt-free-result.php
 * @return void 
 */
function mysqli_stmt_free_result () {}

/**
 * @link http://php.net/manual/en/function.mysqli-stmt-get-warnings.php
 * @param stmt mysqli_stmt
 * @return object 
 */
function mysqli_stmt_get_warnings (mysqli_stmt $stmt) {}

/**
 * Get the ID generated from the previous INSERT operation
 * @link http://php.net/manual/en/function.mysqli-stmt-insert-id.php
 * @param stmt mysqli_stmt
 * @return mixed 
 */
function mysqli_stmt_insert_id (mysqli_stmt $stmt) {}

/**
 * Resets a prepared statement
 * @link http://php.net/manual/en/function.mysqli-stmt-reset.php
 * @return bool 
 */
function mysqli_stmt_reset () {}

/**
 * Returns the number of parameter for the given statement
 * @link http://php.net/manual/en/function.mysqli-stmt-param-count.php
 * @param stmt mysqli_stmt
 * @return int an integer representing the number of parameters.
 */
function mysqli_stmt_param_count (mysqli_stmt $stmt) {}

/**
 * Send the query and return
 * @link http://php.net/manual/en/function.mysqli-send-query.php
 * @param query string
 * @return bool 
 */
function mysqli_send_query ($query) {}

/**
 * Force execution of a query on a slave in a master/slave setup
 * @link http://php.net/manual/en/function.mysqli-slave-query.php
 * @param link mysqli
 * @param query string
 * @return bool 
 */
function mysqli_slave_query (mysqli $link, $query) {}

/**
 * Returns the SQLSTATE error from previous MySQL operation
 * @link http://php.net/manual/en/function.mysqli-sqlstate.php
 * @param link mysqli
 * @return string a string containing the SQLSTATE error code for the last error.
 */
function mysqli_sqlstate (mysqli $link) {}

/**
 * Used for establishing secure connections using SSL
 * @link http://php.net/manual/en/function.mysqli-ssl-set.php
 * @param key string
 * @param cert string
 * @param ca string
 * @param capath string
 * @param cipher string
 * @return bool 
 */
function mysqli_ssl_set ($key, $cert, $ca, $capath, $cipher) {}

/**
 * Gets the current system status
 * @link http://php.net/manual/en/function.mysqli-stat.php
 * @return string 
 */
function mysqli_stat () {}

/**
 * Returns the total number of rows changed, deleted, or
  inserted by the last executed statement
 * @link http://php.net/manual/en/function.mysqli-stmt-affected-rows.php
 * @param stmt mysqli_stmt
 * @return int 
 */
function mysqli_stmt_affected_rows (mysqli_stmt $stmt) {}

/**
 * Closes a prepared statement
 * @link http://php.net/manual/en/function.mysqli-stmt-close.php
 * @return bool 
 */
function mysqli_stmt_close () {}

/**
 * Seeks to an arbitray row in statement result set
 * @link http://php.net/manual/en/function.mysqli-stmt-data-seek.php
 * @param offset int
 * @return void 
 */
function mysqli_stmt_data_seek ($offset) {}

/**
 * Returns the error code for the most recent statement call
 * @link http://php.net/manual/en/function.mysqli-stmt-errno.php
 * @param stmt mysqli_stmt
 * @return int 
 */
function mysqli_stmt_errno (mysqli_stmt $stmt) {}

/**
 * Returns a string description for last statement error
 * @link http://php.net/manual/en/function.mysqli-stmt-error.php
 * @param stmt mysqli_stmt
 * @return string 
 */
function mysqli_stmt_error (mysqli_stmt $stmt) {}

/**
 * Return the number of rows in statements result set
 * @link http://php.net/manual/en/function.mysqli-stmt-num-rows.php
 * @param stmt mysqli_stmt
 * @return int 
 */
function mysqli_stmt_num_rows (mysqli_stmt $stmt) {}

/**
 * Returns SQLSTATE error from previous statement operation
 * @link http://php.net/manual/en/function.mysqli-stmt-sqlstate.php
 * @param stmt mysqli_stmt
 * @return string a string containing the SQLSTATE error code for the last error.
 */
function mysqli_stmt_sqlstate (mysqli_stmt $stmt) {}

/**
 * Transfers a result set from the last query
 * @link http://php.net/manual/en/function.mysqli-store-result.php
 * @return mysqli_result a buffered result object or false if an error occurred.
 */
function mysqli_store_result () {}

/**
 * Transfers a result set from a prepared statement
 * @link http://php.net/manual/en/function.mysqli-stmt-store-result.php
 * @return bool 
 */
function mysqli_stmt_store_result () {}

/**
 * Returns the thread ID for the current connection
 * @link http://php.net/manual/en/function.mysqli-thread-id.php
 * @param link mysqli
 * @return int the Thread ID for the current connection.
 */
function mysqli_thread_id (mysqli $link) {}

/**
 * Returns whether thread safety is given or not
 * @link http://php.net/manual/en/function.mysqli-thread-safe.php
 * @return bool 
 */
function mysqli_thread_safe () {}

/**
 * Initiate a result set retrieval
 * @link http://php.net/manual/en/function.mysqli-use-result.php
 * @return mysqli_result an unbuffered result object or false if an error occurred.
 */
function mysqli_use_result () {}

/**
 * Returns the number of warnings from the last query for the given link
 * @link http://php.net/manual/en/function.mysqli-warning-count.php
 * @param link mysqli
 * @return int 
 */
function mysqli_warning_count (mysqli $link) {}

/**
 * Alias for <function>mysqli_stmt_bind_param</function>
 * @link http://php.net/manual/en/function.mysqli-bind-param.php
 * @param var1
 * @param var2
 */
function mysqli_bind_param ($var1, $var2) {}

/**
 * Alias for <function>mysqli_stmt_bind_result</function>
 * @link http://php.net/manual/en/function.mysqli-bind-result.php
 * @param var1
 */
function mysqli_bind_result ($var1) {}

/**
 * Alias of <function>mysqli_character_set_name</function>
 * @link http://php.net/manual/en/function.mysqli-client-encoding.php
 */
function mysqli_client_encoding () {}

/**
 * Alias of <function>mysqli_real_escape_string</function>
 * @link http://php.net/manual/en/function.mysqli-escape-string.php
 */
function mysqli_escape_string () {}

/**
 * Alias for <function>mysqli_stmt_fetch</function>
 * @link http://php.net/manual/en/function.mysqli-fetch.php
 */
function mysqli_fetch () {}

/**
 * Alias for <function>mysqli_stmt_param_count</function>
 * @link http://php.net/manual/en/function.mysqli-param-count.php
 */
function mysqli_param_count () {}

/**
 * Alias for <function>mysqli_stmt_result_metadata</function>
 * @link http://php.net/manual/en/function.mysqli-get-metadata.php
 */
function mysqli_get_metadata () {}

/**
 * Alias for <function>mysqli_stmt_send_long_data</function>
 * @link http://php.net/manual/en/function.mysqli-send-long-data.php
 */
function mysqli_send_long_data () {}

/**
 * Alias of <function>mysqli_options</function>
 * @link http://php.net/manual/en/function.mysqli-set-opt.php
 */
function mysqli_set_opt () {}

define ('MYSQLI_READ_DEFAULT_GROUP', 5);
define ('MYSQLI_READ_DEFAULT_FILE', 4);
define ('MYSQLI_OPT_CONNECT_TIMEOUT', 0);
define ('MYSQLI_OPT_LOCAL_INFILE', 8);
define ('MYSQLI_INIT_COMMAND', 3);
define ('MYSQLI_CLIENT_SSL', 2048);
define ('MYSQLI_CLIENT_COMPRESS', 32);
define ('MYSQLI_CLIENT_INTERACTIVE', 1024);
define ('MYSQLI_CLIENT_IGNORE_SPACE', 256);
define ('MYSQLI_CLIENT_NO_SCHEMA', 16);
define ('MYSQLI_CLIENT_FOUND_ROWS', 2);
define ('MYSQLI_STORE_RESULT', 0);
define ('MYSQLI_USE_RESULT', 1);
define ('MYSQLI_ASSOC', 1);
define ('MYSQLI_NUM', 2);
define ('MYSQLI_BOTH', 3);
define ('MYSQLI_STMT_ATTR_UPDATE_MAX_LENGTH', 0);
define ('MYSQLI_STMT_ATTR_CURSOR_TYPE', 1);
define ('MYSQLI_CURSOR_TYPE_NO_CURSOR', 0);
define ('MYSQLI_CURSOR_TYPE_READ_ONLY', 1);
define ('MYSQLI_CURSOR_TYPE_FOR_UPDATE', 2);
define ('MYSQLI_CURSOR_TYPE_SCROLLABLE', 4);
define ('MYSQLI_STMT_ATTR_PREFETCH_ROWS', 2);
define ('MYSQLI_NOT_NULL_FLAG', 1);
define ('MYSQLI_PRI_KEY_FLAG', 2);
define ('MYSQLI_UNIQUE_KEY_FLAG', 4);
define ('MYSQLI_MULTIPLE_KEY_FLAG', 8);
define ('MYSQLI_BLOB_FLAG', 16);
define ('MYSQLI_UNSIGNED_FLAG', 32);
define ('MYSQLI_ZEROFILL_FLAG', 64);
define ('MYSQLI_AUTO_INCREMENT_FLAG', 512);
define ('MYSQLI_TIMESTAMP_FLAG', 1024);
define ('MYSQLI_SET_FLAG', 2048);
define ('MYSQLI_NUM_FLAG', 32768);
define ('MYSQLI_PART_KEY_FLAG', 16384);
define ('MYSQLI_GROUP_FLAG', 32768);
define ('MYSQLI_TYPE_DECIMAL', 0);
define ('MYSQLI_TYPE_TINY', 1);
define ('MYSQLI_TYPE_SHORT', 2);
define ('MYSQLI_TYPE_LONG', 3);
define ('MYSQLI_TYPE_FLOAT', 4);
define ('MYSQLI_TYPE_DOUBLE', 5);
define ('MYSQLI_TYPE_NULL', 6);
define ('MYSQLI_TYPE_TIMESTAMP', 7);
define ('MYSQLI_TYPE_LONGLONG', 8);
define ('MYSQLI_TYPE_INT24', 9);
define ('MYSQLI_TYPE_DATE', 10);
define ('MYSQLI_TYPE_TIME', 11);
define ('MYSQLI_TYPE_DATETIME', 12);
define ('MYSQLI_TYPE_YEAR', 13);
define ('MYSQLI_TYPE_NEWDATE', 14);
define ('MYSQLI_TYPE_ENUM', 247);
define ('MYSQLI_TYPE_SET', 248);
define ('MYSQLI_TYPE_TINY_BLOB', 249);
define ('MYSQLI_TYPE_MEDIUM_BLOB', 250);
define ('MYSQLI_TYPE_LONG_BLOB', 251);
define ('MYSQLI_TYPE_BLOB', 252);
define ('MYSQLI_TYPE_VAR_STRING', 253);
define ('MYSQLI_TYPE_STRING', 254);
define ('MYSQLI_TYPE_CHAR', 1);
define ('MYSQLI_TYPE_INTERVAL', 247);
define ('MYSQLI_TYPE_GEOMETRY', 255);
define ('MYSQLI_TYPE_NEWDECIMAL', 246);
define ('MYSQLI_TYPE_BIT', 16);
define ('MYSQLI_RPL_MASTER', 0);
define ('MYSQLI_RPL_SLAVE', 1);
define ('MYSQLI_RPL_ADMIN', 2);
define ('MYSQLI_NO_DATA', 100);
define ('MYSQLI_DATA_TRUNCATED', 101);
define ('MYSQLI_REPORT_INDEX', 4);
define ('MYSQLI_REPORT_ERROR', 1);
define ('MYSQLI_REPORT_STRICT', 2);
define ('MYSQLI_REPORT_ALL', 255);
define ('MYSQLI_REPORT_OFF', 0);

// End of mysqli v.0.1

// Start of pcntl v.

/**
 * Forks the currently running process
 * @link http://php.net/manual/en/function.pcntl-fork.php
 * @return int 
 */
function pcntl_fork () {}

/**
 * Waits on or returns the status of a forked child
 * @link http://php.net/manual/en/function.pcntl-waitpid.php
 * @param pid int
 * @param status int
 * @param options int[optional]
 * @return int 
 */
function pcntl_waitpid ($pid, &$status, $options = null) {}

/**
 * Waits on or returns the status of a forked child
 * @link http://php.net/manual/en/function.pcntl-wait.php
 * @param status int
 * @param options int[optional]
 * @return int 
 */
function pcntl_wait (&$status, $options = null) {}

/**
 * Installs a signal handler
 * @link http://php.net/manual/en/function.pcntl-signal.php
 * @param signo int
 * @param handler callback
 * @param restart_syscalls bool[optional]
 * @return bool 
 */
function pcntl_signal ($signo, $handler, $restart_syscalls = null) {}

/**
 * Checks if status code represents a normal exit
 * @link http://php.net/manual/en/function.pcntl-wifexited.php
 * @param status int
 * @return bool true if the child status code represents a normal exit, false
 */
function pcntl_wifexited ($status) {}

/**
 * Checks whether the child process is currently stopped
 * @link http://php.net/manual/en/function.pcntl-wifstopped.php
 * @param status int
 * @return bool true if the child process which caused the return is
 */
function pcntl_wifstopped ($status) {}

/**
 * Checks whether the status code represents a termination due to a signal
 * @link http://php.net/manual/en/function.pcntl-wifsignaled.php
 * @param status int
 * @return bool true if the child process exited because of a signal which was
 */
function pcntl_wifsignaled ($status) {}

/**
 * Returns the return code of a terminated child
 * @link http://php.net/manual/en/function.pcntl-wexitstatus.php
 * @param status int
 * @return int the return code, as an integer.
 */
function pcntl_wexitstatus ($status) {}

/**
 * Returns the signal which caused the child to terminate
 * @link http://php.net/manual/en/function.pcntl-wtermsig.php
 * @param status int
 * @return int the signal number, as an integer.
 */
function pcntl_wtermsig ($status) {}

/**
 * Returns the signal which caused the child to stop
 * @link http://php.net/manual/en/function.pcntl-wstopsig.php
 * @param status int
 * @return int the signal number.
 */
function pcntl_wstopsig ($status) {}

/**
 * Executes specified program in current process space
 * @link http://php.net/manual/en/function.pcntl-exec.php
 * @param path string
 * @param args array[optional]
 * @param envs array[optional]
 * @return void false on error and does not return on success.
 */
function pcntl_exec ($path, array $args = null, array $envs = null) {}

/**
 * Set an alarm clock for delivery of a signal
 * @link http://php.net/manual/en/function.pcntl-alarm.php
 * @param seconds int
 * @return int the time in seconds that any previously scheduled alarm had
 */
function pcntl_alarm ($seconds) {}

/**
 * Get the priority of any process
 * @link http://php.net/manual/en/function.pcntl-getpriority.php
 * @param pid int[optional]
 * @param process_identifier int[optional]
 * @return int 
 */
function pcntl_getpriority ($pid = null, $process_identifier = null) {}

/**
 * Change the priority of any process
 * @link http://php.net/manual/en/function.pcntl-setpriority.php
 * @param priority int
 * @param pid int[optional]
 * @param process_identifier int[optional]
 * @return bool 
 */
function pcntl_setpriority ($priority, $pid = null, $process_identifier = null) {}

define ('WNOHANG', 1);
define ('WUNTRACED', 2);
define ('SIG_IGN', 1);
define ('SIG_DFL', 0);
define ('SIG_ERR', -1);
define ('SIGHUP', 1);
define ('SIGINT', 2);
define ('SIGQUIT', 3);
define ('SIGILL', 4);
define ('SIGTRAP', 5);
define ('SIGABRT', 6);
define ('SIGIOT', 6);
define ('SIGBUS', 7);
define ('SIGFPE', 8);
define ('SIGKILL', 9);
define ('SIGUSR1', 10);
define ('SIGSEGV', 11);
define ('SIGUSR2', 12);
define ('SIGPIPE', 13);
define ('SIGALRM', 14);
define ('SIGTERM', 15);
define ('SIGSTKFLT', 16);
define ('SIGCLD', 17);
define ('SIGCHLD', 17);
define ('SIGCONT', 18);
define ('SIGSTOP', 19);
define ('SIGTSTP', 20);
define ('SIGTTIN', 21);
define ('SIGTTOU', 22);
define ('SIGURG', 23);
define ('SIGXCPU', 24);
define ('SIGXFSZ', 25);
define ('SIGVTALRM', 26);
define ('SIGPROF', 27);
define ('SIGWINCH', 28);
define ('SIGPOLL', 29);
define ('SIGIO', 29);
define ('SIGPWR', 30);
define ('SIGSYS', 31);
define ('SIGBABY', 31);
define ('PRIO_PGRP', 1);
define ('PRIO_USER', 2);
define ('PRIO_PROCESS', 0);

// End of pcntl v.

// Start of pgsql v.

/**
 * Open a PostgreSQL connection
 * @link http://php.net/manual/en/function.pg-connect.php
 * @param connection_string string
 * @param connect_type int[optional]
 * @return resource 
 */
function pg_connect ($connection_string, $connect_type = null) {}

/**
 * Open a persistent PostgreSQL connection
 * @link http://php.net/manual/en/function.pg-pconnect.php
 * @param connection_string string
 * @param connect_type int[optional]
 * @return resource 
 */
function pg_pconnect ($connection_string, $connect_type = null) {}

/**
 * Closes a PostgreSQL connection
 * @link http://php.net/manual/en/function.pg-close.php
 * @param connection resource[optional]
 * @return bool 
 */
function pg_close ($connection = null) {}

/**
 * Get connection status
 * @link http://php.net/manual/en/function.pg-connection-status.php
 * @param connection resource
 * @return int 
 */
function pg_connection_status ($connection) {}

/**
 * Get connection is busy or not
 * @link http://php.net/manual/en/function.pg-connection-busy.php
 * @param connection resource
 * @return bool true if the connection is busy, false otherwise.
 */
function pg_connection_busy ($connection) {}

/**
 * Reset connection (reconnect)
 * @link http://php.net/manual/en/function.pg-connection-reset.php
 * @param connection resource
 * @return bool 
 */
function pg_connection_reset ($connection) {}

/**
 * Returns the host name associated with the connection
 * @link http://php.net/manual/en/function.pg-host.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_host ($connection = null) {}

/**
 * Get the database name
 * @link http://php.net/manual/en/function.pg-dbname.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_dbname ($connection = null) {}

/**
 * Return the port number associated with the connection
 * @link http://php.net/manual/en/function.pg-port.php
 * @param connection resource[optional]
 * @return int 
 */
function pg_port ($connection = null) {}

/**
 * Return the TTY name associated with the connection
 * @link http://php.net/manual/en/function.pg-tty.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_tty ($connection = null) {}

/**
 * Get the options associated with the connection
 * @link http://php.net/manual/en/function.pg-options.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_options ($connection = null) {}

/**
 * Returns an array with client, protocol and server version (when available)
 * @link http://php.net/manual/en/function.pg-version.php
 * @param connection resource[optional]
 * @return array an array with client, protocol
 */
function pg_version ($connection = null) {}

/**
 * Ping database connection
 * @link http://php.net/manual/en/function.pg-ping.php
 * @param connection resource[optional]
 * @return bool 
 */
function pg_ping ($connection = null) {}

/**
 * Looks up a current parameter setting of the server.
 * @link http://php.net/manual/en/function.pg-parameter-status.php
 * @param connection resource
 * @param param_name string
 * @return string 
 */
function pg_parameter_status ($connection, $param_name) {}

/**
 * Returns the current in-transaction status of the server.
 * @link http://php.net/manual/en/function.pg-transaction-status.php
 * @param connection resource
 * @return int 
 */
function pg_transaction_status ($connection) {}

/**
 * Execute a query
 * @link http://php.net/manual/en/function.pg-query.php
 * @param query string
 * @return resource 
 */
function pg_query ($query) {}

/**
 * Submits a command to the server and waits for the result, with the ability to pass parameters separately from the SQL command text.
 * @link http://php.net/manual/en/function.pg-query-params.php
 * @param connection resource
 * @param query string
 * @param params array
 * @return resource 
 */
function pg_query_params ($connection, $query, array $params) {}

/**
 * Submits a request to create a prepared statement with the 
  given parameters, and waits for completion.
 * @link http://php.net/manual/en/function.pg-prepare.php
 * @param connection resource
 * @param stmtname string
 * @param query string
 * @return resource 
 */
function pg_prepare ($connection, $stmtname, $query) {}

/**
 * Sends a request to execute a prepared statement with given parameters, and waits for the result.
 * @link http://php.net/manual/en/function.pg-execute.php
 * @param connection resource
 * @param stmtname string
 * @param params array
 * @return resource 
 */
function pg_execute ($connection, $stmtname, array $params) {}

/**
 * Sends asynchronous query
 * @link http://php.net/manual/en/function.pg-send-query.php
 * @param connection resource
 * @param query string
 * @return bool 
 */
function pg_send_query ($connection, $query) {}

/**
 * Submits a command and separate parameters to the server without waiting for the result(s).
 * @link http://php.net/manual/en/function.pg-send-query-params.php
 * @param connection resource
 * @param query string
 * @param params array
 * @return bool 
 */
function pg_send_query_params ($connection, $query, array $params) {}

/**
 * Sends a request to create a prepared statement with the given parameters, without waiting for completion.
 * @link http://php.net/manual/en/function.pg-send-prepare.php
 * @param connection resource
 * @param stmtname string
 * @param query string
 * @return bool true on success, false on failure. Use pg_get_result
 */
function pg_send_prepare ($connection, $stmtname, $query) {}

/**
 * Sends a request to execute a prepared statement with given parameters, without waiting for the result(s).
 * @link http://php.net/manual/en/function.pg-send-execute.php
 * @param connection resource
 * @param stmtname string
 * @param params array
 * @return bool true on success, false on failure. Use pg_get_result
 */
function pg_send_execute ($connection, $stmtname, array $params) {}

/**
 * Cancel an asynchronous query
 * @link http://php.net/manual/en/function.pg-cancel-query.php
 * @param connection resource
 * @return bool 
 */
function pg_cancel_query ($connection) {}

/**
 * Returns values from a result resource
 * @link http://php.net/manual/en/function.pg-fetch-result.php
 * @param result resource
 * @param row int
 * @param field mixed
 * @return string 
 */
function pg_fetch_result ($result, $row, $field) {}

/**
 * Get a row as an enumerated array
 * @link http://php.net/manual/en/function.pg-fetch-row.php
 * @param result resource
 * @param row int[optional]
 * @param result_type int[optional]
 * @return array 
 */
function pg_fetch_row ($result, $row = null, $result_type = null) {}

/**
 * Fetch a row as an associative array
 * @link http://php.net/manual/en/function.pg-fetch-assoc.php
 * @param result resource
 * @param row int[optional]
 * @return array 
 */
function pg_fetch_assoc ($result, $row = null) {}

/**
 * Fetch a row as an array
 * @link http://php.net/manual/en/function.pg-fetch-array.php
 * @param result resource
 * @param row int[optional]
 * @param result_type int[optional]
 * @return array 
 */
function pg_fetch_array ($result, $row = null, $result_type = null) {}

/**
 * Fetch a row as an object
 * @link http://php.net/manual/en/function.pg-fetch-object.php
 * @param result resource
 * @param row int[optional]
 * @param result_type int[optional]
 * @return object 
 */
function pg_fetch_object ($result, $row = null, $result_type = null) {}

/**
 * Fetches all rows from a result as an array
 * @link http://php.net/manual/en/function.pg-fetch-all.php
 * @param result resource
 * @return array 
 */
function pg_fetch_all ($result) {}

/**
 * Fetches all rows in a particular result column as an array
 * @link http://php.net/manual/en/function.pg-fetch-all-columns.php
 * @param result resource
 * @param column int[optional]
 * @return array 
 */
function pg_fetch_all_columns ($result, $column = null) {}

/**
 * Returns number of affected records (tuples)
 * @link http://php.net/manual/en/function.pg-affected-rows.php
 * @param result resource
 * @return int 
 */
function pg_affected_rows ($result) {}

/**
 * Get asynchronous query result
 * @link http://php.net/manual/en/function.pg-get-result.php
 * @param connection resource[optional]
 * @return resource 
 */
function pg_get_result ($connection = null) {}

/**
 * Set internal row offset in result resource
 * @link http://php.net/manual/en/function.pg-result-seek.php
 * @param result resource
 * @param offset int
 * @return bool 
 */
function pg_result_seek ($result, $offset) {}

/**
 * Get status of query result
 * @link http://php.net/manual/en/function.pg-result-status.php
 * @param result resource
 * @param type int[optional]
 * @return mixed 
 */
function pg_result_status ($result, $type = null) {}

/**
 * Free result memory
 * @link http://php.net/manual/en/function.pg-free-result.php
 * @param result resource
 * @return bool 
 */
function pg_free_result ($result) {}

/**
 * Returns the last row's OID
 * @link http://php.net/manual/en/function.pg-last-oid.php
 * @param result resource
 * @return string 
 */
function pg_last_oid ($result) {}

/**
 * Returns the number of rows in a result
 * @link http://php.net/manual/en/function.pg-num-rows.php
 * @param result resource
 * @return int 
 */
function pg_num_rows ($result) {}

/**
 * Returns the number of fields in a result
 * @link http://php.net/manual/en/function.pg-num-fields.php
 * @param result resource
 * @return int 
 */
function pg_num_fields ($result) {}

/**
 * Returns the name of a field
 * @link http://php.net/manual/en/function.pg-field-name.php
 * @param result resource
 * @param field_number int
 * @return string 
 */
function pg_field_name ($result, $field_number) {}

/**
 * Returns the field number of the named field
 * @link http://php.net/manual/en/function.pg-field-num.php
 * @param result resource
 * @param field_name string
 * @return int 
 */
function pg_field_num ($result, $field_name) {}

/**
 * Returns the internal storage size of the named field
 * @link http://php.net/manual/en/function.pg-field-size.php
 * @param result resource
 * @param field_number int
 * @return int 
 */
function pg_field_size ($result, $field_number) {}

/**
 * Returns the type name for the corresponding field number
 * @link http://php.net/manual/en/function.pg-field-type.php
 * @param result resource
 * @param field_number int
 * @return string 
 */
function pg_field_type ($result, $field_number) {}

/**
 * Returns the type ID (OID) for the corresponding field number
 * @link http://php.net/manual/en/function.pg-field-type-oid.php
 * @param result resource
 * @param field_number int
 * @return int 
 */
function pg_field_type_oid ($result, $field_number) {}

/**
 * Returns the printed length
 * @link http://php.net/manual/en/function.pg-field-prtlen.php
 * @param result resource
 * @param row_number int
 * @param field_name_or_number mixed
 * @return int 
 */
function pg_field_prtlen ($result, $row_number, $field_name_or_number) {}

/**
 * Test if a field is SQL <literal>NULL</literal>
 * @link http://php.net/manual/en/function.pg-field-is-null.php
 * @param result resource
 * @param row int
 * @param field mixed
 * @return int 1 if the field in the given row is SQL NULL, 0
 */
function pg_field_is_null ($result, $row, $field) {}

/**
 * Returns the name or oid of the tables field
 * @link http://php.net/manual/en/function.pg-field-table.php
 * @param result resource
 * @param field_number int
 * @param oid_only bool[optional]
 * @return mixed 
 */
function pg_field_table ($result, $field_number, $oid_only = null) {}

/**
 * Gets SQL NOTIFY message
 * @link http://php.net/manual/en/function.pg-get-notify.php
 * @param connection resource
 * @param result_type int[optional]
 * @return array 
 */
function pg_get_notify ($connection, $result_type = null) {}

/**
 * Gets the backend's process ID
 * @link http://php.net/manual/en/function.pg-get-pid.php
 * @param connection resource
 * @return int 
 */
function pg_get_pid ($connection) {}

/**
 * Get error message associated with result
 * @link http://php.net/manual/en/function.pg-result-error.php
 * @param result resource
 * @return string a string if there is an error associated with the
 */
function pg_result_error ($result) {}

/**
 * Returns an individual field of an error report.
 * @link http://php.net/manual/en/function.pg-result-error-field.php
 * @param result resource
 * @param fieldcode int
 * @return string 
 */
function pg_result_error_field ($result, $fieldcode) {}

/**
 * Get the last error message string of a connection
 * @link http://php.net/manual/en/function.pg-last-error.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_last_error ($connection = null) {}

/**
 * Returns the last notice message from PostgreSQL server
 * @link http://php.net/manual/en/function.pg-last-notice.php
 * @param connection resource
 * @return string 
 */
function pg_last_notice ($connection) {}

/**
 * Send a NULL-terminated string to PostgreSQL backend
 * @link http://php.net/manual/en/function.pg-put-line.php
 * @param data string
 * @return bool 
 */
function pg_put_line ($data) {}

/**
 * Sync with PostgreSQL backend
 * @link http://php.net/manual/en/function.pg-end-copy.php
 * @param connection resource[optional]
 * @return bool 
 */
function pg_end_copy ($connection = null) {}

/**
 * Copy a table to an array
 * @link http://php.net/manual/en/function.pg-copy-to.php
 * @param connection resource
 * @param table_name string
 * @param delimiter string[optional]
 * @param null_as string[optional]
 * @return array 
 */
function pg_copy_to ($connection, $table_name, $delimiter = null, $null_as = null) {}

/**
 * Insert records into a table from an array
 * @link http://php.net/manual/en/function.pg-copy-from.php
 * @param connection resource
 * @param table_name string
 * @param rows array
 * @param delimiter string[optional]
 * @param null_as string[optional]
 * @return bool 
 */
function pg_copy_from ($connection, $table_name, array $rows, $delimiter = null, $null_as = null) {}

/**
 * Enable tracing a PostgreSQL connection
 * @link http://php.net/manual/en/function.pg-trace.php
 * @param pathname string
 * @param mode string[optional]
 * @param connection resource[optional]
 * @return bool 
 */
function pg_trace ($pathname, $mode = null, $connection = null) {}

/**
 * Disable tracing of a PostgreSQL connection
 * @link http://php.net/manual/en/function.pg-untrace.php
 * @param connection resource[optional]
 * @return bool 
 */
function pg_untrace ($connection = null) {}

/**
 * Create a large object
 * @link http://php.net/manual/en/function.pg-lo-create.php
 * @param connection resource[optional]
 * @return int 
 */
function pg_lo_create ($connection = null) {}

/**
 * Delete a large object
 * @link http://php.net/manual/en/function.pg-lo-unlink.php
 * @param connection resource
 * @param oid int
 * @return bool 
 */
function pg_lo_unlink ($connection, $oid) {}

/**
 * Open a large object
 * @link http://php.net/manual/en/function.pg-lo-open.php
 * @param connection resource
 * @param oid int
 * @param mode string
 * @return resource 
 */
function pg_lo_open ($connection, $oid, $mode) {}

/**
 * Close a large object
 * @link http://php.net/manual/en/function.pg-lo-close.php
 * @param large_object resource
 * @return bool 
 */
function pg_lo_close ($large_object) {}

/**
 * Read a large object
 * @link http://php.net/manual/en/function.pg-lo-read.php
 * @param large_object resource
 * @param len int[optional]
 * @return string 
 */
function pg_lo_read ($large_object, $len = null) {}

/**
 * Write to a large object
 * @link http://php.net/manual/en/function.pg-lo-write.php
 * @param large_object resource
 * @param data string
 * @param len int[optional]
 * @return int 
 */
function pg_lo_write ($large_object, $data, $len = null) {}

/**
 * Reads an entire large object and send straight to browser
 * @link http://php.net/manual/en/function.pg-lo-read-all.php
 * @param large_object resource
 * @return int 
 */
function pg_lo_read_all ($large_object) {}

/**
 * Import a large object from file
 * @link http://php.net/manual/en/function.pg-lo-import.php
 * @param connection resource
 * @param pathname string
 * @return int 
 */
function pg_lo_import ($connection, $pathname) {}

/**
 * Export a large object to file
 * @link http://php.net/manual/en/function.pg-lo-export.php
 * @param connection resource
 * @param oid int
 * @param pathname string
 * @return bool 
 */
function pg_lo_export ($connection, $oid, $pathname) {}

/**
 * Seeks position within a large object
 * @link http://php.net/manual/en/function.pg-lo-seek.php
 * @param large_object resource
 * @param offset int
 * @param whence int[optional]
 * @return bool 
 */
function pg_lo_seek ($large_object, $offset, $whence = null) {}

/**
 * Returns current seek position a of large object
 * @link http://php.net/manual/en/function.pg-lo-tell.php
 * @param large_object resource
 * @return int 
 */
function pg_lo_tell ($large_object) {}

/**
 * Escape a string for insertion into a text field
 * @link http://php.net/manual/en/function.pg-escape-string.php
 * @param connection resource[optional]
 * @param data string
 * @return string 
 */
function pg_escape_string ($connection = null, $data) {}

/**
 * Escape a string for insertion into a bytea field
 * @link http://php.net/manual/en/function.pg-escape-bytea.php
 * @param connection resource[optional]
 * @param data string
 * @return string 
 */
function pg_escape_bytea ($connection = null, $data) {}

/**
 * Unescape binary for bytea type
 * @link http://php.net/manual/en/function.pg-unescape-bytea.php
 * @param data string
 * @return string 
 */
function pg_unescape_bytea ($data) {}

/**
 * Determines the verbosity of messages returned by <function>pg_last_error</function> 
   and <function>pg_result_error</function>.
 * @link http://php.net/manual/en/function.pg-set-error-verbosity.php
 * @param connection resource
 * @param verbosity int
 * @return int 
 */
function pg_set_error_verbosity ($connection, $verbosity) {}

/**
 * Gets the client encoding
 * @link http://php.net/manual/en/function.pg-client-encoding.php
 * @param connection resource[optional]
 * @return string 
 */
function pg_client_encoding ($connection = null) {}

/**
 * Set the client encoding
 * @link http://php.net/manual/en/function.pg-set-client-encoding.php
 * @param encoding string
 * @return int 0 on success or -1 on error.
 */
function pg_set_client_encoding ($encoding) {}

/**
 * Get meta data for table
 * @link http://php.net/manual/en/function.pg-meta-data.php
 * @param connection resource
 * @param table_name string
 * @return array 
 */
function pg_meta_data ($connection, $table_name) {}

/**
 * Convert associative array values into suitable for SQL statement
 * @link http://php.net/manual/en/function.pg-convert.php
 * @param connection resource
 * @param table_name string
 * @param assoc_array array
 * @param options int[optional]
 * @return array 
 */
function pg_convert ($connection, $table_name, array $assoc_array, $options = null) {}

/**
 * Insert array into table
 * @link http://php.net/manual/en/function.pg-insert.php
 * @param connection resource
 * @param table_name string
 * @param assoc_array array
 * @param options int[optional]
 * @return mixed 
 */
function pg_insert ($connection, $table_name, array $assoc_array, $options = null) {}

/**
 * Update table
 * @link http://php.net/manual/en/function.pg-update.php
 * @param connection resource
 * @param table_name string
 * @param data array
 * @param condition array
 * @param options int[optional]
 * @return mixed 
 */
function pg_update ($connection, $table_name, array $data, array $condition, $options = null) {}

/**
 * Deletes records
 * @link http://php.net/manual/en/function.pg-delete.php
 * @param connection resource
 * @param table_name string
 * @param assoc_array array
 * @param options int[optional]
 * @return mixed 
 */
function pg_delete ($connection, $table_name, array $assoc_array, $options = null) {}

/**
 * Select records
 * @link http://php.net/manual/en/function.pg-select.php
 * @param connection resource
 * @param table_name string
 * @param assoc_array array
 * @param options int[optional]
 * @return mixed 
 */
function pg_select ($connection, $table_name, array $assoc_array, $options = null) {}

function pg_exec () {}

function pg_getlastoid () {}

function pg_cmdtuples () {}

function pg_errormessage () {}

function pg_numrows () {}

function pg_numfields () {}

function pg_fieldname () {}

function pg_fieldsize () {}

function pg_fieldtype () {}

function pg_fieldnum () {}

function pg_fieldprtlen () {}

function pg_fieldisnull () {}

function pg_freeresult () {}

function pg_result () {}

function pg_loreadall () {}

function pg_locreate () {}

function pg_lounlink () {}

function pg_loopen () {}

function pg_loclose () {}

function pg_loread () {}

function pg_lowrite () {}

function pg_loimport () {}

function pg_loexport () {}

function pg_clientencoding () {}

function pg_setclientencoding () {}


/**
 * Passed to pg_connect to force the creation of a new connection,
 * rather then re-using an existing identical connection.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_CONNECT_FORCE_NEW', 2);

/**
 * Passed to pg_fetch_array. Return an associative array of field
 * names and values.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_ASSOC', 1);

/**
 * Passed to pg_fetch_array. Return a numerically indexed array of field
 * numbers and values.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_NUM', 2);

/**
 * Passed to pg_fetch_array. Return an array of field values
 * that is both numerically indexed (by field number) and associated (by field name).
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_BOTH', 3);

/**
 * Returned by pg_connection_status indicating that the database
 * connection is in an invalid state.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_CONNECTION_BAD', 1);

/**
 * Returned by pg_connection_status indicating that the database
 * connection is in a valid state.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_CONNECTION_OK', 0);

/**
 * Returned by pg_transaction_status. Connection is
 * currently idle, not in a transaction.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TRANSACTION_IDLE', 0);

/**
 * Returned by pg_transaction_status. A command
 * is in progress on the connection. A query has been sent via the connection
 * and not yet completed.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TRANSACTION_ACTIVE', 1);

/**
 * Returned by pg_transaction_status. The connection
 * is idle, in a transaction block.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TRANSACTION_INTRANS', 2);

/**
 * Returned by pg_transaction_status. The connection
 * is idle, in a failed transaction block.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TRANSACTION_INERROR', 3);

/**
 * Returned by pg_transaction_status. The connection
 * is bad.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TRANSACTION_UNKNOWN', 4);

/**
 * Passed to pg_set_error_verbosity.
 * Specified that returned messages include severity, primary text, 
 * and position only; this will normally fit on a single line.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_ERRORS_TERSE', 0);

/**
 * Passed to pg_set_error_verbosity.
 * The default mode produces messages that include the above 
 * plus any detail, hint, or context fields (these may span 
 * multiple lines).
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_ERRORS_DEFAULT', 1);

/**
 * Passed to pg_set_error_verbosity.
 * The verbose mode includes all available fields.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_ERRORS_VERBOSE', 2);

/**
 * Passed to pg_lo_seek. Seek operation is to begin
 * from the start of the object.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_SEEK_SET', 0);

/**
 * Passed to pg_lo_seek. Seek operation is to begin
 * from the current position.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_SEEK_CUR', 1);

/**
 * Passed to pg_lo_seek. Seek operation is to begin
 * from the end of the object.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_SEEK_END', 2);

/**
 * Passed to pg_result_status. Indicates that
 * numerical result code is desired.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_STATUS_LONG', 1);

/**
 * Passed to pg_result_status. Indicates that
 * textual result command tag is desired.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_STATUS_STRING', 2);

/**
 * Returned by pg_result_status. The string sent to the server
 * was empty.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_EMPTY_QUERY', 0);

/**
 * Returned by pg_result_status. Successful completion of a 
 * command returning no data.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_COMMAND_OK', 1);

/**
 * Returned by pg_result_status. Successful completion of a command 
 * returning data (such as a SELECT or SHOW).
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_TUPLES_OK', 2);

/**
 * Returned by pg_result_status. Copy Out (from server) data 
 * transfer started.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_COPY_OUT', 3);

/**
 * Returned by pg_result_status. Copy In (to server) data 
 * transfer started.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_COPY_IN', 4);

/**
 * Returned by pg_result_status. The server's response 
 * was not understood.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_BAD_RESPONSE', 5);

/**
 * Returned by pg_result_status. A nonfatal error 
 * (a notice or warning) occurred.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_NONFATAL_ERROR', 6);

/**
 * Returned by pg_result_status. A fatal error 
 * occurred.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_FATAL_ERROR', 7);

/**
 * Passed to pg_result_error_field.
 * The severity; the field contents are ERROR, 
 * FATAL, or PANIC (in an error message), or 
 * WARNING, NOTICE, DEBUG, 
 * INFO, or LOG (in a notice message), or a localized 
 * translation of one of these. Always present.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_SEVERITY', 83);

/**
 * Passed to pg_result_error_field.
 * The SQLSTATE code for the error. The SQLSTATE code identifies the type of error 
 * that has occurred; it can be used by front-end applications to perform specific 
 * operations (such as error handling) in response to a particular database error. 
 * This field is not localizable, and is always present.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_SQLSTATE', 67);

/**
 * Passed to pg_result_error_field.
 * The primary human-readable error message (typically one line). Always present.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_MESSAGE_PRIMARY', 77);

/**
 * Passed to pg_result_error_field.
 * Detail: an optional secondary error message carrying more detail about the problem. May run to multiple lines.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_MESSAGE_DETAIL', 68);

/**
 * Passed to pg_result_error_field.
 * Hint: an optional suggestion what to do about the problem. This is intended to differ from detail in that it
 * offers advice (potentially inappropriate) rather than hard facts. May run to multiple lines.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_MESSAGE_HINT', 72);

/**
 * Passed to pg_result_error_field.
 * A string containing a decimal integer indicating an error cursor position as an index into the original 
 * statement string. The first character has index 1, and positions are measured in characters not bytes.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_STATEMENT_POSITION', 80);

/**
 * Passed to pg_result_error_field.
 * This is defined the same as the PG_DIAG_STATEMENT_POSITION field, but 
 * it is used when the cursor position refers to an internally generated 
 * command rather than the one submitted by the client. The 
 * PG_DIAG_INTERNAL_QUERY field will always appear when this 
 * field appears.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_INTERNAL_POSITION', 112);

/**
 * Passed to pg_result_error_field.
 * The text of a failed internally-generated command. This could be, for example, a 
 * SQL query issued by a PL/pgSQL function.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_INTERNAL_QUERY', 113);

/**
 * Passed to pg_result_error_field.
 * An indication of the context in which the error occurred. Presently 
 * this includes a call stack traceback of active procedural language 
 * functions and internally-generated queries. The trace is one entry 
 * per line, most recent first.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_CONTEXT', 87);

/**
 * Passed to pg_result_error_field.
 * The file name of the PostgreSQL source-code location where the error 
 * was reported.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_SOURCE_FILE', 70);

/**
 * Passed to pg_result_error_field.
 * The line number of the PostgreSQL source-code location where the 
 * error was reported.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_SOURCE_LINE', 76);

/**
 * Passed to pg_result_error_field.
 * The name of the PostgreSQL source-code function reporting the error.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_DIAG_SOURCE_FUNCTION', 82);

/**
 * Passed to pg_convert.
 * Ignore conversion of &null; into SQL NOT NULL columns.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_CONV_IGNORE_DEFAULT', 2);

/**
 * Passed to pg_convert.
 * Use SQL NULL in place of an empty string.
 * @link http://php.net/manual/en/pgsql.constants.php
 */
define ('PGSQL_CONV_FORCE_NULL', 4);
define ('PGSQL_CONV_IGNORE_NOT_NULL', 8);
define ('PGSQL_DML_NO_CONV', 256);
define ('PGSQL_DML_EXEC', 512);
define ('PGSQL_DML_ASYNC', 1024);
define ('PGSQL_DML_STRING', 2048);

// End of pgsql v.

// Start of soap v.

/**
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapClient  {

	public function SoapClient () {}

	/**
	 * Calls a SOAP function (deprecated)
	 * @link http://php.net/manual/en/function.soap-soapclient-call.php
	 * @param function_name string
	 * @param arguments array
	 * @param options array[optional]
	 * @param input_headers array[optional]
	 * @param output_headers array[optional]
	 * @return mixed 
	 */
	public function __call ($function_name, array $arguments, array $options = null, array $input_headers = null, array $output_headers = null) {}

	/**
	 * Calls a SOAP function
	 * @link http://php.net/manual/en/function.soap-soapclient-soapcall.php
	 * @param function_name string
	 * @param arguments array
	 * @param options array[optional]
	 * @param input_headers mixed[optional]
	 * @param output_headers array[optional]
	 * @return mixed 
	 */
	public function __soapCall ($function_name, array $arguments, array $options = null, $input_headers = null, array &$output_headers = null) {}

	/**
	 * Returns last SOAP request
	 * @link http://php.net/manual/en/function.soap-soapclient-getlastrequest.php
	 * @return string 
	 */
	public function __getLastRequest () {}

	/**
	 * Returns last SOAP response.
	 * @link http://php.net/manual/en/function.soap-soapclient-getlastresponse.php
	 * @return string 
	 */
	public function __getLastResponse () {}

	/**
	 * Returns last SOAP request headers
	 * @link http://php.net/manual/en/function.soap-soapclient-getlastrequestheaders.php
	 * @return string 
	 */
	public function __getLastRequestHeaders () {}

	/**
	 * Returns last SOAP response headers.
	 * @link http://php.net/manual/en/function.soap-soapclient-getlastresponseheaders.php
	 * @return string 
	 */
	public function __getLastResponseHeaders () {}

	/**
	 * Returns list of SOAP functions
	 * @link http://php.net/manual/en/function.soap-soapclient-getfunctions.php
	 * @return array 
	 */
	public function __getFunctions () {}

	/**
	 * Returns list of SOAP types
	 * @link http://php.net/manual/en/function.soap-soapclient-gettypes.php
	 * @return array 
	 */
	public function __getTypes () {}

	/**
	 * Performs a SOAP request
	 * @link http://php.net/manual/en/function.soap-soapclient-dorequest.php
	 * @param request string
	 * @param location string
	 * @param action string
	 * @param version int
	 * @param one_way int[optional]
	 * @return string 
	 */
	public function __doRequest ($request, $location, $action, $version, $one_way = null) {}

	/**
	 * Sets the cookie that will be sent with the SOAP request
	 * @link http://php.net/manual/en/function.soap-soapclient-setcookie.php
	 * @param name string
	 * @param value string[optional]
	 * @return void 
	 */
	public function __setCookie ($name, $value = null) {}

	public function __setLocation () {}

	public function __setSoapHeaders () {}

}

/**
 * SoapVar is a special low-level class for encoding
 * parameters and returning values in non-WSDL mode. It's
 * just a data holder and does not have any special methods except the constructor. 
 * It's useful when you want to set the type property in SOAP request or response.
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapVar  {

	public function SoapVar () {}

}

/**
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapServer  {

	public function SoapServer () {}

	/**
	 * Sets persistence mode of SoapServer
	 * @link http://php.net/manual/en/function.soap-soapserver-setpersistence.php
	 * @param mode int
	 * @return void 
	 */
	public function setPersistence ($mode) {}

	/**
	 * Sets class which will handle SOAP requests
	 * @link http://php.net/manual/en/function.soap-soapserver-setclass.php
	 * @param class_name string
	 * @param args mixed[optional]
	 * @param ... mixed[optional]
	 * @return void 
	 */
	public function setClass ($class_name, $args = null) {}

	public function setObject () {}

	/**
	 * Adds one or several functions those will handle SOAP requests
	 * @link http://php.net/manual/en/function.soap-soapserver-addfunction.php
	 * @param functions mixed
	 * @return void 
	 */
	public function addFunction ($functions) {}

	/**
	 * Returns list of defined functions
	 * @link http://php.net/manual/en/function.soap-soapserver-getfunctions.php
	 * @return array 
	 */
	public function getFunctions () {}

	/**
	 * Handles a SOAP request
	 * @link http://php.net/manual/en/function.soap-soapserver-handle.php
	 * @param soap_request string[optional]
	 * @return void 
	 */
	public function handle ($soap_request = null) {}

	/**
	 * Issue SoapServer fault indicating an error
	 * @link http://php.net/manual/en/function.soap-soapserver-fault.php
	 * @param code string
	 * @param string string
	 * @param actor string[optional]
	 * @param details mixed[optional]
	 * @param name string[optional]
	 * @return void 
	 */
	public function fault ($code, $string, $actor = null, $details = null, $name = null) {}

	public function addSoapHeader () {}

}

/**
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapFault extends Exception  {
	protected $message;
	protected $code;
	protected $file;
	protected $line;


	public function SoapFault () {}

	public function __toString () {}

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

}

/**
 * SoapParam is a special low-level class for naming 
 * parameters and returning values in non-WSDL mode. 
 * It's just a data holder and it does not have any special methods except 
 * its constructor.
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapParam  {

	public function SoapParam () {}

}

/**
 * SoapHeader is a special low-level class for passing 
 * or returning SOAP headers. It's just a data holder and it does not have any 
 * special methods except its constructor. It can be used in the method to pass a SOAP header or 
 * in a SOAP header handler to return the header in a SOAP response.
 * @link http://php.net/manual/en/ref.soap.php
 */
class SoapHeader  {

	public function SoapHeader () {}

}

/**
 * Set whether to use the SOAP error handler and return the former value
 * @link http://php.net/manual/en/function.use-soap-error-handler.php
 * @param handler bool[optional]
 * @return bool 
 */
function use_soap_error_handler ($handler = null) {}

/**
 * Checks if SOAP call was failed
 * @link http://php.net/manual/en/function.is-soap-fault.php
 * @param obj mixed
 * @return bool 
 */
function is_soap_fault ($obj) {}

define ('SOAP_1_1', 1);
define ('SOAP_1_2', 2);
define ('SOAP_PERSISTENCE_SESSION', 1);
define ('SOAP_PERSISTENCE_REQUEST', 2);
define ('SOAP_FUNCTIONS_ALL', 999);
define ('SOAP_ENCODED', 1);
define ('SOAP_LITERAL', 2);
define ('SOAP_RPC', 1);
define ('SOAP_DOCUMENT', 2);
define ('SOAP_ACTOR_NEXT', 1);
define ('SOAP_ACTOR_NONE', 2);
define ('SOAP_ACTOR_UNLIMATERECEIVER', 3);
define ('SOAP_COMPRESSION_ACCEPT', 32);
define ('SOAP_COMPRESSION_GZIP', 0);
define ('SOAP_COMPRESSION_DEFLATE', 16);
define ('SOAP_AUTHENTICATION_BASIC', 0);
define ('SOAP_AUTHENTICATION_DIGEST', 1);
define ('UNKNOWN_TYPE', 999998);
define ('XSD_STRING', 101);
define ('XSD_BOOLEAN', 102);
define ('XSD_DECIMAL', 103);
define ('XSD_FLOAT', 104);
define ('XSD_DOUBLE', 105);
define ('XSD_DURATION', 106);
define ('XSD_DATETIME', 107);
define ('XSD_TIME', 108);
define ('XSD_DATE', 109);
define ('XSD_GYEARMONTH', 110);
define ('XSD_GYEAR', 111);
define ('XSD_GMONTHDAY', 112);
define ('XSD_GDAY', 113);
define ('XSD_GMONTH', 114);
define ('XSD_HEXBINARY', 115);
define ('XSD_BASE64BINARY', 116);
define ('XSD_ANYURI', 117);
define ('XSD_QNAME', 118);
define ('XSD_NOTATION', 119);
define ('XSD_NORMALIZEDSTRING', 120);
define ('XSD_TOKEN', 121);
define ('XSD_LANGUAGE', 122);
define ('XSD_NMTOKEN', 123);
define ('XSD_NAME', 124);
define ('XSD_NCNAME', 125);
define ('XSD_ID', 126);
define ('XSD_IDREF', 127);
define ('XSD_IDREFS', 128);
define ('XSD_ENTITY', 129);
define ('XSD_ENTITIES', 130);
define ('XSD_INTEGER', 131);
define ('XSD_NONPOSITIVEINTEGER', 132);
define ('XSD_NEGATIVEINTEGER', 133);
define ('XSD_LONG', 134);
define ('XSD_INT', 135);
define ('XSD_SHORT', 136);
define ('XSD_BYTE', 137);
define ('XSD_NONNEGATIVEINTEGER', 138);
define ('XSD_UNSIGNEDLONG', 139);
define ('XSD_UNSIGNEDINT', 140);
define ('XSD_UNSIGNEDSHORT', 141);
define ('XSD_UNSIGNEDBYTE', 142);
define ('XSD_POSITIVEINTEGER', 143);
define ('XSD_NMTOKENS', 144);
define ('XSD_ANYTYPE', 145);

/**
 * Added in PHP 5.1.0.
 * @link http://php.net/manual/en/soap.constants.php
 */
define ('XSD_ANYXML', 147);
define ('APACHE_MAP', 200);
define ('SOAP_ENC_OBJECT', 301);
define ('SOAP_ENC_ARRAY', 300);
define ('XSD_1999_TIMEINSTANT', 401);
define ('XSD_NAMESPACE', "http://www.w3.org/2001/XMLSchema");
define ('XSD_1999_NAMESPACE', "http://www.w3.org/1999/XMLSchema");
define ('SOAP_SINGLE_ELEMENT_ARRAYS', 1);

/**
 * Added in PHP 5.1.0.
 * @link http://php.net/manual/en/soap.constants.php
 */
define ('SOAP_WAIT_ONE_WAY_CALLS', 2);
define ('SOAP_USE_XSI_ARRAY_TYPE', 4);
define ('WSDL_CACHE_NONE', 0);
define ('WSDL_CACHE_DISK', 1);
define ('WSDL_CACHE_MEMORY', 2);
define ('WSDL_CACHE_BOTH', 3);

// End of soap v.

// Start of sysvmsg v.

/**
 * Create or attach to a message queue
 * @link http://php.net/manual/en/function.msg-get-queue.php
 * @param key int
 * @param perms int[optional]
 * @return resource an id that can be used to access the System V message queue.
 */
function msg_get_queue ($key, $perms = null) {}

/**
 * Send a message to a message queue
 * @link http://php.net/manual/en/function.msg-send.php
 * @param queue resource
 * @param msgtype int
 * @param message mixed
 * @param serialize bool[optional]
 * @param blocking bool[optional]
 * @param errorcode int[optional]
 * @return bool 
 */
function msg_send ($queue, $msgtype, $message, $serialize = null, $blocking = null, &$errorcode = null) {}

/**
 * Receive a message from a message queue
 * @link http://php.net/manual/en/function.msg-receive.php
 * @param queue resource
 * @param desiredmsgtype int
 * @param msgtype int
 * @param maxsize int
 * @param message mixed
 * @param unserialize bool[optional]
 * @param flags int[optional]
 * @param errorcode int[optional]
 * @return bool 
 */
function msg_receive ($queue, $desiredmsgtype, &$msgtype, $maxsize, &$message, $unserialize = null, $flags = null, &$errorcode = null) {}

/**
 * Destroy a message queue
 * @link http://php.net/manual/en/function.msg-remove-queue.php
 * @param queue resource
 * @return bool 
 */
function msg_remove_queue ($queue) {}

/**
 * Returns information from the message queue data structure
 * @link http://php.net/manual/en/function.msg-stat-queue.php
 * @param queue resource
 * @return array 
 */
function msg_stat_queue ($queue) {}

/**
 * Set information in the message queue data structure
 * @link http://php.net/manual/en/function.msg-set-queue.php
 * @param queue resource
 * @param data array
 * @return bool 
 */
function msg_set_queue ($queue, array $data) {}

define ('MSG_IPC_NOWAIT', 1);
define ('MSG_EAGAIN', 11);
define ('MSG_ENOMSG', 42);
define ('MSG_NOERROR', 2);
define ('MSG_EXCEPT', 4);

// End of sysvmsg v.

// Start of tidy v.2.0

class tidy  {

	public function getOpt () {}

	public function cleanRepair () {}

	public function parseFile () {}

	public function parseString () {}

	public function repairString () {}

	public function repairFile () {}

	public function diagnose () {}

	public function getRelease () {}

	public function getConfig () {}

	public function getStatus () {}

	public function getHtmlVer () {}

	public function isXhtml () {}

	public function isXml () {}

	public function root () {}

	public function head () {}

	public function html () {}

	public function body () {}

	/**
	 * Constructs a new tidy object
	 * @link http://php.net/manual/en/function.tidy-construct.php
	 */
	public function __construct () {}

}

/**
 * @link http://php.net/manual/en/ref.tidy.php
 */
final class tidyNode  {

	/**
	 * Returns true if this node has children
	 * @link http://php.net/manual/en/function.tidyNode-hasChildren.php
	 */
	public function hasChildren () {}

	/**
	 * Returns true if this node has siblings
	 * @link http://php.net/manual/en/function.tidyNode-hasSiblings.php
	 */
	public function hasSiblings () {}

	/**
	 * Returns true if this node represents a comment
	 * @link http://php.net/manual/en/function.tidyNode-isComment.php
	 */
	public function isComment () {}

	/**
	 * Returns true if this node is part of a HTML document
	 * @link http://php.net/manual/en/function.tidyNode-isHtml.php
	 */
	public function isHtml () {}

	/**
	 * Returns true if this node represents text (no markup)
	 * @link http://php.net/manual/en/function.tidyNode-isText.php
	 */
	public function isText () {}

	/**
	 * Returns true if this node is JSTE
	 * @link http://php.net/manual/en/function.tidyNode-isJste.php
	 */
	public function isJste () {}

	/**
	 * Returns true if this node is ASP
	 * @link http://php.net/manual/en/function.tidyNode-isAsp.php
	 */
	public function isAsp () {}

	/**
	 * Returns true if this node is PHP
	 * @link http://php.net/manual/en/function.tidyNode-isPhp.php
	 */
	public function isPhp () {}

	/**
	 * returns the parent node of the current node
	 * @link http://php.net/manual/en/function.tidynode-getparent.php
	 * @return tidyNode a tidyNode if the node has a parent, or &null;
	 */
	public function getParent () {}

}

/**
 * Returns the value of the specified configuration option for the tidy document
 * @link http://php.net/manual/en/function.tidy-getopt.php
 */
function tidy_getopt () {}

/**
 * Parse a document stored in a string
 * @link http://php.net/manual/en/function.tidy-parse-string.php
 */
function tidy_parse_string () {}

/**
 * Parse markup in file or URI
 * @link http://php.net/manual/en/function.tidy-parse-file.php
 */
function tidy_parse_file () {}

/**
 * Return a string representing the parsed tidy markup
 * @link http://php.net/manual/en/function.tidy-get-output.php
 */
function tidy_get_output () {}

/**
 * Return warnings and errors which occurred parsing the specified document
 * @link http://php.net/manual/en/function.tidy-get-error-buffer.php
 */
function tidy_get_error_buffer () {}

/**
 * Execute configured cleanup and repair operations on parsed markup
 * @link http://php.net/manual/en/function.tidy-clean-repair.php
 */
function tidy_clean_repair () {}

/**
 * Repair a string using an optionally provided configuration file
 * @link http://php.net/manual/en/function.tidy-repair-string.php
 */
function tidy_repair_string () {}

/**
 * Repair a file and return it as a string
 * @link http://php.net/manual/en/function.tidy-repair-file.php
 */
function tidy_repair_file () {}

/**
 * Run configured diagnostics on parsed and repaired markup
 * @link http://php.net/manual/en/function.tidy-diagnose.php
 */
function tidy_diagnose () {}

/**
 * Get release date (version) for Tidy library
 * @link http://php.net/manual/en/function.tidy-get-release.php
 */
function tidy_get_release () {}

/**
 * Get current Tidy configuration
 * @link http://php.net/manual/en/function.tidy-get-config.php
 */
function tidy_get_config () {}

/**
 * Get status of specified document
 * @link http://php.net/manual/en/function.tidy-get-status.php
 */
function tidy_get_status () {}

/**
 * Get the Detected HTML version for the specified document
 * @link http://php.net/manual/en/function.tidy-get-html-ver.php
 */
function tidy_get_html_ver () {}

/**
 * Indicates if the document is a XHTML document
 * @link http://php.net/manual/en/function.tidy-is-xhtml.php
 */
function tidy_is_xhtml () {}

/**
 * Indicates if the document is a generic (non HTML/XHTML) XML document
 * @link http://php.net/manual/en/function.tidy-is-xml.php
 */
function tidy_is_xml () {}

/**
 * Returns the Number of Tidy errors encountered for specified document
 * @link http://php.net/manual/en/function.tidy-error-count.php
 */
function tidy_error_count () {}

/**
 * Returns the Number of Tidy warnings encountered for specified document
 * @link http://php.net/manual/en/function.tidy-warning-count.php
 */
function tidy_warning_count () {}

/**
 * Returns the Number of Tidy accessibility warnings encountered for specified document
 * @link http://php.net/manual/en/function.tidy-access-count.php
 */
function tidy_access_count () {}

/**
 * Returns the Number of Tidy configuration errors encountered for specified document
 * @link http://php.net/manual/en/function.tidy-config-count.php
 */
function tidy_config_count () {}

/**
 * Returns a tidyNode object representing the root of the tidy parse tree
 * @link http://php.net/manual/en/function.tidy-get-root.php
 */
function tidy_get_root () {}

/**
 * Returns a tidyNode Object starting from the &lt;head&gt; tag of the tidy parse tree
 * @link http://php.net/manual/en/function.tidy-get-head.php
 */
function tidy_get_head () {}

/**
 * Returns a tidyNode Object starting from the &lt;html&gt; tag of the tidy parse tree
 * @link http://php.net/manual/en/function.tidy-get-html.php
 */
function tidy_get_html () {}

/**
 * Returns a tidyNode Object starting from the &lt;body&gt; tag of the tidy parse tree
 * @link http://php.net/manual/en/function.tidy-get-body.php
 */
function tidy_get_body () {}

/**
 * ob_start callback function to repair the buffer
 * @link http://php.net/manual/en/function.ob-tidyhandler.php
 */
function ob_tidyhandler () {}

define ('TIDY_TAG_UNKNOWN', 0);
define ('TIDY_TAG_A', 1);
define ('TIDY_TAG_ABBR', 2);
define ('TIDY_TAG_ACRONYM', 3);
define ('TIDY_TAG_ADDRESS', 4);
define ('TIDY_TAG_ALIGN', 5);
define ('TIDY_TAG_APPLET', 6);
define ('TIDY_TAG_AREA', 7);
define ('TIDY_TAG_B', 8);
define ('TIDY_TAG_BASE', 9);
define ('TIDY_TAG_BASEFONT', 10);
define ('TIDY_TAG_BDO', 11);
define ('TIDY_TAG_BGSOUND', 12);
define ('TIDY_TAG_BIG', 13);
define ('TIDY_TAG_BLINK', 14);
define ('TIDY_TAG_BLOCKQUOTE', 15);
define ('TIDY_TAG_BODY', 16);
define ('TIDY_TAG_BR', 17);
define ('TIDY_TAG_BUTTON', 18);
define ('TIDY_TAG_CAPTION', 19);
define ('TIDY_TAG_CENTER', 20);
define ('TIDY_TAG_CITE', 21);
define ('TIDY_TAG_CODE', 22);
define ('TIDY_TAG_COL', 23);
define ('TIDY_TAG_COLGROUP', 24);
define ('TIDY_TAG_COMMENT', 25);
define ('TIDY_TAG_DD', 26);
define ('TIDY_TAG_DEL', 27);
define ('TIDY_TAG_DFN', 28);
define ('TIDY_TAG_DIR', 29);
define ('TIDY_TAG_DIV', 30);
define ('TIDY_TAG_DL', 31);
define ('TIDY_TAG_DT', 32);
define ('TIDY_TAG_EM', 33);
define ('TIDY_TAG_EMBED', 34);
define ('TIDY_TAG_FIELDSET', 35);
define ('TIDY_TAG_FONT', 36);
define ('TIDY_TAG_FORM', 37);
define ('TIDY_TAG_FRAME', 38);
define ('TIDY_TAG_FRAMESET', 39);
define ('TIDY_TAG_H1', 40);
define ('TIDY_TAG_H2', 41);
define ('TIDY_TAG_H3', 42);
define ('TIDY_TAG_H4', 43);
define ('TIDY_TAG_H5', 44);
define ('TIDY_TAG_H6', 45);
define ('TIDY_TAG_HEAD', 46);
define ('TIDY_TAG_HR', 47);
define ('TIDY_TAG_HTML', 48);
define ('TIDY_TAG_I', 49);
define ('TIDY_TAG_IFRAME', 50);
define ('TIDY_TAG_ILAYER', 51);
define ('TIDY_TAG_IMG', 52);
define ('TIDY_TAG_INPUT', 53);
define ('TIDY_TAG_INS', 54);
define ('TIDY_TAG_ISINDEX', 55);
define ('TIDY_TAG_KBD', 56);
define ('TIDY_TAG_KEYGEN', 57);
define ('TIDY_TAG_LABEL', 58);
define ('TIDY_TAG_LAYER', 59);
define ('TIDY_TAG_LEGEND', 60);
define ('TIDY_TAG_LI', 61);
define ('TIDY_TAG_LINK', 62);
define ('TIDY_TAG_LISTING', 63);
define ('TIDY_TAG_MAP', 64);
define ('TIDY_TAG_MARQUEE', 65);
define ('TIDY_TAG_MENU', 66);
define ('TIDY_TAG_META', 67);
define ('TIDY_TAG_MULTICOL', 68);
define ('TIDY_TAG_NOBR', 69);
define ('TIDY_TAG_NOEMBED', 70);
define ('TIDY_TAG_NOFRAMES', 71);
define ('TIDY_TAG_NOLAYER', 72);
define ('TIDY_TAG_NOSAVE', 73);
define ('TIDY_TAG_NOSCRIPT', 74);
define ('TIDY_TAG_OBJECT', 75);
define ('TIDY_TAG_OL', 76);
define ('TIDY_TAG_OPTGROUP', 77);
define ('TIDY_TAG_OPTION', 78);
define ('TIDY_TAG_P', 79);
define ('TIDY_TAG_PARAM', 80);
define ('TIDY_TAG_PLAINTEXT', 81);
define ('TIDY_TAG_PRE', 82);
define ('TIDY_TAG_Q', 83);
define ('TIDY_TAG_RB', 84);
define ('TIDY_TAG_RBC', 85);
define ('TIDY_TAG_RP', 86);
define ('TIDY_TAG_RT', 87);
define ('TIDY_TAG_RTC', 88);
define ('TIDY_TAG_RUBY', 89);
define ('TIDY_TAG_S', 90);
define ('TIDY_TAG_SAMP', 91);
define ('TIDY_TAG_SCRIPT', 92);
define ('TIDY_TAG_SELECT', 93);
define ('TIDY_TAG_SERVER', 94);
define ('TIDY_TAG_SERVLET', 95);
define ('TIDY_TAG_SMALL', 96);
define ('TIDY_TAG_SPACER', 97);
define ('TIDY_TAG_SPAN', 98);
define ('TIDY_TAG_STRIKE', 99);
define ('TIDY_TAG_STRONG', 100);
define ('TIDY_TAG_STYLE', 101);
define ('TIDY_TAG_SUB', 102);
define ('TIDY_TAG_SUP', 103);
define ('TIDY_TAG_TABLE', 104);
define ('TIDY_TAG_TBODY', 105);
define ('TIDY_TAG_TD', 106);
define ('TIDY_TAG_TEXTAREA', 107);
define ('TIDY_TAG_TFOOT', 108);
define ('TIDY_TAG_TH', 109);
define ('TIDY_TAG_THEAD', 110);
define ('TIDY_TAG_TITLE', 111);
define ('TIDY_TAG_TR', 112);
define ('TIDY_TAG_TT', 113);
define ('TIDY_TAG_U', 114);
define ('TIDY_TAG_UL', 115);
define ('TIDY_TAG_VAR', 116);
define ('TIDY_TAG_WBR', 117);
define ('TIDY_TAG_XMP', 118);
define ('TIDY_NODETYPE_ROOT', 0);
define ('TIDY_NODETYPE_DOCTYPE', 1);
define ('TIDY_NODETYPE_COMMENT', 2);
define ('TIDY_NODETYPE_PROCINS', 3);
define ('TIDY_NODETYPE_TEXT', 4);
define ('TIDY_NODETYPE_START', 5);
define ('TIDY_NODETYPE_END', 6);
define ('TIDY_NODETYPE_STARTEND', 7);
define ('TIDY_NODETYPE_CDATA', 8);
define ('TIDY_NODETYPE_SECTION', 9);
define ('TIDY_NODETYPE_ASP', 10);
define ('TIDY_NODETYPE_JSTE', 11);
define ('TIDY_NODETYPE_PHP', 12);
define ('TIDY_NODETYPE_XMLDECL', 13);

// End of tidy v.2.0

// Start of xmlwriter v.0.1

/**
 * @link http://php.net/manual/en/ref.xmlwriter.php
 */
class XMLWriter  {

	/**
	 * Create new xmlwriter using source uri for output
	 * @link http://php.net/manual/en/function.xmlwriter-open-uri.php
	 * @param uri string
	 * @return bool 
	 */
	public function openUri ($uri) {}

	/**
	 * Create new xmlwriter using memory for string output
	 * @link http://php.net/manual/en/function.xmlwriter-open-memory.php
	 * @return bool 
	 */
	public function openMemory () {}

	/**
	 * Toggle indentation on/off
	 * @link http://php.net/manual/en/function.xmlwriter-set-indent.php
	 * @param indent bool
	 * @return bool 
	 */
	public function setIndent ($indent) {}

	/**
	 * Set string used for indenting
	 * @link http://php.net/manual/en/function.xmlwriter-set-indent-string.php
	 * @param indentString string
	 * @return bool 
	 */
	public function setIndentString ($indentString) {}

	/**
	 * Create start comment
	 * @link http://php.net/manual/en/function.xmlwriter-start-comment.php
	 * @return bool 
	 */
	public function startComment () {}

	/**
	 * Create end comment
	 * @link http://php.net/manual/en/function.xmlwriter-end-comment.php
	 * @return bool 
	 */
	public function endComment () {}

	/**
	 * Create start attribute
	 * @link http://php.net/manual/en/function.xmlwriter-start-attribute.php
	 * @param name string
	 * @return bool 
	 */
	public function startAttribute ($name) {}

	/**
	 * End attribute
	 * @link http://php.net/manual/en/function.xmlwriter-end-attribute.php
	 * @return bool 
	 */
	public function endAttribute () {}

	/**
	 * Write full attribute
	 * @link http://php.net/manual/en/function.xmlwriter-write-attribute.php
	 * @param name string
	 * @param value string
	 * @return bool 
	 */
	public function writeAttribute ($name, $value) {}

	/**
	 * Create start namespaced attribute
	 * @link http://php.net/manual/en/function.xmlwriter-start-attribute-ns.php
	 * @param prefix string
	 * @param name string
	 * @param uri string
	 * @return bool 
	 */
	public function startAttributeNs ($prefix, $name, $uri) {}

	/**
	 * Write full namespaced attribute
	 * @link http://php.net/manual/en/function.xmlwriter-write-attribute-ns.php
	 * @param prefix string
	 * @param name string
	 * @param uri string
	 * @param content string
	 * @return bool 
	 */
	public function writeAttributeNs ($prefix, $name, $uri, $content) {}

	/**
	 * Create start element tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-element.php
	 * @param name string
	 * @return bool 
	 */
	public function startElement ($name) {}

	/**
	 * End current element
	 * @link http://php.net/manual/en/function.xmlwriter-end-element.php
	 * @return bool 
	 */
	public function endElement () {}

	/**
	 * End current element
	 * @link http://php.net/manual/en/function.xmlwriter-full-end-element.php
	 * @return bool 
	 */
	public function fullEndElement () {}

	/**
	 * Create start namespaced element tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-element-ns.php
	 * @param prefix string
	 * @param name string
	 * @param uri string
	 * @return bool 
	 */
	public function startElementNs ($prefix, $name, $uri) {}

	/**
	 * Write full element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-element.php
	 * @param name string
	 * @param content string[optional]
	 * @return bool 
	 */
	public function writeElement ($name, $content = null) {}

	/**
	 * Write full namesapced element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-element-ns.php
	 * @param prefix string
	 * @param name string
	 * @param uri string
	 * @param content string[optional]
	 * @return bool 
	 */
	public function writeElementNs ($prefix, $name, $uri, $content = null) {}

	/**
	 * Create start PI tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-pi.php
	 * @param target string
	 * @return bool 
	 */
	public function startPi ($target) {}

	/**
	 * End current PI
	 * @link http://php.net/manual/en/function.xmlwriter-end-pi.php
	 * @return bool 
	 */
	public function endPi () {}

	/**
	 * Writes a PI
	 * @link http://php.net/manual/en/function.xmlwriter-write-pi.php
	 * @param target string
	 * @param content string
	 * @return bool 
	 */
	public function writePi ($target, $content) {}

	/**
	 * Create start CDATA tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-cdata.php
	 * @return bool 
	 */
	public function startCdata () {}

	/**
	 * End current CDATA
	 * @link http://php.net/manual/en/function.xmlwriter-end-cdata.php
	 * @return bool 
	 */
	public function endCdata () {}

	/**
	 * Write full CDATA tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-cdata.php
	 * @param content string
	 * @return bool 
	 */
	public function writeCdata ($content) {}

	/**
	 * Write text
	 * @link http://php.net/manual/en/function.xmlwriter-text.php
	 * @param content string
	 * @return bool 
	 */
	public function text ($content) {}

	/**
	 * Write a raw XML text
	 * @link http://php.net/manual/en/function.xmlwriter-write-raw.php
	 * @param content string
	 * @return bool 
	 */
	public function writeRaw ($content) {}

	/**
	 * Create document tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-document.php
	 * @param version string[optional]
	 * @param encoding string[optional]
	 * @param standalone string[optional]
	 * @return bool 
	 */
	public function startDocument ($version = null, $encoding = null, $standalone = null) {}

	/**
	 * End current document
	 * @link http://php.net/manual/en/function.xmlwriter-end-document.php
	 * @return bool 
	 */
	public function endDocument () {}

	/**
	 * Write full comment tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-comment.php
	 * @param content string
	 * @return bool 
	 */
	public function writeComment ($content) {}

	/**
	 * Create start DTD tag
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd.php
	 * @param qualifiedName string
	 * @param publicId string[optional]
	 * @param systemId string[optional]
	 * @return bool 
	 */
	public function startDtd ($qualifiedName, $publicId = null, $systemId = null) {}

	/**
	 * End current DTD
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd.php
	 * @return bool 
	 */
	public function endDtd () {}

	/**
	 * Write full DTD tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd.php
	 * @param name string
	 * @param publicId string[optional]
	 * @param systemId string[optional]
	 * @param subset string[optional]
	 * @return bool 
	 */
	public function writeDtd ($name, $publicId = null, $systemId = null, $subset = null) {}

	/**
	 * Create start DTD element
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-element.php
	 * @param qualifiedName string
	 * @return bool 
	 */
	public function startDtdElement ($qualifiedName) {}

	/**
	 * End current DTD element
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-element.php
	 * @return bool 
	 */
	public function endDtdElement () {}

	/**
	 * Write full DTD element tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-element.php
	 * @param name string
	 * @param content string
	 * @return bool 
	 */
	public function writeDtdElement ($name, $content) {}

	/**
	 * Create start DTD AttList
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-attlist.php
	 * @param name string
	 * @return bool 
	 */
	public function startDtdAttlist ($name) {}

	/**
	 * End current DTD AttList
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-attlist.php
	 * @return bool 
	 */
	public function endDtdAttlist () {}

	/**
	 * Write full DTD AttList tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-attlist.php
	 * @param name string
	 * @param content string
	 * @return bool 
	 */
	public function writeDtdAttlist ($name, $content) {}

	/**
	 * Create start DTD Entity
	 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-entity.php
	 * @param name string
	 * @param isparam bool
	 * @return bool 
	 */
	public function startDtdEntity ($name, $isparam) {}

	/**
	 * End current DTD Entity
	 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-entity.php
	 * @return bool 
	 */
	public function endDtdEntity () {}

	/**
	 * Write full DTD Entity tag
	 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-entity.php
	 * @param name string
	 * @param content string
	 * @return bool 
	 */
	public function writeDtdEntity ($name, $content) {}

	/**
	 * Returns current buffer
	 * @link http://php.net/manual/en/function.xmlwriter-output-memory.php
	 * @param flush bool[optional]
	 * @return bool the current buffer as a string.
	 */
	public function outputMemory ($flush = null) {}

	/**
	 * Flush current buffer
	 * @link http://php.net/manual/en/function.xmlwriter-flush.php
	 * @param empty bool[optional]
	 * @return mixed 
	 */
	public function flush ($empty = null) {}

}

/**
 * Create new xmlwriter using source uri for output
 * @link http://php.net/manual/en/function.xmlwriter-open-uri.php
 * @param uri string
 * @return bool 
 */
function xmlwriter_open_uri ($uri) {}

/**
 * Create new xmlwriter using memory for string output
 * @link http://php.net/manual/en/function.xmlwriter-open-memory.php
 * @return bool 
 */
function xmlwriter_open_memory () {}

/**
 * Toggle indentation on/off
 * @link http://php.net/manual/en/function.xmlwriter-set-indent.php
 * @param indent bool
 * @return bool 
 */
function xmlwriter_set_indent ($indent) {}

/**
 * Set string used for indenting
 * @link http://php.net/manual/en/function.xmlwriter-set-indent-string.php
 * @param indentString string
 * @return bool 
 */
function xmlwriter_set_indent_string ($indentString) {}

/**
 * Create start comment
 * @link http://php.net/manual/en/function.xmlwriter-start-comment.php
 * @return bool 
 */
function xmlwriter_start_comment () {}

/**
 * Create end comment
 * @link http://php.net/manual/en/function.xmlwriter-end-comment.php
 * @return bool 
 */
function xmlwriter_end_comment () {}

/**
 * Create start attribute
 * @link http://php.net/manual/en/function.xmlwriter-start-attribute.php
 * @param name string
 * @return bool 
 */
function xmlwriter_start_attribute ($name) {}

/**
 * End attribute
 * @link http://php.net/manual/en/function.xmlwriter-end-attribute.php
 * @return bool 
 */
function xmlwriter_end_attribute () {}

/**
 * Write full attribute
 * @link http://php.net/manual/en/function.xmlwriter-write-attribute.php
 * @param name string
 * @param value string
 * @return bool 
 */
function xmlwriter_write_attribute ($name, $value) {}

/**
 * Create start namespaced attribute
 * @link http://php.net/manual/en/function.xmlwriter-start-attribute-ns.php
 * @param prefix string
 * @param name string
 * @param uri string
 * @return bool 
 */
function xmlwriter_start_attribute_ns ($prefix, $name, $uri) {}

/**
 * Write full namespaced attribute
 * @link http://php.net/manual/en/function.xmlwriter-write-attribute-ns.php
 * @param prefix string
 * @param name string
 * @param uri string
 * @param content string
 * @return bool 
 */
function xmlwriter_write_attribute_ns ($prefix, $name, $uri, $content) {}

/**
 * Create start element tag
 * @link http://php.net/manual/en/function.xmlwriter-start-element.php
 * @param name string
 * @return bool 
 */
function xmlwriter_start_element ($name) {}

/**
 * End current element
 * @link http://php.net/manual/en/function.xmlwriter-end-element.php
 * @return bool 
 */
function xmlwriter_end_element () {}

/**
 * End current element
 * @link http://php.net/manual/en/function.xmlwriter-full-end-element.php
 * @return bool 
 */
function xmlwriter_full_end_element () {}

/**
 * Create start namespaced element tag
 * @link http://php.net/manual/en/function.xmlwriter-start-element-ns.php
 * @param prefix string
 * @param name string
 * @param uri string
 * @return bool 
 */
function xmlwriter_start_element_ns ($prefix, $name, $uri) {}

/**
 * Write full element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-element.php
 * @param name string
 * @param content string[optional]
 * @return bool 
 */
function xmlwriter_write_element ($name, $content = null) {}

/**
 * Write full namesapced element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-element-ns.php
 * @param prefix string
 * @param name string
 * @param uri string
 * @param content string[optional]
 * @return bool 
 */
function xmlwriter_write_element_ns ($prefix, $name, $uri, $content = null) {}

/**
 * Create start PI tag
 * @link http://php.net/manual/en/function.xmlwriter-start-pi.php
 * @param target string
 * @return bool 
 */
function xmlwriter_start_pi ($target) {}

/**
 * End current PI
 * @link http://php.net/manual/en/function.xmlwriter-end-pi.php
 * @return bool 
 */
function xmlwriter_end_pi () {}

/**
 * Writes a PI
 * @link http://php.net/manual/en/function.xmlwriter-write-pi.php
 * @param target string
 * @param content string
 * @return bool 
 */
function xmlwriter_write_pi ($target, $content) {}

/**
 * Create start CDATA tag
 * @link http://php.net/manual/en/function.xmlwriter-start-cdata.php
 * @return bool 
 */
function xmlwriter_start_cdata () {}

/**
 * End current CDATA
 * @link http://php.net/manual/en/function.xmlwriter-end-cdata.php
 * @return bool 
 */
function xmlwriter_end_cdata () {}

/**
 * Write full CDATA tag
 * @link http://php.net/manual/en/function.xmlwriter-write-cdata.php
 * @param content string
 * @return bool 
 */
function xmlwriter_write_cdata ($content) {}

/**
 * Write text
 * @link http://php.net/manual/en/function.xmlwriter-text.php
 * @param content string
 * @return bool 
 */
function xmlwriter_text ($content) {}

/**
 * Write a raw XML text
 * @link http://php.net/manual/en/function.xmlwriter-write-raw.php
 * @param content string
 * @return bool 
 */
function xmlwriter_write_raw ($content) {}

/**
 * Create document tag
 * @link http://php.net/manual/en/function.xmlwriter-start-document.php
 * @param version string[optional]
 * @param encoding string[optional]
 * @param standalone string[optional]
 * @return bool 
 */
function xmlwriter_start_document ($version = null, $encoding = null, $standalone = null) {}

/**
 * End current document
 * @link http://php.net/manual/en/function.xmlwriter-end-document.php
 * @return bool 
 */
function xmlwriter_end_document () {}

/**
 * Write full comment tag
 * @link http://php.net/manual/en/function.xmlwriter-write-comment.php
 * @param content string
 * @return bool 
 */
function xmlwriter_write_comment ($content) {}

/**
 * Create start DTD tag
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd.php
 * @param qualifiedName string
 * @param publicId string[optional]
 * @param systemId string[optional]
 * @return bool 
 */
function xmlwriter_start_dtd ($qualifiedName, $publicId = null, $systemId = null) {}

/**
 * End current DTD
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd.php
 * @return bool 
 */
function xmlwriter_end_dtd () {}

/**
 * Write full DTD tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd.php
 * @param name string
 * @param publicId string[optional]
 * @param systemId string[optional]
 * @param subset string[optional]
 * @return bool 
 */
function xmlwriter_write_dtd ($name, $publicId = null, $systemId = null, $subset = null) {}

/**
 * Create start DTD element
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-element.php
 * @param qualifiedName string
 * @return bool 
 */
function xmlwriter_start_dtd_element ($qualifiedName) {}

/**
 * End current DTD element
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-element.php
 * @return bool 
 */
function xmlwriter_end_dtd_element () {}

/**
 * Write full DTD element tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-element.php
 * @param name string
 * @param content string
 * @return bool 
 */
function xmlwriter_write_dtd_element ($name, $content) {}

/**
 * Create start DTD AttList
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-attlist.php
 * @param name string
 * @return bool 
 */
function xmlwriter_start_dtd_attlist ($name) {}

/**
 * End current DTD AttList
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-attlist.php
 * @return bool 
 */
function xmlwriter_end_dtd_attlist () {}

/**
 * Write full DTD AttList tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-attlist.php
 * @param name string
 * @param content string
 * @return bool 
 */
function xmlwriter_write_dtd_attlist ($name, $content) {}

/**
 * Create start DTD Entity
 * @link http://php.net/manual/en/function.xmlwriter-start-dtd-entity.php
 * @param name string
 * @param isparam bool
 * @return bool 
 */
function xmlwriter_start_dtd_entity ($name, $isparam) {}

/**
 * End current DTD Entity
 * @link http://php.net/manual/en/function.xmlwriter-end-dtd-entity.php
 * @return bool 
 */
function xmlwriter_end_dtd_entity () {}

/**
 * Write full DTD Entity tag
 * @link http://php.net/manual/en/function.xmlwriter-write-dtd-entity.php
 * @param name string
 * @param content string
 * @return bool 
 */
function xmlwriter_write_dtd_entity ($name, $content) {}

/**
 * Returns current buffer
 * @link http://php.net/manual/en/function.xmlwriter-output-memory.php
 * @param flush bool[optional]
 * @return bool the current buffer as a string.
 */
function xmlwriter_output_memory ($flush = null) {}

/**
 * Flush current buffer
 * @link http://php.net/manual/en/function.xmlwriter-flush.php
 * @param empty bool[optional]
 * @return mixed 
 */
function xmlwriter_flush ($empty = null) {}

// End of xmlwriter v.0.1

/**
 * Returns the number of arguments passed to the function
 * @link http://php.net/manual/en/function.func-num-args.php
 * @return int the number of arguments passed into the current user-defined
 */
function func_num_args () {}

/**
 * Return an item from the argument list
 * @link http://php.net/manual/en/function.func-get-arg.php
 * @param arg_num int
 * @return mixed the specified argument, or false on error.
 */
function func_get_arg ($arg_num) {}

/**
 * Returns an array comprising a function's argument list
 * @link http://php.net/manual/en/function.func-get-args.php
 * @return array an array in which each element is a copy of the corresponding
 */
function func_get_args () {}

/**
 * Get string length
 * @link http://php.net/manual/en/function.strlen.php
 * @param string string
 * @return int 
 */
function strlen ($string) {}

/**
 * Binary safe string comparison
 * @link http://php.net/manual/en/function.strcmp.php
 * @param str1 string
 * @param str2 string
 * @return int &lt; 0 if str1 is less than
 */
function strcmp ($str1, $str2) {}

/**
 * Binary safe string comparison of the first n characters
 * @link http://php.net/manual/en/function.strncmp.php
 * @param str1 string
 * @param str2 string
 * @param len int
 * @return int &lt; 0 if str1 is less than
 */
function strncmp ($str1, $str2, $len) {}

/**
 * Binary safe case-insensitive string comparison
 * @link http://php.net/manual/en/function.strcasecmp.php
 * @param str1 string
 * @param str2 string
 * @return int &lt; 0 if str1 is less than
 */
function strcasecmp ($str1, $str2) {}

/**
 * Binary safe case-insensitive string comparison of the first n characters
 * @link http://php.net/manual/en/function.strncasecmp.php
 * @param str1 string
 * @param str2 string
 * @param len int
 * @return int &lt; 0 if str1 is less than
 */
function strncasecmp ($str1, $str2, $len) {}

/**
 * Return the current key and value pair from an array and advance the array cursor
 * @link http://php.net/manual/en/function.each.php
 * @param var1
 */
function each (&$var1) {}

/**
 * Sets which PHP errors are reported
 * @link http://php.net/manual/en/function.error-reporting.php
 * @param level int[optional]
 * @return int the old error_reporting
 */
function error_reporting ($level = null) {}

/**
 * Defines a named constant
 * @link http://php.net/manual/en/function.define.php
 * @param name string
 * @param value mixed
 * @param case_insensitive bool[optional]
 * @return bool 
 */
function define ($name, $value, $case_insensitive = null) {}

/**
 * Checks whether a given named constant exists
 * @link http://php.net/manual/en/function.defined.php
 * @param name string
 * @return bool true if the named constant given by name
 */
function defined ($name) {}

/**
 * Returns the name of the class of an object
 * @link http://php.net/manual/en/function.get-class.php
 * @param object object[optional]
 * @return string the name of the class of which object is an
 */
function get_class ($object = null) {}

/**
 * Retrieves the parent class name for object or class
 * @link http://php.net/manual/en/function.get-parent-class.php
 * @param object mixed[optional]
 * @return string the name of the parent class of the class of which
 */
function get_parent_class ($object = null) {}

/**
 * Checks if the class method exists
 * @link http://php.net/manual/en/function.method-exists.php
 * @param object object
 * @param method_name string
 * @return bool true if the method given by method_name
 */
function method_exists ($object, $method_name) {}

/**
 * Checks if the object or class has a property
 * @link http://php.net/manual/en/function.property-exists.php
 * @param class mixed
 * @param property string
 * @return bool true if the property exists, false if it doesn't exist or
 */
function property_exists ($class, $property) {}

/**
 * Checks if the class has been defined
 * @link http://php.net/manual/en/function.class-exists.php
 * @param class_name string
 * @param autoload bool[optional]
 * @return bool true if class_name is a defined class,
 */
function class_exists ($class_name, $autoload = null) {}

/**
 * Checks if the interface has been defined
 * @link http://php.net/manual/en/function.interface-exists.php
 * @param interface_name string
 * @param autoload bool[optional]
 * @return bool true if the interface given by
 */
function interface_exists ($interface_name, $autoload = null) {}

/**
 * Return &true; if the given function has been defined
 * @link http://php.net/manual/en/function.function-exists.php
 * @param function_name string
 * @return bool true if function_name exists and is a
 */
function function_exists ($function_name) {}

/**
 * Returns an array with the names of included or required files
 * @link http://php.net/manual/en/function.get-included-files.php
 * @return array an array of the names of all files.
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
 * @param object mixed
 * @param class_name string
 * @return bool 
 */
function is_subclass_of ($object, $class_name) {}

/**
 * Checks if the object is of this class or has this class as one of its parents
 * @link http://php.net/manual/en/function.is-a.php
 * @param object object
 * @param class_name string
 * @return bool true if the object is of this class or has this class as one of
 */
function is_a ($object, $class_name) {}

/**
 * Get the default properties of the class
 * @link http://php.net/manual/en/function.get-class-vars.php
 * @param class_name string
 * @return array an associative array of default public properties of the class.
 */
function get_class_vars ($class_name) {}

/**
 * Gets the properties of the given object
 * @link http://php.net/manual/en/function.get-object-vars.php
 * @param object object
 * @return array an associative array of defined object properties for the
 */
function get_object_vars ($object) {}

/**
 * Gets the class methods' names
 * @link http://php.net/manual/en/function.get-class-methods.php
 * @param class_name mixed
 * @return array an array of method names defined for the class specified by
 */
function get_class_methods ($class_name) {}

/**
 * Generates a user-level error/warning/notice message
 * @link http://php.net/manual/en/function.trigger-error.php
 * @param error_msg string
 * @param error_type int[optional]
 * @return bool 
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
 * @param error_handler callback
 * @param error_types int[optional]
 * @return mixed a string containing the previously defined
 */
function set_error_handler ($error_handler, $error_types = null) {}

/**
 * Restores the previous error handler function
 * @link http://php.net/manual/en/function.restore-error-handler.php
 * @return bool 
 */
function restore_error_handler () {}

/**
 * Sets a user-defined exception handler function
 * @link http://php.net/manual/en/function.set-exception-handler.php
 * @param exception_handler callback
 * @return string the name of the previously defined exception handler, or &null; on error. If
 */
function set_exception_handler ($exception_handler) {}

/**
 * Restores the previously defined exception handler function
 * @link http://php.net/manual/en/function.restore-exception-handler.php
 * @return bool 
 */
function restore_exception_handler () {}

/**
 * Returns an array with the name of the defined classes
 * @link http://php.net/manual/en/function.get-declared-classes.php
 * @return array an array of the names of the declared classes in the current
 */
function get_declared_classes () {}

/**
 * Returns an array of all declared interfaces
 * @link http://php.net/manual/en/function.get-declared-interfaces.php
 * @return array an array of the names of the declared interfaces in the current
 */
function get_declared_interfaces () {}

/**
 * Returns an array of all defined functions
 * @link http://php.net/manual/en/function.get-defined-functions.php
 * @return array an multidimensional array containing a list of all defined
 */
function get_defined_functions () {}

/**
 * Returns an array of all defined variables
 * @link http://php.net/manual/en/function.get-defined-vars.php
 * @return array 
 */
function get_defined_vars () {}

/**
 * Create an anonymous (lambda-style) function
 * @link http://php.net/manual/en/function.create-function.php
 * @param args string
 * @param code string
 * @return string a unique function name as a string, or false on error.
 */
function create_function ($args, $code) {}

/**
 * Returns the resource type
 * @link http://php.net/manual/en/function.get-resource-type.php
 * @param handle resource
 * @return string 
 */
function get_resource_type ($handle) {}

/**
 * Returns an array with the names of all modules compiled and loaded
 * @link http://php.net/manual/en/function.get-loaded-extensions.php
 * @return array an indexed array of all the modules names.
 */
function get_loaded_extensions () {}

/**
 * Find out whether an extension is loaded
 * @link http://php.net/manual/en/function.extension-loaded.php
 * @param name string
 * @return bool true if the extension identified by name
 */
function extension_loaded ($name) {}

/**
 * Returns an array with the names of the functions of a module
 * @link http://php.net/manual/en/function.get-extension-funcs.php
 * @param module_name string
 * @return array an array with all the functions, or false if
 */
function get_extension_funcs ($module_name) {}

/**
 * Returns an associative array with the names of all the constants and their values
 * @link http://php.net/manual/en/function.get-defined-constants.php
 * @param categorize mixed[optional]
 * @return array 
 */
function get_defined_constants ($categorize = null) {}

/**
 * Generates a backtrace
 * @link http://php.net/manual/en/function.debug-backtrace.php
 * @return array an associative array. The possible returned elements
 */
function debug_backtrace () {}

/**
 * Prints a backtrace
 * @link http://php.net/manual/en/function.debug-print-backtrace.php
 * @return void 
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
define ('TRUE', true);
define ('FALSE', false);
define ('NULL', null);
define ('ZEND_THREAD_SAFE', false);
define ('PHP_VERSION', "5.2.4");
define ('PHP_OS', "Linux");
define ('PHP_SAPI', "cli");

?>