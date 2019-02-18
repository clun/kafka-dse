create keyspace demo_sdc WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

use demo_sdc;

CREATE TABLE stocks_ticks ( 
	symbol text,
	value_date timestamp,
	value double,
	PRIMARY KEY (symbol, value_date)
) WITH CLUSTERING ORDER BY (value_date DESC);

