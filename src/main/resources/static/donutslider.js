/*
var pieGenerator = d3.pie()
	.value(function(d) {return d.quantity;})
	.sort(function(a, b) {
		return a.name.localeCompare(b.name);
	});

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

function doUpdate() {
  update(fruits2);
}

// Create an arc generator with configuration
var arcGenerator = d3.arc()
	.innerRadius(75)
	.outerRadius(200)
  .padAngle(.02)
  .padRadius(100)
  .cornerRadius(4);

function update(myData) {

    var arcData = pieGenerator(myData);
    var colorDomain = myData.map(function(a) {return a.name;});
    var colorScale = d3.scaleOrdinal()
      .domain(colorDomain)
      .range(['#ff2800','#58595B','#006c93','#8D2048','#00746F'])

  // Create a path element and set its d attribute
    var u = d3.select('g')
  	.selectAll('path');

  	u.data(arcData)
    .enter()
  	.append('path')
    .merge(u)
  	.attr('d', arcGenerator)
    .each(function(d){
      d3.select(this)
        .style('fill',function(d) {
          return colorScale(d.data.name);
        })
    });

    u.exit().remove();
  }
  update(fruits);
 */

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

    update(data,100);
});

function update(myData,i) {
    myData = myData.filter(function( element ) {
        return element.framenumber === i;
    });
    var arcData = pieGenerator(myData);
    var colorDomain = myData.map(function (a) {
        return a.surface;
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
                    return colorScale(d.data.surface);
                })
        });

    u.exit().remove();
}

function doUpdate() {

}


