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

            // hint: attrs refers to the HTML tag attributes
            //in D3, any selection[0] contains the group
            //selection[0][0] is the DOM node
            var chart = d3.select(element[0]);
            //to our original directive markup bars-chart
            //we add a div with out chart styling and bind each
            //data entry to the chart
            chart.append("div").attr("class", "chart")
                .selectAll('div')
                .data(data).enter().append("div")
                //.attr("popover", function(item){
                //    return terms[item.termID].name;
                //}).attr("popover-trigger", function(item){
                //    return "mouseenter";
                //})
                .transition().ease("elastic")
                .style("width", function(item) {
                    return item.ratio*100 + "%";
                }).style("background", function(item){
                    return terms[item.termID].color;
                }).text(function(item) {
                    var text = terms[item.termID].name;
                    //return (text.length > 10?(text.substring(0,10)+"..."):text) +
                    //    " " + item.ratio*100 + "% (" + item.nUsers + ")" ;
                    return item.ratio*100 + "%";
                });
        }
    };
}]);