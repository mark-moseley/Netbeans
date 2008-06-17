/*
* Support js
*/


var rjsSupport = {

    getHttpRequest: function() {
        var xmlHttpReq;
        try
        {    // Firefox, Opera 8.0+, Safari, IE7.0+
            xmlHttpReq=new XMLHttpRequest();
        }
        catch (e)
        {    // Internet Explorer 6.0+, 5.0+
            try
            {
                xmlHttpReq=new ActiveXObject("Msxml2.XMLHTTP");
            }
            catch (e)
            {
                try
                {
                    xmlHttpReq=new ActiveXObject("Microsoft.XMLHTTP");
                }
                catch (e)
                {
                    this.debug("Your browser does not support AJAX!");
                }
            }
        }
        return xmlHttpReq;
     },

     open : function(method, url2, mimeType, paramLen, async) {

        //add timestamp to make url unique in case of IE7
        var url = url2;
        var timestamp = new Date().getTime();
        if(url.indexOf("?") != -1)
            url = url+"&timestamp="+timestamp;
        else
            url = url+"?timestamp="+timestamp;

        var xmlHttpReq = this.getHttpRequest();
        if(xmlHttpReq == null) {
            this.debug('Error: Cannot create XMLHttpRequest');
            return null;
        }
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //this.debug("Permission UniversalBrowserRead denied.");
        }
        try {
            xmlHttpReq.open(method, url, async);
        } catch( e ) {
            this.debug('Error: XMLHttpRequest.open failed for: '+url+' Error name: '+e.name+' Error message: '+e.message);
            return null;
        }
        if (mimeType != null) {
            if(method == 'GET') {
                //this.debug("setting GET accept: "+mimeType);
                xmlHttpReq.setRequestHeader('Accept', mimeType);
            } else if(method == 'POST' || method == 'PUT'){
                //this.debug("setting content-type: "+mimeType);
                //Send the proper header information along with the request
                xmlHttpReq.setRequestHeader("Content-Type", mimeType);
                xmlHttpReq.setRequestHeader("Content-Length", paramLen);
                xmlHttpReq.setRequestHeader("Connection", "close");
            }
        }
        //For cache control on IE7
        xmlHttpReq.setRequestHeader("Cache-Control", "no-cache");
        xmlHttpReq.setRequestHeader("Pragma", "no-cache");
        xmlHttpReq.setRequestHeader("Expires", "-1");

        return xmlHttpReq;
    },

    loadXml : function(xmlStr) {
        var doc2;
        // code for IE
        if (window.ActiveXObject)
        {
            doc2=new ActiveXObject("Microsoft.XMLDOM");
            doc2.async="false";
            doc2.loadXML(xmlStr);
        }
        // code for Mozilla, Firefox, Opera, etc.
        else
        {
            var parser=new DOMParser();
            doc2=parser.parseFromString(xmlStr,getDefaultMime());
        }
        return doc2;
    },

    findIdFromUrl : function(u) {
        var li = u.lastIndexOf('/');
        if(li != -1) {
            var u2 = u.substring(0, li);      
            var li2 = u2.lastIndexOf('/');
            u2 = u.substring(0, li2);
            return u.substring(li2+1, li);
        }
        return -1;
    },

    get : function(url, mime) {
        var xmlHttpReq = this.open('GET', url, mime, 0, false);
        xmlHttpReq.send(null);
        try {
          if (xmlHttpReq.readyState == 4) {
              var rtext = xmlHttpReq.responseText;
              if(rtext == undefined || rtext == '' || rtext.indexOf('HTTP Status') != -1) {
                  if(rtext != undefined)
                      this.debug('Failed XHR(GET, '+url+'): Server returned --> ' + rtext);
                  return '-1';
              }
              return rtext;           
           }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return '-1';
    },

    post : function(url, mime, content) {
        var xmlHttpReq = this.open('POST', url, mime, content.length, false);
        xmlHttpReq.send(content);
        try {
            if (xmlHttpReq.readyState == 4) {
                var status = xmlHttpReq.status;
                if(status == 201) {
                    return true;
                } else {
                    this.debug('Failed XHR(POST, '+url+'): Server returned --> ' + status);
                }
            }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    put : function(url, mime, content) {
        var xmlHttpReq = this.open('PUT', url, mime, content.length, false);
        xmlHttpReq.send(content);
        try {
          if (xmlHttpReq.readyState == 4) {
              var status = xmlHttpReq.status;
              if(status == 204) {
                  return true;
              } else {
                  this.debug('Failed XHR(PUT, '+url+'): Server returned --> ' + status);
              }
          }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    delete_ : function(url) {
        var xmlHttpReq = this.open('DELETE', url, 'application/xml', 0, false);
        xmlHttpReq.send(null);
        try {
          if (xmlHttpReq.readyState == 4) {
              var status = xmlHttpReq.status;
              if(status == 204) {
                  return true;
              } else {
                  this.debug('Failed XHR(DELETE, '+url+'): Server returned --> ' + status);
              }
          }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    debug : function(message) {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp == null) {
            dbgComp = document.createElement("div");
            dbgComp.setAttribute("id", "dbgComp");
            dbgComp.style.border = "#2574B7 1px solid";
            dbgComp.style.font = "12pt/14pt sans-serif";
            var br = document.createElement("div");
            document.getElementsByTagName("body")[0].appendChild(br);
            br.innerHTML = '<br/><br/><br/>';
            document.getElementsByTagName("body")[0].appendChild(dbgComp);
            if((typeof rjsConfig!="undefined") && rjsConfig.isDebug) {
                dbgComp.style.display = "";
            } else {
                dbgComp.style.display = "none";
            }
            var tab = 'width: 20px; border-right: #2574B7 1px solid; border-top: #2574B7 1px solid; border-left: #2574B7 1px solid; border-bottom: #2574B7 1px solid; color: #000000; text-align: center;';
            var addActionStr = '<div style="'+tab+'"><a style="text-decoration: none" href="javascript:rjsSupport.closeDebug()"><span style="color: red">X</span></a></div>';        
            dbgComp.innerHTML = '<table><tr><td><span style="color: blue">Rest Debug Window</span></td><td>'+addActionStr + '</td></tr></table><br/>';
        }
        var s = dbgComp.innerHTML;
        var now = new Date();
        var dateStr = now.getHours()+':'+now.getMinutes()+':'+now.getSeconds();
        dbgComp.innerHTML = s + '<span style="color: red">rest debug('+dateStr+'): </span>' + message + "<br/>";
    },
    
    closeDebug : function() {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp != null) {
            dbgComp.style.display = "none";
            dbgComp.innerHTML = '';
        }
    }
}
