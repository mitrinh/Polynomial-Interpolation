
/**
 * Michael Trinh
 * CS 3010
 * Project 3
 * Description: Creates a divided difference table from the given data in a text
 *  file and uses that to create the interpolating polynomial. Prints the
 *  polynomial in both the Newton's form and Lagrange's form and simplified form.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Michael Trinh
 */
public final class PolynomialInterpolation {
    Scanner input;
    double[][] table;
    int numOfColumns;
    int numOfRows;
    // truncate to 3 decimal places to look cleaner
    DecimalFormat df = new DecimalFormat("0.000");
    
    public PolynomialInterpolation() throws FileNotFoundException {
        initializeTable();
        computeTable();
        printTable(table);
        System.out.println("\n Interpolating polynomial in Newton's Form is: ");
        printPolynomial(getNewtonForm());
        System.out.println("\n Interpolating polynomial in Lagrange's Form is: ");
        printPolynomial(getLagrangeForm());
        System.out.println("\n Simplified polynomial is: ");
        printPolynomial(getSimplifiedForm());
    }
    
    private void initializeTable() {
        String[] temp;
        System.out.print("Enter the filename including extension: ");
        input = new Scanner(System.in);
        File file = new File(input.nextLine());
        try (Scanner fileScanner = new Scanner(file)) {
            temp = fileScanner.nextLine().split("\\s+");
            table = new double[temp.length][];
            numOfColumns = temp.length+1;
            numOfRows = numOfColumns-1;
            int rowCount = 0;
            // creates the jegged array
            for(int i = numOfColumns; i > 1; i--){
                table[rowCount] = new double[i];
                rowCount++;
            }
            int column = 0;
            addLinetoTable(temp, column);
            column++;
            // adds any remaining data to table
            while (fileScanner.hasNextLine()) {
                temp = fileScanner.nextLine().split("\\s+");
                addLinetoTable(temp, column);
                column++;
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
    
    // adds the row of values to the table's column
    private void addLinetoTable(String[] row, int column) {
        for (int i = 0; i < row.length; i++) {
            table[i][column] = Double.parseDouble(row[i]);
        }
    }
    
    // computes the remaining values in the table
    private void computeTable() {
        // starts at two since x and f(x) should be initialized
        int column = 2;
        while(table.length > 2 && column < numOfColumns) {
            for(int j = 0; j < (numOfColumns-column); j++){
                // a3 = f[x3,x2,x1]-f[x2,x1,x0] / x3-x0
                // j+1+column-2 = the +1 offset plus iteration count = column-2
                table[j][column] = (table[j+1][column-1] - table[j][column-1]) / 
                        (table[j+1+column-2][0] - table[j][0]);
            }
            column++;
        }
    }
    
    private String[] getNewtonForm() {
        String[] newtonPolynomial = new String[numOfRows];
        // used to hold strings of (x-xi)
        String[] helperPolynomial = new String[numOfRows];
        helperPolynomial = getHelperPolynomial(helperPolynomial);
        newtonPolynomial[0] = df.format(table[0][1]);
        for(int i = 1; i < newtonPolynomial.length; i++){
            if(table[0][i+1] == 0) newtonPolynomial[i] = "+";
            else if(table[0][i+1] > 0) newtonPolynomial[i] = " + " + df.format(table[0][i+1]);
            else newtonPolynomial[i] = " - " + df.format(table[0][i+1]*-1);
            for(int j = 0; j < i; j++){
                newtonPolynomial[i] += helperPolynomial[j];
            }
        }
        return newtonPolynomial;
    }
    
    private String[] getHelperPolynomial(String[] helperPolynomial) {
        for(int i = 0; i < helperPolynomial.length; i++){
            if(table[i][0] == 0) helperPolynomial[i] = "x";
            else if(table[i][0] > 0) helperPolynomial[i] = "(x-"  + df.format(table[i][0]) + ")";
            else helperPolynomial[i] = "(x+" + df.format(table[i][0]*-1) + ")"; 
        }
        return helperPolynomial;
    }
    
    private String[] getLagrangeForm() {
        String[] lagrangePolynomial = new String[numOfRows];
        double tempProduct = 1;
        // used to hold strings of (x-xi)
        String[] helperPolynomial = new String[numOfRows];
        helperPolynomial = getHelperPolynomial(helperPolynomial);
        for(int i = 0; i < lagrangePolynomial.length;i++) {
            lagrangePolynomial[i] = "(";
            for(int j = 0; j < lagrangePolynomial.length; j++) {
                if(i != j) lagrangePolynomial[i] += helperPolynomial[j]; 
            }
            lagrangePolynomial[i] += "/";
            for(int j = 0; j < lagrangePolynomial.length; j++) {
                if(i != j) tempProduct *= (table[i][0] - table[j][0]);
            }
            lagrangePolynomial[i] += df.format(tempProduct) + ")(" + df.format(table[i][1]) + ") ";
            if(i != lagrangePolynomial.length-1) lagrangePolynomial[i] += "+ ";
            tempProduct = 1;
        }
        return lagrangePolynomial;
    }
    
    private String[] getSimplifiedForm() {
        String[] simplifiedPolynomial = new String[numOfRows];
        double[][] tempPolynomial = new double[numOfRows][numOfRows];
        // split the polynomial into like terms of different exponents 
        // where each row has the calculated coefficient for that exponent
        for(int i = 0; i < tempPolynomial.length; i++){
            Arrays.fill(tempPolynomial[i], 0);
            // multiply (x-xj) with the f[] coefficient and set the product to a cell 
            for(int j = 0; j < i; j++){
                tempPolynomial[i][j] = (table[0][i+1]*1) + (table[0][i+1]*table[j+1][0]*-1);
            }
        }
        double tempSum = 0;
        for(int i = 0; i < simplifiedPolynomial.length; i++){
            // add the row for each column as they are like terms
            for(int j = 0; j < tempPolynomial[i].length; j++){
                tempSum += tempPolynomial[j][i];
            }
            if(tempSum < 0) simplifiedPolynomial[i] = df.format(tempSum*-1);
            else simplifiedPolynomial[i] = df.format(tempSum);
            if(i != 0) simplifiedPolynomial[i] += "x^" + i;
            if(i != simplifiedPolynomial.length-1) {
                if(tempSum < 0) simplifiedPolynomial[i] += " - ";
                else simplifiedPolynomial[i] += " + ";
            }   
            tempSum = 0;
        }
        return simplifiedPolynomial;
    }
    
    public void printPolynomial(String[] polynomial){
        for (String factor : polynomial) {
            System.out.print(factor);
        }
        System.out.println("");
    }
    
    public void printTable(double[][] matrix) {
        for (double[] i : matrix) {
            for (double j : i) {
                System.out.print(df.format(j) + "  ");    
            }
            System.out.println("");
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Michael Trinh's Polynomial Interpolation");
        PolynomialInterpolation polyI = new PolynomialInterpolation();
    }
}
