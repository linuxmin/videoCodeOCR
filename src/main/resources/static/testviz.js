d3.csv("total_duration.csv", function (error, data) {


    var height = 480;
    var width = 600;

    var svg = d3.select("svg").attr("style", "height: 480px; width: 600px;");

    var g = svg.append("g");

    var minMax = d3.extent(data, d => d.duration);

    var xScale = d3.scaleLinear().domain(minMax).range([100, width]);
    var yScale = d3.scaleBand().range([height, 0]);

    yScale.domain(data.map(function (d) {
        return d.method_name;
    })).padding(0.1);

    g.append("g")
        .attr("transform", "translate(0," + 480 + ")")
        .call(d3.axisBottom(xScale))
        .append("text")
        .attr("x", 480 - 250)
        .attr("y", 600 - 100)
        .attr("text-anchor", "end")
        .attr("stroke", "black")
        .text("Year");

    g.append("g")
        .call(d3.axisLeft(yScale)
            .ticks(10))
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 0)
        .attr("dy", "-5.1em")
        .attr("text-anchor", "end")
        .attr("stroke", "black")
        .text("Stock Price");


    const tooltip = d3.select('body').append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);


    g.selectAll(".bar")
        .data(data)
        .enter().append("rect")
        .attr("class", "bar")
        .attr("x", 0)
        .attr("y", function (d) {
            return yScale(d.method_name);
        })
        .attr("height", yScale.bandwidth())
        .attr("width", function (d) {
            return xScale(d.duration);
        }).on('mouseover', (d) => {
        tooltip.transition().duration(200).style('opacity', 0.9);
        tooltip.html(`<span>Method: ${d.method_name}, Duration: ${d.duration}</span>`)
            .style('left', `${d3.event.layerX}px`)
            .style('top', `${(d3.event.layerY - 28)}px`);
    })
        .on('mouseout', () => tooltip.transition().duration(500).style('opacity', 0));


});
