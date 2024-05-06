import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



// for mains243.xml
public class MovieParser extends DefaultHandler {
    ArrayList<String> oldGenres = new ArrayList<>();
    Map<String, String> newGenreMap = new HashMap<>();

    ArrayList<Movie> myMovies;
    private Movie tempMovie;
    private String tempVal;

    private DataSource dataSource;

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String JDBC_USER = "your_username";
    private static final String JDBC_PASSWORD = "your_password";

    public MovieParser() {
        // initialize genre map and myMovies arraylist to store movies
        myMovies = new ArrayList<Movie>();
        create_genre_map();

        // connect to moviedb
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        }
        catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void create_genre_map() {
        newGenreMap.put("Ctxx", "Uncategorized");
        newGenreMap.put("Actn", "Action");
        newGenreMap.put("Advt", "Adventure");
        newGenreMap.put("AvGa", "Avant Garde");
        newGenreMap.put("Camp", "Now - Camp");
        newGenreMap.put("Cart", "Cartoon");
        newGenreMap.put("CnR", "Cops and Robbers");
        newGenreMap.put("Comd", "Comedy");
        newGenreMap.put("Disa", "Disaster");
        newGenreMap.put("Docu", "Documentary");
        newGenreMap.put("Dram", "Drama");
        newGenreMap.put("Epic", "Epic");
        newGenreMap.put("Faml", "Family");
        newGenreMap.put("Hist", "History");
        newGenreMap.put("Horr", "Horror");
        newGenreMap.put("Musc", "Musical");
        newGenreMap.put("Myst", "Mystery");
        newGenreMap.put("Noir", "Black");
        newGenreMap.put("Porn", "Pornography");
        newGenreMap.put("Romt", "Romantic");
        newGenreMap.put("ScFi", "Sci-Fi");
        newGenreMap.put("Surl", "Sureal");
        newGenreMap.put("Susp", "Thriller");
        newGenreMap.put("West", "Western");
    }

    public void startParse() {
        parseDocument();
        populate_existing_genres();
        System.out.println(oldGenres);
        printDataToFile("movieresults.txt");
    }

    public void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("../stanford-movies/mains243.xml", this);

        } catch (SAXException se) {
            System.out.println("SAXException: ");
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            System.out.println("ParserConfigurationException: ");
            pce.printStackTrace();
        } catch (IOException ie) {
            System.out.println("IOException: ");
            ie.printStackTrace();
        }
    }

    public void populate_existing_genres() {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT name FROM genres;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                oldGenres.add(rs.getString("name"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printDataToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Movies '" + myMovies.size() + "'.\n");

            Iterator<Movie> it = myMovies.iterator();
            while (it.hasNext()) {
                writer.write(it.next().toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Event Handlers

    // startElement() is invoked when the parsing begins for an element
    // use it to construct List of Movies
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of employee
            tempMovie = new Movie();
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

        if (tempMovie != null) {
            if (qName.equalsIgnoreCase("film")) {
                //add it to the list
                myMovies.add(tempMovie);
            }
            else if (qName.equalsIgnoreCase("t")) {
                tempMovie.setTitle(tempVal);
            }
            else if (qName.equalsIgnoreCase("year")) {
                tempMovie.setYear(tempVal);
            }
            else if (qName.equalsIgnoreCase("cat")) {
                String full_cat = newGenreMap.get(tempVal);
                tempMovie.setGenres(full_cat);
            }
            else if (qName.equalsIgnoreCase("dirn")) {
                tempMovie.setDirector(tempVal);
            }
        }

    }

    public static void main(String[] args) {
        MovieParser my_parser = new MovieParser();
        my_parser.startParse();
    }

}