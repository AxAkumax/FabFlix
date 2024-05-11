import java.util.Objects;

public class StarInMovie {
    String movie_fid;
    String star_name;
    String movie_name;
    String director;
    String reason;

    StarInMovie() {

        this.director = "";
        this.reason = "None";
    }

    StarInMovie(String movie_fid, String star_name, String movie_name, String director) {
        this.movie_fid = movie_fid.toLowerCase();
        this.star_name = star_name;
        this.movie_name = movie_name;
        this.director = director;
        this.reason = "None";
    }

    public void setMovieFID(String movie_fid) {
        this.movie_fid = movie_fid.toLowerCase();
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

    public String getMovieFID() {
        return movie_fid;
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

        sb.append("FID: " + getMovieFID() + ", ");
        sb.append("Star Name: " + getStarName() + ", ");
        sb.append("Movie Name: " + getMovieName() + ", ");
        sb.append("Director: " + getDirector() + "\n");

        if (!getReason().equals("None")) {
            sb.append("Reason: " + getReason() + "\n");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (this.getClass() == otherObj.getClass()) {
            StarInMovie other = (StarInMovie) otherObj;

            return this.getMovieFID().equals(other.getMovieFID()) &&
                    this.getStarName().equals(other.getStarName());

//            return this.getMovieName().equals(other.getMovieName()) &&
//                    this.getStarName().equals(other.getStarName()) &&
//                    this.getDirector().equals(other.getDirector());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMovieFID(), getStarName());
//        return Objects.hash(this.getMovieName(), this.getStarName(), this.getDirector());
    }


}
