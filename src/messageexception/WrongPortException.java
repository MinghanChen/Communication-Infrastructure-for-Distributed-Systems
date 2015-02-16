package messageexception;

public class WrongPortException extends Exception{
	public WrongPortException() {
		super();
		System.out.println("illegal port number!");
	}
}
