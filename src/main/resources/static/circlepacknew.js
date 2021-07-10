d3.json("testcircleforce.json", loadIt);


var svg = d3.select("body").append("svg")
    .attr("width", 300)
    .attr("height", 300);

function loadIt(error, data) {

    var outerLinks = data.links;

    var outerNodes = data.nodes;

    var link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(outerLinks)
        .enter().append("line")
        .attr("stroke-width", function (d) {
            return Math.sqrt(d.value);
        });

    var outerLayout = d3.layout.force()
        .size([300, 300])
        .charge(-4000)
        .gravity(0.85)
        .nodes(data.nodes)
        .links(data.links)
        .on("tick", outerTick)
        .start();


    var outerNodes2 = svg.selectAll("g.outer")
        .data(outerNodes, function (d) {
            return d.numb;
        })
        .enter()
        .append("g")
        .attr("class", "outer")
        .attr("id", function (d) {
            return d.id;
        })
        .call(outerLayout.drag());

    outerNodes2
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        .attr("r", 40);


    outerNodes.forEach(function (outerNode) {
        var innerData = outerNode.inner_nodes;
        var innerLayout = d3.layout.force()
            .size([40, 40])
            .charge(-600)
            .gravity(0.75)
            .links([])
            .nodes(innerData)
            .on("tick", e => {
                innerAnodes.attr("transform", function (d) {
                    return "translate(" + (d.x - 20) + "," + (d.y - 20) + ")";
                });
            })
            .start();

        var aNode = svg.select("g.outer#" + outerNode.id);

        var innerAnodes = aNode.selectAll("g.inner")
            .data(innerData, function (d) {
                return d.id;
            })
            .enter()
            .append("g")
            .attr("class", "inner")
            .attr("id", function (d) {
                return d.id;
            })
            .call(innerLayout.drag()
                .on("dragstart", function () {
                    d3.event.sourceEvent.stopPropagation();
                })
            );

        innerAnodes
            .append("circle")
            .style("fill", "orange")
            .style("stroke", "blue")
            .attr("r", 6);
    })

    function outerTick(e) {
        link
            .attr("x1", function (d) {
                return d.source.x;
            })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });
        outerNodes2.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        });
    }

}
