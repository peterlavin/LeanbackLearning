<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>BS Workpad</title>


<!-- 		Two imports related to bootstrap -->
<!-- 		<script src="jplayer_javascript/jquery-1.11.1.js"></script> -->
		<script src="jplayer_javascript/jquery-2.0.0.js"></script>
        <script src="bootstrap/3.2.0/js/bootstrap.min.js"></script>



		<!--  Own script containing all customised Javascript for this page, needs to be imported after jquery -->
		<script type="text/javascript" src="lbl_javascript/lbl_specific.js"></script>
		
		
<!-- 		Bootstrap css  -->
		<meta name="viewport" content="width=device-width, initial-scale=1">
    	<link rel="stylesheet" href="bootstrap/3.2.0/css/bootstrap.min.css">
    	
<!--     	Places an icon in the browser tab -->
    	<link rel="shortcut icon" href="images/favicon.ico" />
    	
    	<!-- 		Own css file for local settings -->
  		<link rel="stylesheet" href="css/lbl_specific.css">
  		
  		
<!--   		Needed until document.ready() is included -->
  		<script type="text/javascript">
	  		function setup(){
  				
//   				$("#time_feedback").css("visibility", "hidden");
//   				// TODO change this to use the g+ signin variable
//   		        $('#usermsg').append($('<h4> Welcome "User Name"</h4>'));
//   		        $('#play_button').hide();
//   		        $('#loader').hide();
  		        
//   		        // Set language and detail buttons to default highlighting
//   		  		$('#en_btn').css({"background":"#BEBEBE"});
//   				$('#en_btn').css({"font-weight":"bold"});
//   				$('#2_lod_btn').css({"background":"#BEBEBE"});
//   				$('#2_lod_btn').css({"font-weight":"bold"});
  				
//   				/* var which 'knows' what stage (1 or 2) the selection and input process is at */
//   				submitStage = 'first';
  			
//   				console.log('setup() has been called');
  			}
  		</script>
  		
  		
  		<script type="text/javascript">

// 	var name = "test_string";
	
// 	$(document).ready(function() {
						

// 					$('#button_submit').click(function(event) {
						
// 						console.log("Test line in submit.click(fn(event)) [HEADER]");

// 												$.get('SequenceServlet', {
// 																	name : name
// 																},
// 																function(responseText) {

// 																	console.log("Returned text:\n" + responseText);

// 																}); //end of function(responseText) brace
											
// 										}); // end submit.click function(event)
						
// 					}); // end document.ready function

	</script>
  		
  		
  		
  		
  		
  		
  		
  		
  		
  		
  		

</head>




<body onload="setup()">




. 
    <div class="container-fluid" id="heading_topic">
       
      <div class="col-xs-4" id="lbl_logo">
       	<img src="images/logo_placeholder2.png" alt="Leanback learning" style="width:100px;height:50px">
      </div>
      
      <div class="col-xs-4" id="welcome_msg">
      	  <div id="usermsg"></div>
      </div>
      
      <div class="col-xs-4" id="sign_out_btm" >
      	<input type="image" src="images/google_signout.png" height="30" width="100" name="saveForm" id="disconnect"/>
      </div>
    
      <form class="form-horizontal" role="form" id="topic_input">
        <div class="form-group">
          <div class="col-sm-10">
            <input type="text" class="form-control" id="topics" placeholder="What do you want to learn about?">
          </div>
        </div>
    </form>
    
    </div>
    
    
    
    
    
    
    
   	<div class="container-fluid" id="lang_lod_play">
	<div class="col-xs-6">
	
		<div class="row btn-toolbar"  id="output_lang">  
			<button class="btn output_lang_btn" id="fr_btn"OnClick="setLanguage('fr')">French</button>
			<button class="btn output_lang_btn" id="en_btn" OnClick="setLanguage('en')">English</button>
			<button class="btn output_lang_btn" id="de_btn" OnClick="setLanguage('de')">German</button>
		</div>
	    <div class="row btn-toolbar" id="level_of_detail">  
			<button class="btn level_of_detail_btn" id="1_lod_btn" OnClick="setDetail(1)">Overview</button>
			<button class="btn level_of_detail_btn" id="2_lod_btn" OnClick="setDetail(2)">Normal</button>
			<button class="btn level_of_detail_btn" id="3_lod_btn" OnClick="setDetail(3)">Detailed</button>
		</div>
	
	</div>
	<div class="col-xs-6">

	<div class="row" id="play_button">
		<input id="play_image" type="image" height="75" width="75" src="images/play_button.png" name="saveForm" OnClick="testToggle()"/>
	</div>
	
	<div class="row" id="continue_1">
        <button type="button" class="btn btn-success" OnClick="testToggle()">Continue</button>
    </div>
    
    <div class="row" id="loader">
	    <img src="images/ajax_loader_gray_256.gif" style="width:125px;height:125px">
    </div>
    
	</div>
	</div>
    
    <div class="container-fluid">
	
	
	<div class="row" id="time_feedback">
	
		<div class="btn-toolbar" id="time_feedback_toolbar">
		  
<!-- 		  Dummy placeholder button to maintain space on page until ready, this is hidden -->
			<button class="btn">0</button>

		</div>
	
	</div>
	
    </div>
    
    
<!--     Test code to show/hide the time feedback row of buttons, TODO remove -->

<hr>
<h6>Test code below here</h6>
    
<!--     <button id="hideTimeButton" type="button" OnClick="hideTimeButtons()">Hide/Show</button> -->
    <br>
    <button type="button" OnClick="createButtons(399)">Create 399 s</button>
    <button type="button" OnClick="createButtons(401)">Create 401 s</button>
    <button type="button" OnClick="createButtons(699)">Create 699 s</button>
    <button type="button" OnClick="createButtons(701)">Create 701 s</button>
    <button type="button" OnClick="createButtons(1000)">Create 1000 s</button>
    <button type="button" OnClick="createButtons(3000)">Create 3000 s</button>
	<button type="button" OnClick="createButtons(10000)">Create 10K s</button>
	<br><br>
	<button type="button" OnClick="toggleLoader()">Toggle Loader</button>
	<button type="button" OnClick="toggleSubmitStage()">Toggle Sub Stg</button>
	<button type="button" OnClick="setErrorMsg()">Test Msg</button>
	
	<button id="button_submit" type="button">doGet()</button>









</body>
</html>