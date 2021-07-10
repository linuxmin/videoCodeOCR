d3.json("class_network.json", loadIt);


var svg = d3.select("body").append("svg")
    .attr("width", 300)
    .attr("height", 300);

////////////////////////
// outer force layout
function loadIt(error, data) {
    var outerData = data;

    var outerLayout = d3.layout.force()
        .size([300, 300])
        .charge(-4000)
        .gravity(0.85)
        .links([])
        .nodes(outerData)
        .on("tick", outerTick)
        .start();

    var outerNodes = svg.selectAll("g.outer")
        .data(outerData, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "outer")
        .attr("id", function (d) {
            return d.id;
        })
        .call(outerLayout.drag());

    outerNodes
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        .attr("r", 40);

////////////////////////
// inner force layouts

    var innerAdata = [{"id": "A1"}, {"id": "A2"}];

    var innerAlayout = d3.layout.force()
        .size([40, 40])
        .charge(-600)
        .gravity(0.75)
        .links([])
        .nodes(innerAdata)
        .on("tick", innerAtick)
        .start();

    var aNode = svg.select("g.outer#A");

    var innerAnodes = aNode.selectAll("g.inner")
        .data(innerAdata, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "inner")
        .attr("id", function (d) {
            return d.id;
        })
        .call(innerAlayout.drag()
            .on("dragstart", function () {
                d3.event.sourceEvent.stopPropagation();
            })
        );

    innerAnodes
        .append("circle")
        .style("fill", "orange")
        .style("stroke", "blue")
        .attr("r", 6);

///////

    var innerBdata = [{"id": "B1"}, {"id": "B2"}];

    var innerBlayout = d3.layout.force()
        .size([40, 40])
        .charge(-600)
        .gravity(0.75)
        .links([])
        .nodes(innerBdata)
        .on("tick", innerBtick)
        .start();

    var bNode = svg.select("g.outer#B");

    var innerBnodes = bNode.selectAll("g.inner")
        .data(innerBdata, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "inner")
        .attr("id", function (d) {
            return d.id;
        })
        .call(innerBlayout.drag()
            .on("dragstart", function () {
                d3.event.sourceEvent.stopPropagation();
            })
        );

    innerBnodes
        .append("circle")
        .style("fill", "orange")
        .style("stroke", "blue")
        .attr("r", 6);

    //////////////////////////
    // functions

    function outerTick(e) {
        outerNodes.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        });
    }

    function innerAtick(e) {
        innerAnodes.attr("transform", function (d) {
            return "translate(" + (d.x - 20) + "," + (d.y - 20) + ")";
        });
    }

    function innerBtick(e) {
        innerBnodes.attr("transform", function (d) {
            return "translate(" + (d.x - 20) + "," + (d.y - 20) + ")";
        });
    }

}
