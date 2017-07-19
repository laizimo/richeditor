import {RE} from './richeditor.js';

export class Font {
    constructor(){
        this.font = document.getElementById('fontBtn');
        this.fontTool = document.getElementsByClassName('font')[0];
        this.handleFontClick = this.handleFontClick.bind(this);
        this.fontToolShow = this.fontToolShow.bind(this);
        this.fontToolHidden = this.fontToolHidden.bind(this);
    }

    handleFontClick(){
        RE.prevFocus();
        if(this.font.className && this.font.className.indexOf('active') !== -1){
            this.font.className = "";
            this.fontToolHidden();
        }else{
            this.font.classList.add('active');
            this.fontToolShow()
        }
    }

    fontToolShow(){
        this.fontTool.style.display = "flex";
        this.fontTool.addEventListener('click', this.handleFontToolClick, false);
    }

    handleFontToolClick(ev){   //当点击字体工具栏时
        RE.prevFocus();
        var target = null;
        if(ev.target.id){
            target = ev.target;
        }else{
            target = ev.target.parentNode;
        }
        if(target.className && target.className.indexOf('active') !== -1){
            target.className = "";
            RE.exec(target.id);
        }else{
            target.className += 'active';
            RE.exec(target.id);
        }
    }

    fontToolHidden() {
        this.fontTool.style.display = "none";
        this.fontTool.removeEventListener('click', this.handleFontToolClick, false);
    }

    addClick(){
        this.font.addEventListener('click', this.handleFontClick, false);
    }

    removeClick(){
        this.font.removeEventListener('click', this.handleFontClick, false);
    }
}