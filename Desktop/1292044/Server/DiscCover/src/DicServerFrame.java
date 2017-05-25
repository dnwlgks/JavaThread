import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DicServerFrame extends JFrame {
	private JTextField eng = new JTextField(10);
	private JTextField kor = new JTextField(10);
	private JButton saveBtn = new JButton("저장");
	private JButton searchBtn = new JButton("검색");
	private JTextArea ta = new JTextArea(7, 40);
	private HashMap<String, String> dicMap = new HashMap<String, String>();

	public DicServerFrame() {
		setSize(300, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = getContentPane();
		c.setLayout(new FlowLayout());
		c.add(new JLabel("영어"));
		c.add(eng);
		c.add(new JLabel("한글"));
		c.add(kor);
		c.add(saveBtn);
		c.add(searchBtn);
		c.add(new JScrollPane(ta));

		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String engWord = eng.getText();
				String korWord = kor.getText();
				dicMap.put(engWord, korWord);
			}
		});

		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String engWord = eng.getText();
				String korWord = dicMap.get(engWord);
				if (korWord == null)
					kor.setText("없는 단어");
				else
					kor.setText(korWord);
			}
		});

		setVisible(true);
		
		new ServerThread().start();
	}

	class ServerThread extends Thread {
		public void run() {
			ServerSocket listener = null;
			Socket socket = null;
			try {
				listener = new ServerSocket(9999);
				while (true) {
					socket = listener.accept();
					ServiceThread th = new ServiceThread(socket);
					th.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	class ServiceThread extends Thread {
		Socket socket;

		public ServiceThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				while (true) {
					String engWord = in.readLine();
					String korWord = dicMap.get(engWord);
					if (korWord == null)
						korWord = "없음";
					out.write(korWord + "\n");
					out.flush();
					ta.append(engWord);
					int pos = ta.getText().length();
					ta.setCaretPosition(pos);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new DicServerFrame();

	}

}
 