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

		// ���s����B
		this.BBB(resultsReal, resultsVirtual);

		// �󋵂��_���v
		this.humanStatus.Dump();
	}

	private void BBB(ArrayList<ArrayList<Action>> resultsReal, ArrayList<ArrayList<Action>> resultsVirtual) throws Exception {

		// �o�ꂷ�鉼�zRoom��񋓂���B���zRoom���֘A����TryResult�𔲂��o���B
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

		// �o�ꂷ��A�C�e����񋓂��A�A�C�e�����ƂɁA���i��Min�AMax���v�Z���Ă����B
		HashMap<ItemDef, int[]> productItemPriceMap = new HashMap<ItemDef, int[]>();
		HashMap<ItemDef, int[]> materialItemPriceMap = new HashMap<ItemDef, int[]>();
		int[] makerPriceMap = new int[2];
		{
			makerPriceMap[0] = Integer.MAX_VALUE;
			makerPriceMap[1] = -Integer.MAX_VALUE;

			for (ArrayList<Action> resultSequence : resultsReal) {
				Room roomVirtual = null;
				for (Action res : resultSequence) {
					if (res instanceof TraderAction) {
						TraderAction result = (TraderAction) res;

						{
							CallForItem cfi = result.deliverRoom.GetDesiredItem(result.itemDef, Double.MAX_VALUE, Double.MAX_VALUE);
							int[] priceset = materialItemPriceMap.get(cfi.itemDef);
							if (priceset == null) {
								priceset = new int[2];
								priceset[0] = Integer.MAX_VALUE;
								priceset[1] = -Integer.MAX_VALUE;
								materialItemPriceMap.put(result.itemDef, priceset);
							}
							if (cfi.price < priceset[0]) {
								priceset[0] = cfi.priceIndex;
							}
							if (cfi.price > priceset[1]) {
								priceset[1] = cfi.priceIndex;
							}
						}

						{
							ItemCatalog ic = result.shopRoom.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);
							int[] priceset = productItemPriceMap.get(result.itemDef);
							if (priceset == null) {
								priceset = new int[2];
								priceset[0] = Integer.MAX_VALUE;
								priceset[1] = -Integer.MAX_VALUE;
								productItemPriceMap.put(result.itemDef, priceset);
							}
							if (ic.price < priceset[0]) {
								priceset[0] = ic.priceIndex;
							}
							if (ic.price > priceset[1]) {
								priceset[1] = ic.priceIndex;
							}
						}

					} else if (res instanceof MakerAction) {
						MakerAction result = (MakerAction) res;

						CallForMaker cfm = result.factoryRoom.GetDesiredMaker(Double.MAX_VALUE);
						if (cfm.wageForFullWork < makerPriceMap[0]) {
							makerPriceMap[0] = cfm.wageIndex;
						}
						if (cfm.wageForFullWork > makerPriceMap[1]) {
							makerPriceMap[1] = cfm.wageIndex;
						}

					} else if (res instanceof ConsumerAction) {
						ConsumerAction result = (ConsumerAction) res;

						{
							ItemCatalog ic = result.shopRoom.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);
							int[] priceset = productItemPriceMap.get(result.itemDef);
							if (priceset == null) {
								priceset = new int[2];
								priceset[0] = Integer.MAX_VALUE;
								priceset[1] = -Integer.MAX_VALUE;
								productItemPriceMap.put(result.itemDef, priceset);
							}
							if (ic.price < priceset[0]) {
								priceset[0] = ic.priceIndex;
							}
							if (ic.price > priceset[1]) {
								priceset[1] = ic.priceIndex;
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

		// �񋓂����o�ꂷ��e���z�������ЂƂ����܂��āA�V�~�����[�V���������s���Ă݂�B
		for (Entry<Room, ArrayList<ArrayList<Action>>> e : virtualRoom2ResultSequence.entrySet()) {
			Room roomTarget = e.getKey();
			ArrayList<ArrayList<Action>> resultsTarget = e.getValue();
			ArrayList<ArrayList<Action>> results2 = new ArrayList<ArrayList<Action>>();
			results2.addAll(resultsReal);
			results2.addAll(resultsTarget);

			this.EvaluateAndFeedbackAction(results2, roomTarget, productItemPriceMap, materialItemPriceMap, makerPriceMap);
		}

		// ���i��SWEEP���Ȃ���A�s�����̑I���m����]�����āA���i�ɂ����Ғl��Feedback����B
		this.EvaluateAndFeedbackAction(resultsReal, null, productItemPriceMap, materialItemPriceMap, makerPriceMap);

		// �s����₩�烉���_���ɂЂƂ�I�����āA���s����B
		this.EvaluateAndExecuteAction(resultsReal);
	}

	private void EvaluateAndFeedbackAction(ArrayList<ArrayList<Action>> actionSequenceList, Room roomVirtual,
			HashMap<ItemDef, int[]> productItemPriceMap, HashMap<ItemDef, int[]> materialItemPriceMap, int[] makerPriceMap) throws Exception {

		// Action�ɓo�ꂷ�镔���ꗗ�����B
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

		// �������ɁA���i�̔����i�A�ޗ����承�i�A�����������i�A�𓮂����āA�e�A�N�V�����̑I���m�����v�Z����B
		{
			for (Room roomPriceTest : roomPriceTestMap.keySet()) {

				// �J���҂̒����𓮂����āAFeedback����B
				if (roomPriceTest instanceof FactoryRoom) {
					FactoryRoom factoryRoomPriceTest = (FactoryRoom) roomPriceTest;

					CallForMaker cfmOrg = factoryRoomPriceTest.GetDesiredMaker(Double.MAX_VALUE);

					int indexMin = cfmOrg.wageIndex - 10;
					int indexMax = cfmOrg.wageIndex + 10;
					if (true) {
						int indexMax2 = makerPriceMap[0];
						int indexMin2 = makerPriceMap[1];
						if (indexMax2 > indexMax) indexMax = indexMax2;
						if (indexMin2 < indexMin) indexMin = indexMin2;
					}

					for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
						factoryRoomPriceTest.FeedbackAboutMakerPrice(cfmOrg, priceIndex, 0);
					}

					for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
						factoryRoomPriceTest.SetMakerPriceIndex(priceIndex);
						this.Feedback(actionSequenceList, 2, roomPriceTest);
					}
					factoryRoomPriceTest.SetMakerPriceIndex(cfmOrg.wageIndex);
				}

				// ���i���i�𓮂����āAFeedback����B
				if (roomPriceTest instanceof ShopRoom) {

					ShopRoom shopRoomPriceTest = (ShopRoom) roomPriceTest;
					ItemCatalog icOrg = shopRoomPriceTest.GetProductItem(Double.MAX_VALUE, Double.MAX_VALUE);

					int indexMin = icOrg.priceIndex - 10;
					int indexMax = icOrg.priceIndex + 10;
					if (true) {
						int indexMax2;
						int indexMin2;
						{
							int[] productPriceset = productItemPriceMap.get(icOrg.itemDef);
							int[] materialPriceset = materialItemPriceMap.get(icOrg.itemDef);
							indexMin2 = productPriceset[0] < materialPriceset[0] ? productPriceset[0] : materialPriceset[0];
							indexMax2 = productPriceset[1] > materialPriceset[1] ? productPriceset[1] : materialPriceset[1];
						}
						if (indexMax2 > indexMax) indexMax = indexMax2;
						if (indexMin2 < indexMin) indexMin = indexMin2;
					}

					for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
						shopRoomPriceTest.FeedbackAboutProductPrice(priceIndex, 0);
					}

					for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
						shopRoomPriceTest.SetProductPriceIndex(priceIndex);
						this.Feedback(actionSequenceList, 1, roomPriceTest);
					}
					shopRoomPriceTest.SetProductPriceIndex(icOrg.priceIndex);
				}

				// �ޗ��̔�����艿�i�𓮂����āAFeedback����B
				if (roomPriceTest instanceof DeliverRoom) {

					DeliverRoom deliverRoomPriceTest = (DeliverRoom) roomPriceTest;
					ArrayList<CallForItem> list = deliverRoomPriceTest.GetDesiredItemList(Double.MAX_VALUE, Double.MAX_VALUE);
					for (CallForItem cfiOrg : list) {

						int indexMin = cfiOrg.priceIndex - 10;
						int indexMax = cfiOrg.priceIndex + 10;
						if (true) {
							int indexMax2;
							int indexMin2;
							{
								int[] productPriceset = productItemPriceMap.get(cfiOrg.itemDef);
								int[] materialPriceset = materialItemPriceMap.get(cfiOrg.itemDef);
								indexMin2 = productPriceset[0] < materialPriceset[0] ? productPriceset[0] : materialPriceset[0];
								indexMax2 = productPriceset[1] > materialPriceset[1] ? productPriceset[1] : materialPriceset[1];
							}
							if (indexMax2 > indexMax) indexMax = indexMax2;
							if (indexMin2 < indexMin) indexMin = indexMin2;
						}

						for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
							deliverRoomPriceTest.FeedbackAboutDeliverPrice(cfiOrg.itemDef, priceIndex, 0);
						}

						for (int priceIndex = indexMin; priceIndex <= indexMax; priceIndex++) {
							deliverRoomPriceTest.SetMaterialPriceIndex(cfiOrg.itemDef, priceIndex);
							this.Feedback(actionSequenceList, 0, roomPriceTest);
						}
						deliverRoomPriceTest.SetMaterialPriceIndex(cfiOrg.itemDef, cfiOrg.priceIndex);
					}
				}
			}
		}
	}

	private void Feedback(ArrayList<ArrayList<Action>> actionSequenceList, int feedBackTarget, Room roomPriceTest) throws Exception {
		int numActionSequence = actionSequenceList.size();
		ExecutePack[] ep = this.ComputeExecutePack(actionSequenceList);
		if (ep == null) return;

		// �t�B�[�h�o�b�N��^����B
		for (int i = 0; i < numActionSequence; i++) {
			ArrayList<ExecuteResult> resultSequence = ep[i].resultSequence;
			if (resultSequence == null) continue;
			double p = ep[i].prob;

			for (ExecuteResult res : resultSequence) {
				if (res instanceof TraderExecuteResult) {
					TraderExecuteResult result = (TraderExecuteResult) res;
					if (roomPriceTest == result.action.shopRoom && feedBackTarget == 1) {
						result.action.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.priceIndex, result.itemCatalog.numPick * p);
					}

					if (roomPriceTest == result.action.deliverRoom && feedBackTarget == 0) {
						result.action.deliverRoom.FeedbackAboutDeliverPrice(result.callForItem.itemDef, result.callForItem.priceIndex,
								result.callForItem.numPick * p);
					}
				} else if (res instanceof MakerExecuteResult) {
					MakerExecuteResult result = (MakerExecuteResult) res;
					if (roomPriceTest == result.action.factoryRoom && feedBackTarget == 2) {
						result.action.factoryRoom.FeedbackAboutMakerPrice(result.caalForMaker, result.caalForMaker.wageIndex,
								result.caalForMaker.numMake * p);
					}
				} else if (res instanceof ConsumerExecuteResult) {
					ConsumerExecuteResult result = (ConsumerExecuteResult) res;
					if (roomPriceTest == result.action.shopRoom && feedBackTarget == 1) {
						result.action.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.priceIndex, result.itemCatalog.numPick * p);
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

		// �I���m�����v�Z����B
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

			// �I���m�����v�Z����B
			double[] weights = new double[numActionSequence];
			double weightTotal = 0;
			{
				// TODO:rate�����K�v
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
