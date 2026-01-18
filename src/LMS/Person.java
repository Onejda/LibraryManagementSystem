package LMS;

/**
 * Abstract base class for all persons in the library system.
 * Handles common attributes like ID, name, password, address, and phone number.
 */
public abstract class Person {

    protected int id;
    protected String password;
    protected String name;
    protected String address;
    protected int phoneNo;

    static int currentIdNumber = 0;

    public Person(int idNum, String name, String address, int phoneNum) {
        currentIdNumber++;

        if (idNum == -1) {
            id = currentIdNumber;
        } else {
            id = idNum;
        }

        password = Integer.toString(id);
        this.name = name;
        this.address = address;
        phoneNo = phoneNum;
    }

    /**
     * Prints information about this person
     */
    public void printInfo() {
        System.out.println("-----------------------------------------");
        System.out.println("\nThe details are: \n");
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("Phone No: " + phoneNo + "\n");
    }

    // Setter Methods
    public void setAddress(String a) {
        address = a;
        // Update in database
        DatabaseManager.getInstance().updatePerson(id, name, address, phoneNo);
    }

    public void setPhone(int p) {
        phoneNo = p;
        // Update in database
        DatabaseManager.getInstance().updatePerson(id, name, address, phoneNo);
    }

    public void setName(String n) {
        name = n;
        // Update in database
        DatabaseManager.getInstance().updatePerson(id, name, address, phoneNo);
    }

    // Getter Methods
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public int getPhoneNumber() {
        return phoneNo;
    }

    public int getID() {
        return id;
    }

    public static void setIDCount(int n) {
        currentIdNumber = n;
    }

    public static int getIDCount() {
        return currentIdNumber;
    }
}