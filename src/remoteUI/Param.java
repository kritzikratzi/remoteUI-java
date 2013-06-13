package remoteUI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Use these classes to annotate your data. 
 * Param supports optional settings: 
 * - color: The color that will be use in the ofxRemoteUI client. 
 *          Can be specified as hex or rgb with or without alpha (alpha always as last param) 
 *          All this is the same dark red: 
 *          	#500 
 *          	#500f
 *          	#550000
 *          	#550000ff
 *          	85,0,0
 *          	85,0,0,255  
 * - group: Some group name you like to use. 
 * 
 * Ints and floats allow you to specify a range too.
 * Floats should use fmin and fmax
 * @Param(imin=5, imax=10, group="My Group", color="#0f0")
 * public int myInteger; 
 * 
 * 
 * Your params MUST have public visibility, otherwise it won't be listed.  
 * 
 * @author hansi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Param {
	java.lang.String group() default "defaultGroup";
	java.lang.String col() default "#fff"; 
	int min() default Integer.MIN_VALUE; 
	int max() default Integer.MAX_VALUE;
	float fmin() default Integer.MIN_VALUE; 
	float fmax() default Integer.MAX_VALUE;
}