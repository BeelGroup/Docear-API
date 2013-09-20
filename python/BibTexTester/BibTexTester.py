'''
Created on Nov 5, 2012
This module tests if all brackets are closed correctly within an entry 

@author: stefan
'''

import sys

def main():
    in_entry = False
    entry_name = ''
    
    open_brackets = 0
    
    #file = open('/home/stefan/Downloads/library.bib')
    file = open(sys.argv[1])
    
    while True:
        line = file.readline()
        if not line:
            break
        
        if line.startswith('@'):
            in_entry = True
            entry_name = line
            if line.count(',') > 1:
                print ("more than one key in {0}".format(entry_name))
        
        if not in_entry:
            continue
        
        if line.startswith('@'):
            if open_brackets > 0:
                print("too many opening brackets in {0}".format(entry_name))
                return
            elif open_brackets < 0:
                print("too many closing brackets in {0}".format(entry_name))
                return
            open_brackets = 0
        
        open_brackets += line.count('{')
        open_brackets -= line.count('}')
        
        if open_brackets < 0:
            print ("too many closing brackets in {0}".format(entry_name))
        
        
    if open_brackets != 0:
        print ("too many closing/opening brackets in last entry")

if __name__ == "__main__":
    main()
    print("done")
