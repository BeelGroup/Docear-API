'''
Created on May 23, 2012
clicked recommendations on 1st, 2nd, 3rd, ... showing
@author: stefan
'''
import _mysql

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")
date_lo = '2013-07-01'
date_hi = '2014-02-01'

def _create_tmp_rec_clicked(gap, auto, approach):    
    query = """DROP TABLE IF EXISTS tmp_rec_base"""
    db.query(query)
    
    query = """CREATE TABLE tmp_rec_base (iteration INT, user_id BIGINT, fulltext_url_id BIGINT, delivered TIMESTAMP)"""
    db.query(query)
          
    query = """INSERT INTO tmp_rec_base select 1 as iteration, user_id, fulltext_url_id, min(delivered) as delivered from recommendations_documents R 
                JOIN user_models U ON (R.user_model_id = U.id) JOIN algorithms A ON (A.id = U.algorithm_id) 
                where user_id not in (1,2,27) and delivered between '"""+date_lo+"""' and '"""+date_hi+"""'"""
    if auto >= 0:
        query += " AND auto="+str(auto)
    
    if approach == 0:
         query += " AND data_element_type <> 2 AND approach = 1"
    elif approach == 1:    
        query += " AND approach = 2"
    elif approach == 2:
        query += " AND data_element_type = 2 AND approach = 1"
    query += """ group by user_id, fulltext_url_id"""
    
    db.query(query)
    
    for i in range(2,51):
        query = """INSERT INTO tmp_rec_base SELECT """+str(i)+""", R.user_id, R.fulltext_url_id, min(R.delivered) as delivered from recommendations_documents R 
                JOIN tmp_rec_base T ON (R.fulltext_url_id=T.fulltext_url_id AND R.user_id=T.user_id AND T.iteration="""+str(i-1)+""") 
                JOIN user_models U ON (R.user_model_id = U.id) JOIN algorithms A ON (A.id = U.algorithm_id)
                WHERE R.user_id not in (1,2,27) and R.delivered between '"""+date_lo+"""' and '"""+date_hi+"""' AND R.delivered > DATE_ADD(T.delivered, INTERVAL """+str(gap)+""" DAY)"""
        if auto >= 0:
            query += " AND auto="+str(auto)               
        if approach == 0:
             query += " AND data_element_type <> 2 AND approach = 1"
        elif approach == 1:    
            query += " AND approach = 2"
        elif approach == 2:
            query += " AND data_element_type = 2 AND approach = 1"               
        query += """ group by user_id, fulltext_url_id"""
        db.query(query)
        
    #CLEAN BASE DATA FROM SESSIONS WITH LESS THAN 10 DOCUMENTS   
    query= """DELETE A FROM tmp_rec_base A JOIN (select user_id, delivered, count(*) from recommendations_documents WHERE delivered between '"""+date_lo+"""' and '"""+date_hi+"""'
            GROUP BY user_id, delivered having count(distinct fulltext_url_id) <>10) B
            ON (A.user_id=B.user_id AND A.delivered = B.delivered)"""
    db.query(query)
    ##########################################################

    query = """DROP TABLE IF EXISTS tmp_rec_clicked"""
    db.query(query)
    query = """CREATE TABLE tmp_rec_clicked AS SELECT T.iteration, T.user_id, T.fulltext_url_id, T.delivered, R.clicked AS clicked FROM tmp_rec_base T
            JOIN recommendations_documents R 
            ON (T.user_id = R.user_id AND T.fulltext_url_id = R.fulltext_url_id AND T.delivered = R.delivered) 
            GROUP BY T.iteration, T.user_id, T.fulltext_url_id, T.delivered HAVING COUNT(*)=1"""
    db.query(query)

    
    
def _create_results(gap, auto, approach):    
    tablename = "tmp_rec_results_"+str(gap)+"_days"    
    if approach == 1:
        tablename += "_stereo"        
    elif approach == 2:
        tablename += "_bibcoup"
    if auto == 0:
        tablename += "_selected"
    elif auto == 1:
        tablename += "_auto"
        
    print("create table: "+tablename)
    
    query = """DROP TABLE IF EXISTS """+tablename
    db.query(query)    
    
    query = """CREATE TABLE """+tablename+""" (clicked INT, i1 INT, o1 INT"""
    for i in range(2,51):
        query += ",i" + str(i) + " INT"
        query += ",o" + str(i) + " INT"
    query += ")"    
    db.query(query)
    
    for i in range (-1,51):
        query = """INSERT INTO """+tablename+"""(clicked) VALUES ("""+str(i)+""")"""
        db.query(query)        
        
    for i in range (1,51):            
        ### CREATE TEMP TABLE
        query = """DROP TABLE IF EXISTS tmp_rec_counter"""
        db.query(query)
        query = """CREATE TABLE tmp_rec_counter AS SELECT DISTINCT LE.* FROM tmp_rec_clicked LE JOIN 
                (SELECT * FROM tmp_rec_clicked WHERE clicked IS NOT NULL AND iteration = """+str(i)+""") EQ
                ON (LE.user_id = EQ.user_id AND LE.fulltext_url_id = EQ.fulltext_url_id)
                WHERE LE.clicked IS NOT NULL AND LE.iteration<="""+str(i)
        db.query(query) 
        
        ### ADD DATA INTO i-COLUMNS
        query = """UPDATE """+tablename+""" T JOIN 
                (SELECT clicked, count(*) AS counter FROM
                (SELECT user_id, fulltext_url_id, count(*) as clicked FROM
                tmp_rec_counter C                
                GROUP BY user_id, fulltext_url_id) BASE
                GROUP BY clicked) R
                ON (T.clicked = R.clicked)
                SET i"""+str(i)+"""=R.counter"""
        db.query(query)
        
        query = """UPDATE """+tablename+""" T JOIN
            (SELECT count(*) AS counter FROM tmp_rec_clicked WHERE iteration = """+str(i)+""" AND clicked IS NULL) R 
            ON (T.clicked = 0)
            SET i"""+str(i)+""" = R.counter"""
        db.query(query)
        
        ### ADD DATA INTO o-COLUMNS
        query = """UPDATE """+tablename+""" T JOIN
                (SELECT CLICKED IS NOT NULL AS clicked, COUNT(A.fulltext_url_id) counter FROM tmp_rec_clicked A JOIN
                (SELECT user_id, delivered, fulltext_url_id FROM tmp_rec_clicked WHERE iteration="""+str(i)+""") B
                ON (A.user_id = B.user_id AND A.delivered = B.delivered)
                WHERE A.iteration = 1
                GROUP BY clicked IS NOT NULL) X
                ON (T.clicked = X.clicked)
                SET o"""+str(i)+""" = counter"""
        db.query(query)
        
        
        ### CREATE USER DISINCT DATASETS
        query = """UPDATE """+tablename+""" T JOIN
                (SELECT COUNT(DISTINCT user_id) AS u FROM tmp_rec_clicked WHERE iteration = """+str(i)+""") X                
                SET T.i"""+str(i)+"""=X.u, T.o"""+str(i)+"""=X.u
                WHERE clicked=-1"""
        db.query(query)
        
        query = "UPDATE "+tablename+" SET i"+str(i)+" = 0 WHERE i"+str(i)+" IS NULL"
        db.query(query)
        query = "UPDATE "+tablename+" SET o"+str(i)+" = 0 WHERE o"+str(i)+" IS NULL"
        db.query(query)
        
    
def _standard(approach):
    for gap in (0,1,3,7,14):
        _create_tmp_rec_clicked(gap, -1, approach)
        _create_results(gap, -1, approach)
        
    for gap in (0,1,3,7,14):
        _create_tmp_rec_clicked(gap, 0, approach)
        _create_results(gap, 0, approach)
        
    for gap in (0,1,3,7,14):
        _create_tmp_rec_clicked(gap, 1, approach)
        _create_results(gap, 1, approach)

if __name__ == '__main__':  
    _standard(0) #normal
    _standard(1) #stereotype
    _standard(2) #bibcoup
    