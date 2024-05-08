import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

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
        parseDocument();
        printDataToFile("STARS.txt", myStars);
        printDataToFile("STAR_ERRORS.txt", inconsistentStars);

        try {
            insertStars();
        }
        catch (SQLException e) {
            e.printStackTrace();
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
                String id = "A" + count;
                System.out.println("inserting: " + s);

               if (s.getBirthYear().equals("")) {
                   without_year_statement.setString(1, id);
                   without_year_statement.setString(2, s.getName());
                   statement = without_year_statement;
               }
               else {
                   with_year_statement.setString(1, id);
                   with_year_statement.setString(2, s.getName());
                   with_year_statement.setString(3, s.getBirthYear());
                   statement = with_year_statement;
               }

               int rows_affected = statement.executeUpdate();
               count++;
            }
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

                //add it to the list
                if (consistent) {
                    myStars.add(tempStar);
                }
                else {
                    inconsistentStars.add(tempStar);
                }

            }
            else if (qName.equalsIgnoreCase("stagename")) {
                checkNameInconsistencies(tempVal);
                tempStar.setName(tempVal);
            }
            else if (qName.equalsIgnoreCase("dob")) {
                checkDOBInconsistencies(tempVal);
                tempStar.setBirthYear(tempVal);
            }
        }
    }

    public void checkNameInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempStar.setReason("Name is empty");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempStar.setReason("Name is too long");
        }
    }

    public void checkDOBInconsistencies(String tempVal) {
        if (!tempVal.equals("") && !tempVal.matches("\\d+")) {
            consistent = false;
            tempStar.setReason("Year contains non-numeric characters");
        }
    }
}