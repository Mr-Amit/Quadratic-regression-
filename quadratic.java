import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import java.util.*;
import java.io.*; 

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
        uptill = -1;
        obs = new WeightedObservedPoints();
        prev = 0;
        done = 0;
        xny =new ArrayList<XnY>();
        arr = new double[5][5];
        prev_coeff = new double[3];
    }
    
    public double[][] getCoeffs(double x, double y, double sensitivity, int sig_dig){
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        if(done != 0){
            prev_coeff = fitter.fit(obs.toList());
        }
        
        obs.add(x,y);
        done = 1;
        xny.add(new XnY(x, y));
        double[] coeffs = fitter.fit(obs.toList());
        //System.out.println(coeffs[0] + ", " + coeffs[1] + ", " + coeffs[2]);
        if(sensitivity < sensitivityCalc(coeffs)){
            update(coeffs);
            return arr;
        }
        else{
            update(prev_coeff);
            obs.clear();
            prev = xny.size() -1;
            xny.clear();
            obs.add(x, y);
            xny.add(new XnY(x, y));
            coeffs = defaultCoeff();
            update(coeffs);
            return arr;
        }
            
    }


    double[] defaultCoeff(){
        double[] def = {-0.01, -0.01, 1};
        return def;
    }

    
    double mean(){
        double sum = 0;
        for(XnY ele: xny){
            sum += ele.getv(1);
        }
        return sum/xny.size();
    }

    void update(double[] coeffs){
        uptill = (uptill + 1)%5;
        arr[uptill][0] = xny.get(0).getv(0);
        arr[uptill][1] = xny.get(xny.size() -1).getv(0);
        arr[uptill][2] = coeffs[0];
        arr[uptill][3] = coeffs[1];
        arr[uptill][4] = coeffs[2];
    }

    double predict(double [] coeffs, double x){
        return coeffs[0]*x*x + coeffs[1]*x + coeffs[2];
    }

    double sensitivityCalc(double[] coeffs){

        double[] predictedValues = new double[xny.size()];
        double residualSumOfSquares = 0;
        
        for (int i=prev; i< xny.size(); i++) {
            predictedValues[i] = predict(coeffs, xny.get(i).getv(0));
            double Val = xny.get(i).getv(1);
            double t = Math.pow((predictedValues[i] - Val), 2);
            residualSumOfSquares  += t;
        }
        
        double avgActualValue = mean();
        double totalSumOfSquares = 0;
        for (int i=prev; i<xny.size(); i++) {
            totalSumOfSquares += Math.pow( (predictedValues[i] - avgActualValue),2);

        }
        return 1.0 - (residualSumOfSquares/totalSumOfSquares);
    }
    
}



public class App {
    public static void main(String args[]) {
        Coeffs a = new Coeffs();
        
    }
}
