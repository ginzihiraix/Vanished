package vanished.Simulator;

public class HumanDef {

	static int status_baby = 0;
	static int status_student = 1;
	static int status_adult = 6;

	// eat�̍ő�l
	static double eatMax = 1.0;

	// eat��0�Ɍ����Ă������x�B���̂Ƃ���12���Ԃ�0�ɂȂ�悤�ɐݒ�B
	static double eatDecreaseSpeedPerMin = 1.0 / (24 * 60);

	// sleep�̍ő�l
	static double sleepMax = 1.0;

	// sleep��0�Ɍ����Ă������x�B���̂Ƃ���24���Ԃ�0�ɂȂ�悤�ɐݒ�B
	static double sleepDecreaseSpeedPerMin = 1.0 / (48 * 60);

	// fun�̍ő�l
	static double funMax = 1.0;

	// fun��0�Ɍ����Ă������x�B���̂Ƃ���24���Ԃ�0�ɂȂ�悤�ɐݒ�B
	static double funDecreaseSpeedPerMin = 1.0 / (48 * 60);

	// eat�̂��߂Ɉړ�����Ƃ��́A�ő�ړ����ԁB
	static long maxMoveTimeForEat = 60 * 2;

	// Trade�̂��߂Ɉړ�����Ƃ��́A�ő�ړ�����
	static long maxMoveTimeForTrade = 60 * 6;

	// Work�̂��߂Ɉړ�����Ƃ��́A�ő�ړ�����
	static long maxMoveTimeForWork = 60 * 1;

	// �����Ȃ��Ƃ����Ȃ��Œ᎞�Ԕ䗦
	static double rateWork = 10.0 / 24.0;

	// �q���𐶂߂镽��Utility��臒l
	static double thresholdUtilityForBirth = 0.5;

	// 5�N�Ɉ�x�̋@��Ŏq���𐶂ފm���B
	static double probForBirth = 0.2;

	static double thresholdUtilityForDeath = 0.1;

}
