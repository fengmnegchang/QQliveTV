/*****************************************************************************/
/*************************** MRAID bridge ************************************/
/** MRAID javascript bridge called mraidview for the communication between   */
/** mraid javascript controller and sdk, and indirectly, with the application*/
/** and the device.                                                          */
/** Author: rockychen                                                        */
/*****************************************************************************/

(function(){
    /** The mraidview object */
    var mraidstub = window.mraidview = {};
	
    mraidstub.close = mraidstub.createCalendarEvent = mraidstub.expand = mraidstub.getParams = mraidstub.getRichdata = mraidstub.videoSeek = mraidstub.getProgress = mraidstub.getUrlsForVids = mraidstub.getUserKey
	 = mraidstub.mraidLoaded = mraidstub.open = mraidstub.pause = mraidstub.playVideo = mraidstub.removeRichAd = mraidstub.resize = mraidstub.resume = mraidstub.setObjectViewable = mraidstub.setOrientationProperties
	 = mraidstub.shareToWXFriend = mraidstub.shareToWXTimeLine = mraidstub.showSharePanel = mraidstub.storePicture = mraidstub.useCustomClose = mraidstub.viewMore = mraidstub.stageReady = mraidstub.richMediaViewPing
	 = mraidstub.richMediaArea = mraidstub.skipAd = mraidstub.cancelSplashAdCountdown = mraidstub.vibrate = mraidstub.showLoginPanel = mraidstub.getLoginStatus = mraidstub.startMicrophone = mraidstub.endMicrophone
	 = mraidstub.startMic = mraidstub.endMic= mraidstub.callForMotionEvent = mraidstub.hasApp = mraidstub.openApp = mraidstub.scanQRCode = function(){};
	
    /** The set of listeners for MRAID Native Bridge Events. */
    var listeners = {};
	
    var mIsDestroyed = false;
	
    /** Stringify an object. */
    var stringify = mraidstub.stringify = function(obj) {
    	if (typeof obj === 'object') {
    		var out = [];
    		// for Array
    		if (obj instanceof Array) {
    			for (var p in obj) {
    				out.push(obj[p]);
    			}			
    			return '[' + out.join(',') + ']';
    		} else {
    			// for other object
    			for (var p in obj) {
    				if (typeof obj[p] == 'string') {
    					out.push("'" + p + "': '" + obj[p] + "'");
    				} else {
    					out.push("'" + p + "': " + obj[p]);
    				}
    				
    			}
    			return '{' + out.join(',') + '}';
    		}
    	} else {
    		return String(obj);
    	}
    };
    
    /** Called by the Android SDK when various events occured. */
    mraidstub.fireEvent = function() {
    	var args = new Array(arguments.length);
    	for (var i = 0, len = arguments.length; i < len; i++) {
    		args[i] = arguments[i];
    	}
    	var event = args.shift();
    	
    	for (var id in listeners[event]) {
    		var handler = listeners[event][id];
        	handler.apply({}, args);
        }
    };

    /**
    * Subscribes a specific handler method to a specific event.
    * 
    * @param event - string, name of event to listen for
    * @param listener - function to execute
    * @returns none
    */
    mraidstub.addEventListener = function(event, listener) {
    	var handlers = listeners[event];
    	if (handlers == null) {
    		// no handlers defined yet, set it up
    		listeners[event] = [];
    		handlers = listeners[event];
    	}
    	// see if the listener is already present
    	for (var handler in handlers) {
    		if (listener == handler) {
    			// listener already present, nothing to do
    			return;
    		}
    	}
    	// not present yet, go ahead and add it
    	handlers.push(listener);
    };
    
    /**
    * Unsubscribes a specific handler method from a specific event.
    * 
    * @param event - string, name of event to listen for
    * @param listener - function to be removed
    * @returns none 
    */
    mraidstub.removeEventListener = function(event, listener) {
		var handlers = listeners[event];
		if (handlers != null) {
			var idx = handlers.indexOf(listener);				
			if(idx !== -1) {
				handlers.splice(idx, 1);
			}
		}
    };
    
    var MraidBridge = window.MraidBridge = {
        queue: [],
        invokeJSCallback: function(){
            var d = Array.prototype.slice.call(arguments, 0);
            var c = d.shift();
            var e = d.shift();
            this.queue[c].apply(this, d);
            if (!e) {
                delete this.queue[c]
            }
        }
    };
	
    MraidBridge.invokeAndroid = function(){
        var f = Array.prototype.slice.call(arguments, 0);
        var e = [];
        for (var h = 1; h < f.length; h++) {
            var c = f[h];
            var j = typeof c;
            if (j == "object" && c instanceof Array) {
                e[e.length] = "array";
            }
            else if(c == null || j == undefined){
                f[h] = null;
                e[e.length] = "object";
            }
            else {
                e[e.length] = j;
            }
            if (j == "function") {
                var d = MraidBridge.queue.length;
                MraidBridge.queue[d] = c;
                f[h] = d
            }
        }
        var methodname = f.shift();
        var exp = JSON.stringify(({
            method: methodname,
            types: e,
            args: f
        }));
        if(mIsDestroyed) {
                throw "MraidBridge call(" + exp + ") error : webview is destroyed"
        }
        var g = JSON.parse(prompt(exp));
        if (g.code != 200) {
            throw "MraidBridge call(" + exp + ") error with code:" + g.code + ", message:" + g.result
        }
        return g.result
    };
	
    Object.getOwnPropertyNames(mraidstub).forEach(function(d) {
        var c = mraidstub[d];
        if (typeof c === "function" && d != "stringify" && d != "fireEvent" && d != "addEventListener" && d != "removeEventListener") {
            mraidstub[d] = function() {
                return MraidBridge.invokeAndroid.apply(mraidstub, [d].concat(Array.prototype.slice.call(arguments, 0)))
            }
        }
    });
	
    mraidstub.addEventListener('destroyed', function() {
            mIsDestroyed = true;		
    });
	
    mraidstub.addEventListener('ready', function() {
            mIsDestroyed = false;		
    });
})();


/*****************************************************************************/
/*************************** Mraid controller ********************************/
/** Provides ad designers access to MRAID methods and events.                */
/** The ad creative uses the controller to perform advertising-related       */
/** interactions with the Ad Container.                                      */
/*****************************************************************************/

(function() {
	/** The mraid controller object. */
	var mraid = window.mraid = {};
	
	/** The current mraid version that sdk supports. */
	var VERSIONS = mraid.VERSIONS = {
		V1: '1.0',
		V2: '2.0'
	};
	
	/** The events supported by MRAID v.2 */
	var EVENTS = mraid.EVENTS = {
		READY: 'ready',
		ERROR: 'error',
		STATE_CHANGE: 'stateChange',
		VIEWABLE_CHANGE: 'viewableChange',
		SIZE_CHANGE: 'sizeChange',
		INFO: 'info'	
	};
	
	/** The state of the ad container. */ 
    var STATES = mraid.STATES = {
        LOADING: 'loading', // initial state
        DEFAULT: 'default',     
        EXPANDED: 'expanded',
        RESIZED: 'resized',
        HIDDEN: 'hidden'
    };
    
    /** The type of ad. */
    var PLACEMENT_TYPES = mraid.PLACEMENT_TYPES = {
    	UNKNOWN: 'unknown',
    	INLINE: 'inline',
    	INTERSTITIAL: 'interstitial'
	};
	
	/** The additional features that sdk supports */
  	var supportFeatures = {
  		sms: false,
  		tel: false,
  		calendar: false,
  		storePicture: false,
  		inlineVideo: false
  	};
  	
	/** Properties that ad to be expanded. */
  	var expandProperties = {
    	width: -1, // full screen
    	height: -1, // full screen
    	useCustomClose: false,
    	isModal: true // always true for mraid 2.0
	};
	
	/** Orientation properties providing additional control over expandable or interstitial ad. */
	var orientationProperties = {
		allowOrientationChange: true, // default is true
		forceOrientation: "none" // value can be "portrait|landscape|none", default is "none"
	};
	
	/** Properties that ad to be resized. */
	var resizeProperties = {
		width: 0,
		height: 0,
		customClosePosition: 'top-right', // (optional) string – either "top-left", "top-right", "center", 
										  // "bottom-left", "bottom-right," “top-center,” or “bottom-center”
		offsetX: 0,
		offsetY: 0,
		allowOffscreen: true
	};
	
	/** an array that contains the valid positions of resize close region. */
	var closePositionArray = ['top-left', 'top-right', 'bottom-left',
				'bottom-right', 'center', 'top-center', 'bottom-center'];

	/** Current position and size of the ad view. */
	var currentPosition = {
		x: 0,
		y: 0,
		width: 0,
		height: 0
	};
	
	/** The default position and size of the ad view. */
	var defaultPosition = {
		x: 0,
		y: 0,
		width: 0,
		height: 0
	};
	
	/** The actual density-inddependent-pixel of width and height of the device. */
	var screenSize = {
		width: 0,
		height: 0
	};
	
	/** The maximum width and height the view can resize/expand to. */
	var maxSize = {
		width: 0,
		height: 0
	};	
	
	/** app context. */
	var appContext;
	var apiSupports;
	
    var mState = STATES.LOADING;
    var mPlacementType = PLACEMENT_TYPES.UNKNOWN;
	var mIsViewable = false;
	var useCustomClose = false;
	var hasSetResizeProperties = false; // indicates that resizeproperties has been set or not
	
	var listeners = {};
	
	/** the mraid event listener */
	var EventListeners = function(event) {
        this.event = event;
        this.count = 0;
        var listeners = {};
        
        this.add = function(func) {
            var id = String(func);
            if (!listeners[id]) {
                listeners[id] = func;
                this.count++;
            }
        };
        
        this.remove = function(func) {
        	
            var id = String(func);
            if (listeners[id]) {
                listeners[id] = null;
                delete listeners[id];
                this.count--;
                return true;
            } else {
                return false;
            }
        };
        
        this.removeAll = function() {
        	for (var id in listeners) {
        		this.remove(listeners[id]);
        	}
        };
        
        this.broadcast = function(args) {
        	for (var id in listeners) {
        		listeners[id].apply({}, args);
        	}
        };
        
        this.toString = function() {
            var out = [event, ':'];
            for (var id in listeners) {
            	out.push('|', id, '|');
            }
            return out.join('');
        };
    };
	
	/**********************************/
	/**********COMMON METHODS**********/
	/**********************************/	
	
    /** Stringify an object, copied the method from mraidview. */
    var stringify = mraid.stringify = mraidview.stringify;
    
	/**
	 * Checks that the object contains the value or not. If checkElem is set to true,
	 * It checks the object containing the element name or not.
	 * @param value - the value given
	 * @param object/array
	 * @param checkElem - boolean value, true: compare the element name and given value
	 * @returns boolean value 
	 */
	var contains = function(value, obj, checkElem) {
		if (!checkElem) {
			for (var i in obj) {
				if (obj[i] == value) {
					return true;
				}
			}
		} else {
			// for checking the element name equals the given value or not
			for (var i in obj) {
				if (i == value) {
					return true;
				}
			}
		}
		
		return false;
	};
	
	/**
     * Broadcasts the event.
     * @returns none
     */
    var broadcastEvent = function() {
    	var args = new Array(arguments.length);
    	for (var i = 0, len = arguments.length; i < len; i++) {
    		args[i] = arguments[i];
    	}
    	var event = args.shift();
    	try {
    		if (listeners[event]) {
    			listeners[event].broadcast(args);
    		}
    	} catch (e) {}
    };
    
    /**
     * Validates the obj with validators
     * @param obj - the object to be validated
     * @param validators - the object, which is the corresponding validator
     * @param action - string the function name which uses this method, for logging
     * @param merge - boolean value, if true, the obj should have all properties of validator 
     * @returns boolean value, true if pass, and vice verse
     */
    var validate = function(obj, validators, action, merge) {
        if (!merge) {
            if (obj === null) {
                broadcastEvent(EVENTS.ERROR, 'Required object missing.', action);
                return false;
            } else {
                for (var i in validators) {
                    if (validators.hasOwnProperty(i) && obj[i] === undefined) {
                        broadcastEvent(EVENTS.ERROR, 'Object missing required property ' + i, action);
                        return false;
                    }
                }
            }
        }
        
        for (var i in obj) {
            if (!validators[i]) {
                broadcastEvent(EVENTS.ERROR, 'Invalid property specified - ' + i + '.', action);
                return false;
            } else if (!validators[i](obj[i])) {
                broadcastEvent(EVENTS.ERROR, 'Value of property ' + i + '<' + obj[i] + '>' +' is not valid type.', action);
                return false;
            }
        }
        return true;
    };
    
    /**
	 * Does the shallow copy from an object
	 * @param {} p the object to be copied
	 * @returns {} a new object
	 */
	var shallowCopy = function(p) {
		var c = {};
		
		for (var i in p) {
			c[i] = p[i];
		}
		
		return c;
	};
	
	var mergeObject = function(fromObj, toObj) {
		for (var i in fromObj) {
			toObj[i] = fromObj[i];
		}
	};
	
    /**
     * Handles the ready event from SDK, which is triggers when the container
     * is fully loaded, initialized, and ready for any calls from the ad creative.
     */
	mraidview.addEventListener(EVENTS.READY, function() {
        broadcastEvent(EVENTS.READY);
        broadcastEvent(EVENTS.INFO, "mraid is ready");
    });
    
	/**
	 * Handles the error event, which is triggered whenever a container error occurs.
	 */
	mraidview.addEventListener(EVENTS.ERROR, function(message, action) {
        broadcastEvent(EVENTS.ERROR, message, action);
    });
    
    /**
     * Handles the stateChange event, which is triggered when the Ad View changes from SDK.
     */
	mraidview.addEventListener(EVENTS.STATE_CHANGE, function(state) {
		mState = state;
        broadcastEvent(EVENTS.STATE_CHANGE, mState);
        broadcastEvent(EVENTS.INFO, "state changed to " + state);
    });
    
    /**
     * Handles the viewableChange event from SDK, which is triggered when the 
     * ad moves from on-screen to off-screen and vice versa.
     */
	mraidview.addEventListener(EVENTS.VIEWABLE_CHANGE, function(isViewable) {
		mIsViewable = isViewable;		
        broadcastEvent(EVENTS.VIEWABLE_CHANGE, mIsViewable);
        broadcastEvent(EVENTS.INFO, "viewable changed to " + isViewable);
    });
    
    /**
     * Handles the sizeChange event from SDK, which is triggered when the
     * ad’s size within the app UI changes.
     */
	mraidview.addEventListener(EVENTS.SIZE_CHANGE, function(width, height) {
        broadcastEvent(EVENTS.SIZE_CHANGE, width, height);
        broadcastEvent(EVENTS.INFO, "size changed to width:" + width + ", height:" + height);
    });
    
    /** Handles the other property event from SDK. */
    mraidview.addEventListener('property', function(properties) {
        for (var property in properties) {
            var handler = propertyHandlers[property];
            handler(properties[property]);
        }
    });
    
    /**
     * Handles other response of the events fired from SDK.
     */
    var propertyHandlers = {
        placementType: function(val) {
        	mPlacementType = val;
        	broadcastEvent(EVENTS.INFO, "setting placementType to " + val);
        },
        setMaxSize: function(val) {
        	var obj = eval('(' + val + ')');
        	setMaxSize(obj);
        },
        setCurrentPosition: function(val) {
        	var obj = eval('(' + val + ')');
        	setCurrentPosition(obj);
        },
        setDefaultPosition: function(val) {
        	var obj = eval('(' + val + ')');
        	setDefaultPosition(obj);
        },
        setScreenSize: function(val) {
        	var obj = eval('(' + val + ')');
        	setScreenSize(obj);
        },
        setSupports: function(val) {
        	var obj = eval('(' + val + ')');
        	setSupports(obj);
        },
        setAppContext: function(val) {
        	var obj = eval('(' + val + ')');
        	setAppContext(obj);
        },
        setApiSupports: function(val) {
        	setApiSupports(val);
	}
	};
	
    // Sets the app context
    var setAppContext = function(obj){
        apiSupports['getAppContext'] = true;
        apiSupports['getAndroidVersion'] = true;
        appContext = obj;
        broadcastEvent(EVENTS.INFO, "setting appcontext to  " + stringify(appContext));
    };
	
    // Sets api supports
    var setApiSupports = function(obj){
        var apis = obj.split(',');
        apiSupports = {};
        for (var i = 0; i < apis.length; i++) {
            apiSupports[apis[i]] = true;
        }
        broadcastEvent(EVENTS.INFO, "setting api supports to  " + apiSupports);
    };
	
	// Sets the maxSize properties
	var setMaxSize = function(obj) {
		maxSize.width = obj.width;
        maxSize.height = obj.height;
        broadcastEvent(EVENTS.INFO, "setting maxSize to " + stringify(obj));
	};
	
	// Sets the screenSize properties
	var setScreenSize = function(obj) {
		screenSize.width = obj.width;
        screenSize.height = obj.height;
        broadcastEvent(EVENTS.INFO, "setting screenSize to " + stringify(obj));
	};
	
	// Sets the supports properties
	var setSupports = function(obj) {
		supportFeatures.sms = obj.sms;
        supportFeatures.tel = obj.tel;
        supportFeatures.calendar = obj.calendar;
        supportFeatures.storePicture = obj.storePicture;
        supportFeatures.inlineVideo = obj.inlineVideo;    
        broadcastEvent(EVENTS.INFO, "setting supportFeatures to " + stringify(obj));
	};
	
	// Sets the currentPosition properties
	var setCurrentPosition = function(obj) {
		currentPosition.x = obj.x;
		currentPosition.y = obj.y;
		currentPosition.width = obj.width;
		currentPosition.height = obj.height;
		broadcastEvent(EVENTS.INFO, "setting currentPosition to " + stringify(obj));
	};
	
	// Sets the defaultPosition properties
	var setDefaultPosition = function(obj) {
		defaultPosition.x = obj.x;
		defaultPosition.y = obj.y;
		defaultPosition.width = obj.width;
		defaultPosition.height = obj.height;
		broadcastEvent(EVENTS.INFO, "setting defaultPosition to " + stringify(obj));
	};
	
	// The validators for expand properties.
	var expandPropertyValidators = {
		width: function(v) { return !isNaN(v) && v >= 0; },
		height: function(v) { return !isNaN(v) && v >= 0; },
		useCustomClose: function(v) { return (typeof v === 'boolean'); },
		isModal: function(v) { return (typeof v === 'boolean') && v == true; }
	};
	
	// The validators for orientation properties.
	var orientationPropertyValidators = {
		allowOrientationChange: function(v) { return (typeof v === 'boolean'); },
		forceOrientation: function(v) { return (typeof v === 'string') && (v == 'portrait' || v == 'landscape' || v == 'none'); }
	};
    
    // The validators for resize properties.
    var resizePropertyValidators = {
    	width: function(v) { return !isNaN(v) && v >= 0 && v <= maxSize.width; },
		height: function(v) { return !isNaN(v) && v >= 0 && v <= maxSize.height; },
		customClosePosition: function(v) { return (typeof v === 'string') 
											&& contains(v, closePositionArray); },
		offsetX: function(v) { return !isNaN(v) },
		offsetY: function(v) { return !isNaN(v) },
		allowOffscreen: function(v) { return (typeof v === 'boolean'); }
    };
    
    /********************************************************************/
	/** Public methods and events of mraid version 2.0 for ad designers */
	/********************************************************************/
	
	/**
	 * Returns the version of MRAID specification in use.
	 * 
	 * @returns String - the MRAID version that this SDK is certified against by the IAB,
	 * or that this SDK is compliant with. e.g. the current version is '2.0'
	 */
	mraid.getVersion = function() {
		return VERSIONS.V2;
	};
	
	/**
	 * Subscribes a specific handler method to a specific event. In this way,
	 * multiple listeners can subscribe to a specific event, and a single
	 * listener can handle multiple events.
	 * 
	 * @param event - string, name of event to listen for
	 * @param listener - function to execute
	 * @returns none 
	 */
    mraid.addEventListener = function(event, listener) {
		if (!event || !listener) {
			broadcastEvent(EVENTS.ERROR, 'Both event and listener are required.', 'addEventListener');
		} else if (!contains(event, EVENTS)) {
			broadcastEvent(EVENTS.ERROR, 'Unknown event: ' + event, 'addEventListener');
        } else {
            if (!listeners[event]) {
            	listeners[event] = new EventListeners(event);
            }
            listeners[event].add(listener);
        }	
	};

	/**
	 * Unsubscribes a specific handler method from a specific event.
	 * If no listener function is specified, then all functions listening to
	 * the event will be removed.
	 * 
	 * @param event - string, name of event
	 * @param listener - function to be removed
	 * @returns none
	 */
	mraid.removeEventListener = function(event, listener) {
        if (!event) {
            broadcastEvent(EVENTS.ERROR, 'Must specify an event.', 'removeEventListener');
        } else if (!listeners[event]) {
        	broadcastEvent(EVENTS.ERROR, 'Event "' + event + '" not subscribed yet.', 'removeEventListener');
        } else if (!listener) {
        	listeners[event].removeAll();
        } else {
        	if (!listeners[event].remove(listener)) {
        		broadcastEvent(EVENTS.ERROR, 'Listener not currently registered for event', 'removeEventListener');
        	} else {
        		// removed successfully
        		broadcastEvent(EVENTS.INFO, event + " is removed.");
        	}
        	
			if (listeners[event].count == 0) {
                listeners[event] = null;
                delete listeners[event];
            }
        }
    };
    
    /**
	 * Returns the current state of the ad container.
	 * 
	 * @returns String: "loading", "default", "expanded”, “resized,” or “hidden” 
	 */
	mraid.getState = function() {
		return mState;
	};
	
	/**
	 * Returns the placement of ad that it initially displayed in.
	 * 
	 * @returns String: "inline", "interstitial"
	 */
	mraid.getPlacementType = function() {
		return mPlacementType;
	};
	
	/**
	 * Returns whether the ad container is currently on or off the screen.
	 * 
	 * @returns boolean - true: container is on-screen and viewable by the user;
	 * false: container is off-screen and not viewable
	 */
	mraid.isViewable = function() {
		return mIsViewable;
	};
	
	/**
	 * Displays an embedded browser window in the application that loads an external URL.
	 * 
	 * @param url - string, url to be opened
	 * @returns none 
	 */
	mraid.open = function(url) {
		if (!url) {
			broadcastEvent(EVENTS.ERROR, 'URL is required.', 'open');
		} else {
			mraidview.open(url);
		}		
	};

	/**
	 * Expands the ad. If url is set, it opens a new view.
	 * 
	 * @param String url
	 * @returns none 
	 */
	mraid.expand = function(url) {
		if (mPlacementType == PLACEMENT_TYPES.INTERSTITIAL) {
			broadcastEvent(EVENTS.ERROR, 'An interstitia ad can not be expanded.', 'expand');
			return;
		}
		
		if (mState == STATES.DEFAULT || mState == STATES.RESIZED) {
			if (!url) {
				url = null;
			}
			var propertiesStr = stringify(expandProperties);			
			mraidview.expand(propertiesStr, url);		
		} else {		
			broadcastEvent(EVENTS.ERROR, 'Ad can only be expanded from the default/resized state.', 'expand');
		}
	};
	
	/**
	 * Returns the whole JavaScript Object expandProperties object.
	 * 
	 * @returns object -  expandProperties
	 */
	mraid.getExpandProperties = function() {
		var properties = shallowCopy(expandProperties);
		
		return properties;
	};
	
	/**
	 * Sets the expand properties.
	 * 
	 * @param properties - the expand properties to be set
	 * @returns none
	 */
	mraid.setExpandProperties = function(properties) {
		if (validate(properties, expandPropertyValidators, "setExpandProperties", true)) {
			for (var i in properties) {
				expandProperties[i] = properties[i];
			}
			
			broadcastEvent(EVENTS.INFO, 'set expandProperties to ' + stringify(properties));
		}		
	};
	
	/**
	 * Returns the whole JavaScript Object orientationProperties object.
	 * 
	 * @returns object - orientationProperties
	 */
	mraid.getOrientationProperties = function() {
		var properties = shallowCopy(orientationProperties);
		
		return properties;
	};
	
	/**
	 * Sets the orientation properties.
	 * 
	 * @param properties - the orientation properties to be set
	 * @returns none 
	 */
	mraid.setOrientationProperties = function(properties) {
		if (validate(properties, orientationPropertyValidators, "setOrientationProperties", true)) {
			for(var i in properties) {
				orientationProperties[i] = properties[i];
			}
			var propertiesStr = stringify(orientationProperties);
			mraidview.setOrientationProperties(propertiesStr);
			broadcastEvent(EVENTS.INFO, 'set orientationProperties to ' + stringify(properties));
		}
		
	};
	 
	/**
	 * Closes the ad view.
	 * 
	 * @returns none 
	 */
	mraid.close = function() {
		if (mState === STATES.HIDDEN) {
	      broadcastEvent(EVENTS.ERROR, 'Ad cannot be closed when it is already hidden.', 'close');
	    } else {
	    	mraidview.close();
	    }		
	};
	
	/**
	 * Sets the close indicator. If true, ad designer should provide a custom indicator
	 * calling close method. If false, the default close indicator will be used.
	 * 
	 * @param boolean customClose
	 * @returns none 
	 */
	mraid.useCustomClose = function(customClose) {
		expandProperties.useCustomClose = customClose;
		useCustomClose = customClose;
		mraidview.useCustomClose(customClose);
		broadcastEvent(EVENTS.INFO, 'set useCustomClose to ' + customClose);
	};
	
	/**
	 * The ad must set the related parameters via setResizeProperties method 
	 * BEFORE attemping to call this method.
	 * 
	 * @returns none
	 */
	mraid.resize = function() {
		if (!hasSetResizeProperties) {
			broadcastEvent(EVENTS.ERROR, 'resize properties must be set before using resize.', 'resize');
			return;
		} 
		
		if (mPlacementType == PLACEMENT_TYPES.INTERSTITIAL) {
			broadcastEvent(EVENTS.ERROR, 'An interstitia ad can not be resized.', 'expand');
			return;
		}
		
		if (mState == STATES.DEFAULT || mState == STATES.RESIZED) {
			var propertiesStr = stringify(resizeProperties);
			mraidview.resize(propertiesStr);		
		} else {
			broadcastEvent(EVENTS.ERROR, 'Ad can ONLY be resized in the default/resized state.', 'resize');
		}		
	};
	
	/**
	 * Returns the whole JavaScript Object resizeProperties object.
	 * 
	 * @returns object - resizeProperties
	 */
	mraid.getResizeProperties = function() {
		var properties = shallowCopy(resizeProperties);
		
		return properties;
	};
	
	/**
	 * Sets the resize properties.
	 * @param properties - the resize properties to be set
	 * @returns none 
	 */
	mraid.setResizeProperties = function(properties) {
		if (validate(properties, resizePropertyValidators, "setResizeProperties", true)) {
			for(var i in properties) {
				resizeProperties[i] = properties[i];
			}
			
			hasSetResizeProperties = true;			
			broadcastEvent(EVENTS.INFO, 'set resizeProperties to ' + stringify(properties));
		}		
	};
		
	/**
	 * Returns the current position and size of the ad view.
	 * 
	 * @returns object - currentPosition
	 */
	mraid.getCurrentPosition = function() {
		var position = shallowCopy(currentPosition);
		
		return position;
	}
	
	/**
	 * Returns the position and size of the default ad view.
	 * 
	 * @returns object - defaultPosition
	 */
	mraid.getDefaultPosition = function() {
		var position = shallowCopy(defaultPosition);
		
		return position;
	};
	
	/**
	 * Returns the maximum size (in density-independent pixel width and height)
	 * an ad can expand or resize to.
	 * 
	 * @returns object - maxSize
	 */
	mraid.getMaxSize = function() {
		var size = shallowCopy(maxSize);
		
		return size;
	}
	
	/**
	 * Returns the current actual pixel width and height, based on the current
	 * orientation, in density-independent pixels.
	 * 
	 * @returns object - screenSize
	 */
	mraid.getScreenSize = function() {
		var size = shallowCopy(screenSize);
		
		return size;
	};
	

	
	/**
	 * Checks the feature is supported or not by SDK.
	 * 
	 * @returns boolean - true, supported and vice verse
	 */
	mraid.supports = function(feature) {
		if (contains(feature, supportFeatures, true)) {
			return supportFeatures[feature];
		} else {
			broadcastEvent(EVENTS.ERROR, 'No such feature: ' + feature, 'supports');
			return false;
		}		
	};
	
	/**
	 * Places a picture in the device's photo album.
	 * 
	 * @param URI - string, the url of the picture to be stored 
	 * @returns none
	 */
	mraid.storePicture = function(URI) {
		if (!supportFeatures.storePicture) {
			broadcastEvent(EVENTS.ERROR, 'storePicture is not supported.', 'storePicture');
		} else {
			if (URI) {
				mraidview.storePicture(URI);
			} else {
				broadcastEvent(EVENTS.ERROR, 'uri is missing or invalid.', 'storePicture');
			}	
		}
		
	};
	
	/**
	 * Opens the device UI to create a new calendar event.
	 * 
	 * @param parameters none
	 * @returns none
	 */
	mraid.createCalendarEvent = function(parameters) {
		if (!supportFeatures.calendar) {
			broadcastEvent(EVENTS.ERROR, 'createCalendarEvent is not supported.', 'createCalendarEvent');
		} else {
			var propertiesStr = stringify(parameters);
			mraidview.createCalendarEvent(propertiesStr);	
		}
		
		
	};
	
	/**
	 * Plays a video on the device via the device’s native, external player.
	 * 
	 * @param URI - String, the URI of the video or video stream
	 * @returns none 
	 */
	mraid.playVideo = function(URI) {
		if (URI) {
			mraidview.playVideo(URI);
		} else {
			broadcastEvent(EVENTS.ERROR, 'uri is missing or invalid.', 'playVideo');
		}	
	};
	

	var extend = mraid.extend = {};
	
	var networkStatus = {};
	networkStatus.status = 'offline';
	
	extend.EVENTS = {
		LOGIN_STATUS_CHANGE : 'loginStatusChange',
		DURATION_CHANGE: 'durationChange',
		APPLICATION_RESIGN_ACTIVE: 'applicationResignActive',
		LANDING_PAGE_DISMISS: 'landingPageDismiss',
		APPLICATION_BECOME_ACTIVE: 'applicationBecomeActive',
		NETWORK_STATUS_CHANGE: 'networkStatusChange'	
	};
	
	mraidview.addEventListener(extend.EVENTS.NETWORK_STATUS_CHANGE, function(status) {
		networkStatus.status = status;
        apiSupports['getNetworkStatus'] = true;
        broadcastEvent(extend.EVENTS.NETWORK_STATUS_CHANGE, status);
        broadcastEvent(EVENTS.INFO, "mraid network status change: " + status);
	});
	
	extend.apiSupports = function(apis){
        var results = {};
        for (var i = 0; i < apis.length; i++) {
            var api = apis[i];
            results[api] = apiSupports[api] == true;
        }
        broadcastEvent(EVENTS.INFO, "apiSupports" + stringify(results));
        return results;
    }
	
	
	extend.addEventListener = function(event, listener) {
		mraidview.addEventListener(event, listener);	
	};
    
	extend.removeEventListener = function(event, listener) {
		mraidview.removeEventListener(event, listener);
	};
	
	
	/**
	* Share with weixin firend.
	*
	* @param title - the title for the share
	* @param subtitle - the content for the share
	* @param articleUrl - the url for the share
	* @param imgUrl - the image url for the share
	* @returns none
	*/
	extend.shareToWXFriend = mraidview.shareToWXFriend;

	/**
	 * Share with weixin firend circle.
	*
	* @param title - the title for the share
	* @param subtitle - the content for the share
	* @param articleUrl - the url for the share
	* @param imgUrl - the image url for the share
	* @returns none
	*/
	extend.shareToWXTimeLine = mraidview.shareToWXTimeLine;
    
	/**
	 * Show the share panel.
	*
	* @param title - the title for the share
	* @param subtitle - the content for the share
	* @param articleUrl - the url for the share
	* @param imgUrl - the image url for the share
	* @returns none
	*/
	extend.showSharePanel = mraidview.showSharePanel;

	/**
	 * get user key.
	*
	* @returns user key
	*/
	extend.getUserKey =	mraidview.getUserKey;		
	
	/**
	 * called from h5 when it's ready.
	*
	*/
	extend.stageReady = mraidview.stageReady;		
	
	extend.richMediaViewPing = mraidview.richMediaViewPing;
	
	extend.richMediaArea = function(x, y, w, h) {
		var exp = JSON.stringify(({
		x: x,
		y: y,
		w: w,
		h: h
		}));
		mraidview.richMediaArea(exp);
	};
	
	
	extend.getProgress = mraidview.getProgress;
	
	extend.pause = mraidview.pause;
	
	extend.resume = mraidview.resume;
	
	extend.getParams = mraidview.getParams;
	
	extend.getRichdata = mraidview.getRichdata;
	
	extend.videoSeek = mraidview.videoSeek;
	
	extend.viewMore = function(url) {
		mraidview.viewMore(url);		
	};
	
	extend.removeRichAd = mraidview.removeRichAd;		
	
	extend.setObjectViewable = mraidview.setObjectViewable;
	
	extend.getUrlsForVids = mraidview.getUrlsForVids;
	
	extend.skipAd = mraidview.skipAd;
	
	extend.cancelSplashAdCountdown = mraidview.cancelSplashAdCountdown;
	
	extend.vibrate = mraidview.vibrate;
	
	extend.showLoginPanel = mraidview.showLoginPanel;
	
	extend.getLoginStatus = mraidview.getLoginStatus;

	extend.startMicrophone = mraidview.startMicrophone;

	extend.endMicrophone = mraidview.endMicrophone;
	
	extend.startMic = mraidview.startMic;

	extend.endMic = mraidview.endMic;

	extend.callForMotionEvent = mraidview.callForMotionEvent;
	
	extend.hasApp = mraidview.hasApp;
	
	extend.openApp = mraidview.openApp;
	
	extend.scanQRCode = mraidview.scanQRCode;

	extend.getNetworkStatus  = function(){
		return networkStatus;
	}
	
	extend.getAppContext = function(){
		return appContext;
	}
	
	
	//Temp added in 4.3 to make fluent transition to new 'extend' api, suppose to be removed in the future version.
	
	Object.getOwnPropertyNames(mraidview).forEach(function(d) {
        var c = mraidview[d];
        if (typeof c === "function" && d != "mraidLoaded" && d != "fireEvent" && d != "addEventListener" && d != "removeEventListener" && mraid[d] == null) {
		mraid[d] = mraidview[d];
        }
        });
		
	mraid.richMediaArea = function(x, y, w, h) {
		var exp = JSON.stringify(({
		x: x,
		y: y,
		w: w,
		h: h
		}));
		mraidview.richMediaArea(exp);
	};
	
	mraid.getAndroidVersion =  function() {
		var appContext = extend.getAppContext();
		return appContext.androidVersion;
	};
	/**
	 * 视频富媒体广告新增部分。
	 */
	var mainVideo = mraid.mainVideo = {};
	mainVideo.EVENTS = {
		DURATION_CHANGE: 'durationChange'
	};
	
	// 合并EVENTS
	//mergeObject(mainVideo.EVENTS, EVENTS);
	
    	mainVideo.addEventListener = mraidview.addEventListener;
    	
	mainVideo.removeEventListener = mraidview.removeEventListener

	
	
	mainVideo.getProgress = mraidview.getProgress;
	mainVideo.pause = mraidview.pause;
	mainVideo.resume = mraidview.resume;


	var mainAd = mraid.mainAd = {};
	mainAd.EVENTS = {
		APPLICATION_RESIGN_ACTIVE: 'applicationResignActive',
		LANDING_PAGE_DISMISS: 'landingPageDismiss',
		APPLICATION_BECOME_ACTIVE: 'applicationBecomeActive'	
	};
	
	// 合并EVENTS
	//mergeObject(mainAd.EVENTS, EVENTS);
	
	mainAd.addEventListener = mraidview.addEventListener;
    
	mainAd.removeEventListener = mraidview.removeEventListener
	
	mainAd.getParams = mraidview.getParams;
	mainAd.viewMore = function(url) {
		mraidview.viewMore(url);		
	};
	
	mainAd.removeRichAd = mraidview.removeRichAd;
	
	mainAd.setObjectViewable = mraidview.setObjectViewable;
	
	mainAd.getUrlsForVids = mraidview.getUrlsForVids;
	
	mainAd.skipAd = mraidview.skipAd;
	
	mainAd.cancelSplashAdCountdown = mraidview.cancelSplashAdCountdown;

	/** mraid public methods are done. */
	
	// indicates that mraid.js was loaded already.
	mraidview.mraidLoaded();
	
	/*
	mraid.addEventListener('info', function(info) {
		console.log("mraid info message: " + info);
	});
	mraid.addEventListener('error', function(info) {
		console.log("mraid error message: " + info);
	});
	*/
	
})();

function loadscript(url){
	console.log("loadscript:" + url);
	var script = document.createElement("script");
	script.setAttribute('type','text/javascript'); 
	script.setAttribute('src',url);
	document.body.appendChild(script);
}