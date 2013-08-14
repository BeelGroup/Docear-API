package org.sciplore.xtract.textstructure;

public class FontSpec {
	private int id;
	private int size;
	
	private String text="";
	
	private long totalNumberOfUsage=0;
	private long totalNumberOfCharachters=0 ;
	private float averagecharachtersPerSegment=0 ;
	private String color;
	
      public FontSpec(int idv,int sizev,String familyv,String colorv)
      {
    	  id=idv ;
    	  size=sizev ;
    	  color = colorv;
      }
      public void addSegmentInfo(int numChars)
      {
    	  totalNumberOfCharachters+=numChars ;
    	  totalNumberOfUsage+=1 ;
    	  averagecharachtersPerSegment=(float)totalNumberOfCharachters/totalNumberOfUsage ;
      }
      
      public long getTotalNumberOfUsage()
      {
    	return totalNumberOfUsage ;  
      }
      
      public long getTotalNumberOfCharachters()
      {
    	return totalNumberOfCharachters ;  
      }
      
      public void addText(String str)
      {
    	 text+="\n" + str ;  
      }

      public float getAveragecharachtersPerSegment()
      {
    	return averagecharachtersPerSegment ;  
      }
      
      public int getId()
      {
    	  return id ;
      }
      
      public String getColor()
      {
    	  return color;
      }
      
      public String toString()
      {
/*    	  String temp="<fontspec id=\"" +
    	  		id + "\" size=\""+size+"\" family=\""+family+"\"  " +
  				"color=\""+ color+"\" totalNumberOfUsage=\""+totalNumberOfUsage+"\"" +
  				"totalNumberOfCharachters=\""+totalNumberOfCharachters+"\"  " +
  				"averagecharachtersPerSegment=\""+averagecharachtersPerSegment+"\"   ></fontspec>\n" ;
*/
    	  String temp="<fontspec id=\"" +
	  		id + "\" size=\""+size+">" ;
    	  temp+=text+"\n" ;
    	  return temp ;
      }

      public int getSize() {
		return size;
	}
}
