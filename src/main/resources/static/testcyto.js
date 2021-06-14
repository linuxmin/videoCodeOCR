var lineGenerator = d3.line()
    .curve(d3.curveLinear);


var xScale = d3.scaleLinear().range([0, 1600]);
var yScale = d3.scaleLinear().range([880, 0]);
var length;
var path;
var total_duration;

function doTheMagic(d) {
    var line = d3.line()
        .x(function (d) {
            return xScale(d.x_norm);
        })
        //        .x(function(d) { return d.x_norm })
        .y(function (d) {
            return yScale(d.y_norm);
        });

    var svg = d3.select('svg');

    total_duration = (d3.max(d, d => d.world_timestamp) - d3.min(d, d => d.world_timestamp)) * 500;



    path = svg.append("path")
        .datum(d)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-width", 1.5)
        .attr("d", line)


    length = path.node().getTotalLength();

    d3.select('svg')
        .selectAll('circle')
        .data(d)
        .enter()
        .append('circle')
        .attr('cx', function (d) {
            return xScale(d.x_norm);
        })
        .attr('cy', function (d) {
            return yScale(d.y_norm);
        })
        .attr('r', 3)
        .attr("fill", "none")
       // .attr("stroke", "#aaa");
}

d3.csv("code_gaze.csv", function (d) {
    doTheMagic(d);
});

// This function will animate the path over and over again
function animateLine() {
    // Animate the path by setting the initial offset and dasharray and then transition the offset to 0
    path.attr("stroke-dasharray", length + " " + length)
        .attr("stroke-dashoffset", length)
        .transition()
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .duration(total_duration)
        .on("end", () => setTimeout(repeat, 10000)); // this will repeat the animation after waiting 1 second

    // Animate the dashoffset changes
    text.transition()
        .duration(total_duration)
        .tween("text", function (t) {
            const i = d3.interpolateRound(0, length);
            return function (t) {
                this.textContent = "stroke-dashoffset: " + i(t);
            };
        });
};


var xScaleFix = d3.scaleLinear().range([0, 210]);
var yScaleFix = d3.scaleLinear().range([290, 0]);
var lengthFix;
var pathFix;

d3.csv("examples/fixations_cover.csv", function (d) {
    var lineFix = d3.line()
        .x(function (d) {
            return xScaleFix(d.norm_pos_x);
        })
        //        .x(function(d) { return d.x_norm })
        .y(function (d) {
            return yScaleFix(d.norm_pos_y);
        });

    var svgFix = d3.select("#svgFix");

    pathFix = svgFix.append("path")
        .datum(d)
        .attr("id", "pathFix")
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-width", 1.5)
        .attr("d", lineFix)

    d3.select('#svgFix')
        .selectAll('circle')
        .data(d)
        .enter()
        .append('circle')
        .attr('cx', function (d) {
            return xScale(d.norm_pos_x);
        })
        .attr('cy', function (d) {
            return yScale(d.norm_pos_y);
        })
        .attr('r', 3)
        .attr("fill", "none")
        .attr("stroke", "#aaa");


    lengthFix = pathFix.node().getTotalLength();

});

var xScaleWord = d3.scaleLinear().domain([0, 20]).range([0, 1]);
var yScaleWord = d3.scaleLinear().domain([0, 800]).range([0, 1]);

d3.csv("capture.csv", function (d) {
    d3.select("#svgFix")
        .selectAll('circle')
        .data(d)
        .enter()
        .append('circle')
        .attr('cx', function (d) {

            var xScaleWord1 = xScaleWord(d.x);

            return xScaleFix(xScaleWord1);
        })


        .attr('cy', function (d) {

            var yScaleWord1 = yScaleWord(d.y);

            return yScaleFix(yScaleWord1);
        })
        .attr('r', 5)
        .attr("fill", "red");
});

// This function will animate the path over and over again
function animateLineFixation() {
    updateData();
    // Animate the path by setting the initial offset and dasharray and then transition the offset to 0
    pathFix.attr("stroke-dasharray", lengthFix + " " + lengthFix)
        .attr("stroke-dashoffset", lengthFix)
        .transition()
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .duration(6000)
        .on("end", () => setTimeout(repeat, 1000)); // this will repeat the animation after waiting 1 second

    // Animate the dashoffset changes
    text.transition()
        .duration(6000)
        .tween("text", function (t) {
            const i = d3.interpolateRound(0, lengthFix);
            return function (t) {
                this.textContent = "stroke-dashoffset: " + i(t);
            };
        });
}


function updateData() {

    // Get the data again
    d3.csv("capture_old.csv", function (d) {
        var selector = d3.select("#svgFix")
            .selectAll('circle')
            .data(d);

        var entering = selector.enter();
        entering
            .append('circle')
            .attr('cx', function (d) {

                var xScaleWord1 = xScaleWord(d.x);

                return xScaleFix(xScaleWord1);
            })
            .attr('cy', function (d,) {

                var yScaleWord1 = yScaleWord(d.y);

                return yScaleFix(yScaleWord1);
            })
            .attr('r', 5)
            .attr("fill", "red");

        var exiting = selector.exit();
        exiting.remove();

        function getData(d) {
            return getWordsForFrame(d); // d is a chunk
        }
    })
}

function getWordsForFrame(d) {

    for (i = 0; i < d.length; i++) {
        var framenumber = d[i].framenumber;
        if (framenumber.localeCompare("598") === 0)
            return d[i];
    }
}

