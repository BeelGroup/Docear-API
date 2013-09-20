'''
Created on Oct 8, 2012

@author: stefan
'''

import os
import subprocess
import time
import zipfile
import zlib

document_path = '/srv/docear/documents'
compression = zipfile.ZIP_DEFLATED

def main():
    
    log = open('/home/stefan/tools/pdftodownload.log', 'w')    
    
    i = 0
    for root, dirs, files in os.walk(document_path):
        for file in files:            
            if file.lower().endswith(".pdf"):
                try:
                    f = os.path.join(root, file)                    
                    p = subprocess.Popen(["pdftotext", "-q",f ,f + ".txt"])
                    p.wait()
                    if p.returncode:
                        log.write('error converting file "{0}"\n'.format(f))
                        log.flush()
                        os.rename(f, f + ".err")
                    else:                        
                        zf = zipfile.ZipFile(f + ".zip", "w")
                        try:
                            zf.write(f + ".txt", compress_type=compression)
                        finally:
                            zf.close()                        
                        os.remove(f)
                        os.remove(f + ".txt")
                    if i%10 == 0:
                        time.sleep(0.2)
                    if i%100 == 0:
                        log.write(str(i)+"\n")
                        log.flush()                        
                       
                except Exception as ex:
                    log.write ('error converting file "{0}" to text'.format(f))
                    log.writelines(ex)
                    log.write ('\n')     
                    log.flush()               
                i += 1            
        
    log.write("done after {0} files \n".format(str(i)))
    log.close()

if __name__ == "__main__":
    main()
