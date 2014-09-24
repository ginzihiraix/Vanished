package vanished.Simulator.Structure;

import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanManager;
import vanished.Simulator.HumanSimulationException;
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

	boolean forBuilding = false;

	FactoryProductManager factoryProductManager;

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

		// しかかり中の製品
		// double shikakarichu = 0;

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
				double cost = this.GetDesiredItem(materialItemDef).price * fmm.factoryMaterialInfo.amount;
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
		public double wage;
		public long duration;

		public CallForMaker(ItemDef itemDef, Skill skill, double wage, long duration) {
			this.itemDef = itemDef;
			this.skill = skill;
			this.wage = wage;
			this.duration = duration;
		}
	}

	// 欲しい人材リストを返す。
	public CallForMaker GetDesiredMaker() {
		double wage = this.factoryProductManager.factoryMakerManager.wage * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		CallForMaker cfw = new CallForMaker(this.factoryProductManager.factoryProductInfo.itemDef,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill, wage,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
		return cfw;
	}

	// 労働時間を返す。
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

	// 作業してアイテムを作る。
	public MakeResult Make(CallForMaker cfm, long timeNow, boolean simulation) throws HumanSimulationException {

		// 作るアイテムの個数を調べる。
		double numMakableMin;
		{
			numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

			// 材料の在庫による制約を考慮する。
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();

				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numStock = materialStockManager.GetNumStock();

				double numMakable = numStock / materialManager.factoryMaterialInfo.amount;

				if (numMakable < numMakableMin) {
					numMakableMin = numMakable;
				}
			}

			// 製品の在庫の上限を考慮する。
			{
				double numMakable = shopStockManager.FindStockSpace();
				if (numMakable < numMakableMin) {
					numMakableMin = numMakable;
				}
			}

			if (numMakableMin <= 0) throw new HumanSimulationException("FactoryRoom.Make : Couldn't make the product because of lack of stock");
		}

		// 賃金と労働時間を求める。個数割り。
		MakeResult result;
		{
			double gain = numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * cfm.wage;
			long duration = (long) (numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
			if (duration == 0) throw new HumanSimulationException("FactoryRoom.Make : duration = 0");
			result = new MakeResult(gain, duration);
		}

		// Workerに挨拶する。
		this.Greeting(timeNow, result.duration, simulation);

		if (simulation == false) {
			// 材料を減らす。
			for (Entry<ItemDef, FactoryMaterialManager> e : factoryProductManager.factoryMaterialManager.entrySet()) {
				ItemDef materialItemDef = e.getKey();
				FactoryMaterialManager materialManager = e.getValue();
				StockManager materialStockManager = this.deliverStockManager.get(materialItemDef);
				double numUse = numMakableMin * materialManager.factoryMaterialInfo.amount;
				materialStockManager.Get(timeNow, numUse, simulation);
			}

			// 製品を増やす。
			{
				double numCreate = numMakableMin;
				if (numCreate > 0) {
					Item itemProduct = new Item(cfm.itemDef, numCreate);
					shopStockManager.Put(timeNow, itemProduct, simulation);
				}
			}

			// 賃金を払う。
			this.AddMoney(timeNow, -result.gain);
		} else {
		}

		return result;
	}

	// 商品価格に対してフォードバックを与える。どのくらいの確率で選択されたか、各Humanがフィードバックを与える。
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
		long duration = timeNow - this.timeLastManagePrice;
		this.timeLastManagePrice = timeNow;

		int minImpression = 10;

		// 何日後に完成を目指すかは、どこかで設定できるようにすべき。
		long durationFutureTarget;
		if (this.forBuilding == true) {
			durationFutureTarget = 60L * 24L * 30;
		} else {
			durationFutureTarget = 60L * 24L * 365L * 10L * 1000L;
		}

		double productStock = this.shopStockManager.GetNumStock();
		double productCapacity = this.shopStockManager.GetCapacity();

		{
			double gainGlobal = -Double.MAX_VALUE;
			FeedbackLog feedbackProductGlobal = null;
			TreeMap<ItemDef, FeedbackLog> feedbackMaterialGlobal = null;
			FeedbackLog feedbackMakerGlobal = null;

			FeedbackLog[] feedbackProductList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();

			// 製品の露出が一回も無いときは、「現在の製品価格で販売量0」を設定しておく。
			{
				if (feedbackProductList.length == 0 || feedbackProductList[0].impressionTotal < minImpression) {
					feedbackProductList = new FeedbackLog[1];
					feedbackProductList[0] = new FeedbackLog(this.shopStockManager.price);
				}
			}

			// 利益が最も大きくなる設定を探す。
			// 製品価格に応じて、売れる速度が違う。
			// 売れる速度に対して、適切な材料価格と労働者賃金を設定したときの、利益を計算する。
			for (FeedbackLog procutFeedback : feedbackProductList) {

				// 販売量に見合うだけの材料を仕入れるための価格設定を探す。
				TreeMap<ItemDef, FeedbackLog> materialFeedbackBest = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();

					// 販売速度numSellと最も近い速度で仕入れできる、最適仕入れ価格を見つける。
					FeedbackLog materialFeedbakLocalBest = null;
					{
						FactoryMaterialManager fmm = e2.getValue();

						StockManager msm = this.deliverStockManager.get(materialItemDef);
						double materialStock = msm.GetNumStock();
						double materialCapacity = msm.GetCapacity();
						double numMaterialTarget;
						if (this.forBuilding == false) {
							numMaterialTarget = procutFeedback.quantityTotal * fmm.factoryMaterialInfo.amount
									+ (materialCapacity / 2 - materialStock) / durationFutureTarget * duration;
						} else {
							numMaterialTarget = 1.0 * fmm.factoryMaterialInfo.amount / durationFutureTarget * duration;
						}

						FeedbackLog[] materialFeedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
						if (materialFeedbackList.length > 0 && materialFeedbackList[0].impressionTotal > minImpression) {
							double scoreBest = Double.MAX_VALUE;
							for (FeedbackLog materialFeedback : materialFeedbackList) {
								double score = Math.abs(numMaterialTarget - materialFeedback.quantityTotal);
								if (score <= scoreBest) {
									scoreBest = score;
									materialFeedbakLocalBest = materialFeedback;
								}
							}
						}
						if (materialFeedbakLocalBest == null) {
							materialFeedbakLocalBest = new FeedbackLog(msm.price);
							materialFeedbakLocalBest.quantityTotal = numMaterialTarget;
						}
					}
					materialFeedbackBest.put(materialItemDef, materialFeedbakLocalBest);
				}

				// 販売量に見合うだけの労働者を確保するための価格設定を探す。
				FeedbackLog makerFeedbackBest = null;
				{
					double numMakerTarget;
					if (this.forBuilding == false) {
						numMakerTarget = (procutFeedback.quantityTotal + (productCapacity / 2 - productStock) / durationFutureTarget * duration)
								/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
					} else {
						numMakerTarget = (1.0 / durationFutureTarget * duration)
								/ this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
					}

					FeedbackLog[] makerFeedbackList = this.factoryProductManager.factoryMakerManager.feedbackManager
							.CollectResultWithEqualImpressionAdjust();
					if (makerFeedbackList.length > 0 && makerFeedbackList[0].impressionTotal > minImpression) {
						double scoreBest = Double.MAX_VALUE;
						for (FeedbackLog makerFeedback : makerFeedbackList) {
							double score = Math.abs(numMakerTarget - makerFeedback.quantityTotal);
							if (score < scoreBest) {
								scoreBest = score;
								makerFeedbackBest = makerFeedback;
							}
						}
					}
					if (makerFeedbackBest == null) {
						makerFeedbackBest = new FeedbackLog(this.factoryProductManager.factoryMakerManager.wage);
						makerFeedbackBest.quantityTotal = numMakerTarget;
					}
				}

				// 生産速度を求める。
				double numProducableMin = procutFeedback.quantityTotal;
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
						double numProducable = makerFeedbackBest.quantityTotal
								* this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
						if (numProducable < numProducableMin) {
							numProducableMin = numProducable;
						}
					}
				}

				// 利潤を求める。
				double gain;
				{
					{
						gain = numProducableMin * procutFeedback.price;
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
					feedbackProductGlobal = procutFeedback;
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
