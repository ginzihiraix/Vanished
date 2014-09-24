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

		// TODO:�������Ȃ��Ȃ��Ă���A�Ƃ肠��������
		// if (humanStatus.money < 1000) {
		// humanStatus.money += 1000;
		// }

		ArrayList<TryResult> workResults = new ArrayList<TryResult>();
		ArrayList<TryResult> workResultsVirtual = new ArrayList<TryResult>();
		if (humanStatus.ShouldWork() == true) {
			// �����Ȃ��Ƃ����Ȃ��ꍇ
			// �����_���ɍs���𐶐����āA�P�ʎ��ԓ�����ɉ҂���z����ԑ傫���Ȃ�d����I������B
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
			// �����Ȃ��Ă������ꍇ�A�܂��͐E���Ȃ���ԁB
			// �����_���ɍs����I�����āA�P�ʔ�p������ɑ���������p����ԑ傫���Ȃ�s�������肷��B
			for (int frame = 0; frame < 1000; frame++) {
				try {
					TryResult res = null;
					HumanStatus humanStatusNew = new HumanStatus(mm, humanStatus);
					int actionType = OtherUtility.rand.nextInt(2);
					switch (actionType) {
					case 0: { // �������Ȃ�
						res = humanStatusNew.TryNop();
						break;
					}
					case 1: { // �H�ׂ�
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

		// ���s����B
		// this.ExecuteAction(workResults, consumeResults, workResultsVirtual, consumeResultsVirtual);
		this.AAA(workResults, consumeResults, workResultsVirtual, consumeResultsVirtual);

		// ����_���v
		this.humanStatus.Dump();
	}

	private void AAA(ArrayList<TryResult> workResults, ArrayList<TryResult> consumeResults, ArrayList<TryResult> workResultsVirtual,
			ArrayList<TryResult> consumeResultsVirtual) throws Exception {

		// �o�ꂷ�鉼�zRoom��񋓂���B���zRoom���֘A����TryResult�𔲂��o���B
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

		// �񋓂����o�ꂷ��e���z�������ЂƂ����܂��āA�V�~�����[�V���������s���Ă݂�B
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
		// �I���m�����v�Z����BWork
		// //////////////////////////////////////////////
		int numWorkResults = workResults.size();

		// �X�R�A���v�Z����B
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

		// �I���m�����v�Z����B
		double[] workWeights = new double[numWorkResults];
		double workWeightTotal = 0;
		{
			// TODO:rate�����K�v
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
		// �I���m�����v�Z����BConsume
		// //////////////////////////////////////////////
		double wagePerMin = this.humanStatus.wageMovingAverage.GetAverage(this.humanStatus.timeSimulationComplete);
		if (wagePerMin == 0) wagePerMin = 1.0e-10;
		double minToGainUnitMoney = 1 / wagePerMin;

		int numConsumeResults = consumeResults.size();

		// �X�R�A���v�Z����B
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

		// �I���m�����v�Z����B
		double[] consumeWeights = new double[numConsumeResults];
		double consumeWeightTotal = 0;
		{
			// TODO:rate�����K�v
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
		// weight�Ń����_���ɃA�N�V������I�����Ď��s����B
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
					// TODO:���z�����̉��z�V�~�����[�V�����ɂ��āA�ǂ��ɂ�����B
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
		// ���Ғl��Feedback����B
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
		// �I���m�����v�Z����BWork
		// //////////////////////////////////////////////
		int numWorkResults = workResults.size();

		// �X�R�A���v�Z����B
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

		// �I���m�����v�Z����B
		double[] workWeights = new double[numWorkResults];
		double workWeightTotal = 0;
		{
			// TODO:rate�����K�v
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
		// �I���m�����v�Z����BWorkVirtual
		// //////////////////////////////////////////////
		int numWorkResultsVirtual = workResultsVirtual.size();

		// �X�R�A���v�Z����B
		double[] workScoresVirtual = new double[numWorkResultsVirtual];
		{
			for (int i = 0; i < numWorkResultsVirtual; i++) {
				TryResult res = workResultsVirtual.get(i);
				double score = (res.moneyEnd - res.moneyStart) / (res.timeEnd - res.timeStart);
				workScoresVirtual[i] = score;
			}
		}

		// �I���m�����v�Z����B
		double[] workWeightsVirtual = new double[numWorkResultsVirtual];
		{
			// TODO:rate�����K�v
			double rate = 3;
			if (workScoreMax > 0) {
				for (int i = 0; i < numWorkResultsVirtual; i++) {
					double weight = Math.exp(workScoresVirtual[i] / workScoreMax * rate);
					workWeightsVirtual[i] = weight;
				}
			}
		}

		// //////////////////////////////////////////////
		// �I���m�����v�Z����BConsume
		// //////////////////////////////////////////////
		double wagePerMin = this.humanStatus.wageMovingAverage.GetAverage(this.humanStatus.timeSimulationComplete);
		if (wagePerMin == 0) wagePerMin = 1.0e-10;
		double minToGainUnitMoney = 1 / wagePerMin;

		int numConsumeResults = consumeResults.size();

		// �X�R�A���v�Z����B
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

		// �I���m�����v�Z����B
		double[] consumeWeights = new double[numConsumeResults];
		double consumeWeightTotal = 0;
		{
			// TODO:rate�����K�v
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
		// �I���m�����v�Z����BConsume Virtual
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
			// TODO:rate�����K�v
			double rate = 3;
			if (consumeScoreMax > 0) {
				for (int i = 0; i < numConsumeResultsVirtual; i++) {
					double weight = Math.exp(consumeScoresVirtual[i] / consumeScoreMax * rate);
					consumeWeightsVirtual[i] = weight;
				}
			}
		}

		// ///////////////////////////////////////////////////
		// �eVirtualRoom�ɑ΂��āAReal�������Ƃ��̑I���s�������z�I�Ɏ��s����B
		// ///////////////////////////////////////////////////
		{
			// TryResult����Index�ւ̃}�b�s���O���\�z����BWork��
			HashMap<TryResult, Integer> res2indexWork = new HashMap<TryResult, Integer>();
			for (int i = 0; i < numWorkResultsVirtual; i++) {
				TryResult res = workResultsVirtual.get(i);
				res2indexWork.put(res, i);
			}

			// TryResult����Index�ւ̃}�b�s���O���\�z����BConsume��
			HashMap<TryResult, Integer> res2indexConsume = new HashMap<TryResult, Integer>();
			for (int i = 0; i < numConsumeResultsVirtual; i++) {
				TryResult res = consumeResultsVirtual.get(i);
				res2indexConsume.put(res, i);
			}

			// �o�ꂷ�鉼�zRoom��񋓂���B���zRoom���֘A����TryResult�𔲂��o���B
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

			// �e���zRoom��Real���������́A�A�N�V�����̑I���m�����v�Z���āA���zRoom�ɑ΂���Feedback��^����B
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
		// weight�Ń����_���ɃA�N�V������I�����Ď��s����B
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
		// ���Ғl��Feedback����B
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
