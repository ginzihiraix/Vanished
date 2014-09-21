package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.HumanStatus.ConsumeResult;
import vanished.Simulator.HumanStatus.MakerWorkResult;
import vanished.Simulator.HumanStatus.NopResult;
import vanished.Simulator.HumanStatus.TraderWorkResult;
import vanished.Simulator.HumanStatus.TryResult;
import vanished.Simulator.Structure.Room;

public class Human {

	MapManager mm;

	// status
	HumanStatus humanStatus;

	public Human(MapManager mm, long timeNow, Room currentRoom) throws Exception {
		this.mm = mm;
		humanStatus = new HumanStatus(mm, timeNow, currentRoom);
	}

	public Human(MapManager mm, HumanStatus humanStatus) throws Exception {
		this.mm = mm;
		this.humanStatus = humanStatus;
	}

	public void GenerateAndExecuteAction() throws Exception {

		// TODO:金が少なくなってたら、とりあえず足す
		// if (humanStatus.money < 1000) {
		// humanStatus.money += 1000;
		// }

		ArrayList<TryResult> workResults = new ArrayList<TryResult>();
		if (humanStatus.ShouldWork() == true) {
			// 働かないといけない場合
			// ランダムに行動を生成して、単位時間当たりに稼げる額が一番大きくなる仕事を選択する。
			for (int frame = 0; frame < 100; frame++) {
				try {
					TryResult res = null;
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(3);
					switch (actionType) {
					case 0: { // Buyer
						res = humanStatusNew.TryTrader();
						break;
					}
					case 1: { // Worker
						res = humanStatusNew.TryWorker();
						break;
					}
					case 2: { // Maker
						res = humanStatusNew.TryMaker();
						break;
					}
					}

					workResults.add(res);
					if (workResults.size() > 30) break;
				} catch (HumanSimulationException e) {
					// e.printStackTrace();
				}
			}
		}

		ArrayList<TryResult> consumeResults = new ArrayList<TryResult>();
		{
			// 働かなくてもいい場合、または職がない状態。
			// ランダムに行動を選択して、単位費用当たりに増加する効用が一番大きくなる行動を決定する。
			for (int frame = 0; frame < 100; frame++) {
				try {
					TryResult res = null;
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(2);
					switch (actionType) {
					case 0: { // 何もしない
						// 何もしない
						res = humanStatusNew.TryNop();
						break;
					}
					case 1: { // 食べる
						res = humanStatusNew.TryConsume();
						break;
					}
					}

					consumeResults.add(res);
					if (consumeResults.size() > 30) break;
				} catch (HumanSimulationException e) {
					// e.printStackTrace();
				}
			}
		}

		// 実行する。
		this.ExecuteAction(workResults, consumeResults);

		// 情況をダンプ
		this.humanStatus.Dump();
	}

	private void ExecuteAction(ArrayList<TryResult> workResults, ArrayList<TryResult> consumeResults) throws Exception {

		// //////////////////////////////////////////////
		// 選択確率を計算する。Work
		// //////////////////////////////////////////////
		int numWorkResults = workResults.size();

		// スコアを計算する。
		double[] workScores = new double[numWorkResults];
		{
			for (int i = 0; i < numWorkResults; i++) {
				TryResult res = workResults.get(i);
				double score = (res.moneyEnd - res.moneyStart) / (res.timeEnd - res.timeStart);
				workScores[i] = score;
			}
		}

		// 選択確率を計算する。
		double[] workWeights = new double[numWorkResults];
		double workWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3.0;
			double max = 0;
			for (int i = 0; i < numWorkResults; i++) {
				if (workScores[i] > max) {
					max = workScores[i];
				}
			}
			if (max > 0) {
				for (int i = 0; i < numWorkResults; i++) {
					double weight = Math.exp(workScores[i] / max * rate);
					workWeights[i] = weight;
					workWeightTotal += weight;
				}
			}
		}

		// //////////////////////////////////////////////
		// 選択確率を計算する。Consume
		// //////////////////////////////////////////////
		double wagePerMin = this.humanStatus.wageMovingAverage.GetAverage(this.humanStatus.timeSimulationComplete);
		if (wagePerMin == 0) wagePerMin = 1.0e-10;
		double minToGainUnitMoney = 1 / wagePerMin;

		int numConsumeResults = consumeResults.size();

		// スコアを計算する。
		double[] consumeScores = new double[numConsumeResults];
		{
			for (int i = 0; i < numConsumeResults; i++) {
				TryResult res = consumeResults.get(i);
				double utilDelta = res.utilEnd - res.utilStart;
				double duration = res.timeEnd - res.timeStart;
				double moneyUsed = res.moneyStart - res.moneyEnd;
				double score = utilDelta / (duration + moneyUsed * minToGainUnitMoney);
				consumeScores[i] = score;
			}
		}

		// 選択確率を計算する。
		double[] consumeWeights = new double[numConsumeResults];
		double consumeWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3;
			double max = 0;
			for (int i = 0; i < numConsumeResults; i++) {
				if (consumeScores[i] > max) {
					max = consumeScores[i];
				}
			}
			if (max > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					double weight = Math.exp(consumeScores[i] / max * rate);
					consumeWeights[i] = weight;
					consumeWeightTotal += weight;
				}
			}
		}

		// //////////////////////////////////////////////
		// weightでランダムにアクションを選択して実行する。
		// //////////////////////////////////////////////
		if (workWeightTotal > 0) {
			TryResult resSelected = null;
			double r = OtherUtility.rand.nextDouble();
			double sum = 0;
			for (int i = 0; i < numWorkResults; i++) {
				sum += workWeights[i];
				if (r < sum / workWeightTotal) {
					resSelected = workResults.get(i);
					break;
				}
			}

			if (resSelected instanceof TraderWorkResult) {
				TraderWorkResult result = (TraderWorkResult) resSelected;
				humanStatus.DoTrader(result);
			} else if (resSelected instanceof MakerWorkResult) {
				MakerWorkResult result = (MakerWorkResult) resSelected;
				humanStatus.DoMaker(result);
			} else {
				throw new Exception("fatal error");
			}
		} else if (consumeWeightTotal > 0) {
			TryResult resSelected = null;
			double r = OtherUtility.rand.nextDouble();
			double sum = 0;
			for (int i = 0; i < numConsumeResults; i++) {
				sum += consumeWeights[i];
				if (r < sum / consumeWeightTotal) {
					resSelected = consumeResults.get(i);
					break;
				}
			}

			if (resSelected instanceof NopResult) {
				humanStatus.DoNop();
			} else if (resSelected instanceof ConsumeResult) {
				ConsumeResult result = (ConsumeResult) resSelected;
				humanStatus.DoConsume(result);
			}
		} else {
			humanStatus.DoNop();
		}

		// //////////////////////////////////////////////
		// 期待値をFeedbackする。
		// //////////////////////////////////////////////
		{
			for (int i = 0; i < numWorkResults; i++) {
				TryResult resSelected = workResults.get(i);
				double prob = 0;
				if (workWeightTotal > 0) {
					prob = workWeights[i] / workWeightTotal;
				}
				if (resSelected instanceof TraderWorkResult) {
					TraderWorkResult result = (TraderWorkResult) resSelected;
					result.deliverRoom.FeedbackAboutDeliverPrice(result.itemDef, result.callForItem.price, prob * result.numPick);
					result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, prob * result.numPick);
				} else if (resSelected instanceof MakerWorkResult) {
					MakerWorkResult result = (MakerWorkResult) resSelected;
					result.factoryRoom.FeedbackAboutMakerPrice(result.cfm, result.cfm.wage, 1.0 * prob);
				}
			}
			if (workWeightTotal == 0 && consumeWeightTotal > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					TryResult resSelected = consumeResults.get(i);
					double prob = consumeWeights[i] / consumeWeightTotal;
					if (resSelected instanceof NopResult) {
					} else if (resSelected instanceof ConsumeResult) {
						ConsumeResult result = (ConsumeResult) resSelected;
						result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, 1.0 * prob);
					}
				}
			}
		}
	}

	public Human Birth() throws Exception {
		double utilAverage = humanStatus.utilityMovingAverage.GetAverage(humanStatus.timeSimulationComplete);
		if (utilAverage < HumanDef.thresholdUtilityForBirth) return null;
		if (OtherUtility.rand.nextDouble() > HumanDef.probForBirth) return null;

		HumanStatus humanStatusNew = this.humanStatus.Birth();
		if (humanStatusNew == null) return null;

		Human ret = new Human(mm, humanStatusNew);
		return ret;
	}

	public boolean Death() throws Exception {
		double utilAverage = humanStatus.utilityMovingAverage.GetAverage(humanStatus.timeSimulationComplete);
		long year = (this.humanStatus.timeSimulationComplete - this.humanStatus.timeBorn) / (60L * 24L * 365L);
		if (year > 3) {
			return true;
		} else if (year >= 1) {
			if (utilAverage > HumanDef.thresholdUtilityForDeath) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}
