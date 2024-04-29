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
//test
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
        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {

             String genreIdParameter = request.getParameter("genreId");
//            String titleStartParameter = request.getParameter("titleStart");
//
//            String sortAttribute = request.getParameter("sortAttribute"); // Get sort attribute
//            int recordsPerPage = Integer.parseInt(request.getParameter("recordsPerPage"));
//            int currentPage = Integer.parseInt(request.getParameter("page"));
//            int offset = (currentPage - 1) * recordsPerPage;

            if (genreIdParameter != null) {
                System.out.println("line 46 " + genreIdParameter);
                int genreId = Integer.parseInt(genreIdParameter);
                // Execute SQL query to get movies for the given genreId
                String query = "SELECT " +
                "m.id AS movie_id, " +
                        "m.title AS movie_title, " +
                        "m.year AS movie_year, " +
                        "m.director AS movie_director, " +
                        "GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') AS star_ids_and_names, " +
                        "GROUP_CONCAT(DISTINCT CONCAT(g.id, ';', g.name) SEPARATOR ';') AS genres, " +
                        "AVG(r.rating) AS average_rating " + // Calculate average rating
                        "FROM movies m " +
                        "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "JOIN genres g ON gm.genreId = g.id " +
                        "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "JOIN stars s ON sm.starId = s.id " +
                        "LEFT JOIN ratings r ON m.id = r.movieId " + // Join with ratings table
                        "WHERE g.id = ? " +
                        "GROUP BY m.id, m.title, m.year, m.director";

                //System.out.println(sortAttribute);

                //query += "ORDER BY " + sortAttribute;
                //+ " LIMIT ? OFFSET ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, genreId);

    //                statement.setInt(2, recordsPerPage); // Set limit
//                statement.setInt(3, offset); // Set offset

                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();

                while (rs.next()) {

                    String movieId = rs.getString("movie_id");
                    String movieTitle = rs.getString("movie_title");
                    String movieYear = rs.getString("movie_year");
                    String movieDirector = rs.getString("movie_director");
                    String movieGenres = rs.getString("genres");
                    double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", movieId);
                    jsonObject.addProperty("title", movieTitle);
                    jsonObject.addProperty("year", movieYear);
                    jsonObject.addProperty("director", movieDirector);
                    jsonObject.addProperty("stars", rs.getString("star_ids_and_names"));
                    jsonObject.addProperty("genres", movieGenres);
                    jsonObject.addProperty("rating", averageRating);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();
                jsonResponse.add("movies", jsonArray);
                out.print(jsonResponse.toString());

                // Write JSON string to output
                //out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
//            else if (titleStartParameter != null) {
//                System.out.println("line 77 " + titleStartParameter);
//                String query;
//                PreparedStatement statement;
//                if (titleStartParameter.equals("*")) {
//                    System.out.println("line 88 " + titleStartParameter);
//
//                    query = "SELECT " +
//                            "m.id AS movie_id, " +
//                            "m.title AS movie_title, " +
//                            "m.year AS movie_year, " +
//                            "m.director AS movie_director, " +
//                            "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
//                            "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
//                            "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
//                            "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
//                            "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
//                            "WHERE gm.movieId = m.id) AS movie_genres, " +
//                            "AVG(r.rating) AS average_rating " +
//                            "FROM movies m " +
//                            "LEFT JOIN ratings r ON m.id = r.movieId " +
//                            "JOIN genres_in_movies gm ON gm.movieId = m.id " +
//                            "WHERE m.title REGEXP '^[^a-zA-Z0-9]' " +
//                            "GROUP BY m.id, m.title, m.year, m.director " +
//                            "ORDER BY average_rating DESC;";
//                    statement = conn.prepareStatement(query);
//                    query+= " LIMIT ? OFFSET ?";
//
//                    statement.setInt(1, recordsPerPage); // Set limit
//                    statement.setInt(2, offset); // Set offset
//
//                }
//                else {
//                    // Execute SQL query to get movies for the given genreId
//                    // query = "SELECT title FROM movies WHERE LOWER(title) LIKE LOWER(?)";
//
//                    query = "SELECT " +
//                            "m.id AS movie_id, " +
//                            "m.title AS movie_title, " +
//                            "m.year AS movie_year, " +
//                            "m.director AS movie_director, " +
//                            "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
//                            "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
//                            "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
//                            "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
//                            "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
//                            "WHERE gm.movieId = m.id) AS movie_genres, " +
//                            "AVG(r.rating) AS average_rating " +
//                            "FROM movies m " +
//                            "LEFT JOIN ratings r ON m.id = r.movieId " +
//                            "JOIN genres_in_movies gm ON gm.movieId = m.id " +
//                            "WHERE LOWER(m.title) LIKE LOWER(?) " +
//                            "GROUP BY m.id, m.title, m.year, m.director " +
//                            "ORDER BY average_rating DESC;";
//                    statement = conn.prepareStatement(query);
//                    statement.setString(1, titleStartParameter + "%");
//                    query+= " LIMIT ? OFFSET ?";
//
//                    statement.setInt(2, recordsPerPage); // Set limit
//                    statement.setInt(3, offset); // Set offset
//                }
//
//                ResultSet rs = statement.executeQuery();
//                JsonArray jsonArray = new JsonArray();
//                System.out.println(rs);
//                while (rs.next()) {
//                    String stars_and_ids = rs.getString("star_ids_and_names");
//                    String[] parts = stars_and_ids.split(";");
//
//                    String movieStars = "";
//                    String movieStarIds = "";
//
//                    for (int i = 0; i < parts.length; i += 2) {
//                        String id = parts[i] + ", ";
//                        String name = parts[i+1] + ", ";
//
//                        movieStarIds += id;
//                        movieStars += name;
//                    }
//
//                    movieStars = movieStars.substring(0, movieStars.length() - 2);
//                    movieStarIds = movieStarIds.substring(0, movieStarIds.length() - 2);
//
//                    String movieId = rs.getString("movie_id");
//                    String movieTitle = rs.getString("movie_title");
//                    String movieYear = rs.getString("movie_year");
//                    String movieDirector = rs.getString("movie_director");
//                    String movieGenres = rs.getString("movie_genres");
//                    double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;
//
//                    JsonObject jsonObject = new JsonObject();
//                    jsonObject.addProperty("movie_id", movieId);
//                    jsonObject.addProperty("movie_title", movieTitle);
//                    jsonObject.addProperty("movie_year", movieYear);
//                    jsonObject.addProperty("movie_director", movieDirector);
//                    jsonObject.addProperty("movie_genres", movieGenres);
//                    jsonObject.addProperty("movie_stars", movieStars);
//                    jsonObject.addProperty("movie_starIds", movieStarIds);
//                    jsonObject.addProperty("average_rating", averageRating);
//
//                    jsonArray.add(jsonObject);
//                }
//
//                rs.close();
//                statement.close();
//
//                // Write JSON string to output
//                out.write(jsonArray.toString());
//                // Set response status to 200 (OK)
//                response.setStatus(200);
//            }
//            else {
//                String query = "SELECT id, name FROM genres ORDER BY name ASC;";
//
//                PreparedStatement statement = conn.prepareStatement(query);
//
//                query+= " LIMIT ? OFFSET ?";
//
//                statement.setInt(1, recordsPerPage); // Set limit
//                statement.setInt(2, offset); // Set offset
//
//                ResultSet rs = statement.executeQuery();
//                JsonArray jsonArray = new JsonArray();
//
//                while (rs.next()) {
//                    String genreId = rs.getString("id");
//                    String genreName = rs.getString("name");
//
//                    JsonObject jsonObject = new JsonObject();
//                    jsonObject.addProperty("genreId", genreId);
//                    jsonObject.addProperty("genreName", genreName);
//
//                    jsonArray.add(jsonObject);
//                }
//
//                rs.close();
//                statement.close();
//
//                // Write JSON string to output
//                out.write(jsonArray.toString());
//                // Set response status to 200 (OK)
//                response.setStatus(200);
//            }
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
