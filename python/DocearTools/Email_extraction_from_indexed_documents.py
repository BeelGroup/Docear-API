'''
Created on Feb 19, 2013

@author: stefan
'''

import _mysql
import os

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")


def Main():
    f = open('/home/stefan/emailFileFilter.lst', 'w')
    
    db.query("SELECT hash FROM documents_pdfhash A JOIN document_xref B on (A.document_id=B.document_id) WHERE indexed=1")
    cursor = db.store_result()
    
    while True:
        row = cursor.fetch_row()
        if not row:
            break
        f.write(str(row[0][0]) + ".zip" + str(os.linesep))
    
    f.close()

if __name__ == '__main__':
    Main()