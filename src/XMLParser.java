import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class XMLParser {
    StarParser starParser = new StarParser();
    MovieParser movieParser = new MovieParser();
    StarInMovieParser starInMovieParser;

    ArrayList<Star> parsed_stars;
    ArrayList<Star> existing_stars;

    HashMap<String, Movie> parsed_movies;
    ArrayList<Movie> existing_movies;


    public void startParsing() {
        long start = System.currentTimeMillis();

        System.out.println("Starting star parsing...");
        starParser.startParse();
        parsed_stars = starParser.getParsedStarsData();
        existing_stars = starParser.getExistingStarsData();


        System.out.println("Starting movie parsing...");
        movieParser.startParse();
        parsed_movies = movieParser.getParsedMoviesData();
        existing_movies = movieParser.getExistingMoviesData();

//        System.out.println("Size of parsed stars: " + parsed_stars.size());
//        System.out.println("Size of existing stars: " + existing_stars.size());
//
//        System.out.println("Size of parsed movies: " + parsed_movies.size());
//        System.out.println("Size of existing movies: " + existing_movies.size());


        System.out.println("Starting star movie parsing...");
        starInMovieParser = new StarInMovieParser(existing_stars, parsed_movies);
        starInMovieParser.startParse();

        long end = System.currentTimeMillis();
        System.out.println("Finished star parsing. Took " + (end - start)/1000 + "s");

//        printArrayToFile("teststar.txt", "testmovie.txt");
    }

    public void printArrayToFile(String fileName1, String fileName2) {
        try (FileWriter writer = new FileWriter(fileName1)) {
            writer.write("Number of Entries '" + existing_stars.size() + "'.\n");

            for (Star myStar : existing_stars) {
                writer.write(myStar.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(fileName2)) {
            writer.write("Number of Entries '" + existing_movies.size() + "'.\n");

            for (Movie myMovie : existing_movies) {
                writer.write(myMovie.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        XMLParser xmlParser = new XMLParser();
        xmlParser.startParsing();
    }
}
