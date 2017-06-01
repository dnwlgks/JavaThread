import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.*;

import javax.swing.*;

public class PushClient extends JFrame {
	private Socket socket=null;
	private Receiver text=new Receiver();
	private BufferedReader in=null;
	private JButton startButton=new JButton("시작");
	public PushClient(){
		super("클라이언트");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//창을 닫을때 프로그램을 죽이는 것
		setSize(350,300);
		Container c=getContentPane();
		c.setLayout(new BorderLayout());
		c.add(new JScrollPane(text), BorderLayout.CENTER);
		c.add(startButton, BorderLayout.SOUTH);
		startButton.addActionListener(new MyActionListener());
		
		setVisible(true);
	}
	public boolean setup(){
		try {
			socket=new Socket("localhost",9999);
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	class MyActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean b=setup();
			if(b==true){
				Thread th=new Thread(text);
				th.start();
				((JButton)e.getSource()).setEnabled(false);
			}
		}
	}
	
	class Receiver extends JTextArea implements Runnable{//쓰레드 역활을 겸하는 클래스/
		public Receiver(){
			//this.setEditable(false);
		}
		public void run(){
			while(true){
				try {
					String msg=in.readLine();
					this.append(msg+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) {
		//기다리는 cunsumer가 있으면 들어오면 깨워주고 비어지길 기다리는 producer이 잇으면 비웠다고 깨워줘야한다.(wait-notify)
		new PushClient();
	}
}
