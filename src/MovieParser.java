import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



// for mains243.xml
public class MovieParser extends DefaultHandler {
    Map<String, String> genreNameMap = new HashMap<>();
    HashSet<String> new_genres = new HashSet<>();

    ArrayList<Movie> myMovies = new ArrayList<>();
    ArrayList<Movie> inconsistentMovies  = new ArrayList<>();

    private Movie tempMovie;
    private String tempVal;

    boolean consistent = true;


    // Data already in the database
    HashSet<Movie> existing_movies = new HashSet<>();
    HashSet<String> existing_genres = new HashSet<>();


    Connection conn;

    public MovieParser(){
        create_genre_map();
        connectDatabase();
    }

    public void create_genre_map() {
        genreNameMap.put("Ctxx", "Uncategorized");
        genreNameMap.put("Actn", "Action");
        genreNameMap.put("Advt", "Adventure");
        genreNameMap.put("AvGa", "Avant Garde");
        genreNameMap.put("Camp", "Now - Camp");
        genreNameMap.put("Cart", "Cartoon");
        genreNameMap.put("CnR", "Cops and Robbers");
        genreNameMap.put("Comd", "Comedy");
        genreNameMap.put("Disa", "Disaster");
        genreNameMap.put("Docu", "Documentary");
        genreNameMap.put("Dram", "Drama");
        genreNameMap.put("Epic", "Epic");
        genreNameMap.put("Faml", "Family");
        genreNameMap.put("Hist", "History");
        genreNameMap.put("Horr", "Horror");
        genreNameMap.put("Musc", "Musical");
        genreNameMap.put("Myst", "Mystery");
        genreNameMap.put("Noir", "Black");
        genreNameMap.put("Porn", "Pornography");
        genreNameMap.put("Romt", "Romance");
        genreNameMap.put("ScFi", "Sci-Fi");
        genreNameMap.put("Surl", "Surreal");
        genreNameMap.put("Susp", "Thriller");
        genreNameMap.put("West", "Western");
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

    public void startParse() throws ClassNotFoundException {
        try {
            getExistingMovies();
            getExistingGenres();
            parseDocument();
            insertMoviesAndGenres();

            printDataToFile("MOVIES.txt", myMovies);
            printDataToFile("MOVIE_ERRORS.txt", inconsistentMovies);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        for (String g: new_genres) {
            System.out.println(g);
        }
    }

    public void insertMoviesAndGenres() throws SQLException {
        if (conn != null) {
            String m_query = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?);";
            String g_query = "INSERT INTO genres (name) VALUES (?);";
            String mg_query = "INSERT INTO genres_in_movies (genre_id, movie_id) VALUES (?, ?);";
        }
    }

    public void getExistingMovies() throws SQLException {
        if (conn != null) {
            String query = "SELECT id, title, year, director " +
                    "FROM movies;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String year = resultSet.getString("year");
                String director = resultSet.getString("director");

                Movie movie = new Movie(id, title, year, director);
                existing_movies.add(movie);
            }
        }
    }

    public void getExistingGenres() throws SQLException {
        if (conn != null){
            String query = "SELECT name FROM genres;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                existing_genres.add(rs.getString("name"));
            }
        }
    }

    private void printDataToFile(String fileName, ArrayList<Movie> data) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Number of entries '" + data.size() + "'.\n");

            Iterator<Movie> it = data.iterator();
            while (it.hasNext()) {
                writer.write(it.next().toString() + "\n");
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
            sp.parse("./stanford-movies/mains243.xml", this);

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
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of employee
            tempMovie = new Movie();
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

        if (tempMovie != null) {
            if (qName.equalsIgnoreCase("film")) {
                checkMovieDuplicateInconsistencies(tempMovie);

                if (consistent) {
                    myMovies.add(tempMovie);
                }
                else {
                    inconsistentMovies.add(tempMovie);
                }

            }
            else if (qName.equalsIgnoreCase("fid")) {
                checkIdInconsistencies(tempVal);
                tempMovie.setMovieId(tempVal.trim());
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
                String full_cat = genreNameMap.getOrDefault(tempVal, "");
                documentNewGenre(full_cat);
                tempMovie.setGenres(full_cat);
            }
            else if (qName.equalsIgnoreCase("dirn")) {
                checkDirectorInconsistencies(tempVal);
                tempMovie.setDirector(tempVal.trim());
            }
        }
    }

    public void documentNewGenre(String full_cat) {
        if (!full_cat.equals("")) {
            if (!existing_genres.contains(full_cat) && !new_genres.contains(full_cat)) {
                new_genres.add(full_cat);
            }
        }
    }

    public void checkMovieDuplicateInconsistencies(Movie tempMovie) {
        if (existing_movies.contains(tempMovie)) {
            consistent = false;
            tempMovie.setReason("Duplicate: Movie exists in database already");
        }
        else if (myMovies.contains(tempMovie)) {
            consistent = false;
            tempMovie.setReason("Duplicate: Movie parsed already");
        }
    }

    public void checkIdInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempMovie.setReason("Movie Id is unknown");
        }
        else if (tempVal.length() > 10) {
            consistent = false;
            tempMovie.setReason("Movie Id is too long");
        }
    }

    public void checkTitleInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempMovie.setReason("Title is unknown");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempMovie.setReason("Title is too long");
        }
    }

    public void checkYearInconsistencies(String tempVal) {
        if (tempVal.isEmpty()) {
            consistent = false;
            tempMovie.setReason("Year is unknown");
        }
        else if (!tempVal.matches("\\d+")) {
            consistent = false;
            tempMovie.setReason("Year contains non-numeric characters");
        }
    }

    public void checkDirectorInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempMovie.setReason("Director is unknown");
        }
        else if (tempVal.length() > 100) {
            consistent = false;
            tempMovie.setReason("Director name is too long");
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        MovieParser my_parser = new MovieParser();
        my_parser.startParse();
    }

}