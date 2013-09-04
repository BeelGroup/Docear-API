#!/usr/bin/env python
'''Created on Feb 3, 2013

@author: stefan
'''
from functools import wraps
import errno
import os
import signal
import _mysql
import sys
import smtplib

from email.mime.text import MIMEText

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")
email_lines = list()

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
def _test_recommendations():
    try: 
        _test_query('SELECT COUNT(*) FROM recommendations_documents_set S WHERE S.created>DATE_SUB(NOW(), INTERVAL 1 DAY);',
                    150,
                    'recommendation sets created')
        
        _test_query('SELECT COUNT(*) FROM recommendations_documents_set S WHERE S.delivered>DATE_SUB(NOW(), INTERVAL 1 DAY);',
                    50,
                    'recommendation sets delivered')
    except:
        email_lines.append('Unexpected error: {0}'.format(sys.exc_info()[0]))
        
@timeout(2, os.strerror(errno.ETIMEDOUT))
def _test_pdf_downloader():
    try:
        _test_query('SELECT COUNT(*) FROM google_document_queries WHERE created_date>DATE_SUB(NOW(), INTERVAL 1 DAY) ',
                    400,
                    'google document queries created: ')
        
        _test_query('SELECT COUNT(*) FROM google_document_queries WHERE query_date>DATE_SUB(NOW(), INTERVAL 1 DAY) ',
                    400,
                    'google document queries used')
        
        _test_query('SELECT COUNT(*) FROM document_xref WHERE last_attempt>DATE_SUB(NOW(), INTERVAL 1 DAY)',
                    1000,
                    'pdf files indexed')
    except:
        email_lines.append('Unexpected error: {0}'.format(sys.exc_info()[0]))
        
    
def _test_query(query, lower_bound, email_line_prefix):
    db.query(query)
    cursor = db.store_result()    
    row = cursor.fetch_row()
    if not row or int(row[0][0]) < lower_bound:
        email_lines.append(email_line_prefix+': '+row[0][0]+' ['+str(lower_bound)+']')
    

def send_email():
    text = '''This email was created using the DocearWatchdog which tests basic Docear services every 24 hours.
    All of the following lines describe suspicously low service work with the corresponding line count in the last 24 hours and the [lower bound] used to test the service:
    
    '''
    
    for line in email_lines:
        text += line + '\n'    
    
    msg = MIMEText(text)    
    msg['Subject'] = 'DocearWatchdog Warning'
    me = 'DocearServiceWatchdog@docear.org'
    msg['From'] = me
    to = 'core@docear.org'
    msg['To'] = to
    
    s = smtplib.SMTP('localhost')
    s.sendmail(me, to, msg.as_string())
    s.quit()
    

def _main():    
    _test_recommendations()
    _test_pdf_downloader()
    
    if len(email_lines) > 0:
        send_email()
    
if __name__ == '__main__':
    _main()
