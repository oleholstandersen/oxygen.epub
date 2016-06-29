package dk.nota.oxygen.common;

public class TestConsoleWorker extends AbstractConsoleWorker {
	
	public TestConsoleWorker(ConsoleWindow consoleWindow) {
		super(consoleWindow);
	}

	public static void main(String[] args) {
		TestConsoleWorker testConsoleWorker = new TestConsoleWorker(
				new ConsoleWindow("Test"));
		testConsoleWorker.execute();
	}

	@Override
	protected Object doInBackground() throws Exception {
		for (int i = 1; i <= 50000; i++) {
			getConsoleWorker().writeToConsole("Test");
		}
		return null;
	}

}
