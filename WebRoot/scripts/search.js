var newRowsAuthor = 0;
var newRowsJournal = 0;
var newRowsCounter = 0;

function tab(source){
	var e1 = document.getElementById("Medline");
	
	if (e1)
		e1.style.display = 'none';
	if (document.getElementById('medlineTab'))
		document.getElementById('medlineTab').style.fontWeight='normal';	
		
		
	e1 = document.getElementById("ClinicalTrials");
	if (e1)
		e1.style.display = 'none';
	if (document.getElementById('ctTab'))
		document.getElementById('ctTab').style.fontWeight='normal';			
		
	e1 = document.getElementById(source);
	if (e1)
		e1.style.display = 'block';		
	else
	 	alert(source);
		
	if (source=='Medline')
		document.getElementById('medlineTab').style.fontWeight='bold';
	else
		document.getElementById('ctTab').style.fontWeight='bold';

}

function toggleOptions(){

	//all this hacking is because html:form won't let me give it a name nor html:select an id
	var s1 = document.getElementById("selectTD");
	var e1 = s1.getElementsByTagName("select");		
	var source = e1.item(0).value;


	if (source == "medline"){
		document.getElementById("pubmedOptions").style.display= 'block';
		document.getElementById("clinicalTrialOptions").style.display = 'none';
	}else if (source == "ctrials"){
		document.getElementById("pubmedOptions").style.display= 'none';
		document.getElementById("clinicalTrialOptions").style.display = 'block';
	}else{
		document.getElementById("pubmedOptions").style.display= 'block';
		document.getElementById("clinicalTrialOptions").style.display = 'block';
	}
}



function enableInputs(node, enabled) {
   var kinput, kid
   var kids
   var kinputArray = ["button", "input", "optgroup", "option", "select", "textarea"];
//   alert(enabled +" " +node.tagName)
   for (kinput in kinputArray) {
      kids = node.getElementsByTagName(kinputArray[kinput])
//	  alert (kids.length)
	  for (var i = 0; i < kids.length; i++) {
	     kid = kids[i]
		 kid.disabled = !enabled
      }
   }
}


function retainNames (rowcopyFields) {
	for (var i=0; i < rowcopyFields.length; i++) {
		var theName = rowcopyFields[i].name 
		if (theName) {
			if (rowcopyFields[i].type == "radio") {
				rowcopyFields[i].value = theName + newRowsCounter;
			} else {
				rowcopyFields[i].name = theName + newRowsCounter;
				rowcopyFields[i].id = theName +  newRowsCounter;  // IE can't find just created elements by name, so it needs ID.
			}
		}
	}
}

function remrow(event)		// if presents row will not removed but hided with display:none
{

      var target;
            if (event) {  // mozilla et al
                target = event.target;
            } else {  // IE
                event = window.event;
				target = event.srcElement;
            }
	if (target.myrow) {
		var trparent = target.myrow.parentNode;
		trparent.removeChild(target.myrow);
		if (target.lable == "Author") newRowsAuthor--; 
		if (target.lable == "Journal") newRowsJournal--;
	}
	if (newRowsAuthor == 0) { showhide('AuthBlock', true, '' , 'hide'); } 
	if (newRowsJournal == 0) { showhide('JourBlock', true, '' , 'hide'); }
	return false;
}

function rem_rows(blockid)
{
	
	var tbtarget = document.getElementById(blockid).getElementsByTagName("tbody")[0];
	if (tbtarget) {
        var tbody = document.createElement('tbody');
        var tbparent = tbtarget.parentNode;
        tbparent.removeChild(tbtarget);    
        tbparent.appendChild(tbody);
    }
	if (blockid == "auth_dd") { Author_Count=0;  newRowsAuthor=0;  }
	if (blockid == "jour_dd") { Journal_Count=0; newRowsJournal=0; }

}

function addrow(tab, lable) {
	newRowsCounter++;
	if (lable == "Author") { newRowsAuthor++; newRowsCounter = newRowsAuthor; }
	if (lable == "Journal") { newRowsJournal++; newRowsCounter = newRowsJournal; }
	
	var src  = document.getElementById(tab).getElementsByTagName("tfoot")[0].getElementsByTagName("tr")[0];
	var dest = document.getElementById(tab).getElementsByTagName("tbody")[0];
	var srcopy = src.cloneNode(true);
	retainNames (srcopy.getElementsByTagName("input"));
	dest.appendChild(srcopy);
	
	// Assume the "remove" tag is the first and only link in the <tr></tr>
	var alink = srcopy.getElementsByTagName("a");
	
	if (alink) {
	   alink = alink.item(0);
	   alink.myrow = srcopy;
	   alink.lable = lable;
	   alink.onclick = remrow;
	}


	if (typeof suggest == "object") suggest.load();
    
//	return false;
}


function SetFocus( focusname ) 
{
    if (document.getElementById) {
      var el = document.getElementById( focusname );
      if ( typeof el == "object" ) { 
        el.focus(); 
      }
    }
}


function showhide(target, disableChildren, image_id , todo) {
   var node = document.getElementById(target)
   if (!node) { return false }
//   if (todo) node.shown=(todo? "hide": "show")?false:true;
	if (todo == "hide") { node.shown = true; }
	if (todo == "show") { node.shown = false; }

   if (node.shown) {
      node.shown = false
      node.style.display = "none"
	  if (image_id) document.getElementById(image_id).src = larrow_open.src;
	  if (disableChildren) {
	  	enableInputs(node, false);
	  }
   } else {
      node.shown = true
      node.style.display = "block"
	  if (image_id) document.getElementById(image_id).src = larrow_closed.src;
	  if (disableChildren) {
	     enableInputs(node, true);
      }
   }
   return false
}

function pubmedExtra(){
	var node = document.getElementById("pubmedExtraOptions");
	if (node.style.display=='block')
		node.style.display = 'none';
	else
		node.style.display = 'block';
}

function setAll(nodeName, value) {
   if (!document.getElementById) return false
   var node= document.getElementById(nodeName)

   if (node) {
      var cbs = node.getElementsByTagName("INPUT")
      for (var i = 0; i < cbs.length; i++) {
         var cb = cbs[i]
         if (cb.getAttribute("TYPE").toUpperCase() == "CHECKBOX") {
            cb.checked = value
         } else {
         cb.value = ""; 
		 }
      }
   }
   return false;
}