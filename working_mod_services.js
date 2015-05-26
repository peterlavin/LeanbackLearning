//var wiki = require('wiki');
var https = require('https');
var xml = require('xml');
var chunk = require('wiki/modules/chunking');
var chunk1 = require('wiki/modules/chunking1');
//var wikiContent = require('wiki/modules/getwikicontent');
var express = require('express');
var merger = require('wiki/modules/merger');
var _workers = require('./getwiki');
//var MsTranslator = require('mstranslator');
var textrank = require('wiki/modules/textrank');
var funs = require('wiki/modules/functionalities');
var slidesmerger = require('wiki/modules/slidesmerger');
var mergelangs = require('wiki/modules/mergelangs');
var multisum = require('wiki/modules/multisumm');
var translator = require("cngl_translate");
var bing = require("cngl_bing");
var wikilangs = require("cngl_wikilangs");
var request = require('request');
var app = express();


// for new WC and language data for GLOBIC (PL)
var preMultiDocSumWC = 0;
var postMultiDocSumWC = 0;
var xmldoc = require('xmldoc');
 
app.configure(function(){
    app.use(express.bodyParser());    
});

/*
translation account1:

Started on 23/04/2014
Characters/month 2 m
*/
/*
var clientId = "MostafaBayomi3";
var clientSecret = "AUudJx6ef2Z644igKMEkcokEbP66/Yqs0ncZOBzsiXs=";
*/
/*
translation account2:

Started on 21/05/2014
Characters/month 2 m
*/
var summ = 0;
var arr =[];
var clientId = "bayomim";
var clientSecret = "hTjBJJ5zX2kGWE3viIZVf5RBke1+Yy7zYTq54teBxLE=";

//var client = new MsTranslator({client_id:clientId, client_secret:clientSecret});



app.get('/summariser/:title&:time&:detail&:outputlang', function(req, res) {
var ti = new Date();
 var langentered='en'; // make it english by default
  
  var query = req.params.title;
//  var time = req.params.time/100; //////////////mostafa 1
  var time = req.params.time; ////////////// orig line
  var detail = req.params.detail;
  var outputlang = req.params.outputlang;

  
  arr =[];
var maintitle = "",title2="",title3="",_lang1="",_lang2="",_lang3="";

function check(){
	if(arr.length == 3){
	var level_1_len = 0,level_2_len = 0,level_3_len = 0;
		clearInterval(tim);
		for(var i=0;i<arr.length;i++){
			level_1_len+=arr[i].level_1;
			level_2_len+=arr[i].level_2;
			level_3_len+=arr[i].level_3;
		}
		res.set({ 'content-type': 'application/xml; charset=utf-8' })
		var objXML={
			level_1:level_1_len,
			level_2:level_2_len,
			level_3:level_3_len
		}
		var xmlRes = createCountXML(objXML);
		res.send(xmlRes);
		var ti2 = new Date();
		var diff = Math.abs(new Date() - ti);

		console.log("Time: "+(diff/1000)+" seconds");
		res.end();
		console.log("\n============ End first step ============\n");	
	}
}

  
  if(query =="check"&& time=="1" && detail =="2" && outputlang =="3"){
      var d2 = new Date();
	res.type('application/json');
	res.json("200");
	console.log("Time = " + Math.abs(ti - d2));
	res.end();
	
  }
  else if(time == "null"){
  // respond with the no. of words 20% and 50%
  var tim = setInterval(check,1000);
  console.log("first case");
  var detect_param={text:query};
  translator.detect(detect_param,function(err,data){
	if(data){
		var langentered = data;
		console.log("lang detected and is : -- "+data);
		var finalLang = "en-US";
		switch(langentered){
			case "en":
			finalLang = "en-US";
			break;
			case "fr":
			finalLang = "fr-FR";
			break;
			case "de":
			finalLang = "de-DE";
			break;
		}
		bing.getQuery({
		query:query,
		site:"wikipedia.org",
		lang:finalLang
		}, function(title){
			console.log("TITLE:"+title);
			maintitle = title;
			_lang1 = langentered;
			 var toLang1 = "";
			  var toLang2 = "";
			  _case = 1;
			 
			  switch (langentered)
			  {
				case "en":
				toLang1 = "fr";
				toLang2 = "de";
				break;
				
				case "fr":
				toLang1 = "en";
				toLang2 = "de";
				break;
				
				case "de":
				toLang1 = "en";
				toLang2 = "fr";
				break;
				
				default:
				langentered = "en";
				toLang1 = "fr";
				toLang2 = "de";
			  }

			var lang1_params = { 
			  text: title,
			  from: langentered,
			  to: toLang1
			};
			var lang2_params = { 
			  text: title,
			  from: langentered,
			  to: toLang2
			};	
			getWitkiWords(langentered,title,0,function(){
				wikilangs.getURL(lang1_params,function(url){
					title2 = purifyURL(url);
					_lang2 = toLang1;
				getWitkiWords(toLang1,title2,1,function(){
					wikilangs.getURL(lang2_params,function(url){
							title3 = purifyURL(url);
							_lang3 = toLang2;
							getWitkiWords(toLang2,title3,2,null);
				});
			})
			})
			});
			
			
		});// EO bing.getQuery
	  
	  
	  
	  
	}
	  else{// no language detected or error in args
			res.json(createResponse("Error in your query"));
			res.end();
	 }
  
  });
  
  }
  else{ // continue on
  time = time/100;
  console.log("second case");
   var dummyCounter = 0;
  var firstLang = "en",firstInd = 0;
  var secondLang = "fr",secondInd = 1;
  var thirdLang = "de",thirdInd = 2;
  var detect_param={text:query};
  translator.detect(detect_param,function(err,data){
  console.log("tata"+data);
  dummyCounter++;
  if(data|| dummyCounter >0){

  var langentered = data;
  console.log("lang detected and is : -- "+data);

	var finalLang = "en-US";
	switch(langentered){
		case "en":
		finalLang = "en-US";
		break;
		case "fr":
		finalLang = "fr-FR";
		break;
		case "de":
		finalLang = "de-DE";
		break;
	}
bing.getQuery({
	query:query,
	site:"wikipedia.org",
	lang:finalLang
}, function(title){

console.log("title : "+title);
  var toLang1 = "",
  toLang2 = "",
  _case = 1;
  switch (langentered)
  {
	case "en":
	toLang1 = "fr";
	toLang2 = "de";
	_case = 1;
	firstLang = "en";firstInd = 0;
	secondLang = "fr";secondInd = 1;
	thirdLang = "de";thirdInd = 2;
	break;
	
	case "fr":
	toLang1 = "en";
	toLang2 = "de";
	_case = 2;
	firstLang = "fr";firstInd = 1;
	secondLang = "en";secondInd = 0;
	thirdLang = "de";thirdInd = 2;
	break;
	
	case "de":
	toLang1 = "en";
	toLang2 = "fr";
	_case = 3;
	firstLang = "de";firstInd = 2;
	secondLang = "en";secondInd = 0;
	thirdLang = "fr";thirdInd = 1;
	break;
	
	default:
	langentered = "en";
	toLang1 = "fr";
	toLang2 = "de";
	_case = 1;
  }
  // translate the query for the first time


var lang1_params = { 
  text: title,
  from: langentered,
  to: toLang1
};
var lang2_params = { 
  text: title,
  from: langentered,
  to: toLang2
};
var titleInLangs= {
	en:"",
	en_trans:0,
	fr:"",
	fr_trans:0,
	de:"",
	de_trans:0
};
function swch(_c,lang1,lang2){
if(lang1&& lang1!="No URL")
{
	lang1 = purifyURL(lang1);
}
if(lang2 && lang2!="No URL")
{
	lang2 = purifyURL(lang2);
}
	switch (_c){
		case 1:
		titleInLangs.en = title;
		titleInLangs.fr = lang1;
		titleInLangs.de = lang2;
		break;
		
		case 2:
		titleInLangs.en = lang1;
		titleInLangs.fr = title;
		titleInLangs.de = lang2;
		break;
		
		case 3:
		titleInLangs.en = lang1;
		titleInLangs.fr = lang2;
		titleInLangs.de = title;
		break;
	}
	
	switch (outputlang){
		case "en":
		titleInLangs.en_trans = 0;
		titleInLangs.fr_trans = 1;
		titleInLangs.de_trans = 1;
		break;
		
		case "fr":
		titleInLangs.en_trans = 1;
		titleInLangs.fr_trans = 0;
		titleInLangs.de_trans = 1;
		break;
		
		case "de":
		titleInLangs.en_trans = 1;
		titleInLangs.fr_trans = 1;
		titleInLangs.de_trans = 0;
		break;
	}
}



var l1= null;
var l2= null;
var tick = 0;
var arr = [];
function checkTrans(){
tick++;
	if(l1 !=null && l2!= null){
	// here, call the workers function
	
	var first_ob = createWorkersObj(firstLang);
	_workers.getWikiContent(first_ob,arr,firstInd,function(con){
			if(con){
				if(l1 !="No URL"){
				var second_ob = createWorkersObj(secondLang);
				_workers.getWikiContent(second_ob,arr,secondInd,null);
				}
				else{
					arr[secondInd]="No data";
				}
				
				if(l2 !="No URL"){
					var third_ob = createWorkersObj(thirdLang);
					_workers.getWikiContent(third_ob,arr,thirdInd);
				}
				else{
					arr[thirdInd]="No data";
				}
			}
			else{
			    var noResult = createResponse("No Result Found");
				res.type('application/json');
				res.json(noResult);
				res.end();
				//return;
			}
	
	});
	clearInterval(tim);
	}
	else if (tick > 1000) // late in translation process more than 10 seconds
	{
		clearInterval(tim);
		//res.type('application/json');
		res.json("Error #tr01!! Please try again.");
		res.end();
	}

}
var level = 1;
function createWorkersObj(lang){
	// time = (time / 100);  // mostafa 2 (orig suggested fix)

    
    if (detail >= 0 && detail <= 33) // third level
    {
        level = 1;
    }
    else if(detail >33 && detail <= 70) // second level
    {
        level = 2;
    }
    else if(detail >70 && detail <= 100) // first level
    {
        level = 3;
    }
	var _title,_trans;
	switch(lang){
		case "en":
			_title = titleInLangs.en;
			_trans = titleInLangs.en_trans;
		break;
		case "fr":
			_title = titleInLangs.fr;
			_trans = titleInLangs.fr_trans;
		break;
		case "de":
			_title = titleInLangs.de;
			_trans = titleInLangs.de_trans;
		break;
	
	
	}
	
	var toWorkersObj = {
		time : time,
		level:level,
		lang:lang,
		to_lang:outputlang,
		
		title:_title,
		trans:_trans,
	}
	return toWorkersObj;
}

wikilangs.getURL(lang1_params,function(url){
	console.log("lang 1: "+url);
	l1 = url;
	swch(_case,l1,l2);

});
wikilangs.getURL(lang2_params,function(url){
	console.log("lang 2: "+url);
	l2 = url;
	swch(_case,l1,l2);

});
translator.translate(lang1_params,function(err,data){
	l1 = data;
	swch(_case,l1,l2);
});
translator.translate(lang2_params,function(err,data){
	l2 = data;  
	swch(_case,l1,l2);
});

var tim = setInterval(checkTrans,100);

	var timer = setInterval(check,500);
	var che = 500;
	var finalXML = "";
	function check(){
	che+=500;
	if(che<1000000){
		if(arr[0]&& arr[1] && arr[2]) // start to translate, combine the three summaries, and run TR for the output of the combination
		{
			if(arr[0] === "No data" && arr[1] === "No data" &&arr[2] === "No data") // no result maches in any lang
			{
				var noResult = createResponse("No Result Found");
				res.type('application/json');
				res.json(noResult);
				res.end();
			}
			else {
			// heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeer
			// order them regarding the outputlang
			arr = orderArr(arr,outputlang);





			
			var toto = arr.length;
			var temp = -1;
				for(var kt = 0 ; kt < toto; kt++)
				{
				temp++;
					if(arr[temp] === "No data")
					{
					
							arr.splice(temp, 1);
							temp--;
					}
					  
				} 
				var tempArr = [];
						
				if(arr.length == 1) // just one language, respond direct.
				{

				console.log("One lang available\n");
				if(outputlang == "en"){
				removeENnoeAscii(arr[0]);
				}
					//one lang : arr[0] = [["slide title","slide text"],["slide title","slide text"],["slide title","slide text"]]
					// use funs.sentinize
					var finalSentences=[];
					var d = arr[0];
					for(var i =0; i<d.length;i++)
					{
						//console.log(d[i][1].toString()+"-------\n");
						finalSentences.push(d[i][1].toString());
					}
					
					// to remove none ascii characters from en text
					//console.log("POLISH : "+_outputlang);
					//	if(_outputlang == "en"){
					//text = text.replace(/[^A-Za-z 0-9 \.,\?""!@#\$%\^&\*\(\)-_=\+;:<>\/\\\|\}\{\[\]`~]*/g, '');
					//	console.log("POLISH :OK ");
					//}
					
					
					var dd  = finalSentences.join(". ");
					var f = funs.sentenise(dd.toString(),"yes");



					// Although not necessary, count words in final XML doc to return to LBL
					// There will be no losses due to duplicate sentences in a single language
					// presentation
					
			                for (var j = 0; j < f.length; j++) {
                        		var slideText = f[j].toString().trim();
			                slideTextArr = slideText.split(" ");
					//console.log("DEBUG Pre (1 lang): " + slideText);	
					//console.log("Adding: " + slideTextArr.length);	
		                        preMultiDocSumWC += slideTextArr.length;
    					}

                    			console.log("preMultiDocSumWC for arr.len=1 is: " + preMultiDocSumWC + "\n")
			
		                    	// When there is just one language is available, no losses
                		    	// no occur as no documents are translated and merged.
					postMultiDocSumWC = preMultiDocSumWC;





					finalXML = createXML (f);
				}
				else if(arr.length == 2) // two languages
				{
				if(outputlang == "en"){
				removeENnoeAscii(arr[0]);
				}
					// call the two languages merger
					console.log("Two langs\n\n");
					var tt = mergelangs.merge(arr[0],arr[1],outputlang,1);


					console.log("Print of tt[0], contains sections\n");
		                    	for (var i = 0; i < tt[0].length; i++) {
        	        	    	var text = tt[0][i];
                		    	console.log("tt[0]: " + text);
					}
					console.log();






					// count the words before merging
		                    	for (var i = 0; i < tt[1].length; i++) {
        	        	    	var slideText = tt[1][i].toString();
                		    	slideTextArr = slideText.split(" ");
					//console.log("DEBUG Pre (2 lang): " + slideText);
		                    	preMultiDocSumWC += slideTextArr.length;
					}
					
					console.log("\npreMultiDocSumWC for arr.len=2 is: " + preMultiDocSumWC + "\n");





					// apply summary again, but multible
					var rett = multisum.summ(tt[1],outputlang,time);
					var tst = mergeSentences(rett);


                                        // Calculate post merging wordcount

					var finalArray = tst.split(";;;");

                                        // Count the words after merging to detect losses due to 'near duplicate' sentences
                                        for (var i2 = 0; i2 < tst[1].length; i2++) {
                                        var slideText = tst[1][i2].toString().trim();
                                        slideTextArr = slideText.split(" ");
					//console.log("DEBUG Post (2 lang): " + slideText);
					//console.log("Adding: " + slideTextArr.length);
                                        postMultiDocSumWC += slideTextArr.length;
                                        }


                                        console.log("\npostMultiDocSumWC for arr.len=2 is: " + postMultiDocSumWC + "\n");



					finalXML = createXML(finalArray);
					}


					else if(arr.length == 3) // three languages
					{
					if(outputlang == "en"){
					removeENnoeAscii(arr[0]);
					}
					// call the three languages merger
					console.log("Three langs\n\n");
					var tt = mergelangs.merge(arr[0],arr[1],outputlang,1);
					if(tt){
					var tt2 = mergelangs.merge(tt,arr[2],outputlang,2);
					}
					else{
					console.log("Noo TT");
					}



                                        console.log("Print of tt2[0], contains sections headings. tt2 len is: " + tt2.length  + "\n");
                                        for (var i = 0; i < tt2[0].length; i++) {
                                        	var sectionTitle = tt2[0][i];
	                                        var sectionContent = tt2[1][i];
        	                                console.log(i + ": " + sectionTitle + ", " + sectionContent.split(" ").length);
                                        }
                                        console.log();




					// added PL MB
	                		for (var i = 0; i < tt2[1].length; i++) {
					   var slideText = tt2[1][i].toString().trim();
					   slideTextArr = slideText.split(" ");
					   //console.log("DEBUG Pre (3 lang): " + slideText);
					   //console.log("Adding: " + slideTextArr.length);
					   preMultiDocSumWC += slideTextArr.length;
					}
					
					console.log("\npreMultiDocSumWC for arr.len=3 is: " + preMultiDocSumWC + "\n");




					// apply summary again
					var rett = multisum.summ(tt2[1],outputlang,time);
					var tst = mergeSentences(rett);


					// Calculate post merging wordcount
				

					var finalArray = tst.split(";;;");


	
                                        // added PL MB, counts the words after merging and removal of near duplicate sentences.
                                        for (var i2 = 0; i2 < finalArray.length; i2++) {
                                           var slideText = finalArray[i2].toString().trim();
                                           slideTextArr = slideText.split(" ");
					   //console.log("DEBUG Post (3 lang): " + slideText);
					   //console.log("Adding: " + slideTextArr.length);
                                           postMultiDocSumWC += slideTextArr.length;
                                        }

					console.log("\npostMultiDocSumWC for arr.len=3 is: " + postMultiDocSumWC + "\n");

					finalXML = createXML(finalArray);
				}

				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				

				// Now that all word counts are gathered, create a comma separated string
				// of the languages of the available articles which made up the content
				// for example, "en,fr,de"

				var langString = "";
				console.log("\nPrint of arr[]\n");
				for(var a2 = 0; a2 < arr.length; a2++){
					console.log("arr[" + a2 + "]");
					if(a2==0){
						langString = data;
					}
					else if(a2==1){
						langString = langString + "," + toLang1;
					}
					else if(a2==2){
						langString = langString + "," + toLang2;
					}
				}

				console.log("\nlangString: " + langString + "\n");


				// Next, create the XML element which will be inserted into the existing
				// finalXML document

				var metricsElm = new xmldoc.XmlDocument(
				"<metrics>" +
				"<preMergeWC>" + preMultiDocSumWC + "</preMergeWC>" +
				"<postMergeWC>" + postMultiDocSumWC + "</postMergeWC>" + 
				"<langDetected>" + data + "</langDetected>" +
				"<langsUsedInContent>" + langString + "</langsUsedInContent>" +
				"</metrics>")

				// Next, convert the 'section' array (of arrays) to an XML string
				// for insertion to the final returned XML
				
				var sectionsXmlStr = "<visual>"

				for (var i = 0; i < tt2[0].length; i++) {
                			var sectionTitle = tt2[0][i];
					var sectionContent = tt2[1][i];
					sectionsXmlStr += "<section><name>" + sectionTitle + "</name><value>" + sectionContent.split(" ").length + "</value></section>"
                		}

			        sectionsXmlStr += "</visual>";

	
				// Convert this XML like string to an XML document
				var visualData = new xmldoc.XmlDocument(sectionsXmlStr);


				// Then, extract the content from the existing finalXML doc
				
				var finalXMLObj = new xmldoc.XmlDocument(finalXML);
				
				var content = finalXMLObj.childNamed("content");

				// Then, recreate a new XML doc using both parts
				
				var finalXMLwtMetrics = new xmldoc.XmlDocument("<?xml version='1.0' encoding='UTF-8' standalone='no'?>" +
				"<presentation>" +
				content + metricsElm + visualData + // plus any other elements needed
				"</presentation>");

				// Print for debug only
				console.log(finalXMLwtMetrics.toString({pretty:true}) + "\n"); 

				res.set({ 'content-type': 'application/xml; charset=utf-8' })
				//res.type('application/xml');

				// Sent/return to caller, needs to be converted to string first
				res.send(finalXMLwtMetrics.toString());
				res.end();
	
				console.log("\n=============== End of process (2nd step) ==================");
	
			}
			clearInterval(timer);
		}
		}
		else{
		clearInterval(timer);
		res.type('application/json');
		res.json(createResponse("Session time out, please try again"));
		res.end();
		}
	
	}
	
	
});
  
 
	 }// EO if(data) in detecting lang
 else{// no language detected or error in args
		res.json(createResponse("Error in your query"));
		res.end();
 }
	}
	);//EO after detecting lang
 
  
  
  }
});// EO app.get()

//var client = new MsTranslator({client_id:clientId, client_secret:clientSecret});



console.log("Port arg pased was:... " + process.argv.slice(2));

var portToUse = 9999;

if(process.argv[2] == undefined) {
  console.log("No arg found, using default val: " + portToUse);
}
else {
 console.log("Port arg found... " + process.argv.slice(2));
 portToUse = process.argv[2];
}


app.listen(process.env.PORT || portToUse);
console.log("SSC with GLOBIC data Collection running\nlistening to port " + portToUse + "\n");

function mergeSentences(array){
var r = array;
	for(var i = 0; i<r.length;i++){
		r[i] = r[i].join(";;;");
	}
	
	r = r.join(";;;");
	//console.log("RR:"+r);
	return r;
}


function createXML(txt){
var _xml = "";
 _xml = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>'+
			'<presentation>'+
			   '<content>'+
				'<slide>';
				
	for(var i =0; i<txt.length;i++)
	{
		_xml +='<sentence>'+txt[i].toString()+'.</sentence>';
	}
	_xml+= '</slide>'+
		  '</content>'+
		'</presentation>';
		
		return _xml;
}
function createResponse(msg){
	/*
	var _response = "";
	_response ='<?xml version="1.0" encoding="UTF-8" standalone="no"?>'+
			'<response>'+
			   msg+
			'</response>';
			
	return _response;
	*/
	return msg;
}


function purifyURL(url){
	var title = url.split("/");
	return  title[title.length-1];
}

function orderArr(_ar,outLang){
	var temp = null;
	switch (outLang){
		case "fr":
		temp = _ar[0];
		_ar[0] = _ar[1];
		_ar[1] = temp;
		break;
		case "de":
		temp = _ar.pop();// de in temp
		_ar.unshift(temp); // insert de at the top
		break;
	}
	return _ar;
}

function getWitkiWords(lang,title,index,cb){
	var wikiAPI = "https://"+lang+".wikipedia.org/w/api.php?action=query&prop=extracts&titles="+title+"&redirects=&format=json";
	var options = {
	method: 'GET',
		url: wikiAPI,
		proxy: 'http://www-proxy.scss.tcd.ie:8080'
	};
	request(options,  function(err,  res,  body)  {
	console.log("API :"+wikiAPI);
	var t = "";
			if(err) {
			arr[index] = 0;
			}
			if(!res||res.statusCode === 404)
			{
				console.log("Resource not found");
				arr[index] = 0;
				
			}
			else {
			console.log("body :"+res.statusCode);
				var oo1 = JSON.parse(body);
				var oo = oo1.query.pages;
				
				if (oo["-1"] != undefined) {
					//en_finalContent= null; // no content for this title or this language
					var dummyObj = {
					level_1:0,
					level_2:0,
					level_3:0,
					}
					arr[index] = dummyObj;
					console.log("No "+lang+" data because of retrieving process");
				}
				else {
					for (var p in oo) {
						if (oo[p].extract) {
							t = oo[p].extract;
						}
					}

					if(t.indexOf("</b> may refer to:</p>") != -1||t.indexOf("commonly refers to") != -1||t.indexOf("</b> peut faire r�f�rence � :</p>") != -1||t.indexOf("peut faire r\u00e9f\u00e9rence \u00e0\u00a0:</p>") != -1||t.indexOf("</b> steht als Abk�rzung f�r:</p>") != -1||t.indexOf("</b> steht als Abk\u00fcrzung f\u00fcr:</p>") != -1)
								{
									
										arr[index] = 0;
					}
					else{
					
				t = t.replace(/<\s*li[^>]*>(.*?)<\/li\s*>/g,"");
				t = t.replace(/<ul>/g,"");
				t = t.replace(/<\/ul>/g,"");
				//remove the dl -- dd
				t = t.replace(/<\s*dd[^>]*>(.*?)<\/dd\s*>/g,""); 
				t = t.replace(/<dl>/g,"");
				t = t.replace(/<\/dl>/g,"");
				
				//remove the dl -- dt
				t = t.replace(/<\s*dt[^>]*>(.*?)<\/dt\s*>/g,""); 
				t = t.replace(/<dl>/g,"");
				t = t.replace(/<\/dl>/g,"");
				//remove <small>
				t = t.replace(/<\s*small[^>]*>(.*?)<\/small\s*>/g,""); 
				var fobj ={};
				fobj = chunk1.cunkIntoSlides(t,{lang:lang,title:title});
				arr[index]= fobj;
				console.log("Lang :"+lang+"\tLevel 1: "+fobj.level_1+"\tLevel 2: "+fobj.level_2+"\tLevel 3: "+fobj.level_3);
				//summ+=t.split(" ").length;
				//arr[index] = t.split(" ").length;
				//console.log("lllllll: "+index+": is: "+arr[index]);
			}			
		}
		}
		if(cb){
		console.log("CALL BACK"+arr.length);
		cb();
		}
	})
	
}

function createCountXML(obj){
	var xml = "<wordcount>";
	xml+="<level_1>"+obj.level_1+"</level_1>";
	xml+="<level_2>"+obj.level_2+"</level_2>";
	xml+="<level_3>"+obj.level_3+"</level_3>";
	xml+= "</wordcount>";
	return xml;

}

function removeENnoeAscii(ar){
	
		var item = ar[0]; // the first sentence
		console.log("ITEM:"+item);
	

}


// Method to convert an array of arrays with section headings and data
// to an XML string of sections and their wordcounts in separate elements
function convertSectionsToXmlStr(tt2) {
	
	var sectionsXmlStr = "<visual>"

		for (var i = 0; i < tt2[0].length; i++) {
		    var sectionTitle = tt2[0][i];
		    var sectionContent = tt2[1][i];
		    
		    sectionsXmlStr += "<section><name>" + sectionTitle + "</name><value>" + sectionContent.split(" ").length + "</value></section>"
		    
		}
	
	sectionsXmlStr += "</visual>";
		
	return sectionsXmlStr;
	
}
