import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import static java.lang.Double.POSITIVE_INFINITY;

public class Main {
    static WebElement element;
    static Path outputPath;


    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Lucas\\IdeaProjects\\NaqtScraper\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

        List<String[]> gameOutputs = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter link to any round of tournament:");

        //Link to tournament round page. Any round is acceptable
        //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
        while(!scanner.hasNext()) {

        }

        String link = scanner.nextLine();
        //String link = "https://www.naqt.com/stats/tournament/round.jsp?tournament_id=13983&round=1";
        String usableLink = link.substring(0, link.length() - 1);

        System.out.println("Please enter tournament name to use:");

        //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
        while(!scanner.hasNext()) {

        }

        parseType tournamentStyle;

        if (usableLink.contains("naqt")) {
            tournamentStyle = parseType.NAQTUnknown;
        } else {
            tournamentStyle = parseType.QBDatabase;
        }

        String tournament = scanner.nextLine();

        String timestamp = String.valueOf(Time.from(Instant.now()));
        timestamp = timestamp.replaceAll(" ", "_");
        timestamp = timestamp.replaceAll(":", "");
        System.out.println(timestamp);
        outputPath = Path.of("C:\\Users\\Public\\Downloads\\" + tournament + "_Gen_At_" + timestamp + ".csv");


        System.out.println("Please enter tournament start time & date. Format like MM/DD/YY HH:MM AM/PM, or type \"Multi\" for a multi day tournament:");

        //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
        while(!scanner.hasNext()) {

        }

        String startTime = scanner.next();
        boolean singleDay;
        //Change these to ArrayLists

        ArrayList<String> extractedTime = new ArrayList<>();
        ArrayList<String> extractedDate = new ArrayList<>();
        ArrayList<String> extractedAMPM = new ArrayList<>();
        ArrayList<Object> roundDayInterval = new ArrayList<>();
        if (startTime.contains("Multi")) {
            singleDay = false;
            System.out.println("Please enter number of tournament days:");
            // noinspection StatementWithEmptyBody
            while(!scanner.hasNextInt()) {

            }
            int numDays = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < numDays; i++) {
                System.out.println("Please enter start time & date for day " + (i + 1) + " (MM/DD/YY HH:MM AM/PM):");
                //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
                while(!scanner.hasNext()) {

                }
                startTime = scanner.nextLine();
                extractedTime.add(startTime.substring(9, 14));
                extractedDate.add(startTime.substring(0, 8));
                extractedAMPM.add(startTime.substring(15, 17));
                System.out.println("Please enter round interval for day " + (i + 1) + "(inclusive, ex: 1-3):");

                // noinspection StatementWithEmptyBody
                while(!scanner.hasNextLine()) {

                }
                String[] roundInterval = scanner.nextLine().split("-");
                roundDayInterval.add(Integer.parseInt(roundInterval[0]));
                roundDayInterval.add(Integer.parseInt(roundInterval[1]));
            }
        } else {
            singleDay = true;
            extractedTime.add(startTime.substring(9, 14));
            extractedDate.add(startTime.substring(0, 8));
            extractedAMPM.add(startTime.substring(15, 17));
            roundDayInterval.add(1);
            roundDayInterval.add(POSITIVE_INFINITY);
        }

/*
        System.out.println(outputPath.getFileName());
        System.out.println(extractedTime);
        System.out.println(extractedDate);
        System.out.println(extractedAMPM);
*/

        WebDriver driver = new ChromeDriver();
        driver.get(link);

        String curRoundTime;

        int divisionCount = 1;
        if(tournamentStyle != parseType.QBDatabase) {
            String scoringRules = null;
            List<WebElement> tournamentInfo = driver.findElements(By.xpath("/html/body/div/section[1]/div/table/tbody/tr"));
            for (int i = 0; i < tournamentInfo.size(); i++) {
                element = driver.findElement(By.xpath("/html/body/div/section[1]/div/table/tbody/tr[" + (i + 1) + "]/td[1]"));
                String curInfo = element.getText();
                if (curInfo.contains("Scoring")) {
                    scoringRules = driver.findElement(By.xpath("/html/body/div/section[1]/div/table/tbody/tr[" + (i + 1) + "]/th")).getText();
                    break;
                }
            }
            assert scoringRules != null;
            if(scoringRules.contains("TOMCAT")) {
                tournamentStyle = parseType.NAQTTomcat;
            } else if (scoringRules.contains("Tossups with Powers")) {
                tournamentStyle = parseType.NAQTTossPowerNoBonus;
            } else {
                List<WebElement> divisions = driver.findElements(By.xpath("/html/body/div/section[5]/section"));
                divisionCount = divisions.size();
                if (divisionCount > 1) {
                    System.out.println("Multiple divisions detected. Do you want to log all divisions or just the first one? (A/O)");
                    //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
                    while (!scanner.hasNext()) {

                    }
                    String divisionChoice = scanner.nextLine();
                   if (divisionChoice.equals("A")) {
                        tournamentStyle = parseType.NAQTMultiALL;
                    } else if (divisionChoice.equals("O")) {
                       tournamentStyle = parseType.NAQTStandard;
                    } else {
                        System.out.println("Invalid input. Defaulting to logging first division only.");
                        tournamentStyle = parseType.NAQTStandard;
                    }
                } else {
                    tournamentStyle = parseType.NAQTStandard;
                }
            }
        }

        List<WebElement> rounds = driver.findElements(By.xpath("/html/body/div/section[3]/nav/a"));
        int roundsToLoad = rounds.size(), roundCur;
        System.out.println(tournamentStyle);
        System.out.println("Division: 1");
        for (int i = 0; i < roundsToLoad; i++) {
            roundCur = i + 1;
            int interval = 0;
            //Determine current round's day and time
            if (singleDay) {
                curRoundTime = addTime(extractedTime.get(interval), extractedAMPM.get(interval), 30 * i);
            } else {
                for (int j = 0; j < roundDayInterval.size(); j += 2) {
                    if (roundCur >= (int) roundDayInterval.get(j) && roundCur <= (int) roundDayInterval.get(j + 1)) {
                        interval = j;
                        break;
                    }
                }
                curRoundTime = addTime(extractedTime.get(interval), extractedAMPM.get(interval), 30 * ((i - 1) - (int) roundDayInterval.get(clamp((interval * 2) - 1, 0, roundDayInterval.size() - 1))));
            }
            System.out.println("Current round time: " + curRoundTime);



            driver.get(usableLink + roundCur);

            System.out.println("Round: " + roundCur);

            List<WebElement> games = driver.findElements(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr"));
            int gamesToLoad = games.size();

            //System.out.println(gamesToLoad);
            //Start at 1 because XMLPath starts at 1 for some reason
            for (int j = 1; j <= gamesToLoad; j++) {
                String team1, team2;
                String score1, score2;
                switch (tournamentStyle){
                    case NAQTStandard, NAQTMultiALL -> {
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[1]/a"));
                        team1 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[1]"));
                        score1 = element.getText();

                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[2]/a"));
                        team2 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[7]"));
                        score2 = element.getText();
                    }
                    case NAQTTossPowerNoBonus -> {
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[1]/a"));
                        team1 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[1]"));
                        score1 = element.getText();

                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[2]/a"));
                        team2 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[4]"));
                        score2 = element.getText();
                    }
                    case NAQTTomcat -> {
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[1]/a"));
                        team1 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[1]"));
                        score1 = element.getText();

                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/th[2]/a"));
                        team2 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[5]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + j + "]/td[3]"));
                        score2 = element.getText();
                    }
                    default -> throw new IllegalStateException(tournamentStyle + " is not a currently supported tournament style");
                }

                if(team1.contains("Bye") || team2.contains("Bye") || score1.contains("Forfeit") || score2.contains("Forfeit")) {
                    continue;
                }

                team1 = TeamLookup.NAQTtoDatabase(team1);
                team2 = TeamLookup.NAQTtoDatabase(team2);

                System.out.println("Winning Team: " + team1 + "; Score: " + score1 + " | Losing Team: " + team2 + "; Score: " + score2);
                String[] csvRow = {extractedDate.get(interval) + " " + curRoundTime, tournament, team1, team2, "1", "0"};

                gameOutputs.add(csvRow);
            }

            System.out.println();
        }
        //If the tournament is a multi-division tournament, we need to load the rest of the divisions
        if(tournamentStyle == parseType.NAQTMultiALL) {
            for (int j = 2; j <= divisionCount; j++) {
                System.out.println("Division: " + j);
                rounds = driver.findElements(By.xpath("/html/body/div/section["+ (j + 3) + "]/nav/a"));
                roundsToLoad = rounds.size();
                for (int i = 0; i < roundsToLoad; i++) {
                    roundCur = i + 1;
//
//                    curRoundTime = addTime(extractedTime, extractedAMPM, 30 * i);
//
//                    driver.get(usableLink + roundCur);
//
//                    System.out.println("Round: " + roundCur);
                    roundCur = i + 1;
                    int interval = 0;
                    //Determine current round's day and time
                    if (singleDay) {
                        curRoundTime = addTime(extractedTime.get(interval), extractedAMPM.get(interval), 30 * i);
                    } else {
                        for (int k = 0; j < roundDayInterval.size(); j += 2) {
                            if (roundCur >= (int) roundDayInterval.get(j) && roundCur <= (int) roundDayInterval.get(k + 1)) {
                                interval = k;
                                break;
                            }
                        }
                        curRoundTime = addTime(extractedTime.get(interval), extractedAMPM.get(interval), 30 * (i - (int) roundDayInterval.get(clamp((2 * interval), 0))));
                    }

                    List<WebElement> games = driver.findElements(By.xpath("/html/body/div/section[" + (j + 3) + "]/section[1]/div/div[2]/div[2]/table/tbody/tr"));
                    int gamesToLoad = games.size();

                    //System.out.println(gamesToLoad);


                    //Start at 1 because XMLPath starts at 1 for some reason
                    for (int k = 1; k <= gamesToLoad; k++) {
                        String team1, team2;
                        String score1, score2;
                        element = driver.findElement(By.xpath("/html/body/div/section[" + (j + 3) + "]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + k + "]/th[1]/a"));
                        team1 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[" + (j + 3) + "]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + k + "]/td[1]"));
                        score1 = element.getText();

                        element = driver.findElement(By.xpath("/html/body/div/section[" + (j + 3) + "]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + k + "]/th[2]/a"));
                        team2 = element.getText();
                        element = driver.findElement(By.xpath("/html/body/div/section[" + (j + 3) + "]/section[1]/div/div[2]/div[2]/table/tbody/tr[" + k + "]/td[7]"));
                        score2 = element.getText();
                        team1 = TeamLookup.NAQTtoDatabase(team1);
                        team2 = TeamLookup.NAQTtoDatabase(team2);

                        if(team1.contains("Bye") || team2.contains("Bye") || score1.contains("Forfeit") || score2.contains("Forfeit")) {
                            continue;
                        }

                        System.out.println("Winning Team: " + team1 + "; Score: " + score1 + " | Losing Team: " + team2 + "; Score: " + score2);
                        String[] csvRow = {extractedDate + " " + curRoundTime, tournament, team1, team2, "1", "0"};

                        gameOutputs.add(csvRow);
                    }

                    System.out.println();
                }
            }

        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath.toString()))) {
            writer.writeAll(gameOutputs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        driver.close();
    }

    /**
     *
     * @param input Input in HH:MM
     * @param minAdd Minutes to add
     * @return Returns the time in HH:MM
     */
    public static String addTime(String input, String ampm, int minAdd) {
        int hour = Integer.parseInt(input.substring(0,2));
        int minute = Integer.parseInt(input.substring(3, 5));

        minute += minAdd;
        while (minute >= 60) {
            minute -= 60;
            hour++;
        }

        String outputAMPM = ampm;

        if(hour > 12) {
            hour -= 12;
            if (outputAMPM.equals("PM")) {
                outputAMPM = "AM";
            } else {
                outputAMPM = "PM";
            }

            while (hour > 12) {
                hour -= 12;
                if (outputAMPM.equals("PM")) {
                    outputAMPM = "AM";
                } else {
                    outputAMPM = "PM";
                }
            }

        } else if (hour == 12) {
            if (outputAMPM == "PM") {
                outputAMPM = "AM";
            } else {
                outputAMPM = "PM";
            }
        }



        String outputHour;
        if (hour < 10) {
            outputHour = "0" + hour;
        } else {
            outputHour = String.valueOf(hour);
        }

        String outputMin;
        if (minute < 10) {
            outputMin = "0" + minute;
        } else {
            outputMin = String.valueOf(minute);
        }
        return outputHour + ":" + outputMin + " " + outputAMPM;
    }

    public enum parseType {
        QBDatabase,
        NAQTStandard,
        NAQTMultiALL,
        NAQTUnknown,
        NAQTTomcat,
        NAQTTossPowerNoBonus
    }

    //Clamp the value between min and max
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    //Clamp the value above the min
    public static int clamp(int value, int min) {
        return Math.max(min, value);
    }
}