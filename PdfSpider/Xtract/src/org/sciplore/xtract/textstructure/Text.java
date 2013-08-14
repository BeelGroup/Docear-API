package org.sciplore.xtract.textstructure;

import java.util.Vector;

public class Text {

	private int top ;
	private int left ;
	private int width ;
	public int height ;
	private int fontId ;
	private Vector<TextSegment> TextSegments=null ;
		
	public Text (int topv,int leftv,int widthv,int heightv,int fontv)
	{
		top=topv ;
		left=leftv ;
		width=widthv ;
		height=heightv ;
		fontId=fontv ;
		TextSegments=new Vector<TextSegment>() ;
	}

	public int getFontId() {
		return fontId;
	}

	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public int getWidth() {
		return width;
	}

	public void addTextSegment(TextSegment texSeg)
	{
		TextSegments.add(texSeg) ;
	}
	
	public String getText()
	{
		String temp="" ;
		for (TextSegment text:TextSegments)
		{
			temp+=text.getText() ;
		}
		return temp ;
	}
	public Vector<TextSegment> getTextSegments()
	{
		return TextSegments ;
	}	
	public String toString()
	{
		String Temp="" ;
		
	/*	Temp="Top:" + top + "\tLeft:" + left 
		+"\tWidth:" + width + "\tHeight:" +height
		+"\tFont:" + fontId + "\n" ;
		
		for(TextSegment texSeg:TextSegments)
		{
			Temp+=texSeg.toString() ;
		}	
	*/	
		return Temp;
	}
}
