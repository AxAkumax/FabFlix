import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("inside doget");
        System.out.println(request.getQueryString());

        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String genreIdParameter = request.getParameter("genreId");
            String titleStartParameter = request.getParameter("titleStart");

            if (genreIdParameter != null) {
                System.out.println("line 46 " + genreIdParameter);
                int genreId = Integer.parseInt(genreIdParameter);
                // Execute SQL query to get movies for the given genreId
                String query = "SELECT " +
                        "m.id AS movie_id, " +
                        "m.title AS movie_title, " +
                        "m.year AS movie_year, " +
                        "m.director AS movie_director, " +
                        "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
                        "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
                        "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
                        "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
                        "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                        "WHERE gm.movieId = m.id) AS movie_genres, " +
                        "AVG(r.rating) AS average_rating " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gm ON gm.movieId = m.id " +
                        "WHERE gm.genreId = ? " +
                        "GROUP BY m.id, m.title, m.year, m.director " +
                        "ORDER BY average_rating DESC;";


                System.out.println("line 67");
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, genreId);
                System.out.println("line 70");
                ResultSet rs = statement.executeQuery();
                System.out.println("line 72");
                JsonArray jsonArray = new JsonArray();
                System.out.println(rs);
                while (rs.next()) {
                    String stars_and_ids = rs.getString("star_ids_and_names");
                    String[] parts = stars_and_ids.split(";");

                    String movieStars = "";
                    String movieStarIds = "";

                    for (int i = 0; i < parts.length; i += 2) {
                        String id = parts[i] + ", ";
                        String name = parts[i+1] + ", ";

                        movieStarIds += id;
                        movieStars += name;
                    }

                    movieStars = movieStars.substring(0, movieStars.length() - 2);
                    movieStarIds = movieStarIds.substring(0, movieStarIds.length() - 2);


                    String movieId = rs.getString("movie_id");
                    String movieTitle = rs.getString("movie_title");
                    String movieYear = rs.getString("movie_year");
                    String movieDirector = rs.getString("movie_director");
                    String movieGenres = rs.getString("movie_genres");
                    double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_stars", movieStars);
                    jsonObject.addProperty("movie_starIds", movieStarIds);
                    jsonObject.addProperty("movie_genres", movieGenres);
                    jsonObject.addProperty("average_rating", averageRating);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else if (titleStartParameter != null) {
                System.out.println("line 77 " + titleStartParameter);
                String query;
                PreparedStatement statement;
                if (titleStartParameter.equals("*")) {
                    System.out.println("line 88 " + titleStartParameter);

                    query = "SELECT " +
                            "m.id AS movie_id, " +
                            "m.title AS movie_title, " +
                            "m.year AS movie_year, " +
                            "m.director AS movie_director, " +
                            "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
                            "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
                            "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
                            "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
                            "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                            "WHERE gm.movieId = m.id) AS movie_genres, " +
                            "AVG(r.rating) AS average_rating " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "JOIN genres_in_movies gm ON gm.movieId = m.id " +
                            "WHERE m.title REGEXP '^[^a-zA-Z0-9]' " +
                            "GROUP BY m.id, m.title, m.year, m.director " +
                            "ORDER BY average_rating DESC;";
                    statement = conn.prepareStatement(query);
                }
                else {
                    // Execute SQL query to get movies for the given genreId
                    // query = "SELECT title FROM movies WHERE LOWER(title) LIKE LOWER(?)";

                    query = "SELECT " +
                            "m.id AS movie_id, " +
                            "m.title AS movie_title, " +
                            "m.year AS movie_year, " +
                            "m.director AS movie_director, " +
                            "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
                            "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
                            "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
                            "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
                            "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                            "WHERE gm.movieId = m.id) AS movie_genres, " +
                            "AVG(r.rating) AS average_rating " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "JOIN genres_in_movies gm ON gm.movieId = m.id " +
                            "WHERE LOWER(m.title) LIKE LOWER(?) " +
                            "GROUP BY m.id, m.title, m.year, m.director " +
                            "ORDER BY average_rating DESC;";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, titleStartParameter + "%");
                }

                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();
                System.out.println(rs);
                while (rs.next()) {
                    String stars_and_ids = rs.getString("star_ids_and_names");
                    String[] parts = stars_and_ids.split(";");

                    String movieStars = "";
                    String movieStarIds = "";

                    for (int i = 0; i < parts.length; i += 2) {
                        String id = parts[i] + ", ";
                        String name = parts[i+1] + ", ";

                        movieStarIds += id;
                        movieStars += name;
                    }

                    movieStars = movieStars.substring(0, movieStars.length() - 2);
                    movieStarIds = movieStarIds.substring(0, movieStarIds.length() - 2);

                    String movieId = rs.getString("movie_id");
                    String movieTitle = rs.getString("movie_title");
                    String movieYear = rs.getString("movie_year");
                    String movieDirector = rs.getString("movie_director");
                    String movieGenres = rs.getString("movie_genres");
                    double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_genres", movieGenres);
                    jsonObject.addProperty("movie_stars", movieStars);
                    jsonObject.addProperty("movie_starIds", movieStarIds);
                    jsonObject.addProperty("average_rating", averageRating);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else {
                String query = "SELECT id, name FROM genres ORDER BY name ASC;";

                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();

                while (rs.next()) {
                    String genreId = rs.getString("id");
                    String genreName = rs.getString("name");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("genreId", genreId);
                    jsonObject.addProperty("genreName", genreName);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }


}
