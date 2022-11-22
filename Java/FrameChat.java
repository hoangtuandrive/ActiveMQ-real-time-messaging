package tuan5_message;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.apache.log4j.BasicConfigurator;

public class FrameChat extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea txtMess;
	private JButton btnSend;
	private JTextArea txtChat;

	public FrameChat() throws Exception {
		setTitle("Chat");
		setSize(400, 420);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		JPanel panel_top = new JPanel();
		panel_top.setLayout(null);
		panel_top.setBounds(10, 10, 365, 300);
		panel_top.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Nội dung xem"));
		add(panel_top);

		txtChat = new JTextArea(60, 70);
		txtChat.setLayout(null);
		JScrollPane scroll = new JScrollPane(txtChat);
		scroll.setBounds(10, 20, 340, 270);
		panel_top.add(scroll);

		JPanel panel_bot = new JPanel();
		panel_bot.setLayout(null);
		panel_bot.setBounds(10, 310, 365, 50);
		add(panel_bot);

		JLabel lblSend = new JLabel("Enter text:");
		lblSend.setLayout(null);
		lblSend.setBounds(10, 10, 100, 20);
		panel_bot.add(lblSend);

		txtMess = new JTextArea();
		txtMess.setLayout(null);
		txtMess.setBounds(80, 10, 200, 40);
		txtMess.setBorder(BorderFactory.createLineBorder(Color.gray));
		panel_bot.add(txtMess);

		btnSend = new JButton("Send");
		btnSend.setLayout(null);
		btnSend.setBounds(290, 10, 70, 30);
		panel_bot.add(btnSend);

		btnSend.addActionListener(this);
		subcriberMess();
	}

	public static void main(String[] args) throws Exception {
		new FrameChat().setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o.equals(btnSend)) {
			String message = txtMess.getText();
			try {
				publisherMess("Em: "+message);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			txtMess.setText("");
			txtMess.requestFocus();
		}
	}

	public void publisherMess(String message) throws Exception {
		// thiết lập môi trường cho JMS logging
		BasicConfigurator.configure();
		// thiết lập môi trường cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// tạo context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		// tạo connection
		Connection con = factory.createConnection("admin", "admin");
		// nối đến MOM
		con.start();
		// tạo session
		Session session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
		Destination destination = (Destination) ctx.lookup("dynamicTopics/thanthidet");
		// tạo producer
		MessageProducer producer = session.createProducer(destination);
		// Tạo 1 message
		Message msg = session.createTextMessage(message);
		// gửi
		producer.send(msg);
		// shutdown connection
		session.close();
		con.close();
	}

	public void subcriberMess() throws Exception {
		// thiết lập môi trường cho JMS
		BasicConfigurator.configure();
		// thiết lập môi trường cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// tạo context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		// tạo connection
		Connection con = factory.createConnection("admin", "admin");
		// nối đến MOM
		con.start();
		// tạo session
		Session session = con.createSession(/* transaction */false, /* ACK */Session.CLIENT_ACKNOWLEDGE);
		// tạo consumer
		Destination destination = (Destination) ctx.lookup("dynamicTopics/thanthidet");
		MessageConsumer receiver = session.createConsumer(destination);
		// receiver.receive();//blocked method
		// Cho receiver lắng nghe trên queue, chừng có message thì notify
		receiver.setMessageListener(new MessageListener() {
			// @Override
			// có message đến queue, phương thức này được thực thi
			public void onMessage(Message msg) {// msg là message nhận được
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();
						txtChat.append(txt+"\n");
						msg.acknowledge();// gửi tín hiệu ack
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
