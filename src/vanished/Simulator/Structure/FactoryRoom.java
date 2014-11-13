package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.FactoryProductManager.FactoryMaterialManager;

public class FactoryRoom extends ShopRoom {

	private boolean forBuilding = false;

	private FactoryProductManager factoryProductManager;

	public FactoryRoom(Building building, FactoryRoomDef roomDef, boolean forBuilding) {
		super(building, roomDef);
		factoryProductManager = new FactoryProductManager(roomDef.factoryProductInfo);
		this.forBuilding = forBuilding;
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);
		System.out.println("===Factory Department===");
		System.out.println("factory skillname : " + this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill.GetName());
		double wageForFullWork = this.factoryProductManager.factoryMakerManager.GetWage();
		System.out.println("product wage : " + wageForFullWork);
	}

	public class CallForMaker {
		public ItemDef itemDef;
		public Skill skill;
		public double wageForFullWork;
		public int wageIndex;
		public double numMake;
		public long duration;
		public double gain;

		public CallForMaker(ItemDef itemDef, Skill skill, double wageForFullWork, int wageIndex, double numMake, long duration, double gain) {
			this.itemDef = itemDef;
			this.skill = skill;
			this.wageForFullWork = wageForFullWork;
			this.wageIndex = wageIndex;
			this.numMake = numMake;
			this.duration = duration;
			this.gain = gain;
		}
	}

	// 欲しい人材リストを返す。
	public CallForMaker GetDesiredMaker(double numMakeMax) {
		FactoryRoomDef roomDef = (FactoryRoomDef) (this.roomDef);

		// 作るアイテムの個数を調べる。
		double numMakableMin = Double.MAX_VALUE;
		{
			if (numMakeMax < numMakableMin) numMakableMin = numMakeMax;

			if (this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake < numMakableMin) {
				numMakableMin = this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;
			}

			if (numMakableMin == 0) return null;
		}

		double wageForFullWork = this.factoryProductManager.factoryMakerManager.GetWage();
		int wageIndex = this.factoryProductManager.factoryMakerManager.GetWageIndex();

		long duration = (long) (numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * this.factoryProductManager.factoryMakerManager.factoryMakerInfo.durationForMake);
		if (duration == 0) duration = 1L;

		double gain = wageForFullWork * numMakableMin / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake;

		CallForMaker cfw = new CallForMaker(roomDef.productItemDef, this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill,
				wageForFullWork, wageIndex, numMakableMin, duration, gain);
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
		FactoryRoomDef roomDef = (FactoryRoomDef) (this.roomDef);

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

		CallForMakerInKind cfw = new CallForMakerInKind(roomDef.productItemDef,
				this.factoryProductManager.factoryMakerManager.factoryMakerInfo.skill, numMakableMin, duration);
		return cfw;
	}

	public void SetMakerPriceIndex(int index) {
		this.factoryProductManager.factoryMakerManager.SetWageIndex(index);
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

			makerOutputMoneyEMA.Add(timeNow, cfm.gain);
		}
	}

	public Item MakeInKind(CallForMakerInKind cfm, long timeNow, boolean simulation) throws Exception {

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
	public void FeedbackAboutMakerPrice(CallForMaker cfm, int priceIndex, double quantity) {
		// System.out.println("FeedbackAboutMakerPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
		this.factoryProductManager.factoryMakerManager.Feedback(priceIndex, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	public void DiscardOldLog(long timeNow) throws Exception {
		super.DiscardOldLog(timeNow);
	}

	private ExponentialMovingAverage makerOutputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 1, true);

	public double GetMakerOutputMoneyEMA(long timeNow) {
		return this.makerOutputMoneyEMA.GetAverage(timeNow);
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

		// TODO:何日後に完成を目指すかは、どこかで設定できるようにすべき。
		long durationFutureTarget;
		if (this.forBuilding == true) {
			durationFutureTarget = 60L * 24L * 30L;
		} else {
			durationFutureTarget = 60L * 24L * 10L;
		}

		{
			double productNumStock = this.shopStockManager.GetNumStock();

			class Pack {
				FeedbackLog productFeedback = null;
				FeedbackLog makerFeedback = null;
				TreeMap<ItemDef, FeedbackLog> materialFeedback = null;
				double profit = 0;

				public Pack(FeedbackLog productFeedback, FeedbackLog makerFeedback, TreeMap<ItemDef, FeedbackLog> materialFeedback, double profit) {
					this.productFeedback = productFeedback;
					this.makerFeedback = makerFeedback;
					this.materialFeedback = materialFeedback;
					this.profit = profit;
				}
			}

			ArrayList<Pack> packs = new ArrayList<Pack>();

			// 利益が最も大きくなる設定を探す。
			// 製品価格に応じて、売れる速度が違う。
			// 売れる速度に対して、適切な材料価格と労働者賃金を設定したときの、利益を計算する。
			FeedbackLog[] productFeedbackList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			for (FeedbackLog productFeedback : productFeedbackList) {

				double salesAccount = productFeedback.quantityTotal / duration * durationFutureTarget * productFeedback.priceIndex;

				// 販売量に見合うだけの労働者を確保するための価格設定を探す。
				FeedbackLog makerFeedbackBest = null;
				double makerCostBest = Double.MAX_VALUE;
				{
					FeedbackLog[] makerFeedbackList = this.factoryProductManager.factoryMakerManager.CollectResultWithEqualImpressionAdjust();

					double priceMax = 0;
					for (FeedbackLog makerFeedback : makerFeedbackList) {
						if (makerFeedback.priceIndex > priceMax) {
							priceMax = makerFeedback.priceIndex;
						}
					}

					for (FeedbackLog makerFeedback : makerFeedbackList) {

						double productAdd = makerFeedback.quantityTotal / duration * durationFutureTarget;
						double productSub = productFeedback.quantityTotal / duration * durationFutureTarget;

						double cost = productAdd / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
								* makerFeedback.priceIndex;

						double costExt = 0;
						double res = productAdd + productNumStock - productSub;
						if (res < 0) {
							costExt = -res / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake * priceMax * 1000;
						}

						double score = cost + costExt;

						if (score < makerCostBest) {
							makerCostBest = score;
							makerFeedbackBest = makerFeedback;
						}
					}
				}

				// 販売量に見合うだけの材料を仕入れるための価格設定を探す。
				TreeMap<ItemDef, FeedbackLog> materialFeedbackBestMap = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				double materialCostBestTotal = 0;

				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();
					FactoryMaterialManager fmm = e2.getValue();
					StockManager msm = this.deliverStockManager.get(materialItemDef);

					// 販売速度numSellと最も近い速度で仕入れできる、最適仕入れ価格を見つける。
					FeedbackLog materialFeedbackBest = null;
					double materialCostBest = Double.MAX_VALUE;
					{
						double materialNumStock = msm.GetNumStock();

						FeedbackLog[] materialFeedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();

						double priceMax = 0;
						for (FeedbackLog materialFeedback : materialFeedbackList) {
							if (materialFeedback.priceIndex > priceMax) {
								priceMax = materialFeedback.priceIndex;
							}
						}

						for (FeedbackLog materialFeedback : materialFeedbackList) {

							double materialAdd = materialFeedback.quantityTotal / duration * durationFutureTarget;
							double materialSub = productFeedback.quantityTotal * fmm.factoryMaterialInfo.amount / duration * durationFutureTarget;

							double cost = materialAdd * materialFeedback.priceIndex;

							double costExt = 0;
							double res = materialAdd + materialNumStock - materialSub;
							if (res < 0) {
								costExt = -res * priceMax * 1000;
							}

							double score = cost + costExt;

							if (score <= materialCostBest) {
								materialCostBest = score;
								materialFeedbackBest = materialFeedback;
							}
						}
					}
					materialFeedbackBestMap.put(materialItemDef, materialFeedbackBest);
					materialCostBestTotal += materialCostBest;
				}

				Pack pack = new Pack(productFeedback, makerFeedbackBest, materialFeedbackBestMap, salesAccount - makerCostBest
						- materialCostBestTotal);
				packs.add(pack);
			}

			if (true) {
				Pack packMax = null;
				if (true) {
					double profitMax = -Double.MAX_VALUE;
					for (Pack pack : packs) {
						if (pack.profit > profitMax) {
							packMax = pack;
							profitMax = pack.profit;
						}
					}
				}

				// TODO: 出力してみる。
				if (false) {
					for (Pack pack : packs) {

						FeedbackLog productFeedback = pack.productFeedback;
						FeedbackLog makerFeedback = pack.makerFeedback;

						double gain = productFeedback.quantityTotal / duration * durationFutureTarget * productFeedback.priceIndex;

						System.out.println("================================");
						if (pack == packMax) {
							System.out.println("Selected");
						}

						System.out.println("利益 : " + pack.profit);

						System.out.println("売り上げ : " + gain);
						System.out.println(productFeedback.priceIndex + ", " + productFeedback.quantityTotal / duration * durationFutureTarget);

						// 販売量に見合うだけの労働者を確保するための価格設定を探す。
						{
							double productAdd = makerFeedback.quantityTotal / duration * durationFutureTarget;
							double productSub = productFeedback.quantityTotal / duration * durationFutureTarget;

							double cost = productAdd / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
									* makerFeedback.priceIndex;

							double costExt = 0;
							double res = productAdd + productNumStock - productSub;
							if (res < 0) {
								costExt = -res / this.factoryProductManager.factoryMakerManager.factoryMakerInfo.numProductPerMake
										* this.factoryProductManager.factoryMakerManager.GetWage() * 10;
							}

							double costTotal = cost + costExt;

							System.out.println(makerFeedback.priceIndex + ", " + makerFeedback.quantityTotal / duration * durationFutureTarget);
							System.out.println("costTotal : " + costTotal + " = " + cost + " + " + costExt);
						}

						// 販売量に見合うだけの材料を仕入れるための価格設定を探す。

						for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
							ItemDef materialItemDef = e2.getKey();
							FactoryMaterialManager fmm = e2.getValue();
							StockManager msm = this.deliverStockManager.get(materialItemDef);
							FeedbackLog materialFeedback = pack.materialFeedback.get(materialItemDef);

							// 販売速度numSellと最も近い速度で仕入れできる、最適仕入れ価格を見つける。
							{
								double materialNumStock = msm.GetNumStock();

								double materialAdd = materialFeedback.quantityTotal / duration * durationFutureTarget;
								double materialSub = productFeedback.quantityTotal * fmm.factoryMaterialInfo.amount / duration * durationFutureTarget;

								double cost = materialAdd * materialFeedback.priceIndex;

								double costExt = 0;
								double res = materialAdd + materialNumStock - materialSub;
								if (res < 0) {
									costExt = -res * msm.GetPriceWithRate() * 10;
								}

								double costTotal = cost + costExt;

								System.out.println(materialFeedback.priceIndex + ", " + materialFeedback.quantityTotal
										/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget);
								System.out.println("material " + materialItemDef.GetName() + " costTotal : " + costTotal + " = " + cost + " + "
										+ costExt);
							}
						}
					}
				}

				// 最適な価格に設定する。
				{
					{
						this.shopStockManager.SetPrice(packMax.productFeedback.priceIndex);
					}

					{
						this.factoryProductManager.factoryMakerManager.SetWageIndex(packMax.makerFeedback.priceIndex);
					}

					for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
						ItemDef itemDef = e.getKey();
						StockManager msm = e.getValue();
						FeedbackLog feedbackMaterial = packMax.materialFeedback.get(itemDef);
						msm.SetPrice(feedbackMaterial.priceIndex);
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
					if (feedback.priceIndex == this.shopStockManager.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.priceIndex + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
							* durationFutureTarget + "個" + flag);
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
				// System.out.println(msm.stockManagerInfo.itemDef.GetName());
				FeedbackLog[] feedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.priceIndex == msm.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.priceIndex + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal
							/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget + "個" + flag);
				}
				msm.ResetStatisticalParameters();
			}

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("$                      ManageMakerWage                        $");
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			{
				FeedbackLog[] feedbackList = this.factoryProductManager.factoryMakerManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.priceIndex == this.factoryProductManager.factoryMakerManager.GetWage()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.priceIndex + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
							* durationFutureTarget + "個" + flag);
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
