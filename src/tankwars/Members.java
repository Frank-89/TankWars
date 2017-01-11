package tankwars;

import java.util.Vector;

import javax.sound.sampled.*;

import java.io.*;

class AePlayWave extends Thread {
	private String filename;

	public AePlayWave(String wavfile) {
		filename = wavfile;
	}

	public void run() {
		File soundFile = new File(filename);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}

		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		auline.start();
		int nBytesRead = 0;
		byte[] abData = new byte[2048];

		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
					auline.write(abData, 0, nBytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			auline.drain();
			auline.close();
		}
	}
}

class Node {
	int x;
	int y;
	int direct;

	public Node(int x, int y, int direct) {
		this.x = x;
		this.y = y;
		this.direct = direct;
	}
}

// record class
class Recorder {
	private static int enNum = 20;
	private static int myLife = 3;
	public static int allEnNum = 0;
	private static FileWriter fw = null;
	private static BufferedWriter bw = null;
	private static FileReader fr = null;
	private static BufferedReader br = null;
	private Vector<EnemyTank> ets = new Vector<>();
	static Vector<Node> nodes = new Vector<Node>();

	public static int getEnNum() {
		return enNum;
	}

	public static void setEnNum(int enNum) {
		Recorder.enNum = enNum;
	}

	public static int getMyLife() {
		return myLife;
	}

	public static void setMyLife(int myLife) {
		Recorder.myLife = myLife;
	}

	public static int getAllEnNum() {
		return allEnNum;
	}

	public static void setAllEnNum(int allEnNum) {
		Recorder.allEnNum = allEnNum;
	}

	public Vector<EnemyTank> getEts() {
		return ets;
	}

	public void setEts(Vector<EnemyTank> ets) {
		this.ets = ets;
	}

	public static void reduceEnNum() {
		enNum--;
	}

	public static void reduceMyLife() {
		myLife--;
	}

	public static void addEnNumRec() {
		allEnNum++;
	}

	public static void keepRecording() {
		try {
			fw = new FileWriter("file/myRecording.txt");
			bw = new BufferedWriter(fw);
			bw.write(allEnNum + "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				fw.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void getRecording() {
		try {
			fr = new FileReader("file/myRecording.txt");
			br = new BufferedReader(fr);
			String n = br.readLine();
			allEnNum = Integer.parseInt(n);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public void keepRecAndEnemyTank() {
		try {
			fw = new FileWriter("file/myRecording.txt");
			bw = new BufferedWriter(fw);
			bw.write(allEnNum + "\r\n");
			for (int i = 0; i < ets.size(); i++) {
				EnemyTank et = ets.get(i);
				if (et.isLive) {
					String recorder = et.x + " " + et.y + " " + et.direct;
					bw.write(recorder + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				fw.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public Vector<Node> getNodesAndEnNums() {
		try {
			fr = new FileReader("file/myRecording.txt");
			br = new BufferedReader(fr);
			String n = "";
			n = br.readLine();
			allEnNum = Integer.parseInt(n);
			while ((n = br.readLine()) != null) {
				String[] xyz = n.split(" ");
				Node node = new Node(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
				nodes.add(node);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return nodes;
	}
}

// bomb class
class Bomb {
	int x;
	int y;
	int life = 9;
	boolean isLive = true;

	public Bomb(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void lifeDown() {
		if (life > 0) {
			life--;
		} else {
			this.isLive = false;
		}
	}
}

// bullet class
class Shot implements Runnable {
	int x;
	int y;
	int direct;
	int speed = 1;
	boolean isLive = true;

	public Shot(int x, int y, int direct) {
		this.x = x;
		this.y = y;
		this.direct = direct;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
			switch (direct) {
			case 0:
				y -= speed;
				break;
			case 1:
				x += speed;
				break;
			case 2:
				y += speed;
				break;
			case 3:
				x -= speed;
				break;
			}

			// decide whether the bullet is live
			if (x < 0 || x > 400 || y < 0 || y > 300) {
				this.isLive = false;
				break;
			}
		}
	}
}

// general tank class
class Tank {
	int x = 0;
	int y = 0;
	int direct = 0; // 0->up, 1->right, 2->down, 3->left
	int speed = 1;
	int color;
	boolean isLive = true;

	public Tank(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}

// hero tank class
class Hero extends Tank {
	// Shot s = null;
	Vector<Shot> ss = new Vector<Shot>();
	Shot s = null;

	public Hero(int x, int y) {
		super(x, y);
	}

	// fire
	public void shotEnemy() {
		switch (this.direct) {
		case 0:
			s = new Shot((int) (x + 9.5), (int) (y - 0.5), 0);
			ss.add(s);
			break;
		case 1:
			s = new Shot((int) (x + 29.5), (int) (y + 9.5), 1);
			ss.add(s);
			break;
		case 2:
			s = new Shot((int) (x + 9.5), (int) (y + 29.5), 2);
			ss.add(s);
			break;
		case 3:
			s = new Shot((int) (x - 0.5), (int) (y + 9.5), 3);
			ss.add(s);
			break;
		}
		// start the bullet
		Thread t = new Thread(s);
		t.start();
	}

	public void moveUp() {
		y -= 3 * speed;
	}

	public void moveRight() {
		x += 3 * speed;
	}

	public void moveDown() {
		y += 3 * speed;
	}

	public void moveLeft() {
		x -= 3 * speed;
	}
}

// enemy tank class
class EnemyTank extends Tank implements Runnable {
	int time = 0;
	Vector<Shot> ss = new Vector<>();
	Vector<EnemyTank> ets = new Vector<>();

	public EnemyTank(int x, int y) {
		super(x, y);
	}

	// get enemy tank vector on MyPanel
	public void setEts(Vector<EnemyTank> vv) {
		this.ets = vv;
	}

	// check if hit other enemy tank
	public boolean isTouchOtherEnemy() {
		boolean b = false;
		switch (this.direct) {
		case 0:
			for (int i = 0; i < ets.size(); i++) {
				EnemyTank et = ets.get(i);
				if (et != this) {
					if (et.direct == 0 || et.direct == 2) {
						if (this.x >= et.x && this.x <= et.x + 20 && this.y >= et.y && this.y <= et.y + 30) {
							return true;
						}
						if (this.x + 20 >= et.x && this.x + 20 <= et.x + 20 && this.y >= et.y && this.y <= et.y + 30) {
							return true;
						}
					}
					if (et.direct == 1 || et.direct == 3) {
						if (this.x >= et.x && this.x <= et.x + 30 && this.y >= et.y && this.y <= et.y + 20) {
							return true;
						}
						if (this.x + 20 >= et.x && this.x + 20 <= et.x + 30 && this.y >= et.y && this.y <= et.y + 20) {
							return true;
						}
					}
				}
			}
			break;
		case 1:
			for (int i = 0; i < ets.size(); i++) {
				EnemyTank et = ets.get(i);
				if (et != this) {
					if (et.direct == 0 || et.direct == 2) {
						if (this.x + 30 >= et.x && this.x + 30 <= et.x + 20 && this.y >= et.y && this.y <= et.y + 30) {
							return true;
						}
						if (this.x + 30 >= et.x && this.x + 30 <= et.x + 20 && this.y + 20 >= et.y
								&& this.y + 20 <= et.y + 30) {
							return true;
						}
					}
					if (et.direct == 1 || et.direct == 3) {
						if (this.x + 30 >= et.x && this.x + 30 <= et.x + 30 && this.y >= et.y && this.y <= et.y + 20) {
							return true;
						}
						if (this.x + 30 >= et.x && this.x + 30 <= et.x + 30 && this.y + 20 >= et.y
								&& this.y + 20 <= et.y + 20) {
							return true;
						}
					}
				}
			}
			break;
		case 2:
			for (int i = 0; i < ets.size(); i++) {
				EnemyTank et = ets.get(i);
				if (et != this) {
					if (et.direct == 0 || et.direct == 2) {
						if (this.x >= et.x && this.x <= et.x + 20 && this.y + 30 >= et.y && this.y + 30 <= et.y + 30) {
							return true;
						}
						if (this.x + 20 >= et.x && this.x + 20 <= et.x + 20 && this.y + 30 >= et.y
								&& this.y + 30 <= et.y + 30) {
							return true;
						}
					}
					if (et.direct == 1 || et.direct == 3) {
						if (this.x >= et.x && this.x <= et.x + 30 && this.y + 30 >= et.y && this.y + 30 <= et.y + 20) {
							return true;
						}
						if (this.x + 20 >= et.x && this.x + 20 <= et.x + 30 && this.y + 30 >= et.y
								&& this.y + 30 <= et.y + 20) {
							return true;
						}
					}
				}
			}
			break;
		case 3:
			for (int i = 0; i < ets.size(); i++) {
				EnemyTank et = ets.get(i);
				if (et != this) {
					if (et.direct == 0 || et.direct == 2) {
						if (this.x >= et.x && this.x <= et.x + 20 && this.y >= et.y && this.y <= et.y + 30) {
							return true;
						}
						if (this.x >= et.x && this.x <= et.x + 20 && this.y + 20 >= et.y && this.y + 20 <= et.y + 30) {
							return true;
						}
					}
					if (et.direct == 1 || et.direct == 3) {
						if (this.x >= et.x && this.x <= et.x + 30 && this.y >= et.y && this.y <= et.y + 20) {
							return true;
						}
						if (this.x >= et.x && this.x <= et.x + 30 && this.y + 20 >= et.y && this.y + 20 <= et.y + 20) {
							return true;
						}
					}
				}
			}
			break;
		}
		return b;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}

			switch (this.direct) {
			case 0:
				for (int i = 0; i < 30; i++) {
					if (y > 0 && !this.isTouchOtherEnemy()) {
						y -= speed;
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 1:
				for (int i = 0; i < 30; i++) {
					if (x < 380 && !this.isTouchOtherEnemy()) {
						x += speed;
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 2:
				for (int i = 0; i < 30; i++) {
					if (y < 270 && !this.isTouchOtherEnemy()) {
						y += speed;
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 3:
				for (int i = 0; i < 30; i++) {
					if (x > 00 && !this.isTouchOtherEnemy()) {
						x -= speed;
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			}

			this.time++;
			if (time % 2 == 0) {
				// decide if need load new bullet for enemy tank
				if (isLive) {
					if (ss.size() < 5) {
						Shot s = null;
						// there is no bullet
						switch (direct) {
						case 0:
							s = new Shot((int) (x + 9.5), (int) (y - 0.5), 0);
							ss.add(s);
							break;
						case 1:
							s = new Shot((int) (x + 29.5), (int) (y + 9.5), 1);
							ss.add(s);
							break;
						case 2:
							s = new Shot((int) (x + 9.5), (int) (y + 29.5), 2);
							ss.add(s);
							break;
						case 3:
							s = new Shot((int) (x - 0.5), (int) (y + 9.5), 3);
							ss.add(s);
							break;
						}

						// start bullet thread
						Thread t = new Thread(s);
						t.start();
					}
				}
			}

			// randomly produce a new direct for enemy tank
			this.direct = (int) (Math.random() * 4);

			// decide if enemy tank is dead
			if (this.isLive == false) {
				// if dead, out of thread
				break;
			}
		}
	}
}
