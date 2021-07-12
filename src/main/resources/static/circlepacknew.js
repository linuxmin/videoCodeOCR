d3.json("testcircleforce.json", loadIt);
var width = 1440;
var height = 960;

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

function loadIt(error, data) {

    var outerNodes = data.nodes
    var outerLinks = data.links;

    buildOuterGraph(outerNodes, outerLinks);
    buildInnerGraph(outerNodes);
    createPathMarkers();

}

function buildOuterGraph(outerNodes, outerLinks) {
    buildGraphNodesAndLinks(outerNodes, outerLinks);
    buildOuterLayout(outerNodes, outerLinks);
}

function buildInnerGraph(outerNodes) {
    outerNodes.forEach(function (outerNode) {

        var innerNodes = outerNode.inner_nodes;
        var innerLinks = [];

        innerNodes.forEach(function (innerLink) {
            if (innerLink.source !== innerLink.target) {
                innerLinks.push({
                    source: innerLink.source,
                    target: innerLink.target
                });
            }
        })


        buildGraphNodesAndLinks(innerNodes, innerLinks);
        buildInnerLayout(outerNode, innerNodes, innerLinks);

    })
}

function buildGraphNodesAndLinks(nodes, links) {
    var tempObj = {}

    nodes.forEach(function (d, i) {
        tempObj[d.id] = i;
    })

    links.forEach(function (d) {
        d.source = tempObj[d.source];
        d.target = tempObj[d.target];
    })
}


function buildOuterLayout(outerNodes, outerLinks) {
    var outerLayout = d3.layout.force()
        .size([width, height])
        .charge(-80000)
        .gravity(0.85)
        .nodes(outerNodes)
        .links(outerLinks)
        .on("tick", () => {
            outerPath.attr("d", function (d) {
                var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);
                return "M" +
                    d.source.x + "," +
                    d.source.y + "A" +
                    dr + "," + dr + " 0 0,1 " +
                    d.target.x + "," +
                    d.target.y;
            });
            outerNodesSelect.attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
        })
        .start();

    var outerPath = svg.append("svg:g").selectAll("path")
        .data(outerLayout.links())
        .enter().append("svg:path")
        .attr("class", "link")
        .attr("marker-end", "url(#outer)");


    var outerNodesSelect = svg.selectAll("g.outer")
        .data(outerNodes, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "outer")
        .attr("id", function (d) {
            return d.id;
        })
        .call(outerLayout.drag());

    outerNodesSelect
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        .attr("r", 80);
}

function buildInnerLayout(outerNode, innerNodes, innerLinks) {
    var outerNodeSelect = svg.select("g.outer#" + outerNode.id);

    var innerNodesSelect = outerNodeSelect.selectAll("g.inner")
        .data(innerNodes, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "inner")
        .attr("id", function (d) {
            return d.id;
        });

    var innerLayout = d3.layout.force()
        .size([160, 160])
        .charge(-600)
        .gravity(1)
        .links(innerLinks)
        .nodes(innerNodes)
        .on("tick", () => {

            innerPath.attr("d", function (d) {
                var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);
                return "M" +
                    (d.source.x - 50) + "," +
                    (d.source.y - 50) + "A" +
                    dr + "," + dr + " 0 0,1 " +
                    (d.target.x - 50) + "," +
                    (d.target.y - 50);
            });
            innerNodesSelect.attr("transform", function (d) {
                return "translate(" + (d.x - 50) + "," + (d.y - 50) + ")";
            });
        })
        .start();

    var innerPath = outerNodeSelect.append("svg:g").selectAll("path")
        .data(innerLayout.links())
        .enter().append("svg:path")
        .attr("class", "link")
        .attr("marker-end", "url(#inner)");

    innerNodesSelect
        .append("circle")
        .style("fill", "orange")
        .style("stroke", "blue")
        .attr("r", 6);

    innerNodesSelect.call(innerLayout.drag()
        .on("dragstart", function () {
            d3.event.sourceEvent.stopPropagation();
        })
    );
}

function createPathMarkers() {
    // build the arrow.
    svg.append("svg:defs").selectAll("marker")
        .data(["outer"])      // Different link/path types can be defined here
        .enter().append("svg:marker")    // This section adds in the arrows
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 95)
        .attr("refY", -16)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");

    svg.append("svg:defs").selectAll("marker")
        .data(["inner"])      // Different link/path types can be defined here
        .enter().append("svg:marker")    // This section adds in the arrows
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -1.5)
        .attr("markerWidth", 4)
        .attr("markerHeight", 4)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
}
