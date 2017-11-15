import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ParticleView extends JFrame
{
	public ParticlePanel panel;
	private int windowX;
	private int windowY;
	
	public ParticleView(String WindowTitle)
	{
		this.setTitle(WindowTitle);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//this.setUndecorated(true);
		//this.setSize(800+16, 600+39);
		
		this.setVisible(true);
		
		this.windowX = this.getWidth();
		this.windowY = this.getHeight();
		
		this.panel = new ParticlePanel(this.windowX, this.windowY);
		this.getContentPane().add(this.panel);
		this.setIgnoreRepaint(true);
	}
	
	public void PaintParticleView(int n, float[]X, float[]Y)
	{
		if(panel.isRendering.compareAndSet(false, true))
		{
			return;
		}
		
		panel.n = n;
		panel.X = X;
		panel.Y = Y;

		this.repaint(0);
		
	}
}


@SuppressWarnings("serial")
class ParticlePanel extends JPanel
{
	//Color bgColor = new Color(32,16,32);
	int windowX;
	int windowY;
	int centerXoffset;
	int centerYoffset;
	//Color partColor = new Color(255,128,114,16);
	//Color partColor = new Color(255,255,255,32);
	
	float[] X;
	float[] Y;
	
	int n;
	AtomicBoolean isRendering = new AtomicBoolean(false);
	
	protected ParticlePanel(int windowX, int windowY)
	{
		this.windowX = Math.max(windowX,0);
		this.windowY = Math.max(windowY,0);
		this.centerXoffset = (int) (this.windowX/2.0);
		this.centerYoffset = (int) (this.windowY/2.0);
		this.setIgnoreRepaint(true);
	}
	
	public void paintComponent(Graphics g) 
	{
		
		g.setColor(Color.BLACK);
		g.fillRect(0,0,this.windowX,this.windowY);
		
		g.setColor(Color.WHITE);
		
		for (int i = 0; i < n; i++)
		{
			g.fillRect((int)(X[i]+this.centerXoffset), (int)(Y[i]+this.centerYoffset), 1, 1);
		}
		
		this.isRendering.set(false);

	}
}
