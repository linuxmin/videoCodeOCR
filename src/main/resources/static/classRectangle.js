// get the data
d3.json("method_test.json", function (error, data) {

    var nestedData = d3.nest().key(d => d.duration_current).entries(data);

    var links = [];
    var nodes = [];

    var i = 0;

    nodes.push({
        id: nestedData[0].key,
        name: nestedData[0].values[0].class_name.substring(nestedData[0].values[0].class_name.lastIndexOf('.')+1,nestedData[0].values[0].class_name.length),
        x: (nestedData[0].key / (Math.random() + (2 * Math.random()))),
        y: (nestedData[0].key / (1 + (2 * Math.random())))
    });



    for (var j = 0; j < nestedData.length; j++) {

        if (nestedData[i].values[0].class_name !== nestedData[j].values[0].class_name) {
            links.push({
                source: nestedData[i].key,
                target: nestedData[j].key
            });


            nodes.push({
                id: nestedData[j].key,
                name: nestedData[j].values[0].class_name.substring(nestedData[j].values[0].class_name.lastIndexOf('.')+1,nestedData[j].values[0].class_name.length),
                x: (nestedData[j].key / (Math.random() + (2 * Math.random()))),
                y: (nestedData[j].key / (1 + (2 * Math.random())))

            });


            i = j;
        }
    }
    links.push({
        source: "17157",
        target: "1"
    });
// Compute the distinct nodes from the links.
    links.forEach(function (link) {
        link.source = nodes.find(d => d.id === link.source);
        link.target = nodes.find(d => d.id === link.target);
        link.value = +link.source;
    });

    var width = 1440,
        height = 500;

    /*    var force = d3.layout.force()
            .nodes(d3.values(nodes))
            .links(links)
            .size([width, height])
            .linkDistance(60)
            .charge(d3.forceManyBody)
            .on("tick", tick)
            .start();*/

    var force = d3.forceSimulation(nodes)
        .force("charge", d3.forceManyBody().strength(-3000))
        .force('center', d3.forceCenter(width / 2, height / 2))
        .force('collision', d3.forceCollide().radius(function (d) {
            return d.radius
        }))
        .force("link", d3.forceLink(links).id(function (d) {
            return d.id;
        }))
        .on("tick", tick);


    var svg = d3.selectAll("svg")
        .attr("width", width)
        .attr("height", height);

// build the arrow.
    svg.append("svg:defs").selectAll("marker")
        .data(["end"])      // Different link/path types can be defined here
        .enter().append("svg:marker")    // This section adds in the arrows
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -1.5)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");

// add the links and the arrows
    var path = svg.append("svg:g").selectAll("path")
        .data(links)
        .enter().append("svg:path")
        //    .attr("class", function(d) { return "link " + d.type; })
        .attr("class", "link")
        .attr("id", function (d, i) {
            return "path" + i;
        })
        .attr("marker-end", "url(#end)");

// define the nodes
    var node = svg.selectAll(".node")
        .data(nodes)
        .enter().append("g")
        .attr("class", "node");

    node.append("text")
        .attr("x", 30 + 4)
        .attr("y", "0.31em")
        .text(function (d) {
            return d.name;
        }).clone(true).lower();

// add the nodes
    node.append("circle").attr("id", function (n) {
        return "rec" + n.id;
    }).attr("r", 30).style("fill", function (n) {
        if (n.id === "1") {
            return "green";
        }
        return "yellow"
    });



    for (var i = 0; i < links.length; i++) {
        d3.selectAll("svg").append("text")
            .attr("id", "path" + i + "-text")
            .append("textPath")
            .attr("xlink:href", "#path" + i).attr("startOffset", "50%")
            .text(i);

        d3.selectAll("svg").append("use")
            .attr("id", "path" + i + "-line").attr("xlink:href", "#path" + i);
    }

// add the curvy lines
    function tick() {
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
        }).transition()
            .duration(1000);

        node
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
    }

    /* var clickableNodes = ["1","4809"];

     // Set up dictionary of neighbors
     var node2neighbors = {};
     for (var i = 0; i < nodes.length; i++) {
         var name = nodes[i].id;
         node2neighbors[name] = links.filter(function (d) {
             return d.source.id == name || d.target.id == name;
         }).map(function (d) {
             return d.source.id == name ? d.target.id : d.source.id;
         });
     }


     node.filter(function (n) {
         return clickableNodes.indexOf(n.id) != -1;
     })
         .on("click", function (n) {
             // Determine if current node's neighbors and their links are visible
             var active = n.active ? false : true // toggle whether node is active
                 , newOpacity = active ? 0 : 1;

             // Extract node's name and the names of its neighbors
             var name = n.id
                 , neighbors = node2neighbors[name];

             // Hide the neighbors and their links
             for (var i = 0; i < neighbors.length; i++) {
                 d3.select("#rec" + neighbors[i]).style("opacity", newOpacity);
                 d3.select("#path" + neighbors[i]).style("opacity", newOpacity);
                 d3.select(".link").style("opacity", newOpacity);

             }
             // Update whether or not the node is active
             n.active = active;
         });*/
});
