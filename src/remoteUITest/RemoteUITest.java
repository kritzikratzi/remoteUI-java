package remoteUITest; 

import remoteUI.Param;
import remoteUI.RemoteUIServer;



public class RemoteUITest {
	@Param(min=0, max=10, group="Great group") 
	public int someInteger; 
	
	@Param(fmin=-1, fmax=-0.5f, col="#f005")
	public float whySoNegative; 
	
	@Param(group="Great group", col="0,255,0,125" )
	public String someString; 
	
	@Param
	public boolean whatTheBool; 
	
	public static void main(String[] args) throws InterruptedException {
		new RemoteUITest(); 
	}
	
	
	public RemoteUITest() throws InterruptedException{
		new RemoteUIServer( this, 10000 ); 
		
		while( true ){
			System.out.println(  
					"values: " +  someInteger + " / " +  whySoNegative + " / " +  
					someString + " / " + whatTheBool
			); 
			
			Thread.sleep( 3000 ); 
		}
	}
}
