import java.util.Objects;

public class Star {
    private String star_id;
    private String star_dbid;
    private String name;
    private String birth_year = "";
    private String reason;

    public Star() {
        this.star_id = "";
        this.star_dbid = "";
        this.name = "";
        this.birth_year = "";
        this.reason = "None";
    }

    public Star(String star_dbid, String name, String birth_year) {
        this.star_id = "";
        this.star_dbid = star_dbid;
        this.name = name;

        if (birth_year == null) {
            birth_year = "";
        }
        else {
            this.birth_year = birth_year;
        }

        this.reason = "None";
    }
    
    public void setName(String name){
        this.name = name;
    }

    public void setStarId(String star_id){
        this.star_id = star_id;
    }

    public void setStarDbid(String star_dbid){
        this.star_dbid = star_dbid;
    }
    
    public void setBirthYear(String birth_year){
        if (birth_year == null) {
            birth_year = "";
        }
        this.birth_year = birth_year;
    }

    public void setReason(String reason){
        this.reason = reason;
    }

    public String getStarId(){
        return star_id;
    }

    public String getStarDbid(){
        return star_dbid;
    }

    public String getName(){
        return name;
    }
    
    public String getBirthYear(){
        return birth_year;
    }

    public String getReason(){
        return reason;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("Star Id: " + getStarId() + ", ");
        sb.append("Star Dbid: " + getStarDbid() + ", ");
        sb.append("Star Name: " + getName() + ", ");
        sb.append("Birth Year: " + getBirthYear() + "\n");

        if (!getReason().equals("None")) {
            sb.append("Reason: " + getReason() + "\n");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (this.getClass() == otherObj.getClass()) {
            Star other = (Star) otherObj;

            return this.getName().equals(other.getName()) &&
                    this.getBirthYear().equals(other.getBirthYear());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getStarDbid());
    }

}