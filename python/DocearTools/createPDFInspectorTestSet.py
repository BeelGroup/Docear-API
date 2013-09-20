'''
Created on May 23, 2012

@author: stefan
'''
from os import path
import _mysql
import os
import shutil
import urllib2
import urlparse

from functools import wraps
import errno
import os
import signal

class TimeoutError(Exception):
    pass

def timeout(seconds=10, error_message=os.strerror(errno.ETIME)):
    def decorator(func):
        def _handle_timeout(signum, frame):
            raise TimeoutError(error_message)

        def wrapper(*args, **kwargs):
            signal.signal(signal.SIGALRM, _handle_timeout)
            signal.alarm(seconds)
            try:
                result = func(*args, **kwargs)
            finally:
                signal.alarm(0)
            return result

        return wraps(func)(wrapper)

    return decorator


number = 0
db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")
meta = open("testdata.csv", "w")

def _get_random_papers():     
    db.query("""SELECT fid, url, title FROM (select id AS fid, document_id, url
    from fulltext_url order by rand() limit 1500) A 
    JOIN documents B ON (A.document_id = B.id)""")
#    db.query("""SELECT fid, url, title FROM (select id AS fid, document_id, url
#    from fulltext_url limit 14) A 
#    JOIN documents B ON (A.document_id = B.id)""")
    
    cursor = db.store_result()
    
    while True:        
        row = cursor.fetch_row()
        if not row:
            break
        _process_row(row)
    
def _process_row(row):
    global number
    fid = row[0][0]
    url = row[0][1]
    title = row[0][2]
                
    try:
        number += 1
        _download(url, str(number).zfill(4)+"-"+str(fid)+".pdf")        
        write_csv(fid, title)
    except Exception as ex:
        print(ex)
        number -= 1
        print("can't download {0}".format(url))
        
@timeout(120, os.strerror(errno.ETIMEDOUT))
def _download(url, fileName=None):
    def getFileName(url,openUrl):
        if 'Content-Disposition' in openUrl.info():
            # If the response has Content-Disposition, try to get filename from it
            cd = dict(map(
                lambda x: x.strip().split('=') if '=' in x else (x.strip(),''),
                openUrl.info()['Content-Disposition'].split(';')))
            if 'filename' in cd:
                filename = cd['filename'].strip("\"'")
                if filename: return filename
        # if no filename was found above, parse it out of the final URL.
        return os.path.basename(urlparse.urlsplit(openUrl.url)[2])
        
    r = None
    try:
        r = urllib2.urlopen(urllib2.Request(url))
        fileName = fileName or getFileName(url,r)
        with open(fileName, 'wb') as f:
            shutil.copyfileobj(r,f)    
    finally:
        if r:
            r.close()
    
def write_csv(fid, title):
    meta.write(str(number).zfill(4) + "|" + str(fid) + "|" + title + "\n")
        
def main():
   _get_random_papers()
   meta.close()
        
if __name__ == '__main__':
    main()