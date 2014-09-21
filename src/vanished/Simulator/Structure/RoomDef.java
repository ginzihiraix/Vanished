package vanished.Simulator.Structure;

import java.util.Properties;

public class RoomDef {
	String name;

	// �����ɓ����l�̃L���p�V�e�B
	int capacityHuman;

	// �����Ɋi�[�ł���A�C�e���̃L���p�V�e�B
	double capacityItemWeight;

	public RoomDef(String name, Properties p) {
		this.name = name;

		this.capacityHuman = Integer.parseInt(p.getProperty("all.capacityHuman"));

		this.capacityItemWeight = Double.parseDouble(p.getProperty("all.capacityItemWeight"));

	}
}