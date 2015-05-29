<!--
/*
 *
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Taken from ... (Jan 2015)
 * https://github.com/googleplus/gplus-quickstart-javascript/blob/master/index.html
 *
 */
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
  <title>LeanbackLearning</title>
  
  
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
  		
  			  	<!--  The following four lines are related to JPlayer Playlists only -->
		<link href="playlist/css/jPlayer.css" rel="stylesheet" type="text/css" />
		<link href="playlist/skin/pink.flag/jplayer.pink.flag.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="playlist/js/jquery.jplayer.min.js"></script>
		<script type="text/javascript" src="playlist/js/jplayer.playlist.min.js"></script>
  		
		<!-- 	Page level configuration, added for button work -->
		<meta name="google-signin-clientid" content="1014444466376-781shj0dnkd1igkfv39scjto8bs1kkdk.apps.googleusercontent.com"/>
		<meta name="google-signin-scope" content="https://www.googleapis.com/auth/plus.login" />
		<meta name="google-signin-requestvisibleactions" content="http://schema.org/AddAction" />
		<meta name="google-signin-cookiepolicy" content="single_host_origin" />
		
		
		<!-- CSS file specific to Treemap visualisations -->
		<link rel="stylesheet" href="css/treemap_specific.css">

		<!-- Javascript file specific to Treemap visualisations -->
		<script src="lbl_javascript/highcharts/highcharts.js"></script>
		<script src="lbl_javascript/highcharts/modules/heatmap.js"></script>
		<script src="lbl_javascript/highcharts/modules/treemap.js"></script>
		
		
		<!--    Font Awesome icons AC -->    	
    	<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">

<!--    Open Sans Font AC-->    	
    	<link href='http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800' rel='stylesheet' type='text/css'>
		
		


<script type="text/javascript" src="lbl_javascript/lbl_google_specific.js"></script>

<!--  IMPORTANT This javascript must be left in this page to avoid popup blocking by browser -->
<script src="https://apis.google.com/js/client:platform.js?onload=render" async defer>

 /* Executed when the APIs finish loading */
 
 function render() {

   // Additional params including the callback, the rest of the params will
   // come from the page-level configuration.
   var additionalParams = {
     'callback': signinCallback
   };

   // Attach a click listener to a button to trigger the flow.
   var signinButton = document.getElementById('signinButton');
   signinButton.addEventListener('click', function() {
     gapi.auth.signIn(additionalParams); // Will use page level configuration
   });
 }
 </script>

</head>



<body>
  
<!--   Div which is hidden/shown when signin is successful -->
  <div id="authOps" style="display:none">
  
  
  
  
  
<!--   Put all lbl stuff in here -->

<div class="row">
      <div class="text-center col-sm-4 col-sm-offset-4" id="welcome_msg">
      	  <div id="usermsg"></div>
      </div>
      <div class="text-right" id="sign_out_btn" >
      	<input id="disconnect" type="image" src="images/google_signout.png" name="saveForm"/>
      </div>
</div>

<!--  Main Widget -->
<div id="all_body" class="container text-center">

   <div id="heading_topic" class="row">
      <div class="col-sm-4 col-sm-offset-4 text-center" id="lbl_logo">
       	<input id="logo_image" class="logo_image" type="image" src="images/lbllogo.png" alt="Leanback learning" OnClick="reload()"/>
      </div>
   </div>
    
   <div class="row">
     <div class="col-sm-8 col-sm-offset-2 text-center">
       <form class="form-horizontal" role="form" id="topic_input">
         <div class="form-group">          
             <input name="test" type="text" class="form-control" id="topics" placeholder="What do you want to learn about?" oninput="validateInput()">
         </div>
       </form>
     </div>    
   </div>
   
   <div class="row">
   	  <div class="col-sm-8 col-sm-offset-2 text-center" id="error_msg" style="display: none;">
 	    <!-- Area to display errors -->
 	  </div>
   </div>
   
   	<div class="row">
   	  <div class="col-sm-2" id="previsualContainer" style="display: none;">
 	    <!-- Centering visual container -->
	  </div>
   	  <div class="col-sm-8 text-center" id="visualContainer" style="display: none;">
 	    <!-- Treemap -->
	  </div>
	</div>

   	<div class="row">
   	  <div class="col-sm-6 col-sm-offset-2 text-center" id="playerProgressBar" style="display: none;">
 	    <!-- Player Progress Bar -->
 	  </div>
 	<div id="continue_loader_play" class="col-sm-2 text-center" style="display: none;">
		<div id="play_button">
			<button class="button_image" id="play_image" name="saveForm" OnClick="localPlay()"></button>
		</div>
		<div id="pause_button">
		    <button class="button_image" id="pause_image" OnClick="localPause()"></button>
		</div>
        <div id="stop_button">
		    <button class="button_image" id="stop_image" OnClick="localStopAndReset()"></button>
		</div>
	</div>
   </div>
        
   <div class="row" id="lang_lod_play">
	 <div class="col-sm-6 col-sm-offset-2 text-left" id="output_lang">  
			<button class="btn output_lang_btn" id="fr_btn"OnClick="setLanguage('fr')">French</button>
			<button class="btn output_lang_btn" id="en_btn" OnClick="setLanguage('en')">English</button>
			<button class="btn output_lang_btn" id="de_btn" OnClick="setLanguage('de')">German</button>
	 </div>
   </div>
		
   <div class="row" id="detail_buttons" style="display: none;">
	 <div class="col-sm-6 col-sm-offset-2 text-left" id="level_of_detail">  
		<button class="btn output_lang_btn" id="1_lod_btn" OnClick="setDetail(1)">Overview</button>
		<button class="btn output_lang_btn" id="2_lod_btn" OnClick="setDetail(2)">Normal</button>
		<button class="btn output_lang_btn" id="3_lod_btn" OnClick="setDetail(3)">Detailed</button>
	 </div>
   </div>

   <!-- Time Estimation buttons are automatically generated from lbl_specific.js -->   
   <div class="row" id="time_feedback" style="display: none;">
   </div>
	
	<div class="col-sm-4 col-sm-offset-6 text-right" id="reset">
		<button class="btn-lg btn-success btn-reset" id="startover" OnClick="reload()"/>Reset<i class="fa fa-refresh arrow-icon"></i></button>
		<button class="btn-lg btn-success btn-continue" id="continue_image"/>Continue<i class="fa fa-arrow-right arrow-icon"></i></button>
	</div>
	
	<!-- HTML required for the player (only the list is shown here) -->
    <div id="container" style="display:none;">
	  <div id="content_main">
		<section>
		<div id="jquery_jplayer_1" class="jp-jplayer"></div>
		<div id="jp_container_1" class="jp-audio">
			<div class="jp-type-playlist">
				<div class="jp-playlist" id="playlist-part">
					<ul>
						<li></li>
					</ul>
				</div>
				 <div class="jp-no-solution">
					 <span>Update Required</span>
					 To play the media you will need to either update your browser to a recent version or update your <a href="http://get.adobe.com/flashplayer/" target="_blank">Flash plugin</a>.
				 </div>
			</div>
		</div>
        </section>
	  </div>
    </div>    
    
    
<!--     Test code to show/hide the time feedback row of buttons, TODO remove -->

<div id="testbuttons" style="display:none;">

<hr>
<h6>Test code below here</h6>
    
<!--     <button id="hideTimeButton" type="button" OnClick="hideTimeButtons()">Hide/Show</button> -->
    <br>
    <button type="button" OnClick="createTimeButtons(399)">Create 399 s</button>
    <button type="button" OnClick="createTimeButtons(401)">Create 401 s</button>
    <button type="button" OnClick="createTimeButtons(699)">Create 699 s</button>
    <button type="button" OnClick="createTimeButtons(701)">Create 701 s</button>
    <button type="button" OnClick="createTimeButtons(1000)">Create 1000 s</button>
    <button type="button" OnClick="createTimeButtons(3000)">Create 3000 s</button>
	<button type="button" OnClick="createTimeButtons(10000)">Create 10K s</button>
	<br><br>
	<button type="button" OnClick="toggleLoader()">Toggle Loader</button>
	<button type="button" OnClick="toggleSubmitStage()">Toggle Sub Stg</button>
	<button type="button" OnClick="setErrorMsg('A test Error Message')">Test Msg</button>
	
	<button id="button_continue" type="button">doGet()</button>
	
	<button type="button" OnClick="greyLanguageButtons();greyDetailButtons()">Test Grey</button>
	
	<button type="button" OnClick="restoreDetailButtons()">Restore</button>
	
	<button type="button" OnClick="greyTimeButtons()">Grey Time</button>
	
	<button id="hideButton" type="button" OnClick="togglePlayer()">Toggle JP View</button>
	
	<button id="selectZero" type="button" OnClick="selectZero()">Select 0</button>
	
	<button id="stopPlayer" type="button" OnClick="localStopAndReset()">Stop n Reset</button>
	
	<button id="toggleVisual" type="button" OnClick="toggleVisual()">Toggle Visual</button>
	
		
</div>
	
<!-- 	above here goes to index.jsp -->
	

</div>





<!-- 	end of Lbl html in authOps div ---------------------------------------------------------- -->

<!-- <hr> -->

<!-- 		Not needed (for now) -->
<!--     <h2>User's profile information</h2> -->
<!--     <div id="profile"></div> -->
    

<!-- 		Not needed (for now) -->
<!--     <h2>User's friends that are visible to this app</h2> -->
<!--     <div id="visiblePeople"></div> -->

<!-- 		Not needed (for now) -->
<!--     <h2>Authentication Logs</h2> -->
<!--     <pre id="authResult"></pre> -->
    
    
    
    
    

  </div> <!--   End of authOps div, shown/hidden by successful login/logout -->



  
  
  <!-- 	This div shows the red rectangular sign-in button -->
  <div class="centered_button" id="gConnect">
    
<!--     Added by PL to allow an image to be used for login btn -->
   <div class="centered_button" id="logo_signin">
 	 <input id="signinButton" type="image" src="images/lbllogo.png" height="200" width="200" name="saveForm" OnClick="gapi.auth.signIn()"/>
   </div>

    <button class="g-signin"
        data-scope="https://www.googleapis.com/auth/plus.login"
        data-requestvisibleactions="http://schemas.google.com/AddActivity"
        data-clientId="1014444466376-781shj0dnkd1igkfv39scjto8bs1kkdk.apps.googleusercontent.com"
        data-callback="onSignInCallback"
        data-theme="dark"
        data-cookiepolicy="single_host_origin">

    </button>
  </div>
  
  
<!--   Not usually seen, if things work, this is hidden -->
  <div id="loaderror">
    This section will be hidden by JQuery. If you can see this message, you
    may be viewing the file rather than running a web server.<br />
    The sample must be run from http or https. See instructions at
    <a href="https://developers.google.com/+/quickstart/javascript">
    https://developers.google.com/+/quickstart/javascript</a>.
  </div>


</body>
</html>