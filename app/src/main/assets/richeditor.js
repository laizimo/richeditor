'use strict';
'use struct';

var RE = {
	currentRange: {
		startContainer: null,
		startOffset: 0,
		endContainer: null,
		endOffset: 0
	},
	cache: {
		editor: null,
		title: null,
		currentLink: null
	},
	commandSet: ['bold', 'italic', 'strikethrough', 'redo', 'undo'],
	schemeCache: {
		FOCUS_SCHEME: 'focus://',
		CHANGE_SCHEME: 'change://',
		STATE_SCHEME: 'state://',
		CALLBACK_SCHEME: 'callback://',
		IMAGE_SCHEME: 'image://'
	},
	setting: {
		screenWidth: 0,
		screenDpr: 0,
		margin: 20
	},
	imageCache: new Map(),
	init: function init() {
		//初始化内部变量
		var _self = this;
		_self.initCache();
		_self.initSetting();
		_self.bind();
	},
	bind: function bind() {
		var _self = this;

		var _self$schemeCache = _self.schemeCache,
		    FOCUS_SCHEME = _self$schemeCache.FOCUS_SCHEME,
		    STATE_SCHEME = _self$schemeCache.STATE_SCHEME,
		    CALLBACK_SCHEME = _self$schemeCache.CALLBACK_SCHEME;


		document.addEventListener('selectionchange', _self.saveRange, false);

		_self.cache.title.addEventListener('focus', function () {
			AndroidContentFocus.setViewEnabled(true);
		}, false);

		_self.cache.title.addEventListener('blur', function () {
			AndroidContentFocus.setViewEnabled(false);
		}, false);

		_self.cache.editor.addEventListener('blur', function () {
			_self.saveRange();
		}, false);

		_self.cache.editor.addEventListener('click', function (evt) {
			_self.saveRange();
			_self.getEditItem(evt);
		}, false);

		_self.cache.editor.addEventListener('keyup', function (evt) {
			if (e.which == 37 || e.which == 39 || e.which == 13 || e.which == 8) {
				_self.getEditItem(evt);
			}
		}, false);

		_self.cache.editor.addEventListener('input', function () {
			window.location.href = CALLBACK_SCHEME + encodeURI(_self.getHtml());
		}, false);
	},
	initCache: function initCache() {
		var _self = this;
		_self.cache.editor = document.getElementById('editor');
		_self.cache.title = document.getElementById('title');
		_self.cache.editor.style.minHeight = window.innerHeight - 69 + 'px';
	},
	initSetting: function initSetting() {
		var _self = this;
		_self.setting.screenWidth = window.innerWidth - 20;
		_self.setting.screenDpr = window.devicePixelRatio;
	},
	focus: function focus() {
		//聚焦
		var _self = this;
		var range = document.createRange();
		range.selectNodeContents(this.cache.editor);
		range.collapse(false);
		var select = window.getSelection();
		select.removeAllRanges();
		select.addRange(range);
		_self.cache.editor.focus();
	},
	getHtml: function getHtml() {
		var _self = this;
		return _self.cache.editor.innerHTML;
	},
	saveRange: function saveRange() {
		//保存节点位置
		var _self = this;
		var selection = window.getSelection();
		if (selection.rangeCount > 0) {
			var range = selection.getRangeAt(0);
			var startContainer = range.startContainer,
			    startOffset = range.startOffset,
			    endContainer = range.endContainer,
			    endOffset = range.endOffset;

			_self.currentRange = {
				startContainer: range.startContainer,
				startOffset: range.startOffset,
				endContainer: range.endContainer,
				endOffset: range.endOffset
			};
		}
	},
	reduceRange: function reduceRange() {
		//还原节点位置
		var _self = this;
		var _self$currentRange = _self.currentRange,
		    startContainer = _self$currentRange.startContainer,
		    startOffset = _self$currentRange.startOffset,
		    endContainer = _self$currentRange.endContainer,
		    endOffset = _self$currentRange.endOffset;

		var range = document.createRange();
		var selection = window.getSelection();
		selection.removeAllRanges();
		range.setStart(startContainer, startOffset);
		range.setEnd(endContainer, endOffset);
		selection.addRange(range);
	},
	exec: function exec(command) {
		//执行指令
		var _self = this;
		if (_self.commandSet.indexOf(command) !== -1) {
			console.log(111);
			document.execCommand(command, false, null);
		} else {
			console.log(1111);
			var value = '<' + command + '>';
			document.execCommand('formatBlock', false, value);
		}
	},
	getEditItem: function getEditItem(evt) {
		//通过点击时，去获得一个当前位置的所有状态
		var _self = this;
		var _self$schemeCache2 = _self.schemeCache,
		    STATE_SCHEME = _self$schemeCache2.STATE_SCHEME,
		    CHANGE_SCHEME = _self$schemeCache2.CHANGE_SCHEME,
		    IMAGE_SCHEME = _self$schemeCache2.IMAGE_SCHEME;

		if (evt.target && evt.target.tagName === 'A') {
			_self.cache.currentLink = evt.target;
			var name = evt.target.innerText;
			var href = evt.target.getAttribute('href');
			window.location.href = CHANGE_SCHEME + encodeURI(name + '@_@' + href);
		} else if (evt.target && (evt.target.tagName === 'IMG' || evt.target.className === 'cover')) {
			var parentNode = evt.target.parentNode;
			var img = parentNode.querySelector('img');
			var url = img.getAttribute('src');
			var id = img.getAttribute('data-id');
			window.location.href = IMAGE_SCHEME + encodeURI(id);
		} else {
			var items = [];
			_self.commandSet.forEach(function (item) {
				if (document.queryCommandState(item)) {
					items.push(item);
				}
			});
			if (document.queryCommandValue('formatBlock')) {
				items.push(document.queryCommandValue('formatBlock'));
			}
			window.location.href = STATE_SCHEME + encodeURI(items.join(','));
		}
	},
	insertHtml: function insertHtml(html) {
		var _self = this;
		document.execCommand('insertHtml', false, html);
	},
	insertLine: function insertLine() {
		var _self = this;
		var html = '<hr><div><br></div>';
		_self.insertHtml(html);
	},
	insertLink: function insertLink(name, url) {
		var _self = this;
		var html = '<a href="' + url + '" class="editor-link">' + name + '</a>';
		_self.insertHtml(html);
	},
	changeLink: function changeLink(name, url) {
		var _self = this;
		var current = _self.cache.currentLink;
		var len = name.length;
		current.innerText = name;
		current.setAttribute('href', url);
		var selection = window.getSelection();
		var range = selection.getRangeAt(0).cloneRange();
		var _self$currentRange2 = _self.currentRange,
		    startContainer = _self$currentRange2.startContainer,
		    endContainer = _self$currentRange2.endContainer;

		selection.removeAllRanges();
		range.setStart(startContainer, len);
		range.setEnd(endContainer, len);
		selection.addRange(range);
	},
	insertImage: function insertImage(url, width, height, id) {
		var _self = this;
		var newWidth = 0,
		    newHeight = 0;
		var _self$setting = _self.setting,
		    screenWidth = _self$setting.screenWidth,
		    screenDpr = _self$setting.screenDpr;

		if (width > screenWidth * screenDpr) {
			newWidth = screenWidth;
			newHeight = height * newWidth / width;
		} else {
			newWidth = width / screenDpr;
			newHeight = height / screenDpr;
		}
		var image = '<div><br></div><div class="img-block">\n\t\t\t\t<div style="width: ' + newWidth + 'px" class="process">\n\t\t\t\t\t<div class="fill">\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t\t<img class="images" data-id="' + id + '" style="width: ' + newWidth + 'px; height: ' + newHeight + 'px;" src="' + url + '"/>\n\t\t\t\t<div class="cover" style="width: ' + newWidth + 'px; height: ' + newHeight + 'px"></div>\n\t\t\t\t<div class="delete">\n\t\t\t\t\t<img src="./reload.png">\n\t\t\t\t\t<div class="tips">\u56FE\u7247\u4E0A\u4F20\u5931\u8D25\uFF0C\u8BF7\u70B9\u51FB\u91CD\u8BD5</div>\n\t\t\t\t</div>\n\t\t\t\t<input type="text" placeholder="\u8BF7\u8F93\u5165\u56FE\u7247\u540D\u5B57">\n\t\t\t</div><div><br></div>';
		_self.insertHtml(image);
		var img = document.querySelector('img[src="' + url + '"]');
		img.parentNode.contentEditable = false;
		_self.imageCache.set(id, img.parentNode);
	},
	changeProcess: function changeProcess(id, process) {
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		var fill = imgBlock.querySelector('.fill');
		fill.style.width = process + '%';
		if (process == 100) {
			var cover = imgBlock.querySelector('.cover');
			var process = imgBlock.querySelector('.process');
			imgBlock.removeChild(cover);
			imgBlock.removeChild(process);
			_self.imageCache.delete(id);
		}
	},
	removeImage: function removeImage(id) {
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		imgBlock.parentNode.removeChild(imgBlock);
	},
	uploadFailure: function uploadFailure(id) {
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		var del = imgBlock.querySelector('.delete');
		del.style.display = 'block';
		console.log('uploadFailure');
	},
	uploadReload: function uploadReload(id) {
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		var del = imgBlock.querySelector('.delete');
		del.style.display = 'none';
	}
};

RE.init();
