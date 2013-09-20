'''
Created on Nov 5, 2012
This module tests if all brackets are closed correctly within an entry 

@author: stefan
'''
import os

def main():
    s = set()
    
    in_entry = False
    entry_name = ''
    
    open_brackets = 0
    
    f = open('/home/stefan/work/testset/testdata.csv')
    
    while True:
        line = f.readline()
        if not line:
            break
        
        s.add(str(line.split('|')[0]) + '-' + str(line.split('|')[1]))
    
    print s
    
    f.close()
    
    
    for f in os.listdir('/home/stefan/work/testset/pdfs/'):        
        if not (f.split('.')[0]) in s:
            print f
            os.remove(os.path.join('/home/stefan/work/testset/pdfs', f))
            

if __name__ == "__main__":
    main()
    print("done")
