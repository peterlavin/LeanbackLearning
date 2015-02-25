<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport"
			content="width=device-width, initial-scale=1.0, user-scalable=no" />
		
		<title>Leanback Learning - Test Page</title>
		
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		
		<!-- Bootstrap core CSS -->
		<link href="css/bootstrap.css" rel="stylesheet">
		
		<link href="playlist/skin/pink.flag/jplayer.pink.flag.css" rel="stylesheet" type="text/css" />
		
		<!-- jPlayer CSS, modified -->
		<link rel="stylesheet" href="css/lbldemo.css">
		
		<!-- Own CSS, modified -->
		<link href="playlist/css/jPlayer.css" rel="stylesheet" type="text/css" />
		
		<!-- Not modified -->
		<link href="playlist/js/prettify/prettify-jPlayer.css" rel="stylesheet" type="text/css" />
		
		<!-- Not modified -->
		<link rel="stylesheet" href="css/normalize.css">
		
		<!-- Not modified, need for differences in browsers -->
		<link rel="shortcut icon" href="playlist/graphics/jplayer.ico" type="image/x-icon" />
		
		<script type="text/javascript" src="playlist/code.jquery.com/jquery-1.7.2.min.js"></script>
		
		<script type="text/javascript" src="playlist/js/jquery.jplayer.min.js"></script>
		
		<script type="text/javascript" src="playlist/js/jplayer.playlist.min.js"></script>
		
		<script type="text/javascript" src="playlist/js/jquery.jplayer.inspector.js"></script>
		
		<script type="text/javascript" src="playlist/js/themeswitcher.js"></script>
	
		<script src="bootstrap_js/bootstrap.min.js"></script>
		
		<!--  Own script containing all customised Javascript for this page, needs to be imported after jquery -->
		<script type="text/javascript" src="lbl_js/indexplaylist.js"></script>
	
	
	</head>

<body onresize="changePadding()">
	
	<div class="container-fluid">
		<!-- first row, contains the topics text entry box -->
		<div class="btn-toolbar row form_row form_row_resize" id="name_row">
			<div class="col-xs-12" id="name_col">
				<div class="input-group">
					<input type="text" class="form-control" id="name"
						value="Your name? (optional)"
						onfocus="if(this.value == 'Your name? (optional)') { this.value = ''; } "
						onblur="if(this.value == '') { this.value = 'Your name? (optional)'; } " />
					<span class="input-group-addon glyphicon glyphicon-user"></span>
				</div>
			</div>
		</div>
		<!-- 2nd row, contains the time details slider -->
		<div class="btn-toolbar row form_row form_row_resize" id="topics_row">
			<div class="col-xs-12" id="topics_col">
				<div class="input-group">
					<input type="text" class="form-control" id="topics"
						value="Presentation topic?"
						onfocus="if(this.value == 'Presentation topic?') { this.value = ''; } "
						onblur="if(this.value.trim() == '') { this.value = 'Presentation topic?'; } " />
					<span class="input-group-addon glyphicon glyphicon-pencil"></span>
				</div>
			</div>
		</div>
		<div class="btn-toolbar row form_row form_row_resize"
			id="select_lod_row">
			<!-- btn-group-sm/lg here changes the button size -->
			<div
				class="btn-group btn-group-lg btn-group-justified btn-group-fill-height">
				<a class="btn btn-primary dropdown-toggle" id="button_lod_sel"
					data-toggle="dropdown" href="#">Select Level of Detail Required
					<span class="caret"></span>
				</a>
				<ul class="dropdown-menu" id="lod_dropdown">
					<li><a href="#">High level overview</a></li>
					<li><a href="#">Detailed introduction</a></li>
					<li><a href="#">All detail</a></li>
				</ul>
			</div>
		</div>
		<!-- Row for time/summarisation options -->
		<div class="btn-toolbar row form_row" id="time_feedback_labels_row"
			style='text-align: center'>
			<div class="col-xs-4 form_row_resize" id="time_feedback_min"
				style='text-align: left; vertical-align: text-bottom'>
				<label></label>
			</div>
			<div class="col-xs-4 form_row_resize" id="time_feedback_caption"
				style='text-align: center; vertical-align: text-top;'>
				<label>Select a prefered time</label>
			</div>
			<div class="col-xs-4 form_row_resize" id="time_feedback_max"
				style='text-align: right; vertical-align: text-bottom'>
				<label></label>
			</div>
		</div>
		<!-- Row which contains the slider -->
		<div class="btn-toolbar row form_row">
			<div class="col-xs-12 form_row_resize" id="time_feedback_row">
				<input name="time_preference_slider" id="time_preference_slider"
					onchange="updateTimeSlider()" type="range" min="5" max="80"
					value="42" step="1" />
			</div>
		</div>
		<!-- Row to be shown if there are no results -->
		<div class="btn-toolbar row form_row" id="error_feedback_row">
			<div class="col-xs-12 form_row_resize" id="error_feedback"
				style='text-align: center'>
				<label>Empty now</label>
			</div>
		</div>
		<div class="btn-toolbar row form_row form_row_resize"
			id="output_lang_row">
			<!-- btn-group-sm/lg here changes the button size -->
			<div
				class="btn-group btn-group-lg btn-group-justified btn-group-fill-height">
				<a class="btn btn-primary dropdown-toggle" id="button_lang_sel"
					data-toggle="dropdown" href="#">Select output language <span
					class="caret"></span></a>
				<ul class="dropdown-menu" id="language_dropdown">
					<li><a href="#">English</a></li>
					<li><a href="#">French</a></li>
					<li><a href="#">German</a></li>
				</ul>
			</div>
		</div>
		<!-- Row for wait/loading image for word-count (short) operation -->
		<!-- Loader image, displayed while word-count is being processed -->
		<div class="btn-toolbar row form_row_resize" id="wordcount_loader_img">
			<div>
				<img src="images/ajax_loader_gray_256.gif" height="40px">
			</div>
		</div>
		<!-- Jplayer, not visible until made visible after request has been sent -->
		<div class="btn-toolbar row form_row" id="media_player_row">
			<!-- start of html for jplayer -->
			<!-- <div id="jquery_jplayer_1" class="jp-jplayer"></div> -->
			<!-- Placeholder image to mimic the mediaplayer until it is made visible -->
			<!-- this becomes invisible when the request is sent -->
			<div class="btn-toolbar row" id="mediaplayer_blank_img">
				<div>
					<img src="images/mediaplayer_blank_logo_no-buttons.png">
				</div>
			</div>
			<!-- Loader image, displayed while job is being processed -->
			<div class="btn-toolbar row" id="mediaplayer_loader_img">
				<div>
					<img src="images/ajax_loader_gray_256.gif" height="156px">
				</div>
			</div>
			
			
			
			
			<!-- start of paste in for playlist -->
			
			<div id="jquery_jplayer_1" class="jp-jplayer"></div>
						
			<div id="jp_container_1" class="jp-audio">
				<div class="jp-type-playlist">
					<div class="jp-gui jp-interface">
						<ul class="jp-controls">
							<li><a href="javascript:;" class="jp-previous" tabindex="1">previous</a></li>
							<li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
							<li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
							<li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
							<li><a href="javascript:;" class="jp-next" tabindex="1">next</a></li>
							<li><a href="javascript:;" class="jp-mute" tabindex="1"
								title="mute">mute</a></li>
							<li><a href="javascript:;" class="jp-unmute" tabindex="1"
								title="unmute">unmute</a></li>
							<li><a href="javascript:;" class="jp-volume-max"
								tabindex="1" title="max volume">max volume</a></li>
						</ul>
						<!-- Hides the progress and seek bar -->
						<div class="jp-progress">
							<div class="jp-seek-bar">
								<div class="jp-play-bar"></div>
							</div>
						</div>
						<div class="jp-volume-bar">
							<div class="jp-volume-bar-value"></div>
						</div>
						<div class="jp-current-time"></div>
						<div class="jp-duration"></div>
					</div>
					<!-- end of paste in for playlist -->
					<div class="jp-playlist" id="playlist-part">
						<ul>
							<li></li>
						</ul>
					</div>
					<div class="jp-no-solution">
						<span>Update Required</span> To play the media you will need to
						either update your browser to a recent version or update your <a
							href="http://get.adobe.com/flashplayer/" target="_blank">Flash
							plugin</a>.
					</div>
				</div>
			</div>
			
			
			
			
			
			
			
			
			
			<div id="jplayer_inspector_1"></div>
		</div>
		<!-- End of Jplayer div -->
		
		
		
		
		
		<!-- Two cols in this row, contains two buttons, Submit and Clear -->
		<div class="btn-toolbar row form_row form_row_resize last_row_resize"
			id="buttons_row">
			<div
				class="btn-group btn-group-lg btn-group-justified btn-group-fill-height">
				<div class="btn-group btn-group-justified">
					<div class="btn-group">
						<button type="button" class="btn btn-default"
							id="button_startover" onclick="reload()">Start over</button>
					</div>
					<div class="btn-group">
						<button type="button" class="btn btn-default" id="button_submit">
							Submit <span class="glyphicon glyphicon-search"></span>
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- End of container div -->
	
</body>
</html>