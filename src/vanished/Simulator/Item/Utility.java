package vanished.Simulator.Item;

import vanished.Simulator.UtilityManager.UtilityDecay;
import vanished.Simulator.UtilityManager.UtilityDuration;

public class Utility {
	// 大項目
	public String category;

	// ユーティリティ名
	public String subcategory;

	// アイテムの効用の長さ
	public UtilityDuration duration;

	// 2個目の効用の減衰の仕方
	public UtilityDecay decay;

	// ゼロでとめるか、ゼロ以下になったら死ぬ（例外を投げる）かのフラグ。
	public boolean negativeDie = false;

	public double speed;

	public double p;

	public double power;

	public Utility(String category, String subcategory, String durationStr, String decayStr, double power) throws Exception {
		this.category = category;

		this.subcategory = subcategory;

		{
			if (durationStr.equals("1day")) {
				duration = UtilityDuration.OneDay;
			} else if (durationStr.equals("1week")) {
				duration = UtilityDuration.OneWeek;
			} else if (durationStr.equals("1month")) {
				duration = UtilityDuration.OneMonth;
			} else if (durationStr.equals("3month")) {
				duration = UtilityDuration.ThreeMonth;
			} else if (durationStr.equals("6month")) {
				duration = UtilityDuration.SixMonth;
			} else if (durationStr.equals("1year")) {
				duration = UtilityDuration.OneYear;
			} else if (durationStr.equals("3year")) {
				duration = UtilityDuration.ThreeYear;
			} else if (durationStr.equals("10year")) {
				duration = UtilityDuration.TenYear;
			} else {
				throw new Exception("UtilityManager : utility string error");
			}

			switch (duration) {
			case OneDay:
				speed = 1.0 / (60 * 24);
				break;
			case OneWeek:
				speed = 1.0 / (60 * 24 * 7);
				break;
			case OneMonth:
				speed = 1.0 / (60 * 24 * 30);
				break;
			case ThreeMonth:
				speed = 1.0 / (60 * 24 * 30 * 3);
				break;
			case SixMonth:
				speed = 1.0 / (60 * 24 * 30 * 6);
				break;
			case OneYear:
				speed = 1.0 / (60 * 24 * 30 * 12);
				break;
			case ThreeYear:
				speed = 1.0 / (60 * 24 * 30 * 12 * 3);
				break;
			case TenYear:
				speed = 1.0 / (60 * 24 * 30 * 12 * 10);
				break;
			}
		}

		{
			if (decayStr.equals("one")) {
				this.decay = UtilityDecay.one;
			} else if (decayStr.equals("threequarter")) {
				this.decay = UtilityDecay.threequarter;
			} else if (decayStr.equals("half")) {
				this.decay = UtilityDecay.half;
			} else if (decayStr.equals("quarter")) {
				this.decay = UtilityDecay.quarter;
			} else if (decayStr.equals("zero")) {
				this.decay = UtilityDecay.zero;
			} else {
				throw new Exception("UtilityManager : utility string error");
			}

			switch (decay) {
			case one:
				p = 1;
				break;
			case threequarter:
				p = Math.log(0.75 + 1) / Math.log(2);
				break;
			case half:
				p = Math.log(0.5 + 1) / Math.log(2);
				break;
			case quarter:
				p = Math.log(0.25 + 1) / Math.log(2);
				break;
			case zero:
				p = 0.001;
				break;
			}
		}

		this.power = power;

		if (category.equals("eat") || category.equals("sleep")) {
			this.negativeDie = true;
		} else {
			this.negativeDie = false;
		}
		// TODO
		this.negativeDie = false;
	}
}