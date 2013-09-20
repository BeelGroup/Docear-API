'''
Created on May 23, 2012

@author: stefan
'''
import os
import zipfile
import _mysql

minimum_version_number = 90
zip_resources_path = '/srv/docear/mindmaps/'
target_folder = '/home/stefan/joeran'

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def _unzip_file(zip_file, user_id, filename):    
    f = os.path.join(zip_resources_path, zip_file)
    zobjf = None
   
    try:
        zobjf = zipfile.ZipFile(f)
    except Exception:
        print("error when opening: {0}".format(f))
    try:    
        for name in zobjf.namelist():
            if name.endswith('.mm'):
                outfile = None
                try:
                    target_folder_with_user = os.path.join(target_folder, str(user_id))
                    if not os.path.exists(target_folder_with_user):
                        os.makedirs(target_folder_with_user)
                    outfile = open(os.path.join(target_folder, str(user_id), filename), 'wb')
                    outfile.write(zobjf.read(name))
                except Exception as ex:
                    print ex
                finally:
                    outfile.close()
    except Exception:
        print("error when opening zip file objects from file: {0}".format(f))
            
def _write_properties_file(row):    
    try:
        revision_id = row[0][0]
        user_id = row[0][1]
        storage_path = row[0][2]
        filename = row[0][3]
        
        _unzip_file(storage_path, user_id, filename)
    except IndexError as ex:
        print("Error ({0}) when trying to fetch line for row: {1}".format(ex, row))
            
    
def _getCursor():
    #only latest mindmap revision per user
    db.query("""SELECT A.id, M.user, M.storage_path, M.filename FROM mindmaps M JOIN 
            (SELECT MAX(id) AS id, mindmap_id FROM mindmaps M GROUP BY mindmap_id) A
            ON (A.id = M.id)
            ORDER BY A.id DESC limit 1000""")        
    
    return db.store_result()

def _getTuple(cursor):
    return cursor.fetch_row()
    
def main():
    cursor = _getCursor()
    row = _getTuple(cursor)
    
    while row:
        _write_properties_file(row)
        row = _getTuple(cursor)
        
if __name__ == '__main__':
    main()