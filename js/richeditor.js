'use struct'

const RE = {
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
	imageCache: new Map(),
	init: function(){    //初始化内部变量
		const _self = this;
		_self.initCache();
		_self.bind();
	},
	bind: function(){
		const _self = this;

		document.addEventListener('selectionchange', _self.saveRange, false);

		_self.cache.title.addEventListener('focus', function(){
			// window.location.href = 'state://'+ encodeURI('input');
			_self.reduceRange();
			_self.exec('blockquote');
			setTimeout(() => {
				_self.exec('p');
			}, 2000);
		}, false);

		_self.cache.editor.addEventListener('blur', () => {
			_self.saveRange();
		}, false);

		_self.cache.editor.addEventListener('click', (evt) => {
			_self.saveRange();
			_self.getEditItem(evt.target);
		}, false);

		
		_self.cache.editor.addEventListener('keyup', (e) => {
			if(e.which == 37 || e.which == 39){
				_self.getEditItem(e.target);
			}
		}, false);

		_self.cache.editor.addEventListener('input', () => {
			// window.location.href = "callback://" + encodeURI(_self.getHtml());
		}, false);
	},
	initCache: function(){
		const _self = this;
		_self.cache.editor = document.getElementById('editor');
		_self.cache.title = document.getElementById('title');
	},
	focus: function(){   //聚焦
		const _self = this;
		const range = document.createRange();
		range.selectNodeContents(this.cache.editor);
		range.collapse(false);
		const select = window.getSelection();
		select.removeAllRanges();
		select.addRange(range);
		_self.cache.editor.focus();
	},
	getHtml: function(){
		const _self = this;
		return _self.cache.editor.innerHTML;
	},
	saveRange: function(){   //保存节点位置
		_self = this;
		const selection = window.getSelection();
		if(selection.rangeCount > 0){
			const range = selection.getRangeAt(0);
			const { startContainer, startOffset, endContainer, endOffset} = range;
			_self.currentRange = {
				startContainer: range.startContainer,
				startOffset: range.startOffset,
				endContainer: range.endContainer,
				endOffset: range.endOffset
			};
		}
	},
	reduceRange: function(){  //还原节点位置
		const _self = this;
		const { startContainer, startOffset, endContainer, endOffset} = _self.currentRange;
		const range = document.createRange();
		const selection = window.getSelection();
		selection.removeAllRanges();
		range.setStart(startContainer, startOffset);
		range.setEnd(endContainer, endOffset);
		selection.addRange(range);
	},
	exec: function(command){    //执行指令
		const _self = this;
		if(_self.commandSet.indexOf(command) !== -1){
			document.execCommand(command, false, null);
		}else{
			let value = '<'+command+'>';
			document.execCommand('formatBlock', false, value);
		}
	},
	getEditItem: function(evt){      //通过点击时，去获得一个当前位置的所有状态
		const _self = this;
		if(evt.target === 'A'){
			_self.cache.currentLink = evt.target;
			window.location.href = 'state://'+encodeURI('a');
		}else{
			const items = [];
			_self.commandSet.forEach((item) => {
				if(document.queryCommandState(item)){
					items.push(item);
				}
			});
			if(document.queryCommandValue('formatBlock')){
				items.push(document.queryCommandValue('formatBlock'));
			}
			window.location.href = 'state://'+encodeURI(items.join(','));
		}
	},
	insertHtml: function(html){
		const _self = this;
		document.execCommand('insertHtml', false, html);
	},
	insertLine: function(){
		const _self = this;
		const html = '<hr><div><br></div>';
		_self.insertHtml(html);
	},
	insertLink: function(name, url){
		const _self = this;
		const html = `<a href="${url}" class="editor-link">${name}</a>`;
		_self.insertHtml(html);
	},
	changeLink: function(name, url){
		const _self = this;
		const current = _self.cache.currentLink;
		const len = name.length;
		current.innerText = name;
		current.setAttribute('href', url);
		const selection = window.getSelection();
		const range = selection.getRangeAt(0).cloneRange();
		const { startContainer, endContainer } = _self.currentRange;
		selection.removeAllRanges();
		range.setStart(startContainer, len);
		range.setEnd(endContainer, len);
		selection.addRange(range);
	},
	insertImage: function(url, width, height){
		const _self = this;
		const image = `<div class="img-block">
				<div style="width: ${width}px" class="process">
					<div class="fill">
					</div>
				</div>
				<img class="images" style="width: ${width}px; height: ${height}px;" src="${url}"/>
				<div class="cover" style="width: ${width}px; height: ${height}px">
				</div>
				<input type="text" value="来自简书">
			</div><div><br></div>`;
		_self.insertHtml(image);
		const img = document.querySelector(`img[src="${url}"]`);
		img.parentNode.contentEditable = false;
		_self.imageCache.set(url, img.parentNode);
	},
	changeProcess: function(url, process){
		const _self = this;
		const imgBlock = _self.imageCache.get(url);
		const fill = imgBlock.querySelector('.fill');
		fill.style.width = `${process}%`;
		if(process == 100){
			const cover = imgBlock.querySelector('.cover');
			const process = imgBlock.querySelector('.process');
			cover.style.display = 'none';
			process.style.display = 'none';
			_self.imageCache.delete(url);
		}
	},
	removeImage: function(url){
		const _self = this;
		const imgBlock = _self.imageCache.get(url);
		_self.cache.editor.removeChild(imgBlock);
	}
};

RE.init();