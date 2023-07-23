import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;



import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    static final String testPath="C:\\Users\\ivans\\OneDrive\\Desktop\\timski\\timski\\shacl_files\\shaclStart.ttl";
    static final String testPath2="C:\\Users\\ivans\\OneDrive\\Desktop\\test2.ttl";
    public static void main(String[] args) throws FileNotFoundException {

        List<Triple> triplesList=convertShaclFileToTriples(testPath);
        String targetClass=triplesList.stream()
                .filter(f->f.getPredicate().toString().equals("http://www.w3.org/ns/shacl#targetClass"))
                .findFirst().get().getObject().toString();
        System.out.println("Printing triples");
        triplesList.forEach(triple -> {
            System.out.print(triple.getSubject()+" ---> ");
            System.out.print(triple.getPredicate()+" ---> ");
            System.out.println(triple.getObject()+" ---> ");});
        System.out.println();

        List<Object> properties=getShPropertiesFromTriples(triplesList);

        Map<Object,Map<Node,List<Node>>> map=new HashMap<>();
        // tuka gi sobirame site mapi koi gi vadime vo sluchaj da ima in ili or -------------------------------------
//        Map<Node,Map<Node,List<Node>>> tempMapForOr_In=new HashMap<>();
//        triplesList.stream().filter(f->najdiDaliImaInIliOr(f)).collect(Collectors.toList())
//                    .forEach(k->{
//                        tempMapForOr_In.put(k.getObject(),najdiPrv(triplesList,k.getObject(),k.getPredicate()));
//                        });
        //do tuka------------------------------------------------------------
        properties.forEach(property->{

            List<Triple> shPropertyTriples=listPropertiesAsSubjects(property,triplesList);

            Map<Object, Map<Node, List<Node>>> tmp=createRestrictionMap(shPropertyTriples);

            Object key=new ArrayList<Object>(tmp.keySet()).get(0);
            map.put(key,tmp.get(key));
            map.entrySet().stream().forEach((k)->
            {
            });
        });
        ShapeMapper mappedShape = new ShapeMapper(targetClass, map);

        System.out.println(mappedShape);

//        System.out.println("priting map\n");
//        map.forEach((k,v)->{
//            System.out.print("key " + k + " --->");
//            v.forEach((k1,v1)->{
//                System.out.print(" key1 " + k1 + " --->");
//                System.out.print("value1 " + v1 + "    ");
//            });
//            System.out.println();
//        });
    }

    public static List<Triple> convertShaclFileToTriples(String filePath){
        Model model = RDFDataMgr.loadModel(filePath);
        model.write(System.out, "TTL");

        Graph shapesGraph = RDFDataMgr.loadGraph(filePath);
        Shapes shapes = Shapes.parse(shapesGraph);
        return shapes.getGraph().find().toList();
    }

    public static List<Object> getShPropertiesFromTriples(List<Triple> triples){
        return triples.stream()
                .filter(t->t.getPredicate().toString().contains("property"))
                .map((Function<Triple, Object>) Triple::getObject)
                .collect(Collectors.toList());
    }

    public static List<Triple> listPropertiesAsSubjects(Object property,List<Triple> triplesList){
        return triplesList.stream()
                .filter(triple -> triple.getSubject().equals(property))
                .collect(Collectors.toList());
    }

    public static Map<Object,Map<Node,List<Node>>> createRestrictionMap(List<Triple> shProperties){
        Map<Node,List<Node>> restrictions=new HashMap<>();
        AtomicReference<Node> path=new AtomicReference<>();

        shProperties.forEach(triple -> {
            if(!triple.getPredicate().toString().contains("path")){
                if(restrictions.containsKey(triple.getPredicate()))
                    restrictions.get(triple.getPredicate()).add(triple.getObject());
                else {
                    List<Node> list=new ArrayList<>();
                    list.add(triple.getObject());
                    restrictions.put(triple.getPredicate(), list);
                }
            }
            else
                path.set(triple.getObject());
        });
        Map<Object, Map<Node, List<Node>>> map=new HashMap<>();
        map.put(path,restrictions);
        return map;
    }

    public static boolean najdiDaliImaInIliOr(Triple triple)
    {
        if(triple.getPredicate().toString().equals("http://www.w3.org/ns/shacl#or")||
                triple.getPredicate().toString().equals("http://www.w3.org/ns/shacl#in"))
            return true;
        return false;
    }
    public static Map<Node,List<Node>> najdiPrv(List<Triple> triplesList, Node kraj,Node literalKey)
    {

        Map<Node,List<Node>> mapa=new HashMap<>();
        List<List<Node>> krajna=new ArrayList<>();
        najdivoInIliOr(triplesList, kraj,krajna);
        //-----------------------tuka gi stava vo mapa ako in/or ako ne se so literali---------------
        if(krajna.get(0).size()>1) {
            krajna.forEach(e -> {
                if (mapa.containsKey(e.get(0)))
                    mapa.get(e.get(0)).add(e.get(1));
                else mapa.put(e.get(0), new ArrayList<>(List.of(e.get(1))));
            });
            //-----------------------------------do tuka----------------
        }
        else
        //-----------------------tuka gi stava vo mapa ako in/or ako se so literali---------------
        {
            mapa.put(literalKey,krajna.stream().map(e->e.get(0)).collect(Collectors.toList()));
        }
        //-----------------------------------do tuka----------------

        System.out.println("priting map from in or or \n");
        mapa.forEach((k,v)->{
            System.out.print("key " + k + " --->");
            System.out.print("value  " + v + " --->");
            System.out.println();
        });
        return mapa;
    }
    public static void najdivoInIliOr(List<Triple> triplesList,Node node,List<List<Node>> vekjeNajdeni)
    {
        triplesList.forEach(f->{
            if(f.getSubject().equals(node))
            {
                if(f.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")) {
                    if(!f.getObject().isBlank())
                        vekjeNajdeni.add(new ArrayList<>(List.of(f.getObject())));
                    else {
                        vekjeNajdeni.addAll(triplesList.stream()
                                .filter(t->t.getSubject().equals(f.getObject()))
                                .map(x->new ArrayList<>(List.of(x.getPredicate(),x.getObject())))
                                .collect(Collectors.toList()));
                    }
                }
                else
                {
                    if(f.getObject().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
                        return;
                    najdivoInIliOr(triplesList,f.getObject(),vekjeNajdeni);
                }
            }
        });
    }
//    public static Map<Object,Map<Node,List<Node>>> joinMaps(Map<Object,Map<Node,List<Node>>> mainMap,
//                                                            Map<Node,Map<Node,List<Node>>> map2)
//    {
//       List<Map<Node,List<Node>>> tempmap = new ArrayList<>(mainMap.values());
//       tempmap.forEach(f->{
//           //Node_URI inNode=returnUri(new Node_Variable("http://www.w3.org/ns/shacl#in"));
//           //Node orNode=new Node_Variable("http://www.w3.org/ns/shacl#or");
//
//           System.out.println(f.containsKey(inNode)+" "+f.get(inNode).get(0).isBlank());
//
//           if(f.containsKey(inNode) && f.get(inNode).get(0).isBlank())
//           {
//               Node keyNode=f.keySet().stream().filter(k->k.getName().equals("http://www.w3.org/ns/shacl#in")).findFirst().get();
//               f.replace(keyNode,map2.get(f.get(keyNode)).get(inNode));
//           }
//           //System.out.println(f.containsKey("http://www.w3.org/ns/shacl#in")+" "+f.get("http://www.w3.org/ns/shacl#in").get(0).isBlank());
//           if(f.containsKey("http://www.w3.org/ns/shacl#or") && f.get("http://www.w3.org/ns/shacl#or").get(0).isBlank())
//           {
//               Node keyNode=f.keySet().stream().filter(k->k.getName().equals("http://www.w3.org/ns/shacl#or")).findFirst().get();
//               f.replace(keyNode,map2.get(f.get(keyNode)).get("http://www.w3.org/ns/shacl#or"));
//           }
//       });
//
//
////        return mainMap.values().stream().collect(Collectors.toMap(f->{
////          if( f.containsKey("http://www.w3.org/ns/shacl#in"))
////          {
////           return f.replace(f.keySet().stream().filter(k->k.getName().equals("http://www.w3.org/ns/shacl#in")).findFirst().get(),
////                   map2.get(f.get("http://www.w3.org/ns/shacl#in")).get("http://www.w3.org/ns/shacl#in"));
////          }
////          return f;
////        }
////        ));
////        System.out.println("Printing the replaced map------------------------------");
////        tempmap.forEach((k,v)->{
////            System.out.print("key " + k + " --->");
////            System.out.print("value  " + v + " --->");
////            System.out.println();
////        });
//     return mainMap;
//    }

}
