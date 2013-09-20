'''
Created on Feb 3, 2013

@author: stefan
'''
from functools import wraps
import errno
import os
import signal
import urllib2

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

@timeout(2, os.strerror(errno.ETIMEDOUT))
def _test_webservice():
    resp = urllib2.urlopen("https://api.docear.org/applications/docear/versions/latest").read()    
    if 'version' in resp:
        pass
    else:
        raise Exception()

def _restart_webservice():
    print ('restarting')    
    os.system("/root/tomcat_restart.log")
        

def _main():
    try:
        _test_webservice()
    except:
        _restart_webservice()

if __name__ == '__main__':
    _main()