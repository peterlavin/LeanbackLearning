/**
 * 
 * Javascript specific to Leanback Learning.
 * 
 * Author: Peter Lavin, Jan 2015
 * 
 * 
 */


var submitStage;
var wcSec;
var outputLod;





$(function() {

	$(document).ready(function() {
		
				$("#time_feedback").css("visibility", "hidden");
		        $('#play_button').hide();
		        $('#loader').hide();
		        
		        // Set language and detail buttons to default highlighting
		  		$('#en_btn').css({"background":"#BEBEBE"});
				$('#en_btn').css({"font-weight":"bold"}); // TODO, set the variables also
				$('#2_lod_btn').css({"background":"#BEBEBE"});
				$('#2_lod_btn').css({"font-weight":"bold"});
				
				/* var which 'knows' what stage (1 or 2) the selection and input process is at */
				submitStage = 'first';
			
				console.log('setup() has been called in (document).ready(function() in external JS source file');
				
				
				
				$('#button_submit').click(function(event) {
					
					var arg1 = "one";
					var arg2 = "two";

					console.log("button_submit was click wt: " + arg1 + " : " + arg2);
					
					
					/* Call the Servlet method to process/complete the job */
					$.get('SequenceServlet', {
										testArg1 : arg1,
										testArg2 : arg2
									},
									function(responseText) {
										
										console.log("Test returned is... " + responseText);									
										
									}); //end of function(responseText) brace			
		
					
					
						}); // end submit.click function(event)
					
					
					
					
	}); // end of (document).ready(function) {} brace




}); // end of overall function which contains events





/* For demo only, TODO remove when in app */

function testFn() {
	
	console.log(" -- testFn() called --");
	//$("#time_feedback").css("visibility", "visible");
	
}

function setErrorMsg(){
	
//	$("#time_feedback").show();
	$("#time_feedback").empty();
	$("#time_feedback").append('<div class="btn-toolbar" id="time_feedback_toolbar"></div>');
	$("#time_feedback_toolbar").css({"padding": "0px 05px 0px 5px"});
	$("#time_feedback").append('Test Error Message Test Error Message Test Error Message');
	
}

function toggleSubmitStage(){
	
	// TODO for debug, remove for final ver
	console.log('submitStage was: ' + submitStage);
	
	if(submitStage == 'first'){
		submitStage = 'second';
	}
	else if(submitStage == 'second') {
		submitStage = 'first';
	}
	
	console.log('submitStage is now: ' + submitStage);
	
}

function setLanguage(lang){
	
	// TODO set the language variable here, should also have a default set in doc.ready()
	console.log(lang + " passed.");
	
	// reset all buttons to standard background
	$('.output_lang_btn').css({"background":"#DDDDDD"});
	$('.output_lang_btn').css({"font-weight":"normal"});
	
	// then set the selected button to be dark with bold text
	$('#'+ lang + '_btn').css({"background":"#BEBEBE"});
	$('#'+ lang + '_btn').css({"font-weight":"bold"});
	
}

function setDetail(lod){

	// TODO set the lod variable here
	console.log(lod + " passed.");
	
	// reset all buttons to standard background
	$('.level_of_detail_btn').css({"background":"#DDDDDD"});
	$('.level_of_detail_btn').css({"font-weight":"normal"});
	
	// then set the selected button to be dark with bold text
	$('#'+ lod + '_lod_btn').css({"background":"#BEBEBE"});
	$('#'+ lod + '_lod_btn').css({"font-weight":"bold"});
	
	if (submitStage == 'first') {
		outputLod = lod;
	}
	else {
		if(lod != outputLod){
			createButtons(5555);
		}
	}
	
	

}

function testToggle(){
	
	  $('#play_button').toggle();
	  $('#continue_1').toggle();
	
}


function hideTimeButtons(){
	
	$("#time_feedback").css("visibility", "hidden");
	
}

function toggleLoader(){

	if($('#loader').is(":hidden")){
		$('#loader').show();
		$('#continue_1').hide();
		$('#play_button').hide();
	}
	else{
		$('#loader').hide();
		$('#continue_1').hide();
		$('#play_button').show();
	}
}

function createButtons(wcSec){
	
	// TODO, remove this, for test only
	submitStage = 'second';
	console.log('Ss now: ' + submitStage + ', wcSec passed is: ' + wcSec);
	
	var min = wcSec * 0.05;
	var max = wcSec * 0.8;

// 	Empty the div of its placeholder, append a bootstrap div
	$("#time_feedback").empty();
	$("#time_feedback").append('<div class="btn-toolbar" id="time_feedback_toolbar"></div>');
	$("#time_feedback_toolbar").css({"padding": "0px 05px 0px 5px"});
	
	if(wcSec < 400){

		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">1 - 4 mins available</button>');
		
		// Call function as there is only one option, user may not select this, same for all other cases also...
		setTimeReqd(80);
	}
	else if(wcSec < 700){
		var step = (max - min)/2;
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="5" OnClick="setTimeReqd(5)">' + Math.ceil(min/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="43" OnClick="setTimeReqd(43)">' + Math.ceil((min + step)/60) + ' min</button>');
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">' + Math.ceil(max/60) + ' min</button>');
		
		setTimeReqd(43);
		
	}
	else if(wcSec < 3000) {
		var step = (max - min)/4;
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="5" OnClick="setTimeReqd(5)">' + Math.ceil(min/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="24" OnClick="setTimeReqd(24)">' + Math.ceil((min + step)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="43" OnClick="setTimeReqd(43)">' + Math.ceil((min + step * 2)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="61" OnClick="setTimeReqd(61)">' + Math.ceil((min + step * 3)/60) + ' min</button>');
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">' + Math.ceil(max/60) + ' min</button>');
		
		setTimeReqd(43);
		
	}
	else {
		var step = (max - min)/5;
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="5" OnClick="setTimeReqd(5)">' + Math.ceil(min/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="25" OnClick="setTimeReqd(25)">' + Math.ceil((min + step)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="35" OnClick="setTimeReqd(35)">' + Math.ceil((min + step * 2)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="50" OnClick="setTimeReqd(50)">' + Math.ceil((min + step * 3)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="65" OnClick="setTimeReqd(65)">' + Math.ceil((min + step * 4)/60) + ' min</button>');
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">' + Math.ceil(max/60) + ' min</button>');
		
		setTimeReqd(35);
		
	}
	
// 	Finally, set the div as visible (hidden from page load/ready)
	$("#time_feedback").css("visibility", "visible");

}

function setTimeReqd(setVal) {
	
	// TODO use this to set the (global) time variable for sending to Servlet doGet() method
		
	console.log("setTimeReqd called: " + setVal + ":");
		
	// reset all buttons to standart background
	$('.timeButton').css({"background":"#DDDDDD"});
	$('.timeButton').css({"font-weight":"normal"});
	// then set the selected button to be dark with bold text
	$('#'+setVal).css({"background":"#BEBEBE"});
	$('#'+setVal).css({"font-weight":"bold"});
}


