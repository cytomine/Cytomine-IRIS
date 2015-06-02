/**
 * Created by phil on 10/05/15.
 */
var iris = angular.module("irisApp");

/**
 * Bar chart (horizontal), displaying value as percentage.
 */
iris.directive('barsChart', ["$compile", function ($compile) {

    return {
        //We restrict its use to an element
        //as usually  <bars-chart> is semantically
        //more understandable
        restrict: 'E',
        //this is important,
        //we don't want to overwrite our directive declaration
        //in the HTML mark-up
        replace: false,

        // each argument in the tag can pass objects from the controller
        // to the directive.
        scope: {
            // this is the chart data as an array
            data: '=chartData',
            //
            terms: '=terms'
        },
        link: function (scope, element, attrs) {

            // get the data array of objects of following structure
            //{
            //    termID: 95202360,
            //    ratio: 0.5,
            //    total: 2,
            //    nUsers: 1
            //}
            var data = scope.data;
            var terms = scope.terms;


            var mouseMoveFn = function(item){
                tooltip.transition()
                    .duration(100)
                    .style("opacity", .95);
                tooltip.html(
                    terms[item.termID].name
                    + ", assigned by " + item.nUsers + " of " + item.maxUsers + " users"
                )
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
                };

            var mouseOutFn = function(item){
                tooltip.transition()
                    .duration(100)
                    .style("opacity", 0);
            };


            var tooltip = d3.select("body").append("div")
                .attr("class", "agreement-tooltip")
                .style("opacity", 0);

            // hint: attrs refers to the HTML tag attributes
            //in D3, any selection[0] contains the group
            //selection[0][0] is the DOM node
            var chart = d3.select(element[0]);
            //to our original directive markup bars-chart
            //we add a div with out chart styling and bind each
            //data entry to the chart
            var enterSelection = chart.append("div") // make a div as parent for all data
                .attr("class", "chart") // add the chart attribute to the parent
                .selectAll('div') // make an empty selection (children)
                .data(data).enter(); // bind all the data to the div (children)

            var chartDivs = enterSelection.append("div"); // for each child append a div

            chartDivs.attr("id", function(item){
                    return item.termID;
                })
                //.on("mousemove", function(item){mouseMoveFn(item)})
                //.on("mouseout", function(item){mouseOutFn(item)})
                .transition().ease("elastic")
                .style("width", function(item) {
                    return item.ratio*100 + "%";
                })
                .style("background", function(item){
                    return terms[item.termID].color;
                });

            // add another span showing the label info
            var spans2 = enterSelection.insert("span");
            spans2
                .on("mousemove", function(item){
                    tooltip.transition()
                        .duration(100)
                        .style("opacity", .95);
                    if (Number(item.nUsers) === 1) {
                        tooltip.html(
                            terms[item.termID].name
                            + ", assigned by " + item.nUsers + " of " + item.maxUsers + " users"
                            + '<p style="margin-top: 10px;"><b>Warning</b>: This label has been assigned by '
                            + '<span class="label label-danger">1</span> user only!</p>'
                        );
                    } else {
                        tooltip.html(
                            terms[item.termID].name
                            + ", assigned by " + item.nUsers + " of " + item.maxUsers + " users"
                        );
                    }

                    tooltip
                        .style("left", (d3.event.pageX) + "px")
                        .style("top", (d3.event.pageY - 28) + "px");
                })
                .on("mouseout", function(item){mouseOutFn(item)})
                .text(function(item) {
                var text = terms[item.termID].name;
                return (text.length > 20?(text.substring(0,20)+"..."):text);
            });

            var spans3 = spans2.append("span");
            spans3.style("float", "right");
            spans3.style("margin-top","2px");
            spans3.html(function(item){
                if (Number(item.nUsers) === 1){
                    return '<span class="glyphicon glyphicon-warning-sign" style="color:orangered;"></span>&nbsp;'
                        + (item.ratio*100).toFixed(0) + "% (" + item.nUsers + ")";
                } else {
                    return (item.ratio*100).toFixed(0) + "% (" + item.nUsers + ")";
                }
            });
        }
    };
}]);


/**
 * Heatmap Chart
 */
iris.directive('heatmapChart', ["$compile", "$parse", function ($compile,$parse) {

    return {
        //We restrict its use to an element
        //as usually  <heatmap-chart> is semantically
        //more understandable
        restrict: 'E',
        //this is important,
        //we don't want to overwrite our directive declaration
        //in the HTML mark-up
        replace: false,

        // each argument in the tag can pass objects from the controller
        // to the directive.
        scope: {
            // full chart data
            data: '=chartData',
            // the list of terms
            terms: '=terms',
            // query user to be compared to all others (will be displayed on x-axis)
            queryUser: '=queryUser'
        },
        link: function (scope, element, attrs) {

            var data = scope.data;
            var terms = scope.terms;
            var queryUser = scope.queryUser;

            var margin = { top: 30, right: 5, bottom: 0, left: 30 },
                w_orig = 960,
                h_orig = 430,
                width = w_orig - margin.left - margin.right,
                height = h_orig - margin.top - margin.bottom,
                gridSize = Math.floor(width / 24),
                //legendElementWidth = gridSize*2,
                buckets = 9, // number of different colors
                colors = ["#ffffd9","#edf8b1","#c7e9b4","#7fcdbb","#41b6c4","#1d91c0","#225ea8","#253494","#081d58"], // alternatively colorbrewer.YlGnBu[9]
                users = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],// TODO change to usernames (or abbrev.)
                termlabels = ["1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a",
                    "10a", "11a", "12a", "1p", "2p", "3p", "4p", "5p", "6p", "7p", "8p", "9p", "10p", "11p", "12p"]; // TODO change to term labels


            d3.tsv("content/data/data.tsv",
                function(d) {
                    return {
                        day: +d.day,
                        hour: +d.hour,
                        value: +d.value
                    };
                },
                function(error, data) {
                    var colorScale = d3.scale.quantile()
                        .domain([0, buckets - 1, d3.max(data, function (d) { return d.value; })])
                        .range(colors);

                    var svg = d3.select(element[0]).append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                        .attr("viewBox", '0 0 ' + (width + margin.left + margin.right) + ' ' + (height + margin.top + margin.bottom))
                        .attr("preserveAspectRatio", "xMinYMin meet")
                        .append("g")
                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                    var dayLabels = svg.selectAll(".dayLabel")
                        .data(users)
                        .enter().append("text")
                        .text(function (d) { return d; })
                        .attr("x", 0)
                        .attr("y", function (d, i) { return i * gridSize; })
                        .style("text-anchor", "end")
                        .attr("transform", "translate(-6," + gridSize / 1.5 + ")")
                        .attr("class", function (d, i) { return ((i >= 0 && i <= 4) ? "dayLabel mono axis" : "dayLabel mono axis"); });

                    var timeLabels = svg.selectAll(".timeLabel")
                        .data(termlabels)
                        .enter().append("text")
                        .text(function(d) { return d; })
                        .attr("x", function(d, i) { return i * gridSize; })
                        .attr("y", 0)
                        .style("text-anchor", "middle")
                        .attr("transform", "translate(" + gridSize / 2 + ", -6)")
                        .attr("class", function(d, i) { return ((i >= 7 && i <= 16) ? "timeLabel mono axis" : "timeLabel mono axis"); });

                    var heatMap = svg.selectAll(".hour")
                        .data(data)
                        .enter().append("rect")
                        .attr("x", function(d) { return (d.hour - 1) * gridSize; })
                        .attr("y", function(d) { return (d.day - 1) * gridSize; })
                        .attr("rx", 4)
                        .attr("ry", 4)
                        .attr("class", "hour bordered")
                        .attr("width", gridSize)
                        .attr("height", gridSize)
                        .style("fill", colors[0]);

                    heatMap.transition().duration(1000)
                        .style("fill", function(d) { return colorScale(d.value); });

                    heatMap.append("title").text(function(d) { return d.value; });

                    //var legend = svg.selectAll(".legend")
                    //    .data([0].concat(colorScale.quantiles()), function(d) { return d; })
                    //    .enter().append("g")
                    //    .attr("class", "legend");
                    //
                    //legend.append("rect")
                    //    .attr("x", function(d, i) { return legendElementWidth * i; })
                    //    .attr("y", height)
                    //    .attr("width", legendElementWidth)
                    //    .attr("height", gridSize / 2)
                    //    .style("fill", function(d, i) { return colors[i]; });
                    //
                    //legend.append("text")
                    //    .attr("class", "mono")
                    //    .text(function(d) { return "≥ " + Math.round(d); })
                    //    .attr("x", function(d, i) { return legendElementWidth * i; })
                    //    .attr("y", height + gridSize);
                });
        }
    };
}]);


