package vanished.Simulator.Structure;

import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanManager;
import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.MapManager;
import vanished.Simulator.MovingAverage;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMakerInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMaterialInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryProductInfo;

public class FactoryRoom extends ShopRoom {

	FactoryProductManager factoryProductManager;

	public class FactoryMaterialManager {

		FactoryMaterialInfo factoryMaterialInfo;

		// �������蒆�̍ޗ�
		double shikakarichu = 0;

		public FactoryMaterialManager(FactoryMaterialInfo materialInfo) {
			this.factoryMaterialInfo = materialInfo;
		}
	}

	public class FactoryMakerManager {

		FactoryMakerInfo factoryMakerInfo;

		// �J���҂̒���
		double wage = 1 + OtherUtility.RandGaussian() * 0.1;

		public FactoryMakerManager(FactoryMakerInfo factoryMakerInfo) {
			this.factoryMakerInfo = factoryMakerInfo;
		}

		// ////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////
		// ���v�p
		// ////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////
		public MovingAverage speedAverage = new MovingAverage();
		public MovingAverage speedAverageSimulation = new MovingAverage();

		private FeedbackManager feedbackManager = new FeedbackManager();

		public void ResetStatisticalParameters() {
			feedbackManager.ResetStatisticalParameters();
		}

		public void Feedback(double price, double quantity) {
			feedbackManager.Add(price, quantity);
		}

	}

	public class FactoryProductManager {
		FactoryProductInfo factoryProductInfo;

		// �������蒆�̐��i
		double shikakarichu = 0;

		// ���i��numProductPerWork���̂ɕK�v�ȍޗ�
		TreeMap<ItemDef, FactoryMaterialManager> factoryMaterialManager = new TreeMap<ItemDef, FactoryMaterialManager>(new ItemDefComparator());

		// �����ł���l�ނ̃��X�g
		FactoryMakerManager factoryMakerManager;

		public FactoryProductManager(FactoryProductInfo factoryProductInfo) {
			this.factoryProductInfo = factoryProductInfo;

			for (Entry<ItemDef, FactoryMaterialInfo> e : factoryProductInfo.factoryMaterialInfo.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialInfo materialInfo = e.getValue();
				FactoryMaterialManager mm = new FactoryMaterialManager(materialInfo);
				this.factoryMaterialManager.put(materialItemDef, mm);
			}

			factoryMakerManager = new FactoryMakerManager(this.factoryProductInfo.factoryMakerInfo);
		}
	}

	public FactoryRoom(Building building, FactoryRoomDef roomDef) {
		super(building, roomDef);

		factoryProductManager = new FactoryProductManager(roomDef.factoryProductInfo);

		// ���i���i�����肷��B
		{
			double costMaterialTotal = 0;
			for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e2.getKey();
				FactoryMaterialManager fmm = e2.getValue();
				double cost = this.GetDesiredItem(materialItemDef).price * fmm.factoryMaterialInfo.amount;
				costMaterialTotal += cost;
			}

			double costMakerMax = this.factoryProductManager.factoryMakerManager.wage
					/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

			double costTotal = costMaterialTotal + costMakerMax;

			this.shopStockManager.price = costTotal + 1 + OtherUtility.rand.nextDouble() * 0.5;
		}
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		System.out.println("===Factory Department===");
		System.out.println("factory skillname : " + this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill.GetName());
		System.out.println("product wage : " + this.factoryProductManager.factoryMakerManager.wage);
	}

	public class CallForMaker {
		public ItemDef itemDef;
		public Skill skill;
		public double wage;
		public long duration;

		public CallForMaker(ItemDef itemDef, Skill skill, double wage, long duration) {
			this.itemDef = itemDef;
			this.skill = skill;
			this.wage = wage;
			this.duration = duration;
		}
	}

	// �~�����l�ރ��X�g��Ԃ��B
	public CallForMaker GetDesiredMaker() {
		double wage = this.factoryProductManager.factoryMakerManager.wage * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		CallForMaker cfw = new CallForMaker(this.factoryProductManager.factoryProductInfo.itemDef,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill, wage,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
		return cfw;
	}

	// �J�����Ԃ�Ԃ��B
	public long GetDurationForWork() {
		return this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake;
	}

	public class MakeResult {
		public double gain;
		public long duration;

		public MakeResult(double gain, long duration) {
			this.gain = gain;
			this.duration = duration;
		}
	}

	// ��Ƃ��ăA�C�e�������B
	public MakeResult Make(CallForMaker cfm, long timeNow, boolean simulation) throws HumanSimulationException {

		// ���A�C�e���̌��𒲂ׂ�B
		double numMakableMin;
		{
			numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

			// �ޗ��̍݌ɂɂ�鐧����l������B
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();

				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				int numStock = materialStockManager.GetNumStock();

				double numMakable = (numStock + materialManager.shikakarichu) / materialManager.factoryMaterialInfo.amount;

				if (numMakable < numMakableMin) {
					numMakableMin = numMakable;
				}
			}

			// ���i�̍݌ɂ̏�����l������B
			{
				int space = shopStockManager.FindStockSpace();
				double numMakable = space - factoryProductManager.shikakarichu;
				if (numMakable < numMakableMin) {
					numMakableMin = numMakable;
				}
			}

			if (numMakableMin <= 0) throw new HumanSimulationException("FactoryRoom.Make : Couldn't make the product because of lack of stock");
		}

		// �����ƘJ�����Ԃ����߂�B������B
		MakeResult result;
		{
			double gain = numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * cfm.wage;
			long duration = (long) (numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
			if (duration == 0) throw new HumanSimulationException("FactoryRoom.Make : duration = 0");
			result = new MakeResult(gain, duration);
		}

		// Worker�Ɉ��A����B
		this.Greeting(timeNow, result.duration, simulation);

		// ��ƕ����ɓ���B
		// this.EnterWorkersRoom(cfm.itemDef, timeNow, result.duration, simulation);

		// �ŋ߂̘J���҂̈ړ����ς��A�L���p�V�e�B��葽�������瓭���Ȃ��B
		double workerAverage = this.factoryProductManager.factoryMakerManager.speedAverage.GetTotal() / (timeNow - timeLastManagePrice);
		if (workerAverage > this.factoryProductManager.factoryMakerManager.factoryMakerInfo.capacityMaker) throw new HumanSimulationException(
				"FactoryRoom.EnterWorkersRoom : num of worker is full");

		// �ޗ������炷�B
		for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
			ItemDef materialItemDef = e.getKey();
			FactoryMaterialManager materialManager = e.getValue();

			StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
			int numStock = materialStockManager.GetNumStock();

			double numUse = numMakableMin * materialManager.factoryMaterialInfo.amount;

			double numRest = numStock + materialManager.shikakarichu - numUse;

			int numRestInt = (int) numRest;

			materialManager.shikakarichu = numRest - numRestInt;

			int numPick = numStock - numRestInt;

			materialStockManager.Get(timeNow, numPick, simulation);
		}

		// ���i�𑝂₷�B
		{
			double numCreate = numMakableMin + factoryProductManager.shikakarichu;
			int numCreateInt = (int) numCreate;
			factoryProductManager.shikakarichu = numCreate - numCreateInt;
			if (numCreateInt > 0) {
				Item itemProduct = new Item(cfm.itemDef, numCreateInt);
				shopStockManager.Put(timeNow, itemProduct, simulation);
			}
		}

		if (simulation == false) {
			this.AddMoney(timeNow, -result.gain);
			this.factoryProductManager.factoryMakerManager.speedAverage.Add(result.duration);
		} else {
			this.factoryProductManager.factoryMakerManager.speedAverageSimulation.Add(result.duration);
		}

		return result;
	}

	// ���i���i�ɑ΂��ăt�H�[�h�o�b�N��^����B�����炾������No1�̑I�����ɂȂ����̂��A�eHuman���t�B�[�h�o�b�N��^����B
	public void FeedbackAboutMakerPrice(CallForMaker cfm, double price, double quantity) {
		this.factoryProductManager.factoryMakerManager.Feedback(price, quantity);
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Test
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	long timeLastManagePrice = 0;

	public void ManagePriceSet(MapManager mm, HumanManager hm, long timeNow) throws Exception {
		if (timeNow - this.timeLastManagePrice < 60 * 24) return;

		int minImpression = 10;

		long duration = timeNow - this.timeLastManagePrice;
		long durationFutureTarget = 60L * 24L * 365L * 10L;

		this.timeLastManagePrice = timeNow;

		{
			double gainGlobal = -Double.MAX_VALUE;
			FeedbackLog feedbackProductGlobal = null;
			TreeMap<ItemDef, FeedbackLog> feedbackMaterialGlobal = null;
			FeedbackLog feedbackMakerGlobal = null;

			FeedbackLog[] feedbackProductList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			{
				if (feedbackProductList.length == 0 || feedbackProductList[0].impressionTotal < minImpression) {
					feedbackProductList = new FeedbackLog[1];
					feedbackProductList[0] = new FeedbackLog(this.shopStockManager.price);
				}
			}

			double productStock = this.shopStockManager.GetNumStock();
			double productCapacity = this.shopStockManager.GetCapacity();

			for (FeedbackLog feedbackProduct : feedbackProductList) {

				TreeMap<ItemDef, FeedbackLog> feedbackMaterialBest = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();

					// �̔����xnumSell�ƍł��߂����x�Ŏd����ł���A�œK�d���ꉿ�i��������B
					FeedbackLog feedbakMaterialLocalBest = null;
					{
						FactoryMaterialManager fmm = e2.getValue();

						StockManager msm = this.deliverStockManager.get(materialItemDef);
						double stockMaterial = msm.GetNumStock();
						double capacityMaterial = msm.GetCapacity();
						double numMaterialTarget = feedbackProduct.quantityTotal * fmm.factoryMaterialInfo.amount
								+ (capacityMaterial / 2 - stockMaterial) / durationFutureTarget * duration;

						FeedbackLog[] feedbackMaterialList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
						double scoreBest = Double.MAX_VALUE;
						if (feedbackMaterialList.length > 0 && feedbackMaterialList[0].impressionTotal > minImpression) {
							for (FeedbackLog feedbackMaterial : feedbackMaterialList) {
								double score = Math.abs(numMaterialTarget - feedbackMaterial.quantityTotal);
								if (score <= scoreBest) {
									scoreBest = score;
									feedbakMaterialLocalBest = feedbackMaterial;
								}
							}
						}
						if (feedbakMaterialLocalBest == null) {
							feedbakMaterialLocalBest = new FeedbackLog(msm.price);
							feedbakMaterialLocalBest.quantityTotal = numMaterialTarget;
						}
					}
					feedbackMaterialBest.put(materialItemDef, feedbakMaterialLocalBest);
				}

				FeedbackLog feedbackMakerBest = null;
				{
					double numMakerTarget = (feedbackProduct.quantityTotal + (productCapacity / 2 - productStock) / durationFutureTarget * duration)
							/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

					FeedbackLog[] feedbackMakerList = this.factoryProductManager.factoryMakerManager.feedbackManager
							.CollectResultWithEqualImpressionAdjust();
					if (feedbackMakerList.length > 0 && feedbackMakerList[0].impressionTotal > minImpression) {
						double scoreBest = Double.MAX_VALUE;
						for (FeedbackLog feedbackMaker : feedbackMakerList) {
							double score = Math.abs(numMakerTarget - feedbackMaker.quantityTotal);
							if (score < scoreBest) {
								scoreBest = score;
								feedbackMakerBest = feedbackMaker;
							}
						}
					}
					if (feedbackMakerBest == null) {
						feedbackMakerBest = new FeedbackLog(this.factoryProductManager.factoryMakerManager.wage);
						feedbackMakerBest.quantityTotal = numMakerTarget;
					}
				}

				// ���Y���x�����߂�B
				double numProducableMin = feedbackProduct.quantityTotal;
				{
					for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
						ItemDef materialItemDef = e2.getKey();
						FactoryMaterialManager fmm = e2.getValue();
						FeedbackLog feedbackMaterial = feedbackMaterialBest.get(materialItemDef);
						double numProducable = feedbackMaterial.quantityTotal / fmm.factoryMaterialInfo.amount;
						if (numProducable < numProducableMin) {
							numProducableMin = numProducable;
						}
					}

					{
						double numProducable = feedbackMakerBest.quantityTotal
								* this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
						if (numProducable < numProducableMin) {
							numProducableMin = numProducable;
						}
					}
				}

				// ���������߂�B
				double gain;
				{
					{
						gain = numProducableMin * feedbackProduct.price;
					}

					for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
						ItemDef materialItemDef = e2.getKey();
						FactoryMaterialManager fmm = e2.getValue();
						FeedbackLog feedbackMaterial = feedbackMaterialBest.get(materialItemDef);
						double cost = numProducableMin * fmm.factoryMaterialInfo.amount * feedbackMaterial.price;
						gain -= cost;
					}

					{
						double cost = numProducableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
								* feedbackMakerBest.price;
						gain -= cost;
					}
				}

				if (gain > gainGlobal) {
					gainGlobal = gain;
					feedbackProductGlobal = feedbackProduct;
					feedbackMaterialGlobal = feedbackMaterialBest;
					feedbackMakerGlobal = feedbackMakerBest;
				}
			}

			// �œK�ȉ��i�ɐݒ肷��B
			{
				{
					this.shopStockManager.price = feedbackProductGlobal.price;
				}

				for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
					ItemDef itemDef = e.getKey();
					StockManager msm = e.getValue();
					FeedbackLog feedbackMaterial = feedbackMaterialGlobal.get(itemDef);
					msm.price = feedbackMaterial.price;
				}

				{
					this.factoryProductManager.factoryMakerManager.wage = feedbackMakerGlobal.price;
				}
			}
			System.out.println(gainGlobal);
		}

		if (true) {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                     ManageProductPricie                     $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			{
				FeedbackLog[] feedbackList = shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == this.shopStockManager.price) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal + "��" + flag);
				}
				shopStockManager.ResetStatisticalParameters();
			}

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                    ManageMaterialPricie                     $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
				StockManager msm = e.getValue();
				System.out.println(msm.stockManagerInfo.itemDef.GetName());
				FeedbackLog[] feedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == msm.price) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal + "��" + flag);
				}
				msm.ResetStatisticalParameters();
			}

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                      ManageMakerWage                        $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			{
				FeedbackLog[] feedbackList = this.factoryProductManager.factoryMakerManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == this.factoryProductManager.factoryMakerManager.wage) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal + "��" + flag);
				}
				this.factoryProductManager.factoryMakerManager.ResetStatisticalParameters();
			}
		}
	}

	public void ManagePriceSet2(MapManager mm, HumanManager hm, long timeNow) throws Exception {
		// if (timeNow - this.timeLastManagePrice < 60 * 12) return;
		//
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		// System.out.println("$                    ManageMaterialPricie                     $");
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		//
		// long duration = timeNow - this.timeLastManagePrice;
		//
		// for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
		// ItemDef materialItemDef = e.getKey();
		// StockManager msm = e.getValue();
		//
		// int capacity = msm.GetCapacity();
		// int numStock = msm.GetNumStock();
		//
		// // �݌ɂ̔����Ԃ𒲂ׂ�B
		// double in = msm.GetInputTotal();
		// double ins = msm.GetInputTotalSimulation();
		// double out = msm.GetOutputTotal();
		//
		// double numStockTarget = capacity * 0.8;
		// long durationTraget = 60 * 24 * 3;
		// double maxHitRate = 0.5;
		// double inNeed = (numStockTarget - numStock) * duration / durationTraget + out;
		//
		// if (in < inNeed && in < ins * maxHitRate) {
		// double wageOld = msm.price;
		// msm.price *= (1.05 + OtherUtility.rand.nextDouble() * 0.05);
		// System.out.println("�ޗ��� : " + materialItemDef.GetName());
		// System.out.println("�w�������� : " + in / ins);
		// System.out.println("�d�����UP : " + wageOld + "��" + msm.price);
		// } else {
		// double wageOld = msm.price;
		// msm.price /= (1.05 + OtherUtility.rand.nextDouble() * 0.05);
		// System.out.println("�ޗ��� : " + materialItemDef.GetName());
		// System.out.println("�w�������� : " + in / ins);
		// System.out.println("�d�����DOWN : " + wageOld + "��" + msm.price);
		// }
		// }
		//
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		// System.out.println("$                      ManageMakerWage                        $");
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		//
		// for (Entry<ItemDef, FactoryProductManager> e : factoryProductManager.entrySet()) {
		// ItemDef productItemDef = e.getKey();
		// FactoryProductManager fp = e.getValue();
		//
		// StockManager ssm = shopStockManager.get(productItemDef);
		//
		// double out = ssm.GetOutputTotal();
		//
		// int capacity = ssm.GetCapacity();
		// int numStock = ssm.GetNumStock();
		//
		// for (Entry<Skill, FactoryMakerManager> e2 : fp.factoryMakerManager.entrySet()) {
		// FactoryMakerManager factoryMakerManager = e2.getValue();
		//
		// // Maker�̌ٗp��Ԃ𒲂ׂ�B
		// double workerTotalMinutes = factoryMakerManager.speedAverage.GetTotal();
		// double workerTotalMinutesSimulation = factoryMakerManager.speedAverageSimulation.GetTotal();
		//
		// double numStockTarget = capacity * 0.8;
		// long durationTraget = 60 * 24 * 3;
		// double maxHitRate = 0.5;
		// double inNeed = (numStockTarget - numStock) * duration / durationTraget + out;
		//
		// double in = ssm.GetInputTotal();
		// double ins = ssm.GetInputTotalSimulation();
		//
		// if (in < inNeed && in < ins * maxHitRate) {
		// double wageOld = factoryMakerManager.wage;
		// factoryMakerManager.wage *= (1.05 + OtherUtility.rand.nextDouble() * 0.05);
		// System.out.println("HitRate : " + workerTotalMinutes / workerTotalMinutesSimulation);
		// System.out.println("����UP : " + wageOld + "��" + factoryMakerManager.wage);
		// } else {
		// double wageOld = factoryMakerManager.wage;
		// factoryMakerManager.wage /= (1.05 + OtherUtility.rand.nextDouble() * 0.05);
		// System.out.println("HitRate : " + workerTotalMinutes / workerTotalMinutesSimulation);
		// System.out.println("����DOWN : " + wageOld + "��" + factoryMakerManager.wage);
		// }
		// factoryMakerManager.speedAverage.Clear();
		// factoryMakerManager.speedAverageSimulation.Clear();
		// }
		// }
		//
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		// System.out.println("$                     ManageProductPricie                     $");
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		//
		// // ���i���i�����肷��B
		// for (Entry<ItemDef, FactoryProductManager> e : factoryProductManager.entrySet()) {
		// ItemDef productItemDef = e.getKey();
		// // FactoryProductManager fp = e.getValue();
		//
		// StockManager ssm = shopStockManager.get(productItemDef);
		//
		// // double in = ssm.GetInputTotal();
		// // double ins = ssm.GetInputTotalSimulation();
		//
		// double out = ssm.GetOutputTotal();
		// double outs = ssm.GetOutputTotalSimulation();
		//
		// if (outs > 0) {
		// if (out == 0) {
		// ssm.price /= 1.01;
		// gainsIndex--;
		// if (gainsIndex < 0) gainsIndex = 0;
		// } else {
		// double gain = this.GetMoney() - moneyOld;
		// moneyOld = this.GetMoney();
		//
		// double gainSpeed = gain / duration;
		//
		// gains[gainsIndex] = gains[gainsIndex] * 0.9 + 0.1 * gainSpeed;
		//
		// gainsIndex += OtherUtility.rand.nextInt(3) - 1;
		// if (gainsIndex < 0) gainsIndex = 0;
		// if (gainsIndex >= 1000) gainsIndex = 1000;
		// ssm.price = Math.pow(1.01, gainsIndex - 500);
		//
		// for (int i = gainsIndex - 10; i <= gainsIndex + 10; i++) {
		// if (i < 0) continue;
		// if (i >= 1000) continue;
		// if (i == gainsIndex) {
		// System.out.print(", �y" + gains[i] + "�z");
		// } else {
		// System.out.print(", " + gains[i]);
		// }
		// }
		// System.out.println();
		// System.out.println();
		// }
		// }
		//
		// }
		//
		// for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
		// StockManager sm = e.getValue();
		// sm.ResetStatisticalParameters();
		// }
		//
		// for (Entry<ItemDef, StockManager> e : shopStockManager.entrySet()) {
		// StockManager sm = e.getValue();
		// sm.ResetStatisticalParameters();
		// }
		//
		// this.timeLastManagePrice = timeNow;
	}

	double[] gains = new double[1000];
	int gainsIndex = 500;

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Obsolete
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

}
