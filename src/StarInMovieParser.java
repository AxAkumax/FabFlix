import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



//
public class StarInMovieParser extends DefaultHandler {
    ArrayList<StarInMovie> starMovies;
    private StarInMovie tempStarMovie;
    private String tempVal;

    public StarInMovieParser() {
        starMovies = new ArrayList<StarInMovie>();
    }

    public void startParse() {
        parseDocument();
        printDataToFile("starmovieresults.txt");
    }

    public static void main(String[] args) {
        StarInMovieParser my_parser = new StarInMovieParser();
        my_parser.startParse();
    }

    private void printDataToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Star Movies'" + starMovies.size() + "'.\n");

            for (StarInMovie myStarMovie : starMovies) {
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
            sp.parse("../stanford-movies/casts124.xml", this);

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
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of employee
            tempStarMovie = new StarInMovie();
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

        if (tempStarMovie != null) {
            if (qName.equalsIgnoreCase("m")) {
                //add it to the list
                starMovies.add(tempStarMovie);
            }
            else if (qName.equals("is")) {
                tempStarMovie.setDirector(tempVal);
            }
            else if (qName.equalsIgnoreCase("t")) {
                tempStarMovie.setMovieName(tempVal);
            }
            else if (qName.equalsIgnoreCase("a")) {
                tempStarMovie.setStarName(tempVal);
            }
        }
    }

}