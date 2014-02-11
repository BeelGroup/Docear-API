'''
Created on Feb 11, 2014

@author: stefan
'''

import sys
import _mysql

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def _get_user_id():
    if len(sys.argv) == 0:
        _print_usage()
        sys.exit(-1)
        
    user_id = 0 
    try:
        user_id = int(sys.argv[1])
    except Exception:
        _print_usage()
        sys.exit(-1)
                
    return user_id

def _delete_user_from_db(user_id):
    print('deleting Docear user with user_id: {0}'.format(user_id))
    
    db.query("SELECT id FROM recommendations_documents_set WHERE user_id={0}".format(user_id))
    cursor = db.store_result()
    
    while True:
        row = cursor.fetch_row()
        if not row:
            break        
        rec_doc_set_id = row[0][0]
#         db.query('DELETE FROM recommendations WHERE recommendations_documents_set_id={0}'.format(rec_doc_set_id))
#         db.query('DELETE FROM recommendations_documents_set WHERE id={0}'.format(rec_doc_set_id))
    
    
    
    
        
def _print_usage():
    print('usage: python RmDocearUser <id of user to remove>')

if __name__ == '__main__':
    user_id = _get_user_id()    
    _delete_user_from_db(user_id)
    