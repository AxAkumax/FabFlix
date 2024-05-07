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



//
public class StarParser extends DefaultHandler {
    ArrayList<Star> myStars;
    ArrayList<Star> inconsistentStars;

    private Star tempStar;
    private String tempVal;

    // 0 means the entry is consistent
    // 1 means the entry is inconsistent
    int flag = 0;

    public StarParser() {

        myStars = new ArrayList<Star>();
        inconsistentStars = new ArrayList<>();
    }

    public void startParse() {
        parseDocument();
        printDataToFile("STARS.txt");
        printInconsistenciesToFile("STAR_ERRORS.txt");
    }

    public static void main(String[] args) {
        StarParser my_parser = new StarParser();
        my_parser.startParse();
    }

    private void printDataToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Stars '" + myStars.size() + "'.\n");

            for (Star myStar : myStars) {
                writer.write(myStar.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printInconsistenciesToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("No of Stars '" + inconsistentStars.size() + "'.\n");

            for (Star myStar : inconsistentStars) {
                writer.write(myStar.getReason() + "\n");
                writer.write(myStar.toString() + "\n");
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
            sp.parse("../stanford-movies/actors63.xml", this);

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
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of employee
            tempStar = new Star();
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

        if (tempStar != null) {
            if (qName.equalsIgnoreCase("actor")) {

                //add it to the list
                if (flag == 0) {
                    myStars.add(tempStar);
                }
                else {
                    inconsistentStars.add(tempStar);
                }

            }
            else if (qName.equalsIgnoreCase("stagename")) {
                checkNameInconsistencies(tempVal);
                tempStar.setName(tempVal);
            }
            else if (qName.equalsIgnoreCase("dob")) {
                checkDOBInconsistencies(tempVal);
                tempStar.setBirthYear(tempVal);
            }
        }
    }

    public void checkNameInconsistencies(String tempVal) {
        tempVal = tempVal.trim();

        if (tempVal.equals("")) {
            flag = 1;
            tempStar.setReason("Name is empty");
        }
        else if (tempVal.length() > 100) {
            flag = 1;
            tempStar.setReason("Name is too long");
        }
    }

    public void checkDOBInconsistencies(String tempVal) {
        tempVal = tempVal.trim();

        if (!tempVal.equals("") && !tempVal.matches("\\d+")) {
            flag = 1;
            tempStar.setReason("Year contains non-numeric characters");
        }
    }
}