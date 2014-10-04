package vanished.GUI;

import ibm.ANACONDA.MatrixUtility;
import ibm.ANACONDA.MyMatrix;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.Rect;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.Room;

import com.jogamp.opengl.util.gl2.GLUT;

public class GraphicManager {

	MapManager mm;
	HumanManager hm;

	public GraphicManager(MapManager mm, HumanManager hm) {
		this.mm = mm;
		this.hm = hm;
	}

	public void Render(GL2 gl, LightSetting lightGlobal, Camera camera) throws Exception {

		GLUT glut = new GLUT();

		double distance = camera.GetDistance();
		int radius = (int) (distance * 1);
		if (radius > 30) radius = 30;
		if (radius < 5) radius = 5;
		int numPlane = radius * 2;

		// 本来のカメラの注視点
		MyMatrix lookat = camera.GetLookat();

		// 本来のカメラの注視点（整数版）
		int cx = (int) (lookat.get(0, 0));
		int cz = (int) (lookat.get(2, 0));

		// (cx,cz)を中心とした幅numPlaneのVoxelを作る。
		VoxelManager vm = new VoxelManager(numPlane, 2, numPlane);
		for (int x = 0; x < numPlane; x++) {
			for (int z = 0; z < numPlane; z++) {
				vm.PutVoxel(x, 0, z, 1);
			}
		}

		// (cx-radius, cy-radius) - (cx+radius, cy+radius)の範囲にある建物を集める。
		ArrayList<Building> buildingList = mm.GetBuildingList(cx - radius, cx + radius, cz - radius, cz + radius);
		for (Building building : buildingList) {
			Rect rect = building.GetLocation();
			for (int x = rect.left; x < rect.right; x++) {
				for (int z = rect.bottom; z < rect.top; z++) {
					vm.PutVoxel(x - cx + radius, 1, z - cz + radius, 1);
				}
			}
		}

		// Voxelを描画する。
		{
			// Voxel用にView行列を設定する。
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			float[] m = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
			gl.glMultMatrixf(m, 0);

			float[] m2 = camera.GetRi4();
			gl.glMultMatrixf(m2, 0);

			MyMatrix RModel = MatrixUtility.ConstructDiagonalMatrix(1, 3);
			double[][] tModelData = { { cx - radius }, { -1 }, { cz - radius } };
			MyMatrix tModel = new MyMatrix(tModelData);
			float[] m3 = MU.ExpandMat4(MU.GetR4(RModel, tModel));
			gl.glMultMatrixf(m3, 0);

			vm.Reconstruct();
			vm.Draw(gl, lightGlobal);
		}

		// グラフを描画する。
		// {
		// for (Building building : buildingList) {
		// Rect rect = building.GetLocation();
		// double px = (rect.left + rect.right) / 2;
		// double py = (rect.bottom + rect.top) / 2;
		// }
		// }

		// 売り上げ・費用の棒グラフを描画する。
		{
			long timeNow = hm.GetOldestTime();

			// 最大値を計算する。
			double max = 0;
			for (Building building : buildingList) {
				ArrayList<Room> rooms = building.GetRoomList();
				for (Room room : rooms) {
					if (room instanceof FactoryRoom == false) continue;
					FactoryRoom froom = (FactoryRoom) room;

					double productInputMoneyEMA = froom.GetProductInputMoneyEMA(timeNow);
					if (productInputMoneyEMA > max) {
						max = productInputMoneyEMA;
					}
				}
			}

			// グラフを書く。
			{
				gl.glDisable(GL2.GL_TEXTURE_2D);
				gl.glDisable(GL2.GL_BLEND);
				lightGlobal.SetLight(gl);
				gl.glEnable(GL2.GL_COLOR_MATERIAL);
				// gl.glEnable(GL2.GL_NORMALIZE);

				for (Building building : buildingList) {
					Rect rect = building.GetLocation();
					double px = (rect.left + rect.right) / 2.0;
					double pz = (rect.bottom + rect.top) / 2.0;
					double[][] posData = { { px }, { 1 }, { pz } };
					MyMatrix pos = new MyMatrix(posData);

					ArrayList<Room> rooms = building.GetRoomList();
					double numRoom = 0;
					for (Room room : rooms) {
						if (room instanceof FactoryRoom == false) continue;
						numRoom++;
					}

					double numRoom2 = 0;
					for (Room room : rooms) {
						if (room instanceof FactoryRoom == false) continue;
						FactoryRoom froom = (FactoryRoom) room;
						double productInputMoneyEMA = froom.GetProductInputMoneyEMA(timeNow);

						{
							gl.glLoadIdentity();

							float[] m0 = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
							gl.glMultMatrixf(m0, 0);

							float[] m2 = camera.GetRi4();
							gl.glMultMatrixf(m2, 0);

							MyMatrix RModel = MatrixUtility.ConstructDiagonalMatrix(1, 3);
							float[] m3 = MU.ExpandMat4(MU.GetR4(RModel, pos));
							gl.glMultMatrixf(m3, 0);

							gl.glScaled(0.03, 3 * productInputMoneyEMA / max, 0.03);

							gl.glRotated(-90.0, 1.0, 0.0, 0.0);

							gl.glColor3d(1, 0, 0);
							glut.glutSolidCylinder(1, 1, 16, 1);
						}

						numRoom2++;
					}
				}
			}

			// 文字列を描画
			// Font font = new Font("Arial", java.awt.Font.PLAIN, 12);
			// TextRenderer tr = new TextRenderer(font, true, true);
			// tr.beginRendering(100, 100);
			// tr.setColor(0, 0, 0, 1.0f);
			// gl.glDisable(GL2.GL_TEXTURE_2D);
			// gl.glDisable(GL2.GL_BLEND);
			// lightGlobal.SetLight(gl);
			//
			// for (Building building : buildingList) {
			// Rect rect = building.GetLocation();
			// double px = (rect.left + rect.right) / 2.0;
			// double pz = (rect.bottom + rect.top) / 2.0;
			// double[][] posData = { { px }, { 1 }, { pz } };
			// MyMatrix pos = new MyMatrix(posData);
			//
			// {
			// gl.glLoadIdentity();
			// float[] m = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
			// gl.glMultMatrixf(m, 0);
			//
			// float[] m2 = camera.GetRi4();
			// gl.glMultMatrixf(m2, 0);
			//
			// MyMatrix RModel = MatrixUtility.ConstructDiagonalMatrix(1, 3);
			// float[] m3 = MU.ExpandMat4(MU.GetR4(RModel, pos));
			// gl.glMultMatrixf(m3, 0);
			//
			// gl.glPushMatrix();
			// gl.glScaled(1, 10, 1);
			// gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
			// // glut.glutSolidCylinder(0.1, 0.1, 16, 1);
			// glut.glutWireCylinder(0.1, 0.1, 16, 1);
			// gl.glPopMatrix();
			//
			// }
			//
			// }

			// tr.endRendering();
		}
	}
}
