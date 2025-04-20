using Bruhment;
using System;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Text.Json.Serialization;
using System.Windows.Forms;
using WinFormsApp1;

namespace WinFormsApp1
{
    public partial class Form1 : Form
    {
        private Packet pack;
        private Graphics graphics;
        private Pen pen;
        private Brush brush;
        private System.Windows.Forms.Timer timer;

        public Form1()
        {
            InitializeComponent();

            graphics = panel1.CreateGraphics();
            pen = new Pen(Color.Red, 2);
            brush = new SolidBrush(Color.Red);
            timer = new System.Windows.Forms.Timer();
            timer.Interval = 100;
            timer.Tick += Timer_Tick;
            pack = new Packet(graphics, pen, panel1, brush);
            timer.Start();

        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            panel1.BackColor = Color.White;
            //            panel1.BackColor = Color.White;
            //            Bruhment.Rectangle rect = new Bruhment.Rectangle(pack, 400, 200);
            //            rect.Draw();

        }

        /*public System.Drawing.PointF[] rotate(System.Drawing.PointF[] pts, int angle)
        {
            for (int i = 0; i < pts.Length; i++)
            {
                pts[i].X += panel1.Width / 2;
                pts[i].Y += panel1.Height / 2;
            }
            float phi = (float)angle * (float)Math.PI / 180;
            for (int i = 0; i < panel1.Width; i++)
            {
                float x = pts[i].X;
                float y = pts[i].Y;
                pts[i].X = (-y) * (float)Math.Cos(phi) + (x) * (float)Math.Sin(phi);
                pts[i].Y = y * (float)Math.Sin(phi) + y * (float)Math.Cos(phi);
            }

            return pts;
        }*/


        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void button2_Click(object sender, EventArgs e)
        {
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {


        }

        private void button1_Click(object sender, EventArgs e)
        {
            int a, b;
            a = int.Parse(textBox1.Text);
            b = int.Parse(textBox2.Text);
            Bruhment.Circle rect = new Bruhment.Circle(pack, a);
            rect.Draw();
        }

        private void button3_Click(object sender, EventArgs e)
        {
            pack.panel.Refresh();

        }

        private void Form1_Load(object sender, EventArgs e)
        {

        }

        private void comboBox1_SelectedIndexChanged_1(object sender, EventArgs e)
        {

        }
    }


}