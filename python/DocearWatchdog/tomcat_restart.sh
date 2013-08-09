#!/bin/bash

date >> /root/tomcat_restart.log
/opt/apache-tomcat-7.0.25/bin/shutdown.sh
sleep 10
kill -9 $(ps ax | grep java.*apache-tomcat-7.0.25 | grep -v grep | sed 's/^[^0-9]*\([0-9]*\).*$/\1/')
rm -r /opt/apache-tomcat-7.0.25/work/Catalina/api.docear.org/*
/opt/apache-tomcat-7.0.25/bin/startup.sh

cd /home/stefan/hidemyass && python HideMyAssReconnecter.py >> /home/stefan/hidemyass/hma.log

if [ "$1" == "-v" ]
	then echo "Please test if the tomcat is running again by using https://api.docear.org/applications/docear/versions/latest" | cat - /root/tomcat_restart.log | mail -s "WebserviceMonitor: restarting tomcat on fks01" core@docear.org;
fi
