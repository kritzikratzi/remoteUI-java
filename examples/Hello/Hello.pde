import remoteUI.*; 
import oscP5.*; // we also need the osc library
import netP5.*;

// if you would like to control a param you must add an annotation like this: 
@Param(min=100, max=500)
public int numRects = 100; 

// for floats use fmin and fmax instead of min and max
@Param(fmin=0, fmax=PI/10)
public float rotation = 0.1; 

// all parameters are optional 
@Param
public boolean showText = false; 

// but there are two more: 
@Param(group="Text only")
public String line1 = "First line";

@Param(group="Text only",col="#ff0000")
public String line2 = "Second line"; 


void setup() {
  size(400,400);
  smooth();
  
  // start the server on port 10000
  new RemoteUIServer( this, 10000 ); 

  PFont font = createFont("",40);
  textFont(font);
}

void draw() {
  background(0);
  
  fill(255);
  if( showText ){
    text(line1, 10, 50);
    text(line2, 30, 90);
  }
  
  stroke( 255 ); 
  noFill(); 
  float dx = 10*width/numRects;
  translate( 0, 90 ); 
  for( int x = 10; x < width; x+= dx ){
    translate( dx, 0 ); 
    rotate( rotation );
    rect( 0, 0, 30, 30 ); 
  }  
}
