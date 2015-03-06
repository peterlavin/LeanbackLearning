/**
 * 
 * Javascript specific to Leanback Learning.
 * 
 * Author: Peter Lavin, Jan 2015
 * 
 * 
 */


var wcSec;

var outputlang = "en";
var detail = "2";
var langentered = "";

var	topics = "";

/* var which 'knows' what stage (1 or 2) the selection and input process is at */
var submitStage = 'first';

/* Word-count values returned in first stage */
var level_1_wcSec = 0;
var level_2_wcSec = 0;
var level_3_wcSec = 0;

var currentWcSec = 0;
var obj;

// set here when returned from the doPost() call, passed again to doGet() method for use there.
var jobID;


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
				
				
				
				$('#button_continue').click(function(event) {
					
					if (submitStage == 'first') {
						
						/*
						 * Validate 'topics' entered, can't be empty or just white spaces 
						 */
						if (topics = $('#topics').val().trim() == "") {
							alert("Please enter what you would like to learn about");
							document.getElementById('topics').value = "";
							document.getElementById('topics').placeholder = "What do you want to learn about?";
							return false;
						}
					
						// Set topics
						topics = $('#topics').val();
						
						// Disable topics input, grey-out lang and detail buttons
						$("#topics").attr("disabled", true);
						greyLanguageButtons();
						greyDetailButtons();
						
						$('#loader').show();
						$('#continue').hide();
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
					///////////////////////////////////////////////////////////////////////////////////
					/* **********************************************************************************
					 *  if idnum and name are undefined, set them to some value for testing in input_dev.jsp use
					 * TODO remove this when in index.jsp
					 */
					
					if(typeof idnum == 'undefined'){
						var idnum = '123456789'; 
					};
					
					if(typeof name == 'undefined'){
					    var name = 'Joe Soap'; 
					};
					console.log("button_continue was click wt:\n\nidnum: " + idnum + "\nname: " + name + "\ntopics: " + topics + "\ndet: " + detail + "\nlang: " + outputlang);
					
					///////////////////////////////////////////////////////////////////////////////////
					
					/* Call the Servlet method to process/complete the job */
					$.post('SequenceServlet', {
						
										idnum : idnum,
										name : name,
										topics : topics,
										init_detail : detail,
										outputlang : outputlang
										
									},
									function(responseText) {
										
										
										
										
										console.log("Test returned is... " + responseText);
										
										submitStage = 'second';
										console.log('submitStage is now: ' + submitStage);
										
										obj = eval(responseText);
										
										/* Get the three values from the JSON */
										level_1_wcSec = obj[1].level_1;
										level_2_wcSec = obj[1].level_2;
										level_3_wcSec = obj[1].level_3;
										
										
										if (level_1_wcSec == 0) {
											// show message to user - there is nothing available, try something different
											
											
											
											
											
											
											
											
											
											
										}
										else if (level_1_wcSec == "failure") {
											console.log("\nFailure reported from doPost method for: " + topics);
										}
										else {
											
											console.log("\nSuccess reported from doPost method for: " + topics);
											
										/* Sets the variable which will force the alternative (next) to be processed on second submit */
										submitStage = 'second';
										
										/* Set the current WC value, this depends on level of detail selected */
										setCurrentWcSec();
											
										}
										
										
										console.log("LOD is currently set to be " + detail);
										
										createButtons(currentWcSec);
										
										$('#loader').hide();
										$('#continue').show();
										restoreDetailButtons();
										
									}); //end of function(responseText) brace			
					}
					else if(submitStage == 'second'){
						
					console.log("\n\nSecond phase has been called, ready to call SSC for content\n\n");
					
					greyDetailButtons();
					
					$('#time_feedback_toolbar').fadeTo(250, 0.6);
					
					// this doesnt work !!!!!!!!! need to do individual buttons!!!!!!!
					$('#time_feedback_toolbar').attr("disabled", true);
					
					greyTimeButtons();
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
						
						//   call doGet, create jPlayer instance
						
					}
					
					
						}); // end submit.click function(event)
					
					
					
					
	}); // end of (document).ready(function) {} brace




}); // end of overall function which contains events
























/* For demo only, TODO remove when in app */


function setCurrentWcSec() {

	// Recall that detail is a global variable
	if (detail == '1') {
		currentWcSec = level_1_wcSec;
	} else if (detail == '2') {
		currentWcSec = level_2_wcSec;
	} else if (detail == '3') {
		currentWcSec = level_3_wcSec;
	}
	
} 







function testFn() {
	
	console.log(" -- testFn() called --");
	//$("#time_feedback").css("visibility", "visible");
	
}

function setErrorMsg(){
	
//	$("#time_feedback").show();
	$("#time_feedback").empty();
	$("#time_feedback").append('<div class="btn-toolbar" id="time_feedback_toolbar"></div>');
	// Custom padding to keep flow when buttons are replaced with an error message
	$("#time_feedback_toolbar").css({"padding": "14px 0px 0px 14px"});
	$("#time_feedback").append('Error Message: Placeholder failure message feedback for user');
	
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
	
	outputlang = lang;
	
}

function setDetail(detailFromButton){

	/*
	 * Changes are made to buttons only if a button calls for
	 * a change of detail, i.e. it is not a repeat selection
	 * of the same level of detail.
	 */
	if(detailFromButton != detail){
	
	// reset all buttons to standard background
	$('.level_of_detail_btn').css({"background":"#DDDDDD"});
	$('.level_of_detail_btn').css({"font-weight":"normal"});
	
	// then set the selected button to be dark with bold text
	$('#'+ detailFromButton + '_lod_btn').css({"background":"#BEBEBE"});
	$('#'+ detailFromButton + '_lod_btn').css({"font-weight":"bold"});
	
	console.log("Detail was " + detail);
	detail = detailFromButton;
	console.log("Detail now is " + detail);
	
		if(submitStage == "second"){
			
			setCurrentWcSec();
			
			createButtons(currentWcSec);
	
		}
	}

}





function testToggle(){
	
	  $('#play_button').toggle();
	  $('#continue_1').toggle();
	
}

function greyLanguageButtons(){
	
	$("#fr_btn").fadeTo(250, 0.6);
	$("#fr_btn").attr("disabled", true);
	
	$("#en_btn").fadeTo(250, 0.6);
	$("#en_btn").attr("disabled", true);
	
	$("#de_btn").fadeTo(250, 0.6);
	$("#de_btn").attr("disabled", true);
	
}

function greyDetailButtons() {
	
	$("#1_lod_btn").fadeTo(250, 0.6);
	$("#1_lod_btn").attr("disabled", true);
	
	$("#2_lod_btn").fadeTo(250, 0.6);
	$("#2_lod_btn").attr("disabled", true);
	
	$("#3_lod_btn").fadeTo(250, 0.6);
	$("#3_lod_btn").attr("disabled", true);
	
}

function restoreDetailButtons(){
	
	$("#1_lod_btn").animate({opacity:'1'});
	$("#1_lod_btn").attr("disabled", false);
	
	$("#2_lod_btn").animate({opacity:'1'});
	$("#2_lod_btn").attr("disabled", false);
	
	$("#3_lod_btn").animate({opacity:'1'});
	$("#3_lod_btn").attr("disabled", false);
	
}

function greyTimeButtons(){
	
	//$('#time_feedback_toolbar').
	
	
	
	
	
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


