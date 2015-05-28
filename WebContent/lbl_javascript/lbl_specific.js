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
var lastEndedValue = 0;

$(function() {

	$(document).ready(function() {
		
		
				// hide the test buttons, TODO remove when finished dev
				//$('#testbuttons').hide();
		
				// Set initial button visiblity as needed
		        $('#play_button').hide();
		        $('#pause_button').hide();
		        $('#stop_button').hide();
		        $('#startover').hide();
		    	$('#jp_container_1').hide();
//		    	$("#time_feedback").hide();
//		    	$("#time_feedback").css("visibility", "hidden");
		    	
		    	
		        
		        // Set language and detail buttons to default highlighting
		  		$('#en_btn').addClass("output_lang_btn_active");
				$('#2_lod_btn').addClass("output_lang_btn_active");
				
				/* var which 'knows' what stage (1 or 2) the selection and input process is at */
				submitStage = 'first';
				
				// Disable the continue button (activated when a input is made)
//				$("#button_continue").attr("disabled", true);
				setContinueDisabled();
			
				$('#continue_image').click(function(event) {
					
					///////////////////////////////////////////////////////////////////////////////////
					/* **********************************************************************************
					 *  if idnum and name are undefined, set them to some value for testing in input_dev.jsp use
					 * TODO remove this when in index.jsp
					 */
					
					if (submitStage == 'first') {
						
						// Validate 'topics' entered, it can't be empty or be just white spaces 
						if (topics = $('#topics').val().trim() == "") {
							//alert("Please enter what you would like to learn about");
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
						$('#logo_image').addClass("logo_image_moving");
						$('#logo_image').removeClass("logo_image");
						setContinueDisabled();
						
						// Show the 'Start-Over' button in case of failures
						$('#startover').show();
					
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
											setErrorMsg("Error: No presentation available for <span style='font-weight:bold;'>" +
													topics + "</span>, please try another topic");
											
											$('#logo_image').removeClass("logo_image_moving");
											$('#logo_image').addClass("logo_image");
											$('#startover').show();
											
										}
										else if (level_1_wcSec == "failure") {

											console.log("\nFailure reported from doPost method for: " + topics);
											
											setErrorMsg("SSC failure for: " + topics);
											
											$('#logo_image').removeClass("logo_image_moving");
											$('#logo_image').addClass("logo_image");
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
										
										$('#logo_image').removeClass("logo_image_moving");
										$('#logo_image').addClass("logo_image");
										setContinueActive();
										$('#startover').show();
										restoreDetailButtons();
										
										}
										
									}); //end of function(responseText) brace			
					}
					
					else if(submitStage == 'second'){
						
						console.log("\n\nSecond phase has been called, ready to call SSC for content\n\n");
						
						greyDetailButtons();
						
						greyTimeButtons();
						
						$('#logo_image').removeClass("logo_image");
						$('#logo_image').addClass("logo_image_moving");
						
						setContinueDisabled();
						
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
						
						var returnedJsonArray = JSON.parse(responseText);
						
						// Get the first part of the JSON, the (estimated) seconds of the presentation
						var retSecondsObj = returnedJsonArray[0];
						
						retSeconds = retSecondsObj.seconds;
						
						// Get the second part of the JSON, the actual playlist
						var playlist = returnedJsonArray[1];
						
						
						// vis debug code was here
						
						
						
						// Get URL of first part and check that there is actually some audio there to play
						
						var firstAudioPart = eval(returnedJsonArray[1]);
						
						var audioUrl = firstAudioPart[0].mp3; 
						console.log("\nFirst audio URL... " + audioUrl);
						
						// if retSeconds is 0, there was a problem, details
						// of the failure are contained in the 'playlist' part
						// of the JSON returned, this is displayed to the user
						
						if(retSeconds==0){
							
							var error_message = firstAudioPart[0].title;
							var error_message_a = firstAudioPart[1].title;
						
							setErrorMsg(error_message + ", " + error_message_a);
							
							$('#logo_image').removeClass("logo_image_moving");
							$('#logo_image').addClass("logo_image");
							$('#startover').show();
							$('#play_button').hide();
							
							// TODO, hide the visualisation toggle buttons on the UI (if visible)
							
						}
						
						// if seconds is not 0, proceed to set up the media player
						// and create the progress bar, etc.
						else {
						
												
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
								
								lastEndedValue++;

								console.log(lastEndedValue + " of " + Object.keys(thisPlayer.playlist).length + " in playlist completed");
								
								if(Object.keys(thisPlayer.playlist).length == 1){
									console.log("End of SINGLE item playlist, now stopping");
									// stop now if there is only one item in the playlist
									localStopAndReset();
								}
								//else if((thisPlayer.current + 1) == Object.keys(thisPlayer.playlist).length && (thisPlayer.current + 1) == lastEndedValue){
								else if((lastEndedValue) == Object.keys(thisPlayer.playlist).length){
									console.log("End of last item in multi-item playlist, now stopping");
									localStopAndReset();
								}
								
							}
						});
						
						//////////////////// start of modified code
						
						// Now that the Jplayer is in place, create the visualation div

						// Get the visual data array from the 'array of arrays' returned
						var visualData = returnedJsonArray[2];
						
						
						// Some debug testing...
						if(visualData){
							var firstVisualPart = eval(returnedJsonArray[2])
							console.log("Sample (first) visual data... " + firstVisualPart[0].name);
						}
						else {
							// If no visual data is available, this logic is used
							console.log("No visual data found");
							visualData = "[{\'name\': \'No Data\',\'value\': 0}]";
						}
						
						// array from returned JSON... visualData
						// Capitalise the first word of 'topics' for heading of the Treemap
						var capitalisedTopics = topics.charAt(0).toUpperCase() + topics.slice(1);
						
				    
					    $('#visualContainer').highcharts({
				    	    series: [{
				            type: "treemap",
				            layoutAlgorithm: 'squarified', 
				            data: visualData
					        }],
					        title: {
					            text: capitalisedTopics
					        },
					    plotOptions: {
					    	series: {
					        	dataLabels:{overflow:'none', crop:true, enabled:true},
					            cursor: 'pointer',
					            point: {
					                events: {
					                    click: function () {
					                    	
					                        alert('Category: ' + this.name);
					                        
					                    }
					                }
					            }
					        }
					    },
					    });
				            
						$("#playerProgressBar").css("display", "inline");
						$('#logo_image').removeClass("logo_image_moving");
						$('#logo_image').addClass("logo_image");
						$('#continue_image').css("display", "none");
						$('#startover').removeClass("btn-reset");
						$('#startover').addClass("btn-reset-alone");
						$('#play_button').css("display", "inline");
						$('#pause_button').css("display", "none");
						$('#stop_button').css("display", "none");
						$('#lang_lod_play').css("display", "none");
						$('#detail_buttons').css("display", "none");
						$('#time_feedback').css("display", "none");
						$('#previsualContainer').css("display", "inline-block");
						$('#visualContainer').css("display", "inline-block");
						$('#continue_loader_play').css("display", "inline");
						setUpProgressBar(retSeconds);
						
				            
//				            data: [{
//				                name: 'TEST DATA',
//				                value: 296
//				            }, {
//				                name: 'History',
//				                value: 249
//				            }, {
//				                name: 'Geography',
//				                value: 18
//				            }, {
//				                name: 'Climate',
//				                value: 126
//				            }, {
//				                name: 'Culture',
//				                value: 253
//				            }, {
//				                name: 'Media - Broadcasting',
//				                value: 90
//				            }, {
//				                name: 'Places of interest',
//				                value: 203
//				            }
//				            , {
//				                name: 'Local gov, politics',
//				                value: 112
//				            }, {
//				                name: 'Economy - Retail',
//				                value: 71
//				            }, {
//				                name: 'Transport - Air',
//				                value: 22
//				            }, {
//				                name: 'Education',
//				                value: 72
//				            }, {
//				                name: 'Sport',
//				                value: 13
//				            }, {
//				                name: 'Demographics',
//				                value: 96
//				            }]
				    	    
				    	    

						} // end of seconds == 0 if/else stm
						
							
						}); //end of function(responseText) brace
					
					} // end of submitStage if, else if
					
				}); // end submit.click function(event)
					
		}); // end of (document).ready(function) {} brace

	}); // end of overall function which contains events




function setContinueActive() {
	
	$("#continue_image").attr("disabled", false);
	$("#continue_image").css("opacity", "1.0");
	
	
}

function setContinueDisabled() {
	
	$("#continue_image").attr("disabled", true);
	$("#continue_image").css("opacity", "0.5");
	
}


function validateInput(){
	
	// Validate 'topics' entered, it can't be empty or be just white spaces 
	if (topics = $('#topics').val().trim() == "") {
		
		setContinueDisabled();
				
		document.getElementById('topics').value = "";
		
		document.getElementById('topics').placeholder = "What do you want to learn about?";
		
	}
	else {
		
		setContinueActive();
		
	}
	
}

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
	$('#play_button').css("display", "none");
	$('#pause_button').css("display", "inline");
	$('#stop_button').css("display", "inline");
	playing = true;
	
}

function localPause() {
	
	$("#jquery_jplayer_1").jPlayer("pause");
	$('#play_button').css("display", "inline");
	$('#pause_button').css("display", "none");
	$('#stop_button').css("display", "none");
	
	playing = false;
}

function localStopAndReset(){
	
	$("#jquery_jplayer_1").jPlayer("stop");
	
	// revert to track one in the playlist
	if(thisPlayer){
		thisPlayer.select(0);
	}
	
	// set global var playing to false to stop progress bar
	playing = false;
	
	// set up buttons for restarting TODO, add stop button when ready
	$('#play_button').css("display", "inline");
	$('#pause_button').css("display", "none");
	$('#stop_button').css("display", "none");
	
	// reset progress bar to zero again
	clearInterval(intervalTimer);
		
	// Create a new progress bar using the global var retSeconds
	setUpProgressBar(retSeconds);
	
	// reset the lastEndedValue to zero
	lastEndedValue = 0;
			
}




function testFn() {
	
	console.log(" -- testFn() called --");
	//$("#time_feedback").css("visibility", "visible");
	
}

function setErrorMsg(errorMessage){
	
	$("#error_msg").append(errorMessage);
	$("#error_msg").css("display", "inline");
	
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
	
	if(lang != outputlang){
		
	
		// TODO set the language variable here, should also have a default set in doc.ready()
			
		// reset all buttons to standard background
		$('#'+ outputlang + '_btn').addClass("output_lang_btn");
		$('#'+ outputlang + '_btn').removeClass("output_lang_btn_active");
	
		// then set the selected button to be dark with bold text
		$('#'+ lang + '_btn').addClass("output_lang_btn_active");
		$('#'+ lang + '_btn').removeClass("output_lang_btn");
		
		console.log("Output Lang was '" + outputlang + "' now '" + lang + "'");
	
		outputlang = lang;
	
	
	}
	
	
}

function setDetail(detailFromButton){

	/*
	 * Changes are made to buttons only if a button calls for
	 * a change of detail, i.e. it is not a repeat selection
	 * of the same level of detail.
	 */
	if(detailFromButton != detail){
	
	// reset all buttons to standard background
	$('#'+ detail + '_lod_btn').addClass("output_lang_btn");
	$('#'+ detail + '_lod_btn').removeClass("output_lang_btn_active");
	
	// then set the selected button to be dark with bold text
	$('#'+ detailFromButton + '_lod_btn').addClass("output_lang_btn_active");
	$('#'+ detailFromButton + '_lod_btn').removeClass("output_lang_btn");
	
	console.log("Detail was " + detail + " now " + detailFromButton);
	detail = detailFromButton;
	
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

function toggleVisual(){
	
	if($('#visualContainer').is(":hidden")){

		$("#visualContainer").toggle();
				
		// Set logo size to smaller size 
		$("#logo_image").css({ 'height': "50px" });
		$("#logo_image").css({ 'width': "50px" });
		$('#lbl_logo').css({'padding':'15px 0px 0px 0px'});
		
	}
	else{

		$("#visualContainer").toggle();
		
		// Revert size of logo size to orig 
		$("#logo_image").css({'height': "143px"});
		$("#logo_image").css({'width': "143px"});
		
		$('#lbl_logo').css({'padding':'15px 15px 15px 15px'});
		

		
	}
	
	$('#topic_input').toggle();
	$('#lang_lod_play').toggle();
	
	// (Hack alert!!!) need to reset/refresh the css settings
	// to get the width to be correct DIDN'T WORK
//	$('#visualContainer').css({'min-width':'300px'});
//	$('#visualContainer').css({'max-width':'600px'});
//	$('#visualContainer').css({'height':'310px'});
	
	
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
	// Selects all the children of the div and pushes 
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

function createTimeButtons(wcSec){
	
	console.log('Ss now: ' + submitStage + ', wcSec use for time buttons is: ' + wcSec);
	
	var min = wcSec * 0.05;
	var max = wcSec * 0.8;

// 	Empty the div of its placeholder, append a bootstrap div
	$("#time_feedback").empty();
	$("#time_feedback").append('<div class="col-sm-6 col-sm-offset-2 text-left" id="time_feedback_toolbar"></div>');
	
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
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="24" OnClick="setTimeReqd(24)">' + Math.ceil((min + step * 1)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="43" OnClick="setTimeReqd(43)">' + Math.ceil((min + step * 2)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="61" OnClick="setTimeReqd(61)">' + Math.ceil((min + step * 3)/60) + ' min</button>');
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">' + Math.ceil(max/60) + ' min</button>');
		
		setTimeReqd(43);
		
	}
	else {
		var step = (max - min)/5;
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="5" OnClick="setTimeReqd(5)">' + Math.ceil(min/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="25" OnClick="setTimeReqd(25)">' + Math.ceil((min + step * 1)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="35" OnClick="setTimeReqd(35)">' + Math.ceil((min + step * 2)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="50" OnClick="setTimeReqd(50)">' + Math.ceil((min + step * 3)/60) + ' min</button>');
	 	$('#time_feedback_toolbar').append('<button class="btn timeButton" id="65" OnClick="setTimeReqd(65)">' + Math.ceil((min + step * 4)/60) + ' min</button>');
		$('#time_feedback_toolbar').append('<button class="btn timeButton" id="80" OnClick="setTimeReqd(80)">' + Math.ceil(max/60) + ' min</button>');
		
		setTimeReqd(35);
	}
	
// 	Finally, set the div as visible (hidden from page load/ready)
	$("#time_feedback").css("display", "block");
	$("#detail_buttons").css("display", "block");
}

function setTimeReqd(setVal) {
	
	console.log("TIME: " + time);
	if(setVal != time){
		
		console.log("setTimeReqd called: " + setVal);

		// reset all buttons to standart background
		$('#'+time).addClass("timeButton");
		$('#'+time).removeClass("timeButton_active");
		time = setVal;

		// then set the selected button to be dark with bold text
		$('#'+setVal).addClass("timeButton_active");
		$('#'+setVal).removeClass("timeButton");
	}
	
}


function setUpProgressBar(duration) {
	
	
	// Firstly, create the progress bar in the time_feedback div
	
	$("#playerProgressBar").empty();
	
	$("#playerProgressBar").append('<div id="progresslabel"><h1></h1></div>' + 
			'<div class="progress">' +
			'<div id="progressvalue" class="progress-bar" role="progressbar" + aria-valuenow="0" aria-' +
			'valuemin="0" aria-valuemax="100" style="width:0%"></div></div>');
	
	
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

//////////// test for CORS

function createCORSRequest(method, url) {
	  var xhr = new XMLHttpRequest();
	  if ("withCredentials" in xhr) {
	    // XHR for Chrome/Firefox/Opera/Safari.
	    xhr.open(method, url, true);
	  } else if (typeof XDomainRequest != "undefined") {
	    // XDomainRequest for IE.
	    xhr = new XDomainRequest();
	    xhr.open(method, url);
	  } else {
	    // CORS not supported.
	    xhr = null;
	  }
	  return xhr;
	}

	// Helper method to parse the title tag from the response.
	function getTitle(text) {
	  return text.match('<title>(.*)?</title>')[1];
	}

	// Make the actual CORS request.
	function makeCorsRequest() {
	  // All HTML5 Rocks properties support CORS.
	  var url = 'http://updates.html5rocks.com';

	  var xhr = createCORSRequest('GET', url);
	  if (!xhr) {
	    alert('CORS not supported');
	    return;
	  }

	  // Response handlers.
	  xhr.onload = function() {
	    var text = xhr.responseText;
	    var title = getTitle(text);
	    alert('Response from CORS request to ' + url + ': ' + title);
	  };

	  xhr.onerror = function() {
	    alert('Woops, there was an error making the request.');
	  };

	  xhr.send();
	}


function startByEnter(ev){
	
		var unicode=ev.charCode? ev.charCode : ev.keyCode
	
		if(unicode === 13){
			
			//$('#continue_image').click();
			
			// need to call validation here also
			//validateInput()
			
			// cannot return 'false' here as it gets issued as the contents of the input box
			return false;
			//return $('#topics').val();
			
		}
//		else {
//			return false;
//		}
		
}
	
	
	
