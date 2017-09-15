'use struct'

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
	init: function(){    //初始化内部变量
		var _self = this;
		_self.initCache();
		_self.initSetting();
		_self.bind();
	},
	bind: function(){
		var _self = this;

		const { FOCUS_SCHEME, STATE_SCHEME, CALLBACK_SCHEME } = _self.schemeCache;

		document.addEventListener('selectionchange', _self.saveRange, false);

		_self.cache.title.addEventListener('focus', function(){
			AndroidContentFocus.setViewEnabled(true);
		}, false);

		_self.cache.title.addEventListener('blur', () => {
			AndroidContentFocus.setViewEnabled(false);
		}, false);

		_self.cache.editor.addEventListener('blur', () => {
			_self.saveRange();
		}, false);

		_self.cache.editor.addEventListener('click', (evt) => {
			_self.saveRange();
			_self.getEditItem(evt);
		}, false);

		
		_self.cache.editor.addEventListener('keyup', (evt) => {
			if(e.which == 37 || e.which == 39 || e.which == 13 || e.which == 8){
				_self.getEditItem(evt);
			}
		}, false);

		_self.cache.editor.addEventListener('input', () => {
			window.location.href = CALLBACK_SCHEME + encodeURI(_self.getHtml());
		}, false);
	},
	initCache: function(){
		var _self = this;
		_self.cache.editor = document.getElementById('editor');
		_self.cache.title = document.getElementById('title');
		_self.cache.editor.style.minHeight = window.innerHeight - 69 + 'px';
	},
	initSetting: function(){
		const _self = this;
		_self.setting.screenWidth = window.innerWidth - 20;
		_self.setting.screenDpr = window.devicePixelRatio;
	},
	focus: function(){   //聚焦
		var _self = this;
		var range = document.createRange();
		range.selectNodeContents(this.cache.editor);
		range.collapse(false);
		var select = window.getSelection();
		select.removeAllRanges();
		select.addRange(range);
		_self.cache.editor.focus();
	},
	getHtml: function(){
		var _self = this;
		return _self.cache.editor.innerHTML;
	},
	saveRange: function(){   //保存节点位置
		var _self = this;
		var selection = window.getSelection();
		if(selection.rangeCount > 0){
			var range = selection.getRangeAt(0);
			var { startContainer, startOffset, endContainer, endOffset} = range;
			_self.currentRange = {
				startContainer: range.startContainer,
				startOffset: range.startOffset,
				endContainer: range.endContainer,
				endOffset: range.endOffset
			};
		}
	},
	reduceRange: function(){  //还原节点位置
		var _self = this;
		var { startContainer, startOffset, endContainer, endOffset} = _self.currentRange;
		var range = document.createRange();
		var selection = window.getSelection();
		selection.removeAllRanges();
		range.setStart(startContainer, startOffset);
		range.setEnd(endContainer, endOffset);
		selection.addRange(range);
	},
	exec: function(command){    //执行指令
		var _self = this;
		if(_self.commandSet.indexOf(command) !== -1){
			console.log(111);
			document.execCommand(command, false, null);
		}else{
			console.log(1111);
			let value = '<'+command+'>';
			document.execCommand('formatBlock', false, value);
		}
	},
	getEditItem: function(evt){      //通过点击时，去获得一个当前位置的所有状态
		var _self = this;
		const { STATE_SCHEME, CHANGE_SCHEME, IMAGE_SCHEME } = _self.schemeCache;
		if(evt.target && evt.target.tagName === 'A'){
			_self.cache.currentLink = evt.target;
			const name = evt.target.innerText;
			const href = evt.target.getAttribute('href');
			window.location.href = CHANGE_SCHEME + encodeURI(name + '@_@' + href);
		}else if(evt.target && ( evt.target.tagName === 'IMG' || evt.target.className === 'cover')){
			const parentNode = evt.target.parentNode;
			const img = parentNode.querySelector('img');
			const url = img.getAttribute('src');
			const id = img.getAttribute('data-id');
			window.location.href = IMAGE_SCHEME + encodeURI(id);
		}else{
			var items = [];
			_self.commandSet.forEach((item) => {
				if(document.queryCommandState(item)){
					items.push(item);
				}
			});
			if(document.queryCommandValue('formatBlock')){
				items.push(document.queryCommandValue('formatBlock'));
			}
			window.location.href = STATE_SCHEME + encodeURI(items.join(','));
		}
	},
	insertHtml: function(html){
		var _self = this;
		document.execCommand('insertHtml', false, html);
	},
	insertLine: function(){
		var _self = this;
		var html = '<hr><div><br></div>';
		_self.insertHtml(html);
	},
	insertLink: function(name, url){
		var _self = this;
		var html = `<a href="${url}" class="editor-link">${name}</a>`;
		_self.insertHtml(html);
	},
	changeLink: function(name, url){
		var _self = this;
		var current = _self.cache.currentLink;
		var len = name.length;
		current.innerText = name;
		current.setAttribute('href', url);
		var selection = window.getSelection();
		var range = selection.getRangeAt(0).cloneRange();
		var { startContainer, endContainer } = _self.currentRange;
		selection.removeAllRanges();
		range.setStart(startContainer, len);
		range.setEnd(endContainer, len);
		selection.addRange(range);
	},
	insertImage: function(url, width, height, id){
		var _self = this;
		let newWidth=0, newHeight = 0;
		const { screenWidth, screenDpr } = _self.setting;
		if(width > screenWidth * screenDpr){
			newWidth = screenWidth;
			newHeight = height * newWidth / width;
		}else{
			newWidth = width / screenDpr;
			newHeight = height / screenDpr;
		}
		var image = `<div><br></div><div class="img-block">
				<div style="width: ${newWidth}px" class="process">
					<div class="fill">
					</div>
				</div>
				<img class="images" data-id="${id}" style="width: ${newWidth}px; height: ${newHeight}px;" src="${url}"/>
				<div class="cover" style="width: ${newWidth}px; height: ${newHeight}px"></div>
				<div class="delete">
					<img src="./reload.png">
					<div class="tips">图片上传失败，请点击重试</div>
				</div>
				<input type="text" placeholder="请输入图片名字">
			</div><div><br></div>`;
		_self.insertHtml(image);
		var img = document.querySelector(`img[src="${url}"]`);
		img.parentNode.contentEditable = false;
		_self.imageCache.set(id, img.parentNode);
	},
	changeProcess: function(id, process){
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		var fill = imgBlock.querySelector('.fill');
		fill.style.width = `${process}%`;
		if(process == 100){
			var cover = imgBlock.querySelector('.cover');
			var process = imgBlock.querySelector('.process');
			imgBlock.removeChild(cover);
			imgBlock.removeChild(process);
			_self.imageCache.delete(id);
		}
	},
	removeImage: function(id){
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		imgBlock.parentNode.removeChild(imgBlock);
	},
	uploadFailure: function(id){
		const _self = this;
		const imgBlock = _self.imageCache.get(id);
		const del = imgBlock.querySelector('.delete');
		del.style.display = 'block';
		console.log('uploadFailure');
	},
	uploadReload: function(id){
		const _self = this;
		const imgBlock = _self.imageCache.get(id);
		const del = imgBlock.querySelector('.delete');
		del.style.display = 'none';
	}
};

RE.init();