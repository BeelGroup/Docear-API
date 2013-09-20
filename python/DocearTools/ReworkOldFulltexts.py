'''
Created on Jan 23, 2013

@author: stefan
'''
import fnmatch
import os

#document_path = '/srv/docear/documents'
document_path = '/home/stefan/documents'

def walk_dir():
    for root, dirs, files in os.walk(document_path):
        if len(files) == 0 and len(dirs)==0:
            os.rmdir(root)
                  
        for file in files:
            path = os.path.join(root, file)
            tokens = file.split('.')            
            if tokens[-1] == 'err':                
                os.rename(path, os.path.join(root, '.'.join(tokens[0: -1])))
            elif tokens[-1] not in ('zip', 'pdf', 'cite'):
                print tokens[-1]
        

def main():
    walk_dir()

if __name__ == '__main__':
    main()
    