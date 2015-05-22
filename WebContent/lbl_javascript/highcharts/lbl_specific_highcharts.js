/**
 * New node file
 */

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
        	dataLabels:{overflow:'none', crop:true, enabled:true},
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