import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.color.*;
import java.awt.event.*;
import java.util.*;

public class SelectionCouleur4 implements PlugInFilter, MouseListener {
	
	ImagePlus img;
	ImagePlus img2; 
	ImageCanvas canvas;
	static Vector images = new Vector();
	
	int couleurVersNG(int couleur) {
		int r = new Color(couleur).getRed();
		int g = new Color(couleur).getGreen();
		int b = new Color(couleur).getBlue();
		
		int ng = (int)(0.3*r+0.59*g+0.11*b);
		r=ng;
		g=ng;
		b=ng;
		int ngcolor = new Color(r,g,b).getRGB();
		return ngcolor;
	}
	double distance(int couleur1, int couleur2) {
		int red1 = new Color(couleur1).getRed();
		int green1 = new Color(couleur1).getGreen();
		int blue1 = new Color(couleur1).getBlue();
		
		int red2 = new Color(couleur2).getRed();
		int green2 = new Color(couleur2).getGreen();
		int blue2 = new Color(couleur2).getBlue();
		
		double distance = Math.pow((red1 - red2), 2) + Math.pow((green1 - green2), 2)  + Math.pow((blue1 - blue2), 2);
		return distance;
				
	}
	public void run(ImageProcessor ip) {
		
		IJ.log("Comparaison de couleurs basée sur les valeurs RGB");
		IJ.log("Cliquez sur un pixel de l'image pour sélectionner une couleur");
		IJ.log("Les couleurs seront ajoutées à la nouvelle image");
		
		this.img2 = img.duplicate();
		//transformation du duplicata en nuances de gris
		ImageProcessor ip2 = img2.getProcessor();
		int width = ip2.getWidth();
		int height = ip2.getHeight();
		
		for (int j = 0;j < height; j++)
		{
			for(int i = 0; i < width; i++)
			{
				int value = ip2.getPixel(i,j);
				int new_value = couleurVersNG(value);
				ip2.putPixel(i,j,new_value);
				
			}
		}
		
		Integer id = new Integer(img.getID());
		if (images.contains(id)) {
			IJ.log("Already listening to this image");
			return;
		} else {
			ImageWindow win = img.getWindow();
			canvas = win.getCanvas();
			canvas.addMouseListener(this); //ajout du listener a l'image courante
			images.addElement(id);
		}
			
	}

	
	public int setup(String arg, ImagePlus img)
	{
		this.img = img;
		IJ.register(MouseListener.class);
		return DOES_RGB+NO_CHANGES;
	}
	
	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		
		ImageProcessor ip = img.getProcessor();
		ImageProcessor ip2 = img2.getProcessor();
		
		
		//coordonnées absolues
		int x = e.getX(); 
		int y = e.getY();
		//coordonnées du pixel de l'image
		int oX = canvas.offScreenX(x);
		int oY = canvas.offScreenY(y);
		
		
		int couleurRef=ip.getPixel(oX, oY); //recupere couleurRef sur l'image d'origine
		int r = new Color(couleurRef).getRed();
		int g = new Color(couleurRef).getGreen();
		int b = new Color(couleurRef).getBlue();
		IJ.log("Couleur :");
		IJ.log(Integer.toString(r)+","+Integer.toString(g)+","+Integer.toString(b)); //affichage de la couleur selectionnee
		
		int distanceMax = 10000;// seuil à choisir
		
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		//parcours le duplicata
		int count = 0;
		for (int j = 0;j < height; j++)
		{
			for(int i = 0; i < width; i++)
			{
				int value = ip.getPixel(i,j);
				double distance = distance(value,couleurRef); //calcule la distance sur l'image d'origine
				
				if (distance <= distanceMax)
				{
					count++;
					ip2.putPixel(i,j,value); //remplace la nuances de gris par la valeur originelle si elle est assez proche de la couleur choisie
				}
			}
		}
		IJ.log(Integer.toString(count)+" pixels affectés");
		img2.show();

	}	
	public void mouseEntered(MouseEvent e) {}
	
	public static String modifiers(int flags) {
		String s = " [ ";
		if (flags == 0) return "";
		if ((flags & Event.SHIFT_MASK) != 0) s += "Shift ";
		if ((flags & Event.CTRL_MASK) != 0) s += "Control ";
		if ((flags & Event.META_MASK) != 0) s += "Meta (right button) ";
		if ((flags & Event.ALT_MASK) != 0) s += "Alt ";
		s += "]";
		if (s.equals(" [ ]"))
 			s = " [no modifiers]";
		return s;
	}
}
