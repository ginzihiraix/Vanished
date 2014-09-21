package vanished.Simulator.Structure;

import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;

public class FactoryRoomDef extends ShopRoomDef {

	FactoryProductInfo factoryProductInfo;

	public class FactoryMaterialInfo {

		// ���Y����A�C�e��
		ItemDef itemDef;

		// ���i��ɂ��K�v�ȗ�
		double amount;

		public FactoryMaterialInfo(ItemDef itemDef, String prefix, Properties p) {
			this.itemDef = itemDef;
			this.amount = Double.parseDouble(p.getProperty(prefix + "amount"));
		}
	}

	public class FactoryMakerInfo {
		// �K�v�ȃX�L��
		Skill skill;

		// �J���҂̈��̘J������
		long durationForMake;

		// ���̘J���Ő�������A�C�e���̐�
		double numProductPerMake;

		// �����҂̃L���p�V�e�B
		int capacityMaker;

		public FactoryMakerInfo(Skill skill, String prefix, Properties p) {
			this.skill = skill;
			this.durationForMake = Integer.parseInt(p.getProperty(prefix + "durationForMake"));
			this.numProductPerMake = Double.parseDouble(p.getProperty(prefix + "numProductPerMake"));
			this.capacityMaker = Integer.parseInt(p.getProperty(prefix + "capacityMaker"));
		}
	}

	public class FactoryProductInfo {
		ItemDef itemDef;

		// ���i��numProductPerWork���̂ɕK�v�ȍޗ�
		TreeMap<ItemDef, FactoryMaterialInfo> factoryMaterialInfo = new TreeMap<ItemDef, FactoryMaterialInfo>(new ItemDefComparator());

		// �����ł���X�L��
		FactoryMakerInfo factoryMakerInfo;

		public FactoryProductInfo(ItemDef itemDef, String prefix, Properties p) throws Exception {
			this.itemDef = itemDef;

			TreeMap<ItemDef, Boolean> items = new TreeMap<ItemDef, Boolean>(new ItemDefComparator());
			for (String keyOrg : p.stringPropertyNames()) {
				if (keyOrg.startsWith(prefix) == false) continue;
				String key = keyOrg.replace(prefix, "");
				String[] parts = key.split("\\.");
				if (parts.length >= 2 && parts[0].equals("material")) {
					ItemDef materialItemDef = GlobalParameter.dm.GetItemDef(parts[1]);
					items.put(materialItemDef, true);
				}
			}

			for (ItemDef materialItemDef : items.keySet()) {
				FactoryMaterialInfo mi = new FactoryMaterialInfo(materialItemDef, prefix + "material." + materialItemDef.GetName() + ".", p);
				factoryMaterialInfo.put(materialItemDef, mi);
			}

			String skillName = p.getProperty(prefix + "skill");
			Skill skill = GlobalParameter.dm.GetSkill(skillName);
			factoryMakerInfo = new FactoryMakerInfo(skill, prefix, p);
		}
	}

	public FactoryRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		String prefix = "factory.";
		this.factoryProductInfo = new FactoryProductInfo(this.productItemDef, prefix, p);
	}
}
