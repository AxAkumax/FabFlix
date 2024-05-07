public class Star {
    private String name;
    private String birth_year;
    private String reason;

    public Star() {
        this.reason = "None";
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setBirthYear(String birth_year){
        this.birth_year = birth_year;
    }

    public void setReason(String reason){
        this.reason = reason;
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

        sb.append("Star Name: " + getName() + "\n");
        sb.append("Birth Year: " + getBirthYear() + "\n");

        return sb.toString();
    }

}