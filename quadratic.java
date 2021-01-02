import java.util.ArrayList;
import java.lang.Math;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;


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
        done = 1;
        obs.add(x,y);
        done = 1;
        xny.add(new XnY(x, y));
        double[] coeffs = fitter.fit(obs.toList());
        //System.out.println(coeffs[0] + ", " + coeffs[1] + ", " + coeffs[2]);
        double calcSensitivity = sensitivityCalc(coeffs);
        System.out.println("Sensitivity : " + calcSensitivity);
        if(sensitivity < calcSensitivity){
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
        System.out.println("Mean " + sum/xny.size());
        return sum/xny.size();
    }

    void update(double[] coeffs){
        uptill = (uptill + 1)%5;
        arr[uptill][0] = xny.get(0).getv(0);
        arr[uptill][1] = xny.get(xny.size() -1).getv(0);
        arr[uptill][2] = coeffs[2];
        arr[uptill][3] = coeffs[1];
        arr[uptill][4] = coeffs[0];
    }

    double predict(double [] coeffs, double x){
        return coeffs[2]*x*x + coeffs[1]*x + coeffs[0];
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
        double[][] ans = new double[5][10];

        // Sample data from data.csv
        double[] yvals = {
            -0.033611998,
            -0.027372017,
            -0.039851978,
            -0.05233194,
            -0.058571921,
            -0.064811902,
            -0.077291863,
            -0.077291863,
            -0.077291863,
            -0.039851978
        };
        double[] xvals = {
            11.3861786,	
            11.39468088,
            11.42018774,
            11.43152412,
            11.4428605,	
            11.45419688,
            11.46553326,
            11.47686964,
            11.48820601,
            11.49104011
        };
        for(int i = 0; i < 10; i ++){
            ans = a.getCoeffs(xvals[i], yvals[i], 0.5, 2);
            print2D(ans);
        }
    }
    static void print2D(double mat[][]) 
    { 
        for (int i = 0; i < mat.length; i++){
  
            System.out.print("[");
            for (int j = 0; j < mat[i].length; j++) 
                System.out.print(mat[i][j] + ", "); 
            System.out.println("]");
        }
        System.out.println("\n");
    } 
}
