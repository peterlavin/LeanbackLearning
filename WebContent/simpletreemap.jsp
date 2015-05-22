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
            data: [{
                name: 'Introduction',
                value: 296
            }, {
                name: 'History',
                value: 249
            }, {
                name: 'Geography',
                value: 18
            }, {
                name: 'Climate',
                value: 126
            }, {
                name: 'Culture',
                value: 253
            }, {
                name: 'Media - Broadcasting',
                value: 90
            }, {
                name: 'Places of interest',
                value: 203
            }
            , {
                name: 'Local gov, politics',
                value: 112
            }, {
                name: 'Economy - Retail',
                value: 71
            }, {
                name: 'Transport - Air',
                value: 22
            }, {
                name: 'Education',
                value: 72
            }, {
                name: 'Sport',
                value: 13
            }, {
                name: 'Demographics',
                value: 96
            }]
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
                    	
                        alert('Category: ' + this.name);
                        
                    }
                }
            }
        }
    },
    });
});

</script>



</head>
<body>



<div id="visualContainer"></div>



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