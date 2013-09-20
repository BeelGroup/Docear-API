#!/usr/bin/env python
'''
Created on Feb 20, 2013

@author: stefan
'''

import subprocess
import time
import datetime

def run(cmd):
    call = ["/bin/bash", "-c", cmd]
    p = subprocess.Popen(call, stdout=subprocess.PIPE)
    for line in p.stdout:
        print line[0: -1]
    p.wait()
    
    return p.returncode

def reconnect():

    print(str(datetime.datetime.now())+" --> RECONNECTING")
    run("java -jar stefans-reconnecter.jar")

def query_worker():    
    ret_code = run('java -jar googleQueryWorker.jar sleep=800 maxDocs=20 noDataWait=2 timeout=30 >> "$(date +"%Y-%d-%m_%R:%S")".log')
    if ret_code == 9999:
        print("NOTHING TODO -> WAITING...")
        #sleep for 5 minuites
        time.sleep(600)
    else:
        time.sleep(1800)
        reconnect()

def Main():
    reconnect()
    while True:        
        query_worker()
     

if __name__ == '__main__':
    Main()
