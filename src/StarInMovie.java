public class StarInMovie {
    String star_name;
    String movie_name;
    String director;
    String reason;

    StarInMovie() {
        this.reason = "None";
    }

    StarInMovie(String star_name, String movie_name, String director) {
        this.star_name = star_name;
        this.movie_name = movie_name;
        this.director = director;
    }

    public void setStarName(String star_name) {
        this.star_name = star_name;
    }

    public void setMovieName(String movie_name) {
        this.movie_name = movie_name;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStarName() {
        return star_name;
    }

    public String getMovieName() {
        return movie_name;
    }

    public String getDirector() {
        return director;
    }

    public String getReason() {
        return reason;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("Star Name: " + getStarName() + "\n");
        sb.append("Movie Name: " + getMovieName() + "\n");
        sb.append("Director: " + getDirector() + "\n");

        return sb.toString();
    }

}
