'''
Created on May 23, 2012

@author: stefan
'''
import os
import zipfile
import _mysql

minimum_version_number = 90
zip_resources_path = '/srv/docear/mindmaps/'
mm_parser_cache = '/home/stefan/work2/mindmap-parser/cache'
mm_parser_new = '/home/stefan/work2/mindmap-parser/new'

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def _unzip_file(zip_file, mm_file):    
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
                    outfile = open(os.path.join(mm_parser_cache, mm_file), 'wb')
                    outfile.write(zobjf.read(name))
                except Exception as ex:
                    print ex
                finally:
                    outfile.close()
    except Exception:
        print("error when opening zip file objects from file: {0}".format(f))
        
    try:
        os.rename(os.path.join(mm_parser_cache, mm_file), os.path.join(mm_parser_new, mm_file))
    except OSError as e:
        print(os.path.join(mm_parser_cache, mm_file) + " --> " +  os.path.join(mm_parser_new, mm_file))
        print e
        
def _write_properties_file(row):    
    try:
        revision_id = row[0][0]
        user_id = row[0][1]
        allow_content_research = row[0][2]
        allow_information_retrieval = row[0][3]
        allow_usage_research = row[0][4]
        allow_recommendations = row[0][5]
        revision_timestamp = row[0][6]
        map_type = row[0][7] 
        affiliation = row[0][8] 
        build = row[0][9]
        storage_path = row[0][10]
        
        mm_file = revision_id + ".mm"
        _unzip_file(storage_path, mm_file)
        
        outfile = open(os.path.join(mm_parser_new, mm_file + ".properties"), "w")
        outfile.write("user_id={0}\r\n".format(user_id))
        outfile.write("revision={0}\r\n".format(revision_id))
        outfile.write("allow_content_research={0}\r\n".format(allow_content_research))
        outfile.write("allow_information_retrieval={0}\r\n".format(allow_information_retrieval))
        outfile.write("allow_usage_research={0}\r\n".format(allow_usage_research))
        outfile.write("allow_recommendations={0}\r\n".format(allow_recommendations))
        outfile.write("revision_timestamp={0}\r\n".format(revision_timestamp))
        if map_type:
            outfile.write("map_type={0}\r\n".format(map_type))
        if affiliation:
            outfile.write("affiliation={0}\r\n".format(affiliation))            
        outfile.write("app_build={0}\r\n".format(build))
    
        outfile.close()
    except IndexError as ex:
        print("Error ({0}) when trying to fetch line for row: {1}".format(ex, row))
            
    
def _getCursor():
    #only latest mindmap revision per user
    db.query("""SELECT A.id, user, allow_content_research, allow_information_retrieval, allow_usage_research, allow_recommendations, revision, map_type, affiliation, build, storage_path 
        FROM mindmaps A
        JOIN (SELECT MAX(X.id) AS id, build from mindmaps X JOIN applications Y ON (X.application = Y.id) WHERE build >= 90 AND name='Docear' 
        AND (allow_content_research + allow_information_retrieval + allow_usage_research + allow_recommendations) > 0 
        GROUP BY user, mindmap_id) B
        ON (A.id = B.id)""")
    
#     #temporary for missing pdf_hashes
#     db.query("""SELECT A.id, user, allow_content_research, allow_information_retrieval, allow_usage_research, allow_recommendations, revision, map_type, affiliation, build, storage_path 
#             FROM mindmaps A JOIN applications B ON (A.application = B.id) WHERE build >= 90 AND name='Docear' 
#             AND A.id BETWEEN 187190 AND 205703
#             AND (allow_content_research + allow_information_retrieval + allow_usage_research + allow_recommendations) > 0
#             ORDER BY id""")    
    
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