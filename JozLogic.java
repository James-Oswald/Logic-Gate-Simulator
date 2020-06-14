

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
//import javax.script.ScriptException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.Dimension;
//import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class JozLogic
{
	public static class Coord
	{
		public int x, y;
		
		public Coord(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
	
	public abstract class CanvasObject
	{
		protected Coord pos, dragPoint;
		protected int width, height;
		protected boolean drag;
		
		public abstract void draw(Graphics g);
		public abstract void update() throws Exception;
		public abstract void move(Coord pos);
		
		protected void super2(Coord pos, int width, int height)
		{
			this.pos = pos;
			this.width = width;
			this.height = height;
			drag = false;
		}
		
		public void setPos(Coord pos)
		{
			this.pos = pos;
		}
		
		public Coord getDiag()
		{
			return new Coord(pos.x + width, pos.y + height);
		}
		
		public Coord getPos()
		{
			return pos;
		}
		
		public void setDrag(boolean drag)
		{
			this.drag = drag;
		}
		
		public boolean getDrag()
		{
			return drag;
		}
		
		public void setDragPoint(Coord dp)
		{
			dragPoint = dp;
		}
		
		public Coord getDragPoint()
		{
			return dragPoint;
		}
		
	}
	
	public enum Mode
	{
		IN,
		OUT
	}
	
	public class Node extends CanvasObject
	{
		//private boolean  bound
		private boolean state, selState;
		private Coord selCoord;
		private Mode mode;
		private ArrayList <Node> linked;
		private CanvasObject parrent;
		private String name, logic;
		private final int SIZE = 10;
		
		public Node(CanvasObject parrent, String name, Coord pos, Mode mode, String logic)
		{
			this(pos, mode);
			this.parrent = parrent;
			this.name = name;
			this.logic = logic;
		}
		
		public Node(Coord pos, Mode mode)
		{
			super2(pos, SIZE, SIZE);
			this.mode = mode;
			this.linked = new ArrayList <Node> ();
			selState = false;
		}
		
		public void bind(Node n) throws Exception
		{
			if(n.getMode() != mode)
			{
				linked.add(n);
				boolean aBound = false; //already bound
				for(int i = 0; i < n.getLinked().size(); i++)
				{
					if(n.getLinked().get(i) == this)
					{
						aBound = true;
					}
				}
				if(!aBound)
				{
					n.bind(this);
				}
				update();
			}
			else
			{
				throw new Exception("Can't link nodes of the same IO Mode");
			}
		}
		
		public void update() throws Exception
		{
			if(mode == Mode.IN)
			{
				boolean combineState = false;
				for(int i = 0; i < linked.size(); i++)
				{
					if(linked.get(i).getState())
					{
						combineState = true;
					}
				}
				state = combineState;
				if(parrent != null)
				{
					parrent.update();
				}
			}
			else if(mode == Mode.OUT)
			{
				for(int i = 0; i < linked.size(); i++)
				{
					linked.get(i).update();
				}
			}
			else
			{
				throw new Exception("Node has no IO Mode");
			}
		}
		
		public void draw(Graphics g)
		{
			g.setColor(state ? Color.red : Color.black);
			g.drawOval(pos.x, pos.y, width, height);
			if(mode == Mode.OUT)
			{
				Coord lpos; 
				for(int i = 0; i < linked.size(); i++)
				{
					lpos = linked.get(i).getPos();
					g.drawLine(pos.x + SIZE / 2, pos.y + SIZE / 2, lpos.x + SIZE / 2, lpos.y + SIZE / 2);
				}
			}
			if(selState)
			{
				if(selCoord != null)
				{
					g.drawLine(pos.x + SIZE / 2, pos.y + SIZE / 2, selCoord.x + SIZE / 2, selCoord.y + SIZE / 2);
				}
			}
		}
		
		public void beginSelection()
		{
			selState = true;
		}
		
		public void endSelection(Node link) throws Exception
		{
			selState = false;
			selCoord = null;
			bind(link);
		}
		
		public void endSelection()
		{
			selState = false;
		}
		
		public void move(Coord pos)
		{
			setPos(pos);
		}
		
		public Mode getMode()
		{
			return mode;
		}
		
		public String getLogic() throws Exception
		{
			if(mode == Mode.OUT)
			{
				return logic;
			}
			else
			{
				throw new Exception("Mode is IN or not declared");
			}
		}
		
		public ArrayList <Node> getLinked()
		{
			return linked; 
		}
		
		public Coord getPos()
		{
			return pos;
		}
		
		public String getName()
		{
			return name;
		}
		
		public boolean getSelState()
		{
			return selState;
		}
		
		public boolean getState()
		{
			return state;
		}
		
		public void setState(boolean state) throws Exception
		{
			this.state = state;
			this.update();
		}
		
		public void setSelCoord(Coord selCoord)
		{
			this.selCoord = selCoord;
		}
	}
	
	public class LogicObject extends CanvasObject
	{
		private ArrayList <Node> nodes;
		private ArrayList <Integer> outs, ins;
		private String name; //, path;
		private final int TOS = 1;
		private BufferedImage img;
		
		public LogicObject(Coord pos, String path) throws Exception
		{
			img = ImageIO.read(new File(path + "\\pic.png"));
			super2(pos, img.getWidth(), img.getHeight());
			nodes = new ArrayList <Node> ();
			outs = new ArrayList <Integer> ();
			ins = new ArrayList <Integer> ();
			//this.path = path; 
			img = ImageIO.read(new File(path + "\\pic.png"));
			Scanner s = new Scanner(new File(path + "\\data.txt"));
			ArrayList <String> data = new ArrayList <String> ();
			while (s.hasNext())
			{
				data.add(s.nextLine());
			}
			name = data.get(0);
			Coord loc;
			String logic;
			RefString forward = new RefString();
			Mode mode;
			for(int i = TOS; i < data.size(); i++)
			{
				forward.val = data.get(i);
				mode = clip(forward, " ").equals("IN") ? Mode.IN : Mode.OUT;
				name = clip(forward, " ");
				if(mode == Mode.IN)
				{
					loc = new Coord(Integer.parseInt(clip(forward, " ")) + pos.x, Integer.parseInt(clip(forward, ";")) + pos.y);
					logic = "";
					ins.add(i - TOS);
				}
				else if(mode == Mode.OUT)
				{
					loc = new Coord(Integer.parseInt(clip(forward, " ")) + pos.x, Integer.parseInt(clip(forward, " ")) + pos.y);
					logic = clip(forward, ";");
					outs.add(i - TOS);
				}
				else
				{
					throw new Exception("No IO Mode");
				}
				nodes.add(new Node(this, name, loc, mode, logic));
			}
			update();
		}
		
		public class RefString
		{
			public String val;
			
			public RefString()
			{
				val = "";
			}
		}
		
		public String clip(RefString s, String index)
		{
			String rv = s.val.substring(0, s.val.indexOf(index));
			s.val = s.val.substring(s.val.indexOf(index) + 1);
			return rv;
		}
		
		public void update() throws Exception
		{
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine se = sem.getEngineByName("JavaScript");
			String logic, curName;
			//boolean done = false;
			for(int i = 0; i < outs.size(); i++)
			{
				logic = nodes.get(outs.get(i)).getLogic();
				for(int j = 0; j < ins.size(); j++)
				{
					curName = nodes.get(ins.get(j)).getName();
					if(logic.indexOf(curName) >= 0)
					{
						DEBUG(nodes.get(ins.get(j)).getName() + " : " + String.valueOf(nodes.get(ins.get(j)).getState()));
						logic = logic.replaceAll(curName, (nodes.get(ins.get(j)).getState() ? "true" : "false"));
						DEBUG(logic);
					}
				}
				DEBUG(String.valueOf((boolean)se.eval(logic)));
				nodes.get(outs.get(i)).setState((boolean)se.eval(logic));
			}
		}
		
		public void draw(Graphics g)
		{
			g.drawImage(img, pos.x, pos.y, null);
			for(int i = 0; i < nodes.size(); i++)
			{
				nodes.get(i).draw(g);
			}
		}
		
		public void move(Coord npos)
		{
			Node curNode;
			for(int i = 0; i < nodes.size(); i++)
			{
				curNode = nodes.get(i);
				curNode.move(new Coord(npos.x + (curNode.getPos().x - pos.x), npos.y + (curNode.getPos().y - pos.y)));
			}
			setPos(npos);
		}
		
		public ArrayList <Node> getNodes()
		{
			return nodes;
		}
		
		public Node getNodeByName(String name) throws Exception
		{
			for(int i = 0; i < nodes.size(); i++)
			{
				if(nodes.get(i).getName().equals(name))
				{
					return nodes.get(i);
				}
			}
			throw new Exception("name not found!");
		}
		
		public void setNodeStateByName(String name, boolean state) throws Exception
		{
			for(int i = 0; i < nodes.size(); i++)
			{
				if(nodes.get(i).getName().equals(name))
				{
					nodes.get(i).setState(state);
				}
			}
			throw new Exception("name not found!");
		}
		
		public BufferedImage getImg()
		{
			return img;
		}
	}
	
	public class LogicCanvas extends JPanel implements MouseListener, MouseMotionListener
	{
		private static final long serialVersionUID = 2569011249533893137L;

		private ActiveArrayList objects;
		private ArrayList <Node> nodes;
		private ArrayList <CanvasObject> all;
		//private String path;
		
		public LogicCanvas(String path) throws Exception
		{
			super();
			setBackground(Color.white);
			addMouseListener(this);
			addMouseMotionListener(this);
			//this.path = path;
			objects = new ActiveArrayList();
			nodes = new ArrayList <Node> ();
			all = new ArrayList <CanvasObject> ();
		}
		
		public void add(CanvasObject obj)
		{
			objects.add(obj);
		}
		
		@Override public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			for(int i = 0; i < all.size(); i++)
			{
				all.get(i).draw(g);
			}
		}
		
		public class ActiveArrayList extends ArrayList <CanvasObject>
		{
			private static final long serialVersionUID = -3639863101354244055L;

			public ActiveArrayList()
			{
				super();
			}
			
			private boolean update()
			{
				try
				{
					all.clear();
					nodes.clear();
					for(int i = 0; i < objects.size(); i++)
					{
						if(get(i) instanceof LogicObject)
						{
							all.addAll(((LogicObject)get(i)).getNodes());
							nodes.addAll(((LogicObject)get(i)).getNodes());
						}
						else if(get(i) instanceof Node)
						{
							nodes.add((Node)get(i));
						}
					}
					all.addAll(this);
					return true;
				}
				catch(Exception e)
				{
					return false;
				}
			}
			
			@Override public boolean add(CanvasObject in)
			{
				super.add(in);
				return update();
			}
			
			@Override public boolean remove(Object in)
			{
				super.remove(in);
				return update();
			}
			
			@Override public void clear()
			{
				super.clear();
				nodes.clear();
				all.clear();
			}
		}
		
		public void mousePressed(MouseEvent e) 
		{
			if(e.getButton() == 1)
			{
				Coord pos, diag;
				CanvasObject curObject;
				for(int i = 0; i < objects.size(); i++)
				{
					curObject = objects.get(i);
					pos = curObject.getPos();
					diag = curObject.getDiag();
					if(e.getX() >= pos.x && e.getY() >= pos.y && e.getX() <= diag.x && e.getY() <= diag.y)
					{
						curObject.setDragPoint(new Coord(e.getX() - pos.x, e.getY() - pos.y));
						curObject.setDrag(true);
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) 
		{
			for(int i = 0; i < objects.size(); i++)
			{
				if(objects.get(i).getDrag())
				{
					objects.get(i).setDrag(false);
				}
			}
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.getButton() == 1)
			{
				boolean nia = true;
				Node curNode, selNode = null;
				Coord pos, diag;
				CanvasObject curObject;
				//ClickedObjectSearch:
				for(int i = 0; i < all.size(); i++)
				{
					curObject = all.get(i);
					pos = curObject.getPos();
					diag = curObject.getDiag();
					if(e.getX() >= pos.x && e.getY() >= pos.y && e.getX() <= diag.x && e.getY() <= diag.y)
					{
						if(curObject instanceof Node)
						{
							nia = false;
							curNode = (Node)curObject;
							DEBUG(curNode.getName() != null ? curNode.getName() : "null");
							findSelNode:for(int j = 0; j < nodes.size(); j++)
							{
								if(nodes.get(j).getSelState())
								{
									selNode = nodes.get(j);
									break findSelNode;
								}
							}
							if(selNode != null)
							{
								try
								{
									if(selNode.getLinked().indexOf(curNode) < 0)
									{
										selNode.endSelection(curNode);
										repaint();
									}
								}
								catch(Exception ex)
								{
									DEBUG(ex.getMessage());
								}
							}
							else
							{
								curNode.beginSelection();
							}
						}
						else if(curObject instanceof LogicObject)
						{
							//Future actions for Clicked Logic Objects
						}
					}
				}
				if(nia)
				{
					for(int j = 0; j < nodes.size(); j++)
					{
						nodes.get(j).endSelection();
						repaint();
					}
				}
			}
		}
		
		public void mouseMoved(MouseEvent e)
		{
			for(int i = 0; i < nodes.size(); i++)
			{
				if(nodes.get(i).getSelState())
				{
					nodes.get(i).setSelCoord(new Coord(e.getX(), e.getY()));
					repaint();
				}
			}
		}
		
		public void mouseDragged(MouseEvent e)
		{
			Coord dragPoint;
			for(int i = 0; i < objects.size(); i++)
			{
				if(objects.get(i).getDrag())
				{
					dragPoint = objects.get(i).getDragPoint();
					objects.get(i).move(new Coord(e.getX() - dragPoint.x, e.getY() - dragPoint.y));
					repaint();
				}
			}
		}
		
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
	}
	
	public static class MenuBar extends JPanel
	{
		private static final long serialVersionUID = -5260156892650698151L;

		public MenuBar()
		{
			super();
			setLayout(new FlowLayout());
			setBackground(new Color(200, 200, 200));
		}
	}
	
	public class SidePanel extends JPanel
	{
		private static final long serialVersionUID = 6096484822603856170L;
		
		private final String LOP = "/resources/LogicObjects/";
		private MainDisplay parent;
		private JList <String> listObject;
		private ArrayList <String> list;
		
		public SidePanel(MainDisplay parent)
		{
			super(); 
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(new Color(150, 150, 150));
			this.parent = parent;
			list = new ArrayList <String> ();
			updateList();
			listObject = new JList <String> (list.toArray(new String[list.size()]));
			JScrollPane scroll = new JScrollPane(listObject);
			scroll.setPreferredSize(new Dimension(100, 150));
			add(scroll);
			JButton addItem = new JButton("Add Selected");
			addItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(listObject.getSelectedValue() != null)
					{
						LogicCanvas lc = parent.getLogicCanvas();
						try
						{
							lc.add(new LogicObject(new Coord(lc.getWidth() / 2, lc.getHeight() / 2), parent.getCurDir() + LOP + listObject.getSelectedValue()));
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
						lc.repaint();
					}
				}
			});
			add(addItem);
		}
		
		public void updateList()
		{
			list.clear();
			File rootBin = new File(parent.getCurDir() + LOP);
			File[] objects = rootBin.listFiles();
			for(int i = 0; i < objects.length; i++)
			{
				list.add(objects[i].getName());
			}
		}
	}
	
	public class MainDisplay extends JPanel
	{
		private static final long serialVersionUID = -426551985539199108L;

		private final String CD;
		private SidePanel sp;
		private LogicCanvas lc;
		
		public MainDisplay(String CD) throws Exception
		{
			this.CD = CD;
			setLayout(new BorderLayout());
			sp = new SidePanel(this);
			add(sp, BorderLayout.WEST);
			lc = new LogicCanvas(CD);
			add(lc, BorderLayout.CENTER);
		}
		
		public LogicCanvas getLogicCanvas()
		{
			return lc;
		}
		
		public String getCurDir()
		{
			return CD;
		}
	}
	
	public static void main(String[] args)
	{
		JozLogic obj = new JozLogic();
		try 
		{
			obj.run();
		}
		catch(Exception e)
		{
			DEBUG(e.getMessage());
			e.printStackTrace();
		}
	} 

	public void run() throws Exception
	{
		JFrame window = new JFrame("JozLogic");
		window.add(new MainDisplay(System.getProperty("user.dir")));
		window.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		window.setIconImage(ImageIO.read(new File("resources/lex.png")));
		window.setSize(1000, 1000);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	
	public static void DEBUG(String s)
	{
		System.out.println(s);
	}
}