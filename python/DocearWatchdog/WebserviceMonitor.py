#!/usr/bin/env python
'''
Created on Feb 3, 2013

@author: stefan
'''
from functools import wraps
from email.mime.text import MIMEText
import errno
import os
import signal
import urllib2
import smtplib
import time

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

@timeout(10, os.strerror(errno.ETIMEDOUT))
def _request_graphdb():
    try:            
        resp = urllib2.urlopen("http://localhost:7474")            
        return True        
    except:
        print("http://localhost:7474 not available")
        return False

def _test_graphdb():
    error_msg = ''
    valid = False
    for i in range(5):
        time.sleep(3)
        valid = valid or _request_graphdb()
        if valid:
            break
        
    if not valid:
        error_msg += '- GraphDB is not available at http://localhost:7474 on fks01!' + os.linesep
        print('- GraphDB is not available at http://localhost:7474 on fks01!' + os.linesep)
    
    if not os.path.exists('/home/stefan/work/mindmap-parser'):
        error_msg += '- The folder md02:/home/stefan/work is not mounted at fks01:/home/stefan/work!' + os.linesep
        print('- The folder md02:/home/stefan/work is not mounted at fks01:/home/stefan/work!' + os.linesep)
        
    if len(error_msg) > 0:
        send_graph_db_not_available_email(error_msg)
    
        
def send_graph_db_not_available_email(error_msg):
    text = '''Recommendations cannot be computed and new mind maps cannot be indexed right now! The reason(s) ''' + os.linesep + os.linesep + error_msg 
        
    msg = MIMEText(text)    
    msg['Subject'] = 'GraphDB not available warning'
    me = 'WebserviceMonitor@docear.org'
    msg['From'] = me
    #to = 'core@docear.org'
    to = 'core@docear.org'
    msg['To'] = to
    
    smtp_host = 'mail.ovgu.de'
    smtp_port = 587
    server = smtplib.SMTP()
    server.connect(smtp_host,smtp_port)
    server.ehlo()
    server.starttls()
    server.login("genzmehr", "FarbKopiererV49")    
    
    server.sendmail(me, to, msg.as_string())
    server.quit()
    

@timeout(2, os.strerror(errno.ETIMEDOUT))
def _test_webservice():
    resp = urllib2.urlopen("https://api.docear.org/applications/docear/versions/latest").read()
    if 'version' in resp:
        pass
    else:
        raise Exception()

def _restart_webservice():
    os.system("/root/tomcat_restart.sh")


def _main():
    _test_graphdb()
    
    try:
        _test_webservice()        
    except:
        _restart_webservice()
    
if __name__ == '__main__':
    _main()
