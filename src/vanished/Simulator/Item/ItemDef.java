package vanished.Simulator.Item;

import java.util.ArrayList;
import java.util.Properties;

import vanished.Simulator.ObjectDef;

public class ItemDef extends ObjectDef {

	// アイテムの名前
	String name;

	// アイテム1単位の重さ。
	double weight;

	// 効用のリスト
	ArrayList<Utility> utilities = new ArrayList<Utility>();

	public ItemDef(String name, Properties p) throws Exception {
		this.name = name;

		weight = Double.parseDouble(p.getProperty("weight"));

		String utilityDuration = p.getProperty("utility.duration");

		{
			String prefix = "utility.";
			for (String keyOrg : p.stringPropertyNames()) {
				if (keyOrg.startsWith(prefix) == false) continue;
				String key = keyOrg.replace(prefix, "");
				String[] parts = key.split("\\.");
				if (parts.length >= 2 && parts[0].equals("power")) {
					String utilityType = parts[1];
					String utilityName = parts[2];
					String utilityDecay = parts[3];
					double power = Double.parseDouble(p.getProperty(keyOrg));

					Utility u = new Utility(utilityType, utilityName, utilityDuration, utilityDecay, power);
					this.utilities.add(u);
				}
			}
		}
	}

	public String GetName() {
		return name;
	}

	public double GetWeight() {
		return weight;
	}

	public ArrayList<Utility> GetUtilities() {
		return this.utilities;
	}
}
