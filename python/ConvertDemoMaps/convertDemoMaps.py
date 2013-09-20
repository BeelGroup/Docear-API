'''
Created on Aug 23, 2012

@author: stefan
'''

from optparse import OptionParser


def read_config():
    usage = "usage: %prog [mm-files]"
    parser = OptionParser(usage=usage)
    full = False
#    parser.add_option("-f", "--full", dest="full", action="store_true",
#                      help="download the full manga")
#    parser.add_option("-u", "--update", dest="update", action="store_true",
#                      help="update the manga (process backwards until hitting a "\
#                           "a processed chapter")
#    parser.add_option("-o", "--offset", dest="offset", default="0",
#                      help="skip <offset> chapters")
#    parser.add_option("-n", "--number", dest="number", default=None,
#                      help="download <number> chapters")
#    parser.add_option("-c", "--chunk-size", dest="chunk_size", default=100,
#                      help="split the mobi in <chunk-size> chapter chunks, default=100")

    [options, args] = parser.parse_args()
    print(options)
    if len(args) != 1:
        parser.error("please pass a folder or a url to the application")
    if options.full and options.update:
        parser.error("aptions -f and -u are mutually exclusive")

if __name__ == '__main__':
    read_config()