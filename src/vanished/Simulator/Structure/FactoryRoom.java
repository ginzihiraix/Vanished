package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMakerInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMaterialInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryProductInfo;

public class FactoryRoom extends ShopRoom {

	private boolean forBuilding = false;

	private boolean openFactory = true;

	private FactoryProductManager factoryProductManager;

	public class FactoryMaterialManager {

		FactoryMaterialInfo factoryMaterialInfo;

		public FactoryMaterialManager(FactoryMaterialInfo materialInfo) {
			this.factoryMaterialInfo = materialInfo;
		}
	}

	public class FactoryMakerManager {

		FactoryMakerInfo factoryMakerInfo;

		// �J���҂̒���
		// double wage = 1 + OtherUtility.RandGaussian() * 0.1;
		double wage = 1;
		double wageRate = 1;

		public FactoryMakerManager(FactoryMakerInfo factoryMakerInfo) {
			this.factoryMakerInfo = factoryMakerInfo;
		}

		// ////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////
		// ���v�p
		// ////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////
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

	public FactoryRoom(Building building, FactoryRoomDef roomDef, boolean forBuilding) {
		super(building, roomDef);
		factoryProductManager = new FactoryProductManager(roomDef.factoryProductInfo);
		this.forBuilding = forBuilding;
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
		public double wageForFullWork;
		public double numMake;
		public long duration;
		public double gain;

		public CallForMaker(ItemDef itemDef, Skill skill, double wageForFullWork, double numMake, long duration, double gain) {
			this.itemDef = itemDef;
			this.skill = skill;
			this.wageForFullWork = wageForFullWork;
			this.numMake = numMake;
			this.duration = duration;
			this.gain = gain;
		}
	}

	// �~�����l�ރ��X�g��Ԃ��B
	public CallForMaker GetDesiredMaker(double numMakeMax) {

		if (this.openFactory == false) return null;

		// ���A�C�e���̌��𒲂ׂ�B
		double numMakableMin = Double.MAX_VALUE;
		{
			if (numMakeMax < numMakableMin) numMakableMin = numMakeMax;

			if (this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake < numMakableMin) {
				numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
			}

			if (numMakableMin == 0) return null;
		}

		double wageForFullWork = this.factoryProductManager.factoryMakerManager.wage * this.factoryProductManager.factoryMakerManager.wageRate;

		long duration = (long) (numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
		if (duration == 0) duration = 1L;

		double gain = wageForFullWork * numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

		CallForMaker cfw = new CallForMaker(this.factoryProductManager.factoryProductInfo.itemDef,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill, wageForFullWork, numMakableMin, duration, gain);
		return cfw;
	}

	public class CallForMakerInKind {
		public ItemDef itemDef;
		public Skill skill;
		public double numMake;
		public long duration;

		public CallForMakerInKind(ItemDef itemDef, Skill skill, double numMake, long duration) {
			this.itemDef = itemDef;
			this.skill = skill;
			this.numMake = numMake;
			this.duration = duration;
		}
	}

	public CallForMakerInKind GetDesiredMakerInKind(double numMakeMax) {

		// �ޗ���K�v�Ƃ���ꏊ�́AInKind�ł��Ȃ��B
		if (this.factoryProductManager.factoryMaterialManager.size() > 0) return null;

		// ���A�C�e���̌��𒲂ׂ�B
		double numMakableMin = Double.MAX_VALUE;
		{
			if (numMakeMax < numMakableMin) numMakableMin = numMakeMax;

			if (this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake < numMakableMin) {
				numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
			}

			if (numMakableMin == 0) return null;
		}

		long duration = (long) (numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
		if (duration == 0) duration = 1L;

		CallForMakerInKind cfw = new CallForMakerInKind(this.factoryProductManager.factoryProductInfo.itemDef,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill, numMakableMin, duration);
		return cfw;
	}

	public void SetMakerPriceRate(double rate) {
		this.factoryProductManager.factoryMakerManager.wageRate = rate;
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
	public void Make(CallForMaker cfm, long timeNow, boolean simulation) throws Exception {

		if (this.openFactory == false) throw new Exception("fatal error");

		this.Enter(timeNow, cfm.duration, simulation);

		// ���i�𑝂₷�B
		{
			shopStockManager.Put(timeNow, cfm.numMake, simulation);
		}

		if (simulation == false) {
			// �ޗ������炷�B
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();
				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numUse = cfm.numMake * materialManager.factoryMaterialInfo.amount;
				materialStockManager.Get(timeNow, numUse, simulation);
			}

			// �����𕥂��B
			this.AddMoney(timeNow, -cfm.gain);
		}
	}

	public Item MakeInKind(CallForMakerInKind cfm, long timeNow, boolean simulation) throws Exception {

		this.Enter(timeNow, cfm.duration, simulation);

		if (simulation == false) {
			// �ޗ������炷�B
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();
				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numUse = cfm.numMake * materialManager.factoryMaterialInfo.amount;
				materialStockManager.Get(timeNow, numUse, simulation);
			}
		}

		// ������Ԃ��B
		Item itemProduct = new Item(cfm.itemDef, cfm.numMake);
		return itemProduct;
	}

	// ���i���i�ɑ΂��ăt�H�[�h�o�b�N��^����B�ǂ̂��炢�̊m���őI�����ꂽ���A�eHuman���t�B�[�h�o�b�N��^����B
	public void FeedbackAboutMakerPrice(CallForMaker cfm, double price, double quantity) {
		// System.out.println("FeedbackAboutMakerPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
		this.factoryProductManager.factoryMakerManager.Feedback(price, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// �C�x���g�L�^�p
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	public void DiscardOldLog(long timeNow) throws Exception {
		super.DiscardOldLog(timeNow);
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Test
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	long timeLastManagePrice = 0;

	public void ManagePriceSet(MapManager mm, HumanManager hm, long timeNow) throws Exception {
		if (timeNow - this.timeLastManagePrice < 60 * 24) return;

		// ���ʂ�0�ɂȂ�悤�ȗ�����ǉ����Ă����B
		if (true) {
			this.FeedbackAboutProductPrice(0, 0);

			CallForMaker cfm = this.GetDesiredMaker(Double.MAX_VALUE);
			this.FeedbackAboutMakerPrice(cfm, 0, 0);

			for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e2.getKey();
				this.FeedbackAboutDeliverPrice(materialItemDef, 0, 0);
			}
		}

		long duration = timeNow - this.timeLastManagePrice;
		this.timeLastManagePrice = timeNow;

		// TODO:������Ɋ�����ڎw�����́A�ǂ����Őݒ�ł���悤�ɂ��ׂ��B
		long durationFutureTarget;
		if (this.forBuilding == true) {
			durationFutureTarget = 60L * 24L * 30L;
		} else {
			durationFutureTarget = 60L * 24L * 10L;
		}

		if (false) {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                     ManageProductPricie                     $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			{
				FeedbackLog[] feedbackList = shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == this.shopStockManager.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal / duration
							* durationFutureTarget + "��" + flag);
				}
			}

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                    ManageMaterialPricie                     $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
				StockManager msm = e.getValue();
				ItemDef itemDef = e.getKey();
				FactoryMaterialManager fmm = this.factoryProductManager.factoryMaterialManager.get(itemDef);
				System.out.println(msm.stockManagerInfo.itemDef.GetName());
				FeedbackLog[] feedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == msm.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal
							/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget + "��" + flag);
				}
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
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal / duration
							* durationFutureTarget + "��" + flag);
				}
			}
		}

		double productNumStock = this.shopStockManager.GetNumStock();
		{
			class Pack {
				FeedbackLog productFeedback = null;
				FeedbackLog makerFeedback = null;
				TreeMap<ItemDef, FeedbackLog> materialFeedback = null;
				double speedDef = 0;
				double stockDef = 0;
				double gain = 0;

				public Pack(FeedbackLog productFeedback, FeedbackLog makerFeedback, TreeMap<ItemDef, FeedbackLog> materialFeedback) {
					this.productFeedback = productFeedback;
					this.makerFeedback = makerFeedback;
					this.materialFeedback = materialFeedback;
				}
			}

			ArrayList<Pack> packs = new ArrayList<Pack>();

			// ���v���ł��傫���Ȃ�ݒ��T���B
			// ���i���i�ɉ����āA����鑬�x���Ⴄ�B
			// ����鑬�x�ɑ΂��āA�K�؂ȍޗ����i�ƘJ���Ғ�����ݒ肵���Ƃ��́A���v���v�Z����B
			FeedbackLog[] productFeedbackList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			for (FeedbackLog productFeedback : productFeedbackList) {

				// �̔��ʂɌ����������̘J���҂��m�ۂ��邽�߂̉��i�ݒ��T���B
				FeedbackLog makerFeedbackBest = null;
				{
					double targetSpeedMake;
					if (this.forBuilding == false) {
						targetSpeedMake = productFeedback.quantityTotal / duration - productNumStock / durationFutureTarget;
					} else {
						targetSpeedMake = 1.0 / durationFutureTarget;
					}

					FeedbackLog[] makerFeedbackList = this.factoryProductManager.factoryMakerManager.feedbackManager
							.CollectResultWithEqualImpressionAdjust();
					double scoreBest = Double.MAX_VALUE;
					for (FeedbackLog makerFeedback : makerFeedbackList) {
						double score = Math.abs(targetSpeedMake - makerFeedback.quantityTotal / duration);
						if (score < scoreBest) {
							scoreBest = score;
							makerFeedbackBest = makerFeedback;
						}
					}
				}

				// �̔��ʂɌ����������̍ޗ����d����邽�߂̉��i�ݒ��T���B
				TreeMap<ItemDef, FeedbackLog> materialFeedbackBest = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();
					FactoryMaterialManager fmm = e2.getValue();
					StockManager msm = this.deliverStockManager.get(materialItemDef);

					// �̔����xnumSell�ƍł��߂����x�Ŏd����ł���A�œK�d���ꉿ�i��������B
					FeedbackLog materialFeedbackLocalBest = null;
					{
						double materialStock = msm.GetNumStock();
						double targetSpeedMaterial;
						if (this.forBuilding == false) {
							targetSpeedMaterial = (productFeedback.quantityTotal / duration - productNumStock / durationFutureTarget)
									* fmm.factoryMaterialInfo.amount - materialStock / durationFutureTarget;
						} else {
							targetSpeedMaterial = 1.0 * fmm.factoryMaterialInfo.amount / durationFutureTarget;
						}

						FeedbackLog[] materialFeedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
						double scoreBest = Double.MAX_VALUE;
						for (FeedbackLog materialFeedback : materialFeedbackList) {
							double score = Math.abs(targetSpeedMaterial - materialFeedback.quantityTotal / duration);
							if (score <= scoreBest) {
								scoreBest = score;
								materialFeedbackLocalBest = materialFeedback;
							}
						}
					}
					materialFeedbackBest.put(materialItemDef, materialFeedbackLocalBest);
				}

				Pack pack = new Pack(productFeedback, makerFeedbackBest, materialFeedbackBest);
				packs.add(pack);
			}

			if (true) {
				// �����̍ݍZ�������ʂ̐��x���v�Z����B
				if (false) {
					for (Pack pack : packs) {
						double speedSell = pack.productFeedback.quantityTotal / duration;

						double speedMakeProduct = pack.makerFeedback.quantityTotal / duration + productNumStock / durationFutureTarget;

						double speedDefTotal = Math.abs(speedMakeProduct - speedSell);
						double stockDefTotal = Math.abs(productNumStock + (pack.makerFeedback.quantityTotal - pack.productFeedback.quantityTotal)
								/ duration * durationFutureTarget)
								/ productNumStock;
						int count = 1;

						for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
							ItemDef materialItemDef = e2.getKey();
							FactoryMaterialManager fmm = e2.getValue();
							StockManager msm = this.deliverStockManager.get(materialItemDef);
							FeedbackLog materialFeedback = pack.materialFeedback.get(materialItemDef);

							double materialNumStock = msm.GetNumStock();

							double speedProvideMaterial = materialFeedback.quantityTotal / fmm.factoryMaterialInfo.amount / duration
									+ materialNumStock / fmm.factoryMaterialInfo.amount / durationFutureTarget;
							double speedDef = Math.abs(speedProvideMaterial - speedSell);

							double stockDef = Math.abs(materialNumStock + (pack.makerFeedback.quantityTotal - materialFeedback.quantityTotal)
									/ duration * durationFutureTarget)
									/ materialNumStock;
							speedDefTotal += speedDef;
							stockDefTotal += stockDef;
							count++;
						}

						pack.speedDef = speedDefTotal / count;
						pack.stockDef = stockDefTotal / count;
					}
				}

				// ����Ȃ��ޗ��́A�����̃��X�N���l������10�{�Ŏd�����Ɖ��肵�āA�R�X�g���v�Z�B
				for (Pack pack : packs) {
					double gain = pack.productFeedback.quantityTotal / duration * durationFutureTarget * pack.productFeedback.price;

					{
						double cost = pack.makerFeedback.quantityTotal
								/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * pack.makerFeedback.price;
						gain -= cost;

						double res = productNumStock + pack.makerFeedback.quantityTotal / duration * durationFutureTarget
								- pack.productFeedback.quantityTotal / duration * durationFutureTarget;
						if (res < 0) {
							double costExt = -res / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
									* pack.makerFeedback.price * 10;
							gain -= costExt;
						}
					}

					for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
						ItemDef materialItemDef = e2.getKey();
						FactoryMaterialManager fmm = e2.getValue();
						FeedbackLog materialFeedback = pack.materialFeedback.get(materialItemDef);
						StockManager msm = this.deliverStockManager.get(materialItemDef);
						double materialNumStock = msm.GetNumStock();

						double cost = materialFeedback.quantityTotal * materialFeedback.price;
						gain -= cost;

						double res = materialNumStock + materialFeedback.quantityTotal / duration * durationFutureTarget
								- pack.productFeedback.quantityTotal * fmm.factoryMaterialInfo.amount / duration * durationFutureTarget;
						if (res < 0) {
							double costExt = -res * materialFeedback.price * 10;
							gain -= costExt;
						}
					}

					pack.gain = gain;
				}

				double gainMax = -Double.MAX_VALUE;
				Pack packMax = null;

				if (false) {
					for (Pack pack : packs) {
						double s = Math.abs(pack.speedDef * durationFutureTarget / productNumStock);
						if (s < 0.05) {
							if (pack.gain > gainMax) {
								packMax = pack;
							}
						}
					}
				}

				if (true) {
					for (Pack pack : packs) {
						if (pack.gain > gainMax) {
							packMax = pack;
						}
					}
				}

				if (false) {
					for (Pack pack : packs) {
						double s = Math.abs(pack.speedDef * durationFutureTarget / productNumStock);
						if (s < 0.05) {
							if (pack.gain > gainMax) {
								packMax = pack;
							}
						}
					}
				}

				if (false) {
					if (packMax == null) {
						double min = Double.MAX_VALUE;
						for (Pack pack : packs) {
							if (pack.speedDef < min) {
								min = pack.speedDef;
								packMax = pack;
							}
						}
					}
				}

				// �œK�ȉ��i�ɐݒ肷��B
				{
					{
						if (packMax.productFeedback.price == 0) {
							this.shopStockManager.Close();
						} else {
							this.shopStockManager.Open();
							this.shopStockManager.SetPrice(packMax.productFeedback.price);
						}
					}

					{
						if (packMax.makerFeedback.price == 0) {
							this.openFactory = false;
						} else {
							this.openFactory = true;
							this.factoryProductManager.factoryMakerManager.wage = packMax.makerFeedback.price;
						}
					}

					for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
						ItemDef itemDef = e.getKey();
						StockManager msm = e.getValue();
						FeedbackLog feedbackMaterial = packMax.materialFeedback.get(itemDef);

						if (feedbackMaterial.price == 0) {
							msm.Close();
						} else {
							msm.Open();
							msm.SetPrice(feedbackMaterial.price);
						}
					}
				}
			}
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
					if (feedback.price == this.shopStockManager.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal / duration
							* durationFutureTarget + "��" + flag);
				}
				shopStockManager.ResetStatisticalParameters();
			}

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                    ManageMaterialPricie                     $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
				StockManager msm = e.getValue();
				ItemDef itemDef = e.getKey();
				FactoryMaterialManager fmm = this.factoryProductManager.factoryMaterialManager.get(itemDef);
				System.out.println(msm.stockManagerInfo.itemDef.GetName());
				FeedbackLog[] feedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == msm.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal
							/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget + "��" + flag);
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
					System.out.println(feedback.price + "�~, " + feedback.impressionTotal + "��, " + feedback.quantityTotal / duration
							* durationFutureTarget + "��" + flag);
				}
				this.factoryProductManager.factoryMakerManager.ResetStatisticalParameters();
			}
		}
	}
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Obsolete
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

}
