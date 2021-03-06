var svg = d3.select("svg"),
    width = svg.attr("width"),
    height = svg.attr("height"),
    radius = Math.min(width, height) / 2;

var g = svg.append("g")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

var color = d3.scaleOrdinal(['#4daf4a', '#377eb8', '#ff7f00', '#984ea3', '#e41a1c']);

var pie = d3.pie().value(function (d) {
    return (d.gaze_count / d.total_count) * 100;
});

var path = d3.arc()
    .outerRadius(radius - 10)
    .innerRadius(0);

var label = d3.arc()
    .outerRadius(radius)
    .innerRadius(radius - 80);

function visualizePieChart(videoName) {
    var s = "/extracted-dir/" + videoName + "/vizData/normal_surface_gaze_distribution.csv";

    d3.csv(s, function (error, data) {
        if (error) {
            throw error;
        }
        var arc = g.selectAll(".arc")
            .data(pie(data))
            .enter().append("g")
            .attr("class", "arc");

        arc.append("path")
            .attr("d", path)
            .attr("fill", function (d) {
                return color(d.data.surface_name);
            }).on("mouseover", onMouseOver) //Add listener for the mouseover event
            .on("mouseout", onMouseOut)   //Add listener for the mouseout event

        console.log(arc)

        arc.append("text")
            .attr("transform", function (d) {
                return "translate(" + label.centroid(d) + ")";
            })
            .text(function (d) {
                return d.data.surface_name;
            });
    });
}

svg.append("g")
    .attr("transform", "translate(" + (width / 2 - 120) + "," + 20 + ")")
    .append("text")
    .text("Gaze counts per Surface")
    .attr("class", "title")



//mouseover event handler function
function onMouseOver(d, i) {
    d3.select(this).attr('class', 'highlight');

    g.append("text")
        .attr('class', 'val')
        .text(function() {
            return [ +d.value + '%'];  // fixation_duration of the text
        });
}

//mouseout event handler function
function onMouseOut(d, i) {
    // use the text label class to remove label on mouseout
    d3.select(this).attr('class', 'g');

    d3.selectAll('.val')
        .remove()
}
