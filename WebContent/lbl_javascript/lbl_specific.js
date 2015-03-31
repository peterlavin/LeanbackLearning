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
var time;

var currentWcSec = 0;
var obj;

var playing = false;

// set here when returned from the doPost() call, passed again to doGet() method for use there.
var jobID;

var retSeconds;

var thisPlayer;

var intervalTimer;

$(function() {

	$(document).ready(function() {
		
		
				// Set initial button visiblity as needed
		        $('#play_button').hide();
		        $('#pause_button').hide();
		        $('#loader').hide();
		        $('#startover').hide();
		    	$('#jp_container_1').hide();
		    	$("#time_feedback").css("visibility", "hidden");
		        
		        // Set language and detail buttons to default highlighting
		  		$('#en_btn').css({"background":"#BEBEBE"});
				$('#en_btn').css({"font-weight":"bold"});
				$('#2_lod_btn').css({"background":"#BEBEBE"});
				$('#2_lod_btn').css({"font-weight":"bold"});
				
				/* var which 'knows' what stage (1 or 2) the selection and input process is at */
				submitStage = 'first';
			
				$('#button_continue').click(function(event) {
					
					///////////////////////////////////////////////////////////////////////////////////
					/* **********************************************************************************
					 *  if idnum and name are undefined, set them to some value for testing in input_dev.jsp use
					 * TODO remove this when in index.jsp
					 */
					
					if(typeof idnum == 'undefined'){
						var idnum = '123456789'; 
					}
					
					if(typeof name == 'undefined'){
					    var name = 'Joe Soap'; 
					}
					
					if (submitStage == 'first') {
						
						// Validate 'topics' entered, it can't be empty or be just white spaces 
						if (topics = $('#topics').val().trim() == "") {
							alert("Please enter what you would like to learn about");
							document.getElementById('topics').value = "";
							document.getElementById('topics').placeholder = "What do you want to learn about?";
							return false;
						}
					
						// After validatin, get topics from input
						topics = $('#topics').val();
						
						// Disable topics input, grey-out lang and detail buttons
						$("#topics").attr("disabled", true);
						greyLanguageButtons();
						greyDetailButtons();
						
						// Show the loader and wait for a response from SSC
						$('#loader').show();
						$('#continue').hide();
					
					console.log("button_continue was click wt:\n\nidnum: " + idnum + "\nname: " + name + "\ntopics: " + topics + "\ndet: " + detail + "\nlang: " + outputlang);
					
					// Call the Servlet method to get word-count information for topics
					$.post('SequenceServlet', {
						
										idnum : idnum,
										name : name,
										topics : topics,
										init_detail : detail,
										outputlang : outputlang
										
									},
									function(responseText) {
										
										submitStage = 'second';
										console.log('submitStage is now: ' + submitStage);

										console.log("Test returned is... " + responseText);
										
										obj = eval(responseText);
										
										// Get the job ID number from the response
										jobID = obj[0].jobid;
										
										// Get the three values from the JSON
										level_1_wcSec = obj[1].level_1;
										level_2_wcSec = obj[1].level_2;
										level_3_wcSec = obj[1].level_3;
										
										// level_1 is used to determine success or not
										console.log("level_1_wcSec = " + level_1_wcSec);
										
										if (level_1_wcSec == 0) {
											
											// show message to user, i.e. there is nothing available, try something different
											setErrorMsg("No presentation available for <span style='font-weight:bold;'>" + topics + "</span>, please try something else");
											
											$('#loader').hide();
											$('#startover').show();
											
										}
										else if (level_1_wcSec == "failure") {

											console.log("\nFailure reported from doPost method for: " + topics);
											
											setErrorMsg("SSC failure for: " + topics);
											
											$('#loader').hide();
											$('#startover').show();
											
										}
										else {
											
											console.log("\nSuccess reported from doPost method for: " + topics);
											
											/* Sets the variable which will force the alternative (next) to be processed on second submit */
											submitStage = 'second';
										
											/* Set the current WC value, this depends on level of detail selected */
											setCurrentWcSec();
											
										console.log("LOD is currently set to be " + detail);
										
										createTimeButtons(currentWcSec);
										
										$('#loader').hide();
										$('#continue').show();
										restoreDetailButtons();
										
										}
										
									}); //end of function(responseText) brace			
					}
					
					else if(submitStage == 'second'){
						
						console.log("\n\nSecond phase has been called, ready to call SSC for content\n\n");
						
						greyDetailButtons();
						
						greyTimeButtons();
						
						$('#loader').show();
						
						$('#continue').hide();
						
						$.get('SequenceServlet',
								{
									idnum : idnum,
									name : name,
									jobid : jobID,
									topics : topics,
									time : time,
									final_detail : detail,
									outputlang : outputlang
								},
								function(responseText) {
									
						console.log("Full returned...\n" + responseText);
						//var playlist = [{"title":"Part 1 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_1.mp3"},{"title":"Part 2 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_2.mp3"},{"title":"Part 3 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_3.mp3"}];
						
						var playlistAndDuration = JSON.parse(responseText);
						
						var retSecondsObj = playlistAndDuration[0];
						retSeconds = retSecondsObj.seconds;
						
						var playlist = playlistAndDuration[1];
												
						
					 	
						thisPlayer = new jPlayerPlaylist({
							jPlayer: "#jquery_jplayer_1",
							cssSelectorAncestor: ""
						},
						playlist,
						{
							swfPath: "playlist/js",
							supplied: "mp3",
							wmode: "window",
							smoothPlayBar: true,
							cssSelector: {title: "#title", play: "#play", pause: "#pause", stop: "#stop", currentTime: "#currentTime", duration: "#duration"},
							keyEnabled: true,
							ended : function() {
								console.log("Ended called at..." + thisPlayer.current)
								
								console.log("Length is: " + Object.keys(thisPlayer.playlist).length);
								
								if((thisPlayer.current + 1) == Object.keys(thisPlayer.playlist).length){
									
									console.log("End of last item in playlist, stopping");
									//localStop();
									// set a var and stop the player in the setInterval loop on the next 'paused == true'
								}
								
							}
						});
						
						
						
						setUpProgressBar(retSeconds);
						$('#loader').hide();
						$('#play_button').show();
						
						
						
						
						
						}); //end of function(responseText) brace
					
					} // end of submitStage if, else if
					
				}); // end submit.click function(event)
					
		}); // end of (document).ready(function) {} brace

	}); // end of overall function which contains events






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


function reload() {
	
	window.location.reload(true);

}

/*
 * Show and hide the JPlayer <div>
 */
function togglePlayer(){
	$('#jp_container_1').toggle();
}



/*
 * Scripts for two local buttons, play and pause
 */
function localPlay() {
	
	$("#jquery_jplayer_1").jPlayer("play");
	$('#play_button').hide();
	$('#pause_button').show();
	
	playing = true;
	
}

function localPause() {
	
	$("#jquery_jplayer_1").jPlayer("pause");
	$('#play_button').show();
	$('#pause_button').hide();
	
	playing = false;
}

function localStop(){
	
	$("#jquery_jplayer_1").jPlayer("stop");
	
	// revert to track one in the playlist
	thisPlayer.select(0);
	
	// set global var playing to false to stop progress bar
	playing = false;
	
	// set up buttons for restarting TODO, add stop button when ready
	$('#play_button').show();
	$('#pause_button').hide();
	
	// reset progress bar to zero again
	clearInterval(intervalTimer);
		
	// Create a new progress bar using the global var retSeconds
	setUpProgressBar(retSeconds);
			
}




function testFn() {
	
	console.log(" -- testFn() called --");
	//$("#time_feedback").css("visibility", "visible");
	
}

function setErrorMsg(errorMessage){
	
	$("#time_feedback").empty();
	$("#time_feedback").append('<div class="btn-toolbar" id="time_feedback_toolbar"></div>');
	
	// Custom padding to keep flow when buttons are replaced with an error message
	$("#time_feedback_toolbar").css({"padding": "14px 0px 0px 14px"});
	$("#time_feedback").append(errorMessage);
	
	$("#time_feedback").css("visibility", "visible");
	
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
			
			createTimeButtons(currentWcSec);
	
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
	
	var children = [];
	$("#time_feedback_toolbar").children().each(function() {
		children.push(this);
	});
	
	
	for(i=0; i< children.length; i++ ){
		
		var thisChild = children[i];
        $(children[i]).fadeTo(250, 0.6);
		$(thisChild).attr("disabled", true);
		
	}
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

function createTimeButtons(wcSec){
	
	console.log('Ss now: ' + submitStage + ', wcSec use for time buttons is: ' + wcSec);
	
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
	
	time = setVal;
	
		
	// reset all buttons to standart background
	$('.timeButton').css({"background":"#DDDDDD"});
	$('.timeButton').css({"font-weight":"normal"});
	// then set the selected button to be dark with bold text
	$('#'+setVal).css({"background":"#BEBEBE"});
	$('#'+setVal).css({"font-weight":"bold"});
}


function setUpProgressBar(duration) {
	
	
	// Firstly, create the progress bar in the time_feedback div
	
	$("#time_feedback").empty();
	
	$("#time_feedback").append('<div class="container"><div id="progresslabel"><h1></h1></div>' + 
			'<div class="progress">' +
			'<div id="progressvalue" class="progress-bar" role="progressbar" + aria-valuenow="0" aria-' +
			'valuemin="0" aria-valuemax="100" style="width:0%"></div></div></div>');
	
	
	// initialise times for progress  bar
	var initTimer = duration, minutes, seconds;
    initMinutes = parseInt(initTimer / 60, 10);
    initSeconds = parseInt(initTimer % 60, 10);
    initSeconds = initSeconds < 10 ? "0" + initSeconds : initSeconds;
    
	document.getElementById('progresslabel').innerHTML = "0:00" + " of " + initMinutes + ":" + initSeconds + " played";
	
	var timer = 0, minutes, seconds;
	
    intervalTimer = setInterval(function () {
    	
      if(playing){
    	
        minutes = parseInt(timer / 60, 10);
        seconds = parseInt(timer % 60, 10);
        seconds = seconds < 10 ? "0" + seconds : seconds;
        
//        console.log(duration);
//        console.log("Timer is: " + timer + "\n");

        currentValue = Math.round((timer/duration) * 100);
//        
        document.getElementById('progressvalue').setAttribute("style", "width:" + currentValue + "%");
        document.getElementById('progressvalue').setAttribute("aria-valuenow",currentValue);
    	document.getElementById('progresslabel').innerHTML = minutes + ":" + seconds + " of " + initMinutes + ":" + initSeconds + " played";
        
        ++ timer;
        
        // when timer reaches 0, stop
        if (timer > duration) {
            timer = duration;
        }
        
       }
            
    }, 1000);
}






function togglePlayingStatus(){
	
	if(playing == false){
		playing = true;
	}
	else {
		playing = false;
	}
	
}


