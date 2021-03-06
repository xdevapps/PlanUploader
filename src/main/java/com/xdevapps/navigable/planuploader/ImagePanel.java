/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xdevapps.navigable.planuploader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;

/**
 *
 * @author athang213
 */
public class ImagePanel extends JPanel{
    
    public class Pair {
        int x, y;
        
        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
        
    private ArrayList<Polygon> polygons;
    private ArrayList<Pair> points;
    private ArrayList<Pair> centres;
    private ArrayList<ArrayList<Pair>> edges;
    private ArrayList<Pair> path;
    private Pair selectedPt;
    private Pair origin;
    private int isPathSrcSet;
    
    public Pair getOrigin() {
        return origin;
    }
    
    private BufferedImage image;

    private final Color rColor = Color.decode("#f2180d");
    
    private Color regionColor = new Color((float)rColor.getRed()/255.f,
            (float)rColor.getGreen()/255.f,
            (float)rColor.getBlue()/255.f,
            0.65f);
    
    public ImagePanel(BufferedImage image) {
        this.image = image;
        this.points = new ArrayList<>();
        this.centres = new ArrayList<>();
        this.polygons = new ArrayList<>();
        this.edges = new ArrayList< ArrayList<Pair> >(0);
        this.path = new ArrayList<>();
        selectedPt = null;
        origin = new Pair(0, 0);
        isPathSrcSet = 0;
    }
    
    @Override
    public void paint(Graphics graphics) {
        if(image.getWidth() > image.getHeight())
            graphics.drawImage(image, 0, 0, 1280, 1280 * image.getHeight() / image.getWidth(), this);
        
        ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
        
        ((Graphics2D)graphics).setStroke(new BasicStroke(2));
        graphics.setColor(Color.BLUE);
        
        graphics.drawLine(0+origin.x,origin.y,1280+origin.x,origin.y);
        graphics.drawLine(origin.x,0+origin.y,origin.x,1280 * image.getHeight() / image.getWidth()+origin.y);
        
        ((Graphics2D)graphics).setStroke(new BasicStroke(2));
        graphics.setColor(Color.RED);
        for(Pair point : points) {
            graphics.drawLine(point.x-3, point.y-3, point.x+3, point.y+3);
            graphics.drawLine(point.x-3, point.y+3, point.x+3, point.y-3);           
        }
        
        ((Graphics2D)graphics).setStroke(new BasicStroke(1.5f));
        graphics.setColor(Color.blue);
        for(Polygon polygon : polygons) {
            graphics.setColor(regionColor);
            graphics.fillPolygon(polygon);
            graphics.setColor(Color.RED);
            graphics.drawPolygon(polygon);
        }
        
        graphics.setColor(Color.CYAN);
        for(int j = 0;j<edges.size();j++) {
            int temp = edges.get(j).size()-1;
            
            for(int i=0;i<edges.get(j).size()-1;i++)
            {    
                Pair p1 = new Pair(edges.get(j).get(i).x,edges.get(j).get(i).y);
                Pair p2 = new Pair(edges.get(j).get(i+1).x,edges.get(j).get(i+1).y);
                graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            }    
        }
        
        for(int i=0;i<path.size()-1;i++)
        {    
                Pair p1 = new Pair(path.get(i).x,path.get(i).y);
                Pair p2 = new Pair(path.get(i+1).x,path.get(i+1).y);
                graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
        }    
        
        graphics.setColor(Color.BLUE);
        for(Pair centre : centres) {
            graphics.fillOval(centre.x, centre.y, 5, 5);
        }
        
        if(selectedPt != null){
            ((Graphics2D)graphics).setStroke(new BasicStroke(2.5f));
            graphics.setColor(Color.ORANGE);
            graphics.drawOval(selectedPt.x-2,selectedPt.y-2,8,8);
        }   
        
        
        graphics.dispose();
    }    

    public int isCloseTo(int x, int y) {
        for(int i=0; i<centres.size(); i++) {
            if(Math.abs(centres.get(i).x - x) < 15 && Math.abs(centres.get(i).y - y) < 15)
                return i;
        }
        return -1;
    }
    
    public void drawPoint(int x, int y) {
        System.out.println("MouseClicked(" + x + "," + y + ")");
        points.add(new Pair(x,y));
    }
    
    public void drawPolygon() {
        int[] xArr = new int[points.size()];
        int[] yArr = new int[points.size()];
        for(int i=0; i<points.size(); i++){
            xArr[i] = points.get(i).x;
            yArr[i] = points.get(i).y;
        }
        polygons.add(new Polygon(xArr,yArr,points.size()));
        getCentroid(xArr, yArr, points.size());
        points.clear();
        System.out.println("POINTS CLEARED : " + points.size());
    }
    
    public void drawEdge(int x1, int y1) {
         System.out.println("MouseClicked(" + x1 + "," + y1 + ")");
        int index = isCloseTo(x1,y1);
        System.out.println("Index: "+index);
        if(isPathSrcSet == 0 && index != -1)
        {
            path.add(new Pair(centres.get(index).x,centres.get(index).y));
            isPathSrcSet = 1;
        }
        else if(isPathSrcSet == 1 && index != -1)
        {
            path.add(new Pair(centres.get(index).x,centres.get(index).y));
            ArrayList<Pair> temp = path;
            edges.add(temp);
            path = new ArrayList<Pair>();
            isPathSrcSet = 0;
        }
        else if(isPathSrcSet == 1 && index == -1)
        {
            path.add(new Pair(x1,y1));
        }
    }
    
    void getCentroid(int[] x, int[] y, int length) {
        int X = 0;
        int Y = 0;
        
        for(int i=0; i < length; i++) {
            int x1 = x[i];
            int y1 = y[i];
            
            X += x1;
            Y += y1;
        }
        
        X /= length;
        Y /= length;
        
        System.out.println("CENTROID : " + X + " , " + Y);
        
        centres.add(new Pair(X, Y));
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    public void reset() {
        this.points.clear();
        this.polygons.clear();
        this.centres.clear();
        this.edges.clear();
        selectedPt = null;
        
    }
    
    public byte [] writeToFile() throws IOException{
            Exporter ex = new Exporter();
            ex.writeVertices(centres,origin.x,origin.y);
            ex.writeEdges(edges,centres);
            ex.flush();
            return ex.exportBytes();
    }
    
  /*  public void deletePolygon(int x,int y) {
    	int i,j;
    	ArrayList<Integer> temp = new ArrayList<>(0); 
    	for(i = 0;i < polygons.size();i++) {
    		if(polygons.get(i).contains(x,y)) {
    			System.out.println("Size : "+edges.size());
    			for(j=0;j<edges.size();j++)
    			{	
    				System.out.println("i : "+i+" Src : "+edges.get(j).x+" Dest : "+edges.get(j).y+" j : "+j);
    				if(edges.get(j).x == i || edges.get(j).y == i)
    				{
    					temp.add(j);
    				}
    			}
    			Collections.sort(temp, Collections.reverseOrder());
    			
    			for(int temp1 : temp)
    			{	
    				edges.remove(temp1);
    			}
    			for(Pair p : edges)
    			{
    				System.out.println(p.x +" "+ p.y);
    			}
    			
    			polygons.remove(i);
    			//System.out.println("Hello1");
    			centres.remove(i);
    			for(Pair p : edges)
    			{
    				if(p.x > i)
    				{
    					p.x--;
    				}
    				
    				if(p.y > i)
    				{
    					p.y--;
    				}
    				//System.out.println(p.x +" "+ p.y);
    			}
    			for(Pair p : edges)
    			{
    				System.out.println(p.x +" "+ p.y);
    			}
                        selectedPt = -1;
    			break;
    		}
    	}
    	
    }*/

    
    public void highlightPt(int x,int y){
        int index = isCloseTo(x,y);
        selectedPt = new Pair(0,0);
        if(index == -1){  
            selectedPt.x = x;
            selectedPt.y = y;
        }
        else
        {
            selectedPt.x = centres.get(index).x;
            selectedPt.y = centres.get(index).y;
        }
        repaint();
    }
    
    public void setAxis(int x,int y){
        origin.x = x;
        origin.y = y;
        repaint();
    }
    
}
