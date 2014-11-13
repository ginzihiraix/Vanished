package vanished.Simulator;


public class Main {

	public static void main(String[] args) {
		try {
			SimulationMain sm = new SimulationMain();
			sm.start();

			// GUI_Game gui = new GUI_Game(sm.mapManager, sm.humanManager);

			sm.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
