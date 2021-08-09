/*var a = d3.text("ex4.dot").then( function(data) {
    var t = d3.transition()
        .duration(2000)
        .ease(d3.easeLinear);


    var digraphArray = data.split("|");
    var graphviz = d3.select("#graph").graphviz();

    var dot = graphviz.transition(t)
        .fade(false).growEnteringEdges(true)
        .dot(digraphArray[1]);


    graphviz.render();
    console.log(digraphArray[1]);

   // console.log(data);

});*/


var a = d3.text("ex4.dot").then(function (data) {

    var dotIndex = 0;
    var graphviz = d3.select("#graph").graphviz()
        .transition(function () {
            return d3.transition("main")
                .ease(d3.easeLinear)
                .delay(500)
                .duration(1500);
        })
        .on("initEnd", render);

    function render() {
        var dotArray = data.split("|");
        var dotArrayLength = dotArray.length;
        var dot = dotArray[dotIndex];

        graphviz
            .renderDot(dot)
            .on("end", function () {
                dotIndex = dotIndex + 1;
                if (dotIndex === dotArrayLength) {
                    dotIndex = 0;
                } else {
                    render();
                }
            });
    }
});



