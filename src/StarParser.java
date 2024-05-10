import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



// to insert actors63.xml
public class StarParser extends DefaultHandler {
    ArrayList<Star> myStars = new ArrayList<Star>();
    ArrayList<Star> inconsistentStars = new ArrayList<>();;

    private Star tempStar;
    private String tempVal;

    boolean consistent = true;

    ArrayList<Star> existingStars = new ArrayList<Star>();
    ArrayList<Star> duplicates = new ArrayList<Star>();

    int max_star_id;

    Connection conn;

    public StarParser() {
        connectDatabase();
    }

    public static void main(String[] args) {
        StarParser my_parser = new StarParser();
        my_parser.startParse();
    }

    public void connectDatabase() {
        try {
            String dbtype = "mysql";
            String dbname = "moviedb";
            String username = "mytestuser";
            String password = "My6$Password";

            // Incorporate mySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the test database
            conn = DriverManager.getConnection("jdbc:" + dbtype + ":///" + dbname + "?autoReconnect=true&useSSL=false",
                                                username, password);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startParse() {
        long start = 0;
        long end = 0;

        try {
            start = System.currentTimeMillis();
            getExistingStars();
            parseDocument();

            insertStars();
            end = System.currentTimeMillis();

            printDataToFile("STARS.txt", myStars);
            printDataToFile("STAR_ERRORS.txt", inconsistentStars);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        double total_time = (end - start)/1000.0;
        System.out.println("Time in Seconds for Movie Parser: " + total_time);

        int count = 0;
        for (Star s: existingStars) {
            // System.out.println(s);
            count++;
        }
        System.out.println("Number of existing stars: " + count);

        System.out.println("max star id: " + max_star_id);
    }

    public void getExistingStars() throws SQLException {
        if (conn != null) {
            String query = "SELECT id, name, birthYear FROM stars;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String star_id = rs.getString("id");
                String star_name = rs.getString("name");
                String star_birthYear = rs.getString("birthYear");

                Star new_star = new Star(star_id, star_name, star_birthYear);

                existingStars.add(new_star);
            }

            query = "SELECT max(id) as max_star_id FROM stars;";
            statement = conn.prepareStatement(query);
            rs = statement.executeQuery();
            while (rs.next()) {
                String max_id = rs.getString("max_star_id");
                max_star_id = Integer.parseInt(max_id.substring(2));
            }
        }
    }

    public void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("./stanford-movies/actors63.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void printDataToFile(String fileName, ArrayList<Star> data) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Number of Entries '" + data.size() + "'.\n");

            for (Star myStar : data) {
                writer.write(myStar.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertStars() throws SQLException {
        if (conn != null) {
            String with_year_query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?);";
            String without_year_query = "INSERT INTO stars (id, name) VALUES (?, ?);";

            PreparedStatement with_year_statement = conn.prepareStatement(with_year_query);
            PreparedStatement without_year_statement = conn.prepareStatement(without_year_query);
            PreparedStatement statement;

            int count = 1;

            for (Star s : myStars) {
                max_star_id++;
                String star_id = "nm" + max_star_id;

                s.setStarId(star_id);

               if (s.getBirthYear().equals("")) {
                   without_year_statement.setString(1, star_id);
                   without_year_statement.setString(2, s.getName());
                   statement = without_year_statement;
               }
               else {
                   with_year_statement.setString(1, star_id);
                   with_year_statement.setString(2, s.getName());
                   with_year_statement.setString(3, s.getBirthYear());
                   statement = with_year_statement;
               }

               int rows_affected = statement.executeUpdate();
               count++;
            }
            System.out.println("Inserted " + count + " rows");
            System.out.println("max id now " + max_star_id);
        }
    }


    //Event Handlers

    // startElement() is invoked when the parsing begins for an element
    // use it to construct List of Movies
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of employee
            tempStar = new Star();
            consistent = true;
        }
    }

    // characters(char[], int, int) receives characters with boundaries.
    // We’ll convert them to a String and store it in a variable
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    // endElement() is invoked when the parsing ends for an element
    // this is when we’ll assign the content of the tags to their respective variables
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tempVal = tempVal.trim();

        if (tempStar != null) {
            if (qName.equalsIgnoreCase("actor")) {
                checkStarInconsistencies(tempStar);

                //add it to the list
                if (consistent) {
                    if (tempStar.getName().equals("John Cassavetes")) {

                        System.out.println("in my stars: " + tempStar);
                    }
                    myStars.add(tempStar);
                }
                else {
                    if (tempStar.getName().equals("John Cassavetes")) {
                        System.out.println("inconsistent: " + tempStar);
                    }
                    inconsistentStars.add(tempStar);
                }

            }
            else if (qName.equalsIgnoreCase("stagename")) {
                checkNameInconsistencies();
                tempStar.setName(tempVal);
            }
            else if (qName.equalsIgnoreCase("dob")) {
                checkDOBInconsistencies();
                tempStar.setBirthYear(tempVal);
            }
        }
    }

    public void checkStarInconsistencies(Star tempStar) {
        if (existingStars.contains(tempStar)) {
            consistent = false;
            tempStar.setReason("Duplicate: Star exists in database already");
        }
        else if (myStars.contains(tempStar)) {
            consistent = false;
            tempStar.setReason("Duplicate: Star parsed already");
        }
    }

    public void checkNameInconsistencies() {
        if (tempVal.equals("")) {
            consistent = false;
            tempStar.setReason("Name is empty");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempStar.setReason("Name is too long");
        }
    }

    public void checkDOBInconsistencies() {
        if (!tempVal.equals("") && !tempVal.matches("\\d+")) {
//            consistent = false;
//            tempStar.setReason("Year contains non-numeric characters.");
            tempVal = "";
        }
    }
}