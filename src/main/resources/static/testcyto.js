var cy = cytoscape({

    container: document.getElementById('cy'),

    boxSelectionEnabled: false,
    autounselectify: true,

    style: cytoscape.stylesheet()
        .selector('node')
        .style({
            'content': 'data(id)'
        })
        .selector('edge')
        .style({
            'curve-style': 'bezier',
            'target-arrow-shape': 'triangle',
            'width': 4,
            'line-color': '#ddd',
            'target-arrow-color': '#ddd',
            'opacity': 0
        })
        .selector('.highlighted')
        .style({
            'background-color': '#61bffc',
            'line-color': '#61bffc',
            'target-arrow-color': '#61bffc',
            'transition-property': 'background-color, line-color, target-arrow-color',
            'transition-duration': '0.5s',
            'opacity': 1
        }),

    elements: {
        nodes: [{
            data: {
                id: 'a'
            }
        },
            {
                data: {
                    id: 'b'
                }
            },
            {
                data: {
                    id: 'c'
                }
            },
            {
                data: {
                    id: 'd'
                }
            },
            {
                data: {
                    id: 'e'
                }
            }
        ],

        edges: [{
            data: {
                id: 'a"e',
                weight: 100,
                source: 'a',
                target: 'e'
            }
        },
            {
                data: {
                    id: 'ab',
                    weight: 3,
                    source: 'a',
                    target: 'b'
                }
            },
            {
                data: {
                    id: 'be',
                    weight: 4,
                    source: 'b',
                    target: 'e'
                }
            },
            {
                data: {
                    id: 'bc',
                    weight: 5,
                    source: 'b',
                    target: 'c'
                }
            },
            {
                data: {
                    id: 'ce',
                    weight: 6,
                    source: 'c',
                    target: 'e'
                }
            },
            {
                data: {
                    id: 'cd',
                    weight: 2,
                    source: 'c',
                    target: 'd'
                }
            },
            {
                data: {
                    id: 'de',
                    weight: 7,
                    source: 'd',
                    target: 'e'
                }
            }
        ]
    },

    layout: {
        name: 'avsdf',
        directed: true,
        roots: '#a',
        padding: 10
    }
});

var dijkstra = cy.elements().dijkstra('#a', function(edge){
    return edge.data('weight');
});
var bfs = dijkstra.pathTo( cy.$('#e') );
var x = 0;
var highlightNextEle = function(){
    var el=bfs[x];
    el.addClass('highlighted');
    if(x<bfs.length){
        x++;
        setTimeout(highlightNextEle, 500);
    }
};
highlightNextEle();