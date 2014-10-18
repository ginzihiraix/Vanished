package vanished.GUI;

import ibm.ANACONDA.MyMatrix;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import vanished.Simulator.HumanManager;
import vanished.Simulator.MapManager;
import vanished.Simulator.Structure.Building;

import com.jogamp.opengl.util.Animator;

public class GUI_Game implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	int width = 800;
	int height = 600;

	MapManager mm;
	HumanManager hm;

	GraphicManager gm;

	LightSetting lightGlobal;
	Camera camera = new Camera();

	public GUI_Game(MapManager mm, HumanManager hm) {
		this.mm = mm;
		this.hm = hm;

		gm = new GraphicManager(mm, hm);

		Frame frame = new Frame("Vanished");
		frame.addKeyListener(this);

		// 3Dを描画するコンポーネント
		GLCanvas canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);

		frame.add(canvas);
		frame.setSize(width, height);

		Animator animator = new Animator(canvas);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		animator.start();
		frame.setVisible(true);
		frame.setFocusable(false);
		canvas.setFocusable(true);
	}

	public void LoadTexture(GL2 gl, String filename, int textureID) throws Exception {
		BufferedImage image = ImageIO.read(new File(filename));
		width = image.getWidth();
		height = image.getHeight();
		DataBufferByte dbi = (DataBufferByte) image.getRaster().getDataBuffer();
		byte[] data = dbi.getData();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = (y * width + x) * 3;
				byte r = data[index + 2];
				byte g = data[index + 1];
				byte b = data[index + 0];
				data[index + 0] = r;
				data[index + 1] = g;
				data[index + 2] = b;
			}
		}
		ByteBuffer b = ByteBuffer.wrap(data);

		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, width, height, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, b);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		GL2 gl = drawable.getGL().getGL2();

		try {
			LoadTexture(gl, "system/texture/texture_001.png", 1);
			LoadTexture(gl, "system/texture/shade_0.png", 1000);
			LoadTexture(gl, "system/texture/shade_1.png", 1001);
			LoadTexture(gl, "system/texture/shade_2.png", 1002);
			LoadTexture(gl, "system/texture/shade_3.png", 1003);
			LoadTexture(gl, "system/texture/shade_4.png", 1004);
			LoadTexture(gl, "system/texture/shade_5.png", 1005);
			LoadTexture(gl, "system/texture/shade_6.png", 1006);
			LoadTexture(gl, "system/texture/shade_7.png", 1007);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// DITHERをオフにします。 DITHERとは量子化の誤差を最小にするべく サンプルデータに意図的に追加される誤った信号・データのこと。
		gl.glDisable(GL2.GL_DITHER);
		// gl.glEnable(GL2.GL_DITHER);

		// OpenGLにスムージングを設定
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_FASTEST);
		// gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		// 背景色
		gl.glClearColor(0, 0, 0, 1);

		// スムースシェーディング：平面のポリゴンを曲面に見せかける処理。
		gl.glShadeModel(GL2.GL_SMOOTH);

		// 多角形に影を付けるには、各多角形の前後関係を決定する必要がある。 これをするのが、デプステストです。 このデプステストを有効にします。
		gl.glEnable(GL2.GL_DEPTH_TEST);

		// カリングの設定
		gl.glFrontFace(GL2.GL_CCW);

		gl.glDepthFunc(GL2.GL_LEQUAL);

		// ライトの設定
		float[] lightPos = { 1, 8, -3, 0 };
		float[] lightDiffuseColor = { 0.5f, 0.5f, 0.5f, 1 };
		float[] lightAmbientColor = { 0.5f, 0.5f, 0.5f, 1 };
		lightGlobal = new LightSetting(true, lightPos, lightDiffuseColor, lightAmbientColor);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("reshape");

		GL2 gl = drawable.getGL().getGL2();

		// OpenGLが表示に使う長方形領域を設定します。 (x,y)が表示領域の左上の座標、(w,h)が表示領域の幅と高さになります。
		gl.glViewport(0, 0, width, height);

		// 透視投影(遠近投影法)の行列を指定する。
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// ビューボリューム(視体積)を指定します。 引数は視体積の左、右、下、上、近クリップ面、遠クリップ面の座標を定義
		float ratio = (float) width / height;
		float near = 0.1f;
		float far = 10000;
		gl.glFrustum(-ratio / 2 * near, ratio / 2 * near, -0.5f * near, 0.5f * near, near, far);
		this.width = width;
		this.height = height;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// System.out.println("display");

		GL2 gl = drawable.getGL().getGL2();

		try {
			synchronized (this) {
				// 画面をクリア
				gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

				// Voxelをレンダリング
				gm.Render(gl, lightGlobal, camera);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		System.out.println("dispose");

	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// クリックした場所にビルがあれば、ビル情報を別窓で表示する。
		try {
			Point point = event.getPoint();
			double[][] p1Data = { { (point.x - width * 0.5) / height }, { (height * 0.5 - point.y) / height }, { 1 } };
			MyMatrix p1 = new MyMatrix(p1Data);

			MyMatrix R = camera.GetR();
			MyMatrix t = camera.GetT();

			MyMatrix v1 = R.times(p1);

			double rate1 = -t.get(1, 0) / v1.get(1, 0);

			if (rate1 > 0) {
				MyMatrix pos = t.plus(v1.times(rate1));

				int cx = (int) pos.get(0, 0);
				int cz = (int) pos.get(2, 0);

				ArrayList<Building> ret = mm.GetBuildingList(cx, cx + 1, cz, cz + 1);
				if (ret.size() > 0) {
					Building b = ret.get(0);

					GUI_Graph_Building graph = new GUI_Graph_Building(b);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		pointOld = event.getPoint();

	}

	@Override
	public void mousePressed(MouseEvent e) {
		pointOld = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pointOld = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	Point pointOld = null;

	@Override
	public void mouseDragged(MouseEvent event) {

		// System.out.println(event.getModifiers());

		try {
			Point point = event.getPoint();
			// System.out.println(point.x + ", " + point.y);

			if (pointOld != null) {
				if (event.getModifiers() == MouseEvent.BUTTON1_MASK) {
					double[][] p1Data = { { (point.x - width * 0.5) / height }, { (height * 0.5 - point.y) / height }, { 1 } };
					double[][] p2Data = { { (pointOld.x - width * 0.5) / height }, { (height * 0.5 - pointOld.y) / height }, { 1 } };
					MyMatrix p1 = new MyMatrix(p1Data);
					MyMatrix p2 = new MyMatrix(p2Data);

					MyMatrix R = camera.GetR();
					MyMatrix t = camera.GetT();

					MyMatrix v1 = R.times(p1);
					MyMatrix v2 = R.times(p2);

					double rate1 = -t.get(1, 0) / v1.get(1, 0);
					double rate2 = -t.get(1, 0) / v2.get(1, 0);

					if (rate1 > 0 && rate2 > 0) {
						v1.timesEquals(rate1);
						v2.timesEquals(rate2);
						MyMatrix def = v2.minus(v1);
						camera.MoveLookat(def);
					}
				} else if (event.getModifiers() == MouseEvent.BUTTON2_MASK) {

					double[][] p1Data = { { -point.x }, { point.y }, { 1 } };
					double[][] p2Data = { { -pointOld.x }, { pointOld.y }, { 1 } };
					MyMatrix p1 = new MyMatrix(p1Data);
					MyMatrix p2 = new MyMatrix(p2Data);

					MyMatrix R = camera.GetR();

					MyMatrix defLocal = p2.minus(p1).times(0.005);
					MyMatrix defGlobal = R.times(defLocal);

					camera.ZMove(defGlobal);
				}
			}

			pointOld = point;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		this.camera.ChangeDistanceLevel(rot);

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
