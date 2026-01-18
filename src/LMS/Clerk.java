package LMS;

/**
 * Clerk class representing library clerks.
 * Extends Staff with desk number information.
 */
public class Clerk extends Staff {

    public int deskNo;
    public static int currentDeskNumber = 0;

    public Clerk(int id, String n, String a, int ph, double s, int dk) {
        super(id, n, a, ph, s);

        if (dk == -1) {
            currentDeskNumber++;
            deskNo = currentDeskNumber;
        } else {
            deskNo = dk;
            if (dk > currentDeskNumber) {
                currentDeskNumber = dk;
            }
        }
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Desk Number: " + deskNo);
    }

    public int getDeskNo() {
        return deskNo;
    }

    public static void setDeskCount(int n) {
        currentDeskNumber = n;
    }

    /**
     * Saves this clerk to the database
     */
    public void saveToDatabase() {
        DatabaseManager db = DatabaseManager.getInstance();
        db.insertPersonWithId(id, name, password, address, phoneNo);
        db.insertStaff(id, "Clerk", salary);
        db.insertClerk(id, deskNo);
    }
}