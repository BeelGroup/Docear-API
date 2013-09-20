#!/usr/bin/env python
'''
Created on Feb 20, 2013

@author: stefan
'''
from functools import wraps
import datetime
import errno
import os
import signal
import subprocess
import threading
import time
import urllib2


class CommandExecutorThread(threading.Thread):
    def __init__(self, cmd):
        threading.Thread.__init__(self)        
        self.cmd = cmd
        
    def run(self):
        print("run")   
        call = ["/bin/bash", "-c", self.cmd]
        p = subprocess.Popen(call, stdout=subprocess.PIPE)    
        for line in p.stdout:
            print line[0: -1]
        p.wait()
        
        return p.returncode
    

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


@timeout(5, os.strerror(errno.ETIMEDOUT))
def test_connection():    
    response = urllib2.urlopen('http://python.org')
    if len(response.read()) > 0:
        return True
    else:
        return False


def reconnect():
    print(str(datetime.datetime.now())+" --> RECONNECTING")
    
    i = 0
    while True:
        try:
            thread = CommandExecutorThread("/home/stefan/hidemyass/reconnect.sh")
            thread.daemon = True
            thread.start()
            print("wait")
            time.sleep(20)
            if test_connection():
                i=0
                print "connected"
                break
            else:
                print "not connected"
            if i>3:
                print "sleep"
                time.sleep(300)
            i+=1
        except Exception as ex:
            print ex
            pass


def Main():
    reconnect()

if __name__ == '__main__':
    Main()
