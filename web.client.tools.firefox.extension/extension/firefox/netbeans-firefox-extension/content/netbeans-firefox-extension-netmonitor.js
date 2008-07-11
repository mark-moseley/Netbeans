/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.NetBeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


(function() {
    const ignoreThese = /about:|javascript:|resource:|chrome:|jar:/;
    const DEBUG = false;

    //Should we move this to constants.js?
    const STATE_IS_WINDOW = NetBeans.Constants.WebProgressListenerIF.STATE_IS_WINDOW;
    const STATE_IS_DOCUMENT = NetBeans.Constants.WebProgressListenerIF.STATE_IS_DOCUMENT;
    const STATE_IS_NETWORK = NetBeans.Constants.WebProgressListenerIF.STATE_IS_NETWORK;
    const STATE_IS_REQUEST = NetBeans.Constants.WebProgressListenerIF.STATE_IS_REQUEST;

    const STATE_START = NetBeans.Constants.WebProgressListenerIF.STATE_START;
    const STATE_STOP = NetBeans.Constants.WebProgressListenerIF.STATE_STOP;
    const STATE_TRANSFERRING = NetBeans.Constants.WebProgressListenerIF.STATE_TRANSFERRING;

    const SEEK_SET = NetBeans.Constants.SeakableStreamIF.NS_SEEK_SET;



    const observerService = NetBeans.Utils.CCSV(
        NetBeans.Constants.ObserverServiceCID,
        NetBeans.Constants.ObserverServiceIF);

    const NOTIFY_ALL= NetBeans.Constants.WebProgressIF.NOTIFY_ALL;

    const mimeExtensionMap =
    {
        "txt": "text/plain",
        "html": "text/html",
        "htm": "text/html",
        "xhtml": "text/html",
        "xml": "text/xml",
        "css": "text/css",
        "js": "application/x-javascript",
        "jss": "application/x-javascript",
        "jpg": "image/jpeg",
        "jpeg": "image/jpeg",
        "gif": "image/gif",
        "png": "image/png",
        "bmp": "image/bmp",
        "swf": "application/x-shockwave-flash"
    };

    const mimeCategoryMap =
    {
        "text/plain": "txt",
        "application/octet-stream": "bin",
        "text/html": "html",
        "text/xml": "html",
        "text/css": "css",
        "application/x-javascript": "js",
        "text/javascript": "js",
        "application/javascript" : "js",
        "image/jpeg": "image",
        "image/gif": "image",
        "image/png": "image",
        "image/bmp": "image",
        "application/x-shockwave-flash": "flash"
    };



    var netFeatures = {
        netFilterCategory: null,
        disableNetMonitor: false,
        collectHttpHeaders: false
    };
    var socket;
    var requestsId = {};
    var requests = [];
    var topWindow;
    var myContext;
    this.initMonitor = function  (context, browser, _socket) {
        myContext = context;
        topWindow = context.window;
        if( !netFeatures.disableNetMonitor ){
            monitorContext(context, browser);
            if( !_socket )
                NetBeans.Logger.log("net.initMonitor - Socket is null");
            socket = _socket;
        }
    }

    this.destroyMonitor = function(context, browser) {
        myContext = null;
        if (context.networkListener) {
            if ( DEBUG ) NetBeans.Logger.log("net.destroyMonitor - Unmonitor request by removing listeners.")
            unmonitorContext(context, browser);
            socket = null;
            topWindow = null;
        }

    }

    var NetObserver =
    {
        QueryInterface: function(iid)
        {
            if( iid.equals(NetBeans.Constants.SupportsIF) ||
                iid.equals(NetBeans.Constants.ObserverIF))
                {
                return this;
            }

            throw NetBeans.Constants.NS_NOINTERFACE;
        },

        // nsIObserver
        //@type {nsIHttpChannel} channel
        observe: function(aNsISupport, topic, data)
        {

            if (topic == "http-on-modify-request") {
                this.onModifyRequest(aNsISupport);
            } else if (topic == "http-on-examine-response") {
                this.onExamineResponse(aNsISupport);
            } else {
              if (DEBUG) { NetBeans.Logger.log("netmonitor.observer: topic is: " + topic);}
            }

        },
        /*
         * @param {nsISupport} aNsISupport
         * @type {nsIHttpChannel} request
         * @type {NetActivity} activity
         */
        onModifyRequest: function (aNsISupport) {
            var DEBUG_METHOD = false & DEBUG;
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);
            /*
             *Joelle: You need to store this in the requestID probably as an nsIRequest rather than httpChannel
             *http://people.mozilla.com/~axel/doxygen/html/interfacensIRequest.html
             **/

              if ( isRelevantWindow(request) ){
                if (DEBUG_METHOD) { NetBeans.Logger.log("netmonitor.onModifyRequest:" + request.URI.asciiSpec);}
                requests.push(request);
                var id = uuid();
                var activity = getHttpRequestHeaders(request);
                activity.uuid = uuid();
                requestsId[requests.indexOf(request)] = activity.uuid;
                activity.time = nowTime();
                activity.url = request.URI.asciiSpec;
                activity.category = getRequestCategory(request);
                activity.load_init = request.loadFlags & request.LOAD_INITIAL_DOCUMENT_URI;

                //activity.postText = getPostTextFromRequest(request, myContext);
                if ( activity.method == "post" || activity.method == "POST") {
                  activity.postText = getPostText(activity, request, myContext);
                } else {
                  if (DEBUG_METHOD) NetBeans.Logger.log("netBeans.onModifyRequest - request.name:" + request.name);
                  activity.urlParams = parseURLParams(request.name);
                }
                //if (DEBUG) { NetBeans.Logger.log("netmonitor.sendNetActivity: about to send net activity." + request.URI.asciiSpec);}
                sendNetActivity(activity);
              } else if (DEBUG_METHOD)  {
                 NetBeans.Logger.log("netmonitor.onModifyRequest: IGNORING" + request.URI.asciiSpec);
                  if ( request.notificationCallbacks ){
                      NetBeans.Logger.log("netmonitor.onModifyRequest: NOTIFICATION CALLBACK DOES EXIST: notificationCallbacks:" + request.notificationCallbacks);
                  }
              }

           // }
        },

        onExamineResponse: function( aNsISupport ){
            var DEBUG_METHOD = false & DEBUG;
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);
            if (DEBUG_METHOD) { NetBeans.Logger.log("<-----  netmonitor.onExamineResponse: " + request.URI.asciiSpec);}

            var index = requests.indexOf(request);

            if(  index != -1 ){
                requests.pop(request);
                if (DEBUG_METHOD) { NetBeans.Logger.log("netmonitor.onExamineResponse: request is relevant" + request.URI.asciiSpec);}
                var activity = getHttpResponseHeaders(request);

                if ( activity ) {
                    activity.time = nowTime();
                    activity.uuid = requestsId[index];
                    activity.url = request.URI.asciiSpec;
                    activity.status = request.responseStatus;
                    if (!activity.mimeType && request.contentType) {
                      activity.mimeType = getMimeType(request.contentType, request.name);
                    }
                    requestsId[index]=null;
                    sendExamineNetResponse(activity);
                }
            }
        }


    }

    /*
     * isRelevantWindow - is the window a subclass of the window we are debugging?
     * @param {nsIHttpChannel} aRequest
     * @type {nsIDOMWindow} win
     * @return {bool}
     */
    function isRelevantWindow(aRequest) {
        var DEBUG_METHOD = false & DEBUG;

        var webProgress = getRequestWebProgress(aRequest);
        var win = null;
        if( !webProgress){
            if (DEBUG_METHOD) NetBeans.Logger.log("net.isRelevantWindow - Your webprogress value is no good.");
            return false;
        }

        win = safeGetWindow(webProgress)
        //var win = webProgress ? safeGetWindow(webProgress) : null;
        if( !win || !( win instanceof NetBeans.Constants.DOMWindowIF)){
            if( DEBUG_METHOD ) NetBeans.Logger.log("ERROR: net.isRelevantWindow - null or not a DOMWINDOW");
            return false;
        }


        if ( topWindow == win){
            return true;
        } else if ( !win.parent ) {
            if( DEBUG_METHOD ) NetBeans.Logger.log("net.isRelevantWindow - No parent to check.");
            return false;
        }
        if( DEBUG_METHOD ) NetBeans.Logger.log("net.isRelevantWindow - Checking if relevant to parent.");
        return isRelevantWindow(win.parent);

    //return ( topWindow == win || win.top == topWindow )
    }

    function NetProgressListener(context)
    {
        this.context = context;
    }

    NetProgressListener.prototype = {
        QueryInterface: function(iid)
        {
            if (iid.equals(NetBeans.Constants.WebProgressListenerIF)
                || iid.equals(NetBeans.Constants.SupportsWeakReferenceIF)
                || iid.equals(NetBeans.Constants.SupportsIF))
                {
                return this;
            }

            throw NetBeans.Constants.NS_NOINTERFACE;
        },
        //void onProgressChange ( nsIWebProgress webProgress , nsIRequest request , PRInt32 curSelfProgress , PRInt32 maxSelfProgress , PRInt32 curTotalProgress , PRInt32 maxTotalProgress )
        onProgressChange : function(progress, request, current, max, total, maxTotal )
        {
            if ( requests.indexOf(request) != -1){
                sendProgressUpdate(progress, request, current, max, total, maxTotal, nowTime());
            }
        },
        //void onLocationChange ( nsIWebProgress webProgress , nsIRequest request , nsIURI location )
        onLocationChange: function() {
        //NetBeans.Logger.log("On Location Change");
        },
        //void onSecurityChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 state )
        onSecurityChange : function() {
        //NetBeans.Logger.log("On Security Change");
        },
        //void onStatusChange ( nsIWebProgress webProgress , nsIRequest request , nsresult status , PRUnichar* message )
        onStatusChange : function() {
        //NetBeans.Logger.log("On Status Change");
        },
        //void onStateChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 stateFlags , nsresult status )
        onStateChange : function() {
        //NetBeans.Logger.log("On State Change");
        }
    }


    function monitorContext(aContext, browser)
    {

        if (!aContext.netProgressListener)
        {
            var netProgressListener = aContext.netProgressListener = new NetProgressListener(aContext);
            //Listening to the progress of the request
            browser.addProgressListener(netProgressListener, NOTIFY_ALL);

            observerService.addObserver(NetObserver, "http-on-modify-request", false);
            observerService.addObserver(NetObserver, "http-on-examine-response", false);
        }
    }

    // Maybe we should store browser inside context like firebug.
    function unmonitorContext(aContext,browser)
    {
        if (aContext.netProgressListener)
        {
            if (browser.docShell)
                browser.removeProgressListener(aContext.netProgressListener, NOTIFY_ALL);

            // XXXjoe We also want to do this when the context is hidden, so that
            // background files are only logged in the currently visible context
            observerService.removeObserver(NetObserver, "http-on-modify-request", false);
            observerService.removeObserver(NetObserver, "http-on-examine-response", false);

            delete aContext.netProgressListener;
        }
    }

    /*
     * @param {String} href
     * @return {String}
     */
    function parseURLParams( href ){

        if (!href){
            return "";
        }

        var hrefPieces = href.split("?");
        if ( hrefPieces.length != 2 ) {
            return null;
        }

        var searchString = hrefPieces[1];
        var nvPairs = searchString.split("&");
        return nvPairs;
    }

    function NetActivity (){
    }

    /*
     * @param {nsISupport} aRequest
     * @type {nsIHttpChannel} http
     * @return {NetActivity} activity
     */
    function getHttpResponseHeaders(aRequest)
    {
        //        if ( DEBUG ) {NetBeans.Logger.log("GetHttpResponseHeaders: ");  }
        var activity = new NetActivity();
        try
        {
            //var http = QI(request, nsIHttpChannel);
            var http = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
            activity.method = http.requestMethod;

            var responseHeaders = [];

            http.visitResponseHeaders({
                visitHeader: function(name, value)
                {
                    responseHeaders.push({
                        name: name,
                        value: value
                    });
                }
            });
            activity.responseHeaders = responseHeaders;
        }
        catch (exc)
        {
            NetBeans.Logger.log("netmonitor.getHttpResponseHeaders: exception" + exc);
            activity = null;
        } finally {
            return activity;
        }
    }


    /*
     * @param {nsISupport} aRequest
     * @type {nsIHttpChannel} http
     * @type {NetActivity} activity
     */
    function getHttpRequestHeaders( aRequest )
    {

        //        if( DEBUG ){ NetBeans.Logger.log("GetHttpRequestHeaders: "); }
        var activity = new NetActivity();
        try
        {
            //var http = QI(request, nsIHttpChannel);
            var http = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
            activity.method = http.requestMethod;
            //activity.status = aRequest.responseStatus;
            //activity.urlParams = parseURLParams(activity.href);

            //if (!activity.mimeType && aRequest.contentType )
            //     activity.mimeType = getMimeType(aRequest.contentType, aRequest.name);

            var requestHeaders = [];

            http.visitRequestHeaders({
                visitHeader: function(name, value)
                {
                    requestHeaders.push({
                        name: name,
                        value: value
                    });
                }
            });
            activity.requestHeaders = requestHeaders;
        }
        catch (exc) {
            NetBeans.Logger.log("netmonitor.getHttpRequestHeaders: exception" + exc);
            activity = null;
        } finally {
            return activity;
        }

    }

    function getMimeType(mimeType, uri)
    {
        if (!mimeType || !(mimeCategoryMap.hasOwnProperty(mimeType)))
        {
            var ext = getFileExtension(uri);
            //if( DEBUG ) {NetBeans.Logger.log("netmonitor - getFileExtension: " + ext); }
            if (!ext)
                return mimeType;
            else
            {
                var extMimeType = mimeExtensionMap[ext.toLowerCase()];
                return extMimeType ? extMimeType : mimeType;
            }
        }
        else
            return mimeType;
    }

    /*
     * @param {string} uri
     */
    function getFileExtension( uri ){
        var ext = "";
        var index = uri.indexOf('.');
        if ( index > -1 && uri.length) {
            ext = uri.substr(index,uri.length);
        }
        return ext;
    }

    /*
     * On Observe when topic is "http-on-modify-request"
     * @param {NetActivity} aActivity
     */
    function sendNetActivity ( aActivity ){
        var netActivity = <http/>;
        netActivity.type="request";
        netActivity.id=aActivity.uuid;
        netActivity.method=aActivity.method;
        netActivity.timestamp=aActivity.time;
        netActivity.urlParams=aActivity.urlParams;
        netActivity.url = aActivity.url;
        netActivity.postText = aActivity.postText;
        netActivity.load_init = aActivity.load_init;
        var headers = aActivity.requestHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if (DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }

        socket.send(netActivity);
    }
    /*
     * On Observe when topic is "http-on-examine-request"
     * @param {NetActivity} aActivity;
     */
    function sendExamineNetResponse ( aActivity ){

        var netActivity = <http/>;
        netActivity.type = "response";
        netActivity.id = aActivity.uuid;
        netActivity.timestamp=aActivity.time;
        netActivity.status = aActivity.status;
        netActivity.url = aActivity.url;
        netActivity.mimeType = aActivity.mimeType;
        var headers = aActivity.responseHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if(DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }
        socket.send(netActivity);
    }

    function sendProgressUpdate(progress, aRequest, current, max, total, maxTotal, time) {

        var request = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
        var index = requests.indexOf(request);
        var uuid = requestsId[index];


        var netActivity = <http />;
        netActivity.timestamp = time;
        netActivity.type ="progress";
        netActivity.id = uuid;
        netActivity.current = current;
        netActivity.max = max;
        netActivity.total = total;
        netActivity.maxTotal = maxTotal;
        if( DEBUG ){
            NetBeans.Logger.log(netActivity.toXMLString());
        }
        socket.send(netActivity);
    }


    /*
     * getRequestWebProgress
     * @param {nsIHttpChannel} aRequest
     * @return {nsIWebProgress}
     */
    function getRequestWebProgress(aRequest) {
        try
        {
        var DEBUG_METHOD = false && DEBUG;
        if(DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress: - aRequest:" + aRequest);

            var i = 0;
            var myInterface = null;
            if (aRequest.notificationCallbacks)
            {
                if(DEBUG_METHOD)  NetBeans.Logger.log("net.getRequestWebProgress: Notification Callback does exist #2.:" + aRequest.notificationCallbacks);
                var bypass = false;
                if (getRequestCategory(aRequest) == "xhr")
                {
                    if(DEBUG_METHOD)  NetBeans.Logger.log("net.getRequestWebProgress: - begin visit requestHeaders aRequest.notificationCallbacks.channel: " + aRequest.notificationCallbacks.channel);
                    aRequest.notificationCallbacks.channel.visitRequestHeaders(
                    {
                            visitHeader: function(header, value)
                            {
                                if (DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress.visitHeader header: " + header + " value:" + value);
                                if (header == "X-Moz" && value == "microsummary") {
                                    if(DEUBG_METHOD) NetBeans.Logger.log("net.getRequestWebPRogress.visitHeader MATCH");
                                    bypass = true;
                                }
                            }
                        });
                }
                if (!bypass){
                    myInterface = GetInterface( aRequest.notificationCallbacks, NetBeans.Constants.WebProgressIF);
                    if(myInterface && DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress - myInterface: "+ myInterface);
                    return myInterface;
                }

            }
        } catch (exc) { if (DEBUG_METHOD) NetBeans.Logger.log("XXXX. net.getRequestWebProgress - Exception occurred: #1" + exc); }

        try {
            if ( aRequest.loadGroup && DEBUG && DEBUG_METHOD ) NetBeans.Logger.log("net.getRequestWebProgress - loadGroup:" + aRequest.loadGroup );
            if (aRequest.loadGroup && aRequest.loadGroup.groupObserver) {
                myInterface = aRequest.loadGroup.groupObserver.QueryInterface(NetBeans.Constants.WebProgressIF);
                if( DEBUG && DEBUG_METHOD ) NetBeans.Logger.log("net.getRequestWebProgress - myInterface: "+ myInterface);
                return myInterface;
            } else if( DEBUG_METHOD ) { NetBeans.Logger.log("net.getRequestWebProgress does not have loadGropu or groupObserver properties.")};
        }
        catch (exc) { if (DEBUG_METHOD) NetBeans.Logger.log(i++ + "XXXX. net.getRequestWebProgress - Exception occurred: #2" + exc);}

        return null;

    }


    function isURLEncodedFile(request, text)
    {
        if (text && text.indexOf("Content-Type: application/x-www-form-urlencoded") != -1)
            return true;

        // The header value doesn't have to be alway exactly "application/x-www-form-urlencoded",
        // there can be even charset specified. So, use indexOf rather than just "==".
        //var headerValue = findHeader(file.requestHeaders, "Content-Type");
        if ( !request || !request.contentType )
            return false;

        var headerValue = request.contentType;
        if (headerValue && headerValue.indexOf("application/x-www-form-urlencoded") == 0)
            return true;

        return false;
    }

    function convertToUnicode (text, charset)
    {
        if (!text)
            return "";
        try
        {
            var conv = NetBeans.Utils.CCSV(
            NetBeans.Constants.ScriptableUnicodeConverterServiceCID,
            NetBeans.Constants.ScriptableUnicodeConverterIF);

           // if( DEBUG ) NetBeans.Logger.log("netmonitor.convertToUnicode: convertSErvice" + conv);
            conv.charset = charset ? charset : "UTF-8";
            return conv.ConvertToUnicode(text);
        }
        catch (exc) { NetBeans.Logger.log("netmonitor.convertToUnicode: " + exc); }
        return text;
    }

    function readFromStream(stream, charset)
    {
        try
        {
            var binaryInputStream = NetBeans.Utils.CCSV(
            NetBeans.Constants.BinaryInputStreamCID,
            NetBeans.Constants.BinaryInputStreamIF);

            //if ( DEBUG ){NetBeans.Logger.log("netmonitor.readFromStream - binaryInputStream: " + binaryInputStream); }

            binaryInputStream.setInputStream(stream);

            var segments = [];
            for (var count = stream.available(); count; count = stream.available()){
                //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - count: " + count);
                var bytes = binaryInputStream.readBytes(count);
                //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - bytes: " + bytes);
                segments.push(bytes);
            }
            var text = segments.join("");
            var convertedText = convertToUnicode(text, charset);
            //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - convertedText:" + convertedText);

            return convertedText;
         }
         catch(exc) { }
    }

    function parseURLEncodedText (text)
    {
        const maxValueLength = 25000;

        var params = [];

        var args = text.split("&");
        for (var i = 0; i < args.length; ++i)
        {
            var parts = args[i].split("=");
            if (parts.length == 2)
            {
                if (parts[1].length > maxValueLength)
                    parts[1] = this.$STR("LargeData");

                params.push({name: unescape(parts[0]), value: unescape(parts[1])});
            }
            else
                params.push({name: unescape(parts[0]), value: ""});
        }
        params.sort(function(a, b) { return a.name < b.name ? -1 : 1; });

        return params;
    };



    function getPostText(activity, request, context)
    {
        if ( DEBUG )  NetBeans.Logger.log("  netmonitor.getPostText href:" + activity.url );

        var postText;
        // According to firebug this stuff is supposidely needed...
//        if( activity.url ){
//          if ( DEBUG ) NetBeans.Logger.log("  netmonitor.getPostText - using getPostTextFromPage");
//          postText = getPostTextFromPage(activity.url, context);
//        }
//        if (!postText) {
//          if ( DEBUG ) NetBeans.Logger.log("  netmonitor.getPostText - using getPostTextFromRequest");
//          postText = getPostTextFromRequest(request, context);
//        }

        if( !postText) {
            if ( DEBUG ) NetBeans.Logger.log("  netmonitor.getPostText - using getPostTextFromUploadStream");
            var uploadChannel = request.QueryInterface(NetBeans.Constants.UploadChannelIF);
            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - Upload:" + uploadChannel);}
            var uploadStream = uploadChannel.uploadStream;

            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - uploadStream:" + uploadStream);}
            var text = getPostTextFromUploadStream(uploadStream, context);
            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - text:" + text);}

            if (isURLEncodedFile(request, text)) {
                //if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  URL ENCODED");
                var lines = text.split("\n");
                var params = parseURLEncodedText(lines[lines.length-1]);
                //if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  params:" + params);

                postText = "";
                var pair;
                for( pair in params ){
                    postText += params[pair].name  + "=" +  params[pair].value + " ";
                    if (DEBUG) NetBeans.Logger.log( params[pair].name + ":" + params[pair].value);
                }

            }
            else
            {
                if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  not URL ENCODED");
                postText = text;
                /*  var postText = formatPostText(text);
                  if (postText)
                      insertWrappedText(postText, postTextBox);*/
            }


        }

        return postText;
    }


//    function getPostTextFromRequest(request, context) {
//        try {
//
//            if ( !request.notificationCallbacks) {
//                return null;
//            }
//            var xhrRequest = GetInterface(request.notificationCallbacks, NetBeans.Constants.XMLHttpRequestIF);
//            if( xhrRequest ) {
//                if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromrequest - xhrRequest detected: " + xhrRequest);
//                return getPostTextFromXHR(xhrRequest, context);
//            }
//            return null;
//        } catch (exc) { NetBeans.Logger.log(" netmonitor.getPostTextFromRequest: " + exc);}
//    }
//
//    function getPostTextFromPage (url, context) {
//      if (url == context.browser.contentWindow.location.href)
//      {
//          try
//          {
//              var webNav = context.browser.webNavigation;
//              var descriptor =  webNav.QueryInterface(NetBeans.Constants.WebPageDescriptorIF).currentDescriptor;
//              var entry = descriptor.QueryInterface(NetBeans.Constants.SHEntryIF);
//              if (entry && entry.postData)
//              {
//                  var postStream = entry.postData.QueryInterface(NetBeans.Constants.SeekableStreamIF);
//                  postStream.seek(SEEK_SET, 0);
//
//                  var charset = context.window.document.characterSet;
//                  return readFromStream(postStream, charset);
//              }
//           }
//           catch (exc) { if (DEBUG)   NetBeans.Logger.log(" netmonitor.readPostTextFromPage FAILS, url:"+url, exc);    } }
//    }

    function getPostTextFromUploadStream ( uploadStream, context ){
        if (uploadStream)
            {
            //if( DEBUG ){  NetBeans.Logger.log("netmonitor.getPostTextFromUploadStream - uploadStream:" + uploadStream);     }
            var seekableStream = uploadStream.QueryInterface(NetBeans.Constants.SeakableStreamIF);
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream seekableStream: " + seekableStream);
            if (seekableStream) seekableStream.seek(SEEK_SET, 0);
            var charset = context.window.document.characterSet;
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream charset: " + charset);
            var text = readFromStream(uploadStream, charset);
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream text: " + text);
            if (seekableStream) seekableStream.seek(SEEK_SET, 0); //Not sure why firebug does this as well?

            return text;
          } //else { if( DEBUG ){ NetBeans.Logger.log(" netmonitor.getPostTextFromUploadStream - uploadStream is null"); } }
          return null;
    }

    function getPostTextFromXHR(xhrRequest, context) {

        if( DEBUG ){ NetBeans.Logger.log("  netmonitor- getPostTextFromXHR: " + xhrRequest); }

        try
        {
          var channel = xhrRequest.channel;
          var uploadStream = channel.QueryInterface(NetBeans.Constants.UploadChannelIF()).uploadStream;
          return getPostTextFromUploadStream(uploadStream, context);
        }
        catch(exc){ NetBeans.Logger.log(" netmonitor.getPostTextFromXHR: " + exc);}

      return null;
    }


    /*
     * @param {nsIWebProgress} aWebProgress
     * @return {nsIDOMWindow}
     */
    function safeGetWindow(aWebProgress)
    {
        var SAFE_GET_WINDOW_DEBUG = false;
        var win = null;
        if (DEBUG && SAFE_GET_WINDOW_DEBUG) NetBeans.Logger.log("net.safeGetWindow");
        try
        {
            if ( !aWebProgress || !aWebProgress.DOMWindow){
                if (DEBUG && SAFE_GET_WINDOW_DEBUG)  NetBeans.Logger.log("net.safeGetWindow - aWebProgress:" + aWebProgress + " & its DOMWindow:" + aWebProgress.DOMWindow);
                return;
            } else {
                win = aWebProgress.DOMWindow;
                if( !win )  NetBeans.Logger.log("net.safeGetWindow - window is null");
            }
        }
        catch (exc){ NetBeans.Logger.log("net.safeGetWindow - Exception: " + exc);}
        return win;
    }
    function getRequestCategory(aRequest)
    {
        try
        {
            if (aRequest.notificationCallbacks && aRequest.notificationCallbacks instanceof XMLHttpRequest){
                return "xhr";
            }
            return null;
        }
        catch (exc) {}
    }


    function safeGetName(request)
    {
        try
        {
            return request.name;
        }
        catch (exc)
        {
            return null;
        }
    }


    function nowTime()
    {
        return (new Date()).getTime();
    }

    function GetInterface(obj, aInterface)
    {
        if( DEBUG ) NetBeans.Logger.log("net.GetInterace obj:" + obj + " aInterface: "+ aInterfaace);
        if(!obj || !aInterface ){
            NetBeans.Logger.log("net.GetInterface - you are passing null params");
        }
        try
        {
            return obj.getInterface(aInterface);
        }
        catch (e)
        {
            if (e.name == NetBeans.Constants.NS_NOINTERFACE)
            {
                //if (DEBUG)
                NetBeans.Logger.Log("net.GetInterface - obj has no interface: ", aInterface, obj);
            }
        }

        return null;
    }

    function S4() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    function uuid() {
        return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
    }


}).apply(NetBeans.NetMonitor);