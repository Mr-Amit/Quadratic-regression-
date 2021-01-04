import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.lang.Math;
import java.util.Scanner; 
import java.io.*;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;


public class App {
    public static void main(String args[]) throws Exception {
        Coeffs a = new Coeffs();
        double[][] ans = new double[10][8];
        double[] xvals = new double[1200];
        double[] yvals = new double[1200];


        try {
     
            // Put the input.txt in the desired location 
            File fileObj = new File("C:\\Users\\lenovo\\Downloads\\input.txt");
            // This input file contains all the data value pairs from the Data.csv file
            Scanner fileReader = new Scanner(fileObj);
            int i = 0;
            while (fileReader.hasNextLine()) {
                String dataCollected = fileReader.nextLine();
                String[] dataArr = dataCollected.split("\t");
                xvals[i] = Double.parseDouble(dataArr[0]);
                yvals[i] = Double.parseDouble(dataArr[1]);
                i += 1;
            }
            fileReader.close();
            } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        Scanner input = new Scanner (System.in);  
        System.out.println("Enter the allowed max failed prediction : ");  
        int maxFailedPred = input.nextInt();

        System.out.println("Enter the the total tolerance: ");  
        double tolerance = input.nextDouble();  
        
        System.out.print("Enter the round of digits: ");      
        int significantDigits = input.nextInt();       

        // The amount of data I want to take from the text file
        for(int i = 0; i < 300; i ++){

            
//   getCoeffs(double x, double y, double tolerance, int maxFailedPred, int significantDigits){

            ans = a.getCoeffs(xvals[i], yvals[i], tolerance, maxFailedPred, significantDigits);                 //Function call to get Coefficients
            print2D(ans);
        }
        // To write the values that are not written till now
        if(a.xny.size() != 0){
            a.writeXnCalYtoFile(a.xny);
        }
    }
    // Function to prind the 2-Dimensional Matrix
    static void print2D(double mat[][]) throws Exception{ 
        int flag = 0;
        for (int i = 0; i < mat.length; i++){
  
            System.out.print("[");
            for (int j = 0; j < 8; j++){ 
                if(mat[i][0] != 0 || mat[i][1] !=0)
                    if(j < 7)
                        System.out.print(mat[i][j] + ", "); 
                    else
                        System.out.print(mat[i][j]); 
                else{
                    //To remember it has reached the end 
                    flag = 1;
                }
            }

            System.out.println("]");
            if (flag == 1){     // break when the end is reached
                break;
            }
            
        }
        System.out.println("\n");
    }

}

// To store every x and y value pairs in the same object

class XnY{
    double x, y;
    double yCalc;
    XnY(double X, double Y){
        x = X;
        y = Y;
    }
    double getValue(int i){
        // return x for 0 and y for 1
        if(i == 0){
            return x;
        }
        else{
            return y;
        }
    }
    void setValue(double yCalc){
        this.yCalc = yCalc;
    }
    double getValue(){
        return yCalc;
    }
}

class Coeffs{

    ArrayList<XnY> xny; 
    double[][] arr; 

    // rowPointer is a Pointer to the current Row in the 2 dimensional matrix
    int rowPointer;
    WeightedObservedPoints obs;
    int prev;
    double[] prevCoeffs;

    // isObsNotEmpty is used as a flag to see if it is used ever
    int isObsNotEmpty;

    // Constructor
    Coeffs(){
        rowPointer = 0;
        obs = new WeightedObservedPoints();
        prev = 0;
        isObsNotEmpty = 0;
        xny =new ArrayList<XnY>();
        arr = new double[10][8];                
        prevCoeffs = new double[3];
        
    }
    


    // The Required Function
    public double[][] getCoeffs(double x, double y, double tolerance, int maxFailedPred, int significantDigits){
        //fitter is the default curve fitter
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        if(isObsNotEmpty != 0){
            prevCoeffs = fitter.fit(obs.toList());
        }
        isObsNotEmpty = 1;
        x = Math.round (x * 10000.0) / 10000.0;
        y = Math.round (y * 10000.0) / 10000.0;
        obs.add(x,y);
        isObsNotEmpty = 1;
        xny.add(new XnY(x, y));
        double[] coeffs = fitter.fit(obs.toList()); 
        if(xny.size() < 6){
            // My implementation of your virtual point
            if(coeffs[2] > 0 && xny.size() > 2){
                double xprev = x; 
                double avgDist = (x- xny.get(xny.size() - 2).getValue(0))/2;
                double yprev = y; 
                x = xprev + avgDist;
                y = yprev/2;
                x = Math.round (x * 10000.0) / 10000.0;
                y = Math.round (y * 10000.0) / 10000.0;
                obs.add(x, y);
                xny.add(new XnY(x, y));
            }
            coeffs = fitter.fit(obs.toList());
            updateRow(coeffs, significantDigits);
            return arr;
        }
        else{  
            // Here is what you prescribed for tolerance but that was giving way off the top values
            // tolerance = Math.abs(1/(40 * coeffs[2]))
            int FailedPred = FailiureCalc(coeffs, tolerance);
            if(maxFailedPred > FailedPred){
                if(coeffs[2] > 0 && xny.size() > 2){    //Virtual point 
                    double xprev = x; 
                    double avgDist = (x- xny.get(xny.size() - 2).getValue(0))/2;
                    double yprev = y; 
                    x = xprev + avgDist;
                    y = yprev/2;
                    x = Math.round (x * 10000.0) / 10000.0;
                    y = Math.round (y * 10000.0) / 10000.0;
                    obs.add(x, y);
                    xny.add(new XnY(x, y));
                }
                coeffs = fitter.fit(obs.toList());
                updateRow(coeffs, significantDigits);
                return arr;
            }
            else{
                if(prevCoeffs[2] > 0 && xny.size() > 2){
                    double xprev = xny.get(xny.size() - 2).getValue(0); 
                    double avgDist = (xprev - xny.get(xny.size() - 4).getValue(0))/2;
                    double yprev = y; 
                    double x_ = xprev + avgDist;
                    double y_ = yprev/2;
                    x_ = Math.round (x_ * 10000.0) / 10000.0;
                    y_ = Math.round (y_ * 10000.0) / 10000.0;
                    obs.add(x_, y_);
                    xny.add(new XnY(x_, y_));
                }

                updateRow(prevCoeffs, significantDigits);
                rowPointer += 1;
                obs.clear();
                writeXnCalYtoFile(xny);
                xny.clear();
                obs.add(x, y);
                xny.add(new XnY(x, y));
                return arr;
            }
        }
    }
    int whereIsThePointInTheParabola(double x, double yCalculated, double a, double b, double c){
        double h=-1*b / (2*a);
        if (x<h) 
            return -1;
        if (x==h)
            return 0;
        return 1;
        }

    double slope(double x, double a, double b){
            return 2*a*x+b;
        }

    void updateRow(double[] coeffs, int significantDigits){
        rowPointer = (rowPointer)%10;
        double x = xny.get(0).getValue(0);
        arr[rowPointer][0] = x;
        x = xny.get(xny.size() -1).getValue(0);
        System.out.println("The size : " + xny.size());                             // Prints the number of points used
        arr[rowPointer][1] = xny.get(xny.size() -1).getValue(0);
        arr[rowPointer][2] = RoundOf(coeffs[2], significantDigits);
        arr[rowPointer][3] = RoundOf(coeffs[1], significantDigits);
        arr[rowPointer][4] = RoundOf(coeffs[0], significantDigits);

        double y = coeffs[2]*x*x + coeffs[1]*x + coeffs[0];
        
        arr[rowPointer][5] = RoundOf(y, significantDigits); 
        arr[rowPointer][6] = RoundOf(whereIsThePointInTheParabola(x, y, arr[rowPointer][2], arr[rowPointer][3], arr[rowPointer][4]), significantDigits);
        arr[rowPointer][7] = RoundOf(slope(x, arr[rowPointer][2], arr[rowPointer][3]), significantDigits);
        updateXnYcalc(xny, coeffs);
    }

    double predictY(double [] coeffs, double x){
        return coeffs[2]*x*x + coeffs[1]*x + coeffs[1];
    }

    int FailiureCalc(double[] coeffs, double tolerance){

        double calcY;
        int FailedPred = 0;
        
        for (int i=0; i< xny.size(); i++) {
            
            calcY = predictY(coeffs, xny.get(i).getValue(0));
            double actualValue = xny.get(i).getValue(1);

            ////////////////////////////////                 LOOK HERE           \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
            // The condition is like this because am allowing more points to under the graph then above it
            if(actualValue > calcY + tolerance || actualValue < calcY - 3*tolerance){
                FailedPred += 1;
            }
        }
        return FailedPred;
    }

    double RoundOf(double num, int significantDigits){
        return Math.round(num*Math.pow(10,significantDigits))/Math.pow(10,significantDigits);
    }

    void updateXnYcalc(ArrayList<XnY> xny, double[] coeffs){
        double yCalc;
        double x;
        for(int i=0; i < xny.size(); i++){
            x = xny.get(i).getValue(0);
            yCalc = coeffs[2]*x*x + coeffs[1]*x + coeffs[0];
            xny.get(i).setValue(yCalc);
        }

    }
    void writeXnCalYtoFile(ArrayList<XnY> xny){
        try{    
            FileWriter fw=new FileWriter("D:\\testout.txt", true);  
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);  
            double yCalc;
            double x;
            for(int i=0; i < xny.size(); i++){
                x = xny.get(i).getValue(0);
                yCalc = xny.get(i).yCalc;
                out.write(x + " " + yCalc + "\n");   
            } 

        out.close();
        bw.close();
        fw.close();    
        }
        catch(Exception e){System.out.println(e);}    
    }   
}
