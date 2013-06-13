package remoteUI; 

import java.awt.Color;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;


/**
 * Yey 
 * @Example Hello
 * 
 * @author hansi
 */
public class RemoteUIServer implements OscEventListener {
	public final static String VERSION = "remoteUI_0.9";

	public  OscP5 osc;
	
	public LinkedHashMap<String, ParamInstance> fields = new LinkedHashMap<String, ParamInstance>();
	
	
	public RemoteUIServer( Object data, int port ){
		osc = new OscP5( this, port, OscP5.UDP );
		osc.properties().setRemoteAddress( new NetAddress( "localhost", port + 1 ) ); 
		osc.addListener( this );
		
		add( data ); 
		
		System.out.println( VERSION + " for processing by hansi");
	}
	
	
	/**
	 * Track an objects properties
	 * Make all your fields public and annotate them with the {@link remoteUI.Param} annotation. 
	 * 
	 * @param object
	 */
	public void add( Object object ){
		for( Field field : object.getClass().getFields() ){
			Param param = field.getAnnotation( Param.class ); 
			if( param != null ){
				String name = field.getName(); 
				String type = null; 
				if( field.getType() == int.class ) type = "INT"; 
				if( field.getType() == float.class ) type = "FLT"; 
				if( field.getType() == boolean.class ) type = "BOL"; 
				if( field.getType() == String.class ) type = "STR"; 
				
				if( type != null ){
					if( fields.get( name ) == null ){
						fields.put( name, new ParamInstance( name, type, param, field, object ) );
					}
					else{
						System.err.println( "Not adding field: " + object.getClass().getName() + "." + name ); 
						System.err.println( "Field was already added before from " + fields.get( name ).object.getClass().getName() + ".java" );  
						System.out.println( "Avoid duplicates! " ); 
					}
				}
				else{
					System.err.println( "Unsupported Field: " + name ); 
					System.err.println( "Type needs to be int, float, boolean or String" ); 
				}
			}
		}
	}
	
	
	@Override
	public void oscEvent( OscMessage msg ) {
		String command = msg.addrPattern().replaceFirst( "([A-Z]+) .*", "$1" ); 
		switch( Command.valueOf( command ) ){
		//////////////////////////////////
		case HELO:
			System.out.println( "Say hi back!" ); 
			sendMessage( "HELO" );
			break; 
		//////////////////////////////////
		case REQU: 
			for( ParamInstance field : fields.values() ){
				// check for int
				field.send(); 
			}
			break;
		//////////////////////////////////
		case SEND:
			Scanner scanner = new Scanner( msg.addrPattern() );
			String type = scanner.skip( "[A-Z]+ " ).next(); 
			String name = scanner.next(); 
			
			ParamInstance field = fields.get( name ); 
			if( field != null && field.type.equals( type ) ){
				field.receive( msg ); 
			}
			break; 
		//////////////////////////////////
		case TEST: 
			sendMessage( "TEST" ); 
			break; 
		//////////////////////////////////
		default: 
			System.out.println( "Unknown message: " + msg  + "; args: " + Arrays.toString( msg.arguments() ) );
			break; 
		}
	}

	
	public void sendMessage( String addrPattern ){
		osc.send( new OscMessage( addrPattern ) ); 
		if( !addrPattern.equals( "TEST") )
			System.out.println( "SEND: " + addrPattern + ": -- no args --" ); 
	}
	
	public void sendMessage( String addrPattern, Color color, String group, Object ... args ){
		int len = args.length; 
		Object realArgs[] = new Object[ 5 + len]; 
		System.arraycopy( args, 0, realArgs, 0, len ); 
		realArgs[len+0] = color.getRed();  
		realArgs[len+1] = color.getGreen();  
		realArgs[len+2] = color.getBlue();  
		realArgs[len+3] = color.getAlpha();  
		realArgs[len+4] = group;  
		
		osc.send( new OscMessage( addrPattern, realArgs ) );
		System.out.println( "SEND: " + addrPattern + ": " + Arrays.toString( realArgs ) ); 
	}
	
	@Override
	public void oscStatus(OscStatus theStatus) {
		System.out.println( "Status: " + theStatus ); 
	}
	
	private final static Pattern 
		shortHexPattern = Pattern.compile( "#[A-F0-9]{3}", Pattern.CASE_INSENSITIVE ),  
		shortHexaPattern = Pattern.compile( "#[A-F0-9]{4}", Pattern.CASE_INSENSITIVE ),  
		hexPattern = Pattern.compile( "#[A-F0-9]{6}", Pattern.CASE_INSENSITIVE ), 
		hexaPattern = Pattern.compile( "#[A-F0-9]{8}", Pattern.CASE_INSENSITIVE ), 
		rgbPattern = Pattern.compile( "([0-9]+),\\s*([0-9]+),\\s*([0-9]+)" ), 
		rgbaPattern = Pattern.compile( "([0-9]+),\\s*([0-9]+),\\s*([0-9]+),\\s*([0-9]+)" ); 
	
	public static Color parseColor( String text ){
		if( text == null || text.equals( "" ) ){
			return new Color( 255, 255, 255, 0 ); 
		}
		
		Matcher matcher; 
		// #ff0 = 255,255,0
		if( ( matcher = shortHexPattern.matcher( text ) ).matches() ){
			return new Color(
				Integer.parseInt( text.charAt( 1 ) + "" + text.charAt( 1 ), 16 ), 
				Integer.parseInt( text.charAt( 2 ) + "" + text.charAt( 2 ), 16 ), 
				Integer.parseInt( text.charAt( 3 ) + "" + text.charAt( 3 ), 16 ),
				255
			); 
		}
		
		// #ff05 = 255,255, alpha = 0x55 = 85
		if( ( matcher = shortHexaPattern.matcher( text ) ).matches() ){
			return new Color(
					Integer.parseInt( text.charAt( 1 ) + "" + text.charAt( 1 ), 16 ), 
					Integer.parseInt( text.charAt( 2 ) + "" + text.charAt( 2 ), 16 ), 
					Integer.parseInt( text.charAt( 3 ) + "" + text.charAt( 3 ), 16 ),
					Integer.parseInt( text.charAt( 4 ) + "" + text.charAt( 4 ), 16 )
				); 
		}

		// #ff0000 = 255,0,0
		if( ( matcher = hexPattern.matcher( text ) ).matches() ){
			return new Color(
					Integer.parseInt( text.substring( 1, 3 ), 16 ), 
					Integer.parseInt( text.substring( 3, 5 ), 16 ), 
					Integer.parseInt( text.substring( 5, 7 ), 16 ), 
					255
				); 
		}
	
		// #ff0000fe = 255,0,0, alpha = 254 
		if( ( matcher = hexaPattern.matcher( text ) ).matches() ){
			return new Color(
					Integer.parseInt( text.substring( 1, 3 ), 16 ), 
					Integer.parseInt( text.substring( 3, 5 ), 16 ), 
					Integer.parseInt( text.substring( 5, 7 ), 16 ), 
					Integer.parseInt( text.substring( 7, 9 ), 16 ) 
				); 
		}
	
		// 255,0,0 
		if( ( matcher = rgbPattern.matcher( text ) ).matches() ){
			return new Color(
					Integer.parseInt( matcher.group( 1 ) ), 
					Integer.parseInt( matcher.group( 2 ) ), 
					Integer.parseInt( matcher.group( 3 ) ), 
					255 
				); 
		}
	
		// 255,0,0,125  (alpha last)  
		if( ( matcher = rgbaPattern.matcher( text ) ).matches() ){
			return new Color(
					Integer.parseInt( matcher.group( 1 ) ), 
					Integer.parseInt( matcher.group( 2 ) ), 
					Integer.parseInt( matcher.group( 3 ) ), 
					Integer.parseInt( matcher.group( 4 ) ) 
				); 
		}
	
		System.err.println( "wtf is this shit? definitely not a color! " + text ); 
		return Color.white; 
	}
	
	/**
	 * A field on some object 
	 */
	class ParamInstance{
		String name; 
		String type; 
		Param param; 
		Field field; 
		Object object; 
		
		BigDecimal min, max; 
		
		public ParamInstance( String name, String type, Param param, Field field, Object object ){
			this.name = name;
			this.type = type; 
			this.param = param; 
			this.field = field; 
			this.object = object;
			this.min = BigDecimal.valueOf( param.min() ).max( BigDecimal.valueOf( param.fmin() ) ); 
			this.max = BigDecimal.valueOf( param.max() ).min( BigDecimal.valueOf( param.fmax() ) );
			System.out.println( type + "mfmfmf=" + param.max() + " " + param.fmax() + " " + max.toString() );  
		}
		
		/**
		 * Send the parameter over magical osc to the remoteui client
		 */
		public void send(){
			Object args = null; 
			try{
				if( type.equals( "INT" ) ) args = new Object[]{ field.getInt( object ), min.intValue(), max.intValue() }; 
				if( type.equals( "FLT" ) ) args = new Object[]{ field.getFloat( object ), min.floatValue(), max.floatValue() };
				if( type.equals( "BOL" ) ) args = new Object[]{ field.getBoolean( object ) == false? 0:1 };
				if( type.equals( "STR" ) ) args = new Object[]{ field.get( object ) == null? "" : field.get( object ) };
			}
			catch( IllegalAccessException e ){
				e.printStackTrace(); 
				return; 
			}
			
			sendMessage( 
				"SEND " + type + " " + name, 
				parseColor( param.col() ), 
				param.group(), 
				(Object[]) args
			); 
		}
		
		/**
		 * Receive a parameter over osc
		 */
		public void receive( OscMessage msg ){
			try{
				if( type.equals( "INT" ) ) field.setInt( object, (Integer) msg.arguments()[0] );
				if( type.equals( "FLT" ) ) field.setFloat( object, (Float) msg.arguments()[0] ); 
				if( type.equals( "BOL" ) ) field.setBoolean( object, ((Integer) msg.arguments()[0] == 0?false:true) ); 
				if( type.equals( "STR" ) ) field.set( object, (String) msg.arguments()[0] ); 
			}
			catch( Exception e ){
				e.printStackTrace(); 
			}
		}
	}
	
	
	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
	
	
	
	private enum Command{
		HELO, TEST, REQU, SEND
	}

}
