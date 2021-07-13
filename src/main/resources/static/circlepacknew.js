d3.json("testcircleforce.json", loadIt);
d3.json("testcircleforce.json", loadIt2);

var width = 1440;
var height = 800;

var zoom = d3.behavior.zoom()
    .scaleExtent([1, 10])
    .on("zoom", zoomed);

var zoom2 = d3.behavior.zoom()
    .scaleExtent([1, 10])
    .on("zoom", zoomed2);

var svg = d3.select("#hier").append("svg")
    .attr("width", width)
    .attr("height", height).attr("viewBox", "0 0 " + width + " " + height)
    .attr("preserveAspectRatio", "xMidYMid")
    .call(zoom).append('svg:g');

var svg2 = d3.select("#dort").append("svg")
    .attr("width", width)
    .attr("height", height).attr("preserveAspectRatio", "xMidYMid").call(zoom2).append('svg:g');


function zoomed() {
    svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

function zoomed2() {
    svg2.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}


svg.call(zoom);
svg2.call(zoom);


function loadIt(error, data) {

    var outerNodes = data.nodes
    var outerLinks = data.links;

    buildOuterGraph(outerNodes, outerLinks);
    buildInnerGraph(outerNodes);
    createPathMarkers();
}

function loadIt2(error, data) {

    var outerNodes = data.nodes
    var outerLinks = data.links;

    buildGraphNodesAndLinks(outerNodes, outerLinks);
    buildOuterLayout2(outerNodes, outerLinks);
    buildInnerGraph2(outerNodes);

    createPathMarkers2();
}

function buildOuterGraph(outerNodes, outerLinks) {
    buildGraphNodesAndLinks(outerNodes, outerLinks);
    buildOuterLayout(outerNodes, outerLinks);
}


function buildInnerGraph2(outerNodes) {
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
        buildInnerLayout2(outerNode, innerNodes, innerLinks);

    })
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


function buildOuterLayout2(outerNodes, outerLinks) {


    var outerLayout = d3.layout.force()
        .size([width, height])
        .charge(-80000)
        .gravity(2.85)
        .nodes(outerNodes)
        .links(outerLinks)
        .on("tick", outerTick)
        .start();


    var outerPath = svg2.selectAll("path")
        .data(outerLayout.links())
        .enter().append("svg:path")
        .attr("class", "link")
        .attr("marker-end", "url(#outer)");

    var outerNodesSelect = svg2.selectAll("g.outer")
        .data(outerNodes, function (d) {
            return d.id;
        })
        .enter()
        .append("g")
        .attr("class", "outer")
        .attr("id", function (d) {
            return d.id;
        })
        .call(outerLayout.drag())

    outerNodesSelect
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        .attr("r", 80);

    var outerLables = outerNodesSelect.append("text")
        .attr("class", "outerText")
        .attr("dy", "-4.80em")
        .attr("dx", "-6.80em")
        .text(function (d) {
            return d.name;
        });

    function outerTick() {
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
    }
}

function buildOuterLayout(outerNodes, outerLinks) {


    var outerLayout = d3.layout.force()
        .size([width, height])
        .charge(-80000)
        .gravity(0.85)
        .nodes(outerNodes)
        .links(outerLinks)
        .on("tick", outerTick)
        .start();


    var outerPath = svg.selectAll("path")
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
        .call(outerLayout.drag())

    outerNodesSelect
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        .attr("r", 80);

    var outerLables = outerNodesSelect.append("text")
        .attr("class", "outerText")
        .attr("dy", "-4.80em")
        .attr("dx", "-6.80em")
        .text(function (d) {
            return d.name;
        });

    function outerTick() {
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
    }
}


function buildInnerLayout2(outerNode, innerNodes, innerLinks) {
    var outerNodeSelect = svg2.select("g.outer#" + outerNode.id);

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
    var innerLables = innerNodesSelect.append("text")
        .attr("class", "innerText")
        .attr("dy", "-1.80em")
        .attr("dx", "-0.80em")
        .text(function (d) {
            return d.name;
        });
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
    var innerLables = innerNodesSelect.append("text")
        .attr("class", "innerText")
        .attr("dy", "-1.80em")
        .attr("dx", "-0.80em")
        .text(function (d) {
            return d.name;
        });
}

function createPathMarkers2() {
    // build the arrow.
    svg2.append("svg:defs").selectAll("marker")
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

    svg2.append("svg:defs").selectAll("marker")
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
