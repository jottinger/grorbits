package grorbits;


public abstract class InitialConditionsL extends InitialConditions{
  double sign;
  Object[][] icData ;
  Orbit orbit;
  int numPoints;
  double dt;
  
  public InitialConditionsL(Orbit orbit, double a, double invB, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=new Double(a);
    icData[1][1]=new Double(1/invB);//changed!!!
    icData[2][1]=new Double(r);
    this.dt=dt;
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsL(Orbit orbit, double a, double r, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    this.dt=dt;
    initializeVariables();
    icData[0][1]=new Double(a);
    icData[2][1]=new Double(r);
    icData[3][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    this.dt=dt;
    
    computeInitialState();
  }
  
  
  public void setRingShell(){
    if(orbit.getClass().toString().endsWith("Newton")){
      icData[3][0]="<html>\u03b8 (initial) degrees</html>";
    }
    else {
      if(Math.abs(getA())<1e-6){
        icData[3][0]="<html>\u03b8<sub>shell</sub> (initial) degrees</html>";
      }
      else{
        icData[3][0]="<html>\u03b8<sub>ring</sub> (initial) degrees</html>";
      }
    }
  }
  
  public void initializeVariables(){
    icData = new Object[4][2];
    icData[0][0]="<html>a<sup>*</sup> \u2261 a/M</html>";
    icData[1][0]="<html>b<sup>*</sup> \u2261 b/M</html>";//changed!!!
    icData[2][0]="<html>r<sup>*</sup> \u2261 r/M (initial)</html>";
    icData[3][0]="<html>\u03b8<sub>shell</sub> (initial) degrees</html>";
    
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
    return ((Double)icData[0][1]).doubleValue();
  }
  
  public double getInvB(){
    return 1/((Double)icData[1][1]).doubleValue(); //changed!!!
  }

  public double getEffPotParameter(){
    return getInvB();
  }
  
  public String getEffPotParameterLabel(){
    return "b/M = ";
  }
  
  public String getEffPotParameterUnit(){
    return " ";
  }
  
  public double getR(){
    return ((Double)icData[2][1]).doubleValue();
  }
  
  public double getDT(){
    return dt;
  }
  
  public double getTheta0(){
    return Math.toRadians(((Double)icData[3][1]).doubleValue());
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
  
  public void setInvB(double invB){
    icData[1][1]= new Double(1/invB);//changed!!!
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setInvB(value);
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-10) rr=1e-10;
    icData[2][1]=new Double(rr);
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
  
  public void setTheta0(double theta0){
    icData[3][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    orbit.reset();
  }
  
  public double getEm() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public double getLm() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public double getV0() {
    return 1;
  }

  public void setEm(double Em) {
    //  do nothing here
    
  }

  public void setLm(double Lm) {
    // do nothing here
    
  }

  public void setV0(double v0) {
    // do nothing here
    
  }
  
}
