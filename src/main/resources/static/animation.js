var lineGenerator = d3.line()
    .curve(d3.curveLinear);


var xScale = d3.scaleLinear().range([0, 210]);
var yScale = d3.scaleLinear().range([290, 0]);
var length;
var path;
d3.csv("examples/gaze_cover.csv", function (d) {
    var line = d3.line()
        .x(function (d) {
            return xScale(d.x_norm);
        })
        //        .x(function(d) { return d.x_norm })
        .y(function (d) {
            return yScale(d.y_norm);
        });

    var svg = d3.select('svg');

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
        .attr('r', 3);
});

// This function will animate the path over and over again
function animateLine() {
    // Animate the path by setting the initial offset and dasharray and then transition the offset to 0
    path.attr("stroke-dasharray", length + " " + length)
        .attr("stroke-dashoffset", length)
        .transition()
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .duration(6000)
        .on("end", () => setTimeout(repeat, 1000)); // this will repeat the animation after waiting 1 second

    // Animate the dashoffset changes
    text.transition()
        .duration(6000)
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


    lengthFix = pathFix.node().getTotalLength();

    d3.select("#svgFix")
        .selectAll('circle')
        .data(d)
        .enter()
        .append('circle')
        .attr('cx', function (d) {
            return xScaleFix(d.norm_pos_x);
        })
        .attr('cy', function (d) {
            return yScaleFix(d.norm_pos_y);
        })
        .attr('r', 3);
});

// This function will animate the path over and over again
function animateLineFixation() {
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
};

// we use a pie layout to render the circles
// in case we want some spacing, we need to change
// the value in the 'data' array
const width = 400;
const height = 400;
const pieData = d3
    .pie()
    .sort(null)
    .value((d) => {
        return d.value;
    })

const svg = d3.create("svg")
    .attr("width", width)
    .attr("height", height);

const mainGroup = svg
    .append("g")
    .attr("id", "main")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

// Insert lines and circles groups, lines first so they are behind circles
const linesGroup = mainGroup.append("g").attr("id", "lines");
const circlesGroup = mainGroup.append("g").attr("id", "circles");

const circles = [];
for(let item of pieData) {
    const [x, y] = arc.centroid(item);
    circles.push({x, y});
}

// Draw circles using pie data and centroid method to get the center position
circlesGroup
    .selectAll("circle")
    .data(circles, (_, index) => index)
    .join((enter) => {
        enter
            .append("circle")
            .attr("id", (_, index) => {
                return `circle-${index}`;
            })
            .attr("r", 20)
            .attr("cx", (d) => {
                return d.x;
            })
            .attr("cy", (d) => {
                return d.y;
            })
            .style("stroke-width", "2px")
            .style("stroke", "#000")
            .style("fill", "#963cff");
    });

for (let line of lines) {
    const fromCircle = circles[line.from];
    const toCircle = circles[line.to];

    const fromP = { x: fromCircle.x, y: fromCircle.y };
    const toP = { x: toCircle.x, y: toCircle.y };

    const path = d3.path();
    path.moveTo(fromP.x, fromP.y);
    path.quadraticCurveTo(0, 0, toP.x, toP.y);

    linesGroup
        .append("path")
        .style("fill", "none")
        .style("stroke-width", "2px")
        .style("stroke-dasharray", "10 10")
        .style("stroke", "#000")
        .attr("d", path);
}

