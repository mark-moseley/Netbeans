/*
* Supporting js for testing resource beans
*/
var wadlDoc;            
var app;
var wadlURL = baseURL+"/application.wadl";  
var wadlErr = 'Cannot access WADL: Please restart your REST application, and refresh this page.';
var currentUrl;
var currentValidUrl;
var breadCrumbs = new Array();
var currentContainer;
var treeHook;
var myTree;

function getHttpRequest() {
    var xmlHttpReq;
    try
    {    // Firefox, Opera 8.0+, Safari, IE7.0+
        xmlHttpReq=new XMLHttpRequest();
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //alert("Permission UniversalBrowserRead denied.");
        }
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
                alert("Your browser does not support AJAX!");
            }
        }
    }
    return xmlHttpReq;
 }
 function open(method, url, mimeType, paramLen, async) {
    currentUrl = url;
    var xmlHttpReq = getHttpRequest();
    if(xmlHttpReq == null) {
    	alert('Error: Cannot create XMLHttpRequest');
        return null;
    }
    try {
        netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
    } catch (e) {
        //alert("Permission UniversalBrowserRead denied.");
    }
    try {
        xmlHttpReq.open(method, url, async);
    } catch( e ) {
        //alert('Error: XMLHttpRequest.open failed for: '+strURL+' Error name: '+e.name+' Error message: '+e.message);
        return null;
    }
    log("mimeType: "+mimeType);
    if (mimeType != null) {
        if(method == 'GET') {
            //alert("setting GET accept: "+mimeType);
            xmlHttpReq.setRequestHeader('Accept', mimeType);
        } else if(method == 'POST' || method == 'PUT'){
            //alert("setting content-type: "+mimeType);
            //Send the proper header information along with the request
            xmlHttpReq.setRequestHeader("Content-Type", mimeType);
            xmlHttpReq.setRequestHeader("Content-Length", paramLen);
            xmlHttpReq.setRequestHeader("Connection", "close");
        }
    }
    currentValidUrl = url;
    return xmlHttpReq;
}
function updateMenu(xmlHttpReq) {                
    try {
        if (xmlHttpReq.readyState == 4) {
            var rtext = xmlHttpReq.responseText;                   
            if(rtext == undefined || rtext == "" || rtext.indexOf("HTTP Status") != -1) {   
                var newUrl = prompt(wadlErr, baseURL);
                if(newUrl != null && baseURL != newUrl) {
                    baseURL = newUrl;
                    wadlURL = baseURL+"/application.wadl";
                    init();
                }
                return;
            }
            //alert("[here]");
            setvisibility('main', 'inherit');
            document.getElementById('subheader').innerHTML = '<br/><b>WADL: </b>'+wadlURL;
            wadlDoc = loadXml(rtext);
            if(wadlDoc != null) {                
                initTree(wadlDoc);
            }
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: ' + e.name + ' message: ' + e.message);
    }
}
function initTree(wadlDoc) {
    var myTree = createTree(wadlDoc);
    var treeString = myTree.toString();
    document.getElementById('leftSidebar').innerHTML = treeString;
    showCategory('resources');
}
function refreshTree(wadlDoc) {
    var myTree = createTree(wadlDoc);
    var treeString = myTree.toString();                            
    document.getElementById('leftSidebar').innerHTML = treeString;
}
function createTree(wadlDoc) {
    var app=wadlDoc.documentElement;
    var myTree = new tree();
    var rs;
    if(app != null) {
        rs = app.getElementsByTagName('resources')[0];
        var context = rs.attributes.getNamedItem('base').nodeValue;
        var begin = context.indexOf('/', 7);
        if(begin != -1)
            context = context.substring(begin, context.length);
        var index = context.indexOf('/', 1);
        if(context.length > 1 && index != -1)
            context = context.substring(1, index);
        var resources = new category(rs.nodeName, context);
        myTree.add(resources);
        var rarr = rs.getElementsByTagName('resource');
        for(i=0;i<rarr.length;i++) {
            var r = rarr[i];   
            var path = r.attributes.getNamedItem('path');
            var cName = path.nodeValue;
            if(cName != null && cName.indexOf('/') != -1)
                cName = cName.substring(1);
            var start = new category(path.nodeValue, cName);
            var methods = r.getElementsByTagName('method');
            for(j=0;j<methods.length;j++) {
                var m = methods[j];                            
                var mName = m.attributes.getNamedItem("name").nodeValue;
                var response = m.getElementsByTagName('response');
                var mediaType = getMediaType(response);
                if(mediaType != null)
                    mName = mName + '(' + mediaType + ')';
                var methodst = createItem(mName,'showContent(\''+app.nodeName+'/'+rs.nodeName+'/'+r.nodeName+'\', '+i+', '+j+')');
                start.add(methodst);
                treeHook = start;
            }
            resources.add(start);
        }
    }
    return myTree;
}
function createCategory(id, name) {
    return new category(id, name);
}
function createItem(name, uri) {
    return new item(name, uri);
}
function getMediaType(response) {
    var mediaType = null;                
    if(response != null && response.length > 0) {
        var rep = response[0].getElementsByTagName('representation');
        if(rep != null && rep.length > 0) {                        
            if(rep[0].attributes.length > 0) {
                var att = rep[0].attributes.getNamedItem('mediaType');
                if(att != null)
                    mediaType = att.nodeValue
            }
        }
    }
    return mediaType;
}
function setvisibility(id, state) {
    try {
        document.getElementById(id).style.visibility = state;
    } catch(e) {}
}
function changeMethod()
{
    var methodNode = document.getElementById("methodSel");
    var method = methodNode.options[methodNode.selectedIndex].value;
    var formSubmittal = document.getElementById("formSubmittal");
    if(formSubmittal != null) {
        var content = formSubmittal.innerHTML;
        var index = content.indexOf('method');
        if(index != -1) {
            var index2 = content.indexOf('name');
            formSubmittal.innerHTML = content.substring(0, index)+" method='"+method+"' "+content.substring(index2);
        }
    }
    document.getElementById("method").value = method;
    var paramRep = getParamRep(null, method);
    document.getElementById("paramHook").innerHTML = paramRep;
    //alert(formSubmittal.innerHTML);
    updatepage('result', '');
    updatepage('resultheaders', '');
};
function changeMimeType()
{
    var mimeNode = document.getElementById("mimeSel");
    var mime = mimeNode.options[mimeNode.selectedIndex].value;
    document.getElementById("mimeType").value = mime;
};
function showContent(path, ri, mi) {
    //updatepage('result', 'Loading...');
    var app1 = wadlDoc.documentElement;
    var rs = app1.getElementsByTagName('resources')[0];
    var r = rs.getElementsByTagName('resource')[ri];
    var m = r.getElementsByTagName('method')[mi];
    var mName = m.attributes.getNamedItem("name").nodeValue;
    var qmName = mName;
    //var req = m.getElementsByTagName('request');
    var response = m.getElementsByTagName('response');
    var mediaType = getMediaType(response);
    var uri = r.attributes.getNamedItem('path').nodeValue;
    doShowContent(uri, mName, mediaType);     
}
function doShowContent(uri) {
    doShowContent(uri, getDefaultMethod(), 'text/xml');
}
function getDefaultMime() {
    return "application/xml";
}
function getDefaultMethod() {
    return "GET";
}
function doShowContent(uri, mName, mediaType) {
    updatepage('result', '');
    updatepage('resultheaders', '');
    var qmName = '';
    if(mediaType != null)
        qmName = qmName + "("+mediaType+")";
    else
        mediaType = getDefaultMime();
    showBreadCrumbs(uri);
    var str = "<b>Method: </b>";
    str += "<select id='methodSel' name='methodSel' onchange='javascript:changeMethod();'>";
    str += "  <option selected value='GET'>GET</option>";
    str += "  <option value='PUT'>PUT</option>";
    str += "  <option value='POST'>POST</option>";
    str += "  <option value='DELETE'>DELETE</option>";
    str += "</select>";
    str += "&nbsp;&nbsp;<b>MIME: </b>";
    str += "<select id='mimeSel' name='mimeSel' onchange='javascript:changeMimeType();'>";
    str += "  <option value='application/xml'>application/xml</option>";
    str += "  <option value='text/xml'>text/xml</option>";
    str += "  <option value='text/plain'>text/plain</option>";
    str += "  <option value='text/html'>text/html</option>";    
    str += "</select>";
    str += "<br/><br/>";
    str += getFormRep(null, uri, mName, mediaType);
    document.getElementById('testres').innerHTML = str;
    var req = uri;
    var disp = getDisplayUri(req);
    var uriLink = "<a id='"+req+"' href=javascript:doShowContent('"+req+"') >"+disp+"</a>";
    updatepage('request', '<b>Resource:</b> '+uriLink+' (<a href="'+req+'" target="_blank">'+req+'</a>)');
    //alert(str);
    /*try {
        testResource();
    } catch(e) { alert(e.name+e.message);}*/
}
function getFormRep(req, uri, mName, mediaType) {
    if(mName == null || mName == 'undefined')
        mName = getDefaultMethod();
    if(mediaType == null || mediaType == 'undefined')
        mediaType = 'text/xml';
    //alert(req + uri + mName + mediaType);
    var str = "<div id='formSubmittal'>";
    str += "<form action='' method="+mName+" name='form1'>";
    str += "<div id='paramHook'></div>";
    //str += getParamRep(req, mName);
    str += "<input name='path' value='"+uri+"' type='hidden'>";
    str += "<input id='method' name='method' value='"+mName+"' type='hidden'>";
    str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
    str += "<br/><input value='Test...' type='button' onclick='testResource()'>";
    str += "</form>";
    str += "</div>";
    return str;
}
function showBreadCrumbs(uri) {
    var nav = document.getElementById('navigation');
    var disp = getDisplayUri(uri);
    var count = 0;
    for(i=0;i<breadCrumbs.length;i++) {
        if(breadCrumbs[i] == disp) {
            count++;
        }
    }
    if(count == 0) {
        breadCrumbs[breadCrumbs.length+1] = disp;
        var uriLink = "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+disp+"</a>";
        if(nav.innerHTML != '') {
            bcCount++;
            var uriPerLine = 5;
            //alert(bcCount);
            //alert(Math.round(bcCount/uriPerLine)+ ' ' + bcCount/uriPerLine);
            if(Math.round(bcCount/uriPerLine) == bcCount/uriPerLine)
                nav.innerHTML = nav.innerHTML + " , <br/>" + uriLink;
            else
                nav.innerHTML = nav.innerHTML + " , " + uriLink;
        } else
            nav.innerHTML = uriLink;
    }
}
var bcCount = 0;
function getParamRep(req, mName) {
    var str = "";
    if(mName == 'PUT' || mName == 'POST')
        str = "<textarea id='blobParam' name='params' rows='8' cols='70'></textarea><br/>";
    else if(mName == 'GET') {
        if(req != null && req.length > 0) {                    
            for(i=0;i<req.length;i++) {
                var params = req[i].getElementsByTagName('param');
                if(params != null) {
                    for(j=0;j<params.length;j++) {
                        var pname = params[j].attributes.getNamedItem('name').nodeValue;
                        var num = j+1;
                        str += "<b>Param"+num+":</b>"+"<input id='params' name='"+pname+"' type='text' value='"+pname+"'>"+"<br><br>";
                    }
                }
            }
        } else {
            str = "";
        }
    }
    else if(mName == 'DELETE')
        str = "";
    if(str != "")
        str = "<b>Resource Inputs:</b><br><br><div style='margin-left:20px'>"+str+"</div><br/>";
    return str;
}
function testResource() {
    updatepage('result', '');
    var mimetype = getRep();
    var method = getMethod();
    //alert('method: '+method+'mimetype: '+mimetype);
    var p = '';
    var path = document.forms[0].path.value;
    var found = path.indexOf( "{" );
    if (found != -1){
        if(document.forms[0].params != null) {
            var len = document.forms[0].params.length;
            for(j=0;j<len;j++) {
                var param = document.forms[0].params[j]
                path = path.replace("{"+param.name+"}", param.value);
            }
        }
    } else {
        if(document.forms[0].params != null) {
            var len = document.forms[0].params.length;
            for(j=0;j<len;j++) {
                var param = document.forms[0].params[j]
                if(len == 1 || len-j>1)
                    p += param.name+"="+param.value;
                else
                    p += param.name+"="+param.value+"&";
            }
        }
    }
    var params = null;
    var paramLength = 0;
    if(method == 'POST' || method == 'PUT'){
        var blobParam = document.getElementById('blobParam').value;
        if(blobParam != null && blobParam != undefined)
            params = blobParam;
        else if(p != null && p != undefined)
            params = p;
        if(params != null)
            paramLength = params.length;
    } else if(method == 'GET' || method == 'DELETE') {
        paramLength = 0;
    }
    var req;
    if(path.indexOf('http:') != -1)
        req = path;
    else
        req = baseURL+path
    if(method == 'GET' && p.length > 0)
        req+= "?"+p;
    var disp = getDisplayUri(req);
    if (mimetype == 'image/jpg') {//image
        alert('The image/jpg MimeType currently does not work with the MimeType selection method.\nInstead of seeing the image, you will see the image data');
    } else {
        //alert('method: '+method+'mimetype: '+mimetype+' length: '+paramLength+'params: '+params);
        var xmlHttpReq4 = open(method, req, mimetype, paramLength, true);
        xmlHttpReq4.onreadystatechange = function() { updateContent(xmlHttpReq4); };
        xmlHttpReq4.send(params);
    }
}
function createIFrame(currentValidUrl) {
    var c = '<iframe src="'+currentValidUrl+'" width="600" height="300" align="left">'+
            '<p>See <a href="'+currentValidUrl+'">"'+currentValidUrl+'"</a>.</p>'+
        '</iframe>';
    return c;
}
function showTableView(flag) {
    var tableNode = document.getElementById('tableContent').style;
    var rawNode = document.getElementById('rawContent').style;
    var tabs1 = document.getElementById('tabs1').style;
    var tabs2 = document.getElementById('tabs2').style;
    if(flag == 'true') {
        tableNode.display="block";
        rawNode.display="none";
        tabs1.display="block";
        tabs2.display="none";
    } else {
        tableNode.display="none";
        rawNode.display="block";
        tabs2.display="block";
        tabs1.display="none";
    }        
}
function updateContent(xmlHttpReq) {
    try {
        if (xmlHttpReq.readyState == 4) {
            var content = xmlHttpReq.responseText;
            var ndx = content.indexOf('HTTP Status');            
            if(ndx != -1)
                alert(content.substring(ndx, ndx+16));
            var ndx2 = content.indexOf('Caused by: java.lang.');
            if(ndx2 != -1)
                alert('Server returned error: '+content);
            //alert('result: ['+content+']');
            if(content != null && content != undefined) {
                try {
                    var tableContent = '';
                    //alert(content);
                    var cErr = '<table border=1><tr><td width=600>Content may not have Container-Containee Relationship.'+
                            ' See Raw View for content.</td></tr></table>';
                    if(content.indexOf("<?xml ") != -1) {
                        tableContent = getContainerTable(content);
                        if(tableContent == null || tableContent.length <= 594 || 
                                tableContent.length == content.length)
                            tableContent = cErr;
                    } else {
                        tableContent = cErr;
                    }
                    var rawContent = createIFrame(currentValidUrl);
                    updatepage('result', '<b>Content:</b> '+
                        '<div id="tabs1">'+'<table class="result"><tr>'+
                        '<td width=100 class="tbs1"><a href="javascript:showTableView(\'true\')"><span style="color: #ffffff;">Tabular View</span></a></td>'+
                        '<td width=100 class="tbs2"><a href="javascript:showTableView(\'false\')"><span style="color: #000000;">Raw View</span></a></td></tr></table>'+
                        '</div>'+
                        '<div id="tabs2" style="display: none;">'+'<table class="result"><tr>'+
                        '<td width=100 class="tbs2"><a href="javascript:showTableView(\'true\')"><span style="color: #000000;">Tabular View</span></a></td>'+
                        '<td width=100 class="tbs1"><a href="javascript:showTableView(\'false\')"><span style="color: #ffffff;">Raw View</span></a></td></tr></table>'+
                        '</div>'+
                        '<div id="menu_bottom" class="tbs1 tabsbottom"></div>'+
                        '<div id="tableContent">'+tableContent+'</div>'+
                        '<div id="rawContent" style="display: none;">'+rawContent+'</div>');
                } catch( e ) {
                    //alert('upd err '+e.name+e.mesage);
                    var c = createIFrame(currentValidUrl);
                    updatepage('result', '<b>Content:</b> '+c);
                }  
            }
            //alert(xmlHttpReq.getAllResponseHeaders());
            var hTable = getHeaderAsTable(xmlHttpReq.getAllResponseHeaders());
            updatepage('resultheaders', '<b>Response Headers:</b> '+hTable);
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: ' + e.name + ' message: ' + e.message);
    }
} 
function getHeaderAsTable(header) {
    //alert(header);    
    var str = "<table class='results' border='1'>";
    str += "<thead class='resultHeader'>";
    var colNames = new Array()
    colNames[0] = "header"
    colNames[1] = "value"
    var colSizes = new Array()
    colSizes[0] = "80"
    colSizes[1] = "250"
    for (i=0;i<colNames.length;i++) {
        str += "<th width='"+colSizes[i]+"' align='left'><font color='#FFFFFF'><b>"+colNames[i]+"</b></font></th>";
    }
    str += "</thead>";
    str += "<tbody>";
    var rows = header.split('\r\n');
    if(rows.length == 1)
        rows = header.split('\n');
    for(i=0;i<rows.length;i++) {
        //alert(rows[i]);
        var index = rows[i].indexOf(':');
        var name = rows[i].substring(0, index);
        var val = rows[i].substring(index+1);
        str += "<tr style='font-size: 9px;'>";
        str += "<td>"+name+"</td>";
        str += "<td>"+val+"</td>";
        str += "</tr>";
    }
    str += "</tbody></table>";
    return str;
}
function loadXml(xmlStr) {
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
}
function getContainerTable(xmlStr) {
    //alert(xmlStr);
    var ret = '';  
    if(xmlStr != null)
        ret = xmlStr.replace(/~lt~/g, "<");
    else
        return ret;
    var doc2 = null;
    try {
        doc2 = loadXml(ret);
    } catch(e) {}
    if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
        try {
            var container=doc2.documentElement;
            if(container == null || container.nodeName == 'html')
                return ret;
            var colNames = new Array()
            colNames[0] = "ID"
            colNames[1] = "URI"
            var colSizes = new Array()
            colSizes[0] = "100"
            colSizes[1] = "500"
            var str = "<table class='results' border='1'>";
            str += "<thead class='resultHeader'>";
            for (i=0;i<colNames.length;i++) {
                str += "<th width='"+colSizes[i]+"' align='left'><font color='#FFFFFF'><b>"+colNames[i]+"</b></font></th>";
            }
            str += "</thead>";
            str += "<tbody id='containerTable'>";
            str += findUri(container);
            str += "</tbody></table>";
            ret = str;
        } catch(e) {
            alert('err: '+e.name+e.message);
        }
    }
    return ret;
}
function findUri(container) {
    var str = '';
    var refs = container.childNodes;
    var count = 0;
    for(i=0;i<refs.length;i++) {
        var refsChild = refs[i];
        if(refsChild.nodeValue == null) {//DOM Elements only
            var ref = refsChild;
            str += "<tr style='font-size: 9px;'>";                
            var refChilds = ref.childNodes;
            for(j=0;j<refChilds.length;j++) {
                var refChild = refChilds[j];
                if(refChild.nodeValue == null) {//DOM Elements only
                    var id = refChild;
                    if(ref.attributes != null && ref.attributes.length > 0 && 
                            ref.attributes.getNamedItem('uri') != null) {
                        var uri = ref.attributes.getNamedItem('uri').nodeValue;
                        str += "<td>"+id.childNodes[0].nodeValue+"</td>";
                        str += "<td>";
                        var disp = getDisplayUri(uri);
                        str += "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+disp+"</a>";
                        str += "&nbsp;&nbsp;(<a href='"+uri+"' target='_blank'>"+uri+"</a>)";
                        str += "</td>";
                        str += "</tr>";
                    }
                    //findUri(str, ref);
                }
            }
            count++;
        }
    }
    return str;
}
function getDisplayUri(uri) {
    var disp = uri;
    if(disp.length > baseURL.length)
        disp = disp.substring(baseURL.length);
    return disp;
}
function updatepage(id, str){
    document.getElementById(id).innerHTML = str;
}
function log(msg) {
    //document.getElementById('log').innerHTML = msg;
}
function getRep() {
    var resource = document.getElementById('mimeType');
    if(resource != null)
        return resource.value;
    else
        return getDefaultMime();
}
function getMethod() {
    var resource = document.getElementById('method');
    if(resource != null)
        return resource.value;
    else
        return getDefaultMethod();
}
function init() {
    var params = new Array();
    var method = getDefaultMethod();
    var xmlHttpReq = open(method, wadlURL, null, 0, true);
    if(xmlHttpReq != null) {
        xmlHttpReq.onreadystatechange = function() { updateMenu(xmlHttpReq); };
        xmlHttpReq.send(null);
    } else {
        setvisibility('main', 'inherit');
        var str = '<b>Help Page</b><br/><br/>'+
            '<p>Cannot access WADL: Please restart your REST application, and refresh this page.</p>' +
            '<p>If you still see this error and if you are accessing this page using Firefox with Firebug plugin, then'+
            '<br/>you need to disable firebug for local files. That is from Firefox menubar, check '+
            '<br/>Tools > Firebug > Disable Firebug for Local Files</p>';
        document.getElementById('content').innerHTML = str;
    }            
}

/*
* Function to support uri navigation
*/
var expand = new Image();
expand.src = "expand.gif";
var collapse = new Image();
collapse.src = "collapse.gif";
var og = new Image();
og.src = "og.gif";
var cg = new Image();
cg.src = "cg.gif";

function tree(){
    this.categories = new Array();
    this.add = addCategory;
    this.toString = getTreeString;
    this.getCategories = listCategories;
}

function category(id, text){
    this.id = id;
    this.text = text;
    this.write = writeCategory;
    this.add = addItem;
    this.items = new Array();
    this.getItems = listItems;
}

function item(text, link){
    this.text = text;
    this.link = link;
    this.write = writeItem;
}

function getTreeString(){
    var treeString = '';
    for(var i=0;i<this.categories.length;i++) {
        treeString += this.categories[i].write();
    }
    return treeString;
}

function addCategory(category){
    this.categories[this.categories.length] = category;
}

function listCategories(){
    return this.categories;
}

function writeCategory(){
    var uri = baseURL + this.id;
    var categoryString = '<span class="category"';
    categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="updateTree(\'' + this.id + '\')">';
    categoryString += '<img src="collapse.gif" id="I' + this.id + '">';
    categoryString += "<a href=javascript:doShowContent('"+uri+"') >"+ this.text + "</a>";
    categoryString += '</span>';
    categoryString += '<span class="item" id="';
    categoryString += this.id + '">';
    var numitems = this.items.length;
    for(var j=0;j<numitems;j++)
        categoryString += this.items[j].write();
    categoryString += '</span>';
    return categoryString;
}

function addItem(item){
    this.items[this.items.length] = item;
}

function listItems(){
    return this.items;
}

function writeItem(){
    var itemString = '<img src="cc.gif" border="0">';
    itemString += '<a href="#" onClick="' + this.link + '">';
    itemString += '<img src="item.gif" border="0">';
    itemString += this.text;
    itemString += '</a><br>';
    return itemString;
}

function showCategory(category){
    var categoryNode = document.getElementById(category).style;
    if(categoryNode.display=="block")
        categoryNode.display="none";
    else
        categoryNode.display="block";
    changeGesture(category);
}

function updateTree(catId){    
    var myTree = createTree(wadlDoc);
    document.getElementById('leftSidebar').innerHTML = myTree.toString();
    childrenContent = '';
    getChildren(catId);
    currentCategory = catId;
    setTimeout("alertIt()",500);
}

function alertIt(){
    var catId = currentCategory;
    var categoryNode = document.getElementById(catId);
    categoryNode.innerHTML = childrenContent;
    showCategory('resources');
    showCategory(catId);
}

function getChildren(uri) {
    var xmlHttpReq5 = open('GET', baseURL+uri, 'text/xml', 0, true);
    xmlHttpReq5.onreadystatechange = function() { getChildrenContent(xmlHttpReq5); };
    xmlHttpReq5.send(null);
}

var childrenContent = '';

function getChildrenContent(xmlHttpReq5) {
    var content = xmlHttpReq5.responseText;
    //alert(content);
    if(content.indexOf('HTTP Status') == -1) {
        var ret = getChildrenAsItems(content);
        //alert(ret);
        if(ret == null)
            childrenContent = '';
        else
            childrenContent = ret;
    } else {
        childrenContent = '';
    }
}

function getChildrenAsItems(xmlStr) {
    var ret = null;  
    if(xmlStr != null)
        ret = xmlStr.replace(/~lt~/g, "<");
    else
        return ret;
    var doc2 = null;
    try {
        doc2 = loadXml(ret);
    } catch(e) { return null;}
    if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
        try {
            var container=doc2.documentElement;
            if(container == null || container.nodeName == 'html')
                return ret;
            var str = "";
            var refs = container.childNodes;
            var count = 0;
            for(i=0;i<refs.length;i++) {
                var refsChild = refs[i];
                if(refsChild.nodeValue == null) {//DOM Elements only
                    var ref = refsChild;             
                    var refChilds = ref.childNodes;
                    for(j=0;j<refChilds.length;j++) {
                        var refChild = refChilds[j];
                        if(refChild.nodeValue == null) {//DOM Elements only
                            var id = refChild;
                            if(ref.attributes != null && ref.attributes.length > 0 && 
                                    ref.attributes.getNamedItem('uri') != null) {
                                var uri = ref.attributes.getNamedItem('uri').nodeValue;
                                var idval = id.childNodes[0].nodeValue;
                                var disp = getDisplayUri(uri);
                                str += getItemString(idval, uri);
                                //str += "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+disp+"</a><br/>";
                            }
                        }
                    }
                    count++;
                }
            }
            ret = str;
        } catch(e) {
            //alert('err: '+e.name+e.message);
            return null;
        }
    }
    return ret;
}

function getItemString(name, uri){
    var itemString = '<img src="cc.gif" border="0">';
    itemString += '<img src="item.gif" border="0">';
    itemString += '<a href="javascript:doShowContent(\''+uri+'\')">';
    itemString += name;
    itemString += '</a><br>';
    return itemString;
}

function changeGesture(img){
    ImageNode = document.getElementById('I' + img);
    ImageNode1 = document.getElementById('I1' + img);
    if(ImageNode.src.indexOf('collapse.gif')>-1) {
        ImageNode.src = expand.src;
        ImageNode1.src = og.src;
    } else {
        ImageNode.src = collapse.src;
        ImageNode1.src = cg.src;
    }
}