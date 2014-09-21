package vanished.Simulator;

public class HumanDef {

	static int status_baby = 0;
	static int status_student = 1;
	static int status_adult = 6;

	// eatの最大値
	static double eatMax = 1.0;

	// eatが0に減っていく速度。今のところ12時間で0になるように設定。
	static double eatDecreaseSpeedPerMin = 1.0 / (24 * 60);

	// sleepの最大値
	static double sleepMax = 1.0;

	// sleepが0に減っていく速度。今のところ24時間で0になるように設定。
	static double sleepDecreaseSpeedPerMin = 1.0 / (48 * 60);

	// funの最大値
	static double funMax = 1.0;

	// funが0に減っていく速度。今のところ24時間で0になるように設定。
	static double funDecreaseSpeedPerMin = 1.0 / (48 * 60);

	// eatのために移動するときの、最大移動時間。
	static long maxMoveTimeForEat = 60 * 2;

	// Tradeのために移動するときの、最大移動時間
	static long maxMoveTimeForTrade = 60 * 6;

	// Workのために移動するときの、最大移動時間
	static long maxMoveTimeForWork = 60 * 1;

	// 働かないといけない最低時間比率
	static double rateWork = 10.0 / 24.0;

	// 子供を生める平均Utilityの閾値
	static double thresholdUtilityForBirth = 0.5;

	// 5年に一度の機会で子供を生む確率。
	static double probForBirth = 0.2;

	static double thresholdUtilityForDeath = 0.1;

}
