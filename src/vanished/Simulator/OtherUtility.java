package vanished.Simulator;

import java.util.Random;

public class OtherUtility {

	static public Random rand = new Random();

	static public double wageMin = 0.1 / (60 * 8);

	static public long durationRecoverInitialCost = 60L * 24L * 365L;

	public static double RandGaussian() {
		double a = rand.nextDouble();
		double b = rand.nextDouble();
		return Math.sqrt(-2 * Math.log(a)) * Math.cos(2 * Math.PI * b);
	}

}
