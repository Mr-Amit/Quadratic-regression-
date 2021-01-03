import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.lang.Math;
import java.util.Scanner; 
import java.io.*;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;


public class quadratic {
    public static void main(String args[]) throws Exception {
        Coeffs a = new Coeffs();
        double[][] ans = new double[5][10];
        double[] xvals = new double[1200];
        double[] yvals = new double[1200];

        File file = new File("C:\\Users\\lenovo\\Desktop\\JAVA\\textFile.txt"); //You dont need to do anything here I made it for python
          
        file.delete();
        FileWriter wrt = new FileWriter("C:\\Users\\lenovo\\Desktop\\JAVA\\textFile.txt"); //You dont need to do anything here I made it for python

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
        w2f(xvals, wrt, 10);
        w2f(yvals, wrt, 10);

        for(int i = 0; i < 300; i ++){
            ans = a.getCoeffs(xvals[i], yvals[i], 2, 6, 2);                 //Function call to get Coefficients
            print2D(ans,  wrt);
        }

        wrt.close();

    }
    static void print2D(double mat[][],  FileWriter wrt) throws Exception{ 
        wrt.write("\n");

        for (int i = 0; i < mat.length; i++){
  
            System.out.print("[");
            for (int j = 0; j < 5; j++) 
                if(mat[i][0] != 0 || mat[i][1] !=0)
                    if(j < 4)
                        System.out.print(mat[i][j] + ", "); 
                    else
                        System.out.print(mat[i][j]); 
            System.out.println("]");
            w2f(mat[i], wrt, 5);
            
        }
        System.out.println("\n");
    }

    static void w2f(double[] arr, FileWriter wrt, int len) throws Exception{
        //int len = arr.length;
        for (int i = 0; i < len; i++) {
           wrt.write(arr[i] + " ");
        }
        wrt.write("\n");
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
        arr = new double[10][5];                
        prev_coeff = new double[3];
        
    }
    


    // The Required Function
    public double[][] getCoeffs(double x, double y, double tolerance, int minFailedPred, int sig_dig){
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
            if(minFailedPred > FailedPred){
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


    void update(double[] coeffs, int sig_dig){
        uptill = (uptill)%10;
        arr[uptill][0] = xny.get(0).getv(0);
        System.out.println("The size : " + xny.size());                             // print length
        arr[uptill][1] = xny.get(xny.size() -1).getv(0);
        arr[uptill][2] = Math.round(coeffs[2]*Math.pow(10,sig_dig))/Math.pow(10,sig_dig);
        arr[uptill][3] = Math.round(coeffs[1]*Math.pow(10,sig_dig))/Math.pow(10,sig_dig);
        arr[uptill][4] = Math.round(coeffs[0]*Math.pow(10,sig_dig))/Math.pow(10,sig_dig);
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
            if(Val > calcY + tolerance){
                FailedPred += 1;
            }
        }
        return FailedPred;
    }
    
}
