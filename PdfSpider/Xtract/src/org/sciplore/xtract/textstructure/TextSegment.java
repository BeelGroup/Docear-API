package org.sciplore.xtract.textstructure;

//this class holds the information of one segment of text 
//which has the same decoration
//for example the following text contains 3  TextSegment
//<text font="6">Hirsch's  <i>h</i>-type  indices</text>
//1st segment contains "Hirsch's  "  with none decoration
//2nd segment contains "h"  with italic decoration
//3rd segment contains "-type  indices"
public class TextSegment {	
	public enum Decoration{
		none,
		bold,
		italic,
		boldItalic
	}	
	private String text="" ;
	public TextSegment(String Textv,Decoration decorv) {
    	text=Textv ;
	}
	public int getSize()
	{
		return text.length() ;
	}
    public String toString()
    {
    	String temp="" ;
    	/*
    	switch (Decor)
    	{
	    	case none: 
	    		temp=text;
	    		break ;
	    	case bold:
	    		temp="<b>"+text+"</b>" ;
	    		break ;
	    	case italic:
	    		temp="<i>"+text+"</i>" ;
	    		break ;	    		
	    	case boldItalic:
	    		temp="<b><i>"+text+"</i></b>" ;
	    		break ;
    		default:
    			temp=text ;
    			break ;
    	}    
    	*/
    	return temp + "\n";
    }

	public String getText() {
		return text;
	}
    
	public void setText(String val) {
		text=val;
	}

}
