var width = 450;
height = 350;
radius = Math.min(width, height) / 4;

var color = d3.scaleOrdinal().range(['darkblue','steelblue','blue',  'lightblue']);

var pie = d3.pie()
    .value(function(d) { return d['JAN2016']; })
    .sort(null);

var arc = d3.arc()
    .innerRadius(radius - 50)
    .outerRadius(radius - 20);

var svg = d3.selectAll("svg")
    .attr("width", width)
    .attr("height", height)
    .append("g")
    .attr('transform', 'translate(' + 100 +',' + 75 + ')');

var period = ['JAN 2016','FEB 2016','MAR 2016', 'APR 2016', 'MAY 2016', 'JUN 2016',
    'JUL 2016', 'AUG 2016', 'SEP 2016', 'OCT 2016', 'NOV 2016', 'DEC 2016'];
var pie_data = {
    'JAN2016': [16,4,1,30],
    'FEB2016': [17,4,0,30],
    'MAR2016': [16,5,1,29],
    'APR2016': [17,4,1,29],
    'MAY2016': [17,4,1,29],
    'JUN2016': [17,4,2,28],
    'JUL2016': [18,3,2,28],
    'AUG2016': [18,3,2,28],
    'SEP2016': [18,3,2,28],
    'OCT2016': [18,3,3,27],
    'NOV2016': [18,3,3,27],
    'DEC2016': [18,3,3,27]
};

var path = svg.datum(formatData(pie_data)).selectAll("path")
    .data(pie)
    .enter().append("path")
    .attr("fill", function(d, i) { return color(i); })
    .attr("d", arc)
    .each(function(d) { this._current = d; }); // store the initial angles

d3.select('#timeslide').on('input', function() {
    change(this.value);
});

function change(key) {
    var value =  period[key].replace(' ','');
    document.getElementById('range').innerHTML=period[key];
    pie.value(function(d) { return d[value]; }); // change the value function
    path = path.data(pie); // compute the new angles
    path.transition().duration(750).attrTween("d", arcTween); // redraw the arcs
}

function arcTween(a) {
    var i = d3.interpolate(this._current, a);
    this._current = i(0);
    return function(t) {
        return arc(i(t));
    };
}

function formatData(data){
    return data[Object.keys(data)[0]].map(function(item, index){
        let obj = {};
        Object.keys(data).forEach(function(key){
            obj[key] = data[key][index] //JAN2016 : [index]
        })
        return obj;
    })
}

/*

var pieGenerator = d3.pie().value(function (d) {
    return d.quantity;
});
// Create an arc generator with configuration
var arcGenerator = d3.arc()
    .innerRadius(75)
    .outerRadius(200)
    .padAngle(.02)
    .padRadius(100)
    .cornerRadius(4);


var fruits = [
    {name: 'Apples', quantity: 20},
    {name: 'Bananas', quantity: 40},
    {name: 'Cherries', quantity: 50},
    {name: 'Damsons', quantity: 10},
    {name: 'Elderberries', quantity: 30},
];
var fruits2 = [
    {name: 'Blueberries', quantity: 40},
    {name: 'Bananas', quantity: 40},
    {name: 'Cherries', quantity: 50},
    {name: 'Damsons', quantity: 10},
    {name: 'Elderberries', quantity: 30},
];

d3.csv("captures/threesurfaces/surfaces/gaze_positions_on_surfaces.csv", function (error, rawData) {
    if (error) {
        throw error;
    }

    var codeCount = 0;
    var navCount = 0;

    var data = rawData.map(function (d) {
        if (d.surface === "code") {
            if (d.on_surf === "True") {
                codeCount++;
            }
            return {
                framenumber: +d.world_index,
                surface: d.surface,
                counter: +codeCount
            };
        }
        if (d.on_surf === "True") {
            navCount++;
        }
        return {
            framenumber: +d.world_index,
            surface: d.surface,
            counter: +navCount
        };
    });

    console.log(data[0]);

    console.log(pieGenerator(data))



    update(fruits);
});

function update(myData) {

    var arcData = pieGenerator(myData);
    var colorDomain = myData.map(function (a) {
        return a.name;
    });
    var colorScale = d3.scaleOrdinal()
        .domain(colorDomain)
        .range(['#ff2800', '#58595B', '#006c93', '#8D2048', '#00746F'])

    // Create a path element and set its d attribute
    var u = d3.select('g')
        .selectAll('path');

    u.data(arcData)
        .enter()
        .append('path')
        .merge(u)
        .attr('d', arcGenerator)
        .each(function (d) {
            d3.select(this)
                .style('fill', function (d) {
                    return colorScale(d.data.name);
                })
        });

    u.exit().remove();
}

function doUpdate() {
    update(fruits2);
}
*/


