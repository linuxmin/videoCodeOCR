/*const svg = d3.select("svg"),
    diameter = +svg.attr("width"),
    g = svg.append("g").attr("transform", "translate(2,2)"),
    format = d3.format(",d");*/
d3.json("method_test.json", nestIt);
var xScale = d3.scaleLinear().domain([0, 18835]).range([200, 800]);
var yScale = d3.scaleLinear().domain([0, 18835]).range([100, 500]);

var lineFunction = d3.line()
    .x(function (d) {
        return d.x;
    })
    .y(function (d) {
        return d.y;
    });

function nestIt(data) {
    var nestedData = d3.nest().key(d => d.duration_current).entries(data);

    //   nestedData.forEach(d => console.log(d));
    var links = [];
    var nodes = [];

    var i = 0;
    nodes.push({
        id: nestedData[0].key,
        name: nestedData[0].values[0].class_name,
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
                name: nestedData[j].values[0].class_name,
                x: (nestedData[j].key / (Math.random() + (2 * Math.random()))),
                y: (nestedData[j].key / (1 + (2 * Math.random())))

            });
            i = j;
        }
    }
    console.log(nodes);

    var width = 1440, height = 500;

    var svg = d3.select("svg")
        .attr("width", width)
        .attr("height", height);

    // var c10 = d3.scaleBandcategory10();
    /*
        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function(d) { return d.id; }).distance(200))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2));*/

    /*  var link = svg.append("g")
          .attr("class", "links")
          .selectAll("line")
          .data(links)
          .enter().append("line")
          .attr("stroke-width", 1)
          .attr("stroke", "gray")
          .attr("fill", "none") .transition()
          .duration(6000)
          .ease(d3.easeLinear);*/


    var linking = svg.selectAll("link")
        .data(links)
        .enter()
        .append("line")
        .attr("class", "link")
        .attr("x1", function (l) {
            var sourceNode = nodes.find(d => d.id === l.source);

            d3.select(this).attr("y1", yScale(sourceNode.y));

            return xScale(sourceNode.x);
        })
        .attr("x2", function (l) {
            var targetNode = nodes.find(d => d.id === l.target);

            d3.select(this).attr("y2", yScale(targetNode.y));
            return xScale(targetNode.x);
        })
        //   .attr("fill", "none")
        .attr("stroke-width", 1)
        .attr("stroke", "gray").attr("stroke-dasharray", 600 + " " + 600)
        .attr("stroke-dashoffset", 600).transition()
        .attr("stroke-dashoffset", 0)
        .duration(5000)
        .delay(function (d, i) {
            return i * 5000;
        })
        .ease(d3.easeLinear);


    var node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("r", 10)
        .attr("stroke", "gray")
        .attr("cx", function (d) {

            return xScale(d.x);
        })
        .attr("cy", function (d) {
            return yScale(d.y);
        })
        .attr("fill", function (d) {
            return d.id;
        });

    /*    simulation
            .nodes(nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(links);*/

    function ticked() {
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

        node
            .attr("cx", function (d) {
                return d.x;
            })
            .attr("cy", function (d) {
                return d.y;
            });
    }

}

/*

const links = [
    {source: "animate1", target: "animate4"},
    {source: "animate6", target: "animate9"},
    {source: "animate9", target: "animate6"},
];

let root = {
    "children": [
        {
            "name": "analytics",
            "children": [
                {
                    "name": "animate1",
                    "size": 1500
                },
                {
                    "name": "animate2",
                    "size": 1500
                },
                {
                    "name": "animate3",
                    "size": 1500
                }
            ]
        },
        {
            "name": "analytics",
            "children": [
                {
                    "name": "animate4",
                    "size": 1500
                },
                {
                    "name": "animate5",
                    "size": 1500
                },
                {
                    "name": "animate6",
                    "size": 1500
                }
            ]
        },
        {
            "name": "analytics",
            "children": [
                {
                    "name": "animate7",
                    "size": 1500
                },
                {
                    "name": "animate8",
                    "size": 1500
                },
                {
                    "name": "animate9",
                    "size": 1500
                }
            ]
        }
    ]
}


svg.append("svg:defs")
    .append("svg:marker")
    .attr("id", "arrow")
    .attr("viewBox", "0 0 10 10")
    .attr("refX", 10)
    .attr("refY", 5)
    .attr("markerUnits", "strokeWidth")
    .attr("markerWidth", 6)
    .attr("markerHeight", 3)
    .attr("orient", "auto")
    .append("svg:path")
    .style("stroke", "none")
    .attr("d", "M 0 0 L 10 5 L 0 10 z");


const pack = d3.pack()
    .size([diameter / 2, diameter / 2])
    .padding(40)

root = d3.hierarchy(root)
    .sum(function (d) {
        return d.size;
    })
    .sort(function (a, b) {
        return b.value - a.value;
    });

const node = g.selectAll(".node")
    .data(pack(root).descendants())
    .enter()
    .filter(function (d) {
        return !!d.data.name
    })
    .append("g")
    .attr('id', function (d) {
        return d.data.name;
    })
    .attr("class", function (d) {
        return d.children ? "node" : "leaf node";
    })
    .attr("transform", function (d) {
        return "translate(" + d.x + "," + d.y + ")";
    });

node.append("title")
    .text(function (d) {
        return d.data.name + "\n" + format(d.value);
    });


const arrow = svg.selectAll('path.arrow').data(links, JSON.stringify);
arrow.enter()
    .append("path")
    .attr("class", "arrow")
    .attr("x1", function (d) {
        let translate = getTranslate(d.source);
        return translate[0]
    })
    .attr("x2", function (d) {
        let translate = getTranslate(d.target);
        return translate[0]
    })
    .attr("y1", function (d) {
        let translate = getTranslate(d.source);
        return translate[1]
    })
    .attr("y2", function (d) {
        let translate = getTranslate(d.target);
        return translate[1]
    })
    .attr("d", function (d) {
        let source = getTranslate(d.source),
            target = getTranslate(d.target),
            x1 = source[0],
            x2 = target[0],
            y1 = source[1],
            y2 = target[1];


        let dx = x1 - x2,
            dy = y1 - y2,
            dr = Math.sqrt(dx * dx + dy * dy);
        // return "M" + x1 + "," + y1 + "A" + dr + "," + dr + " 0 0,1 " + (x2 - 0.6) + "," + (y2 - 0.9);
        return "M" + x1 + "," + y1 + "A" + dr + "," + dr + " 0 0,1 " + x2 + "," + y2;
    })
    .style("stroke", "black")
    .style("fill", "none")
    .style("stroke-width", 3)
    .attr("marker-end", "url(#arrow)");


node.append("circle")
    .attr("r", function (d) {
        return d.r;
    })

node.filter(function (d) {
    return !d.children;
}).append("text")
    .attr("dy", "0.3em")
    .text(function (d) {
        return d.data.name.substring(0, d.r / 3);
    });


function getTranslate(datum) {
    const circle = d3.select('#' + datum);
    const string = circle.attr("transform");
    const translate = string.substring(string.indexOf("(") + 1, string.indexOf(")")).split(",");
    return translate;
}
*/
