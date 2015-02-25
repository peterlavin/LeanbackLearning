/**
 * 
 * Javascript specific to Leanback Learning, this file is imported into index.jsp
 * 
 * Author: Peter Lavin
 * 
 * 
 */

	// All declared as a global variable as they are needed by several functions
//	var urlTitle = "";
//	var displayTitle = "";

	var outputlang = "";
	var outputLod = "";
	var langentered = "";

	var name = "";
	var	topics = "";
	
	/* var which 'knows' what stage (1 or 2) the selection and input process is at */
	var submitStage = 'first';
	
	/* Values for the time slider presented to the user */
	var minSeconds = 0;
	var maxSeconds = 0;
	
	/* Word-count values returned in first stage */
	var level_1_wcSec = 0;
	var level_2_wcSec = 0;
	var level_3_wcSec = 0;
	
	var currentWcSec = 0;
	var obj;

$(function() {
	
	// when document is ready and loaded, the event 'onload' is fired.
	// the function in ready(...) is a callback fn
	$(document)
			.ready(
					function() {
						/* Set the time slider colors in preparation for being made visible */
						updateTimeSlider();
						/* Hide all components which are not yet needed */
						document.getElementById("jp_container_1").style.display = "none";
						document.getElementById("time_feedback_labels_row").style.display = "none";
						document.getElementById("time_feedback_row").style.display = "none";
						document.getElementById("wordcount_loader_img").style.display = "none";
						document.getElementById("error_feedback_row").style.display = "none";
						document.getElementById("mediaplayer_loader_img").style.display = "none";
						console.log("Actual measured ht: "
								+ document.documentElement.clientHeight);
						changePadding();
						document.getElementById('button_startover').innerHTML = "Clear <span class=\"glyphicon glyphicon-refresh\"></span>";
						
						$('#button_submit').click(function(event) {
											if (submitStage == 'first') {
												console.log("\n\nSubmit function called for "
																+ submitStage
																+ " stage.");
												name = $('#name').val();
												if (name == "Your name? (optional)") {
													name = "guest";
												}
												topics = $('#topics').val();
												langentered = "auto"; // now detected automatically, auto used for database entry only
												
												
												/* checks for no topics, or only white spaces in topics box */
												var missing = [];
												
												if (topics.trim() == "" || topics == "Presentation topic?") {
													document.getElementById('topics').value = "Presentation topic?";
													missing.push("TOPICS");
												}
												
												if (outputLod == "") {
													missing.push("LEVEL OF DETAIL");
												}
												
												/* checks for no output language selected */
												if (outputlang == "") {
													missing.push("OUTPUT LANGUAGE");
												}
												
												/* Depending on length of missing array, alert the user to what is missing */ 
												
												if(missing.length == 1){
													alert("Please enter...\n\n" + missing[0] + "   for your presentation");
													return false;
												}
												else if(missing.length == 2){
													alert("Please enter...\n\n" + missing[0] + "\n\nand\n\n" + missing[1] + "   for your presentation");
													return false;
												}
												else if(missing.length == 3){
													alert("Please enter...\n\n" + missing[0] + ",\n\n" + missing[1] + "\n\nand\n\n" + missing[2] + "   for your presentation");
													return false;
												}
													
												
												/* Now that all the validation test are passed, show the player and disable
												 the elements of the form */
												document.getElementById('name').disabled = true;
												document
														.getElementById('topics').disabled = true;
												document
														.getElementById('button_submit').disabled = true;
												// enable the start-over/reload button and change text
												document
														.getElementById('button_startover').innerHTML = "Start over <span class=\"glyphicon glyphicon-refresh\"></span>";
												/* Swap the Select output lang for a waiting/loading image */
												document
														.getElementById("output_lang_row").style.display = "none";
												document
														.getElementById("wordcount_loader_img").style.display = "block";
												
												$.post('SequenceServlet', 																{
																	prelimTopics : topics,
																},
																/* The JSON returned here contains seconds values */
																function(
																		postresp) {
																	obj = JSON
																			.parse(postresp);
																	console
																			.log("Level 1: "
																					+ obj.level_1);
																	console
																			.log("Level 2: "
																					+ obj.level_2);
																	console
																			.log("Level 3: "
																					+ obj.level_3);
																	/* Get the three values from the JSON */
																	level_1_wcSec = obj.level_1;
																	level_2_wcSec = obj.level_2;
																	level_3_wcSec = obj.level_3;
																	/* First, check if there are 'words' available, i.e. not zero values */
																	if (level_1_wcSec == 0) {
																		console
																				.log("\nNo results found for: "
																						+ topics);
																		/* Show the 'No results found' feedback, set the text and show it to the user */
																		document
																				.getElementById("error_feedback_row").style.display = "block";
																		document
																				.getElementById('error_feedback').innerHTML = "No results found for <span style='font-weight:bold;'>"
																				+ topics
																				+ "</span>, please try different topic words";
																		/* Change 'Clear' to 'Start Over' to start another job */
																		document
																				.getElementById('button_startover').innerHTML = "Start over <span class=\"glyphicon glyphicon-refresh\"></span>";
																		/* Hide the 'wait' image */
																		document
																				.getElementById("wordcount_loader_img").style.display = "none";
																		/* Disable the 'Level of detail' as it shouldn't be used now */
																		document
																				.getElementById('button_lod_sel').disabled = true;
																		/* NB, the submit button is and remains disabled=true */
																	} else if (level_1_wcSec == "failure") {
																		console
																				.log("\nFailure reported from doPost method for: "
																						+ topics);
																		/* Show the error feedback, set the text and show it to the user */
																		document
																				.getElementById("error_feedback_row").style.display = "block";
																		document
																				.getElementById('error_feedback').innerHTML = "<span style='font-weight:bold'>Error with SSC service, please try again</span>";
																		/* Change 'Clear' to 'Start Over' to start another job */
																		document
																				.getElementById('button_startover').innerHTML = "Start over <span class=\"glyphicon glyphicon-refresh\"></span>";
																		/* Hide the 'wait' image */
																		document
																				.getElementById("wordcount_loader_img").style.display = "none";
																		/* Disable the 'Level of detail' as it shouldn't be used now */
																		document
																				.getElementById('button_lod_sel').disabled = true;
																		/* NB, the submit button is and remains disabled=true */
																	} else {
																		console
																				.log("\nSuccess reported from doPost method for: "
																						+ topics);
																		/* Sets the variable which will force the alternative (next) to be processed on second submit */
																		submitStage = 'second';
																		/* Depending on Level of Detail selected, set the current WC value */
																		if (outputLod == '1') {
																			currentWc = level_1_wcSec;
																		} else if (outputLod == '2') {
																			currentWc = level_2_wcSec;
																		} else if (outputLod == '3') {
																			currentWc = level_3_wcSec;
																		}
																		/* Hide the 'wait' image, make the 'Select prefered time' slider visible */
																		document
																				.getElementById("wordcount_loader_img").style.display = "none";
																		document
																				.getElementById("time_feedback_labels_row").style.display = "block";
																		document
																				.getElementById("time_feedback_row").style.display = "block";
																		/* Process these values and set values on slider */
																		setMinMaxSeconds(
																				outputLod,
																				currentWc);
																		updateTimeFeedback(
																				minSeconds,
																				maxSeconds);
																		// change text in the Submit button to reflect stage
																		document
																				.getElementById('button_submit').innerHTML = "Create audio <span class=\"glyphicon glyphicon-search\"></span>";
																		document
																				.getElementById('button_submit').disabled = false;
																	}
																});
												console
														.log("End of first stage");
											} // end of first stage processing
											else { // this 'else' happens once submitStage is set to second
												console
														.log("\n\nSubmit function called for "
																+ submitStage
																+ " stage.");
												var time = $(
														'#time_preference_slider')
														.val();
												console
														.log("time Pref value is: "
																+ time);
												/* Swap media player image for a wait/loading image */
												document
														.getElementById("mediaplayer_blank_img").style.display = "none";
												document
														.getElementById("mediaplayer_loader_img").style.display = "block";
												/* Set padding for the loader image */
												document
														.getElementById("mediaplayer_loader_img").style.padding = "5px";
												/* De-activate LOD dropdown and time pref slider as no longer needed */
												document
														.getElementById('time_feedback_row').disabled = true;
												//////////////document.getElementById('select_lod_row').disabled = true;
												/* Disable these three items as no longer needed */
												document
														.getElementById('button_lang_sel').disabled = true;
												document
														.getElementById('time_preference_slider').disabled = true;
												document
														.getElementById('button_submit').disabled = true;
												document
														.getElementById('button_lod_sel').disabled = true;
												
												
												/* Call the Servlet method to process/complete the job */
												$.get('SequenceServlet',
																{
																	name : name,
																	topics : topics,
																	time : time,
																	detail : outputLod,
																	langentered : langentered, // now 'auto' as Mostafa is now detecting this automatically
																	outputlang : outputlang
																},
																function(responseText) {
																	document
																			.getElementById("jplayer_inspector_1").style.display = "none";
																	console
																			.log("Playlist text:\n"
																					+ responseText);
																	/* Converts a string in JSON format to be a JSON object */
																	var playlist = JSON
																			.parse(responseText);
																	new jPlayerPlaylist(
																			{
																				jPlayer : "#jquery_jplayer_1",
																				cssSelectorAncestor : "#jp_container_1"
																			},
																			playlist,
																			{
																				swfPath : "playlist/js",
																				supplied : "mp3",
																				wmode : "window",
																				smoothPlayBar : true,
																				keyEnabled : true
																			});
																	/* Variables for elements of the webpage which need to be changed
																	 now that the response is available */
																	// removes the image used to display the media player
																	document
																			.getElementById("mediaplayer_loader_img").style.display = "none";
																	// shows the actual media player, now that it's ready
																	document
																			.getElementById("jp_container_1").style.display = "block";
																	// and set the padding of this div and buttons row to override general padding
																	document
																			.getElementById("media_player_row").style.padding = "20px 20px 20px 20px";
																	document
																			.getElementById("buttons_row").style.padding = "5px 20px 20px 20px";
																}); //end of function(responseText) brace
												
											} // end of main else stm for submit boolean
											
										}); // end submit.click function(event)
						
					}); // end document.ready function
	
	
	
	// Script to set the 'Select Level of Detail (aka as LOD here) option from the dropdown item selects, also sets caption on buttons
	$("#lod_dropdown li a")
				.click(
						function() {
							var outputLodLong = $(this).text();
							if (outputLodLong == 'High level overview') {
								outputLod = '1';
								$(this)
										.parents('.btn-group')
										.find('.dropdown-toggle')
										.html(
												'<b>'
														+ outputLodLong
														+ '</b>'
														+ ' selected <span class="caret"></span>');
							} else if (outputLodLong == 'Detailed introduction') {
								outputLod = '2';
								$(this)
										.parents('.btn-group')
										.find('.dropdown-toggle')
										.html(
												'<b>'
														+ outputLodLong
														+ '</b>'
														+ ' selected <span class="caret"></span>');
							} else if (outputLodLong == 'All detail') {
								outputLod = '3';
								$(this)
										.parents('.btn-group')
										.find('.dropdown-toggle')
										.html(
												'<b>'
														+ outputLodLong
														+ '</b>'
														+ ' selected <span class="caret"></span>');
							}
							// This is called each time a new selection is made, this need to only happen when
							// after the wc times are available
							console
									.log("About to call updateTFb from dropdown change");
							/* Update currentWc after this change to Lod selection */
							if (outputLod == '1') {
								currentWc = level_1_wcSec;
							} else if (outputLod == '2') {
								currentWc = level_2_wcSec;
							} else if (outputLod == '3') {
								currentWc = level_3_wcSec;
							}
							setMinMaxSeconds(outputLod, currentWc);
							updateTimeFeedback(minSeconds, maxSeconds);
						});
	
	

	// Script to set the 'Select output language value from the dropdown item selectes, also sets caption on buttons
	
	$("#language_dropdown li a")
	.click(
			function() {
				var outputlangLong = $(this).text();
				if (outputlangLong == 'English') {
					outputlang = 'en';
					$(this)
							.parents('.btn-group')
							.find('.dropdown-toggle')
							.html(
									'<b>'
											+ outputlangLong
											+ '</b>'
											+ ' selected <span class="caret"></span>');
				} else if (outputlangLong == 'French') {
					outputlang = 'fr';
					$(this)
							.parents('.btn-group')
							.find('.dropdown-toggle')
							.html(
									'<b>'
											+ outputlangLong
											+ '</b>'
											+ ' selected <span class="caret"></span>');
				} else if (outputlangLong == 'German') {
					outputlang = 'de';
					$(this)
							.parents('.btn-group')
							.find('.dropdown-toggle')
							.html(
									'<b>'
											+ outputlangLong
											+ '</b>'
											+ ' selected <span class="caret"></span>');
				}
			});

	
	
					
}); // end of overall function which contains events



// All other functions are below here...

/*
 * Test function for debuging
 */
function testFn() {
	
	alert("Test function was called");
	
}

function get_wordcounts() {
	
	console.log("get_wordcounts() called...");
	//alert("get_wordcounts() called...");

    $('#time_options').show('slow');
//    $('#sample').hide();
    
    // disable topic input box
    // freeze/disable language
    
}

function show_play_button() {
	
	// hide all input forms/buttons
	$('#inputs_lang_level').hide();
	$('#time_options').hide();
	
	// show media player (JPlayer)
	$('#play_button').show();
	
}



function reload() {
	
	window.location.reload(true);

}

function changePadding() {
	//console.log("Actual measured ht: " + document.documentElement.clientHeight);
	// This 'arbitary' number is the px height of the form with zero top and btm padding in 5 of the elements
	var measauredBaseHt = 427;
	var makeup;
	var newPadding = 0;
	makeup = (document.documentElement.clientHeight - measauredBaseHt);
	console.log("Initial makeup reqd where measaured base ht is "
			+ measauredBaseHt + " is: " + makeup);
	if (makeup < 0) {
		makeup = 0;
	}
	console.log("Req total makeup is: " + makeup);
	// arbitary number got by taking pixel measurements from page
	var newPaddingFloat = (makeup / 10.0);
	//console.log("newPaddingFloat is: " + newPaddingFloat);
	var newPadding = Math.round(newPaddingFloat);
	console.log("newPadding is : " + newPadding);
	//////////alert("Ht and calculated new padding is: " + document.documentElement.clientHeight + ":" + newPadding);
	if (newPadding < 3) {
		newPadding = 3;
	}
	var elmList = document.getElementsByClassName('form_row_resize');
	for (i = 0; i < elmList.length; i++) {
		elmList[i].style.padding = newPadding + "px 20px " + newPadding
				+ "px 20px";
		// console.log("Found: " + elmList[i].style.getPropertyValue('padding-top'));
	}
	/* Special cases for the 'time_feedback_row' as this is two elements joined */
	/* and needs less padding */
	var specialCasePadding = document
			.getElementById('time_feedback_row').style
			.getPropertyValue('padding-bottom');
	specialCasePadding = specialCasePadding.substring(0,
			specialCasePadding.length - 2);
	var halfThis = Math.ceil((specialCasePadding / 2));
	var thirdThis = Math.ceil((specialCasePadding / 3));
	/* Set top and bottom padding for both (label and slider) of time_feedback_row to this half value */
	document.getElementById('time_feedback_min').style.padding = halfThis
			+ 'px 0px ' + thirdThis + 'px 10px';
	document.getElementById('time_feedback_caption').style.padding = halfThis
			+ 'px 0px ' + thirdThis + 'px 0px';
	document.getElementById('time_feedback_max').style.padding = halfThis
			+ 'px 10px ' + thirdThis + 'px 0px';
	document.getElementById('time_feedback_row').style.padding = thirdThis
			+ 'px 20px ' + newPadding + 'px 20px';
	/* Special case for submit/clear buttons in stage 2 due to playlist visibility */
	document.getElementById("buttons_row").style.padding = '5px 20px '
			+ newPadding + 'px 20px';
	/* End of special cases padding changes */
	var doubleNewPadding = (newPadding * 1.85);
	doubleNewPadding = Math.round(doubleNewPadding);
	// pixel rounding causes white space at btm at some viewport sized, this fills it with padding
	// changes here happen in the very bottom row only
	document.getElementById("buttons_row").style.paddingBottom = doubleNewPadding
			+ "px";
	console.log("End of this changePadding() pass");
}


function updateTimeFeedback(min, max) {
	console.log("uTF called");
	/* Restore slider to mid-point */
	$("#time_preference_slider").attr("value", "42");
	updateTimeSlider();
	/* Format seconds value into minutes, where values are small, use text/fixed values */
	if (submitStage == "second") {
		if (min < 60) {
			document.getElementById('time_feedback_min').innerHTML = "<span style='font-weight:bold'>Less than 30 sec</span>";
		} else if (min < 90) {
			document.getElementById('time_feedback_min').innerHTML = "<span style='font-weight:bold'>About 1 minute</span>";
		} else {
			document.getElementById('time_feedback_min').innerHTML = "<span style='font-weight:bold'>"
					+ Math.round(min / 60) + " minutes</span>";
		}
		/* Do the same for the maximum values */
		if (max < 60) {
			document.getElementById('time_feedback_max').innerHTML = "<span style='font-weight:bold'>Less than 30 sec</span>";
		} else if (max < 90) {
			document.getElementById('time_feedback_max').innerHTML = "<span style='font-weight:bold'>About 1 minute</span>";
		} else {
			document.getElementById('time_feedback_max').innerHTML = "<span style='font-weight:bold'>"
					+ Math.round(max / 60) + " minutes</span>";
		}
	} // end of submitStage if stm
}


function setMinMaxSeconds(outputLod, seconds) {
	minSeconds = seconds * 0.05; // corresponds to the 5% min value in the time_preference_slider
	maxSeconds = seconds * 0.8; // corresponds to the 80% min value in the time_preference_slider
}


function updateTimeSlider() {
	var val = ($('#time_preference_slider').val() - $(
			'#time_preference_slider').attr('min'))
			/ ($('#time_preference_slider').attr('max') - $(
					'#time_preference_slider').attr('min'));
	$('#time_preference_slider').css(
			'background-image',
			'-webkit-gradient(linear, left top, right top, '
					+ 'color-stop(' + val + ', #381D60), ' // color below slider
					+ 'color-stop(' + val + ', #EAEFDE)' + ')'); // color above slider
}

