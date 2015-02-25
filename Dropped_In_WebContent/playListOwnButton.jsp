<!DOCTYPE html>
<html>
<head>
<meta charset=utf-8 />

<title>JPlayListOwnBtn</title>

<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />

<!--  The following five lines are related to JPlayer Playlists only -->
<link href="playlist/css/jPlayer.css" rel="stylesheet" type="text/css" />
<link href="playlist/skin/pink.flag/jplayer.pink.flag.css" rel="stylesheet" type="text/css" />

<!-- <script type="text/javascript" src="playlist/code.jquery.com/jquery-1.7.2.min.js"></script> -->
<script type="text/javascript" src="playlist/code.jquery.com/jquery-2.0.0.js"></script>

<script type="text/javascript" src="playlist/js/jquery.jplayer.min.js"></script>
<script type="text/javascript" src="playlist/js/jplayer.playlist.min.js"></script>




<script type="text/javascript">

$(document).ready(function(){
	

	// This playlist is replaced by the one returned by the Java Servlet
	// TODO, in next ver, this will be nested in a JSON which also has the total playtime (seconds) for the entire playlist
 	var playlist = [{"title":"Part 1 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_1.mp3"},{"title":"Part 2 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_2.mp3"},{"title":"Part 3 of 3","mp3":"http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_3.mp3"}];
	
	new jPlayerPlaylist({
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
		keyEnabled: true
		
	});

});

</script>

</head>
<body class="demo" onload="setUpButtons()">

<!-- 	Show and hide buttons (test only), and play and pause buttons -->
  <div>
    <br><br>
    <button id="hideButton" type="button" OnClick="togglePlayer()">Toggle JP View</button>
    <input id="playButton" type="image" src="images/play_button.png" height="75" width="75" OnClick="localPlay()"/>
    <input id="pauseButton" type="image" src="images/pause_button.png" height="75" width="75" OnClick="localPause()"/>
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

<script type="text/javascript">

/*
 * Sets up buttons as required at start
 */
function setUpButtons(){
	$('#playButton').show();
	$('#pauseButton').hide();
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
	$('#playButton').hide();
	$('#pauseButton').show();
	
}

function localPause() {
	
	$("#jquery_jplayer_1").jPlayer("pause");
	$('#playButton').show();
	$('#pauseButton').hide();
}

</script>


</body>

<script>
! function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0],
        p = /^http:/.test(d.location) ? 'http' : 'https';
    if (!d.getElementById(id)) {
        js = d.createElement(s);
        js.id = id;
        js.src = p + '://platform.twitter.com/widgets.js';
        fjs.parentNode.insertBefore(js, fjs);
    }
}(document, 'script', 'twitter-wjs');

</script>

<script type="text/javascript" src="playlist/js/prettify/prettify-jPlayer.js"></script>

</html>
