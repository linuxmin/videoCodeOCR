var lineGenerator = d3.line()
    .curve(d3.curveLinear);

var xScale = d3.scaleLinear().range([0, 800]);
var yScale = d3.scaleLinear().range([400, 0]);
var length;
var path;
var total_duration;
var line;

function visualizeInput(videoName) {
    var s = "/extracted-dir/" + videoName + "/vizData/fixations_on_surface_Code.csv";

    d3.csv(s, function (d) {
        doTheMagic(d);
    });
}

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
        .duration(total_duration)
        .ease(d3.easeLinear)
        .tween("line", function () {
            var interp = d3.interpolateNumber(totalLength, 0);
            var self = d3.select(this);
            return function (t) {
                var offset = interp(t);
                self.attr("stroke-dashoffset", offset);

                var xPos = path.node().getPointAtLength(totalLength - offset).x;
                svg.selectAll(".point").each(function () {
                    var point = d3.select(this);
                    var number = +point.attr('cx');

                    if (xPos === number) {
                        console.log("xPos")
                        console.log(xPos)
                        console.log("pointcx")
                        console.log(number)
                        console.log(" ");

                    }

                    if (xPos > number) {
                        point.style('opacity', 1);
                    }
                })
            };
        });

    length = path.node().getTotalLength();


    svg.append("linearGradient").attr("id", "line-gradient").attr("gradientUnits", "userSpaceOnUse").attr("x1", 0).attr("y1", yScale(0)).attr("x2", 1).attr("y2", yScale(1)).selectAll("stop").data([{
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

// This function will animate the path over and over again
function animateLine() {
    var totalLength = path.node().getTotalLength();
    path
        .attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(total_duration)
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .tween("line", function () {
            var interp = d3.interpolateNumber(totalLength, 0);
            var self = d3.select(this);
            return function (t) {
                var offset = interp(t);
                self.attr("stroke-dashoffset", offset);

                var xPos = path.node().getPointAtLength(totalLength - offset).x;
                d3.select('svg')
                    .selectAll('.point').each(function () {
                    var point = d3.select(this);
                    var number = +point.attr('cx');
                    if (xPos > number) {
                        point.style('opacity', 1);
                        point.style("fill", "red");
                    }
                })
            };
        });

};


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