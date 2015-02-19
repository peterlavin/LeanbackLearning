<!DOCTYPE html>
<html>
<head>

<!-- From... http://jplayer.org/latest/quick-start-guide/api-cssSelector/ -->

  <script type="text/javascript" src="jplayer_javascript/jquery-2.0.0.js"></script>
  <script type="text/javascript" src="jplayer_javascript/jquery.jplayer.js"></script>
  
  <script type="text/javascript">
    $(document).ready(function(){
      $("#jquery_jplayer_1").jPlayer({
        ready: function () {
          $(this).jPlayer("setMedia", {
            title: "Big Buck Bunny",
            m4v: "http://www.jplayer.org/video/m4v/Big_Buck_Bunny_Trailer.m4v",
            ogv: "http://www.jplayer.org/video/ogv/Big_Buck_Bunny_Trailer.ogv",
            poster: "http://www.jplayer.org/video/poster/Big_Buck_Bunny_Trailer_480x270.png"
          });
        },
        swfPath: "/js",
        supplied: "m4v, ogv",
        cssSelectorAncestor: "",
        cssSelector: {
          title: "#title",
          play: "#play",
          pause: "#pause",
          stop: "#stop",
//           mute: "#mute",
//           unmute: "#unmute",
          currentTime: "#currentTime",
         duration: "#duration"
        },
        size: {
          width: "320px",
          height: "180px"
        }
      });
    });
  </script>
  
  <style>
    div.jp-jplayer {
      border:1px solid #009be3;
    }
  </style>
  
</head>

<body onload="setUpButtons()">

<script type="text/javascript">

/*
 * Sets up buttons as required at start
 */
function setUpButtons(){
	$('#showButton').hide();
	$('#hideButton').show();
	$('#playButton').show();
	$('#pauseButton').hide();
}

/*
 * Show and hide the JPlayer <div>
 */
function hidePlayer(){
	$('#jquery_jplayer_1').hide();
	$('#showButton').show();
	$('#hideButton').hide();
}

function showPlayer(){
	$('#jquery_jplayer_1').show();
	$('#showButton').hide();
	$('#hideButton').show();	
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

  <p>
    <button id="play">play</button>
    <button id="pause">pause</button>
    <button id="stop">stop</button>
<!--     <button id="mute">mute</button> -->
<!--     <button id="unmute">unmute</button> -->
    <br><br>
    <button id="hideButton" type="button" OnClick="hidePlayer()">Hide JP</button>
    <button id="showButton" type="button" OnClick="showPlayer()">Show JP</button>
    <br><br>
    <span id="title"></span>
    <br><br>
    <span id="currentTime"></span> - <span id="duration"></span>
  </p>
  <div id="jquery_jplayer_1" class="jp-jplayer"></div>
  <br><br>

  <input id="playButton" type="image" src="images/play_button.png" height="75" width="75" OnClick="localPlay()"/>
  <input id="pauseButton" type="image" src="images/pause_button.png" height="75" width="75" OnClick="localPause()"/>
  
  
  
  
  
  
  

</body>
<html>







