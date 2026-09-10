CREATE DATABASE mosip_sbitestkit
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_sbitestkit IS 'database to store test runs in sbi test kit';

\c mosip_sbitestkit postgres

DROP SCHEMA IF EXISTS sbitestkit CASCADE;
CREATE SCHEMA sbitestkit;
ALTER SCHEMA sbitestkit OWNER TO postgres;
ALTER DATABASE mosip_sbitestkit SET search_path TO sbitestkit,pg_catalog,public;