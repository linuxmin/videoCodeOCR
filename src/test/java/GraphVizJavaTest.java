import static guru.nidi.graphviz.attribute.Attributes.attr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

public class GraphVizJavaTest
{

    @Test
    public void createDotFile() throws IOException
    {
        final String userRunDir = System.getProperties().getProperty("user.dir");
        final String pathToSource = userRunDir + "/src/test/resources/test3.mp4";
        final String pathToDest = userRunDir + "/src/test/resources/";

        final Path pathToSrc = Paths.get(pathToSource);
        final Path pathToDstDir = Paths.get(pathToDest);

        Graph g = Factory.graph().directed()

            .nodeAttr().with(Font.name("arial"))
            .linkAttr().with("class", "link-class")
            .with(
                Factory.node("a").with(Color.RED).link(Factory.node("b")),
                Factory.node("b").link(
                    Link.to(Factory.node("c")).with(attr("weight", 5), Style.DASHED)
                )
            );

        final List<MutableNode> nodesList = new ArrayList<>(g.toMutable().nodes());

/*        Graph g2 = Factory.graph().directed()
            .nodeAttr().with(Font.name("arial"))
            .linkAttr().with("class", "link-class")
            .with(
                Factory.node("d").with(Color.RED).link(Factory.node("b")),
                Factory.node("e").link(
                    to(Factory.node("f")).with(attr("weight", 5), Style.BOLD)
                )
            );*/

        Graph g2 = Factory.graph().directed()
            .nodeAttr().with(Font.name("arial"))
            .linkAttr().with("class", "link-class")
            .with(
                nodesList
            ).with(
                Factory.node("d").with(Color.RED).link(Factory.node("b")),
                Factory.node("e").link(
                    Link.to(Factory.node("f")).with(attr("weight", 5), Style.BOLD)
                ));

        MutableGraph mg = Factory.mutGraph().setDirected(true).use((gr, ctx) -> {
            Factory.mutNode("b");
            Factory.nodeAttrs().add(Color.RED);
            Factory.mutNode("c").addLink(Factory.mutNode("a"));
        });

        g.addTo(mg);

        final String firstString = Graphviz.fromGraph(g).height(300).render(Format.DOT).toString();
        final String secondString = Graphviz.fromGraph(g2).height(300).render(Format.DOT).toString();

        final String resultString = firstString + "|" + secondString;

        Graphviz.fromString(resultString).height(300).render(Format.DOT).toFile(new File("example/ex8.dot"));

    }
}

