#!/usr/bin/env python

'''
Created on Feb 11, 2014

@author: stefan
'''

import sys
import _mysql
import os.path
import shutil

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def _get_user_id():
    if len(sys.argv) == 0:
        _print_usage()
        sys.exit(-1)
    
    user_id = -1    
    user = str()    
    
    try:
        user = sys.argv[1]
    except Exception:            
        _print_usage()
        sys.exit(-1)        
    
    query = 'SELECT U.id, U.username, P.name_first, P.name_middle, P.name_last, C.uri FROM users U JOIN persons P ON (U.person_id = P.id) JOIN contacts C ON (C.person_id = P.id) WHERE '
    try:
        user_id = int(user)     
    except Exception:
        user = str(sys.argv[1])
    
    if user_id > 0:
        query += "U.id='{0}'".format(user_id)
    elif '@' in user:    
        query += "C.uri='{0}'".format(user)
    else:
        query += "U.username='{0}'".format(user)
    
    db.query(query)
    cursor = db.store_result()
    row = cursor.fetch_row()
    if not row:
        print("user not found -- aborting!")
        sys.exit(1)
        
    user_id = row[0][0]
    username = row[0][1]
    name_first = row[0][2]
    name_middle = row[0][3]
    name_last = row[0][4]
    email = row[0][5]        
        
    if _ask_to_delete(user_id, username, name_first, name_middle, name_last, email):
        print('running')
        return user_id
    else:
        print('aborting!')
        sys.exit(1)
        
def _ask_to_delete(user_id, username, name_first, name_middle, name_last, email):    
    print("USER DETAILS?")
    print("user_id: {0}".format(user_id))
    print("username: {0}".format(username))
    print("name_first: {0}".format(name_first))
    print("name_middle: {0}".format(name_middle))
    print("name_last: {0}".format(name_last))
    print("email: {0}".format(email))
    
    if raw_input('Do you really want to delete this user? [y/N]: ') != 'y':
        return False
    
    return True
        

def delete_user_from_db(user_id):
    print('deleting Docear user_id with user_id: {0}'.format(user_id))
    
    _remove_from_tmp_tables(user_id)
    _remove_from_recommendations(user_id)
    _remove_from_others(user_id)
    _remove_mindmaps(user_id)
    _remove_from_main_tables(user_id)
    
    _remove_mindmap_files(user_id)
    
def _remove_from_recommendations(user_id):
    # delete old recommendation entries
    db.query('DELETE FROM recommendations_documents WHERE user_id = {0}'.format(user_id))
        
    # delete newer recommendation entries
    db.query("SELECT id FROM recommendations_documents_set WHERE user_id = {0}".format(user_id))
    cursor = db.store_result()    
    while True:
        row = cursor.fetch_row()
        if not row:
            break
        db.query('DELETE FROM recommendations_documents WHERE recommendations_documents_set_id = {0}'.format(row[0][0]))
        db.query('DELETE FROM recommendations_documents_set WHERE id = {0}'.format(row[0][0]))
        
    
    db.query('DELETE FROM rec_stats WHERE user_id = {0}'.format(user_id))    
    db.query('DELETE FROM recommendations_evaluator_chache WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM recommendations_users_settings WHERE user_id = {0}'.format(user_id))
    
def _remove_from_tmp_tables(user_id):
    db.query('DELETE FROM tmp_clustering_mindmaps WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM tmp_rec_users WHERE user_id = {0}'.format(user_id))
    
def _remove_from_others(user_id):
    db.query('DELETE FROM alerts WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM documents_bibtex_users WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM feedback WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM keywords WHERE user_id = {0}'.format(user_id))     
    db.query('DELETE FROM log WHERE user_id = {0}'.format(user_id))    
    db.query('DELETE FROM newsletter WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM publist WHERE user_id = {0}'.format(user_id)) 
    db.query('DELETE FROM usage_stats WHERE user_id = {0}'.format(user_id)) 
    db.query('DELETE FROM user_activation WHERE user_id = {0}'.format(user_id)) 
    db.query('DELETE FROM user_password_request WHERE user_id = {0}'.format(user_id))
    db.query('DELETE FROM users_applications WHERE user_id = {0}'.format(user_id))
    
def _remove_mindmaps(user_id):
    db.query("SELECT id FROM mindmaps WHERE user = {0}".format(user_id))
    cursor = db.store_result()    
    while True:
        row = cursor.fetch_row()
        if not row:
            break
        db.query('DELETE FROM tmp_clustering_mindmaps WHERE mm_latest_rev_id = {0}'.format(row[0][0]))
        db.query('DELETE FROM mindmaps_pdfhash WHERE mindmap_id = {0}'.format(row[0][0]))
        db.query('DELETE FROM mindmap_nodes WHERE mindmaprevision_id = {0}'.format(row[0][0]))
        db.query('DELETE FROM mindmaps WHERE id = {0}'.format(row[0][0]))
    
def _remove_from_main_tables(user_id):
    db.query('DELETE FROM users WHERE id = {0}'.format(user_id))

def _remove_mindmap_files(user_id):
    f = '/srv/docear/mindmaps/{0}'.format(user_id)
    if (os.path.isdir(f)):
        print ("deleting user's mind maps")
        shutil.rmtree(f, ignore_errors=False, onerror=None)
    else:
        print ("user has no mind maps to delete")    
        
def _print_usage():
    print('usage: RmDocearUser [user | user_name | email-address]')

if __name__ == '__main__':
    user = _get_user_id()    
    delete_user_from_db(user)
    print('Done.')
    