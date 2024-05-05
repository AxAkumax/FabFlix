import java.util.ArrayList;

public class Movie {
    private String title;
    private String year;
    private String director;
    private ArrayList<String> genres;

    public Movie() {
        this.genres = new ArrayList<String>();
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
        this.genres.add(genre);
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

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("Title: " + getTitle() + "\n");
        sb.append("Year: " + getYear() + "\n");
        sb.append("Director: " + getDirector() + "\n");
        sb.append("Genres: " + getGenres() + "\n");

        return sb.toString();
    }
}



