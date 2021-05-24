function generateBarChart(fileName) {
    d3.csv(fileName, function (error, data) {

        var maxDuration = d3.max(data, d => parseInt(d.duration));


        var nestedEntries = d3.nest().key(d => d.method_name).entries(data);
        var xScale = d3.scaleBand().range([0, 600]).padding(0.4);
        var svg = d3.select("svg").attr("style", "height: 480px; width: 600px;");

        var g = svg.append("g");

        nestedEntries.forEach(data => {
            var min = d3.min(data.values, d => parseInt(d.duration));
            var max = d3.max(data.values, d => parseInt(d.duration));

            var method_duration = max - min;
            data.method_duration = method_duration;
        })

        var yScale = d3.scaleLinear().domain([0, d3.max(nestedEntries, d => d.method_duration)]).range([0, 460]);
        xScale.domain(nestedEntries.map(function (d) {
            return d.key;
        }));

        g.append("g")
            .attr("transform", "translate(0," + 480 + ")")
            .call(d3.axisBottom(xScale))
            .append("text")
            .attr("y", 480 - 250)
            .attr("x", 600 - 100)
            .attr("text-anchor", "end")
            .attr("stroke", "black")
            .text("Year");

        g.append("g")
            .call(d3.axisLeft(yScale).tickFormat(function (d) {
                return "$" + d;
            })
                .ticks(10))
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", "-5.1em")
            .attr("text-anchor", "end")
            .attr("stroke", "black")
            .text("Stock Price");

        g.selectAll(".bar")
            .data(nestedEntries)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", function (d) {
                return xScale(d.key);
            })
            .attr("y", function (d) {
                return yScale(d.method_duration);
            })
            .attr("width", xScale.bandwidth())
            .attr("height", function (d) {
                return 480 - yScale(d.method_duration);
            });


    });
}