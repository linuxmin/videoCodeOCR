d3.json("testcircleforce.json", loadIt);
var width = 1440;
var height = 960;

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

function loadIt(error, data) {

    var outerLinks = data.links;

    var outerNodes = data.nodes;
    /*

        var link = svg.append("g")
            .attr("class", "links")
            .selectAll("line")
            .data(outerLinks)
            .enter().append("line");
    */


    var outerLayout = d3.layout.force()
        .size([width, height])
        .charge(-80000)
        .gravity(0.85)
        .nodes(data.nodes)
        .links(data.links)
        .on("tick", outerTick)
        .start();


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
        .data(["end"])      // Different link/path types can be defined here
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

// add the links and the arrows
    var path = svg.append("svg:g").selectAll("path")
        .data(outerLayout.links())
        .enter().append("svg:path")
        //    .attr("class", function(d) { return "link " + d.type; })
        .attr("class", "link")
        .attr("marker-end", "url(#outer)");


    var outerNodes2 = svg.selectAll("g.outer")
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

    outerNodes2
        .append("circle")
        .style("fill", "pink")
        .style("stroke", "blue")
        //.style("opacity",0.5)
        .attr("r", 80);


    outerNodes.forEach(function (outerNode) {

        var innerData = outerNode.inner_nodes;
        var inner_links = [];

        innerData.forEach(function (innerLink) {
            if (innerLink.source !== innerLink.target) {
                inner_links.push({
                    source: innerLink.source,
                    target: innerLink.target
                });
            }
        })


        var obj = {}

        innerData.forEach(function (d, i) {
            obj[d.id] = i;
        })

        inner_links.forEach(function (d) {
            d.source = obj[d.source];
            d.target = obj[d.target];
        })


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
            });

        innerAnodes
            .append("circle")
            .style("fill", "orange")
            .style("stroke", "blue")
            .attr("r", 6);
        /*
                var innerLink = innerAnodes.append("g")
                    .attr("class", "links")
                    .selectAll("line")
                    .data(inner_links)
                    .enter().append("line");*/


        var innerLayout = d3.layout.force()
            .size([160, 160])
            .charge(-600)
            .gravity(1)
            .links(inner_links)
            .nodes(innerData)
            .on("tick", innerTick)
            .start();

        var innerPath = aNode.append("svg:g").selectAll("path")
            .data(innerLayout.links())
            .enter().append("svg:path")
            //    .attr("class", function(d) { return "link " + d.type; })
            .attr("class", "link")
            .attr("marker-end", "url(#end)");


        innerAnodes.call(innerLayout.drag()
            .on("dragstart", function () {
                d3.event.sourceEvent.stopPropagation();
            })
        );

        function innerTick(e) {

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
            innerAnodes.attr("transform", function (d) {
                return "translate(" + (d.x - 50) + "," + (d.y - 50) + ")";
            });
        }

    })

    function outerTick(e) {
        path.attr("d", function (d) {
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
        outerNodes2.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        });
    }

}
