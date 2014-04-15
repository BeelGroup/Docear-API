'''
Created on May 08, 2013
clicked recommendations on 1st, 2nd, 3rd, ... showing
@author: stefan
'''
import _mysql

db=_mysql.connect(host="localhost", port=3306, user="docear", passwd="ppLmQ8esxJtTGQtz", db="docear")

def main():  


    query = """CREATE TABLE IF NOT EXISTS `tmp_rec_users` (
          `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
          `user_id` bigint(20) unsigned DEFAULT NULL,
          `sets_total` int(10) unsigned DEFAULT NULL,
          `recs_total` int(10) unsigned DEFAULT NULL,
          `clicks_total` int(10) unsigned DEFAULT NULL,
          `ctr` double unsigned DEFAULT NULL,
          `sets_total_app_ge65` int(10) unsigned DEFAULT NULL,
          `recs_total_app_ge65` int(10) unsigned DEFAULT NULL,
          `clicks_total_app_ge65` int(10) unsigned DEFAULT NULL,
          `ctr_app_ge65` double unsigned DEFAULT NULL,
          `recommendations_active` tinyint(3) unsigned DEFAULT NULL,
          `recommendations_active_mthd2` tinyint(3) unsigned DEFAULT NULL,
          `docear_started_x_days_ago` int(10) unsigned DEFAULT NULL,
          `registered_x_days_ago` int(10) unsigned DEFAULT NULL,
          `received_recs_x_days_ago` int(10) unsigned DEFAULT NULL,
          `stated_docear_on_x_days` int(10) unsigned DEFAULT NULL,
          `mindmaps_total` int(10) unsigned DEFAULT NULL,
          `papers_total` int(10) unsigned DEFAULT NULL,
          `revisions_total` int(10) unsigned DEFAULT NULL,
          `active` int(10) unsigned DEFAULT NULL COMMENT '1 for users who have started docear within the past 30 days and registered at least 30 days earlier',
          `max_mindmap_application_id` int(10) unsigned DEFAULT NULL,          
          PRIMARY KEY (`id`),
          KEY `FK__rec_users_id` (`user_id`),
          CONSTRAINT `FK__rec_users_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;"""
    db.query(query)


      
    query = """TRUNCATE tmp_rec_users"""
    db.query(query)
    
    query = """INSERT INTO tmp_rec_users(user_id) SELECT id FROM users WHERE id NOT IN (1, 2, 27)""";
    db.query(query)
    
    #update users with sets_total    
    query = """UPDATE tmp_rec_users T JOIN 
            (SELECT user_id, count(*) AS counter FROM recommendations_documents_set S 
            WHERE user_id NOT IN (1, 2, 27) AND S.delivered IS NOT NULL AND (offline_evaluator IS NULL || offline_evaluator = 0) GROUP BY user_id) X
            ON (X.user_id=T.user_id)
            SET T.sets_total=X.counter""";
    db.query(query)

    #insert recs_total
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter from recommendations_documents_set S JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET recs_total = Y.counter;"""
    db.query(query)

    
    #update clicks_total
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter 
            FROM recommendations_documents_set S 
            JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL
            AND R.clicked IS NOT NULL
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.clicks_total = Y.counter"""
    db.query(query)
    
    #update ctr
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter 
            FROM recommendations_documents_set S 
            JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.ctr = X.clicks_total / Y.counter"""
    db.query(query)
    
    #update sets_total_app_ge65
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter 
            FROM recommendations_documents_set S 
            WHERE S.delivered IS NOT NULL
            AND S.application_id >= 65
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.sets_total_app_ge65 = Y.counter"""
    db.query(query)
    
    #update recs_total_app_ge65
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter from recommendations_documents_set S JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL 
            AND S.application_id >= 65
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET recs_total_app_ge65 = Y.counter"""
    db.query(query)
    
    #update clicks_total_app_ge65
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter 
            FROM recommendations_documents_set S 
            JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL
            AND S.application_id >= 65
            AND R.clicked IS NOT NULL
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.clicks_total_app_ge65 = Y.counter"""
    db.query(query)
    
    #update ctr_app_ge65
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT S.user_id, count(*) AS counter 
            FROM recommendations_documents_set S 
            JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.application_id >= 65
            AND S.delivered IS NOT NULL
            GROUP BY S.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.ctr_app_ge65 = X.clicks_total_app_ge65 / Y.counter"""
    db.query(query)  
    
    #clean clicks_total and ctr columns from NULL values
    query = """UPDATE tmp_rec_users X SET X.clicks_total = 0 WHERE X.clicks_total IS NULL"""
    db.query(query)
    query = """UPDATE tmp_rec_users X SET X.clicks_total_app_ge65 = 0 WHERE X.clicks_total_app_ge65 IS NULL"""
    db.query(query)
    query = """UPDATE tmp_rec_users X SET X.ctr = 0 WHERE X.ctr IS NULL"""
    db.query(query)
    query = """UPDATE tmp_rec_users X SET X.ctr_app_ge65 = 0 WHERE X.ctr_app_ge65 IS NULL"""
    db.query(query)
    query = """UPDATE tmp_rec_users X SET X.sets_total_app_ge65 = 0 WHERE X.sets_total_app_ge65 IS NULL"""
    db.query(query) 
    query = """UPDATE tmp_rec_users X SET X.recs_total_app_ge65 = 0 WHERE X.recs_total_app_ge65 IS NULL"""
    db.query(query)
    
    #update docear_started_x_days_ago
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT user_id, TIMESTAMPDIFF(DAY, MAX(time), now()) AS days 
            FROM users_applications 
            WHERE user_id IS NOT NULL GROUP BY user_id) Y 
            ON (X.user_id = Y.user_id)
            SET X.docear_started_x_days_ago = Y.days"""
    db.query(query)
    
    #update registered_x_days_ago
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT id AS user_id, TIMESTAMPDIFF(DAY, registrationdate, now()) AS days 
            FROM users) Y 
            ON (X.user_id = Y.user_id)
            SET X.registered_x_days_ago = Y.days"""
    db.query(query)
    
    #update received_recs_x_days_ago   
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT X.user_id, TIMESTAMPDIFF(DAY, MAX(S.delivered), NOW()) AS days
            FROM tmp_rec_users X 
            JOIN recommendations_documents_set S ON (S.user_id = X.user_id)
            WHERE S.delivered IS NOT NULL
            GROUP BY X.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.received_recs_x_days_ago = Y.days"""
    db.query(query)
       
    
    #update stated_docear_on_x_days
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT A.user_id, COUNT(DISTINCT(DATE(A.time))) AS counter 
            FROM users_applications A 
            WHERE user_id IS NOT NULL 
            GROUP BY A.user_id) Y
            ON (X.user_id = Y.user_id)
            SET X.stated_docear_on_x_days = Y.counter"""
    db.query(query)
    
    #update mindmaps_total and revisions_total
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT M.user AS user_id, COUNT(DISTINCT(M.mindmap_id)) AS mm_counter, COUNT(*) AS rev_counter
            FROM mindmaps M 
            WHERE M.user IS NOT NULL 
            GROUP BY M.user) Y
            ON (X.user_id = Y.user_id)
            SET X.mindmaps_total = Y.mm_counter, X.revisions_total = Y.rev_counter"""
    db.query(query)
    
    #update papers_total and revisions_total
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT user AS user_id, COUNT(distinct pdfhash) AS paper_counter from mindmaps_pdfhash P JOIN mindmaps M ON (P.mindmap_id = M.id) GROUP BY M.user) Y
            ON (X.user_id = Y.user_id)
            SET X.papers_total = Y.paper_counter"""
    db.query(query)
    
    #update active
    query = """UPDATE tmp_rec_users X 
            SET X.active = 1
            WHERE (X.registered_x_days_ago - X.docear_started_x_days_ago) > 30
            AND X.docear_started_x_days_ago <= 30;"""    
    db.query(query)
    query = """UPDATE tmp_rec_users X
            SET X.active = 0
            WHERE X.active IS NULL"""
    db.query(query)
    
    #update max_mindmap_application_id
    query = """UPDATE tmp_rec_users X JOIN 
            (SELECT user AS user_id, MAX(application) AS application_id FROM mindmaps GROUP BY user) Y ON (X.user_id = Y.user_id)
            SET X.max_mindmap_application_id = Y.application_id"""
    db.query(query)
    
def update_recommendations_active():
    #update recommendations_active
    query = """UPDATE tmp_rec_users X SET X.recommendations_active=1"""
    db.query(query)
    query = """UPDATE tmp_rec_users X 
            JOIN users U ON (X.user_id = U.id)
            SET X.recommendations_active = 0
            WHERE U.allow_recommendations = 0"""
    db.query(query)
    query = """UPDATE tmp_rec_users X JOIN 
            (SELECT T.user_id, COUNT(DISTINCT U.id) AS counter FROM tmp_rec_users T
            JOIN (SELECT user_id, MAX(delivered) AS delivered FROM recommendations_documents_set GROUP BY user_id) S ON (S.user_id = T.user_id)
            JOIN users_applications U ON (U.user_id = T.user_id)
            WHERE (T.received_recs_x_days_ago - T.docear_started_x_days_ago)>7
            AND U.time > S.delivered
            GROUP BY T.user_id HAVING COUNT(DISTINCT DATE(U.time)) > 1) Y
            ON (X.user_id = Y.user_id)
            SET X.recommendations_active=0"""
    db.query(query)
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT M.user AS user_id, MAX(revision) as rev_date FROM mindmaps M JOIN tmp_rec_users T ON (T.user_id = M.user) 
            WHERE T.recommendations_active = 0) A
            ON (A.user_id = X.user_id)
            JOIN
            (SELECT user_id, MAX(time) AS time FROM users_applications S 
            GROUP BY user_id) B
            ON (B.user_id = X.user_id)
            SET X.recommendations_active = 1
            WHERE A.rev_date > DATE_SUB(B.time, INTERVAL 7 DAY)"""
        
    
    #methdod 2
    query = """UPDATE tmp_rec_users X SET X.recommendations_active_mthd2=1"""
    db.query(query)
    query = """UPDATE tmp_rec_users X 
            JOIN users U ON (X.user_id = U.id)
            SET X.recommendations_active_mthd2 = 0
            WHERE U.allow_recommendations = 0"""
    db.query(query)
    query = """UPDATE tmp_rec_users X
            SET X.recommendations_active_mthd2 = 0
            WHERE (X.received_recs_x_days_ago - X.docear_started_x_days_ago)>7"""            
    db.query(query)
    query = """UPDATE tmp_rec_users X JOIN
            (SELECT M.user AS user_id, MAX(revision) as rev_date FROM mindmaps M JOIN tmp_rec_users T ON (T.user_id = M.user) 
            WHERE T.recommendations_active_mthd2 = 0) A
            ON (A.user_id = X.user_id)
            JOIN
            (SELECT user_id, MAX(time) AS time FROM users_applications S 
            GROUP BY user_id) B
            ON (B.user_id = X.user_id)
            SET X.recommendations_active_mthd2 = 1
            WHERE A.rev_date > DATE_SUB(B.time, INTERVAL 7 DAY)"""
    
def update_recommendations_documents_set():
    #rec_amount_current
    query = """UPDATE recommendations_documents_set X JOIN 
            (SELECT S.id, count(*) AS c FROM recommendations_documents_set S JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id) 
            GROUP BY S.id) Y
            ON (X.id = Y.id)
            SET X.rec_amount_current = Y.c"""
    db.query(query)    
    
    #rec_amount_should
    query = """UPDATE recommendations_documents_set X SET X.rec_amount_should = 10"""
    db.query(query)
    
    #rec_original_rank*
    query = """UPDATE recommendations_documents_set X JOIN
            (SELECT S.id, MIN(R.original_rank) AS min_rank, MAX(R.original_rank) AS max_rank, SUM(R.original_rank)/COUNT(*) AS avg_rank
            FROM recommendations_documents_set S JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id) 
            GROUP BY S.id) Y
            ON (X.id = Y.id)
            SET X.rec_original_rank_min = Y.min_rank, X.rec_original_rank_max = Y.max_rank, X.rec_original_rank_avg = Y.avg_rank"""
    db.query(query)
    
    #rec_clicked_*
    query = """UPDATE recommendations_documents_set X JOIN
            (SELECT S.id, COUNT(*) AS c FROM recommendations_documents_set S JOIN recommendations_documents R ON (R.recommendations_documents_set_id = S.id)
            WHERE S.delivered IS NOT NULL
            AND R.clicked IS NOT NULL
            GROUP BY S.id) Y
            ON (X.id = Y.id) 
            SET X.rec_clicked_count = Y.c, X.rec_clicked_ctr = Y.c/X.rec_amount_current"""
    db.query(query)
    query = """UPDATE recommendations_documents_set S SET S.rec_clicked_count=0, S.rec_clicked_ctr=0 WHERE S.delivered IS NOT NULL AND S.rec_clicked_count IS NULL"""
    db.query(query)
      
def rename_table_id_columns(tablename, alias):
    columns = ""
    
    query = """SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '{0}'""".format(tablename);
    db.query(query)
    cursor = db.store_result()
       
    while True:
        row = cursor.fetch_row()  
              
        if not row:
            break;
        
        for column in row[0]:
            columns += alias+'.'+column+' AS '+alias+'_'+column
            columns += ','
    
    return columns[:-1]          
      
def update_user_person_table():
    query = """DROP TABLE IF EXISTS tmp_user_person"""
    db.query(query)
    
    target_query = "CREATE TABLE tmp_user_person AS SELECT "
    target_query += rename_table_id_columns('users', 'U') + ', ' + rename_table_id_columns('persons', 'P') + ',' + rename_table_id_columns('tmp_rec_users', 'T') + ',' + rename_table_id_columns('recommendations_users_settings', 'S')
    target_query += " FROM users U "
    target_query += " JOIN persons P ON (U.person_id = P.id)" 
    target_query += " JOIN tmp_rec_users T ON (T.user_id = U.id)"
    target_query += " JOIN recommendations_users_settings S ON (S.user_id = U.id)"
    
    print target_query
    db.query(target_query)
    
if __name__ == '__main__':  
    main()
    update_recommendations_active()
    update_recommendations_documents_set()
    update_user_person_table()
