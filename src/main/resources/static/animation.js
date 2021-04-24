var url = 'https://gist.githubusercontent.com/d3byex/5a8267f90a0d215fcb3e/raw/2d30d7b9b85220395727311dcb94345ef64ca10c/multi_network.html';
d3.json(url, function (error, data) {
    var width = 960, height = 500;
    var svg = d3.select('body')
        .append('svg')
        .attr({
            width: width,
            height: height
        });

    var force = d3.layout.force()
        .size([width, height])
        .linkDistance(1)
        .charge(-4000)
        .nodes(data.nodes)
        .links(data.edges)
        .start();

    var linkTypes = d3.set(data.edges.map(function (d) {
        return d.type;
    })).values();

    svg.append("defs")
        .selectAll("marker")
        .data(linkTypes)
        .enter()
        .append("marker")
        .attr({
            id: function (d) { return d; },
            viewBox: "0 -5 10 10",
            refX: 20,
            refY: -1.5,
            markerWidth: 6,
            markerHeight: 6,
            orient: "auto"
        })
        .append("path")
        .attr("d", "M0,-5L10,0L0,5");



    var edges = svg.append("g")
        .selectAll("path")
        .data(force.links())
        .enter()
        .append("path")
        .attr("class", function (d) {
            return "link " + d.type;
        })
        .attr("marker-end", function(d) {
            return "url(#" + d.type + ")";
        });

    var nodes = svg.append('g')
        .selectAll('g')
        .data(force.nodes())
        .enter()
        .append('g')
        .call(force.drag);

    var colors = d3.scale.category20();
    nodes.append('circle')
        .attr({
            r: 10,
            fill: function (d, i) {
                return colors(i);
            },
            stroke: 'black',
            'stroke-width': 0
        })
        .call(force.drag()
            .on("dragstart", function (d) {
                d.fixed = true;
                d3.select(this).attr('stroke-width', 3);
            }))
        .on('dblclick', function (d) {
            d.fixed = false;
            d3.select(this).attr('stroke-width', 0);
        });

    nodes.append('text')
        .attr({
            dx: 12,
            dy: '.35em',
            'pointer-events': 'none'
        })
        .style('font', '10px sans-serif')
        .text(function (d) { return d.name });

    force.on("tick", function () {
        edges.attr("d", function (d) {
            var dx = d.target.x - d.source.x,
                dy = d.target.y - d.source.y,
                dr = Math.sqrt(dx * dx + dy * dy);
            return "M" + d.source.x + "," + d.source.y + "A" +
                dr + "," + dr + " 0 0,1 " +
                d.target.x + "," + d.target.y;
        });
        nodes.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        });
    });

});
