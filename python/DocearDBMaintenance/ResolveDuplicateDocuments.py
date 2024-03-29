'''
Created on Sep 20, 2013

@author: stefan
'''

import sys
import _mysql
from datetime import datetime
from datetime import timedelta

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")


def resolveDuplicate(doc_id, cleantitle):
    db.query("SELECT D.id FROM documents D WHERE D.cleantitle = '"+cleantitle+"' ORDER BY D.id")
    cursor = db.store_result()        
    row = cursor.fetch_row()
     
    min_doc_id = row[0][0]
#     try:
    while True:
        row = cursor.fetch_row()
        if not row:
            break
         
        duplicate_doc_id = int(row[0][0])            
         
        db.query("UPDATE citations C SET C.cited_document_id="+str(min_doc_id)+" WHERE C.cited_document_id="+str(duplicate_doc_id))
        db.query("UPDATE documents_persons DP SET DP.document_id="+str(min_doc_id)+" WHERE DP.document_id="+str(duplicate_doc_id))
        db.query("UPDATE documents_pdfhash DP SET DP.document_id="+str(min_doc_id)+" WHERE DP.document_id="+str(duplicate_doc_id))
        db.query("UPDATE documents_bibtex DB SET DB.document_id="+str(min_doc_id)+" WHERE DB.document_id="+str(duplicate_doc_id))
        db.query("DELETE FROM recommendations_documents WHERE document_id="+str(duplicate_doc_id))
        db.query("DELETE FROM document_xref WHERE document_id="+str(duplicate_doc_id))
        db.query("DELETE R FROM fulltext_url F JOIN recommendations_documents R ON (R.fulltext_url_id=F.id) WHERE F.document_id="+str(duplicate_doc_id))
        db.query("DELETE FROM fulltext_url WHERE document_id="+str(duplicate_doc_id))
        db.query("DELETE FROM documents WHERE id="+str(duplicate_doc_id))
        db.query("DELETE FROM tmp_duplicates WHERE document_id="+str(min_doc_id))
#     except:
#         sys.stderr.write("error for id: {0} --> {0}\n".format(min_doc_id, sys.exc_info()[0]))

def Main():
    start_time = datetime.now()
    db.query("SELECT COUNT(*) FROM tmp_duplicates")
    cursor = db.store_result()    
    row_count = cursor.fetch_row()[0][0]
    
    db.query("SELECT document_id, cleantitle FROM tmp_duplicates")
    cursor = db.store_result()
    
    counter = 0
    
    while True:
        row = cursor.fetch_row()  
              
        if not row:
            break;
        
        counter += 1
        if (counter % 100 == 0):
            print str(counter)+' of '+ str(row_count) +' execution time so far: ' + str(datetime.now() - start_time)
        
        doc_id = int(row[0][0])
        cleantitle = str(row[0][1])
        
        resolveDuplicate(doc_id, cleantitle)
    

if __name__ == '__main__':
    Main()