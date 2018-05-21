package grorbits;


public abstract class InitialConditionsM extends InitialConditions{
  double sign;
  Object[][] icData ;
  Orbit orbit;
  int numPoints;
  double dt;
    
  public InitialConditionsM(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[1][1]=new Double(Em);
    icData[2][1]=new Double(Lm);
    icData[3][1]=new Double(r);
    icData[0][1]=new Double(a);
    this.dt=dt;
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsM(Orbit orbit, double a, double r, double v0, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    this.dt=dt;
    initializeVariables();
    icData[3][1]=new Double(r);
    icData[0][1]=new Double(a);
    icData[4][1]=new Double(v0);
    icData[5][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    
    computeInitialState();
  }
  
  public void setRingShell(){
    if(orbit.getClass().toString().endsWith("Newton")){
      icData[4][0]="<html>v (initial)</html>";
      icData[5][0]="<html>\u03b8 (initial) degrees</html>";
    }
    else {
      if(Math.abs(getA())<1e-6){
        icData[4][0]="<html>v<sub>shell</sub> (initial)</html>";
        icData[5][0]="<html>\u03b8<sub>shell</sub> (initial) degrees</html>";
      }
      else{
        icData[4][0]="<html>v<sub>ring</sub> (initial)</html>";
        icData[5][0]="<html>\u03b8<sub>ring</sub> (initial) degrees</html>";
      }
    }
  }
  
  public void initializeVariables(){
    icData = new Object[6][2];
      icData[0][0]="<html>a<sup>*</sup> \u2261 a/M</html>";
      icData[1][0]="<html>E<sup>*</sup> \u2261 E/m</html>";
      icData[2][0]="<html>L<sup>*</sup> \u2261 L/(mM)</html>";
      icData[3][0]="<html>r<sup>*</sup> \u2261 r/M (initial)</html>";
      icData[4][0]="<html>v<sub>shell</sub> (initial)</html>";
      icData[5][0]="<html>\u03b8<sub>shell</sub> (initial) degrees</html>";
        
  }
  
  public Object[][] getICData(){
    return icData;
  }
  
  
  public boolean isInward(){
    if(sign==1) return false;
    else return true;
  }
  
  public boolean isOutward(){
    if(sign==-1) return false;
    else return true;
  }
  
  public void setInward(){
    sign=-1;
  }
  
  public void setOutward(){
    sign=1;
  }
  
  public double getA(){
    //return Double.valueOf((icData[0][1]).toString());
    return ((Double)icData[0][1]).doubleValue();
  }
  
  public double getEm(){
    //return Double.valueOf((icData[1][1]).toString());
    return ((Double)icData[1][1]).doubleValue();
  }
  
  public double getLm(){
    //return Double.valueOf((icData[2][1]).toString());
    return ((Double)icData[2][1]).doubleValue();
  }
  
  public double getEffPotParameter(){
    return getEm();
  }
  
  public String getEffPotParameterLabel(){
    return "E/m = ";
  }
  
  public String getEffPotParameterUnit(){
    return " M";
  }
  
  public double getR(){
    //return Double.valueOf((icData[3][1]).toString());
    return ((Double)icData[3][1]).doubleValue();
  }
  
  public double getDT(){
    return dt;
  }
  
  public double getV0(){
    //return Double.valueOf((icData[4][1]).toString());
    return ((Double)icData[4][1]).doubleValue();
  }
  
  public double getTheta0(){
    //return Math.toRadians(Double.valueOf((icData[5][1]).toString()));
    return Math.toRadians(((Double)icData[5][1]).doubleValue());
  }
  
  public int getNumPoints(){
    return orbit.numPoints;
  }
  
  public double getSign(){
    return sign;
  }
  
  public void setA(double a){
    icData[0][1]=new Double(a);
    orbit.reset();
  }
  
  public void setEm(double Em){
    icData[1][1]=new Double(Em);
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setEm(value);
  }
  
  public void setLm(double Lm){
    icData[2][1]=new Double(Lm);
    orbit.reset();
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-10) rr=1e-10;
    icData[3][1]=new Double(rr);
    orbit.reset();
  }
  
  public void setDT(double dt){
    this.dt=dt;
    orbit.odeSolver.initialize(dt);
    orbit.reset();
  }
  

  
  public void setSign(int sign){
    this.sign=sign;
    orbit.reset();
  }
  
  public void setV0(double v0){
    icData[4][1]=new Double(v0);
    adjustEmLmSign();
    orbit.reset();
  }
  
  public void setTheta0(double theta0){
    icData[5][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    orbit.reset();
  }
  
  public double getInvB() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public void setInvB(double invB) {
    //do nothing here 
  }
  
}
