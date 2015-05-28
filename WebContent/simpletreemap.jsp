<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Simple Tree Maps</title>

<script src="jplayer_javascript/jquery-2.0.0.js"></script>

<!-- These need to be imported after jquery -->
<script src="lbl_javascript/highcharts/highcharts.js"></script>
<script src="lbl_javascript/highcharts/modules/heatmap.js"></script>
<script src="lbl_javascript/highcharts/modules/treemap.js"></script>

<link rel="stylesheet" href="css/treemap_specific.css">





<script>

$(function () {
    $('#visualContainer').highcharts({
        series: [{
            type: "treemap",
            layoutAlgorithm: 'squarified',
            data: [
                   {
                     "name": "Cork (city)",
                     "value": 48
                   },
                   {
                     "name": "History",
                     "value": 102
                   },
                   {
                     "name": "Geography",
                     "value": 18
                   },
                   {
                     "name": "Climate",
                     "value": 41
                   },
                   {
                     "name": "Culture",
                     "value": 61
                   },
                   {
                     "name": "Policy",
                     "value": 25
                   },
                   {
                     "name": "Media - Broadcasting",
                     "value": 44
                   },
                   {
                     "name": "Places of interest",
                     "value": 14
                   },
                   {
                     "name": "Local government and politics",
                     "value": 22
                   },
                   {
                     "name": "Economy - Retail",
                     "value": 29
                   },
                   {
                     "name": "Transport - Air",
                     "value": 8
                   },
                   {
                     "name": "Education",
                     "value": 8
                   },
                   {
                     "name": "Sport",
                     "value": 13
                   },
                   {
                     "name": "Demographics",
                     "value": 25
                   }
                 ]
        }],
        title: {
            text: 'Cork City'
        },
    plotOptions: {
        series: {
            cursor: 'pointer',
            point: {
                events: {
                    click: function () {
                    	
                    	addItemToSession(this.name);
                        
                    }
                }
            }
        }
    },
    });
});

</script>

<script>

function addItemToSession(item) {
	
   // alert('Now added: ' + passedName);
    
    
	$.post('TreemapSessionManager', {
		
						add : item
											
					},
					function(responseText) {
						console.log(responseText);
					});
    	
}

function getSession(){
	
		$.post('TreemapSessionManager', {
			
			get : null 
			
		},
		function(responseText) {
			console.log(responseText);
		});
	
}

function clearSession(){
	
	$.post('TreemapSessionManager', {
		
		clear : null
		
	},
	function(responseText) {
		console.log(responseText);
	});

}



</script>



</head>
<body>

<div id="visualContainer"></div>

<button id="getSessionBtm" type="button" OnClick="getSession()">Get Session</button>
<button id="clearSessionBtm" type="button" OnClick="clearSession()">Clear Session</button>



<script>
function allowDrop(ev) {
    ev.preventDefault();
}

// This function specifies what is to be dragged using the setData
function drag(ev) {
    ev.dataTransfer.setData("text", ev.target.id);
}

function drop(ev) {
	// Prevents the default of not allowing an element to be dropped into another one
    ev.preventDefault();
    var data = ev.dataTransfer.getData("text");
    ev.target.appendChild(document.getElementById(data));
}
</script>


<!-- the allowDrop(event) here allows data to be dropped on this div -->
<div id="div1" ondrop="drop(event)" ondragover="allowDrop(event)"></div>

<div id="div2" ondrop="drop(event)" ondragover="allowDrop(event)">

<!-- This image is dragabble -->
<img id="drag1" src="images/google_signout.png" draggable="true" ondragstart="drag(event)" width="135" height="50">

</div>





</body>
</html>