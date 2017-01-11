package tankwars;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

@SuppressWarnings("serial")
public class MyTankGame extends JFrame implements ActionListener {
	MyPanel mp = null;
	// define a start panel
	MyStartPanel msp = null;
	// create menu bar
	JMenuBar jmb = null;
	JMenu jm1 = null;
	JMenuItem jmi1 = null;
	JMenuItem jmi2 = null;
	JMenuItem jmi3 = null;
	JMenuItem jmi4 = null;

	public MyTankGame() {
		jmb = new JMenuBar();
		jm1 = new JMenu("Game(G)");
		jm1.setMnemonic('G');

		jmi1 = new JMenuItem("New Game(N)");
		jmi1.setMnemonic('N');
		jmi1.addActionListener(this);
		jmi1.setActionCommand("Start New Game");

		jmi2 = new JMenuItem("Exit Game(E)");
		jmi2.setMnemonic('E');
		jmi2.addActionListener(this);
		jmi2.setActionCommand("Exit Current Game");

		jmi3 = new JMenuItem("Save & Exit Game(S)");
		jmi3.setMnemonic('S');
		jmi3.addActionListener(this);
		jmi3.setActionCommand("Save & Exit Current Game");

		jmi4 = new JMenuItem("Continue Last Game(C)");
		jmi4.setMnemonic('C');
		jmi4.addActionListener(this);
		jmi4.setActionCommand("Continue Game");

		jm1.add(jmi1);
		jm1.add(jmi2);
		jm1.add(jmi3);
		jm1.add(jmi4);
		jmb.add(jm1);

		msp = new MyStartPanel();
		Thread t = new Thread(msp);
		t.start();
		this.setJMenuBar(jmb);
		this.add(msp);
		this.setSize(600, 500);
		this.setVisible(true);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		MyTankGame mtg = new MyTankGame();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Start New Game")) {
			mp = new MyPanel("newGame");
			Thread t = new Thread(mp);
			t.start();
			this.remove(msp);
			this.add(mp); // add mp to JFrame
			this.addKeyListener(mp); // register listener
			this.setVisible(true);
			Recorder.setAllEnNum(0);
		} else if (e.getActionCommand().equals("Exit Current Game")) {
			// save the number of enTank
			Recorder.keepRecording();
			System.exit(0);
		} else if (e.getActionCommand().equals("Save & Exit Current Game")) {
			Recorder rd = new Recorder();
			rd.setEts(mp.ets);
			rd.keepRecAndEnemyTank();
			System.exit(0);
		} else if (e.getActionCommand().equals("Continue Game")) {
			mp = new MyPanel("con");

			Thread t = new Thread(mp);
			t.start();
			this.remove(msp);
			this.add(mp); // add mp to JFrame
			this.addKeyListener(mp); // register listener
			this.setVisible(true);
		}
	}
}

@SuppressWarnings("serial")
class MyStartPanel extends JPanel implements Runnable {
	int times = 0;

	public void paint(Graphics g) {
		super.paint(g);
		g.fillRect(0, 0, 400, 300);
		// message
		if (times % 2 == 0) {
			g.setColor(Color.yellow);
			Font myFonot = new Font("Times New Roman", Font.BOLD, 30);
			g.setFont(myFonot);
			g.drawString("STAGE: 1", 125, 150);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			times++;
			this.repaint();
		}

	}
}

@SuppressWarnings("serial")
class MyPanel extends JPanel implements KeyListener, Runnable {
	// declare hero tank
	Hero hero = null;

	// declare enemy tank
	Vector<EnemyTank> ets = new Vector<EnemyTank>();
	Vector<Node> nodes = new Vector<>();
	int enSize = 6;

	// declare bomb set
	Vector<Bomb> bombs = new Vector<Bomb>();

	// declare three pictures
	Image image1 = null;
	Image image2 = null;
	Image image3 = null;

	// declare sound
	AePlayWave apw = null;

	// construct
	public MyPanel(String flag) {
		// recover recorder
		Recorder.getRecording();

		// instantiation hero tank
		hero = new Hero(200, 200);

		if (flag.equals("newGame")) {
			// instantiation enemy tank
			for (int i = 0; i < enSize; i++) {
				// create the enemy tank
				EnemyTank et = new EnemyTank((i + 1) * 50, 0);
				et.setColor(0);
				et.setDirect(2);
				et.setEts(ets);
				// start enemy tank
				Thread t = new Thread(et);
				t.start();
				// add a bullet to enemy tank
				Shot s = new Shot((int) (et.x + 9.5), (int) (et.y + 29.5), 2);
				et.ss.add(s);
				Thread t2 = new Thread(s);
				t2.start();
				// add enemy tank to the set
				ets.add(et);
			}
		} else {
			nodes = new Recorder().getNodesAndEnNums();
			// instantiation enemy tank
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				// create the enemy tank
				EnemyTank et = new EnemyTank(node.x, node.y);
				et.setColor(0);
				et.setDirect(2);
				et.setEts(ets);
				// start enemy tank
				Thread t = new Thread(et);
				t.start();
				// add a bullet to enemy tank
				Shot s = new Shot((int) (et.x + 9.5), (int) (et.y + 29.5), 2);
				et.ss.add(s);
				Thread t2 = new Thread(s);
				t2.start();
				// add enemy tank to the set
				ets.add(et);
			}
		}

		AePlayWave apw = new AePlayWave("sound/start.wav");
		apw.start();

		try {
			image1 = ImageIO.read(new File("image/bomb_1.gif"));
			image2 = ImageIO.read(new File("image/bomb_2.gif"));
			image3 = ImageIO.read(new File("image/bomb_3.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// draw message
	public void showInfo(Graphics g) {
		// draw message tank
		this.drawTank(80, 330, g, 0, 0);
		g.setColor(Color.black);
		g.drawString(Recorder.getEnNum() + "", 105, 350);
		this.drawTank(130, 330, g, 0, 1);
		g.setColor(Color.black);
		g.drawString(Recorder.getMyLife() + "", 155, 350);

		// draw the score
		g.setColor(Color.black);
		Font f = new Font("Times New Roman", Font.BOLD, 20);
		g.setFont(f);
		g.drawString("The total score", 420, 20);

		this.drawTank(420, 60, g, 0, 0);
		g.setColor(Color.black);
		g.drawString(Recorder.getAllEnNum() + "", 460, 80);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.fillRect(0, 0, 400, 300);
		this.showInfo(g);

		// draw hero tank
		if (hero.isLive) {
			this.drawTank(hero.getX(), hero.getY(), g, this.hero.direct, 1);
		}

		// draw bullet
		for (int i = 0; i < hero.ss.size(); i++) {
			Shot myShot = hero.ss.get(i);
			if (myShot != null && myShot.isLive == true) {
				g.draw3DRect(myShot.x, myShot.y, 1, 1, false);
			}
			if (myShot.isLive == false) {
				hero.ss.remove(myShot);
			}
		}

		// draw bomb
		for (int i = 0; i < bombs.size(); i++) {
			Bomb b = bombs.get(i);
			if (b.life > 6) {
				g.drawImage(image1, b.x, b.y, 30, 30, this);
			} else if (b.life > 4) {
				g.drawImage(image2, b.x, b.y, 30, 30, this);
			} else {
				g.drawImage(image3, b.x, b.y, 30, 30, this);
			}
			b.lifeDown();
			if (b.life == 0) {
				bombs.remove(b);
			}
		}

		// draw enemy tank
		for (int i = 0; i < ets.size(); i++) {
			EnemyTank et = ets.get(i);
			if (et.isLive) {
				// draw enemy tank's bullet
				this.drawTank(et.getX(), et.getY(), g, et.getDirect(), 0);
				for (int j = 0; j < et.ss.size(); j++) {
					// get bullet
					Shot enemyShot = et.ss.get(j);
					if (enemyShot.isLive) {
						g.draw3DRect(enemyShot.x, enemyShot.y, 1, 1, false);
					} else {
						et.ss.remove(enemyShot);
					}
				}
			}
		}
	}

	// decide enemy tank's shot if hit me
	public void hitMe() {
		for (int i = 0; i < this.ets.size(); i++) {
			EnemyTank et = ets.get(i);
			for (int j = 0; j < et.ss.size(); j++) {
				Shot enemyShot = et.ss.get(j);
				if (hero.isLive) {
					this.hitTank(enemyShot, hero);
				}
			}
		}
	}

	// decide myShot if hit enemy tank
	public void hitEnemyTank() {
		// hit enemy tank or not
		for (int i = 0; i < hero.ss.size(); i++) {
			// get bullet
			Shot myShot = hero.ss.get(i);
			// check bullet is live
			if (myShot.isLive) {
				for (int j = 0; j < ets.size(); j++) {
					EnemyTank et = ets.get(j);
					if (et.isLive) {
						if (this.hitTank(myShot, et)) {
							apw = new AePlayWave(
									"sound/explosion.wav");
							apw.start();
							Recorder.reduceEnNum();
							Recorder.addEnNumRec();
						}
					}
				}
			}
		}
	}

	// decide if bullet hit tank
	public boolean hitTank(Shot s, Tank et) {
		boolean b2 = false;
		// decide tank's direct
		switch (et.direct) {
		case 0:
		case 2:
			if (s.x > et.x && s.x < et.x + 20 && s.y > et.y && s.y < et.y + 30) {
				s.isLive = false;
				et.isLive = false;
				b2 = true;
				Bomb b = new Bomb(et.x, et.y);
				bombs.add(b);
			}
			break;
		case 1:
		case 3:
			if (s.x > et.x && s.x < et.x + 30 && s.y > et.y && s.y < et.y + 20) {
				s.isLive = false;
				et.isLive = false;
				b2 = true;
				Bomb b = new Bomb(et.x, et.y);
				bombs.add(b);
			}
			break;
		}
		return b2;
	}

	// draw a tank
	public void drawTank(int x, int y, Graphics g, int direct, int type) {
		switch (type) {
		case 0:
			g.setColor(Color.cyan);
			break;
		case 1:
			g.setColor(Color.yellow);
			break;
		}

		switch (direct) {
		case 0: // up
			g.fill3DRect(x, y, 5, 30, false);
			g.fill3DRect(x + 15, y, 5, 30, false);
			g.fill3DRect(x + 5, y + 5, 10, 20, false);
			g.fillOval(x + 5, y + 10, 10, 10);
			g.drawLine(x + 10, y + 15, x + 10, y);
			break;
		case 1: // right
			g.fill3DRect(x, y, 30, 5, false);
			g.fill3DRect(x, y + 15, 30, 5, false);
			g.fill3DRect(x + 5, y + 5, 20, 10, false);
			g.fillOval(x + 10, y + 5, 10, 10);
			g.drawLine(x + 15, y + 10, x + 30, y + 10);
			break;
		case 2: // down
			g.fill3DRect(x, y, 5, 30, false);
			g.fill3DRect(x + 15, y, 5, 30, false);
			g.fill3DRect(x + 5, y + 5, 10, 20, false);
			g.fillOval(x + 5, y + 10, 10, 10);
			g.drawLine(x + 10, y + 15, x + 10, y + 30);
			break;
		case 3: // left
			g.fill3DRect(x, y, 30, 5, false);
			g.fill3DRect(x, y + 15, 30, 5, false);
			g.fill3DRect(x + 5, y + 5, 20, 10, false);
			g.fillOval(x + 10, y + 5, 10, 10);
			g.drawLine(x + 15, y + 10, x, y + 10);
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// w->up, s->down, a->left, d->right
		// set hero's direct
		if (e.getKeyCode() == KeyEvent.VK_W) {
			this.hero.setDirect(0);
			this.hero.moveUp();
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			this.hero.setDirect(1);
			this.hero.moveRight();
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			this.hero.setDirect(2);
			this.hero.moveDown();
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			this.hero.setDirect(3);
			this.hero.moveLeft();
		}

		// decide if user press key 'J'
		if (e.getKeyCode() == KeyEvent.VK_J) {
			apw = new AePlayWave("sound/fire.wav");
			apw.start();
			if (this.hero.ss.size() < 5) {
				this.hero.shotEnemy();
			}
		}

		this.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// repaint the bullet every 0.1s
		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.hitEnemyTank();
			this.hitMe();

			// redraw
			this.repaint();
		}
	}
}