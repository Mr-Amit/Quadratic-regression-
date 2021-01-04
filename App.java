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
            File myObj = new File("C:\\Users\\lenovo\\Downloads\\input.txt");
            // This input file contains all the data value pairs from the Data.csv file
            Scanner myReader = new Scanner(myObj);
            int i = 0;
            while (myReader.hasNextLine()) {
                String str = myReader.nextLine();
                String[] strs = str.split("\t");
                xvals[i] = Double.parseDouble(strs[0]);
                yvals[i] = Double.parseDouble(strs[1]);
                i += 1;
            }
            myReader.close();
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
        int sig_digs = input.nextInt();       

        // The amount of data I want to take from the text file
        for(int i = 0; i < 300; i ++){

            
//   getCoeffs(double x, double y, double tolerance, int maxFailedPred, int sig_dig){

            ans = a.getCoeffs(xvals[i], yvals[i], tolerance, maxFailedPred, sig_digs);                 //Function call to get Coefficients
            print2D(ans);
        }
    }

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
                    flag = 1;
                }
            }

            System.out.println("]");
            if (flag == 1){
                break;
            }
            
        }
        System.out.println("\n");
    }

}


class XnY{
    double x, y;
    XnY(double X, double Y){
        x = X;
        y = Y;
    }
    double getv(int i){
        if(i == 0){
            return x;
        }
        else{
            return y;
        }
    }
}

class Coeffs{

    ArrayList<XnY> xny; 
    double[][] arr; 
    int uptill;
    WeightedObservedPoints obs;
    int prev;
    double[] prev_coeff;
    int done;
    Coeffs(){
        uptill = 0;
        obs = new WeightedObservedPoints();
        prev = 0;
        done = 0;
        xny =new ArrayList<XnY>();
        arr = new double[10][8];                
        prev_coeff = new double[3];
        
    }
    


    // The Required Function
    public double[][] getCoeffs(double x, double y, double tolerance, int maxFailedPred, int sig_dig){
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        if(done != 0){
            prev_coeff = fitter.fit(obs.toList());
        }
        done = 1;
        x = Math.round (x * 10000.0) / 10000.0;
        y = Math.round (y * 10000.0) / 10000.0;
        obs.add(x,y);
        done = 1;
        xny.add(new XnY(x, y));
        double[] coeffs = fitter.fit(obs.toList()); 
        if(xny.size() < 6){
            // My implementation of your virtual point
            if(coeffs[2] > 0 && xny.size() > 2){
                double xprev = x; 
                double avgDist = (x- xny.get(xny.size() - 2).getv(0))/2;
                double yprev = y; 
                x = xprev + avgDist;
                y = yprev/2;
                x = Math.round (x * 10000.0) / 10000.0;
                y = Math.round (y * 10000.0) / 10000.0;
                obs.add(x, y);
                xny.add(new XnY(x, y));
            }
            coeffs = fitter.fit(obs.toList());
            update(coeffs, sig_dig);
            return arr;
        }
        else{  
            int FailedPred = FailiureCalc(coeffs, tolerance);
            if(maxFailedPred > FailedPred){
                if(coeffs[2] > 0 && xny.size() > 2){    //Virtual point 
                    double xprev = x; 
                    double avgDist = (x- xny.get(xny.size() - 2).getv(0))/2;
                    double yprev = y; 
                    x = xprev + avgDist;
                    y = yprev/2;
                    x = Math.round (x * 10000.0) / 10000.0;
                    y = Math.round (y * 10000.0) / 10000.0;
                    obs.add(x, y);
                    xny.add(new XnY(x, y));
                }
                coeffs = fitter.fit(obs.toList());
                update(coeffs, sig_dig);
                return arr;
            }
            else{
                if(prev_coeff[2] > 0 && xny.size() > 2){
                    double xprev = xny.get(xny.size() - 2).getv(0); 
                    double avgDist = (xprev - xny.get(xny.size() - 4).getv(0))/2;
                    double yprev = y; 
                    double x_ = xprev + avgDist;
                    double y_ = yprev/2;
                    x_ = Math.round (x_ * 10000.0) / 10000.0;
                    y_ = Math.round (y_ * 10000.0) / 10000.0;
                    obs.add(x_, y_);
                    xny.add(new XnY(x_, y_));
                }

                update(prev_coeff, sig_dig);
                uptill += 1;
                obs.clear();
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

    void update(double[] coeffs, int sig_dig){
        uptill = (uptill)%10;
        double x = xny.get(0).getv(0);
        arr[uptill][0] = x;
        x = xny.get(xny.size() -1).getv(0);
        System.out.println("The size : " + xny.size());                             // print length
        arr[uptill][1] = xny.get(xny.size() -1).getv(0);
        arr[uptill][2] = RoundOf(coeffs[2], sig_dig);
        arr[uptill][3] = RoundOf(coeffs[1], sig_dig);
        arr[uptill][4] = RoundOf(coeffs[0], sig_dig);

        double y = coeffs[2]*x*x + coeffs[1]*x + coeffs[0];
        
        arr[uptill][5] = RoundOf(y, sig_dig); 
        arr[uptill][6] = RoundOf(whereIsThePointInTheParabola(x, y, arr[uptill][2], arr[uptill][3], arr[uptill][4]), sig_dig);
        arr[uptill][7] = RoundOf(slope(x, arr[uptill][2], arr[uptill][3]), sig_dig);
    }

    double predict(double [] coeffs, double x){
        return coeffs[2]*x*x + coeffs[1]*x + coeffs[1];
    }

    int FailiureCalc(double[] coeffs, double tolerance){

        double calcY;
        int FailedPred = 0;
        
        for (int i=0; i< xny.size(); i++) {
            
            calcY = predict(coeffs, xny.get(i).getv(0));
            double Val = xny.get(i).getv(1);

            ////////////////////////////////                 LOOK HERE           \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
            // The condition is like this because am allowing more points to under the graph then above it
            if(Val > calcY + tolerance || Val < calcY - 3*tolerance){
                FailedPred += 1;
            }
        }
        return FailedPred;
    }

    double RoundOf(double num, int sig_dig){
        return Math.round(num*Math.pow(10,sig_dig))/Math.pow(10,sig_dig);
    }
    
}