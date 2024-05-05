public class Star {
    private String name;
    private String birth_year;

    public Star() {
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setBirthYear(String birth_year){
        this.birth_year = birth_year;
    }
    
    public String getName(){
        return name;
    }
    
    public String getBirthYear(){
        return birth_year;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("Star Name: " + getName() + "\n");
        sb.append("Birth Year: " + getBirthYear() + "\n");

        return sb.toString();
    }

}