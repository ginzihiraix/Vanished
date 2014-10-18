package vanished.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import vanished.Simulator.HumanStatus.Action;
import vanished.Simulator.HumanStatus.ConsumerAction;
import vanished.Simulator.HumanStatus.ConsumerExecuteResult;
import vanished.Simulator.HumanStatus.ExecuteResult;
import vanished.Simulator.HumanStatus.MakeAndConsumeAction;
import vanished.Simulator.HumanStatus.MakeAndConsumeExecuteResult;
import vanished.Simulator.HumanStatus.MakerAction;
import vanished.Simulator.HumanStatus.MakerExecuteResult;
import vanished.Simulator.HumanStatus.TraderAction;
import vanished.Simulator.HumanStatus.TraderExecuteResult;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Structure.DeliverRoom;
import vanished.Simulator.Structure.DeliverRoom.CallForItem;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.FactoryRoom.CallForMaker;
import vanished.Simulator.Structure.Room;
import vanished.Simulator.Structure.ShopRoom;
import vanished.Simulator.Structure.ShopRoom.ItemCatalog;

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
		ArrayList<ArrayList<Action>> resultsReal = new ArrayList<ArrayList<Action>>();
		ArrayList<ArrayList<Action>> resultsVirtual = new ArrayList<ArrayList<Action>>();
		for (int frame = 0; frame < 10000; frame++) {
			try {
				ArrayList<Action> res = new ArrayList<Action>();
				boolean realFlag = true;
				{
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(3);
					switch (actionType) {
					case 0: // Trader
						realFlag = humanStatusNew.SampleTraderAndConsume(realFlag, res);
						break;
					case 1: // Maker
						realFlag = humanStatusNew.SampleMakerAndConsume(realFlag, res);
						break;
					case 2:// MakerInKind
						realFlag = humanStatusNew.SampleConsumeWithWork(realFlag, res);
						break;
					}
				}

				if (realFlag == true) {
					resultsReal.add(res);
				} else {
					resultsVirtual.add(res);
				}

				if (resultsReal.size() >= 30) break;
			} catch (HumanSimulationException e) {
				// e.printStackTrace();
			}
		}

		// 実行する。
		this.BBB(resultsReal, resultsVirtual);

		// 情況をダンプ
		this.humanStatus.Dump();
	}

	private void BBB(ArrayList<ArrayList<Action>> resultsReal, ArrayList<ArrayList<Action>> resultsVirtual) throws Exception {

		// 登場する仮想Roomを列挙する。仮想Roomが関連するTryResultを抜き出す。
		HashMap<Room, ArrayList<ArrayList<Action>>> virtualRoom2ResultSequence = new HashMap<Room, ArrayList<ArrayList<Action>>>();
		{
			for (ArrayList<Action> resultSequence : resultsVirtual) {
				Room roomVirtual = null;
				for (Action res : resultSequence) {
					if (res instanceof TraderAction) {
						TraderAction result = (TraderAction) res;
						if (result.shopRoom.IsReal() == false) {
							roomVirtual = result.shopRoom;
						}
					} else if (res instanceof MakerAction) {
						MakerAction result = (MakerAction) res;
						if (result.factoryRoom.IsReal() == false) {
							roomVirtual = result.factoryRoom;
						}
					} else if (res instanceof ConsumerAction) {
						ConsumerAction result = (ConsumerAction) res;
						if (result.shopRoom.IsReal() == false) {
							roomVirtual = result.shopRoom;
						}
					} else if (res instanceof MakeAndConsumeAction) {
						MakeAndConsumeAction result = (MakeAndConsumeAction) res;
						if (result.makeAndConsumeRoom.IsReal() == false) {
							roomVirtual = result.makeAndConsumeRoom;
						}
					} else {
						throw new Exception("fatail error");
					}
				}

				ArrayList<ArrayList<Action>> results = virtualRoom2ResultSequence.get(roomVirtual);
				if (results == null) {
					results = new ArrayList<ArrayList<Action>>();
					virtualRoom2ResultSequence.put(roomVirtual, results);
				}
				results.add(resultSequence);
			}
		}

		// 登場するアイテムを列挙し、アイテムごとに、Min、Maxを計算しておく。
		HashMap<ItemDef, double[]> productItemPriceMap = new HashMap<ItemDef, double[]>();
		HashMap<ItemDef, double[]> materialItemPriceMap = new HashMap<ItemDef, double[]>();
		double[] makerPriceMap = new double[2];
		{
			makerPriceMap[0] = Double.MAX_VALUE;
			makerPriceMap[1] = -Double.MAX_VALUE;

			for (ArrayList<Action> resultSequence : resultsReal) {
				Room roomVirtual = null;
				for (Action res : resultSequence) {
					if (res instanceof TraderAction) {
						TraderAction result = (TraderAction) res;

						{
							CallForItem cfi = result.deliverRoom.GetDesiredItem(result.itemDef, Double.MAX_VALUE, Double.MAX_VALUE);
							double[] priceset = materialItemPriceMap.get(cfi.itemDef);
							if (priceset == null) {
								priceset = new double[2];
								priceset[0] = Double.MAX_VALUE;
								priceset[1] = -Double.MAX_VALUE;
								materialItemPriceMap.put(result.itemDef, priceset);
							}
							if (cfi.price < priceset[0]) {
								priceset[0] = cfi.price;
							}
							if (cfi.price > priceset[1]) {
								priceset[1] = cfi.price;
							}
						}

						{
							ItemCatalog ic = result.shopRoom.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);
							double[] priceset = productItemPriceMap.get(result.itemDef);
							if (priceset == null) {
								priceset = new double[2];
								priceset[0] = Double.MAX_VALUE;
								priceset[1] = -Double.MAX_VALUE;
								productItemPriceMap.put(result.itemDef, priceset);
							}
							if (ic.price < priceset[0]) {
								priceset[0] = ic.price;
							}
							if (ic.price > priceset[1]) {
								priceset[1] = ic.price;
							}
						}

					} else if (res instanceof MakerAction) {
						MakerAction result = (MakerAction) res;

						CallForMaker cfm = result.factoryRoom.GetDesiredMaker(Double.MAX_VALUE);
						if (cfm.wageForFullWork < makerPriceMap[0]) {
							makerPriceMap[0] = cfm.wageForFullWork;
						}
						if (cfm.wageForFullWork > makerPriceMap[1]) {
							makerPriceMap[1] = cfm.wageForFullWork;
						}

					} else if (res instanceof ConsumerAction) {
						ConsumerAction result = (ConsumerAction) res;

						{
							ItemCatalog ic = result.shopRoom.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);
							double[] priceset = productItemPriceMap.get(result.itemDef);
							if (priceset == null) {
								priceset = new double[2];
								priceset[0] = Double.MAX_VALUE;
								priceset[1] = -Double.MAX_VALUE;
								productItemPriceMap.put(result.itemDef, priceset);
							}
							if (ic.price < priceset[0]) {
								priceset[0] = ic.price;
							}
							if (ic.price > priceset[1]) {
								priceset[1] = ic.price;
							}
						}

					} else if (res instanceof MakeAndConsumeAction) {
						MakeAndConsumeAction result = (MakeAndConsumeAction) res;
						if (result.makeAndConsumeRoom.IsReal() == false) {
							roomVirtual = result.makeAndConsumeRoom;
						}
					} else {
						throw new Exception("fatail error");
					}
				}

				ArrayList<ArrayList<Action>> results = virtualRoom2ResultSequence.get(roomVirtual);
				if (results == null) {
					results = new ArrayList<ArrayList<Action>>();
					virtualRoom2ResultSequence.put(roomVirtual, results);
				}
				results.add(resultSequence);
			}
		}

		// 列挙した登場する各仮想部屋をひとつだけまぜて、シミュレーションを実行してみる。
		for (Entry<Room, ArrayList<ArrayList<Action>>> e : virtualRoom2ResultSequence.entrySet()) {
			Room roomTarget = e.getKey();
			ArrayList<ArrayList<Action>> resultsTarget = e.getValue();
			ArrayList<ArrayList<Action>> results2 = new ArrayList<ArrayList<Action>>();
			results2.addAll(resultsReal);
			results2.addAll(resultsTarget);

			this.EvaluateAndFeedbackAction(results2, roomTarget, productItemPriceMap, materialItemPriceMap, makerPriceMap);
		}

		// 価格をSWEEPしながら、行動候補の選択確率を評価して、価格による期待値をFeedbackする。
		this.EvaluateAndFeedbackAction(resultsReal, null, productItemPriceMap, materialItemPriceMap, makerPriceMap);

		// 行動候補からランダムにひとつを選択して、実行する。
		this.EvaluateAndExecuteAction(resultsReal);
	}

	private void EvaluateAndFeedbackAction(ArrayList<ArrayList<Action>> actionSequenceList, Room roomVirtual,
			HashMap<ItemDef, double[]> productItemPriceMap, HashMap<ItemDef, double[]> materialItemPriceMap, double[] makerPriceMap) throws Exception {

		// Actionに登場する部屋一覧を作る。
		HashMap<Room, Boolean> roomPriceTestMap = new HashMap<Room, Boolean>();
		for (ArrayList<Action> actionSequence : actionSequenceList) {
			Room roomPriceTest = null;
			for (Action action : actionSequence) {
				if (action instanceof TraderAction) {
					TraderAction result = (TraderAction) action;
					roomPriceTest = result.shopRoom;
				} else if (action instanceof MakerAction) {
					MakerAction result = (MakerAction) action;
					roomPriceTest = result.factoryRoom;
				} else if (action instanceof ConsumerAction) {
					ConsumerAction result = (ConsumerAction) action;
					roomPriceTest = result.shopRoom;
				} else if (action instanceof MakeAndConsumeAction) {
					MakeAndConsumeAction result = (MakeAndConsumeAction) action;
					roomPriceTest = result.makeAndConsumeRoom;
				} else {
					throw new Exception("fatail error");
				}
			}
			roomPriceTestMap.put(roomPriceTest, true);
		}

		// 部屋毎に、商品販売価格、材料買取価格、製造賃金価格、を動かして、各アクションの選択確率を計算する。
		{
			double stepSize = 1.05;

			for (Room roomPriceTest : roomPriceTestMap.keySet()) {

				// 労働者の賃金を動かして、Feedbackする。
				if (roomPriceTest instanceof FactoryRoom) {
					FactoryRoom factoryRoomPriceTest = (FactoryRoom) roomPriceTest;
					double wage = factoryRoomPriceTest.GetDesiredMaker(Double.MAX_VALUE).wageForFullWork;

					int indexMin = -10;
					int indexMax = +10;
					if (false) {
						double priceMin = makerPriceMap[0];
						double priceMax = makerPriceMap[1];
						if (false) {
							for (Room roomPriceTest2 : roomPriceTestMap.keySet()) {
								FactoryRoom factoryRoomPriceTest2 = (FactoryRoom) roomPriceTest2;
								double wage2 = factoryRoomPriceTest2.GetDesiredMaker(Double.MAX_VALUE).wageForFullWork;
								if (wage2 > priceMax) priceMax = wage2;
								if (wage2 < priceMin) priceMin = wage2;
							}
						}
						int indexMax2 = (int) (Math.log(priceMax / wage * 1.5) / Math.log(stepSize));
						int indexMin2 = (int) (Math.log(priceMin / wage / 1.5) / Math.log(stepSize));
						if (indexMax2 > indexMax) indexMax = indexMax2;
						if (indexMin2 < indexMin) indexMin = indexMin2;
					}

					for (int index = indexMin; index <= indexMax; index++) {
						double priceRate = Math.pow(stepSize, index);
						factoryRoomPriceTest.SetMakerPriceRate(priceRate);
						CallForMaker cfm = factoryRoomPriceTest.GetDesiredMaker(Double.MAX_VALUE);
						factoryRoomPriceTest.FeedbackAboutMakerPrice(cfm, cfm.wageForFullWork, 0);
					}

					for (int index = indexMin; index <= indexMax; index++) {
						double priceRate = Math.pow(stepSize, index);
						factoryRoomPriceTest.SetMakerPriceRate(priceRate);
						this.Feedback(actionSequenceList, 2, roomPriceTest);
					}
					factoryRoomPriceTest.SetMakerPriceRate(1);
				}

				// 商品価格を動かして、Feedbackする。
				if (roomPriceTest instanceof ShopRoom) {

					ShopRoom shopRoomPriceTest = (ShopRoom) roomPriceTest;

					ItemDef itemDef = shopRoomPriceTest.GetProductItemDef();
					double price = shopRoomPriceTest.GetProductItemPrice();

					int indexMin = -10;
					int indexMax = 10;
					if (false) {
						double priceMax = 0;
						double priceMin = Double.MAX_VALUE;
						{
							// double[] priceset = materialItemPriceMap.get(itemDef);
							// priceMin = priceset[0];
							// priceMax = priceset[1];
							double[] productPriceset = productItemPriceMap.get(itemDef);
							double[] materialPriceset = materialItemPriceMap.get(itemDef);
							priceMin = productPriceset[0] < materialPriceset[0] ? productPriceset[0] : materialPriceset[0];
							priceMax = productPriceset[1] > materialPriceset[1] ? productPriceset[1] : materialPriceset[1];
						}
						if (false) {
							for (Room roomPriceTest2 : roomPriceTestMap.keySet()) {
								DeliverRoom deliverRoomPriceTest2 = (DeliverRoom) roomPriceTest2;
								CallForItem cfi2 = deliverRoomPriceTest2.GetDesiredItem(itemDef, Double.MAX_VALUE, Double.MAX_VALUE);
								if (cfi2 == null) continue;
								double price2 = cfi2.price;
								if (price2 > priceMax) priceMax = price2;
								if (price2 < priceMin) priceMin = price2;
							}
						}
						int indexMax2 = (int) (Math.log(priceMax / price * 1.5) / Math.log(stepSize));
						int indexMin2 = (int) (Math.log(priceMin / price / 1.5) / Math.log(stepSize));
						if (indexMax2 > indexMax) indexMax = indexMax2;
						if (indexMin2 < indexMin) indexMin = indexMin2;
					}

					for (int index = indexMin; index <= indexMax; index++) {
						double priceRate = Math.pow(stepSize, index);
						shopRoomPriceTest.SetProductPriceRate(priceRate);
						ItemCatalog ic = shopRoomPriceTest.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);
						shopRoomPriceTest.FeedbackAboutProductPrice(ic.price, 0);
					}

					for (int index = indexMin; index <= indexMax; index++) {
						double priceRate = Math.pow(stepSize, index);
						shopRoomPriceTest.SetProductPriceRate(priceRate);
						this.Feedback(actionSequenceList, 1, roomPriceTest);
					}
					shopRoomPriceTest.SetProductPriceRate(1);
				}

				// 材料の買い取り価格を動かして、Feedbackする。
				if (roomPriceTest instanceof DeliverRoom) {

					DeliverRoom deliverRoomPriceTest = (DeliverRoom) roomPriceTest;
					ArrayList<CallForItem> list = deliverRoomPriceTest.GetDesiredItemList(Double.MAX_VALUE, Double.MAX_VALUE);
					for (CallForItem cfi : list) {

						ItemDef itemDef = cfi.itemDef;
						double price = cfi.price;

						int indexMin = -10;
						int indexMax = 10;
						if (false) {
							double priceMax = 0;
							double priceMin = Double.MAX_VALUE;
							{
								double[] productPriceset = productItemPriceMap.get(itemDef);
								double[] materialPriceset = materialItemPriceMap.get(itemDef);
								priceMin = productPriceset[0] < materialPriceset[0] ? productPriceset[0] : materialPriceset[0];
								priceMax = productPriceset[1] > materialPriceset[1] ? productPriceset[1] : materialPriceset[1];
							}
							if (false) {
								for (Room roomPriceTest2 : roomPriceTestMap.keySet()) {
									ShopRoom shopRoomPriceTest2 = (ShopRoom) roomPriceTest2;
									ItemDef itemDef2 = shopRoomPriceTest2.GetProductItemDef();
									if (itemDef != itemDef2) continue;
									double price2 = shopRoomPriceTest2.GetProductItemPrice();
									if (price2 > priceMax) priceMax = price2;
									if (price2 < priceMin) priceMin = price2;
								}
							}
							int indexMax2 = (int) (Math.log(priceMax / price * 1.5) / Math.log(stepSize));
							int indexMin2 = (int) (Math.log(priceMin / price / 1.5) / Math.log(stepSize));
							if (indexMax2 > indexMax) indexMax = indexMax2;
							if (indexMin2 < indexMin) indexMin = indexMin2;
						}

						for (int index = indexMin; index <= indexMax; index++) {
							double priceRate = Math.pow(stepSize, index);
							deliverRoomPriceTest.SetMaterialPriceRate(itemDef, priceRate);
							CallForItem cfi2 = deliverRoomPriceTest.GetDesiredItem(itemDef, Double.MAX_VALUE, Double.MAX_VALUE);
							deliverRoomPriceTest.FeedbackAboutDeliverPrice(itemDef, cfi2.price, 0);
						}

						for (int index = indexMin; index <= indexMax; index++) {
							double priceRate = Math.pow(stepSize, index);
							deliverRoomPriceTest.SetMaterialPriceRate(cfi.itemDef, priceRate);
							this.Feedback(actionSequenceList, 0, roomPriceTest);
						}
						deliverRoomPriceTest.SetMaterialPriceRate(cfi.itemDef, 1);
					}
				}
			}
		}
	}

	private void Feedback(ArrayList<ArrayList<Action>> actionSequenceList, int feedBackTarget, Room roomPriceTest) throws Exception {
		int numActionSequence = actionSequenceList.size();
		ExecutePack[] ep = this.ComputeExecutePack(actionSequenceList);
		if (ep == null) return;

		// フィードバックを与える。
		for (int i = 0; i < numActionSequence; i++) {
			ArrayList<ExecuteResult> resultSequence = ep[i].resultSequence;
			if (resultSequence == null) continue;
			double p = ep[i].prob;

			for (ExecuteResult res : resultSequence) {
				if (res instanceof TraderExecuteResult) {
					TraderExecuteResult result = (TraderExecuteResult) res;
					if (roomPriceTest == result.action.shopRoom && feedBackTarget == 1) {
						result.action.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
					}

					if (roomPriceTest == result.action.deliverRoom && feedBackTarget == 0) {
						result.action.deliverRoom.FeedbackAboutDeliverPrice(result.callForItem.itemDef, result.callForItem.price,
								result.callForItem.numPick * p);
					}
				} else if (res instanceof MakerExecuteResult) {
					MakerExecuteResult result = (MakerExecuteResult) res;
					if (roomPriceTest == result.action.factoryRoom && feedBackTarget == 2) {
						result.action.factoryRoom.FeedbackAboutMakerPrice(result.caalForMaker, result.caalForMaker.wageForFullWork,
								result.caalForMaker.numMake * p);
					}
				} else if (res instanceof ConsumerExecuteResult) {
					ConsumerExecuteResult result = (ConsumerExecuteResult) res;
					if (roomPriceTest == result.action.shopRoom && feedBackTarget == 1) {
						result.action.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
					}
				} else if (res instanceof MakeAndConsumeExecuteResult) {
				} else {
					throw new Exception("fatail error");
				}
			}
		}
	}

	private void EvaluateAndExecuteAction(ArrayList<ArrayList<Action>> actionSequenceList) throws Exception {
		int numActionSequence = actionSequenceList.size();
		ExecutePack[] ep = this.ComputeExecutePack(actionSequenceList);

		if (ep != null) {
			double[] prob = new double[numActionSequence];
			for (int i = 0; i < numActionSequence; i++) {
				prob[i] = ep[i].prob;
			}
			int index = this.ChooseRandomly(prob);

			humanStatus.ExecuteAction(actionSequenceList.get(index), false);
		} else {
			humanStatus.DoNop();
		}
	}

	class ExecutePack {
		ArrayList<ExecuteResult> resultSequence;
		double score;
		double prob;

		public ExecutePack(ArrayList<ExecuteResult> resultSequence, double score, double prob) {
			this.resultSequence = resultSequence;
			this.score = score;
			this.prob = prob;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ExecutePack[] ComputeExecutePack(ArrayList<ArrayList<Action>> actionSequenceList) throws Exception {
		int numActionSequence = actionSequenceList.size();

		ArrayList[] resultSequenceList = new ArrayList[numActionSequence];
		double[] scores = new double[numActionSequence];

		for (int i = 0; i < numActionSequence; i++) {
			ArrayList<Action> actionSequence = actionSequenceList.get(i);
			HumanStatus humanStatusTest = new HumanStatus(this.mm, this.humanStatus);
			long timeStart = humanStatusTest.timeSimulationComplete;
			double utilStart = humanStatusTest.ComputeUtility();

			try {
				ArrayList<ExecuteResult> resultSequence = humanStatusTest.ExecuteAction(actionSequence, true);
				resultSequenceList[i] = resultSequence;
			} catch (HumanSimulationException e) {
				// e.printStackTrace();
				continue;
			}

			long timeEnd = humanStatusTest.timeSimulationComplete;
			double utilEnd = humanStatusTest.ComputeUtility();

			long duration = timeEnd - timeStart;
			double utilDelta = utilEnd - utilStart;
			double score = utilDelta / duration;
			if (score < 0) score = 0;

			scores[i] = score;
		}

		// 選択確率を計算する。
		double[] prob = new double[numActionSequence];
		{
			double scoreMax = -Double.MAX_VALUE;
			double scoreMin = Double.MAX_VALUE;
			for (int i = 0; i < numActionSequence; i++) {
				if (resultSequenceList[i] == null) continue;
				double score = scores[i];
				if (score > scoreMax) scoreMax = score;
				if (score < scoreMin) scoreMin = score;
			}

			// 選択確率を計算する。
			double[] weights = new double[numActionSequence];
			double weightTotal = 0;
			{
				// TODO:rate調整必要
				double rate = 100;
				double scoreDelta = scoreMax - scoreMin;
				if (scoreDelta == 0) scoreDelta = 1;

				for (int i = 0; i < numActionSequence; i++) {
					if (resultSequenceList[i] == null) continue;
					double weight = Math.exp((scores[i] - scoreMin) / scoreDelta * rate);
					weights[i] = weight;
					weightTotal += weight;
				}
			}

			if (weightTotal == 0) return null;

			for (int i = 0; i < numActionSequence; i++) {
				prob[i] = weights[i] / weightTotal;
			}
		}

		ExecutePack[] ret = new ExecutePack[numActionSequence];
		for (int i = 0; i < numActionSequence; i++) {
			ret[i] = new ExecutePack(resultSequenceList[i], scores[i], prob[i]);
		}
		return ret;
	}

	private int ChooseRandomly(double[] prob) {
		int indexSelected = -1;
		double r = OtherUtility.rand.nextDouble();
		double sum = 0;
		for (int i = 0; i < prob.length; i++) {
			sum += prob[i];
			if (r < sum) {
				indexSelected = i;
				break;
			}
		}
		return indexSelected;
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
