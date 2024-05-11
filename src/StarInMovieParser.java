import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



// for casts124.xml
public class StarInMovieParser extends DefaultHandler {
    ArrayList<StarInMovie> starMovies = new ArrayList<StarInMovie>();
    ArrayList<StarInMovie> inconsistentEntries = new ArrayList<StarInMovie>();;

//    ArrayList<Star> parsedStars;
//    ArrayList<Movie> parsedMovies;

    HashSet<StarInMovie> existingStarsInMovies = new HashSet<StarInMovie>();
    HashSet<Star> existingStars;
//    HashSet<Movie> existingMovies;
    HashMap<String, Movie> parsedMovies;

    private StarInMovie tempStarMovie;
    private String tempVal;
    private String director;

    private boolean directorflag;
    boolean consistent = true;

    int max_star_id = 0;
    int max_movie_id = 0;

    Connection conn;

    public StarInMovieParser(ArrayList<Star> existingStars, HashMap<String, Movie> parsedMovies) {
        this.existingStars = new HashSet<>(existingStars);
        this.parsedMovies = parsedMovies;
        director = "";

        connectDatabase();
    }

    public void startParse() {

        try {
            getMaxIds();
            getExistingStarsInMovies();

            parseDocument();

            insertStarsInMovies();

            printDataToFile("STARMOVIES.txt", starMovies);
            printDataToFile("STARMOVIE_ERRORS.txt", inconsistentEntries);
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertStarsInMovies() throws SQLException {
        if (conn != null) {
            String query = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            for (StarInMovie sm: starMovies) {
//                System.out.println("inserting: " + sm);

                Movie movie = parsedMovies.get(sm.getMovieFID());
                String starId = findStarId(sm.getStarName());

                if (starId != null) {
                    String movieId = movie.getMovieDBID();
                    ps.setString(1, starId);
                    ps.setString(2, movieId);
                    ps.executeUpdate();
                }
                else {
                    sm.setReason("star id was not found in stars table");
                    inconsistentEntries.add(sm);
                }
            }
        }
    }

    public String findStarId(String star_name) {
        for (Star s: existingStars) {
            if (s.getName().equals(star_name)) {
                return s.getStarDbid();
            }
        }
        return null;
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

    public void getMaxIds() throws SQLException {
        if (conn != null) {
            String query = "SELECT max(id) as max_star_id FROM stars;";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String star_id = rs.getString("max_star_id");
                max_star_id = Integer.parseInt(star_id.substring(2));
            }

            query = "SELECT max(id) as max_movie_id FROM movies;";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                String movie_id = rs.getString("max_movie_id");
                max_movie_id = Integer.parseInt(movie_id.substring(2));
            }
        }
    }

    public void getExistingStarsInMovies() throws SQLException {
        if (conn != null) {
            String query = "SELECT sm.starId, sm.movieId, m.title, m.year, m.director, s.name " +
                            "FROM stars_in_movies sm " +
                            "JOIN movies m ON sm.movieId = m.id " +
                            "JOIN stars s ON sm.starId = s.id;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                String movieId = rs.getString("movieId");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");

                StarInMovie new_entry = new StarInMovie(movieId, starName, title, director);
                existingStarsInMovies.add(new_entry);
            }
        }
    }

    public void printDataToFile(String fileName, ArrayList<StarInMovie> data) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Number of Entries '" + data.size() + "'.\n");

            for (StarInMovie myStarMovie : data) {
                writer.write(myStarMovie.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("./stanford-movies/casts124.xml", this);

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

    //Event Handlers

    // startElement() is invoked when the parsing begins for an element
    // use it to construct List of Movies
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";

        if (qName.equalsIgnoreCase("is")) {
            directorflag = true;
        }
        else if (qName.equalsIgnoreCase("m")) {
            tempStarMovie = new StarInMovie();
            consistent = true;
        }
    }

    // characters(char[], int, int) receives characters with boundaries.
    // We’ll convert them to a String and store it in a variable
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);

        if (directorflag) {
            director = tempVal;
            checkNameInconsistencies(director, "Director");  // for director's name
            directorflag = false;
        }

    }

    // endElement() is invoked when the parsing ends for an element
    // this is when we’ll assign the content of the tags to their respective variables
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tempVal = tempVal.trim();

        if (tempStarMovie != null) {

            if (qName.equalsIgnoreCase("m")) {
                tempStarMovie.setDirector(director);
                checkStarMovieInconsistencies();

                if (consistent) {
                    starMovies.add(tempStarMovie);
                } else {
                    inconsistentEntries.add(tempStarMovie);
                }

            }
            else if (qName.equalsIgnoreCase("f")) {
                tempStarMovie.setMovieFID(tempVal);
            } else if (qName.equalsIgnoreCase("t")) {
                checkMovieTitleInconsistencies(tempVal);
                tempStarMovie.setMovieName(tempVal);
            } else if (qName.equalsIgnoreCase("a")) {

                checkNameInconsistencies(tempVal, "Star");
                tempStarMovie.setStarName(tempVal);
            }
        }
    }

    public void checkStarMovieInconsistencies() {
        if (starMovies.contains(tempStarMovie)) {
            consistent = false;
            tempStarMovie.setReason("Duplicate: Star movie parsed already");
        }
        else if (existingStarsInMovies.contains(tempStarMovie)) {
            consistent = false;
            tempStarMovie.setReason("Duplicate: Star movie exists in database already");
        }
        else if (!parsedMovies.containsKey(tempStarMovie.getMovieFID())) {
            consistent = false;
            tempStarMovie.setReason("Referenced movie id does not exist in mains243.xml");

        }

        if (tempStarMovie.getDirector().equals("")) {
            consistent = false;
            tempStarMovie.setDirector("Unknown Director");
        }
    }

    private void checkMovieTitleInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempStarMovie.setReason("Title is Unknown");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempStarMovie.setReason("Title is too long");
        }
    }

    private void checkNameInconsistencies(String tempVal, String who) {
        if (tempVal.equals("")) {
            consistent = false;
            tempStarMovie.setReason(who + " Name is Unknown");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempStarMovie.setReason(who + " Name is too long");
        }
    }

}