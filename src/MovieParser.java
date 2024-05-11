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
    // a map of category abbreviations to its full category name
    Map<String, String> genreNameMap = new HashMap<>();

    // arrays to hold good and inconsistent movies
    ArrayList<Movie> myMovies = new ArrayList<>();
    ArrayList<Movie> inconsistentMovies  = new ArrayList<>();

    // variables to hold temporary information from the SAX Parser
    private Movie tempMovie;
    private String tempVal;

    // flag to check if the movie data in consistent or not
    boolean consistent = true;

    // Data already in the database
    ArrayList<Movie> existing_movies = new ArrayList<>();
    HashMap<Integer, String> existing_genres = new HashMap<>();
    HashMap<String, Movie> parsed_movie_map = new HashMap<>();

    // index to hold max movie id. Increment from there to get new movie ids
    int max_movie_id = 0;

    // index to hold max genre id. Increment from there to get new genre ids
    // a genre index to hold the number from where new genres start, to insert into database
    int max_genre_id = 0;
    int new_genre_start_index = 0;

    // SQL connection
    Connection conn;


    /**
    * Movie Parser constructor:
    *   creates the abbreviations to full form category map and connects to database.
    * */
    public MovieParser(){
        create_genre_map();
        connectDatabase();
    }


    /**
    * Creates map where key is abbreviations and value is its full form.
     * */
    public void create_genre_map() {
        genreNameMap.put("Ctxx", "Uncategorized");
        genreNameMap.put("Actn", "Action");
        genreNameMap.put("BioP", "Biography");
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


    /**
     * Connects to database and stores the connection is variable "conn".
     */
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


    /**
     * Starts off the parsing and database process
     * Prints the movies and inconsistencies to a file.
     */
    public void startParse() {
        try {
            getExistingMovies();
            getExistingGenres();
            parseDocument();

            insertGenres();
            insertMovies();
            insertGenresInMovies();

            printDataToFile("MOVIES.txt", myMovies);
            printDataToFile("MOVIE_ERRORS.txt", inconsistentMovies);
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Movie> getParsedMoviesData() {
        return parsed_movie_map;
    }

    public ArrayList<Movie> getExistingMoviesData() {
        return existing_movies;
    }

    /**
     * Inserts new genres that were collected during parsing into the database.
     * @throws SQLException
     */
    public void insertGenres() throws SQLException {
        if (conn != null) {
            String query = "INSERT INTO genres (id, name) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);

            for (Map.Entry<Integer, String> entry : existing_genres.entrySet()) {
                if (entry.getKey() >= new_genre_start_index) {
                    statement.setInt(1, entry.getKey());
                    statement.setString(2, entry.getValue());
                    statement.executeUpdate();
                }
            }
        }
    }


    /**
     * Inserts consistent movies into the database.
     * @throws SQLException
     */
    public void insertMovies() throws SQLException {
        if (conn != null) {
            String query = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?);";
            PreparedStatement statement = conn.prepareStatement(query);

            for (int i = 0; i < myMovies.size(); i++) {
                Movie movie = myMovies.get(i);

                max_movie_id++;
                String movie_id = "tt" + max_movie_id;

                movie.setDBID(movie_id);

                statement.setString(1, movie_id);
                statement.setString(2, movie.getTitle());
                statement.setInt(3, Integer.parseInt(movie.getYear()));
                statement.setString(4, movie.getDirector());

                statement.executeUpdate();

                existing_movies.add(movie);
            }
        }

        System.out.println("Movies in db: " + existing_movies.size());
    }


    /**
     * Given a category name, obtains the genre id that is associate with it.
     * Since the existing genre map is (key, value) -> (genre id, genre name),
        * iterates through the map's values, and find the key associated with it.
     * @param genre_name
     * @return
     */
    public int getGenreId(String genre_name) {
        for (Map.Entry<Integer, String> entry : existing_genres.entrySet()) {
            if (entry.getValue().equals(genre_name)) {
                return entry.getKey();
            }
        }
        return -1;
    }


    /**
     * Populates the genres_in_movies table.
     * @throws SQLException
     */
    public void insertGenresInMovies() throws SQLException {
        if (conn != null) {
            String query = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);

            for (Movie movie : myMovies) {
                for (String genre: movie.getGenres()) {
                    int genre_id = getGenreId(genre);

                    if (genre_id != -1) {
                        statement.setInt(1, genre_id);
                        statement.setString(2, movie.getMovieDBID());
                        statement.executeUpdate();
                    }
                }
            }
        }
    }


    /**
     * obtains the data from movies table, and stores it in memory
     * also gets the max movie id, so that it can be incremented when inserting new movies
     * @throws SQLException
     */
    public void getExistingMovies() throws SQLException {
        if (conn != null) {
            // get the existing movies from database
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

            // get the max movie_id
            query = "SELECT max(id) as movie_max FROM movies;";
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String movie_id = resultSet.getString("movie_max");
                max_movie_id = Integer.parseInt(movie_id.substring(2));
            }
        }
    }


    /**
     * obtains the data from genres table, and stores it in memory
     * also gets the max genre id, so that it can be incremented when inserting new genres
     * @throws SQLException
     */
    public void getExistingGenres() throws SQLException {
        if (conn != null){
            // getting all the genres from the database
            String query = "SELECT id, name FROM genres;";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");

                existing_genres.put(id, name);
            }

            // getting the max id of the genres already in the database, to increment new genres
            String max_query = "SELECT max(id) as genre_max FROM genres;";
            statement = conn.prepareStatement(max_query);
            rs = statement.executeQuery();

            while (rs.next()) {
                max_genre_id = rs.getInt("genre_max");
                new_genre_start_index = max_genre_id + 1;
            }
        }
    }


    /**
     * Given a filename and a Movies array, prints the movie data to a file
     * Used to print movies and inconsistent_movies to a file
     * @param fileName
     * @param data_array
     */
    private void printDataToFile(String fileName, ArrayList<Movie> data_array) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Number of entries '" + data_array.size() + "'.\n");

            Iterator<Movie> it = data_array.iterator();
            while (it.hasNext()) {
                writer.write(it.next().toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * starts the SAX Parser
     */
    public void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("./stanford-movies/mains243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    /**
     * SAX event handler: invoked when the parsing begins for an element
     *
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @throws SAXException
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of employee
            tempMovie = new Movie();
            consistent = true;
        }
    }


    /**
     * SAX event handler: receives characters with boundaries.
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @throws SAXException
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }


    /**
     * SAX event handler: invoked when the parsing ends for an element.
     *
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     */
    public void endElement(String uri, String localName, String qName) {
        tempVal = tempVal.trim();

        if (tempMovie != null) {
            if (qName.equalsIgnoreCase("film")) {
                checkMovieInconsistencies(tempMovie);

                if (consistent) {
                    myMovies.add(tempMovie);
                    parsed_movie_map.put(tempMovie.getMovieFID(), tempMovie);
                }
                else {
                    inconsistentMovies.add(tempMovie);
                }

            }
            else if (qName.equalsIgnoreCase("fid") || qName.equalsIgnoreCase("filmed")) {
                checkIdInconsistencies(tempVal);
                tempMovie.setFID(tempVal.trim());
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


    /**
     * Checks if the given genre has been seen already
     * If new genre, uses max_genre_id to increment genre id and add it to existing_genres
     * @param full_cat
     */
    public void documentNewGenre(String full_cat) {
        if (!full_cat.equals("")) {
            if (!existing_genres.containsValue(full_cat)) {
                max_genre_id++;
                existing_genres.put(max_genre_id, full_cat);
            }
        }
    }


    /**
     * Checks for inconsistencies in the movie object like:
            * if a movie was in database already
            * if a movie was seen during parsing
            * if the movie's fields (id, title, year, director) are empty because tag is not present in the XML
     * @param tempMovie
     */
    public void checkMovieInconsistencies(Movie tempMovie) {
        // checking for duplicates
        if (existing_movies.contains(tempMovie)) {
            consistent = false;
            tempMovie.setReason("Duplicate: Movie exists in database already");
        }
        else if (myMovies.contains(tempMovie)) {
            consistent = false;
            tempMovie.setReason("Duplicate: Movie parsed already");
        }

        // checking if movie's fid is never encountered
        if (tempMovie.getMovieFID().equals("")) {
            consistent = false;
            tempMovie.setReason("XML file does not contain any movie ID");
        }
        else if (tempMovie.getTitle().equals("")) {
            consistent = false;
            tempMovie.setReason("XML file does not contain any title");
        }
        else if (tempMovie.getYear().equals("")) {
            consistent = false;
            tempMovie.setReason("XML file does not contain any year");
        }
        else if (tempMovie.getDirector().equals("")) {
            consistent = false;
            tempMovie.setReason("XML file does not contain any director");
        }
    }


    /**
     * Checks if movie id in XML is empty. Counts that as an inconsistency
     * @param tempVal
     */
    public void checkIdInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            consistent = false;
            tempMovie.setReason("Movie Id is unknown");
        }
    }


    /**
     * Checks if movie title is empty (title NOT NULL) or longer than 100 chars (title VARCHAR(100)).
     * Counts both as an inconsistency.
     * @param tempVal
     */
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


    /**
     * Checks if movie year is empty (year NOT NULL) or is not an integer (year INTEGER).
     * @param tempVal
     */
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


    /**
     * Checks if movie director is empty (director NOT NULL) or longer than 100 chars (director VARCHAR(100)).
     * @param tempVal
     */
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


    /**
     * Entry point for MovieParser. calls startParse().
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        MovieParser my_parser = new MovieParser();
        my_parser.startParse();
    }

}