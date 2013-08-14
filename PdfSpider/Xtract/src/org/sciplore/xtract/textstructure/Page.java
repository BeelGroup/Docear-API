package org.sciplore.xtract.textstructure;

import java.util.Vector;

public class Page {
	public int number ;
	public String position ;
	public int top ;
	public int left ;
	public int height ;
	public int width ;
	
	private Vector<FontSpec> Fonts=null ;
	private Vector<Text> Texts=null ; 
	
	public Page(int numberv,String positionv,int topv,int leftv,int heightv,int widthv)
	{
		number=numberv ;
		position=positionv ;
		top=topv ;
		left=leftv ;
		height=heightv ;
		width=widthv ;
		
		Fonts= new Vector<FontSpec>() ;
		Texts= new Vector<Text>() ;
	}
	public void addFont(FontSpec fontSpecv)
	{
		Fonts.add(fontSpecv) ;		
	}
	public void addText(Text textv)
	{
		Texts.add(textv) ;		
	}
	public Vector<FontSpec> getFonts()
	{
		return Fonts;
	}
	public Vector<Text> getTexts()
	{
		return Texts ;
	}
	
	public FontSpec getFontSpecification(int fontId)
	{
		for (FontSpec fs:Fonts)
		{
			if (fs.getId()==fontId)
				return fs ;
		}
		return null ;
	}
	
	public String toString()
	{
		String Temp="" ;
			
		/*Temp+="number=" + number +	"\tposition="+position +
		"\ttop="+ top +"\tleft="+ left +"\theight="+ height 
		+"\twidth="+ width +"\n";
		*/
		
		for(FontSpec fo:Fonts)
		{
			Temp+=fo.toString() ;
		}
		
		/*for(Text tex:Texts)
		{
			Temp+=tex.toString() ;
		}*/
		
		return Temp;
	}

}
