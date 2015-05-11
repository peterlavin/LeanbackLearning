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




















   <div class="container-fluid" id="heading_topic">
       
      <div class="col-xs-4" id="lbl_logo">
       	<input id="logo_image" type="image" src="images/LeanBackLearning_v2CROP.png" alt="Leanback learning" OnClick="reload()"/>
      </div>
      
      <div class="col-xs-4" id="welcome_msg">
      	  <div id="usermsg"></div>
      </div>
      
      <div class="col-xs-4" id="sign_out_btn" >
      	<input id="disconnect" type="image" src="images/google_signout.png" name="saveForm"/>
      </div>
    
      <form class="form-horizontal" role="form" id="topic_input">
        <div class="form-group">
          <div class="col-sm-10">
            <input name="test" type="text" class="form-control" id="topics" placeholder="What do you want to learn about?" oninput="validateInput()">
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

	<div id="continue_loader_play">
	
		<div class="row" id="continue">
			<input class="button_image" id="continue_image" type="image" src="images/continue.png" height="50" width="50"/>
<!--         	<button type="button" class="btn btn-success" id="button_continue">Continue <span class="glyphicon glyphicon-arrow-right"></span></button> -->
    	</div>

    	<div class="row" id="loader">
	    	<img class="button_image" id="loader_image" src="images/ajax_loader_gray_256.gif">
    	</div>
    
		<div class="row" id="play_button">
			<input class="button_image" id="play_image" type="image" height="50" width="50" src="images/play_button.png" name="saveForm" OnClick="localPlay()"/>
		</div>
	
		<div class="row" id="pause_button">
		    <input class="button_image" id="pause_image" type="image" src="images/pause_button.png" height="50" width="50" OnClick="localPause()"/>
		</div>
		
	</div>
	
	<div id="startover_pause_stop">
	
		<div class="row" id="stop_button">
		    <input class="button_image" id="stop_image" type="image" src="images/stop_button.png" height="40" width="40" OnClick="localStopAndReset()"/>
		</div>

	    <div class="row" id="startover">
	    	<input class="button_image" id="startover_image" type="image" src="images/startover.png" height="40" width="40" OnClick="reload()"/>
<!-- 	        <button type="button" class="btn btn-success" id="button_startover" OnClick="reload()" >Start Over <span class="glyphicon glyphicon-refresh"></span></button> -->
	    </div>
		
		
			
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
    
    
    
    
		
		
    
    
    
    
    
    <!-- 	HTML required for the player (only the list is shown here) -->
    <div id="container">
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

<div id="testbuttons">

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
		
</div>



























<!-- 	end of Lbl html in authOps div ---------------------------------------------------------- -->

<hr>

<!-- 		Not needed (for now) -->
<!--     <h2>User's profile information</h2> -->
    <div id="profile"></div>
    

<!-- 		Not needed (for now) -->
<!--     <h2>User's friends that are visible to this app</h2> -->
    <div id="visiblePeople"></div>

<!-- 		Not needed (for now) -->
<!--     <h2>Authentication Logs</h2> -->
    <pre id="authResult"></pre>
    
<!--   End of authOps div, shown/hidden by successful login/logout -->
  </div> 



  
  
  <!-- 	This div shows the red rectangular sign-in button -->
  <div class="centered_button" id="gConnect">
    
<!--     Added by PL to allow an image to be used for login btn -->
   <div class="centered_button" id="logo_signin">
 	 <input id="signinButton" type="image" src="images/LeanBackLearning_v2CROP.png" height="200" width="200" name="saveForm" OnClick="gapi.auth.signIn()"/>
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