/**
 * Created by phil on 10/05/15.
 */
var iris = angular.module("irisApp");

/**
 * Bar chart (horizontal), displaying value as percentage.
 */
iris.directive('barsChart', ["$parse", function ($parse) {

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
            foo: '=foo'
        },
        link: function (scope, element, attrs) {
            // hint: attrs refers to the HTML tag attributes


            //in D3, any selection[0] contains the group
            //selection[0][0] is the DOM node
            var chart = d3.select(element[0]);
            //to our original directive markup bars-chart
            //we add a div with out chart styling and bind each
            //data entry to the chart
            chart.append("div").attr("class", "chart")
                .selectAll('div')
                .data(scope.data).enter().append("div")
                .transition().ease("elastic")
                .style("width", function(d) { return d + "%"; })
                .text(function(d) { return d + "%"; });

            // TODO display numbers outside
        }
    };
}]);