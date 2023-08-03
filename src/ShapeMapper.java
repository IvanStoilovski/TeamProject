import org.apache.jena.graph.Node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ShapeMapper {

    private final String targetClass;
    Map<Node, Map<Node, List<Node>>> restrictionsMap;

    public ShapeMapper(String targetClass, Map<Node, Map<Node, List<Node>>> restrictionsMap) {
        this.targetClass = targetClass;
        this.restrictionsMap = restrictionsMap;
    }

    public String printMap() {
        StringBuilder sb = new StringBuilder();
        restrictionsMap.forEach((k, v) -> {
            sb.append("key ").append(k).append(" ===>");
            AtomicInteger keyCounter = new AtomicInteger(1);
            v.forEach((k1, v1) -> {
                sb.append(" keyInside").append(keyCounter.get()).append(" ").append(k1).append(" ---> ");
                sb.append("valueInside").append(keyCounter.getAndIncrement()).append(" ").append(v1).append("  ||  ");
            });
            sb.append("\n");
        });
        return sb.toString();
    }

    @Override
    public String toString() {
        return "This is the mapped shape: " + "\n" +
                "The target class is: " + targetClass + "\n" +
                "The restrictions are mapped as follows: " + "\n"
                + printMap();
    }

    public String getTargetClass() {
        return targetClass;
    }

    public Map<Node, Map<Node, List<Node>>> getRestrictionsMap() {
        return restrictionsMap;
    }
}
