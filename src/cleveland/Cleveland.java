package cleveland;

import cleveland.console.ConsoleApplication;
import cleveland.example.MyCliApplication;

import java.io.IOException;

/**
 * cleveland brown
 */
public class Cleveland extends ConsoleApplication {
	protected static String[] args;

	@Override
	protected void invokeMain(final String[] args) {
		MyCliApplication.main(args);
	}

	public static void main(String[] args) throws RuntimeException,IOException {
		System.out.println("Before sleep");
		try {
		  Thread.sleep(5000);
		} catch (InterruptedException e) {
		  throw new RuntimeException(e);
		}
		System.out.println("After sleep");
	}
}
