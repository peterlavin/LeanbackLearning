<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Simple Tree Maps</title>

<script src="jplayer_javascript/jquery-2.0.0.js"></script>

<!-- These need to be imported after jquery -->
<script src="http://code.highcharts.com/highcharts.js"></script>
<script src="http://code.highcharts.com/modules/heatmap.js"></script>
<script src="http://code.highcharts.com/modules/treemap.js"></script>


<link rel="stylesheet" href="css/treemap_specific.css">

<script>

$(function () {
    $('#container').highcharts({
        series: [{
            type: "treemap",
            layoutAlgorithm: 'squarified',
            data: [{
                name: 'A',
                value: 6
            }, {
                name: 'B',
                value: 6
            }, {
                name: 'C',
                value: 4
            }, {
                name: 'D',
                value: 3
            }, {
                name: 'E',
                value: 2
            }, {
                name: 'F',
                value: 2
            }, {
                name: 'G',
                value: 1
            }]
        }],
        title: {
            text: 'Highcharts Treemap'
        }
    });
});



</script>



</head>
<body>

<div id="container"></div>

</body>
</html>