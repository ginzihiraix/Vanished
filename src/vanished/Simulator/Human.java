package vanished.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import vanished.Simulator.HumanStatus.TryBuyResult;
import vanished.Simulator.HumanStatus.TryConsumeResult;
import vanished.Simulator.HumanStatus.TryConsumeWithWorkResult;
import vanished.Simulator.HumanStatus.TryMakerResult;
import vanished.Simulator.HumanStatus.TryResult;
import vanished.Simulator.HumanStatus.TrySellResult;
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
		ArrayList<ArrayList<TryResult>> resultsReal = new ArrayList<ArrayList<TryResult>>();
		ArrayList<ArrayList<TryResult>> resultsVirtual = new ArrayList<ArrayList<TryResult>>();
		for (int frame = 0; frame < 10000; frame++) {
			try {
				ArrayList<TryResult> res = new ArrayList<TryResult>();
				boolean realFlag = true;
				{
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(3);
					switch (actionType) {
					case 0: // Trader
						realFlag = humanStatusNew.TryTraderAndConsume(realFlag, res);
						break;
					case 1: // Maker
						realFlag = humanStatusNew.TryMakerAndConsume(realFlag, res);
						break;
					case 2:// MakerInKind
						realFlag = humanStatusNew.TryConsumeWithWork(realFlag, res);
						break;
					}
				}

				if (realFlag == true) {
					resultsReal.add(res);
				} else {
					resultsVirtual.add(res);
				}

				if (resultsReal.size() > 30) break;
			} catch (HumanSimulationException e) {
				// e.printStackTrace();
			}
		}

		// ���s����B
		this.BBB(resultsReal, resultsVirtual);

		// ����_���v
		this.humanStatus.Dump();
	}

	private void BBB(ArrayList<ArrayList<TryResult>> resultsReal, ArrayList<ArrayList<TryResult>> resultsVirtual) throws Exception {

		// �o�ꂷ�鉼�zRoom��񋓂���B���zRoom���֘A����TryResult�𔲂��o���B
		HashMap<Room, ArrayList<ArrayList<TryResult>>> virtualRoom2ResultSequence = new HashMap<Room, ArrayList<ArrayList<TryResult>>>();
		HashMap<Room, ArrayList<ArrayList<TryResult>>> realRoom2ResultSequence = new HashMap<Room, ArrayList<ArrayList<TryResult>>>();
		{
			for (ArrayList<TryResult> resultSequence : resultsVirtual) {

				for (TryResult res : resultSequence) {

					Room roomVirtual = null;
					Room roomReal = null;

					if (res instanceof TryBuyResult) {
						TryBuyResult result = (TryBuyResult) res;
						if (result.shopRoom.IsReal() == false) {
							roomVirtual = result.shopRoom;
						} else {
							roomReal = result.shopRoom;
						}
					} else if (res instanceof TrySellResult) {
						TrySellResult result = (TrySellResult) res;
						if (result.deliverRoom.IsReal() == false) {
							roomVirtual = result.deliverRoom;
						} else {
							roomReal = result.deliverRoom;
						}
					} else if (res instanceof TryConsumeResult) {
						TryConsumeResult result = (TryConsumeResult) res;
						if (result.shopRoom.IsReal() == false) {
							roomVirtual = result.shopRoom;
						} else {
							roomReal = result.shopRoom;
						}
					} else if (res instanceof TryMakerResult) {
						TryMakerResult result = (TryMakerResult) res;
						if (result.factoryRoom.IsReal() == false) {
							roomVirtual = result.factoryRoom;
						} else {
							roomReal = result.factoryRoom;
						}
					} else if (res instanceof TryConsumeWithWorkResult) {
						TryConsumeWithWorkResult result = (TryConsumeWithWorkResult) res;
						if (result.factoryRoom.IsReal() == false) {
							roomVirtual = result.factoryRoom;
						} else {
							roomReal = result.factoryRoom;
						}
					} else {
						throw new Exception("fatail error");
					}

					if (roomVirtual != null) {
						ArrayList<ArrayList<TryResult>> results = virtualRoom2ResultSequence.get(roomVirtual);
						if (results == null) {
							results = new ArrayList<ArrayList<TryResult>>();
							virtualRoom2ResultSequence.put(roomVirtual, results);
						}
						results.add(resultSequence);
					}
					if (roomReal != null) {
						ArrayList<ArrayList<TryResult>> results = realRoom2ResultSequence.get(roomReal);
						if (results == null) {
							results = new ArrayList<ArrayList<TryResult>>();
							virtualRoom2ResultSequence.put(roomReal, results);
						}
						results.add(resultSequence);
					}
				}
			}
		}

		// �񋓂����o�ꂷ��e���z�������ЂƂ����܂��āA�V�~�����[�V���������s���Ă݂�B
		for (Entry<Room, ArrayList<ArrayList<TryResult>>> e : virtualRoom2ResultSequence.entrySet()) {
			Room roomTarget = e.getKey();
			ArrayList<ArrayList<TryResult>> resultsTarget = e.getValue();
			ArrayList<ArrayList<TryResult>> results2 = new ArrayList<ArrayList<TryResult>>();
			results2.addAll(resultsReal);
			results2.addAll(resultsTarget);
			this.EvaluateAndExecuteAction(results2, roomTarget);
		}

		// �s����₩�烉���_���ɂЂƂ�I�����āA���s����B
		this.EvaluateAndExecuteAction(resultsReal, null);
	}

	private void EvaluateAndExecuteAction(ArrayList<ArrayList<TryResult>> results, Room roomVirtual) throws Exception {
		// //////////////////////////////////////////////
		// �I���m�����v�Z����B
		// //////////////////////////////////////////////
		int numSequence = results.size();

		double scoreMax = -Double.MAX_VALUE;
		double scoreMin = Double.MAX_VALUE;
		double[] scores = new double[numSequence];
		for (int i = 0; i < numSequence; i++) {
			ArrayList<TryResult> sequence = results.get(i);
			TryResult resStart = sequence.get(0);
			TryResult resEnd = sequence.get(sequence.size() - 1);
			double duration = resEnd.timeEnd - resStart.timeStart;
			double utilDelta = resEnd.utilEnd - resStart.utilStart;
			double score = utilDelta / duration;
			scores[i] = score;
			if (score > scoreMax) scoreMax = score;
			if (score < scoreMin) scoreMin = score;
		}

		// �I���m�����v�Z����B
		double[] weights = new double[numSequence];
		double weightTotal = 0;
		{
			// TODO:rate�����K�v
			double rate = 10;
			double scoreDelta = scoreMax - scoreMin;
			if (scoreDelta == 0) scoreDelta = 1;

			for (int i = 0; i < numSequence; i++) {
				double weight = Math.exp((scores[i] - scoreMin) / scoreDelta * rate);
				weights[i] = weight;
				weightTotal += weight;
			}
		}

		double[] prob = new double[numSequence];
		for (int i = 0; i < numSequence; i++) {
			prob[i] = weights[i] / weightTotal;
		}

		if (roomVirtual == null) {
			// //////////////////////////////////////////////
			// weight�Ń����_���ɃA�N�V������I�����Ď��s����B
			// //////////////////////////////////////////////
			if (weightTotal > 0) {
				int index = this.ChooseRandomly(prob);
				if (index == -1) index = results.size() - 1;
				ArrayList<TryResult> sequence = results.get(index);
				humanStatus.DoAction(sequence);
			} else {
				humanStatus.DoNop();
			}

			// //////////////////////////////////////////////
			// ���Ғl��Feedback����B
			// //////////////////////////////////////////////
			if (weightTotal > 0) {
				for (int i = 0; i < numSequence; i++) {
					ArrayList<TryResult> sequence = results.get(i);
					double p = prob[i];

					for (TryResult res : sequence) {
						if (res instanceof TryBuyResult) {
							TryBuyResult result = (TryBuyResult) res;
							result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
						} else if (res instanceof TrySellResult) {
							TrySellResult result = (TrySellResult) res;
							result.deliverRoom.FeedbackAboutDeliverPrice(result.callForItem.itemDef, result.callForItem.price,
									result.callForItem.numPick * p);
						} else if (res instanceof TryConsumeResult) {
							TryConsumeResult result = (TryConsumeResult) res;

							// TODO
							if (result.itemCatalog.itemDef.GetName().equals("�\�[�Z�[�W")) {
								int a = 0;
							}

							result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
						} else if (res instanceof TryMakerResult) {
							TryMakerResult result = (TryMakerResult) res;
							result.factoryRoom.FeedbackAboutMakerPrice(result.cfm, result.cfm.wageForFullWork, result.cfm.numMake * p);
						} else if (res instanceof TryConsumeWithWorkResult) {
							TryConsumeWithWorkResult result = (TryConsumeWithWorkResult) res;
						} else {
							throw new Exception("fatail error");
						}
					}
				}
			}
		} else {
			// //////////////////////////////////////////////
			// weight�Ń����_���ɃA�N�V������I�����Ď��s����B���z�����ɑ΂��Ă̂݁B
			// //////////////////////////////////////////////
			if (weightTotal > 0) {
				int index = this.ChooseRandomly(prob);
				ArrayList<TryResult> sequence = results.get(index);
				for (TryResult res : sequence) {
					if (res instanceof TryBuyResult) {
						TryBuyResult result = (TryBuyResult) res;
						if (result.shopRoom == roomVirtual) {
							result.shopRoom.BuyProductItem(humanStatus.timeSimulationComplete, result.itemCatalog, false);
						}
					} else if (res instanceof TrySellResult) {
						TrySellResult result = (TrySellResult) res;
						if (result.deliverRoom == roomVirtual) {
							result.deliverRoom.SellItem(humanStatus.timeSimulationComplete, result.callForItem, false);
						}
					} else if (res instanceof TryConsumeResult) {
						TryConsumeResult result = (TryConsumeResult) res;
						if (result.shopRoom == roomVirtual) {
							result.shopRoom.BuyProductItem(humanStatus.timeSimulationComplete, result.itemCatalog, false);
						}
					} else if (res instanceof TryMakerResult) {
						TryMakerResult result = (TryMakerResult) res;
						if (result.factoryRoom == roomVirtual) {
							result.factoryRoom.Make(result.cfm, humanStatus.timeSimulationComplete, false);
						}
					} else if (res instanceof TryConsumeWithWorkResult) {
						TryConsumeWithWorkResult result = (TryConsumeWithWorkResult) res;
					} else {
						throw new Exception("fatail error");
					}
				}
			} else {
				System.out.println("��邱�ƂȂ�");
			}

			// //////////////////////////////////////////////
			// ���Ғl��Feedback����B���z�����ɑ΂��Ă̂݁B
			// //////////////////////////////////////////////
			if (weightTotal > 0) {
				for (int i = 0; i < numSequence; i++) {
					ArrayList<TryResult> sequence = results.get(i);
					double p = prob[i];

					for (TryResult res : sequence) {
						if (res instanceof TryBuyResult) {
							TryBuyResult result = (TryBuyResult) res;
							if (result.shopRoom == roomVirtual) {
								result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
							}
						} else if (res instanceof TrySellResult) {
							TrySellResult result = (TrySellResult) res;
							if (result.deliverRoom == roomVirtual) {
								result.deliverRoom.FeedbackAboutDeliverPrice(result.callForItem.itemDef, result.callForItem.price,
										result.callForItem.numPick * p);
							}
						} else if (res instanceof TryConsumeResult) {
							TryConsumeResult result = (TryConsumeResult) res;
							if (result.shopRoom == roomVirtual) {
								result.shopRoom.FeedbackAboutProductPrice(result.itemCatalog.price, result.itemCatalog.numPick * p);
							}
						} else if (res instanceof TryMakerResult) {
							TryMakerResult result = (TryMakerResult) res;
							if (result.factoryRoom == roomVirtual) {
								result.factoryRoom.FeedbackAboutMakerPrice(result.cfm, result.cfm.wageForFullWork, result.cfm.numMake * p);
							}
						} else if (res instanceof TryConsumeWithWorkResult) {
							TryConsumeWithWorkResult result = (TryConsumeWithWorkResult) res;
							if (result.factoryRoom == roomVirtual) {
							}
						} else {
							throw new Exception("fatail error");
						}
					}
				}
			} else {
				System.out.println("��邱�ƂȂ�");
			}
		}
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
