import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



// for mains243.xml
public class MovieParser extends DefaultHandler {
    ArrayList<Movie> myMovies;

    private String tempVal;

    //to maintain context
    private Movie tempMovie;

    public MovieParser() {
        myMovies = new ArrayList<Movie>();
    }

    public void startParse() {
        parseDocument();
        printDataToFile("movieresults.txt");
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
                tempMovie.setGenres(tempVal);
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