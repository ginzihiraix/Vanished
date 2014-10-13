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

		// 労働者の賃金
		// double wage = 1 + OtherUtility.RandGaussian() * 0.1;
		double wage = 1;
		double wageRate = 1;

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

		if (this.openFactory == false) return null;

		// 作るアイテムの個数を調べる。
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

	// 作業してアイテムを作る。
	public void Make(CallForMaker cfm, long timeNow, boolean simulation) throws Exception {

		if (this.openFactory == false) throw new Exception("fatal error");

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
		// System.out.println("FeedbackAboutMakerPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
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

		// 数量が0になるような料金を追加しておく。
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

		// TODO:何日後に完成を目指すかは、どこかで設定できるようにすべき。
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
							* durationFutureTarget + "個" + flag);
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal
							/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget + "個" + flag);
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
							* durationFutureTarget + "個" + flag);
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

			// 利益が最も大きくなる設定を探す。
			// 製品価格に応じて、売れる速度が違う。
			// 売れる速度に対して、適切な材料価格と労働者賃金を設定したときの、利益を計算する。
			FeedbackLog[] productFeedbackList = this.shopStockManager.feedbackManager.CollectResultWithEqualImpressionAdjust();
			for (FeedbackLog productFeedback : productFeedbackList) {

				// 販売量に見合うだけの労働者を確保するための価格設定を探す。
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

				// 販売量に見合うだけの材料を仕入れるための価格設定を探す。
				TreeMap<ItemDef, FeedbackLog> materialFeedbackBest = new TreeMap<ItemDef, FeedbackLog>(new ItemDefComparator());
				for (Entry<ItemDef, FactoryMaterialManager> e2 : this.factoryProductManager.factoryMaterialManager.entrySet()) {
					ItemDef materialItemDef = e2.getKey();
					FactoryMaterialManager fmm = e2.getValue();
					StockManager msm = this.deliverStockManager.get(materialItemDef);

					// 販売速度numSellと最も近い速度で仕入れできる、最適仕入れ価格を見つける。
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
				// 将来の在校調整結果の精度を計算する。
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

				// 足りない材料は、未来のリスクを考慮して10倍で仕入れると仮定して、コストを計算。
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

				// 最適な価格に設定する。
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
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
				System.out.println(msm.stockManagerInfo.itemDef.GetName());
				FeedbackLog[] feedbackList = msm.feedbackManager.CollectResultWithEqualImpressionAdjust();
				// TODO
				for (FeedbackLog feedback : feedbackList) {
					String flag = "";
					if (feedback.price == msm.GetPriceWithRate()) {
						flag = " <-BEST";
					}
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal
							/ fmm.factoryMaterialInfo.amount / duration * durationFutureTarget + "個" + flag);
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
					System.out.println(feedback.price + "円, " + feedback.impressionTotal + "回, " + feedback.quantityTotal / duration
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
