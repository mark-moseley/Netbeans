/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */


var redirect_delay = 1000;

var lang_id="";
var option_id="";
var platform_id="";
var url = "";
var filename = "";	    
var string = "";
var parent_folder = "";

function initialize() {
	    string = "" + window.location;	    
            var query    = string.substring(string.indexOf("?") + 1, string.length);
	    var sep = "&";	    
	    var email = "";
            var monthly = "0";
	    var weekly  = "0";
	    var contact = "0";
	    var email_sep    = "email=";
            var monthly_sep  = "monthly=";
            var weekly_sep   = "weekly=";
            var contact_sep  = "contact=";
            var start_page_string = (string.indexOf("?")==-1) ? string : string.substring(0, string.indexOf("?"));            
            parent_folder  = start_page_string.substring(0, start_page_string.lastIndexOf("/") + 1);

	    if(query!="" && query != string/* && query.indexOf(sep)!=-1*/)  {
		    while(query!="") {
		            var lang_sep     = "lang=";
			    var platform_sep = "platform=";
			    var option_sep   = "option=";
                            var filename_sep = "filename=";
                            

			    if(query.indexOf(lang_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					lang_id = query.substring(lang_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
        	        	 } else {	
					lang_id = query.substring(lang_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(platform_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					platform_id = query.substring(platform_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					platform_id = query.substring(platform_sep.length, query.length);
					query = "";
				 }		 
			   } else if(query.indexOf(option_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					option_id = query.substring(option_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					option_id = query.substring(option_sep.length, query.length);
					query = "";
				 }		 
		           } else if(query.indexOf(email_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					email = query.substring(email_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					email = query.substring(email_sep.length, query.length);
					query = "";
				 }		 
		           } else if(query.indexOf(monthly_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					monthly = query.substring(monthly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					monthly = query.substring(monthly_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(weekly_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					weekly = query.substring(weekly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					weekly = query.substring(weekly_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(contact_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					contact = query.substring(contact_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					contact = query.substring(contact_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(filename_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					filename = query.substring(filename_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					filename = query.substring(filename_sep.length, query.length);
					query = "";
				 }
		            } else {
				query = "";
			    }
	            }
		    if(email!="") {			
			var phpRequest = SUBSCRIPTION_PHP_URL;
			phpRequest += "?" + email_sep   + email;
			phpRequest += "&" + monthly_sep + monthly;
			phpRequest += "&" + weekly_sep  + weekly;
			phpRequest += "&" + contact_sep + contact;
			phpRequest += "&timestamp=" + new Date().getTime();
			var image = new Image();
			image.src = phpRequest;
			image.style.display="none";
		    } 
	            if(filename!="") {
			for(var i=0;i<PLATFORM_IDS.length;i++) {
			    for(var j=0;j<BUNDLE_IDS.length;j++) {
				var testFileName = get_file_name(PLATFORM_IDS[i], BUNDLE_IDS[j]);
				
				if(testFileName==filename) {
				    platform_id = PLATFORM_IDS[i];
				    option_id   = BUNDLE_IDS[j];
				    lang_id     = get_language(LANGUAGE_IDS);
				    if(lang_id=="") lang_id = "en";
				    i = PLATFORM_IDS.length;
				    j = BUNDLE_IDS.length;	
				    filename = "";
                                }
                            }
                        }
		    }

		    if(option_id != "" && platform_id != "") {
	    	        if (USE_BOUNCER == 1) {
                            url      = get_file_bouncer_url(platform_id, option_id);
                        } else {
                            url      = get_file_url(get_file_name(platform_id, option_id));
		        }
                        filename     = get_file_name(platform_id, option_id);
		    } else if(filename!="") {
	    	        ADDITIONAL_BUNDLES = new Array();
			ADDITIONAL_BUNDLES[0] = "javadoc";
			ADDITIONAL_BUNDLES[1] = "src";
			ADDITIONAL_BUNDLES[2] = "platform-src";

			if (USE_BOUNCER == 1) {
                	    for(var i=0;i<ADDITIONAL_BUNDLES.length;i++) {
			        var testFileName = get_file_name("zip", ADDITIONAL_BUNDLES[i]);
			            if(testFileName==filename) {
				        url      = get_file_bouncer_url("zip", ADDITIONAL_BUNDLES[i]);
					break;
				    }
                            }
			}

			if (url == "") {
			    USE_BOUNCER = 0;
			    url      = get_file_url(filename);
			}
		    }

            	    window.onload = delayedredirect;
            }
}

function delayedredirect() {
     setTimeout("redirect()",redirect_delay);
}
function redirect() {
     window.location = url;
}

function write_download_header() {
	document.write('<p>');
	document.write(AUTOMATIC_DOWNLOAD_MESSAGE.replace('{0}',url));
	document.write('</p>');
}

function getMD5(name) {
	var md5 = "";
        for (var i = 0; i < file_names.length; i++) {		
		if(file_names[i] == filename) {		
			md5 = file_md5s[i];
			break;
		}
        }
	return md5;
}

function write_download_info() {
	var size = getSize(filename);
	var md5 = getMD5(filename);		
	var platform_display_name = getPlatformShortName(platform_id);
	var lang_display_name     = getLanguageName(lang_id);
        var option_display_name   = getBundleShortName(option_id);

	
	/* format size */
	mb = Math.floor(size / (1024 * 1024));
	mb_dec = Math.floor((size - (mb * 1024 * 1024))/ (1024 * 102));		
        size = mb + ((mb_dec>0) ? ('.' + mb_dec) : '');

	document.write('<br>');
        document.write('<p class="file_information">');

        var info = "";
	if (platform_display_name!="" && lang_display_name!="" && filename!="") {
		 info = INFO_MESSAGE.
				replace('{0}', PRODUCT_NAME.replace('{0}',BUILD_DISPLAY_VERSION)).
		 		replace('{1}', ((option_display_name != "") ? (' ' + option_display_name) : '')).
		 		replace('{2}', ((platform_id == 'zip') ? (platform_display_name) : (INSTALLER_MESSAGE.replace('{0}',platform_display_name)))).
		 		replace('{3}', lang_display_name).
		 		replace('{4}', lang_id).
		 		replace('{5}', get_file_name_short(platform_id,option_id)).
				replace('{6}', size).
		 		replace('{7}', md5);
    	} else if(filename!="") {
		var filename_short = filename.substring(filename.lastIndexOf("/") + 1, filename.length);
		info = INFO_MESSAGE_OTHER.
		 		replace('{0}', filename_short).
				replace('{1}', size).
		 		replace('{2}', md5);
	} else {
		info = NOFILE_MESSAGE;
	}
        document.write(info);
	document.write('</p>');
}
