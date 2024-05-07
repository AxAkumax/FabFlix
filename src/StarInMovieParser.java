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



// for casts124.xml
public class StarInMovieParser extends DefaultHandler {
    ArrayList<StarInMovie> starMovies;
    ArrayList<StarInMovie> inconsistentEntries;

    private StarInMovie tempStarMovie;
    private String tempVal;
    private String director;

    private boolean directorflag;

    // 0 means the entry is consistent
    // 1 means the entry is inconsistent
    int inconsistent_flag = 0;

    public StarInMovieParser() {
        director = "";
        starMovies = new ArrayList<StarInMovie>();
        inconsistentEntries = new ArrayList<StarInMovie>();
    }

    public void startParse() {
        parseDocument();
        printDataToFile("STARMOVIES.txt");
        printInconsistenciesToFile("STARMOVIE_ERRORS.txt");
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

    private void printInconsistenciesToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Movies '" + inconsistentEntries.size() + "'.\n");

            Iterator<StarInMovie> it = inconsistentEntries.iterator();
            while (it.hasNext()) {
                StarInMovie m = it.next();
                writer.write(m.getReason() + "\n");
                writer.write(m.toString()  + "\n");
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
        tempVal = "";

        if (qName.equalsIgnoreCase("is")) {
            directorflag = true;
        }
        else if (qName.equalsIgnoreCase("m")) {
            tempStarMovie = new StarInMovie();
            inconsistent_flag = 0;
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

//        System.out.println(qName);
//        System.out.println("temp: " + tempVal);

        if (tempStarMovie != null) {

            if (qName.equalsIgnoreCase("m")) {
                tempStarMovie.setDirector(director);

                if (inconsistent_flag == 0) {
                    starMovies.add(tempStarMovie);
                }
                else {
                    inconsistentEntries.add(tempStarMovie);
                }

            } else if (qName.equalsIgnoreCase("t")) {
                checkMovieTitleInconsistencies(tempVal);
                tempStarMovie.setMovieName(tempVal);
            } else if (qName.equalsIgnoreCase("a")) {

                checkNameInconsistencies(tempVal, "Star");
                tempStarMovie.setStarName(tempVal);
            }
        }

//        System.out.println("temp: " + tempVal + "\n");
    }

    private void checkMovieTitleInconsistencies(String tempVal) {
        if (tempVal.equals("")) {
            inconsistent_flag = 1;
            tempStarMovie.setReason("Title is Unknown");
        }
        else if (tempVal.length() > 100) {
            inconsistent_flag = 1;
            tempStarMovie.setReason("Title is too long");
        }
    }

    private void checkNameInconsistencies(String tempVal, String who) {
        if (tempVal.equals("")) {
            inconsistent_flag = 1;
            tempStarMovie.setReason(who + " Name is Unknown");
        }
        else if (tempVal.length() > 100) {
            inconsistent_flag = 1;
            tempStarMovie.setReason(who + " Name is too long");
        }
    }

}