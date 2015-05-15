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


// 0: Cork (city), 96
// 1: History, 249
// 2: Geography, 18
// 3: Climate, 126
// 4: Culture, 253
// 5: Policy, 25
// 6: Media - Broadcasting, 90
// 7: Places of interest, 203
// 8: Local government and politics, 112
// 9: Economy - Retail, 71
// 10: Transport - Air, 22
// 11: Education, 72
// 12: Sport, 13
// 13: Demographics, 96




$(function () {
    $('#containerXX').highcharts({
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
        }
    });
});

</script>



</head>
<body>

<div id="containerXX"></div>

</body>
</html>