-- object: sbitestkituser | type: ROLE --

CREATE ROLE sbitestkituser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;
	
-- ddl-end --

CREATE ROLE sysadmin WITH 
	SUPERUSER
	CREATEDB
	CREATEROLE
	INHERIT
	LOGIN
	REPLICATION
	PASSWORD :sysadminpwd;