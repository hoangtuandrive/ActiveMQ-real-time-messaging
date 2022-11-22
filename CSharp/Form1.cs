using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Apache.NMS.ActiveMQ.Commands;
namespace Tuan4_Message
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            init();
        }

        private void btnSend_Click(object sender, EventArgs e)
        {
            IConnectionFactory factory = new ConnectionFactory("tcp://localhost:61616");
            IConnection con = factory.CreateConnection("admin", "admin");
            con.Start();//nối tới MOM
            ISession session = con.CreateSession(AcknowledgementMode.AutoAcknowledge);
            ActiveMQTopic destination = new ActiveMQTopic("thanthidet");
            IMessageProducer producer = session.CreateProducer(destination);

            String m ="Anh: "+ txtMessage.Text;

            ActiveMQTextMessage tmsg = new ActiveMQTextMessage(m);
            producer.Send(tmsg);
            //Console.WriteLine("press any key to continue");
            //Console.ReadKey();
        }

        void init()
        {
            //Console.WriteLine("press enter to exit");
            IConnectionFactory factory = new ConnectionFactory("tcp://localhost:61616");
            IConnection con = factory.CreateConnection("admin", "admin");
            con.Start();//nối tới MOM
            ISession session = con.CreateSession(AcknowledgementMode.AutoAcknowledge);
            ActiveMQTopic destination = new ActiveMQTopic("thanthidet");
            IMessageConsumer producer = session.CreateConsumer(destination);
            producer.Listener += Producer_Listener;

            //String m = producer.ToString();
            //richTextBoxMessage.AppendText(m + "\n");
            //Console.WriteLine(m);
            //Console.ReadLine();
            
        }
       
        private void Producer_Listener(IMessage message)
        {
            if (message is ActiveMQTextMessage)
            {
               
                ActiveMQTextMessage tmsg = message as ActiveMQTextMessage;
                String txt = tmsg.Text;
                //Console.WriteLine(txt);
                SetText(""+txt);
            }
        }

        delegate void SetTextCallback(string text);

        private void SetText(string text)
        {
            if (this.richTextBoxMessage.InvokeRequired)
            {
                SetTextCallback callback = new SetTextCallback(SetText);
                this.Invoke(callback, new object[] { text });
            }
            else
            {
                this.richTextBoxMessage.AppendText(text + "\n");
            }
        }
    }
}
