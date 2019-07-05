var myElement=null;

function addBox(text){

	if (myElement)
		removeBox();
		
	var e = document.createElement('div');
//	var h = document.createElement('a');
	

	e.setAttribute('class','abstract');
	e.innerHTML = "<a class='abstract' href='javascript:void(0)' onclick='removeBox()'>CLOSE</a>"+
	              "<h1 class='abstract'>Abstract:</h1><br>"+text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")+"<br>";
	
//	h.href = 'javascript:void(0)';
//	h.setAttribute('onclick','removeBox()');
//	h.setAttribute('class','abstract');
//	h.innerHTML= 'CLOSE';

//	e.appendChild(h);
	
	document.body.appendChild(e);
	myElement = e;
	
}

function removeBox(){
	document.body.removeChild(myElement);
	myElement=null;
}