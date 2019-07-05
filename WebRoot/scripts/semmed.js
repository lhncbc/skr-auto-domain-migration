function getRealPos(ele,dir){
	var pos;
	(dir=="x") ? pos = ele.offsetLeft : pos = ele.offsetTop;
	var tempEle = ele.offsetParent;
	while(tempEle != null){
		pos += (dir=="x") ? tempEle.offsetLeft : tempEle.offsetTop;
		tempEle = tempEle.offsetParent;
	}
	return pos;
}