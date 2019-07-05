function tab(source){
	var tabs = new Array();
	tabs[0]='rCitations';
	tabs[1]='rPredications';
	tabs[2]='nrCitations';
	tabs[3]='nrPredications';
	
	var tabButtons = new Array();
	tabButtons['rCitations'] = 'rcTab';
	tabButtons['rPredications'] = 'rpTab';
	tabButtons['nrCitations'] = 'nrcTab';
	tabButtons['nrPredications'] = 'nrpTab';
	
	for(var i=0;i<4;i++){
		var e1 = document.getElementById(tabs[i]);
		if (e1)
			e1.style.display = 'none';
	}
	
	for(var x in tabButtons)
		document.getElementById(tabButtons[x]).style.fontWeight = 'normal';		
	
	if (document.getElementById(source))
		document.getElementById(source).style.display = 'block';
	if (tabButtons[source])
		document.getElementById(tabButtons[source]).style.fontWeight = 'bold';		
}