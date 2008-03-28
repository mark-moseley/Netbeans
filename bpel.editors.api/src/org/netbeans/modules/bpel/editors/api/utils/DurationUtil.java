/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.editors.api.utils;

import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.14
 */
public final class DurationUtil {

  private DurationUtil() {}

  public static Duration parseDuration(String value, boolean throwException) {
//out();
//out("PARSE duration: " + value);
    boolean hasMinus = false;
    int years = 0;
    int months = 0;
    int days = 0;
    int hours = 0;
    int minutes = 0;
    double seconds = 0.0;

    value = removeQuotes(value);

    if (value == null || value.length() == 0) {
      return throwException("Value is empty.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    int k;
    boolean wasDesignator = false;
    boolean wasDesignatorT = false;

    // minus
    if (value.charAt(0) == MINUS.charAt(0)) {
      hasMinus = true;
      value = value.substring(1);
    }
    // P
    if (value.charAt(0) != P_DELIM.charAt(0)) {
      return throwException("There is no 'P' symbol.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    value = value.substring(1);

    // years 
    k = value.indexOf(Y_DELIM);
   
    if (k != -1) {
      wasDesignator = true;
      years = parseInt(value.substring(0, k));
       
      if (years < 0) {
        return throwException("Error in years.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // months 
    k = value.indexOf(M_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      months = parseInt(value.substring(0, k));
        
      if (months < 0) {
        return throwException("Error in months.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // days 
    k = value.indexOf(D_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      days = parseInt(value.substring(0, k));
        
      if (days < 0) {
        return throwException("Error in days.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    if (value.length() == 0) {
      return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
    }
    // T
    if (value.charAt(0) != T_DELIM.charAt(0)) {
      return throwException("Symbol 'T' is expected.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    value = value.substring(1);

    // hours 
    k = value.indexOf(H_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      hours = parseInt(value.substring(0, k));
        
      if (hours < 0) {
        return throwException("Error in hours.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // minutes 
    k = value.indexOf(M_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      minutes = parseInt(value.substring(0, k));
        
      if (minutes < 0) {
        return throwException("Error in minutes.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // seconds 
    k = value.indexOf(S_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      seconds = parseDouble(value.substring(0, k));
        
      if (seconds < 0) {
        return throwException("Error in seconds.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    if ( !wasDesignatorT) {
      return throwException("The designator 'T' must be absent if and only if all of the time items are absent.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    if ( !wasDesignator) {
      return throwException("At least one number and its designator must be present.", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
//out("CREATE DURATION: " + seconds);
    return throwException(null, false, hasMinus, years, months, days, hours, minutes, seconds);
  }

  private static Duration throwException(String message, boolean throwException, 
    boolean hasMinus,
    int years,
    int months,
    int days,
    int hours,
    int minutes,
    double seconds)
  {
//if (message != null) out("EXCEPTION: " + message);
    if (throwException) {
      throw new IllegalArgumentException(message);
    }
    return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
  }

  public static String addQuotes(String value) {
    return QUOTE + value + QUOTE;
  }

  public static String removeQuotes(String value) {
    if (value == null) {
      return null;
    }
    if (value.startsWith(DurationUtil.QUOTE)) {
      value = value.substring(1);
    }
    if (value.endsWith(DurationUtil.QUOTE)) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }

  public static String getContent(
    boolean isFor,
    int year,
    int month,
    int day,
    int hour,
    int minute,
    double second)
  {
    if (isFor) {
      StringBuffer content = new StringBuffer();
      content.append(P_DELIM);
      content.append(getStr(year));
      content.append(Y_DELIM);
      content.append(getStr(month));
      content.append(M_DELIM);
      content.append(getStr(day));
      content.append(D_DELIM);
      content.append(T_DELIM);
      content.append(getStr(hour));
      content.append(H_DELIM);
      content.append(getStr(minute));
      content.append(M_DELIM);
      content.append(getStr(second));
      content.append(S_DELIM);
      return content.toString();
    }
    else {
      return getParseUntil(
        getString(year),
        getString(month),
        getString(day),
        getString(hour),
        getString(minute),
        getStr(second));
    }
  }

  public static String getParseUntil(
    String year,
    String month,
    String day,
    String hour,
    String minute,
    String second)
  {
    StringBuffer content = new StringBuffer();
    content.append(year);
    content.append(MINUS);
    content.append(month);
    content.append(MINUS);
    content.append(day);
    content.append(T_DELIM);
    content.append(hour);
    content.append(COLON);
    content.append(minute);
    content.append(COLON);
    content.append(second);
    return content.toString();
  }

  public static int parseInt(String value) {
    return getInt(value);
  }

  public static double parseDouble(String value) {
    return getDouble(value);
  }

  private static String getString(int value) {
    if (0 <= value && value <= NINE) {
      return ZERO + value;
    }
    return getStr(value);
  }

  private static String getStr(int value) {
    return EMPTY + value;
  }
  
  private static String getStr(double value) {
    return EMPTY + value;
  }
  
  private static final int NINE = 9;

  private static final String EMPTY   =  ""; // NOI18N
  private static final String ZERO    = "0"; // NOI18N
  private static final String MINUS   = "-"; // NOI18N
  private static final String COLON   = ":"; // NOI18N
  private static final String QUOTE   = "'"; // NOI18N

  private static final String D_DELIM = "D"; // NOI18N
  private static final String H_DELIM = "H"; // NOI18N
  private static final String M_DELIM = "M"; // NOI18N
  private static final String P_DELIM = "P"; // NOI18N
  private static final String S_DELIM = "S"; // NOI18N
  private static final String Y_DELIM = "Y"; // NOI18N
  public  static final String T_DELIM = "T"; // NOI18N
}
