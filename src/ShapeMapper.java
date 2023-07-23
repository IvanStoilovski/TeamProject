
//import com.github.jsonldjava.core.RDFDataset;

import org.apache.jena.graph.Node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ShapeMapper {

    private String targetClass;
    Map<Object,Map<Node, List<Node>>> restrictionsMap;

    public ShapeMapper(String targetClass,Map<Object,Map<Node, List<Node>>> restrictionsMap)
    {
        this.targetClass=targetClass;
        this.restrictionsMap=restrictionsMap;
    }
    public String printMap()
    {
        StringBuilder sb= new StringBuilder();
        restrictionsMap.forEach((k,v)->{
            sb.append("key " + k + " ===>");
            AtomicInteger keyCounter= new AtomicInteger(1);
            v.forEach((k1,v1)->{
                sb.append(" keyInside"+ keyCounter.get() + " " + k1 + " ---> ");
                sb.append("valueInside"+ keyCounter.getAndIncrement() + " " + v1 + "  ||  ");
            });
            sb.append("\n");
        });
        return sb.toString();
    }
    @Override
    public String toString() {
        return "This is the mapped shape: " +"\n"+
                "The target class is: " + targetClass + "\n" +
                "The restrictions are mapped as follows: "+"\n"
                + printMap();
    }
}
