/**
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

webui.suntheme4_2.dojo.provide("webui.suntheme4_2.widget.bubble");

webui.suntheme4_2.dojo.require("webui.suntheme4_2.browser");
webui.suntheme4_2.dojo.require("webui.suntheme4_2.widget.widgetBase");

/**
 * @name webui.suntheme4_2.widget.bubble
 * @extends webui.suntheme4_2.widget.widgetBase
 * @class This class contains functions for the bubble widget.
 * @constructor This function is used to construct a bubble widget.
 */
webui.suntheme4_2.dojo.declare("webui.suntheme4_2.widget.bubble", webui.suntheme4_2.widget.widgetBase, {
    // Set defaults.
    constructor: function() {
        this.defaultTime = 2000;
        this.openDelayTime = 500;
        this.bubbleLeftConst = 5;
        this.topConst = 2;
    },   
    widgetName: "bubble" // Required for theme properties.
});

/**
 * This function is used to close bubble help.
 *
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.close = function() {
    if (this.openTimerId != null) {
        clearTimeout(this.openTimerId);
    }
    if (this.getProps().visible == false) {
        return false;
    }
     
    var _id = this.id;
    this.timerId = setTimeout(function() {
        // New literals are created every time this function is called, and it's 
        // saved by closure magic.
        webui.suntheme4_2.dijit.byId(_id).setProps({visible: false});
        webui.suntheme4_2.dijit.byId(_id).srcElm.focus();
    }, this.defaultTime);

    return true;
};

/**
 * This object contains event topics.
 * <p>
 * Note: Event topics must be prototyped for inherited functions. However, these
 * topics must also be available statically so that developers may subscribe to
 * events.
 * </p>
 * @ignore
 */
webui.suntheme4_2.widget.bubble.event =
        webui.suntheme4_2.widget.bubble.prototype.event = {
    /**
     * This object contains refresh event topics.
     * @ignore
     */
    refresh: {
        /** Refresh event topic for custom AJAX implementations to listen for. */
        beginTopic: "webui_suntheme4_2_widget_bubble_event_refresh_begin",

        /** Refresh event topic for custom AJAX implementations to listen for. */
        endTopic: "webui_suntheme4_2_widget_bubble_event_refresh_end"
    },

    /**
     * This object contains state event topics.
     * @ignore
     */
    state: {
        /** State event topic for custom AJAX implementations to listen for. */
        beginTopic: "webui_suntheme4_2_widget_bubble_event_state_begin",

        /** State event topic for custom AJAX implementations to listen for. */
        endTopic: "webui_suntheme4_2_widget_bubble_event_state_end"
    }
};

/**
 * This function is used to get widget properties. Please see the 
 * setProps() function for a list of supported properties.
 *
 * @return {Object} Key-Value pairs of properties.
 */
webui.suntheme4_2.widget.bubble.prototype.getProps = function() {
    var props = this.inherited("getProps", arguments);

    // Set properties.
    if (this.title != null) { props.title = this.title; }
    if (this.contents != null) { props.contents = this.contents; }
    if (this.height != null) { props.height = this.height; }
    if (this.width != null) { props.width = this.width; }
    if (this.autoClose != null) { props.autoClose = this.autoClose; }
    if (this.duration != null) { props.duration = this.duration; }
    if (this.closeButton != null) {props.closeButton = this.closeButton;}
    if (this.openDelay != null) {props.openDelay = this.openDelay;}
    if (this.focusId != null) {props.focusId = this.focusId;}
    if (this.tabIndex != null) {props.tabIndex = this.tabIndex;}
    
    return props;
};

/**
 * Helper function to create callback for onClick event.
 *
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onClickCallback = function(event) {
    // Close the popup if close button is clicked.
    event = this.widget.getEvent(event);

    var target = (event.target)
        ? event.target 
        : ((event.srcElement) 
            ? event.srcElement : null);

    if (webui.suntheme4_2.browser.isIe5up()) {
        if (window.event != null) {
            window.event.cancelBubble = true;
        }
    } else {
        event.stopPropagation();
    }
    if (this.closeBtn == target) {
        clearTimeout(this.timerId);
        this.setProps({visible: false});
        this.srcElm.focus();
    }
    return true;
};

/**
 * Helper function to create callback for close event.
 *
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onCloseCallback = function(event) {
    if (event == null) {
        return false;
    }
    
    if ((event.type == "keydown" && event.keyCode == 27)
            || event.type == "click") {
        clearTimeout(this.timerId); 
        
        if (this.srcElm != null && this.getProps().visible) {
            if (this.srcElm.focus) {
                this.srcElm.focus();
            }
        }      
        this.setProps({visible: false});
    }
    return true;
};

/**
 * Helper function to create callback for shift + tab event.
 * Shift+Tab should not allow user to tab out of bubble component.
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onShftTabCallback = function(event) {
    if (event == null) {
        return false;
    }
    event = this.widget.getEvent(event);

    var target = (event.target)
        ? event.target 
        : ((event.srcElement) 
            ? event.srcElement : null);
    if (target == this.bubbleHeader) {                    
        if (webui.suntheme4_2.browser.isFirefox() && (event.shiftKey && (event.keyCode == 9))) {
            if (this.focusId != null) {
                document.getElementById(this.focusId).focus();        
            } else {                
                 this.bubbleHeader.focus();
            }
            event.stopPropagation();
            event.preventDefault(); 
        }
     }
     return true;
};

/**
 * Helper function to create callback for tab event.
 * Cyclic tabbing behavior is implemented for bubble to prevent tab out of bubble component. 
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onTabCallback = function(event) {
    if (event == null) {
        return false;
    }
    event = this.widget.getEvent(event);

    var target = (event.target)
        ? event.target 
        : ((event.srcElement) 
            ? event.srcElement : null);
    if (webui.suntheme4_2.browser.isFirefox()) {        
        if (this.contentEnd == target) {
            this.bubbleHeader.focus();
        } else if (this.bubbleHeader == target && this.focusId != null && (event.keyCode == 9)) {
            document.getElementById(this.focusId).focus();            
        }
        event.stopPropagation();
        event.preventDefault(); 
    }
    return true;
};
    
/**
 * Helper function to create callback for onMouseOver event.
 *
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onMouseOverCallback = function(event) {
    clearTimeout(this.timerId);
    return true;
};

/**
 * Helper function to create callback for onMouseOut event.
 *
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.onMouseOutCallback = function(event) {
    if (this.autoClose == true) {
        clearTimeout(this.timerId);            
        this.close();            
    }
    return true;
};

/**
 * This function is use to invoke buuble help.
 *
 * @param {Event} event The JavaScript event.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.open = function(event) {
    // Get the absolute position of the target.
    var evt = this.widget.getEvent(event);
    // A11Y - open the bubble if its Ctrl key + F1
    if (evt.type == "keydown") {
        if (!(evt.ctrlKey && (evt.keyCode == 112))) {
            return false;
        }
        evt.stopPropagation();
        evt.preventDefault();  
    }
    this.srcElm = (evt.target) 
        ? evt.target : ((evt.srcElement) 
            ? evt.srcElement : null);

    var absPos = this.widget.getPosition(this.srcElm);
    this.srcElm.targetLeft = absPos[0];
    this.srcElm.targetTop = absPos[1];
   
    if (this.timerId != null) {
        clearTimeout(this.timerId);
        this.timerId = null;
    }
    
    if (this.openDelay != null && this.openDelay >= 0) {
        this.openDelayTime = this.openDelay;
    }

    // There should be delay before opening the bubble if open delay is specified.
    // If openDelay is less than zero then there will be dafault 0.5 sec delay.  
    
    var id = this.id; // Closure magic.
    this.openTimerId = setTimeout(function() {
        // Store the active bubble id to form element.
        // Check for the id if its available then close the pending bubble.
        if (webui.suntheme4_2.widget.bubble.activeBubbleId && webui.suntheme4_2.widget.bubble.activeBubbleId != id) {                
            clearTimeout(webui.suntheme4_2.dijit.byId(webui.suntheme4_2.widget.bubble.activeBubbleId).timerId);
            webui.suntheme4_2.dijit.byId(webui.suntheme4_2.widget.bubble.activeBubbleId).setProps({visible: false});
            webui.suntheme4_2.widget.bubble.activeBubbleId = null;                
        }     
        webui.suntheme4_2.widget.bubble.activeBubbleId = id;            
        webui.suntheme4_2.dijit.byId(id).setProps({visible: true});
        webui.suntheme4_2.dijit.byId(id).setPosition();
    }, this.openDelayTime);           
    
    if (this.duration != null && this.duration >= 0) {
        this.defaultTime = this.duration;
    } 
    return true;
};

/**
 * This function is used to fill in remaining template properties, after the
 * buildRendering() function has been processed.
 * <p>
 * Note: Unlike Dojo 0.4, the DOM nodes don't exist in the document, yet. 
 * </p>
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.postCreate = function () {
    // Set ids.
    if (this.id) {
        this.bottomLeftArrow.id = this.id + "_bottomLeftArrow";
        this.bottomRightArrow.id = this.id + "_bottomRightArrow";
        this.topLeftArrow.id = this.id + "_topLeftArrow";
        this.topRightArrow.id = this.id + "_topRightArrow";
    }

    // Set public functions.
    this.domNode.close = function() { return webui.suntheme4_2.dijit.byId(this.id).close(); };
    this.domNode.open = function(event) { return webui.suntheme4_2.dijit.byId(this.id).open(event); };

    // Set events.

    // The onClick on window should close bubble.
    this.dojo.connect(document, "onclick", this, "onCloseCallback");

    // The escape key should also close bubble.
    this.dojo.connect(document, "onkeydown", this, "onCloseCallback");

    // The onClick event for component body. Closes the bubble only when
    // close button is clicked.
    this.dojo.connect(this.domNode, "onclick", this, "onClickCallback");

    // Do not close the popup if mouseover on bubble if mouseover on bubble 
    // component then clear the timer and do not close bubble.
    this.dojo.connect(this.domNode, "onmouseover", this, "onMouseOverCallback");

    // Close the popup if mouseout and autoClose is true if onmouseout and 
    // autoClose is true then close the bubble.
    this.dojo.connect(this.domNode, "onmouseout", this, "onMouseOutCallback");
    
    // The onfocus event for contentEnd. This is needed to handle tab event. 
    this.dojo.connect(this.contentEnd, "onfocus", this, "onTabCallback");
    
    // The onkeydown event for bubbleHeader. This is needed to handle tab event. 
    this.dojo.connect(this.bubbleHeader, "onkeydown", this, "onTabCallback");
    
    // The onkeydown event for component body. This is needed to handle shift+tab event.
    this.dojo.connect(this.domNode, "onkeydown", this, "onShftTabCallback");
    
    // Initialize the BubbleTitle width as a percentage of the bubble header.    
    if (this.bubbleTitle != null) {
        this.bubbleTitle.style.width = this.theme.getProperty("styles", 
            "BUBBLE_TITLEWIDTH") + "%";
    }
    return this.inherited("postCreate", arguments);
};

/**
 * This function is used to position the bubble.
 *
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.setPosition = function() {
    
    // THIS CODE BLOCK IS NECESSARY WHEN THE PAGE FONT IS VERY SMALL,
    // AND WHICH OTHERWISE CAUSES THE PERCENTAGE OF THE HEADER WIDTH
    // ALLOCATED TO THE BUBBLE TITLE TO BE TOO LARGE SUCH THAT IT
    // ENCROACHES ON THE SPACE ALLOCATED FOR THE CLOSE BUTTON ICON,
    // RESULTING IN LAYOUT MISALIGNMENT IN THE HEADER.

    // Assume BubbleTitle width max percentage of the bubble header.
    var maxPercent = this.theme.getProperty("styles", "BUBBLE_TITLEWIDTH");

    // Sum of widths of all elements in the header BUT the title.  This includes
    // the width of the close button icon, and the margins around the button and
    // the title.  This should be a themeable parameter that matches the left/right
    // margins specified in the stylesheet for "BubbleTitle" and "BubbleCloseBtn".
    var nonTitleWidth = this.theme.getProperty("styles", "BUBBLE_NONTITLEWIDTH");

    // Get the widths (in pixels) of the bubble header and title
    var headerWidth = this.bubbleHeader.offsetWidth;
    var titleWidth = this.bubbleTitle.offsetWidth;

    // Revise the aforementioned percentage downward until the title no longer
    // encroaches on the space allocated for the close button.  We decrement by
    // 5% each time because doing so in smaller chunks when the font gets very small so 
    // only results in unnecessary extra loop interations.
    //
    if (headerWidth > nonTitleWidth) {
        while ((maxPercent > 5) && (titleWidth > (headerWidth - nonTitleWidth))) {
            maxPercent -= 5;
            this.bubbleTitle.style.width = maxPercent + "%";
            titleWidth = this.bubbleTitle.offsetWidth;
        }
    }

    // Get DOM bubble object associated with this Bubble instance.
    var bubble = this.domNode;

    // If this.style is not null that means developer has specified positioning
    // for component. 
    if (this.domNode != null && this.style != null && this.style.length > 0) {
        if (bubble.style.length != null) {
            for (var i = 0; i < bubble.style.length; i++) {
                if (bubble.style[i] == "top") {
                    this.top = bubble.style.top;
                }
                if (bubble.style[i] == "left") {
                    this.left = bubble.style.left;
                }
            }
        } else {
            // For IE, simply query the style attributes.
            if (bubble.style.top != "") {
                this.top = bubble.style.top;
            }
            if (bubble.style.left != "") {
                this.left = bubble.style.left;
            }
        }
    }

    if ((this.top != null) && (this.left != null)) {
        bubble.style.left = this.left;
        bubble.style.top = this.top;    
    } else {        

        var topLeftArrow = document.getElementById(this.topLeftArrow.id);
        var topRightArrow = document.getElementById(this.topRightArrow.id);
        var bottomLeftArrow = document.getElementById(this.bottomLeftArrow.id);
        var bottomRightArrow = document.getElementById(this.bottomRightArrow.id);
        // hide all callout arrows.
        this.common.setVisible(bottomLeftArrow, false);
        this.common.setVisible(bottomRightArrow, false);
        this.common.setVisible(topLeftArrow, false);
        this.common.setVisible(topRightArrow, false);

        bottomLeftArrow.style.display = "none";
        bottomRightArrow.style.display = "none";
        topLeftArrow.style.display = "none";
        topRightArrow.style.display = "none";

        var slidLeft = false;

        // Assume default bubble position northeast of target, which implies a 
        // bottomLeft callout arrow
        this.arrow = bottomLeftArrow;

        // Try to position bubble to right of srcElm.
        var bubbleLeft = this.srcElm.targetLeft + this.srcElm.offsetWidth + this.bubbleLeftConst;

        // Check if right edge of bubble exceeds page boundary.
        var rightEdge = bubbleLeft + bubble.offsetWidth;
        if (rightEdge > this.widget.getPageWidth()) {

            // Shift bubble to left side of target;  implies a bottomRight arrow.
            bubbleLeft = this.srcElm.targetLeft - bubble.offsetWidth;
            this.arrow = bottomRightArrow;
            slidLeft = true;

            // If left edge of bubble crosses left page boundary, then
            // reposition bubble back to right of target and implies to go
            // back to bottomLeft arrow.  User will need to use scrollbars
            // to position bubble into view.
            if (bubbleLeft <= 0) {
                bubbleLeft = this.srcElm.targetLeft + this.srcElm.offsetWidth + this.bubbleLeftConst;
                this.arrow = bottomLeftArrow;
                slidLeft = false;
            }
        }

        // Try to position bubble above source element
        var bubbleTop = this.srcElm.targetTop - bubble.offsetHeight;

        // Check if top edge of bubble crosses top page boundary
        if (bubbleTop <= 0) {
            // Shift bubble to bottom of target.  User may need to use scrollbars
            // to position bubble into view.
            bubbleTop = this.srcElm.targetTop + this.srcElm.offsetHeight + this.bubbleLeftConst;

            // Use appropriate top arrow depending on left/right position.
            if (slidLeft == true)
                this.arrow = topRightArrow;
            else
                this.arrow = topLeftArrow;
        }

        // Set new bubble position.
        bubble.style.left = bubbleLeft + "px";
        bubble.style.top = bubbleTop + "px";

        // If rendering a callout arrow, set it's position relative to the bubble.
        if (this.arrow != null) {
           this.arrow.style.display = "block";
           this.common.setVisible(this.arrow, true);

           if (this.arrow == topLeftArrow) {
               this.arrow.style.top = -(bubble.offsetHeight - this.topConst) + "px";               
           }
           if (this.arrow == topRightArrow) {
               this.arrow.style.top = -(bubble.offsetHeight - this.topConst) + "px";               
           }
        }
    }
    if (this.focusId != null) {
        document.getElementById(this.focusId).focus();        
    } else {
        if (webui.suntheme4_2.browser.isFirefox()) {
            this.bubbleHeader.focus();
        }
    }
    return true;
};

/**
 * This function is used to set widget properties using Object literals.
 * <p>
 * Note: This function extends the widget object for later updates. Further, the
 * widget shall be updated only for the given key-value pairs.
 * </p><p>
 * If the notify param is true, the widget's state change event shall be
 * published. This is typically used to keep client-side state in sync with the
 * server.
 * </p>
 *
 * @param {Object} props Key-Value pairs of properties.
 * @config {boolean} autoClose 
 * @config {Object} closeButton 
 * @config {Array} contents 
 * @config {int} duration 
 * @config {String} id Uniquely identifies an element within a document.
 * @config {int} openDelay 
 * @config {String} title Provides a title for element.
 * @config {int} width 
 * @config {boolean} visible Hide or show element.
 * @param {boolean} notify Publish an event for custom AJAX implementations to listen for.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.widget.bubble.prototype.setProps = function(props, notify) {
    if (props == null) {
        return false;
    }
    
    // Replace contents -- do not extend.
    if (props.contents) {
        this.contents = null;
    }

    // Extend widget object for later updates.
    return this.inherited("setProps", arguments);
};

/**
 * This function is used to set widget properties. Please see the setProps() 
 * function for a list of supported properties.
 * <p>
 * Note: This function should only be invoked through setProps().
 * </p>
 * @param {Object} props Key-Value pairs of properties.
 * @return {boolean} true if successful; otherwise, false.
 * @private
 */
webui.suntheme4_2.widget.bubble.prototype._setProps = function(props) {
    if (props == null) {
        return false;
    }
    //Cyclic focus behavior is supported for firefox browser only.
    //If tabIndex values are provided for elements inside bubble then developer needs to set a valid tabIndex 
    //value for bubble component to achieve cyclic focus behavior. 
    if (webui.suntheme4_2.browser.isFirefox()) {
        if (this.getProps().tabIndex >= 0) {
            this.contentEnd.tabIndex = this.getProps().tabIndex;
        } else {
            this.contentEnd.tabIndex = 0;
        }   
    }
    // Set title.
    if (props.title) {
        this.widget.addFragment(this.titleNode, props.title);
    }

    // hide/display close button
    if (props.closeButton != null) {
        var classNames = this.closeBtn.className.split(" ");
        var closeButtonClass = this.theme.getClassName("BUBBLE_CLOSEBTN");
        var noCloseButtonClass = this.theme.getClassName("BUBBLE_NOCLOSEBTN");

        if (props.closeButton == false) {
            this.common.stripStyleClass(this.closeBtn, closeButtonClass);
            if (!this.common.checkStyleClasses(classNames, noCloseButtonClass))
             this.common.addStyleClass(this.closeBtn, noCloseButtonClass);
        } else {          
          if (!this.common.checkStyleClasses(classNames, closeButtonClass))
             this.common.addStyleClass(this.closeBtn, closeButtonClass);
        }
    }

    // Set width.
    if (props.width > 0) {                    
        this.domNode.style.width = props.width + "px";        
    }

    // Set contents.
    if (props.contents) {
        // Remove child nodes.
        this.widget.removeChildNodes(this.childNode);

        for (var i = 0; i < props.contents.length; i++) {
            this.widget.addFragment(this.childNode, props.contents[i], "last");
        }
    }

    // Set remaining properties.
    return this.inherited("_setProps", arguments);
};
