d3.json("graph_two.json", data => createForceLayout(data));

function createForceLayout(graph) {
    var nodes = graph.nodes;
    var edges = graph.edges;

    var marker = d3.select("svg").append('defs')
        .append('marker')
        .attr("id", "Triangle")
        .attr("refX", 12)
        .attr("refY", 6)
        .attr("markerUnits", 'userSpaceOnUse')
        .attr("markerWidth", 12)
        .attr("markerHeight", 18)
        .attr("orient", 'auto')
        .append('path')
        .attr("d", 'M 0 0 12 6 0 12 3 6');

    var nodeHash = {};
    nodes.forEach(node => {
        nodeHash[node.id] = node;
    });

    edges.forEach(edge => {
        edge.weight = parseInt(edge.weight);
        edge.source = nodeHash[edge.source];
        edge.target = nodeHash[edge.target];
    });

    nodes.forEach(d => {
        d.degreeCentrality = edges
            .filter(p => p.source === d || p.target === d)
            .length;
    });

    var linkForce = d3.forceLink().strength(d => d.weight * .1);

    var simulation = d3.forceSimulation()
        .force("charge", d3.forceManyBody().strength(-500))
        .force("x", d3.forceX(250))
        .force("y", d3.forceY(250))
        .force("link", linkForce)
        .nodes(nodes)
        .on("tick", forceTick);

    simulation.force("link").links(edges);

    d3.select("svg").selectAll("line.link")
        .data(edges, d => `${d.source.id}-${d.target.id}`)
        .enter()
        .append("line")
        .attr("class", "link")
        .style("opacity", .5)
        .style("stroke-width", d => d.weight);

    d3.selectAll("line").attr("marker-end", "url(#Triangle)");

    var nodeEnter = d3.select("svg").selectAll("g.node")
        .data(nodes, d => d.id)
        .enter()
        .append("g")
        .attr("class", "node");
    nodeEnter.append("circle")
        .attr("r", d => d.degreeCentrality * 2);

    nodeEnter.append("text")
        .style("text-anchor", "middle")
        .attr("y", 15)
        .text(d => d.id);

    var drag = d3.drag();

    drag
        .on("drag", dragging);
    d3.selectAll("g.node").call(drag);
    function dragging(d) {
        var e = d3.event;
        d.fx = e.x;
        d.fy = e.y;
        if (simulation.alpha() < 0.1) {
            simulation.alpha(0.1);
            simulation.restart();
        }
    }

    function forceTick() {
        d3.selectAll("line.link")
            .attr("x1", d => d.source.x)
            .attr("x2", d => d.target.x)
            .attr("y1", d => d.source.y)
            .attr("y2", d => d.target.y);
        d3.selectAll("g.node")
            .attr("transform", d => `translate(${d.x},${d.y})`);
    }
}