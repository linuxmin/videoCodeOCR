var svg = d3.select('svg'),
    width = +svg.attr("width"),
    height = +svg.attr("height"),
    color = d3.scaleOrdinal(d3.schemeCategory10);


/////// SLIDER ///////
var L = 10;
var slider_size = 0.75*width;
var left_margin = 0.5*(width - slider_size);

var x = d3.scaleLinear()
    .domain([0,10])
    .range([left_margin, slider_size + left_margin])
    .clamp(true);

var slider = svg.append("g")
    .attr("transform", "translate(15,"+(height-50)+")");

slider.append("line")
    .attr("class", "track")
    .attr("x1", x.range()[0])
    .attr("x2", x.range()[1])
    .select(function() { return this.parentNode.appendChild(this.cloneNode(true)); })
    .attr("class", "track-inset")
    .select(function() { return this.parentNode.appendChild(this.cloneNode(true)); })
    .attr("class", "track-overlay")
    .call(d3.drag()
        .on("start.interrupt", function() { slider.interrupt(); })
        .on("start drag", function() { return hue(x.invert(d3.event.x)); }));

var seconds = d3.range(0,200,40)
var dx = L/(seconds.length-1)
var xticks = d3.range(0,L+dx,dx)

slider.insert("g", ".track-overlay")
    .attr("class", "ticks")
    .attr("transform", "translate(0," + 25 + ")")
    .selectAll("text")
    .data(xticks)
    .enter().append("text")
    .attr("x", x)
    .attr("text-anchor", "middle")
    .text(function(d,i) { return seconds[i]; });

var handle = slider.insert("circle", ".track-overlay")
    .attr("class", "handle")
    .attr("r", 9)
    .attr("cx", x.range()[0]); //initial position to zero

function hue(h) {
    handle.attr("cx", x(h));
}

function dragstarted(d) {
    if (!d3.event.active) simulation.alphaTarget(0).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(d) {
    d.fx = d3.event.x;
    d.fy = d3.event.y;
}

function dragended(d) {
    if (!d3.event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}


/////// GRAPH ////////
d3.json("graph.json", function(error, graph) {
    if (error) throw error;

    //user-defined parameters
    var maxDistance = 300, //max distance between two nodes
        minDistance = 100, //min distance betwween two nodes
        maxRadius = 30, //max radius of circle
        minRadius = 8, //min radius of circle
        minLinkwidth = 5, //min width of link
        maxLinkwidth = 9 //max width of link

    var [maxConnect, maxFraction] = getnetworkProp(graph);

    var nodes = graph.nodes,
        nodeById = d3.map(nodes, function(d) { return d.id; }),
        links = graph.links,
        value = links.map(function(d){return d.value}),

        l = []
    links.forEach(function(link) {
        var s = nodeById.get(link.source),
            t = nodeById.get(link.target),
            v = link.value,
            y = link.second;

        l.push({source: s, target: t, second: y, value:v});
    });

    links = l

    simulation = d3.forceSimulation(nodes)

    simulation.force("charge", d3.forceManyBody())
        .force("link", d3.forceLink(links))
        .on("tick", ticked);

    var g = svg.append("g").attr("transform", "translate(" + width / 2 + "," + 0.45 * height + ")"),
        link = g.append("g").attr("stroke", "#000").attr("stroke-width", 1.5).selectAll(".link"),
        node = g.append("g").attr("stroke", "#fff").attr("stroke-width", 1.5).selectAll(".node");

    restart();

    d3.interval(function() {
        restart();
    },150)


    function restart() {

        var current_second = seconds[Math.round(x.invert(jQuery(".handle").attr("cx"))/dx)];
        //get "radius" of each node for current_year
        var fraction = graph.nodes.map(function(d) {return d.fraction}).map(function(d) {return d[current_second.toString()];})

        // Apply the general update pattern to the nodes.
        node = node.data(nodes, function(d) { return d.id;});
        node.exit().remove();
        node = node.enter().append("circle")
            .attr("class","node")
            .attr("fill", function(d) { return color(d.id); })
            .merge(node)
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));

        node.append("title")
            .text(function(d) { return d.id; });

        //apply transition to radii of nodes
        node.transition()
            .duration(50)
            .attr("r",function(d) {return Math.max(minRadius,d.fraction[current_second.toString()]/maxFraction * maxRadius);})

        // Apply the general update pattern to the links
        links_filtered = links.filter(function(d) {return d.second==current_second;});
        link = link.data(links_filtered, function(d) { return d.source.id + "-" + d.target.id; });
        link.exit().remove();
        link = link.enter()
            .append("line")
            .attr("class", "link")
            .merge(link);

        //define transition to width of edges
        link.transition()
            .duration(50)
            .attr("stroke-width", function(d,i) { return  Math.max(minLinkwidth,d.value/maxConnect * maxLinkwidth);})


        // Update and restart the simulation.
        simulation.nodes(nodes);
        simulation.force("link").links(links)
        simulation.force("link", d3.forceLink(links)
            .distance(function(d) {
                if(d.second==current_second){
                    return Math.min(maxConnect/d.value * minDistance,maxDistance);
                }
                else{
                    return maxDistance;
                }})
        )

        simulation.alpha(0.4).restart();
    }

    //this function defines position of nodes and links
    //at each "simulation time step"
    function ticked() {
        node.attr("cx", function(d) { return d.x; })
            .attr("cy", function(d) { return d.y; })

        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });
    }


})

//this function calculates properties of network
//(i.e., max connection between nodes, max fraction value of node)
function getnetworkProp(graph){
    //1) max connection between nodes
    var maxConnect = Math.max.apply(Math,graph.links.map(function(d) {return d.value;}));   //max connection between nodes

    //2) max fraction value (used to draw nodes radii)
    var maxFraction = 0;
    var arr, obj, maxf;
    for (i=0;i<graph.nodes.length;i++){
        obj = graph.nodes[i].fraction
        arr = Object.keys( obj ).map(function (key) { return obj[key]; });
        maxf = Math.max.apply( null, arr );
        maxFraction = Math.max(maxFraction,maxf);
    }
    return [maxConnect, maxFraction];
}