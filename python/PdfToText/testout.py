f = open('/home/stefan/pdftodownload.log', 'w')    

f.write('hello_world{0}'.format('!'))
f.flush()
f.write('hello_world')
f.flush()
f.close()