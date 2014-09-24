package vanished.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
		ArrayList<TryResult> workResultsVirtual = new ArrayList<TryResult>();
		if (humanStatus.ShouldWork() == true) {
			// 働かないといけない場合
			// ランダムに行動を生成して、単位時間当たりに稼げる額が一番大きくなる仕事を選択する。
			for (int frame = 0; frame < 1000; frame++) {
				try {
					TryResult res = null;
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(2);
					switch (actionType) {
					case 0: { // Buyer
						res = humanStatusNew.TryTrader();
						break;
					}
					case 1: { // Maker
						res = humanStatusNew.TryMaker();
						break;
					}
					}

					if (res.realFlag == true) {
						workResults.add(res);
					} else {
						workResultsVirtual.add(res);
					}
					if (workResults.size() > 30) break;
				} catch (HumanSimulationException e) {
					// e.printStackTrace();
				}
			}
		}

		ArrayList<TryResult> consumeResults = new ArrayList<TryResult>();
		ArrayList<TryResult> consumeResultsVirtual = new ArrayList<TryResult>();
		{
			// 働かなくてもいい場合、または職がない状態。
			// ランダムに行動を選択して、単位費用当たりに増加する効用が一番大きくなる行動を決定する。
			for (int frame = 0; frame < 1000; frame++) {
				try {
					TryResult res = null;
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(2);
					switch (actionType) {
					case 0: { // 何もしない
						res = humanStatusNew.TryNop();
						break;
					}
					case 1: { // 食べる
						res = humanStatusNew.TryConsume();
						break;
					}
					}

					if (res.realFlag == true) {
						consumeResults.add(res);
					} else {
						consumeResultsVirtual.add(res);
					}
					if (consumeResults.size() > 30) break;
				} catch (HumanSimulationException e) {
					// e.printStackTrace();
				}
			}
		}

		// 実行する。
		// this.ExecuteAction(workResults, consumeResults, workResultsVirtual, consumeResultsVirtual);
		this.AAA(workResults, consumeResults, workResultsVirtual, consumeResultsVirtual);

		// 情況をダンプ
		this.humanStatus.Dump();
	}

	private void AAA(ArrayList<TryResult> workResults, ArrayList<TryResult> consumeResults, ArrayList<TryResult> workResultsVirtual,
			ArrayList<TryResult> consumeResultsVirtual) throws Exception {

		// 登場する仮想Roomを列挙する。仮想Roomが関連するTryResultを抜き出す。
		HashMap<Room, ArrayList<TryResult>> roomResultMap = new HashMap<Room, ArrayList<TryResult>>();
		{
			ArrayList<TryResult> resultVirtualAll = new ArrayList<TryResult>();
			resultVirtualAll.addAll(workResultsVirtual);
			resultVirtualAll.addAll(consumeResultsVirtual);
			for (TryResult res : resultVirtualAll) {
				if (res instanceof TraderWorkResult) {
					TraderWorkResult result = (TraderWorkResult) res;
					if (result.deliverRoom.IsReal() == false) {
						ArrayList<TryResult> ress = roomResultMap.get(result.deliverRoom);
						if (ress == null) {
							ress = new ArrayList<TryResult>();
							roomResultMap.put(result.deliverRoom, ress);
						}
						ress.add(res);
					}
					if (result.shopRoom.IsReal() == false) {
						ArrayList<TryResult> ress = roomResultMap.get(result.shopRoom);
						if (ress == null) {
							ress = new ArrayList<TryResult>();
							roomResultMap.put(result.shopRoom, ress);
						}
						ress.add(res);
					}
				} else if (res instanceof MakerWorkResult) {
					MakerWorkResult result = (MakerWorkResult) res;
					if (result.factoryRoom.IsReal() == false) {
						ArrayList<TryResult> ress = roomResultMap.get(result.factoryRoom);
						if (ress == null) {
							ress = new ArrayList<TryResult>();
							roomResultMap.put(result.factoryRoom, ress);
						}
						ress.add(res);
					}
				} else if (res instanceof ConsumeResult) {
					ConsumeResult result = (ConsumeResult) res;
					if (result.shopRoom.IsReal() == false) {
						ArrayList<TryResult> ress = roomResultMap.get(result.shopRoom);
						if (ress == null) {
							ress = new ArrayList<TryResult>();
							roomResultMap.put(result.shopRoom, ress);
						}
						ress.add(res);
					}
				} else if (res instanceof NopResult) {
					NopResult result = (NopResult) res;
				}
			}
		}

		// 列挙した登場する各仮想部屋をひとつだけまぜて、シミュレーションを実行してみる。
		for (Entry<Room, ArrayList<TryResult>> e : roomResultMap.entrySet()) {
			ArrayList<TryResult> workResults2 = new ArrayList<TryResult>();
			workResults2.addAll(workResults);

			ArrayList<TryResult> consumeResults2 = new ArrayList<TryResult>();
			consumeResults2.addAll(consumeResults);

			Room virtualRoomTarget = e.getKey();
			ArrayList<TryResult> virtualResultsTarget = e.getValue();

			for (TryResult res : virtualResultsTarget) {
				if (res instanceof TraderWorkResult) {
					workResults2.add(res);
				} else if (res instanceof MakerWorkResult) {
					workResults2.add(res);
				} else if (res instanceof ConsumeResult) {
					consumeResults2.add(res);
				} else if (res instanceof NopResult) {
					consumeResults2.add(res);
				}
			}
			this.ExecuteAction(workResults2, consumeResults2, virtualRoomTarget);
		}

		this.ExecuteAction(workResults, consumeResults, null);
	}

	private void ExecuteAction(ArrayList<TryResult> workResults, ArrayList<TryResult> consumeResults, Room virtualRoomTarget) throws Exception {

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

		double workScoreMax = 0;
		for (int i = 0; i < numWorkResults; i++) {
			if (workScores[i] > workScoreMax) {
				workScoreMax = workScores[i];
			}
		}

		// 選択確率を計算する。
		double[] workWeights = new double[numWorkResults];
		double workWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3.0;
			if (workScoreMax > 0) {
				for (int i = 0; i < numWorkResults; i++) {
					double weight = Math.exp(workScores[i] / workScoreMax * rate);
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

		double consumeScoreMax = 0;
		for (int i = 0; i < numConsumeResults; i++) {
			if (consumeScores[i] > consumeScoreMax) {
				consumeScoreMax = consumeScores[i];
			}
		}

		// 選択確率を計算する。
		double[] consumeWeights = new double[numConsumeResults];
		double consumeWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3;
			if (consumeScoreMax > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					double weight = Math.exp(consumeScores[i] / consumeScoreMax * rate);
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
				if (virtualRoomTarget == null) {
					humanStatus.DoTrader(result);
				} else {
					// TODO:仮想建物の仮想シミュレーションについて、どうにかする。
					// if (result.deliverRoom == virtualRoomTarget) {
					// result.deliverRoom.SellItemVirtual(timeNow, item, price, simulation)
					//
					// } else if (result.shopRoom == virtualRoomTarget) {
					//
					// }
				}
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
			if (workWeightTotal > 0) {
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
			} else if (consumeWeightTotal > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					TryResult resSelected = consumeResults.get(i);
					double prob = consumeWeights[i] / consumeWeightTotal;
					if (resSelected instanceof NopResult) {
					} else if (resSelected instanceof ConsumeResult) {
						ConsumeResult result = (ConsumeResult) resSelected;
						result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, 1.0 * prob);
					}
				}
			} else {
				// do nothing
			}
		}
	}

	private void ExecuteAction(ArrayList<TryResult> workResults, ArrayList<TryResult> consumeResults, ArrayList<TryResult> workResultsVirtual,
			ArrayList<TryResult> consumeResultsVirtual) throws Exception {

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

		double workScoreMax = 0;
		for (int i = 0; i < numWorkResults; i++) {
			if (workScores[i] > workScoreMax) {
				workScoreMax = workScores[i];
			}
		}

		// 選択確率を計算する。
		double[] workWeights = new double[numWorkResults];
		double workWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3.0;
			if (workScoreMax > 0) {
				for (int i = 0; i < numWorkResults; i++) {
					double weight = Math.exp(workScores[i] / workScoreMax * rate);
					workWeights[i] = weight;
					workWeightTotal += weight;
				}
			}
		}

		// //////////////////////////////////////////////
		// 選択確率を計算する。WorkVirtual
		// //////////////////////////////////////////////
		int numWorkResultsVirtual = workResultsVirtual.size();

		// スコアを計算する。
		double[] workScoresVirtual = new double[numWorkResultsVirtual];
		{
			for (int i = 0; i < numWorkResultsVirtual; i++) {
				TryResult res = workResultsVirtual.get(i);
				double score = (res.moneyEnd - res.moneyStart) / (res.timeEnd - res.timeStart);
				workScoresVirtual[i] = score;
			}
		}

		// 選択確率を計算する。
		double[] workWeightsVirtual = new double[numWorkResultsVirtual];
		{
			// TODO:rate調整必要
			double rate = 3;
			if (workScoreMax > 0) {
				for (int i = 0; i < numWorkResultsVirtual; i++) {
					double weight = Math.exp(workScoresVirtual[i] / workScoreMax * rate);
					workWeightsVirtual[i] = weight;
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

		double consumeScoreMax = 0;
		for (int i = 0; i < numConsumeResults; i++) {
			if (consumeScores[i] > consumeScoreMax) {
				consumeScoreMax = consumeScores[i];
			}
		}

		// 選択確率を計算する。
		double[] consumeWeights = new double[numConsumeResults];
		double consumeWeightTotal = 0;
		{
			// TODO:rate調整必要
			double rate = 3;
			if (consumeScoreMax > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					double weight = Math.exp(consumeScores[i] / consumeScoreMax * rate);
					consumeWeights[i] = weight;
					consumeWeightTotal += weight;
				}
			}
		}

		// //////////////////////////////////////////////
		// 選択確率を計算する。Consume Virtual
		// //////////////////////////////////////////////
		int numConsumeResultsVirtual = consumeResultsVirtual.size();

		double[] consumeScoresVirtual = new double[numConsumeResultsVirtual];
		{
			for (int i = 0; i < numConsumeResultsVirtual; i++) {
				TryResult res = consumeResultsVirtual.get(i);
				double utilDelta = res.utilEnd - res.utilStart;
				double duration = res.timeEnd - res.timeStart;
				double moneyUsed = res.moneyStart - res.moneyEnd;
				double score = utilDelta / (duration + moneyUsed * minToGainUnitMoney);
				consumeScoresVirtual[i] = score;
			}
		}

		double[] consumeWeightsVirtual = new double[numConsumeResultsVirtual];
		{
			// TODO:rate調整必要
			double rate = 3;
			if (consumeScoreMax > 0) {
				for (int i = 0; i < numConsumeResultsVirtual; i++) {
					double weight = Math.exp(consumeScoresVirtual[i] / consumeScoreMax * rate);
					consumeWeightsVirtual[i] = weight;
				}
			}
		}

		// ///////////////////////////////////////////////////
		// 各VirtualRoomに対して、Realだったときの選択行動を仮想的に実行する。
		// ///////////////////////////////////////////////////
		{
			// TryResultからIndexへのマッピングを構築する。Work版
			HashMap<TryResult, Integer> res2indexWork = new HashMap<TryResult, Integer>();
			for (int i = 0; i < numWorkResultsVirtual; i++) {
				TryResult res = workResultsVirtual.get(i);
				res2indexWork.put(res, i);
			}

			// TryResultからIndexへのマッピングを構築する。Consume版
			HashMap<TryResult, Integer> res2indexConsume = new HashMap<TryResult, Integer>();
			for (int i = 0; i < numConsumeResultsVirtual; i++) {
				TryResult res = consumeResultsVirtual.get(i);
				res2indexConsume.put(res, i);
			}

			// 登場する仮想Roomを列挙する。仮想Roomが関連するTryResultを抜き出す。
			HashMap<Room, ArrayList<TryResult>> roomResultMap = new HashMap<Room, ArrayList<TryResult>>();
			{
				ArrayList<TryResult> resultVirtualAll = new ArrayList<TryResult>();
				resultVirtualAll.addAll(workResultsVirtual);
				resultVirtualAll.addAll(consumeResultsVirtual);
				for (TryResult res : resultVirtualAll) {
					if (res instanceof TraderWorkResult) {
						TraderWorkResult result = (TraderWorkResult) res;
						if (result.deliverRoom.IsReal() == false) {
							ArrayList<TryResult> ress = roomResultMap.get(result.deliverRoom);
							if (ress == null) {
								ress = new ArrayList<TryResult>();
								roomResultMap.put(result.deliverRoom, ress);
							}
							ress.add(res);
						}
						if (result.shopRoom.IsReal() == false) {
							ArrayList<TryResult> ress = roomResultMap.get(result.shopRoom);
							if (ress == null) {
								ress = new ArrayList<TryResult>();
								roomResultMap.put(result.shopRoom, ress);
							}
							ress.add(res);
						}
					} else if (res instanceof MakerWorkResult) {
						MakerWorkResult result = (MakerWorkResult) res;
						if (result.factoryRoom.IsReal() == false) {
							ArrayList<TryResult> ress = roomResultMap.get(result.factoryRoom);
							if (ress == null) {
								ress = new ArrayList<TryResult>();
								roomResultMap.put(result.factoryRoom, ress);
							}
							ress.add(res);
						}
					} else if (res instanceof ConsumeResult) {
						ConsumeResult result = (ConsumeResult) res;
						if (result.shopRoom.IsReal() == false) {
							ArrayList<TryResult> ress = roomResultMap.get(result.shopRoom);
							if (ress == null) {
								ress = new ArrayList<TryResult>();
								roomResultMap.put(result.shopRoom, ress);
							}
							ress.add(res);
						}
					} else if (res instanceof NopResult) {
						NopResult result = (NopResult) res;
					}
				}
			}

			// 各仮想RoomがRealだった時の、アクションの選択確率を計算して、仮想Roomに対してFeedbackを与える。
			for (Entry<Room, ArrayList<TryResult>> e : roomResultMap.entrySet()) {
				ArrayList<TryResult> ress = e.getValue();
				double workWeightVirtualTotal = 0;
				for (TryResult res : ress) {
					Integer index = res2indexWork.get(res);
					if (index == null) continue;
					workWeightVirtualTotal += workWeightsVirtual[index];
				}

				double workWeightTotalAll = workWeightTotal + workWeightVirtualTotal;

				double consumeWeightVirtualTotal = 0;
				for (TryResult res : ress) {
					Integer index = res2indexConsume.get(res);
					if (index == null) continue;
					consumeWeightVirtualTotal += consumeWeightsVirtual[index];
				}

				double consumeWeightTotalAll = consumeWeightTotal + consumeWeightVirtualTotal;

				if (workWeightTotalAll > 0) {
					TryResult resSelected = null;
					double r = OtherUtility.rand.nextDouble();
					double sum = workWeightTotal;
					for (TryResult res : ress) {
						Integer index = res2indexWork.get(res);
						if (index == null) continue;
						sum += workWeightsVirtual[index];
						if (r < sum / workWeightTotalAll) {
							resSelected = workResultsVirtual.get(index);
							break;
						}
					}

					if (resSelected instanceof TraderWorkResult) {
						TraderWorkResult result = (TraderWorkResult) resSelected;
						HumanStatus hs = new HumanStatus(mm, humanStatus);
						hs.DoTrader(result);
					} else if (resSelected instanceof MakerWorkResult) {
						MakerWorkResult result = (MakerWorkResult) resSelected;
						HumanStatus hs = new HumanStatus(mm, humanStatus);
						hs.DoMaker(result);
					}
				} else if (consumeWeightTotalAll > 0) {

					TryResult resSelected = null;
					double r = OtherUtility.rand.nextDouble();
					double sum = consumeWeightTotal;

					for (TryResult res : ress) {
						Integer index = res2indexConsume.get(res);
						if (index == null) continue;
						sum += consumeWeightsVirtual[index];
						if (r < sum / consumeWeightTotalAll) {
							resSelected = consumeResultsVirtual.get(index);
							break;
						}
					}

					if (resSelected instanceof NopResult) {
					} else if (resSelected instanceof ConsumeResult) {
						ConsumeResult result = (ConsumeResult) resSelected;
						HumanStatus hs = new HumanStatus(mm, humanStatus);
						hs.DoConsume(result);
					}
				} else {
					// do nothing
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
			if (workWeightTotal > 0) {
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
			} else if (consumeWeightTotal > 0) {
				for (int i = 0; i < numConsumeResults; i++) {
					TryResult resSelected = consumeResults.get(i);
					double prob = consumeWeights[i] / consumeWeightTotal;
					if (resSelected instanceof NopResult) {
					} else if (resSelected instanceof ConsumeResult) {
						ConsumeResult result = (ConsumeResult) resSelected;
						result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, 1.0 * prob);
					}
				}
			} else {
				// do nothing
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
