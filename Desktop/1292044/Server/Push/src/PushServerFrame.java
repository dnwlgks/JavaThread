import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.*;

import javax.swing.*;

public class PushServerFrame extends JFrame{
	private int threadCount=0;
	private int deliveredCount=0;
	private MyTextField text=new MyTextField(10);
	private JLabel clientCountLabel=new JLabel("0");
	private JLabel deliveredCountLabel=new JLabel("0");
	private JTextArea clientList=new JTextArea(7,30);
	public  PushServerFrame(){
		super("서버");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//창을 닫을때 프로그램을 죽이는 것
		setSize(350,300);
		Container c=getContentPane();
		c.setLayout(new FlowLayout());
		c.add(new JLabel("텍스트"));
		c.add(text);
		c.add(new JLabel("접속자수"));
		c.add(clientCountLabel);
		c.add(new JLabel("전송한수"));
		c.add(deliveredCountLabel);
		c.add(new JScrollPane(clientList));
		setVisible(true);
		
		ServerThread th=new ServerThread();
		th.start();
	}
	class ServerThread extends Thread{
		public void run(){
			ServerSocket listener=null;
			Socket socket=null;
			try {
				listener =new ServerSocket(9999);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while(true){
				try {
					socket=listener.accept();
					clientList.append("client 접속\n");
					ServiceThread th=new ServiceThread(socket);
					th.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	class ServiceThread extends Thread{
		private Socket socket=null;
		public ServiceThread(Socket socket){
			this.socket=socket;
			threadCount++;
			clientCountLabel.setText(Integer.toString(threadCount));
			text.increaseDeliveredCount();
		}
		
		public void run(){
			try {
				BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				while(true){
					String msg=text.get();
					clientList.append(msg+"\n");
					out.write(msg+"\n");
					out.flush();
				}
				
			}catch (java.net.SocketException e) {
				//상대 소켓이 닫힌 경우
				threadCount--;
				clientCountLabel.setText(Integer.toString(threadCount));
				text.decreaseDeliveredCount();
				return;
			}
			
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	class MyTextField extends JTextField{
		public MyTextField(int n){
			super(n);//위에 클래스에게 넘겨주는 것
			this.addActionListener(new ActionListener(){//<Enter>키가 들어온다.

				@Override
				public void actionPerformed(ActionEvent e) {
					put();
					
				}
				
			});
		}
		public void clearDeliveredCount(){
			deliveredCount=0;
			deliveredCountLabel.setText("0");
		}
		
		public void increaseDeliveredCount(){
			deliveredCount++;
			deliveredCountLabel.setText(Integer.toString(deliveredCount));
		}
		
		public void decreaseDeliveredCount(){
			deliveredCount--;
			deliveredCountLabel.setText(Integer.toString(deliveredCount));
		}
		
		public synchronized void put(){
			if(deliveredCount!=threadCount){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}///wait for Notify() call!!
			}
			clearDeliveredCount();
			this.notifyAll();//wait 함수를 수행하고 있는 모든 클라이언트들을 깨운다.
		}
		
		public synchronized String get(){//충돌이 일어나지 않도록 싱크로 나이즈   줄서라!! 수행하고 싶으면 막아놔
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			increaseDeliveredCount();
			if(deliveredCount==threadCount){
				notify();
			}
			return this.getText();
		}
	}
	
	
	public static void main(String[] args) {
		//기다리는 cunsumer가 있으면 들어오면 깨워주고 비어지길 기다리는 producer이 잇으면 비웠다고 깨워줘야한다.(wait-notify)
		new PushServerFrame();
		
	}

}
