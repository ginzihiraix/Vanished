package vanished.Simulator;

public class ExponentialMovingAverage {
	boolean eventData = false;
	double r = 0;
	double moveMoment = 0;
	long timeLast = 0;
	double valueLast = 0;

	public ExponentialMovingAverage(long duration, boolean eventData) {
		r = Math.pow(0.5, 1.0 / duration);
		this.eventData = eventData;
	}

	public ExponentialMovingAverage(ExponentialMovingAverage ema) {
		this.eventData = ema.eventData;
		this.r = ema.r;
		this.moveMoment = ema.moveMoment;
		this.timeLast = ema.timeLast;
		this.valueLast = ema.valueLast;
	}

	public void Add(long timeNow, double value) {
		long timeDef = timeNow - timeLast;
		if (this.eventData) {
			double rd = Math.pow(r, timeDef);
			moveMoment = rd * moveMoment + (1 - r) * value;
		} else {
			double rd = Math.pow(r, timeDef);
			moveMoment = rd * moveMoment + (r - rd) * valueLast + (1 - r) * value;
			valueLast = value;
		}
		timeLast = timeNow;
	}

	public double GetAverage(long timeNow) {
		long timeDef = timeNow - timeLast;
		double rd = Math.pow(r, timeDef);
		if (this.eventData) {
			return rd * moveMoment;
		} else {
			return rd * moveMoment + (1 - rd) * valueLast;
		}
	}

	public void Clear(long timeNow) {
		moveMoment = 0;
		timeLast = timeNow;
		valueLast = 0;
	}
}