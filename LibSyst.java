import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class LibSyst {

    public static void main(String[] args) {
        String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db20";
        String dbUsername = "Group20";
        String dbPassword = "31703170";

        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
            System.out.println("Welcome to library inquery system!");
            menu(con);
        } catch (ClassNotFoundException e) {
            System.out.println("[Error]: Java MySQL DB Driver not found!!");
            System.exit(1);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void menu(Connection con) {
        System.out.println("\n-----Main Menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Operations for administrator");
        System.out.println("2. Operations for library user");
        System.out.println("3. Operations for librarian");
        System.out.println("4. Operations for library director");
        System.out.println("5. Exit this program");

        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 5) {
                System.out.print("Enter Your Choice: ");
                input = scan.nextInt();
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
            menu(con);
        }

        if (input == 1)
            adminOperate(con);
        else if (input == 2)
            userOperate(con);
        else if (input == 3)
            librarianOperate(con);
        else if (input == 4)
            libDirectorOperate(con);
        else if (input == 5) {
            System.out.println("Exit");
            System.exit(1);
        }

    }

    /* CODES BELOW ------- adminOperate ------ */

    public static void adminOperate(Connection con) {
        System.out.println("\n-----Operations for administrator menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Create all tables");
        System.out.println("2. Delete all tables");
        System.out.println("3. Load from datafile");
        System.out.println("4. Show number of records in each table");
        System.out.println("5. Return to the main menu");
        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 5) {
                System.out.print("Enter Your Choice: ");
                input = scan.nextInt();
            }
            if (input == 1) createAllTables(con);
            else if (input == 2) deleteAllTable(con);
            else if (input == 3) loadData(con);
            else if (input == 4) showRecords(con);
            else menu(con);
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
        adminOperate(con);
    }

    public static void createAllTables(Connection con) {
        System.out.print("Processing...");
        try {
            Statement stmt = con.createStatement();

            //Create category table
            String category = "CREATE TABLE category(cid integer primary key,"
                    + "max integer not null," + "period integer not null);";
            stmt.executeUpdate(category); // Execute SQL QUERY

            //Create libuser table
            String libuser = "CREATE TABLE libuser(libuid varchar(10) primary key,"
                    + "name varchar(25) not null," + "address varchar(100) not null,"
                    + "cid integer not null," + "FOREIGN KEY(cid) REFERENCES category(cid));";
            stmt.executeUpdate(libuser); // Execute SQL QUERY

            //Create book table
            String book = "CREATE TABLE book(callnum varchar(8) primary key,"
                    + "title varchar(30) not null," + "publish varchar(10));";
            stmt.executeUpdate(book);// Execute SQL QUERY

            //Create copy table
            String copy = "CREATE TABLE copy(callnum varchar(8) not null,"
                    + "copynum integer not null," + "PRIMARY KEY(callnum, copynum),"
                    + "FOREIGN KEY(callnum) REFERENCES book(callnum));";
            stmt.executeUpdate(copy);// Execute SQL QUERY

            //Create borrow table
            String borrow = "CREATE TABLE borrow(libuid varchar(10) not null,"
                    + "callnum varchar(8) not null," + "copynum integer not null,"
                    + "checkout_date varchar(10) not null," + "return_date varchar(10),"
                    + "PRIMARY KEY(libuid, callnum, copynum, checkout_date),"
                    + "FOREIGN KEY(libuid) REFERENCES libuser(libuid),"
                    + "FOREIGN KEY(callnum, copynum) REFERENCES copy(callnum, copynum));";
            stmt.executeUpdate(borrow);// Execute SQL QUERY

            //Create authorship table
            String authorship = "CREATE TABLE authorship(aname varchar(25) not null,"
                    + "callnum varchar(8) not null,"
                    + "PRIMARY KEY(aname, callnum),"
                    + "FOREIGN KEY(callnum) REFERENCES book(callnum));";
            stmt.executeUpdate(authorship);// Execute SQL QUERY
            System.out.println("Done! Database is initialized!");
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }

    }

    public static void deleteAllTable(Connection con) {
        System.out.println("Processing...");
        String[] tables = {"category", "libuser", "book", "copy", "borrow", "authorship"};
        String sqlDrop = "DROP TABLE IF EXISTS ";

        String[] tableDropList = new String[tables.length];
        for (int i = 0; i < tables.length; i++) {
            tableDropList[i] = sqlDrop + tables[i]; //SQL: DROP TABLE IF EXISTS [tablesnames];
        }

        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("SET foreign_key_checks = 0"); //DEL all tables and NO foreign key checks
            for (int i = 0; i < tables.length; i++) {
                stmt.executeUpdate(tableDropList[i]);
            }
            stmt.executeUpdate("SET foreign_key_checks = 1"); //
            System.out.println("Done! Database is removed!");
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }

    }

    public static void loadData(Connection con) throws Exception {
        System.out.print("Type in the Source Data Folder Path: ");

        Scanner scan = new Scanner(System.in);
        String path = scan.nextLine();
        System.out.print("Processing...");

        /* category table */
        try {
            File file = new File(path + "/" + "category.txt");
            Scanner scanCategory = new Scanner(file);
            String sqlInsert = "INSERT INTO category VALUES (?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sqlInsert);

            while (scanCategory.hasNextLine()) {
                String rows = scanCategory.nextLine();
                String[] data = rows.split("\t");

                for (int i = 0; i < data.length; i++) {
                    pstmt.setInt(i + 1, Integer.parseInt(data[i]));
                }
                pstmt.execute();
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }

        /* libusers table */
        try {
            File file = new File(path + "/" + "user.txt");
            Scanner scanUser = new Scanner(file);
            String sqlInsert = "INSERT INTO libuser VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sqlInsert);

            while (scanUser.hasNextLine()) {
                String rows = scanUser.nextLine();
                String[] data = rows.split("\t");

                int i;
                for (i = 0; i < data.length - 1; i++) {
                    pstmt.setString(i + 1, data[i]);
                }
                pstmt.setInt(i + 1, Integer.parseInt(data[i]));
                pstmt.execute();
            }
        } catch (Exception e) {
            System.out.println("libusers[Error]: " + e);
        }

        /* book table + authorship table + copy table*/
        try {
            File file = new File(path + "/" + "book.txt");
            Scanner scanBook = new Scanner(file);
            PreparedStatement pstmtOfBook = con.prepareStatement("INSERT INTO book VALUES (?, ?, ?)");//[0], [2], [4]
            PreparedStatement pstmtOfAuthorship = con.prepareStatement("INSERT INTO authorship VALUES (?, ?)");//[3]!spl, [0]
            PreparedStatement pstmtOfCopy = con.prepareStatement("INSERT INTO copy VALUES (?, ?)");//[0], [1]

            while (scanBook.hasNextLine()) {
                String rows = scanBook.nextLine();
                String[] data = rows.split("\t");
                String[] authorName = data[3].split(",");

                pstmtOfBook.setString(1, data[0]);
                pstmtOfBook.setString(2, data[2]);
                pstmtOfBook.setString(3, data[4]);
                pstmtOfBook.execute();

                pstmtOfAuthorship.setString(2, data[0]);
                for (int i = 0; i < authorName.length; i++) {
                    pstmtOfAuthorship.setString(1, authorName[i]);
                    pstmtOfAuthorship.execute();
                }

                pstmtOfCopy.setString(1, data[0]);
                for (int i = 0; i < Integer.parseInt(data[1]); i++) {
                    pstmtOfCopy.setInt(2, i + 1);
                    pstmtOfCopy.execute();
                }

            }
        } catch (Exception e) {
            System.out.println("book [Error]: " + e);
        }

        /* borrow table*/
        try {
            File file = new File(path + "/" + "check_out.txt");
            Scanner scanCheckOut = new Scanner(file);
            PreparedStatement pstmtOfBorrow = con.prepareStatement("INSERT INTO borrow VALUES (?, ?, ?, ?, ?)");//[2], [0], [1// ], [3], [4]

            while (scanCheckOut.hasNextLine()) {
                String rows = scanCheckOut.nextLine();
                String[] data = rows.split("\t");

                pstmtOfBorrow.setString(1, data[2]);
                pstmtOfBorrow.setString(2, data[0]);
                pstmtOfBorrow.setInt(3, Integer.parseInt(data[1]));
                pstmtOfBorrow.setString(4, data[3]);
                if (data[4].equals("null"))
                    pstmtOfBorrow.setNull(5, Types.NULL);
                else
                    pstmtOfBorrow.setString(5, data[4]);

                pstmtOfBorrow.execute();
            }

        } catch (Exception e) {
            System.out.println("borrow[Error]: " + e);
        }
        System.out.println("Done! Data is inputted to the database!");
    }

    public static void showRecords(Connection con) {
        System.out.println("\nNumber of records in each table: ");
        String sqlCount = "SELECT COUNT(*) FROM ";
        String[] tables = {"category", "libuser", "book", "copy", "borrow", "authorship"};

        String[] countTableRec = new String[tables.length];
        for (int i = 0; i < tables.length; i++) {
            countTableRec[i] = sqlCount + tables[i]; //SQL: DROP TABLE IF EXISTS [tablesnames];
        }

        try {
            Statement stmt = con.createStatement();
            for (int i = 0; i < tables.length; i++) {
                ResultSet rs = stmt.executeQuery(countTableRec[i]);
                rs.next();
                int count = rs.getInt("COUNT(*)");
                System.out.println("\033[3m" + tables[i] + "\033[0m" + ": " + count); //print in italic font
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
    }

    /* CODES ABOVE ------- adminOperate ------ */

    public static void userOperate(Connection con) {
        System.out.println("\n-----Operations for library user menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Search for books");
        System.out.println("2. Show checkout records of a user");
        System.out.println("3. Return to the main menu");
        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 3) {
                System.out.print("Enter Your Choice: ");
                input = scan.nextInt();
            }
            if (input == 1) {
                searchBook(con);
            }else if(input == 2){
                showAllCheckOutRec(con);
            }else if(input == 3){
                menu(con);
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
        userOperate(con);
    }

    public static void searchBook(Connection con) {
        System.out.println("Choose the search criteria:");
        System.out.println("1. Call Number");
        System.out.println("2. Title");
        System.out.println("3. Author");
        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 3) {
                System.out.print("Enter Your Choice: ");
                Scanner scan2 = new Scanner(System.in);
                input = scan2.nextInt();
            }
            String sqlFindByCallNum = "";
            String sqlFindLentCopy = "";
            String sqlFindNumOfCopies = "";

            System.out.print("Type in the search keyword: ");
            String[] callNum = null;

            if (input == 1) { //Find by callNum
                String callNumToSearch =  scan.nextLine();
                sqlFindByCallNum = "SELECT callnum " +
                        "FROM book " +
                        "WHERE book.callnum LIKE BINARY '%" + callNumToSearch + "%' ";
                Statement stmt = con.createStatement();
                ResultSet rsCallNum = stmt.executeQuery(sqlFindByCallNum);

                rsCallNum.last();
                int numRows = rsCallNum.getRow();

                if(numRows >= 1) {
                    callNum = new String[1];
                    callNum[0] = scan.nextLine();
                }
            }
            else if(input == 2) {
                String title =  scan.nextLine();
                sqlFindByCallNum = "SELECT callnum " +
                        "FROM book " +
                        "WHERE book.title LIKE BINARY '%" + title + "%' " +
                        "ORDER BY callnum;";
                Statement stmt = con.createStatement();
                ResultSet rsCallNum = stmt.executeQuery(sqlFindByCallNum);

                rsCallNum.last();
                int numRows = rsCallNum.getRow();
                String[] temp = new String[numRows];
                rsCallNum.first();

                if(numRows >= 1) {
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = rsCallNum.getString(1);
                        rsCallNum.next();
                    }
                    callNum = temp.clone();
                }
            }
            else if(input == 3){
                String author =  scan.nextLine();
                sqlFindByCallNum = "SELECT callnum " +
                        "FROM authorship " +
                        "WHERE authorship.aname LIKE BINARY '%" + author + "%' " +
                        "ORDER BY callnum;";
                Statement stmt = con.createStatement();
                ResultSet rsCallNum = stmt.executeQuery(sqlFindByCallNum);

                rsCallNum.last();
                int numRows = rsCallNum.getRow();
                String[] temp = new String[numRows];
                rsCallNum.first();

                if(numRows >= 1) {
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = rsCallNum.getString(1);
                        rsCallNum.next();
                    }
                    callNum = temp.clone();
                }
            }

            if(callNum == null){
                System.out.println("No book matches your keyword!");
                System.out.println("End of Query");
                userOperate(con);
            }
            System.out.println("|Call Num|Title|Author|Available No. of Copy|");
            for(int i = 0 ; i < callNum.length; i++){
                sqlFindByCallNum = "SELECT book.callnum, book.title, authorship.aname " +
                        "FROM authorship inner join book " +
                        "ON authorship.callnum = book.callnum " +
                        "WHERE authorship.callnum = '" + callNum[i] + "';";

                sqlFindLentCopy = "SELECT COUNT(*) FROM borrow "
                        + "WHERE borrow.callnum = '" + callNum[i] + "';";

                sqlFindNumOfCopies = "SELECT COUNT(*) FROM copy "
                        + "WHERE copy.callnum = '" + callNum[i] + "';";

                System.out.print("|" + callNum[i] + "|");

                Statement stmt = con.createStatement();
                ResultSet rsCallNum = stmt.executeQuery(sqlFindByCallNum);

                rsCallNum.first();
                String title = rsCallNum.getString(2);
                System.out.print("|" + title + "|");

                while (true) {
                    String aname = rsCallNum.getString(3);
                    System.out.print(aname);
                    if (!rsCallNum.next()) break;
                    else System.out.print(", ");
                }

                stmt = con.createStatement();
                ResultSet rsNumCopies = stmt.executeQuery(sqlFindNumOfCopies);
                rsNumCopies.first();
                int numCopies = rsNumCopies.getInt(1);

                stmt = con.createStatement();
                ResultSet rsFindLentCopy = stmt.executeQuery(sqlFindLentCopy);
                rsFindLentCopy.first();
                int UnavailableCopies = rsFindLentCopy.getInt(1);

                int avaCopies = numCopies - UnavailableCopies;
                System.out.println("|" + avaCopies + "|");
            }
        } catch (Exception e) {
            System.out.println("[Warning]: THE CODE SHOULD CONTAINS 7 DIGITS AND 1 CAPITAL LETTER ");
            System.out.println("[Error]: " + e);
        }
        System.out.println("End of Query");
        userOperate(con);
    }

    public static void showAllCheckOutRec(Connection con) throws SQLException {
        System.out.print("Enter the User ID: ");
        Scanner scan = new Scanner(System.in);
        String libUserID = scan.nextLine();
        String sqlShowAllRec = "SELECT callnum, copynum, checkout_date, IF(return_date IS NULL, 'No', 'Yes') " +
                               "FROM borrow " +
                               "WHERE libuid = '" + libUserID + "' " +
                               "ORDER BY STR_TO_DATE(checkout_date, \"%d/%m/%Y\") desc; ";

        String sqlGetTitle = "SELECT title " +
                             "FROM book " +
                             "WHERE callnum IN ( SELECT callnum FROM borrow " +
                                                "WHERE libuid = '"+libUserID+"' " +
                                                "ORDER BY STR_TO_DATE(checkout_date, \"%d/%m/%Y\") desc)";

        try {
            Statement stmt = con.createStatement();
            Statement stmt2 = con.createStatement();
            Statement stmt3 = con.createStatement();

            ResultSet rsGetBorrow = stmt.executeQuery(sqlShowAllRec);
            ResultSet rsGetTitle = stmt2.executeQuery(sqlGetTitle);

            rsGetBorrow.last();
            int numRows = rsGetBorrow.getRow();
            rsGetBorrow.beforeFirst();
            rsGetTitle.beforeFirst();

            if(numRows == 0){
                System.out.println("No matches of this user");
                userOperate(con);
            }

            System.out.println("Loan Record:");
            System.out.println("|CallNum|CopyNum|Title|Author|Check-out|Returned?|");
            for(int i = 0 ; i < numRows; i++){
                rsGetTitle.next();
                rsGetBorrow.next();
                String callNum = rsGetBorrow.getString(1);

                System.out.print("|" + callNum);
                System.out.print("|" + rsGetBorrow.getInt(2));
                System.out.print("|" + rsGetTitle.getString(1) + "|");


                String sqlGetAuthor = "SELECT aname "+
                        "FROM authorship " +
                        "WHERE callnum = '"+ callNum + "'";
                ResultSet rsGetAuthor = stmt3.executeQuery(sqlGetAuthor);

                rsGetAuthor.first();
                while (true) {
                    String aname = rsGetAuthor.getString(1);
                    System.out.print(aname);
                    if (!rsGetAuthor.next()) break;
                    else System.out.print(", ");
                }

                System.out.print("|" + rsGetBorrow.getString(3));
                System.out.println("|" + rsGetBorrow.getString(4)+ "|");
            }
            System.out.println("End Of Query");

        } catch (Exception e){
            System.out.println("[Error]: " + e);
        }

    }


    public static void librarianOperate(Connection con) {
        System.out.println("\n-----Operations for librarian menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Book Borrowing");
        System.out.println("2. Book Returning");
        System.out.println("3. Return to the main menu");
        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 3) {
                System.out.print("Enter Your Choice: ");
                input = scan.nextInt();
            }
            if (input == 1) {
                borrowBook(con);
            }else if(input == 2){
                returnBook(con);
            }else if(input == 3){
                menu(con);
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
        librarianOperate(con);

    }

    public static void borrowBook(Connection con){
        Scanner scan = new Scanner(System.in);
        try {
            System.out.print("Enter The User ID: ");
            String libUID = scan.nextLine();
            System.out.print("Enter The Call Number: ");
            String callNum = scan.nextLine();
            System.out.print("Enter The Copy Number: ");
            int copyNum = scan.nextInt();

            String sqlCheckAva = "SELECT IF(return_date IS NULL, 'No', 'Yes') " +
                                 "From borrow WHERE callnum = '" + callNum+ "' AND " +
                                 " copynum= " + copyNum + ";";

            String sqlBorrow = "INSERT INTO borrow VALUE (?, ?, ?, ?, ?)";

            Statement stmt = con.createStatement();
            ResultSet checkAva = stmt.executeQuery(sqlCheckAva);
            try{
                while(checkAva.next()) {
                        if("No" == checkAva.getString(1));
                        System.out.println("[Error]: The Book \"call Number:" + callNum + ", copy number: " + copyNum + "\" is not available");
                        librarianOperate(con);
                    }
            } catch (Exception e){
                System.out.println("Check availability failed");
                System.out.println("[Error]:" + e);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.now();

            PreparedStatement pstmt = con.prepareStatement(sqlBorrow);
            pstmt.setString(1, libUID);
            pstmt.setString(2, callNum);
            pstmt.setInt(3, copyNum);
            pstmt.setString(4, dtf.format(localDate));
            pstmt.setNull(5, Types.NULL);
            pstmt.executeUpdate();
            System.out.println("Book borrowing performed successfully!!!");

        } catch (Exception e) {
            System.out.println("[Error]: Borrow opertaion fail maybe the input is wrong");
            System.out.println("[Error]: " + e);
        }
        librarianOperate(con);
    }

    public static void returnBook(Connection con){
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter The User ID: ");
        String libUID = scan.nextLine();
        System.out.print("Enter The Call Number: ");
        String callNum = scan.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copyNum = scan.nextInt();

        String sqlCheck = "SELECT libuid, callnum, copynum " +
                          "FROM borrow " +
                          "WHERE libuid = '" + libUID +"' " +
                          "AND callnum = '" + callNum +"' " +
                          "AND copynum = " + copyNum +" ";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlCheck);
            if (rs.next()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.now();

                String sqlReturn = "UPDATE borrow SET return_date = '" + dtf.format(localDate) + "' " +
                        "WHERE libuid = '" + libUID +"' " +
                        "AND callnum = '" + callNum +"' " +
                        "AND copynum = " + copyNum +" ;";

                stmt.execute(sqlReturn);
                System.out.println("Book returning performed successfully!!!");
            } else {
                System.out.println("[Error]: The record doesn't exist!");
            }
        } catch (Exception e) {
            System.out.println("[Error]: "+ e);
        }
    }

    public static void libDirectorOperate(Connection con) {
        System.out.println("\n-----Operations for library director menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. List all un-returned book copies which are checked-out within a period");
        System.out.println("2. Return to the main menu");

        Scanner scan = new Scanner(System.in);
        int input = 0;
        try {
            while (input < 1 || input > 5) {
                System.out.print("Enter Your Choice: ");
                input = scan.nextInt();
            }

            if (input == 1) findUnreturnedBook(con);
            else if (input == 2) menu(con);
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }

    }

    public static void findUnreturnedBook(Connection con) {
        String startDate, endDate;
        Scanner scan = new Scanner(System.in);
        System.out.print("Type in the starting date [DD/MM/YYYY]: ");
        startDate = scan.nextLine();
        System.out.print("Type in the ending date [DD/MM/YYYY]: ");
        endDate = scan.nextLine();

        String sqlGetUnreturnBook = "select libuid, callnum, copynum, checkout_date "
                + "FROM borrow WHERE return_date IS NULL "
                + "AND STR_TO_DATE(checkout_date, \"%d/%m/%Y\") >= STR_TO_DATE(\"" + startDate + "\", \"%d/%m/%Y\")"
                + "AND STR_TO_DATE(checkout_date, \"%d/%m/%Y\") <= STR_TO_DATE(\"" + endDate + "\", \"%d/%m/%Y\")"
                + "order by STR_TO_DATE(checkout_date, \"%d/%m/%Y\") desc;";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlGetUnreturnBook);
            rs.last();
            int numRows = rs.getRow();
            rs.beforeFirst();

            System.out.println("|LibUID|CallNum|CopyNum|Checkout|");
            for (int i = 0; i < numRows; i++) {
                rs.next();
                String libuid = rs.getString(1);
                String callnum = rs.getString(2);
                int copynum = rs.getInt(3);
                String checkOutDate = rs.getString(4);

                System.out.println("|" + libuid + "|" + callnum + "|" + copynum + "|" + checkOutDate + "|");
            }
        } catch (Exception e) {
            System.out.println("[Error]: " + e);
        }
    }
}
