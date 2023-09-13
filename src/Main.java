import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Main {

    static final String testPath = "G:\\TimskiProektShaclGenerator\\src\\shacl files\\example3.ttl";

    private static final RdfGenerator rdfGenerator = new RdfGenerator();

    public static void main(String[] args) throws FileNotFoundException {

        List<Triple> triplesList = convertShaclFileToTriples(testPath);

        String targetClass = triplesList.stream()
                .filter(f -> f.getPredicate().toString().equals("http://www.w3.org/ns/shacl#targetClass"))
                .findFirst().get().getObject().toString();

        List<Object> properties = getShPropertiesFromTriples(triplesList);

        Map<Node, List<Node>> inOrMapValues = new HashMap<>();
        triplesList.stream()
                .filter(Main::findInOr)
                .toList()
                .forEach(inOrTriple -> inOrMapValues.put(inOrTriple.getObject(),
                        findRecursiveInOr(triplesList, inOrTriple.getObject(), new ArrayList<>())));

        Map<Node, Map<Node, List<Node>>> map = new HashMap<>();
        properties.forEach(property -> {
            List<Triple> shPropertyTriples = listPropertiesAsSubjects(property, triplesList);
            for (int i = 0; i < shPropertyTriples.size(); i++) {
                if (inOrMapValues.containsKey(shPropertyTriples.get(i).getObject())) {
                    Triple replacedTriple = shPropertyTriples.get(i);
                    shPropertyTriples.remove(shPropertyTriples.get(i));
                    shPropertyTriples.add(new Triple(
                            replacedTriple.getSubject(),
                            replacedTriple.getPredicate(),
                            NodeFactory.createURI(inOrMapValues.get(replacedTriple.getObject()).toString().replace("[", "").replace("]", ""))
                    ));
                }
            }

            Map<Node, Map<Node, List<Node>>> tmp = createRestrictionMap(shPropertyTriples);
            Node key = NodeFactory.createURI(new ArrayList<>(tmp.keySet()).get(0).toString());
            map.put(key, tmp.get(key));
        });

        ShapeMapper mappedShape = new ShapeMapper(targetClass, map);

        System.out.println(mappedShape);

        rdfGenerator.createMockRdf(mappedShape).write(System.out, "TURTLE");
    }

    public static List<Triple> convertShaclFileToTriples(String filePath) {
        Model model = RDFDataMgr.loadModel(filePath);
        model.write(System.out, "TTL");
        System.out.println();

        Graph shapesGraph = RDFDataMgr.loadGraph(filePath);
        Shapes shapes = Shapes.parse(shapesGraph);
        return shapes.getGraph().find().toList();
    }

    public static List<Object> getShPropertiesFromTriples(List<Triple> triples) {
        return triples.stream()
                .filter(t -> t.getPredicate().toString().contains("property"))
                .map(Triple::getObject)
                .collect(Collectors.toList());
    }

    public static List<Triple> listPropertiesAsSubjects(Object property, List<Triple> triplesList) {
        return triplesList.stream()
                .filter(triple -> triple.getSubject().equals(property))
                .collect(Collectors.toList());
    }

    public static Map<Node, Map<Node, List<Node>>> createRestrictionMap(List<Triple> shProperties) {
        Map<Node, List<Node>> restrictions = new HashMap<>();
        AtomicReference<Node> path = new AtomicReference<>();

        shProperties.forEach(triple -> {
            if (!triple.getPredicate().toString().contains("path")) {
                if (restrictions.containsKey(triple.getPredicate()))
                    restrictions.get(triple.getPredicate()).add(triple.getObject());
                else {
                    List<Node> list = new ArrayList<>();
                    list.add(triple.getObject());
                    restrictions.put(triple.getPredicate(), list);
                }
            } else
                path.set(triple.getObject());
        });
        Map<Node, Map<Node, List<Node>>> map = new HashMap<>();
        map.put(path.get(), restrictions);
        return map;
    }

    public static boolean findInOr(Triple triple) {
        return triple.getPredicate().toString().equals("http://www.w3.org/ns/shacl#or") ||
                triple.getPredicate().toString().equals("http://www.w3.org/ns/shacl#in");
    }

    public static List<Node> findRecursiveInOr(List<Triple> tripleList, Node inOrObject, List<Node> result) {
        List<Triple> firstAndRest = tripleList.stream()
                .filter(triple -> triple.getSubject().equals(inOrObject))
                .toList();
        Triple first = firstAndRest.stream()
                .filter(triple -> triple.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"))
                .findFirst()
                .get();
        Triple last = firstAndRest.stream().
                filter(triple -> triple.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"))
                .findFirst()
                .get();
        result.add(first.getObject());
        if (last.getObject().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
            return result;
        else
            return findRecursiveInOr(tripleList, last.getObject(), result);
    }
}
