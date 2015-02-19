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
  
  <!-- JavaScript specific to this application that is not related to API calls -->	
<!--   <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js" ></script> -->
  
  
  
  
  
<!-- 		Two imports related to bootstrap -->
<!-- 		<script src="jplayer_javascript/jquery-1.11.1.js"></script> -->
		<script src="jplayer_javascript/jquery-2.0.0.js"></script>
        <script src="bootstrap/3.2.0/js/bootstrap.min.js"></script>
        
		<!--  Own script containing all customised Javascript for this page, needs to be imported after jquery -->
		<script type="text/javascript" src="lbl_javascript/lbl_specific.js"></script>      
        
		<!-- 		Bootstrap css  -->
		<meta name="viewport" content="width=device-width, initial-scale=1">
    	<link rel="stylesheet" href="bootstrap/3.2.0/css/bootstrap.min.css">
    	
		<!-- 		Own css file for local settings -->
  		<link rel="stylesheet" href="css/lbl_specific.css">

<!-- 	Page level configuration, added for button work -->
<meta name="google-signin-clientid" content="1014444466376-781shj0dnkd1igkfv39scjto8bs1kkdk.apps.googleusercontent.com"/>
<meta name="google-signin-scope" content="https://www.googleapis.com/auth/plus.login" />
<meta name="google-signin-requestvisibleactions" content="http://schema.org/AddAction" />
<meta name="google-signin-cookiepolicy" content="single_host_origin" />


<script type="text/javascript" src="lbl_javascript/lbl_google_specific.js"></script>

<!--  Own script containing all customised Javascript for this page, needs to be imported after jquery -->
<script type="text/javascript" src="lbl_javascript/lbl_specific.js"></script>

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

<!--   		Needed until document.ready() is included -->
  		<script type="text/javascript">
	  		function setup(){
  				
  				$("#time_feedback").css("visibility", "hidden");
  		        $('#play_button').hide();
  			
  			}
  		</script>

</head>



<body onload="setup()">


  
<!--   Div which is hidden/shown when signin is successful -->
  <div id="authOps" style="display:none">
  
<!--   Put all lbl stuff in here -->




    <div class="container-fluid" id="heading_topic">
       
      <div class="col-xs-4" id="lbl_logo">
      	<input type="image" src="images/logo_placeholder2.png" height="50" width="100"/>
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




<!-- 	end of Lbl stuff in  authOps ---------------------------------------------------------- -->

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
 	 <input id="signinButton" type="image" src="images/blank_logo.png" name="saveForm" OnClick="gapi.auth.signIn()"/>
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