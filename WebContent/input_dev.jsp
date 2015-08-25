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
		
		
<!-- 	Bootstrap css  -->
		<meta name="viewport" content="width=device-width, initial-scale=1">
    	<link rel="stylesheet" href="bootstrap/3.2.0/css/bootstrap.min.css">

<!--    Font Awesome icons -->    	
    	<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">

<!--    Open Sans Font -->    	
    	<link href='http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800' rel='stylesheet' type='text/css'>
    	
<!--     	Places an icon in the browser tab -->
    	<link rel="shortcut icon" href="images/favicon.ico" />
    	
    	<!-- 		Own css file for local settings -->
  		<link rel="stylesheet" href="css/lbl_specific.css">
  		
  		  		
	  	<!--  The following four lines are related to JPlayer Playlists only -->
		<link href="playlist/css/jPlayer.css" rel="stylesheet" type="text/css" />
		<link href="playlist/skin/pink.flag/jplayer.pink.flag.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="playlist/js/jquery.jplayer.min.js"></script>
		<script type="text/javascript" src="playlist/js/jplayer.playlist.min.js"></script>
		
		
<!-- 		Added here to initialize idnum and name, not needed otherwise -->
		<script type="text/javascript" src="lbl_javascript/lbl_google_specific.js"></script>
		
		
		
		<!-- CSS file specific to Treemap visualisations -->
		<link rel="stylesheet" href="css/treemap_specific.css">

		<!-- Javascript file specific to Treemap visualisations -->
		<script src="lbl_javascript/highcharts/highcharts.js"></script>
		<script src="lbl_javascript/highcharts/modules/heatmap.js"></script>
		<script src="lbl_javascript/highcharts/modules/treemap.js"></script>
		
		
<!-- 		<script src="lbl_javascript/highcharts/lbl_specific_highcharts.js"></script> -->


</head>



<body>

<!--  Top bar with Sign Out button -->










<div class="row">
      <div class="text-center col-sm-4 col-sm-offset-4" id="welcome_msg">
      	  <div id="usermsg"><h5>Welcome Mr A User</h5></div>
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







</body>
</html>