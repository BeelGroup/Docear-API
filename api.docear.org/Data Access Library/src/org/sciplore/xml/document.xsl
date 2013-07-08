<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/documents">    
	<html xmlns="http://www.w3.org/1999/xhtml">
	    <head>		
		  <title>arxiv</title>
		</head>
	    
        <body>
            <table border="0" width="50%" cellpadding="2" cellspacing="2">
            <tbody>
		      <xsl:for-each select="document">		        
			    <tr>
			      <td>
			         <h2>
			         <xsl:number value="position()" format="1. "/>                                      
                     <xsl:value-of select="title"/>
                     </h2>
			      </td>
			    </tr>
			    <tr>
			      <td> 
			         <xsl:text>Author(s):  </xsl:text> 
                     <xsl:for-each select="authors/author">
                     <xsl:value-of select="name_complete"/>
                     <xsl:if test="not (position()=last())"> 
                        <xsl:text>and </xsl:text> 
                     </xsl:if>
                     </xsl:for-each>
                  </td>
			    </tr>
			    <tr>
			      <td>
			         <xsl:text>Abstract:  </xsl:text> 
			         <xsl:value-of select="abstract"/>
			      </td>
			    </tr>
			    <tr>
			      <td>
			         <a>
			            <xsl:attribute name="href"><xsl:value-of select="fulltexts/fulltext" /></xsl:attribute>
                        <xsl:value-of select="fulltexts/fulltext" />
                     </a>
			      </td>
			    </tr>				
              </xsl:for-each>
            </tbody>
            </table>
        </body>
    </html>
	</xsl:template>
</xsl:stylesheet>