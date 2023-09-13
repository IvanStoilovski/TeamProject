import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DistinguishRestrictions {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    public static final String checkForDatatype = "http://www.w3.org/ns/shacl#datatype";
    public static final String checkForClass = "http://www.w3.org/ns/shacl#class";
    public static final String checkForIn = "http://www.w3.org/ns/shacl#in";
    public static final String checkForOr = "http://www.w3.org/ns/shacl#or";
    public static final String checkForNodeKind = "http://www.w3.org/ns/shacl#nodeKind";
    public static final String checkForMinLength = "http://www.w3.org/ns/shacl#minLength";
    public static final String checkForMaxLength = "http://www.w3.org/ns/shacl#maxLength";
    public static final String checkFOrQualifiedShape = "http://www.w3.org/ns/shacl#qualifiedValueShape";

    public static Node analyzePathRestrictions(Map<Node, List<Node>> pathRestrictions, Map<String, String> prefixMap) {
        for (Node key : pathRestrictions.keySet()) {
            if (key.toString().equals(checkForIn) || key.toString().equals(checkForOr)) {
                Node node = pathRestrictions.get(key).get(0);
                String[] values = node.toString().split(", ");
                int size = values.length;
                boolean isLiteral=true;
                String value = values[secureRandom.nextInt(size)];
                if(value.contains("#")) {
                    isLiteral = false;
                    //value = value.split("#")[1];
                }
                else {
                    value = value.substring(1, value.length() - 1);
                }
                if(isLiteral) {
                    return NodeFactory.createLiteral(value);
                }
                else
                {
                    String [] inOrValue = value.split("#");
                    String uri = prefixMap.get(inOrValue[0]) + ":" +inOrValue[1];
                    return NodeFactory.createURI(uri);
                }
            } else if (key.toString().equals(checkForDatatype)) {
                String[] parts = pathRestrictions.get(key).get(0).toString().split("#");
                switch (parts[1]) {
                    case "string":
                        int maxLength = 10;
                        int minLength = 0;
                        List<String> restrictionKeysAsString = pathRestrictions.keySet().stream().map(Node::toString).collect(Collectors.toList());
                        if (restrictionKeysAsString.contains(checkForMaxLength)) {
                            maxLength = getMaxLength(pathRestrictions);
                        }
                        if (restrictionKeysAsString.contains(checkForMinLength)) {
                            minLength = getMinLength(pathRestrictions);
                            if (maxLength < minLength) {
                                maxLength += 10;
                            }
                        }
                        return NodeFactory.createLiteral(generateRandomString(minLength, maxLength));

                    case "integer":
                        return getRandomNumber();
                }
            } else if (key.toString().equals(checkForClass)) {
                String element = pathRestrictions.get(key).toString().split("#")[0].substring(1);
                String uri = prefixMap.get(element) + ":" + generateRandomString(0,10);
                return NodeFactory.createURI(uri);

            }
        }
        return null;
    }

    public static String generateRandomString(int minLength, int maxLength) {
        StringBuilder sb = new StringBuilder();
        if (minLength == maxLength)
        {
            for (int i = 0; i < maxLength; i++) {
                int randomIndex = secureRandom.nextInt(CHARACTERS.length());
                char randomChar = CHARACTERS.charAt(randomIndex);
                sb.append(randomChar);
            }
        }
        else {
            for (int i = minLength; i < maxLength; i++) {
                int randomIndex = secureRandom.nextInt(CHARACTERS.length());
                char randomChar = CHARACTERS.charAt(randomIndex);
                sb.append(randomChar);
            }
        }
        return sb.toString();
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

    private static Integer getMaxLength(Map<Node, List<Node>> pathRestrictions) {
        List<Node> restrictionTypes = pathRestrictions.keySet().stream().toList();
        Optional<Node> max = restrictionTypes.stream()
                .filter(restrictionType -> restrictionType.toString().contains(checkForMaxLength))
                .findFirst();
        if (max.isPresent()) {
            return Integer.parseInt(
                    max.map(node -> pathRestrictions.get(node)
                                    .get(0)
                                    .toString()
                                    .split("\\^\\^")[0]
                                    .replace("\"", ""))
                            .get());
        }
        return null;
    }

    private static Integer getMinLength(Map<Node, List<Node>> pathRestrictions) {
        List<Node> restrictionTypes = pathRestrictions.keySet().stream().toList();
        Optional<Node> min = restrictionTypes.stream()
                .filter(restrictionType -> restrictionType.toString().contains(checkForMinLength))
                .findFirst();
        if (min.isPresent()) {
            return Integer.parseInt(
                    min.map(node -> pathRestrictions
                                    .get(node)
                                    .get(0)
                                    .toString()
                                    .split("\\^\\^")[0]
                                    .replace("\"", ""))
                            .get());
        }
        return null;
    }

}
