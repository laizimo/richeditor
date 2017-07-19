import './less/index.less';
import {RE} from './js/richeditor.js';
import {Font} from './js/font.js';

if (module.hot) {
  module.hot.accept();
}

window.onload = function(){
    var font = new Font();
    RE.editor.addEventListener('focus', editorFocus(font), false);
    RE.editor.addEventListener('click', editorClick, false);
    RE.editor.addEventListener('keyup', editorKeyup, false);
    RE.focus();
}

function editorFocus(){
    var argus = Array.prototype.slice.call(arguments);
    argus.forEach((value) => value.addClick());
}

function editorClick() {
    RE.isClick = true;
    RE.prepareInsert();
}

function editorKeyup(e) {
    RE.prepareInsert();
    var KEYLEFT = 37,KEYRIGHT = 39;
    if(e.which == 37 || e.which == 39){
        console.warn("warning");
    } 
}

function editorInput() {
    console.log(input);
}