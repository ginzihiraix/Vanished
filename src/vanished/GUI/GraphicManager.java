package vanished.GUI;

import ibm.ANACONDA.MyMatrix;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.Rect;
import vanished.Simulator.Structure.Building;

public class GraphicManager {

	Camera camera = new Camera();

	MapManager mm;
	HumanManager hm;

	public GraphicManager(MapManager mm, HumanManager hm) {
		this.mm = mm;
		this.hm = hm;
	}

	public void Render(GL2 gl, LightSetting lightGlobal) {
		MyMatrix lookat = camera.GetLookat();
		int cx = (int) (lookat.get(0, 0) / 10);
		int cy = (int) (lookat.get(1, 0) / 10);

		double distance = camera.GetDistance();

		int delta = (int) (distance * 3 / 10);

		int numPlane = delta * 2 * delta * 2;

		VoxelManager vm = new VoxelManager(numPlane, 2, numPlane);
		for (int x = 0; x < numPlane; x++) {
			for (int z = 0; z < numPlane; z++) {
				vm.PutVoxel(x, 0, z, 1);
			}
		}

		ArrayList<Building> buildingList = mm.GetBuildingList(cx - numPlane, cx + numPlane, cy - numPlane, cy + numPlane);
		for (Building building : buildingList) {
			Rect rect = building.GetLocation();
			for (int x = rect.left; x < rect.right; x++) {
				for (int z = rect.bottom; z < rect.top; z++) {
					vm.PutVoxel(x, 1, z, 1);
				}
			}
		}

		vm.PutVoxel(0, 1, 0, 1);
		vm.Reconstruct();
		vm.Draw(gl, lightGlobal);
	}

}
