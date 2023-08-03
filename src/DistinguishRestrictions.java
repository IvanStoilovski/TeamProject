import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DistinguishRestrictions {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    public static final String checkForDatatype = "http://www.w3.org/ns/shacl#datatype";
    public static final String checkForClass = "http://www.w3.org/ns/shacl#class";
    public static final String checkForIn = "http://www.w3.org/ns/shacl#in";
    public static final String checkForOr = "http://www.w3.org/ns/shacl#or";

    public static Node analyzePathRestrictions(Map<Node, List<Node>> pathRestrictions, Map<String, String> prefixMap) {
        for (Node key : pathRestrictions.keySet()) {
            if (key.toString().equals(checkForIn) || key.toString().equals(checkForOr)) {
                Node node = pathRestrictions.get(key).get(0);
                String[] values = node.toString().split(", ");
                int size = values.length;
                String value = values[secureRandom.nextInt(size)];
                value = value.split("#")[1];
                return NodeFactory.createLiteral(value);
            } else if (key.toString().equals(checkForDatatype)) {
                String[] parts = pathRestrictions.get(key).get(0).toString().split("#");
                switch (parts[1]) {
                    case "string":
                        return generateRandomString();
                    case "integer":
                        return getRandomNumber();
                }
            } else if (key.toString().equals(checkForClass)) {
                String element = pathRestrictions.get(key).toString().split("#")[0].substring(1);
                String uri = prefixMap.get(element) + ":" + generateRandomString();
                return NodeFactory.createURI(uri.replaceAll("\"", ""));
            }
        }
        return null;
    }

    public static Node generateRandomString() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return NodeFactory.createLiteral(sb.toString().replaceAll("\"", ""));
    }

    public static LocalDate getRandomDate() {
        // Define the range of dates
        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);
        return startDate.plusDays(randomDays);
    }

    public static Node getRandomNumber() {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        Integer number = ThreadLocalRandom.current().nextInt(min, max + 1);
        return NodeFactory.createLiteral(Integer.toString(number), XSDDatatype.XSDinteger);
    }


    public static Boolean getRandomBoolean() throws NoSuchAlgorithmException {
        return SecureRandom.getInstanceStrong().nextBoolean();
    }

}
