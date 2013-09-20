#!/usr/local/bin/python
# -*- coding: cp1252 -*-
'''
Created on Jun 6, 2013

@author: stefan
'''

import os
import sys
import re
import string
import unicodedata

author_counts = dict()
author_names = dict()

def remove_accents(text):
    u = unicode(text, "cp1252")
    return ''.join(x for x in unicodedata.normalize('NFKD', u) if x in string.ascii_letters).lower()

def get_author_count(text):
    author_count = text.count(",")
    if author_count == 1 and text.find(" and ")>=0:
        author_count = 2
    return author_count
    
def get_page_count(text):
    pps  = re.findall("([0-9]+–[0-9]+)", text)
    
    page_count = "";
    
    for pp in pps:
        try:
            tokens = pp.split("–")            
            page_count = str(int(tokens[1])-int(tokens[0])+1)            
        except:
            pass
        
    return page_count    

def get_year(text):    
    tokens =  re.findall(" ([0-9]{4,4})[^–]", text)
    if tokens:
        return tokens[-1]
    else:
        return None
    
def get_venue_type(text):
    text = text.lower()
    if text.find(" in ") == 0:
        return "proceedings"
    elif text.find(" journal ") >= 0:
        return "journal"
    elif text.find(", vol.") >= 0:
        return "journal"
    else:
        return "misc"

def fill_authors_list(text):
    global author_counts
    global author_names
    
    authors = text[text.find("]")+1:].split(",")
    for author in authors:
        if len(author.strip()) == 0:
            authors.remove(author)
            
    if len(authors)==1:
        authors = authors[0].split("and")
    for author in authors:        
        and_pos = author.find(" and ")
        if and_pos >= 0:
            author = author[and_pos+5:]
        author = author.strip()
        if len(author) == 0:
            continue
        family_name = remove_accents(author.split(" ")[-1])
        
        author_names[family_name] = author
        author_counts[family_name] = author_counts.get(family_name, 0)+1        

def parse_line(line):    
    front = line.split("“")[0]
    rear = line.split("”")[-1]

    fill_authors_list(front)
    
    ref_id = front[:front.find("]")+1]
    author_count = get_author_count(front)    
    year = get_year(rear)
    page_count = get_page_count(rear)
    venue_type = get_venue_type(rear)
    
    return str(ref_id)+";"+str(author_count)+";"+str(year)+";"+str(page_count)+";"+venue_type
    
def main():
    global authors_counts
    global author_names
    
    infile = open(sys.argv[1])
    outfile_papers = open(os.path.splitext(os.path.basename(sys.argv[1]))[0]+"_papers.txt", "w")
    outfile_authors = open(os.path.splitext(os.path.basename(sys.argv[1]))[0]+"_authors.txt", "w")
    
    while True:
        line = infile.readline()
        if not line:
            break
        
        outfile_papers.write(parse_line(line)+"\r\n")        

    for family_name in author_counts:        
        outfile_authors.write(str(author_names[family_name])+";"+str(author_counts[family_name])+"\r\n")
        
    outfile_papers.close()
    outfile_authors.close()        
    
if __name__ == '__main__':
    main()
