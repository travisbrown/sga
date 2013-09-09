import sys
sys.path.insert(0, '/home/ubuntu/code/sga/search/service/flask')

from sga_search import sga_search as application

""" This goes to apache2 config file. In ubuntu 12.04: /etc/apache2/sites-available/default

<VirtualHost *>
    ServerName sga_search

    WSGIDaemonProcess sga_search user=www-data group=www-data threads=5
    WSGIScriptAlias / /var/www/sga_search/wsgi/sga_search.wsgi

    <Directory /var/www/sga_search/sga_search>
        WSGIProcessGroup sga_search
        WSGIApplicationGroup %{GLOBAL}
        Order deny,allow
        Allow from all
    </Directory>
</VirtualHost>

"""