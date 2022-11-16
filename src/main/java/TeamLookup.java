import javax.print.attribute.standard.MediaSize;
import java.util.ArrayList;
import java.util.List;

public class TeamLookup {
    public static String NAQTtoDatabase(String NAQT) {
        char teamLevel;
        boolean hasExplicitLevel = true;
        switch (NAQT.charAt(NAQT.length() - 1)) {
            case 'A' -> teamLevel = 'A';
            case 'B' -> teamLevel = 'B';
            case 'C' -> teamLevel = 'C';
            case 'D' -> teamLevel = 'D';
            case 'E' -> teamLevel = 'E';
            case 'F' -> teamLevel = 'F';
            case 'G' -> teamLevel = 'G';
            case 'H' -> teamLevel = 'H';
            case 'I' -> teamLevel = 'I';
            default -> {
                teamLevel = 'A';
                hasExplicitLevel = false;
            }
        }
        String rawName;
        if(hasExplicitLevel) {
            rawName = NAQT.substring(0, NAQT.length() - 2);
        } else {
            rawName = NAQT;
        }

        if(badNames.contains(rawName)) {
            return convertBadName(rawName, teamLevel);
        }

        String returnable;
        if (hasExplicitLevel) {
            returnable = rawName + " High School - " + teamLevel;
        } else {
            returnable = NAQT + " High School - A";
        }

        return returnable;
    }

    private static List<String> badNames = new ArrayList<>();

    static {
        badNames.add("North St. Paul");
        badNames.add("Mounds Park");
        badNames.add("St. Croix Prep");
        badNames.add("Saint Agnes");
        badNames.add("Pine City");
        badNames.add("St. Paul Academy");
        badNames.add("Parnassus Prep");
        badNames.add("Holy Family");
        badNames.add("Providence");
        badNames.add("Breck");
    }

    private static String convertBadName(String badName, char teamLevel) {
        String returnable;
        switch (badName) {
            case "North St. Paul" -> returnable = "North High School";
            case "Mounds Park" -> returnable = "Mounds Park Academy";
            case "St. Croix Prep" -> returnable = "St. Croix Preparatory Academy";
            case "Saint Agnes" -> returnable = "Saint Agnes School";
            case "Pine City" -> returnable = "Pine City Junior/Senior High School";
            case "St. Paul Academy" -> returnable = "St. Paul Academy and Summit School";
            case "Parnassus Prep" -> returnable = "Parnassus Preparatory School";
            case "Holy Family" -> returnable = "Holy Family Catholic School";
            case "Providence" -> returnable = "Providence Academy";
            case "Breck" -> returnable = "Breck School";
            default -> throw new IllegalStateException("Unexpected value: " + badName);
        }

        return returnable + " - " + teamLevel;
    }

}
