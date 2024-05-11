import java.util.ArrayList;
import java.util.Objects;

public class Movie {
    private String fid;
    private String dbId;
    private String title;
    private String year;
    private String director;
    private String reason;
    private ArrayList<String> genres;

    public Movie() {
        this.fid = "";
        this.dbId = "";
        this.title = "";
        this.year = "";
        this.director = "";
        this.reason = "None";
        this.genres = new ArrayList<String>();
    }

    public Movie(String dbid, String title, String year, String director) {
        this.fid = "";
        this.dbId = dbid;
        this.title = title;
        this.year = year;
        this.director = director;
        this.reason = "None";

        this.genres = new ArrayList<String>();
    }

    public void setFID(String fid) {
        this.fid = fid.toLowerCase();
    }

    public void setDBID(String dbId) {
        this.dbId = dbId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setGenres(String genre) {
        if (genre != null) {
            this.genres.add(genre);
        }
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMovieFID() {
        return fid;
    }

    public String getMovieDBID() {
        return dbId;
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public String getYear() {
        return year;
    }

    public String getReason() {
        return reason;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("FID: " + getMovieFID() + ", ");
        sb.append("DBID: " + getMovieDBID() + ", ");
        sb.append("Title: " + getTitle() + ", ");
        sb.append("Year: " + getYear() + ", ");
        sb.append("Director: " + getDirector() + ", ");
        sb.append("Genres: " + getGenres() + "\n");

        if (!getReason().equals("None")) {
            sb.append("Reason: " + getReason() + "\n");
        }

        return sb.toString();
    }

    /**
     * Returns true if the given object is a Movie with the same instance
     * variable values as this one
     */
    @Override
    public boolean equals(Object otherObj) {
        //return false; // FIX ME

        if (this.getClass() == otherObj.getClass()) {
            Movie other = (Movie)otherObj;

            return (this.title.equals(other.getTitle()) &&
                    this.year.equals(other.getYear()) &&
                    this.director.equals(other.getDirector()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dbId);
    }
}



