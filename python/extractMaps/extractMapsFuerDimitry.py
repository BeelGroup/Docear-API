'''
Created on May 23, 2012

@author: stefan
'''
import os
import zipfile
import _mysql


zip_resources_path = '/srv/docear/mindmaps/'
mm_parser_cache = '/home/stefan/dimitry/mindmap-parser/cache'
mm_parser_new = '/home/stefan/dimitry/mindmap-parser/new'

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def _unzip_file(zip_file):
    f = os.path.join(zip_resources_path, zip_file)
    zobjf = None
    
    mm_file = _write_properties_file(zip_file)
    if not mm_file:
        return
    
    try:
        zobjf = zipfile.ZipFile(f)
    except Exception:
        print("error when opening: {0}".format(f))
        
    for name in zobjf.namelist():
        if name.endswith('.mm'):
            outfile = None
            try:
                outfile = open(os.path.join(mm_parser_cache, mm_file), 'wb')
                outfile.write(zobjf.read(name))            
            finally:
                outfile.close()
    try:
        os.rename(os.path.join(mm_parser_cache, mm_file), os.path.join(mm_parser_new, mm_file))
    except OSError as e:
        print(os.path.join(mm_parser_cache, mm_file) + " --> " +  os.path.join(mm_parser_new, mm_file))
        print e
        
def _write_properties_file(path):            
    db.query("SELECT id, user, allow_content_research, allow_information_retrieval, allow_usage_research, allow_recommendations, revision FROM mindmaps WHERE storage_path='" + path + "' AND (allow_content_research +" \
             "allow_information_retrieval + allow_usage_research) > 0")
    r = db.store_result()
    
    row = r.fetch_row()
    try:
        revision_id = row[0][0]
        user_id = row[0][1]
        allow_content_research = row[0][2]
        allow_information_retrieval = row[0][3]
        allow_usage_research = row[0][4]
        allow_recommendations = row[0][5]
        revision_timestamp = row[0][6]
        
        mm_file = revision_id + ".mm"
        
        outfile = None
        outfile = open(os.path.join(mm_parser_new, mm_file + ".properties"), "w")
        outfile.write("user_id={0}\r\n".format(user_id))    
        outfile.write("revision={0}\r\n".format(revision_id))
        outfile.write("allow_content_research={0}\r\n".format(allow_content_research))
        outfile.write("allow_information_retrieval={0}\r\n".format(allow_information_retrieval))
        outfile.write("allow_usage_research={0}\r\n".format(allow_usage_research))
        outfile.write("allow_recommendations={0}\r\n".format(allow_recommendations))
        outfile.write("revision_timestamp={0}\r\n".format(revision_timestamp))
    
        outfile.close()
    except IndexError:
#        print("Error when trying to fetch line for: {0}".format(path))
        return None
    except IOError:        
        return None
        
    return mm_file
    
        
def main():
    for root, dirs, files in os.walk(zip_resources_path):
        path = root[len(zip_resources_path):]
        for filename in files:      
            _unzip_file(os.path.join(path, filename))            
        
if __name__ == '__main__':
    main()