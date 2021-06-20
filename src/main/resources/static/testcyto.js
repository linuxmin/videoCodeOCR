var lineGenerator = d3.line()
    .curve(d3.curveLinear);


var xScale = d3.scaleLinear().range([0, 800]);
var yScale = d3.scaleLinear().range([400, 0]);
var length;
var path;
var total_duration;

var csvFile;
var line;
function doTheMagic(d) {
     line = d3.line()
        .x(function (d) {
            return xScale(d.norm_pos_x);
        })
        //        .x(function(d) { return d.norm_pos_x })
        .y(function (d) {
            return yScale(d.norm_pos_y);
        });

    var svg = d3.select('svg');

    total_duration = (d3.max(d, d => d.world_timestamp) - d3.min(d, d => d.world_timestamp)) * 500;



    path = svg.append("path")
        .datum(d.filter(function (d) {
                return d.on_surf === "True"
            })
        )
        .attr("id", "path")
        .attr("fill", "none")
        .attr("stroke-width", 1.5)
        .attr("d", line);

    var totalLength = path.node().getTotalLength();
    console.log(totalLength);
    d3.select('svg')
        .selectAll('circle')
        .data(d.filter(function (d) {
            return d.on_surf === "True"
        }))
        .enter()
        .append('circle')
        .attr("class", "point")
        .attr('cx', function (d) {
            return xScale(d.norm_pos_x);
        })
        .attr('cy', function (d) {
            return yScale(d.norm_pos_y);
        })
        .attr('r', 3)
        //  .attr("fill", "none")
        .attr("fill", "#aaa").style("opacity", 0);

    path
        .attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(6000)
        .ease(d3.easeLinear)
        .tween("line", function() {
            var interp = d3.interpolateNumber(totalLength, 0);
            var self = d3.select(this);
            return function(t) {
                var offset = interp(t);
                self.attr("stroke-dashoffset", offset);

                var xPos = path.node().getPointAtLength(totalLength - offset).x;
                svg.selectAll(".point").each(function(){
                    var point = d3.select(this);
                    var number = +point.attr('cx');

                    if(xPos === number){
                        console.log("xPos")
                        console.log(xPos)
                        console.log("pointcx")
                        console.log(number)
                        console.log(" ");

                    }

                    if (xPos > number){
                        point.style('opacity',1);
                    }
                })
            };
        });

    length = path.node().getTotalLength();


    svg.append("linearGradient").attr("id", "line-gradient").attr("gradientUnits", "userSpaceOnUse").attr("x1", 0).attr("y1", yScaleFix(0)).attr("x2", 1).attr("y2", yScaleFix(1)).selectAll("stop").data([{
        offset: "0%",
        color: "yellow"
    }, {offset: "20%", color: "red"}, {offset: "40%", color: "purple"}, {offset: "62%", color: "black"}, {
        offset: "62%",
        color: "black"
    }, {offset: "100%", color: "yellow"}]).enter().append("stop").attr("offset", function (d) {
        return d.offset;
    }).attr("stop-color", function (d) {
        return d.color;
    });
}

function visualizeInput(videoName) {
    var s = "/extracted-dir/" + videoName + "/vizData/fixations_on_surface_Code.csv";

    d3.csv(s, function (d) {
        doTheMagic(d);
    });
}


// This function will animate the path over and over again
function animateLine() {
    var totalLength = path.node().getTotalLength();
    path
        .attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(20000)
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .tween("line", function() {
            var interp = d3.interpolateNumber(totalLength, 0);
            var self = d3.select(this);
            return function(t) {
                var offset = interp(t);
                self.attr("stroke-dashoffset", offset);

                var xPos = path.node().getPointAtLength(totalLength - offset).x;
                d3.select('svg')
                    .selectAll('.point').each(function(){
                    var point = d3.select(this);
                    var number = +point.attr('cx');
                    if (xPos > number){
                        point.style('opacity',1);
                        point.style("fill","red");
                    }
                })
            };
        });

  /*  // Animate the path by setting the initial offset and dasharray and then transition the offset to 0
    path.attr("stroke-dasharray", length + " " + length)
        .attr("stroke-dashoffset", length)
        .transition()
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .duration(total_duration/3)
        .on("end", () => setTimeout(repeat, 10000)); // this will repeat the animation after waiting 1 second
*/

};


var xScaleFix = d3.scaleLinear().range([0, 800]);
var yScaleFix = d3.scaleLinear().range([400, 0]);
var lengthFix;
var pathFix;

d3.csv("code_gaze.csv", function (d) {
    var lineFix = d3.line()
        .x(function (d) {
            return xScaleFix(d.norm_pos_x);
        })
        //        .x(function(d) { return d.norm_pos_x })
        .y(function (d) {
            return yScaleFix(d.norm_pos_y);
        });

    var lineData = d.map(function (point, index, arr) {
        var next = arr[index + 1],
            prev = arr[index - 1];
        return {
            x: point.norm_pos_x,
            y: point.norm_pos_y,
            x1: point.norm_pos_x,
            y1: point.norm_pos_y,
            x2: (next) ? next.norm_pos_x : prev.norm_pos_x,
            y2: (next) ? next.norm_pos_y : prev.norm_pos_y
        };
    });


    console.log(lineData);
    var svgFix = d3.select("#svgFix");

    pathFix = svgFix.append("path")
        .datum(d)
        .attr("id", "pathFix")
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-width", 1.5)
        .attr("d", lineFix).style("stroke", function (d) {
            if (Math.abs((d[0].norm_pos_x - d[1].norm_pos_x)) > 0.5) {
                return "blue";
            }
            return "red";
        });

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
        .attr("fill", "none").style("opacity", 0);;


    lengthFix = pathFix.node().getTotalLength();
    /*
        svgFix.append("linearGradient").attr("id", "line-gradient-fix").attr("gradientUnits", "userSpaceOnUse").attr("x1", xScaleFix(0)).attr("y1", yScaleFix(0)).attr("x2", xScaleFix(0)).attr("y2", yScaleFix(1)).selectAll("stop").data([{
            offset: "0%",
            color: "lawngreen"
        }, {offset: "10%", color: "red"}, {offset: "20%", color: "purple"}, {offset: "62%", color: "black"}, {
            offset: "62%",
            color: "black"
        }, {offset: "100%", color: "red"}]).enter().append("stop").attr("offset", function (d) {
            return d.offset;
        }).attr("stop-color", function (d) {
            return d.color;
        });*/

});

var xScaleWord = d3.scaleLinear().domain([0, 20]).range([0, 1]);
var yScaleWord = d3.scaleLinear().domain([0, 800]).range([0, 1]);


// This function will animate the path over and over again
function animateLineFixation() {
    updateData();
    // Animate the path by setting the initial offset and dasharray and then transition the offset to 0
    pathFix.attr("stroke-dasharray", lengthFix + " " + lengthFix)
        .attr("stroke-dashoffset", lengthFix)
        .transition()
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .duration(total_duration)
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

//Get path start point for placing marker
function pathStartPoint(path) {
    var d = path.attr("d"),
        dsplitted = d.split(" ");
    return dsplitted[1];
}

function transition(path) {
    path.transition()
        .duration(7500)
        .attrTween("stroke-dasharray", tweenDash)
        .each("end", function () {
            d3.select(this).call(transition);
        });// infinite loop
}

function tweenDash() {
    var l = path.node().getTotalLength();
    var i = d3.interpolateString("0," + l, l + "," + l); // interpolation of stroke-dasharray style attr
    return function (t) {
        var marker = d3.select("#marker");
        var p = path.node().getPointAtLength(t * l);
        marker.attr("transform", "translate(" + p.x + "," + p.y + ")");//move marker
        return i(t);
    }
}