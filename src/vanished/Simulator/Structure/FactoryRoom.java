package vanished.Simulator.Structure;

import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMakerInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMaterialInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryProductInfo;

public class FactoryRoom extends ShopRoom {

	private boolean forBuilding = false;

	private FactoryProductManager factoryProductManager;

	public class FactoryMaterialManager {

		FactoryMaterialInfo factoryMaterialInfo;

		public FactoryMaterialManager(FactoryMaterialInfo materialInfo) {
			this.factoryMaterialInfo = materialInfo;
		}
	}

	public class FactoryMakerManager {

		FactoryMakerInfo factoryMakerInfo;

		// 労働者の賃金
		double wage = 1 + OtherUtility.RandGaussian() * 0.1;

		public FactoryMakerManager(FactoryMakerInfo factoryMakerInfo) {
			this.factoryMakerInfo = factoryMakerInfo;
		}

		// ////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////
		// 統計用
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

		// 製品をnumProductPerWork個作るのに必要な材料
		TreeMap<ItemDef, FactoryMaterialManager> factoryMaterialManager = new TreeMap<ItemDef, FactoryMaterialManager>(new ItemDefComparator());

		// 製造できる人材のリスト
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

		// 商品価格を決定する。
		{
			double costMaterialTotal = 0;
			for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e2.getKey();
				FactoryMaterialManager fmm = e2.getValue();
				double cost = this.GetDesiredItemWithNewPrice(materialItemDef, Double.MAX_VALUE, Double.MAX_VALUE).price
						* fmm.factoryMaterialInfo.amount;
				costMaterialTotal += cost;
			}

			double costMakerMax = this.factoryProductManager.factoryMakerManager.wage
					/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

			double costTotal = costMaterialTotal + costMakerMax;

			this.shopStockManager.price = costTotal + 1 + OtherUtility.rand.nextDouble() * 0.5;
		}

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

	// 欲しい人材リストを返す。
	public CallForMaker GetDesiredMaker(double numMakeMax) {

		// 作るアイテムの個数を調べる。
		double numMakableMin = Double.MAX_VALUE;
		{
			if (numMakeMax < numMakableMin) numMakableMin = numMakeMax;

			if (this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake < numMakableMin) {
				numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
			}

			if (numMakableMin == 0) return null;
		}

		double wageForFullWork = this.factoryProductManager.factoryMakerManager.wage * Math.pow(1.005, OtherUtility.rand.nextInt(41) - 41 / 2);

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

		// 材料を必要とする場所は、InKindできない。
		if (this.factoryProductManager.factoryMaterialManager.size() > 0) return null;

		// 作るアイテムの個数を調べる。
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

	public class MakeResult {
		public double gain;
		public long duration;

		public MakeResult(double gain, long duration) {
			this.gain = gain;
			this.duration = duration;
		}
	}

	// 作業してアイテムを作る。
	public void Make(CallForMaker cfm, long timeNow, boolean simulation) throws Exception {
		this.Enter(timeNow, cfm.duration, simulation);

		// 製品を増やす。
		{
			shopStockManager.Put(timeNow, cfm.numMake, simulation);
		}

		if (simulation == false) {
			// 材料を減らす。
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();
				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numUse = cfm.numMake * materialManager.factoryMaterialInfo.amount;
				materialStockManager.Get(timeNow, numUse, simulation);
			}

			// 賃金を払う。
			this.AddMoney(timeNow, -cfm.gain);
		}
	}

	public Item MakeInKind(CallForMakerInKind cfm, long timeNow, boolean simulation) throws Exception {
		this.Enter(timeNow, cfm.duration, simulation);

		if (simulation == false) {
			// 材料を減らす。
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();
				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numUse = cfm.numMake * materialManager.factoryMaterialInfo.amount;
				materialStockManager.Get(timeNow, numUse, simulation);
			}
		}

		// 現物を返す。
		Item itemProduct = new Item(cfm.itemDef, cfm.numMake);
		return itemProduct;
	}

	// 商品価格に対してフォードバックを与える。どのくらいの確率で選択されたか、各Humanがフィードバックを与える。
	public void FeedbackAboutMakerPrice(CallForMaker cfm, double price, double quantity) {
		this.factoryProductManager.factoryMakerManager.Feedback(price, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
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

		// 露出が少ないものがある場合は、価格設定はやめる。
		int minImpression = 10;
		{
			boolean skipFlag = false;
			FeedbackLog[] feedbackProductList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			if (feedbackProductList.length < 5 || feedbackProductList[0].impressionTotal < minImpression) {
				skipFlag = true;
				this.shopStockManager.price /= 1.05;
			}

			for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e2.getKey();
				StockManager msm = this.deliverStockManager.get(materialItemDef);

				FeedbackLog[] materialFeedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				if (materialFeedbackList.length < 5 || materialFeedbackList[0].impressionTotal < minImpression) {
					skipFlag = true;
					msm.price *= 1.05;
				}
			}

			FeedbackLog[] makerFeedbackList = this.factoryProductManager.factoryMakerManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			if (makerFeedbackList.length < 5 || makerFeedbackList[0].impressionTotal < minImpression) {
				skipFlag = true;
				this.factoryProductManager.factoryMakerManager.wage *= 1.05;
			}

			if (skipFlag == true) {
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
							System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
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
							System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
						}
						msm.ResetStatisticalParameters();
					}

					System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
					System.out.println("$                      ManageMakerWage                        $");
					System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
					{
						FeedbackLog[] feedbackList = this.factoryProductManager.factoryMakerManager.feedbackManager
								.CollectResultWithEqualImpressionAdjust();
						// TODO
						for (FeedbackLog feedback : feedbackList) {
							String flag = "";
							if (feedback.price == this.factoryProductManager.factoryMakerManager.wage) {
								flag = " <-BEST";
							}
							System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
						}
						this.factoryProductManager.factoryMakerManager.ResetStatisticalParameters();
					}
				}
				return;
			}
		}

		long duration = timeNow - this.timeLastManagePrice;
		this.timeLastManagePrice = timeNow;

		// TODO:何日後に完成を目指すかは、どこかで設定できるようにすべき。
		long durationFutureTarget;
		if (this.forBuilding == true) {
			durationFutureTarget = 60L * 24L * 30L;
		} else {
			durationFutureTarget = 60L * 24L * 30L;
		}

		double productStock = this.shopStockManager.GetNumStock();
		{
			double gainGlobal = -Double.MAX_VALUE;
			FeedbackLog feedbackProductGlobal = null;
			TreeMap<ItemDef, FeedbackLog> feedbackMaterialGlobal = null;
			FeedbackLog feedbackMakerGlobal = null;

			FeedbackLog[] feedbackProductList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();

			// 利益が最も大きくなる設定を探す。
			// 製品価格に応じて、売れる速度が違う。
			// 売れる速度に対して、適切な材料価格と労働者賃金を設定したときの、利益を計算する。
			for (FeedbackLog productFeedback : feedbackProductList) {

				// 販売量に見合うだけの材料を仕入れるための価格設定を探す。
				TreeMap<ItemDef, FeedbackLog> materialFeedbackBest = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();
					FactoryMaterialManager fmm = e2.getValue();
					StockManager msm = this.deliverStockManager.get(materialItemDef);

					// 販売速度numSellと最も近い速度で仕入れできる、最適仕入れ価格を見つける。
					FeedbackLog materialFeedbakLocalBest = null;
					{
						double materialStock = msm.GetNumStock();
						double targetSpeedMaterial;
						if (this.forBuilding == false) {
							targetSpeedMaterial = (productFeedback.quantityTotal / duration - productStock / durationFutureTarget)
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
								materialFeedbakLocalBest = materialFeedback;
							}
						}
					}
					materialFeedbackBest.put(materialItemDef, materialFeedbakLocalBest);
				}

				// 販売量に見合うだけの労働者を確保するための価格設定を探す。
				FeedbackLog makerFeedbackBest = null;
				{
					double targetSpeedMake;
					if (this.forBuilding == false) {
						targetSpeedMake = productFeedback.quantityTotal / duration - productStock / durationFutureTarget;
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

				// 生産速度を求める。
				double numProducableMin = productFeedback.quantityTotal;
				{
					for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
						ItemDef materialItemDef = e2.getKey();
						FactoryMaterialManager fmm = e2.getValue();
						FeedbackLog feedbackMaterial = materialFeedbackBest.get(materialItemDef);
						double numProducable = feedbackMaterial.quantityTotal / fmm.factoryMaterialInfo.amount;
						if (numProducable < numProducableMin) {
							numProducableMin = numProducable;
						}
					}

					{
						double numProducable = makerFeedbackBest.quantityTotal;
						if (numProducable < numProducableMin) {
							numProducableMin = numProducable;
						}
					}
				}

				// 利潤を求める。
				double gain;
				{
					{
						gain = numProducableMin * productFeedback.price;
					}

					for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
						ItemDef materialItemDef = e2.getKey();
						FactoryMaterialManager fmm = e2.getValue();
						FeedbackLog feedbackMaterial = materialFeedbackBest.get(materialItemDef);
						double cost = numProducableMin * fmm.factoryMaterialInfo.amount * feedbackMaterial.price;
						gain -= cost;
					}

					{
						double cost = numProducableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
								* makerFeedbackBest.price;
						gain -= cost;
					}
				}

				if (gain > gainGlobal) {
					gainGlobal = gain;
					feedbackProductGlobal = productFeedback;
					feedbackMaterialGlobal = materialFeedbackBest;
					feedbackMakerGlobal = makerFeedbackBest;
				}
			}

			// 最適な価格に設定する。
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal + "個" + flag);
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
