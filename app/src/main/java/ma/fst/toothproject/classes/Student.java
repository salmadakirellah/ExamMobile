package ma.fst.toothproject.classes;
public class Student {
    private int id;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String image;
    private String number;

    // Constructors

    public Student() {
    }

    public Student(int id, String userName, String password, String firstName, String lastName, String image, String number) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        this.number = number;
    }
    public Student(String userName, String password  ) {

        this.userName = userName;
        this.password = password;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
