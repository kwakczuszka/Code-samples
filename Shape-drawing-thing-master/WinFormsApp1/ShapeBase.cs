using System.Windows.Forms;
using System.Drawing.Drawing2D;
public static class Global
{
    public const Int16 _PREC = 200;

}

namespace Bruhment
{

    public class Packet
    {
        public Packet(Graphics gr, Pen pn, Panel pnl, Brush brsh)
        {
            graphics = gr;
            pen = pn;
            panel = pnl;
            brush = brsh;
        }
        public Graphics graphics;
        public Pen pen;
        public Panel panel;
        public Brush brush;

    }
    public abstract class Shape
    {
        public void Draw() {
            pck.graphics.DrawPolygon(pck.pen, points);
        }

        public void Clear()
        {
            pck.graphics.Clear(Color.White);
        }
        public Packet pck;
        public System.Drawing.PointF[] points;
        
        public Shape() {
            points = new System.Drawing.PointF[Global._PREC];
        }
    }

    public class Rectangle : Shape {
        public Rectangle(Packet pck, float a, float b)
        {
            a /= 2;
            b /= 2;
            int x = pck.panel.Width / 2;
            int y = pck.panel.Height / 2;
            this.points = new PointF[4];
            this.points[0] = new PointF(x + a, y + b);
            this.points[1] = new PointF(x + a, y - b);
            this.points[2] = new PointF(x - a, y - b);
            this.points[3] = new PointF(x - a, y + b);
            this.pck = pck;
        }
    }
    
    public class Square : Shape {
        public Square(Packet pck, float a)
        {
            a /= 2;
            int x = pck.panel.Width / 2;
            int y = pck.panel.Height / 2;
            points = new PointF[4];
            points[0] = new PointF(x + a, y + a);
            points[1] = new PointF(x + a, y - a);
            points[2] = new PointF(x - a, y - a);
            points[3] = new PointF(x - a, y + a);
            this.pck = pck;
        }
    }

    public class Circle : Shape {
        public int radius;
        public Circle(Packet pck, int r)
        {
            radius = r;
            this.pck = pck;
        }

        public new void Draw()
        {
            System.Drawing.Rectangle rect = new System.Drawing.Rectangle();
            rect.Width = 2 * radius;
            rect.Height = 2 * radius;
            pck.graphics.DrawEllipse(pck.pen, rect);
        }
    }

    public class Triangle : Shape { 
    
    }

    public class Parallelogram : Shape { 
    
    }
}