package vanished.Simulator.Structure;

import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMakerInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryMaterialInfo;
import vanished.Simulator.Structure.FactoryRoomDef.FactoryProductInfo;

public class FactoryProductManager {
	public FactoryProductInfo factoryProductInfo;

	// 製品をnumProductPerWork個作るのに必要な材料
	public TreeMap<ItemDef, FactoryMaterialManager> factoryMaterialManager = new TreeMap<ItemDef, FactoryMaterialManager>(new ItemDefComparator());

	// 製造できる人材
	public FactoryMakerManager factoryMakerManager;

	public class FactoryMaterialManager {

		public FactoryMaterialInfo factoryMaterialInfo;

		public FactoryMaterialManager(FactoryMaterialInfo materialInfo) {
			this.factoryMaterialInfo = materialInfo;
		}
	}

	public class FactoryMakerManager {

		public FactoryMakerInfo factoryMakerInfo;

		// 労働者の賃金
		private int wageIndex = 0;
		private double wageStepSize = 1.01;

		public FactoryMakerManager(FactoryMakerInfo factoryMakerInfo) {
			this.factoryMakerInfo = factoryMakerInfo;
		}

		public double GetWage() {
			return Math.pow(wageStepSize, wageIndex);
		}

		public int GetWageIndex() {
			return wageIndex;
		}

		public void SetWageIndex(int index) {
			this.wageIndex = index;
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

		public void Feedback(int priceIndex, double quantity) {
			// feedbackManager.Add(priceIndex, quantity);
		}

		public FeedbackLog[] CollectResultWithEqualImpressionAdjust() {
			return feedbackManager.CollectResultWithEqualImpressionAdjust();
		}
	}

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
