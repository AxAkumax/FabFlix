import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
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
    ArrayList<Movie> inconsistentMovies;

    private Movie tempMovie;
    private String tempVal;

    // 0 means the entry is consistent
    // 1 means the entry is inconsistent
    int flag = 0;

    private DataSource dataSource;


    public MovieParser() {
        // initialize genre map and myMovies arraylist to store movies
        myMovies = new ArrayList<Movie>();
        inconsistentMovies = new ArrayList<Movie>();
        create_genre_map();
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
//        populate_existing_genres();
//        System.out.println(oldGenres);
        printDataToFile("MOVIES.txt");
        printInconsistenciesToFile("MOVIE_ERRORS.txt");
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

        try (Connection conn = dataSource.getConnection()){
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

    private void printInconsistenciesToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Movies '" + inconsistentMovies.size() + "'.\n");

            Iterator<Movie> it = inconsistentMovies.iterator();
            while (it.hasNext()) {
                Movie m = it.next();
                writer.write(m.getReason() + "\n");
                writer.write(m.toString()  + "\n");
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
            flag = 0;
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
                if (flag == 0) {
                    myMovies.add(tempMovie);
                }
                else {
                    inconsistentMovies.add(tempMovie);
                }

            }
            else if (qName.equalsIgnoreCase("t")) {
                checkTitleInconsistencies(tempVal);
                tempMovie.setTitle(tempVal.trim());
            }
            else if (qName.equalsIgnoreCase("year")) {
                checkYearInconsistencies(tempVal);
                tempMovie.setYear(tempVal.trim());
            }
            else if (qName.equalsIgnoreCase("cat")) {
                String full_cat = newGenreMap.get(tempVal);
                tempMovie.setGenres(full_cat);
            }
            else if (qName.equalsIgnoreCase("dirn")) {
                checkDirectorInconsistencies(tempVal);
                tempMovie.setDirector(tempVal.trim());
            }
        }
    }

    public void checkTitleInconsistencies(String tempVal) {
        tempVal = tempVal.trim();

        if (tempVal.equals("")) {
            flag = 1;
            tempMovie.setReason("Title is unknown");
        }
        else if (tempVal.length() > 100) {
            flag = 1;
            tempMovie.setReason("Title is too long");
        }
    }

    public void checkYearInconsistencies(String tempVal) {
        tempVal = tempVal.trim();
        if (tempVal.isEmpty()) {
            flag = 1;
            tempMovie.setReason("Year is unknown");
        }
        else if (!tempVal.matches("\\d+")) {
            flag = 1;
            tempMovie.setReason("Year contains non-numeric characters");
        }
    }

    public void checkDirectorInconsistencies(String tempVal) {
        tempVal = tempVal.trim();
        if (tempVal.isEmpty()) {
            flag = 1;
            tempMovie.setReason("Director is unknown");
        }
        else if (tempVal.length() > 100) {
            flag = 1;
            tempMovie.setReason("Director name is too long");
        }
    }

    public static void main(String[] args) {
        MovieParser my_parser = new MovieParser();
        my_parser.startParse();
    }

}