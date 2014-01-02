'''
Created on Dec 22, 2013

@author: stefan
'''
import _mysql, os

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")


def write_data_file(cursor):
    f = open('user_data.mat', 'w')
#     f.write(str(row[0][0]) + ".zip" + str(os.linesep))
    X_data = str()
    
    X_column_count = 0
    X_row_count = 0
    
    while True:
        row = cursor.fetch_row()
        if not row:
            break
        

        if not X_column_count:
		      X_column_count = len(row[0])

        X_row_count += 1
        
        for token in row[0]:
            X_data += str(token) + ' '
                    
        X_data += str(os.linesep)

    X_header = '# name: data' + str(os.linesep)
    X_header += '# type: matrix' + str(os.linesep)
    X_header += '# rows: ' + str(X_row_count) + str(os.linesep)
    X_header += '# columns: ' + str(X_column_count) + str(os.linesep)
    
    f.write(X_header + X_data)


def Main():
    query = """SELECT U.id, DATEDIFF(NOW(), registrationdate) AS days_registered, COUNT(DISTINCT mindmap_id) AS mm_count, COUNT(*) AS rev_count, DATEDIFF(MAX(revision),MIN(revision)) AS mm_max_days_diff, SUM(M.filesize) AS size
FROM users U
JOIN mindmaps M ON (M.user = U.id AND M.revision>='2013-06-01' AND U.allow_recommendations=1)
GROUP BY U.id"""
    
    db.query(query)
    cursor = db.store_result() 
    
    write_data_file(cursor)    
        

if __name__ == '__main__':  
    Main()
